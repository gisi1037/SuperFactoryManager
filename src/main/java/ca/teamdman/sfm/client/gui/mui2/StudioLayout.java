package ca.teamdman.sfm.client.gui.mui2;

/** Logical GUI coordinates, independent of display pixels and Minecraft GUI scale. */
public final class StudioLayout {

    public static boolean compact(int width, int height) {
        return width < 600 || height < 300;
    }

    public static StudioLayout station(int width, int height, int actions, int categories) {
        return new StudioLayout(width, compact(width, height) ? 5 : actions, compact(width, height) ? 0 : categories);
    }

    public static int splitMiddle(int left, int right, int width, double ratio) {
        int available = Math.max(0, width - left - right);
        int minimum = Math.min(200, available / 2);
        double bounded = Double.isNaN(ratio) ? .5 : Math.max(0, Math.min(1, ratio));
        int split = (int) Math.round(available * bounded);
        return left + Math.max(minimum, Math.min(available - minimum, split));
    }

    public final int columns, buttonWidth, categoryTop, editorTop;

    public static int inspectorWidth(int screenWidth) {
        return Math.max(1, Math.min(420, screenWidth - 16));
    }

    public static int inspectorHeight(int screenHeight) {
        return Math.max(1, Math.min(440, screenHeight - 16));
    }

    public StudioLayout(int width, int actions, int categories) {
        int available = Math.max(1, width - 16);
        int capacity = Math.max(1, (available + 3) / 55);
        int count = Math.max(1, Math.max(actions, categories));
        int rows = (count + capacity - 1) / capacity;
        columns = (count + rows - 1) / rows;
        buttonWidth = Math.max(1, (available - (columns - 1) * 3) / columns);
        categoryTop = 44 + rows(actions) * 21;
        editorTop = categoryTop + rows(categories) * 19 + 4;
    }

    private int rows(int count) {
        return (count + columns - 1) / columns;
    }

    public int x(int index) {
        return 8 + index % columns * (buttonWidth + 3);
    }

    public int actionY(int index) {
        return 44 + index / columns * 21;
    }

    public int categoryY(int index) {
        return categoryTop + index / columns * 19;
    }
}
