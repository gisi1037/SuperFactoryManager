package ca.teamdman.sfm.client.gui.syntax;

import java.util.ArrayDeque;
import java.util.Deque;

/** Editing aid only: bounded delimiter scan; cached by source/caret, never a second type checker. */
public final class StudioCallHelp {

    private StudioCallHelp() {}

    public static final class Help {

        public final SfmlCompletionCatalog.Entry entry;
        public final int argument;

        private Help(SfmlCompletionCatalog.Entry entry, int argument) {
            this.entry = entry;
            this.argument = argument;
        }
    }

    private static final class Frame {

        final char delimiter;
        final int offset;
        int argument;

        Frame(char delimiter, int offset) {
            this.delimiter = delimiter;
            this.offset = offset;
        }
    }

    public static Help at(String source, int caret, SfmlHighlighter.Highlighted colors,
        SfmlCompletionCatalog.Snapshot catalog) {
        if (source == null || caret < 0
            || caret > source.length()
            || source.length() > SfmlCompletionCatalog.MAX_SOURCE_CHARACTERS) return null;
        Deque<Frame> frames = new ArrayDeque<>();
        boolean quoted = false, comment = false;
        for (int i = 0; i < caret; i++) {
            char c = source.charAt(i);
            if (comment) {
                if (c == '\n' || c == '\r') comment = false;
            } else if (quoted) {
                if (c == '\\' && i + 1 < caret) i++;
                else if (c == '"') quoted = false;
            } else if (c == '"') quoted = true;
            else if (c == '-' && i + 1 < caret && source.charAt(i + 1) == '-') {
                comment = true;
                i++;
            } else if (c == '(' || c == '[' || c == '{') {
                if (frames.size() >= 64) return null;
                frames.push(new Frame(c, i));
            } else if (c == ')' || c == ']' || c == '}') {
                char expected = c == ')' ? '(' : c == ']' ? '[' : '{';
                if (frames.isEmpty() || frames.pop().delimiter != expected) return null;
            } else if (c == ',' && !frames.isEmpty()) frames.peek().argument++;
        }
        if (comment) return null;
        for (Frame frame : frames) {
            if (frame.delimiter != '(') continue;
            int name = frame.offset - 1;
            while (name >= 0 && Character.isWhitespace(source.charAt(name))) name--;
            if (name < 0) continue;
            SfmlCompletionCatalog.Entry entry = StudioSymbolHelp.at(source, colors, catalog, name);
            if (entry != null) return new Help(entry, frame.argument);
        }
        return null;
    }
}
