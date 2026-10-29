package ca.teamdman.sfml.ast;

import java.util.Arrays;

/**
 * Literal glob matching without regex backtracking. Only '*' and '?' are operators.
 * Matching takes O(pattern code points * text code points) worst-case work and constant
 * additional space. Wildcards consume Unicode code points, excluding Java regex's default
 * line terminators; case folding is ASCII-only, as with Pattern.CASE_INSENSITIVE alone.
 */
public final class GlobMatcher {

    private final int[] pattern;

    public GlobMatcher(String glob) {
        if (glob == null) throw new IllegalArgumentException("Glob is required");
        int[] points = new int[glob.length()];
        int size = 0;
        for (int offset = 0; offset < glob.length();) {
            int point = glob.codePointAt(offset);
            offset += Character.charCount(point);
            if (point != '*' || size == 0 || points[size - 1] != '*') points[size++] = fold(point);
        }
        pattern = Arrays.copyOf(points, size);
    }

    public boolean matches(CharSequence text) {
        if (text == null) return false;
        int position = 0, input = 0, star = -1, retry = 0;
        while (input < text.length()) {
            int point = Character.codePointAt(text, input);
            if (position < pattern.length && pattern[position] != '*'
                && (pattern[position] == '?' ? wildcard(point) : pattern[position] == fold(point))) {
                position++;
                input += Character.charCount(point);
            } else if (position < pattern.length && pattern[position] == '*') {
                star = position++;
                retry = input;
            } else if (star >= 0 && retry < text.length()) {
                int skipped = Character.codePointAt(text, retry);
                if (!wildcard(skipped)) return false;
                retry += Character.charCount(skipped);
                input = retry;
                position = star + 1;
            } else return false;
        }
        while (position < pattern.length && pattern[position] == '*') position++;
        return position == pattern.length;
    }

    private static int fold(int point) {
        return point >= 'A' && point <= 'Z' ? point + ('a' - 'A') : point;
    }

    private static boolean wildcard(int point) {
        return point != '\n' && point != '\r' && point != 0x85 && point != 0x2028 && point != 0x2029;
    }
}
