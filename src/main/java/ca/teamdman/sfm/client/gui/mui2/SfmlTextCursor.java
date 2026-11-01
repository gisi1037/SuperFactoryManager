package ca.teamdman.sfm.client.gui.mui2;

import java.nio.IntBuffer;

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

    @SubscribeEvent
    public void draw(GuiScreenEvent.DrawScreenEvent.Post event) {
        ModularScreen screen = ModularScreen.getCurrent();
        IWidget hovered = screen != null && SuperFactoryManager.MODID.equals(screen.getOwner()) ? screen.getContext()
            .getTopHovered() : null;
        set(
            hovered != null && hovered.isEnabled()
                && (hovered instanceof SfmlCodeEditorWidget || hovered instanceof BaseTextFieldWidget));
    }

    @SubscribeEvent
    public void open(GuiOpenEvent event) {
        set(false);
    }

    private void set(boolean text) {
        if (text == owned || text && unavailable || !Mouse.isCreated()) return;
        try {
            if (text) {
                if (beam == null) beam = create();
                previous = Mouse.getNativeCursor();
                Mouse.setNativeCursor(beam);
                owned = true;
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

    private static Cursor create() throws LWJGLException {
        int size = Math.max(16, Cursor.getMinCursorSize());
        if (size > 64 || size > Cursor.getMaxCursorSize()) throw new IllegalStateException("Unsupported cursor size");
        IntBuffer pixels = BufferUtils.createIntBuffer(size * size);
        int center = size / 2;
        for (int y = 0; y < size; y++) for (int x = 0; x < size; x++) {
            boolean inside = beam(x, y, center, size);
            boolean edge = beam(x - 1, y, center, size) || beam(x + 1, y, center, size)
                || beam(x, y - 1, center, size)
                || beam(x, y + 1, center, size);
            pixels.put(inside ? 0xff101010 : edge ? 0xffffffff : 0);
        }
        pixels.flip();
        return new Cursor(size, size, center, center, 1, pixels, null);
    }

    private static boolean beam(int x, int y, int center, int size) {
        return Math.abs(x - center) <= 1 && y >= 3 && y <= size - 4
            || Math.abs(x - center) <= 4 && (y == 3 || y == size - 4);
    }
}
