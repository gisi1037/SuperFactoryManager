package ca.teamdman.sfm.perf;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ca.teamdman.sfm.common.util.BlockPos;

/** Pure fixture geometry and work expectations, shared by the in-pack runner and tests. */
public final class BenchmarkPlan {

    public static final int STOCK = 27 * 64;
    public static final int CABLES_PER_MANAGER = 16;
    public final int managers;
    public final boolean shared;
    public final String workload;
    public final int interval;
    public final List<BlockPos> origins = new ArrayList<>();
    public final Set<BlockPos> cables = new LinkedHashSet<>();
    public final Set<BlockPos> positions = new LinkedHashSet<>();

    public BenchmarkPlan(int managers, boolean shared, String workload, int interval) {
        if (managers < 0 || managers > 100 || interval < 1) throw new IllegalArgumentException("Invalid fixture size");
        this.managers = managers;
        this.shared = shared;
        this.workload = workload;
        this.interval = interval;
        for (int i = 0; i < managers; i++) {
            BlockPos origin = new BlockPos(-240 + i % 10 * 5, 224, -650 + i / 10 * 5);
            origins.add(origin);
            positions.add(origin);
            for (BlockPos chest : chests(origin)) positions.add(chest);
            if (!shared) {
                cables.add(origin.offset(0, 0, 1));
                for (int dy = 0; dy < 15; dy++) cables.add(origin.offset(0, dy, 2));
            }
        }
        if (shared && managers > 0) {
            for (int row = 0; row <= (managers - 1) / 10; row++) {
                int lastColumn = Math.min(9, managers - row * 10 - 1);
                for (int x = -243; x <= -240 + lastColumn * 5; x++) {
                    cables.add(new BlockPos(x, 224, -648 + row * 5));
                }
            }
            for (int z = -648; z <= -648 + (managers - 1) / 10 * 5; z++) {
                cables.add(new BlockPos(-243, 224, z));
            }
            for (BlockPos origin : origins) cables.add(origin.offset(0, 0, 1));
            List<BlockPos> base = new ArrayList<>(cables);
            for (int dy = 1; cables.size() < managers * CABLES_PER_MANAGER; dy++) {
                for (BlockPos pos : base) {
                    if (cables.size() == managers * CABLES_PER_MANAGER) break;
                    cables.add(pos.offset(0, dy, 0));
                }
            }
        }
        for (BlockPos cable : cables) {
            if (!positions.add(cable)) throw new IllegalStateException("Overlapping fixture blocks");
        }
    }

    public static BlockPos[] chests(BlockPos origin) {
        return new BlockPos[] { origin.offset(-1, 0, 0), origin.offset(1, 0, 0), origin.offset(0, 1, 0),
            origin.offset(0, -1, 0) };
    }

    public int completed(int ticks) {
        return ticks / interval;
    }

    public boolean primaryMoves() {
        return "active".equals(workload) || "complex".equals(workload) || "long_interval".equals(workload);
    }

    public String name() {
        return managers + "-" + (shared ? "shared" : "separate") + "-" + workload;
    }

    public String program() {
        StringBuilder output = new StringBuilder("OUTPUT ");
        if ("complex".equals(workload)) {
            for (int i = 0; i < 19; i++) output.append("1 minecraft:__sfm_perf_no_match_")
                .append(i)
                .append(", ");
            output.append("1 minecraft:cobble*stone");
        } else output.append("1");
        return "EVERY " + interval
            + " TICKS DO\n INPUT minecraft:cobblestone FROM source\n "
            + output
            + " TO dest\n FORGET\n INPUT minecraft:stick FROM witness_source\n OUTPUT 1 TO witness_dest\nEND";
    }

    public static List<BenchmarkPlan> focus() {
        List<BenchmarkPlan> result = new ArrayList<>();
        result.add(new BenchmarkPlan(0, false, "control", 1));
        result.add(new BenchmarkPlan(100, false, "active", 1));
        result.add(new BenchmarkPlan(100, true, "active", 1));
        result.add(new BenchmarkPlan(100, true, "blocked", 1));
        result.add(new BenchmarkPlan(100, true, "long_interval", 1200));
        return result;
    }

    public static List<BenchmarkPlan> matrix() {
        List<BenchmarkPlan> result = new ArrayList<>();
        result.add(new BenchmarkPlan(0, false, "control", 1));
        for (int count : new int[] { 1, 10, 100 }) {
            for (boolean shared : new boolean[] { false, true }) {
                for (String workload : new String[] { "active", "blocked", "empty" }) {
                    result.add(new BenchmarkPlan(count, shared, workload, 1));
                }
            }
        }
        for (boolean shared : new boolean[] { false, true }) result.add(new BenchmarkPlan(100, shared, "complex", 1));
        result.add(new BenchmarkPlan(100, true, "absent_resource", 1));
        result.add(new BenchmarkPlan(100, true, "long_interval", 1200));
        return result;
    }
}
