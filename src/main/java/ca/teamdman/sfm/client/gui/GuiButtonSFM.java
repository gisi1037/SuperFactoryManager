package ca.teamdman.sfm.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;

/**
 * Height-aware and 9-patch slice rendering button for SFM GUIs.
 * Avoids cutting off bottom shadows and borders on buttons shorter than 20px (e.g. Reset button).
 */
public class GuiButtonSFM extends GuiButton {

    public GuiButtonSFM(int id, int x, int y, int width, int height, String text) {
        super(id, x, y, width, height, text);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!this.visible) return;

        FontRenderer fontrenderer = mc.fontRenderer;
        mc.getTextureManager().bindTexture(buttonTextures);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition
                && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
        int k = this.getHoverState(this.field_146123_n);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int halfW = this.width / 2;
        int remW = this.width - halfW;
        int halfH = this.height / 2;
        int remH = this.height - halfH;

        // Top-left
        this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + k * 20, halfW, halfH);
        // Top-right
        this.drawTexturedModalRect(this.xPosition + halfW, this.yPosition, 200 - remW, 46 + k * 20, remW, halfH);
        // Bottom-left (takes bottom border and shadow from texture!)
        this.drawTexturedModalRect(this.xPosition, this.yPosition + halfH, 0, 46 + k * 20 + (20 - remH), halfW, remH);
        // Bottom-right (takes bottom border and shadow from texture!)
        this.drawTexturedModalRect(this.xPosition + halfW, this.yPosition + halfH, 200 - remW, 46 + k * 20 + (20 - remH), remW, remH);

        this.mouseDragged(mc, mouseX, mouseY);
        int color = 14737632;

        if (packedFGColour != 0) {
            color = packedFGColour;
        } else if (!this.enabled) {
            color = 10526880;
        } else if (this.field_146123_n) {
            color = 16777120;
        }

        int textY = this.yPosition + (this.height - 8) / 2;
        this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, textY, color);
    }
}
