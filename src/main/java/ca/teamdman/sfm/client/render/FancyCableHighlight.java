package ca.teamdman.sfm.client.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;

import org.lwjgl.opengl.GL11;

import ca.teamdman.sfm.common.block.BlockFancyCable;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/** Vanilla draws one enclosing rectangle. Outline the actual cable pieces instead. */
public final class FancyCableHighlight {

    @SubscribeEvent
    public void draw(DrawBlockHighlightEvent event) {
        MovingObjectPosition hit = event.target;
        if (event.subID != 0 || hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;
        EntityPlayer player = event.player;
        World world = player.worldObj;
        if (!world.blockExists(hit.blockX, hit.blockY, hit.blockZ)) return;
        Block block = world.getBlock(hit.blockX, hit.blockY, hit.blockZ);
        if (!(block instanceof BlockFancyCable cable)) return;
        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks;
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks;
        GL11.glPushAttrib(
            GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT
                | GL11.GL_CURRENT_BIT
                | GL11.GL_DEPTH_BUFFER_BIT
                | GL11.GL_LINE_BIT);
        try {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(0, 0, 0, .4F);
            GL11.glLineWidth(2);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDepthMask(false);
            for (int part = 0; part <= 6; part++) {
                AxisAlignedBB bounds = cable.getPartBounds(world, hit.blockX, hit.blockY, hit.blockZ, part);
                if (bounds != null) RenderGlobal.drawOutlinedBoundingBox(
                    bounds.expand(.002, .002, .002)
                        .getOffsetBoundingBox(-x, -y, -z),
                    -1);
            }
        } finally {
            GL11.glPopAttrib();
        }
        event.setCanceled(true);
    }
}
