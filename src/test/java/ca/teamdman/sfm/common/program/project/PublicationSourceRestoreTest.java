package ca.teamdman.sfm.common.program.project;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PublicationSourceRestoreTest {

    private static final String SOURCE = "NAME \"Private restore source\" EVERY 20 TICKS DO END";
    private final UUID owner = UUID.randomUUID(), other = UUID.randomUUID();
    @TempDir
    Path world;

    @Test
    void readsExactOlderBuildWithoutChangingDraftPublishingOrIssuingExecutionAuthority() throws Exception {
        ProjectRepository repository = new ProjectRepository(world);
        ProjectDraft draft = repository.create(owner, "Original name", SOURCE);
        PublicationReceipt receipt = repository.publish(owner, draft.id, 0);
        ProjectDraft current = repository.save(owner, draft.id, 0, "Current name", "unfinished draft");
        PublicationSource restored = repository.readPublicationSource(owner, draft.id, 0, receipt.digest);
        assertEquals(SOURCE, restored.source);
        assertEquals(owner, restored.receipt.owner);
        assertEquals(draft.id, restored.receipt.project);
        assertEquals(0, restored.receipt.revision);
        assertEquals(receipt.digest, restored.receipt.digest);
        ProjectDraft unchanged = repository.load(owner, draft.id);
        assertEquals(current.revision, unchanged.revision);
        assertEquals(current.name, unchanged.name);
        assertEquals(current.source, unchanged.source);
        assertFalse(Files.exists(world.resolve("sfm/execution-grants")));
        assertFalse(Files.exists(receipt(draft, 1)));
    }

    @Test
    void digestKnowledgeAndExecutionGrantDoNotAuthorizeAnotherOwnerToReadSource() throws Exception {
        ProjectRepository repository = new ProjectRepository(world);
        ProjectDraft draft = repository.create(owner, "Private", SOURCE);
        PublicationReceipt receipt = repository.publish(owner, draft.id, 0);
        ExecutionReference bearer = repository.issueExecutionGrant(owner, draft.id, 0, receipt.digest);
        assertEquals(receipt.digest, bearer.digest);
        ProjectRepository.Failure denied = assertThrows(
            ProjectRepository.Failure.class,
            () -> repository.readPublicationSource(other, draft.id, 0, bearer.digest));
        assertEquals(ProjectResult.Status.NOT_FOUND, denied.status);
        assertFalse(
            denied.getMessage()
                .contains(SOURCE));
        assertFalse(
            denied.getMessage()
                .contains(bearer.token));
    }

    @Test
    void selectionMustHaveAnExistingOwnerReceiptWithExactRevisionAndDigest() throws Exception {
        ProjectRepository repository = new ProjectRepository(world);
        ProjectDraft draft = repository.create(owner, "Saved", SOURCE);
        String digest = new PublishedProgram(SOURCE).digest();
        assertEquals(
            ProjectResult.Status.NOT_FOUND,
            assertThrows(
                ProjectRepository.Failure.class,
                () -> repository.readPublicationSource(owner, draft.id, 0, digest)).status);
        PublicationReceipt receipt = repository.publish(owner, draft.id, 0);
        assertEquals(
            ProjectResult.Status.STALE,
            assertThrows(
                ProjectRepository.Failure.class,
                () -> repository.readPublicationSource(
                    owner,
                    draft.id,
                    0,
                    new PublishedProgram(SOURCE + " -- different").digest())).status);
        assertEquals(
            ProjectResult.Status.NOT_FOUND,
            assertThrows(
                ProjectRepository.Failure.class,
                () -> repository.readPublicationSource(owner, draft.id, 1, receipt.digest)).status);
        assertThrows(
            IllegalArgumentException.class,
            () -> repository.readPublicationSource(owner, draft.id, -1, digest));
        assertThrows(
            IllegalArgumentException.class,
            () -> repository.readPublicationSource(owner, draft.id, 0, "../invalid"));
    }

    @Test
    void forgedReceiptOwnerAndCorruptOrMissingBundleFailWithoutSourceFallback() throws Exception {
        ProjectRepository repository = new ProjectRepository(world);
        ProjectDraft draft = repository.create(owner, "Saved", SOURCE);
        PublicationReceipt publication = repository.publish(owner, draft.id, 0);
        Path receipt = receipt(draft, 0), bundle = world.resolve("sfm/programs/" + publication.digest + ".sfmp");
        byte[] validReceipt = Files.readAllBytes(receipt), validBundle = Files.readAllBytes(bundle);
        Files.write(receipt, new PublicationReceipt(other, draft.id, 0, publication.digest, "Forged").encode());
        assertThrows(IOException.class, () -> repository.readPublicationSource(owner, draft.id, 0, publication.digest));
        Files.write(receipt, validReceipt);
        Files.write(bundle, new byte[] { 1, 2 });
        assertThrows(IOException.class, () -> repository.readPublicationSource(owner, draft.id, 0, publication.digest));
        Files.delete(bundle);
        assertThrows(IOException.class, () -> repository.readPublicationSource(owner, draft.id, 0, publication.digest));
        Files.write(bundle, validBundle);
        assertEquals(SOURCE, repository.readPublicationSource(owner, draft.id, 0, publication.digest).source);
    }

    @Test
    void sourcePayloadIsBoundToItsReceiptRatherThanArbitraryDraftText() {
        PublicationReceipt receipt = new PublicationReceipt(
            owner,
            UUID.randomUUID(),
            0,
            new PublishedProgram(SOURCE).digest(),
            "Saved");
        assertThrows(IllegalArgumentException.class, () -> new PublicationSource(receipt, "different private source"));
        assertEquals(SOURCE, new PublicationSource(receipt, SOURCE).source);
    }

    @Test
    void workerReadUsesReservedAuthoringQueueAndCloseDiscardsQueuedSourceCallbacks() throws Exception {
        ProjectRepository repository = new ProjectRepository(world);
        ProjectDraft draft = repository.create(owner, "Saved", SOURCE);
        PublicationReceipt receipt = repository.publish(owner, draft.id, 0);
        AtomicReference<ProjectResult<PublicationSource>> completion = new AtomicReference<>();
        try (ProjectWorkspace workspace = new ProjectWorkspace(world)) {
            synchronized (repository.transactionLock()) {
                for (int i = 0; i < ProjectWorkspace.MAX_AUTHORING_PENDING; i++)
                    assertTrue(workspace.readPublicationSource(owner, draft.id, 0, receipt.digest, completion::set));
                assertNull(completion.get());
                assertFalse(workspace.readPublicationSource(owner, draft.id, 0, receipt.digest, completion::set));
                workspace.close();
            }
            assertEquals(0, workspace.drainCallbacks(ProjectWorkspace.MAX_PENDING));
            assertNull(completion.get());
            assertFalse(workspace.readPublicationSource(owner, draft.id, 0, receipt.digest, completion::set));
        }
    }

    @Test
    void workerReadReturnsOnlyAfterServerDrainsAndFailuresAreSourceFree() throws Exception {
        ProjectRepository repository = new ProjectRepository(world);
        ProjectDraft draft = repository.create(owner, "Saved", SOURCE);
        PublicationReceipt receipt = repository.publish(owner, draft.id, 0);
        try (ProjectWorkspace workspace = new ProjectWorkspace(world)) {
            AtomicReference<ProjectResult<PublicationSource>> completion = new AtomicReference<>();
            assertTrue(workspace.readPublicationSource(owner, draft.id, 0, receipt.digest, completion::set));
            await(workspace, completion);
            assertTrue(
                completion.get()
                    .isSuccess());
            assertEquals(SOURCE, completion.get().value.source);
            completion.set(null);
            assertTrue(workspace.readPublicationSource(other, draft.id, 0, receipt.digest, completion::set));
            await(workspace, completion);
            assertFalse(
                completion.get()
                    .isSuccess());
            assertNull(completion.get().value);
            assertFalse(completion.get().message.contains(SOURCE));
            assertFalse(completion.get().message.contains(world.toString()));
        }
    }

    private Path receipt(ProjectDraft draft, long revision) {
        return world.resolve("sfm/projects/" + draft.owner + "/" + draft.id + "/publications/" + revision + ".bin");
    }

    private static void await(ProjectWorkspace workspace, AtomicReference<?> result) throws Exception {
        long deadline = System.nanoTime() + 5_000_000_000L;
        while (result.get() == null && System.nanoTime() < deadline) {
            workspace.drainCallbacks(1);
            Thread.sleep(1);
        }
        assertNotNull(result.get());
    }
}
