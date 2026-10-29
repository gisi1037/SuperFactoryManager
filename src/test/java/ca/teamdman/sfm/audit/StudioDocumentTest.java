package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.client.blueprint.BlueprintDocument;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Pin;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioDocument;
import ca.teamdman.sfm.common.program.ProgramDocument;

/** Canonical code, optional visual layout and incomplete heap drafts have distinct owners. */
class StudioDocumentTest {

    private static final String CODE = "-- Keep this exact comment.\nname \"Iron\"\n"
        + "every 20 ticks do\n  input from Source -- keep casing too\n  output to Destination\nend\n";

    @Test
    void unsupportedValidAndInvalidCodeCannotBeReplacedWithAPartialVisualGraph() {
        for (String source : new String[] {
            "let $counter = 0\nevery 20 ticks do $counter = $counter + 1 print($counter) end",
            "every 20 ticks do input from" }) {
            StudioDocument document = new StudioDocument();
            document.load(source);
            assertThrows(IllegalArgumentException.class, document::openVisual);
            assertNull(document.graph());
            assertEquals(source, document.code());
            assertEquals(source, document.source());
            assertFalse(document.incomplete(), "A refused import leaves the text document available for editing");
        }
    }

    @Test
    void layoutOnlyChangesPreserveExactBodyCommentsAndHiddenMetadataAcrossRepeatedSaves() {
        StudioDocument document = opened(CODE);
        UUID input = find(document.graph(), Type.INPUT).id;
        document.editGraph(
            graph -> graph.node(input)
                .setPosition(321, 123));
        String saved = document.source();
        assertEquals(CODE, document.code());
        assertTrue(saved.startsWith(CODE + BlueprintDocument.MARKER));
        assertFalse(
            document.code()
                .contains(BlueprintDocument.MARKER));
        document.editGraph(
            graph -> graph.node(input)
                .setPosition(322, 124));
        assertEquals(CODE, document.code());
        assertEquals(1, occurrences(document.source(), BlueprintDocument.MARKER));
        assertEquals(1, occurrences(document.code(), "Keep this exact comment."));

        StudioDocument reloaded = opened(document.source());
        assertEquals(CODE, reloaded.code());
        assertEquals(
            322,
            reloaded.graph()
                .node(input)
                .x());
        assertEquals(
            124,
            reloaded.graph()
                .node(input)
                .y());
    }

    @Test
    void structuralChangesUndoAndRedoBothCodeAndNodeProperties() {
        StudioDocument document = opened(CODE);
        UUID output = find(document.graph(), Type.OUTPUT).id;
        document.editGraph(
            graph -> graph.node(output)
                .setClause("OUTPUT 8 TO NewDestination"));
        String changed = document.source();
        assertTrue(
            document.code()
                .contains("OUTPUT 8 TO NewDestination"));
        assertFalse(document.incomplete());
        assertEquals(
            BlueprintCompiler.generate(document.graph()),
            BlueprintCompiler.generate(BlueprintDocument.open(document.source()).graph));
        assertTrue(document.undo());
        assertEquals(CODE, document.code());
        assertEquals(CODE, document.source());
        assertEquals(
            "output to Destination",
            document.graph()
                .node(output)
                .clause());
        assertTrue(document.redo());
        assertEquals(changed, document.source());
        assertEquals(
            "OUTPUT 8 TO NewDestination",
            document.graph()
                .node(output)
                .clause());
    }

    @Test
    void disconnectedGraphBlocksSaveWithoutLosingTheLastSafeSourceAndHasUndoRedo() {
        StudioDocument document = opened(CODE);
        UUID input = find(document.graph(), Type.INPUT).id;
        String safe = document.source();
        document.editGraph(graph -> graph.disconnectInput(input));
        assertTrue(document.incomplete());
        assertFalse(
            document.problem()
                .isEmpty());
        assertEquals(safe, document.source());
        assertEquals(CODE, document.code());
        assertThrows(IllegalStateException.class, () -> document.editCode(""));
        assertTrue(document.undo());
        assertFalse(document.incomplete());
        assertEquals(
            2,
            document.graph()
                .edges()
                .size());
        assertTrue(document.redo());
        assertTrue(document.incomplete());
        assertEquals(
            1,
            document.graph()
                .edges()
                .size());
        UUID timer = find(document.graph(), Type.TIMER).id;
        document.editGraph(graph -> graph.connect(timer, Pin.NEXT, input));
        assertFalse(document.incomplete());
        assertEquals(CODE, document.code());
    }

    @Test
    void invalidClauseRemainsRecoverableAndFailedMutationsRestorePriorModel() {
        StudioDocument document = opened(CODE);
        UUID output = find(document.graph(), Type.OUTPUT).id;
        document.editGraph(
            graph -> graph.node(output)
                .setClause("OUTPUT TO"));
        assertTrue(document.incomplete());
        assertEquals(CODE, document.code());
        assertTrue(document.undo());
        assertFalse(document.incomplete());
        String before = document.source();
        double x = document.graph()
            .node(output)
            .x();
        assertThrows(
            IllegalArgumentException.class,
            () -> document.editGraph(
                graph -> graph.node(output)
                    .setPosition(Double.NaN, 0)));
        assertEquals(before, document.source());
        assertEquals(
            x,
            document.graph()
                .node(output)
                .x());
        assertFalse(document.incomplete());
    }

    @Test
    void heapRecoveryRetainsIncompleteNodesAndNeverSharesMutableGraphObjects() {
        StudioDocument original = opened(CODE);
        Node loose = new Node(Type.OUTPUT, 456, 789);
        original.editGraph(graph -> graph.add(loose));
        StudioDocument.Snapshot recovery = original.snapshot();
        assertTrue(original.incomplete());
        loose.setClause("OUTPUT 99 TO changed_after_snapshot");

        StudioDocument restored = new StudioDocument();
        restored.restore(recovery);
        assertTrue(restored.incomplete());
        assertEquals(CODE, restored.code());
        assertEquals(CODE, restored.source());
        assertEquals(
            Type.OUTPUT.defaultClause,
            restored.graph()
                .node(loose.id)
                .clause());
        restored.graph()
            .node(loose.id)
            .setPosition(1, 2);

        StudioDocument restoredAgain = new StudioDocument();
        restoredAgain.restore(recovery);
        assertEquals(
            456,
            restoredAgain.graph()
                .node(loose.id)
                .x());
        assertEquals(
            789,
            restoredAgain.graph()
                .node(loose.id)
                .y());
        assertNotSame(restored.graph(), restoredAgain.graph());
    }

    @Test
    void codeEditsInvalidateStaleGraphAndOnlyUseCurrentCodeWhenVisualReopens() {
        StudioDocument document = opened(CODE);
        UUID output = find(document.graph(), Type.OUTPUT).id;
        document.editGraph(
            graph -> graph.node(output)
                .setPosition(400, 500));
        String next = "every 7 ticks do input from other output 1 to latest end";
        document.editCode(next);
        assertEquals(next, document.code());
        assertNull(document.graph());
        assertFalse(document.undo(), "Code buffer owns its own undo; visual history cannot restore stale code");
        assertFalse(document.redo());
        document.openVisual();
        assertEquals("every 7 ticks", find(document.graph(), Type.TIMER).clause());
        assertEquals("output 1 to latest", find(document.graph(), Type.OUTPUT).clause());
        assertFalse(
            document.code()
                .contains(BlueprintDocument.MARKER));
        assertFalse(
            document.source()
                .startsWith(CODE));
    }

    @Test
    void nearLimitCodeTakesPrecedenceOverOptionalLayout() {
        StudioDocument document = opened(CODE);
        UUID output = find(document.graph(), Type.OUTPUT).id;
        document.editGraph(
            graph -> graph.node(output)
                .setPosition(10, 20));
        String base = "\nevery 20 ticks do input from input end";
        StringBuilder large = new StringBuilder("--");
        while (large.length() + base.length() < ProgramDocument.MAX_SOURCE_BYTES) large.append('x');
        large.append(base);
        String code = large.toString();
        assertEquals(ProgramDocument.MAX_SOURCE_BYTES, code.getBytes(StandardCharsets.UTF_8).length);
        ProgramDocument.validateSource(code);
        document.editCode(code);
        assertEquals(code, document.code());
        assertEquals(code, document.source(), "Optional layout must be dropped instead of truncating source");
        assertFalse(
            document.source()
                .contains(BlueprintDocument.MARKER));
    }

    @Test
    void malformedSidecarCannotReplaceCurrentSourceOrLeakIntoCodeView() {
        String source = CODE + BlueprintDocument.MARKER + "broken_not_gzip";
        StudioDocument document = opened(source);
        assertEquals(CODE, document.code());
        assertEquals(source, document.source(), "Loading does not silently rewrite the saved document");
        UUID output = find(document.graph(), Type.OUTPUT).id;
        document.editGraph(
            graph -> graph.node(output)
                .setPosition(12, 34));
        assertEquals(CODE, document.code());
        assertFalse(
            document.source()
                .contains("broken_not_gzip"));
        BlueprintDocument saved = BlueprintDocument.open(document.source());
        assertTrue(saved.restoredLayout);
        assertEquals(
            12,
            saved.graph.node(output)
                .x());
    }

    @Test
    void dragCheckpointGroupsManyPixelsIntoOneUndoAndNewEditClearsRedo() {
        StudioDocument document = opened(CODE);
        UUID output = find(document.graph(), Type.OUTPUT).id;
        double before = document.graph()
            .node(output)
            .x();
        document.checkpoint();
        for (int x = 1; x <= 20; x++) document.graph()
            .node(output)
            .setPosition(x, 30);
        document.graphChanged();
        assertTrue(document.undo());
        assertEquals(
            before,
            document.graph()
                .node(output)
                .x());
        assertTrue(document.redo());
        assertEquals(
            20,
            document.graph()
                .node(output)
                .x());
        assertTrue(document.undo());
        document.editGraph(
            graph -> graph.node(output)
                .setPosition(99, 30));
        assertFalse(document.redo());
    }

    @Test
    void loadingAnotherProgramResetsGraphAndHistory() {
        StudioDocument document = opened(CODE);
        UUID output = find(document.graph(), Type.OUTPUT).id;
        document.editGraph(
            graph -> graph.node(output)
                .setPosition(99, 30));
        String next = "every 1 ticks do end";
        document.load(next);
        assertNull(document.graph());
        assertEquals(next, document.code());
        assertEquals(next, document.source());
        assertFalse(document.incomplete());
        assertFalse(document.undo());
        assertFalse(document.redo());
    }

    @Test
    void restoringAnotherProjectStartsFreshHistoryInsteadOfUndoingAcrossPrograms() {
        StudioDocument previous = opened(CODE);
        UUID output = find(previous.graph(), Type.OUTPUT).id;
        previous.editGraph(
            graph -> graph.node(output)
                .setPosition(99, 30));
        assertTrue(previous.undo());
        StudioDocument next = opened("every 7 ticks do input from another end");
        StudioDocument.Snapshot nextProject = next.snapshot();
        previous.restore(nextProject);
        assertEquals(next.code(), previous.code());
        assertFalse(previous.undo(), "Project B cannot inherit project A's undo history");
        assertFalse(previous.redo(), "Project B cannot inherit project A's redo history");
        assertEquals("every 7 ticks", find(previous.graph(), Type.TIMER).clause());
    }

    @Test
    void oversizedUtf8DraftSurvivesLoadAndHeapRecoveryWhileSaveAndVisualImportRefuseIt() {
        StudioDocument withLayout = opened(CODE);
        UUID output = find(withLayout.graph(), Type.OUTPUT).id;
        withLayout.editGraph(
            graph -> graph.node(output)
                .setPosition(15, 25));
        String metadata = withLayout.source()
            .substring(
                withLayout.source()
                    .indexOf(BlueprintDocument.MARKER));
        StringBuilder body = new StringBuilder("--");
        for (int i = 0; i < 10000; i++) body.append('\u3042');
        body.append('\n')
            .append(CODE);
        String draftCode = body.toString();
        String source = draftCode + metadata;
        assertTrue(
            draftCode.length() < ProgramDocument.MAX_SOURCE_BYTES,
            "Exercise the UTF-8 byte limit, not only Java character count");
        assertThrows(IllegalArgumentException.class, () -> ProgramDocument.validateSource(source));
        StudioDocument document = new StudioDocument();
        assertDoesNotThrow(() -> document.load(source));
        assertEquals(source, document.source());
        assertEquals(draftCode, document.code());
        assertThrows(IllegalArgumentException.class, document::openVisual);
        assertNull(document.graph());
        assertEquals(source, document.source());

        StudioDocument restored = new StudioDocument();
        restored.restore(document.snapshot());
        assertEquals(source, restored.source());
        assertEquals(draftCode, restored.code());
        assertFalse(
            restored.code()
                .contains(BlueprintDocument.MARKER));
        restored.editCode(CODE);
        assertEquals(CODE, restored.code());
        assertDoesNotThrow(() -> ProgramDocument.validateSource(restored.source()));
        assertDoesNotThrow(restored::openVisual);
    }

    @Test
    void pastedSerializedProgramUsesItsOwnHiddenSidecarInsteadOfDuplicatingTheOldOne() {
        StudioDocument document = opened(CODE);
        UUID firstOutput = find(document.graph(), Type.OUTPUT).id;
        document.editGraph(
            graph -> graph.node(firstOutput)
                .setPosition(15, 25));
        String nextCode = "-- another project\nevery 7 ticks do input from newest output to final end\n";
        StudioDocument pasted = opened(nextCode);
        UUID nextOutput = find(pasted.graph(), Type.OUTPUT).id;
        pasted.editGraph(
            graph -> graph.node(nextOutput)
                .setPosition(333, 444));
        String serialized = pasted.source();

        document.editCode(serialized);
        assertEquals(nextCode, document.code());
        assertEquals(serialized, document.source());
        assertEquals(1, occurrences(document.source(), BlueprintDocument.MARKER));
        assertFalse(
            document.code()
                .contains(BlueprintDocument.MARKER));
        assertNull(document.graph());
        document.openVisual();
        assertEquals(
            333,
            document.graph()
                .node(nextOutput)
                .x());
        assertEquals(
            444,
            document.graph()
                .node(nextOutput)
                .y());

        String quotedMarker = "name \"-- sfm:blueprint:v2 marker inside a name\"\nevery 3 ticks do end";
        document.editCode(quotedMarker);
        assertEquals(quotedMarker, document.code(), "Marker text in a string is executable source, not metadata");
        assertTrue(
            document.source()
                .startsWith(quotedMarker));
        document.openVisual();
        assertEquals(
            "name \"-- sfm:blueprint:v2 marker inside a name\"",
            document.graph()
                .nameClause());
    }

    private static StudioDocument opened(String source) {
        StudioDocument document = new StudioDocument();
        document.load(source);
        document.openVisual();
        return document;
    }

    private static Node find(BlueprintGraph graph, Type type) {
        return graph.nodes()
            .stream()
            .filter(node -> node.type == type)
            .findFirst()
            .get();
    }

    private static int occurrences(String source, String value) {
        int count = 0;
        for (int offset = 0; (offset = source.indexOf(value, offset)) >= 0; offset += value.length()) count++;
        return count;
    }
}
