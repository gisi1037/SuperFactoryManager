package ca.teamdman.sfm.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Bounded presentation of compiler output; diagnostic wording and navigation remain compiler-owned. */
public final class StudioProblems {

    public static final int MAX_ISSUES = 64, MAX_MESSAGE = 1024, MAX_CONTEXT = 180, MAX_SUMMARY = 16;

    private StudioProblems() {}

    public static final class Issue {

        public final EditorDiagnostic location;
        public final String heading, message, context;

        private Issue(String source, String error) {
            location = EditorDiagnostic.first(error);
            heading = location == null ? "Program issue"
                : "Line " + location.line + ", column " + ((long) location.column + 1);
            // Strip only the compiler's known location prefix. Do not reinterpret ANTLR messages.
            String prefix = location == null ? "" : "Line " + location.line + ", Col " + location.column + ":";
            int position = prefix.isEmpty() ? -1 : error.indexOf(prefix);
            message = bounded(
                position < 0 ? error
                    : error.substring(position + prefix.length())
                        .trim(),
                MAX_MESSAGE);
            context = location == null ? "" : sourceContext(source, location);
        }
    }

    public static final class Snapshot {

        public final List<Issue> issues;
        public final List<String> summary;
        public final int omitted;

        private Snapshot(List<Issue> issues, List<String> summary, int omitted) {
            this.issues = Collections.unmodifiableList(issues);
            this.summary = Collections.unmodifiableList(summary);
            this.omitted = omitted;
        }
    }

    public static Snapshot snapshot(String source, List<String> errors, List<String> summary) {
        List<Issue> issues = new ArrayList<>();
        for (int i = 0; i < Math.min(MAX_ISSUES, errors.size()); i++) {
            String error = errors.get(i);
            issues.add(new Issue(source, error == null ? "Unknown compiler issue" : error));
        }
        List<String> details = new ArrayList<>();
        for (int i = 0; i < Math.min(MAX_SUMMARY, summary.size()); i++) {
            String line = summary.get(i);
            if (line != null && !line.isEmpty()) details.add(bounded(line, MAX_MESSAGE));
        }
        int omitted = Math.max(0, errors.size() - MAX_ISSUES);
        if (omitted > 0) details.add(omitted + " more issues. Fix the first issues, then check again.");
        if (summary.size() > MAX_SUMMARY) details.add("Additional status details omitted.");
        return new Snapshot(issues, details, omitted);
    }

    private static String sourceContext(String source, EditorDiagnostic location) {
        int offset = location.offset(source);
        int start = source.lastIndexOf('\n', Math.max(-1, offset - 1)) + 1;
        int end = source.indexOf('\n', offset);
        if (end < 0) end = source.length();
        int before = source.codePointCount(start, offset), after = source.codePointCount(offset, end);
        int left = source.offsetByCodePoints(offset, -Math.min(before, MAX_CONTEXT / 2));
        int right = source.offsetByCodePoints(offset, Math.min(after, MAX_CONTEXT - Math.min(before, MAX_CONTEXT / 2)));
        String excerpt = source.substring(left, right);
        if (excerpt.endsWith("\r")) excerpt = excerpt.substring(0, excerpt.length() - 1);
        return location.line + " | " + (left > start ? "..." : "") + readable(excerpt) + (right < end ? "..." : "");
    }

    private static String bounded(String value, int limit) {
        // Bound the input before expanding tabs; even a malformed external diagnostic stays small.
        int count = value.codePointCount(0, value.length());
        if (count > limit) value = value.substring(0, value.offsetByCodePoints(0, limit));
        value = readable(value);
        int displayCount = value.codePointCount(0, value.length());
        boolean truncated = count > limit || displayCount > limit;
        String text = truncated ? value.substring(0, value.offsetByCodePoints(0, limit - 3)) : value;
        return text + (truncated ? "..." : "");
    }

    private static String readable(String value) {
        return value.replace('\r', ' ')
            .replace('\n', ' ')
            .replace("\t", "    ")
            .replace('\u00a7', '?');
    }
}
