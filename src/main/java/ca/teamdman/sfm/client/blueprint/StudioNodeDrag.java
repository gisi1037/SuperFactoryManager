package ca.teamdman.sfm.client.blueprint;

import java.util.UUID;

/** Absolute pointer anchoring avoids snap drift; a whole gesture owns one undo entry. */
public final class StudioNodeDrag {

    public static final int GRID = 24;
    private final StudioDocument document;
    private UUID selected;
    private double pointerX, pointerY, originX, originY, zoom;
    private boolean changed;

    public StudioNodeDrag(StudioDocument document) {
        this.document = document;
    }

    public boolean active() {
        return selected != null;
    }

    public void begin(UUID id, double x, double y, double scale) {
        if (active()) throw new IllegalStateException("Finish the current drag first");
        BlueprintGraph.Node node = document.graph()
            .node(id);
        if (node == null || !Double.isFinite(scale) || scale <= 0) throw new IllegalArgumentException("Invalid drag");
        selected = id;
        pointerX = x;
        pointerY = y;
        originX = node.x();
        originY = node.y();
        zoom = scale;
        changed = false;
    }

    /** Frame sampling updates positions only; serialization waits until release/focus loss. */
    public void update(double x, double y, boolean snap) {
        if (!active() || document.graph() == null) return;
        BlueprintGraph.Node node = document.graph()
            .node(selected);
        if (node == null) return;
        double nextX = coordinate(originX + (x - pointerX) / zoom, snap);
        double nextY = coordinate(originY + (y - pointerY) / zoom, snap);
        if (node.x() == nextX && node.y() == nextY) return;
        if (!changed) {
            document.checkpoint();
            changed = true;
        }
        node.setPosition(nextX, nextY);
    }

    private static double coordinate(double value, boolean snap) {
        return Math.max(-999000, Math.min(999000, snap ? Math.round(value / GRID) * GRID : value));
    }

    public boolean finish() {
        boolean edited = changed;
        selected = null;
        changed = false;
        if (edited) document.graphChanged();
        return edited;
    }
}
