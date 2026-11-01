package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.IdentityHashMap;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class SfmlTimerOffsetTest {

    @Test
    void offsetIsChosenOnceWithoutIdleEvaluationsAndKeepsTheSameCadence() throws Exception {
        Run run = new Run(
            build(
                "state phase=3 state runs=0 integer choose(integer value){return value;} "
                    + "every 10 plus (choose(phase)) ticks {runs=runs+1;phase=-1;}"));
        for (int tick = 0; tick <= 35; tick++) run.tick(tick, tick);
        assertEquals(-1, run.value(0), "Subsequent polls must not re-evaluate the now-invalid offset");
        assertEquals(4, run.value(1));
        SfmlFunctionTest.assertPlanOwnership(run.program, new IdentityHashMap<>());
    }

    @Test
    void sharedCompiledProgramsKeepIndependentManagerOffsetsAndResetExplicitly() throws Exception {
        Program program = build("state phase=3 state runs=0 every 10 + (phase) ticks {runs=runs+1;phase=7;}");
        Run first = new Run(program), second = new Run(program);
        second.values[0] = DataValue.integer(7);
        for (int tick = 0; tick <= 35; tick++) {
            first.tick(tick, tick);
            second.tick(tick, tick);
        }
        assertEquals(4, first.value(1));
        assertEquals(3, second.value(1));
        first.manager.getProgramExecution(program)
            .clear();
        first.tick(37, 37);
        assertEquals(5, first.value(1));
    }

    @Test
    void existingNumericOffsetsRemainIdenticalAndSecondsAndGlobalClocksStillWork() throws Exception {
        Run numeric = new Run(build("state runs=0 every 10 plus 3 ticks {runs=runs+1;}"));
        Run expression = new Run(build("state runs=0 every 10 plus (3) ticks {runs=runs+1;}"));
        for (int tick = 0; tick <= 35; tick++) {
            numeric.tick(tick, tick);
            expression.tick(tick, tick);
            assertEquals(numeric.value(0), expression.value(0));
        }
        Run global = new Run(build("state runs=0 every 10g + (3) ticks {runs=runs+1;}"));
        global.tick(103, 1);
        assertEquals(1, global.value(0));
        Run seconds = new Run(build("state runs=0 every 2 plus (1) seconds {runs=runs+1;}"));
        seconds.tick(20, 20);
        seconds.tick(60, 60);
        assertEquals(2, seconds.value(0));
    }

    @Test
    void waitResumesWithoutOverlappingOrRerollingAndBreakKeepsThePhase() throws Exception {
        Run run = new Run(
            build(
                "state started=0 state done=0 every 10 plus (3) ticks {started=started+1;wait(12);done=done+1;break;}"));
        run.tick(0, 0);
        run.tick(3, 3);
        run.tick(13, 13);
        assertEquals(1, run.value(0));
        assertEquals(0, run.value(1));
        run.tick(15, 15);
        assertEquals(1, run.value(1));
        run.tick(23, 23);
        assertEquals(2, run.value(0));
    }

    @Test
    void staggeredOffsetsSpreadEqualWorkWithoutReducingExecutionCount() throws Exception {
        Program staggered = build("state phase=0 state runs=0 every 20 plus (phase) ticks {runs=runs+1;}");
        Program plain = build("state phase=0 state runs=0 every 20 ticks {runs=runs+1;}");
        Run[] spread = new Run[20], aligned = new Run[20];
        for (int i = 0; i < 20; i++) {
            spread[i] = new Run(staggered);
            spread[i].values[0] = DataValue.integer(i);
            aligned[i] = new Run(plain);
        }
        long spreadTotal = 0, alignedTotal = 0, spreadPeak = 0, alignedPeak = 0;
        for (int tick = 0; tick < 60; tick++) {
            long spreadNow = 0, alignedNow = 0;
            for (int i = 0; i < 20; i++) {
                long before = spread[i].value(1), otherBefore = aligned[i].value(1);
                spread[i].tick(tick, tick);
                aligned[i].tick(tick, tick);
                spreadNow += spread[i].value(1) - before;
                alignedNow += aligned[i].value(1) - otherBefore;
            }
            spreadTotal += spreadNow;
            alignedTotal += alignedNow;
            spreadPeak = Math.max(spreadPeak, spreadNow);
            alignedPeak = Math.max(alignedPeak, alignedNow);
        }
        assertEquals(60, spreadTotal);
        assertEquals(alignedTotal, spreadTotal);
        assertEquals(20, alignedPeak);
        assertEquals(1, spreadPeak, "Deterministic distinct phases; random choices can still collide");
    }

    @Test
    void invalidOffsetsAndSuspendingHeadersAreRejectedAndVisualRefusesLossyImport() throws Exception {
        for (String source : new String[] { "every 10 plus (true) ticks {}",
            "integer phase(){wait(1);return 0;} every 10 plus (phase()) ticks {}" })
            assertFalse(
                new ProgramBuilder(source).build()
                    .isSuccess(),
                source);
        for (String offset : new String[] { "-1", "2147483648" }) {
            Run run = new Run(build("every 10 plus (" + offset + ") ticks {}"));
            assertThrows(IllegalArgumentException.class, () -> run.tick(0, 0));
        }
        assertFalse(BlueprintCompiler.inspectText("every 20 plus (random(0,19)) ticks {}").report.isAvailable());
    }

    private static Program build(String source) {
        var built = new ProgramBuilder(source).build();
        assertTrue(built.isSuccess(), built.getErrorMessage());
        return built.getProgram();
    }

    private static final class Run implements StateAccess {

        final Fixture fixture = new Fixture();
        final TileEntityManager manager;
        final Program program;
        final DataValue[] values;

        Run(Program program) throws Exception {
            this.program = program;
            manager = RecoveryFixtures.manager(fixture);
            values = new DataValue[program.getStateSchema()
                .size()];
            for (int i = 0; i < values.length; i++) values[i] = program.getStateSchema()
                .getDeclarations()
                .get(i)
                .getDefaultValue();
        }

        void tick(long world, long tick) {
            fixture.world.time = world;
            ProgramContext context = new ProgramContext(
                fixture.world,
                manager,
                new CableNetwork(fixture.world, MANAGER),
                fixture.labels,
                tick,
                false);
            context.setStateAccess(this);
            program.tick(context);
        }

        long value(int slot) {
            return values[slot].asLong();
        }

        public DataValue read(int slot) {
            return values[slot];
        }

        public void write(int slot, DataValue value) {
            values[slot] = value;
        }
    }
}
