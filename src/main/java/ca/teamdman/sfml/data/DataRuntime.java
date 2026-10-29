package ca.teamdman.sfml.data;

import java.util.Arrays;

/** An invocation owns the shared budgets; each trigger/function owns its local frame. */
public final class DataRuntime {

    private final StateAccess state;
    private ProgramGlobals globals;
    private final DataValue[] locals;
    private final DataQueries queries;
    public static final int MAX_LOOP_STEPS = 1024;
    public static final int MAX_FUNCTION_CALLS = 256;
    public static final int MAX_FUNCTION_DEPTH = 16;
    public static final int MAX_ACTIVE_FUNCTION_LOCALS = 4096;
    private final Budget budget;

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
    }

    public void endTrigger() {
        Arrays.fill(locals, null);
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
        requireGlobals().write(slot, value);
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
        if (persistent) {
            if (!value.getType()
                .isPersistable()) throw new IllegalArgumentException("STATE cannot persist opaque handles");
            requireState().write(slot, value);
        } else locals[slot] = value;
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
        return new DataRuntime(null, localCount, queries, budget);
    }

    void exitFunction() {
        Arrays.fill(locals, null);
        budget.depth--;
        budget.activeLocals -= locals.length;
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
