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
        if (tree instanceof SFMLParser.IntervalSpaceContext
            && ((SFMLParser.IntervalSpaceContext) tree).dataExpression() != null
            || tree instanceof SFMLParser.IntervalNoSpaceContext
                && ((SFMLParser.IntervalNoSpaceContext) tree).dataExpression() != null) {
            blockers.add(
                at(
                    Kind.DATA_EXPRESSION,
                    "Runtime timer offsets currently require the text editor",
                    (ParserRuleContext) tree));
            return;
        }
        if (tree instanceof SFMLParser.BreakStatementContext) {
            blockers
                .add(at(Kind.BREAK_STATEMENT, "BREAK currently requires the text editor", (ParserRuleContext) tree));
            return;
        }
        // Named redstone headers now have a complete graph root. Continue inspecting every body statement.
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
        if (tree instanceof SFMLParser.WaitStatementContext || tree instanceof SFMLParser.CircuitStatementContext) {
            boolean wait = tree instanceof SFMLParser.WaitStatementContext;
            blockers.add(
                at(
                    wait ? Kind.WAIT_STATEMENT : Kind.CIRCUIT_STATEMENT,
                    (wait ? "WAIT" : "Ghost circuit action") + " currently requires the text editor",
                    (ParserRuleContext) tree));
            inspectCalls(tree, blockers);
            return;
        }
        if (tree instanceof SFMLParser.TransferStatementContext) {
            // Result capture remains separate from ordinary scoped declaration and assignment nodes.
            if (((SFMLParser.TransferStatementContext) tree).INTO() != null) blockers.add(
                at(
                    Kind.TRANSFER_STATEMENT,
                    "TRANSFER result capture currently requires the text editor",
                    (ParserRuleContext) tree));
            inspectCalls(tree, blockers);
            return;
        }
        if (tree instanceof SFMLParser.PrintStatementContext) {
            // The action owns all of its expression islands; player calls remain text-only.
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
            // Predicate expressions own only this IF's conditions. Bodies retain every general blocker.
            for (SFMLParser.BoolexprContext predicate : branch.boolexpr()) inspect(predicate, blockers);
            for (SFMLParser.BlockContext body : branch.block()) inspect(body, blockers);
            return;
        }
        for (int i = 0; i < tree.getChildCount(); i++) inspect(tree.getChild(i), blockers);
    }

    private static boolean playerCall(SFMLParser.FunctionCallContext context) {
        String name = context.functionName()
            .getText()
            .toUpperCase(java.util.Locale.ROOT);
        int receiver = name.lastIndexOf('.');
        if (receiver >= 0) {
            // Successful whole-source compilation has resolved these as receiver queries.
            // Any player definition still blocks the complete program independently.
            switch (name.substring(receiver + 1)) {
                case "SIDE":
                case "SLOTS":
                case "AVAILABLE":
                case "COUNT":
                case "EMPTY":
                case "CONTENTS":
                case "FAVAILABLE":
                case "FCOUNT":
                case "FEMPTY":
                case "FCONTENTS":
                    return false;
                default:
                    return true;
            }
        }
        switch (name) {
            case "LABEL":
            case "ITEM":
            case "ORE":
            case "FLUID":
            case "ABS":
            case "LEN":
            case "MIN":
            case "MAX":
            case "CLAMP":
            case "RANDOM":
            case "TOTAL":
            case "STOCK":
            case "SHORTAGE":
            case "ARRAY":
            case "PEEK":
            case "CONTAINS":
            case "INDEXOF":
                return false;
            default:
                return true;
        }
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
