package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class SfmlStateFunctionTest {

    @Test
    void functionsReadCallingStateAndProceduresCanWriteItWithoutParameters() throws Exception {
        Run run = new Run(
            program(
                "state count = 2 state result = 0 "
                    + "integer current() { return count; } void increment() { count = current() + 1; } "
                    + "every 20 ticks { increment(); result = current(); }"));
        run.tick(20);
        assertEquals(3, run.state.values[0].asLong());
        assertEquals(3, run.state.values[1].asLong());
        run.tick(40);
        assertEquals(4, run.state.values[0].asLong());
    }

    @Test
    void oneCompiledProgramNeverSharesStateBetweenManagers() throws Exception {
        Program program = program(
            "state count = 0 void increment() { $count = count + 1; } " + "every 20 ticks { increment(); }");
        Run a = new Run(program), b = new Run(program);
        a.tick(20);
        a.tick(40);
        b.tick(20);
        assertEquals(2, a.state.values[0].asLong());
        assertEquals(1, b.state.values[0].asLong());
    }

    @Test
    void proceduresRetainTheirFrameAndUseFreshStateAfterWaiting() throws Exception {
        Run run = new Run(
            program(
                "state count = 0 void increment() { count = count + 1; wait(2); "
                    + "count = count + 1; } every 20 ticks { increment(); } "
                    + "every 1 ticks { count = count + 10; }"));
        run.tick(20);
        run.tick(21);
        run.tick(22);
        assertEquals(32, run.state.values[0].asLong());
    }

    @Test
    void waitingValueFunctionsFreezeCompletedOperandsButLaterStatementsReadFreshState() throws Exception {
        Run run = new Run(
            program(
                "state count = 1 state result = 0 " + "integer delayed() { wait(2); return count; } "
                    + "integer read() { return count + delayed(); } "
                    + "every 20 ticks { result = read(); } every 1 ticks { count = count + 1; }"));
        run.tick(20);
        run.tick(21);
        run.tick(22);
        assertEquals(4, run.state.values[1].asLong()); // Captured 1 + current 3, before the other trigger runs.
    }

    @Test
    void pureValueFunctionsCannotWriteStateAndDeclarationsCannotShadowState() {
        invalid("state count = 0 integer bad() { count = 1; return count; } every 20 ticks {}");
        invalid("state count = [0] integer bad() { count[0] = 1; return 0; } every 20 ticks {}");
        invalid("state count = 0 integer bad(integer count) { return count; } every 20 ticks {}");
        invalid("state count = 0 void bad() { let count = 1; } every 20 ticks {}");
        invalid("let count = 0 integer bad() { return count; } every 20 ticks {}");
    }

    private static void invalid(String source) {
        assertFalse(
            new ProgramBuilder(source).build()
                .isSuccess(),
            source);
    }

    private static Program program(String source) {
        ProgramBuildResult result = new ProgramBuilder(source).build();
        assertTrue(result.isSuccess(), result.getErrorMessage());
        return result.getProgram();
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

        public DataValue read(int slot) {
            return values[slot];
        }

        public void write(int slot, DataValue value) {
            values[slot] = value;
        }
    }

    private static final class Run {

        final Fixture fixture = new Fixture();
        final TileEntityManager manager;
        final Program program;
        final Memory state;

        Run(Program program) throws Exception {
            this.program = program;
            state = new Memory(program);
            manager = RecoveryFixtures.manager(fixture);
        }

        void tick(long now) {
            fixture.world.time = now;
            ProgramContext context = new ProgramContext(
                fixture.world,
                manager,
                new CableNetwork(fixture.world, MANAGER),
                fixture.labels,
                now,
                false);
            context.setStateAccess(state);
            program.tick(context);
        }
    }
}
