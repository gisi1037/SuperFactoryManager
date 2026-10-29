package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.gui.ManagerControlToken;
import ca.teamdman.sfm.common.net.PacketManagerAction;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

class ManagerControlTokenTest {

    private static final UUID SESSION = new UUID(123, 456);

    @Test
    void delayedActionRejectsReplacedDiskAndReopenedContainerEvenWhenWindowIdWraps() {
        ManagerControlToken queued = new ManagerControlToken(SESSION, 27, 42);
        assertTrue(queued.matches(SESSION, 27, 42));
        assertFalse(queued.matches(SESSION, 28, 42), "Replacing even an identical disk changes its binding");
        assertFalse(queued.matches(new UUID(123, 457), 27, 42), "A reused window ID does not reuse a session");
        assertFalse(queued.matches(SESSION, 27, 43));
    }

    @Test
    void tokenCodecIsFixedSizeAndPreservesUnsignedUuidBits() {
        ManagerControlToken original = new ManagerControlToken(new UUID(Long.MIN_VALUE, -1), Long.MAX_VALUE, 100);
        ByteBuf buffer = Unpooled.buffer();
        try {
            original.write(buffer);
            assertEquals(ManagerControlToken.WIRE_BYTES, buffer.readableBytes());
            ManagerControlToken decoded = ManagerControlToken.read(buffer);
            assertEquals(original, decoded);
            assertEquals(original.hashCode(), decoded.hashCode());
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    @Test
    void malformedTokenCannotSupplyUnboundedLengthsOrInvalidAuthority() {
        assertThrows(NullPointerException.class, () -> new ManagerControlToken(null, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new ManagerControlToken(SESSION, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> new ManagerControlToken(SESSION, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new ManagerControlToken(SESSION, 0, 101));
        ByteBuf truncated = Unpooled.buffer(ManagerControlToken.WIRE_BYTES - 1);
        try {
            truncated.writeZero(ManagerControlToken.WIRE_BYTES - 1);
            assertThrows(IllegalArgumentException.class, () -> ManagerControlToken.read(truncated));
        } finally {
            truncated.release();
        }
    }

    @Test
    void guardedPacketFrameIsFixedAndOldConstructorCannotSupplyAuthority() throws Exception {
        ManagerControlToken token = new ManagerControlToken(SESSION, 7, 42);
        PacketManagerAction original = new PacketManagerAction(10, 20, 30, PacketManagerAction.ACTION_RESET, token);
        ByteBuf frame = Unpooled.buffer();
        ByteBuf oldFrame = Unpooled.buffer();
        try {
            original.toBytes(frame);
            assertEquals(50, frame.readableBytes());
            PacketManagerAction decoded = new PacketManagerAction();
            decoded.fromBytes(frame);
            assertTrue(valid(decoded));
            java.lang.reflect.Field tokenField = PacketManagerAction.class.getDeclaredField("token");
            tokenField.setAccessible(true);
            assertEquals(token, tokenField.get(decoded));
            new PacketManagerAction(10, 20, 30, PacketManagerAction.ACTION_RESET).toBytes(oldFrame);
            PacketManagerAction missing = new PacketManagerAction();
            missing.fromBytes(oldFrame);
            assertFalse(valid(missing));
        } finally {
            frame.release();
            oldFrame.release();
        }
    }

    @Test
    void truncatedLegacyUnknownVersionAndTrailingPacketBytesFailClosed() throws Exception {
        PacketManagerAction original = new PacketManagerAction(
            10,
            20,
            30,
            PacketManagerAction.ACTION_RESET,
            new ManagerControlToken(SESSION, 7, 42));
        ByteBuf frame = Unpooled.buffer();
        try {
            original.toBytes(frame);
            for (int length = 0; length < frame.readableBytes(); length++) {
                PacketManagerAction decoded = new PacketManagerAction();
                decoded.fromBytes(frame.slice(0, length));
                assertFalse(valid(decoded), "Truncated length " + length);
            }
            ByteBuf wrongVersion = frame.copy();
            ByteBuf trailing = frame.copy();
            ByteBuf legacy = Unpooled.buffer();
            try {
                wrongVersion.setByte(4, 127);
                PacketManagerAction decoded = new PacketManagerAction();
                decoded.fromBytes(wrongVersion);
                assertFalse(valid(decoded));
                trailing.writeByte(0);
                decoded.fromBytes(trailing);
                assertFalse(valid(decoded));
                legacy.writeInt(10)
                    .writeInt(20)
                    .writeInt(30)
                    .writeInt(PacketManagerAction.ACTION_RESET);
                decoded.fromBytes(legacy);
                assertFalse(valid(decoded));
            } finally {
                wrongVersion.release();
                trailing.release();
                legacy.release();
            }
        } finally {
            frame.release();
        }
    }

    private static boolean valid(PacketManagerAction packet) throws Exception {
        java.lang.reflect.Field field = PacketManagerAction.class.getDeclaredField("valid");
        field.setAccessible(true);
        return field.getBoolean(packet);
    }
}
