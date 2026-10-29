package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.EditorDiagnostic;
import ca.teamdman.sfm.client.gui.mui2.SfmlEditorBuffer;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class EditorDiagnosticTest {

    @Test
    void actualCompileErrorNavigatesWithoutChangingDraftOrUndo() {
        SfmlEditorBuffer draft = new SfmlEditorBuffer();
        draft.load("EVERY 20 TICKS DO\n  LET $x = 1\nEND");
        draft.move(
            draft.source()
                .indexOf("1"),
            false);
        draft.insert("\"text\" + ");
        String source = draft.source();
        EditorDiagnostic location = EditorDiagnostic.first(
            new ProgramBuilder(source).build()
                .getErrorMessage());
        assertNotNull(location);
        assertEquals(2, location.line);
        draft.move(location.offset(source), false);
        assertTrue(draft.caret() >= source.indexOf("LET"));
        assertEquals(source, draft.source());
        assertFalse(draft.selected());
        assertTrue(draft.undo());
        assertEquals("EVERY 20 TICKS DO\n  LET $x = 1\nEND", draft.source());
    }

    @Test
    void unicodeAndOutOfRangeLocationsNeverSplitCharactersOrLines() {
        String source = "first\n\ud83d\ude00x\n";
        assertEquals(
            8,
            EditorDiagnostic.first("Line 2, Col 1: bad")
                .offset(source));
        assertEquals(
            9,
            EditorDiagnostic.first("Line 2, Col 999: bad")
                .offset(source));
        assertEquals(
            source.length(),
            EditorDiagnostic.first("Line 999, Col 0: bad")
                .offset(source));
        assertEquals(
            0,
            EditorDiagnostic.first("Line 1, Col 0: bad")
                .offset(source));
        assertEquals(2, EditorDiagnostic.first("Line 2, Col 0: first\nLine 3, Col 0: next").line);
    }

    @Test
    void diagnosticsWithoutAUsableLocationAreIgnored() {
        for (String text : new String[] { null, "Valid program", "Program exceeds limit", "Line 0, Col 1: invalid",
            "Line 999999999999999, Col 0: invalid", "Line 1, Col -1: invalid" })
            assertNull(EditorDiagnostic.first(text));
    }
}
