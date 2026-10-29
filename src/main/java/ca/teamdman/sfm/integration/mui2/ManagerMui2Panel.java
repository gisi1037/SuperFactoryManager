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
import com.cleanroommc.modularui.widgets.TextWidget;
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
        syncManager.bindPlayerInventory(owner);
        ManagerPlayerInventory inventory = new ManagerPlayerInventory(syncManager, owner.inventory);

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

        // The server binds the same canonical slot keys; only client presentation reflows.
        final int[] page = { 0 };
        ModularPanel panel = new ModularPanel(PANEL_NAME).widthRel(1f)
            .heightRel(1f)
            .pos(0, 0)
            .background(ManagerConsoleTheme.SHELL)
            .resizeableOnDrag(false)
            .excludeAreaInRecipeViewer();
        panel.child(
            new TextWidget<>("Factory Manager").color(0xffefa09a)
                .pos(8, 6)
                .size(110, 12));
        panel.child(
            IKey.str(manager.xCoord + " / " + manager.yCoord + " / " + manager.zCoord)
                .asWidget()
                .pos(120, 7)
                .height(10)
                .scale(.8f));
        panel.child(
            IKey.dynamic(
                () -> recovery.getBoolValue() ? "\u00a7eRecovery blocks activation"
                    : !hasDisk.getBoolValue() ? "No source disk installed"
                        : running.getBoolValue() ? "\u00a7aRunning"
                            : compiled.getBoolValue() ? "Paused" : "Program unavailable")
                .asWidget()
                .pos(32, 23)
                .height(12));
        panel.child(
            new ItemSlot().syncHandler(DISK_SYNC_KEY)
                .pos(8, 21)
                .name("program_disk")
                .addTooltipLine("Installed source disk - manager equipment"));
        panel.child(
            button(
                manager,
                syncManager,
                controlToken,
                "run",
                () -> running.getBoolValue() ? "Pause" : "Start",
                () -> hasDisk.getBoolValue() && compiled.getBoolValue() && !recovery.getBoolValue(),
                0,
                21,
                56).right(8)
                    .background(ManagerConsoleTheme.PRIMARY));
        String[] destinations = { "Overview", "Program", "Network" };
        for (int i = 0; i < destinations.length; i++) {
            final int destination = i;
            panel.child(
                new ButtonWidget<>().pos(8 + i * 61, 42)
                    .size(59, 17)
                    .background(ManagerConsoleTheme.BUTTON)
                    .hoverBackground(ManagerConsoleTheme.HOVER)
                    .overlay(
                        IKey.dynamic(
                            () -> (page[0] == destination ? "\u00a7f" : "\u00a77") + destinations[destination]))
                    .onMousePressed(mouse -> {
                        if (mouse != 0) return false;
                        page[0] = destination;
                        return true;
                    }));
        }
        panel.child(button(manager, syncManager, controlToken, "terminal", () -> "Terminal", () -> true, 191, 42, 58));
        panel.child(button(manager, syncManager, controlToken, "logs", () -> "Logs", diskPresent, 251, 42, 53));
        panel.child(
            IKey.dynamic(() -> "Last tick: " + abbreviate(status.getStringValue()))
                .asWidget()
                .pos(8, 65)
                .widthRelOffset(1f, -64)
                .height(12)
                .scale(.85f)
                .addTooltipLine(IKey.dynamic(status::getStringValue))
                .setEnabledIf(widget -> page[0] == 0));
        panel.child(
            graph.pos(8, 79)
                .widthRelOffset(.5f, -12)
                .height(26)
                .setEnabledIf(widget -> page[0] == 0));
        panel.child(
            IKey.dynamic(graph::readout)
                .asWidget()
                .leftRelAnchor(.5f, 0)
                .top(80)
                .height(12)
                .scale(.8f)
                .setEnabledIf(widget -> page[0] == 0));
        panel.child(
            IKey.dynamic(() -> "Log level: " + logLevel.getStringValue())
                .asWidget()
                .leftRelAnchor(.5f, 0)
                .top(95)
                .height(10)
                .scale(.75f)
                .setEnabledIf(widget -> page[0] == 0));
        panel.child(
            button(manager, syncManager, controlToken, "diagnostics", () -> "Info", diskPresent, 0, 63, 40).right(8)
                .setEnabledIf(widget -> page[0] == 0));
        panel.child(
            IKey.str("Source disk editing; changes apply to this manager.")
                .asWidget()
                .pos(8, 63)
                .height(10)
                .scale(.75f)
                .setEnabledIf(widget -> page[0] == 1));
        String[] programActions = { "text", "visual", "paste", "copy", "example", "reset_state", "reset" };
        String[] programTitles = { "Edit code", "Visual", "Paste", "Copy code", "Example", "Reset state",
            "Reset disk" };
        for (int i = 0; i < programActions.length; i++) {
            final int action = i;
            panel.child(
                button(
                    manager,
                    syncManager,
                    controlToken,
                    programActions[i],
                    () -> programTitles[action],
                    i == 4 ? () -> true : diskPresent,
                    8 + i % 4 * 75,
                    75 + i / 4 * 18,
                    72).setEnabledIf(widget -> page[0] == 1));
        }
        panel.child(
            IKey.str("Live network browser: planned")
                .asWidget()
                .pos(8, 64)
                .height(12)
                .setEnabledIf(widget -> page[0] == 2));
        panel.child(
            IKey.str("Saved labels do not prove loaded inventory access.")
                .asWidget()
                .pos(8, 79)
                .height(10)
                .scale(.75f)
                .setEnabledIf(widget -> page[0] == 2));
        panel.child(
            button(manager, syncManager, controlToken, "rebuild", () -> "Rebuild network", diskPresent, 8, 96, 104)
                .setEnabledIf(widget -> page[0] == 2));
        panel.child(
            button(manager, syncManager, controlToken, "config", () -> "Server limits", () -> true, 115, 96, 90)
                .setEnabledIf(widget -> page[0] == 2));
        if (Config.showDiscordButton) panel.child(
            button(manager, syncManager, controlToken, "discord", () -> "Discord", () -> true, 208, 96, 70)
                .setEnabledIf(widget -> page[0] == 2));
        panel.child(
            inventory.left(0)
                .right(0)
                .top(112)
                .bottom(4));
        return panel;
    }

    private static String abbreviate(String text) {
        String firstLine = text.replace('\n', ' ')
            .replace('\r', ' ');
        return firstLine.length() <= 60 ? firstLine : firstLine.substring(0, 57) + "...";
    }

    private static ButtonWidget<?> button(TileEntityManager manager, PanelSyncManager sync,
        GenericSyncValue<ManagerControlToken, ?> token, String page, Supplier<String> title, BooleanSupplier available,
        int x, int y, int width) {
        BooleanSupplier ready = () -> available.getAsBoolean() && (!requiresToken(page) || token.getValue() != null);
        return new ButtonWidget<>().pos(x, y)
            .size(width, 16)
            .background(ManagerConsoleTheme.BUTTON)
            .hoverBackground(ManagerConsoleTheme.HOVER)
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
