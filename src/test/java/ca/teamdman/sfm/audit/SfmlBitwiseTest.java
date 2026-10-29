package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataQueries;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.ProgramStateSchema;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class SfmlBitwiseTest {

    @Test
    void binaryOperatorsAgreeForStateDefaultsAndDynamicLocalsAtSignedBoundaries() {
        Object[][] cases = { { 6L, "&", 3L, 2L }, { 6L, "|", 3L, 7L }, { 6L, "^", 3L, 5L },
            { -1L, "&", Long.MIN_VALUE, Long.MIN_VALUE }, { Long.MIN_VALUE, "|", Long.MAX_VALUE, -1L },
            { -1L, "^", Long.MIN_VALUE, Long.MAX_VALUE }, { Long.MIN_VALUE, "^", Long.MIN_VALUE, 0L },
            { 1L, "<<", 0L, 1L }, { 1L, "<<", 63L, Long.MIN_VALUE }, { Long.MAX_VALUE, "<<", 1L, -2L },
            { Long.MIN_VALUE, "<<", 1L, 0L }, { -1L, "<<", 63L, Long.MIN_VALUE },
            { Long.MIN_VALUE, ">>", 0L, Long.MIN_VALUE }, { Long.MIN_VALUE, ">>", 1L, -4611686018427387904L },
            { Long.MIN_VALUE, ">>", 63L, -1L }, { Long.MAX_VALUE, ">>", 63L, 0L }, { -1L, ">>>", 0L, -1L },
            { -1L, ">>>", 1L, Long.MAX_VALUE }, { Long.MIN_VALUE, ">>>", 63L, 1L },
            { Long.MAX_VALUE, ">>>", 63L, 0L } };
        for (Object[] test : cases) {
            String left = test[0].toString(), operator = (String) test[1], right = test[2].toString();
            String expression = left + " " + operator + " " + right;
            long expected = (Long) test[3];
            assertEquals(
                expected,
                run("STATE $result = " + expression, "").read(0)
                    .asLong(),
                expression);
            assertEquals(
                expected,
                run(
                    "STATE $result = 0",
                    "LET $left = " + left + " LET $right = " + right + " SET $result = $left " + operator + " $right")
                        .read(0)
                        .asLong(),
                "dynamic " + expression);
        }
    }

    @Test
    void complementUsesAllSixtyFourBitsAndComposesWithUnarySigns() {
        long[][] cases = { { 0, -1 }, { -1, 0 }, { Long.MIN_VALUE, Long.MAX_VALUE },
            { Long.MAX_VALUE, Long.MIN_VALUE } };
        for (long[] test : cases) {
            assertEquals(
                test[1],
                run("STATE $result = ~" + test[0], "").read(0)
                    .asLong());
            assertEquals(
                test[1],
                run("STATE $result = 0", "LET $n = " + test[0] + " SET $result = ~$n").read(0)
                    .asLong());
        }
        assertEquals(
            1,
            run("STATE $result = ~-+2", "").read(0)
                .asLong());
        assertEquals(
            3,
            run("STATE $result = -~2", "").read(0)
                .asLong());
    }

    @Test
    void precedenceAndAssociativityComposeWithArithmeticComparisonsAndBooleanOperators() {
        Object[][] cases = { { "~1 + 2", 0L }, { "1 + 2 * 3 << 1 + 1", 28L }, { "64 >> 2 >> 1", 8L },
            { "1 << 2 << 1", 8L }, { "4 | 3 ^ 1 & 3 << 1 + 1", 7L }, { "8 >> 1 & 3", 0L }, { "8 | 3 ^ 1", 10L },
            { "7 ^ 3 & 1", 6L }, { "8 >> 4 / 2 % 2", 8L } };
        for (Object[] test : cases) {
            String expression = (String) test[0];
            assertEquals(
                (long) test[1],
                run("STATE $result = " + expression, "").read(0)
                    .asLong(),
                expression);
            assertEquals(
                (long) test[1],
                run("STATE $result = 0", "LET $zero = 0 SET $result = " + expression + " + $zero").read(0)
                    .asLong(),
                "dynamic " + expression);
        }
        MemoryState state = run(
            "STATE $result = 0",
            "LET $n = 4 IF (NOT $n & 3 = 0 OR FALSE AND TRUE) THEN SET $result = 99 END "
                + "IF ($n | 1 = 5 AND NOT $n >> 1 < 2 OR FALSE) THEN SET $result = 7 END");
        assertEquals(
            7,
            state.read(0)
                .asLong());
    }

    @Test
    void shiftDistancesOutsideZeroThroughSixtyThreeAreNeverMasked() {
        for (String operator : Arrays.asList("<<", ">>", ">>>")) {
            for (long distance : new long[] { -1, 64, 65, Long.MIN_VALUE, Long.MAX_VALUE }) {
                String expression = "1 " + operator + " " + distance;
                ProgramBuildResult result = new ProgramBuilder("STATE $bad = " + expression + " EVERY 20 TICKS DO END")
                    .build();
                assertFalse(result.isSuccess(), "STATE " + expression);
                assertThrows(
                    IllegalArgumentException.class,
                    () -> run(
                        "STATE $result = 7",
                        "LET $distance = " + distance + " SET $result = 1 " + operator + " $distance"),
                    expression);
            }
        }
    }

    @Test
    void invalidConstantShiftsFaultOnlyWhenTheirConditionalBranchIsEvaluated() {
        MemoryState state = run(
            "STATE $result = 0",
            "IF (FALSE AND 1 << 64 = 0) THEN SET $result = 99 END "
                + "IF (TRUE OR 1 >>> -1 = 0) THEN SET $result = 7 END "
                + "IF FALSE THEN LET $bad = 1 >> 64 END");
        assertEquals(
            7,
            state.read(0)
                .asLong());
        for (String condition : Arrays.asList("TRUE AND 1 << 64 = 0", "FALSE OR 1 >>> -1 = 0")) {
            assertThrows(
                IllegalArgumentException.class,
                () -> run("STATE $result = 7", "IF (" + condition + ") THEN SET $result = 99 END"),
                condition);
        }
    }

    @Test
    void failedShiftAssignmentPreservesPreviousStateAndStopsLaterStatements() {
        Program program = program(
            "STATE $result = { earlier: 0, kept: [5, 6] } EVERY 20 TICKS DO "
                + "SET $result.earlier = 7 LET $distance = 64 SET $result.kept[1] = 1 << $distance "
                + "SET $result.earlier = 99 END");
        MemoryState state = new MemoryState(program.getStateSchema());
        assertThrows(IllegalArgumentException.class, () -> program.tick(context(state)));
        assertEquals(
            7,
            state.read(0)
                .get("earlier")
                .asLong());
        assertEquals(
            5,
            state.read(0)
                .get("kept")
                .get(0)
                .asLong());
        assertEquals(
            6,
            state.read(0)
                .get("kept")
                .get(1)
                .asLong());
    }

    @Test
    void everyBitwiseOperatorRequiresIntegerOperandsWithoutCoercion() {
        for (String value : Arrays.asList("TRUE", "\"1\"", "[1]", "{value: 1}", "LABEL(\"source\")", "ITEM(\"*\")")) {
            assertRejected("~" + value);
            for (String operator : Arrays.asList("&", "|", "^", "<<", ">>", ">>>")) {
                assertRejected(value + " " + operator + " 1");
                assertRejected("1 " + operator + " " + value);
            }
        }
    }

    @Test
    void binaryOperandsAreObservedLeftThenRightExactlyOnce() {
        String[] operators = { "&", "|", "^", "<<", ">>", ">>>" };
        long[] expected = { 2, 7, 5, 48, 0, 0 };
        for (int i = 0; i < operators.length; i++) {
            RecordingQueries queries = new RecordingQueries();
            DataExpression expression = new DataCompiler()
                .compile(count("left") + " " + operators[i] + " " + count("right"), 1, 0);
            assertTrue(queries.calls.isEmpty());
            assertEquals(
                expected[i],
                expression.evaluate(new DataRuntime(null, 0, queries))
                    .asLong());
            assertEquals(Arrays.asList("left", "right"), queries.calls, operators[i]);
            queries = new RecordingQueries();
            queries.failLeft = true;
            DataRuntime runtime = new DataRuntime(null, 0, queries);
            assertThrows(IllegalArgumentException.class, () -> expression.evaluate(runtime));
            assertEquals(Arrays.asList("left"), queries.calls, operators[i]);
        }
    }

    @Test
    void bitwiseSourceRoundTripPreservesDefaultsAndDynamicMeaning() {
        Program original = program(
            "NAME \"Bitwise\" STATE $result = 1 << 63 EVERY 20 TICKS DO "
                + "LET $n = $result LET $distance = 63 SET $result = (~$n & 15 | 32 ^ 1) "
                + "+ ($n >> $distance) + ($n >>> $distance) + (1 << 2) END");
        Program rebuilt = program(original.toString());
        MemoryState state = new MemoryState(rebuilt.getStateSchema());
        assertEquals(
            Long.MIN_VALUE,
            state.read(0)
                .asLong());
        rebuilt.tick(context(state));
        assertEquals(
            51,
            state.read(0)
                .asLong());
    }

    @Test
    void legacyComparisonsBooleanWordsAndResourceWildcardsStillParse() {
        Program legacy = program(
            "EVERY 20g+1 TICKS DO INPUT a*b FROM LET OUTPUT *3 TO STATE FORGET SET "
                + "IF TRUE AND NOT FALSE OR FALSE THEN END IF REDSTONE >= 0 THEN END "
                + "IF source HAS >= 1 THEN END END");
        assertFalse(legacy.hasDataFeatures());
        assertEquals(
            7,
            run(
                "STATE $result = 0",
                "LET $n = 4 IF ($n GE 4 AND $n LE 4 " + "AND $n GT 3 AND $n LT 5 AND $n EQ 4) THEN SET $result = 7 END")
                    .read(0)
                    .asLong());
    }

    @Test
    void compactSymbolsAndCommentsKeepTheSameTokenBoundaries() {
        MemoryState state = run(
            "STATE $result=0",
            "LET $x=5 LET $bits=[$x<<2,~$x,$x^3] LET $record={mask:$bits[0]|$bits[2]} "
                + "LET $twice=$x -- operator after comment\r\n<<1 "
                + "SET $result=($record.mask&~2)>>>1 "
                + "IF ($x>>1>=2 AND $x<<1<=10 AND $twice=10) THEN SET $result=$result|1 END");
        assertEquals(
            11,
            state.read(0)
                .asLong());
    }

    private static void assertRejected(String expression) {
        assertThrows(IllegalArgumentException.class, () -> new DataCompiler().compile(expression, 1, 0), expression);
        assertFalse(
            new ProgramBuilder("EVERY 20 TICKS DO LET $bad = " + expression + " END").build()
                .isSuccess(),
            expression);
    }

    private static String count(String label) {
        return "LABEL(\"" + label + "\").count(ITEM(\"*\"))";
    }

    private static Program program(String source) {
        ProgramBuildResult result = new ProgramBuilder(source).build();
        assertTrue(result.isSuccess(), result.getErrorMessage());
        return result.getProgram();
    }

    private static MemoryState run(String declarations, String body) {
        Program program = program(declarations + " EVERY 20 TICKS DO " + body + " END");
        MemoryState state = new MemoryState(program.getStateSchema());
        program.tick(context(state));
        return state;
    }

    private static ProgramContext context(MemoryState state) {
        ProgramContext context = new ProgramContext(null, null, null, null, 20, false);
        context.setStateAccess(state);
        return context;
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

    private static final class RecordingQueries implements DataQueries {

        final List<String> calls = new ArrayList<>();
        boolean failLeft;

        @Override
        public long count(LabelReference label, ResourceSelector selector, QueryBudget budget) {
            calls.add(label.getName());
            budget.chargeSlot();
            if (failLeft && label.getName()
                .equals("left")) throw new IllegalArgumentException("left fault");
            return label.getName()
                .equals("left") ? 6 : 3;
        }

        @Override
        public boolean empty(LabelReference label, ResourceSelector selector, QueryBudget budget) {
            throw new AssertionError("Unexpected empty query");
        }

        @Override
        public boolean available(LabelReference label, ResourceSelector selector, QueryBudget budget) {
            throw new AssertionError("Unexpected available query");
        }
    }
}
