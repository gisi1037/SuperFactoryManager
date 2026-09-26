package ca.teamdman.sfm.common.net;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import ca.teamdman.sfm.common.item.ItemLabelGun;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketSetLabel implements IMessage {

    private String label;

    public PacketSetLabel() {}

    public PacketSetLabel(String label) {
        this.label = label != null ? label : "default";
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        label = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, label);
    }

    public static class Handler implements IMessageHandler<PacketSetLabel, IMessage> {

        @Override
        public IMessage onMessage(PacketSetLabel message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player != null) {
                ItemStack held = player.getHeldItem();
                if (held != null && held.getItem() instanceof ItemLabelGun) {
                    ItemLabelGun.setActiveLabel(held, message.label);
                }
            }
            return null;
        }
    }
}
