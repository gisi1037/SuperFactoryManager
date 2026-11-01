package ca.teamdman.sfm.common.program.project;

import java.util.Objects;

/** Private immutable build source. A digest or execution/copy grant alone never authorizes this payload. */
public final class PublicationSource {

    public final PublicationReceipt receipt;
    public final String source;

    public PublicationSource(PublicationReceipt receipt, String source) {
        this.receipt = Objects.requireNonNull(receipt, "receipt");
        if (!receipt.digest.equals(new PublishedProgram(source).digest()))
            throw new IllegalArgumentException("Published source does not match its receipt");
        this.source = source;
    }
}
