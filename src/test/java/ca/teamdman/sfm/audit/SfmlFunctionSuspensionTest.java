package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.CompiledFunction;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

/** Compare the proven path with the previous conservative path through real trigger scheduling. */
class SfmlFunctionSuspensionTest {

    @Test
    void completedFastRefCallIsMemoizedAcrossALaterWaitingOperand() throws Exception {
        String source = "STATE calls = 0 STATE answer = 0 " + "integer bump(ref integer n) { n = n + 1; return n; } "
            + "integer later() { wait(2); return 5; } "
            + "EVERY 100 TICKS { answer = bump(ref calls) + later() + bump(ref calls); }";
        for (boolean conservative : new boolean[] { false, true }) {
            Run run = new Run(source, conservative);
            if (!conservative) {
                assertFalse(
                    run.program.getFunctions()
                        .get(0)
                        .maySuspend());
                assertTrue(
                    run.program.getFunctions()
                        .get(1)
                        .maySuspend());
            }
            run.tick(100);
            assertEquals(1, run.value(0));
            assertEquals(0, run.value(1));
            run.tick(102);
            assertEquals(2, run.value(0));
            assertEquals(8, run.value(1));
            assertEquals(0, run.sleeping());
        }
    }

    @Test
    void waitingArgumentFinishesBeforeFastEntryWithoutRecapturingEarlierRef() throws Exception {
        String source = "STATE values = [1,2] STATE cursor = 0 STATE calls = 0 "
            + "integer later() { wait(2); return 7; } "
            + "void apply(ref integer cell, integer value, ref integer count) { count = count + 1; cell = value; } "
            + "EVERY 100 TICKS { apply(ref values[cursor], later(), ref calls); } "
            + "EVERY 1 TICKS { cursor = 1; }";
        for (boolean conservative : new boolean[] { false, true }) {
            Run run = new Run(source, conservative);
            run.tick(100);
            assertEquals(0, run.value(2));
            run.tick(102);
            assertEquals(1, run.value(2));
            assertEquals(
                7,
                run.state.values[0].get(0)
                    .asLong());
            assertEquals(
                2,
                run.state.values[0].get(1)
                    .asLong());
        }
    }

    @Test
    void copiedArraysForwardedWholeAndIndexedRefsKeepTheirEpochBehaviorAcrossWait() throws Exception {
        String source = "STATE answer = 0 " + "void increment(ref integer n) { n = n + 1; } "
            + "integer edit(integer[] copied, ref integer[] target) { push(ref copied, 9); push(ref target, 4); return len(copied) * 10 + len(target); } "
            + "integer forward(ref integer[] target) { return edit(target, ref target); } "
            + "EVERY 100 TICKS { let values = array([1,2]); increment(ref values[0]); "
            + "answer = forward(ref values) * 100 + values[0] * 10 + len(values); wait(1); answer = answer + len(values); }";
        for (boolean conservative : new boolean[] { false, true }) {
            Run run = new Run(source, conservative);
            run.tick(100);
            assertEquals(3323, run.value(0));
            run.tick(101);
            assertEquals(3326, run.value(0));
            assertEquals(0, run.sleeping());
        }
        String stale = "LET values = array([1,2]) "
            + "void invalidate(ref integer cell, ref integer[] whole) { push(ref whole, 9); cell = 8; } "
            + "EVERY 100 TICKS { invalidate(ref values[0], ref values); wait(1); }";
        for (boolean conservative : new boolean[] { false, true }) {
            Run run = new Run(stale, conservative);
            IllegalArgumentException failure = assertThrows(IllegalArgumentException.class, () -> run.tick(100));
            assertTrue(
                failure.getMessage()
                    .contains("Indexed reference invalidated"),
                failure.getMessage());
            DataValue retained = run.manager.getProgramGlobals(run.program)
                .read(0);
            assertEquals(3, retained.size(), "The earlier structural mutation remains committed");
            assertEquals(
                1,
                retained.get(0)
                    .asLong());
            assertEquals(
                0,
                run.context.getDataRuntime()
                    .getFunctionDepth());
            assertEquals(0, run.sleeping());
        }
    }

    @Test
    void allLoopStylesAndEarlyReturnAgreeWithConservativeExecution() throws Exception {
        String source = "STATE answer = 0 " + "integer work() { let total = 0; for i = 1 to 3 { total = total + i; } "
            + "for value in [1,2,3] { total = total + value; } "
            + "for (index, value) in array([1,2,3]) { total = total + index + value; } "
            + "for (let i = 0; i < 3; i = i + 1) { total = total + i; } "
            + "let n = 0; while (n < 3) { total = total + n; n = n + 1; } "
            + "for (;;) { return total; } return 999; } "
            + "EVERY 100 TICKS { answer = work(); wait(1); answer = answer + 1; }";
        for (boolean conservative : new boolean[] { false, true }) {
            Run run = new Run(source, conservative);
            run.tick(100);
            assertEquals(27, run.value(0));
            run.tick(101);
            assertEquals(28, run.value(0));
            assertEquals(
                0,
                run.context.getDataRuntime()
                    .getFunctionDepth());
        }
    }

    @Test
    void fastVoidNativeInputsRetainPerIterationAllowancesAndConserveInventory() throws Exception {
        String source = "void gather() { for i = 1 to 2 { INPUT 1 FROM \"source\"; } "
            + "for value in [1,2] { INPUT 1 FROM \"source\"; } "
            + "for (index, value) in [1,2] { INPUT 1 FROM \"source\"; } "
            + "for (let i = 0; i < 2; i = i + 1) { INPUT 1 FROM \"source\"; } "
            + "let n = 0; while (n < 2) { INPUT 1 FROM \"source\"; n = n + 1; } } "
            + "EVERY 100 TICKS { gather(); wait(1); OUTPUT TO \"dest\"; }";
        for (boolean conservative : new boolean[] { false, true }) {
            Run run = new Run(source, conservative);
            run.fixture.stock(0, 17);
            run.tick(100);
            assertEquals(17, count(run.fixture.source));
            assertEquals(0, count(run.fixture.dest));
            run.tick(101);
            assertEquals(7, count(run.fixture.source));
            assertEquals(10, count(run.fixture.dest));
            assertEquals(
                0,
                run.context.getDataRuntime()
                    .getFunctionDepth());
        }
    }

    @Test
    void provenRuntimePlansRetainNoCompilerOrInvocationState() throws Exception {
        Run run = new Run(
            "integer leaf(integer n) { return n + 1; } integer outer() { let total = 0; "
                + "for (index, value) in [1,2] { total = total + leaf(value); } return total; } "
                + "EVERY 100 TICKS { let answer = outer(); wait(1); }",
            false);
        for (CompiledFunction function : run.program.getFunctions()) assertFalse(function.maySuspend());
        SfmlFunctionTest.assertPlanOwnership(run.program, new IdentityHashMap<>());
        run.tick(100);
        SfmlFunctionTest.assertPlanOwnership(run.program, new IdentityHashMap<>());
    }

    private static final class Memory implements StateAccess {

        final DataValue[] values;

        Memory(Program program) {
            values = new DataValue[program.getStateSchema()
                .size()];
            for (int i = 0; i < values.length; i++) values[i] = program.getStateSchema()
                .getDeclarations()
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

    private static final class Run {

        final Fixture fixture = new Fixture();
        final TileEntityManager manager = RecoveryFixtures.manager(fixture);
        final Program program;
        final Memory state;
        ProgramContext context;

        Run(String source, boolean conservative) throws Exception {
            ProgramBuildResult result = new ProgramBuilder(source).build();
            assertTrue(result.isSuccess(), result.getErrorMessage());
            program = result.getProgram();
            state = new Memory(program);
            if (conservative) {
                Field field = CompiledFunction.class.getDeclaredField("maySuspend");
                field.setAccessible(true);
                for (CompiledFunction function : program.getFunctions()) field.setBoolean(function, true);
            }
        }

        void tick(long now) {
            fixture.world.time = now;
            context = new ProgramContext(
                fixture.world,
                manager,
                new CableNetwork(fixture.world, MANAGER),
                fixture.labels,
                now,
                false);
            context.setStateAccess(state);
            program.tick(context);
        }

        long value(int slot) {
            return state.values[slot].asLong();
        }

        int sleeping() {
            return manager.getProgramExecution(program)
                .sleepingCount();
        }
    }
}
