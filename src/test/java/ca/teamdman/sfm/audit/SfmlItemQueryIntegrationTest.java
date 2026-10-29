package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;
import ca.teamdman.sfml.program_builder.VisualCapabilityAnalyzer;

class SfmlItemQueryIntegrationTest {

    @Test
    void localRestrictedHandlesAndNoArgumentPredicatesCompileAndObserveTheSamePhysicalInventory() throws Exception {
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        fixture.stock(0, 6);
        fixture.stock(1, 11);
        Program program = program(
            "STATE $result = {selected:0, original:0, empty:FALSE, available:FALSE, missing:TRUE} EVERY 20 TICKS DO "
                + "LET $source = LABEL(\"source\") LET $slot = 1 "
                + "LET $selected = $source.side(\"north\").slots($slot, $slot) "
                + "SET $result.selected = $selected.count(ITEM(\"*\")) "
                + "SET $result.original = $source.count(ITEM(\"*\")) "
                + "SET $result.available = $selected.available() "
                + "SET $result.empty = $source.slots(2, 2).empty() "
                + "SET $result.missing = LABEL(\"missing\").available() OR LABEL(\"missing\").empty() END");
        Memory state = new Memory(program);
        program.tick(context(fixture, state, 20));
        assertEquals(
            11,
            state.value.get("selected")
                .asLong());
        assertEquals(
            17,
            state.value.get("original")
                .asLong());
        assertTrue(
            state.value.get("empty")
                .asBoolean());
        assertTrue(
            state.value.get("available")
                .asBoolean());
        assertFalse(
            state.value.get("missing")
                .asBoolean());
        assertEquals(17, AuditRegressionTest.count(fixture.source));
        assertEquals(
            new java.util.HashSet<>(java.util.Arrays.asList("source", "missing")),
            program.getReferencedLabels());
    }

    private static Program program(String source) {
        ProgramBuildResult result = new ProgramBuilder(source).build();
        assertTrue(result.isSuccess(), result.getErrorMessage());
        return result.getProgram();
    }

    private static ProgramContext context(AuditRegressionTest.Fixture fixture, StateAccess state, long tick) {
        ProgramContext context = new ProgramContext(
            fixture.world,
            null,
            new CableNetwork(fixture.world, AuditRegressionTest.MANAGER),
            fixture.labels,
            tick,
            false);
        context.setStateAccess(state);
        return context;
    }

    @Test
    void compiledTypedReadsSeeEarlierTransfersAndCollectBothTypedAndLegacyLabels() throws Exception {
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        fixture.stock(0, 6);
        Program program = program(
            "STATE $result = {before:0, after:0, available:FALSE} EVERY 20 TICKS DO "
                + "LET $src = LABEL(\"source\") LET $all = ITEM(\"*\") "
                + "SET $result.before = $src.count($all) INPUT 2 FROM source OUTPUT TO dest "
                + "SET $result.after = $src.count($all) SET $result.available = $src.available($all) END");
        Memory state = new Memory(program);
        program.tick(context(fixture, state, 20));
        assertEquals(
            6,
            state.value.get("before")
                .asLong());
        assertEquals(
            4,
            state.value.get("after")
                .asLong());
        assertTrue(
            state.value.get("available")
                .asBoolean());
        assertEquals(4, AuditRegressionTest.count(fixture.source));
        assertEquals(2, AuditRegressionTest.count(fixture.dest));
        assertEquals(
            2,
            program.getReferencedLabels()
                .size());
        assertTrue(
            program.getReferencedLabels()
                .containsAll(java.util.Arrays.asList("source", "dest")));
    }

    @Test
    void missingTypedGroupDoesNotChangeLegacyHasZeroAndShortCircuitAvoidsCountFault() throws Exception {
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        Program program = program(
            "STATE $result = {legacy:FALSE, typed:TRUE, guarded:FALSE} EVERY 20 TICKS DO "
                + "IF missing HAS = 0 THEN SET $result.legacy = TRUE END "
                + "LET $src = LABEL(\"missing\") LET $all = ITEM(\"*\") "
                + "SET $result.typed = $src.empty($all) "
                + "IF ($src.available($all) AND $src.count($all) > 0) THEN SET $result.guarded = TRUE END END");
        Memory state = new Memory(program);
        program.tick(context(fixture, state, 20));
        assertTrue(
            state.value.get("legacy")
                .asBoolean());
        assertFalse(
            state.value.get("typed")
                .asBoolean());
        assertFalse(
            state.value.get("guarded")
                .asBoolean());
    }

    @Test
    void completeProgramClassificationKeepsTypedQueriesInText() {
        String source = "EVERY 20 TICKS DO IF (FALSE AND LABEL(\"unreachable\").empty(ITEM(\"*\"))) THEN END END";
        ProgramBuildResult result = new ProgramBuilder(source).buildWithSyntax();
        assertTrue(result.isSuccess(), result.getErrorMessage());
        assertFalse(
            VisualCapabilityAnalyzer.analyze(result)
                .isAvailable());
        assertEquals(
            Collections.singleton("unreachable"),
            result.getProgram()
                .getReferencedLabels());
    }

    @Test
    void sharedQueryBudgetFaultsAcrossDueTriggersButResetsForNextInvocation() throws Exception {
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        fixture.add("large", AuditRegressionTest.SRC, new AuditRegressionTest.InventoryTile(4000));
        String trigger = " EVERY 20 TICKS DO LET $n = LABEL(\"large\").count(ITEM(\"*\")) SET $done = $done + 1 END";
        Program program = program("STATE $done = 0" + trigger + trigger + trigger);
        Memory state = new Memory(program);
        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> program.tick(context(fixture, state, 20)));
        assertTrue(
            error.getMessage()
                .contains("budget exhausted"),
            error.getMessage());
        assertEquals(2, state.value.asLong(), "No partial result from the third query may publish its SET");
        assertThrows(IllegalArgumentException.class, () -> program.tick(context(fixture, state, 40)));
        assertEquals(4, state.value.asLong(), "A new invocation receives a new budget");
        fixture.run("IF large HAS = 0 THEN FORGET END"); // Legacy HAS has no new query cutoff.
    }

    @Test
    void noDueTriggersOrQueriesCreateNoQueryService() throws Exception {
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        Program program = program("EVERY 20 TICKS DO LET $n = LABEL(\"source\").count(ITEM(\"*\")) END");
        ProgramContext idle = context(fixture, null, 1);
        program.tick(idle);
        Field service = ProgramContext.class.getDeclaredField("itemQueries");
        service.setAccessible(true);
        assertNull(service.get(idle));
        ProgramContext arithmetic = context(fixture, null, 20);
        program("EVERY 20 TICKS DO LET $n = 1 + 2 END").tick(arithmetic);
        assertNull(service.get(arithmetic));
    }

    private static final class Memory implements StateAccess {

        DataValue value;

        Memory(Program program) {
            value = program.getStateSchema()
                .getDeclarations()
                .get(0)
                .getDefaultValue();
        }

        public DataValue read(int slot) {
            return value;
        }

        public void write(int slot, DataValue next) {
            value = next;
        }
    }
}
