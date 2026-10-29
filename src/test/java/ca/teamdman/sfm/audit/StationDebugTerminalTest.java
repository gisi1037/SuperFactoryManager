package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.net.PacketDebugTerminalReply;
import ca.teamdman.sfm.common.net.PacketStationDebugReply;
import ca.teamdman.sfm.common.net.PacketStationDebugRequest;
import ca.teamdman.sfm.common.net.PacketStationDebugRequest.Action;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.tileentity.TileEntityProgrammingStation;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfm.integration.mui2.ProgrammingStationMui2Container;
import cpw.mods.fml.common.gameevent.TickEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

class StationDebugTerminalTest {

    private Fixture fixture;
    private TileEntityManager manager;
    private TileEntityProgrammingStation station;
    private EntityPlayerMP player;
    private ProgrammingStationMui2Container container;
    private boolean connected;
    private long requestId;

    @BeforeEach
    void begin() throws Exception {
        ServerUiTasks.clear();
        ServerUiTasks.INSTANCE.tick(new TickEvent.ServerTickEvent(TickEvent.Phase.START));
        fixture = new Fixture();
        manager = RecoveryFixtures.manager(fixture);
        station = new TileEntityProgrammingStation();
        station.setWorldObj(fixture.world);
        station.xCoord = MANAGER.x + 1;
        station.yCoord = MANAGER.y;
        station.zCoord = MANAGER.z;
        fixture.world.tiles.put(new BlockPos(station.xCoord, station.yCoord, station.zCoord), station);
        player = allocate(EntityPlayerMP.class);
        player.worldObj = fixture.world;
        connected = true;
        player.playerNetServerHandler = new NetHandlerPlayServer(null, new NetworkManager(false) {

            @Override
            public boolean isChannelOpen() {
                return connected;
            }
        }, player);
        player.posX = MANAGER.x + .5;
        player.posY = MANAGER.y + .5;
        player.posZ = MANAGER.z + .5;
        container = new ProgrammingStationMui2Container(station, UUID.randomUUID());
        container.windowId = 19;
        player.openContainer = container;
    }

    @AfterEach
    void end() {
        PacketStationDebugRequest.clear(container);
        ServerUiTasks.clear();
    }

    @Test
    void explicitConnectionAndCommandsUseExistingBoundedServiceWithoutImplicitCapture() {
        assertFalse(
            manager.getDebugConsole()
                .isEnabled());
        assertFalse(process(Action.COMMAND, "capture on").accepted);
        PacketStationDebugReply opened = process(Action.CONNECT, "");
        assertTrue(opened.accepted);
        assertNotNull(opened.payload);
        assertFalse(opened.payload.capturing);
        assertFalse(
            manager.getDebugConsole()
                .isEnabled());
        assertTrue(process(Action.COMMAND, "capture on").accepted);
        assertTrue(
            manager.getDebugConsole()
                .isEnabled());
        manager.getDebugConsole()
            .append("captured test");
        PacketStationDebugReply poll = process(Action.COMMAND, "");
        assertEquals(Collections.singletonList("captured test"), poll.payload.history);
        assertTrue(poll.payload.result.isEmpty());
        assertTrue(process(Action.DISCONNECT, "").accepted);
        assertFalse(process(Action.COMMAND, "clear").accepted);
        assertEquals(
            1,
            manager.getDebugConsole()
                .snapshot()
                .size());
        assertTrue(
            manager.getDebugConsole()
                .isEnabled(),
            "Disconnect does not change another user's capture setting");
    }

    @Test
    void sessionWindowReplacementDeathDisconnectAndWrongThreadCannotAcquireStationAuthority() throws Exception {
        PacketStationDebugRequest request = request(Action.CONNECT, "");
        assertSame(container, PacketStationDebugRequest.authorizedStation(player, request));
        UUID session = request.sessionId;
        request.sessionId = UUID.randomUUID();
        assertNull(PacketStationDebugRequest.process(player, request));
        request.sessionId = session;
        request.windowId++;
        assertNull(PacketStationDebugRequest.process(player, request));
        request.windowId--;
        request.x++;
        assertNull(PacketStationDebugRequest.process(player, request));
        request.x--;
        player.isDead = true;
        assertNull(PacketStationDebugRequest.process(player, request));
        player.isDead = false;
        connected = false;
        assertNull(PacketStationDebugRequest.process(player, request));
        connected = true;
        player.posX += 20;
        assertNull(PacketStationDebugRequest.process(player, request));
        player.posX -= 20;
        BlockPos position = new BlockPos(station.xCoord, station.yCoord, station.zCoord);
        fixture.world.unloaded.add(position);
        assertNull(PacketStationDebugRequest.process(player, request));
        fixture.world.unloaded.clear();
        fixture.world.tiles.put(position, new TileEntityProgrammingStation());
        assertNull(PacketStationDebugRequest.process(player, request));
        fixture.world.tiles.put(position, station);
        AtomicReference<PacketStationDebugReply> offThread = new AtomicReference<>();
        Thread thread = new Thread(() -> offThread.set(PacketStationDebugRequest.process(player, request)));
        thread.start();
        thread.join();
        assertNull(offThread.get());
    }

    @Test
    void targetReplacementUnloadRangeAndContainerCloseRequireExplicitReconnect() throws Exception {
        assertTrue(process(Action.CONNECT, "").accepted);
        fixture.world.unloaded.add(MANAGER);
        assertFalse(process(Action.COMMAND, "capture on").accepted, "No unloaded getTileEntity access is permitted");
        fixture.world.unloaded.clear();
        assertFalse(process(Action.COMMAND, "capture on").accepted);
        assertTrue(process(Action.CONNECT, "").accepted);
        TileEntityManager replacement = RecoveryFixtures.manager(fixture);
        assertFalse(process(Action.COMMAND, "capture on").accepted);
        assertFalse(
            replacement.getDebugConsole()
                .isEnabled());
        manager = replacement;
        assertTrue(process(Action.CONNECT, "").accepted);
        // Still close enough to the station at x+1, but beyond 8 blocks from the manager.
        player.posX = MANAGER.x + 8.6;
        assertFalse(process(Action.COMMAND, "capture on").accepted);
        player.posX = MANAGER.x + .5;
        assertTrue(process(Action.CONNECT, "").accepted);
        PacketStationDebugRequest.clear(container);
        assertFalse(process(Action.COMMAND, "capture on").accepted);
    }

    @Test
    void staleRequestCannotRepeatCommandsOrClearHistory() {
        assertTrue(process(Action.CONNECT, "").accepted);
        PacketStationDebugRequest stale = request(Action.COMMAND, "clear");
        assertTrue(PacketStationDebugRequest.process(player, stale).accepted);
        manager.getDebugConsole()
            .append("new output");
        assertFalse(PacketStationDebugRequest.process(player, stale).accepted);
        assertEquals(
            Collections.singletonList("new output"),
            manager.getDebugConsole()
                .snapshot());
    }

    @Test
    void bothWireEnvelopesRejectTruncationTrailingBytesAndInvalidCommands() {
        PacketStationDebugRequest request = request(Action.COMMAND, "labels exact_case");
        request.windowId = 255;
        ByteBuf bytes = Unpooled.buffer();
        try {
            request.toBytes(bytes);
            PacketStationDebugRequest decoded = new PacketStationDebugRequest();
            decoded.fromBytes(bytes.duplicate());
            assertEquals(request.sessionId, decoded.sessionId);
            assertEquals(request.command, decoded.command);
            for (int length = 0; length < bytes.readableBytes(); length++) {
                final int n = length;
                assertThrows(
                    RuntimeException.class,
                    () -> new PacketStationDebugRequest().fromBytes(bytes.slice(0, n)));
            }
            bytes.writeByte(1);
            assertThrows(RuntimeException.class, () -> new PacketStationDebugRequest().fromBytes(bytes.duplicate()));
        } finally {
            bytes.release();
        }
        request.command = new String(new char[129]).replace('\0', 'x');
        assertThrows(IllegalArgumentException.class, request::validate);
        request.command = "\uD800";
        assertThrows(IllegalArgumentException.class, request::validate);
        request.command = "capture on";
        request.action = Action.CONNECT;
        assertThrows(IllegalArgumentException.class, request::validate);
        request.action = Action.COMMAND;
        request.command = "status";
        PacketStationDebugReply reply = new PacketStationDebugReply(
            request,
            true,
            "Connected",
            new PacketDebugTerminalReply(
                0,
                0,
                0,
                1,
                1,
                false,
                0,
                Collections.singletonList("status"),
                Collections.singletonList("print")));
        ByteBuf response = Unpooled.buffer();
        try {
            reply.toBytes(response);
            PacketStationDebugReply decoded = new PacketStationDebugReply();
            decoded.fromBytes(response.duplicate());
            assertTrue(decoded.matches(request));
            assertEquals(reply.payload.history, decoded.payload.history);
            for (int length = 0; length < response.readableBytes(); length++) {
                final int n = length;
                assertThrows(
                    RuntimeException.class,
                    () -> new PacketStationDebugReply().fromBytes(response.slice(0, n)));
            }
            response.writeByte(1);
            assertThrows(RuntimeException.class, () -> new PacketStationDebugReply().fromBytes(response.duplicate()));
        } finally {
            response.release();
        }
    }

    @Test
    void replyCorrelationIncludesStationSessionTargetWindowAndGlobalRequestIdentity() {
        PacketStationDebugRequest request = request(Action.CONNECT, "");
        PacketStationDebugReply reply = new PacketStationDebugReply(request, false, "Unavailable", null);
        assertTrue(reply.matches(request));
        request.requestId++;
        assertFalse(reply.matches(request));
        request.requestId--;
        request.windowId++;
        assertFalse(reply.matches(request));
        request.windowId--;
        request.targetX++;
        assertFalse(reply.matches(request));
        request.targetX--;
        request.targetY++;
        assertFalse(reply.matches(request));
        request.targetY--;
        request.targetZ++;
        assertFalse(reply.matches(request));
        request.targetZ--;
        request.x++;
        assertFalse(reply.matches(request));
        request.x--;
        request.sessionId = UUID.randomUUID();
        assertFalse(reply.matches(request));
    }

    private PacketStationDebugReply process(Action action, String command) {
        return PacketStationDebugRequest.process(player, request(action, command));
    }

    private PacketStationDebugRequest request(Action action, String command) {
        PacketStationDebugRequest request = new PacketStationDebugRequest();
        request.x = station.xCoord;
        request.y = station.yCoord;
        request.z = station.zCoord;
        request.targetX = MANAGER.x;
        request.targetY = MANAGER.y;
        request.targetZ = MANAGER.z;
        request.sessionId = container.sessionId;
        request.windowId = container.windowId;
        request.requestId = ++requestId;
        request.action = action;
        request.command = command;
        return request;
    }
}
