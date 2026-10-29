package ca.teamdman.sfm.common.net;

import java.util.UUID;

import ca.teamdman.sfm.SuperFactoryManager;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Exact station session and explicit target accompany every bounded terminal result. */
public final class PacketStationDebugReply implements IMessage {

    private static final int VERSION = 0x53544402;
    public static final int MAX_WIRE_BYTES = 25000;
    public int x, y, z, windowId, targetX, targetY, targetZ;
    public UUID sessionId;
    public long requestId;
    public boolean accepted;
    public String message = "";
    public PacketDebugTerminalReply payload;

    public PacketStationDebugReply() {}

    public PacketStationDebugReply(PacketStationDebugRequest request, boolean accepted, String message,
        PacketDebugTerminalReply payload) {
        x = request.x;
        y = request.y;
        z = request.z;
        windowId = request.windowId;
        targetX = request.targetX;
        targetY = request.targetY;
        targetZ = request.targetZ;
        sessionId = request.sessionId;
        requestId = request.requestId;
        this.accepted = accepted;
        this.message = message;
        this.payload = payload;
    }

    private void validate() {
        if (sessionId == null || requestId <= 0
            || windowId < 1
            || windowId > 255
            || message == null
            || message.length() > 256
            || !accepted && payload != null)
            throw new IllegalArgumentException("Invalid station terminal reply identity");
    }

    public boolean matches(PacketStationDebugRequest request) {
        return request != null && x == request.x
            && y == request.y
            && z == request.z
            && windowId == request.windowId
            && sessionId != null
            && sessionId.equals(request.sessionId)
            && requestId == request.requestId
            && targetX == request.targetX
            && targetY == request.targetY
            && targetZ == request.targetZ;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        validate();
        int start = buffer.writerIndex();
        buffer.writeInt(VERSION);
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeInt(windowId);
        ProgramEditWire.uuid(buffer, sessionId);
        buffer.writeLong(requestId);
        buffer.writeInt(targetX);
        buffer.writeInt(targetY);
        buffer.writeInt(targetZ);
        buffer.writeBoolean(accepted);
        ProgramEditWire.string(buffer, message, 768);
        buffer.writeBoolean(payload != null);
        if (payload != null) payload.toBytes(buffer);
        if (buffer.writerIndex() - start > MAX_WIRE_BYTES)
            throw new IllegalArgumentException("Station terminal reply too large");
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        if (buffer.readableBytes() > MAX_WIRE_BYTES || buffer.readInt() != VERSION)
            throw new IllegalArgumentException("Invalid station terminal reply frame");
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        windowId = buffer.readInt();
        sessionId = ProgramEditWire.uuid(buffer);
        requestId = buffer.readLong();
        targetX = buffer.readInt();
        targetY = buffer.readInt();
        targetZ = buffer.readInt();
        accepted = buffer.readBoolean();
        message = ProgramEditWire.string(buffer, 768);
        payload = null;
        if (buffer.readBoolean()) {
            payload = new PacketDebugTerminalReply();
            payload.fromBytes(buffer);
        }
        if (buffer.isReadable()) throw new IllegalArgumentException("Trailing station terminal reply bytes");
        validate();
    }

    public static final class Handler implements IMessageHandler<PacketStationDebugReply, IMessage> {

        @Override
        public IMessage onMessage(PacketStationDebugReply reply, MessageContext context) {
            SuperFactoryManager.proxy.receiveStationDebugReply(reply);
            return null;
        }
    }
}
