package ca.teamdman.sfm.common.program.project;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ca.teamdman.sfm.client.gui.StationSourceTransfer;
import ca.teamdman.sfm.common.net.PacketStationRequest.Action;

class ProgramArchiveWorldTransferTest {

    @TempDir
    Path root;

    @Test
    void latestSavedSourcesMoveBetweenWorldStoresWithoutHistoryForeignProjectsOrOverwrites() throws Exception {
        UUID owner = UUID.randomUUID(), targetOwner = UUID.randomUUID(), stranger = UUID.randomUUID();
        ProjectRepository from = new ProjectRepository(java.nio.file.Files.createDirectory(root.resolve("from")));
        ProjectRepository to = new ProjectRepository(java.nio.file.Files.createDirectory(root.resolve("to")));
        ProjectDraft saved = from.create(owner, "Saved", "every 20 ticks do end");
        from.publish(owner, saved.id, 0);
        saved = from.save(owner, saved.id, 0, "Saved", "-- latest saved draft, not a build\ninvalid source 😀");
        from.create(owner, "Library", "void helper() {return;}");
        from.create(stranger, "Private", "never export this source");
        ProjectDraft existing = to.create(targetOwner, "Saved", "existing destination source");
        StationSourceTransfer export = new StationSourceTransfer(owner, null);
        while (export.next() != null) {
            StationSourceTransfer.Request request = export.next();
            if (request.action == Action.LIST) export.accept(from.list(owner, request.offset), null);
            else {
                assertEquals(Action.ARCHIVE_LOAD, request.action);
                export.accept(null, from.load(owner, request.project));
            }
        }
        List<ProgramSourceArchive.Entry> entries = ProgramSourceArchive.decode(export.exported());
        assertEquals(2, entries.size());
        assertTrue(
            entries.stream()
                .anyMatch(entry -> entry.source.contains("latest saved draft")));
        assertFalse(
            entries.stream()
                .anyMatch(entry -> entry.name.equals("Private")));
        StationSourceTransfer restore = new StationSourceTransfer(targetOwner, entries);
        while (restore.next() != null) {
            StationSourceTransfer.Request request = restore.next();
            if (request.action == Action.LIST) restore.accept(to.list(targetOwner, 0), null);
            else {
                assertEquals(Action.ARCHIVE_CREATE, request.action);
                ProjectDraft copy = to.create(targetOwner, request.entry.name, request.entry.source);
                assertNotEquals(saved.id, copy.id);
                assertEquals(0, copy.revision);
                assertEquals(0, to.listPublications(targetOwner, copy.id, 0).total);
                restore.accept(null, copy);
            }
        }
        assertEquals(2, restore.completed());
        assertEquals(3, to.list(targetOwner, 0).total);
        assertEquals("existing destination source", to.load(targetOwner, existing.id).source);
        assertEquals(0, to.list(owner, 0).total);
        assertEquals(saved.source, from.load(owner, saved.id).source);
    }
}
