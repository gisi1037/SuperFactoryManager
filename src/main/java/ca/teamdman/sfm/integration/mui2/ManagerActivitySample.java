package ca.teamdman.sfm.integration.mui2;

import java.util.Arrays;
import java.util.Locale;

import io.netty.buffer.ByteBuf;

/** Bounded immutable operational telemetry. Contains no source, AST, inventories, labels or disk capabilities. */
public final class ManagerActivitySample {

    public static final int HISTORY_LIMIT = 20;
    private static final int FORMAT = 1;

    public enum Phase {

        EMPTY("No disk", "Insert a program disk to begin.", 0xff9b9da5),
        LOADING("Loading", "Preparing a program; execution has not started.", 0xffd6b476),
        RUNNING("Running", "Clock advances even if no trigger or transfer runs.", 0xffa8d4bc),
        PAUSED("Paused", "Execution paused; update cost is still sampled.", 0xffb5b7c2),
        RECOVERY("Recovery blocked", "Resolve transfer recovery before resuming.", 0xffe5bb76),
        UNAVAILABLE("Not ready", "Open Logs for details; Start retries loading.", 0xffd99098);

        public final String label, detail;
        public final int color;

        Phase(String label, String detail, int color) {
            this.label = label;
            this.detail = detail;
            this.color = color;
        }
    }

    public static final ManagerActivitySample EMPTY = new ManagerActivitySample(new double[0], 0, 0, Phase.EMPTY, 0);
    public final long measuredUpdates, schedulerTick;
    public final Phase phase;
    public final double latestMs, averageMs, windowMaxMs, lifetimePeakMs;
    private final double[] history;
    private volatile Labels formatted;

    /** History is chronological; unused leading ring cells are excluded using the actual measured update count. */
    public ManagerActivitySample(double[] recent, long measuredUpdates, long schedulerTick, Phase phase,
        double lifetimePeakMs) {
        if (recent == null || measuredUpdates < 0 || phase == null || !validTime(lifetimePeakMs)) throw invalid();
        this.measuredUpdates = measuredUpdates;
        this.schedulerTick = schedulerTick;
        this.phase = phase;
        this.lifetimePeakMs = lifetimePeakMs;
        int count = (int) Math.min(Math.min(recent.length, HISTORY_LIMIT), measuredUpdates);
        history = new double[count];
        double mean = 0, maximum = 0;
        for (int i = 0; i < count; i++) {
            double value = recent[recent.length - count + i];
            if (!validTime(value)) throw invalid();
            history[i] = value;
            mean += (value - mean) / (i + 1);
            maximum = Math.max(maximum, value);
        }
        latestMs = count == 0 ? 0 : history[count - 1];
        averageMs = mean;
        windowMaxMs = maximum;

    }

    /** Memoized only by a visible client readout; the server sync path does no number formatting. */
    public Labels labels() {
        Labels present = formatted;
        if (present == null) formatted = present = new Labels(this);
        return present;
    }

    public static final class Labels {

        public final String latestLabel, averageLabel, windowMaxLabel, lifetimePeakLabel, clockLabel, compactLabel,
            scaleLabel, windowLabel;

        private Labels(ManagerActivitySample sample) {
            int count = sample.count();
            latestLabel = "Latest " + (count == 0 ? "--" : ms(sample.latestMs));
            averageLabel = "Mean " + (count == 0 ? "--" : ms(sample.averageMs));
            windowMaxLabel = "Window max " + (count == 0 ? "--" : ms(sample.windowMaxMs));
            lifetimePeakLabel = "Lifetime peak " + (sample.measuredUpdates == 0 ? "--" : ms(sample.lifetimePeakMs));
            clockLabel = "Scheduler tick #" + sample.schedulerTick;
            compactLabel = sample.phase.label + " | " + latestLabel + " | " + averageLabel;
            scaleLabel = "0 to " + ms(Math.max(.01, sample.windowMaxMs)) + " | oldest to newest";
            windowLabel = "Full manager update cost | last " + count;
        }
    }

    /** Classify only fixed status prefixes; raw status/source text is never retained or transported here. */
    public static Phase phase(boolean hasDisk, boolean requestedRun, boolean compiled, boolean canRun, boolean recovery,
        String status) {
        if (recovery) return Phase.RECOVERY;
        if (!hasDisk) return Phase.EMPTY;
        if (!compiled && status != null
            && (status.startsWith("Loading published program")
                || status.startsWith("Published reference retry requested")))
            return Phase.LOADING;
        if (compiled) return requestedRun ? Phase.RUNNING : Phase.PAUSED;
        if (canRun && status != null && status.startsWith("Published reference awaiting activation"))
            return requestedRun ? Phase.LOADING : Phase.PAUSED;
        return Phase.UNAVAILABLE;
    }

    public int count() {
        return history.length;
    }

    public double sample(int index) {
        return history[index];
    }

    /** A clock marker, explicitly not a statement/transfer progress percentage. */
    public int clockMarker() {
        return (int) Math.floorMod(schedulerTick, (long) HISTORY_LIMIT);
    }

    public void write(ByteBuf buffer) {
        buffer.writeByte(FORMAT);
        buffer.writeByte(phase.ordinal());
        buffer.writeLong(measuredUpdates);
        buffer.writeLong(schedulerTick);
        buffer.writeDouble(lifetimePeakMs);
        buffer.writeByte(history.length);
        for (double value : history) buffer.writeDouble(value);
    }

    public static ManagerActivitySample read(ByteBuf buffer) {
        if (buffer.readUnsignedByte() != FORMAT) throw invalid();
        int phase = buffer.readUnsignedByte();
        long updates = buffer.readLong(), clock = buffer.readLong();
        double peak = buffer.readDouble();
        int count = buffer.readUnsignedByte();
        if (phase >= Phase.values().length || updates < 0 || count > HISTORY_LIMIT || count > updates) throw invalid();
        double[] history = new double[count];
        for (int i = 0; i < count; i++) history[i] = buffer.readDouble();
        return new ManagerActivitySample(history, updates, clock, Phase.values()[phase], peak);
    }

    private static boolean validTime(double value) {
        return value >= 0 && !Double.isNaN(value) && !Double.isInfinite(value);
    }

    private static String ms(double value) {
        return String.format(Locale.ROOT, value >= 10000 ? "%.3g ms" : "%.3f ms", value);
    }

    private static IllegalArgumentException invalid() {
        return new IllegalArgumentException("Invalid manager activity sample");
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ManagerActivitySample)) return false;
        ManagerActivitySample sample = (ManagerActivitySample) other;
        return measuredUpdates == sample.measuredUpdates && schedulerTick == sample.schedulerTick
            && phase == sample.phase
            && Double.doubleToLongBits(lifetimePeakMs) == Double.doubleToLongBits(sample.lifetimePeakMs)
            && Arrays.equals(history, sample.history);
    }

    @Override
    public int hashCode() {
        int hash = Long.hashCode(measuredUpdates);
        hash = 31 * hash + Long.hashCode(schedulerTick);
        hash = 31 * hash + phase.hashCode();
        hash = 31 * hash + Double.hashCode(lifetimePeakMs);
        return 31 * hash + Arrays.hashCode(history);
    }
}
