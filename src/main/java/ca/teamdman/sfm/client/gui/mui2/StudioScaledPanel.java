package ca.teamdman.sfm.client.gui.mui2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.widget.sizer.Unit;

/** One MUI transform handles drawing, clipping and hit testing for the editor and all its modals. */
public class StudioScaledPanel extends ModularPanel {

    private static StudioScalePreference preference;
    private static int gameScale = 1, displayWidth = 320, displayHeight = 240;
    private static float ratio = 1;

    public StudioScaledPanel(String name) {
        super(name);
        left(() -> (logicalWidth(getScreen()) - getArea().width) / 2.0, Unit.Measure.PIXEL);
        top(() -> (logicalHeight(getScreen()) - getArea().height) / 2.0, Unit.Measure.PIXEL);
    }

    /** Called only on opening, resolution changes or a deliberate preference change, never per draw. */
    public static void updateResolution() {
        Minecraft mc = Minecraft.getMinecraft();
        if (preference == null) preference = new StudioScalePreference(
            new java.io.File(mc.mcDataDir, "config/sfm-studio.properties").toPath());
        displayWidth = mc.displayWidth;
        displayHeight = mc.displayHeight;
        gameScale = new ScaledResolution(mc, displayWidth, displayHeight).getScaleFactor();
        ratio = preference.effective(displayWidth, displayHeight, gameScale) / (float) gameScale;
    }

    public static int logicalWidth(ModularScreen screen) {
        return StudioScalePreference.logicalSize(screen.getScreenArea().width, ratio);
    }

    public static int logicalHeight(ModularScreen screen) {
        return StudioScalePreference.logicalSize(screen.getScreenArea().height, ratio);
    }

    public static String scaleLabel() {
        int selected = preference.preferred();
        int effective = preference.effective(displayWidth, displayHeight, gameScale);
        return selected == 0 ? "Game " + effective + "x"
            : selected == effective ? effective + "x" : selected + "x (" + effective + "x)";
    }

    public static void changeScale(ModularScreen screen, int direction) {
        int maximum = StudioScalePreference.maximum(displayWidth, displayHeight);
        int next = direction == 0 ? 0
            : Math.max(1, Math.min(maximum, preference.effective(displayWidth, displayHeight, gameScale) + direction));
        preference.remember(next);
        updateResolution();
        // Resize existing widgets; do not reopen the station or discard source, undo or pending fields.
        for (ModularPanel open : screen.getPanelManager()
            .getOpenPanels()) open.scheduleResize();
    }

    static int logicalMouse(ModularPanel panel, int coordinate) {
        return panel instanceof StudioScaledPanel ? (int) Math.floor(coordinate / ratio) : coordinate;
    }

    @Override
    public void transform(IViewportStack stack) {
        // Apply before the panel translation so centered modals and child coordinates share one space.
        stack.scale(ratio, ratio);
        super.transform(stack);
    }
}
