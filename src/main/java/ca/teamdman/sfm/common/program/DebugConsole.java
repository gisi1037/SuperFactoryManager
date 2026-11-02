package ca.teamdman.sfm.common.program;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/** Server-owned, bounded heap history. The manager persists the explicit capture choice separately. */
public final class DebugConsole {

    public static final int MAX_LINES = 200;
    public static final int MAX_CHARACTERS = 16384;
    public static final int MAX_LINE_LENGTH = 512;
    private final Deque<String> lines = new ArrayDeque<>();
    private boolean enabled;
    private int characters;
    private long droppedLines;
    private final Runnable captureChanged;

    public DebugConsole() {
        this(false, () -> {});
    }

    /** Loading a saved choice must not mark the chunk dirty or replay previous output. */
    public DebugConsole(boolean enabled, Runnable captureChanged) {
        this.enabled = enabled;
        this.captureChanged = java.util.Objects.requireNonNull(captureChanged, "captureChanged");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        captureChanged.run();
    }

    /** Command replies may append even when PRINT capture is disabled. */
    public void append(String line) {
        String safe = DebugValueFormatter.sanitize(line);
        while (!lines.isEmpty() && (lines.size() >= MAX_LINES || characters + safe.length() > MAX_CHARACTERS)) {
            characters -= lines.removeFirst()
                .length();
            recordDroppedLine();
        }
        lines.addLast(safe);
        characters += safe.length();
    }

    public List<String> snapshot() {
        return Collections.unmodifiableList(new ArrayList<>(lines));
    }

    public void clear() {
        lines.clear();
        characters = 0;
        droppedLines = 0;
    }

    public long getDroppedLines() {
        return droppedLines;
    }

    public void recordDroppedLine() {
        if (droppedLines < Long.MAX_VALUE) droppedLines++;
    }
}
