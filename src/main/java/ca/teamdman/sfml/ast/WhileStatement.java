package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;

public final class WhileStatement implements Statement {

    private final BoolExpr condition;
    private final Block body;

    public WhileStatement(BoolExpr condition, Block body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void tick(ProgramContext context) {
        context.enterLoop();
        try {
            while (true) {
                context.getDataRuntime()
                    .loopStep();
                if (!condition.evaluate(context)) break;
                body.tick(context);
            }
        } finally {
            context.exitLoop();
        }
    }

    @Override
    public String toString() {
        return "WHILE " + condition + " DO\n" + body + "\nEND";
    }
}
