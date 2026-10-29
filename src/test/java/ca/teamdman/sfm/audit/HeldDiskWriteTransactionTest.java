package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.item.ModItems;
import ca.teamdman.sfm.common.program.HeldDiskWriteTransaction;
import ca.teamdman.sfm.common.program.ProgramDiskData;
import ca.teamdman.sfm.common.program.ProgramDocument;
import ca.teamdman.sfm.common.program.ProgramStateStore;
import ca.teamdman.sfm.common.program.project.ExecutionReference;

class HeldDiskWriteTransactionTest {

    private Item previous;
    private final ExecutionReference reference = new ExecutionReference(hex('a'), hex('b'));

    @BeforeEach
    void registeredDisk() {
        previous = ModItems.disk;
        ModItems.disk = new ItemDisk();
    }

    @AfterEach
    void restoreRegistry() {
        ModItems.disk = previous;
    }

    @Test
    void captureIsReadOnlyAndCommitReplacesExactlyOneHeldDiskWithSourceFreeIdentity() {
        InventoryPlayer inventory = inventory();
        ItemStack original = inventory.getCurrentItem(), other = new ItemStack(new Item(), 12);
        inventory.setInventorySlotContents(1, other);
        HeldDiskWriteTransaction write = HeldDiskWriteTransaction.capture(inventory);
        assertNull(original.getTagCompound());
        assertSame(original, inventory.getCurrentItem());
        ItemStack installed = write.commit(inventory, reference);
        assertSame(installed, inventory.getCurrentItem());
        assertNotSame(original, installed);
        assertNull(original.getTagCompound(), "The old medium is never mutated before slot publication");
        assertSame(other, inventory.getStackInSlot(1));
        assertEquals(12, other.stackSize);
        assertEquals(reference, ProgramDiskData.readReference(installed));
        assertNotNull(ProgramDocument.read(installed));
        assertEquals(0, ProgramDocument.read(installed).revision);
        assertFalse(
            installed.getTagCompound()
                .hasKey(ItemDisk.NBT_PROGRAM));
        assertFalse(
            installed.getTagCompound()
                .hasKey(ProgramStateStore.NBT_KEY));
        NBTTagCompound saved = (NBTTagCompound) installed.getTagCompound()
            .copy();
        assertThrows(IllegalStateException.class, () -> write.commit(inventory, reference));
        assertSame(installed, inventory.getCurrentItem());
        assertEquals(saved, installed.getTagCompound());
    }

    @Test
    void anySavedCustomOrUnknownMetadataIsRejectedWithoutClearingIt() {
        for (String key : Arrays.asList(
            ItemDisk.NBT_PROGRAM,
            ProgramDiskData.NBT_REFERENCE,
            ProgramDocument.NBT_KEY,
            ProgramStateStore.NBT_KEY,
            "sfm:labels",
            "sfm:fluid_recovery",
            "sfm:unknown",
            "display",
            "foreign:metadata")) {
            InventoryPlayer inventory = inventory();
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString(key, "");
            inventory.getCurrentItem()
                .setTagCompound(tag);
            NBTTagCompound saved = (NBTTagCompound) tag.copy();
            assertThrows(IllegalStateException.class, () -> HeldDiskWriteTransaction.capture(inventory));
            assertEquals(
                saved,
                inventory.getCurrentItem()
                    .getTagCompound());
        }
    }

    @Test
    void swappedIdenticalMediumSlotCountDamageOrMetadataRejectsAStaleCompletion() {
        for (int change = 0; change < 7; change++) {
            InventoryPlayer inventory = inventory();
            ItemStack physical = inventory.getCurrentItem();
            HeldDiskWriteTransaction write = HeldDiskWriteTransaction.capture(inventory);
            InventoryPlayer current = inventory;
            switch (change) {
                case 0:
                    inventory.currentItem = 1;
                    break;
                case 1:
                    inventory.setInventorySlotContents(0, physical.copy());
                    break;
                case 2:
                    inventory.setInventorySlotContents(0, null);
                    break;
                case 3:
                    physical.stackSize = 2;
                    break;
                case 4:
                    physical.setItemDamage(1);
                    break;
                case 5:
                    physical.setTagCompound(new NBTTagCompound());
                    physical.getTagCompound()
                        .setBoolean("changed", true);
                    break;
                case 6:
                    current = inventory();
                    break;
                default:
                    throw new AssertionError();
            }
            InventoryPlayer checked = current;
            ItemStack before = checked.getCurrentItem();
            assertThrows(IllegalStateException.class, () -> write.commit(checked, reference));
            assertSame(before, checked.getCurrentItem());
            assertNull(ProgramDocument.read(physical));
        }
    }

    @Test
    void emptyMetadataIsFreshButChangingItsPresenceInvalidatesPendingWrite() {
        InventoryPlayer inventory = inventory();
        inventory.getCurrentItem()
            .setTagCompound(new NBTTagCompound());
        HeldDiskWriteTransaction write = HeldDiskWriteTransaction.capture(inventory);
        inventory.getCurrentItem()
            .setTagCompound(null);
        assertThrows(IllegalStateException.class, () -> write.commit(inventory, reference));
        HeldDiskWriteTransaction fresh = HeldDiskWriteTransaction.capture(inventory);
        inventory.getCurrentItem()
            .setTagCompound(new NBTTagCompound());
        assertThrows(IllegalStateException.class, () -> fresh.commit(inventory, reference));
        assertNotNull(
            HeldDiskWriteTransaction.capture(inventory)
                .commit(inventory, reference));
    }

    @Test
    void onlyTheRegisteredSingleUndamagedDiskInARealHotbarSlotCanBeCaptured() {
        InventoryPlayer inventory = inventory();
        assertThrows(IllegalStateException.class, () -> HeldDiskWriteTransaction.capture(null));
        for (int slot : new int[] { -1, 9, Integer.MAX_VALUE }) {
            inventory.currentItem = slot;
            assertThrows(IllegalStateException.class, () -> HeldDiskWriteTransaction.capture(inventory));
        }
        inventory.currentItem = 0;
        for (ItemStack wrong : new ItemStack[] { null, new ItemStack(new ItemDisk()), new ItemStack(ModItems.disk, 0),
            new ItemStack(ModItems.disk, 2), new ItemStack(ModItems.disk, 1, 1) }) {
            inventory.setInventorySlotContents(0, wrong);
            assertThrows(IllegalStateException.class, () -> HeldDiskWriteTransaction.capture(inventory));
            assertSame(wrong, inventory.getCurrentItem());
        }
    }

    @Test
    void preparationFailureLeavesTheFreshPhysicalMediumUnchangedAndRetryable() {
        InventoryPlayer inventory = inventory();
        ItemStack original = inventory.getCurrentItem();
        HeldDiskWriteTransaction write = HeldDiskWriteTransaction.capture(inventory);
        assertThrows(IllegalArgumentException.class, () -> write.commit(inventory, null));
        assertSame(original, inventory.getCurrentItem());
        assertNull(original.getTagCompound());
        assertNotNull(write.commit(inventory, reference));
    }

    @Test
    void independentlyWrittenDisksDoNotShareTheirDocumentIdentityOrMutableNbt() {
        InventoryPlayer first = inventory(), second = inventory();
        ItemStack a = HeldDiskWriteTransaction.capture(first)
            .commit(first, reference);
        ItemStack b = HeldDiskWriteTransaction.capture(second)
            .commit(second, reference);
        assertNotEquals(ProgramDocument.read(a).id, ProgramDocument.read(b).id);
        assertEquals(ProgramDiskData.readReference(a), ProgramDiskData.readReference(b));
        a.getTagCompound()
            .getCompoundTag(ProgramDiskData.NBT_REFERENCE)
            .setString("digest", hex('c'));
        assertEquals(reference, ProgramDiskData.readReference(b));
    }

    private static InventoryPlayer inventory() {
        InventoryPlayer inventory = new InventoryPlayer(null);
        inventory.currentItem = 0;
        inventory.setInventorySlotContents(0, new ItemStack(ModItems.disk));
        return inventory;
    }

    private static String hex(char value) {
        char[] chars = new char[64];
        Arrays.fill(chars, value);
        return new String(chars);
    }
}
