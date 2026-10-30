package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Writer;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import ca.teamdman.sfm.common.program.project.ExecutionReference;
import ca.teamdman.sfm.common.program.project.PreparedExecution;
import ca.teamdman.sfm.common.program.project.ProjectDraft;
import ca.teamdman.sfm.common.program.project.ProjectPage;
import ca.teamdman.sfm.common.program.project.ProjectResult;
import ca.teamdman.sfm.common.program.project.ProjectWorkspace;
import ca.teamdman.sfm.common.program.project.PublicationReceipt;
import ca.teamdman.sfml.ast.Program;

/** Equal completed work, real storage/compiler; deliberately diagnostic rather than a timing assertion. */
@EnabledIfSystemProperty(named = "sfm.auditBenchmark", matches = "true")
class ReferenceAuthoringLatencyCharacterizationTest {

    private static final long SEED = Long.parseLong(System.getProperty("sfm.characterizationSeed", "1037"));
    private static final int COLD_LOADS = 256, AUTHOR_REQUESTS = 32;
    private static final long POLL_NANOS = 1_000_000, DEADLINE_NANOS = TimeUnit.SECONDS.toNanos(120);
    @TempDir
    Path temporary;

    @Test
    void characterizeAuthoringDuringBulkColdLoads() throws Exception {
        JsonObject report = new JsonObject();
        report.addProperty("format", 1);
        report.addProperty("benchmark", "reference-authoring-contention");
        report.addProperty("seed", SEED);
        report.addProperty("label", label());
        report.addProperty("javaVersion", System.getProperty("java.version"));
        report.addProperty("vm", System.getProperty("java.vm.name"));
        report.addProperty(
            "maxHeapBytes",
            Runtime.getRuntime()
                .maxMemory());
        report.addProperty(
            "availableProcessors",
            Runtime.getRuntime()
                .availableProcessors());
        report.addProperty(
            "conditions",
            "One real temporary world, one complex published build/grant, 256 fresh mutable "
                + "Programs, 16 exact owner draft loads and 16 owner catalog lists. Initial cold offers precede all author "
                + "offers. Each logical frame retries every unadmitted cold request, then every unadmitted author request, "
                + "then drains 8 callbacks. Rejected operations remain pending until accepted and completed. Logical frames "
                + "use 1 ms polling, not 20 TPS pacing; cold is not an OS cache flush. Setup and 4 compiler priming loads "
                + "excluded. Latency starts at each operation's first offer and includes rejection/retry waiting. "
                + "This workspace contention diagnostic does not model manager retry backoff or Java 17 game throughput.");
        report.addProperty(
            "admissionAccounting",
            "Cold accepted is a logical FIFO registration when enqueueExecution "
                + "exists, otherwise a raw worker job. Actual completions and distinct Program identities are checked. "
                + "Authoring accepted always means a raw worker job. No skipped/rejected work counts as a completion.");
        Path output = output();
        try (ProjectWorkspace workspace = new ProjectWorkspace(temporary)) {
            UUID owner = new UUID(SEED, 20261029);
            String source = complexSource();
            ProjectDraft draft = call(
                workspace,
                callback -> assertTrue(workspace.create(owner, "Contention fixture", source, callback)));
            PublicationReceipt publication = call(
                workspace,
                callback -> assertTrue(workspace.publish(owner, draft.id, 0, callback)));
            ExecutionReference reference = call(
                workspace,
                callback -> assertTrue(
                    workspace.issueExecutionGrant(owner, draft.id, 0, publication.digest, callback)));
            for (int i = 0; i < 4; i++) {
                PreparedExecution primed = call(
                    workspace,
                    callback -> assertTrue(workspace.prepareExecution(reference, callback)));
                assertEquals(source, primed.source);
            }
            Admission admission = new Admission(workspace);
            report.addProperty(
                "coldAdmissionApi",
                admission.queued == null ? "direct-prepare-with-manual-retry"
                    : "fifo-enqueue-with-manual-registration-retry");
            report.addProperty("sourceUtf8Bytes", source.getBytes(StandardCharsets.UTF_8).length);
            report.addProperty("compilerPrimingLoads", 4);
            List<Cold> cold = new ArrayList<>();
            for (int i = 0; i < COLD_LOADS; i++) cold.add(new Cold(reference));
            List<Author> authors = new ArrayList<>();
            for (int i = 0; i < AUTHOR_REQUESTS; i++) authors.add(new Author(i % 2 == 0, admission));
            Collections.shuffle(authors, new Random(SEED));
            long[] gcBefore = gc();
            long start = System.nanoTime(), frame = 0;
            long deadline = start + DEADLINE_NANOS;
            do {
                frame++;
                admission.frame = frame;
                for (Cold request : cold) if (!request.accepted) request.offer(admission, frame);
                for (Author request : authors) if (!request.accepted) request.offer(workspace, owner, draft, frame);
                if (frame == 1) {
                    report.add("initialColdOffers", accounting(cold));
                    report.add("initialAuthorOffers", accounting(authors));
                }
                workspace.drainCallbacks(8);
                assertTrue(
                    System.nanoTime() < deadline,
                    "Contention workload failed to complete before diagnostic deadline");
                if (!complete(cold) || !complete(authors)) LockSupport.parkNanos(POLL_NANOS);
            } while (!complete(cold) || !complete(authors));
            long elapsed = System.nanoTime() - start;
            Set<Program> plans = Collections.newSetFromMap(new IdentityHashMap<Program, Boolean>());
            for (Cold request : cold) {
                assertNotNull(request.prepared);
                assertEquals(reference, request.prepared.reference);
                assertEquals(source, request.prepared.source);
                assertTrue(plans.add(request.prepared.program), "Cold callers received a shared mutable Program");
            }
            report.addProperty("distinctMutablePrograms", plans.size());
            report.addProperty("logicalFrames", frame);
            report.addProperty("wallNanos", elapsed);
            report.addProperty("wallMs", elapsed / 1e6);
            report.addProperty(
                "allCompletionsPerWallSecond",
                (COLD_LOADS + AUTHOR_REQUESTS) * 1e9 / Math.max(1, elapsed));
            report.add("cold", results(cold));
            report.add("authoring", results(authors));
            List<Author> loads = new ArrayList<>(), lists = new ArrayList<>();
            for (Author author : authors) (author.load ? loads : lists).add(author);
            report.add("authorLoads", results(loads));
            report.add("authorLists", results(lists));
            report.add("gc", gcDelta(gcBefore, gc()));
            report.addProperty("correctOwnerRevisionAndSource", true);
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
            System.out.println("REFERENCE_AUTHOR_CHAR_JSON " + output.toAbsolutePath());
        }
    }

    private abstract static class Request {

        long attempts, rejected, firstAt, firstFrame, completedAt, completedFrame;
        boolean accepted;

        void beforeOffer(long frame) {
            attempts++;
            if (firstAt == 0) {
                firstAt = System.nanoTime();
                firstFrame = frame;
            }
        }

        void recordAdmission(boolean result) {
            accepted = result;
            if (!result) rejected++;
        }

        void completed(long frame) {
            assertEquals(0, completedAt, "Callback delivered more than once");
            completedAt = System.nanoTime();
            completedFrame = frame;
        }
    }

    private static final class Cold extends Request {

        final Object key = new Object();
        final ExecutionReference reference;
        PreparedExecution prepared;

        Cold(ExecutionReference reference) {
            this.reference = reference;
        }

        void offer(Admission admission, long frame) throws Exception {
            beforeOffer(frame);
            recordAdmission(admission.submit(key, reference, result -> {
                assertTrue(result.isSuccess(), "Real grant/bundle preparation failed");
                prepared = result.value;
                completed(admission.frame);
            }));
        }
    }

    private static final class Author extends Request {

        final boolean load;
        final Admission clock;

        Author(boolean load, Admission clock) {
            this.load = load;
            this.clock = clock;
        }

        void offer(ProjectWorkspace workspace, UUID owner, ProjectDraft draft, long frame) {
            beforeOffer(frame);
            if (load) recordAdmission(workspace.load(owner, draft.id, result -> {
                assertTrue(result.isSuccess(), "Authenticated owner draft load failed");
                ProjectDraft actual = result.value;
                assertEquals(owner, actual.owner);
                assertEquals(draft.id, actual.id);
                assertEquals(0, actual.revision);
                assertEquals(draft.source, actual.source);
                completed(clock.frame);
            }));
            else recordAdmission(workspace.list(owner, 0, result -> {
                assertTrue(result.isSuccess(), "Authenticated owner catalog list failed");
                ProjectPage page = result.value;
                assertEquals(1, page.total);
                assertEquals(0, page.offset);
                assertEquals(1, page.entries.size());
                assertEquals(draft.id, page.entries.get(0).id);
                assertEquals(0, page.entries.get(0).revision);
                completed(clock.frame);
            }));
        }
    }

    private static final class Admission {

        final ProjectWorkspace workspace;
        final Method queued;
        long frame;

        Admission(ProjectWorkspace workspace) throws Exception {
            this.workspace = workspace;
            Method found;
            try {
                found = ProjectWorkspace.class.getMethod(
                    "enqueueExecution",
                    Object.class,
                    ExecutionReference.class,
                    BooleanSupplier.class,
                    Consumer.class);
            } catch (NoSuchMethodException baseline) {
                found = null;
            }
            queued = found;
        }

        boolean submit(Object key, ExecutionReference reference, Consumer<ProjectResult<PreparedExecution>> callback)
            throws Exception {
            return queued == null ? workspace.prepareExecution(reference, callback)
                : (Boolean) queued.invoke(workspace, key, reference, (BooleanSupplier) () -> true, callback);
        }
    }

    private static boolean complete(List<? extends Request> requests) {
        for (Request request : requests) if (request.completedAt == 0) return false;
        return true;
    }

    private static JsonObject accounting(List<? extends Request> requests) {
        long attempts = 0, admitted = 0, rejected = 0, completed = 0;
        for (Request request : requests) {
            attempts += request.attempts;
            if (request.accepted) admitted++;
            rejected += request.rejected;
            if (request.completedAt != 0) completed++;
        }
        JsonObject result = new JsonObject();
        result.addProperty("expected", requests.size());
        result.addProperty("attempted", attempts);
        result.addProperty("admitted", admitted);
        result.addProperty("rejected", rejected);
        result.addProperty("completed", completed);
        return result;
    }

    private static JsonObject results(List<? extends Request> requests) {
        JsonObject result = accounting(requests);
        long[] latency = new long[requests.size()], frames = new long[requests.size()];
        int index = 0;
        for (Request request : requests) {
            assertTrue(request.accepted);
            assertTrue(request.completedAt != 0);
            latency[index] = request.completedAt - request.firstAt;
            frames[index++] = request.completedFrame - request.firstFrame;
        }
        result.add("firstRequestedToCompletionNs", quantiles(latency, 1));
        result.add("firstRequestedToCompletionMs", quantiles(latency, 1e6));
        result.add("callbackFramesAfterFirstRequest", quantiles(frames, 1));
        return result;
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

    private static <T> T call(ProjectWorkspace workspace, Consumer<Consumer<ProjectResult<T>>> submit)
        throws Exception {
        AtomicReference<ProjectResult<T>> reply = new AtomicReference<>();
        submit.accept(reply::set);
        long deadline = System.nanoTime() + DEADLINE_NANOS;
        while (reply.get() == null) {
            workspace.drainCallbacks(8);
            assertTrue(System.nanoTime() < deadline, "Contention fixture setup did not finish");
            if (reply.get() == null) LockSupport.parkNanos(POLL_NANOS);
        }
        assertTrue(
            reply.get()
                .isSuccess(),
            "Contention fixture setup failed");
        return reply.get().value;
    }

    private static String complexSource() {
        StringBuilder text = new StringBuilder("NAME \"authoring_contention\" STATE $count = 0 ");
        for (int i = 0; i < 32; i++) text.append("FUNCTION f")
            .append(i)
            .append("($x: INTEGER) RETURNS INTEGER DO LET $a = $x + 1 LET $b = $a * 2 ")
            .append("LET $c = $b - 1 RETURN $c END ");
        return text.append("EVERY 1 TICKS DO SET $count = $count + 1 END")
            .toString();
    }

    private static String label() {
        String label = System.getenv("SFM_REFERENCE_AUTHORING_BENCHMARK_LABEL");
        if (label == null || label.isEmpty()) return "unlabelled";
        if (!label.matches("[A-Za-z0-9_.-]{1,80}"))
            throw new IllegalArgumentException("Invalid authoring benchmark label");
        return label;
    }

    private static Path output() throws Exception {
        String output = System.getenv("SFM_REFERENCE_AUTHORING_BENCHMARK_OUTPUT");
        return output == null || output.isEmpty()
            ? Files.createTempFile("sfm-reference-authoring-" + SEED + "-", ".json")
            : Paths.get(output);
    }

    private static long[] gc() {
        long count = 0, time = 0;
        for (GarbageCollectorMXBean collector : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (collector.getCollectionCount() < 0 || collector.getCollectionTime() < 0) return new long[] { -1, -1 };
            count += collector.getCollectionCount();
            time += collector.getCollectionTime();
        }
        return new long[] { count, time };
    }

    private static JsonObject gcDelta(long[] before, long[] after) {
        JsonObject result = new JsonObject();
        result.addProperty("supported", before[0] >= 0 && after[0] >= 0);
        if (before[0] >= 0 && after[0] >= 0) {
            result.addProperty("collections", after[0] - before[0]);
            result.addProperty("timeMs", after[1] - before[1]);
        }
        return result;
    }
}
