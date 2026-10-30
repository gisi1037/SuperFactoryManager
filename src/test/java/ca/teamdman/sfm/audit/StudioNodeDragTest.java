package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.StudioDocument;
import ca.teamdman.sfm.client.blueprint.StudioNodeDrag;
import ca.teamdman.sfm.client.gui.mui2.StudioLayout;

class StudioNodeDragTest {

    private static final String CODE = "every 20 ticks do\n input from source\n output to destination\nend\n";

    private static StudioDocument document() {
        StudioDocument document = new StudioDocument();
        document.load(CODE);
        document.openVisual();
        return document;
    }

    @Test
    void manyFrameSamplesRemainOneUndoableEditAndSerializationWaitsUntilRelease() {
        StudioDocument document = document();
        Node node = document.graph()
            .nodes()
            .iterator()
            .next();
        double initialX = node.x(), initialY = node.y();
        StudioNodeDrag drag = new StudioNodeDrag(document);
        drag.begin(node.id, 10, 20, 2);
        for (int frame = 1; frame <= 120; frame++) drag.update(10 + frame, 20 - frame, false);
        assertEquals(initialX + 60, node.x());
        assertEquals(initialY - 60, node.y());
        assertEquals(CODE, document.source(), "Frames must not regenerate source/metadata");
        assertTrue(drag.finish());
        assertEquals(CODE, document.code());
        assertTrue(document.undo());
        assertEquals(
            initialX,
            document.graph()
                .node(node.id)
                .x());
        assertEquals(
            initialY,
            document.graph()
                .node(node.id)
                .y());
        assertFalse(document.undo(), "One gesture must create exactly one history entry");
        assertTrue(document.redo());
        assertEquals(
            initialX + 60,
            document.graph()
                .node(node.id)
                .x());
    }

    @Test
    void shiftSnapUsesWorldGridWithoutLosingSmallMovementsOrAccumulatingRounding() {
        StudioDocument document = document();
        Node node = document.graph()
            .nodes()
            .iterator()
            .next();
        node.setPosition(7, -11);
        StudioNodeDrag drag = new StudioNodeDrag(document);
        drag.begin(node.id, 0, 0, .5);
        for (int frame = 1; frame <= 17; frame++) drag.update(frame, frame, true);
        assertEquals(48, node.x());
        assertEquals(24, node.y());
        drag.update(17, 17, false);
        assertEquals(41, node.x(), "Releasing Shift restores the absolute unsnapped pointer target");
        assertEquals(23, node.y());
        drag.update(-21, -21, true);
        assertEquals(-24, node.x());
        assertEquals(-48, node.y());
        assertTrue(drag.finish());
    }

    @Test
    void clickWithoutMovementDoesNotDirtyHistoryAndInspectorCannotConsumeTheCanvas() {
        StudioDocument document = document();
        Node node = document.graph()
            .nodes()
            .iterator()
            .next();
        StudioNodeDrag drag = new StudioNodeDrag(document);
        drag.begin(node.id, 5, 8, 1);
        drag.update(5, 8, false);
        assertFalse(drag.finish());
        assertFalse(document.undo());
        for (int width : new int[] { 900, 960, 1280, 1920 })
            for (int left : new int[] { 86, 164, 270 }) for (boolean split : new boolean[] { false, true })
                for (int preferred : new int[] { -999, 150, 300, 9999 }) {
                    int inspector = StudioLayout.inspectorPaneWidth(width, left, split, preferred);
                    assertTrue(inspector >= 150 && inspector <= 420);
                    assertTrue(width - left - inspector - 16 >= (split ? 400 : 240));
                }
    }
}
