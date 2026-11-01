package ca.teamdman.sfm.common.program.project;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import ca.teamdman.sfm.common.util.BoundedUtf8;

/** Portable saved source only: no owner/project IDs, builds, execution grants or runtime state. */
public final class ProgramSourceArchive {

    public static final String PREFIX = "SFM-PROGRAMS-1:";
    private static final int MAGIC = 0x53464d41, VERSION = 1;
    private static final int MAX_PACKED_BYTES = 4 * 1024 * 1024;
    public static final int MAX_TEXT_CHARACTERS = PREFIX.length() + (MAX_PACKED_BYTES + 2) / 3 * 4;

    public static final class Entry {

        public final String name, source;

        public Entry(String name, String source) {
            ProjectRepository.validateName(name);
            BoundedUtf8.encode(source, ProjectWorkspace.MAX_SOURCE_BYTES);
            this.name = name;
            this.source = source;
        }
    }

    private ProgramSourceArchive() {}

    public static String encode(List<Entry> entries) {
        if (entries == null || entries.size() > ProjectWorkspace.MAX_PROJECTS_PER_OWNER)
            throw new IllegalArgumentException("Too many saved programs for one archive");
        ByteArrayOutputStream packed = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(new GZIPOutputStream(packed))) {
            out.writeInt(MAGIC);
            out.writeInt(VERSION);
            out.writeInt(entries.size());
            for (Entry entry : entries) {
                if (entry == null) throw new IllegalArgumentException("Missing archive entry");
                write(out, entry.name, ProjectWorkspace.MAX_NAME_BYTES);
                write(out, entry.source, ProjectWorkspace.MAX_SOURCE_BYTES);
            }
        } catch (IOException failure) {
            throw new IllegalArgumentException("Could not encode saved programs", failure);
        }
        if (packed.size() > MAX_PACKED_BYTES) throw new IllegalArgumentException("Program archive is too large");
        return PREFIX + Base64.getEncoder()
            .encodeToString(packed.toByteArray());
    }

    /** Validate the whole archive before the client is allowed to submit its first create request. */
    public static List<Entry> decode(String text) {
        if (text == null || text.length() > MAX_TEXT_CHARACTERS + 64)
            throw new IllegalArgumentException("Program archive is too large");
        String value = text.trim();
        if (!value.startsWith(PREFIX) || value.length() > MAX_TEXT_CHARACTERS)
            throw new IllegalArgumentException("Clipboard is not a supported SFM program archive");
        try {
            byte[] packed = Base64.getDecoder()
                .decode(value.substring(PREFIX.length()));
            if (packed.length > MAX_PACKED_BYTES) throw new IllegalArgumentException("Program archive is too large");
            try (DataInputStream in = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(packed)))) {
                if (in.readInt() != MAGIC || in.readInt() != VERSION)
                    throw new IllegalArgumentException("Unsupported program archive version");
                int count = in.readInt();
                if (count < 0 || count > ProjectWorkspace.MAX_PROJECTS_PER_OWNER)
                    throw new IllegalArgumentException("Invalid archive program count");
                List<Entry> entries = new ArrayList<>(count);
                for (int i = 0; i < count; i++) entries.add(
                    new Entry(read(in, ProjectWorkspace.MAX_NAME_BYTES), read(in, ProjectWorkspace.MAX_SOURCE_BYTES)));
                // Forces trailer/CRC validation, and rejects any additional decompressed data without reading it all.
                if (in.read() != -1) throw new IllegalArgumentException("Unexpected program archive data");
                return Collections.unmodifiableList(entries);
            }
        } catch (IOException | IllegalArgumentException failure) {
            throw new IllegalArgumentException(
                "Invalid or damaged SFM program archive: " + failure.getMessage(),
                failure);
        }
    }

    private static void write(DataOutputStream out, String value, int maximum) throws IOException {
        ByteBuffer bytes = BoundedUtf8.encode(value, maximum);
        out.writeInt(bytes.remaining());
        byte[] contents = new byte[bytes.remaining()];
        bytes.get(contents);
        out.write(contents);
    }

    private static String read(DataInputStream in, int maximum) throws IOException {
        int length = in.readInt();
        if (length < 0 || length > maximum) throw new IllegalArgumentException("Archive text exceeds its byte limit");
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
            .toString();
    }
}
