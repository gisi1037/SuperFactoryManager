package ca.teamdman.sfm.common.diagnostics;

import java.util.Arrays;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/** Server-thread-only, opt-in totals for complete manager updates across all dimensions. */
public final class ManagerTickProfiler {

    public static final ManagerTickProfiler INSTANCE = new ManagerTickProfiler();
    private static final int CAPACITY = 600;
    private final long[] times = new long[CAPACITY];
    private final long[] updates = new long[CAPACITY];
    private boolean enabled;
    private boolean collecting;
    private int next;
    private int count;
    private long tickNanos;
    private long tickUpdates;

    public void setEnabled(boolean enabled) {
        // A fresh capture starts at the next START event; stopping retains completed samples.
        if (enabled) reset();
        this.enabled = enabled;
        collecting = false;
    }

    public void reset() {
        enabled = false;
        collecting = false;
        next = 0;
        count = 0;
        tickNanos = 0;
        tickUpdates = 0;
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (!enabled) return;
        if (event.phase == TickEvent.Phase.START) {
            tickNanos = 0;
            tickUpdates = 0;
            collecting = true;
        } else if (collecting) {
            times[next] = tickNanos;
            updates[next] = tickUpdates;
            next = (next + 1) % CAPACITY;
            count = Math.min(count + 1, CAPACITY);
            collecting = false;
        }
    }

    public void recordUpdate(long nanos) {
        if (!collecting) return;
        tickNanos += Math.max(0, nanos);
        tickUpdates++;
    }

    /** Snapshot work and sorting happen only on request, never inside a manager update. */
    public Snapshot snapshot() {
        long[] sorted = Arrays.copyOf(times, count);
        long total = 0;
        long samples = 0;
        for (int i = 0; i < count; i++) {
            total += times[i];
            samples += updates[i];
        }
        Arrays.sort(sorted);
        return new Snapshot(enabled, sorted, total, samples);
    }

    public static final class Snapshot {

        public final boolean enabled;
        public final int ticks;
        public final long updates;
        public final long totalNanos;
        public final long p50Nanos;
        public final long p95Nanos;
        public final long p99Nanos;
        public final long maxNanos;

        private Snapshot(boolean enabled, long[] sorted, long total, long updates) {
            this.enabled = enabled;
            ticks = sorted.length;
            this.updates = updates;
            totalNanos = total;
            p50Nanos = percentile(sorted, 50);
            p95Nanos = percentile(sorted, 95);
            p99Nanos = percentile(sorted, 99);
            maxNanos = percentile(sorted, 100);
        }

        private static long percentile(long[] sorted, int percentile) {
            return sorted.length == 0 ? 0 : sorted[(sorted.length * percentile + 99) / 100 - 1];
        }
    }
}
