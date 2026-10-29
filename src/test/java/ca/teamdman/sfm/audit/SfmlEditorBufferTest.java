package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.mui2.SfmlEditorBuffer;

class SfmlEditorBufferTest {

    @Test
    void findWrapsBothDirectionsAndDoesNotAddUndoEdits() {
        SfmlEditorBuffer buffer = new SfmlEditorBuffer();
        buffer.load("ped ped PED");
        assertTrue(buffer.find("ped", false, false));
        assertEquals(0, buffer.start());
        assertTrue(buffer.find("ped", false, false));
        assertEquals(4, buffer.start());
        assertTrue(buffer.find("ped", true, false));
        assertEquals(0, buffer.start());
        assertTrue(buffer.find("ped", true, false));
        assertEquals(8, buffer.start());
        assertTrue(buffer.find("ped", false, true));
        assertEquals(0, buffer.start());
        assertFalse(buffer.undo());
    }

    @Test
    void replaceRequiresAnExactSelectionAndTreatsSpecialCharactersLiterally() {
        SfmlEditorBuffer buffer = new SfmlEditorBuffer();
        buffer.load("$a.* $a.*");
        assertFalse(buffer.replaceMatch("$a.*", "$x\\1", true));
        assertEquals("$a.*", buffer.selection());
        assertTrue(buffer.replaceMatch("$a.*", "$x\\1", true));
        assertEquals("$x\\1 $a.*", buffer.source());
        assertEquals("$a.*", buffer.selection());
        assertTrue(buffer.undo());
        assertEquals("$a.* $a.*", buffer.source());
    }

    @Test
    void replaceAllIsNonOverlappingOneUndoAndNormalizesLineEndings() {
        SfmlEditorBuffer buffer = new SfmlEditorBuffer();
        buffer.load("aaaa AA");
        assertEquals(3, buffer.replaceAll("aa", "b\r\n", false));
        assertEquals("b\nb\n b\n", buffer.source());
        assertTrue(buffer.undo());
        assertEquals("aaaa AA", buffer.source());
        assertFalse(buffer.undo());
        assertTrue(buffer.redo());
        assertEquals("b\nb\n b\n", buffer.source());
    }

    @Test
    void searchNeverSelectsHalfASurrogateAndCaseFoldRetainsSourceOffsets() {
        SfmlEditorBuffer buffer = new SfmlEditorBuffer();
        buffer.load("😀X İx");
        assertFalse(buffer.find("\uDE00", false, true));
        assertEquals(0, buffer.replaceAll("\uD83D", "broken", true));
        assertTrue(buffer.find("x", false, false));
        assertEquals(2, buffer.start());
        assertTrue(buffer.find("x", false, false));
        assertEquals(5, buffer.start());
        assertEquals(1, buffer.replaceAll("😀", "箱", true));
        assertEquals("箱X İx", buffer.source());
    }

    @Test
    void oversizedReplaceAllPreservesSourceSelectionAndRedoAtomically() {
        SfmlEditorBuffer buffer = new SfmlEditorBuffer();
        buffer.load("aaaa");
        buffer.move(4, false);
        buffer.insert("z");
        buffer.undo();
        buffer.select(1, 3);
        String replacement = String.join("", Collections.nCopies(20000, "b"));
        assertThrows(IllegalArgumentException.class, () -> buffer.replaceAll("a", replacement, true));
        assertEquals("aaaa", buffer.source());
        assertEquals("aa", buffer.selection());
        assertTrue(buffer.redo());
        assertEquals("aaaaz", buffer.source());
    }

    @Test
    void emptyAndAbsentQueriesLeaveSelectionAndHistoryAlone() {
        SfmlEditorBuffer buffer = new SfmlEditorBuffer();
        buffer.load("abc");
        buffer.select(1, 2);
        assertFalse(buffer.find("", false, false));
        assertFalse(buffer.find("missing", true, true));
        assertEquals(0, buffer.replaceAll("", "x", true));
        assertEquals(0, buffer.replaceAll("missing", "x", true));
        assertEquals("b", buffer.selection());
        assertFalse(buffer.undo());
    }

    @Test
    void rejectedOversizedInsertAndLoadKeepSelectionSourceAndUndoRedoHistory() {
        SfmlEditorBuffer buffer = new SfmlEditorBuffer();
        buffer.load("abc");
        buffer.move(3, false);
        buffer.insert("d");
        buffer.select(1, 3);
        String excessive = String.join("", Collections.nCopies(SfmlEditorBuffer.MAX_CHARACTERS + 1, "x"));
        assertThrows(IllegalArgumentException.class, () -> buffer.insert(excessive));
        assertThrows(IllegalArgumentException.class, () -> buffer.load(excessive));
        assertEquals("abcd", buffer.source());
        assertEquals(1, buffer.anchor());
        assertEquals(3, buffer.caret());
        assertEquals("bc", buffer.selection());
        assertTrue(buffer.undo());
        assertEquals("abc", buffer.source());
        assertThrows(IllegalArgumentException.class, () -> buffer.insert(excessive));
        assertTrue(buffer.redo(), "Rejected input must not erase redo history");
        assertEquals("abcd", buffer.source());
        assertEquals("bc", buffer.selection());
    }

    @Test
    void exactCharacterBoundaryAcceptsUnicodeAndNormalizedCrLfButRejectsOverflowAtomically() {
        SfmlEditorBuffer buffer = new SfmlEditorBuffer();
        String prefix = String.join("", Collections.nCopies(SfmlEditorBuffer.MAX_CHARACTERS - 2, "a"));
        buffer.load(prefix);
        buffer.move(prefix.length(), false);
        buffer.insert("😀");
        assertEquals(
            SfmlEditorBuffer.MAX_CHARACTERS,
            buffer.source()
                .length());
        assertThrows(IllegalArgumentException.class, () -> buffer.insert("x"));
        assertEquals(prefix + "😀", buffer.source());
        assertEquals(SfmlEditorBuffer.MAX_CHARACTERS, buffer.caret());
        assertTrue(buffer.undo());
        assertEquals(prefix, buffer.source());
        buffer.load(String.join("", Collections.nCopies(SfmlEditorBuffer.MAX_CHARACTERS, "\r\n")));
        assertEquals(
            SfmlEditorBuffer.MAX_CHARACTERS,
            buffer.source()
                .length());
        assertTrue(
            buffer.source()
                .chars()
                .allMatch(character -> character == '\n'));
    }

    @Test
    void multilinePasteInMiddlePreservesBothSidesAndIsOneUndoableEdit() {
        SfmlEditorBuffer buffer = new SfmlEditorBuffer();
        buffer.load("INPUT FROM source");
        buffer.move(6, false);
        buffer.insert("1\nOUTPUT 2\nFORGET\n");
        assertEquals("INPUT 1\nOUTPUT 2\nFORGET\nFROM source", buffer.source());
        assertTrue(buffer.undo());
        assertEquals("INPUT FROM source", buffer.source());
        assertEquals(6, buffer.caret());
        assertTrue(buffer.redo());
        assertEquals("INPUT 1\nOUTPUT 2\nFORGET\nFROM source", buffer.source());
    }

    @Test
    void deletingAdjacentAndDistantMultilineSelectionsJoinsOnlyTheRetainedText() {
        for (String source : new String[] { "abc\ndef", "abc\nremoved\nremoved\ndef" }) {
            SfmlEditorBuffer buffer = new SfmlEditorBuffer();
            buffer.load(source);
            buffer.select(2, source.length() - 2);
            buffer.delete(false);
            assertEquals("abef", buffer.source());
            assertTrue(buffer.undo());
            assertEquals(source, buffer.source());
        }
    }

    @Test
    void pasteNormalizesLineEndingsAndRetainsTrailingEmptyLinesAndUnicode() {
        SfmlEditorBuffer buffer = new SfmlEditorBuffer();
        buffer.insert("NAME \"箱😀\"\r\nEVERY 20 TICKS DO\rEND\r\n\r\n");
        assertEquals("NAME \"箱😀\"\nEVERY 20 TICKS DO\nEND\n\n", buffer.source());
        buffer.move(
            buffer.source()
                .indexOf("😀") + 2,
            false);
        buffer.delete(false);
        assertFalse(
            buffer.source()
                .contains("😀"));
        assertTrue(
            buffer.source()
                .contains("箱"));
        buffer.undo();
        assertTrue(
            buffer.source()
                .contains("箱😀"));
    }

    @Test
    void caretAndSelectionsNeverSplitSurrogatePairs() {
        SfmlEditorBuffer buffer = new SfmlEditorBuffer();
        buffer.load("a😀b");
        buffer.move(2, false);
        assertEquals(1, buffer.caret());
        assertEquals(3, buffer.next(buffer.caret()));
        buffer.select(1, 3);
        assertEquals("😀", buffer.selection());
        buffer.insert("箱");
        assertEquals("a箱b", buffer.source());
    }

    @Test
    void invalidDraftRemainsAvailableUntilExplicitLoadAndNewEditsClearRedo() {
        SfmlEditorBuffer buffer = new SfmlEditorBuffer();
        String draft = "EVERY unfinished\nLET $name = [\"箱😀\",\n";
        buffer.load(draft);
        buffer.move(0, false);
        buffer.move(draft.length(), true);
        assertEquals(draft, buffer.selection());
        assertEquals(draft, buffer.source());
        buffer.move(draft.length(), false);
        buffer.insert("2");
        buffer.undo();
        assertEquals(draft, buffer.source());
        buffer.insert("3");
        assertFalse(buffer.redo());
        assertEquals(draft + "3", buffer.source());
    }
}
