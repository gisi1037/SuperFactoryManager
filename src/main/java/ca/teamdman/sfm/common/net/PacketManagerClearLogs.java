package ca.teamdman.sfm.common.net;

import net.minecraft.entity.player.EntityPlayerMP;

import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketManagerClearLogs implements IMessage {

    private int x;
    private int y;
    private int z;

    public PacketManagerClearLogs() {}

    public PacketManagerClearLogs(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    public static class Handler implements IMessageHandler<PacketManagerClearLogs, IMessage> {

        @Override
        public IMessage onMessage(PacketManagerClearLogs message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player != null && player.worldObj != null) {
                TileEntityManager manager = ManagerAccess.get(player, message.x, message.y, message.z);
                if (manager != null) {
                    manager.clearLogs();
                    return new PacketManagerLogs(
                        message.x,
                        message.y,
                        message.z,
                        manager.getLogLevel()
                            .name(),
                        manager.getLogs());
                }
            }
            return null;
        }
    }
}
