package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.ProgrammingStationDraftState;
import ca.teamdman.sfm.client.gui.mui2.Mui2ProgrammingStation;
import ca.teamdman.sfm.common.net.PacketStationReply;
import ca.teamdman.sfm.common.net.PacketStationRequest.Action;
import ca.teamdman.sfm.common.program.project.ProjectDraft;
import ca.teamdman.sfm.common.program.project.ProjectValidation;
import ca.teamdman.sfm.common.program.project.PublicationReceipt;
import ca.teamdman.sfm.common.program.project.PublishedProgram;

class ProgrammingStationDraftStateTest {

    private static final UUID OWNER = new UUID(1, 2), PROJECT = new UUID(3, 4);

    @Test
    void discardingUnsavedEditsClearsOnlyLocalIdentityAndLeavesTheSavedProjectUntouched() {
        ProgrammingStationDraftState state = loaded();
        ProjectDraft saved = state.saved;
        UUID local = state.localId;
        state.name = "unsaved name";
        state.source = "unsaved source";
        state.discardDraft();
        assertTrue(state.canReplace());
        assertNull(state.saved);
        assertEquals("Untitled", state.name);
        assertEquals("", state.source);
        assertNotEquals(local, state.localId);
        assertEquals("original name", saved.name);
        assertEquals("original source", saved.source);
        long request = state.begin(Action.LOAD);
        assertTrue(state.receive(request, Action.LOAD, true, saved, null));
        assertEquals("original source", state.source);
    }

    @Test
    void discardCannotForgetAnInFlightOrUnconfirmedSave() {
        ProgrammingStationDraftState state = loaded();
        state.source = "unsaved source";
        long request = state.begin(Action.SAVE);
        assertThrows(IllegalStateException.class, state::discardDraft);
        timeout(state);
        assertThrows(IllegalStateException.class, state::discardDraft);
        assertEquals("unsaved source", state.source);
        assertTrue(state.receive(request, Action.SAVE, false, null, null));
        state.uncertainWrite = true;
        assertThrows(IllegalStateException.class, state::discardDraft);
        state.uncertainWrite = false;
        assertDoesNotThrow(state::discardDraft);
    }

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
    void publicationAndHistoryRequireSavedIdentityAndPublishRequiresCleanNameAndSource() {
        ProgrammingStationDraftState unsaved = new ProgrammingStationDraftState();
        assertThrows(IllegalStateException.class, () -> unsaved.begin(Action.PUBLISH));
        assertThrows(IllegalStateException.class, () -> unsaved.begin(Action.LIST_PUBLICATIONS));
        ProgrammingStationDraftState state = loaded();
        ProgrammingStationDraftState savedState = state;
        state.source = "unsaved";
        assertThrows(IllegalStateException.class, () -> savedState.begin(Action.PUBLISH));
        long list = state.begin(Action.LIST_PUBLICATIONS);
        assertTrue(state.receive(list, Action.LIST_PUBLICATIONS, true, null, null));
        assertEquals("unsaved", state.source, "History can be viewed while retaining unsaved source");
        state.source = state.saved.source;
        state.name = "unsaved name";
        assertThrows(IllegalStateException.class, () -> savedState.begin(Action.PUBLISH));
    }

    @Test
    void publicationReceiptMustMatchExactOwnerProjectRevisionAndRequest() {
        ProgrammingStationDraftState state = loaded();
        PublicationReceipt correct = receipt(OWNER, PROJECT, 0);
        long id = state.begin(Action.PUBLISH);
        assertFalse(state.receive(id + 1, Action.PUBLISH, true, null, null, correct));
        assertFalse(state.receive(id, Action.VALIDATE, true, null, null, correct));
        assertNull(state.publication);
        assertTrue(state.awaitingReply());
        assertTrue(state.receive(id, Action.PUBLISH, true, null, null, correct));
        assertSame(correct, state.publication);
        assertTrue(
            state.revisionLine()
                .contains("Published r0"));
        for (PublicationReceipt wrong : new PublicationReceipt[] { receipt(UUID.randomUUID(), PROJECT, 0),
            receipt(OWNER, UUID.randomUUID(), 0), receipt(OWNER, PROJECT, 1) }) {
            ProgrammingStationDraftState fresh = loaded();
            long request = fresh.begin(Action.PUBLISH);
            assertTrue(fresh.receive(request, Action.PUBLISH, true, null, null, wrong));
            assertNull(fresh.publication);
            assertEquals("original source", fresh.source);
            assertEquals(0, fresh.saved.revision);
        }
        ProgrammingStationDraftState nonPublish = loaded();
        long validate = nonPublish.begin(Action.VALIDATE);
        assertTrue(nonPublish.receive(validate, Action.VALIDATE, true, null, null, correct));
        assertNull(nonPublish.publication);
    }

    @Test
    void timedOutPublicationAcceptsExactLateReceiptWithoutReplacingNewerLocalEdits() {
        ProgrammingStationDraftState state = loaded();
        ProjectDraft saved = state.saved;
        long id = state.begin(Action.PUBLISH);
        timeout(state);
        assertFalse(state.uncertainWrite, "Publishing never changes the saved draft");
        assertTrue(state.awaitingReply());
        state.source = "new local source";
        state.name = "new local name";
        PublicationReceipt receipt = receipt(OWNER, PROJECT, 0);
        assertTrue(state.receive(id, Action.PUBLISH, true, null, null, receipt));
        assertSame(saved, state.saved);
        assertSame(receipt, state.publication);
        assertEquals("new local source", state.source);
        assertEquals("new local name", state.name);
        assertTrue(state.dirty());
        assertFalse(state.receive(id, Action.PUBLISH, true, null, null, receipt));
    }

    @Test
    void rejectedPublishKeepsSavedDraftAndPreviousReceiptAndResetClearsPublication() {
        ProgrammingStationDraftState state = loaded();
        PublicationReceipt previous = receipt(OWNER, PROJECT, 0);
        state.publication = previous;
        ProjectDraft saved = state.saved;
        long id = state.begin(Action.PUBLISH);
        assertTrue(state.receive(id, Action.PUBLISH, false, null, null, null));
        assertSame(saved, state.saved);
        assertSame(previous, state.publication);
        assertEquals("original source", state.source);
        assertEquals("original name", state.name);
        state.newDraft();
        assertNull(state.publication);
    }

    @Test
    void loadingDifferentProjectClearsPreviousProjectsPublicationReceipt() {
        ProgrammingStationDraftState state = loaded();
        state.publication = receipt(OWNER, PROJECT, 0);
        long id = state.begin(Action.LOAD);
        ProjectDraft other = new ProjectDraft(UUID.randomUUID(), OWNER, 0, "Other", "other source");
        assertTrue(state.receive(id, Action.LOAD, true, other, null));
        assertSame(other, state.saved);
        assertNull(state.publication, "Published status must belong to the selected project");
        assertFalse(
            state.revisionLine()
                .contains("Published"));
    }

    private static PublicationReceipt receipt(UUID owner, UUID project, long revision) {
        return new PublicationReceipt(
            owner,
            project,
            revision,
            new PublishedProgram("EVERY 20 TICKS DO END").digest(),
            "original name");
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

    @Test
    void heldDiskWritingIsDiscoverableWhileServerTransactionsRetainAuthority() {
        assertTrue(Mui2ProgrammingStation.REFERENCE_DISKS_ENABLED);
    }

    @Test
    void diskWriteRequiresExplicitCurrentProjectSelectionAndAllowsAnOlderBuildWithDirtyDraft() {
        ProgrammingStationDraftState unsaved = new ProgrammingStationDraftState();
        assertThrows(IllegalStateException.class, () -> unsaved.begin(Action.WRITE_DISK));
        ProgrammingStationDraftState state = loaded();
        assertThrows(IllegalStateException.class, () -> state.begin(Action.WRITE_DISK));
        assertThrows(IllegalArgumentException.class, () -> state.selectBuild(receipt(UUID.randomUUID(), PROJECT, 0)));
        assertThrows(IllegalArgumentException.class, () -> state.selectBuild(receipt(OWNER, UUID.randomUUID(), 0)));
        long save = state.begin(Action.SAVE);
        assertTrue(state.receive(save, Action.SAVE, true, draft(7, "new saved source"), null));
        PublicationReceipt old = receipt(OWNER, PROJECT, 0);
        state.selectBuild(old);
        assertSame(old, state.selectedBuild());
        assertTrue(state.isSelectedBuild(receipt(OWNER, PROJECT, 0)));
        assertFalse(state.isSelectedBuild(receipt(OWNER, PROJECT, 7)));
        state.source = "newer local draft";
        state.name = "newer local name";
        long write = state.begin(Action.WRITE_DISK);
        assertEquals(0, state.selectedBuild().revision, "Disk selection is independent of saved r7");
        assertEquals("newer local draft", state.source);
        assertThrows(IllegalStateException.class, () -> state.selectBuild(receipt(OWNER, PROJECT, 7)));
        assertTrue(state.receive(write, Action.WRITE_DISK, true, null, null));
        assertSame(old, state.selectedBuild());
        assertEquals(7, state.saved.revision);
        assertTrue(state.dirty());
    }

    @Test
    void diskWriteAcknowledgmentsAndErrorsNeverChangeSourceSavedBaselineValidationOrPublication() {
        for (boolean accepted : new boolean[] { false, true }) {
            ProgrammingStationDraftState state = loaded();
            ProjectDraft baseline = state.saved;
            PublicationReceipt selected = receipt(OWNER, PROJECT, 0);
            state.selectBuild(selected);
            state.publication = selected;
            state.validation = new ProjectValidation(PROJECT, 0, true, "Valid");
            ProjectValidation previousValidation = state.validation;
            long write = state.begin(Action.WRITE_DISK);
            state.name = "name edited after sending";
            state.source = "source edited after sending";
            assertFalse(state.receive(write + 1, Action.WRITE_DISK, accepted, null, null));
            assertFalse(state.receive(write, Action.SAVE, accepted, null, null));
            assertTrue(
                state.receive(
                    write,
                    Action.WRITE_DISK,
                    accepted,
                    draft(8, "must not install"),
                    new ProjectValidation(PROJECT, 0, false, "must not replace"),
                    receipt(OWNER, PROJECT, 8)));
            assertSame(baseline, state.saved);
            assertSame(selected, state.selectedBuild());
            assertSame(selected, state.publication);
            assertSame(previousValidation, state.validation);
            assertEquals("name edited after sending", state.name);
            assertEquals("source edited after sending", state.source);
            assertFalse(state.uncertainWrite);
            assertFalse(state.awaitingReply());
        }
    }

    @Test
    void diskWriteTimeoutWarnsToInspectHeldDiskKeepsCorrelationAndNeverRetriesAutomatically() {
        ProgrammingStationDraftState state = loaded();
        state.selectBuild(receipt(OWNER, PROJECT, 0));
        ProjectDraft baseline = state.saved;
        long write = state.begin(Action.WRITE_DISK);
        timeout(state);
        assertTrue(state.awaitingReply());
        assertFalse(state.busy());
        assertFalse(state.uncertainWrite, "Disk-write uncertainty does not change project-save uncertainty");
        assertTrue(
            state.timeoutMessage()
                .contains("Check the held disk"));
        assertTrue(
            state.timeoutMessage()
                .contains("do not write another disk"));
        assertThrows(IllegalStateException.class, () -> state.begin(Action.WRITE_DISK));
        state.source = "local source after timeout";
        assertTrue(state.receive(write, Action.WRITE_DISK, true, null, null));
        assertSame(baseline, state.saved);
        assertEquals("local source after timeout", state.source);
        assertFalse(state.receive(write, Action.WRITE_DISK, true, null, null));
    }

    @Test
    void buildSelectionSurvivesSameProjectSaveAndClearsOnProjectSwitchOrLocalReset() {
        ProgrammingStationDraftState state = loaded();
        PublicationReceipt selected = receipt(OWNER, PROJECT, 0);
        state.selectBuild(selected);
        long save = state.begin(Action.SAVE);
        assertTrue(state.receive(save, Action.SAVE, true, draft(1, "saved edit"), null));
        assertSame(selected, state.selectedBuild());
        long load = state.begin(Action.LOAD);
        ProjectDraft different = new ProjectDraft(UUID.randomUUID(), OWNER, 0, "Different", "source");
        assertTrue(state.receive(load, Action.LOAD, true, different, null));
        assertNull(state.selectedBuild());
        assertThrows(IllegalStateException.class, () -> state.begin(Action.WRITE_DISK));
        state.selectBuild(receipt(OWNER, different.id, 0));
        state.newDraft();
        assertNull(state.selectedBuild());
        assertNull(state.saved);
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
