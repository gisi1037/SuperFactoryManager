package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ItemContents;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;

/** Cached logical retention costs remain independent of invocation budgets and native sequence type sizes. */
class GlobalValueBoundsTest {

    @Test
    void stringsLabelsAndSelectorsCountTheirRetainedTextIncludingSharedReferences() {
        DataValue text = DataValue.string("hello");
        Map<String, DataValue> fields = new HashMap<>();
        fields.put("text", text);
        fields.put(
            "label",
            DataValue.label(
                LabelReference.literal("output")
                    .side(1)
                    .slots(0, 3)));
        fields.put("item", DataValue.selector(ResourceSelector.item("bounds:item")));
        fields.put("ore", DataValue.selector(ResourceSelector.ore("ingotIron")));
        DataValue value = DataValue.array(Arrays.asList(DataValue.struct(fields), DataValue.struct(fields)));
        assertEquals(11, value.getRetainedCellCount());
        assertEquals(2 * (5 + 6 + 11 + 9), value.getRetainedCharacters());
        assertEquals(10, value.getStringCharacterCount());
        assertEquals(0, value.getRetainedOpaqueBytes());
        assertEquals(
            1,
            DataValue.integer(0)
                .getRetainedCellCount());
        assertEquals(
            0,
            DataValue.bool(true)
                .getRetainedCharacters());
    }

    @Test
    void actualNestedObservationRowsCountEvenThoughTheirTypesDescribeOnlyOneCell() {
        DataValue contents = contents(2);
        assertEquals(
            1,
            contents.getType()
                .getCellCount());
        assertEquals(23, contents.getRetainedCellCount(), "Root + six group cells + two eight-cell locations");
        assertEquals(
            17,
            contents.get(0)
                .get("locations")
                .getRetainedCellCount());
        assertEquals(22, contents.getRetainedCharacters(), "The row ID and item identity each retain the ID");
        assertEquals(11, contents.getStringCharacterCount());
        DataValue nested = DataValue
            .struct(Collections.singletonMap("rows", DataValue.array(Arrays.asList(contents, contents))));
        assertEquals(48, nested.getRetainedCellCount());
        assertEquals(44, nested.getRetainedCharacters());
        assertEquals(2 * contents.getRetainedOpaqueBytes(), nested.getRetainedOpaqueBytes());
    }

    @Test
    void exactNbtAccountsForPayloadCapacityAndAllRetainedCompoundKeyStorage() {
        NBTTagCompound child = new NBTTagCompound();
        child.setByte("inner", (byte) 1);
        NBTTagCompound root = new NBTTagCompound();
        root.setTag("a", child);
        root.setString("long_key", "value");
        DataValue key = key(root);
        assertEquals(1, key.getRetainedCellCount());
        assertEquals(11 + 1 + 5 + 8, key.getRetainedCharacters());
        assertEquals(0, key.getStringCharacterCount(), "NBT string values live only in encoded bytes");
        // 67 encoded bytes retain a 128-byte buffer. Outer key array has four reference slots.
        // Item 40 + registry String 80 + NBT 64 + byte[] 152 + outer array 56 + inner arrays 72
        // + retained key Strings (64 + 72 + 72).
        assertEquals(672, key.getRetainedOpaqueBytes());
        root.setString("later", "not retained");
        child.setString("inner", "changed");
        assertEquals(25, key.getRetainedCharacters());
        assertEquals(672, key.getRetainedOpaqueBytes());
        assertEquals(264, key(null).getRetainedOpaqueBytes(), "Even null NBT retains its 32-byte buffer");
    }

    @Test
    void compoundSidecarSpareCapacityAndEmptyCompoundArraysAreRetained() {
        NBTTagCompound root = new NBTTagCompound();
        for (int i = 0; i < 4; i++) root.setTag("k" + i, new NBTTagCompound());
        DataValue key = key(root);
        assertEquals(19, key.getRetainedCharacters());
        // Five compounds require eight outer reference slots, and each empty compound retains an array.
        // Item/registry 120 + NBT 64 + 64-byte payload with header 88 + outer array 88
        // + root key array 56 + four empty arrays 96 + four Strings 256.
        assertEquals(768, key.getRetainedOpaqueBytes());
    }

    @Test
    void ancestorUpdatesApplyCachedDeltasForEveryMetricWithoutChangingTheOriginal() {
        DataValue small = contents(1), large = contents(3);
        DataValue original = DataValue.array(Arrays.asList(small, small));
        DataValue updated = original.with(1, large);
        assertEquals(31, original.getRetainedCellCount());
        assertEquals(47, updated.getRetainedCellCount());
        assertEquals(44, updated.getRetainedCharacters());
        assertEquals(22, updated.getStringCharacterCount());
        assertEquals(2 * small.getRetainedOpaqueBytes(), updated.getRetainedOpaqueBytes());

        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("long_key", "payload");
        DataValue oldKey = key(null), newKey = key(nbt);
        DataValue keys = DataValue.array(Arrays.asList(oldKey, oldKey));
        DataValue changedKeys = keys.with(0, newKey);
        assertEquals(
            oldKey.getRetainedOpaqueBytes() + newKey.getRetainedOpaqueBytes(),
            changedKeys.getRetainedOpaqueBytes());
        assertEquals(30, changedKeys.getRetainedCharacters());
        assertEquals(22, keys.getRetainedCharacters());

        DataValue strings = DataValue.array(Arrays.asList(DataValue.string("a"), DataValue.string("b")));
        DataValue changedStrings = strings.with(1, DataValue.string("\u0000\u00e9"));
        assertEquals(3, changedStrings.getRetainedCharacters());
        assertEquals(3, changedStrings.getStringCharacterCount());
        assertEquals(5, changedStrings.getStringNbtBytes());
        assertEquals(3, changedStrings.getRetainedCellCount());
    }

    private static DataValue key(NBTTagCompound nbt) {
        return DataValue.itemIdentity(ItemIdentity.of("bounds:item", 0, nbt, new QueryBudget()));
    }

    private static DataValue contents(int locations) {
        QueryBudget budget = new QueryBudget();
        ItemContents.Builder builder = new ItemContents.Builder(budget);
        ItemIdentity item = key(null).asItemIdentity();
        for (int i = 0; i < locations; i++) builder.add(item, 0, i, 64, 0, i, 63, 1);
        return DataValue.contents(builder.build(), budget, new DataRuntime(null, 0, null, budget));
    }
}
