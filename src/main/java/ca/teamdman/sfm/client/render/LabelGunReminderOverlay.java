package ca.teamdman.sfm.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import ca.teamdman.sfm.common.item.ItemLabelGun;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LabelGunReminderOverlay {

    @SubscribeEvent
    public void onRenderText(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings.showDebugInfo) return;
        EntityPlayer player = mc.thePlayer;
        if (player == null) return;

        ItemStack held = player.getHeldItem();
        if (held == null || !(held.getItem() instanceof ItemLabelGun)) {
            return;
        }

        ItemLabelGun.LabelGunViewMode mode = ItemLabelGun.getViewMode(held);
        if (mode == ItemLabelGun.LabelGunViewMode.SHOW_ALL) {
            return;
        }

        String msg;
        if (mode == ItemLabelGun.LabelGunViewMode.SHOW_ONLY_ACTIVE_LABEL_AND_TARGETED_BLOCK) {
            String active = ItemLabelGun.getActiveLabel(held);
            msg = EnumChatFormatting.YELLOW + "Showing blocks with active label "
                + (active.isEmpty() ? EnumChatFormatting.GRAY + "(none)"
                    : EnumChatFormatting.AQUA + "\"" + active + "\"")
                + EnumChatFormatting.YELLOW
                + " + targeted block. Cycle mode in GUI.";
        } else {
            msg = EnumChatFormatting.YELLOW + "Showing only targeted block labels. Cycle mode in GUI.";
        }

        FontRenderer font = mc.fontRenderer;
        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int x = res.getScaledWidth() / 2 - font.getStringWidth(msg) / 2;
        int y = 18;

        font.drawStringWithShadow(msg, x, y, 0xFFFFFF);
    }
}
