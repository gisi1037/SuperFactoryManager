package ca.teamdman.sfm.client.blueprint;

import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

/** Event-driven diagnostic refresh. Ticks consume a scheduled check; idle ticks never parse source. */
public final class StudioDraftDiagnostics {

    public static final class Result {

        public final String message;
        /** Null when an incomplete graph or pending inspector takes precedence; prior decorations stay intact. */
        public final ProgramBuildResult compiled;

        private Result(String message, ProgramBuildResult compiled) {
            this.message = message;
            this.compiled = compiled;
        }
    }

    private int delay = -1;
    private boolean pending;

    public void schedule(int ticks) {
        delay = Math.max(0, ticks);
    }

    /** Both entering AND leaving pending state invalidate its message, including Cancel and Reset. */
    public void pendingChanged(boolean current) {
        if (pending == current) return;
        pending = current;
        schedule(0);
    }

    public boolean tick() {
        return delay >= 0 && --delay < 0;
    }

    public Result check(StudioDocument document, boolean currentPending) {
        delay = -1;
        pending = currentPending;
        if (document.incomplete()) return new Result("Visual draft: " + document.problem(), null);
        if (currentPending)
            return new Result("Inspector has unapplied changes. Apply/Cancel Fields or Apply/Reset the clause.", null);
        ProgramBuildResult built = new ProgramBuilder(document.code()).build();
        return new Result(
            built.isSuccess() ? "Draft: valid | Ctrl+S save | Ctrl+Space insert | F4 error" : built.getErrorMessage(),
            built);
    }
}
