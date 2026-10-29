package ca.teamdman.sfm.common.net;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import ca.teamdman.sfm.common.program.ProgramDocument;
import ca.teamdman.sfm.common.util.BoundedUtf8;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;

/** Bounded wire primitives shared by editor snapshots and save requests. */
final class ProgramEditWire {

    static final int MAX_SOURCE_BYTES = ProgramDocument.MAX_SOURCE_BYTES;

    private ProgramEditWire() {}

    static UUID uuid(ByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }

    static void uuid(ByteBuf buf, UUID id) {
        buf.writeLong(id.getMostSignificantBits());
        buf.writeLong(id.getLeastSignificantBits());
    }

    static String string(ByteBuf buf, int maximum) {
        int length = ByteBufUtils.readVarInt(buf, 3);
        if (length < 0 || length > maximum || length > buf.readableBytes())
            throw new IllegalArgumentException("Invalid editor text length");
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        try {
            return StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString();
        } catch (CharacterCodingException error) {
            throw new IllegalArgumentException("Invalid editor UTF-8", error);
        }
    }

    static void string(ByteBuf buf, String text, int maximum) {
        ByteBuffer bytes = BoundedUtf8.encode(text, maximum);
        ByteBufUtils.writeVarInt(buf, bytes.remaining(), 3);
        buf.writeBytes(bytes);
    }
}
