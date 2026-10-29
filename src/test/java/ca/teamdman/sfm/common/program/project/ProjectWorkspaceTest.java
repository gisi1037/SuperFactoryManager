package ca.teamdman.sfm.common.program.project;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectWorkspaceTest {

    @TempDir
    Path world;

    private final UUID owner = UUID.randomUUID(), other = UUID.randomUUID();

    @Test
    void invalidDraftSurvivesRestartAndOptimisticSaveNeverOverwritesNewerSource() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft created = store.create(owner, "Unfinished", "EVERY 20 TICKS DO\n LET $unfinished =");
        assertEquals(0, created.revision);
        ProjectDraft updated = store.save(owner, created.id, 0, "Unfinished II", created.source + " [1,");
        ProjectRepository restarted = new ProjectRepository(world);
        assertEquals(updated.source, restarted.load(owner, created.id).source);
        assertEquals(1, restarted.load(owner, created.id).revision);
        assertStatus(ProjectResult.Status.STALE, () -> restarted.save(owner, created.id, 0, "Stale", "wrong"));
        assertEquals("Unfinished II", restarted.load(owner, created.id).name);
        assertEquals(updated.source, restarted.load(owner, created.id).source);
    }

    @Test
    void everyReadSaveAndListIsPrivateAndWorldSpecific() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft created = store.create(owner, "Private", "source");
        assertStatus(ProjectResult.Status.NOT_FOUND, () -> store.load(other, created.id));
        assertStatus(ProjectResult.Status.NOT_FOUND, () -> store.save(other, created.id, 0, "Stolen", "changed"));
        assertTrue(store.list(other, 0).entries.isEmpty());
        assertEquals(1, store.list(owner, 0).total);
        Path secondWorld = Files.createDirectory(world.resolve("other-world"));
        assertStatus(ProjectResult.Status.NOT_FOUND, () -> new ProjectRepository(secondWorld).load(owner, created.id));
        assertEquals("source", store.load(owner, created.id).source);
    }

    @Test
    void namesAndSourceNeverBecomePathsAndInterruptedTemporariesAreIgnored() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft created = store.create(owner, "../../outside", "../code\\other");
        Path directory = draftPath(created).getParent();
        Files.write(directory.resolve("draft.pending-interrupted"), new byte[] { 0, 1, 2 });
        assertEquals("../code\\other", store.load(owner, created.id).source);
        assertEquals(1, store.list(owner, 0).total);
        assertFalse(Files.exists(world.resolve("outside")));
        UUID interrupted = UUID.randomUUID();
        Path orphan = directory.getParent()
            .resolve(interrupted.toString());
        Files.createDirectory(orphan);
        Files.write(orphan.resolve("draft.pending-interrupted"), new byte[] { 7 });
        assertEquals(1, store.list(owner, 0).total);
        assertStatus(ProjectResult.Status.NOT_FOUND, () -> store.load(owner, interrupted));
    }

    @Test
    void strictUtf8BoundsAndNameRulesRejectBeforeChangingSavedDraft() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft created = store.create(owner, "Draft", "original");
        String[] invalidSources = { repeat("x", ProjectWorkspace.MAX_SOURCE_BYTES + 1),
            repeat("\u00e9", ProjectWorkspace.MAX_SOURCE_BYTES / 2 + 1), "\uD800" };
        for (String source : invalidSources)
            assertThrows(IllegalArgumentException.class, () -> store.save(owner, created.id, 0, "Draft", source));
        String[] invalidNames = { "", " \t ", "line\nbreak", "\u00a7cColor", "\uDC00",
            repeat("x", ProjectWorkspace.MAX_NAME_BYTES + 1) };
        for (String name : invalidNames)
            assertThrows(IllegalArgumentException.class, () -> store.save(owner, created.id, 0, name, "changed"));
        assertEquals("original", store.load(owner, created.id).source);
        assertEquals(0, store.load(owner, created.id).revision);
    }

    @Test
    void malformedUnknownAndOversizedFilesFailClosedAndAreNeverOverwritten() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft created = store.create(owner, "Draft", "original");
        Path file = draftPath(created);
        byte[] original = Files.readAllBytes(file);
        byte[] unknown = original.clone();
        unknown[7] = 2;
        byte[] forgedOwner = original.clone();
        forgedOwner[8] ^= 1;
        byte[][] invalid = { unknown, forgedOwner, new byte[] { 1, 2, 3 },
            new byte[ProjectWorkspace.MAX_SOURCE_BYTES + ProjectWorkspace.MAX_NAME_BYTES + 129], invalidUtf8(created),
            trailing(original) };
        for (byte[] bytes : invalid) {
            Files.write(file, bytes);
            assertThrows(IOException.class, () -> store.load(owner, created.id));
            assertThrows(IOException.class, () -> store.save(owner, created.id, 0, "New", "new"));
            assertArrayEquals(bytes, Files.readAllBytes(file));
        }
        Files.write(file, original);
        assertEquals("original", store.load(owner, created.id).source);
    }

    @Test
    void catalogIsPagedImmutableBoundedAndContainsMetadataOnly() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        for (int i = 0; i < ProjectWorkspace.PAGE_SIZE + 1; i++) store.create(owner, "Draft " + i, "private source");
        ProjectPage first = store.list(owner, 0), last = store.list(owner, ProjectWorkspace.PAGE_SIZE);
        assertEquals(ProjectWorkspace.PAGE_SIZE, first.entries.size());
        assertEquals(ProjectWorkspace.PAGE_SIZE + 1, first.total);
        assertEquals(1, last.entries.size());
        ProjectPage beyondEnd = store.list(owner, ProjectWorkspace.MAX_PROJECTS_PER_OWNER);
        assertEquals(first.total, beyondEnd.offset);
        assertTrue(beyondEnd.entries.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> first.entries.clear());
        assertFalse(
            first.entries.stream()
                .anyMatch(entry -> entry.id.equals(last.entries.get(0).id)));
        assertThrows(IllegalArgumentException.class, () -> store.list(owner, -1));
        assertThrows(
            IllegalArgumentException.class,
            () -> store.list(owner, ProjectWorkspace.MAX_PROJECTS_PER_OWNER + 1));
        // Unknown catalog entries are not deserialized, followed or silently accepted.
        Files.createDirectory(
            world.resolve("sfm/projects")
                .resolve(owner.toString())
                .resolve("not-an-id"));
        assertThrows(IOException.class, () -> store.list(owner, 0));
    }

    @Test
    void projectLimitIncludesUnpublishedOrphansAndRemainsFinite() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft first = store.create(owner, "First", "");
        Path ownerDirectory = draftPath(first).getParent()
            .getParent();
        for (int i = 1; i < ProjectWorkspace.MAX_PROJECTS_PER_OWNER; i++) Files.createDirectory(
            ownerDirectory.resolve(
                UUID.randomUUID()
                    .toString()));
        assertStatus(ProjectResult.Status.LIMIT, () -> store.create(owner, "Too many", ""));
        assertEquals(1, store.list(owner, 0).total);
    }

    @Test
    void callbacksRunOnlyWhenDrainedOnCallerThreadAndCloseSuppressesOldWorldResults() throws Exception {
        ProjectWorkspace workspace = new ProjectWorkspace(world);
        AtomicReference<ProjectResult<ProjectDraft>> result = new AtomicReference<>();
        AtomicReference<Thread> callbackThread = new AtomicReference<>();
        assertTrue(workspace.create(owner, "Draft", "invalid source", completed -> {
            callbackThread.set(Thread.currentThread());
            result.set(completed);
        }));
        awaitCallbacks(workspace, result);
        assertTrue(
            result.get()
                .isSuccess());
        assertSame(Thread.currentThread(), callbackThread.get());
        result.set(null);
        assertTrue(workspace.load(owner, resultDraftId(world), result::set));
        workspace.close();
        assertTrue(workspace.isClosed());
        assertEquals(0, workspace.drainCallbacks(ProjectWorkspace.MAX_PENDING));
        assertNull(result.get());
        assertFalse(workspace.create(owner, "Closed", "", result::set));
        workspace.close();
    }

    @Test
    void outstandingRequestsIncludingCompletedCallbacksHaveOneSharedBound() throws Exception {
        try (ProjectWorkspace workspace = new ProjectWorkspace(world)) {
            AtomicReference<ProjectResult<ProjectPage>> result = new AtomicReference<>();
            for (int i = 0; i < ProjectWorkspace.MAX_PENDING; i++) assertTrue(workspace.list(owner, 0, result::set));
            assertFalse(workspace.list(owner, 0, result::set));
            awaitCallbacks(workspace, result);
            assertTrue(
                result.get()
                    .isSuccess());
            assertTrue(workspace.list(owner, 0, ignored -> {}));
            assertThrows(
                IllegalArgumentException.class,
                () -> workspace.drainCallbacks(ProjectWorkspace.MAX_PENDING + 1));
        }
    }

    @Test
    void asyncFailureAndStaleValidationDoNotLeakPathsOrModifyInvalidDrafts() throws Exception {
        try (ProjectWorkspace workspace = new ProjectWorkspace(world)) {
            ProjectDraft draft = call(workspace, callback -> workspace.create(owner, "Invalid", "EVERY DO", callback));
            ProjectValidation invalid = call(
                workspace,
                callback -> workspace.validate(owner, draft.id, draft.revision, callback));
            assertEquals(draft.id, invalid.id);
            assertEquals(0, invalid.revision);
            assertFalse(invalid.valid);
            assertFalse(invalid.message.isEmpty());
            assertTrue(invalid.message.length() <= ProjectWorkspace.MAX_DIAGNOSTIC_CHARS);
            ProjectDraft valid = call(
                workspace,
                callback -> workspace.save(owner, draft.id, 0, "Valid", "EVERY 20 TICKS DO END", callback));
            ProjectValidation accepted = call(
                workspace,
                callback -> workspace.validate(owner, draft.id, valid.revision, callback));
            assertTrue(accepted.valid, accepted.message);
            assertEquals(1, accepted.revision);
            AtomicReference<ProjectResult<ProjectValidation>> stale = new AtomicReference<>();
            assertTrue(workspace.validate(owner, draft.id, 0, stale::set));
            awaitCallbacks(workspace, stale);
            assertEquals(ProjectResult.Status.STALE, stale.get().status);
            AtomicReference<ProjectResult<ProjectValidation>> forbidden = new AtomicReference<>();
            assertTrue(workspace.validate(other, draft.id, 1, forbidden::set));
            awaitCallbacks(workspace, forbidden);
            assertEquals(ProjectResult.Status.NOT_FOUND, forbidden.get().status);
            assertFalse(forbidden.get().message.contains(world.toString()));
        }
    }

    @Test
    void queuedSavesSerializeTheirRevisionCheckAndOnlyOneWins() throws Exception {
        try (ProjectWorkspace workspace = new ProjectWorkspace(world)) {
            ProjectDraft draft = call(workspace, callback -> workspace.create(owner, "Draft", "initial", callback));
            AtomicReference<ProjectResult<ProjectDraft>> first = new AtomicReference<>(),
                second = new AtomicReference<>();
            assertTrue(workspace.save(owner, draft.id, 0, "First", "first", first::set));
            assertTrue(workspace.save(owner, draft.id, 0, "Second", "second", second::set));
            awaitCallbacks(workspace, first);
            awaitCallbacks(workspace, second);
            assertTrue(
                first.get()
                    .isSuccess());
            assertEquals(ProjectResult.Status.STALE, second.get().status);
            ProjectDraft stored = call(workspace, callback -> workspace.load(owner, draft.id, callback));
            assertEquals("first", stored.source);
            assertEquals(1, stored.revision);
        }
    }

    @Test
    void closeCannotReturnWhileAnOldWorldCallbackIsInProgress() throws Exception {
        ProjectWorkspace workspace = new ProjectWorkspace(world);
        ExecutorService threads = Executors.newFixedThreadPool(2);
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1), closed = new CountDownLatch(1);
        try {
            assertTrue(workspace.list(owner, 0, result -> {
                entered.countDown();
                try {
                    assertTrue(release.await(5, TimeUnit.SECONDS));
                } catch (InterruptedException error) {
                    throw new AssertionError(error);
                }
            }));
            Future<?> drain = threads.submit(() -> {
                long deadline = System.nanoTime() + 5_000_000_000L;
                while (entered.getCount() != 0 && System.nanoTime() < deadline) {
                    workspace.drainCallbacks(1);
                    Thread.yield();
                }
            });
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            Future<?> close = threads.submit(() -> {
                workspace.close();
                closed.countDown();
            });
            assertFalse(closed.await(50, TimeUnit.MILLISECONDS));
            release.countDown();
            drain.get(5, TimeUnit.SECONDS);
            close.get(5, TimeUnit.SECONDS);
            assertEquals(0, closed.getCount());
            assertTrue(workspace.isClosed());
        } finally {
            release.countDown();
            workspace.close();
            threads.shutdownNow();
        }
    }

    @Test
    void owningDirectoriesAndDraftsMustNotBeSymlinks() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Draft", "original");
        Path outside = Files.createTempFile("sfm-project-symlink", ".bin");
        try {
            Files.write(outside, Files.readAllBytes(draftPath(draft)));
            Files.delete(draftPath(draft));
            try {
                Files.createSymbolicLink(draftPath(draft), outside);
            } catch (IOException | UnsupportedOperationException | SecurityException unavailable) {
                org.junit.jupiter.api.Assumptions.assumeTrue(false, "Symlink creation is unavailable");
            }
            assertThrows(IOException.class, () -> store.load(owner, draft.id));
            assertThrows(IOException.class, () -> store.save(owner, draft.id, 0, "Overwrite", "bad"));
        } finally {
            Files.deleteIfExists(outside);
        }
    }

    @Test
    void reopeningWorldCannotAcknowledgeANewerSaveWhileOldWorkerCanStillPublish() throws Exception {
        ProjectRepository repository = new ProjectRepository(world);
        ProjectDraft initial = repository.create(owner, "Initial", "initial");
        ProjectWorkspace old = new ProjectWorkspace(world), reopened = new ProjectWorkspace(world.resolve("."));
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        AtomicReference<ProjectResult<ProjectDraft>> oldReply = new AtomicReference<>(),
            newReply = new AtomicReference<>();
        try {
            Class<?> operationType = Class.forName(ProjectWorkspace.class.getName() + "$Operation");
            Object operation = java.lang.reflect.Proxy.newProxyInstance(
                operationType.getClassLoader(),
                new Class<?>[] { operationType },
                (proxy, method, arguments) -> {
                    entered.countDown();
                    boolean waiting = true;
                    while (waiting) {
                        try {
                            waiting = !release.await(5, TimeUnit.SECONDS);
                        } catch (InterruptedException ignored) { /* Atomic move may ignore cancellation too. */ }
                    }
                    return repository.save(owner, initial.id, 0, "Old", "old publication");
                });
            java.lang.reflect.Method submit = ProjectWorkspace.class
                .getDeclaredMethod("submit", operationType, Consumer.class);
            submit.setAccessible(true);
            assertEquals(
                Boolean.TRUE,
                submit.invoke(old, operation, (Consumer<ProjectResult<ProjectDraft>>) oldReply::set));
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            old.close();
            assertTrue(reopened.save(owner, initial.id, 0, "New", "new publication", newReply::set));
            for (int i = 0; i < 50; i++) {
                reopened.drainCallbacks(1);
                Thread.sleep(1);
            }
            assertNull(newReply.get(), "New lifecycle must not ACK while the previous lifecycle can publish");
            release.countDown();
            awaitCallbacks(reopened, newReply);
            assertNotNull(newReply.get());
            assertEquals(ProjectResult.Status.STALE, newReply.get().status);
            assertNull(oldReply.get(), "Closed workspace suppresses its callback");
            ProjectDraft finalDraft = repository.load(owner, initial.id);
            assertEquals(1, finalDraft.revision);
            assertEquals("old publication", finalDraft.source);
        } finally {
            release.countDown();
            old.close();
            reopened.close();
        }
    }

    private Path draftPath(ProjectDraft draft) {
        return world.resolve("sfm/projects")
            .resolve(draft.owner.toString())
            .resolve(draft.id.toString())
            .resolve("draft.bin");
    }

    private UUID resultDraftId(Path path) throws IOException {
        return new ProjectRepository(path).list(owner, 0).entries.get(0).id;
    }

    private static <T> T call(ProjectWorkspace workspace, Consumer<Consumer<ProjectResult<T>>> operation)
        throws Exception {
        AtomicReference<ProjectResult<T>> result = new AtomicReference<>();
        operation.accept(result::set);
        awaitCallbacks(workspace, result);
        assertTrue(
            result.get()
                .isSuccess(),
            result.get().message);
        return result.get().value;
    }

    private static void awaitCallbacks(ProjectWorkspace workspace, AtomicReference<?> result) throws Exception {
        long deadline = System.nanoTime() + 5_000_000_000L;
        while (result.get() == null && System.nanoTime() < deadline) {
            workspace.drainCallbacks(1);
            Thread.sleep(1);
        }
        assertNotNull(result.get(), "Project worker did not complete");
    }

    private static void assertStatus(ProjectResult.Status status, CheckedOperation operation) {
        assertEquals(status, assertThrows(ProjectRepository.Failure.class, operation::run).status);
    }

    private static String repeat(String text, int count) {
        return String.join("", Collections.nCopies(count, text));
    }

    private static byte[] invalidUtf8(ProjectDraft draft) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            output.writeInt(0x53464D50);
            output.writeInt(1);
            output.writeLong(draft.owner.getMostSignificantBits());
            output.writeLong(draft.owner.getLeastSignificantBits());
            output.writeLong(draft.id.getMostSignificantBits());
            output.writeLong(draft.id.getLeastSignificantBits());
            output.writeLong(0);
            output.writeInt(1);
            output.writeByte(0xFF);
            output.writeInt(0);
        }
        return bytes.toByteArray();
    }

    private static byte[] trailing(byte[] original) {
        return java.util.Arrays.copyOf(original, original.length + 1);
    }

    private interface CheckedOperation {

        void run() throws Exception;
    }
}
