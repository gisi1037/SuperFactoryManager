package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.TerminalCommandHistory;

class TerminalCommandHistoryTest {

    @Test
    void upDownRecallPreservesAnUnsubmittedDraftAndStopsAtBothEnds() {
        TerminalCommandHistory history = new TerminalCommandHistory();
        assertEquals("draft", history.previous("draft"));
        assertEquals("draft", history.next("draft"));
        history.record("status");
        history.record("labels Input Chest");
        assertEquals("labels Input Chest", history.previous("state $unfinished"));
        assertEquals("status", history.previous("labels Input Chest"));
        assertEquals("status", history.previous("status"));
        assertEquals("labels Input Chest", history.next("status"));
        assertEquals("state $unfinished", history.next("labels Input Chest"));
        assertEquals("edited draft", history.next("edited draft"));
        assertEquals("labels Input Chest", history.previous("edited draft"));
        assertEquals("edited draft", history.next("labels Input Chest"));
    }

    @Test
    void pollsInvalidInputAndConsecutiveDuplicatesDoNotCrowdOutUsefulCommands() {
        TerminalCommandHistory history = new TerminalCommandHistory();
        history.record("status");
        history.record("");
        history.record("  ");
        history.record(null);
        history.record(new String(new char[129]).replace('\0', 'x'));
        history.record(" status ");
        history.record("logs 2");
        assertEquals("logs 2", history.previous("draft"));
        assertEquals("status", history.previous("logs 2"));
        assertEquals("status", history.previous("status"));
        history.record("labels");
        assertEquals("labels", history.previous("new draft"));
        assertEquals("new draft", history.next("labels"));
    }

    @Test
    void oldestCommandsAreEvictedAndHistoryIsLocalToOnePanel() {
        TerminalCommandHistory history = new TerminalCommandHistory();
        for (int i = 0; i < 40; i++) history.record("state $value" + i);
        String current = "draft";
        for (int i = 39; i >= 8; i--) {
            current = history.previous(current);
            assertEquals("state $value" + i, current);
        }
        assertEquals("state $value8", history.previous(current));
        TerminalCommandHistory reopened = new TerminalCommandHistory();
        assertEquals("private draft", reopened.previous("private draft"));
    }
}
