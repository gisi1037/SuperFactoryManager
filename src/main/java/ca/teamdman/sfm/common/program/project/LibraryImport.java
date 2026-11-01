package ca.teamdman.sfm.common.program.project;

import java.util.Objects;
import java.util.UUID;

/** Exact library selection. Identifiers describe a selection; they never confer source-read authority. */
public final class LibraryImport {

    public static final int MAX_ALIAS_CHARS = 32;
    public final String alias, digest;
    public final UUID project;
    public final long revision;

    public LibraryImport(String alias, UUID project, long revision, String digest) {
        if (alias == null || alias.isEmpty() || alias.length() > MAX_ALIAS_CHARS)
            throw new IllegalArgumentException("Invalid library alias");
        for (int i = 0; i < alias.length(); i++) {
            char c = alias.charAt(i);
            if (!(c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c == '_' || i > 0 && c >= '0' && c <= '9'))
                throw new IllegalArgumentException("Invalid library alias");
        }
        if (project == null || revision < 0 || !PublicationIO.isDigest(digest))
            throw new IllegalArgumentException("Invalid library selection");
        this.alias = alias;
        this.project = project;
        this.revision = revision;
        this.digest = digest;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof LibraryImport)) return false;
        LibraryImport other = (LibraryImport) object;
        return alias.equals(other.alias) && project.equals(other.project)
            && revision == other.revision
            && digest.equals(other.digest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias, project, revision, digest);
    }
}
