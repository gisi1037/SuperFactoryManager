package ca.teamdman.sfml.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraftforge.common.util.ForgeDirection;

import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.util.BlockPos;

public final class LabelAccess implements ASTNode {

    private final List<Label> labels;
    private final SideQualifier sides;
    private final NumberRangeSet slots;
    private final RoundRobin roundRobin;
    private final boolean qualified;
    private Label selectedLabel;
    private static final NumberRangeSet NO_SLOTS = NumberRangeSet.of(new NumberRange(-1, -1));

    public LabelAccess(List<Label> labels, SideQualifier sides, NumberRangeSet slots, RoundRobin roundRobin) {
        this.labels = labels != null ? Collections.unmodifiableList(new ArrayList<>(labels)) : Collections.emptyList();
        this.sides = sides == null || sides == SideQualifier.DEFAULT ? SideQualifier.DEFAULT
            : new SideQualifier(Collections.unmodifiableList(new ArrayList<>(sides.getSides())));
        this.slots = slots == null || slots == NumberRangeSet.ALL ? NumberRangeSet.ALL
            : new NumberRangeSet(Collections.unmodifiableList(new ArrayList<>(slots.getRanges())));
        this.roundRobin = roundRobin != null ? roundRobin : RoundRobin.DISABLED;
        boolean restricted = false;
        for (Label label : this.labels) restricted |= label.isDynamic() || label.getReference()
            .getSideMask() != 63
            || label.getReference()
                .hasSlotSelection();
        qualified = restricted;
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

    public boolean isDynamic() {
        for (Label label : labels) if (label.isDynamic()) return true;
        return false;
    }

    /** Resolve before selection or mutation. Registered inputs retain these immutable handles. */
    public LabelAccess resolve(ProgramContext context) {
        if (!isDynamic() && !(qualified && roundRobin.isEnabled())) return this;
        List<Label> resolved = new ArrayList<>(labels.size());
        for (Label label : labels) resolved.add(label.resolve(context));
        // Selection remains lazy: registering INPUT must not consume a round-robin turn.
        return new LabelAccess(resolved, sides, slots, roundRobin);
    }

    /** FORGET must not turn an unselected alias into a new round-robin selection. */
    public LabelAccess keeping(List<Label> remaining) {
        if (selectedLabel != null && !remaining.contains(selectedLabel))
            return new LabelAccess(Collections.emptyList(), sides, slots, RoundRobin.DISABLED);
        LabelAccess result = new LabelAccess(remaining, sides, slots, roundRobin);
        result.selectedLabel = selectedLabel;
        return result;
    }

    private boolean includes(Label label, BlockPos position, LabelPositionHolder holder) {
        if (selectedLabel != null && label != selectedLabel) return false;
        return holder.getPositions(label.getName())
            .contains(position);
    }

    private boolean permits(Label label, ForgeDirection side) {
        int mask = label.getReference()
            .getSideMask();
        return side == ForgeDirection.UNKNOWN ? mask == 63 : (mask & 1 << side.ordinal()) != 0;
    }

    /** Preserve each handle's side restriction; do not take a cross product of label qualifiers. */
    public SideQualifier getSides(BlockPos position, LabelPositionHolder holder) {
        if (!qualified) return sides;
        List<Side> allowed = new ArrayList<>();
        for (Side side : sides.getSides()) {
            for (Label label : labels) {
                if (includes(label, position, holder) && permits(label, side.getDirection())) {
                    allowed.add(side);
                    break;
                }
            }
        }
        return new SideQualifier(allowed);
    }

    public NumberRangeSet getSlots(BlockPos position, ForgeDirection side, LabelPositionHolder holder) {
        if (!qualified) return slots;
        if (side != ForgeDirection.UNKNOWN && !sides.resolveDirections()
            .contains(side)) return NO_SLOTS;
        List<NumberRange> ranges = new ArrayList<>();
        for (Label label : labels) {
            if (!includes(label, position, holder)) continue;
            // An unsided inventory exposes the same backing slots from every physical face.
            if (side != ForgeDirection.UNKNOWN && !permits(label, side)) continue;
            boolean allowed = false;
            for (ForgeDirection physical : sides.resolveDirections()) if (permits(label, physical)) allowed = true;
            if (!allowed) continue;
            ca.teamdman.sfml.data.LabelReference ref = label.getReference();
            for (NumberRange range : slots.getRanges()) {
                int min = Math.max(range.min, ref.getFirstSlot()), max = Math.min(range.max, ref.getLastSlot());
                if (min <= max) ranges.add(new NumberRange(min, max));
            }
            if (slots.getRanges()
                .isEmpty()) ranges.add(new NumberRange(ref.getFirstSlot(), ref.getLastSlot()));
        }
        // Empty NumberRangeSet denotes ALL in the legacy API. Negative slots represent NONE.
        if (ranges.isEmpty()) return NO_SLOTS;
        return new NumberRangeSet(ranges);
    }

    public boolean hasHandleSlots() {
        for (Label label : labels) if (label.getReference()
            .hasSlotSelection()) return true;
        return false;
    }

    /** Tank APIs have no slot index: a slot-qualified handle cannot safely address a tank. */
    public SideQualifier getFluidSides(BlockPos position, LabelPositionHolder holder) {
        if (!slots.isAll()) return new SideQualifier(Collections.emptyList());
        if (!qualified) return sides;
        List<Side> allowed = new ArrayList<>();
        for (Side side : sides.getSides()) {
            for (Label label : labels) {
                if (!label.getReference()
                    .hasSlotSelection() && includes(label, position, holder) && permits(label, side.getDirection())) {
                    allowed.add(side);
                    break;
                }
            }
        }
        return allowed.equals(sides.getSides()) ? sides : new SideQualifier(allowed);
    }

    public List<BlockPos> getPositions(LabelPositionHolder holder) {
        if (holder == null || labels.isEmpty()) {
            return Collections.emptyList();
        }
        if (qualified && selectedLabel != null) return new ArrayList<>(holder.getPositions(selectedLabel.getName()));

        if (roundRobin.isEnabled() && roundRobin.getMode() == RoundRobin.Mode.BY_LABEL) {
            int selectedLabelIdx = roundRobin.nextIndex(labels.size());
            Label l = labels.get(selectedLabelIdx);
            if (qualified) selectedLabel = l;
            return new ArrayList<>(holder.getPositions(l.getName()));
        }

        java.util.Set<BlockPos> unique = new java.util.LinkedHashSet<>();
        for (Label l : labels) {
            unique.addAll(holder.getPositions(l.getName()));
        }

        List<BlockPos> result = new ArrayList<>(unique);
        if (roundRobin.isEnabled() && roundRobin.getMode() == RoundRobin.Mode.BY_BLOCK && !result.isEmpty()) {
            int selectedBlockIdx = roundRobin.nextIndex(result.size());
            return Collections.singletonList(result.get(selectedBlockIdx));
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < labels.size(); i++) {
            if (i > 0) builder.append(", ");
            builder.append(labels.get(i));
        }
        if (roundRobin != null && roundRobin.isEnabled()) {
            builder.append(" ")
                .append(roundRobin);
        }
        if (sides != null && !sides.equals(SideQualifier.DEFAULT)
            && !sides.getSides()
                .isEmpty()) {
            builder.append(" ");
            for (int i = 0; i < sides.getSides()
                .size(); i++) {
                if (i > 0) builder.append(", ");
                builder.append(
                    sides.getSides()
                        .get(i)
                        .name());
            }
            builder.append(" SIDE");
        }
        if (slots != null && !slots.isAll()) {
            builder.append(" SLOTS");
            for (NumberRange range : slots.getRanges()) {
                builder.append(" ")
                    .append(range);
            }
        }
        return builder.toString();
    }
}
