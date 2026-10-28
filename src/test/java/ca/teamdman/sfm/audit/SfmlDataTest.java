package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ProgramStateSchema;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class SfmlDataTest {

    private static Program program(String source) {
        ProgramBuildResult result = new ProgramBuilder(source).build();
        assertTrue(result.isSuccess(), result.getErrorMessage());
        return result.getProgram();
    }

    private static ProgramContext context(MemoryState state) {
        ProgramContext context = new ProgramContext(null, null, null, null, 20, false);
        context.setStateAccess(state);
        return context;
    }

    private static MemoryState run(String declarations, String body) {
        Program program = program(declarations + " EVERY 20 TICKS DO " + body + " END");
        MemoryState state = new MemoryState(program.getStateSchema());
        program.tick(context(state));
        return state;
    }

    @Test
    void arithmeticAndNestedValueUpdatesHaveValueSemantics() {
        MemoryState state = run(
            "STATE $result = { old: 0, updated: 0, arithmetic: 0 }",
            "LET $a = [{ amount: 4 }, { amount: 8 }] LET $b = $a " + "SET $b[0].amount = $a[1].amount * 3 + 2 "
                + "SET $result.old = $a[0].amount SET $result.updated = $b[0].amount "
                + "SET $result.arithmetic = -7 / 3 + -7 % 3");
        assertEquals(
            4,
            state.read(0)
                .get("old")
                .asLong());
        assertEquals(
            26,
            state.read(0)
                .get("updated")
                .asLong());
        assertEquals(
            -3,
            state.read(0)
                .get("arithmetic")
                .asLong());
    }

    @Test
    void localsResetAndStateIsSharedOnlyThroughTheExplicitBridge() {
        Program program = program(
            "STATE $count = 0 EVERY 20 TICKS DO LET $n = 1 SET $count = $count + $n SET $n = 9 END "
                + "EVERY 20 TICKS DO LET $n = 2 SET $count = $count + $n END");
        MemoryState first = new MemoryState(program.getStateSchema());
        MemoryState second = new MemoryState(program.getStateSchema());
        program.tick(context(first));
        program.tick(context(first));
        program.tick(context(second));
        assertEquals(
            6,
            first.read(0)
                .asLong());
        assertEquals(
            3,
            second.read(0)
                .asLong());
        assertThrows(IllegalStateException.class, () -> program.tick(context(null)));
    }

    @Test
    void compilationRejectsUnknownNamesShadowingAndChangedShapes() {
        String[] bodies = { "SET $unknown = 1", "LET $a = $a", "LET $a = 1 IF TRUE THEN LET $a = 2 END",
            "IF TRUE THEN LET $a = 1 END SET $a = 2", "LET $a = [1, TRUE]", "LET $a = []",
            "LET $a = [1, 2] SET $a = [1]", "LET $a = { x: 1, x: 2 }", "LET $a = { x: 1 } SET $a.y = 3",
            "LET $a = 1 IF ($a) THEN END", "LET $a = 2*3", "LET $a = 9223372036854775808",
            "LET $a = [1] IF ($a = $a) THEN END" };
        for (String body : bodies) assertFalse(
            new ProgramBuilder("EVERY 20 TICKS DO " + body + " END").build()
                .isSuccess(),
            body);
        assertFalse(
            new ProgramBuilder("STATE $a = 1 STATE $b = $a EVERY 20 TICKS DO END").build()
                .isSuccess());
        assertFalse(
            new ProgramBuilder("STATE $a = 1 / 0 EVERY 20 TICKS DO END").build()
                .isSuccess());
        assertFalse(
            new ProgramBuilder("STATE $a = 1 EVERY 20 TICKS DO LET $a = 2 END").build()
                .isSuccess());
    }

    @Test
    void runtimeFaultsPreserveEarlierStateButDoNotPublishFailedAssignments() {
        Program program = program(
            "STATE $a = 0 STATE $b = [1, 2] EVERY 20 TICKS DO SET $a = 7 SET $b[2] = 99 SET $a = 9 END "
                + "EVERY 20 TICKS DO SET $a = 20 END");
        MemoryState state = new MemoryState(program.getStateSchema());
        assertThrows(IllegalArgumentException.class, () -> program.tick(context(state)));
        assertEquals(
            7,
            state.read(0)
                .asLong());
        assertEquals(
            1,
            state.read(1)
                .get(0)
                .asLong());
        Program overflow = program("STATE $a = 0 EVERY 20 TICKS DO SET $a = 9223372036854775807 + 1 END");
        MemoryState untouched = new MemoryState(overflow.getStateSchema());
        assertThrows(IllegalArgumentException.class, () -> overflow.tick(context(untouched)));
        assertEquals(
            0,
            untouched.read(0)
                .asLong());
    }

    @Test
    void shortCircuitPreservesFaultTimingAndSignedMinimum() {
        MemoryState state = run(
            "STATE $n = 0",
            "IF (FALSE AND 1 / 0 > 0) THEN SET $n = 99 END "
                + "IF (TRUE OR 1 / 0 > 0) THEN SET $n = -9223372036854775808 END");
        assertEquals(
            Long.MIN_VALUE,
            state.read(0)
                .asLong());
        assertThrows(IllegalArgumentException.class, () -> run("STATE $n = 0", "SET $n = -9223372036854775808 / -1"));
        assertEquals(
            0,
            run("STATE $n = 5", "SET $n = -9223372036854775808 % -1").read(0)
                .asLong());
    }

    @Test
    void dynamicInputSnapshotsBeforeLaterAssignmentsAndKeepsQuotaAcrossOutputs() throws Exception {
        Program program = program(
            "EVERY 20 TICKS DO LET $n = 8 INPUT ($n) FROM source SET $n = 32 "
                + "OUTPUT 3 TO dest OUTPUT 64 TO dest END");
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        fixture.stock(0, 32);
        program.tick(
            new ProgramContext(
                fixture.world,
                null,
                new CableNetwork(fixture.world, AuditRegressionTest.MANAGER),
                fixture.labels,
                20,
                false));
        assertEquals(24, AuditRegressionTest.count(fixture.source));
        assertEquals(8, AuditRegressionTest.count(fixture.dest));
    }

    @Test
    void everyOutputLimitIsValidatedBeforeAnyTransferAndPriorOutputRemains() throws Exception {
        Program program = program(
            "EVERY 20 TICKS DO LET $bad = -1 INPUT FROM source OUTPUT 2 TO dest " + "OUTPUT 4, ($bad) TO dest END");
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        fixture.stock(0, 32);
        ProgramContext context = new ProgramContext(
            fixture.world,
            null,
            new CableNetwork(fixture.world, AuditRegressionTest.MANAGER),
            fixture.labels,
            20,
            false);
        assertThrows(IllegalArgumentException.class, () -> program.tick(context));
        assertEquals(30, AuditRegressionTest.count(fixture.source));
        assertEquals(2, AuditRegressionTest.count(fixture.dest));
    }

    @Test
    void dynamicRetentionAndHasCountsUseTheCurrentScalarSnapshot() throws Exception {
        Program program = program(
            "EVERY 20 TICKS DO LET $reserve = 12 IF source HAS >= ($reserve) THEN "
                + "INPUT 100 RETAIN ($reserve) FROM source OUTPUT 100 TO dest END END");
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        fixture.stock(0, 32);
        program.tick(
            new ProgramContext(
                fixture.world,
                null,
                new CableNetwork(fixture.world, AuditRegressionTest.MANAGER),
                fixture.labels,
                20,
                false));
        assertEquals(12, AuditRegressionTest.count(fixture.source));
        assertEquals(20, AuditRegressionTest.count(fixture.dest));
    }

    @Test
    void oldKeywordsAndWildcardsKeepTheirLegacyParseAndNoFrame() {
        Program legacy = program("EVERY 20g+1 TICKS DO INPUT a*b FROM LET OUTPUT *3 TO STATE FORGET SET END");
        assertFalse(legacy.hasDataFeatures());
        Program plain = program("EVERY 20 TICKS DO IF (TRUE AND FALSE) THEN END END");
        assertFalse(plain.hasDataFeatures());
        ProgramContext context = context(null);
        plain.tick(context);
        assertThrows(IllegalStateException.class, context::getDataRuntime);
    }

    @Test
    void dataSourceCanonicalRoundTripPreservesMeaning() {
        Program original = program(
            "NAME \"Data\" STATE $counter = 3 EVERY 20 TICKS DO LET $opts = {b: TRUE, a: [2, 3]} "
                + "IF ($opts.b) THEN SET $counter = $counter + $opts.a[1] END END");
        Program rebuilt = program(original.toString());
        MemoryState state = new MemoryState(rebuilt.getStateSchema());
        rebuilt.tick(context(state));
        assertEquals(
            6,
            state.read(0)
                .asLong());
    }

    @Test
    void carriageReturnCommentsAgreeWithTheSfmlLexer() {
        MemoryState state = run("STATE $out = 0", "LET $a = 1 -- comment\r + 2 SET $out = $a");
        assertEquals(
            3,
            state.read(0)
                .asLong());
    }

    @Test
    void expressionAndAllocationBudgetsFailBeforePublication() {
        DataCompiler compiler = new DataCompiler();
        compiler.beginTrigger();
        compiler.pushScope();
        DataCompiler.Binding binding = compiler.declareLocal("a", DataType.INTEGER);
        DataRuntime runtime = new DataRuntime(null, 1);
        binding.write(runtime, DataValue.integer(1));
        DataExpression rhs = compiler.compile("2", 1, 0);
        runtime.work(16384 - runtime.getWork());
        assertThrows(
            IllegalArgumentException.class,
            () -> compiler.assignment("$a", rhs, 1, 0)
                .execute(runtime));
        List<DataValue> large = new ArrayList<>();
        for (int i = 0; i < 257; i++) large.add(DataValue.integer(i));
        assertThrows(IllegalArgumentException.class, () -> DataValue.array(large));
        DataType tooLarge = DataType.array(DataType.INTEGER, 256);
        assertThrows(IllegalArgumentException.class, () -> DataType.array(tooLarge, 256));
        assertThrows(IllegalArgumentException.class, () -> runtime.allocate(16385));
    }

    @Test
    void wordOperatorNamesRemainValidStructFields() {
        MemoryState state = run(
            "STATE $result = 0",
            "LET $a = { plus: 2, input: 3, state: 4 } SET $result = $a.plus + $a.input + $a.state");
        assertEquals(
            9,
            state.read(0)
                .asLong());
    }

    @Test
    void dynamicRedstoneOutputsAreRejectedInEveryOptionalQuantityPosition() {
        String[] outputs = { "OUTPUT ($n) REDSTONE LEVEL TO dest", "OUTPUT REDSTONE LEVEL ($n) TO dest",
            "OUTPUT 4 REDSTONE LEVEL ($n) TO dest", "TO dest OUTPUT 4 REDSTONE LEVEL ($n)" };
        for (String output : outputs) {
            ProgramBuildResult result = new ProgramBuilder("EVERY 20 TICKS DO LET $n = 3 " + output + " END").build();
            assertFalse(result.isSuccess(), output);
            assertTrue(
                result.getErrorMessage()
                    .contains("Dynamic redstone OUTPUT"),
                result.getErrorMessage());
        }
        Program literal = program("EVERY 20 TICKS DO OUTPUT 4 REDSTONE LEVEL 8 TO dest END");
        assertEquals(
            4,
            ((ca.teamdman.sfml.ast.RedstoneOutputStatement) literal.getTriggers()
                .get(0)
                .getBlock()
                .getStatements()
                .get(0)).getLevel());
    }

    @Test
    void dynamicRedstoneComparisonsUseCurrentThresholdAndShortCircuit() {
        MemoryState state = run(
            "STATE $result = 0",
            "LET $threshold = 9223372036854775807 "
                + "IF REDSTONE < ($threshold) THEN SET $result = 1 END SET $threshold = 0 "
                + "IF REDSTONE = ($threshold) THEN SET $result = $result + 1 END "
                + "IF FALSE AND REDSTONE > (-1) THEN SET $result = 99 END");
        assertEquals(
            2,
            state.read(0)
                .asLong());
        assertThrows(IllegalArgumentException.class, () -> run("STATE $result = 0", "IF REDSTONE > (-1) THEN END"));
    }

    @Test
    void dynamicQuantityCanonicalRoundTripPreservesZeroMaximumRetentionAndEach() throws Exception {
        String[] bodies = { "LET $n = 8 INPUT FROM source OUTPUT ($n) RETAIN 0 TO dest",
            "LET $n = 8 INPUT ($n) EACH RETAIN 9223372036854775807 EACH FROM source OUTPUT ($n) TO dest" };
        for (String body : bodies) {
            Program original = program("EVERY 20 TICKS DO " + body + " END");
            Program rebuilt = program(original.toString());
            AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
            fixture.stock(0, 32);
            rebuilt.tick(
                new ProgramContext(
                    fixture.world,
                    null,
                    new CableNetwork(fixture.world, AuditRegressionTest.MANAGER),
                    fixture.labels,
                    20,
                    false));
            assertEquals(32, AuditRegressionTest.count(fixture.source));
            assertEquals(0, AuditRegressionTest.count(fixture.dest));
        }
        Program each = program("EVERY 20 TICKS DO LET $n = 8 INPUT ($n) EACH RETAIN ($n) EACH FROM EACH source END");
        Program rebuilt = program(each.toString());
        ca.teamdman.sfml.ast.InputStatement input = (ca.teamdman.sfml.ast.InputStatement) rebuilt.getTriggers()
            .get(0)
            .getBlock()
            .getStatements()
            .get(1);
        assertTrue(input.isEach());
        assertTrue(
            input.getResourceLimits()
                .getLimits()
                .get(0)
                .getLimit()
                .getQuantity()
                .isEach());
        assertTrue(
            input.getResourceLimits()
                .getLimits()
                .get(0)
                .getLimit()
                .getRetention()
                .isEach());
    }

    private static final class MemoryState implements StateAccess {

        private final DataValue[] values;

        MemoryState(ProgramStateSchema schema) {
            values = new DataValue[schema.size()];
            for (int i = 0; i < values.length; i++) values[i] = schema.getDeclarations()
                .get(i)
                .getDefaultValue();
        }

        @Override
        public DataValue read(int slot) {
            return values[slot];
        }

        @Override
        public void write(int slot, DataValue value) {
            assertEquals(values[slot].getType(), value.getType());
            values[slot] = value;
        }
    }
}
