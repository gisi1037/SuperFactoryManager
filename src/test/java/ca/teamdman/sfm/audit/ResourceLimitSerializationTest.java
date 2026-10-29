package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistrySimple;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.ast.InputStatement;
import ca.teamdman.sfml.ast.OutputStatement;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.ast.ResourceIdSet;
import ca.teamdman.sfml.ast.ResourceIdentifier;
import ca.teamdman.sfml.ast.ResourceLimit;
import ca.teamdman.sfml.ast.ResourceLimits;
import ca.teamdman.sfml.ast.ResourceQuantity;
import ca.teamdman.sfml.ast.Statement;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class ResourceLimitSerializationTest {

    private static final Item FIRST = new Item(), SECOND = new Item(), ESCAPED = new Item();
    private static final String ESCAPED_ID = "serialization_test:odd\\path\"part\ninside\tend";

    static {
        try {
            Field field = RegistrySimple.class.getDeclaredField("registryObjects");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> names = (Map<String, Object>) field.get(Item.itemRegistry);
            names.put("serialization_test:first", FIRST);
            names.put("serialization_test:second", SECOND);
            names.put(ESCAPED_ID, ESCAPED);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    @Test
    void literalQuantityRetentionAndEachFlagsSurviveBothIoDefaultsAndNumericExtremes() {
        for (String quantity : Arrays.asList("0", "0 EACH", "7 EACH", Long.MAX_VALUE + " EACH")) {
            for (String retention : Arrays.asList("0", "0 EACH", "3 EACH", Long.MAX_VALUE + " EACH")) {
                assertEquivalent(
                    roundTrip(
                        "EVERY 20 TICKS DO INPUT " + quantity
                            + " RETAIN "
                            + retention
                            + " item:serialization_test:first OR item:serialization_test:second FROM EACH source "
                            + "OUTPUT "
                            + quantity
                            + " RETAIN "
                            + retention
                            + " item:*:* TO EMPTY SLOTS IN EACH dest END"));
            }
        }
        assertEquivalent(roundTrip("EVERY 20 TICKS DO INPUT FROM source OUTPUT TO dest END"));
        assertEquivalent(roundTrip("EVERY 20 TICKS DO INPUT * FROM source OUTPUT * TO dest END"));
    }

    @Test
    void dynamicLimitsUseTheSameEmitterAndKeepSelectorsTagsAndExclusions() {
        assertEquivalent(
            roundTrip(
                "STATE $quantity = 7 STATE $reserve = 3 EVERY 20 TICKS DO "
                    + "INPUT ($quantity) EACH RETAIN ($reserve) EACH item:serialization_test:first "
                    + "OR item:serialization_test:second WITHOUT TAG blocked* EXCEPT item:other:* FROM EACH source "
                    + "OUTPUT ($quantity) EACH RETAIN (0) EACH fluid:*:water TO EMPTY SLOTS IN EACH dest END"));
    }

    @Test
    void withAndWithoutPredicatesRemainAttachedToTheSameDisjunction() {
        for (String predicate : Arrays.asList("WITH TAG ingot*", "WITHOUT TAG ore:blocked/part")) {
            Program[] pair = roundTrip(
                "EVERY 20 TICKS DO INPUT 4 RETAIN 1 item:serialization_test:first "
                    + "OR item:serialization_test:second "
                    + predicate
                    + " FROM source END");
            assertEquivalent(pair);
            ResourceLimit before = limits(statements(pair[0]).get(0)).getLimits()
                .get(0);
            ResourceLimit after = limits(statements(pair[1]).get(0)).getLimits()
                .get(0);
            assertEquals(
                1,
                limits(statements(pair[1]).get(0)).getLimits()
                    .size());
            assertEquals(
                2,
                after.getResourceIds()
                    .getIdentifiers()
                    .size());
            assertEquals(
                before.getWith()
                    .isNegated(),
                after.getWith()
                    .isNegated());
            assertEquals(
                before.getWith()
                    .getTagMatcher()
                    .getPatternString(),
                after.getWith()
                    .getTagMatcher()
                    .getPatternString());
        }
    }

    @Test
    void oneDisjunctionKeepsOneSharedQuotaRatherThanBecomingTwoLimits() throws Exception {
        Program[] pair = roundTrip(
            "EVERY 20 TICKS DO INPUT 3 serialization_test:first OR serialization_test:second "
                + "FROM source OUTPUT TO dest END");
        assertEquivalent(pair);
        for (Program program : pair) {
            Fixture fixture = new Fixture();
            fixture.source.setInventorySlotContents(0, new ItemStack(FIRST, 3));
            fixture.source.setInventorySlotContents(1, new ItemStack(SECOND, 3));
            execute(program, fixture);
            assertEquals(3, count(fixture.source));
            assertEquals(3, count(fixture.dest));
        }
    }

    @Test
    void literalInputReserveAndOutputStockCapKeepTheirTransferSemantics() throws Exception {
        Program[] pair = roundTrip(
            "EVERY 20 TICKS DO INPUT 7 RETAIN 3 serialization_test:first FROM source "
                + "OUTPUT 5 RETAIN 6 serialization_test:first TO dest END");
        assertEquivalent(pair);
        for (Program program : pair) {
            Fixture fixture = new Fixture();
            fixture.source.setInventorySlotContents(0, new ItemStack(FIRST, 10));
            fixture.dest.setInventorySlotContents(0, new ItemStack(FIRST, 4));
            execute(program, fixture);
            assertEquals(8, count(fixture.source));
            assertEquals(6, count(fixture.dest));
        }
    }

    @Test
    void emptySlotOutputRetainsTheRestrictionAndActualDestinationSlotChoice() throws Exception {
        Program[] pair = roundTrip("EVERY 20 TICKS DO INPUT FROM source " + "OUTPUT 2 TO EMPTY SLOTS IN dest END");
        assertEquivalent(pair);
        for (Program program : pair) {
            Fixture fixture = new Fixture();
            fixture.source.setInventorySlotContents(0, new ItemStack(FIRST, 5));
            fixture.dest.setInventorySlotContents(0, new ItemStack(FIRST, 60));
            execute(program, fixture);
            assertEquals(60, fixture.dest.getStackInSlot(0).stackSize);
            assertEquals(2, fixture.dest.getStackInSlot(1).stackSize);
            assertEquals(3, count(fixture.source));
        }
    }

    @Test
    void quotedResourceEscapesRoundTripThroughTheRealParserWithoutChangingLiteralApiValues() {
        ResourceIdentifier original = ResourceIdentifier.fromString(ESCAPED_ID);
        String encoded = original.toString();
        assertTrue(encoded.contains("\\\\"));
        assertTrue(encoded.contains("\\\""));
        assertTrue(encoded.contains("\\n"));
        assertTrue(encoded.contains("\\t"));
        Program[] pair = roundTrip("EVERY 20 TICKS DO INPUT " + encoded + " FROM source END");
        ResourceIdentifier parsed = limits(statements(pair[1]).get(0)).getLimits()
            .get(0)
            .getResourceIds()
            .getIdentifiers()
            .get(0);
        assertEquals(original.getNamespace(), parsed.getNamespace());
        assertEquals(original.getName(), parsed.getName());
        assertEquals(original.getMetadata(), parsed.getMetadata());
        assertTrue(original.matchesItem(new ItemStack(ESCAPED)));
        assertTrue(parsed.matchesItem(new ItemStack(ESCAPED)));
        assertFalse(parsed.matchesItem(new ItemStack(FIRST)));
        // Parsing source escapes is separate from the unchanged literal-construction API.
        assertEquals(
            "literal\\nname",
            ResourceIdentifier.fromString("ns:literal\\nname")
                .getName());
    }

    @Test
    void wildcardAliasesAndItemFluidMetadataDomainsKeepMatchingBehavior() {
        for (String raw : Arrays.asList(
            "*",
            ".*",
            "item:*:*",
            "item:.*:.*",
            "fluid:*:water",
            "serialization_test:first:7",
            "serialization_test:first:*",
            "#ore:ingot*")) {
            ResourceIdentifier original = ResourceIdentifier.fromString(raw);
            ResourceIdentifier parsed = ResourceIdentifier.fromSourceLiteral(original.toString());
            assertEquals(original.getType(), parsed.getType(), raw);
            assertEquals(original.getMetadata(), parsed.getMetadata(), raw);
            assertEquals(original.getTag(), parsed.getTag(), raw);
            if (original.getTag() == null) for (ItemStack sample : Arrays
                .asList(new ItemStack(FIRST, 1, 7), new ItemStack(FIRST, 1, 8), new ItemStack(SECOND)))
                assertEquals(original.matchesItem(sample), parsed.matchesItem(sample), raw);
            assertEquivalent(roundTrip("EVERY 20 TICKS DO INPUT " + original + " FROM source END"));
        }
        assertEquals("\"*\"", ResourceIdentifier.MATCH_ALL.toString());
    }

    private static Program[] roundTrip(String source) {
        Program first = program(source);
        Program second = program(first.toString());
        assertEquals(first.toString(), second.toString());
        return new Program[] { first, second };
    }

    private static void assertEquivalent(Program[] pair) {
        List<Statement> first = statements(pair[0]), second = statements(pair[1]);
        assertEquals(first.size(), second.size());
        for (int i = 0; i < first.size(); i++) {
            assertEquals(
                first.get(i)
                    .getClass(),
                second.get(i)
                    .getClass());
            ResourceLimits a = limits(first.get(i)), b = limits(second.get(i));
            assertEquals(
                a.getLimits()
                    .size(),
                b.getLimits()
                    .size());
            for (int j = 0; j < a.getLimits()
                .size(); j++) {
                ResourceLimit left = a.getLimits()
                    .get(j),
                    right = b.getLimits()
                        .get(j);
                assertQuantity(
                    left.getLimit()
                        .getQuantity(),
                    right.getLimit()
                        .getQuantity());
                assertQuantity(
                    left.getLimit()
                        .getRetention(),
                    right.getLimit()
                        .getRetention());
                assertIds(left.getResourceIds(), right.getResourceIds());
                assertEquals(
                    left.getWith()
                        .isNegated(),
                    right.getWith()
                        .isNegated());
                assertEquals(
                    left.getWith()
                        .getTagMatcher() == null ? null
                            : left.getWith()
                                .getTagMatcher()
                                .getPatternString(),
                    right.getWith()
                        .getTagMatcher() == null ? null
                            : right.getWith()
                                .getTagMatcher()
                                .getPatternString());
            }
            assertIds(a.getExclusions(), b.getExclusions());
            if (first.get(i) instanceof OutputStatement) {
                OutputStatement left = (OutputStatement) first.get(i), right = (OutputStatement) second.get(i);
                assertEquals(left.isEach(), right.isEach());
                assertEquals(left.isEmptySlotsOnly(), right.isEmptySlotsOnly());
            } else assertEquals(((InputStatement) first.get(i)).isEach(), ((InputStatement) second.get(i)).isEach());
        }
    }

    private static void assertQuantity(ResourceQuantity first, ResourceQuantity second) {
        assertEquals(first.isDynamic(), second.isDynamic());
        assertEquals(first.isEach(), second.isEach());
        if (first.isDynamic()) assertEquals(first.toString(), second.toString());
        else assertEquals(first.getAmount(), second.getAmount());
    }

    private static void assertIds(ResourceIdSet first, ResourceIdSet second) {
        if (first == null || second == null) {
            assertSame(first, second);
            return;
        }
        assertEquals(
            first.getIdentifiers()
                .size(),
            second.getIdentifiers()
                .size());
        for (int i = 0; i < first.getIdentifiers()
            .size(); i++) {
            ResourceIdentifier a = first.getIdentifiers()
                .get(i),
                b = second.getIdentifiers()
                    .get(i);
            assertEquals(a.getType(), b.getType());
            assertEquals(a.getMetadata(), b.getMetadata());
            assertEquals(a.getTag(), b.getTag());
            assertEquals(normalize(a.getNamespace()), normalize(b.getNamespace()));
            assertEquals(normalize(a.getName()), normalize(b.getName()));
        }
    }

    private static String normalize(String pattern) {
        return pattern.equals(".*") ? "*" : pattern;
    }

    private static ResourceLimits limits(Statement statement) {
        return statement instanceof InputStatement ? ((InputStatement) statement).getResourceLimits()
            : ((OutputStatement) statement).getResourceLimits();
    }

    private static List<Statement> statements(Program program) {
        return program.getTriggers()
            .get(0)
            .getBlock()
            .getStatements();
    }

    private static Program program(String source) {
        ProgramBuildResult result = new ProgramBuilder(source).build();
        assertTrue(result.isSuccess(), result.getErrorMessage() + "\n" + source);
        return result.getProgram();
    }

    private static void execute(Program program, Fixture fixture) {
        program.tick(
            new ProgramContext(
                fixture.world,
                null,
                new CableNetwork(fixture.world, MANAGER),
                fixture.labels,
                20,
                false));
    }
}
