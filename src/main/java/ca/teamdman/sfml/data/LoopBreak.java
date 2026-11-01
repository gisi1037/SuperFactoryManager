package ca.teamdman.sfml.data;

/** Internal lexical loop control, not a failure. Shared, stackless and never stored in a continuation. */
public final class LoopBreak extends RuntimeException {

    public static final LoopBreak INSTANCE = new LoopBreak();

    private LoopBreak() {
        super(null, null, false, false);
    }
}
