package ca.teamdman.sfm.client.gui.syntax;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ListTokenSource;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.langs.SFMLParser;
import ca.teamdman.sfm.client.gui.syntax.SfmlCompletionCatalog.Entry;

/** Explicit completion-time lexical scope index. It never executes initializers or infers a binding's type. */
final class StudioVariables {

    private static final String PLACEHOLDER = "$__sfm_completion__";

    private StudioVariables() {}

    static List<Entry> visible(String source, int start, int end) {
        // Replace even a lone '$' with a valid variable token, keeping the surrounding draft intact.
        String draft = source.substring(0, start) + PLACEHOLDER + source.substring(end);
        SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(draft));
        lexer.removeErrorListeners();
        List<Token> tokens = new ArrayList<>();
        int brackets = 0, blocks = 0, unary = 0, previous = -1;
        Token anchor = null;
        int position = draft.codePointCount(0, start);
        for (int count = 0; count < 16000; count++) {
            Token token = lexer.nextToken();
            if (token.getType() == Token.EOF) break;
            if (count == 15999) return Collections.emptyList();
            tokens.add(token);
            if (token.getChannel() != Token.DEFAULT_CHANNEL) continue;
            int type = token.getType();
            if (token.getStartIndex() == position && type == SFMLLexer.VARIABLE) anchor = token;
            if (type == SFMLLexer.LPAREN || type == SFMLLexer.LBRACKET || type == SFMLLexer.LBRACE) brackets++;
            if (type == SFMLLexer.RPAREN || type == SFMLLexer.RBRACKET || type == SFMLLexer.RBRACE) brackets--;
            if (type == SFMLLexer.FUNCTION || type == SFMLLexer.EVERY
                || type == SFMLLexer.FOR
                || type == SFMLLexer.WHILE
                || type == SFMLLexer.IF && previous != SFMLLexer.ELSE) blocks++;
            if (type == SFMLLexer.END) blocks--;
            unary = type == SFMLLexer.NOT || type == SFMLLexer.DASH
                || type == SFMLLexer.PLUS
                || type == SFMLLexer.BIT_NOT ? unary + 1 : 0;
            // Completion is assistance for ordinary drafts. Refuse pathological parser recursion before parsing.
            if (brackets > 16 || blocks > 32 || unary > 32) return Collections.emptyList();
            previous = type;
        }
        if (anchor == null) return Collections.emptyList();
        CommonTokenStream stream = new CommonTokenStream(new ListTokenSource(tokens));
        SFMLParser parser = new SFMLParser(stream);
        parser.removeErrorListeners();
        parser.setErrorHandler(new BailErrorStrategy());
        SFMLParser.ProgramContext program;
        try {
            program = parser.program();
        } catch (ParseCancellationException invalid) {
            return Collections.emptyList(); // The ordinary diagnostic still explains the syntax error.
        }
        TerminalNode target = null;
        List<ParseTree> nodes = new ArrayList<>();
        Deque<ParseTree> pending = new ArrayDeque<>();
        pending.push(program);
        while (!pending.isEmpty()) {
            ParseTree node = pending.pop();
            nodes.add(node);
            if (node instanceof TerminalNode && ((TerminalNode) node).getSymbol() == anchor)
                target = (TerminalNode) node;
            for (int i = node.getChildCount() - 1; i >= 0; i--) pending.push(node.getChild(i));
        }
        if (target == null || declarationSite(target)) return Collections.emptyList();
        Set<ParseTree> active = Collections.newSetFromMap(new IdentityHashMap<>());
        for (ParseTree node = target; node != null; node = node.getParent()) active.add(node);
        boolean function = false;
        for (ParseTree node : active) {
            if (node instanceof SFMLParser.StateDeclarationContext) return Collections.emptyList();
            if (node instanceof SFMLParser.FunctionDefinitionContext) function = true;
        }
        Map<String, Entry> names = new LinkedHashMap<>();
        for (ParseTree node : nodes) {
            if (node instanceof SFMLParser.StateDeclarationContext) {
                SFMLParser.StateDeclarationContext declaration = (SFMLParser.StateDeclarationContext) node;
                if (!function && completed(declaration, position))
                    add(names, declaration.VARIABLE(), "Persistent state");
            } else if (node instanceof SFMLParser.DataDeclarationContext) {
                SFMLParser.DataDeclarationContext declaration = (SFMLParser.DataDeclarationContext) node;
                boolean global = declaration.getParent() == program;
                if (completed(declaration, position) && (global ? !function
                    : active.contains(
                        declaration.getParent()
                            .getParent())))
                    add(names, declaration.VARIABLE(), global ? "Program-wide variable" : "Local variable");
            } else if (node instanceof SFMLParser.FunctionParameterContext) {
                SFMLParser.FunctionParameterContext parameter = (SFMLParser.FunctionParameterContext) node;
                if (active.contains(parameter.getParent()) && completed(parameter, position)) add(
                    names,
                    parameter.VARIABLE(),
                    "Function parameter: " + parameter.dataType()
                        .getText());
            } else if (node instanceof SFMLParser.CountedForContext) {
                SFMLParser.CountedForContext loop = (SFMLParser.CountedForContext) node;
                if (active.contains(loop.block())) add(names, loop.VARIABLE(), "Read-only loop variable");
            } else if (node instanceof SFMLParser.ArrayForContext) {
                SFMLParser.ArrayForContext loop = (SFMLParser.ArrayForContext) node;
                if (active.contains(loop.block())) add(names, loop.VARIABLE(), "Read-only loop variable");
            } else if (node instanceof SFMLParser.TransferStatementContext) {
                SFMLParser.TransferStatementContext transfer = (SFMLParser.TransferStatementContext) node;
                if (transfer.VARIABLE() != null && completed(transfer, position)
                    && active.contains(
                        transfer.getParent()
                            .getParent()))
                    add(names, transfer.VARIABLE(), "Transfer result");
            }
        }
        return new ArrayList<>(names.values());
    }

    private static boolean declarationSite(TerminalNode target) {
        ParseTree parent = target.getParent();
        return parent instanceof SFMLParser.DataDeclarationContext
            || parent instanceof SFMLParser.StateDeclarationContext
            || parent instanceof SFMLParser.FunctionParameterContext
            || parent instanceof SFMLParser.CountedForContext
            || parent instanceof SFMLParser.ArrayForContext
            || parent instanceof SFMLParser.TransferStatementContext;
    }

    private static boolean completed(ParserRuleContext declaration, int position) {
        return declaration.getStop() != null && declaration.getStop()
            .getStopIndex() < position;
    }

    private static void add(Map<String, Entry> names, TerminalNode variable, String role) {
        String name = variable.getText();
        if (names.size() < SfmlCompletionCatalog.MAX_ENTRIES && !names.containsKey(name)) names.put(
            name,
            SfmlCompletionCatalog.variable(
                name,
                role + " declared on line "
                    + variable.getSymbol()
                        .getLine()
                    + ". The compiler checks its fixed type and initializer."));
    }
}
