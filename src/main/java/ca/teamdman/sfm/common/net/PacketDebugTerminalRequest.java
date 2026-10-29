package ca.teamdman.sfm.common.net;

import net.minecraft.entity.player.EntityPlayerMP;

import ca.teamdman.sfm.common.program.DebugConsole;
import ca.teamdman.sfm.common.program.DebugTerminalService;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public final class PacketDebugTerminalRequest implements IMessage {

    private static final int VERSION = 1;
    public int x, y, z, windowId;
    public long requestId;
    public String command = "";
    public boolean valid;

    public PacketDebugTerminalRequest() {}

    public PacketDebugTerminalRequest(int x, int y, int z, int windowId, long requestId, String command) {
        if (command == null || command.length() > DebugTerminalService.MAX_COMMAND_LENGTH)
            throw new IllegalArgumentException("Terminal command exceeds 128 characters");
        if (windowId < 1 || windowId > 100 || requestId <= 0)
            throw new IllegalArgumentException("Invalid terminal request");
        this.x = x;
        this.y = y;
        this.z = z;
        this.windowId = windowId;
        this.requestId = requestId;
        this.command = command;
        valid = true;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        valid = false;
        try {
            if (buf.readableBytes() > 420 || buf.readInt() != VERSION) return;
            x = buf.readInt();
            y = buf.readInt();
            z = buf.readInt();
            windowId = buf.readInt();
            requestId = buf.readLong();
            command = ProgramEditWire.string(buf, 384);
            valid = !buf.isReadable() && windowId >= 1
                && windowId <= 100
                && requestId > 0
                && command.length() <= DebugTerminalService.MAX_COMMAND_LENGTH;
        } catch (RuntimeException malformed) {
            valid = false;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(VERSION);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(windowId);
        buf.writeLong(requestId);
        ProgramEditWire.string(buf, command, 384);
    }

    public static final class Handler implements IMessageHandler<PacketDebugTerminalRequest, IMessage> {

        @Override
        public IMessage onMessage(PacketDebugTerminalRequest request, MessageContext context) {
            if (!request.valid) return null;
            EntityPlayerMP player = context.getServerHandler().playerEntity;
            ServerUiTasks.enqueue(() -> {
                TileEntityManager manager = DebugTerminalService
                    .authorizedManager(player, request.x, request.y, request.z, request.windowId);
                if (manager == null) return;
                java.util.List<String> result = DebugTerminalService.execute(manager, request.command);
                DebugConsole console = manager.getDebugConsole();
                SFMNetwork.WRAPPER.sendTo(
                    new PacketDebugTerminalReply(
                        request.x,
                        request.y,
                        request.z,
                        request.windowId,
                        request.requestId,
                        console.isEnabled(),
                        console.getDroppedLines(),
                        result,
                        console.snapshot()),
                    player);
            });
            return null;
        }
    }
}
