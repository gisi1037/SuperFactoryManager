package ca.teamdman.sfm.common.net;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.gui.ManagerControlToken;
import ca.teamdman.sfm.common.program.ManagerNetworkBrowser;
import ca.teamdman.sfm.common.program.ManagerNetworkBrowser.Page;
import ca.teamdman.sfm.common.program.ManagerNetworkBrowser.Row;
import ca.teamdman.sfm.common.util.BlockPos;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

class ManagerNetworkWireTest {

    private static final ManagerControlToken TOKEN = new ManagerControlToken(new UUID(1, 2), 3, 4);

    @Test
    void requestsBindTheExactManagerSessionAndProgressiveSearchWithoutSourceOrContents() {
        PacketManagerNetworkRequest request = request();
        request.snapshotId = 7;
        request.cursor = 128;
        request.query = "assembler \u4e2d";
        request.filter = ManagerNetworkBrowser.FILTER_ITEMS;
        byte[] wire = encode(request);
        PacketManagerNetworkRequest decoded = request(wire);
        assertEquals(TOKEN, decoded.token);
        assertEquals(5, decoded.requestId);
        assertEquals(7, decoded.snapshotId);
        assertEquals(128, decoded.cursor);
        assertEquals(request.query, decoded.query);
        assertEquals(request.filter, decoded.filter);
        assertTrue(wire.length < PacketManagerNetworkRequest.MAX_PACKET_BYTES);
        for (int size = 0; size < wire.length; size++) {
            byte[] truncated = Arrays.copyOf(wire, size);
            assertThrows(RuntimeException.class, () -> request(truncated));
        }
        assertThrows(RuntimeException.class, () -> request(Arrays.copyOf(wire, wire.length + 1)));
        byte[] wrongVersion = wire.clone();
        wrongVersion[0] ^= 1;
        assertThrows(RuntimeException.class, () -> request(wrongVersion));
    }

    @Test
    void requestBoundsRejectUnboundCursorsUnknownFiltersAndInvalidUtf8() {
        PacketManagerNetworkRequest request = request();
        request.cursor = 1;
        assertThrows(IllegalArgumentException.class, () -> encode(request));
        request.cursor = 0;
        request.filter = 3;
        assertThrows(IllegalArgumentException.class, () -> encode(request));
        request.filter = 0;
        request.query = repeat("x", 65);
        assertThrows(IllegalArgumentException.class, () -> encode(request));
        request.query = "\ud800";
        assertThrows(IllegalArgumentException.class, () -> encode(request));
        request.query = "";
        request.snapshotId = -1;
        assertThrows(IllegalArgumentException.class, () -> encode(request));
    }

    @Test
    void metadataReplyPreservesUnavailableCoordinatesKindsLabelsAndSnapshotIdentity() {
        Row row = new Row(
            new BlockPos(Integer.MIN_VALUE, -1, Integer.MAX_VALUE),
            "Unloaded \u4e2d",
            "ped, input",
            0,
            ManagerNetworkBrowser.STATE_UNLOADED);
        Page page = new Page(9, 0, -1, 1, 100, 90, false, true, "Cached metadata", Collections.singletonList(row));
        PacketManagerNetworkReply reply = new PacketManagerNetworkReply(request(), true, "", page);
        byte[] wire = encode(reply);
        Page decoded = reply(wire).page;
        assertEquals(9, decoded.snapshotId);
        assertEquals(100, decoded.capturedTick);
        assertEquals(90, decoded.topologyTick);
        assertFalse(decoded.topologyCurrent);
        assertTrue(decoded.partial);
        assertEquals(row.position, decoded.rows.get(0).position);
        assertEquals(row.labels, decoded.rows.get(0).labels);
        assertEquals(row.state, decoded.rows.get(0).state);
        assertThrows(UnsupportedOperationException.class, () -> decoded.rows.clear());
        for (int size = 0; size < wire.length; size++) {
            byte[] truncated = Arrays.copyOf(wire, size);
            assertThrows(RuntimeException.class, () -> reply(truncated));
        }
        assertThrows(RuntimeException.class, () -> reply(Arrays.copyOf(wire, wire.length + 1)));
        byte[] invalidFlag = wire.clone();
        invalidFlag[52] = 2;
        assertThrows(IllegalArgumentException.class, () -> reply(invalidFlag));
    }

    @Test
    void maximumMetadataPageFitsEnvelopeAndRejectionsCannotCarryAcceptedRows() {
        Row row = new Row(new BlockPos(0, 64, 0), repeat("\u4e2d", 96), repeat("\u4e2d", 256), 3, 5);
        Page page = new Page(1, 0, -1, 8, 0, -1, false, true, repeat("\u4e2d", 256), Collections.nCopies(8, row));
        PacketManagerNetworkReply accepted = new PacketManagerNetworkReply(
            request(),
            true,
            repeat("\u4e2d", 256),
            page);
        byte[] wire = encode(accepted);
        assertTrue(wire.length < PacketManagerNetworkReply.MAX_PACKET_BYTES);
        assertEquals(8, reply(wire).page.rows.size());
        assertThrows(
            IllegalArgumentException.class,
            () -> new PacketManagerNetworkReply(request(), false, "Rejected", page));
        assertThrows(
            IllegalArgumentException.class,
            () -> new PacketManagerNetworkReply(request(), true, "Accepted", null));
        PacketManagerNetworkReply rejected = reply(
            encode(new PacketManagerNetworkReply(request(), false, "Refresh", null)));
        assertFalse(rejected.accepted);
        assertNull(rejected.page);
        assertEquals("Refresh", rejected.message);
        assertThrows(
            IllegalArgumentException.class,
            () -> new Page(1, 0, 129, 256, 0, -1, false, false, "", Collections.emptyList()));
        assertThrows(IllegalArgumentException.class, () -> new Row(row.position, row.name, row.labels, 4, 0));
        assertThrows(IllegalArgumentException.class, () -> new Row(row.position, row.name, row.labels, 0, 6));
    }

    private static PacketManagerNetworkRequest request() {
        return new PacketManagerNetworkRequest(1, 64, 2, TOKEN, 5, 0, 0, "", 0);
    }

    private static String repeat(String text, int count) {
        return String.join("", Collections.nCopies(count, text));
    }

    private static byte[] encode(IMessage message) {
        ByteBuf buffer = Unpooled.buffer();
        try {
            message.toBytes(buffer);
            byte[] wire = new byte[buffer.readableBytes()];
            buffer.readBytes(wire);
            return wire;
        } finally {
            buffer.release();
        }
    }

    private static PacketManagerNetworkRequest request(byte[] wire) {
        ByteBuf buffer = Unpooled.wrappedBuffer(wire);
        try {
            PacketManagerNetworkRequest result = new PacketManagerNetworkRequest();
            result.fromBytes(buffer);
            return result;
        } finally {
            buffer.release();
        }
    }

    private static PacketManagerNetworkReply reply(byte[] wire) {
        ByteBuf buffer = Unpooled.wrappedBuffer(wire);
        try {
            PacketManagerNetworkReply result = new PacketManagerNetworkReply();
            result.fromBytes(buffer);
            return result;
        } finally {
            buffer.release();
        }
    }
}
