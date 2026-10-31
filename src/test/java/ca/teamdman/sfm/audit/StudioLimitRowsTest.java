package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintDocument;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioDocument;
import ca.teamdman.sfm.client.blueprint.StudioFieldDraft;
import ca.teamdman.sfm.client.blueprint.StudioFieldPicker;
import ca.teamdman.sfm.client.blueprint.StudioLimitRows;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfml.ast.InputStatement;
import ca.teamdman.sfml.ast.OutputStatement;
import ca.teamdman.sfml.ast.ResourceLimits;
import ca.teamdman.sfml.ast.Statement;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class StudioLimitRowsTest {

    private static final UUID PROJECT = new UUID(14, 1037);

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

    @Test
    void parsedRowsPreserveQuotedCommasUnicodeDuplicatesOrAndExactSeparatorGaps() {
        String a = "0016 EACH RETAIN 0002 EACH \"item:Room, OR 🏭\" oR item:b wItH TAG#ingot*";
        String text = a + " \t,\t2 item:c," + a;
        StudioLimitRows rows = new StudioLimitRows(text);
        assertEquals(3, rows.size());
        assertEquals(
            a,
            rows.rows()
                .get(0).text);
        assertEquals(text, rows.source());
        assertNotEquals(
            rows.rows()
                .get(0).id,
            rows.rows()
                .get(2).id);
        List<StudioLimitRows.Row> snapshot = rows.rows();
        StudioLimitRows copy = new StudioLimitRows(rows);
        copy.set(
            copy.rows()
                .get(1).id,
            "4 item:d");
        assertEquals(text.replace("2 item:c", "4 item:d"), copy.source());
        assertEquals("2 item:c", snapshot.get(1).text);
        copy.remove(
            copy.rows()
                .get(1).id);
        assertEquals(a + " \t,\t" + a, copy.source());
        copy.append("WITH #ingot*");
        assertEquals(a + " \t,\t" + a + ", WITH #ingot*", copy.source());
        assertEquals(text, rows.source());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.clear());
    }

    @Test
    void bothDirectionsAndOrdersEditWholeListWithoutCollapsingIndependentAllowances() {
        String group = "16 EACH RETAIN 2 EACH item:a OR item:b WITHOUT #dust*\t, 3 RETAIN 1 item:c,4 fluid:water";
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT }) for (boolean first : new boolean[] { false, true }) {
            String access = (type == Type.INPUT ? "FROM" : "TO EMPTY SLOTS IN")
                + " EACH \"Factory 🏭\",other ROUND ROBIN BY LABEL North,Top SIDE SLOTS 0,2-8";
            String action = (type == Type.INPUT ? "InPuT " : "OuTpUt ") + group + " EXCEPT item:dirt,fluid:lava";
            String clause = first ? access + "  " + action : action + "  " + access;
            Node node = node(type, clause);
            StudioFieldDraft draft = draft(node);
            assertTrue(draft.hasLimitRows());
            assertFalse(draft.hasResourceRows());
            assertEquals(3, draft.limitCount());
            assertTrue(
                StudioNodeProperties.inspect(type, clause)
                    .stream()
                    .noneMatch(
                        f -> f.getKey()
                            .equals("amount")
                            || f.getKey()
                                .equals("oreFilter")));
            draft.setLimit(
                draft.limitRows()
                    .get(1).id,
                "5 RETAIN 1 item:new");
            String changed = draft.appliedClause(PROJECT, node, program(clause));
            assertEquals(clause.replace("3 RETAIN 1 item:c", "5 RETAIN 1 item:new"), changed);
            ResourceLimits parsed = limits(changed);
            assertEquals(
                3,
                parsed.getLimits()
                    .size());
            assertEquals(
                16,
                parsed.getLimits()
                    .get(0)
                    .getLimit()
                    .getQuantity()
                    .getAmount());
            assertEquals(
                5,
                parsed.getLimits()
                    .get(1)
                    .getLimit()
                    .getQuantity()
                    .getAmount());
            assertEquals(
                4,
                parsed.getLimits()
                    .get(2)
                    .getLimit()
                    .getQuantity()
                    .getAmount());
            assertEquals(
                2,
                parsed.getLimits()
                    .get(0)
                    .getResourceIds()
                    .getIdentifiers()
                    .size());
            assertTrue(
                parsed.getLimits()
                    .get(0)
                    .getWith()
                    .isNegated());
        }
    }

    @Test
    void globalsApplyBeforeTwoToOneAndReopeningReturnsOriginalSingleLimitControls() {
        String clause = "OUTPUT 16 RETAIN 2 item:a,3 item:b EXCEPT item:c TO EMPTY SLOTS IN EACH source,spare ROUND ROBIN BY BLOCK NORTH SIDE SLOTS 0-2";
        Node node = node(Type.OUTPUT, clause);
        StudioFieldDraft draft = draft(node);
        draft.removeLimit(
            draft.limitRows()
                .get(1).id);
        assertTrue(draft.hasLimitRows());
        assertEquals(1, draft.limitCount());
        draft.setLabel(
            draft.labelRows()
                .get(0).id,
            "renamed");
        draft.setExclusion(
            draft.exclusionRows()
                .get(0).id,
            "item:d");
        draft.set("each", "false");
        draft.set("roundRobin", "label");
        draft.set("sides", "SOUTH");
        draft.set("slots", "1-3");
        draft.set("emptySlots", "false");
        String changed = draft.appliedClause(PROJECT, node, program(clause));
        assertEquals(
            "OUTPUT 16 RETAIN 2 item:a EXCEPT item:d TO   renamed,spare ROUND ROBIN BY LABEL SOUTH SIDE SLOTS 1-3",
            changed);
        StudioFieldDraft reopened = draft(node(Type.OUTPUT, changed));
        assertFalse(reopened.hasLimitRows());
        assertTrue(reopened.hasResourceRows());
        assertEquals("16", reopened.value("amount"));
        assertEquals(
            1,
            limits(changed).getLimits()
                .size());
        assertEquals(clause, node.clause());
    }

    @Test
    void missingQuantitiesAndRetentionKeepDirectionSpecificDefaultsAndFilterOnlyRows() {
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT }) {
            String clause = (type == Type.INPUT ? "INPUT " : "OUTPUT ") + "item:a,WITH #ingot* "
                + (type == Type.INPUT ? "FROM source" : "TO dest");
            Node node = node(type, clause);
            StudioFieldDraft draft = draft(node);
            assertEquals(clause, draft.appliedClause(PROJECT, node, program(clause)));
            draft.removeLimit(
                draft.limitRows()
                    .get(0).id);
            String changed = draft.appliedClause(PROJECT, node, program(clause));
            assertEquals(clause.replace("item:a,", ""), changed);
            assertEquals(
                type == Type.INPUT ? 0 : Long.MAX_VALUE,
                limits(changed).getLimits()
                    .get(0)
                    .getLimit()
                    .getRetention()
                    .getAmount());
            assertEquals(
                Long.MAX_VALUE,
                limits(changed).getLimits()
                    .get(0)
                    .getLimit()
                    .getQuantity()
                    .getAmount());
            assertEquals(
                "ingot*",
                limits(changed).getLimits()
                    .get(0)
                    .getWith()
                    .getTagMatcher()
                    .getPatternString());
        }
    }

    @Test
    void blankInjectedOrUnsupportedCellsRefuseWholeApplyAndPreservePendingGlobals() {
        String clause = "INPUT 16 item:a,2 item:b FROM source";
        Node node = node(Type.INPUT, clause);
        for (String invalid : new String[] { "", " ", "item:a,", "1 item:a,2 item:b", "item:a FROM stolen",
            "item:a OUTPUT TO dest", "item:a -- hide", "item:a\n", "item:a\0", "item:a?", "\"unfinished",
            "(1+2) item:a", "9223372036854775808 item:a", "item:a WITH (#a OR #b)" }) {
            StudioFieldDraft draft = draft(node);
            UUID row = draft.limitRows()
                .get(0).id;
            draft.setLimit(row, invalid);
            draft.setLabel(
                draft.labelRows()
                    .get(0).id,
                "renamed");
            StudioFieldDraft copy = new StudioFieldDraft(draft);
            assertTrue(copy.dirty());
            assertEquals(invalid, copy.limitValue(row));
            assertEquals(draft.limitRevision(), copy.limitRevision());
            assertThrows(
                IllegalArgumentException.class,
                () -> copy.appliedClause(PROJECT, node, program(clause)),
                invalid);
            assertEquals(clause, node.clause());
            assertEquals("renamed", copy.value("label"));
        }
    }

    @Test
    void joinedTextCannotHideMalformedCellPartitionsAcrossCopiesAndRepair() {
        String clause = "INPUT \"item:a,b\",item:c FROM source";
        Node node = node(Type.INPUT, clause);
        StudioFieldDraft draft = draft(node);
        String original = draft.value("limits");
        draft.setLimit(
            draft.limitRows()
                .get(0).id,
            "\"item:a");
        draft.setLimit(
            draft.limitRows()
                .get(1).id,
            "b\",item:c");
        assertEquals(original, draft.value("limits"));
        assertTrue(draft.dirty());
        StudioFieldDraft copy = new StudioFieldDraft(draft);
        assertTrue(copy.dirty());
        assertThrows(IllegalArgumentException.class, () -> copy.appliedClause(PROJECT, node, program(clause)));
        copy.set("limits", original);
        assertFalse(copy.dirty());
        assertTrue(draft.dirty());
    }

    @Test
    void explicitRemoveAppendBoundsAndRevisionChangesAreAtomicAndDoNotShareSnapshots() {
        StudioLimitRows rows = new StudioLimitRows("item:a,item:b");
        List<StudioLimitRows.Row> snapshot = rows.rows();
        UUID removed = snapshot.get(0).id, revision = rows.revision();
        StudioLimitRows copy = new StudioLimitRows(rows);
        copy.remove(removed);
        assertNotEquals(revision, copy.revision());
        assertEquals(2, rows.size());
        assertThrows(IllegalArgumentException.class, () -> copy.set(removed, "item:new"));
        assertThrows(
            IllegalArgumentException.class,
            () -> copy.remove(
                copy.rows()
                    .get(0).id));
        UUID last = copy.rows()
            .get(0).id, before = copy.revision();
        assertThrows(
            IllegalArgumentException.class,
            () -> copy.set(last, String.join("", Collections.nCopies(4097, "x"))));
        assertEquals(before, copy.revision());
        assertEquals("item:b", copy.value(last));
        UUID blank = copy.append("");
        assertEquals("item:b, ", copy.source());
        assertThrows(IllegalArgumentException.class, () -> StudioLimitRows.validateRow(Type.INPUT, copy.value(blank)));
        copy.remove(blank);
        assertEquals("item:b", copy.source());
        assertEquals("item:a", snapshot.get(0).text);
    }

    @Test
    void initialAbsentSingleAndOtherNodesNeverGainLimitRowsOrPickers() {
        for (String clause : new String[] { "INPUT FROM source", "INPUT 16 item:a FROM source",
            "INPUT item:a OR item:b FROM source" }) {
            StudioFieldDraft draft = draft(node(Type.INPUT, clause));
            assertFalse(draft.hasLimitRows());
            assertTrue(draft.hasResourceRows());
            assertThrows(IllegalArgumentException.class, () -> draft.addLimit("item:b"));
            assertThrows(
                IllegalArgumentException.class,
                () -> StudioNodeProperties.updateIndependentLimits(Type.INPUT, clause, "item:a,item:b"));
        }
        String clause = "INPUT item:a,item:b FROM source";
        StudioFieldDraft draft = draft(node(Type.INPUT, clause));
        for (StudioNodeProperties.Field field : StudioNodeProperties.inspect(Type.INPUT, clause)) if (field.getKey()
            .equals("limits")) assertNull(StudioFieldPicker.target(Type.INPUT, field, draft.value("limits")));
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties.updateIndependentLimits(Type.IF, "source HAS = 0", "item:a"));
    }

    @Test
    void capsTrailingCommaUnsafeAndDynamicSourcesStayManualWithoutTruncation() {
        String group = String.join(",", Collections.nCopies(32, "item:a"));
        StudioLimitRows rows = new StudioLimitRows(group);
        UUID revision = rows.revision();
        assertThrows(IllegalArgumentException.class, () -> rows.append("item:b"));
        assertEquals(revision, rows.revision());
        assertTrue(draft(node(Type.INPUT, "INPUT " + group + " FROM source")).hasLimitRows());
        for (String manual : new String[] { group + ",item:b", "item:a,item:b,", "(1+2) item:a,item:b" }) {
            String clause = "INPUT " + manual + " FROM source";
            assertFalse(
                StudioNodeProperties.inspect(Type.INPUT, clause)
                    .stream()
                    .anyMatch(
                        f -> f.getKey()
                            .equals("limits")),
                clause);
            assertThrows(
                IllegalArgumentException.class,
                () -> StudioNodeProperties.updateIndependentLimits(Type.INPUT, clause, "item:c"));
        }
        for (String invalid : new String[] { "", " ", "item:a,", "item:a --hide", "item:a?", "item:a\n,item:b" })
            assertFalse(StudioLimitRows.supports(invalid), invalid);
    }

    @Test
    void completeRowsRetainCompilerSupportedTrailingOrAndUnlimitedInnerAlternatives() {
        String group = "item:a OR,item:b", clause = "INPUT " + group + " FROM source";
        Node node = node(Type.INPUT, clause);
        StudioFieldDraft draft = draft(node);
        assertTrue(draft.hasLimitRows());
        assertEquals(
            "item:a OR",
            draft.limitRows()
                .get(0).text);
        assertEquals(clause, draft.appliedClause(PROJECT, node, program(clause)));
        String many = String.join(" OR ", Collections.nCopies(33, "item:a"));
        draft.setLimit(
            draft.limitRows()
                .get(0).id,
            many);
        assertEquals(
            33,
            limits(draft.appliedClause(PROJECT, node, program(clause))).getLimits()
                .get(0)
                .getResourceIds()
                .getIdentifiers()
                .size());
    }

    @Test
    void compactQuotedWholeListReplacementsKeepKeywordTokenBoundariesAndOneRowUpdaterWorks() {
        String compact = "INPUT\"item:a\",\"item:b\"FROM\"source😀\"";
        assertEquals(
            "INPUT 2 item:c,3 item:d FROM\"source😀\"",
            StudioNodeProperties.updateIndependentLimits(Type.INPUT, compact, "2 item:c,3 item:d"));
        String one = StudioNodeProperties.updateIndependentLimits(Type.INPUT, compact, "item:c");
        assertEquals("INPUT item:c FROM\"source😀\"", one);
        assertEquals(
            "INPUT item:d FROM\"source😀\"",
            StudioNodeProperties.updateIndependentLimits(Type.INPUT, one, "item:d"));
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties.updateIndependentLimits(Type.INPUT, one, "item:c,item:d"));
    }

    @Test
    void combinedApplyPreservesOwnerFencesAllGraphIdentitiesAndExactSourceUndoRedo() {
        String source = "-- 🏭\r\nevery 20 ticks do if source HAS > 0 then INPUT 16 item:a,2 item:b FROM source OUTPUT TO dest else forget end end";
        StudioDocument document = new StudioDocument();
        document.load(source);
        document.openVisual();
        Node node = document.graph()
            .nodes()
            .stream()
            .filter(n -> n.type == Type.INPUT)
            .findFirst()
            .get();
        StudioFieldDraft draft = new StudioFieldDraft(
            PROJECT,
            node,
            document.code(),
            StudioNodeProperties.inspect(Type.INPUT, node.clause()));
        List<String> graph = identities(document.graph());
        draft.addLimit("");
        draft.setLabel(
            draft.labelRows()
                .get(0).id,
            "renamed");
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, document.code()));
        assertEquals(source, document.source());
        assertFalse(document.undo());
        draft.setLimit(
            draft.limitRows()
                .get(2).id,
            "3 fluid:water");
        assertThrows(
            IllegalArgumentException.class,
            () -> draft.appliedClause(UUID.randomUUID(), node, document.code()));
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, document.code() + " "));
        assertThrows(
            IllegalArgumentException.class,
            () -> draft.appliedClause(PROJECT, node(Type.INPUT, node.clause()), document.code()));
        String changed = draft.appliedClause(PROJECT, node, document.code());
        document.editGraph(
            g -> g.node(node.id)
                .setClause(changed));
        assertEquals(graph, identities(BlueprintDocument.open(document.source()).graph));
        assertTrue(document.undo());
        assertEquals(source, document.source());
        assertFalse(document.undo());
        assertTrue(document.redo());
        assertEquals(
            changed,
            document.graph()
                .node(node.id)
                .clause());
        assertEquals(graph, identities(document.graph()));
    }

    @Test
    void deepUnsupportedPendingSyntaxRefusesWithoutParserOrTreeWalkStackOverflow() {
        String left = String.join("", Collections.nCopies(1500, "("));
        String right = String.join("", Collections.nCopies(1500, ")"));
        String clause = "INPUT item:a,item:b FROM source";
        Node node = node(Type.INPUT, clause);
        for (String deep : new String[] { left + "1" + right + " item:a", "RETAIN " + left + "1" + right,
            "WITH " + left + "#a" + right, "WITH " + String.join("", Collections.nCopies(700, "NOT ")) + "#a",
            "WITH #a" + String.join("", Collections.nCopies(500, " OR #a")) }) {
            assertTrue(deep.length() < BlueprintGraph.MAX_CLAUSE);
            StudioFieldDraft draft = draft(node);
            draft.setLimit(
                draft.limitRows()
                    .get(0).id,
                deep);
            draft.setLabel(
                draft.labelRows()
                    .get(0).id,
                "renamed");
            StudioFieldDraft copy = new StudioFieldDraft(draft);
            assertThrows(IllegalArgumentException.class, () -> copy.appliedClause(PROJECT, node, program(clause)));
            assertEquals(
                deep,
                copy.limitRows()
                    .get(0).text);
            assertEquals(clause, node.clause());
        }
        String quoted = "\"item:" + left + right + "\"";
        StudioLimitRows.validateRow(Type.INPUT, quoted);
        assertEquals(quoted, new StudioLimitRows(quoted).source());
        String many = String.join(" OR ", Collections.nCopies(200, "item:a"));
        StudioLimitRows.validateRow(Type.INPUT, many);
    }

    private static List<String> identities(BlueprintGraph graph) {
        List<String> result = new ArrayList<>();
        for (Node node : graph.nodes()) result.add(node.id + ":" + node.type);
        for (BlueprintGraph.Edge edge : graph.edges()) result.add(edge.from + ":" + edge.pin + ":" + edge.to);
        Collections.sort(result);
        return result;
    }
}
