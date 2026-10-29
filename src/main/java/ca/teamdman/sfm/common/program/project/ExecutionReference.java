package ca.teamdman.sfm.common.program.project;

/** A small bearer capability. Treat its opaque token as private; a digest alone grants no execution authority. */
public final class ExecutionReference {

    public final String token, digest;

    public ExecutionReference(String token, String digest) {
        if (!PublicationIO.isDigest(token) || !PublicationIO.isDigest(digest))
            throw new IllegalArgumentException("Invalid execution reference");
        this.token = token;
        this.digest = digest;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ExecutionReference)) return false;
        ExecutionReference reference = (ExecutionReference) other;
        return token.equals(reference.token) && digest.equals(reference.digest);
    }

    @Override
    public int hashCode() {
        return 31 * token.hashCode() + digest.hashCode();
    }
}
