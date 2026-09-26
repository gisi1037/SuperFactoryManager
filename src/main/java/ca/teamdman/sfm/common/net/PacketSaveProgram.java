package ca.teamdman.sfm.common.net;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketSaveProgram implements IMessage {

    private int x;
    private int y;
    private int z;
    private String code;
    private boolean running;

    public PacketSaveProgram() {}

    public PacketSaveProgram(int x, int y, int z, String code, boolean running) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.code = code != null ? code : "";
        this.running = running;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        running = buf.readBoolean();
        code = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeBoolean(running);
        ByteBufUtils.writeUTF8String(buf, code);
    }

    public static class Handler implements IMessageHandler<PacketSaveProgram, IMessage> {

        @Override
        public IMessage onMessage(PacketSaveProgram message, MessageContext ctx) {
            World world = ctx.getServerHandler().playerEntity.worldObj;
            if (world != null) {
                TileEntity te = world.getTileEntity(message.x, message.y, message.z);
                if (te instanceof TileEntityManager) {
                    TileEntityManager manager = (TileEntityManager) te;
                    manager.setRunning(message.running);
                    manager.saveCodeAndRecompile(message.code);
                    world.markBlockForUpdate(message.x, message.y, message.z);
                }
            }
            return null;
        }
    }
}
