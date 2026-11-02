package ca.teamdman.sfm.common.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import ca.teamdman.sfm.Config;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.program.DiskCopyPlan;

/** One blank, one copy. Preparation and final validation are cold; ordinary work ticks only count down. */
public final class TileEntityDiskCopier extends TileEntity implements ISidedInventory {

    // Preserve the first three indices so existing saves and indexed automation retain their meanings.
    public static final int SOURCE = 0, MEDIUM = 1, OUTPUT = 2, CAPACITY = DiskCopyPlan.MAX_BATCH_SIZE;
    private final ItemStack[] slots = new ItemStack[1 + CAPACITY * 2];
    private DiskCopyPlan pending;
    private int[] inputSlots, outputSlots;
    private ItemStack[] mediaBindings;
    private long generation, attempted = -1;
    private int progress;
    private int cycleTicks = 20;
    private String status = "Insert source and blank disks";

    @Override
    public void updateEntity() {
        if (worldObj == null || worldObj.isRemote || isInvalid() || !ServerUiTasks.isServerThread()) return;
        if (pending == null) {
            if (attempted == generation) return; // Invalid/full/idle inputs do not repeatedly decode disk NBT.
            attempted = generation;
            if (!startBatch()) return;
        }
        if (!pending.ownsSource(slots[SOURCE]) || !workCurrent()) {
            cancel("Disk changed; reinsert media to retry");
            return;
        }
        if (++progress < cycleTicks) return;
        if (!worldObj.blockExists(xCoord, yCoord, zCoord) || worldObj.getTileEntity(xCoord, yCoord, zCoord) != this) {
            cancel("Copier is no longer loaded");
            return;
        }
        ItemStack[] copies;
        DiskCopyPlan finishing = pending;
        long completingGeneration = generation;
        ItemStack source = slots[SOURCE];
        ItemStack[] media = mediaBindings;
        int[] inputs = inputSlots, outputs = outputSlots;
        try {
            copies = finishing.completeBatch(source, media);
        } catch (IllegalArgumentException | IllegalStateException failure) {
            cancel(failure.getMessage());
            return;
        }
        // Even a reentrant metadata reader cannot redirect completion onto replacement media.
        boolean bound = loaded() && slots[SOURCE] == source && DiskCopyPlan.isDisk(source);
        for (int i = 0; bound && i < inputs.length; i++)
            bound = slots[inputs[i]] == media[i] && DiskCopyPlan.isBlank(media[i]) && slots[outputs[i]] == null;
        if (!bound || generation != completingGeneration || pending != finishing) {
            cancel("Disk changed; copy cancelled");
            return;
        }
        // All fallible validation is finished. Publish the complete batch on the server thread.
        for (int i = 0; i < inputs.length; i++) {
            slots[inputs[i]] = null;
            slots[outputs[i]] = copies[i];
        }
        cancel("Copies ready (" + copies.length + ")");
        attempted = -1; // A smaller configured batch may leave more blanks and output space for the next cycle.
        super.markDirty();
    }

    private boolean startBatch() {
        long startingGeneration = generation;
        int[] inputs = new int[CAPACITY], outputs = new int[CAPACITY];
        int space = 0, count = 0;
        for (int i = 0; i < CAPACITY; i++) if (slots[outputSlot(i)] == null) outputs[space++] = outputSlot(i);
        if (space == 0) {
            status = "Remove completed copies";
            return false;
        }
        for (int i = 0; i < CAPACITY && count < Math.min(space, getBatchLimit()); i++)
            if (DiskCopyPlan.isBlank(slots[mediumSlot(i)])) inputs[count++] = mediumSlot(i);
        if (count == 0) {
            status = "Insert fresh blank SFM disks";
            return false;
        }
        ItemStack[] media = new ItemStack[count];
        for (int i = 0; i < count; i++) media[i] = slots[inputs[i]];
        try {
            DiskCopyPlan plan = DiskCopyPlan.prepareBatch(slots[SOURCE], media);
            if (generation != startingGeneration) {
                status = "Disks changed; restarting copy";
                return false;
            }
            inputSlots = java.util.Arrays.copyOf(inputs, count);
            outputSlots = java.util.Arrays.copyOf(outputs, count);
            mediaBindings = media;
            cycleTicks = Math.max(1, Math.min(12000, Config.diskCopierTicksPerCycle));
            pending = plan;
            status = "Copying " + count + " disk(s)";
            return true;
        } catch (IllegalArgumentException | IllegalStateException failure) {
            status = failure.getMessage();
            return false;
        }
    }

    private boolean workCurrent() {
        DiskCopyPlan plan = pending;
        int[] inputs = inputSlots, outputs = outputSlots;
        if (plan == null || inputs == null || outputs == null) return false;
        for (int i = 0; i < inputs.length; i++)
            if (!plan.ownsMedium(i, slots[inputs[i]]) || slots[outputs[i]] != null) return false;
        return pending == plan;
    }

    private boolean loaded() {
        return worldObj != null && !worldObj.isRemote
            && !isInvalid()
            && ServerUiTasks.isServerThread()
            && worldObj.blockExists(xCoord, yCoord, zCoord)
            && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this;
    }

    public static int mediumSlot(int index) {
        if (index < 0 || index >= CAPACITY) throw new IndexOutOfBoundsException("Blank disk index");
        return index == 0 ? MEDIUM : index + 2;
    }

    public static int outputSlot(int index) {
        if (index < 0 || index >= CAPACITY) throw new IndexOutOfBoundsException("Output disk index");
        return index == 0 ? OUTPUT : index + CAPACITY + 1;
    }

    public static boolean isMediumSlot(int slot) {
        return slot == MEDIUM || slot >= 3 && slot <= CAPACITY + 1;
    }

    public static boolean isOutputSlot(int slot) {
        return slot == OUTPUT || slot >= CAPACITY + 2 && slot < 1 + CAPACITY * 2;
    }

    public int getCycleTicks() {
        return pending != null ? cycleTicks : Math.max(1, Math.min(12000, Config.diskCopierTicksPerCycle));
    }

    public int getBatchLimit() {
        return Math.max(1, Math.min(CAPACITY, Config.diskCopierCopiesPerCycle));
    }

    private void cancel(String message) {
        pending = null;
        inputSlots = outputSlots = null;
        mediaBindings = null;
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
            : isMediumSlot(slot) && DiskCopyPlan.isBlank(stack);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        if (side < 0 || side > 5) return new int[0];
        if (side == 1) return new int[] { SOURCE };
        int[] indices = new int[CAPACITY];
        for (int i = 0; i < CAPACITY; i++) indices[i] = side == 0 ? outputSlot(i) : mediumSlot(i);
        return indices;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return side >= 1 && side <= 5
            && (side == 1 ? slot == SOURCE : isMediumSlot(slot))
            && isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return side == 0 && isOutputSlot(slot);
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
