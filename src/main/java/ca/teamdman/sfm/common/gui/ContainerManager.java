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

        // Slot 0: Disk slot
        addSlotToContainer(new Slot(manager, 0, 186, 18) {

            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack != null && stack.getItem() instanceof ItemDisk;
            }
        });

        // Player Inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 28 + col * 18, 142 + row * 18));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 28 + col * 18, 200));
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
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
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
