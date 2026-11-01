package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.Block;
import ca.teamdman.sfml.ast.InputStatement;
import ca.teamdman.sfml.ast.OutputStatement;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.Continuation;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.SfmlRuntimeException;
import ca.teamdman.sfml.data.SourceLocation;
import ca.teamdman.sfml.data.StateAccess;

class SfmlRuntimeLocationTest {

    private static final String NATIVE_FAULT = "OUTPUT 1 REDSTONE LEVEL TO label(\"private label\").slots(0,0);";

    @Test
    void nativeMainStatementRetainsItsExactLineColumnAndKindWithoutSourceText() {
        Program program = parse("EVERY 20 TICKS {\n  " + NATIVE_FAULT + "\n}");
        SfmlRuntimeException failure = assertThrows(
            SfmlRuntimeException.class,
            () -> program.tick(new ProgramContext(null, null, null, null, 20, false)));
        assertEquals(2, failure.getLine());
        assertEquals(2, failure.getColumn());
        assertEquals("RedstoneOutputStatement", failure.getStatementKind());
        assertTrue(
            failure.getMessage()
                .contains("slot-qualified"));
        assertFalse(
            failure.getMessage()
                .contains("private label"));
        assertNotNull(failure.getCause());
    }

    @Test
    void nestedProcedureRetainsInnerNativeLocationAndEveryFunctionContext() {
        Program program = parse(
            "void inner() {\n  " + NATIVE_FAULT + "\n}\n" + "void outer() { inner(); }\nEVERY 20 TICKS { outer(); }");
        SfmlRuntimeException failure = assertThrows(
            SfmlRuntimeException.class,
            () -> program.tick(new ProgramContext(null, null, null, null, 20, false)));
        assertEquals(2, failure.getLine());
        assertEquals(2, failure.getCol());
        assertEquals("RedstoneOutputStatement", failure.getKind());
        assertEquals(Arrays.asList("inner", "outer"), failure.getFunctionContexts());
        assertTrue(
            failure.getMessage()
                .contains("In function inner"));
        assertTrue(
            failure.getMessage()
                .contains("In function outer"));
    }

    @Test
    void expressionFaultKeepsInnerExpressionCoordinatesAndOriginalArithmeticCause() {
        Program program = parse(
            "integer inner() {\n  RETURN 1 / 0;\n}\n"
                + "integer outer() { RETURN inner(); }\nEVERY 20 TICKS { LET answer = outer(); }");
        SfmlRuntimeException failure = assertThrows(
            SfmlRuntimeException.class,
            () -> program.tick(new ProgramContext(null, null, null, null, 20, false)));
        assertEquals(2, failure.getLine());
        assertEquals(9, failure.getColumn());
        assertEquals("Expression", failure.getStatementKind());
        assertEquals(Arrays.asList("inner", "outer"), failure.getFunctionContexts());
        Throwable cause = failure;
        for (int i = 0; i < 16 && cause.getCause() != null; i++) cause = cause.getCause();
        assertInstanceOf(ArithmeticException.class, cause);
    }

    @Test
    void nativeFaultAfterWaitKeepsItsLocationWithoutReplayingEarlierReferenceMutation() throws Exception {
        Run run = new Run(
            "STATE answer = 0\nvoid edit(ref integer value) {\n" + "  value = value + 1;\n  WAIT(2);\n  "
                + NATIVE_FAULT
                + "\n  value = value + 10;\n}\nEVERY 10 TICKS { edit(ref answer); }");
        assertDoesNotThrow(() -> run.tick(10));
        assertEquals(1, run.state.values[0].asLong());
        assertEquals(
            1,
            run.manager.getProgramExecution(run.program)
                .sleepingCount());
        SfmlRuntimeException failure = assertThrows(SfmlRuntimeException.class, () -> run.tick(12));
        assertEquals(5, failure.getLine());
        assertEquals(2, failure.getColumn());
        assertEquals("RedstoneOutputStatement", failure.getStatementKind());
        assertEquals(Collections.singletonList("edit"), failure.getFunctionContexts());
        assertEquals(1, run.state.values[0].asLong());
        assertEquals(
            0,
            run.manager.getProgramExecution(run.program)
                .sleepingCount());
    }

    @Test
    void pauseIdentityAndUnannotatedBlockExceptionBehaviorAreUnchanged() {
        DataRuntime runtime = new DataRuntime(null, 0);
        runtime.enableContinuation(10);
        Continuation.Pause pause = assertThrows(Continuation.Pause.class, () -> runtime.waitTicks(new Object(), 2));
        SourceLocation location = new SourceLocation(7, 4, "WaitStatement");
        assertSame(pause, location.annotate(pause));
        Block annotated = new Block(
            Collections.singletonList(ignored -> { throw pause; }),
            Collections.singletonList(location));
        assertSame(
            pause,
            assertThrows(
                Continuation.Pause.class,
                () -> annotated.tick(new ProgramContext(null, null, null, null, 20, false))));
        IllegalStateException original = new IllegalStateException("fixture");
        Block unannotated = Block.of(ignored -> { throw original; });
        assertSame(
            original,
            assertThrows(
                IllegalStateException.class,
                () -> unannotated.tick(new ProgramContext(null, null, null, null, 20, false))));
    }

    @Test
    void annotationsPreserveStatementTypesAndBoundDiagnosticFormatting() {
        Program program = parse("EVERY 20 TICKS { INPUT FROM \"source\"; OUTPUT TO \"dest\"; }");
        assertInstanceOf(
            InputStatement.class,
            program.getTriggers()
                .get(0)
                .getBlock()
                .getStatements()
                .get(0));
        assertInstanceOf(
            OutputStatement.class,
            program.getTriggers()
                .get(0)
                .getBlock()
                .getStatements()
                .get(1));
        IllegalStateException cause = new IllegalStateException(String.join("", Collections.nCopies(3000, "x\n")));
        SfmlRuntimeException failure = SfmlRuntimeException.at(new SourceLocation(4, 1, "OutputStatement"), cause);
        assertTrue(
            failure.getMessage()
                .length() < 1200);
        assertFalse(
            failure.getMessage()
                .contains("\n"));
        assertSame(cause, failure.getCause());
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
        final TileEntityManager manager = RecoveryFixtures.manager(fixture);
        final Program program;
        final Memory state;

        Run(String source) throws Exception {
            program = parse(source);
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
    }
}
