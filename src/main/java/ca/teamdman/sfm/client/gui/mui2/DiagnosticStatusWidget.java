package ca.teamdman.sfm.client.gui.mui2;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.widgets.TextWidget;

/** Clickable existing status row, with no extra toolbar/footer space. */
public final class DiagnosticStatusWidget extends TextWidget<DiagnosticStatusWidget> implements Interactable {

    private final BooleanSupplier navigate;

    public DiagnosticStatusWidget(Supplier<String> display, Supplier<String> message, BooleanSupplier navigate) {
        super(IKey.dynamic(display));
        this.navigate = navigate;
        addTooltipLine(IKey.dynamic(message));
        addTooltipLine(IKey.str("Click or F4: go to the first error in the current draft."));
    }

    @Override
    public Result onMousePressed(int mouse) {
        return mouse == 0 && navigate.getAsBoolean() ? Result.SUCCESS : Result.IGNORE;
    }
}
