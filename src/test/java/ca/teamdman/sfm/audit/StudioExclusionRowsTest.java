package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.client.blueprint.BlueprintDocument;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioDocument;
import ca.teamdman.sfm.client.blueprint.StudioExclusionRows;
import ca.teamdman.sfm.client.blueprint.StudioFieldDraft;
import ca.teamdman.sfm.client.blueprint.StudioFieldPicker;
import ca.teamdman.sfm.client.blueprint.StudioFieldPicker.Session;
import ca.teamdman.sfm.client.blueprint.StudioFieldPicker.Target;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfm.client.blueprint.StudioOreFilter;
import ca.teamdman.sfml.ast.InputStatement;
import ca.teamdman.sfml.ast.OutputStatement;
import ca.teamdman.sfml.ast.ResourceLimits;
import ca.teamdman.sfml.ast.Statement;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class StudioExclusionRowsTest {

    private static final UUID PROJECT = new UUID(9, 1037);

    private static String program(String clause) {
        return "every 20 ticks do " + clause + " end";
    }

    private static StudioFieldDraft draft(Node node, String source) {
        return new StudioFieldDraft(PROJECT, node, source, StudioNodeProperties.inspect(node.type, node.clause()));
    }

    private static ResourceLimits limits(String clause) {
        ProgramBuildResult built = new ProgramBuilder(program(clause)).build();
        assertTrue(built.isSuccess(), built.getErrorMessage());
        Statement statement = built.getProgram()
            .getTriggers()
            .get(0)
            .getBlock()
            .getStatements()
            .get(0);
        return statement instanceof InputStatement ? ((InputStatement) statement).getResourceLimits()
            : ((OutputStatement) statement).getResourceLimits();
    }

    private static Session picker(StudioFieldDraft draft, UUID row) {
        Target target = StudioFieldPicker.exclusionRowTarget(draft, row);
        assertNotNull(target);
        StudioFieldPicker.Snapshot snapshot = StudioFieldPicker.items(Arrays.asList("minecraft:gold_ingot"), target);
        Session session = new Session(draft, target, snapshot);
        session.select(snapshot.search("gold").entries.get(0));
        return session;
    }

    @Test
    void orderedRowsPreserveDuplicateCaseUnicodeQuotedCommasAndEveryUnchangedSeparator() {
        String text = "item:Case,\t\"item:Room 🏭, 😀\" ,  item:Case,fluid:water";
        StudioExclusionRows rows = new StudioExclusionRows(text);
        assertEquals(text, rows.source());
        assertEquals(4, rows.size());
        assertNotEquals(
            rows.rows()
                .get(0).id,
            rows.rows()
                .get(2).id);
        rows.set(
            rows.rows()
                .get(1).id,
            "\"item:New 🏭, 😀\"");
        assertEquals(text.replace("Room", "New"), rows.source());
        StudioExclusionRows copy = new StudioExclusionRows(rows);
        assertEquals(rows.revision(), copy.revision());
        assertEquals(
            rows.rows()
                .get(1).id,
            copy.rows()
                .get(1).id);
        assertThrows(
            UnsupportedOperationException.class,
            () -> copy.rows()
                .clear());
        copy.set(
            copy.rows()
                .get(0).id,
            "item:Other");
        assertEquals(
            "item:Case",
            rows.rows()
                .get(0).text);
    }

    @Test
    void removalUsesFollowingDelimiterOrPrecedingFinalAndZeroIsExplicit() {
        String text = "item:a,\titem:B ,  \"item:C 😀\",item:D";
        String[] expected = { "item:B ,  \"item:C 😀\",item:D", "item:a,\t\"item:C 😀\",item:D",
            "item:a,\titem:B ,  item:D", "item:a,\titem:B ,  \"item:C 😀\"" };
        for (int i = 0; i < 4; i++) {
            StudioExclusionRows rows = new StudioExclusionRows(text);
            UUID revision = rows.revision();
            rows.remove(
                rows.rows()
                    .get(i).id);
            assertEquals(expected[i], rows.source());
            assertNotEquals(revision, rows.revision());
            rows.append("item:new");
            assertEquals(expected[i] + ", item:new", rows.source());
        }
        StudioExclusionRows rows = new StudioExclusionRows("");
        UUID id = rows.append("");
        assertEquals(1, rows.size());
        assertThrows(IllegalArgumentException.class, () -> StudioExclusionRows.validateLiteral(rows.value(id)));
        rows.remove(id);
        assertEquals(0, rows.size());
        assertEquals("", rows.source());
    }

    @Test
    void bothOrdersKeepKeywordSpansDefaultsEachOreLabelsRoutingAndComplexInclusionLimits() {
        String list = "\"item:Old 🏭, 😀\",\titem:Case , item:Case";
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT }) for (boolean first : new boolean[] { false, true })
            for (String inclusion : new String[] { "", "16 EACH RETAIN 2 EACH item:a WITHOUT #dust*",
                "16 item:a,2 item:b", "item:a OR item:b", "16 item:a," }) {
                    String action = (type == Type.INPUT ? "InPuT" : "oUtPuT")
                        + (inclusion.isEmpty() ? "" : " " + inclusion)
                        + " eXcEpT\t"
                        + list;
                    String access = (type == Type.INPUT ? "FROM" : "TO EMPTY SLOTS IN")
                        + " EACH \"Case\",\"Case\" ROUND ROBIN BY LABEL North SIDE SLOTS 0-8";
                    String clause = first ? access + "  " + action : action + "  " + access;
                    Node node = new Node(UUID.randomUUID(), type, 0, 0, clause);
                    StudioFieldDraft draft = draft(node, program(clause));
                    assertEquals(3, draft.exclusionCount());
                    draft.setExclusion(
                        draft.exclusionRows()
                            .get(1).id,
                        "item:New");
                    String changed = draft.appliedClause(PROJECT, node, program(clause));
                    assertEquals(clause.replace(list, list.replace("\titem:Case", "\titem:New")), changed);
                    assertEquals(
                        limits(clause).getLimits()
                            .toString(),
                        limits(changed).getLimits()
                            .toString());
                    assertEquals(
                        3,
                        limits(changed).getExclusions()
                            .getIdentifiers()
                            .size());
                    for (StudioExclusionRows.Row row : draft.exclusionRows()) draft.removeExclusion(row.id);
                    String removed = draft.appliedClause(PROJECT, node, program(clause));
                    assertEquals(clause.replace("eXcEpT\t" + list, ""), removed);
                    assertNull(limits(removed).getExclusions());
                    assertEquals(
                        limits(clause).getLimits()
                            .toString(),
                        limits(removed).getLimits()
                            .toString());
                }
    }

    @Test
    void absentAndCompactQuotedBoundariesAddOnlyRequiredSpacesAndPreserveDirectionalDefaults() {
        for (String clause : new String[] { "INPUT FROM\"src😀\"", "FROM\"src😀\"INPUT", "OUTPUT TO\"dest😀\"",
            "TO\"dest😀\"OUTPUT" }) {
            Type type = clause.contains("INPUT") ? Type.INPUT : Type.OUTPUT;
            Node node = new Node(UUID.randomUUID(), type, 0, 0, clause);
            StudioFieldDraft draft = draft(node, program(clause));
            assertEquals(0, draft.exclusionCount());
            UUID first = draft.addExclusion("\"item:a\"");
            draft.addExclusion("fluid:water");
            String changed = draft.appliedClause(PROJECT, node, program(clause));
            assertEquals(
                limits(clause).getLimits()
                    .toString(),
                limits(changed).getLimits()
                    .toString());
            draft.removeExclusion(first);
            draft.removeExclusion(
                draft.exclusionRows()
                    .get(0).id);
            assertFalse(draft.dirty());
            assertEquals(clause, draft.appliedClause(PROJECT, node, program(clause)));
        }
        String compact = "INPUT\"item:a\"EXCEPT\"item:b\",\"item:c\"FROM\"src😀\"";
        String changed = StudioNodeProperties.updateExclusions(Type.INPUT, compact, "item:d,item:e");
        assertEquals("INPUT\"item:a\"EXCEPT item:d,item:e FROM\"src😀\"", changed);
        assertEquals("INPUT\"item:a\"FROM\"src😀\"", StudioNodeProperties.updateExclusions(Type.INPUT, compact, ""));
    }

    @Test
    void blankInvalidRowsAndBoundarySpoofingRemainDirtyAcrossCopiesAndRefuseAtomically() {
        String clause = "INPUT 16 EXCEPT \"item:a, b\", item:c FROM \"source\"", source = program(clause);
        Node node = new Node(UUID.randomUUID(), Type.INPUT, 0, 0, clause);
        StudioFieldDraft draft = draft(node, source);
        String original = draft.value("exclusion");
        draft.setExclusion(
            draft.exclusionRows()
                .get(0).id,
            "\"item:a");
        draft.setExclusion(
            draft.exclusionRows()
                .get(1).id,
            "b\", item:c");
        assertEquals(original, draft.value("exclusion"));
        assertTrue(draft.dirty());
        StudioFieldDraft copy = new StudioFieldDraft(draft);
        assertTrue(copy.dirty());
        assertThrows(IllegalArgumentException.class, () -> copy.appliedClause(PROJECT, node, source));
        assertTrue(copy.set("exclusion", original));
        assertFalse(copy.dirty());
        for (String bad : new String[] { "", " ", "item:a,item:b", "item:a,", "item:a OR item:b", "item:a -- hide",
            "item:a WITH #dust*", "EXCEPT item:a", "bad?", "bad.path", "a\nb", "a\rb", "a\0b", "\"unfinished" }) {
            StudioFieldDraft pending = draft(node, source);
            UUID row = pending.exclusionRows()
                .get(1).id;
            pending.setExclusion(row, bad);
            pending.set("amount", "32");
            StudioFieldDraft retained = new StudioFieldDraft(pending);
            assertEquals(bad, retained.exclusionValue(row));
            assertEquals(pending.exclusionRevision(), retained.exclusionRevision());
            assertThrows(IllegalArgumentException.class, () -> retained.appliedClause(PROJECT, node, source), bad);
            assertEquals(clause, node.clause());
            assertEquals("32", retained.value("amount"));
        }
    }

    @Test
    void pickerRepairsOnlyItsBlankOrInvalidRowAndNeverAppliesMalformedNeighbors() {
        String clause = "OUTPUT 16 EXCEPT item:a,item:b TO \"dest\"", source = program(clause);
        Node node = new Node(UUID.randomUUID(), Type.OUTPUT, 0, 0, clause);
        for (String pending : new String[] { "", "item:x,item:y", "\"unfinished" }) {
            StudioFieldDraft draft = draft(node, source);
            UUID row = draft.exclusionRows()
                .get(0).id,
                neighbor = draft.exclusionRows()
                    .get(1).id;
            draft.setExclusion(row, pending);
            draft.setExclusion(neighbor, "");
            draft.set("amount", "32");
            Session picker = picker(draft, row);
            assertEquals(pending, draft.exclusionValue(row)); // Selecting/canceling is modal-local.
            assertTrue(picker.choose(draft, PROJECT, node, source));
            assertEquals("\"item:minecraft:gold_ingot\"", draft.exclusionValue(row));
            assertEquals("", draft.exclusionValue(neighbor));
            assertEquals("32", draft.value("amount"));
            assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, source));
            draft.removeExclusion(neighbor);
            assertEquals(
                "OUTPUT 32 EXCEPT \"item:minecraft:gold_ingot\" TO \"dest\"",
                draft.appliedClause(PROJECT, node, source));
            assertEquals(clause, node.clause());
        }
    }

    @Test
    void pickerRejectsShiftedRemovedRecreatedAbaAndStaleOwnersWithoutChangingPendingRows() {
        String clause = "INPUT EXCEPT item:a, item:a, item:a FROM \"source\"", source = program(clause);
        Node node = new Node(UUID.randomUUID(), Type.INPUT, 0, 0, clause);
        for (boolean aba : new boolean[] { false, true }) {
            StudioFieldDraft draft = draft(node, source);
            UUID row = draft.exclusionRows()
                .get(1).id;
            Session picker = picker(draft, row);
            draft.removeExclusion(
                aba ? row
                    : draft.exclusionRows()
                        .get(0).id);
            if (aba) draft.addExclusion("item:a");
            String pending = draft.value("exclusion");
            assertThrows(IllegalArgumentException.class, () -> picker.choose(draft, PROJECT, node, source));
            assertEquals(pending, draft.value("exclusion"));
        }
        StudioFieldDraft draft = draft(node, source);
        UUID row = draft.exclusionRows()
            .get(0).id;
        Session picker = picker(draft, row);
        assertThrows(
            IllegalArgumentException.class,
            () -> picker.choose(new StudioFieldDraft(draft), PROJECT, node, source));
        assertThrows(IllegalArgumentException.class, () -> picker.choose(draft, UUID.randomUUID(), node, source));
        assertThrows(IllegalArgumentException.class, () -> picker.choose(draft, PROJECT, node, source + " "));
        assertThrows(
            IllegalArgumentException.class,
            () -> picker.choose(draft, PROJECT, new Node(UUID.randomUUID(), Type.INPUT, 0, 0, clause), source));
        draft.setExclusion(row, "item:changed");
        assertThrows(IllegalArgumentException.class, () -> picker.choose(draft, PROJECT, node, source));
        assertEquals("item:changed", draft.exclusionValue(row));
    }

    @Test
    void capThirtyTwoIsEditableAndTrailingCommaOrLargerListsRemainEntirelyManual() {
        String list = String.join(",", Collections.nCopies(32, "item:same"));
        StudioExclusionRows rows = new StudioExclusionRows(list);
        UUID revision = rows.revision();
        assertThrows(IllegalArgumentException.class, () -> rows.append("extra"));
        assertEquals(revision, rows.revision());
        assertEquals(list, rows.source());
        String clause = "INPUT EXCEPT " + list + " FROM \"source\"";
        assertTrue(draft(new Node(UUID.randomUUID(), Type.INPUT, 0, 0, clause), program(clause)).hasExclusionRows());
        for (String manual : new String[] { list + ",extra", list + ",", "item:a," }) {
            String full = "INPUT 16 EXCEPT " + manual + " FROM \"source\"";
            StudioFieldDraft draft = draft(new Node(UUID.randomUUID(), Type.INPUT, 0, 0, full), program(full));
            assertFalse(draft.hasExclusionRows());
            assertThrows(IllegalArgumentException.class, () -> new StudioExclusionRows(manual));
            assertEquals(full.replace("16", "32"), StudioNodeProperties.update(Type.INPUT, full, "amount", "32"));
            assertNotNull(BlueprintCompiler.importText(program(full)));
            assertEquals(full, new Node(UUID.randomUUID(), Type.INPUT, 0, 0, full).clause());
        }
    }

    @Test
    void combinedRowsOreResourceLabelsAndQuantityCommitOnceWithExactSourceUuidUndoRedo() {
        String source = "-- 🏭\r\n"
            + program("INPUT 16 RETAIN 2 EXCEPT item:a,item:b FROM \"a\",\"b\" OUTPUT TO \"dest\"");
        StudioDocument document = new StudioDocument();
        document.load(source);
        document.openVisual();
        Node node = document.graph()
            .nodes()
            .stream()
            .filter(n -> n.type == Type.INPUT)
            .findFirst()
            .get();
        StudioFieldDraft draft = draft(node, document.code());
        UUID row = draft.exclusionRows()
            .get(1).id;
        draft.setExclusion(row, "");
        draft.setLabel(
            draft.labelRows()
                .get(0).id,
            "\"changed\"");
        draft.set("amount", "32");
        draft.set("resource", "item:minecraft:iron_ingot");
        draft.setOreMode(StudioOreFilter.Mode.WITHOUT);
        draft.setOrePattern("dust*");
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, document.code()));
        assertEquals(source, document.source());
        assertFalse(document.undo());
        StudioFieldDraft copy = new StudioFieldDraft(draft);
        assertEquals(
            row,
            copy.exclusionRows()
                .get(1).id);
        copy.setExclusion(row, "fluid:water");
        String applied = copy.appliedClause(PROJECT, node, document.code());
        assertEquals(
            "INPUT 32 RETAIN 2 item:minecraft:iron_ingot WITHOUT #dust*   EXCEPT item : a , fluid:water FROM \"changed\" , \"b\"",
            applied);
        document.editGraph(
            g -> g.node(node.id)
                .setClause(applied));
        assertNotNull(BlueprintDocument.open(document.source()).graph.node(node.id));
        assertEquals(
            applied,
            document.graph()
                .node(node.id)
                .clause());
        assertTrue(document.undo());
        assertEquals(source, document.source());
        assertFalse(document.undo());
        assertTrue(document.redo());
        assertEquals(
            applied,
            document.graph()
                .node(node.id)
                .clause());
    }

    @Test
    void blankNewRowIsDirtyDespiteEmptyJoinedSourceAndExplicitRemovalRestoresNoOp() {
        String clause = "OUTPUT TO \"dest\"", source = program(clause);
        Node node = new Node(UUID.randomUUID(), Type.OUTPUT, 0, 0, clause);
        StudioFieldDraft draft = draft(node, source);
        UUID row = draft.addExclusion("");
        assertEquals("", draft.value("exclusion"));
        assertTrue(draft.dirty());
        assertTrue(new StudioFieldDraft(draft).dirty());
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, source));
        draft.removeExclusion(row);
        assertFalse(draft.dirty());
        assertEquals(clause, draft.appliedClause(PROJECT, node, source));
    }
}
