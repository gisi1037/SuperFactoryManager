package ca.teamdman.sfm.common.net;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.util.BoundedUtf8;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.recipe.RecipeCatalog;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Bounded display summaries. Shared token headers avoid repeating large strings for every candidate. */
public final class PacketStationRecipeReply implements IMessage {

    private static final int VERSION = 0x53545202;
    public static final int MAX_WIRE_BYTES = 16000, MAX_ROWS = 32, MAX_DETAIL_BYTES = 4096;
    public int x, y, z, windowId;
    public UUID sessionId;
    public long requestId;
    public PacketStationRecipeRequest.Action action;
    public boolean accepted, coverageComplete;
    public RecipeCatalog.Status status;
    public String message = "", detail = "";
    public List<Entry> entries = Collections.emptyList();

    public static final class Entry {

        public final RecipeCatalog.Token reference;
        public final String title;

        public Entry(RecipeCatalog.Token reference, String title) {
            if (reference == null || title == null || title.length() > 128)
                throw new IllegalArgumentException("Invalid recipe summary");
            this.reference = reference;
            this.title = title;
        }
    }

    public PacketStationRecipeReply() {}

    private PacketStationRecipeReply(PacketStationRecipeRequest request, boolean accepted, RecipeCatalog.Status status,
        boolean complete, String message) {
        x = request.x;
        y = request.y;
        z = request.z;
        windowId = request.windowId;
        sessionId = request.sessionId;
        requestId = request.requestId;
        action = request.action;
        this.accepted = accepted;
        this.status = status;
        coverageComplete = complete;
        this.message = message;
    }

    public static PacketStationRecipeReply error(PacketStationRecipeRequest request, RecipeCatalog.Status status,
        String message) {
        return new PacketStationRecipeReply(request, false, status, false, message);
    }

    static PacketStationRecipeReply from(PacketStationRecipeRequest request, RecipeCatalog.Result result,
        QueryBudget budget) {
        if (result.recipes.size() > MAX_ROWS) return error(
            request,
            RecipeCatalog.Status.LIMIT_EXCEEDED,
            "Too many UI candidates; use SFML to inspect the full result");
        if (request.action == PacketStationRecipeRequest.Action.SELECT && result.recipes.size() > 1)
            throw new IllegalArgumentException("Invalid selected result");
        budget.chargeAllocation(result.recipes.size() * 3 + 2);
        budget.chargeSlots(result.recipes.size() * 160);
        PacketStationRecipeReply reply = new PacketStationRecipeReply(
            request,
            true,
            result.status,
            result.coverageComplete,
            "Server candidates, not an import of NEI ingredients. Choose explicitly; no feasibility guarantee.");
        List<Entry> rows = new ArrayList<>(result.recipes.size());
        for (int i = 0; i < result.recipes.size(); i++) {
            RecipeDescriptor recipe = result.recipes.get(i);
            rows.add(
                new Entry(
                    result.reference(i),
                    "EU/t " + recipe.eut
                        + " | "
                        + recipe.duration
                        + " ticks | items "
                        + recipe.itemInputs.size()
                        + "/"
                        + recipe.itemOutputs.size()
                        + " | fluids "
                        + recipe.fluidInputs.size()
                        + "/"
                        + recipe.fluidOutputs.size()
                        + " | unsupported "
                        + recipe.unsupportedRequirements.size()));
        }
        reply.entries = Collections.unmodifiableList(rows);
        if (request.action == PacketStationRecipeRequest.Action.SELECT && !result.recipes.isEmpty())
            reply.detail = describe(result.recipes.get(0), budget);
        reply.validate();
        return reply;
    }

    private static String shortId(String id) {
        return id.length() > 64 ? id.substring(0, 61) + "..." : id;
    }

    private static String describe(RecipeDescriptor recipe, QueryBudget budget) {
        // Reserve the bounded preview before constructing it. Native requirements remain in the immutable catalog.
        budget.chargeSlots(MAX_DETAIL_BYTES);
        budget.chargeAllocation(64);
        StringBuilder out = new StringBuilder(512);
        out.append("Preview only; IDs may be shortened. Chances /10000, fluids in mB.\n");
        for (boolean input : new boolean[] { true, false }) {
            List<RecipeDescriptor.ItemPart> items = input ? recipe.itemInputs : recipe.itemOutputs;
            List<RecipeDescriptor.FluidPart> fluids = input ? recipe.fluidInputs : recipe.fluidOutputs;
            out.append(input ? "Inputs: " : "Outputs: ")
                .append(items.size())
                .append(" item entries; ")
                .append(fluids.size())
                .append(" fluid entries\n");
            for (int i = 0; i < Math.min(2, items.size()); i++) {
                RecipeDescriptor.ItemPart part = items.get(i);
                out.append("slot ")
                    .append(part.slot)
                    .append(' ')
                    .append(shortId(part.item.getRegistryId()))
                    .append(':')
                    .append(part.item.getMetadata())
                    .append(" x")
                    .append(part.amount)
                    .append(" chance ")
                    .append(part.chance)
                    .append(' ')
                    .append(part.matching)
                    .append(" nbt=")
                    .append(part.nbtSensitive)
                    .append('\n');
            }
            for (int i = 0; i < Math.min(2, fluids.size()); i++) {
                RecipeDescriptor.FluidPart part = fluids.get(i);
                out.append("slot ")
                    .append(part.slot)
                    .append(' ')
                    .append(shortId(part.fluid.getId()))
                    .append(' ')
                    .append(part.amount)
                    .append("mB chance ")
                    .append(part.chance)
                    .append('\n');
            }
        }
        out.append(
            "At most 2 entries per group shown. Amount zero is a catalyst.\nFull alternatives/ore/NBT policies: use recipe(reference).\nUnsupported requirements: ")
            .append(recipe.unsupportedRequirements.size());
        String text = out.toString();
        BoundedUtf8.encode(text, MAX_DETAIL_BYTES);
        return text;
    }

    public boolean matches(PacketStationRecipeRequest request) {
        return request != null && x == request.x
            && y == request.y
            && z == request.z
            && windowId == request.windowId
            && sessionId != null
            && sessionId.equals(request.sessionId)
            && requestId == request.requestId
            && action == request.action;
    }

    private void validate() {
        if (sessionId == null || requestId <= 0
            || windowId < 1
            || windowId > 255
            || action == null
            || status == null
            || message == null
            || message.length() > 256
            || detail == null
            || entries == null
            || entries.size() > MAX_ROWS
            || !accepted && !entries.isEmpty()
            || action == PacketStationRecipeRequest.Action.SELECT && entries.size() > 1
            || action == PacketStationRecipeRequest.Action.SEARCH && !detail.isEmpty())
            throw new IllegalArgumentException("Invalid recipe reply");
        BoundedUtf8.encode(message, 768);
        BoundedUtf8.encode(detail, MAX_DETAIL_BYTES);
        if (!entries.isEmpty()) {
            RecipeCatalog.Token header = entries.get(0).reference;
            for (Entry entry : entries) {
                BoundedUtf8.encode(entry.title, 128);
                if (!header.session.equals(entry.reference.session) || header.revision != entry.reference.revision
                    || !header.adapter.equals(entry.reference.adapter)
                    || !header.map.equals(entry.reference.map))
                    throw new IllegalArgumentException("Mixed recipe snapshots");
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        validate();
        int start = buffer.writerIndex();
        buffer.writeInt(VERSION);
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeInt(windowId);
        ProgramEditWire.uuid(buffer, sessionId);
        buffer.writeLong(requestId);
        buffer.writeByte(action.ordinal());
        buffer.writeBoolean(accepted);
        buffer.writeByte(status.ordinal());
        buffer.writeBoolean(coverageComplete);
        ProgramEditWire.string(buffer, message, 768);
        ProgramEditWire.string(buffer, detail, MAX_DETAIL_BYTES);
        buffer.writeByte(entries.size());
        if (!entries.isEmpty()) {
            RecipeCatalog.Token header = entries.get(0).reference;
            ProgramEditWire.uuid(buffer, header.session);
            buffer.writeLong(header.revision);
            ProgramEditWire.string(buffer, header.adapter, 384);
            ProgramEditWire.string(buffer, header.map, 384);
            for (Entry entry : entries) {
                ProgramEditWire.string(buffer, entry.reference.fingerprint, 64);
                ProgramEditWire.string(buffer, entry.title, 128);
            }
        }
        if (buffer.writerIndex() - start > MAX_WIRE_BYTES)
            throw new IllegalArgumentException("Recipe reply frame limit");
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        if (buffer.readableBytes() > MAX_WIRE_BYTES || buffer.readInt() != VERSION)
            throw new IllegalArgumentException("Invalid recipe reply frame");
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        windowId = buffer.readInt();
        sessionId = ProgramEditWire.uuid(buffer);
        requestId = buffer.readLong();
        int operation = buffer.readUnsignedByte();
        if (operation >= PacketStationRecipeRequest.Action.values().length)
            throw new IllegalArgumentException("Invalid recipe action");
        action = PacketStationRecipeRequest.Action.values()[operation];
        accepted = buffer.readBoolean();
        int ordinal = buffer.readUnsignedByte();
        if (ordinal >= RecipeCatalog.Status.values().length)
            throw new IllegalArgumentException("Invalid recipe status");
        status = RecipeCatalog.Status.values()[ordinal];
        coverageComplete = buffer.readBoolean();
        message = ProgramEditWire.string(buffer, 768);
        detail = ProgramEditWire.string(buffer, MAX_DETAIL_BYTES);
        int count = buffer.readUnsignedByte();
        if (count > MAX_ROWS) throw new IllegalArgumentException("Recipe row limit");
        List<Entry> rows = new ArrayList<>(count);
        if (count > 0) {
            UUID catalogSession = ProgramEditWire.uuid(buffer);
            long revision = buffer.readLong();
            String adapter = ProgramEditWire.string(buffer, 384), map = ProgramEditWire.string(buffer, 384);
            for (int i = 0; i < count; i++) rows.add(
                new Entry(
                    new RecipeCatalog.Token(catalogSession, revision, adapter, map, ProgramEditWire.string(buffer, 64)),
                    ProgramEditWire.string(buffer, 128)));
        }
        entries = Collections.unmodifiableList(rows);
        if (buffer.isReadable()) throw new IllegalArgumentException("Trailing recipe reply data");
        validate();
    }

    public static final class Handler implements IMessageHandler<PacketStationRecipeReply, IMessage> {

        @Override
        public IMessage onMessage(PacketStationRecipeReply reply, MessageContext context) {
            SuperFactoryManager.proxy.receiveStationRecipeReply(reply);
            return null;
        }
    }
}
