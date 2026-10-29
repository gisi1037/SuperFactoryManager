package ca.teamdman.sfm.client.gui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** SFML diagnostics use one-based lines and zero-based Unicode code-point columns. */
public final class EditorDiagnostic {

    private static final Pattern LOCATION = Pattern.compile("\\bLine ([0-9]+), Col ([0-9]+):");
    public final int line, column;

    private EditorDiagnostic(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public static EditorDiagnostic first(String message) {
        if (message == null) return null;
        Matcher match = LOCATION.matcher(message);
        if (!match.find()) return null;
        try {
            int line = Integer.parseInt(match.group(1)), column = Integer.parseInt(match.group(2));
            return line > 0 ? new EditorDiagnostic(line, column) : null;
        } catch (NumberFormatException invalid) {
            return null;
        }
    }

    /** Clamp stale EOF/columns without landing inside a supplementary Unicode character. */
    public int offset(String source) {
        int start = 0;
        for (int row = 1; row < line; row++) {
            int end = source.indexOf('\n', start);
            if (end < 0) return source.length();
            start = end + 1;
        }
        int end = source.indexOf('\n', start);
        if (end < 0) end = source.length();
        return source.offsetByCodePoints(start, Math.min(column, source.codePointCount(start, end)));
    }
}
