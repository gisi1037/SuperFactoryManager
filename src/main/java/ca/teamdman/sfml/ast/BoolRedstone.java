package ca.teamdman.sfml.ast;

import java.util.ArrayList;
import java.util.List;

import ca.teamdman.sfm.common.program.ProgramContext;

public final class BoolRedstone implements BoolExpr {

    private final ComparisonOperator comparison;
    private final int targetValue;
    private final ResourceQuantity dynamicTarget;
    // null for the unlabelled form, which reads the manager's own input and the network's receivers
    private final LabelAccess labelAccess;
    private final SetOperator setOperator;

    public BoolRedstone(ComparisonOperator comparison, int targetValue) {
        this(null, null, comparison, targetValue, null);
    }

    public BoolRedstone(ComparisonOperator comparison, ResourceQuantity target) {
        this(null, null, comparison, 0, target);
    }

    public BoolRedstone(SetOperator setOperator, LabelAccess labelAccess, ComparisonOperator comparison,
        int targetValue) {
        this(setOperator, labelAccess, comparison, targetValue, null);
    }

    public BoolRedstone(SetOperator setOperator, LabelAccess labelAccess, ComparisonOperator comparison,
        ResourceQuantity target) {
        this(setOperator, labelAccess, comparison, 0, target);
    }

    private BoolRedstone(SetOperator setOperator, LabelAccess labelAccess, ComparisonOperator comparison,
        int targetValue, ResourceQuantity dynamicTarget) {
        this.setOperator = setOperator != null ? setOperator : SetOperator.OVERALL;
        this.labelAccess = labelAccess;
        this.comparison = comparison != null ? comparison : ComparisonOperator.GREATER;
        this.targetValue = targetValue;
        this.dynamicTarget = dynamicTarget;
    }

    @Override
    public boolean evaluate(ProgramContext context) {
        long target = dynamicTarget == null ? targetValue : dynamicTarget.evaluate(context);
        if (labelAccess == null) {
            int currentSignal = context.getRedstonePower();
            return comparison.test(currentSignal, target);
        }

        return test(context.labelledRedstone(labelAccess), target);
    }

    /** {@link #test(List, long)} against this condition's constant target. */
    public boolean test(List<Integer> levels) {
        return test(levels, getTargetValue());
    }

    /**
     * Applies the comparison and set operator to the redstone levels read from each labelled block.
     * OVERALL compares the strongest level; the other set operators compare each block's level on its own.
     */
    public boolean test(List<Integer> levels, long target) {
        int max = 0;
        List<Boolean> satisfactionResults = new ArrayList<>(levels.size());
        for (int level : levels) {
            if (level > max) max = level;
            satisfactionResults.add(comparison.test(level, target));
        }
        boolean overallSatisfied = comparison.test(max, target);
        return setOperator.test(overallSatisfied, satisfactionResults);
    }

    public ComparisonOperator getComparison() {
        return comparison;
    }

    public int getTargetValue() {
        if (dynamicTarget != null) throw new IllegalStateException("Dynamic redstone condition requires execution");
        return targetValue;
    }

    public LabelAccess getLabelAccess() {
        return labelAccess;
    }

    public SetOperator getSetOperator() {
        return setOperator;
    }

    @Override
    public String toString() {
        String condition = "REDSTONE " + comparison.getSymbol()
            + " "
            + (dynamicTarget == null ? Integer.toString(targetValue) : dynamicTarget.toString());
        return labelAccess == null ? condition : setOperator + " " + labelAccess + " " + condition;
    }
}
