package ca.teamdman.sfm.common.net;

import net.minecraft.world.World;

import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketManagerAction implements IMessage {

    public static final int ACTION_RESET = 1;
    public static final int ACTION_REBUILD = 2;
    public static final int ACTION_RESET_STATE = 3;
    public static final int ACTION_TOGGLE_RUNNING = 4;

    private int x;
    private int y;
    private int z;
    private int action;

    public PacketManagerAction() {}

    public PacketManagerAction(int x, int y, int z, int action) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.action = action;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.action = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(action);
    }

    public static class Handler implements IMessageHandler<PacketManagerAction, IMessage> {

        @Override
        public IMessage onMessage(PacketManagerAction msg, MessageContext ctx) {
            World world = ctx.getServerHandler().playerEntity.worldObj;
            if (world != null) {
                TileEntityManager manager = ManagerAccess.get(ctx.getServerHandler().playerEntity, msg.x, msg.y, msg.z);
                if (manager != null) {
                    if (msg.action == ACTION_RESET) {
                        manager.reset();
                    } else if (msg.action == ACTION_TOGGLE_RUNNING) {
                        if (manager.hasCompiledProgram() && !manager.hasPendingRecovery())
                            manager.setRunning(!manager.isRunning());
                    } else if (msg.action == ACTION_RESET_STATE) {
                        manager.resetProgramState();
                    } else if (msg.action == ACTION_REBUILD) {
                        manager.rebuildNetwork();
                    }
                    world.markBlockForUpdate(msg.x, msg.y, msg.z);
                }
            }
            return null;
        }
    }
}
