package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;

public final class IfStatement implements Statement {

    private final BoolExpr condition;
    private final Block trueBlock;
    private final Block falseBlock;

    public IfStatement(BoolExpr condition, Block trueBlock, Block falseBlock) {
        this.condition = condition != null ? condition : BoolFalse.INSTANCE;
        this.trueBlock = trueBlock != null ? trueBlock : new Block(null);
        this.falseBlock = falseBlock;
    }

    @Override
    public void tick(ProgramContext context) {
        boolean result = condition.evaluate(context);
        if (result) {
            trueBlock.tick(context);
        } else if (falseBlock != null) {
            falseBlock.tick(context);
        }
    }

    public BoolExpr getCondition() {
        return condition;
    }

    public Block getTrueBlock() {
        return trueBlock;
    }

    public Block getFalseBlock() {
        return falseBlock;
    }
}
