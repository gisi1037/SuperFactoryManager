package ca.teamdman.sfm.common.diagnostics;

import java.util.Arrays;

/** Opt-in server-thread phase totals. Scan time is INCLUDED in compatibility/program time when nested. */
public final class ManagerPhaseProfiler {

    // Constant for this JVM: the JIT can remove disabled phase instrumentation from ordinary updates.
    public static final boolean ENABLED = Boolean.getBoolean("sfm.phaseProfile");
    private static final long[] COUNTERS = new long[8];

    private ManagerPhaseProfiler() {}

    public static long begin() {
        return ENABLED ? System.nanoTime() : 0;
    }

    public static void scan(long start, int cables, int visited) {
        if (!ENABLED) return;
        COUNTERS[0] += System.nanoTime() - start;
        COUNTERS[1]++;
        COUNTERS[2] += cables;
        COUNTERS[3] += visited;
    }

    public static void compatibility(long start) {
        if (ENABLED) COUNTERS[4] += System.nanoTime() - start;
    }

    public static void program(long start) {
        if (ENABLED) COUNTERS[5] += System.nanoTime() - start;
    }

    public static void update(long elapsed) {
        if (!ENABLED) return;
        COUNTERS[6] += elapsed;
        COUNTERS[7]++;
    }

    /** Called at the benchmark tick boundary, never from another thread. No world references retained. */
    public static void reset() {
        Arrays.fill(COUNTERS, 0);
    }

    public static long[] snapshot() {
        return COUNTERS.clone();
    }
}
