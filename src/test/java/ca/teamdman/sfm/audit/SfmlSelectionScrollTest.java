package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.cleanroommc.modularui.screen.viewport.GuiContext;

import ca.teamdman.sfm.client.gui.mui2.SfmlSelectionScroll;

class SfmlSelectionScrollTest {

    @Test
    void muiPointerMustBeSampledBeforeTheEditorsOwnScrollTransformAtEveryScale() {
        for (float scale : new float[] { .5f, 1f, 2f }) {
            GuiContext context = new GuiContext();
            context.pushMatrix();
            context.updateState((int) (200 * scale), (int) (240 * scale), 0);
            context.scale(scale, scale);
            context.translate(120, 90);
            int viewportX = context.getMouseX(), viewportY = context.getMouseY();
            assertEquals(80, viewportX);
            assertEquals(150, viewportY);
            SfmlSelectionScroll scroll = new SfmlSelectionScroll();
            scroll.reset(0);
            scroll.update(20_000_000, viewportX, viewportY, 1000, 400, false);
            assertEquals(0, scroll.dx);
            assertEquals(0, scroll.dy, "Middle-of-editor selection must not trigger autoscroll");
            int scrollY = 336;
            assertEquals(48, (viewportY + scrollY - 3) / 10);
            context.translate(-960, -scrollY);
            assertEquals(viewportX + 960, context.getMouseX());
            assertEquals(
                viewportY + scrollY,
                context.getMouseY(),
                "MUI's content draw phase already includes scrolling; do not add it again for input");
        }
    }

    @Test
    void stationaryPointerOutsideViewportKeepsScrollingAtComparableFrameRates() {
        assertTrue(Math.abs(distance(10, 50_000_000) - distance(50, 10_000_000)) <= 1);
        assertTrue(distance(10, 50_000_000) > 100);
    }

    @Test
    void stopsInInteriorDisablesHorizontalInWrapAndBoundsLongFramePauses() {
        SfmlSelectionScroll scroll = new SfmlSelectionScroll();
        scroll.reset(0);
        scroll.update(20_000_000, -40, -40, 200, 200, false);
        assertTrue(scroll.dx < 0);
        assertTrue(scroll.dy < 0);
        scroll.update(40_000_000, 100, 100, 200, 200, false);
        assertEquals(0, scroll.dx);
        assertEquals(0, scroll.dy);
        scroll.update(100_000_000_000L, 400, 400, 200, 200, true);
        assertEquals(0, scroll.dx);
        assertEquals(30, scroll.dy);
        scroll.reset(100_000_000_000L);
        assertEquals(0, scroll.dy);
    }

    private static int distance(int frames, long interval) {
        SfmlSelectionScroll scroll = new SfmlSelectionScroll();
        scroll.reset(0);
        int pixels = 0;
        for (int i = 1; i <= frames; i++) {
            scroll.update(i * interval, 100, 220, 200, 200, false);
            pixels += scroll.dy;
        }
        return pixels;
    }
}
