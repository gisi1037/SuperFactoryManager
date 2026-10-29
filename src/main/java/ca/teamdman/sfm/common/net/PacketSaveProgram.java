package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.common.program.ProgramEditSnapshot;
import ca.teamdman.sfm.common.util.ProgramRevision;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Mandatory server-issued edit snapshot; legacy unguarded saves are intentionally rejected. */
public final class PacketSaveProgram implements IMessage {

    // SFM + format version; unlike a small version number, cannot alias a valid legacy world x coordinate.
    static final int VERSION = 0x53464d01;
    int x, y, z;
    long requestId;
    boolean save;
    ProgramEditSnapshot expected;
    String code = "";

    public PacketSaveProgram() {}

    public static PacketSaveProgram open(int x, int y, int z, long requestId) {
        PacketSaveProgram packet = new PacketSaveProgram();
        packet.x = x;
        packet.y = y;
        packet.z = z;
        packet.requestId = requestId;
        return packet;
    }

    public static PacketSaveProgram save(int x, int y, int z, long requestId, ProgramEditSnapshot expected,
        String code) {
        if (expected == null) throw new IllegalArgumentException("Editor snapshot required");
        PacketSaveProgram packet = open(x, y, z, requestId);
        packet.save = true;
        packet.expected = expected;
        packet.code = code;
        return packet;
    }

    public boolean acceptsSource(String source) {
        return expectedDigest != null ? ProgramRevision.matches(expectedDigest, source)
            : expected != null && expected.source.equals(source);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(VERSION);
        buf.writeBoolean(save);
        buf.writeLong(requestId);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        if (save) {
            ProgramEditWire.uuid(buf, expected.sessionId);
            buf.writeLong(expected.bindingGeneration);
            ProgramEditWire.uuid(buf, expected.documentId);
            buf.writeLong(expected.revision);
            // Bounded source fingerprint supplements the monotonic document revision.
            buf.writeBytes(ProgramRevision.digest(expected.source));
            ProgramEditWire.string(buf, code, ProgramEditWire.MAX_SOURCE_BYTES);
        }
    }

    byte[] expectedDigest;

    @Override
    public void fromBytes(ByteBuf buf) {
        if (buf.readInt() != VERSION) throw new IllegalArgumentException("Unsupported program edit protocol");
        save = buf.readBoolean();
        requestId = buf.readLong();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        expected = null;
        expectedDigest = null;
        if (requestId <= 0) throw new IllegalArgumentException("Invalid program request ID");
        if (save) {
            expected = new ProgramEditSnapshot(
                ProgramEditWire.uuid(buf),
                buf.readLong(),
                ProgramEditWire.uuid(buf),
                buf.readLong(),
                "");
            expectedDigest = new byte[32];
            buf.readBytes(expectedDigest);
            code = ProgramEditWire.string(buf, ProgramEditWire.MAX_SOURCE_BYTES);
        }
        if (buf.isReadable()) throw new IllegalArgumentException("Trailing program edit data");
    }

    public static final class Handler implements IMessageHandler<PacketSaveProgram, IMessage> {

        @Override
        public IMessage onMessage(PacketSaveProgram packet, MessageContext context) {
            ProgramEditDispatcher.enqueue(context.getServerHandler().playerEntity, packet);
            return null;
        }
    }
}
