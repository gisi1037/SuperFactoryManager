package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataValue;

/** DataValue is immutable, so retaining the evaluated value provides a stable snapshot. */
public final class ArrayForStatement implements Statement {

    private final DataCompiler.Binding binding;
    private final DataExpression array;
    private final Block body;

    public ArrayForStatement(DataCompiler.Binding binding, DataExpression array, Block body) {
        this.binding = binding;
        this.array = array;
        this.body = body;
    }

    @Override
    public void tick(ProgramContext context) {
        DataRuntime runtime = context.getDataRuntime();
        DataValue snapshot = array.evaluate(runtime);
        context.enterLoop();
        try {
            for (int index = 0; index < snapshot.size(); index++) {
                runtime.loopStep();
                binding.write(runtime, snapshot.get(index));
                body.tick(context);
            }
        } finally {
            context.exitLoop();
        }
    }

    @Override
    public String toString() {
        return "FOR $" + binding.name + " IN " + array + " DO\n" + body + "\nEND";
    }
}
