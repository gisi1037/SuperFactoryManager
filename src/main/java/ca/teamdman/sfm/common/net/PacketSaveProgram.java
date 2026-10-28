package ca.teamdman.sfm.common.net;

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
    private byte[] expectedRevision;

    public PacketSaveProgram() {}

    public PacketSaveProgram(int x, int y, int z, String code, boolean running) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.code = code != null ? code : "";
        this.running = running;
    }

    public static PacketSaveProgram guarded(int x, int y, int z, String code, String original) {
        PacketSaveProgram packet = new PacketSaveProgram(x, y, z, code, false);
        packet.expectedRevision = ca.teamdman.sfm.common.util.ProgramRevision.digest(original);
        return packet;
    }

    public boolean acceptsSource(String current) {
        return expectedRevision == null
            || ca.teamdman.sfm.common.util.ProgramRevision.matches(expectedRevision, current);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        running = buf.readBoolean();
        code = ByteBufUtils.readUTF8String(buf);
        expectedRevision = null;
        if (buf.isReadable() && buf.readBoolean()) {
            expectedRevision = new byte[32];
            buf.readBytes(expectedRevision);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeBoolean(running);
        ByteBufUtils.writeUTF8String(buf, code);
        buf.writeBoolean(expectedRevision != null);
        if (expectedRevision != null) buf.writeBytes(expectedRevision);
    }

    public static class Handler implements IMessageHandler<PacketSaveProgram, IMessage> {

        @Override
        public IMessage onMessage(PacketSaveProgram message, MessageContext ctx) {
            World world = ctx.getServerHandler().playerEntity.worldObj;
            if (world != null) {
                TileEntityManager manager = ManagerAccess
                    .get(ctx.getServerHandler().playerEntity, message.x, message.y, message.z);
                if (manager != null) {
                    if (message.expectedRevision != null) {
                        String current = ca.teamdman.sfm.common.item.ItemDisk.getProgramString(manager.getDisk());
                        if (manager.getDisk() == null || !message.acceptsSource(current)) {
                            ctx.getServerHandler().playerEntity.addChatMessage(
                                new net.minecraft.util.ChatComponentText(
                                    "[SFM] Blueprint not saved: disk source changed. Reopen the editor to load the current program."));
                            return null;
                        }
                        if (message.code.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 32700
                            || !new ca.teamdman.sfml.program_builder.ProgramBuilder(message.code).build()
                                .isSuccess()) {
                            ctx.getServerHandler().playerEntity.addChatMessage(
                                new net.minecraft.util.ChatComponentText(
                                    "[SFM] Blueprint not saved: invalid or oversized program."));
                            return null;
                        }
                    } else manager.setRunning(message.running);
                    manager.saveCodeAndRecompile(message.code);
                    world.markBlockForUpdate(message.x, message.y, message.z);
                }
            }
            return null;
        }
    }
}
