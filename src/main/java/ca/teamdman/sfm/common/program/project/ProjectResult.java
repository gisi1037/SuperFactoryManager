package ca.teamdman.sfm.common.program.project;

/** Storage outcomes are explicit; a failed save never acknowledges a newer draft. */
public final class ProjectResult<T> {

    public enum Status {
        OK,
        NOT_FOUND,
        STALE,
        INVALID,
        LIMIT,
        IO_ERROR,
        CLOSED,
        BUSY
    }

    public final Status status;
    public final T value;
    public final String message;

    private ProjectResult(Status status, T value, String message) {
        this.status = status;
        this.value = value;
        this.message = message;
    }

    public boolean isSuccess() {
        return status == Status.OK;
    }

    static <T> ProjectResult<T> success(T value) {
        return new ProjectResult<>(Status.OK, value, "");
    }

    static <T> ProjectResult<T> failure(Status status, String message) {
        return new ProjectResult<>(status, null, message);
    }
}
