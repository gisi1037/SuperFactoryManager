package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.mui2.SfmlEditorBuffer;
import ca.teamdman.sfm.client.gui.syntax.SfmlAutoClose;

class SfmlAutoCloseTest {

    @Test
    void createsBodiesAndClosersForBothStylesAndFunctionHeaders() {
        check("every 20 ticks do|", "every 20 ticks do\n  |\nend");
        check("EVERY 20 TICKS DO|", "EVERY 20 TICKS DO\n  |\nEND");
        check("  if (true) {|", "  if (true) {\n    |\n  }");
        check("integer choose(integer value) do|", "integer choose(integer value) do\n  |\nend");
        check("void foo() {|", "void foo() {\n  |\n}");
        check("while(true) do|", "while(true) do\n  |\nend");
        check("every 20 ticks do -- timer|", "every 20 ticks do -- timer\n  |\nend");
    }

    @Test
    void reusesExistingClosersAndSharedElseIfEnd() {
        check("if(true) {|}", "if(true) {\n  |\n}");
        check("if(true) then| end", "if(true) then\n  |\nend");
        check("if(true) then|\nend", "if(true) then\n  |\nend");
        check(
            "every 20 ticks do\n  if(true) then|\n  end\nend",
            "every 20 ticks do\n  if(true) then\n    |\n  end\nend");
        check("every 20 ticks do\n  if(true) then|\nend", "every 20 ticks do\n  if(true) then\n    |\n  end\nend");
        check("every 20 ticks {\n  if(true){|\n}", "every 20 ticks {\n  if(true){\n    |\n  }\n}");
        check("if(true) then\nelse if(false) then|\nend", "if(true) then\nelse if(false) then\n  |\nend");
        check("if(true) then\nelse|\nend", "if(true) then\nelse\n  |\nend");
        check("if(true) {|print(1)}", "if(true) {\n  |print(1)}");
    }

    @Test
    void commentsStringsAndMemberNamesAreNotBlockHeaders() {
        check("-- if true then|", "-- if true then\n|");
        check("print(\"{ then do\")|", "print(\"{ then do\")\n|");
        check("print(\"not closed {|", "print(\"not closed {\n|");
        check("let value = record.then|", "let value = record.then\n|");
        check("print(\"😀\")\nif(true){|}", "print(\"😀\")\nif(true){\n  |\n}");
    }

    @Test
    void completionAndCaretAreOneUndoStepAndSelectionsAreOrdinaryNewlines() {
        SfmlEditorBuffer buffer = marked("if(true){|}");
        int before = buffer.caret();
        SfmlAutoClose.enter(buffer, true);
        int after = buffer.caret();
        assertTrue(buffer.undo());
        assertEquals("if(true){}", buffer.source());
        assertEquals(before, buffer.caret());
        assertFalse(buffer.undo());
        assertTrue(buffer.redo());
        assertEquals(after, buffer.caret());
        buffer.load("  if(true){replace}");
        buffer.select(11, 19);
        SfmlAutoClose.enter(buffer, true);
        assertEquals("  if(true){\n  ", buffer.source());
        assertTrue(buffer.undo());
        assertEquals(11, buffer.anchor());
        assertEquals(19, buffer.caret());
    }

    @Test
    void disabledCompletionStillIndentsAndLengthRefusalKeepsSourceAndHistory() {
        SfmlEditorBuffer buffer = marked("if(true){|");
        SfmlAutoClose.enter(buffer, false);
        assertEquals("if(true){\n  ", buffer.source());
        String full = "if(true){"
            + String.join("", java.util.Collections.nCopies(SfmlEditorBuffer.MAX_CHARACTERS - 9, " "));
        buffer.load(full);
        buffer.move(full.length(), false);
        assertThrows(IllegalArgumentException.class, () -> SfmlAutoClose.enter(buffer, true));
        assertEquals(full, buffer.source());
        assertEquals(full.length(), buffer.caret());
        assertFalse(buffer.undo());
    }

    private static void check(String before, String after) {
        SfmlEditorBuffer buffer = marked(before);
        SfmlAutoClose.enter(buffer, true);
        assertEquals(after.replace("|", ""), buffer.source(), before);
        assertEquals(after.indexOf('|'), buffer.caret(), before);
    }

    private static SfmlEditorBuffer marked(String text) {
        SfmlEditorBuffer buffer = new SfmlEditorBuffer();
        buffer.load(text.replace("|", ""));
        buffer.move(text.indexOf('|'), false);
        return buffer;
    }
}
