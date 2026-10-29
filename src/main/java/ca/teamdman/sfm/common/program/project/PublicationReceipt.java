package ca.teamdman.sfm.common.program.project;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

/** Owner-scoped publication metadata. Knowledge of a content digest grants no private-source access. */
public final class PublicationReceipt {

    private static final int MAGIC = 0x53464D52, FORMAT = 1;
    static final int MAX_BYTES = ProjectWorkspace.MAX_NAME_BYTES + 128;
    public final UUID owner, project;
    public final long revision;
    public final String digest, name;

    public PublicationReceipt(UUID owner, UUID project, long revision, String digest, String name) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.project = Objects.requireNonNull(project, "project");
        if (revision < 0 || !PublicationIO.isDigest(digest)) throw new IllegalArgumentException("Invalid publication");
        ProjectRepository.validateName(name);
        this.revision = revision;
        this.digest = digest;
        this.name = name;
    }

    byte[] encode() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            output.writeInt(MAGIC);
            output.writeInt(FORMAT);
            PublicationIO.writeId(output, owner);
            PublicationIO.writeId(output, project);
            output.writeLong(revision);
            PublicationIO.writeString(output, digest, 64);
            PublicationIO.writeString(output, name, ProjectWorkspace.MAX_NAME_BYTES);
        }
        return bytes.toByteArray();
    }

    static PublicationReceipt decode(byte[] bytes) throws IOException {
        if (bytes.length > MAX_BYTES) throw PublicationIO.invalid();
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes))) {
            if (input.readInt() != MAGIC || input.readInt() != FORMAT) throw PublicationIO.invalid();
            PublicationReceipt receipt = new PublicationReceipt(
                PublicationIO.readId(input),
                PublicationIO.readId(input),
                input.readLong(),
                PublicationIO.readString(input, 64),
                PublicationIO.readString(input, ProjectWorkspace.MAX_NAME_BYTES));
            if (input.read() != -1) throw PublicationIO.invalid();
            return receipt;
        } catch (IllegalArgumentException | NullPointerException error) {
            throw PublicationIO.invalid();
        }
    }
}
