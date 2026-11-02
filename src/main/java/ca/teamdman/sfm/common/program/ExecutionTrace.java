package ca.teamdman.sfm.common.program;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.teamdman.sfml.data.DataCompiler.Binding;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.SourceLocation;

/** Opt-in RAM snapshots, never executable frames. No queries, evaluation, NBT or shared logs. */
public final class ExecutionTrace {

    public static final int MAX_EVENTS = 128;
    public static final int MAX_LOCALS = 8;
    public static final int LIFETIME_TICKS = 200;
    private final List<Event> events = new ArrayList<>();
    private final long started;
    private final int limit;
    private boolean recording = true;

    public ExecutionTrace(long tick, int limit) {
        if (limit < 1 || limit > MAX_EVENTS) throw new IllegalArgumentException("Trace limit must be 1..128");
        this.started = tick;
        this.limit = limit;
    }

    public static final class Event {

        private final int id;
        private final long tick;
        private final SourceLocation location;
        private final ExecutionTrace owner;
        private String outcome = "entered";
        private List<String> locals = Collections.emptyList();

        private Event(ExecutionTrace owner, int id, long tick, SourceLocation location) {
            this.owner = owner;
            this.id = id;
            this.tick = tick;
            this.location = location;
        }
    }

    public void expire(long tick) {
        if (tick < started || tick - started >= LIFETIME_TICKS) recording = false;
    }

    public void stop() {
        recording = false;
    }

    public Event begin(long tick, SourceLocation location) {
        expire(tick);
        if (!recording || location == null) return null;
        Event event = new Event(this, events.size() + 1, tick, location);
        events.add(event);
        if (events.size() >= limit) recording = false;
        return event;
    }

    /** Formatting has its own fixed limits and deliberately consumes none of the script's budgets. */
    public void finish(Event event, String outcome, DataRuntime runtime, Binding[] visible) {
        if (event == null || event.owner != this) return;
        event.outcome = outcome;
        if (runtime == null || visible == null || event.location.isModuleLocation()) return;
        List<String> values = new ArrayList<>();
        for (int i = 0; i < visible.length && i < MAX_LOCALS; i++) {
            Binding binding = visible[i];
            DataValue local = runtime.debugLocal(binding.slot);
            String value = runtime.isDebugReference(binding.slot) ? "<ref; inspect caller>"
                : local == null ? "<uninitialized>" : DebugValueFormatter.format(local);
            String name = binding.name.substring(0, Math.min(64, binding.name.length()));
            values.add(DebugValueFormatter.sanitize(name + " = " + value));
        }
        event.locals = Collections.unmodifiableList(values);
    }

    public List<String> status() {
        return Collections.singletonList(
            "Trace " + (recording ? "ON" : "OFF")
                + ": "
                + events.size()
                + "/"
                + limit
                + " statements; expires after 200 manager ticks. Use trace list or locals <event>. No execution is paused.");
    }

    public List<String> list(ca.teamdman.sfm.common.program.RuntimeSourceMap sourceMap, int requestedFirst) {
        List<String> result = new ArrayList<>();
        int first = requestedFirst == 0 ? Math.max(0, events.size() - 15) : requestedFirst - 1;
        if (first < 0 || first >= Math.max(1, events.size())) return Collections.singletonList("No such trace page.");
        int end = Math.min(events.size(), first + 15);
        result.add(
            "Trace events " + (events.isEmpty() ? "0" : first + 1)
                + ".."
                + end
                + "/"
                + events.size()
                + "; trace list <first> pages; locals <event> reads a snapshot.");
        for (int i = first; i < end; i++) {
            Event event = events.get(i);
            SourceLocation at = sourceMap == null ? event.location : sourceMap.map(event.location);
            result.add(
                "#" + event.id
                    + " tick "
                    + event.tick
                    + " | "
                    + (at.isModuleLocation() ? at.getModuleAlias() + " r" + at.getModuleRevision() + " | " : "")
                    + "line "
                    + at.getLine()
                    + ":"
                    + (at.getColumn() + 1)
                    + " "
                    + at.getStatementKind()
                    + " | "
                    + event.outcome);
        }
        return result;
    }

    public List<String> list(ca.teamdman.sfm.common.program.RuntimeSourceMap sourceMap) {
        return list(sourceMap, 0);
    }

    public List<String> locals(int id) {
        if (id < 1 || id > events.size()) return Collections.singletonList("No captured trace event " + id + ".");
        Event event = events.get(id - 1);
        List<String> result = new ArrayList<>();
        result.add(
            "Event #" + id
                + " "
                + event.outcome
                + ": up to 8 lexical locals after this attempt; values are snapshots.");
        result.addAll(event.locals);
        if (event.locals.isEmpty()) result.add(
            event.location.isModuleLocation() ? "Library locals are not exposed."
                : "No captured locals in this scope.");
        return result;
    }
}
