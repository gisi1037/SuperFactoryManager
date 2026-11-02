package ca.teamdman.sfm.common.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.program.DiskCopyPlan;

/** One blank, one copy. Preparation and final validation are cold; ordinary work ticks only count down. */
public final class TileEntityDiskCopier extends TileEntity implements ISidedInventory {

    public static final int SOURCE = 0, MEDIUM = 1, OUTPUT = 2, COPY_TICKS = 100;
    private final ItemStack[] slots = new ItemStack[3];
    private DiskCopyPlan pending;
    private long generation, attempted = -1;
    private int progress;
    private String status = "Insert source and blank disks";

    @Override
    public void updateEntity() {
        if (worldObj == null || worldObj.isRemote || isInvalid() || !ServerUiTasks.isServerThread()) return;
        if (pending == null) {
            if (attempted == generation) return; // Invalid/full/idle inputs do not repeatedly decode disk NBT.
            attempted = generation;
            if (slots[OUTPUT] != null) {
                status = "Remove the output disk";
                return;
            }
            try {
                pending = DiskCopyPlan.prepare(slots[SOURCE], slots[MEDIUM]);
                status = "Copying program and labels";
            } catch (IllegalArgumentException | IllegalStateException failure) {
                status = failure.getMessage();
                return;
            }
        }
        if (!pending.owns(slots[SOURCE], slots[MEDIUM]) || slots[OUTPUT] != null) {
            cancel("Disk changed; reinsert media to retry");
            return;
        }
        if (++progress < COPY_TICKS) return;
        if (!worldObj.blockExists(xCoord, yCoord, zCoord) || worldObj.getTileEntity(xCoord, yCoord, zCoord) != this) {
            cancel("Copier is no longer loaded");
            return;
        }
        ItemStack copy;
        DiskCopyPlan finishing = pending;
        long completingGeneration = generation;
        ItemStack source = slots[SOURCE], medium = slots[MEDIUM];
        try {
            copy = finishing.complete(source, medium);
        } catch (IllegalArgumentException | IllegalStateException failure) {
            cancel(failure.getMessage());
            return;
        }
        // Even a reentrant metadata reader cannot redirect completion onto replacement media.
        if (generation != completingGeneration || pending != finishing
            || slots[SOURCE] != source
            || slots[MEDIUM] != medium
            || slots[OUTPUT] != null
            || !DiskCopyPlan.isDisk(source)
            || !DiskCopyPlan.isBlank(medium)) {
            cancel("Disk changed; copy cancelled");
            return;
        }
        // All fallible validation is finished. Server-thread publication changes both slots together.
        slots[MEDIUM] = null;
        slots[OUTPUT] = copy;
        pending = null;
        progress = 0;
        status = "Copy ready";
        super.markDirty();
    }

    private void cancel(String message) {
        pending = null;
        progress = 0;
        status = message;
    }

    public int getProgress() {
        return progress;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public void markDirty() {
        generation++;
        cancel("Checking disks");
        super.markDirty();
    }

    @Override
    public void invalidate() {
        cancel("Copier unloaded");
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        cancel("Copier unloaded");
        attempted = -1;
        super.onChunkUnload();
    }

    @Override
    public int getSizeInventory() {
        return slots.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot >= 0 && slot < slots.length ? slots[slot] : null;
    }

    @Override
    public ItemStack decrStackSize(int slot, int count) {
        ItemStack stack = getStackInSlot(slot);
        if (stack == null || count <= 0) return null;
        ItemStack taken;
        if (count >= stack.stackSize) {
            taken = stack;
            slots[slot] = null;
        } else taken = stack.splitStack(count);
        markDirty();
        return taken;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = getStackInSlot(slot);
        if (stack != null) {
            slots[slot] = null;
            markDirty();
        }
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot < 0 || slot >= slots.length) throw new IndexOutOfBoundsException("Disk copier slot");
        // Do not truncate an invalid external stack and silently destroy its excess items.
        slots[slot] = stack;
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "tile.sfm.diskCopier.name";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return player != null && worldObj != null
            && !isInvalid()
            && player.worldObj == worldObj
            && worldObj.blockExists(xCoord, yCoord, zCoord)
            && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + .5, yCoord + .5, zCoord + .5) <= 64;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot == SOURCE ? DiskCopyPlan.isDisk(stack) && !DiskCopyPlan.isBlank(stack)
            : slot == MEDIUM && DiskCopyPlan.isBlank(stack);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return side < 0 || side > 5 ? new int[0]
            : side == 0 ? new int[] { OUTPUT } : side == 1 ? new int[] { SOURCE } : new int[] { MEDIUM };
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return side >= 1 && side <= 5 && slot == (side == 1 ? SOURCE : MEDIUM) && isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return side == 0 && slot == OUTPUT;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        for (int i = 0; i < slots.length; i++) {
            tag.removeTag("disk" + i);
            if (slots[i] != null) tag.setTag("disk" + i, slots[i].writeToNBT(new NBTTagCompound()));
        }
        // Pending candidates/STATE/clocks never enter the save; an interrupted cycle restarts unconsumed.
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        for (int i = 0; i < slots.length; i++)
            slots[i] = tag.hasKey("disk" + i, 10) ? ItemStack.loadItemStackFromNBT(tag.getCompoundTag("disk" + i))
                : null;
        generation = 0;
        attempted = -1;
        cancel("Insert source and blank disks");
    }
}
