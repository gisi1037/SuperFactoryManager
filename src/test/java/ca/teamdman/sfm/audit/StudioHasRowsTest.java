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
import ca.teamdman.sfm.client.blueprint.StudioResourceAlternatives;

class StudioHasRowsTest {

    private static final UUID PROJECT = new UUID(12, 1037);

    private static String program(String clause) {
        return "every 20 ticks do if " + clause + " then input from \"spare\" output to \"dest\" else forget end end";
    }

    private static StudioFieldDraft draft(Node node, String source) {
        return new StudioFieldDraft(PROJECT, node, source, StudioNodeProperties.inspect(Type.IF, node.clause()));
    }

    private static Node node(String clause) {
        return new Node(UUID.randomUUID(), Type.IF, 0, 0, clause);
    }

    private static StudioFieldPicker.Session picker(StudioFieldDraft draft, UUID row, boolean label) {
        StudioFieldPicker.Target target = label ? StudioFieldPicker.labelRowTarget(draft, row)
            : StudioFieldPicker.resourceRowTarget(draft, row);
        assertNotNull(target);
        StudioFieldPicker.Snapshot snapshot = label ? StudioFieldPicker.labels(program("\"spare\" HAS > 0"), target)
            : StudioFieldPicker.items(Arrays.asList("minecraft:gold_ingot"), target);
        StudioFieldPicker.Session session = new StudioFieldPicker.Session(draft, target, snapshot);
        session.select(snapshot.search(label ? "spare" : "gold").entries.get(0));
        return session;
    }

    @Test
    void groupedHasEditsOnlyChosenSpansAndPreservesEverySetOperatorAndAccessModifier() {
        String labels = "\"Room 🏭\" ,\t\"b\",\"Room 🏭\"", resources = "item : a\toR\t\"fluid:Water\" Or item : a";
        for (String set : new String[] { "", "OVERALL ", "SOME ", "EVERY ", "EACH ", "ONE ", "LONE " }) {
            String clause = "((" + set
                + labels
                + " ROUND ROBIN BY LABEL North,Top SIDE SLOTS 0,2-8 HAS gE 00009 "
                + resources
                + " WITHOUT #forge:bad EXCEPT item:dirt,fluid:water))";
            Node node = node(clause);
            StudioFieldDraft draft = draft(node, program(clause));
            assertEquals(3, draft.labelCount());
            assertEquals(3, draft.resourceCount());
            assertFalse(draft.hasExclusionRows());
            assertEquals(
                4,
                StudioNodeProperties.inspect(Type.IF, clause)
                    .size());
            assertEquals(clause, draft.appliedClause(PROJECT, node, program(clause)));
            draft.setLabel(
                draft.labelRows()
                    .get(1).id,
                "\"New 🏭\"");
            draft.setResource(
                draft.resourceRows()
                    .get(1).id,
                "fluid:lava");
            draft.set("comparator", "!=");
            draft.set("amount", "00010");
            assertEquals(
                clause.replace(",\t\"b\",", ",\t\"New 🏭\",")
                    .replace("\"fluid:Water\"", "fluid:lava")
                    .replace("HAS gE 00009", "HAS != 00010"),
                draft.appliedClause(PROJECT, node, program(clause)));
            assertEquals(clause, node.clause());
        }
    }

    @Test
    void explicitRemovalRestoresDefaultAndCompactQuotedBoundariesRemainTokenSafe() {
        for (String clause : new String[] { "\"a\" HAS=0", "(\"a\" HAS=0)", "((\"src😀\"HAS=0))",
            "(\"a\" HAS=0 WITH #ingotIron EXCEPT item:dirt)", "(\"src😀\"HAS=0\"item:a\"oR\"item:b\")" }) {
            Node node = node(clause);
            StudioFieldDraft draft = draft(node, program(clause));
            draft.addResource("fluid:water");
            String added = draft.appliedClause(PROJECT, node, program(clause));
            assertTrue(added.contains("fluid:water"), added);
            for (StudioResourceAlternatives.Row row : draft.resourceRows()) draft.removeResource(row.id);
            String removed = draft.appliedClause(PROJECT, node, program(clause));
            assertFalse(removed.contains("item:a"));
            assertFalse(removed.contains("fluid:water"));
            assertEquals(0, draft(node(removed), program(removed)).resourceCount());
            if (!clause.contains("item:a")) assertEquals(clause, removed);
        }
        assertEquals(
            "(\"source\"HAS=0)",
            StudioNodeProperties.update(Type.IF, "(\"src😀\"HAS=0)", "label", "\"source\""));
    }

    @Test
    void blankOrInvalidCellsRefuseTheWholeDraftAndSurviveCopies() {
        String clause = "\"a\",\"b\" HAS > 0 item:a OR item:b", source = program(clause);
        Node node = node(clause);
        for (boolean label : new boolean[] { true, false }) for (String invalid : new String[] { "", " ", "a,b",
            "item:a OR item:b", "a -- hide", "\"unfinished", "x?", "a\nb", "a\0b" }) {
                StudioFieldDraft draft = draft(node, source);
                UUID row = label ? draft.labelRows()
                    .get(0).id
                    : draft.resourceRows()
                        .get(0).id;
                if (label) draft.setLabel(row, invalid);
                else draft.setResource(row, invalid);
                draft.set("amount", "32");
                StudioFieldDraft copy = new StudioFieldDraft(draft);
                assertTrue(copy.dirty());
                assertEquals(invalid, label ? copy.labelValue(row) : copy.resourceValue(row));
                assertThrows(IllegalArgumentException.class, () -> copy.appliedClause(PROJECT, node, source), invalid);
                assertEquals(clause, node.clause());
                assertEquals("32", copy.value("amount"));
            }
        StudioFieldDraft single = draft(node("\"a\" HAS = 0"), program("\"a\" HAS = 0"));
        assertThrows(
            IllegalArgumentException.class,
            () -> single.removeLabel(
                single.labelRows()
                    .get(0).id));
        UUID blank = single.addResource("");
        assertTrue(single.dirty());
        assertThrows(
            IllegalArgumentException.class,
            () -> single.appliedClause(PROJECT, node("\"a\" HAS = 0"), program("\"a\" HAS = 0")));
        single.removeResource(blank);
        assertFalse(single.dirty());
    }

    @Test
    void joinedTextCannotHideInvalidRowPartitionsAcrossRecoveryCopies() {
        String clause = "\"a,b\",\"c\" HAS > 0 \"item:a OR b\" OR item:c", source = program(clause);
        Node node = node(clause);
        for (boolean label : new boolean[] { true, false }) {
            StudioFieldDraft draft = draft(node, source);
            String original = draft.value(label ? "label" : "resource");
            if (label) {
                draft.setLabel(
                    draft.labelRows()
                        .get(0).id,
                    "\"a");
                draft.setLabel(
                    draft.labelRows()
                        .get(1).id,
                    "b\",\"c\"");
            } else {
                draft.setResource(
                    draft.resourceRows()
                        .get(0).id,
                    "\"item:a");
                draft.setResource(
                    draft.resourceRows()
                        .get(1).id,
                    "b\" OR item:c");
            }
            assertEquals(original, draft.value(label ? "label" : "resource"));
            assertTrue(draft.dirty());
            StudioFieldDraft copy = new StudioFieldDraft(draft);
            assertTrue(copy.dirty());
            assertThrows(IllegalArgumentException.class, () -> copy.appliedClause(PROJECT, node, source));
            copy.set(label ? "label" : "resource", original);
            assertFalse(copy.dirty());
        }
    }

    @Test
    void bothPickersRepairTheirOwnRowAndUnrelatedPartitionsDoNotOverFence() {
        String clause = "\"a\",\"b\" HAS > 0 item:a OR item:b", source = program(clause);
        Node node = node(clause);
        for (boolean label : new boolean[] { true, false }) {
            StudioFieldDraft draft = draft(node, source);
            UUID row = label ? draft.labelRows()
                .get(0).id
                : draft.resourceRows()
                    .get(0).id;
            StudioFieldPicker.Session session = picker(draft, row, label);
            if (label) draft.addResource("fluid:water");
            else draft.addLabel("\"third\"");
            draft.set("amount", "9");
            assertTrue(session.choose(draft, PROJECT, node, source));
            assertEquals(
                label ? "\"spare\"" : "\"item:minecraft:gold_ingot\"",
                label ? draft.labelValue(row) : draft.resourceValue(row));
            assertTrue(
                draft.appliedClause(PROJECT, node, source)
                    .contains("HAS > 9"));
        }
    }

    @Test
    void stalePickersRefuseRemovedShiftedAbaAndWrongOwnersForBothKinds() {
        String clause = "\"a\",\"a\",\"a\" HAS > 0 item:a OR item:a OR item:a", source = program(clause);
        Node node = node(clause);
        for (boolean label : new boolean[] { true, false }) for (boolean aba : new boolean[] { true, false }) {
            StudioFieldDraft draft = draft(node, source);
            UUID target = label ? draft.labelRows()
                .get(1).id
                : draft.resourceRows()
                    .get(1).id;
            StudioFieldPicker.Session session = picker(draft, target, label);
            if (label) {
                draft.removeLabel(
                    aba ? target
                        : draft.labelRows()
                            .get(0).id);
                if (aba) draft.addLabel("\"a\"");
            } else {
                draft.removeResource(
                    aba ? target
                        : draft.resourceRows()
                            .get(0).id);
                if (aba) draft.addResource("item:a");
            }
            assertThrows(IllegalArgumentException.class, () -> session.choose(draft, PROJECT, node, source));
        }
        for (boolean label : new boolean[] { true, false }) {
            StudioFieldDraft draft = draft(node, source);
            UUID target = label ? draft.labelRows()
                .get(0).id
                : draft.resourceRows()
                    .get(0).id;
            StudioFieldPicker.Session session = picker(draft, target, label);
            assertThrows(
                IllegalArgumentException.class,
                () -> session.choose(new StudioFieldDraft(draft), PROJECT, node, source));
            assertThrows(IllegalArgumentException.class, () -> session.choose(draft, UUID.randomUUID(), node, source));
            assertThrows(IllegalArgumentException.class, () -> session.choose(draft, PROJECT, node, source + " "));
            assertThrows(IllegalArgumentException.class, () -> session.choose(draft, PROJECT, node(clause), source));
            if (label) draft.setLabel(target, "\"changed\"");
            else draft.setResource(target, "item:changed");
            assertThrows(IllegalArgumentException.class, () -> session.choose(draft, PROJECT, node, source));
        }
    }

    @Test
    void capsAreExplicitAndUnsupportedConditionsStayFullyManualWithoutTruncation() {
        String labels = String.join(",", Collections.nCopies(32, "\"a\"")),
            resources = String.join(" OR ", Collections.nCopies(32, "item:a"));
        String clause = labels + " HAS > 0 " + resources;
        StudioFieldDraft draft = draft(node(clause), program(clause));
        assertEquals(32, draft.labelCount());
        assertEquals(32, draft.resourceCount());
        assertThrows(IllegalArgumentException.class, () -> draft.addLabel("\"a\""));
        assertThrows(IllegalArgumentException.class, () -> draft.addResource("item:a"));
        for (String manual : new String[] { labels + ",a HAS > 0", "\"a\" HAS > 0 " + resources + " OR item:a",
            "\"a\" HAS > 0 item:a OR", "NOT \"a\",\"b\" HAS > 0", "\"a\" HAS > 0 AND \"b\" HAS > 0",
            "\"a\" HAS > 0 OR \"b\" HAS > 0", "TRUE", "REDSTONE" }) {
            assertTrue(
                StudioNodeProperties.inspect(Type.IF, manual)
                    .isEmpty(),
                manual);
            assertThrows(
                IllegalArgumentException.class,
                () -> StudioNodeProperties.update(Type.IF, manual, "amount", "1"));
        }
    }

    @Test
    void oneCombinedApplyKeepsNodeBranchIdentitiesAndExactUndoRedo() {
        String clause = "(SOME \"a\",\"b\" HAS > 0 item:a OR item:b)", source = program(clause);
        StudioDocument document = new StudioDocument();
        document.load(source);
        document.openVisual();
        Node node = document.graph()
            .nodes()
            .stream()
            .filter(n -> n.type == Type.IF)
            .findFirst()
            .get();
        String initial = document.source();
        java.util.List<UUID> ids = new java.util.ArrayList<>();
        for (Node n : document.graph()
            .nodes()) ids.add(n.id);
        StudioFieldDraft draft = draft(node, document.code());
        draft.addLabel("\"third\"");
        draft.addResource("fluid:water");
        draft.set("comparator", "!=");
        draft.set("amount", "2");
        String changed = draft.appliedClause(PROJECT, node, document.code());
        document.editGraph(
            g -> g.node(node.id)
                .setClause(changed));
        assertTrue(document.undo());
        assertEquals(initial, document.source());
        assertFalse(document.undo());
        assertTrue(document.redo());
        assertEquals(
            changed,
            document.graph()
                .node(node.id)
                .clause());
        java.util.List<UUID> after = new java.util.ArrayList<>();
        for (Node n : BlueprintDocument.open(document.source()).graph.nodes()) after.add(n.id);
        assertEquals(ids, after);
    }
}
