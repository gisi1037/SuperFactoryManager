package ca.teamdman.sfm.common.program;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;

import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfm.compat.gregtech.GregTechCircuitAdapter;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;

/** Bounded machine configuration action. A group preflight failure changes no machine. */
public final class GhostCircuitService {

    public static final class Result {

        public final int matched, changed;
        public final String status;

        Result(int matched, int changed, String status) {
            this.matched = matched;
            this.changed = changed;
            this.status = status;
        }
    }

    private final ProgramContext context;
    private final GhostCircuitAccess.Adapter adapter;

    public GhostCircuitService(ProgramContext context) {
        this(context, GregTechCircuitAdapter::find);
    }

    public GhostCircuitService(ProgramContext context, GhostCircuitAccess.Adapter adapter) {
        this.context = context;
        this.adapter = adapter;
    }

    public static void validate(LabelReference label, long configuration) {
        if (label == null || label.hasSlotSelection() || label.getSideMask() != 63) throw new IllegalArgumentException(
            "SETCIRCUIT requires a whole-machine label without side/slot restrictions");
        if (configuration < -1 || configuration > 24)
            throw new IllegalArgumentException("SETCIRCUIT accepts 1..24; 0 or -1 clears the ghost circuit");
    }

    public Result set(LabelReference label, long configuration) {
        validate(label, configuration);
        if (context.getWorld() == null || context.getNetwork() == null) return new Result(0, 0, "unavailable");
        if (context.getWorld().isRemote || net.minecraft.server.MinecraftServer.getServer() != null
            && !ca.teamdman.sfm.common.net.ServerUiTasks.isServerThread())
            throw new IllegalArgumentException("SETCIRCUIT requires server-thread execution");
        QueryBudget budget = context.getInvocationQueryBudget();
        Set<BlockPos> positions = context.getLabelPositionHolder()
            .getPositions(label.getName());
        if (positions.isEmpty()) return new Result(0, 0, "unavailable");
        budget.chargeAllocation(2);
        List<Endpoint> endpoints = new ArrayList<>();
        Set<TileEntity> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        Object disk = context.getManager() == null ? null
            : context.getManager()
                .getDisk();
        long generation = context.getManager() == null ? 0
            : context.getManager()
                .getDiskGeneration();
        for (BlockPos position : positions) {
            TileEntity tile = tile(position, budget);
            if (tile == null) return new Result(0, 0, "unavailable");
            if (!seen.add(tile)) continue;
            budget.chargeProvider();
            GhostCircuitAccess access = adapter.find(tile, budget);
            if (access == null) return new Result(0, 0, "unsupported");
            budget.chargeAllocation(3);
            endpoints.add(new Endpoint(position, tile, access));
        }
        int desired = configuration <= 0 ? -1 : (int) configuration;
        int matched = 0, changed = 0;
        for (Endpoint endpoint : endpoints) {
            try (QueryBudget.Reservation reservation = budget.reserve(16, 96, 64, 64)) {
                QueryBudget commit = reservation.budget();
                if (!authority(disk, generation, commit) || !current(endpoint, commit))
                    return new Result(matched, changed, changed > 0 ? "partial" : "unavailable");
                commit.chargeProvider();
                int before = endpoint.access.get(commit);
                if (before != desired) {
                    if (!authority(disk, generation, commit) || !current(endpoint, commit))
                        return new Result(matched, changed, changed > 0 ? "partial" : "unavailable");
                    commit.chargeProvider();
                    endpoint.access.set(desired, commit);
                    context.setDidSomething(true);
                    if (!authority(disk, generation, commit) || !current(endpoint, commit))
                        return new Result(matched, changed, "partial");
                    commit.chargeProvider();
                    if (endpoint.access.get(commit) != desired) return new Result(matched, changed, "partial");
                    changed++;
                }
                matched++;
            }
        }
        return new Result(matched, changed, "complete");
    }

    private boolean authority(Object disk, long generation, QueryBudget budget) {
        if (context.getManager() == null) return true;
        if (context.getManager()
            .getDisk() != disk
            || context.getManager()
                .getDiskGeneration() != generation
            || context.getManager()
                .hasPendingRecovery()
            || context.getManager()
                .isInvalid()
            || context.getManager()
                .getWorldObj() != context.getWorld())
            return false;
        BlockPos position = context.getManager()
            .getBlockPos();
        budget.chargePosition();
        budget.chargeProviders(2);
        return position.isLoaded(context.getWorld()) && context.getWorld()
            .getTileEntity(position.x, position.y, position.z) == context.getManager();
    }

    private boolean current(Endpoint endpoint, QueryBudget budget) {
        if (tile(endpoint.position, budget) != endpoint.tile) return false;
        budget.chargeProvider();
        boolean current = endpoint.access.current(endpoint.tile, budget);
        return current && tile(endpoint.position, budget) == endpoint.tile;
    }

    private TileEntity tile(BlockPos position, QueryBudget budget) {
        budget.chargePosition();
        budget.chargeProviders(3);
        TileEntity tile = context.getNetwork()
            .isConnected(position) && position.isLoaded(context.getWorld()) ? context.getWorld()
                .getTileEntity(position.x, position.y, position.z) : null;
        return tile != null && !tile.isInvalid() ? tile : null;
    }

    private static final class Endpoint {

        final BlockPos position;
        final TileEntity tile;
        final GhostCircuitAccess access;

        Endpoint(BlockPos position, TileEntity tile, GhostCircuitAccess access) {
            this.position = position;
            this.tile = tile;
            this.access = access;
        }
    }
}
