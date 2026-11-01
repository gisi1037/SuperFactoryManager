package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.ResumableLoops;

/** One mutable loop scope with initialization, Boolean test, body and update in source execution order. */
public final class JavaForStatement implements Statement {

    private final Statement initializer, step;
    private final DataExpression condition;
    private final Block body;

    public JavaForStatement(Statement initializer, DataExpression condition, Statement step, Block body) {
        this.initializer = initializer;
        this.condition = condition;
        this.step = step;
        this.body = body;
    }

    @Override
    public void tick(ProgramContext context) {
        DataRuntime runtime = context.getDataRuntime();
        context.enterLoop();
        try {
            if (context.isResumable()) {
                ResumableLoops.javaFor(runtime, this, initializer == null ? null : () -> {
                    initializer.tick(context);
                    return null;
                },
                    () -> condition == null || condition.evaluate(runtime)
                        .asBoolean(),
                    () -> {
                        body.tick(context);
                        return null;
                    },
                    step == null ? null : () -> {
                        step.tick(context);
                        return null;
                    });
                return;
            }
            if (initializer != null) initializer.tick(context);
            while (true) {
                runtime.loopStep();
                if (condition != null && !condition.evaluate(runtime)
                    .asBoolean()) break;
                body.tick(context);
                if (step != null) step.tick(context);
            }
        } finally {
            context.exitLoop();
        }
    }

    @Override
    public String toString() {
        return "FOR (" + (initializer == null ? "" : initializer)
            + "; "
            + (condition == null ? "" : condition)
            + "; "
            + (step == null ? "" : step)
            + ") DO\n"
            + body
            + "\nEND";
    }
}
