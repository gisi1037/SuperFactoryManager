package ca.teamdman.sfm.integration.mui2;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.utils.item.InvWrapper;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ca.teamdman.sfm.common.tileentity.TileEntityManager;

/** A real manager disk slot with side-effect-free validity and display polling. */
public final class ManagerDiskSlot extends ModularSlot {

    private final TileEntityManager manager;

    public ManagerDiskSlot(TileEntityManager manager) {
        super(new InvWrapper(Objects.requireNonNull(manager, "manager")), 0);
        this.manager = manager;
    }

    @Override
    public ItemStack getStack() {
        // Serializing live STATE on every UI poll is both expensive and unnecessary.
        return manager.getDisk();
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        // SlotItemHandler's default simulation temporarily clears/restores a modifiable
        // inventory. That would invalidate edit generations and recompile this disk.
        return manager.isItemValidForSlot(0, stack);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        // Also avoid SlotItemHandler's temporary clear/restore limit probe.
        return manager.getInventoryStackLimit();
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return manager.isUseableByPlayer(player);
    }
}
