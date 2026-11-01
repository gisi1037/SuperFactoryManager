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
            if (!runtime.isResumable()) {
                for (Command command : commands) {
                    runtime.work(1);
                    DataValue returned = command.execute(runtime);
                    if (returned != null) return returned;
                }
                return null;
            }
            Continuation flow = runtime.continuation();
            Continuation.Progress cursor = flow.progress(this);
            while (cursor.index < commands.size()) {
                Command command = commands.get(cursor.index);
                runtime.work(1);
                DataValue returned = flow.command(command, () -> command.execute(runtime));
                if (returned != null) {
                    flow.finish(this);
                    return returned;
                }
                cursor.index++;
            }
            flow.finish(this);
            return null;
        }
    }

    private final String name, source;
    private final SourceLocation definitionLocation;
    private final List<String> parameterNames;
    private final List<DataType> parameterTypes;
    private final List<Boolean> referenceParameters;
    private final boolean hasReferenceParameters;
    private final DataType returnType;
    private final Set<CompiledFunction> calls = new LinkedHashSet<>();
    private final Set<String> referencedLabels = new LinkedHashSet<>();
    private Body body;
    private int localCount;
    private boolean mayWait;
    // A manually assembled or not-yet-validated plan always takes the conservative path.
    private boolean maySuspend = true;
    private boolean mayMutateReferences;

    void markWait() {
        mayWait = true;
    }

    void markReferenceMutation() {
        mayMutateReferences = true;
    }

    public boolean mayWait() {
        return mayWait;
    }

    public boolean maySuspend() {
        return maySuspend;
    }

    boolean isSealed() {
        return body != null;
    }

    void finalizeSuspension() {
        if (body == null) throw new IllegalStateException("Function is not sealed");
        maySuspend = mayWait;
    }

    void inheritWait() {
        for (CompiledFunction called : calls) {
            mayWait |= called.mayWait;
            mayMutateReferences |= called.mayMutateReferences;
        }
    }

    CompiledFunction(String name, List<String> names, List<DataType> types, DataType returnType, String source) {
        this(name, names, types, Collections.nCopies(types.size(), false), returnType, source);
    }

    CompiledFunction(String name, List<String> names, List<DataType> types, List<Boolean> references,
        DataType returnType, String source) {
        this(name, names, types, references, returnType, source, new SourceLocation(1, 0, "Function"));
    }

    CompiledFunction(String name, List<String> names, List<DataType> types, List<Boolean> references,
        DataType returnType, String source, SourceLocation definitionLocation) {
        if (references.size() != types.size() || names.size() != types.size())
            throw new IllegalArgumentException("Function parameter metadata mismatch");
        boolean ref = false;
        for (Boolean reference : references) ref |= Boolean.TRUE.equals(reference);
        this.name = name;
        this.parameterNames = Collections.unmodifiableList(new ArrayList<>(names));
        this.parameterTypes = Collections.unmodifiableList(new ArrayList<>(types));
        this.referenceParameters = Collections.unmodifiableList(new ArrayList<>(references));
        hasReferenceParameters = ref;
        mayMutateReferences = ref;
        this.returnType = returnType;
        this.source = source;
        this.definitionLocation = definitionLocation;
    }

    public String getName() {
        return name;
    }

    public SourceLocation getDefinitionLocation() {
        return definitionLocation;
    }

    public String getModuleAlias() {
        return definitionLocation.getModuleAlias();
    }

    public long getModuleRevision() {
        return definitionLocation.getModuleRevision();
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

    public boolean isReferenceParameter(int index) {
        return Boolean.TRUE.equals(referenceParameters.get(index));
    }

    public boolean hasReferenceParameters() {
        return hasReferenceParameters;
    }

    /** Includes indirect calls so initialization and trigger subscriptions remain read-only. */
    public boolean mayMutateReferences() {
        return mayMutateReferences;
    }

    public List<Boolean> getReferenceParameters() {
        return referenceParameters;
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
        if (returnType != DataType.VOID && !body.alwaysReturns())
            throw new IllegalArgumentException("Function " + name + " must RETURN on every path");
        this.body = body;
        this.localCount = localCount;
    }

    DataValue invoke(DataRuntime caller, DataValue[] arguments, Object call) {
        return invoke(caller, arguments, null, call);
    }

    DataValue invoke(DataRuntime caller, DataValue[] arguments, DataReference[] references, Object call) {
        DataRuntime frame = caller.functionFrame(call, localCount, arguments, references, maySuspend);
        boolean sleeping = false;
        try {
            DataValue value = body.execute(frame);
            if (value == null && returnType == DataType.VOID) return DataValue.voidValue();
            if (value == null || !returnType.equals(value.getType()))
                throw new IllegalStateException("Compiled function return contract violated: " + name);
            return value;
        } catch (Continuation.Pause pause) {
            sleeping = true;
            throw pause;
        } catch (IllegalArgumentException ex) {
            throw SfmlRuntimeException.inFunction(name, ex);
        } finally {
            caller.leaveFunction(call, frame, sleeping);
        }
    }

    @Override
    public String toString() {
        return source;
    }
}
