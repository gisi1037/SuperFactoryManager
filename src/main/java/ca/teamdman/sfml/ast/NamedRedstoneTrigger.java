package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;

/** Native port edges are captured before any trigger body executes. */
public final class NamedRedstoneTrigger implements Trigger {

    private final LabelAccess source;
    private final boolean falling;
    private final int sideMask;
    private final Block block;

    public NamedRedstoneTrigger(LabelAccess source, boolean falling, Block block) {
        if (source == null || source.getRoundRobin()
            .isEnabled()
            || !source.getSlots()
                .isAll())
            throw new IllegalArgumentException("Named redstone triggers do not accept slots or round robin");
        int mask = 0;
        for (net.minecraftforge.common.util.ForgeDirection side : source.getSides()
            .resolveDirections()) {
            if (side == net.minecraftforge.common.util.ForgeDirection.UNKNOWN)
                throw new IllegalArgumentException("Named redstone triggers require absolute physical sides");
            mask |= 1 << side.ordinal();
        }
        if (mask == 0) throw new IllegalArgumentException("Named redstone trigger requires at least one side");
        this.source = source;
        this.falling = falling;
        sideMask = mask;
        this.block = block == null ? new Block(null) : block;
    }

    public LabelAccess getSource() {
        return source;
    }

    public boolean isFalling() {
        return falling;
    }

    public int getSideMask() {
        return sideMask;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public boolean shouldRun(ProgramContext context) {
        return context.hasNamedRedstoneEdge(this);
    }

    @Override
    public String toStringCondensed() {
        StringBuilder text = new StringBuilder("EVERY REDSTONE ").append(falling ? "FALLING" : "PULSE")
            .append(" FROM ");
        for (int i = 0; i < source.getLabels()
            .size(); i++) {
            if (i != 0) text.append(", ");
            text.append(
                ca.teamdman.sfml.data.DataValue.string(
                    source.getLabels()
                        .get(i)
                        .getName()));
        }
        if (sideMask != 63) {
            boolean first = true;
            for (Side side : source.getSides()
                .getSides()) {
                text.append(first ? " " : ", ")
                    .append(side.name());
                first = false;
            }
            text.append(" SIDE");
        }
        return text.append(" DO")
            .toString();
    }

    @Override
    public String toString() {
        return toStringCondensed() + "\n" + block + "\nEND";
    }
}
