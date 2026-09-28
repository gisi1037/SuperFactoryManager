package ca.teamdman.sfml.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NumberRangeSet implements ASTNode {

    public static final NumberRangeSet ALL = new NumberRangeSet(Collections.singletonList(NumberRange.MAX_RANGE));

    private final List<NumberRange> ranges;

    public NumberRangeSet(List<NumberRange> ranges) {
        this.ranges = ranges != null ? ranges : Collections.emptyList();
    }

    public List<NumberRange> getRanges() {
        return ranges;
    }

    public boolean contains(int slot) {
        if (ranges.isEmpty()) {
            return true;
        }
        for (NumberRange range : ranges) {
            if (range.contains(slot)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAll() {
        return ranges.isEmpty() || (ranges.size() == 1 && ranges.get(0)
            .equals(NumberRange.MAX_RANGE));
    }

    @Override
    public String toString() {
        if (isAll()) {
            return "all";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ranges.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(ranges.get(i));
        }
        return sb.toString();
    }

    public static NumberRangeSet of(NumberRange... ranges) {
        List<NumberRange> list = new ArrayList<>();
        Collections.addAll(list, ranges);
        return new NumberRangeSet(list);
    }
}
