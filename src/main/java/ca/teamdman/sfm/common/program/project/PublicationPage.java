package ca.teamdman.sfm.common.program.project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable paged metadata for one authenticated owner's project; never includes source. */
public final class PublicationPage {

    public final List<PublicationReceipt> entries;
    public final int total, offset;

    public PublicationPage(List<PublicationReceipt> entries, int total, int offset) {
        this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
        this.total = total;
        this.offset = offset;
    }
}
