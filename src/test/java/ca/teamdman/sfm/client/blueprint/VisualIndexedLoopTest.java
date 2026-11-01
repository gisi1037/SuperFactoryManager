package ca.teamdman.sfm.client.blueprint;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Pin;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.common.util.ProgramRevision;

class VisualIndexedLoopTest {

    @Test
    void bothNewLoopsImportWithBodyAndAfterAndKeepStableLayout() {
        String source = "LET values = [4,5] STATE total = 0 EVERY 20 TICKS { "
            + "FOR (LET index = 0; index < LEN(values); index = index + 1) { total = total + values[index]; } "
            + "FOR (index, value) IN values { PRINT(index, value); } PRINT(total); }";
        BlueprintGraph graph = BlueprintCompiler.importText(source);
        Node indexed = graph.nodes()
            .stream()
            .filter(n -> n.type == Type.JAVA_FOR)
            .findFirst()
            .get();
        Node each = graph.nodes()
            .stream()
            .filter(n -> n.type == Type.INDEXED_FOREACH)
            .findFirst()
            .get();
        assertNotNull(graph.next(indexed.id, Pin.BODY));
        assertEquals(each.id, graph.next(indexed.id, Pin.AFTER).id);
        assertNotNull(graph.next(each.id, Pin.BODY));
        assertNotNull(graph.next(each.id, Pin.AFTER));
        indexed.setPosition(324, 128);
        BlueprintDocument reopened = BlueprintDocument.open(BlueprintDocument.save(graph));
        assertEquals(
            324,
            reopened.graph.node(indexed.id)
                .x());
        assertTrue(BlueprintCompiler.sameTokens(source, BlueprintCompiler.generate(reopened.graph)));
        StudioSourceMap map = StudioSourceMap.build(new UUID(10, 37), source, source, graph);
        assertEquals(indexed.id, map.atCaret(source.indexOf("index <")).node);
        assertEquals(each.id, map.atCaret(source.indexOf("index, value")).node);
    }

    @Test
    void inspectorUpdatesWholeJavaHeaderAndBothForeachBindersWithoutTouchingSource() {
        String header = "FOR (LET index = 0; index < 3; index = index + 1)";
        assertEquals("FOR (; TRUE;)", StudioNodeProperties.update(Type.JAVA_FOR, header, "header", "FOR (; TRUE;)"));
        String each = "FOR (index, value) IN values";
        assertEquals(
            "FOR (ordinal, value) IN values",
            StudioNodeProperties.update(Type.INDEXED_FOREACH, each, "indexName", "ordinal"));
        assertEquals(
            "FOR (index, entry) IN values",
            StudioNodeProperties.update(Type.INDEXED_FOREACH, each, "name", "entry"));
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties.update(Type.JAVA_FOR, header, "header", header + " DO PRINT(1) END"));
    }

    @Test
    void mandatoryHeaderSeparatorsCannotNormalizeAwayInEitherBlockSyntax() {
        for (boolean braces : new boolean[] { false, true }) {
            String open = braces ? "{" : "DO", close = braces ? "}" : "END";
            String prefix = "LET flags = ARRAY([FALSE]) EVERY 20 TICKS " + open + " FOR (";
            String body = ") " + open + " PRINT(1) " + close + " " + close;
            String tested = prefix + "; POP(ref flags);" + body;
            String initialized = prefix + "POP(ref flags); ;" + body;
            assertFalse(ProgramRevision.sameExecutableTokens(tested, initialized));
            assertFalse(BlueprintCompiler.sameTokens(tested, initialized), "Mandatory separator roles were lost");
        }
        assertTrue(
            BlueprintCompiler
                .sameTokens("LET n=1; EVERY 20 TICKS DO PRINT(n); END", "LET n=1 EVERY 20 TICKS { PRINT(n) }"),
            "Optional terminators still normalize");
    }

    @Test
    void emptyAndOptionalClausesRoundTripAndInvalidScopesKeepTheSafeDraft() {
        for (String header : new String[] { "FOR (;;)", "FOR (; FALSE;)", "FOR (LET index=0; FALSE; index=index+1)" }) {
            String source = "EVERY 20 TICKS DO " + header + " DO END END";
            assertTrue(
                BlueprintCompiler.sameTokens(source, BlueprintCompiler.generate(BlueprintCompiler.importText(source))));
        }
        StudioDocument document = new StudioDocument();
        document.load("EVERY 20 TICKS { FOR (LET index=0; index<2; index=index+1) { PRINT(index) } }");
        document.openVisual();
        Node loop = document.graph()
            .nodes()
            .stream()
            .filter(n -> n.type == Type.JAVA_FOR)
            .findFirst()
            .get();
        String safe = document.code();
        document.editGraph(
            graph -> graph.node(loop.id)
                .setClause("FOR (LET index=0; missing; index=index+1)"));
        assertTrue(document.incomplete());
        assertEquals(safe, document.code());
        assertTrue(document.undo());
        assertFalse(document.incomplete());
    }
}
