package ca.teamdman.sfm.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Fixed command vocabulary only: no network/provider lookup or implicit execution. */
public final class TerminalCommandCompletion {

    private static final List<String> COMMANDS = Arrays
        .asList("help", "status", "state", "labels", "logs", "clear", "capture on", "capture off");
    private final List<String> matches = new ArrayList<>();
    private String last;
    private int index;

    public String complete(String text, boolean reverse) {
        if (text == null || text.length() > 128) {
            reset();
            return text;
        }
        if (!text.equals(last)) {
            matches.clear();
            String prefix = text.trim()
                .toLowerCase(Locale.ROOT);
            for (String command : COMMANDS) if (command.startsWith(prefix)) matches.add(command);
            index = reverse ? 0 : -1;
        }
        if (matches.isEmpty()) {
            last = null;
            return text;
        }
        index = Math.floorMod(index + (reverse ? -1 : 1), matches.size());
        last = matches.get(index);
        return last;
    }

    public void reset() {
        matches.clear();
        last = null;
        index = 0;
    }
}
