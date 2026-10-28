package ca.teamdman.sfm.common.recovery;

import java.util.UUID;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import ca.teamdman.sfm.common.inventory.InventoryUtils;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.ast.NumberRangeSet;

/** Explicit server-thread recovery actions; no work is added to normal transfer ticks. */
public final class ItemRecovery {

    public static final String KEY = "sfm:recovery_item";
    public static final String LAST_KEY = "sfm:last_item_recovery";

    private ItemRecovery() {}

    private static NBTTagCompound record(TileEntityManager manager) {
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
        if (record.getInteger("version") != 1)
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
        if (stack == null || stack.stackSize <= 0 || stack.stackSize > stack.getMaxStackSize())
            throw new IllegalStateException("Recovery stack is invalid or its item is unavailable");
        return stack.copy();
    }

    /** Claim only known ownership into an adjacent inventory; uncertainty never materializes items. */
    public static int claim(TileEntityManager manager, String id, ForgeDirection direction, String actor) {
        NBTTagCompound record = matchingRecord(manager, id);
        if (!"pending".equals(record.getString("state")))
            throw new IllegalStateException("Uncertain outcome: inspect inventories and use resolve-present");
        if (direction == null || direction == ForgeDirection.UNKNOWN)
            throw new IllegalArgumentException("Use north, south, east, west, up or down");
        ItemStack remaining = stack(record);
        ItemStack owner = manager.getDisk();
        World world = manager.getWorldObj();
        BlockPos target = manager.getBlockPos()
            .offset(direction);
        IInventory inventory = InventoryUtils.getInventory(world, target);
        if (inventory == null || inventory instanceof TileEntityManager)
            throw new IllegalStateException("Adjacent loaded inventory required; managers cannot receive recovery");
        ForgeDirection side = direction.getOpposite();
        int moved = 0;
        for (int slot : InventoryUtils.getAccessibleSlots(inventory, side, NumberRangeSet.ALL)) {
            if (remaining.stackSize == 0) break;
            int accepted;
            try {
                accepted = InventoryUtils.insertItem(inventory, slot, side, remaining.copy(), true, false);
            } catch (RuntimeException e) {
                uncertain(manager, record, target, side, slot, actor);
                throw e;
            }
            requireOwner(manager, owner, record, id);
            if (accepted <= 0) continue;
            // Journal uncertainty BEFORE invoking a callback that may mutate and then throw.
            uncertain(manager, record, target, side, slot, actor);
            ItemStack offered = remaining.copy();
            offered.stackSize = accepted;
            int inserted = InventoryUtils.insertItem(inventory, slot, side, offered, false, false);
            requireOwner(manager, owner, record, id);
            remaining.stackSize -= inserted;
            moved += inserted;
            if (remaining.stackSize == 0) {
                archive(manager, record, "claimed", actor);
            } else {
                record.setTag("stack", remaining.writeToNBT(new NBTTagCompound()));
                record.setString("state", "pending");
                manager.markDirty();
            }
        }
        return moved;
    }

    private static void requireOwner(TileEntityManager manager, ItemStack owner, NBTTagCompound record, String id) {
        if (manager.getDisk() == owner && owner.hasTagCompound()
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
        stack(record);
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
