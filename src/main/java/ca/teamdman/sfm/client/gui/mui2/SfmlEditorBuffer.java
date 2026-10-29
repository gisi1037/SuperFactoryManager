package ca.teamdman.sfm.client.gui.mui2;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/** Source and selection only. No GUI dependencies; view changes never rewrite or discard this draft. */
public final class SfmlEditorBuffer {

    public static final int MAX_CHARACTERS = 65536;
    private String source = "";
    private int caret, anchor;
    private final Deque<Edit> undo = new ArrayDeque<>(), redo = new ArrayDeque<>();

    private static final class Edit {

        final String source;
        final int caret, anchor;

        Edit(String source, int caret, int anchor) {
            this.source = source;
            this.caret = caret;
            this.anchor = anchor;
        }
    }

    public String source() {
        return source;
    }

    public int caret() {
        return caret;
    }

    public int anchor() {
        return anchor;
    }

    public int start() {
        return Math.min(caret, anchor);
    }

    public int end() {
        return Math.max(caret, anchor);
    }

    public boolean selected() {
        return caret != anchor;
    }

    public String selection() {
        return source.substring(start(), end());
    }

    public void load(String text) {
        String normalized = normalize(text);
        checkLength(normalized.length());
        source = normalized;
        caret = anchor = 0;
        undo.clear();
        redo.clear();
    }

    public void select(int anchor, int caret) {
        this.anchor = boundary(anchor);
        this.caret = boundary(caret);
    }

    public void move(int offset, boolean extend) {
        caret = boundary(offset);
        if (!extend) anchor = caret;
    }

    public int previous(int offset) {
        return offset <= 0 ? 0 : source.offsetByCodePoints(offset, -1);
    }

    public int next(int offset) {
        return offset >= source.length() ? source.length() : source.offsetByCodePoints(offset, 1);
    }

    public void insert(String text) {
        String normalized = normalize(text);
        checkLength((long) source.length() - (end() - start()) + normalized.length());
        String next = source.substring(0, start()) + normalized + source.substring(end());
        int offset = start() + normalized.length();
        if (!next.equals(source)) {
            undo.addLast(new Edit(source, caret, anchor));
            if (undo.size() > 100) undo.removeFirst();
            redo.clear();
            source = next;
        }
        caret = anchor = offset;
    }

    public void delete(boolean forward) {
        if (!selected()) {
            if (forward) select(caret, next(caret));
            else select(previous(caret), caret);
        }
        insert("");
    }

    /** Literal next/previous match with wrap. Finding changes selection only, never edit history. */
    public boolean find(String query, boolean backwards, boolean matchCase) {
        String needle = normalize(query);
        int[] matches = matches(needle, matchCase);
        if (matches.length == 0) return false;
        int found;
        if (backwards) {
            int before = start() - needle.length();
            found = matches[matches.length - 1];
            for (int i = matches.length - 1; i >= 0; i--) if (matches[i] <= before) {
                found = matches[i];
                break;
            }
        } else {
            int after = selected() ? end() : caret;
            found = matches[0];
            for (int offset : matches) if (offset >= after) {
                found = offset;
                break;
            }
        }
        select(found, found + needle.length());
        return true;
    }

    /** Replace only an exact selected match, then select the next match. Otherwise just find the next one. */
    public boolean replaceMatch(String query, String replacement, boolean matchCase) {
        String needle = normalize(query);
        if (needle.isEmpty() || end() - start() != needle.length() || !same(source, start(), needle, matchCase)) {
            find(needle, false, matchCase);
            return false;
        }
        insert(replacement);
        find(needle, false, matchCase);
        return true;
    }

    /** Nonoverlapping literal replacements, checked before allocation/mutation and committed as one undo step. */
    public int replaceAll(String query, String replacement, boolean matchCase) {
        String needle = normalize(query), inserted = normalize(replacement);
        int[] matches = matches(needle, matchCase);
        int count = 0, consumed = 0;
        for (int offset : matches) if (offset >= consumed) {
            count++;
            consumed = offset + needle.length();
        }
        if (count == 0) return 0;
        long length = source.length() + (long) count * (inserted.length() - needle.length());
        checkLength(length);
        StringBuilder result = new StringBuilder((int) length);
        consumed = 0;
        int afterLast = 0;
        for (int offset : matches) if (offset >= consumed) {
            result.append(source, consumed, offset)
                .append(inserted);
            consumed = offset + needle.length();
            afterLast = result.length();
        }
        result.append(source, consumed, source.length());
        String next = result.toString();
        if (!next.equals(source)) {
            undo.addLast(new Edit(source, caret, anchor));
            if (undo.size() > 100) undo.removeFirst();
            redo.clear();
            source = next;
            caret = anchor = afterLast;
        }
        return count;
    }

    /** KMP bounds work to source + query length even for repetitive large drafts. */
    private int[] matches(String needle, boolean matchCase) {
        if (needle.isEmpty() || needle.length() > source.length()) return new int[0];
        int[] prefix = new int[needle.length()];
        for (int i = 1, j = 0; i < needle.length(); i++) {
            char next = folded(needle.charAt(i), matchCase);
            while (j > 0 && next != folded(needle.charAt(j), matchCase)) j = prefix[j - 1];
            if (next == folded(needle.charAt(j), matchCase)) j++;
            prefix[i] = j;
        }
        int[] offsets = new int[source.length() - needle.length() + 1];
        int count = 0;
        for (int i = 0, j = 0; i < source.length(); i++) {
            char next = folded(source.charAt(i), matchCase);
            while (j > 0 && next != folded(needle.charAt(j), matchCase)) j = prefix[j - 1];
            if (next == folded(needle.charAt(j), matchCase)) j++;
            if (j == needle.length()) {
                int offset = i + 1 - needle.length();
                if (boundary(offset) == offset && boundary(i + 1) == i + 1) offsets[count++] = offset;
                j = prefix[j - 1];
            }
        }
        return Arrays.copyOf(offsets, count);
    }

    private static boolean same(String source, int offset, String needle, boolean matchCase) {
        for (int i = 0; i < needle.length(); i++)
            if (folded(source.charAt(offset + i), matchCase) != folded(needle.charAt(i), matchCase)) return false;
        return true;
    }

    private static char folded(char value, boolean matchCase) {
        // Fold one UTF-16 code unit without expanding text, so every result retains the original source offsets.
        return matchCase ? value : Character.toLowerCase(Character.toUpperCase(value));
    }

    public boolean undo() {
        return restore(undo, redo);
    }

    public boolean redo() {
        return restore(redo, undo);
    }

    private boolean restore(Deque<Edit> from, Deque<Edit> to) {
        if (from.isEmpty()) return false;
        to.addLast(new Edit(source, caret, anchor));
        Edit edit = from.removeLast();
        source = edit.source;
        caret = edit.caret;
        anchor = edit.anchor;
        return true;
    }

    private int boundary(int offset) {
        int result = Math.max(0, Math.min(offset, source.length()));
        if (result > 0 && result < source.length()
            && Character.isHighSurrogate(source.charAt(result - 1))
            && Character.isLowSurrogate(source.charAt(result))) result--;
        return result;
    }

    private static String normalize(String text) {
        // Even an all-CRLF input cannot shrink by more than half.
        if (text != null && text.length() > MAX_CHARACTERS * 2) checkLength(text.length());
        return text == null ? ""
            : text.replace("\r\n", "\n")
                .replace('\r', '\n');
    }

    private static void checkLength(long length) {
        if (length > MAX_CHARACTERS) throw new IllegalArgumentException(
            "Draft exceeds the 65536-character editor limit; source was kept unchanged");
    }
}
