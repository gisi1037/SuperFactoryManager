package ca.teamdman.sfm.common.net;

import java.util.ArrayList;
import java.util.List;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.gui.ManagerControlToken;
import ca.teamdman.sfm.common.program.ManagerNetworkBrowser;
import ca.teamdman.sfm.common.program.ManagerNetworkBrowser.Page;
import ca.teamdman.sfm.common.program.ManagerNetworkBrowser.Row;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfm.common.util.BoundedUtf8;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Bounded metadata only: no stacks, provider handles, source, grants, disk NBT or executable values. */
public final class PacketManagerNetworkReply implements IMessage {

    private static final int VERSION = 0x53464e02;
    public static final int MAX_PACKET_BYTES = 16384;
    public int x, y, z;
    public ManagerControlToken token;
    public long requestId;
    public boolean accepted;
    public String message = "";
    public Page page;

    public PacketManagerNetworkReply() {}

    public PacketManagerNetworkReply(PacketManagerNetworkRequest request, boolean accepted, String message, Page page) {
        x = request.x;
        y = request.y;
        z = request.z;
        token = request.token;
        requestId = request.requestId;
        this.accepted = accepted;
        this.message = message;
        this.page = page;
        validate();
    }

    private void validate() {
        if (token == null || requestId <= 0
            || accepted != (page != null)
            || message == null
            || message.length() > ManagerNetworkBrowser.MAX_MESSAGE_CHARS)
            throw new IllegalArgumentException("Invalid network reply");
        BoundedUtf8.encode(message, ManagerNetworkBrowser.MAX_MESSAGE_BYTES);
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        validate();
        buffer.writeInt(VERSION);
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        token.write(buffer);
        buffer.writeLong(requestId);
        buffer.writeBoolean(accepted);
        ProgramEditWire.string(buffer, message, ManagerNetworkBrowser.MAX_MESSAGE_BYTES);
        if (page == null) return;
        buffer.writeLong(page.snapshotId);
        buffer.writeInt(page.cursor);
        buffer.writeInt(page.nextCursor);
        buffer.writeInt(page.totalCandidates);
        buffer.writeLong(page.capturedTick);
        buffer.writeLong(page.topologyTick);
        buffer.writeBoolean(page.topologyCurrent);
        buffer.writeBoolean(page.partial);
        ProgramEditWire.string(buffer, page.message, ManagerNetworkBrowser.MAX_MESSAGE_BYTES);
        buffer.writeByte(page.rows.size());
        for (Row row : page.rows) {
            buffer.writeInt(row.position.x);
            buffer.writeInt(row.position.y);
            buffer.writeInt(row.position.z);
            buffer.writeByte(row.kind);
            buffer.writeByte(row.state);
            ProgramEditWire.string(buffer, row.name, ManagerNetworkBrowser.MAX_NAME_BYTES);
            ProgramEditWire.string(buffer, row.labels, ManagerNetworkBrowser.MAX_LABEL_BYTES);
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        if (buffer.readableBytes() > MAX_PACKET_BYTES || buffer.readInt() != VERSION)
            throw new IllegalArgumentException("Unsupported or oversized network reply");
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        token = ManagerControlToken.read(buffer);
        requestId = buffer.readLong();
        accepted = flag(buffer);
        message = ProgramEditWire.string(buffer, ManagerNetworkBrowser.MAX_MESSAGE_BYTES);
        page = null;
        if (accepted) {
            long snapshot = buffer.readLong();
            int cursor = buffer.readInt(), next = buffer.readInt(), total = buffer.readInt();
            long captured = buffer.readLong(), topology = buffer.readLong();
            boolean current = flag(buffer), partial = flag(buffer);
            String notice = ProgramEditWire.string(buffer, ManagerNetworkBrowser.MAX_MESSAGE_BYTES);
            int count = buffer.readUnsignedByte();
            if (count > ManagerNetworkBrowser.PAGE_SIZE) throw new IllegalArgumentException("Oversized network page");
            List<Row> rows = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                BlockPos position = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
                int kind = buffer.readUnsignedByte(), state = buffer.readUnsignedByte();
                String name = ProgramEditWire.string(buffer, ManagerNetworkBrowser.MAX_NAME_BYTES);
                String labels = ProgramEditWire.string(buffer, ManagerNetworkBrowser.MAX_LABEL_BYTES);
                rows.add(new Row(position, name, labels, kind, state));
            }
            page = new Page(snapshot, cursor, next, total, captured, topology, current, partial, notice, rows);
        }
        if (buffer.isReadable()) throw new IllegalArgumentException("Trailing network reply data");
        validate();
    }

    private static boolean flag(ByteBuf buffer) {
        int value = buffer.readUnsignedByte();
        if (value > 1) throw new IllegalArgumentException("Invalid network reply flag");
        return value == 1;
    }

    public static final class Handler implements IMessageHandler<PacketManagerNetworkReply, IMessage> {

        @Override
        public IMessage onMessage(PacketManagerNetworkReply packet, MessageContext context) {
            SuperFactoryManager.proxy.receiveManagerNetworkReply(packet);
            return null;
        }
    }
}
