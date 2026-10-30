package ca.teamdman.sfm.integration.mui2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.widget.AbstractScrollWidget;
import com.cleanroommc.modularui.widget.scroll.HorizontalScrollData;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.VoidWidget;

/** Read-only, clipped, wheel/scrollbar output. Rewrap only on content or width changes. */
final class ConsoleOutputWidget extends AbstractScrollWidget<VoidWidget, ConsoleOutputWidget> {

    private List<String> lines = Collections.emptyList();
    private final List<String> rows = new ArrayList<>();
    private int measuredWidth = -1;
    private boolean dirty = true, wrap = true, follow = true, scrollToEnd;

    ConsoleOutputWidget() {
        super(new HorizontalScrollData(false, 6), new VerticalScrollData(false, 6));
        background(new Rectangle().color(0xff11151a));
        name("manager_diagnostic_output");
    }

    void lines(List<String> content) {
        if (lines.equals(content)) return;
        lines = new ArrayList<>(content);
        dirty = true;
        scrollToEnd = follow;
    }

    void wrap(boolean enabled) {
        if (wrap != enabled) dirty = true;
        wrap = enabled;
    }

    void follow(boolean enabled) {
        follow = enabled;
        scrollToEnd = enabled;
    }

    boolean follows() {
        return follow;
    }

    @Override
    public boolean onMouseScroll(UpOrDown direction, int amount) {
        boolean consumed = super.onMouseScroll(direction, amount);
        if (consumed) follow = false;
        return consumed;
    }

    @Override
    public void onMouseDrag(int button, long elapsed) {
        follow = false;
        super.onMouseDrag(button, elapsed);
    }

    @Override
    public void preDraw(ModularGuiContext context, boolean transformed) {
        super.preDraw(context, transformed);
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        if (transformed) {
            Platform.setupDrawFont();
            int first = Math.max(0, getScrollY() / 10);
            int end = Math.min(rows.size(), first + getArea().height / 10 + 2);
            for (int i = first; i < end; i++) font.drawString(rows.get(i), 4, 3 + i * 10, 0xffd7dce3);
            return;
        }
        // Maintain extents before MUI2 applies this frame's scroll transform.
        int width = Math.max(8, getArea().width - 14);
        if (dirty || measuredWidth != width) {
            measuredWidth = width;
            dirty = false;
            rows.clear();
            int widest = 0;
            for (String line : lines) {
                if (rows.size() >= 4096) break;
                String clipped = line.length() > 2048 ? line.substring(0, 2048) : line;
                widest = Math.max(widest, font.getStringWidth(clipped));
                if (wrap) {
                    for (String row : font.listFormattedStringToWidth(clipped, width)) {
                        if (rows.size() == 4096) break;
                        rows.add(row);
                    }
                } else rows.add(clipped);
            }
            getScrollArea().getScrollY()
                .setScrollSize(rows.size() * 10 + 6);
            getScrollArea().getScrollX()
                .setScrollSize(wrap ? 0 : widest + 14);
        }
        getScrollArea().getScrollX()
            .clamp(getScrollArea());
        getScrollArea().getScrollY()
            .clamp(getScrollArea());
        if (scrollToEnd) {
            getScrollArea().getScrollY()
                .scrollTo(getScrollArea(), Integer.MAX_VALUE);
            scrollToEnd = false;
        }
    }
}
