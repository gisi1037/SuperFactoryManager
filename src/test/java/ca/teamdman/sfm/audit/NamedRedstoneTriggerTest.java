package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.program.RedstoneTriggerTracker;
import ca.teamdman.sfm.common.tileentity.TileEntityRedstonePort;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.ast.NamedRedstoneTrigger;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ProgramStateSchema;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuilder;
import ca.teamdman.sfml.program_builder.VisualCapabilityReport.Kind;

/** End-to-end eligibility, lifecycle and ordering tests with notification-maintained port counters. */
class NamedRedstoneTriggerTest {

    @Test
    void aliasesSidesAndQuotedLabelsRoundTripAndInvalidQualifiersAreRejected() {
        for (String edge : new String[] { "PULSE", "RISING", "FALLING" }) {
            Program program = parse("EVERY REDSTONE " + edge + " FROM \"my signal\" NORTH, EAST SIDE DO FORGET END");
            NamedRedstoneTrigger trigger = (NamedRedstoneTrigger) program.getTriggers()
                .get(0);
            assertEquals((1 << 2) | (1 << 5), trigger.getSideMask());
            assertEquals(edge.equals("FALLING"), trigger.isFalling());
            assertEquals(
                trigger.toString(),
                parse(trigger.toString()).getTriggers()
                    .get(0)
                    .toString());
        }
        for (String qualifier : new String[] { "ROUND ROBIN", "SLOTS 0-2147483647", "NULL SIDE", "FRONT SIDE" }) {
            assertFalse(
                new ProgramBuilder("EVERY REDSTONE PULSE FROM signal " + qualifier + " DO END").build()
                    .isSuccess(),
                qualifier);
        }
        parse("EVERY 20 TICKS DO INPUT rising:falling FROM rising OUTPUT TO falling END");
    }

    @Test
    void namedTriggersRemainTextOnlyWithoutLosingSource() {
        String source = "EVERY REDSTONE FALLING FROM sensor NORTH SIDE DO FORGET END";
        BlueprintCompiler.ImportResult result = BlueprintCompiler.inspectText(source);
        assertNull(result.graph);
        assertEquals(
            Kind.NAMED_REDSTONE_TRIGGER,
            result.report.getBlockers()
                .get(0).kind);
        assertEquals(
            1,
            result.report.getBlockers()
                .get(0).line);
        assertThrows(BlueprintCompiler.ImportException.class, () -> BlueprintCompiler.importText(source));
    }

    @Test
    void initialHighIsOnlyBaselineAndShortPulsesDeliverBothEdgesOnce() throws Exception {
        Rig rig = new Rig(
            "STATE $up = 0 STATE $down = 0 " + "EVERY REDSTONE PULSE FROM signal NORTH SIDE DO SET $up = $up + 1 END "
                + "EVERY REDSTONE FALLING FROM signal NORTH SIDE DO SET $down = $down + 1 END");
        CounterPort port = rig.port(SRC);
        port.rising[2] = 8;
        rig.tick();
        assertEquals(0, rig.value(0));
        port.rising[2] += 3;
        port.falling[2] += 3;
        rig.tick();
        assertEquals(1, rig.value(0));
        assertEquals(1, rig.value(1));
        rig.tick();
        assertEquals(1, rig.value(0));
        assertEquals(1, rig.value(1));
    }

    @Test
    void heldHighMemberDoesNotHideAnotherPortsEdgeAndIdleDoesNotResamplePower() throws Exception {
        Rig rig = new Rig(risingProgram());
        CounterPort first = rig.port(SRC), second = rig.port(DST);
        first.rising[2] = 1;
        rig.tick();
        rig.tick();
        assertEquals(1, first.refreshes);
        assertEquals(1, second.refreshes);
        second.rising[2]++;
        rig.tick();
        assertEquals(1, rig.value(0));
        assertEquals(2, first.refreshes, "Every member of an edge-candidate group is freshly validated");
        assertEquals(2, second.refreshes);
        second.rising[5]++;
        rig.tick();
        assertEquals(1, rig.value(0), "Unselected faces cannot trigger the program");
    }

    @Test
    void repeatedTriggersAndIndependentControllersObserveWithoutConsumingEdges() throws Exception {
        Rig rig = new Rig("STATE $hits = 0 " + risingBody() + risingBody());
        CounterPort port = rig.port(SRC);
        RedstoneTriggerTracker other = new RedstoneTriggerTracker();
        MemoryState otherState = new MemoryState(rig.program.getStateSchema());
        rig.tick();
        rig.tick(other, otherState);
        port.rising[2]++;
        rig.tick();
        rig.tick(other, otherState);
        assertEquals(2, rig.value(0));
        assertEquals(
            2,
            otherState.read(0)
                .asLong());
    }

    @Test
    void allNamedEligibilityIsFrozenBeforeTimerOutputsButQueriesSeeTheNewValue() throws Exception {
        Rig rig = new Rig(
            "STATE $hits = 0 STATE $level = 0 " + "EVERY 20 TICKS DO OUTPUT 7 REDSTONE LEVEL TO signal NORTH SIDE "
                + "SET $level = LABEL(\"signal\").side(\"north\").redstone() END "
                + risingBody());
        CounterPort port = rig.port(SRC);
        rig.tick();
        assertEquals(7, rig.value(1));
        assertEquals(0, rig.value(0));
        rig.tick();
        assertEquals(1, rig.value(0), "An earlier body cannot change eligibility already captured this invocation");
        rig.tick();
        assertEquals(1, rig.value(0));
        assertEquals(1, port.rising[2]);
    }

    @Test
    void incompleteAndUnavailableGroupsAdvanceValidMembersWithoutReplayingAnEdge() throws Exception {
        Rig rig = new Rig(risingProgram());
        CounterPort first = rig.port(SRC), second = rig.port(DST);
        rig.tick();
        first.rising[2]++;
        second.onRefresh = () -> second.available = 0;
        rig.tick();
        assertEquals(0, rig.value(0));
        second.onRefresh = null;
        second.available = 63;
        rig.tick();
        assertEquals(0, rig.value(0));
        rig.fixture.labels.addPosition("signal", EXTRA); // label member has no tile
        first.rising[2]++;
        rig.tick();
        assertEquals(0, rig.value(0));
        rig.fixture.labels.removePosition("signal", EXTRA);
        rig.tick();
        assertEquals(0, rig.value(0));
    }

    @Test
    void unloadingReplacementAndEpochChangesAreBaselinesNotFabricatedPulses() throws Exception {
        Rig rig = new Rig(risingProgram());
        CounterPort port = rig.port(SRC);
        rig.tick();
        rig.fixture.world.unloaded.add(SRC);
        port.rising[2]++;
        rig.tick();
        rig.fixture.world.unloaded.remove(SRC);
        rig.tick();
        assertEquals(0, rig.value(0));
        port.epoch++;
        port.rising[2]++;
        rig.tick();
        assertEquals(0, rig.value(0));
        port = rig.port(SRC);
        port.rising[2] = 100;
        rig.tick();
        assertEquals(0, rig.value(0));
        port.rising[2]++;
        rig.tick();
        assertEquals(1, rig.value(0));
    }

    @Test
    void topologyInvalidationAndExplicitResumeResetRebaseButPeriodicScansDoNot() throws Exception {
        Rig rig = new Rig(risingProgram());
        CounterPort port = rig.port(SRC);
        rig.tick();
        port.rising[2]++;
        CableNetwork.invalidate(rig.fixture.world);
        rig.tick();
        assertEquals(0, rig.value(0));
        port.rising[2]++;
        rig.fixture.world.time += 21;
        rig.tick();
        assertEquals(1, rig.value(0));
        port.rising[2]++;
        rig.tracker.reset();
        rig.tick();
        assertEquals(1, rig.value(0));
    }

    @Test
    void replacementDuringRefreshIsUnavailableAndProviderFailureDoesNotCommitBaselines() throws Exception {
        Rig rig = new Rig(risingProgram());
        CounterPort port = rig.port(SRC);
        rig.tick();
        port.rising[2]++;
        port.failure = new IllegalStateException("bad provider");
        assertThrows(IllegalArgumentException.class, rig::tick);
        assertEquals(0, rig.value(0));
        port.failure = null;
        rig.tick();
        assertEquals(1, rig.value(0));
        port.rising[2]++;
        port.onRefresh = () -> rig.fixture.world.tiles.put(SRC, new TileEntity());
        rig.tick();
        assertEquals(1, rig.value(0));
    }

    @Test
    void exhaustedSnapshotBudgetPreventsEarlierTimerActionsAndSharesAllowanceWithQueries() throws Exception {
        Rig rig = new Rig("STATE $hits = 0 EVERY 20 TICKS DO SET $hits = $hits + 1 END " + risingBody());
        rig.port(SRC);
        ProgramContext context = rig.context(rig.tracker, rig.state);
        context.getInvocationQueryBudget()
            .chargeProviders(QueryBudget.MAX_PROVIDER_CALLS);
        assertThrows(IllegalArgumentException.class, () -> rig.program.tick(context));
        assertEquals(0, rig.value(0));
        rig.tick();
        assertEquals(1, rig.value(0));
        assertTrue(
            context.getInvocationQueryBudget()
                .getProviderCalls() <= QueryBudget.MAX_PROVIDER_CALLS);
    }

    @Test
    void refreshedMissedEdgeValidatesEveryMemberOfOverlappingGroupsBeforeDelivery() throws Exception {
        Rig rig = new Rig(
            "STATE $north = 0 STATE $south = 0 "
                + "EVERY REDSTONE PULSE FROM edge_a NORTH SIDE DO SET $north = $north + 1 END "
                + "EVERY REDSTONE PULSE FROM edge_b SOUTH SIDE DO SET $south = $south + 1 END");
        CounterPort first = rig.port(SRC), second = rig.port(DST);
        rig.fixture.labels.addPosition("edge_a", SRC);
        rig.fixture.labels.addPosition("edge_b", SRC);
        rig.fixture.labels.addPosition("edge_b", DST);
        rig.tick();
        first.rising[2]++;
        first.onRefresh = () -> first.rising[3]++;
        second.onRefresh = () -> second.available = 0;
        rig.tick();
        assertEquals(1, rig.value(0));
        assertEquals(0, rig.value(1), "A newly discovered edge cannot bypass validation of the overlapping group");
        assertEquals(2, second.refreshes);
    }

    @Test
    void executableSaveRebasesEdgesButCommentsAndRejectedEditsPreserveThem() throws Exception {
        Rig rig = new Rig(risingProgram());
        CounterPort port = rig.port(SRC);
        ca.teamdman.sfm.common.tileentity.TileEntityManager manager = RecoveryFixtures.manager(rig.fixture);
        assertNull(manager.trySaveProgram(risingProgram()));
        RedstoneTriggerTracker observer = manager.getRedstoneTriggerTracker();
        manager.getCompiledProgram()
            .tick(rig.context(observer, rig.state));
        port.rising[2]++;
        String changed = risingProgram().replace("+ 1", "+ 2");
        assertNull(manager.trySaveProgram(changed));
        manager.getCompiledProgram()
            .tick(rig.context(observer, rig.state));
        assertEquals(0, rig.value(0), "Old pending edges must not enter a replacement executable");
        port.rising[2]++;
        Program executable = manager.getCompiledProgram();
        assertNull(manager.trySaveProgram(changed + " -- comment"));
        assertSame(executable, manager.getCompiledProgram());
        manager.getCompiledProgram()
            .tick(rig.context(observer, rig.state));
        assertEquals(2, rig.value(0));
        port.rising[2]++;
        assertNotNull(manager.trySaveProgram("invalid"));
        assertSame(executable, manager.getCompiledProgram());
        manager.getCompiledProgram()
            .tick(rig.context(observer, rig.state));
        assertEquals(4, rig.value(0));
    }

    private static String risingProgram() {
        return "STATE $hits = 0 " + risingBody();
    }

    private static String risingBody() {
        return "EVERY REDSTONE PULSE FROM signal NORTH SIDE DO SET $hits = $hits + 1 END ";
    }

    private static final class Rig {

        final Fixture fixture = new Fixture();
        final CableNetwork network = new CableNetwork(fixture.world, MANAGER);
        final RedstoneTriggerTracker tracker = new RedstoneTriggerTracker();
        final Program program;
        final MemoryState state;

        Rig(String source) throws Exception {
            program = parse(source);
            state = new MemoryState(program.getStateSchema());
        }

        CounterPort port(BlockPos pos) {
            CounterPort port = new CounterPort();
            fixture.add("signal", pos, port);
            return port;
        }

        ProgramContext context(RedstoneTriggerTracker observer, MemoryState values) {
            ProgramContext context = new ProgramContext(fixture.world, null, network, fixture.labels, 20, false);
            context.setRedstoneTracker(observer);
            context.setStateAccess(values);
            return context;
        }

        void tick() {
            tick(tracker, state);
        }

        void tick(RedstoneTriggerTracker observer, MemoryState values) {
            program.tick(context(observer, values));
        }

        long value(int slot) {
            return state.read(slot)
                .asLong();
        }
    }

    private static final class MemoryState implements StateAccess {

        private final DataValue[] values;

        MemoryState(ProgramStateSchema schema) {
            values = new DataValue[schema.size()];
            for (int i = 0; i < values.length; i++) values[i] = schema.getDeclarations()
                .get(i)
                .getDefaultValue();
        }

        @Override
        public DataValue read(int slot) {
            return values[slot];
        }

        @Override
        public void write(int slot, DataValue value) {
            values[slot] = value;
        }
    }

    private static final class CounterPort extends TileEntityRedstonePort {

        final long[] rising = new long[6], falling = new long[6];
        final int[] levels = new int[6];
        long epoch = 1;
        int available = 63, refreshes;
        Runnable onRefresh;
        RuntimeException failure;

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
            return rising[side];
        }

        @Override
        public long getFallingCount(int side) {
            return falling[side];
        }

        @Override
        public boolean isInputAvailable(int side) {
            return (available & 1 << side) != 0;
        }

        @Override
        public int getInputLevel(int side) {
            return levels[side];
        }

        @Override
        public void refreshInputs(Runnable beforeProviderCall) {
            beforeProviderCall.run();
            refreshes++;
            if (failure != null) throw failure;
            if (onRefresh != null) onRefresh.run();
        }

        @Override
        public boolean setOutput(List<ForgeDirection> sides, int level) {
            boolean changed = false;
            for (ForgeDirection direction : sides) {
                int side = direction.ordinal();
                if (levels[side] == level) continue;
                if (levels[side] == 0 && level > 0) rising[side]++;
                if (levels[side] > 0 && level == 0) falling[side]++;
                levels[side] = level;
                changed = true;
            }
            return changed;
        }
    }
}
