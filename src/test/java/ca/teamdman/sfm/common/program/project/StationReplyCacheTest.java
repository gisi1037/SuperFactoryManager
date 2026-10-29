package ca.teamdman.sfm.common.program.project;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.net.PacketStationReply;
import ca.teamdman.sfm.common.net.PacketStationRequest;
import ca.teamdman.sfm.integration.mui2.ProgrammingStationMui2Container;

class StationReplyCacheTest {

    @Test
    void aCompletedWriteCachesItsAckOnceAndCannotReplaceAReplyOrClearANewerPendingOperation() {
        ProgrammingStationMui2Container container = new ProgrammingStationMui2Container(null, UUID.randomUUID());
        PacketStationRequest first = new PacketStationRequest();
        first.requestId = 1;
        container.pendingRequestId = 1;
        PacketStationReply success = new PacketStationReply(first, true, "Written");
        assertTrue(StationProjectService.cacheReply(container, first, success));
        assertEquals(0, container.pendingRequestId);
        assertSame(success, container.lastReply);
        assertFalse(StationProjectService.cacheReply(container, first, new PacketStationReply(first, false, "Late")));
        assertSame(success, container.lastReply);
        container.pendingRequestId = 2;
        assertFalse(StationProjectService.cacheReply(container, first, success));
        assertEquals(2, container.pendingRequestId);
        assertSame(success, container.lastReply);
        PacketStationRequest second = new PacketStationRequest();
        second.requestId = 2;
        PacketStationReply rejected = new PacketStationReply(second, false, "Rejected");
        assertTrue(StationProjectService.cacheReply(container, second, rejected));
        assertEquals(0, container.pendingRequestId);
        assertSame(rejected, container.lastReply);
    }
}
