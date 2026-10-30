package ca.teamdman.sfm.common.program.project;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ca.teamdman.sfml.ast.Program;

class ReferenceAdmissionTest {

    @TempDir
    Path world;
    private static final ExecutionReference MISSING = new ExecutionReference(hex('a'), hex('b'));

    @Test
    void fifoLoadsWholeBurstWithPrivatePlansAndFinitePerDrainWork() throws Exception {
        ProjectRepository repository = new ProjectRepository(world);
        UUID owner = UUID.randomUUID();
        ProjectDraft draft = repository.create(owner, "Counter", "STATE $n = 0 EVERY 20 TICKS DO SET $n = $n + 1 END");
        PublicationReceipt receipt = repository.publish(owner, draft.id, 0);
        ExecutionReference reference = repository.issueExecutionGrant(owner, draft.id, 0, receipt.digest);
        List<Integer> order = new ArrayList<>();
        Set<Program> plans = Collections.newSetFromMap(new IdentityHashMap<>());
        try (ProjectWorkspace workspace = new ProjectWorkspace(world)) {
            for (int i = 0; i < 128; i++) {
                final int index = i;
                if (i != 0 && i % ProjectWorkspace.MAX_EXECUTION_REGISTRATIONS == 0) workspace.drainCallbacks(0);
                assertTrue(workspace.enqueueExecution(new Object(), reference, () -> true, result -> {
                    assertTrue(result.isSuccess(), result.message);
                    assertTrue(plans.add(result.value.program), "Mutable compiled programs must not be shared");
                    order.add(index);
                }));
            }
            assertEquals(128, workspace.getWaitingExecutionCount());
            assertEquals(0, workspace.getPendingExecutionCount());
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(15);
            while (order.size() < 128 && System.nanoTime() < deadline) {
                workspace.drainCallbacks(8);
                assertTrue(workspace.getLastAdmissionInspections() <= ProjectWorkspace.MAX_ADMISSION_INSPECTIONS);
                assertTrue(workspace.getLastAdmissionSubmissions() <= ProjectWorkspace.MAX_EXECUTION_STARTS);
                assertTrue(workspace.getPendingExecutionCount() <= ProjectWorkspace.MAX_EXECUTION_PENDING);
                Thread.sleep(1);
            }
            assertEquals(128, order.size());
            for (int i = 0; i < order.size(); i++) assertEquals(i, order.get(i));
            assertEquals(128, plans.size());
            assertEquals(0, workspace.getWaitingExecutionCount());
            assertEquals(0, workspace.getPendingExecutionCount());
        }
    }

    @Test
    void bothWorkloadsKeepReservedCapacityAndCancelCannotInventWorkerSlots() throws Exception {
        ProjectRepository repository = new ProjectRepository(world);
        AtomicInteger canceledCallbacks = new AtomicInteger(), authorCallbacks = new AtomicInteger();
        Object[] keys = new Object[10];
        try (ProjectWorkspace workspace = new ProjectWorkspace(world)) {
            synchronized (repository.transactionLock()) {
                for (int i = 0; i < ProjectWorkspace.MAX_AUTHORING_PENDING; i++)
                    assertTrue(workspace.list(UUID.randomUUID(), 0, result -> authorCallbacks.incrementAndGet()));
                assertFalse(workspace.list(UUID.randomUUID(), 0, result -> {}));
                for (int i = 0; i < keys.length; i++) {
                    keys[i] = new Object();
                    assertTrue(
                        workspace.enqueueExecution(
                            keys[i],
                            MISSING,
                            () -> true,
                            result -> canceledCallbacks.incrementAndGet()));
                }
                for (int i = 0; i < 4; i++) workspace.drainCallbacks(8);
                assertEquals(ProjectWorkspace.MAX_EXECUTION_PENDING, workspace.getPendingExecutionCount());
                assertEquals(ProjectWorkspace.MAX_AUTHORING_PENDING, workspace.getPendingAuthoringCount());
                assertEquals(2, workspace.getWaitingExecutionCount());
                for (Object key : keys) workspace.cancelExecution(key);
                assertEquals(0, workspace.getWaitingExecutionCount());
                assertEquals(ProjectWorkspace.MAX_EXECUTION_PENDING, workspace.getPendingExecutionCount());
                assertFalse(
                    workspace.prepareExecution(MISSING, result -> {}),
                    "Canceled submitted jobs still own slots");
            }
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (workspace.getPendingExecutionCount() + workspace.getPendingAuthoringCount() != 0
                && System.nanoTime() < deadline) {
                workspace.drainCallbacks(8);
                Thread.sleep(1);
            }
            assertEquals(0, workspace.getPendingExecutionCount());
            assertEquals(0, workspace.getPendingAuthoringCount());
            assertEquals(ProjectWorkspace.MAX_AUTHORING_PENDING, authorCallbacks.get());
            assertEquals(0, canceledCallbacks.get());
        }
    }

    @Test
    void staleHeadsConsumeInspectionBudgetWithoutTouchingFilesOrLosingFollowingWork() throws Exception {
        Path unused = world.resolve("never-created");
        AtomicInteger callbacks = new AtomicInteger();
        try (ProjectWorkspace workspace = new ProjectWorkspace(unused)) {
            for (int i = 0; i < 20; i++)
                assertTrue(workspace.enqueueExecution(new Object(), MISSING, () -> false, result -> {
                    assertEquals(ProjectResult.Status.STALE, result.status);
                    callbacks.incrementAndGet();
                }));
            workspace.drainCallbacks(8);
            assertEquals(ProjectWorkspace.MAX_ADMISSION_INSPECTIONS, callbacks.get());
            assertEquals(0, workspace.getPendingExecutionCount());
            assertEquals(0, workspace.getLastAdmissionSubmissions());
            workspace.drainCallbacks(8);
            workspace.drainCallbacks(8);
            assertEquals(20, callbacks.get());
            assertEquals(0, workspace.getWaitingExecutionCount());
            assertFalse(Files.exists(unused));
        }
    }

    @Test
    void waitingCancellationAndDuplicateIdentityCannotCorruptFifo() {
        Object first = new Object(), middle = new Object(), last = new Object();
        List<Integer> delivered = new ArrayList<>();
        try (ProjectWorkspace workspace = new ProjectWorkspace(world)) {
            assertTrue(workspace.enqueueExecution(first, MISSING, () -> false, result -> delivered.add(1)));
            assertTrue(workspace.enqueueExecution(middle, MISSING, () -> false, result -> delivered.add(2)));
            assertTrue(workspace.enqueueExecution(last, MISSING, () -> false, result -> delivered.add(3)));
            assertThrows(
                IllegalStateException.class,
                () -> workspace.enqueueExecution(middle, MISSING, () -> true, result -> {}));
            workspace.cancelExecution(middle);
            workspace.cancelExecution(middle);
            assertEquals(2, workspace.getWaitingExecutionCount());
            workspace.drainCallbacks(8);
            assertEquals(java.util.Arrays.asList(1, 3), delivered);
        }
    }

    @Test
    void registrationAndWaitingCapacityAreFiniteAndCloseRetiresAllAuthority() {
        AtomicInteger callbacks = new AtomicInteger();
        Object first = new Object();
        ProjectWorkspace workspace = new ProjectWorkspace(world);
        for (int i = 0; i < ProjectWorkspace.MAX_WAITING_EXECUTIONS; i++) {
            if (i != 0 && i % ProjectWorkspace.MAX_EXECUTION_REGISTRATIONS == 0) {
                assertFalse(workspace.enqueueExecution(new Object(), MISSING, () -> true, result -> {}));
                workspace.drainCallbacks(0);
            }
            assertTrue(
                workspace.enqueueExecution(
                    i == 0 ? first : new Object(),
                    MISSING,
                    () -> true,
                    result -> callbacks.incrementAndGet()));
        }
        workspace.drainCallbacks(0);
        assertFalse(workspace.enqueueExecution(new Object(), MISSING, () -> true, result -> {}));
        workspace.cancelExecution(first);
        assertTrue(
            workspace.enqueueExecution(new Object(), MISSING, () -> true, result -> callbacks.incrementAndGet()));
        workspace.close();
        assertEquals(0, workspace.getWaitingExecutionCount());
        assertEquals(0, workspace.getPendingExecutionCount());
        assertFalse(workspace.enqueueExecution(new Object(), MISSING, () -> true, result -> {}));
        assertEquals(0, workspace.drainCallbacks(8));
        assertEquals(0, callbacks.get());
    }

    @Test
    void throwingCallbacksStillRetireSlotsAdmitColdWorkAndResetRegistrationBudget() throws Exception {
        AtomicInteger nextCallbacks = new AtomicInteger();
        try (ProjectWorkspace workspace = new ProjectWorkspace(world)) {
            assertTrue(
                workspace.list(UUID.randomUUID(), 0, result -> { throw new IllegalStateException("test consumer"); }));
            for (int i = 0; i < ProjectWorkspace.MAX_EXECUTION_REGISTRATIONS; i++) assertTrue(
                workspace
                    .enqueueExecution(new Object(), MISSING, () -> false, result -> nextCallbacks.incrementAndGet()));
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            boolean caught = false;
            while (!caught && System.nanoTime() < deadline) {
                try {
                    workspace.drainCallbacks(8);
                } catch (IllegalStateException expected) {
                    caught = true;
                }
                Thread.sleep(1);
            }
            assertTrue(caught);
            assertTrue(nextCallbacks.get() >= ProjectWorkspace.MAX_ADMISSION_INSPECTIONS);
            assertEquals(0, workspace.getPendingAuthoringCount());
            assertTrue(workspace.enqueueExecution(new Object(), MISSING, () -> false, result -> {}));
        }
    }

    @Test
    void collidingRegistrationAttemptsDoNotEvaluateLiveFencesBeforeBoundedAdmission() {
        AtomicInteger fences = new AtomicInteger();
        try (ProjectWorkspace workspace = new ProjectWorkspace(world)) {
            int enrolled = 0;
            for (int i = 0; i < 5000; i++) {
                if (workspace.enqueueExecution(new Object(), MISSING, () -> {
                    fences.incrementAndGet();
                    return false;
                }, result -> {})) enrolled++;
            }
            assertEquals(ProjectWorkspace.MAX_EXECUTION_REGISTRATIONS, enrolled);
            assertEquals(0, fences.get());
            workspace.drainCallbacks(8);
            assertEquals(ProjectWorkspace.MAX_ADMISSION_INSPECTIONS, fences.get());
        }
    }

    private static String hex(char value) {
        return String.join("", Collections.nCopies(64, String.valueOf(value)));
    }
}
