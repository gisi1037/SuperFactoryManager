package ca.teamdman.sfm.common.net;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

import ca.teamdman.sfm.common.tileentity.LogLevel;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketManagerSetLogLevel implements IMessage {

    private int x;
    private int y;
    private int z;
    private String logLevel;
    private boolean runOnce;

    public PacketManagerSetLogLevel() {}

    public PacketManagerSetLogLevel(int x, int y, int z, String logLevel, boolean runOnce) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.logLevel = logLevel != null ? logLevel : LogLevel.OFF.name();
        this.runOnce = runOnce;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        runOnce = buf.readBoolean();
        logLevel = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeBoolean(runOnce);
        ByteBufUtils.writeUTF8String(buf, logLevel);
    }

    public static class Handler implements IMessageHandler<PacketManagerSetLogLevel, IMessage> {

        @Override
        public IMessage onMessage(PacketManagerSetLogLevel message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player != null && player.worldObj != null) {
                TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);
                if (te instanceof TileEntityManager) {
                    TileEntityManager manager = (TileEntityManager) te;
                    LogLevel level = LogLevel.fromString(message.logLevel);

                    if (message.runOnce) {
                        manager.runOnceWithLogLevel(level);
                    } else {
                        manager.setLogLevel(level);
                    }

                    // Return updated logs back to the client
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
