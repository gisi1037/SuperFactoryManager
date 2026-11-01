package ca.teamdman.sfml.data;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.langs.SFMLParser;

class FunctionSuspensionPlanTest {

    @Test
    void onlyCompleteSealedGraphPublishesTheSuspensionProof() {
        DataCompiler compiler = new DataCompiler();
        CompiledFunction ready = function(compiler, "ready");
        ready.seal(new CompiledFunction.Body(Collections.emptyList(), false), 0);
        CompiledFunction unfinished = function(compiler, "unfinished");
        assertTrue(ready.maySuspend());
        compiler.validateFunctions();
        assertTrue(ready.maySuspend(), "An incomplete graph cannot publish an execution shortcut");
        assertTrue(unfinished.maySuspend());
        unfinished.seal(new CompiledFunction.Body(Collections.emptyList(), false), 0);
        compiler.validateFunctions();
        assertFalse(ready.maySuspend());
        assertFalse(unfinished.maySuspend());
    }

    @Test
    void directAndTransitiveWaitZeroRemainConservativeButPureForwardCallsDoNot() {
        DataCompiler compiler = compiler(
            "void direct() { wait(0); } void indirect() { direct(); } "
                + "integer forward() { return leaf(); } integer leaf() { return 7; }");
        assertTrue(
            compiler.getFunctions()
                .get(0)
                .maySuspend());
        assertTrue(
            compiler.getFunctions()
                .get(1)
                .maySuspend());
        assertFalse(
            compiler.getFunctions()
                .get(2)
                .maySuspend());
        assertFalse(
            compiler.getFunctions()
                .get(3)
                .maySuspend());
        DataRuntime runtime = new DataRuntime(null, 0);
        runtime.enableContinuation(10);
        compiler.compileCallStatement("indirect()", 1, 0)
            .evaluate(runtime);
        assertEquals(0, runtime.getFunctionDepth());
    }

    @Test
    void manualPlansAndOldFrameOverloadsKeepResumableBehavior() {
        DataCompiler compiler = new DataCompiler();
        CompiledFunction manual = function(compiler, "manual");
        manual.seal(new CompiledFunction.Body(Collections.singletonList(runtime -> {
            assertTrue(runtime.isResumable());
            runtime.waitTicks(new Object(), 0);
            return null;
        }), false), 0);
        DataRuntime caller = new DataRuntime(null, 0);
        caller.enableContinuation(10);
        Object call = new Object();
        caller.evaluate(call, () -> manual.invoke(caller, new DataValue[0], call));
        DataRuntime old = caller.enterFunction(0);
        assertTrue(old.isResumable());
        old.exitFunction();
        Object otherCall = new Object();
        caller.evaluate(otherCall, () -> {
            DataRuntime frame = caller.functionFrame(otherCall, 0, new DataValue[0]);
            assertTrue(frame.isResumable());
            caller.leaveFunction(otherCall, frame, false);
            return null;
        });
        assertEquals(0, caller.getFunctionDepth());
    }

    @Test
    void fastFramesRemoveContinuationAllocationChargesAndKeepCallAndWorkLimits() throws Exception {
        String definitions = "integer leaf(integer n) { let result = n + 1; return result; } "
            + "integer outer() { return leaf(3) + leaf(4); }";
        DataCompiler fast = compiler(definitions), conservative = compiler(definitions);
        forceConservative(conservative);
        DataRuntime fastRun = new DataRuntime(null, 0), oldRun = new DataRuntime(null, 0);
        fastRun.enableContinuation(10);
        oldRun.enableContinuation(10);
        assertEquals(
            9,
            fast.compile("outer()", 1, 0)
                .evaluate(fastRun)
                .asLong());
        assertEquals(
            9,
            conservative.compile("outer()", 1, 0)
                .evaluate(oldRun)
                .asLong());
        assertEquals(oldRun.getFunctionCalls(), fastRun.getFunctionCalls());
        assertEquals(oldRun.getWork(), fastRun.getWork());
        assertTrue(fastRun.getAllocatedCells() < oldRun.getAllocatedCells());
        assertEquals(0, fastRun.getFunctionDepth());
        assertEquals(0, oldRun.getFunctionDepth());
        for (boolean baseline : Arrays.asList(false, true)) {
            DataCompiler calls = compiler("integer leaf() { return 1; }");
            if (baseline) forceConservative(calls);
            DataExpression expression = calls.compile("leaf()", 1, 0);
            DataRuntime runtime = new DataRuntime(null, 0);
            runtime.enableContinuation(10);
            for (int i = 0; i < DataRuntime.MAX_FUNCTION_CALLS; i++) {
                runtime.continuation()
                    .nextIteration();
                assertEquals(
                    1,
                    expression.evaluate(runtime)
                        .asLong());
            }
            runtime.continuation()
                .nextIteration();
            assertThrows(IllegalArgumentException.class, () -> expression.evaluate(runtime));
            assertEquals(0, runtime.getFunctionDepth());
        }
    }

    @Test
    void returnedValuesErrorsAndDepthOverflowReleaseEveryFrame() throws Exception {
        for (boolean baseline : Arrays.asList(false, true)) {
            StringBuilder definitions = new StringBuilder("integer fail() { return 1 / 0; } ");
            for (int i = 0; i < 17; i++) definitions.append("integer depth")
                .append(i)
                .append("() { return ")
                .append(i == 16 ? "7" : "depth" + (i + 1) + "()")
                .append("; } ");
            DataCompiler compiler = compiler(definitions.toString());
            if (baseline) forceConservative(compiler);
            DataRuntime runtime = new DataRuntime(null, 0);
            runtime.enableContinuation(10);
            DataExpression failure = compiler.compile("fail()", 1, 0);
            assertThrows(IllegalArgumentException.class, () -> failure.evaluate(runtime));
            assertEquals(0, runtime.getFunctionDepth());
            DataExpression overflow = compiler.compile("depth0()", 1, 0);
            IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> overflow.evaluate(runtime));
            assertTrue(
                error.getMessage()
                    .contains("depth"),
                error.getMessage());
            assertEquals(0, runtime.getFunctionDepth());
            assertEquals(
                7,
                compiler.compile("depth16()", 1, 0)
                    .evaluate(runtime)
                    .asLong());
        }
    }

    private static CompiledFunction function(DataCompiler compiler, String name) {
        return compiler.declareFunction(name, Collections.emptyList(), Collections.emptyList(), DataType.VOID, "");
    }

    private static DataCompiler compiler(String source) {
        SFMLParser parser = new SFMLParser(new CommonTokenStream(new SFMLLexer(CharStreams.fromString(source))));
        SFMLParser.ProgramContext syntax = parser.program();
        assertEquals(0, parser.getNumberOfSyntaxErrors());
        DataCompiler compiler = new DataCompiler();
        new FunctionCompiler(compiler).compile(syntax.functionDefinition());
        compiler.beginTrigger();
        compiler.pushScope();
        return compiler;
    }

    private static void forceConservative(DataCompiler compiler) throws Exception {
        Field field = CompiledFunction.class.getDeclaredField("maySuspend");
        field.setAccessible(true);
        for (CompiledFunction function : compiler.getFunctions()) field.setBoolean(function, true);
    }
}
