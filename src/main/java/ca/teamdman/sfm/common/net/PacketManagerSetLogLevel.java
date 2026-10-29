package ca.teamdman.sfm.common.net;

import net.minecraft.entity.player.EntityPlayerMP;

import ca.teamdman.sfm.common.tileentity.LogLevel;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketManagerSetLogLevel implements IMessage {

    private static final int MAGIC = 0x53464d47;
    private ca.teamdman.sfm.common.gui.ManagerControlToken token;
    private boolean valid;
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

    public PacketManagerSetLogLevel(int x, int y, int z, String level, boolean runOnce,
        ca.teamdman.sfm.common.gui.ManagerControlToken token) {
        this(x, y, z, level, runOnce);
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
            runOnce = buf.readBoolean();
            logLevel = ProgramEditWire.string(buf, 16);
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
        buf.writeBoolean(runOnce);
        ProgramEditWire.string(buf, logLevel, 16);
    }

    public static class Handler implements IMessageHandler<PacketManagerSetLogLevel, IMessage> {

        @Override
        public IMessage onMessage(PacketManagerSetLogLevel message, MessageContext ctx) {
            if (!message.valid) return null;
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            ServerUiTasks.enqueue(() -> {
                if (player == null || player.worldObj == null) return;
                TileEntityManager manager = ManagerAccess.get(player, message.x, message.y, message.z, message.token);
                if (manager != null) {
                    LogLevel level = LogLevel.fromString(message.logLevel);
                    if (message.runOnce) manager.runOnceWithLogLevel(level);
                    else manager.setLogLevel(level);
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
