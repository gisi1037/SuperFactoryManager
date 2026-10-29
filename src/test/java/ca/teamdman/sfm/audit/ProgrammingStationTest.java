package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;

import ca.teamdman.sfm.common.net.PacketStationReply;
import ca.teamdman.sfm.common.net.PacketStationRequest;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.program.project.ProjectDraft;
import ca.teamdman.sfm.common.program.project.ProjectPage;
import ca.teamdman.sfm.common.program.project.ProjectValidation;
import ca.teamdman.sfm.common.program.project.StationProjectService;
import ca.teamdman.sfm.common.tileentity.TileEntityProgrammingStation;
import ca.teamdman.sfm.integration.mui2.ProgrammingStationMui2Container;
import ca.teamdman.sfm.integration.mui2.ProgrammingStationMui2Factory;
import cpw.mods.fml.common.gameevent.TickEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

class ProgrammingStationTest {

    @BeforeEach
    void begin() {
        ServerUiTasks.clear();
        ServerUiTasks.INSTANCE.tick(new TickEvent.ServerTickEvent(TickEvent.Phase.START));
    }

    @AfterEach
    void end() {
        ServerUiTasks.clear();
    }

    @Test
    void stationRejectsHiddenSlotActionsIncludingOutsideDrops() {
        ProgrammingStationMui2Container container = new ProgrammingStationMui2Container(null, UUID.randomUUID());
        for (int slot : new int[] { -999, -1, 0, 8, 35, 215 }) {
            for (int mode = 0; mode <= 6; mode++) {
                for (int button = 0; button < 9; button++) {
                    assertNull(container.slotClick(slot, button, mode, null));
                }
            }
            assertNull(container.transferStackInSlot(null, slot));
        }
        assertFalse(container.canDragIntoSlot(null));
        assertTrue(container.inventorySlots.isEmpty());
    }

    @Test
    void approvedStationHasNoPlayerInventoryBinding() throws Exception {
        Fixture fixture = new Fixture();
        station(fixture);
        EntityPlayerMP player = player(fixture);
        // A hidden player-slot binding would dereference this null inventory.
        assertNull(player.inventory);
        ProgrammingStationMui2Factory.Data data = new ProgrammingStationMui2Factory.Data(
            player,
            MANAGER.x,
            MANAGER.y,
            MANAGER.z);
        ModularPanel panel = ProgrammingStationMui2Factory.INSTANCE.createPanel(data, null, new UISettings());
        assertEquals(ProgrammingStationMui2Factory.PANEL_NAME, panel.getName());
        assertNotNull(data.getSessionId());
        assertNull(player.inventory);
    }

    @Test
    void stationPersistsOnlyProjectReferenceAndSurvivesMalformedIds() {
        TileEntity.addMapping(TileEntityProgrammingStation.class, "sfm.programmingStation");
        TileEntityProgrammingStation station = new TileEntityProgrammingStation();
        UUID project = UUID.randomUUID();
        station.selectProject(project);
        NBTTagCompound tag = new NBTTagCompound();
        station.writeToNBT(tag);
        assertEquals(project.toString(), tag.getString("sfm:project"));
        assertFalse(tag.hasKey("sfm:program"));
        assertEquals(
            5,
            tag.func_150296_c()
                .size(),
            "Only tile ID, coordinates and project UUID");
        TileEntityProgrammingStation loaded = new TileEntityProgrammingStation();
        loaded.readFromNBT(tag);
        assertEquals(project, loaded.getProjectId());
        tag.setString("sfm:project", "../../another-world/private.sfml");
        loaded.readFromNBT(tag);
        assertNull(loaded.getProjectId());
        assertFalse(loaded.canUpdate());
    }

    @Test
    void liveStationAuthorityChecksSessionRangeWindowReplacementAndThread() throws Exception {
        Fixture fixture = new Fixture();
        TileEntityProgrammingStation station = station(fixture);
        EntityPlayerMP player = player(fixture);
        UUID session = UUID.randomUUID();
        ProgrammingStationMui2Container container = new ProgrammingStationMui2Container(station, session);
        container.windowId = 9;
        player.openContainer = container;
        PacketStationRequest request = request();
        request.sessionId = session;
        assertSame(container, StationProjectService.authorized(player, request));
        player.isDead = true;
        assertNull(StationProjectService.authorized(player, request));
        player.isDead = false;
        request.sessionId = UUID.randomUUID();
        assertNull(StationProjectService.authorized(player, request));
        request.sessionId = session;
        request.windowId++;
        assertNull(StationProjectService.authorized(player, request));
        request.windowId--;
        player.posX = 100;
        assertNull(StationProjectService.authorized(player, request));
        player.posX = MANAGER.x + .5;
        fixture.world.unloaded.add(MANAGER);
        assertNull(StationProjectService.authorized(player, request));
        fixture.world.unloaded.clear();
        fixture.world.tiles.put(MANAGER, new TileEntityProgrammingStation());
        assertNull(StationProjectService.authorized(player, request));
        fixture.world.tiles.put(MANAGER, station);
        AtomicReference<ProgrammingStationMui2Container> offThread = new AtomicReference<>(container);
        Thread worker = new Thread(() -> offThread.set(StationProjectService.authorized(player, request)));
        worker.start();
        worker.join();
        assertNull(offThread.get());
    }

    @Test
    void factoryDeniesWithoutInventoryBindingAndCannotTrustClientSession() throws Exception {
        Fixture fixture = new Fixture();
        station(fixture);
        EntityPlayerMP player = player(fixture);
        player.posX = 100;
        ProgrammingStationMui2Factory.Data data = new ProgrammingStationMui2Factory.Data(
            player,
            MANAGER.x,
            MANAGER.y,
            MANAGER.z);
        UISettings settings = new UISettings();
        ModularPanel panel = ProgrammingStationMui2Factory.INSTANCE.createPanel(data, null, settings);
        assertEquals(ProgrammingStationMui2Factory.DENIED_PANEL, panel.getName());
        assertNull(data.getSessionId());
        assertFalse(settings.canPlayerInteractWithUI(player));
        assertNull(player.inventory);
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        try {
            buffer.writeVarIntToBuffer(MANAGER.x);
            buffer.writeVarIntToBuffer(MANAGER.y);
            buffer.writeVarIntToBuffer(MANAGER.z);
            buffer.writeBoolean(true);
            buffer.writeLong(1);
            buffer.writeLong(2);
            assertNull(
                ProgrammingStationMui2Factory.INSTANCE.readGuiData(player, buffer)
                    .getSessionId());
        } finally {
            buffer.release();
        }
    }

    @Test
    void everyRequestAndReplyPayloadRoundTripsIncludingInitialRevisionZero() {
        for (PacketStationRequest.Action action : PacketStationRequest.Action.values()) {
            PacketStationRequest request = request();
            request.action = action;
            request.projectId = UUID.randomUUID();
            request.name = "Local project";
            request.source = "invalid draft \ud83d\ude00";
            ByteBuf buffer = Unpooled.buffer();
            try {
                request.toBytes(buffer);
                PacketStationRequest copy = new PacketStationRequest();
                copy.fromBytes(buffer);
                assertEquals(request.source, copy.source);
                assertEquals(action, copy.action);
                assertEquals(request.sessionId, copy.sessionId);
                PacketStationReply reply = new PacketStationReply(request, true, "Saved");
                if (action == PacketStationRequest.Action.LIST) reply.page = new ProjectPage(
                    Arrays.asList(new ProjectPage.Entry(request.projectId, 0, request.name)),
                    1,
                    0);
                else if (action == PacketStationRequest.Action.VALIDATE)
                    reply.validation = new ProjectValidation(request.projectId, 0, false, "Line 1: invalid draft");
                else reply.draft = new ProjectDraft(
                    request.projectId,
                    UUID.randomUUID(),
                    0,
                    request.name,
                    request.source);
                buffer.clear();
                reply.toBytes(buffer);
                PacketStationReply decoded = new PacketStationReply();
                decoded.fromBytes(buffer);
                assertEquals(reply.accepted, decoded.accepted);
                assertEquals(reply.requestId, decoded.requestId);
                assertEquals(reply.sessionId, decoded.sessionId);
                if (reply.draft != null) assertEquals(reply.draft.source, decoded.draft.source);
                if (reply.validation != null) assertFalse(decoded.validation.valid);
                if (reply.page != null) assertEquals(1, decoded.page.entries.size());
            } finally {
                buffer.release();
            }
        }
    }

    @Test
    void wireRejectsOversizeInvalidUnicodeTrailingDataAndInvalidRevisions() {
        PacketStationRequest request = request();
        request.source = new String(new char[30001]).replace('\0', 'a');
        assertThrows(IllegalArgumentException.class, request::validate);
        request.source = "\ud800";
        assertThrows(IllegalArgumentException.class, request::validate);
        request.source = "";
        ByteBuf buffer = Unpooled.buffer();
        try {
            request.toBytes(buffer);
            buffer.writeByte(0);
            assertThrows(IllegalArgumentException.class, () -> new PacketStationRequest().fromBytes(buffer));
            buffer.clear();
            PacketStationReply reply = new PacketStationReply(request, true, "Invalid");
            reply.page = new ProjectPage(java.util.Collections.emptyList(), 0, 1);
            reply.toBytes(buffer);
            assertThrows(IllegalArgumentException.class, () -> new PacketStationReply().fromBytes(buffer));
            buffer.clear();
            request.action = PacketStationRequest.Action.LOAD;
            reply = new PacketStationReply(request, true, "Invalid");
            reply.draft = new ProjectDraft(UUID.randomUUID(), UUID.randomUUID(), -1, "Name", "");
            reply.toBytes(buffer);
            assertThrows(IllegalArgumentException.class, () -> new PacketStationReply().fromBytes(buffer));
        } finally {
            buffer.release();
        }
    }

    private static PacketStationRequest request() {
        PacketStationRequest request = new PacketStationRequest();
        request.x = MANAGER.x;
        request.y = MANAGER.y;
        request.z = MANAGER.z;
        request.windowId = 9;
        request.sessionId = UUID.randomUUID();
        request.requestId = 1;
        return request;
    }

    private static TileEntityProgrammingStation station(Fixture fixture) {
        TileEntityProgrammingStation station = new TileEntityProgrammingStation();
        station.setWorldObj(fixture.world);
        station.xCoord = MANAGER.x;
        station.yCoord = MANAGER.y;
        station.zCoord = MANAGER.z;
        fixture.world.tiles.put(MANAGER, station);
        return station;
    }

    private static EntityPlayerMP player(Fixture fixture) throws Exception {
        EntityPlayerMP player = allocate(EntityPlayerMP.class);
        player.worldObj = fixture.world;
        player.playerNetServerHandler = new net.minecraft.network.NetHandlerPlayServer(
            null,
            new net.minecraft.network.NetworkManager(false) {

                @Override
                public boolean isChannelOpen() {
                    return true;
                }
            },
            player);
        player.posX = MANAGER.x + .5;
        player.posY = MANAGER.y + .5;
        player.posZ = MANAGER.z + .5;
        return player;
    }
}
