package ca.teamdman.sfm.common.program;

import java.util.BitSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.data.ItemTransferResult;
import ca.teamdman.sfml.data.ItemTransferResult.Status;
import ca.teamdman.sfml.data.QueryBudget;

/** Plan and validate a whole batch before committing through callback-free storage writes. */
final class ExactTransferPlan<K> {

    abstract static class Cell<K> {

        final TileEntity tile;
        final Object storage;
        final int index, initialAmount, capacity;
        K key;
        int amount;

        Cell(TileEntity tile, Object storage, int index, K key, int amount, int capacity) {
            this.tile = tile;
            this.storage = storage;
            this.index = index;
            this.key = key;
            this.amount = amount;
            this.initialAmount = amount;
            this.capacity = capacity;
        }

        abstract boolean matches(K other);

        int limit(K incoming) {
            return capacity;
        }

        abstract void prepare();

        abstract boolean present();

        abstract boolean unchanged();

        /** Only assign prepared storage. No provider calls, budgets or notifications here. */
        abstract void write();

        void notifyChanged() {
            tile.markDirty();
        }
    }

    private final ProgramContext context;
    private final QueryBudget budget;
    private final ItemStack disk;
    private final long generation;
    private final long requested;

    ExactTransferPlan(ProgramContext context, long requested) {
        this.context = context;
        this.requested = requested;
        budget = context.getInvocationQueryBudget();
        disk = context.getManager()
            .getDisk();
        generation = context.getManager()
            .getDiskGeneration();
    }

    ItemTransferResult execute(List<Cell<K>> sources, List<Cell<K>> destinations) {
        return execute(sources, destinations, false, false);
    }

    /** Ordinary transfer keeps source-major order and may commit a partial result. */
    ItemTransferResult executeOrdered(List<Cell<K>> sources, List<Cell<K>> destinations, boolean all) {
        return execute(sources, destinations, true, all);
    }

    private ItemTransferResult execute(List<Cell<K>> sources, List<Cell<K>> destinations, boolean ordered,
        boolean all) {
        budget.chargeAllocation(2);
        Map<Object, BitSet> selected = new IdentityHashMap<>();
        long stock = 0;
        for (Cell<K> source : sources) {
            budget.chargeSlot();
            BitSet slots = selected.get(source.storage);
            if (slots == null) {
                budget.chargeAllocation(3);
                slots = new BitSet();
                selected.put(source.storage, slots);
            }
            slots.set(source.index);
            if (source.key != null) stock = Math.addExact(stock, source.amount);
        }
        if (ordered) {
            long target = all ? stock : requested;
            if (target == 0) return new ItemTransferResult(0, 0, Status.COMPLETE);
            if (stock == 0) return refused(Status.NO_MATCH);
            long moved = target - allocate(sources, destinations, selected, target, false);
            if (moved == 0) return new ItemTransferResult(target, 0, Status.NO_CAPACITY);
            return commit(sources, destinations, target, moved);
        }
        if (stock < requested) return refused(Status.NO_MATCH);

        // Merge first so an empty slot or tank remains available for a different resource.
        long remaining = allocate(sources, destinations, selected, requested, true);
        if (remaining != 0) remaining = allocate(sources, destinations, selected, remaining, false);
        if (remaining != 0) return refused(Status.NO_CAPACITY);

        return commit(sources, destinations, requested, requested);
    }

    private long allocate(List<Cell<K>> sources, List<Cell<K>> destinations, Map<Object, BitSet> selected,
        long remaining, boolean mergeOnly) {
        for (Cell<K> source : sources) {
            if (remaining == 0) break;
            if (source.key == null || source.amount == 0) continue;
            for (Cell<K> destination : destinations) {
                if (remaining == 0 || source.amount == 0) break;
                budget.chargeSlot();
                BitSet sourceSlots = selected.get(destination.storage);
                if (sourceSlots != null && sourceSlots.get(destination.index)) continue;
                if (destination.key == null ? mergeOnly : !destination.matches(source.key)) continue;
                int room = destination.limit(source.key) - destination.amount;
                if (room <= 0) continue;
                int count = (int) Math.min(remaining, Math.min(source.amount, room));
                if (destination.key == null) destination.key = source.key;
                destination.amount += count;
                source.amount -= count;
                remaining -= count;
            }
        }
        return remaining;
    }

    private ItemTransferResult commit(List<Cell<K>> sources, List<Cell<K>> destinations, long target, long moved) {
        budget.chargeAllocation(3);
        List<Cell<K>> changed = new java.util.ArrayList<>();
        Map<TileEntity, Cell<K>> notifications = new IdentityHashMap<>();
        prepare(sources, changed, notifications);
        prepare(destinations, changed, notifications);
        ItemTransferResult unavailable = new ItemTransferResult(target, 0, Status.UNAVAILABLE);
        for (Cell<K> cell : changed) if (!cell.present()) return unavailable;
        for (Cell<K> cell : changed) if (!cell.unchanged()) return unavailable;
        TileEntityManager manager = context.getManager();
        if (manager.getDisk() != disk || manager.getDiskGeneration() != generation || manager.hasPendingRecovery())
            return unavailable;
        budget.chargeProviders(notifications.size());
        ItemTransferResult result = new ItemTransferResult(
            target,
            moved,
            target == moved ? Status.COMPLETE : Status.PARTIAL);

        for (Cell<K> cell : changed) cell.write();
        context.setDidSomething(true);
        RuntimeException error = null;
        for (Cell<K> cell : notifications.values()) {
            try {
                cell.notifyChanged();
            } catch (RuntimeException failure) {
                if (error == null) error = failure;
            }
        }
        if (error != null) throw new IllegalStateException(
            "Transfer committed " + moved + " before a storage notification failed",
            error);
        return result;
    }

    private void prepare(List<Cell<K>> cells, List<Cell<K>> changed, Map<TileEntity, Cell<K>> notifications) {
        for (Cell<K> cell : cells) {
            if (cell.amount == cell.initialAmount) continue;
            budget.chargeAllocation(2);
            cell.prepare();
            changed.add(cell);
            notifications.put(cell.tile, cell);
        }
    }

    private ItemTransferResult refused(Status status) {
        return new ItemTransferResult(requested, 0, status);
    }
}
