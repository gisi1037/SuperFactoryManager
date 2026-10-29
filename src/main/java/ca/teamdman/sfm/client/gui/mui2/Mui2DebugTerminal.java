package ca.teamdman.sfm.client.gui.mui2;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import ca.teamdman.sfm.client.gui.TerminalCommandHistory;
import ca.teamdman.sfm.common.net.PacketDebugTerminalReply;
import ca.teamdman.sfm.common.net.PacketDebugTerminalRequest;
import ca.teamdman.sfm.common.net.SFMNetwork;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

/** Bounded server diagnostics in a secondary MUI2 panel, retaining the live manager container. */
public final class Mui2DebugTerminal {

    private static final AtomicLong REQUESTS = new AtomicLong();
    private static final int PAGE_LINES = 8;
    private final TileEntityManager manager;
    private final int windowId;
    private final TerminalCommandHistory commands = new TerminalCommandHistory();
    private final ca.teamdman.sfm.client.gui.TerminalCommandCompletion completion = new ca.teamdman.sfm.client.gui.TerminalCommandCompletion();
    private List<String> history = Collections.emptyList();
    private List<String> result = Collections.singletonList("Type help for commands. PRINT capture starts OFF.");
    private ModularPanel panel;
    private String command = "", notice = "Connecting to manager";
    private int ticks, lastPoll = -20, pendingAt, page, column;
    private long latestRequest;
    private boolean pending, pendingPoll, capturing, showResult = true, cancelled;
    private String deferredCommand;
    private long dropped;

    public Mui2DebugTerminal(TileEntityManager manager, int windowId) {
        this.manager = manager;
        this.windowId = windowId;
    }

    public ModularPanel build(Runnable closeAction) {
        panel = new ModularPanel("sfm_debug_terminal") {

            @Override
            public boolean disablePanelsBelow() {
                return true;
            }
        }.size(312, 228)
            .onCloseAction(this::cancel);
        panel.child(
            new TextWidget<>("Manager debug terminal").pos(8, 7)
                .size(190, 12));
        panel.child(
            new TextWidget<>(IKey.dynamic(() -> trim(notice, 190))).pos(8, 24)
                .size(190, 12));
        panel.child(
            button("", 204, 18, 100, () -> request("capture " + (capturing ? "off" : "on")))
                .overlay(IKey.dynamic(() -> "Capture: " + (capturing ? "ON" : "OFF"))));
        panel.child(
            button("", 8, 40, 64, () -> view(false)).overlay(IKey.dynamic(() -> (showResult ? "" : "> ") + "PRINT")));
        panel.child(
            button("", 76, 40, 64, () -> view(true)).overlay(IKey.dynamic(() -> (showResult ? "> " : "") + "Result")));
        panel.child(button("Clear", 144, 40, 52, () -> request("clear")));
        panel.child(button("Help", 200, 40, 48, () -> request("help")));
        panel.child(button("Copy", 252, 40, 52, this::copy));
        ListWidget<IWidget, ?> list = new ListWidget<>().pos(8, 64)
            .size(296, 80);
        for (int i = 0; i < PAGE_LINES; i++) {
            final int row = i;
            list.child(
                new TextWidget<>(IKey.dynamic(() -> displayLine(row))).size(292, 10)
                    .addTooltipLine(IKey.dynamic(() -> fullLine(row))));
        }
        panel.child(list);
        panel.child(button("Previous", 8, 147, 60, () -> page = Math.max(0, page - 1)));
        panel.child(
            new TextWidget<>(IKey.dynamic(() -> (page + 1) + " / " + pages())).pos(72, 151)
                .size(82, 12));
        panel.child(button("Next", 158, 147, 58, () -> page = Math.min(pages() - 1, page + 1)));
        panel.child(button("<", 222, 147, 38, () -> column = Math.max(0, column - 20)));
        panel.child(button(">", 266, 147, 38, () -> column = Math.min(511, column + 20)));
        TextFieldWidget field = new TextFieldWidget() {

            @Override
            public Result onKeyPressed(char character, int key) {
                if (key == Keyboard.KEY_TAB) {
                    command = completion.complete(getText(), GuiScreen.isShiftKeyDown());
                    setText(command);
                    return Result.SUCCESS;
                }
                if (key == Keyboard.KEY_UP || key == Keyboard.KEY_DOWN) {
                    completion.reset();
                    command = key == Keyboard.KEY_UP ? commands.previous(getText()) : commands.next(getText());
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
            .setFocusOnGuiOpen(true)
            .autoUpdateOnChange(true)
            .value(new StringValue.Dynamic(() -> command, text -> command = text));
        panel.child(
            field.pos(8, 168)
                .size(238, 18)
                .addTooltipLine(
                    IKey.str(
                        "Tab/Shift+Tab: complete commands. Up/Down: recall; Down past newest restores your draft.")));
        panel.child(button("Run", 250, 167, 54, () -> request(command)));
        // Leave the minimum screen's bottom overlay strip free for stock pack controls.
        panel.child(button("Status", 8, 188, 56, () -> request("status")));
        panel.child(button("State", 68, 188, 50, () -> request("state")));
        panel.child(
            new TextWidget<>(IKey.dynamic(() -> "Dropped: " + dropped)).pos(124, 192)
                .size(112, 12));
        panel.child(button("Done", 242, 188, 62, closeAction));
        panel.onUpdateListener(widget -> {
            if (cancelled || !panel.isOpen()) return;
            ticks++;
            if (pending && ticks - pendingAt >= 60) {
                pending = false;
                notice = "No reply; check connection";
                sendDeferred();
            }
            if (!pending && ticks - lastPoll >= 20) {
                lastPoll = ticks;
                request("");
            }
        });
        return panel;
    }

    public void cancel() {
        cancelled = true;
        pending = false;
        deferredCommand = null;
    }

    /** The proxy delivers on the client thread; a reopened panel has a distinct globally increasing request ID. */
    public void receive(PacketDebugTerminalReply reply) {
        if (cancelled || panel == null
            || !panel.isOpen()
            || reply.x != manager.xCoord
            || reply.y != manager.yCoord
            || reply.z != manager.zCoord
            || reply.windowId != windowId
            || reply.requestId != latestRequest) return;
        if (Minecraft.getMinecraft().thePlayer == null
            || Minecraft.getMinecraft().thePlayer.openContainer.windowId != windowId) return;
        pending = false;
        boolean follow = !showResult && page == pages() - 1;
        capturing = reply.capturing;
        dropped = reply.dropped;
        history = reply.history;
        if (!reply.result.isEmpty()) {
            result = reply.result;
            showResult = true;
            page = column = 0;
        } else if (follow) page = pages() - 1;
        page = Math.min(page, pages() - 1);
        notice = history.isEmpty() ? "No PRINT output recorded" : history.size() + " PRINT lines retained";
        sendDeferred();
    }

    private void sendDeferred() {
        if (deferredCommand == null) return;
        String text = deferredCommand;
        deferredCommand = null;
        request(text);
    }

    private void request(String text) {
        if (cancelled || panel == null || !panel.isOpen()) return;
        if (text == null || text.length() > 128) {
            notice = "Maximum 128 command characters";
            return;
        }
        if (pending) {
            // A read-only background poll must not swallow Enter. Keep one command, never overwrite it.
            if (pendingPoll && deferredCommand == null
                && !text.trim()
                    .isEmpty()) {
                deferredCommand = text;
                notice = "Command queued after status refresh";
            } else notice = "Waiting for server reply";
            return;
        }
        latestRequest = REQUESTS.incrementAndGet();
        pending = true;
        pendingPoll = text.trim()
            .isEmpty();
        pendingAt = ticks;
        lastPoll = ticks;
        SFMNetwork.WRAPPER.sendToServer(
            new PacketDebugTerminalRequest(
                manager.xCoord,
                manager.yCoord,
                manager.zCoord,
                windowId,
                latestRequest,
                text));
        commands.record(text);
        if (!text.trim()
            .isEmpty()) completion.reset();
    }

    private void view(boolean commands) {
        showResult = commands;
        column = 0;
        page = commands ? 0 : pages() - 1;
    }

    private List<String> lines() {
        return showResult ? result : history;
    }

    private int pages() {
        return Math.max(1, (lines().size() + PAGE_LINES - 1) / PAGE_LINES);
    }

    private String fullLine(int row) {
        List<String> lines = lines();
        int index = page * PAGE_LINES + row;
        if (index >= lines.size())
            return index == 0 && !showResult ? "No PRINT output. Enable capture, then run a trigger." : "";
        return lines.get(index);
    }

    private String displayLine(int row) {
        String line = fullLine(row);
        return trim(line.substring(Math.min(column, line.length())), 284);
    }

    private static String trim(String text, int width) {
        return Minecraft.getMinecraft().fontRenderer.trimStringToWidth(text, width);
    }

    private void copy() {
        StringBuilder text = new StringBuilder(
            "-- SFM debug terminal: " + (showResult ? "command result" : "PRINT history") + " --\n");
        for (String line : lines()) text.append(line)
            .append('\n');
        GuiScreen.setClipboardString(text.toString());
        notice = "Copied " + lines().size() + " lines";
    }

    private static ButtonWidget<?> button(String label, int x, int y, int width, Runnable action) {
        return new ButtonWidget<>().pos(x, y)
            .size(width, 20)
            .overlay(IKey.str(label))
            .onMousePressed(mouse -> {
                if (mouse != 0) return false;
                action.run();
                return true;
            });
    }
}
