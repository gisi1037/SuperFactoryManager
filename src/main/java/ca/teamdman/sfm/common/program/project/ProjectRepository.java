package ca.teamdman.sfm.common.program.project;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import ca.teamdman.sfm.common.util.BoundedUtf8;

/** Worker-only filesystem backend. Paths come exclusively from authenticated UUIDs. */
final class ProjectRepository {

    private static final int MAGIC = 0x53464D50, FORMAT = 1;
    private static final int MAX_FILE_BYTES = ProjectWorkspace.MAX_SOURCE_BYTES + ProjectWorkspace.MAX_NAME_BYTES + 128;
    private static final Object[] WORLD_LOCKS = new Object[64];
    static {
        for (int i = 0; i < WORLD_LOCKS.length; i++) WORLD_LOCKS[i] = new Object();
    }
    private final Path worldDirectory;

    Object transactionLock() {
        return WORLD_LOCKS[(worldDirectory.hashCode() & Integer.MAX_VALUE) % WORLD_LOCKS.length];
    }

    ProjectRepository(Path worldDirectory) {
        this.worldDirectory = worldDirectory.toAbsolutePath()
            .normalize();
    }

    ProjectDraft create(UUID owner, String name, String source) throws IOException {
        validate(name, source);
        Path directory = ownerDirectory(owner, true);
        List<UUID> existing = projectIds(directory);
        if (existing.size() >= ProjectWorkspace.MAX_PROJECTS_PER_OWNER)
            throw new Failure(ProjectResult.Status.LIMIT, "Project limit reached");
        UUID id = UUID.randomUUID();
        ProjectDraft draft = new ProjectDraft(id, owner, 0, name, source);
        Path project = directory.resolve(id.toString());
        Files.createDirectory(project);
        write(project, draft, false);
        return draft;
    }

    ProjectDraft load(UUID owner, UUID id) throws IOException {
        requireId(id);
        Path directory = ownerDirectory(owner, false);
        if (directory == null) throw missing();
        Path project = directory.resolve(id.toString());
        if (!Files.exists(project, LinkOption.NOFOLLOW_LINKS)) throw missing();
        requireDirectory(project);
        Path file = project.resolve("draft.bin");
        if (!Files.exists(file, LinkOption.NOFOLLOW_LINKS)) throw missing();
        if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) throw malformed();
        long size = Files.size(file);
        if (size < 0 || size > MAX_FILE_BYTES) throw malformed();
        // Bound the read even if a local administrator changes a file after the size check.
        byte[] bytes = new byte[MAX_FILE_BYTES + 1];
        int length = 0;
        try (java.io.InputStream input = Files.newInputStream(file)) {
            while (length < bytes.length) {
                int read = input.read(bytes, length, bytes.length - length);
                if (read < 0) break;
                length += read;
            }
        }
        if (length > MAX_FILE_BYTES) throw malformed();
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes, 0, length))) {
            if (input.readInt() != MAGIC || input.readInt() != FORMAT) throw malformed();
            UUID storedOwner = readId(input), storedId = readId(input);
            long revision = input.readLong();
            String name = readString(input, ProjectWorkspace.MAX_NAME_BYTES);
            String source = readString(input, ProjectWorkspace.MAX_SOURCE_BYTES);
            if (!owner.equals(storedOwner) || !id.equals(storedId) || revision < 0 || input.read() != -1)
                throw malformed();
            validate(name, source);
            return new ProjectDraft(id, owner, revision, name, source);
        } catch (IllegalArgumentException error) {
            throw malformed();
        }
    }

    ProjectDraft save(UUID owner, UUID id, long expectedRevision, String name, String source) throws IOException {
        validate(name, source);
        ProjectDraft before = load(owner, id);
        if (before.revision != expectedRevision)
            throw new Failure(ProjectResult.Status.STALE, "Project changed; reload before saving");
        if (before.revision == Long.MAX_VALUE)
            throw new Failure(ProjectResult.Status.LIMIT, "Project revision exhausted");
        ProjectDraft after = new ProjectDraft(id, owner, before.revision + 1, name, source);
        write(ownerDirectory(owner, false).resolve(id.toString()), after, true);
        return after;
    }

    ProjectPage list(UUID owner, int offset) throws IOException {
        if (offset < 0 || offset > ProjectWorkspace.MAX_PROJECTS_PER_OWNER)
            throw new IllegalArgumentException("Invalid project page");
        Path directory = ownerDirectory(owner, false);
        List<ProjectDraft> drafts = new ArrayList<>();
        if (directory != null) for (UUID id : projectIds(directory)) {
            // An interrupted first create has no published draft and is not part of the catalog.
            if (!Files.exists(
                directory.resolve(id.toString())
                    .resolve("draft.bin"),
                LinkOption.NOFOLLOW_LINKS)) continue;
            drafts.add(load(owner, id));
        }
        drafts.sort(Comparator.comparing(draft -> draft.id.toString()));
        int start = Math.min(offset, drafts.size());
        List<ProjectPage.Entry> page = new ArrayList<>();
        for (int i = start; i < drafts.size() && page.size() < ProjectWorkspace.PAGE_SIZE; i++)
            page.add(new ProjectPage.Entry(drafts.get(i)));
        return new ProjectPage(page, drafts.size(), start);
    }

    private Path ownerDirectory(UUID owner, boolean create) throws IOException {
        requireId(owner);
        requireDirectory(worldDirectory);
        Path current = worldDirectory;
        for (String part : new String[] { "sfm", "projects", owner.toString() }) {
            current = current.resolve(part);
            if (!Files.exists(current, LinkOption.NOFOLLOW_LINKS)) {
                if (!create) return null;
                Files.createDirectory(current);
            }
            requireDirectory(current);
        }
        return current;
    }

    private static List<UUID> projectIds(Path directory) throws IOException {
        List<UUID> ids = new ArrayList<>();
        int scanned = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path child : stream) {
                if (++scanned > ProjectWorkspace.MAX_PROJECTS_PER_OWNER)
                    throw new Failure(ProjectResult.Status.LIMIT, "Project catalog exceeds its limit");
                try {
                    String name = child.getFileName()
                        .toString();
                    UUID id = UUID.fromString(name);
                    if (!name.equals(id.toString())) throw malformed();
                    requireDirectory(child);
                    ids.add(id);
                } catch (IllegalArgumentException error) {
                    throw malformed();
                }
            }
        }
        return ids;
    }

    private static void requireDirectory(Path path) throws IOException {
        if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) throw malformed();
    }

    private static void requireId(UUID id) {
        if (id == null) throw new IllegalArgumentException("Project identity is required");
    }

    private static void validate(String name, String source) {
        BoundedUtf8.encode(name, ProjectWorkspace.MAX_NAME_BYTES);
        BoundedUtf8.encode(source, ProjectWorkspace.MAX_SOURCE_BYTES);
        if (name.trim()
            .isEmpty()) throw new IllegalArgumentException("Project name is required");
        for (int i = 0; i < name.length(); i++) {
            if (Character.isISOControl(name.charAt(i)) || name.charAt(i) == '\u00a7')
                throw new IllegalArgumentException("Invalid project name");
        }
    }

    private static void write(Path project, ProjectDraft draft, boolean replace) throws IOException {
        requireDirectory(project);
        Path target = project.resolve("draft.bin");
        if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)
            && (!replace || !Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS))) throw malformed();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            output.writeInt(MAGIC);
            output.writeInt(FORMAT);
            writeId(output, draft.owner);
            writeId(output, draft.id);
            output.writeLong(draft.revision);
            writeString(output, draft.name, ProjectWorkspace.MAX_NAME_BYTES);
            writeString(output, draft.source, ProjectWorkspace.MAX_SOURCE_BYTES);
        }
        Path temporary = project.resolve("draft.pending-" + UUID.randomUUID());
        try {
            try (FileChannel output = FileChannel
                .open(temporary, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                ByteBuffer buffer = ByteBuffer.wrap(bytes.toByteArray());
                while (buffer.hasRemaining()) output.write(buffer);
                output.force(true);
            }
            // Fail closed if the filesystem cannot provide atomic visibility; no delete-then-rename fallback.
            if (replace)
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            else Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static String readString(DataInputStream input, int maximum) throws IOException {
        int length = input.readInt();
        if (length < 0 || length > maximum) throw malformed();
        byte[] bytes = new byte[length];
        input.readFully(bytes);
        try {
            return StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString();
        } catch (CharacterCodingException error) {
            throw malformed();
        }
    }

    private static void writeString(DataOutputStream output, String value, int maximum) throws IOException {
        ByteBuffer encoded = BoundedUtf8.encode(value, maximum);
        output.writeInt(encoded.remaining());
        while (encoded.hasRemaining()) output.writeByte(encoded.get());
    }

    private static UUID readId(DataInputStream input) throws IOException {
        return new UUID(input.readLong(), input.readLong());
    }

    private static void writeId(DataOutputStream output, UUID id) throws IOException {
        output.writeLong(id.getMostSignificantBits());
        output.writeLong(id.getLeastSignificantBits());
    }

    private static Failure missing() {
        return new Failure(ProjectResult.Status.NOT_FOUND, "Project not found or access denied");
    }

    private static Failure malformed() {
        return new Failure(ProjectResult.Status.IO_ERROR, "Invalid or unsupported project storage");
    }

    static final class Failure extends IOException {

        final ProjectResult.Status status;

        Failure(ProjectResult.Status status, String message) {
            super(message);
            this.status = status;
        }
    }
}
