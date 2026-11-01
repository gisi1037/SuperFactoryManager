package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.client.blueprint.BlueprintDocument;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioDocument;
import ca.teamdman.sfm.client.blueprint.StudioFieldDraft;
import ca.teamdman.sfm.client.blueprint.StudioFieldPicker;
import ca.teamdman.sfm.client.blueprint.StudioFieldPicker.Session;
import ca.teamdman.sfm.client.blueprint.StudioFieldPicker.Snapshot;
import ca.teamdman.sfm.client.blueprint.StudioFieldPicker.Target;
import ca.teamdman.sfm.client.blueprint.StudioLabelRows;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfm.client.blueprint.StudioSourceMap;
import ca.teamdman.sfml.ast.InputStatement;
import ca.teamdman.sfml.ast.LabelAccess;
import ca.teamdman.sfml.ast.OutputStatement;
import ca.teamdman.sfml.ast.ResourceLimits;
import ca.teamdman.sfml.ast.Statement;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class StudioLabelRowsTest {

    @Test
    void invalidCellBoundariesCannotLookCleanJustBecauseJoinedTextMatchesTheOriginal() {
        String clause = "INPUT FROM \"a, b\", \"c\"";
        Node node = new Node(UUID.randomUUID(), Type.INPUT, 0, 0, clause);
        StudioFieldDraft draft = draft(node, program(clause));
        String initial = draft.value("label");
        draft.setLabel(
            draft.labelRows()
                .get(0).id,
            "\"a");
        draft.setLabel(
            draft.labelRows()
                .get(1).id,
            "b\", \"c\"");
        assertEquals(initial, draft.value("label"));
        assertTrue(draft.dirty());
        assertTrue(new StudioFieldDraft(draft).dirty());
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, program(clause)));
        assertTrue(draft.set("label", initial));
        assertFalse(draft.dirty());
        assertEquals(clause, node.clause());
    }

    @Test
    void pickerCanRepairBlankOrInvalidPendingRowWithoutApplyingMalformedNeighbors() {
        String clause = "INPUT FROM \"first\",\"second\"",
            source = program(clause) + " " + program("OUTPUT TO \"dest\"");
        Node node = new Node(UUID.randomUUID(), Type.INPUT, 0, 0, clause);
        for (String pending : new String[] { "", "\"unfinished", "a,b", "bad -- text" }) {
            StudioFieldDraft draft = draft(node, source);
            UUID first = draft.labelRows()
                .get(0).id,
                second = draft.labelRows()
                    .get(1).id;
            draft.setLabel(first, pending);
            draft.setLabel(second, "\"neighbor");
            Session repair = picker(draft, first, source, "dest");
            assertTrue(repair.choose(draft, PROJECT, node, source));
            assertEquals("\"dest\"", draft.labelValue(first));
            assertEquals("\"neighbor", draft.labelValue(second));
            assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, source));
            assertEquals(clause, node.clause());
            draft.setLabel(second, "\"second\"");
            assertEquals("INPUT FROM \"dest\",\"second\"", draft.appliedClause(PROJECT, node, source));
        }
    }

    private static final UUID PROJECT = new UUID(1, 55);

    private static String program(String clause) {
        return "EVERY 20 TICKS DO " + clause + " END";
    }

    private static StudioFieldDraft draft(Node node, String source) {
        return new StudioFieldDraft(PROJECT, node, source, StudioNodeProperties.inspect(node.type, node.clause()));
    }

    private static Statement compiled(String clause) {
        ProgramBuildResult built = new ProgramBuilder(program(clause)).build();
        assertTrue(built.isSuccess(), built.getErrorMessage());
        return built.getProgram()
            .getTriggers()
            .get(0)
            .getBlock()
            .getStatements()
            .get(0);
    }

    private static LabelAccess labels(Statement statement) {
        return statement instanceof InputStatement ? ((InputStatement) statement).getLabelAccess()
            : ((OutputStatement) statement).getLabelAccess();
    }

    private static ResourceLimits limits(Statement statement) {
        return statement instanceof InputStatement ? ((InputStatement) statement).getResourceLimits()
            : ((OutputStatement) statement).getResourceLimits();
    }

    private static List<String> names(Statement statement) {
        return labels(statement).getLabels()
            .stream()
            .map(l -> l.getName())
            .collect(Collectors.toList());
    }

    private static Session picker(StudioFieldDraft draft, UUID row, String source, String choice) {
        Target target = StudioFieldPicker.labelRowTarget(draft, row);
        assertNotNull(target);
        Snapshot snapshot = StudioFieldPicker.labels(source, target);
        Session session = new Session(draft, target, snapshot);
        session.select(
            snapshot.search(choice).entries.stream()
                .filter(c -> c.value.equals(choice))
                .findFirst()
                .get());
        return session;
    }

    @Test
    void editsRetainDuplicateCaseUnicodeQuotesAndEveryUnchangedLexemeAndDelimiter() {
        String source = "\"Case\",\t\"same\" ,  \"same\",\"Back\\\\path\", \"Escaped\\\"quote\", \"Room 🏭, 😀\"";
        StudioLabelRows rows = new StudioLabelRows(source);
        assertEquals(source, rows.source());
        assertEquals(6, rows.size());
        UUID first = rows.rows()
            .get(0).id,
            middle = rows.rows()
                .get(3).id,
            last = rows.rows()
                .get(5).id;
        rows.set(first, "\"Other\"");
        rows.set(middle, "\"New\\\\path\"");
        rows.set(last, "\"Last 😀\"");
        assertEquals(
            source.replace("Case", "Other")
                .replace("\"Back\\\\path\"", "\"New\\\\path\"")
                .replace("\"Room 🏭, 😀\"", "\"Last 😀\""),
            rows.source());
        assertEquals(
            rows.rows()
                .get(1).text,
            rows.rows()
                .get(2).text);
        assertNotEquals(
            rows.rows()
                .get(1).id,
            rows.rows()
                .get(2).id);
        assertThrows(
            UnsupportedOperationException.class,
            () -> rows.rows()
                .clear());
    }

    @Test
    void removalUsesFollowingDelimiterExceptFinalAndAppendAddsOnlyCommaSpace() {
        String source = "\"a\",\t\"B\" ,  \"C 😀\",\"D\"";
        String[] expected = { "\"B\" ,  \"C 😀\",\"D\"", "\"a\",\t\"C 😀\",\"D\"", "\"a\",\t\"B\" ,  \"D\"",
            "\"a\",\t\"B\" ,  \"C 😀\"" };
        for (int index = 0; index < 4; index++) {
            StudioLabelRows rows = new StudioLabelRows(source);
            UUID revision = rows.revision();
            rows.remove(
                rows.rows()
                    .get(index).id);
            assertEquals(expected[index], rows.source());
            assertNotEquals(revision, rows.revision());
            rows.append("\"new_label\"");
            assertEquals(expected[index] + ", \"new_label\"", rows.source());
        }
        StudioLabelRows single = new StudioLabelRows("\"onlyLabel\"");
        UUID id = single.rows()
            .get(0).id, revision = single.revision();
        assertThrows(IllegalArgumentException.class, () -> single.remove(id));
        assertEquals("\"onlyLabel\"", single.source());
        assertEquals(revision, single.revision());
    }

    @Test
    void exactlyThirtyTwoIsEditableAndThirtyThreeStaysWholeClauseWithoutTruncation() {
        String list = String.join(",", java.util.Collections.nCopies(32, "\"same\""));
        StudioLabelRows rows = new StudioLabelRows(list);
        UUID revision = rows.revision();
        assertThrows(IllegalArgumentException.class, () -> rows.append("extra"));
        assertEquals(list, rows.source());
        assertEquals(revision, rows.revision());
        assertFalse(
            StudioNodeProperties.inspect(Type.INPUT, "INPUT FROM " + list)
                .isEmpty());
        String larger = list + ",\"extra\"";
        assertTrue(
            StudioNodeProperties.inspect(Type.INPUT, "INPUT FROM " + larger)
                .isEmpty());
        assertThrows(IllegalArgumentException.class, () -> new StudioLabelRows(larger));
        assertEquals(33, names(compiled("INPUT FROM " + larger)).size());
        assertEquals(
            2,
            BlueprintCompiler.importText(program("INPUT FROM " + larger))
                .nodes()
                .size());
    }

    @Test
    void pendingInvalidRowsCannotInjectLabelsModifiersStatementsOrPartiallyApplyCombinedFields() {
        String clause = "INPUT 16 RETAIN 2 FROM \"first\",\"second\" North SIDE";
        Node node = new Node(UUID.randomUUID(), Type.INPUT, 0, 0, clause);
        for (String bad : new String[] { "", "a,b", "a North SIDE", "a -- hide", "\"unterminated",
            "a\nOUTPUT TO stolen", "a@" }) {
            StudioFieldDraft draft = draft(node, program(clause));
            UUID row = draft.labelRows()
                .get(0).id;
            draft.setLabel(row, bad);
            draft.set("amount", "32");
            assertThrows(
                IllegalArgumentException.class,
                () -> draft.appliedClause(PROJECT, node, program(clause)),
                bad);
            assertEquals(clause, node.clause());
            assertEquals(bad, draft.labelValue(row));
            assertEquals("32", draft.value("amount"));
        }
        StudioFieldDraft draft = draft(node, program(clause));
        draft.addLabel("\"third\"");
        draft.set("resource", "item:a OR item:b");
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, program(clause)));
        assertEquals(clause, node.clause());
        assertEquals(3, draft.labelCount());
    }

    @Test
    void rowSizeAndAppendFailuresAreAtomicAndInvalidTextCanBeRetainedForCorrection() {
        StudioLabelRows rows = new StudioLabelRows("\"first\",\"second\"");
        String before = rows.source();
        UUID revision = rows.revision();
        String huge = new String(new char[4097]).replace('\0', 'x');
        assertThrows(
            IllegalArgumentException.class,
            () -> rows.set(
                rows.rows()
                    .get(0).id,
                huge));
        assertThrows(IllegalArgumentException.class, () -> rows.append(huge));
        assertEquals(before, rows.source());
        assertEquals(revision, rows.revision());
        rows.set(
            rows.rows()
                .get(0).id,
            "\"pending");
        assertTrue(
            rows.source()
                .startsWith("\"pending,"));
        StudioLabelRows retained = new StudioLabelRows(rows);
        assertEquals(rows.source(), retained.source());
    }

    @Test
    void deepCopiesRetainRowIdentityRevisionAndOtherPendingFieldsWithoutSharingMutations() {
        Node node = new Node(Type.OUTPUT, 0, 0);
        node.setClause("OUTPUT 16 RETAIN 2 TO \"first\",\"second\"");
        StudioFieldDraft original = draft(node, program(node.clause()));
        original.setLabel(
            original.labelRows()
                .get(0).id,
            "\"unfinished");
        original.set("amount", "32");
        original.set("resource", "item:a");
        StudioFieldDraft retained = new StudioFieldDraft(original);
        assertEquals(original.labelRevision(), retained.labelRevision());
        assertEquals(
            original.labelRows()
                .get(0).id,
            retained.labelRows()
                .get(0).id);
        assertEquals(original.value("label"), retained.value("label"));
        retained.setLabel(
            retained.labelRows()
                .get(0).id,
            "\"fixed\"");
        retained.addLabel("\"third\"");
        assertEquals(2, original.labelCount());
        assertEquals(3, retained.labelCount());
        assertTrue(
            original.value("label")
                .startsWith("\"unfinished"));
        assertEquals("32", retained.value("amount"));
        assertEquals("item:a", retained.value("resource"));
        assertNotEquals(original.labelRevision(), retained.labelRevision());
    }

    @Test
    void duplicateShiftAndSameTextRemoveRecreateAbaInvalidateRowPickerBeforeMutation() {
        String clause = "INPUT FROM \"same\", \"same\", \"same\"";
        String source = program(clause) + "\n" + program("OUTPUT TO \"dest\"");
        Node node = new Node(UUID.randomUUID(), Type.INPUT, 0, 0, clause);
        StudioFieldDraft draft = draft(node, source);
        UUID row = draft.labelRows()
            .get(1).id;
        Session picker = picker(draft, row, source, "dest");
        draft.removeLabel(row);
        draft.addLabel("\"same\"");
        assertEquals("\"same\", \"same\", \"same\"", draft.value("label")); // Value-only ownership would incorrectly
                                                                            // pass.
        assertThrows(IllegalArgumentException.class, () -> picker.choose(draft, PROJECT, node, source));
        assertEquals("\"same\", \"same\", \"same\"", draft.value("label"));
        StudioFieldDraft shifted = draft(node, source);
        UUID target = shifted.labelRows()
            .get(1).id;
        Session old = picker(shifted, target, source, "dest");
        shifted.removeLabel(
            shifted.labelRows()
                .get(0).id);
        assertThrows(IllegalArgumentException.class, () -> old.choose(shifted, PROJECT, node, source));
        assertEquals("\"same\", \"same\"", shifted.value("label"));
    }

    @Test
    void rowPickerCancelAndChooseRetainOtherPendingRowsAndRejectStaleProjectSourceOrDraft() {
        String clause = "OUTPUT 16 TO \"first\",\"second\"";
        String source = program(clause) + "\n" + program("INPUT FROM \"dest\"");
        Node node = new Node(UUID.randomUUID(), Type.OUTPUT, 0, 0, clause);
        StudioFieldDraft draft = draft(node, source);
        draft.set("amount", "32");
        UUID row = draft.labelRows()
            .get(1).id;
        Session canceled = picker(draft, row, source, "dest");
        assertEquals("\"first\",\"second\"", draft.value("label"));
        assertEquals("32", draft.value("amount"));
        assertThrows(
            IllegalArgumentException.class,
            () -> canceled.choose(new StudioFieldDraft(draft), PROJECT, node, source));
        assertThrows(IllegalArgumentException.class, () -> canceled.choose(draft, UUID.randomUUID(), node, source));
        assertThrows(IllegalArgumentException.class, () -> canceled.choose(draft, PROJECT, node, source + " "));
        assertTrue(canceled.choose(draft, PROJECT, node, source));
        assertEquals("\"first\",\"dest\"", draft.value("label"));
        assertEquals("32", draft.value("amount"));
        assertEquals(clause, node.clause());
    }

    @Test
    void bothOrdersAndDirectionsRetainEveryOutsideByteAllowanceAndExactLegacyNameOrder() {
        String group = "\"Case\",\t\"same\" , \"same\",\"Escaped\\\"quote\",\"Back\\\\path\",\"Room 🏭, 😀\"";
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT })
            for (boolean sourceFirst : new boolean[] { false, true }) {
                String access = (type == Type.INPUT ? "FROM" : "TO EMPTY SLOTS IN") + " EACH "
                    + group
                    + " ROUND ROBIN BY LABEL North,Top SIDE SLOTS 0,2-8";
                String action = (type == Type.INPUT ? "InPuT" : "oUtPuT")
                    + " 64 EACH ReTaIn 7 EACH item:a WITHOUT #forge:bad EXCEPT item:b,fluid:water";
                String clause = sourceFirst ? access + "  " + action : action + "  " + access;
                Node node = new Node(UUID.randomUUID(), type, 0, 0, clause);
                StudioFieldDraft draft = draft(node, program(clause));
                draft.setLabel(
                    draft.labelRows()
                        .get(0).id,
                    "\"Other\"");
                draft.addLabel("\"last\"");
                String applied = draft.appliedClause(PROJECT, node, program(clause));
                assertEquals(clause.replace(group, group.replace("Case", "Other") + ", \"last\""), applied);
                Statement a = compiled(clause), b = compiled(applied);
                assertEquals(
                    Arrays.asList("Other", "same", "same", "Escaped\"quote", "Back\\path", "Room 🏭, 😀", "last"),
                    names(b));
                assertEquals(limits(a).toString(), limits(b).toString());
                assertEquals(
                    labels(a).getSides()
                        .toString(),
                    labels(b).getSides()
                        .toString());
                assertEquals(
                    labels(a).getSlots()
                        .toString(),
                    labels(b).getSlots()
                        .toString());
            }
    }

    @Test
    void compactFinalQuotedLabelChangesGetOnlyNecessaryBoundarySeparators() {
        for (String clause : new String[] { "FROM\"a\",\"b\"INPUT 16", "TO\"a\",\"b\"OUTPUT 16",
            "INPUT 16 FROM\"a\",\"b\"NORTH SIDE" }) {
            Type type = clause.contains("INPUT") ? Type.INPUT : Type.OUTPUT;
            Node node = new Node(UUID.randomUUID(), type, 0, 0, clause);
            StudioFieldDraft draft = draft(node, program(clause));
            draft.setLabel(
                draft.labelRows()
                    .get(1).id,
                "\"changed\"");
            String applied = draft.appliedClause(PROJECT, node, program(clause));
            assertEquals(clause.replace("\"b\"", "\"changed\""), applied);
            assertEquals(Arrays.asList("a", "changed"), names(compiled(applied)));
        }
    }

    @Test
    void oneAtomicCombinedApplyUndoRedoKeepsNodeUuidLayoutAndFreshNavigation() {
        String source = "-- 🏭\r\n" + program("INPUT 16 RETAIN 2 FROM \"first\",\"second\" OUTPUT TO \"dest\"");
        StudioDocument document = new StudioDocument();
        document.load(source);
        document.openVisual();
        Node node = document.graph()
            .nodes()
            .stream()
            .filter(n -> n.type == Type.INPUT)
            .findFirst()
            .get();
        StudioSourceMap old = StudioSourceMap
            .prepare(PROJECT, document, StudioSourceMap.normalize(document.code()), false, false, false);
        StudioFieldDraft draft = draft(node, document.code());
        draft.addLabel("\"third\"");
        draft.set("amount", "32");
        draft.set("resource", "item:a OR item:b");
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, document.code()));
        assertEquals(source, document.source());
        assertFalse(document.undo());
        draft.set("resource", "item:minecraft:iron_ingot");
        String applied = draft.appliedClause(PROJECT, node, document.code());
        document.editGraph(
            graph -> graph.node(node.id)
                .setClause(applied));
        assertFalse(
            old.matches(PROJECT, document.code(), StudioSourceMap.normalize(document.code()), document.graph()));
        StudioSourceMap fresh = StudioSourceMap
            .prepare(PROJECT, document, StudioSourceMap.normalize(document.code()), false, false, false);
        assertEquals(
            node.id,
            fresh.atCaret(
                document.code()
                    .indexOf("third")).node);
        assertNotNull(BlueprintDocument.open(document.source()).graph.node(node.id));
        assertEquals(Arrays.asList("first", "second", "third"), names(compiled(applied)));
        assertTrue(document.undo());
        assertEquals(source, document.source());
        assertNotNull(
            document.graph()
                .node(node.id));
        assertFalse(document.undo());
        assertTrue(document.redo());
        assertEquals(
            applied,
            document.graph()
                .node(node.id)
                .clause());
    }
}
