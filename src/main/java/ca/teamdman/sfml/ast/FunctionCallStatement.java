package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.data.DataExpression;

/** An explicit invocation may discard a value or execute a void procedure. */
public final class FunctionCallStatement implements Statement {

    private final DataExpression invocation;

    public FunctionCallStatement(DataExpression invocation) {
        this.invocation = invocation;
    }

    @Override
    public void tick(ProgramContext context) {
        invocation.evaluate(context.getDataRuntime());
    }

    @Override
    public String toString() {
        return invocation.toString();
    }
}
