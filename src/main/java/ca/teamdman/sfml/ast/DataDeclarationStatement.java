package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataExpression;

public final class DataDeclarationStatement implements Statement {

    private final DataCompiler.Binding binding;
    private final DataExpression initializer;

    public DataDeclarationStatement(DataCompiler.Binding binding, DataExpression initializer) {
        this.binding = binding;
        this.initializer = initializer;
    }

    @Override
    public void tick(ProgramContext context) {
        binding.write(context.getDataRuntime(), initializer.evaluate(context.getDataRuntime()));
    }

    @Override
    public String toString() {
        return "LET $" + binding.name + " = " + initializer;
    }
}
