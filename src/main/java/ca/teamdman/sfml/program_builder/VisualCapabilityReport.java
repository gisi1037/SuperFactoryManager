package ca.teamdman.sfml.program_builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable authoring diagnostics. Safe for common/server consumers; contains no client classes or parse tree. */
public final class VisualCapabilityReport {

    public enum Kind {
        INVALID_SOURCE,
        STATE_DECLARATION,
        LOCAL_DECLARATION,
        ASSIGNMENT,
        DATA_EXPRESSION,
        PRINT_STATEMENT,
        LOOP_STATEMENT,
        FUNCTION_DEFINITION,
        FUNCTION_CALL,
        NAMED_REDSTONE_TRIGGER,
        ELSE_IF,
        GRAPH_LIMIT,
        SOURCE_FIDELITY,
        TRANSFER_STATEMENT
    }

    public static final class Blocker {

        public final Kind kind;
        public final String message;
        /** One-based line and zero-based column; -1 means the compiler supplied no location. */
        public final int line, column;
        /** Unicode code-point offsets, end exclusive; -1 when unavailable. */
        public final int startOffset, endOffset;

        public Blocker(Kind kind, String message, int line, int column, int startOffset, int endOffset) {
            if (kind == null || message == null) throw new IllegalArgumentException("Missing visual diagnostic");
            this.kind = kind;
            this.message = message;
            this.line = line;
            this.column = column;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        @Override
        public String toString() {
            return line < 0 ? message : "Line " + line + ", Col " + column + ": " + message;
        }
    }

    private final List<Blocker> blockers;

    public VisualCapabilityReport(List<Blocker> blockers) {
        this.blockers = Collections.unmodifiableList(new ArrayList<>(blockers));
    }

    public boolean isAvailable() {
        return blockers.isEmpty();
    }

    public List<Blocker> getBlockers() {
        return blockers;
    }

    public String getMessage() {
        StringBuilder message = new StringBuilder();
        for (Blocker blocker : blockers) {
            if (message.length() > 0) message.append('\n');
            message.append(blocker);
        }
        return message.toString();
    }
}
