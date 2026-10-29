package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataQueries;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;

class SfmlQueryAccessTest {

    @Test
    void modifiersAreImmutablePreserveOtherRestrictionsAndUseLastCallForTheirOwnRestriction() {
        LabelReference original = LabelReference.literal("Exact Case");
        LabelReference selected = original.side(2)
            .slots(3, 8);
        assertEquals("Exact Case", selected.getName());
        assertEquals(4, selected.getSideMask());
        assertEquals(3, selected.getFirstSlot());
        assertEquals(8, selected.getLastSlot());
        assertEquals(63, original.getSideMask());
        assertEquals(0, original.getFirstSlot());
        assertEquals(Integer.MAX_VALUE, original.getLastSlot());
        LabelReference replaced = selected.side(1)
            .slots(4, 4);
        assertEquals(2, replaced.getSideMask());
        assertEquals(4, replaced.getFirstSlot());
        assertEquals(4, replaced.getLastSlot());
        assertEquals(4, selected.getSideMask());
        assertEquals(3, selected.getFirstSlot());
        assertNotSame(original, selected);
        assertEquals("LABEL(\"Exact Case\").side(\"north\").slots(3, 8)", selected.toString());
    }

    @Test
    void everyAbsoluteSideAndLegacyVerticalAliasIsResolvedWithoutWorldAccess() {
        String[] names = { "down", "up", "north", "south", "west", "east", "BOTTOM", "Top" };
        int[] directions = { 0, 1, 2, 3, 4, 5, 0, 1 };
        for (int i = 0; i < names.length; i++) {
            DataCompiler compiler = new DataCompiler();
            LabelReference label = compiler.compile("LABEL(\"source\").side(\"" + names[i] + "\").slots(0, 8)", 1, 0)
                .evaluate(new DataRuntime(null, 0))
                .asLabel();
            assertEquals(1 << directions[i], label.getSideMask());
            assertEquals(Collections.singleton("source"), compiler.getReferencedLabels());
        }
    }

    @Test
    void malformedModifierCallsTypesAndRelativeSidesAreRejected() {
        for (String suffix : Arrays.asList(
            ".side()",
            ".side(1)",
            ".side(\"north\", \"south\")",
            ".side(\"north\" + \"\")",
            ".side(\"front\")",
            ".side(\"back\")",
            ".side(\"left\")",
            ".side(\"right\")",
            ".side(\" north\")",
            ".slots()",
            ".slots(0)",
            ".slots(0, 1, 2)",
            ".slots(TRUE, 1)",
            ".slots(0, \"1\")",
            ".empty(ITEM(\"*\"), ITEM(\"*\"))")) {
            assertThrows(
                IllegalArgumentException.class,
                () -> new DataCompiler().compile("LABEL(\"source\")" + suffix, 1, 0),
                suffix);
        }
        DataCompiler compiler = new DataCompiler();
        compiler.beginTrigger();
        compiler.pushScope();
        compiler.declareLocal("face", DataType.STRING);
        assertThrows(IllegalArgumentException.class, () -> compiler.compile("LABEL(\"source\").side($face)", 1, 0));
        assertThrows(IllegalArgumentException.class, () -> compiler.compile("ITEM(\"*\").side(\"up\")", 1, 0));
        assertThrows(IllegalArgumentException.class, () -> compiler.compile("3.slots(0, 1)", 1, 0));
    }

    @Test
    void invalidSlotBoundsFaultWithLocationBeforeAnyObservation() {
        for (String bounds : Arrays
            .asList("-1, 0", "2, 1", "0, 2147483648", "9223372036854775807, 9223372036854775807")) {
            RecordingQueries queries = new RecordingQueries();
            DataExpression expression = new DataCompiler()
                .compile("LABEL(\"source\").slots(" + bounds + ").available()", 7, 4);
            IllegalArgumentException fault = assertThrows(
                IllegalArgumentException.class,
                () -> expression.evaluate(new DataRuntime(null, 0, queries)));
            assertTrue(
                fault.getMessage()
                    .startsWith("Line 7, Col 4:"),
                fault.getMessage());
            assertTrue(queries.labels.isEmpty());
        }
        LabelReference edge = LabelReference.literal("source")
            .slots(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, edge.getFirstSlot());
    }

    @Test
    void runtimeBoundsEvaluateReceiverThenFirstThenLastOnceAndKeepSharedQueryBudget() {
        RecordingQueries queries = new RecordingQueries();
        DataExpression expression = new DataCompiler().compile(
            "[LABEL(\"source\")][LABEL(\"receiver\").count(ITEM(\"*\"))]"
                + ".side(\"north\").slots(LABEL(\"first\").count(ITEM(\"*\")), LABEL(\"last\").count(ITEM(\"*\"))).available()",
            1,
            0);
        assertTrue(
            expression.evaluate(new DataRuntime(null, 0, queries))
                .asBoolean());
        assertEquals(Arrays.asList("receiver", "first", "last", "source"), queries.names);
        LabelReference label = queries.labels.get(3);
        assertEquals(4, label.getSideMask());
        assertEquals(1, label.getFirstSlot());
        assertEquals(3, label.getLastSlot());
        for (QueryBudget budget : queries.budgets) assertSame(queries.budgets.get(0), budget);
    }

    @Test
    void noArgumentPredicatesUseItemWildcardAndNeverQueryDuringCompilationOrShortCircuit() {
        DataCompiler compiler = new DataCompiler();
        RecordingQueries queries = new RecordingQueries();
        DataRuntime runtime = new DataRuntime(null, 0, queries);
        assertFalse(
            compiler.compile("LABEL(\"source\").empty()", 1, 0)
                .evaluate(runtime)
                .asBoolean());
        assertTrue(
            compiler.compile("LABEL(\"source\").available()", 1, 0)
                .evaluate(runtime)
                .asBoolean());
        assertEquals(Arrays.asList("source", "source"), queries.names);
        assertEquals(Arrays.asList("*", "*"), queries.selectors);
        assertFalse(
            compiler.compile("FALSE AND LABEL(\"dead\").side(\"up\").slots(0, 1).empty()", 1, 0)
                .evaluate(runtime)
                .asBoolean());
        assertEquals(2, queries.names.size());
        assertTrue(
            compiler.getReferencedLabels()
                .containsAll(Arrays.asList("source", "dead")));
        assertThrows(IllegalArgumentException.class, () -> compiler.compile("LABEL(\"source\").count()", 1, 0));
        assertThrows(
            IllegalArgumentException.class,
            () -> compiler.declareState("bad", "LABEL(\"source\").slots(0, 1)", 1, 0));
        assertThrows(
            IllegalArgumentException.class,
            () -> compiler.declareState("bad", "FALSE AND LABEL(\"source\").empty()", 1, 0));
    }

    @Test
    void dynamicModifiersUseExpressionWorkAndAllocationBudgetsWithoutCreatingAQueryService() {
        DataCompiler compiler = new DataCompiler();
        compiler.beginTrigger();
        compiler.pushScope();
        DataCompiler.Binding binding = compiler.declareLocal("source", DataType.LABEL_REF);
        DataExpression expression = compiler.compile("$source.side(\"up\").slots(0, 1)", 1, 0);
        DataRuntime runtime = new DataRuntime(null, 1);
        binding.write(runtime, DataValue.label(LabelReference.literal("source")));
        assertEquals(
            1,
            expression.evaluate(runtime)
                .asLabel()
                .getLastSlot());
        assertEquals(2, runtime.getAllocatedCells());
        runtime.allocate(16382);
        assertThrows(IllegalArgumentException.class, () -> expression.evaluate(runtime));
        DataRuntime exhaustedWork = new DataRuntime(null, 1);
        binding.write(exhaustedWork, DataValue.label(LabelReference.literal("source")));
        exhaustedWork.work(16383);
        assertThrows(IllegalArgumentException.class, () -> expression.evaluate(exhaustedWork));
    }

    private static final class RecordingQueries implements DataQueries {

        final List<String> names = new ArrayList<>(), selectors = new ArrayList<>();
        final List<LabelReference> labels = new ArrayList<>();
        final List<QueryBudget> budgets = new ArrayList<>();

        private void record(LabelReference label, ResourceSelector selector, QueryBudget budget) {
            names.add(label.getName());
            labels.add(label);
            selectors.add(selector.getId());
            budgets.add(budget);
            budget.chargeProvider();
        }

        public long count(LabelReference label, ResourceSelector selector, QueryBudget budget) {
            record(label, selector, budget);
            return label.getName()
                .equals("first") ? 1
                    : label.getName()
                        .equals("last") ? 3 : 0;
        }

        public boolean empty(LabelReference label, ResourceSelector selector, QueryBudget budget) {
            record(label, selector, budget);
            return false;
        }

        public boolean available(LabelReference label, ResourceSelector selector, QueryBudget budget) {
            record(label, selector, budget);
            return true;
        }
    }
}
