package ca.teamdman.sfml.data;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Versioned, length-delimited identity encoding for recipe descriptors, never execution authority. */
final class IdentityFingerprint {

    private final MessageDigest digest;

    private IdentityFingerprint() {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException missing) {
            throw new IllegalStateException("SHA-256 is unavailable", missing);
        }
    }

    static byte[] create(int kind, String id, int metadata, BoundedItemNbt nbt, QueryBudget budget) {
        if (budget == null) throw new IllegalArgumentException("Identity fingerprint requires a query budget");
        // Reserve before hashing/allocating. This is bounded cold catalog work, not a per-tick live-stack hash.
        budget.chargeSlots(Math.addExact(21, Math.addExact(Math.multiplyExact(2, id.length()), nbt.getLength())));
        budget.chargeAllocation(256); // Fixed digest/control/result allowance, including the returned 32-byte array.
        IdentityFingerprint writer = new IdentityFingerprint();
        for (byte value : new byte[] { 'S', 'F', 'M', '-', 'I', 'D', 0, 1 }) writer.digest.update(value);
        writer.digest.update((byte) kind);
        writer.integer(id.length());
        for (int i = 0; i < id.length(); i++) {
            char value = id.charAt(i);
            writer.digest.update((byte) (value >>> 8));
            writer.digest.update((byte) value);
        }
        writer.integer(metadata);
        writer.integer(nbt.getLength());
        nbt.fingerprint(writer);
        return writer.digest.digest();
    }

    private void integer(int value) {
        digest.update((byte) (value >>> 24));
        digest.update((byte) (value >>> 16));
        digest.update((byte) (value >>> 8));
        digest.update((byte) value);
    }

    /** Only this final writer can consume private canonical bytes; no caller-supplied digest sees them. */
    void bytes(byte[] payload, int length) {
        digest.update(payload, 0, length);
    }
}
