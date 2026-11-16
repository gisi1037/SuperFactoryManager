package ca.teamdman.sfm.common.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import ca.teamdman.sfm.common.tileentity.TileEntityWirelessConnector;

/** A cable endpoint whose second connection is a saved, reciprocal wireless pair. */
public final class BlockWirelessConnector extends BlockCable {

    public BlockWirelessConnector() {
        setBlockName("sfm.wirelessConnector");
        setBlockTextureName("superfactorymanager:wireless_connector");
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileEntityWirelessConnector();
    }
}
