package ca.teamdman.sfm.client.blueprint;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.langs.SFMLParser;

/** One editor predicate, never a statement wrapper. No evaluation, queries or runtime policy changes. */
public final class StudioConditionSyntax {

    private static final class Frame {

        final int close;
        int unary;
        boolean operand = true, member;

        Frame(int close) {
            this.close = close;
        }
    }

    private StudioConditionSyntax() {}

    public static SFMLParser.BoolexprContext parse(String source) {
        if (source == null || source.length() > BlueprintGraph.MAX_CLAUSE
            || source.trim()
                .isEmpty()
            || source.indexOf('\n') >= 0
            || source.indexOf('\r') >= 0
            || source.indexOf('\0') >= 0)
            throw new IllegalArgumentException("A condition must be one nonblank bounded line");
        CommonTokenStream stream = new CommonTokenStream(new SFMLLexer(CharStreams.fromString(source)));
        ((SFMLLexer) stream.getTokenSource()).removeErrorListeners();
        ((SFMLLexer) stream.getTokenSource()).addErrorListener(new BaseErrorListener() {

            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object symbol, int line, int column, String message,
                RecognitionException cause) {
                throw new IllegalArgumentException("Invalid condition: " + message);
            }
        });
        stream.fill();
        List<Token> tokens = new ArrayList<>();
        for (Token token : stream.getTokens()) {
            if (token.getType() == SFMLLexer.LINE_COMMENT)
                throw new IllegalArgumentException("Comments are not condition fields");
            if (token.getChannel() == Token.DEFAULT_CHANNEL) tokens.add(token);
        }
        // Preserve old legacy ceilings, but bound data-only syntax BEFORE any recursive parser work.
        int newDepth = delimiters(tokens);
        if (looksTyped(tokens)) {
            if (newDepth > StudioExpressionSyntax.MAX_DEPTH) complex();
            StudioExpressionSyntax.predicate(source);
        }
        SFMLParser parser = new SFMLParser(stream);
        parser.removeErrorListeners();
        parser.setErrorHandler(new BailErrorStrategy());
        try {
            SFMLParser.BoolexprContext condition = parser.boolexpr();
            if (parser.getCurrentToken()
                .getType() != Token.EOF)
                throw new IllegalArgumentException("Enter one condition without IF, THEN, ELSE or END");
            if (hasData(condition)) {
                if (newDepth > StudioExpressionSyntax.MAX_DEPTH) complex();
                int operators = 0, postfix = 0;
                ArrayDeque<ParseTree> pending = new ArrayDeque<>();
                pending.push(condition);
                while (!pending.isEmpty()) {
                    ParseTree current = pending.pop();
                    if (current instanceof SFMLParser.DataExpressionContext) {
                        // Stop at each outer island; argument/array/struct commas are local siblings.
                        int[] work = StudioExpressionSyntax.conditionWork(slice(source, current));
                        operators += work[0];
                        postfix += work[1];
                    } else {
                        if (current instanceof SFMLParser.BooleanConjunctionContext
                            || current instanceof SFMLParser.BooleanDisjunctionContext
                            || current instanceof SFMLParser.BooleanNegationContext
                            || current instanceof SFMLParser.BooleanHasContext
                            || current instanceof SFMLParser.BooleanRedstoneContext) operators++;
                        for (int i = 0; i < current.getChildCount(); i++) pending.push(current.getChild(i));
                    }
                    // Legacy label/resource commas never reset the enclosing Boolean chain's work.
                    if (operators > StudioExpressionSyntax.MAX_OPERATORS
                        || postfix > StudioExpressionSyntax.MAX_POSTFIX) complex();
                }
            }
            return condition;
        } catch (ParseCancellationException invalid) {
            throw new IllegalArgumentException("Enter one complete condition, without statement keywords", invalid);
        } catch (StackOverflowError invalid) {
            throw new IllegalArgumentException("Condition is too complex; use Text", invalid);
        }
    }

    /**
     * Every data primary has a lexical witness absent from a valid legacy-only predicate.
     * Parentheses after comparators are computed thresholds; parentheses after names are calls.
     * Literal arithmetic/strings/boolean comparisons witness BooleanData even without a constructor.
     * Legacy resource strings, slot ranges, WITH groups and label commas stay on the old path.
     */
    private static boolean looksTyped(List<Token> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            int kind = tokens.get(i)
                .getType();
            int prior = i == 0 ? Token.EOF
                : tokens.get(i - 1)
                    .getType();
            int next = i + 1 == tokens.size() ? Token.EOF
                : tokens.get(i + 1)
                    .getType();
            if (kind == SFMLLexer.VARIABLE || kind == SFMLLexer.LBRACKET
                || kind == SFMLLexer.LBRACE
                || kind == SFMLLexer.DOT
                || kind == SFMLLexer.PLUS
                || kind == SFMLLexer.PERCENT
                || kind == SFMLLexer.BIT_NOT
                || kind == SFMLLexer.BIT_AND
                || kind == SFMLLexer.BIT_OR
                || kind == SFMLLexer.BIT_XOR
                || kind == SFMLLexer.SHIFT_LEFT
                || kind == SFMLLexer.SHIFT_RIGHT
                || kind == SFMLLexer.SHIFT_UNSIGNED_RIGHT) return true;
            if (kind == SFMLLexer.LPAREN
                && (StudioExpressionSyntax.comparison(prior) || !StudioExpressionSyntax.predicateGroupPrefix(prior)))
                return true;
            if (kind == SFMLLexer.NUMBER && (prior == SFMLLexer.LPAREN || prior == SFMLLexer.NOT
                || prior == SFMLLexer.AND
                || prior == SFMLLexer.OR)) return true;
            if (prior == SFMLLexer.LPAREN && (kind == SFMLLexer.AND || kind == SFMLLexer.OR
                || kind == SFMLLexer.WITH
                || kind == SFMLLexer.WITHOUT)) return true;
            if (kind == SFMLLexer.DASH && (prior == SFMLLexer.LPAREN || prior == SFMLLexer.NOT)) return true;
            if (StudioExpressionSyntax.comparison(kind) && prior == SFMLLexer.RPAREN) return true;
            if ((kind == SFMLLexer.TRUE || kind == SFMLLexer.FALSE || kind == SFMLLexer.STRING)
                && StudioExpressionSyntax.comparison(next)) return true;
            if (kind == SFMLLexer.STRING && (prior == SFMLLexer.LPAREN || prior == SFMLLexer.NOT)
                && next != SFMLLexer.HAS
                && next != SFMLLexer.COMMA
                && next != SFMLLexer.ROUND
                && next != SFMLLexer.SLOTS
                && next != SFMLLexer.SLOT
                && next != SFMLLexer.NORTH
                && next != SFMLLexer.EAST
                && next != SFMLLexer.SOUTH
                && next != SFMLLexer.WEST
                && next != SFMLLexer.TOP
                && next != SFMLLexer.BOTTOM
                && next != SFMLLexer.EACH
                && next != SFMLLexer.LEFT
                && next != SFMLLexer.RIGHT
                && next != SFMLLexer.FRONT
                && next != SFMLLexer.BACK
                && next != SFMLLexer.NULL) return true;
        }
        return false;
    }

    public static boolean hasData(ParseTree tree) {
        ArrayDeque<ParseTree> pending = new ArrayDeque<>();
        pending.push(tree);
        while (!pending.isEmpty()) {
            ParseTree current = pending.pop();
            if (current instanceof SFMLParser.DataExpressionContext) return true;
            for (int i = 0; i < current.getChildCount(); i++) pending.push(current.getChild(i));
        }
        return false;
    }

    private static String slice(String source, ParseTree tree) {
        org.antlr.v4.runtime.ParserRuleContext context = (org.antlr.v4.runtime.ParserRuleContext) tree;
        return source.substring(
            source.offsetByCodePoints(
                0,
                context.getStart()
                    .getStartIndex()),
            source.offsetByCodePoints(
                0,
                context.getStop()
                    .getStopIndex() + 1));
    }

    /** Flat preflight: quoted tokens are opaque; controls cannot smuggle in a statement wrapper. */
    private static int delimiters(List<Token> tokens) {
        ArrayDeque<Frame> frames = new ArrayDeque<>();
        frames.addLast(new Frame(Token.EOF));
        int maxDepth = 0, notRun = 0;
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            int kind = token.getType();
            if (kind == Token.EOF) break;
            Frame frame = frames.peekLast();
            boolean key = i + 1 < tokens.size() && tokens.get(i + 1)
                .getType() == SFMLLexer.COLON;
            if (!frame.member && !key
                && (kind == SFMLLexer.THEN || kind == SFMLLexer.ELSE || kind == SFMLLexer.END || kind == SFMLLexer.DO))
                throw new IllegalArgumentException("Condition fields cannot contain statement keywords");
            notRun = kind == SFMLLexer.NOT ? notRun + 1 : 0;
            if (notRun > 128) throw new IllegalArgumentException("Program nesting exceeds 128");
            if (kind == SFMLLexer.LPAREN || kind == SFMLLexer.LBRACKET || kind == SFMLLexer.LBRACE) {
                if (frames.size() > 128) throw new IllegalArgumentException("Program nesting exceeds 128");
                frames.addLast(
                    new Frame(
                        kind == SFMLLexer.LPAREN ? SFMLLexer.RPAREN
                            : kind == SFMLLexer.LBRACKET ? SFMLLexer.RBRACKET : SFMLLexer.RBRACE));
            } else if (kind == SFMLLexer.RPAREN || kind == SFMLLexer.RBRACKET || kind == SFMLLexer.RBRACE) {
                if (frames.size() == 1 || frame.close != kind)
                    throw new IllegalArgumentException("Unbalanced condition");
                frames.removeLast();
                frame = frames.peekLast();
                frame.operand = false;
                frame.member = false;
                frame.unary = 0;
            } else if (kind == SFMLLexer.DOT) {
                frame.member = true;
            } else if (!frame.member && !key
                && frame.operand
                && (kind == SFMLLexer.NOT || kind == SFMLLexer.PLUS
                    || kind == SFMLLexer.DASH
                    || kind == SFMLLexer.BIT_NOT)) {
                        frame.unary++;
                    } else {
                        frame.unary = 0;
                        frame.member = false;
                        frame.operand = kind == SFMLLexer.AND || kind == SFMLLexer.OR
                            || kind == SFMLLexer.COMMA
                            || kind == SFMLLexer.COLON
                            || StudioExpressionSyntax.operator(token);
                    }
            int active = frames.size() - 1;
            for (Frame ancestor : frames) active += ancestor.unary;
            maxDepth = Math.max(maxDepth, active);
        }
        if (frames.size() != 1) throw new IllegalArgumentException("Unbalanced condition");
        return maxDepth;
    }

    private static void complex() {
        throw new IllegalArgumentException("Condition exceeds editor nesting/chain budget; use Text");
    }
}
