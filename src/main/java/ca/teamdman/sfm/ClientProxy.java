package ca.teamdman.sfm;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;

import ca.teamdman.sfm.client.render.ClientItemWorldRenderer;
import ca.teamdman.sfm.client.render.LabelGunReminderOverlay;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        ClientItemWorldRenderer worldRenderer = new ClientItemWorldRenderer();
        MinecraftForge.EVENT_BUS.register(worldRenderer);
        cpw.mods.fml.common.FMLCommonHandler.instance()
            .bus()
            .register(worldRenderer);
        MinecraftForge.EVENT_BUS.register(new LabelGunReminderOverlay());
        int fancyCableRenderId = cpw.mods.fml.client.registry.RenderingRegistry.getNextAvailableRenderId();
        ca.teamdman.sfm.common.block.BlockFancyCable.renderId = fancyCableRenderId;
        cpw.mods.fml.client.registry.RenderingRegistry
            .registerBlockHandler(new ca.teamdman.sfm.client.render.RenderFancyCable(fancyCableRenderId));

        if (Boolean.parseBoolean(System.getProperty("sfm.testHarness", "false"))) {
            ca.teamdman.sfm.client.TestAutomationController.init();
        }
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
        if (ca.teamdman.sfm.compat.Compat.isNEILoaded()) {
            ca.teamdman.sfm.compat.nei.NEICompat.hideItems();
        }
    }

    @Override
    public void openLabelGun(EntityPlayer player, ItemStack stack) {
        ca.teamdman.sfm.client.gui.mui2.Mui2LabelGun.open(player, stack);
    }

    @Override
    public void sendLabelGunUse(ItemStack stack, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        Minecraft.getMinecraft()
            .getNetHandler()
            .addToSendQueue(new C08PacketPlayerBlockPlacement(x, y, z, side, stack, hitX, hitY, hitZ));
    }

    @Override
    public void receiveProgramEditReply(final ca.teamdman.sfm.common.net.PacketProgramEditReply reply) {
        Minecraft.getMinecraft()
            .func_152344_a(new Runnable() {

                @Override
                public void run() {
                    com.cleanroommc.modularui.screen.ModularScreen modular = com.cleanroommc.modularui.screen.ModularScreen
                        .getCurrent();
                    if (modular instanceof ca.teamdman.sfm.client.gui.ProgramEditSession.Receiver) {
                        ((ca.teamdman.sfm.client.gui.ProgramEditSession.Receiver) modular)
                            .receiveProgramEditReply(reply);
                        return;
                    }
                    net.minecraft.client.gui.GuiScreen screen = Minecraft.getMinecraft().currentScreen;
                    if (screen instanceof ca.teamdman.sfm.client.gui.ProgramEditSession.Receiver)
                        ((ca.teamdman.sfm.client.gui.ProgramEditSession.Receiver) screen)
                            .receiveProgramEditReply(reply);
                }
            });
    }

    @Override
    public void openManagerPage(TileEntityManager manager, String page,
        ca.teamdman.sfm.common.gui.ManagerControlToken token) {
        com.cleanroommc.modularui.screen.ModularScreen screen = com.cleanroommc.modularui.screen.ModularScreen
            .getCurrent();
        if (screen instanceof ca.teamdman.sfm.integration.mui2.ManagerMui2Screen)
            ((ca.teamdman.sfm.integration.mui2.ManagerMui2Screen) screen).openPage(page, token);
    }

    @Override
    public void receiveLabelGunSession(ca.teamdman.sfm.common.net.PacketLabelGunSession reply) {
        Minecraft.getMinecraft()
            .func_152344_a(() -> ca.teamdman.sfm.client.gui.ClientLabelGunSession.receive(reply.token));
    }

    @Override
    public void receiveManagerLogs(final ca.teamdman.sfm.common.net.PacketManagerLogs reply) {
        Minecraft.getMinecraft()
            .func_152344_a(new Runnable() {

                @Override
                public void run() {
                    Minecraft mc = Minecraft.getMinecraft();
                    if (mc.theWorld == null) return;
                    TileEntity tile = mc.theWorld.getTileEntity(reply.getX(), reply.getY(), reply.getZ());
                    if (tile instanceof TileEntityManager) {
                        TileEntityManager manager = (TileEntityManager) tile;
                        manager.setLogLevel(ca.teamdman.sfm.common.tileentity.LogLevel.fromString(reply.getLogLevel()));
                        manager.setClientLogs(reply.getLogs());
                    }
                    com.cleanroommc.modularui.screen.ModularScreen screen = com.cleanroommc.modularui.screen.ModularScreen
                        .getCurrent();
                    if (screen instanceof ca.teamdman.sfm.integration.mui2.ManagerMui2Screen)
                        ((ca.teamdman.sfm.integration.mui2.ManagerMui2Screen) screen).receiveLogs(reply);
                }
            });
    }

    @Override
    public void receiveDebugTerminal(final ca.teamdman.sfm.common.net.PacketDebugTerminalReply reply) {
        Minecraft.getMinecraft()
            .func_152344_a(() -> {
                com.cleanroommc.modularui.screen.ModularScreen screen = com.cleanroommc.modularui.screen.ModularScreen
                    .getCurrent();
                if (screen instanceof ca.teamdman.sfm.integration.mui2.ManagerMui2Screen)
                    ((ca.teamdman.sfm.integration.mui2.ManagerMui2Screen) screen).receiveDebugTerminal(reply);
            });
    }

    @Override
    public void receiveStationReply(final ca.teamdman.sfm.common.net.PacketStationReply reply) {
        Minecraft.getMinecraft()
            .func_152344_a(() -> {
                com.cleanroommc.modularui.screen.ModularScreen screen = com.cleanroommc.modularui.screen.ModularScreen
                    .getCurrent();
                if (screen instanceof ca.teamdman.sfm.integration.mui2.ProgrammingStationScreen)
                    ((ca.teamdman.sfm.integration.mui2.ProgrammingStationScreen) screen).receiveStationReply(reply);
            });
    }

    @Override
    public void receiveStationDebugReply(final ca.teamdman.sfm.common.net.PacketStationDebugReply reply) {
        Minecraft.getMinecraft()
            .func_152344_a(() -> {
                com.cleanroommc.modularui.screen.ModularScreen screen = com.cleanroommc.modularui.screen.ModularScreen
                    .getCurrent();
                if (screen instanceof ca.teamdman.sfm.integration.mui2.ProgrammingStationScreen)
                    ((ca.teamdman.sfm.integration.mui2.ProgrammingStationScreen) screen)
                        .receiveStationDebugReply(reply);
            });
    }

    @Override
    public void openProgrammingStation(ca.teamdman.sfm.common.tileentity.TileEntityProgrammingStation station,
        java.util.UUID sessionId) {
        com.cleanroommc.modularui.screen.ModularScreen screen = com.cleanroommc.modularui.screen.ModularScreen
            .getCurrent();
        if (screen instanceof ca.teamdman.sfm.integration.mui2.ProgrammingStationScreen)
            ((ca.teamdman.sfm.integration.mui2.ProgrammingStationScreen) screen).openProjects();
    }

}
