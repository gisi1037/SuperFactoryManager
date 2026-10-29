package ca.teamdman.sfml.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Compiled once, then sealed before publication. It contains no invocation or manager state. */
public final class CompiledFunction {

    @FunctionalInterface
    public interface Command {

        /** Null means continue; every RETURN value propagates through branches and loops. */
        DataValue execute(DataRuntime runtime);
    }

    public static final class Body implements Command {

        private final List<Command> commands;
        private final boolean returns;

        public Body(List<Command> commands, boolean returns) {
            this.commands = Collections.unmodifiableList(new ArrayList<>(commands));
            this.returns = returns;
        }

        public boolean alwaysReturns() {
            return returns;
        }

        @Override
        public DataValue execute(DataRuntime runtime) {
            for (Command command : commands) {
                runtime.work(1);
                DataValue returned = command.execute(runtime);
                if (returned != null) return returned;
            }
            return null;
        }
    }

    private final String name, source;
    private final List<String> parameterNames;
    private final List<DataType> parameterTypes;
    private final DataType returnType;
    private final Set<CompiledFunction> calls = new LinkedHashSet<>();
    private final Set<String> referencedLabels = new LinkedHashSet<>();
    private Body body;
    private int localCount;

    CompiledFunction(String name, List<String> names, List<DataType> types, DataType returnType, String source) {
        this.name = name;
        this.parameterNames = Collections.unmodifiableList(new ArrayList<>(names));
        this.parameterTypes = Collections.unmodifiableList(new ArrayList<>(types));
        this.returnType = returnType;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public List<String> getParameterNames() {
        return parameterNames;
    }

    public List<DataType> getParameterTypes() {
        return parameterTypes;
    }

    public DataType getReturnType() {
        return returnType;
    }

    void calls(CompiledFunction function) {
        if (body != null) throw new IllegalStateException("Function is sealed");
        calls.add(function);
    }

    Set<CompiledFunction> getCalls() {
        return calls;
    }

    void referencesLabel(String label) {
        if (body != null) throw new IllegalStateException("Function is sealed");
        referencedLabels.add(label);
    }

    Set<String> getReferencedLabels() {
        return referencedLabels;
    }

    void seal(Body body, int localCount) {
        if (this.body != null) throw new IllegalStateException("Function is already sealed");
        if (!body.alwaysReturns())
            throw new IllegalArgumentException("Function " + name + " must RETURN on every path");
        this.body = body;
        this.localCount = localCount;
    }

    DataValue invoke(DataRuntime caller, DataValue[] arguments) {
        DataRuntime frame = caller.enterFunction(localCount);
        try {
            for (int i = 0; i < arguments.length; i++) frame.write(false, i, arguments[i]);
            DataValue value = body.execute(frame);
            if (value == null || !returnType.equals(value.getType()))
                throw new IllegalStateException("Compiled function return contract violated: " + name);
            return value;
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("In function " + name + ": " + ex.getMessage());
        } finally {
            frame.exitFunction();
        }
    }

    @Override
    public String toString() {
        return source;
    }
}
