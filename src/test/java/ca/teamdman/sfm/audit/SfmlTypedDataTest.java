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
import ca.teamdman.sfml.data.ProgramStateSchema;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;

class SfmlTypedDataTest {

    @Test
    void literalConstructorsPreserveCaseAndRemainTypedInsideCollections() {
        DataCompiler compiler = new DataCompiler();
        DataExpression label = compiler.compile("lAbEl(\"Exact Case\")", 1, 0);
        assertEquals(DataType.LABEL_REF, label.getType());
        assertEquals(
            "Exact Case",
            label.evaluate(new DataRuntime(null, 0))
                .asLabel()
                .getName());
        DataExpression selector = compiler.compile("iTeM(\"minecraft:iron_ingot\")", 1, 0);
        assertEquals(DataType.RESOURCE_SELECTOR, selector.getType());
        assertNotNull(
            selector.evaluate(new DataRuntime(null, 0))
                .asSelector());
        DataValue nested = compiler.compile("{labels: [LABEL(\"Exact Case\")], count: 3}", 1, 0)
            .evaluate(new DataRuntime(null, 0));
        assertFalse(
            nested.getType()
                .isPersistable());
        assertEquals(
            3,
            nested.get("count")
                .asLong());
        assertEquals(
            "Exact Case",
            nested.get("labels")
                .get(0)
                .asLabel()
                .getName());
        assertThrows(
            IllegalArgumentException.class,
            () -> nested.get("labels")
                .get(0)
                .asString());
        assertTrue(
            DataType.struct(Collections.singletonMap("safe", DataType.array(DataType.INTEGER, 2)))
                .isPersistable());
    }

    @Test
    void constructorsRejectDynamicStringsUnsupportedSelectorsAndCoercion() {
        for (String source : Arrays.asList(
            "LABEL(\"a\" + \"b\")",
            "LABEL(1)",
            "LABEL()",
            "LABEL(\"a\", \"b\")",
            "ITEM(\"minecraft:iron_*\")",
            "ITEM(\"#ore:ingotIron\")",
            "ITEM(\"minecraft:iron_ingot{tag:1}\")",
            "FLUID(\"water\")",
            "ORE(\"ingotIron\" + \"\")",
            "\"source\".count(ITEM(\"*\"))",
            "LABEL(\"source\").count(\"minecraft:iron_ingot\")",
            "LABEL(\"source\").count()",
            "LABEL(\"source\").count(ITEM(\"*\"), ITEM(\"*\"))",
            "LABEL(\"source\").insert(ITEM(\"*\"))")) {
            assertThrows(IllegalArgumentException.class, () -> new DataCompiler().compile(source, 1, 0), source);
        }
        DataCompiler compiler = new DataCompiler();
        compiler.beginTrigger();
        compiler.pushScope();
        compiler.declareLocal("name", DataType.STRING);
        assertThrows(IllegalArgumentException.class, () -> compiler.compile("LABEL($name)", 1, 0));
        assertThrows(IllegalArgumentException.class, () -> compiler.compile("ITEM($name)", 1, 0));
        assertThrows(IllegalArgumentException.class, () -> compiler.compile("ORE($name)", 1, 0));
    }

    @Test
    void liveObservationsNeverFoldAndEvaluateReceiverThenArgumentExactlyOnce() {
        DataCompiler compiler = new DataCompiler();
        RecordingQueries queries = new RecordingQueries();
        DataExpression expression = compiler.compile(
            "[LABEL(\"receiver\")][LABEL(\"receiver-index\").count(ITEM(\"*\"))]"
                + ".CoUnT([ITEM(\"*\")][LABEL(\"argument-index\").count(ITEM(\"*\"))])",
            1,
            0);
        assertTrue(queries.calls.isEmpty());
        DataRuntime runtime = new DataRuntime(null, 0, queries);
        assertEquals(
            7,
            expression.evaluate(runtime)
                .asLong());
        assertEquals(Arrays.asList("receiver-index", "argument-index", "receiver"), queries.calls);
        QueryBudget budget = queries.budgets.get(0);
        for (QueryBudget used : queries.budgets) assertSame(budget, used);
        runtime.endTrigger();
        runtime.beginTrigger();
        assertFalse(
            compiler.compile("LABEL(\"receiver\").empty(ITEM(\"*\"))", 1, 0)
                .evaluate(runtime)
                .asBoolean());
        assertSame(budget, queries.budgets.get(queries.budgets.size() - 1));
        assertTrue(
            compiler.compile("LABEL(\"receiver\").available(ITEM(\"*\"))", 1, 0)
                .evaluate(new DataRuntime(null, 0, queries))
                .asBoolean());
        assertNotSame(budget, queries.budgets.get(queries.budgets.size() - 1));
    }

    @Test
    void shortCircuitSkipsObservationButKeepsEveryReferencedLiteralLabel() {
        DataCompiler compiler = new DataCompiler();
        RecordingQueries queries = new RecordingQueries();
        DataRuntime runtime = new DataRuntime(null, 0, queries);
        assertFalse(
            compiler.compile("FALSE AND LABEL(\"unreachable\").empty(ITEM(\"*\"))", 1, 0)
                .evaluate(runtime)
                .asBoolean());
        assertTrue(
            compiler.compile("TRUE OR LABEL(\"also-unreachable\").available(ITEM(\"*\"))", 1, 0)
                .evaluate(runtime)
                .asBoolean());
        compiler.compile("{labels: [LABEL(\"nested\")]}.labels[0]", 1, 0);
        assertTrue(queries.calls.isEmpty());
        assertEquals(
            3,
            compiler.getReferencedLabels()
                .size());
        assertTrue(
            compiler.getReferencedLabels()
                .containsAll(Arrays.asList("unreachable", "also-unreachable", "nested")));
        assertThrows(
            UnsupportedOperationException.class,
            () -> compiler.getReferencedLabels()
                .add("mutation"));
        assertEquals(
            9,
            compiler.compile("{count: [8,9]}.count[1]", 1, 0)
                .evaluate(runtime)
                .asLong());
    }

    @Test
    void stateRejectsOpaqueHandlesRecursivelyAndQueriesEvenInDeadBranches() {
        for (String source : Arrays.asList(
            "LABEL(\"source\")",
            "ITEM(\"*\")",
            "[LABEL(\"source\")]",
            "{nested: {selector: ITEM(\"*\")}}",
            "LABEL(\"source\").count(ITEM(\"*\"))",
            "FALSE AND LABEL(\"source\").empty(ITEM(\"*\"))",
            "TRUE OR LABEL(\"source\").available(ITEM(\"*\"))")) {
            DataCompiler compiler = new DataCompiler();
            assertThrows(IllegalArgumentException.class, () -> compiler.declareState("bad", source, 1, 0), source);
            assertEquals(
                0,
                compiler.getStateSchema()
                    .size());
        }
        assertThrows(
            IllegalArgumentException.class,
            () -> new ProgramStateSchema.Declaration("bad", DataValue.label(LabelReference.literal("source"))));
        assertThrows(
            IllegalArgumentException.class,
            () -> new ProgramStateSchema.Declaration(
                "bad",
                DataValue
                    .struct(Collections.singletonMap("selector", DataValue.selector(ResourceSelector.item("*"))))));
        DataExpression live = new DataCompiler().compile("LABEL(\"source\").count(ITEM(\"*\"))", 1, 0);
        assertThrows(IllegalArgumentException.class, () -> live.evaluate(new DataRuntime(null, 0)));
    }

    @Test
    void persistabilityIsCachedThroughDeepWideTypesAndConstantFoldedHandles() {
        DataType deepSafe = DataType.INTEGER, deepHandle = DataType.LABEL_REF;
        for (int i = 0; i < 31; i++) {
            deepSafe = DataType.array(deepSafe, 1);
            deepHandle = DataType.array(deepHandle, 1);
        }
        assertTrue(deepSafe.isPersistable());
        assertFalse(deepHandle.isPersistable());
        java.util.Map<String, DataType> fields = new java.util.TreeMap<>();
        for (int i = 0; i < 256; i++) fields.put("field" + i, DataType.INTEGER);
        DataType safe = DataType.struct(fields);
        fields.put("field255", DataType.array(DataType.RESOURCE_SELECTOR, 1));
        DataType opaque = DataType.struct(fields);
        assertTrue(safe.isPersistable(), "Later changes to a constructor map cannot change an immutable cached type");
        assertFalse(opaque.isPersistable());
        DataCompiler compiler = new DataCompiler();
        assertFalse(
            compiler.compile("[LABEL(\"source\")][0]", 1, 0)
                .getType()
                .isPersistable());
        assertFalse(
            compiler.compile("{selector: ITEM(\"*\")}.selector", 1, 0)
                .getType()
                .isPersistable());
        assertTrue(
            compiler.compile("{value: 3, selector: ITEM(\"*\")}.value", 1, 0)
                .getType()
                .isPersistable());
    }

    private static final class RecordingQueries implements DataQueries {

        final List<String> calls = new ArrayList<>();
        final List<QueryBudget> budgets = new ArrayList<>();

        @Override
        public long count(LabelReference label, ResourceSelector selector, QueryBudget budget) {
            calls.add(label.getName());
            budgets.add(budget);
            budget.chargeSlot();
            return label.getName()
                .equals("receiver") ? 7 : 0;
        }

        @Override
        public boolean empty(LabelReference label, ResourceSelector selector, QueryBudget budget) {
            calls.add(label.getName());
            budgets.add(budget);
            budget.chargeProvider();
            return false;
        }

        @Override
        public boolean available(LabelReference label, ResourceSelector selector, QueryBudget budget) {
            calls.add(label.getName());
            budgets.add(budget);
            budget.chargePosition();
            return true;
        }
    }
}
