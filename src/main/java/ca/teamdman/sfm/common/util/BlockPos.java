package ca.teamdman.sfm.common.util;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public final class BlockPos {

    public static final BlockPos ORIGIN = new BlockPos(0, 0, 0);

    public final int x;
    public final int y;
    public final int z;

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public BlockPos offset(ForgeDirection direction) {
        if (direction == null || direction == ForgeDirection.UNKNOWN) {
            return this;
        }
        return new BlockPos(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
    }

    public BlockPos offset(int dx, int dy, int dz) {
        return new BlockPos(x + dx, y + dy, z + dz);
    }

    public TileEntity getTileEntity(IBlockAccess world) {
        return isLoaded(world) ? world.getTileEntity(x, y, z) : null;
    }

    public Block getBlock(IBlockAccess world) {
        return isLoaded(world) ? world.getBlock(x, y, z) : null;
    }

    public int getBlockMetadata(IBlockAccess world) {
        return isLoaded(world) ? world.getBlockMetadata(x, y, z) : 0;
    }

    public boolean isLoaded(IBlockAccess world) {
        return world != null && (!(world instanceof World) || ((World) world).blockExists(x, y, z));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockPos that)) return false;
        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        return (31 * x + y) * 31 + z;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + "]";
    }
}
