package ca.teamdman.sfm.compat.stevefm;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;

import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfm.compat.Compat;

public final class SteveFMCompat {

    private static Field fieldStrengths;
    private static Field fieldStrong;
    private static Field fieldUpdatedStrength;
    private static Field fieldUpdatedStrong;
    private static boolean reflectionInitDone = false;

    private SteveFMCompat() {}

    public static boolean isCable(Block block, int meta) {
        try {
            if (vswe.stevesfactory.blocks.ModBlocks.blockCable != null) {
                if (vswe.stevesfactory.blocks.ModBlocks.blockCable.isCable(block, meta)) {
                    return true;
                }
            }
            if (block instanceof vswe.stevesfactory.blocks.BlockCableDirectionAdvanced) {
                return ((vswe.stevesfactory.blocks.BlockCableDirectionAdvanced) block).isAdvanced(meta);
            }
        } catch (Throwable ignored) {}
        return false;
    }

    public static IInventory getInventory(World world, BlockPos pos, TileEntity te) {
        if (world == null || pos == null || te == null) return null;
        try {
            if (te instanceof vswe.stevesfactory.blocks.TileEntityCluster) {
                vswe.stevesfactory.blocks.TileEntityCluster cluster = (vswe.stevesfactory.blocks.TileEntityCluster) te;
                for (vswe.stevesfactory.blocks.TileEntityClusterElement elem : cluster.getElements()) {
                    if (elem instanceof IInventory) {
                        cluster.setWorldObject(elem);
                        return (IInventory) elem;
                    }
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public static IFluidHandler getFluidHandler(World world, BlockPos pos, TileEntity te) {
        if (world == null || pos == null || te == null) return null;
        try {
            if (te instanceof vswe.stevesfactory.blocks.TileEntityCluster) {
                vswe.stevesfactory.blocks.TileEntityCluster cluster = (vswe.stevesfactory.blocks.TileEntityCluster) te;
                for (vswe.stevesfactory.blocks.TileEntityClusterElement elem : cluster.getElements()) {
                    if (elem instanceof IFluidHandler) {
                        cluster.setWorldObject(elem);
                        return (IFluidHandler) elem;
                    }
                    if (Compat.isStevesAddonsLoaded() && "stevesaddons.tileentities.TileEntityAENode".equals(
                        elem.getClass()
                            .getName())) {
                        cluster.setWorldObject(elem);
                        try {
                            java.lang.reflect.Method getTankMethod = elem.getClass()
                                .getMethod("getTank");
                            Object tank = getTankMethod.invoke(elem);
                            if (tank instanceof IFluidHandler) return (IFluidHandler) tank;
                        } catch (Throwable ignored) {}
                    }
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public static boolean isReceiver(World world, BlockPos pos) {
        if (world == null || pos == null) return false;
        try {
            return vswe.stevesfactory.blocks.TileEntityCluster
                .getTileEntity(vswe.stevesfactory.blocks.TileEntityInput.class, world, pos.x, pos.y, pos.z) != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static int getReceiverPower(World world, BlockPos pos) {
        if (world == null || pos == null) return 0;
        try {
            vswe.stevesfactory.blocks.TileEntityInput input = vswe.stevesfactory.blocks.TileEntityCluster
                .getTileEntity(vswe.stevesfactory.blocks.TileEntityInput.class, world, pos.x, pos.y, pos.z);
            if (input == null) return 0;
            int max = 0;
            int[] power = input.getPower();
            if (power != null) {
                for (int p : power) {
                    if (p > max) max = p;
                }
            }
            return max;
        } catch (Throwable ignored) {
            return 0;
        }
    }

    public static int getMaxReceiverPower(World world, Set<BlockPos> peripherals) {
        if (world == null || peripherals == null || peripherals.isEmpty()) return 0;
        int max = 0;
        for (BlockPos pos : peripherals) {
            int p = getReceiverPower(world, pos);
            if (p > max) {
                max = p;
                if (max == 15) break;
            }
        }
        return max;
    }

    public static boolean isEmitter(World world, BlockPos pos) {
        if (world == null || pos == null) return false;
        try {
            return vswe.stevesfactory.blocks.TileEntityCluster
                .getTileEntity(vswe.stevesfactory.blocks.TileEntityOutput.class, world, pos.x, pos.y, pos.z) != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void initReflection() {
        if (reflectionInitDone) return;
        reflectionInitDone = true;
        try {
            fieldStrengths = vswe.stevesfactory.blocks.TileEntityOutput.class.getDeclaredField("strengths");
            fieldStrengths.setAccessible(true);
            fieldStrong = vswe.stevesfactory.blocks.TileEntityOutput.class.getDeclaredField("strong");
            fieldStrong.setAccessible(true);
            fieldUpdatedStrength = vswe.stevesfactory.blocks.TileEntityOutput.class.getDeclaredField("updatedStrength");
            fieldUpdatedStrength.setAccessible(true);
            fieldUpdatedStrong = vswe.stevesfactory.blocks.TileEntityOutput.class.getDeclaredField("updatedStrong");
            fieldUpdatedStrong.setAccessible(true);
        } catch (Throwable ignored) {}
    }

    public static boolean setRedstoneOutput(World world, BlockPos pos, List<ForgeDirection> sides, int strength,
        boolean strong) {
        if (world == null || pos == null) return false;
        try {
            initReflection();
            TileEntity te = world.getTileEntity(pos.x, pos.y, pos.z);
            vswe.stevesfactory.blocks.TileEntityOutput output = vswe.stevesfactory.blocks.TileEntityCluster
                .getTileEntity(vswe.stevesfactory.blocks.TileEntityOutput.class, world, pos.x, pos.y, pos.z);
            if (output == null) return false;

            int[] strengths = fieldStrengths != null ? (int[]) fieldStrengths.get(output) : null;
            int[] updatedStrength = fieldUpdatedStrength != null ? (int[]) fieldUpdatedStrength.get(output) : null;
            boolean[] strongArr = fieldStrong != null ? (boolean[]) fieldStrong.get(output) : null;
            boolean[] updatedStrong = fieldUpdatedStrong != null ? (boolean[]) fieldUpdatedStrong.get(output) : null;

            List<ForgeDirection> targetSides = (sides != null && !sides.isEmpty()) ? sides
                : Arrays.asList(ForgeDirection.VALID_DIRECTIONS);

            for (ForgeDirection dir : targetSides) {
                if (dir == null || dir == ForgeDirection.UNKNOWN) continue;
                int side = dir.ordinal();
                if (strengths != null && side < strengths.length) strengths[side] = strength;
                if (updatedStrength != null && side < updatedStrength.length) updatedStrength[side] = strength;
                if (strongArr != null && side < strongArr.length) strongArr[side] = strong;
                if (updatedStrong != null && side < updatedStrong.length) updatedStrong[side] = strong;
            }

            output.markDirty();
            if (te != null) {
                te.markDirty();
                world.markBlockForUpdate(pos.x, pos.y, pos.z);
                Block block = te.getBlockType();
                if (block != null) {
                    world.notifyBlocksOfNeighborChange(pos.x, pos.y, pos.z, block);
                    for (ForgeDirection dir : targetSides) {
                        if (dir == null || dir == ForgeDirection.UNKNOWN) continue;
                        world.notifyBlockOfNeighborChange(
                            pos.x + dir.offsetX,
                            pos.y + dir.offsetY,
                            pos.z + dir.offsetZ,
                            block);
                    }
                }
                if (!world.isRemote && te instanceof vswe.stevesfactory.network.IPacketBlock) {
                    vswe.stevesfactory.network.PacketHandler
                        .sendBlockPacket((vswe.stevesfactory.network.IPacketBlock) te, null, 0);
                }
            }
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isBUD(World world, BlockPos pos) {
        if (world == null || pos == null) return false;
        try {
            return vswe.stevesfactory.blocks.TileEntityCluster
                .getTileEntity(vswe.stevesfactory.blocks.TileEntityBUD.class, world, pos.x, pos.y, pos.z) != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean checkAndClearBUDUpdates(World world, Set<BlockPos> peripherals) {
        if (world == null || peripherals == null || peripherals.isEmpty()) return false;
        boolean updated = false;
        for (BlockPos pos : peripherals) {
            try {
                vswe.stevesfactory.blocks.TileEntityBUD bud = vswe.stevesfactory.blocks.TileEntityCluster
                    .getTileEntity(vswe.stevesfactory.blocks.TileEntityBUD.class, world, pos.x, pos.y, pos.z);
                if (bud == null) continue;
                int[] data = bud.getData();
                boolean changed = false;
                for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
                    ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[i];
                    int nx = pos.x + dir.offsetX;
                    int ny = pos.y + dir.offsetY;
                    int nz = pos.z + dir.offsetZ;
                    Block b = world.getBlock(nx, ny, nz);
                    int meta = world.getBlockMetadata(nx, ny, nz);
                    int currentVal = (Block.getIdFromBlock(b) << 4) | (meta & 15);
                    if (data != null && i < data.length && data[i] != currentVal) {
                        changed = true;
                        break;
                    }
                }
                if (changed) {
                    bud.updateData();
                    bud.makeOld();
                    updated = true;
                }
            } catch (Throwable ignored) {}
        }
        return updated;
    }
}
