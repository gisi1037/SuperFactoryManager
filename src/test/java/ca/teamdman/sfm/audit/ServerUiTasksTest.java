package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.net.ServerUiTasks;
import cpw.mods.fml.common.gameevent.TickEvent;

class ServerUiTasksTest {

    @BeforeEach
    void resetQueue() {
        ServerUiTasks.clear();
    }

    @AfterEach
    void clearQueue() {
        ServerUiTasks.clear();
    }

    private static void tick(TickEvent.Phase phase) {
        ServerUiTasks.INSTANCE.tick(new TickEvent.ServerTickEvent(phase));
    }

    @Test
    void enqueueDefersExecutionUntilStartPhaseAndPreservesFifoAcrossBoundedDrains() {
        List<Integer> executed = new ArrayList<>();
        for (int i = 0; i < 19; i++) {
            final int value = i;
            assertTrue(ServerUiTasks.enqueue(() -> executed.add(value)));
        }
        assertTrue(executed.isEmpty());
        tick(TickEvent.Phase.END);
        assertTrue(executed.isEmpty());
        tick(TickEvent.Phase.START);
        assertEquals(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7), executed);
        tick(TickEvent.Phase.START);
        assertEquals(16, executed.size());
        tick(TickEvent.Phase.START);
        assertEquals(19, executed.size());
        for (int i = 0; i < 19; i++) assertEquals(
            i,
            executed.get(i)
                .intValue());
        tick(TickEvent.Phase.START);
        assertEquals(19, executed.size());
    }

    @Test
    void capacityRejectsTheSixtyFifthTaskAndDrainingReleasesExactlyEightSlots() {
        AtomicInteger executed = new AtomicInteger();
        for (int i = 0; i < 64; i++) assertTrue(ServerUiTasks.enqueue(executed::incrementAndGet));
        assertFalse(ServerUiTasks.enqueue(() -> fail("Rejected task must never execute")));
        tick(TickEvent.Phase.START);
        assertEquals(8, executed.get());
        for (int i = 0; i < 8; i++) assertTrue(ServerUiTasks.enqueue(executed::incrementAndGet));
        assertFalse(ServerUiTasks.enqueue(() -> fail("Queue must remain bounded")));
        for (int i = 0; i < 8; i++) tick(TickEvent.Phase.START);
        assertEquals(72, executed.get());
    }

    @Test
    void clearDiscardsPendingWorkAndReleasesAllCapacity() {
        for (int i = 0; i < 64; i++) assertTrue(ServerUiTasks.enqueue(() -> fail("Cleared work must never execute")));
        ServerUiTasks.clear();
        AtomicInteger executed = new AtomicInteger();
        for (int i = 0; i < 64; i++) assertTrue(ServerUiTasks.enqueue(executed::incrementAndGet));
        for (int i = 0; i < 8; i++) tick(TickEvent.Phase.START);
        assertEquals(64, executed.get());
    }

    @Test
    void failedTaskDoesNotBlockFollowingWorkOrEscapeTheTickBoundary() {
        AtomicInteger executed = new AtomicInteger();
        assertTrue(
            ServerUiTasks.enqueue(() -> { throw new IllegalStateException("Expected UI task fixture failure"); }));
        assertTrue(ServerUiTasks.enqueue(executed::incrementAndGet));
        assertDoesNotThrow(() -> tick(TickEvent.Phase.START));
        assertEquals(1, executed.get());
    }
}
