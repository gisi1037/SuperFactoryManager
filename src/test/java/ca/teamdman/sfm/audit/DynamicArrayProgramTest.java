package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.CompiledFunction;
import ca.teamdman.sfml.data.Continuation;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ProgramExecution;
import ca.teamdman.sfml.data.ProgramGlobals;
import ca.teamdman.sfml.data.VersionedStateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

/** Exercise collection effects through the actual compiler, program scheduler and manager-owned RAM. */
class DynamicArrayProgramTest {

    @Test
    void typedEmptyPushPopAndDiscardUseActualLengthsAndRejectEmptyPopWithoutPublication() throws Exception {
        Run run = new Run(
            "STATE popped = 0 STATE length = 99 LET values = ARRAY(\"integer\") "
                + "EVERY 10 TICKS { PUSH(ref values, 4); PUSH(ref values, 7); "
                + "popped = POP(ref values); POP(ref values); length = LEN(values); }");
        run.tick(10);
        assertEquals(7, run.number(0));
        assertEquals(0, run.number(1));
        assertArray(run.global(0));
        Run empty = new Run("LET values = ARRAY(\"integer\") EVERY 10 TICKS { POP(ref values); }");
        assertThrows(IllegalArgumentException.class, () -> empty.tick(10));
        assertArray(empty.global(0));
        compile(run.program.toString());
    }

    @Test
    void dynamicLabelQueueCanBePassedByRefAndReturnTheRemovedLabel() throws Exception {
        Run run = new Run(
            "LET targets = ARRAY(\"label_ref\") LET chosen = LABEL(\"initial\") "
                + "label_ref take(ref label_ref[] queue) { RETURN POP(ref queue); } "
                + "EVERY 10 TICKS { PUSH(ref targets, LABEL(\"source\")); PUSH(ref targets, LABEL(\"dest\")); "
                + "chosen = take(ref targets); }");
        run.tick(10);
        assertEquals(
            "dest",
            run.global(1)
                .asLabel()
                .getName());
        assertEquals(
            1,
            run.global(0)
                .size());
        assertEquals(
            "source",
            run.global(0)
                .get(0)
                .asLabel()
                .getName());
        compile(run.program.toString());
    }

    @Test
    void ordinaryParametersAndReturnedArraysKeepValueSemanticsWhileRefPublishes() throws Exception {
        Run run = new Run(
            "LET values = ARRAY([1,2]) LET copied = ARRAY(\"integer\") "
                + "void local(integer[] copy) { PUSH(ref copy, 9); copy[0] = 8; } "
                + "integer[] duplicate(integer[] copy) { PUSH(ref copy, 4); RETURN copy; } "
                + "void grow(ref integer[] target) { PUSH(ref target, 3); } "
                + "EVERY 10 TICKS { local(values); copied = duplicate(values); grow(ref values); }");
        run.tick(10);
        assertArray(run.global(0), 1, 2, 3);
        assertArray(run.global(1), 1, 2, 4);
        compile(run.program.toString());
    }

    @Test
    void wrongTypesFixedArraysImplicitRefsUntypedEmptyAndOpaqueDynamicStateAreRejected() {
        String[] invalid = { "EVERY 10 TICKS { LET values = ARRAY(\"integer\"); PUSH(ref values, TRUE); }",
            "EVERY 10 TICKS { LET values = [1,2]; PUSH(ref values, 3); }",
            "EVERY 10 TICKS { LET values = [1,2]; LET last = POP(ref values); }",
            "EVERY 10 TICKS { LET values = ARRAY([1]); PUSH(values, 2); }",
            "EVERY 10 TICKS { LET values = ARRAY([1]); LET last = POP(values); }",
            "EVERY 10 TICKS { LET values = ARRAY([1]); LET result = PUSH(ref values, 2); }",
            "EVERY 10 TICKS { LET values = ARRAY([]); }", "EVERY 10 TICKS { LET values = ARRAY(); }",
            "EVERY 10 TICKS { LET values = ARRAY(\"void\"); }", "STATE values = ARRAY(\"label_ref\") EVERY 10 TICKS {}",
            "STATE values = {nested: ARRAY([LABEL(\"a\")])} EVERY 10 TICKS {}",
            "STATE values = [ARRAY([ITEM(\"*\")])] EVERY 10 TICKS {}",
            "void fixed(ref integer[2] values) {} EVERY 10 TICKS { LET values = ARRAY([1,2]); fixed(ref values); }",
            "void dynamic(ref integer[] values) {} EVERY 10 TICKS { LET values = [1,2]; dynamic(ref values); }" };
        for (String source : invalid) assertFalse(
            new ProgramBuilder(source).build()
                .isSuccess(),
            source);
    }

    @Test
    void foreachKeepsItsOriginalSnapshotWhenTheLiveArrayGrows() throws Exception {
        Run run = new Run(
            "STATE sum = 0 STATE length = 0 LET values = ARRAY([1,2,3]) "
                + "EVERY 10 TICKS { FOR value IN values DO sum = sum + value; PUSH(ref values, value + 10); END "
                + "length = LEN(values); }");
        run.tick(10);
        assertEquals(6, run.number(0));
        assertEquals(6, run.number(1));
        assertArray(run.global(0), 1, 2, 3, 11, 12, 13);
    }

    @Test
    void ordinaryElementWritesKeepSiblingIndexedReferencesUsable() throws Exception {
        Run run = new Run(
            "LET values = ARRAY([1,2]) " + "void edit(ref integer first, ref integer secondValue) { "
                + "first = first + 10; secondValue = secondValue + first; first = first + 1; } "
                + "EVERY 10 TICKS { edit(ref values[0], ref values[1]); }");
        run.tick(10);
        assertArray(run.global(0), 12, 13);
    }

    @Test
    void indexedAssignmentFaultsAfterItsRhsPopWithoutClobberingTheRemainingTarget() throws Exception {
        Run run = new Run(
            "STATE done = 0 LET values = ARRAY([10,20,30]) "
                + "EVERY 10 TICKS { values[0] = POP(ref values); done = 1; }");
        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class, () -> run.tick(10));
        assertTrue(
            failure.getMessage()
                .contains("invalidated"),
            failure.getMessage());
        assertArray(run.global(0), 10, 20);
        assertEquals(0, run.number(0));
    }

    @Test
    void indexedRefsAcrossWaitRejectOtherTriggerGrowthRebindingAndSameValueAba() throws Exception {
        for (String mutation : new String[] { "PUSH(ref values, 3);", "values = ARRAY([8,9]);",
            "LET removed = POP(ref values); PUSH(ref values, removed);" }) {
            Run run = new Run(
                "STATE done = 0 LET values = ARRAY([1,2]) " + "void later(ref integer cell) { WAIT(2); cell = 99; } "
                    + "EVERY 10 TICKS { later(ref values[0]); done = 1; } "
                    + "EVERY 1 TICKS { "
                    + mutation
                    + " }");
            run.tick(10);
            DataValue afterMutation = run.global(0);
            IllegalArgumentException failure = assertThrows(IllegalArgumentException.class, () -> run.tick(12));
            assertTrue(
                failure.getMessage()
                    .contains("invalidated"),
                failure.getMessage());
            assertEquals(afterMutation, run.global(0), "A stale write must not reach the current array");
            assertEquals(0, run.number(0));
        }
    }

    @Test
    void persistentDynamicRefsAndCapturedAssignmentsRejectStructuralChangesAcrossWait() throws Exception {
        for (String target : new String[] { "later(ref values[0]);", "values[0] = numberLater();" }) {
            for (String mutation : new String[] { "PUSH(ref values, 3);", "values = ARRAY([8,9]);",
                "LET removed = POP(ref values); PUSH(ref values, removed);" }) {
                Run run = new Run(
                    "STATE done = 0 STATE values = ARRAY([1,2]) "
                        + "void later(ref integer cell) { WAIT(2); cell = 99; } "
                        + "integer numberLater() { WAIT(2); RETURN 99; } "
                        + "EVERY 10 TICKS { "
                        + target
                        + " done = 1; } EVERY 1 TICKS { "
                        + mutation
                        + " }");
                run.tick(10);
                DataValue afterMutation = run.state.read(1);
                IllegalArgumentException failure = assertThrows(IllegalArgumentException.class, () -> run.tick(12));
                assertTrue(
                    failure.getMessage()
                        .contains("invalidated"),
                    failure.getMessage());
                assertSame(afterMutation, run.state.read(1));
                assertEquals(0, run.number(0));
            }
        }
    }

    @Test
    void persistentWholeArrayRefsResumeAgainstCurrentContentsAndDoNotReplayEarlierPop() throws Exception {
        Run run = new Run(
            "STATE changed = FALSE STATE answer = 0 STATE values = ARRAY([3,5,7]) "
                + "integer later() { WAIT(2); RETURN 100; } "
                + "EVERY 10 TICKS { answer = POP(ref values) + later(); PUSH(ref values, 9); } "
                + "EVERY 1 TICKS { IF (!changed) THEN PUSH(ref values, 2); changed = TRUE; END }");
        run.tick(10);
        assertArray(run.state.read(2), 3, 5, 2);
        run.tick(11);
        assertArray(run.state.read(2), 3, 5, 2);
        run.tick(12);
        assertEquals(107, run.number(1));
        assertArray(run.state.read(2), 3, 5, 2, 9);
    }

    @Test
    void wholeArrayRefCapturedBeforeWaitingPushValueRereadsCurrentRoot() throws Exception {
        Run run = new Run(
            "STATE changed = FALSE LET values = ARRAY([1]) " + "integer later() { WAIT(2); RETURN 9; } "
                + "EVERY 10 TICKS { PUSH(ref values, later()); } "
                + "EVERY 1 TICKS { IF (NOT changed) THEN PUSH(ref values, 2); changed = TRUE; END }");
        run.tick(10);
        assertArray(run.global(0), 1, 2);
        run.tick(11);
        assertArray(run.global(0), 1, 2);
        run.tick(12);
        assertArray(run.global(0), 1, 2, 9);
    }

    @Test
    void popBeforeWaitingSecondOperandCompletesExactlyOnce() throws Exception {
        Run run = new Run(
            "STATE answer = 0 LET values = ARRAY([3,5,7]) " + "integer later() { WAIT(2); RETURN 100; } "
                + "EVERY 10 TICKS { answer = POP(ref values) + later(); }");
        run.tick(10);
        assertArray(run.global(0), 3, 5);
        assertEquals(0, run.number(0));
        run.tick(11);
        assertArray(run.global(0), 3, 5);
        run.tick(12);
        assertEquals(107, run.number(0));
        assertArray(run.global(0), 3, 5);
        run.tick(13);
        assertArray(run.global(0), 3, 5);
    }

    @Test
    void forwardedNestedArrayWriterInvalidatesOuterAndSiblingIndexedAliasesConservatively() throws Exception {
        for (String subsequent : new String[] { "LET length = LEN(first);", "LET length = LEN(secondValue);" }) {
            Run run = new Run(
                "LET rows = ARRAY(\"integer[]\") " + "void grow(ref integer[] row) { PUSH(ref row, 2); } "
                    + "void outer(ref integer[] first, ref integer[] secondValue) { grow(ref first); "
                    + subsequent
                    + " } "
                    + "EVERY 10 TICKS { PUSH(ref rows, ARRAY([1])); outer(ref rows[0], ref rows[0]); }");
            IllegalArgumentException failure = assertThrows(IllegalArgumentException.class, () -> run.tick(10));
            assertTrue(
                failure.getMessage()
                    .contains("invalidated"),
                failure.getMessage());
            assertEquals(
                1,
                run.global(0)
                    .size());
            assertArray(
                run.global(0)
                    .get(0),
                1,
                2);
        }
    }

    @Test
    void nativeEffectsPropagateThroughWrappersAndCannotRunDuringInitializationOrSubscription() throws Exception {
        String integers = "integer wrapper() { RETURN leaf(); } "
            + "integer leaf() { LET values = ARRAY([1]); RETURN POP(ref values); } ";
        String labels = "label_ref wrapper() { RETURN leaf(); } "
            + "label_ref leaf() { LET values = ARRAY([LABEL(\"signal\")]); RETURN POP(ref values); } ";
        for (String source : new String[] { "LET values = ARRAY([1]) LET last = POP(ref values) EVERY 10 TICKS {}",
            "LET result = wrapper() " + integers + "EVERY 10 TICKS {}",
            "STATE result = wrapper() " + integers + "EVERY 10 TICKS {}",
            labels + "EVERY REDSTONE RISING FROM wrapper() DO END",
            "LET values = ARRAY([LABEL(\"signal\")]) EVERY REDSTONE RISING FROM POP(ref values) DO END" })
            assertFalse(
                new ProgramBuilder(source).build()
                    .isSuccess(),
                source);
        Run normal = new Run("STATE result = 0 " + integers + "EVERY 10 TICKS { result = wrapper(); }");
        for (CompiledFunction function : normal.program.getFunctions())
            assertTrue(function.mayMutateReferences(), function.getName());
        normal.tick(10);
        assertEquals(1, normal.number(0));
        Run pure = new Run("LET values = ARRAY([1,2]) EVERY 10 TICKS {}");
        pure.tick(10);
        assertArray(pure.global(0), 1, 2);
    }

    @Test
    void sleepingIndexedAssignmentChargesCapturedPrimitivePathForFixedAndDynamicArrays() throws Exception {
        for (String initializer : new String[] { "[0]", "ARRAY([0])" }) {
            Run run = new Run(
                "STATE started = 0 LET rows = " + initializer
                    + " integer later() { WAIT(2); RETURN 7; } "
                    + "EVERY 10 TICKS { started = started + 1; rows[0] = later(); }");
            run.tick(10);
            ProgramExecution execution = run.manager.getProgramExecution(run.program);
            assertEquals(1, execution.sleepingCount());
            assertArray(run.global(0), 0);
            assertEquals(1, run.number(0));

            // Inspect only the manager's sleeping entry and its runtime, not compiler-private path classes.
            Field sleepingField = ProgramExecution.class.getDeclaredField("sleeping");
            sleepingField.setAccessible(true);
            Map<?, ?> sleeping = (Map<?, ?>) sleepingField.get(execution);
            Object entry = sleeping.values()
                .iterator()
                .next();
            Field runtimeField = entry.getClass()
                .getDeclaredField("runtime");
            runtimeField.setAccessible(true);
            DataRuntime runtime = (DataRuntime) runtimeField.get(entry);
            Continuation.Bounds observed = new Continuation.Bounds();
            runtime.retain(observed);
            Field bytesField = Continuation.Bounds.class.getDeclaredField("bytes");
            bytesField.setAccessible(true);
            int retainedBytes = bytesField.getInt(observed);
            // Fixed paths keep the existing int[] cost; dynamic paths also retain their epoch wrapper.
            int pathBytes = initializer.startsWith("ARRAY") ? 52 : 20;
            assertTrue(retainedBytes >= pathBytes, "Sleeping assignment omitted its primitive path: " + retainedBytes);
            Continuation.Bounds exact = new Continuation.Bounds();
            exact.opaque(ProgramGlobals.MAX_OPAQUE_BYTES - retainedBytes);
            assertDoesNotThrow(() -> runtime.retain(exact));
            assertEquals(ProgramGlobals.MAX_OPAQUE_BYTES, bytesField.getInt(exact));
            Continuation.Bounds over = new Continuation.Bounds();
            over.opaque(ProgramGlobals.MAX_OPAQUE_BYTES - retainedBytes + 1);
            assertThrows(IllegalArgumentException.class, () -> runtime.retain(over));

            run.tick(12);
            assertArray(run.global(0), 7);
            assertEquals(1, run.number(0), "Resumption must not replay the pre-assignment statement");
            assertEquals(0, execution.sleepingCount());
            run.tick(13);
            assertArray(run.global(0), 7);
            assertEquals(1, run.number(0));
        }
    }

    @Test
    void extendInsertRemoveClearAndMembershipRunThroughCompilerAndSourceGeneration() throws Exception {
        Run run = new Run(
            "STATE removed = 0 STATE last = 0 STATE found = -2 STATE missing = -2 "
                + "STATE present = FALSE STATE absent = TRUE STATE finalLength = -1 "
                + "LET values = ARRAY([1,2]) LET beforeClear = ARRAY(\"integer\") "
                + "EVERY 10 TICKS { EXTEND(ref values, [3,4]); INSERT(ref values, 1, 9); "
                + "removed = REMOVEAT(ref values, 2); last = PEEK(values); found = INDEXOF(values, 9); "
                + "missing = INDEXOF(values, 99); present = CONTAINS(values, 3); absent = CONTAINS(values, 2); "
                + "beforeClear = values; CLEAR(ref values); finalLength = LEN(values); }");
        run.tick(10);
        assertEquals(2, run.number(0));
        assertEquals(4, run.number(1));
        assertEquals(1, run.number(2));
        assertEquals(-1, run.number(3));
        assertTrue(
            run.state.read(4)
                .asBoolean());
        assertFalse(
            run.state.read(5)
                .asBoolean());
        assertEquals(0, run.number(6));
        assertArray(run.global(0));
        assertArray(run.global(1), 1, 9, 3, 4);
        compile(run.program.toString());
    }

    @Test
    void additionalNativeMutatorsPropagateEffectsThroughParameterlessWrappers() {
        for (String mutation : new String[] { "EXTEND(ref values, [3]);", "INSERT(ref values, 1, 3);",
            "LET removed = REMOVEAT(ref values, 0);", "CLEAR(ref values);" }) {
            String functions = "integer wrapper() { RETURN leaf(); } integer leaf() { LET values = ARRAY([1,2]); "
                + mutation
                + " RETURN LEN(values); } ";
            Program ordinary = compile(functions + "EVERY 10 TICKS { LET result = wrapper(); }");
            for (CompiledFunction function : ordinary.getFunctions())
                assertTrue(function.mayMutateReferences(), mutation + function.getName());
            assertFalse(
                new ProgramBuilder("LET result = wrapper() " + functions + "EVERY 10 TICKS {}").build()
                    .isSuccess(),
                mutation);
            assertFalse(
                new ProgramBuilder("STATE result = wrapper() " + functions + "EVERY 10 TICKS {}").build()
                    .isSuccess(),
                mutation);
            String subscriptions = "label_ref wrapper() { RETURN leaf(); } "
                + "label_ref leaf() { LET values = ARRAY([1,2]); "
                + mutation
                + " RETURN LABEL(\"signal\"); } "
                + "EVERY REDSTONE RISING FROM wrapper() DO END";
            assertFalse(
                new ProgramBuilder(subscriptions).build()
                    .isSuccess(),
                mutation);
        }
    }

    private static Program compile(String source) {
        ProgramBuildResult built = new ProgramBuilder(source).build();
        assertTrue(built.isSuccess(), built.getErrorMessage());
        return built.getProgram();
    }

    private static void assertArray(DataValue array, long... expected) {
        assertEquals(expected.length, array.size());
        for (int i = 0; i < expected.length; i++) assertEquals(
            expected[i],
            array.get(i)
                .asLong(),
            "Element " + i);
    }

    private static final class Memory implements VersionedStateAccess {

        final DataValue[] values;
        final Object identity = new Object();
        final long[] generations;

        Memory(Program program) {
            values = new DataValue[program.getStateSchema()
                .size()];
            generations = new long[values.length];
            for (int i = 0; i < values.length; i++) values[i] = program.getStateSchema()
                .getDeclarations()
                .get(i)
                .getDefaultValue();
        }

        @Override
        public DataValue read(int slot) {
            return values[slot];
        }

        @Override
        public void write(int slot, DataValue value) {
            write(
                slot,
                value,
                value.getType()
                    .containsDynamicArray());
        }

        @Override
        public Object identity() {
            return identity;
        }

        @Override
        public long generation(int slot) {
            return generations[slot];
        }

        @Override
        public void write(int slot, DataValue value, boolean structural) {
            if (!values[slot].getType()
                .equals(value.getType())
                || !value.getType()
                    .isPersistable())
                throw new IllegalArgumentException("STATE type/persistence contract");
            long next = structural ? Math.addExact(generations[slot], 1) : generations[slot];
            values[slot] = value;
            generations[slot] = next;
        }
    }

    private static final class Run {

        final Fixture fixture = new Fixture();
        final TileEntityManager manager = RecoveryFixtures.manager(fixture);
        final Program program;
        final Memory state;

        Run(String source) throws Exception {
            program = compile(source);
            state = new Memory(program);
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
            context.setStateAccess(state);
            program.tick(context);
        }

        long number(int slot) {
            return state.read(slot)
                .asLong();
        }

        DataValue global(int slot) {
            return manager.getProgramGlobals(program)
                .read(slot);
        }
    }
}
