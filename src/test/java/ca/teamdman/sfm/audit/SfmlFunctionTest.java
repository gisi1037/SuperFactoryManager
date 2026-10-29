package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.langs.SFMLParser;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataQueries;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.FunctionCompiler;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.ProgramStateSchema;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;
import ca.teamdman.sfml.program_builder.VisualCapabilityAnalyzer;
import ca.teamdman.sfml.program_builder.VisualCapabilityReport;

class SfmlFunctionTest {

    private static Program program(String source) {
        ProgramBuildResult result = new ProgramBuilder(source).build();
        assertTrue(result.isSuccess(), result.getErrorMessage());
        return result.getProgram();
    }

    private static DataCompiler compiler(String definitions) {
        DataCompiler compiler = new DataCompiler();
        SFMLParser parser = new SFMLParser(new CommonTokenStream(new SFMLLexer(CharStreams.fromString(definitions))));
        new FunctionCompiler(compiler).compile(
            parser.program()
                .functionDefinition());
        assertEquals(0, parser.getNumberOfSyntaxErrors());
        compiler.beginTrigger();
        compiler.pushScope();
        return compiler;
    }

    private static DataValue evaluate(String definitions, String call) {
        return compiler(definitions).compile(call, 1, 0)
            .evaluate(new DataRuntime(null, 0));
    }

    @Test
    void forwardNamespacedCallsUseFixedTypesAndRoundTrip() {
        String source = "STATE $result = 0 "
            + "FUNCTION math.twice($x: INTEGER) RETURNS INTEGER DO RETURN math.add($x, $x) END "
            + "FUNCTION math.add($a: INTEGER, $b: INTEGER) RETURNS INTEGER DO RETURN $a + $b END "
            + "EVERY 20 TICKS DO SET $result = math.twice(21) END";
        Program original = program(source);
        Program rebuilt = program(original.toString());
        MemoryState state = new MemoryState(rebuilt.getStateSchema());
        rebuilt.tick(context(state));
        assertEquals(
            42,
            state.read(0)
                .asLong());
        assertEquals(
            2,
            rebuilt.getFunctions()
                .size());
        assertThrows(
            UnsupportedOperationException.class,
            () -> rebuilt.getFunctions()
                .clear());
    }

    @Test
    void booleanStringHandleArrayAndStructTypesAreExact() {
        assertTrue(
            evaluate("FUNCTION choose($x: BOOLEAN) RETURNS BOOLEAN DO RETURN NOT $x END", "choose(FALSE)").asBoolean());
        assertEquals(
            "ab",
            evaluate("FUNCTION text($s: STRING) RETURNS STRING DO RETURN $s + \"b\" END", "text(\"a\")").asString());
        assertEquals(
            5,
            evaluate("FUNCTION total($a: INTEGER[3]) RETURNS INTEGER DO RETURN $a[0] + $a[2] END", "total([1, 2, 4])")
                .asLong());
        assertEquals(
            9,
            evaluate(
                "FUNCTION field($a: {n: INTEGER, name: STRING}) RETURNS INTEGER DO RETURN $a.n END",
                "field({name: \"test\", n: 9})").asLong());
        assertEquals(
            8,
            evaluate(
                "FUNCTION rows($a: {n: INTEGER}[2]) RETURNS INTEGER DO RETURN $a[1].n END",
                "rows([{n: 4}, {n: 8}])").asLong());
        assertEquals(2, evaluate("FUNCTION make() RETURNS INTEGER[2] DO RETURN [1, 2] END", "make()[1]").asLong());
        assertEquals(
            6,
            evaluate(
                "FUNCTION matrix($a: INTEGER[3][2]) RETURNS INTEGER DO RETURN $a[2][1] END",
                "matrix([[1, 2], [3, 4], [5, 6]])").asLong());
        assertEquals(
            "source",
            evaluate(
                "FUNCTION source($s: LABEL_REF) RETURNS LABEL_REF DO RETURN $s.slots(0, 1) END",
                "source(LABEL(\"source\"))").asLabel()
                    .getName());
        assertNotNull(
            evaluate(
                "FUNCTION resource($r: RESOURCE_SELECTOR) RETURNS RESOURCE_SELECTOR DO RETURN $r END",
                "resource(ITEM(\"*\"))").asSelector());
        for (String argument : Arrays.asList("TRUE", "[1, 2]", "[1, 2, 3, 4]", "[TRUE, TRUE, TRUE]")) {
            DataCompiler compiler = compiler("FUNCTION total($a: INTEGER[3]) RETURNS INTEGER DO RETURN $a[0] END");
            assertThrows(IllegalArgumentException.class, () -> compiler.compile("total(" + argument + ")", 1, 0));
        }
    }

    @Test
    void parameterCopiesAndFunctionLocalUpdatesCannotChangeCallerCollections() {
        Program program = program(
            "STATE $result = 0 FUNCTION mutate($a: {n: INTEGER}[2]) RETURNS INTEGER DO "
                + "SET $a[0].n = 99 LET $sum = 0 FOR $row IN $a DO SET $sum = $sum + $row.n END RETURN $sum END "
                + "EVERY 20 TICKS DO LET $original = [{n: 1}, {n: 2}] LET $changed = mutate($original) "
                + "SET $result = $changed + $original[0].n END");
        MemoryState state = new MemoryState(program.getStateSchema());
        program.tick(context(state));
        assertEquals(
            102,
            state.read(0)
                .asLong());
    }

    @Test
    void nestedFramesDoNotCaptureCallerLocalsOrStateAndNamesAreCaseSensitive() {
        String[] invalid = { "STATE $x = 7 FUNCTION f() RETURNS INTEGER DO RETURN $x END",
            "STATE $x = 7 FUNCTION f() RETURNS INTEGER DO SET $x = 8 RETURN 1 END",
            "FUNCTION f() RETURNS INTEGER DO RETURN $x END EVERY 20 TICKS DO LET $x = 7 LET $y = f() END",
            "FUNCTION f($x: INTEGER) RETURNS INTEGER DO LET $x = 1 RETURN $x END",
            "FUNCTION f($x: INTEGER, $x: INTEGER) RETURNS INTEGER DO RETURN $x END",
            "FUNCTION f() RETURNS INTEGER DO RETURN 1 END EVERY 20 TICKS DO LET $x = F() END" };
        for (String source : invalid) assertFalse(
            new ProgramBuilder(source).build()
                .isSuccess(),
            source);
        assertEquals(
            4,
            evaluate(
                "FUNCTION outer($x: INTEGER) RETURNS INTEGER DO RETURN inner(2) + $x END "
                    + "FUNCTION inner($x: INTEGER) RETURNS INTEGER DO RETURN $x END",
                "outer(2)").asLong());
        // A STATE name is not captured; an explicit parameter with the same spelling is legal.
        program("STATE $x = 7 FUNCTION f($x: INTEGER) RETURNS INTEGER DO RETURN $x END");
    }

    @Test
    void onlyPureAndObservationStatementsAreAllowedAndStateDefaultsCannotCallFunctions() {
        for (String body : Arrays
            .asList("PRINT(1)", "INPUT FROM source", "OUTPUT TO dest", "FORGET", "OUTPUT REDSTONE TO target"))
            assertFalse(
                new ProgramBuilder("FUNCTION f() RETURNS INTEGER DO " + body + " RETURN 1 END").build()
                    .isSuccess(),
                body);
        assertFalse(
            new ProgramBuilder("STATE $x = f() FUNCTION f() RETURNS INTEGER DO RETURN 1 END").build()
                .isSuccess());
        assertFalse(
            new ProgramBuilder("EVERY 20 TICKS DO RETURN 1 END").build()
                .isSuccess());
        assertFalse(
            new ProgramBuilder("FUNCTION f() RETURNS INTEGER DO IF REDSTONE THEN RETURN 1 ELSE RETURN 2 END END")
                .build()
                .isSuccess());
    }

    @Test
    void returnFlowRequiresEveryPathAndRejectsUnreachableStatements() {
        String[] invalid = { "", "LET $x = 1", "IF TRUE THEN RETURN 1 END", "WHILE TRUE DO RETURN 1 END",
            "FOR $i = 1 TO 1 DO RETURN 1 END", "RETURN TRUE", "RETURN 1 LET $x = 1",
            "IF TRUE THEN RETURN 1 ELSE RETURN 2 END RETURN 3" };
        for (String body : invalid) assertFalse(
            new ProgramBuilder("FUNCTION f() RETURNS INTEGER DO " + body + " END").build()
                .isSuccess(),
            body);
        assertEquals(
            2,
            evaluate(
                "FUNCTION f($x: INTEGER) RETURNS INTEGER DO IF ($x < 0) THEN RETURN 1 ELSE IF ($x = 0) THEN RETURN 2 ELSE RETURN 3 END END",
                "f(0)").asLong());
        assertEquals(
            7,
            evaluate(
                "FUNCTION f() RETURNS INTEGER DO FOR $i = 1 TO 5 DO IF ($i = 3) THEN RETURN 7 END END RETURN 0 END",
                "f()").asLong());
    }

    @Test
    void recursionAndWrongSignaturesAreCompileErrorsEvenWhenUnreachable() {
        String[] invalid = { "FUNCTION f() RETURNS INTEGER DO RETURN f() END",
            "FUNCTION f() RETURNS INTEGER DO RETURN g() END FUNCTION g() RETURNS INTEGER DO RETURN f() END",
            "FUNCTION f() RETURNS INTEGER DO IF FALSE THEN RETURN f() ELSE RETURN 1 END END",
            "FUNCTION f() RETURNS INTEGER DO RETURN missing() END",
            "FUNCTION f($x: INTEGER) RETURNS INTEGER DO RETURN $x END FUNCTION g() RETURNS INTEGER DO RETURN f() END",
            "FUNCTION f() RETURNS INTEGER DO RETURN 1 END FUNCTION f() RETURNS INTEGER DO RETURN 2 END",
            "FUNCTION f($x: NUMBER) RETURNS INTEGER DO RETURN 1 END",
            "FUNCTION f($x: INTEGER[257]) RETURNS INTEGER DO RETURN 1 END",
            "FUNCTION ITEM() RETURNS INTEGER DO RETURN 1 END", "FUNCTION NOT() RETURNS INTEGER DO RETURN 1 END" };
        for (String source : invalid) assertFalse(
            new ProgramBuilder(source).build()
                .isSuccess(),
            source);
    }

    @Test
    void queryArgumentsEvaluateOnceInOrderAndBodiesShareOneQueryBudget() {
        DataCompiler compiler = compiler(
            "FUNCTION sum($a: INTEGER, $b: INTEGER) RETURNS INTEGER DO "
                + "RETURN $a + $b + LABEL(\"body\").count(ITEM(\"*\")) END");
        RecordingQueries queries = new RecordingQueries();
        DataExpression call = compiler
            .compile("sum(LABEL(\"a\").count(ITEM(\"*\")), LABEL(\"b\").count(ITEM(\"*\")))", 1, 0);
        DataRuntime runtime = new DataRuntime(null, 0, queries);
        assertEquals(
            6,
            call.evaluate(runtime)
                .asLong());
        assertEquals(Arrays.asList("a", "b", "body"), queries.names);
        for (QueryBudget budget : queries.budgets) assertSame(queries.budgets.get(0), budget);
        assertEquals(1, runtime.getFunctionCalls());
        assertTrue(
            compiler.getReferencedLabels()
                .containsAll(Arrays.asList("a", "b", "body")));
    }

    @Test
    void labelLiteralsInsideForwardCalleesAreCollectedAndNeverQueriedAtCompileTime() {
        Program program = program(
            "FUNCTION f() RETURNS INTEGER DO RETURN g() END "
                + "FUNCTION g() RETURNS INTEGER DO RETURN LABEL(\"Hidden Source\").count(ITEM(\"*\")) END "
                + "EVERY 20 TICKS DO LET $x = f() END");
        assertTrue(
            program.getReferencedLabels()
                .contains("Hidden Source"));
    }

    @Test
    void queryBudgetExhaustionInsideSecondCallCannotMintAnotherAllowance() {
        DataCompiler compiler = compiler(
            "FUNCTION f() RETURNS INTEGER DO RETURN LABEL(\"source\").count(ITEM(\"*\")) END");
        RecordingQueries queries = new RecordingQueries();
        queries.slotCharge = 9000;
        DataRuntime runtime = new DataRuntime(null, 0, queries);
        DataExpression call = compiler.compile("f()", 9, 4);
        assertEquals(
            1,
            call.evaluate(runtime)
                .asLong());
        assertTrue(
            assertThrows(IllegalArgumentException.class, () -> call.evaluate(runtime)).getMessage()
                .contains("Query slot work"));
        assertEquals(0, runtime.getFunctionDepth());
    }

    @Test
    void allLoopFormsAndEarlyReturnWorkInsideFunctions() {
        String functions = "FUNCTION f($values: INTEGER[3]) RETURNS INTEGER DO LET $sum = 0 "
            + "FOR $value IN $values DO SET $sum = $sum + $value END "
            + "FOR $i = 3 TO 1 STEP -1 DO SET $sum = $sum + $i END "
            + "WHILE ($sum < 20) DO SET $sum = $sum + 1 END RETURN $sum END";
        assertEquals(20, evaluate(functions, "f([1, 2, 3])").asLong());
        assertEquals(
            5,
            evaluate("FUNCTION f() RETURNS INTEGER DO WHILE TRUE DO RETURN 5 END RETURN 0 END", "f()").asLong());
        assertEquals(
            9,
            evaluate("FUNCTION f() RETURNS INTEGER DO FOR $value IN [9] DO RETURN $value END RETURN 0 END", "f()")
                .asLong());
    }

    @Test
    void loopCallAndExpressionLimitsAreSharedAcrossFramesAndDueTriggers() {
        Program program = program(
            "FUNCTION f() RETURNS INTEGER DO FOR $i = 1 TO 600 DO END RETURN 1 END "
                + "EVERY 20 TICKS DO LET $x = f() END EVERY 20 TICKS DO LET $y = f() END");
        ProgramContext context = context(null);
        assertTrue(
            assertThrows(IllegalArgumentException.class, () -> program.tick(context)).getMessage()
                .contains("Loop step"));
        assertEquals(
            1024,
            context.getDataRuntime()
                .getLoopSteps());
        assertEquals(
            0,
            context.getDataRuntime()
                .getFunctionDepth());
        DataCompiler compiler = compiler("FUNCTION f() RETURNS INTEGER DO RETURN 1 END");
        DataExpression call = compiler.compile("f()", 3, 0);
        DataRuntime runtime = new DataRuntime(null, 0);
        for (int i = 0; i < DataRuntime.MAX_FUNCTION_CALLS; i++) assertEquals(
            1,
            call.evaluate(runtime)
                .asLong());
        assertTrue(
            assertThrows(IllegalArgumentException.class, () -> call.evaluate(runtime)).getMessage()
                .contains("Function call budget"));
        assertEquals(256, runtime.getFunctionCalls());
        DataRuntime limited = new DataRuntime(null, 0);
        limited.work(16383);
        assertThrows(IllegalArgumentException.class, () -> call.evaluate(limited));
        assertEquals(0, limited.getFunctionDepth());
    }

    @Test
    void deepAndAmplifiedAcyclicCallsAreBoundedAndUnwindFrames() {
        StringBuilder chain = new StringBuilder();
        for (int i = 0; i < 17; i++) chain.append("FUNCTION f")
            .append(i)
            .append("() RETURNS INTEGER DO RETURN ")
            .append(i == 16 ? "1" : "f" + (i + 1) + "()")
            .append(" END ");
        DataCompiler compiler = compiler(chain.toString());
        DataRuntime runtime = new DataRuntime(null, 0);
        assertTrue(
            assertThrows(
                IllegalArgumentException.class,
                () -> compiler.compile("f0()", 1, 0)
                    .evaluate(runtime)).getMessage()
                        .contains("Function call depth"));
        assertEquals(0, runtime.getFunctionDepth());
        assertEquals(16, runtime.getFunctionCalls());
        StringBuilder dag = new StringBuilder();
        for (int i = 0; i < 9; i++) dag.append("FUNCTION f")
            .append(i)
            .append("() RETURNS INTEGER DO RETURN ")
            .append(i == 8 ? "1" : "f" + (i + 1) + "() + f" + (i + 1) + "()")
            .append(" END ");
        DataCompiler amplified = compiler(dag.toString());
        DataRuntime bounded = new DataRuntime(null, 0);
        assertTrue(
            assertThrows(
                IllegalArgumentException.class,
                () -> amplified.compile("f0()", 1, 0)
                    .evaluate(bounded)).getMessage()
                        .contains("Function call budget"));
        assertEquals(0, bounded.getFunctionDepth());
    }

    @Test
    void compiledFunctionsNeverShareMutableFramesBetweenManagers() {
        Program program = program(
            "STATE $value = 0 FUNCTION increment($x: INTEGER) RETURNS INTEGER DO LET $local = $x "
                + "SET $local = $local + 1 RETURN $local END EVERY 20 TICKS DO SET $value = increment($value) END");
        MemoryState first = new MemoryState(program.getStateSchema());
        MemoryState second = new MemoryState(program.getStateSchema());
        program.tick(context(first));
        program.tick(context(first));
        program.tick(context(second));
        assertEquals(
            2,
            first.read(0)
                .asLong());
        assertEquals(
            1,
            second.read(0)
                .asLong());
    }

    @Test
    void keywordsRetainLegacyLabelResourceAndMemberMeaningAndVisualNamesFunctions() {
        Program legacy = program("EVERY 20 TICKS DO INPUT function FROM returns OUTPUT return TO function END");
        assertFalse(legacy.hasDataFeatures());
        assertEquals(
            6,
            evaluate(
                "FUNCTION value($s: {function: INTEGER, returns: INTEGER, return: INTEGER}) RETURNS INTEGER DO "
                    + "RETURN $s.function + $s.returns + $s.return END",
                "value({function: 1, returns: 2, return: 3})").asLong());
        ProgramBuildResult result = new ProgramBuilder(
            "FUNCTION f() RETURNS INTEGER DO RETURN 1 END EVERY 20 TICKS DO LET $x = f() END").buildWithSyntax();
        assertTrue(result.isSuccess(), result.getErrorMessage());
        VisualCapabilityReport report = VisualCapabilityAnalyzer.analyze(result);
        assertTrue(
            report.getBlockers()
                .stream()
                .anyMatch(blocker -> blocker.kind == VisualCapabilityReport.Kind.FUNCTION_DEFINITION));
        assertTrue(
            report.getBlockers()
                .stream()
                .anyMatch(blocker -> blocker.kind == VisualCapabilityReport.Kind.FUNCTION_CALL));
    }

    @Test
    void unusedDefinitionsDoNotAllocateRuntimeFramesForLegacyTriggers() {
        Program program = program(
            "FUNCTION f($x: INTEGER) RETURNS INTEGER DO RETURN $x + LABEL(\"huge storage group\").count(ITEM(\"*\")) END EVERY 20 TICKS DO FORGET END");
        assertFalse(program.hasDataFeatures());
        assertTrue(
            program.getReferencedLabels()
                .isEmpty());
        program.tick(context(null));
        Program reached = program(
            "FUNCTION unused() RETURNS INTEGER DO RETURN LABEL(\"unused\").count(ITEM(\"*\")) END "
                + "FUNCTION outer() RETURNS INTEGER DO RETURN inner() END "
                + "FUNCTION inner() RETURNS INTEGER DO RETURN LABEL(\"reached\").count(ITEM(\"*\")) END "
                + "EVERY 20 TICKS DO IF FALSE THEN LET $count = outer() END INPUT FROM direct END");
        assertTrue(reached.hasDataFeatures());
        assertTrue(
            reached.getReferencedLabels()
                .containsAll(Arrays.asList("reached", "direct")));
        assertFalse(
            reached.getReferencedLabels()
                .contains("unused"));
    }

    @Test
    void signaturesAndCumulativeFrameAllocationsHaveExplicitBounds() {
        StringBuilder parameters = new StringBuilder();
        for (int i = 0; i < 17; i++) {
            if (i > 0) parameters.append(", ");
            parameters.append("$p")
                .append(i)
                .append(": INTEGER");
        }
        assertFalse(
            new ProgramBuilder("FUNCTION f(" + parameters + ") RETURNS INTEGER DO RETURN 1 END").build()
                .isSuccess());
        StringBuilder declarations = new StringBuilder("FUNCTION f() RETURNS INTEGER DO IF FALSE THEN ");
        for (int i = 0; i < 256; i++) declarations.append("LET $v")
            .append(i)
            .append(" = 1 ");
        declarations.append("END RETURN 1 END");
        DataCompiler compiler = compiler(declarations.toString());
        DataRuntime runtime = new DataRuntime(null, 0);
        DataExpression call = compiler.compile("f()", 1, 0);
        for (int i = 0; i < 64; i++) assertEquals(
            1,
            call.evaluate(runtime)
                .asLong());
        assertTrue(
            assertThrows(IllegalArgumentException.class, () -> call.evaluate(runtime)).getMessage()
                .contains("collection budget"));
        assertEquals(0, runtime.getFunctionDepth());
        assertFalse(
            new ProgramBuilder(
                declarations.toString()
                    .replace("RETURN 1", "LET $overflow = 1 RETURN 1")).build()
                        .isSuccess());
    }

    @Test
    void publishedPlansRetainNoCompilerParseTreeOrInvocationState() throws Exception {
        Program program = program(
            "STATE $result = 0 FUNCTION outer($x: INTEGER) RETURNS INTEGER DO "
                + "LET $sum = 0 FOR $i = 1 TO 2 DO SET $sum = $sum + inner($x) END RETURN $sum END "
                + "FUNCTION inner($x: INTEGER) RETURNS INTEGER DO RETURN $x + 1 END "
                + "EVERY 20 TICKS DO SET $result = outer(3) END");
        assertPlanOwnership(program, new java.util.IdentityHashMap<>());
    }

    private static void assertPlanOwnership(Object value, java.util.IdentityHashMap<Object, Boolean> visited)
        throws Exception {
        if (value == null || visited.put(value, Boolean.TRUE) != null) return;
        assertFalse(
            value instanceof DataCompiler || value instanceof FunctionCompiler
                || value instanceof DataRuntime
                || value instanceof org.antlr.v4.runtime.tree.ParseTree
                || value instanceof StateAccess,
            "Runtime plan retained " + value.getClass()
                .getName());
        if (value instanceof Iterable<?>) {
            for (Object item : (Iterable<?>) value) assertPlanOwnership(item, visited);
            return;
        }
        Class<?> type = value.getClass();
        if (!type.getName()
            .startsWith("ca.teamdman.sfml.")) return;
        for (; type != null && type.getName()
            .startsWith("ca.teamdman.sfml."); type = type.getSuperclass()) {
            for (java.lang.reflect.Field field : type.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || field.getType()
                    .isPrimitive()) continue;
                field.setAccessible(true);
                assertPlanOwnership(field.get(value), visited);
            }
        }
    }

    @Test
    void queryFunctionsComposeWithCallerTransfersAndObserveTheSameTickResult() throws Exception {
        Fixture fixture = new Fixture();
        fixture.stock(0, 8);
        Program program = program(
            "STATE $after = 0 "
                + "FUNCTION contents($source: LABEL_REF) RETURNS INTEGER DO RETURN $source.count(ITEM(\"*\")) END "
                + "FUNCTION transferable($source: LABEL_REF, $reserve: INTEGER) RETURNS INTEGER DO RETURN contents($source) - $reserve END "
                + "EVERY 20 TICKS DO INPUT FROM source OUTPUT (transferable(LABEL(\"source\"), 2)) TO dest "
                + "SET $after = contents(LABEL(\"source\")) END");
        MemoryState state = new MemoryState(program.getStateSchema());
        ProgramContext context = new ProgramContext(
            fixture.world,
            null,
            new ca.teamdman.sfm.common.network.CableNetwork(fixture.world, MANAGER),
            fixture.labels,
            20,
            false);
        context.setStateAccess(state);
        program.tick(context);
        assertEquals(
            2,
            state.read(0)
                .asLong(),
            "The helper reads the inventory after the earlier OUTPUT");
        assertEquals(2, count(fixture.source));
        assertEquals(6, count(fixture.dest));
        assertEquals(8, count(fixture.source) + count(fixture.dest));
        assertEquals(
            3,
            context.getDataRuntime()
                .getFunctionCalls());
    }

    private static ProgramContext context(StateAccess state) {
        ProgramContext context = new ProgramContext(null, null, null, null, 20, false);
        context.setStateAccess(state);
        return context;
    }

    private static final class MemoryState implements StateAccess {

        private final DataValue[] values;

        MemoryState(ProgramStateSchema schema) {
            values = new DataValue[schema.size()];
            for (int i = 0; i < values.length; i++) values[i] = schema.getDeclarations()
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

    private static final class RecordingQueries implements DataQueries {

        private final List<String> names = new ArrayList<>();
        private final List<QueryBudget> budgets = new ArrayList<>();
        private int slotCharge;

        @Override
        public long count(LabelReference source, ResourceSelector selector, QueryBudget budget) {
            names.add(source.getName());
            budgets.add(budget);
            budget.chargeSlots(slotCharge);
            return names.size();
        }

        @Override
        public boolean empty(LabelReference source, ResourceSelector selector, QueryBudget budget) {
            return count(source, selector, budget) == 0;
        }

        @Override
        public boolean available(LabelReference source, ResourceSelector selector, QueryBudget budget) {
            count(source, selector, budget);
            return true;
        }
    }
}
