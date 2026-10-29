package ca.teamdman.sfm.common.net;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import ca.teamdman.sfm.common.item.ItemLabelGun;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketLabelGunAction implements IMessage {

    public static final int ACTION_CLEAR = 0, ACTION_PRUNE = 1, ACTION_CYCLE_VIEW = 2;
    private static final int MAGIC = 0x53464c41;
    private int action;
    private LabelGunAccess.Token token;
    private boolean valid;

    public PacketLabelGunAction() {}

    public PacketLabelGunAction(int action) {
        this(action, null);
    }

    public PacketLabelGunAction(int action, LabelGunAccess.Token token) {
        this.action = action;
        this.token = token;
        valid = token != null && action >= ACTION_CLEAR && action <= ACTION_CYCLE_VIEW;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        valid = false;
        token = null;
        try {
            if (buf.readableBytes() != 32 || buf.readInt() != MAGIC) return;
            action = buf.readInt();
            token = LabelGunAccess.Token.read(buf);
            valid = action >= ACTION_CLEAR && action <= ACTION_CYCLE_VIEW;
        } catch (RuntimeException malformed) {
            token = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(MAGIC);
        if (token == null) return;
        buf.writeInt(action);
        token.write(buf);
    }

    public static class Handler implements IMessageHandler<PacketLabelGunAction, IMessage> {

        @Override
        public IMessage onMessage(PacketLabelGunAction message, MessageContext context) {
            if (!message.valid) return null;
            EntityPlayerMP player = context.getServerHandler().playerEntity;
            ServerUiTasks.enqueue(() -> {
                ItemStack held = LabelGunAccess.authorized(player, message.token);
                if (held == null) return;
                if (message.action == ACTION_CLEAR) ItemLabelGun.clearAll(held);
                else if (message.action == ACTION_PRUNE) ItemLabelGun.prune(held);
                else ItemLabelGun.cycleViewMode(held);
                player.inventoryContainer.detectAndSendChanges();
            });
            return null;
        }
    }
}
