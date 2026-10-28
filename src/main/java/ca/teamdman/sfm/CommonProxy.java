package ca.teamdman.sfm;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ca.teamdman.sfm.common.block.ModBlocks;
import ca.teamdman.sfm.common.command.CommandSfmProfile;
import ca.teamdman.sfm.common.command.CommandSfmRecovery;
import ca.teamdman.sfm.common.diagnostics.ManagerTickProfiler;
import ca.teamdman.sfm.common.gui.GuiHandler;
import ca.teamdman.sfm.common.item.ModItems;
import ca.teamdman.sfm.common.net.SFMNetwork;
import ca.teamdman.sfm.common.recipe.ModRecipes;
import cpw.mods.fml.common.FMLCommonHandler;
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
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new ca.teamdman.sfm.common.network.NetworkEvents());
        FMLCommonHandler.instance()
            .bus()
            .register(ManagerTickProfiler.INSTANCE);
    }

    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(SuperFactoryManager.instance, new GuiHandler());
        ModRecipes.init();
    }

    public void postInit(FMLPostInitializationEvent event) {
        ModRecipes.postInit();
    }

    public void serverStarting(FMLServerStartingEvent event) {
        ManagerTickProfiler.INSTANCE.reset();
        event.registerServerCommand(new CommandSfmProfile());
        event.registerServerCommand(new CommandSfmRecovery());
    }

    // Client hooks use common types so server class verification never resolves GUI classes.
    public void openLabelGun(EntityPlayer player, ItemStack stack) {}

    public void sendLabelGunUse(ItemStack stack, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {}

    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

}
