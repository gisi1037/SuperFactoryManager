package ca.teamdman.sfml.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.util.BlockPos;

public final class LabelAccess implements ASTNode {

    private final List<Label> labels;
    private final SideQualifier sides;
    private final NumberRangeSet slots;
    private final RoundRobin roundRobin;

    public LabelAccess(List<Label> labels, SideQualifier sides, NumberRangeSet slots, RoundRobin roundRobin) {
        this.labels = labels != null ? labels : Collections.emptyList();
        this.sides = sides != null ? sides : SideQualifier.DEFAULT;
        this.slots = slots != null ? slots : NumberRangeSet.ALL;
        this.roundRobin = roundRobin != null ? roundRobin : RoundRobin.DISABLED;
    }

    public List<Label> getLabels() {
        return labels;
    }

    public SideQualifier getSides() {
        return sides;
    }

    public NumberRangeSet getSlots() {
        return slots;
    }

    public RoundRobin getRoundRobin() {
        return roundRobin;
    }

    public List<BlockPos> getPositions(LabelPositionHolder holder) {
        if (holder == null || labels.isEmpty()) {
            return Collections.emptyList();
        }

        if (roundRobin.isEnabled() && roundRobin.getMode() == RoundRobin.Mode.BY_LABEL) {
            int selectedLabelIdx = roundRobin.nextIndex(labels.size());
            Label l = labels.get(selectedLabelIdx);
            return new ArrayList<>(holder.getPositions(l.getName()));
        }

        List<BlockPos> result = new ArrayList<>();
        for (Label l : labels) {
            result.addAll(holder.getPositions(l.getName()));
        }

        if (roundRobin.isEnabled() && roundRobin.getMode() == RoundRobin.Mode.BY_BLOCK && !result.isEmpty()) {
            int selectedBlockIdx = roundRobin.nextIndex(result.size());
            return Collections.singletonList(result.get(selectedBlockIdx));
        }

        return result;
    }
}
