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
        if (context.getManager() != null && context.getManager()
            .getLogLevel()
            .includes(ca.teamdman.sfm.common.tileentity.LogLevel.DEBUG)) {
            context.log(
                ca.teamdman.sfm.common.tileentity.LogLevel.DEBUG,
                (result ? "TRUE: " : "FALSE: ") + condition.toString());
        }
        if (result) {
            trueBlock.tick(context);
        } else if (falseBlock != null) {
            falseBlock.tick(context);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("IF ")
            .append(condition)
            .append(" THEN\n");
        sb.append(trueBlock.toString())
            .append("\n");
        if (falseBlock != null && !falseBlock.getStatements()
            .isEmpty()) {
            sb.append("ELSE\n")
                .append(falseBlock.toString())
                .append("\n");
        }
        sb.append("END");
        return sb.toString();
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
