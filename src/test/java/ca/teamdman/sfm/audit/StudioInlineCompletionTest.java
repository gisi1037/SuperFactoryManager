package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.mui2.SfmlEditorBuffer;
import ca.teamdman.sfm.client.gui.syntax.StudioInlineCompletion;
import ca.teamdman.sfm.client.gui.syntax.StudioInlineCompletion.Suggestion;

class StudioInlineCompletionTest {

    @Test
    void typingWaitsForTheLatestDebounceAndComputesOnlyOnce() {
        StudioInlineCompletion completion = new StudioInlineCompletion();
        completion.typed("la", 2, false, 0);
        completion.update("la", 2, false, StudioInlineCompletion.DELAY_NANOS - 1);
        assertNull(completion.current("la", 2, false));
        completion.typed("lab", 3, false, 100);
        completion.update("lab", 3, false, StudioInlineCompletion.DELAY_NANOS);
        assertNull(completion.current("lab", 3, false));
        completion.update("lab", 3, false, StudioInlineCompletion.DELAY_NANOS + 100);
        Suggestion result = completion.current("lab", 3, false);
        assertNotNull(result);
        assertEquals("label(\"<name>\")", result.text);
        assertEquals("el(\"<name>\")", result.suffix);
        completion.update("lab", 3, false, Long.MAX_VALUE);
        assertSame(result, completion.current("lab", 3, false));
    }

    @Test
    void scopeMethodsAndKeywordsUseExistingNamesAndNeverReplaceTypedCharacters() {
        Suggestion variable = suggest("let $Supply = 1\nlet $copy = $Su");
        assertNotNull(variable);
        assertEquals("$Supply", variable.text);
        assertEquals("pply", variable.suffix);
        Suggestion method = suggest("label(\"ped\").em");
        assertNotNull(method);
        assertEquals("empty()", method.text);
        Suggestion keyword = suggest("ev");
        assertNotNull(keyword);
        assertEquals("every", keyword.text);
        // Preserve the existing scope index's refusal of an unparseable draft.
        assertNull(suggest("let $Supply = 1\nevery 20 ticks do print($Su"));
        assertNull(suggest("let $Supply = 1\nlet $copy = $su"));
        assertNull(suggest("EV"));
        assertNull(suggest("label(\"ped\").ev"));
        assertNull(suggest("label(\"ped\"). ev"));
        assertNull(suggest("$unknown"));
    }

    @Test
    void noGhostInsideSelectionsCommentsStringsOrExistingText() {
        for (String text : new String[] { "-- la", "print(\"la", "print(\"escaped \\\" la", "a" })
            assertNull(suggest(text), text);
        StudioInlineCompletion completion = new StudioInlineCompletion();
        completion.typed("la()", 2, false, 0);
        completion.update("la()", 2, false, StudioInlineCompletion.DELAY_NANOS);
        assertNull(completion.current("la()", 2, false));
        completion.typed("la", 2, true, 0);
        completion.update("la", 2, false, StudioInlineCompletion.DELAY_NANOS);
        assertNull(completion.current("la", 2, false));
    }

    @Test
    void sourceCaretSelectionAndDismissalInvalidateVisibleAndPendingSuggestions() {
        for (int mode = 0; mode < 4; mode++) {
            StudioInlineCompletion completion = new StudioInlineCompletion();
            completion.typed("la", 2, false, 0);
            completion.update("la", 2, false, StudioInlineCompletion.DELAY_NANOS);
            assertNotNull(completion.current("la", 2, false));
            if (mode == 0) completion.update("lab", 3, false, Long.MAX_VALUE);
            if (mode == 1) completion.update("la", 1, false, Long.MAX_VALUE);
            if (mode == 2) completion.update("la", 2, true, Long.MAX_VALUE);
            if (mode == 3) completion.dismiss();
            completion.update("la", 2, false, Long.MAX_VALUE);
            assertNull(completion.current("la", 2, false));
        }
        StudioInlineCompletion completion = new StudioInlineCompletion();
        completion.typed("la", 2, false, 0);
        completion.dismiss();
        completion.update("la", 2, false, StudioInlineCompletion.DELAY_NANOS);
        assertNull(completion.current("la", 2, false));
    }

    @Test
    void acceptingTheDisplayedSuffixIsOneUndoAndPreservesSourceAndCaret() {
        SfmlEditorBuffer buffer = new SfmlEditorBuffer();
        buffer.load("-- retained\nlet $x = la");
        buffer.move(
            buffer.source()
                .length(),
            false);
        String before = buffer.source();
        int caret = buffer.caret();
        Suggestion suggestion = suggest(before);
        assertNotNull(suggestion);
        buffer.insert(suggestion.suffix);
        assertEquals(before + suggestion.suffix, buffer.source());
        assertTrue(buffer.undo());
        assertEquals(before, buffer.source());
        assertEquals(caret, buffer.caret());
        assertFalse(buffer.selected());
        assertFalse(buffer.undo());
    }

    private static Suggestion suggest(String source) {
        StudioInlineCompletion completion = new StudioInlineCompletion();
        completion.typed(source, source.length(), false, 0);
        completion.update(source, source.length(), false, StudioInlineCompletion.DELAY_NANOS);
        return completion.current(source, source.length(), false);
    }
}
