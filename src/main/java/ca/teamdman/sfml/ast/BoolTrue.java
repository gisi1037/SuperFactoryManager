package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;

public final class BoolTrue implements BoolExpr {

    public static final BoolTrue INSTANCE = new BoolTrue();

    @Override
    public boolean evaluate(ProgramContext context) {
        return true;
    }

    @Override
    public String toString() {
        return "TRUE";
    }
}
