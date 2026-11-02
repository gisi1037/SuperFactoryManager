package ca.teamdman.sfm.client.gui.mui2;

import java.nio.IntBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;

import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.widgets.textfield.BaseTextFieldWidget;

import ca.teamdman.sfm.SuperFactoryManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** One lazily created native I-beam; restore the prior cursor without overriding another mod's later choice. */
@SideOnly(Side.CLIENT)
public final class SfmlTextCursor {

    private Cursor beam, previous;
    private boolean owned, unavailable;
    private int linePixels;

    @SubscribeEvent
    public void draw(GuiScreenEvent.DrawScreenEvent.Post event) {
        ModularScreen screen = ModularScreen.getCurrent();
        IWidget hovered = screen != null && SuperFactoryManager.MODID.equals(screen.getOwner()) ? screen.getContext()
            .getTopHovered() : null;
        boolean text = hovered != null && hovered.isEnabled()
            && (hovered instanceof SfmlCodeEditorWidget || hovered instanceof BaseTextFieldWidget);
        Minecraft mc = Minecraft.getMinecraft();
        int height = text && hovered.getPanel() instanceof StudioScaledPanel ? StudioScaledPanel.textLinePixels()
            : 10 * new ScaledResolution(mc, mc.displayWidth, mc.displayHeight).getScaleFactor();
        set(text, Math.max(14, Math.min(60, height + 2)));
    }

    @SubscribeEvent
    public void open(GuiOpenEvent event) {
        set(false, 0);
    }

    private void set(boolean text, int height) {
        if (text == owned && (!text || linePixels == height) || text && unavailable || !Mouse.isCreated()) return;
        try {
            if (text) {
                Cursor old = beam;
                Cursor next = beam == null || linePixels != height ? create(height) : beam;
                if (!owned) previous = Mouse.getNativeCursor();
                beam = next;
                Mouse.setNativeCursor(beam);
                linePixels = height;
                owned = true;
                if (old != null && old != beam) old.destroy();
            } else {
                if (Mouse.getNativeCursor() == beam) Mouse.setNativeCursor(previous);
                owned = false;
                previous = null;
            }
        } catch (LWJGLException | RuntimeException failure) {
            // Some native backends do not support custom cursors. Never disrupt text input for presentation.
            unavailable = true;
            try {
                if (beam != null && Mouse.getNativeCursor() == beam) Mouse.setNativeCursor(null);
            } catch (LWJGLException | RuntimeException ignored) {
                // Backend failure cannot be repaired by repeatedly trying on every frame.
            }
            owned = false;
            previous = null;
            SuperFactoryManager.LOG.debug("SFM text cursor unavailable on this native backend", failure);
        }
    }

    private static Cursor create(int height) throws LWJGLException {
        int maximum = Math.min(64, Cursor.getMaxCursorSize());
        int size = Math.max(Math.min(maximum, height + 4), Cursor.getMinCursorSize());
        if (size < 12 || size > maximum) throw new IllegalStateException("Unsupported cursor size");
        int halfHeight = Math.min(height / 2, (size - 3) / 2);
        IntBuffer pixels = BufferUtils.createIntBuffer(size * size);
        int center = size / 2;
        for (int y = 0; y < size; y++) for (int x = 0; x < size; x++) {
            boolean inside = beam(x, y, center, halfHeight);
            boolean edge = beam(x - 1, y, center, halfHeight) || beam(x + 1, y, center, halfHeight)
                || beam(x, y - 1, center, halfHeight)
                || beam(x, y + 1, center, halfHeight);
            pixels.put(inside ? 0xff101010 : edge ? 0xffffffff : 0);
        }
        pixels.flip();
        return new Cursor(size, size, center, center, 1, pixels, null);
    }

    private static boolean beam(int x, int y, int center, int halfHeight) {
        int distance = Math.abs(y - center);
        return Math.abs(x - center) <= 1 && distance <= halfHeight
            || Math.abs(x - center) <= 4 && distance == halfHeight;
    }
}
