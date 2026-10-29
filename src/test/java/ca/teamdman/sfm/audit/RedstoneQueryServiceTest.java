package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.program.RedstoneQueryService;
import ca.teamdman.sfm.common.tileentity.TileEntityRedstonePort;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.ast.Label;
import ca.teamdman.sfml.ast.LabelAccess;
import ca.teamdman.sfml.ast.RedstoneOutputStatement;
import ca.teamdman.sfml.ast.SideQualifier;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;

/** The service validates whole native groups; hardware sampling itself has separate real-port tests. */
class RedstoneQueryServiceTest {

    private static final LabelReference SIGNAL = LabelReference.literal("signal");

    @Test
    void queriesRefreshAndReadMaximumAcrossEveryNamedPortAndSelectedFace() throws Exception {
        Fixture fixture = new Fixture();
        RecordingPort first = port(fixture, SRC), second = port(fixture, DST);
        first.levels[2] = 5;
        first.levels[3] = 13;
        second.levels[2] = 9;
        RedstoneQueryService service = service(fixture);
        assertEquals(13, service.redstone(SIGNAL, new QueryBudget()));
        assertEquals(9, service.redstone(SIGNAL.side(2), new QueryBudget()));
        assertTrue(service.powered(SIGNAL.side(2), new QueryBudget()));
        assertFalse(service.powered(SIGNAL.side(0), new QueryBudget()));
        first.levels[2] = 14;
        assertEquals(14, service.redstone(SIGNAL.side(2), new QueryBudget()));
        assertEquals(5, first.refreshes);
        assertEquals(5, second.refreshes);
    }

    @Test
    void missingIncompleteNonNativeAndSelectedUnavailableMembersNeverBecomeZeroOrPartialReadings() throws Exception {
        Fixture fixture = new Fixture();
        RecordingPort first = port(fixture, SRC);
        first.levels[2] = 15;
        assertUnavailable(service(fixture), LabelReference.literal("missing"));
        fixture.labels.addPosition("signal", DST);
        assertUnavailable(service(fixture), SIGNAL); // ordinary inventory is not a redstone port
        fixture.world.tiles.remove(DST);
        assertUnavailable(service(fixture), SIGNAL);
        RecordingPort second = port(fixture, DST);
        second.available[2] = false;
        assertUnavailable(service(fixture), SIGNAL.side(2));
        assertTrue(service(fixture).available(SIGNAL.side(3), new QueryBudget()));
        second.available[2] = true;
        int beforeUnload = second.refreshes;
        fixture.world.unloaded.add(DST);
        assertUnavailable(service(fixture), SIGNAL);
        assertEquals(beforeUnload, second.refreshes, "Unloaded members must not reach their provider");
    }

    @Test
    void disconnectedMembersAndTileReplacementAreRecheckedOnEveryObservation() throws Exception {
        Fixture fixture = new Fixture();
        RecordingPort original = port(fixture, SRC);
        original.levels[2] = 6;
        RedstoneQueryService service = service(fixture);
        assertEquals(6, service.redstone(SIGNAL.side(2), new QueryBudget()));
        RecordingPort replacement = port(fixture, SRC);
        replacement.levels[2] = 12;
        assertEquals(12, service.redstone(SIGNAL.side(2), new QueryBudget()));
        assertEquals(1, original.refreshes);
        BlockPos disconnected = new BlockPos(40, 64, 40);
        RecordingPort far = port(fixture, disconnected);
        assertUnavailable(service, SIGNAL);
        assertEquals(0, far.refreshes);
    }

    @Test
    void providerFailureMalformedLevelAndReplacementDuringSamplingFaultOrBecomeUnavailable() throws Exception {
        Fixture fixture = new Fixture();
        RecordingPort port = port(fixture, SRC);
        RedstoneQueryService service = service(fixture);
        port.failure = new IllegalStateException("provider failure");
        assertThrows(IllegalArgumentException.class, () -> service.available(SIGNAL, new QueryBudget()));
        port.failure = null;
        port.levels[2] = 16;
        assertThrows(IllegalArgumentException.class, () -> service.available(SIGNAL.side(2), new QueryBudget()));
        port.levels[2] = 0;
        port.onRefresh = () -> fixture.world.tiles.put(SRC, new TileEntity());
        assertUnavailable(service, SIGNAL);
    }

    @Test
    void budgetExhaustionAndExplicitSlotRestrictionsFailBeforeSampling() throws Exception {
        Fixture fixture = new Fixture();
        RecordingPort port = port(fixture, SRC);
        RedstoneQueryService service = service(fixture);
        QueryBudget budget = new QueryBudget();
        budget.chargeProviders(QueryBudget.MAX_PROVIDER_CALLS - 3);
        assertThrows(IllegalArgumentException.class, () -> service.available(SIGNAL, budget));
        assertEquals(0, port.refreshes);
        assertThrows(
            IllegalArgumentException.class,
            () -> service.available(SIGNAL.slots(0, Integer.MAX_VALUE), new QueryBudget()));
        assertEquals(0, port.refreshes);
        QueryBudget shared = new QueryBudget();
        int success = 0;
        try {
            while (true) {
                service.redstone(SIGNAL.side(2), shared);
                success++;
            }
        } catch (IllegalArgumentException expected) {
            assertTrue(
                expected.getMessage()
                    .contains("budget"));
        }
        assertTrue(success > 0 && success < QueryBudget.MAX_PROVIDER_CALLS);
        assertTrue(port.refreshes >= success && port.refreshes <= success + 1);
    }

    @Test
    void nativeOutputSupportsDynamicLevelsPhysicalFacesAndLeavesItemQuotasUnchanged() throws Exception {
        Fixture fixture = new Fixture();
        fixture.stock(0, 6);
        RecordingPort port = port(fixture, EXTRA);
        fixture.run(
            "LET $strength = 7 INPUT 2 FROM source OUTPUT ($strength) REDSTONE LEVEL TO signal NORTH SIDE OUTPUT TO dest");
        assertEquals(7, port.outputs[2]);
        for (int side = 0; side < 6; side++) if (side != 2) assertEquals(0, port.outputs[side]);
        assertEquals(2, count(fixture.dest));
        assertEquals(4, count(fixture.source));
        fixture.run("OUTPUT 99 REDSTONE LEVEL TO signal");
        for (int level : port.outputs) assertEquals(15, level);
    }

    @Test
    void playerWrittenHelpersCanGuardNativeObservationsAndDriveDynamicOutputs() throws Exception {
        Fixture fixture = new Fixture();
        RecordingPort port = port(fixture, EXTRA);
        port.levels[2] = 7;
        fixture.runProgram(
            "FUNCTION signals.level($port: LABEL_REF) RETURNS INTEGER DO "
                + "IF ($port.redstoneAvailable()) THEN RETURN $port.side(\"north\").redstone() ELSE RETURN 0 END END "
                + "EVERY 20 TICKS DO OUTPUT (signals.level(LABEL(\"signal\"))) REDSTONE LEVEL TO signal EAST SIDE END");
        assertEquals(7, port.outputs[5]);
        assertEquals(2, port.refreshes);
        assertEquals(1, port.outputWrites);
    }

    @Test
    void outputRejectsInvalidSidesOrStrengthBeforeMutationAndSkipsUnloadedDisconnectedTargets() throws Exception {
        Fixture fixture = new Fixture();
        RecordingPort port = port(fixture, SRC);
        ProgramContext context = context(fixture);
        RedstoneOutputStatement invalidSide = new RedstoneOutputStatement(
            new LabelAccess(Arrays.asList(new Label("signal")), SideQualifier.NULL, null, null),
            4,
            false);
        assertThrows(IllegalArgumentException.class, () -> invalidSide.tick(context));
        assertEquals(0, port.outputWrites);
        assertThrows(IllegalArgumentException.class, () -> fixture.run("OUTPUT (16) REDSTONE LEVEL TO signal"));
        assertEquals(0, port.outputWrites);
        fixture.world.unloaded.add(SRC);
        fixture.run("OUTPUT 4 REDSTONE LEVEL TO signal");
        assertEquals(0, port.outputWrites);
        fixture.world.unloaded.remove(SRC);
        BlockPos farPosition = new BlockPos(40, 64, 40);
        RecordingPort far = port(fixture, farPosition);
        fixture.run("OUTPUT 4 REDSTONE LEVEL TO signal");
        assertEquals(1, port.outputWrites);
        assertEquals(0, far.outputWrites);
    }

    @Test
    void nativeWriteAttemptsShareOneBoundAcrossLoopsAndDueTriggersEvenWhenUnchanged() throws Exception {
        Fixture fixture = new Fixture();
        RecordingPort port = port(fixture, EXTRA);
        port.reportChanged = false;
        IllegalArgumentException fault = assertThrows(
            IllegalArgumentException.class,
            () -> fixture.runProgram(
                "EVERY 20 TICKS DO FOR $i = 0 TO 127 DO OUTPUT 3 REDSTONE LEVEL TO signal NORTH SIDE END END "
                    + "EVERY 20 TICKS DO FOR $j = 0 TO 128 DO OUTPUT 3 REDSTONE LEVEL TO signal NORTH SIDE END END"));
        assertTrue(
            fault.getMessage()
                .contains("write budget"),
            fault.getMessage());
        assertEquals(256, port.outputWrites);
        assertEquals(3, port.outputs[2], "Earlier completed writes remain in place after the budget fault");
        fixture.run("OUTPUT 4 REDSTONE LEVEL TO signal NORTH SIDE");
        assertEquals(257, port.outputWrites, "A fresh invocation receives a new allowance");
    }

    private static void assertUnavailable(RedstoneQueryService service, LabelReference label) {
        assertFalse(service.available(label, new QueryBudget()));
        assertThrows(IllegalArgumentException.class, () -> service.redstone(label, new QueryBudget()));
        assertThrows(IllegalArgumentException.class, () -> service.powered(label, new QueryBudget()));
    }

    private static RecordingPort port(Fixture fixture, BlockPos pos) {
        RecordingPort port = new RecordingPort();
        fixture.world.tiles.put(pos, port);
        fixture.labels.addPosition("signal", pos);
        return port;
    }

    private static ProgramContext context(Fixture fixture) {
        return new ProgramContext(
            fixture.world,
            null,
            new CableNetwork(fixture.world, MANAGER),
            fixture.labels,
            20,
            false);
    }

    private static RedstoneQueryService service(Fixture fixture) {
        return new RedstoneQueryService(context(fixture));
    }

    private static final class RecordingPort extends TileEntityRedstonePort {

        final int[] levels = new int[6], outputs = new int[6];
        final boolean[] available = { true, true, true, true, true, true };
        int refreshes, outputWrites;
        boolean reportChanged = true;
        RuntimeException failure;
        Runnable onRefresh;

        @Override
        public void refreshInputs() {
            refreshes++;
            if (failure != null) throw failure;
            if (onRefresh != null) onRefresh.run();
        }

        @Override
        public void refreshInputs(Runnable beforeProviderCall) {
            beforeProviderCall.run();
            refreshInputs();
        }

        @Override
        public int getInputLevel(int side) {
            return levels[side];
        }

        @Override
        public boolean isInputAvailable(int side) {
            return available[side];
        }

        @Override
        public boolean setOutput(List<ForgeDirection> sides, int level) {
            outputWrites++;
            for (ForgeDirection side : new ArrayList<>(sides)) outputs[side.ordinal()] = level;
            return reportChanged;
        }
    }
}
