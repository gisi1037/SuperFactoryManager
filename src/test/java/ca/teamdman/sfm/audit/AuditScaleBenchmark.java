package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.ast.Program;

/** Reproducible synthetic timing experiment; no wall-clock pass/fail thresholds. */
@EnabledIfSystemProperty(named = "sfm.auditBenchmark", matches = "true")
public class AuditScaleBenchmark {

    @Test
    void scaling() throws Exception {
        System.out.println(
            "AUDIT_ENV java=" + System.getProperty("java.version")
                + " vm="
                + System.getProperty("java.vm.name")
                + " maxHeap="
                + Runtime.getRuntime()
                    .maxMemory()
                + " processors="
                + Runtime.getRuntime()
                    .availableProcessors());
        for (int[] scenario : new int[][] { { 1, 54, 1 }, { 10, 1000, 1 }, { 100, 1000, 1 }, { 1, 10000, 1 },
            { 1, 1000, 20 }, { 1, 1000, 200 } }) {
            int managers = scenario[0], slots = scenario[1], outputs = scenario[2];
            List<AuditRegressionTest.Fixture> fixtures = new ArrayList<>();
            List<CableNetwork> networks = new ArrayList<>();
            for (int i = 0; i < managers; i++) {
                AuditRegressionTest.Fixture f = new AuditRegressionTest.Fixture();
                f.world.tiles.put(AuditRegressionTest.SRC, new AuditRegressionTest.InventoryTile(slots));
                f.world.tiles.put(AuditRegressionTest.DST, new AuditRegressionTest.InventoryTile(4));
                fixtures.add(f);
                networks.add(new CableNetwork(f.world, AuditRegressionTest.MANAGER));
            }
            StringBuilder text = new StringBuilder("EVERY 20 TICKS DO INPUT FROM source ");
            for (int i = 0; i < outputs; i++) text.append("OUTPUT 1 TO dest ");
            text.append("END");
            Program program = AuditRegressionTest.parse(text.toString());
            long[] times = new long[40];
            for (int iteration = -10; iteration < times.length; iteration++) {
                for (AuditRegressionTest.Fixture f : fixtures) {
                    AuditRegressionTest.InventoryTile source = (AuditRegressionTest.InventoryTile) f.world.tiles
                        .get(AuditRegressionTest.SRC);
                    AuditRegressionTest.InventoryTile dest = (AuditRegressionTest.InventoryTile) f.world.tiles
                        .get(AuditRegressionTest.DST);
                    for (int slot = 0; slot < slots; slot++)
                        source.setInventorySlotContents(slot, new ItemStack(AuditRegressionTest.ITEM, 64));
                    for (int slot = 0; slot < 4; slot++) dest.setInventorySlotContents(slot, null);
                }
                long start = System.nanoTime();
                for (int i = 0; i < managers; i++) {
                    AuditRegressionTest.Fixture f = fixtures.get(i);
                    program.tick(new ProgramContext(f.world, null, networks.get(i), f.labels, 20, false));
                }
                long elapsed = System.nanoTime() - start;
                for (AuditRegressionTest.Fixture f : fixtures) {
                    AuditRegressionTest.InventoryTile source = (AuditRegressionTest.InventoryTile) f.world.tiles
                        .get(AuditRegressionTest.SRC);
                    AuditRegressionTest.InventoryTile dest = (AuditRegressionTest.InventoryTile) f.world.tiles
                        .get(AuditRegressionTest.DST);
                    assertEquals(outputs, AuditRegressionTest.count(dest));
                    assertEquals(slots * 64, AuditRegressionTest.count(source) + AuditRegressionTest.count(dest));
                }
                if (iteration >= 0) times[iteration] = elapsed;
            }
            Arrays.sort(times);
            System.out.printf(
                java.util.Locale.ROOT,
                "AUDIT_SCALE managers=%d slots=%d outputs=%d samples=40 warmup=10 movedPerSample=%d p50Ms=%.3f p95Ms=%.3f maxMs=%.3f%n",
                managers,
                slots,
                outputs,
                managers * outputs,
                times[20] / 1e6,
                times[37] / 1e6,
                times[39] / 1e6);
        }
    }
}
