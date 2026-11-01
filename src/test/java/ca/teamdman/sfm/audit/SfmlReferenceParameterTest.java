package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataReference;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ProgramGlobals;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class SfmlReferenceParameterTest {

    @Test
    void explicitRefUpdatesCallerButOrdinaryParametersRemainCopies() throws Exception {
        Run run = new Run(
            "STATE answer = 0 " + "void copy(integer value) { value = value + 100; } "
                + "void increment(ref integer value) { value = value + 1; } "
                + "EVERY 10 TICKS { LET count = 4; copy(count); increment(ref count); answer = count; }");
        run.tick(10);
        assertEquals(
            5,
            run.value(0)
                .asLong());
        parse(run.program.toString());
    }

    @Test
    void legacySignatureSpellingAndExplicitDollarLvaluesRemainSupported() throws Exception {
        Run run = new Run(
            "STATE $answer = 1 " + "FUNCTION increment(ref $value: INTEGER) RETURNS VOID DO $value = $value + 2 END "
                + "EVERY 10 TICKS DO increment(ref $answer) END");
        run.tick(10);
        assertEquals(
            3,
            run.value(0)
                .asLong());
    }

    @Test
    void aliasesObserveEachOtherImmediatelyRatherThanCopyingBackOnReturn() throws Exception {
        Run run = new Run(
            "STATE answer = 0 "
                + "void combine(ref integer first, ref integer secondValue) { first = first + 1; secondValue = secondValue + first; } "
                + "EVERY 10 TICKS { LET count = 2; combine(ref count, ref count); answer = count; }");
        run.tick(10);
        assertEquals(
            6,
            run.value(0)
                .asLong());
    }

    @Test
    void arraysAndStructFieldsWriteThroughExactPathsAndPreserveSiblings() throws Exception {
        Run run = new Run(
            "STATE data = {rows:[1,2], untouched:7} " + "void replace(ref integer value) { value = 9; } "
                + "EVERY 10 TICKS { replace(ref data.rows[1]); }");
        run.tick(10);
        assertEquals(
            1,
            run.value(0)
                .get("rows")
                .get(0)
                .asLong());
        assertEquals(
            9,
            run.value(0)
                .get("rows")
                .get(1)
                .asLong());
        assertEquals(
            7,
            run.value(0)
                .get("untouched")
                .asLong());
    }

    @Test
    void arrayIndexIsFrozenBeforeCalleeChangesItsAlias() throws Exception {
        Run run = new Run(
            "STATE values = [1,2] STATE index = 0 "
                + "void choose(ref integer cell, ref integer cursor) { cursor = 1; cell = 9; } "
                + "EVERY 10 TICKS { choose(ref values[index], ref index); }");
        run.tick(10);
        assertEquals(
            9,
            run.value(0)
                .get(0)
                .asLong());
        assertEquals(
            2,
            run.value(0)
                .get(1)
                .asLong());
        assertEquals(
            1,
            run.value(1)
                .asLong());
    }

    @Test
    void refCapturedBeforeLaterWaitingArgumentKeepsItsOriginalIndex() throws Exception {
        Run run = new Run(
            "STATE values = [1,2] STATE index = 0 " + "integer later() { WAIT(2); RETURN 7; } "
                + "void apply(ref integer cell, integer value) { cell = value; } "
                + "EVERY 10 TICKS { apply(ref values[index], later()); } "
                + "EVERY 1 TICKS { index = 1; }");
        run.tick(10);
        assertEquals(
            1,
            run.value(0)
                .get(0)
                .asLong());
        assertEquals(
            1,
            run.value(1)
                .asLong());
        run.tick(12);
        assertEquals(
            7,
            run.value(0)
                .get(0)
                .asLong());
        assertEquals(
            2,
            run.value(0)
                .get(1)
                .asLong());
    }

    @Test
    void nestedRefsToCallerLocalSurviveWaitWithoutRetainingLiveServices() throws Exception {
        Run run = new Run(
            "STATE answer = 0 " + "void inner(ref integer value) { WAIT(2); value = value + 3; } "
                + "void outer(ref integer value) { inner(ref value); value = value + 4; } "
                + "EVERY 10 TICKS { LET local = 1; outer(ref local); answer = local; }");
        run.tick(10);
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        int[] counts = { 0, 0 };
        detached(run.manager.getProgramExecution(run.program), visited, counts);
        assertEquals(3, counts[0], "Sleeping trigger and both function frames must detach");
        assertEquals(1, counts[1], "Forwarded whole-variable references share their original local address");
        run.tick(12);
        assertEquals(
            8,
            run.value(0)
                .asLong());
    }

    @Test
    void stateReferenceResumesAgainstCurrentRootWithoutClobberingOtherTriggerWrites() throws Exception {
        Run run = new Run(
            "STATE data = {first:1, second:2} "
                + "void edit(ref integer value) { value = value + 1; WAIT(2); value = value + 10; } "
                + "EVERY 10 TICKS { edit(ref data.first); } "
                + "EVERY 1 TICKS { data.second = 8; }");
        run.tick(10);
        assertEquals(
            2,
            run.value(0)
                .get("first")
                .asLong());
        run.tick(12);
        assertEquals(
            12,
            run.value(0)
                .get("first")
                .asLong());
        assertEquals(
            8,
            run.value(0)
                .get("second")
                .asLong());
    }

    @Test
    void globalReferenceRebindsAcrossWaitAndDoesNotMakeGlobalsImplicitFunctionScope() throws Exception {
        Run run = new Run(
            "STATE answer = 0 LET shared = 4 " + "void edit(ref integer value) { WAIT(2); value = value + 3; } "
                + "EVERY 10 TICKS { edit(ref shared); answer = shared; }");
        run.tick(10);
        run.tick(12);
        assertEquals(
            7,
            run.value(0)
                .asLong());
        assertFalse(
            new ProgramBuilder("LET shared = 4 void edit() { shared = 7; } EVERY 10 TICKS { edit(); }").build()
                .isSuccess());
    }

    @Test
    void reloadAndUnloadCancelReferencesTogetherWithTheirSleepingCallFrames() throws Exception {
        for (boolean unload : new boolean[] { false, true }) {
            Run run = new Run(
                "STATE answer = 0 void edit(ref integer value) { WAIT(2); value = 9; } "
                    + "EVERY 10 TICKS { edit(ref answer); }");
            run.tick(10);
            assertEquals(
                1,
                run.manager.getProgramExecution(run.program)
                    .sleepingCount());
            if (unload) run.manager.onChunkUnload();
            else run.manager.setInventorySlotContents(
                0,
                run.manager.getDisk()
                    .copy());
            run.tick(12);
            assertEquals(
                0,
                run.value(0)
                    .asLong());
        }
    }

    @Test
    void refsAreExplicitWritableExactTypedLvalues() {
        String prefix = "void edit(ref integer value) { value = 9; } ";
        for (String body : new String[] { "LET number = 1; edit(number);", "edit(ref 1);",
            "LET number = true; edit(ref number);", "LET number = 1; edit(ref number + 1);", "edit(ref absent);",
            "FOR number = 0 TO 2 DO edit(ref number); END", "LET rows = [1,2]; edit(ref rows);" })
            assertFalse(
                new ProgramBuilder(prefix + "EVERY 10 TICKS { " + body + " }").build()
                    .isSuccess(),
                body);
        for (String source : new String[] {
            "void edit(integer value) {} EVERY 10 TICKS { LET value = 1; edit(ref value); }",
            "void edit(ref void value) {} EVERY 10 TICKS {}",
            "void edit(ref integer value) {} integer bad() { LET value=1; edit(ref value); RETURN value; } EVERY 10 TICKS {}" })
            assertFalse(
                new ProgramBuilder(source).build()
                    .isSuccess(),
                source);
    }

    @Test
    void valueFunctionsReturnAfterMutatingExplicitStateAliasesAndNestedFields() throws Exception {
        Run run = new Run(
            "STATE data = {rows:[1,2], untouched:7} STATE answer = 0 "
                + "integer combine(ref integer first, ref integer other) { "
                + "first = first + 1; other = other + first; RETURN first + other; } "
                + "EVERY 10 TICKS { answer = combine(ref data.rows[1], ref data.rows[1]); }");
        run.tick(10);
        assertEquals(
            12,
            run.value(1)
                .asLong());
        assertEquals(
            6,
            run.value(0)
                .get("rows")
                .get(1)
                .asLong());
        assertEquals(
            1,
            run.value(0)
                .get("rows")
                .get(0)
                .asLong());
        assertEquals(
            7,
            run.value(0)
                .get("untouched")
                .asLong());
        parse(run.program.toString());
    }

    @Test
    void fixedLabelArrayRefCanBeChangedAndReturnedAsALabel() throws Exception {
        Run run = new Run(
            "LET labels = [label(\"a\"),label(\"b\"),label(\"c\"),label(\"d\"),"
                + "label(\"e\"),label(\"f\"),label(\"g\"),label(\"h\")] LET chosen = label(\"initial\") "
                + "label_ref assign(ref label_ref[8] targets) { targets[0] = label(\"source\"); RETURN targets[0]; } "
                + "EVERY 10 TICKS { chosen = assign(ref labels); }");
        run.tick(10);
        ProgramGlobals globals = run.manager.getProgramGlobals(run.program);
        assertEquals(
            "source",
            globals.read(0)
                .get(0)
                .asLabel()
                .getName());
        assertEquals(
            "h",
            globals.read(0)
                .get(7)
                .asLabel()
                .getName());
        assertEquals(
            "source",
            globals.read(1)
                .asLabel()
                .getName());
        assertFalse(
            new ProgramBuilder(
                "label_ref assign(ref label_ref[8] targets) { RETURN targets[0]; } "
                    + "EVERY 10 TICKS { LET labels = [label(\"one\")]; LET chosen = assign(ref labels); }").build()
                        .isSuccess());
    }

    @Test
    void valueRefCallsReturnExactlyOnceAcrossWaitAndForwardExplicitReferences() throws Exception {
        Run run = new Run(
            "STATE count = 1 STATE answer = 0 " + "integer outer(ref integer cell) { RETURN inner(ref cell); } "
                + "integer inner(ref integer value) { value = value + 1; WAIT(2); value = value + 10; RETURN value; } "
                + "EVERY 10 TICKS { answer = count + outer(ref count); }");
        run.tick(10);
        assertEquals(
            2,
            run.value(0)
                .asLong());
        assertEquals(
            0,
            run.value(1)
                .asLong());
        run.tick(11);
        assertEquals(
            2,
            run.value(0)
                .asLong());
        run.tick(12);
        assertEquals(
            12,
            run.value(0)
                .asLong());
        assertEquals(
            13,
            run.value(1)
                .asLong(),
            "The operand read before WAIT must not replay");
        run.tick(13);
        assertEquals(
            12,
            run.value(0)
                .asLong());
    }

    @Test
    void compoundAssignmentPublishesIntoCurrentRootAfterRefRhsMutatesASibling() throws Exception {
        for (String wait : new String[] { "", "WAIT(1);" }) {
            Run run = new Run(
                "STATE pair = {left:1, right:2} " + "integer bump(ref integer cell) { cell = cell + 1; "
                    + wait
                    + " RETURN 9; } "
                    + "EVERY 10 TICKS { pair.left = bump(ref pair.right); }");
            run.tick(10);
            if (!wait.isEmpty()) run.tick(11);
            assertEquals(
                9,
                run.value(0)
                    .get("left")
                    .asLong());
            assertEquals(
                3,
                run.value(0)
                    .get("right")
                    .asLong());
        }
    }

    @Test
    void assignmentIndexRefMutationAndRhsRefMutationPreserveCurrentRootAndFrozenIndex() throws Exception {
        Run run = new Run(
            "STATE rows = [1,2] STATE answer = 0 " + "integer choose(ref integer cell) { cell = 7; RETURN 0; } "
                + "integer replace(ref integer cell) { cell = cell + 1; RETURN 9; } "
                + "EVERY 10 TICKS { rows[choose(ref rows[1])] = replace(ref rows[1]); answer = rows[1]; }");
        run.tick(10);
        assertEquals(
            9,
            run.value(0)
                .get(0)
                .asLong());
        assertEquals(
            8,
            run.value(0)
                .get(1)
                .asLong());
        assertEquals(
            8,
            run.value(1)
                .asLong());
    }

    @Test
    void valueFunctionsCannotObtainImplicitStateReferencesOrNativeActionAuthority() {
        String increment = "integer increment(ref integer cell) { cell = cell + 1; RETURN cell; } ";
        for (String body : new String[] { "integer bad() { RETURN increment(ref count); }",
            "integer bad() { RETURN increment(ref nested.values[0]); }",
            "integer bad(ref integer cell) { count = 1; RETURN cell; }",
            "integer bad(ref integer cell) { INPUT FROM \"source\"; RETURN cell; }",
            "integer bad(ref integer cell) { transfer(label(\"source\"),label(\"dest\")); RETURN cell; }" }) {
            ProgramBuildResult built = new ProgramBuilder(
                "STATE count = 0 STATE nested = {values:[1,2]} " + increment + body + " EVERY 10 TICKS {}").build();
            assertFalse(built.isSuccess(), body);
        }
    }

    @Test
    void refEffectsAreRejectedInGlobalInitializersAndNamedTriggerSourcesIncludingForwardWrappers() {
        String functions = "label_ref wrapper() { LET local = label(\"old\"); RETURN assign(ref local); } "
            + "label_ref assign(ref label_ref target) { target = label(\"signal\"); RETURN target; } ";
        for (String source : new String[] {
            "LET target = label(\"old\") LET result = assign(ref target) " + functions + "EVERY 10 TICKS {}",
            "LET result = wrapper() " + functions + "EVERY 10 TICKS {}",
            functions + "EVERY REDSTONE RISING FROM wrapper() DO END",
            "LET target = label(\"old\") " + functions + "EVERY REDSTONE RISING FROM assign(ref target) DO END" }) {
            ProgramBuildResult built = new ProgramBuilder(source).build();
            assertFalse(built.isSuccess(), source);
            assertTrue(
                built.getErrorMessage()
                    .contains("REF effects"),
                built.getErrorMessage());
        }
        assertFalse(
            new ProgramBuilder(
                "STATE count = 0 STATE bad = increment(ref count) "
                    + "integer increment(ref integer cell) { cell = cell + 1; RETURN cell; } EVERY 10 TICKS {}").build()
                        .isSuccess());
        ProgramBuildResult pure = new ProgramBuilder(
            "LET result = pure() label_ref pure() { RETURN label(\"signal\"); } "
                + "EVERY REDSTONE RISING FROM pure() DO END").build();
        assertTrue(pure.isSuccess(), pure.getErrorMessage());
    }

    @Test
    void disabledPrintSkipsValueRefEffectsButEnabledPrintEvaluatesThemOnce() throws Exception {
        for (boolean enabled : new boolean[] { false, true }) {
            Run run = new Run(
                "STATE count = 0 integer increment(ref integer cell) { cell = cell + 1; RETURN cell; } "
                    + "EVERY 10 TICKS { PRINT(increment(ref count)); }");
            run.manager.getDebugConsole()
                .setEnabled(enabled);
            run.tick(10);
            assertEquals(
                enabled ? 1 : 0,
                run.value(0)
                    .asLong());
        }
    }

    @Test
    void invalidPathFailsBeforeTheProcedureCanMutateAnotherReference() throws Exception {
        Run run = new Run(
            "STATE values = [1,2] STATE answer = 0 "
                + "void edit(ref integer value, ref integer other) { other = 9; value = 8; } "
                + "EVERY 10 TICKS { edit(ref values[3], ref answer); }");
        assertThrows(IllegalArgumentException.class, () -> run.tick(10));
        assertEquals(
            0,
            run.value(1)
                .asLong());
        assertEquals(
            1,
            run.value(0)
                .get(0)
                .asLong());
    }

    @Test
    void refsNeverBecomeValuesOrRelaxStatePersistenceAndShapeConstraints() {
        DataCompiler compiler = new DataCompiler();
        compiler.beginTrigger();
        compiler.pushScope();
        DataCompiler.Binding array = compiler.declareLocal("array", DataType.array(DataType.INTEGER, 2));
        DataRuntime runtime = new DataRuntime(null, 1);
        array.write(runtime, DataValue.array(java.util.Arrays.asList(DataValue.integer(1), DataValue.integer(2))));
        int[] path = { 0 };
        DataReference ref = runtime.reference(array, path);
        path[0] = 1;
        ref.write(runtime, DataValue.integer(7));
        assertEquals(
            7,
            runtime.read(false, 0)
                .get(0)
                .asLong());
        assertEquals(
            2,
            runtime.read(false, 0)
                .get(1)
                .asLong());
        assertThrows(IllegalArgumentException.class, () -> ref.write(runtime, DataValue.bool(true)));
        assertThrows(IllegalArgumentException.class, () -> runtime.reference(array, new int[] { 2 }));
        assertThrows(IllegalArgumentException.class, () -> compiler.compile("ref array", 1, 0));
        assertFalse(
            new ProgramBuilder("STATE value = label(\"source\") EVERY 10 TICKS {}").build()
                .isSuccess());
    }

    @Test
    void procedureLoopsRetainInputRegistrationBudgetsAndIndependentQuotaIdentities() throws Exception {
        Run run = new Run(
            "void pump() { FOR n = 1 TO 257 DO INPUT FROM \"source\"; FORGET; END } " + "EVERY 10 TICKS { pump(); }");
        assertThrows(IllegalArgumentException.class, () -> run.tick(10));
        assertEquals(0, count(run.fixture.dest));
    }

    @Test
    void everyProcedureLoopInputKeepsItsOwnTransferAllowance() throws Exception {
        Run run = new Run(
            "void gather() { FOR n = 1 TO 2 DO INPUT 1 FROM \"source\"; END } "
                + "EVERY 10 TICKS { gather(); OUTPUT TO \"dest\"; }");
        run.fixture.stock(0, 7);
        run.tick(10);
        assertEquals(2, count(run.fixture.dest));
        assertEquals(5, count(run.fixture.source));
    }

    @Test
    void repeatedProcedureCallsKeepIndependentInputTransferAllowancesWithoutALoop() throws Exception {
        Run run = new Run(
            "void gather() { INPUT 1 FROM \"source\"; } "
                + "EVERY 10 TICKS { gather(); gather(); OUTPUT TO \"dest\"; }");
        run.fixture.stock(0, 7);
        run.tick(10);
        assertEquals(2, count(run.fixture.dest));
        assertEquals(5, count(run.fixture.source));
    }

    @Test
    void repeatedProcedureCallsShareTheInputRegistrationCapWithoutALoop() throws Exception {
        StringBuilder source = new StringBuilder(
            "void gather() { INPUT FROM \"source\"; INPUT FROM \"source\"; } " + "EVERY 10 TICKS { ");
        for (int i = 0; i < 129; i++) source.append("gather(); ");
        Run run = new Run(
            source.append('}')
                .toString());
        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class, () -> run.tick(10));
        assertTrue(
            failure.getMessage()
                .contains("registration"),
            failure.getMessage());
        assertEquals(0, count(run.fixture.dest));
    }

    private static void detached(Object value, Set<Object> visited, int[] counts) throws Exception {
        if (value == null || !visited.add(value)) return;
        if (value instanceof Map<?, ?>) {
            for (Object child : ((Map<?, ?>) value).values()) detached(child, visited, counts);
            return;
        }
        if (value instanceof Object[]) {
            for (Object child : (Object[]) value) detached(child, visited, counts);
            return;
        }
        String name = value.getClass()
            .getName();
        if (!(value instanceof DataRuntime) && !(value instanceof DataReference)
            && !name.startsWith("ca.teamdman.sfml.data.ProgramExecution")
            && !name.startsWith("ca.teamdman.sfml.data.Continuation")) return;
        if (value instanceof DataRuntime) counts[0]++;
        if (value instanceof DataReference) counts[1]++;
        for (Field field : value.getClass()
            .getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || field.getType()
                .isPrimitive()) continue;
            field.setAccessible(true);
            Object child = field.get(value);
            if (value instanceof DataRuntime && (field.getName()
                .equals("state")
                || field.getName()
                    .equals("queries")
                || field.getName()
                    .equals("globals")
                || field.getName()
                    .equals("budget")))
                assertNull(child, "Sleeping reference frame retained live " + field.getName());
            assertFalse(
                value instanceof DataReference && (child instanceof DataRuntime || child instanceof StateAccess
                    || child instanceof ProgramContext
                    || child instanceof ProgramGlobals),
                "References must retain addresses or value cells only");
            detached(child, visited, counts);
        }
    }

    private static final class Memory implements StateAccess {

        final DataValue[] values;
        final DataType[] types;

        Memory(Program program) {
            values = new DataValue[program.getStateSchema()
                .size()];
            types = new DataType[values.length];
            for (int i = 0; i < values.length; i++) {
                values[i] = program.getStateSchema()
                    .getDeclarations()
                    .get(i)
                    .getDefaultValue();
                types[i] = values[i].getType();
            }
        }

        public DataValue read(int slot) {
            return values[slot];
        }

        public void write(int slot, DataValue value) {
            if (!types[slot].equals(value.getType()) || !value.getType()
                .isPersistable()) throw new IllegalArgumentException("STATE type/persistence contract");
            values[slot] = value;
        }
    }

    private static final class Run {

        final Fixture fixture = new Fixture();
        final TileEntityManager manager = RecoveryFixtures.manager(fixture);
        final Program program;
        final Memory state;

        Run(String source) throws Exception {
            ProgramBuildResult built = new ProgramBuilder(source).build();
            assertTrue(built.isSuccess(), built.getErrorMessage());
            program = built.getProgram();
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

        DataValue value(int slot) {
            return state.read(slot);
        }
    }
}
