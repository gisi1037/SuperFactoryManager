package ca.teamdman.sfm.client.gui.mui2;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.TextWidget;

import ca.teamdman.sfm.common.net.PacketManagerClearLogs;
import ca.teamdman.sfm.common.net.PacketManagerRequestLogs;
import ca.teamdman.sfm.common.net.PacketManagerSetLogLevel;
import ca.teamdman.sfm.common.net.SFMNetwork;
import ca.teamdman.sfm.common.tileentity.LogEntry;
import ca.teamdman.sfm.common.tileentity.LogLevel;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

/** Secondary manager panel; its caller retains the manager screen and server container. */
public final class Mui2LogsPanel {

    private static final int PAGE_LINES = 11;
    private static final int TEXT_WIDTH = 284;
    private static final LogLevel[] LEVELS = { LogLevel.OFF, LogLevel.TRACE, LogLevel.DEBUG, LogLevel.INFO,
        LogLevel.WARN, LogLevel.ERROR };
    private static boolean savedWrap;
    private final TileEntityManager manager;
    private final ca.teamdman.sfm.common.gui.ManagerControlToken token;
    private final List<LogEntry> logs = new ArrayList<>();
    private final List<String> lines = new ArrayList<>();
    private LogLevel level;
    private boolean wrap = savedWrap;
    private int page, column, ticks;

    public Mui2LogsPanel(TileEntityManager manager) {
        this(manager, null);
    }

    public Mui2LogsPanel(TileEntityManager manager, ca.teamdman.sfm.common.gui.ManagerControlToken token) {
        this.token = token;
        this.manager = manager;
        level = manager.getLogLevel();
        logs.addAll(manager.getLogs());
    }

    public ModularPanel build(Runnable closeAction) {
        ModularPanel panel = new ModularPanel("sfm_logs") {

            @Override
            public boolean disablePanelsBelow() {
                return true;
            }
        }.size(312, 228);
        panel.child(
            new TextWidget<>("Manager logs").pos(8, 7)
                .size(296, 12));
        for (int i = 0; i < LEVELS.length; i++) {
            final LogLevel selected = LEVELS[i];
            panel.child(button(selected.name(), 8 + i * 50, 23, 46, () -> {
                if (selected == level) return;
                SFMNetwork.WRAPPER.sendToServer(
                    new PacketManagerSetLogLevel(
                        manager.xCoord,
                        manager.yCoord,
                        manager.zCoord,
                        selected.name(),
                        false,
                        token));
                request();
            }).overlay(IKey.dynamic(() -> (selected == level ? "\u00a7a" : "") + selected.name())));
        }
        ListWidget<IWidget, ?> list = new ListWidget<>().pos(8, 46)
            .size(296, 112);
        for (int i = 0; i < PAGE_LINES; i++) {
            final int row = i;
            list.child(new TextWidget<>(IKey.dynamic(() -> displayLine(row))).size(292, 10));
        }
        panel.child(list);
        panel.child(button("Previous", 8, 162, 60, () -> page = Math.max(0, page - 1)));
        panel.child(
            new TextWidget<>(IKey.dynamic(() -> "Page " + (page + 1) + " / " + pages())).pos(72, 166)
                .size(82, 12));
        panel.child(button("Next", 158, 162, 58, () -> page = Math.min(pages() - 1, page + 1)));
        panel.child(button("<", 222, 162, 38, () -> column = Math.max(0, column - 20)));
        panel.child(button(">", 266, 162, 38, () -> { if (!wrap) column = Math.min(maxColumn(), column + 20); }));
        // Leave the minimum screen's bottom overlay strip free for stock pack controls.
        panel.child(button("Copy", 8, 188, 46, this::copy));
        panel.child(button("Clear", 58, 188, 46, () -> {
            SFMNetwork.WRAPPER
                .sendToServer(new PacketManagerClearLogs(manager.xCoord, manager.yCoord, manager.zCoord, token));
            logs.clear();
            page = column = 0;
            rebuild();
            request();
        }));
        panel.child(button("Refresh", 108, 188, 54, this::request));
        panel.child(
            new ButtonWidget<>().pos(166, 188)
                .size(72, 20)
                .overlay(IKey.dynamic(() -> "Wrap: " + (wrap ? "ON" : "OFF")))
                .onMousePressed(button -> {
                    if (button != 0) return false;
                    wrap = !wrap;
                    savedWrap = wrap;
                    page = column = 0;
                    rebuild();
                    return true;
                }));
        panel.child(button("Done", 242, 188, 62, closeAction));
        panel.onUpdateListener(widget -> {
            if (!panel.isOpen()) return;
            if (ticks++ % 10 == 0) request();
            if (ticks % 20 == 0) rebuild();
        });
        rebuild();
        page = pages() - 1;
        return panel;
    }

    /** Called on the client thread by the manager screen's log reply hook. */
    public void receive(int x, int y, int z, String logLevel, List<LogEntry> entries) {
        if (x != manager.xCoord || y != manager.yCoord || z != manager.zCoord) return;
        level = LogLevel.fromString(logLevel);
        logs.clear();
        if (entries != null) logs.addAll(entries);
        rebuild();
    }

    private void request() {
        SFMNetwork.WRAPPER.sendToServer(new PacketManagerRequestLogs(manager.xCoord, manager.yCoord, manager.zCoord));
    }

    private void rebuild() {
        lines.clear();
        if (logs.isEmpty()) {
            lines.add("No log entries. Choose a log level to record execution.");
            lines.add("Trace, debug and info turn off after one execution.");
            lines.add("Logging adds execution overhead. Copy logs to inspect elsewhere.");
        } else {
            long now = System.currentTimeMillis();
            for (LogEntry entry : logs) {
                long seconds = Math.max(0, (now - entry.getTimestamp()) / 1000);
                String text = seconds / 60 + "m"
                    + seconds % 60
                    + "s ago ["
                    + entry.getLevel()
                        .name()
                    + "] "
                    + entry.getMessage();
                for (String line : text.split("\\n", -1)) {
                    if (wrap) lines
                        .addAll(Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(line, TEXT_WIDTH));
                    else lines.add(line);
                }
            }
        }
        page = Math.min(page, pages() - 1);
        column = wrap ? 0 : Math.min(column, maxColumn());
    }

    private int pages() {
        return Math.max(1, (lines.size() + PAGE_LINES - 1) / PAGE_LINES);
    }

    private int maxColumn() {
        int maximum = 0;
        for (String line : lines) maximum = Math.max(maximum, line.length());
        return Math.max(0, maximum - 1);
    }

    private String displayLine(int row) {
        int index = page * PAGE_LINES + row;
        if (index >= lines.size()) return "";
        String line = lines.get(index);
        return Minecraft.getMinecraft().fontRenderer
            .trimStringToWidth(line.substring(Math.min(column, line.length())), TEXT_WIDTH);
    }

    private void copy() {
        StringBuilder text = new StringBuilder("-- SUPER FACTORY MANAGER LOGS --\n");
        long now = System.currentTimeMillis();
        for (LogEntry entry : logs) {
            long seconds = Math.max(0, (now - entry.getTimestamp()) / 1000);
            text.append(seconds / 60)
                .append('m')
                .append(seconds % 60)
                .append("s ago [")
                .append(
                    entry.getLevel()
                        .name())
                .append("] ")
                .append(entry.getMessage())
                .append('\n');
        }
        GuiScreen.setClipboardString(text.toString());
    }

    private static ButtonWidget<?> button(String title, int x, int y, int width, Runnable action) {
        return new ButtonWidget<>().pos(x, y)
            .size(width, 20)
            .overlay(IKey.str(title))
            .onMousePressed(button -> {
                if (button != 0) return false;
                action.run();
                return true;
            });
    }
}
