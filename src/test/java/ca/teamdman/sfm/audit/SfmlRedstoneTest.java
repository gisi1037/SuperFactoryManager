package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.ast.Label;
import ca.teamdman.sfml.ast.LabelAccess;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.ast.RedstoneOutputStatement;
import ca.teamdman.sfml.ast.ResourceQuantity;
import ca.teamdman.sfml.ast.RoundRobin;
import ca.teamdman.sfml.ast.SideQualifier;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataQueries;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class SfmlRedstoneTest {

    @Test
    void typedObservationsHaveFixedTypesAndShareTheInvocationBudgetWithItemQueries() {
        RecordingQueries queries = new RecordingQueries();
        DataCompiler compiler = new DataCompiler();
        QueryBudget shared = new QueryBudget();
        shared.chargeProviders(3);
        DataRuntime runtime = new DataRuntime(null, 0, queries, shared);
        assertEquals(
            11,
            compiler.compile("LABEL(\"sensor\").side(\"north\").redstone()", 2, 4)
                .evaluate(runtime)
                .asLong());
        assertTrue(
            compiler.compile("LABEL(\"sensor\").powered()", 2, 4)
                .evaluate(runtime)
                .asBoolean());
        assertTrue(
            compiler.compile("LABEL(\"sensor\").redstoneAvailable()", 2, 4)
                .evaluate(runtime)
                .asBoolean());
        assertEquals(
            4,
            compiler.compile("LABEL(\"stock\").count(ITEM(\"*\"))", 2, 4)
                .evaluate(runtime)
                .asLong());
        assertEquals(Arrays.asList("level", "powered", "available", "item"), queries.calls);
        for (QueryBudget seen : queries.budgets) assertSame(shared, seen);
        assertEquals(7, shared.getProviderCalls());
        assertEquals(
            4,
            queries.labels.get(0)
                .getSideMask());
        assertEquals(new java.util.HashSet<>(Arrays.asList("sensor", "stock")), compiler.getReferencedLabels());
    }

    @Test
    void queryArgumentsWrongReceiversAndLiveStateDefaultsFailBeforeObservation() {
        for (String expression : Arrays.asList(
            "LABEL(\"sensor\").redstone(1)",
            "LABEL(\"sensor\").powered(TRUE)",
            "LABEL(\"sensor\").redstoneAvailable(ITEM(\"*\"))",
            "ITEM(\"*\").redstone()",
            "1.powered()")) {
            assertThrows(
                IllegalArgumentException.class,
                () -> new DataCompiler().compile(expression, 1, 0),
                expression);
        }
        assertThrows(
            IllegalArgumentException.class,
            () -> new DataCompiler().declareState("signal", "LABEL(\"sensor\").redstone()", 1, 0));
        assertThrows(
            IllegalArgumentException.class,
            () -> new DataCompiler().declareState("signal", "LABEL(\"sensor\").powered()", 1, 0));
        assertThrows(
            IllegalArgumentException.class,
            () -> new DataCompiler().declareState("signal", "LABEL(\"sensor\").redstoneAvailable()", 1, 0));
    }

    @Test
    void explicitDefaultSlotSelectionIsStillRejectedAndShortCircuitDoesNotObserve() {
        RecordingQueries queries = new RecordingQueries();
        DataRuntime runtime = new DataRuntime(null, 0, queries);
        DataCompiler compiler = new DataCompiler();
        for (String method : Arrays.asList("redstone", "powered", "redstoneAvailable")) {
            DataExpression expression = compiler
                .compile("LABEL(\"sensor\").slots(0, 2147483647).side(\"north\")." + method + "()", 3, 5);
            assertThrows(IllegalArgumentException.class, () -> expression.evaluate(runtime));
        }
        assertFalse(
            compiler.compile("FALSE AND LABEL(\"sensor\").powered()", 1, 0)
                .evaluate(runtime)
                .asBoolean());
        assertTrue(
            compiler.compile("TRUE OR LABEL(\"sensor\").powered()", 1, 0)
                .evaluate(runtime)
                .asBoolean());
        assertTrue(queries.calls.isEmpty());
        assertTrue(
            LabelReference.literal("sensor")
                .slots(0, Integer.MAX_VALUE)
                .side(2)
                .hasSlotSelection());
        assertFalse(
            LabelReference.literal("sensor")
                .side(2)
                .hasSlotSelection());
    }

    @Test
    void playerQueryHelpersCompileAndCollectOnlyReachableLabels() {
        Program program = program(
            "FUNCTION controls.on($port: LABEL_REF) RETURNS BOOLEAN DO RETURN $port.side(\"north\").powered() END "
                + "EVERY 20 TICKS DO IF (controls.on(LABEL(\"sensor\"))) THEN OUTPUT 15 REDSTONE LEVEL TO emitter END END");
        assertTrue(program.hasDataFeatures());
        assertTrue(
            program.getReferencedLabels()
                .containsAll(Arrays.asList("sensor", "emitter")));
        program(program.toString());
    }

    @Test
    void dynamicOutputEvaluatesOnceAndFaultsBeforeRoundRobinSelection() {
        ProgramContext context = new ProgramContext(null, null, null, null, 20, false);
        context.beginDataInvocation(0, false);
        int[] reads = { 0 };
        DataExpression expression = new DataExpression() {

            @Override
            public DataType getType() {
                return DataType.INTEGER;
            }

            @Override
            public DataValue evaluate(DataRuntime runtime) {
                reads[0]++;
                return DataValue.integer(16);
            }
        };
        RoundRobin roundRobin = new RoundRobin(RoundRobin.Mode.BY_LABEL);
        LabelAccess access = new LabelAccess(
            Arrays.asList(new Label("a"), new Label("b")),
            SideQualifier.ALL,
            null,
            roundRobin);
        RedstoneOutputStatement output = new RedstoneOutputStatement(
            access,
            new ResourceQuantity(expression, false),
            false);
        assertThrows(IllegalArgumentException.class, () -> output.tick(context));
        assertEquals(1, reads[0]);
        assertEquals(0, roundRobin.nextIndex(2), "Invalid expression must not advance destination selection");
        for (String expressionSource : Arrays.asList("-1", "16", "9223372036854775807")) {
            RedstoneOutputStatement invalid = new RedstoneOutputStatement(
                access,
                new ResourceQuantity(new DataCompiler().compile(expressionSource, 1, 0), false),
                false);
            assertThrows(IllegalArgumentException.class, () -> invalid.tick(context));
        }
    }

    @Test
    void literalOutputsKeepClampingAndFirstOptionalQuantitySemantics() {
        for (String source : Arrays.asList(
            "OUTPUT 99 REDSTONE LEVEL TO emitter",
            "OUTPUT REDSTONE LEVEL 99 TO emitter",
            "OUTPUT 99 REDSTONE LEVEL 3 TO emitter",
            "TO emitter OUTPUT 99 REDSTONE LEVEL 3")) {
            Program program = program("EVERY 20 TICKS DO " + source + " END");
            RedstoneOutputStatement output = (RedstoneOutputStatement) program.getTriggers()
                .get(0)
                .getBlock()
                .getStatements()
                .get(0);
            assertEquals(15, output.getLevel());
            assertFalse(output.isDynamic());
            assertFalse(program.hasDataFeatures());
        }
    }

    private static Program program(String source) {
        ProgramBuildResult result = new ProgramBuilder(source).build();
        assertTrue(result.isSuccess(), result.getErrorMessage());
        return result.getProgram();
    }

    private static final class RecordingQueries implements DataQueries {

        final List<String> calls = new ArrayList<>();
        final List<QueryBudget> budgets = new ArrayList<>();
        final List<LabelReference> labels = new ArrayList<>();

        void record(String method, LabelReference label, QueryBudget budget) {
            budget.chargeProvider();
            calls.add(method);
            budgets.add(budget);
            labels.add(label);
        }

        @Override
        public long redstone(LabelReference source, QueryBudget budget) {
            record("level", source, budget);
            return 11;
        }

        @Override
        public boolean powered(LabelReference source, QueryBudget budget) {
            record("powered", source, budget);
            return true;
        }

        @Override
        public boolean redstoneAvailable(LabelReference source, QueryBudget budget) {
            record("available", source, budget);
            return true;
        }

        @Override
        public long count(LabelReference source, ResourceSelector selector, QueryBudget budget) {
            record("item", source, budget);
            return 4;
        }

        @Override
        public boolean empty(LabelReference source, ResourceSelector selector, QueryBudget budget) {
            return false;
        }

        @Override
        public boolean available(LabelReference source, ResourceSelector selector, QueryBudget budget) {
            return true;
        }
    }
}
