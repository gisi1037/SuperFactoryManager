package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.SuperFactoryManager;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Reply-only held-stack authority; clients cannot mint a server binding. */
public final class PacketLabelGunSession implements IMessage {

    public LabelGunAccess.Token token;

    public PacketLabelGunSession() {}

    public PacketLabelGunSession(LabelGunAccess.Token token) {
        this.token = token;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        if (buf.readableBytes() != 24) throw new IllegalArgumentException("Invalid label gun session frame");
        token = LabelGunAccess.Token.read(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        token.write(buf);
    }

    public static final class Handler implements IMessageHandler<PacketLabelGunSession, IMessage> {

        @Override
        public IMessage onMessage(PacketLabelGunSession message, MessageContext context) {
            SuperFactoryManager.proxy.receiveLabelGunSession(message);
            return null;
        }
    }
}
