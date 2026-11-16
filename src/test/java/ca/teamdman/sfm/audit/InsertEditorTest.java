package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.mui2.SfmlCodeEditorWidget;

class InsertEditorTest {

    @Test
    void templateReplacesOnlyChosenRangeSelectsItsOwnPlaceholderAndUndoesOnce() {
        AtomicInteger edits = new AtomicInteger();
        SfmlCodeEditorWidget editor = editor(edits);
        String source = "every 20 ticks {\n  OLD\n}";
        editor.load(source);
        int start = source.indexOf("OLD");
        editor.buffer.select(start, start + 3);
        assertTrue(editor.insertTemplate("let <name> = 1\nprint(<name>)", start, start + 3));
        assertEquals("every 20 ticks {\n  let <name> = 1\n  print(<name>)\n}", editor.source());
        assertEquals("<name>", editor.buffer.selection());
        assertEquals(1, edits.get());
        assertTrue(editor.buffer.undo());
        assertEquals(source, editor.source());
        assertEquals("OLD", editor.buffer.selection());
        assertFalse(editor.buffer.undo());
    }

    @Test
    void insertionDoesNotEatNearbyIdentifierAndOversizeRejectionKeepsSelectionAndRedo() {
        SfmlCodeEditorWidget editor = editor(new AtomicInteger());
        editor.load("foobar");
        editor.buffer.move(3, false);
        assertTrue(editor.insertTemplate("X", 3, 3));
        assertEquals("fooXbar", editor.source());
        editor.buffer.undo();
        editor.buffer.select(1, 3);
        String excessive = String.join("", Collections.nCopies(65537, "x"));
        assertFalse(editor.insertTemplate(excessive, 1, 3));
        assertEquals("foobar", editor.source());
        assertEquals("oo", editor.buffer.selection());
        assertFalse(editor.insertTemplate("bad", -1, 1));
        assertFalse(editor.insertTemplate("bad", 0, 100));
        assertEquals("oo", editor.buffer.selection());
        assertTrue(editor.buffer.redo());
    }

    private static SfmlCodeEditorWidget editor(AtomicInteger edits) {
        return new SfmlCodeEditorWidget(edits::incrementAndGet, () -> {}, () -> {}, () -> {}, () -> true, error -> {});
    }
}
