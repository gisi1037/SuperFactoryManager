package ca.teamdman.sfm.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import ca.teamdman.sfm.common.net.LabelGunAccess;

/** Client cache supplies a token once; an open editor never follows a replacement binding. */
public final class ClientLabelGunSession {

    private static World world;
    private static LabelGunAccess.Token token;

    private ClientLabelGunSession() {}

    public static void receive(LabelGunAccess.Token next) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null && next.entityId == mc.thePlayer.getEntityId()
            && next.slot == mc.thePlayer.inventory.currentItem) {
            world = mc.theWorld;
            token = next;
        }
    }

    public static LabelGunAccess.Token current(EntityPlayer player) {
        return player != null && player.worldObj == world
            && token != null
            && token.entityId == player.getEntityId()
            && token.slot == player.inventory.currentItem ? token : null;
    }
}
