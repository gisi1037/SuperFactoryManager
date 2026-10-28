package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataType;

public final class DataBoolExpr implements BoolExpr {

    private final DataExpression expression;

    public DataBoolExpr(DataExpression expression) {
        if (expression.getType() != DataType.BOOLEAN)
            throw new IllegalArgumentException("IF expression requires a boolean");
        this.expression = expression;
    }

    @Override
    public boolean evaluate(ProgramContext context) {
        return expression.evaluate(context.getDataRuntime())
            .asBoolean();
    }

    @Override
    public String toString() {
        return "(" + expression + ")";
    }
}
