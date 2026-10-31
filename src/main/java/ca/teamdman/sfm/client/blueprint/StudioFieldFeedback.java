package ca.teamdman.sfm.client.blueprint;

/** Last attempted Fields action, never live validation or compiler Problems. All work is event-driven. */
public final class StudioFieldFeedback {

    public static final class Snapshot {

        public final StudioFieldDraft owner;
        public final String message;
        private final Object generation;

        private Snapshot(StudioFieldDraft owner, String message, Object generation) {
            this.owner = owner;
            this.message = message;
            this.generation = generation;
        }
    }

    private StudioFieldDraft owner;
    private Object generation = new Object();
    private String message = "", publishedFooter;

    /** Rebuilt/recovered forms start unchecked, just as the old popup-local error did. */
    public String open(StudioFieldDraft draft, String status) {
        if (draft == null) throw new IllegalArgumentException("Missing Fields owner");
        String result = clear(status, "Fields reopened; Apply to check.", true);
        owner = draft;
        return result;
    }

    public String message(StudioFieldDraft draft) {
        return owner == draft ? message : "";
    }

    /** Rejected/no-op mutations keep an existing diagnostic and its Details generation current. */
    public void rejected(StudioFieldDraft draft, String failure) {
        if (owner == null || owner != draft || !message.isEmpty()) return;
        message = failure == null ? "Fields action failed." : failure;
        generation = new Object();
    }

    public String failedApply(StudioFieldDraft draft, String failure, String status) {
        if (owner == null || owner != draft) return status;
        message = failure == null ? "Fields action failed." : failure;
        generation = new Object();
        // Identity is intentional: unrelated newer text with identical contents does not belong to Fields.
        publishedFooter = new String("Fields kept: " + message);
        return publishedFooter;
    }

    /** Invoke only after an accepted mutation returned true, including newly invalid pending text. */
    public String changed(StudioFieldDraft draft, String status) {
        return owner != null && owner == draft ? clear(status, "Fields changed; Apply to check.", true) : status;
    }

    public String finish(StudioFieldDraft draft, String status, String replacement) {
        if (owner == null || owner != draft) return status;
        String result = clear(status, replacement, false);
        owner = null;
        return result;
    }

    public Snapshot snapshot(StudioFieldDraft draft) {
        return owner != null && owner == draft && !message.isEmpty() ? new Snapshot(draft, message, generation) : null;
    }

    public boolean current(Snapshot snapshot, StudioFieldDraft draft) {
        return snapshot != null && owner != null
            && owner == draft
            && snapshot.owner == draft
            && snapshot.generation == generation
            && !message.isEmpty();
    }

    private String clear(String status, String replacement, boolean keepOwnership) {
        boolean owned = publishedFooter != null && status == publishedFooter;
        String result = owned ? new String(replacement) : status;
        // Pending neutral invitations belong to Fields too, so a later Cancel can retire them safely.
        publishedFooter = owned && keepOwnership ? result : null;
        message = "";
        generation = new Object();
        return result;
    }
}
