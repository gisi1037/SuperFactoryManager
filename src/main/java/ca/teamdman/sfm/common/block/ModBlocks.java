package ca.teamdman.sfm.common.block;

import net.minecraft.block.Block;

import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import cpw.mods.fml.common.registry.GameRegistry;

public final class ModBlocks {

    public static Block manager;
    public static Block cable;

    public static void init() {
        manager = new BlockManager();
        cable = new BlockCable();

        GameRegistry.registerBlock(manager, "manager");
        GameRegistry.registerBlock(cable, "cable");

        GameRegistry.registerTileEntity(TileEntityManager.class, "sfm.manager");
    }
}
