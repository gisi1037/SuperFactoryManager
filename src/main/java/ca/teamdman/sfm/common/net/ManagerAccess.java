package ca.teamdman.sfm.common.net;

import net.minecraft.entity.player.EntityPlayer;

import ca.teamdman.sfm.common.gui.ManagerEditContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

public final class ManagerAccess {

    private ManagerAccess() {}

    /** Read-only tools must reject unloaded/foreign-world managers before vanilla container checks can look them up. */
    public static TileEntityManager getLoaded(EntityPlayer player, int x, int y, int z,
        ca.teamdman.sfm.common.gui.ManagerControlToken token) {
        if (!PlayerUiAccess.isLive(player) || !(player.openContainer instanceof ManagerEditContext)) return null;
        TileEntityManager manager = ((ManagerEditContext) player.openContainer).getManager();
        if (manager == null || manager.xCoord != x
            || manager.yCoord != y
            || manager.zCoord != z
            || manager.getWorldObj() == null
            || player.worldObj != manager.getWorldObj()
            || manager.isInvalid()
            || !manager.getWorldObj()
                .blockExists(x, y, z)
            || manager.getWorldObj()
                .getTileEntity(x, y, z) != manager)
            return null;
        return get(player, x, y, z, token);
    }

    public static TileEntityManager get(EntityPlayer player, int x, int y, int z,
        ca.teamdman.sfm.common.gui.ManagerControlToken token) {
        TileEntityManager manager = get(player, x, y, z);
        if (manager == null || token == null) return null;
        ManagerEditContext context = (ManagerEditContext) player.openContainer;
        return token.matches(context.getEditSessionId(), manager.getDiskGeneration(), player.openContainer.windowId)
            ? manager
            : null;
    }

    public static TileEntityManager get(EntityPlayer player, int x, int y, int z) {
        if (!PlayerUiAccess.isLive(player) || !(player.openContainer instanceof ManagerEditContext)) return null;
        ManagerEditContext container = (ManagerEditContext) player.openContainer;
        TileEntityManager manager = container.getManager();
        if (manager.xCoord != x || manager.yCoord != y
            || manager.zCoord != z
            || !player.openContainer.canInteractWith(player)) return null;
        return manager;
    }
}
