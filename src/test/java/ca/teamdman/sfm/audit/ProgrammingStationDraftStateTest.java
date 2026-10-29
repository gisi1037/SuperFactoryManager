package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.ProgrammingStationDraftState;
import ca.teamdman.sfm.common.net.PacketStationReply;
import ca.teamdman.sfm.common.net.PacketStationRequest.Action;
import ca.teamdman.sfm.common.program.project.ProjectDraft;
import ca.teamdman.sfm.common.program.project.ProjectValidation;

class ProgrammingStationDraftStateTest {

    private static final UUID OWNER = new UUID(1, 2), PROJECT = new UUID(3, 4);

    @Test
    void sourceAndNameChangesBothProtectLoadAndNewUntilExplicitlyRetained() {
        ProgrammingStationDraftState state = loaded();
        state.name = "renamed";
        assertTrue(state.dirty());
        assertFalse(state.canReplace());
        assertThrows(IllegalStateException.class, state::newDraft);
        assertThrows(IllegalStateException.class, () -> state.begin(Action.LOAD));
        state.name = state.saved.name;
        state.source = "changed source";
        assertTrue(state.dirty());
        assertThrows(IllegalStateException.class, () -> state.begin(Action.LOAD));
        UUID previousLocal = state.localId;
        state.afterRetainingDraft();
        assertTrue(state.canReplace());
        assertEquals("", state.source);
        assertEquals("Untitled", state.name);
        assertNotEquals(previousLocal, state.localId, "Uncreated drafts need distinct recovery identities");
    }

    @Test
    void saveTimeoutKeepsCorrelationAndLateAckDoesNotOverwriteNewerEdits() {
        ProgrammingStationDraftState state = loaded();
        state.source = "sent source";
        long id = state.begin(Action.SAVE);
        timeout(state);
        assertTrue(state.awaitingReply());
        assertFalse(state.busy());
        assertTrue(state.uncertainWrite);
        assertThrows(IllegalStateException.class, () -> state.begin(Action.SAVE));
        state.source = "newer local source";
        state.name = "newer local name";
        assertFalse(state.receive(id + 1, Action.SAVE, true, draft(1, "sent source"), null));
        assertFalse(state.receive(id, Action.LOAD, true, draft(1, "sent source"), null));
        assertTrue(state.receive(id, Action.SAVE, true, draft(1, "sent source"), null));
        assertEquals(1, state.saved.revision);
        assertEquals("sent source", state.saved.source);
        assertEquals("newer local source", state.source);
        assertEquals("newer local name", state.name);
        assertTrue(state.dirty());
        assertFalse(state.uncertainWrite);
    }

    @Test
    void createTimeoutDoesNotInventAProjectIdOrPermitADuplicateCreate() {
        ProgrammingStationDraftState state = new ProgrammingStationDraftState();
        state.name = "new project";
        state.source = "saved source";
        long id = state.begin(Action.CREATE);
        timeout(state);
        assertNull(state.saved);
        assertTrue(state.uncertainWrite);
        assertThrows(IllegalStateException.class, () -> state.begin(Action.CREATE));
        state.name = "newer local name";
        assertTrue(state.receive(id, Action.CREATE, true, draft(0, "saved source"), null));
        assertEquals(PROJECT, state.saved.id);
        assertFalse(state.uncertainWrite);
        assertEquals("newer local name", state.name, "Late acknowledgment does not rename newer local content");
    }

    @Test
    void rejectedWritePreservesTheWholeDraftAndItsSavedBaseline() {
        ProgrammingStationDraftState state = loaded();
        ProjectDraft baseline = state.saved;
        state.name = "my unsaved name";
        state.source = "my unsaved source";
        long id = state.begin(Action.SAVE);
        assertTrue(state.receive(id, Action.SAVE, false, null, null));
        assertSame(baseline, state.saved);
        assertEquals("my unsaved name", state.name);
        assertEquals("my unsaved source", state.source);
        assertTrue(state.dirty());
    }

    @Test
    void recoveryReconcilesSavedRevisionWhileKeepingTheLatestLocalSourceAndName() {
        ProgrammingStationDraftState state = loaded();
        state.name = "recovered name";
        state.source = "recovered source";
        state.uncertainWrite = true;
        long id = state.beginRecoveryLoad();
        timeout(state);
        state.source = "edited recovery source";
        assertTrue(state.receive(id, Action.LOAD, true, draft(7, "server source"), null));
        assertEquals(7, state.saved.revision);
        assertEquals("server source", state.saved.source);
        assertEquals("edited recovery source", state.source);
        assertEquals("recovered name", state.name);
        assertFalse(state.uncertainWrite);
        assertTrue(state.dirty());
        assertTrue(
            state.revisionLine()
                .contains("Saved r7"));
    }

    @Test
    void ordinaryLateLoadCannotDiscardEditingStartedAfterTimeout() {
        ProgrammingStationDraftState state = loaded();
        ProjectDraft previous = state.saved;
        long id = state.begin(Action.LOAD);
        timeout(state);
        state.source = "new local edits";
        assertTrue(state.receive(id, Action.LOAD, true, draft(8, "different server source"), null));
        assertTrue(state.ignoredLateLoad);
        assertSame(previous, state.saved);
        assertEquals("new local edits", state.source);
    }

    @Test
    void generationOnlyLateLoadKeepsPreviousProjectAndItsUnchangedSerializedSource() {
        ProgrammingStationDraftState state = loaded();
        ProjectDraft previous = state.saved;
        long id = state.begin(Action.LOAD);
        timeout(state);
        state.noteLocalEdit(); // A disconnected node has no new serializable source yet.
        assertFalse(state.dirty(), "Visual dirty tracking remains the caller's responsibility");
        assertTrue(state.receive(id, Action.LOAD, true, draft(8, "different server source"), null));
        assertTrue(state.ignoredLateLoad);
        assertSame(previous, state.saved);
        assertEquals("original source", state.source);
        assertEquals("original name", state.name);
    }

    @Test
    void generationOnlyLateSaveAdvancesBaselineWithoutNormalizingTheLocalDraft() {
        ProgrammingStationDraftState state = loaded();
        state.name = "  original name  ";
        long id = state.begin(Action.SAVE);
        timeout(state);
        state.noteLocalEdit();
        assertTrue(state.receive(id, Action.SAVE, true, draft(1, "original source"), null));
        assertEquals(1, state.saved.revision);
        assertEquals("original name", state.saved.name);
        assertEquals("  original name  ", state.name, "A late acknowledgement must preserve the local edit generation");
        assertEquals("original source", state.source);
        assertFalse(state.uncertainWrite);
        assertFalse(state.ignoredLateLoad);
    }

    @Test
    void intentionalRecoveryLoadReconcilesDespiteVisualOnlyEditsAfterTimeout() {
        ProgrammingStationDraftState state = loaded();
        state.uncertainWrite = true;
        long id = state.beginRecoveryLoad();
        timeout(state);
        state.noteLocalEdit();
        assertTrue(state.receive(id, Action.LOAD, true, draft(7, "new server baseline"), null));
        assertEquals(7, state.saved.revision);
        assertEquals("new server baseline", state.saved.source);
        assertEquals("original source", state.source);
        assertEquals("original name", state.name);
        assertFalse(state.uncertainWrite);
        assertFalse(state.ignoredLateLoad);
    }

    @Test
    void untouchedLoadAcceptsEditsThatPrecededItsCapturedGeneration() {
        ProgrammingStationDraftState state = loaded();
        state.noteLocalEdit();
        long id = state.begin(Action.LOAD);
        timeout(state);
        assertTrue(state.receive(id, Action.LOAD, true, draft(2, "loaded source"), null));
        assertEquals(2, state.saved.revision);
        assertEquals("loaded source", state.source);
        assertFalse(state.ignoredLateLoad);
        assertFalse(state.dirty());
    }

    @Test
    void retainedDraftResetStartsFreshGenerationWithoutReusingRequestIdentity() {
        ProgrammingStationDraftState state = loaded();
        long previous = state.begin(Action.LOAD);
        timeout(state);
        state.noteLocalEdit();
        state.afterRetainingDraft();
        long current = state.begin(Action.LOAD);
        assertNotEquals(previous, current);
        assertFalse(state.receive(previous, Action.LOAD, true, draft(9, "stale source"), null));
        assertTrue(state.awaitingReply());
        assertTrue(state.receive(current, Action.LOAD, true, draft(3, "fresh source"), null));
        assertEquals("fresh source", state.source);
        assertFalse(state.ignoredLateLoad);
    }

    @Test
    void saturatedGenerationStillDetectsNewEditsAndDoesNotPoisonTheNextRequest() throws Exception {
        ProgrammingStationDraftState state = loaded();
        java.lang.reflect.Field generation = ProgrammingStationDraftState.class.getDeclaredField("localEditGeneration");
        generation.setAccessible(true);
        generation.setLong(state, Long.MAX_VALUE);
        long first = state.begin(Action.LOAD);
        state.noteLocalEdit();
        assertTrue(state.receive(first, Action.LOAD, true, draft(4, "must not replace"), null));
        assertTrue(state.ignoredLateLoad);
        assertEquals("original source", state.source);
        assertEquals(Long.MAX_VALUE, generation.getLong(state));
        long second = state.begin(Action.LOAD);
        assertTrue(state.receive(second, Action.LOAD, true, draft(5, "untouched snapshot"), null));
        assertEquals("untouched snapshot", state.source);
        assertFalse(state.ignoredLateLoad);
    }

    @Test
    void validationIsForTheExactSavedRevisionAndDirtyDraftsMustBeSavedFirst() {
        ProgrammingStationDraftState state = loaded();
        state.source = "edit";
        assertThrows(IllegalStateException.class, () -> state.begin(Action.VALIDATE));
        state.source = state.saved.source;
        long id = state.begin(Action.VALIDATE);
        assertTrue(
            state.receive(id, Action.VALIDATE, true, null, new ProjectValidation(PROJECT, 99, true, "wrong revision")));
        assertNull(state.validation);
        id = state.begin(Action.VALIDATE);
        assertTrue(state.receive(id, Action.VALIDATE, true, null, new ProjectValidation(PROJECT, 0, true, "Valid")));
        assertTrue(
            state.revisionLine()
                .contains("Validated r0: OK"));
        state.source = "a later edit";
        assertTrue(
            state.revisionLine()
                .contains("edited"));
        assertEquals(0, state.validation.revision, "An edit cannot inherit a new validation revision");
    }

    @Test
    void routeCorrelationRejectsDifferentStationWindowAndReopenedSession() {
        UUID session = new UUID(5, 6);
        PacketStationReply reply = new PacketStationReply();
        reply.x = 1;
        reply.y = 2;
        reply.z = 3;
        reply.windowId = 42;
        reply.sessionId = session;
        assertTrue(ProgrammingStationDraftState.matches(session, 42, 1, 2, 3, reply));
        assertFalse(ProgrammingStationDraftState.matches(new UUID(5, 7), 42, 1, 2, 3, reply));
        assertFalse(ProgrammingStationDraftState.matches(session, 43, 1, 2, 3, reply));
        assertFalse(ProgrammingStationDraftState.matches(session, 42, 4, 2, 3, reply));
        assertFalse(ProgrammingStationDraftState.matches(session, 42, 1, 4, 3, reply));
        assertFalse(ProgrammingStationDraftState.matches(session, 42, 1, 2, 4, reply));
        assertFalse(ProgrammingStationDraftState.matches(session, 42, 1, 2, 3, null));
    }

    private static ProgrammingStationDraftState loaded() {
        ProgrammingStationDraftState state = new ProgrammingStationDraftState();
        long id = state.begin(Action.LOAD);
        assertTrue(state.receive(id, Action.LOAD, true, draft(0, "original source"), null));
        return state;
    }

    private static ProjectDraft draft(long revision, String source) {
        return new ProjectDraft(PROJECT, OWNER, revision, "original name", source);
    }

    private static void timeout(ProgrammingStationDraftState state) {
        for (int i = 0; i < 200; i++) assertFalse(state.tick());
        assertTrue(state.tick());
        assertFalse(state.tick());
    }
}
