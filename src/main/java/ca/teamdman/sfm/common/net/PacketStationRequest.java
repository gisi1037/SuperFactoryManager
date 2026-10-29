package ca.teamdman.sfm.common.net;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;

import ca.teamdman.sfm.common.program.project.ProjectWorkspace;
import ca.teamdman.sfm.common.program.project.StationProjectService;
import ca.teamdman.sfm.common.util.BoundedUtf8;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Bounded authoring requests; identity always comes from the authenticated connection. */
public final class PacketStationRequest implements IMessage {

    private static final int VERSION = 0x53545001;
    public static final int MAX_PACKET_BYTES = 32000;

    public enum Action {
        LIST,
        CREATE,
        LOAD,
        SAVE,
        VALIDATE
    }

    public int x, y, z, windowId, offset;
    public UUID sessionId, projectId;
    public long requestId, expectedRevision;
    public Action action = Action.LIST;
    public String name = "", source = "";

    public void validate() {
        if (action == null || sessionId == null || requestId <= 0 || windowId <= 0 || windowId > 255)
            throw new IllegalArgumentException("Invalid station session/request");
        if (expectedRevision < 0 || offset < 0 || offset > ProjectWorkspace.MAX_PROJECTS_PER_OWNER)
            throw new IllegalArgumentException("Invalid project revision/page");
        if ((action == Action.LOAD || action == Action.SAVE || action == Action.VALIDATE) && projectId == null)
            throw new IllegalArgumentException("A project is required");
        BoundedUtf8.encode(name, ProjectWorkspace.MAX_NAME_BYTES);
        BoundedUtf8.encode(source, ProjectWorkspace.MAX_SOURCE_BYTES);
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        validate();
        buffer.writeInt(VERSION);
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeInt(windowId);
        ProgramEditWire.uuid(buffer, sessionId);
        buffer.writeLong(requestId);
        buffer.writeByte(action.ordinal());
        buffer.writeBoolean(projectId != null);
        if (projectId != null) ProgramEditWire.uuid(buffer, projectId);
        buffer.writeLong(expectedRevision);
        buffer.writeInt(offset);
        ProgramEditWire.string(buffer, name, ProjectWorkspace.MAX_NAME_BYTES);
        ProgramEditWire.string(buffer, source, ProjectWorkspace.MAX_SOURCE_BYTES);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        if (buffer.readableBytes() > MAX_PACKET_BYTES || buffer.readInt() != VERSION)
            throw new IllegalArgumentException("Unsupported or oversized station request");
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        windowId = buffer.readInt();
        sessionId = ProgramEditWire.uuid(buffer);
        requestId = buffer.readLong();
        int index = buffer.readUnsignedByte();
        if (index >= Action.values().length) throw new IllegalArgumentException("Unknown station action");
        action = Action.values()[index];
        projectId = buffer.readBoolean() ? ProgramEditWire.uuid(buffer) : null;
        expectedRevision = buffer.readLong();
        offset = buffer.readInt();
        name = ProgramEditWire.string(buffer, ProjectWorkspace.MAX_NAME_BYTES);
        source = ProgramEditWire.string(buffer, ProjectWorkspace.MAX_SOURCE_BYTES);
        if (buffer.isReadable()) throw new IllegalArgumentException("Trailing station request data");
        validate();
    }

    public static final class Handler implements IMessageHandler<PacketStationRequest, IMessage> {

        @Override
        public IMessage onMessage(PacketStationRequest packet, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().playerEntity;
            // Netty must not resolve a world, station, project or container.
            ServerUiTasks.enqueue(() -> StationProjectService.INSTANCE.process(player, packet));
            return null;
        }
    }
}
