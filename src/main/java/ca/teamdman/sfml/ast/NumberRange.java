package ca.teamdman.sfml.ast;

public final class NumberRange implements ASTNode {

    public static final NumberRange MAX_RANGE = new NumberRange(0, Integer.MAX_VALUE);

    public final int min;
    public final int max;

    public NumberRange(int min, int max) {
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
    }

    public boolean contains(int value) {
        return value >= min && value <= max;
    }

    @Override
    public String toString() {
        return min == max ? String.valueOf(min) : min + "-" + max;
    }
}
