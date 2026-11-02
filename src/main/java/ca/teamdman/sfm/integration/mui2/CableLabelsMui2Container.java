package ca.teamdman.sfm.integration.mui2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.screen.ModularContainer;

import ca.teamdman.sfm.common.network.CableLabelEditor;

/** Label-only capability: deliberately does not implement ManagerEditContext or expose inventory interaction. */
public final class CableLabelsMui2Container extends ModularContainer {

    public final CableLabelEditor editor;

    public CableLabelsMui2Container(CableLabelEditor editor) {
        this.editor = editor;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return (editor == null || editor.canInteract(player)) && super.canInteractWith(player);
    }

    @Override
    public ItemStack slotClick(int slot, int button, int mode, EntityPlayer player) {
        return null;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
        return null;
    }

    @Override
    public boolean canDragIntoSlot(Slot slot) {
        return false;
    }
}
