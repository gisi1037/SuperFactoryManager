package ca.teamdman.sfm.common.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

public class ContainerManager extends Container {

    private final TileEntityManager manager;

    public ContainerManager(InventoryPlayer playerInv, TileEntityManager manager) {
        this.manager = manager;

        // Slot 0: Disk slot (matches modern SFM manager.png at 15, 47)
        addSlotToContainer(new Slot(manager, 0, 15, 47) {

            @Override
            public ItemStack getStack() {
                // GUI polling observes the last snapshot without serializing live STATE each frame/tick.
                return manager.getDisk();
            }

            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack != null && stack.getItem() instanceof ItemDisk;
            }
        });

        // Player Inventory (3x9 at 8, 84)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Hotbar (1x9 at 8, 142)
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    public TileEntityManager getManager() {
        return manager;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return manager.isUseableByPlayer(player);
    }

    @Override
    public ItemStack slotClick(int slotId, int button, int mode, EntityPlayer player) {
        // Vanilla may copy the disk before removing it in several click modes.
        manager.flushProgramState();
        return super.slotClick(slotId, button, mode, player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= inventorySlots.size()) return null;
        manager.flushProgramState();
        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(slotIndex);

        if (slot != null && slot.getHasStack()) {
            ItemStack current = slot.getStack();
            itemstack = current.copy();

            if (slotIndex == 0) {
                // From Disk slot to player inventory
                if (!this.mergeItemStack(current, 1, 37, true)) {
                    return null;
                }
            } else {
                // From player inventory to Disk slot
                if (current.getItem() instanceof ItemDisk) {
                    if (!this.mergeItemStack(current, 0, 1, false)) {
                        return null;
                    }
                } else {
                    return null;
                }
            }

            if (current.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (current.stackSize == itemstack.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(player, current);
        }

        return itemstack;
    }
}
