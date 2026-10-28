package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import ca.teamdman.sfm.common.block.BlockCable;
import ca.teamdman.sfm.common.diagnostics.ManagerTickProfiler;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;
import cpw.mods.fml.common.gameevent.TickEvent;

/** Measures actual manager updates; fixture reset and verification stay outside the timer. */
@EnabledIfSystemProperty(named = "sfm.auditBenchmark", matches = "true")
class ManagerUpdateBenchmark {

    @Test
    void completeUpdateScenarios() throws Exception {
        try {
            run("idle", 100, 54, 0, false);
            run("paused", 100, 1000, 0, false);
            run("active", 100, 1000, 0, false);
            run("blocked", 100, 1000, 0, false);
            run("active", 1, 10000, 0, false);
            run("cold", 1, 54, 1000, false);
            run("active", 100, 1000, 0, true);
        } finally {
            ManagerTickProfiler.INSTANCE.reset();
        }
    }

    private void run(String mode, int count, int slots, int cables, boolean profile) throws Exception {
        List<AuditRegressionTest.Fixture> fixtures = new ArrayList<>();
        List<TileEntityManager> managers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            AuditRegressionTest.Fixture f = new AuditRegressionTest.Fixture();
            f.world.tiles.put(AuditRegressionTest.SRC, new AuditRegressionTest.InventoryTile(slots));
            if (cables > 0) {
                BlockCable cable = new BlockCable();
                for (int z = 1; z <= cables; z++) f.world.blocks.put(new BlockPos(0, 64, z), cable);
            }
            TileEntityManager manager = new TileEntityManager();
            manager.setWorldObj(f.world);
            manager.yCoord = 64;
            if (!"idle".equals(mode)) {
                ItemStack disk = new ItemStack(new ItemDisk());
                ItemDisk.setProgramString(disk, "EVERY 1 TICKS DO INPUT FROM source OUTPUT 1 TO dest END");
                LabelPositionHolder.saveTo(disk, f.labels);
                manager.setInventorySlotContents(0, disk);
            }
            if ("paused".equals(mode)) manager.setRunning(false);
            fixtures.add(f);
            managers.add(manager);
        }
        ManagerTickProfiler profiler = ManagerTickProfiler.INSTANCE;
        profiler.reset();
        profiler.setEnabled(profile);
        long[] times = new long[40];
        boolean moves = "active".equals(mode) || "cold".equals(mode);
        for (int iteration = -10; iteration < times.length; iteration++) {
            for (int i = 0; i < count; i++) {
                AuditRegressionTest.Fixture f = fixtures.get(i);
                AuditRegressionTest.InventoryTile source = (AuditRegressionTest.InventoryTile) f.world.tiles
                    .get(AuditRegressionTest.SRC);
                for (int slot = 0; slot < slots; slot++)
                    source.setInventorySlotContents(slot, new ItemStack(AuditRegressionTest.ITEM, 64));
                for (int slot = 0; slot < 3; slot++) f.dest.setInventorySlotContents(
                    slot,
                    "blocked".equals(mode) ? new ItemStack(AuditRegressionTest.OTHER, 64) : null);
                f.world.time++;
                if ("cold".equals(mode)) managers.get(i)
                    .onChunkUnload();
            }
            profiler.onServerTick(new TickEvent.ServerTickEvent(TickEvent.Phase.START));
            long start = System.nanoTime();
            for (TileEntityManager manager : managers) manager.updateEntity();
            long elapsed = System.nanoTime() - start;
            profiler.onServerTick(new TickEvent.ServerTickEvent(TickEvent.Phase.END));
            for (AuditRegressionTest.Fixture f : fixtures) {
                int moved = moves ? 1 : 0;
                int initialDest = "blocked".equals(mode) ? 192 : 0;
                assertEquals(initialDest + moved, AuditRegressionTest.count(f.dest));
                assertEquals(
                    slots * 64 - moved,
                    AuditRegressionTest
                        .count((AuditRegressionTest.InventoryTile) f.world.tiles.get(AuditRegressionTest.SRC)));
            }
            if (iteration >= 0) times[iteration] = elapsed;
        }
        if (profile) {
            assertEquals(50, profiler.snapshot().ticks);
            assertEquals(50L * count, profiler.snapshot().updates);
        }
        Arrays.sort(times);
        System.out.printf(
            Locale.ROOT,
            "MANAGER_UPDATE mode=%s managers=%d slots=%d cables=%d profile=%s samples=40 warmup=10 movedPerSample=%d p50Ms=%.3f p95Ms=%.3f maxMs=%.3f%n",
            mode,
            count,
            slots,
            cables,
            profile,
            moves ? count : 0,
            times[20] / 1e6,
            times[37] / 1e6,
            times[39] / 1e6);
    }
}
