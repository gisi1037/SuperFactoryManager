package ca.teamdman.sfm.client.gui;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import ca.teamdman.sfm.common.net.PacketStationReply;
import ca.teamdman.sfm.common.net.PacketStationRequest.Action;
import ca.teamdman.sfm.common.program.project.ProjectDraft;
import ca.teamdman.sfm.common.program.project.ProjectValidation;
import ca.teamdman.sfm.common.program.project.PublicationReceipt;
import ca.teamdman.sfm.common.program.project.PublicationSource;

/** Draft identity, dirty guards and late acknowledgments, independent of rendering and networking. */
public final class ProgrammingStationDraftState {

    private static final AtomicLong IDS = new AtomicLong();
    public String name = "Untitled", source = "";
    public UUID localId = UUID.randomUUID();
    public ProjectDraft saved;
    public ProjectValidation validation;
    public PublicationReceipt publication;
    public PublicationReceipt libraryPublication;
    private PublicationReceipt selectedBuild;
    public boolean uncertainWrite, ignoredLateLoad, ignoredLateRestore;
    private long requestId;
    private Action action;
    private int ticks;
    private boolean waiting, timedOut;
    private boolean archiveRequest;
    private boolean recoveryLoad;
    private String sentName, sentSource;
    private long localEditGeneration, sentEditGeneration;
    private ProjectDraft sentSaved;
    private PublicationReceipt sentBuild;
    private boolean editedAtGenerationLimit;

    /** A transient picker may inspect other projects, but may edit only the exact draft it opened over. */
    public static final class DraftSnapshot {

        private final UUID local;
        private final ProjectDraft saved;
        private final String name, source;
        private final long generation;

        private DraftSnapshot(ProgrammingStationDraftState state) {
            local = state.localId;
            saved = state.saved;
            name = state.name;
            source = state.source;
            generation = state.localEditGeneration;
        }
    }

    public DraftSnapshot snapshot() {
        return new DraftSnapshot(this);
    }

    public boolean current(DraftSnapshot snapshot) {
        return snapshot != null && localEditGeneration != Long.MAX_VALUE
            && snapshot.generation == localEditGeneration
            && snapshot.local.equals(localId)
            && snapshot.saved == saved
            && snapshot.name.equals(name)
            && snapshot.source.equals(source);
    }

    /** Includes visual-only edits that leave the last serializable source unchanged. */
    public void noteLocalEdit() {
        if (localEditGeneration < Long.MAX_VALUE) localEditGeneration++;
        else editedAtGenerationLimit = true;
    }

    public boolean dirty() {
        return !name.equals(saved == null ? "Untitled" : saved.name)
            || !source.equals(saved == null ? "" : saved.source);
    }

    /** A server result describes the editor only while its exact saved name/source/revision still match. */
    public ProjectValidation currentValidation() {
        return !dirty() && !uncertainWrite
            && saved != null
            && validation != null
            && validation.id.equals(saved.id)
            && validation.revision == saved.revision ? validation : null;
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

    public boolean matchesPending(long id, Action receivedAction) {
        return waiting && id == requestId && receivedAction == action;
    }

    public Action action() {
        return action;
    }

    public PublicationReceipt selectedBuild() {
        return selectedBuild;
    }

    public void selectBuild(PublicationReceipt receipt) {
        if (waiting) throw new IllegalStateException("Wait for the current request before selecting another build.");
        if (receipt == null || saved == null || !receipt.owner.equals(saved.owner) || !receipt.project.equals(saved.id))
            throw new IllegalArgumentException("Select a published build from the current project.");
        selectedBuild = receipt;
    }

    public boolean isSelectedBuild(PublicationReceipt receipt) {
        return receipt != null && selectedBuild != null
            && receipt.owner.equals(selectedBuild.owner)
            && receipt.project.equals(selectedBuild.project)
            && receipt.revision == selectedBuild.revision
            && receipt.digest.equals(selectedBuild.digest);
    }

    /** Timeout never cancels or retries a server write, including writing a held physical disk. */
    public String timeoutMessage() {
        if (action == Action.DELETE) return "No delete confirmation; it may have applied. Current draft kept. "
            + "A late reply is accepted; reopen Projects to inspect before retrying.";
        if (archiveRequest) return "No transfer confirmation. Import may be partial; do not retry blindly. "
            + "Current draft kept. A late reply is still accepted; reopen and inspect projects if needed.";
        if (action == Action.WRITE_DISK)
            return "No disk-write confirmation. Check the held disk before retrying; do not write another disk. "
                + "Draft kept; a late reply is still accepted.";
        return action == Action.CREATE || action == Action.SAVE
            ? "No confirmation; save may have applied. Draft kept. Copy or close to recover later."
            : "No server reply. Draft kept; reopen to retry. A late reply is still accepted here.";
    }

    public long begin(Action next) {
        return begin(next, false);
    }

    /** Transfers borrow only the correlated pending slot, never the editor's saved/dirty baseline. */
    public long beginArchive(Action next) {
        if (next != Action.LIST && next != Action.ARCHIVE_LOAD && next != Action.ARCHIVE_CREATE)
            throw new IllegalArgumentException("Unsupported archive request");
        if (next == Action.ARCHIVE_CREATE && uncertainWrite)
            throw new IllegalStateException("Reconcile the previous unconfirmed save before importing");
        long id = begin(next, true);
        archiveRequest = true;
        return id;
    }

    public boolean receiveArchive(long id, Action next) {
        if (!archiveRequest || !matchesPending(id, next)) return false;
        waiting = timedOut = archiveRequest = false;
        return true;
    }

    public long beginRecoveryLoad() {
        return begin(Action.LOAD, true);
    }

    /** Caller has explicitly retained or discarded the current dirty/visual draft in the restore guard. */
    public long beginGuardedBuildRestore() {
        return begin(Action.RESTORE_BUILD, true);
    }

    private long begin(Action next, boolean recovering) {
        if (waiting)
            throw new IllegalStateException("Waiting for the previous request; a timed-out save may still apply.");
        if (next == Action.DELETE && uncertainWrite)
            throw new IllegalStateException("Reconcile the unconfirmed save before deleting a project.");
        if (next == Action.LOAD && !recovering && !canReplace())
            throw new IllegalStateException("Keep or save the current source and name before loading another project.");
        if (next == Action.RESTORE_BUILD && (!recovering && !canReplace() || uncertainWrite))
            throw new IllegalStateException(
                "Keep or discard the current draft and reconcile unconfirmed saves before restoring a build.");
        if ((next == Action.SAVE || next == Action.CREATE) && uncertainWrite) throw new IllegalStateException(
            "Previous save unconfirmed. Copy this draft and inspect the saved project before writing again.");
        if ((next == Action.SAVE || next == Action.VALIDATE
            || next == Action.PUBLISH
            || next == Action.PUBLISH_LIBRARY
            || next == Action.LIST_PUBLICATIONS
            || next == Action.WRITE_DISK
            || next == Action.RESTORE_BUILD) && saved == null)
            throw new IllegalStateException("Create a project first.");
        if ((next == Action.VALIDATE || next == Action.PUBLISH || next == Action.PUBLISH_LIBRARY) && dirty())
            throw new IllegalStateException(
                "Save source and name changes before checking or publishing a saved revision.");
        if (next == Action.PUBLISH_LIBRARY && uncertainWrite)
            throw new IllegalStateException("Reconcile the unconfirmed save before publishing a library.");
        if ((next == Action.WRITE_DISK || next == Action.RESTORE_BUILD)
            && (selectedBuild == null || !selectedBuild.owner.equals(saved.owner)
                || !selectedBuild.project.equals(saved.id)))
            throw new IllegalStateException("Select an immutable published build before writing the held disk.");
        action = next;
        archiveRequest = false;
        recoveryLoad = next == Action.LOAD && recovering;
        sentName = name;
        sentSource = source;
        sentEditGeneration = localEditGeneration;
        sentSaved = saved;
        sentBuild = selectedBuild;
        // Saturation never wraps; a further edit still invalidates this request's snapshot.
        editedAtGenerationLimit = false;
        waiting = true;
        timedOut = false;
        ignoredLateLoad = false;
        ignoredLateRestore = false;
        ticks = 0;
        return requestId = IDS.incrementAndGet();
    }

    public boolean tick() {
        if (!waiting || timedOut || ++ticks <= 200) return false;
        timedOut = true;
        if (!archiveRequest && (action == Action.CREATE || action == Action.SAVE)) uncertainWrite = true;
        return true;
    }

    /** A timeout does not cancel the request. A matching late write ack advances only the saved baseline. */
    public boolean receive(long id, Action receivedAction, boolean accepted, ProjectDraft draft,
        ProjectValidation checked) {
        return receive(id, receivedAction, accepted, draft, checked, null);
    }

    public boolean receive(long id, Action receivedAction, boolean accepted, ProjectDraft draft,
        ProjectValidation checked, PublicationReceipt published) {
        return receive(id, receivedAction, accepted, draft, checked, published, null);
    }

    public boolean receive(long id, Action receivedAction, boolean accepted, ProjectDraft draft,
        ProjectValidation checked, PublicationReceipt published, PublicationSource restored) {
        if (archiveRequest || !matchesPending(id, receivedAction)) return false;
        waiting = false;
        timedOut = false;
        if (action == Action.CREATE || action == Action.SAVE || accepted && action == Action.LOAD)
            uncertainWrite = false;
        if (!accepted || action == Action.WRITE_DISK) return true;
        if (action == Action.RESTORE_BUILD) {
            boolean edited = localEditGeneration != sentEditGeneration || editedAtGenerationLimit
                || !name.equals(sentName)
                || !source.equals(sentSource);
            if (restored == null || sentBuild == null
                || sentSaved == null
                || saved == null
                || edited
                || !saved.owner.equals(sentSaved.owner)
                || !saved.id.equals(sentSaved.id)
                || saved.revision != sentSaved.revision
                || !saved.name.equals(sentSaved.name)
                || !saved.source.equals(sentSaved.source)
                || !sameBuild(sentBuild, selectedBuild)
                || !sameBuild(sentBuild, restored.receipt)) {
                ignoredLateRestore = true;
                return true;
            }
            source = restored.source;
            validation = null;
            noteLocalEdit();
            return true;
        }
        if (draft != null && (action == Action.CREATE || action == Action.SAVE || action == Action.LOAD)) {
            boolean edited = localEditGeneration != sentEditGeneration || editedAtGenerationLimit
                || !name.equals(sentName)
                || !source.equals(sentSource);
            if (action == Action.LOAD && edited && !recoveryLoad) {
                ignoredLateLoad = true;
                return true;
            }
            if (saved == null || !saved.owner.equals(draft.owner) || !saved.id.equals(draft.id)) {
                publication = null;
                libraryPublication = null;
                selectedBuild = null;
            }
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
        if (published != null && action == Action.PUBLISH_LIBRARY
            && saved != null
            && sentSaved != null
            && saved == sentSaved
            && published.project.equals(saved.id)
            && published.owner.equals(saved.owner)
            && published.revision == saved.revision) libraryPublication = published;
        return true;
    }

    public void newDraft() {
        if (!canReplace()) throw new IllegalStateException("Keep or save the current draft before creating another.");
        reset();
    }

    /** Encoding/preflight failed before a packet was sent; this cannot cancel a server operation. */
    public void notSent() {
        archiveRequest = false;
        waiting = timedOut = false;
        requestId = 0;
    }

    /** The caller must first retain the dirty draft in the bounded recovery cache. */
    public void afterRetainingDraft() {
        if (busy()) throw new IllegalStateException("Wait for the current request before switching projects.");
        reset();
    }

    /** Discard only the local draft; an unconfirmed server write must be reconciled first. */
    public void discardDraft() {
        if (waiting || uncertainWrite)
            throw new IllegalStateException("Resolve the pending server request before discarding this draft.");
        reset();
    }

    /** Deleting a saved project never discards the open buffer or silently recreates the old identity. */
    public boolean detachDeletedProject(UUID id) {
        if (saved == null || !saved.id.equals(id)) return false;
        if (waiting) throw new IllegalStateException("Wait for delete confirmation before detaching the draft.");
        saved = null;
        localId = UUID.randomUUID();
        validation = null;
        publication = libraryPublication = selectedBuild = null;
        uncertainWrite = false;
        noteLocalEdit();
        return true;
    }

    private void reset() {
        saved = null;
        localId = UUID.randomUUID();
        validation = null;
        publication = null;
        libraryPublication = null;
        selectedBuild = null;
        name = "Untitled";
        source = "";
        waiting = timedOut = uncertainWrite = false;
        archiveRequest = false;
        requestId = 0;
        localEditGeneration = sentEditGeneration = 0;
        editedAtGenerationLimit = false;
        sentSaved = null;
        sentBuild = null;
        ignoredLateRestore = false;
    }

    private static boolean sameBuild(PublicationReceipt first, PublicationReceipt second) {
        return first != null && second != null
            && first.owner.equals(second.owner)
            && first.project.equals(second.project)
            && first.revision == second.revision
            && first.digest.equals(second.digest);
    }

    public String revisionLine() {
        String revision = saved == null ? "Not created" : "Saved r" + saved.revision;
        if (dirty()) revision += " | edited";
        ProjectValidation current = currentValidation();
        if (current != null) revision += " | Validated r" + current.revision + ": " + (current.valid ? "OK" : "ERROR");
        else revision += " | not validated";
        if (publication != null) revision += " | Published r" + publication.revision;
        if (libraryPublication != null) revision += " | Library r" + libraryPublication.revision;
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
