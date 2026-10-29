package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.RegistrySimple;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ExpressionLimits;
import ca.teamdman.sfml.data.ProgramGlobals;
import ca.teamdman.sfml.data.ProgramStateSchema;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class ProgramGlobalLetTest {

    private static final String COUNTER = "LET $count = 0 EVERY 1 TICKS DO SET $count = $count + 1 END";

    @Test
    void triggersShareGlobalsWhileTheirLocalsResetAcrossFreshManagerContexts() throws Exception {
        Program program = program(
            "LET $count = 0 EVERY 20 TICKS DO LET $n = 1 SET $count = $count + $n SET $n = 99 END "
                + "EVERY 20 TICKS DO LET $n = 2 SET $count = $count + $n END");
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        ProgramContext first = context(fixture, manager, 20);
        program.tick(first);
        ProgramGlobals values = first.getProgramGlobals(program);
        assertTrue(values.isInitialized());
        assertEquals(
            3,
            values.read(0)
                .asLong());
        ProgramContext second = context(fixture, manager, 40);
        program.tick(second);
        assertSame(values, second.getProgramGlobals(program));
        assertEquals(
            6,
            values.read(0)
                .asLong());
    }

    @Test
    void managersSharingOneCompiledPlanHaveIndependentGlobals() throws Exception {
        Program shared = program(COUNTER);
        AuditRegressionTest.Fixture first = new AuditRegressionTest.Fixture();
        AuditRegressionTest.Fixture second = new AuditRegressionTest.Fixture();
        TileEntityManager firstManager = RecoveryFixtures.manager(first),
            secondManager = RecoveryFixtures.manager(second);
        ProgramContext a = context(first, firstManager, 1), b = context(second, secondManager, 1);
        shared.tick(a);
        shared.tick(context(first, firstManager, 2));
        shared.tick(b);
        assertNotSame(a.getProgramGlobals(shared), b.getProgramGlobals(shared));
        assertEquals(
            2,
            a.getProgramGlobals(shared)
                .read(0)
                .asLong());
        assertEquals(
            1,
            b.getProgramGlobals(shared)
                .read(0)
                .asLong());
    }

    @Test
    void managerlessContextsOwnTheirFallbackStoreAndDoNotMutateThePlan() {
        Program shared = program(COUNTER);
        ProgramContext first = new ProgramContext(null, null, null, null, 1, false);
        ProgramContext second = new ProgramContext(null, null, null, null, 1, false);
        shared.tick(first);
        shared.tick(first);
        shared.tick(second);
        assertEquals(
            2,
            first.getProgramGlobals(shared)
                .read(0)
                .asLong());
        assertEquals(
            1,
            second.getProgramGlobals(shared)
                .read(0)
                .asLong());
        assertNotSame(first.getProgramGlobals(shared), second.getProgramGlobals(shared));
    }

    @Test
    void observationsInitializeAtFirstExecutionEvenWhenNoTriggerIsDueAndRunOnlyOnce() throws Exception {
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        fixture.stock(0, 4);
        Program program = program(
            "LET $initial = LABEL(\"source\").count(ITEM(\"*\")) LET $runs = 0 "
                + "EVERY 20 TICKS DO SET $runs = $runs + 1 END");
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        fixture.stock(0, 7);
        ProgramContext first = context(fixture, manager, 1);
        assertFalse(
            first.getProgramGlobals(program)
                .isInitialized());
        program.tick(first);
        ProgramGlobals values = first.getProgramGlobals(program);
        assertEquals(
            7,
            values.read(0)
                .asLong(),
            "Compilation must not observe inventory contents");
        assertEquals(
            0,
            values.read(1)
                .asLong(),
            "No trigger is due at tick one");
        assertTrue(
            first.getInvocationQueryBudget()
                .getProviderCalls() > 0);
        fixture.stock(0, 13);
        ProgramContext next = context(fixture, manager, 2);
        program.tick(next);
        assertEquals(
            7,
            values.read(0)
                .asLong());
        assertEquals(
            0,
            next.getInvocationQueryBudget()
                .getProviderCalls());
        program.tick(context(fixture, manager, 20));
        assertEquals(
            1,
            values.read(1)
                .asLong());
        assertEquals(
            7,
            values.read(0)
                .asLong());
    }

    @Test
    void failedInitializationPublishesNothingAndRetriesEveryInitializer() throws Exception {
        Program program = program(
            "LET $observed = LABEL(\"source\").count(ITEM(\"*\")) LET $quotient = 12 / $denominator "
                + "STATE $denominator = 0 STATE $triggerRuns = 0 "
                + "EVERY 1 TICKS DO SET $triggerRuns = $triggerRuns + 1 END");
        MemoryState state = new MemoryState(program.getStateSchema());
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        fixture.stock(0, 4);
        ProgramContext failing = context(fixture, manager, 1);
        failing.setStateAccess(state);
        assertThrows(IllegalArgumentException.class, () -> program.tick(failing));
        ProgramGlobals untouched = failing.getProgramGlobals(program);
        assertFalse(untouched.isInitialized());
        assertThrows(IllegalArgumentException.class, () -> untouched.read(0));
        assertThrows(IllegalArgumentException.class, () -> untouched.read(1));
        assertEquals(
            0,
            state.read(1)
                .asLong());
        fixture.stock(0, 9);
        state.write(0, DataValue.integer(3));
        ProgramContext retry = context(fixture, manager, 2);
        retry.setStateAccess(state);
        program.tick(retry);
        ProgramGlobals completed = retry.getProgramGlobals(program);
        assertTrue(completed.isInitialized());
        assertNotSame(untouched, completed);
        assertEquals(
            9,
            completed.read(0)
                .asLong(),
            "The first observation must run again after failure");
        assertEquals(
            4,
            completed.read(1)
                .asLong());
        assertEquals(
            1,
            state.read(1)
                .asLong());
    }

    @Test
    void initializationUsesTheInvocationQueryBudgetBeforePublishingAnyValues() throws Exception {
        Program program = program(
            "LET $first = 5 LET $count = LABEL(\"source\").count(ITEM(\"*\")) EVERY 20 TICKS DO END");
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        ProgramContext context = context(fixture, null, 1);
        context.getInvocationQueryBudget()
            .chargeProviders(QueryBudget.MAX_PROVIDER_CALLS);
        assertThrows(IllegalArgumentException.class, () -> program.tick(context));
        assertFalse(
            context.getProgramGlobals(program)
                .isInitialized());
        assertThrows(
            IllegalArgumentException.class,
            () -> context.getProgramGlobals(program)
                .read(0));
    }

    @Test
    void retainedExactItemKeyDetectsOnlyNbtChangesAcrossFreshContextsWithAllEqualityOperators() throws Exception {
        Item item = new Item();
        Field registry = RegistrySimple.class.getDeclaredField("registryObjects");
        registry.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> names = (Map<String, Object>) registry.get(Item.itemRegistry);
        names.put("sfm_global_nbt:item", item);
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        ItemStack stack = new ItemStack(item, 1, 4);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("infusionProgress", 1);
        stack.setTagCompound(nbt);
        fixture.source.setInventorySlotContents(0, stack);
        Program program = program(
            "LET $baseline = LABEL(\"source\").contents()[0].item LET $changed = FALSE LET $same = FALSE LET $equal = FALSE "
                + "EVERY 20 TICKS DO LET $current = LABEL(\"source\").contents()[0].item "
                + "SET $changed = $current != $baseline SET $same = $current = $baseline "
                + "SET $equal = $current == $baseline END");
        ProgramContext initial = context(fixture, manager, 20);
        program.tick(initial);
        ProgramGlobals globals = initial.getProgramGlobals(program);
        DataValue retained = globals.read(0);
        assertFalse(
            globals.read(1)
                .asBoolean());
        assertTrue(
            globals.read(2)
                .asBoolean());
        assertTrue(
            globals.read(3)
                .asBoolean());
        stack.stackSize = 19;
        program.tick(context(fixture, manager, 40));
        assertFalse(
            globals.read(1)
                .asBoolean(),
            "Stack count is not part of exact identity");
        nbt.setInteger("infusionProgress", 2);
        program.tick(context(fixture, manager, 60));
        assertSame(retained, globals.read(0));
        assertEquals(
            "sfm_global_nbt:item",
            retained.asItemIdentity()
                .getRegistryId());
        assertEquals(
            4,
            retained.asItemIdentity()
                .getMetadata());
        assertTrue(
            globals.read(1)
                .asBoolean(),
            "Registry ID and metadata are unchanged; only NBT differs");
        assertFalse(
            globals.read(2)
                .asBoolean());
        assertFalse(
            globals.read(3)
                .asBoolean());
        nbt.setInteger("infusionProgress", 1);
        program.tick(context(fixture, manager, 80));
        assertFalse(
            globals.read(1)
                .asBoolean());
        assertTrue(
            globals.read(2)
                .asBoolean());
        assertTrue(
            globals.read(3)
                .asBoolean());
    }

    @Test
    void earlierGlobalsStateAndExplicitPureFunctionParametersComposeAndRoundTrip() {
        Program original = program(
            "LET $base = $seed + 1 LET $rows = [{amount: twice($base)}, {amount: 3}] STATE $seed = 4 "
                + "FUNCTION twice($base: INTEGER) RETURNS INTEGER DO RETURN $base * 2 END "
                + "EVERY 1 TICKS DO LET $copy = $rows SET $rows[0].amount = $rows[0].amount + $base "
                + "SET $seed = $copy[0].amount END");
        Program rebuilt = program(original.toString());
        assertEquals(
            2,
            rebuilt.getGlobalDeclarations()
                .size());
        assertThrows(
            UnsupportedOperationException.class,
            () -> rebuilt.getGlobalDeclarations()
                .clear());
        MemoryState state = new MemoryState(rebuilt.getStateSchema());
        ProgramContext context = new ProgramContext(null, null, null, null, 1, false);
        context.setStateAccess(state);
        rebuilt.tick(context);
        assertEquals(
            5,
            context.getProgramGlobals(rebuilt)
                .read(0)
                .asLong());
        assertEquals(
            15,
            context.getProgramGlobals(rebuilt)
                .read(1)
                .get(0)
                .get("amount")
                .asLong());
        assertEquals(
            10,
            state.read(0)
                .asLong(),
            "Local snapshots retain the original immutable rows");
    }

    @Test
    void declarationsRejectForwardSelfDuplicateShadowAndTypeErrorsAndFunctionsCannotCaptureGlobals() {
        String[] invalid = { "LET $a = $a EVERY 1 TICKS DO END", "LET $a = $b LET $b = 1 EVERY 1 TICKS DO END",
            "LET $a = 1 LET $a = 2 EVERY 1 TICKS DO END", "LET $a = 1 STATE $a = 2 EVERY 1 TICKS DO END",
            "LET $a = 1 EVERY 1 TICKS DO LET $a = 2 END", "LET $a = 1 EVERY 1 TICKS DO IF TRUE THEN LET $a = 2 END END",
            "LET $a = 1 EVERY 1 TICKS DO FOR $a = 1 TO 2 DO END END",
            "LET $a = [1, 2] EVERY 1 TICKS DO SET $a = [1] END",
            "LET $a = [{amount: 1}] EVERY 1 TICKS DO SET $a[0].amount = TRUE END",
            "LET $a = 1 FUNCTION capture() RETURNS INTEGER DO RETURN $a END EVERY 1 TICKS DO END",
            "LET $a = 1 FUNCTION mutate() RETURNS INTEGER DO SET $a = 2 RETURN 1 END EVERY 1 TICKS DO END",
            "LET $a = PRINT(1) EVERY 1 TICKS DO END",
            "LET $a = effect() FUNCTION effect() RETURNS INTEGER DO PRINT(1) RETURN 1 END EVERY 1 TICKS DO END",
            "LET $a = effect() FUNCTION effect() RETURNS INTEGER DO INPUT FROM source RETURN 1 END EVERY 1 TICKS DO END" };
        for (String source : invalid) assertFalse(
            new ProgramBuilder(source).build()
                .isSuccess(),
            source);
        StringBuilder tooMany = new StringBuilder();
        for (int i = 0; i <= ProgramGlobals.MAX_GLOBALS; i++) tooMany.append("LET $g")
            .append(i)
            .append(" = 0 ");
        tooMany.append("EVERY 1 TICKS DO END");
        assertFalse(
            new ProgramBuilder(tooMany.toString()).build()
                .isSuccess());
    }

    @Test
    void runtimeNestedAssignmentFailurePreservesThePreviousGlobalAndEarlierAssignments() {
        Program program = program(
            "LET $counter = 0 LET $rows = [{amount: 4}] EVERY 1 TICKS DO SET $counter = 7 "
                + "SET $rows[1].amount = 99 SET $counter = 9 END");
        ProgramContext context = new ProgramContext(null, null, null, null, 1, false);
        assertThrows(IllegalArgumentException.class, () -> program.tick(context));
        assertEquals(
            7,
            context.getProgramGlobals(program)
                .read(0)
                .asLong());
        assertEquals(
            4,
            context.getProgramGlobals(program)
                .read(1)
                .get(0)
                .get("amount")
                .asLong());
    }

    @Test
    void pauseAndFormattingOnlySavePreserveGlobalsButRecompileAndSemanticEditsResetThem() throws Exception {
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        manager.saveCodeAndRecompile(COUNTER);
        assertTrue(manager.hasCompiledProgram(), manager.getStatusMessage());
        Program original = manager.getCompiledProgram();
        manager.updateEntity();
        manager.updateEntity();
        ProgramGlobals values = manager.getProgramGlobals(original);
        assertEquals(
            2,
            values.read(0)
                .asLong());
        manager.setRunning(false);
        manager.updateEntity();
        assertSame(values, manager.getProgramGlobals(original));
        assertEquals(
            2,
            values.read(0)
                .asLong());
        manager.setRunning(true);
        manager.updateEntity();
        assertEquals(
            3,
            values.read(0)
                .asLong());
        String formatted = "-- formatting only\n" + COUNTER;
        assertNull(manager.trySaveProgram(formatted));
        assertSame(original, manager.getCompiledProgram());
        assertSame(values, manager.getProgramGlobals(original));
        manager.updateEntity();
        assertEquals(
            4,
            values.read(0)
                .asLong());
        manager.recompile(formatted);
        Program recompiled = manager.getCompiledProgram();
        assertNotSame(original, recompiled);
        assertFalse(
            manager.getProgramGlobals(recompiled)
                .isInitialized());
        manager.updateEntity();
        assertEquals(
            1,
            manager.getProgramGlobals(recompiled)
                .read(0)
                .asLong());
        assertNull(manager.trySaveProgram(COUNTER.replace("+ 1", "+ 2")));
        Program edited = manager.getCompiledProgram();
        assertFalse(
            manager.getProgramGlobals(edited)
                .isInitialized());
        manager.updateEntity();
        assertEquals(
            2,
            manager.getProgramGlobals(edited)
                .read(0)
                .asLong());
    }

    @Test
    void reloadStateResetAndDiskRemovalDiscardGlobalsInsteadOfPersistingThem() throws Exception {
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        manager.saveCodeAndRecompile(COUNTER);
        manager.updateEntity();
        manager.updateEntity();
        NBTTagCompound saved = new NBTTagCompound();
        manager.writeToNBT(saved);
        TileEntityManager loaded = new TileEntityManager();
        loaded.setWorldObj(fixture.world);
        loaded.readFromNBT(saved);
        Program loadedProgram = loaded.getCompiledProgram();
        assertNotNull(loadedProgram, loaded.getStatusMessage());
        assertFalse(
            loaded.getProgramGlobals(loadedProgram)
                .isInitialized());
        loaded.updateEntity();
        assertEquals(
            1,
            loaded.getProgramGlobals(loadedProgram)
                .read(0)
                .asLong());
        loaded.resetProgramState();
        Program reset = loaded.getCompiledProgram();
        assertFalse(
            loaded.getProgramGlobals(reset)
                .isInitialized());
        loaded.setRunning(true);
        loaded.updateEntity();
        assertEquals(
            1,
            loaded.getProgramGlobals(reset)
                .read(0)
                .asLong());
        ItemStack removed = loaded.decrStackSize(0, 1);
        assertNotNull(removed);
        loaded.setInventorySlotContents(0, removed);
        Program replaced = loaded.getCompiledProgram();
        assertFalse(
            loaded.getProgramGlobals(replaced)
                .isInitialized());
        loaded.updateEntity();
        assertEquals(
            1,
            loaded.getProgramGlobals(replaced)
                .read(0)
                .asLong());
    }

    @Test
    void chunkUnloadDiscardsGlobalsEvenWhenTheSameManagerObjectIsReused() throws Exception {
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        manager.saveCodeAndRecompile(COUNTER);
        manager.updateEntity();
        manager.updateEntity();
        Program program = manager.getCompiledProgram();
        ProgramGlobals previous = manager.getProgramGlobals(program);
        assertEquals(
            2,
            previous.read(0)
                .asLong());
        manager.onChunkUnload();
        assertNotSame(previous, manager.getProgramGlobals(program));
        assertFalse(
            manager.getProgramGlobals(program)
                .isInitialized());
        manager.updateEntity();
        assertEquals(
            1,
            manager.getProgramGlobals(program)
                .read(0)
                .asLong());
    }

    @Test
    void retainedCharacterAndCellLimitFailuresDoNotPublishOrCorruptStoreCounters() {
        Program strings = program(
            "LET $a = \"\" LET $b = \"\" LET $c = \"\" LET $d = \"\" LET $e = \"\" EVERY 1 TICKS DO END");
        ProgramGlobals values = new ProgramGlobals(strings.getGlobalDeclarations());
        char[] chars = new char[ExpressionLimits.MAX_STRING_LENGTH];
        Arrays.fill(chars, 'x');
        DataValue full = DataValue.string(new String(chars));
        for (int i = 0; i < 4; i++) values.write(i, full);
        DataValue empty = DataValue.string("");
        values.write(4, empty);
        values.finishInitialization();
        assertThrows(IllegalArgumentException.class, () -> values.write(4, DataValue.string("x")));
        assertSame(empty, values.read(4));
        assertThrows(IllegalArgumentException.class, () -> values.write(4, DataValue.integer(0)));
        assertSame(empty, values.read(4));
        values.write(0, empty);
        values.write(4, DataValue.string("x"));
        assertEquals(
            "x",
            values.read(4)
                .asString());

        StringBuilder source = new StringBuilder("LET $a = [0");
        for (int i = 1; i < 256; i++) source.append(", 0");
        source.append("] EVERY 1 TICKS DO END");
        Program arrays = program(source.toString());
        ProgramGlobals cells = new ProgramGlobals(
            Collections.nCopies(
                16,
                arrays.getGlobalDeclarations()
                    .get(0)));
        DataValue array = DataValue.array(Collections.nCopies(256, DataValue.integer(0)));
        for (int i = 0; i < 15; i++) cells.write(i, array);
        assertThrows(IllegalArgumentException.class, () -> cells.write(15, array));
        assertThrows(IllegalArgumentException.class, () -> cells.read(15));
        assertSame(array, cells.read(0));
    }

    @Test
    void existingInfusionExamplesCompileWithoutGlobalDeclarations() throws Exception {
        for (String name : Arrays.asList("infusion-current-methods.sfml", "infusion-current-legacy.sfml")) {
            String source = new String(
                Files.readAllBytes(Paths.get("examples", "logistics", name)),
                StandardCharsets.UTF_8);
            Program example = program(source);
            assertTrue(
                example.getGlobalDeclarations()
                    .isEmpty(),
                name);
        }
    }

    @Test
    void automaticInfusionExampleRetainsItsGlobalBaselineAndRoundTrips() throws Exception {
        String source = new String(
            Files.readAllBytes(Paths.get("examples", "logistics", "infusion-auto-nbt.sfml")),
            StandardCharsets.UTF_8);
        Program example = program(source);
        assertFalse(
            example.getGlobalDeclarations()
                .isEmpty());
        assertEquals(
            example.getGlobalDeclarations()
                .size(),
            program(example.toString()).getGlobalDeclarations()
                .size());
    }

    private static Program program(String source) {
        ProgramBuildResult result = new ProgramBuilder(source).build();
        assertTrue(result.isSuccess(), result.getErrorMessage());
        return result.getProgram();
    }

    private static ProgramContext context(AuditRegressionTest.Fixture fixture, TileEntityManager manager, long tick) {
        return new ProgramContext(
            fixture.world,
            manager,
            new CableNetwork(fixture.world, AuditRegressionTest.MANAGER),
            fixture.labels,
            tick,
            false);
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
            values[slot] = value;
        }
    }
}
