package ca.teamdman.sfm.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import ca.teamdman.sfm.compat.Compat;
import ca.teamdman.sfm.compat.stevefm.SteveFMCompat;

public class BlockCable extends Block {

    public BlockCable() {
        super(Material.iron);
        setHardness(1.5F);
        setResistance(10.0F);
        setStepSound(soundTypeMetal);
        setBlockName("sfm.cable");
        setBlockTextureName("superfactorymanager:cable");
        setCreativeTab(CreativeTabs.tabRedstone);
    }

    @Override
    public void onBlockAdded(net.minecraft.world.World world, int x, int y, int z) {
        ca.teamdman.sfm.common.network.CableNetwork.invalidate(world);
        super.onBlockAdded(world, x, y, z);
    }

    @Override
    public void breakBlock(net.minecraft.world.World world, int x, int y, int z, Block block, int meta) {
        ca.teamdman.sfm.common.network.CableNetwork.invalidate(world);
        super.breakBlock(world, x, y, z, block, meta);
    }

    public boolean canConnectTo(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        int targetX = x + side.offsetX;
        int targetY = y + side.offsetY;
        int targetZ = z + side.offsetZ;
        Block neighbor = world.getBlock(targetX, targetY, targetZ);
        if (neighbor instanceof BlockCable || neighbor instanceof BlockManager) {
            return true;
        }
        if (Compat.isSFMLoaded()) {
            int meta = world.getBlockMetadata(targetX, targetY, targetZ);
            if (SteveFMCompat.isCable(neighbor, meta)) {
                return true;
            }
        }
        return world.getTileEntity(targetX, targetY, targetZ) != null;
    }
}
