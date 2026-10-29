package ca.teamdman.sfm.common.program;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.tileentity.TileEntityRedstonePort;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.ast.Label;
import ca.teamdman.sfml.ast.NamedRedstoneTrigger;
import ca.teamdman.sfml.data.QueryBudget;

/** Manager-owned, heap-only baselines. Reading an edge never consumes it for another controller. */
public final class RedstoneTriggerTracker {

    private Map<BlockPos, Observation> previous = Collections.emptyMap();
    private World world;
    private CableNetwork network;
    private long topology;

    private static final class Selection {

        final Set<BlockPos> positions = new LinkedHashSet<>();
        boolean complete = true;
    }

    private static final class Observation {

        final TileEntityRedstonePort port;
        final long epoch;
        final int mask, available;
        final long[] rising = new long[6], falling = new long[6];

        Observation(TileEntityRedstonePort port, int mask, QueryBudget budget) {
            this.port = port;
            this.mask = mask;
            epoch = port.getSignalEpoch();
            int valid = 0;
            for (int side = 0; side < 6; side++) {
                if ((mask & 1 << side) == 0) continue;
                budget.chargeProvider();
                if (!port.isInputAvailable(side)) continue;
                valid |= 1 << side;
                rising[side] = port.getRisingCount(side);
                falling[side] = port.getFallingCount(side);
            }
            available = valid;
        }
    }

    public void reset() {
        previous = Collections.emptyMap();
        world = null;
        network = null;
    }

    /** Transactionally capture all eligibility, then publish baselines before any program action. */
    public Map<NamedRedstoneTrigger, Boolean> capture(ProgramContext context, List<NamedRedstoneTrigger> triggers,
        QueryBudget budget) {
        World currentWorld = context.getWorld();
        CableNetwork currentNetwork = context.getNetwork();
        if (currentWorld == null || currentWorld.isRemote || currentNetwork == null) {
            reset();
            return Collections.emptyMap();
        }
        if (net.minecraft.server.MinecraftServer.getServer() != null
            && !ca.teamdman.sfm.common.net.ServerUiTasks.isServerThread())
            throw new IllegalArgumentException("Redstone trigger observation requires the server thread");
        long generation = currentNetwork.getTopologyGeneration();
        boolean rebase = world != currentWorld || network != currentNetwork || topology != generation;
        Map<BlockPos, Observation> old = rebase ? Collections.emptyMap() : previous;
        Map<NamedRedstoneTrigger, Selection> selections = new IdentityHashMap<>();
        Map<BlockPos, Integer> requested = new LinkedHashMap<>();
        for (NamedRedstoneTrigger trigger : triggers) {
            budget.chargeAllocation(1);
            Selection selection = new Selection();
            for (Label label : trigger.getSource()
                .getLabels()) {
                Set<BlockPos> positions = context.getLabelPositionHolder()
                    .getPositions(label.getName());
                if (positions.isEmpty()) selection.complete = false;
                for (BlockPos pos : positions) {
                    budget.chargePosition();
                    if (selection.positions.add(pos)) budget.chargeAllocation(1);
                    Integer mask = requested.get(pos);
                    if (mask == null) budget.chargeAllocation(1);
                    requested.put(pos, (mask == null ? 0 : mask) | trigger.getSideMask());
                }
            }
            selections.put(trigger, selection);
        }

        Map<BlockPos, Observation> sampled = new HashMap<>();
        for (Map.Entry<BlockPos, Integer> entry : requested.entrySet()) {
            BlockPos pos = entry.getKey();
            budget.chargeProvider();
            if (!currentNetwork.isConnected(pos)) continue;
            budget.chargeProvider();
            if (!pos.isLoaded(currentWorld)) continue;
            budget.chargeProvider();
            TileEntity tile = currentWorld.getTileEntity(pos.x, pos.y, pos.z);
            if (!(tile instanceof TileEntityRedstonePort)) continue;
            TileEntityRedstonePort port = (TileEntityRedstonePort) tile;
            // Ordinary input notifications synchronously maintain counters. Resampling
            // stable ports here would turn every subscribed input into a six-face poll.
            if (rebase || !old.containsKey(pos) || !port.isInputInitialized()) refresh(port, budget);
            if (!stillPresent(currentWorld, currentNetwork, pos, port, budget)) continue;
            budget.chargeAllocation(13);
            sampled.put(pos, new Observation(port, entry.getValue(), budget));
        }

        // Keep idle subscriptions cheap, but revalidate the whole group before delivering
        // a cached edge. An unavailable member must never produce a partial-group event.
        Set<BlockPos> verified = new LinkedHashSet<>();
        ArrayDeque<BlockPos> verify = new ArrayDeque<>();
        for (NamedRedstoneTrigger trigger : triggers) {
            Selection selection = selections.get(trigger);
            if (!selection.complete) continue;
            boolean edge = false;
            for (BlockPos pos : selection.positions)
                edge |= changed(old.get(pos), sampled.get(pos), trigger.getSideMask(), trigger.isFalling());
            if (edge) for (BlockPos pos : selection.positions) if (verified.add(pos)) {
                budget.chargeAllocation(1);
                verify.addLast(pos);
            }
        }
        while (!verify.isEmpty()) {
            BlockPos pos = verify.removeFirst();
            Observation now = sampled.get(pos);
            if (now == null) continue;
            if (!stillPresent(currentWorld, currentNetwork, pos, now.port, budget)) {
                sampled.remove(pos);
                continue;
            }
            refresh(now.port, budget);
            if (!stillPresent(currentWorld, currentNetwork, pos, now.port, budget)) {
                sampled.remove(pos);
                continue;
            }
            budget.chargeAllocation(13);
            sampled.put(pos, new Observation(now.port, now.mask, budget));
            // Refresh can discover a missed edge on another selected face. Close over
            // overlapping groups before publishing any new eligibility.
            for (NamedRedstoneTrigger trigger : triggers) {
                Selection selection = selections.get(trigger);
                if (!selection.complete || !selection.positions.contains(pos)
                    || !changed(old.get(pos), sampled.get(pos), trigger.getSideMask(), trigger.isFalling())) continue;
                for (BlockPos member : selection.positions) if (verified.add(member)) {
                    budget.chargeAllocation(1);
                    verify.addLast(member);
                }
            }
        }

        // Provider callbacks can synchronously edit topology. Do not publish an edge
        // across such a gap; retain the starting generation so the next capture rebases.
        boolean stableTopology = generation == currentNetwork.getTopologyGeneration();
        Map<NamedRedstoneTrigger, Boolean> eligible = new IdentityHashMap<>();
        for (NamedRedstoneTrigger trigger : triggers) {
            Selection selection = selections.get(trigger);
            boolean complete = selection.complete && !selection.positions.isEmpty(), edge = false;
            int mask = trigger.getSideMask();
            for (BlockPos pos : selection.positions) {
                Observation now = sampled.get(pos), before = old.get(pos);
                if (now == null || (now.available & mask) != mask) {
                    complete = false;
                    continue;
                }
                edge |= changed(before, now, mask, trigger.isFalling());
            }
            eligible.put(trigger, stableTopology && complete && edge);
        }
        previous = sampled;
        world = currentWorld;
        network = currentNetwork;
        topology = generation;
        return Collections.unmodifiableMap(eligible);
    }

    private static boolean changed(Observation before, Observation now, int mask, boolean falling) {
        if (before == null || now == null || before.port != now.port || before.epoch != now.epoch) return false;
        int comparable = mask & before.mask & before.available & now.available;
        for (int side = 0; side < 6; side++) {
            if ((comparable & 1 << side) != 0
                && (falling ? before.falling[side] != now.falling[side] : before.rising[side] != now.rising[side]))
                return true;
        }
        return false;
    }

    private static void refresh(TileEntityRedstonePort port, QueryBudget budget) {
        try {
            port.refreshInputs(budget::chargeProvider);
        } catch (RuntimeException error) {
            throw new IllegalArgumentException("Native redstone provider failed: " + error.getMessage(), error);
        }
    }

    private static boolean stillPresent(World world, CableNetwork network, BlockPos pos, TileEntityRedstonePort port,
        QueryBudget budget) {
        budget.chargeProvider();
        if (!pos.isLoaded(world) || port.isInvalid()) return false;
        budget.chargeProvider();
        if (world.getTileEntity(pos.x, pos.y, pos.z) != port) return false;
        budget.chargeProvider();
        return network.isConnected(pos);
    }
}
