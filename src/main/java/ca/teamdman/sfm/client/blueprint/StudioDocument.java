package ca.teamdman.sfm.client.blueprint;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

/** One project source owner. View changes never replace unsupported code with a partial graph. */
public final class StudioDocument {

    private String code = "", source = "", sidecar = "", problem = "";
    private BlueprintGraph graph;
    private BlueprintDocument imported;
    private final Deque<Snapshot> undo = new ArrayDeque<>(), redo = new ArrayDeque<>();

    public String code() {
        return code;
    }

    public String source() {
        return source;
    }

    public String problem() {
        return problem;
    }

    public BlueprintGraph graph() {
        return graph;
    }

    public boolean incomplete() {
        return graph != null && !problem.isEmpty();
    }

    public void load(String source) {
        this.source = source;
        code = BlueprintDocument.withoutMetadata(source);
        try {
            sidecar = BlueprintDocument.prepareTextSave("", source).source;
        } catch (IllegalArgumentException oversizedDraft) {
            sidecar = "";
        }
        graph = null;
        imported = null;
        problem = "";
        undo.clear();
        redo.clear();
    }

    /** Keep the layout as a candidate; it is checked against new code when Visual is opened. */
    public void editCode(String code) {
        if (incomplete())
            throw new IllegalStateException("Finish or undo the disconnected visual draft before editing code.");
        if (this.code.equals(code)) return;
        if ((code.contains(BlueprintDocument.MARKER) || code.contains(BlueprintDocument.LEGACY_MARKER))
            && !BlueprintDocument.withoutMetadata(code)
                .equals(code)) {
            load(code);
            return;
        }
        this.code = code;
        source = attach(code, sidecar);
        graph = null;
        imported = null;
        problem = "";
        undo.clear();
        redo.clear();
    }

    private static String attach(String code, String sidecar) {
        if (sidecar.isEmpty()) return code;
        String candidate = code + (code.endsWith("\n") ? "" : "\n") + sidecar.trim();
        try {
            ca.teamdman.sfm.common.program.ProgramDocument.validateSource(candidate);
            return candidate;
        } catch (IllegalArgumentException tooLarge) {
            // Source takes precedence over optional layout at the serialized size limit.
            return code;
        }
    }

    public void openVisual() {
        if (graph != null) return;
        BlueprintDocument next = BlueprintDocument.open(source);
        imported = next;
        graph = next.graph;
        problem = "";
    }

    public void editGraph(Consumer<BlueprintGraph> edit) {
        if (graph == null) throw new IllegalStateException("Open Visual first.");
        Snapshot before = snapshot();
        try {
            edit.accept(graph);
        } catch (IllegalArgumentException error) {
            restoreFields(before);
            throw error;
        }
        push(undo, before);
        redo.clear();
        graphChanged();
    }

    /** Used once at the start of a drag, not for every pixel of movement. */
    public void checkpoint() {
        push(undo, snapshot());
        redo.clear();
    }

    public void graphChanged() {
        try {
            String next = imported == null ? BlueprintDocument.save(graph) : imported.saveEdited(graph);
            source = next;
            code = BlueprintDocument.withoutMetadata(next);
            sidecar = BlueprintDocument.prepareTextSave("", next).source;
            problem = "";
        } catch (IllegalArgumentException invalid) {
            // Keep both the incomplete graph and the last safe source. Save is explicitly blocked.
            problem = invalid.getMessage();
        }
    }

    public boolean undo() {
        return move(undo, redo);
    }

    public boolean redo() {
        return move(redo, undo);
    }

    private boolean move(Deque<Snapshot> from, Deque<Snapshot> to) {
        if (from.isEmpty()) return false;
        push(to, snapshot());
        restoreFields(from.pop());
        return true;
    }

    private static void push(Deque<Snapshot> history, Snapshot snapshot) {
        history.push(snapshot);
        while (history.size() > 32) history.removeLast();
    }

    public Snapshot snapshot() {
        return new Snapshot(this);
    }

    public void restore(Snapshot snapshot) {
        undo.clear();
        redo.clear();
        restoreFields(snapshot);
    }

    private void restoreFields(Snapshot snapshot) {
        source = snapshot.source;
        code = snapshot.code;
        sidecar = snapshot.sidecar;
        problem = snapshot.problem;
        graph = snapshot.graph == null ? null : snapshot.graph.copy();
        imported = snapshot.imported;
    }

    /** Heap-only recovery; no world, widget, authority token or callback is retained. */
    public static final class Snapshot {

        private final String source, code, sidecar, problem;
        private final BlueprintGraph graph;
        private final BlueprintDocument imported;

        private Snapshot(StudioDocument document) {
            source = document.source;
            code = document.code;
            sidecar = document.sidecar;
            problem = document.problem;
            graph = document.graph == null ? null : document.graph.copy();
            imported = document.imported;
        }
    }
}
