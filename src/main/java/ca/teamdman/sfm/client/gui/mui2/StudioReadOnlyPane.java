package ca.teamdman.sfm.client.gui.mui2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.widget.Widget;

/** Cached bounded output. Resizing or receiving new data trims once; drawing does not rebuild text or rows. */
final class StudioReadOnlyPane extends Widget<StudioReadOnlyPane> implements Interactable {

    private List<String> lines = Collections.emptyList(), display = Collections.emptyList();
    private int cachedWidth = -1, first, column;
    private final BooleanSupplier input;

    StudioReadOnlyPane(BooleanSupplier input) {
        this.input = input;
        addTooltipLine("Wheel: scroll. Shift + wheel: horizontal scroll. Copy preserves complete lines.");
    }

    void lines(List<String> value) {
        if (value.equals(lines)) return;
        lines = new ArrayList<>(value.subList(0, Math.min(512, value.size())));
        cachedWidth = -1;
        first = Math.min(first, Math.max(0, lines.size() - 1));
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> theme) {
        int width = getArea().width, height = getArea().height;
        if (cachedWidth != width) {
            List<String> clipped = new ArrayList<>(lines.size());
            for (String line : lines) clipped.add(
                Minecraft.getMinecraft().fontRenderer
                    .trimStringToWidth(line.substring(Math.min(column, line.length())), Math.max(1, width - 10)));
            display = clipped;
            cachedWidth = width;
        }
        Gui.drawRect(0, 0, width, height, 0xff15171b);
        Stencil.apply(0, 0, width, height, context);
        try {
            Platform.setupDrawFont();
            for (int row = first, y = 4; row < display.size() && y + 9 <= height; row++, y += 11)
                Minecraft.getMinecraft().fontRenderer.drawString(display.get(row), 4, y, 0xffd9dde3);
        } finally {
            Stencil.remove();
        }
        if (lines.size() * 11 > height) {
            int marker = Math.max(3, height * Math.max(1, height / 11) / Math.max(1, lines.size()));
            int y = Math.min(height - marker, first * (height - marker) / Math.max(1, lines.size() - 1));
            Gui.drawRect(width - 3, y, width - 1, y + marker, 0xff98616a);
        }
    }

    @Override
    public boolean onMouseScroll(UpOrDown direction, int amount) {
        if (!isHovering() || !input.getAsBoolean()) return false;
        int delta = direction.isUp() ? -3 : 3;
        if (GuiScreen.isShiftKeyDown()) {
            column = Math.max(0, Math.min(4096, column + delta));
            cachedWidth = -1;
        } else first = Math
            .max(0, Math.min(Math.max(0, lines.size() - Math.max(1, getArea().height / 11)), first + delta));
        return true;
    }
}
