package ca.teamdman.sfml.ast;

public final class Interval implements ASTNode {

    private final int ticks;
    private final boolean isGlobal;
    private final int offset;

    public Interval(int ticks, boolean isGlobal, int offset) {
        this.ticks = Math.max(1, ticks);
        this.isGlobal = isGlobal;
        this.offset = offset;
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
}
