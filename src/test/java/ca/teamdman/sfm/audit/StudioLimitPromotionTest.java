package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioDocument;
import ca.teamdman.sfm.client.blueprint.StudioFieldDraft;
import ca.teamdman.sfm.client.blueprint.StudioFieldPicker;
import ca.teamdman.sfm.client.blueprint.StudioLimitRows;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfm.client.blueprint.StudioOreFilter;

class StudioLimitPromotionTest {

    private static final UUID PROJECT = new UUID(15, 1037);

    private static String program(String clause) {
        return "every 20 ticks do " + clause + " end";
    }

    private static Node node(Type type, String clause) {
        return new Node(UUID.randomUUID(), type, 0, 0, clause);
    }

    private static StudioFieldDraft draft(Node node) {
        return new StudioFieldDraft(
            PROJECT,
            node,
            program(node.clause()),
            StudioNodeProperties.inspect(node.type, node.clause()));
    }

    private static StudioFieldDraft promote(StudioFieldDraft draft, Node node) {
        return draft.addIndependentLimit(PROJECT, node, program(node.clause()));
    }

    private static String first(StudioFieldDraft draft) {
        return draft.limitRows()
            .get(0).text;
    }

    @Test
    void explicitConversionPreservesBothOrdersUnicodeQuotedCommasAndOmittedDefaults() {
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT }) for (boolean sourceFirst : new boolean[] { false,
            true })
            for (String limit : new String[] { "item:a", "0016 EACH RETAIN 0002 EACH item:a oR item:b wItH TAG#ingot*",
                "WITH #ingot*", "RETAIN 0002 EACH", "\"item:Room, OR 🏭\"" }) {
                    String access = (type == Type.INPUT ? "FROM" : "TO EMPTY SLOTS IN")
                        + " EACH \"箱 🏭\",other North SIDE SLOTS 0-8";
                    String action = (type == Type.INPUT ? "InPuT " : "OuTpUt ") + limit + " EXCEPT item:dirt";
                    String clause = sourceFirst ? access + "  " + action : action + "  " + access;
                    Node node = node(type, clause);
                    StudioFieldDraft original = draft(node);
                    assertTrue(original.canAddIndependentLimit(), clause);
                    UUID labels = original.labelRevision(), exclusions = original.exclusionRevision();
                    StudioFieldDraft converted = promote(original, node);
                    assertNotSame(original, converted);
                    assertEquals(limit, first(converted));
                    assertEquals(
                        "",
                        converted.limitRows()
                            .get(1).text);
                    assertTrue(converted.dirty());
                    assertFalse(converted.canAddIndependentLimit());
                    assertFalse(original.dirty());
                    assertTrue(original.canAddIndependentLimit());
                    assertEquals(labels, converted.labelRevision());
                    assertEquals(exclusions, converted.exclusionRevision());
                    assertEquals(
                        original.labelRows()
                            .get(0).id,
                        converted.labelRows()
                            .get(0).id);
                    assertEquals(clause, node.clause());
                    assertThrows(
                        IllegalArgumentException.class,
                        () -> converted.appliedClause(PROJECT, node, program(clause)));
                    converted.setLimit(
                        converted.limitRows()
                            .get(1).id,
                        "3 fluid:water");
                    assertEquals(
                        clause.replace(limit, limit + ", 3 fluid:water"),
                        converted.appliedClause(PROJECT, node, program(clause)));
                }
    }

    @Test
    void typedAllowanceRetentionOrOreAndGlobalEditsCarryIntoOneAtomicFinalClause() {
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT }) {
            String clause = (type == Type.INPUT ? "INPUT" : "OUTPUT")
                + " 0016 EACH RETAIN 0002 EACH item:a OR item:b wItH TAG#ingotIron EXCEPT item:dirt "
                + (type == Type.INPUT ? "FROM" : "TO EMPTY SLOTS IN")
                + " EACH \"source\",spare ROUND ROBIN BY BLOCK North SIDE SLOTS 0-8";
            Node node = node(type, clause);
            StudioFieldDraft original = draft(node);
            original.set("amount", "00020");
            original.set("retain", "0003");
            original.set("amountEach", "false");
            original.set("retainEach", "false");
            original.setResource(
                original.resourceRows()
                    .get(1).id,
                "item:c");
            original.setOreMode(StudioOreFilter.Mode.WITHOUT);
            original.setOrePattern("dust*");
            original.setLabel(
                original.labelRows()
                    .get(0).id,
                "\"renamed 🏭\"");
            original.setExclusion(
                original.exclusionRows()
                    .get(0).id,
                "fluid:lava");
            original.set("each", "false");
            original.set("roundRobin", "label");
            original.set("sides", "SOUTH");
            original.set("slots", "1-3");
            if (type == Type.OUTPUT) original.set("emptySlots", "false");
            String pending = original.appliedClause(PROJECT, node, program(clause));
            String limit = StudioNodeProperties.singleLimitText(type, pending);
            StudioFieldDraft converted = promote(original, node);
            assertEquals(limit, first(converted));
            assertEquals("00020", original.value("amount"));
            assertEquals(StudioOreFilter.Mode.WITHOUT, original.oreMode());
            converted.setLimit(
                converted.limitRows()
                    .get(1).id,
                "7 RETAIN 1 item:d");
            assertEquals(
                pending.replace(limit, limit + ", 7 RETAIN 1 item:d"),
                converted.appliedClause(PROJECT, node, program(clause)));
            assertEquals(clause, node.clause());
        }
    }

    @Test
    void invalidPendingCellsAndGlobalsRefuseConversionWithoutAnyDraftMutation() {
        String clause = "INPUT 16 RETAIN 2 item:a WITH #ingot* EXCEPT item:dirt FROM source";
        for (String key : new String[] { "amount", "retain", "label", "resource", "oreFilter", "exclusion", "sides",
            "slots" }) {
            Node node = node(Type.INPUT, clause);
            StudioFieldDraft original = draft(node);
            if (key.equals("oreFilter")) original.setOrePattern("bad?pattern");
            else if (key.equals("resource")) original.setResource(
                original.resourceRows()
                    .get(0).id,
                "");
            else if (key.equals("exclusion")) original.setExclusion(
                original.exclusionRows()
                    .get(0).id,
                "");
            else if (key.equals("label")) original.setLabel(
                original.labelRows()
                    .get(0).id,
                "a,b");
            else original.set(key, "not valid!");
            String before = snapshot(original);
            assertThrows(IllegalArgumentException.class, () -> promote(original, node), key);
            assertEquals(before, snapshot(original));
            assertFalse(original.hasLimitRows());
            assertTrue(original.canAddIndependentLimit());
            assertEquals(clause, node.clause());
        }
    }

    private static String snapshot(StudioFieldDraft draft) {
        return draft.original + draft.source
            + draft.dirty()
            + draft.labelRevision()
            + draft.resourceRevision()
            + draft.exclusionRevision()
            + draft.labelRows()
                .get(0).id
            + draft.resourceRows()
                .get(0).id
            + draft.exclusionRows()
                .get(0).id
            + draft.value("amount")
            + draft.value("retain")
            + draft.value("label")
            + draft.value("resource")
            + draft.value("exclusion")
            + draft.value("sides")
            + draft.value("slots")
            + draft.oreMode()
            + draft.orePattern();
    }

    @Test
    void originalProjectNodeClauseAndSourceFencesRemainAuthoritativeAfterConversion() {
        Node node = node(Type.INPUT, "INPUT item:a FROM source");
        StudioFieldDraft original = draft(node);
        assertThrows(
            IllegalArgumentException.class,
            () -> original.addIndependentLimit(UUID.randomUUID(), node, program(node.clause())));
        assertThrows(
            IllegalArgumentException.class,
            () -> original.addIndependentLimit(PROJECT, node(Type.INPUT, node.clause()), program(node.clause())));
        assertThrows(
            IllegalArgumentException.class,
            () -> original.addIndependentLimit(PROJECT, node, program(node.clause()) + " "));
        StudioFieldDraft converted = promote(original, node);
        converted.setLimit(
            converted.limitRows()
                .get(1).id,
            "2 item:b");
        assertTrue(converted.matches(PROJECT, node, program(node.clause())));
        assertThrows(
            IllegalArgumentException.class,
            () -> converted.appliedClause(UUID.randomUUID(), node, program(node.clause())));
        node.setClause("INPUT item:c FROM source");
        assertThrows(IllegalArgumentException.class, () -> converted.appliedClause(PROJECT, node, converted.source));
    }

    @Test
    void retiredControlsAndOldPickersCannotOverwriteRowsButGlobalPickersStillWork() {
        Node node = node(Type.INPUT, "INPUT 16 EACH RETAIN 2 EACH item:a WITH #ingot* EXCEPT item:dirt FROM source");
        StudioFieldDraft original = draft(node);
        StudioFieldPicker.Target target = StudioFieldPicker.resourceRowTarget(
            original,
            original.resourceRows()
                .get(0).id);
        StudioFieldPicker.Snapshot suggestions = StudioFieldPicker.items(Arrays.asList("minecraft:gold_ingot"), target);
        StudioFieldPicker.Session old = new StudioFieldPicker.Session(original, target, suggestions);
        old.select(suggestions.search("gold").entries.get(0));
        StudioFieldDraft converted = promote(original, node);
        for (String key : new String[] { "amount", "amountEach", "retain", "retainEach", "resource", "oreFilter" }) {
            assertTrue(converted.retiredLimitField(key));
            assertThrows(IllegalArgumentException.class, () -> converted.value(key));
            assertThrows(IllegalArgumentException.class, () -> converted.set(key, "1"));
        }
        assertFalse(converted.hasResourceRows());
        assertNull(
            StudioFieldPicker.resourceRowTarget(
                converted,
                original.resourceRows()
                    .get(0).id));
        assertThrows(
            IllegalArgumentException.class,
            () -> converted.setResource(
                original.resourceRows()
                    .get(0).id,
                "item:b"));
        assertThrows(IllegalArgumentException.class, () -> converted.addResource("item:b"));
        assertThrows(IllegalArgumentException.class, () -> converted.oreMode());
        assertThrows(IllegalArgumentException.class, () -> converted.setOrePattern("dust*"));
        assertThrows(IllegalArgumentException.class, () -> old.choose(converted, PROJECT, node, converted.source));
        StudioFieldPicker.Target label = StudioFieldPicker.labelRowTarget(
            converted,
            converted.labelRows()
                .get(0).id);
        StudioFieldPicker.Snapshot labels = StudioFieldPicker.labels(program(node.clause()), label);
        StudioFieldPicker.Session fresh = new StudioFieldPicker.Session(converted, label, labels);
        fresh.select(labels.search("source").entries.get(0));
        fresh.choose(converted, PROJECT, node, converted.source);
        converted.setExclusion(
            converted.exclusionRows()
                .get(0).id,
            "fluid:lava");
        converted.setLimit(
            converted.limitRows()
                .get(1).id,
            "2 item:b");
        assertTrue(
            converted.appliedClause(PROJECT, node, converted.source)
                .contains("EXCEPT fluid:lava"));
        assertEquals(
            "item:a",
            original.resourceRows()
                .get(0).text);
    }

    @Test
    void phaseCopiesRetainModeInvalidRowsIdsAndIndependentRecoveryEdits() {
        Node node = node(Type.OUTPUT, "OUTPUT 4 item:a TO dest");
        StudioFieldDraft original = draft(node), typedCopy = new StudioFieldDraft(original);
        typedCopy.set("amount", "8");
        StudioFieldDraft converted = promote(typedCopy, node);
        StudioFieldDraft recovered = new StudioFieldDraft(converted);
        assertTrue(recovered.hasLimitRows());
        assertEquals(converted.limitRevision(), recovered.limitRevision());
        assertEquals(
            converted.limitRows()
                .get(1).id,
            recovered.limitRows()
                .get(1).id);
        assertThrows(IllegalArgumentException.class, () -> recovered.appliedClause(PROJECT, node, recovered.source));
        recovered.setLimit(
            recovered.limitRows()
                .get(1).id,
            "2 item:b");
        assertEquals(
            "",
            converted.limitRows()
                .get(1).text);
        assertEquals("4", original.value("amount"));
        assertEquals("8", typedCopy.value("amount"));
        assertEquals("OUTPUT 8 item:a, 2 item:b TO dest", recovered.appliedClause(PROJECT, node, recovered.source));
        assertThrows(IllegalArgumentException.class, () -> recovered.value("amount"));
    }

    @Test
    void removingBlankReturnsExactSingleWithoutMaterializingDefaultsAndLastRemovalRefuses() {
        for (String limit : new String[] { "item:a", "0016 EACH item:a wItH TAG#ingot*" }) {
            Node node = node(Type.INPUT, "INPUT " + limit + " FROM source");
            StudioFieldDraft converted = promote(draft(node), node);
            converted.removeLimit(
                converted.limitRows()
                    .get(1).id);
            assertTrue(converted.hasLimitRows());
            assertThrows(
                IllegalArgumentException.class,
                () -> converted.removeLimit(
                    converted.limitRows()
                        .get(0).id));
            assertEquals(node.clause(), converted.appliedClause(PROJECT, node, converted.source));
            Node reopened = node(Type.INPUT, converted.appliedClause(PROJECT, node, converted.source));
            assertFalse(draft(reopened).hasLimitRows());
            assertTrue(draft(reopened).canAddIndependentLimit());
        }
    }

    @Test
    void conversionAndCancellationDoNotEnterHistoryFinalApplyIsExactlyOneUndoRedo() {
        String source = "-- 🏭\r\nevery 20 ticks do if source HAS > 0 then INPUT 16 item:a FROM source OUTPUT TO dest else forget end end";
        StudioDocument document = new StudioDocument();
        document.load(source);
        document.openVisual();
        Node node = document.graph()
            .nodes()
            .stream()
            .filter(n -> n.type == Type.INPUT)
            .findFirst()
            .get();
        List<String> identities = identities(document.graph());
        StudioFieldDraft original = new StudioFieldDraft(
            PROJECT,
            node,
            document.code(),
            StudioNodeProperties.inspect(node.type, node.clause()));
        original.set("amount", "8");
        StudioFieldDraft cancelled = original.addIndependentLimit(PROJECT, node, document.code());
        assertEquals(source, document.source());
        assertFalse(document.undo());
        StudioFieldDraft converted = new StudioFieldDraft(cancelled);
        converted.setLimit(
            converted.limitRows()
                .get(1).id,
            "2 item:b");
        String clause = converted.appliedClause(PROJECT, node, document.code());
        document.editGraph(
            g -> g.node(node.id)
                .setClause(clause));
        assertEquals(identities, identities(document.graph()));
        String applied = document.source();
        assertTrue(document.undo());
        assertEquals(source, document.source());
        assertFalse(document.undo());
        assertTrue(document.redo());
        assertEquals(applied, document.source());
        assertEquals(identities, identities(document.graph()));
    }

    private static List<String> identities(BlueprintGraph graph) {
        List<String> result = graph.nodes()
            .stream()
            .map(n -> n.id + ":" + n.type + ":" + n.hasElse)
            .collect(Collectors.toList());
        for (BlueprintGraph.Edge edge : graph.edges()) result.add(edge.from + ":" + edge.pin + ":" + edge.to);
        return result;
    }

    @Test
    void absentMultipleTrailingCommaAndUnsupportedFormsNeverImplicitlyPromote() {
        for (String clause : new String[] { "INPUT FROM source", "INPUT item:a,item:b FROM source",
            "INPUT item:a, FROM source", "INPUT item:a OR FROM source",
            "INPUT " + String.join(" OR ", Collections.nCopies(33, "item:a")) + " FROM source" }) {
            Node node = node(Type.INPUT, clause);
            StudioFieldDraft original = draft(node);
            assertFalse(original.canAddIndependentLimit(), clause);
            assertThrows(IllegalArgumentException.class, () -> promote(original, node));
        }
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties
                .updateIndependentLimits(Type.INPUT, "INPUT item:a FROM source", "item:a,item:b"));
        assertEquals(
            "INPUT item:a,item:b FROM source",
            StudioNodeProperties.promoteSingleLimit(Type.INPUT, "INPUT item:a FROM source", "item:a,item:b"));
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties.promoteSingleLimit(Type.INPUT, "INPUT FROM source", "item:a,item:b"));
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties
                .promoteSingleLimit(Type.INPUT, "INPUT item:a,item:b FROM source", "item:c,item:d"));
    }

    @Test
    void pendingEditsThatRemoveExplicitLimitRefuseWithoutLosingTypedDraft() {
        Node node = node(Type.INPUT, "INPUT item:a FROM source");
        StudioFieldDraft original = draft(node);
        original.removeResource(
            original.resourceRows()
                .get(0).id);
        assertEquals("INPUT  FROM source", original.appliedClause(PROJECT, node, original.source));
        UUID revision = original.resourceRevision();
        assertThrows(IllegalArgumentException.class, () -> promote(original, node));
        assertEquals(0, original.resourceCount());
        assertEquals(revision, original.resourceRevision());
        assertTrue(original.canAddIndependentLimit());
    }

    @Test
    void deepPendingOrInjectedSyntaxIsGuardedBeforeConversionAndQuotedTextRemainsLiteral() {
        Node node = node(Type.INPUT, "INPUT 16 item:a WITH #ingot* FROM source");
        String left = String.join("", Collections.nCopies(1500, "("));
        String right = String.join("", Collections.nCopies(1500, ")"));
        for (String pending : new String[] { left + "1" + right,
            "WITH " + String.join("", Collections.nCopies(700, "NOT ")) + "#a",
            "WITH #a" + String.join("", Collections.nCopies(500, " OR #a")), "item:a -- comment" }) {
            StudioFieldDraft original = draft(node);
            original.setResource(
                original.resourceRows()
                    .get(0).id,
                pending);
            UUID revision = original.resourceRevision();
            assertThrows(IllegalArgumentException.class, () -> promote(original, node));
            assertEquals(
                pending,
                original.resourceRows()
                    .get(0).text);
            assertEquals(revision, original.resourceRevision());
            assertFalse(original.hasLimitRows());
        }
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties
                .singleLimitText(Type.INPUT, "INPUT " + left + "1" + right + " item:a FROM source"));
        String quoted = "\"item:" + left + right + "\"";
        Node literal = node(Type.INPUT, "INPUT " + quoted + " FROM source");
        assertEquals(quoted, first(promote(draft(literal), literal)));
    }

    @Test
    void promotedRowsRetainCapsRemovedIdsPartitionsAndFinalValidation() {
        Node node = node(Type.INPUT, "INPUT item:a FROM source");
        StudioFieldDraft converted = promote(draft(node), node);
        UUID blank = converted.limitRows()
            .get(1).id;
        converted.removeLimit(blank);
        assertThrows(IllegalArgumentException.class, () -> converted.setLimit(blank, "2 item:b"));
        for (int i = 1; i < StudioLimitRows.MAX_ROWS; i++) converted.addLimit("2 item:b");
        assertThrows(IllegalArgumentException.class, () -> converted.addLimit("3 item:c"));
        assertEquals(32, converted.limitCount());
        assertTrue(
            converted.appliedClause(PROJECT, node, converted.source)
                .contains("item:a, 2 item:b"));
        converted.setLimit(
            converted.limitRows()
                .get(1).id,
            "item:a,item:b");
        assertThrows(IllegalArgumentException.class, () -> converted.appliedClause(PROJECT, node, converted.source));
    }
}
