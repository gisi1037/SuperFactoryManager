package ca.teamdman.sfml.data;

/** Confirmed progress only; contains no world objects or transfer authority. */
public final class ItemTransferResult {

    public enum Status {
        COMPLETE,
        PARTIAL,
        NO_MATCH,
        NO_CAPACITY,
        UNAVAILABLE
    }

    private final long requested, moved;
    private final Status status;

    public ItemTransferResult(long requested, long moved, Status status) {
        if (requested < 0 || moved < 0 || moved > requested || status == null)
            throw new IllegalArgumentException("Invalid item transfer result");
        this.requested = requested;
        this.moved = moved;
        this.status = status;
    }

    public long getRequested() {
        return requested;
    }

    public long getMoved() {
        return moved;
    }

    public long getRemaining() {
        return requested - moved;
    }

    public Status getStatus() {
        return status;
    }
}
