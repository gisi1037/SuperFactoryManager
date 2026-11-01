package ca.teamdman.sfm.common.program.project;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ca.teamdman.sfm.common.util.BoundedUtf8;

/** Immutable exact library source; a content hash is not permission to read or import it. */
public final class PublishedLibrary {

    private static final int MAGIC = 0x53464D4C;
    public static final int FORMAT = 1;
    public static final int MAX_BYTES = ProjectWorkspace.MAX_SOURCE_BYTES + 256;
    public final String source;

    public PublishedLibrary(String source) {
        BoundedUtf8.encode(source, ProjectWorkspace.MAX_SOURCE_BYTES);
        this.source = source;
    }

    public byte[] encode() {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream output = new DataOutputStream(bytes)) {
                output.writeInt(MAGIC);
                output.writeInt(FORMAT);
                PublicationIO.writeString(output, PublishedProgram.LANGUAGE, 32);
                PublicationIO.writeString(output, PublishedProgram.API, 32);
                PublicationIO.writeString(output, PublishedProgram.PROFILE, 32);
                PublicationIO.writeString(output, source, ProjectWorkspace.MAX_SOURCE_BYTES);
            }
            return bytes.toByteArray();
        } catch (IOException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    public static PublishedLibrary decode(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length > MAX_BYTES) throw PublicationIO.invalid();
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes))) {
            if (input.readInt() != MAGIC || input.readInt() != FORMAT
                || !PublishedProgram.LANGUAGE.equals(PublicationIO.readString(input, 32))
                || !PublishedProgram.API.equals(PublicationIO.readString(input, 32))
                || !PublishedProgram.PROFILE.equals(PublicationIO.readString(input, 32))) throw PublicationIO.invalid();
            PublishedLibrary library = new PublishedLibrary(
                PublicationIO.readString(input, ProjectWorkspace.MAX_SOURCE_BYTES));
            if (input.read() != -1) throw PublicationIO.invalid();
            return library;
        } catch (IllegalArgumentException error) {
            throw PublicationIO.invalid();
        }
    }

    public String digest() {
        return PublicationIO.digest(encode());
    }
}
