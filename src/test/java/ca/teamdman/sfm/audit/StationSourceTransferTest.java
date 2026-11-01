package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.ProgrammingStationDraftState;
import ca.teamdman.sfm.client.gui.StationSourceTransfer;
import ca.teamdman.sfm.common.net.PacketStationRequest.Action;
import ca.teamdman.sfm.common.program.project.ProgramSourceArchive;
import ca.teamdman.sfm.common.program.project.ProgramSourceArchive.Entry;
import ca.teamdman.sfm.common.program.project.ProjectDraft;
import ca.teamdman.sfm.common.program.project.ProjectPage;

class StationSourceTransferTest {

    private static final UUID OWNER = new UUID(1, 2);

    @Test
    void exportsEveryCatalogPageAndSavedRevisionWithoutAnyBuildRequests() {
        StationSourceTransfer transfer = new StationSourceTransfer(OWNER, null);
        List<ProjectPage.Entry> catalog = new ArrayList<>();
        for (int i = 0; i < 35; i++) catalog.add(new ProjectPage.Entry(new UUID(0, i + 1), i, "Program " + i));
        transfer.accept(new ProjectPage(catalog.subList(0, 32), 35, 0), null);
        assertEquals(Action.LIST, transfer.next().action);
        assertEquals(32, transfer.next().offset);
        transfer.accept(new ProjectPage(catalog.subList(32, 35), 35, 32), null);
        for (ProjectPage.Entry entry : catalog) {
            assertEquals(Action.ARCHIVE_LOAD, transfer.next().action);
            assertEquals(entry.id, transfer.next().project);
            transfer
                .accept(null, new ProjectDraft(entry.id, OWNER, entry.revision, entry.name, "saved " + entry.revision));
        }
        assertNull(transfer.next());
        List<Entry> restored = ProgramSourceArchive.decode(transfer.exported());
        assertEquals(35, restored.size());
        assertEquals("saved 34", restored.get(34).source);
    }

    @Test
    void changedCatalogSourceOrWrongOwnerCannotProduceAnExport() {
        ProjectPage.Entry entry = new ProjectPage.Entry(new UUID(0, 1), 3, "Saved");
        StationSourceTransfer transfer = new StationSourceTransfer(OWNER, null);
        transfer.accept(new ProjectPage(Collections.singletonList(entry), 1, 0), null);
        assertThrows(
            IllegalArgumentException.class,
            () -> transfer.accept(null, new ProjectDraft(entry.id, OWNER, 4, "Saved", "changed")));
        assertThrows(
            IllegalArgumentException.class,
            () -> transfer.accept(null, new ProjectDraft(entry.id, UUID.randomUUID(), 3, "Saved", "private")));
        assertNull(transfer.exported());
    }

    @Test
    void importChecksCapacityThenCreatesCopiesAndStopsOnlyAfterAcknowledgment() {
        List<Entry> entries = Arrays.asList(new Entry("Saved", "source"), new Entry("Saved", "other source"));
        StationSourceTransfer full = new StationSourceTransfer(OWNER, entries);
        assertThrows(
            IllegalArgumentException.class,
            () -> full.accept(new ProjectPage(Collections.emptyList(), 127, 0), null));
        assertEquals(0, full.completed());
        StationSourceTransfer transfer = new StationSourceTransfer(OWNER, entries);
        transfer.accept(new ProjectPage(Collections.emptyList(), 0, 0), null);
        assertEquals(Action.ARCHIVE_CREATE, transfer.next().action);
        assertNull(transfer.next().project);
        assertEquals("source", transfer.next().entry.source);
        transfer.stopAfterReply();
        assertNotNull(transfer.next(), "Stop must not forget an outstanding server write");
        transfer.accept(null, new ProjectDraft(UUID.randomUUID(), OWNER, 0, "Saved", "source"));
        assertNull(transfer.next());
        assertEquals(1, transfer.completed());
        assertTrue(
            transfer.message()
                .contains("1/2"));
    }

    @Test
    void completeImportAcknowledgesEachNewCopyAndExportCancellationKeepsClipboardResultAbsent() {
        List<Entry> entries = Collections.singletonList(new Entry("Saved", "source"));
        StationSourceTransfer transfer = new StationSourceTransfer(OWNER, entries);
        transfer.accept(new ProjectPage(Collections.emptyList(), 0, 0), null);
        transfer.accept(null, new ProjectDraft(UUID.randomUUID(), OWNER, 0, "Saved", "source"));
        assertNull(transfer.next());
        assertEquals(1, transfer.completed());
        assertNull(transfer.exported());
        StationSourceTransfer cancelled = new StationSourceTransfer(OWNER, null);
        cancelled.stopAfterReply();
        cancelled.accept(new ProjectPage(Collections.emptyList(), 0, 0), null);
        assertNull(cancelled.exported());
        assertNull(cancelled.next());
    }

    @Test
    void transferRepliesAndTimeoutsNeverReplaceOrMarkTheCurrentDirtyDraftAsAnUnconfirmedSave() {
        ProgrammingStationDraftState state = new ProgrammingStationDraftState();
        state.saved = new ProjectDraft(UUID.randomUUID(), OWNER, 7, "Original", "saved source");
        state.name = "My unsaved name";
        state.source = "My unsaved source";
        ProjectDraft original = state.saved;
        UUID local = state.localId;
        for (Action action : new Action[] { Action.LIST, Action.ARCHIVE_LOAD, Action.ARCHIVE_CREATE }) {
            long id = state.beginArchive(action);
            assertFalse(
                state.receive(id, action, true, new ProjectDraft(UUID.randomUUID(), OWNER, 0, "Copy", "copy"), null));
            assertFalse(state.receiveArchive(id + 1, action));
            for (int i = 0; i < 201; i++) state.tick();
            assertFalse(state.uncertainWrite);
            assertTrue(state.awaitingReply());
            assertThrows(IllegalStateException.class, () -> state.begin(Action.SAVE));
            assertTrue(state.receiveArchive(id, action));
            assertSame(original, state.saved);
            assertEquals(local, state.localId);
            assertEquals("My unsaved name", state.name);
            assertEquals("My unsaved source", state.source);
            assertTrue(state.dirty());
        }
    }
}
