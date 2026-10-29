package ca.teamdman.sfm.audit;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter;

/** Optional diagnostic main: pass a separately compiled original highlighter class to compare both arms. */
public final class SfmlHighlighterCharacterization {

    private static final int WARMUP = 4, SAMPLES = 12;

    private SfmlHighlighterCharacterization() {}

    public static void main(String[] args) throws Exception {
        Method baseline = args.length == 0 ? null
            : Class.forName(args[0])
                .getMethod("highlight", String.class);
        System.out.printf(
            Locale.ROOT,
            "HIGH_ENV java=%s vm=%s os=%s arch=%s processors=%d heap=%d warmup=%d samples=%d baseline=%s%n",
            System.getProperty("java.version"),
            System.getProperty("java.vm.name"),
            System.getProperty("os.name"),
            System.getProperty("os.arch"),
            Runtime.getRuntime()
                .availableProcessors(),
            Runtime.getRuntime()
                .maxMemory(),
            WARMUP,
            SAMPLES,
            args.length == 0 ? "none" : args[0]);
        if (baseline != null) verifyCorpus(baseline);
        // The largest ASCII source is 29,975 bytes, within the 30,000-byte editor source limit.
        for (int count : new int[] { 1000, 2000, 4000, 8000, 14980 }) {
            String source = "LET $value = " + chain(count) + "(1)";
            String expected = signature(SfmlHighlighter.highlight(source));
            System.out.printf(
                Locale.ROOT,
                "HIGH_FIXTURE components=%d bytes=%d sourceSha256=%s spansSha256=%s%n",
                count,
                source.getBytes(StandardCharsets.UTF_8).length,
                sha256(source),
                sha256(expected));
            long[][] times = new long[baseline == null ? 1 : 2][SAMPLES];
            for (int sample = -WARMUP; sample < SAMPLES; sample++) {
                for (int turn = 0; turn < times.length; turn++) {
                    int arm = times.length == 1 ? 0 : (sample + WARMUP + turn) % 2;
                    long start = System.nanoTime();
                    Object highlighted = arm == 0 ? SfmlHighlighter.highlight(source) : baseline.invoke(null, source);
                    long nanos = System.nanoTime() - start;
                    if (!expected.equals(signature(highlighted))) {
                        throw new AssertionError(
                            "Full line/span/category mismatch: components=" + count + " arm=" + arm);
                    }
                    if (sample >= 0) {
                        times[arm][sample] = nanos;
                        System.out.printf(
                            Locale.ROOT,
                            "HIGH_SAMPLE components=%d arm=%s sample=%d nanos=%d%n",
                            count,
                            arm == 0 ? "optimized" : "baseline",
                            sample,
                            nanos);
                    }
                }
            }
            for (int arm = 0; arm < times.length; arm++) {
                Arrays.sort(times[arm]);
                System.out.printf(
                    Locale.ROOT,
                    "HIGH_RESULT components=%d arm=%s medianNs=%d p95Ns=%d p99Ns=%d maxNs=%d%n",
                    count,
                    arm == 0 ? "optimized" : "baseline",
                    percentile(times[arm], 0.5),
                    percentile(times[arm], 0.95),
                    percentile(times[arm], 0.99),
                    times[arm][times[arm].length - 1]);
            }
        }
    }

    static String chain(int count) {
        StringBuilder source = new StringBuilder(count * 2);
        for (int i = 0; i < count; i++) {
            if (i > 0) source.append('.');
            source.append('a');
        }
        return source.toString();
    }

    private static void verifyCorpus(Method baseline) throws Exception {
        String[] cases = { "", "a.b()", "a.b", ".a.b()", "$x.a.b()", "a..b()", "a.b.()", "a.FUNCTION.b()",
            "a.RISING.FALLING()", "mod:a.b()", "FUNCTION a.b($x: INTEGER) RETURNS INTEGER DO RETURN $x END",
            "a -- hidden\n. -- hidden\nb -- hidden\n()", "a. -- hidden\n.b()", "a( b.c() ).d.e()",
            "EVERY REDSTONE RISING FROM sensor DO END\nFOR $i = 1 TO 10 STEP 1 DO END",
            "NAME \"\uD83D\uDE00 a.b()\" -- a.b()\nLET $x = \uD83D\uDE00 a.b()" };
        for (String source : cases) verify(baseline, source);
        Random random = new Random(1037);
        String[] fragments = { "a", "b", "RISING", "FALLING", ".", "(", ")", "FUNCTION", "INTEGER", "FROM", "$x", ":",
            " ", "\n", "-- a.b()\n", "\"a.b()\"", "\uD83D\uDE00" };
        for (int sample = 0; sample < 300; sample++) {
            StringBuilder source = new StringBuilder();
            for (int i = 0; i < 80; i++) source.append(fragments[random.nextInt(fragments.length)])
                .append(' ');
            verify(baseline, source.toString());
        }
        System.out.println("HIGH_EQUIVALENCE fullSpans=316 seed=1037");
    }

    private static void verify(Method baseline, String source) throws Exception {
        if (!signature(SfmlHighlighter.highlight(source)).equals(signature(baseline.invoke(null, source)))) {
            throw new AssertionError("Full line/span/category mismatch in source: " + source);
        }
    }

    private static long percentile(long[] sorted, double quantile) {
        return sorted[Math.max(0, (int) Math.ceil(sorted.length * quantile) - 1)];
    }

    private static String signature(Object highlighted) throws Exception {
        StringBuilder out = new StringBuilder();
        for (Object line : (List<?>) highlighted.getClass()
            .getField("lines")
            .get(highlighted)) {
            String text = (String) line.getClass()
                .getField("text")
                .get(line);
            out.append(text.length())
                .append(':')
                .append(text)
                .append('\n');
            for (Object span : (List<?>) line.getClass()
                .getField("spans")
                .get(line)) {
                out.append(
                    span.getClass()
                        .getField("start")
                        .getInt(span))
                    .append(',');
                out.append(
                    span.getClass()
                        .getField("end")
                        .getInt(span))
                    .append(',');
                out.append(
                    span.getClass()
                        .getField("style")
                        .get(span))
                    .append(';');
            }
            out.append('\n');
        }
        return out.toString();
    }

    private static String sha256(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder out = new StringBuilder();
        for (byte element : digest) out.append(String.format(Locale.ROOT, "%02x", element & 255));
        return out.toString();
    }
}
