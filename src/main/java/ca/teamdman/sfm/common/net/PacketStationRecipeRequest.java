package ca.teamdman.sfm.common.net;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.integration.mui2.ProgrammingStationMui2Container;
import ca.teamdman.sfml.data.DataQueries;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.MachineRecipeMaps;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.recipe.RecipeCatalog;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Station inspection only. Client recipe displays supply hints, never requirements or execution authority. */
public final class PacketStationRecipeRequest implements IMessage {

    private static final int VERSION = 0x53545201;
    public static final int MAX_WIRE_BYTES = 1400;

    public enum Action {
        SEARCH,
        SELECT
    }

    public int x, y, z, windowId, metadata;
    public UUID sessionId;
    public long requestId;
    public Action action = Action.SEARCH;
    public String item = "", map = "";
    public RecipeCatalog.Token reference;

    public void validate() {
        if (sessionId == null || requestId <= 0
            || windowId < 1
            || windowId > 255
            || action == null
            || item == null
            || item.length() > 128
            || !MachineRecipeMaps.qualified(map)) throw new IllegalArgumentException("Invalid station recipe query");
        ItemIdentity.of(item, metadata, null, new QueryBudget());
        if ((action == Action.SELECT) != (reference != null) || reference != null && !map.equals(reference.map))
            throw new IllegalArgumentException("Invalid recipe selection");
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        validate();
        buffer.writeInt(VERSION);
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeInt(windowId);
        ProgramEditWire.uuid(buffer, sessionId);
        buffer.writeLong(requestId);
        buffer.writeByte(action.ordinal());
        ProgramEditWire.string(buffer, item, 384);
        buffer.writeInt(metadata);
        ProgramEditWire.string(buffer, map, 128);
        if (reference != null) token(buffer, reference);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        if (buffer.readableBytes() > MAX_WIRE_BYTES || buffer.readInt() != VERSION)
            throw new IllegalArgumentException("Invalid recipe request frame");
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        windowId = buffer.readInt();
        sessionId = ProgramEditWire.uuid(buffer);
        requestId = buffer.readLong();
        int ordinal = buffer.readUnsignedByte();
        if (ordinal >= Action.values().length) throw new IllegalArgumentException("Invalid recipe action");
        action = Action.values()[ordinal];
        item = ProgramEditWire.string(buffer, 384);
        metadata = buffer.readInt();
        map = ProgramEditWire.string(buffer, 128);
        reference = action == Action.SELECT ? token(buffer) : null;
        if (buffer.isReadable()) throw new IllegalArgumentException("Trailing recipe request data");
        validate();
    }

    static void token(ByteBuf buffer, RecipeCatalog.Token token) {
        ProgramEditWire.uuid(buffer, token.session);
        buffer.writeLong(token.revision);
        ProgramEditWire.string(buffer, token.adapter, 384);
        ProgramEditWire.string(buffer, token.map, 384);
        ProgramEditWire.string(buffer, token.fingerprint, 64);
    }

    static RecipeCatalog.Token token(ByteBuf buffer) {
        return new RecipeCatalog.Token(
            ProgramEditWire.uuid(buffer),
            buffer.readLong(),
            ProgramEditWire.string(buffer, 384),
            ProgramEditWire.string(buffer, 384),
            ProgramEditWire.string(buffer, 64));
    }

    public static ProgrammingStationMui2Container authorized(EntityPlayerMP player,
        PacketStationRecipeRequest request) {
        if (!ServerUiTasks.isServerThread() || !PlayerUiAccess.isLive(player)
            || !(player.openContainer instanceof ProgrammingStationMui2Container)) return null;
        ProgrammingStationMui2Container container = (ProgrammingStationMui2Container) player.openContainer;
        return container.windowId == request.windowId && container.sessionId.equals(request.sessionId)
            && container.station != null
            && !container.station.isInvalid()
            && container.station.xCoord == request.x
            && container.station.yCoord == request.y
            && container.station.zCoord == request.z
            && container.station.isUsableBy(player) ? container : null;
    }

    public static PacketStationRecipeReply process(EntityPlayerMP player, PacketStationRecipeRequest request) {
        return process(player, request, new ProgramContext(null, null, null, null, 0, false));
    }

    /** Qualification injects the same common observation interface; production always uses the server catalog. */
    public static PacketStationRecipeReply process(EntityPlayerMP player, PacketStationRecipeRequest request,
        DataQueries queries) {
        if (!ServerUiTasks.isServerThread()) return null;
        request.validate();
        ProgrammingStationMui2Container container = authorized(player, request);
        if (container == null) return null;
        if (request.requestId <= container.lastRecipeRequestId)
            return PacketStationRecipeReply.error(request, RecipeCatalog.Status.STALE, "Stale inspection request");
        container.lastRecipeRequestId = request.requestId;
        QueryBudget budget = new QueryBudget();
        ItemIdentity output = ItemIdentity.of(request.item, request.metadata, null, budget);
        try {
            RecipeCatalog.Result result = request.action == Action.SEARCH ? queries.recipes(output, request.map, budget)
                : queries.recipe(request.reference, budget);
            if (authorized(player, request) != container) return null;
            if (request.action == Action.SELECT && !result.recipes.isEmpty()) {
                RecipeDescriptor selected = result.recipes.get(0);
                boolean matches = false;
                for (RecipeDescriptor.ItemPart part : selected.itemOutputs) {
                    budget.chargeSlot();
                    if (output.matches(part.item, budget)) matches = true;
                }
                if (!selected.map.equals(request.map) || !matches) return PacketStationRecipeReply
                    .error(request, RecipeCatalog.Status.STALE, "Selection does not produce this exact output");
            }
            return PacketStationRecipeReply.from(request, result, budget);
        } catch (IllegalArgumentException limit) {
            return authorized(player, request) == container ? PacketStationRecipeReply.error(
                request,
                RecipeCatalog.Status.LIMIT_EXCEEDED,
                "Inspection work limit; no partial result published") : null;
        }
    }

    public static final class Handler implements IMessageHandler<PacketStationRecipeRequest, IMessage> {

        @Override
        public IMessage onMessage(PacketStationRecipeRequest request, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().playerEntity;
            ServerUiTasks.enqueue(() -> {
                PacketStationRecipeReply reply = process(player, request);
                if (reply != null) SFMNetwork.WRAPPER.sendTo(reply, player);
            });
            return null;
        }
    }
}
