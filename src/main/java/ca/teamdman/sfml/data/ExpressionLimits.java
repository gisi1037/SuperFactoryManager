package ca.teamdman.sfml.data;

/** Provisional bounded-language guardrails, not a server tick-time guarantee. */
public final class ExpressionLimits {

    public static final int MAX_STATE_DECLARATIONS = 256;
    public static final int MAX_STATE_CELLS = 4096;
    public static final int MAX_DEPTH = 32;
    public static final int MAX_CONTAINER_WIDTH = 256;
    public static final int MAX_STATE_BYTES = 256 * 1024;
    public static final int MAX_LOCALS = 256;
    public static final int MAX_WORK = 16384;
    public static final int MAX_ALLOCATED_CELLS = 16384;
    public static final int MAX_CONSTANT_CELLS = 16384;

    public static final int MAX_STATE_STRING_CHARACTERS = 16384;
    public static final int MAX_STRING_LENGTH = 4096;
    public static final int MAX_ALLOCATED_STRING_CHARACTERS = 16384;
    public static final int MAX_CONSTANT_STRING_CHARACTERS = 16384;

    private ExpressionLimits() {}
}
