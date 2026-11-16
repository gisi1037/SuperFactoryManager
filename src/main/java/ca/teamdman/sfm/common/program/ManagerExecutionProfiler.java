package ca.teamdman.sfm.common.program;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;

import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.SourceLocation;

/** Opt-in, server-thread sample window. Statement timings include any nested work. */
public final class ManagerExecutionProfiler {

    public static final int DEFAULT_UPDATE_LIMIT = 1200;
    public static final int MAX_UPDATE_LIMIT = 4096;
    private static final int MAX_STATEMENTS = 256, MAX_TRIGGERS = 256, MAX_STATEMENT_ATTEMPTS_PER_UPDATE = 2048;
    private final long[] samples;
    private final long[] gameTicks, programSamples;
    private final boolean[] due;
    private final Hotspot[] triggers = new Hotspot[MAX_TRIGGERS];
    private final IdentityHashMap<SourceLocation, Hotspot> statements = new IdentityHashMap<>();
    private Program program;
    private int count, dueUpdates, programUpdates, statementAttempts;
    private double totalNs, programTotalNs;
    private long maximumNs, programMaximumNs, skippedStatements, skippedTriggers;
    private boolean collecting = true;

    public ManagerExecutionProfiler(int updateLimit) {
        if (updateLimit < 1 || updateLimit > MAX_UPDATE_LIMIT)
            throw new IllegalArgumentException("Profile update limit must be from 1 to 4096");
        samples = new long[updateLimit];
        gameTicks = new long[updateLimit];
        programSamples = new long[updateLimit];
        Arrays.fill(programSamples, -1);
        due = new boolean[updateLimit];
    }

    public boolean isCollecting() {
        return collecting;
    }

    public void recordUpdate(long elapsedNs, boolean due) {
        recordUpdate(elapsedNs, due, -1);
    }

    /** One due update means at least one trigger began or resumed execution. */
    public void recordUpdate(long elapsedNs, boolean due, long gameTick) {
        if (!collecting) return;
        long elapsed = Math.max(0, elapsedNs);
        gameTicks[count] = gameTick;
        this.due[count] = due;
        samples[count++] = elapsed;
        totalNs += elapsed;
        maximumNs = Math.max(maximumNs, elapsed);
        if (due) dueUpdates++;
        if (count == samples.length) collecting = false;
    }

    public boolean bindProgram(Program current) {
        if (program == null) program = current;
        if (program == current) return true;
        stop();
        return false;
    }

    public boolean matchesProgram(Program current) {
        return program == current;
    }

    public void beginUpdate() {
        statementAttempts = 0;
    }

    public void recordProgram(long elapsedNs) {
        if (!collecting) return;
        long elapsed = Math.max(0, elapsedNs);
        programSamples[count] = elapsed;
        programUpdates++;
        programTotalNs += elapsed;
        programMaximumNs = Math.max(programMaximumNs, elapsed);
    }

    public long beginStatement() {
        if (statementAttempts >= MAX_STATEMENT_ATTEMPTS_PER_UPDATE) {
            skippedStatements++;
            return 0;
        }
        statementAttempts++;
        return System.nanoTime();
    }

    public void recordStatement(SourceLocation at, long start) {
        long elapsed = System.nanoTime() - start;
        Hotspot entry = statements.get(at);
        if (entry == null) {
            if (statements.size() >= MAX_STATEMENTS) {
                skippedStatements++;
                return;
            }
            entry = new Hotspot(0, at);
            statements.put(at, entry);
        }
        entry.record(elapsed, false);
    }

    public void recordTrigger(int index, long start, boolean resumed) {
        if (index >= triggers.length) {
            skippedTriggers++;
            return;
        }
        long elapsed = System.nanoTime() - start;
        if (triggers[index] == null) triggers[index] = new Hotspot(index + 1, null);
        triggers[index].record(elapsed, resumed);
    }

    public Report report(RuntimeSourceMap sourceMap) {
        List<Hotspot> triggerRows = new ArrayList<>(), statementRows = new ArrayList<>();
        for (Hotspot entry : triggers) if (entry != null) triggerRows.add(entry.copy(null));
        for (Hotspot entry : statements.values()) statementRows.add(entry.copy(sourceMap));
        Comparator<Hotspot> hottest = Comparator.comparingLong(Hotspot::totalNs)
            .reversed();
        triggerRows.sort(hottest.thenComparingInt(row -> row.trigger));
        statementRows.sort(
            hottest.thenComparingInt(row -> row.location.getLine())
                .thenComparingInt(row -> row.location.getColumn()));
        return new Report(triggerRows, statementRows, skippedTriggers, skippedStatements);
    }

    void writeSamples(com.google.gson.stream.JsonWriter out) throws java.io.IOException {
        out.beginArray();
        for (int i = 0; i < count; i++) {
            out.beginObject();
            out.name("game_tick")
                .value(gameTicks[i]);
            out.name("update_ns")
                .value(samples[i]);
            if (programSamples[i] >= 0) out.name("program_ns")
                .value(programSamples[i]);
            out.name("trigger_due")
                .value(due[i]);
            out.endObject();
        }
        out.endArray();
    }

    public static final class Hotspot {

        public final int trigger;
        public final SourceLocation location;
        private long attempts, resumes, totalNs, maximumNs;

        private Hotspot(int trigger, SourceLocation location) {
            this.trigger = trigger;
            this.location = location;
        }

        private void record(long elapsed, boolean resumed) {
            attempts++;
            if (resumed) resumes++;
            totalNs += elapsed;
            maximumNs = Math.max(maximumNs, elapsed);
        }

        private Hotspot copy(RuntimeSourceMap sourceMap) {
            Hotspot copy = new Hotspot(trigger, sourceMap == null ? location : sourceMap.map(location));
            copy.attempts = attempts;
            copy.resumes = resumes;
            copy.totalNs = totalNs;
            copy.maximumNs = maximumNs;
            return copy;
        }

        public long attempts() {
            return attempts;
        }

        public long resumes() {
            return resumes;
        }

        public long totalNs() {
            return totalNs;
        }

        public long maximumNs() {
            return maximumNs;
        }
    }

    public static final class Report {

        public final List<Hotspot> triggers, statements;
        public final long skippedTriggers, skippedStatements;

        private Report(List<Hotspot> triggers, List<Hotspot> statements, long skippedTriggers, long skippedStatements) {
            this.triggers = java.util.Collections.unmodifiableList(triggers);
            this.statements = java.util.Collections.unmodifiableList(statements);
            this.skippedTriggers = skippedTriggers;
            this.skippedStatements = skippedStatements;
        }
    }

    public void stop() {
        collecting = false;
    }

    /** Sorting/copying is bounded and happens only on an explicit diagnostic request. */
    public Snapshot snapshot() {
        long[] ordered = Arrays.copyOf(samples, count);
        Arrays.sort(ordered);
        return new Snapshot(
            collecting,
            samples.length,
            count,
            dueUpdates,
            count == 0 ? 0 : totalNs / count / 1_000_000.0,
            percentile(ordered, 50),
            percentile(ordered, 95),
            percentile(ordered, 99),
            maximumNs / 1_000_000.0,
            programUpdates,
            programUpdates == 0 ? 0 : programTotalNs / programUpdates / 1_000_000.0,
            programMaximumNs / 1_000_000.0);
    }

    private static double percentile(long[] ordered, int percent) {
        if (ordered.length == 0) return 0;
        int index = (ordered.length * percent + 99) / 100 - 1;
        return ordered[index] / 1_000_000.0;
    }

    /** Immutable numeric data only; durations are milliseconds, percentiles use nearest rank. */
    public static final class Snapshot {

        public static final Snapshot EMPTY = new Snapshot(false, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        public final boolean collecting;
        public final int updateLimit, count, dueUpdates;
        public final double averageMs, p50Ms, p95Ms, p99Ms, maximumMs;
        public final int programUpdates;
        public final double programAverageMs, programMaximumMs;

        private Snapshot(boolean collecting, int updateLimit, int count, int dueUpdates, double averageMs, double p50Ms,
            double p95Ms, double p99Ms, double maximumMs, int programUpdates, double programAverageMs,
            double programMaximumMs) {
            this.collecting = collecting;
            this.updateLimit = updateLimit;
            this.count = count;
            this.dueUpdates = dueUpdates;
            this.averageMs = averageMs;
            this.p50Ms = p50Ms;
            this.p95Ms = p95Ms;
            this.p99Ms = p99Ms;
            this.maximumMs = maximumMs;
            this.programUpdates = programUpdates;
            this.programAverageMs = programAverageMs;
            this.programMaximumMs = programMaximumMs;
        }
    }
}
