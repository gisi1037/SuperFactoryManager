package ca.teamdman.sfm.integration.mui2;

import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;

import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;

/** Stock MUI2 binding. Optional inventory mods extend the framework from their own side. */
public final class PlayerInventoryWidgets {

    private static final SlotGroupWidget.SlotConsumer IDENTITY = (index, slot) -> slot;

    private PlayerInventoryWidgets() {}

    /** Bind the real owner's inventory through MUI2 so inventory capacity extensions participate. */
    public static SlotGroupWidget bind(PanelSyncManager syncManager, EntityPlayer owner) {
        Objects.requireNonNull(syncManager, "syncManager");
        Objects.requireNonNull(owner, "owner");
        syncManager.bindPlayerInventory(owner);
        return create(IDENTITY, owner.inventory);
    }

    /** Use canonical player keys without linking to another inventory implementation. */
    public static SlotGroupWidget create(SlotGroupWidget.SlotConsumer mapper, InventoryPlayer owner) {
        Objects.requireNonNull(mapper, "mapper");
        Objects.requireNonNull(owner, "owner");
        return SlotGroupWidget.playerInventory(mapper);
    }
}
