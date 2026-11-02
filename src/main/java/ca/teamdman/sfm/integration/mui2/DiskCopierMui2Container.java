package ca.teamdman.sfm.integration.mui2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.screen.ModularContainer;

import ca.teamdman.sfm.common.tileentity.TileEntityDiskCopier;

/** Keep the stock inventory and exact hotbar swap handling, independent of optional inventory extensions. */
public final class DiskCopierMui2Container extends ModularContainer {

    private final TileEntityDiskCopier copier;

    public DiskCopierMui2Container(TileEntityDiskCopier copier) {
        this.copier = copier;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return copier.isUseableByPlayer(player) && super.canInteractWith(player);
    }

    @Override
    public ItemStack slotClick(int slotId, int button, int mode, EntityPlayer player) {
        if (mode == 2) {
            if (button < 0 || button >= 9 || slotId < 0 || slotId >= inventorySlots.size()) return null;
            Slot target = getSlot(slotId);
            if (ManagerPlayerSlotSwap.owns(target, player)) {
                ManagerPlayerSlotSwap.apply(target, findPlayerSlot(player, button), player);
                return null;
            }
        }
        return super.slotClick(slotId, button, mode, player);
    }
}
