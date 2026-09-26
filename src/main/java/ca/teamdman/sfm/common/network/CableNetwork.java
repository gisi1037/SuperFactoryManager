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
import ca.teamdman.sfm.common.util.BlockPos;

public final class CableNetwork {

    private final World world;
    private final BlockPos managerPos;
    private final Set<BlockPos> cables = new HashSet<>();
    private final Set<BlockPos> peripherals = new HashSet<>();
    private long lastScanTime = -1;

    public CableNetwork(World world, BlockPos managerPos) {
        this.world = world;
        this.managerPos = managerPos;
    }

    public void scan() {
        if (world == null || managerPos == null) return;
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
    }

    private void checkAndAdd(BlockPos pos, Queue<BlockPos> queue, Set<BlockPos> visited) {
        if (visited.contains(pos)) return;
        visited.add(pos);

        Block block = pos.getBlock(world);
        if (block instanceof BlockCable) {
            cables.add(pos);
            queue.add(pos);
        } else {
            TileEntity te = pos.getTileEntity(world);
            if (te != null) {
                peripherals.add(pos);
            }
        }
    }

    public boolean isConnected(BlockPos pos) {
        if (pos == null) return false;
        if (world.getTotalWorldTime() - lastScanTime > 20 || lastScanTime < 0) {
            scan();
        }
        return peripherals.contains(pos) || cables.contains(pos);
    }

    public Set<BlockPos> getPeripherals() {
        if (world.getTotalWorldTime() - lastScanTime > 20 || lastScanTime < 0) {
            scan();
        }
        return Collections.unmodifiableSet(peripherals);
    }

    public Set<BlockPos> getCables() {
        if (world.getTotalWorldTime() - lastScanTime > 20 || lastScanTime < 0) {
            scan();
        }
        return Collections.unmodifiableSet(cables);
    }
}
