package ca.teamdman.sfm.common.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import ca.teamdman.sfm.common.tileentity.TileEntityProgrammingStation;
import ca.teamdman.sfm.integration.mui2.ProgrammingStationMui2Factory;

/** Authoring access point. Removing it never removes projects from the world store. */
public final class BlockProgrammingStation extends BlockContainer {

    public BlockProgrammingStation() {
        super(Material.iron);
        setBlockName("sfm.programmingStation");
        setBlockTextureName("superfactorymanager:programming_station_front");
        setHardness(2.5F);
        setResistance(15);
        setStepSound(soundTypeMetal);
        setCreativeTab(CreativeTabs.tabRedstone);
    }

    private net.minecraft.util.IIcon iconTop, iconBottom, iconSide;

    @Override
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister registry) {
        blockIcon = registry.registerIcon("superfactorymanager:programming_station_front");
        iconTop = registry.registerIcon("superfactorymanager:programming_station_top");
        iconBottom = registry.registerIcon("superfactorymanager:programming_station_bottom");
        iconSide = registry.registerIcon("superfactorymanager:programming_station_side");
    }

    @Override
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public net.minecraft.util.IIcon getIcon(int side, int metadata) {
        if (side == 0) return iconBottom;
        if (side == 1) return iconTop;
        // Previously placed stations have metadata zero; show their screen on the south face.
        int front = metadata >= 2 && metadata <= 5 ? metadata : 3;
        return side == front ? blockIcon : iconSide;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int direction = MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + .5D) & 3;
        int front = direction == 0 ? 2 : direction == 1 ? 5 : direction == 2 ? 3 : 4;
        world.setBlockMetadataWithNotify(x, y, z, front, 2);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityProgrammingStation();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileEntityProgrammingStation)
                ProgrammingStationMui2Factory.INSTANCE.open(player, (TileEntityProgrammingStation) tile);
        }
        return true;
    }
}
