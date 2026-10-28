package ca.teamdman.sfm.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Small compare-and-save token; source length does not enlarge a guarded save packet. */
public final class ProgramRevision {

    private ProgramRevision() {}

    public static byte[] digest(String source) {
        try {
            return MessageDigest.getInstance("SHA-256")
                .digest(source.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean matches(byte[] expected, String source) {
        return expected != null && expected.length == 32 && MessageDigest.isEqual(expected, digest(source));
    }
}
