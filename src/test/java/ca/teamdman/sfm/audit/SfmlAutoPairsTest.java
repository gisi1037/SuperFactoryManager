package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.mui2.SfmlEditorBuffer;
import ca.teamdman.sfm.client.gui.syntax.SfmlAutoClose;
import ca.teamdman.sfm.client.gui.syntax.SfmlAutoPairs;

class SfmlAutoPairsTest {

    @Test
    void typingAQuotedFunctionArgumentCompletesBothPairsAndSkipsClosers() {
        SfmlEditorBuffer b = marked("label|");
        type(b, "(");
        assertMarked("label(|)", b);
        type(b, "\"");
        assertMarked("label(\"|\")", b);
        type(b, "signal");
        type(b, "\"");
        assertMarked("label(\"signal\"|)", b);
        type(b, ")");
        assertMarked("label(\"signal\")|", b);
    }

    @Test
    void openingAndWrappingAreSingleUndoStepsWithOriginalSelectionDirection() {
        SfmlEditorBuffer b = marked("call|");
        type(b, "(");
        assertTrue(b.undo());
        assertMarked("call|", b);
        assertTrue(b.redo());
        assertMarked("call(|)", b);
        b.load("abc");
        b.select(3, 0);
        type(b, "(");
        assertEquals("(abc)", b.source());
        assertEquals(4, b.anchor());
        assertEquals(1, b.caret());
        assertEquals("abc", b.selection());
        assertTrue(b.undo());
        assertEquals("abc", b.source());
        assertEquals(3, b.anchor());
        assertEquals(0, b.caret());
        assertFalse(b.undo());
    }

    @Test
    void wrappingQuotesRetainsUnicodeSelectionAndOneUndo() {
        SfmlEditorBuffer b = marked("label(箱😀|)");
        b.select(6, 9);
        type(b, "\"");
        assertEquals("label(\"箱😀\")", b.source());
        assertEquals("箱😀", b.selection());
        assertTrue(b.undo());
        assertEquals("label(箱😀)", b.source());
        assertEquals("箱😀", b.selection());
    }

    @Test
    void escapedQuotesAreInsertedLiterallyIncludingBeforeAnExistingQuote() {
        SfmlEditorBuffer b = marked("\"text\\|\"");
        type(b, "\"");
        assertMarked("\"text\\\"|\"", b);
        b = marked("\"text\\\\|\"");
        type(b, "\"");
        assertMarked("\"text\\\\\"|", b);
    }

    @Test
    void stringsCommentsAndMultilineStringsReceiveLiteralPunctuation() {
        for (String prefix : new String[] { "-- note ", "\"text ", "\"first\nsecond ", "print(\"-- not a comment " }) {
            SfmlEditorBuffer b = marked(prefix + "|");
            type(b, "(");
            assertMarked(prefix + "(|", b);
        }
        SfmlEditorBuffer b = marked("-- note\nlabel|");
        type(b, "(");
        assertMarked("-- note\nlabel(|)", b);
        b = marked("-- note |");
        type(b, "\"");
        assertMarked("-- note \"|", b);
    }

    @Test
    void openingBeforeExistingTextAndUnmatchedClosingCharactersRemainLiteral() {
        SfmlEditorBuffer b = marked("call|argument");
        type(b, "(");
        assertMarked("call(|argument", b);
        b = marked("|)");
        type(b, ")");
        assertMarked(")|)", b);
        b = marked("print(\"(|)\")");
        type(b, ")");
        assertMarked("print(\"()|)\")", b);
    }

    @Test
    void emptyPairsBackspaceTogetherAndUndoRestoresTheOriginalCaret() {
        for (String source : new String[] { "call(|)", "label(\"|\")" }) {
            SfmlEditorBuffer b = marked(source);
            SfmlAutoPairs.delete(b, false, true);
            assertMarked(source.startsWith("call") ? "call|" : "label(|)", b);
            assertTrue(b.undo());
            assertMarked(source, b);
        }
        SfmlEditorBuffer b = marked("\"(|)\"");
        SfmlAutoPairs.delete(b, false, true);
        assertMarked("\"|)\"", b);
        b = marked("-- (|)");
        SfmlAutoPairs.delete(b, false, true);
        assertMarked("-- |)", b);
    }

    @Test
    void disablingPairingForwardDeleteAndSelectionsKeepNormalEditing() {
        SfmlEditorBuffer b = marked("call|");
        SfmlAutoPairs.type(b, "(", false);
        assertMarked("call(|", b);
        b = marked("(|)");
        SfmlAutoPairs.delete(b, true, true);
        assertMarked("(|", b);
        b = marked("(|)");
        SfmlAutoPairs.delete(b, false, false);
        assertMarked("|)", b);
        b.load("abc");
        b.select(0, 3);
        type(b, ")");
        assertMarked(")|", b);
    }

    @Test
    void skipRetainsRedoAndBulkTextNeverGetsAdditionalCharacters() {
        SfmlEditorBuffer b = marked("(|)");
        type(b, "x");
        b.undo();
        type(b, ")");
        assertMarked("()|", b);
        assertTrue(b.redo());
        assertEquals("(x)", b.source());
        b.load("");
        type(b, "label(\"source\")\r\n");
        assertMarked("label(\"source\")\n|", b);
        b.load("");
        b.insert("("); // Paste/snippet insertion bypasses typing assistance, even for one character.
        assertMarked("(|", b);
    }

    @Test
    void rejectedPairingPreservesSourceSelectionAndRedo() {
        String prefix = String.join("", Collections.nCopies(SfmlEditorBuffer.MAX_CHARACTERS - 1, "a"));
        SfmlEditorBuffer b = marked(prefix + "|");
        b.insert("x");
        b.undo();
        assertThrows(IllegalArgumentException.class, () -> type(b, "("));
        assertMarked(prefix + "|", b);
        assertTrue(b.redo());
        assertEquals(prefix + "x", b.source());
    }

    @Test
    void pairedConditionsStillWorkWithExistingBraceAndEndCompletion() {
        SfmlEditorBuffer b = marked("if|");
        type(b, "(");
        type(b, "true");
        type(b, ")");
        type(b, " {");
        SfmlAutoClose.enter(b, true);
        assertMarked("if(true) {\n  |\n}", b);
    }

    private static void type(SfmlEditorBuffer buffer, String text) {
        SfmlAutoPairs.type(buffer, text, true);
    }

    private static SfmlEditorBuffer marked(String source) {
        SfmlEditorBuffer b = new SfmlEditorBuffer();
        int caret = source.indexOf('|');
        b.load(source.replace("|", ""));
        b.move(caret, false);
        return b;
    }

    private static void assertMarked(String expected, SfmlEditorBuffer b) {
        assertEquals(
            expected,
            b.source()
                .substring(0, b.caret()) + "|"
                + b.source()
                    .substring(b.caret()));
    }
}
