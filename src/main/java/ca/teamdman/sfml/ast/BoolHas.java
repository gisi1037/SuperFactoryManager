package ca.teamdman.sfml.ast;

import java.util.ArrayList;
import java.util.List;

import ca.teamdman.sfm.common.inventory.InventoryUtils;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.util.BlockPos;

public final class BoolHas implements BoolExpr {

    private final SetOperator setOperator;
    private final LabelAccess labelAccess;
    private final ComparisonOperator comparisonOperator;
    private final long quantity;
    private final ResourceIdSet resourceIds;
    private final With with;
    private final ResourceIdSet except;

    public BoolHas(SetOperator setOperator, LabelAccess labelAccess, ComparisonOperator comparisonOperator,
        long quantity, ResourceIdSet resourceIds, With with, ResourceIdSet except) {
        this.setOperator = setOperator != null ? setOperator : SetOperator.OVERALL;
        this.labelAccess = labelAccess;
        this.comparisonOperator = comparisonOperator != null ? comparisonOperator : ComparisonOperator.GREATER;
        this.quantity = quantity;
        this.resourceIds = resourceIds != null ? resourceIds : ResourceIdSet.MATCH_ALL;
        this.with = with != null ? with : With.ALWAYS_TRUE;
        this.except = except;
    }

    @Override
    public boolean evaluate(ProgramContext context) {
        if (labelAccess == null) return false;

        List<BlockPos> positions = labelAccess.getPositions(context.getLabelPositionHolder());
        long overallCount = 0;
        List<Boolean> satisfactionResults = new ArrayList<>();

        for (BlockPos pos : positions) {
            long inThisBlock = 0;
            if (resourceIds.hasItemType()) {
                inThisBlock += InventoryUtils.countMatchingItems(
                    context.getWorld(),
                    pos,
                    labelAccess.getSides(),
                    labelAccess.getSlots(),
                    resourceIds,
                    with,
                    except);
            }
            if (resourceIds.hasFluidType()) {
                inThisBlock += InventoryUtils
                    .countMatchingFluids(context.getWorld(), pos, labelAccess.getSides(), resourceIds, except);
            }

            overallCount += inThisBlock;
            satisfactionResults.add(comparisonOperator.test(inThisBlock, quantity));
        }

        boolean overallSatisfied = comparisonOperator.test(overallCount, quantity);
        return setOperator.test(overallSatisfied, satisfactionResults);
    }

    public SetOperator getSetOperator() {
        return setOperator;
    }

    public LabelAccess getLabelAccess() {
        return labelAccess;
    }

    public ComparisonOperator getComparisonOperator() {
        return comparisonOperator;
    }

    public long getQuantity() {
        return quantity;
    }

    public ResourceIdSet getResourceIds() {
        return resourceIds;
    }

    public With getWith() {
        return with;
    }

    public ResourceIdSet getExcept() {
        return except;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (setOperator != null) {
            sb.append(setOperator).append(" ");
        }
        if (labelAccess != null) {
            sb.append(labelAccess).append(" ");
        }
        sb.append("HAS");
        if (comparisonOperator != null) {
            sb.append(" ").append(comparisonOperator.getSymbol());
        }
        sb.append(" ").append(quantity);
        if (resourceIds != null && resourceIds != ResourceIdSet.MATCH_ALL) {
            sb.append(" ").append(resourceIds);
        }
        if (with != null && with != With.ALWAYS_TRUE) {
            sb.append(" ").append(with);
        }
        if (except != null) {
            sb.append(" EXCEPT ").append(except);
        }
        return sb.toString();
    }
}
