package ca.teamdman.sfml.data;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import org.junit.jupiter.api.Test;

class StatePayloadBytesTest {

    private static final DataType INTEGERS = DataType.dynamicArray(DataType.INTEGER);

    @Test
    void scalarAndArrayCacheUsesExactNbtPayloadFormulasWithoutChangingStringOnlyCounters() throws Exception {
        DataValue unicode = DataValue.string("\0é😀");
        assertEquals(
            25,
            DataValue.integer(1)
                .getStatePayloadBytes());
        assertEquals(
            18,
            DataValue.bool(false)
                .getStatePayloadBytes());
        assertEquals(
            19,
            DataValue.string("")
                .getStatePayloadBytes());
        assertEquals(10, unicode.getStringNbtBytes());
        assertEquals(4, unicode.getStringCharacterCount());
        assertEquals(29, unicode.getStatePayloadBytes());
        DataValue fixed = DataValue.array(Arrays.asList(DataValue.integer(1), DataValue.integer(2)));
        DataValue dynamic = DataValue
            .dynamicArray(INTEGERS, new DataValue[] { DataValue.integer(1), DataValue.integer(2) });
        assertEquals(72, fixed.getStatePayloadBytes());
        assertEquals(91, dynamic.getStatePayloadBytes());
        DataValue empty = DataValue.dynamicArray(DataType.dynamicArray(DataType.STRING), new DataValue[0]);
        assertEquals(40, empty.getStatePayloadBytes());
        for (DataValue value : Arrays
            .asList(DataValue.integer(Long.MIN_VALUE), DataValue.bool(true), unicode, fixed, dynamic, empty))
            assertEquals(serializedPayload(value), value.getStatePayloadBytes());
    }

    @Test
    void nestedStructAndFixedArrayUpdatesUsePayloadDeltasForChangedDynamicLengthsAndUnicode() throws Exception {
        DataType strings = DataType.dynamicArray(DataType.STRING);
        DataValue small = DataValue.dynamicArray(strings, new DataValue[] { DataValue.string("a") });
        DataValue large = DataValue
            .dynamicArray(strings, new DataValue[] { DataValue.string("\0"), DataValue.string("😀") });
        DataValue pairs = DataValue.collection(DataType.array(strings, 2), new DataValue[] { small, small });
        Map<String, DataValue> fields = new LinkedHashMap<>();
        fields.put("flag", DataValue.bool(true));
        fields.put("rows", pairs);
        DataValue before = DataValue.struct(fields);
        assertEquals(22 + 18 + 4 + 18 + 18 + 4 + pairs.getStatePayloadBytes(), before.getStatePayloadBytes());
        DataValue changed = before.with(
            before.getType()
                .fieldIndex("rows"),
            pairs.with(1, large));
        assertEquals(
            before.getStatePayloadBytes() - small.getStatePayloadBytes() + large.getStatePayloadBytes(),
            changed.getStatePayloadBytes());
        assertEquals(serializedPayload(before), before.getStatePayloadBytes());
        assertEquals(serializedPayload(changed), changed.getStatePayloadBytes());
        assertEquals(2, before.getStringNbtBytes());
        assertEquals(9, changed.getStringNbtBytes());
        assertEquals(
            1,
            before.get("rows")
                .get(1)
                .size());
        assertEquals(
            2,
            changed.get("rows")
                .get(1)
                .size());
        assertEquals(
            -1,
            DataValue.label(LabelReference.literal("source"))
                .getStatePayloadBytes());
        DataValue handles = DataValue.dynamicArray(
            DataType.dynamicArray(DataType.LABEL_REF),
            new DataValue[] { DataValue.label(LabelReference.literal("a")) });
        assertEquals(-1, handles.getStatePayloadBytes());
        assertEquals(
            -1,
            handles.with(0, DataValue.label(LabelReference.literal("b")))
                .getStatePayloadBytes());
        assertEquals(
            -1,
            DataValue.struct(Collections.singletonMap("handles", handles))
                .getStatePayloadBytes());
    }

    @Test
    void defaultSchemaCountsActualCellsForDynamicValuesIncludingRepeatedSharedChildren() {
        DataValue[] entries = new DataValue[256];
        Arrays.fill(entries, DataValue.integer(0));
        DataValue full = DataValue.dynamicArray(INTEGERS, entries);
        assertEquals(
            1,
            full.getType()
                .getCellCount());
        assertEquals(257, full.getRetainedCellCount());
        java.util.List<ProgramStateSchema.Declaration> declarations = new java.util.ArrayList<>();
        for (int i = 0; i < 15; i++) declarations.add(new ProgramStateSchema.Declaration("a" + i, full));
        assertEquals(15, new ProgramStateSchema(declarations).size());
        declarations.add(new ProgramStateSchema.Declaration("overflow", full));
        assertThrows(IllegalArgumentException.class, () -> new ProgramStateSchema(declarations));
        DataValue[] rows = new DataValue[16];
        Arrays.fill(rows, full);
        rows[15] = DataValue.dynamicArray(INTEGERS, Arrays.copyOf(entries, 239));
        DataValue matrix = DataValue.dynamicArray(DataType.dynamicArray(INTEGERS), rows);
        assertEquals(4096, matrix.getRetainedCellCount());
        ProgramStateSchema.Declaration exact = new ProgramStateSchema.Declaration("matrix", matrix);
        assertEquals(1, new ProgramStateSchema(Collections.singletonList(exact)).size());
        assertThrows(
            IllegalArgumentException.class,
            () -> new ProgramStateSchema(
                Arrays.asList(exact, new ProgramStateSchema.Declaration("extra", DataValue.bool(true)))));
    }

    @Test
    void dynamicPersistenceDependsOnEveryNestedElementTypeAndExistingStringBoundsRemain() {
        assertTrue(INTEGERS.isPersistable());
        assertTrue(
            DataType.array(INTEGERS, 0)
                .isPersistable());
        assertTrue(
            DataType.struct(Collections.singletonMap("items", INTEGERS))
                .isPersistable());
        for (DataType opaque : Arrays.asList(
            DataType.LABEL_REF,
            DataType.RESOURCE_SELECTOR,
            DataType.ITEM_KEY,
            DataType.ITEM_CONTENTS,
            DataType.ITEM_LOCATIONS,
            DataType.FLUID_KEY,
            DataType.FLUID_CONTENTS)) {
            DataType nested = DataType.struct(Collections.singletonMap("values", DataType.dynamicArray(opaque)));
            assertFalse(nested.isPersistable());
            assertEquals(
                -1,
                DataValue.dynamicArray(DataType.dynamicArray(opaque), new DataValue[0])
                    .getStatePayloadBytes());
        }
        char[] text = new char[4096];
        Arrays.fill(text, 'x');
        DataValue string = DataValue.string(new String(text));
        DataValue strings = DataValue
            .dynamicArray(DataType.dynamicArray(DataType.STRING), new DataValue[] { string, string, string, string });
        ProgramStateSchema.Declaration maximum = new ProgramStateSchema.Declaration("maximum", strings);
        assertEquals(1, new ProgramStateSchema(Collections.singletonList(maximum)).size());
        assertThrows(
            IllegalArgumentException.class,
            () -> new ProgramStateSchema(
                Arrays.asList(maximum, new ProgramStateSchema.Declaration("extra", DataValue.string("a")))));
    }

    /**
     * Independent vanilla NBT serialization verifies the cached compound payload, excluding the unnamed root header.
     */
    private static int serializedPayload(DataValue value) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        CompressedStreamTools.write(encode(value), new DataOutputStream(bytes));
        return bytes.size() - 3;
    }

    private static NBTTagCompound encode(DataValue value) {
        NBTTagCompound tag = new NBTTagCompound();
        switch (value.getType()
            .getKind()) {
            case INTEGER:
                tag.setByte("kind", (byte) 1);
                tag.setLong("value", value.asLong());
                break;
            case BOOLEAN:
                tag.setByte("kind", (byte) 2);
                tag.setByte("value", value.asBoolean() ? (byte) 1 : (byte) 0);
                break;
            case STRING:
                tag.setByte("kind", (byte) 5);
                tag.setString("value", value.asString());
                break;
            case ARRAY:
            case DYNAMIC_ARRAY:
                tag.setByte(
                    "kind",
                    value.getType()
                        .getKind() == DataType.Kind.ARRAY ? (byte) 3 : (byte) 6);
                if (value.getType()
                    .getKind() == DataType.Kind.DYNAMIC_ARRAY)
                    tag.setString(
                        "element",
                        value.getType()
                            .getElementType()
                            .getSignature());
                NBTTagList children = new NBTTagList();
                for (int i = 0; i < value.size(); i++) children.appendTag(encode(value.get(i)));
                tag.setTag("value", children);
                break;
            case STRUCT:
                tag.setByte("kind", (byte) 4);
                NBTTagList fields = new NBTTagList();
                for (String name : value.getType()
                    .getFieldNames()) {
                    NBTTagCompound field = new NBTTagCompound();
                    field.setString("name", name);
                    field.setTag("value", encode(value.get(name)));
                    fields.appendTag(field);
                }
                tag.setTag("value", fields);
                break;
            default:
                throw new IllegalArgumentException("Not a persistable test value");
        }
        return tag;
    }
}
