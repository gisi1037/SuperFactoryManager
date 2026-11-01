package ca.teamdman.sfm.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import ca.teamdman.sfm.common.net.PacketStationRequest.Action;
import ca.teamdman.sfm.common.program.project.ProgramSourceArchive;
import ca.teamdman.sfm.common.program.project.ProjectDraft;
import ca.teamdman.sfm.common.program.project.ProjectPage;
import ca.teamdman.sfm.common.program.project.ProjectWorkspace;

/** Serial, bounded authoring requests. Never reads builds, overwrites projects or replaces the open draft. */
public final class StationSourceTransfer {

    public static final class Request {

        public final Action action;
        public final UUID project;
        public final int offset;
        public final ProgramSourceArchive.Entry entry;

        private Request(Action action, UUID project, int offset, ProgramSourceArchive.Entry entry) {
            this.action = action;
            this.project = project;
            this.offset = offset;
            this.entry = entry;
        }
    }

    private final UUID owner;
    private final boolean importing;
    private final List<ProgramSourceArchive.Entry> entries;
    private final List<ProjectPage.Entry> catalog = new ArrayList<>();
    private final Set<UUID> seen = new HashSet<>();
    private Request next = new Request(Action.LIST, null, 0, null);
    private int total = -1, completed;
    private boolean stopped;
    private String exported, message = "Reading saved-program catalog...";

    public StationSourceTransfer(UUID owner, List<ProgramSourceArchive.Entry> imported) {
        if (owner == null) throw new IllegalArgumentException("Missing archive owner");
        if (imported != null && imported.size() > ProjectWorkspace.MAX_PROJECTS_PER_OWNER)
            throw new IllegalArgumentException("Too many programs to import");
        this.owner = owner;
        importing = imported != null;
        entries = imported == null ? new ArrayList<>() : new ArrayList<>(imported);
        if (entries.contains(null)) throw new IllegalArgumentException("Missing imported program");
    }

    public Request next() {
        return next;
    }

    public String message() {
        return message;
    }

    public String exported() {
        return exported;
    }

    public int completed() {
        return completed;
    }

    public boolean importing() {
        return importing;
    }

    public List<ProgramSourceArchive.Entry> entries() {
        return Collections.unmodifiableList(entries);
    }

    public void stopAfterReply() {
        stopped = true;
        message = "Stopping after the current reply; confirmed imported copies are kept.";
    }

    public void fail(String reason) {
        next = null;
        message = (importing ? "Import stopped: " + completed + "/" + entries.size() + " confirmed. "
            : "Export stopped. ") + reason;
    }

    public void accept(ProjectPage page, ProjectDraft draft) {
        if (next == null) throw new IllegalStateException("No archive request is pending");
        if (next.action == Action.LIST) {
            if (page == null || page.offset != next.offset
                || page.total < 0
                || page.total > ProjectWorkspace.MAX_PROJECTS_PER_OWNER
                || total >= 0 && total != page.total
                || page.entries.size() > ProjectWorkspace.PAGE_SIZE
                || page.entries.size() > page.total - page.offset)
                throw new IllegalArgumentException("Saved-program catalog changed; retry export");
            total = page.total;
            if (importing) {
                if (entries.size() > ProjectWorkspace.MAX_PROJECTS_PER_OWNER - total)
                    throw new IllegalArgumentException("Not enough project slots; no programs imported");
            } else {
                for (ProjectPage.Entry entry : page.entries) {
                    if (entry == null || entry.id == null || entry.revision < 0 || !seen.add(entry.id))
                        throw new IllegalArgumentException("Invalid or repeated saved-program catalog entry");
                    catalog.add(entry);
                }
                if (catalog.size() < total) {
                    if (page.entries.isEmpty()) throw new IllegalArgumentException("Incomplete saved-program catalog");
                    next = new Request(Action.LIST, null, catalog.size(), null);
                    message = "Listed " + catalog.size() + "/" + total + " saved programs...";
                    if (stopped) fail("Cancelled; clipboard unchanged.");
                    return;
                }
            }
        } else {
            if (draft == null || draft.id == null || !owner.equals(draft.owner))
                throw new IllegalArgumentException("Unexpected saved-program source reply");
            if (importing) {
                ProgramSourceArchive.Entry entry = entries.get(completed);
                if (draft.revision != 0 || !entry.name.equals(draft.name)
                    || !entry.source.equals(draft.source)
                    || !seen.add(draft.id)) throw new IllegalArgumentException("Unexpected imported program reply");
            } else {
                ProjectPage.Entry entry = catalog.get(completed);
                if (!entry.id.equals(draft.id) || entry.revision != draft.revision || !entry.name.equals(draft.name))
                    throw new IllegalArgumentException("A saved program changed during export; retry");
                entries.add(new ProgramSourceArchive.Entry(draft.name, draft.source));
            }
            completed++;
        }
        if (stopped) {
            fail(importing ? "Cancelled. Existing programs were not overwritten." : "Cancelled; clipboard unchanged.");
            return;
        }
        int size = importing ? entries.size() : catalog.size();
        if (completed == size) {
            next = null;
            if (!importing) exported = ProgramSourceArchive.encode(entries);
            message = importing ? "Imported " + completed + " new saved programs. Existing projects and draft kept."
                : "Exported " + completed + " saved programs to clipboard. No builds or unsaved edits.";
        } else {
            next = importing ? new Request(Action.ARCHIVE_CREATE, null, 0, entries.get(completed))
                : new Request(Action.ARCHIVE_LOAD, catalog.get(completed).id, 0, null);
            message = (importing ? "Importing " : "Exporting ") + (completed + 1) + "/" + size + "...";
        }
    }
}
