package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.client.blueprint.BlueprintDocument;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Pin;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;
import ca.teamdman.sfml.program_builder.VisualCapabilityReport.Kind;

class VisualDataLoopTest {

    private static BlueprintGraph supported(String source) {
        BlueprintCompiler.ImportResult imported = BlueprintCompiler.inspectText(source);
        assertTrue(imported.report.isAvailable(), imported.report.getMessage());
        assertNotNull(imported.graph);
        String generated = BlueprintCompiler.generate(imported.graph);
        ProgramBuildResult built = new ProgramBuilder(generated).build();
        assertTrue(built.isSuccess(), built.getErrorMessage());
        assertEquals(generated, BlueprintCompiler.generate(BlueprintCompiler.importText(generated)));
        return imported.graph;
    }

    private static Node first(BlueprintGraph graph, Type type) {
        return graph.nodes()
            .stream()
            .filter(node -> node.type == type)
            .findFirst()
            .get();
    }

    @Test
    void mixedRootDeclarationsKeepSourceOrderAndCanBeInsertedAfterTriggers() {
        BlueprintGraph imported = supported(
            "LET $before = 1; STATE $sum = 0; LET $after = $before + 1; " + "EVERY 20 TICKS DO SET $sum = $after END");
        assertEquals(
            Arrays.asList(Type.GLOBAL_LET, Type.STATE, Type.GLOBAL_LET, Type.TIMER),
            imported.roots()
                .stream()
                .map(node -> node.type)
                .collect(Collectors.toList()));
        BlueprintGraph graph = new BlueprintGraph();
        Node timer = new Node(Type.TIMER, 20, 20), state = new Node(Type.STATE, 20, 100),
            global = new Node(Type.GLOBAL_LET, 20, 180);
        graph.add(timer);
        graph.add(state);
        graph.add(global);
        assertEquals(Arrays.asList(state, global, timer), graph.roots());
        String generated = BlueprintCompiler.generate(graph);
        assertTrue(generated.indexOf("STATE") < generated.indexOf("LET"));
        assertTrue(generated.indexOf("LET") < generated.indexOf("EVERY"));
        for (Pin pin : Pin.values()) {
            assertThrows(IllegalArgumentException.class, () -> graph.connect(timer.id, pin, state.id));
            assertThrows(IllegalArgumentException.class, () -> graph.connect(state.id, pin, timer.id));
        }
        assertTrue(
            graph.edges()
                .isEmpty());
    }

    @Test
    void loopsOwnBodyAndAfterWithoutAllowingCyclicWires() {
        BlueprintGraph graph = supported(
            "EVERY 20 TICKS DO FOR $i = 1 TO 3 DO PRINT($i) END "
                + "FOR $row IN [{value: 2}, {value: 3}] DO PRINT($row.value) END WHILE FALSE DO END PRINT(9) END");
        Node counted = first(graph, Type.FOR), array = first(graph, Type.FOREACH), loop = first(graph, Type.WHILE);
        assertEquals(Type.PRINT, graph.next(counted.id, Pin.BODY).type);
        assertEquals(array, graph.next(counted.id, Pin.AFTER));
        assertEquals(loop, graph.next(array.id, Pin.AFTER));
        assertNull(graph.next(loop.id, Pin.BODY));
        assertEquals(Type.PRINT, graph.next(loop.id, Pin.AFTER).type);
        assertThrows(IllegalArgumentException.class, () -> graph.connect(loop.id, Pin.BODY, counted.id));
        assertThrows(IllegalArgumentException.class, () -> graph.connect(counted.id, Pin.NEXT, loop.id));
        assertEquals(array, graph.next(counted.id, Pin.AFTER));
        BlueprintDocument reopened = BlueprintDocument.open(BlueprintDocument.save(graph));
        assertTrue(reopened.restoredLayout);
        assertNotNull(reopened.graph.node(counted.id));
        assertEquals(
            graph.edges()
                .size(),
            reopened.graph.edges()
                .size());
    }

    @Test
    void grammarOwnedBracesNormalizeWhileStructBracesAndValuesRemainIntact() {
        BlueprintGraph graph = supported(
            "NAME \"Loops 🏭\" LET $rows = [{value: 3}, {value: 4}]; STATE $sum = 0; "
                + "EVERY 20 TICKS { FOR $i = 2 TO 1 STEP -1 { FOR $row IN $rows { "
                + "LET $remaining = $row.value; WHILE ($remaining > 0) { "
                + "IF ($i == 1) { SET $sum = $sum + 1; } ELSE { SET $sum = $sum + 2; } "
                + "SET $remaining = $remaining - 1; } } } PRINT($sum); }");
        assertEquals(21, executeSum(BlueprintCompiler.generate(graph)));
        assertTrue(
            first(graph, Type.GLOBAL_LET).clause()
                .contains("{ value : 3 }"));
        assertFalse(
            first(graph, Type.FOR).clause()
                .contains("DO"));
        assertFalse(
            first(graph, Type.WHILE).clause()
                .contains("SET"));
    }

    @Test
    void syntaxOnlyClauseValidationAllowsReferencesAndCompleteBuildResolvesTheirScopes() {
        String[][] clauses = { { "IF", "($amount > 0)" }, { "PRINT", "PRINT($amount)" },
            { "TRANSFER", "TRANSFER($source, $destination, ITEM(\"*\"), $amount)" },
            { "INPUT", "INPUT ($amount) FROM $source" }, { "OUTPUT", "OUTPUT ($amount) TO $destination" },
            { "FOR", "FOR $i = $start TO $finish STEP $step" }, { "FOREACH", "FOR $row IN $rows" },
            { "WHILE", "WHILE ($amount > 0)" }, { "LET", "LET $local = $amount" },
            { "SET", "SET $amount = $amount - 1" }, { "GLOBAL_LET", "LET $shared = $other" },
            { "STATE", "STATE $count = 0" } };
        for (String[] clause : clauses)
            assertDoesNotThrow(() -> BlueprintCompiler.validateClause(Type.valueOf(clause[0]), clause[1]), clause[1]);
        supported(
            "LET $source = LABEL(\"source\") LET $destination = LABEL(\"dest\") STATE $sum = 0 "
                + "EVERY 20 TICKS DO LET $amount = LEN([1, 2]) FOR $i = 1 TO $amount DO "
                + "IF ($i > 0) THEN INPUT ($i) FROM $source OUTPUT ($amount) TO $destination "
                + "TRANSFER($source, $destination, ITEM(\"*\"), $i) PRINT($i) END "
                + "SET $sum = $sum + $i END END");
        BlueprintGraph invalid = new BlueprintGraph();
        Node timer = new Node(Type.TIMER, 20, 20), print = new Node(Type.PRINT, 20, 100);
        print.setClause("PRINT($missing)");
        invalid.add(timer);
        invalid.add(print);
        invalid.connect(timer.id, Pin.NEXT, print.id);
        assertThrows(IllegalArgumentException.class, () -> BlueprintCompiler.generate(invalid));
        assertFalse(
            BlueprintCompiler.inspectText("EVERY 20 TICKS DO FOR $i = 1 TO 2 DO END PRINT($i) END").report
                .isAvailable());
    }

    @Test
    void plainIdentifierReceiverQueriesStayScopedAndImportWithLoopsAndActions() {
        assertDoesNotThrow(() -> BlueprintCompiler.validateClause(Type.FOREACH, "FOR row IN source.contents()"));
        assertDoesNotThrow(() -> BlueprintCompiler.validateClause(Type.IF, "(source.empty())"));
        supported(
            "LET source = LABEL(\"src\") EVERY 20 TICKS DO FOR row IN source.contents() DO "
                + "IF (source.empty()) THEN PRINT(row) ELSE TRANSFER(source, LABEL(\"dest\")) END END END");
        BlueprintCompiler.ImportResult defined = BlueprintCompiler.inspectText(
            "FUNCTION tools.count() RETURNS INTEGER DO RETURN 1 END EVERY 20 TICKS DO PRINT(tools.count()) END");
        assertFalse(defined.report.isAvailable());
        assertTrue(
            defined.report.getBlockers()
                .stream()
                .anyMatch(blocker -> blocker.kind == Kind.FUNCTION_DEFINITION));
    }

    @Test
    void selfContainedFieldClausesRetainSemanticFeedbackWithoutRequiringScopeForReferences() {
        String[][] invalid = { { "TIMER", "EVERY 0 TICKS" }, { "IF", "(1)" },
            { "INPUT", "INPUT FROM \"source\" FRONT SIDE" }, { "TRANSFER", "TRANSFER(1, LABEL(\"dest\"))" },
            { "PRINT", "PRINT(9223372036854775808)" }, { "FOR", "FOR $i = TRUE TO 3" }, { "FOREACH", "FOR $i IN 3" },
            { "WHILE", "WHILE (1)" } };
        for (String[] clause : invalid) assertThrows(
            IllegalArgumentException.class,
            () -> BlueprintCompiler.validateClause(Type.valueOf(clause[0]), clause[1]),
            clause[1]);
        for (String clause : Arrays.asList("LET $local = 1", "LET local = 1"))
            assertDoesNotThrow(() -> BlueprintCompiler.validateClause(Type.LET, clause));
        assertDoesNotThrow(() -> BlueprintCompiler.validateClause(Type.FOR, "FOR $i = 1 TO 3"));
        assertDoesNotThrow(() -> BlueprintCompiler.validateClause(Type.SET, "SET $row.value = $other"));
        assertDoesNotThrow(() -> BlueprintCompiler.validateClause(Type.INPUT, "INPUT FROM source"));
        assertDoesNotThrow(() -> BlueprintCompiler.validateClause(Type.PRINT, "PRINT(amount)"));
    }

    @Test
    void unsupportedFeaturesAreStillFoundInsideNestedLoopBodies() {
        String source = "STATE $n = 0 FUNCTION twice(x: INTEGER) RETURNS INTEGER DO RETURN x * 2 END "
            + "EVERY 20 TICKS DO FOR $i = 1 TO 2 DO LET $x = twice($i) WHILE FALSE DO "
            + "WAIT 1 SETCIRCUIT(LABEL(\"source\"), 1) "
            + "TRANSFER(LABEL(\"source\"), LABEL(\"dest\")) INTO $moved "
            + "IF TRUE THEN ELSE IF FALSE THEN END END END END";
        BlueprintCompiler.ImportResult imported = BlueprintCompiler.inspectText(source);
        assertNull(imported.graph);
        List<Kind> kinds = imported.report.getBlockers()
            .stream()
            .map(blocker -> blocker.kind)
            .collect(Collectors.toList());
        assertEquals(
            Arrays.asList(
                Kind.FUNCTION_DEFINITION,
                Kind.FUNCTION_CALL,
                Kind.WAIT_STATEMENT,
                Kind.CIRCUIT_STATEMENT,
                Kind.TRANSFER_STATEMENT,
                Kind.ELSE_IF),
            kinds);
    }

    @Test
    void loopClauseLimitMeasuresHeaderAndNotTheCompleteBody() {
        StringBuilder source = new StringBuilder("EVERY 20 TICKS DO FOR $i = 1 TO 2 DO\n");
        for (int i = 0; i < 60; i++)
            source.append("PRINT(\"A long body remains a collection of individually bounded action nodes\")\n");
        source.append("END END");
        assertTrue(source.length() > BlueprintGraph.MAX_CLAUSE);
        supported(source.toString());
        assertThrows(
            IllegalArgumentException.class,
            () -> BlueprintCompiler.validateClause(Type.FOR, "FOR $i = 1 TO 3 DO PRINT(1) END"));
        assertThrows(
            IllegalArgumentException.class,
            () -> BlueprintCompiler.validateClause(Type.FOREACH, "FOR $i = 1 TO 3"));
        assertThrows(
            IllegalArgumentException.class,
            () -> BlueprintCompiler.validateClause(Type.GLOBAL_LET, "LET $a = 1 STATE $b = 2"));
        assertThrows(
            IllegalArgumentException.class,
            () -> BlueprintCompiler.validateClause(Type.LET, "LET $a = 1 -- comment"));
    }

    private static long executeSum(String source) {
        ProgramBuildResult built = new ProgramBuilder(source).build();
        assertTrue(built.isSuccess(), built.getErrorMessage());
        Program program = built.getProgram();
        DataValue[] state = program.getStateSchema()
            .getDeclarations()
            .stream()
            .map(declaration -> declaration.getDefaultValue())
            .toArray(DataValue[]::new);
        ProgramContext context = new ProgramContext(null, null, null, null, 20, false);
        context.setStateAccess(new StateAccess() {

            @Override
            public DataValue read(int slot) {
                return state[slot];
            }

            @Override
            public void write(int slot, DataValue value) {
                state[slot] = value;
            }
        });
        program.tick(context);
        return state[0].asLong();
    }
}
