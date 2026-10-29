package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.block.BlockCable;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.program.RedstoneTriggerTracker;
import ca.teamdman.sfm.common.tileentity.TileEntityRedstonePort;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.ast.NamedRedstoneTrigger;
import ca.teamdman.sfml.data.QueryBudget;

/** Scoped connection history must preserve pending edges without replaying across gaps. */
class RedstoneTopologyHistoryTest {

    @Test
    void unrelatedBlockAndChunkEventsPreserveBothPendingEdgesExactlyOnce() throws Exception {
        Rig rig = new Rig(MANAGER);
        rig.capture(false, false);
        rig.pulse();
        int reads = rig.world.blockReads;
        CableNetwork.invalidate(rig.world, 200, 64, 200);
        CableNetwork.invalidateChunk(rig.world, 20, -20);
        assertEquals(reads, rig.world.blockReads, "Event history does not query world providers");
        rig.capture(true, true);
        rig.capture(false, false);
        rig.pulse();
        rig.world.time += 21;
        rig.capture(true, true);
    }

    @Test
    void managerNeighborNotificationRebasesItsNetworkButNotDistantNetworks() throws Exception {
        FakeWorld world = world();
        Rig first = new Rig(world, MANAGER), second = new Rig(world, new BlockPos(100, 64, 100));
        first.capture(false, false);
        second.capture(false, false);
        first.pulse();
        second.pulse();
        first.edit(MANAGER);
        first.capture(false, false);
        second.capture(true, true);
        first.pulse();
        first.capture(true, true);
    }

    @Test
    void transientCableBreakAndReconnectBeforeObservationRebases() throws Exception {
        Rig rig = new Rig(MANAGER);
        rig.capture(false, false);
        rig.world.blocks.remove(rig.bridge);
        rig.edit(rig.bridge);
        rig.pulse();
        rig.world.blocks.put(rig.bridge, rig.cable);
        rig.edit(rig.bridge);
        rig.capture(false, false);
        rig.capture(false, false);
        rig.pulse();
        rig.capture(true, true);
    }

    @Test
    void observedDisconnectAndReconnectDoNotReplayDisconnectedCounters() throws Exception {
        Rig rig = new Rig(MANAGER);
        rig.capture(false, false);
        rig.world.blocks.remove(rig.bridge);
        rig.edit(rig.bridge);
        rig.pulse();
        rig.capture(false, false);
        rig.world.blocks.put(rig.bridge, rig.cable);
        rig.edit(rig.bridge);
        rig.capture(false, false);
        rig.pulse();
        rig.capture(true, true);
    }

    @Test
    void transientChunkGapRebasesEvenWhenTheSameTilesReturnBeforeCapture() throws Exception {
        Rig rig = new Rig(new BlockPos(14, 64, 0));
        rig.capture(false, false);
        CableNetwork.invalidateChunk(rig.world, 1, 0);
        rig.pulse();
        CableNetwork.invalidateChunk(rig.world, 1, 0);
        rig.capture(false, false);
        rig.pulse();
        rig.capture(true, true);
    }

    @Test
    void providerChunkTwoBlocksBeyondPeripheralIsPartOfGapHistory() throws Exception {
        Rig rig = new Rig(new BlockPos(12, 64, 8));
        assertEquals(14, rig.portPos.x);
        rig.capture(false, false);
        rig.pulse();
        CableNetwork.invalidateChunk(rig.world, 1, 0);
        rig.capture(false, false);
        rig.pulse();
        rig.capture(true, true);
    }

    @Test
    void unloadedFrontierChunksAndNegativeCoordinatesAreTrackedWithoutLoading() throws Exception {
        FakeWorld world = world();
        BlockPos manager = new BlockPos(-15, 64, -8), frontier = new BlockPos(-17, 64, -8);
        world.blocks.put(new BlockPos(-16, 64, -8), new BlockCable());
        world.unloaded.add(frontier);
        CableNetwork network = new CableNetwork(world, manager);
        long generation = network.getTopologyGeneration();
        int reads = world.blockReads;
        CableNetwork.invalidateChunk(world, -2, -1);
        assertEquals(generation + 1, network.getTopologyGeneration());
        assertEquals(reads, world.blockReads);
        generation = network.getTopologyGeneration();
        network.scan();
        CableNetwork.invalidateChunk(world, -2, -1);
        assertEquals(generation + 1, network.getTopologyGeneration(), "Rescans retain unloaded frontier chunks");
    }

    @Test
    void relevantEditDuringProviderCaptureFailsClosedAndNextObservationRebases() throws Exception {
        Rig rig = new Rig(MANAGER);
        rig.capture(false, false);
        rig.pulse();
        rig.port.onRefresh = () -> rig.edit(rig.bridge);
        rig.capture(false, false);
        rig.port.onRefresh = null;
        rig.capture(false, false);
        rig.pulse();
        rig.capture(true, true);
    }

    @Test
    void unrelatedEditDuringProviderCapturePreservesEligibility() throws Exception {
        Rig rig = new Rig(MANAGER);
        rig.capture(false, false);
        rig.pulse();
        rig.port.onRefresh = () -> CableNetwork.invalidateChunk(rig.world, 200, 200);
        rig.capture(true, true);
        rig.port.onRefresh = null;
        rig.capture(false, false);
    }

    @Test
    void peripheralReplacementEpochResetAndManagerResetRemainBaselines() throws Exception {
        Rig rig = new Rig(MANAGER);
        rig.capture(false, false);
        rig.port = new CounterPort();
        rig.pulse();
        rig.world.tiles.put(rig.portPos, rig.port);
        rig.capture(false, false);
        rig.pulse();
        rig.port.epoch++;
        rig.capture(false, false);
        rig.pulse();
        rig.tracker.reset();
        rig.capture(false, false);
        rig.pulse();
        rig.capture(true, true);
    }

    @Test
    void unknownWorldEditRemainsConservativeAndOtherWorldsCannotRebase() throws Exception {
        Rig rig = new Rig(MANAGER);
        rig.capture(false, false);
        rig.pulse();
        CableNetwork.invalidate(world());
        rig.capture(true, true);
        rig.pulse();
        CableNetwork.invalidate(rig.world);
        rig.capture(false, false);
    }

    @Test
    void ordinaryDiscoveryDoesNotRegisterForNativeEventHistory() throws Exception {
        FakeWorld world = world();
        CableNetwork[] ordinary = new CableNetwork[256];
        for (int i = 0; i < ordinary.length; i++) {
            ordinary[i] = new CableNetwork(world, new BlockPos(i * 5, 64, 100));
            ordinary[i].getCables();
        }
        Field registryField = CableNetwork.class.getDeclaredField("EDGE_NETWORKS");
        registryField.setAccessible(true);
        Map<?, ?> registry = (Map<?, ?>) registryField.get(null);
        assertNull(registry.get(world));
        ordinary[0].getTopologyGeneration();
        Map<?, ?> subscribers = (Map<?, ?>) registry.get(world);
        assertEquals(1, subscribers.size(), "Only a native edge subscriber joins the event loop");
        int reads = world.blockReads;
        for (int i = 0; i < 100; i++) CableNetwork.invalidate(world, 20000 + i, 64, 20000);
        assertEquals(reads, world.blockReads);
        assertEquals(1, subscribers.size());
    }

    private static final class Rig {

        final FakeWorld world;
        final BlockPos bridge, portPos;
        final Block cable = new BlockCable();
        final LabelPositionHolder labels = new LabelPositionHolder();
        final CableNetwork network;
        final RedstoneTriggerTracker tracker = new RedstoneTriggerTracker();
        final List<NamedRedstoneTrigger> triggers = Arrays.asList(
            (NamedRedstoneTrigger) parse("EVERY REDSTONE PULSE FROM signal NORTH SIDE DO FORGET END").getTriggers()
                .get(0),
            (NamedRedstoneTrigger) parse("EVERY REDSTONE FALLING FROM signal NORTH SIDE DO FORGET END").getTriggers()
                .get(0));
        CounterPort port = new CounterPort();

        Rig(BlockPos manager) throws Exception {
            this(world(), manager);
        }

        Rig(FakeWorld world, BlockPos manager) {
            this.world = world;
            bridge = manager.offset(1, 0, 0);
            portPos = manager.offset(2, 0, 0);
            world.blocks.put(bridge, cable);
            world.tiles.put(portPos, port);
            labels.addPosition("signal", portPos);
            network = new CableNetwork(world, manager);
        }

        void edit(BlockPos pos) {
            CableNetwork.invalidate(world, pos.x, pos.y, pos.z);
        }

        void pulse() {
            port.rising++;
            port.falling++;
        }

        void capture(boolean rising, boolean falling) {
            ProgramContext context = new ProgramContext(world, null, network, labels, 20, false);
            Map<NamedRedstoneTrigger, Boolean> result = tracker.capture(context, triggers, new QueryBudget());
            assertEquals(rising, Boolean.TRUE.equals(result.get(triggers.get(0))), "Rising edge");
            assertEquals(falling, Boolean.TRUE.equals(result.get(triggers.get(1))), "Falling edge");
        }
    }

    private static final class CounterPort extends TileEntityRedstonePort {

        long rising, falling, epoch = 1;
        Runnable onRefresh;

        @Override
        public boolean isInputInitialized() {
            return true;
        }

        @Override
        public long getSignalEpoch() {
            return epoch;
        }

        @Override
        public long getRisingCount(int side) {
            return rising;
        }

        @Override
        public long getFallingCount(int side) {
            return falling;
        }

        @Override
        public boolean isInputAvailable(int side) {
            return true;
        }

        @Override
        public void refreshInputs(Runnable beforeProviderCall) {
            beforeProviderCall.run();
            if (onRefresh != null) onRefresh.run();
        }
    }
}
