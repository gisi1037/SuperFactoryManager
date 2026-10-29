package ca.teamdman.sfm.common.program.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Paged catalog metadata; listing never returns other players' projects or source text. */
public final class ProjectPage {

    public static final class Entry {

        public final UUID id;
        public final long revision;
        public final String name;

        Entry(ProjectDraft draft) {
            this(draft.id, draft.revision, draft.name);
        }

        public Entry(UUID id, long revision, String name) {
            this.id = id;
            this.revision = revision;
            this.name = name;
        }
    }

    public final List<Entry> entries;
    public final int total, offset;

    public ProjectPage(List<Entry> entries, int total, int offset) {
        this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
        this.total = total;
        this.offset = offset;
    }
}
