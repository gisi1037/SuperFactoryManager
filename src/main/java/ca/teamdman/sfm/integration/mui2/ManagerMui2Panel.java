package ca.teamdman.sfm.integration.mui2;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import net.minecraft.entity.player.EntityPlayer;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.DoubleSyncValue;
import com.cleanroommc.modularui.value.sync.GenericSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ca.teamdman.sfm.Config;
import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.gui.ManagerControlToken;
import ca.teamdman.sfm.common.gui.ManagerEditContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

/** Common-side manager widgets; client actions enter through the sided proxy. */
public final class ManagerMui2Panel {

    public static final String PANEL_NAME = "sfm_manager";
    public static final String DISK_SYNC_KEY = "sfm_disk";
    public static final int WIDTH = 312;
    public static final int HEIGHT = 220;

    private ManagerMui2Panel() {}

    /**
     * Builds on both logical sides. The caller's custom container must flush manager
     * STATE before slotClick/transferStackInSlot, before vanilla or MUI2 copies a disk.
     */
    public static ModularPanel build(TileEntityManager manager, EntityPlayer owner, PanelSyncManager syncManager) {
        Objects.requireNonNull(manager, "manager");
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(syncManager, "syncManager");
        syncManager.registerSlotGroup(DISK_SYNC_KEY, 1);
        syncManager.itemSlot(DISK_SYNC_KEY, new ManagerDiskSlot(manager).slotGroup(DISK_SYNC_KEY));
        SlotGroupWidget inventory = PlayerInventoryWidgets.bind(syncManager, owner);

        BooleanSyncValue hasDisk = new BooleanSyncValue(() -> manager.getDisk() != null);
        BooleanSyncValue running = new BooleanSyncValue(manager::isRunning);
        BooleanSyncValue compiled = new BooleanSyncValue(manager::hasCompiledProgram);
        BooleanSyncValue recovery = new BooleanSyncValue(manager::hasPendingRecovery);
        StringSyncValue status = new StringSyncValue(() -> Objects.toString(manager.getStatusMessage(), ""));
        StringSyncValue logLevel = new StringSyncValue(
            () -> manager.getLogLevel()
                .name());
        DoubleSyncValue peak = new DoubleSyncValue(manager::getPeakTickTimeMs);
        int historySize = manager.getTickTimes().length;
        GenericSyncValue<double[], ?> history = GenericSyncValue.builder(double[].class)
            .getter(manager::getTickTimes)
            .equals(Arrays::equals)
            .copy(values -> values.clone())
            .serializer((buffer, values) -> { for (double value : values) buffer.writeDouble(value); })
            .deserializer(buffer -> {
                double[] values = new double[historySize];
                for (int i = 0; i < values.length; i++) values[i] = buffer.readDouble();
                return values;
            })
            .build();
        syncManager.syncValue("has_disk", hasDisk);
        syncManager.syncValue("running", running);
        syncManager.syncValue("compiled", compiled);
        syncManager.syncValue("recovery", recovery);
        syncManager.syncValue("status", status);
        syncManager.syncValue("log_level", logLevel);
        syncManager.syncValue("peak", peak);
        syncManager.syncValue("history", history);
        ManagerControlToken[] issuedToken = { null };
        GenericSyncValue<ManagerControlToken, ?> controlToken = GenericSyncValue.builder(ManagerControlToken.class)
            .getter(() -> {
                ModularContainer container = syncManager.getContainer();
                if (syncManager.isClient() || !(container instanceof ManagerEditContext) || container.windowId < 1)
                    return null;
                ManagerEditContext context = (ManagerEditContext) container;
                long generation = manager.getDiskGeneration();
                ManagerControlToken cached = issuedToken[0];
                if (cached == null || !cached.matches(context.getEditSessionId(), generation, container.windowId)) {
                    cached = new ManagerControlToken(context.getEditSessionId(), generation, container.windowId);
                    issuedToken[0] = cached;
                }
                return cached;
            })
            .serializer((buffer, value) -> value.write(buffer))
            .deserializer(ManagerControlToken::read)
            .equalsDefault()
            .copyImmutable()
            .nullable()
            .build();
        syncManager.syncValue("control_token", controlToken);
        BooleanSupplier diskPresent = hasDisk::getBoolValue;
        ManagerTickGraph graph = new ManagerTickGraph(history::getValue, peak::getDoubleValue);

        // MUI2 logical coordinates track GUI scale. The inventory API owns responsive
        // viewport sizing and scrolling; no raw display pixels or fixed slot count here.
        ModularPanel panel = ModularPanel.defaultPanel(PANEL_NAME, WIDTH, HEIGHT)
            .child(
                IKey.str("Program")
                    .asWidget()
                    .pos(8, 8))
            .child(
                IKey.str("Manager")
                    .asWidget()
                    .pos(144, 8))
            .child(
                IKey.dynamic(() -> "State: " + (running.getBoolValue() ? "\u00a7arunning" : "\u00a7cstopped"))
                    .asWidget()
                    .pos(144, 26))
            .child(
                IKey.dynamic(() -> "Log level: " + logLevel.getStringValue())
                    .asWidget()
                    .pos(144, 39)
                    .scale(0.8f))
            .child(
                new ItemSlot().syncHandler(DISK_SYNC_KEY)
                    .pos(151, 58)
                    .name("program_disk"))
            .child(
                graph.pos(186, 58)
                    .size(114, 38))
            .child(
                IKey.dynamic(graph::readout)
                    .asWidget()
                    .pos(144, 102)
                    .scale(0.85f))
            .child(
                IKey.dynamic(() -> abbreviate(status.getStringValue()))
                    .asWidget()
                    .pos(144, 115)
                    .width(160)
                    .height(10)
                    .scale(0.8f)
                    .addTooltipLine(IKey.dynamic(status::getStringValue)))
            .child(
                IKey.str("Inventory")
                    .asWidget()
                    .pos(144, 127))
            .child(
                inventory.left(140)
                    .bottom(7));

        panel.child(
            button(manager, syncManager, controlToken, "paste", () -> "Paste from clipboard", diskPresent, 8, 24, 120));
        panel.child(button(manager, syncManager, controlToken, "text", () -> "Text", diskPresent, 8, 44, 59));
        panel.child(button(manager, syncManager, controlToken, "visual", () -> "Visual", diskPresent, 69, 44, 59));
        panel.child(
            button(
                manager,
                syncManager,
                controlToken,
                "reset_state",
                () -> "Reset persistent state",
                diskPresent,
                8,
                64,
                120));
        panel.child(
            button(manager, syncManager, controlToken, "example", () -> "View examples", () -> true, 8, 84, 120));
        panel.child(button(manager, syncManager, controlToken, "copy", () -> "Copy code", diskPresent, 8, 104, 78));
        panel.child(
            button(
                manager,
                syncManager,
                controlToken,
                "run",
                () -> running.getBoolValue() ? "Stop" : "Run",
                () -> hasDisk.getBoolValue() && compiled.getBoolValue() && !recovery.getBoolValue(),
                88,
                104,
                40));
        panel.child(button(manager, syncManager, controlToken, "logs", () -> "Logs", diskPresent, 8, 124, 59));
        panel.child(button(manager, syncManager, controlToken, "terminal", () -> "Terminal", () -> true, 69, 124, 59));
        panel.child(
            button(
                manager,
                syncManager,
                controlToken,
                "rebuild",
                () -> "Rebuild cable network",
                diskPresent,
                8,
                144,
                120));
        panel.child(
            button(manager, syncManager, controlToken, "config", () -> "View server config", () -> true, 8, 164, 120));
        if (Config.showDiscordButton) panel
            .child(button(manager, syncManager, controlToken, "discord", () -> "Discord", () -> true, 8, 184, 120));
        panel.child(button(manager, syncManager, controlToken, "reset", () -> "Reset", diskPresent, 251, 8, 53));
        panel.child(
            button(
                manager,
                syncManager,
                controlToken,
                "diagnostics",
                () -> "!",
                () -> hasDisk.getBoolValue() && !status.getStringValue()
                    .isEmpty() && !"Running".equalsIgnoreCase(status.getStringValue()),
                172,
                60,
                12));
        return panel;
    }

    private static String abbreviate(String text) {
        String firstLine = text.replace('\n', ' ')
            .replace('\r', ' ');
        return firstLine.length() <= 32 ? firstLine : firstLine.substring(0, 29) + "...";
    }

    private static ButtonWidget<?> button(TileEntityManager manager, PanelSyncManager sync,
        GenericSyncValue<ManagerControlToken, ?> token, String page, Supplier<String> title, BooleanSupplier available,
        int x, int y, int width) {
        BooleanSupplier ready = () -> available.getAsBoolean() && (!requiresToken(page) || token.getValue() != null);
        return new ButtonWidget<>().pos(x, y)
            .size(width, 16)
            .name("action_" + page)
            .overlay(IKey.dynamic(() -> (ready.getAsBoolean() ? "" : "\u00a78") + title.get()))
            .onMousePressed(mouseButton -> {
                if (mouseButton != 0) return false;
                if (sync.isClient() && ready.getAsBoolean())
                    SuperFactoryManager.proxy.openManagerPage(manager, page, token.getValue());
                return true;
            });
    }

    private static boolean requiresToken(String page) {
        return "logs".equals(page) || "run".equals(page)
            || "reset".equals(page)
            || "reset_state".equals(page)
            || "rebuild".equals(page);
    }
}
