package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;

public final class BoolParen implements BoolExpr {

    private final BoolExpr inner;

    public BoolParen(BoolExpr inner) {
        this.inner = inner != null ? inner : BoolFalse.INSTANCE;
    }

    @Override
    public boolean evaluate(ProgramContext context) {
        return inner.evaluate(context);
    }

    @Override
    public String toString() {
        return "(" + inner + ")";
    }
}
