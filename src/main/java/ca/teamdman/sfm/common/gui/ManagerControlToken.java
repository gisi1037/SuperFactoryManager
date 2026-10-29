package ca.teamdman.sfm.common.gui;

import java.util.Objects;
import java.util.UUID;

import io.netty.buffer.ByteBuf;

/** Immutable authority for a manager action, bound to one container opening and disk binding. */
public final class ManagerControlToken {

    public static final int WIRE_BYTES = 28;
    public final UUID sessionId;
    public final long diskGeneration;
    public final int windowId;

    public ManagerControlToken(UUID sessionId, long diskGeneration, int windowId) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        if (diskGeneration < 0) throw new IllegalArgumentException("Negative disk generation");
        if (windowId < 1 || windowId > 100) throw new IllegalArgumentException("Invalid manager window ID");
        this.diskGeneration = diskGeneration;
        this.windowId = windowId;
    }

    public boolean matches(UUID activeSession, long activeGeneration, int activeWindow) {
        return sessionId.equals(activeSession) && diskGeneration == activeGeneration && windowId == activeWindow;
    }

    public void write(ByteBuf buffer) {
        buffer.writeLong(sessionId.getMostSignificantBits());
        buffer.writeLong(sessionId.getLeastSignificantBits());
        buffer.writeLong(diskGeneration);
        buffer.writeInt(windowId);
    }

    public static ManagerControlToken read(ByteBuf buffer) {
        if (buffer.readableBytes() < WIRE_BYTES) throw new IllegalArgumentException("Truncated manager control token");
        return new ManagerControlToken(
            new UUID(buffer.readLong(), buffer.readLong()),
            buffer.readLong(),
            buffer.readInt());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ManagerControlToken)) return false;
        ManagerControlToken token = (ManagerControlToken) other;
        return matches(token.sessionId, token.diskGeneration, token.windowId);
    }

    @Override
    public int hashCode() {
        int result = sessionId.hashCode();
        result = 31 * result + Long.hashCode(diskGeneration);
        return 31 * result + windowId;
    }
}
