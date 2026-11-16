package ca.teamdman.sfm.common.block;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockFancyCable extends BlockCable {

    public static int renderId = -1;

    // Shared by rendering, physical collision, mouse targeting and selection outlines.
    public static final float CORE_MIN = 4F / 16F, CORE_MAX = 12F / 16F;
    public static final float ARM_MIN = 5F / 16F, ARM_MAX = 11F / 16F;
    private static final double[][] PARTS = { { CORE_MIN, CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX, CORE_MAX },
        { ARM_MIN, 0, ARM_MIN, ARM_MAX, CORE_MIN, ARM_MAX }, { ARM_MIN, CORE_MAX, ARM_MIN, ARM_MAX, 1, ARM_MAX },
        { ARM_MIN, ARM_MIN, 0, ARM_MAX, ARM_MAX, CORE_MIN }, { ARM_MIN, ARM_MIN, CORE_MAX, ARM_MAX, ARM_MAX, 1 },
        { 0, ARM_MIN, ARM_MIN, CORE_MIN, ARM_MAX, ARM_MAX }, { CORE_MAX, ARM_MIN, ARM_MIN, 1, ARM_MAX, ARM_MAX } };

    public BlockFancyCable() {
        super();
        setBlockName("sfm.fancyCable");
        setBlockTextureName("superfactorymanager:fancy_cable");
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return renderId;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        float minX = canConnectTo(world, x, y, z, ForgeDirection.WEST) ? 0.0F : CORE_MIN;
        float maxX = canConnectTo(world, x, y, z, ForgeDirection.EAST) ? 1.0F : CORE_MAX;
        float minY = canConnectTo(world, x, y, z, ForgeDirection.DOWN) ? 0.0F : CORE_MIN;
        float maxY = canConnectTo(world, x, y, z, ForgeDirection.UP) ? 1.0F : CORE_MAX;
        float minZ = canConnectTo(world, x, y, z, ForgeDirection.NORTH) ? 0.0F : CORE_MIN;
        float maxZ = canConnectTo(world, x, y, z, ForgeDirection.SOUTH) ? 1.0F : CORE_MAX;
        setBlockBounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setBlockBounds(CORE_MIN, CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX, CORE_MAX);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask, List list,
        Entity collidingEntity) {
        for (int part = 0; part < PARTS.length; part++) {
            AxisAlignedBB bounds = getPartBounds(world, x, y, z, part);
            if (bounds != null && mask.intersectsWith(bounds)) list.add(bounds);
        }
    }

    /** Part zero is the core, followed by the six ForgeDirection arms. Returns a fresh world-space box. */
    public AxisAlignedBB getPartBounds(IBlockAccess world, int x, int y, int z, int part) {
        if (part < 0 || part >= PARTS.length
            || part > 0 && !canConnectTo(world, x, y, z, ForgeDirection.VALID_DIRECTIONS[part - 1])) return null;
        double[] bounds = PARTS[part];
        return AxisAlignedBB
            .getBoundingBox(x + bounds[0], y + bounds[1], z + bounds[2], x + bounds[3], y + bounds[4], z + bounds[5]);
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 start, Vec3 end) {
        MovingObjectPosition closest = null;
        double distance = Double.POSITIVE_INFINITY;
        // Trace the pieces directly. An enclosing box incorrectly makes the gaps between arms clickable.
        for (int part = 0; part < PARTS.length; part++) {
            AxisAlignedBB bounds = getPartBounds(world, x, y, z, part);
            MovingObjectPosition hit = bounds == null ? null : bounds.calculateIntercept(start, end);
            if (hit == null) continue;
            double candidate = start.squareDistanceTo(hit.hitVec);
            if (candidate < distance) {
                closest = new MovingObjectPosition(x, y, z, hit.sideHit, hit.hitVec);
                closest.subHit = part;
                distance = candidate;
            }
        }
        return closest;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        return true;
    }
}
