package ca.teamdman.sfm.integration.mui2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import ca.teamdman.sfm.common.tileentity.LogEntry;
import ca.teamdman.sfm.common.tileentity.LogLevel;

/** Pure bounded log filtering; display filters never change server recording. */
public final class ManagerLogView {

    private ManagerLogView() {}

    public static List<String> filter(List<LogEntry> entries, LogLevel minimum, String search) {
        String query = search == null ? "" : search.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        int start = Math.max(0, entries.size() - 200);
        for (int i = start; i < entries.size(); i++) {
            LogEntry entry = entries.get(i);
            if (minimum != null && !minimum.includes(entry.getLevel())) continue;
            String line = entry.toString();
            if (!line.toLowerCase(Locale.ROOT)
                .contains(query)) continue;
            long seconds = Math.floorMod(entry.getTimestamp() / 1000, 86400L);
            String time = String.format(Locale.ROOT, "%02d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60);
            result.add(time + " " + line);
        }
        return result.isEmpty() ? Collections.singletonList("No matching retained logs.") : result;
    }
}
