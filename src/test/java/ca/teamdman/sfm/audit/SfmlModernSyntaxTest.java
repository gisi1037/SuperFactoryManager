package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ProgramStateSchema;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

/** The modern forms compile to the same typed blocks and bounded invocation frames. */
class SfmlModernSyntaxTest {

    @Test
    void typeFirstFunctionsAndBraceControlFlowExecuteAndRoundTrip() {
        String source = "state answer = 0 "
            + "integer sum(integer n) { let total = 0; for i = 1 to n { total = total + i; } "
            + "while(total < 8) { total = total + 1; } if(total = 8) { return total; } else { return 0; } } "
            + "every 20 ticks { answer = sum(3); }";
        Program program = program(source);
        assertEquals(8, run(program).values[0].asLong());
        assertEquals(8, run(program(program.toString())).values[0].asLong());
    }

    @Test
    void typeFirstThenAndDoBodiesPreserveOldDollarBindings() {
        Program program = program(
            "state $answer = 0 integer increment(integer $n) then return $n + 1 end "
                + "every 20 ticks do $answer = increment(4) end");
        assertEquals(5, run(program).values[0].asLong());
    }

    @Test
    void arrayForBraceElseIfAndFunctionsAfterTriggersAreAccepted() {
        Program program = program(
            "state answer = 0 every 20 ticks { answer = sum(); } "
                + "integer sum() { let total = 0; for value in [2, 3] { total = total + value; } "
                + "if(total = 0) { return 0; } else if(total = 5) { return total; } else { return 1; } }");
        assertEquals(5, run(program).values[0].asLong());
    }

    @Test
    void voidProceduresCanFallThroughReturnAndCallOtherProcedures() {
        Program program = program(
            "void first(integer n) { if(n = 0) { return; } second(n); } "
                + "void second(integer n) then let copy = n end "
                + "every 20 ticks { first(0); first(2); }");
        assertDoesNotThrow(() -> run(program));
        assertTrue(
            new ProgramBuilder(program.toString()).build()
                .isSuccess());
    }

    @Test
    void voidIsNotAValueAndValueFunctionsCannotInvokeProcedures() {
        invalid("void foo() {} every 20 ticks { let bad = foo(); }");
        invalid("void foo() {} integer bar() { foo(); return 1; } every 20 ticks {}");
        invalid("void foo(void value) {} every 20 ticks {}");
        invalid("void foo() { return 1; } every 20 ticks {}");
        invalid("integer foo() { return; } every 20 ticks {}");
        invalid("integer foo() { let value = 1; } every 20 ticks {}");
    }

    @Test
    void valueFunctionsStillRejectEffectsButVoidProceduresAcceptThem() {
        invalid("integer foo() { print(1); return 1; } every 20 ticks {}");
        Program program = program("void foo() { print(1); } every 20 ticks { foo(); }");
        assertDoesNotThrow(() -> run(program));
    }

    @Test
    void mixedDelimitersCannotSilentlyCloseTheWrongBlock() {
        invalid("every 20 ticks { if(true) then print(1); } end");
        invalid("every 20 ticks do if(true) { print(1); end }");
    }

    @Test
    void procedureActionsAndNestedWaitingValuesResumeWithoutRepeatingTransfers() throws Exception {
        Program program = program(
            "integer amount() { wait(1); return 2; } "
                + "void move(label_ref src, label_ref dest) { transfer(src, dest, 1); wait(2); "
                + "transfer(src, dest, amount()); } every 10 ticks { move(label(\"source\"), label(\"dest\")); }");
        Fixture fixture = new Fixture();
        fixture.source.setInventorySlotContents(0, new ItemStack(procedureFixtureItem(), 5));
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        MemoryState state = new MemoryState(program.getStateSchema());
        for (long time : new long[] { 10, 11, 12, 13 }) {
            fixture.world.time = time;
            ProgramContext context = new ProgramContext(
                fixture.world,
                manager,
                new CableNetwork(fixture.world, MANAGER),
                fixture.labels,
                time,
                false);
            context.setStateAccess(state);
            program.tick(context);
            assertEquals(time < 13 ? 1 : 3, count(fixture.dest));
            assertEquals(5, count(fixture.source) + count(fixture.dest));
        }
        assertEquals(
            0,
            manager.getProgramExecution(program)
                .sleepingCount());
    }

    @SuppressWarnings("deprecation")
    private static Item procedureFixtureItem() {
        String name = "sfm_modern_syntax:procedure_transfer";
        try {
            java.lang.reflect.Field field = net.minecraft.util.RegistrySimple.class.getDeclaredField("registryObjects");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> names = (java.util.Map<String, Object>) field.get(Item.itemRegistry);
            Item existing = (Item) names.get(name);
            if (existing != null) return existing;
            Item item = new Item();
            names.put(name, item); // Forge's addObject entry point requires LaunchClassLoader, absent in JUnit.
            return item;
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    private static void invalid(String source) {
        assertFalse(
            new ProgramBuilder(source).build()
                .isSuccess(),
            source);
    }

    private static Program program(String source) {
        ProgramBuildResult result = new ProgramBuilder(source).build();
        assertTrue(result.isSuccess(), result.getErrorMessage());
        return result.getProgram();
    }

    private static MemoryState run(Program program) {
        MemoryState state = new MemoryState(program.getStateSchema());
        ProgramContext context = new ProgramContext(null, null, null, null, 20, false);
        context.setStateAccess(state);
        program.tick(context);
        return state;
    }

    private static final class MemoryState implements StateAccess {

        final DataValue[] values;

        MemoryState(ProgramStateSchema schema) {
            values = new DataValue[schema.size()];
            for (int i = 0; i < values.length; i++) values[i] = schema.getDeclarations()
                .get(i)
                .getDefaultValue();
        }

        public DataValue read(int slot) {
            return values[slot];
        }

        public void write(int slot, DataValue value) {
            values[slot] = value;
        }
    }
}
