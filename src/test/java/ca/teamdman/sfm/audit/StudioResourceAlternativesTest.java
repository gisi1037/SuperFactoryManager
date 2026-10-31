package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintDocument;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioDocument;
import ca.teamdman.sfm.client.blueprint.StudioFieldDraft;
import ca.teamdman.sfm.client.blueprint.StudioFieldPicker;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfm.client.blueprint.StudioOreFilter;
import ca.teamdman.sfm.client.blueprint.StudioResourceAlternatives;
import ca.teamdman.sfml.ast.InputStatement;
import ca.teamdman.sfml.ast.OutputStatement;
import ca.teamdman.sfml.ast.ResourceLimit;
import ca.teamdman.sfml.ast.ResourceLimits;
import ca.teamdman.sfml.ast.Statement;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class StudioResourceAlternativesTest {

    private static final UUID PROJECT = new UUID(11, 1037);

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

    private static void commonLimit(ResourceLimits a, ResourceLimits b) {
        assertEquals(
            1,
            a.getLimits()
                .size());
        assertEquals(
            1,
            b.getLimits()
                .size());
        ResourceLimit x = a.getLimits()
            .get(0),
            y = b.getLimits()
                .get(0);
        assertEquals(
            x.getLimit()
                .getQuantity()
                .getAmount(),
            y.getLimit()
                .getQuantity()
                .getAmount());
        assertEquals(
            x.getLimit()
                .getQuantity()
                .isEach(),
            y.getLimit()
                .getQuantity()
                .isEach());
        assertEquals(
            x.getLimit()
                .getRetention()
                .getAmount(),
            y.getLimit()
                .getRetention()
                .getAmount());
        assertEquals(
            x.getLimit()
                .getRetention()
                .isEach(),
            y.getLimit()
                .getRetention()
                .isEach());
        assertEquals(
            x.getWith()
                .isNegated(),
            y.getWith()
                .isNegated());
        assertEquals(
            String.valueOf(
                x.getWith()
                    .getTagMatcher()),
            String.valueOf(
                y.getWith()
                    .getTagMatcher()));
        assertEquals(String.valueOf(a.getExclusions()), String.valueOf(b.getExclusions()));
    }

    private static StudioFieldPicker.Session picker(StudioFieldDraft draft, UUID row) {
        StudioFieldPicker.Target target = StudioFieldPicker.resourceRowTarget(draft, row);
        assertNotNull(target);
        StudioFieldPicker.Snapshot snapshot = StudioFieldPicker.items(Arrays.asList("minecraft:gold_ingot"), target);
        StudioFieldPicker.Session session = new StudioFieldPicker.Session(draft, target, snapshot);
        session.select(snapshot.search("gold").entries.get(0));
        return session;
    }

    @Test
    void compoundOreFilterKeepsExistingCompilerRefusalAndNoPartialFields() {
        for (String filter : new String[] { "WITH (#ingotIron OR #dustIron)", "WITHOUT NOT #dustIron",
            "WITH #a AND #b" }) {
            String clause = "INPUT item:a " + filter + " FROM source";
            ProgramBuildResult built = new ProgramBuilder("every 20 ticks do " + clause + " end").build();
            assertFalse(built.isSuccess());
            assertTrue(
                StudioNodeProperties.inspect(Type.INPUT, clause)
                    .isEmpty());
        }
    }

    @Test
    void orderedModelPreservesDuplicateCaseQuotedOrUnicodeAndSeparatorGaps() {
        String text = "item:Case\toR\t\"item:Room OR 🏭\" Or  item:Case";
        StudioResourceAlternatives rows = new StudioResourceAlternatives(text);
        assertEquals(3, rows.size());
        assertEquals(text, rows.source());
        assertNotEquals(
            rows.rows()
                .get(0).id,
            rows.rows()
                .get(2).id);
        rows.set(
            rows.rows()
                .get(1).id,
            "\"item:New OR 🏭\"");
        assertEquals(text.replace("Room", "New"), rows.source());
        StudioResourceAlternatives copy = new StudioResourceAlternatives(rows);
        assertEquals(rows.revision(), copy.revision());
        assertEquals(
            rows.rows()
                .get(1).id,
            copy.rows()
                .get(1).id);
        copy.remove(
            copy.rows()
                .get(1).id);
        assertEquals("item:Case\toR\titem:Case", copy.source());
        copy.append("fluid:water");
        assertEquals("item:Case\toR\titem:Case OR fluid:water", copy.source());
        assertEquals(3, rows.size());
        assertThrows(
            UnsupportedOperationException.class,
            () -> copy.rows()
                .clear());
    }

    @Test
    void bothOrdersRetainOneCommonLimitAndEveryOutsideSpanDuringEditAndRemoval() {
        String group = "\"item:Old OR 🏭\"\toR\titem:Case Or item:Case";
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT }) for (boolean first : new boolean[] { false, true }) {
            String access = (type == Type.INPUT ? "FROM" : "TO EMPTY SLOTS IN")
                + " EACH \"箱 🏭\",\"箱 🏭\" ROUND ROBIN BY LABEL North SIDE SLOTS 0-8";
            String action = (type == Type.INPUT ? "InPuT" : "oUtPuT") + " 16 EACH ReTaIn 2 EACH "
                + group
                + " wItHoUt TAG#dust* eXcEpT item:bad,fluid:lava";
            String clause = first ? access + "  " + action : action + "  " + access;
            Node node = new Node(UUID.randomUUID(), type, 0, 0, clause);
            StudioFieldDraft draft = draft(node, program(clause));
            assertEquals(3, draft.resourceCount());
            draft.setResource(
                draft.resourceRows()
                    .get(1).id,
                "item:New");
            draft.addResource("fluid:water");
            String changed = draft.appliedClause(PROJECT, node, program(clause));
            assertEquals(
                clause.replace(group, group.replace("\titem:Case", "\titem:New") + " OR fluid:water"),
                changed);
            commonLimit(limits(clause), limits(changed));
            assertEquals(
                4,
                limits(changed).getLimits()
                    .get(0)
                    .getResourceIds()
                    .getIdentifiers()
                    .size());
            for (StudioResourceAlternatives.Row row : draft.resourceRows()) draft.removeResource(row.id);
            String removed = draft.appliedClause(PROJECT, node, program(clause));
            assertEquals(clause.replace(group, ""), removed);
            commonLimit(limits(clause), limits(removed));
        }
    }

    @Test
    void absentSelectorDefaultsAndFilterOnlyInsertionRemovalPreserveInputOutputRetention() {
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT })
            for (boolean first : new boolean[] { false, true }) for (String rest : new String[] { "", "16",
                "RETAIN 2 EACH", "16 EACH RETAIN 2 EACH", "WITH #ingotIron", "WITHOUT #dust*" }) {
                    String access = type == Type.INPUT ? "FROM source" : "TO dest";
                    String action = (type == Type.INPUT ? "INPUT" : "OUTPUT") + (rest.isEmpty() ? "" : " " + rest);
                    String clause = first ? access + " " + action : action + " " + access;
                    Node node = new Node(UUID.randomUUID(), type, 0, 0, clause);
                    StudioFieldDraft draft = draft(node, program(clause));
                    assertEquals(0, draft.resourceCount());
                    draft.addResource("item:a");
                    draft.addResource("fluid:water");
                    String added = draft.appliedClause(PROJECT, node, program(clause));
                    commonLimit(limits(clause), limits(added));
                    assertEquals(
                        2,
                        limits(added).getLimits()
                            .get(0)
                            .getResourceIds()
                            .getIdentifiers()
                            .size());
                    for (StudioResourceAlternatives.Row row : draft.resourceRows()) draft.removeResource(row.id);
                    assertFalse(draft.dirty());
                    assertEquals(clause, draft.appliedClause(PROJECT, node, program(clause)));
                    if (rest.isEmpty()) assertEquals(
                        type == Type.INPUT ? 0 : Long.MAX_VALUE,
                        limits(added).getLimits()
                            .get(0)
                            .getLimit()
                            .getRetention()
                            .getAmount());
                }
    }

    @Test
    void invalidPartitionAndSingleBlankCannotLookCleanByJoinedTextAndAreRetainedAcrossCopies() {
        String clause = "INPUT \"item:a OR b\" OR item:c FROM source", source = program(clause);
        Node node = new Node(UUID.randomUUID(), Type.INPUT, 0, 0, clause);
        StudioFieldDraft draft = draft(node, source);
        String initial = draft.value("resource");
        draft.setResource(
            draft.resourceRows()
                .get(0).id,
            "\"item:a");
        draft.setResource(
            draft.resourceRows()
                .get(1).id,
            "b\" OR item:c");
        assertEquals(initial, draft.value("resource"));
        assertTrue(draft.dirty());
        StudioFieldDraft copy = new StudioFieldDraft(draft);
        assertTrue(copy.dirty());
        assertThrows(IllegalArgumentException.class, () -> copy.appliedClause(PROJECT, node, source));
        assertTrue(copy.set("resource", initial));
        assertFalse(copy.dirty());
        String empty = "INPUT FROM source";
        Node blankNode = new Node(UUID.randomUUID(), Type.INPUT, 0, 0, empty);
        StudioFieldDraft blank = draft(blankNode, program(empty));
        UUID row = blank.addResource("");
        assertEquals("", blank.value("resource"));
        assertTrue(new StudioFieldDraft(blank).dirty());
        assertThrows(IllegalArgumentException.class, () -> blank.appliedClause(PROJECT, blankNode, program(empty)));
        blank.removeResource(row);
        assertFalse(blank.dirty());
        assertEquals(empty, blank.appliedClause(PROJECT, blankNode, program(empty)));
    }

    @Test
    void perCellValidationRejectsCommasOrClausesCommentsAndLexerErrorsWithoutPartialApply() {
        String clause = "INPUT 16 item:a OR item:b FROM source", source = program(clause);
        Node node = new Node(UUID.randomUUID(), Type.INPUT, 0, 0, clause);
        for (String invalid : new String[] { "", " ", "item:a OR item:b", "item:a,item:b", "item:a OR", "a?", "a.b",
            "item:a WITH #dust*", "item:a EXCEPT item:c", "item:a -- hide", "\"unfinished", "a\nb", "a\rb", "a\0b" }) {
            StudioFieldDraft draft = draft(node, source);
            UUID row = draft.resourceRows()
                .get(0).id;
            draft.setResource(row, invalid);
            draft.set("amount", "32");
            StudioFieldDraft copy = new StudioFieldDraft(draft);
            assertEquals(invalid, copy.resourceValue(row));
            assertEquals(draft.resourceRevision(), copy.resourceRevision());
            assertThrows(IllegalArgumentException.class, () -> copy.appliedClause(PROJECT, node, source), invalid);
            assertEquals(clause, node.clause());
            assertEquals("32", copy.value("amount"));
        }
    }

    @Test
    void pickerRepairsOnlyItsRowAndLabelExclusionEditsDoNotInterfereWithResourceRevision() {
        String clause = "INPUT item:a OR item:b EXCEPT item:c FROM source", source = program(clause);
        Node node = new Node(UUID.randomUUID(), Type.INPUT, 0, 0, clause);
        StudioFieldDraft draft = draft(node, source);
        UUID row = draft.resourceRows()
            .get(0).id,
            neighbor = draft.resourceRows()
                .get(1).id;
        draft.setResource(row, "");
        draft.setResource(neighbor, "\"invalid");
        StudioFieldPicker.Session picker = picker(draft, row);
        draft.setLabel(
            draft.labelRows()
                .get(0).id,
            "renamed");
        draft.setExclusion(
            draft.exclusionRows()
                .get(0).id,
            "item:d");
        assertTrue(picker.choose(draft, PROJECT, node, source));
        assertEquals("\"item:minecraft:gold_ingot\"", draft.resourceValue(row));
        assertEquals("\"invalid", draft.resourceValue(neighbor));
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, source));
        draft.removeResource(neighbor);
        assertEquals(
            "INPUT \"item:minecraft:gold_ingot\" EXCEPT item:d FROM renamed",
            draft.appliedClause(PROJECT, node, source));
        assertEquals(clause, node.clause());
    }

    @Test
    void stalePickerRejectsStructuralAbaRemovedShiftedCopiesOwnersAndChangedRowContent() {
        String clause = "OUTPUT item:a OR item:a OR item:a TO dest", source = program(clause);
        Node node = new Node(UUID.randomUUID(), Type.OUTPUT, 0, 0, clause);
        for (boolean aba : new boolean[] { true, false }) {
            StudioFieldDraft draft = draft(node, source);
            UUID target = draft.resourceRows()
                .get(1).id;
            StudioFieldPicker.Session picker = picker(draft, target);
            draft.removeResource(
                aba ? target
                    : draft.resourceRows()
                        .get(0).id);
            if (aba) draft.addResource("item:a");
            String pending = draft.value("resource");
            assertThrows(IllegalArgumentException.class, () -> picker.choose(draft, PROJECT, node, source));
            assertEquals(pending, draft.value("resource"));
        }
        StudioFieldDraft draft = draft(node, source);
        UUID target = draft.resourceRows()
            .get(0).id;
        StudioFieldPicker.Session picker = picker(draft, target);
        assertThrows(
            IllegalArgumentException.class,
            () -> picker.choose(new StudioFieldDraft(draft), PROJECT, node, source));
        assertThrows(IllegalArgumentException.class, () -> picker.choose(draft, UUID.randomUUID(), node, source));
        assertThrows(IllegalArgumentException.class, () -> picker.choose(draft, PROJECT, node, source + " "));
        assertThrows(
            IllegalArgumentException.class,
            () -> picker.choose(draft, PROJECT, new Node(UUID.randomUUID(), Type.OUTPUT, 0, 0, clause), source));
        draft.setResource(target, "item:other");
        assertThrows(IllegalArgumentException.class, () -> picker.choose(draft, PROJECT, node, source));
    }

    @Test
    void thirtyTwoAllowedButIndependentLimitsTrailingCommaTrailingOrAndOversizeStayManual() {
        String group = String.join(" OR ", Collections.nCopies(32, "item:same"));
        StudioResourceAlternatives rows = new StudioResourceAlternatives(group);
        UUID revision = rows.revision();
        assertThrows(IllegalArgumentException.class, () -> rows.append("extra"));
        assertEquals(revision, rows.revision());
        assertEquals(group, rows.source());
        String clause = "INPUT " + group + " FROM source";
        assertTrue(draft(new Node(UUID.randomUUID(), Type.INPUT, 0, 0, clause), program(clause)).hasResourceRows());
        for (String manual : new String[] { group + " OR extra", "item:a OR", "item:a,", "16 item:a,2 item:b" }) {
            String full = "INPUT " + manual + " EXCEPT item:c FROM source";
            Node node = new Node(UUID.randomUUID(), Type.INPUT, 0, 0, full);
            StudioFieldDraft draft = draft(node, program(full));
            assertFalse(draft.hasResourceRows());
            assertTrue(draft.hasExclusionRows());
            assertEquals(full, draft.appliedClause(PROJECT, node, program(full)));
            assertThrows(
                IllegalArgumentException.class,
                () -> StudioNodeProperties.updateResourceAlternatives(Type.INPUT, full, "item:new"));
            assertThrows(IllegalArgumentException.class, () -> new StudioResourceAlternatives(manual));
        }
    }

    @Test
    void combinedOreAndRowsRemainAtomicAfterOrAddedAndUndoRestoresExactSourceAndUuid() {
        String source = "-- 🏭\r\n"
            + program("INPUT 16 EACH RETAIN 2 EACH item:a WITH #ingotIron EXCEPT item:c FROM a,b OUTPUT TO dest");
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
        UUID row = draft.addResource("");
        draft.set("amount", "32");
        draft.setLabel(
            draft.labelRows()
                .get(0).id,
            "renamed");
        draft.setOreMode(StudioOreFilter.Mode.WITHOUT);
        draft.setOrePattern("dust*");
        draft.setExclusion(
            draft.exclusionRows()
                .get(0).id,
            "item:d");
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, document.code()));
        assertEquals(source, document.source());
        assertFalse(document.undo());
        StudioFieldDraft copy = new StudioFieldDraft(draft);
        copy.setResource(row, "fluid:water");
        assertEquals(draft.resourceRevision(), copy.resourceRevision());
        String applied = copy.appliedClause(PROJECT, node, document.code());
        assertTrue(applied.contains(" OR fluid:water"), applied);
        assertEquals(
            "INPUT 32 EACH RETAIN 2 EACH item : a OR fluid:water WITHOUT # dust* EXCEPT item:d FROM renamed , b",
            applied);
        assertTrue(
            limits(applied).getLimits()
                .get(0)
                .getWith()
                .isNegated());
        assertEquals(
            "dust*",
            limits(applied).getLimits()
                .get(0)
                .getWith()
                .getTagMatcher()
                .getPatternString());
        assertEquals(
            1,
            limits(applied).getLimits()
                .size());
        assertEquals(
            2,
            limits(applied).getLimits()
                .get(0)
                .getResourceIds()
                .getIdentifiers()
                .size());
        assertEquals(
            32,
            limits(applied).getLimits()
                .get(0)
                .getLimit()
                .getQuantity()
                .getAmount());
        document.editGraph(
            g -> g.node(node.id)
                .setClause(applied));
        assertNotNull(BlueprintDocument.open(document.source()).graph.node(node.id));
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
    void compactTokenBoundariesAndLegacySingleResourceSetterRetainExistingBehavior() {
        String compact = "INPUT\"item:a\"oR\"item:b\"FROM\"src😀\"";
        assertEquals(
            "INPUT item:c OR item:d FROM\"src😀\"",
            StudioNodeProperties.updateResourceAlternatives(Type.INPUT, compact, "item:c OR item:d"));
        assertEquals("INPUT FROM\"src😀\"", StudioNodeProperties.updateResourceAlternatives(Type.INPUT, compact, ""));
        String clause = "OUTPUT item:a TO dest", source = program(clause);
        Node node = new Node(UUID.randomUUID(), Type.OUTPUT, 0, 0, clause);
        StudioFieldDraft draft = draft(node, source);
        draft.set("resource", "item:b");
        assertEquals(1, draft.resourceCount());
        assertEquals("OUTPUT item:b TO dest", draft.appliedClause(PROJECT, node, source));
        draft.set("resource", "");
        assertEquals(0, draft.resourceCount());
        assertEquals("OUTPUT  TO dest", draft.appliedClause(PROJECT, node, source));
        draft.set("resource", "item:a OR item:b");
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, source));
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties
                .updateResourceAlternatives(Type.REDSTONE, "OUTPUT REDSTONE LEVEL TO a", "item:a"));
    }
}
