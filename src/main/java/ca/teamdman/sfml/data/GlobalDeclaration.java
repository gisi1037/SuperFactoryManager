package ca.teamdman.sfml.data;

/** Immutable global initializer plan; live values belong to one manager activation. */
public final class GlobalDeclaration {

    private final DataCompiler.Binding binding;
    private final DataExpression initializer;

    public GlobalDeclaration(DataCompiler.Binding binding, DataExpression initializer) {
        this.binding = binding;
        this.initializer = initializer;
    }

    public DataType getType() {
        return binding.type;
    }

    public void initialize(DataRuntime runtime) {
        binding.write(runtime, initializer.evaluate(runtime));
    }

    @Override
    public String toString() {
        return "LET $" + binding.name + " = " + initializer;
    }
}
