package ca.teamdman.sfm.client.gui.syntax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
            Style style = classify(tokens, i);
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

    private static Style classify(List<? extends Token> tokens, int index) {
        int type = tokens.get(index)
            .getType();
        if (isIdentifier(type) && isResourcePart(tokens, index)) return Style.RESOURCE;
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
                return Style.LABEL;
            case SFMLLexer.COLON:
            case SFMLLexer.SLASH:
                return Style.RESOURCE;
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
            case SFMLLexer.GE_SYMBOL:
            case SFMLLexer.LE_SYMBOL:
            case SFMLLexer.AND:
            case SFMLLexer.OR:
            case SFMLLexer.NOT:
            case SFMLLexer.HASHTAG:
            case SFMLLexer.PLUS:
            case SFMLLexer.PERCENT:
            case SFMLLexer.DOT:
            case SFMLLexer.DASH:
                return Style.OPERATOR;
            case SFMLLexer.HAS:
            case SFMLLexer.OVERALL:
            case SFMLLexer.SOME:
            case SFMLLexer.ONE:
            case SFMLLexer.LONE:
            case SFMLLexer.FROM:
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
            case SFMLLexer.IDENTIFIER:
            case SFMLLexer.LET:
            case SFMLLexer.SET:
            case SFMLLexer.STATE:
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
