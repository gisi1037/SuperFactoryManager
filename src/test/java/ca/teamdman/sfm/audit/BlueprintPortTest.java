package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.client.blueprint.BlueprintDocument;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Pin;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.common.net.PacketSaveProgram;
import ca.teamdman.sfm.common.util.ProgramRevision;
import ca.teamdman.sfml.program_builder.ProgramBuilder;
import io.netty.buffer.Unpooled;

/** The visual front end must preserve the qualified text engine, including exact transfer counts. */
class BlueprintPortTest {

    @TestFactory
    List<DynamicTest> originalAndRoundTrippedCoreProgramsHaveIdenticalResults() throws Exception {
        List<DynamicTest> cases = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(getClass().getResourceAsStream("/sfml/v1/core.tsv"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.isEmpty()) continue;
                String[] fields = line.split("\t", -1);
                cases.add(DynamicTest.dynamicTest(fields[0], () -> {
                    if (!fields[5].isEmpty()) {
                        assertThrows(IllegalArgumentException.class, () -> BlueprintDocument.open(fields[6]));
                    } else {
                        String generated = BlueprintCompiler.generate(BlueprintDocument.open(fields[6]).graph);
                        assertArrayEquals(
                            execute(fields[6], fields[1], fields[2]),
                            execute(generated, fields[1], fields[2]));
                        assertArrayEquals(
                            new int[] { Integer.parseInt(fields[3]), Integer.parseInt(fields[4]) },
                            execute(generated, fields[1], fields[2]));
                    }
                }));
            }
        }
        return cases;
    }

    private static int[] execute(String source, String initial, String dest) throws Exception {
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        fixture.stock(0, Integer.parseInt(initial));
        if (Integer.parseInt(dest) > 0) fixture.dest.setInventorySlotContents(
            0,
            new net.minecraft.item.ItemStack(AuditRegressionTest.ITEM, Integer.parseInt(dest)));
        fixture.runProgram(source);
        return new int[] { AuditRegressionTest.count(fixture.source), AuditRegressionTest.count(fixture.dest) };
    }

    @Test
    void branchContinuationRunsAfterTheChosenBranch() throws Exception {
        for (String condition : new String[] { "TRUE", "FALSE" }) {
            String source = "EVERY 20 TICKS DO IF " + condition
                + " THEN INPUT 4 FROM source OUTPUT 2 TO dest ELSE INPUT 7 FROM source OUTPUT 3 TO dest END OUTPUT 1 TO dest END";
            String generated = BlueprintCompiler.generate(BlueprintCompiler.importText(source));
            assertArrayEquals(execute(source, "10", "0"), execute(generated, "10", "0"));
            assertArrayEquals(
                new int[] { condition.equals("TRUE") ? 7 : 6, condition.equals("TRUE") ? 3 : 4 },
                execute(generated, "10", "0"));
        }
    }

    @Test
    void advancedClausesAndTriggerOrderSurviveImportAndMetadataReload() {
        String source = "NAME \"Keep Case\"\n-- retain this explanation\n" + "EVERY 20 GLOBAL PLUS 2 TICKS DO\n"
            + " INPUT 64 EACH RETAIN 8 EACH minecraft:iron_ingot EXCEPT minecraft:stone FROM EACH \"Input A\", source ROUND ROBIN BY BLOCK TOP SIDE SLOTS 0-8\n"
            + " IF NOT (SOME source HAS >= 3 minecraft:iron_ingot) THEN OUTPUT 4 EACH RETAIN 2 EACH TO EMPTY SLOTS IN EACH dest BOTTOM SIDE SLOT 1 ELSE FORGET source, dest END\n"
            + " FORGET\nEND\nEVERY REDSTONE PULSE DO OUTPUT 15 REDSTONE LEVEL TO dest END";
        assertTrue(
            new ProgramBuilder(source).build()
                .isSuccess());
        BlueprintGraph graph = BlueprintCompiler.importText(source);
        String generated = BlueprintCompiler.generate(graph);
        assertTrue(generated.contains("64 EACH RETAIN 8 EACH"));
        assertTrue(generated.contains("ROUND ROBIN BY BLOCK TOP SIDE SLOTS 0 - 8"));
        assertTrue(generated.contains("EMPTY SLOTS IN EACH"));
        assertTrue(generated.indexOf("GLOBAL") < generated.indexOf("EVERY REDSTONE PULSE"));
        assertTrue(generated.contains("-- retain this explanation"));
        Node first = graph.triggers()
            .get(0);
        first.setPosition(-123.5, 321.25);
        BlueprintDocument reopened = BlueprintDocument.open(BlueprintDocument.save(graph));
        assertTrue(reopened.restoredLayout);
        assertEquals(generated, BlueprintCompiler.generate(reopened.graph));
        assertEquals(
            -123.5,
            reopened.graph.node(first.id)
                .x());
        assertEquals(
            321.25,
            reopened.graph.node(first.id)
                .y());
    }

    @Test
    void commentsAndUnicodeMarkersInsideLabelsAreNotMistakenForMetadata() {
        String code = "-- first\nEVERY 20 TICKS DO INPUT FROM \"箱😀\n-- sfm:blueprint:v1 literal\" -- explain\nOUTPUT TO dest END";
        BlueprintDocument document = BlueprintDocument.open(code);
        String saved = BlueprintDocument.save(document.graph);
        assertTrue(BlueprintDocument.open(saved).restoredLayout);
        assertTrue(
            BlueprintCompiler.generate(document.graph)
                .contains("-- explain"));
        assertTrue(
            BlueprintCompiler.generate(document.graph)
                .contains("箱😀\n-- sfm:blueprint:v1 literal"));
    }

    @Test
    void editedTextWinsOverSavedGraph() {
        String saved = BlueprintDocument
            .save(BlueprintCompiler.importText("EVERY 20 TICKS DO INPUT FROM source OUTPUT 3 TO dest END"));
        BlueprintDocument changed = BlueprintDocument.open(saved.replace("OUTPUT 3", "OUTPUT 7"));
        assertFalse(changed.restoredLayout);
        assertTrue(
            BlueprintCompiler.generate(changed.graph)
                .contains("OUTPUT 7"));
        assertTrue(changed.notice.contains("stale"));
    }

    @Test
    void malformedDuplicateAndOverexpandedLayoutsFallBackToCurrentText() throws Exception {
        String code = "EVERY 20 TICKS DO INPUT FROM source END\n";
        for (String suffix : new String[] { "not-base64", Base64.getEncoder()
            .encodeToString(new byte[] { 1, 2, 3 }) }) {
            BlueprintDocument document = BlueprintDocument.open(code + BlueprintDocument.MARKER + suffix);
            assertFalse(document.restoredLayout);
            assertEquals(
                2,
                document.graph.nodes()
                    .size());
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (GZIPOutputStream zip = new GZIPOutputStream(bytes)) {
            zip.write(new byte[262145]);
        }
        BlueprintDocument bomb = BlueprintDocument.open(
            code + BlueprintDocument.MARKER
                + Base64.getEncoder()
                    .encodeToString(bytes.toByteArray()));
        assertFalse(bomb.restoredLayout);
        assertEquals(
            2,
            bomb.graph.nodes()
                .size());
        String good = BlueprintDocument.save(BlueprintCompiler.importText(code));
        BlueprintDocument duplicate = BlueprintDocument
            .open(good + "\n" + good.substring(good.indexOf(BlueprintDocument.MARKER)));
        assertFalse(duplicate.restoredLayout);
        assertEquals(
            2,
            duplicate.graph.nodes()
                .size());
    }

    @Test
    void unsupportedElseIfAndInvalidTextFailWithoutReturningAPartialGraph() {
        assertThrows(
            IllegalArgumentException.class,
            () -> BlueprintDocument.open("EVERY 20 TICKS DO IF TRUE THEN FORGET ELSE IF FALSE THEN FORGET END END"));
        assertThrows(IllegalArgumentException.class, () -> BlueprintDocument.open("EVERY nope"));
    }

    @Test
    void aNodeCannotInjectAnotherStatementOrTrigger() {
        for (String clause : new String[] { "INPUT FROM source OUTPUT TO dest",
            "INPUT FROM source END EVERY 20 TICKS DO", "INPUT FROM source -- hidden" })
            assertThrows(IllegalArgumentException.class, () -> BlueprintCompiler.validateClause(Type.INPUT, clause));
        assertThrows(
            IllegalArgumentException.class,
            () -> BlueprintCompiler.validateClause(Type.IF, "TRUE THEN OUTPUT TO dest END IF FALSE"));
        assertThrows(
            IllegalArgumentException.class,
            () -> BlueprintCompiler
                .validateClause(Type.TIMER, "EVERY 20 TICKS DO INPUT FROM source END EVERY 20 TICKS"));
    }

    @Test
    void connectionsReplaceSinglePinsAndRejectCyclesWithoutDestroyingOldWires() {
        BlueprintGraph graph = new BlueprintGraph();
        Node root = new Node(Type.TIMER, 0, 0), a = new Node(Type.INPUT, 0, 80), b = new Node(Type.OUTPUT, 0, 160);
        graph.add(root);
        graph.add(a);
        graph.add(b);
        graph.connect(root.id, Pin.NEXT, a.id);
        graph.connect(a.id, Pin.NEXT, b.id);
        assertThrows(IllegalArgumentException.class, () -> graph.connect(b.id, Pin.NEXT, a.id));
        assertSame(a, graph.next(root.id, Pin.NEXT));
        assertSame(b, graph.next(a.id, Pin.NEXT));
        assertThrows(IllegalArgumentException.class, () -> graph.connect(a.id, Pin.NEXT, root.id));
        graph.connect(root.id, Pin.NEXT, b.id);
        assertNull(graph.next(a.id, Pin.NEXT));
        assertThrows(IllegalArgumentException.class, () -> BlueprintCompiler.generate(graph)); // a is now disconnected
        graph.remove(a.id);
        assertDoesNotThrow(() -> BlueprintCompiler.generate(graph));
        BlueprintGraph copy = graph.copy();
        copy.node(b.id)
            .setClause("OUTPUT 2 TO dest");
        assertNotEquals(
            copy.node(b.id)
                .clause(),
            b.clause());
    }

    @Test
    void nodeNestingAndUtf8SaveBoundsAreEnforced() {
        BlueprintGraph graph = new BlueprintGraph();
        Node previous = new Node(Type.TIMER, 0, 0);
        graph.add(previous);
        for (int i = 0; i < 255; i++) {
            Node node = new Node(Type.FORGET, 0, i * 80);
            graph.add(node);
            graph.connect(previous.id, Pin.NEXT, node.id);
            previous = node;
        }
        assertThrows(IllegalArgumentException.class, () -> graph.add(new Node(Type.INPUT, 0, 0)));
        assertDoesNotThrow(() -> BlueprintDocument.save(graph));
        BlueprintGraph deep = new BlueprintGraph();
        previous = new Node(Type.TIMER, 0, 0);
        deep.add(previous);
        Pin pin = Pin.NEXT;
        for (int i = 0; i < 33; i++) {
            Node node = new Node(Type.IF, 0, i * 80);
            deep.add(node);
            deep.connect(previous.id, pin, node.id);
            previous = node;
            pin = Pin.TRUE;
        }
        assertThrows(IllegalArgumentException.class, () -> BlueprintCompiler.generate(deep));
        BlueprintGraph wide = new BlueprintGraph();
        wide.setComments("-- " + String.join("", java.util.Collections.nCopies(12000, "箱")));
        assertThrows(IllegalArgumentException.class, () -> BlueprintDocument.save(wide));
        assertThrows(IllegalArgumentException.class, () -> new Node(Type.INPUT, Double.NaN, 0));
        assertThrows(IllegalArgumentException.class, () -> new Node(Type.INPUT, 0, Double.POSITIVE_INFINITY));
    }

    @Test
    void guardedSavesRejectStaleSourceAfterPacketRoundTripAndLegacySavesStillWork() {
        PacketSaveProgram guarded = PacketSaveProgram.guarded(1, 2, 3, "EVERY 20 TICKS DO END", "old");
        io.netty.buffer.ByteBuf bytes = Unpooled.buffer();
        try {
            guarded.toBytes(bytes);
            PacketSaveProgram decoded = new PacketSaveProgram();
            decoded.fromBytes(bytes);
            assertTrue(decoded.acceptsSource("old"));
            assertFalse(decoded.acceptsSource("new"));
            assertTrue(ProgramRevision.matches(ProgramRevision.digest("箱😀"), "箱😀"));
            assertFalse(ProgramRevision.matches(new byte[1], "old"));
            bytes.clear();
            new PacketSaveProgram(1, 2, 3, "code", true).toBytes(bytes);
            decoded.fromBytes(bytes);
            assertTrue(decoded.acceptsSource("any source"));
            bytes.readerIndex(0);
            bytes.writerIndex(bytes.writerIndex() - 1); // previous-version packet without optional guard
            decoded.fromBytes(bytes);
            assertTrue(decoded.acceptsSource("any source"));
        } finally {
            bytes.release();
        }
    }
}
