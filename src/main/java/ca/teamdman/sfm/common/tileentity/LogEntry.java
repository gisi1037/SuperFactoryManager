package ca.teamdman.sfm.common.tileentity;

public class LogEntry {

    private final long timestamp;
    private final LogLevel level;
    private final String message;

    public LogEntry(LogLevel level, String message) {
        this(System.currentTimeMillis(), level, message);
    }

    public LogEntry(long timestamp, LogLevel level, String message) {
        this.timestamp = timestamp;
        this.level = level != null ? level : LogLevel.INFO;
        this.message = message != null ? message : "";
    }

    public long getTimestamp() {
        return timestamp;
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "[" + level.name() + "] " + message;
    }
}
