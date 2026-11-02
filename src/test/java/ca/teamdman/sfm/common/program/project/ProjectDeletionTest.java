package ca.teamdman.sfm.common.program.project;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectDeletionTest {

    @TempDir
    Path world;

    private final UUID owner = UUID.randomUUID(), other = UUID.randomUUID();

    @Test
    void exactRevisionIsArchivedAtomicallyAndRetryDoesNotResurrectIt() throws Exception {
        ProjectRepository repository = new ProjectRepository(world);
        ProjectDraft draft = repository.create(owner, "Delete me", "unfinished source");
        Path file = path(draft).resolve("draft.bin");
        byte[] original = Files.readAllBytes(file);
        assertEquals(draft.id, repository.delete(owner, draft.id, 0));
        assertFalse(Files.exists(file));
        assertArrayEquals(original, Files.readAllBytes(path(draft).resolve("deleted.bin")));
        assertEquals(0, repository.list(owner, 0).total);
        assertStatus(ProjectResult.Status.NOT_FOUND, () -> repository.load(owner, draft.id));
        assertStatus(ProjectResult.Status.NOT_FOUND, () -> repository.save(owner, draft.id, 0, "Resurrect", "source"));
        assertEquals(draft.id, new ProjectRepository(world).delete(owner, draft.id, 0));
        assertStatus(ProjectResult.Status.STALE, () -> repository.delete(owner, draft.id, 1));
        assertArrayEquals(original, Files.readAllBytes(path(draft).resolve("deleted.bin")));
    }

    @Test
    void staleForeignAndCollidingDeletionPreserveEverySavedByte() throws Exception {
        ProjectRepository repository = new ProjectRepository(world);
        ProjectDraft draft = repository.create(owner, "Private", "first");
        repository.create(other, "Other", "untouched");
        ProjectDraft current = repository.save(owner, draft.id, 0, "Private", "newer");
        byte[] original = Files.readAllBytes(path(draft).resolve("draft.bin"));
        assertStatus(ProjectResult.Status.STALE, () -> repository.delete(owner, draft.id, 0));
        assertStatus(ProjectResult.Status.NOT_FOUND, () -> repository.delete(other, draft.id, 1));
        assertEquals(current.source, repository.load(owner, draft.id).source);
        Path collision = path(draft).resolve("deleted.bin");
        byte[] archive = { 1, 2, 3 };
        Files.write(collision, archive);
        assertThrows(ProjectRepository.Failure.class, () -> repository.delete(owner, draft.id, 1));
        assertArrayEquals(original, Files.readAllBytes(path(draft).resolve("draft.bin")));
        assertArrayEquals(archive, Files.readAllBytes(collision));
    }

    @Test
    void publishedExecutionAndLibraryReceiptsStillWorkAfterDraftDeletion() throws Exception {
        ProjectRepository repository = new ProjectRepository(world);
        String source = "every 20 ticks do print(1) end";
        ProjectDraft draft = repository.create(owner, "Published", source);
        PublicationReceipt publication = repository.publish(owner, draft.id, 0);
        ExecutionReference disk = repository.issueExecutionGrant(owner, draft.id, 0, publication.digest);
        ProjectDraft library = repository
            .create(owner, "Helpers", "integer twice(integer value) { return value * 2; }");
        PublicationReceipt receipt = repository.publishLibrary(owner, library.id, 0);
        LibraryImport link = new LibraryImport("math", library.id, 0, receipt.digest);
        PublishedLibrary before = repository.loadLibrary(owner, link);
        repository.delete(owner, draft.id, 0);
        repository.delete(owner, library.id, 0);
        ProjectRepository restarted = new ProjectRepository(world);
        assertNotNull(restarted.prepareExecution(disk).program);
        assertEquals(source, restarted.readPublicationSource(owner, draft.id, 0, publication.digest).source);
        assertEquals(before.source, restarted.loadLibrary(owner, link).source);
        assertEquals(0, restarted.list(owner, 0).total);
    }

    @Test
    void deletingAnActiveProjectFreesOneCatalogSlotWithoutRemovingHistory() throws Exception {
        ProjectRepository repository = new ProjectRepository(world);
        ProjectDraft first = null;
        for (int i = 0; i < ProjectWorkspace.MAX_PROJECTS_PER_OWNER; i++) {
            ProjectDraft draft = repository.create(owner, "Project " + i, "");
            if (first == null) first = draft;
        }
        assertStatus(ProjectResult.Status.LIMIT, () -> repository.create(owner, "Too many", ""));
        assertNotNull(first);
        repository.delete(owner, first.id, first.revision);
        repository.create(owner, "Replacement", "");
        assertEquals(ProjectWorkspace.MAX_PROJECTS_PER_OWNER, repository.list(owner, 0).total);
        assertTrue(Files.exists(path(first).resolve("deleted.bin")));
    }

    private Path path(ProjectDraft draft) {
        return world.resolve("sfm/projects")
            .resolve(draft.owner.toString())
            .resolve(draft.id.toString());
    }

    private static void assertStatus(ProjectResult.Status expected, Action action) {
        assertEquals(expected, assertThrows(ProjectRepository.Failure.class, action::run).status);
    }

    private interface Action {

        void run() throws Exception;
    }
}
