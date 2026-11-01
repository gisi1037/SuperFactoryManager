package ca.teamdman.sfm.common.program.project;

import java.util.Objects;

/** Closed immutable dependency, including both its exact selection and the verified source artifact. */
public final class LibraryModule {

    public final LibraryImport reference;
    public final PublishedLibrary library;

    public LibraryModule(LibraryImport reference, PublishedLibrary library) {
        this.reference = Objects.requireNonNull(reference, "reference");
        this.library = Objects.requireNonNull(library, "library");
        if (!reference.digest.equals(library.digest()))
            throw new IllegalArgumentException("Library source does not match its selection");
    }
}
