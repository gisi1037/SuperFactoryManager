package ca.teamdman.sfml.ast;

public final class RoundRobin implements ASTNode {

    public enum Mode {
        DISABLED,
        BY_LABEL,
        BY_BLOCK
    }

    public static final RoundRobin DISABLED = new RoundRobin(Mode.DISABLED);

    private final Mode mode;
    private int counter = 0;

    public RoundRobin(Mode mode) {
        this.mode = mode != null ? mode : Mode.DISABLED;
    }

    public Mode getMode() {
        return mode;
    }

    public boolean isEnabled() {
        return mode != Mode.DISABLED;
    }

    public int nextIndex(int size) {
        if (size <= 0) return 0;
        int idx = Math.floorMod(counter, size);
        counter = idx + 1;
        return idx;
    }
}
