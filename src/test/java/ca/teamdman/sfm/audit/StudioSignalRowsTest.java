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
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfml.ast.RedstoneOutputStatement;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class StudioSignalRowsTest {

    private static final UUID PROJECT = new UUID(13, 1037);

    private static String program(String clause) {
        return "every 20 ticks do " + clause + " input from spare end";
    }

    private static Node node(String clause) {
        return new Node(UUID.randomUUID(), Type.REDSTONE, 0, 0, clause);
    }

    private static StudioFieldDraft draft(Node node, String source) {
        return new StudioFieldDraft(PROJECT, node, source, StudioNodeProperties.inspect(Type.REDSTONE, node.clause()));
    }

    private static RedstoneOutputStatement output(String clause) {
        ProgramBuildResult built = new ProgramBuilder(program(clause)).build();
        assertTrue(built.isSuccess(), built.getErrorMessage());
        return (RedstoneOutputStatement) built.getProgram()
            .getTriggers()
            .get(0)
            .getBlock()
            .getStatements()
            .get(0);
    }

    private static StudioFieldPicker.Session picker(StudioFieldDraft draft, UUID row) {
        StudioFieldPicker.Target target = StudioFieldPicker.labelRowTarget(draft, row);
        assertNotNull(target);
        StudioFieldPicker.Snapshot snapshot = StudioFieldPicker
            .labels(program("OUTPUT REDSTONE LEVEL TO spare"), target);
        StudioFieldPicker.Session session = new StudioFieldPicker.Session(draft, target, snapshot);
        session.select(snapshot.search("spare").entries.get(0));
        return session;
    }

    @Test
    void bothOrdersQuantityPositionsAndEachSpellingsPreserveAllOutsideSpans() {
        String labels = "\"Signal 🏭\" ,\tb,\"Signal 🏭\"";
        for (String each : new String[] { "", "EACH " })
            for (String quantity : new String[] { "00027", "00027 EACH" }) {
                String access = each + labels + " ROUND ROBIN BY LABEL North,Top SIDE SLOTS 0,2-8";
                for (String clause : new String[] { "OUTPUT " + quantity + " REDSTONE LEVEL TO " + access,
                    "OUTPUT REDSTONE LEVELS " + quantity + " TO " + access,
                    "TO " + access + " OUTPUT " + quantity + " REDSTONE LEVEL",
                    "TO " + access + " OUTPUT REDSTONE LEVELS " + quantity }) {
                    Node node = node(clause);
                    StudioFieldDraft draft = draft(node, program(clause));
                    assertEquals(3, draft.labelCount());
                    assertFalse(draft.hasResourceRows());
                    assertFalse(draft.hasExclusionRows());
                    assertEquals(
                        2,
                        StudioNodeProperties.inspect(Type.REDSTONE, clause)
                            .size());
                    assertEquals(clause, draft.appliedClause(PROJECT, node, program(clause)));
                    draft.setLabel(
                        draft.labelRows()
                            .get(1).id,
                        "\"Other Room\"");
                    draft.set("level", "00028");
                    String changed = draft.appliedClause(PROJECT, node, program(clause));
                    assertEquals(
                        clause.replace(",\tb,", ",\t\"Other Room\",")
                            .replace("00027", "00028"),
                        changed);
                    RedstoneOutputStatement original = output(clause), edited = output(changed);
                    assertEquals(15, edited.getLevel());
                    assertEquals(original.isEach(), edited.isEach());
                    assertEquals(
                        original.getLabelAccess()
                            .getSides()
                            .getSides(),
                        edited.getLabelAccess()
                            .getSides()
                            .getSides());
                    assertEquals(
                        original.getLabelAccess()
                            .getRoundRobin()
                            .getMode(),
                        edited.getLabelAccess()
                            .getRoundRobin()
                            .getMode());
                    assertEquals(
                        3,
                        edited.getLabelAccess()
                            .getLabels()
                            .size());
                }
            }
    }

    @Test
    void omittedAndExtremeStaticLevelsKeepDefaultClampLexemesAndSingleLabelSetter() {
        for (String clause : new String[] { "OUTPUT REDSTONE LEVEL TO first,second",
            "TO first,second OUTPUT REDSTONE LEVELS" }) {
            Node node = node(clause);
            StudioFieldDraft draft = draft(node, program(clause));
            assertEquals("15", draft.value("level"));
            draft.setLabel(
                draft.labelRows()
                    .get(0).id,
                "renamed");
            String renamed = draft.appliedClause(PROJECT, node, program(clause));
            assertEquals(clause.replace("first", "renamed"), renamed);
            assertEquals(15, output(renamed).getLevel());
            draft.set("level", "00007");
            assertEquals(
                renamed.replace("REDSTONE", "00007 REDSTONE"),
                draft.appliedClause(PROJECT, node, program(clause)));
            for (String level : new String[] { "0", "00015", "16", "9223372036854775807" }) {
                draft.set("level", level);
                String changed = draft.appliedClause(PROJECT, node, program(clause));
                assertTrue(changed.contains(level + " REDSTONE"));
                assertEquals((int) Math.min(15, Long.parseLong(level)), output(changed).getLevel());
            }
        }
        assertEquals(
            "OUTPUT 3 REDSTONE LEVEL TO other",
            StudioNodeProperties.update(Type.REDSTONE, "OUTPUT 3 REDSTONE LEVEL TO single", "label", "other"));
    }

    @Test
    void invalidCellsAndLevelRefuseWholeApplyAndSurviveIndependentRecoveryCopies() {
        String clause = "OUTPUT 3 REDSTONE LEVEL TO first,second", source = program(clause);
        Node node = node(clause);
        for (String invalid : new String[] { "", " ", "a,b", "a NORTH SIDE", "a -- hide", "\"unfinished", "a?", "a\nb",
            "a\0b" }) {
            StudioFieldDraft draft = draft(node, source);
            UUID row = draft.labelRows()
                .get(0).id;
            draft.setLabel(row, invalid);
            draft.set("level", "7");
            StudioFieldDraft copy = new StudioFieldDraft(draft);
            assertEquals(invalid, copy.labelValue(row));
            assertEquals(draft.labelRevision(), copy.labelRevision());
            assertThrows(IllegalArgumentException.class, () -> copy.appliedClause(PROJECT, node, source), invalid);
            assertEquals("7", copy.value("level"));
            assertEquals(clause, node.clause());
            copy.setLabel(row, "fixed");
            assertEquals(invalid, draft.labelValue(row));
            assertTrue(
                copy.appliedClause(PROJECT, node, source)
                    .contains("fixed"));
        }
        for (String invalid : new String[] { "-1", "1g", "9223372036854775808", "(1 + 2)", "1 EACH", "1 -- hide" }) {
            StudioFieldDraft draft = draft(node, source);
            draft.addLabel("third");
            draft.set("level", invalid);
            assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, source));
            assertEquals(3, draft.labelCount());
            assertEquals(clause, node.clause());
        }
    }

    @Test
    void malformedPartitionsCannotLookCleanAndLastLabelCannotBeRemoved() {
        String clause = "OUTPUT REDSTONE LEVEL TO \"a,b\",c", source = program(clause);
        Node node = node(clause);
        StudioFieldDraft draft = draft(node, source);
        String original = draft.value("label");
        draft.setLabel(
            draft.labelRows()
                .get(0).id,
            "\"a");
        draft.setLabel(
            draft.labelRows()
                .get(1).id,
            "b\",c");
        assertEquals(original, draft.value("label"));
        assertTrue(draft.dirty());
        StudioFieldDraft copy = new StudioFieldDraft(draft);
        assertTrue(copy.dirty());
        assertThrows(IllegalArgumentException.class, () -> copy.appliedClause(PROJECT, node, source));
        copy.set("label", original);
        assertFalse(copy.dirty());
        Node single = node("OUTPUT REDSTONE LEVEL TO a");
        StudioFieldDraft one = draft(single, program(single.clause()));
        UUID row = one.labelRows()
            .get(0).id, revision = one.labelRevision();
        assertThrows(IllegalArgumentException.class, () -> one.removeLabel(row));
        assertEquals(
            row,
            one.labelRows()
                .get(0).id);
        assertEquals(revision, one.labelRevision());
        assertFalse(one.dirty());
    }

    @Test
    void rowPickerRepairsOnlyItsCellAndOtherPendingLevelEditsDoNotOverFence() {
        String clause = "TO first,second OUTPUT 3 REDSTONE LEVEL", source = program(clause);
        Node node = node(clause);
        StudioFieldDraft draft = draft(node, source);
        UUID row = draft.labelRows()
            .get(0).id,
            neighbor = draft.labelRows()
                .get(1).id;
        draft.setLabel(row, "");
        draft.setLabel(neighbor, "\"invalid");
        StudioFieldPicker.Session session = picker(draft, row);
        draft.set("level", "7");
        assertTrue(session.choose(draft, PROJECT, node, source));
        assertEquals("\"spare\"", draft.labelValue(row));
        assertEquals("\"invalid", draft.labelValue(neighbor));
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, source));
        draft.removeLabel(neighbor);
        assertEquals("TO \"spare\" OUTPUT 7 REDSTONE LEVEL", draft.appliedClause(PROJECT, node, source));
    }

    @Test
    void stalePickerFencesRemovedShiftedAbaOwnersProjectsNodesSourcesAndChangedFields() {
        String clause = "OUTPUT 3 REDSTONE LEVEL TO a,a,a", source = program(clause);
        Node node = node(clause);
        for (boolean aba : new boolean[] { true, false }) {
            StudioFieldDraft draft = draft(node, source);
            UUID row = draft.labelRows()
                .get(1).id;
            StudioFieldPicker.Session session = picker(draft, row);
            draft.removeLabel(
                aba ? row
                    : draft.labelRows()
                        .get(0).id);
            if (aba) draft.addLabel("a");
            assertThrows(IllegalArgumentException.class, () -> session.choose(draft, PROJECT, node, source));
        }
        StudioFieldDraft draft = draft(node, source);
        UUID row = draft.labelRows()
            .get(0).id;
        StudioFieldPicker.Session session = picker(draft, row);
        assertThrows(
            IllegalArgumentException.class,
            () -> session.choose(new StudioFieldDraft(draft), PROJECT, node, source));
        assertThrows(IllegalArgumentException.class, () -> session.choose(draft, UUID.randomUUID(), node, source));
        assertThrows(IllegalArgumentException.class, () -> session.choose(draft, PROJECT, node(clause), source));
        assertThrows(IllegalArgumentException.class, () -> session.choose(draft, PROJECT, node, source + " "));
        draft.setLabel(row, "other");
        assertThrows(IllegalArgumentException.class, () -> session.choose(draft, PROJECT, node, source));
    }

    @Test
    void capTwoQuantitiesDynamicAndMalformedFormsStayManualWithoutTruncation() {
        String group = String.join(",", Collections.nCopies(32, "signal"));
        String clause = "OUTPUT 3 REDSTONE LEVEL TO " + group;
        StudioFieldDraft draft = draft(node(clause), program(clause));
        assertEquals(32, draft.labelCount());
        UUID revision = draft.labelRevision();
        assertThrows(IllegalArgumentException.class, () -> draft.addLabel("extra"));
        assertEquals(revision, draft.labelRevision());
        for (String manual : new String[] { clause + ",extra", "OUTPUT 1 REDSTONE LEVEL 15 TO a,b",
            "TO a,b OUTPUT 1 REDSTONE LEVEL 15", "OUTPUT (2+3) REDSTONE LEVEL TO a,b", "OUTPUT REDSTONE LEVEL TO a,",
            "OUTPUT 3 REDSTONE LEVEL TO", "OUTPUT 3 REDSTONE LEVEL TO a?" }) {
            assertTrue(
                StudioNodeProperties.inspect(Type.REDSTONE, manual)
                    .isEmpty(),
                manual);
            assertThrows(
                IllegalArgumentException.class,
                () -> StudioNodeProperties.update(Type.REDSTONE, manual, "level", "7"));
        }
    }

    @Test
    void compactQuotedBoundariesAndCombinedEditPreserveExactSourceHistoryAndAllBranchIdentities() {
        for (String compact : new String[] { "TO\"a\",\"b\"OUTPUT REDSTONE LEVEL",
            "OUTPUT REDSTONE LEVEL TO\"a\",\"b\"" }) {
            String changed = StudioNodeProperties.update(Type.REDSTONE, compact, "label", "c,d");
            assertTrue(changed.contains("c,d"));
            assertEquals(
                2,
                output(changed).getLabelAccess()
                    .getLabels()
                    .size());
            assertEquals(15, output(changed).getLevel());
        }
        String source = "-- 🏭\r\nevery 20 ticks do if source HAS > 0 then TO EACH \"a\",b OUTPUT 3 EACH REDSTONE LEVEL else PRINT(\"skip\") end end";
        StudioDocument document = new StudioDocument();
        document.load(source);
        document.openVisual();
        Node node = document.graph()
            .nodes()
            .stream()
            .filter(n -> n.type == Type.REDSTONE)
            .findFirst()
            .get();
        List<String> graph = identities(document.graph());
        StudioFieldDraft draft = draft(node, document.code());
        draft.addLabel("\"Other 🏭\"");
        draft.set("level", "00007");
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

    private static List<String> identities(BlueprintGraph graph) {
        List<String> result = new ArrayList<>();
        for (Node node : graph.nodes()) result.add(node.id + ":" + node.type);
        for (BlueprintGraph.Edge edge : graph.edges()) result.add(edge.from + ":" + edge.pin + ":" + edge.to);
        Collections.sort(result);
        return result;
    }
}
