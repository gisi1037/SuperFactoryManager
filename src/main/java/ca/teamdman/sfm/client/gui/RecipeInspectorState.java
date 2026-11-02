package ca.teamdman.sfm.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import ca.teamdman.sfm.common.net.PacketStationRecipeReply;
import ca.teamdman.sfm.common.net.PacketStationRecipeRequest;
import ca.teamdman.sfml.data.recipe.RecipeCatalog;

/** Inspection state has no source/document authority. Delayed replies cannot select or edit a new search. */
public final class RecipeInspectorState {

    public static final int TIMEOUT_TICKS = 120;
    private static final String[] NO_DETAIL = new String[0];
    private final int x, y, z, window;
    private final UUID session;
    private long sequence;
    private int remaining;
    private PacketStationRecipeRequest pending;
    private RecipeSelectionHint.Value output;
    private PacketStationRecipeReply.Entry selected;
    private List<PacketStationRecipeReply.Entry> entries = Collections.emptyList();
    private String status = "Use a remembered NEI hint or enter an exact null-NBT output.", detail = "";
    private String[] detailLines = NO_DETAIL;

    public RecipeInspectorState(int x, int y, int z, int window, UUID session) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.window = window;
        this.session = session;
    }

    public List<PacketStationRecipeReply.Entry> entries() {
        return entries;
    }

    public String status() {
        return status;
    }

    public String detail() {
        return detail;
    }

    public int detailLineCount() {
        return detailLines.length;
    }

    public String detailLine(int index) {
        return index >= 0 && index < detailLines.length ? detailLines[index] : "";
    }

    private void setDetail(String text) {
        detail = text;
        detailLines = text.isEmpty() ? NO_DETAIL : text.split("\n", -1);
    }

    public boolean busy() {
        return pending != null;
    }

    public boolean canCopy() {
        return selected != null && !busy();
    }

    public boolean selected(int index) {
        return index >= 0 && index < entries.size()
            && selected != null
            && selected.reference.fingerprint.equals(entries.get(index).reference.fingerprint);
    }

    public void clear() {
        pending = null;
        selected = null;
        output = null;
        entries = Collections.emptyList();
        setDetail("");
        status = "Search changed; Lookup again.";
    }

    public void cancel() {
        pending = null;
        selected = null;
        setDetail("");
    }

    public void tick() {
        if (pending != null && --remaining <= 0) {
            pending = null;
            selected = null;
            setDetail("");
            status = "Inspection timed out. Lookup or select again.";
        }
    }

    public PacketStationRecipeRequest search(RecipeSelectionHint.Value value) {
        clear();
        output = value;
        return begin(PacketStationRecipeRequest.Action.SEARCH, null);
    }

    public PacketStationRecipeRequest select(int index) {
        if (busy() || index < 0 || index >= entries.size() || output == null)
            throw new IllegalStateException("Choose a current server candidate");
        selected = null;
        setDetail("");
        return begin(PacketStationRecipeRequest.Action.SELECT, entries.get(index).reference);
    }

    private PacketStationRecipeRequest begin(PacketStationRecipeRequest.Action action, RecipeCatalog.Token reference) {
        if (output == null || sequence == Long.MAX_VALUE)
            throw new IllegalStateException("Reopen the station inspector");
        PacketStationRecipeRequest request = new PacketStationRecipeRequest();
        request.x = x;
        request.y = y;
        request.z = z;
        request.windowId = window;
        request.sessionId = session;
        request.requestId = ++sequence;
        request.action = action;
        request.item = output.item;
        request.metadata = output.metadata;
        request.map = output.map;
        request.reference = reference;
        request.validate();
        pending = request;
        remaining = TIMEOUT_TICKS;
        status = action == PacketStationRecipeRequest.Action.SEARCH ? "Querying server catalog..."
            : "Revalidating selected server recipe...";
        return request;
    }

    public boolean receive(PacketStationRecipeReply reply) {
        if (reply == null || !reply.matches(pending)) return false;
        PacketStationRecipeRequest request = pending;
        pending = null;
        if (reply.entries.size() > PacketStationRecipeReply.MAX_ROWS) {
            clear();
            status = "Invalid inspection response";
            return false;
        }
        status = reply.status.name()
            .toLowerCase(Locale.ROOT) + " | " + reply.message;
        selected = null;
        if (request.action == PacketStationRecipeRequest.Action.SEARCH) {
            entries = reply.accepted ? Collections.unmodifiableList(new ArrayList<>(reply.entries))
                : Collections.emptyList();
            setDetail("");
            status = "Candidates: " + entries.size() + " | complete=" + reply.coverageComplete + " | " + status;
        } else {
            setDetail(reply.detail);
            if (reply.accepted && reply.status == RecipeCatalog.Status.MATCHES
                && reply.coverageComplete
                && reply.entries.size() == 1) {
                PacketStationRecipeReply.Entry entry = reply.entries.get(0);
                if (same(request.reference, entry.reference)) selected = entry;
                else {
                    setDetail("");
                    status = "Selection changed. Lookup again.";
                }
            }
        }
        return true;
    }

    private static boolean same(RecipeCatalog.Token a, RecipeCatalog.Token b) {
        return a != null && b != null
            && a.session.equals(b.session)
            && a.revision == b.revision
            && a.adapter.equals(b.adapter)
            && a.map.equals(b.map)
            && a.fingerprint.equals(b.fingerprint);
    }

    public String copyQuery() {
        if (!canCopy()) throw new IllegalStateException("Select and validate a complete server recipe first");
        return RecipeSelectionHint.query(output, selected.reference.fingerprint);
    }
}
