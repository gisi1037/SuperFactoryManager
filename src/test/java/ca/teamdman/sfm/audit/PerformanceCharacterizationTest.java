package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import com.sun.management.ThreadMXBean;

import ca.teamdman.sfm.common.block.BlockCable;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.ast.Program;

/** Diagnostic evidence only: counts useful work and current-thread allocation; no timing thresholds. */
@EnabledIfSystemProperty(named = "sfm.auditBenchmark", matches = "true")
class PerformanceCharacterizationTest {

    private static final int WARMUP = 8;
    private static final int SAMPLES = 24;
    private static final long SEED = Long.parseLong(System.getProperty("sfm.characterizationSeed", "1037"));
    private static final ThreadMXBean MEMORY = (ThreadMXBean) ManagementFactory.getThreadMXBean();

    private static long allocated() {
        assertTrue(MEMORY.isThreadAllocatedMemorySupported(), "Pinned HotSpot allocation counter required");
        if (!MEMORY.isThreadAllocatedMemoryEnabled()) MEMORY.setThreadAllocatedMemoryEnabled(true);
        return MEMORY.getThreadAllocatedBytes(
            Thread.currentThread()
                .getId());
    }

    @Test
    void transferWork() throws Exception {
        System.out.printf(
            Locale.ROOT,
            "CHAR_ENV java=%s vm=%s heap=%d processors=%d warmup=%d samples=%d%n",
            System.getProperty("java.version"),
            System.getProperty("java.vm.name"),
            Runtime.getRuntime()
                .maxMemory(),
            Runtime.getRuntime()
                .availableProcessors(),
            WARMUP,
            SAMPLES);
        System.out.println("CHAR_SEED " + SEED);
        List<int[]> scenarios = new ArrayList<>(
            Arrays.asList(
                new int[][] { { 0, 54, 27, 1 }, { 1, 54, 27, 1 }, { 0, 1000, 27, 1 }, { 1, 1000, 27, 1 },
                    { 0, 1000, 1000, 1 }, { 1, 1000, 1000, 1 }, { 2, 1000, 1000, 1 }, { 3, 1000, 1000, 1 },
                    { 0, 1000, 27, 20 }, { 1, 1000, 27, 20 }, { 0, 1000, 27, 200 }, { 4, 1000, 27, 1 },
                    { 5, 1000, 27, 1 } }));
        Collections.shuffle(scenarios, new Random(SEED));
        String[] modes = { "active", "blocked", "empty", "sparse", "fullCompatible", "ruleRejected" };
        for (int[] scenario : scenarios) transfer(modes[scenario[0]], scenario[1], scenario[2], scenario[3]);
    }

    private void transfer(String mode, int sourceSlots, int destinationSlots, int outputs) throws Exception {
        Fixture f = new Fixture();
        CountingInventory source = new CountingInventory(sourceSlots);
        CountingInventory destination = new CountingInventory(destinationSlots);
        f.world.tiles.put(SRC, source);
        f.world.tiles.put(DST, destination);
        CableNetwork network = new CableNetwork(f.world, MANAGER);
        StringBuilder text = new StringBuilder("EVERY 1 TICKS DO INPUT FROM source ");
        for (int i = 0; i < outputs; i++) text.append("OUTPUT 1 TO dest ");
        Program program = parse(
            text.append("END")
                .toString());
        Metrics measurements = new Metrics();
        boolean fullCompatible = "fullCompatible".equals(mode);
        boolean blocked = "blocked".equals(mode) || fullCompatible;
        boolean rejected = "ruleRejected".equals(mode), empty = "empty".equals(mode), sparse = "sparse".equals(mode);
        destination.reject = rejected;
        int initial = empty ? 0 : sparse ? 64 : sourceSlots * 64;
        int moved = blocked || rejected || empty ? 0 : outputs;
        for (int i = -WARMUP; i < SAMPLES; i++) {
            for (int slot = 0; slot < sourceSlots; slot++) source.setInventorySlotContents(
                slot,
                empty || (sparse && slot != sourceSlots - 1) ? null : new ItemStack(ITEM, 64));
            for (int slot = 0; slot < destinationSlots; slot++) destination
                .setInventorySlotContents(slot, blocked ? new ItemStack(fullCompatible ? ITEM : OTHER, 64) : null);
            source.reads = destination.reads = destination.rules = 0;
            long bytes = allocated(), start = System.nanoTime();
            program.tick(new ProgramContext(f.world, null, network, f.labels, 1, false));
            long nanos = System.nanoTime() - start, allocation = allocated() - bytes;
            if (i >= 0) measurements.add(i, nanos, allocation, source.reads, destination.reads, destination.rules);
            assertEquals(initial - moved, count(source));
            assertEquals((blocked ? destinationSlots * 64 : 0) + moved, count(destination));
            for (int slot = 0; slot < sourceSlots; slot++) {
                ItemStack stack = source.getStackInSlot(slot);
                if (stack != null) assertSame(ITEM, stack.getItem());
            }
            for (int slot = 0; slot < destinationSlots; slot++) {
                ItemStack stack = destination.getStackInSlot(slot);
                if (stack != null) assertSame(blocked && !fullCompatible ? OTHER : ITEM, stack.getItem());
            }
            if (moved > 0) assertEquals(Math.min(moved, 64), destination.getStackInSlot(0).stackSize);
        }
        measurements.print(
            "transfer mode=" + mode
                + " sourceSlots="
                + sourceSlots
                + " destinationSlots="
                + destinationSlots
                + " outputs="
                + outputs
                + " moved="
                + moved);
    }

    @Test
    void topologyWork() throws Exception {
        for (int managers : new int[] { 1, 10, 100 }) {
            FakeWorld world = world();
            BlockCable cable = new BlockCable();
            for (int z = 1; z <= 1000; z++) world.blocks.put(new BlockPos(0, 64, z), cable);
            List<CableNetwork> networks = new ArrayList<>();
            Metrics cold = new Metrics(), expired = new Metrics(), invalidated = new Metrics();
            for (int i = -WARMUP; i < SAMPLES; i++) {
                networks.clear();
                world.blockReads = 0;
                long bytes = allocated(), start = System.nanoTime();
                for (int m = 0; m < managers; m++)
                    networks.add(new CableNetwork(world, new BlockPos(1, 64, 1 + m * (1000 / managers))));
                long nanos = System.nanoTime() - start, allocation = allocated() - bytes;
                if (i >= 0) cold.add(i, nanos, allocation, world.blockReads, 0, 0);
                world.blockReads = 0;
                for (CableNetwork network : networks) assertEquals(
                    1000,
                    network.getCables()
                        .size());
                assertEquals(0, world.blockReads, "Warm topology queries should reuse the existing scan");
                world.time += 21;
                measureNetworks(networks, world, expired, i);
                // This is the same global invalidation API used for unrelated block/chunk changes.
                CableNetwork.invalidate(world);
                measureNetworks(networks, world, invalidated, i);
            }
            cold.print("topology mode=cold sharedCables=1000 managers=" + managers);
            expired.print("topology mode=expiry sharedCables=1000 managers=" + managers);
            invalidated.print("topology mode=globalInvalidation sharedCables=1000 managers=" + managers);
        }
    }

    private void measureNetworks(List<CableNetwork> networks, FakeWorld world, Metrics measurements, int index) {
        world.blockReads = 0;
        long bytes = allocated(), start = System.nanoTime();
        int memberships = 0;
        for (CableNetwork network : networks) memberships += network.getCables()
            .size();
        long nanos = System.nanoTime() - start, allocation = allocated() - bytes;
        int reads = world.blockReads;
        assertEquals(1000 * networks.size(), memberships);
        for (CableNetwork network : networks) for (BlockPos position : network.getCables()) {
            assertEquals(0, position.x);
            assertEquals(64, position.y);
            assertTrue(position.z >= 1 && position.z <= 1000);
        }
        if (index >= 0) measurements.add(index, nanos, allocation, reads, 0, 0);
    }

    @Test
    void completeManagerWork() throws Exception {
        managers(false, 2);
        managers(false, 1000);
        managers(true, 2);
        managers(true, 1000);
    }

    private void managers(boolean due, int labelCount) throws Exception {
        List<Fixture> fixtures = new ArrayList<>();
        List<TileEntityManager> managers = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Fixture f = new Fixture();
            LabelPositionHolder labels = f.labels;
            for (int label = 2; label < labelCount; label++)
                labels.addPosition("unused" + label, new BlockPos(label + 100, 64, 10));
            TileEntityManager manager = new TileEntityManager();
            manager.setWorldObj(f.world);
            manager.yCoord = 64;
            ItemStack disk = new ItemStack(new ItemDisk());
            ItemDisk.setProgramString(
                disk,
                "EVERY " + (due ? 1 : 1200) + " TICKS DO INPUT FROM source OUTPUT 1 TO dest END");
            LabelPositionHolder.saveTo(disk, labels);
            manager.setInventorySlotContents(0, disk);
            manager.updateEntity(); // Compilation and initial discovery are excluded from this warm scenario.
            fixtures.add(f);
            managers.add(manager);
        }
        Metrics measurements = new Metrics();
        for (int i = -WARMUP; i < SAMPLES; i++) {
            for (Fixture f : fixtures) {
                f.world.time++;
                f.source.setInventorySlotContents(0, new ItemStack(ITEM, 64));
                f.dest.setInventorySlotContents(0, null);
            }
            long bytes = allocated(), start = System.nanoTime();
            for (TileEntityManager manager : managers) manager.updateEntity();
            long nanos = System.nanoTime() - start, allocation = allocated() - bytes;
            if (i >= 0) measurements.add(i, nanos, allocation, -1, -1, -1);
            for (Fixture f : fixtures) {
                assertEquals(due ? 1 : 0, count(f.dest));
                assertEquals(due ? 63 : 64, count(f.source));
            }
        }
        measurements.print("manager due=" + due + " managers=100 labels=" + labelCount + " moved=" + (due ? 100 : 0));
    }

    private static final class CountingInventory extends InventoryTile {

        long reads, rules;
        boolean reject;

        CountingInventory(int size) {
            super(size);
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            reads++;
            return super.getStackInSlot(slot);
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack) {
            rules++;
            return !reject;
        }
    }

    private static final class Metrics {

        final long[] nanos = new long[SAMPLES], bytes = new long[SAMPLES], first = new long[SAMPLES],
            second = new long[SAMPLES], third = new long[SAMPLES];

        void add(int index, long time, long allocation, long a, long b, long c) {
            nanos[index] = time;
            bytes[index] = allocation;
            first[index] = a;
            second[index] = b;
            third[index] = c;
        }

        void print(String scenario) {
            // Preserve correlated tuples before sorting marginal distributions.
            for (int i = 0; i < SAMPLES; i++) System.out.printf(
                Locale.ROOT,
                "CHAR_SAMPLE %s sample=%d nanos=%d allocatedBytes=%d first=%d second=%d third=%d%n",
                scenario,
                i,
                nanos[i],
                bytes[i],
                first[i],
                second[i],
                third[i]);
            Arrays.sort(nanos);
            Arrays.sort(bytes);
            Arrays.sort(first);
            Arrays.sort(second);
            Arrays.sort(third);
            System.out.printf(
                Locale.ROOT,
                "CHAR %s p50Ms=%.3f maxMs=%.3f allocatedBytes=%d sourceReadsOrBlockReads=%d destinationReads=%d insertionRules=%d%n",
                scenario,
                nanos[SAMPLES / 2] / 1e6,
                nanos[SAMPLES - 1] / 1e6,
                bytes[SAMPLES / 2],
                first[SAMPLES / 2],
                second[SAMPLES / 2],
                third[SAMPLES / 2]);
        }
    }
}
