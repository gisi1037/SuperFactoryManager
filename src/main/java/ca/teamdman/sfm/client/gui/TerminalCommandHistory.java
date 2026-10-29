package ca.teamdman.sfm.client.gui;

import java.util.ArrayList;
import java.util.List;

/** Panel-local command recall. Polls are excluded and browsing preserves the unfinished command. */
public final class TerminalCommandHistory {

    public static final int MAX_ENTRIES = 32;
    private final List<String> entries = new ArrayList<>();
    private int cursor;
    private String draft = "";

    public void record(String command) {
        if (command == null || command.length() > 128
            || command.trim()
                .isEmpty())
            return;
        String text = command.trim();
        if (entries.isEmpty() || !entries.get(entries.size() - 1)
            .equals(text)) {
            entries.add(text);
            if (entries.size() > MAX_ENTRIES) entries.remove(0);
        }
        cursor = entries.size();
        draft = "";
    }

    public String previous(String current) {
        if (entries.isEmpty()) return current;
        if (cursor == entries.size()) draft = current;
        cursor = Math.max(0, cursor - 1);
        return entries.get(cursor);
    }

    public String next(String current) {
        if (cursor >= entries.size()) return current;
        cursor++;
        return cursor == entries.size() ? draft : entries.get(cursor);
    }
}
