package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.integration.mui2.ManagerActivitySample;
import ca.teamdman.sfm.integration.mui2.ManagerActivitySample.Phase;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

class ManagerActivitySampleTest {

    @Test
    void warmupExcludesUnusedRingCellsAndKeepsTheChronologicalMeasuredWindow() {
        double[] ring = new double[20];
        ring[17] = 1;
        ring[18] = 2;
        ring[19] = 6;
        ManagerActivitySample sample = new ManagerActivitySample(ring, 3, 24, Phase.RUNNING, 8);
        assertEquals(3, sample.count());
        assertEquals(1, sample.sample(0));
        assertEquals(6, sample.sample(2));
        assertEquals(6, sample.latestMs);
        assertEquals(3, sample.averageMs);
        assertEquals(6, sample.windowMaxMs);
        assertEquals(8, sample.lifetimePeakMs);
        assertTrue(sample.labels().windowLabel.endsWith("last 3"));
        assertSame(sample.labels(), sample.labels(), "Unchanged frames reuse their formatted readouts");
        assertEquals(4, sample.clockMarker());
        assertEquals("Scheduler tick #24", sample.labels().clockLabel);
    }

    @Test
    void historyRetentionIsBoundedAndCallerMutationCannotRewriteTheSyncedSample() {
        double[] longHistory = new double[1000];
        Arrays.fill(longHistory, -1); // Older discarded values must not be scanned or retained.
        for (int i = 980; i < 1000; i++) longHistory[i] = i - 980;
        ManagerActivitySample sample = new ManagerActivitySample(longHistory, 1000, 9, Phase.PAUSED, 19);
        assertEquals(ManagerActivitySample.HISTORY_LIMIT, sample.count());
        assertEquals(0, sample.sample(0));
        assertEquals(19, sample.latestMs);
        assertEquals(9.5, sample.averageMs);
        longHistory[999] = 999;
        assertEquals(19, sample.latestMs);
        assertEquals(19, sample.sample(19));
    }

    @Test
    void emptyAndZeroCostMeasurementsRemainDifferentAndHaveNoInventedThroughput() {
        ManagerActivitySample empty = ManagerActivitySample.EMPTY;
        assertEquals(0, empty.count());
        assertEquals("Latest --", empty.labels().latestLabel);
        assertEquals("Mean --", empty.labels().averageLabel);
        assertEquals("Lifetime peak --", empty.labels().lifetimePeakLabel);
        ManagerActivitySample zero = new ManagerActivitySample(new double[20], 1, 0, Phase.EMPTY, 0);
        assertEquals(1, zero.count());
        assertEquals("Latest 0.000 ms", zero.labels().latestLabel);
        assertFalse(zero.labels().compactLabel.contains("items"));
        assertFalse(zero.labels().compactLabel.contains("transfers"));
    }

    @Test
    void timingValidationRejectsNonfiniteOrNegativeValuesAndMeanDoesNotOverflow() {
        for (double invalid : new double[] { -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY }) {
            assertThrows(
                IllegalArgumentException.class,
                () -> new ManagerActivitySample(new double[] { invalid }, 1, 0, Phase.RUNNING, 0));
            assertThrows(
                IllegalArgumentException.class,
                () -> new ManagerActivitySample(new double[] { 0 }, 1, 0, Phase.RUNNING, invalid));
        }
        ManagerActivitySample large = new ManagerActivitySample(
            new double[] { Double.MAX_VALUE, Double.MAX_VALUE },
            2,
            0,
            Phase.RUNNING,
            Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, large.averageMs);
        assertFalse(Double.isInfinite(large.averageMs));
        assertTrue(
            large.labels().latestLabel.length() < 30,
            "Exceptional values should use a bounded scientific readout");
    }

    @Test
    void activitySeparatesRequestedRunningFromLoadingAndRecoveryAndMissingExecutable() {
        assertEquals(Phase.EMPTY, phase(false, true, true, true, false, "Running"));
        assertEquals(Phase.RECOVERY, phase(true, true, true, true, true, "Running"));
        assertEquals(Phase.RUNNING, phase(true, true, true, true, false, "Running published program"));
        assertEquals(Phase.PAUSED, phase(true, false, true, true, false, "Published program ready; paused"));
        assertEquals(Phase.LOADING, phase(true, true, false, true, false, "Loading published program"));
        assertEquals(Phase.LOADING, phase(true, false, false, true, false, "Loading published program"));
        assertEquals(Phase.LOADING, phase(true, true, false, true, false, "Published reference retry requested"));
        assertEquals(Phase.PAUSED, phase(true, false, false, true, false, "Published reference awaiting activation"));
        assertEquals(
            Phase.UNAVAILABLE,
            phase(true, false, false, true, false, "Published program unavailable; use Run to retry"));
        assertEquals(Phase.UNAVAILABLE, phase(true, true, false, false, false, null));
    }

    @Test
    void scalarClassificationNeverRetainsOrRendersPrivateSourceStatusOrGrantTokens() {
        String secret = "PRIVATE_SOURCE_AND_GRANT_aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        Phase phase = phase(true, false, false, true, false, "Published program unavailable " + secret);
        ManagerActivitySample sample = new ManagerActivitySample(new double[] { .01 }, 1, 8, phase, .01);
        for (String text : new String[] { sample.labels().compactLabel, sample.labels().clockLabel, sample.phase.detail,
            sample.labels().latestLabel, sample.labels().averageLabel, sample.labels().windowLabel,
            sample.labels().scaleLabel }) assertFalse(text.contains(secret));
        assertEquals(Phase.UNAVAILABLE, sample.phase);
    }

    @Test
    void clockMarkersRemainBoundedAcrossResetsAndSignedLongLimits() {
        for (long clock : new long[] { Long.MIN_VALUE, -21, -1, 0, 19, 20, Long.MAX_VALUE }) {
            ManagerActivitySample sample = new ManagerActivitySample(new double[0], 0, clock, Phase.PAUSED, 0);
            assertTrue(sample.clockMarker() >= 0);
            assertTrue(sample.clockMarker() < ManagerActivitySample.HISTORY_LIMIT);
            assertEquals(clock, sample.schedulerTick);
        }
    }

    @Test
    void immutableSampleCodecRoundTripsEveryPhaseWithinSmallFixedEnvelope() {
        double[] values = new double[ManagerActivitySample.HISTORY_LIMIT];
        for (int i = 0; i < values.length; i++) values[i] = i / 1000.0;
        for (Phase phase : Phase.values()) {
            ManagerActivitySample sample = new ManagerActivitySample(values, Long.MAX_VALUE, Long.MIN_VALUE, phase, 5);
            byte[] encoded = encode(sample);
            assertTrue(encoded.length <= 187);
            ManagerActivitySample decoded = decode(encoded);
            assertEquals(sample, decoded);
            assertEquals(sample.hashCode(), decoded.hashCode());
            assertEquals(sample.labels().averageLabel, decoded.labels().averageLabel);
            assertEquals(sample.labels().clockLabel, decoded.labels().clockLabel);
        }
        assertEquals(ManagerActivitySample.EMPTY, decode(encode(ManagerActivitySample.EMPTY)));
    }

    @Test
    void codecRejectsUnknownFormatPhaseOversizedWindowImpossibleCountAndInvalidTiming() {
        byte[] valid = encode(new ManagerActivitySample(new double[] { 1 }, 1, 7, Phase.RUNNING, 2));
        byte[] version = valid.clone(), phase = valid.clone(), count = valid.clone(), updates = valid.clone(),
            peak = valid.clone(), timing = valid.clone();
        version[0] = 2;
        phase[1] = (byte) 255;
        count[26] = 21;
        Arrays.fill(updates, 2, 10, (byte) 0xff);
        java.nio.ByteBuffer.wrap(peak)
            .putDouble(18, Double.NaN);
        java.nio.ByteBuffer.wrap(timing)
            .putDouble(27, -1);
        for (byte[] malformed : new byte[][] { version, phase, count, updates, peak, timing })
            assertThrows(IllegalArgumentException.class, () -> decode(malformed));
        for (int length = 0; length < valid.length; length++) {
            byte[] truncated = Arrays.copyOf(valid, length);
            assertThrows(RuntimeException.class, () -> decode(truncated));
        }
    }

    private static Phase phase(boolean disk, boolean run, boolean compiled, boolean canRun, boolean recovery,
        String status) {
        return ManagerActivitySample.phase(disk, run, compiled, canRun, recovery, status);
    }

    private static byte[] encode(ManagerActivitySample sample) {
        ByteBuf buffer = Unpooled.buffer();
        try {
            sample.write(buffer);
            byte[] bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            return bytes;
        } finally {
            buffer.release();
        }
    }

    private static ManagerActivitySample decode(byte[] bytes) {
        ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
        try {
            ManagerActivitySample sample = ManagerActivitySample.read(buffer);
            assertFalse(buffer.isReadable());
            return sample;
        } finally {
            buffer.release();
        }
    }
}
