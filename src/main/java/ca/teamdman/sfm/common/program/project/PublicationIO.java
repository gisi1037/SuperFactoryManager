package ca.teamdman.sfm.common.program.project;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import ca.teamdman.sfm.common.util.BoundedUtf8;

/** Strict, bounded primitives shared only by the versioned publication codecs. */
final class PublicationIO {

    private PublicationIO() {}

    static String readString(DataInputStream input, int maximum) throws IOException {
        int length = input.readInt();
        if (length < 0 || length > maximum) throw invalid();
        byte[] bytes = new byte[length];
        input.readFully(bytes);
        return StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
            .toString();
    }

    static void writeString(DataOutputStream output, String value, int maximum) throws IOException {
        ByteBuffer bytes = BoundedUtf8.encode(value, maximum);
        output.writeInt(bytes.remaining());
        while (bytes.hasRemaining()) output.writeByte(bytes.get());
    }

    static UUID readId(DataInputStream input) throws IOException {
        return new UUID(input.readLong(), input.readLong());
    }

    static void writeId(DataOutputStream output, UUID id) throws IOException {
        output.writeLong(id.getMostSignificantBits());
        output.writeLong(id.getLeastSignificantBits());
    }

    static boolean isDigest(String digest) {
        if (digest == null || digest.length() != 64) return false;
        for (int i = 0; i < digest.length(); i++) {
            char c = digest.charAt(i);
            if (!(c >= '0' && c <= '9' || c >= 'a' && c <= 'f')) return false;
        }
        return true;
    }

    static String digest(byte[] bytes) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(bytes);
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

    static IOException invalid() {
        return new IOException("Invalid or unsupported publication storage");
    }
}
