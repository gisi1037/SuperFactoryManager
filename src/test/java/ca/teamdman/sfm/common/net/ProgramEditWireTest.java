package ca.teamdman.sfm.common.net;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.program.ProgramEditSnapshot;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

class ProgramEditWireTest {

    private static final ProgramEditSnapshot SNAPSHOT = new ProgramEditSnapshot(
        UUID.fromString("d3370edf-a08e-4b68-a793-73376acdf384"),
        18,
        UUID.fromString("1177b954-df21-430d-a6d8-6b3475b3d621"),
        42,
        "NAME \"箱😀\"\nEVERY 20 TICKS DO END");

    @Test
    void openRequestPreservesItsTargetAndCorrelationIdAndRejectsInvalidIds() {
        PacketSaveProgram decoded = request(encode(PacketSaveProgram.open(-17, 128, 901, Long.MAX_VALUE)));
        assertFalse(decoded.save);
        assertEquals(-17, decoded.x);
        assertEquals(128, decoded.y);
        assertEquals(901, decoded.z);
        assertEquals(Long.MAX_VALUE, decoded.requestId);
        assertNull(decoded.expected);
        assertNull(decoded.expectedDigest);
        for (long invalid : new long[] { 0, -1, Long.MIN_VALUE })
            assertThrows(RuntimeException.class, () -> request(encode(PacketSaveProgram.open(1, 2, 3, invalid))));
        for (long invalid : new long[] { 0, -1, Long.MIN_VALUE }) assertThrows(
            IllegalArgumentException.class,
            () -> reply(encode(new PacketProgramEditReply(1, 2, 3, 4, invalid, false, null, "Rejected"))));
    }

    @Test
    void saveRequestPreservesUnicodeDraftAndAllSnapshotGuardsIncludingExactSourceFingerprint() {
        String candidate = "-- 😀 draft\nNAME \"箱\"\nEVERY 20 TICKS DO FORGET END";
        PacketSaveProgram decoded = request(encode(PacketSaveProgram.save(5, 70, -8, 1037, SNAPSHOT, candidate)));
        assertTrue(decoded.save);
        assertEquals(1037, decoded.requestId);
        assertEquals(candidate, decoded.code);
        assertEquals(SNAPSHOT.sessionId, decoded.expected.sessionId);
        assertEquals(SNAPSHOT.documentId, decoded.expected.documentId);
        assertEquals(SNAPSHOT.bindingGeneration, decoded.expected.bindingGeneration);
        assertEquals(SNAPSHOT.revision, decoded.expected.revision);
        assertTrue(decoded.acceptsSource(SNAPSHOT.source));
        assertFalse(decoded.acceptsSource(SNAPSHOT.source + " "));
        assertFalse(decoded.acceptsSource(SNAPSHOT.source.replace("箱", "箱子")));
        assertFalse(decoded.acceptsSource(""));
    }

    @Test
    void sourceBoundsCountUtf8BytesAndRejectOversizedOrImpossibleDeclaredLengths() {
        for (String boundary : new String[] { repeat("a", ProgramEditWire.MAX_SOURCE_BYTES),
            repeat("箱", ProgramEditWire.MAX_SOURCE_BYTES / 3) }) {
            PacketSaveProgram decoded = request(encode(PacketSaveProgram.save(0, 64, 0, 1, SNAPSHOT, boundary)));
            assertEquals(boundary, decoded.code);
            assertThrows(
                IllegalArgumentException.class,
                () -> encode(PacketSaveProgram.save(0, 64, 0, 1, SNAPSHOT, boundary + "a")));
        }
        for (int declared : new int[] { 1, ProgramEditWire.MAX_SOURCE_BYTES, ProgramEditWire.MAX_SOURCE_BYTES + 1,
            2097151 }) {
            ByteBuf bytes = Unpooled.buffer();
            try {
                ByteBufUtils.writeVarInt(bytes, declared, 3);
                assertThrows(
                    RuntimeException.class,
                    () -> ProgramEditWire.string(bytes, ProgramEditWire.MAX_SOURCE_BYTES));
            } finally {
                bytes.release();
            }
        }
    }

    @Test
    void completeMaximumFramesFitTheVanillaCustomPayloadLimit() {
        String source = repeat("a", ProgramEditWire.MAX_SOURCE_BYTES);
        ProgramEditSnapshot boundary = new ProgramEditSnapshot(SNAPSHOT.sessionId, 18, SNAPSHOT.documentId, 42, source);
        for (IMessage frame : new IMessage[] { PacketSaveProgram.save(1, 2, 3, 7, SNAPSHOT, source),
            new PacketProgramEditReply(1, 2, 3, 4, 7, true, boundary, repeat("箱", 240)) }) {
            ByteBuf bytes = Unpooled.buffer();
            ByteBuf exact = null;
            try {
                bytes.writeByte(0); // SimpleImpl's message discriminator precedes our envelope.
                frame.toBytes(bytes);
                exact = bytes.copy();
                assertTrue(exact.readableBytes() < 32767);
                assertNotNull(new net.minecraft.network.play.client.C17PacketCustomPayload("sfm", exact.array()));
            } finally {
                bytes.release();
                if (exact != null) exact.release();
            }
        }
    }

    @Test
    void malformedUtf8InASaveDraftIsRejectedInsteadOfReplacingCharacters() {
        byte[] bytes = encode(PacketSaveProgram.save(0, 64, 0, 1, SNAPSHOT, "ab"));
        bytes[bytes.length - 2] = (byte) 0xc3;
        bytes[bytes.length - 1] = 0x28;
        assertThrows(IllegalArgumentException.class, () -> request(bytes));
    }

    @Test
    void unknownProtocolAndLegacyUnguardedRequestAreRejected() {
        byte[] unknown = encode(PacketSaveProgram.open(1, 2, 3, 7));
        ByteBuf version = Unpooled.wrappedBuffer(unknown);
        try {
            version.setInt(0, 999);
            assertThrows(IllegalArgumentException.class, () -> new PacketSaveProgram().fromBytes(version));
        } finally {
            version.release();
        }
        ByteBuf replyVersion = Unpooled
            .wrappedBuffer(encode(new PacketProgramEditReply(1, 2, 3, 4, 7, false, null, "Rejected")));
        try {
            replyVersion.setInt(0, 999);
            assertThrows(IllegalArgumentException.class, () -> new PacketProgramEditReply().fromBytes(replyVersion));
        } finally {
            replyVersion.release();
        }
        for (int legacyX : new int[] { 0, 1 }) {
            ByteBuf legacy = Unpooled.buffer();
            try {
                legacy.writeInt(legacyX);
                legacy.writeInt(64);
                legacy.writeInt(0);
                ByteBufUtils.writeUTF8String(legacy, "ABCDEFGHIJ");
                legacy.writeBoolean(true);
                legacy.writeBoolean(false);
                // x=1 and this 25-byte legacy payload previously aliased a complete version-1 open request.
                assertThrows(IllegalArgumentException.class, () -> new PacketSaveProgram().fromBytes(legacy));
            } finally {
                legacy.release();
            }
        }
    }

    @Test
    void everyTruncatedRequestOrAcceptedReplyIsRejected() {
        byte[] save = encode(PacketSaveProgram.save(1, 2, 3, 19, SNAPSHOT, "Unicode 箱😀"));
        byte[] open = encode(PacketSaveProgram.open(1, 2, 3, 20));
        byte[] accepted = encode(new PacketProgramEditReply(1, 2, 3, 7, 19, true, SNAPSHOT, "Saved 箱😀"));
        for (byte[] complete : new byte[][] { save, open }) {
            for (int length = 0; length < complete.length; length++) {
                byte[] truncated = Arrays.copyOf(complete, length);
                assertThrows(RuntimeException.class, () -> request(truncated), "Request prefix length " + length);
            }
        }
        for (int length = 0; length < accepted.length; length++) {
            byte[] truncated = Arrays.copyOf(accepted, length);
            assertThrows(RuntimeException.class, () -> reply(truncated), "Reply prefix length " + length);
        }
    }

    @Test
    void trailingBytesCannotBeIgnoredOnEitherMessage() {
        byte[] save = encode(PacketSaveProgram.save(1, 2, 3, 27, SNAPSHOT, "draft"));
        byte[] open = encode(PacketSaveProgram.open(1, 2, 3, 28));
        byte[] response = encode(new PacketProgramEditReply(1, 2, 3, 4, 27, false, null, "Conflict"));
        for (byte[] bytes : new byte[][] { save, open })
            assertThrows(IllegalArgumentException.class, () -> request(Arrays.copyOf(bytes, bytes.length + 1)));
        assertThrows(IllegalArgumentException.class, () -> reply(Arrays.copyOf(response, response.length + 1)));
    }

    @Test
    void acceptedAndRejectedRepliesPreserveRequestIdentityAndSourceSnapshot() {
        PacketProgramEditReply accepted = reply(
            encode(new PacketProgramEditReply(-9, 64, 12, 17, 813, true, SNAPSHOT, "Saved 箱😀")));
        assertTrue(accepted.accepted);
        assertEquals(-9, accepted.x);
        assertEquals(64, accepted.y);
        assertEquals(12, accepted.z);
        assertEquals(17, accepted.windowId);
        assertEquals(813, accepted.requestId);
        assertEquals("Saved 箱😀", accepted.message);
        assertEquals(SNAPSHOT.sessionId, accepted.snapshot.sessionId);
        assertEquals(SNAPSHOT.documentId, accepted.snapshot.documentId);
        assertEquals(SNAPSHOT.bindingGeneration, accepted.snapshot.bindingGeneration);
        assertEquals(SNAPSHOT.revision, accepted.snapshot.revision);
        assertEquals(SNAPSHOT.source, accepted.snapshot.source);
        PacketProgramEditReply rejected = reply(
            encode(new PacketProgramEditReply(-9, 64, 12, 17, 814, false, null, "Stale draft; keep your edits")));
        assertFalse(rejected.accepted);
        assertNull(rejected.snapshot);
        assertEquals(814, rejected.requestId);
        assertEquals("Stale draft; keep your edits", rejected.message);
        PacketProgramEditReply bounded = reply(
            encode(
                new PacketProgramEditReply(1, 2, 3, 4, 815, false, null, repeat("a", 239) + "😀 trailing diagnostic")));
        assertEquals(repeat("a", 239), bounded.message);
    }

    @Test
    void replyCannotClaimAcceptanceWithoutSnapshotOrReturnSnapshotOnRejection() {
        for (PacketProgramEditReply inconsistent : new PacketProgramEditReply[] {
            new PacketProgramEditReply(1, 2, 3, 4, 29, true, null, "Saved"),
            new PacketProgramEditReply(1, 2, 3, 4, 30, false, SNAPSHOT, "Rejected") }) {
            assertThrows(IllegalArgumentException.class, () -> reply(encode(inconsistent)));
        }
    }

    @Test
    void unpairedSurrogatesCannotSilentlyRewriteSourceDuringEncoding() {
        for (String invalid : new String[] { "NAME \"\ud800\"", "NAME \"\udc00\"" }) {
            assertThrows(
                IllegalArgumentException.class,
                () -> encode(PacketSaveProgram.save(0, 64, 0, 31, SNAPSHOT, invalid)));
        }
    }

    private static byte[] encode(IMessage message) {
        ByteBuf bytes = Unpooled.buffer();
        try {
            message.toBytes(bytes);
            byte[] copy = new byte[bytes.readableBytes()];
            bytes.readBytes(copy);
            return copy;
        } finally {
            bytes.release();
        }
    }

    private static PacketSaveProgram request(byte[] encoded) {
        ByteBuf bytes = Unpooled.wrappedBuffer(encoded);
        try {
            PacketSaveProgram packet = new PacketSaveProgram();
            packet.fromBytes(bytes);
            return packet;
        } finally {
            bytes.release();
        }
    }

    private static PacketProgramEditReply reply(byte[] encoded) {
        ByteBuf bytes = Unpooled.wrappedBuffer(encoded);
        try {
            PacketProgramEditReply packet = new PacketProgramEditReply();
            packet.fromBytes(bytes);
            return packet;
        } finally {
            bytes.release();
        }
    }

    private static String repeat(String text, int count) {
        return String.join("", Collections.nCopies(count, text));
    }
}
