package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.client.blueprint.BlueprintDocument;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.BlueprintLayoutMatcher;
import ca.teamdman.sfm.client.blueprint.StudioDocument;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfm.client.blueprint.StudioSourceMap;

class VisualDataIntegrationTest {

    private static final UUID PROJECT = new UUID(1037, 1);

    @Test
    void structuredFieldsReplaceOnlyTheirOwnedHeaderSpans() {
        assertEquals(
            "let amount = (limit + 1)",
            StudioNodeProperties.update(Type.LET, "let amount = 0", "value", "(limit + 1)"));
        assertEquals("state phase = 2", StudioNodeProperties.update(Type.STATE, "state phase = 0", "value", "2"));
        assertEquals(
            "data.rows[index] = 3",
            StudioNodeProperties.update(Type.SET, "data.rows[index] = 1", "value", "3"));
        assertEquals(
            "for index = start to finish step 2",
            StudioNodeProperties.update(Type.FOR, "for index = start to finish step 1", "step", "2"));
        assertEquals(
            "for row in source.contents()",
            StudioNodeProperties.update(Type.FOREACH, "for row in values", "values", "source.contents()"));
        assertEquals(
            "while (remaining > 0)",
            StudioNodeProperties.update(Type.WHILE, "while false", "condition", "(remaining > 0)"));
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties.update(Type.LET, "let amount = 0", "name", "amount = 1 print(1)"));
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties.update(Type.FOR, "for index = 1 to 2", "end", "2 DO PRINT(1) END"));
    }

    @Test
    void navigationMapsDeclarationsAndNestedLoopHeadersWithoutClaimingBodyOrEnd() {
        String source = "let title = \"🏭\"\nstate sum = 0\nevery 20 ticks {\n"
            + "for index = 1 to 2 {\nlet values = [index]\nfor value in values {\n"
            + "while false { print(title) }\nsum = sum + value\n}\n}\nprint(sum)\n}";
        BlueprintGraph graph = BlueprintCompiler.importText(source);
        StudioSourceMap map = StudioSourceMap.build(PROJECT, source, source, graph);
        assertEquals(Type.GLOBAL_LET, graph.node(map.atCaret(source.indexOf("title")).node).type);
        assertEquals(Type.STATE, graph.node(map.atCaret(source.indexOf("state")).node).type);
        assertEquals(Type.FOR, graph.node(map.atCaret(source.indexOf("for index")).node).type);
        assertEquals(Type.FOREACH, graph.node(map.atCaret(source.indexOf("for value")).node).type);
        assertEquals(Type.WHILE, graph.node(map.atCaret(source.indexOf("while")).node).type);
        assertEquals(Type.SET, graph.node(map.atCaret(source.indexOf("sum = sum")).node).type);
        assertNull(map.atCaret(source.indexOf("}\n}\nprint")));
        assertTrue(map.matches(PROJECT, source, source, graph));
    }

    @Test
    void visualEditsAndUndoRetainLoopBodyLayoutAndVariableScope() {
        String source = "let count = 2 every 20 ticks { for index = 1 to count { print(index) } print(count) }";
        StudioDocument document = new StudioDocument();
        document.load(source);
        document.openVisual();
        Node loop = document.graph()
            .nodes()
            .stream()
            .filter(node -> node.type == Type.FOR)
            .findFirst()
            .get();
        document.editGraph(
            graph -> graph.node(loop.id)
                .setPosition(333, 222));
        String before = document.source();
        document.editGraph(
            graph -> graph.node(loop.id)
                .setClause("for index = 1 to (count + 1)"));
        assertFalse(document.incomplete(), document.problem());
        BlueprintDocument reopened = BlueprintDocument.open(document.source());
        assertEquals(
            333,
            reopened.graph.node(loop.id)
                .x());
        assertTrue(document.undo());
        assertEquals(before, document.source());
        assertTrue(document.redo());
        String safe = document.code();
        document.editGraph(
            graph -> graph.node(loop.id)
                .setClause("for index = 1 to missing"));
        assertTrue(document.incomplete());
        assertEquals(safe, document.code(), "Invalid scoped edits retain the last complete source");
        assertTrue(document.undo());
        assertFalse(document.incomplete());
    }

    @Test
    void exactVariableNamesPreventSwappedAssignmentIdentities() {
        String prefix = "state $left = 0 state $right = 0 every 20 ticks { ";
        BlueprintGraph before = BlueprintCompiler.importText(prefix + "$left = 1 $right = 2 }");
        BlueprintGraph after = BlueprintCompiler.importText(prefix + "$right = 2 $left = 1 }");
        BlueprintGraph matched = BlueprintLayoutMatcher.match(before, after).graph;
        for (Node node : before.nodes()) if (node.type == Type.SET) {
            assertEquals(
                node.clause(),
                matched.node(node.id)
                    .clause());
        }
    }
}
