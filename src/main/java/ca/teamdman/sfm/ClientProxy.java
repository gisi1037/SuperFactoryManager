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
        MinecraftForge.EVENT_BUS.register(new ClientItemWorldRenderer());
        MinecraftForge.EVENT_BUS.register(new LabelGunReminderOverlay());
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }
}
