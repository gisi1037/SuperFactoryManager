package ca.teamdman.sfm.integration.mui2;

/** Fixed machine footprint; only its height contracts on small scaled screens. */
public final class ManagerMachineLayout {

    public static final int WIDTH = 312;
    public static final int HEIGHT = 288;
    public static final int CONTENT_TOP = 64;
    public static final int INVENTORY_RESERVE = 98;

    private ManagerMachineLayout() {}

    public static int height(int screenHeight) {
        return Math.min(HEIGHT, Math.max(232, screenHeight - 8));
    }

    public static int contentHeight(int screenHeight) {
        return height(screenHeight) - CONTENT_TOP - INVENTORY_RESERVE;
    }
}
