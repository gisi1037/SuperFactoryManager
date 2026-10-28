package ca.teamdman.sfm.common.net;

import net.minecraft.entity.player.EntityPlayer;

import ca.teamdman.sfm.common.gui.ContainerManager;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

public final class ManagerAccess {

    private ManagerAccess() {}

    public static TileEntityManager get(EntityPlayer player, int x, int y, int z) {
        if (player == null || !(player.openContainer instanceof ContainerManager)) return null;
        ContainerManager container = (ContainerManager) player.openContainer;
        TileEntityManager manager = container.getManager();
        if (manager.xCoord != x || manager.yCoord != y || manager.zCoord != z || !container.canInteractWith(player))
            return null;
        return manager;
    }
}
