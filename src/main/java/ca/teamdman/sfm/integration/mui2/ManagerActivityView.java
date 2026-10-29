package ca.teamdman.sfm.integration.mui2;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** At most twenty timing bars plus a scheduler-clock marker, drawn only from the server-synced immutable sample. */
public final class ManagerActivityView extends Widget<ManagerActivityView> {

    private final Supplier<ManagerActivitySample> sample;
    private final boolean detailed;

    public ManagerActivityView(Supplier<ManagerActivitySample> sample, boolean detailed) {
        this.sample = sample;
        this.detailed = detailed;
        name(detailed ? "manager_activity_detail" : "manager_activity_compact");
        if (!detailed) {
            addTooltipLine("Full manager update cost, including idle checks/recovery/discovery; not item throughput.");
            addTooltipLine("The moving marker follows scheduler ticks, not statement or transfer progress.");
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> theme) {
        int width = getArea().width, height = getArea().height;
        if (width < 12 || height < 12) return;
        ManagerActivitySample value = sample.get();
        if (value == null) value = ManagerActivitySample.EMPTY;
        GuiDraw.drawRect(0, 0, width, height, 0x44000000);
        if (!detailed) {
            int middle = width / 2;
            plot(value, 4, 2, middle - 8, height - 4);
            clock(value, middle + 4, 3, width - middle - 8, 6);
            text(value.labels().clockLabel, middle + 4, 13, width - middle - 8, 0xffb7bcc5);
            return;
        }
        text(value.phase.label, 6, 4, width - 12, value.phase.color);
        text(value.phase.detail, 6, 16, width - 12, 0xffb7bcc5);
        text(value.labels().latestLabel, 6, 33, width / 2 - 12, 0xffe7e8eb);
        text(value.labels().averageLabel, width / 2, 33, width / 2 - 6, 0xffe7e8eb);
        text(value.labels().windowMaxLabel, 6, 45, width / 2 - 12, 0xffb7bcc5);
        text(value.labels().lifetimePeakLabel, width / 2, 45, width / 2 - 6, 0xffb7bcc5);
        text(value.labels().windowLabel, 6, 60, width - 12, 0xffe7e8eb);
        plot(value, 6, 73, width - 12, 54);
        text(value.labels().scaleLabel, 6, 131, width - 12, 0xffb7bcc5);
        text(value.labels().clockLabel, 6, 146, width - 12, value.phase.color);
        clock(value, 6, 158, width - 12, 7);
        text("Statement trace unavailable (not sampled).", 6, 174, width - 12, 0xff969aa5);
    }

    @SideOnly(Side.CLIENT)
    private static void plot(ManagerActivitySample value, int x, int y, int width, int height) {
        if (width < 2 || height < 2) return;
        GuiDraw.drawRect(x, y + height - 1, width, 1, 0xff515660);
        double scale = Math.max(.01, value.windowMaxMs);
        int first = ManagerActivitySample.HISTORY_LIMIT - value.count();
        for (int slot = 0; slot < ManagerActivitySample.HISTORY_LIMIT; slot++) {
            int left = x + slot * width / ManagerActivitySample.HISTORY_LIMIT;
            int right = x + (slot + 1) * width / ManagerActivitySample.HISTORY_LIMIT;
            GuiDraw.drawRect(left, y, Math.max(1, right - left - 1), height - 1, 0x223f4650);
            if (slot < first) continue;
            double ms = value.sample(slot - first);
            int bar = (int) Math.round(Math.min(1, ms / scale) * (height - 1));
            if (bar > 0) GuiDraw.drawRect(
                left,
                y + height - 1 - bar,
                Math.max(1, right - left - 1),
                bar,
                slot == ManagerActivitySample.HISTORY_LIMIT - 1 ? 0xffb7d9c7 : 0xff8298a4);
        }
        if (value.count() == 0) text("No samples yet", x + 4, y + 4, width - 8, 0xff969aa5);
    }

    @SideOnly(Side.CLIENT)
    private static void clock(ManagerActivitySample value, int x, int y, int width, int height) {
        for (int slot = 0; slot < ManagerActivitySample.HISTORY_LIMIT; slot++) {
            int left = x + slot * width / ManagerActivitySample.HISTORY_LIMIT;
            int right = x + (slot + 1) * width / ManagerActivitySample.HISTORY_LIMIT;
            boolean lit = value.phase == ManagerActivitySample.Phase.RUNNING && slot == value.clockMarker();
            GuiDraw.drawRect(left, y, Math.max(1, right - left - 1), height, lit ? value.phase.color : 0xff41464f);
        }
    }

    @SideOnly(Side.CLIENT)
    private static void text(String value, int x, int y, int width, int color) {
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        if (font != null && width > 0) font.drawString(font.trimStringToWidth(value, width), x, y, color);
    }
}
