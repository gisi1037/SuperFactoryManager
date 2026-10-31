package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioDocument;
import ca.teamdman.sfm.client.blueprint.StudioExpressionSyntax;
import ca.teamdman.sfm.client.blueprint.StudioFieldDraft;
import ca.teamdman.sfm.client.blueprint.StudioFieldFeedback;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfm.client.blueprint.StudioPrintArguments;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class StudioPrintArgumentsTest {

    private static final UUID PROJECT = new UUID(19, 1037);
    private static final String UNICODE = "\u03c0\uD83C\uDFED";

    private static String program(String clause) {
        return "every 20 ticks do " + clause + " end";
    }

    private static Node node(String clause) {
        return new Node(UUID.randomUUID(), Type.PRINT, 0, 0, clause);
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

    private static String repeat(String value, int count) {
        return String.join("", Collections.nCopies(count, value));
    }

    @Test
    void parsedTopLevelSpansKeepQuotedEscapedNestedCommasAndUnicodeExact() {
        String clause = "pRiNt(  \"" + UNICODE
            + ",\\\"x\" ,\t [1,2,] , {a:1,b:[2,3]} , LABEL(\"x,y\").slots(0, 2).available()  )";
        StudioPrintArguments rows = new StudioPrintArguments(clause);
        assertEquals(4, rows.size());
        assertEquals(
            "[1,2,]",
            rows.rows()
                .get(1).text);
        assertEquals(
            "{a:1,b:[2,3]}",
            rows.rows()
                .get(2).text);
        assertEquals(clause, rows.applyTo(clause));
        Node node = node(clause);
        assertEquals(clause, apply(draft(node), node));
    }

    @Test
    void firstMiddleLastRemovalKeepsUntouchedSeparatorsAndAppendUsesOneGap() {
        String clause = "pRiNt(  \"x,a\" ,\t [1,2] , 3  )";
        String[] expected = { "pRiNt(  [1,2] , 3  )", "pRiNt(  \"x,a\" ,\t 3  )", "pRiNt(  \"x,a\" ,\t [1,2]  )" };
        for (int i = 0; i < 3; i++) {
            StudioPrintArguments rows = new StudioPrintArguments(clause);
            rows.remove(
                rows.rows()
                    .get(i).id);
            assertEquals(expected[i], rows.applyTo(clause));
        }
        StudioPrintArguments rows = new StudioPrintArguments(clause);
        rows.append(" 4 ");
        assertEquals("pRiNt(  \"x,a\" ,\t [1,2] , 3, 4  )", rows.applyTo(clause));
    }

    @Test
    void textEditsKeepIdentityAndRevisionAndCopiesAreIndependent() {
        StudioPrintArguments rows = new StudioPrintArguments("PRINT(1,2)");
        UUID first = rows.alias(0), revision = rows.revision();
        StudioPrintArguments copy = new StudioPrintArguments(rows);
        assertTrue(copy.set(first, " 9 "));
        assertEquals(revision, copy.revision());
        assertEquals("1", rows.value(first));
        assertEquals(" 9 ", copy.value(first));
        assertEquals("PRINT(9,2)", copy.applyTo("PRINT(1,2)"));
        assertFalse(copy.set(first, " 9 "));
        assertThrows(
            UnsupportedOperationException.class,
            () -> rows.rows()
                .clear());
    }

    @Test
    void refusedCapLastRemovalAndOversizedActionsKeepAliasesAndRevision() {
        StudioPrintArguments one = new StudioPrintArguments("PRINT(1)");
        UUID first = one.alias(0), revision = one.revision();
        assertThrows(IllegalArgumentException.class, () -> one.remove(first));
        assertThrows(IllegalArgumentException.class, () -> one.append(repeat("1", 4096)));
        assertThrows(IllegalArgumentException.class, () -> one.set(first, repeat("1", 4096)));
        assertEquals(revision, one.revision());
        assertFalse(one.aliasesRetired());
        assertEquals("PRINT(1)", one.applyTo("PRINT(1)"));
        StudioPrintArguments eight = new StudioPrintArguments("PRINT(1,2,3,4,5,6,7,8)");
        UUID fullRevision = eight.revision();
        assertThrows(IllegalArgumentException.class, () -> eight.append("9"));
        assertEquals(fullRevision, eight.revision());
        assertFalse(eight.aliasesRetired());
        assertEquals(8, eight.size());
    }

    @Test
    void aliasesRetirePermanentlyThroughAbaAndCopiesWhileRowsStayStable() {
        StudioPrintArguments rows = new StudioPrintArguments("PRINT(1,2)");
        UUID first = rows.alias(0), revision = rows.revision();
        UUID added = rows.append("3");
        assertNotEquals(revision, rows.revision());
        rows.remove(added);
        assertEquals(2, rows.size());
        assertTrue(rows.aliasesRetired());
        assertThrows(IllegalArgumentException.class, () -> rows.alias(0));
        StudioPrintArguments copy = new StudioPrintArguments(rows);
        assertTrue(copy.aliasesRetired());
        assertThrows(IllegalArgumentException.class, () -> copy.alias(1));
        assertThrows(IllegalArgumentException.class, () -> copy.value(added));
        assertEquals("1", copy.value(first));
    }

    @Test
    void blankAppendBlocksEntireApplyAndRepairKeepsOtherPendingEdits() {
        Node node = node("PRINT(1,2)");
        StudioFieldDraft draft = draft(node);
        draft.set("expression0", "9");
        UUID blank = draft.addPrintArgument("");
        assertThrows(IllegalArgumentException.class, () -> apply(draft, node));
        assertEquals("PRINT(1,2)", node.clause());
        StudioFieldDraft copy = new StudioFieldDraft(draft);
        assertEquals("", copy.printArgumentValue(blank));
        copy.setPrintArgument(blank, "3");
        assertEquals("PRINT(9,2, 3)", apply(copy, node));
        assertEquals("", draft.printArgumentValue(blank));
    }

    @Test
    void scalarAliasesDelegateUntilStructureChangesAndNeverOverwriteRows() {
        Node node = node("pRiNt( 1 , 2 )");
        StudioFieldDraft draft = draft(node);
        UUID first = draft.printArgumentRows()
            .get(0).id;
        draft.set("expression0", " 9 ");
        assertEquals(" 9 ", draft.printArgumentValue(first));
        assertEquals(StudioNodeProperties.update(Type.PRINT, node.clause(), "expression0", " 9 "), apply(draft, node));
        UUID added = draft.addPrintArgument("3");
        draft.setPrintArgument(first, "7");
        assertThrows(IllegalArgumentException.class, () -> draft.value("expression0"));
        assertThrows(IllegalArgumentException.class, () -> draft.set("expression1", "99"));
        assertEquals("pRiNt( 7 , 2, 3 )", apply(draft, node));
        draft.removePrintArgument(added);
        assertTrue(draft.retiredPrintField("expression0"));
        assertTrue(new StudioFieldDraft(draft).retiredPrintField("expression1"));
        assertEquals("pRiNt( 7 , 2 )", apply(draft, node));
    }

    @Test
    void invalidPendingExpressionsStayPendingAndCannotInjectStatementsOrRows() {
        for (String invalid : Arrays.asList(
            "",
            " ",
            "1,2",
            "1) FORGET PRINT(2",
            "1 --x",
            "1\n",
            "1\r",
            "1\0",
            "\"x\n\"",
            "1 + TRUE",
            "unknown()",
            "$unknown")) {
            Node node = node("PRINT(1)");
            StudioFieldDraft draft = draft(node);
            UUID row = draft.printArgumentRows()
                .get(0).id;
            draft.setPrintArgument(row, invalid);
            assertThrows(IllegalArgumentException.class, () -> apply(draft, node), invalid);
            assertEquals(invalid, draft.printArgumentValue(row));
            assertEquals("PRINT(1)", node.clause());
        }
    }

    @Test
    void unsupportedInitialClausesKeepExactSourceAndManualFallback() {
        for (String clause : Arrays.asList(
            "PRINT()",
            "PRINT(1,)",
            "PRINT(1,2,3,4,5,6,7,8,9)",
            "PRINT(unknown())",
            "PRINT(" + repeat("(", 33) + "1" + repeat(")", 33) + ")")) {
            Node node = node(clause);
            StudioFieldDraft draft = draft(node);
            assertFalse(draft.hasPrintArguments(), clause);
            assertEquals(clause, node.clause());
            assertEquals(clause, draft.original);
        }
    }

    @Test
    void rawAndPrintWrapperShareDepthBudgetAndSeparateArgumentsResetIt() {
        String accepted = repeat("(", 32) + "1" + repeat(")", 32);
        StudioExpressionSyntax.expression(accepted);
        StudioExpressionSyntax.print("PRINT(" + accepted + ")");
        Node node = node("PRINT(1)");
        StudioFieldDraft draft = draft(node);
        draft.set("expression0", accepted);
        // Lexer nesting counts delimiters; the existing compiler also counts the literal leaf.
        assertThrows(IllegalArgumentException.class, () -> apply(draft, node));
        accepted = repeat("(", 31) + "1" + repeat(")", 31);
        draft.set("expression0", accepted);
        assertEquals("PRINT(" + accepted + ")", apply(draft, node));
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioExpressionSyntax.expression(repeat("(", 33) + "1" + repeat(")", 33)));
        String clause = "PRINT(" + String.join(",", Collections.nCopies(8, accepted)) + ")";
        Node many = node(clause);
        assertEquals(clause, apply(draft(many), many));
    }

    @Test
    void unaryOperatorAndPostfixLimitsRejectBeforeRecursiveParsing() {
        for (String value : Arrays.asList(
            repeat("!", 33) + "TRUE",
            "1" + repeat("+1", 65),
            "1" + repeat(" * 1", 65),
            "LABEL(\"x\")" + repeat(".available()", 33),
            repeat("(", 20) + repeat("!", 13) + "TRUE" + repeat(")", 20))) {
            assertThrows(IllegalArgumentException.class, () -> StudioExpressionSyntax.expression(value), value);
            assertThrows(IllegalArgumentException.class, () -> StudioPrintArguments.validateRow(value), value);
        }
    }

    @Test
    void wideArrayAndStructSiblingExpressionsHaveIndependentBudgets() {
        String expression = "(1" + repeat("+1", 29) + ")";
        String array = "[" + String.join(",", Collections.nCopies(32, expression)) + "]";
        Node node = node("PRINT(" + array + ")");
        assertEquals(node.clause(), apply(draft(node), node));
        List<String> fields = new ArrayList<>();
        for (int i = 0; i < 32; i++) fields.add("f" + i + ":" + expression);
        Node struct = node("PRINT({" + String.join(",", fields) + "})");
        assertEquals(struct.clause(), apply(draft(struct), struct));
        StudioExpressionSyntax.expression("{OR:1,AND:2,NOT:3}");
        StudioExpressionSyntax.expression("{x:1}.OR");
    }

    @Test
    void combinedNestedOperatorCostIsRejectedBeforeParserAndDraftApplyIsAtomic() {
        String value = "1";
        for (int i = 0; i < 31; i++) value = "(" + value + repeat("+1", 60) + ")";
        assertEquals(3783, value.length());
        String pending = value;
        assertThrows(IllegalArgumentException.class, () -> StudioExpressionSyntax.expression(pending));
        Node node = node("PRINT(1)");
        StudioFieldDraft draft = draft(node);
        draft.set("expression0", pending);
        assertThrows(IllegalArgumentException.class, () -> apply(draft, node));
        assertEquals("PRINT(1)", node.clause());
        assertEquals(pending, draft.value("expression0"));
    }

    @Test
    void ownershipFencesRejectOtherProjectNodeTypeSourceAndClause() {
        Node node = node("PRINT(1)");
        StudioFieldDraft draft = draft(node);
        draft.addPrintArgument("2");
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(UUID.randomUUID(), node, draft.source));
        assertThrows(
            IllegalArgumentException.class,
            () -> draft.appliedClause(PROJECT, node("PRINT(1)"), draft.source));
        assertThrows(IllegalArgumentException.class, () -> draft.appliedClause(PROJECT, node, draft.source + " "));
        assertThrows(
            IllegalArgumentException.class,
            () -> draft.appliedClause(PROJECT, new Node(node.id, Type.TRANSFER, 0, 0, node.clause()), draft.source));
        node.setClause("PRINT(2)");
        assertThrows(IllegalArgumentException.class, () -> apply(draft, node));
        assertThrows(
            IllegalArgumentException.class,
            () -> StudioNodeProperties
                .updatePrintArguments(Type.TRANSFER, "PRINT(1)", new StudioPrintArguments("PRINT(1)")));
        assertThrows(IllegalArgumentException.class, () -> new StudioPrintArguments("PRINT(1)").applyTo("PRINT(2)"));
    }

    @Test
    void acceptedRowActionsInvalidateFeedbackButNoopAndRefusalsKeepIt() {
        Node node = node("PRINT(1)");
        StudioFieldDraft draft = draft(node);
        StudioFieldFeedback feedback = new StudioFieldFeedback();
        feedback.open(draft, "");
        String footer = feedback.failedApply(draft, "old failure", "");
        StudioFieldFeedback.Snapshot old = feedback.snapshot(draft);
        assertFalse(draft.set("expression0", "1"));
        assertThrows(
            IllegalArgumentException.class,
            () -> draft.removePrintArgument(
                draft.printArgumentRows()
                    .get(0).id));
        assertTrue(feedback.current(old, draft));
        UUID added = draft.addPrintArgument("");
        footer = feedback.changed(draft, footer);
        assertFalse(feedback.current(old, draft));
        assertNull(feedback.snapshot(draft));
        footer = feedback.failedApply(draft, "blank", footer);
        draft.setPrintArgument(added, "2");
        footer = feedback.changed(draft, footer);
        assertEquals("Fields changed; Apply to check.", footer);
        feedback.failedApply(draft, "later", footer);
        draft.removePrintArgument(added);
        feedback.changed(draft, footer);
        assertNull(feedback.snapshot(draft));
        assertEquals("PRINT(1)", node.clause());
    }

    private static List<String> identities(BlueprintGraph graph) {
        List<String> ids = new ArrayList<>();
        for (Node node : graph.nodes()) ids.add(node.id + ":" + node.type + ":" + node.hasElse);
        for (BlueprintGraph.Edge edge : graph.edges()) ids.add(edge.from + ":" + edge.pin + ":" + edge.to);
        return ids;
    }

    private static List<String> run(String source, boolean capture) throws Exception {
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        TileEntityManager manager = new TileEntityManager();
        manager.getDebugConsole()
            .setEnabled(capture);
        ProgramContext context = new ProgramContext(
            fixture.world,
            manager,
            new CableNetwork(fixture.world, AuditRegressionTest.MANAGER),
            fixture.labels,
            20,
            false);
        ProgramBuildResult built = new ProgramBuilder(source).buildWithSyntax();
        assertTrue(built.isSuccess(), built.getErrorMessage());
        built.getProgram()
            .tick(context);
        if (!capture) assertEquals(
            0,
            context.getDataRuntime()
                .getWork());
        return manager.getDebugConsole()
            .snapshot();
    }

    @Test
    void realRuntimeOracleSeesOnlyAppliedRowsWithOneUndoRedoAndStableGraphIds() throws Exception {
        String source = "-- " + UNICODE
            + "\r\nevery 20 ticks do if TRUE then PRINT(\"old\") else PRINT(\"wrong\") end end";
        StudioDocument document = new StudioDocument();
        document.load(source);
        document.openVisual();
        Node node = document.graph()
            .nodes()
            .stream()
            .filter(
                n -> n.type == Type.PRINT && n.clause()
                    .contains("old"))
            .findFirst()
            .get();
        StudioFieldDraft draft = new StudioFieldDraft(
            PROJECT,
            node,
            document.code(),
            StudioNodeProperties.inspect(node.type, node.clause()));
        List<String> ids = identities(document.graph());
        draft.set("expression0", "\"slot, " + UNICODE + "\"");
        for (String value : Arrays.asList("(2+3)", "[1,2]", "{x:7}", "LABEL(\"missing\").available()", "ITEM(\"*\")"))
            draft.addPrintArgument(value);
        assertEquals(Collections.singletonList("old"), run(document.source(), true));
        assertFalse(document.undo());
        String clause = draft.appliedClause(PROJECT, node, document.code());
        document.editGraph(
            g -> g.node(node.id)
                .setClause(clause));
        List<String> expected = Collections.singletonList("slot, " + UNICODE + " 5 [1, 2] {x: 7} FALSE ITEM(\"*\")");
        assertEquals(expected, run(document.source(), true));
        assertEquals(ids, identities(document.graph()));
        assertTrue(document.undo());
        assertEquals(source, document.source());
        assertEquals(Collections.singletonList("old"), run(document.source(), true));
        assertFalse(document.undo());
        assertTrue(document.redo());
        assertEquals(expected, run(document.source(), true));
        assertEquals(ids, identities(document.graph()));
    }

    @Test
    void disabledCaptureSkipsEditedFaultingExpressionAndQuery() throws Exception {
        Node node = node("PRINT(1)");
        StudioFieldDraft draft = draft(node);
        draft.set("expression0", "1/0");
        draft.addPrintArgument("LABEL(\"missing\").count(ITEM(\"*\"))");
        assertTrue(run(program(apply(draft, node)), false).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> run(program(apply(draft, node)), true));
    }
}
