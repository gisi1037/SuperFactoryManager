package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import com.sun.management.ThreadMXBean;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.program.project.LibraryImport;
import ca.teamdman.sfm.common.program.project.LibraryModule;
import ca.teamdman.sfm.common.program.project.PublishedLibrary;
import ca.teamdman.sfm.common.program.project.PublishedProgram;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.BundleCompiler;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

/** Headless diagnostics only: closed in-memory inputs, no repository/backend, and no timing thresholds. */
@EnabledIfSystemProperty(named = "sfm.auditBenchmark", matches = "true")
class LibraryPerformanceCharacterizationTest {

    private static final int WARMUP = 256, SAMPLES = 24, LENGTH = 64;
    private static final long SUM = (long) LENGTH * (LENGTH - 1) / 2;
    private static final AllocationMeter ALLOCATION = new AllocationMeter();

    @Test
    void characterizeClosedLibraryCompilationAndEquivalentInlineExecution() throws Exception {
        String function = "INTEGER sum(INTEGER[64] values) { LET total = 0; "
            + "FOR value IN values { total = total + value; } RETURN total; }\n";
        StringBuilder entries = new StringBuilder();
        for (int i = 0; i < LENGTH; i++) {
            if (i != 0) entries.append(',');
            entries.append(i);
        }
        String declarations = "STATE checksum = 0 LET values = [" + entries + "] ";
        String body = "EVERY 1 TICKS { checksum = checksum + %ssum(values); WAIT 0; }";
        String inlineSource = declarations + function + String.format(Locale.ROOT, body, "");
        PublishedLibrary library = new PublishedLibrary(function);
        LibraryImport reference = new LibraryImport("math", new UUID(1037, 20261101), 3, library.digest());
        String root = "import math from \"" + reference.project
            + "\" revision 3 digest \""
            + reference.digest
            + "\";\n"
            + declarations
            + String.format(Locale.ROOT, body, "math.");
        // Never publish or open a project/artifact: this is the complete, already-resolved compile input.
        PublishedProgram bundle = new PublishedProgram(
            root,
            Collections.singletonList(new LibraryModule(reference, library)));
        assertEquals(1, bundle.modules.size());
        assertEquals(library.digest(), bundle.modules.get(0).reference.digest);
        String bundleDigest = bundle.digest();
        Case inline = new Case("inline", () -> new ProgramBuilder(inlineSource).build());
        Case imported = new Case("one-library", () -> BundleCompiler.build(bundle));
        System.out.printf(
            Locale.ROOT,
            "LIBRARY_CHAR_ENV java=%s vm=%s vmVersion=%s os=%s arch=%s heapBytes=%d processors=%d "
                + "warmup=%d samples=%d allocatedBytesSupported=%s%n",
            System.getProperty("java.version"),
            System.getProperty("java.vm.name"),
            System.getProperty("java.vm.version"),
            System.getProperty("os.name"),
            System.getProperty("os.arch"),
            Runtime.getRuntime()
                .maxMemory(),
            Runtime.getRuntime()
                .availableProcessors(),
            WARMUP,
            SAMPLES,
            ALLOCATION.supported);
        System.out.println(
            "LIBRARY_CHAR_MODEL headless=true artifactIo=none repositoryOrBackend=none "
                + "compileIncludes=grammar,semantic-link,closed-digest-validation executionIncludes=Program.tick,scheduler,functions,state-write "
                + "executionExcludes=compilation,context-setup,fixture-setup,NBT-flush arrayLength=64 usefulSum=2016 "
                + "comparison=no-required-speedup,no-full-pack-or-port-performance-claim");
        for (int sample = -WARMUP; sample < SAMPLES; sample++) {
            if ((sample & 1) == 0) {
                inline.compile(sample);
                imported.compile(sample);
            } else {
                imported.compile(sample);
                inline.compile(sample);
            }
        }
        assertNotSame(inline.plan, imported.plan);
        assertNotSame(
            inline.plan,
            inline.build.get()
                .getProgram());
        assertNotSame(
            imported.plan,
            imported.build.get()
                .getProgram());
        assertNotSame(
            inline.plan.getFunctions()
                .get(0),
            imported.plan.getFunctions()
                .get(0));
        assertEquals(root, imported.plan.toString());
        assertFalse(
            imported.plan.toString()
                .contains("RETURN total"),
            "Linked printing must not flatten module source");
        assertEquals(bundleDigest, bundle.digest(), "Compilation must leave the immutable closed input unchanged");
        Run inlineRun = new Run(inline.plan), importedRun = new Run(imported.plan);
        assertNotSame(inlineRun.manager, importedRun.manager);
        assertNotSame(inlineRun.fixture.world, importedRun.fixture.world);
        assertNotSame(inlineRun.values, importedRun.values);
        for (int sample = -WARMUP; sample < SAMPLES; sample++) {
            long tick = sample + WARMUP + 1L;
            if ((sample & 1) == 0) {
                inlineRun.tick(tick, sample);
                importedRun.tick(tick, sample);
            } else {
                importedRun.tick(tick, sample);
                inlineRun.tick(tick, sample);
            }
            assertEquals(tick * SUM, inlineRun.values[0].asLong());
            assertEquals(inlineRun.values[0].asLong(), importedRun.values[0].asLong());
        }
        inline.compile.print("compile", inline.name);
        imported.compile.print("compile", imported.name);
        inlineRun.execution.print("run", inline.name);
        importedRun.execution.print("run", imported.name);
        System.out.printf(
            Locale.ROOT,
            "LIBRARY_CHAR_VERIFIED checksum=%d measuredRunsPerCase=%d "
                + "sourceItems=0 destinationItems=0 runProviderBlockReads=0 pendingContinuations=0 artifactIoByConstruction=none%n",
            inlineRun.values[0].asLong(),
            SAMPLES);
    }

    private static final class Case {

        final String name;
        final Supplier<ProgramBuildResult> build;
        final Stats compile = new Stats();
        Program plan;

        Case(String name, Supplier<ProgramBuildResult> build) {
            this.name = name;
            this.build = build;
        }

        void compile(int sample) {
            long beforeBytes = ALLOCATION.read(), before = System.nanoTime();
            ProgramBuildResult result = build.get();
            long elapsed = System.nanoTime() - before, bytes = ALLOCATION.delta(beforeBytes);
            assertTrue(result.isSuccess(), result.getErrorMessage());
            plan = result.getProgram();
            if (sample >= 0) compile.add(sample, elapsed, bytes);
        }
    }

    private static final class Run implements StateAccess {

        final AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        final TileEntityManager manager = RecoveryFixtures.manager(fixture);
        final CableNetwork network = new CableNetwork(fixture.world, AuditRegressionTest.MANAGER);
        final Program program;
        final DataValue[] values;
        final Stats execution = new Stats();

        Run(Program program) throws Exception {
            this.program = program;
            values = program.getStateSchema()
                .getDeclarations()
                .stream()
                .map(declaration -> declaration.getDefaultValue())
                .toArray(DataValue[]::new);
        }

        void tick(long now, int sample) {
            fixture.world.time = now;
            ProgramContext context = new ProgramContext(fixture.world, manager, network, fixture.labels, now, false);
            context.setStateAccess(this);
            int blockReads = fixture.world.blockReads;
            long beforeBytes = ALLOCATION.read(), before = System.nanoTime();
            program.tick(context);
            long elapsed = System.nanoTime() - before, bytes = ALLOCATION.delta(beforeBytes);
            if (sample >= 0) execution.add(sample, elapsed, bytes);
            assertEquals(blockReads, fixture.world.blockReads, "Numeric useful work must not query world providers");
            assertEquals(0, AuditRegressionTest.count(fixture.source));
            assertEquals(0, AuditRegressionTest.count(fixture.dest));
            assertEquals(
                0,
                manager.getProgramExecution(program)
                    .sleepingCount(),
                "WAIT 0 must finish this scheduled invocation");
        }

        @Override
        public DataValue read(int slot) {
            return values[slot];
        }

        @Override
        public void write(int slot, DataValue value) {
            values[slot] = value;
        }
    }

    private static final class Stats {

        final long[] nanos = new long[SAMPLES], bytes = new long[SAMPLES];

        void add(int sample, long elapsed, long allocation) {
            nanos[sample] = elapsed;
            bytes[sample] = allocation;
        }

        void print(String phase, String variant) {
            long[] sortedNanos = nanos.clone(), sortedBytes = bytes.clone();
            Arrays.sort(sortedNanos);
            Arrays.sort(sortedBytes);
            System.out.printf(
                Locale.ROOT,
                "LIBRARY_CHAR phase=%s variant=%s medianNs=%d maxNs=%d "
                    + "medianAllocatedBytes=%d maxAllocatedBytes=%d warmup=%d samples=%d%n",
                phase,
                variant,
                median(sortedNanos),
                sortedNanos[SAMPLES - 1],
                median(sortedBytes),
                sortedBytes[SAMPLES - 1],
                WARMUP,
                SAMPLES);
        }

        private static long median(long[] sorted) {
            return sorted[SAMPLES / 2 - 1] + (sorted[SAMPLES / 2] - sorted[SAMPLES / 2 - 1]) / 2;
        }
    }

    private static final class AllocationMeter {

        final ThreadMXBean bean;
        final boolean supported;

        AllocationMeter() {
            java.lang.management.ThreadMXBean base = ManagementFactory.getThreadMXBean();
            bean = base instanceof ThreadMXBean ? (ThreadMXBean) base : null;
            boolean available = bean != null && bean.isThreadAllocatedMemorySupported();
            if (available && !bean.isThreadAllocatedMemoryEnabled()) {
                try {
                    bean.setThreadAllocatedMemoryEnabled(true);
                } catch (UnsupportedOperationException | SecurityException unavailable) {
                    available = false;
                }
            }
            supported = available;
        }

        long read() {
            return supported ? bean.getThreadAllocatedBytes(
                Thread.currentThread()
                    .getId())
                : -1;
        }

        long delta(long before) {
            long after = read();
            return before < 0 || after < 0 ? -1 : after - before;
        }
    }
}
