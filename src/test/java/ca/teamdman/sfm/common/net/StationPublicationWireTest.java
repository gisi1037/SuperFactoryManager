package ca.teamdman.sfm.common.net;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.program.project.ProjectDraft;
import ca.teamdman.sfm.common.program.project.ProjectPage;
import ca.teamdman.sfm.common.program.project.ProjectValidation;
import ca.teamdman.sfm.common.program.project.ProjectWorkspace;
import ca.teamdman.sfm.common.program.project.PublicationPage;
import ca.teamdman.sfm.common.program.project.PublicationReceipt;
import ca.teamdman.sfm.common.program.project.PublishedProgram;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

class StationPublicationWireTest {

    private static final UUID OWNER = new UUID(1, 2), PROJECT = new UUID(3, 4);
    private static final String DIGEST = new PublishedProgram("EVERY 20 TICKS DO END").digest();

    @Test
    void publicationRequestsContainOnlySavedProjectAndRevisionAndRejectClientSource() {
        for (PacketStationRequest.Action action : new PacketStationRequest.Action[] {
            PacketStationRequest.Action.PUBLISH, PacketStationRequest.Action.LIST_PUBLICATIONS }) {
            PacketStationRequest request = request(action);
            request.expectedRevision = Long.MAX_VALUE;
            if (action == PacketStationRequest.Action.LIST_PUBLICATIONS)
                request.offset = ProjectWorkspace.MAX_PUBLICATIONS_PER_PROJECT;
            PacketStationRequest copy = decodeRequest(encode(request));
            assertEquals(PROJECT, copy.projectId);
            assertEquals(Long.MAX_VALUE, copy.expectedRevision);
            assertEquals(request.offset, copy.offset);
            assertEquals("", copy.source);
            assertEquals("", copy.name);
            for (String value : new String[] { "changed", "\u00e9", "\ud800" }) {
                request.source = value;
                assertThrows(IllegalArgumentException.class, request::validate);
                request.source = "";
                request.name = value;
                assertThrows(IllegalArgumentException.class, request::validate);
                request.name = "";
            }
            request.projectId = null;
            assertThrows(IllegalArgumentException.class, request::validate);
            request.projectId = PROJECT;
            request.expectedRevision = -1;
            assertThrows(IllegalArgumentException.class, request::validate);
        }
        PacketStationRequest publish = request(PacketStationRequest.Action.PUBLISH);
        publish.offset = 1;
        assertThrows(IllegalArgumentException.class, publish::validate);
        PacketStationRequest list = request(PacketStationRequest.Action.LIST_PUBLICATIONS);
        list.offset = ProjectWorkspace.MAX_PUBLICATIONS_PER_PROJECT + 1;
        assertThrows(IllegalArgumentException.class, list::validate);
        list.offset = -1;
        assertThrows(IllegalArgumentException.class, list::validate);
    }

    @Test
    void decoderRejectsSourceBearingSavePacketRetaggedAsPublicationAction() {
        PacketStationRequest save = request(PacketStationRequest.Action.SAVE);
        save.name = "unsaved name";
        save.source = "unsaved source";
        for (PacketStationRequest.Action action : new PacketStationRequest.Action[] {
            PacketStationRequest.Action.PUBLISH, PacketStationRequest.Action.LIST_PUBLICATIONS }) {
            byte[] bytes = encode(save);
            bytes[44] = (byte) action.ordinal();
            assertThrows(IllegalArgumentException.class, () -> decodeRequest(bytes));
        }
    }

    @Test
    void acceptedRepliesRequireExactlyOnePayloadMatchingTheirActionAndRejectedRepliesCarryNone() {
        for (PacketStationRequest.Action action : PacketStationRequest.Action.values()) {
            int expected = action == PacketStationRequest.Action.LIST ? 1
                : action == PacketStationRequest.Action.VALIDATE ? 2
                    : action == PacketStationRequest.Action.PUBLISH ? 3
                        : action == PacketStationRequest.Action.LIST_PUBLICATIONS ? 4 : 0;
            for (int payload = -1; payload < 5; payload++) {
                PacketStationReply reply = reply(action, true);
                addPayload(reply, payload);
                byte[] encoded = encode(reply);
                if (payload == expected) assertTrue(decodeReply(encoded).accepted);
                else assertThrows(IllegalArgumentException.class, () -> decodeReply(encoded));
                reply.accepted = false;
                byte[] rejected = encode(reply);
                if (payload == -1) assertFalse(decodeReply(rejected).accepted);
                else assertThrows(IllegalArgumentException.class, () -> decodeReply(rejected));
            }
        }
        PacketStationReply mixed = reply(PacketStationRequest.Action.PUBLISH, true);
        addPayload(mixed, 0);
        addPayload(mixed, 3);
        assertThrows(IllegalArgumentException.class, () -> decodeReply(encode(mixed)));
    }

    @Test
    void receiptWirePreservesExactOwnerProjectRevisionDigestAndUnicodeDisplayNameWithoutSource() {
        PacketStationReply reply = reply(PacketStationRequest.Action.PUBLISH, true);
        reply.publication = new PublicationReceipt(OWNER, PROJECT, Long.MAX_VALUE, DIGEST, "Saved \u00e9 \uD83D\uDE80");
        PacketStationReply decoded = decodeReply(encode(reply));
        assertEquals(OWNER, decoded.publication.owner);
        assertEquals(PROJECT, decoded.publication.project);
        assertEquals(Long.MAX_VALUE, decoded.publication.revision);
        assertEquals(DIGEST, decoded.publication.digest);
        assertEquals(reply.publication.name, decoded.publication.name);
        assertNull(decoded.draft);
        assertNull(decoded.page);
        assertNull(decoded.validation);
        assertNull(decoded.publications);
    }

    @Test
    void publicationPageChecksTotalsOffsetsCountAndConsistentUniqueReceiptIdentities() {
        PublicationReceipt first = receipt(OWNER, PROJECT, 1), second = receipt(OWNER, PROJECT, 0);
        PacketStationReply valid = reply(PacketStationRequest.Action.LIST_PUBLICATIONS, true);
        valid.publications = new PublicationPage(Arrays.asList(first, second), 2, 0);
        PublicationPage page = decodeReply(encode(valid)).publications;
        assertEquals(2, page.total);
        assertEquals(0, page.offset);
        assertEquals(1, page.entries.get(0).revision);
        assertEquals(0, page.entries.get(1).revision);
        assertThrows(UnsupportedOperationException.class, () -> page.entries.clear());
        for (PublicationPage invalid : new PublicationPage[] { new PublicationPage(Collections.emptyList(), -1, 0),
            new PublicationPage(Collections.emptyList(), ProjectWorkspace.MAX_PUBLICATIONS_PER_PROJECT + 1, 0),
            new PublicationPage(Collections.emptyList(), 1, -1), new PublicationPage(Collections.emptyList(), 1, 2),
            new PublicationPage(Arrays.asList(first, second), 1, 0),
            new PublicationPage(Arrays.asList(first, second), 2, 1),
            new PublicationPage(
                Collections.nCopies(ProjectWorkspace.PAGE_SIZE + 1, first),
                ProjectWorkspace.PAGE_SIZE + 1,
                0),
            new PublicationPage(Arrays.asList(first, first), 2, 0),
            new PublicationPage(Arrays.asList(first, receipt(UUID.randomUUID(), PROJECT, 0)), 2, 0),
            new PublicationPage(Arrays.asList(first, receipt(OWNER, UUID.randomUUID(), 0)), 2, 0) }) {
            PacketStationReply malformed = reply(PacketStationRequest.Action.LIST_PUBLICATIONS, true);
            malformed.publications = invalid;
            assertThrows(IllegalArgumentException.class, () -> decodeReply(encode(malformed)));
        }
    }

    @Test
    void fullPublicationPageAndEmptyTerminalPageStayWithinPacketEnvelope() {
        java.util.List<PublicationReceipt> entries = new java.util.ArrayList<>();
        String maxName = String.join("", Collections.nCopies(ProjectWorkspace.MAX_NAME_BYTES, "x"));
        for (int i = 0; i < ProjectWorkspace.PAGE_SIZE; i++)
            entries.add(new PublicationReceipt(OWNER, PROJECT, Long.MAX_VALUE - i, DIGEST, maxName));
        PacketStationReply reply = reply(PacketStationRequest.Action.LIST_PUBLICATIONS, true);
        reply.publications = new PublicationPage(entries, ProjectWorkspace.MAX_PUBLICATIONS_PER_PROJECT, 0);
        byte[] encoded = encode(reply);
        assertTrue(encoded.length < PacketStationRequest.MAX_PACKET_BYTES);
        assertEquals(ProjectWorkspace.PAGE_SIZE, decodeReply(encoded).publications.entries.size());
        reply.publications = new PublicationPage(
            Collections.emptyList(),
            ProjectWorkspace.MAX_PUBLICATIONS_PER_PROJECT,
            ProjectWorkspace.MAX_PUBLICATIONS_PER_PROJECT);
        assertTrue(decodeReply(encode(reply)).publications.entries.isEmpty());
    }

    @Test
    void publicationDecodersRejectOldVersionUnknownActionTrailingOversizedAndEveryTruncatedPacket() {
        PacketStationRequest request = request(PacketStationRequest.Action.PUBLISH);
        PacketStationReply reply = reply(PacketStationRequest.Action.PUBLISH, true);
        reply.publication = receipt(OWNER, PROJECT, 0);
        byte[] requestBytes = encode(request), replyBytes = encode(reply);
        for (boolean response : new boolean[] { false, true }) {
            byte[] valid = response ? replyBytes : requestBytes;
            byte[] oldVersion = valid.clone(), action = valid.clone();
            oldVersion[3]--;
            action[44] = (byte) 255;
            for (byte[] malformed : new byte[][] { oldVersion, action, Arrays.copyOf(valid, valid.length + 1),
                new byte[PacketStationRequest.MAX_PACKET_BYTES + 1] }) assertThrows(RuntimeException.class, () -> {
                    if (response) decodeReply(malformed);
                    else decodeRequest(malformed);
                });
            for (int length = 0; length < valid.length; length++) {
                byte[] truncated = Arrays.copyOf(valid, length);
                assertThrows(RuntimeException.class, () -> {
                    if (response) decodeReply(truncated);
                    else decodeRequest(truncated);
                });
            }
        }
    }

    @Test
    void receiptDecodeRejectsNegativeRevisionNoncanonicalDigestAndInvalidName() {
        PacketStationReply reply = reply(PacketStationRequest.Action.PUBLISH, true);
        reply.publication = receipt(OWNER, PROJECT, 0);
        byte[] original = encode(reply);
        int revisionIndex, digestIndex, nameIndex;
        ByteBuf cursor = Unpooled.wrappedBuffer(original);
        try {
            cursor.readerIndex(46); // Fixed identity, action and accepted fields.
            ProgramEditWire.string(cursor, 4096);
            assertFalse(cursor.readBoolean()); // draft
            assertFalse(cursor.readBoolean()); // page
            assertFalse(cursor.readBoolean()); // validation
            assertTrue(cursor.readBoolean()); // publication
            cursor.skipBytes(32); // owner and project UUIDs
            revisionIndex = cursor.readerIndex();
            cursor.skipBytes(8);
            int digestLength = ByteBufUtils.readVarInt(cursor, 3);
            digestIndex = cursor.readerIndex();
            cursor.skipBytes(digestLength);
            ByteBufUtils.readVarInt(cursor, 3);
            nameIndex = cursor.readerIndex();
        } finally {
            cursor.release();
        }
        byte[] negativeRevision = original.clone(), digest = original.clone(), invalidUtf8 = original.clone(),
            controlName = original.clone();
        Arrays.fill(negativeRevision, revisionIndex, revisionIndex + 8, (byte) 0xff);
        digest[digestIndex] = 'G';
        invalidUtf8[nameIndex] = (byte) 0xff;
        controlName[nameIndex] = '\n';
        for (byte[] malformed : new byte[][] { negativeRevision, digest, invalidUtf8, controlName })
            assertThrows(IllegalArgumentException.class, () -> decodeReply(malformed));
    }

    private static PacketStationRequest request(PacketStationRequest.Action action) {
        PacketStationRequest request = new PacketStationRequest();
        request.action = action;
        request.sessionId = new UUID(5, 6);
        request.projectId = PROJECT;
        request.windowId = 9;
        request.requestId = 1037;
        return request;
    }

    private static PacketStationReply reply(PacketStationRequest.Action action, boolean accepted) {
        return new PacketStationReply(request(action), accepted, "");
    }

    private static PublicationReceipt receipt(UUID owner, UUID project, long revision) {
        return new PublicationReceipt(owner, project, revision, DIGEST, "Saved");
    }

    private static void addPayload(PacketStationReply reply, int payload) {
        switch (payload) {
            case 0:
                reply.draft = new ProjectDraft(PROJECT, OWNER, 0, "Saved", "source");
                break;
            case 1:
                reply.page = new ProjectPage(
                    Collections.singletonList(new ProjectPage.Entry(PROJECT, 0, "Saved")),
                    1,
                    0);
                break;
            case 2:
                reply.validation = new ProjectValidation(PROJECT, 0, true, "Valid");
                break;
            case 3:
                reply.publication = receipt(OWNER, PROJECT, 0);
                break;
            case 4:
                reply.publications = new PublicationPage(Collections.singletonList(receipt(OWNER, PROJECT, 0)), 1, 0);
                break;
            default:
                break;
        }
    }

    private static byte[] encode(IMessage message) {
        ByteBuf buffer = Unpooled.buffer();
        try {
            message.toBytes(buffer);
            byte[] bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            return bytes;
        } finally {
            buffer.release();
        }
    }

    private static PacketStationRequest decodeRequest(byte[] bytes) {
        ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
        try {
            PacketStationRequest request = new PacketStationRequest();
            request.fromBytes(buffer);
            return request;
        } finally {
            buffer.release();
        }
    }

    private static PacketStationReply decodeReply(byte[] bytes) {
        ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
        try {
            PacketStationReply reply = new PacketStationReply();
            reply.fromBytes(buffer);
            return reply;
        } finally {
            buffer.release();
        }
    }
}
