package ca.teamdman.sfm.common.program.project;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExecutionGrantTest {

    private static final String SOURCE = "NAME \"Private source sentinel\" EVERY 20 TICKS DO END";
    private static final String TOKEN = repeat("ab", 32);
    private static final String DIGEST = new PublishedProgram(SOURCE).digest();
    private final UUID owner = UUID.randomUUID(), other = UUID.randomUUID();
    @TempDir
    Path world;

    @Test
    void referenceIsStrictImmutableIdentityWithoutTokenBearingStringRepresentation() {
        ExecutionReference first = new ExecutionReference(TOKEN, DIGEST);
        ExecutionReference copy = new ExecutionReference(TOKEN, DIGEST);
        assertEquals(first, copy);
        assertEquals(first.hashCode(), copy.hashCode());
        assertNotEquals(first, new ExecutionReference(repeat("cd", 32), DIGEST));
        assertNotEquals(first, new ExecutionReference(TOKEN, repeat("0", 64)));
        assertNotEquals(first, null);
        assertNotEquals(first, TOKEN);
        assertFalse(
            first.toString()
                .contains(TOKEN));
        for (String invalid : new String[] { null, "", TOKEN.substring(1), TOKEN + "0", TOKEN.toUpperCase(),
            "../" + repeat("a", 61), repeat("g", 64), "\uD800" }) {
            assertThrows(IllegalArgumentException.class, () -> new ExecutionReference(invalid, DIGEST));
            assertThrows(IllegalArgumentException.class, () -> new ExecutionReference(TOKEN, invalid));
        }
    }

    @Test
    void grantCodecBindsExactReferenceOwnerProjectRevisionAndPermission() throws Exception {
        UUID project = UUID.randomUUID();
        ExecutionGrant grant = new ExecutionGrant(
            new ExecutionReference(TOKEN, DIGEST),
            owner,
            project,
            Long.MAX_VALUE);
        byte[] bytes = grant.encode();
        assertEquals(ExecutionGrant.MAX_BYTES, bytes.length);
        ExecutionGrant decoded = ExecutionGrant.decode(bytes);
        assertEquals(grant.reference, decoded.reference);
        assertEquals(owner, decoded.owner);
        assertEquals(project, decoded.project);
        assertEquals(Long.MAX_VALUE, decoded.revision);
        assertArrayEquals(bytes, decoded.encode());
        assertEquals(
            1,
            ByteBuffer.wrap(bytes)
                .getInt(bytes.length - 4),
            "V1 grants EXECUTE_AND_COPY only");
        assertThrows(IllegalArgumentException.class, () -> new ExecutionGrant(grant.reference, owner, project, -1));
    }

    @Test
    void grantCodecRejectsUnknownVersionPermissionUtf8LengthsNegativeRevisionTrailingAndTruncation() throws Exception {
        byte[] valid = new ExecutionGrant(new ExecutionReference(TOKEN, DIGEST), owner, UUID.randomUUID(), 0).encode();
        byte[] version = valid.clone(), magic = valid.clone(), permission = valid.clone(), negative = valid.clone(),
            token = valid.clone(), digest = valid.clone(), utf8 = valid.clone(), length = valid.clone();
        version[7] = 2;
        magic[3] ^= 1;
        permission[187] = 2;
        Arrays.fill(negative, 176, 184, (byte) 0xff);
        token[12] = 'A';
        digest[80] = 'G';
        utf8[12] = (byte) 0xff;
        ByteBuffer.wrap(length)
            .putInt(76, Integer.MAX_VALUE);
        for (byte[] malformed : new byte[][] { version, magic, permission, negative, token, digest, utf8, length,
            Arrays.copyOf(valid, valid.length + 1) })
            assertThrows(IOException.class, () -> ExecutionGrant.decode(malformed));
        for (int size = 0; size < valid.length; size++) {
            byte[] truncated = Arrays.copyOf(valid, size);
            assertThrows(IOException.class, () -> ExecutionGrant.decode(truncated));
        }
        assertThrows(IOException.class, () -> ExecutionGrant.decode(null));
    }

    @Test
    void exactOlderPublishedRevisionIssuesAfterDraftChangesWithoutPublishingOrLoadingLatest() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Saved", SOURCE);
        PublicationReceipt receipt = store.publish(owner, draft.id, 0);
        store.save(owner, draft.id, 0, "Invalid new draft", "EVERY DO");
        ExecutionReference reference = store.issueExecutionGrant(owner, draft.id, 0, receipt.digest);
        ExecutionGrant grant = ExecutionGrant.decode(Files.readAllBytes(grantPath(reference)));
        assertEquals(owner, grant.owner);
        assertEquals(draft.id, grant.project);
        assertEquals(0, grant.revision);
        assertEquals(receipt.digest, reference.digest);
        assertEquals(SOURCE, store.prepareExecution(reference).source);
        assertEquals("EVERY DO", store.load(owner, draft.id).source);
        assertFalse(Files.exists(receiptPath(draft, 1)));
        assertStatus(
            ProjectResult.Status.NOT_FOUND,
            () -> store.issueExecutionGrant(owner, draft.id, 1, receipt.digest));
        // Publication authority survives loss of the mutable draft; its exact immutable receipt remains authoritative.
        Files.delete(
            receiptPath(draft, 0).getParent()
                .getParent()
                .resolve("draft.bin"));
        ExecutionReference second = store.issueExecutionGrant(owner, draft.id, 0, receipt.digest);
        assertNotEquals(reference.token, second.token);
        assertEquals(SOURCE, store.prepareExecution(second).source);
    }

    @Test
    void callerIdentifiersCannotCreateAuthorityForOtherOwnerUnpublishedRevisionOrWrongDigest() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Saved", SOURCE);
        assertStatus(ProjectResult.Status.NOT_FOUND, () -> store.issueExecutionGrant(owner, draft.id, 0, DIGEST));
        assertFalse(Files.exists(world.resolve("sfm/execution-grants")));
        assertFalse(Files.exists(receiptPath(draft, 0)));
        PublicationReceipt receipt = store.publish(owner, draft.id, 0);
        assertStatus(
            ProjectResult.Status.NOT_FOUND,
            () -> store.issueExecutionGrant(other, draft.id, 0, receipt.digest));
        assertStatus(
            ProjectResult.Status.NOT_FOUND,
            () -> store.issueExecutionGrant(owner, UUID.randomUUID(), 0, receipt.digest));
        assertStatus(
            ProjectResult.Status.NOT_FOUND,
            () -> store.issueExecutionGrant(owner, draft.id, 1, receipt.digest));
        assertStatus(ProjectResult.Status.STALE, () -> store.issueExecutionGrant(owner, draft.id, 0, repeat("0", 64)));
        assertThrows(
            IllegalArgumentException.class,
            () -> store.issueExecutionGrant(owner, draft.id, -1, receipt.digest));
        assertThrows(
            IllegalArgumentException.class,
            () -> store.issueExecutionGrant(owner, draft.id, 0, "../anything"));
        assertFalse(Files.exists(world.resolve("sfm/execution-grants")));
    }

    @Test
    void fabricatedTokenDigestSubstitutionAndDifferentWorldFailWithoutFallback() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ExecutionReference reference = issue(store);
        assertStatus(
            ProjectResult.Status.NOT_FOUND,
            () -> store.prepareExecution(new ExecutionReference(TOKEN, reference.digest)));
        assertStatus(
            ProjectResult.Status.NOT_FOUND,
            () -> store.prepareExecution(new ExecutionReference(reference.token, repeat("0", 64))));
        Path otherWorld = Files.createDirectory(world.resolve("another-world"));
        ProjectRepository foreign = new ProjectRepository(otherWorld);
        ProjectDraft foreignDraft = foreign.create(owner, "Same source", SOURCE);
        foreign.publish(owner, foreignDraft.id, 0);
        assertStatus(ProjectResult.Status.NOT_FOUND, () -> foreign.prepareExecution(reference));
        assertFalse(Files.exists(otherWorld.resolve("sfm/execution-grants")));
    }

    @Test
    void copiedReferencePreparesDistinctMutableProgramsAndNoSourceIsWrittenIntoReference() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ExecutionReference reference = issue(store);
        PreparedExecution first = store.prepareExecution(reference);
        PreparedExecution second = store.prepareExecution(new ExecutionReference(reference.token, reference.digest));
        assertEquals(owner, first.sourceOwner);
        assertEquals(owner, second.sourceOwner);
        assertEquals(reference, first.reference);
        assertEquals(reference, second.reference);
        assertEquals(SOURCE, first.source);
        assertEquals(first.source, second.source);
        assertNotNull(first.program);
        assertNotSame(first.program, second.program, "Every activation must own its mutable execution plan");
    }

    @Test
    void actualGrantIsRereadAfterSuccessAndCorruptionOrRemovalNeverUsesAnEarlierPreparation() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ExecutionReference reference = issue(store);
        assertNotNull(store.prepareExecution(reference).program);
        Path file = grantPath(reference);
        byte[] original = Files.readAllBytes(file);
        byte[] unknown = original.clone();
        unknown[7] = 2;
        byte[] wrongToken = original.clone();
        wrongToken[12] = wrongToken[12] == 'a' ? (byte) 'b' : (byte) 'a';
        for (byte[] corrupt : new byte[][] { unknown, Arrays.copyOf(original, original.length - 1),
            Arrays.copyOf(original, original.length + 1), new byte[ExecutionGrant.MAX_BYTES + 1], wrongToken }) {
            Files.write(file, corrupt);
            assertThrows(IOException.class, () -> store.prepareExecution(reference));
            assertArrayEquals(corrupt, Files.readAllBytes(file));
        }
        Files.delete(file);
        assertStatus(ProjectResult.Status.NOT_FOUND, () -> store.prepareExecution(reference));
        assertFalse(Files.exists(file));
    }

    @Test
    void exactBundleIsRereadAfterSuccessAndCorruptDifferentOrMissingArtifactsFailClosed() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ExecutionReference reference = issue(store);
        assertNotNull(store.prepareExecution(reference).program);
        Path file = bundlePath(reference);
        byte[] original = Files.readAllBytes(file);
        for (byte[] corrupt : new byte[][] { new byte[] { 1, 2 }, Arrays.copyOf(original, original.length - 1),
            Arrays.copyOf(original, original.length + 1), new PublishedProgram(SOURCE + "\n").encode(),
            new byte[PublishedProgram.MAX_BYTES + 1] }) {
            Files.write(file, corrupt);
            assertThrows(IOException.class, () -> store.prepareExecution(reference));
            assertArrayEquals(corrupt, Files.readAllBytes(file));
        }
        Files.delete(file);
        assertThrows(IOException.class, () -> store.prepareExecution(reference));
        assertFalse(Files.exists(file));
    }

    @Test
    void corruptedReceiptOrBundleNeverIssuesAGrantOrModifiesPrivateStorage() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Saved", SOURCE);
        PublicationReceipt receipt = store.publish(owner, draft.id, 0);
        Path file = receiptPath(draft, 0);
        byte[] original = Files.readAllBytes(file), forged = original.clone();
        forged[8] ^= 1;
        Files.write(file, forged);
        assertThrows(IOException.class, () -> store.issueExecutionGrant(owner, draft.id, 0, receipt.digest));
        assertArrayEquals(forged, Files.readAllBytes(file));
        Files.write(file, original);
        Path bundle = world.resolve("sfm/programs")
            .resolve(receipt.digest + ".sfmp");
        Files.write(bundle, new byte[] { 7 });
        assertThrows(IOException.class, () -> store.issueExecutionGrant(owner, draft.id, 0, receipt.digest));
        assertArrayEquals(new byte[] { 7 }, Files.readAllBytes(bundle));
        assertFalse(Files.exists(world.resolve("sfm/execution-grants")));
        assertEquals(SOURCE, store.load(owner, draft.id).source);
    }

    @Test
    void grantCountQuotaIncludesInterruptedTemporariesAndOrphansButExistingReferencesStillPrepare() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ExecutionReference reference = issue(store);
        ExecutionGrant grant = ExecutionGrant.decode(Files.readAllBytes(grantPath(reference)));
        Path directory = grantPath(reference).getParent();
        for (int i = 1; i < ProjectWorkspace.MAX_EXECUTION_GRANTS_PER_WORLD; i++)
            Files.write(directory.resolve(".pending-" + UUID.randomUUID()), new byte[0]);
        assertStatus(
            ProjectResult.Status.LIMIT,
            () -> store.issueExecutionGrant(owner, grant.project, grant.revision, grant.reference.digest));
        assertNotNull(store.prepareExecution(reference).program);
        try (java.util.stream.Stream<Path> files = Files.list(directory)) {
            assertEquals(ProjectWorkspace.MAX_EXECUTION_GRANTS_PER_WORLD, files.count());
        }
    }

    @Test
    void grantByteQuotaFailsBeforePublishingAnAdditionalToken() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ExecutionReference reference = issue(store);
        ExecutionGrant grant = ExecutionGrant.decode(Files.readAllBytes(grantPath(reference)));
        Path orphan = grantPath(reference).getParent()
            .resolve(TOKEN + ".bin");
        try (FileChannel output = FileChannel.open(orphan, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            output.position(ProjectWorkspace.MAX_EXECUTION_GRANT_BYTES_PER_WORLD - 1);
            output.write(ByteBuffer.wrap(new byte[] { 0 }));
        }
        assertStatus(
            ProjectResult.Status.LIMIT,
            () -> store.issueExecutionGrant(owner, grant.project, grant.revision, grant.reference.digest));
        assertNotNull(store.prepareExecution(reference).program);
        try (java.util.stream.Stream<Path> files = Files.list(grantPath(reference).getParent())) {
            assertEquals(2, files.count());
        }
    }

    @Test
    void grantFileAndDirectoryLinksCannotSupplyAuthorityOrBeOverwritten() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ExecutionReference reference = issue(store);
        Path grantFile = grantPath(reference);
        byte[] original = Files.readAllBytes(grantFile);
        ExecutionGrant grant = ExecutionGrant.decode(original);
        Path outside = Files.createTempFile("sfm-execution-grant", ".bin");
        try {
            Files.write(outside, original);
            Files.delete(grantFile);
            try {
                Files.createSymbolicLink(grantFile, outside);
            } catch (IOException | UnsupportedOperationException | SecurityException unavailable) {
                org.junit.jupiter.api.Assumptions.assumeTrue(false, "Symlink creation is unavailable");
            }
            assertThrows(IOException.class, () -> store.prepareExecution(reference));
            assertThrows(
                IOException.class,
                () -> store.issueExecutionGrant(owner, grant.project, grant.revision, reference.digest));
            assertArrayEquals(original, Files.readAllBytes(outside));
            Files.delete(grantFile);
            Files.delete(grantFile.getParent());
            Path outsideDirectory = Files.createDirectory(world.resolve("external-grants"));
            Files.write(outsideDirectory.resolve(reference.token + ".bin"), original);
            Files.createSymbolicLink(grantFile.getParent(), outsideDirectory);
            assertThrows(IOException.class, () -> store.prepareExecution(reference));
            assertThrows(
                IOException.class,
                () -> store.issueExecutionGrant(owner, grant.project, grant.revision, reference.digest));
            assertArrayEquals(original, Files.readAllBytes(outsideDirectory.resolve(reference.token + ".bin")));
        } finally {
            Files.deleteIfExists(outside);
        }
    }

    @Test
    void nonRegularGrantAndGrantRootAreRejectedWithoutOverwritingThem() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ExecutionReference reference = issue(store);
        Path file = grantPath(reference);
        ExecutionGrant grant = ExecutionGrant.decode(Files.readAllBytes(file));
        Files.delete(file);
        Files.createDirectory(file);
        assertThrows(IOException.class, () -> store.prepareExecution(reference));
        assertThrows(
            IOException.class,
            () -> store.issueExecutionGrant(owner, grant.project, grant.revision, reference.digest));
        assertTrue(Files.isDirectory(file));
        Files.delete(file);
        Files.delete(file.getParent());
        Files.write(file.getParent(), new byte[] { 7 });
        assertThrows(IOException.class, () -> store.prepareExecution(reference));
        assertThrows(
            IOException.class,
            () -> store.issueExecutionGrant(owner, grant.project, grant.revision, reference.digest));
        assertArrayEquals(new byte[] { 7 }, Files.readAllBytes(file.getParent()));
    }

    @Test
    void compileFailureUsesGenericMessageAndDoesNotReturnPrivateSource() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ExecutionReference issued = issue(store);
        ExecutionGrant original = ExecutionGrant.decode(Files.readAllBytes(grantPath(issued)));
        PublishedProgram invalid = new PublishedProgram("EVERY Private_source_sentinel DO");
        ExecutionReference reference = new ExecutionReference(issued.token, invalid.digest());
        Files.write(bundlePath(reference), invalid.encode());
        Files.write(grantPath(reference), new ExecutionGrant(reference, owner, original.project, 0).encode());
        AtomicReference<ProjectResult<PreparedExecution>> result = new AtomicReference<>();
        try (ProjectWorkspace workspace = new ProjectWorkspace(world)) {
            assertTrue(workspace.prepareExecution(reference, result::set));
            await(workspace, result);
            assertEquals(ProjectResult.Status.INVALID, result.get().status);
            assertEquals("Published program cannot be compiled", result.get().message);
            assertNull(result.get().value);
            assertFalse(result.get().message.contains(reference.token));
            assertFalse(result.get().message.contains("Private_source_sentinel"));
        }
    }

    @Test
    void asyncIssueAndPrepareUseWorldLockAndCallerDrainedCallbacks() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ProjectDraft draft = store.create(owner, "Saved", SOURCE);
        PublicationReceipt receipt = store.publish(owner, draft.id, 0);
        AtomicReference<ProjectResult<ExecutionReference>> issued = new AtomicReference<>();
        AtomicReference<Thread> callbackThread = new AtomicReference<>();
        try (ProjectWorkspace workspace = new ProjectWorkspace(world)) {
            synchronized (store.transactionLock()) {
                assertTrue(workspace.issueExecutionGrant(owner, draft.id, 0, receipt.digest, result -> {
                    callbackThread.set(Thread.currentThread());
                    issued.set(result);
                }));
                Thread.sleep(20);
                assertFalse(Files.exists(world.resolve("sfm/execution-grants")));
                assertEquals(0, workspace.drainCallbacks(1));
            }
            await(workspace, issued);
            assertTrue(
                issued.get()
                    .isSuccess(),
                issued.get().message);
            assertSame(Thread.currentThread(), callbackThread.get());
            AtomicReference<ProjectResult<PreparedExecution>> prepared = new AtomicReference<>();
            assertTrue(workspace.prepareExecution(issued.get().value, prepared::set));
            await(workspace, prepared);
            assertTrue(
                prepared.get()
                    .isSuccess(),
                prepared.get().message);
            assertEquals(SOURCE, prepared.get().value.source);
        }
    }

    @Test
    void executionPreparationSharesFiniteQueueBoundAndCloseSuppressesCallbacks() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ExecutionReference reference = issue(store);
        AtomicReference<ProjectResult<PreparedExecution>> result = new AtomicReference<>();
        try (ProjectWorkspace workspace = new ProjectWorkspace(world)) {
            synchronized (store.transactionLock()) {
                for (int i = 0; i < ProjectWorkspace.MAX_EXECUTION_PENDING; i++)
                    assertTrue(workspace.prepareExecution(reference, result::set));
                assertFalse(workspace.prepareExecution(reference, result::set));
                workspace.close();
            }
            assertEquals(0, workspace.drainCallbacks(ProjectWorkspace.MAX_PENDING));
            assertNull(result.get());
            assertFalse(workspace.prepareExecution(reference, result::set));
            assertFalse(workspace.issueExecutionGrant(owner, UUID.randomUUID(), 0, DIGEST, ignored -> {}));
        }
    }

    @Test
    void asynchronousFailuresNeverDiscloseOpaqueTokenPrivateSourceOrFilesystemPath() throws Exception {
        ProjectRepository store = new ProjectRepository(world);
        ExecutionReference reference = issue(store);
        Files.write(grantPath(reference), new byte[] { 1, 2 });
        AtomicReference<ProjectResult<PreparedExecution>> result = new AtomicReference<>();
        try (ProjectWorkspace workspace = new ProjectWorkspace(world)) {
            assertTrue(workspace.prepareExecution(reference, result::set));
            await(workspace, result);
            assertEquals(ProjectResult.Status.IO_ERROR, result.get().status);
            assertFalse(result.get().message.contains(reference.token));
            assertFalse(result.get().message.contains(SOURCE));
            assertFalse(result.get().message.contains(world.toString()));
            assertNull(result.get().value);
        }
    }

    private ExecutionReference issue(ProjectRepository store) throws IOException {
        ProjectDraft draft = store.create(owner, "Saved", SOURCE);
        PublicationReceipt receipt = store.publish(owner, draft.id, 0);
        return store.issueExecutionGrant(owner, draft.id, 0, receipt.digest);
    }

    private Path grantPath(ExecutionReference reference) {
        return world.resolve("sfm/execution-grants")
            .resolve(reference.token + ".bin");
    }

    private Path bundlePath(ExecutionReference reference) {
        return world.resolve("sfm/programs")
            .resolve(reference.digest + ".sfmp");
    }

    private Path receiptPath(ProjectDraft draft, long revision) {
        return world.resolve("sfm/projects")
            .resolve(draft.owner.toString())
            .resolve(draft.id.toString())
            .resolve("publications")
            .resolve(revision + ".bin");
    }

    private static void await(ProjectWorkspace workspace, AtomicReference<?> result) throws Exception {
        long deadline = System.nanoTime() + 5_000_000_000L;
        while (result.get() == null && System.nanoTime() < deadline) {
            workspace.drainCallbacks(1);
            Thread.sleep(1);
        }
        assertNotNull(result.get(), "Execution grant worker did not complete");
    }

    private static void assertStatus(ProjectResult.Status status, Operation operation) {
        assertEquals(status, assertThrows(ProjectRepository.Failure.class, operation::run).status);
    }

    private static String repeat(String value, int times) {
        return String.join("", Collections.nCopies(times, value));
    }

    private interface Operation {

        void run() throws Exception;
    }
}
