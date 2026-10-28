package ca.teamdman.sfm.common.network;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

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
    private static final int SCAN_INTERVAL = 21;
    private long version;
    private final int scanPhase;
    private long nextScanTime;

    public static synchronized void invalidate(World world) {
        if (world != null && !world.isRemote) VERSIONS.put(world, VERSIONS.getOrDefault(world, 0L) + 1);
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

        lastScanTime = world.getTotalWorldTime();
        version = version(world);
        // Refresh every 21 ticks on a stable position-based phase instead of aligning
        // every newly loaded manager. The first refresh can be earlier; invalidations
        // still bypass the deadline, and discovery never changes a program's due tick.
        long delay = Math.floorMod(scanPhase - Math.floorMod(lastScanTime, (long) SCAN_INTERVAL), (long) SCAN_INTERVAL);
        if (delay == 0) delay = SCAN_INTERVAL;
        nextScanTime = lastScanTime > Long.MAX_VALUE - delay ? Long.MAX_VALUE : lastScanTime + delay;
        return visited.size();
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
