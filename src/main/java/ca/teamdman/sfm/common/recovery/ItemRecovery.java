package ca.teamdman.sfm.common.recovery;

import java.util.UUID;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import ca.teamdman.sfm.common.inventory.InventoryUtils;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.program.ProgramStateStore;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.ast.NumberRangeSet;

/** Explicit server-thread recovery actions; no work is added to normal transfer ticks. */
public final class ItemRecovery {

    public static final String KEY = "sfm:recovery_item";
    public static final String LAST_KEY = "sfm:last_item_recovery";

    private ItemRecovery() {}

    private static NBTTagCompound record(TileEntityManager manager) {
        if (manager.isItemRecoveryInProgress())
            throw new IllegalStateException("An item transfer or recovery claim is still in progress");
        World world = manager.getWorldObj();
        if (world == null || world.isRemote
            || manager.isInvalid()
            || manager.getBlockPos()
                .getTileEntity(world) != manager)
            throw new IllegalStateException("Manager is not loaded on this server");
        ItemStack disk = manager.getDisk();
        if (disk == null || !(disk.getItem() instanceof ItemDisk)
            || !disk.hasTagCompound()
            || !disk.getTagCompound()
                .hasKey(KEY, 10))
            throw new IllegalStateException("No readable item recovery record");
        NBTTagCompound record = disk.getTagCompound()
            .getCompoundTag(KEY);
        if (record.getInteger("version") != 1 && record.getInteger("version") != 2)
            throw new IllegalStateException("Unsupported item recovery version; record retained");
        String state = record.getString("state");
        if (!"pending".equals(state) && !"uncertain".equals(state))
            throw new IllegalStateException("Unsupported item recovery state; record retained");
        if (manager.isRunning()) manager.setRunning(false);
        return record;
    }

    /** Assign an ID to legacy v1 records only on inspection, never on an idle tick. */
    public static NBTTagCompound inspect(TileEntityManager manager) {
        NBTTagCompound record = record(manager);
        if (record.getString("recoveryId")
            .isEmpty()) {
            record.setString(
                "recoveryId",
                UUID.randomUUID()
                    .toString());
            manager.markDirty();
        }
        return (NBTTagCompound) record.copy();
    }

    private static NBTTagCompound matchingRecord(TileEntityManager manager, String id) {
        NBTTagCompound record = record(manager);
        if (id == null || id.isEmpty() || !id.equals(record.getString("recoveryId")))
            throw new IllegalStateException("Recovery ID changed or missing; inspect the current disk first");
        if (manager.getDisk()
            .getTagCompound()
            .hasKey("sfm:recovery_fluid"))
            throw new IllegalStateException("Conflicting item/fluid records; retained for inspection");
        return record;
    }

    private static ItemStack stack(NBTTagCompound record) {
        if (!record.hasKey("stack", 10)) throw new IllegalStateException("Recovery stack is missing");
        ItemStack stack = ItemStack.loadItemStackFromNBT(record.getCompoundTag("stack"));
        if (stack == null || stack.stackSize <= 0
            || stack.stackSize > stack.getMaxStackSize()
            || record.getInteger("version") == 2 && (stack.stackSize != 1 || amount(record) <= 0
                || !record.hasKey("attemptAmount", 3)
                || record.getInteger("attemptAmount") < 0
                || record.getInteger("attemptAmount") > amount(record)))
            throw new IllegalStateException("Recovery stack is invalid or its item is unavailable");
        return stack.copy();
    }

    public static int amount(NBTTagCompound record) {
        return record.getInteger("version") == 2 ? record.hasKey("amount", 3) ? record.getInteger("amount") : 0
            : record.getCompoundTag("stack")
                .getByte("Count");
    }

    /** Claim only known ownership into an adjacent inventory; uncertainty never materializes items. */
    public static int claim(TileEntityManager manager, String id, ForgeDirection direction, String actor) {
        NBTTagCompound record = matchingRecord(manager, id);
        if (!"pending".equals(record.getString("state")))
            throw new IllegalStateException("Uncertain outcome: inspect inventories and use resolve-present");
        manager.setItemRecoveryInProgress(true);
        try {
            return claimPending(manager, id, direction, actor, record);
        } finally {
            manager.setItemRecoveryInProgress(false);
        }
    }

    private static int claimPending(TileEntityManager manager, String id, ForgeDirection direction, String actor,
        NBTTagCompound record) {
        if (direction == null || direction == ForgeDirection.UNKNOWN)
            throw new IllegalArgumentException("Use north, south, east, west, up or down");
        ItemStack owner = manager.getDisk();
        long generation = manager.getDiskGeneration();
        ItemStack remaining = stack(record);
        boolean bulk = record.getInteger("version") == 2;
        int outstanding = amount(record);
        if (bulk && record.getInteger("attemptAmount") != 0)
            throw new IllegalStateException("Pending recovery includes an unresolved attempt");
        requireOwner(manager, owner, generation, record, id);
        World world = manager.getWorldObj();
        BlockPos target = manager.getBlockPos()
            .offset(direction);
        IInventory inventory = InventoryUtils.getInventory(world, target);
        if (inventory == null || inventory instanceof TileEntityManager)
            throw new IllegalStateException("Adjacent loaded inventory required; managers cannot receive recovery");
        ForgeDirection side = direction.getOpposite();
        int moved = 0;
        for (int slot : InventoryUtils.getAccessibleSlots(inventory, side, NumberRangeSet.ALL)) {
            if (outstanding == 0) break;
            remaining.stackSize = Math.min(outstanding, remaining.getMaxStackSize());
            int accepted;
            try {
                accepted = InventoryUtils.insertItem(inventory, slot, side, remaining.copy(), true, false);
            } catch (RuntimeException e) {
                uncertain(manager, record, target, side, slot, actor);
                throw e;
            }
            requireOwner(manager, owner, generation, record, id);
            if (accepted <= 0) continue;
            // Journal uncertainty BEFORE invoking a callback that may mutate and then throw.
            uncertain(manager, record, target, side, slot, actor);
            if (bulk) record.setInteger("attemptAmount", accepted);
            ItemStack offered = remaining.copy();
            offered.stackSize = accepted;
            int inserted = InventoryUtils.insertItem(inventory, slot, side, offered, false, false);
            requireOwner(manager, owner, generation, record, id);
            outstanding -= inserted;
            moved += inserted;
            if (outstanding == 0) {
                archive(manager, record, "claimed", actor);
            } else {
                if (bulk) {
                    record.setInteger("amount", outstanding);
                    record.setInteger("attemptAmount", 0);
                } else {
                    remaining.stackSize = outstanding;
                    record.setTag("stack", remaining.writeToNBT(new NBTTagCompound()));
                }
                record.setString("state", "pending");
                manager.markDirty();
            }
        }
        return moved;
    }

    private static void requireOwner(TileEntityManager manager, ItemStack owner, long generation, NBTTagCompound record,
        String id) {
        if (manager.getDisk() == owner && manager.getDiskGeneration() == generation
            && owner.hasTagCompound()
            && owner.getTagCompound()
                .getCompoundTag(KEY) == record
            && id.equals(record.getString("recoveryId"))
            && manager.getBlockPos()
                .getTileEntity(manager.getWorldObj()) == manager)
            return;
        record.setString("state", "uncertain");
        record.setString("attemptStage", "recovery owner changed");
        manager.setRunning(false);
        throw new IllegalStateException("Recovery owner changed during callback; old record remains uncertain");
    }

    /** Write-ahead ownership evidence for one bounded native extraction and its placements. */
    public static final class BulkJournal {

        private final TileEntityManager manager;
        private final ItemStack disk;
        private final long generation;
        private final World world;
        private final NBTTagCompound root, entry;
        private boolean attempting;

        public BulkJournal(TileEntityManager manager, ItemStack unit, int amount, BlockPos source, int sourceSlot,
            ForgeDirection sourceSide, BlockPos target, int targetSlot, ForgeDirection targetSide) {
            this.manager = manager;
            disk = manager.getDisk();
            generation = manager.getDiskGeneration();
            world = manager.getWorldObj();
            if (disk == null || !(disk.getItem() instanceof ItemDisk)
                || manager.hasPendingRecovery()
                || amount <= 0
                || unit.stackSize != 1) throw new IllegalStateException("No available disk for bulk item recovery");
            manager.flushProgramState();
            NBTTagCompound prior = disk.getTagCompound();
            root = prior == null ? new NBTTagCompound() : prior;
            entry = new NBTTagCompound();
            entry.setInteger("version", 2);
            entry.setString("state", "uncertain");
            entry.setString(
                "recoveryId",
                UUID.randomUUID()
                    .toString());
            entry.setTag("stack", unit.writeToNBT(new NBTTagCompound()));
            entry.setInteger("amount", amount);
            entry.setInteger("attemptAmount", amount);
            entry.setString("stage", "destination insertion");
            entry.setInteger("sourceX", source.x);
            entry.setInteger("sourceY", source.y);
            entry.setInteger("sourceZ", source.z);
            entry.setInteger("sourceSlot", sourceSlot);
            entry.setInteger("sourceSide", sourceSide.ordinal());
            entry.setInteger("sourceDimension", world.provider.dimensionId);
            setTarget(entry, target, targetSlot, targetSide, world.provider.dimensionId);

            NBTTagCompound preview = new NBTTagCompound();
            for (String key : root.func_150296_c()) preview.setTag(key, root.getTag(key));
            preview.setTag(KEY, entry);
            ItemStack previewDisk = new ItemStack(disk.getItem(), 1, disk.getItemDamage());
            previewDisk.setTagCompound(preview);
            ProgramStateStore.validateCompleteDisk(previewDisk);
            if (!attached() || disk.getTagCompound() != prior || manager.hasPendingRecovery())
                throw new IllegalStateException("Bulk item transfer disk changed before journaling");
            if (prior == null) disk.setTagCompound(root);
            root.setTag(KEY, entry);
            attempt("extraction", amount);
        }

        private boolean attached() {
            return manager.getWorldObj() == world && !manager.isInvalid()
                && manager.getDisk() == disk
                && manager.getDiskGeneration() == generation
                && manager.getBlockPos()
                    .getTileEntity(world) == manager;
        }

        public void check() {
            if (!attached() || disk.getTagCompound() != root || root.getTag(KEY) != entry)
                throw new IllegalStateException("Bulk item transfer owner changed; reconcile the original disk");
        }

        public void attempt(String stage, int count) {
            check();
            attempting = true;
            entry.setString("stage", stage);
            entry.setInteger("attemptAmount", count);
            manager.markDirty();
        }

        public void placement(BlockPos target, int slot, ForgeDirection side, int count) {
            attempt("destination insertion", count);
            setTarget(entry, target, slot, side, world.provider.dimensionId);
        }

        public void confirmed(int outstanding) {
            check();
            attempting = false;
            entry.setInteger("amount", outstanding);
            entry.setInteger("attemptAmount", 0);
            manager.markDirty();
        }

        public void finish() {
            check();
            if (attempting || amount(entry) != 0) throw new IllegalStateException("Bulk item transfer is unresolved");
            root.removeTag(KEY);
            manager.markDirty();
        }

        public void pause() {
            if (attached() && disk.getTagCompound() == root && root.getTag(KEY) == entry) {
                if (!attempting && amount(entry) > 0) entry.setString("state", "pending");
                else if (!attempting) root.removeTag(KEY);
                manager.markDirty();
            }
            manager.setRunning(false);
        }
    }

    private static void uncertain(TileEntityManager manager, NBTTagCompound record, BlockPos target,
        ForgeDirection side, int slot, String actor) {
        record.setString("state", "uncertain");
        record.setString("attemptStage", "recovery claim");
        setTarget(record, target, slot, side, manager.getWorldObj().provider.dimensionId);
        record.setString("actor", actor == null ? "" : actor);
        manager.markDirty();
    }

    public static void setTarget(NBTTagCompound record, BlockPos target, int slot, ForgeDirection side, int dimension) {
        record.setInteger("targetX", target.x);
        record.setInteger("targetY", target.y);
        record.setInteger("targetZ", target.z);
        record.setInteger("targetSlot", slot);
        record.setInteger("targetSide", side.ordinal());
        record.setInteger("targetDimension", dimension);
    }

    /** Operator acknowledgement only: no resource is created, inserted, or automatically replayed. */
    public static void resolvePresent(TileEntityManager manager, String id, String actor) {
        NBTTagCompound record = matchingRecord(manager, id);
        if (!"uncertain".equals(record.getString("state"))) throw new IllegalStateException(
            "Known remainder must be claimed; resolve-present would discard owned items");
        ItemStack owner = manager.getDisk();
        long generation = manager.getDiskGeneration();
        manager.setItemRecoveryInProgress(true);
        try {
            stack(record);
            requireOwner(manager, owner, generation, record, id);
            reconcile(manager, record, actor);
        } finally {
            manager.setItemRecoveryInProgress(false);
        }
    }

    private static void reconcile(TileEntityManager manager, NBTTagCompound record, String actor) {
        if (record.getInteger("version") == 2) {
            // Reconcile only the callback's offered slice. Later slots were never offered these items.
            int known = amount(record) - record.getInteger("attemptAmount");
            if (known > 0) {
                record.setInteger("amount", known);
                record.setInteger("attemptAmount", 0);
                record.setString("state", "pending");
                record.setString("stage", "unoffered remainder");
                manager.markDirty();
                return;
            }
        }
        archive(manager, record, "resolved-present", actor);
    }

    private static void archive(TileEntityManager manager, NBTTagCompound record, String state, String actor) {
        NBTTagCompound last = (NBTTagCompound) record.copy();
        last.setString("state", state);
        last.setString("actor", actor == null ? "" : actor);
        last.setLong(
            "resolvedAt",
            manager.getWorldObj()
                .getTotalWorldTime());
        // One inactive history record is bounded and cannot be passed back to the claim API.
        manager.getDisk()
            .getTagCompound()
            .setTag(LAST_KEY, last);
        manager.getDisk()
            .getTagCompound()
            .removeTag(KEY);
        manager.itemRecoveryResolved();
    }
}
