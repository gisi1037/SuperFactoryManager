package ca.teamdman.sfm.common.tileentity;

public enum LogLevel {

    OFF("Off", 0x888888),
    ERROR("Error", 0xFF5555),
    WARN("Warn", 0xFFAA00),
    INFO("Info", 0x55FF55),
    DEBUG("Debug", 0x55FFFF),
    TRACE("Trace", 0xAAAAAA);

    private final String displayName;
    private final int color;

    LogLevel(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }

    public boolean includes(LogLevel other) {
        if (this == OFF) return false;
        // Ordinal order: OFF(0), ERROR(1), WARN(2), INFO(3), DEBUG(4), TRACE(5)
        // If this is TRACE (5), it includes all. If this is ERROR (1), it includes only ERROR.
        return other.ordinal() <= this.ordinal() && other != OFF;
    }

    public static LogLevel fromString(String name) {
        if (name == null) return OFF;
        for (LogLevel l : values()) {
            if (l.name()
                .equalsIgnoreCase(name) || l.displayName.equalsIgnoreCase(name)) {
                return l;
            }
        }
        return OFF;
    }
}
