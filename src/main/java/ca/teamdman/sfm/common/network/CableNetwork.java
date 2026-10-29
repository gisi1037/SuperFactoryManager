package ca.teamdman.sfm.common.network;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import ca.teamdman.sfm.common.block.BlockCable;
import ca.teamdman.sfm.common.diagnostics.ManagerPhaseProfiler;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfm.compat.Compat;
import ca.teamdman.sfm.compat.stevefm.SteveFMCompat;

public final class CableNetwork {

    private static final java.util.Map<World, Long> VERSIONS = new java.util.WeakHashMap<>();
    // Only networks that have observed named native edges register here. Both levels
    // are weak: the event registry must not keep managers or unloaded worlds alive.
    private static final Map<World, Map<CableNetwork, Boolean>> EDGE_NETWORKS = new WeakHashMap<>();
    private static final int SCAN_INTERVAL = 21;
    private boolean trackingTopology;
    private long topologyGeneration;
    private Set<BlockPos> topologyPositions = Collections.emptySet();
    private Set<Long> topologyChunks = Collections.emptySet();
    private long version;
    private final int scanPhase;
    private long nextScanTime;

    /** Unknown edit location: retain the conservative history reset for every subscriber. */
    public static synchronized void invalidate(World world) {
        invalidate(world, null, null);
    }

    public static synchronized void invalidate(World world, int x, int y, int z) {
        invalidate(world, new BlockPos(x, y, z), null);
    }

    public static synchronized void invalidateChunk(World world, int chunkX, int chunkZ) {
        invalidate(world, null, chunkKey(chunkX, chunkZ));
    }

    private static void invalidate(World world, BlockPos pos, Long chunk) {
        if (world == null || world.isRemote) return;
        VERSIONS.put(world, VERSIONS.getOrDefault(world, 0L) + 1);
        Map<CableNetwork, Boolean> subscribers = EDGE_NETWORKS.get(world);
        if (subscribers == null) return;
        // O(native-subscribed networks), constant-time footprint checks, no world
        // reads. Ordinary discovery-only managers do not participate in this loop.
        for (CableNetwork network : subscribers.keySet()) {
            if (pos == null && chunk == null || pos != null && network.topologyPositions.contains(pos)
                || chunk != null && network.topologyChunks.contains(chunk)) network.topologyGeneration++;
        }
    }

    private static long chunkKey(int x, int z) {
        return (long) x << 32 | z & 0xffffffffL;
    }

    private static synchronized long version(World world) {
        return VERSIONS.getOrDefault(world, 0L);
    }

    private boolean needsScan() {
        if (world == null) return false;
        long now = world.getTotalWorldTime();
        return version != version(world) || lastScanTime < 0
            || now < lastScanTime
            || (now != lastScanTime && now >= nextScanTime);
    }

    private final World world;
    private final BlockPos managerPos;
    private final Set<BlockPos> cables = new HashSet<>();
    private final Set<BlockPos> peripherals = new HashSet<>();
    private long lastScanTime = -1;

    public CableNetwork(World world, BlockPos managerPos) {
        this.world = world;
        this.managerPos = managerPos;
        scanPhase = managerPos == null ? 0 : Math.floorMod(managerPos.hashCode(), SCAN_INTERVAL);
        scan();
    }

    public void scan() {
        long start = ManagerPhaseProfiler.begin();
        int visited = 0;
        try {
            visited = scanNetwork();
        } finally {
            ManagerPhaseProfiler.scan(start, cables.size(), visited);
        }
    }

    private int scanNetwork() {
        if (world == null || managerPos == null) return 0;
        cables.clear();
        peripherals.clear();

        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();

        // Start search from blocks adjacent to manager
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            BlockPos neighbor = managerPos.offset(dir);
            checkAndAdd(neighbor, queue, visited);
        }

        while (!queue.isEmpty()) {
            BlockPos curr = queue.poll();
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                BlockPos next = curr.offset(dir);
                checkAndAdd(next, queue, visited);
            }
        }

        int visitedCount = visited.size();
        if (trackingTopology) {
            visited.add(managerPos);
            updateTopologyFootprint(visited);
        }
        lastScanTime = world.getTotalWorldTime();
        version = version(world);
        // Refresh every 21 ticks on a stable position-based phase instead of aligning
        // every newly loaded manager. The first refresh can be earlier; invalidations
        // still bypass the deadline, and discovery never changes a program's due tick.
        long delay = Math.floorMod(scanPhase - Math.floorMod(lastScanTime, (long) SCAN_INTERVAL), (long) SCAN_INTERVAL);
        if (delay == 0) delay = SCAN_INTERVAL;
        nextScanTime = lastScanTime > Long.MAX_VALUE - delay ? Long.MAX_VALUE : lastScanTime + delay;
        return visitedCount;
    }

    private void checkAndAdd(BlockPos pos, Queue<BlockPos> queue, Set<BlockPos> visited) {
        if (!visited.add(pos) || !pos.isLoaded(world)) return;

        Block block = pos.getBlock(world);
        int meta = pos.getBlockMetadata(world);

        boolean isCable = (block instanceof BlockCable);
        if (!isCable && Compat.isSFMLoaded()) {
            isCable = SteveFMCompat.isCable(block, meta);
        }

        if (isCable) {
            cables.add(pos);
            queue.add(pos);
            TileEntity te = pos.getTileEntity(world);
            if (te != null) {
                peripherals.add(pos);
            }
        } else {
            TileEntity te = pos.getTileEntity(world);
            if (te != null) {
                peripherals.add(pos);
            }
        }
    }

    public boolean isConnected(BlockPos pos) {
        if (pos == null || !pos.isLoaded(world)) return false;
        if (needsScan()) {
            scan();
        }
        return peripherals.contains(pos) || cables.contains(pos);
    }

    /** Scoped edit history survives transient gaps even when no manager observes the gap. */
    public long getTopologyGeneration() {
        synchronized (CableNetwork.class) {
            if (!trackingTopology && world != null && !world.isRemote && managerPos != null) {
                trackingTopology = true;
                Set<BlockPos> positions = new HashSet<>(cables);
                positions.addAll(peripherals);
                // Manager neighbor notifications carry its own coordinate; these
                // remain conservative local rebases even without a proven cable gap.
                positions.add(managerPos);
                for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
                    positions.add(managerPos.offset(direction));
                    for (BlockPos cable : cables) positions.add(cable.offset(direction));
                }
                updateTopologyFootprint(positions);
                EDGE_NETWORKS.computeIfAbsent(world, ignored -> new WeakHashMap<>())
                    .put(this, Boolean.TRUE);
            }
            return topologyGeneration;
        }
    }

    private void updateTopologyFootprint(Set<BlockPos> positions) {
        topologyPositions = positions;
        Set<Long> chunks = new HashSet<>();
        for (BlockPos pos : positions) chunks.add(chunkKey(pos.x >> 4, pos.z >> 4));
        // Native inputs can conduct through a solid neighbor to a second block.
        // Include provider chunk gaps without querying or polling those providers.
        for (BlockPos pos : peripherals) {
            for (int x = (pos.x - 2) >> 4; x <= (pos.x + 2) >> 4; x++)
                for (int z = (pos.z - 2) >> 4; z <= (pos.z + 2) >> 4; z++) chunks.add(chunkKey(x, z));
        }
        topologyChunks = chunks;
    }

    public Set<BlockPos> getPeripherals() {
        if (needsScan()) {
            scan();
        }
        return Collections.unmodifiableSet(peripherals);
    }

    public Set<BlockPos> getCables() {
        if (needsScan()) {
            scan();
        }
        return Collections.unmodifiableSet(cables);
    }
}
