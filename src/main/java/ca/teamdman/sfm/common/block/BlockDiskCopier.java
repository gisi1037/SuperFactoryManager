package ca.teamdman.sfm.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import ca.teamdman.sfm.common.tileentity.TileEntityDiskCopier;
import ca.teamdman.sfm.integration.mui2.DiskCopierMui2Factory;

/** A separate media writer, never a second controller or a source-code authoring endpoint. */
public final class BlockDiskCopier extends BlockContainer {

    public BlockDiskCopier() {
        super(Material.iron);
        setBlockName("sfm.diskCopier");
        setBlockTextureName("superfactorymanager:manager_side");
        setHardness(2.5F);
        setResistance(15);
        setStepSound(soundTypeMetal);
        setCreativeTab(CreativeTabs.tabRedstone);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityDiskCopier();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileEntityDiskCopier)
                DiskCopierMui2Factory.INSTANCE.open(player, (TileEntityDiskCopier) tile);
        }
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int metadata) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!world.isRemote && tile instanceof TileEntityDiskCopier) {
            TileEntityDiskCopier copier = (TileEntityDiskCopier) tile;
            for (int i = 0; i < copier.getSizeInventory(); i++) {
                ItemStack stack = copier.getStackInSlotOnClosing(i);
                if (stack != null) world.spawnEntityInWorld(new EntityItem(world, x + .5, y + .5, z + .5, stack));
            }
        }
        super.breakBlock(world, x, y, z, block, metadata);
    }
}
