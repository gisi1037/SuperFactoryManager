package ca.teamdman.sfm.common.net;

import net.minecraft.entity.player.EntityPlayer;

import ca.teamdman.sfm.common.gui.ManagerEditContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

public final class ManagerAccess {

    private ManagerAccess() {}

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
