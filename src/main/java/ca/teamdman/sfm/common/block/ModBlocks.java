package ca.teamdman.sfm.common.block;

import net.minecraft.block.Block;

import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.tileentity.TileEntityProgrammingStation;
import ca.teamdman.sfm.common.tileentity.TileEntityRedstonePort;
import cpw.mods.fml.common.registry.GameRegistry;

public final class ModBlocks {

    public static Block manager;
    public static Block cable;
    public static Block fancyCable;
    public static Block programmingStation;
    public static Block redstonePort;

    public static void init() {
        manager = new BlockManager();
        cable = new BlockCable();
        fancyCable = new BlockFancyCable();
        programmingStation = new BlockProgrammingStation();
        redstonePort = new BlockRedstonePort();

        GameRegistry.registerBlock(manager, "manager");
        GameRegistry.registerBlock(cable, "cable");
        GameRegistry.registerBlock(fancyCable, "fancyCable");
        GameRegistry.registerBlock(programmingStation, "programmingStation");
        GameRegistry.registerBlock(redstonePort, "redstonePort");

        GameRegistry.registerTileEntity(TileEntityManager.class, "sfm.manager");
        GameRegistry.registerTileEntity(TileEntityProgrammingStation.class, "sfm.programmingStation");
        GameRegistry.registerTileEntity(TileEntityRedstonePort.class, "sfm.redstonePort");
    }
}
