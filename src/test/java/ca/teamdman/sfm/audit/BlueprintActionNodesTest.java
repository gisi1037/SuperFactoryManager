package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.sfm.client.blueprint.BlueprintCanvasGeometry;
import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.client.blueprint.BlueprintDocument;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioDocument;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties.Field;
import ca.teamdman.sfm.client.gui.mui2.StudioLayout;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;
import ca.teamdman.sfml.program_builder.VisualCapabilityReport.Kind;

class BlueprintActionNodesTest {

    private static final String FROM = "LaBeL(\"Input 🏭\").side(\"north\").slots(0, 2)",
        TO = "LABEL(\"Destination Case\")";

    private static BlueprintGraph supported(String source) {
        BlueprintCompiler.ImportResult result = BlueprintCompiler.inspectText(source);
        assertTrue(result.report.isAvailable(), result.report.getMessage());
        assertNotNull(result.graph);
        assertEquals(compiled(source), compiled(BlueprintCompiler.generate(result.graph)));
        return result.graph;
    }

    private static String compiled(String source) {
        ProgramBuildResult built = new ProgramBuilder(source).build();
        assertTrue(built.isSuccess(), built.getErrorMessage());
        // Expression printers retain original whitespace. Token identity keeps spaces inside
        // strings meaningful without mistaking punctuation spacing for an execution change.
        SFMLLexer lexer = new SFMLLexer(
            CharStreams.fromString(
                built.getProgram()
                    .toString()));
        StringBuilder canonical = new StringBuilder();
        for (Token token : lexer.getAllTokens()) if (token.getChannel() == Token.DEFAULT_CHANNEL) {
            canonical.append(token.getType())
                .append(':');
            int type = token.getType();
            if (type == SFMLLexer.STRING || type == SFMLLexer.IDENTIFIER
                || type == SFMLLexer.NUMBER
                || type == SFMLLexer.NUMBER_WITH_G_SUFFIX
                || type == SFMLLexer.VARIABLE) canonical.append(token.getText());
            canonical.append('|');
        }
        return canonical.toString();
    }

    private static Node first(BlueprintGraph graph, Type type) {
        return graph.nodes()
            .stream()
            .filter(node -> node.type == type)
            .findFirst()
            .get();
    }

    @Test
    void everyTransferArityRoundTripsInNestedBranchesWithCommentsAndUnicode() {
        for (String tail : new String[] { "", ", ItEm(\"minecraft:iron_ingot\")",
            ", ItEm(\"minecraft:iron_ingot\"), (2 + 3) * 4" }) {
            String source = "name \"Case 🏭\"\n-- route 😀\nevery 20 ticks do\n"
                + "if true then if false then print(\"skipped\") else\n"
                + "tRaNsFeR("
                + FROM
                + ", "
                + TO
                + tail
                + ") -- transfer comment\n"
                + "end else print(\"other\") end\nprint(\"after\", 3) end";
            BlueprintGraph graph = supported(source);
            String generated = BlueprintCompiler.generate(graph);
            assertTrue(generated.contains("-- route 😀"));
            assertTrue(generated.contains("-- transfer comment"));
            Node transfer = first(graph, Type.TRANSFER);
            assertTrue(
                transfer.clause()
                    .startsWith("tRaNsFeR"));
            assertTrue(
                transfer.clause()
                    .contains("\"Input 🏭\""));
            assertTrue(
                transfer.clause()
                    .contains("\"Destination Case\""));
            assertEquals(
                tail.isEmpty() ? 2 : tail.endsWith("4") ? 4 : 3,
                StudioNodeProperties.inspect(Type.TRANSFER, transfer.clause())
                    .size());
            BlueprintDocument reopened = BlueprintDocument.open(BlueprintDocument.save(graph));
            assertTrue(reopened.restoredLayout);
            assertEquals(transfer.id, first(reopened.graph, Type.TRANSFER).id);
            assertEquals(compiled(source), compiled(BlueprintCompiler.generate(reopened.graph)));
        }
    }

    @Test
    void printOwnsOnlyItsExpressionsAndRetainsAllEightArgumentSpans() {
        String clause = "pRiNt(\"message 😀\", 1 + 2, [1, 2], {value: 7}, true, "
            + "LABEL(\"Input 🏭\").available(), ITEM(\"*\"), 8)";
        supported("EVERY 20 TICKS DO " + clause + " END");
        List<Field> fields = StudioNodeProperties.inspect(Type.PRINT, clause);
        assertEquals(8, fields.size());
        for (int i = 0; i < fields.size(); i++) assertEquals(
            "expression" + i,
            fields.get(i)
                .getKey());
        assertEquals(
            clause.replace("1 + 2", "(4 + 5)"),
            StudioNodeProperties.update(Type.PRINT, clause, "expression1", "(4 + 5)"));
        assertEquals(
            clause.replace("{value: 7}", "{value: 9, title: \"X\"}"),
            StudioNodeProperties.update(Type.PRINT, clause, "expression3", "{value: 9, title: \"X\"}"));
    }

    @Test
    void transferEditsReplaceExactlyOneExpressionWithoutChangingTheOverload() {
        String clause = "tRaNsFeR(  " + FROM + " ,  " + TO + " , ItEm(\"*\"), (2 + 3)  )";
        String[] keys = { "source", "destination", "resource", "amount" };
        String[] old = { FROM, TO, "ItEm(\"*\")", "(2 + 3)" };
        String[] values = { "LABEL(\"Other 🏭\").slots(1, 1)", "LABEL(\"Dest\").side(\"top\")",
            "ITEM(\"minecraft:gold_ingot\")", "(7 * 2)" };
        for (int i = 0; i < keys.length; i++) assertEquals(
            clause.replace(old[i], values[i]),
            StudioNodeProperties.update(Type.TRANSFER, clause, keys[i], values[i]));
        String automatic = "TRANSFER(LABEL(\"s\"), LABEL(\"d\"))";
        assertEquals(
            2,
            StudioNodeProperties.inspect(Type.TRANSFER, automatic)
                .size());
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties.update(Type.TRANSFER, automatic, "amount", "1"));
    }

    @Test
    void fieldsRejectInjectionCommentsTypeErrorsOverflowAndUndefinedVariablesAtomically() {
        String transfer = "TRANSFER(LABEL(\"s\"), LABEL(\"d\"), ITEM(\"*\"), 1)";
        for (String bad : new String[] { "", "LABEL(\"s\"), LABEL(\"extra\")", "LABEL(\"s\")) PRINT(1)",
            "LABEL(\"s\") -- hide", "LABEL(\"s\")\nPRINT(1)", "LABEL(\"s\")\rPRINT(1)", "LABEL(\"s\")\0",
            "LABEL(\"unterminated)", "1", "$undefined", "LABEL(\"s\") @" })
            assertThrows(
                IllegalArgumentException.class,
                () -> StudioNodeProperties.update(Type.TRANSFER, transfer, "source", bad),
                bad);
        for (String bad : new String[] { "ITEM(\"*\")", "true", "9223372036854775808", "1,2", "1 -- hide" })
            assertThrows(
                IllegalArgumentException.class,
                () -> StudioNodeProperties.update(Type.TRANSFER, transfer, "amount", bad),
                bad);
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties.update(Type.TRANSFER, transfer, "resource", "LABEL(\"s\")"));
        String print = "PRINT(\"kept\", 1)";
        for (String bad : new String[] { "1,2", "1) TRANSFER(LABEL(\"s\"),LABEL(\"d\")) PRINT(1", "1 -- hide", "$bad" })
            assertThrows(
                IllegalArgumentException.class,
                () -> StudioNodeProperties.update(Type.PRINT, print, "expression0", bad),
                bad);
        StudioDocument document = new StudioDocument();
        document.load("EVERY 20 TICKS DO " + transfer + " END");
        document.openVisual();
        String before = document.source();
        UUID id = first(document.graph(), Type.TRANSFER).id;
        String originalClause = document.graph()
            .node(id)
            .clause();
        assertThrows(IllegalArgumentException.class, () -> document.editGraph(graph -> {
            graph.node(id)
                .setClause(StudioNodeProperties.update(Type.TRANSFER, transfer, "amount", "2"));
            graph.node(id)
                .setClause(
                    StudioNodeProperties.update(
                        Type.TRANSFER,
                        graph.node(id)
                            .clause(),
                        "source",
                        "1"));
        }));
        assertEquals(before, document.source());
        assertEquals(
            originalClause,
            document.graph()
                .node(id)
                .clause());
        assertFalse(document.undo());
    }

    @Test
    void unsupportedEnclosingAndSiblingConstructsKeepTheWholeOriginalSource() {
        String transfer = "TRANSFER(LABEL(\"source\"), LABEL(\"dest\"))";
        String[] sources = { "STATE $x = 1 EVERY 20 TICKS DO " + transfer + " PRINT($x) END",
            "EVERY 20 TICKS DO " + transfer + " LET $x = 1 PRINT($x) END",
            "EVERY 20 TICKS DO FOR $x = 0 TO 2 DO " + transfer + " PRINT($x) END END",
            "EVERY 20 TICKS DO WHILE FALSE DO " + transfer + " PRINT(1) END END",
            "EVERY 20 TICKS DO " + transfer + " IF (1 > 0) THEN INPUT (1 + 2) FROM source END END",
            "EVERY 20 TICKS DO IF TRUE THEN " + transfer + " ELSE IF FALSE THEN PRINT(1) END END",
            "EVERY 20 TICKS DO " + transfer + " INTO $result PRINT($result.moved) END",
            "EVERY 20 TICKS DO " + transfer + " PRINT(LEN([1,2])) END",
            "FUNCTION echo($n: INTEGER) RETURNS INTEGER DO RETURN $n END EVERY 20 TICKS DO " + transfer
                + " PRINT(echo(1)) END",
            // Slice21 supports the named wrapper; a local declaration must still block its whole import.
            "EVERY REDSTONE RISING FROM signal DO " + transfer + " LET $x = 1 PRINT($x) END",
            "STATE $x = 1 EVERY 20 TICKS DO " + transfer + " SET $x = 2 PRINT($x) END",
            "EVERY 20 TICKS DO FOR $x IN [1,2] DO " + transfer + " PRINT($x) END END" };
        for (String source : sources) {
            compiled(source);
            BlueprintCompiler.ImportResult result = BlueprintCompiler.inspectText(source);
            assertFalse(result.report.isAvailable(), source);
            assertNull(result.graph, source);
            StudioDocument document = new StudioDocument();
            document.load(source);
            assertThrows(IllegalArgumentException.class, document::openVisual);
            assertEquals(source, document.source());
            assertEquals(source, document.code());
            assertNull(document.graph());
        }
        assertTrue(
            BlueprintCompiler.inspectText(sources[4]).report.getBlockers()
                .stream()
                .anyMatch(blocker -> blocker.kind == Kind.DATA_EXPRESSION));
        assertTrue(
            BlueprintCompiler.inspectText(sources[7]).report.getBlockers()
                .stream()
                .anyMatch(blocker -> blocker.kind == Kind.FUNCTION_CALL));
        assertTrue(
            BlueprintCompiler.inspectText(sources[8]).report.getBlockers()
                .stream()
                .anyMatch(blocker -> blocker.kind == Kind.FUNCTION_DEFINITION));
        assertTrue(
            BlueprintCompiler.inspectText(sources[9]).report.getBlockers()
                .stream()
                .anyMatch(blocker -> blocker.kind == Kind.LOCAL_DECLARATION));
        assertTrue(
            BlueprintCompiler.inspectText(sources[10]).report.getBlockers()
                .stream()
                .anyMatch(blocker -> blocker.kind == Kind.ASSIGNMENT));
    }

    @Test
    void fieldEditsAndTextReopeningKeepIdsLayoutUndoAndSourceOwnership() {
        StudioDocument document = new StudioDocument();
        document
            .load("-- 🏭 kept\nEVERY 20 TICKS DO TRANSFER(LABEL(\"s\"),LABEL(\"d\"),ITEM(\"*\"),1) PRINT(\"hi\") END");
        document.openVisual();
        UUID id = first(document.graph(), Type.TRANSFER).id;
        document.editGraph(
            graph -> graph.node(id)
                .setPosition(117, 233));
        String before = document.source();
        document.editGraph(
            graph -> graph.node(id)
                .setClause(
                    StudioNodeProperties.update(
                        Type.TRANSFER,
                        graph.node(id)
                            .clause(),
                        "amount",
                        "4 + 1")));
        String after = document.source();
        assertNotEquals(before, after);
        assertTrue(document.undo());
        assertEquals(before, document.source());
        assertTrue(document.redo());
        assertEquals(after, document.source());
        BlueprintDocument reopened = BlueprintDocument.open(after);
        assertEquals(
            117,
            reopened.graph.node(id)
                .x());
        assertEquals(
            233,
            reopened.graph.node(id)
                .y());
        assertEquals(id, first(reopened.graph, Type.TRANSFER).id);
        BlueprintDocument textEdited = BlueprintDocument.open(after.replace("4 + 1", "7 + 1"));
        assertEquals(id, first(textEdited.graph, Type.TRANSFER).id);
        assertTrue(
            first(textEdited.graph, Type.TRANSFER).clause()
                .contains("7 + 1"));
        assertTrue(
            BlueprintCompiler.generate(reopened.graph)
                .contains("-- 🏭 kept"));
    }

    @Test
    void duplicatePrintIdentitiesAndDisconnectedActionDraftsRemainRecoverable() {
        StudioDocument document = new StudioDocument();
        document.load("EVERY 20 TICKS DO PRINT(\"same\") PRINT(\"same\") END");
        document.openVisual();
        List<UUID> ids = document.graph()
            .nodes()
            .stream()
            .map(node -> node.id)
            .collect(Collectors.toList());
        BlueprintDocument reopened = BlueprintDocument.open(BlueprintDocument.save(document.graph()));
        assertEquals(
            ids,
            reopened.graph.nodes()
                .stream()
                .map(node -> node.id)
                .collect(Collectors.toList()));
        for (Type type : new Type[] { Type.TRANSFER, Type.PRINT }) {
            String safe = document.source();
            Node disconnected = new Node(type, 400, 100);
            document.editGraph(graph -> graph.add(disconnected));
            assertTrue(document.incomplete());
            assertEquals(safe, document.source());
            assertThrows(IllegalStateException.class, () -> document.editCode(""));
            StudioDocument recovered = new StudioDocument();
            recovered.restore(document.snapshot());
            assertEquals(
                disconnected.clause(),
                recovered.graph()
                    .node(disconnected.id)
                    .clause());
            assertTrue(recovered.incomplete());
            assertTrue(document.undo());
            assertFalse(document.incomplete());
            assertTrue(document.redo());
            assertTrue(document.incomplete());
            assertTrue(document.undo());
        }
    }

    @Test
    void actionNodesUseExistingGraphLimitsAndEveryDefaultClauseCompiles() {
        for (Type type : Type.values()) BlueprintCompiler.validateClause(type, type.defaultClause);
        String source = "EVERY 20 TICKS DO\n";
        StringBuilder text = new StringBuilder(source);
        for (int i = 0; i < 255; i++) text.append("PRINT(1)\n");
        assertEquals(
            256,
            supported(text + "END").nodes()
                .size());
        BlueprintCompiler.ImportResult tooMany = BlueprintCompiler.inspectText(text + "PRINT(2)\nEND");
        assertNull(tooMany.graph);
        assertTrue(
            tooMany.report.getBlockers()
                .stream()
                .anyMatch(blocker -> blocker.kind == Kind.GRAPH_LIMIT));
        char[] characters = new char[4090];
        java.util.Arrays.fill(characters, 'x');
        String oversized = "EVERY 20 TICKS DO PRINT(\"" + new String(characters) + "\") END";
        compiled(oversized);
        BlueprintCompiler.ImportResult longClause = BlueprintCompiler.inspectText(oversized);
        assertNull(longClause.graph);
        assertTrue(
            longClause.report.getBlockers()
                .stream()
                .anyMatch(blocker -> blocker.kind == Kind.GRAPH_LIMIT));
        assertNotEquals(
            compiled("EVERY 20 TICKS DO PRINT(\"a b\") END"),
            compiled("EVERY 20 TICKS DO PRINT(\"ab\") END"));
    }

    @Test
    void nodePaletteSeparatesScrollableContentFromFooterAtSmallAndIndependentScales() {
        for (int displayWidth : new int[] { 640, 1280, 1920, 3840 })
            for (int displayHeight : new int[] { 480, 720, 1080, 2160 })
                for (int scale = 1; scale <= Math.min(displayWidth / 320, displayHeight / 240); scale++) {
                    int width = displayWidth / scale, height = displayHeight / scale;
                    StudioLayout.NodePalette layout = new StudioLayout.NodePalette(width, height);
                    assertTrue(layout.width <= width - 12);
                    assertTrue(layout.height <= height - 12);
                    assertTrue(layout.listHeight >= 54);
                    assertTrue(26 + layout.listHeight < layout.footerY);
                    assertTrue(layout.footerY + 20 <= layout.height - 8);
                    // The viewport scrolls; adding actions does not move the footer or lose the last row.
                    assertTrue(Type.values().length * 21 > layout.listHeight);
                }
    }

    @Test
    void legacyCanvasOriginAndFitUseTheExpandedToolbarBoundary() {
        BlueprintGraph nodes = supported("EVERY 20 TICKS DO PRINT(1) END");
        double minY = nodes.nodes()
            .stream()
            .mapToDouble(Node::y)
            .min()
            .getAsDouble();
        double maxY = nodes.nodes()
            .stream()
            .mapToDouble(node -> node.y() + 48)
            .max()
            .getAsDouble();
        double zoom = BlueprintCanvasGeometry.fitZoom(320, 240 - 68, 144, maxY - minY);
        double pan = 10 - minY * zoom;
        for (Node node : nodes.nodes()) {
            assertTrue(BlueprintCanvasGeometry.screenY(node.y(), pan, zoom) > BlueprintCanvasGeometry.TOP);
            assertTrue(BlueprintCanvasGeometry.screenY(node.y() + 48, pan, zoom) < 240 - 68);
        }
        for (double scale : new double[] { .25, .5, 1, 1.5 }) {
            double world = BlueprintCanvasGeometry.worldY(112, 6, scale);
            assertEquals(112, BlueprintCanvasGeometry.screenY(world, 6, scale));
            double anchored = BlueprintCanvasGeometry.anchoredPanY(112, world, scale * 1.1);
            assertEquals(112, BlueprintCanvasGeometry.screenY(world, anchored, scale * 1.1));
        }
        assertEquals(0d, BlueprintCanvasGeometry.worldY(78, 6, 2));
        assertEquals(10d, BlueprintCanvasGeometry.worldY(98, 6, 2));
    }
}
