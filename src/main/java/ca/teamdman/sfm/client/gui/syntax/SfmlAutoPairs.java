package ca.teamdman.sfm.client.gui.syntax;

import ca.teamdman.sfm.client.gui.mui2.SfmlEditorBuffer;

/** Typing assistance only: scan a bounded prefix on punctuation input, never on a render or a plain paste. */
public final class SfmlAutoPairs {

    private SfmlAutoPairs() {}

    public static void type(SfmlEditorBuffer buffer, String text, boolean enabled) {
        if (!enabled || text == null || text.length() != 1) {
            buffer.insert(text);
            return;
        }
        char typed = text.charAt(0);
        if (typed != '(' && typed != ')' && typed != '"') {
            buffer.insert(text);
            return;
        }
        String source = buffer.source();
        int start = buffer.start(), end = buffer.end();
        Context context = context(source, start);
        char next = start < source.length() ? source.charAt(start) : 0;
        if (!buffer.selected() && !context.comment
            && next == typed
            && (typed == ')' && !context.string && context.rounds > 0
                || typed == '"' && context.string && !context.escaped)) {
            // A closer already exists. Moving the caret is not a source edit and must retain redo history.
            buffer.move(start + 1, false);
            return;
        }
        if ((typed == '(' || typed == '"') && !context.comment
            && !context.string
            && (buffer.selected() || mayCloseBefore(next))) {
            char closing = typed == '(' ? ')' : '"';
            int anchor = buffer.anchor(), caret = buffer.caret();
            String selected = source.substring(start, end);
            // insert checks the complete resulting size before touching source, selection or history.
            buffer.insert(typed + selected + closing);
            if (start == end) buffer.move(start + 1, false);
            else buffer.select(anchor + 1, caret + 1);
            return;
        }
        buffer.insert(text);
    }

    public static void delete(SfmlEditorBuffer buffer, boolean forward, boolean enabled) {
        int caret = buffer.caret();
        String source = buffer.source();
        if (enabled && !forward && !buffer.selected() && caret > 0 && caret < source.length()) {
            char opening = source.charAt(caret - 1), closing = source.charAt(caret);
            if (opening == '(' && closing == ')' || opening == '"' && closing == '"') {
                Context context = context(source, caret - 1);
                if (!context.string && !context.comment) {
                    buffer.replaceRange(caret - 1, caret + 1, "");
                    return;
                }
            }
        }
        buffer.delete(forward);
    }

    private static boolean mayCloseBefore(char next) {
        return next == 0 || Character.isWhitespace(next) || ")]},;:".indexOf(next) >= 0;
    }

    private static final class Context {

        boolean string, comment, escaped;
        int rounds;
    }

    /** SFML uses double-quoted, possibly multiline strings and -- line comments. Escapes consume one character. */
    private static Context context(String source, int end) {
        Context state = new Context();
        for (int i = 0; i < end; i++) {
            char c = source.charAt(i);
            if (state.comment) {
                if (c == '\n') state.comment = false;
            } else if (state.string) {
                if (state.escaped) state.escaped = false;
                else if (c == '\\') state.escaped = true;
                else if (c == '"') state.string = false;
            } else if (c == '-' && i + 1 < end && source.charAt(i + 1) == '-') {
                state.comment = true;
                i++;
            } else if (c == '"') state.string = true;
            else if (c == '(') state.rounds++;
            else if (c == ')') state.rounds = Math.max(0, state.rounds - 1);
        }
        return state;
    }
}
