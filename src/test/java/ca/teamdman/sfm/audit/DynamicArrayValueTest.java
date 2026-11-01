package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;

import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataTypeParser;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.ProgramGlobals;
import ca.teamdman.sfml.data.ProgramStateSchema;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class DynamicArrayValueTest {

    private static final DataType INTEGERS = DataType.dynamicArray(DataType.INTEGER);

    private static DataValue integers(int length) {
        DataValue[] values = new DataValue[length];
        Arrays.fill(values, DataValue.integer(0));
        return DataValue.dynamicArray(INTEGERS, values);
    }

    @Test
    void lengthBelongsToTheValueAndDynamicTypesStayDistinctFromFixedShapes() {
        DataValue empty = integers(0), one = integers(1), full = integers(256);
        for (DataValue value : Arrays.asList(empty, one, full)) {
            assertEquals(INTEGERS, value.getType());
            assertEquals(
                DataType.Kind.DYNAMIC_ARRAY,
                value.getType()
                    .getKind());
            assertTrue(
                value.getType()
                    .isSequence());
            assertEquals(
                0,
                value.getType()
                    .getLength());
            assertEquals(value.size() + 1, value.getRetainedCellCount());
        }
        assertEquals(0, empty.size());
        assertEquals(256, full.size());
        assertEquals("[]", empty.toString());
        assertEquals("[0]", one.toString());
        assertEquals(
            INTEGERS.hashCode(),
            DataType.dynamicArray(DataType.INTEGER)
                .hashCode());
        assertNotEquals(INTEGERS, DataType.array(DataType.INTEGER, 0));
        assertNotEquals(INTEGERS, DataType.array(DataType.INTEGER, 1));
        assertNotEquals(INTEGERS, DataType.dynamicArray(DataType.BOOLEAN));
        assertEquals(
            "ARRAY[2:INTEGER]",
            DataType.array(DataType.INTEGER, 2)
                .getSignature());
    }

    @Test
    void publicConstructionCopiesStorageAndWithCopiesValuesWithoutChangingTheirType() {
        DataValue[] input = { DataValue.integer(1), DataValue.integer(2) };
        DataValue original = DataValue.dynamicArray(INTEGERS, input);
        input[0] = DataValue.integer(99);
        DataValue changed = original.with(1, DataValue.integer(7));
        assertEquals(
            1,
            original.get(0)
                .asLong());
        assertEquals(
            2,
            original.get(1)
                .asLong());
        assertEquals(
            7,
            changed.get(1)
                .asLong());
        assertEquals(original.getType(), changed.getType());
        assertEquals("[1, 7]", changed.toString());
        assertThrows(IllegalArgumentException.class, () -> original.with(0, DataValue.bool(true)));
        assertThrows(IllegalArgumentException.class, () -> original.with(0, null));
        assertThrows(IllegalArgumentException.class, () -> original.get(-1));
        assertThrows(IllegalArgumentException.class, () -> original.with(2, DataValue.integer(1)));
        assertThrows(IllegalArgumentException.class, () -> integers(0).get(0));
    }

    @Test
    void variableLengthChildrenShareOneElementTypeAndUpdatesEnforceActualRetainedCells() {
        DataType rowsType = DataType.dynamicArray(INTEGERS);
        DataValue[] rows = new DataValue[16];
        Arrays.fill(rows, integers(256));
        rows[15] = integers(239);
        DataValue boundary = DataValue.dynamicArray(rowsType, rows);
        assertEquals(4096, boundary.getRetainedCellCount());
        assertEquals(1, rowsType.getCellCount(), "Dynamic type stores a minimum, not its live logical length");
        assertThrows(IllegalArgumentException.class, () -> boundary.with(15, integers(240)));
        assertEquals(
            239,
            boundary.get(15)
                .size());
        rows[15] = integers(240);
        assertThrows(IllegalArgumentException.class, () -> DataValue.dynamicArray(rowsType, rows));

        rows[15] = integers(238);
        DataValue fixed = DataValue.collection(DataType.array(INTEGERS, 16), rows);
        DataValue wrapped = DataValue.struct(Collections.singletonMap("rows", fixed));
        assertEquals(4096, wrapped.getRetainedCellCount());
        DataValue grownFixed = fixed.with(15, integers(239));
        assertEquals(4096, grownFixed.getRetainedCellCount());
        assertThrows(IllegalArgumentException.class, () -> wrapped.with(0, grownFixed));
        assertThrows(
            IllegalArgumentException.class,
            () -> DataValue.struct(Collections.singletonMap("rows", boundary)));
        assertEquals(
            238,
            wrapped.get("rows")
                .get(15)
                .size());
    }

    @Test
    void retainedStringAndOpaquePayloadBoundsIncludeRepeatedSharedChildren() {
        char[] chars = new char[4096];
        Arrays.fill(chars, 'x');
        DataValue text = DataValue.string(new String(chars));
        DataType strings = DataType.dynamicArray(DataType.STRING);
        DataValue limit = DataValue.dynamicArray(strings, new DataValue[] { text, text, text, text });
        assertEquals(ProgramGlobals.MAX_CHARACTERS, limit.getRetainedCharacters());
        assertThrows(
            IllegalArgumentException.class,
            () -> DataValue.dynamicArray(strings, new DataValue[] { text, text, text, text, text }));
        DataValue small = DataValue.dynamicArray(strings, new DataValue[] { DataValue.string("a") });
        assertEquals(1, small.getStringCharacterCount());
        assertEquals(
            4096,
            small.with(0, text)
                .getStringCharacterCount());
        assertEquals(1, small.getRetainedCharacters());

        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByteArray("payload", new byte[2048]);
        DataValue key = DataValue.itemIdentity(ItemIdentity.of("dynamic:item", 0, nbt, new QueryBudget()));
        int count = ProgramGlobals.MAX_OPAQUE_BYTES / key.getRetainedOpaqueBytes() + 1;
        assertTrue(count <= 256);
        DataValue[] keys = new DataValue[count];
        Arrays.fill(keys, key);
        assertThrows(
            IllegalArgumentException.class,
            () -> DataValue.dynamicArray(DataType.dynamicArray(DataType.ITEM_KEY), keys));
    }

    @Test
    void dynamicContainmentPreventsStatePersistenceEvenInsideEmptyFixedArraysAndStructs() {
        for (DataType type : Arrays.asList(
            INTEGERS,
            DataType.array(INTEGERS, 0),
            DataType.struct(Collections.singletonMap("values", INTEGERS)),
            DataType.dynamicArray(DataType.array(DataType.INTEGER, 3)))) {
            assertTrue(type.containsDynamicArray());
            assertFalse(type.isPersistable());
        }
        assertFalse(
            DataType.array(DataType.INTEGER, 3)
                .containsDynamicArray());
        assertFalse(
            DataType.struct(Collections.singletonMap("value", DataType.INTEGER))
                .containsDynamicArray());
        assertTrue(
            DataType.array(DataType.INTEGER, 3)
                .isPersistable());
        assertThrows(IllegalArgumentException.class, () -> new ProgramStateSchema.Declaration("values", integers(0)));
        assertThrows(
            IllegalArgumentException.class,
            () -> new ProgramStateSchema.Declaration(
                "values",
                DataValue.struct(Collections.singletonMap("nested", integers(1)))));
    }

    @Test
    void malformedValuesNeverIntroduceAnImplicitFixedDynamicConversion() {
        assertThrows(IllegalArgumentException.class, () -> DataType.dynamicArray(null));
        assertThrows(IllegalArgumentException.class, () -> DataType.dynamicArray(DataType.VOID));
        assertThrows(IllegalArgumentException.class, () -> DataValue.dynamicArray(INTEGERS, null));
        assertThrows(IllegalArgumentException.class, () -> DataValue.dynamicArray(null, new DataValue[0]));
        assertThrows(IllegalArgumentException.class, () -> integers(257));
        assertThrows(IllegalArgumentException.class, () -> DataValue.dynamicArray(INTEGERS, new DataValue[] { null }));
        assertThrows(
            IllegalArgumentException.class,
            () -> DataValue.dynamicArray(INTEGERS, new DataValue[] { DataValue.bool(true) }));
        assertThrows(
            IllegalArgumentException.class,
            () -> DataValue
                .dynamicArray(DataType.array(DataType.INTEGER, 1), new DataValue[] { DataValue.integer(1) }));
        assertThrows(IllegalArgumentException.class, () -> DataValue.collection(INTEGERS, new DataValue[0]));
        assertThrows(IllegalArgumentException.class, () -> DataValue.array(Collections.emptyList()));
    }

    @Test
    void grammarTypeParserSupportsAllScalarNamesAndMixedDimensionsInTheirWrittenOrder() {
        for (DataType type : Arrays.asList(
            DataType.INTEGER,
            DataType.BOOLEAN,
            DataType.STRING,
            DataType.LABEL_REF,
            DataType.RESOURCE_SELECTOR,
            DataType.ITEM_KEY,
            DataType.FLUID_KEY,
            DataType.ITEM_CONTENTS,
            DataType.ITEM_LOCATIONS,
            DataType.FLUID_CONTENTS))
            assertEquals(
                type,
                DataTypeParser.parse(
                    type.getKind()
                        .name()
                        .toLowerCase(java.util.Locale.ROOT)));
        assertEquals(
            DataType.dynamicArray(DataType.array(DataType.INTEGER, 2)),
            DataTypeParser.parse("INTEGER [ ] [ 2 ]"));
        assertEquals(DataType.array(INTEGERS, 2), DataTypeParser.parse("INTEGER[2][]"));
        assertEquals(DataType.array(DataType.array(DataType.INTEGER, 2), 3), DataTypeParser.parse("INTEGER[3][2]"));
        DataType struct = DataTypeParser.parse("{title: STRING, rows: {count: INTEGER, tags: STRING[]}[][2]}");
        assertTrue(struct.containsDynamicArray());
        assertFalse(struct.isPersistable());
        assertEquals(
            DataType.Kind.DYNAMIC_ARRAY,
            struct.getFieldType(struct.fieldIndex("rows"))
                .getKind());
        ProgramBuildResult signature = new ProgramBuilder(
            "FUNCTION identity(a: INTEGER[][2]) RETURNS INTEGER[][2] " + "DO RETURN a END EVERY 20 TICKS DO END")
                .build();
        assertTrue(signature.isSuccess(), signature.getErrorMessage());
        ProgramBuildResult mismatch = new ProgramBuilder(
            "FUNCTION wrong() RETURNS INTEGER[] DO RETURN [1, 2] END " + "EVERY 20 TICKS DO END").build();
        assertFalse(mismatch.isSuccess());
    }

    @Test
    void typeLiteralParserRejectsVoidInjectionAndOversizedOrDeepTypesBeforeRuntime() {
        for (String source : Arrays.asList(
            "",
            "VOID",
            "VOID[]",
            "{x: VOID}",
            "{x: INTEGER, x: BOOLEAN}",
            "unknown",
            "INTEGER[-1]",
            "INTEGER[257]",
            "INTEGER[256][256]",
            "INTEGER[999999999999]",
            "INTEGER INTEGER",
            "INTEGER[] PRINT(1)",
            "INTEGER -- comment",
            "INTEGER@")) assertThrows(IllegalArgumentException.class, () -> DataTypeParser.parse(source), source);
        assertThrows(IllegalArgumentException.class, () -> DataTypeParser.parse(null));
        StringBuilder wide = new StringBuilder();
        for (int i = 0; i < 1025; i++) wide.append(' ');
        assertThrows(IllegalArgumentException.class, () -> DataTypeParser.parse(wide.toString()));
        StringBuilder deep = new StringBuilder("INTEGER");
        for (int i = 0; i < 32; i++) deep.append("[]");
        assertThrows(IllegalArgumentException.class, () -> DataTypeParser.parse(deep.toString()));
        assertEquals(
            32,
            DataTypeParser.parse(deep.substring(0, deep.length() - 2))
                .getDepth());
        StringBuilder nested = new StringBuilder();
        for (int i = 0; i < 33; i++) nested.append("{x:");
        nested.append("INTEGER");
        for (int i = 0; i < 33; i++) nested.append('}');
        assertThrows(IllegalArgumentException.class, () -> DataTypeParser.parse(nested.toString()));
    }
}
