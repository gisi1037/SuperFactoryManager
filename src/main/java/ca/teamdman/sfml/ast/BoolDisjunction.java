package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;

public final class BoolDisjunction implements BoolExpr {

    private final BoolExpr left;
    private final BoolExpr right;

    public BoolDisjunction(BoolExpr left, BoolExpr right) {
        this.left = left != null ? left : BoolFalse.INSTANCE;
        this.right = right != null ? right : BoolFalse.INSTANCE;
    }

    @Override
    public boolean evaluate(ProgramContext context) {
        return context.evaluateCondition(left) || context.evaluateCondition(right);
    }

    public BoolExpr getLeft() {
        return left;
    }

    public BoolExpr getRight() {
        return right;
    }

    @Override
    public String toString() {
        return left + " OR " + right;
    }
}
