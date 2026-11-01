package ca.teamdman.sfml.data;

/** Compiler annotation only: one-based line, ANTLR zero-based column, and a grammar kind. */
public final class SourceLocation {

    private final int line, column;
    private final String statementKind;

    public SourceLocation(int line, int column, String statementKind) {
        if (line < 1 || column < 0) throw new IllegalArgumentException("Invalid SFML source location");
        this.line = line;
        this.column = column;
        this.statementKind = SfmlRuntimeException.bounded(statementKind, 64, "Statement");
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getStatementKind() {
        return statementKind;
    }

    public RuntimeException annotate(RuntimeException failure) {
        if (failure instanceof Continuation.Pause) return failure;
        return SfmlRuntimeException.at(this, failure);
    }
}
