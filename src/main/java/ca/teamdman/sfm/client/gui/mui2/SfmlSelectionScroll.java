package ca.teamdman.sfm.client.gui.mui2;

/** Frame-rate-independent selection scrolling, with fractional pixels and bounded pause recovery. */
public final class SfmlSelectionScroll {

    private long last;
    private double carryX, carryY;
    public int dx, dy;

    public void reset(long now) {
        last = now;
        carryX = carryY = 0;
        dx = dy = 0;
    }

    public void update(long now, int x, int y, int width, int height, boolean wrap) {
        double seconds = Math.max(0, Math.min(.05, (now - last) / 1_000_000_000.0));
        last = now;
        double vx = wrap ? 0 : speed(x, width), vy = speed(y, height);
        carryX = vx == 0 ? 0 : carryX + vx * seconds;
        carryY = vy == 0 ? 0 : carryY + vy * seconds;
        dx = (int) carryX;
        dy = (int) carryY;
        carryX -= dx;
        carryY -= dy;
    }

    private static double speed(int pointer, int extent) {
        int edge = Math.min(12, Math.max(1, extent / 4));
        if (pointer < edge) return -Math.min(600.0, 40.0 + (edge - (double) pointer) * 12);
        if (pointer > extent - edge) return Math.min(600.0, 40.0 + (pointer - (double) extent + edge) * 12);
        return 0;
    }
}
