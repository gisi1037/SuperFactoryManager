package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.mui2.StudioLayout;
import ca.teamdman.sfm.client.gui.syntax.SnippetCategory;

class StudioLayoutTest {

    @Test
    void inspectorReflowsBetweenLargeAndMinimumScreensWithMargins() {
        assertEquals(420, StudioLayout.inspectorWidth(960));
        assertEquals(440, StudioLayout.inspectorHeight(540));
        assertEquals(304, StudioLayout.inspectorWidth(320));
        assertEquals(224, StudioLayout.inspectorHeight(240));
        for (int width : new int[] { 320, 427, 640, 960 }) for (int height : new int[] { 240, 360, 540 }) {
            assertTrue(StudioLayout.inspectorWidth(width) <= width - 16);
            assertTrue(StudioLayout.inspectorHeight(height) <= height - 16);
        }
    }

    @Test
    void splitResizeClampsBothEditorsAndSurvivesNarrowAndInvalidRatios() {
        assertEquals(460, StudioLayout.splitMiddle(126, 166, 960, .5));
        assertEquals(326, StudioLayout.splitMiddle(126, 166, 960, -10));
        assertEquals(594, StudioLayout.splitMiddle(126, 166, 960, 10));
        assertEquals(460, StudioLayout.splitMiddle(126, 166, 960, Double.NaN));
        assertEquals(160, StudioLayout.splitMiddle(8, 8, 320, 0));
        assertEquals(160, StudioLayout.splitMiddle(8, 8, 320, 1));
    }

    @Test
    void compactStationKeepsMoreThanTenCodeRowsAtTheMinimumScreen() {
        for (int width : new int[] { 320, 427, 640, 960 }) for (int height : new int[] { 240, 360, 540 }) {
            StudioLayout layout = StudioLayout.station(width, height, 11, SnippetCategory.values().length);
            assertTrue(height - 47 - layout.editorTop >= 110, "Useful source space at " + width + "x" + height);
            int count = StudioLayout.compact(width, height) ? 5 : 11;
            for (int i = 0; i < count; i++) {
                assertTrue(layout.x(i) >= 8);
                assertTrue(layout.x(i) + layout.buttonWidth <= width - 8);
            }
        }
    }

    @Test
    void toolbarDoesNotOverlapEditorOrLeaveScreenAtSupportedScales() {
        int categories = SnippetCategory.values().length;
        for (int width : new int[] { 320, 426, 640, 854, 1280, 1920 }) {
            StudioLayout layout = new StudioLayout(width, 10, categories);
            for (int i = 0; i < 10; i++) {
                assertTrue(layout.x(i) >= 8);
                assertTrue(layout.x(i) + layout.buttonWidth <= width - 8);
                assertTrue(layout.actionY(i) + 18 < layout.categoryTop);
            }
            for (int i = 0; i < categories; i++) {
                assertTrue(layout.x(i) + layout.buttonWidth <= width - 8);
                assertTrue(layout.categoryY(i) + 16 < layout.editorTop);
            }
            assertTrue(layout.buttonWidth >= 52, "Controls must fit their longest labels at " + width);
            assertTrue(240 - 47 - layout.editorTop >= 60, "Keep a usable editor at minimum height");
        }
    }
}
