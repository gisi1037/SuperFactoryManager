package ca.teamdman.sfm;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import ca.teamdman.sfm.client.render.ClientItemWorldRenderer;
import ca.teamdman.sfm.client.render.LabelGunReminderOverlay;
import ca.teamdman.sfm.common.gui.GuiHandler;
import ca.teamdman.sfm.common.gui.GuiLabelGun;
import ca.teamdman.sfm.common.gui.GuiManager;
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
        Minecraft.getMinecraft()
            .displayGuiScreen(new GuiLabelGun(player, stack));
    }

    @Override
    public void sendLabelGunUse(ItemStack stack, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        Minecraft.getMinecraft()
            .getNetHandler()
            .addToSendQueue(new C08PacketPlayerBlockPlacement(x, y, z, side, stack, hitX, hitY, hitZ));
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == GuiHandler.GUI_MANAGER) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileEntityManager) return new GuiManager(player.inventory, (TileEntityManager) tile);
        } else if (id == GuiHandler.GUI_LABEL_GUN) return new GuiLabelGun(player, player.getHeldItem());
        return null;
    }

}
