package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ProgramExecution;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

/** Run actual programs at explicit world times; no sleeps, worker world access or timer-dependent assertions. */
class SfmlWaitTest {

    @Test
    void resumesOnTheDeadlineRegardlessOfTheOriginalInterval() throws Exception {
        Run run = new Run(
            "STATE $started = 0 STATE $done = 0 EVERY 10 TICKS DO "
                + "$started = $started + 1 LET $local = 7 wait(3) $done = $done + $local END");
        run.tick(10);
        assertEquals(1, run.value(0));
        assertEquals(0, run.value(1));
        run.tick(11);
        run.tick(12);
        assertEquals(1, run.value(0));
        assertEquals(0, run.value(1));
        run.tick(13);
        assertEquals(1, run.value(0));
        assertEquals(7, run.value(1));
        assertEquals(
            0,
            run.manager.getProgramExecution(run.program)
                .sleepingCount());
    }

    @Test
    void waitingTriggerDoesNotQueueDuplicatesAndOtherTriggersContinue() throws Exception {
        Run run = new Run(
            "STATE $started = 0 STATE $done = 0 STATE $other = 0 "
                + "EVERY 2 TICKS DO $started = $started + 1 wait(5) $done = $done + 1 END "
                + "EVERY 1 TICKS DO $other = $other + 1 END");
        for (int t = 2; t <= 7; t++) run.tick(t);
        assertEquals(1, run.value(0));
        assertEquals(1, run.value(1));
        assertEquals(6, run.value(2));
        run.tick(8);
        assertEquals(2, run.value(0));
        assertEquals(1, run.value(1));
    }

    @Test
    void waitWorksInsideNestedFunctionsAndRetainsCallerOperandsExactlyOnce() throws Exception {
        Run run = new Run(
            "STATE $answer = 0 "
                + "FUNCTION inner($n: INTEGER) RETURNS INTEGER DO LET $local = $n + 2 wait(2) RETURN $local END "
                + "FUNCTION outer($n: INTEGER) RETURNS INTEGER DO RETURN inner($n) + inner(3) END "
                + "EVERY 10 TICKS DO $answer = label(\"source\").count(item(\"*\")) "
                + "+ outer(label(\"source\").count(item(\"*\"))) END");
        run.fixture.stock(0, 5);
        run.tick(10);
        run.fixture.stock(0, 20);
        run.tick(12);
        assertEquals(0, run.value(0));
        run.tick(14);
        assertEquals(17, run.value(0)); // 5 + (5+2) + (3+2), never 20 from the later live inventory.
    }

    @Test
    void waitingWhileConditionIsRecheckedAfterEachCompletedIteration() throws Exception {
        Run run = new Run(
            "STATE $ready = false " + "FUNCTION ready($src: LABEL_REF) RETURNS BOOLEAN DO "
                + "WHILE (!$src.empty()) DO wait 2 END RETURN true END "
                + "EVERY 10 TICKS DO $ready = ready(label(\"source\")) END");
        run.fixture.stock(0, 1);
        run.tick(10);
        run.tick(12);
        assertFalse(run.state.values[0].asBoolean());
        run.fixture.source.setInventorySlotContents(0, null);
        run.tick(14);
        assertTrue(run.state.values[0].asBoolean());
    }

    @Test
    void countedAndArrayLoopsKeepTheirIterationAndSnapshotAcrossWaits() throws Exception {
        Run run = new Run(
            "STATE $answer = 0 " + "FUNCTION sum() RETURNS INTEGER DO LET $sum = 0 "
                + "FOR $i = 1 TO 2 DO FOR $n IN [3, 4] DO wait(1) $sum = $sum + $i * $n END END RETURN $sum END "
                + "EVERY 10 TICKS DO $answer = sum() END");
        run.tick(10);
        run.tick(11);
        run.tick(12);
        run.tick(13);
        assertEquals(0, run.value(0));
        run.tick(14);
        assertEquals(21, run.value(0));
    }

    @Test
    void triggerLoopsAndChosenIfBranchResumeWithoutReenteringEarlierStatements() throws Exception {
        Run run = new Run(
            "STATE $answer = 0 STATE $flag = true EVERY 10 TICKS DO "
                + "IF ($flag) THEN FOR $i = 1 TO 2 DO $answer = $answer + $i wait(1) END ELSE $answer = 1000 END END "
                + "EVERY 1 TICKS DO $flag = false END");
        run.tick(10);
        assertEquals(1, run.value(0));
        run.tick(11);
        assertEquals(3, run.value(0));
        run.tick(12);
        assertEquals(3, run.value(0));
    }

    @Test
    void zeroIsANoopAndNegativeOrOverflowingDurationsFault() throws Exception {
        Run zero = new Run("STATE $n = 0 EVERY 10 TICKS DO wait(0) $n = 1 END");
        zero.tick(10);
        assertEquals(1, zero.value(0));
        Run negative = new Run("EVERY 10 TICKS DO wait(-1) END");
        assertThrows(IllegalArgumentException.class, () -> negative.tick(10));
        Run overflow = new Run("EVERY 10 TICKS DO wait(9223372036854775807) END");
        assertThrows(ArithmeticException.class, () -> overflow.tick(10));
        assertEquals(
            0,
            negative.manager.getProgramExecution(negative.program)
                .sleepingCount());
    }

    @Test
    void inputCountersSurviveButProvidersAndRetentionAreRefreshed() throws Exception {
        Run run = new Run(
            "EVERY 10 TICKS DO INPUT 5 RETAIN 2 FROM \"source\" "
                + "OUTPUT 3 TO \"dest\" wait(2) OUTPUT 5 TO \"dest\" END");
        run.fixture.stock(0, 10);
        run.tick(10);
        assertEquals(3, count(run.fixture.dest));
        InventoryTile replacement = new InventoryTile(3);
        replacement.setInventorySlotContents(0, new ItemStack(ITEM, 10));
        run.fixture.world.tiles.put(SRC, replacement);
        run.tick(12);
        assertEquals(5, count(run.fixture.dest)); // Old INPUT allowance is 5, not a fresh 5 after waiting.
        assertEquals(8, count(replacement));
        assertEquals(7, count(run.fixture.source)); // The removed provider was never used again.
    }

    @Test
    void retentionIsRecomputedWhenStockChangesDuringWait() throws Exception {
        Run run = new Run(
            "EVERY 10 TICKS DO INPUT RETAIN 2 FROM \"source\" "
                + "OUTPUT 3 TO \"dest\" wait(2) OUTPUT TO \"dest\" END");
        run.fixture.stock(0, 10);
        run.tick(10);
        run.fixture.stock(0, 5);
        run.tick(12);
        assertEquals(6, count(run.fixture.dest));
        assertEquals(2, count(run.fixture.source));
    }

    @Test
    void globalInitializersCannotSuspendAndOrdinaryFunctionsStillWork() {
        for (String code : new String[] {
            "FUNCTION later() RETURNS INTEGER DO wait(1) RETURN 2 END EVERY 1 TICKS DO END",
            "LET $global = later() FUNCTION later() RETURNS INTEGER DO wait(1) RETURN 2 END EVERY 1 TICKS DO END" }) {
            ProgramBuildResult result = new ProgramBuilder(code).build();
            if (code.startsWith("LET")) assertFalse(result.isSuccess());
            else assertTrue(result.isSuccess(), result.getErrorMessage());
        }
        assertFalse(
            new ProgramBuilder("EVERY 1 TICKS DO wait(true) END").build()
                .isSuccess());
        assertFalse(
            new ProgramBuilder("EVERY 1 TICKS DO LET $n = wait(1) END").build()
                .isSuccess());
    }

    @Test
    void diskReplacementAndChunkUnloadCancelSleepingInvocations() throws Exception {
        for (boolean unload : new boolean[] { false, true }) {
            Run run = new Run("STATE $n = 0 EVERY 10 TICKS DO wait(2) $n = 1 END");
            run.tick(10);
            if (unload) run.manager.onChunkUnload();
            else run.manager.setInventorySlotContents(
                0,
                run.manager.getDisk()
                    .copy());
            run.tick(12);
            assertEquals(0, run.value(0));
        }
    }

    @Test
    void programsAndManagersNeverShareSleepingRuntimeState() throws Exception {
        Run first = new Run(
            "STATE $n = 0 EVERY 10 TICKS DO LET $v = label(\"source\").count(item(\"*\")) wait(2) $n = $v END");
        Run second = new Run(first.program);
        first.fixture.stock(0, 1);
        second.fixture.stock(0, 9);
        first.tick(10);
        second.tick(10);
        first.tick(12);
        second.tick(12);
        assertEquals(1, first.value(0));
        assertEquals(9, second.value(0));
    }

    @Test
    void waitsRoundTripThroughTheCompiler() {
        ProgramBuildResult built = new ProgramBuilder(
            "FUNCTION later() RETURNS BOOLEAN DO wait(3) RETURN true END "
                + "EVERY 10 TICKS DO IF (later()) THEN wait 2 END END").build();
        assertTrue(built.isSuccess(), built.getErrorMessage());
        ProgramBuildResult rebuilt = new ProgramBuilder(
            built.getProgram()
                .toString()).build();
        assertTrue(rebuilt.isSuccess(), rebuilt.getErrorMessage());
    }

    @Test
    void legacyHasLeftOperandRemainsFrozenWhileTheRightFunctionWaits() throws Exception {
        Run run = new Run(
            "STATE $answer = 0 " + "FUNCTION later() RETURNS BOOLEAN DO wait(2) RETURN true END "
                + "EVERY 10 TICKS DO IF \"source\" HAS > 0 AND (later()) THEN "
                + "$answer = 1 ELSE $answer = 2 END END");
        run.fixture.stock(0, 5);
        run.tick(10);
        assertEquals(
            1,
            run.manager.getProgramExecution(run.program)
                .sleepingCount());
        run.fixture.source.setInventorySlotContents(0, null);
        run.tick(12);
        assertEquals(1, run.value(0), "A completed HAS operand must not be observed again after WAIT");
    }

    @Test
    void fluidQuotaSurvivesWaitWhileTheReplacementProviderIsReadFresh() throws Exception {
        Run run = new Run(
            "EVERY 10 TICKS DO INPUT 500 RETAIN 200 fluid:water FROM \"source\" "
                + "OUTPUT 300 fluid:water TO \"dest\" wait(2) OUTPUT 500 fluid:water TO \"dest\" END");
        TankTile original = new TankTile(1000), target = new TankTile(0);
        run.fixture.world.tiles.put(SRC, original);
        run.fixture.world.tiles.put(DST, target);
        run.tick(10);
        assertEquals(300, target.amount);
        TankTile replacement = new TankTile(500);
        run.fixture.world.tiles.put(SRC, replacement);
        run.tick(12);
        assertEquals(500, target.amount, "Only the remaining 200 mB INPUT quota may move");
        assertEquals(300, replacement.amount);
        assertEquals(700, original.amount, "A suspended invocation must release the old tank provider");
        assertEquals(1500, original.amount + replacement.amount + target.amount);
    }

    @Test
    void fluidRetentionIsRecomputedFromStockAtResume() throws Exception {
        Run run = new Run(
            "EVERY 10 TICKS DO INPUT RETAIN 200 fluid:water FROM \"source\" "
                + "OUTPUT 300 fluid:water TO \"dest\" wait(2) OUTPUT fluid:water TO \"dest\" END");
        TankTile source = new TankTile(1000), target = new TankTile(0);
        run.fixture.world.tiles.put(SRC, source);
        run.fixture.world.tiles.put(DST, target);
        run.tick(10);
        source.amount = 500;
        run.tick(12);
        assertEquals(600, target.amount);
        assertEquals(200, source.amount);
    }

    @Test
    void indexedAssignmentFreezesItsPathAndPreservesChangesToOtherFields() throws Exception {
        Run run = new Run(
            "STATE $values = [{a:10,b:20},{a:30,b:40}] STATE $index = 0 STATE $done = 0 "
                + "FUNCTION index($n: INTEGER) RETURNS INTEGER DO wait(1) RETURN $n END "
                + "FUNCTION later() RETURNS INTEGER DO wait(2) RETURN 7 END "
                + "EVERY 10 TICKS DO $values[index($index)].a = later() $done = 1 END "
                + "EVERY 1 TICKS DO $index = 1 $values[0].b = 99 END");
        run.tick(10);
        assertEquals(
            10,
            run.state.values[0].get(0)
                .get("a")
                .asLong());
        run.tick(11);
        assertEquals(0, run.value(2));
        run.tick(13);
        assertEquals(1, run.value(2));
        assertEquals(
            7,
            run.state.values[0].get(0)
                .get("a")
                .asLong());
        assertEquals(
            99,
            run.state.values[0].get(0)
                .get("b")
                .asLong());
        assertEquals(
            30,
            run.state.values[0].get(1)
                .get("a")
                .asLong());
        assertEquals(
            40,
            run.state.values[0].get(1)
                .get("b")
                .asLong());
    }

    @Test
    void printKeepsEarlierArgumentsAndCompletesWaitingCallsWhenCaptureIsDisabled() throws Exception {
        for (boolean disableWhileWaiting : new boolean[] { false, true }) {
            Run run = new Run(
                "STATE $done = 0 " + "FUNCTION later() RETURNS INTEGER DO wait(2) RETURN 7 END "
                    + "EVERY 10 TICKS DO PRINT(label(\"source\").count(item(\"*\")), later()) $done = 1 END");
            run.fixture.stock(0, 5);
            run.manager.getDebugConsole()
                .setEnabled(true);
            run.tick(10);
            assertEquals(0, run.value(0));
            assertTrue(
                run.manager.getDebugConsole()
                    .snapshot()
                    .isEmpty());
            run.fixture.stock(0, 20);
            if (disableWhileWaiting) run.manager.getDebugConsole()
                .setEnabled(false);
            run.tick(12);
            assertEquals(1, run.value(0));
            assertEquals(
                0,
                run.manager.getProgramExecution(run.program)
                    .sleepingCount());
            assertEquals(
                disableWhileWaiting ? Collections.emptyList() : Collections.singletonList("5 7"),
                run.manager.getDebugConsole()
                    .snapshot());
        }
    }

    @Test
    void disabledPrintDoesNotStartAWaitingArgumentEvenIfCaptureLaterTurnsOn() throws Exception {
        Run run = new Run(
            "STATE $done = 0 " + "FUNCTION later() RETURNS INTEGER DO wait(2) RETURN 7 END "
                + "EVERY 10 TICKS DO PRINT(later()) $done = 1 END");
        run.tick(10);
        assertEquals(1, run.value(0));
        assertEquals(
            0,
            run.manager.getProgramExecution(run.program)
                .sleepingCount());
        run.manager.getDebugConsole()
            .setEnabled(true);
        run.tick(12);
        assertTrue(
            run.manager.getDebugConsole()
                .snapshot()
                .isEmpty());
    }

    @Test
    void aFaultInAnotherDueTriggerCancelsPreviouslySleepingInvocations() throws Exception {
        Run run = new Run(
            "STATE $done = 0 EVERY 10 TICKS DO wait(2) $done = 1 END " + "EVERY 10 TICKS DO LET $bad = 1 / 0 END");
        assertThrows(IllegalArgumentException.class, () -> run.tick(10));
        assertEquals(
            0,
            run.manager.getProgramExecution(run.program)
                .sleepingCount());
        run.tick(12);
        assertEquals(0, run.value(0));
    }

    @Test
    void sleepingTriggerLimitFailsClosedWithoutLeavingPartialInvocations() throws Exception {
        StringBuilder source = new StringBuilder();
        for (int i = 0; i < ProgramExecution.MAX_SLEEPING_TRIGGERS; i++)
            source.append("EVERY 10 TICKS DO wait(100) END ");
        Run accepted = new Run(source.toString());
        accepted.tick(10);
        assertEquals(
            ProgramExecution.MAX_SLEEPING_TRIGGERS,
            accepted.manager.getProgramExecution(accepted.program)
                .sleepingCount());
        Run rejected = new Run(
            source.append("EVERY 10 TICKS DO wait(100) END")
                .toString());
        assertThrows(IllegalArgumentException.class, () -> rejected.tick(10));
        assertEquals(
            0,
            rejected.manager.getProgramExecution(rejected.program)
                .sleepingCount());
    }

    @Test
    void sleepingNestedFramesReleaseTheirLiveInvocationServices() throws Exception {
        Run run = new Run(
            "STATE $answer = 0 " + "FUNCTION inner() RETURNS INTEGER DO wait(2) RETURN 7 END "
                + "FUNCTION outer() RETURNS INTEGER DO RETURN inner() END "
                + "EVERY 10 TICKS DO $answer = outer() END");
        run.tick(10);
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        assertEquals(
            3,
            assertDetachedFrames(run.manager.getProgramExecution(run.program), visited),
            "The sleeping trigger and both function frames must detach");
        run.tick(12);
        assertEquals(7, run.value(0));
    }

    /** Traverse only continuation bookkeeping, never reflection into Minecraft or JDK object internals. */
    private static int assertDetachedFrames(Object value, Set<Object> visited) throws Exception {
        if (value == null || !visited.add(value)) return 0;
        if (value instanceof Map<?, ?>) {
            int count = 0;
            for (Object child : ((Map<?, ?>) value).values()) count += assertDetachedFrames(child, visited);
            return count;
        }
        String type = value.getClass()
            .getName();
        if (!type.startsWith("ca.teamdman.sfml.data.ProgramExecution")
            && !type.startsWith("ca.teamdman.sfml.data.Continuation")
            && !(value instanceof DataRuntime)) return 0;
        int count = value instanceof DataRuntime ? 1 : 0;
        for (Field field : value.getClass()
            .getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || field.getType()
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
                assertNull(child, "Sleeping runtime retained live " + field.getName());
            count += assertDetachedFrames(child, visited);
        }
        return count;
    }

    private static final class Memory implements StateAccess {

        final DataValue[] values;

        Memory(Program program) {
            values = new DataValue[program.getStateSchema()
                .size()];
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

        Run(Program program) throws Exception {
            this.program = program;
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

        long value(int slot) {
            return state.values[slot].asLong();
        }
    }
}
