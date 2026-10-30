package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.Writer;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.minecraft.nbt.NBTBase;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sun.management.ThreadMXBean;

import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.program.ProgramStateStore;
import ca.teamdman.sfm.common.tileentity.LogLevel;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.tileentity.TileEntityRedstonePort;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

/** Public synthetic programs and fake peripherals; timings are diagnostic evidence, never pass/fail budgets. */
@EnabledIfSystemProperty(named = "sfm.auditBenchmark", matches = "true")
class ProgramDiversityCharacterizationTest {

    private static final long SEED = Long.parseLong(System.getProperty("sfm.characterizationSeed", "1037"));
    private static final int COMPILE_WARMUP = 4, RUNTIME_WARMUP = 16, COHORT = 16;
    private static final int COMPILE_SAMPLES = setting("SFM_PROGRAM_BENCHMARK_COMPILE_SAMPLES", 24, 8, 128);
    private static final int RUNTIME_SAMPLES = setting("SFM_PROGRAM_BENCHMARK_RUNTIME_SAMPLES", 64, 16, 256);
    private static final AllocationMeter ALLOCATION = new AllocationMeter();

    @Test
    void characterizeFreshCompilationAndAlreadyCompiledManagerExecution() throws Exception {
        JsonObject report = new JsonObject();
        report.addProperty("format", 1);
        report.addProperty("benchmark", "sfml-program-diversity");
        report.addProperty("seed", SEED);
        report.addProperty("label", label());
        report.addProperty("javaVersion", System.getProperty("java.version"));
        report.addProperty("vm", System.getProperty("java.vm.name"));
        report.addProperty("os", System.getProperty("os.name"));
        report.addProperty(
            "availableProcessors",
            Runtime.getRuntime()
                .availableProcessors());
        report.addProperty(
            "maxHeapBytes",
            Runtime.getRuntime()
                .maxMemory());
        report.addProperty("allocationSupported", ALLOCATION.bean != null);
        report.addProperty("compileWarmup", COMPILE_WARMUP);
        report.addProperty("compileSamples", COMPILE_SAMPLES);
        report.addProperty("runtimeWarmupFrames", RUNTIME_WARMUP);
        report.addProperty("runtimeSamples", RUNTIME_SAMPLES);
        report.addProperty("cohortSize", COHORT);
        report.addProperty(
            "conditions",
            "Fresh ProgramBuilder per compilation; generated DFA/JIT caches may already "
                + "be warm. Runtime measures full manager.updateEntity on private compiled legacy disks, including fresh "
                + "ProgramContext construction and ordinary small-network validation/discovery. Compilation, disk "
                + "activation, fixture construction, assertions, world-clock advancement and JSON output excluded from "
                + "timed runtime segments. No storage IO, shared Program, forced GC or cold-reference work. Logging OFF. "
                + "Synthetic loaded fake world and native redstone-port adapter; no pack hardware/20 TPS guarantee. "
                + "Each state/result checked every frame. Existing PerformanceCharacterizationTest separately covers "
                + "large active/blocked item transfers and topology scaling. GC phase deltas include untimed assertions.");
        Path output = output();
        JsonArray cases = new JsonArray();
        report.add("cases", cases);
        List<Workload> workloads = workloads();
        Collections.shuffle(workloads, new Random(SEED));
        try {
            for (Workload workload : workloads) {
                JsonObject result = new JsonObject();
                result.addProperty("name", workload.name);
                result.addProperty("publicFixtureSource", workload.source());
                result.addProperty(
                    "sourceUtf8Bytes",
                    workload.source()
                        .getBytes(StandardCharsets.UTF_8).length);
                result.addProperty("cadenceTicks", workload.cadence);
                result.add("compile", compile(workload));
                result.add("warmManagerRuntime", runtime(workload));
                cases.add(result);
                System.out.println(
                    "PROGRAM_CHAR_CASE " + workload.name
                        + " compile="
                        + COMPILE_SAMPLES
                        + " completedManagerUpdates="
                        + (COHORT * RUNTIME_SAMPLES));
            }
            report.addProperty("completedSuccessfully", true);
        } catch (Throwable error) {
            report.addProperty("completedSuccessfully", false);
            report.addProperty(
                "failureType",
                error.getClass()
                    .getSimpleName());
            throw error;
        } finally {
            try (Writer writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
                new GsonBuilder().setPrettyPrinting()
                    .create()
                    .toJson(report, writer);
            }
            System.out.println("PROGRAM_CHAR_JSON " + output.toAbsolutePath());
        }
    }

    private static JsonObject compile(Workload workload) {
        String source = workload.source();
        Program previous = null;
        for (int i = 0; i < COMPILE_WARMUP; i++) previous = build(source);
        Metrics metrics = new Metrics(COMPILE_SAMPLES);
        GcSnapshot gc = GcSnapshot.capture();
        for (int sample = 0; sample < COMPILE_SAMPLES; sample++) {
            long allocation = ALLOCATION.bytes(), start = System.nanoTime();
            ProgramBuildResult result = new ProgramBuilder(source).build();
            long elapsed = System.nanoTime() - start, allocated = delta(ALLOCATION.bytes(), allocation);
            metrics.record(sample, elapsed, allocated);
            assertTrue(result.isSuccess(), result.getErrorMessage());
            Program program = result.getProgram();
            assertNotSame(previous, program, "Fresh builds reused a mutable execution Program");
            assertEquals(
                2,
                program.getStateSchema()
                    .size());
            assertEquals(workload.name, program.getName());
            assertEquals(
                workload.name.equals("functions") ? 2 : 0,
                program.getFunctions()
                    .size());
            previous = program;
        }
        JsonObject result = metrics.json();
        result.addProperty("attempted", COMPILE_SAMPLES);
        result.addProperty("completed", COMPILE_SAMPLES);
        result.addProperty("failed", 0);
        result.addProperty("compilationsPerMeasuredSecond", COMPILE_SAMPLES * 1e9 / Math.max(1, metrics.totalNanos()));
        result.add("gc", gc.delta(GcSnapshot.capture()));
        return result;
    }

    private static Program build(String source) {
        ProgramBuildResult result = new ProgramBuilder(source).build();
        assertTrue(result.isSuccess(), result.getErrorMessage());
        return result.getProgram();
    }

    private static JsonObject runtime(Workload workload) throws Exception {
        List<Rig> rigs = new ArrayList<>();
        Set<Program> plans = Collections.newSetFromMap(new IdentityHashMap<Program, Boolean>());
        Set<ProgramStateStore> states = Collections.newSetFromMap(new IdentityHashMap<ProgramStateStore, Boolean>());
        for (int i = 0; i < COHORT; i++) {
            Rig rig = new Rig(workload);
            assertTrue(plans.add(rig.program), "Private managers shared a mutable Program");
            assertTrue(states.add(rig.state), "Private managers shared mutable STATE");
            rigs.add(rig);
        }
        for (int frame = 1; frame <= RUNTIME_WARMUP; frame++) executeFrame(rigs, workload, frame, null, -1);
        Metrics metrics = new Metrics(RUNTIME_SAMPLES);
        GcSnapshot gc = GcSnapshot.capture();
        long dueBefore = rigs.get(0)
            .expectedRuns(RUNTIME_WARMUP);
        for (int sample = 0; sample < RUNTIME_SAMPLES; sample++)
            executeFrame(rigs, workload, RUNTIME_WARMUP + sample + 1, metrics, sample);
        long dueAfter = rigs.get(0)
            .expectedRuns(RUNTIME_WARMUP + RUNTIME_SAMPLES);
        JsonObject result = metrics.json();
        result.addProperty("attemptedManagerUpdates", (long) COHORT * RUNTIME_SAMPLES);
        result.addProperty("completedManagerUpdates", (long) COHORT * RUNTIME_SAMPLES);
        result.addProperty("completedSchedulerTicks", (long) COHORT * RUNTIME_SAMPLES);
        result.addProperty("completedDueTriggerCycles", COHORT * (dueAfter - dueBefore));
        result.addProperty("intentionalNotDueUpdates", COHORT * (RUNTIME_SAMPLES - (dueAfter - dueBefore)));
        result.addProperty("distinctPrivatePrograms", plans.size());
        result.addProperty("distinctPrivateStateStores", states.size());
        result.addProperty(
            "updatesPerMeasuredSecond",
            COHORT * (double) RUNTIME_SAMPLES * 1e9 / Math.max(1, metrics.totalNanos()));
        result.addProperty("stateAndResultsCorrect", true);
        result.addProperty("noWarmStateSerialization", true);
        result.add("gc", gc.delta(GcSnapshot.capture()));
        return result;
    }

    private static void executeFrame(List<Rig> rigs, Workload workload, int frame, Metrics metrics, int sample) {
        for (Rig rig : rigs) rig.fixture.world.time++;
        long allocation = ALLOCATION.bytes(), start = System.nanoTime();
        for (Rig rig : rigs) rig.manager.updateEntity();
        long elapsed = System.nanoTime() - start, allocated = delta(ALLOCATION.bytes(), allocation);
        if (metrics != null) metrics.record(sample, elapsed, allocated);
        for (Rig rig : rigs) rig.verify(workload, frame);
    }

    private static final class Rig {

        final Fixture fixture = new Fixture();
        final RecordingPort port = new RecordingPort();
        final TileEntityManager manager;
        final Program program;
        final ProgramStateStore state;
        final NBTBase committedState;
        final long initialTick;
        final int cadence;

        Rig(Workload workload) throws Exception {
            cadence = workload.cadence;
            fixture.stock(0, 31);
            fixture.stock(1, 17);
            fixture.world.tiles.put(EXTRA, port);
            fixture.labels.addPosition("signal", EXTRA);
            manager = RecoveryFixtures.manager(fixture);
            manager.setLogLevel(LogLevel.OFF);
            ItemDisk.setProgramString(manager.getDisk(), workload.source());
            LabelPositionHolder.saveTo(manager.getDisk(), fixture.labels);
            manager.setRunning(true);
            manager.updateEntity(); // Real source compile, initial STATE activation and discovery, all excluded.
            program = manager.getCompiledProgram();
            state = manager.getProgramState();
            assertNotNull(program, manager.getStatusMessage());
            assertNotNull(state, manager.getStatusMessage());
            assertTrue(manager.isRunning(), manager.getStatusMessage());
            initialTick = manager.getManagerTick();
            committedState = manager.getDisk()
                .getTagCompound()
                .getTag(ProgramStateStore.NBT_KEY);
            verify(workload, 0);
        }

        long expectedRuns(int frames) {
            return (initialTick + frames) / cadence;
        }

        void verify(Workload workload, int frames) {
            long runs = expectedRuns(frames);
            assertEquals(initialTick + frames, manager.getManagerTick());
            assertTrue(manager.isRunning(), manager.getStatusMessage());
            assertSame(program, manager.getCompiledProgram(), "Warm update replaced the installed Program");
            assertSame(state, manager.getProgramState());
            assertEquals(
                runs,
                state.read(0)
                    .asLong(),
                "A due trigger cycle was skipped");
            assertEquals(
                workload.expectedValue(runs),
                state.read(1)
                    .asLong(),
                workload.name);
            assertSame(
                committedState,
                manager.getDisk()
                    .getTagCompound()
                    .getTag(ProgramStateStore.NBT_KEY),
                "Warm STATE was encoded or replaced");
            assertEquals(48, count(fixture.source), "Observations mutated source stock");
            assertEquals(0, count(fixture.dest));
            assertSame(
                ITEM,
                fixture.source.getStackInSlot(0)
                    .getItem());
            assertSame(
                ITEM,
                fixture.source.getStackInSlot(1)
                    .getItem());
            if (workload.name.equals("redstone")) {
                assertEquals(runs, port.writes, "Each due native output must execute");
                assertEquals(runs == 0 ? 0 : 7, port.outputs[ForgeDirection.EAST.ordinal()]);
                for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
                    if (side != ForgeDirection.EAST) assertEquals(0, port.outputs[side.ordinal()]);
                assertEquals(runs, port.refreshes, "Native input must be freshly observed each cycle");
            } else assertEquals(0, port.writes);
        }
    }

    private static final class RecordingPort extends TileEntityRedstonePort {

        final int[] outputs = new int[6];
        long writes, refreshes;

        @Override
        public void refreshInputs() {
            refreshes++;
        }

        @Override
        public void refreshInputs(Runnable beforeProviderCall) {
            beforeProviderCall.run();
            refreshInputs();
        }

        @Override
        public int getInputLevel(int side) {
            return side == ForgeDirection.NORTH.ordinal() ? 7 : 0;
        }

        @Override
        public boolean isInputAvailable(int side) {
            return true;
        }

        @Override
        public boolean setOutput(List<ForgeDirection> sides, int level) {
            writes++;
            boolean changed = false;
            for (ForgeDirection side : sides) {
                changed |= outputs[side.ordinal()] != level;
                outputs[side.ordinal()] = level;
            }
            return changed; // Fake port records native writes; actual hardware/neighbor notifications are out of scope.
        }
    }

    private static final class Workload {

        final String name, functions, body;
        final int cadence;
        final long valuePerCycle;

        Workload(String name, int cadence, String functions, String body, long valuePerCycle) {
            this.name = name;
            this.cadence = cadence;
            this.functions = functions;
            this.body = body;
            this.valuePerCycle = valuePerCycle;
        }

        String source() {
            return "NAME \"" + name
                + "\" STATE $runs = 0 STATE $value = 0 "
                + functions
                + " EVERY "
                + cadence
                + " TICKS DO SET $runs = $runs + 1 "
                + body
                + " END";
        }

        long expectedValue(long runs) {
            if (!name.equals("arithmetic_branches")) return runs * valuePerCycle;
            long result = 0;
            for (long run = 1; run <= runs; run++) {
                long n = run % 8 + 1, mixed = (n * 9 + 11) / 2;
                result += n > 3 ? mixed : -n;
            }
            return result;
        }
    }

    private static List<Workload> workloads() {
        List<Workload> result = new ArrayList<>();
        result.add(new Workload("counter", 1, "", "SET $value = $value + 1", 1));
        result.add(
            new Workload(
                "arithmetic_branches",
                1,
                "",
                "LET $n = $runs % 8 + 1 LET $mixed = ($n * 9 + 11) / 2 "
                    + "IF ($n > 3) THEN SET $value = $value + $mixed ELSE SET $value = $value - $n END",
                0));
        result.add(
            new Workload(
                "bounded_loops",
                1,
                "",
                "LET $sum = 0 FOR $i = 1 TO 16 DO SET $sum = $sum + $i END "
                    + "LET $j = 0 WHILE ($j < 8) DO SET $sum = $sum + $j SET $j = $j + 1 END "
                    + "SET $value = $value + $sum",
                164));
        result.add(
            new Workload(
                "arrays_structs",
                1,
                "",
                "LET $rows = [{n: 1}, {n: 2}, {n: 3}, {n: 4}] LET $copy = $rows SET $copy[1].n = 9 "
                    + "LET $sum = 0 FOR $row IN $copy DO SET $sum = $sum + $row.n END "
                    + "SET $value = $value + $sum + $rows[1].n",
                19));
        result.add(
            new Workload(
                "functions",
                1,
                "FUNCTION math.twice($x: INTEGER) RETURNS INTEGER DO RETURN math.add($x, $x) END "
                    + "FUNCTION math.add($a: INTEGER, $b: INTEGER) RETURNS INTEGER DO RETURN $a + $b END",
                "SET $value = $value + math.twice(21)",
                42));
        result.add(
            new Workload(
                "item_queries",
                1,
                "",
                "LET $source = LABEL(\"source\") IF ($source.available()) THEN "
                    + "IF (NOT $source.empty()) THEN SET $value = $value + $source.count(ITEM(\"*\")) END END",
                48));
        result.add(
            new Workload(
                "redstone",
                1,
                "",
                "LET $level = LABEL(\"signal\").side(\"north\").redstone() "
                    + "SET $value = $value + $level OUTPUT ($level) REDSTONE LEVEL TO signal EAST SIDE",
                7));
        result.add(new Workload("not_due_counter", 1200, "", "SET $value = $value + 1", 1));
        return result;
    }

    private static final class Metrics {

        final long[] nanos, allocation;

        Metrics(int size) {
            nanos = new long[size];
            allocation = new long[size];
        }

        void record(int sample, long ns, long bytes) {
            nanos[sample] = ns;
            allocation[sample] = bytes;
        }

        long totalNanos() {
            long total = 0;
            for (long value : nanos) total += value;
            return total;
        }

        JsonObject json() {
            JsonObject result = new JsonObject();
            result.add("sampleNanos", quantiles(nanos, 1));
            result.add("sampleMillis", quantiles(nanos, 1e6));
            boolean supported = true;
            long total = 0;
            for (long value : allocation) {
                if (value < 0) supported = false;
                total += value;
            }
            result.addProperty("allocationSupported", supported);
            result.add("sampleAllocatedBytes", supported ? quantiles(allocation, 1) : JsonNull.INSTANCE);
            if (supported) result.addProperty("totalAllocatedBytes", total);
            result.addProperty("measuredWorkNanos", totalNanos());
            return result;
        }
    }

    private static JsonObject quantiles(long[] values, double divisor) {
        long[] sorted = values.clone();
        Arrays.sort(sorted);
        JsonObject result = new JsonObject();
        result.addProperty("samples", sorted.length);
        result.addProperty("p50", sorted[(int) Math.ceil(.50 * sorted.length) - 1] / divisor);
        result.addProperty("p95", sorted[(int) Math.ceil(.95 * sorted.length) - 1] / divisor);
        result.addProperty("p99", sorted[(int) Math.ceil(.99 * sorted.length) - 1] / divisor);
        result.addProperty("max", sorted[sorted.length - 1] / divisor);
        JsonArray raw = new JsonArray();
        for (long value : values) raw.add(new JsonPrimitive(value / divisor));
        result.add("raw", raw);
        return result;
    }

    private static long delta(long after, long before) {
        return before < 0 || after < before ? -1 : after - before;
    }

    private static final class AllocationMeter {

        final ThreadMXBean bean;

        AllocationMeter() {
            java.lang.management.ThreadMXBean raw = ManagementFactory.getThreadMXBean();
            ThreadMXBean available = raw instanceof ThreadMXBean ? (ThreadMXBean) raw : null;
            try {
                if (available != null && available.isThreadAllocatedMemorySupported()) {
                    if (!available.isThreadAllocatedMemoryEnabled()) available.setThreadAllocatedMemoryEnabled(true);
                } else available = null;
            } catch (RuntimeException unsupported) {
                available = null;
            }
            bean = available;
        }

        long bytes() {
            return bean == null ? -1
                : bean.getThreadAllocatedBytes(
                    Thread.currentThread()
                        .getId());
        }
    }

    private static final class GcSnapshot {

        final Map<String, long[]> values = new LinkedHashMap<>();

        static GcSnapshot capture() {
            GcSnapshot result = new GcSnapshot();
            for (GarbageCollectorMXBean collector : ManagementFactory.getGarbageCollectorMXBeans()) result.values
                .put(collector.getName(), new long[] { collector.getCollectionCount(), collector.getCollectionTime() });
            return result;
        }

        JsonArray delta(GcSnapshot after) {
            JsonArray result = new JsonArray();
            for (Map.Entry<String, long[]> entry : values.entrySet()) {
                long[] end = after.values.get(entry.getKey()), start = entry.getValue();
                JsonObject collector = new JsonObject();
                collector.addProperty("collector", entry.getKey());
                boolean supported = end != null && start[0] >= 0
                    && start[1] >= 0
                    && end[0] >= start[0]
                    && end[1] >= start[1];
                collector.addProperty("supported", supported);
                if (supported) {
                    collector.addProperty("collections", end[0] - start[0]);
                    collector.addProperty("timeMs", end[1] - start[1]);
                }
                result.add(collector);
            }
            return result;
        }
    }

    private static int setting(String name, int fallback, int minimum, int maximum) {
        String setting = System.getenv(name);
        int value = setting == null || setting.isEmpty() ? fallback : Integer.parseInt(setting);
        if (value < minimum || value > maximum) throw new IllegalArgumentException("Invalid program benchmark setting");
        return value;
    }

    private static String label() {
        String label = System.getenv("SFM_PROGRAM_BENCHMARK_LABEL");
        if (label == null || label.isEmpty()) return "unlabelled";
        if (!label.matches("[A-Za-z0-9_.-]{1,80}"))
            throw new IllegalArgumentException("Invalid program benchmark label");
        return label;
    }

    private static Path output() throws Exception {
        String path = System.getenv("SFM_PROGRAM_BENCHMARK_OUTPUT");
        return path == null || path.isEmpty()
            ? Files.createTempFile("sfm-program-characterization-" + SEED + "-", ".json")
            : Paths.get(path);
    }
}
