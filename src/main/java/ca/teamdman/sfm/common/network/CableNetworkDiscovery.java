package ca.teamdman.sfm.common.network;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import ca.teamdman.sfm.common.block.BlockCable;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfm.compat.Compat;
import ca.teamdman.sfm.compat.stevefm.SteveFMCompat;

/**
 * Incremental, read-only discovery for cable tools. A cable scan finds its component and candidate Managers; a
 * separately selected Manager scan includes all its adjacent cable branches. Managers never bridge components.
 * Snapshots contain coordinates only, never inventories or program data, and grant no authority to edit a disk.
 */
public final class CableNetworkDiscovery {

    public static final int POSITIONS_PER_TICK = 128;
    public static final int MAX_POSITIONS = 32768;

    public enum Status {
        SCANNING,
        COMPLETE,
        STALE,
        LIMIT,
        UNAVAILABLE
    }

    public static final class Snapshot {

        public final Set<BlockPos> cables, peripherals, managers, unloaded;
        public final long revision;

        private Snapshot(CableNetworkDiscovery discovery) {
            cables = immutable(discovery.cables);
            peripherals = immutable(discovery.peripherals);
            managers = immutable(discovery.managers);
            unloaded = immutable(discovery.unloaded);
            revision = discovery.revision;
        }

        private static Set<BlockPos> immutable(Set<BlockPos> positions) {
            return Collections.unmodifiableSet(new LinkedHashSet<>(positions));
        }
    }

    private final World world;
    private final BlockPos root;
    private final boolean managerRoot;
    private final long revision;
    private final Queue<BlockPos> pending = new ArrayDeque<>();
    private final Set<BlockPos> seen = new HashSet<>();
    private final Set<BlockPos> cables = new LinkedHashSet<>(), peripherals = new LinkedHashSet<>(),
        managers = new LinkedHashSet<>(), unloaded = new LinkedHashSet<>();
    private Status status = Status.SCANNING;
    private long lastAdvance;
    private boolean advanced;
    private int inspected;
    private Snapshot snapshot;

    private CableNetworkDiscovery(World world, BlockPos root, boolean managerRoot) {
        requireServer(world);
        if (!valid(root)) throw new IllegalArgumentException("Invalid network discovery origin");
        this.world = world;
        this.root = root;
        this.managerRoot = managerRoot;
        revision = CableNetwork.getWorldRevision(world);
        enqueue(root);
    }

    public static CableNetworkDiscovery fromCable(World world, BlockPos position) {
        return new CableNetworkDiscovery(world, position, false);
    }

    public static CableNetworkDiscovery fromManager(World world, BlockPos position) {
        return new CableNetworkDiscovery(world, position, true);
    }

    /** At most one bounded batch per world tick, even if a UI calls repeatedly. No chunk or provider is opened. */
    public Status advance() {
        requireServer(world);
        if (getStatus() != Status.SCANNING) return status;
        long now = world.getTotalWorldTime();
        if (advanced && now == lastAdvance) return status;
        if (advanced && now < lastAdvance) return finish(Status.STALE);
        advanced = true;
        lastAdvance = now;
        for (int count = 0; count < POSITIONS_PER_TICK && !pending.isEmpty(); count++) {
            BlockPos position = pending.remove();
            inspected++;
            if (!position.isLoaded(world)) {
                if (position.equals(root)) return finish(Status.UNAVAILABLE);
                unloaded.add(position);
                continue;
            }
            Block block = position.getBlock(world);
            TileEntity tile = position.getTileEntity(world);
            boolean manager = tile instanceof TileEntityManager && !tile.isInvalid();
            boolean cable = block instanceof BlockCable
                || Compat.isSFMLoaded() && SteveFMCompat.isCable(block, position.getBlockMetadata(world));
            boolean origin = position.equals(root);
            if (origin && (managerRoot ? !manager : !(block instanceof BlockCable))) return finish(Status.UNAVAILABLE);
            if (manager) managers.add(position);
            if (cable) cables.add(position);
            if (tile != null && !tile.isInvalid() && !(origin && managerRoot)) peripherals.add(position);
            if (cable || origin && managerRoot) {
                for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
                    if (!enqueue(position.offset(side))) return finish(Status.LIMIT);
                }
            }
        }
        if (getStatus() != Status.SCANNING) return status;
        return pending.isEmpty() ? finish(Status.COMPLETE) : status;
    }

    /** Broad existing topology/chunk invalidation is conservative; callers must explicitly refresh stale scans. */
    public Status getStatus() {
        requireServer(world);
        if ((status == Status.SCANNING || status == Status.COMPLETE)
            && revision != CableNetwork.getWorldRevision(world)) finish(Status.STALE);
        return status;
    }

    public int getInspectedPositions() {
        return inspected;
    }

    public Snapshot getSnapshot() {
        if (getStatus() != Status.COMPLETE) throw new IllegalStateException("Network discovery is " + status);
        return snapshot;
    }

    private boolean enqueue(BlockPos position) {
        if (!valid(position) || seen.contains(position)) return true;
        if (seen.size() == MAX_POSITIONS) return false;
        seen.add(position);
        pending.add(position);
        return true;
    }

    private Status finish(Status result) {
        status = result;
        snapshot = result == Status.COMPLETE ? new Snapshot(this) : null;
        pending.clear();
        seen.clear();
        cables.clear();
        peripherals.clear();
        managers.clear();
        unloaded.clear();
        return result;
    }

    private static boolean valid(BlockPos position) {
        return position != null && position.y >= 0
            && position.y < 256
            && position.x >= -30000000
            && position.x < 30000000
            && position.z >= -30000000
            && position.z < 30000000;
    }

    private static void requireServer(World world) {
        if (world == null || world.isRemote || !ServerUiTasks.isServerThread())
            throw new IllegalStateException("Network discovery requires the server thread");
    }
}
