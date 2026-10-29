package ca.teamdman.sfm;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ca.teamdman.sfm.common.block.ModBlocks;
import ca.teamdman.sfm.common.command.CommandSfmProfile;
import ca.teamdman.sfm.common.command.CommandSfmRecovery;
import ca.teamdman.sfm.common.diagnostics.ManagerTickProfiler;
import ca.teamdman.sfm.common.item.ModItems;
import ca.teamdman.sfm.common.net.SFMNetwork;
import ca.teamdman.sfm.common.recipe.ModRecipes;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        SuperFactoryManager.LOG.info("Initializing Super Factory Manager at version " + Tags.VERSION);

        ModBlocks.init();
        ModItems.init();
        FMLCommonHandler.instance()
            .bus()
            .register(ca.teamdman.sfm.common.net.ProgramEditDispatcher.INSTANCE);
        FMLCommonHandler.instance()
            .bus()
            .register(ca.teamdman.sfm.common.net.ServerUiTasks.INSTANCE);
        FMLCommonHandler.instance()
            .bus()
            .register(ca.teamdman.sfm.common.net.LabelGunAccess.INSTANCE);
        FMLCommonHandler.instance()
            .bus()
            .register(ca.teamdman.sfm.common.program.project.StationProjectService.INSTANCE);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS
            .register(ca.teamdman.sfm.common.program.project.StationProjectService.INSTANCE);
        SFMNetwork.init();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new ca.teamdman.sfm.common.network.NetworkEvents());
        FMLCommonHandler.instance()
            .bus()
            .register(ManagerTickProfiler.INSTANCE);
    }

    public void init(FMLInitializationEvent event) {
        com.cleanroommc.modularui.factory.GuiManager
            .registerFactory(ca.teamdman.sfm.integration.mui2.ManagerMui2Factory.INSTANCE);
        com.cleanroommc.modularui.factory.GuiManager
            .registerFactory(ca.teamdman.sfm.integration.mui2.ProgrammingStationMui2Factory.INSTANCE);
        ModRecipes.init();
    }

    public void postInit(FMLPostInitializationEvent event) {
        ModRecipes.postInit();
    }

    public void serverStarting(FMLServerStartingEvent event) {
        ca.teamdman.sfm.common.program.project.StationProjectService.INSTANCE.close();
        ManagerTickProfiler.INSTANCE.reset();
        ca.teamdman.sfm.common.net.ProgramEditDispatcher.clear();
        ca.teamdman.sfm.common.net.ServerUiTasks.clear();
        ca.teamdman.sfm.common.net.LabelGunAccess.clear();
        event.registerServerCommand(new CommandSfmProfile());
        event.registerServerCommand(new CommandSfmRecovery());
    }

    public void serverStopped(cpw.mods.fml.common.event.FMLServerStoppedEvent event) {
        ca.teamdman.sfm.common.program.project.StationProjectService.INSTANCE.close();
        ca.teamdman.sfm.common.net.ProgramEditDispatcher.clear();
        ca.teamdman.sfm.common.net.ServerUiTasks.clear();
        ca.teamdman.sfm.common.net.LabelGunAccess.clear();
    }

    public void openManagerPage(ca.teamdman.sfm.common.tileentity.TileEntityManager manager, String page,
        ca.teamdman.sfm.common.gui.ManagerControlToken token) {}

    public void receiveLabelGunSession(ca.teamdman.sfm.common.net.PacketLabelGunSession reply) {}

    public void receiveManagerLogs(ca.teamdman.sfm.common.net.PacketManagerLogs reply) {}

    public void receiveDebugTerminal(ca.teamdman.sfm.common.net.PacketDebugTerminalReply reply) {}

    public void receiveStationReply(ca.teamdman.sfm.common.net.PacketStationReply reply) {}

    public void receiveStationDebugReply(ca.teamdman.sfm.common.net.PacketStationDebugReply reply) {}

    public void openProgrammingStation(ca.teamdman.sfm.common.tileentity.TileEntityProgrammingStation station,
        java.util.UUID sessionId) {}

    public void receiveProgramEditReply(ca.teamdman.sfm.common.net.PacketProgramEditReply reply) {}

    // Client hooks use common types so server class verification never resolves GUI classes.
    public void openLabelGun(EntityPlayer player, ItemStack stack) {}

    public void sendLabelGunUse(ItemStack stack, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {}

    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

}
