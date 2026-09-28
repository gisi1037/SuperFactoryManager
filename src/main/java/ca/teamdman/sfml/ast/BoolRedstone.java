package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;

public final class BoolRedstone implements BoolExpr {

    private final ComparisonOperator comparison;
    private final int targetValue;

    public BoolRedstone(ComparisonOperator comparison, int targetValue) {
        this.comparison = comparison != null ? comparison : ComparisonOperator.GREATER;
        this.targetValue = targetValue;
    }

    @Override
    public boolean evaluate(ProgramContext context) {
        int currentSignal = context.getRedstonePower();
        return comparison.test(currentSignal, targetValue);
    }

    public ComparisonOperator getComparison() {
        return comparison;
    }

    public int getTargetValue() {
        return targetValue;
    }

    @Override
    public String toString() {
        return "REDSTONE " + comparison + " " + targetValue;
    }
}
