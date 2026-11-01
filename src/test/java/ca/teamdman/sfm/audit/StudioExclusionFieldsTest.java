package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintDocument;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioDocument;
import ca.teamdman.sfm.client.blueprint.StudioFieldDraft;
import ca.teamdman.sfm.client.blueprint.StudioFieldPicker;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties.Field;
import ca.teamdman.sfm.client.blueprint.StudioOreFilter.Mode;
import ca.teamdman.sfml.ast.InputStatement;
import ca.teamdman.sfml.ast.OutputStatement;
import ca.teamdman.sfml.ast.ResourceLimits;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class StudioExclusionFieldsTest {

    private static final UUID PROJECT = new UUID(8, 1041);

    private static String program(String clause) {
        return "every 20 ticks do " + clause + " end";
    }

    private static Field field(Type type, String clause) {
        return StudioNodeProperties.inspect(type, clause)
            .stream()
            .filter(
                f -> f.getKey()
                    .equals("exclusion"))
            .findFirst()
            .orElse(null);
    }

    private static String edit(Type type, String clause, String value) {
        return StudioNodeProperties.update(type, clause, "exclusion", value);
    }

    private static StudioFieldDraft draft(Node node, String source) {
        return new StudioFieldDraft(PROJECT, node, source, StudioNodeProperties.inspect(node.type, node.clause()));
    }

    private static ResourceLimits limits(String clause) {
        ProgramBuildResult built = new ProgramBuilder(program(clause)).build();
        assertTrue(built.isSuccess(), built.getErrorMessage());
        Object statement = built.getProgram()
            .getTriggers()
            .get(0)
            .getBlock()
            .getStatements()
            .get(0);
        return statement instanceof InputStatement ? ((InputStatement) statement).getResourceLimits()
            : ((OutputStatement) statement).getResourceLimits();
    }

    private static String access(Type type) {
        return (type == Type.INPUT ? "FROM" : "TO EMPTY SLOTS IN")
            + " EACH \"箱 🏭\",\"箱 🏭\" ROUND ROBIN BY LABEL North,Top SIDE SLOTS 0,2-8";
    }

    @Test
    void bothOrdersPreserveExactKeywordGapsInclusionOreRoutingAndUnicode() {
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT }) for (boolean first : new boolean[] { false, true }) {
            String action = (type == Type.INPUT ? "InPuT" : "oUtPuT")
                + " 16 EACH RETAIN 3 EACH item:Mod:Part wItHoUt TAG#ingotIron eXcEpT\t\"item:Old:Part:4\"";
            String clause = first ? access(type) + "  " + action : action + "  " + access(type);
            assertEquals("\"item:Old:Part:4\"", field(type, clause).getValue());
            String changed = edit(type, clause, "\"item:New:Part:7\"");
            assertEquals(clause.replace("\"item:Old:Part:4\"", "\"item:New:Part:7\""), changed);
            assertEquals(
                limits(clause).getLimits()
                    .toString(),
                limits(changed).getLimits()
                    .toString());
            String removed = edit(type, changed, "");
            assertEquals(changed.replace("eXcEpT\t\"item:New:Part:7\"", ""), removed);
            assertNull(limits(removed).getExclusions());
            assertEquals(clause, edit(type, clause, field(type, clause).getValue()));
        }
    }

    @Test
    void absentInclusionAndFilterOnlyInsertionsRemovalsPreserveDirectionalDefaults() {
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT }) for (boolean first : new boolean[] { false, true })
            for (String rest : new String[] { "", "16", "RETAIN 2 EACH", "16 EACH RETAIN 2 EACH", "WITH #ingotIron",
                "WITHOUT #dustIron", "item:minecraft:iron_ingot" }) {
                    String action = (type == Type.INPUT ? "INPUT" : "OUTPUT") + (rest.isEmpty() ? "" : " " + rest);
                    String clause = first ? access(type) + " " + action : action + " " + access(type);
                    String added = edit(type, clause, "fluid:water"), removed = edit(type, added, "");
                    assertEquals(
                        limits(clause).getLimits()
                            .toString(),
                        limits(added).getLimits()
                            .toString());
                    assertEquals(
                        limits(clause).getLimits()
                            .toString(),
                        limits(removed).getLimits()
                            .toString());
                    assertNull(limits(removed).getExclusions());
                    if (rest.isEmpty() || rest.startsWith("WITH")) {
                        assertEquals(
                            Long.MAX_VALUE,
                            limits(removed).getLimits()
                                .get(0)
                                .getLimit()
                                .getQuantity()
                                .getAmount());
                        assertEquals(
                            type == Type.INPUT ? 0 : Long.MAX_VALUE,
                            limits(removed).getLimits()
                                .get(0)
                                .getLimit()
                                .getRetention()
                                .getAmount());
                    }
                    assertEquals(clause, edit(type, clause, ""));
                }
    }

    @Test
    void globalExclusionIsIndependentOfAmbiguousInclusionLimits() {
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT }) for (String inclusion : new String[] {
            "16 item:a,2 item:b", "item:a OR item:b", "item:a OR", "16 item:a,", "WITH #ingotIron," }) {
                String clause = (type == Type.INPUT ? "INPUT " : "OUTPUT ") + inclusion
                    + (type == Type.INPUT ? " FROM \"source\"" : " TO \"dest\"");
                assertNotNull(field(type, clause));
                String added = edit(type, clause, "item:c");
                assertEquals(
                    limits(clause).getLimits()
                        .toString(),
                    limits(added).getLimits()
                        .toString());
                assertEquals(
                    1,
                    limits(added).getExclusions()
                        .getIdentifiers()
                        .size());
                assertEquals(added.replace("item:c", "item:d"), edit(type, added, "item:d"));
                assertNull(limits(edit(type, added, "")).getExclusions());
            }
    }

    @Test
    void trailingExclusionsStayManualButOtherFieldsRemain() {
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT })
            for (String excluded : new String[] { "item:a,", "item:a,item:b," }) {
                String clause = (type == Type.INPUT ? "INPUT 16 EXCEPT " : "OUTPUT 16 EXCEPT ") + excluded
                    + (type == Type.INPUT ? " FROM \"source\"" : " TO \"dest\"");
                limits(clause);
                assertNull(field(type, clause));
                assertFalse(
                    StudioNodeProperties.inspect(type, clause)
                        .isEmpty());
                assertThrows(IllegalArgumentException.class, () -> edit(type, clause, "item:c"));
                assertEquals(clause.replace("16", "32"), StudioNodeProperties.update(type, clause, "amount", "32"));
            }
    }

    @Test
    void compactQuotedSpansAddOnlyTokenSafeSpacesAndRemoveWholeKeyword() {
        for (String clause : new String[] { "INPUT\"item:a\"EXCEPT\"item:b\"FROM\"src😀\"",
            "FROM\"src😀\"INPUT EXCEPT\"item:b\"", "OUTPUT\"item:a\"EXCEPT\"item:b\"TO\"dest😀\"",
            "TO\"dest😀\"OUTPUT EXCEPT\"item:b\"" }) {
            Type type = clause.contains("INPUT") ? Type.INPUT : Type.OUTPUT;
            String changed = edit(type, clause, "item:c");
            assertEquals(
                clause.replace("EXCEPT\"item:b\"", "EXCEPT item:c" + (clause.endsWith("\"item:b\"") ? "" : " ")),
                changed);
            String removed = edit(type, clause, "");
            assertEquals(clause.replace("EXCEPT\"item:b\"", ""), removed);
            assertFalse(removed.contains("EXCEPT"));
            limits(changed);
            limits(removed);
        }
    }

    @Test
    void invalidOrInjectedPendingTextIsRetainedAndApplyIsAtomic() {
        String clause = "INPUT 16 FROM \"a\",\"b\"", source = program(clause);
        Node node = new Node(UUID.randomUUID(), Type.INPUT, 0, 0, clause);
        for (String invalid : new String[] { "item:a,item:b", "item:a OR item:b", "item:a -- comment",
            "item:a WITH #ingotIron", "EXCEPT item:a", "\"unfinished", "a?", "a.b", "a\nb", "a\rb", "a\0b" }) {
            StudioFieldDraft draft = draft(node, source);
            draft.set("amount", "32");
            draft.set("exclusion", invalid);
            StudioFieldDraft retained = new StudioFieldDraft(draft);
            assertEquals(invalid, retained.value("exclusion"));
            assertThrows(IllegalArgumentException.class, () -> retained.appliedClause(PROJECT, node, source), invalid);
            assertEquals(clause, node.clause());
            assertEquals("32", retained.value("amount"));
        }
    }

    @Test
    void combinedLabelsResourceOreAndExclusionApplyOnceWithExactUndo() {
        String source = "-- 🏭\r\n" + program("INPUT 16 RETAIN 2 FROM \"a\",\"b\" OUTPUT TO \"dest\"");
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
        UUID row = draft.labelRows()
            .get(0).id;
        draft.setLabel(row, "\"changed\"");
        draft.set("amount", "32");
        draft.set("resource", "item:minecraft:iron_ingot");
        draft.setOreMode(Mode.WITHOUT);
        draft.setOrePattern("dust*");
        draft.set("exclusion", "item:a,item:b");
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, document.code()));
        assertEquals(source, document.source());
        assertFalse(document.undo());
        StudioFieldDraft retained = new StudioFieldDraft(draft);
        retained.set("exclusion", "item:minecraft:coal");
        assertEquals(
            row,
            retained.labelRows()
                .get(0).id);
        assertEquals(draft.labelRevision(), retained.labelRevision());
        String applied = retained.appliedClause(PROJECT, node, document.code());
        assertEquals(
            "INPUT 32 RETAIN 2 item:minecraft:iron_ingot WITHOUT #dust*  EXCEPT item:minecraft:coal   FROM \"changed\" , \"b\"",
            applied);
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
    void pickerUsesExistingEncodingBudgetsAndPendingOwnershipFences() {
        String clause = "OUTPUT TO \"dest\"", source = program(clause);
        Node node = new Node(UUID.randomUUID(), Type.OUTPUT, 0, 0, clause);
        StudioFieldDraft draft = draft(node, source);
        Field field = field(Type.OUTPUT, clause);
        StudioFieldPicker.Target target = StudioFieldPicker.target(Type.OUTPUT, field, "");
        assertNotNull(target);
        StudioFieldPicker.Snapshot snapshot = StudioFieldPicker
            .items(Arrays.asList("minecraft:iron_ingot", "minecraft:gold_ingot"), target);
        StudioFieldPicker.Session session = new StudioFieldPicker.Session(draft, target, snapshot);
        session.select(snapshot.search("minecraft:gold_ingot").entries.get(0));
        assertFalse(draft.dirty());
        assertEquals("", draft.value("exclusion")); // Selection/Cancel is modal-local until Choose.
        assertTrue(session.choose(draft, PROJECT, node, source));
        assertEquals("\"item:minecraft:gold_ingot\"", draft.value("exclusion"));
        assertEquals(clause, node.clause());
        assertThrows(IllegalArgumentException.class, () -> session.choose(draft, PROJECT, node, source));
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(new UUID(9, 9), node, source));
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, source + " "));
        assertThrows(
            IllegalArgumentException.class,
            () -> draft.appliedClause(PROJECT, new Node(UUID.randomUUID(), Type.OUTPUT, 0, 0, clause), source));
        assertTrue(
            draft.appliedClause(PROJECT, node, source)
                .contains("EXCEPT \"item:minecraft:gold_ingot\""));
        draft.set("exclusion", "");
        assertFalse(draft.dirty());
        assertEquals(clause, draft.appliedClause(PROJECT, node, source));
    }
}
