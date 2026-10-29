package ca.teamdman.sfm.common.program.project;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

import ca.teamdman.sfm.common.util.BoundedUtf8;

/**
 * Immutable source bundle, never a mutable compiled Program or an access grant. V1 has a root source only and an
 * explicitly empty dependency closure. Changing any semantic identifier or exact UTF-8 source changes its digest.
 */
public final class PublishedProgram {

    private static final int MAGIC = 0x53464D42;
    public static final int FORMAT = 1;
    public static final String LANGUAGE = "sfml", API = "sfm-java-1", PROFILE = "gtnh-1.7.10-v1";
    public static final int MAX_BYTES = ProjectWorkspace.MAX_SOURCE_BYTES + 256;
    public final String source;
    public final List<String> dependencies = Collections.emptyList();

    public PublishedProgram(String source) {
        BoundedUtf8.encode(source, ProjectWorkspace.MAX_SOURCE_BYTES);
        this.source = source;
    }

    /** Canonical big-endian integers and byte-length-prefixed strict UTF-8; no timestamps or project metadata. */
    public byte[] encode() {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (DataOutputStream output = new DataOutputStream(bytes)) {
                output.writeInt(MAGIC);
                output.writeInt(FORMAT);
                PublicationIO.writeString(output, LANGUAGE, 32);
                PublicationIO.writeString(output, API, 32);
                PublicationIO.writeString(output, PROFILE, 32);
                PublicationIO.writeString(output, source, ProjectWorkspace.MAX_SOURCE_BYTES);
                output.writeInt(0);
            }
            return bytes.toByteArray();
        } catch (IOException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    public static PublishedProgram decode(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length > MAX_BYTES) throw PublicationIO.invalid();
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes))) {
            if (input.readInt() != MAGIC || input.readInt() != FORMAT
                || !LANGUAGE.equals(PublicationIO.readString(input, 32))
                || !API.equals(PublicationIO.readString(input, 32))
                || !PROFILE.equals(PublicationIO.readString(input, 32))) throw PublicationIO.invalid();
            String source = PublicationIO.readString(input, ProjectWorkspace.MAX_SOURCE_BYTES);
            if (input.readInt() != 0 || input.read() != -1) throw PublicationIO.invalid();
            return new PublishedProgram(source);
        } catch (IllegalArgumentException error) {
            throw PublicationIO.invalid();
        }
    }

    public String digest() {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(encode());
            StringBuilder hex = new StringBuilder(64);
            for (byte value : digest) {
                hex.append(Character.forDigit((value >>> 4) & 15, 16));
                hex.append(Character.forDigit(value & 15, 16));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }
}
