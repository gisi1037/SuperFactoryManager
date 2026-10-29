package ca.teamdman.sfm.common.net;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.program.DebugTerminalService;
import ca.teamdman.sfm.common.program.DebugValueFormatter;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Independent bounded command results and latest PRINT history; no STATE envelope on the wire. */
public final class PacketDebugTerminalReply implements IMessage {

    public static final int MAX_WIRE_BYTES = 24000;
    public static final int MAX_HISTORY_LINES = 200;
    public static final int MAX_LINE_LENGTH = 512;
    private static final int MAX_LINE_BYTES = MAX_LINE_LENGTH * 3;
    public int x, y, z, windowId;
    public long requestId, dropped;
    public boolean capturing;
    public List<String> result = Collections.emptyList(), history = Collections.emptyList();

    public PacketDebugTerminalReply() {}

    public PacketDebugTerminalReply(int x, int y, int z, int windowId, long requestId, boolean capturing, long dropped,
        List<String> result, List<String> history) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.windowId = windowId;
        this.requestId = requestId;
        this.capturing = capturing;
        this.dropped = Math.max(0, dropped);
        int budget = MAX_WIRE_BYTES - 64;
        List<String> selected = new ArrayList<>();
        for (String line : result) {
            if (selected.size() >= DebugTerminalService.MAX_RESULT_LINES) break;
            String clipped = DebugValueFormatter.sanitize(line);
            int bytes = clipped.getBytes(StandardCharsets.UTF_8).length + 3;
            if (bytes > budget) break;
            budget -= bytes;
            selected.add(clipped);
        }
        this.result = Collections.unmodifiableList(selected);
        selected = new ArrayList<>();
        for (int i = history.size() - 1; i >= 0 && selected.size() < MAX_HISTORY_LINES; i--) {
            String clipped = DebugValueFormatter.sanitize(history.get(i));
            int bytes = clipped.getBytes(StandardCharsets.UTF_8).length + 3;
            if (bytes > budget) break;
            budget -= bytes;
            selected.add(clipped);
        }
        Collections.reverse(selected);
        this.history = Collections.unmodifiableList(selected);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        int start = buf.writerIndex();
        buf.writeInt(1);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(windowId);
        buf.writeLong(requestId);
        buf.writeBoolean(capturing);
        buf.writeLong(dropped);
        writeLines(buf, result);
        writeLines(buf, history);
        if (buf.writerIndex() - start > MAX_WIRE_BYTES) throw new IllegalArgumentException("Terminal reply too large");
    }

    private static void writeLines(ByteBuf buf, List<String> lines) {
        buf.writeInt(lines.size());
        for (String line : lines) ProgramEditWire.string(buf, line, MAX_LINE_BYTES);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        if (buf.readableBytes() > MAX_WIRE_BYTES || buf.readInt() != 1)
            throw new IllegalArgumentException("Invalid terminal reply frame");
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        windowId = buf.readInt();
        requestId = buf.readLong();
        capturing = buf.readBoolean();
        dropped = buf.readLong();
        if (windowId < 1 || windowId > 100 || requestId <= 0 || dropped < 0)
            throw new IllegalArgumentException("Invalid terminal reply identity");
        result = readLines(buf, DebugTerminalService.MAX_RESULT_LINES);
        history = readLines(buf, MAX_HISTORY_LINES);
        if (buf.isReadable()) throw new IllegalArgumentException("Trailing terminal reply bytes");
    }

    private static List<String> readLines(ByteBuf buf, int maximum) {
        int count = buf.readInt();
        if (count < 0 || count > maximum) throw new IllegalArgumentException("Invalid terminal line count");
        List<String> lines = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String line = ProgramEditWire.string(buf, MAX_LINE_BYTES);
            if (line.length() > MAX_LINE_LENGTH) throw new IllegalArgumentException("Terminal line too long");
            lines.add(line);
        }
        return Collections.unmodifiableList(lines);
    }

    public static final class Handler implements IMessageHandler<PacketDebugTerminalReply, IMessage> {

        @Override
        public IMessage onMessage(PacketDebugTerminalReply reply, MessageContext context) {
            SuperFactoryManager.proxy.receiveDebugTerminal(reply);
            return null;
        }
    }
}
