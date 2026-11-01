package ca.teamdman.sfml.data;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;

class DynamicStateRuntimeTest {

    private static final DataType INTEGERS = DataType.dynamicArray(DataType.INTEGER);

    @Test
    void plainAdaptersRemainValidForFixedStateButCannotSilentlyLoseDynamicEpochs() {
        DataValue[] cell = { DataValue.integer(1) };
        StateAccess plain = new StateAccess() {

            @Override
            public DataValue read(int slot) {
                return cell[slot];
            }

            @Override
            public void write(int slot, DataValue value) {
                cell[slot] = value;
            }
        };
        DataRuntime runtime = new DataRuntime(plain, 0);
        runtime.write(true, 0, DataValue.integer(2));
        assertEquals(
            2,
            runtime.read(true, 0)
                .asLong());
        assertThrows(IllegalArgumentException.class, () -> runtime.write(true, 0, integers(3)));
        assertEquals(2, cell[0].asLong());
        cell[0] = integers(4);
        assertThrows(IllegalArgumentException.class, () -> runtime.read(true, 0));
        assertThrows(IllegalArgumentException.class, () -> runtime.generation(true, 0));
    }

    @Test
    void stateElementWritesPreserveAliasesAndStructuralWritesInvalidateThem() {
        ProgramGlobals store = store();
        store.write(0, integers(1, 2));
        DataRuntime runtime = new DataRuntime(store, 0);
        DataReference whole = DataReference.addressed(DataCompiler.BindingKind.STATE, 0, INTEGERS);
        DataReference first = whole.select(runtime, new int[] { 0 });
        DataReference second = whole.select(runtime, new int[] { 1 });
        long epoch = runtime.generation(true, 0);
        first.write(runtime, DataValue.integer(8));
        assertEquals(epoch, runtime.generation(true, 0));
        assertEquals(
            2,
            second.read(runtime)
                .asLong());
        whole.write(runtime, integers(3));
        assertEquals(epoch + 1, runtime.generation(true, 0));
        assertThrows(IllegalArgumentException.class, () -> first.read(runtime));
        assertThrows(IllegalArgumentException.class, () -> second.write(runtime, DataValue.integer(9)));
        assertEquals(
            3,
            store.read(0)
                .get(0)
                .asLong());
    }

    @Test
    void detachedFramesAcceptSameHeapAndRejectSameEpochReplacementHeap() {
        ProgramGlobals store = store();
        store.write(0, integers(1));
        DataRuntime frame = new DataRuntime(store, 0);
        frame.read(true, 0);
        frame.detach();
        frame.bind(new DataRuntime(store, 0));
        assertEquals(
            1,
            frame.read(true, 0)
                .get(0)
                .asLong());
        frame.detach();
        ProgramGlobals replacement = store();
        replacement.write(0, integers(9));
        assertEquals(store.generation(0), replacement.generation(0));
        assertThrows(IllegalArgumentException.class, () -> frame.bind(new DataRuntime(replacement, 0)));
        frame.bind(new DataRuntime(store, 0));
        assertEquals(
            1,
            frame.read(true, 0)
                .get(0)
                .asLong());
    }

    private static ProgramGlobals store() {
        DataCompiler compiler = new DataCompiler();
        compiler.pushScope();
        DataCompiler.Binding binding = compiler.declareLocal("values", INTEGERS);
        return new ProgramGlobals(Collections.singletonList(new GlobalDeclaration(binding, null)));
    }

    private static DataValue integers(long... values) {
        DataValue[] cells = new DataValue[values.length];
        for (int i = 0; i < values.length; i++) cells[i] = DataValue.integer(values[i]);
        return DataValue.dynamicArray(INTEGERS, cells);
    }
}
