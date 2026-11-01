package ca.teamdman.sfml.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfml.data.DynamicArrayFunctions.Operation;

class DynamicArrayFunctionsTest {

    private static final DataType INTEGERS = DataType.dynamicArray(DataType.INTEGER);

    @Test
    void operationsPublishValuesAndReturnTheDocumentedResults() {
        Target target = new Target(integers(1, 3));
        assertSame(DataValue.voidValue(), target.apply(Operation.INSERT, DataValue.integer(1), DataValue.integer(2)));
        assertNumbers(target.value(), 1, 2, 3);
        assertSame(DataValue.voidValue(), target.apply(Operation.PUSH, DataValue.integer(4)));
        assertEquals(
            4,
            target.apply(Operation.PEEK)
                .asLong());
        assertEquals(
            2,
            target.apply(Operation.REMOVEAT, DataValue.integer(1))
                .asLong());
        assertNumbers(target.value(), 1, 3, 4);
        assertEquals(
            4,
            target.apply(Operation.POP)
                .asLong());
        assertNumbers(target.value(), 1, 3);
        assertSame(DataValue.voidValue(), target.apply(Operation.CLEAR));
        assertEquals(
            0,
            target.value()
                .size());
        assertEquals(5, target.epochs[0]);
        assertSame(Operation.REMOVEAT, Operation.parse("removeAt"));
        assertNull(Operation.parse("reserve"));
        assertTrue(Operation.INSERT.mutates);
        assertTrue(Operation.INSERT.isVoid);
        assertEquals(3, Operation.INSERT.arity);
    }

    @Test
    void extendUsesFixedOrSelfSnapshotsAndInvalidatesOldIndexedAliases() {
        Target target = new Target(integers(1, 2));
        DataReference oldElement = target.reference.select(target.runtime, new int[] { 0 });
        DataValue snapshot = target.value();
        target.apply(Operation.EXTEND, snapshot);
        assertNumbers(snapshot, 1, 2);
        assertNumbers(target.value(), 1, 2, 1, 2);
        assertThrows(IllegalArgumentException.class, () -> oldElement.read(target.runtime));
        target.apply(Operation.EXTEND, DataValue.array(Arrays.asList(DataValue.integer(8), DataValue.integer(9))));
        assertNumbers(target.value(), 1, 2, 1, 2, 8, 9);
    }

    @Test
    void emptyNoOpsPreserveEpochsWhileEmptyReadsAndInvalidIndicesLeaveTargetsUnchanged() {
        Target target = new Target(integers());
        DataValue empty = target.value();
        target.apply(Operation.CLEAR);
        target.apply(Operation.EXTEND, integers());
        assertSame(empty, target.value());
        assertEquals(0, target.epochs[0]);
        assertEquals(0, target.runtime.getAllocatedCells());
        assertThrows(IllegalArgumentException.class, () -> target.apply(Operation.POP));
        assertThrows(IllegalArgumentException.class, () -> target.apply(Operation.PEEK));
        assertThrows(IllegalArgumentException.class, () -> target.apply(Operation.REMOVEAT, DataValue.integer(0)));
        assertThrows(
            IllegalArgumentException.class,
            () -> target.apply(Operation.INSERT, DataValue.integer(-1), DataValue.integer(1)));
        assertThrows(
            IllegalArgumentException.class,
            () -> target.apply(Operation.INSERT, DataValue.integer(Long.MAX_VALUE), DataValue.integer(1)));
        target.apply(Operation.INSERT, DataValue.integer(0), DataValue.integer(7));
        target.apply(Operation.INSERT, DataValue.integer(1), DataValue.integer(8));
        assertNumbers(target.value(), 7, 8);
    }

    @Test
    void workAllocationWidthAndRetainedPayloadFailuresPublishNothing() {
        Target work = new Target(integers(1));
        work.runtime.work(ExpressionLimits.MAX_WORK - 1);
        assertUnchangedFailure(work, () -> work.apply(Operation.PUSH, DataValue.integer(2)));
        assertEquals(0, work.runtime.getAllocatedCells());
        Target allocation = new Target(integers(1));
        allocation.runtime.allocate(ExpressionLimits.MAX_ALLOCATED_CELLS);
        assertUnchangedFailure(allocation, () -> allocation.apply(Operation.PUSH, DataValue.integer(2)));
        long[] values = new long[ExpressionLimits.MAX_CONTAINER_WIDTH];
        Target width = new Target(integers(values));
        assertUnchangedFailure(width, () -> width.apply(Operation.PUSH, DataValue.integer(2)));
        assertEquals(0, width.runtime.getAllocatedCells());
        DataType strings = DataType.dynamicArray(DataType.STRING);
        DataValue large = DataValue.string(String.join("", Collections.nCopies(4096, "x")));
        Target retained = new Target(DataValue.dynamicArray(strings, new DataValue[] { large, large, large, large }));
        assertUnchangedFailure(retained, () -> retained.apply(Operation.PUSH, DataValue.string("x")));
    }

    @Test
    void membershipReturnsFirstIndexAndUsesDeepTypedValueSemantics() {
        DataValue first = DataValue.struct(Collections.singletonMap("name", DataValue.string(new String("same"))));
        DataValue equal = DataValue.struct(Collections.singletonMap("name", DataValue.string(new String("same"))));
        DataValue missing = DataValue.struct(Collections.singletonMap("name", DataValue.string("other")));
        DataValue sequence = DataValue.array(Arrays.asList(first, first));
        DataRuntime runtime = new DataRuntime(null, 0);
        assertTrue(pure(Operation.CONTAINS, sequence, equal, runtime).asBoolean());
        assertEquals(0, pure(Operation.INDEXOF, sequence, equal, runtime).asLong());
        assertEquals(-1, pure(Operation.INDEXOF, sequence, missing, runtime).asLong());
        DataValue labels = DataValue.array(
            Collections.singletonList(
                DataValue.label(
                    LabelReference.of("ped")
                        .side(2)
                        .slots(1, 3))));
        assertTrue(
            pure(
                Operation.CONTAINS,
                labels,
                DataValue.label(
                    LabelReference.of("ped")
                        .side(2)
                        .slots(1, 3)),
                runtime).asBoolean());
        assertFalse(
            pure(
                Operation.CONTAINS,
                labels,
                DataValue.label(
                    LabelReference.of("ped")
                        .side(3)
                        .slots(1, 3)),
                runtime).asBoolean());
        DataValue selectors = DataValue
            .array(Collections.singletonList(DataValue.selector(ResourceSelector.item("minecraft:stone"))));
        assertTrue(
            pure(Operation.CONTAINS, selectors, DataValue.selector(ResourceSelector.item("minecraft:stone")), runtime)
                .asBoolean());
    }

    @Test
    void distinctStringComparisonsReserveWorkWhileSharedValuesUseConstantWork() {
        String text = String.join("", Collections.nCopies(4096, "x"));
        DataValue shared = DataValue.string(text);
        DataValue sequence = DataValue.array(Collections.singletonList(shared));
        DataRuntime fast = new DataRuntime(null, 0);
        fast.work(ExpressionLimits.MAX_WORK - 2);
        assertTrue(pure(Operation.CONTAINS, sequence, shared, fast).asBoolean());
        assertEquals(ExpressionLimits.MAX_WORK, fast.getWork());
        DataRuntime bounded = new DataRuntime(null, 0);
        bounded.work(ExpressionLimits.MAX_WORK - 100);
        DataValue separate = DataValue.string(new String(text));
        assertThrows(IllegalArgumentException.class, () -> pure(Operation.CONTAINS, sequence, separate, bounded));
    }

    @Test
    void invalidTypesAndImplicitOrFixedMutationAreRejected() {
        Target target = new Target(integers(1));
        assertUnchangedFailure(target, () -> target.apply(Operation.PUSH, DataValue.bool(true)));
        assertUnchangedFailure(
            target,
            () -> target.apply(Operation.INSERT, DataValue.bool(false), DataValue.integer(2)));
        assertUnchangedFailure(
            target,
            () -> target.apply(Operation.EXTEND, DataValue.array(Collections.singletonList(DataValue.bool(false)))));
        assertUnchangedFailure(
            target,
            () -> DynamicArrayFunctions.apply(Operation.CLEAR, target.value(), new DataValue[0], null, target.runtime));
        DataValue fixed = DataValue.array(Collections.singletonList(DataValue.integer(1)));
        assertThrows(
            IllegalArgumentException.class,
            () -> DynamicArrayFunctions
                .apply(Operation.POP, fixed, new DataValue[0], target.reference, target.runtime));
        assertEquals(
            1,
            DynamicArrayFunctions.apply(Operation.PEEK, fixed, new DataValue[0], null, target.runtime)
                .asLong());
    }

    private static DataValue pure(Operation operation, DataValue sequence, DataValue value, DataRuntime runtime) {
        return DynamicArrayFunctions.apply(operation, sequence, new DataValue[] { value }, null, runtime);
    }

    private static void assertUnchangedFailure(Target target, Runnable action) {
        DataValue before = target.value();
        long generation = target.epochs[0];
        assertThrows(IllegalArgumentException.class, action::run);
        assertSame(before, target.value());
        assertEquals(generation, target.epochs[0]);
    }

    private static void assertNumbers(DataValue array, long... expected) {
        assertEquals(expected.length, array.size());
        for (int i = 0; i < expected.length; i++) assertEquals(
            expected[i],
            array.get(i)
                .asLong());
    }

    private static DataValue integers(long... values) {
        DataValue[] result = new DataValue[values.length];
        for (int i = 0; i < values.length; i++) result[i] = DataValue.integer(values[i]);
        return DataValue.dynamicArray(INTEGERS, result);
    }

    private static final class Target {

        final DataRuntime runtime = new DataRuntime(null, 0);
        final DataValue[] cells;
        final long[] epochs = { 0 };
        final DataReference reference;

        Target(DataValue value) {
            cells = new DataValue[] { value };
            reference = DataReference.local(cells, epochs, 0, value.getType());
        }

        DataValue value() {
            return cells[0];
        }

        DataValue apply(Operation operation, DataValue... extra) {
            return DynamicArrayFunctions.apply(operation, value(), extra, reference, runtime);
        }
    }
}
