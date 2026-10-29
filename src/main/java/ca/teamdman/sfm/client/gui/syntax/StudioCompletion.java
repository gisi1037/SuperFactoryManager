package ca.teamdman.sfm.client.gui.syntax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.sfm.client.gui.syntax.SfmlCompletionCatalog.Entry;
import ca.teamdman.sfm.client.gui.syntax.SfmlCompletionCatalog.Kind;
import ca.teamdman.sfm.client.gui.syntax.SfmlCompletionCatalog.Snapshot;

/** Explicit Ctrl+Space completion only. Compiler diagnostics remain authoritative for receiver types and scope. */
public final class StudioCompletion {

    private static final int MAX_TOKENS = 16000;

    public static final class Candidate {

        private final Entry entry;
        private final int start, end;
        private final String text;

        private Candidate(Entry entry, int start, int end, String text) {
            this.entry = entry;
            this.start = start;
            this.end = end;
            this.text = text;
        }

        public String getName() {
            return entry.getName();
        }

        public String getSignature() {
            return entry.getSignature();
        }

        public String getHelp() {
            return entry.getDescription();
        }

        public Entry getEntry() {
            return entry;
        }

        /** Half-open UTF-16 replacement range, applied as one editor-buffer operation. */
        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public String getText() {
            return text;
        }
    }

    private StudioCompletion() {}

    public static List<Candidate> suggest(String source, int caret, int limit) {
        if (!valid(source, caret, limit)) return Collections.emptyList();
        return suggest(source, caret, SfmlCompletionCatalog.scan(source), limit);
    }

    /** Use the caller's current per-edit catalog snapshot to avoid scanning function headers again. */
    public static List<Candidate> suggest(String source, int caret, Snapshot catalog, int limit) {
        if (!valid(source, caret, limit) || catalog == null || insideText(source, caret))
            return Collections.emptyList();
        int start = caret;
        while (start > 0 && identifier(source.charAt(start - 1))) start--;
        int end = caret;
        while (end < source.length() && identifier(source.charAt(end))) end++;
        if (start > 0 && source.charAt(start - 1) == '$') {
            start--;
            String prefix = source.substring(start, caret)
                .toLowerCase(Locale.ROOT);
            List<Candidate> variables = new ArrayList<>();
            for (Entry entry : StudioVariables.visible(source, start, end)) {
                if (entry.getName()
                    .toLowerCase(Locale.ROOT)
                    .startsWith(prefix)) variables.add(new Candidate(entry, start, end, entry.getName()));
                if (variables.size() >= Math.min(limit, SfmlCompletionCatalog.MAX_ENTRIES)) break;
            }
            return Collections.unmodifiableList(variables);
        }

        List<Token> tokens = tokens(source);
        if (tokens == null) return Collections.emptyList();
        int[] offsets = offsets(source);
        int dot = start - 1;
        while (dot >= 0 && Character.isWhitespace(source.charAt(dot))) dot--;
        boolean method = false;
        if (dot >= 0 && source.charAt(dot) == '.') {
            int token = tokenAt(tokens, offsets, dot);
            method = token >= 0 && receiver(tokens, token - 1, 0);
        }
        if (!method) {
            if (dot >= 0 && source.charAt(dot) == '.' && dot != start - 1) return Collections.emptyList(); // Spaced,
                                                                                                           // unrecognized
                                                                                                           // field/namespace
                                                                                                           // path: keep
                                                                                                           // the raw
                                                                                                           // source.
            // Qualified player functions have exact namespace/case identities. Plain field paths are not methods.
            while (start > 0 && (identifier(source.charAt(start - 1)) || source.charAt(start - 1) == '.')) start--;
            while (end < source.length() && (identifier(source.charAt(end)) || source.charAt(end) == '.')) end++;
            if (start > 0 && (source.charAt(start - 1) == '$' || source.charAt(start - 1) == ':'))
                return Collections.emptyList();
            if (start < caret && !nameStart(source.charAt(start))) return Collections.emptyList();
        }
        String prefix = source.substring(start, caret)
            .toLowerCase(Locale.ROOT);
        int next = end;
        while (next < source.length() && Character.isWhitespace(source.charAt(next))) next++;
        boolean hasParenthesis = next < source.length() && source.charAt(next) == '(';
        List<Candidate> result = new ArrayList<>();
        int boundedLimit = Math.min(limit, SfmlCompletionCatalog.MAX_ENTRIES);
        for (Entry entry : catalog.entries()) {
            if ((entry.getKind() == Kind.METHOD) != method || !entry.getName()
                .toLowerCase(Locale.ROOT)
                .startsWith(prefix)) continue;
            String insertion = entry.getName();
            if (!hasParenthesis) {
                insertion = entry.getTemplate();
                if (method) {
                    int separator = insertion.indexOf('.');
                    if (separator < 0) continue;
                    insertion = insertion.substring(separator + 1);
                }
            }
            result.add(new Candidate(entry, start, end, insertion));
            if (result.size() >= boundedLimit) break;
        }
        return Collections.unmodifiableList(result);
    }

    private static boolean valid(String source, int caret, int limit) {
        return source != null && source.length() <= SfmlCompletionCatalog.MAX_SOURCE_CHARACTERS
            && caret >= 0
            && caret <= source.length()
            && limit > 0
            && !(caret > 0 && caret < source.length()
                && Character.isHighSurrogate(source.charAt(caret - 1))
                && Character.isLowSurrogate(source.charAt(caret)));
    }

    /** Match the grammar's quoted strings, escapes and -- comments, including incomplete editing tokens. */
    private static boolean insideText(String source, int caret) {
        boolean quoted = false, comment = false;
        for (int i = 0; i < caret; i++) {
            char c = source.charAt(i);
            if (comment) {
                if (c == '\n' || c == '\r') comment = false;
                continue;
            }
            if (quoted) {
                if (c == '\\') {
                    if (i + 1 < caret) i++;
                } else if (c == '"') quoted = false;
            } else if (c == '"') quoted = true;
            else if (c == '-' && i + 1 < source.length() && source.charAt(i + 1) == '-') {
                comment = true;
                i++;
            }
        }
        return quoted || comment;
    }

    private static List<Token> tokens(String source) {
        SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(source));
        lexer.INCLUDE_UNUSED = true;
        lexer.removeErrorListeners();
        List<Token> result = new ArrayList<>();
        for (int count = 0; count < MAX_TOKENS; count++) {
            Token token = lexer.nextToken();
            if (token.getType() == Token.EOF) return result;
            if (token.getChannel() == Token.DEFAULT_CHANNEL) result.add(token);
        }
        return null;
    }

    private static int[] offsets(String source) {
        int[] result = new int[source.codePointCount(0, source.length()) + 1];
        int codePoint = 0;
        for (int offset = 0; offset < source.length();) {
            result[codePoint++] = offset;
            offset += Character.charCount(source.codePointAt(offset));
        }
        result[codePoint] = source.length();
        return result;
    }

    private static int tokenAt(List<Token> tokens, int[] offsets, int position) {
        for (int i = tokens.size() - 1; i >= 0; i--) {
            int start = offsets[tokens.get(i)
                .getStartIndex()];
            if (start == position) return i;
            if (start < position) break;
        }
        return -1;
    }

    /** A bare variable may be a label; offer reference methods without inferring its type or declaring it valid. */
    private static boolean receiver(List<Token> tokens, int end, int depth) {
        if (end < 0 || depth > 16) return false;
        Token last = tokens.get(end);
        if (last.getType() == SFMLLexer.VARIABLE) return end == 0 || tokens.get(end - 1)
            .getType() != SFMLLexer.DOT;
        if (last.getType() != SFMLLexer.RPAREN) return false;
        int nesting = 1, open = end - 1;
        while (open >= 0) {
            int type = tokens.get(open)
                .getType();
            if (type == SFMLLexer.RPAREN) nesting++;
            else if (type == SFMLLexer.LPAREN && --nesting == 0) break;
            open--;
        }
        if (open < 1) return false;
        String method = tokens.get(open - 1)
            .getText();
        if (method.equalsIgnoreCase("label")) {
            return end == open + 2 && tokens.get(open + 1)
                .getType() == SFMLLexer.STRING
                && (open < 2 || tokens.get(open - 2)
                    .getType() != SFMLLexer.DOT);
        }
        if (!method.equalsIgnoreCase("side") && !method.equalsIgnoreCase("slots")) return false;
        return open >= 3 && tokens.get(open - 2)
            .getType() == SFMLLexer.DOT && receiver(tokens, open - 3, depth + 1);
    }

    private static boolean identifier(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_';
    }

    private static boolean nameStart(char c) {
        return identifier(c) && !(c >= '0' && c <= '9');
    }
}
