package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.audit.AuditRegressionTest.FakeWorld;
import ca.teamdman.sfm.audit.AuditRegressionTest.InventoryTile;
import ca.teamdman.sfm.common.block.BlockCable;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.util.BlockPos;

/** Compare world discovery with an independent coordinate-graph reachability model. */
class TopologyScanTest {

    @Test
    void seededComponentsBoundariesAndCableTilesMatchCoordinateModel() throws Exception {
        Block cable = new BlockCable();
        for (int seed = 0; seed < 30; seed++) {
            FakeWorld world = AuditRegressionTest.world();
            BlockPos origin = new BlockPos(seed % 2 == 0 ? -29999990 : 29999990, seed % 3 == 0 ? 1 : 254, -97);
            Random random = new Random(seed);
            Set<BlockPos> cablePositions = new HashSet<>();
            for (int dx = -4; dx <= 4; dx++) for (int dy = -2; dy <= 2; dy++) for (int dz = -4; dz <= 4; dz++) {
                BlockPos pos = origin.offset(dx, dy, dz);
                if (random.nextInt(3) == 0) {
                    cablePositions.add(pos);
                    world.blocks.put(pos, cable);
                }
                if (random.nextInt(6) == 0) world.tiles.put(pos, new InventoryTile(1));
                if (random.nextInt(17) == 0) world.unloaded.add(pos);
            }
            Set<BlockPos> expectedCables = new HashSet<>();
            ArrayDeque<BlockPos> frontier = new ArrayDeque<>();
            for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) frontier.add(origin.offset(direction));
            while (!frontier.isEmpty()) {
                BlockPos pos = frontier.remove();
                if (!loaded(world, pos) || !cablePositions.contains(pos) || !expectedCables.add(pos)) continue;
                for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) frontier.add(pos.offset(direction));
            }
            Set<BlockPos> inspected = new HashSet<>(expectedCables);
            for (BlockPos pos : expectedCables)
                for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) inspected.add(pos.offset(direction));
            for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) inspected.add(origin.offset(direction));
            Set<BlockPos> expectedPeripherals = new HashSet<>();
            for (BlockPos pos : world.tiles.keySet())
                if (loaded(world, pos) && inspected.contains(pos)) expectedPeripherals.add(pos);
            CableNetwork actual = new CableNetwork(world, origin);
            assertEquals(expectedCables, actual.getCables(), "cables, seed " + seed);
            assertEquals(expectedPeripherals, actual.getPeripherals(), "tiles, seed " + seed);
        }
    }

    @Test
    void periodicRefreshesSpreadOutWithoutReducingDiscoveryOrDelayingInvalidations() throws Exception {
        FakeWorld world = AuditRegressionTest.world();
        Block cable = new BlockCable();
        CableNetwork[] networks = new CableNetwork[100];
        for (int i = 0; i < networks.length; i++) {
            BlockPos origin = new BlockPos(-240 + i % 10 * 5, 224, -650 + i / 10 * 5);
            world.blocks.put(origin.offset(0, 0, 1), cable);
            networks[i] = new CableNetwork(world, origin);
        }
        for (int tick = 0; tick < 100; tick++) {
            world.time++;
            for (CableNetwork network : networks) network.getCables();
        }
        int[] scans = new int[100];
        int maximumInOneTick = 0;
        for (int tick = 0; tick < 420; tick++) {
            world.time++;
            int scansThisTick = 0;
            for (int i = 0; i < networks.length; i++) {
                int readsBefore = world.blockReads;
                networks[i].getCables();
                if (world.blockReads != readsBefore) {
                    scans[i]++;
                    scansThisTick++;
                }
            }
            maximumInOneTick = Math.max(maximumInOneTick, scansThisTick);
        }
        for (int count : scans) assertEquals(20, count, "Same periodic refresh work");
        assertTrue(maximumInOneTick <= 8, "The representative 100-manager layout spreads across phases");
        CableNetwork.invalidate(world);
        for (CableNetwork network : networks) {
            int readsBefore = world.blockReads;
            network.getCables();
            assertTrue(world.blockReads > readsBefore, "Invalidation is immediate, even before the deadline");
            readsBefore = world.blockReads;
            network.getCables();
            assertEquals(readsBefore, world.blockReads, "Repeated reads in one tick do not rescan");
        }
    }

    @Test
    void noEventRefreshRemainsWithinTwentyOneTicksAtEveryStartingPhase() throws Exception {
        Block cable = new BlockCable();
        for (int start = 100; start < 121; start++) {
            FakeWorld world = AuditRegressionTest.world();
            world.time = start;
            BlockPos bridge = new BlockPos(1, 64, 0), target = new BlockPos(2, 64, 0);
            world.blocks.put(bridge, cable);
            world.tiles.put(target, new InventoryTile(1));
            CableNetwork network = new CableNetwork(world, new BlockPos(0, 64, 0));
            assertTrue(network.isConnected(target));
            world.blocks.remove(bridge);
            int elapsed = 0;
            while (network.isConnected(target) && elapsed < 21) {
                elapsed++;
                world.time++;
            }
            assertFalse(network.isConnected(target));
            assertTrue(elapsed > 0 && elapsed <= 21);
        }
    }

    private static boolean loaded(FakeWorld world, BlockPos pos) {
        return pos.y >= 0 && pos.y < 256 && !world.unloaded.contains(pos);
    }

    @Test
    void invalidationRefreshesSplitMergeAndPeripheralReplacementWithoutChangingLiveViews() throws Exception {
        FakeWorld world = AuditRegressionTest.world();
        Block cable = new BlockCable();
        BlockPos first = new BlockPos(-1, 64, 0), bridge = new BlockPos(-2, 64, 0), target = new BlockPos(-3, 64, 0);
        world.blocks.put(first, cable);
        world.blocks.put(bridge, cable);
        world.tiles.put(target, new InventoryTile(1));
        CableNetwork network = new CableNetwork(world, new BlockPos(0, 64, 0));
        Set<BlockPos> cableView = network.getCables();
        Set<BlockPos> tileView = network.getPeripherals();
        assertTrue(network.isConnected(target));
        assertThrows(UnsupportedOperationException.class, () -> cableView.clear());
        world.blocks.remove(bridge);
        CableNetwork.invalidate(world);
        assertFalse(network.isConnected(target));
        assertEquals(1, cableView.size());
        assertFalse(tileView.contains(target));
        world.blocks.put(bridge, cable);
        CableNetwork.invalidate(world);
        assertTrue(network.isConnected(target));
        world.tiles.remove(target);
        CableNetwork.invalidate(world);
        assertFalse(network.isConnected(target));
        world.tiles.put(target, new InventoryTile(1));
        CableNetwork.invalidate(world);
        assertTrue(network.isConnected(target));
    }

    @Test
    void fallbackExpiryClockRewindAndUnloadedPathsRemainFresh() throws Exception {
        FakeWorld world = AuditRegressionTest.world();
        Block cable = new BlockCable();
        BlockPos bridge = new BlockPos(0, 64, -1), target = new BlockPos(0, 64, -2);
        world.blocks.put(bridge, cable);
        world.tiles.put(target, new InventoryTile(1));
        CableNetwork network = new CableNetwork(world, new BlockPos(0, 64, 0));
        assertTrue(network.isConnected(target));
        world.blocks.remove(bridge); // Simulate a third-party change without an event.
        world.time += 21;
        assertFalse(network.isConnected(target));
        world.blocks.put(bridge, cable);
        world.time--;
        assertTrue(network.isConnected(target));
        world.unloaded.add(bridge);
        CableNetwork.invalidate(world);
        assertFalse(network.isConnected(target));
        world.unloaded.clear();
        CableNetwork.invalidate(world);
        assertTrue(network.isConnected(target));
    }
}
