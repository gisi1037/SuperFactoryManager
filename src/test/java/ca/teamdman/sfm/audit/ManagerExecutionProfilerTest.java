package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.SaveHandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.program.DebugTerminalService;
import ca.teamdman.sfm.common.program.ManagerExecutionProfiler;
import ca.teamdman.sfm.common.program.ProgramStateStore;
import ca.teamdman.sfm.common.tileentity.LogLevel;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.data.SourceLocation;

class ManagerExecutionProfilerTest {

    @TempDir
    Path temporary;

    @Test
    void boundedSampleWindowUsesNearestRankAndStopsWithoutOverwritingResults() {
        assertThrows(IllegalArgumentException.class, () -> new ManagerExecutionProfiler(0));
        assertThrows(IllegalArgumentException.class, () -> new ManagerExecutionProfiler(4097));
        ManagerExecutionProfiler profiler = new ManagerExecutionProfiler(100);
        for (int i = 1; i <= 100; i++) profiler.recordUpdate(i * 1_000_000L, i % 4 == 0);
        ManagerExecutionProfiler.Snapshot snapshot = profiler.snapshot();
        assertFalse(snapshot.collecting);
        assertEquals(100, snapshot.count);
        assertEquals(25, snapshot.dueUpdates);
        assertEquals(50.5, snapshot.averageMs, 0.000001);
        assertEquals(50, snapshot.p50Ms);
        assertEquals(95, snapshot.p95Ms);
        assertEquals(99, snapshot.p99Ms);
        assertEquals(100, snapshot.maximumMs);
        profiler.recordUpdate(999_000_000, true);
        assertEquals(100, profiler.snapshot().count);
        assertEquals(100, profiler.snapshot().maximumMs);
        profiler = new ManagerExecutionProfiler(2);
        profiler.recordUpdate(-1, false);
        profiler.stop();
        assertEquals(1, profiler.snapshot().count);
        assertEquals(0, profiler.snapshot().averageMs);
    }

    @Test
    void detailLimitsReportSkippedAttemptsWithoutStoppingUpdateCollection() {
        ManagerExecutionProfiler profiler = new ManagerExecutionProfiler(1);
        profiler.beginUpdate();
        for (int line = 1; line <= 257; line++) {
            SourceLocation location = new SourceLocation(line, 0, "Statement");
            profiler.recordStatement(location, profiler.beginStatement());
        }
        for (int attempt = 257; attempt < 2048; attempt++) profiler.beginStatement();
        assertEquals(0, profiler.beginStatement());
        profiler.recordUpdate(3000, true, 4);
        ManagerExecutionProfiler.Report report = profiler.report(null);
        assertEquals(256, report.statements.size());
        assertEquals(2, report.skippedStatements);
        assertEquals(1, profiler.snapshot().count);
    }

    @Test
    void fullUpdateProfilerCountsIdleAndDueUpdatesAndDoesNotCountManualRun() throws Exception {
        Fixture fixture = new Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        ItemStack disk = manager.getDisk();
        ItemDisk.setProgramString(disk, "STATE $count = 0 EVERY 2 TICKS DO SET $count = $count + 1 END");
        manager.setInventorySlotContents(0, disk);
        manager.setRunning(true);
        Field field = TileEntityManager.class.getDeclaredField("executionProfiler");
        field.setAccessible(true);
        assertNull(field.get(manager));
        assertSame(ManagerExecutionProfiler.Snapshot.EMPTY, manager.getExecutionProfileSnapshot());
        manager.updateEntity();
        assertNull(field.get(manager), "OFF must not allocate the sample collector");
        manager.setManagerTick(0);
        DebugTerminalService.execute(manager, "profile start 5");
        for (int i = 0; i < 5; i++) manager.updateEntity();
        ManagerExecutionProfiler.Snapshot snapshot = manager.getExecutionProfileSnapshot();
        assertEquals(5, snapshot.count);
        assertEquals(2, snapshot.dueUpdates);
        assertFalse(snapshot.collecting);
        assertEquals(
            2,
            manager.getProgramState()
                .read(0)
                .asLong());
        DebugTerminalService.execute(manager, "profile start 3");
        manager.runOnceWithLogLevel(LogLevel.OFF);
        assertEquals(0, manager.getExecutionProfileSnapshot().count);
        manager.setRunning(false);
        manager.updateEntity();
        assertEquals(1, manager.getExecutionProfileSnapshot().count);
        assertEquals(0, manager.getExecutionProfileSnapshot().dueUpdates);
        assertEquals(0, manager.getExecutionProfileSnapshot().programUpdates);
        DebugTerminalService.execute(manager, "profile stop");
        manager.updateEntity();
        assertEquals(1, manager.getExecutionProfileSnapshot().count);
        DebugTerminalService.execute(manager, "profile reset");
        assertNull(field.get(manager));
        for (String command : new String[] { "profile start 0", "profile start 4097", "profile start nope",
            "profile start 3 extra", "profile reset extra" }) {
            DebugTerminalService.execute(manager, command);
            assertNull(field.get(manager), command);
        }
        manager.startExecutionProfile(2);
        manager.setInventorySlotContents(
            0,
            manager.getStackInSlot(0)
                .copy());
        assertNull(field.get(manager), "A replacement physical disk starts a new diagnostic lifetime");
        manager.startExecutionProfile(2);
        manager.onChunkUnload();
        assertNull(field.get(manager));
    }

    @Test
    void profileTimesTriggerResumesAndFunctionStatementsAndDumpsRawUpdates() throws Exception {
        Fixture fixture = new Fixture();
        fixture.world.saveHandler = new SaveHandler(temporary.toFile(), "audit", false);
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        ItemStack disk = manager.getDisk();
        ItemDisk.setProgramString(disk, """
            name "private example"
            integer cost(integer n) { return n + 1 }
            every 1 ticks {
              wait(2)
              let result = cost(4)
            }
            """);
        manager.setInventorySlotContents(0, disk);
        manager.setRunning(true);
        DebugTerminalService.execute(manager, "profile start 3");
        for (int tick = 1; tick <= 3; tick++) {
            fixture.world.time = tick;
            manager.updateEntity();
        }

        ManagerExecutionProfiler.Snapshot summary = manager.getExecutionProfileSnapshot();
        assertEquals(3, summary.count);
        assertEquals(3, summary.programUpdates);
        assertEquals(2, summary.dueUpdates);
        assertFalse(summary.collecting);
        ManagerExecutionProfiler.Report report = manager.getExecutionProfileReport(null);
        assertEquals(1, report.triggers.size());
        assertEquals(
            2,
            report.triggers.get(0)
                .attempts());
        assertEquals(
            1,
            report.triggers.get(0)
                .resumes());
        assertTrue(
            report.statements.stream()
                .anyMatch(row -> row.location.getLine() == 2));
        assertTrue(
            DebugTerminalService.execute(manager, "profile status")
                .toString()
                .contains("Hot statements"));

        String reply = DebugTerminalService.execute(manager, "profile dump")
            .get(0);
        assertTrue(reply.startsWith("Saved sfm-profiles/"), reply);
        Path dump = temporary.resolve("audit")
            .resolve(reply.substring("Saved ".length()));
        String json = new String(Files.readAllBytes(dump), StandardCharsets.UTF_8);
        JsonObject root = new JsonParser().parse(json)
            .getAsJsonObject();
        JsonArray samples = root.getAsJsonArray("samples");
        assertEquals(3, samples.size());
        assertEquals(
            1,
            samples.get(0)
                .getAsJsonObject()
                .get("game_tick")
                .getAsLong());
        assertFalse(
            samples.get(1)
                .getAsJsonObject()
                .get("trigger_due")
                .getAsBoolean());
        assertTrue(
            samples.get(2)
                .getAsJsonObject()
                .get("trigger_due")
                .getAsBoolean());
        assertEquals(
            1,
            root.getAsJsonArray("triggers")
                .get(0)
                .getAsJsonObject()
                .get("resumes")
                .getAsLong());
        assertEquals(
            report.statements.size(),
            root.getAsJsonArray("statements_inclusive")
                .size());
        assertFalse(json.contains("private example"));
    }

    @Test
    void watchCapsDisplayedVariablesAndFormatsValuesWithoutAnExpressionEvaluator() {
        java.util.List<ca.teamdman.sfml.data.ProgramStateSchema.Declaration> declarations = new java.util.ArrayList<>();
        for (int i = 0; i < 20; i++) declarations.add(
            new ca.teamdman.sfml.data.ProgramStateSchema.Declaration(
                "value" + i,
                ca.teamdman.sfml.data.DataValue.integer(i)));
        ItemStack disk = new ItemStack(new net.minecraft.item.Item());
        ProgramStateStore state = ProgramStateStore
            .activate(disk, new ca.teamdman.sfml.data.ProgramStateSchema(declarations));
        List<String> lines = DebugTerminalService.inspectVariables(state, null, null, null);
        assertEquals(16, lines.size());
        assertTrue(
            lines.get(15)
                .contains("15/20"));
        assertEquals(
            java.util.Collections.singletonList("STATE $value19 = 19"),
            DebugTerminalService.inspectVariables(state, null, null, "$value19"));
        assertTrue(
            DebugTerminalService.inspectVariables(state, null, null, "$value0 + 1")
                .get(0)
                .startsWith("No initialized variable"));
        assertTrue(state.isDirty(), "Read-only inspection must not serialize/flush STATE");
    }

    @Test
    void watchReadsInitializedLegacyGlobalsAndDirtyStateWithoutInitializingOrFlushing() throws Exception {
        Fixture fixture = new Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        ItemStack disk = manager.getDisk();
        ItemDisk.setProgramString(
            disk,
            "STATE $count = 0 LET $session = 7 EVERY 1 TICKS DO SET $count = $count + 1 SET $session = $session + 1 END");
        manager.setInventorySlotContents(0, disk);
        Field globals = TileEntityManager.class.getDeclaredField("programGlobals");
        globals.setAccessible(true);
        List<String> before = DebugTerminalService.execute(manager, "watch");
        assertTrue(
            before.stream()
                .anyMatch(line -> line.contains("STATE $count = 0")));
        assertNull(globals.get(manager), "Inspecting globals cannot initialize/evaluate them");
        manager.setRunning(true);
        manager.updateEntity();
        ProgramStateStore state = manager.getProgramState();
        assertTrue(state.isDirty());
        NBTTagCompound root = disk.getTagCompound();
        NBTTagCompound savedBefore = (NBTTagCompound) root.copy();
        List<String> watch = DebugTerminalService.execute(manager, "watch");
        assertTrue(watch.contains("STATE $count = 1"));
        assertTrue(watch.contains("GLOBAL $session = 8"));
        assertEquals(
            java.util.Collections.singletonList("GLOBAL $session = 8"),
            DebugTerminalService.execute(manager, "watch $session"));
        assertTrue(state.isDirty());
        assertSame(root, disk.getTagCompound());
        assertEquals(savedBefore, root);
        assertTrue(watch.size() <= DebugTerminalService.MAX_RESULT_LINES);
        manager.getWorldObj().isRemote = true;
        assertTrue(
            DebugTerminalService.execute(manager, "watch")
                .get(0)
                .contains("server thread"));
        assertFalse(manager.startExecutionProfile(2));
    }
}
