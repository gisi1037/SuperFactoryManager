package ca.teamdman.sfm.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

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

    public boolean canConnectTo(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        Block neighbor = world.getBlock(x + side.offsetX, y + side.offsetY, z + side.offsetZ);
        if (neighbor instanceof BlockCable || neighbor instanceof BlockManager) {
            return true;
        }
        return world.getTileEntity(x + side.offsetX, y + side.offsetY, z + side.offsetZ) != null;
    }
}
