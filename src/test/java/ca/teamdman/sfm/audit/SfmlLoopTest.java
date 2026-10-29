package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.ast.Block;
import ca.teamdman.sfml.ast.CountedForStatement;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.ast.WhileStatement;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ProgramStateSchema;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;
import ca.teamdman.sfml.program_builder.VisualCapabilityAnalyzer;
import ca.teamdman.sfml.program_builder.VisualCapabilityReport;

class SfmlLoopTest {

    private static Program program(String source) {
        ProgramBuildResult result = new ProgramBuilder(source).build();
        assertTrue(result.isSuccess(), result.getErrorMessage());
        return result.getProgram();
    }

    private static ProgramContext context(StateAccess state) {
        ProgramContext context = new ProgramContext(null, null, null, null, 20, false);
        context.setStateAccess(state);
        return context;
    }

    private static MemoryState run(String declarations, String body) {
        Program program = program(declarations + " EVERY 20 TICKS DO " + body + " END");
        MemoryState state = new MemoryState(program.getStateSchema());
        program.tick(context(state));
        return state;
    }

    @Test
    void countedRangesAreInclusiveAndDefaultStepIsAlwaysPositive() {
        assertEquals(15, run("STATE $sum = 0", "FOR $i = 1 TO 5 DO SET $sum = $sum + $i END").number(0));
        assertEquals(9, run("STATE $sum = 0", "FOR $i = 5 TO 1 STEP -2 DO SET $sum = $sum + $i END").number(0));
        assertEquals(0, run("STATE $sum = 0", "FOR $i = 5 TO 1 DO SET $sum = 99 END").number(0));
        assertEquals(0, run("STATE $sum = 0", "FOR $i = 1 TO 5 STEP -1 DO SET $sum = 99 END").number(0));
        assertEquals(7, run("STATE $sum = 0", "FOR $i = 7 TO 7 DO SET $sum = $i END").number(0));
    }

    @Test
    void boundsAndStepAreSnapshottedBeforeBodyAndBodyLocalsReinitialize() {
        MemoryState state = run(
            "STATE $bound = 3 STATE $sum = 0",
            "LET $stride = 1 " + "FOR $i = 1 TO $bound STEP $stride DO LET $local = 1 SET $local = $local + 1 "
                + "SET $sum = $sum + $i + $local SET $bound = 100 SET $stride = 100 END");
        assertEquals(12, state.number(1));
    }

    @Test
    void boundEvaluationRunsOnceInSourceOrder() {
        DataCompiler compiler = new DataCompiler();
        compiler.beginTrigger();
        compiler.pushScope();
        DataCompiler.Binding binder = compiler.declareReadOnlyLocal("i", DataType.INTEGER);
        List<String> calls = new ArrayList<>();
        ProgramContext context = context(null);
        context.beginDataInvocation(1, false);
        new CountedForStatement(
            binder,
            recorded(calls, "start", 1),
            recorded(calls, "end", 2),
            recorded(calls, "step", 1),
            Block.of(c -> calls.add("body"))).tick(context);
        assertEquals(Arrays.asList("start", "end", "step", "body", "body"), calls);
        assertFalse(context.isInLoop());
    }

    @Test
    void signedBoundaryCompletionDoesNotOverflow() {
        String[] ranges = { "9223372036854775807 TO 9223372036854775807",
            "-9223372036854775808 TO -9223372036854775808 STEP -1", "9223372036854775806 TO 9223372036854775807 STEP 2",
            "-9223372036854775807 TO -9223372036854775808 STEP -2",
            "0 TO -9223372036854775808 STEP -9223372036854775808",
            "9223372036854775807 TO -9223372036854775808 STEP -9223372036854775808" };
        long[] expected = { 1, 1, 1, 1, 2, 2 };
        for (int i = 0; i < ranges.length; i++) assertEquals(
            expected[i],
            run("STATE $n = 0", "FOR $i = " + ranges[i] + " DO SET $n = $n + 1 END").number(0),
            ranges[i]);
    }

    @Test
    void zeroStepFaultsBeforeBodyEvenForAnOtherwiseEmptyRange() {
        Program program = program("STATE $n = 0 EVERY 20 TICKS DO FOR $i = 9 TO 1 STEP 0 DO SET $n = 1 END END");
        MemoryState state = new MemoryState(program.getStateSchema());
        ProgramContext context = context(state);
        assertTrue(
            assertThrows(IllegalArgumentException.class, () -> program.tick(context)).getMessage()
                .contains("zero"));
        assertEquals(0, state.number(0));
        assertFalse(context.isInLoop());
    }

    @Test
    void arrayIterationRetainsNestedImmutableSnapshotAndAllowsOuterMutation() {
        MemoryState state = run(
            "STATE $sum = 0",
            "LET $items = [{n: 1}, {n: 2}, {n: 3}] "
                + "FOR $value IN $items DO SET $sum = $sum + $value.n SET $items[1].n = 99 END "
                + "FOR $value IN [\"a\", \"b\"] DO LET $local = $value END");
        assertEquals(6, state.number(0));
    }

    @Test
    void bindersHaveLexicalScopeAreReadOnlyAndCannotShadow() {
        String[] invalid = { "FOR $i = 1 TO 2 DO SET $i = 3 END", "FOR $v IN [{n: 1}] DO SET $v.n = 3 END",
            "FOR $v IN [[1]] DO SET $v[0] = 3 END", "FOR $i = 1 TO 2 DO END LET $n = $i",
            "LET $i = 1 FOR $i = 1 TO 2 DO END", "FOR $i = 1 TO 2 DO LET $i = 1 END",
            "FOR $i = 1 TO 2 DO FOR $i IN [1] DO END END", "FOR $i = TRUE TO 2 DO END",
            "FOR $i = 1 TO 2 STEP FALSE DO END", "FOR $i IN 7 DO END", "WHILE (7) DO END" };
        for (String body : invalid) assertFalse(
            new ProgramBuilder("EVERY 20 TICKS DO " + body + " END").build()
                .isSuccess(),
            body);
        assertFalse(
            new ProgramBuilder("STATE $i = 0 EVERY 20 TICKS DO FOR $i IN [1] DO END END").build()
                .isSuccess());
        program("EVERY 20 TICKS DO FOR $i IN [1] DO END FOR $i = 1 TO 2 DO END END");
    }

    @Test
    void whileConditionsReevaluateAndShortCircuitInBothBooleanForms() {
        assertEquals(
            5,
            run(
                "STATE $n = 0",
                "WHILE ($n < 5) DO SET $n = $n + 1 END " + "WHILE (FALSE AND 1 / 0 > 0) DO SET $n = 99 END "
                    + "WHILE FALSE AND REDSTONE DO SET $n = 99 END").number(0));
        program("EVERY 20 TICKS DO WHILE source HAS > 3 minecraft:stone DO FORGET END END");
    }

    @Test
    void emptyForeverLoopFaultsAtSharedStepLimitAndUnwindsLoopDepth() {
        Program program = program("EVERY 20 TICKS DO WHILE TRUE DO END END");
        assertTrue(program.hasDataFeatures());
        ProgramContext context = context(null);
        assertTrue(
            assertThrows(IllegalArgumentException.class, () -> program.tick(context)).getMessage()
                .contains("Loop step"));
        assertEquals(
            DataRuntime.MAX_LOOP_STEPS,
            context.getDataRuntime()
                .getLoopSteps());
        assertFalse(context.isInLoop());
    }

    @Test
    void exactForLimitSucceedsWhileNextEntryFaultsWithoutUndoingPriorEffects() {
        assertEquals(1024, run("STATE $n = 0", "FOR $i = 1 TO 1024 DO SET $n = $n + 1 END").number(0));
        Program program = program("STATE $n = 0 EVERY 20 TICKS DO FOR $i = 1 TO 1025 DO SET $n = $n + 1 END END");
        MemoryState state = new MemoryState(program.getStateSchema());
        ProgramContext context = context(state);
        assertThrows(IllegalArgumentException.class, () -> program.tick(context));
        assertEquals(1024, state.number(0));
        assertFalse(context.isInLoop());
    }

    @Test
    void finalFalseWhileCheckConsumesBudgetAndNestedTriggersShareIt() {
        Program exact = program("STATE $n = 0 EVERY 20 TICKS DO WHILE ($n < 1023) DO SET $n = $n + 1 END END");
        MemoryState exactState = new MemoryState(exact.getStateSchema());
        ProgramContext exactContext = context(exactState);
        exact.tick(exactContext);
        assertEquals(
            1024,
            exactContext.getDataRuntime()
                .getLoopSteps());
        Program cross = program(
            "STATE $n = 0 EVERY 20 TICKS DO FOR $i = 1 TO 512 DO SET $n = $n + 1 END END "
                + "EVERY 20 TICKS DO FOR $i = 1 TO 512 DO SET $n = $n + 1 END WHILE FALSE DO END END");
        MemoryState state = new MemoryState(cross.getStateSchema());
        ProgramContext context = context(state);
        assertThrows(IllegalArgumentException.class, () -> cross.tick(context));
        assertEquals(1024, state.number(0));
        Program nested = program(
            "STATE $n = 0 EVERY 20 TICKS DO FOR $i = 1 TO 512 DO "
                + "FOR $j = 1 TO 1 DO SET $n = $n + 1 END END WHILE FALSE DO END END");
        MemoryState nestedState = new MemoryState(nested.getStateSchema());
        ProgramContext nestedContext = context(nestedState);
        assertThrows(IllegalArgumentException.class, () -> nested.tick(nestedContext));
        assertEquals(512, nestedState.number(0));
        assertFalse(nestedContext.isInLoop());
        Program finalCheck = program("STATE $n = 0 EVERY 20 TICKS DO WHILE ($n < 1024) DO SET $n = $n + 1 END END");
        MemoryState finalState = new MemoryState(finalCheck.getStateSchema());
        assertThrows(IllegalArgumentException.class, () -> finalCheck.tick(context(finalState)));
        assertEquals(1024, finalState.number(0));
    }

    @Test
    void exhaustedStepBudgetStopsBeforeConditionObservation() {
        ProgramContext context = context(null);
        context.beginDataInvocation(0, false);
        for (int i = 0; i < DataRuntime.MAX_LOOP_STEPS; i++) context.getDataRuntime()
            .loopStep();
        List<String> observations = new ArrayList<>();
        WhileStatement loop = new WhileStatement(c -> {
            observations.add("condition");
            return false;
        }, Block.of(c -> fail("Body must not run")));
        assertThrows(IllegalArgumentException.class, () -> loop.tick(context));
        assertTrue(observations.isEmpty());
        assertFalse(context.isInLoop());
    }

    @Test
    void loopNestingHasAnExplicitCompileBound() {
        StringBuilder allowed = new StringBuilder("EVERY 20 TICKS DO ");
        for (int i = 0; i < 16; i++) allowed.append("WHILE FALSE DO ");
        for (int i = 0; i < 17; i++) allowed.append("END ");
        program(allowed.toString());
        String rejected = allowed.toString()
            .replaceFirst("EVERY 20 TICKS DO", "EVERY 20 TICKS DO WHILE FALSE DO") + " END";
        ProgramBuildResult result = new ProgramBuilder(rejected).build();
        assertFalse(result.isSuccess());
        assertTrue(
            result.getErrorMessage()
                .contains("Loop nesting exceeds 16"),
            result.getErrorMessage());
    }

    @Test
    void repeatedLegacyStatementsAlsoConsumeTheSharedExpressionWorkAllowance() {
        StringBuilder body = new StringBuilder("STATE $n = 0 EVERY 20 TICKS DO FOR $i = 1 TO 1024 DO ");
        for (int i = 0; i < 20; i++) body.append("FORGET ");
        body.append("SET $n = $n + 1 END END");
        Program program = program(body.toString());
        MemoryState state = new MemoryState(program.getStateSchema());
        ProgramContext context = context(state);
        assertTrue(
            assertThrows(IllegalArgumentException.class, () -> program.tick(context)).getMessage()
                .contains("Expression work"));
        assertTrue(state.number(0) > 0 && state.number(0) < 1024);
        assertFalse(context.isInLoop());
    }

    @Test
    void keywordsRemainLegacyIdentifiersAndLoopsRoundTripWithVisualBlocker() {
        Program legacy = program("EVERY 20 TICKS DO INPUT for FROM while OUTPUT step TO for END");
        assertFalse(legacy.hasDataFeatures());
        Program original = program(
            "STATE $sum = 0 EVERY 20 TICKS DO LET $fields = {for: 2, while: 3, step: 1} "
                + "FOR $i = $fields.for TO $fields.while STEP $fields.step DO FOR $v IN [1, 2] DO "
                + "SET $sum = $sum + $i + $v END END WHILE FALSE DO END END");
        Program rebuilt = program(original.toString());
        MemoryState state = new MemoryState(rebuilt.getStateSchema());
        rebuilt.tick(context(state));
        assertEquals(16, state.number(0));
        ProgramBuildResult result = new ProgramBuilder("EVERY 20 TICKS DO WHILE FALSE DO END END").buildWithSyntax();
        VisualCapabilityReport report = VisualCapabilityAnalyzer.analyze(result);
        assertFalse(report.isAvailable());
        assertEquals(
            VisualCapabilityReport.Kind.LOOP_STATEMENT,
            report.getBlockers()
                .get(0).kind);
    }

    private static DataExpression recorded(List<String> calls, String name, long value) {
        return new DataExpression() {

            @Override
            public DataType getType() {
                return DataType.INTEGER;
            }

            @Override
            public DataValue evaluate(DataRuntime runtime) {
                calls.add(name);
                return DataValue.integer(value);
            }
        };
    }

    private static final class MemoryState implements StateAccess {

        private final DataValue[] values;

        MemoryState(ProgramStateSchema schema) {
            values = new DataValue[schema.size()];
            for (int i = 0; i < values.length; i++) values[i] = schema.getDeclarations()
                .get(i)
                .getDefaultValue();
        }

        long number(int slot) {
            return values[slot].asLong();
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
}
