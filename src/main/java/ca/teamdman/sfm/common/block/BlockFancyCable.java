package ca.teamdman.sfm.common.block;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockFancyCable extends BlockCable {

    public static int renderId = -1;

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
        float minX = canConnectTo(world, x, y, z, ForgeDirection.WEST) ? 0.0F : 0.25F;
        float maxX = canConnectTo(world, x, y, z, ForgeDirection.EAST) ? 1.0F : 0.75F;
        float minY = canConnectTo(world, x, y, z, ForgeDirection.DOWN) ? 0.0F : 0.25F;
        float maxY = canConnectTo(world, x, y, z, ForgeDirection.UP) ? 1.0F : 0.75F;
        float minZ = canConnectTo(world, x, y, z, ForgeDirection.NORTH) ? 0.0F : 0.25F;
        float maxZ = canConnectTo(world, x, y, z, ForgeDirection.SOUTH) ? 1.0F : 0.75F;
        setBlockBounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setBlockBounds(0.25F, 0.25F, 0.25F, 0.75F, 0.75F, 0.75F);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask, List list,
        Entity collidingEntity) {
        // Core bounding box (4/16 to 12/16 = 0.25 to 0.75)
        AxisAlignedBB core = AxisAlignedBB
            .getBoundingBox(x + 0.25F, y + 0.25F, z + 0.25F, x + 0.75F, y + 0.75F, z + 0.75F);
        if (mask.intersectsWith(core)) {
            list.add(core);
        }

        // Connection arms (cross section 5/16 to 11/16 = 0.3125 to 0.6875)
        if (canConnectTo(world, x, y, z, ForgeDirection.DOWN)) {
            AxisAlignedBB bb = AxisAlignedBB
                .getBoundingBox(x + 0.3125F, y + 0.0F, z + 0.3125F, x + 0.6875F, y + 0.25F, z + 0.6875F);
            if (mask.intersectsWith(bb)) {
                list.add(bb);
            }
        }
        if (canConnectTo(world, x, y, z, ForgeDirection.UP)) {
            AxisAlignedBB bb = AxisAlignedBB
                .getBoundingBox(x + 0.3125F, y + 0.75F, z + 0.3125F, x + 0.6875F, y + 1.0F, z + 0.6875F);
            if (mask.intersectsWith(bb)) {
                list.add(bb);
            }
        }
        if (canConnectTo(world, x, y, z, ForgeDirection.NORTH)) {
            AxisAlignedBB bb = AxisAlignedBB
                .getBoundingBox(x + 0.3125F, y + 0.3125F, z + 0.0F, x + 0.6875F, y + 0.6875F, z + 0.25F);
            if (mask.intersectsWith(bb)) {
                list.add(bb);
            }
        }
        if (canConnectTo(world, x, y, z, ForgeDirection.SOUTH)) {
            AxisAlignedBB bb = AxisAlignedBB
                .getBoundingBox(x + 0.3125F, y + 0.3125F, z + 0.75F, x + 0.6875F, y + 0.6875F, z + 1.0F);
            if (mask.intersectsWith(bb)) {
                list.add(bb);
            }
        }
        if (canConnectTo(world, x, y, z, ForgeDirection.WEST)) {
            AxisAlignedBB bb = AxisAlignedBB
                .getBoundingBox(x + 0.0F, y + 0.3125F, z + 0.3125F, x + 0.25F, y + 0.6875F, z + 0.6875F);
            if (mask.intersectsWith(bb)) {
                list.add(bb);
            }
        }
        if (canConnectTo(world, x, y, z, ForgeDirection.EAST)) {
            AxisAlignedBB bb = AxisAlignedBB
                .getBoundingBox(x + 0.75F, y + 0.3125F, z + 0.3125F, x + 1.0F, y + 0.6875F, z + 0.6875F);
            if (mask.intersectsWith(bb)) {
                list.add(bb);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        return true;
    }
}
