package ca.teamdman.sfml.data;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Invocation-independent execution values only. No world, inventory or provider is captured here. */
public final class Continuation {

    public static final class Pause extends RuntimeException {

        private static final long serialVersionUID = 1L;
        public final long due;

        Pause(long due) {
            super(null, null, false, false);
            this.due = due;
        }
    }

    /** A compiled loop owns one cursor per active invocation, never one on its shared AST. */
    public static final class Progress {

        public int index, phase;
        public long current, end, step, due;
        public boolean initialized, inBody;
        public DataValue snapshot;
    }

    public interface RetainedValues {

        void visit(Consumer<DataValue> values);
    }

    /** Compiler-owned snapshots may retain bounded primitive paths as well as data values. */
    public interface RetainedState {

        void retain(Bounds bounds);
    }

    private static final class Frame {

        final Map<Object, Frame> children = new IdentityHashMap<>();
        final Map<Object, Progress> cursors = new IdentityHashMap<>();
        final Map<Object, Evaluation> expressions = new IdentityHashMap<>();
    }

    private static final class Evaluation {

        final Map<Object, DataValue> nodes = new IdentityHashMap<>();
        final Map<Object, DataRuntime> calls = new IdentityHashMap<>();
        boolean complete;
        Object value;
    }

    private final DataRuntime owner;
    private final Frame root = new Frame();
    private Frame current = root;
    private Evaluation active;

    Continuation(DataRuntime owner) {
        this.owner = owner;
    }

    public <T> T command(Object key, Supplier<T> action) {
        Frame parent = current;
        Frame frame = parent.children.get(key);
        if (frame == null) {
            owner.allocate(4);
            frame = new Frame();
            parent.children.put(key, frame);
        }
        current = frame;
        try {
            T result = action.get();
            parent.children.remove(key);
            return result;
        } finally {
            current = parent;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T evaluate(Object key, Supplier<T> expression) {
        Evaluation saved = active;
        Evaluation evaluation = current.expressions.get(key);
        if (evaluation == null) {
            owner.allocate(4);
            evaluation = new Evaluation();
            current.expressions.put(key, evaluation);
        }
        if (evaluation.complete) return (T) evaluation.value;
        active = evaluation;
        try {
            T value = expression.get();
            evaluation.value = value;
            evaluation.complete = true;
            return value;
        } finally {
            active = saved;
        }
    }

    DataValue node(Object key, Supplier<DataValue> expression) {
        if (active == null) return expression.get();
        DataValue value = active.nodes.get(key);
        if (value != null) return value;
        owner.allocate(1);
        value = expression.get();
        active.nodes.put(key, value);
        return value;
    }

    public Progress progress(Object key) {
        Progress progress = current.cursors.get(key);
        if (progress == null) {
            owner.allocate(2);
            progress = new Progress();
            current.cursors.put(key, progress);
        }
        return progress;
    }

    public void finish(Object key) {
        current.cursors.remove(key);
    }

    public void nextIteration() {
        current.expressions.clear();
    }

    DataRuntime call(Object key) {
        if (active == null) throw new IllegalStateException("Resumable calls require an expression frame");
        return active.calls.get(key);
    }

    void call(Object key, DataRuntime frame) {
        if (active == null) throw new IllegalStateException("Resumable calls require an expression frame");
        active.calls.put(key, frame);
    }

    void finishCall(Object key) {
        active.calls.remove(key);
    }

    public void waitTicks(Object key, long ticks, long now) {
        if (ticks < 0) throw new IllegalArgumentException("WAIT ticks cannot be negative");
        if (ticks == 0) return;
        Progress progress = progress(key);
        if (!progress.initialized) {
            progress.due = Math.addExact(now, ticks);
            progress.initialized = true;
            throw new Pause(progress.due);
        }
        if (now < progress.due) throw new Pause(progress.due);
        finish(key);
    }

    void bindChildren(DataRuntime live) {
        bind(root, live);
    }

    private void bind(Frame frame, DataRuntime live) {
        for (Frame child : frame.children.values()) bind(child, live);
        for (Evaluation evaluation : frame.expressions.values())
            for (DataRuntime call : evaluation.calls.values()) call.bind(live);
    }

    void detach() {
        detach(root);
    }

    private void detach(Frame frame) {
        for (Frame child : frame.children.values()) detach(child);
        for (Evaluation evaluation : frame.expressions.values())
            for (DataRuntime call : evaluation.calls.values()) call.detach();
    }

    public void retain(Bounds bounds) {
        retain(root, bounds);
    }

    private void retain(Frame frame, Bounds bounds) {
        bounds.entry();
        for (Frame child : frame.children.values()) retain(child, bounds);
        for (Progress progress : frame.cursors.values()) {
            bounds.entry();
            bounds.value(progress.snapshot);
        }
        for (Evaluation evaluation : frame.expressions.values()) {
            bounds.entry();
            if (evaluation.value instanceof DataValue) bounds.value((DataValue) evaluation.value);
            else if (evaluation.value instanceof DataReference) ((DataReference) evaluation.value).retain(bounds);
            else if (evaluation.value instanceof RetainedState) ((RetainedState) evaluation.value).retain(bounds);
            else if (evaluation.value instanceof RetainedValues)
                ((RetainedValues) evaluation.value).visit(bounds::value);
            else if (evaluation.value instanceof int[]) bounds.opaque(16 + ((int[]) evaluation.value).length * 4);
            for (DataValue value : evaluation.nodes.values()) {
                bounds.entry();
                bounds.value(value);
            }
            for (DataRuntime call : evaluation.calls.values()) call.retain(bounds);
        }
    }

    /** Manager-wide, conservative bounds across all its sleeping trigger/call frames. */
    public static final class Bounds {

        private final Map<DataValue, Boolean> seen = new IdentityHashMap<>();
        private int entries, cells, characters, bytes;

        public void entry() {
            if (++entries > 4096) throw new IllegalArgumentException("WAIT retained-frame budget exceeded");
        }

        public void value(DataValue value) {
            if (value == null || seen.put(value, Boolean.TRUE) != null) return;
            cells = Math.addExact(cells, value.getRetainedCellCount());
            characters = Math.addExact(characters, value.getRetainedCharacters());
            bytes = Math.addExact(bytes, value.getRetainedOpaqueBytes());
            if (cells > ProgramGlobals.MAX_CELLS || characters > ProgramGlobals.MAX_CHARACTERS
                || bytes > ProgramGlobals.MAX_OPAQUE_BYTES)
                throw new IllegalArgumentException("WAIT retained-value budget exceeded");
        }

        public void opaque(int amount) {
            bytes = Math.addExact(bytes, amount);
            if (bytes > ProgramGlobals.MAX_OPAQUE_BYTES)
                throw new IllegalArgumentException("WAIT retained-value budget exceeded");
        }
    }
}
