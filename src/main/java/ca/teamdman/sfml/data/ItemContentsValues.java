package ca.teamdman.sfml.data;

import java.util.List;

import ca.teamdman.sfml.data.ItemContents.Group;
import ca.teamdman.sfml.data.ItemContents.Location;

/** Converts a native observation once, charging its real shape instead of its opaque type's cell count. */
final class ItemContentsValues {

    private ItemContentsValues() {}

    static DataValue materialize(ItemContents contents, QueryBudget owner, DataRuntime runtime) {
        if (contents == null || runtime == null)
            throw new IllegalArgumentException("Item contents and runtime required");
        runtime.requireQueryBudget(owner);
        List<Group> groups = contents.groups(owner);
        int groupCount = groups.size(), locations = 0, characters = 0;
        if (groupCount > ItemContents.MAX_GROUPS) throw new IllegalArgumentException("Too many item groups");
        // The native lists/keys are immutable. Preflight is bounded and allocates no row or child array.
        runtime.work(groupCount);
        owner.chargeSlots(groupCount);
        for (Group group : groups) {
            int count = group.getLocations()
                .size();
            if (count > ItemContents.MAX_LOCATIONS - locations)
                throw new IllegalArgumentException("Too many item locations");
            locations += count;
            characters = Math.addExact(
                characters,
                group.getItem()
                    .getRegistryId()
                    .length());
        }
        // One sequence root, six logical cells per group and eight per physical location.
        int cells = 1 + groupCount * 6 + locations * 8;
        int references = groupCount * 6 + locations * 8;
        runtime.allocate(cells);
        runtime.allocateString(characters);
        // Includes row validation and cached aggregate-string walks in DataValue constructors.
        runtime.work(cells * 3 + characters);
        owner.chargeSlots(cells * 3 + characters);
        owner.chargeAllocation(cells + references);

        DataType groupType = DataType.ITEM_CONTENTS.getElementType();
        DataType locationType = DataType.ITEM_LOCATIONS.getElementType();
        DataValue[] rows = new DataValue[groupCount];
        for (int i = 0; i < groupCount; i++) {
            Group group = groups.get(i);
            ItemIdentity item = group.getItem();
            List<Location> origins = group.getLocations();
            DataValue[] locationRows = new DataValue[origins.size()];
            for (int j = 0; j < locationRows.length; j++) {
                Location origin = origins.get(j);
                DataValue[] fields = new DataValue[7];
                fields[locationType.fieldIndex("dimension")] = DataValue.integer(origin.dimension);
                fields[locationType.fieldIndex("x")] = DataValue.integer(origin.x);
                fields[locationType.fieldIndex("y")] = DataValue.integer(origin.y);
                fields[locationType.fieldIndex("z")] = DataValue.integer(origin.z);
                fields[locationType.fieldIndex("slot")] = DataValue.integer(origin.slot);
                fields[locationType.fieldIndex("sideMask")] = DataValue.integer(origin.sideMask);
                fields[locationType.fieldIndex("amount")] = DataValue.integer(origin.amount);
                locationRows[j] = DataValue.ownedCollection(locationType, fields);
            }
            DataValue[] fields = new DataValue[5];
            fields[groupType.fieldIndex("item")] = DataValue.itemIdentity(item);
            fields[groupType.fieldIndex("id")] = DataValue.string(item.getRegistryId());
            fields[groupType.fieldIndex("metadata")] = DataValue.integer(item.getMetadata());
            fields[groupType.fieldIndex("total")] = DataValue.integer(group.getTotal());
            fields[groupType.fieldIndex("locations")] = DataValue.ownedSequence(DataType.ITEM_LOCATIONS, locationRows);
            rows[i] = DataValue.ownedCollection(groupType, fields);
        }
        return DataValue.ownedSequence(DataType.ITEM_CONTENTS, rows);
    }
}
