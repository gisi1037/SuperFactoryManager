package ca.teamdman.sfm.common.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

public class BlockManager extends BlockContainer {

    public BlockManager() {
        super(Material.iron);
        setHardness(2.5F);
        setResistance(15.0F);
        setStepSound(soundTypeMetal);
        setBlockName("sfm.manager");
        setBlockTextureName("superfactorymanager:manager");
        setCreativeTab(CreativeTabs.tabRedstone);
    }

    private net.minecraft.util.IIcon iconTop;
    private net.minecraft.util.IIcon iconBot;
    private net.minecraft.util.IIcon iconSide;

    @Override
    public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister reg) {
        iconTop = reg.registerIcon("superfactorymanager:manager_top");
        iconBot = reg.registerIcon("superfactorymanager:manager_bot");
        iconSide = reg.registerIcon("superfactorymanager:manager_side");
        this.blockIcon = iconSide;
    }

    @Override
    public net.minecraft.util.IIcon getIcon(int side, int meta) {
        if (side == 1) return iconTop;
        if (side == 0) return iconBot;
        return iconSide;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityManager();
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, net.minecraft.block.Block block) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityManager) {
            ((TileEntityManager) te).rebuildNetwork();
        }
        super.onNeighborBlockChange(world, x, y, z, block);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(SuperFactoryManager.instance, 0, world, x, y, z);
        }
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int meta) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityManager) {
            TileEntityManager manager = (TileEntityManager) te;
            ItemStack disk = manager.getDisk();
            if (disk != null) {
                float rx = world.rand.nextFloat() * 0.8F + 0.1F;
                float ry = world.rand.nextFloat() * 0.8F + 0.1F;
                float rz = world.rand.nextFloat() * 0.8F + 0.1F;
                EntityItem entity = new EntityItem(world, x + rx, y + ry, z + rz, disk);
                world.spawnEntityInWorld(entity);
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
}
