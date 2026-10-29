package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataValue;

/** Inclusive signed ranges; bounds are evaluated once, before introducing the binder. */
public final class CountedForStatement implements Statement {

    private final DataCompiler.Binding binding;
    private final DataExpression first, last, step;
    private final Block body;

    public CountedForStatement(DataCompiler.Binding binding, DataExpression first, DataExpression last,
        DataExpression step, Block body) {
        this.binding = binding;
        this.first = first;
        this.last = last;
        this.step = step;
        this.body = body;
    }

    @Override
    public void tick(ProgramContext context) {
        DataRuntime runtime = context.getDataRuntime();
        long current = first.evaluate(runtime)
            .asLong();
        long end = last.evaluate(runtime)
            .asLong();
        long increment = step == null ? 1
            : step.evaluate(runtime)
                .asLong();
        if (increment == 0) throw new IllegalArgumentException("FOR STEP cannot be zero");
        context.enterLoop();
        try {
            while (increment > 0 ? current <= end : current >= end) {
                runtime.loopStep();
                binding.write(runtime, DataValue.integer(current));
                body.tick(context);
                // Overflow means the mathematical successor is outside all representable end bounds.
                if (increment > 0 ? current > Long.MAX_VALUE - increment : current < Long.MIN_VALUE - increment) break;
                current += increment;
            }
        } finally {
            context.exitLoop();
        }
    }

    @Override
    public String toString() {
        return "FOR $" + binding.name
            + " = "
            + first
            + " TO "
            + last
            + (step == null ? "" : " STEP " + step)
            + " DO\n"
            + body
            + "\nEND";
    }
}
