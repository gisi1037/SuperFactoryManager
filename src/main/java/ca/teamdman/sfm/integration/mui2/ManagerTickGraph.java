package ca.teamdman.sfm.integration.mui2;

import java.util.Locale;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Small live MUI2 history plot. Drawing code is stripped from dedicated servers. */
public final class ManagerTickGraph extends Widget<ManagerTickGraph> {

    private final Supplier<double[]> history;
    private final DoubleSupplier peak;
    private int hovered = -1;
    private int formattedIndex = Integer.MIN_VALUE;
    private long formattedBits;
    private String readout = "";

    public ManagerTickGraph(Supplier<double[]> history, DoubleSupplier peak) {
        this.history = history;
        this.peak = peak;
        name("tick_history");
        addTooltipLine("Complete manager update time; green <= 5 ms, yellow <= 15 ms.");
    }

    /** Format only when the displayed sample changes, rather than once per rendered frame. */
    public String readout() {
        double[] values = history.get();
        int index = isHovering() && hovered >= 0 && hovered < values.length ? hovered : -1;
        double value = index >= 0 ? values[index] : peak.getAsDouble();
        long bits = Double.doubleToLongBits(value);
        if (index != formattedIndex || bits != formattedBits) {
            readout = (index >= 0 ? "Hovered tick: " : "Peak tick: ") + colorCode(value)
                + String.format(Locale.ROOT, "%.3f", value)
                + "\u00a7r ms";
            formattedIndex = index;
            formattedBits = bits;
        }
        return readout;
    }

    private static String colorCode(double ms) {
        return ms <= 5 ? "\u00a7a" : ms <= 15 ? "\u00a7e" : "\u00a7c";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> theme) {
        int width = getArea().width;
        int height = getArea().height;
        GuiDraw.drawRect(0, 0, width, height, 0x33000000);
        GuiDraw.drawRect(0, height - 1, width, 1, 0x88000000);
        double[] values = history.get();
        if (values.length == 0 || width < 2 || height < 2) return;
        float step = (width - 1f) / Math.max(1, values.length - 1);
        hovered = isHovering() ? Math.max(0, Math.min(values.length - 1, Math.round(context.getMouseX() / step))) : -1;
        double denominator = Math.log10(Math.max(50_000_000D, peak.getAsDouble() * 1_000_000D));
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_CURRENT_BIT | GL11.GL_LINE_BIT | GL11.GL_COLOR_BUFFER_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(1.5f);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i = 0; i < values.length; i++) {
            double ms = values[i];
            double normalized = Math.log10(Math.max(1D, ms * 1_000_000D)) / denominator;
            float y = height - 1 - (float) Math.max(0, Math.min(1, normalized)) * (height - 2);
            GL11.glColor4f(ms <= 5 ? 0.33f : 1f, ms <= 15 ? 1f : 0.33f, 0.33f, 1f);
            GL11.glVertex2f(i * step, y);
        }
        GL11.glEnd();
        if (hovered >= 0) {
            GL11.glColor4f(1f, 1f, 1f, 0.85f);
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex2f(hovered * step, 0);
            GL11.glVertex2f(hovered * step, height);
            GL11.glEnd();
        }
        GL11.glPopAttrib();
    }
}
