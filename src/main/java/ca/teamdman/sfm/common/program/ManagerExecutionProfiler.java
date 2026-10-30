package ca.teamdman.sfm.common.program;

import java.util.Arrays;

/** Opt-in, server-thread sample window. Full-update samples include idle/recovery work, never source/AST data. */
public final class ManagerExecutionProfiler {

    public static final int DEFAULT_UPDATE_LIMIT = 1200;
    public static final int MAX_UPDATE_LIMIT = 4096;
    private final long[] samples;
    private int count, dueUpdates;
    private double totalNs;
    private long maximumNs;
    private boolean collecting = true;

    public ManagerExecutionProfiler(int updateLimit) {
        if (updateLimit < 1 || updateLimit > MAX_UPDATE_LIMIT)
            throw new IllegalArgumentException("Profile update limit must be from 1 to 4096");
        samples = new long[updateLimit];
    }

    public boolean isCollecting() {
        return collecting;
    }

    /** No allocation or sorting during collection. One due update means at least one trigger began execution. */
    public void recordUpdate(long elapsedNs, boolean due) {
        if (!collecting) return;
        long elapsed = Math.max(0, elapsedNs);
        samples[count++] = elapsed;
        totalNs += elapsed;
        maximumNs = Math.max(maximumNs, elapsed);
        if (due) dueUpdates++;
        if (count == samples.length) collecting = false;
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
            maximumNs / 1_000_000.0);
    }

    private static double percentile(long[] ordered, int percent) {
        if (ordered.length == 0) return 0;
        int index = (ordered.length * percent + 99) / 100 - 1;
        return ordered[index] / 1_000_000.0;
    }

    /** Immutable numeric data only; durations are milliseconds, percentiles use nearest rank. */
    public static final class Snapshot {

        public static final Snapshot EMPTY = new Snapshot(false, 0, 0, 0, 0, 0, 0, 0, 0);
        public final boolean collecting;
        public final int updateLimit, count, dueUpdates;
        public final double averageMs, p50Ms, p95Ms, p99Ms, maximumMs;

        private Snapshot(boolean collecting, int updateLimit, int count, int dueUpdates, double averageMs, double p50Ms,
            double p95Ms, double p99Ms, double maximumMs) {
            this.collecting = collecting;
            this.updateLimit = updateLimit;
            this.count = count;
            this.dueUpdates = dueUpdates;
            this.averageMs = averageMs;
            this.p50Ms = p50Ms;
            this.p95Ms = p95Ms;
            this.p99Ms = p99Ms;
            this.maximumMs = maximumMs;
        }
    }
}
