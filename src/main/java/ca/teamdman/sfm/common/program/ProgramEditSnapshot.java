package ca.teamdman.sfm.common.program;

import java.util.UUID;

/** One server-issued view of the source and its physical disk binding. */
public final class ProgramEditSnapshot {

    public final UUID sessionId, documentId;
    public final long bindingGeneration, revision;
    public final String source;

    public ProgramEditSnapshot(UUID sessionId, long bindingGeneration, UUID documentId, long revision, String source) {
        this.sessionId = sessionId;
        this.bindingGeneration = bindingGeneration;
        this.documentId = documentId;
        this.revision = revision;
        this.source = source;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public long getBindingGeneration() {
        return bindingGeneration;
    }

    public long getRevision() {
        return revision;
    }

    public String getSource() {
        return source;
    }
}
