package ca.teamdman.sfm.client.gui.mui2;

import net.minecraft.client.gui.Gui;

import com.cleanroommc.modularui.api.drawable.IDrawable;

/** Shared pixel-machine surfaces. Drawables are cached; rendering allocates no textures or blur buffers. */
final class StudioTheme {

    static final IDrawable SHELL = bevel(0xf518191d, 0xff454850, 0xff101215);
    static final IDrawable BUTTON = bevel(0xff34363c, 0xff62656d, 0xff131417);
    static final IDrawable HOVER = bevel(0xff464951, 0xff92969f, 0xff17181c);
    static final IDrawable PRIMARY = bevel(0xff843a42, 0xffc4787d, 0xff351c21);

    private StudioTheme() {}

    private static IDrawable bevel(int fill, int light, int dark) {
        return (context, x, y, width, height, theme) -> {
            Gui.drawRect(x, y, x + width, y + height, fill);
            Gui.drawRect(x, y, x + width, y + 1, light);
            Gui.drawRect(x, y, x + 1, y + height, light);
            Gui.drawRect(x, y + height - 1, x + width, y + height, dark);
            Gui.drawRect(x + width - 1, y, x + width, y + height, dark);
        };
    }
}
