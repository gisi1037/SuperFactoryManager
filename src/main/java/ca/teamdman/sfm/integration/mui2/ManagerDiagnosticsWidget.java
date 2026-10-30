package ca.teamdman.sfm.integration.mui2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import ca.teamdman.sfm.client.gui.TerminalCommandCompletion;
import ca.teamdman.sfm.client.gui.TerminalCommandHistory;
import ca.teamdman.sfm.common.gui.ManagerControlToken;
import ca.teamdman.sfm.common.net.PacketDebugTerminalReply;
import ca.teamdman.sfm.common.net.PacketDebugTerminalRequest;
import ca.teamdman.sfm.common.net.PacketManagerClearLogs;
import ca.teamdman.sfm.common.net.PacketManagerLogs;
import ca.teamdman.sfm.common.net.PacketManagerRequestLogs;
import ca.teamdman.sfm.common.net.PacketManagerSetLogLevel;
import ca.teamdman.sfm.common.net.SFMNetwork;
import ca.teamdman.sfm.common.tileentity.LogEntry;
import ca.teamdman.sfm.common.tileentity.LogLevel;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

/** Inline diagnostics keep the machine, installed disk, player inventory and NEI visible. */
final class ManagerDiagnosticsWidget extends ParentWidget<ManagerDiagnosticsWidget> {

    private static final AtomicLong REQUESTS = new AtomicLong();
    private final TileEntityManager manager;
    private final ManagerConsoleBody body;
    private final Supplier<ManagerControlToken> control;
    private final ConsoleOutputWidget output = new ConsoleOutputWidget();
    private final TerminalCommandHistory history = new TerminalCommandHistory();
    private final TerminalCommandCompletion completion = new TerminalCommandCompletion();
    private List<String> terminalResult = Collections
        .singletonList("Type help for commands. PRINT capture starts OFF.");
    private List<String> print = Collections.emptyList(),
        profile = Collections.singletonList("Record full manager update costs; no per-statement tracing."),
        watch = Collections.singletonList("Program globals and persistent variables are read-only snapshots.");
    private List<LogEntry> logs = Collections.emptyList();
    private List<String> filteredLogs = Collections.singletonList("Waiting for retained logs.");
    private String command = "", query = "", variable = "", notice = "", deferred;
    private LogLevel recording = LogLevel.OFF, filter;
    private int ticks, lastPoll = -20, pendingAt, selected = -1, pendingPage, deferredPage;
    private long latestRequest, dropped;
    private boolean pending, capture, printView, wrap = true, live = true, closed;

    ManagerDiagnosticsWidget(TileEntityManager manager, ManagerConsoleBody body,
        Supplier<ManagerControlToken> control) {
        this.manager = manager;
        this.body = body;
        this.control = control;
        name("manager_inline_diagnostics");
        button(
            () -> "Capture: " + (capture ? "ON" : "OFF"),
            0,
            0,
            82,
            () -> request("capture " + (capture ? "off" : "on"))).setEnabledIf(w -> page() == 3);
        button(() -> printView ? "PRINT" : "Results", 84, 0, 56, () -> {
            printView = !printView;
            refreshOutput();
        }).setEnabledIf(w -> page() == 3);
        button(() -> "Clear PRINT", 142, 0, 72, () -> request("clear")).setEnabledIf(w -> page() == 3);
        button(() -> "Help", 216, 0, 38, () -> request("help")).setEnabledIf(w -> page() == 3);
        button(() -> "Copy", 256, 0, 40, this::copy).setEnabledIf(w -> page() == 3);
        TextFieldWidget input = new TextFieldWidget() {

            @Override
            public Result onKeyPressed(char character, int key) {
                if (!isFocused()) return Result.IGNORE;
                if (key == Keyboard.KEY_TAB) {
                    command = completion.complete(getText(), GuiScreen.isShiftKeyDown());
                    setText(command);
                    return Result.SUCCESS;
                }
                if (key == Keyboard.KEY_UP || key == Keyboard.KEY_DOWN) {
                    completion.reset();
                    command = key == Keyboard.KEY_UP ? history.previous(getText()) : history.next(getText());
                    setText(command);
                    return Result.SUCCESS;
                }
                if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) {
                    request(command);
                    return Result.SUCCESS;
                }
                return super.onKeyPressed(character, key);
            }
        }.setMaxLength(128)
            .autoUpdateOnChange(true)
            .setFocusOnGuiOpen(false)
            .value(new StringValue.Dynamic(() -> command, value -> command = value));
        child(
            input.left(0)
                .right(48)
                .bottom(14)
                .height(17)
                .setEnabledIf(w -> page() == 3));
        button(() -> "Run", 0, 0, 44, () -> request(command)).right(0)
            .bottom(14)
            .setEnabledIf(w -> page() == 3);

        button(() -> "Record: " + recording.name(), 0, 0, 88, this::cycleRecording).setEnabledIf(w -> page() == 4);
        button(() -> "Show: " + (filter == null ? "ALL" : filter.name()), 90, 0, 88, () -> {
            filter = filter == null ? LogLevel.ERROR
                : filter == LogLevel.TRACE ? null : LogLevel.values()[filter.ordinal() + 1];
            filterLogs();
        }).setEnabledIf(w -> page() == 4)
            .addTooltipLine("Display filter only. Recording level is a separate server setting.");
        button(() -> "Wrap: " + (wrap ? "ON" : "OFF"), 180, 0, 66, () -> {
            wrap = !wrap;
            output.wrap(wrap);
        }).setEnabledIf(w -> page() == 4);
        button(() -> "Clear", 248, 0, 48, () -> {
            if (control.get() == null) return;
            SFMNetwork.WRAPPER.sendToServer(
                new PacketManagerClearLogs(manager.xCoord, manager.yCoord, manager.zCoord, control.get()));
            logs = Collections.emptyList();
            filterLogs();
        }).setEnabledIf(w -> page() == 4);
        child(
            new TextFieldWidget().setMaxLength(128)
                .autoUpdateOnChange(true)
                .setFocusOnGuiOpen(false)
                .value(new StringValue.Dynamic(() -> query, value -> {
                    query = value;
                    filterLogs();
                }))
                .pos(0, 20)
                .size(128, 16)
                .setEnabledIf(w -> page() == 4)
                .addTooltipLine("Search retained log messages"));
        button(
            () -> output.follows() ? "Follow: ON" : "Follow: OFF",
            130,
            20,
            70,
            () -> output.follow(!output.follows())).setEnabledIf(w -> page() == 4);
        button(() -> live ? "Live" : "Frozen", 202, 20, 46, () -> live = !live).setEnabledIf(w -> page() == 4);
        button(() -> "Copy", 250, 20, 46, this::copy).setEnabledIf(w -> page() == 4);

        button(() -> "Record", 0, 0, 64, () -> request("profile start")).setEnabledIf(w -> page() == 5)
            .addTooltipLine("Measure the next 1,200 full manager updates, including idle work. Stops automatically.");
        button(() -> "Stop", 66, 0, 54, () -> request("profile stop")).setEnabledIf(w -> page() == 5);
        button(() -> "Reset", 122, 0, 54, () -> request("profile reset")).setEnabledIf(w -> page() == 5);
        button(() -> "Refresh", 178, 0, 66, () -> request("profile status")).setEnabledIf(w -> page() == 5);
        button(() -> "Copy", 246, 0, 50, this::copy).setEnabledIf(w -> page() == 5);

        child(
            new TextFieldWidget().setMaxLength(96)
                .autoUpdateOnChange(true)
                .setFocusOnGuiOpen(false)
                .value(new StringValue.Dynamic(() -> variable, value -> variable = value))
                .pos(0, 0)
                .size(136, 17)
                .setEnabledIf(w -> page() == 6)
                .addTooltipLine(
                    "Optional exact $variable name. Leave blank to watch a bounded list. Not an expression evaluator."));
        button(() -> "Read", 138, 0, 46, () -> request(watchCommand())).setEnabledIf(w -> page() == 6);
        button(() -> live ? "Live: ON" : "Live: OFF", 186, 0, 66, () -> live = !live).setEnabledIf(w -> page() == 6);
        button(() -> "Copy", 254, 0, 42, this::copy).setEnabledIf(w -> page() == 6);
        child(
            output.left(0)
                .right(0)
                .top(21)
                .bottom(14));
        child(
            new TextWidget<>(IKey.dynamic(() -> notice)).left(0)
                .right(0)
                .bottom(0)
                .height(10)
                .scale(.65f)
                .color(0xffb5bec8));
    }

    private int page() {
        return body.page();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (closed) return;
        ticks++;
        if (selected != page()) {
            selected = page();
            // Keep accepted/queued commands alive when switching views. Replies are
            // routed to their originating view instead of silently losing a Stop.
            lastPoll = -20;
            output.top(page() == 4 ? 40 : 21)
                .bottom(page() == 3 ? 35 : 14);
            output.follow(page() == 4 || page() == 3 && printView);
            refreshOutput();
            scheduleResize();
        }
        if (page() < 3) return;
        if (pending && ticks - pendingAt >= 60) {
            pending = false;
            notice = "Reply timed out; commands are not automatically repeated.";
            deferred = null;
        }
        if (ticks - lastPoll < 20 || pending) return;
        lastPoll = ticks;
        if (page() == 4) {
            if (live) SFMNetwork.WRAPPER
                .sendToServer(new PacketManagerRequestLogs(manager.xCoord, manager.yCoord, manager.zCoord));
        } else if (page() == 5) request("profile status");
        else if (page() == 6) {
            if (live) request(watchCommand());
        } else request("");
    }

    private String watchCommand() {
        return "watch" + (variable.trim()
            .isEmpty() ? "" : " " + variable.trim());
    }

    void receive(PacketDebugTerminalReply reply) {
        if (closed || !pending
            || reply.requestId != latestRequest
            || reply.windowId != getScreen().getContainer().windowId
            || reply.x != manager.xCoord
            || reply.y != manager.yCoord
            || reply.z != manager.zCoord) return;
        pending = false;
        capture = reply.capturing;
        dropped = reply.dropped;
        print = reply.history;
        if (!reply.result.isEmpty()) {
            if (pendingPage == 5) profile = reply.result;
            else if (pendingPage == 6) watch = reply.result;
            else {
                terminalResult = reply.result;
                printView = false;
            }
        }
        notice = page() == 5 ? "Opt-in full-update timings; not per-instruction or whole-server TPS."
            : page() == 6 ? "Live read-only values; local trigger variables expire after execution."
                : "PRINT: " + print.size() + " retained; " + reply.dropped + " dropped. Tab completes commands.";
        refreshOutput();
        if (deferred != null) {
            String next = deferred;
            int origin = deferredPage;
            deferred = null;
            request(next, origin);
        }
    }

    void receive(PacketManagerLogs reply) {
        if (closed || reply.getX() != manager.xCoord
            || reply.getY() != manager.yCoord
            || reply.getZ() != manager.zCoord) return;
        recording = LogLevel.fromString(reply.getLogLevel());
        logs = new ArrayList<>(reply.getLogs());
        filterLogs();
    }

    void close() {
        closed = true;
        pending = false;
        deferred = null;
    }

    private void request(String text) {
        request(text, page());
    }

    private void request(String text, int origin) {
        if (closed || origin < 3 || text == null || text.length() > 128) return;
        if (pending) {
            if (!text.trim()
                .isEmpty()) {
                if (deferred == null) {
                    deferred = text;
                    deferredPage = origin;
                    notice = "Command queued after the pending reply.";
                } else notice = "Waiting for queued command; this action was not sent.";
            }
            return;
        }
        pending = true;
        pendingAt = lastPoll = ticks;
        pendingPage = origin;
        latestRequest = REQUESTS.incrementAndGet();
        if (origin == 3) history.record(text);
        SFMNetwork.WRAPPER.sendToServer(
            new PacketDebugTerminalRequest(
                manager.xCoord,
                manager.yCoord,
                manager.zCoord,
                getScreen().getContainer().windowId,
                latestRequest,
                text));
    }

    private void cycleRecording() {
        ManagerControlToken token = control.get();
        if (token == null) return;
        LogLevel next = recording == LogLevel.TRACE ? LogLevel.OFF : LogLevel.values()[recording.ordinal() + 1];
        SFMNetwork.WRAPPER.sendToServer(
            new PacketManagerSetLogLevel(manager.xCoord, manager.yCoord, manager.zCoord, next.name(), false, token));
        recording = next;
    }

    private void filterLogs() {
        filteredLogs = ManagerLogView.filter(logs, filter, query);
        if (page() == 4) {
            notice = logs.size() + " retained entries; UTC time. Search and severity only filter this view.";
            refreshOutput();
        }
    }

    private void refreshOutput() {
        List<String> lines = page() == 4 ? filteredLogs
            : page() == 5 ? profile : page() == 6 ? watch : printView ? print : terminalResult;
        output.lines(
            lines.isEmpty() ? Collections.singletonList("No output. Enable PRINT capture to record program messages.")
                : lines);
        output.wrap(page() != 4 || wrap);
    }

    private void copy() {
        List<String> lines = page() == 4 ? filteredLogs
            : page() == 5 ? profile : page() == 6 ? watch : printView ? print : terminalResult;
        GuiScreen.setClipboardString(String.join("\n", lines));
    }

    private ButtonWidget<?> button(Supplier<String> title, int x, int y, int width, Runnable action) {
        ButtonWidget<?> widget = new ButtonWidget<>().pos(x, y)
            .size(width, 16)
            .background(ManagerConsoleTheme.BUTTON)
            .hoverBackground(ManagerConsoleTheme.HOVER)
            .overlay(
                IKey.dynamic(title)
                    .scale(.8f))
            .onMousePressed(mouse -> {
                if (mouse != 0) return false;
                action.run();
                return true;
            });
        child(widget);
        return widget;
    }
}
