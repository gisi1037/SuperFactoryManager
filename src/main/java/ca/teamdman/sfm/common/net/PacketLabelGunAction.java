package ca.teamdman.sfm.common.net;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import ca.teamdman.sfm.common.item.ItemLabelGun;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketLabelGunAction implements IMessage {

    public static final int ACTION_CLEAR = 0;
    public static final int ACTION_PRUNE = 1;
    public static final int ACTION_CYCLE_VIEW = 2;

    private int action;

    public PacketLabelGunAction() {}

    public PacketLabelGunAction(int action) {
        this.action = action;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        action = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(action);
    }

    public static class Handler implements IMessageHandler<PacketLabelGunAction, IMessage> {

        @Override
        public IMessage onMessage(PacketLabelGunAction message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player != null) {
                ItemStack held = player.getHeldItem();
                if (held != null && held.getItem() instanceof ItemLabelGun) {
                    if (message.action == ACTION_CLEAR) {
                        ItemLabelGun.clearAll(held);
                    } else if (message.action == ACTION_PRUNE) {
                        ItemLabelGun.prune(held);
                    } else if (message.action == ACTION_CYCLE_VIEW) {
                        ItemLabelGun.cycleViewMode(held);
                    }
                }
            }
            return null;
        }
    }
}
