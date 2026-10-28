package ca.teamdman.sfm.common.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

    public static final int GUI_MANAGER = 0;
    public static final int GUI_LABEL_GUN = 1;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == GUI_MANAGER) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityManager) {
                return new ContainerManager(player.inventory, (TileEntityManager) te);
            }
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return SuperFactoryManager.proxy.getClientGuiElement(id, player, world, x, y, z);
    }
}
