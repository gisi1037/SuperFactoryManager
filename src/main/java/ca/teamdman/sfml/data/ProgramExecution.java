package ca.teamdman.sfml.data;

import java.util.IdentityHashMap;
import java.util.Map;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.ast.Trigger;

/** One manager activation's bounded RAM continuations; a compiled Program never owns these values. */
public final class ProgramExecution {

    public static final int MAX_SLEEPING_TRIGGERS = 64;
    private final Map<Trigger, Sleeping> sleeping = new IdentityHashMap<>();

    private static final class Sleeping {

        final DataRuntime runtime;
        final ProgramContext.SuspendedInputs inputs;
        final long due;

        Sleeping(DataRuntime runtime, ProgramContext.SuspendedInputs inputs, long due) {
            this.runtime = runtime;
            this.inputs = inputs;
            this.due = due;
        }
    }

    public int sleepingCount() {
        return sleeping.size();
    }

    public void clear() {
        sleeping.clear();
    }

    public void tick(Program program, ProgramContext context, int localCount, boolean needsState, boolean started) {
        DataRuntime live = started ? context.getDataRuntime() : null;
        long now = context.getWorldTime();
        try {
            for (Trigger trigger : program.getTriggers()) {
                Sleeping pending = sleeping.get(trigger);
                if (pending != null ? now < pending.due : !trigger.shouldRun(context)) continue;
                if (live == null) {
                    context.beginDataInvocation(localCount, needsState);
                    live = context.getDataRuntime();
                }
                live.setGameTick(now);
                DataRuntime runtime;
                context.clearAllInputs();
                if (pending == null) runtime = live.triggerFrame(localCount, now);
                else {
                    sleeping.remove(trigger);
                    runtime = pending.runtime;
                    runtime.bind(live);
                    context.restoreInputs(pending.inputs);
                }
                context.setExecutionRuntime(runtime);
                context.setDidSomething(true);
                boolean paused = false;
                try {
                    trigger.getBlock()
                        .tick(context);
                } catch (Continuation.Pause pause) {
                    if (sleeping.size() >= MAX_SLEEPING_TRIGGERS)
                        throw new IllegalArgumentException("WAIT sleeping-trigger limit exceeded");
                    Sleeping saved = new Sleeping(runtime, context.suspendInputs(), pause.due);
                    sleeping.put(trigger, saved);
                    Continuation.Bounds bounds = new Continuation.Bounds();
                    for (Sleeping value : sleeping.values()) {
                        value.runtime.retain(bounds);
                        value.inputs.retain(bounds);
                    }
                    runtime.detach();
                    paused = true;
                } finally {
                    context.clearAllInputs();
                    context.setExecutionRuntime(live);
                    if (!paused) runtime.endTrigger();
                }
            }
        } catch (RuntimeException | Error failure) {
            clear();
            throw failure;
        }
    }
}
