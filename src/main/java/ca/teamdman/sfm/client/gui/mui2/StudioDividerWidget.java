package ca.teamdman.sfm.client.gui.mui2;

import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;

import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.widget.Widget;

/** Resizes views only; the source, graph layout and inventory allocation stay unchanged. */
final class StudioDividerWidget extends Widget<StudioDividerWidget> implements Interactable {

    private final BooleanSupplier available;
    private final IntConsumer move;
    private final boolean horizontal;
    private boolean dragging;

    StudioDividerWidget(BooleanSupplier available, IntConsumer move) {
        this(available, move, false, "Drag to resize Visual and Code. Source and node positions are preserved.");
    }

    StudioDividerWidget(BooleanSupplier available, IntConsumer move, boolean horizontal, String tooltip) {
        this.available = available;
        this.move = move;
        this.horizontal = horizontal;
        width(6);
        background(new Rectangle().color(0xff292e35));
        hoverBackground(new Rectangle().color(0xff9d6269));
        addTooltipLine(tooltip);
    }

    @Override
    public Result onMousePressed(int button) {
        if (button != 0 || !available.getAsBoolean()) return Result.IGNORE;
        dragging = true;
        return Result.SUCCESS;
    }

    @Override
    public void onMouseDrag(int button, long elapsed) {
        if (dragging && available.getAsBoolean())
            move.accept(horizontal ? getContext().getAbsMouseY() : getContext().getAbsMouseX());
    }

    @Override
    public boolean onMouseRelease(int button) {
        boolean wasDragging = dragging;
        dragging = false;
        return wasDragging;
    }
}
