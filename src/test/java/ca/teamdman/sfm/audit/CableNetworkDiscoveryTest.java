package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static ca.teamdman.sfm.common.network.CableNetworkDiscovery.Status.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.block.BlockCable;
import ca.teamdman.sfm.common.block.BlockFancyCable;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.network.CableNetworkDiscovery;
import ca.teamdman.sfm.common.network.CableNetworkDiscovery.Snapshot;
import ca.teamdman.sfm.common.network.CableNetworkDiscovery.Status;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;
import cpw.mods.fml.common.gameevent.TickEvent;

class CableNetworkDiscoveryTest {

    @BeforeEach
    void serverThread() {
        ServerUiTasks.clear();
        ServerUiTasks.INSTANCE.tick(new TickEvent.ServerTickEvent(TickEvent.Phase.START));
    }

    @AfterEach
    void cleanup() {
        ServerUiTasks.clear();
    }

    @Test
    void componentIncludesAllPeripheralKindsAndMultipleManagersButDoesNotBridgeThroughThem() throws Exception {
        FakeWorld world = world();
        BlockPos origin = new BlockPos(0, 64, 0), cable = new BlockPos(1, 64, 0), first = new BlockPos(2, 64, 0),
            second = new BlockPos(1, 64, 1), behind = new BlockPos(3, 64, 0), nonInventory = new BlockPos(0, 65, 0);
        world.blocks.put(origin, new BlockCable());
        world.blocks.put(cable, new BlockFancyCable());
        world.blocks.put(behind, new BlockCable());
        world.tiles.put(first, new TileEntityManager());
        world.tiles.put(second, new TileEntityManager());
        world.tiles.put(nonInventory, new TileEntity());
        BlockPos inventory = new BlockPos(0, 64, -1);
        world.tiles.put(inventory, new InventoryTile(1) {

            @Override
            public int getSizeInventory() {
                throw new AssertionError("Discovery must not inspect capabilities or contents");
            }

            @Override
            public ItemStack getStackInSlot(int slot) {
                throw new AssertionError("Discovery must not read or flush inventory STATE");
            }
        });
        CableNetworkDiscovery scan = CableNetworkDiscovery.fromCable(world, origin);
        assertEquals(COMPLETE, scan.advance());
        Snapshot snapshot = scan.getSnapshot();
        assertEquals(positions(origin, cable), snapshot.cables);
        assertEquals(positions(first, second), snapshot.managers);
        assertEquals(positions(first, second, inventory, nonInventory), snapshot.peripherals);
        assertFalse(snapshot.cables.contains(behind));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.peripherals.clear());
        int reads = world.blockReads;
        assertSame(snapshot, scan.getSnapshot());
        assertEquals(COMPLETE, scan.advance());
        assertEquals(reads, world.blockReads, "Completed scans are never repeated by polling");
    }

    @Test
    void selectedManagerScanIncludesItsSeparateBranchesAndMatchesRuntimeDiscovery() throws Exception {
        FakeWorld world = world();
        world.tiles.put(MANAGER, new TileEntityManager());
        world.blocks.put(new BlockPos(-1, 64, 0), new BlockCable());
        world.blocks.put(new BlockPos(1, 64, 0), new BlockFancyCable());
        world.tiles.put(new BlockPos(-2, 64, 0), new TileEntity());
        world.tiles.put(new BlockPos(2, 64, 0), new TileEntity());
        world.tiles.put(new BlockPos(0, 65, 0), new TileEntity());
        CableNetwork network = new CableNetwork(world, MANAGER);
        long runtimeRevision = network.getSnapshotRevision(), lastScan = network.getLastScanTime();
        CableNetworkDiscovery scan = CableNetworkDiscovery.fromManager(world, MANAGER);
        assertEquals(COMPLETE, scan.advance());
        Snapshot snapshot = scan.getSnapshot();
        Set<BlockPos> expected = new HashSet<>(network.peekPeripherals());
        expected.remove(MANAGER);
        assertEquals(expected, snapshot.peripherals);
        assertEquals(positions(MANAGER), snapshot.managers);
        assertEquals(runtimeRevision, network.getSnapshotRevision());
        assertEquals(lastScan, network.getLastScanTime(), "Tool discovery does not advance runtime cache deadlines");
    }

    @Test
    void largeScansAreBoundedPerTickIncludingRepeatedUiRequests() throws Exception {
        FakeWorld world = world();
        BlockCable cable = new BlockCable();
        for (int x = 0; x < 1000; x++) world.blocks.put(new BlockPos(x, 64, 0), cable);
        CableNetworkDiscovery scan = CableNetworkDiscovery.fromCable(world, MANAGER);
        assertEquals(SCANNING, scan.advance());
        assertEquals(CableNetworkDiscovery.POSITIONS_PER_TICK, scan.getInspectedPositions());
        int reads = world.blockReads;
        assertThrows(IllegalStateException.class, scan::getSnapshot, "Partial scans cannot authorize editing");
        for (int request = 0; request < 10; request++) assertEquals(SCANNING, scan.advance());
        assertEquals(reads, world.blockReads);
        while (scan.getStatus() == SCANNING) {
            world.time++;
            int before = scan.getInspectedPositions();
            scan.advance();
            assertTrue(scan.getInspectedPositions() - before <= CableNetworkDiscovery.POSITIONS_PER_TICK);
        }
        assertEquals(COMPLETE, scan.getStatus());
        assertEquals(1000, scan.getSnapshot().cables.size());
    }

    @Test
    void topologyAndChunkChangesFencePartialAndCompletedScansIncludingBreakReplace() throws Exception {
        FakeWorld world = world();
        world.blocks.put(MANAGER, new BlockCable());
        CableNetworkDiscovery complete = CableNetworkDiscovery.fromCable(world, MANAGER);
        assertEquals(COMPLETE, complete.advance());
        CableNetwork.invalidate(world, 0, 64, 0);
        CableNetwork.invalidate(world, 0, 64, 0); // Same-looking replacement is still a different topology history.
        int reads = world.blockReads;
        assertEquals(STALE, complete.getStatus());
        assertThrows(IllegalStateException.class, complete::getSnapshot);
        assertEquals(STALE, complete.advance());
        assertEquals(reads, world.blockReads);
        for (int x = 1; x < 100; x++) world.blocks.put(new BlockPos(x, 64, 0), new BlockCable());
        CableNetworkDiscovery partial = CableNetworkDiscovery.fromCable(world, MANAGER);
        assertEquals(SCANNING, partial.advance());
        CableNetwork.invalidateChunk(world, 0, 0);
        assertEquals(STALE, partial.advance());
    }

    @Test
    void unloadsAndInvalidOriginsNeverCauseChunkLoadsOrAdvertiseFalseConnectivity() throws Exception {
        FakeWorld world = world();
        world.blocks.put(MANAGER, new BlockCable());
        BlockPos gap = new BlockPos(1, 64, 0), beyond = new BlockPos(2, 64, 0);
        world.unloaded.add(gap);
        world.blocks.put(gap, new BlockCable());
        world.blocks.put(beyond, new BlockCable());
        CableNetworkDiscovery scan = CableNetworkDiscovery.fromCable(world, MANAGER);
        assertEquals(COMPLETE, scan.advance());
        assertEquals(positions(gap), scan.getSnapshot().unloaded);
        assertEquals(positions(MANAGER), scan.getSnapshot().cables);
        CableNetworkDiscovery unloaded = CableNetworkDiscovery.fromCable(world, gap);
        assertEquals(UNAVAILABLE, unloaded.advance());
        assertThrows(IllegalStateException.class, unloaded::getSnapshot);
        assertEquals(
            UNAVAILABLE,
            CableNetworkDiscovery.fromManager(world, MANAGER)
                .advance());
        world.blocks.clear();
        assertEquals(
            UNAVAILABLE,
            CableNetworkDiscovery.fromCable(world, MANAGER)
                .advance());
    }

    @Test
    void scanCapacityFailsClosedInsteadOfReturningATruncatedComponent() throws Exception {
        FakeWorld world = world();
        BlockCable cable = new BlockCable();
        for (int x = 0; x < 7000; x++) world.blocks.put(new BlockPos(x, 64, 0), cable);
        CableNetworkDiscovery scan = CableNetworkDiscovery.fromCable(world, MANAGER);
        Status status;
        do {
            status = scan.advance();
            world.time++;
        } while (status == SCANNING);
        assertEquals(LIMIT, status);
        assertTrue(scan.getInspectedPositions() <= CableNetworkDiscovery.MAX_POSITIONS);
        assertThrows(IllegalStateException.class, scan::getSnapshot);
    }

    @Test
    void serverThreadAndOriginBoundsAreCheckedBeforeReadingWorld() throws Exception {
        FakeWorld world = world();
        assertThrows(IllegalArgumentException.class, () -> CableNetworkDiscovery.fromCable(world, null));
        assertThrows(
            IllegalArgumentException.class,
            () -> CableNetworkDiscovery.fromCable(world, new BlockPos(Integer.MAX_VALUE, 64, 0)));
        world.blocks.put(MANAGER, new BlockCable());
        CableNetworkDiscovery scan = CableNetworkDiscovery.fromCable(world, MANAGER);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread worker = new Thread(() -> {
            try {
                scan.advance();
            } catch (Throwable error) {
                failure.set(error);
            }
        });
        worker.start();
        worker.join();
        assertInstanceOf(IllegalStateException.class, failure.get());
        assertEquals(0, world.blockReads);
        java.lang.reflect.Field remote = net.minecraft.world.World.class.getDeclaredField("isRemote");
        remote.setAccessible(true);
        remote.setBoolean(world, true);
        assertThrows(IllegalStateException.class, () -> CableNetworkDiscovery.fromCable(world, MANAGER));
        assertEquals(0, world.blockReads);
    }

    private static Set<BlockPos> positions(BlockPos... positions) {
        return new HashSet<>(Arrays.asList(positions));
    }
}
