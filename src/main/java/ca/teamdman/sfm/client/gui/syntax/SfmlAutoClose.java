package ca.teamdman.sfm.client.gui.syntax;

import java.util.List;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.sfm.client.gui.mui2.SfmlEditorBuffer;

/** Enter is one bounded, undoable edit. Tokenization happens on Enter only, never during drawing. */
public final class SfmlAutoClose {

    private static final Pattern SIGNATURE = Pattern
        .compile("(?is)[a-z_][a-z_0-9]*(?:\\s*\\[\\s*[0-9]*\\s*\\])*\\s+[a-z_][a-z_0-9.]*\\s*\\(.*\\)\\s*(?:do|then)");

    private SfmlAutoClose() {}

    public static void enter(SfmlEditorBuffer buffer, boolean enabled) {
        String source = buffer.source();
        int start = buffer.start(), end = buffer.end();
        int lineStart = source.lastIndexOf('\n', Math.max(-1, start - 1)) + 1;
        int indent = 0;
        while (lineStart + indent < start && source.charAt(lineStart + indent) == ' ') indent++;
        String base = source.substring(lineStart, lineStart + indent);
        String insertion = "\n" + base;
        int caret = insertion.length();
        if (!buffer.selected()) {
            SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(source));
            lexer.INCLUDE_UNUSED = true;
            lexer.removeErrorListeners();
            List<? extends Token> tokens = lexer.getAllTokens();
            int[] offsets = new int[source.codePointCount(0, source.length()) + 1];
            int cp = 0;
            for (int i = 0; i < source.length();) {
                offsets[cp++] = i;
                i += Character.charCount(source.codePointAt(i));
            }
            offsets[cp] = source.length();
            Token last = null, first = null, previous = null;
            boolean inString = false;
            for (Token token : tokens) {
                int from = offsets[token.getStartIndex()], to = offsets[token.getStopIndex() + 1];
                if (from >= start) break;
                if (token.getType() == SFMLLexer.UNUSED && token.getText()
                    .equals("\"")) inString = true;
                if (token.getType() == SFMLLexer.STRING && from < start && to > start) inString = true;
                if (token.getChannel() != Token.DEFAULT_CHANNEL || to > start || from < lineStart) continue;
                if (first == null) first = token;
                previous = last;
                last = token;
            }
            if (!inString && last != null && (previous == null || previous.getType() != SFMLLexer.DOT)) {
                int type = last.getType();
                String header = source.substring(lineStart, offsets[last.getStopIndex() + 1])
                    .trim();
                boolean branch = first.getType() == SFMLLexer.ELSE;
                boolean keyword = !branch && (type == SFMLLexer.THEN && first.getType() == SFMLLexer.IF
                    || type == SFMLLexer.DO && (first.getType() == SFMLLexer.EVERY || first.getType() == SFMLLexer.FOR
                        || first.getType() == SFMLLexer.WHILE
                        || first.getType() == SFMLLexer.FUNCTION)
                    || (type == SFMLLexer.DO || type == SFMLLexer.THEN) && SIGNATURE.matcher(header)
                        .matches());
                boolean opens = type == SFMLLexer.LBRACE || keyword;
                if (opens || branch && (type == SFMLLexer.THEN || type == SFMLLexer.ELSE)) {
                    insertion += "  ";
                    caret = insertion.length();
                }
                if (enabled && opens) {
                    int opening = offsets[last.getStartIndex()];
                    SfmlBlockMatches.Pair existing = null;
                    for (SfmlBlockMatches.Pair pair : SfmlBlockMatches.collect(tokens, offsets)) {
                        if (pair.start == opening) {
                            existing = pair;
                            break;
                        }
                    }
                    if (existing != null) {
                        int closingLine = source.lastIndexOf('\n', existing.closeStart - 1) + 1;
                        String closingIndent = source.substring(closingLine, existing.closeStart);
                        // While typing a nested header, keep a less-indented closer for its outer block.
                        if (closingLine > start && closingIndent.trim()
                            .isEmpty() && closingIndent.length() < indent) existing = null;
                    }
                    if (existing != null) {
                        // Split a compact empty pair without duplicating its existing closer.
                        String between = source.substring(start, existing.closeStart);
                        if (between.indexOf('\n') < 0 && between.trim()
                            .isEmpty()) {
                            end = existing.closeStart;
                            insertion += "\n" + base;
                        }
                    } else {
                        int lineEnd = source.indexOf('\n', start);
                        if (lineEnd < 0) lineEnd = source.length();
                        if (source.substring(start, lineEnd)
                            .trim()
                            .isEmpty()) {
                            String close = type == SFMLLexer.LBRACE ? "}"
                                : last.getText()
                                    .equals(
                                        last.getText()
                                            .toUpperCase(java.util.Locale.ROOT)) ? "END" : "end";
                            insertion += "\n" + base + close;
                        }
                    }
                }
            }
        }
        int anchor = buffer.anchor(), oldCaret = buffer.caret();
        buffer.select(start, end);
        try {
            buffer.insert(insertion);
            buffer.move(start + caret, false);
        } catch (IllegalArgumentException rejected) {
            buffer.select(anchor, oldCaret);
            throw rejected;
        }
    }
}
