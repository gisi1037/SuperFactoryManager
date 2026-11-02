package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.net.PacketStationRecipeReply;
import ca.teamdman.sfm.common.net.PacketStationRecipeRequest;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.tileentity.TileEntityProgrammingStation;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfm.integration.mui2.ProgrammingStationMui2Container;
import ca.teamdman.sfml.data.DataQueries;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;
import ca.teamdman.sfml.data.recipe.RecipeCatalog;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor;
import cpw.mods.fml.common.gameevent.TickEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

class StationRecipeInspectionTest {

    private static final String MAP = "gt.recipe.macerator", OUTPUT = "recipe_ui:result";
    private Fixture fixture;
    private TileEntityProgrammingStation station;
    private EntityPlayerMP player;
    private ProgrammingStationMui2Container container;
    private boolean connected;
    private long next;
    private Lookup lookup;

    @BeforeEach
    void begin() throws Exception {
        ServerUiTasks.clear();
        ServerUiTasks.INSTANCE.tick(new TickEvent.ServerTickEvent(TickEvent.Phase.START));
        fixture = new Fixture();
        station = new TileEntityProgrammingStation();
        station.setWorldObj(fixture.world);
        station.xCoord = 1;
        station.yCoord = 64;
        station.zCoord = 1;
        fixture.world.tiles.put(new BlockPos(1, 64, 1), station);
        player = allocate(EntityPlayerMP.class);
        player.worldObj = fixture.world;
        connected = true;
        player.playerNetServerHandler = new NetHandlerPlayServer(null, new NetworkManager(false) {

            @Override
            public boolean isChannelOpen() {
                return connected;
            }
        }, player);
        player.posX = 1.5;
        player.posY = 64.5;
        player.posZ = 1.5;
        container = new ProgrammingStationMui2Container(station, UUID.randomUUID());
        container.windowId = 19;
        player.openContainer = container;
        lookup = new Lookup(2);
    }

    @AfterEach
    void end() {
        ServerUiTasks.clear();
    }

    private PacketStationRecipeRequest request() {
        PacketStationRecipeRequest request = new PacketStationRecipeRequest();
        request.x = station.xCoord;
        request.y = station.yCoord;
        request.z = station.zCoord;
        request.windowId = container.windowId;
        request.sessionId = container.sessionId;
        request.requestId = ++next;
        request.item = OUTPUT;
        request.map = MAP;
        return request;
    }

    private PacketStationRecipeReply process(PacketStationRecipeRequest request) {
        return PacketStationRecipeRequest.process(player, request, lookup);
    }

    @Test
    void searchPreservesAmbiguityAndSelectionRevalidatesExactServerRecipeWithoutMutations() {
        PacketStationRecipeReply found = process(request());
        assertTrue(found.accepted);
        assertTrue(found.coverageComplete);
        assertEquals(2, found.entries.size());
        assertEquals("", found.detail, "Search must not select a candidate automatically");
        PacketStationRecipeRequest select = request();
        select.action = PacketStationRecipeRequest.Action.SELECT;
        select.reference = found.entries.get(1).reference;
        PacketStationRecipeReply chosen = process(select);
        assertTrue(chosen.accepted);
        assertEquals(1, chosen.entries.size());
        assertEquals(select.reference.fingerprint, chosen.entries.get(0).reference.fingerprint);
        assertTrue(chosen.detail.contains("recipe_ui:input"));
        assertTrue(chosen.detail.contains("Amount zero is a catalyst"));
        assertNull(station.getProjectId());
        assertEquals(0, count(fixture.source));
        assertEquals(0, count(fixture.dest));
    }

    @Test
    void selectionCannotSubstituteAnotherOutputMapOrExpiredSession() {
        PacketStationRecipeReply found = process(request());
        PacketStationRecipeRequest select = request();
        select.action = PacketStationRecipeRequest.Action.SELECT;
        select.reference = found.entries.get(0).reference;
        select.item = "recipe_ui:other";
        PacketStationRecipeReply wrong = process(select);
        assertFalse(wrong.accepted);
        assertEquals(RecipeCatalog.Status.STALE, wrong.status);
        assertTrue(wrong.entries.isEmpty());
        select = request();
        select.action = PacketStationRecipeRequest.Action.SELECT;
        RecipeCatalog.Token original = found.entries.get(0).reference;
        select.reference = new RecipeCatalog.Token(
            UUID.randomUUID(),
            original.revision,
            original.adapter,
            original.map,
            original.fingerprint);
        assertEquals(RecipeCatalog.Status.STALE, process(select).status);
        select.map = "gt.recipe.metalbender";
        PacketStationRecipeRequest invalid = select;
        assertThrows(IllegalArgumentException.class, () -> process(invalid));
    }

    @Test
    void containerSessionDistanceUnloadedStationAndDisconnectedPlayerAreCheckedBeforeQuery() {
        PacketStationRecipeRequest request = request();
        request.sessionId = UUID.randomUUID();
        assertNull(process(request));
        request = request();
        request.windowId++;
        assertNull(process(request));
        player.posX = 100;
        assertNull(process(request()));
        player.posX = 1.5;
        fixture.world.unloaded.add(new BlockPos(1, 64, 1));
        assertNull(process(request()));
        fixture.world.unloaded.clear();
        connected = false;
        assertNull(process(request()));
        assertEquals(0, lookup.calls);
    }

    @Test
    void replyIsDroppedWhenAQueryCallbackChangesStationAuthorityAndReplayDoesNotQueryAgain() {
        PacketStationRecipeRequest first = request();
        assertNotNull(process(first));
        int calls = lookup.calls;
        assertFalse(process(first).accepted);
        assertEquals(calls, lookup.calls);
        lookup.callback = () -> station.invalidate();
        assertNull(process(request()));
    }

    @Test
    void oversizedCandidateSetHasAnExplicitLimitWithoutPartialOrFalseUniqueResults() {
        lookup = new Lookup(PacketStationRecipeReply.MAX_ROWS + 1);
        PacketStationRecipeReply result = process(request());
        assertFalse(result.accepted);
        assertFalse(result.coverageComplete);
        assertEquals(RecipeCatalog.Status.LIMIT_EXCEEDED, result.status);
        assertTrue(result.entries.isEmpty());
    }

    @Test
    void requestAndReplyRoundTripKeepTokensStationIdentityAndSelectedPreview() {
        PacketStationRecipeRequest request = request();
        ByteBuf requestWire = Unpooled.buffer();
        request.toBytes(requestWire);
        PacketStationRecipeRequest decoded = new PacketStationRecipeRequest();
        decoded.fromBytes(requestWire);
        PacketStationRecipeReply reply = process(decoded);
        ByteBuf wire = Unpooled.buffer();
        reply.toBytes(wire);
        PacketStationRecipeReply received = new PacketStationRecipeReply();
        received.fromBytes(wire);
        assertTrue(received.matches(request));
        assertEquals(2, received.entries.size());
        assertEquals(reply.entries.get(0).reference.fingerprint, received.entries.get(0).reference.fingerprint);
        PacketStationRecipeRequest select = request();
        select.action = PacketStationRecipeRequest.Action.SELECT;
        select.reference = received.entries.get(0).reference;
        ByteBuf selectionWire = Unpooled.buffer();
        select.toBytes(selectionWire);
        decoded = new PacketStationRecipeRequest();
        decoded.fromBytes(selectionWire);
        reply = process(decoded);
        wire = Unpooled.buffer();
        reply.toBytes(wire);
        received = new PacketStationRecipeReply();
        received.fromBytes(wire);
        assertEquals(reply.detail, received.detail);
        assertTrue(received.matches(select));
        requestWire.release();
        wire.release();
        selectionWire.release();
    }

    @Test
    void oversizedFramesStringsCountsAndTrailingBytesAreRejectedBeforeUnboundedAllocation() {
        ByteBuf oversized = Unpooled.buffer()
            .writeZero(PacketStationRecipeRequest.MAX_WIRE_BYTES + 1);
        assertThrows(IllegalArgumentException.class, () -> new PacketStationRecipeRequest().fromBytes(oversized));
        assertEquals(0, oversized.readerIndex());
        oversized.release();
        ByteBuf good = Unpooled.buffer();
        request().toBytes(good);
        ByteBuf invalidString = Unpooled.buffer()
            .writeBytes(good, 0, 45)
            .writeByte(0x81)
            .writeByte(3)
            .writeZero(385);
        assertThrows(IllegalArgumentException.class, () -> new PacketStationRecipeRequest().fromBytes(invalidString));
        invalidString.release();
        ByteBuf trailing = good.copy()
            .writeByte(0);
        assertThrows(IllegalArgumentException.class, () -> new PacketStationRecipeRequest().fromBytes(trailing));
        trailing.release();
        ByteBuf ordinal = good.copy()
            .setByte(44, 255);
        assertThrows(IllegalArgumentException.class, () -> new PacketStationRecipeRequest().fromBytes(ordinal));
        ordinal.release();
        good.release();
        PacketStationRecipeReply empty = PacketStationRecipeReply.error(request(), RecipeCatalog.Status.STALE, "Stale");
        ByteBuf count = Unpooled.buffer();
        empty.toBytes(count);
        count.setByte(count.writerIndex() - 1, PacketStationRecipeReply.MAX_ROWS + 1);
        assertThrows(IllegalArgumentException.class, () -> new PacketStationRecipeReply().fromBytes(count));
        count.release();
    }

    @Test
    void maximumReplyIsBoundedAndMixedCatalogSnapshotsCannotBeEncoded() {
        PacketStationRecipeReply reply = PacketStationRecipeReply.error(request(), RecipeCatalog.Status.MATCHES, "");
        reply.accepted = true;
        List<PacketStationRecipeReply.Entry> rows = new ArrayList<>();
        UUID session = UUID.randomUUID();
        String title = String.join("", Collections.nCopies(128, "a"));
        for (int i = 0; i < PacketStationRecipeReply.MAX_ROWS; i++) rows.add(
            new PacketStationRecipeReply.Entry(
                new RecipeCatalog.Token(session, 1, "fixture", MAP, String.format("%064x", i)),
                title));
        reply.entries = rows;
        ByteBuf wire = Unpooled.buffer();
        reply.toBytes(wire);
        assertTrue(wire.readableBytes() < PacketStationRecipeReply.MAX_WIRE_BYTES);
        PacketStationRecipeReply decoded = new PacketStationRecipeReply();
        decoded.fromBytes(wire);
        assertEquals(PacketStationRecipeReply.MAX_ROWS, decoded.entries.size());
        wire.release();
        rows.set(
            0,
            new PacketStationRecipeReply.Entry(
                new RecipeCatalog.Token(UUID.randomUUID(), 1, "fixture", MAP, String.format("%064x", 0)),
                title));
        ByteBuf invalid = Unpooled.buffer();
        assertThrows(IllegalArgumentException.class, () -> reply.toBytes(invalid));
        assertEquals(0, invalid.writerIndex());
        invalid.release();
    }

    private static final class Lookup implements DataQueries {

        final RecipeCatalog catalog;
        int calls;
        Runnable callback = () -> {};

        Lookup(int count) {
            RecipeCatalog.Builder builder = new RecipeCatalog.Builder(UUID.randomUUID(), 1);
            builder.registerMap(MAP);
            for (int i = 0; i < count; i++) builder.add(
                new RecipeDescriptor(
                    "fixture",
                    MAP,
                    24 + i,
                    20,
                    1,
                    false,
                    false,
                    Collections.singletonList(part("recipe_ui:input", 0)),
                    Collections.singletonList(part(OUTPUT, 1)),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    new QueryBudget()),
                new QueryBudget());
            catalog = builder.finish();
        }

        private static RecipeDescriptor.ItemPart part(String id, long amount) {
            return new RecipeDescriptor.ItemPart(
                0,
                ItemIdentity.of(id, 0, null, new QueryBudget()),
                amount,
                10000,
                RecipeDescriptor.Matching.EXACT,
                false,
                "",
                Collections.emptyList(),
                Collections.emptyList());
        }

        @Override
        public RecipeCatalog.Result recipes(ItemIdentity output, String map, QueryBudget budget) {
            calls++;
            callback.run();
            return catalog.find(output, map, budget, 1);
        }

        @Override
        public RecipeCatalog.Result recipe(RecipeCatalog.Token token, QueryBudget budget) {
            calls++;
            callback.run();
            return catalog.resolve(token, budget, 1);
        }

        @Override
        public long count(LabelReference source, ResourceSelector selector, QueryBudget budget) {
            throw new AssertionError("No inventory queries");
        }

        @Override
        public boolean empty(LabelReference source, ResourceSelector selector, QueryBudget budget) {
            throw new AssertionError("No inventory queries");
        }

        @Override
        public boolean available(LabelReference source, ResourceSelector selector, QueryBudget budget) {
            throw new AssertionError("No inventory queries");
        }
    }
}
