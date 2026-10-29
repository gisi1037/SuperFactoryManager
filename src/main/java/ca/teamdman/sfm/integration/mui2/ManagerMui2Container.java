package ca.teamdman.sfm.integration.mui2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.screen.ModularContainer;

import ca.teamdman.sfm.common.gui.ManagerEditContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

/** Keeps manager access and disk ownership checks while MUI2 supplies dynamic inventory routing. */
public final class ManagerMui2Container extends ModularContainer implements ManagerEditContext {

    private final TileEntityManager manager;
    private final State edits = new State();

    public ManagerMui2Container(TileEntityManager manager) {
        this.manager = manager;
    }

    @Override
    public TileEntityManager getManager() {
        return manager;
    }

    @Override
    public State getEditState() {
        return edits;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return manager.isUseableByPlayer(player) && super.canInteractWith(player);
    }

    @Override
    public ItemStack slotClick(int slotId, int button, int mode, EntityPlayer player) {
        // Snapshot before MUI2/vanilla makes cursor, swap, drag, drop or quick-move copies.
        manager.flushProgramState();
        if (mode == 2) {
            if (button < 0 || button >= 9 || slotId < 0 || slotId >= inventorySlots.size()) return null;
            Slot target = getSlot(slotId);
            if (ManagerPlayerSlotSwap.owns(target, player)) {
                // Vanilla sees ModularSlot's dummy inventory and can redirect the
                // displaced hotbar stack to an unrelated first-empty player slot.
                ManagerPlayerSlotSwap.apply(target, findPlayerSlot(player, button), player);
                return null;
            }
        }
        return super.slotClick(slotId, button, mode, player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        manager.flushProgramState();
        return super.transferStackInSlot(player, slotIndex);
    }
}
