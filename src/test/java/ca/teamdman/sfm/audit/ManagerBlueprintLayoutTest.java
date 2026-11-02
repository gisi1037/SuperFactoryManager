package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.mui2.Mui2BlueprintEditor.HeaderLayout;

class ManagerBlueprintLayoutTest {

    @Test
    void visibleRowsFitSmallWindowsAndHiddenPaletteDoesNotReserveSpace() {
        for (int width : new int[] { 300, 313, 400, 640, 1024 }) {
            HeaderLayout preview = new HeaderLayout(width, 0), graph = new HeaderLayout(width, 8);
            for (int i = 0; i < 10; i++) {
                assertTrue(preview.x(i) >= 6);
                assertTrue(preview.x(i) + 54 <= width - 6);
                assertTrue(preview.y(i) + 14 < preview.canvasTop);
            }
            for (int i = 0; i < 8; i++) {
                assertTrue(graph.x(i) + 54 <= width - 6);
                assertTrue(graph.paletteY(i) + 14 < graph.canvasTop);
            }
            assertTrue(preview.canvasTop < graph.canvasTop);
            assertTrue(235 - preview.canvasTop - 28 >= 160, "Preview keeps at least sixteen source lines");
            assertTrue(235 - graph.canvasTop - 57 >= 90, "Small graph windows retain usable canvas space");
        }
        HeaderLayout wide = new HeaderLayout(1024, 0);
        assertEquals(10, wide.columns);
        assertEquals(26, wide.canvasTop, "Wide previews start directly below the single toolbar row");
    }
}
