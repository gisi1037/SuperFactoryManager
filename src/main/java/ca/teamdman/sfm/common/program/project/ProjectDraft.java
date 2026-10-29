package ca.teamdman.sfm.common.program.project;

import java.util.UUID;

/** Immutable authoring snapshot. Source is a draft, not an executable or a controller binding. */
public final class ProjectDraft {

    public final UUID id, owner;
    public final long revision;
    public final String name, source;

    public ProjectDraft(UUID id, UUID owner, long revision, String name, String source) {
        this.id = id;
        this.owner = owner;
        this.revision = revision;
        this.name = name;
        this.source = source;
    }
}
