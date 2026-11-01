package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioDocument;
import ca.teamdman.sfm.client.blueprint.StudioFieldDraft;
import ca.teamdman.sfm.client.blueprint.StudioFieldPicker;
import ca.teamdman.sfm.client.blueprint.StudioIoAllowances;
import ca.teamdman.sfm.client.blueprint.StudioIoAllowances.Part;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfm.client.blueprint.StudioOreFilter;
import ca.teamdman.sfml.ast.InputStatement;
import ca.teamdman.sfml.ast.Limit;
import ca.teamdman.sfml.ast.OutputStatement;
import ca.teamdman.sfml.ast.Statement;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class StudioIoAllowancesTest {

    private static final UUID PROJECT = new UUID(16, 1037);

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

    private static String apply(StudioFieldDraft draft, Node node) {
        return draft.appliedClause(PROJECT, node, draft.source);
    }

    private static Limit compiled(String clause) {
        ProgramBuildResult built = new ProgramBuilder(program(clause)).build();
        assertTrue(built.isSuccess(), built.getErrorMessage());
        Statement statement = built.getProgram()
            .getTriggers()
            .get(0)
            .getBlock()
            .getStatements()
            .get(0);
        return (statement instanceof InputStatement ? ((InputStatement) statement).getResourceLimits()
            : ((OutputStatement) statement).getResourceLimits()).getLimits()
                .get(0)
                .getLimit();
    }

    @Test
    void allPresenceAndEachCombinationsPreserveBothOrdersAndOutsideLexemes() {
        String resources = "\"item:箱, OR 🏭\" oR item:b wItH TAG#ingot*";
        String row = "0016 eAcH \tReTaIn 0002 eAcH " + resources;
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT }) for (boolean first : new boolean[] { false, true })
            for (int enabled = 0; enabled < 4; enabled++) for (int each = 0; each < 4; each++) {
                String access = (type == Type.INPUT ? "FROM" : "TO EMPTY SLOTS IN")
                    + " EACH \"Room 🏭\",\"other\" ROUND ROBIN BY BLOCK North,Top SIDE SLOTS 0,2-8";
                String action = (type == Type.INPUT ? "InPuT " : "OuTpUt ") + row + " eXcEpT item:dirt,fluid:lava";
                String clause = first ? access + "  " + action : action + "  " + access;
                Node node = node(type, clause);
                StudioFieldDraft draft = draft(node);
                assertTrue(draft.hasIoAllowances());
                draft.setAllowanceEnabled(Part.AMOUNT, (enabled & 1) != 0);
                draft.setAllowanceEnabled(Part.RETAIN, (enabled & 2) != 0);
                draft.setAllowanceEach(Part.AMOUNT, (each & 1) != 0);
                draft.setAllowanceEach(Part.RETAIN, (each & 2) != 0);
                String changed = apply(draft, node);
                Limit limit = compiled(changed);
                assertEquals(
                    (enabled & 1) != 0 ? 16 : Long.MAX_VALUE,
                    limit.getQuantity()
                        .getAmount());
                assertEquals(
                    (enabled & 2) != 0 ? 2 : type == Type.INPUT ? 0 : Long.MAX_VALUE,
                    limit.getRetention()
                        .getAmount());
                assertEquals(
                    (enabled & 1) != 0 && (each & 1) != 0,
                    limit.getQuantity()
                        .isEach());
                assertEquals(
                    (enabled & 2) != 0 && (each & 2) != 0,
                    limit.getRetention()
                        .isEach());
                assertTrue(changed.contains(resources));
                assertTrue(changed.contains(access));
                assertTrue(changed.contains(" eXcEpT item:dirt,fluid:lava"));
                if ((enabled & 2) != 0) assertTrue(changed.contains("ReTaIn 0002"));
                assertEquals(clause, node.clause());
            }
    }

    @Test
    void omittedDefaultsExplicitZeroAndMaxAreDifferentAndNeverMaterializedOnNoop() {
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT }) {
            Node node = node(
                type,
                (type == Type.INPUT ? "INPUT" : "OUTPUT") + " item:a "
                    + (type == Type.INPUT ? "FROM \"source\"" : "TO \"dest\""));
            StudioFieldDraft draft = draft(node);
            assertFalse(draft.allowanceEnabled(Part.AMOUNT));
            assertFalse(draft.allowanceEnabled(Part.RETAIN));
            assertEquals("", draft.allowanceText(Part.AMOUNT));
            assertEquals(node.clause(), apply(draft, node));
            assertEquals(
                Long.MAX_VALUE,
                compiled(node.clause()).getQuantity()
                    .getAmount());
            assertEquals(
                type == Type.INPUT ? 0 : Long.MAX_VALUE,
                compiled(node.clause()).getRetention()
                    .getAmount());
            for (String literal : new String[] { "0", Long.toString(Long.MAX_VALUE) }) {
                for (Part part : Part.values()) {
                    draft.setAllowanceEnabled(part, true);
                    draft.setAllowanceText(part, literal);
                }
                Limit limit = compiled(apply(draft, node));
                assertEquals(
                    Long.parseLong(literal),
                    limit.getQuantity()
                        .getAmount());
                assertEquals(
                    Long.parseLong(literal),
                    limit.getRetention()
                        .getAmount());
            }
        }
    }

    @Test
    void enabledBlankRefusesDisabledLatentStateSurvivesCopiesAndReenableRestoresExactSyntax() {
        Node node = node(Type.INPUT, "INPUT 0016 eAcH \tReTaIn 0002 eAcH item:a FROM \"source\"");
        StudioFieldDraft original = draft(node);
        for (Part part : Part.values()) original.setAllowanceEnabled(part, false);
        assertFalse(
            compiled(apply(original, node)).getQuantity()
                .isEach());
        StudioFieldDraft copy = new StudioFieldDraft(original);
        assertEquals("0016", copy.allowanceText(Part.AMOUNT));
        assertTrue(copy.allowanceEach(Part.RETAIN));
        copy.setAllowanceText(Part.AMOUNT, "\nnot valid");
        assertDoesNotThrow(() -> apply(copy, node));
        copy.setAllowanceEnabled(Part.AMOUNT, true);
        assertThrows(IllegalArgumentException.class, () -> apply(copy, node));
        assertEquals("\nnot valid", copy.allowanceText(Part.AMOUNT));
        copy.setAllowanceText(Part.AMOUNT, "0016");
        copy.setAllowanceEnabled(Part.RETAIN, true);
        assertFalse(copy.dirty());
        assertEquals(node.clause(), apply(copy, node));
        assertFalse(original.allowanceEnabled(Part.AMOUNT));
        Node absentNumber = node(Type.INPUT, "INPUT item:a FROM \"source\"");
        StudioFieldDraft blank = draft(absentNumber);
        blank.setAllowanceEnabled(Part.RETAIN, true);
        assertEquals("", blank.allowanceText(Part.RETAIN));
        assertThrows(IllegalArgumentException.class, () -> apply(blank, absentNumber));
        blank.setAllowanceEnabled(Part.RETAIN, false);
        assertFalse(blank.dirty());
    }

    @Test
    void invalidEnabledValuesRefuseApplyAndPromotionWithoutLosingPendingGlobalsOrIds() {
        for (Part part : Part.values())
            for (String bad : new String[] { "", "-1", "+1", "9223372036854775808", "0x10", "1 EACH", "1,2",
                "1 -- comment", "\n5", "5\r", "\0" + "5", String.join("", Collections.nCopies(1500, "(")) + "1" }) {
                    Node node = node(
                        Type.INPUT,
                        "INPUT 16 RETAIN 2 item:a WITH #ingot* EXCEPT item:dirt FROM \"source\"");
                    StudioFieldDraft draft = draft(node);
                    draft.setAllowanceText(part, bad);
                    draft.setLabel(
                        draft.labelRows()
                            .get(0).id,
                        "\"renamed\"");
                    UUID labels = draft.labelRevision(), resources = draft.resourceRevision(), label = draft.labelRows()
                        .get(0).id;
                    StudioFieldDraft copy = new StudioFieldDraft(draft);
                    assertThrows(IllegalArgumentException.class, () -> apply(copy, node), bad);
                    assertThrows(
                        IllegalArgumentException.class,
                        () -> copy.addIndependentLimit(PROJECT, node, copy.source),
                        bad);
                    assertEquals(bad, copy.allowanceText(part));
                    assertEquals(labels, copy.labelRevision());
                    assertEquals(resources, copy.resourceRevision());
                    assertEquals(
                        label,
                        copy.labelRows()
                            .get(0).id);
                    assertEquals(
                        "\"renamed\"",
                        copy.labelRows()
                            .get(0).text);
                    assertFalse(copy.hasLimitRows());
                    assertEquals(node.clause(), draft.original);
                }
    }

    @Test
    void removingOnlyNumberRestoresAbsentListAndOriginalModeSurvivesUntilApply() {
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT })
            for (String row : new String[] { "0016 EACH", "rEtAiN 0002 EACH", "16 EACH RETAIN 2 EACH" }) {
                Node node = node(
                    type,
                    (type == Type.INPUT ? "INPUT " : "OUTPUT ") + row
                        + " EXCEPT item:dirt "
                        + (type == Type.INPUT ? "FROM \"source\"" : "TO \"dest\""));
                StudioFieldDraft draft = draft(node);
                for (Part part : Part.values()) draft.setAllowanceEnabled(part, false);
                String changed = apply(draft, node);
                assertTrue(changed.contains("EXCEPT item:dirt"));
                assertFalse(
                    changed.toUpperCase(java.util.Locale.ROOT)
                        .contains("RETAIN"));
                assertEquals(
                    Long.MAX_VALUE,
                    compiled(changed).getQuantity()
                        .getAmount());
                assertEquals(
                    type == Type.INPUT ? 0 : Long.MAX_VALUE,
                    compiled(changed).getRetention()
                        .getAmount());
                assertTrue(draft.hasIoAllowances());
                assertThrows(
                    IllegalArgumentException.class,
                    () -> draft.addIndependentLimit(PROJECT, node, draft.source));
                StudioFieldDraft reopened = draft(node(type, changed));
                assertTrue(reopened.hasIoAllowances());
                assertFalse(reopened.canAddIndependentLimit());
            }
    }

    @Test
    void coupledNumberEditsComposeWithResourceRemovalOreAndGlobalChangesInEitherDirection() {
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT }) {
            String clause = (type == Type.INPUT ? "INPUT " : "OUTPUT ")
                + "16 RETAIN 2 item:a WITH #ingot* EXCEPT item:dirt "
                + (type == Type.INPUT ? "FROM" : "TO EMPTY SLOTS IN")
                + " EACH \"source\" ROUND ROBIN BY BLOCK North SIDE SLOTS 0-8";
            Node node = node(type, clause);
            StudioFieldDraft draft = draft(node);
            for (Part part : Part.values()) draft.setAllowanceEnabled(part, false);
            draft.removeResource(
                draft.resourceRows()
                    .get(0).id);
            draft.setOreMode(StudioOreFilter.Mode.OFF);
            draft.setLabel(
                draft.labelRows()
                    .get(0).id,
                "\"renamed 🏭\"");
            draft.set("sides", "SOUTH");
            draft.set("slots", "1-3");
            draft.set("each", "false");
            draft.set("roundRobin", "label");
            if (type == Type.OUTPUT) draft.set("emptySlots", "false");
            String removed = apply(draft, node);
            assertTrue(removed.contains("EXCEPT item:dirt"));
            assertTrue(removed.contains("\"renamed 🏭\""));
            assertTrue(removed.contains("SOUTH SIDE SLOTS 1-3"));
            assertTrue(removed.contains("ROUND ROBIN BY LABEL"));
            assertEquals(
                type == Type.INPUT ? 0 : Long.MAX_VALUE,
                compiled(removed).getRetention()
                    .getAmount());
            draft.setAllowanceEnabled(Part.AMOUNT, true);
            draft.setAllowanceText(Part.AMOUNT, "8");
            assertEquals(
                8,
                compiled(apply(draft, node)).getQuantity()
                    .getAmount());
            assertFalse(apply(draft, node).contains("item:a"));
        }
        Node selector = node(Type.INPUT, "INPUT item:a FROM \"source\"");
        StudioFieldDraft insert = draft(selector);
        insert.setAllowanceEnabled(Part.AMOUNT, true);
        insert.setAllowanceText(Part.AMOUNT, "7");
        insert.removeResource(
            insert.resourceRows()
                .get(0).id);
        assertEquals("7", StudioNodeProperties.singleLimitText(Type.INPUT, apply(insert, selector)));
    }

    @Test
    void legacyLiteralSettersRemainCompatibleAndNewModesCannotBypassAbsentOrManualEligibility() {
        Node node = node(Type.INPUT, "INPUT 0016 EACH RETAIN 0002 EACH item:a FROM \"source\"");
        StudioFieldDraft draft = draft(node);
        draft.set("amount", "8");
        draft.set("retain", "3");
        draft.set("amountEach", "false");
        draft.set("retainEach", "false");
        assertEquals("INPUT 8  RETAIN 3  item:a FROM \"source\"", apply(draft, node));
        for (String clause : new String[] { "INPUT item:a,item:b FROM \"source\"", "INPUT 4 item:a, FROM \"source\"",
            "INPUT 4 item:a OR FROM \"source\"",
            "INPUT " + String.join(" OR ", Collections.nCopies(33, "item:a")) + " FROM \"source\"",
            "INPUT (1+2) item:a FROM \"source\"" }) {
            Node manual = node(Type.INPUT, clause);
            StudioFieldDraft old = draft(manual);
            assertFalse(old.hasIoAllowances(), clause);
            assertThrows(IllegalArgumentException.class, () -> old.setAllowanceEnabled(Part.AMOUNT, true));
            StudioIoAllowances values = new StudioIoAllowances("item:a");
            values.setEnabled(Part.AMOUNT, true);
            values.setLiteral(Part.AMOUNT, "8");
            assertThrows(
                IllegalArgumentException.class,
                () -> StudioNodeProperties.updateSingleAllowances(Type.INPUT, clause, values));
            if (clause.startsWith("INPUT 4")) assertEquals(
                clause.replace("INPUT 4", "INPUT 8"),
                StudioNodeProperties.update(Type.INPUT, clause, "amount", "8"));
        }
        StudioFieldDraft initiallyOmitted = draft(node(Type.INPUT, "INPUT item:a FROM \"source\""));
        assertThrows(IllegalArgumentException.class, () -> initiallyOmitted.set("amount", "8"));
        assertThrows(IllegalArgumentException.class, () -> initiallyOmitted.set("amountEnabled", "true"));
    }

    @Test
    void promotionCapturesNewOptionalNumbersAndRetiresAllNumericApis() {
        Node node = node(Type.OUTPUT, "TO \"dest\" OUTPUT item:a");
        StudioFieldDraft draft = draft(node);
        draft.setAllowanceEnabled(Part.AMOUNT, true);
        draft.setAllowanceText(Part.AMOUNT, "0008");
        draft.setAllowanceEach(Part.AMOUNT, true);
        draft.setAllowanceEnabled(Part.RETAIN, true);
        draft.setAllowanceText(Part.RETAIN, "0002");
        StudioFieldDraft rows = draft.addIndependentLimit(PROJECT, node, draft.source);
        assertEquals(
            "0008 EACH RETAIN 0002 item:a",
            rows.limitRows()
                .get(0).text);
        assertFalse(rows.hasIoAllowances());
        for (Part part : Part.values()) {
            assertThrows(IllegalArgumentException.class, () -> rows.allowanceEnabled(part));
            assertThrows(IllegalArgumentException.class, () -> rows.allowanceText(part));
            assertThrows(IllegalArgumentException.class, () -> rows.allowanceEach(part));
            assertThrows(IllegalArgumentException.class, () -> rows.setAllowanceEnabled(part, true));
            assertThrows(IllegalArgumentException.class, () -> rows.setAllowanceText(part, "9"));
            assertThrows(IllegalArgumentException.class, () -> rows.setAllowanceEach(part, true));
        }
        for (String key : new String[] { "amountEnabled", "retainEnabled" }) {
            assertTrue(rows.retiredLimitField(key));
            assertThrows(IllegalArgumentException.class, () -> rows.value(key));
            assertThrows(IllegalArgumentException.class, () -> rows.set(key, "true"));
        }
        rows.setLimit(
            rows.limitRows()
                .get(0).id,
            "3 RETAIN 4 fluid:water");
        rows.setLimit(
            rows.limitRows()
                .get(1).id,
            "2 item:b");
        assertTrue(apply(rows, node).contains("OUTPUT 3 RETAIN 4 fluid:water, 2 item:b"));
        assertEquals("0008", draft.allowanceText(Part.AMOUNT));
    }

    @Test
    void numberEditsDoNotStaleResourcePickersAndCopiesKeepIndependentPendingModes() {
        Node node = node(Type.INPUT, "INPUT item:a FROM \"source\"");
        StudioFieldDraft draft = draft(node);
        StudioFieldPicker.Target target = StudioFieldPicker.resourceRowTarget(
            draft,
            draft.resourceRows()
                .get(0).id);
        StudioFieldPicker.Snapshot snapshot = StudioFieldPicker
            .items(Collections.singletonList("minecraft:coal"), target);
        StudioFieldPicker.Session picker = new StudioFieldPicker.Session(draft, target, snapshot);
        picker.select(snapshot.search("coal").entries.get(0));
        UUID revision = draft.resourceRevision();
        draft.setAllowanceEnabled(Part.AMOUNT, true);
        draft.setAllowanceText(Part.AMOUNT, "8");
        StudioFieldDraft copy = new StudioFieldDraft(draft);
        copy.setAllowanceText(Part.AMOUNT, "4");
        copy.setAllowanceEnabled(Part.RETAIN, true);
        assertEquals("8", draft.allowanceText(Part.AMOUNT));
        assertFalse(draft.allowanceEnabled(Part.RETAIN));
        assertThrows(IllegalArgumentException.class, () -> apply(copy, node));
        assertEquals(revision, draft.resourceRevision());
        picker.choose(draft, PROJECT, node, draft.source);
        assertTrue(apply(draft, node).contains("8 \"item:minecraft:coal\""));
    }

    @Test
    void sourceFencesAndOneAtomicUndoRedoKeepAllGraphAndBranchIdentities() {
        String source = "-- 🏭\r\nevery 20 ticks do if \"source\" HAS > 0 then INPUT item:a FROM \"source\" OUTPUT TO \"dest\" else forget end end";
        StudioDocument document = new StudioDocument();
        document.load(source);
        document.openVisual();
        Node node = document.graph()
            .nodes()
            .stream()
            .filter(n -> n.type == Type.INPUT)
            .findFirst()
            .get();
        List<String> ids = identities(document.graph());
        StudioFieldDraft draft = new StudioFieldDraft(
            PROJECT,
            node,
            document.code(),
            StudioNodeProperties.inspect(node.type, node.clause()));
        draft.setAllowanceEnabled(Part.AMOUNT, true);
        draft.setAllowanceText(Part.AMOUNT, "8");
        draft.setAllowanceEnabled(Part.RETAIN, true);
        draft.setAllowanceText(Part.RETAIN, "2");
        assertEquals(source, document.source());
        assertFalse(document.undo());
        assertThrows(
            IllegalArgumentException.class,
            () -> draft.appliedClause(UUID.randomUUID(), node, document.code()));
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, document.code() + " "));
        assertThrows(
            IllegalArgumentException.class,
            () -> draft.appliedClause(PROJECT, node(Type.INPUT, node.clause()), document.code()));
        String clause = draft.appliedClause(PROJECT, node, document.code());
        document.editGraph(
            g -> g.node(node.id)
                .setClause(clause));
        String after = document.source();
        assertEquals(ids, identities(document.graph()));
        assertTrue(document.undo());
        assertEquals(source, document.source());
        assertFalse(document.undo());
        assertTrue(document.redo());
        assertEquals(after, document.source());
        assertEquals(ids, identities(document.graph()));
    }

    private static List<String> identities(BlueprintGraph graph) {
        List<String> ids = new ArrayList<>();
        for (Node node : graph.nodes()) ids.add(node.id + ":" + node.type + ":" + node.hasElse);
        for (BlueprintGraph.Edge edge : graph.edges()) ids.add(edge.from + ":" + edge.pin + ":" + edge.to);
        return ids;
    }

    @Test
    void compactTokenBoundariesQuotedCommasAndUnicodeStayExactOutsideInsertedPrefix() {
        String row = "\"item:箱, OR 🏭\"";
        for (String clause : new String[] { "INPUT" + row + " FROM\"source\"", "FROM\"source\" INPUT" + row }) {
            Node node = node(Type.INPUT, clause);
            StudioFieldDraft draft = draft(node);
            draft.setAllowanceEnabled(Part.AMOUNT, true);
            draft.setAllowanceText(Part.AMOUNT, "0008");
            String changed = apply(draft, node);
            assertTrue(changed.contains(row));
            assertTrue(changed.contains("FROM\"source\""));
            assertEquals(
                8,
                compiled(changed).getQuantity()
                    .getAmount());
            draft.setAllowanceEnabled(Part.AMOUNT, false);
            assertTrue(draft.dirty()); // Disabled text remains latent control state until Apply/Cancel.
            assertEquals("0008", draft.allowanceText(Part.AMOUNT));
            assertEquals(clause, apply(draft, node));
        }
    }

    @Test
    void aggregateBoundsAndMalformedBooleanPendingValuesRefuseWithoutChangingSource() {
        Node node = node(Type.INPUT, "INPUT 16 EACH RETAIN 2 EACH item:a FROM \"source\"");
        StudioFieldDraft draft = draft(node);
        draft.set("amountEach", "not a boolean");
        assertThrows(IllegalArgumentException.class, () -> apply(draft, node));
        draft.set("amountEach", "true");
        String zeros = String.join("", Collections.nCopies(3000, "0"));
        for (Part part : Part.values()) draft.setAllowanceText(part, zeros);
        assertThrows(IllegalArgumentException.class, () -> apply(draft, node));
        assertEquals(zeros, draft.allowanceText(Part.AMOUNT));
        assertEquals(node.clause(), draft.original);
        assertThrows(IllegalArgumentException.class, () -> draft.setAllowanceText(Part.AMOUNT, zeros + zeros));
    }
}
