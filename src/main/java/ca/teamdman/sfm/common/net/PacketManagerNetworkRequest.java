package ca.teamdman.sfm.common.net;

import net.minecraft.entity.player.EntityPlayerMP;

import ca.teamdman.sfm.common.gui.ManagerControlToken;
import ca.teamdman.sfm.common.gui.ManagerEditContext;
import ca.teamdman.sfm.common.program.ManagerNetworkBrowser;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BoundedUtf8;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Explicit metadata reads only. Netty decodes bounded values; the server thread resolves the live session. */
public final class PacketManagerNetworkRequest implements IMessage {

    private static final int VERSION = 0x53464e01;
    public static final int MAX_PACKET_BYTES = 384;
    public int x, y, z, cursor, filter;
    public ManagerControlToken token;
    public long requestId, snapshotId;
    public String query = "";

    public PacketManagerNetworkRequest() {}

    public PacketManagerNetworkRequest(int x, int y, int z, ManagerControlToken token, long requestId, long snapshotId,
        int cursor, String query, int filter) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.token = token;
        this.requestId = requestId;
        this.snapshotId = snapshotId;
        this.cursor = cursor;
        this.query = query;
        this.filter = filter;
        validate();
    }

    private void validate() {
        if (token == null || requestId <= 0
            || snapshotId < 0
            || cursor < 0
            || cursor > ManagerNetworkBrowser.MAX_CANDIDATES
            || snapshotId == 0 && cursor != 0
            || filter < ManagerNetworkBrowser.FILTER_ALL
            || filter > ManagerNetworkBrowser.FILTER_FLUIDS
            || query == null
            || query.length() > ManagerNetworkBrowser.MAX_QUERY_CHARS)
            throw new IllegalArgumentException("Invalid network browser request");
        BoundedUtf8.encode(query, ManagerNetworkBrowser.MAX_QUERY_BYTES);
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        validate();
        buffer.writeInt(VERSION);
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        token.write(buffer);
        buffer.writeLong(requestId);
        buffer.writeLong(snapshotId);
        buffer.writeInt(cursor);
        buffer.writeByte(filter);
        ProgramEditWire.string(buffer, query, ManagerNetworkBrowser.MAX_QUERY_BYTES);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        if (buffer.readableBytes() > MAX_PACKET_BYTES || buffer.readInt() != VERSION)
            throw new IllegalArgumentException("Unsupported or oversized network request");
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        token = ManagerControlToken.read(buffer);
        requestId = buffer.readLong();
        snapshotId = buffer.readLong();
        cursor = buffer.readInt();
        filter = buffer.readUnsignedByte();
        query = ProgramEditWire.string(buffer, ManagerNetworkBrowser.MAX_QUERY_BYTES);
        if (buffer.isReadable()) throw new IllegalArgumentException("Trailing network request data");
        validate();
    }

    public static final class Handler implements IMessageHandler<PacketManagerNetworkRequest, IMessage> {

        @Override
        public IMessage onMessage(PacketManagerNetworkRequest packet, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().playerEntity;
            ServerUiTasks.enqueue(() -> read(player, packet));
            return null;
        }
    }

    /** Resolve again after name callbacks: a provider cannot carry an old reply into a replacement window/disk. */
    static void read(EntityPlayerMP player, PacketManagerNetworkRequest request) {
        TileEntityManager manager = ManagerAccess.getLoaded(player, request.x, request.y, request.z, request.token);
        if (manager == null || player.worldObj != manager.getWorldObj()) return;
        ManagerEditContext owner = (ManagerEditContext) player.openContainer;
        PacketManagerNetworkReply reply;
        try {
            ManagerNetworkBrowser.Page page = ManagerNetworkBrowser.read(
                manager,
                owner.getNetworkBrowserSession(),
                request.snapshotId,
                request.cursor,
                request.query,
                request.filter);
            reply = new PacketManagerNetworkReply(request, true, "", page);
        } catch (RuntimeException failure) {
            reply = new PacketManagerNetworkReply(request, false, reason(failure), null);
        }
        if (player.openContainer != owner || player.worldObj != manager.getWorldObj()
            || ManagerAccess.getLoaded(player, request.x, request.y, request.z, request.token) != manager) return;
        SFMNetwork.WRAPPER.sendTo(reply, player);
    }

    private static String reason(RuntimeException failure) {
        // A foreign name provider's message can itself throw or contain unbounded/control text.
        try {
            String text = failure.getMessage();
            if (text == null) return "Network metadata unavailable; refresh the view.";
            StringBuilder safe = new StringBuilder();
            for (int i = 0; i < Math.min(text.length(), 240); i++) {
                char value = text.charAt(i);
                safe.append(
                    value < 32 || value == 127 || value == '\u00a7' || Character.isSurrogate(value) ? '?' : value);
            }
            return safe.toString();
        } catch (RuntimeException unavailable) {
            return "Network metadata unavailable; refresh the view.";
        }
    }
}
