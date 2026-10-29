package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

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

/** Bare assignments reuse the exact SET target resolution, type checks and execution path. */
class SfmlDirectAssignmentTest {

    @Test
    void declaredTriggerLocalsAndStateAcceptBothSpellingsInOneBlock() {
        for (String assignment : Arrays.asList("", "SET ")) {
            Program program = program(
                "STATE $result = 0 EVERY 20 TICKS DO LET $n = 1 " + assignment
                    + "$n = $n + 1 SET $n = $n + 2 "
                    + assignment
                    + "$result = $n END");
            Memory state = run(program);
            assertEquals(
                4,
                state.read(0)
                    .asLong());
        }
    }

    @Test
    void programGlobalsShareDirectWritesAcrossTriggersAndFreshManagerContexts() throws Exception {
        Program program = program(
            "LET $count = 0 STATE $result = 0 "
                + "EVERY 20 TICKS DO LET $n = 1 $count = $count + $n $result = $count END "
                + "EVERY 20 TICKS DO LET $n = 2 $count = $count + $n $result = $count END");
        Fixture fixture = new Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        Memory state = new Memory(program.getStateSchema());
        ProgramContext first = context(fixture, manager, state, 20);
        program.tick(first);
        assertEquals(
            3,
            state.read(0)
                .asLong());
        ProgramContext next = context(fixture, manager, state, 40);
        program.tick(next);
        assertSame(first.getProgramGlobals(program), next.getProgramGlobals(program));
        assertEquals(
            6,
            next.getProgramGlobals(program)
                .read(0)
                .asLong());
        assertEquals(
            6,
            state.read(0)
                .asLong());
    }

    @Test
    void globalStateAndLocalNestedTargetsUseTheExistingCopyOnWriteUpdates() {
        Program program = program(
            "LET $global = {values:[1,2]} STATE $state = {values:[3,4]} "
                + "EVERY 20 TICKS DO LET $index = 1 LET $local = {rows:[{n:1},{n:2}]} "
                + "$global.values[$index] = 7 $state.values[0] = $global.values[1] "
                + "$local.rows[1].n = $global.values[1] + 1 $state.values[1] = $local.rows[1].n END");
        Memory state = new Memory(program.getStateSchema());
        ProgramContext context = bareContext(state);
        program.tick(context);
        assertEquals(
            7,
            state.read(0)
                .get("values")
                .get(0)
                .asLong());
        assertEquals(
            8,
            state.read(0)
                .get("values")
                .get(1)
                .asLong());
        assertEquals(
            1,
            context.getProgramGlobals(program)
                .read(0)
                .get("values")
                .get(0)
                .asLong());
        assertEquals(
            7,
            context.getProgramGlobals(program)
                .read(0)
                .get("values")
                .get(1)
                .asLong());
    }

    @Test
    void functionsCanAssignParametersLocalsAndNestedParameterFieldsWithoutChangingArguments() {
        Program program = program(
            "STATE $result = 0 FUNCTION adjust($n: INTEGER, $row: {n: INTEGER}) RETURNS INTEGER DO "
                + "$n = $n + 1 $row.n = $row.n + $n LET $local = $row.n $local = $local + 1 RETURN $local END "
                + "EVERY 20 TICKS DO LET $argument = {n:4} $result = adjust(2, $argument) "
                + "IF ($argument.n != 4) THEN $result = -1 END END");
        assertEquals(
            8,
            run(program).read(0)
                .asLong());
    }

    @Test
    void functionParameterNamesRemainIndependentFromProgramGlobals() {
        Program program = program(
            "LET $n = 9 STATE $result = 0 " + "FUNCTION bump($n: INTEGER) RETURNS INTEGER DO $n = $n + 1 RETURN $n END "
                + "EVERY 20 TICKS DO $result = bump(2) + $n END");
        assertEquals(
            12,
            run(program).read(0)
                .asLong());
    }

    @Test
    void assignmentsWorkInsideCountedArrayAndWhileLoopsAndFunctionLoopBodies() {
        Program program = program(
            "STATE $result = 0 " + "FUNCTION sum($n: INTEGER) RETURNS INTEGER DO LET $total = 0 "
                + "FOR $i = 1 TO $n DO $total = $total + $i END RETURN $total END "
                + "EVERY 20 TICKS DO LET $total = sum(3) FOR $item IN [1,2] DO $total = $total + $item END "
                + "LET $remaining = 2 WHILE ($remaining > 0) DO "
                + "$total = $total + $remaining $remaining = $remaining - 1 END $result = $total END");
        assertEquals(
            12,
            run(program).read(0)
                .asLong());
    }

    @Test
    void declarationsRemainMandatoryAndNamesKeepTheirExistingScopeAndCaseRules() {
        for (String body : Arrays.asList(
            "$missing = 1",
            "$later = 1 LET $later = 0",
            "LET $known = 0 $known = $missing",
            "LET $lower = 0 $LOWER = 1",
            "IF TRUE THEN LET $inside = 0 END $inside = 1",
            "number = 1",
            "SET number = 1",
            "$row.field = 1")) rejected("EVERY 20 TICKS DO " + body + " END");
        rejected("EVERY 20 TICKS DO LET $local = 0 END EVERY 20 TICKS DO $local = 1 END");
        rejected("LET $global = 0 $global = 1 EVERY 20 TICKS DO END");
        rejected(
            "LET $global = 0 FUNCTION bad() RETURNS INTEGER DO $global = 1 RETURN 1 END " + "EVERY 20 TICKS DO END");
        rejected(
            "STATE $saved = 0 FUNCTION bad() RETURNS INTEGER DO $saved = 1 RETURN 1 END " + "EVERY 20 TICKS DO END");
    }

    @Test
    void directSyntaxCannotBypassStrictTypesFieldNamesOrReadonlyLoopBindings() {
        for (String body : Arrays.asList(
            "LET $n = 0 $n = TRUE",
            "LET $values = [1,2] $values = [3]",
            "LET $row = {n:1} $row.other = 2",
            "LET $values = [1,2] $values[TRUE] = 3",
            "FOR $i = 1 TO 2 DO $i = 0 END",
            "FOR $row IN [{n:1}] DO $row.n = 2 END")) rejected("EVERY 20 TICKS DO " + body + " END");
        rejected(
            "FUNCTION bad() RETURNS INTEGER DO FOR $i = 1 TO 2 DO $i = 0 END RETURN 1 END " + "EVERY 20 TICKS DO END");
    }

    @Test
    void letStillDeclaresAndCannotRedeclareOrShadowAnExistingVisibleName() {
        for (String source : Arrays.asList(
            "EVERY 20 TICKS DO LET $n = 1 LET $n = 2 END",
            "EVERY 20 TICKS DO LET $n = 1 IF TRUE THEN LET $n = 2 END END",
            "LET $n = 1 EVERY 20 TICKS DO LET $n = 2 END",
            "STATE $n = 1 EVERY 20 TICKS DO LET $n = 2 END",
            "FUNCTION bad($n: INTEGER) RETURNS INTEGER DO LET $n = 2 RETURN $n END EVERY 20 TICKS DO END"))
            rejected(source);
        Program valid = program("STATE $result = 0 EVERY 20 TICKS DO LET $n = 1 $n = 2 $result = $n END");
        assertEquals(
            2,
            run(valid).read(0)
                .asLong());
    }

    @Test
    void statementTargetsRequireSingleEqualsWhileComparisonAliasesRemainExpressions() {
        for (String delimiter : Arrays.asList("==", "!=", "+=", ":="))
            rejected("STATE $n = 0 EVERY 20 TICKS DO $n " + delimiter + " 1 END");
        Program program = program(
            "STATE $n = 0 STATE $same = FALSE STATE $different = FALSE "
                + "EVERY 20 TICKS DO $same = 1 == 1 $different = 1 != 2 "
                + "IF ($n = 1) THEN $n = 99 ELSE $n = 7 END END");
        Memory state = run(program);
        assertEquals(
            7,
            state.read(0)
                .asLong(),
            "An equality expression must not assign its left operand");
        assertTrue(
            state.read(1)
                .asBoolean());
        assertTrue(
            state.read(2)
                .asBoolean());
    }

    @Test
    void bareAndSetAssignmentsRoundTripThroughTheSameCanonicalExecutionPath() {
        String source = "LET $global = 1 STATE $result = 0 "
            + "FUNCTION twice($n: INTEGER) RETURNS INTEGER DO $n = $n + $n RETURN $n END "
            + "EVERY 20 TICKS DO LET $row = {n:2} $row.n = twice($row.n) "
            + "$global = $global + 1 SET $result = $row.n + $global END";
        Program original = program(source), rebuilt = program(original.toString());
        assertEquals(original.toString(), rebuilt.toString());
        assertEquals(
            6,
            run(original).read(0)
                .asLong());
        assertEquals(
            6,
            run(rebuilt).read(0)
                .asLong());
    }

    @Test
    void dollarTargetsLeaveLegacyWildcardResourcesAndKeywordLabelsUnambiguous() throws Exception {
        Program legacy = program("EVERY 20g+1 TICKS DO INPUT a*b FROM LET OUTPUT *3 TO STATE FORGET SET END");
        assertFalse(legacy.hasDataFeatures());
        Program program = program(
            "STATE $result = 0 EVERY 20 TICKS DO LET $amount = 2 $amount = 3 "
                + "INPUT ($amount) * FROM source OUTPUT * TO dest $result = $amount END");
        Fixture fixture = new Fixture();
        fixture.stock(0, 10);
        Memory state = new Memory(program.getStateSchema());
        execute(program, fixture, state);
        assertEquals(7, count(fixture.source));
        assertEquals(3, count(fixture.dest));
        assertEquals(
            3,
            state.read(0)
                .asLong());
    }

    private static void rejected(String source) {
        assertFalse(
            new ProgramBuilder(source).build()
                .isSuccess(),
            source);
    }

    private static Program program(String source) {
        ProgramBuildResult result = new ProgramBuilder(source).build();
        assertTrue(result.isSuccess(), result.getErrorMessage() + "\n" + source);
        return result.getProgram();
    }

    private static Memory run(Program program) {
        Memory state = new Memory(program.getStateSchema());
        program.tick(bareContext(state));
        return state;
    }

    private static ProgramContext bareContext(Memory state) {
        ProgramContext context = new ProgramContext(null, null, null, null, 20, false);
        context.setStateAccess(state);
        return context;
    }

    private static ProgramContext context(Fixture fixture, TileEntityManager manager, Memory state, long tick) {
        ProgramContext context = new ProgramContext(
            fixture.world,
            manager,
            new CableNetwork(fixture.world, MANAGER),
            fixture.labels,
            tick,
            false);
        context.setStateAccess(state);
        return context;
    }

    private static void execute(Program program, Fixture fixture, Memory state) {
        program.tick(context(fixture, null, state, 20));
    }

    private static final class Memory implements StateAccess {

        final DataValue[] values;

        Memory(ProgramStateSchema schema) {
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
            values[slot] = value;
        }
    }
}
