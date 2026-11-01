package ca.teamdman.sfml.data;

import java.util.Arrays;
import java.util.Objects;

/**
 * One invocation's typed lvalue address. Local roots retain only the caller's value cells;
 * STATE/global roots retain a slot number, resolved against the currently bound runtime.
 * No world, inventory, storage service, or runtime is captured by a reference.
 */
public final class DataReference {

    private static final int[] ROOT = new int[0];
    private final DataCompiler.BindingKind kind;
    private final DataValue[] cells;
    private final long[] generations;
    private final int slot;
    private final DataType rootType, type;
    private final int[] path;
    private final boolean indexedDynamic;
    private long capturedGeneration;

    private DataReference(DataCompiler.BindingKind kind, DataValue[] cells, long[] generations, int slot,
        DataType rootType, DataType type, int[] path, boolean indexedDynamic, long capturedGeneration) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.cells = cells;
        this.generations = generations;
        this.slot = slot;
        this.rootType = Objects.requireNonNull(rootType, "rootType");
        this.type = Objects.requireNonNull(type, "type");
        this.path = path;
        this.indexedDynamic = indexedDynamic;
        this.capturedGeneration = capturedGeneration;
        if (slot < 0 || rootType == DataType.VOID || type == DataType.VOID)
            throw new IllegalArgumentException("Reference requires a non-void variable");
        if (kind == DataCompiler.BindingKind.STATE && !rootType.isPersistable())
            throw new IllegalArgumentException("STATE references require persistable values");
    }

    static DataReference local(DataValue[] cells, int slot, DataType rootType) {
        if (rootType.containsDynamicArray())
            throw new IllegalArgumentException("Dynamic local references require shared generations");
        return local(cells, null, slot, rootType);
    }

    static DataReference local(DataValue[] cells, long[] generations, int slot, DataType rootType) {
        Objects.requireNonNull(cells, "cells");
        if (slot < 0 || slot >= cells.length) throw new IllegalArgumentException("Invalid local reference slot");
        if (generations != null && generations.length != cells.length
            || rootType.containsDynamicArray() && generations == null)
            throw new IllegalArgumentException("Invalid local reference generations");
        return new DataReference(
            DataCompiler.BindingKind.LOCAL,
            cells,
            generations,
            slot,
            rootType,
            rootType,
            ROOT,
            false,
            0);
    }

    static DataReference addressed(DataCompiler.BindingKind kind, int slot, DataType rootType) {
        if (kind == DataCompiler.BindingKind.LOCAL)
            throw new IllegalArgumentException("Local references require caller value cells");
        return new DataReference(kind, null, null, slot, rootType, rootType, ROOT, false, 0);
    }

    public DataType getType() {
        return type;
    }

    /** Append an already evaluated path. The original indices can subsequently change freely. */
    public DataReference select(int[] suffix) {
        return select(null, suffix);
    }

    /** Capture actual dynamic lengths and the root epoch without retaining the evaluating runtime. */
    public DataReference select(DataRuntime runtime, int[] suffix) {
        Objects.requireNonNull(suffix, "suffix");
        if (runtime != null) requireCurrent(runtime);
        if (suffix.length == 0) return indexedDynamic
            ? new DataReference(kind, cells, generations, slot, rootType, type, path, true, capturedGeneration)
            : this;
        if (suffix.length > 32 - path.length) throw new IllegalArgumentException("Reference path exceeds 32");
        DataType selected = type;
        DataValue cursor = runtime != null && type.containsDynamicArray() ? read(runtime) : null;
        boolean guarded = indexedDynamic;
        for (int index : suffix) {
            boolean dynamic = selected.getKind() == DataType.Kind.DYNAMIC_ARRAY;
            if (selected.getKind() != DataType.Kind.ARRAY && !dynamic && selected.getKind() != DataType.Kind.STRUCT)
                throw new IllegalArgumentException("Reference paths require mutable arrays or structs");
            if (dynamic && runtime == null)
                throw new IllegalArgumentException("Dynamic reference selection requires a runtime");
            if (index < 0 || index >= (dynamic ? cursor.size() : selected.getLength()))
                throw new IllegalArgumentException("Reference index out of range: " + index);
            guarded |= dynamic;
            selected = selected.getKind() == DataType.Kind.ARRAY || dynamic ? selected.getElementType()
                : selected.getFieldType(index);
            if (cursor != null) cursor = cursor.get(index);
        }
        int[] joined = Arrays.copyOf(path, path.length + suffix.length);
        System.arraycopy(suffix, 0, joined, path.length, suffix.length);
        return new DataReference(
            kind,
            cells,
            generations,
            slot,
            rootType,
            selected,
            joined,
            guarded,
            runtime == null ? capturedGeneration : generation(runtime));
    }

    public DataValue read(DataRuntime runtime) {
        runtime.work(1 + path.length);
        requireCurrent(runtime);
        DataValue value = root(runtime);
        for (int index : path) value = value.get(index);
        if (!type.equals(value.getType())) throw new IllegalArgumentException("Reference type/shape changed");
        return value;
    }

    public void write(DataRuntime runtime, DataValue value) {
        runtime.work(1);
        publish(runtime, value);
    }

    /** Publication still enforces collection budgets and STATE/global store constraints. */
    void publish(DataRuntime runtime, DataValue value) {
        publish(
            runtime,
            value,
            value != null && value.getType()
                .containsDynamicArray());
    }

    void publish(DataRuntime runtime, DataValue value, boolean structural) {
        requireCurrent(runtime);
        if (value == null || !type.equals(value.getType()))
            throw new IllegalArgumentException("Reference assignment type/shape mismatch");
        DataValue replacement = value;
        if (path.length != 0) {
            DataValue cursor = root(runtime);
            DataValue[] parents = new DataValue[path.length];
            int copiedCells = path.length;
            for (int i = 0; i < path.length; i++) {
                parents[i] = cursor;
                copiedCells = Math.addExact(copiedCells, cursor.size());
                cursor = cursor.get(path[i]);
            }
            runtime.work(path.length);
            runtime.allocate(copiedCells);
            // Rebuild from the current root, preserving writes made through sibling aliases or during WAIT.
            for (int i = path.length - 1; i >= 0; i--) replacement = parents[i].with(path[i], replacement);
        }
        if (!rootType.equals(replacement.getType()))
            throw new IllegalArgumentException("Reference root type/shape mismatch");
        if (kind == DataCompiler.BindingKind.LOCAL)
            DataRuntime.publishLocal(cells, generations, slot, replacement, structural);
        else if (kind == DataCompiler.BindingKind.GLOBAL) runtime.publishGlobal(slot, replacement, structural);
        else runtime.publish(true, slot, replacement, structural);
        if (indexedDynamic) capturedGeneration = rootGeneration(runtime);
    }

    /** Effective calling root, including a reference forwarded through several parameter frames. */
    public long generation(DataRuntime runtime) {
        requireCurrent(runtime);
        return rootGeneration(runtime);
    }

    private long rootGeneration(DataRuntime runtime) {
        return kind == DataCompiler.BindingKind.LOCAL ? generations == null ? 0 : generations[slot]
            : kind == DataCompiler.BindingKind.GLOBAL ? runtime.globalGeneration(slot) : runtime.generation(true, slot);
    }

    private void requireCurrent(DataRuntime runtime) {
        if (indexedDynamic && capturedGeneration != rootGeneration(runtime))
            throw new IllegalArgumentException("Indexed reference invalidated by structural array replacement");
    }

    private DataValue root(DataRuntime runtime) {
        DataValue root = kind == DataCompiler.BindingKind.LOCAL ? cells[slot]
            : kind == DataCompiler.BindingKind.GLOBAL ? runtime.readGlobal(slot) : runtime.read(true, slot);
        if (root == null) throw new IllegalArgumentException("Uninitialized reference variable");
        if (!rootType.equals(root.getType())) throw new IllegalArgumentException("Reference root type/shape changed");
        return root;
    }

    public void retain(Continuation.Bounds bounds) {
        bounds.entry();
        bounds.opaque(32 + path.length * 4);
        if (generations != null) bounds.opaque(16 + generations.length * 8);
        if (cells != null) for (DataValue value : cells) bounds.value(value);
    }
}
