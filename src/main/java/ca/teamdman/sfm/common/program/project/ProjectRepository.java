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
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import ca.teamdman.sfm.common.util.BoundedUtf8;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

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

    /** Called under the world transaction lock, on the workspace worker only. */
    PublicationReceipt publish(UUID owner, UUID id, long expectedRevision) throws IOException {
        if (expectedRevision < 0) throw new IllegalArgumentException("Invalid project revision");
        ProjectDraft draft = load(owner, id);
        Path project = ownerDirectory(owner, false).resolve(id.toString());
        Path publications = childDirectory(project, "publications", false);
        Path receiptFile = publications == null ? null : publications.resolve(expectedRevision + ".bin");
        if (receiptFile != null && Files.exists(receiptFile, LinkOption.NOFOLLOW_LINKS)) {
            PublicationReceipt existing = readReceipt(receiptFile, owner, id, expectedRevision);
            verifyProgram(existing.digest);
            if (expectedRevision > draft.revision
                || expectedRevision == draft.revision && (!existing.name.equals(draft.name)
                    || !existing.digest.equals(new PublishedProgram(draft.source).digest())))
                throw malformed();
            return existing; // Lost ACK can be reconciled even after a newer draft was saved.
        }
        if (draft.revision != expectedRevision)
            throw new Failure(ProjectResult.Status.STALE, "Project changed; reload before publishing");
        ProgramBuildResult build = new ProgramBuilder(draft.source).build();
        if (!build.isSuccess())
            throw new Failure(ProjectResult.Status.INVALID, ProjectWorkspace.diagnostic(build.getErrorMessage()));
        // The mutable compiled plan is intentionally discarded, never cached or shared.
        PublishedProgram program = new PublishedProgram(draft.source);
        PublicationReceipt receipt = new PublicationReceipt(owner, id, expectedRevision, program.digest(), draft.name);
        if (publications != null
            && publicationRevisions(publications).size() >= ProjectWorkspace.MAX_PUBLICATIONS_PER_PROJECT)
            throw new Failure(ProjectResult.Status.LIMIT, "Publication limit reached");
        Path programs = programDirectory(true);
        Path bundle = programs.resolve(receipt.digest + ".sfmp");
        if (Files.exists(bundle, LinkOption.NOFOLLOW_LINKS)) {
            PublishedProgram existing = verifyProgram(receipt.digest);
            if (!existing.source.equals(program.source)) throw malformed();
        } else {
            checkProgramQuota(programs, program.encode().length);
            writeImmutable(programs, bundle, program.encode());
        }
        // Bundle visibility precedes the receipt. An interrupted publish can leave only an orphan bundle.
        publications = childDirectory(project, "publications", true);
        writeImmutable(publications, publications.resolve(expectedRevision + ".bin"), receipt.encode());
        return receipt;
    }

    PublicationPage listPublications(UUID owner, UUID id, int offset) throws IOException {
        if (offset < 0 || offset > ProjectWorkspace.MAX_PUBLICATIONS_PER_PROJECT)
            throw new IllegalArgumentException("Invalid publication page");
        load(owner, id); // Existence and ownership are required even for an empty catalog.
        Path project = ownerDirectory(owner, false).resolve(id.toString());
        Path directory = childDirectory(project, "publications", false);
        List<Long> revisions = directory == null ? new ArrayList<>() : publicationRevisions(directory);
        revisions.sort(Comparator.reverseOrder());
        int start = Math.min(offset, revisions.size());
        List<PublicationReceipt> page = new ArrayList<>();
        for (int i = start; i < revisions.size() && page.size() < ProjectWorkspace.PAGE_SIZE; i++) {
            long revision = revisions.get(i);
            page.add(readReceipt(directory.resolve(revision + ".bin"), owner, id, revision));
        }
        return new PublicationPage(page, revisions.size(), start);
    }

    /** Issues only from an existing authenticated publication; never publishes or substitutes a draft. */
    ExecutionReference issueExecutionGrant(UUID owner, UUID id, long revision, String expectedDigest)
        throws IOException {
        requireId(owner);
        requireId(id);
        if (revision < 0 || !PublicationIO.isDigest(expectedDigest))
            throw new IllegalArgumentException("Invalid published build selection");
        Path ownerPath = ownerDirectory(owner, false);
        if (ownerPath == null) throw missing();
        Path project = ownerPath.resolve(id.toString());
        if (!Files.exists(project, LinkOption.NOFOLLOW_LINKS)) throw missing();
        requireDirectory(project);
        Path publications = childDirectory(project, "publications", false);
        if (publications == null) throw missing();
        Path receiptFile = publications.resolve(revision + ".bin");
        if (!Files.exists(receiptFile, LinkOption.NOFOLLOW_LINKS)) throw missing();
        PublicationReceipt receipt = readReceipt(receiptFile, owner, id, revision);
        if (!expectedDigest.equals(receipt.digest))
            throw new Failure(ProjectResult.Status.STALE, "Published build does not match the selection");
        verifyProgram(receipt.digest);
        Path grants = executionGrantDirectory(true);
        // All files, including interrupted temporary writes and orphan grants, consume the finite allowance.
        checkExecutionGrantQuota(grants, ExecutionGrant.MAX_BYTES);
        for (int attempt = 0; attempt < 8; attempt++) {
            byte[] random = new byte[32];
            GrantRandom.INSTANCE.nextBytes(random);
            StringBuilder token = new StringBuilder(64);
            for (byte value : random) {
                token.append(Character.forDigit((value >>> 4) & 15, 16));
                token.append(Character.forDigit(value & 15, 16));
            }
            ExecutionReference reference = new ExecutionReference(token.toString(), receipt.digest);
            Path target = grants.resolve(reference.token + ".bin");
            if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) continue;
            writeImmutable(grants, target, new ExecutionGrant(reference, owner, id, revision).encode());
            return reference;
        }
        throw new Failure(ProjectResult.Status.IO_ERROR, "Execution grant request failed");
    }

    /** Every activation rereads and validates its actual grant and exact bundle, then builds a private mutable plan. */
    PreparedExecution prepareExecution(ExecutionReference reference) throws IOException {
        if (reference == null) throw new IllegalArgumentException("Execution reference is required");
        Path grants = executionGrantDirectory(false);
        if (grants == null) throw missingGrant();
        Path file = grants.resolve(reference.token + ".bin");
        if (!Files.exists(file, LinkOption.NOFOLLOW_LINKS)) throw missingGrant();
        ExecutionGrant grant = ExecutionGrant.decode(readBounded(file, ExecutionGrant.MAX_BYTES));
        if (!reference.equals(grant.reference)) throw missingGrant();
        PublishedProgram bundle = verifyProgram(reference.digest);
        ProgramBuildResult build = new ProgramBuilder(bundle.source).build();
        if (!build.isSuccess()) throw new Failure(ProjectResult.Status.INVALID, "Published program cannot be compiled");
        return new PreparedExecution(reference, build.getProgram(), bundle.source, grant.owner);
    }

    private Path executionGrantDirectory(boolean create) throws IOException {
        requireDirectory(worldDirectory);
        Path sfm = childDirectory(worldDirectory, "sfm", create);
        return sfm == null ? null : childDirectory(sfm, "execution-grants", create);
    }

    private static void checkExecutionGrantQuota(Path directory, int addedBytes) throws IOException {
        int count = 0;
        long bytes = addedBytes;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path file : stream) {
                if (++count >= ProjectWorkspace.MAX_EXECUTION_GRANTS_PER_WORLD)
                    throw new Failure(ProjectResult.Status.LIMIT, "World execution grant limit reached");
                if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) throw malformed();
                String name = file.getFileName()
                    .toString();
                if (!isPending(name)
                    && !(name.endsWith(".bin") && PublicationIO.isDigest(name.substring(0, name.length() - 4))))
                    throw malformed();
                long size = Files.size(file);
                if (size > ProjectWorkspace.MAX_EXECUTION_GRANT_BYTES_PER_WORLD - bytes)
                    throw new Failure(ProjectResult.Status.LIMIT, "World execution grant byte limit reached");
                bytes += size;
            }
        }
        if (bytes > ProjectWorkspace.MAX_EXECUTION_GRANT_BYTES_PER_WORLD)
            throw new Failure(ProjectResult.Status.LIMIT, "World execution grant byte limit reached");
    }

    private static Failure missingGrant() {
        return new Failure(ProjectResult.Status.NOT_FOUND, "Execution grant not found or access denied");
    }

    /** Lazy initialization keeps entropy acquisition inside the admitted storage worker operation. */
    private static final class GrantRandom {

        private static final SecureRandom INSTANCE = new SecureRandom();
    }

    private static PublicationReceipt readReceipt(Path file, UUID owner, UUID id, long revision) throws IOException {
        PublicationReceipt receipt = PublicationReceipt.decode(readBounded(file, PublicationReceipt.MAX_BYTES));
        if (!owner.equals(receipt.owner) || !id.equals(receipt.project) || revision != receipt.revision)
            throw malformed();
        return receipt;
    }

    private PublishedProgram verifyProgram(String digest) throws IOException {
        Path directory = programDirectory(false);
        if (directory == null) throw malformed();
        PublishedProgram program = PublishedProgram
            .decode(readBounded(directory.resolve(digest + ".sfmp"), PublishedProgram.MAX_BYTES));
        if (!digest.equals(program.digest())) throw malformed();
        return program;
    }

    private Path programDirectory(boolean create) throws IOException {
        requireDirectory(worldDirectory);
        Path sfm = childDirectory(worldDirectory, "sfm", create);
        return sfm == null ? null : childDirectory(sfm, "programs", create);
    }

    private static Path childDirectory(Path parent, String name, boolean create) throws IOException {
        requireDirectory(parent);
        Path directory = parent.resolve(name);
        if (!Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) {
            if (!create) return null;
            Files.createDirectory(directory);
        }
        requireDirectory(directory);
        return directory;
    }

    private static List<Long> publicationRevisions(Path directory) throws IOException {
        List<Long> revisions = new ArrayList<>();
        int scanned = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path file : stream) {
                if (++scanned > ProjectWorkspace.MAX_PUBLICATIONS_PER_PROJECT)
                    throw new Failure(ProjectResult.Status.LIMIT, "Publication catalog exceeds its limit");
                if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) throw malformed();
                String name = file.getFileName()
                    .toString();
                // Interrupted temporary writes consume quota but are never acknowledged as publications.
                if (isPending(name)) continue;
                if (!name.endsWith(".bin")) throw malformed();
                try {
                    long revision = Long.parseLong(name.substring(0, name.length() - 4));
                    if (revision < 0 || !name.equals(revision + ".bin")) throw malformed();
                    revisions.add(revision);
                } catch (NumberFormatException error) {
                    throw malformed();
                }
            }
        }
        // Include orphan temporary entries in capacity checks as well as the finite scan bound.
        if (scanned >= ProjectWorkspace.MAX_PUBLICATIONS_PER_PROJECT && scanned > revisions.size())
            throw new Failure(ProjectResult.Status.LIMIT, "Publication catalog exceeds its limit");
        return revisions;
    }

    private static boolean isPending(String name) throws IOException {
        if (!name.startsWith(".pending-")) return false;
        try {
            String id = name.substring(9);
            if (!UUID.fromString(id)
                .toString()
                .equals(id)) throw malformed();
            return true;
        } catch (IllegalArgumentException error) {
            throw malformed();
        }
    }

    private static void checkProgramQuota(Path directory, int addedBytes) throws IOException {
        int count = 0;
        long bytes = addedBytes;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path file : stream) {
                if (++count >= ProjectWorkspace.MAX_PROGRAMS_PER_WORLD)
                    throw new Failure(ProjectResult.Status.LIMIT, "World program limit reached");
                if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) throw malformed();
                String name = file.getFileName()
                    .toString();
                if (!isPending(name)
                    && !(name.endsWith(".sfmp") && PublicationIO.isDigest(name.substring(0, name.length() - 5))))
                    throw malformed();
                long size = Files.size(file);
                if (size > ProjectWorkspace.MAX_PROGRAM_BYTES_PER_WORLD - bytes)
                    throw new Failure(ProjectResult.Status.LIMIT, "World program byte limit reached");
                bytes += size;
                if (bytes > ProjectWorkspace.MAX_PROGRAM_BYTES_PER_WORLD)
                    throw new Failure(ProjectResult.Status.LIMIT, "World program byte limit reached");
            }
        }
        if (bytes > ProjectWorkspace.MAX_PROGRAM_BYTES_PER_WORLD)
            throw new Failure(ProjectResult.Status.LIMIT, "World program byte limit reached");
    }

    private static byte[] readBounded(Path file, int maximum) throws IOException {
        if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS) || Files.size(file) > maximum) throw malformed();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[Math.min(4096, maximum + 1)];
        try (java.io.InputStream input = Files.newInputStream(file)) {
            int read;
            while ((read = input.read(buffer)) != -1) {
                if (bytes.size() + read > maximum) throw malformed();
                bytes.write(buffer, 0, read);
            }
        }
        return bytes.toByteArray();
    }

    private static void writeImmutable(Path directory, Path target, byte[] bytes) throws IOException {
        requireDirectory(directory);
        if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) throw malformed();
        Path temporary = directory.resolve(".pending-" + UUID.randomUUID());
        try {
            try (FileChannel output = FileChannel
                .open(temporary, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) output.write(buffer);
                output.force(true);
            }
            // Internal writers serialize under the world lock; never replace an existing immutable artifact.
            if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) throw malformed();
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(temporary);
        }
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
        BoundedUtf8.encode(source, ProjectWorkspace.MAX_SOURCE_BYTES);
        validateName(name);
    }

    static void validateName(String name) {
        BoundedUtf8.encode(name, ProjectWorkspace.MAX_NAME_BYTES);
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
