package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraftforge.common.util.ForgeDirection;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityRedstonePort;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;
import cpw.mods.fml.common.gameevent.TickEvent;

class SfmlPulseTest {

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
    void helperRunsImmediatelyAndContinuesThroughTheNextStatementAndProcedure() throws Exception {
        Program p = program(
            "void emit(label_ref port) { pulse(port, 4, 8) } "
                + "every 1 ticks { pulse(label(\"first\").side(\"north\"), 2) "
                + "emit(label(\"second\")) pulse(label(\"third\"), 1) }");
        AuditRegressionTest.Fixture f = new AuditRegressionTest.Fixture();
        RecordingPort port = new RecordingPort();
        f.world.tiles.put(AuditRegressionTest.SRC, port);
        for (String name : Arrays.asList("first", "second", "third"))
            f.labels.addPosition(name, AuditRegressionTest.SRC);
        ProgramContext context = new ProgramContext(
            f.world,
            null,
            new CableNetwork(f.world, AuditRegressionTest.MANAGER),
            f.labels,
            1,
            false);
        p.tick(context);
        assertEquals(Arrays.asList("4:2:15", "63:4:8", "63:1:15"), port.pulses);
        assertEquals(
            3,
            context.getInvocationQueryBudget()
                .getPositions());
        program(p.toString());
    }

    @Test
    void argumentsAndActionPlacementAreCompilerChecked() {
        for (String body : Arrays.asList(
            "pulse(label(\"a\"))",
            "pulse(\"a\", 2)",
            "pulse(label(\"a\"), true)",
            "pulse(label(\"a\"), 2, false)",
            "let a = pulse(label(\"a\"), 2)",
            "pulse(label(\"a\"), 1, 15, 3)")) {
            assertFalse(
                new ProgramBuilder("every 1 ticks { " + body + " }").build()
                    .isSuccess(),
                body);
        }
        assertFalse(
            new ProgramBuilder("integer f() { pulse(label(\"a\"), 2) return 0 } every 1 ticks {}").build()
                .isSuccess());
        assertThrows(
            IllegalArgumentException.class,
            () -> new DataCompiler().declareState("bad", "pulse(label(\"a\"), 2)", 1, 0));
    }

    @Test
    void existingExactNamePlayerFunctionStillTakesPrecedence() {
        Program p = program("integer pulse(integer n) { return n + 1 } every 1 ticks { let n = pulse(1) }");
        assertDoesNotThrow(() -> p.tick(new ProgramContext(null, null, null, null, 1, false)));
    }

    @Test
    void nativeWriteLimitAndServerThreadChecksApplyBeforePortMutation() throws Exception {
        AuditRegressionTest.Fixture f = new AuditRegressionTest.Fixture();
        RecordingPort port = new RecordingPort();
        f.world.tiles.put(AuditRegressionTest.SRC, port);
        ProgramContext context = new ProgramContext(
            f.world,
            null,
            new CableNetwork(f.world, AuditRegressionTest.MANAGER),
            f.labels,
            1,
            false);
        LabelReference source = LabelReference.literal("source");
        for (int i = 0; i < ProgramContext.MAX_NATIVE_REDSTONE_WRITES; i++) context.pulseRedstone(source, 2, 15);
        assertThrows(IllegalArgumentException.class, () -> context.pulseRedstone(source, 2, 15));
        assertEquals(ProgramContext.MAX_NATIVE_REDSTONE_WRITES, port.pulses.size());
        ServerUiTasks.clear();
        assertThrows(IllegalArgumentException.class, () -> context.pulseRedstone(source, 2, 15));
        assertEquals(ProgramContext.MAX_NATIVE_REDSTONE_WRITES, port.pulses.size());
    }

    @Test
    void serviceRejectsInvalidDurationsSlotsAndStrengthsWithoutAWorld() {
        ProgramContext context = new ProgramContext(null, null, null, null, 1, false);
        LabelReference port = LabelReference.literal("a");
        for (long ticks : new long[] { 0, -1, Long.MAX_VALUE })
            assertThrows(IllegalArgumentException.class, () -> context.pulseRedstone(port, ticks, 15));
        assertThrows(IllegalArgumentException.class, () -> context.pulseRedstone(port.slots(0, 0), 2, 15));
        assertThrows(IllegalArgumentException.class, () -> context.pulseRedstone(port, 2, Long.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> context.pulseRedstone(port, 2, 15));
    }

    private static Program program(String source) {
        ProgramBuildResult result = new ProgramBuilder(source).build();
        assertTrue(result.isSuccess(), result.getErrorMessage());
        return result.getProgram();
    }

    private static final class RecordingPort extends TileEntityRedstonePort {

        final List<String> pulses = new ArrayList<>();

        @Override
        public boolean pulse(List<ForgeDirection> sides, long ticks, int strength) {
            int mask = 0;
            for (ForgeDirection side : sides) mask |= 1 << side.ordinal();
            pulses.add(mask + ":" + ticks + ":" + strength);
            return true;
        }
    }
}
