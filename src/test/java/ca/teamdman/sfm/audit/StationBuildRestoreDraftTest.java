package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.ProgrammingStationDraftState;
import ca.teamdman.sfm.common.net.PacketStationRequest.Action;
import ca.teamdman.sfm.common.program.project.ProjectDraft;
import ca.teamdman.sfm.common.program.project.PublicationReceipt;
import ca.teamdman.sfm.common.program.project.PublicationSource;
import ca.teamdman.sfm.common.program.project.PublishedProgram;

class StationBuildRestoreDraftTest {

    private static final UUID OWNER = new UUID(1, 2), PROJECT = new UUID(3, 4);
    private static final String OLD = "NAME \"Older\" EVERY 20 TICKS DO END";

    private static ProgrammingStationDraftState loaded() {
        ProgrammingStationDraftState state = new ProgrammingStationDraftState();
        state.saved = new ProjectDraft(
            PROJECT,
            OWNER,
            7,
            "Current project name",
            "NAME \"Current\" EVERY 20 TICKS DO END");
        state.name = state.saved.name;
        state.source = state.saved.source;
        state.selectBuild(receipt(OWNER, PROJECT, 2, OLD));
        return state;
    }

    private static PublicationReceipt receipt(UUID owner, UUID project, long revision, String source) {
        return new PublicationReceipt(
            owner,
            project,
            revision,
            new PublishedProgram(source).digest(),
            "Older build name");
    }

    private static PublicationSource source(ProgrammingStationDraftState state) {
        return new PublicationSource(state.selectedBuild(), OLD);
    }

    @Test
    void restoredBuildChangesOnlyLocalSourceAndKeepsCurrentCasBaselineAndName() {
        ProgrammingStationDraftState state = loaded();
        ProjectDraft baseline = state.saved;
        PublicationSource source = source(state);
        long request = state.begin(Action.RESTORE_BUILD);
        assertTrue(state.receive(request, Action.RESTORE_BUILD, true, null, null, null, source));
        assertEquals(OLD, state.source);
        assertEquals("Current project name", state.name);
        assertSame(baseline, state.saved);
        assertEquals(7, state.saved.revision);
        assertTrue(state.dirty());
        assertFalse(state.uncertainWrite);
        assertDoesNotThrow(() -> state.begin(Action.SAVE));
    }

    @Test
    void dirtyDraftRequiresExplicitGuardAndRemainsVisibleUntilSuccessfulReply() {
        ProgrammingStationDraftState state = loaded();
        state.source = "my unsaved draft";
        state.name = "my unsaved name";
        assertThrows(IllegalStateException.class, () -> state.begin(Action.RESTORE_BUILD));
        long request = state.beginGuardedBuildRestore();
        assertEquals("my unsaved draft", state.source);
        assertTrue(state.receive(request, Action.RESTORE_BUILD, true, null, null, null, source(state)));
        assertEquals(OLD, state.source);
        assertEquals("my unsaved name", state.name);
        assertEquals(7, state.saved.revision);
    }

    @Test
    void sourceEditsVisualEditsAndAnEditUndoAbaAllRejectLateRestore() {
        for (int edit = 0; edit < 3; edit++) {
            ProgrammingStationDraftState state = loaded();
            String original = state.source;
            long request = state.begin(Action.RESTORE_BUILD);
            timeout(state);
            if (edit == 0) state.source = "typed after timeout";
            state.noteLocalEdit();
            if (edit == 2) {
                state.source = "temporary";
                state.noteLocalEdit();
                state.source = original;
                state.noteLocalEdit();
            }
            String expected = state.source;
            assertTrue(state.receive(request, Action.RESTORE_BUILD, true, null, null, null, source(state)));
            assertTrue(state.ignoredLateRestore);
            assertEquals(expected, state.source);
            assertEquals(7, state.saved.revision);
        }
    }

    @Test
    void replyMustMatchExactSelectedOwnerProjectRevisionAndDigest() {
        for (int mismatch = 0; mismatch < 4; mismatch++) {
            ProgrammingStationDraftState state = loaded();
            String before = state.source;
            PublicationReceipt expected = state.selectedBuild();
            String returned = mismatch == 3 ? OLD + " -- different" : OLD;
            PublicationReceipt wrong = receipt(
                mismatch == 0 ? UUID.randomUUID() : expected.owner,
                mismatch == 1 ? UUID.randomUUID() : expected.project,
                mismatch == 2 ? 3 : expected.revision,
                returned);
            long request = state.begin(Action.RESTORE_BUILD);
            assertTrue(
                state.receive(
                    request,
                    Action.RESTORE_BUILD,
                    true,
                    null,
                    null,
                    null,
                    new PublicationSource(wrong, returned)));
            assertTrue(state.ignoredLateRestore);
            assertEquals(before, state.source);
        }
    }

    @Test
    void changedCurrentProjectOrSavedRevisionRejectsCallbackWithoutReplacingWork() {
        for (boolean project : new boolean[] { false, true }) {
            ProgrammingStationDraftState state = loaded();
            PublicationSource source = source(state);
            String before = state.source;
            long request = state.begin(Action.RESTORE_BUILD);
            state.saved = new ProjectDraft(
                project ? UUID.randomUUID() : PROJECT,
                OWNER,
                project ? 7 : 8,
                state.name,
                state.source);
            assertTrue(state.receive(request, Action.RESTORE_BUILD, true, null, null, null, source));
            assertTrue(state.ignoredLateRestore);
            assertEquals(before, state.source);
        }
    }

    @Test
    void unrelatedReplyDoesNotConsumePendingRestoreAndFailureNeverChangesDraft() {
        ProgrammingStationDraftState state = loaded();
        String before = state.source;
        long request = state.begin(Action.RESTORE_BUILD);
        assertFalse(state.matchesPending(request + 1, Action.RESTORE_BUILD));
        assertFalse(state.receive(request + 1, Action.RESTORE_BUILD, true, null, null, null, source(state)));
        assertFalse(state.receive(request, Action.LOAD, true, state.saved, null));
        assertTrue(state.matchesPending(request, Action.RESTORE_BUILD));
        assertTrue(state.receive(request, Action.RESTORE_BUILD, false, null, null, null, null));
        assertEquals(before, state.source);
        assertFalse(state.awaitingReply());
        assertFalse(state.uncertainWrite);
    }

    @Test
    void unchangedLateReplyMayRestoreButPendingAndUncertainWritesCannotBeBypassed() {
        ProgrammingStationDraftState state = loaded();
        long request = state.begin(Action.RESTORE_BUILD);
        timeout(state);
        assertThrows(IllegalStateException.class, state::beginGuardedBuildRestore);
        assertTrue(state.receive(request, Action.RESTORE_BUILD, true, null, null, null, source(state)));
        assertEquals(OLD, state.source);
        state.uncertainWrite = true;
        assertThrows(IllegalStateException.class, state::beginGuardedBuildRestore);
    }

    @Test
    void restoringAlreadyMatchingSourceIsAValidNoOpWithoutAdvancingTheSavedRevision() {
        ProgrammingStationDraftState state = loaded();
        state.saved = new ProjectDraft(PROJECT, OWNER, 7, state.name, OLD);
        state.source = OLD;
        long request = state.begin(Action.RESTORE_BUILD);
        assertTrue(state.receive(request, Action.RESTORE_BUILD, true, null, null, null, source(state)));
        assertFalse(state.ignoredLateRestore);
        assertFalse(state.dirty());
        assertEquals(7, state.saved.revision);
    }

    private static void timeout(ProgrammingStationDraftState state) {
        for (int i = 0; i < 201; i++) state.tick();
        assertTrue(state.awaitingReply());
        assertFalse(state.busy());
    }
}
