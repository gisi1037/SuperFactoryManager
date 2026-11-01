package ca.teamdman.sfml.data;

/** Compiler annotation only: one-based line, ANTLR zero-based column, and a grammar kind. */
public final class SourceLocation {

    private final int line, column;
    private final String statementKind;
    private final String moduleAlias;
    private final long moduleRevision;

    public SourceLocation(int line, int column, String statementKind) {
        this(line, column, statementKind, null, -1);
    }

    public SourceLocation(int line, int column, String statementKind, String moduleAlias, long moduleRevision) {
        if (line < 1 || column < 0) throw new IllegalArgumentException("Invalid SFML source location");
        this.line = line;
        this.column = column;
        this.statementKind = SfmlRuntimeException.bounded(statementKind, 64, "Statement");
        if (moduleAlias != null && (!moduleAlias.matches("[A-Za-z_][A-Za-z0-9_]{0,31}") || moduleRevision < 0))
            throw new IllegalArgumentException("Invalid module source location");
        this.moduleAlias = moduleAlias;
        this.moduleRevision = moduleAlias == null ? -1 : moduleRevision;
    }

    public String getModuleAlias() {
        return moduleAlias;
    }

    public long getModuleRevision() {
        return moduleRevision;
    }

    public boolean isModuleLocation() {
        return moduleAlias != null;
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
        if (failure instanceof Continuation.Pause || failure instanceof LoopBreak) return failure;
        return SfmlRuntimeException.at(this, failure);
    }
}
