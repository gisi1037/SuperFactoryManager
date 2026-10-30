package ca.teamdman.sfm.common.net;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

import ca.teamdman.sfm.common.program.DebugConsole;
import ca.teamdman.sfm.common.program.DebugTerminalService;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BoundedUtf8;
import ca.teamdman.sfm.integration.mui2.ProgrammingStationMui2Container;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Station authority is distinct from a manager editing session; no existing manager permission is widened. */
public final class PacketStationDebugRequest implements IMessage {

    private static final int VERSION = 0x53544401;
    public static final int MAX_WIRE_BYTES = 512;

    public enum Action {
        CONNECT,
        COMMAND,
        DISCONNECT
    }

    public int x, y, z, windowId, targetX, targetY, targetZ;
    public UUID sessionId;
    public long requestId;
    public Action action = Action.CONNECT;
    public String command = "";

    public void validate() {
        if (sessionId == null || requestId <= 0
            || windowId < 1
            || windowId > 255
            || action == null
            || command == null
            || command.length() > DebugTerminalService.MAX_COMMAND_LENGTH)
            throw new IllegalArgumentException("Invalid station terminal identity or command");
        BoundedUtf8.encode(command, 384);
        if (action != Action.COMMAND && !command.isEmpty())
            throw new IllegalArgumentException("Connection actions cannot carry terminal commands");
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
        buffer.writeInt(targetX);
        buffer.writeInt(targetY);
        buffer.writeInt(targetZ);
        buffer.writeByte(action.ordinal());
        ProgramEditWire.string(buffer, command, 384);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        if (buffer.readableBytes() > MAX_WIRE_BYTES || buffer.readInt() != VERSION)
            throw new IllegalArgumentException("Invalid station terminal frame");
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        windowId = buffer.readInt();
        sessionId = ProgramEditWire.uuid(buffer);
        requestId = buffer.readLong();
        targetX = buffer.readInt();
        targetY = buffer.readInt();
        targetZ = buffer.readInt();
        int ordinal = buffer.readUnsignedByte();
        if (ordinal >= Action.values().length) throw new IllegalArgumentException("Unknown station terminal action");
        action = Action.values()[ordinal];
        command = ProgramEditWire.string(buffer, 384);
        if (buffer.isReadable()) throw new IllegalArgumentException("Trailing station terminal bytes");
        validate();
    }

    private static final Map<ProgrammingStationMui2Container, Binding> BINDINGS = new WeakHashMap<>();

    private static final class Binding {

        long lastRequest;
        WeakReference<TileEntityManager> target = new WeakReference<>(null);
    }

    public static void clear(ProgrammingStationMui2Container container) {
        if (ServerUiTasks.isServerThread()) BINDINGS.remove(container);
    }

    /** Server-thread only, including connection checks, loaded-tile resolution and diagnostic execution. */
    public static PacketStationDebugReply process(EntityPlayerMP player, PacketStationDebugRequest request) {
        if (!ServerUiTasks.isServerThread()) return null;
        request.validate();
        ProgrammingStationMui2Container container = authorizedStation(player, request);
        if (container == null) return null;
        Binding binding = BINDINGS.get(container);
        if (binding == null) {
            binding = new Binding();
            BINDINGS.put(container, binding);
        }
        if (request.requestId <= binding.lastRequest)
            return new PacketStationDebugReply(request, false, "Stale terminal request", null);
        binding.lastRequest = request.requestId;
        if (request.action == Action.DISCONNECT) {
            binding.target.clear();
            return new PacketStationDebugReply(request, true, "Disconnected", null);
        }
        TileEntityManager target = loadedTarget(player, request);
        if (target == null || request.action == Action.COMMAND && binding.target.get() != target) {
            binding.target.clear();
            return new PacketStationDebugReply(
                request,
                false,
                "Manager unavailable, replaced or out of range. Connect to a loaded manager within 8 blocks.",
                null);
        }
        if (request.action == Action.CONNECT) binding.target = new WeakReference<>(target);
        List<String> result = request.action == Action.CONNECT
            ? Collections
                .singletonList("Connected. Type help; capture is enabled only by an explicit capture on command.")
            : DebugTerminalService.execute(target, request.command, player.getUniqueID());
        DebugConsole console = target.getDebugConsole();
        // Reuse the existing independently bounded diagnostic codec inside the station envelope.
        PacketDebugTerminalReply payload = new PacketDebugTerminalReply(
            0,
            0,
            0,
            1,
            1,
            console.isEnabled(),
            console.getDroppedLines(),
            result,
            console.snapshot());
        return new PacketStationDebugReply(request, true, "Connected", payload);
    }

    public static ProgrammingStationMui2Container authorizedStation(EntityPlayerMP player,
        PacketStationDebugRequest request) {
        if (!ServerUiTasks.isServerThread() || !PlayerUiAccess.isLive(player)
            || !(player.openContainer instanceof ProgrammingStationMui2Container)) return null;
        ProgrammingStationMui2Container container = (ProgrammingStationMui2Container) player.openContainer;
        return container.windowId == request.windowId && container.sessionId.equals(request.sessionId)
            && container.station != null
            && !container.station.isInvalid()
            && container.station.xCoord == request.x
            && container.station.yCoord == request.y
            && container.station.zCoord == request.z
            && container.station.isUsableBy(player) ? container : null;
    }

    private static TileEntityManager loadedTarget(EntityPlayerMP player, PacketStationDebugRequest request) {
        // blockExists precedes getTileEntity: a request must never cause a chunk load.
        if (player.getDistanceSq(request.targetX + .5, request.targetY + .5, request.targetZ + .5) > 64
            || !player.worldObj.blockExists(request.targetX, request.targetY, request.targetZ)) return null;
        TileEntity tile = player.worldObj.getTileEntity(request.targetX, request.targetY, request.targetZ);
        if (!(tile instanceof TileEntityManager)) return null;
        TileEntityManager manager = (TileEntityManager) tile;
        return manager.isUseableByPlayer(player) ? manager : null;
    }

    public static final class Handler implements IMessageHandler<PacketStationDebugRequest, IMessage> {

        @Override
        public IMessage onMessage(PacketStationDebugRequest request, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().playerEntity;
            ServerUiTasks.enqueue(() -> {
                PacketStationDebugReply reply = process(player, request);
                if (reply != null) SFMNetwork.WRAPPER.sendTo(reply, player);
            });
            return null;
        }
    }
}
