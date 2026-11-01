package ca.teamdman.sfml.data;

import java.util.Locale;

/** Bounded operations on immutable sequence snapshots; only explicit references publish structural replacements. */
public final class DynamicArrayFunctions {

    public enum Operation {

        PUSH(2, true, true),
        POP(1, true, false),
        EXTEND(2, true, true),
        INSERT(3, true, true),
        REMOVEAT(2, true, false),
        CLEAR(1, true, true),
        PEEK(1, false, false),
        CONTAINS(2, false, false),
        INDEXOF(2, false, false);

        public final int arity;
        public final boolean mutates, isVoid;

        Operation(int arity, boolean mutates, boolean isVoid) {
            this.arity = arity;
            this.mutates = mutates;
            this.isVoid = isVoid;
        }

        public static Operation parse(String name) {
            if (name == null) return null;
            switch (name.toUpperCase(Locale.ROOT)) {
                case "PUSH":
                    return PUSH;
                case "POP":
                    return POP;
                case "EXTEND":
                    return EXTEND;
                case "INSERT":
                    return INSERT;
                case "REMOVEAT":
                    return REMOVEAT;
                case "CLEAR":
                    return CLEAR;
                case "PEEK":
                    return PEEK;
                case "CONTAINS":
                    return CONTAINS;
                case "INDEXOF":
                    return INDEXOF;
                default:
                    return null;
            }
        }
    }

    private DynamicArrayFunctions() {}

    /** Arguments are already evaluated; mutators receive the fresh target read after all other arguments. */
    public static DataValue apply(Operation operation, DataValue array, DataValue[] extra, DataReference reference,
        DataRuntime runtime) {
        runtime.work(1);
        if (operation == null || array == null || extra == null || extra.length != operation.arity - 1)
            throw new IllegalArgumentException("Invalid collection operation arguments");
        DataType type = array.getType();
        if (!type.isSequence()) throw new IllegalArgumentException("Collection operation requires a sequence");
        if (operation.mutates && (type.getKind() != DataType.Kind.DYNAMIC_ARRAY || reference == null
            || !reference.getType()
                .equals(type)))
            throw new IllegalArgumentException("Collection mutation requires an explicit dynamic-array reference");
        int size = array.size();
        if (operation == Operation.CONTAINS || operation == Operation.INDEXOF) {
            requireElement(type, extra[0]);
            int index = -1;
            for (int i = 0; i < size; i++) if (same(array.get(i), extra[0], runtime)) {
                index = i;
                break;
            }
            return operation == Operation.CONTAINS ? DataValue.bool(index >= 0) : DataValue.integer(index);
        }
        if (operation == Operation.PEEK || operation == Operation.POP) {
            if (size == 0) throw new IllegalArgumentException("Cannot read or remove from an empty array");
            if (operation == Operation.PEEK) return array.get(size - 1);
        }
        int index = size, added = 0;
        DataValue extension = null;
        switch (operation) {
            case PUSH:
                requireElement(type, extra[0]);
                added = 1;
                break;
            case INSERT:
                index = index(extra[0], size, true);
                requireElement(type, extra[1]);
                added = 1;
                break;
            case REMOVEAT:
                index = index(extra[0], size, false);
                added = -1;
                break;
            case POP:
                index = size - 1;
                added = -1;
                break;
            case EXTEND:
                extension = extra[0];
                if (extension == null || !extension.getType()
                    .isSequence()
                    || !type.getElementType()
                        .equals(
                            extension.getType()
                                .getElementType()))
                    throw new IllegalArgumentException("Extend requires a sequence with the same element type");
                added = extension.size();
                if (added == 0) return DataValue.voidValue();
                break;
            case CLEAR:
                if (size == 0) return DataValue.voidValue();
                added = -size;
                break;
            default:
                throw new IllegalArgumentException("Unsupported collection operation");
        }
        int nextSize = size + added;
        if (nextSize > ExpressionLimits.MAX_CONTAINER_WIDTH)
            throw new IllegalArgumentException("Collection width exceeds 256");
        // Copy, factory element validation and retained-value aggregation are all reserved before allocation.
        runtime.work(1 + nextSize * 3);
        runtime.allocate(1 + nextSize);
        DataValue[] values = new DataValue[nextSize];
        if (operation == Operation.EXTEND) {
            for (int i = 0; i < size; i++) values[i] = array.get(i);
            for (int i = 0; i < added; i++) values[size + i] = extension.get(i);
        } else if (operation == Operation.PUSH || operation == Operation.INSERT) {
            for (int i = 0; i < index; i++) values[i] = array.get(i);
            values[index] = extra[operation == Operation.PUSH ? 0 : 1];
            for (int i = index; i < size; i++) values[i + 1] = array.get(i);
        } else if (operation != Operation.CLEAR) {
            for (int i = 0; i < index; i++) values[i] = array.get(i);
            for (int i = index + 1; i < size; i++) values[i - 1] = array.get(i);
        }
        DataValue replacement = DataValue.ownedDynamicArray(type, values);
        reference.publish(runtime, replacement, true);
        return operation.isVoid ? DataValue.voidValue() : array.get(index);
    }

    private static void requireElement(DataType sequence, DataValue value) {
        if (value == null || !sequence.getElementType()
            .equals(value.getType())) throw new IllegalArgumentException("Collection element type mismatch");
    }

    private static int index(DataValue value, int size, boolean inserting) {
        if (value == null || value.getType() != DataType.INTEGER)
            throw new IllegalArgumentException("Array index requires an integer");
        long index = value.asLong();
        if (index < 0 || index >= size + (inserting ? 1 : 0))
            throw new IllegalArgumentException("Array index out of range: " + index);
        return (int) index;
    }

    /** Membership equality only; this does not extend the language's equality operator to collections. */
    private static boolean same(DataValue left, DataValue right, DataRuntime runtime) {
        runtime.work(1);
        if (left == right) return true;
        DataType type = left.getType();
        if (!type.equals(right.getType())) return false;
        switch (type.getKind()) {
            case INTEGER:
                return left.asLong() == right.asLong();
            case BOOLEAN:
                return left.asBoolean() == right.asBoolean();
            case STRING:
                return sameText(left.asString(), right.asString(), runtime);
            case LABEL_REF:
                LabelReference a = left.asLabel(), b = right.asLabel();
                return a.getSideMask() == b.getSideMask() && a.hasSlotSelection() == b.hasSlotSelection()
                    && a.getFirstSlot() == b.getFirstSlot()
                    && a.getLastSlot() == b.getLastSlot()
                    && sameText(a.getName(), b.getName(), runtime);
            case RESOURCE_SELECTOR:
                ResourceSelector x = left.asSelector(), y = right.asSelector();
                return x.getKind() == y.getKind() && sameText(x.getId(), y.getId(), runtime);
            case ITEM_KEY:
                return runtime.sameItem(left.asItemIdentity(), right.asItemIdentity());
            case FLUID_KEY:
                return runtime.sameFluid(left.asFluidIdentity(), right.asFluidIdentity());
            case ARRAY:
            case DYNAMIC_ARRAY:
            case STRUCT:
            case ITEM_CONTENTS:
            case ITEM_LOCATIONS:
            case FLUID_CONTENTS:
                if (left.size() != right.size()) return false;
                for (int i = 0; i < left.size(); i++) if (!same(left.get(i), right.get(i), runtime)) return false;
                return true;
            default:
                throw new IllegalArgumentException("Unsupported collection membership type");
        }
    }

    private static boolean sameText(String left, String right, DataRuntime runtime) {
        if (left == right) return true;
        if (left.length() != right.length()) return false;
        runtime.work(left.length());
        return left.equals(right);
    }
}
