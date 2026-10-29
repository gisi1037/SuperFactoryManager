package ca.teamdman.sfm.client.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import ca.teamdman.sfm.common.item.ItemLabelGun;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.net.PacketSetLabel;
import ca.teamdman.sfm.common.net.SFMNetwork;
import ca.teamdman.sfm.common.util.BlockPos;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientItemWorldRenderer {

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (event.dwheel == 0) return;
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;
        if (player == null) return;
        ca.teamdman.sfm.common.net.LabelGunAccess.Token token = ca.teamdman.sfm.client.gui.ClientLabelGunSession
            .current(player);
        if (token == null) return;

        ItemStack held = player.getHeldItem();
        if (held != null && held.getItem() instanceof ItemLabelGun && player.isSneaking()) {
            int dir = event.dwheel > 0 ? 1 : -1;
            String next = ItemLabelGun.getNextLabel(held, dir);
            if (!next.isEmpty()) {
                ItemLabelGun.setActiveLabel(held, next);
                SFMNetwork.WRAPPER.sendToServer(new PacketSetLabel(next, token));
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onKeyInput(cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null) return;
        EntityPlayer player = mc.thePlayer;
        if (player == null) return;
        ca.teamdman.sfm.common.net.LabelGunAccess.Token token = ca.teamdman.sfm.client.gui.ClientLabelGunSession
            .current(player);
        if (token == null) return;

        ItemStack held = player.getHeldItem();
        if (held == null || !(held.getItem() instanceof ItemLabelGun)) return;

        if (Keyboard.getEventKeyState()) {
            int key = Keyboard.getEventKey();
            if (key == Keyboard.KEY_V) {
                ItemLabelGun.cycleViewMode(held);
                SFMNetwork.WRAPPER.sendToServer(
                    new ca.teamdman.sfm.common.net.PacketLabelGunAction(
                        ca.teamdman.sfm.common.net.PacketLabelGunAction.ACTION_CYCLE_VIEW,
                        token));
            } else if (key == Keyboard.KEY_G || key == Keyboard.KEY_L) {
                ca.teamdman.sfm.client.gui.mui2.Mui2LabelGun.open(player, held);
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.thePlayer;
        if (player == null) return;

        ItemStack held = player.getHeldItem();
        if (held == null || !(held.getItem() instanceof ItemLabelGun)) {
            return;
        }

        ItemLabelGun.LabelGunViewMode viewMode = ItemLabelGun.getViewMode(held);
        String activeLabel = ItemLabelGun.getActiveLabel(held);
        LabelPositionHolder holder = LabelPositionHolder.from(held);

        MovingObjectPosition mop = mc.objectMouseOver;
        BlockPos lookingAt = null;
        if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            lookingAt = new BlockPos(mop.blockX, mop.blockY, mop.blockZ);
        }

        // Collect positions to labels
        Map<BlockPos, List<String>> posToLabels = new HashMap<>();

        if (viewMode == ItemLabelGun.LabelGunViewMode.SHOW_ALL) {
            for (Map.Entry<String, Set<BlockPos>> entry : holder.getAllLabels()
                .entrySet()) {
                String lbl = entry.getKey();
                for (BlockPos p : entry.getValue()) {
                    posToLabels.computeIfAbsent(p, k -> new ArrayList<>())
                        .add(lbl);
                }
            }
        } else if (viewMode == ItemLabelGun.LabelGunViewMode.SHOW_ONLY_ACTIVE_LABEL_AND_TARGETED_BLOCK) {
            if (!activeLabel.isEmpty()) {
                for (BlockPos p : holder.getPositions(activeLabel)) {
                    posToLabels.computeIfAbsent(p, k -> new ArrayList<>())
                        .add(activeLabel);
                }
            }
            if (lookingAt != null) {
                Set<String> blockLabels = holder.getLabelsForPosition(lookingAt);
                for (String lbl : blockLabels) {
                    List<String> list = posToLabels.computeIfAbsent(lookingAt, k -> new ArrayList<>());
                    if (!list.contains(lbl)) {
                        list.add(lbl);
                    }
                }
            }
        } else if (viewMode == ItemLabelGun.LabelGunViewMode.SHOW_ONLY_TARGETED_BLOCK) {
            if (lookingAt != null) {
                Set<String> blockLabels = holder.getLabelsForPosition(lookingAt);
                for (String lbl : blockLabels) {
                    posToLabels.computeIfAbsent(lookingAt, k -> new ArrayList<>())
                        .add(lbl);
                }
            }
        }

        if (posToLabels.isEmpty()) return;

        double camX = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks;
        double camY = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks;
        double camZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks;

        // 1. Draw translucent bounding boxes around labeled blocks (visible through walls)
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);

        for (Map.Entry<BlockPos, List<String>> entry : posToLabels.entrySet()) {
            BlockPos p = entry.getKey();
            boolean hasActive = entry.getValue()
                .contains(activeLabel);

            float r = hasActive ? 0.0f : 0.8f;
            float g = hasActive ? 0.8f : 0.2f;
            float b = hasActive ? 1.0f : 0.8f;
            float alpha = 0.25f;

            double minX = p.x - camX;
            double minY = p.y - camY;
            double minZ = p.z - camZ;
            double maxX = minX + 1.0;
            double maxY = minY + 1.0;
            double maxZ = minZ + 1.0;

            renderBoundingBoxFilled(minX, minY, minZ, maxX, maxY, maxZ, r, g, b, alpha);
            renderBoundingBoxOutline(minX, minY, minZ, maxX, maxY, maxZ, r, g, b, 0.7f);
        }

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();

        // 2. Draw floating billboard text labels
        FontRenderer font = mc.fontRenderer;
        for (Map.Entry<BlockPos, List<String>> entry : posToLabels.entrySet()) {
            BlockPos p = entry.getKey();
            List<String> labels = entry.getValue();

            GL11.glPushMatrix();
            GL11.glTranslated(p.x + 0.5 - camX, p.y + 0.6 - camY, p.z + 0.5 - camZ);
            GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
            GL11.glScalef(-0.025F, -0.025F, 0.025F);

            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

            int totalHeight = labels.size() * (font.FONT_HEIGHT + 2);
            int startY = -totalHeight / 2;

            for (int i = 0; i < labels.size(); i++) {
                String lbl = labels.get(i);
                int strWidth = font.getStringWidth(lbl);
                int y = startY + i * (font.FONT_HEIGHT + 2);

                // Dark background pill
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                Tessellator tess = Tessellator.instance;
                tess.startDrawingQuads();
                tess.setColorRGBA_F(0.0f, 0.0f, 0.0f, 0.6f);
                tess.addVertex(-strWidth / 2.0 - 3, y - 1, 0);
                tess.addVertex(-strWidth / 2.0 - 3, y + font.FONT_HEIGHT, 0);
                tess.addVertex(strWidth / 2.0 + 3, y + font.FONT_HEIGHT, 0);
                tess.addVertex(strWidth / 2.0 + 3, y - 1, 0);
                tess.draw();
                GL11.glEnable(GL11.GL_TEXTURE_2D);

                int color = lbl.equals(activeLabel) ? 0x55FFFF : 0xFFFFFF;
                font.drawStringWithShadow(lbl, -strWidth / 2, y, color);
            }

            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();
        }
    }

    private void renderBoundingBoxFilled(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
        float r, float g, float b, float a) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setColorRGBA_F(r, g, b, a);

        // Bottom
        t.addVertex(minX, minY, minZ);
        t.addVertex(maxX, minY, minZ);
        t.addVertex(maxX, minY, maxZ);
        t.addVertex(minX, minY, maxZ);

        // Top
        t.addVertex(minX, maxY, maxZ);
        t.addVertex(maxX, maxY, maxZ);
        t.addVertex(maxX, maxY, minZ);
        t.addVertex(minX, maxY, minZ);

        // North
        t.addVertex(minX, minY, minZ);
        t.addVertex(minX, maxY, minZ);
        t.addVertex(maxX, maxY, minZ);
        t.addVertex(maxX, minY, minZ);

        // South
        t.addVertex(maxX, minY, maxZ);
        t.addVertex(maxX, maxY, maxZ);
        t.addVertex(minX, maxY, maxZ);
        t.addVertex(minX, minY, maxZ);

        // West
        t.addVertex(minX, minY, maxZ);
        t.addVertex(minX, maxY, maxZ);
        t.addVertex(minX, maxY, minZ);
        t.addVertex(minX, minY, minZ);

        // East
        t.addVertex(maxX, minY, minZ);
        t.addVertex(maxX, maxY, minZ);
        t.addVertex(maxX, maxY, maxZ);
        t.addVertex(maxX, minY, maxZ);

        t.draw();
    }

    private void renderBoundingBoxOutline(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
        float r, float g, float b, float a) {
        GL11.glLineWidth(2.0f);
        Tessellator t = Tessellator.instance;
        t.startDrawing(GL11.GL_LINES);
        t.setColorRGBA_F(r, g, b, a);

        // Bottom 4 lines
        t.addVertex(minX, minY, minZ);
        t.addVertex(maxX, minY, minZ);
        t.addVertex(maxX, minY, minZ);
        t.addVertex(maxX, minY, maxZ);
        t.addVertex(maxX, minY, maxZ);
        t.addVertex(minX, minY, maxZ);
        t.addVertex(minX, minY, maxZ);
        t.addVertex(minX, minY, minZ);

        // Top 4 lines
        t.addVertex(minX, maxY, minZ);
        t.addVertex(maxX, maxY, minZ);
        t.addVertex(maxX, maxY, minZ);
        t.addVertex(maxX, maxY, maxZ);
        t.addVertex(maxX, maxY, maxZ);
        t.addVertex(minX, maxY, maxZ);
        t.addVertex(minX, maxY, maxZ);
        t.addVertex(minX, maxY, minZ);

        // Vertical 4 lines
        t.addVertex(minX, minY, minZ);
        t.addVertex(minX, maxY, minZ);
        t.addVertex(maxX, minY, minZ);
        t.addVertex(maxX, maxY, minZ);
        t.addVertex(maxX, minY, maxZ);
        t.addVertex(maxX, maxY, maxZ);
        t.addVertex(minX, minY, maxZ);
        t.addVertex(minX, maxY, maxZ);

        t.draw();
    }
}
