package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.client.blueprint.BlueprintDocument;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Pin;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioDocument;
import ca.teamdman.sfm.client.blueprint.StudioFieldDraft;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfm.client.blueprint.StudioSourceMap;
import ca.teamdman.sfm.client.blueprint.StudioSourceMap.Span;
import ca.teamdman.sfm.client.gui.mui2.SfmlCodeEditorWidget;

class StudioSourceMapTest {

    @Test
    void viewRefreshKeepsCrLfSourceGraphHistoryAndUncommittedCodeHistory() {
        String source = "-- 🏭\r\nevery 20 ticks do\r\n print(1)\r\nend\r\n";
        StudioDocument document = new StudioDocument();
        document.load(source);
        document.openVisual();
        Node root = document.graph()
            .triggers()
            .get(0);
        document.editGraph(
            graph -> graph.node(root.id)
                .setPosition(500, 500));
        assertTrue(document.undo());
        String before = document.source();
        int[] changes = { 0 };
        SfmlCodeEditorWidget[] holder = new SfmlCodeEditorWidget[1];
        SfmlCodeEditorWidget editor = new SfmlCodeEditorWidget(() -> {
            changes[0]++;
            document.editCode(holder[0].source());
        }, () -> {}, () -> {}, () -> {}, () -> true, error -> fail(error));
        holder[0] = editor;
        editor.load(document.code());
        assertEquals(0, changes[0], "Loading normalized display text must not publish an edit");
        editor.buffer.move(
            editor.source()
                .length(),
            false);
        editor.buffer.insert("-- undo marker");
        assertTrue(editor.buffer.undo());
        editor.toggleWrap();
        assertTrue(editor.buffer.find("print", false, true));
        editor.refreshAfterCommand();
        editor.diagnostic("Line 3, Col 1: test diagnostic");
        editor.toggleWrap();
        assertEquals(0, changes[0], "Wrap, Find and diagnostics are view operations");
        assertEquals(before, document.source());
        assertTrue(editor.buffer.redo(), "View refresh must preserve code redo history");
        assertTrue(editor.buffer.undo());
        assertTrue(document.redo(), "View refresh must preserve graph redo history");
        assertTrue(document.undo());
        assertEquals(before, document.source());
    }

    @Test
    void changedNotificationsFollowTextEditsAndDoNotResetBlockedDraftHistoryOnViewChanges() {
        int[] changes = { 0 };
        boolean[] blocked = { false };
        SfmlCodeEditorWidget[] holder = new SfmlCodeEditorWidget[1];
        SfmlCodeEditorWidget editor = new SfmlCodeEditorWidget(() -> {
            changes[0]++;
            if (blocked[0]) holder[0].load("original");
        }, () -> {}, () -> {}, () -> {}, () -> true, error -> fail(error));
        holder[0] = editor;
        editor.load("original");
        assertEquals(0, changes[0]);
        editor.buffer.move(
            editor.source()
                .length(),
            false);
        assertTrue(editor.insert(" edit"));
        assertEquals(1, changes[0]);
        editor.undo();
        assertEquals(2, changes[0]);
        blocked[0] = true;
        editor.toggleWrap();
        editor.refreshAfterCommand();
        assertEquals(2, changes[0]);
        assertTrue(editor.buffer.redo(), "A view change must not enter the pending-fields rejection/reload path");
        assertTrue(editor.buffer.undo());
        blocked[0] = false;
        editor.redo();
        assertEquals(3, changes[0]);
        assertEquals("original edit", editor.source());
        editor.refreshAfterCommand();
        assertEquals(3, changes[0], "A no-op command does not publish another edit");
    }

    @Test
    void reorderingIdenticalTriggersWithSameNodeInstancesAndEdgesInvalidatesOldSpanOwnership() {
        String source = program("PRINT(1)") + " " + program("PRINT(1)");
        BlueprintGraph graph = BlueprintCompiler.importText(source);
        StudioSourceMap before = map(source, graph);
        Node first = graph.triggers()
            .get(0), child = graph.next(first.id, Pin.NEXT),
            second = graph.triggers()
                .get(1);
        graph.remove(first.id);
        graph.add(first);
        graph.connect(first.id, Pin.NEXT, child.id);
        assertEquals(
            second.id,
            graph.triggers()
                .get(0).id);
        assertFalse(before.matches(PROJECT, source, source, graph));
        StudioSourceMap reordered = map(source, graph);
        assertEquals(second.id, reordered.atCaret(source.indexOf("EVERY")).node);
        assertEquals(first.id, reordered.atCaret(source.lastIndexOf("EVERY")).node);
    }

    private static final UUID PROJECT = new UUID(11, 12);

    private static StudioSourceMap map(String source, BlueprintGraph graph) {
        return StudioSourceMap.build(PROJECT, source, StudioSourceMap.normalize(source), graph);
    }

    private static String program(String body) {
        return "EVERY 20 TICKS DO " + body + " END";
    }

    private static int nth(String source, String value, int occurrence) {
        int start = -1;
        for (int i = 0; i <= occurrence; i++) start = source.indexOf(value, start + 1);
        assertTrue(start >= 0);
        return start;
    }

    @Test
    void duplicateStatementsFollowTrueFalseAfterRolesAndMultipleTriggersRatherThanTextSearch() {
        String source = "NAME \"Duplicates\"\nevery 20 ticks do print(\"same\") "
            + "if \"source\" HAS > 0 then print(\"same\") if true then print(\"same\") else print(\"same\") end "
            + "print(\"same\") else print(\"same\") end print(\"same\") end "
            + "EVERY REDSTONE PULSE DO print(\"same\") END";
        BlueprintGraph graph = BlueprintCompiler.importText(source);
        StudioSourceMap map = map(source, graph);
        Node first = graph.next(
            graph.triggers()
                .get(0).id,
            Pin.NEXT);
        Node outer = graph.next(first.id, Pin.NEXT), yes = graph.next(outer.id, Pin.TRUE),
            inner = graph.next(yes.id, Pin.NEXT);
        Node[] expected = { first, yes, graph.next(inner.id, Pin.TRUE), graph.next(inner.id, Pin.FALSE),
            graph.next(inner.id, Pin.AFTER), graph.next(outer.id, Pin.FALSE), graph.next(outer.id, Pin.AFTER),
            graph.next(
                graph.triggers()
                    .get(1).id,
                Pin.NEXT) };
        HashSet<UUID> identities = new HashSet<>();
        for (int i = 0; i < expected.length; i++) {
            Span span = map.atCaret(nth(source, "print(", i));
            assertEquals(expected[i].id, span.node);
            assertEquals("print(\"same\")", source.substring(span.sourceStart, span.sourceEnd));
            identities.add(span.node);
        }
        assertEquals(8, identities.size());
        assertEquals(outer.id, map.atCaret(source.indexOf("if \"source\"")).node);
        assertEquals(inner.id, map.atCaret(source.indexOf("if true")).node);
        assertEquals(
            graph.triggers()
                .get(1).id,
            map.atCaret(source.indexOf("REDSTONE PULSE")).node);
        assertEquals(
            graph.nodes()
                .size(),
            expected.length + 4);
    }

    @Test
    void headersHaveExactHalfOpenSpansAndTriviaNameElseEndHaveNoNode() {
        String source = "NAME \"Header\"\n-- before print(0)\nEVERY 20 TICKS DO\n  IF TRUE THEN\n"
            + "    PRINT(\"😀\", -- inside\n  1) -- after\n ELSE\n FORGET\n END\nEND\n";
        BlueprintGraph graph = BlueprintCompiler.importText(source);
        StudioSourceMap map = map(source, graph);
        Span trigger = map.atCaret(source.indexOf("EVERY")), condition = map.atCaret(source.indexOf("IF TRUE")),
            print = map.atCaret(source.indexOf("PRINT("));
        assertEquals("EVERY 20 TICKS DO", source.substring(trigger.sourceStart, trigger.sourceEnd));
        assertEquals("IF TRUE THEN", source.substring(condition.sourceStart, condition.sourceEnd));
        assertEquals("PRINT(\"😀\", -- inside\n  1)", source.substring(print.sourceStart, print.sourceEnd));
        for (String value : new String[] { "NAME", "Header", "-- before", "before print", "-- inside", "inside",
            "-- after", "ELSE", " END", "\nEND" })
            assertNull(
                map.atCaret(source.indexOf(value) + (value.startsWith(" ") || value.startsWith("\n") ? 1 : 0)),
                value);
        for (int offset : new int[] { -1, source.length(), source.length() + 1, trigger.displayEnd,
            condition.displayEnd, print.displayEnd, source.indexOf("  IF"), source.indexOf("EVERY") + 5,
            source.indexOf("😀") + 1 }) assertNull(map.atCaret(offset), "offset " + offset);
        assertEquals(print.node, map.atCaret(source.indexOf("😀")).node);
        assertEquals(print.node, map.atCaret(print.displayEnd - 1).node);
        assertEquals(trigger.node, map.atCaret(trigger.displayEnd - 1).node);
    }

    @Test
    void astralUnicodeCrLfAndLoneCrMapRawBytesToNormalizedUtf16WithoutRewriting() {
        String source = "-- 🏭 before\r\nNAME \"😀 program\"\rEVERY 20 TICKS DO\r\n"
            + "PRINT(\"🏭 value\")\r\nTRANSFER(LABEL(\"😀 in\"), LABEL(\"out\"))\rEND";
        BlueprintGraph graph = BlueprintCompiler.importText(source);
        String display = StudioSourceMap.normalize(source);
        StudioSourceMap map = map(source, graph);
        for (String clause : new String[] { "PRINT(\"🏭 value\")", "TRANSFER(LABEL(\"😀 in\"), LABEL(\"out\"))" }) {
            Span span = map.atCaret(display.indexOf(clause));
            assertEquals(clause, source.substring(span.sourceStart, span.sourceEnd));
            assertEquals(clause, display.substring(span.displayStart, span.displayEnd));
            assertNotEquals(span.sourceStart, span.displayStart);
            assertEquals(span.node, map.atCaret(display.indexOf(clause) + clause.indexOf('(')).node);
        }
        assertTrue(map.matches(PROJECT, source, display, graph));
        assertFalse(map.matches(PROJECT, display, display, graph));
        assertEquals(source, source.substring(0));
    }

    @Test
    void sourceMetadataIsUnmappedAndDocumentCodeKeepsItsOwnOffsetDomain() {
        String source = "-- 🏭\r\n" + program("PRINT(\"same\") PRINT(\"same\")");
        BlueprintDocument imported = BlueprintDocument.open(source);
        String saved = imported.saveEdited(imported.graph);
        StudioDocument document = new StudioDocument();
        document.load(saved);
        document.openVisual();
        StudioSourceMap map = StudioSourceMap
            .prepare(PROJECT, document, StudioSourceMap.normalize(document.code()), false, false, false);
        assertEquals(saved, document.source());
        assertFalse(
            document.code()
                .contains(BlueprintDocument.MARKER));
        StudioSourceMap raw = map(saved, document.graph());
        assertNull(
            raw.atCaret(
                StudioSourceMap.normalize(saved)
                    .indexOf(BlueprintDocument.MARKER)));
        assertEquals(
            map.atCaret(
                StudioSourceMap.normalize(document.code())
                    .indexOf("PRINT")).node,
            raw.atCaret(
                StudioSourceMap.normalize(saved)
                    .indexOf("PRINT")).node);
    }

    @Test
    void exactSourceProjectNodeInstancesAndStructureRejectStaleMapsButViewportPositionsAreIrrelevant() {
        String source = program("IF TRUE THEN PRINT(1) ELSE PRINT(1) END PRINT(1)");
        BlueprintGraph graph = BlueprintCompiler.importText(source);
        StudioSourceMap map = map(source, graph);
        assertFalse(map.matches(UUID.randomUUID(), source, source, graph));
        assertFalse(map.matches(PROJECT, source + " ", source + " ", graph));
        assertFalse(map.matches(PROJECT, source, source + " ", graph));
        assertFalse(map.matches(PROJECT, source, source, BlueprintCompiler.importText(source)));
        assertFalse(map.matches(PROJECT, source, source, graph.copy()));
        Node node = graph.nodes()
            .iterator()
            .next();
        node.setPosition(900, 1000);
        assertTrue(map.matches(PROJECT, source, source, graph));
        node.setClause(
            node.clause()
                .toLowerCase());
        assertFalse(map.matches(PROJECT, source, source, graph));
        BlueprintGraph changedGraph = graph;
        assertThrows(IllegalArgumentException.class, () -> map.requireCurrent(PROJECT, source, source, changedGraph));
        graph = BlueprintCompiler.importText(source);
        StudioSourceMap current = map(source, graph);
        Node branch = graph.next(
            graph.triggers()
                .get(0).id,
            Pin.NEXT);
        branch.hasElse = false;
        assertFalse(current.matches(PROJECT, source, source, graph));
        branch.hasElse = true;
        graph.disconnect(branch.id, Pin.FALSE);
        assertFalse(current.matches(PROJECT, source, source, graph));
    }

    @Test
    void replacingNodeWithSameUuidOrChangingNameAndCommentsInvalidatesCachedIdentity() {
        String source = program("PRINT(1)");
        BlueprintGraph graph = BlueprintCompiler.importText(source);
        StudioSourceMap map = map(source, graph);
        Node root = graph.triggers()
            .get(0), statement = graph.next(root.id, Pin.NEXT);
        graph.remove(statement.id);
        Node replacement = new Node(statement.id, statement.type, statement.x(), statement.y(), statement.clause());
        graph.add(replacement);
        graph.connect(root.id, Pin.NEXT, replacement.id);
        assertFalse(map.matches(PROJECT, source, source, graph));
        StudioSourceMap next = map(source, graph);
        graph.setComments("-- changed");
        assertFalse(next.matches(PROJECT, source, source, graph));
        graph.setComments("");
        graph.setNameClause("NAME \"new\"");
        assertFalse(next.matches(PROJECT, source, source, graph));
    }

    @Test
    void pendingRequestsRawFieldsGesturesAndDisplayMismatchRefuseBeforeLazyImport() {
        StudioDocument document = new StudioDocument();
        document.load(program("INPUT FROM \"source\""));
        String source = document.source();
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioSourceMap.prepare(PROJECT, document, source, true, false, false));
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioSourceMap.prepare(PROJECT, document, source, false, true, false));
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioSourceMap.prepare(PROJECT, document, source, false, false, true));
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioSourceMap.prepare(PROJECT, document, source + " ", false, false, false));
        assertNull(document.graph());
        assertEquals(source, document.source());
        assertFalse(document.undo());
        document.openVisual();
        Node input = document.graph()
            .next(
                document.graph()
                    .triggers()
                    .get(0).id,
                Pin.NEXT);
        StudioFieldDraft fields = new StudioFieldDraft(
            PROJECT,
            input,
            source,
            StudioNodeProperties.inspect(input.type, input.clause()));
        fields.set("label", "\"pending\"");
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioSourceMap.prepare(PROJECT, document, source, false, fields.dirty(), false));
        assertEquals("\"pending\"", fields.value("label"));
        assertEquals(source, document.source());
    }

    @Test
    void unsupportedInvalidStaleAndDisconnectedProgramsNeverGuessAPartialGraph() {
        for (String source : new String[] { "EVERY bad", "LET $x = 1 " + program("WAIT 1 PRINT($x)"),
            program("PRINT(1) WHILE TRUE DO SETCIRCUIT(LABEL(\"source\"), 1) END"),
            program("TRANSFER(LABEL(\"source\"),LABEL(\"dest\")) INTO $m PRINT($m.moved)"),
            program("IF TRUE THEN PRINT(1) ELSE IF FALSE THEN PRINT(2) END") }) {
            StudioDocument document = new StudioDocument();
            document.load(source);
            assertThrows(
                IllegalArgumentException.class,
                () -> StudioSourceMap.prepare(PROJECT, document, source, false, false, false));
            assertNull(document.graph());
            assertEquals(source, document.source());
            assertFalse(document.undo());
        }
        String source = program("PRINT(1)");
        BlueprintGraph wrong = BlueprintCompiler.importText(program("PRINT(2)"));
        assertThrows(IllegalArgumentException.class, () -> map(source, wrong));
        StudioDocument document = new StudioDocument();
        document.load(source);
        document.openVisual();
        document.editGraph(graph -> graph.add(new Node(Type.PRINT, 0, 0)));
        assertTrue(document.incomplete());
        BlueprintGraph disconnected = document.graph();
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioSourceMap.prepare(PROJECT, document, source, false, false, false));
        assertSame(disconnected, document.graph());
        assertEquals(source, document.source());
        assertThrows(IllegalArgumentException.class, () -> map(source, disconnected));
        assertTrue(document.undo());
        assertFalse(document.incomplete());
    }

    @Test
    void explicitSelectionKeepsCodeAndGraphUndoRedoAndRawSourceBytes() {
        String source = "-- 🏭\r\n" + program("PRINT(\"😀\")");
        StudioDocument document = new StudioDocument();
        document.load(source);
        document.openVisual();
        Node root = document.graph()
            .triggers()
            .get(0);
        document.editGraph(
            graph -> graph.node(root.id)
                .setPosition(500, 500));
        assertTrue(document.undo());
        String before = document.source();
        SfmlCodeEditorWidget editor = new SfmlCodeEditorWidget(
            () -> {},
            () -> {},
            () -> {},
            () -> {},
            () -> true,
            error -> fail(error));
        editor.load(source);
        editor.buffer.move(
            editor.source()
                .length(),
            false);
        editor.buffer.insert(" -- undo marker");
        assertTrue(editor.buffer.undo());
        StudioSourceMap map = StudioSourceMap.prepare(PROJECT, document, editor.source(), false, false, false);
        Span target = map.atCaret(
            editor.source()
                .indexOf("PRINT"));
        editor.revealRange(target.displayStart, target.displayEnd);
        assertEquals("PRINT(\"😀\")", editor.buffer.selection());
        assertEquals(target.displayStart, editor.buffer.caret());
        assertEquals(before, document.source());
        assertTrue(editor.buffer.redo());
        assertTrue(editor.buffer.undo());
        assertEquals(StudioSourceMap.normalize(source), editor.source());
        assertTrue(document.redo());
        assertEquals(
            500,
            document.graph()
                .node(root.id)
                .x());
        assertTrue(document.undo());
        assertEquals(before, document.source());
    }
}
