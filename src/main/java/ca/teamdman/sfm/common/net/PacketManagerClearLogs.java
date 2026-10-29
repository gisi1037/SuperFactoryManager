package ca.teamdman.sfm.common.net;

import net.minecraft.entity.player.EntityPlayerMP;

import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketManagerClearLogs implements IMessage {

    private static final int MAGIC = 0x53464d4c;
    private ca.teamdman.sfm.common.gui.ManagerControlToken token;
    private boolean valid;
    private int x;
    private int y;
    private int z;

    public PacketManagerClearLogs() {}

    public PacketManagerClearLogs(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PacketManagerClearLogs(int x, int y, int z, ca.teamdman.sfm.common.gui.ManagerControlToken token) {
        this(x, y, z);
        this.token = token;
        valid = token != null;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        valid = false;
        token = null;
        try {
            if (buf.readableBytes() > 80 || buf.readInt() != MAGIC) return;
            x = buf.readInt();
            y = buf.readInt();
            z = buf.readInt();
            token = ca.teamdman.sfm.common.gui.ManagerControlToken.read(buf);
            valid = !buf.isReadable();
        } catch (RuntimeException malformed) {
            token = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(MAGIC);
        if (token == null) return; // Old coordinate-only callers fail closed.
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        token.write(buf);
    }

    public static class Handler implements IMessageHandler<PacketManagerClearLogs, IMessage> {

        @Override
        public IMessage onMessage(PacketManagerClearLogs message, MessageContext ctx) {
            if (!message.valid) return null;
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            ServerUiTasks.enqueue(() -> {
                if (player == null || player.worldObj == null) return;
                TileEntityManager manager = ManagerAccess.get(player, message.x, message.y, message.z, message.token);
                if (manager != null) {
                    manager.clearLogs();
                    SFMNetwork.WRAPPER.sendTo(
                        new PacketManagerLogs(
                            message.x,
                            message.y,
                            message.z,
                            manager.getLogLevel()
                                .name(),
                            manager.getLogs()),
                        player);
                }
            });
            return null;
        }
    }
}
