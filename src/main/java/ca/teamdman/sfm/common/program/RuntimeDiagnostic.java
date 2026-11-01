package ca.teamdman.sfm.common.program;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import ca.teamdman.sfml.data.SfmlRuntimeException;

/** Bounded failure text only. Never retain a Throwable, execution frame, provider, or source buffer. */
public final class RuntimeDiagnostic {

    public static final int MAX_LINES = 16;
    private final List<String> lines;
    private final long timestamp;

    private RuntimeDiagnostic(List<String> lines) {
        this.lines = Collections.unmodifiableList(lines);
        timestamp = System.currentTimeMillis();
    }

    public static RuntimeDiagnostic capture(Exception failure, long tick, boolean manual) {
        return capture(failure, tick, manual, null);
    }

    public static RuntimeDiagnostic capture(Exception failure, long tick, boolean manual, RuntimeSourceMap sourceMap) {
        List<String> result = new ArrayList<>();
        add(
            result,
            "Last runtime failure (" + (manual ? "manual Run" : "scheduled run") + ", manager tick " + tick + ")");
        Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable root = failure;
        SfmlRuntimeException located = null;
        for (Throwable next = failure; next != null && seen.size() < 32 && seen.add(next); next = next.getCause()) {
            root = next;
            if (located == null && next instanceof SfmlRuntimeException
                && ((SfmlRuntimeException) next).getLocation() != null) located = (SfmlRuntimeException) next;
        }
        if (located != null) {
            ca.teamdman.sfml.data.SourceLocation location = sourceMap == null ? located.getLocation()
                : sourceMap.map(located.getLocation());
            String origin = location.isModuleLocation()
                ? "Library " + location.getModuleAlias() + " r" + location.getModuleRevision() + " line "
                : "SFML line ";
            add(
                result,
                origin + location
                    .getLine() + ", column " + (location.getColumn() + 1) + " | " + location.getStatementKind());
        } else add(result, "SFML source location unavailable for this failure.");
        add(
            result,
            "Cause: " + root.getClass()
                .getSimpleName());
        addMessage(result, root.getMessage() == null ? "No exception message was supplied." : root.getMessage());
        if (located != null) {
            List<String> functions = located.getFunctionContexts();
            for (int i = 0; i < Math.min(4, functions.size()); i++) add(result, "Function: " + functions.get(i));
            if (functions.size() > 4) add(result, "Additional function frames: " + (functions.size() - 4));
        }
        String context = located == null ? failure.getMessage() : located.getDetail();
        if (root != failure && context != null && !context.equals(root.getMessage())) {
            add(result, "Execution context:");
            addMessage(result, context);
        }
        String message = root.getMessage();
        if (message != null && message.substring(0, Math.min(message.length(), 1024))
            .toLowerCase(java.util.Locale.ROOT)
            .contains("budget"))
            add(
                result,
                "Hint: reduce work per run. A longer interval lowers frequency but does not increase a per-run limit.");
        add(result, "Locations refer to the installed program revision, not unsaved Studio edits.");
        return new RuntimeDiagnostic(result);
    }

    private static void addMessage(List<String> lines, String message) {
        // Keep both ends of a nested message without scanning or retaining an arbitrarily large provider payload.
        int limit = Math.min(message.length(), 1024);
        for (int start = 0; start < limit && lines.size() < MAX_LINES - 1; start += 384)
            add(lines, message.substring(start, Math.min(limit, start + 384)));
        if (message.length() > limit)
            add(lines, "[Message truncated] " + message.substring(Math.max(limit, message.length() - 256)));
    }

    private static void add(List<String> lines, String value) {
        if (lines.size() < MAX_LINES) lines.add(DebugValueFormatter.sanitize(value));
    }

    public List<String> lines() {
        return lines;
    }

    public long timestamp() {
        return timestamp;
    }
}
