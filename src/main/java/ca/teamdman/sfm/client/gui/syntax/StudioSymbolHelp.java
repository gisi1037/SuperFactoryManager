package ca.teamdman.sfm.client.gui.syntax;

import ca.teamdman.sfm.client.gui.syntax.SfmlCompletionCatalog.Entry;
import ca.teamdman.sfm.client.gui.syntax.SfmlCompletionCatalog.Kind;
import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter.Style;

/** Cached lexer colors exclude comments/strings. Hover never reparses a program or guesses variable types. */
public final class StudioSymbolHelp {

    private StudioSymbolHelp() {}

    public static Entry at(String source, SfmlHighlighter.Highlighted colors, SfmlCompletionCatalog.Snapshot catalog,
        int offset) {
        if (source == null || colors == null
            || catalog == null
            || offset < 0
            || offset >= source.length()
            || source.length() > SfmlCompletionCatalog.MAX_SOURCE_CHARACTERS) return null;
        int line = 0, lineStart = 0;
        for (int i = 0; i < offset; i++) if (source.charAt(i) == '\n') {
            line++;
            lineStart = i + 1;
        }
        if (line >= colors.lines.size()) return null;
        Style style = colors.lines.get(line)
            .styleAt(offset - lineStart);
        if (style != Style.ACTION || !identifier(source.charAt(offset))) return null;
        int start = offset, end = offset + 1;
        while (start > 0 && identifier(source.charAt(start - 1))) start--;
        while (end < source.length() && identifier(source.charAt(end))) end++;
        int next = end;
        while (next < source.length() && Character.isWhitespace(source.charAt(next))) next++;
        if (next == source.length() || source.charAt(next) != '(') return null;
        String name = source.substring(start, end);
        int before = start - 1;
        while (before >= 0 && Character.isWhitespace(source.charAt(before))) before--;
        boolean member = before >= 0 && source.charAt(before) == '.';
        int qualifiedStart = start;
        while (qualifiedStart > 0
            && (identifier(source.charAt(qualifiedStart - 1)) || source.charAt(qualifiedStart - 1) == '.'))
            qualifiedStart--;
        String qualified = source.substring(qualifiedStart, end);
        // A declared namespace is exact-case and takes precedence over similarly named built-ins.
        for (Entry entry : catalog.entries()) if (entry.getKind() == Kind.PLAYER_FUNCTION && entry.getName()
            .equals(qualified)) return entry;
        for (Entry entry : catalog.entries()) {
            if (entry.getKind() == Kind.PLAYER_FUNCTION) continue;
            if ((entry.getKind() == Kind.METHOD) != member) continue;
            if (!member && qualifiedStart > 0 && source.charAt(qualifiedStart - 1) == '$') continue;
            if (entry.getName()
                .equalsIgnoreCase(name)) return entry;
        }
        return null;
    }

    private static boolean identifier(char value) {
        return value >= 'a' && value <= 'z' || value >= 'A' && value <= 'Z'
            || value >= '0' && value <= '9'
            || value == '_';
    }
}
