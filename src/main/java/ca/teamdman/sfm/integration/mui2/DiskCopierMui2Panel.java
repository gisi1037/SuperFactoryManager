package ca.teamdman.sfm.integration.mui2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.item.InvWrapper;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ca.teamdman.sfm.common.tileentity.TileEntityDiskCopier;

/** Standard machine window: slots and recipe-viewer space remain owned by MUI2. */
public final class DiskCopierMui2Panel {

    public static final String NAME = "sfm_disk_copier";

    private DiskCopierMui2Panel() {}

    public static ModularPanel build(TileEntityDiskCopier copier, EntityPlayer player, PanelSyncManager sync) {
        StringSyncValue status = new StringSyncValue(copier::getStatus);
        IntSyncValue progress = new IntSyncValue(copier::getProgress);
        sync.syncValue("status", status);
        sync.syncValue("progress", progress);
        String[] groups = { "source", "blank", "output" };
        int[] x = { 30, 83, 154 };
        // One machine group keeps quick-moves between the machine and the player, not between its own slots.
        sync.registerSlotGroup("copier_disks", 3);
        ModularPanel panel = new ModularPanel(NAME).size(224, 204)
            .background(ManagerConsoleTheme.SHELL)
            .resizeableOnDrag(false)
            .excludeAreaInRecipeViewer();
        panel.child(
            new TextWidget<>("Disk Copier").pos(8, 7)
                .size(200, 10)
                .color(0xffefa09a));
        for (int i = 0; i < groups.length; i++) {
            final int slot = i;
            sync.itemSlot(groups[i], new ModularSlot(new InvWrapper(copier), i) {

                @Override
                public boolean isItemValid(ItemStack stack) {
                    return copier.isItemValidForSlot(slot, stack);
                }

                @Override
                public int getItemStackLimit(ItemStack stack) {
                    return 1; // Avoid simulated clear/restore invalidating a pending cycle.
                }

                @Override
                public boolean canTakeStack(EntityPlayer owner) {
                    return copier.isUseableByPlayer(owner);
                }
            }.slotGroup("copier_disks"));
            panel.child(
                new TextWidget<>(groups[i]).pos(x[i] - 12, 25)
                    .size(50, 10)
                    .color(0xffb7bcc5));
            panel.child(
                new ItemSlot().syncHandler(groups[i])
                    .pos(x[i], 39));
        }
        panel.child(
            IKey.dynamic(() -> progress.getIntValue() * 100 / TileEntityDiskCopier.COPY_TICKS + "%")
                .asWidget()
                .pos(109, 43)
                .size(40, 10)
                .color(0xffefa09a));
        panel.child(new TextWidget<>(IKey.dynamic(() -> {
            String text = status.getStringValue();
            if (text == null) return "";
            return text.length() <= 60 ? text : text.substring(0, 57) + "...";
        })).pos(8, 65)
            .size(208, 24)
            .maxWidth(208)
            .textAlign(Alignment.TopLeft)
            .color(0xffd3d6dc)
            .addTooltipLine(IKey.dynamic(status::getStringValue)));
        panel.child(
            new TextWidget<>("5 seconds per copy | no EU").pos(8, 92)
                .size(208, 10)
                .color(0xffb7bcc5));
        panel.child(
            new TextWidget<>("Inventory").pos(28, 108)
                .size(168, 10)
                .color(0xffb7bcc5));
        SlotGroupWidget inventory = PlayerInventoryWidgets.bind(sync, player);
        panel.child(
            inventory.leftRel(.5f)
                .bottom(5));
        return panel;
    }
}
