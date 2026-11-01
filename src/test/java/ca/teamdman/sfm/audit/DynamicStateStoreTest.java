package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.program.ProgramStateStore;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ProgramStateSchema;
import ca.teamdman.sfml.data.ProgramStateSchema.Declaration;

class DynamicStateStoreTest {

    private static final DataType INTEGERS = DataType.dynamicArray(DataType.INTEGER);

    @Test
    void emptyAndNestedDynamicValuesRoundTripThroughSerializedNbt() throws Exception {
        DataValue nested = DataValue
            .struct(Collections.singletonMap("rows", DataValue.array(Arrays.asList(integers(), integers(2, 3)))));
        ProgramStateSchema schema = schema(new Declaration("empty", integers()), new Declaration("nested", nested));
        ItemStack disk = disk();
        ProgramStateStore state = ProgramStateStore.activate(disk, schema);
        state.flush();
        assertEquals(6, valueTag(disk, 0).getByte("kind"));
        assertEquals(DataType.INTEGER.getSignature(), valueTag(disk, 0).getString("element"));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        CompressedStreamTools.writeCompressed(disk.getTagCompound(), bytes);
        ItemStack restoredDisk = disk();
        restoredDisk
            .setTagCompound(CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes.toByteArray())));
        ProgramStateStore restored = ProgramStateStore.activate(restoredDisk, schema);
        assertEquals(
            INTEGERS,
            restored.read(0)
                .getType());
        assertEquals(
            0,
            restored.read(0)
                .size());
        assertEquals(
            0,
            restored.read(1)
                .get("rows")
                .get(0)
                .size());
        assertEquals(
            3,
            restored.read(1)
                .get("rows")
                .get(1)
                .get(1)
                .asLong());
        assertFalse(restored.isDirty());
        assertEquals(0, restored.generation(0), "epochs are activation-local and never persisted");
        assertNotSame(state.identity(), restored.identity());
        assertNotSame(state, state.identity());
    }

    @Test
    void savedDynamicLengthSurvivesDefaultChangesAndTypeChangesRequireExplicitReset() {
        ItemStack disk = disk();
        ProgramStateStore original = ProgramStateStore.activate(disk, schema(new Declaration("values", integers())));
        original.write(0, integers(3, 5, 7));
        original.flush();
        ProgramStateStore compatible = ProgramStateStore
            .activate(disk, schema(new Declaration("values", integers(99))));
        assertEquals(
            3,
            compatible.read(0)
                .size());
        assertEquals(
            7,
            compatible.read(0)
                .get(2)
                .asLong());
        assertFalse(compatible.isDirty());
        NBTTagCompound before = (NBTTagCompound) disk.getTagCompound()
            .copy();
        DataValue booleans = DataValue.dynamicArray(DataType.dynamicArray(DataType.BOOLEAN), new DataValue[0]);
        ProgramStateSchema changed = schema(new Declaration("values", booleans));
        assertThrows(IllegalStateException.class, () -> ProgramStateStore.activate(disk, changed));
        assertEquals(before, disk.getTagCompound());
        ProgramStateStore reset = ProgramStateStore.reset(disk, changed);
        assertEquals(before, disk.getTagCompound());
        assertEquals(
            0,
            reset.read(0)
                .size());
        assertTrue(reset.flush());
        assertEquals(DataType.BOOLEAN.getSignature(), valueTag(disk, 0).getString("element"));
    }

    @Test
    void v1ScalarAndFixedArrayEncodingsRemainUnchanged() {
        ProgramStateSchema schema = schema(
            new Declaration("count", DataValue.integer(4)),
            new Declaration("fixed", DataValue.array(Arrays.asList(DataValue.integer(1), DataValue.integer(2)))));
        ItemStack disk = disk();
        ProgramStateStore state = ProgramStateStore.activate(disk, schema);
        state.flush();
        assertEquals(
            1,
            disk.getTagCompound()
                .getCompoundTag(ProgramStateStore.NBT_KEY)
                .getInteger("version"));
        assertEquals(1, valueTag(disk, 0).getByte("kind"));
        assertEquals(3, valueTag(disk, 1).getByte("kind"));
        assertFalse(valueTag(disk, 1).hasKey("element"));
        assertEquals(
            2,
            ProgramStateStore.activate(disk, schema)
                .read(1)
                .get(1)
                .asLong());
    }

    @Test
    void canonicalDescriptorsReconstructFixedEmptyChildrenAndBoundEmptyDynamicTypeDepth() {
        DataValue fixedEmpty = DataValue.collection(DataType.array(DataType.INTEGER, 0), new DataValue[0]);
        DataValue nested = DataValue
            .dynamicArray(DataType.dynamicArray(fixedEmpty.getType()), new DataValue[] { fixedEmpty });
        DataType deepest = DataType.INTEGER;
        for (int depth = 1; depth < 32; depth++) deepest = DataType.dynamicArray(deepest);
        DataValue emptyDeep = DataValue.dynamicArray(deepest, new DataValue[0]);
        ProgramStateSchema schema = schema(
            new Declaration("fixed", fixedEmpty),
            new Declaration("nested", nested),
            new Declaration("deep", emptyDeep));
        ItemStack disk = disk();
        ProgramStateStore.activate(disk, schema)
            .flush();
        ProgramStateStore restored = ProgramStateStore.activate(disk, schema);
        assertEquals(
            fixedEmpty.getType(),
            restored.read(0)
                .getType());
        assertEquals(
            0,
            restored.read(0)
                .size());
        assertEquals(
            fixedEmpty.getType(),
            restored.read(1)
                .get(0)
                .getType());
        assertEquals(
            0,
            restored.read(1)
                .get(0)
                .size());
        assertEquals(
            32,
            restored.read(2)
                .getType()
                .getDepth());
        valueTag(disk, 2).setString("element", deepest.getSignature());
        assertThrows(IllegalStateException.class, () -> ProgramStateStore.activate(disk, schema));
    }

    @Test
    void cachedPayloadAndCompleteDiskSizesTrackActualNestedShapesAndUnicode() throws Exception {
        DataValue initial = records(record("a", 1));
        ItemStack disk = disk();
        ProgramStateStore state = ProgramStateStore.activate(disk, schema(new Declaration("records", initial)));
        state.flush();
        DataValue grown = records(record("\u0000\uD83C\uDFED", 1, 2, 3), record("other"));
        DataValue changedChild = grown.with(0, record("longer", 9));
        for (DataValue next : new DataValue[] { grown, changedChild, records(), initial }) {
            int previousBytes = integerField(state, "envelopeBytes");
            DataValue previous = state.read(0);
            state.write(0, next);
            assertEquals(
                previousBytes + next.getStatePayloadBytes() - previous.getStatePayloadBytes(),
                integerField(state, "envelopeBytes"));
            assertEquals(next.getRetainedCellCount(), integerField(state, "cells"));
            state.flush();
            assertEquals(next.getStatePayloadBytes(), encodedBytes(valueTag(disk, 0)) - 3);
            assertEquals(encodedBytes(disk.getTagCompound()), integerField(state, "diskBytes"));
            assertEquals(
                encodedBytes(
                    disk.getTagCompound()
                        .getCompoundTag(ProgramStateStore.NBT_KEY))
                    + ProgramStateStore.NBT_KEY.length(),
                integerField(state, "envelopeBytes"));
        }
    }

    @Test
    void hotWritesDoNotSerializeNbtAndSameObjectStructuralWritesOnlyAdvanceEpochs() {
        ItemStack disk = disk();
        CountingString probe = new CountingString();
        disk.getTagCompound()
            .setTag("probe", probe);
        ProgramStateStore state = ProgramStateStore.activate(disk, schema(new Declaration("values", integers(1))));
        state.flush();
        probe.reads = 0;
        Object identity = state.identity();
        state.write(0, state.read(0));
        assertEquals(1, state.generation(0));
        assertSame(identity, state.identity());
        assertFalse(state.isDirty());
        assertFalse(state.flush());
        assertEquals(0, probe.reads);
        state.write(0, integers(2, 3));
        assertEquals(0, probe.reads);
        assertEquals(2, state.generation(0));
        state.write(0, integers(4, 5), false);
        assertEquals(2, state.generation(0));
        assertEquals(0, probe.reads);
        assertTrue(state.flush());
        assertTrue(probe.reads > 0, "the probe observes serialization at the explicit flush boundary");
    }

    @Test
    void transportAndEpochFailuresLeaveValuesCountersEpochsDirtyFlagsAndDiskUnchanged() throws Exception {
        ItemStack disk = disk();
        ProgramStateStore state = ProgramStateStore.activate(disk, schema(new Declaration("values", integers())));
        state.flush();
        disk.getTagCompound()
            .setString("padding", "");
        int padding = ProgramStateStore.MAX_DISK_NBT_BYTES - encodedBytes(disk.getTagCompound());
        disk.getTagCompound()
            .setString("padding", repeat('x', padding));
        assertEquals(ProgramStateStore.MAX_DISK_NBT_BYTES, encodedBytes(disk.getTagCompound()));
        assertFailureUnchanged(state, disk, () -> state.write(0, integers(1)));
        disk.getTagCompound()
            .removeTag("padding");
        state.write(0, state.read(0));
        Field epochs = ProgramStateStore.class.getDeclaredField("generations");
        epochs.setAccessible(true);
        ((long[]) epochs.get(state))[0] = Long.MAX_VALUE;
        assertFailureUnchanged(state, disk, () -> state.write(0, state.read(0)));
    }

    @Test
    void dynamicDiscriminatorsDescriptorsAndChildrenAreStrictlyValidatedWithoutChangingDisk() {
        List<Consumer<NBTTagCompound>> corruptions = Arrays.asList(
            value -> value.removeTag("element"),
            value -> value.setString("element", "integer"),
            value -> value.setString("element", "LABEL_REF"),
            value -> value.setInteger("element", 1),
            value -> value.setByte("kind", (byte) 3),
            value -> value.setString("extra", "unknown"),
            value -> {
                NBTTagCompound bool = new NBTTagCompound();
                bool.setByte("kind", (byte) 2);
                bool.setByte("value", (byte) 1);
                NBTTagList children = new NBTTagList();
                children.appendTag(bool);
                value.setTag("value", children);
            });
        ProgramStateSchema schema = schema(new Declaration("values", integers()));
        for (Consumer<NBTTagCompound> corruption : corruptions) {
            ItemStack disk = disk();
            ProgramStateStore.activate(disk, schema)
                .flush();
            corruption.accept(valueTag(disk, 0));
            NBTTagCompound before = (NBTTagCompound) disk.getTagCompound()
                .copy();
            assertThrows(IllegalStateException.class, () -> ProgramStateStore.activate(disk, schema));
            assertEquals(before, disk.getTagCompound());
        }
    }

    @Test
    void activationCountsReconciledSavedLengthsTogetherWithNewDefaults() {
        DataValue[] rowValues = new DataValue[16];
        Arrays.fill(rowValues, DataValue.bool(true));
        DataValue row = DataValue.array(Arrays.asList(rowValues));
        DataType rowsType = DataType.dynamicArray(row.getType());
        DataValue[] rows = new DataValue[128];
        Arrays.fill(rows, row);
        DataValue large = DataValue.dynamicArray(rowsType, rows);
        DataValue empty = DataValue.dynamicArray(rowsType, new DataValue[0]);
        ProgramStateSchema schema = schema(new Declaration("saved", empty), new Declaration("added", large));
        NBTTagCompound root = new NBTTagCompound(), envelope = new NBTTagCompound(), entry = new NBTTagCompound();
        envelope.setInteger("version", 1);
        envelope.setLong("revision", 0);
        envelope.setInteger("count", 1);
        entry.setString("name", "saved");
        entry.setString("type", rowsType.getSignature());
        NBTTagCompound outer = new NBTTagCompound();
        outer.setByte("kind", (byte) 6);
        outer.setString(
            "element",
            row.getType()
                .getSignature());
        NBTTagList outerValues = new NBTTagList();
        for (int i = 0; i < rows.length; i++) {
            NBTTagCompound encodedRow = new NBTTagCompound();
            encodedRow.setByte("kind", (byte) 3);
            NBTTagList booleans = new NBTTagList();
            for (int j = 0; j < rowValues.length; j++) {
                NBTTagCompound bool = new NBTTagCompound();
                bool.setByte("kind", (byte) 2);
                bool.setByte("value", (byte) 1);
                booleans.appendTag(bool);
            }
            encodedRow.setTag("value", booleans);
            outerValues.appendTag(encodedRow);
        }
        outer.setTag("value", outerValues);
        entry.setTag("value", outer);
        NBTTagList entries = new NBTTagList();
        entries.appendTag(entry);
        envelope.setTag("entries", entries);
        root.setTag(ProgramStateStore.NBT_KEY, envelope);
        ItemStack disk = disk();
        disk.setTagCompound(root);
        IllegalStateException failure = assertThrows(
            IllegalStateException.class,
            () -> ProgramStateStore.activate(disk, schema));
        assertTrue(
            failure.getMessage()
                .contains("4096 cells"),
            failure.getMessage());
    }

    private static void assertFailureUnchanged(ProgramStateStore state, ItemStack disk, Runnable write)
        throws Exception {
        DataValue previous = state.read(0);
        long generation = state.generation(0);
        boolean dirty = state.isDirty();
        Field dirtySlots = ProgramStateStore.class.getDeclaredField("dirtySlots");
        dirtySlots.setAccessible(true);
        boolean[] previousDirtySlots = ((boolean[]) dirtySlots.get(state)).clone();
        int[] counters = { integerField(state, "cells"), integerField(state, "stringCharacters"),
            integerField(state, "envelopeBytes"), integerField(state, "diskBytes") };
        NBTTagCompound before = (NBTTagCompound) disk.getTagCompound()
            .copy();
        assertThrows(IllegalStateException.class, write::run);
        assertSame(previous, state.read(0));
        assertEquals(generation, state.generation(0));
        assertEquals(dirty, state.isDirty());
        assertArrayEquals(previousDirtySlots, (boolean[]) dirtySlots.get(state));
        assertArrayEquals(
            counters,
            new int[] { integerField(state, "cells"), integerField(state, "stringCharacters"),
                integerField(state, "envelopeBytes"), integerField(state, "diskBytes") });
        assertEquals(before, disk.getTagCompound());
    }

    private static int integerField(ProgramStateStore state, String name) throws Exception {
        Field field = ProgramStateStore.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(state);
    }

    private static int encodedBytes(NBTTagCompound value) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        CompressedStreamTools.write(value, new DataOutputStream(bytes));
        return bytes.size();
    }

    private static NBTTagCompound valueTag(ItemStack disk, int slot) {
        return disk.getTagCompound()
            .getCompoundTag(ProgramStateStore.NBT_KEY)
            .getTagList("entries", 10)
            .getCompoundTagAt(slot)
            .getCompoundTag("value");
    }

    private static DataValue record(String text, long... values) {
        Map<String, DataValue> fields = new LinkedHashMap<>();
        fields.put("text", DataValue.string(text));
        fields.put("numbers", integers(values));
        return DataValue.struct(fields);
    }

    private static DataValue records(DataValue... records) {
        DataType type = DataType.dynamicArray(record("").getType());
        return DataValue.dynamicArray(type, records);
    }

    private static DataValue integers(long... values) {
        DataValue[] result = new DataValue[values.length];
        for (int i = 0; i < values.length; i++) result[i] = DataValue.integer(values[i]);
        return DataValue.dynamicArray(INTEGERS, result);
    }

    private static ProgramStateSchema schema(Declaration... declarations) {
        return new ProgramStateSchema(Arrays.asList(declarations));
    }

    private static ItemStack disk() {
        ItemStack disk = new ItemStack(new Item());
        disk.setTagCompound(new NBTTagCompound());
        return disk;
    }

    private static String repeat(char character, int count) {
        char[] text = new char[count];
        Arrays.fill(text, character);
        return new String(text);
    }

    private static final class CountingString extends NBTTagString {

        int reads;

        CountingString() {
            super("serialization probe");
        }

        @Override
        public byte getId() {
            reads++;
            return super.getId();
        }
    }
}
