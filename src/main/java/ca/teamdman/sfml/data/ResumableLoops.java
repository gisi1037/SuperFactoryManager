package ca.teamdman.sfml.data;

import java.util.function.Supplier;

/** Loop cursors live in the caller's continuation. Null means normal completion; RETURN propagates. */
public final class ResumableLoops {

    private static final Object JAVA_INIT = new Object(), JAVA_BODY = new Object(), JAVA_STEP = new Object();
    private static final int INIT = 0, TEST = 1, BODY = 2, STEP = 3;

    private ResumableLoops() {}

    /** Each phase retains its own frame; a suspended update must never reenter the completed body. */
    public static DataValue javaFor(DataRuntime runtime, Object key, Supplier<DataValue> initializer,
        Supplier<Boolean> condition, Supplier<DataValue> body, Supplier<DataValue> step) {
        Continuation flow = runtime.continuation();
        Continuation.Progress cursor = flow.progress(key);
        while (true) {
            switch (cursor.phase) {
                case INIT:
                    if (initializer != null) flow.command(JAVA_INIT, initializer);
                    cursor.phase = TEST;
                    break;
                case TEST:
                    // Reserve a test once, including when its expression suspends and resumes later.
                    if (!cursor.initialized) {
                        runtime.loopStep();
                        cursor.initialized = true;
                    }
                    if (!runtime.evaluate(cursor, () -> condition == null || condition.get())) {
                        flow.finish(key);
                        return null;
                    }
                    cursor.initialized = false;
                    cursor.phase = BODY;
                    break;
                case BODY:
                    DataValue returned = flow.command(JAVA_BODY, body);
                    if (returned != null) {
                        flow.finish(key);
                        return returned;
                    }
                    cursor.phase = STEP;
                    break;
                case STEP:
                    if (step != null) flow.command(JAVA_STEP, step);
                    flow.nextIteration();
                    cursor.phase = TEST;
                    break;
                default:
                    throw new IllegalStateException("Invalid FOR phase");
            }
        }
    }

    public static DataValue whileLoop(DataRuntime runtime, Object key, Supplier<Boolean> condition,
        Supplier<DataValue> body) {
        Continuation flow = runtime.continuation();
        Continuation.Progress cursor = flow.progress(key);
        while (true) {
            if (!cursor.inBody) {
                runtime.loopStep();
                if (!runtime.evaluate(cursor, condition)) {
                    flow.finish(key);
                    return null;
                }
                cursor.inBody = true;
            }
            DataValue returned = body.get();
            if (returned != null) {
                flow.finish(key);
                return returned;
            }
            cursor.inBody = false;
            flow.nextIteration();
        }
    }

    public static DataValue counted(DataRuntime runtime, Object key, DataCompiler.Binding binding, DataExpression first,
        DataExpression last, DataExpression step, Supplier<DataValue> body) {
        Continuation flow = runtime.continuation();
        Continuation.Progress cursor = flow.progress(key);
        if (!cursor.initialized) {
            long current = first.evaluate(runtime)
                .asLong();
            long end = last.evaluate(runtime)
                .asLong();
            long increment = step == null ? 1
                : step.evaluate(runtime)
                    .asLong();
            if (increment == 0) throw new IllegalArgumentException("FOR STEP cannot be zero");
            cursor.current = current;
            cursor.end = end;
            cursor.step = increment;
            cursor.initialized = true;
        }
        while (cursor.step > 0 ? cursor.current <= cursor.end : cursor.current >= cursor.end) {
            if (!cursor.inBody) {
                runtime.loopStep();
                binding.write(runtime, DataValue.integer(cursor.current));
                cursor.inBody = true;
            }
            DataValue returned = body.get();
            if (returned != null) {
                flow.finish(key);
                return returned;
            }
            cursor.inBody = false;
            flow.nextIteration();
            if (cursor.step > 0 ? cursor.current > Long.MAX_VALUE - cursor.step
                : cursor.current < Long.MIN_VALUE - cursor.step) break;
            cursor.current += cursor.step;
        }
        flow.finish(key);
        return null;
    }

    public static DataValue array(DataRuntime runtime, Object key, DataCompiler.Binding binding,
        DataExpression expression, Supplier<DataValue> body) {
        return array(runtime, key, null, binding, expression, body);
    }

    public static DataValue array(DataRuntime runtime, Object key, DataCompiler.Binding indexBinding,
        DataCompiler.Binding binding, DataExpression expression, Supplier<DataValue> body) {
        Continuation flow = runtime.continuation();
        Continuation.Progress cursor = flow.progress(key);
        if (!cursor.initialized) {
            cursor.snapshot = expression.evaluate(runtime);
            cursor.initialized = true;
        }
        while (cursor.index < cursor.snapshot.size()) {
            if (!cursor.inBody) {
                runtime.loopStep();
                if (indexBinding != null) indexBinding.write(runtime, DataValue.integer(cursor.index));
                binding.write(runtime, cursor.snapshot.get(cursor.index));
                cursor.inBody = true;
            }
            DataValue returned = body.get();
            if (returned != null) {
                flow.finish(key);
                return returned;
            }
            cursor.inBody = false;
            cursor.index++;
            flow.nextIteration();
        }
        flow.finish(key);
        return null;
    }
}
