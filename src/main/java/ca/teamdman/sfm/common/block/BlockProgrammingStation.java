package ca.teamdman.sfm.common.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import ca.teamdman.sfm.common.tileentity.TileEntityProgrammingStation;
import ca.teamdman.sfm.integration.mui2.ProgrammingStationMui2Factory;

/** Authoring access point. Removing it never removes projects from the world store. */
public final class BlockProgrammingStation extends BlockContainer {

    public BlockProgrammingStation() {
        super(Material.iron);
        setBlockName("sfm.programmingStation");
        setBlockTextureName("minecraft:crafting_table_side");
        setHardness(2.5F);
        setResistance(15);
        setStepSound(soundTypeMetal);
        setCreativeTab(CreativeTabs.tabRedstone);
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
