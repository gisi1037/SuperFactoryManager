package ca.teamdman.sfm.common.gui;

import java.util.UUID;

import ca.teamdman.sfm.common.net.PacketProgramEditReply;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

/** Editing authority belongs to the live server container, independently of its GUI framework. */
public interface ManagerEditContext {

    TileEntityManager getManager();

    State getEditState();

    default UUID getEditSessionId() {
        return getEditState().sessionId;
    }

    default long getLastProgramEditRequest() {
        PacketProgramEditReply reply = getEditState().reply;
        return reply == null ? 0 : reply.requestId;
    }

    default PacketProgramEditReply getProgramEditReply(long requestId) {
        PacketProgramEditReply reply = getEditState().reply;
        return reply != null && reply.requestId == requestId ? reply : null;
    }

    default void rememberProgramEditReply(PacketProgramEditReply reply) {
        if (reply.requestId > getLastProgramEditRequest()) getEditState().reply = reply;
    }

    /** One cached response makes retries idempotent without retaining drafts or an unbounded history. */
    final class State {

        private final UUID sessionId = UUID.randomUUID();
        private PacketProgramEditReply reply;
    }
}
