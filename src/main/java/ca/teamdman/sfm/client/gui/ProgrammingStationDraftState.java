package ca.teamdman.sfm.client.gui;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import ca.teamdman.sfm.common.net.PacketStationReply;
import ca.teamdman.sfm.common.net.PacketStationRequest.Action;
import ca.teamdman.sfm.common.program.project.ProjectDraft;
import ca.teamdman.sfm.common.program.project.ProjectValidation;
import ca.teamdman.sfm.common.program.project.PublicationReceipt;

/** Draft identity, dirty guards and late acknowledgments, independent of rendering and networking. */
public final class ProgrammingStationDraftState {

    private static final AtomicLong IDS = new AtomicLong();
    public String name = "Untitled", source = "";
    public UUID localId = UUID.randomUUID();
    public ProjectDraft saved;
    public ProjectValidation validation;
    public PublicationReceipt publication;
    public boolean uncertainWrite, ignoredLateLoad;
    private long requestId;
    private Action action;
    private int ticks;
    private boolean waiting, timedOut;
    private boolean recoveryLoad;
    private String sentName, sentSource;
    private long localEditGeneration, sentEditGeneration;
    private boolean editedAtGenerationLimit;

    /** Includes visual-only edits that leave the last serializable source unchanged. */
    public void noteLocalEdit() {
        if (localEditGeneration < Long.MAX_VALUE) localEditGeneration++;
        else editedAtGenerationLimit = true;
    }

    public boolean dirty() {
        return !name.equals(saved == null ? "Untitled" : saved.name)
            || !source.equals(saved == null ? "" : saved.source);
    }

    public boolean canReplace() {
        return !dirty() && !waiting && !uncertainWrite;
    }

    public boolean busy() {
        return waiting && !timedOut;
    }

    public boolean awaitingReply() {
        return waiting;
    }

    public Action action() {
        return action;
    }

    public long begin(Action next) {
        return begin(next, false);
    }

    public long beginRecoveryLoad() {
        return begin(Action.LOAD, true);
    }

    private long begin(Action next, boolean recovering) {
        if (waiting)
            throw new IllegalStateException("Waiting for the previous request; a timed-out save may still apply.");
        if (next == Action.LOAD && !recovering && !canReplace())
            throw new IllegalStateException("Keep or save the current source and name before loading another project.");
        if ((next == Action.SAVE || next == Action.CREATE) && uncertainWrite) throw new IllegalStateException(
            "Previous save unconfirmed. Copy this draft and inspect the saved project before writing again.");
        if ((next == Action.SAVE || next == Action.VALIDATE
            || next == Action.PUBLISH
            || next == Action.LIST_PUBLICATIONS) && saved == null)
            throw new IllegalStateException("Create a project first.");
        if ((next == Action.VALIDATE || next == Action.PUBLISH) && dirty()) throw new IllegalStateException(
            "Save source and name changes before checking or publishing a saved revision.");
        action = next;
        recoveryLoad = recovering;
        sentName = name;
        sentSource = source;
        sentEditGeneration = localEditGeneration;
        // Saturation never wraps; a further edit still invalidates this request's snapshot.
        editedAtGenerationLimit = false;
        waiting = true;
        timedOut = false;
        ignoredLateLoad = false;
        ticks = 0;
        return requestId = IDS.incrementAndGet();
    }

    public boolean tick() {
        if (!waiting || timedOut || ++ticks <= 200) return false;
        timedOut = true;
        if (action == Action.CREATE || action == Action.SAVE) uncertainWrite = true;
        return true;
    }

    /** A timeout does not cancel the request. A matching late write ack advances only the saved baseline. */
    public boolean receive(long id, Action receivedAction, boolean accepted, ProjectDraft draft,
        ProjectValidation checked) {
        return receive(id, receivedAction, accepted, draft, checked, null);
    }

    public boolean receive(long id, Action receivedAction, boolean accepted, ProjectDraft draft,
        ProjectValidation checked, PublicationReceipt published) {
        if (!waiting || id != requestId || receivedAction != action) return false;
        waiting = false;
        timedOut = false;
        if (action == Action.CREATE || action == Action.SAVE || accepted && action == Action.LOAD)
            uncertainWrite = false;
        if (!accepted) return true;
        if (draft != null && (action == Action.CREATE || action == Action.SAVE || action == Action.LOAD)) {
            boolean edited = localEditGeneration != sentEditGeneration || editedAtGenerationLimit
                || !name.equals(sentName)
                || !source.equals(sentSource);
            if (action == Action.LOAD && edited && !recoveryLoad) {
                ignoredLateLoad = true;
                return true;
            }
            if (saved == null || !saved.owner.equals(draft.owner) || !saved.id.equals(draft.id)) publication = null;
            saved = draft;
            validation = null;
            if (!edited && !recoveryLoad) {
                name = draft.name;
                source = draft.source;
            }
        }
        if (checked != null && saved != null && checked.id.equals(saved.id) && checked.revision == saved.revision)
            validation = checked;
        if (published != null && action == Action.PUBLISH
            && saved != null
            && published.project.equals(saved.id)
            && published.owner.equals(saved.owner)
            && published.revision == saved.revision) publication = published;
        return true;
    }

    public void newDraft() {
        if (!canReplace()) throw new IllegalStateException("Keep or save the current draft before creating another.");
        reset();
    }

    /** Encoding/preflight failed before a packet was sent; this cannot cancel a server operation. */
    public void notSent() {
        waiting = timedOut = false;
        requestId = 0;
    }

    /** The caller must first retain the dirty draft in the bounded recovery cache. */
    public void afterRetainingDraft() {
        if (busy()) throw new IllegalStateException("Wait for the current request before switching projects.");
        reset();
    }

    private void reset() {
        saved = null;
        localId = UUID.randomUUID();
        validation = null;
        publication = null;
        name = "Untitled";
        source = "";
        waiting = timedOut = uncertainWrite = false;
        requestId = 0;
        localEditGeneration = sentEditGeneration = 0;
        editedAtGenerationLimit = false;
    }

    public String revisionLine() {
        String revision = saved == null ? "Not created" : "Saved r" + saved.revision;
        if (dirty()) revision += " | edited";
        if (validation != null)
            revision += " | Validated r" + validation.revision + ": " + (validation.valid ? "OK" : "ERROR");
        else revision += " | not validated";
        if (publication != null) revision += " | Published r" + publication.revision;
        return revision;
    }

    public static boolean matches(UUID session, int window, int x, int y, int z, PacketStationReply reply) {
        return session != null && reply != null
            && session.equals(reply.sessionId)
            && window == reply.windowId
            && x == reply.x
            && y == reply.y
            && z == reply.z;
    }
}
