package ca.teamdman.sfm.perf;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class BenchmarkPlanTest {

    private static final int[][] DIRECTIONS = { { 1, 0, 0 }, { -1, 0, 0 }, { 0, 1, 0 }, { 0, -1, 0 }, { 0, 0, 1 },
        { 0, 0, -1 } };

    @Test
    void matrixHasEqualCableBudgetsConnectedComponentsAndDisjointInventories() {
        assertEquals(
            23,
            BenchmarkPlan.matrix()
                .size());
        for (BenchmarkPlan plan : BenchmarkPlan.matrix()) {
            assertEquals(16 * plan.managers, plan.cables.size(), plan.name());
            assertEquals(21 * plan.managers, plan.positions.size(), plan.name());
            Set<BlockPos> remaining = new HashSet<>(plan.cables);
            int components = 0;
            while (!remaining.isEmpty()) {
                components++;
                Set<BlockPos> component = new HashSet<>();
                ArrayDeque<BlockPos> queue = new ArrayDeque<>();
                queue.add(
                    remaining.iterator()
                        .next());
                while (!queue.isEmpty()) {
                    BlockPos pos = queue.remove();
                    if (!remaining.remove(pos)) continue;
                    component.add(pos);
                    for (int[] d : DIRECTIONS) queue.add(pos.offset(d[0], d[1], d[2]));
                }
                assertEquals(plan.shared ? 16 * plan.managers : 16, component.size());
            }
            assertEquals(plan.managers == 0 ? 0 : plan.shared ? 1 : plan.managers, components);
            Set<BlockPos> chests = new HashSet<>();
            for (BlockPos origin : plan.origins) {
                assertTrue(plan.cables.contains(origin.offset(0, 0, 1)));
                for (BlockPos chest : BenchmarkPlan.chests(origin)) {
                    assertTrue(chests.add(chest));
                    for (int[] d : new int[][] { { 1, 0, 0 }, { -1, 0, 0 }, { 0, 0, 1 }, { 0, 0, -1 } }) {
                        assertFalse(chests.contains(chest.offset(d[0], d[1], d[2])), "Double chest");
                    }
                }
            }
            for (BlockPos pos : plan.positions) assertTrue(pos.y >= 223 && pos.y < 256);
        }
    }

    @Test
    void everyFixtureProgramCompiles() {
        for (BenchmarkPlan plan : BenchmarkPlan.matrix()) {
            ProgramBuildResult result = new ProgramBuilder(plan.program()).build();
            assertTrue(result.isSuccess(), plan.name() + ": " + result.getErrorMessage());
        }
    }

    @Test
    void stockCoversStandardRunAndExpectedExecutionsRespectIntervals() {
        for (BenchmarkPlan plan : BenchmarkPlan.matrix()) {
            assertTrue(plan.completed(1400) <= BenchmarkPlan.STOCK);
            assertEquals(0, plan.completed(0));
            assertEquals(1, plan.completed(plan.interval));
            assertTrue(
                plan.program()
                    .contains("FORGET"));
        }
        BenchmarkPlan slow = new BenchmarkPlan(100, true, "long_interval", 1200);
        assertEquals(0, slow.completed(1199));
        assertEquals(1, slow.completed(1200));
        assertEquals(2, slow.completed(2400));
    }
}
