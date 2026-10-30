package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Writer;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sun.management.ThreadMXBean;

import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.program.ProgramDiskData;
import ca.teamdman.sfm.common.program.ProgramStateStore;
import ca.teamdman.sfm.common.program.project.ExecutionReference;
import ca.teamdman.sfm.common.program.project.PreparedExecution;
import ca.teamdman.sfm.common.program.project.ProjectDraft;
import ca.teamdman.sfm.common.program.project.ProjectResult;
import ca.teamdman.sfm.common.program.project.ProjectWorkspace;
import ca.teamdman.sfm.common.program.project.PublicationReceipt;
import ca.teamdman.sfm.common.tileentity.LogLevel;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.Program;

/**
 * Opt-in diagnostic evidence, never a timing gate. Real grant/bundle reads and fresh per-manager compilation are used.
 * The fake world isolates activation/validation from pack handlers. Logical ticks advance without 50 ms wall pacing.
 * SFM_REFERENCE_BENCHMARK_OUTPUT selects the JSON destination; all fixture setup and JSON writing are untimed.
 */
@EnabledIfSystemProperty(named = "sfm.auditBenchmark", matches = "true")
class ReferenceLoadingCharacterizationTest {

    private static final long SEED = Long.parseLong(System.getProperty("sfm.characterizationSeed", "1037"));
    private static final int WARMUP = setting("SFM_REFERENCE_BENCHMARK_WARMUP", 16, 0, 256);
    private static final int SAMPLES = setting("SFM_REFERENCE_BENCHMARK_SAMPLES", 96, 16, 1024);
    private static final long POLL_NANOS = 1_000_000;
    private static final long DEADLINE_NANOS = TimeUnit.SECONDS.toNanos(120);
    private static final AllocationMeter ALLOCATION = new AllocationMeter();
    @TempDir
    Path temporary;

    @Test
    void characterizeColdReferenceAdmissionAndWarmValidation() throws Exception {
        JsonObject report = new JsonObject();
        report.addProperty("format", 1);
        report.addProperty("benchmark", "reference-activation-and-warm-validation");
        report.addProperty("seed", SEED);
        report.addProperty("label", label());
        report.addProperty("javaVersion", System.getProperty("java.version"));
        report.addProperty("vm", System.getProperty("java.vm.name"));
        report.addProperty("os", System.getProperty("os.name"));
        report.addProperty(
            "maxHeapBytes",
            Runtime.getRuntime()
                .maxMemory());
        report.addProperty(
            "availableProcessors",
            Runtime.getRuntime()
                .availableProcessors());
        report.addProperty("warmupFrames", WARMUP);
        report.addProperty("measuredWarmFrames", SAMPLES);
        report.addProperty("pollNanos", POLL_NANOS);
        report.addProperty("allocationSupported", ALLOCATION.available());
        report.addProperty(
            "conditions",
            "Headless fake world; real temporary-world grant/bundle storage. Cold means "
                + "no installed manager plan, not an OS page-cache flush. Setup/publication/issuance/JIT priming excluded. "
                + "Logical ticks advance with 1 ms polling, not 20 TPS wall pacing. Every successful activation compiles "
                + "a fresh mutable Program. No throughput or scale claim for the Java 17 game pack is implied.");
        report.addProperty(
            "admissionAccounting",
            "attempted/admitted/rejected count the manager protected gateway calls: "
                + "baseline admissions are raw worker jobs; FIFO admissions are logical queued activations. "
                + "Callbacks report actual completed installations, not registrations. Test callback checks are included.");
        report.addProperty("compilerPrimingLoadsPerCase", 4);
        report.addProperty(
            "comparisonRule",
            "Compare full_completion only at equal expected/completed useful work. "
                + "burst_no_retry can be incomplete; rejections and skipped work are not speedups. Full completion "
                + "explicitly retries busy-latched managers through Run, then immediately Pauses them before completion.");
        JsonArray cases = new JsonArray();
        report.add("cases", cases);
        Path output = output();
        List<Scenario> scenarios = new ArrayList<>();
        for (int size : sizes()) for (boolean complex : new boolean[] { false, true }) {
            scenarios.add(new Scenario(size, complex, false));
            if (size == 128) scenarios.add(new Scenario(size, complex, true));
        }
        Collections.shuffle(scenarios, new Random(SEED));
        int caseIndex = 0;
        try {
            for (Scenario scenario : scenarios) cases.add(characterize(scenario, caseIndex++));
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
            System.out.println("REFERENCE_CHAR_JSON " + output.toAbsolutePath());
        }
    }

    private JsonObject characterize(Scenario scenario, int index) throws Exception {
        Path directory = Files.createDirectory(temporary.resolve("case-" + index));
        try (ProjectWorkspace workspace = new ProjectWorkspace(directory)) {
            int builds = scenario.unique ? scenario.managers : 1;
            ExecutionReference[] references = new ExecutionReference[builds];
            String[] sources = new String[builds];
            for (int i = 0; i < builds; i++) {
                UUID owner = new UUID(SEED ^ index, i / ProjectWorkspace.MAX_PROJECTS_PER_OWNER + 1);
                String source = source(scenario.complex, i);
                sources[i] = source;
                ProjectDraft draft = call(
                    workspace,
                    callback -> assertTrue(workspace.create(owner, "Fixture", source, callback)));
                PublicationReceipt receipt = call(
                    workspace,
                    callback -> assertTrue(workspace.publish(owner, draft.id, 0, callback)));
                references[i] = call(
                    workspace,
                    callback -> assertTrue(
                        workspace.issueExecutionGrant(owner, draft.id, 0, receipt.digest, callback)));
            }
            // Prime the actual cold path, including worker startup/compiler initialization, without sharing a plan.
            for (int i = 0; i < 4; i++) {
                PreparedExecution primed = call(
                    workspace,
                    callback -> assertTrue(workspace.prepareExecution(references[0], callback)));
                assertEquals(sources[0], primed.source);
            }
            AuditRegressionTest.FakeWorld world = AuditRegressionTest.world();
            java.lang.reflect.Field provider = World.class.getDeclaredField("provider");
            provider.setAccessible(true);
            provider.set(world, new net.minecraft.world.WorldProviderSurface());
            List<MeasuredManager> managers = new ArrayList<>();
            for (int i = 0; i < scenario.managers; i++) {
                MeasuredManager manager = new MeasuredManager(workspace);
                manager.setWorldObj(world);
                manager.xCoord = 2 * i;
                manager.yCoord = 64;
                world.tiles.put(manager.getBlockPos(), manager);
                manager.setLogLevel(LogLevel.OFF);
                manager.setInventorySlotContents(0, referenceDisk(references[scenario.unique ? i : 0]));
                manager.setRunning(false); // Activation-only load measurement; no early program execution bonus.
                managers.add(manager);
            }
            long worker = ALLOCATION.worker();
            long allocatedWorkerBefore = ALLOCATION.bytes(worker);
            GcSnapshot gcBefore = GcSnapshot.capture();
            FrameMetrics coldFrames = new FrameMetrics();
            long initialWorldTick = world.time;
            long start = System.nanoTime();
            long deadline = start + DEADLINE_NANOS;
            long logicalTicks = 0;
            do {
                coldFrame(workspace, world, managers, coldFrames, false);
                logicalTicks++;
                checkDeadline(deadline);
                if (!terminal(managers)) LockSupport.parkNanos(POLL_NANOS);
            } while (!terminal(managers));
            long burstEnd = System.nanoTime();
            Counts burstCounts = counts(managers);
            long[] burstLatencies = latencies(managers, start);
            long[] burstTicks = completionTicks(managers, initialWorldTick);
            long burstFrames = logicalTicks;
            // Preserve initial admission loss in its own row, then finish all the same useful work with explicit
            // retries.
            while (!ready(managers)) {
                coldFrame(workspace, world, managers, coldFrames, true);
                logicalTicks++;
                checkDeadline(deadline);
                if (!ready(managers)) LockSupport.parkNanos(POLL_NANOS);
            }
            long fullEnd = System.nanoTime();
            long allocatedWorkerAfter = ALLOCATION.bytes(worker);
            GcSnapshot gcAfter = GcSnapshot.capture();
            Counts fullCounts = counts(managers);
            assertEquals(scenario.managers, fullCounts.completed);
            assertEquals(0, fullCounts.failedCallbacks);
            assertEquals(
                fullCounts.admitted,
                fullCounts.completed,
                "Every admitted logical request must complete exactly once");
            Set<Program> plans = Collections.newSetFromMap(new IdentityHashMap<Program, Boolean>());
            Set<ProgramStateStore> states = Collections
                .newSetFromMap(new IdentityHashMap<ProgramStateStore, Boolean>());
            for (MeasuredManager manager : managers) {
                assertTrue(plans.add(manager.getCompiledProgram()), "Mutable Program was shared between managers");
                assertNotNull(manager.getCompiledProgram());
                assertFalse(manager.isRunning());
                if (scenario.complex) assertTrue(states.add(manager.getProgramState()));
            }
            JsonObject record = scenario.json();
            record.addProperty(
                "programShape",
                scenario.complex
                    ? "32 integer helper functions (3 locals each), one persistent counter; helpers are compile load, not warm execution"
                    : "state-free empty 20-tick trigger");
            record.addProperty("sourceUtf8Bytes", sources[0].getBytes(StandardCharsets.UTF_8).length);
            record.addProperty("sourceGeneratorVersion", 1);
            record.addProperty("separateMutablePlans", plans.size());
            record.addProperty("separateStateStores", states.size());
            JsonObject burst = coldRow(burstCounts, scenario.managers, burstEnd - start, burstLatencies, burstTicks);
            burst.addProperty("logicalTicks", burstFrames);
            burst.addProperty("completeWorkload", burstCounts.completed == scenario.managers);
            record.add("burst_no_retry", burst);
            JsonObject full = coldRow(
                fullCounts,
                scenario.managers,
                fullEnd - start,
                latencies(managers, start),
                completionTicks(managers, initialWorldTick));
            full.addProperty("logicalTicks", logicalTicks);
            full.addProperty("completeWorkload", true);
            full.add("serverFrameNs", quantiles(coldFrames.nanos));
            full.add("serverFrameMs", quantilesMs(coldFrames.nanos));
            full.add("serverFrameAllocatedBytes", allocationTotal(coldFrames.allocations));
            full.add("workerAllocatedBytes", allocationDelta(allocatedWorkerBefore, allocatedWorkerAfter));
            full.add("gc", gcBefore.delta(gcAfter));
            record.add("full_completion", full);
            // Remove the backing files only after every plan is installed: warm correctness now rules out rereads.
            for (ExecutionReference reference : references) {
                Files.delete(
                    directory.resolve("sfm/execution-grants")
                        .resolve(reference.token + ".bin"));
                Files.delete(
                    directory.resolve("sfm/programs")
                        .resolve(reference.digest + ".sfmp"));
            }
            record.add("warm_paused", warm(world, managers, false, scenario.complex));
            record.add("warm_running", warm(world, managers, true, scenario.complex));
            record.addProperty("backingGrantAndBundleFilesRemovedBeforeWarm", true);
            System.out.println(
                "REFERENCE_CHAR_CASE size=" + scenario.managers
                    + " complex="
                    + scenario.complex
                    + " unique="
                    + scenario.unique
                    + " burstCompleted="
                    + burstCounts.completed
                    + " fullCompleted="
                    + fullCounts.completed
                    + " attempts="
                    + fullCounts.attempted);
            return record;
        }
    }

    private static void coldFrame(ProjectWorkspace workspace, AuditRegressionTest.FakeWorld world,
        List<MeasuredManager> managers, FrameMetrics metrics, boolean retry) {
        world.time++;
        long allocation = ALLOCATION.current(), begin = System.nanoTime();
        for (MeasuredManager manager : managers) {
            if (manager.readyAt != 0) continue;
            if (retry && manager.rejectedBusy()) manager.setRunning(true);
            manager.updateEntity();
            if (manager.isRunning()) manager.setRunning(false);
        }
        workspace.drainCallbacks(8);
        long nanos = System.nanoTime() - begin;
        long allocated = delta(ALLOCATION.current(), allocation);
        metrics.nanos.add(nanos);
        metrics.allocations.add(allocated);
    }

    private static JsonObject warm(AuditRegressionTest.FakeWorld world, List<MeasuredManager> managers, boolean running,
        boolean stateful) {
        long[] clockBefore = new long[managers.size()], stateBefore = new long[managers.size()];
        NBTBase[] envelopes = new NBTBase[managers.size()];
        Counts requestsBefore = counts(managers);
        for (int i = 0; i < managers.size(); i++) {
            MeasuredManager manager = managers.get(i);
            manager.setRunning(running);
            clockBefore[i] = manager.getManagerTick();
            if (stateful) stateBefore[i] = manager.getProgramState()
                .read(0)
                .asLong();
            envelopes[i] = manager.getDisk()
                .getTagCompound()
                .getTag(ProgramStateStore.NBT_KEY);
        }
        FrameMetrics frames = new FrameMetrics();
        GcSnapshot gcBefore = GcSnapshot.capture();
        for (int sample = -WARMUP; sample < SAMPLES; sample++) {
            world.time++;
            long allocation = ALLOCATION.current(), begin = System.nanoTime();
            for (MeasuredManager manager : managers) manager.updateEntity();
            long nanos = System.nanoTime() - begin, allocated = delta(ALLOCATION.current(), allocation);
            if (sample >= 0) {
                frames.nanos.add(nanos);
                frames.allocations.add(allocated);
            }
        }
        GcSnapshot gcAfter = GcSnapshot.capture();
        Counts requestsAfter = counts(managers);
        assertEquals(requestsBefore.attempted, requestsAfter.attempted, "Warm execution resubmitted storage work");
        assertEquals(requestsBefore.lookups, requestsAfter.lookups, "Warm execution looked up a storage workspace");
        for (int i = 0; i < managers.size(); i++) {
            MeasuredManager manager = managers.get(i);
            long ticks = running ? WARMUP + SAMPLES : 0;
            assertEquals(clockBefore[i] + ticks, manager.getManagerTick());
            if (stateful) assertEquals(
                stateBefore[i] + ticks,
                manager.getProgramState()
                    .read(0)
                    .asLong());
            assertSame(
                envelopes[i],
                manager.getDisk()
                    .getTagCompound()
                    .getTag(ProgramStateStore.NBT_KEY),
                "Warm ticks serialized or replaced committed STATE");
            assertNotNull(manager.getCompiledProgram());
        }
        JsonObject result = new JsonObject();
        result.addProperty("managers", managers.size());
        result.addProperty("attemptedUpdates", (long) managers.size() * SAMPLES);
        result.addProperty("completedUpdates", (long) managers.size() * SAMPLES);
        result.addProperty("completedSchedulerTicks", running ? (long) managers.size() * SAMPLES : 0);
        result.addProperty("storageLookupDelta", requestsAfter.lookups - requestsBefore.lookups);
        result.addProperty("preparationAttemptDelta", requestsAfter.attempted - requestsBefore.attempted);
        result.add("cohortFrameNs", quantiles(frames.nanos));
        result.add("cohortFrameMs", quantilesMs(frames.nanos));
        result.add("allocatedBytes", allocationTotal(frames.allocations));
        result.add("gcIncludingWarmup", gcBefore.delta(gcAfter));
        long elapsed = sum(frames.nanos);
        result.addProperty("measuredWorkNanos", elapsed);
        result.addProperty(
            "updatesPerMeasuredSecond",
            elapsed == 0 ? 0 : managers.size() * (double) SAMPLES * 1e9 / elapsed);
        result.addProperty("counterAndStateCorrect", true);
        return result;
    }

    private static JsonObject coldRow(Counts counts, int expected, long nanos, long[] latency, long[] ticks) {
        JsonObject row = new JsonObject();
        row.addProperty("expectedManagers", expected);
        row.addProperty("attempted", counts.attempted);
        row.addProperty("admitted", counts.admitted);
        row.addProperty("rejected", counts.rejected);
        row.addProperty("completed", counts.completed);
        row.addProperty("failedCallbacks", counts.failedCallbacks);
        row.addProperty("workspaceLookups", counts.lookups);
        row.addProperty("wallNanos", nanos);
        row.addProperty("completionFraction", counts.completed / (double) expected);
        row.addProperty("completedPerWallSecond", counts.completed * 1e9 / Math.max(1, nanos));
        row.add("successfulCompletionLatencyNs", quantiles(latency));
        row.add("successfulCompletionLatencyMs", quantilesMs(latency));
        row.add("callbackLogicalTicks", quantiles(ticks));
        return row;
    }

    private static boolean ready(List<MeasuredManager> managers) {
        for (MeasuredManager manager : managers) if (manager.readyAt == 0) return false;
        return true;
    }

    private static boolean terminal(List<MeasuredManager> managers) {
        for (MeasuredManager manager : managers) if (manager.readyAt == 0 && !manager.rejectedBusy()) return false;
        return true;
    }

    private static void checkDeadline(long deadline) {
        assertTrue(System.nanoTime() < deadline, "Reference cohort did not settle before its diagnostic deadline");
    }

    private static Counts counts(List<MeasuredManager> managers) {
        Counts counts = new Counts();
        for (MeasuredManager manager : managers) {
            counts.attempted += manager.attempted;
            counts.admitted += manager.admitted;
            counts.rejected += manager.rejected;
            counts.completed += manager.completed;
            counts.failedCallbacks += manager.failed;
            counts.lookups += manager.lookups;
        }
        return counts;
    }

    private static long[] latencies(List<MeasuredManager> managers, long start) {
        long[] values = new long[managers.size()];
        int count = 0;
        for (MeasuredManager manager : managers) if (manager.readyAt != 0) values[count++] = manager.readyAt - start;
        return Arrays.copyOf(values, count);
    }

    private static long[] completionTicks(List<MeasuredManager> managers, long firstTick) {
        long[] values = new long[managers.size()];
        int count = 0;
        for (MeasuredManager manager : managers)
            if (manager.readyAt != 0) values[count++] = manager.readyTick - firstTick;
        return Arrays.copyOf(values, count);
    }

    private static String source(boolean complex, int id) {
        StringBuilder text = new StringBuilder("NAME \"reference_case_").append(id)
            .append("\" ");
        if (!complex) return text.append("EVERY 20 TICKS DO END")
            .toString();
        text.append("STATE $count = 0 ");
        for (int i = 0; i < 32; i++) text.append("FUNCTION f")
            .append(i)
            .append("($x: INTEGER) RETURNS INTEGER DO LET $a = $x + 1 LET $b = $a * 2 ")
            .append("LET $c = $b - 1 RETURN $c END ");
        return text.append("EVERY 1 TICKS DO SET $count = $count + 1 END")
            .toString();
    }

    private static ItemStack referenceDisk(ExecutionReference reference) {
        ItemStack disk = new ItemStack(new ItemDisk());
        NBTTagCompound root = new NBTTagCompound(), tag = new NBTTagCompound();
        tag.setInteger("format", ProgramDiskData.REFERENCE_FORMAT);
        tag.setString("grant", reference.token);
        tag.setString("digest", reference.digest);
        root.setTag(ProgramDiskData.NBT_REFERENCE, tag);
        disk.setTagCompound(root);
        return disk;
    }

    private static <T> T call(ProjectWorkspace workspace, Consumer<Consumer<ProjectResult<T>>> submit)
        throws Exception {
        AtomicReference<ProjectResult<T>> reply = new AtomicReference<>();
        submit.accept(reply::set);
        long deadline = System.nanoTime() + DEADLINE_NANOS;
        while (reply.get() == null) {
            workspace.drainCallbacks(8);
            checkDeadline(deadline);
            if (reply.get() == null) LockSupport.parkNanos(POLL_NANOS);
        }
        assertTrue(
            reply.get()
                .isSuccess(),
            "Fixture storage setup failed");
        return reply.get().value;
    }

    private static JsonObject quantiles(List<Long> values) {
        long[] array = new long[values.size()];
        for (int i = 0; i < array.length; i++) array[i] = values.get(i);
        return quantiles(array);
    }

    private static JsonObject quantiles(long[] values) {
        long[] sorted = values.clone();
        Arrays.sort(sorted);
        JsonObject result = new JsonObject();
        result.addProperty("samples", sorted.length);
        if (sorted.length == 0) return result;
        result.addProperty("p50", percentile(sorted, .50));
        result.addProperty("p95", percentile(sorted, .95));
        result.addProperty("p99", percentile(sorted, .99));
        result.addProperty("max", sorted[sorted.length - 1]);
        JsonArray raw = new JsonArray();
        for (long value : values) raw.add(new JsonPrimitive(value));
        result.add("raw", raw);
        return result;
    }

    private static JsonObject quantilesMs(List<Long> values) {
        long[] array = new long[values.size()];
        for (int i = 0; i < array.length; i++) array[i] = values.get(i);
        return quantilesMs(array);
    }

    private static JsonObject quantilesMs(long[] values) {
        long[] sorted = values.clone();
        Arrays.sort(sorted);
        JsonObject result = new JsonObject();
        result.addProperty("samples", sorted.length);
        if (sorted.length > 0) {
            result.addProperty("p50", percentile(sorted, .50) / 1e6);
            result.addProperty("p95", percentile(sorted, .95) / 1e6);
            result.addProperty("p99", percentile(sorted, .99) / 1e6);
            result.addProperty("max", sorted[sorted.length - 1] / 1e6);
        }
        return result;
    }

    private static long percentile(long[] sorted, double quantile) {
        return sorted[Math.max(0, (int) Math.ceil(quantile * sorted.length) - 1)];
    }

    private static JsonObject allocationTotal(List<Long> values) {
        JsonObject object = new JsonObject();
        boolean supported = true;
        for (long value : values) if (value < 0) supported = false;
        object.addProperty("supported", supported);
        if (supported) {
            object.addProperty("total", sum(values));
            object.add("perFrame", quantiles(values));
        }
        return object;
    }

    private static com.google.gson.JsonElement allocationDelta(long before, long after) {
        return before < 0 || after < before ? JsonNull.INSTANCE : new JsonPrimitive(after - before);
    }

    private static long delta(long after, long before) {
        return before < 0 || after < before ? -1 : after - before;
    }

    private static long sum(List<Long> values) {
        long sum = 0;
        for (long value : values) sum += value;
        return sum;
    }

    private static int setting(String name, int fallback, int minimum, int maximum) {
        String value = System.getenv(name);
        int parsed = value == null || value.isEmpty() ? fallback : Integer.parseInt(value);
        if (parsed < minimum || parsed > maximum)
            throw new IllegalArgumentException("Invalid reference benchmark setting");
        return parsed;
    }

    private static int[] sizes() {
        String input = System.getenv("SFM_REFERENCE_BENCHMARK_SIZES");
        if (input == null || input.isEmpty()) return new int[] { 32, 128, 512 };
        String[] parts = input.split(",");
        if (parts.length > 4) throw new IllegalArgumentException("Too many reference benchmark sizes");
        int[] sizes = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            sizes[i] = Integer.parseInt(parts[i].trim());
            if (sizes[i] < 1 || sizes[i] > 512) throw new IllegalArgumentException("Invalid reference benchmark size");
        }
        return sizes;
    }

    private static String label() {
        String label = System.getenv("SFM_REFERENCE_BENCHMARK_LABEL");
        if (label == null || label.isEmpty()) return "unlabelled";
        if (!label.matches("[A-Za-z0-9_.-]{1,80}"))
            throw new IllegalArgumentException("Invalid reference benchmark label");
        return label;
    }

    private static Path output() throws Exception {
        String output = System.getenv("SFM_REFERENCE_BENCHMARK_OUTPUT");
        return output == null || output.isEmpty()
            ? Files.createTempFile("sfm-reference-characterization-" + SEED + "-", ".json")
            : Paths.get(output);
    }

    private static final class Scenario {

        final int managers;
        final boolean complex, unique;

        Scenario(int managers, boolean complex, boolean unique) {
            this.managers = managers;
            this.complex = complex;
            this.unique = unique;
        }

        JsonObject json() {
            JsonObject result = new JsonObject();
            result.addProperty("managers", managers);
            result.addProperty("complex", complex);
            result.addProperty("uniqueBuilds", unique);
            return result;
        }
    }

    private static final class Counts {

        long attempted, admitted, rejected, completed, failedCallbacks, lookups;
    }

    private static final class FrameMetrics {

        final List<Long> nanos = new ArrayList<>(), allocations = new ArrayList<>();
    }

    private static final class MeasuredManager extends TileEntityManager {

        private final ProjectWorkspace workspace;
        private final Thread server = Thread.currentThread();
        long attempted, admitted, rejected, completed, failed, lookups, readyAt, readyTick;

        MeasuredManager(ProjectWorkspace workspace) {
            this.workspace = workspace;
        }

        @Override
        protected ProjectWorkspace executionWorkspace(World world) {
            lookups++;
            return workspace;
        }

        @Override
        protected boolean executionWorkspaceCurrent(ProjectWorkspace expected, World world) {
            return expected == workspace && !expected.isClosed() && world == getWorldObj();
        }

        @Override
        protected boolean referenceServerThread() {
            return Thread.currentThread() == server;
        }

        @Override
        protected boolean prepareReferenceExecution(ProjectWorkspace store, ExecutionReference reference,
            Consumer<ProjectResult<PreparedExecution>> callback) {
            attempted++;
            boolean accepted = super.prepareReferenceExecution(store, reference, result -> {
                if (result.isSuccess()) {
                    assertEquals(reference, result.value.reference);
                    callback.accept(result);
                    assertNotNull(getCompiledProgram(), "Correct real storage result was not installed");
                    assertEquals(0, completed, "A manager activation completed twice");
                    readyAt = System.nanoTime();
                    readyTick = getWorldObj().getTotalWorldTime();
                    completed++;
                } else {
                    failed++;
                    callback.accept(result);
                }
            });
            if (accepted) admitted++;
            else rejected++;
            return accepted;
        }

        boolean rejectedBusy() {
            return readyAt == 0 && getStatusMessage().startsWith("Reference storage busy; use Run to retry");
        }
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

        boolean available() {
            return bean != null;
        }

        long current() {
            return bytes(
                Thread.currentThread()
                    .getId());
        }

        long bytes(long id) {
            return bean == null || id < 0 ? -1 : bean.getThreadAllocatedBytes(id);
        }

        long worker() {
            if (bean == null) return -1;
            long found = -1;
            for (long id : bean.getAllThreadIds()) {
                ThreadInfo info = bean.getThreadInfo(id);
                if (info != null && "SFM project storage".equals(info.getThreadName())) {
                    if (found != -1) return -1;
                    found = id;
                }
            }
            return found;
        }
    }

    private static final class GcSnapshot {

        final Map<String, long[]> values = new LinkedHashMap<>();

        static GcSnapshot capture() {
            GcSnapshot result = new GcSnapshot();
            for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans())
                result.values.put(bean.getName(), new long[] { bean.getCollectionCount(), bean.getCollectionTime() });
            return result;
        }

        JsonArray delta(GcSnapshot after) {
            JsonArray result = new JsonArray();
            for (Map.Entry<String, long[]> entry : values.entrySet()) {
                long[] end = after.values.get(entry.getKey());
                if (end == null) continue;
                JsonObject collector = new JsonObject();
                collector.addProperty("collector", entry.getKey());
                collector.add("collections", allocationDelta(entry.getValue()[0], end[0]));
                collector.add("timeMs", allocationDelta(entry.getValue()[1], end[1]));
                result.add(collector);
            }
            return result;
        }
    }
}
