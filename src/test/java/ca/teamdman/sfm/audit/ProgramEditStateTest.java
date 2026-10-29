package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.ProgramEditState;
import ca.teamdman.sfm.client.gui.ProgramEditState.Action;
import ca.teamdman.sfm.common.program.ProgramEditSnapshot;

class ProgramEditStateTest {

    private static ProgramEditSnapshot snapshot() throws Exception {
        // Correlation treats snapshots as opaque immutable values; no game classes are initialized.
        return AuditRegressionTest.allocate(ProgramEditSnapshot.class);
    }

    @Test
    void onlyMatchingAcknowledgmentsLoadAndRefreshTheSession() throws Exception {
        ProgramEditState state = new ProgramEditState();
        assertFalse(state.isEditable());
        assertThrows(IllegalStateException.class, () -> state.begin(Action.SAVE));
        long open = state.begin(Action.OPEN);
        ProgramEditSnapshot first = snapshot();
        assertFalse(state.receive(open + 1, true, first));
        assertTrue(state.isPending());
        assertNull(state.snapshot());
        assertTrue(state.receive(open, true, first));
        assertTrue(state.isEditable());
        assertSame(first, state.snapshot());
        long save = state.begin(Action.SAVE);
        assertThrows(IllegalStateException.class, () -> state.begin(Action.SAVE));
        assertFalse(state.isEditable());
        assertFalse(state.receive(open, true, first));
        ProgramEditSnapshot fresh = snapshot();
        assertTrue(state.receive(save, true, fresh));
        assertSame(fresh, state.snapshot());
        assertTrue(state.isEditable());
        assertTrue(state.begin(Action.SAVE) > save);
    }

    @Test
    void rejectedAndTimedOutSavesKeepThePreviousSnapshotAndIgnoreLateReplies() throws Exception {
        ProgramEditState state = new ProgramEditState();
        ProgramEditSnapshot original = snapshot();
        state.receive(state.begin(Action.OPEN), true, original);
        long rejected = state.begin(Action.SAVE);
        assertTrue(state.receive(rejected, false, null));
        assertSame(original, state.snapshot());
        assertTrue(state.isEditable());
        long timedOut = state.begin(Action.SAVE);
        for (int i = 0; i < 200; i++) assertFalse(state.tick());
        assertTrue(state.tick());
        assertSame(original, state.snapshot());
        assertTrue(state.isEditable());
        long newer = state.begin(Action.SAVE);
        assertFalse(state.receive(timedOut, true, snapshot()));
        assertTrue(state.isPending());
        state.cancel();
        assertFalse(state.receive(newer, true, snapshot()));
        assertSame(original, state.snapshot());
    }

    @Test
    void openingFailureDoesNotEnableEditingAndRequestIdsSpanScreens() {
        ProgramEditState first = new ProgramEditState();
        long request = first.begin(Action.OPEN);
        assertTrue(first.receive(request, false, null));
        assertFalse(first.isEditable());
        ProgramEditState second = new ProgramEditState();
        assertTrue(second.begin(Action.OPEN) > request);
        assertFalse(second.receive(request, true, null));
        assertTrue(second.isPending());
    }
}
