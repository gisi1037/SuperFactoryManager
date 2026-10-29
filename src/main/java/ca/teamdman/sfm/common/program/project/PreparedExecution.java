package ca.teamdman.sfm.common.program.project;

import ca.teamdman.sfml.ast.Program;

/**
 * One worker-prepared activation. Its mutable Program belongs exclusively to this result and must not be cached or
 * shared across managers. Source remains server-side; installing it requires live-world and disk-generation checks.
 */
public final class PreparedExecution {

    public final ExecutionReference reference;
    public final Program program;
    public final String source;

    PreparedExecution(ExecutionReference reference, Program program, String source) {
        this.reference = reference;
        this.program = program;
        this.source = source;
    }
}
