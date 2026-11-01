package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.EditorDiagnostic;
import ca.teamdman.sfm.client.gui.StudioProblems;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class StudioProblemsTest {

    @Test
    void actualSyntaxErrorsKeepCompilerWordingAndExposeReadableLocationsAndSource() {
        String source = "EVERY 20 TICKS DO\n  LET $x = )\nEND";
        ProgramBuildResult result = new ProgramBuilder(source).build();
        assertFalse(result.isSuccess());
        StudioProblems.Snapshot display = StudioProblems.snapshot(source, result.getErrors(), Collections.emptyList());
        assertFalse(display.issues.isEmpty());
        for (int i = 0; i < display.issues.size(); i++) {
            StudioProblems.Issue issue = display.issues.get(i);
            String raw = result.getErrors()
                .get(i);
            EditorDiagnostic location = EditorDiagnostic.first(raw);
            assertNotNull(location);
            assertEquals("Line " + location.line + ", column " + (location.column + 1), issue.heading);
            assertEquals(
                raw.substring(raw.indexOf(':') + 1)
                    .trim(),
                issue.message);
            assertTrue(issue.context.startsWith(location.line + " | "));
            assertEquals(location.offset(source), issue.location.offset(source));
        }
    }

    @Test
    void actualSemanticErrorsRemoveOnlyLocationAndCompilerWrapper() {
        String source = "EVERY 20 TICKS DO\n  LET $x = \"text\" - 1\nEND";
        ProgramBuildResult result = new ProgramBuilder(source).build();
        assertFalse(result.isSuccess());
        StudioProblems.Issue issue = StudioProblems.snapshot(source, result.getErrors(), Collections.emptyList()).issues
            .get(0);
        assertNotNull(issue.location);
        assertEquals(2, issue.location.line);
        assertTrue(issue.context.contains("LET $x"));
        assertTrue(
            result.getErrors()
                .get(0)
                .endsWith(issue.message));
        assertFalse(issue.message.startsWith("Compilation failed:"));
        assertFalse(issue.message.startsWith("Line 2, Col"));
    }

    @Test
    void programIssuesRemainReadableWithoutInventedSourceLocation() {
        ProgramBuildResult result = new ProgramBuilder("").build();
        StudioProblems.Issue issue = StudioProblems
            .snapshot("", result.getErrors(), Collections.singletonList("Draft")).issues.get(0);
        assertNull(issue.location);
        assertEquals("Program issue", issue.heading);
        assertEquals("Program is empty", issue.message);
        assertEquals("", issue.context);
    }

    @Test
    void diagnosticsAndContextStayBoundedAtLargeUnicodeLocations() {
        String source = repeat("\ud83d\ude00", 2000);
        String raw = "Line 1, Col 1250: " + repeat("\t\ud83d\ude00", 2000);
        StudioProblems.Snapshot display = StudioProblems
            .snapshot(source, Collections.nCopies(1000, raw), Collections.nCopies(1000, repeat("status ", 1000)));
        assertEquals(StudioProblems.MAX_ISSUES, display.issues.size());
        assertEquals(1000 - StudioProblems.MAX_ISSUES, display.omitted);
        assertEquals(StudioProblems.MAX_SUMMARY + 2, display.summary.size());
        StudioProblems.Issue issue = display.issues.get(0);
        assertTrue(issue.message.codePointCount(0, issue.message.length()) <= StudioProblems.MAX_MESSAGE);
        assertTrue(issue.message.endsWith("..."));
        assertTrue(issue.context.codePointCount(0, issue.context.length()) <= StudioProblems.MAX_CONTEXT + 16);
        assertTrue(issue.context.contains("..."));
        assertEquals(2500, issue.location.offset(source));
        for (String text : Arrays.asList(issue.message, issue.context)) assertValidSurrogates(text);
    }

    @Test
    void eachIssueKeepsItsOwnJumpTargetAndStaleLocationsClamp() {
        String source = "first\n\ud83d\ude00x\nlast";
        StudioProblems.Snapshot display = StudioProblems.snapshot(
            source,
            Arrays.asList("Line 1, Col 0: first issue", "Line 2, Col 1: second issue", "Line 999, Col 999: stale"),
            Collections.emptyList());
        assertEquals(0, display.issues.get(0).location.offset(source));
        assertEquals(8, display.issues.get(1).location.offset(source));
        assertEquals(source.length(), display.issues.get(2).location.offset(source));
        assertTrue(display.issues.get(1).context.contains("\ud83d\ude00x"));
    }

    @Test
    void outputCannotInjectMinecraftFormattingOrMultilineRows() {
        StudioProblems.Issue issue = StudioProblems.snapshot(
            "\u00a7csource",
            Collections.singletonList("Line 1, Col 0: \u00a7cmessage\nnext\rline"),
            Collections.emptyList()).issues.get(0);
        assertFalse(issue.message.contains("\u00a7"));
        assertFalse(issue.context.contains("\u00a7"));
        assertFalse(issue.message.contains("\n"));
        assertFalse(issue.message.contains("\r"));
    }

    private static String repeat(String text, int count) {
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < count; i++) value.append(text);
        return value.toString();
    }

    private static void assertValidSurrogates(String text) {
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            if (Character.isHighSurrogate(character)) {
                assertTrue(i + 1 < text.length());
                assertTrue(Character.isLowSurrogate(text.charAt(++i)));
            } else assertFalse(Character.isLowSurrogate(character));
        }
    }
}
