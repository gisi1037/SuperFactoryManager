package ca.teamdman.sfm.client.gui.syntax;

/** One debounced, disposable ghost suggestion. No compiler, world access, threads or editing side effects. */
public final class StudioInlineCompletion {

    public static final long DELAY_NANOS = 200_000_000L;
    private static final String[] KEYWORDS = { "every", "input", "output", "from", "to", "retain", "forget", "round",
        "robin", "ticks", "seconds", "redstone", "if", "else", "then", "end", "let", "state", "function", "return",
        "while", "for", "in", "true", "false", "and", "or", "not", "do", "break", "ref", "void", "integer", "boolean",
        "string" };

    public static final class Suggestion {

        public final int start, end;
        public final String text, suffix;

        private Suggestion(int start, int end, String text, String prefix) {
            this.start = start;
            this.end = end;
            this.text = text;
            suffix = text.substring(prefix.length());
        }
    }

    private String source;
    private int caret;
    private long due;
    private boolean pending;
    private Suggestion suggestion;

    /** Call only after ordinary text input; navigation, paste, undo and programmatic edits do not schedule work. */
    public void typed(String text, int offset, boolean selected, long now) {
        dismiss();
        if (selected || text == null
            || text.length() > SfmlCompletionCatalog.MAX_SOURCE_CHARACTERS
            || offset < 2
            || offset > text.length()
            || offset < text.length() && text.charAt(offset) != '\n'
            || !identifier(text.charAt(offset - 1))) return;
        source = text;
        caret = offset;
        due = now + DELAY_NANOS;
        pending = true;
    }

    /** Cheap until the deadline; computes candidates at most once for a typing revision. */
    public void update(String text, int offset, boolean selected, long now) {
        if (!matches(text, offset, selected)) {
            dismiss();
            return;
        }
        if (!pending || now - due < 0) return;
        pending = false;
        int start = caret;
        while (start > 0 && identifier(source.charAt(start - 1))) start--;
        if (start > 0 && source.charAt(start - 1) == '$') start--;
        if (caret - start < 2 || StudioCompletion.insideText(source, caret)) return;
        String prefix = source.substring(start, caret);
        for (StudioCompletion.Candidate candidate : StudioCompletion
            .suggest(source, caret, SfmlCompletionCatalog.scan(source), SfmlCompletionCatalog.MAX_ENTRIES)) {
            String insertion = candidate.getText();
            String typed = source.substring(candidate.getStart(), caret);
            if (candidate.getEnd() == caret && insertion.startsWith(typed)
                && insertion.length() > typed.length()
                && insertion.indexOf('\n') < 0) {
                suggestion = new Suggestion(candidate.getStart(), caret, insertion, typed);
                return;
            }
        }
        // Do not turn an unrecognized field, selector or dollar variable into a language keyword.
        int previous = start - 1;
        while (previous >= 0 && Character.isWhitespace(source.charAt(previous))) previous--;
        if (previous >= 0 && ".$:".indexOf(source.charAt(previous)) >= 0 || prefix.charAt(0) == '$') return;
        for (String keyword : KEYWORDS) if (keyword.startsWith(prefix) && keyword.length() > prefix.length()) {
            suggestion = new Suggestion(start, caret, keyword, prefix);
            return;
        }
    }

    public Suggestion current(String text, int offset, boolean selected) {
        return matches(text, offset, selected) ? suggestion : null;
    }

    public void dismiss() {
        source = null;
        pending = false;
        suggestion = null;
    }

    private boolean matches(String text, int offset, boolean selected) {
        return source != null && !selected && caret == offset && source.equals(text);
    }

    private static boolean identifier(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '_';
    }
}
