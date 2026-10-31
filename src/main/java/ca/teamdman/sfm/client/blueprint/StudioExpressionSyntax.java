package ca.teamdman.sfm.client.blueprint;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import ca.teamdman.langs.SFMLLexer;

/** Editor-only lexer guard. Bound recursive work before ANTLR/AST compilation, without parsing on edits. */
public final class StudioExpressionSyntax {

    public static final int MAX_DEPTH = 32, MAX_OPERATORS = 64, MAX_POSTFIX = 32;

    private static final class Frame {

        final int close;
        final boolean siblings;
        int operators, postfix, unary, maxOperators, maxPostfix;
        boolean operand = true, member;

        Frame(int close) {
            this(close, true);
        }

        Frame(int close, boolean siblings) {
            this.close = close;
            this.siblings = siblings;
        }

        void nextValue() {
            // Sibling values have independent budgets; nested groups still contribute to their parent.
            maxOperators = Math.max(maxOperators, operators);
            maxPostfix = Math.max(maxPostfix, postfix);
            operators = postfix = unary = 0;
            operand = true;
            member = false;
        }
    }

    private StudioExpressionSyntax() {}

    public static CommonTokenStream expression(String source) {
        return guarded(source, 0, "Print");
    }

    public static CommonTokenStream print(String source) {
        return guarded(source, SFMLLexer.PRINT, "Print");
    }

    public static CommonTokenStream transferExpression(String source) {
        return guarded(source, 0, "Transfer");
    }

    public static CommonTokenStream transfer(String source) {
        return guarded(source, SFMLLexer.TRANSFER, "Transfer");
    }

    /** Path work for one data island; real argument/container siblings keep their local maxima. */
    static int[] conditionWork(String source) {
        int[] work = new int[2];
        guarded(source, 0, "Condition", work);
        return work;
    }

    private static CommonTokenStream guarded(String source, int wrapper, String name) {
        return guarded(source, wrapper, name, null);
    }

    static void predicate(String source) {
        guarded(source, 0, "Condition", null, true);
    }

    private static CommonTokenStream guarded(String source, int wrapper, String name, int[] work) {
        return guarded(source, wrapper, name, work, false);
    }

    private static CommonTokenStream guarded(String source, int wrapper, String name, int[] work, boolean predicate) {
        boolean clause = wrapper != 0;
        if (source == null || source.length() > BlueprintGraph.MAX_CLAUSE
            || source.trim()
                .isEmpty()
            || source.indexOf('\n') >= 0
            || source.indexOf('\r') >= 0
            || source.indexOf('\0') >= 0)
            throw new IllegalArgumentException(name + " expressions must be one nonblank bounded line");
        SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(source));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new BaseErrorListener() {

            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object symbol, int line, int column, String message,
                RecognitionException cause) {
                throw new IllegalArgumentException("Invalid " + name + " expression: " + message);
            }
        });
        CommonTokenStream stream = new CommonTokenStream(lexer);
        stream.fill();
        List<Token> tokens = new ArrayList<>();
        for (Token token : stream.getTokens()) {
            if (token.getType() == SFMLLexer.LINE_COMMENT)
                throw new IllegalArgumentException("Comments are not " + name + " argument rows");
            if (token.getChannel() == Token.DEFAULT_CHANNEL) tokens.add(token);
        }
        String instruction = name.toUpperCase(java.util.Locale.ROOT);
        if (clause && (tokens.size() < 4 || tokens.get(0)
            .getType() != wrapper
            || tokens.get(1)
                .getType() != SFMLLexer.LPAREN))
            throw new IllegalArgumentException("Use one " + instruction + " clause");
        ArrayDeque<Frame> frames = new ArrayDeque<>();
        frames.addLast(new Frame(clause ? SFMLLexer.RPAREN : Token.EOF, !predicate));
        boolean closed = false;
        for (int i = clause ? 2 : 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            int kind = token.getType();
            if (kind == Token.EOF) break;
            if (closed)
                throw new IllegalArgumentException("Use one " + instruction + " clause without trailing tokens");
            Frame frame = frames.peekLast();
            if (kind == SFMLLexer.RPAREN || kind == SFMLLexer.RBRACKET || kind == SFMLLexer.RBRACE) {
                if (frame.close != kind) throw new IllegalArgumentException("Unbalanced " + name + " expression");
                if (frames.size() == 1) {
                    closed = true;
                    continue;
                }
                Frame child = frames.removeLast();
                frame = frames.peekLast();
                frame.operators += Math.max(child.maxOperators, child.operators);
                frame.postfix += Math.max(child.maxPostfix, child.postfix);
                if (frame.operators > MAX_OPERATORS || frame.postfix > MAX_POSTFIX) complex(name);
                frame.operand = false;
                frame.unary = 0;
                continue;
            }
            if (kind == SFMLLexer.COMMA) {
                if (!predicate && !clause && frames.size() == 1)
                    throw new IllegalArgumentException("Enter one expression per row");
                if (frame.siblings) frame.nextValue();
                else {
                    // Legacy HAS label/resource commas are not independent predicate expressions.
                    frame.operand = true;
                    frame.member = false;
                    frame.unary = 0;
                }
                continue;
            }
            if (kind == SFMLLexer.LPAREN || kind == SFMLLexer.LBRACKET || kind == SFMLLexer.LBRACE) {
                if (kind == SFMLLexer.LBRACKET && !frame.operand && ++frame.postfix > MAX_POSTFIX) complex(name);
                int depth = frames.size(); // Statement wrappers and raw expressions share the same root budget.
                for (Frame ancestor : frames) depth += ancestor.unary;
                if (depth > MAX_DEPTH) complex(name);
                int prior = i == 0 ? Token.EOF
                    : tokens.get(i - 1)
                        .getType();
                boolean siblings = !predicate || kind == SFMLLexer.LBRACE
                    || kind == SFMLLexer.LBRACKET && frame.operand
                    || kind == SFMLLexer.LPAREN && !predicateGroupPrefix(prior);
                frames.addLast(
                    new Frame(
                        kind == SFMLLexer.LPAREN ? SFMLLexer.RPAREN
                            : kind == SFMLLexer.LBRACKET ? SFMLLexer.RBRACKET : SFMLLexer.RBRACE,
                        siblings));
                continue;
            }
            if (kind == SFMLLexer.DOT) {
                if (++frame.postfix > MAX_POSTFIX) complex(name);
                frame.member = true;
                continue;
            }
            boolean key = i + 1 < tokens.size() && tokens.get(i + 1)
                .getType() == SFMLLexer.COLON;
            if (!frame.member && !key && operator(token)) {
                if (++frame.operators > MAX_OPERATORS) complex(name);
                if (frame.operand && (kind == SFMLLexer.NOT || kind == SFMLLexer.PLUS
                    || kind == SFMLLexer.DASH
                    || kind == SFMLLexer.BIT_NOT)) {
                    frame.unary++;
                    int depth = frames.size() - 1;
                    for (Frame ancestor : frames) depth += ancestor.unary;
                    if (depth > MAX_DEPTH) complex(name);
                } else frame.unary = 0;
                frame.operand = true;
            } else {
                frame.operand = kind == SFMLLexer.COLON;
                frame.member = false;
                frame.unary = 0;
            }
        }
        if (frames.size() != 1 || clause && !closed)
            throw new IllegalArgumentException("Unbalanced " + name + " expression");
        if (work != null) {
            Frame root = frames.peekLast();
            work[0] = Math.max(root.maxOperators, root.operators);
            work[1] = Math.max(root.maxPostfix, root.postfix);
        }
        return stream;
    }

    static boolean predicateGroupPrefix(int kind) {
        return kind == Token.EOF || kind == SFMLLexer.LPAREN
            || kind == SFMLLexer.NOT
            || kind == SFMLLexer.AND
            || kind == SFMLLexer.OR
            || kind == SFMLLexer.WITH
            || kind == SFMLLexer.WITHOUT
            || comparison(kind);
    }

    static boolean comparison(int kind) {
        return kind == SFMLLexer.GT || kind == SFMLLexer.GT_SYMBOL
            || kind == SFMLLexer.LT
            || kind == SFMLLexer.LT_SYMBOL
            || kind == SFMLLexer.EQ
            || kind == SFMLLexer.EQ_SYMBOL
            || kind == SFMLLexer.EQ_ALIAS
            || kind == SFMLLexer.NE_SYMBOL
            || kind == SFMLLexer.LE
            || kind == SFMLLexer.LE_SYMBOL
            || kind == SFMLLexer.GE
            || kind == SFMLLexer.GE_SYMBOL;
    }

    static boolean operator(Token token) {
        switch (token.getType()) {
            case SFMLLexer.NOT:
            case SFMLLexer.AND:
            case SFMLLexer.OR:
            case SFMLLexer.PLUS:
            case SFMLLexer.DASH:
            case SFMLLexer.SLASH:
            case SFMLLexer.PERCENT:
            case SFMLLexer.BIT_NOT:
            case SFMLLexer.BIT_AND:
            case SFMLLexer.BIT_OR:
            case SFMLLexer.BIT_XOR:
            case SFMLLexer.SHIFT_LEFT:
            case SFMLLexer.SHIFT_RIGHT:
            case SFMLLexer.SHIFT_UNSIGNED_RIGHT:
            case SFMLLexer.GT:
            case SFMLLexer.GT_SYMBOL:
            case SFMLLexer.LT:
            case SFMLLexer.LT_SYMBOL:
            case SFMLLexer.EQ:
            case SFMLLexer.EQ_SYMBOL:
            case SFMLLexer.EQ_ALIAS:
            case SFMLLexer.NE_SYMBOL:
            case SFMLLexer.LE:
            case SFMLLexer.LE_SYMBOL:
            case SFMLLexer.GE:
            case SFMLLexer.GE_SYMBOL:
                return true;
            default:
                return token.getType() == SFMLLexer.IDENTIFIER && token.getText()
                    .equals("*");
        }
    }

    private static void complex(String name) {
        throw new IllegalArgumentException(name + " expression exceeds editor nesting/chain budget; use Text");
    }
}
