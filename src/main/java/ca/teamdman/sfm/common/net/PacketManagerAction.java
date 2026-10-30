package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.common.gui.ManagerControlToken;
import ca.teamdman.sfm.common.gui.ManagerEditContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketManagerAction implements IMessage {

    private static final int MAGIC = 0x53464d43; // SFMC; deliberately incompatible with the old coordinate-only frame.
    private static final int VERSION = 1;
    private static final int HEADER_BYTES = 22;
    public static final int ACTION_RESET = 1;
    public static final int ACTION_REBUILD = 2;
    public static final int ACTION_RESET_STATE = 3;
    public static final int ACTION_TOGGLE_RUNNING = 4;

    private int x;
    private int y;
    private int z;
    private int action;
    private ManagerControlToken token;
    private boolean valid;

    public PacketManagerAction() {}

    public PacketManagerAction(int x, int y, int z, int action) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.action = action;
    }

    public PacketManagerAction(int x, int y, int z, int action, ManagerControlToken token) {
        this(x, y, z, action);
        this.token = java.util.Objects.requireNonNull(token, "token");
        this.valid = knownAction(action);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        valid = false;
        token = null;
        if (buf.readableBytes() < HEADER_BYTES || buf.readInt() != MAGIC || buf.readUnsignedByte() != VERSION) return;
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.action = buf.readInt();
        int present = buf.readUnsignedByte();
        if (present != 1 || buf.readableBytes() != ManagerControlToken.WIRE_BYTES || !knownAction(action)) return;
        try {
            token = ManagerControlToken.read(buf);
            valid = true;
        } catch (IllegalArgumentException malformed) {
            token = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(MAGIC);
        buf.writeByte(VERSION);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(action);
        buf.writeBoolean(token != null);
        if (token != null) token.write(buf);
    }

    private static boolean knownAction(int action) {
        return action >= ACTION_RESET && action <= ACTION_TOGGLE_RUNNING;
    }

    public static class Handler implements IMessageHandler<PacketManagerAction, IMessage> {

        @Override
        public IMessage onMessage(PacketManagerAction msg, MessageContext ctx) {
            if (!msg.valid || msg.token == null) return null;
            net.minecraft.entity.player.EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            ServerUiTasks.enqueue(() -> {
                if (player == null || player.worldObj == null) return;
                TileEntityManager manager = ManagerAccess.get(player, msg.x, msg.y, msg.z);
                if (manager == null) return;
                ManagerEditContext context = (ManagerEditContext) player.openContainer;
                // Validate on dequeue, after any intervening clicks, close/reopen or disk swap.
                if (!msg.token
                    .matches(context.getEditSessionId(), manager.getDiskGeneration(), player.openContainer.windowId)) {
                    if (msg.action == ACTION_RESET_STATE) player.addChatMessage(
                        new net.minecraft.util.ChatComponentText(
                            "[SFM] State not reset: disk or editing session changed. Reopen the manager."));
                    return;
                }
                if (msg.action == ACTION_RESET) manager.reset();
                else if (msg.action == ACTION_TOGGLE_RUNNING) {
                    if (manager.canRequestRun()) manager.setRunning(!manager.isRunning());
                } else if (msg.action == ACTION_RESET_STATE) {
                    manager.resetProgramState();
                    player.addChatMessage(
                        new net.minecraft.util.ChatComponentText("[SFM] " + manager.getStatusMessage()));
                } else if (msg.action == ACTION_REBUILD) manager.rebuildNetwork();
                player.worldObj.markBlockForUpdate(msg.x, msg.y, msg.z);
            });
            return null;
        }
    }
}
