package ca.teamdman.sfm.common.net;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/** Revalidate the actual connection/entity at the server execution boundary. */
public final class PlayerUiAccess {

    private PlayerUiAccess() {}

    public static boolean isLive(EntityPlayer player) {
        if (player == null || player.isDead || player.worldObj == null) return false;
        if (!(player instanceof EntityPlayerMP)) return true;
        EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
        return serverPlayer.playerNetServerHandler != null && serverPlayer.playerNetServerHandler.netManager != null
            && serverPlayer.playerNetServerHandler.netManager.isChannelOpen();
    }
}
