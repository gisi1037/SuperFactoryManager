package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.TerminalCommandCompletion;

class TerminalCommandCompletionTest {

    @Test
    void completionsCycleInBothDirectionsAndResetAfterEditsOrSubmission() {
        TerminalCommandCompletion completion = new TerminalCommandCompletion();
        assertEquals("capture on", completion.complete("cap", false));
        assertEquals("capture off", completion.complete("capture on", false));
        assertEquals("capture on", completion.complete("capture off", false));
        assertEquals("capture off", completion.complete("capture on", true));
        assertEquals("labels", completion.complete("LAB", false));
        completion.reset();
        assertEquals("capture off", completion.complete("capture ", true));
    }

    @Test
    void profileAndWatchVocabularyCompletesWithoutResolvingNamesOrExecutingCommands() {
        TerminalCommandCompletion completion = new TerminalCommandCompletion();
        assertEquals("watch", completion.complete("wat", false));
        assertEquals("profile start", completion.complete("profile st", false));
        assertEquals("profile stop", completion.complete("profile start", false));
        assertEquals("profile status", completion.complete("profile stop", false));
        assertEquals("watch $private", completion.complete("watch $private", false));
        assertEquals("profile start 1200", completion.complete("profile start 1200", false));
    }

    @Test
    void argumentsUnknownPrefixesAndOversizedTextAreNeverReplaced() {
        TerminalCommandCompletion completion = new TerminalCommandCompletion();
        for (String text : new String[] { "labels Input Chest", "state $counter", "logs 12", "unknown",
            new String(new char[129]) }) assertEquals(text, completion.complete(text, false));
        assertNull(completion.complete(null, false));
        assertEquals("help", completion.complete("", false));
        assertEquals("status", completion.complete("help", false));
    }
}
