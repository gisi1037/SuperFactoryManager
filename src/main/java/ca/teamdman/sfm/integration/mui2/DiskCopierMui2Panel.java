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
        IntSyncValue cycle = new IntSyncValue(copier::getCycleTicks);
        IntSyncValue batch = new IntSyncValue(copier::getBatchLimit);
        sync.syncValue("status", status);
        sync.syncValue("progress", progress);
        sync.syncValue("cycle", cycle);
        sync.syncValue("batch", batch);
        // One machine group keeps quick-moves between the machine and the player, not between its own slots.
        sync.registerSlotGroup("copier_disks", 4);
        ModularPanel panel = new ModularPanel(NAME).size(224, 224)
            .background(ManagerConsoleTheme.SHELL)
            .resizeableOnDrag(false)
            .excludeAreaInRecipeViewer();
        panel.child(
            new TextWidget<>("Disk Copier").pos(8, 7)
                .size(200, 10)
                .color(0xffefa09a));
        panel.child(
            new TextWidget<>("Blank media").pos(8, 23)
                .size(72, 10)
                .color(0xffb7bcc5));
        panel.child(
            new TextWidget<>("Source").pos(91, 23)
                .size(44, 10)
                .color(0xffb7bcc5));
        panel.child(
            new TextWidget<>("Copies").pos(144, 23)
                .size(72, 10)
                .color(0xffb7bcc5));
        addSlot(panel, sync, copier, TileEntityDiskCopier.SOURCE, 103, 36);
        for (int i = 0; i < TileEntityDiskCopier.CAPACITY; i++) {
            addSlot(panel, sync, copier, TileEntityDiskCopier.mediumSlot(i), 8 + i % 4 * 18, 36 + i / 4 * 18);
            addSlot(panel, sync, copier, TileEntityDiskCopier.outputSlot(i), 144 + i % 4 * 18, 36 + i / 4 * 18);
        }
        panel.child(
            IKey.dynamic(() -> progress.getIntValue() * 100 / Math.max(1, cycle.getIntValue()) + "%")
                .asWidget()
                .pos(90, 60)
                .size(44, 10)
                .color(0xffefa09a));
        panel.child(new TextWidget<>(IKey.dynamic(() -> {
            String text = status.getStringValue();
            if (text == null) return "";
            return text.length() <= 60 ? text : text.substring(0, 57) + "...";
        })).pos(8, 95)
            .size(208, 24)
            .maxWidth(208)
            .textAlign(Alignment.TopLeft)
            .color(0xffd3d6dc)
            .addTooltipLine(IKey.dynamic(status::getStringValue)));
        panel.child(IKey.dynamic(() -> {
            int ticks = Math.max(1, cycle.getIntValue());
            return (ticks % 20 == 0 ? ticks / 20 + "s" : ticks + " ticks") + " | up to "
                + Math.max(1, batch.getIntValue())
                + " disks | no EU";
        })
            .asWidget()
            .pos(8, 79)
            .size(208, 10)
            .color(0xffb7bcc5));
        panel.child(
            new TextWidget<>("Inventory").pos(28, 126)
                .size(168, 10)
                .color(0xffb7bcc5));
        SlotGroupWidget inventory = PlayerInventoryWidgets.bind(sync, player);
        panel.child(
            inventory.leftRel(.5f)
                .bottom(5));
        return panel;
    }

    private static void addSlot(ModularPanel panel, PanelSyncManager sync, TileEntityDiskCopier copier, int slot, int x,
        int y) {
        sync.itemSlot("disk", slot, new ModularSlot(new InvWrapper(copier), slot) {

            @Override
            public boolean isItemValid(ItemStack stack) {
                return copier.isItemValidForSlot(slot, stack);
            }

            @Override
            public int getItemStackLimit(ItemStack stack) {
                return 1; // Do not simulate clear/restore: it would cancel a pending cycle.
            }

            @Override
            public boolean canTakeStack(EntityPlayer player) {
                return copier.isUseableByPlayer(player);
            }
        }.slotGroup("copier_disks"));
        panel.child(
            new ItemSlot().syncHandler("disk", slot)
                .pos(x, y)
                .addTooltipLine(
                    slot == TileEntityDiskCopier.SOURCE ? "Source disk is retained; copies start with fresh STATE."
                        : TileEntityDiskCopier.isMediumSlot(slot) ? "Fresh blank SFM disk input"
                            : "Completed copy output"));
    }
}
