package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintDocument;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioDocument;
import ca.teamdman.sfm.client.blueprint.StudioDraftDiagnostics;
import ca.teamdman.sfm.client.blueprint.StudioFieldDraft;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties.Field;
import ca.teamdman.sfm.client.blueprint.StudioOreFilter;
import ca.teamdman.sfm.client.blueprint.StudioOreFilter.Mode;
import ca.teamdman.sfm.client.blueprint.StudioSourceMap;
import ca.teamdman.sfml.ast.InputStatement;
import ca.teamdman.sfml.ast.OutputStatement;
import ca.teamdman.sfml.ast.ResourceLimit;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class StudioOreFilterTest {

    private static final UUID PROJECT = new UUID(6, 1039);

    private static String program(String clause) {
        return "every 20 ticks do " + clause + " end";
    }

    private static Field filter(Type type, String clause) {
        return StudioNodeProperties.inspect(type, clause)
            .stream()
            .filter(
                f -> f.getKey()
                    .equals("oreFilter"))
            .findFirst()
            .orElse(null);
    }

    private static StudioFieldDraft draft(Node node, String source) {
        return new StudioFieldDraft(PROJECT, node, source, StudioNodeProperties.inspect(node.type, node.clause()));
    }

    private static String edit(Type type, String clause, String filter) {
        return StudioNodeProperties.update(type, clause, "oreFilter", filter);
    }

    private static ResourceLimit limit(String clause) {
        ProgramBuildResult built = new ProgramBuilder(program(clause)).build();
        assertTrue(built.isSuccess(), built.getErrorMessage());
        Object statement = built.getProgram()
            .getTriggers()
            .get(0)
            .getBlock()
            .getStatements()
            .get(0);
        return (statement instanceof InputStatement ? ((InputStatement) statement).getResourceLimits()
            : ((OutputStatement) statement).getResourceLimits()).getLimits()
                .get(0);
    }

    private static void sameDefaultsAndSelectors(ResourceLimit a, ResourceLimit b) {
        assertEquals(
            a.getLimit()
                .getQuantity()
                .getAmount(),
            b.getLimit()
                .getQuantity()
                .getAmount());
        assertEquals(
            a.getLimit()
                .getQuantity()
                .isEach(),
            b.getLimit()
                .getQuantity()
                .isEach());
        assertEquals(
            a.getLimit()
                .getRetention()
                .getAmount(),
            b.getLimit()
                .getRetention()
                .getAmount());
        assertEquals(
            a.getLimit()
                .getRetention()
                .isEach(),
            b.getLimit()
                .getRetention()
                .isEach());
        assertEquals(
            a.getResourceIds()
                .toString(),
            b.getResourceIds()
                .toString());
    }

    @Test
    void exactPatternAndPolarityEditsKeepIntroducersUnicodeAndAllOtherSpans() {
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT })
            for (boolean accessFirst : new boolean[] { false, true }) {
                String action = (type == Type.INPUT ? "InPuT" : "oUtPuT")
                    + " 16 EACH RETAIN 3 EACH \"item:Mod:Part:4\" wItH\tTaG #ingotIron EXCEPT item:minecraft:dirt";
                String access = (type == Type.INPUT ? "FROM" : "TO EMPTY SLOTS IN")
                    + " EACH \"箱 🏭\",\"箱 🏭\" ROUND ROBIN BY LABEL North,Top SIDE SLOTS 0,2-8";
                String clause = accessFirst ? access + "  " + action : action + "  " + access;
                assertNotNull(filter(type, clause));
                assertEquals(clause, edit(type, clause, "WITH #ingotIron"));
                assertEquals(clause.replace("ingotIron", "ingotCopper"), edit(type, clause, "WITH #ingotCopper"));
                String negated = edit(type, clause, "WITHOUT #ingotIron");
                assertEquals(clause.replace("wItH", "WITHOUT"), negated);
                sameDefaultsAndSelectors(limit(clause), limit(negated));
                assertTrue(
                    limit(negated).getWith()
                        .isNegated());
                assertEquals(clause.replace("wItH\tTaG #ingotIron", ""), edit(type, clause, ""));
            }
    }

    @Test
    void insertRemoveAcrossAbsentSelectorQuantityRetentionAndWithOnlyPreservesDefaults() {
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT })
            for (boolean accessFirst : new boolean[] { false, true }) for (String rest : new String[] { "", "16",
                "RETAIN 4 EACH", "16 EACH RETAIN 4 EACH", "item:minecraft:iron_ingot",
                "16 EACH item:minecraft:iron_ingot", "WITH #ingotIron", "WITHOUT TAG ingotIron" }) {
                    String action = (type == Type.INPUT ? "INPUT" : "OUTPUT") + (rest.isEmpty() ? "" : " " + rest)
                        + " EXCEPT item:minecraft:dirt";
                    String access = type == Type.INPUT ? "FROM EACH source" : "TO EMPTY SLOTS IN EACH dest";
                    String clause = accessFirst ? access + " " + action : action + " " + access;
                    String changed = edit(type, clause, "WITHOUT #ingot*");
                    String removed = edit(type, changed, "");
                    sameDefaultsAndSelectors(limit(clause), limit(changed));
                    sameDefaultsAndSelectors(limit(clause), limit(removed));
                    assertNull(
                        limit(removed).getWith()
                            .getTagMatcher());
                    if (rest.isEmpty() || rest.startsWith("WITH")) {
                        assertEquals(
                            Long.MAX_VALUE,
                            limit(removed).getLimit()
                                .getQuantity()
                                .getAmount());
                        assertEquals(
                            type == Type.INPUT ? 0 : Long.MAX_VALUE,
                            limit(removed).getLimit()
                                .getRetention()
                                .getAmount());
                    }
                    assertTrue(removed.contains("EXCEPT item:minecraft:dirt"));
                }
    }

    @Test
    void compactQuotedSelectorBoundariesAreSafeAndOnlyNecessarySpacesAreInserted() {
        for (String clause : new String[] { "INPUT\"item:a\"WITH#ingotIron FROM\"箱😀\"",
            "FROM\"箱😀\"INPUT\"item:a\"WITH#ingotIron", "OUTPUT\"item:a\"WITHOUT#ingotIron TO\"箱😀\"",
            "TO\"箱😀\"OUTPUT\"item:a\"WITHOUT#ingotIron" }) {
            Type type = clause.contains("INPUT") ? Type.INPUT : Type.OUTPUT;
            String removed = edit(type, clause, "");
            assertEquals(
                clause.replace(clause.contains("WITHOUT") ? "WITHOUT#ingotIron" : "WITH#ingotIron", ""),
                removed);
            sameDefaultsAndSelectors(limit(clause), limit(removed));
            assertEquals(
                clause.replace("ingotIron", "ingot*"),
                edit(type, clause, clause.contains("WITHOUT") ? "WITHOUT #ingot*" : "WITH #ingot*"));
        }
    }

    @Test
    void ambiguousListsAndCompoundFormsStayManualWithoutDroppingOtherSafeFields() {
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT }) for (String rest : new String[] { "16 item:a,2 item:b",
            "16 item:a OR WITH #oreName", "16 item:a,", "WITH #oreName,", "16," }) {
                String clause = (type == Type.INPUT ? "INPUT " : "OUTPUT ") + rest
                    + (type == Type.INPUT ? " FROM first,second" : " TO first,second");
                limit(clause);
                assertNull(filter(type, clause), clause);
                assertFalse(
                    StudioNodeProperties.inspect(type, clause)
                        .isEmpty());
                assertThrows(IllegalArgumentException.class, () -> edit(type, clause, "WITH #other"));
            }
        for (String value : new String[] { "WITH (#a)", "WITH NOT #a", "WITH #a AND #b", "WITH #a OR #b" }) {
            assertThrows(IllegalArgumentException.class, () -> new StudioOreFilter(value));
            assertNull(filter(Type.INPUT, "INPUT " + value + " FROM source"));
        }
        String labels = IntStream.range(0, 32)
            .mapToObj(i -> "label_" + i)
            .collect(Collectors.joining(","));
        assertNotNull(filter(Type.INPUT, "INPUT FROM " + labels));
        assertNull(filter(Type.INPUT, "INPUT FROM " + labels + ",last_label"));
    }

    @Test
    void enabledBlankInvalidOrInjectedPatternRefusesWhileOffRetainsInvalidPendingText() {
        String clause = "INPUT 16 FROM source", source = program(clause);
        Node node = new Node(UUID.randomUUID(), Type.INPUT, 0, 0, clause);
        for (String pattern : new String[] { "", " ", "ingot?", "ingot.*", "#ingotIron", "ingotIron OR #dustIron",
            "a -- comment", "a\nb", "a\rb", "a\0b", "a FROM dest", "\"ingotIron\"", "a\\b", "😀" }) {
            StudioFieldDraft draft = draft(node, source);
            draft.setOrePattern(pattern);
            draft.setOreMode(Mode.WITH);
            assertTrue(draft.dirty());
            assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, source), pattern);
            assertEquals(clause, node.clause());
            draft.setOreMode(Mode.OFF);
            assertEquals(pattern, draft.orePattern());
            assertEquals(clause, draft.appliedClause(PROJECT, node, source));
            assertEquals(!pattern.isEmpty(), draft.dirty());
        }
    }

    @Test
    void offPatternControlStateCopiesAndRevertsIndependentlyOfSerializedText() {
        String clause = "OUTPUT TO dest", source = program(clause);
        Node node = new Node(UUID.randomUUID(), Type.OUTPUT, 0, 0, clause);
        StudioFieldDraft draft = draft(node, source);
        assertEquals("", draft.value("oreFilter"));
        draft.setOrePattern("ingot?");
        assertEquals("", draft.value("oreFilter"));
        assertTrue(draft.dirty());
        StudioFieldDraft retained = new StudioFieldDraft(draft);
        assertEquals("ingot?", retained.orePattern());
        assertEquals(Mode.OFF, retained.oreMode());
        assertTrue(retained.dirty());
        retained.setOreMode(Mode.WITHOUT);
        assertThrows(IllegalArgumentException.class, () -> retained.appliedClause(PROJECT, node, source));
        assertEquals(Mode.OFF, draft.oreMode());
        draft.setOrePattern("");
        assertFalse(draft.dirty());
        assertEquals(clause, draft.appliedClause(PROJECT, node, source));
        assertThrows(IllegalArgumentException.class, () -> draft.setOreMode(null));
    }

    @Test
    void returningToOriginalControlsRestoresExactIntroducerWithoutFalseDirty() {
        String clause = "INPUT wItHoUt TaG#ingotIron FROM source", source = program(clause);
        Node node = new Node(UUID.randomUUID(), Type.INPUT, 0, 0, clause);
        StudioFieldDraft draft = draft(node, source);
        draft.setOreMode(Mode.OFF);
        draft.setOrePattern("dust*");
        draft.setOreMode(Mode.WITHOUT);
        draft.setOrePattern("ingotIron");
        assertFalse(draft.dirty());
        assertEquals("wItHoUt TaG#ingotIron", draft.value("oreFilter"));
        assertEquals(clause, draft.appliedClause(PROJECT, node, source));
    }

    @Test
    void combinedEditsReparseFilterAfterResourceStructuralChangesAndNeverPartiallyApply() {
        for (Type type : new Type[] { Type.INPUT, Type.OUTPUT })
            for (String initial : new String[] { "", "WITH #ingotIron", "item:a", "item:a WITHOUT #dust*" }) {
                String clause = (type == Type.INPUT ? "INPUT " : "OUTPUT ") + initial
                    + (type == Type.INPUT ? " FROM a,b" : " TO a,b");
                String source = program(clause);
                Node node = new Node(UUID.randomUUID(), type, 0, 0, clause);
                StudioFieldDraft draft = draft(node, source);
                UUID first = draft.labelRows()
                    .get(0).id;
                UUID revision = draft.labelRevision();
                draft.setLabel(first, "edited");
                draft.set("resource", "item:minecraft:gold_ingot");
                draft.setOreMode(Mode.WITHOUT);
                draft.setOrePattern("");
                assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, source));
                assertEquals(clause, node.clause());
                StudioFieldDraft retained = new StudioFieldDraft(draft);
                assertEquals(
                    first,
                    retained.labelRows()
                        .get(0).id);
                assertEquals(revision, retained.labelRevision());
                retained.setOrePattern("ingot*");
                String changed = retained.appliedClause(PROJECT, node, source);
                assertTrue(changed.contains("edited,b"));
                assertTrue(changed.contains("item:minecraft:gold_ingot"));
                assertTrue(
                    limit(changed).getWith()
                        .isNegated());
                assertEquals(
                    "ingot*",
                    limit(changed).getWith()
                        .getTagMatcher()
                        .getPatternString());
                retained.set("resource", "");
                retained.setOreMode(Mode.OFF);
                String removed = retained.appliedClause(PROJECT, node, source);
                assertNull(
                    limit(removed).getWith()
                        .getTagMatcher());
                assertEquals(
                    type == Type.INPUT ? 0 : Long.MAX_VALUE,
                    limit(removed).getLimit()
                        .getRetention()
                        .getAmount());
                assertEquals("", draft.orePattern());
            }
    }

    @Test
    void staleOwnerSourceAndNodeCannotApplyCopiedFilterControls() {
        String clause = "INPUT FROM source", source = program(clause);
        Node node = new Node(UUID.randomUUID(), Type.INPUT, 0, 0, clause);
        StudioFieldDraft draft = draft(node, source);
        draft.setOreMode(Mode.WITH);
        draft.setOrePattern("ingot*");
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(new UUID(8, 8), node, source));
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, source + " "));
        assertThrows(
            IllegalArgumentException.class,
            () -> draft.appliedClause(PROJECT, new Node(UUID.randomUUID(), Type.INPUT, 0, 0, clause), source));
        node.setClause("INPUT FROM other");
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, source));
        assertEquals("ingot*", draft.orePattern());
    }

    @Test
    void atomicApplyKeepsUuidSourceNavigationAndSingleUndoWithPendingDiagnostics() {
        String source = "-- 🏭\r\n" + program("INPUT 16 RETAIN 2 FROM a,b OUTPUT TO dest");
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
        draft.setOreMode(Mode.WITH);
        draft.setOrePattern("");
        draft.set("amount", "32");
        StudioDraftDiagnostics diagnostics = new StudioDraftDiagnostics();
        diagnostics.pendingChanged(draft.dirty());
        assertTrue(diagnostics.tick());
        assertTrue(diagnostics.check(document, draft.dirty()).message.startsWith("Inspector"));
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, document.code()));
        assertEquals(source, document.source());
        assertFalse(document.undo());
        draft.setOrePattern("ingot*");
        String applied = draft.appliedClause(PROJECT, node, document.code());
        document.editGraph(
            g -> g.node(node.id)
                .setClause(applied));
        assertNotNull(BlueprintDocument.open(document.source()).graph.node(node.id));
        StudioSourceMap fresh = StudioSourceMap
            .prepare(PROJECT, document, StudioSourceMap.normalize(document.code()), false, false, false);
        assertEquals(
            node.id,
            fresh.atCaret(
                document.code()
                    .indexOf("ingot*")).node);
        assertTrue(document.undo());
        assertEquals(source, document.source());
        assertFalse(document.undo());
        assertTrue(document.redo());
        assertEquals(
            applied,
            document.graph()
                .node(node.id)
                .clause());
        diagnostics.pendingChanged(false);
        assertTrue(diagnostics.tick());
        assertTrue(diagnostics.check(document, false).message.startsWith("Draft: valid"));
    }
}
