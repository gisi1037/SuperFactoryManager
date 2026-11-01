package ca.teamdman.sfml.data;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

class DynamicReferenceGenerationTest {

    private static final DataType INTEGERS = DataType.dynamicArray(DataType.INTEGER);

    @Test
    void elementWritesPreserveAliasesButWholeReplacementInvalidatesIndexedReferences() {
        DataCompiler.Binding binding = binding(INTEGERS);
        DataRuntime runtime = new DataRuntime(null, 1);
        binding.write(runtime, integers(1, 2));
        DataReference whole = runtime.reference(binding, new int[0]);
        DataReference first = whole.select(runtime, new int[] { 0 });
        DataReference sibling = whole.select(runtime, new int[] { 1 });
        long epoch = runtime.generation(false, 0);
        first.write(runtime, DataValue.integer(7));
        assertEquals(epoch, runtime.generation(false, 0));
        assertEquals(
            2,
            sibling.read(runtime)
                .asLong());
        whole.write(runtime, integers(3, 4, 5));
        assertEquals(epoch + 1, runtime.generation(false, 0));
        assertEquals(
            3,
            whole.read(runtime)
                .size());
        assertThrows(IllegalArgumentException.class, () -> first.read(runtime));
        assertThrows(IllegalArgumentException.class, () -> sibling.write(runtime, DataValue.integer(9)));
        assertEquals(
            4,
            whole.read(runtime)
                .get(1)
                .asLong());
        assertThrows(IllegalArgumentException.class, () -> whole.select(runtime, new int[] { 3 }));
    }

    @Test
    void nestedStructuralWriterRefreshesItselfAndConservativelyInvalidatesOtherAliases() {
        DataType nested = DataType.dynamicArray(INTEGERS);
        DataCompiler.Binding binding = binding(nested);
        DataRuntime runtime = new DataRuntime(null, 1);
        binding.write(runtime, DataValue.dynamicArray(nested, new DataValue[] { integers(1), integers(2) }));
        DataReference whole = runtime.reference(binding, new int[0]);
        DataReference writer = whole.select(runtime, new int[] { 0 });
        DataReference other = whole.select(runtime, new int[] { 1 });
        DataReference forwardedAlias = writer.select(runtime, new int[0]);
        writer.write(runtime, integers(3, 4));
        assertEquals(
            2,
            writer.read(runtime)
                .size());
        writer.write(runtime, integers(5));
        assertEquals(
            5,
            writer.read(runtime)
                .get(0)
                .asLong());
        assertThrows(IllegalArgumentException.class, () -> other.read(runtime));
        assertThrows(IllegalArgumentException.class, () -> forwardedAlias.read(runtime));
        assertEquals(
            2,
            whole.read(runtime)
                .get(1)
                .get(0)
                .asLong());
    }

    @Test
    void refParameterGenerationAndPublicationResolveToTheCallingRootAcrossRebinding() {
        DataCompiler.Binding binding = binding(INTEGERS);
        DataRuntime caller = new DataRuntime(null, 1);
        binding.write(caller, integers(1));
        DataReference whole = caller.reference(binding, new int[0]);
        DataReference indexed = whole.select(caller, new int[] { 0 });
        DataRuntime callee = new DataRuntime(null, 1);
        callee.bindReference(0, whole);
        assertEquals(caller.generation(false, 0), callee.generation(false, 0));
        callee.detach();
        callee.bind(new DataRuntime(null, 0));
        callee.write(false, 0, integers(2, 3));
        assertEquals(caller.generation(false, 0), callee.generation(false, 0));
        assertEquals(
            2,
            caller.read(false, 0)
                .size());
        assertThrows(IllegalArgumentException.class, () -> indexed.read(caller));
    }

    @Test
    void failedLocalPublicationAndCounterOverflowLeaveValueAndGenerationUntouched() {
        DataValue initial = integers(1);
        DataValue[] cells = { initial };
        long[] epochs = { Long.MAX_VALUE };
        DataReference whole = DataReference.local(cells, epochs, 0, INTEGERS);
        DataRuntime runtime = new DataRuntime(null, 0);
        assertThrows(IllegalArgumentException.class, () -> whole.write(runtime, DataValue.integer(2)));
        assertThrows(ArithmeticException.class, () -> whole.write(runtime, integers(2)));
        assertSame(initial, cells[0]);
        assertEquals(Long.MAX_VALUE, epochs[0]);
        DataReference element = whole.select(runtime, new int[] { 0 });
        element.write(runtime, DataValue.integer(8));
        assertEquals(
            8,
            cells[0].get(0)
                .asLong());
        assertEquals(Long.MAX_VALUE, epochs[0]);
    }

    @Test
    void globalBoundsAndOverflowAreCheckedBeforeEitherValueOrGenerationPublication() throws Exception {
        DataType strings = DataType.dynamicArray(DataType.STRING);
        GlobalDeclaration declaration = new GlobalDeclaration(binding(strings), null);
        ProgramGlobals globals = new ProgramGlobals(Arrays.asList(declaration, declaration));
        DataValue empty = DataValue.dynamicArray(strings, new DataValue[0]);
        globals.write(0, empty);
        globals.write(1, empty);
        DataValue text = DataValue.string(String.join("", Collections.nCopies(4096, "x")));
        DataValue large = DataValue.dynamicArray(strings, new DataValue[] { text, text, text });
        globals.write(0, large);
        long unchanged = globals.generation(1);
        assertThrows(IllegalArgumentException.class, () -> globals.write(1, large));
        assertSame(empty, globals.read(1));
        assertEquals(unchanged, globals.generation(1));
        Field field = ProgramGlobals.class.getDeclaredField("generations");
        field.setAccessible(true);
        ((long[]) field.get(globals))[1] = Long.MAX_VALUE;
        assertThrows(ArithmeticException.class, () -> globals.write(1, empty));
        assertSame(empty, globals.read(1));
        assertEquals(Long.MAX_VALUE, globals.generation(1));
    }

    @Test
    void fixedOnlyFramesKeepTheirExistingAllocationChargeAndNoEpochs() {
        DataCompiler.Binding binding = binding(DataType.array(DataType.INTEGER, 1));
        DataRuntime runtime = new DataRuntime(null, 1);
        binding.write(runtime, DataValue.array(Collections.singletonList(DataValue.integer(1))));
        DataReference reference = runtime.reference(binding, new int[] { 0 });
        assertEquals(0, runtime.getAllocatedCells());
        assertEquals(0, runtime.generation(false, 0));
        reference.write(runtime, DataValue.integer(2));
        assertEquals(0, runtime.generation(false, 0));
    }

    @Test
    void addressedGlobalsResolveFreshStoreGenerationsAfterDetachAndBind() {
        ProgramGlobals globals = new ProgramGlobals(
            Collections.singletonList(new GlobalDeclaration(binding(INTEGERS), null)));
        globals.write(0, integers(1));
        DataRuntime runtime = new DataRuntime(null, 0);
        runtime.setGlobals(globals);
        DataReference whole = DataReference.addressed(DataCompiler.BindingKind.GLOBAL, 0, INTEGERS);
        DataReference indexed = whole.select(runtime, new int[] { 0 });
        runtime.detach();
        DataRuntime live = new DataRuntime(null, 0);
        live.setGlobals(globals);
        runtime.bind(live);
        whole.write(runtime, integers(2, 3));
        assertEquals(
            2,
            whole.read(runtime)
                .size());
        assertThrows(IllegalArgumentException.class, () -> indexed.read(runtime));
    }

    private static DataCompiler.Binding binding(DataType type) {
        DataCompiler compiler = new DataCompiler();
        compiler.beginTrigger();
        compiler.pushScope();
        return compiler.declareLocal("values", type);
    }

    private static DataValue integers(long... values) {
        DataValue[] result = new DataValue[values.length];
        for (int i = 0; i < values.length; i++) result[i] = DataValue.integer(values[i]);
        return DataValue.dynamicArray(INTEGERS, result);
    }
}
