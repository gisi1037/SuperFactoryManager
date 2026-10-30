package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.program.ProgramDiskData;
import ca.teamdman.sfm.common.program.ProgramDocument;
import ca.teamdman.sfm.common.program.ProgramStateStore;
import ca.teamdman.sfm.common.program.project.ExecutionReference;
import ca.teamdman.sfm.common.program.project.PreparedExecution;
import ca.teamdman.sfm.common.program.project.ProjectResult;
import ca.teamdman.sfm.common.program.project.ProjectWorkspace;
import ca.teamdman.sfm.common.tileentity.LogLevel;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

/** Controlled worker completions exercise live fences without filesystem reads or asynchronous timing assumptions. */
class ReferenceActivationLifecycleTest {

    private static final String COUNTER = "STATE $count = 0 EVERY 1 TICKS DO SET $count = $count + 1 END";
    @TempDir
    Path temporary;

    @Test
    void insertionAndReloadQueueOnlyOnServerUpdateAndKeepLatestPauseIntent() throws Exception {
        try (Fixture f = new Fixture(temporary.resolve("cold"))) {
            assertEquals(0, f.manager.submitted);
            assertTrue(f.manager.isRunning(), "Insertion retains requested running intent");
            NBTTagCompound saved = new NBTTagCompound();
            f.manager.writeToNBT(saved);
            f.manager.readFromNBT(saved);
            assertEquals(0, f.manager.submitted);
            f.tick();
            assertEquals(1, f.manager.submitted);
            f.manager.setRunning(false);
            f.complete(0, COUNTER);
            assertTrue(f.manager.hasCompiledProgram());
            assertFalse(f.manager.isRunning());
            f.tick();
            assertEquals(
                0,
                f.manager.getProgramState()
                    .read(0)
                    .asLong());
            f.manager.setRunning(true);
            f.tick();
            assertEquals(
                1,
                f.manager.getProgramState()
                    .read(0)
                    .asLong());
            assertFalse(
                f.manager.getDisk()
                    .getTagCompound()
                    .hasKey(ItemDisk.NBT_PROGRAM));
            assertFalse(Files.exists(f.path), "The controlled loader must never touch storage");
        }
    }

    @Test
    void overflowRetriesArePacedWithoutPausingWhileMissingBuildsStillNeedExplicitRetry() throws Exception {
        try (Fixture f = new Fixture(temporary.resolve("overflow"))) {
            f.manager.admitted = false;
            f.tick();
            assertTrue(f.manager.isRunning());
            assertTrue(
                f.manager.getStatusMessage()
                    .contains("waiting for capacity"));
            for (int i = 0; i < 10; i++) {
                f.world.world.time++;
                f.tick();
            }
            assertEquals(1, f.manager.submitted);
            assertEquals(1, f.manager.lookups);
            assertEquals("", logText(f.manager));
            f.manager.setRunning(false);
            f.manager.admitted = true;
            f.world.world.time += 40;
            f.tick();
            assertEquals(2, f.manager.submitted);
            f.complete(0, COUNTER);
            assertTrue(f.manager.hasCompiledProgram());
            assertFalse(f.manager.isRunning(), "Latest pause intent wins even through overflow");
        }
        try (Fixture f = new Fixture(temporary.resolve("missing"))) {
            f.tick();
            f.fail(0, ProjectResult.Status.NOT_FOUND);
            assertFalse(f.manager.isRunning());
            assertTrue(logText(f.manager).contains("Published program unavailable"));
            for (int i = 0; i < 100; i++) {
                f.world.world.time++;
                f.tick();
            }
            assertEquals(1, f.manager.submitted);
            f.manager.setRunning(true);
            f.tick();
            f.complete(1, COUNTER);
            assertTrue(f.manager.hasCompiledProgram());
            assertTrue(f.manager.isRunning());
        }
    }

    @Test
    void physicalDiskDocumentReferenceStateWorldTileAndServiceFencesRejectStaleResults() throws Exception {
        List<Mutation> mutations = new ArrayList<>();
        mutations.add(
            f -> f.manager.setInventorySlotContents(
                0,
                f.manager.getDisk()
                    .copy()));
        mutations.add(
            f -> f.manager.getDisk()
                .getTagCompound()
                .getCompoundTag(ProgramDocument.NBT_KEY)
                .setLong("revision", 1));
        mutations.add(
            f -> f.manager.getDisk()
                .getTagCompound()
                .getCompoundTag(ProgramDiskData.NBT_REFERENCE)
                .setString("digest", hex('c')));
        mutations.add(
            f -> f.manager.getDisk()
                .getTagCompound()
                .setTag(ProgramStateStore.NBT_KEY, new NBTTagCompound()));
        mutations.add(f -> f.world.world.tiles.put(f.manager.getBlockPos(), new TileEntityManager()));
        mutations.add(f -> f.manager.setWorldObj(new AuditRegressionTest.Fixture().world));
        mutations.add(f -> {
            f.manager.xCoord++;
            f.world.world.tiles.put(f.manager.getBlockPos(), f.manager);
        });
        mutations.add(f -> f.manager.onChunkUnload());
        mutations.add(f -> f.manager.invalidate());
        mutations.add(f -> f.manager.current = false);
        mutations.add(f -> f.manager.workspace.close());
        mutations.add(f -> {
            NBTTagCompound saved = new NBTTagCompound();
            f.manager.writeToNBT(saved);
            f.manager.readFromNBT(saved);
        });
        int index = 0;
        for (Mutation mutation : mutations) try (Fixture f = new Fixture(temporary.resolve("fence" + index++))) {
            f.tick();
            assertEquals(1, f.manager.submitted);
            mutation.apply(f);
            f.complete(0, COUNTER);
            assertNull(f.manager.getCompiledProgram(), "Fence " + (index - 1));
        }
    }

    @Test
    void anOldCompletionCannotClearAReplacementRequestOrItsInstalledPlan() throws Exception {
        try (Fixture f = new Fixture(temporary.resolve("replacement"))) {
            f.tick();
            f.manager.setInventorySlotContents(
                0,
                f.manager.getDisk()
                    .copy());
            f.tick();
            assertEquals(2, f.manager.submitted);
            f.complete(1, COUNTER);
            Program installed = f.manager.getCompiledProgram();
            assertNotNull(installed);
            f.complete(0, COUNTER);
            assertSame(installed, f.manager.getCompiledProgram());
        }
    }

    @Test
    void lateRecoveryPreventsActivationAndRemainsOwnedUntilExplicitResolution() throws Exception {
        try (Fixture f = new Fixture(temporary.resolve("recovery"))) {
            f.tick();
            NBTTagCompound evidence = new NBTTagCompound();
            evidence.setString("state", "uncertain");
            f.manager.getDisk()
                .getTagCompound()
                .setTag("sfm:recovery_item", evidence);
            f.complete(0, COUNTER);
            assertFalse(f.manager.hasCompiledProgram());
            assertFalse(f.manager.isRunning());
            assertFalse(f.manager.canRequestRun());
            assertSame(
                evidence,
                f.manager.getDisk()
                    .getTagCompound()
                    .getTag("sfm:recovery_item"));
            for (int i = 0; i < 5; i++) f.tick();
            assertEquals(1, f.manager.submitted);
            f.manager.getDisk()
                .getTagCompound()
                .removeTag("sfm:recovery_item");
            f.manager.setRunning(true);
            f.tick();
            f.complete(1, COUNTER);
            assertTrue(f.manager.hasCompiledProgram());
        }
    }

    @Test
    void warmStatefulTicksNeverResubmitOrSerializeHeapAndCopiesOwnPrivatePlansAndState() throws Exception {
        try (Fixture first = new Fixture(temporary.resolve("first"));
            Fixture second = new Fixture(temporary.resolve("second"))) {
            first.tick();
            first.complete(0, COUNTER);
            NBTBase envelope = first.manager.getDisk()
                .getTagCompound()
                .getTag(ProgramStateStore.NBT_KEY);
            for (int i = 0; i < 100; i++) first.tick();
            assertSame(
                envelope,
                first.manager.getDisk()
                    .getTagCompound()
                    .getTag(ProgramStateStore.NBT_KEY));
            assertTrue(
                first.manager.getProgramState()
                    .isDirty());
            assertEquals(
                100,
                first.manager.getProgramState()
                    .read(0)
                    .asLong());
            assertEquals(1, first.manager.submitted);
            assertEquals(1, first.manager.lookups);
            first.manager.flushProgramState();
            second.manager.setInventorySlotContents(
                0,
                first.manager.getDisk()
                    .copy());
            second.tick();
            second.complete(0, COUNTER);
            assertNotSame(first.manager.getCompiledProgram(), second.manager.getCompiledProgram());
            assertNotSame(first.manager.getProgramState(), second.manager.getProgramState());
            assertEquals(
                100,
                second.manager.getProgramState()
                    .read(0)
                    .asLong());
            second.tick();
            assertEquals(
                101,
                second.manager.getProgramState()
                    .read(0)
                    .asLong());
            assertEquals(
                100,
                first.manager.getProgramState()
                    .read(0)
                    .asLong());
            assertFalse(Files.exists(first.path));
            assertFalse(Files.exists(second.path));
        }
    }

    @Test
    void statelessReferenceReplacementInvalidatesAndSchemaFailureKeepsCommittedState() throws Exception {
        try (Fixture f = new Fixture(temporary.resolve("stateless"))) {
            f.tick();
            f.complete(0, "EVERY 20 TICKS DO PRINT(1) END");
            assertNull(f.manager.getProgramState());
            f.manager.getDisk()
                .getTagCompound()
                .getCompoundTag(ProgramDiskData.NBT_REFERENCE)
                .setString("digest", hex('c'));
            assertNull(f.manager.getCompiledProgram());
            f.tick();
            assertEquals(2, f.manager.submitted);
            assertNull(f.manager.getCompiledProgram());
        }
        try (Fixture f = new Fixture(temporary.resolve("schema"))) {
            f.tick();
            f.complete(0, COUNTER);
            f.tick();
            f.manager.getDisk()
                .getTagCompound()
                .getCompoundTag(ProgramDiskData.NBT_REFERENCE)
                .setString("digest", hex('c'));
            f.tick();
            assertEquals(2, f.manager.submitted);
            NBTTagCompound committed = (NBTTagCompound) f.manager.getDisk()
                .getTagCompound()
                .getCompoundTag(ProgramStateStore.NBT_KEY)
                .copy();
            f.complete(1, "STATE $count = TRUE EVERY 1 TICKS DO PRINT($count) END");
            assertNull(f.manager.getCompiledProgram());
            assertFalse(f.manager.isRunning());
            assertEquals(
                committed,
                f.manager.getDisk()
                    .getTagCompound()
                    .getCompoundTag(ProgramStateStore.NBT_KEY));
        }
    }

    @Test
    void unloadFlushesHeapBeforeInvalidatingAndPersistedPauseSurvivesColdReload() throws Exception {
        try (Fixture f = new Fixture(temporary.resolve("unload"))) {
            f.tick();
            f.complete(0, COUNTER);
            f.tick();
            ProgramStateStore old = f.manager.getProgramState();
            f.manager.setRunning(false);
            f.manager.onChunkUnload();
            assertFalse(old.isDirty());
            NBTTagCompound saved = new NBTTagCompound();
            f.manager.writeToNBT(saved);
            f.manager.readFromNBT(saved);
            assertFalse(f.manager.isRunning());
            int requests = f.manager.submitted;
            f.tick();
            assertEquals(requests + 1, f.manager.submitted);
            f.complete(1, COUNTER);
            assertFalse(f.manager.isRunning());
            assertEquals(
                1,
                f.manager.getProgramState()
                    .read(0)
                    .asLong());
        }
    }

    @Test
    void wrongThreadCompletionCannotPublishAndClosedServiceDoesNotAutoRetry() throws Exception {
        try (Fixture f = new Fixture(temporary.resolve("thread"))) {
            f.tick();
            f.manager.serverThread = false;
            f.complete(0, COUNTER);
            assertNull(f.manager.getCompiledProgram());
            f.manager.serverThread = true;
            f.manager.workspace.close();
            for (int i = 0; i < 10; i++) f.tick();
            assertEquals(1, f.manager.submitted);
            assertFalse(f.manager.isRunning());
        }
    }

    @Test
    void legacyUnloadAndInvalidateRetainTheSamePlanWhileResettingGlobalsForObjectReuse() throws Exception {
        for (boolean invalidate : new boolean[] { false, true }) {
            AuditRegressionTest.Fixture world = new AuditRegressionTest.Fixture();
            TileEntityManager manager = RecoveryFixtures.manager(world);
            manager.saveCodeAndRecompile("LET $count = 0 EVERY 1 TICKS DO SET $count = $count + 1 END");
            manager.updateEntity();
            manager.updateEntity();
            Program original = manager.getCompiledProgram();
            assertEquals(
                2,
                manager.getProgramGlobals(original)
                    .read(0)
                    .asLong());
            if (invalidate) {
                manager.invalidate();
                manager.validate();
            } else manager.onChunkUnload();
            assertSame(original, manager.getCompiledProgram());
            assertFalse(
                manager.getProgramGlobals(original)
                    .isInitialized());
            manager.updateEntity();
            assertSame(original, manager.getCompiledProgram());
            assertEquals(
                1,
                manager.getProgramGlobals(original)
                    .read(0)
                    .asLong());
        }
    }

    @Test
    void referenceManualDebugAndTraceRetainGenericTimingButNeverReconstructPrivateStatements() throws Exception {
        for (LogLevel level : new LogLevel[] { LogLevel.INFO, LogLevel.DEBUG, LogLevel.TRACE })
            try (Fixture f = new Fixture(temporary.resolve("privacy" + level))) {
                f.tick();
                f.complete(0, "EVERY 1 TICKS DO INPUT FROM private_input OUTPUT TO private_output END");
                f.manager.runOnceWithLogLevel(level);
                String messages = logText(f.manager);
                assertTrue(messages.contains("Manual run completed"), messages);
                assertFalse(messages.contains("private_input"), messages);
                assertFalse(messages.contains("private_output"), messages);
                assertFalse(messages.contains("TRIGGERED FROM"), messages);
                assertFalse(messages.contains("EVERY 1 TICKS"), messages);
                assertFalse(messages.contains("INPUT FROM"), messages);
            }
        AuditRegressionTest.Fixture world = new AuditRegressionTest.Fixture();
        TileEntityManager legacy = RecoveryFixtures.manager(world);
        legacy.saveCodeAndRecompile("EVERY 1 TICKS DO INPUT FROM private_input OUTPUT TO private_output END");
        legacy.runOnceWithLogLevel(LogLevel.DEBUG);
        assertTrue(logText(legacy).contains("private_input"), "Legacy source diagnostics remain available");
    }

    @Test
    void referenceRuntimeFailureIsGenericAndAlwaysClearsThePrivateLogScope() throws Exception {
        try (Fixture f = new Fixture(temporary.resolve("privateFailure"))) {
            f.tick();
            f.complete(
                0,
                "FUNCTION private_runtime_name($value: INTEGER) RETURNS INTEGER DO RETURN 5 / $value END "
                    + "EVERY 1 TICKS DO LET $private_result = private_runtime_name(0) END");
            f.manager.runOnceWithLogLevel(LogLevel.TRACE);
            assertFalse(f.manager.isRunning());
            assertEquals("Published program runtime error; manager paused", f.manager.getStatusMessage());
            assertFalse(logText(f.manager).contains("private_runtime_name"));
            assertFalse(logText(f.manager).contains("private_result"));
            f.manager.setLogLevel(LogLevel.DEBUG);
            f.manager.log(LogLevel.DEBUG, "after_private_scope");
            assertTrue(logText(f.manager).contains("after_private_scope"));
        }
    }

    @Test
    void explicitPublishedPrintOutputSurvivesAutomaticSourceLogSuppression() throws Exception {
        try (Fixture f = new Fixture(temporary.resolve("privatePrint"))) {
            f.tick();
            f.complete(0, "EVERY 1 TICKS DO PRINT(\"intentional output\") END");
            f.manager.getDebugConsole()
                .setEnabled(true);
            f.manager.runOnceWithLogLevel(LogLevel.TRACE);
            assertTrue(
                f.manager.getDebugConsole()
                    .snapshot()
                    .toString()
                    .contains("intentional output"));
            assertFalse(logText(f.manager).contains("intentional output"));
            assertFalse(logText(f.manager).contains("PRINT("));
        }
    }

    @Test
    void loadedReferenceStateResetPublishesDefaultsPausesAndKeepsItsExactPrivatePlanAndDiskMetadata() throws Exception {
        try (Fixture f = new Fixture(temporary.resolve("resetLoaded"))) {
            f.tick();
            f.complete(
                0,
                "STATE $count = 7 LET $session = 0 EVERY 1 TICKS DO "
                    + "SET $count = $count + 1 SET $session = $session + 1 END");
            f.tick();
            f.tick();
            Program plan = f.manager.getCompiledProgram();
            ProgramStateStore previous = f.manager.getProgramState();
            assertEquals(
                9,
                previous.read(0)
                    .asLong());
            assertEquals(
                2,
                f.manager.getProgramGlobals(plan)
                    .read(0)
                    .asLong());
            f.manager.getDisk()
                .getTagCompound()
                .setString("test:label_history", "preserved");
            NBTTagCompound metadata = (NBTTagCompound) f.manager.getDisk()
                .getTagCompound()
                .copy();
            metadata.removeTag(ProgramStateStore.NBT_KEY);
            int submitted = f.manager.submitted;
            f.manager.resetProgramState();
            assertFalse(f.manager.isRunning());
            assertSame(plan, f.manager.getCompiledProgram());
            assertNotSame(previous, f.manager.getProgramState());
            assertEquals(
                7,
                f.manager.getProgramState()
                    .read(0)
                    .asLong());
            assertFalse(
                f.manager.getProgramGlobals(plan)
                    .isInitialized());
            assertFalse(
                f.manager.getProgramState()
                    .isDirty());
            NBTTagCompound after = (NBTTagCompound) f.manager.getDisk()
                .getTagCompound()
                .copy();
            after.removeTag(ProgramStateStore.NBT_KEY);
            assertEquals(metadata, after);
            assertEquals(submitted, f.manager.submitted);
            assertEquals("Persistent reference variables reset; program paused", f.manager.getStatusMessage());
            // A duplicated completion from the earlier activation cannot undo reset or pause.
            f.complete(0, "STATE $count = 999 EVERY 1 TICKS DO END");
            assertSame(plan, f.manager.getCompiledProgram());
            assertEquals(
                7,
                f.manager.getProgramState()
                    .read(0)
                    .asLong());
            assertFalse(f.manager.isRunning());
            f.manager.setRunning(true);
            f.tick();
            assertEquals(
                8,
                f.manager.getProgramState()
                    .read(0)
                    .asLong());
            assertEquals(
                1,
                f.manager.getProgramGlobals(plan)
                    .read(0)
                    .asLong());
        }
    }

    @Test
    void resetWhileLoadingRejectsTheResetButItsPauseSurvivesCompletion() throws Exception {
        try (Fixture f = new Fixture(temporary.resolve("resetPending"))) {
            f.tick();
            f.manager.resetProgramState();
            assertFalse(f.manager.isRunning());
            assertTrue(
                f.manager.getStatusMessage()
                    .contains("State not reset"));
            assertNull(f.manager.getProgramState());
            f.complete(0, "STATE $count = 7 EVERY 1 TICKS DO SET $count = $count + 1 END");
            assertFalse(f.manager.isRunning());
            assertEquals(
                7,
                f.manager.getProgramState()
                    .read(0)
                    .asLong());
            assertEquals(1, f.manager.submitted);
        }
    }

    @Test
    void recoveryStaleBindingAndClosedStorageRejectResetWithoutChangingStateOrReference() throws Exception {
        for (int condition = 0; condition < 3; condition++)
            try (Fixture f = new Fixture(temporary.resolve("resetRejected" + condition))) {
                f.tick();
                f.complete(0, COUNTER);
                f.tick();
                ProgramStateStore previous = f.manager.getProgramState();
                if (condition == 0) f.manager.getDisk()
                    .getTagCompound()
                    .setTag("sfm:recovery_item", new NBTTagCompound());
                if (condition == 1) f.manager.getDisk()
                    .getTagCompound()
                    .getCompoundTag(ProgramDiskData.NBT_REFERENCE)
                    .setString("digest", hex('c'));
                if (condition == 2) f.manager.workspace.close();
                NBTTagCompound before = (NBTTagCompound) f.manager.getDisk()
                    .getTagCompound()
                    .copy();
                f.manager.resetProgramState();
                assertFalse(f.manager.isRunning());
                assertSame(previous, f.manager.getProgramState());
                assertEquals(
                    1,
                    previous.read(0)
                        .asLong());
                assertEquals(
                    before,
                    f.manager.getDisk()
                        .getTagCompound());
                assertTrue(
                    f.manager.getStatusMessage()
                        .contains("State not reset"));
                assertEquals(1, f.manager.submitted);
            }
    }

    @Test
    void referenceResetValidationFailureRetainsAcceptedHeapAndSavedStateUntilAnExplicitRetry() throws Exception {
        try (Fixture f = new Fixture(temporary.resolve("resetBound"))) {
            f.tick();
            f.complete(0, COUNTER);
            f.tick();
            ProgramStateStore previous = f.manager.getProgramState();
            Program plan = f.manager.getCompiledProgram();
            NBTBase savedState = f.manager.getDisk()
                .getTagCompound()
                .getTag(ProgramStateStore.NBT_KEY);
            f.manager.getDisk()
                .getTagCompound()
                .setString("test:oversized", new String(new char[31000]).replace('\0', 'x'));
            f.manager.resetProgramState();
            assertFalse(f.manager.isRunning());
            assertSame(previous, f.manager.getProgramState());
            assertSame(plan, f.manager.getCompiledProgram());
            assertTrue(previous.isDirty());
            assertEquals(
                1,
                previous.read(0)
                    .asLong());
            assertSame(
                savedState,
                f.manager.getDisk()
                    .getTagCompound()
                    .getTag(ProgramStateStore.NBT_KEY));
            assertEquals(
                "State not reset: published STATE could not be reset safely; program paused",
                f.manager.getStatusMessage());
            f.manager.getDisk()
                .getTagCompound()
                .removeTag("test:oversized");
            f.manager.resetProgramState();
            assertEquals(
                0,
                f.manager.getProgramState()
                    .read(0)
                    .asLong());
            assertFalse(f.manager.isRunning());
            assertEquals(1, f.manager.submitted);
        }
    }

    @Test
    void explicitlyResettingCorruptReferenceStateUsesTheLoadedSchemaWithoutResolvingSource() throws Exception {
        try (Fixture f = new Fixture(temporary.resolve("resetCorrupt"))) {
            f.tick();
            f.complete(0, COUNTER);
            Program plan = f.manager.getCompiledProgram();
            f.manager.getDisk()
                .getTagCompound()
                .setString(ProgramStateStore.NBT_KEY, "corrupt saved state");
            f.manager.resetProgramState();
            assertFalse(f.manager.isRunning());
            assertSame(plan, f.manager.getCompiledProgram());
            assertEquals(
                0,
                f.manager.getProgramState()
                    .read(0)
                    .asLong());
            assertTrue(
                f.manager.getDisk()
                    .getTagCompound()
                    .hasKey(ProgramStateStore.NBT_KEY, 10));
            assertFalse(
                f.manager.getDisk()
                    .getTagCompound()
                    .hasKey(ItemDisk.NBT_PROGRAM));
            assertEquals(1, f.manager.submitted);
        }
    }

    @Test
    void legacyStateResetStillRecompilesItsEmbeddedSourceAndPauses() throws Exception {
        AuditRegressionTest.Fixture world = new AuditRegressionTest.Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(world);
        manager.saveCodeAndRecompile(COUNTER);
        manager.updateEntity();
        Program previous = manager.getCompiledProgram();
        manager.resetProgramState();
        assertFalse(manager.isRunning());
        assertNotSame(previous, manager.getCompiledProgram());
        assertEquals(
            0,
            manager.getProgramState()
                .read(0)
                .asLong());
        assertEquals(COUNTER, ItemDisk.getProgramString(manager.getDisk()));
    }

    private static String logText(TileEntityManager manager) {
        StringBuilder result = new StringBuilder();
        for (ca.teamdman.sfm.common.tileentity.LogEntry entry : manager.getLogs()) result.append(entry.getMessage())
            .append('\n');
        return result.toString();
    }

    private interface Mutation {

        void apply(Fixture fixture) throws Exception;
    }

    private static String hex(char value) {
        return new String(new char[64]).replace('\0', value);
    }

    private static ItemStack reference(ItemStack disk) {
        NBTTagCompound root = new NBTTagCompound();
        NBTTagCompound reference = new NBTTagCompound();
        reference.setInteger("format", 1);
        reference.setString("grant", hex('a'));
        reference.setString("digest", hex('b'));
        root.setTag(ProgramDiskData.NBT_REFERENCE, reference);
        NBTTagCompound document = new NBTTagCompound();
        document.setInteger("format", 1);
        document.setLong("idMost", 1);
        document.setLong("idLeast", 2);
        document.setLong("revision", 0);
        root.setTag(ProgramDocument.NBT_KEY, document);
        disk.setTagCompound(root);
        return disk;
    }

    @SuppressWarnings("unchecked")
    private static ProjectResult<PreparedExecution> outcome(PreparedExecution value, ProjectResult.Status failure)
        throws Exception {
        Method factory = ProjectResult.class.getDeclaredMethod(
            failure == null ? "success" : "failure",
            failure == null ? new Class<?>[] { Object.class }
                : new Class<?>[] { ProjectResult.Status.class, String.class });
        factory.setAccessible(true);
        return (ProjectResult<PreparedExecution>) (failure == null ? factory.invoke(null, value)
            : factory.invoke(null, failure, "PRIVATE_SOURCE_MUST_NOT_ENTER_CLIENT_STATUS"));
    }

    private static final class ControlledManager extends TileEntityManager {

        static {
            net.minecraft.tileentity.TileEntity.addMapping(ControlledManager.class, "sfm.reference.activation.test");
        }
        ProjectWorkspace workspace;
        boolean current = true, admitted = true, serverThread = true;
        int submitted, lookups;
        final List<Consumer<ProjectResult<PreparedExecution>>> callbacks = new ArrayList<>();
        final List<ExecutionReference> references = new ArrayList<>();

        @Override
        protected ProjectWorkspace executionWorkspace(World world) {
            lookups++;
            return workspace;
        }

        @Override
        protected boolean executionWorkspaceCurrent(ProjectWorkspace expected, World world) {
            return current && expected == workspace && !expected.isClosed() && world == getWorldObj();
        }

        @Override
        protected boolean referenceServerThread() {
            return serverThread;
        }

        @Override
        protected boolean prepareReferenceExecution(ProjectWorkspace store, ExecutionReference reference,
            Consumer<ProjectResult<PreparedExecution>> callback) {
            submitted++;
            if (!admitted) return false;
            callbacks.add(callback);
            references.add(reference);
            return true;
        }
    }

    private static final class Fixture implements AutoCloseable {

        final AuditRegressionTest.Fixture world = new AuditRegressionTest.Fixture();
        final ControlledManager manager = new ControlledManager();
        final Path path;

        Fixture(Path path) throws Exception {
            this.path = path;
            TileEntityManager original = RecoveryFixtures.manager(world);
            manager.setWorldObj(world.world);
            manager.xCoord = original.xCoord;
            manager.yCoord = original.yCoord;
            manager.zCoord = original.zCoord;
            manager.workspace = new ProjectWorkspace(path);
            world.world.tiles.put(manager.getBlockPos(), manager);
            manager.setInventorySlotContents(
                0,
                reference(
                    original.getDisk()
                        .copy()));
        }

        void tick() {
            manager.updateEntity();
        }

        void complete(int index, String source) throws Exception {
            Program program = new ProgramBuilder(source).build()
                .getProgram();
            assertNotNull(program);
            Constructor<PreparedExecution> constructor = PreparedExecution.class
                .getDeclaredConstructor(ExecutionReference.class, Program.class, String.class);
            constructor.setAccessible(true);
            PreparedExecution prepared = constructor.newInstance(manager.references.get(index), program, source);
            manager.callbacks.get(index)
                .accept(outcome(prepared, null));
        }

        void fail(int index, ProjectResult.Status failure) throws Exception {
            manager.callbacks.get(index)
                .accept(outcome(null, failure));
            assertFalse(
                manager.getStatusMessage()
                    .contains("PRIVATE_SOURCE"));
        }

        @Override
        public void close() {
            manager.workspace.close();
        }
    }
}
