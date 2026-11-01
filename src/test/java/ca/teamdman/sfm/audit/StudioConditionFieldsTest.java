package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties.Field;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class StudioConditionFieldsTest {

    private static String edit(Type type, String clause, String key, String value) {
        return StudioNodeProperties.update(type, clause, key, value);
    }

    private static Field field(Type type, String clause, String key) {
        return StudioNodeProperties.inspect(type, clause)
            .stream()
            .filter(
                field -> field.getKey()
                    .equals(key))
            .findFirst()
            .get();
    }

    @Test
    void simpleHasFieldsPreserveEverySetOperatorRestrictionFilterExclusionAndUnicodeByte() {
        for (String set : new String[] { "", "SOME ", "OVERALL ", "EVERY ", "EACH ", "ONE ", "LONE " }) {
            String clause = "(" + set
                + "\"Factory 🏭\" ROUND ROBIN BY BLOCK North,Top SIDE SLOTS 0,2-8 "
                + "HAS gE 00000000000000000000009 \"item:Mod:Part:3\" WITHOUT #forge:bad EXCEPT item:minecraft:dirt, fluid:water)";
            BlueprintCompiler.validateClause(Type.IF, clause);
            assertEquals(
                4,
                StudioNodeProperties.inspect(Type.IF, clause)
                    .size(),
                clause);
            assertEquals(
                clause.replace("\"Factory 🏭\"", "\"Other Room\""),
                edit(Type.IF, clause, "label", "\"Other Room\""));
            assertEquals(clause.replace("HAS gE", "HAS !="), edit(Type.IF, clause, "comparator", "!="));
            assertEquals(
                clause.replace("00000000000000000000009", "00000000000000000000010"),
                edit(Type.IF, clause, "amount", "00000000000000000000010"));
            assertEquals(
                clause.replace("\"item:Mod:Part:3\"", "\"item:Other:Part:7\""),
                edit(Type.IF, clause, "resource", "\"item:Other:Part:7\""));
            assertEquals(clause, edit(Type.IF, clause, "amount", field(Type.IF, clause, "amount").getValue()));
        }
    }

    @Test
    void optionalResourceInsertionAndRemovalKeepFiltersAndExclusions() {
        String clause = "\"source\" HAS >= 2 WITH #forge:ingots/iron EXCEPT item:minecraft:dirt";
        String inserted = edit(Type.IF, clause, "resource", "item:minecraft:iron_ingot");
        assertEquals(clause.replace("2 WITH", "2 item:minecraft:iron_ingot  WITH"), inserted);
        String removed = edit(Type.IF, inserted, "resource", "");
        assertEquals(inserted.replace("item:minecraft:iron_ingot", ""), removed);
        BlueprintCompiler.validateClause(Type.IF, removed);
        for (String comparator : new String[] { "=", "==", "!=", "<", "<=", ">", ">=", "GT", "LT", "EQ", "LE", "GE" })
            BlueprintCompiler.validateClause(Type.IF, edit(Type.IF, clause, "comparator", comparator));
    }

    @Test
    void existingLegacyCompositeConditionsRemainFullClauseFallback() {
        for (String clause : new String[] { "NOT \"source\" HAS > 0", "\"source\" HAS > 0 AND \"dest\" HAS < 8",
            "\"source\" HAS > 0 OR \"dest\" HAS < 8", "\"source\" HAS > 0 item:iron OR", "REDSTONE", "TRUE" }) {
            assertTrue(
                StudioNodeProperties.inspect(Type.IF, clause)
                    .isEmpty(),
                clause);
            assertThrows(IllegalArgumentException.class, () -> edit(Type.IF, clause, "label", "other"));
        }
    }

    @Test
    void hasFieldEditsRejectAdversarialValuesAndCannotInjectResourcesOrStatements() {
        String clause = "\"Room 😀\" HAS > 3 item:minecraft:iron_ingot WITH #forge:ingots EXCEPT item:minecraft:dirt";
        for (String value : new String[] { "x SOUTH SIDE", "x THEN FORGET END", "x -- hide", "", "x\nFORGET", "x\0" })
            assertThrows(IllegalArgumentException.class, () -> edit(Type.IF, clause, "label", value), value);
        for (String value : new String[] { "> 1", ">>", "NOT", "< 1 THEN", "> -- hide" })
            assertThrows(IllegalArgumentException.class, () -> edit(Type.IF, clause, "comparator", value), value);
        for (String value : new String[] { "-1", "1g", "(1+2)", "9223372036854775808", "1 EACH", "1 THEN FORGET" })
            assertThrows(IllegalArgumentException.class, () -> edit(Type.IF, clause, "amount", value), value);
        for (String value : new String[] { "item:a OR item:b", "item:a, item:b", "item:a WITH #x", "item:a -- hide",
            "item:a THEN FORGET END" })
            assertThrows(IllegalArgumentException.class, () -> edit(Type.IF, clause, "resource", value), value);
    }

    @Test
    void bothRedstoneOrdersAndQuantityPositionsRetainAllOtherSpansAndStaticClampSemantics() {
        String access = "EACH \"Signal 😀\" ROUND ROBIN BY LABEL North SIDE SLOTS 0,2-8";
        for (String clause : new String[] { "OUTPUT 27 EACH REDSTONE LEVEL TO " + access,
            "OUTPUT REDSTONE LEVELS 27 EACH TO " + access, "TO " + access + " OUTPUT 27 EACH REDSTONE LEVEL",
            "TO " + access + " OUTPUT REDSTONE LEVELS 27 EACH" }) {
            List<Field> fields = StudioNodeProperties.inspect(Type.REDSTONE, clause);
            assertEquals(2, fields.size(), clause);
            assertEquals("27", field(Type.REDSTONE, clause, "level").getValue());
            assertEquals(
                clause.replace("27 EACH", "00000000000000000000028 EACH"),
                edit(Type.REDSTONE, clause, "level", "00000000000000000000028"));
            assertEquals(
                clause.replace("\"Signal 😀\"", "\"New Signal\""),
                edit(Type.REDSTONE, clause, "label", "\"New Signal\""));
            String changed = edit(Type.REDSTONE, clause, "level", "9223372036854775807");
            ProgramBuildResult built = new ProgramBuilder("EVERY 20 TICKS DO " + changed + " END").build();
            assertTrue(built.isSuccess(), built.getErrorMessage());
            assertTrue(
                built.getProgram()
                    .toString()
                    .contains("OUTPUT 15 REDSTONE LEVEL"));
        }
    }

    @Test
    void omittedLevelUsesExistingDefaultAndTwoQuantityOrDynamicFormsStayManual() {
        for (String clause : new String[] { "OUTPUT REDSTONE LEVEL TO \"signal\"",
            "TO \"signal\" OUTPUT REDSTONE LEVELS" }) {
            assertEquals("15", field(Type.REDSTONE, clause, "level").getValue());
            assertEquals(clause, edit(Type.REDSTONE, clause, "level", "15"));
            assertEquals(clause.replace("REDSTONE", "7 REDSTONE"), edit(Type.REDSTONE, clause, "level", "7"));
        }
        for (String clause : new String[] { "OUTPUT 1 REDSTONE LEVEL 15 TO \"signal\"",
            "TO \"signal\" OUTPUT 1 REDSTONE LEVEL 15", "OUTPUT (2 + 3) REDSTONE LEVEL TO \"signal\"" })
            assertTrue(
                StudioNodeProperties.inspect(Type.REDSTONE, clause)
                    .isEmpty(),
                clause);
        for (String value : new String[] { "-1", "1g", "1, 2", "15 TO \"stolen\"", "1 -- hide", "9223372036854775808" })
            assertThrows(
                IllegalArgumentException.class,
                () -> edit(Type.REDSTONE, "OUTPUT 15 REDSTONE LEVEL TO \"signal\"", "level", value));
    }
}
