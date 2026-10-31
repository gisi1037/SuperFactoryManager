package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Pin;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioDocument;
import ca.teamdman.sfm.client.blueprint.StudioDraftDiagnostics;
import ca.teamdman.sfm.client.blueprint.StudioDraftDiagnostics.Result;
import ca.teamdman.sfm.client.blueprint.StudioFieldDraft;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfm.client.gui.mui2.SfmlEditorBuffer;

class StudioDraftDiagnosticsTest {

    private static StudioDocument document(String source) {
        StudioDocument document = new StudioDocument();
        document.load(source);
        return document;
    }

    private static Result due(StudioDraftDiagnostics diagnostics, StudioDocument document, boolean pending) {
        assertTrue(diagnostics.tick(), "One explicit pending-state transition schedules one check");
        Result result = diagnostics.check(document, pending);
        assertFalse(diagnostics.tick());
        return result;
    }

    @Test
    void fieldsCancelRefreshesValidDiagnosticsWithoutApplyingPendingValuesOrWritingHistory() {
        String source = "-- 🏭\r\nEVERY 20 TICKS DO INPUT FROM source END";
        StudioDocument document = document(source);
        document.openVisual();
        Node node = document.graph()
            .next(
                document.graph()
                    .triggers()
                    .get(0).id,
                Pin.NEXT);
        StudioFieldDraft fields = new StudioFieldDraft(
            UUID.randomUUID(),
            node,
            document.code(),
            StudioNodeProperties.inspect(node.type, node.clause()));
        StudioDraftDiagnostics diagnostics = new StudioDraftDiagnostics();
        assertTrue(diagnostics.check(document, false).compiled.isSuccess());
        fields.set("label", "other");
        diagnostics.pendingChanged(fields.dirty());
        Result pending = due(diagnostics, document, true);
        assertTrue(pending.message.startsWith("Inspector has unapplied changes"));
        assertNull(pending.compiled);
        assertEquals("other", fields.value("label"));
        assertEquals(source, document.source());
        diagnostics.pendingChanged(false);
        Result canceled = due(diagnostics, document, false);
        assertTrue(canceled.compiled.isSuccess());
        assertTrue(canceled.message.startsWith("Draft: valid"));
        assertEquals(source, document.source());
        assertEquals("INPUT FROM source", node.clause());
        assertFalse(document.undo());
        assertFalse(document.redo());
    }

    @Test
    void fieldsApplyRechecksActualCodeAndKeepsTheSingleGraphUndoEntry() {
        StudioDocument document = document("EVERY 20 TICKS DO INPUT FROM source END");
        document.openVisual();
        String before = document.source();
        Node node = document.graph()
            .next(
                document.graph()
                    .triggers()
                    .get(0).id,
                Pin.NEXT);
        UUID project = UUID.randomUUID();
        StudioFieldDraft fields = new StudioFieldDraft(
            project,
            node,
            document.code(),
            StudioNodeProperties.inspect(node.type, node.clause()));
        StudioDraftDiagnostics diagnostics = new StudioDraftDiagnostics();
        fields.set("label", "other");
        diagnostics.pendingChanged(true);
        due(diagnostics, document, true);
        String changed = fields.appliedClause(project, node, document.code());
        document.editGraph(
            graph -> graph.node(node.id)
                .setClause(changed));
        diagnostics.schedule(6);
        diagnostics.pendingChanged(false);
        assertTrue(due(diagnostics, document, false).compiled.isSuccess());
        assertEquals(
            changed,
            document.graph()
                .node(node.id)
                .clause());
        assertTrue(document.undo());
        assertEquals(before, document.source());
        assertFalse(document.undo());
        assertTrue(document.redo());
        assertEquals(
            changed,
            document.graph()
                .node(node.id)
                .clause());
    }

    @Test
    void rawResetAndManualRevertClearPendingMessageButPreserveGenuineCompilerErrors() {
        StudioDocument document = document("EVERY 20 TICKS DO PRINT( END");
        String source = document.source();
        StudioDraftDiagnostics diagnostics = new StudioDraftDiagnostics();
        Result original = diagnostics.check(document, false);
        assertFalse(original.compiled.isSuccess());
        diagnostics.pendingChanged(true);
        assertNull(due(diagnostics, document, true).compiled);
        diagnostics.pendingChanged(false);
        Result reset = due(diagnostics, document, false);
        assertFalse(reset.compiled.isSuccess());
        assertEquals(original.message, reset.message);
        assertEquals(source, document.source());
        assertNull(document.graph());
        assertFalse(document.undo());
    }

    @Test
    void pendingOrCancelNeverMasksAnIncompleteGraphAsValid() {
        StudioDocument document = document("EVERY 20 TICKS DO PRINT(1) END");
        document.openVisual();
        String before = document.source();
        document.editGraph(graph -> graph.add(new Node(Type.PRINT, 1, 2)));
        assertTrue(document.incomplete());
        StudioDraftDiagnostics diagnostics = new StudioDraftDiagnostics();
        diagnostics.pendingChanged(true);
        Result pending = due(diagnostics, document, true);
        assertTrue(pending.message.startsWith("Visual draft: "));
        assertNull(pending.compiled);
        diagnostics.pendingChanged(false);
        Result canceled = due(diagnostics, document, false);
        assertEquals(pending.message, canceled.message);
        assertNull(canceled.compiled);
        assertEquals(before, document.source());
        assertTrue(document.incomplete());
        assertTrue(document.undo());
        diagnostics.schedule(6);
        for (int i = 0; i < 6; i++) assertFalse(diagnostics.tick());
        assertTrue(due(diagnostics, document, false).compiled.isSuccess());
    }

    @Test
    void repeatedEventsCoalesceAndUnchangedSelectionOrIdleTicksNeverScheduleParsing() {
        StudioDocument document = document("EVERY 20 TICKS DO PRINT(1) END");
        StudioDraftDiagnostics diagnostics = new StudioDraftDiagnostics();
        diagnostics.check(document, false);
        for (int i = 0; i < 1000; i++) {
            diagnostics.pendingChanged(false);
            assertFalse(diagnostics.tick());
        }
        diagnostics.pendingChanged(true);
        diagnostics.pendingChanged(true);
        diagnostics.pendingChanged(true);
        assertNull(due(diagnostics, document, true).compiled);
        for (int i = 0; i < 1000; i++) {
            diagnostics.pendingChanged(true);
            assertFalse(diagnostics.tick());
        }
        diagnostics.pendingChanged(false);
        diagnostics.pendingChanged(false);
        assertTrue(due(diagnostics, document, false).compiled.isSuccess());
        diagnostics.schedule(6);
        for (int i = 0; i < 4; i++) assertFalse(diagnostics.tick());
        diagnostics.schedule(6);
        for (int i = 0; i < 6; i++) assertFalse(diagnostics.tick());
        assertTrue(due(diagnostics, document, false).compiled.isSuccess());
    }

    @Test
    void sourceRecheckDoesNotReloadEditorOrDiscardItsRedoHistory() {
        String source = "EVERY 20 TICKS DO PRINT(\"😀\") END";
        StudioDocument document = document(source);
        SfmlEditorBuffer buffer = new SfmlEditorBuffer();
        buffer.load(source);
        buffer.move(source.length(), false);
        buffer.insert(" -- marker");
        assertTrue(buffer.undo());
        StudioDraftDiagnostics diagnostics = new StudioDraftDiagnostics();
        diagnostics.check(document, false);
        diagnostics.pendingChanged(true);
        due(diagnostics, document, true);
        diagnostics.pendingChanged(false);
        assertTrue(due(diagnostics, document, false).compiled.isSuccess());
        assertEquals(source, document.source());
        assertEquals(source, buffer.source());
        assertTrue(buffer.redo());
        assertEquals(source + " -- marker", buffer.source());
        assertTrue(buffer.undo());
    }
}
