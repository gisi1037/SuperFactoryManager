package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;

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

class StudioAbsentIoAllowancesTest {

    private static final UUID PROJECT = new UUID(17, 1037);

    private static Node node(Type type, String clause) {
        return new Node(UUID.randomUUID(), type, 0, 0, clause);
    }

    private static String program(String clause) {
        return "every 20 ticks do " + clause + " end";
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
    void initialDefaultsAndDisabledLatentValuesPreserveExactSource() {
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT }) {
            Node node = node(
                type,
                type == Type.INPUT ? "FROM\"\u7bb1 \ud83c\udfed\" InPuT EXCEPT item:dirt"
                    : "TO EMPTY SLOTS IN EACH\"\u7bb1 \ud83c\udfed\" OuTpUt EXCEPT item:dirt");
            StudioFieldDraft draft = draft(node);
            assertTrue(draft.hasIoAllowances());
            assertFalse(draft.canAddIndependentLimit());
            assertFalse(draft.dirty());
            for (Part part : Part.values()) {
                assertFalse(draft.allowanceEnabled(part));
                assertFalse(draft.allowanceEach(part));
                assertEquals("", draft.allowanceText(part));
                draft.setAllowanceText(part, "\ninvalid latent");
                draft.setAllowanceEach(part, true);
            }
            StudioFieldDraft copy = new StudioFieldDraft(draft);
            assertEquals(node.clause(), apply(copy, node));
            assertTrue(copy.dirty());
            assertFalse(draft(node).dirty()); // Cancel/new inspector reconstructs only committed source.
            assertEquals(
                Long.MAX_VALUE,
                compiled(node.clause()).getQuantity()
                    .getAmount());
            assertEquals(
                type == Type.INPUT ? 0 : Long.MAX_VALUE,
                compiled(node.clause()).getRetention()
                    .getAmount());
        }
    }

    @Test
    void presenceAndEachMatrixInsertsOnlyAtKeywordInBothOrders() {
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT }) for (boolean first : new boolean[] { false, true })
            for (int enabled = 0; enabled < 4; enabled++) for (int each = 0; each < 4; each++) {
                String keyword = type == Type.INPUT ? "InPuT" : "OuTpUt";
                String access = (type == Type.INPUT ? "FROM" : "TO EMPTY SLOTS IN")
                    + " EACH \"\u7bb1 \ud83c\udfed, OR\",other ROUND ROBIN BY BLOCK North,Top SIDE SLOTS 0,2-8";
                String action = keyword + " eXcEpT item:dirt,fluid:lava";
                String clause = first ? access + "\t" + action : action + "\t" + access;
                Node node = node(type, clause);
                StudioFieldDraft draft = draft(node);
                String prefix = "";
                if ((enabled & 1) != 0) prefix = "0008" + ((each & 1) != 0 ? " EACH" : "");
                if ((enabled & 2) != 0)
                    prefix += (prefix.isEmpty() ? "" : " ") + "RETAIN 0002" + ((each & 2) != 0 ? " EACH" : "");
                for (Part part : Part.values()) {
                    int bit = part == Part.AMOUNT ? 1 : 2;
                    draft.setAllowanceEnabled(part, (enabled & bit) != 0);
                    draft.setAllowanceText(part, part == Part.AMOUNT ? "0008" : "0002");
                    draft.setAllowanceEach(part, (each & bit) != 0);
                }
                String changed = apply(draft, node);
                assertEquals(prefix.isEmpty() ? clause : clause.replace(keyword, keyword + " " + prefix), changed);
                Limit limit = compiled(changed);
                assertEquals(
                    (enabled & 1) != 0 ? 8 : Long.MAX_VALUE,
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
                assertFalse(draft.canAddIndependentLimit());
            }
    }

    @Test
    void explicitZeroAndMaxStayDistinctFromOmission() {
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT })
            for (String value : new String[] { "0", "9223372036854775807" }) {
                Node node = node(type, type == Type.INPUT ? "INPUT FROM source" : "TO dest OUTPUT");
                StudioFieldDraft draft = draft(node);
                for (Part part : Part.values()) {
                    draft.setAllowanceEnabled(part, true);
                    draft.setAllowanceText(part, value);
                }
                Limit limit = compiled(apply(draft, node));
                assertEquals(
                    Long.parseLong(value),
                    limit.getQuantity()
                        .getAmount());
                assertEquals(
                    Long.parseLong(value),
                    limit.getRetention()
                        .getAmount());
                for (Part part : Part.values()) draft.setAllowanceEnabled(part, false);
                assertEquals(node.clause(), apply(draft, node));
            }
    }

    @Test
    void invalidEnabledCellsBlockWholeApplyAndSurviveCopyAndRepair() {
        for (Part part : Part.values())
            for (String bad : new String[] { "", "-1", "+1", "9223372036854775808", "1 EACH", "1,2", "1 -- comment",
                "\n5", "5\r", "\0" + "5", String.join("", Collections.nCopies(1500, "(")) + "1" }) {
                    Node node = node(Type.INPUT, "INPUT EXCEPT item:dirt FROM source");
                    StudioFieldDraft draft = draft(node);
                    draft.setAllowanceEnabled(part, true);
                    draft.setAllowanceText(part, bad);
                    draft.setLabel(
                        draft.labelRows()
                            .get(0).id,
                        "renamed");
                    StudioFieldDraft copy = new StudioFieldDraft(draft);
                    UUID revision = copy.labelRevision();
                    assertThrows(IllegalArgumentException.class, () -> apply(copy, node), bad);
                    assertEquals(bad, copy.allowanceText(part));
                    assertEquals(revision, copy.labelRevision());
                    assertEquals(
                        "renamed",
                        copy.labelRows()
                            .get(0).text);
                    assertEquals(node.clause(), draft.original);
                    copy.setAllowanceText(part, "3");
                    assertTrue(apply(copy, node).contains("renamed"));
                    assertEquals(bad, draft.allowanceText(part));
                }
    }

    @Test
    void numbersComposeWithResourcesOreExclusionsAndAllGlobalFields() {
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT }) {
            Node node = node(
                type,
                (type == Type.INPUT ? "FROM" : "TO EMPTY SLOTS IN")
                    + " EACH \"source \ud83c\udfed\",spare ROUND ROBIN BY BLOCK North SIDE SLOTS 0-8 "
                    + (type == Type.INPUT ? "INPUT" : "OUTPUT"));
            StudioFieldDraft draft = draft(node);
            draft.setAllowanceEnabled(Part.AMOUNT, true);
            draft.setAllowanceText(Part.AMOUNT, "8");
            draft.setAllowanceEnabled(Part.RETAIN, true);
            draft.setAllowanceText(Part.RETAIN, "2");
            draft.addResource("item:a");
            UUID second = draft.addResource("fluid:water");
            draft.setOreMode(StudioOreFilter.Mode.WITHOUT);
            draft.setOrePattern("dust*");
            draft.addExclusion("item:dirt");
            draft.setLabel(
                draft.labelRows()
                    .get(0).id,
                "\"renamed \ud83c\udfed\"");
            draft.set("each", "false");
            draft.set("roundRobin", "label");
            draft.set("sides", "SOUTH");
            draft.set("slots", "1-3");
            if (type == Type.OUTPUT) draft.set("emptySlots", "false");
            String changed = apply(draft, node);
            assertTrue(changed.contains("8 RETAIN 2 item:a OR fluid:water WITHOUT #dust*  EXCEPT item:dirt"), changed);
            assertTrue(
                changed.contains("\"renamed \ud83c\udfed\",spare ROUND ROBIN BY LABEL SOUTH SIDE SLOTS 1-3"),
                changed);
            assertEquals(
                8,
                compiled(changed).getQuantity()
                    .getAmount());
            draft.setResource(second, "");
            assertThrows(IllegalArgumentException.class, () -> apply(draft, node));
            assertEquals(node.clause(), draft.original);
        }
    }

    @Test
    void allDisabledNumbersComposeWithResourceAndOreOnlyEditsWithoutInventedDefaults() {
        for (String selector : new String[] { "", "item:a" }) for (boolean filter : new boolean[] { false, true }) {
            Node node = node(Type.INPUT, "INPUT FROM source");
            StudioFieldDraft draft = draft(node);
            if (!selector.isEmpty()) draft.addResource(selector);
            if (filter) {
                draft.setOreMode(StudioOreFilter.Mode.WITH);
                draft.setOrePattern("ingot*");
            }
            String changed = apply(draft, node);
            assertFalse(changed.contains("RETAIN"));
            assertEquals(
                Long.MAX_VALUE,
                compiled(changed).getQuantity()
                    .getAmount());
            assertEquals(
                0,
                compiled(changed).getRetention()
                    .getAmount());
            assertFalse(draft.canAddIndependentLimit());
            if (selector.isEmpty() && !filter) assertEquals(node.clause(), changed);
            else assertTrue(draft(node(Type.INPUT, changed)).canAddIndependentLimit());
        }
    }

    @Test
    void absentPathDoesNotBroadenExistingSingleHelpersOrPromotion() {
        StudioIoAllowances values = StudioIoAllowances.absent();
        values.setEnabled(Part.AMOUNT, true);
        values.setLiteral(Part.AMOUNT, "8");
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties.updateSingleAllowances(Type.INPUT, "INPUT FROM source", values));
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties.updateAbsentAllowances(Type.INPUT, "INPUT item:a FROM source", values));
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties.updateAbsentAllowances(Type.IF, "source HAS > 0", values));
        Node node = node(Type.INPUT, "INPUT FROM source");
        StudioFieldDraft draft = draft(node);
        draft.setAllowanceEnabled(Part.AMOUNT, true);
        draft.setAllowanceText(Part.AMOUNT, "8");
        draft.addResource("item:a");
        assertFalse(draft.canAddIndependentLimit());
        assertThrows(IllegalArgumentException.class, () -> draft.addIndependentLimit(PROJECT, node, draft.source));
        assertThrows(IllegalArgumentException.class, () -> draft.set("amount", "9"));
        String changed = apply(draft, node);
        assertTrue(draft(node(Type.INPUT, changed)).canAddIndependentLimit());
    }

    @Test
    void resourcePickerAndOriginalOwnerFencesRemainValidWithNewNumberModes() {
        Node node = node(Type.INPUT, "INPUT FROM source");
        StudioFieldDraft draft = draft(node);
        UUID resource = draft.addResource("item:a");
        StudioFieldPicker.Target target = StudioFieldPicker.resourceRowTarget(draft, resource);
        StudioFieldPicker.Session picker = new StudioFieldPicker.Session(
            draft,
            target,
            StudioFieldPicker.items(Collections.singletonList("minecraft:coal"), target));
        picker.select(picker.snapshot.search("coal").entries.get(0));
        draft.setAllowanceEnabled(Part.AMOUNT, true);
        draft.setAllowanceText(Part.AMOUNT, "8");
        picker.choose(draft, PROJECT, node, draft.source);
        assertTrue(apply(draft, node).contains("8 \"item:minecraft:coal\""));
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(UUID.randomUUID(), node, draft.source));
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, draft.source + " "));
        assertThrows(
            IllegalArgumentException.class,
            () -> draft.appliedClause(PROJECT, node(Type.INPUT, node.clause()), draft.source));
    }

    @Test
    void sourceHistoryIsOneAtomicEditWithStableNodeAndEdgeIdentity() {
        StudioDocument document = new StudioDocument();
        String source = "-- \ud83c\udfed\r\nevery 20 ticks do INPUT FROM source OUTPUT TO dest end";
        document.load(source);
        document.openVisual();
        Node node = document.graph()
            .nodes()
            .stream()
            .filter(n -> n.type == Type.INPUT)
            .findFirst()
            .get();
        int nodes = document.graph()
            .nodes()
            .size(),
            edges = document.graph()
                .edges()
                .size();
        StudioFieldDraft draft = new StudioFieldDraft(
            PROJECT,
            node,
            document.code(),
            StudioNodeProperties.inspect(node.type, node.clause()));
        draft.setAllowanceEnabled(Part.AMOUNT, true);
        draft.setAllowanceText(Part.AMOUNT, "8");
        draft.addResource("item:a");
        draft.setLabel(
            draft.labelRows()
                .get(0).id,
            "renamed");
        assertEquals(source, document.source());
        assertFalse(document.undo());
        String clause = draft.appliedClause(PROJECT, node, document.code());
        document.editGraph(
            g -> g.node(node.id)
                .setClause(clause));
        String after = document.source();
        assertNotNull(
            document.graph()
                .node(node.id));
        assertEquals(
            nodes,
            document.graph()
                .nodes()
                .size());
        assertEquals(
            edges,
            document.graph()
                .edges()
                .size());
        assertTrue(document.undo());
        assertEquals(source, document.source());
        assertFalse(document.undo());
        assertTrue(document.redo());
        assertEquals(after, document.source());
    }

    @Test
    void oversizedAggregateAndManualFormsRemainBoundedAndAtomic() {
        Node node = node(Type.INPUT, "INPUT FROM source");
        StudioFieldDraft draft = draft(node);
        String zeros = String.join("", Collections.nCopies(3000, "0"));
        for (Part part : Part.values()) {
            draft.setAllowanceEnabled(part, true);
            draft.setAllowanceText(part, zeros);
        }
        assertThrows(IllegalArgumentException.class, () -> apply(draft, node));
        assertEquals(zeros, draft.allowanceText(Part.AMOUNT));
        assertEquals(node.clause(), draft.original);
        for (String clause : new String[] { "INPUT 4 item:a, FROM source", "INPUT item:a,item:b FROM source",
            "INPUT (1+2) item:a FROM source",
            "INPUT " + String.join(" OR ", Collections.nCopies(33, "item:a")) + " FROM source" }) {
            StudioFieldDraft manual = draft(node(Type.INPUT, clause));
            assertFalse(manual.hasIoAllowances());
            assertThrows(IllegalArgumentException.class, () -> manual.setAllowanceEnabled(Part.AMOUNT, true));
        }
    }
}
