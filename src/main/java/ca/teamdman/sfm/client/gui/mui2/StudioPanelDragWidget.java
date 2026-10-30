package ca.teamdman.sfm.client.gui.mui2;

import com.cleanroommc.modularui.api.widget.IFocusedWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.sizer.Unit;

/** A modal title-bar handle; pointer and panel positions share Studio's logical scale. */
final class StudioPanelDragWidget extends Widget<StudioPanelDragWidget> implements Interactable, IFocusedWidget {

    private boolean dragging, positioned;
    private int clickX, clickY, startX, startY, requestedX, requestedY;

    StudioPanelDragWidget() {
        addTooltipLine("Drag the title bar to move this panel.");
    }

    @Override
    public boolean isFocused() {
        return isValid() && getContext().isFocused(this);
    }

    @Override
    public void onFocus(ModularGuiContext context) {}

    @Override
    public void onRemoveFocus(ModularGuiContext context) {
        dragging = false;
    }

    @Override
    public Result onMousePressed(int button) {
        if (button != 0) return Result.IGNORE;
        // Focus delivers release even when a clamped panel leaves the pointer outside its title bar.
        getContext().focus(this);
        ModularPanel panel = getPanel();
        startX = requestedX = panel.getArea().x;
        startY = requestedY = panel.getArea().y;
        clickX = StudioScaledPanel.logicalMouse(panel, getContext().getAbsMouseX());
        clickY = StudioScaledPanel.logicalMouse(panel, getContext().getAbsMouseY());
        dragging = true;
        return Result.SUCCESS;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> theme) {
        updateDrag();
    }

    @Override
    public void onMouseDrag(int button, long elapsed) {
        updateDrag();
    }

    private void updateDrag() {
        if (!dragging) return;
        ModularPanel panel = getPanel();
        int x = startX + StudioScaledPanel.logicalMouse(panel, getContext().getAbsMouseX()) - clickX;
        int y = startY + StudioScaledPanel.logicalMouse(panel, getContext().getAbsMouseY()) - clickY;
        if (x == requestedX && y == requestedY) return;
        requestedX = x;
        requestedY = y;
        if (!positioned) {
            // Keep the moved dialog on-screen after a window or Studio-scale change too.
            panel.left(
                () -> Math.max(
                    0,
                    Math.min(requestedX, StudioScaledPanel.logicalWidth(panel.getScreen()) - panel.getArea().width)),
                Unit.Measure.PIXEL);
            panel.top(
                () -> Math.max(
                    0,
                    Math.min(requestedY, StudioScaledPanel.logicalHeight(panel.getScreen()) - panel.getArea().height)),
                Unit.Measure.PIXEL);
            positioned = true;
        }
        panel.scheduleResize();
    }

    @Override
    public boolean onMouseRelease(int button) {
        updateDrag();
        dragging = false;
        return false;
    }
}
