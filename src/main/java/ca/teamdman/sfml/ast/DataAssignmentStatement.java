package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.data.DataCompiler;

public final class DataAssignmentStatement implements Statement {

    private final DataCompiler.Assignment assignment;

    public DataAssignmentStatement(DataCompiler.Assignment assignment) {
        this.assignment = assignment;
    }

    @Override
    public void tick(ProgramContext context) {
        assignment.execute(context.getDataRuntime());
    }

    @Override
    public String toString() {
        return assignment.toString();
    }
}
