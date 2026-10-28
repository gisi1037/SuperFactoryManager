package ca.teamdman.sfml.data;

import java.util.Arrays;

/** One manager invocation's budget and one due trigger's local frame. */
public final class DataRuntime {

    private final StateAccess state;
    private final DataValue[] locals;
    private int work, cells, stringCharacters;

    public DataRuntime(StateAccess state, int localCount) {
        this.state = state;
        locals = new DataValue[localCount];
    }

    public void beginTrigger() {
        Arrays.fill(locals, null);
    }

    public void endTrigger() {
        Arrays.fill(locals, null);
    }

    public void work(int amount) {
        if (amount < 0 || amount > 16384 - work) throw new IllegalArgumentException("Expression work budget exhausted");
        work += amount;
    }

    public void allocate(int amount) {
        if (amount < 0 || amount > 16384 - cells)
            throw new IllegalArgumentException("Expression collection budget exhausted");
        cells += amount;
    }

    public void allocateString(int amount) {
        if (amount < 0 || amount > ExpressionLimits.MAX_ALLOCATED_STRING_CHARACTERS - stringCharacters)
            throw new IllegalArgumentException("Expression string allocation budget exhausted");
        stringCharacters += amount;
    }

    public int getAllocatedStringCharacters() {
        return stringCharacters;
    }

    public DataValue read(boolean persistent, int slot) {
        work(1);
        DataValue value = persistent ? requireState().read(slot) : locals[slot];
        if (value == null) throw new IllegalArgumentException("Uninitialized variable");
        return value;
    }

    public void write(boolean persistent, int slot, DataValue value) {
        work(1);
        publish(persistent, slot, value);
    }

    void publish(boolean persistent, int slot, DataValue value) {
        if (persistent) requireState().write(slot, value);
        else locals[slot] = value;
    }

    private StateAccess requireState() {
        if (state == null) throw new IllegalStateException("Persistent STATE requires an active disk state store");
        return state;
    }

    public int getWork() {
        return work;
    }

    public int getAllocatedCells() {
        return cells;
    }
}
