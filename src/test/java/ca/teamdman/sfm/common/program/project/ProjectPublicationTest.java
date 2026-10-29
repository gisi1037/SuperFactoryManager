package ca.teamdman.sfm.common.program.project;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectPublicationTest {

    private static final String SOURCE = "EVERY 20 TICKS DO END";
    private final UUID owner = UUID.randomUUID(), other = UUID.randomUUID();
    @TempDir
    Path world;

    @Test
    void canonicalDigestIsStableAndExactSourceAndSemanticHeaderArePreserved() throws Exception {
        PublishedProgram program = new PublishedProgram(SOURCE);
        assertEquals("f5328160bc084011b924f02157418280f2b52efacaaada88f0212c24d91a670e", program.digest());
        assertEquals(SOURCE, PublishedProgram.decode(program.encode()).source);
        assertArrayEquals(
            program.encode(),
            PublishedProgram.decode(program.encode())
                .encode());
        assertNotEquals(program.digest(), new PublishedProgram(SOURCE + "\n").digest());
        String unicode = "// \u00e9 \uD83D\uDE80\r\n" + SOURCE;
        assertEquals(unicode, PublishedProgram.decode(new PublishedProgram(unicode).encode()).source);
        assertTrue(program.dependencies.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> program.dependencies.add("future"));
        assertThrows(IllegalArgumentException.class, () -> new PublishedProgram("\uD800"));
        assertThrows(
            IllegalArgumentException.class,
            () -> new PublishedProgram(
                String.join("", java.util.Collections.nCopies(ProjectWorkspace.MAX_SOURCE_BYTES + 1, "x"))));
    }

    @Test
    void codecRejectsUnknownVersionsInvalidUtf8DependenciesLengthsTrailingAndTruncatedData() throws Exception {
        byte[] valid = new PublishedProgram(SOURCE).encode();
        byte[] version = valid.clone(), language = valid.clone(), api = valid.clone(), profile = valid.clone(),
            utf8 = valid.clone(), dependencies = valid.clone(), negativeLength = valid.clone(),
            hugeLength = valid.clone();
        version[7] = 2;
        language[12] = 'x';
        api[20] = 'x';
        profile[34] = 'x';
        utf8[12] = (byte) 0xff;
        dependencies[dependencies.length - 1] = 1;
        ByteBuffer.wrap(negativeLength)
            .putInt(8, -1);
        ByteBuffer.wrap(hugeLength)
            .putInt(8, Integer.MAX_VALUE);
        for (byte[] invalid : new byte[][] { version, language, api, profile, utf8, dependencies, negativeLength,
            hugeLength, Arrays.copyOf(valid, valid.length + 1), new byte[PublishedProgram.MAX_BYTES + 1] })
            assertThrows(IOException.class, () -> PublishedProgram.decode(invalid));
        for (int length = 0; length < valid.length; length++) {
            byte[] truncated = Arrays.copyOf(valid, length);
            assertThrows(IOException.class, () -> PublishedProgram.decode(truncated));
        }
    }

    @Test
    void publicationIsSavedSnapshotAndLaterDraftsCannotChangeItsReceiptOrBundle() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Saved", SOURCE);
        PublicationReceipt receipt = store.publish(owner, draft.id, 0);
        byte[] original = Files.readAllBytes(receiptPath(draft, 0));
        assertEquals(owner, receipt.owner);
        assertEquals(draft.id, receipt.project);
        assertEquals("Saved", receipt.name);
        assertEquals(SOURCE, PublishedProgram.decode(Files.readAllBytes(bundlePath(receipt))).source);
        store.save(owner, draft.id, 0, "New invalid draft", "EVERY DO");
        PublicationReceipt retry = new ProjectRepository(world).publish(owner, draft.id, 0);
        assertEquals(receipt.digest, retry.digest);
        assertEquals("Saved", retry.name);
        assertArrayEquals(original, Files.readAllBytes(receiptPath(draft, 0)));
        assertEquals("EVERY DO", store.load(owner, draft.id).source);
        assertStatus(ProjectResult.Status.INVALID, () -> store.publish(owner, draft.id, 1));
        assertFalse(Files.exists(receiptPath(draft, 1)));
    }

    @Test
    void unpublishedStaleAndInvalidDraftsDoNotPublishOrModifyDraft() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Incomplete", "EVERY DO");
        assertStatus(ProjectResult.Status.INVALID, () -> store.publish(owner, draft.id, 0));
        store.save(owner, draft.id, 0, "Saved", SOURCE);
        assertStatus(ProjectResult.Status.STALE, () -> store.publish(owner, draft.id, 0));
        assertFalse(Files.exists(world.resolve("sfm/programs")));
        assertTrue(store.listPublications(owner, draft.id, 0).entries.isEmpty());
        assertEquals(SOURCE, store.load(owner, draft.id).source);
    }

    @Test
    void publicationOwnershipAndWorldBoundariesDoNotTreatDigestAsPermission() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Private", SOURCE);
        PublicationReceipt receipt = store.publish(owner, draft.id, 0);
        assertStatus(ProjectResult.Status.NOT_FOUND, () -> store.publish(other, draft.id, 0));
        assertStatus(ProjectResult.Status.NOT_FOUND, () -> store.listPublications(other, draft.id, 0));
        ProjectDraft second = store.create(other, "Own project", SOURCE);
        assertTrue(store.listPublications(other, second.id, 0).entries.isEmpty());
        assertEquals(receipt.digest, store.publish(other, second.id, 0).digest);
        try (java.util.stream.Stream<Path> files = Files.list(world.resolve("sfm/programs"))) {
            assertEquals(1, files.count(), "Exact source deduplicates independently of private metadata");
        }
        Path another = Files.createDirectory(world.resolve("another-world"));
        assertStatus(ProjectResult.Status.NOT_FOUND, () -> new ProjectRepository(another).publish(owner, draft.id, 0));
    }

    @Test
    void receiptCodecRejectsForgedIdentityRevisionUnknownFormatAndTrailingData() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Private", SOURCE);
        store.publish(owner, draft.id, 0);
        Path file = receiptPath(draft, 0);
        byte[] original = Files.readAllBytes(file);
        byte[] version = original.clone(), ownerBytes = original.clone(), project = original.clone(),
            revision = original.clone();
        version[7] = 2;
        ownerBytes[8] ^= 1;
        project[24] ^= 1;
        revision[47] = 1;
        for (byte[] invalid : new byte[][] { version, ownerBytes, project, revision,
            Arrays.copyOf(original, original.length + 1), new byte[] { 1, 2 },
            new byte[PublicationReceipt.MAX_BYTES + 1] }) {
            Files.write(file, invalid);
            assertThrows(IOException.class, () -> store.publish(owner, draft.id, 0));
            assertThrows(IOException.class, () -> store.listPublications(owner, draft.id, 0));
            assertArrayEquals(invalid, Files.readAllBytes(file));
        }
    }

    @Test
    void corruptMissingOrDifferentBundleIsNeverReplacedEvenWhenReceiptAlreadyExists() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Saved", SOURCE);
        PublicationReceipt receipt = store.publish(owner, draft.id, 0);
        Path bundle = bundlePath(receipt), receiptFile = receiptPath(draft, 0);
        byte[] receiptBytes = Files.readAllBytes(receiptFile);
        for (byte[] corrupt : new byte[][] { new byte[] { 1 }, new PublishedProgram(SOURCE + "\n").encode() }) {
            Files.write(bundle, corrupt);
            assertThrows(IOException.class, () -> store.publish(owner, draft.id, 0));
            assertArrayEquals(corrupt, Files.readAllBytes(bundle));
            Files.delete(receiptFile);
            assertThrows(IOException.class, () -> store.publish(owner, draft.id, 0));
            assertArrayEquals(corrupt, Files.readAllBytes(bundle));
            assertFalse(Files.exists(receiptFile));
            Files.write(receiptFile, receiptBytes);
        }
        Files.delete(bundle);
        assertThrows(IOException.class, () -> store.publish(owner, draft.id, 0));
        assertFalse(Files.exists(bundle));
    }

    @Test
    void validButMismatchedCurrentReceiptCannotAcknowledgeAnotherSourceOrName() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft first = store.create(owner, "First", SOURCE);
        ProjectDraft second = store.create(owner, "Second", SOURCE + "\n");
        PublicationReceipt firstReceipt = store.publish(owner, first.id, 0);
        PublicationReceipt secondReceipt = store.publish(owner, second.id, 0);
        Path file = receiptPath(first, 0);
        for (PublicationReceipt wrong : new PublicationReceipt[] {
            new PublicationReceipt(owner, first.id, 0, secondReceipt.digest, first.name),
            new PublicationReceipt(owner, first.id, 0, firstReceipt.digest, "Renamed"),
            new PublicationReceipt(owner, first.id, 1, firstReceipt.digest, first.name) }) {
            Path target = receiptPath(first, wrong.revision);
            Files.write(target, wrong.encode());
            assertThrows(IOException.class, () -> store.publish(owner, first.id, wrong.revision));
            assertArrayEquals(wrong.encode(), Files.readAllBytes(target));
        }
        assertTrue(Files.exists(file));
    }

    @Test
    void orphanBundleFromInterruptedPublishCanBeReusedAndPendingReceiptIsNotListed() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Saved", SOURCE);
        PublicationReceipt first = store.publish(owner, draft.id, 0);
        Path bundle = bundlePath(first);
        byte[] original = Files.readAllBytes(bundle);
        Files.delete(receiptPath(draft, 0));
        Files.write(
            receiptPath(draft, 0).getParent()
                .resolve(".pending-" + UUID.randomUUID()),
            new byte[] { 1 });
        assertTrue(store.listPublications(owner, draft.id, 0).entries.isEmpty());
        PublicationReceipt retry = store.publish(owner, draft.id, 0);
        assertEquals(first.digest, retry.digest);
        assertArrayEquals(original, Files.readAllBytes(bundle));
        assertEquals(1, store.listPublications(owner, draft.id, 0).total);
    }

    @Test
    void metadataPagesAreImmutableNewestFirstAndHaveBoundedOffsets() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Saved", SOURCE);
        for (int i = 0; i <= ProjectWorkspace.PAGE_SIZE; i++) {
            store.publish(owner, draft.id, i);
            if (i < ProjectWorkspace.PAGE_SIZE) store.save(owner, draft.id, i, "Name " + i, SOURCE);
        }
        PublicationPage first = store.listPublications(owner, draft.id, 0);
        assertEquals(ProjectWorkspace.PAGE_SIZE + 1, first.total);
        assertEquals(ProjectWorkspace.PAGE_SIZE, first.entries.size());
        assertEquals(ProjectWorkspace.PAGE_SIZE, first.entries.get(0).revision);
        PublicationPage last = store.listPublications(owner, draft.id, ProjectWorkspace.PAGE_SIZE);
        assertEquals(1, last.entries.size());
        assertEquals(0, last.entries.get(0).revision);
        assertThrows(UnsupportedOperationException.class, () -> first.entries.clear());
        assertThrows(IllegalArgumentException.class, () -> store.listPublications(owner, draft.id, -1));
        assertThrows(
            IllegalArgumentException.class,
            () -> store.listPublications(owner, draft.id, ProjectWorkspace.MAX_PUBLICATIONS_PER_PROJECT + 1));
        PublicationPage end = store.listPublications(owner, draft.id, ProjectWorkspace.MAX_PUBLICATIONS_PER_PROJECT);
        assertEquals(first.total, end.offset);
        assertTrue(end.entries.isEmpty());
    }

    @Test
    void projectPublicationQuotaCountsOrphanTemporariesAndRetryStillWorksAtCapacity() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Saved", SOURCE);
        store.publish(owner, draft.id, 0);
        Path directory = receiptPath(draft, 0).getParent();
        for (int i = 1; i < ProjectWorkspace.MAX_PUBLICATIONS_PER_PROJECT; i++)
            Files.write(directory.resolve(".pending-" + UUID.randomUUID()), new byte[0]);
        assertEquals(0, store.publish(owner, draft.id, 0).revision);
        store.save(owner, draft.id, 0, "New", SOURCE);
        assertStatus(ProjectResult.Status.LIMIT, () -> store.publish(owner, draft.id, 1));
        assertFalse(Files.exists(receiptPath(draft, 1)));
    }

    @Test
    void worldBundleCountAndByteQuotasRejectBeforeReceiptPublication() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Saved", SOURCE);
        Path directory = Files.createDirectories(world.resolve("sfm/programs"));
        for (int i = 0; i < ProjectWorkspace.MAX_PROGRAMS_PER_WORLD; i++)
            Files.write(directory.resolve(String.format("%064x.sfmp", i)), new byte[0]);
        assertStatus(ProjectResult.Status.LIMIT, () -> store.publish(owner, draft.id, 0));
        try (java.util.stream.Stream<Path> files = Files.list(directory)) {
            for (Path file : (Iterable<Path>) files::iterator) Files.delete(file);
        }
        Path huge = directory.resolve(String.format("%064x.sfmp", 0));
        try (FileChannel file = FileChannel.open(huge, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            file.position(ProjectWorkspace.MAX_PROGRAM_BYTES_PER_WORLD - 1);
            file.write(ByteBuffer.wrap(new byte[] { 0 }));
        }
        assertStatus(ProjectResult.Status.LIMIT, () -> store.publish(owner, draft.id, 0));
        assertFalse(Files.exists(receiptPath(draft, 0)));
        assertEquals(0, store.load(owner, draft.id).revision);
    }

    @Test
    void symlinkBundleCannotBecomePrivateSourceOrBeOverwritten() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Saved", SOURCE);
        PublicationReceipt receipt = store.publish(owner, draft.id, 0);
        Path outside = Files.createTempFile("sfm-publication-link", ".bin");
        try {
            byte[] original = Files.readAllBytes(bundlePath(receipt));
            Files.write(outside, original);
            Files.delete(bundlePath(receipt));
            try {
                Files.createSymbolicLink(bundlePath(receipt), outside);
            } catch (IOException | UnsupportedOperationException | SecurityException unavailable) {
                org.junit.jupiter.api.Assumptions.assumeTrue(false, "Symlink creation is unavailable");
            }
            assertThrows(IOException.class, () -> store.publish(owner, draft.id, 0));
            assertArrayEquals(original, Files.readAllBytes(outside));
        } finally {
            Files.deleteIfExists(outside);
        }
    }

    @Test
    void publicationDirectoryAndReceiptSymlinksFailClosed() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Saved", SOURCE);
        store.publish(owner, draft.id, 0);
        Path receipt = receiptPath(draft, 0);
        byte[] original = Files.readAllBytes(receipt);
        Path outside = Files.createTempFile("sfm-receipt-link", ".bin");
        try {
            Files.write(outside, original);
            Files.delete(receipt);
            try {
                Files.createSymbolicLink(receipt, outside);
            } catch (IOException | UnsupportedOperationException | SecurityException unavailable) {
                org.junit.jupiter.api.Assumptions.assumeTrue(false, "Symlink creation is unavailable");
            }
            assertThrows(IOException.class, () -> store.publish(owner, draft.id, 0));
            assertThrows(IOException.class, () -> store.listPublications(owner, draft.id, 0));
            Files.delete(receipt);
            Files.delete(receipt.getParent());
            Path outsideDirectory = Files.createDirectory(world.resolve("external-publications"));
            Files.write(outsideDirectory.resolve("0.bin"), original);
            Files.createSymbolicLink(receipt.getParent(), outsideDirectory);
            assertThrows(IOException.class, () -> store.publish(owner, draft.id, 0));
            assertThrows(IOException.class, () -> store.listPublications(owner, draft.id, 0));
            assertArrayEquals(original, Files.readAllBytes(outside));
        } finally {
            Files.deleteIfExists(outside);
        }
    }

    @Test
    void workerPublicationSharesTransactionLockAndDrainedCallbackLifecycle() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Saved", SOURCE);
        AtomicReference<ProjectResult<PublicationReceipt>> result = new AtomicReference<>();
        AtomicReference<Thread> callbackThread = new AtomicReference<>();
        try (ProjectWorkspace workspace = new ProjectWorkspace(world)) {
            synchronized (store.transactionLock()) {
                assertTrue(workspace.publish(owner, draft.id, 0, completed -> {
                    callbackThread.set(Thread.currentThread());
                    result.set(completed);
                }));
                Thread.sleep(20);
                assertFalse(Files.exists(world.resolve("sfm/programs")));
                assertEquals(0, workspace.drainCallbacks(1));
                assertNull(result.get());
            }
            await(workspace, result);
            assertTrue(
                result.get()
                    .isSuccess(),
                result.get().message);
            assertSame(Thread.currentThread(), callbackThread.get());
            AtomicReference<ProjectResult<PublicationPage>> page = new AtomicReference<>();
            assertTrue(workspace.listPublications(owner, draft.id, 0, page::set));
            await(workspace, page);
            assertEquals(1, page.get().value.total);
            result.set(null);
            assertTrue(workspace.publish(owner, draft.id, 0, result::set));
            workspace.close();
            assertEquals(0, workspace.drainCallbacks(ProjectWorkspace.MAX_PENDING));
            assertNull(result.get());
            assertFalse(workspace.publish(owner, draft.id, 0, result::set));
        }
    }

    @Test
    void publicationQueueAndCompileFailureUseExistingFiniteCompletionBound() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Invalid", "EVERY DO");
        AtomicReference<ProjectResult<PublicationReceipt>> result = new AtomicReference<>();
        try (ProjectWorkspace workspace = new ProjectWorkspace(world)) {
            for (int i = 0; i < ProjectWorkspace.MAX_PENDING; i++)
                assertTrue(workspace.publish(owner, draft.id, 0, result::set));
            assertFalse(workspace.publish(owner, draft.id, 0, result::set));
            await(workspace, result);
            assertEquals(ProjectResult.Status.INVALID, result.get().status);
            assertFalse(result.get().message.isEmpty());
            assertTrue(result.get().message.length() <= ProjectWorkspace.MAX_DIAGNOSTIC_CHARS);
            assertFalse(result.get().message.contains(world.toString()));
        }
    }

    private Path receiptPath(ProjectDraft draft, long revision) {
        return world.resolve("sfm/projects")
            .resolve(draft.owner.toString())
            .resolve(draft.id.toString())
            .resolve("publications")
            .resolve(revision + ".bin");
    }

    private Path bundlePath(PublicationReceipt receipt) {
        return world.resolve("sfm/programs")
            .resolve(receipt.digest + ".sfmp");
    }

    private static void await(ProjectWorkspace workspace, AtomicReference<?> result) throws Exception {
        long deadline = System.nanoTime() + 5_000_000_000L;
        while (result.get() == null && System.nanoTime() < deadline) {
            workspace.drainCallbacks(1);
            Thread.sleep(1);
        }
        assertNotNull(result.get(), "Publication worker did not complete");
    }

    private static void assertStatus(ProjectResult.Status status, Operation operation) {
        assertEquals(status, assertThrows(ProjectRepository.Failure.class, operation::run).status);
    }

    private interface Operation {

        void run() throws Exception;
    }
}
