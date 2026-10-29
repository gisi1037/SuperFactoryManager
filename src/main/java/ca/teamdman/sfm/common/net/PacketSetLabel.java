package ca.teamdman.sfm.common.net;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

import ca.teamdman.sfm.common.item.ItemLabelGun;
import ca.teamdman.sfm.common.util.BoundedUtf8;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class PacketSetLabel implements IMessage {

    public static final int MAX_LABEL_CHARACTERS = 256, MAX_LABEL_BYTES = 768;
    private static final int MAGIC = 0x53464c53;
    private String label;
    private LabelGunAccess.Token token;
    private boolean valid;

    public PacketSetLabel() {}

    public PacketSetLabel(String label) {
        this(label, null);
    }

    public PacketSetLabel(String label, LabelGunAccess.Token token) {
        this.label = label == null ? "default" : label;
        validate(this.label);
        this.token = token;
        valid = token != null;
    }

    private static void validate(String text) {
        if (text.length() > MAX_LABEL_CHARACTERS) throw new IllegalArgumentException("Label exceeds 256 characters");
        BoundedUtf8.encode(text, MAX_LABEL_BYTES);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        valid = false;
        token = null;
        try {
            if (buf.readableBytes() > 800 || buf.readInt() != MAGIC) return;
            token = LabelGunAccess.Token.read(buf);
            label = ProgramEditWire.string(buf, MAX_LABEL_BYTES);
            validate(label);
            valid = !buf.isReadable();
        } catch (RuntimeException malformed) {
            token = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(MAGIC);
        if (token == null) return;
        token.write(buf);
        ProgramEditWire.string(buf, label, MAX_LABEL_BYTES);
    }

    public static class Handler implements IMessageHandler<PacketSetLabel, IMessage> {

        @Override
        public IMessage onMessage(PacketSetLabel message, MessageContext context) {
            if (!message.valid) return null;
            EntityPlayerMP player = context.getServerHandler().playerEntity;
            ServerUiTasks.enqueue(() -> {
                ItemStack held = LabelGunAccess.authorized(player, message.token);
                if (held == null) return;
                ItemLabelGun.setActiveLabel(held, message.label);
                player.inventoryContainer.detectAndSendChanges();
            });
            return null;
        }
    }
}
