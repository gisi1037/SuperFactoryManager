package ca.teamdman.sfm.client.blueprint;

/** One vertical origin for the legacy editor's two toolbar rows, clipping and pointer transforms. */
public final class BlueprintCanvasGeometry {

    public static final int TOP = 72;

    private BlueprintCanvasGeometry() {}

    public static double worldY(double screen, double pan, double zoom) {
        return (screen - TOP - pan) / zoom;
    }

    public static double screenY(double world, double pan, double zoom) {
        return TOP + pan + world * zoom;
    }

    public static double anchoredPanY(double screen, double world, double zoom) {
        return screen - TOP - world * zoom;
    }

    public static double fitZoom(int width, int bottom, double spanX, double spanY) {
        return Math.max(.25, Math.min(1, Math.min((width - 40) / spanX, Math.max(40, bottom - TOP - 16) / spanY)));
    }
}
