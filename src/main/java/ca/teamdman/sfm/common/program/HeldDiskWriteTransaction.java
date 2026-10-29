package ca.teamdman.sfm.common.program;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import ca.teamdman.sfm.common.item.ModItems;
import ca.teamdman.sfm.common.program.project.ExecutionReference;

/** Server-thread, single-slot publication. Pending storage work never reserves or mutates the physical medium. */
public final class HeldDiskWriteTransaction {

    private final InventoryPlayer inventory;
    private final ItemStack disk;
    private final int slot;
    private final NBTTagCompound metadata;
    private boolean committed;

    private HeldDiskWriteTransaction(InventoryPlayer inventory) {
        if (inventory == null || inventory.currentItem < 0 || inventory.currentItem >= 9) throw changed();
        this.inventory = inventory;
        slot = inventory.currentItem;
        disk = inventory.getCurrentItem();
        requireFresh(disk);
        metadata = disk.getTagCompound() == null ? null
            : (NBTTagCompound) disk.getTagCompound()
                .copy();
    }

    public static HeldDiskWriteTransaction capture(InventoryPlayer inventory) {
        return new HeldDiskWriteTransaction(inventory);
    }

    private static void requireFresh(ItemStack disk) {
        if (disk == null || ModItems.disk == null
            || disk.getItem() != ModItems.disk
            || disk.stackSize != 1
            || disk.getItemDamage() != 0
            || disk.getTagCompound() != null && !disk.getTagCompound()
                .hasNoTags())
            throw new IllegalStateException("Hold one fresh blank SFM disk with no saved data or custom metadata");
    }

    /** Candidate construction and validation finish before the one live inventory replacement. */
    public ItemStack commit(InventoryPlayer current, ExecutionReference reference) {
        if (committed || current != inventory || current.currentItem != slot || current.getCurrentItem() != disk)
            throw changed();
        requireFresh(disk);
        NBTTagCompound present = disk.getTagCompound();
        if (metadata == null ? present != null : !metadata.equals(present)) throw changed();
        if (reference == null) throw new IllegalArgumentException("Execution reference is required");
        ItemStack candidate = disk.copy();
        ProgramDocument.ensure(candidate);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("format", ProgramDiskData.REFERENCE_FORMAT);
        tag.setString("grant", reference.token);
        tag.setString("digest", reference.digest);
        candidate.getTagCompound()
            .setTag(ProgramDiskData.NBT_REFERENCE, tag);
        ProgramStateStore.validateCompleteDisk(candidate);
        // No fallible parsing, storage, schema work or network calls after this publication point.
        current.setInventorySlotContents(slot, candidate);
        committed = true;
        current.markDirty();
        return candidate;
    }

    private static IllegalStateException changed() {
        return new IllegalStateException(
            "Held disk or selected slot changed; nothing was written. Hold a fresh disk and retry.");
    }
}
