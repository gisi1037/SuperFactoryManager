package ca.teamdman.sfm.common.program.project;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.teamdman.sfm.common.util.BoundedUtf8;

/**
 * Immutable source bundle, never a mutable compiled Program or an access grant. V1 retains its original root-only
 * bytes. V2 closes over exact library artifacts, so activation never reopens mutable authoring storage.
 */
public final class PublishedProgram {

    private static final int MAGIC = 0x53464D42;
    public static final int FORMAT = 1;
    public static final int LINKED_FORMAT = 2;
    public static final String LANGUAGE = "sfml", API = "sfm-java-1", PROFILE = "gtnh-1.7.10-v1";
    public static final int MAX_MODULES = 8, MAX_SOURCE_BYTES = 120000;
    public static final int MAX_BYTES = MAX_SOURCE_BYTES + MAX_MODULES * 512 + 256;
    public final String source;
    public final List<String> dependencies;
    public final List<LibraryModule> modules;
    public final int format;

    public PublishedProgram(String source) {
        this(source, Collections.emptyList());
    }

    public PublishedProgram(String source, List<LibraryModule> modules) {
        int sourceBytes = BoundedUtf8.encode(source, ProjectWorkspace.MAX_SOURCE_BYTES)
            .remaining();
        if (modules == null || modules.size() > MAX_MODULES)
            throw new IllegalArgumentException("Invalid library closure");
        List<LibraryModule> ordered = new ArrayList<>(modules);
        for (LibraryModule module : ordered) {
            if (module == null) throw new IllegalArgumentException("Invalid library closure");
            sourceBytes += BoundedUtf8.encode(module.library.source, ProjectWorkspace.MAX_SOURCE_BYTES)
                .remaining();
        }
        if (sourceBytes > MAX_SOURCE_BYTES) throw new IllegalArgumentException("Library source limit reached");
        ordered.sort(Comparator.comparing(module -> module.reference.alias));
        Set<String> aliases = new HashSet<>();
        List<String> digests = new ArrayList<>();
        for (LibraryModule module : ordered) {
            if (!aliases.add(module.reference.alias)) throw new IllegalArgumentException("Duplicate library alias");
            digests.add(module.reference.digest);
        }
        this.source = source;
        this.modules = Collections.unmodifiableList(ordered);
        dependencies = Collections.unmodifiableList(digests);
        format = ordered.isEmpty() ? FORMAT : LINKED_FORMAT;
    }

    /** Canonical big-endian integers and byte-length-prefixed strict UTF-8; no timestamps or project metadata. */
    public byte[] encode() {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream output = new DataOutputStream(bytes)) {
                output.writeInt(MAGIC);
                output.writeInt(format);
                PublicationIO.writeString(output, LANGUAGE, 32);
                PublicationIO.writeString(output, API, 32);
                PublicationIO.writeString(output, PROFILE, 32);
                PublicationIO.writeString(output, source, ProjectWorkspace.MAX_SOURCE_BYTES);
                output.writeInt(modules.size());
                for (LibraryModule module : modules) {
                    LibraryImport reference = module.reference;
                    PublicationIO.writeString(output, reference.alias, LibraryImport.MAX_ALIAS_CHARS);
                    PublicationIO.writeId(output, reference.project);
                    output.writeLong(reference.revision);
                    PublicationIO.writeString(output, reference.digest, 64);
                    byte[] library = module.library.encode();
                    output.writeInt(library.length);
                    output.write(library);
                }
            }
            return bytes.toByteArray();
        } catch (IOException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    public static PublishedProgram decode(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length > MAX_BYTES) throw PublicationIO.invalid();
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes))) {
            if (input.readInt() != MAGIC) throw PublicationIO.invalid();
            int format = input.readInt();
            if (format != FORMAT && format != LINKED_FORMAT || !LANGUAGE.equals(PublicationIO.readString(input, 32))
                || !API.equals(PublicationIO.readString(input, 32))
                || !PROFILE.equals(PublicationIO.readString(input, 32))) throw PublicationIO.invalid();
            String source = PublicationIO.readString(input, ProjectWorkspace.MAX_SOURCE_BYTES);
            int count = input.readInt();
            if (count < 0 || count > MAX_MODULES
                || format == FORMAT && count != 0
                || format == LINKED_FORMAT && count == 0) throw PublicationIO.invalid();
            List<LibraryModule> modules = new ArrayList<>();
            String previous = null;
            for (int i = 0; i < count; i++) {
                String alias = PublicationIO.readString(input, LibraryImport.MAX_ALIAS_CHARS);
                if (previous != null && previous.compareTo(alias) >= 0) throw PublicationIO.invalid();
                previous = alias;
                LibraryImport reference = new LibraryImport(
                    alias,
                    PublicationIO.readId(input),
                    input.readLong(),
                    PublicationIO.readString(input, 64));
                int length = input.readInt();
                if (length < 0 || length > PublishedLibrary.MAX_BYTES) throw PublicationIO.invalid();
                byte[] library = new byte[length];
                input.readFully(library);
                modules.add(new LibraryModule(reference, PublishedLibrary.decode(library)));
            }
            if (input.read() != -1) throw PublicationIO.invalid();
            return new PublishedProgram(source, modules);
        } catch (IllegalArgumentException error) {
            throw PublicationIO.invalid();
        }
    }

    public String digest() {
        return PublicationIO.digest(encode());
    }
}
