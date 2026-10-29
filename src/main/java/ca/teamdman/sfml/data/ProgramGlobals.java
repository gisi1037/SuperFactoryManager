package ca.teamdman.sfml.data;

import java.util.List;

/** Bounded RAM-only values. Never serialize this store or attach it to a shared compiled plan. */
public final class ProgramGlobals implements StateAccess {

    public static final int MAX_GLOBALS = 256, MAX_CELLS = 4096, MAX_CHARACTERS = 16384, MAX_OPAQUE_BYTES = 262144;
    private final DataType[] types;
    private final DataValue[] values;
    private int cells, characters, opaqueBytes;
    private boolean initialized;

    public ProgramGlobals(List<GlobalDeclaration> declarations) {
        if (declarations.size() > MAX_GLOBALS)
            throw new IllegalArgumentException("Program globals exceed " + MAX_GLOBALS);
        types = new DataType[declarations.size()];
        values = new DataValue[types.length];
        for (int i = 0; i < types.length; i++) types[i] = declarations.get(i)
            .getType();
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void finishInitialization() {
        for (DataValue value : values)
            if (value == null) throw new IllegalStateException("Uninitialized program global");
        initialized = true;
    }

    @Override
    public DataValue read(int slot) {
        DataValue value = values[slot];
        if (value == null) throw new IllegalArgumentException("Uninitialized program global");
        return value;
    }

    @Override
    public void write(int slot, DataValue value) {
        if (value == null || !types[slot].equals(value.getType()))
            throw new IllegalArgumentException("Program global type/shape mismatch");
        if (value.getRetainedCellCount() > MAX_CELLS || value.getRetainedCharacters() > MAX_CHARACTERS
            || value.getRetainedOpaqueBytes() > MAX_OPAQUE_BYTES)
            throw new IllegalArgumentException("Program global retained-value budget exceeded");
        DataValue old = values[slot];
        int nextCells = cells - (old == null ? 0 : old.getRetainedCellCount()) + value.getRetainedCellCount();
        int nextCharacters = characters - (old == null ? 0 : old.getRetainedCharacters())
            + value.getRetainedCharacters();
        int nextOpaque = opaqueBytes - (old == null ? 0 : old.getRetainedOpaqueBytes())
            + value.getRetainedOpaqueBytes();
        if (nextCells > MAX_CELLS || nextCharacters > MAX_CHARACTERS || nextOpaque > MAX_OPAQUE_BYTES)
            throw new IllegalArgumentException("Program global retained-value budget exceeded");
        // Publish only after every bound passed. Immutable shared values require no deep copy.
        values[slot] = value;
        cells = nextCells;
        characters = nextCharacters;
        opaqueBytes = nextOpaque;
    }
}
