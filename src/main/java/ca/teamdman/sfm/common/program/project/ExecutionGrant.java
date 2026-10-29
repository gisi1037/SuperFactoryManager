package ca.teamdman.sfm.common.program.project;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/** Immutable, world-local server authority. V1 permits execution and copying, never editing private source. */
final class ExecutionGrant {

    private static final int MAGIC = 0x53464D47, FORMAT = 1, EXECUTE_AND_COPY = 1;
    static final int MAX_BYTES = 8 + 2 * (4 + 64) + 2 * 16 + 8 + 4;
    final ExecutionReference reference;
    final UUID owner, project;
    final long revision;

    ExecutionGrant(ExecutionReference reference, UUID owner, UUID project, long revision) {
        if (reference == null || owner == null || project == null || revision < 0)
            throw new IllegalArgumentException("Invalid execution grant");
        this.reference = reference;
        this.owner = owner;
        this.project = project;
        this.revision = revision;
    }

    byte[] encode() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            output.writeInt(MAGIC);
            output.writeInt(FORMAT);
            PublicationIO.writeString(output, reference.token, 64);
            PublicationIO.writeString(output, reference.digest, 64);
            PublicationIO.writeId(output, owner);
            PublicationIO.writeId(output, project);
            output.writeLong(revision);
            output.writeInt(EXECUTE_AND_COPY);
        }
        return bytes.toByteArray();
    }

    static ExecutionGrant decode(byte[] bytes) throws IOException {
        if (bytes == null || bytes.length > MAX_BYTES) throw PublicationIO.invalid();
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes))) {
            if (input.readInt() != MAGIC || input.readInt() != FORMAT) throw PublicationIO.invalid();
            ExecutionReference reference = new ExecutionReference(
                PublicationIO.readString(input, 64),
                PublicationIO.readString(input, 64));
            ExecutionGrant grant = new ExecutionGrant(
                reference,
                PublicationIO.readId(input),
                PublicationIO.readId(input),
                input.readLong());
            if (input.readInt() != EXECUTE_AND_COPY || input.read() != -1) throw PublicationIO.invalid();
            return grant;
        } catch (IllegalArgumentException error) {
            throw PublicationIO.invalid();
        }
    }
}
