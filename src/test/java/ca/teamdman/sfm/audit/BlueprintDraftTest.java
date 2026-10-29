package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.gui.mui2.Mui2BlueprintEditor.Draft;

class BlueprintDraftTest {

    @Test
    void graphAndUnappliedPropertySurviveWithoutSharingMutableEditorObjects() {
        BlueprintGraph graph = BlueprintCompiler.importText("EVERY 20 TICKS DO INPUT FROM source END");
        Node node = graph.nodes()
            .stream()
            .filter(value -> value.type == Type.INPUT)
            .findFirst()
            .get();
        String pending = "INPUT FROM unfinished -- unapplied";
        Draft draft = new Draft(graph, node.id, pending, false, true, UUID.randomUUID(), "canonical", "preview");
        node.setClause("INPUT FROM changed");
        BlueprintGraph restored = draft.graphCopy();
        assertEquals(
            "INPUT FROM source",
            restored.node(node.id)
                .clause());
        restored.node(node.id)
            .setClause("INPUT FROM another_change");
        assertEquals(
            "INPUT FROM source",
            draft.graphCopy()
                .node(node.id)
                .clause());
        assertEquals(pending, draft.property);
        assertTrue(draft.dirty);
        assertTrue(
            draft.recoveryText()
                .contains(pending));
        assertTrue(
            draft.recoveryText()
                .contains("INCOMPLETE VISUAL DRAFT"));
    }

    @Test
    void unwiredGraphsStillExportEveryNodeAndCannotMatchAnotherCanonicalDocument() {
        BlueprintGraph graph = new BlueprintGraph();
        Node timer = new Node(Type.TIMER, 4, 8);
        Node disconnected = new Node(Type.OUTPUT, 16, 32);
        graph.add(timer);
        graph.add(disconnected);
        UUID document = UUID.randomUUID();
        Draft draft = new Draft(
            graph,
            disconnected.id,
            disconnected.clause(),
            false,
            true,
            document,
            "original",
            "invalid preview");
        assertTrue(draft.matches(document, "original"));
        assertFalse(draft.matches(document, "changed"));
        assertFalse(draft.matches(UUID.randomUUID(), "original"));
        assertTrue(
            draft.recoveryText()
                .contains(timer.id.toString()));
        assertTrue(
            draft.recoveryText()
                .contains(disconnected.id.toString()));
        assertEquals(
            2,
            draft.graphCopy()
                .nodes()
                .size());
    }

    @Test
    void validatedGraphsCopyAsSfmlAndUnsupportedSourcesRemainAvailableAsText() {
        String source = "EVERY 20 TICKS DO INPUT FROM source END";
        BlueprintGraph graph = BlueprintCompiler.importText(source);
        Draft draft = new Draft(graph, null, "", false, true, UUID.randomUUID(), source, "preview");
        assertEquals(BlueprintCompiler.generate(graph), draft.recoveryText());
        Draft unsupported = new Draft(null, null, "", false, false, UUID.randomUUID(), "raw source", "raw source");
        assertNull(unsupported.graphCopy());
        assertEquals("raw source", unsupported.recoveryText());
    }
}
