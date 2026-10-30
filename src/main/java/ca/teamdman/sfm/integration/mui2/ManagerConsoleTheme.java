package ca.teamdman.sfm.integration.mui2;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.drawable.GuiDraw;

/** Cached native machine surfaces, sharing Studio's charcoal and muted red palette. */
final class ManagerConsoleTheme {

    static final IDrawable SHELL = bevel(0xf518191d, 0xff454850, 0xff101215);
    static final IDrawable BUTTON = bevel(0xff34363c, 0xff62656d, 0xff131417);
    static final IDrawable HOVER = bevel(0xff464951, 0xff92969f, 0xff17181c);
    static final IDrawable PRIMARY = bevel(0xff843a42, 0xffc4787d, 0xff351c21);

    private ManagerConsoleTheme() {}

    private static IDrawable bevel(int fill, int light, int dark) {
        return (context, x, y, width, height, theme) -> {
            GuiDraw.drawRect(x, y, width, height, fill);
            GuiDraw.drawRect(x, y, width, 1, light);
            GuiDraw.drawRect(x, y, 1, height, light);
            GuiDraw.drawRect(x, y + height - 1, width, 1, dark);
            GuiDraw.drawRect(x + width - 1, y, 1, height, dark);
            if (width > 6 && height > 6) {
                GuiDraw.drawRect(x + 1, y + 1, width - 2, 1, light);
                GuiDraw.drawRect(x + 1, y + 2, 1, height - 4, light);
                GuiDraw.drawRect(x + 2, y + height - 2, width - 3, 1, dark);
                GuiDraw.drawRect(x + width - 2, y + 2, 1, height - 4, dark);
            }
        };
    }
}
