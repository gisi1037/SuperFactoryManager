package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.program.ProgramEditSnapshot;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Explicit result of one editor request; no client-only classes in the common handler. */
public final class PacketProgramEditReply implements IMessage {

    static final int VERSION = 0x53464d01;
    public int x, y, z, windowId;
    public long requestId;
    public boolean accepted;
    public ProgramEditSnapshot snapshot;
    public String message = "";

    public PacketProgramEditReply() {}

    public PacketProgramEditReply(int x, int y, int z, int windowId, long requestId, boolean accepted,
        ProgramEditSnapshot snapshot, String message) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.windowId = windowId;
        this.requestId = requestId;
        this.accepted = accepted;
        this.snapshot = snapshot;
        this.message = message == null ? "Program request rejected" : message;
        // Diagnostics have their own bound, independent of program text.
        if (this.message.length() > 240) {
            int end = 240;
            if (Character.isHighSurrogate(this.message.charAt(end - 1))
                && Character.isLowSurrogate(this.message.charAt(end))) end--;
            this.message = this.message.substring(0, end);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(VERSION);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(windowId);
        buf.writeLong(requestId);
        buf.writeBoolean(accepted);
        ProgramEditWire.string(buf, message, 2048);
        buf.writeBoolean(snapshot != null);
        if (snapshot != null) {
            ProgramEditWire.uuid(buf, snapshot.sessionId);
            buf.writeLong(snapshot.bindingGeneration);
            ProgramEditWire.uuid(buf, snapshot.documentId);
            buf.writeLong(snapshot.revision);
            ProgramEditWire.string(buf, snapshot.source, ProgramEditWire.MAX_SOURCE_BYTES);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        if (buf.readInt() != VERSION) throw new IllegalArgumentException("Unsupported program edit reply protocol");
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        windowId = buf.readInt();
        requestId = buf.readLong();
        if (requestId <= 0) throw new IllegalArgumentException("Invalid program reply request ID");
        accepted = buf.readBoolean();
        message = ProgramEditWire.string(buf, 2048);
        snapshot = null;
        if (buf.readBoolean()) snapshot = new ProgramEditSnapshot(
            ProgramEditWire.uuid(buf),
            buf.readLong(),
            ProgramEditWire.uuid(buf),
            buf.readLong(),
            ProgramEditWire.string(buf, ProgramEditWire.MAX_SOURCE_BYTES));
        if (buf.isReadable() || accepted != (snapshot != null))
            throw new IllegalArgumentException("Invalid editor reply");
    }

    public static final class Handler implements IMessageHandler<PacketProgramEditReply, IMessage> {

        @Override
        public IMessage onMessage(PacketProgramEditReply reply, MessageContext context) {
            SuperFactoryManager.proxy.receiveProgramEditReply(reply);
            return null;
        }
    }
}
