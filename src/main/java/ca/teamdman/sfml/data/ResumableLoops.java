package ca.teamdman.sfml.data;

import java.util.function.Supplier;

/** Loop cursors live in the caller's continuation. Null means normal completion; RETURN propagates. */
public final class ResumableLoops {

    private ResumableLoops() {}

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
        Continuation flow = runtime.continuation();
        Continuation.Progress cursor = flow.progress(key);
        if (!cursor.initialized) {
            cursor.snapshot = expression.evaluate(runtime);
            cursor.initialized = true;
        }
        while (cursor.index < cursor.snapshot.size()) {
            if (!cursor.inBody) {
                runtime.loopStep();
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
