package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.program.DebugConsole;
import ca.teamdman.sfm.common.program.DebugValueFormatter;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ExpressionLimits;
import ca.teamdman.sfml.data.ItemContents;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.StateAccess;

/** Immutable typed rows materialized from an already completed native scan, with no world bootstrap. */
class SfmlContentsValueTest {

    @Test
    void realLengthsAndFixedRowShapesNeedNoPaddingAndChargeActualCells() {
        QueryBudget owner = new QueryBudget();
        ItemContents.Builder builder = new ItemContents.Builder(owner);
        ItemIdentity first = identity("value_test:first", 4, null), second = identity("value_test:second", 9, null);
        builder.add(first, 37, 1, 64, 2, 0, 63, 3);
        builder.add(first, 37, 5, 64, 6, 7, 2, 11);
        builder.add(second, -1, 8, 72, 9, 2, 4, 5);
        ItemContents nativeContents = builder.build();
        DataRuntime runtime = runtime(owner, 0);
        int beforeAllocations = owner.getAllocations();
        DataValue contents = DataValue.contents(nativeContents, owner, runtime);
        assertSame(DataType.ITEM_CONTENTS, contents.getType());
        assertTrue(DataType.ITEM_CONTENTS.isSequence());
        assertTrue(DataType.ITEM_LOCATIONS.isSequence());
        assertEquals(1, DataType.ITEM_CONTENTS.getCellCount());
        assertEquals(0, DataType.ITEM_CONTENTS.getLength(), "Type has no padded array length");
        assertEquals(2, contents.size());
        DataValue row = contents.get(0);
        assertEquals(DataType.ITEM_CONTENTS.getElementType(), row.getType());
        assertEquals(
            "value_test:first",
            row.get("id")
                .asString());
        assertEquals(
            4,
            row.get("metadata")
                .asLong());
        assertEquals(
            14,
            row.get("total")
                .asLong());
        assertEquals(
            first,
            row.get("item")
                .asItemIdentity());
        DataValue locations = row.get("locations");
        assertSame(DataType.ITEM_LOCATIONS, locations.getType());
        assertEquals(2, locations.size());
        DataValue location = locations.get(1);
        assertEquals(DataType.ITEM_LOCATIONS.getElementType(), location.getType());
        assertEquals(
            37,
            location.get("dimension")
                .asLong());
        assertEquals(
            5,
            location.get("x")
                .asLong());
        assertEquals(
            64,
            location.get("y")
                .asLong());
        assertEquals(
            6,
            location.get("z")
                .asLong());
        assertEquals(
            7,
            location.get("slot")
                .asLong());
        assertEquals(
            2,
            location.get("sideMask")
                .asLong());
        assertEquals(
            11,
            location.get("amount")
                .asLong());
        assertEquals(
            1,
            contents.get(1)
                .get("locations")
                .size());
        assertEquals(37, runtime.getAllocatedCells(), "1 root + 6 per group + 8 per location");
        assertEquals(73, owner.getAllocations() - beforeAllocations, "Logical cells plus child references");
        assertEquals(
            "value_test:first".length() + "value_test:second".length(),
            runtime.getAllocatedStringCharacters());
        assertThrows(IllegalArgumentException.class, () -> contents.get(2));
        assertThrows(IllegalArgumentException.class, () -> locations.get(2));
    }

    @Test
    void emptySequenceHasARealZeroLengthAndKnownElementType() {
        QueryBudget owner = new QueryBudget();
        ItemContents empty = new ItemContents.Builder(owner).build();
        DataRuntime runtime = runtime(owner, 0);
        DataValue contents = DataValue.contents(empty, owner, runtime);
        assertEquals(0, contents.size());
        assertEquals("[]", DebugValueFormatter.format(contents));
        assertSame(DataType.ITEM_CONTENTS, contents.getType());
        assertEquals(1, runtime.getAllocatedCells());
        assertEquals(0, runtime.getAllocatedStringCharacters());
        assertThrows(IllegalArgumentException.class, () -> contents.get(0));
    }

    @Test
    void contentsAndLocationSequencesRejectUpdatesButCopiedRowStructsStayOrdinaryValues() {
        QueryBudget owner = new QueryBudget();
        DataValue contents = DataValue.contents(one(owner), owner, runtime(owner, 0));
        DataValue row = contents.get(0), locations = row.get("locations"), location = locations.get(0);
        assertThrows(IllegalArgumentException.class, () -> contents.with(0, row));
        assertThrows(IllegalArgumentException.class, () -> locations.with(0, location));
        DataValue edited = row.with(
            row.getType()
                .fieldIndex("total"),
            DataValue.integer(99));
        assertEquals(
            99,
            edited.get("total")
                .asLong());
        assertEquals(
            7,
            contents.get(0)
                .get("total")
                .asLong());
        DataValue editedLocation = location.with(
            location.getType()
                .fieldIndex("amount"),
            DataValue.integer(1));
        assertEquals(
            1,
            editedLocation.get("amount")
                .asLong());
        assertEquals(
            7,
            locations.get(0)
                .get("amount")
                .asLong());
        assertThrows(
            IllegalArgumentException.class,
            () -> DataValue.collection(DataType.ITEM_CONTENTS, new DataValue[] { row }));
    }

    @Test
    void keysAndSequencesCannotPersistDirectlyOrInsideOrdinaryContainers() {
        QueryBudget owner = new QueryBudget();
        DataValue contents = DataValue.contents(one(owner), owner, runtime(owner, 0));
        DataValue key = contents.get(0)
            .get("item"),
            locations = contents.get(0)
                .get("locations");
        assertSame(DataType.ITEM_KEY, key.getType());
        assertEquals(
            4,
            key.asItemIdentity()
                .getMetadata());
        assertThrows(
            IllegalArgumentException.class,
            () -> DataValue.integer(4)
                .asItemIdentity());
        DataValue[] direct = { key, contents, locations };
        int[] writes = { 0 };
        StateAccess state = new StateAccess() {

            @Override
            public DataValue read(int slot) {
                return DataValue.integer(0);
            }

            @Override
            public void write(int slot, DataValue value) {
                writes[0]++;
            }
        };
        DataRuntime runtime = new DataRuntime(state, 0);
        for (DataValue value : direct) {
            assertFalse(
                value.getType()
                    .isPersistable());
            assertThrows(IllegalArgumentException.class, () -> runtime.write(true, 0, value));
            DataValue array = DataValue.array(Collections.singletonList(value));
            Map<String, DataValue> fields = new HashMap<>();
            fields.put("nested", array);
            DataValue struct = DataValue.struct(fields);
            assertFalse(
                array.getType()
                    .isPersistable());
            assertFalse(
                struct.getType()
                    .isPersistable());
            assertThrows(IllegalArgumentException.class, () -> runtime.write(true, 0, struct));
        }
        assertFalse(
            contents.get(0)
                .getType()
                .isPersistable());
        assertTrue(
            locations.get(0)
                .getType()
                .isPersistable(),
            "Copied coordinates contain ordinary integer fields");
        assertEquals(0, writes[0]);
        assertTrue(
            DataType.array(DataType.INTEGER, 3)
                .isSequence());
        assertFalse(DataType.INTEGER.isSequence());
        assertFalse(DataType.ITEM_KEY.isSequence());
    }

    @Test
    void neitherForeignBudgetNorUnownedRuntimeCanAdoptACompletedSnapshot() {
        QueryBudget owner = new QueryBudget(), foreign = new QueryBudget();
        ItemContents snapshot = one(owner);
        DataRuntime foreignRuntime = runtime(foreign, 0), unowned = new DataRuntime(null, 0);
        assertThrows(IllegalArgumentException.class, () -> DataValue.contents(snapshot, owner, foreignRuntime));
        assertThrows(IllegalArgumentException.class, () -> DataValue.contents(snapshot, foreign, foreignRuntime));
        assertThrows(IllegalArgumentException.class, () -> DataValue.contents(snapshot, owner, unowned));
        assertThrows(IllegalArgumentException.class, () -> DataValue.contents(snapshot, null, runtime(owner, 0)));
        assertThrows(IllegalArgumentException.class, () -> DataValue.contents(null, owner, runtime(owner, 0)));
        assertThrows(IllegalArgumentException.class, () -> DataValue.contents(snapshot, owner, null));
        assertEquals(0, foreignRuntime.getAllocatedCells());
        assertEquals(0, unowned.getAllocatedCells());
        assertEquals(0, foreign.getAllocations());
    }

    @Test
    void eachMaterializationBudgetFailsBeforeAnyValueCanBePublished() {
        for (int exhausted = 0; exhausted < 5; exhausted++) {
            QueryBudget owner = new QueryBudget();
            ItemContents snapshot = one(owner);
            DataRuntime runtime = runtime(owner, 1);
            DataValue sentinel = DataValue.integer(42);
            runtime.write(false, 0, sentinel);
            if (exhausted == 0) runtime.allocate(ExpressionLimits.MAX_ALLOCATED_CELLS);
            if (exhausted == 1) runtime.allocateString(ExpressionLimits.MAX_ALLOCATED_STRING_CHARACTERS);
            if (exhausted == 2) runtime.work(ExpressionLimits.MAX_WORK - runtime.getWork());
            if (exhausted == 3) owner.chargeAllocation(QueryBudget.MAX_ALLOCATIONS - owner.getAllocations());
            if (exhausted == 4) owner.chargeSlots(QueryBudget.MAX_SLOT_WORK - owner.getSlotWork());
            assertThrows(IllegalArgumentException.class, () -> {
                DataValue completed = DataValue.contents(snapshot, owner, runtime);
                runtime.write(false, 0, completed);
            });
            // Reading the unchanged local also consumes expression work; the fully exhausted case cannot read.
            if (exhausted != 2) assertSame(sentinel, runtime.read(false, 0));
        }
    }

    @Test
    void snapshotValuesRemainIndependentAndFormattingIsBoundedWithoutSerializingNbt() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("hidden", "PRIVATE_NBT_PAYLOAD");
        ItemIdentity key = identity("value_test:item", 4, nbt);
        String formattedKey = DebugValueFormatter.format(DataValue.itemIdentity(key));
        assertEquals("ITEM_KEY(\"value_test:item\", 4)", formattedKey);
        assertFalse(formattedKey.contains("PRIVATE_NBT_PAYLOAD"));
        assertFalse(formattedKey.contains("hidden"));
        QueryBudget owner = new QueryBudget();
        ItemContents.Builder builder = new ItemContents.Builder(owner);
        for (int i = 0; i < 24; i++) builder.add(identity("value_test:item", i, null), 37, i, 64, 0, i, 63, i + 1);
        ItemContents snapshot = builder.build();
        DataValue value = DataValue.contents(snapshot, owner, runtime(owner, 0));
        builder.add(identity("value_test:later", 0, null), 37, 99, 64, 0, 0, 63, 8);
        assertEquals(24, value.size());
        String formatted = DebugValueFormatter.format(value);
        assertTrue(formatted.startsWith("[{"));
        assertTrue(formatted.endsWith("\u2026"));
        assertTrue(formatted.length() <= DebugConsole.MAX_LINE_LENGTH);
        assertFalse(formatted.contains("value_test:later"));
        assertEquals(24 * "value_test:item".length(), value.getStringCharacterCount());
        DataValue fixed = DataValue.array(Arrays.asList(DataValue.integer(1), DataValue.integer(2)));
        assertEquals(2, fixed.size());
        assertEquals(
            9,
            fixed.with(0, DataValue.integer(9))
                .get(0)
                .asLong());
        assertEquals(
            1,
            fixed.get(0)
                .asLong());
    }

    private static ItemContents one(QueryBudget owner) {
        ItemContents.Builder builder = new ItemContents.Builder(owner);
        builder.add(identity("value_test:item", 4, null), 37, 1, 64, 2, 0, 63, 7);
        return builder.build();
    }

    private static ItemIdentity identity(String id, int metadata, NBTTagCompound nbt) {
        return ItemIdentity.of(id, metadata, nbt, new QueryBudget());
    }

    private static DataRuntime runtime(QueryBudget owner, int locals) {
        return new DataRuntime(null, locals, null, owner);
    }
}
