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
            if (context.isResumable()) {
                ca.teamdman.sfml.data.ResumableLoops
                    .whileLoop(context.getDataRuntime(), this, () -> condition.evaluate(context), () -> {
                        body.tick(context);
                        return null;
                    });
                return;
            }
            while (true) {
                context.getDataRuntime()
                    .loopStep();
                if (!condition.evaluate(context)) break;
                body.tick(context);
            }
        } catch (ca.teamdman.sfml.data.LoopBreak done) {
            // Returning normally also releases this loop's command frame and any suspended body cursors.
        } finally {
            context.exitLoop();
        }
    }

    @Override
    public String toString() {
        return "WHILE " + condition + " DO\n" + body + "\nEND";
    }
}
