package ca.teamdman.sfm.integration.mui2;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import net.minecraft.entity.player.EntityPlayer;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
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
        BooleanSyncValue canRun = new BooleanSyncValue(manager::canRequestRun);
        BooleanSyncValue recovery = new BooleanSyncValue(manager::hasPendingRecovery);
        StringSyncValue logLevel = new StringSyncValue(
            () -> manager.getLogLevel()
                .name());
        GenericSyncValue<ManagerActivitySample, ?> activity = GenericSyncValue.builder(ManagerActivitySample.class)
            .getter(
                () -> new ManagerActivitySample(
                    manager.getTickTimes(),
                    manager.getTimedUpdateCount(),
                    manager.getManagerTick(),
                    ManagerActivitySample.phase(
                        manager.getDisk() != null,
                        manager.isRunning(),
                        manager.hasCompiledProgram(),
                        manager.canRequestRun(),
                        manager.hasPendingRecovery(),
                        manager.getStatusMessage()),
                    manager.getPeakTickTimeMs()))
            .equalsDefault()
            .copyImmutable()
            .serializer((buffer, value) -> value.write(buffer))
            .deserializer(ManagerActivitySample::read)
            .build();
        syncManager.syncValue("has_disk", hasDisk);
        syncManager.syncValue("running", running);
        syncManager.syncValue("compiled", compiled);
        syncManager.syncValue("can_run", canRun);
        syncManager.syncValue("recovery", recovery);
        syncManager.syncValue("log_level", logLevel);
        syncManager.syncValue("activity", activity);
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
        Supplier<ManagerActivitySample> sampled = () -> activity.getValue() == null ? ManagerActivitySample.EMPTY
            : activity.getValue();

        // The server binds the same canonical slot keys; only client presentation reflows.
        final int[] page = { 0 };
        ModularPanel panel = new ModularPanel(PANEL_NAME).widthRel(1f)
            .heightRel(1f)
            .pos(0, 0)
            .background(ManagerConsoleTheme.SHELL)
            .resizeableOnDrag(false)
            .excludeAreaInRecipeViewer();
        IPanelHandler[] expanded = { null };
        expanded[0] = IPanelHandler.simple(panel, (parent, player) -> {
            ModularPanel detail = new ModularPanel("sfm_manager_activity") {

                @Override
                public boolean disablePanelsBelow() {
                    return true;
                }

                @Override
                public boolean onKeyPressed(char character, int key) {
                    if (key == 1) {
                        closeIfOpen();
                        return true;
                    } // Escape closes only this view.
                    return super.onKeyPressed(character, key);
                }
            }.size(304, 220)
                .background(ManagerConsoleTheme.SHELL);
            detail.child(
                new TextWidget<>("Manager activity").pos(8, 6)
                    .size(210, 12)
                    .color(0xffefa09a));
            detail.child(
                new ManagerActivityView(sampled, true).pos(8, 22)
                    .size(288, 186));
            detail.child(
                new ButtonWidget<>().pos(242, 5)
                    .size(54, 15)
                    .background(ManagerConsoleTheme.BUTTON)
                    .hoverBackground(ManagerConsoleTheme.HOVER)
                    .overlay(IKey.str("Done"))
                    .onMousePressed(mouse -> {
                        if (mouse != 0) return false;
                        expanded[0].closePanel();
                        return true;
                    }));
            return detail;
        }, true);
        panel.child(
            new TextWidget<>("Factory Manager").color(0xffefa09a)
                .pos(8, 6)
                .size(110, 12));
        panel.child(
            IKey.str(manager.xCoord + " / " + manager.yCoord + " / " + manager.zCoord)
                .asWidget()
                .color(0xffb7bcc5)
                .pos(120, 7)
                .height(10)
                .scale(.8f));
        panel.child(
            IKey.dynamic(() -> sampled.get().phase.label)
                .asWidget()
                .color(0xffe7e8eb)
                .pos(32, 23)
                .height(12));
        panel.child(
            new ItemSlot().syncHandler(DISK_SYNC_KEY)
                .pos(8, 21)
                .name("program_disk")
                .addTooltipLine("Installed program disk - manager equipment"));
        panel.child(
            button(
                manager,
                syncManager,
                controlToken,
                "run",
                () -> running.getBoolValue() ? "Pause" : "Start",
                () -> hasDisk.getBoolValue() && canRun.getBoolValue() && !recovery.getBoolValue(),
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
            IKey.dynamic(
                () -> sampled.get()
                    .labels().compactLabel)
                .asWidget()
                .color(0xffb7bcc5)
                .pos(8, 65)
                .widthRelOffset(1f, -72)
                .height(12)
                .scale(.75f)
                .addTooltipLine("Full manager update cost; not transfer throughput or individual statement timing.")
                .setEnabledIf(widget -> page[0] == 0));
        panel.child(
            new ManagerActivityView(sampled, false).pos(8, 79)
                .widthRelOffset(1f, -16)
                .height(26)
                .setEnabledIf(widget -> page[0] == 0));
        panel.child(
            new ButtonWidget<>().pos(0, 63)
                .right(8)
                .size(48, 15)
                .background(ManagerConsoleTheme.BUTTON)
                .hoverBackground(ManagerConsoleTheme.HOVER)
                .overlay(IKey.str("Expand"))
                .onMousePressed(mouse -> {
                    if (mouse != 0) return false;
                    if (syncManager.isClient()) expanded[0].openPanel();
                    return true;
                })
                .setEnabledIf(widget -> page[0] == 0));
        panel.child(
            IKey.str("Source disk editing; changes apply to this manager.")
                .asWidget()
                .color(0xffb7bcc5)
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
                .color(0xffb7bcc5)
                .pos(8, 64)
                .height(12)
                .setEnabledIf(widget -> page[0] == 2));
        panel.child(
            IKey.str("Saved labels do not prove loaded inventory access.")
                .asWidget()
                .color(0xffb7bcc5)
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
