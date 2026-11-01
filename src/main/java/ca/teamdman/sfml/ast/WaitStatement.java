package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.data.DataExpression;

/** WAIT yields the trigger and its active function calls; it never sleeps a Java/server thread. */
public final class WaitStatement implements Statement {

    private final DataExpression ticks;

    public WaitStatement(DataExpression ticks) {
        this.ticks = ticks;
    }

    @Override
    public void tick(ProgramContext context) {
        context.getDataRuntime()
            .waitTicks(
                this,
                ticks.evaluate(context.getDataRuntime())
                    .asLong());
    }

    @Override
    public String toString() {
        return "WAIT (" + ticks + ")";
    }
}
