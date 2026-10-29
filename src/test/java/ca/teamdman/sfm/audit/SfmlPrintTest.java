package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.DebugConsole;
import ca.teamdman.sfm.common.program.DebugValueFormatter;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.PrintStatement;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.ResourceSelector;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;
import ca.teamdman.sfml.program_builder.VisualCapabilityAnalyzer;
import ca.teamdman.sfml.program_builder.VisualCapabilityReport;

class SfmlPrintTest {

    private static ProgramBuildResult build(String source) {
        ProgramBuildResult result = new ProgramBuilder(source).buildWithSyntax();
        assertTrue(result.isSuccess(), result.getErrorMessage());
        return result;
    }

    private static ProgramContext context(AuditRegressionTest.Fixture fixture, TileEntityManager manager) {
        return new ProgramContext(
            fixture.world,
            manager,
            new CableNetwork(fixture.world, AuditRegressionTest.MANAGER),
            fixture.labels,
            20,
            false);
    }

    private static Object field(Object owner, Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(owner);
    }

    @Test
    void disabledPrintSkipsLiveQueriesFaultingExpressionsAndConsoleAllocationButStillChecksTypes() throws Exception {
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        TileEntityManager manager = new TileEntityManager();
        ProgramContext context = context(fixture, manager);
        Program program = build("EVERY 20 TICKS DO PRINT(LABEL(\"missing\").count(ITEM(\"*\")), 1 / 0) END")
            .getProgram();
        assertTrue(program.hasDataFeatures());
        assertTrue(
            program.getReferencedLabels()
                .contains("missing"));
        program.tick(context);
        assertFalse(manager.hasDebugOutputEnabled());
        assertNull(field(manager, TileEntityManager.class, "debugConsole"));
        assertNull(field(context, ProgramContext.class, "itemQueries"));
        assertEquals(
            0,
            context.getDataRuntime()
                .getWork());
        assertFalse(
            new ProgramBuilder("EVERY 20 TICKS DO PRINT($unknown) END").build()
                .isSuccess());
        assertFalse(
            new ProgramBuilder("EVERY 20 TICKS DO PRINT(1 + TRUE) END").build()
                .isSuccess());
        manager.getDebugConsole()
            .setEnabled(true);
        assertThrows(IllegalArgumentException.class, () -> program.tick(context(fixture, manager)));
    }

    @Test
    void enabledPrintEvaluatesEachArgumentExactlyOnceInOrderAndFormatsTypedValues() throws Exception {
        TileEntityManager manager = new TileEntityManager();
        manager.getDebugConsole()
            .setEnabled(true);
        ProgramContext context = context(new AuditRegressionTest.Fixture(), manager);
        context.beginDataInvocation(0, false);
        List<Integer> order = new ArrayList<>();
        List<DataExpression> arguments = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            final int position = i;
            arguments.add(new DataExpression() {

                public DataType getType() {
                    return DataType.INTEGER;
                }

                public DataValue evaluate(DataRuntime runtime) {
                    order.add(position);
                    return DataValue.integer(position);
                }
            });
        }
        new PrintStatement(arguments).tick(context);
        assertEquals(Arrays.asList(0, 1, 2), order);
        assertEquals(
            Collections.singletonList("0 1 2"),
            manager.getDebugConsole()
                .snapshot());
        assertEquals(
            "cycle\\n 1 [\"nested\"]",
            DebugValueFormatter.formatPrint(
                new DataRuntime(null, 0),
                DataValue.string("cycle\n"),
                DataValue.integer(1),
                DataValue.array(Collections.singletonList(DataValue.string("nested")))));
        assertEquals(
            "\"hello\" TRUE [1, 2] {flag: FALSE} LABEL(\"source\").side(\"north\").slots(2, 4) ITEM(\"*\")",
            DebugValueFormatter.format(
                DataValue.string("hello"),
                DataValue.bool(true),
                DataValue.array(Arrays.asList(DataValue.integer(1), DataValue.integer(2))),
                DataValue.struct(Collections.singletonMap("flag", DataValue.bool(false))),
                DataValue.label(
                    LabelReference.literal("source")
                        .side(2)
                        .slots(2, 4)),
                DataValue.selector(ResourceSelector.item("*"))));
    }

    @Test
    void invocationPrintCapIsSharedAcrossDueTriggersSkipsArgumentsAndDoesNotStopStateWork() throws Exception {
        StringBuilder code = new StringBuilder("STATE $done = 0 EVERY 20 TICKS DO ");
        for (int i = 0; i < ProgramContext.MAX_PRINT_LINES; i++) code.append("PRINT(1) ");
        code.append("END EVERY 20 TICKS DO PRINT(LABEL(\"missing\").count(ITEM(\"*\"))) SET $done = $done + 1 END");
        Program program = build(code.toString()).getProgram();
        TileEntityManager manager = new TileEntityManager();
        manager.getDebugConsole()
            .setEnabled(true);
        ProgramContext context = context(new AuditRegressionTest.Fixture(), manager);
        Memory state = new Memory();
        context.setStateAccess(state);
        program.tick(context);
        assertEquals(
            16,
            manager.getDebugConsole()
                .snapshot()
                .size());
        assertEquals(
            1,
            manager.getDebugConsole()
                .getDroppedLines());
        assertEquals(1, state.value.asLong());
        assertNull(field(context, ProgramContext.class, "itemQueries"));
        program.tick(context);
        assertEquals(
            32,
            manager.getDebugConsole()
                .snapshot()
                .size());
        assertEquals(
            2,
            manager.getDebugConsole()
                .getDroppedLines());
        assertEquals(2, state.value.asLong());
    }

    @Test
    void completePrintProgramObservesStateAndTransfersWithoutMutatingTheirValues() throws Exception {
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        fixture.stock(0, 9);
        TileEntityManager manager = new TileEntityManager();
        manager.getDebugConsole()
            .setEnabled(true);
        Program program = build(
            "STATE $done = 0 EVERY 20 TICKS DO " + "PRINT(\"before\", LABEL(\"source\").count(ITEM(\"*\")), $done) "
                + "INPUT 2 FROM source OUTPUT TO dest SET $done = $done + 1 "
                + "PRINT(\"after\", LABEL(\"source\").count(ITEM(\"*\")), $done) END").getProgram();
        ProgramContext context = context(fixture, manager);
        Memory state = new Memory();
        context.setStateAccess(state);
        program.tick(context);
        assertEquals(
            Arrays.asList("before 9 0", "after 7 1"),
            manager.getDebugConsole()
                .snapshot());
        assertEquals(7, AuditRegressionTest.count(fixture.source));
        assertEquals(2, AuditRegressionTest.count(fixture.dest));
        assertEquals(1, state.value.asLong());
    }

    @Test
    void printSyntaxRequiresOneToEightArgumentsAndRemainsAStatementWithPreciseVisualBlocker() {
        for (String body : Arrays.asList("PRINT()", "PRINT(1,)", "PRINT(1,2,3,4,5,6,7,8,9)", "LET $x = PRINT(1)"))
            assertFalse(
                new ProgramBuilder("EVERY 20 TICKS DO " + body + " END").build()
                    .isSuccess(),
                body);
        ProgramBuildResult result = build("EVERY 20 TICKS DO\n PRINT(1,2,3,4,5,6,7,8)\nEND");
        VisualCapabilityReport report = VisualCapabilityAnalyzer.analyze(result);
        assertFalse(report.isAvailable());
        assertEquals(
            1,
            report.getBlockers()
                .size());
        VisualCapabilityReport.Blocker blocker = report.getBlockers()
            .get(0);
        assertEquals(VisualCapabilityReport.Kind.PRINT_STATEMENT, blocker.kind);
        assertEquals(2, blocker.line);
        assertEquals(1, blocker.column);
        assertTrue(blocker.message.contains("PRINT"));
        assertEquals(
            result.getProgram()
                .toString(),
            build(
                result.getProgram()
                    .toString()).getProgram()
                        .toString());
    }

    @Test
    void printTokenStillWorksAsLegacyLabelResourceAndTypedStructField() {
        ProgramBuildResult legacy = build("EVERY 20 TICKS DO INPUT PRINT FROM PRINT OUTPUT TO PRINT END");
        assertEquals(
            Collections.singleton("PRINT"),
            legacy.getProgram()
                .getReferencedLabels());
        assertTrue(
            VisualCapabilityAnalyzer.analyze(legacy)
                .isAvailable());
        build("EVERY 20 TICKS DO LET $record = {PRINT: 1} PRINT($record.PRINT) END");
        assertTrue(
            VisualCapabilityAnalyzer.analyze(build("NAME \"PRINT(1)\" EVERY 20 TICKS DO FORGET END"))
                .isAvailable());
    }

    @Test
    void formatterBoundsCollectionDepthVisitedCellsAndOutputWhileEscapingSingleLineControls() {
        DataValue wide = DataValue.array(Collections.nCopies(256, DataValue.integer(1)));
        DataRuntime runtime = new DataRuntime(null, 0);
        String displayed = DebugValueFormatter.format(runtime, wide);
        assertTrue(displayed.endsWith("\u2026"));
        assertTrue(displayed.length() <= DebugConsole.MAX_LINE_LENGTH);
        assertTrue(runtime.getWork() < 1024, "Formatting must stop after the cell bound");
        DataValue deep = DataValue.integer(1);
        for (int i = 0; i < 12; i++) deep = DataValue.array(Collections.singletonList(deep));
        String nested = DebugValueFormatter.format(deep);
        assertTrue(nested.contains("..."));
        assertTrue(nested.length() < 32);
        assertEquals(
            "\"a\\n\\r\\t\\u0000\\u00a7\"",
            DebugValueFormatter.format(DataValue.string("a\n\r\t\u0000\u00a7")));
        String safe = DebugValueFormatter.sanitize("a\n\r\t\u0000\u00a7\ud800");
        assertEquals("a\\n\\r\\t\\u0000\\u00a7\\ufffd", safe);
        assertEquals("\\udb40\\udc01", DebugValueFormatter.sanitize("\udb40\udc01"));
        String edge = DebugValueFormatter.sanitize(repeat('x', 510) + "\ud83d\ude00");
        assertTrue(edge.length() <= DebugConsole.MAX_LINE_LENGTH);
        for (int i = 0; i < edge.length(); i++) {
            if (Character.isHighSurrogate(edge.charAt(i)))
                assertTrue(++i < edge.length() && Character.isLowSurrogate(edge.charAt(i)));
            else assertFalse(Character.isLowSurrogate(edge.charAt(i)));
        }
    }

    @Test
    void consoleBoundsImmutableSnapshotsEvictionCountersAndCaptureConfiguration() {
        DebugConsole console = new DebugConsole();
        assertFalse(console.isEnabled());
        for (int i = 0; i < 201; i++) console.append(Integer.toString(i));
        assertEquals(
            200,
            console.snapshot()
                .size());
        assertEquals(
            "1",
            console.snapshot()
                .get(0));
        assertEquals(1, console.getDroppedLines());
        List<String> snapshot = console.snapshot();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add("bad"));
        console.clear();
        assertEquals(200, snapshot.size());
        assertEquals(0, console.getDroppedLines());
        console.setEnabled(true);
        for (int i = 0; i < 40; i++) console.append(repeat('x', 600));
        assertTrue(
            console.snapshot()
                .stream()
                .allMatch(line -> line.length() <= DebugConsole.MAX_LINE_LENGTH));
        assertTrue(
            console.snapshot()
                .stream()
                .mapToInt(String::length)
                .sum() <= DebugConsole.MAX_CHARACTERS);
        assertEquals(8, console.getDroppedLines());
        console.clear();
        assertTrue(console.isEnabled());
    }

    @Test
    void consoleHooksNeverDirtyManagerAndCaptureHistoryDoNotSurviveNbtReload() throws Exception {
        CountingManager counter = new CountingManager();
        ProgramContext context = new ProgramContext(null, counter, null, null, 0, false);
        assertFalse(context.hasDebugOutputEnabled());
        assertNull(field(counter, TileEntityManager.class, "debugConsole"));
        context.getDebugConsole()
            .setEnabled(true);
        context.getDebugConsole()
            .append("temporary");
        context.getDebugConsole()
            .clear();
        assertEquals(0, counter.dirtyCalls);
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        manager.saveCodeAndRecompile("EVERY 20 TICKS DO PRINT(1) END");
        NBTTagCompound before = (NBTTagCompound) manager.getDisk()
            .getTagCompound()
            .copy();
        manager.getDebugConsole()
            .setEnabled(true);
        manager.getDebugConsole()
            .append("temporary");
        assertEquals(
            before,
            manager.getDisk()
                .getTagCompound());
        NBTTagCompound saved = new NBTTagCompound();
        manager.writeToNBT(saved);
        TileEntityManager loaded = new TileEntityManager();
        loaded.setWorldObj(fixture.world);
        loaded.readFromNBT(saved);
        assertFalse(loaded.hasDebugOutputEnabled());
        assertNull(field(loaded, TileEntityManager.class, "debugConsole"));
        assertTrue(
            loaded.getDebugConsole()
                .snapshot()
                .isEmpty());
    }

    private static String repeat(char value, int count) {
        char[] characters = new char[count];
        Arrays.fill(characters, value);
        return new String(characters);
    }

    private static final class CountingManager extends TileEntityManager {

        int dirtyCalls;

        @Override
        public void markDirty() {
            dirtyCalls++;
        }
    }

    private static final class Memory implements StateAccess {

        DataValue value = DataValue.integer(0);

        public DataValue read(int slot) {
            return value;
        }

        public void write(int slot, DataValue value) {
            this.value = value;
        }
    }
}
