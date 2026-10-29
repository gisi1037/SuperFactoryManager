package ca.teamdman.sfm.client.gui;

import ca.teamdman.sfm.common.net.PacketProgramEditReply;
import ca.teamdman.sfm.common.net.PacketSaveProgram;
import ca.teamdman.sfm.common.net.SFMNetwork;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

/** One screen's authoritative editor session. Dispatch occurs on the Minecraft client thread. */
public final class ProgramEditSession {

    public interface Receiver {

        void receiveProgramEditReply(PacketProgramEditReply reply);
    }

    public final ProgramEditState state = new ProgramEditState();
    private final int x, y, z, windowId;

    public ProgramEditSession(TileEntityManager manager, int windowId) {
        x = manager.xCoord;
        y = manager.yCoord;
        z = manager.zCoord;
        this.windowId = windowId;
    }

    public void open() {
        SFMNetwork.WRAPPER.sendToServer(PacketSaveProgram.open(x, y, z, state.begin(ProgramEditState.Action.OPEN)));
    }

    public void save(String source) {
        long id = state.begin(ProgramEditState.Action.SAVE);
        SFMNetwork.WRAPPER.sendToServer(PacketSaveProgram.save(x, y, z, id, state.snapshot(), source));
    }

    public boolean receive(PacketProgramEditReply reply) {
        return reply.x == x && reply.y == y
            && reply.z == z
            && reply.windowId == windowId
            && state.receive(reply.requestId, reply.accepted, reply.snapshot);
    }
}
