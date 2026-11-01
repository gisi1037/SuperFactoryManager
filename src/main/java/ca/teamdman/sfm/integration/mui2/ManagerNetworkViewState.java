package ca.teamdman.sfm.integration.mui2;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import ca.teamdman.sfm.common.gui.ManagerControlToken;
import ca.teamdman.sfm.common.program.ManagerNetworkBrowser;
import ca.teamdman.sfm.common.util.BoundedUtf8;

/** One open machine's bounded metadata view. No world, manager, provider or inventory references. */
public final class ManagerNetworkViewState {

    private static final AtomicLong REQUESTS = new AtomicLong();
    private static final int MAX_HISTORY = 4096;

    public static final class Request {

        public final long id, snapshotId;
        public final int cursor, filter;
        public final String query;
        public final ManagerControlToken token;
        private final int direction;

        private Request(long id, long snapshot, int cursor, String query, int filter, ManagerControlToken token,
            int direction) {
            this.id = id;
            snapshotId = snapshot;
            this.cursor = cursor;
            this.query = query;
            this.filter = filter;
            this.token = token;
            this.direction = direction;
        }
    }

    private final Deque<Integer> history = new ArrayDeque<>();
    private ManagerControlToken token;
    private Request pending;
    private ManagerNetworkBrowser.Page page;
    private String activeQuery = "", notice = "Refresh reads cached discovery. Rebuild network discovers connections.";
    private int activeFilter, pendingAt, lastSent = -10;
    private long revision;
    private boolean closed;

    public void control(ManagerControlToken current) {
        if (closed || Objects.equals(token, current)) return;
        token = current;
        pending = null;
        page = null;
        history.clear();
        note("Manager or disk control changed. Refresh for a new read-only snapshot.");
    }

    public Request begin(String query, int filter, int direction, int tick) {
        if (closed || token == null || pending != null) return null;
        if (query == null || query.length() > 64 || filter < 0 || filter > 2 || direction < -1 || direction > 1)
            throw new IllegalArgumentException("Invalid network search or filter");
        BoundedUtf8.encode(query, 256);
        if (tick - lastSent < 5) {
            note("Wait briefly before another network read; cached rows kept.");
            return null;
        }
        if (direction > 0 && !canNext(query, filter) || direction < 0 && !canPrevious(query, filter)) return null;
        long id = REQUESTS.incrementAndGet();
        if (id <= 0) throw new IllegalStateException("Network request identifiers exhausted");
        long snapshot = direction == 0 ? 0 : page.snapshotId;
        int cursor = direction == 0 ? 0 : direction > 0 ? page.nextCursor : history.peekLast();
        pending = new Request(id, snapshot, cursor, query, filter, token, direction);
        pendingAt = lastSent = tick;
        note("Reading bounded cached network metadata...");
        return pending;
    }

    public boolean receive(ManagerControlToken authority, long requestId, boolean accepted, String message,
        ManagerNetworkBrowser.Page result) {
        if (closed || pending == null
            || pending.id != requestId
            || !Objects.equals(token, authority)
            || !pending.token.equals(authority)) return false;
        Request request = pending;
        pending = null;
        if (!accepted || result == null) {
            note(
                (message == null ? "Network read failed." : message)
                    + " Cached rows kept; Refresh starts a new snapshot.");
            return true;
        }
        if (result.snapshotId <= 0 || result.cursor != request.cursor
            || request.snapshotId != 0 && result.snapshotId != request.snapshotId) {
            note("Mismatched network snapshot; cached rows kept. Refresh to retry.");
            return true;
        }
        if (request.direction == 0) history.clear();
        else if (request.direction > 0 && page != null) {
            if (history.size() == MAX_HISTORY) history.removeFirst();
            history.addLast(page.cursor);
        } else if (request.direction < 0 && !history.isEmpty()) history.removeLast();
        page = result;
        activeQuery = request.query;
        activeFilter = request.filter;
        note(result.message);
        return true;
    }

    public void tick(int now) {
        if (pending != null && now - pendingAt > 200) {
            pending = null;
            note("Network read timed out. Cached rows kept; Refresh to retry.");
        }
    }

    public boolean canNext(String query, int filter) {
        return !closed && pending == null && page != null && page.nextCursor >= 0 && matches(query, filter);
    }

    public boolean canPrevious(String query, int filter) {
        return !closed && pending == null && page != null && !history.isEmpty() && matches(query, filter);
    }

    private boolean matches(String query, int filter) {
        return activeFilter == filter && activeQuery.equals(query);
    }

    public boolean pending() {
        return pending != null;
    }

    public ManagerNetworkBrowser.Page page() {
        return page;
    }

    public String notice() {
        return notice;
    }

    public long revision() {
        return revision;
    }

    public void note(String message) {
        notice = message == null ? "" : message;
        if (revision < Long.MAX_VALUE) revision++;
    }

    public void close() {
        closed = true;
        token = null;
        pending = null;
        page = null;
        history.clear();
    }
}
