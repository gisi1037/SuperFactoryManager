package ca.teamdman.sfm.common.program;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/** Server-owned, heap-only diagnostic history. Capture is explicitly enabled and never persisted. */
public final class DebugConsole {

    public static final int MAX_LINES = 200;
    public static final int MAX_CHARACTERS = 16384;
    public static final int MAX_LINE_LENGTH = 512;
    private final Deque<String> lines = new ArrayDeque<>();
    private boolean enabled;
    private int characters;
    private long droppedLines;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
