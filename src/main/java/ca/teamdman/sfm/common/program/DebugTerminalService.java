package ca.teamdman.sfm.common.program;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.net.ManagerAccess;
import ca.teamdman.sfm.common.tileentity.LogEntry;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

/** Server-thread diagnostics only. Commands never evaluate source, flush STATE or access inventory views. */
public final class DebugTerminalService {

    public static final int MAX_COMMAND_LENGTH = 128;
    public static final int MAX_RESULT_LINES = 16;

    private DebugTerminalService() {}

    public static TileEntityManager authorizedManager(EntityPlayer player, int x, int y, int z, int windowId) {
        if (player == null || player.openContainer == null
            || windowId < 1
            || windowId > 100
            || player.openContainer.windowId != windowId) return null;
        return ManagerAccess.get(player, x, y, z);
    }

    public static List<String> execute(TileEntityManager manager, String command) {
        return execute(manager, command, null);
    }

    /** Actor identity comes from the authenticated server player, never from command text or disk metadata. */
    public static List<String> execute(TileEntityManager manager, String command, java.util.UUID actor) {
        if (manager == null || !manager.isLiveDiagnosticsThread())
            return Collections.singletonList("Diagnostics require the live server thread.");
        if (command == null || command.length() > MAX_COMMAND_LENGTH)
            return Collections.singletonList("Command too long (maximum 128 characters). Type help.");
        String trimmed = command.trim();
        if (trimmed.isEmpty()) return Collections.emptyList(); // Polls preserve the last command result.
        String[] words = trimmed.split("\\s+", 3);
        String verb = words[0].toLowerCase(Locale.ROOT);
        DebugConsole console = manager.getDebugConsole();
        switch (verb) {
            case "help":
                if (words.length != 1) break;
                List<String> help = new ArrayList<>();
                help.add("help | status | state [$name] | clear | capture on/off");
                help.add("labels [exact name] | logs [1..15] | watch [$name]");
                help.add("profile start [1..4096 updates] | profile stop/reset/status");
                help.add("Profile is opt-in full server-update time including idle; default 1200 updates.");
                help.add("labels reads saved bindings, not live connectivity or contents.");
                help.add("state reads live STATE; watch also reads initialized program-wide globals.");
                help.add("Reference variables require the source author; PRINT remains intentional output.");
                help.add("Watch is a bounded read-only snapshot; trigger/function locals are unavailable.");
                help.add("PRINT(expr[, expr...]) records up to 8 values when capture is on.");
                help.add("Capture is off by default and adds work while enabled.");
                help.add("clear removes PRINT history; capture/history are temporary manager memory.");
                help.add("Commands inspect diagnostics; arbitrary SFML evaluation is unavailable.");
                return help;
            case "status":
                if (words.length != 1) break;
                return Collections.singletonList(
                    "Manager " + (manager.isRunning() ? "running" : "stopped")
                        + "; compiled="
                        + manager.hasCompiledProgram()
                        + "; capture="
                        + (console.isEnabled() ? "ON" : "OFF")
                        + "; dropped="
                        + console.getDroppedLines()
                        + "; "
                        + clip(manager.getStatusMessage(), 256));
            case "state":
                if (words.length > 2) break;
                if (!manager.canInspectVariables(actor)) return variableAccessDenied();
                return inspectState(manager.getProgramState(), words.length == 2 ? words[1] : null);
            case "watch":
                if (words.length > 2) break;
                return manager.getLiveVariableSnapshot(words.length == 2 ? words[1] : null, actor);
            case "profile":
                return profileCommand(manager, words);
            case "labels":
                // The remainder is the exact label, including spaces and case; no expression parsing.
                String label = trimmed.substring(words[0].length())
                    .trim();
                return inspectLabels(manager.getDisk(), label.isEmpty() ? null : label);
            case "logs":
                if (words.length > 2) break;
                int count = 10;
                if (words.length == 2) {
                    try {
                        count = Integer.parseInt(words[1]);
                    } catch (NumberFormatException invalid) {
                        return Collections.singletonList("Use logs or logs <count>, with count from 1 to 15.");
                    }
                }
                if (count < 1 || count >= MAX_RESULT_LINES)
                    return Collections.singletonList("Log count must be from 1 to 15.");
                return inspectLogs(manager, count);
            case "clear":
                if (words.length != 1) break;
                console.clear();
                return Collections
                    .singletonList("PRINT history cleared. Capture is " + (console.isEnabled() ? "ON." : "OFF."));
            case "capture":
                if (words.length != 2 || !(words[1].equalsIgnoreCase("on") || words[1].equalsIgnoreCase("off"))) break;
                console.setEnabled(words[1].equalsIgnoreCase("on"));
                return Collections.singletonList(
                    "PRINT capture " + (console.isEnabled() ? "ON. Run a trigger to see output."
                        : "OFF. PRINT arguments and queries are skipped."));
            default:
                break;
        }
        return Collections.singletonList("Unknown command or arguments. Type help for supported commands.");
    }

    public static List<String> variableAccessDenied() {
        return Collections.singletonList(
            "Variable inspection is author-only for published programs; execution/copy permission is insufficient.");
    }

    private static List<String> profileCommand(TileEntityManager manager, String[] words) {
        if (words.length >= 2 && words[1].equalsIgnoreCase("start")) {
            int limit = ManagerExecutionProfiler.DEFAULT_UPDATE_LIMIT;
            if (words.length == 3) {
                try {
                    limit = Integer.parseInt(words[2]);
                } catch (NumberFormatException invalid) {
                    return Collections.singletonList("Use profile start [updates], with updates from 1 to 4096.");
                }
            }
            if (limit < 1 || limit > ManagerExecutionProfiler.MAX_UPDATE_LIMIT)
                return Collections.singletonList("Profile update limit must be from 1 to 4096.");
            manager.startExecutionProfile(limit);
            return Collections.singletonList(
                "Profile ON: next " + limit + " full server updates, including idle; auto-stops at the limit.");
        }
        if (words.length == 2) {
            if (words[1].equalsIgnoreCase("stop")) manager.stopExecutionProfile();
            else if (words[1].equalsIgnoreCase("reset")) {
                manager.resetExecutionProfile();
                return Collections.singletonList("Profiler reset; collection OFF.");
            } else if (!words[1].equalsIgnoreCase("status")) return Collections
                .singletonList("Use profile start [updates], profile stop, profile reset or profile status.");
            return inspectProfile(manager.getExecutionProfileSnapshot());
        }
        return Collections.singletonList("Use profile start [updates], profile stop, profile reset or profile status.");
    }

    public static List<String> inspectProfile(ManagerExecutionProfiler.Snapshot profile) {
        List<String> result = new ArrayList<>();
        result.add(
            "Profiler " + (profile.collecting ? "ON" : "OFF")
                + ": "
                + profile.count
                + "/"
                + profile.updateLimit
                + " full updates; due updates="
                + profile.dueUpdates
                + ".");
        result.add(
            String.format(
                Locale.ROOT,
                "ms avg=%.4f p50=%.4f p95=%.4f p99=%.4f max=%.4f",
                profile.averageMs,
                profile.p50Ms,
                profile.p95Ms,
                profile.p99Ms,
                profile.maximumMs));
        result.add("Full tile update includes idle, recovery, discovery and execution; nearest-rank percentiles.");
        result.add("Due updates began at least one trigger; async workers, UI callbacks and manual Run are excluded.");
        return result;
    }

    /** Values are immutable; inspect only a bounded display prefix and never initialize globals or run expressions. */
    public static List<String> inspectVariables(ProgramStateStore state, ca.teamdman.sfml.ast.Program globalPlan,
        ca.teamdman.sfml.data.ProgramGlobals globals, String requested) {
        String wanted = requested != null && requested.startsWith("$") ? requested.substring(1) : requested;
        List<String> result = new ArrayList<>();
        List<String> stateNames = state == null ? Collections.emptyList() : state.getNames();
        List<ca.teamdman.sfml.data.GlobalDeclaration> declarations = globalPlan == null ? Collections.emptyList()
            : globalPlan.getGlobalDeclarations();
        boolean initialized = globals != null && globals.isInitialized();
        int matches = 0;
        for (int i = 0; i < stateNames.size(); i++) {
            String name = stateNames.get(i);
            if (wanted != null && !wanted.equals(name)) continue;
            matches++;
            if (result.size() < MAX_RESULT_LINES - 1) result.add(variableLine("STATE", name, state.read(i)));
        }
        if (initialized) for (int i = 0; i < declarations.size(); i++) {
            String name = declarations.get(i)
                .getName();
            if (wanted != null && !wanted.equals(name)) continue;
            matches++;
            if (result.size() < MAX_RESULT_LINES - 1) result.add(variableLine("GLOBAL", name, globals.read(i)));
        }
        if (matches > result.size()) result.add("Showing " + result.size() + "/" + matches + ". Use watch $name.");
        if (result.isEmpty()) result.add(
            wanted == null ? "No initialized STATE or program-wide globals. Run the program to initialize globals."
                : "No initialized variable named $" + DebugValueFormatter.sanitize(clip(wanted, 64)) + ".");
        if (!initialized && !declarations.isEmpty() && result.size() < MAX_RESULT_LINES)
            result.add("Program-wide globals are not initialized; inspection does not run their initializers.");
        return result;
    }

    private static String variableLine(String scope, String name, ca.teamdman.sfml.data.DataValue value) {
        return DebugValueFormatter.sanitize(scope + " $" + clip(name, 64) + " = " + DebugValueFormatter.format(value));
    }

    /** Inspect only a bounded NBT prefix. Do not decode every label/position or query world providers. */
    public static List<String> inspectLabels(ItemStack disk, String requested) {
        NBTTagCompound root = disk == null ? null : disk.getTagCompound();
        if (root == null || !root.hasKey(LabelPositionHolder.NBT_KEY, 10))
            return Collections.singletonList("No saved labels on the manager disk.");
        NBTTagCompound labels = root.getCompoundTag(LabelPositionHolder.NBT_KEY);
        List<String> result = new ArrayList<>();
        if (requested == null) {
            java.util.Set<String> names = labels.func_150296_c();
            if (names.isEmpty()) return Collections.singletonList("No saved labels on the manager disk.");
            int shown = Math.min(names.size(), MAX_RESULT_LINES - 1);
            result.add(
                "Saved labels: showing " + shown + " / " + names.size() + ". Use labels <exact name> for positions.");
            for (String name : names) {
                if (result.size() >= MAX_RESULT_LINES) break;
                NBTTagList positions = positionList(labels.getTag(name));
                result.add(
                    DebugValueFormatter.sanitize(
                        clip(name, 128) + ": "
                            + (positions == null ? "invalid saved binding"
                                : positions.tagCount() + " saved position entries")));
            }
        } else {
            if (!labels.hasKey(requested)) return Collections.singletonList(
                "No saved label named " + DebugValueFormatter.sanitize(clip(requested, 128))
                    + ". Names are case-sensitive.");
            NBTTagList positions = positionList(labels.getTag(requested));
            if (positions == null) return Collections.singletonList("Invalid saved position list for this label.");
            int shown = Math.min(positions.tagCount(), MAX_RESULT_LINES - 1);
            result.add(
                "Saved positions: showing " + shown + " / " + positions.tagCount() + ". Connectivity not checked.");
            for (int i = 0; i < shown; i++) {
                int[] coordinates = positions.func_150306_c(i);
                result.add(
                    coordinates.length < 3 ? "[invalid coordinate entry]"
                        : coordinates[0] + ", " + coordinates[1] + ", " + coordinates[2]);
            }
        }
        return result;
    }

    private static NBTTagList positionList(NBTBase tag) {
        if (!(tag instanceof NBTTagList)) return null;
        NBTTagList list = (NBTTagList) tag;
        return list.func_150303_d() == 11 || list.tagCount() == 0 && list.func_150303_d() == 0 ? list : null;
    }

    private static List<String> inspectLogs(TileEntityManager manager, int count) {
        List<LogEntry> entries = manager.getRecentLogs(count);
        if (entries.isEmpty())
            return Collections.singletonList("No retained logs. Current log level: " + manager.getLogLevel() + ".");
        List<String> result = new ArrayList<>();
        result.add("Latest " + entries.size() + " log entries (oldest first); level " + manager.getLogLevel() + ".");
        for (LogEntry entry : entries) result.add(DebugValueFormatter.sanitize(entry.toString()));
        return result;
    }

    /** Read only the bounded requested prefix; never serialize/copy a whole STATE collection. */
    public static List<String> inspectState(ProgramStateStore state, String requested) {
        if (state == null || state.getNames()
            .isEmpty())
            return Collections
                .singletonList("No active persistent STATE. Compile a program with STATE declarations first.");
        List<String> names = state.getNames();
        List<String> result = new ArrayList<>();
        String wanted = requested != null && requested.startsWith("$") ? requested.substring(1) : requested;
        if (wanted != null) {
            int index = names.indexOf(wanted);
            if (index < 0) return Collections
                .singletonList("No STATE variable named $" + clip(wanted, 128) + ". Type state to list variables.");
            result.add("$" + clip(names.get(index), 64) + " = " + DebugValueFormatter.format(state.read(index)));
        } else {
            int shown = Math.min(names.size(), MAX_RESULT_LINES - 1);
            for (int i = 0; i < shown; i++)
                result.add("$" + clip(names.get(i), 64) + " = " + DebugValueFormatter.format(state.read(i)));
            if (shown < names.size())
                result.add("Showing " + shown + " / " + names.size() + ". Use state $name for one variable.");
        }
        return result;
    }

    public static String clip(String text, int maximum) {
        if (text == null) return "";
        if (text.length() <= maximum) return text;
        int end = maximum - 3;
        if (end > 0 && Character.isHighSurrogate(text.charAt(end - 1))) end--;
        return text.substring(0, end) + "...";
    }
}
