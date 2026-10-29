package ca.teamdman.sfm.common.net;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.program.project.ProjectDraft;
import ca.teamdman.sfm.common.program.project.ProjectPage;
import ca.teamdman.sfm.common.program.project.ProjectValidation;
import ca.teamdman.sfm.common.program.project.ProjectWorkspace;
import ca.teamdman.sfm.common.program.project.PublicationPage;
import ca.teamdman.sfm.common.program.project.PublicationReceipt;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** One correlated result. Catalog/source/validation are separate, bounded payloads. */
public final class PacketStationReply implements IMessage {

    private static final int VERSION = 0x53545004;
    public int x, y, z, windowId;
    public UUID sessionId;
    public long requestId;
    public PacketStationRequest.Action action = PacketStationRequest.Action.LIST;
    public boolean accepted;
    public String message = "";
    public ProjectDraft draft;
    public ProjectPage page;
    public ProjectValidation validation;
    public PublicationReceipt publication;
    public PublicationPage publications;

    public PacketStationReply() {}

    public PacketStationReply(PacketStationRequest request, boolean accepted, String message) {
        x = request.x;
        y = request.y;
        z = request.z;
        windowId = request.windowId;
        sessionId = request.sessionId;
        requestId = request.requestId;
        action = request.action;
        this.accepted = accepted;
        this.message = trim(message == null ? "Project request rejected" : message, 1024);
    }

    private static String trim(String value, int limit) {
        if (value.length() <= limit) return value;
        if (Character.isHighSurrogate(value.charAt(limit - 1))) limit--;
        return value.substring(0, limit);
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(VERSION);
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeInt(windowId);
        ProgramEditWire.uuid(buffer, sessionId);
        buffer.writeLong(requestId);
        buffer.writeByte(action.ordinal());
        buffer.writeBoolean(accepted);
        ProgramEditWire.string(buffer, message, 4096);
        buffer.writeBoolean(draft != null);
        if (draft != null) {
            ProgramEditWire.uuid(buffer, draft.id);
            ProgramEditWire.uuid(buffer, draft.owner);
            buffer.writeLong(draft.revision);
            ProgramEditWire.string(buffer, draft.name, ProjectWorkspace.MAX_NAME_BYTES);
            ProgramEditWire.string(buffer, draft.source, ProjectWorkspace.MAX_SOURCE_BYTES);
        }
        buffer.writeBoolean(page != null);
        if (page != null) {
            buffer.writeInt(page.total);
            buffer.writeInt(page.offset);
            buffer.writeByte(page.entries.size());
            for (ProjectPage.Entry entry : page.entries) {
                ProgramEditWire.uuid(buffer, entry.id);
                buffer.writeLong(entry.revision);
                ProgramEditWire.string(buffer, entry.name, ProjectWorkspace.MAX_NAME_BYTES);
            }
        }
        buffer.writeBoolean(validation != null);
        if (validation != null) {
            ProgramEditWire.uuid(buffer, validation.id);
            buffer.writeLong(validation.revision);
            buffer.writeBoolean(validation.valid);
            ProgramEditWire.string(buffer, validation.message, 4096);
        }
        buffer.writeBoolean(publication != null);
        if (publication != null) writeReceipt(buffer, publication);
        buffer.writeBoolean(publications != null);
        if (publications != null) {
            buffer.writeInt(publications.total);
            buffer.writeInt(publications.offset);
            buffer.writeByte(publications.entries.size());
            for (PublicationReceipt entry : publications.entries) writeReceipt(buffer, entry);
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        if (buffer.readableBytes() > PacketStationRequest.MAX_PACKET_BYTES || buffer.readInt() != VERSION)
            throw new IllegalArgumentException("Unsupported or oversized station reply");
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        windowId = buffer.readInt();
        sessionId = ProgramEditWire.uuid(buffer);
        requestId = buffer.readLong();
        int index = buffer.readUnsignedByte();
        if (index >= PacketStationRequest.Action.values().length || requestId <= 0 || windowId < 1 || windowId > 255)
            throw new IllegalArgumentException("Invalid station reply identity");
        action = PacketStationRequest.Action.values()[index];
        accepted = buffer.readBoolean();
        message = ProgramEditWire.string(buffer, 4096);
        draft = null;
        page = null;
        validation = null;
        publication = null;
        publications = null;
        if (buffer.readBoolean()) {
            UUID id = ProgramEditWire.uuid(buffer), owner = ProgramEditWire.uuid(buffer);
            long revision = revision(buffer);
            draft = new ProjectDraft(
                id,
                owner,
                revision,
                ProgramEditWire.string(buffer, ProjectWorkspace.MAX_NAME_BYTES),
                ProgramEditWire.string(buffer, ProjectWorkspace.MAX_SOURCE_BYTES));
        }
        if (buffer.readBoolean()) {
            int total = buffer.readInt(), offset = buffer.readInt(), count = buffer.readUnsignedByte();
            if (total < 0 || total > ProjectWorkspace.MAX_PROJECTS_PER_OWNER
                || offset < 0
                || offset > total
                || count > ProjectWorkspace.PAGE_SIZE
                || count > total - offset) throw new IllegalArgumentException("Invalid project catalog page");
            List<ProjectPage.Entry> entries = new ArrayList<>(count);
            for (int i = 0; i < count; i++) entries.add(
                new ProjectPage.Entry(
                    ProgramEditWire.uuid(buffer),
                    revision(buffer),
                    ProgramEditWire.string(buffer, ProjectWorkspace.MAX_NAME_BYTES)));
            page = new ProjectPage(entries, total, offset);
        }
        if (buffer.readBoolean()) validation = new ProjectValidation(
            ProgramEditWire.uuid(buffer),
            revision(buffer),
            buffer.readBoolean(),
            ProgramEditWire.string(buffer, 4096));
        if (buffer.readBoolean()) publication = readReceipt(buffer);
        if (buffer.readBoolean()) {
            int total = buffer.readInt(), offset = buffer.readInt(), count = buffer.readUnsignedByte();
            if (total < 0 || total > ProjectWorkspace.MAX_PUBLICATIONS_PER_PROJECT
                || offset < 0
                || offset > total
                || count > ProjectWorkspace.PAGE_SIZE
                || count > total - offset) throw new IllegalArgumentException("Invalid publication page");
            List<PublicationReceipt> entries = new ArrayList<>(count);
            java.util.Set<Long> revisions = new java.util.HashSet<>();
            for (int i = 0; i < count; i++) {
                PublicationReceipt entry = readReceipt(buffer);
                if (!revisions.add(entry.revision) || !entries.isEmpty()
                    && (!entries.get(0).owner.equals(entry.owner) || !entries.get(0).project.equals(entry.project)))
                    throw new IllegalArgumentException("Mixed or duplicate publication records");
                entries.add(entry);
            }
            publications = new PublicationPage(entries, total, offset);
        }
        int payloads = (draft == null ? 0 : 1) + (page == null ? 0 : 1)
            + (validation == null ? 0 : 1)
            + (publication == null ? 0 : 1)
            + (publications == null ? 0 : 1);
        boolean expectedPayload;
        switch (action) {
            case LIST:
                expectedPayload = page != null;
                break;
            case VALIDATE:
                expectedPayload = validation != null;
                break;
            case PUBLISH:
                expectedPayload = publication != null;
                break;
            case LIST_PUBLICATIONS:
                expectedPayload = publications != null;
                break;
            default:
                expectedPayload = draft != null;
        }
        if (buffer.isReadable() || accepted && (payloads != 1 || !expectedPayload) || !accepted && payloads != 0)
            throw new IllegalArgumentException("Invalid station reply payload");
    }

    private static void writeReceipt(ByteBuf buffer, PublicationReceipt receipt) {
        ProgramEditWire.uuid(buffer, receipt.owner);
        ProgramEditWire.uuid(buffer, receipt.project);
        buffer.writeLong(receipt.revision);
        ProgramEditWire.string(buffer, receipt.digest, 64);
        ProgramEditWire.string(buffer, receipt.name, ProjectWorkspace.MAX_NAME_BYTES);
    }

    private static PublicationReceipt readReceipt(ByteBuf buffer) {
        return new PublicationReceipt(
            ProgramEditWire.uuid(buffer),
            ProgramEditWire.uuid(buffer),
            revision(buffer),
            ProgramEditWire.string(buffer, 64),
            ProgramEditWire.string(buffer, ProjectWorkspace.MAX_NAME_BYTES));
    }

    private static long revision(ByteBuf buffer) {
        long revision = buffer.readLong();
        if (revision < 0) throw new IllegalArgumentException("Invalid project revision");
        return revision;
    }

    public static final class Handler implements IMessageHandler<PacketStationReply, IMessage> {

        @Override
        public IMessage onMessage(PacketStationReply packet, MessageContext context) {
            SuperFactoryManager.proxy.receiveStationReply(packet);
            return null;
        }
    }
}
