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

class LibraryProjectStorageTest {

    private static final String LIBRARY = "integer twice(integer value) { RETURN value * 2; }";
    private final UUID owner = UUID.randomUUID(), other = UUID.randomUUID();
    @TempDir
    Path world;

    @Test
    void libraryPublicationIsExactImmutableAndSeparateFromExecutableReceipts() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Math", LIBRARY);
        PublicationReceipt receipt = store.publishLibrary(owner, draft.id, 0);
        assertEquals(owner, receipt.owner);
        assertEquals(draft.id, receipt.project);
        assertEquals(new PublishedLibrary(LIBRARY).digest(), receipt.digest);
        assertTrue(store.listPublications(owner, draft.id, 0).entries.isEmpty());
        assertEquals(1, store.listLibraryPublications(owner, draft.id, 0).total);
        assertFalse(Files.exists(world.resolve("sfm/programs")));
        assertFalse(Files.exists(world.resolve("sfm/execution-grants")));
        byte[] artifact = Files.readAllBytes(libraryFile(receipt)),
            metadata = Files.readAllBytes(libraryReceipt(draft, 0));
        store.save(owner, draft.id, 0, "New invalid source", "EVERY DO");
        assertEquals(receipt.digest, store.publishLibrary(owner, draft.id, 0).digest);
        assertArrayEquals(artifact, Files.readAllBytes(libraryFile(receipt)));
        assertArrayEquals(metadata, Files.readAllBytes(libraryReceipt(draft, 0)));
        assertEquals(LIBRARY, store.loadLibrary(owner, selection(receipt)).source);
        assertStatus(ProjectResult.Status.INVALID, () -> store.publishLibrary(owner, draft.id, 1));
        assertFalse(Files.exists(libraryReceipt(draft, 1)));
        assertStatus(
            ProjectResult.Status.NOT_FOUND,
            () -> store.issueExecutionGrant(owner, draft.id, 0, receipt.digest));
    }

    @Test
    void onlyExactAuthenticatedOwnerLibraryReceiptAuthorizesImportsNotProgramReceiptsOrHashes() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Math", LIBRARY);
        PublicationReceipt library = store.publishLibrary(owner, draft.id, 0);
        LibraryImport selected = selection(library);
        assertStatus(ProjectResult.Status.NOT_FOUND, () -> store.loadLibrary(other, selected));
        assertStatus(ProjectResult.Status.NOT_FOUND, () -> store.listLibraryPublications(other, draft.id, 0));
        assertStatus(
            ProjectResult.Status.NOT_FOUND,
            () -> store.loadLibrary(owner, new LibraryImport("math", draft.id, 1, library.digest)));
        assertStatus(
            ProjectResult.Status.STALE,
            () -> store
                .loadLibrary(owner, new LibraryImport("math", draft.id, 0, new PublishedProgram(LIBRARY).digest())));
        ProjectDraft executable = store.create(owner, "Not a library", "EVERY 20 TICKS DO END");
        PublicationReceipt program = store.publish(owner, executable.id, 0);
        assertStatus(ProjectResult.Status.NOT_FOUND, () -> store.loadLibrary(owner, selection(program)));
        ProjectDraft attacker = store.create(other, "Unrelated", linkedSource(library));
        assertStatus(ProjectResult.Status.NOT_FOUND, () -> store.publish(other, attacker.id, 0));
        assertFalse(Files.exists(programReceipt(attacker, 0)));
        Path anotherWorld = Files.createDirectory(world.resolve("another-world"));
        assertStatus(
            ProjectResult.Status.NOT_FOUND,
            () -> new ProjectRepository(anotherWorld).loadLibrary(owner, selected));
    }

    @Test
    void authoringImportsRemainPinnedToPublishedRevisionEvenAfterLibraryDraftChangesOrDisappears() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft library = store.create(owner, "Math", LIBRARY);
        PublicationReceipt selected = store.publishLibrary(owner, library.id, 0);
        store.save(owner, library.id, 0, "Changed", "integer twice(integer value) { RETURN value * 9; }");
        store.publishLibrary(owner, library.id, 1);
        Files.delete(draftFile(library));
        ProjectDraft root = store.create(owner, "Uses old math", linkedSource(selected));
        assertTrue(store.validate(owner, root.id, 0).valid);
        PublicationReceipt published = store.publish(owner, root.id, 0);
        PublishedProgram closed = PublishedProgram.decode(Files.readAllBytes(programFile(published)));
        assertEquals(2, closed.format);
        assertEquals(1, closed.modules.size());
        assertEquals(0, closed.modules.get(0).reference.revision);
        assertEquals(LIBRARY, closed.modules.get(0).library.source);
        assertEquals(selected.digest, closed.modules.get(0).reference.digest);
        assertEquals(root.source, closed.source);
        assertEquals(0, store.load(owner, root.id).revision);
        assertFalse(Files.exists(world.resolve("sfm/execution-grants")));
    }

    @Test
    void closedActivationAndLostAckNeverReopenDeletedAuthoringLibrariesAndAlwaysCompilePrivatePlans() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft library = store.create(owner, "Math", LIBRARY);
        PublicationReceipt selected = store.publishLibrary(owner, library.id, 0);
        ProjectDraft root = store.create(owner, "Main", linkedSource(selected));
        PublicationReceipt published = store.publish(owner, root.id, 0);
        ExecutionReference ref = store.issueExecutionGrant(owner, root.id, 0, published.digest);
        Files.delete(libraryReceipt(library, 0));
        Files.delete(libraryFile(selected));
        Files.delete(draftFile(library));
        assertEquals(
            published.digest,
            store.publish(owner, root.id, 0).digest,
            "Same-revision ACK uses the closed artifact");
        store.save(owner, root.id, 0, "New unfinished draft", "EVERY DO");
        assertEquals(published.digest, store.publish(owner, root.id, 0).digest, "Older ACK uses the closed artifact");
        PreparedExecution first = store.prepareExecution(ref), second = store.prepareExecution(ref);
        assertNotSame(first.program, second.program);
        assertNotSame(
            first.program.getFunctions()
                .get(0),
            second.program.getFunctions()
                .get(0));
        assertEquals(root.source, first.source);
        assertEquals(owner, first.sourceOwner);
        assertEquals(published.digest, store.issueExecutionGrant(owner, root.id, 0, published.digest).digest);
        PublicationSource restored = store.readPublicationSource(owner, root.id, 0, published.digest);
        assertEquals(root.source, restored.source);
        assertEquals(2, restored.format);
        assertFalse(restored.source.contains("RETURN value * 2"), "Restore includes root text only");
        Files.delete(programFile(published));
        assertThrows(IOException.class, () -> store.prepareExecution(ref));
        assertThrows(IOException.class, () -> store.publish(owner, root.id, 0));
    }

    @Test
    void missingCorruptForgedAndWrongKindLibrariesFailBeforeExecutableOrGrantPublication() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft library = store.create(owner, "Math", LIBRARY);
        PublicationReceipt selected = store.publishLibrary(owner, library.id, 0);
        ProjectDraft root = store.create(owner, "Main", linkedSource(selected));
        Path artifact = libraryFile(selected), receipt = libraryReceipt(library, 0);
        byte[] validLibrary = Files.readAllBytes(artifact), validReceipt = Files.readAllBytes(receipt);
        for (byte[] corrupt : new byte[][] { new byte[] { 1 }, new PublishedLibrary(LIBRARY + "\n").encode(),
            new PublishedProgram(LIBRARY).encode() }) {
            Files.write(artifact, corrupt);
            assertThrows(IOException.class, () -> store.loadLibrary(owner, selection(selected)));
            assertThrows(IOException.class, () -> store.publish(owner, root.id, 0));
            assertThrows(IOException.class, () -> store.publishLibrary(owner, library.id, 0));
            assertArrayEquals(corrupt, Files.readAllBytes(artifact));
            assertFalse(Files.exists(programReceipt(root, 0)));
        }
        Files.write(artifact, validLibrary);
        for (byte[] corrupt : new byte[][] { new byte[] { 1 }, Arrays.copyOf(validReceipt, validReceipt.length - 1),
            Arrays.copyOf(validReceipt, validReceipt.length + 1) }) {
            Files.write(receipt, corrupt);
            assertThrows(IOException.class, () -> store.loadLibrary(owner, selection(selected)));
            assertThrows(IOException.class, () -> store.publishLibrary(owner, library.id, 0));
            assertThrows(IOException.class, () -> store.listLibraryPublications(owner, library.id, 0));
            assertArrayEquals(corrupt, Files.readAllBytes(receipt));
        }
        Files.write(receipt, new PublicationReceipt(other, library.id, 0, selected.digest, "Forged").encode());
        assertThrows(IOException.class, () -> store.publish(owner, root.id, 0));
        Files.write(receipt, validReceipt);
        Files.delete(artifact);
        assertThrows(IOException.class, () -> store.publish(owner, root.id, 0));
        assertFalse(Files.exists(world.resolve("sfm/programs")));
        assertFalse(Files.exists(world.resolve("sfm/execution-grants")));
    }

    @Test
    void invalidLeafAndStaleDraftCannotPublishLibrariesOrChangeAuthoringData() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        for (String invalid : new String[] { "", "EVERY 20 TICKS DO END", "STATE counter = 0 " + LIBRARY,
            "LET count = 0 " + LIBRARY, "integer math.twice(integer value) { RETURN value; }" }) {
            ProjectDraft draft = store.create(owner, "Invalid library", invalid);
            assertStatus(ProjectResult.Status.INVALID, () -> store.publishLibrary(owner, draft.id, 0));
            assertEquals(invalid, store.load(owner, draft.id).source);
            assertFalse(Files.exists(libraryReceipt(draft, 0)));
        }
        ProjectDraft draft = store.create(owner, "Math", LIBRARY);
        store.save(owner, draft.id, 0, "Math", LIBRARY);
        assertStatus(ProjectResult.Status.STALE, () -> store.publishLibrary(owner, draft.id, 0));
        assertFalse(Files.exists(world.resolve("sfm/libraries")));
    }

    @Test
    void libraryCatalogIsOwnNewestFirstBoundedAndDoesNotCreateExecutablePublicationAuthority() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Math", LIBRARY);
        for (int i = 0; i <= ProjectWorkspace.PAGE_SIZE; i++) {
            store.publishLibrary(owner, draft.id, i);
            if (i < ProjectWorkspace.PAGE_SIZE) store.save(owner, draft.id, i, "Math " + i, LIBRARY);
        }
        PublicationPage first = store.listLibraryPublications(owner, draft.id, 0);
        assertEquals(33, first.total);
        assertEquals(32, first.entries.size());
        assertEquals(32, first.entries.get(0).revision);
        assertEquals(0, store.listLibraryPublications(owner, draft.id, 32).entries.get(0).revision);
        assertThrows(UnsupportedOperationException.class, () -> first.entries.clear());
        assertThrows(IllegalArgumentException.class, () -> store.listLibraryPublications(owner, draft.id, -1));
        assertTrue(store.listLibraryPublications(owner, draft.id, 128).entries.isEmpty());
        assertTrue(store.listPublications(owner, draft.id, 0).entries.isEmpty());
    }

    @Test
    void orphanLibraryArtifactsAreReusableButInterruptedReceiptFilesConsumeCountAndByteQuotas() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Math", LIBRARY);
        PublicationReceipt published = store.publishLibrary(owner, draft.id, 0);
        byte[] original = Files.readAllBytes(libraryFile(published));
        Files.delete(libraryReceipt(draft, 0));
        assertEquals(published.digest, store.publishLibrary(owner, draft.id, 0).digest);
        assertArrayEquals(original, Files.readAllBytes(libraryFile(published)));
        Path receipts = libraryReceipt(draft, 0).getParent();
        store.save(owner, draft.id, 0, "Next", LIBRARY);
        Path pending = receipts.resolve(".pending-" + UUID.randomUUID());
        Files.write(pending, new byte[ProjectWorkspace.MAX_PUBLICATIONS_PER_PROJECT * PublicationReceipt.MAX_BYTES]);
        assertStatus(ProjectResult.Status.LIMIT, () -> store.publishLibrary(owner, draft.id, 1));
        Files.delete(pending);
        for (int i = 1; i < ProjectWorkspace.MAX_PUBLICATIONS_PER_PROJECT; i++)
            Files.write(receipts.resolve(".pending-" + UUID.randomUUID()), new byte[0]);
        assertEquals(published.digest, store.publishLibrary(owner, draft.id, 0).digest);
        assertStatus(ProjectResult.Status.LIMIT, () -> store.publishLibrary(owner, draft.id, 1));
        assertFalse(Files.exists(libraryReceipt(draft, 1)));
    }

    @Test
    void worldLibraryArtifactQuotasCountAllFilesAndBytesBeforePublishingReceipt() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Math", LIBRARY);
        Path directory = Files.createDirectories(world.resolve("sfm/libraries"));
        for (int i = 0; i < ProjectWorkspace.MAX_LIBRARIES_PER_WORLD; i++)
            Files.write(directory.resolve(".pending-" + UUID.randomUUID()), new byte[0]);
        assertStatus(ProjectResult.Status.LIMIT, () -> store.publishLibrary(owner, draft.id, 0));
        try (java.util.stream.Stream<Path> files = Files.list(directory)) {
            for (Path path : (Iterable<Path>) files::iterator) Files.delete(path);
        }
        Path huge = directory.resolve(".pending-" + UUID.randomUUID());
        try (FileChannel file = FileChannel.open(huge, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            file.position(ProjectWorkspace.MAX_LIBRARY_BYTES_PER_WORLD - 1);
            file.write(ByteBuffer.wrap(new byte[] { 0 }));
        }
        assertStatus(ProjectResult.Status.LIMIT, () -> store.publishLibrary(owner, draft.id, 0));
        assertFalse(Files.exists(libraryReceipt(draft, 0)));
        assertEquals(LIBRARY, store.load(owner, draft.id).source);
    }

    @Test
    void executableByteQuotaIncludesEmbeddedLibraryBytesNotOnlyTheSmallRootSource() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft library = store.create(owner, "Math", LIBRARY);
        PublicationReceipt selected = store.publishLibrary(owner, library.id, 0);
        ProjectDraft root = store.create(owner, "Main", linkedSource(selected));
        Path directory = Files.createDirectories(world.resolve("sfm/programs"));
        int rootOnlyBytes = new PublishedProgram(root.source).encode().length;
        try (FileChannel file = FileChannel.open(
            directory.resolve(".pending-" + UUID.randomUUID()),
            StandardOpenOption.CREATE_NEW,
            StandardOpenOption.WRITE)) {
            file.position(ProjectWorkspace.MAX_PROGRAM_BYTES_PER_WORLD - rootOnlyBytes - 2);
            file.write(ByteBuffer.wrap(new byte[] { 0 }));
        }
        assertStatus(ProjectResult.Status.LIMIT, () -> store.publish(owner, root.id, 0));
        assertFalse(Files.exists(programReceipt(root, 0)));
        assertEquals(root.source, store.load(owner, root.id).source);
    }

    @Test
    void libraryAndReceiptLinksFailClosedWithoutFollowingOrOverwritingExternalFiles() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Math", LIBRARY);
        PublicationReceipt selected = store.publishLibrary(owner, draft.id, 0);
        Path artifact = libraryFile(selected), outside = Files.createTempFile("sfm-library-link", ".bin");
        byte[] valid = Files.readAllBytes(artifact);
        try {
            Files.write(outside, valid);
            Files.delete(artifact);
            try {
                Files.createSymbolicLink(artifact, outside);
            } catch (IOException | UnsupportedOperationException | SecurityException unavailable) {
                org.junit.jupiter.api.Assumptions.assumeTrue(false, "Symlink creation unavailable");
            }
            assertThrows(IOException.class, () -> store.loadLibrary(owner, selection(selected)));
            assertThrows(IOException.class, () -> store.publishLibrary(owner, draft.id, 0));
            assertArrayEquals(valid, Files.readAllBytes(outside));
            Files.delete(artifact);
            Files.write(artifact, valid);
            Path receipt = libraryReceipt(draft, 0);
            byte[] metadata = Files.readAllBytes(receipt);
            Files.delete(receipt);
            Files.write(outside, metadata);
            Files.createSymbolicLink(receipt, outside);
            assertThrows(IOException.class, () -> store.loadLibrary(owner, selection(selected)));
            assertThrows(IOException.class, () -> store.listLibraryPublications(owner, draft.id, 0));
            Files.delete(receipt);
            Files.delete(receipt.getParent());
            Path external = Files.createDirectory(world.resolve("outside-library-receipts"));
            Files.write(external.resolve("0.bin"), metadata);
            Files.createSymbolicLink(receipt.getParent(), external);
            assertThrows(IOException.class, () -> store.loadLibrary(owner, selection(selected)));
            assertArrayEquals(metadata, Files.readAllBytes(external.resolve("0.bin")));
            Files.delete(receipt.getParent());
            Files.createDirectory(receipt.getParent());
            Files.write(receipt, metadata);
            Files.delete(artifact);
            Files.delete(artifact.getParent());
            Path externalLibraries = Files.createDirectory(world.resolve("outside-library-artifacts"));
            Files.write(externalLibraries.resolve(artifact.getFileName()), valid);
            Files.createSymbolicLink(artifact.getParent(), externalLibraries);
            assertThrows(IOException.class, () -> store.loadLibrary(owner, selection(selected)));
            assertThrows(IOException.class, () -> store.publishLibrary(owner, draft.id, 0));
            assertArrayEquals(valid, Files.readAllBytes(externalLibraries.resolve(artifact.getFileName())));
        } finally {
            Files.deleteIfExists(outside);
        }
    }

    @Test
    void libraryWorkerUsesExistingTransactionCallbackCapacityAndWorldCloseFences() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Math", LIBRARY);
        try (ProjectWorkspace workspace = new ProjectWorkspace(world)) {
            AtomicReference<ProjectResult<PublicationReceipt>> published = new AtomicReference<>();
            synchronized (store.transactionLock()) {
                assertTrue(workspace.publishLibrary(owner, draft.id, 0, published::set));
                assertNull(published.get());
                assertFalse(Files.exists(world.resolve("sfm/libraries")));
            }
            await(workspace, published);
            assertTrue(
                published.get()
                    .isSuccess(),
                published.get().message);
            AtomicReference<ProjectResult<PublicationPage>> page = new AtomicReference<>();
            assertTrue(workspace.listLibraryPublications(owner, draft.id, 0, page::set));
            await(workspace, page);
            assertEquals(1, page.get().value.total);
            AtomicReference<ProjectResult<PublishedLibrary>> loaded = new AtomicReference<>();
            assertTrue(workspace.loadLibrary(owner, selection(published.get().value), loaded::set));
            await(workspace, loaded);
            assertEquals(LIBRARY, loaded.get().value.source);
            ProjectDraft root = store.create(owner, "Main", linkedSource(published.get().value));
            AtomicReference<ProjectResult<ProjectValidation>> validation = new AtomicReference<>();
            assertTrue(workspace.validate(owner, root.id, 0, validation::set));
            await(workspace, validation);
            assertTrue(
                validation.get()
                    .isSuccess(),
                validation.get().message);
            assertTrue(validation.get().value.valid, validation.get().value.message);
            AtomicReference<ProjectResult<PublicationReceipt>> rootPublication = new AtomicReference<>();
            assertTrue(workspace.publish(owner, root.id, 0, rootPublication::set));
            await(workspace, rootPublication);
            assertTrue(
                rootPublication.get()
                    .isSuccess(),
                rootPublication.get().message);
            loaded.set(null);
            synchronized (store.transactionLock()) {
                for (int i = 0; i < ProjectWorkspace.MAX_AUTHORING_PENDING; i++)
                    assertTrue(workspace.loadLibrary(owner, selection(published.get().value), loaded::set));
                assertFalse(workspace.loadLibrary(owner, selection(published.get().value), loaded::set));
                workspace.close();
            }
            assertEquals(0, workspace.drainCallbacks(ProjectWorkspace.MAX_PENDING));
            assertNull(loaded.get());
        }
    }

    private static LibraryImport selection(PublicationReceipt receipt) {
        return new LibraryImport("math", receipt.project, receipt.revision, receipt.digest);
    }

    private static String linkedSource(PublicationReceipt receipt) {
        return "import math from \"" + receipt.project
            + "\" revision "
            + receipt.revision
            + " digest \""
            + receipt.digest
            + "\"; STATE answer = 0 EVERY 20 TICKS { answer = math.twice(21); }";
    }

    private Path libraryFile(PublicationReceipt receipt) {
        return world.resolve("sfm/libraries/" + receipt.digest + ".sfmlib");
    }

    private Path programFile(PublicationReceipt receipt) {
        return world.resolve("sfm/programs/" + receipt.digest + ".sfmp");
    }

    private Path libraryReceipt(ProjectDraft draft, long revision) {
        return world
            .resolve("sfm/projects/" + draft.owner + "/" + draft.id + "/library-publications/" + revision + ".bin");
    }

    private Path programReceipt(ProjectDraft draft, long revision) {
        return world.resolve("sfm/projects/" + draft.owner + "/" + draft.id + "/publications/" + revision + ".bin");
    }

    private Path draftFile(ProjectDraft draft) {
        return world.resolve("sfm/projects/" + draft.owner + "/" + draft.id + "/draft.bin");
    }

    private static void assertStatus(ProjectResult.Status status, Operation operation) {
        assertEquals(status, assertThrows(ProjectRepository.Failure.class, operation::run).status);
    }

    private interface Operation {

        void run() throws Exception;
    }

    private static void await(ProjectWorkspace workspace, AtomicReference<?> result) throws Exception {
        long deadline = System.nanoTime() + 5_000_000_000L;
        while (result.get() == null && System.nanoTime() < deadline) {
            workspace.drainCallbacks(ProjectWorkspace.MAX_PENDING);
            Thread.sleep(1);
        }
        assertNotNull(result.get());
    }
}
