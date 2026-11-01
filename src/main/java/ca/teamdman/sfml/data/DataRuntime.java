package ca.teamdman.sfml.data;

import java.util.Arrays;
import java.util.function.Supplier;

/** An invocation owns the shared budgets; each trigger/function owns its local frame. */
public final class DataRuntime {

    private StateAccess state;
    private ProgramGlobals globals;
    private final DataValue[] locals;
    private long[] localGenerations;
    private DataReference[] references;
    private DataQueries queries;
    public static final int MAX_LOOP_STEPS = 1024;
    public static final int MAX_FUNCTION_CALLS = 256;
    public static final int MAX_FUNCTION_DEPTH = 16;
    public static final int MAX_ACTIVE_FUNCTION_LOCALS = 4096;
    private Budget budget;
    private Continuation continuation;
    private long gameTick;
    private boolean functionFrame;

    public boolean isFunctionFrame() {
        return functionFrame;
    }

    public boolean isResumable() {
        return continuation != null;
    }

    public Continuation continuation() {
        return continuation;
    }

    public void enableContinuation(long gameTick) {
        continuation = new Continuation(this);
        this.gameTick = gameTick;
    }

    public <T> T evaluate(Object key, Supplier<T> expression) {
        return continuation == null ? expression.get() : continuation.evaluate(key, expression);
    }

    DataValue node(Object key, Supplier<DataValue> expression) {
        return continuation.node(key, expression);
    }

    public void waitTicks(Object key, long ticks) {
        if (continuation == null) throw new IllegalArgumentException("WAIT requires a running resumable trigger");
        continuation.waitTicks(key, ticks, gameTick);
    }

    /** Explicit procedure actions borrow the caller's server authority and shared invocation budgets. */
    public void runAction(ca.teamdman.sfml.ast.Statement statement) {
        if (!(queries instanceof ca.teamdman.sfm.common.program.ProgramContext))
            throw new IllegalArgumentException("Procedure actions require an active server program context");
        ca.teamdman.sfm.common.program.ProgramContext context = (ca.teamdman.sfm.common.program.ProgramContext) queries;
        DataRuntime previous = context.getDataRuntime();
        context.setExecutionRuntime(this);
        try {
            statement.tick(context);
        } finally {
            context.setExecutionRuntime(previous);
        }
    }

    /** Native INPUT registrations inside procedure loops obey the same limits as trigger loops. */
    public DataValue runLoop(Supplier<DataValue> body) {
        if (!(queries instanceof ca.teamdman.sfm.common.program.ProgramContext)) return body.get();
        ca.teamdman.sfm.common.program.ProgramContext context = (ca.teamdman.sfm.common.program.ProgramContext) queries;
        context.enterLoop();
        try {
            return body.get();
        } finally {
            context.exitLoop();
        }
    }

    public DataRuntime triggerFrame(int localCount, long now) {
        DataRuntime frame = new DataRuntime(state, localCount, queries, budget);
        frame.globals = globals;
        frame.enableContinuation(now);
        return frame;
    }

    /** Bind fresh invocation budgets/providers; sleeping frames retain only immutable values and cursors. */
    public void bind(DataRuntime live) {
        state = live.state;
        queries = live.queries;
        globals = live.globals;
        budget = live.budget;
        gameTick = live.gameTick;
        if (continuation != null) continuation.bindChildren(live);
    }

    public void setGameTick(long now) {
        gameTick = now;
    }

    public void detach() {
        state = null;
        queries = null;
        globals = null;
        budget = null;
        if (continuation != null) continuation.detach();
    }

    public void retain(Continuation.Bounds bounds) {
        bounds.entry();
        if (localGenerations != null) bounds.opaque(16 + localGenerations.length * 8);
        for (DataValue value : locals) bounds.value(value);
        if (references != null)
            for (DataReference reference : references) if (reference != null) reference.retain(bounds);
        if (continuation != null) continuation.retain(bounds);
    }

    private static final class Budget {

        private QueryBudget queries;
        private int work, cells, stringCharacters, loopSteps, calls, depth, activeLocals;
    }

    public DataRuntime(StateAccess state, int localCount) {
        this(state, localCount, null);
    }

    public DataRuntime(StateAccess state, int localCount, DataQueries queries) {
        this(state, localCount, queries, new Budget());
    }

    public DataRuntime(StateAccess state, int localCount, DataQueries queries, QueryBudget sharedQueries) {
        this(state, localCount, queries, new Budget());
        budget.queries = sharedQueries;
    }

    private DataRuntime(StateAccess state, int localCount, DataQueries queries, Budget budget) {
        if (localCount < 0 || localCount > 256) throw new IllegalArgumentException("Local bindings exceed 256");
        this.state = state;
        this.queries = queries;
        this.budget = budget;
        locals = new DataValue[localCount];
    }

    private QueryBudget queryBudget() {
        if (queries == null) throw new IllegalArgumentException("Live label queries require an active query service");
        if (budget.queries == null) budget.queries = new QueryBudget();
        return budget.queries;
    }

    /** Verify ownership without adopting a foreign budget or requiring an active query provider. */
    public void requireQueryBudget(QueryBudget sameOwner) {
        if (sameOwner == null || sameOwner != budget.queries)
            throw new IllegalArgumentException("Contents values require their runtime's query budget");
    }

    public DataValue contents(LabelReference label, ResourceSelector selector) {
        QueryBudget owner = queryBudget();
        return DataValue.contents(queries.contents(label, selector, owner), owner, this);
    }

    public boolean sameItem(ItemIdentity left, ItemIdentity right) {
        if (budget.queries == null) budget.queries = new QueryBudget();
        return left.matches(right, budget.queries);
    }

    public boolean sameFluid(FluidIdentity left, FluidIdentity right) {
        if (budget.queries == null) budget.queries = new QueryBudget();
        return left.matches(right, budget.queries);
    }

    public DataValue fluidContents(LabelReference label, ResourceSelector selector) {
        QueryBudget owner = queryBudget();
        return queries.fluidContents(label, selector, owner)
            .materialize(owner, this);
    }

    public long fluidCount(LabelReference label, ResourceSelector selector) {
        return queries.fluidCount(label, selector, queryBudget());
    }

    public boolean fluidEmpty(LabelReference label, ResourceSelector selector) {
        return queries.fluidEmpty(label, selector, queryBudget());
    }

    public boolean fluidAvailable(LabelReference label, ResourceSelector selector) {
        return queries.fluidAvailable(label, selector, queryBudget());
    }

    public long count(LabelReference label, ResourceSelector selector) {
        QueryBudget budget = queryBudget();
        return queries.count(label, selector, budget);
    }

    public boolean empty(LabelReference label, ResourceSelector selector) {
        QueryBudget budget = queryBudget();
        return queries.empty(label, selector, budget);
    }

    public boolean available(LabelReference label, ResourceSelector selector) {
        QueryBudget budget = queryBudget();
        return queries.available(label, selector, budget);
    }

    public long redstone(LabelReference label) {
        return queries.redstone(label, queryBudget());
    }

    public boolean powered(LabelReference label) {
        return queries.powered(label, queryBudget());
    }

    public boolean redstoneAvailable(LabelReference label) {
        return queries.redstoneAvailable(label, queryBudget());
    }

    public void beginTrigger() {
        Arrays.fill(locals, null);
        if (references != null) Arrays.fill(references, null);
    }

    public void endTrigger() {
        Arrays.fill(locals, null);
        if (references != null) Arrays.fill(references, null);
    }

    public void work(int amount) {
        if (amount < 0 || amount > 16384 - budget.work)
            throw new IllegalArgumentException("Expression work budget exhausted");
        budget.work += amount;
    }

    /** Shared by nested loops and every due trigger; charged before conditions or body entry. */
    public void loopStep() {
        if (budget.loopSteps >= MAX_LOOP_STEPS) throw new IllegalArgumentException("Loop step budget exhausted");
        budget.loopSteps++;
    }

    public int getLoopSteps() {
        return budget.loopSteps;
    }

    public void allocate(int amount) {
        if (amount < 0 || amount > 16384 - budget.cells)
            throw new IllegalArgumentException("Expression collection budget exhausted");
        budget.cells += amount;
    }

    public void allocateString(int amount) {
        if (amount < 0 || amount > ExpressionLimits.MAX_ALLOCATED_STRING_CHARACTERS - budget.stringCharacters)
            throw new IllegalArgumentException("Expression string allocation budget exhausted");
        budget.stringCharacters += amount;
    }

    public int getAllocatedStringCharacters() {
        return budget.stringCharacters;
    }

    public DataValue read(boolean persistent, int slot) {
        work(1);
        if (!persistent && references != null && references[slot] != null) return references[slot].read(this);
        DataValue value = persistent ? requireState().read(slot) : locals[slot];
        if (value == null) throw new IllegalArgumentException("Uninitialized variable");
        return value;
    }

    public void setGlobals(ProgramGlobals globals) {
        this.globals = globals;
    }

    public DataValue readGlobal(int slot) {
        work(1);
        return requireGlobals().read(slot);
    }

    public void writeGlobal(int slot, DataValue value) {
        work(1);
        publishGlobal(slot, value);
    }

    void publishGlobal(int slot, DataValue value) {
        publishGlobal(
            slot,
            value,
            value.getType()
                .containsDynamicArray());
    }

    void publishGlobal(int slot, DataValue value, boolean structural) {
        requireGlobals().write(slot, value, structural);
    }

    public long globalGeneration(int slot) {
        return requireGlobals().generation(slot);
    }

    /** Ref parameters report the generation of their caller's root, not their local parameter slot. */
    public long generation(boolean persistent, int slot) {
        if (persistent) return 0;
        if (references != null && references[slot] != null) return references[slot].generation(this);
        return localGenerations == null ? 0 : localGenerations[slot];
    }

    private ProgramGlobals requireGlobals() {
        if (globals == null) throw new IllegalStateException("Program globals require an active execution store");
        return globals;
    }

    public void write(boolean persistent, int slot, DataValue value) {
        work(1);
        publish(persistent, slot, value);
    }

    void publish(boolean persistent, int slot, DataValue value) {
        publish(
            persistent,
            slot,
            value,
            value.getType()
                .containsDynamicArray());
    }

    void publish(boolean persistent, int slot, DataValue value, boolean structural) {
        if (persistent) {
            if (!value.getType()
                .isPersistable()) throw new IllegalArgumentException("STATE cannot persist opaque handles");
            requireState().write(slot, value);
        } else if (references != null && references[slot] != null) references[slot].publish(this, value, structural);
        else {
            if (structural || value.getType()
                .containsDynamicArray()) ensureLocalGenerations();
            publishLocal(locals, localGenerations, slot, value, structural);
        }
    }

    private long[] ensureLocalGenerations() {
        if (localGenerations == null) {
            allocate(locals.length);
            localGenerations = new long[locals.length];
        }
        return localGenerations;
    }

    /** Both direct writes and captured local references publish through the same shared epoch cells. */
    static void publishLocal(DataValue[] cells, long[] generations, int slot, DataValue value, boolean structural) {
        if (structural && generations == null) throw new IllegalStateException("Structural write requires generations");
        long next = structural ? Math.addExact(generations[slot], 1L) : 0;
        cells[slot] = value;
        if (structural) generations[slot] = next;
    }

    /** References retain addresses or local value cells, never a live runtime/provider. */
    public DataReference reference(DataCompiler.Binding binding, int[] path) {
        DataReference root;
        if (binding.kind == DataCompiler.BindingKind.LOCAL) {
            root = references != null && references[binding.slot] != null ? references[binding.slot]
                : DataReference.local(
                    locals,
                    binding.type.containsDynamicArray() ? ensureLocalGenerations() : localGenerations,
                    binding.slot,
                    binding.type);
        } else root = DataReference.addressed(binding.kind, binding.slot, binding.type);
        DataReference selected = root.select(this, path);
        selected.read(this); // Validate the captured path before entering the callee or running effects.
        return selected;
    }

    public void bindReference(int slot, DataReference reference) {
        if (references == null) {
            allocate(locals.length);
            references = new DataReference[locals.length];
        }
        references[slot] = reference;
    }

    private StateAccess requireState() {
        if (state == null) throw new IllegalStateException("Persistent STATE requires an active disk state store");
        return state;
    }

    /** Check every bound before allocating a fresh, manager-independent call frame. */
    DataRuntime enterFunction(int localCount) {
        work(1);
        if (budget.calls >= MAX_FUNCTION_CALLS) throw new IllegalArgumentException("Function call budget exhausted");
        if (budget.depth >= MAX_FUNCTION_DEPTH) throw new IllegalArgumentException("Function call depth exceeds 16");
        if (localCount > MAX_ACTIVE_FUNCTION_LOCALS - budget.activeLocals)
            throw new IllegalArgumentException("Function local frame budget exhausted");
        allocate(localCount);
        budget.calls++;
        budget.depth++;
        budget.activeLocals += localCount;
        DataRuntime frame = new DataRuntime(state, localCount, queries, budget);
        frame.functionFrame = true;
        frame.globals = globals; // Only explicit ref arguments may name caller globals in compiled functions.
        if (isResumable()) frame.enableContinuation(gameTick);
        return frame;
    }

    void exitFunction() {
        Arrays.fill(locals, null);
        if (references != null) Arrays.fill(references, null);
        budget.depth--;
        budget.activeLocals -= locals.length;
    }

    DataRuntime functionFrame(Object call, int localCount, DataValue[] arguments) {
        return functionFrame(call, localCount, arguments, null);
    }

    DataRuntime functionFrame(Object call, int localCount, DataValue[] arguments, DataReference[] refs) {
        DataRuntime frame = isResumable() ? continuation.call(call) : null;
        if (frame == null) {
            frame = enterFunction(localCount);
            try {
                for (int i = 0; i < arguments.length; i++) {
                    if (refs != null && refs[i] != null) frame.bindReference(i, refs[i]);
                    else frame.write(false, i, arguments[i]);
                }
            } catch (RuntimeException | Error failure) {
                frame.exitFunction();
                throw failure;
            }
            if (isResumable()) continuation.call(call, frame);
        } else {
            work(1);
            if (budget.calls >= MAX_FUNCTION_CALLS || budget.depth >= MAX_FUNCTION_DEPTH
                || localCount > MAX_ACTIVE_FUNCTION_LOCALS - budget.activeLocals)
                throw new IllegalArgumentException("Resumed function frame budget exceeded");
            budget.calls++;
            budget.depth++;
            budget.activeLocals += localCount;
            frame.bind(this);
        }
        return frame;
    }

    void leaveFunction(Object call, DataRuntime frame, boolean sleeping) {
        if (!sleeping) {
            Arrays.fill(frame.locals, null);
            if (frame.references != null) Arrays.fill(frame.references, null);
            if (isResumable()) continuation.finishCall(call);
        }
        budget.depth--;
        budget.activeLocals -= frame.locals.length;
    }

    public int getFunctionCalls() {
        return budget.calls;
    }

    public int getFunctionDepth() {
        return budget.depth;
    }

    public int getWork() {
        return budget.work;
    }

    public int getAllocatedCells() {
        return budget.cells;
    }
}
