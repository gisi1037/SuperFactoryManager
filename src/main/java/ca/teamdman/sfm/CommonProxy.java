package ca.teamdman.sfm;

import ca.teamdman.sfm.common.block.ModBlocks;
import ca.teamdman.sfm.common.gui.GuiHandler;
import ca.teamdman.sfm.common.item.ModItems;
import ca.teamdman.sfm.common.net.SFMNetwork;
import ca.teamdman.sfm.common.recipe.ModRecipes;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        SuperFactoryManager.LOG.info("Initializing Super Factory Manager at version " + Tags.VERSION);

        ModBlocks.init();
        ModItems.init();
        SFMNetwork.init();
    }

    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(SuperFactoryManager.instance, new GuiHandler());
        ModRecipes.init();
    }

    public void postInit(FMLPostInitializationEvent event) {}

    public void serverStarting(FMLServerStartingEvent event) {}
}
