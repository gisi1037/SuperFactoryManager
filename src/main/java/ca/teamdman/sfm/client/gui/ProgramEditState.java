package ca.teamdman.sfm.client.gui;

import java.util.concurrent.atomic.AtomicLong;

import ca.teamdman.sfm.common.program.ProgramEditSnapshot;

/** Request correlation and timeout state, independent of Minecraft screens and networking. */
public final class ProgramEditState {

    public enum Action {
        OPEN,
        SAVE
    }

    private static final AtomicLong IDS = new AtomicLong();
    private long pending;
    private int ticks;
    private Action action;
    private ProgramEditSnapshot snapshot;

    public long begin(Action next) {
        if (isPending()) throw new IllegalStateException("A program request is already pending");
        if (next == Action.SAVE && snapshot == null) throw new IllegalStateException("Program is not loaded");
        action = next;
        ticks = 0;
        return pending = IDS.incrementAndGet();
    }

    public boolean receive(long id, boolean accepted, ProgramEditSnapshot next) {
        if (pending == 0 || id != pending) return false;
        pending = 0;
        if (accepted && next != null) snapshot = next;
        return true;
    }

    public boolean tick() {
        if (!isPending() || ++ticks <= 200) return false;
        pending = 0;
        return true;
    }

    public void cancel() {
        pending = 0;
    }

    public boolean isPending() {
        return pending != 0;
    }

    public boolean isEditable() {
        return snapshot != null && !isPending();
    }

    public Action action() {
        return action;
    }

    public ProgramEditSnapshot snapshot() {
        return snapshot;
    }
}
