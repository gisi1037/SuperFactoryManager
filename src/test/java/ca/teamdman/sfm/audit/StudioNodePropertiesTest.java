package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties.Field;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties.FieldType;

class StudioNodePropertiesTest {

    @Test
    void outputLabelAndLiteralAllowancesPreserveEveryOtherByte() {
        String clause = "oUtPuT 16 EACH ReTaIn 2 EACH item:minecraft:iron_ingot WITH #forge:ingots/iron "
            + "EXCEPT item:minecraft:coal TO EMPTY SLOTS IN EACH \"Input NBT:Foo\" "
            + "ROUND ROBIN BY BLOCK North,Top SIDE SLOTS 0,2-8";
        List<Field> fields = StudioNodeProperties.inspect(Type.OUTPUT, clause);
        assertEquals(10, fields.size());
        assertEquals("\"Input NBT:Foo\"", field(fields, "label").getValue());
        assertEquals("16", field(fields, "amount").getValue());
        assertEquals("2", field(fields, "retain").getValue());
        assertEquals(FieldType.NONNEGATIVE_INTEGER, field(fields, "amount").getType());
        assertEquals(
            clause.replace("\"Input NBT:Foo\"", "\"New Chest\""),
            StudioNodeProperties.update(Type.OUTPUT, clause, "label", "\"New Chest\""));
        assertEquals(
            clause.replace("16 EACH", "32 EACH"),
            StudioNodeProperties.update(Type.OUTPUT, clause, "amount", "32"));
        assertEquals(
            clause.replace("ReTaIn 2 EACH", "ReTaIn 0 EACH"),
            StudioNodeProperties.update(Type.OUTPUT, clause, "retain", "0"));
    }

    @Test
    void sourceFirstInputAndUnicodeBeforeNumberUseExactJavaCharacterOffsets() {
        String clause = "FrOm EACH \"Chest \uD83C\uDFED\" Round Robin By Label West SIDE SLOT 1-4 "
            + "InPuT 64 retain 7 item:minecraft:iron_ingot";
        List<Field> fields = StudioNodeProperties.inspect(Type.INPUT, clause);
        assertEquals(9, fields.size());
        assertEquals("\"Chest \uD83C\uDFED\"", field(fields, "label").getValue());
        assertEquals(
            clause.replace("64 retain", "12 retain"),
            StudioNodeProperties.update(Type.INPUT, clause, "amount", "12"));
        assertEquals(
            clause.replace("retain 7", "retain 9"),
            StudioNodeProperties.update(Type.INPUT, clause, "retain", "9"));
        assertEquals(
            clause.replace("\"Chest \uD83C\uDFED\"", "SourceCase"),
            StudioNodeProperties.update(Type.INPUT, clause, "label", "SourceCase"));
    }

    @Test
    void timerIntervalIncludesUnitsGlobalAndOffsetWithoutRewritingEveryKeyword() {
        for (String interval : new String[] { "20 gLoBaL + 5 TiCkS", "20g + 3 seconds", "1 tick", "seconds" }) {
            String clause = "eVeRy  " + interval + "  ";
            List<Field> fields = StudioNodeProperties.inspect(Type.TIMER, clause);
            assertEquals(1, fields.size(), clause);
            assertEquals(
                interval,
                fields.get(0)
                    .getValue());
            assertEquals(
                FieldType.INTERVAL,
                fields.get(0)
                    .getType());
            String changed = StudioNodeProperties.update(Type.TIMER, clause, "interval", "2 GLOBAL + 1 seconds");
            assertEquals("eVeRy  2 GLOBAL + 1 seconds  ", changed);
            BlueprintCompiler.validateClause(Type.TIMER, changed);
        }
        for (String invalid : new String[] { "0 ticks", "-1 ticks", "20 ticks do end every 1 ticks",
            "1 seconds\nDO END", "99999999999999999 seconds", "20" })
            assertThrows(
                IllegalArgumentException.class,
                () -> StudioNodeProperties.update(Type.TIMER, "EVERY 20 TICKS", "interval", invalid),
                invalid);
    }

    @Test
    void ambiguousGroupsAndResourceAllowancesRemainRawRatherThanCollapsingThem() {
        assertTrue(
            StudioNodeProperties.inspect(Type.INPUT, "INPUT 64 FROM first, second NORTH SIDE")
                .isEmpty());
        assertTrue(
            StudioNodeProperties.inspect(Type.OUTPUT, "OUTPUT TO first, second")
                .isEmpty());
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties.update(Type.OUTPUT, "OUTPUT TO first, second", "label", "new"));
        String multiple = "OUTPUT 16 item:minecraft:iron_ingot, 2 RETAIN 1 item:minecraft:coal TO target";
        List<Field> fields = StudioNodeProperties.inspect(Type.OUTPUT, multiple);
        assertEquals(6, fields.size());
        assertEquals(
            "label",
            fields.get(0)
                .getKey());
        assertEquals(
            multiple.replace("target", "newTarget"),
            StudioNodeProperties.update(Type.OUTPUT, multiple, "label", "newTarget"));
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties.update(Type.OUTPUT, multiple, "amount", "32"));
    }

    @Test
    void labelEditorRejectsMultipleLabelsModifiersNewlinesCommentsAndInvalidQuotes() {
        String clause = "INPUT 64 RETAIN 1 FROM source NORTH SIDE SLOTS 0-8";
        for (String invalid : new String[] { "first, second", "other SOUTH SIDE", "other OUTPUT TO stolen",
            "other\nOUTPUT TO stolen", "other\rOUTPUT TO stolen", "other -- hidden", "\"unfinished", "", "other@" })
            assertThrows(
                IllegalArgumentException.class,
                () -> StudioNodeProperties.update(Type.INPUT, clause, "label", invalid),
                invalid);
        assertEquals(
            clause.replace("source", "\"First, Second\""),
            StudioNodeProperties.update(Type.INPUT, clause, "label", "\"First, Second\""));
    }

    @Test
    void numericFieldsRejectExpressionsOverflowAndScopeInjection() {
        String clause = "OUTPUT 16 RETAIN 1 TO target";
        for (String invalid : new String[] { "-1", "1 + 1", "9223372036854775808", "1 EACH", "1\nFORGET", "" })
            assertThrows(
                IllegalArgumentException.class,
                () -> StudioNodeProperties.update(Type.OUTPUT, clause, "amount", invalid),
                invalid);
        assertEquals(
            "OUTPUT 9223372036854775807 RETAIN 1 TO target",
            StudioNodeProperties.update(Type.OUTPUT, clause, "amount", "9223372036854775807"));
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties.update(Type.OUTPUT, clause, "missing", "0"));
    }

    @Test
    void clausesOutsideSmallInspectorSupportKeepFullClauseFallback() {
        assertTrue(
            StudioNodeProperties.inspect(Type.PULSE, "EVERY REDSTONE PULSE")
                .isEmpty());
        assertTrue(
            StudioNodeProperties.inspect(Type.IF, "source HAS GT 0 item:*")
                .isEmpty());
        assertTrue(
            StudioNodeProperties.inspect(Type.REDSTONE, "OUTPUT 15 REDSTONE LEVEL TO target NORTH SIDE")
                .isEmpty());
        assertTrue(
            StudioNodeProperties.inspect(Type.FORGET, "FORGET source")
                .isEmpty());
        assertTrue(
            StudioNodeProperties.inspect(Type.INPUT, "INPUT FROM")
                .isEmpty());
        assertTrue(
            StudioNodeProperties.inspect(Type.INPUT, "INPUT FROM source -- comment")
                .isEmpty());
        assertTrue(
            StudioNodeProperties.inspect(Type.INPUT, "INPUT FROM source\n")
                .isEmpty());
        assertTrue(
            StudioNodeProperties.inspect(null, null)
                .isEmpty());
        String oversized = new String(new char[BlueprintGraph.MAX_CLAUSE + 1]).replace('\0', 'x');
        assertTrue(
            StudioNodeProperties.inspect(Type.INPUT, oversized)
                .isEmpty());
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties.update(Type.INPUT, "INPUT FROM source", "label", oversized));
    }

    @Test
    void absentAmountsAreNotInventedAndFieldsAreImmutable() {
        List<Field> fields = StudioNodeProperties.inspect(Type.INPUT, "INPUT FROM source");
        assertEquals(5, fields.size());
        assertFalse(
            fields.get(0)
                .getHelp()
                .isEmpty());
        assertFalse(
            fields.get(0)
                .getName()
                .isEmpty());
        assertEquals(
            FieldType.LABEL,
            fields.get(0)
                .getType());
        assertThrows(UnsupportedOperationException.class, fields::clear);
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties.update(Type.INPUT, "INPUT FROM source", "amount", "10"));
    }

    @Test
    void existingSideSlotRoundRobinAndContainerModifiersReplaceOnlyTheirOwnSpan() {
        String clause = "FrOm EACH \"Chest \uD83C\uDFED\" Round Robin By Label North, Top SIDE SLOT 1-4 "
            + "InPuT 64 EACH retain 7 EACH item:minecraft:iron_ingot EXCEPT item:minecraft:coal";
        List<Field> fields = StudioNodeProperties.inspect(Type.INPUT, clause);
        assertEquals("North, Top", field(fields, "sides").getValue());
        assertEquals("1-4", field(fields, "slots").getValue());
        assertEquals("label", field(fields, "roundRobin").getValue());
        assertEquals("true", field(fields, "each").getValue());
        assertEquals(FieldType.SIDES, field(fields, "sides").getType());
        assertEquals(
            clause.replace("North, Top SIDE", "South,Bottom SIDE"),
            StudioNodeProperties.update(Type.INPUT, clause, "sides", "South,Bottom"));
        assertEquals(
            clause.replace("SLOT 1-4", "SLOTS 0,2-8"),
            StudioNodeProperties.update(Type.INPUT, clause, "slots", "0,2-8"));
        assertEquals(
            clause.replace("Round Robin By Label", "ROUND ROBIN BY BLOCK"),
            StudioNodeProperties.update(Type.INPUT, clause, "roundRobin", "block"));
        assertEquals(
            clause.replace("FrOm EACH", "FrOm "),
            StudioNodeProperties.update(Type.INPUT, clause, "each", "false"));
        for (Field field : fields) assertEquals(
            clause,
            StudioNodeProperties.update(Type.INPUT, clause, field.getKey(), field.getValue()),
            field.getKey());
        assertEquals(clause, StudioNodeProperties.update(Type.INPUT, clause, "roundRobin", "LABEL"));
        assertEquals(clause, StudioNodeProperties.update(Type.INPUT, clause, "each", "TRUE"));
    }

    @Test
    void absentModifiersCanBeAddedInUiOrderAndRemovedWithoutReformattingFilters() {
        String base = "oUtPuT item:minecraft:iron_ingot EXCEPT item:minecraft:coal TO target";
        String clause = StudioNodeProperties.update(Type.OUTPUT, base, "each", "true");
        clause = StudioNodeProperties.update(Type.OUTPUT, clause, "roundRobin", "block");
        clause = StudioNodeProperties.update(Type.OUTPUT, clause, "sides", "North,Top");
        clause = StudioNodeProperties.update(Type.OUTPUT, clause, "slots", "0,2-8");
        clause = StudioNodeProperties.update(Type.OUTPUT, clause, "emptySlots", "true");
        assertEquals(
            base.replace("TO target", "TO EMPTY SLOTS IN EACH target ROUND ROBIN BY BLOCK North,Top SIDE SLOTS 0,2-8"),
            clause);
        String expected = clause.replace("EMPTY SLOTS IN", "");
        clause = StudioNodeProperties.update(Type.OUTPUT, clause, "emptySlots", "false");
        assertEquals(expected, clause);
        expected = clause.replace("EACH", "");
        clause = StudioNodeProperties.update(Type.OUTPUT, clause, "each", "false");
        assertEquals(expected, clause);
        expected = clause.replace("ROUND ROBIN BY BLOCK", "");
        clause = StudioNodeProperties.update(Type.OUTPUT, clause, "roundRobin", "off");
        assertEquals(expected, clause);
        expected = clause.replace("North,Top SIDE", "");
        clause = StudioNodeProperties.update(Type.OUTPUT, clause, "sides", "");
        assertEquals(expected, clause);
        expected = clause.replace("SLOTS 0,2-8", "");
        clause = StudioNodeProperties.update(Type.OUTPUT, clause, "slots", "");
        assertEquals(expected, clause);
        BlueprintCompiler.validateClause(Type.OUTPUT, clause);
    }

    @Test
    void insertingModifiersHonorsSourceFirstOrderAndMissingMiddleQualifiers() {
        assertEquals(
            "FROM source EACH SIDE INPUT 64",
            StudioNodeProperties.update(Type.INPUT, "FROM source INPUT 64", "sides", "EACH"));
        assertEquals(
            "FROM source SLOTS 0-8 INPUT 64",
            StudioNodeProperties.update(Type.INPUT, "FROM source INPUT 64", "slots", "0-8"));
        assertEquals(
            "FROM source ROUND ROBIN BY BLOCK West SIDE SLOTS 0-8 INPUT 64",
            StudioNodeProperties.update(Type.INPUT, "FROM source West SIDE SLOTS 0-8 INPUT 64", "roundRobin", "block"));
        assertEquals(
            "TO EMPTY SLOTS IN target North SIDE OUTPUT 16",
            StudioNodeProperties.update(Type.OUTPUT, "TO target North SIDE OUTPUT 16", "emptySlots", "true"));
        assertEquals(
            "OUTPUT TO EACH target each SIDE SLOTS 0-8",
            StudioNodeProperties.update(Type.OUTPUT, "OUTPUT TO EACH target SLOTS 0-8", "sides", "each"));
        assertEquals(
            "INPUT FROM source SLOTS 8-2",
            StudioNodeProperties.update(Type.INPUT, "INPUT FROM source", "slots", "8-2"));
    }

    @Test
    void resourceEachAndContainerEachRemainIndependentAndNumbersAreReparsedAfterEveryEdit() {
        String clause = "OUTPUT 16 RETAIN 2 TO target";
        clause = StudioNodeProperties.update(Type.OUTPUT, clause, "amount", "1234");
        clause = StudioNodeProperties.update(Type.OUTPUT, clause, "amountEach", "true");
        clause = StudioNodeProperties.update(Type.OUTPUT, clause, "retain", "456");
        clause = StudioNodeProperties.update(Type.OUTPUT, clause, "retainEach", "true");
        clause = StudioNodeProperties.update(Type.OUTPUT, clause, "each", "true");
        assertEquals("OUTPUT 1234 EACH RETAIN 456 EACH TO EACH target", clause);
        assertEquals(
            "OUTPUT 1234  RETAIN 456 EACH TO EACH target",
            StudioNodeProperties.update(Type.OUTPUT, clause, "amountEach", "false"));
        assertEquals(
            "OUTPUT 1234 EACH RETAIN 456  TO EACH target",
            StudioNodeProperties.update(Type.OUTPUT, clause, "retainEach", "false"));
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties.update(Type.INPUT, "INPUT (1 + 2) FROM source", "amountEach", "true"));
    }

    @Test
    void sideSlotsAndModesRejectInjectionRelativeFacesAndOverflow() {
        String clause = "OUTPUT 16 RETAIN 2 TO target";
        for (String value : new String[] { "North SIDE", "North OUTPUT TO stolen", "Front", "North@", "North\nSIDE" })
            assertThrows(
                IllegalArgumentException.class,
                () -> StudioNodeProperties.update(Type.OUTPUT, clause, "sides", value),
                value);
        for (String value : new String[] { "-1", "2147483648", "0 SLOTS 1", "0 OUTPUT TO stolen", "0@" }) assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties.update(Type.OUTPUT, clause, "slots", value),
            value);
        for (String value : new String[] { "true", "ROUND ROBIN BY BLOCK", "block EACH", "" }) assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties.update(Type.OUTPUT, clause, "roundRobin", value),
            value);
        for (String key : new String[] { "each", "amountEach", "retainEach", "emptySlots" })
            for (String value : new String[] { "1", "yes", "EACH", "true OUTPUT TO stolen", "" }) assertThrows(
                IllegalArgumentException.class,
                () -> StudioNodeProperties.update(Type.OUTPUT, clause, key, value),
                key + ":" + value);
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties.update(Type.INPUT, "INPUT FROM source", "emptySlots", "true"));
    }

    private static Field field(List<Field> fields, String key) {
        return fields.stream()
            .filter(
                field -> field.getKey()
                    .equals(key))
            .findFirst()
            .get();
    }
}
