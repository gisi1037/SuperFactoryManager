package ca.teamdman.sfm.client.gui.mui2;

import net.minecraft.client.gui.Gui;

import com.cleanroommc.modularui.api.drawable.IDrawable;

/** Shared pixel-machine surfaces. Drawables are cached; rendering allocates no textures or blur buffers. */
final class StudioTheme {

    static final IDrawable SHELL = bevel(0xf5191a1e, 0xff454850, 0xff101215);
    static final IDrawable PANEL = bevel(0xff24262b, 0xff454850, 0xff111216);
    static final IDrawable INSET = bevel(0xff17191d, 0xff111216, 0xff42454b);
    static final IDrawable TAB = bevel(0xff303238, 0xff595c63, 0xff121317);
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
            if (width > 6 && height > 6) {
                Gui.drawRect(x + 1, y + 1, x + width - 1, y + 2, light);
                Gui.drawRect(x + 1, y + 2, x + 2, y + height - 2, light);
                Gui.drawRect(x + 2, y + height - 2, x + width - 1, y + height - 1, dark);
                Gui.drawRect(x + width - 2, y + 2, x + width - 1, y + height - 2, dark);
            }
        };
    }
}
