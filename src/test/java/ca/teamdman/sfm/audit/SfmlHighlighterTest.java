package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter;
import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter.Highlighted;
import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter.Line;
import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter.Span;
import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter.Style;

class SfmlHighlighterTest {

    @Test
    void colorsCurrentGrammarCategoriesAndCaseInsensitiveKeywords() {
        Highlighted highlighted = SfmlHighlighter.highlight(
            "Every 20g + 1 ticks do\n" + "  input 64 minecraft:iron_ingot from source\n"
                + "  output retain 3 to target\n"
                + "  forget source\n"
                + "  if source has >= 1 and not false then\n"
                + "    output to target slots 0-2\n"
                + "  end\n"
                + "end");
        assertStyle(highlighted.lines.get(0), "Every", Style.FLOW);
        assertStyle(highlighted.lines.get(0), "20g", Style.NUMBER);
        assertStyle(highlighted.lines.get(0), "+", Style.OPERATOR);
        assertStyle(highlighted.lines.get(0), "ticks", Style.KEYWORD);
        assertStyle(highlighted.lines.get(1), "input", Style.ACTION);
        assertStyle(highlighted.lines.get(1), "64", Style.NUMBER);
        assertStyle(highlighted.lines.get(1), "minecraft:iron_ingot", Style.RESOURCE);
        assertStyle(highlighted.lines.get(1), "from", Style.KEYWORD);
        assertStyle(highlighted.lines.get(1), "source", Style.LABEL);
        assertStyle(highlighted.lines.get(2), "output", Style.ACTION);
        assertStyle(highlighted.lines.get(2), "retain", Style.KEYWORD);
        assertStyle(highlighted.lines.get(3), "forget", Style.ACTION);
        assertStyle(highlighted.lines.get(4), ">=", Style.OPERATOR);
        assertStyle(highlighted.lines.get(4), "and", Style.OPERATOR);
        assertStyle(highlighted.lines.get(4), "not", Style.OPERATOR);
        assertStyle(highlighted.lines.get(4), "false", Style.FLOW);
        assertStyle(highlighted.lines.get(5), "-", Style.OPERATOR);
        assertStyle(highlighted.lines.get(1), "  ", Style.TEXT);
        assertCompleteSpans(highlighted);
    }

    @Test
    void distinguishesTagSyntaxFromActualLineCommentsAndColorsResourceParts() {
        Line line = SfmlHighlighter
            .highlight("INPUT item:minecraft:redstone WITH #forge:ingots/iron FROM storage -- OUTPUT #comment").lines
                .get(0);
        assertStyle(line, "item:minecraft:redstone", Style.RESOURCE);
        assertStyle(line, "#", Style.OPERATOR);
        assertStyle(line, "forge:ingots/iron", Style.RESOURCE);
        assertStyle(line, "WITH", Style.KEYWORD);
        assertStyle(line, "storage", Style.LABEL);
        assertStyle(line, "-- OUTPUT #comment", Style.COMMENT);
        assertStyle(SfmlHighlighter.highlight("WITH TAG ingots").lines.get(0), "ingots", Style.RESOURCE);
        // These keyword tokens are also accepted by the grammar's identifier rule.
        Line keywordIds = SfmlHighlighter.highlight("INPUT mod:global mod:top mod:level").lines.get(0);
        assertStyle(keywordIds, "mod:global", Style.RESOURCE);
        assertStyle(keywordIds, "mod:top", Style.RESOURCE);
        assertStyle(keywordIds, "mod:level", Style.RESOURCE);
    }

    @Test
    void keepsCommentMarkersAndEscapedQuotesInsideStrings() {
        String quoted = "\"quoted -- #tag \\\"OUTPUT\\\" tail\"";
        Line line = SfmlHighlighter.highlight("NAME " + quoted + " OUTPUT -- comment").lines.get(0);
        assertStyle(line, quoted, Style.STRING);
        assertEquals(Style.ACTION, line.styleAt(line.text.lastIndexOf("OUTPUT")));
        assertStyle(line, "-- comment", Style.COMMENT);
    }

    @Test
    void closingQuoteAfterEscapedBackslashDoesNotSwallowTheNextString() {
        String first = "\"C:\\\\\"";
        String second = "\"iron -- not a comment\"";
        Line line = SfmlHighlighter.highlight("LET $path = " + first + " LET $item = " + second + " -- comment").lines
            .get(0);
        assertStyle(line, first, Style.STRING);
        assertStyle(line, second, Style.STRING);
        assertStyle(line, "$path", Style.VARIABLE);
        assertStyle(line, "$item", Style.VARIABLE);
        assertEquals(Style.FLOW, line.styleAt(line.text.lastIndexOf("LET")));
        assertStyle(line, "-- comment", Style.COMMENT);
    }

    @Test
    void splitsMultilineStringsIntoPhysicalLineSpansWithoutLosingTheirStyle() {
        Highlighted highlighted = SfmlHighlighter.highlight("NAME \"first\n-- still #string\nlast\" OUTPUT\n");
        assertEquals(4, highlighted.lines.size());
        assertStyle(highlighted.lines.get(0), "\"first", Style.STRING);
        assertStyle(highlighted.lines.get(1), "-- still #string", Style.STRING);
        assertStyle(highlighted.lines.get(2), "last\"", Style.STRING);
        assertStyle(highlighted.lines.get(2), "OUTPUT", Style.ACTION);
        assertEquals("", highlighted.lines.get(3).text);
        assertTrue(highlighted.lines.get(3).spans.isEmpty());
        assertCompleteSpans(highlighted);
    }

    @Test
    void convertsCodePointTokenOffsetsToUtf16BeforeAndInsideColoredTokens() {
        Highlighted highlighted = SfmlHighlighter
            .highlight("\uD83D\uDE00 INPUT 12\nNAME \"\uD83D\uDE42\" OUTPUT 34 -- \uD83D\uDE80");
        assertStyle(highlighted.lines.get(0), "\uD83D\uDE00", Style.TEXT);
        assertStyle(highlighted.lines.get(0), "INPUT", Style.ACTION);
        assertStyle(highlighted.lines.get(0), "12", Style.NUMBER);
        assertStyle(highlighted.lines.get(1), "\"\uD83D\uDE42\"", Style.STRING);
        assertStyle(highlighted.lines.get(1), "OUTPUT", Style.ACTION);
        assertStyle(highlighted.lines.get(1), "34", Style.NUMBER);
        assertStyle(highlighted.lines.get(1), "-- \uD83D\uDE80", Style.COMMENT);
        assertCompleteSpans(highlighted);
    }

    @Test
    void preservesIncompleteAndUnexpectedSourceWithoutParsingOrDroppingCharacters() {
        String source = "EVERY @\u0000 [futureVariable] = 1\nNAME \"unfinished\nINPUT mod:\n";
        Highlighted highlighted = assertDoesNotThrow(() -> SfmlHighlighter.highlight(source));
        assertEquals(source, joinLines(highlighted.lines));
        assertStyle(highlighted.lines.get(0), "futureVariable", Style.LABEL);
        assertStyle(highlighted.lines.get(0), "@\u0000 [", Style.TEXT);
        assertStyle(highlighted.lines.get(2), "INPUT", Style.ACTION);
        assertStyle(highlighted.lines.get(2), "mod:", Style.RESOURCE);
        assertCompleteSpans(highlighted);
        Highlighted empty = SfmlHighlighter.highlight("");
        assertEquals(1, empty.lines.size());
        assertTrue(empty.lines.get(0).spans.isEmpty());
    }

    @Test
    void physicalLineSpansRetainColorsWhenVisibleSlicesStartOrEndInsideTokens() {
        Line line = SfmlHighlighter.highlight("INPUT minecraft:long_resource_name FROM source -- a long comment").lines
            .get(0);
        int wrappedStart = line.text.indexOf("long_resource");
        int wrappedEnd = line.text.indexOf("_name") + 2;
        Span resource = spanAt(line, wrappedStart);
        assertEquals(Style.RESOURCE, resource.style);
        assertTrue(resource.start < wrappedStart);
        assertTrue(resource.end > wrappedEnd);
        assertEquals("long_resource_n", line.text.substring(wrappedStart, wrappedEnd));
        Span comment = spanAt(line, line.text.indexOf("long comment"));
        assertEquals(Style.COMMENT, comment.style);
        assertEquals(line.text.length(), comment.end);
        // An edit produces a separate cache; the earlier line's spans remain unchanged.
        assertStyle(SfmlHighlighter.highlight("OUTPUT to target").lines.get(0), "OUTPUT", Style.ACTION);
        assertStyle(line, "INPUT", Style.ACTION);
        assertThrows(UnsupportedOperationException.class, () -> line.spans.clear());
    }

    private static void assertStyle(Line line, String text, Style expected) {
        int start = line.text.indexOf(text);
        assertTrue(start >= 0, "Missing expected source text: " + text);
        for (int i = start; i < start + text.length(); i++) {
            assertEquals(expected, line.styleAt(i), "Style at UTF-16 column " + i + " in " + line.text);
        }
    }

    private static Span spanAt(Line line, int column) {
        for (Span span : line.spans) {
            if (span.start <= column && span.end > column) return span;
        }
        throw new AssertionError("No span at column " + column);
    }

    private static void assertCompleteSpans(Highlighted highlighted) {
        for (Line line : highlighted.lines) {
            int end = 0;
            for (Span span : line.spans) {
                assertEquals(end, span.start);
                assertTrue(span.end > span.start);
                assertTrue(span.end <= line.text.length());
                assertFalse(
                    span.start > 0 && Character.isHighSurrogate(line.text.charAt(span.start - 1))
                        && Character.isLowSurrogate(line.text.charAt(span.start)),
                    "A span must not split a surrogate pair");
                end = span.end;
            }
            assertEquals(line.text.length(), end);
        }
    }

    private static String joinLines(List<Line> lines) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) result.append('\n');
            result.append(lines.get(i).text);
        }
        return result.toString();
    }
}
