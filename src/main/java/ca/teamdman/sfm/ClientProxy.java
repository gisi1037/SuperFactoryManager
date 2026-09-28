package ca.teamdman.sfm;

import net.minecraftforge.common.MinecraftForge;

import ca.teamdman.sfm.client.render.ClientItemWorldRenderer;
import ca.teamdman.sfm.client.render.LabelGunReminderOverlay;
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
        if (Boolean.getBoolean("sfm.testHarness") || Boolean.getBoolean("gtnh.testHarness")) {
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
}
