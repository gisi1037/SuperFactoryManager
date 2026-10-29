package ca.teamdman.sfml.program_builder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import ca.teamdman.langs.SFMLParser;
import ca.teamdman.sfml.program_builder.VisualCapabilityReport.Blocker;
import ca.teamdman.sfml.program_builder.VisualCapabilityReport.Kind;

/** Inspects the complete compiled syntax, independent of authoring profile and runtime reachability. */
public final class VisualCapabilityAnalyzer {

    private static final Pattern LOCATION = Pattern.compile("Line (\\d+), Col (\\d+):");

    private VisualCapabilityAnalyzer() {}

    public static VisualCapabilityReport analyze(ProgramBuildResult result) {
        List<Blocker> blockers = new ArrayList<>();
        if (!result.isSuccess()) {
            for (String error : result.getErrors()) {
                Matcher location = LOCATION.matcher(error);
                int line = -1, column = -1;
                if (location.find()) {
                    line = Integer.parseInt(location.group(1));
                    column = Integer.parseInt(location.group(2));
                    error = error.substring(location.end())
                        .trim();
                }
                blockers.add(new Blocker(Kind.INVALID_SOURCE, error, line, column, -1, -1));
            }
        } else {
            if (result.getSyntax() == null)
                throw new IllegalArgumentException("Use buildWithSyntax for Visual analysis");
            inspect(result.getSyntax(), blockers);
            blockers.sort(Comparator.comparingInt(blocker -> blocker.startOffset));
        }
        return new VisualCapabilityReport(blockers);
    }

    private static void inspect(ParseTree tree, List<Blocker> blockers) {
        if (tree instanceof SFMLParser.NamedRedstoneTriggerContext) blockers.add(
            at(
                Kind.NAMED_REDSTONE_TRIGGER,
                "Named redstone edge trigger currently requires the text editor",
                (ParserRuleContext) tree));
        if (tree instanceof SFMLParser.FunctionDefinitionContext) {
            blockers.add(
                at(
                    Kind.FUNCTION_DEFINITION,
                    "Player function definition currently requires the text editor",
                    (ParserRuleContext) tree));
            inspectCalls(tree, blockers);
            return;
        }
        if (tree instanceof SFMLParser.FunctionCallContext && playerCall((SFMLParser.FunctionCallContext) tree)) {
            blockers.add(
                at(
                    Kind.FUNCTION_CALL,
                    "Player function call currently requires the text editor",
                    (ParserRuleContext) tree));
            return;
        }
        if (tree instanceof SFMLParser.ForStatementContext || tree instanceof SFMLParser.WhileStatementContext) {
            blockers.add(
                at(Kind.LOOP_STATEMENT, "Loop statement currently requires the text editor", (ParserRuleContext) tree));
            inspectCalls(tree, blockers);
            return;
        }
        if (tree instanceof SFMLParser.TransferStatementContext) {
            blockers.add(
                at(
                    Kind.TRANSFER_STATEMENT,
                    "Value-driven TRANSFER currently requires the text editor",
                    (ParserRuleContext) tree));
            inspectCalls(tree, blockers);
            return;
        }
        if (tree instanceof SFMLParser.PrintStatementContext) {
            blockers.add(
                at(
                    Kind.PRINT_STATEMENT,
                    "PRINT statement currently requires the text editor",
                    (ParserRuleContext) tree));
            inspectCalls(tree, blockers);
            return;
        }
        Kind dataKind = tree instanceof SFMLParser.StateDeclarationContext ? Kind.STATE_DECLARATION
            : tree instanceof SFMLParser.DataDeclarationContext ? Kind.LOCAL_DECLARATION
                : tree instanceof SFMLParser.DataAssignmentContext ? Kind.ASSIGNMENT
                    : tree instanceof SFMLParser.DataExpressionContext ? Kind.DATA_EXPRESSION : null;
        if (dataKind != null) {
            String name = dataKind == Kind.STATE_DECLARATION ? "STATE declaration"
                : dataKind == Kind.LOCAL_DECLARATION ? "LET declaration"
                    : dataKind == Kind.ASSIGNMENT ? "SET assignment" : "Data expression";
            blockers.add(at(dataKind, name + " currently requires the text editor", (ParserRuleContext) tree));
            // Keep a precise function blocker even when its enclosing expression already blocks Visual.
            inspectCalls(tree, blockers);
            return;
        }
        if (tree instanceof SFMLParser.IfStatementContext) {
            SFMLParser.IfStatementContext branch = (SFMLParser.IfStatementContext) tree;
            for (int i = 1; i < branch.boolexpr()
                .size(); i++) {
                Token condition = branch.ELSE(i - 1)
                    .getSymbol();
                Token end = branch.boolexpr(i)
                    .getStop();
                blockers.add(
                    new Blocker(
                        Kind.ELSE_IF,
                        "ELSE IF currently requires the text editor",
                        condition.getLine(),
                        condition.getCharPositionInLine(),
                        condition.getStartIndex(),
                        end.getStopIndex() + 1));
            }
        }
        for (int i = 0; i < tree.getChildCount(); i++) inspect(tree.getChild(i), blockers);
    }

    private static boolean playerCall(SFMLParser.FunctionCallContext context) {
        String name = context.functionName()
            .getText();
        return !name.equalsIgnoreCase("LABEL") && !name.equalsIgnoreCase("ITEM");
    }

    private static void inspectCalls(ParseTree tree, List<Blocker> blockers) {
        if (tree instanceof SFMLParser.FunctionCallContext && playerCall((SFMLParser.FunctionCallContext) tree)) {
            blockers.add(
                at(
                    Kind.FUNCTION_CALL,
                    "Player function call currently requires the text editor",
                    (ParserRuleContext) tree));
        }
        for (int i = 0; i < tree.getChildCount(); i++) inspectCalls(tree.getChild(i), blockers);
    }

    public static Blocker at(Kind kind, String message, ParserRuleContext context) {
        Token start = context.getStart(), end = context.getStop();
        return new Blocker(
            kind,
            message,
            start.getLine(),
            start.getCharPositionInLine(),
            start.getStartIndex(),
            end.getStopIndex() + 1);
    }
}
