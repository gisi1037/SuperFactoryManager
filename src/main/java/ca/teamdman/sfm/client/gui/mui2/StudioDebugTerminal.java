package ca.teamdman.sfm.client.gui.mui2;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
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

import ca.teamdman.sfm.client.gui.TerminalCommandCompletion;
import ca.teamdman.sfm.client.gui.TerminalCommandHistory;
import ca.teamdman.sfm.common.net.PacketDebugTerminalReply;
import ca.teamdman.sfm.common.net.PacketStationDebugReply;
import ca.teamdman.sfm.common.net.PacketStationDebugRequest;
import ca.teamdman.sfm.common.net.PacketStationDebugRequest.Action;
import ca.teamdman.sfm.common.net.SFMNetwork;
import ca.teamdman.sfm.common.tileentity.TileEntityProgrammingStation;

/** Explicit nearby-manager diagnostics. Opening the IDE neither connects nor enables PRINT capture. */
public final class StudioDebugTerminal {

    private static final AtomicLong REQUESTS = new AtomicLong();
    private static final int PAGE_LINES = 6;
    private final TileEntityProgrammingStation station;
    private final UUID session;
    private final int window;
    private final TerminalCommandHistory commands = new TerminalCommandHistory();
    private final TerminalCommandCompletion completion = new TerminalCommandCompletion();
    private ModularPanel panel;
    private String inputX, inputY, inputZ, command = "", notice = "Choose a nearby manager, then Connect.";
    private int targetX, targetY, targetZ, ticks, pendingAt, lastPoll, page, column;
    private long dropped;
    private boolean pending, connected, capturing, cancelled, showResult = true;
    private Action pendingAction;
    private PacketStationDebugRequest pendingPacket;
    private boolean pendingPoll;
    private String deferred;
    private List<String> history = Collections.emptyList();
    private List<String> result = Collections
        .singletonList("Type help after connecting. Capture starts OFF on a fresh manager.");

    public StudioDebugTerminal(TileEntityProgrammingStation station, UUID session, int window) {
        this.station = station;
        this.session = session;
        this.window = window;
        inputX = Integer.toString(station.xCoord);
        inputY = Integer.toString(station.yCoord);
        inputZ = Integer.toString(station.zCoord);
    }

    public ModularPanel build(Runnable close) {
        panel = new ModularPanel("sfm_studio_terminal") {

            @Override
            public boolean disablePanelsBelow() {
                return true;
            }
        }.size(312, 228)
            .background(StudioTheme.SHELL)
            .onCloseAction(this::cancel);
        panel.child(
            new TextWidget<>("Manager terminal: X / Y / Z").color(0xffefa09a)
                .pos(8, 7)
                .size(296, 12));
        panel.child(coordinate("X", 8, () -> inputX, value -> inputX = value));
        panel.child(coordinate("Y", 76, () -> inputY, value -> inputY = value));
        panel.child(coordinate("Z", 144, () -> inputZ, value -> inputZ = value));
        panel.child(button("Connect", 214, 22, 90, this::connect));
        panel.child(
            new TextWidget<>(IKey.dynamic(() -> trim(notice, 190))).color(0xffb0b3bb)
                .pos(8, 47)
                .size(190, 12)
                .addTooltipLine(IKey.dynamic(() -> notice)));
        panel.child(
            button("", 204, 42, 100, () -> request("capture " + (capturing ? "off" : "on")))
                .overlay(IKey.dynamic(() -> "Capture: " + (capturing ? "ON" : "OFF"))));
        panel.child(
            button("", 8, 64, 64, () -> view(false)).overlay(IKey.dynamic(() -> (showResult ? "" : "> ") + "PRINT")));
        panel.child(
            button("", 76, 64, 64, () -> view(true)).overlay(IKey.dynamic(() -> (showResult ? "> " : "") + "Result")));
        panel.child(button("Clear", 144, 64, 52, () -> request("clear")));
        panel.child(button("Help", 200, 64, 48, () -> request("help")));
        panel.child(button("Copy", 252, 64, 52, this::copy));
        ListWidget<IWidget, ?> list = new ListWidget<>().pos(8, 88)
            .size(296, 62);
        for (int i = 0; i < PAGE_LINES; i++) {
            final int row = i;
            list.child(
                new TextWidget<>(IKey.dynamic(() -> displayLine(row))).color(0xffe7e8eb)
                    .size(292, 10)
                    .addTooltipLine(IKey.dynamic(() -> fullLine(row))));
        }
        panel.child(list);
        panel.child(button("Previous", 8, 153, 60, () -> page = Math.max(0, page - 1)));
        panel.child(
            new TextWidget<>(IKey.dynamic(() -> (page + 1) + " / " + pages())).color(0xffb0b3bb)
                .pos(72, 157)
                .size(82, 12));
        panel.child(button("Next", 158, 153, 58, () -> page = Math.min(pages() - 1, page + 1)));
        panel.child(button("<", 222, 153, 38, () -> column = Math.max(0, column - 20)));
        panel.child(button(">", 266, 153, 38, () -> column = Math.min(511, column + 20)));
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
            .autoUpdateOnChange(true)
            .value(new StringValue.Dynamic(() -> command, value -> command = value));
        panel.child(
            field.pos(8, 177)
                .size(238, 18)
                .addTooltipLine(IKey.str("Tab: commands. Up/Down: history. Enter: run.")));
        panel.child(button("Run", 250, 176, 54, () -> request(command)));
        panel.child(button("Status", 8, 200, 56, () -> request("status")));
        panel.child(button("State", 68, 200, 50, () -> request("state")));
        panel.child(
            new TextWidget<>(IKey.dynamic(() -> "Dropped: " + dropped)).color(0xffb0b3bb)
                .pos(124, 204)
                .size(112, 12));
        panel.child(button("Done", 242, 200, 62, close));
        panel.onUpdateListener(widget -> tick());
        return panel;
    }

    private TextFieldWidget coordinate(String label, int x, java.util.function.Supplier<String> get,
        java.util.function.Consumer<String> set) {
        return new TextFieldWidget().setMaxLength(11)
            .autoUpdateOnChange(true)
            .value(new StringValue.Dynamic(get, set))
            .pos(x, 23)
            .size(62, 18)
            .addTooltipLine(
                IKey.str("Manager " + label + " coordinate; same dimension, within 8 blocks of the player."));
    }

    private void connect() {
        if (pending) {
            notice = "Waiting for server reply";
            return;
        }
        try {
            targetX = Integer.parseInt(inputX.trim());
            targetY = Integer.parseInt(inputY.trim());
            targetZ = Integer.parseInt(inputZ.trim());
        } catch (NumberFormatException invalid) {
            notice = "Use integer X, Y and Z coordinates";
            return;
        }
        connected = false;
        deferred = null;
        send(Action.CONNECT, "");
    }

    private void tick() {
        if (cancelled || panel == null || !panel.isOpen()) return;
        ticks++;
        if (pending && ticks - pendingAt >= 60) {
            pending = connected = false;
            deferred = null;
            notice = "No reply. Connect again to retry.";
        }
        if (connected && !pending && ticks - lastPoll >= 20) send(Action.COMMAND, "");
    }

    private void request(String text) {
        if (!connected) {
            notice = "Connect to a nearby manager first";
            return;
        }
        if (text == null || text.length() > 128) {
            notice = "Maximum 128 command characters";
            return;
        }
        if (pending) {
            if (pendingPoll && deferred == null
                && !text.trim()
                    .isEmpty()) {
                deferred = text;
                notice = "Command queued after refresh";
            } else notice = "Waiting for server reply";
            return;
        }
        commands.record(text);
        completion.reset();
        send(Action.COMMAND, text);
    }

    private PacketStationDebugRequest packet(Action action, String text) {
        PacketStationDebugRequest packet = new PacketStationDebugRequest();
        packet.x = station.xCoord;
        packet.y = station.yCoord;
        packet.z = station.zCoord;
        packet.windowId = window;
        packet.sessionId = session;
        packet.requestId = REQUESTS.incrementAndGet();
        packet.targetX = targetX;
        packet.targetY = targetY;
        packet.targetZ = targetZ;
        packet.action = action;
        packet.command = text;
        return packet;
    }

    private void send(Action action, String text) {
        if (cancelled || panel == null || !panel.isOpen()) return;
        PacketStationDebugRequest packet = packet(action, text);
        try {
            packet.validate();
        } catch (IllegalArgumentException invalid) {
            notice = invalid.getMessage();
            return;
        }
        pendingAction = action;
        pendingPoll = action == Action.COMMAND && text.trim()
            .isEmpty();
        pendingPacket = packet;
        pending = true;
        pendingAt = lastPoll = ticks;
        SFMNetwork.WRAPPER.sendToServer(packet);
    }

    public void receive(PacketStationDebugReply reply) {
        if (cancelled || panel == null || !panel.isOpen() || !pending || !reply.matches(pendingPacket)) return;
        if (Minecraft.getMinecraft().thePlayer == null
            || Minecraft.getMinecraft().thePlayer.openContainer.windowId != window) return;
        pending = false;
        notice = reply.message;
        if (!reply.accepted || reply.payload == null) {
            connected = false;
            deferred = null;
            return;
        }
        connected = true;
        PacketDebugTerminalReply data = reply.payload;
        capturing = data.capturing;
        dropped = data.dropped;
        boolean follow = !showResult && page == pages() - 1;
        history = data.history;
        if (!data.result.isEmpty()) {
            result = data.result;
            showResult = true;
            page = column = 0;
        } else if (follow) page = pages() - 1;
        page = Math.min(page, pages() - 1);
        if (pendingAction == Action.CONNECT) notice = "Connected to " + targetX + ", " + targetY + ", " + targetZ;
        if (deferred != null) {
            String next = deferred;
            deferred = null;
            request(next);
        }
    }

    public void cancel() {
        if (cancelled) return;
        if (connected || pending) SFMNetwork.WRAPPER.sendToServer(packet(Action.DISCONNECT, ""));
        cancelled = true;
        pending = connected = false;
        deferred = null;
    }

    private void view(boolean results) {
        showResult = results;
        column = 0;
        page = results ? 0 : pages() - 1;
    }

    private List<String> lines() {
        return showResult ? result : history;
    }

    private int pages() {
        return Math.max(1, (lines().size() + PAGE_LINES - 1) / PAGE_LINES);
    }

    private String fullLine(int row) {
        int index = page * PAGE_LINES + row;
        return index < lines().size() ? lines().get(index)
            : index == 0 && !showResult ? "No PRINT output. Enable capture, then run a trigger." : "";
    }

    private String displayLine(int row) {
        String line = fullLine(row);
        return trim(line.substring(Math.min(column, line.length())), 284);
    }

    private static String trim(String text, int width) {
        return Minecraft.getMinecraft().fontRenderer.trimStringToWidth(text, width);
    }

    private void copy() {
        StringBuilder text = new StringBuilder("-- SFM station terminal --\n");
        for (String line : lines()) text.append(line)
            .append('\n');
        GuiScreen.setClipboardString(text.toString());
        notice = "Copied " + lines().size() + " lines";
    }

    private static ButtonWidget<?> button(String label, int x, int y, int width, Runnable action) {
        return new ButtonWidget<>().pos(x, y)
            .size(width, 20)
            .background(StudioTheme.BUTTON)
            .hoverBackground(StudioTheme.HOVER)
            .overlay(
                IKey.str(label)
                    .color(0xffe7e8eb))
            .onMousePressed(mouse -> {
                if (mouse != 0) return false;
                action.run();
                return true;
            });
    }
}
