package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;

public final class BoolRedstone implements BoolExpr {

    private final ComparisonOperator comparison;
    private final int targetValue;
    private final ResourceQuantity dynamicTarget;

    public BoolRedstone(ComparisonOperator comparison, int targetValue) {
        this.comparison = comparison != null ? comparison : ComparisonOperator.GREATER;
        this.targetValue = targetValue;
        this.dynamicTarget = null;
    }

    public BoolRedstone(ComparisonOperator comparison, ResourceQuantity target) {
        this.comparison = comparison != null ? comparison : ComparisonOperator.GREATER;
        this.targetValue = 0;
        this.dynamicTarget = target;
    }

    @Override
    public boolean evaluate(ProgramContext context) {
        long target = dynamicTarget == null ? targetValue : dynamicTarget.evaluate(context);
        int currentSignal = context.getRedstonePower();
        return comparison.test(currentSignal, target);
    }

    public ComparisonOperator getComparison() {
        return comparison;
    }

    public int getTargetValue() {
        if (dynamicTarget != null) throw new IllegalStateException("Dynamic redstone condition requires execution");
        return targetValue;
    }

    @Override
    public String toString() {
        return "REDSTONE " + comparison
            + " "
            + (dynamicTarget == null ? Integer.toString(targetValue) : dynamicTarget.toString());
    }
}
