package ca.teamdman.sfm.integration.mui2;

import java.util.function.Supplier;

import com.cleanroommc.modularui.widget.ParentWidget;

import ca.teamdman.sfm.common.gui.ManagerControlToken;

/** Client-local navigation. Every page shares the same equipment and stock inventory. */
final class ManagerConsoleBody extends ParentWidget<ManagerConsoleBody> {

    private int page;
    private Supplier<ManagerControlToken> token = () -> null;

    void control(Supplier<ManagerControlToken> source) {
        token = source;
    }

    ManagerControlToken token() {
        return token.get();
    }

    int page() {
        return page;
    }

    void select(int selected) {
        if (selected >= 0 && selected < 7) page = selected;
    }
}
