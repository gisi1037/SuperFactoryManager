package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.langs.SFMLParser;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.FunctionCompiler;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class SfmlIndexedFunctionLoopTest {

    @Test
    void javaLoopInitializersAreMutableScopedAndSupportOmittedClauses() {
        assertEquals(
            6,
            evaluate(
                "integer calculate() { let total = 0; for (let i = 0; i < 4; i = i + 1) { total = total + i; } return total; }"));
        assertEquals(
            2,
            evaluate(
                "integer calculate() { let total = 0; for (let i = 0; i < 4; i = i + 1) { i = i + 1; total = total + 1; } return total; }"));
        assertEquals(3, evaluate("integer calculate() { let i = 0; for (; i < 3;) { i = i + 1; } return i; }"));
        assertEquals(4, evaluate("integer calculate() { let i = 0; for (i = 2; i < 4; i = i + 1) { } return i; }"));
        assertEquals(7, evaluate("integer calculate() { for (;;) { return 7; } return 0; }"));
        assertEquals(9, evaluate("integer calculate() { for (let i = 0; i < 1; i = i + 1) { } let i = 9; return i; }"));
    }

    @Test
    void initializerCallsRunOnceAndReturnSkipsTheUpdateCall() {
        String bump = "integer bump(ref integer n) { n = n + 1; return n; } ";
        assertEquals(
            1,
            evaluate(
                bump + "integer calculate() { let n = 0; for (bump(ref n); false; bump(ref n)) { n = 99; } return n; }"));
        assertEquals(3, evaluate(bump + "integer calculate() { let n = 0; for (; n < 3; bump(ref n)) { } return n; }"));
        assertEquals(
            42,
            evaluate(
                bump + "integer stop(ref integer n) { for (;; bump(ref n)) { return 42; } return 0; } "
                    + "integer calculate() { let n = 0; let result = stop(ref n); return result + n; }"));
    }

    @Test
    void indexedFormsNestWithJavaLoopsAndPreserveEmptyBounds() {
        assertEquals(
            16,
            evaluate(
                "integer calculate() { let total = 0; for (let i = 0; i < 2; i = i + 1) { for (j, value) in [2, 4] { total = total + i + j + value; } } return total; }"));
        assertEquals(
            6,
            evaluate(
                "FUNCTION calculate() RETURNS INTEGER DO LET total = 0 FOR (index, value) IN [2, 3] DO total = total + index + value END RETURN total END"));
        assertEquals(
            0,
            evaluate(
                "integer calculate() { let total = 0; let items = array([1]); clear(ref items); for (index, value) in items { total = 99; } for (let i = 3; i < 3; i = i + 1) { total = 88; } return total; }"));
    }

    @Test
    void indexedSourceIsCapturedOnceAndOrdinaryArrayParametersRemainCopies() {
        assertEquals(
            109,
            evaluate(
                "integer[] make(ref integer calls) { calls = calls + 1; return array([3, 5]); } "
                    + "integer calculate() { let calls = 0; let total = 0; for (index, value) in make(ref calls) { total = total + index + value; } return calls * 100 + total; }"));
        assertEquals(
            46,
            evaluate(
                "integer edit(integer[] items) { let total = 0; for (index, value) in items { total = total + index + value; push(ref items, 9); } return total * 10 + len(items); } "
                    + "integer calculate() { let items = array([1, 2]); let result = edit(items); return result + len(items); }"));
    }

    @Test
    void invalidTypesReadonlyBindersShadowingAndEscapedScopesAreRejected() {
        String[] invalid = { "integer calculate() { for (let i = 0; 1; i = i + 1) { } return 0; }",
            "integer calculate() { for (let i = 0; i < 1; i = i + 1) { } return i; }",
            "integer calculate(integer i) { for (let i = 0; i < 1; i = i + 1) { } return 0; }",
            "integer calculate() { for (let i = 0; i < 1; child = child + 1) { let child = 0; } return 0; }",
            "integer calculate() { for (index, value) in [1] { index = 1; } return 0; }",
            "integer calculate() { for (index, value) in [1] { value = 1; } return 0; }",
            "integer calculate() { for (index, index) in [1] { } return 0; }",
            "integer calculate() { for (index, value) in 1 { } return 0; }",
            "integer calculate() { for (index, value) in [1] { } return value; }" };
        for (String source : invalid) assertFalse(
            new ProgramBuilder(source).build()
                .isSuccess(),
            source);
    }

    @Test
    void waitingJavaLoopResumesEachPhaseExactlyOnce() throws Exception {
        Run run = new Run(
            "STATE sInit = 0 STATE sTests = 0 STATE sSteps = 0 STATE sBody = 0 STATE answer = 0 "
                + "integer initialize(ref integer n) { n = n + 1; wait(1); return 0; } "
                + "boolean ready(ref integer n, integer i) { n = n + 1; wait(1); return i < 2; } "
                + "integer advance(ref integer n, integer i) { n = n + 1; wait(1); return i + 1; } "
                + "integer sample(ref integer n, integer i) { n = n + 1; wait(1); return i + 1; } "
                + "integer calculate(ref integer starts, ref integer testsCount, ref integer stepsCount, ref integer bodiesCount) { let total = 0; for (let i = initialize(ref starts); ready(ref testsCount, i); i = advance(ref stepsCount, i)) { total = total + sample(ref bodiesCount, i); } return total; } "
                + "EVERY 100 TICKS { answer = calculate(ref sInit, ref sTests, ref sSteps, ref sBody); }");
        run.tick(100);
        assertEquals(1, run.value(0));
        assertEquals(0, run.value(1));
        run.tick(101);
        assertEquals(1, run.value(1));
        run.tick(102);
        assertEquals(1, run.value(3));
        run.tick(103);
        assertEquals(1, run.value(2));
        for (int tick = 104; tick <= 108; tick++) run.tick(tick);
        assertEquals(1, run.value(0));
        assertEquals(3, run.value(1));
        assertEquals(2, run.value(2));
        assertEquals(2, run.value(3));
        assertEquals(3, run.value(4));
        assertEquals(
            0,
            run.manager.getProgramExecution(run.program)
                .sleepingCount());
    }

    @Test
    void indexedSourceAndBodiesWaitWithoutReevaluatingOrRebinding() throws Exception {
        Run run = new Run(
            "STATE reads = 0 STATE visits = 0 STATE answer = 0 "
                + "integer[] make(ref integer n) { n = n + 1; wait(1); return array([4, 6]); } "
                + "integer calculate(ref integer sourceCount, ref integer bodyCount) { let total = 0; for (index, value) in make(ref sourceCount) { bodyCount = bodyCount + 1; wait(1); total = total + index + value; } return total; } "
                + "EVERY 100 TICKS { answer = calculate(ref reads, ref visits); }");
        run.tick(100);
        assertEquals(1, run.value(0));
        assertEquals(0, run.value(1));
        run.tick(101);
        assertEquals(1, run.value(1));
        run.tick(102);
        assertEquals(2, run.value(1));
        run.tick(103);
        assertEquals(1, run.value(0));
        assertEquals(2, run.value(1));
        assertEquals(11, run.value(2));
    }

    @Test
    void voidProcedureLoopsKeepIndependentNativeInputAllowances() throws Exception {
        Run run = new Run(
            "void gather() { for (let i = 0; i < 2; i = i + 1) { INPUT 1 FROM \"source\"; } "
                + "for (index, value) in [1, 2] { INPUT 1 FROM \"source\"; } } "
                + "EVERY 100 TICKS { gather(); OUTPUT TO \"dest\"; }");
        run.fixture.stock(0, 7);
        run.tick(100);
        assertEquals(4, count(run.fixture.dest));
        assertEquals(3, count(run.fixture.source));
    }

    @Test
    void waitingBodyReturnExitsBeforeTheUpdatePhase() throws Exception {
        Run run = new Run(
            "STATE steps = 0 STATE answer = 0 " + "integer advance(ref integer n) { n = n + 1; return n; } "
                + "integer calculate(ref integer counter) { for (;; advance(ref counter)) { wait(1); return 7; } return 0; } "
                + "EVERY 100 TICKS { answer = calculate(ref steps); }");
        run.tick(100);
        assertEquals(0, run.value(1));
        run.tick(101);
        assertEquals(0, run.value(0));
        assertEquals(7, run.value(1));
    }

    @Test
    void resumableEmptyForReachesTheLoopGuardWithoutUnusedPhaseAllocations() throws Exception {
        Run run = new Run(
            "integer spin() { for (; true;) { } return 0; } "
                + "EVERY 100 TICKS { let result = spin(); } EVERY 100 TICKS { wait(1); }");
        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class, () -> run.tick(100));
        assertTrue(
            failure.getMessage()
                .contains("Loop step budget exhausted"),
            failure.getMessage());
    }

    private static long evaluate(String definitions) {
        SFMLParser parser = new SFMLParser(new CommonTokenStream(new SFMLLexer(CharStreams.fromString(definitions))));
        SFMLParser.ProgramContext syntax = parser.program();
        assertEquals(0, parser.getNumberOfSyntaxErrors(), definitions);
        DataCompiler compiler = new DataCompiler();
        new FunctionCompiler(compiler).compile(syntax.functionDefinition());
        compiler.beginTrigger();
        compiler.pushScope();
        return compiler.compile("calculate()", 1, 0)
            .evaluate(new DataRuntime(null, 0))
            .asLong();
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

        Run(String source) throws Exception {
            ProgramBuildResult result = new ProgramBuilder(source).build();
            assertTrue(result.isSuccess(), result.getErrorMessage());
            program = result.getProgram();
            state = new Memory(program);
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

        long value(int slot) {
            return state.values[slot].asLong();
        }
    }
}
