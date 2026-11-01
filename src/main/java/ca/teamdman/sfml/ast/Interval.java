package ca.teamdman.sfml.ast;

public final class Interval implements ASTNode {

    private final int ticks;
    private final boolean isGlobal;
    private final int offset;
    private final ca.teamdman.sfml.data.DataExpression offsetExpression;
    private final ca.teamdman.sfml.data.SourceLocation offsetLocation;

    public Interval(int ticks, boolean isGlobal, int offset) {
        this.ticks = Math.max(1, ticks);
        this.isGlobal = isGlobal;
        this.offset = offset;
        offsetExpression = null;
        offsetLocation = null;
    }

    public Interval(int ticks, boolean isGlobal, ca.teamdman.sfml.data.DataExpression offsetExpression,
        ca.teamdman.sfml.data.SourceLocation offsetLocation) {
        if (ticks < 1 || offsetExpression == null
            || offsetExpression.getType() != ca.teamdman.sfml.data.DataType.INTEGER
            || offsetLocation == null)
            throw new IllegalArgumentException(
                "Runtime timer offset requires a positive interval and INTEGER expression");
        this.ticks = ticks;
        this.isGlobal = isGlobal;
        this.offset = 0;
        this.offsetExpression = offsetExpression;
        this.offsetLocation = offsetLocation;
    }

    public boolean hasRuntimeOffset() {
        return offsetExpression != null;
    }

    /** Evaluate once per manager execution state, never on each eligibility poll. */
    public int resolveOffset(ca.teamdman.sfml.data.DataRuntime runtime) {
        try {
            long value = offsetExpression.evaluate(runtime)
                .asLong();
            if (value < 0 || value > Integer.MAX_VALUE)
                throw new IllegalArgumentException("Timer offset must be between 0 and 2147483647 ticks");
            return (int) value;
        } catch (RuntimeException failure) {
            throw offsetLocation.annotate(failure);
        }
    }

    public boolean matches(long currentTick, int resolvedOffset) {
        return currentTick >= resolvedOffset && (currentTick - resolvedOffset) % ticks == 0;
    }

    public int getTicks() {
        return ticks;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public int getOffset() {
        return offset;
    }

    public boolean matches(long currentTick) {
        long effectiveTick = currentTick - offset;
        if (effectiveTick < 0) return false;
        return effectiveTick % ticks == 0;
    }

    @Override
    public String toString() {
        if (offsetExpression != null)
            return ticks + (isGlobal ? " GLOBAL" : "") + " PLUS (" + offsetExpression + ") TICKS";
        StringBuilder sb = new StringBuilder();
        sb.append(ticks)
            .append(" TICKS");
        if (isGlobal) {
            sb.append(" GLOBAL");
        }
        if (offset != 0) {
            sb.append(" OFFSET ")
                .append(offset);
        }
        return sb.toString();
    }
}
