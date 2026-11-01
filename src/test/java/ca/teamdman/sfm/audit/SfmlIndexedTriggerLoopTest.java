package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.MANAGER;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistrySimple;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.audit.AuditRegressionTest.Fixture;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class SfmlIndexedTriggerLoopTest {

    private static final Item SCAN_ITEM = new Item();

    static {
        try {
            Field backing = RegistrySimple.class.getDeclaredField("registryObjects");
            backing.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> names = (Map<String, Object>) backing.get(Item.itemRegistry);
            names.put("sfm_indexed_trigger_loop:item", SCAN_ITEM);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    @Test
    void everyJavaForPhaseResumesWithoutRepeatingCompletedEffects() throws Exception {
        Run run = new Run(
            "STATE initial = 0 STATE tested = 0 STATE stepped = 0 STATE total = 0 STATE done = 0 "
                + "INTEGER start(REF INTEGER calls) { calls = calls + 1; WAIT 1; RETURN 0; } "
                + "BOOLEAN check(REF INTEGER calls, INTEGER value) { calls = calls + 1; WAIT 1; RETURN value < 2; } "
                + "VOID advance(REF INTEGER calls, REF INTEGER value) { calls = calls + 1; WAIT 1; value = value + 1; } "
                + "EVERY 20 TICKS { FOR (LET i = start(REF initial); check(REF tested, i); advance(REF stepped, REF i)) { "
                + "total = total + i + 1; WAIT 1; } done = 1; }");
        run.tick(20);
        assertEquals(1, run.value(0));
        assertEquals(0, run.value(1));
        run.tick(21);
        assertEquals(1, run.value(1));
        run.tick(22);
        assertEquals(1, run.value(3));
        run.tick(23);
        assertEquals(1, run.value(2));
        for (int tick = 24; tick <= 28; tick++) run.tick(tick);
        assertEquals(1, run.value(0));
        assertEquals(3, run.value(1));
        assertEquals(2, run.value(2));
        assertEquals(3, run.value(3));
        assertEquals(1, run.value(4));
        assertEquals(
            0,
            run.manager.getProgramExecution(run.program)
                .sleepingCount());
    }

    @Test
    void falseFirstTestStillFinishesInitializerAndSkipsBodyAndUpdate() throws Exception {
        Run run = new Run(
            "STATE initial = 0 STATE body = 0 STATE stepped = 0 STATE done = 0 "
                + "VOID start(REF INTEGER count) { count = count + 1; WAIT 1; } "
                + "VOID advance(REF INTEGER count) { count = count + 1; } "
                + "EVERY 20 TICKS DO FOR (start(REF initial); FALSE; advance(REF stepped)) DO "
                + "body = body + 1 END done = 1 END");
        run.tick(20);
        assertEquals(1, run.value(0));
        assertEquals(0, run.value(3));
        run.tick(21);
        assertEquals(1, run.value(0));
        assertEquals(0, run.value(1));
        assertEquals(0, run.value(2));
        assertEquals(1, run.value(3));
    }

    @Test
    void optionalClausesAndExistingMutableAssignmentsExecuteInOrder() throws Exception {
        Run run = new Run(
            "STATE total = 0 EVERY 20 TICKS { LET value = 0; "
                + "FOR (value = 2; value < 5; value = value + 1) { total = total * 10 + value; } "
                + "FOR (; value < 7; ) { value = value + 1; total = total * 10 + value; } "
                + "FOR (; FALSE; ) {} }");
        run.tick(20);
        assertEquals(23467, run.value(0));
    }

    @Test
    void emptyInfiniteLoopsRemainBoundedWithAndWithoutContinuationFrames() throws Exception {
        for (String header : new String[] { ";;", "; TRUE;" }) for (String tail : new String[] { "", "WAIT 0;" }) {
            Run run = new Run("EVERY 20 TICKS { FOR (" + header + ") {} " + tail + " }");
            IllegalArgumentException failure = assertThrows(IllegalArgumentException.class, () -> run.tick(20));
            assertTrue(
                failure.getMessage()
                    .contains("Loop step budget"),
                failure.getMessage());
        }
        StringBuilder nested = new StringBuilder("EVERY 20 TICKS {");
        for (int i = 0; i < 17; i++) nested.append("FOR (; FALSE;) {");
        for (int i = 0; i < 18; i++) nested.append('}');
        ProgramBuildResult result = new ProgramBuilder(nested.toString()).build();
        assertFalse(result.isSuccess());
        assertTrue(
            result.getErrorMessage()
                .contains("Loop nesting exceeds 16"),
            result.getErrorMessage());
    }

    @Test
    void headerAndBodyScopesStaySeparateAndJavaForTestMustBeBoolean() {
        String[] bodies = { "FOR (LET i = 0; i < 1; i = i + 1) {} PRINT(i);",
            "FOR (LET i = 0; i < 1; local = local + 1) { LET local = 0; }",
            "LET i = 4; FOR (LET i = 0; i < 1; i = i + 1) {}", "FOR (; 1;) {}", "FOR (; FALSE; LET i = 0) {}",
            "FOR (WAIT 1; FALSE;) {}", "FOR (LET i = 0; i < 1; i = i + 1) { LET i = 9; }" };
        for (String body : bodies) {
            ProgramBuildResult result = new ProgramBuilder("EVERY 20 TICKS { " + body + " }").build();
            assertFalse(result.isSuccess(), body);
        }
    }

    @Test
    void indexedFixedAndDynamicIterationUseOneSnapshotAcrossWaitAndSourceReplacement() throws Exception {
        Run fixed = new Run(
            "STATE total = 0 STATE done = 0 EVERY 20 TICKS { LET values = [5, 7, 9]; "
                + "FOR (index, value) IN values { total = total + index * 10 + value; values = [1, 2, 3]; WAIT 1; } "
                + "done = 1; }");
        fixed.tick(20);
        fixed.tick(21);
        fixed.tick(22);
        fixed.tick(23);
        assertEquals(51, fixed.value(0));
        assertEquals(1, fixed.value(1));
        Run dynamic = new Run(
            "STATE total = 0 STATE size = 0 EVERY 20 TICKS { LET values = ARRAY([4, 6]); "
                + "FOR (index, value) IN values { total = total + index * 10 + value; PUSH(REF values, 100); WAIT 1; } "
                + "size = LEN(values); }");
        dynamic.tick(20);
        dynamic.tick(21);
        dynamic.tick(22);
        assertEquals(20, dynamic.value(0));
        assertEquals(4, dynamic.value(1));
    }

    @Test
    void indexedSnapshotExpressionRunsOnceEvenWhenItsFunctionWaits() throws Exception {
        Run run = new Run(
            "STATE observed = 0 STATE total = 0 STATE done = 0 "
                + "INTEGER[2] capture(REF INTEGER count) { count = count + 1; WAIT 1; RETURN [4, 6]; } "
                + "EVERY 20 TICKS { FOR (index, value) IN capture(REF observed) { "
                + "total = total + index * 10 + value; WAIT 1; } done = 1; }");
        for (int tick = 20; tick <= 23; tick++) run.tick(tick);
        assertEquals(1, run.value(0));
        assertEquals(20, run.value(1));
        assertEquals(1, run.value(2));
    }

    @Test
    void indexedNativeContentsRetainCapturedRowsAfterWorldStockChanges() throws Exception {
        Run run = new Run(
            "STATE total = 0 EVERY 20 TICKS { "
                + "FOR (index, row) IN LABEL(\"source\").contents() { WAIT 1; total = total + index + row.total; } }");
        run.fixture.source.setInventorySlotContents(0, new ItemStack(SCAN_ITEM, 2));
        run.fixture.source.setInventorySlotContents(1, new ItemStack(SCAN_ITEM, 3));
        run.tick(20);
        run.fixture.source.setInventorySlotContents(0, new ItemStack(SCAN_ITEM, 50));
        run.tick(21);
        assertEquals(5, run.value(0));
    }

    @Test
    void indexedBindersAreReadonlyCannotEscapeAndEmptySnapshotsSkipTheirBody() throws Exception {
        String[] bodies = { "FOR (index, value) IN [1] { index = 4; }", "FOR (index, value) IN [1] { value = 4; }",
            "FOR (index, value) IN [1] { change(REF index); }", "FOR (index, value) IN [1] { change(REF value); }",
            "FOR (index, index) IN [1] {}", "FOR (index, value) IN [1] {} PRINT(index);",
            "FOR (index, value) IN 1 {}" };
        for (String body : bodies) {
            ProgramBuildResult result = new ProgramBuilder(
                "VOID change(REF INTEGER value) { value = 1; } " + "EVERY 20 TICKS { " + body + " }").build();
            assertFalse(result.isSuccess(), body);
        }
        Run empty = new Run(
            "STATE total = 0 EVERY 20 TICKS { "
                + "FOR (index, value) IN ARRAY(\"integer\") { total = total + 1; WAIT 1; } }");
        empty.tick(20);
        assertEquals(0, empty.value(0));
        assertEquals(
            0,
            empty.manager.getProgramExecution(empty.program)
                .sleepingCount());
    }

    @Test
    void bothFormsRoundTripThroughAstSourceWithoutLosingHeaderSeparators() throws Exception {
        Run original = new Run(
            "STATE total = 0 EVERY 20 TICKS { " + "FOR (LET i = 0; i < 3; i = i + 1) { total = total + i; } "
                + "FOR (index, value) IN [4, 5] { total = total + index + value; } FOR (; FALSE;) {} }");
        String printed = original.program.toString();
        assertTrue(printed.contains("FOR (LET $i = 0;"), printed);
        assertTrue(printed.contains("FOR ($index, $value) IN"), printed);
        Run reopened = new Run(printed);
        original.tick(20);
        reopened.tick(20);
        assertEquals(13, original.value(0));
        assertEquals(original.value(0), reopened.value(0));
    }

    private static final class Run {

        final Fixture fixture = new Fixture();
        final TileEntityManager manager = RecoveryFixtures.manager(fixture);
        final Program program;
        final DataValue[] values;

        Run(String source) throws Exception {
            ProgramBuildResult built = new ProgramBuilder(source).build();
            assertTrue(built.isSuccess(), built.getErrorMessage());
            program = built.getProgram();
            values = program.getStateSchema()
                .getDeclarations()
                .stream()
                .map(declaration -> declaration.getDefaultValue())
                .toArray(DataValue[]::new);
        }

        void tick(long now) {
            fixture.world.time = now;
            ProgramContext context = new ProgramContext(
                fixture.world,
                manager,
                new CableNetwork(fixture.world, MANAGER),
                fixture.labels,
                now,
                false);
            context.setStateAccess(new StateAccess() {

                @Override
                public DataValue read(int slot) {
                    return values[slot];
                }

                @Override
                public void write(int slot, DataValue value) {
                    values[slot] = value;
                }
            });
            program.tick(context);
        }

        long value(int slot) {
            return values[slot].asLong();
        }
    }
}
