package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintDocument;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioDocument;
import ca.teamdman.sfm.client.blueprint.StudioFieldDraft;
import ca.teamdman.sfm.client.blueprint.StudioFieldPicker;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties.Field;
import ca.teamdman.sfml.ast.InputStatement;
import ca.teamdman.sfml.ast.OutputStatement;
import ca.teamdman.sfml.ast.ResourceIdentifier;
import ca.teamdman.sfml.ast.ResourceLimit;
import ca.teamdman.sfml.ast.ResourceLimits;
import ca.teamdman.sfml.ast.Statement;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class StudioIoResourceFieldsTest {

    private static Field resource(Type type, String clause) {
        return StudioNodeProperties.inspect(type, clause)
            .stream()
            .filter(
                f -> f.getKey()
                    .equals("resource"))
            .findFirst()
            .orElse(null);
    }

    private static String edit(Type type, String clause, String value) {
        return StudioNodeProperties.update(type, clause, "resource", value);
    }

    private static String program(String clause) {
        return "EVERY 20 TICKS DO " + clause + " END";
    }

    private static Statement compiled(String clause) {
        ProgramBuildResult built = new ProgramBuilder(program(clause)).build();
        assertTrue(built.isSuccess(), built.getErrorMessage());
        return built.getProgram()
            .getTriggers()
            .get(0)
            .getBlock()
            .getStatements()
            .get(0);
    }

    private static ResourceLimits limits(String clause) {
        Statement statement = compiled(clause);
        return statement instanceof InputStatement ? ((InputStatement) statement).getResourceLimits()
            : ((OutputStatement) statement).getResourceLimits();
    }

    private static void sameAllowancesAndFilters(ResourceLimits before, ResourceLimits after) {
        ResourceLimit a = before.getLimits()
            .get(0),
            b = after.getLimits()
                .get(0);
        assertEquals(
            a.getLimit()
                .getQuantity()
                .getAmount(),
            b.getLimit()
                .getQuantity()
                .getAmount());
        assertEquals(
            a.getLimit()
                .getQuantity()
                .isEach(),
            b.getLimit()
                .getQuantity()
                .isEach());
        assertEquals(
            a.getLimit()
                .getRetention()
                .getAmount(),
            b.getLimit()
                .getRetention()
                .getAmount());
        assertEquals(
            a.getLimit()
                .getRetention()
                .isEach(),
            b.getLimit()
                .getRetention()
                .isEach());
        assertEquals(
            a.getWith()
                .isNegated(),
            b.getWith()
                .isNegated());
        assertEquals(
            a.getWith()
                .getTagMatcher() == null ? null
                    : a.getWith()
                        .getTagMatcher()
                        .getPatternString(),
            b.getWith()
                .getTagMatcher() == null ? null
                    : b.getWith()
                        .getTagMatcher()
                        .getPatternString());
        assertEquals(
            before.getExclusions() == null ? null
                : before.getExclusions()
                    .toString(),
            after.getExclusions() == null ? null
                : after.getExclusions()
                    .toString());
    }

    @Test
    void bothDirectionsAndOrdersReplaceExactlyOneSpanKeepingAllNumericAndRoutingMeaning() {
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT })
            for (boolean sourceFirst : new boolean[] { false, true }) {
                String keyword = type == Type.INPUT ? "InPuT" : "oUtPuT",
                    direction = type == Type.INPUT ? "FrOm" : "tO EMPTY SLOTS IN";
                String access = direction + " EACH \"Chest 🏭\" ROUND ROBIN BY BLOCK North,Top SIDE SLOTS 0,2-8";
                String action = keyword
                    + " 64 EACH ReTaIn 7 EACH \"item:Mod:Part:3\" WITHOUT #forge:bad EXCEPT item:minecraft:dirt,fluid:water";
                String clause = sourceFirst ? access + "  " + action : action + "  " + access;
                assertNotNull(resource(type, clause));
                assertEquals("\"item:Mod:Part:3\"", resource(type, clause).getValue());
                String changed = edit(type, clause, "\"item:Other:Part:7\"");
                assertEquals(clause.replace("\"item:Mod:Part:3\"", "\"item:Other:Part:7\""), changed);
                sameAllowancesAndFilters(limits(clause), limits(changed));
                ResourceIdentifier id = limits(changed).getLimits()
                    .get(0)
                    .getResourceIds()
                    .getIdentifiers()
                    .get(0);
                assertEquals(ResourceIdentifier.Type.ITEM, id.getType());
                assertEquals("Other", id.getNamespace());
                assertEquals("Part", id.getName());
                assertEquals(7, id.getMetadata());
                String removed = edit(type, changed, "");
                assertEquals(changed.replace("\"item:Other:Part:7\"", ""), removed);
                sameAllowancesAndFilters(limits(clause), limits(removed));
                assertEquals(
                    ResourceIdentifier.Type.ANY,
                    limits(removed).getLimits()
                        .get(0)
                        .getResourceIds()
                        .getIdentifiers()
                        .get(0)
                        .getType());
                assertEquals(clause, edit(type, clause, resource(type, clause).getValue()));
            }
    }

    @Test
    void absentQuantityOnlyRetentionOnlyAndWithOnlyInsertionsKeepExistingIoDefaults() {
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT })
            for (boolean sourceFirst : new boolean[] { false, true }) for (String limit : new String[] { "", "16",
                "RETAIN 3 EACH", "16 EACH RETAIN 3 EACH", "WITH #forge:ingots", "WITHOUT #forge:dusts" }) {
                    String keyword = type == Type.INPUT ? "INPUT" : "OUTPUT",
                        access = type == Type.INPUT ? "FROM source" : "TO EMPTY SLOTS IN EACH dest";
                    String action = keyword + (limit.isEmpty() ? "" : " " + limit) + " EXCEPT item:minecraft:dirt";
                    String clause = sourceFirst ? access + " " + action : action + " " + access;
                    assertEquals("", resource(type, clause).getValue(), clause);
                    assertEquals(clause, edit(type, clause, ""));
                    String inserted = edit(type, clause, "item:minecraft:iron_ingot");
                    String expected = limit.startsWith("WITH") || limit.startsWith("WITHOUT")
                        ? clause.replace(limit, " item:minecraft:iron_ingot " + limit)
                        : clause.replace(
                            keyword + (limit.isEmpty() ? "" : " " + limit),
                            keyword + (limit.isEmpty() ? "" : " " + limit) + " item:minecraft:iron_ingot ");
                    assertEquals(expected, inserted);
                    sameAllowancesAndFilters(limits(clause), limits(inserted));
                    String removed = edit(type, inserted, "");
                    sameAllowancesAndFilters(limits(clause), limits(removed));
                    ResourceLimit restored = limits(removed).getLimits()
                        .get(0);
                    if (limit.isEmpty() || limit.startsWith("WITH") || limit.startsWith("WITHOUT")) {
                        assertEquals(
                            Long.MAX_VALUE,
                            restored.getLimit()
                                .getQuantity()
                                .getAmount());
                        assertEquals(
                            type == Type.INPUT ? 0 : Long.MAX_VALUE,
                            restored.getLimit()
                                .getRetention()
                                .getAmount());
                    }
                }
    }

    @Test
    void compactQuotedSyntaxAndExplicitWildcardRemovalCannotJoinSurvivingKeywords() {
        for (String clause : new String[] { "INPUT\"item:minecraft:iron_ingot\"FROM\"箱 😀\"",
            "FROM\"箱 😀\"INPUT\"item:minecraft:iron_ingot\"", "OUTPUT\"item:minecraft:iron_ingot\"TO\"箱 😀\"",
            "TO\"箱 😀\"OUTPUT\"item:minecraft:iron_ingot\"" }) {
            Type type = clause.contains("INPUT") ? Type.INPUT : Type.OUTPUT;
            String removed = edit(type, clause, "");
            String expected = clause.replace("\"item:minecraft:iron_ingot\"", "");
            if (expected.startsWith("INPUTFROM")) expected = expected.replace("INPUTFROM", "INPUT FROM");
            if (expected.startsWith("OUTPUTTO")) expected = expected.replace("OUTPUTTO", "OUTPUT TO");
            assertEquals(expected, removed);
            String bare = edit(type, clause, "item:minecraft:gold_ingot");
            String expectedBare = clause.replace("\"item:minecraft:iron_ingot\"", " item:minecraft:gold_ingot")
                .replace("gold_ingotFROM", "gold_ingot FROM")
                .replace("gold_ingotTO", "gold_ingot TO");
            assertEquals(expectedBare, bare);
            sameAllowancesAndFilters(limits(clause), limits(bare));
            sameAllowancesAndFilters(limits(clause), limits(removed));
        }
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT }) {
            String clause = type == Type.INPUT ? "INPUT * FROM source" : "OUTPUT * TO dest";
            sameAllowancesAndFilters(limits(clause), limits(edit(type, clause, "")));
            assertEquals("*", resource(type, clause).getValue());
        }
    }

    @Test
    void independentLimitsAndTrailingPunctuationRemainManualWithoutCollapsingThem() {
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT }) for (String selector : new String[] {
            "16 item:a, 2 RETAIN 1 item:b", "16 item:a OR", "16 item:a,", "WITH #a,", "16," }) {
                String clause = (type == Type.INPUT ? "INPUT " : "OUTPUT ") + selector
                    + (type == Type.INPUT ? " FROM source" : " TO dest");
                compiled(clause);
                assertNull(resource(type, clause), clause);
                assertThrows(IllegalArgumentException.class, () -> edit(type, clause, "item:c"));
                assertTrue(
                    StudioNodeProperties.inspect(type, clause)
                        .stream()
                        .anyMatch(
                            f -> f.getKey()
                                .equals("label")));
            }
        assertNotNull(resource(Type.INPUT, "INPUT FROM first,second"));
    }

    @Test
    void injectedValuesFailAtomicallyWhileFluidAndMetadataRemainValidManualSelectors() {
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT }) {
            String clause = type == Type.INPUT ? "INPUT 16 FROM source" : "OUTPUT RETAIN 2 TO dest";
            for (String value : new String[] { "item:a OR item:b", "item:a,", "item:a WITH #tag",
                "item:a EXCEPT item:b", "item:a -- hide", "item:a\nOUTPUT TO stolen", "item:a\0", "\"unfinished",
                "item:a@" }) assertThrows(IllegalArgumentException.class, () -> edit(type, clause, value), value);
            sameAllowancesAndFilters(limits(clause), limits(edit(type, clause, "fluid:water")));
            sameAllowancesAndFilters(limits(clause), limits(edit(type, clause, "\"item:Mod:Part:3\"")));
        }
    }

    @Test
    void pickerChooseIsPendingAndOneApplyUndoRetainsSourceGraphUuidAndOtherPendingFields() {
        String source = "-- 🏭\r\nEVERY 20 TICKS DO INPUT 16 RETAIN 2 FROM source OUTPUT TO dest END";
        StudioDocument document = new StudioDocument();
        document.load(source);
        document.openVisual();
        Node node = document.graph()
            .nodes()
            .stream()
            .filter(n -> n.type == Type.INPUT)
            .findFirst()
            .get();
        UUID project = UUID.randomUUID();
        StudioFieldDraft draft = new StudioFieldDraft(
            project,
            node,
            document.code(),
            StudioNodeProperties.inspect(node.type, node.clause()));
        draft.set("amount", "32");
        Field field = resource(node.type, node.clause());
        StudioFieldPicker.Target target = StudioFieldPicker.target(node.type, field, draft.value("resource"));
        StudioFieldPicker.Snapshot choices = StudioFieldPicker.items(Arrays.asList("minecraft:iron_ingot"), target);
        StudioFieldPicker.Session picker = new StudioFieldPicker.Session(draft, target, choices);
        picker.select(choices.search("iron_ingot").entries.get(0));
        assertEquals("", draft.value("resource"));
        assertEquals("32", draft.value("amount"));
        picker.choose(draft, project, node, document.code());
        assertEquals(source, document.source());
        assertFalse(document.undo());
        assertEquals("\"item:minecraft:iron_ingot\"", draft.value("resource"));
        String applied = draft.appliedClause(project, node, document.code());
        document.editGraph(
            graph -> graph.node(node.id)
                .setClause(applied));
        BlueprintDocument reopened = BlueprintDocument.open(document.source());
        assertNotNull(reopened.graph.node(node.id));
        assertEquals(
            compiled(applied).toString(),
            compiled(
                reopened.graph.node(node.id)
                    .clause()).toString());
        assertTrue(document.undo());
        assertEquals(source, document.source());
        assertNotNull(
            document.graph()
                .node(node.id));
        assertFalse(document.undo());
        assertTrue(document.redo());
        assertEquals(
            applied,
            document.graph()
                .node(node.id)
                .clause());
    }
}
