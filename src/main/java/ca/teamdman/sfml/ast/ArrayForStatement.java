package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataValue;

/** DataValue is immutable, so retaining the evaluated value provides a stable snapshot. */
public final class ArrayForStatement implements Statement {

    private final DataCompiler.Binding indexBinding, binding;
    private final DataExpression array;
    private final Block body;

    public ArrayForStatement(DataCompiler.Binding binding, DataExpression array, Block body) {
        this(null, binding, array, body);
    }

    public ArrayForStatement(DataCompiler.Binding indexBinding, DataCompiler.Binding binding, DataExpression array,
        Block body) {
        this.indexBinding = indexBinding;
        this.binding = binding;
        this.array = array;
        this.body = body;
    }

    @Override
    public void tick(ProgramContext context) {
        DataRuntime runtime = context.getDataRuntime();
        if (context.isResumable()) {
            context.enterLoop();
            try {
                ca.teamdman.sfml.data.ResumableLoops.array(runtime, this, indexBinding, binding, array, () -> {
                    body.tick(context);
                    return null;
                });
            } catch (ca.teamdman.sfml.data.LoopBreak done) {
                // The owning statement command completes and discards its continuation subtree.
            } finally {
                context.exitLoop();
            }
            return;
        }
        DataValue snapshot = array.evaluate(runtime);
        context.enterLoop();
        try {
            for (int index = 0; index < snapshot.size(); index++) {
                runtime.loopStep();
                if (indexBinding != null) indexBinding.write(runtime, DataValue.integer(index));
                binding.write(runtime, snapshot.get(index));
                body.tick(context);
            }
        } catch (ca.teamdman.sfml.data.LoopBreak done) {
            // Leave only this nearest lexical loop.
        } finally {
            context.exitLoop();
        }
    }

    @Override
    public String toString() {
        String binder = indexBinding == null ? "$" + binding.name
            : "($" + indexBinding.name + ", $" + binding.name + ")";
        return "FOR " + binder + " IN " + array + " DO\n" + body + "\nEND";
    }
}
