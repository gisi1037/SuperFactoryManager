package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.gui.ManagerControlToken;
import ca.teamdman.sfm.common.program.ManagerNetworkBrowser.Page;
import ca.teamdman.sfm.common.program.ManagerNetworkBrowser.Row;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfm.integration.mui2.ManagerMachineLayout;
import ca.teamdman.sfm.integration.mui2.ManagerNetworkViewState;
import ca.teamdman.sfm.integration.mui2.ManagerNetworkViewState.Request;

class ManagerNetworkViewStateTest {

    private static final ManagerControlToken TOKEN = new ManagerControlToken(new UUID(1, 2), 3, 1);

    @Test
    void progressiveEmptyPagesRemainNavigableAndPreviousReturnsThePriorCursor() {
        ManagerNetworkViewState view = view();
        Request first = view.begin("rare", 1, 0, 0);
        assertEquals(0, first.snapshotId);
        assertTrue(view.receive(TOKEN, first.id, true, "", page(8, 0, 128, true)));
        assertTrue(view.page().rows.isEmpty());
        assertTrue(view.canNext("rare", 1));
        Request next = view.begin("rare", 1, 1, 5);
        assertEquals(8, next.snapshotId);
        assertEquals(128, next.cursor);
        assertTrue(view.receive(TOKEN, next.id, true, "", page(8, 128, -1, false)));
        assertFalse(view.canNext("rare", 1));
        assertTrue(view.canPrevious("rare", 1));
        Request previous = view.begin("rare", 1, -1, 10);
        assertEquals(0, previous.cursor);
        assertTrue(view.receive(TOKEN, previous.id, true, "", page(8, 0, 128, true)));
        assertFalse(view.canPrevious("rare", 1));
    }

    @Test
    void queryAndFilterChangesRequireAnExplicitFreshSnapshot() {
        ManagerNetworkViewState view = view();
        Request first = view.begin("ore", 0, 0, 0);
        view.receive(TOKEN, first.id, true, "", page(8, 0, 128, false));
        assertFalse(view.canNext("other", 0));
        assertFalse(view.canNext("ore", 2));
        Request refresh = view.begin("other", 2, 0, 5);
        assertEquals(0, refresh.snapshotId);
        assertEquals(0, refresh.cursor);
        assertEquals("other", refresh.query);
        assertEquals(2, refresh.filter);
        view.receive(TOKEN, refresh.id, true, "", page(9, 0, 128, false));
        assertTrue(view.canNext("other", 2));
        assertFalse(view.canPrevious("other", 2));
    }

    @Test
    void errorsTimeoutsAndBadCorrelationPreservePreviouslyAcceptedRows() {
        ManagerNetworkViewState view = view();
        Request first = view.begin("", 0, 0, 0);
        Page accepted = page(8, 0, 128, false);
        view.receive(TOKEN, first.id, true, "", accepted);
        Request next = view.begin("", 0, 1, 5);
        assertFalse(view.receive(TOKEN, next.id + 1, true, "", page(8, 128, -1, false)));
        assertTrue(view.pending());
        assertTrue(view.receive(TOKEN, next.id, false, "Snapshot expired", null));
        assertSame(accepted, view.page());
        assertTrue(
            view.notice()
                .contains("Snapshot expired"));
        assertFalse(view.canPrevious("", 0));
        Request pending = view.begin("", 0, 1, 10);
        view.tick(211);
        assertFalse(view.pending());
        assertSame(accepted, view.page());
        assertTrue(
            view.notice()
                .contains("timed out"));
        assertFalse(view.receive(TOKEN, pending.id, true, "", page(8, 128, -1, false)));
    }

    @Test
    void diskSessionWindowAndClosedScreenFencesClearCachedPagesAndRejectLateReplies() {
        for (ManagerControlToken changed : new ManagerControlToken[] { new ManagerControlToken(TOKEN.sessionId, 4, 1),
            new ManagerControlToken(new UUID(5, 6), 3, 1), new ManagerControlToken(TOKEN.sessionId, 3, 2) }) {
            ManagerNetworkViewState view = view();
            Request first = view.begin("", 0, 0, 0);
            view.receive(TOKEN, first.id, true, "", page(8, 0, 128, false));
            assertNotNull(view.page());
            Request request = view.begin("", 0, 1, 5);
            view.control(changed);
            assertNull(view.page());
            assertFalse(view.pending());
            assertFalse(view.receive(TOKEN, request.id, true, "", page(8, 128, -1, false)));
        }
        ManagerNetworkViewState closed = view();
        Request old = closed.begin("", 0, 0, 0);
        closed.close();
        assertFalse(closed.receive(TOKEN, old.id, true, "", page(8, 0, 128, false)));
        ManagerNetworkViewState reopened = view();
        Request fresh = reopened.begin("", 0, 0, 0);
        assertNotEquals(old.id, fresh.id);
        assertFalse(reopened.receive(TOKEN, old.id, true, "", page(8, 0, 128, false)));
    }

    @Test
    void admissionIsSinglePendingAndLocallyThrottledAndSnapshotCursorsAreFenced() {
        ManagerNetworkViewState view = view();
        Request request = view.begin("", 0, 0, 0);
        assertNull(view.begin("", 0, 0, 10));
        view.receive(TOKEN, request.id, true, "", page(8, 0, 128, false));
        assertNull(view.begin("", 0, 0, 4));
        Request next = view.begin("", 0, 1, 5);
        Page previous = view.page();
        view.receive(TOKEN, next.id, true, "", page(9, 128, -1, false));
        assertSame(previous, view.page());
        assertTrue(
            view.notice()
                .contains("Mismatched"));
        assertThrows(
            IllegalArgumentException.class,
            () -> view.begin(String.join("", Collections.nCopies(65, "x")), 0, 0, 10));
    }

    @Test
    void compactMachineRetainsASeparateClippedNetworkOutputAboveInventory() {
        int body = ManagerMachineLayout.contentHeight(240);
        assertEquals(70, body);
        assertTrue(36 + 14 < body, "two toolbar rows and footer leave a scrollable output region");
        assertEquals(232, ManagerMachineLayout.height(240));
    }

    private static ManagerNetworkViewState view() {
        ManagerNetworkViewState view = new ManagerNetworkViewState();
        view.control(TOKEN);
        return view;
    }

    private static Page page(long snapshot, int cursor, int next, boolean empty) {
        return new Page(
            snapshot,
            cursor,
            next,
            256,
            20,
            10,
            true,
            false,
            "Cached metadata only",
            empty ? Collections.emptyList()
                : Collections.singletonList(new Row(new BlockPos(1, 2, 3), "Chest", "source", 1, 0)));
    }
}
