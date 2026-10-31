package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.client.blueprint.BlueprintDocument;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioConditionSyntax;
import ca.teamdman.sfm.client.blueprint.StudioDocument;
import ca.teamdman.sfm.client.blueprint.StudioFieldDraft;
import ca.teamdman.sfm.client.blueprint.StudioFieldFeedback;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfm.client.blueprint.StudioSourceMap;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;
import ca.teamdman.sfml.program_builder.VisualCapabilityReport.Kind;

class StudioTypedConditionTest {

    private static final UUID PROJECT = UUID.randomUUID();
    private static final String QUERY = "(LaBeL(\"source\").available() and not LABEL(\"source\").empty())";

    private static String body(String condition) {
        return "EVERY 20 TICKS DO IF " + condition + " THEN PRINT(\"yes\") ELSE PRINT(\"no\") END END";
    }

    private static List<Node> conditions(BlueprintGraph graph) {
        List<Node> result = new ArrayList<>();
        for (Node node : graph.nodes()) if (node.type == Type.IF) result.add(node);
        return result;
    }

    private static String repeat(String text, int count) {
        return String.join("", java.util.Collections.nCopies(count, text));
    }

    private static Program compiled(String source) {
        ProgramBuildResult built = new ProgramBuilder(source).build();
        assertTrue(built.isSuccess(), built.getErrorMessage());
        return built.getProgram();
    }

    @Test
    void allThreePredicateFormsImportWithoutNewTypesPinsOrLayoutFormat() {
        for (String predicate : Arrays.asList(
            QUERY,
            "source HAS >= (2 + 3) item:minecraft:iron_ingot",
            "REDSTONE >= (2 + 3)",
            "(1 + 2 = 3)",
            "source HAS > 0 AND " + QUERY,
            "NOT " + QUERY,
            "({end:TRUE}.end)",
            "(LABEL(\"source\").count(ITEM(\"*\")) = 0)")) {
            String source = body(predicate);
            BlueprintGraph graph = BlueprintCompiler.importText(source);
            assertEquals(1, conditions(graph).size(), predicate);
            BlueprintCompiler.validateClause(
                Type.IF,
                conditions(graph).get(0)
                    .clause());
            compiled(BlueprintCompiler.generate(graph));
            BlueprintDocument restored = BlueprintDocument.open(BlueprintDocument.save(graph));
            assertTrue(restored.restoredLayout);
            assertEquals(conditions(graph).get(0).id, conditions(restored.graph).get(0).id);
        }
        assertEquals(10, Type.values().length);
    }

    @Test
    void basicConditionFieldPreservesExactUntouchedLexemesAndNoop() {
        String clause = "  (LaBeL(\"仓库😀\").available() AND TRUE)  ";
        List<StudioNodeProperties.Field> fields = StudioNodeProperties.inspect(Type.IF, clause);
        assertEquals(1, fields.size());
        assertEquals(
            "condition",
            fields.get(0)
                .getKey());
        assertEquals(
            StudioNodeProperties.FieldType.CONDITION,
            fields.get(0)
                .getType());
        assertEquals(
            clause,
            StudioNodeProperties.update(
                Type.IF,
                clause,
                "condition",
                fields.get(0)
                    .getValue()));
        assertEquals(
            "  (LABEL(\"other\").empty())  ",
            StudioNodeProperties.update(Type.IF, clause, "condition", " (LABEL(\"other\").empty()) "));
        assertEquals(
            4,
            StudioNodeProperties.inspect(Type.IF, "source HAS >= 3")
                .size());
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties.update(Type.IF, clause, "label", "other"));
    }

    @Test
    void singlePredicateParsingRefusesControlCommentsAndStatementInjection() {
        for (String invalid : Arrays.asList(
            "",
            "TRUE THEN PRINT(1)",
            "TRUE END",
            "IF TRUE",
            "TRUE -- hidden",
            "TRUE\n",
            "TRUE\r",
            "TRUE\0",
            "(TRUE",
            "TRUE FALSE",
            "TRUE DO END",
            "TRUE ELSE IF FALSE",
            "(TRUE) THEN INPUT FROM source")) {
            assertThrows(IllegalArgumentException.class, () -> StudioConditionSyntax.parse(invalid), invalid);
            assertThrows(
                IllegalArgumentException.class,
                () -> BlueprintCompiler.validateClause(Type.IF, invalid),
                invalid);
        }
        StudioConditionSyntax.parse("(\"THEN END DO --\" = \"THEN END DO --\")");
        StudioConditionSyntax.parse("({then:TRUE,end:FALSE}.then)");
    }

    @Test
    void scalarTypesAndFunctionAuthorityStillComeFromCompiler() {
        for (String predicate : Arrays.asList(
            "(1)",
            "(\"yes\")",
            "source HAS > (TRUE)",
            "REDSTONE > (\"1\")",
            "(LEN([1]) = 1)",
            "(LABEL(\"source\").noSuchMethod())"))
            assertThrows(
                IllegalArgumentException.class,
                () -> BlueprintCompiler.validateClause(Type.IF, predicate),
                predicate);
    }

    @Test
    void newTypedPredicateDoesNotGrantDataCapabilityToBodiesOrOtherStatements() {
        for (String statement : Arrays.asList(
            "LET $x = 1",
            "INPUT (1+2) FROM source",
            "OUTPUT (1+2) TO dest",
            "FOR $x IN [1] DO PRINT($x) END",
            "WHILE FALSE DO FORGET END",
            "TRANSFER(LABEL(\"source\"),LABEL(\"dest\")) INTO $m")) {
            String source = "EVERY 20 TICKS DO IF " + QUERY + " THEN " + statement + " END END";
            compiled(source);
            StudioDocument document = new StudioDocument();
            document.load(source);
            assertThrows(IllegalArgumentException.class, document::openVisual, statement);
            assertEquals(source, document.source());
            assertNull(document.graph());
        }
        String scoped = "STATE $x = TRUE EVERY 20 TICKS DO IF ($x) THEN PRINT(1) END END";
        assertTrue(
            BlueprintCompiler.inspectText(scoped).report.getBlockers()
                .stream()
                .anyMatch(b -> b.kind == Kind.DATA_EXPRESSION));
        String player = "FUNCTION test() RETURNS BOOLEAN DO RETURN TRUE END EVERY 20 TICKS DO IF (test()) THEN END END";
        assertTrue(
            BlueprintCompiler.inspectText(player).report.getBlockers()
                .stream()
                .anyMatch(b -> b.kind == Kind.FUNCTION_CALL));
        assertFalse(
            BlueprintCompiler
                .inspectText("EVERY 20 TICKS DO IF " + QUERY + " THEN FORGET ELSE IF TRUE THEN FORGET END END").report
                    .isAvailable());
    }

    @Test
    void newBudgetsAccumulateAcrossLegacyCommaListsAndBooleanTerms() {
        String chain = repeat("source,dest HAS >= 0 AND ", 64) + "(1 = 1)";
        assertThrows(IllegalArgumentException.class, () -> StudioConditionSyntax.parse(chain));
        String combined = "(1" + repeat("+1", 32) + " > 0) AND (1" + repeat("+1", 32) + " > 0)";
        assertThrows(IllegalArgumentException.class, () -> StudioConditionSyntax.parse(combined));
        String labels = "source,dest HAS >= 0 AND ";
        StudioConditionSyntax.parse(repeat(labels, 10) + "(1 = 1)");
        String legacy = repeat(labels, 80) + "TRUE";
        compiled(body(legacy));
        BlueprintCompiler.importText(body(legacy)); // No new 64-operator policy on an old legacy-only predicate.
    }

    @Test
    void independentContainerSiblingsRetainLocalBudgetsAndStringsAreOpaque() {
        String arithmetic = "1" + repeat("+1", 50);
        StudioConditionSyntax.parse("([" + arithmetic + "," + arithmetic + "][0] > 0)");
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioConditionSyntax.parse("(1" + repeat("+1", 65) + " > 0)"));
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioConditionSyntax.parse(repeat("(", 33) + "1=1" + repeat(")", 33)));
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioConditionSyntax.parse("(" + repeat("not ", 33) + "LABEL(\"source\").available())"));
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioConditionSyntax.parse("({x:TRUE}" + repeat(".x", 33) + ")"));
        StudioConditionSyntax.parse("(\"" + repeat("THEN,--()", 100) + "\" = \"literal\")");
    }

    @Test
    void newConditionCopiesPreserveInvalidPendingTextOwnerAndFeedback() {
        Node node = new Node(Type.IF, 0, 0);
        node.setClause(QUERY);
        StudioFieldDraft draft = new StudioFieldDraft(
            PROJECT,
            node,
            body(QUERY),
            StudioNodeProperties.inspect(Type.IF, QUERY));
        assertTrue(draft.set("condition", "TRUE THEN END"));
        StudioFieldDraft copy = new StudioFieldDraft(draft);
        assertEquals("TRUE THEN END", copy.value("condition"));
        assertThrows(IllegalArgumentException.class, () -> copy.appliedClause(PROJECT, node, copy.source));
        assertEquals(QUERY, node.clause());
        assertThrows(IllegalArgumentException.class, () -> copy.appliedClause(UUID.randomUUID(), node, copy.source));
        assertThrows(IllegalArgumentException.class, () -> copy.appliedClause(PROJECT, node, copy.source + " "));
        StudioFieldFeedback feedback = new StudioFieldFeedback();
        feedback.open(draft, "");
        String footer = feedback.failedApply(draft, "invalid", "");
        StudioFieldFeedback.Snapshot error = feedback.snapshot(draft);
        assertTrue(feedback.current(error, draft));
        assertTrue(draft.set("condition", "(1=1)"));
        feedback.changed(draft, footer);
        assertFalse(feedback.current(error, draft));
        assertEquals("(1=1)", draft.appliedClause(PROJECT, node, draft.source));
    }

    @Test
    void exactConditionApplyUndoRedoAndDuplicateSourceMapsKeepIdentity() {
        String predicate = "(LABEL(\"仓库😀\").empty())";
        String source = "-- kept\r\nEVERY 20 TICKS DO IF " + predicate
            + " THEN PRINT(1) END IF "
            + predicate
            + " THEN PRINT(1) END END";
        StudioDocument document = new StudioDocument();
        document.load(source);
        document.openVisual();
        Node second = conditions(document.graph()).get(1);
        StudioSourceMap map = StudioSourceMap
            .build(PROJECT, source, StudioSourceMap.normalize(source), document.graph());
        int first = source.indexOf("IF "), next = source.indexOf("IF ", first + 3);
        assertEquals(
            second.id,
            map.atCaret(
                StudioSourceMap.normalize(source.substring(0, next))
                    .length()).node);
        List<UUID> ids = new ArrayList<>();
        for (Node node : document.graph()
            .nodes()) ids.add(node.id);
        StudioFieldDraft pending = new StudioFieldDraft(
            PROJECT,
            second,
            document.code(),
            StudioNodeProperties.inspect(Type.IF, second.clause()));
        pending.set("condition", "(LABEL(\"仓库😀\").available())");
        assertEquals(source, document.source());
        String changed = pending.appliedClause(PROJECT, second, document.code());
        document.editGraph(
            g -> g.node(second.id)
                .setClause(changed));
        String after = document.source();
        assertTrue(document.undo());
        assertEquals(source, document.source());
        assertTrue(document.redo());
        assertEquals(after, document.source());
        assertThrows(
            IllegalArgumentException.class,
            () -> map.requireCurrent(
                PROJECT,
                document.code(),
                StudioSourceMap.normalize(document.code()),
                document.graph()));
        assertEquals(
            ids,
            new ArrayList<>(
                document.graph()
                    .nodes()
                    .stream()
                    .map(n -> n.id)
                    .collect(java.util.stream.Collectors.toList())));
    }

    @Test
    void preventiveTypedBoundsRefuseWithoutAnyParserOverflowCause() {
        for (String predicate : Arrays.asList(
            repeat("(", 100) + "(TRUE) == FALSE" + repeat(")", 100),
            repeat("(", 80) + "LABEL(\"source\").available()" + repeat(")", 80),
            "(" + repeat("NOT ", 80) + "LABEL(\"source\").available())",
            "(" + repeat("[", 80) + "TRUE" + repeat("]", 80) + "[0])",
            "(" + repeat("LABEL(", 80) + "\"source\"" + repeat(")", 80) + ".available())",
            "(1" + repeat("+1", 200) + " > 0)",
            repeat("(source,dest HAS >= 0 AND ", 40) + "(TRUE = TRUE)" + repeat(")", 40))) {
            IllegalArgumentException failure = assertThrows(
                IllegalArgumentException.class,
                () -> StudioConditionSyntax.parse(predicate),
                predicate);
            for (Throwable cause = failure; cause != null; cause = cause.getCause())
                assertFalse(cause instanceof StackOverflowError, "A preventive guard must fail before parser overflow");
        }
    }

    @Test
    void numericPrimariesUnderBooleanOperatorsHavePreventiveTypedWitnesses() {
        for (String predicate : Arrays.asList("(NOT NOT 1=1)", "(TRUE AND 1=1)", "(FALSE OR 1=1)"))
            BlueprintCompiler.importText(body(predicate));
        for (String predicate : Arrays.asList(
            "(" + repeat("NOT ", 80) + "1=1)",
            repeat("(", 100) + "TRUE AND 1=1" + repeat(")", 100),
            repeat("(", 100) + "FALSE OR 1=1" + repeat(")", 100))) {
            Throwable failure = assertThrows(
                IllegalArgumentException.class,
                () -> StudioConditionSyntax.parse(predicate));
            for (Throwable cause = failure; cause != null; cause = cause.getCause())
                assertFalse(cause instanceof StackOverflowError);
        }
    }

    @Test
    void legacyParenthesesNegationResourcesAndPhysicalSidesKeepOldAcceptance() {
        for (String legacy : Arrays.asList(
            repeat("(", 80) + "source HAS > 0" + repeat(")", 80),
            repeat("NOT ", 80) + "source,dest HAS >= 0",
            repeat("(", 80) + "\"source\" NORTH,SOUTH,EAST,WEST SIDE SLOTS 0-2 HAS >= 0 \"item:minecraft:stone\""
                + repeat(")", 80),
            repeat("(", 80) + "source HAS >= 0 \"item:a\" OR \"item:b\"" + repeat(")", 80))) {
            compiled(body(legacy));
            StudioConditionSyntax.parse(legacy);
            BlueprintCompiler.importText(body(legacy));
        }
    }

    private static final class CountingInventory extends AuditRegressionTest.InventoryTile {

        int reads;

        CountingInventory() {
            super(3);
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            reads++;
            return super.getStackInSlot(slot);
        }
    }

    private static ProgramContext tick(AuditRegressionTest.Fixture fixture, Program program, long due) {
        ProgramContext context = new ProgramContext(
            fixture.world,
            null,
            new CableNetwork(fixture.world, AuditRegressionTest.MANAGER),
            fixture.labels,
            due,
            false);
        program.tick(context);
        return context;
    }

    @Test
    void actualProgramTickShortCircuitsQueriesAndMatchesFixedSlotReadCounts() throws Exception {
        String source = "EVERY 20 TICKS DO IF (FALSE AND LABEL(\"source\").empty()) THEN FORGET END "
            + "IF (TRUE OR LABEL(\"source\").empty()) THEN FORGET END "
            + "IF (LABEL(\"missing\").available() AND LABEL(\"missing\").count(ITEM(\"*\")) > 0) THEN FORGET END "
            + "IF (LABEL(\"source\").count(ITEM(\"*\")) = 7) THEN FORGET END END";
        for (String candidate : Arrays
            .asList(source, BlueprintCompiler.generate(BlueprintCompiler.importText(source)))) {
            AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
            CountingInventory inventory = new CountingInventory();
            inventory.setInventorySlotContents(0, new ItemStack(AuditRegressionTest.ITEM, 7));
            fixture.add("source", AuditRegressionTest.SRC, inventory);
            Program program = compiled(candidate);
            tick(fixture, program, 1);
            assertEquals(0, inventory.reads);
            tick(fixture, program, 20);
            assertEquals(3, inventory.reads, "Only the last predicate scans the three physical slots");
            assertEquals(7, inventory.getStackInSlot(0).stackSize);
        }
    }

    @Test
    void actualProgramTickComputedThresholdAndQueriesSeeEarlierEffectsInOrder() throws Exception {
        String source = "EVERY 20 TICKS DO IF source HAS >= (2 + 3) THEN INPUT 2 FROM source OUTPUT TO dest END "
            + "IF (LABEL(\"source\").side(\"north\").slots(0,0).count(ITEM(\"*\")) = 5) THEN "
            + "INPUT 1 FROM source OUTPUT TO dest END IF REDSTONE = (2 - 2) THEN "
            + "INPUT 1 FROM source OUTPUT TO dest END END";
        for (String candidate : Arrays
            .asList(source, BlueprintCompiler.generate(BlueprintCompiler.importText(source)))) {
            AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
            fixture.stock(0, 7);
            tick(fixture, compiled(candidate), 20);
            assertEquals(3, AuditRegressionTest.count(fixture.source));
            assertEquals(4, AuditRegressionTest.count(fixture.dest));
        }
    }

    @Test
    void actualPendingConditionApplyUndoRedoChangesOnlyChosenBranchEffects() throws Exception {
        String source = "EVERY 20 TICKS DO IF (LABEL(\"source\").available()) THEN INPUT 2 FROM source OUTPUT TO dest END END";
        StudioDocument document = new StudioDocument();
        document.load(source);
        document.openVisual();
        Node node = conditions(document.graph()).get(0);
        StudioFieldDraft pending = new StudioFieldDraft(
            PROJECT,
            node,
            document.code(),
            StudioNodeProperties.inspect(Type.IF, node.clause()));
        pending.set("condition", "(LABEL(\"missing\").available())");
        assertMoved(document.source(), 2);
        String changed = pending.appliedClause(PROJECT, node, document.code());
        document.editGraph(
            g -> g.node(node.id)
                .setClause(changed));
        assertMoved(document.source(), 0);
        assertTrue(document.undo());
        assertMoved(document.source(), 2);
        assertTrue(document.redo());
        assertMoved(document.source(), 0);
    }

    private static void assertMoved(String source, int moved) throws Exception {
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        fixture.stock(0, 7);
        tick(fixture, compiled(source), 20);
        assertEquals(moved, AuditRegressionTest.count(fixture.dest));
        assertEquals(7 - moved, AuditRegressionTest.count(fixture.source));
    }
}
