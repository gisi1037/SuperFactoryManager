package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataType;

public final class ResourceQuantity implements ASTNode {

    public static final ResourceQuantity UNLIMITED = new ResourceQuantity(Long.MAX_VALUE, false);
    public static final ResourceQuantity ZERO = new ResourceQuantity(0, false);
    private final long amount;
    private final boolean each;
    private final DataExpression expression;

    public ResourceQuantity(long amount, boolean each) {
        this.amount = amount;
        this.each = each;
        this.expression = null;
    }

    public ResourceQuantity(DataExpression expression, boolean each) {
        if (expression.getType() != DataType.INTEGER)
            throw new IllegalArgumentException("Quantity requires an integer");
        this.expression = expression;
        this.each = each;
        this.amount = 0;
    }

    public long getAmount() {
        if (expression != null) throw new IllegalStateException("Dynamic quantity requires an execution snapshot");
        return amount;
    }

    public boolean isEach() {
        return each;
    }

    public boolean isDynamic() {
        return expression != null;
    }

    public long evaluate(ProgramContext context) {
        long result = expression == null ? amount
            : expression.evaluate(context.getDataRuntime())
                .asLong();
        if (result < 0) throw new IllegalArgumentException("Quantity/threshold must be nonnegative: " + result);
        return result;
    }

    public ResourceQuantity resolve(ProgramContext context) {
        return expression == null ? this : new ResourceQuantity(evaluate(context), each);
    }

    @Override
    public String toString() {
        return (expression == null ? Long.toString(amount) : "(" + expression + ")") + (each ? " EACH" : "");
    }
}
