package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.program.ExecutionTrace;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.SourceLocation;

class ExecutionTraceTest {

    private static ProgramContext context(ExecutionTrace trace) {
        ProgramContext context = new ProgramContext(null, null, null, null, 20, false);
        context.setExecutionTrace(trace);
        return context;
    }

    @Test
    void recordsActualStatementsAndLexicalSnapshotsWithoutChangingWorkOrValues() {
        Program program = parse("every 20 ticks { let n = 1 if(n == 1) { let inner = 7 n = inner } n = n + 1 }");
        ProgramContext normal = context(null);
        program.tick(normal);
        ExecutionTrace trace = new ExecutionTrace(20, 64);
        ProgramContext debug = context(trace);
        program.tick(debug);
        assertEquals(
            normal.getDataRuntime()
                .getWork(),
            debug.getDataRuntime()
                .getWork());
        assertTrue(
            trace.locals(5)
                .toString()
                .contains("n = 8"));
        assertTrue(
            trace.locals(1)
                .toString()
                .contains("n = 1"));
        assertFalse(
            trace.locals(1)
                .toString()
                .contains("inner ="));
        assertTrue(
            trace.locals(3)
                .toString()
                .contains("inner = 7"));
        assertFalse(
            trace.locals(5)
                .toString()
                .contains("inner ="));
        assertTrue(
            trace.list(null)
                .toString()
                .contains("completed"));
    }

    @Test
    void returningFunctionAndRefCallsShowTheirOwnFramesAndCallerUpdates() {
        Program program = parse(
            "integer bump(ref integer n) { let delta = 2 n = n + delta return n } "
                + "every 20 ticks { let n = 3 let answer = bump(ref n) }");
        ExecutionTrace trace = new ExecutionTrace(20, 64);
        program.tick(context(trace));
        assertTrue(
            trace.locals(3)
                .toString()
                .contains("n = <ref; inspect caller>"));
        assertTrue(
            trace.locals(3)
                .toString()
                .contains("delta = 2"));
        assertTrue(
            trace.locals(2)
                .toString()
                .contains("n = 5"));
        assertTrue(
            trace.locals(2)
                .toString()
                .contains("answer = 5"));
    }

    @Test
    void failureAndWaitKeepSnapshotsAndDoNotAlterExceptionsOrContinuation() throws Exception {
        ExecutionTrace trace = new ExecutionTrace(20, 64);
        Program waiting = parse(
            "let answer = 0 integer later(integer n) { let saved = n wait(2) return saved } "
                + "every 20 ticks { answer = later(7) }");
        Fixture fixture = new Fixture();
        fixture.world.time = 20;
        ProgramContext context = new ProgramContext(fixture.world, null, null, null, 20, false);
        context.setExecutionTrace(trace);
        waiting.tick(context);
        assertTrue(
            trace.list(null)
                .toString()
                .contains("waiting"));
        assertTrue(
            trace.locals(3)
                .toString()
                .contains("saved = 7"));
        fixture.world.time = 22;
        waiting.tick(context);
        assertEquals(
            7,
            context.getProgramGlobals(waiting)
                .read(0)
                .asLong());
        assertTrue(
            trace.list(null)
                .toString()
                .contains("tick 22"));
        Program failed = parse("every 20 ticks { let n = 1 let answer = n / 0 }");
        ExecutionTrace failures = new ExecutionTrace(20, 64);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> failed.tick(context(failures)));
        assertTrue(
            exception.getMessage()
                .contains("Line"));
        assertTrue(
            failures.list(null)
                .toString()
                .contains("failed"));
        assertTrue(
            failures.locals(2)
                .toString()
                .contains("n = 1"));
    }

    @Test
    void capturesHaveHardCapsExpiryAndCannotAcceptAnotherCaptureEvents() {
        SourceLocation at = new SourceLocation(1, 0, "Test");
        ExecutionTrace trace = new ExecutionTrace(0, 1);
        ExecutionTrace.Event event = trace.begin(0, at);
        assertNotNull(event);
        assertNull(trace.begin(0, at));
        trace.finish(event, "completed", null, null);
        assertTrue(
            trace.list(null)
                .toString()
                .contains("completed"));
        ExecutionTrace other = new ExecutionTrace(0, 1);
        other.finish(event, "leaked", null, null);
        assertFalse(
            trace.list(null)
                .toString()
                .contains("leaked"));
        assertNull(new ExecutionTrace(0, 128).begin(200, at));
        assertThrows(IllegalArgumentException.class, () -> new ExecutionTrace(0, 129));
    }

    @Test
    void debugReadsNeverInitializeValuesOrSpendScriptBudget() {
        DataRuntime runtime = new DataRuntime(null, 2);
        runtime.write(false, 0, DataValue.integer(4));
        int before = runtime.getWork();
        assertEquals(
            4,
            runtime.debugLocal(0)
                .asLong());
        assertNull(runtime.debugLocal(1));
        assertNull(runtime.debugLocal(2));
        assertEquals(before, runtime.getWork());
    }
}
