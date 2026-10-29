package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.client.blueprint.BlueprintDocument;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;

class BlueprintRoundTripIdentityTest {

    // Pinned v1 DataOutputStream fixture: TIMER UUID(0,1), INPUT UUID(0,2), NEXT edge.
    private static final String LEGACY = "EVERY 20 TICKS DO\n  INPUT FROM source\nEND\n"
        + BlueprintDocument.LEGACY_MARKER
        + "H4sIAAAAAAACCmNgYGB8WN2qIOzXfn/StLMtM0q1cpa/2MXXHin1aNY+Z+7Ve5REGCCAiQEVMDKwhnj6ugY5uEL4DqFQCT7XMNegSAUjA4UQT2fvYDRtQHNYPf0CQkPg+lIaIAxBsLCCW5C/r0JxfmlRcirEGnRrWfxcI0LQDQUAUT+1UckAAAA=";

    private static Node first(BlueprintGraph graph, Type type) {
        return graph.nodes()
            .stream()
            .filter(node -> node.type == type)
            .findFirst()
            .get();
    }

    @Test
    void literalQuantityAndItemEditsPreserveConfidentIdentityButCurrentClausesWin() {
        BlueprintGraph graph = BlueprintCompiler
            .importText("EVERY 20 TICKS DO INPUT 4 minecraft:iron_ingot FROM source OUTPUT 2 TO dest END");
        Node input = first(graph, Type.INPUT);
        input.setPosition(-141.5, 287.25);
        String saved = BlueprintDocument.save(graph);
        for (String changed : new String[] { saved.replace("INPUT 4", "INPUT 7"),
            saved.replace("iron_ingot", "gold_ingot") }) {
            assertNotEquals(saved, changed, "The fixture must actually edit source text");
            BlueprintDocument reopened = BlueprintDocument.open(changed);
            Node current = first(reopened.graph, Type.INPUT);
            assertFalse(reopened.restoredLayout);
            assertEquals(input.id, current.id);
            assertEquals(-141.5, current.x());
            assertEquals(287.25, current.y());
            assertTrue(
                BlueprintCompiler.generate(reopened.graph)
                    .contains(changed.contains("INPUT 7") ? "INPUT 7" : "gold_ingot"));
        }
    }

    @Test
    void exactDuplicateStatementsRestoreTheirDistinctOrderedIdsAndPositions() {
        BlueprintGraph graph = BlueprintCompiler
            .importText("EVERY 20 TICKS DO INPUT FROM source INPUT FROM source END");
        int i = 0;
        for (Node node : graph.nodes()) node.setPosition(100 + i++, 200 + i);
        BlueprintDocument restored = BlueprintDocument.open(BlueprintDocument.save(graph));
        assertTrue(restored.restoredLayout);
        List<Node> before = new ArrayList<>(graph.nodes()), after = new ArrayList<>(restored.graph.nodes());
        assertEquals(before.size(), after.size());
        for (i = 0; i < before.size(); i++) {
            assertEquals(before.get(i).id, after.get(i).id);
            assertEquals(
                before.get(i)
                    .x(),
                after.get(i)
                    .x());
            assertEquals(
                before.get(i)
                    .y(),
                after.get(i)
                    .y());
        }
    }

    @Test
    void formattingOnlyEditsKeepOrderedIdentityForOtherwiseIndistinguishableStatements() {
        BlueprintGraph graph = BlueprintCompiler
            .importText("EVERY 20 TICKS DO INPUT FROM source INPUT FROM source END");
        int offset = 0;
        for (Node node : graph.nodes()) node.setPosition(30 + offset++, 90 + offset);
        String saved = BlueprintDocument.save(graph);
        String formatted = saved.replace("\n  INPUT", "\n      INPUT");
        assertNotEquals(saved, formatted, "The fixture must change executable-body whitespace");
        BlueprintDocument restored = BlueprintDocument.open(formatted);
        assertFalse(restored.restoredLayout, "The exact source revision differs despite identical canonical clauses");
        assertEquals(
            graph.nodes()
                .size(),
            restored.matchedNodes);
        List<Node> before = new ArrayList<>(graph.nodes()), after = new ArrayList<>(restored.graph.nodes());
        for (int i = 0; i < before.size(); i++) {
            assertEquals(before.get(i).id, after.get(i).id);
            assertEquals(
                before.get(i)
                    .x(),
                after.get(i)
                    .x());
            assertEquals(
                before.get(i)
                    .y(),
                after.get(i)
                    .y());
        }
    }

    @Test
    void layoutOnlySavesKeepExactFormattedCommentedUnicodeBodyAndDoNotAccumulateNewlines() {
        String source = "-- explanation 箱😀\nNAME \"Case kept\"\n\nEVERY 20 TICKS DO -- inline\n"
            + "    INPUT FROM \"箱😀\n-- sfm:blueprint:v1 literal\n-- sfm:blueprint:v2 literal\"\nEND\n";
        BlueprintDocument opened = BlueprintDocument.open(source);
        first(opened.graph, Type.INPUT).setPosition(17, 39);
        String saved = opened.saveEdited(opened.graph);
        assertEquals(source, BlueprintDocument.withoutMetadata(saved));
        BlueprintDocument reopened = BlueprintDocument.open(saved);
        assertTrue(reopened.restoredLayout);
        assertEquals(source, BlueprintDocument.withoutMetadata(reopened.saveEdited(reopened.graph)));
        assertEquals(1, occurrences(saved, BlueprintDocument.MARKER + "H4"));
    }

    @Test
    void prepareTextSaveCarriesOldLayoutWithoutRewritingDraftAndDropsItWhenNearTheByteCap() {
        BlueprintGraph graph = BlueprintCompiler.importText("EVERY 20 TICKS DO INPUT 1 FROM source END");
        Node input = first(graph, Type.INPUT);
        input.setPosition(75, 123);
        String authoritative = BlueprintDocument.save(graph);
        String draft = "-- my spacing\nEVERY 20 TICKS DO\n   INPUT 3 FROM source\nEND\n";
        assertTrue(authoritative.contains("INPUT 1"));
        assertTrue(draft.contains("INPUT 3"));
        assertNotEquals(BlueprintDocument.withoutMetadata(authoritative), draft);
        BlueprintDocument.TextSave prepared = BlueprintDocument.prepareTextSave(draft, authoritative);
        assertEquals(draft, BlueprintDocument.withoutMetadata(prepared.source));
        Node restored = first(BlueprintDocument.open(prepared.source).graph, Type.INPUT);
        assertEquals(input.id, restored.id);
        assertEquals(75, restored.x());
        String tail = "\nEVERY 20 TICKS DO END";
        String large = "--"
            + String.join("", java.util.Collections.nCopies(BlueprintDocument.MAX_BYTES - tail.length() - 2, "x"))
            + tail;
        assertEquals(BlueprintDocument.MAX_BYTES, large.getBytes(StandardCharsets.UTF_8).length);
        BlueprintDocument.TextSave dropped = BlueprintDocument.prepareTextSave(large, authoritative);
        assertEquals(large, dropped.source);
        assertFalse(dropped.notice.isEmpty());
    }

    @Test
    void pinnedLegacyLayoutStillOpensAndMigratesToV2WithoutChangingItsProgram() {
        BlueprintDocument legacy = BlueprintDocument.open(LEGACY);
        assertTrue(legacy.restoredLayout);
        assertEquals(new UUID(0, 1), first(legacy.graph, Type.TIMER).id);
        assertEquals(new UUID(0, 2), first(legacy.graph, Type.INPUT).id);
        assertEquals(42, first(legacy.graph, Type.INPUT).x());
        String migrated = legacy.saveEdited(legacy.graph);
        assertTrue(migrated.contains(BlueprintDocument.MARKER));
        assertEquals(BlueprintDocument.withoutMetadata(LEGACY), BlueprintDocument.withoutMetadata(migrated));
    }

    @Test
    void malformedSidecarsNeverReviveOldInstructions() throws Exception {
        BlueprintGraph graph = BlueprintCompiler.importText("EVERY 20 TICKS DO INPUT 4 FROM source END");
        String saved = BlueprintDocument.save(graph);
        byte[] raw = inflate(saved);
        List<String> broken = new ArrayList<>();
        byte[] hash = raw.clone();
        hash[36] ^= 1;
        broken.add(sidecar(hash));
        byte[] future = raw.clone();
        future[3] = 99;
        broken.add(sidecar(future));
        List<Node> nodes = new ArrayList<>(graph.nodes());
        byte[] duplicate = raw.clone();
        byte[] firstId = uuidBytes(nodes.get(0).id), secondId = uuidBytes(nodes.get(1).id);
        int secondPosition = indexOf(duplicate, secondId);
        assertTrue(secondPosition >= 0);
        System.arraycopy(firstId, 0, duplicate, secondPosition, firstId.length);
        broken.add(sidecar(duplicate));
        broken.add(sidecar(Arrays.copyOf(raw, raw.length + 1)));
        broken.add(
            BlueprintDocument.MARKER + Base64.getEncoder()
                .encodeToString(new byte[] { 1, 2, 3 }));
        broken.add(sidecar(new byte[262145]));
        broken.add(
            saved.substring(saved.indexOf(BlueprintDocument.MARKER)) + "\n"
                + saved.substring(saved.indexOf(BlueprintDocument.MARKER)));
        for (String metadata : broken) {
            BlueprintDocument current = BlueprintDocument
                .open("EVERY 20 TICKS DO INPUT 9 FROM source END\n" + metadata);
            assertFalse(current.restoredLayout);
            assertEquals(0, current.matchedNodes);
            assertTrue(
                BlueprintCompiler.generate(current.graph)
                    .contains("INPUT 9"));
            assertFalse(
                BlueprintCompiler.generate(current.graph)
                    .contains("INPUT 4"));
        }
    }

    @Test
    void nonConditionalElseFlagIsRejectedEvenWhenSourceAndGeneratedGraphHashesStillMatch() throws Exception {
        BlueprintGraph graph = BlueprintCompiler.importText("EVERY 20 TICKS DO INPUT 4 FROM source END");
        String saved = BlueprintDocument.save(graph);
        byte[] original = inflate(saved), malformed = original.clone();
        Node timer = first(graph, Type.TIMER), input = first(graph, Type.INPUT);
        int inputStart = indexOf(malformed, uuidBytes(input.id));
        assertTrue(inputStart > 0);
        int timerElseFlag = inputStart - 1;
        assertEquals(0, malformed[timerElseFlag]);
        malformed[timerElseFlag] = 1;
        assertArrayEquals(
            Arrays.copyOfRange(original, 4, 68),
            Arrays.copyOfRange(malformed, 4, 68),
            "Only the semantically invalid flag changes; both digest fields remain valid");
        String body = BlueprintDocument.withoutMetadata(saved);
        BlueprintDocument reopened = BlueprintDocument.open(body + sidecar(malformed));
        assertFalse(reopened.restoredLayout);
        assertEquals(0, reopened.matchedNodes);
        assertNotEquals(timer.id, first(reopened.graph, Type.TIMER).id);
        assertEquals(BlueprintCompiler.generate(graph), BlueprintCompiler.generate(reopened.graph));
        assertEquals(body, BlueprintDocument.withoutMetadata(body + sidecar(malformed)));
    }

    private static byte[] inflate(String source) throws Exception {
        byte[] compressed = Base64.getDecoder()
            .decode(source.substring(source.indexOf(BlueprintDocument.MARKER) + BlueprintDocument.MARKER.length()));
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        try (GZIPInputStream zip = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
            byte[] buffer = new byte[4096];
            int size;
            while ((size = zip.read(buffer)) != -1) raw.write(buffer, 0, size);
        }
        return raw.toByteArray();
    }

    private static String sidecar(byte[] raw) throws Exception {
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (GZIPOutputStream zip = new GZIPOutputStream(compressed)) {
            zip.write(raw);
        }
        return BlueprintDocument.MARKER + Base64.getEncoder()
            .encodeToString(compressed.toByteArray());
    }

    private static byte[] uuidBytes(UUID id) {
        return java.nio.ByteBuffer.allocate(16)
            .putLong(id.getMostSignificantBits())
            .putLong(id.getLeastSignificantBits())
            .array();
    }

    private static int indexOf(byte[] bytes, byte[] target) {
        for (int i = 0; i <= bytes.length - target.length; i++) {
            boolean equal = true;
            for (int j = 0; j < target.length; j++) if (bytes[i + j] != target[j]) {
                equal = false;
                break;
            }
            if (equal) return i;
        }
        return -1;
    }

    private static int occurrences(String value, String part) {
        int count = 0, position = 0;
        while ((position = value.indexOf(part, position)) >= 0) {
            count++;
            position += part.length();
        }
        return count;
    }
}
