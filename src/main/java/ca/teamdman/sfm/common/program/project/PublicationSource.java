package ca.teamdman.sfm.common.program.project;

import java.util.Objects;

import ca.teamdman.sfm.common.util.BoundedUtf8;

/** Private immutable build source. A digest or execution/copy grant alone never authorizes this payload. */
public final class PublicationSource {

    public final PublicationReceipt receipt;
    public final String source;
    public final int format;
    public final String sourceDigest;

    public PublicationSource(PublicationReceipt receipt, String source) {
        this(receipt, new PublishedProgram(source));
    }

    /** Storage verifies the complete closed bundle before returning only its author's root source. */
    public PublicationSource(PublicationReceipt receipt, PublishedProgram verifiedBundle) {
        this.receipt = Objects.requireNonNull(receipt, "receipt");
        if (!receipt.digest.equals(
            Objects.requireNonNull(verifiedBundle, "verifiedBundle")
                .digest()))
            throw new IllegalArgumentException("Published source does not match its receipt");
        source = verifiedBundle.source;
        format = verifiedBundle.format;
        sourceDigest = new PublishedProgram(source).digest();
    }

    private PublicationSource(PublicationReceipt receipt, String source, int format, String sourceDigest) {
        this.receipt = Objects.requireNonNull(receipt, "receipt");
        BoundedUtf8.encode(source, ProjectWorkspace.MAX_SOURCE_BYTES);
        if (format != PublishedProgram.FORMAT && format != PublishedProgram.LINKED_FORMAT
            || !new PublishedProgram(source).digest()
                .equals(sourceDigest)
            || format == PublishedProgram.FORMAT && !receipt.digest.equals(sourceDigest))
            throw new IllegalArgumentException("Published source does not match its receipt");
        this.source = source;
        this.format = format;
        this.sourceDigest = sourceDigest;
    }

    /**
     * Decode only an authenticated author-only server reply. A V2 root checksum verifies this payload, not the omitted
     * private dependency closure; the server verifies the full receipt digest and the client fences its selection.
     */
    public static PublicationSource fromWire(PublicationReceipt receipt, String source, int format,
        String sourceDigest) {
        return new PublicationSource(receipt, source, format, sourceDigest);
    }
}
