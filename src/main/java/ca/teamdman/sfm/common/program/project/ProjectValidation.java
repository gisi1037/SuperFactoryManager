package ca.teamdman.sfm.common.program.project;

import java.util.UUID;

/** Validation describes one exact stored revision; it neither publishes nor installs code. */
public final class ProjectValidation {

    public final UUID id;
    public final long revision;
    public final boolean valid;
    public final String message;

    ProjectValidation(ProjectDraft draft, boolean valid, String diagnostic) {
        this(draft.id, draft.revision, valid, diagnostic);
    }

    public ProjectValidation(UUID id, long revision, boolean valid, String message) {
        this.id = id;
        this.revision = revision;
        this.valid = valid;
        this.message = message;
    }
}
