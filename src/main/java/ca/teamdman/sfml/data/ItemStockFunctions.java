package ca.teamdman.sfml.data;

/**
 * Pure reads of completed immutable observations. TOTAL/STOCK walk O(rows/groups), without world rescans
 * or allocated indices. Work is charged before each walk; exact matching additionally uses the runtime's
 * shared query budget. These bounds describe implementation work, not a measured whole-pack speedup.
 */
public final class ItemStockFunctions {

    private ItemStockFunctions() {}

    public static long total(DataValue contentsOrLocations, DataRuntime runtime) {
        if (runtime == null || contentsOrLocations == null)
            throw new IllegalArgumentException("TOTAL requires item contents or locations and a runtime");
        DataType type = contentsOrLocations.getType();
        String amount;
        if (type == DataType.ITEM_CONTENTS) amount = "total";
        else if (type == DataType.ITEM_LOCATIONS) amount = "amount";
        else throw new IllegalArgumentException("TOTAL requires item contents or locations");
        int size = contentsOrLocations.size();
        runtime.work(1 + size * 3);
        long total = 0;
        try {
            for (int i = 0; i < size; i++) total = Math.addExact(
                total,
                contentsOrLocations.get(i)
                    .get(amount)
                    .asLong());
        } catch (ArithmeticException overflow) {
            throw new IllegalArgumentException("TOTAL item amount overflow", overflow);
        }
        return total;
    }

    /** The native grouping invariant provides at most one group per exact key. No broad-selector overload. */
    public static long stock(DataValue contents, ItemIdentity key, DataRuntime runtime) {
        if (runtime == null || key == null || contents == null || contents.getType() != DataType.ITEM_CONTENTS)
            throw new IllegalArgumentException("STOCK requires item contents, an exact item key and a runtime");
        int size = contents.size();
        runtime.work(1 + size * 3);
        for (int i = 0; i < size; i++) {
            DataValue row = contents.get(i);
            if (runtime.sameItem(
                row.get("item")
                    .asItemIdentity(),
                key))
                return row.get("total")
                    .asLong();
        }
        return 0;
    }

    public static long shortage(long requested, long available) {
        if (requested < 0 || available < 0) throw new IllegalArgumentException("SHORTAGE requires nonnegative amounts");
        // Subtraction is safe only on this branch: both operands are nonnegative and requested is larger.
        return requested > available ? requested - available : 0;
    }
}
