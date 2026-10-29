package ca.teamdman.sfm.client.gui.syntax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

import ca.teamdman.langs.SFMLLexer;

/** Lexer-based colors, cached by the editor on changes. No parsing or tokenizing in draw calls. */
public final class SfmlHighlighter {

    public enum Style {

        TEXT(0xdddddd),
        KEYWORD(0x569cd6),
        FLOW(0xc586c0),
        ACTION(0xdcdcaa),
        LABEL(0x9cdcfe),
        VARIABLE(0x82aaff),
        RESOURCE(0x4ec9b0),
        NUMBER(0xb5cea8),
        STRING(0xce9178),
        COMMENT(0x6a9955),
        OPERATOR(0xd7ba7d);

        public final int color;

        Style(int color) {
            this.color = color;
        }
    }

    /** Half-open UTF-16 offsets into the containing physical line. */
    public static final class Span {

        public final int start, end;
        public final Style style;

        Span(int start, int end, Style style) {
            this.start = start;
            this.end = end;
            this.style = style;
        }
    }

    public static final class Line {

        public final String text;
        public final List<Span> spans;

        Line(String text, List<Span> spans) {
            this.text = text;
            this.spans = Collections.unmodifiableList(spans);
        }

        public Style styleAt(int column) {
            for (Span span : spans) {
                if (column >= span.start && column < span.end) return span.style;
            }
            return Style.TEXT;
        }
    }

    public static final class Highlighted {

        public final List<Line> lines;

        Highlighted(List<Line> lines) {
            this.lines = Collections.unmodifiableList(lines);
        }
    }

    private SfmlHighlighter() {}

    public static Highlighted highlight(String source) {
        SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(source));
        // Keep unexpected characters while editing, without reporting lexer errors on each keystroke.
        lexer.INCLUDE_UNUSED = true;
        lexer.removeErrorListeners();
        List<? extends Token> tokens = lexer.getAllTokens();
        boolean[] functionNames = findFunctionNames(tokens);
        Context context = findContext(tokens, functionNames);
        Style[] colors = new Style[source.length()];
        Arrays.fill(colors, Style.TEXT);
        // ANTLR indexes code points; Java strings and the editor index UTF-16 code units.
        int[] offsets = new int[source.codePointCount(0, source.length()) + 1];
        int cp = 0;
        for (int i = 0; i < source.length();) {
            offsets[cp++] = i;
            i += Character.charCount(source.codePointAt(i));
        }
        offsets[cp] = source.length();
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            Style style = classify(tokens, i, functionNames, context);
            if (style != Style.TEXT) {
                Arrays.fill(colors, offsets[token.getStartIndex()], offsets[token.getStopIndex() + 1], style);
            }
        }
        List<Line> lines = new ArrayList<>();
        int start = 0;
        for (int end = 0; end <= source.length(); end++) {
            if (end != source.length() && source.charAt(end) != '\n') continue;
            List<Span> spans = new ArrayList<>();
            int run = start;
            while (run < end) {
                int next = run + 1;
                while (next < end && colors[next] == colors[run]) next++;
                spans.add(new Span(run - start, next - start, colors[run]));
                run = next;
            }
            lines.add(new Line(source.substring(start, end), spans));
            start = end + 1;
        }
        return new Highlighted(lines);
    }

    private static Style classify(List<? extends Token> tokens, int index, boolean[] functionNames, Context context) {
        int type = tokens.get(index)
            .getType();
        if (isFunctionType(tokens, index, context)) return Style.KEYWORD;
        if (isIdentifier(type) && isResourcePart(tokens, index)) return Style.RESOURCE;
        if (isTypedCall(tokens, index)) return Style.ACTION;
        if (functionNames[index]) return Style.ACTION;
        if (type == SFMLLexer.RISING || type == SFMLLexer.FALLING)
            return isNamedRedstoneEdge(tokens, index) ? Style.FLOW : Style.LABEL;
        if (type == SFMLLexer.FUNCTION || type == SFMLLexer.RETURNS || type == SFMLLexer.RETURN)
            return isFunctionFlow(tokens, index, functionNames) ? Style.FLOW : Style.LABEL;
        if (type == SFMLLexer.TRANSFER) return Style.LABEL; // Calls were already classified above.
        if (type == SFMLLexer.INTO) {
            int before = previousVisible(tokens, index), after = nextVisible(tokens, index);
            return before >= 0 && tokens.get(before)
                .getType() == SFMLLexer.RPAREN
                && after < tokens.size()
                && tokens.get(after)
                    .getType() == SFMLLexer.VARIABLE ? Style.KEYWORD : Style.LABEL;
        }
        if (type == SFMLLexer.FOR || type == SFMLLexer.WHILE || type == SFMLLexer.STEP)
            return isLoopFlow(tokens, index, context) ? Style.FLOW : Style.LABEL;
        switch (type) {
            case SFMLLexer.VARIABLE:
                return Style.VARIABLE;
            case SFMLLexer.LINE_COMMENT:
                return Style.COMMENT;
            case SFMLLexer.STRING:
                return Style.STRING;
            case SFMLLexer.NUMBER:
            case SFMLLexer.NUMBER_WITH_G_SUFFIX:
                return Style.NUMBER;
            case SFMLLexer.IDENTIFIER:
            case SFMLLexer.PRINT:
                return Style.LABEL;
            case SFMLLexer.COLON:
            case SFMLLexer.SLASH:
                return Style.RESOURCE;
            case SFMLLexer.TRANSFER:
            case SFMLLexer.INPUT:
            case SFMLLexer.OUTPUT:
            case SFMLLexer.FORGET:
                return Style.ACTION;
            case SFMLLexer.LET:
            case SFMLLexer.SET:
            case SFMLLexer.STATE:
            case SFMLLexer.EVERY:
            case SFMLLexer.IF:
            case SFMLLexer.THEN:
            case SFMLLexer.ELSE:
            case SFMLLexer.END:
            case SFMLLexer.DO:
            case SFMLLexer.TRUE:
            case SFMLLexer.FALSE:
                return Style.FLOW;
            case SFMLLexer.GT:
            case SFMLLexer.LT:
            case SFMLLexer.EQ:
            case SFMLLexer.GE:
            case SFMLLexer.LE:
            case SFMLLexer.GT_SYMBOL:
            case SFMLLexer.LT_SYMBOL:
            case SFMLLexer.EQ_SYMBOL:
            case SFMLLexer.EQ_ALIAS:
            case SFMLLexer.NE_SYMBOL:
            case SFMLLexer.GE_SYMBOL:
            case SFMLLexer.LE_SYMBOL:
            case SFMLLexer.AND:
            case SFMLLexer.OR:
            case SFMLLexer.NOT:
            case SFMLLexer.HASHTAG:
            case SFMLLexer.PLUS:
            case SFMLLexer.PERCENT:
            case SFMLLexer.BIT_AND:
            case SFMLLexer.BIT_OR:
            case SFMLLexer.BIT_XOR:
            case SFMLLexer.BIT_NOT:
            case SFMLLexer.SHIFT_LEFT:
            case SFMLLexer.SHIFT_RIGHT:
            case SFMLLexer.SHIFT_UNSIGNED_RIGHT:
            case SFMLLexer.DOT:
            case SFMLLexer.DASH:
                return Style.OPERATOR;
            case SFMLLexer.HAS:
            case SFMLLexer.OVERALL:
            case SFMLLexer.SOME:
            case SFMLLexer.ONE:
            case SFMLLexer.LONE:
            case SFMLLexer.FROM:
            case SFMLLexer.INTO:
            case SFMLLexer.TO:
            case SFMLLexer.WHERE:
            case SFMLLexer.SLOTS:
            case SFMLLexer.SLOT:
            case SFMLLexer.RETAIN:
            case SFMLLexer.EACH:
            case SFMLLexer.EXCEPT:
            case SFMLLexer.EMPTY:
            case SFMLLexer.IN:
            case SFMLLexer.WITHOUT:
            case SFMLLexer.WITH:
            case SFMLLexer.TAG:
            case SFMLLexer.ROUND:
            case SFMLLexer.ROBIN:
            case SFMLLexer.BY:
            case SFMLLexer.LABEL:
            case SFMLLexer.BLOCK:
            case SFMLLexer.TOP:
            case SFMLLexer.BOTTOM:
            case SFMLLexer.NORTH:
            case SFMLLexer.EAST:
            case SFMLLexer.SOUTH:
            case SFMLLexer.WEST:
            case SFMLLexer.SIDE:
            case SFMLLexer.LEFT:
            case SFMLLexer.RIGHT:
            case SFMLLexer.FRONT:
            case SFMLLexer.BACK:
            case SFMLLexer.NULL:
            case SFMLLexer.TICKS:
            case SFMLLexer.TICK:
            case SFMLLexer.SECONDS:
            case SFMLLexer.SECOND:
            case SFMLLexer.GLOBAL:
            case SFMLLexer.REDSTONE:
            case SFMLLexer.LEVEL:
            case SFMLLexer.LEVELS:
            case SFMLLexer.PULSE:
            case SFMLLexer.NAME:
                return Style.KEYWORD;
            default:
                // New grammar tokens stay neutral until their intended style is assigned explicitly.
                return Style.TEXT;
        }
    }

    private static boolean isTypedCall(List<? extends Token> tokens, int index) {
        Token token = tokens.get(index);
        if (token.getChannel() != Token.DEFAULT_CHANNEL) return false;
        int after = index + 1, before = index - 1;
        while (after < tokens.size() && tokens.get(after)
            .getChannel() != Token.DEFAULT_CHANNEL) after++;
        if (after == tokens.size() || tokens.get(after)
            .getType() != SFMLLexer.LPAREN) return false;
        while (before >= 0 && tokens.get(before)
            .getChannel() != Token.DEFAULT_CHANNEL) before--;
        int previous = before < 0 ? Token.EOF
            : tokens.get(before)
                .getType();
        String name = token.getText()
            .toUpperCase(Locale.ROOT);
        if (name.equals("COUNT") || name.equals("EMPTY")
            || name.equals("AVAILABLE")
            || name.equals("CONTENTS")
            || name.equals("REDSTONE")
            || name.equals("POWERED")
            || name.equals("REDSTONEAVAILABLE")
            || name.equals("SIDE")
            || name.equals("SLOTS")) return previous == SFMLLexer.DOT;
        return (name.equals("LABEL") || name.equals("ITEM") || name.equals("ORE") || name.equals("PRINT"))
            && previous != SFMLLexer.DOT
            && !isResourcePart(tokens, index);
    }

    private static boolean isFunctionType(List<? extends Token> tokens, int index, Context context) {
        String word = tokens.get(index)
            .getText()
            .toUpperCase(Locale.ROOT);
        if (!(word.equals("INTEGER") || word.equals("BOOLEAN")
            || word.equals("STRING")
            || word.equals("LABEL_REF")
            || word.equals("RESOURCE_SELECTOR")
            || word.equals("ITEM_KEY")
            || word.equals("ITEM_CONTENTS")
            || word.equals("ITEM_LOCATIONS"))) return false;
        return context.functionHeader[index];
    }

    private static final class Context {

        final boolean[] functionHeader, countedStep;

        Context(int size) {
            functionHeader = new boolean[size];
            countedStep = new boolean[size];
        }
    }

    /** Keep the old nearest-keyword rules, but visit each token once instead of rescanning every prefix. */
    private static Context findContext(List<? extends Token> tokens, boolean[] functionNames) {
        Context result = new Context(tokens.size());
        boolean function = false, countedFor = false, hasTo = false;
        for (int i = 0; i < tokens.size(); i++) {
            result.functionHeader[i] = function;
            result.countedStep[i] = countedFor && hasTo;
            Token token = tokens.get(i);
            if (token.getChannel() != Token.DEFAULT_CHANNEL) continue;
            int type = token.getType();
            if (type == SFMLLexer.FUNCTION) function = isFunctionFlow(tokens, i, functionNames);
            else if (type == SFMLLexer.DO || type == SFMLLexer.END
                || type == SFMLLexer.INPUT
                || type == SFMLLexer.OUTPUT
                || type == SFMLLexer.LET
                || type == SFMLLexer.STATE) function = false;
            if (type == SFMLLexer.FOR) {
                int after = nextVisible(tokens, i);
                countedFor = after < tokens.size() && tokens.get(after)
                    .getType() == SFMLLexer.VARIABLE;
                hasTo = false;
            } else if (type == SFMLLexer.TO) hasTo = true;
            else if (type == SFMLLexer.DO || type == SFMLLexer.END
                || type == SFMLLexer.INPUT
                || type == SFMLLexer.OUTPUT
                || type == SFMLLexer.FORGET) {
                    countedFor = hasTo = false;
                }
        }
        return result;
    }

    /** Classify each dotted chain once instead of walking it again for every word. */
    private static boolean[] findFunctionNames(List<? extends Token> tokens) {
        boolean[] names = new boolean[tokens.size()];
        for (int first = 0; first < tokens.size(); first++) {
            if (!isFunctionIdentifier(
                tokens.get(first)
                    .getType()))
                continue;
            int last = first;
            for (int dot = nextVisible(tokens, last); dot < tokens.size() && tokens.get(dot)
                .getType() == SFMLLexer.DOT;) {
                int word = nextVisible(tokens, dot);
                if (word >= tokens.size() || !isFunctionIdentifier(
                    tokens.get(word)
                        .getType()))
                    break;
                last = word;
                dot = nextVisible(tokens, last);
            }
            int before = previousVisible(tokens, first), after = nextVisible(tokens, last);
            if ((before < 0 || tokens.get(before)
                .getType() != SFMLLexer.DOT) && after < tokens.size()
                && tokens.get(after)
                    .getType() == SFMLLexer.LPAREN) {
                for (int word = first; word <= last; word++) {
                    names[word] = isFunctionIdentifier(
                        tokens.get(word)
                            .getType());
                }
            }
            first = last;
        }
        return names;
    }

    private static boolean isFunctionIdentifier(int type) {
        return type == SFMLLexer.IDENTIFIER || type == SFMLLexer.RISING
            || type == SFMLLexer.FALLING
            || type == SFMLLexer.TRANSFER
            || type == SFMLLexer.INTO;
    }

    private static boolean isNamedRedstoneEdge(List<? extends Token> tokens, int index) {
        int before = previousVisible(tokens, index), after = nextVisible(tokens, index);
        if (before < 0 || tokens.get(before)
            .getType() != SFMLLexer.REDSTONE
            || after >= tokens.size()
            || tokens.get(after)
                .getType() != SFMLLexer.FROM)
            return false;
        int every = previousVisible(tokens, before);
        return every >= 0 && tokens.get(every)
            .getType() == SFMLLexer.EVERY;
    }

    private static boolean isFunctionFlow(List<? extends Token> tokens, int index, boolean[] functionNames) {
        int before = previousVisible(tokens, index), after = nextVisible(tokens, index);
        int previous = before >= 0 ? tokens.get(before)
            .getType() : Token.EOF;
        int next = after < tokens.size() ? tokens.get(after)
            .getType() : Token.EOF;
        if (previous == SFMLLexer.DOT || previous == SFMLLexer.FROM
            || previous == SFMLLexer.TO
            || previous == SFMLLexer.EACH
            || previous == SFMLLexer.FORGET
            || previous == SFMLLexer.COMMA) return false;
        if (tokens.get(index)
            .getType() == SFMLLexer.FUNCTION) return after < tokens.size() && functionNames[after];
        if (tokens.get(index)
            .getType() == SFMLLexer.RETURNS) return previous == SFMLLexer.RPAREN;
        return next == SFMLLexer.VARIABLE || next == SFMLLexer.NUMBER
            || next == SFMLLexer.STRING
            || next == SFMLLexer.LPAREN
            || next == SFMLLexer.LBRACKET
            || next == SFMLLexer.LBRACE
            || next == SFMLLexer.TRUE
            || next == SFMLLexer.FALSE
            || next == SFMLLexer.NOT
            || next == SFMLLexer.PLUS
            || next == SFMLLexer.DASH
            || next == SFMLLexer.BIT_NOT
            || isIdentifier(next);
    }

    /** Loop words remain legal legacy labels/resources; color the new header syntax only. */
    private static boolean isLoopFlow(List<? extends Token> tokens, int index, Context context) {
        int after = nextVisible(tokens, index);
        int next = after < tokens.size() ? tokens.get(after)
            .getType() : Token.EOF;
        int before = previousVisible(tokens, index);
        int previous = before >= 0 ? tokens.get(before)
            .getType() : Token.EOF;
        if (previous == SFMLLexer.DOT) return false;
        switch (tokens.get(index)
            .getType()) {
            case SFMLLexer.FOR:
                return next == SFMLLexer.VARIABLE;
            case SFMLLexer.WHILE:
                if (previous == SFMLLexer.FROM || previous == SFMLLexer.TO
                    || previous == SFMLLexer.EACH
                    || previous == SFMLLexer.COMMA
                    || previous == SFMLLexer.FORGET) return false;
                return next == SFMLLexer.LPAREN || next == SFMLLexer.TRUE
                    || next == SFMLLexer.FALSE
                    || next == SFMLLexer.NOT
                    || next == SFMLLexer.REDSTONE
                    || next == SFMLLexer.OVERALL
                    || next == SFMLLexer.SOME
                    || next == SFMLLexer.EVERY
                    || next == SFMLLexer.ONE
                    || next == SFMLLexer.LONE
                    || isIdentifier(next);
            case SFMLLexer.STEP:
                return context.countedStep[index];
            default:
                return false;
        }
    }

    private static int nextVisible(List<? extends Token> tokens, int index) {
        do {
            index++;
        } while (index < tokens.size() && tokens.get(index)
            .getChannel() != Token.DEFAULT_CHANNEL);
        return index;
    }

    private static int previousVisible(List<? extends Token> tokens, int index) {
        do {
            index--;
        } while (index >= 0 && tokens.get(index)
            .getChannel() != Token.DEFAULT_CHANNEL);
        return index;
    }

    private static boolean isResourcePart(List<? extends Token> tokens, int index) {
        int before = index - 1;
        int after = index + 1;
        while (before >= 0 && tokens.get(before)
            .getChannel() != Token.DEFAULT_CHANNEL) before--;
        while (after < tokens.size() && tokens.get(after)
            .getChannel() != Token.DEFAULT_CHANNEL) after++;
        int previous = before >= 0 ? tokens.get(before)
            .getType() : Token.EOF;
        int next = after < tokens.size() ? tokens.get(after)
            .getType() : Token.EOF;
        return previous == SFMLLexer.COLON || previous == SFMLLexer.SLASH
            || previous == SFMLLexer.HASHTAG
            || previous == SFMLLexer.TAG
            || next == SFMLLexer.COLON
            || next == SFMLLexer.SLASH;
    }

    // Match the grammar's identifier rule, including words that also have keyword tokens.
    private static boolean isIdentifier(int type) {
        switch (type) {
            case SFMLLexer.TRANSFER:
            case SFMLLexer.INTO:
            case SFMLLexer.IDENTIFIER:
            case SFMLLexer.LET:
            case SFMLLexer.SET:
            case SFMLLexer.STATE:
            case SFMLLexer.PRINT:
            case SFMLLexer.FUNCTION:
            case SFMLLexer.RETURNS:
            case SFMLLexer.RETURN:
            case SFMLLexer.FOR:
            case SFMLLexer.WHILE:
            case SFMLLexer.STEP:
            case SFMLLexer.RISING:
            case SFMLLexer.FALLING:
            case SFMLLexer.REDSTONE:
            case SFMLLexer.LEVEL:
            case SFMLLexer.LEVELS:
            case SFMLLexer.GLOBAL:
            case SFMLLexer.SECOND:
            case SFMLLexer.SECONDS:
            case SFMLLexer.TOP:
            case SFMLLexer.BOTTOM:
            case SFMLLexer.LEFT:
            case SFMLLexer.RIGHT:
            case SFMLLexer.FRONT:
            case SFMLLexer.BACK:
                return true;
            default:
                return false;
        }
    }
}
