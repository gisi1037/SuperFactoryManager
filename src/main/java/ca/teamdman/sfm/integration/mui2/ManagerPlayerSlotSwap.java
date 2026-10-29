package ca.teamdman.sfm.integration.mui2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.utils.item.PlayerMainInvWrapper;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

/** Exact player-index number swaps; MUI2 handler slots expose a dummy vanilla inventory. */
public final class ManagerPlayerSlotSwap {

    private ManagerPlayerSlotSwap() {}

    public static boolean owns(Slot slot, EntityPlayer player) {
        if (player == null || !(slot instanceof ModularSlot)) return false;
        ModularSlot modular = (ModularSlot) slot;
        return modular.getItemHandler() instanceof PlayerMainInvWrapper
            && ((PlayerMainInvWrapper) modular.getItemHandler()).getInventoryPlayer() == player.inventory;
    }

    /** Refuse partial swaps. Neither the cursor nor any third player index participates. */
    public static boolean apply(Slot target, Slot hotbar, EntityPlayer player) {
        if (!owns(target, player) || !owns(hotbar, player)) return false;
        ModularSlot destination = (ModularSlot) target;
        ModularSlot source = (ModularSlot) hotbar;
        if (source.getSlotIndex() < 0 || source.getSlotIndex() >= 9
            || destination.isPhantom()
            || source.isPhantom()
            || !destination.func_111238_b()
            || !source.func_111238_b()) return false;
        if (destination.getSlotIndex() == source.getSlotIndex()) return false;
        ItemStack targetStack = destination.getStack(), hotbarStack = source.getStack();
        if (targetStack != null && (!destination.canTakeStack(player) || !fits(source, targetStack))) return false;
        if (hotbarStack != null && (!source.canTakeStack(player) || !fits(destination, hotbarStack))) return false;
        destination.putStack(hotbarStack);
        source.putStack(targetStack);
        return true;
    }

    private static boolean fits(ModularSlot slot, ItemStack stack) {
        return stack.stackSize > 0 && slot.isItemValid(stack)
            && stack.stackSize <= Math.min(slot.getSlotStackLimit(), ModularContainer.stackLimit(slot, stack));
    }
}
