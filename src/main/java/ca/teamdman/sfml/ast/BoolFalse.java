package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;

public final class BoolFalse implements BoolExpr {

    public static final BoolFalse INSTANCE = new BoolFalse();

    @Override
    public boolean evaluate(ProgramContext context) {
        return false;
    }
}
