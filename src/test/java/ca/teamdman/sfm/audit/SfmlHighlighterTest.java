package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter;
import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter.Highlighted;
import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter.Line;
import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter.Span;
import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter.Style;
import ca.teamdman.sfm.client.gui.syntax.SnippetCategory;
import ca.teamdman.sfm.client.gui.syntax.SyntaxRegistry;
import ca.teamdman.sfm.client.gui.syntax.SyntaxSnippet;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class SfmlHighlighterTest {

    @Test
    void namedRedstoneEdgeWordsAreFlowOnlyInsideTheTriggerHeader() {
        Highlighted highlighted = SfmlHighlighter.highlight(
            "EVERY REDSTONE RISING FROM sensor NORTH SIDE DO END\n"
                + "EVERY REDSTONE FALLING FROM sensor NORTH SIDE DO END\n"
                + "EVERY REDSTONE -- edge follows\nrIsInG FROM sensor DO END\n"
                + "INPUT RISING:FALLING FROM RISING, FALLING\n"
                + "LET $value = $record.RISING + $record.FALLING\n"
                + "NAME \"RISING FALLING\" -- RISING FALLING\n"
                + "FUNCTION signals.RISING($value: INTEGER) RETURNS INTEGER DO RETURN $value END\n"
                + "LET $value = signals.FALLING(1)");
        assertStyle(highlighted.lines.get(0), "RISING", Style.FLOW);
        assertStyle(highlighted.lines.get(1), "FALLING", Style.FLOW);
        assertStyle(highlighted.lines.get(3), "rIsInG", Style.FLOW);
        assertStyle(highlighted.lines.get(4), "RISING:FALLING", Style.RESOURCE);
        for (String word : new String[] { "RISING", "FALLING" }) {
            assertEquals(
                Style.LABEL,
                highlighted.lines.get(4)
                    .styleAt(highlighted.lines.get(4).text.lastIndexOf(word)));
            assertStyle(highlighted.lines.get(5), word, Style.LABEL);
        }
        assertStyle(highlighted.lines.get(6), "\"RISING FALLING\"", Style.STRING);
        assertStyle(highlighted.lines.get(6), "-- RISING FALLING", Style.COMMENT);
        assertStyle(highlighted.lines.get(7), "RISING", Style.ACTION);
        assertStyle(highlighted.lines.get(8), "FALLING", Style.ACTION);
        assertCompleteSpans(highlighted);
    }

    @Test
    void nativeRedstoneQueriesAreActionsOnlyForDotCalls() {
        Highlighted highlighted = SfmlHighlighter.highlight(
            "LET $level = $sensor.redstone()\nLET $on = $sensor.powered()\n"
                + "LET $ready = $sensor.redstoneAvailable -- call follows\n()\n"
                + "INPUT mod:redstone mod:powered mod:redstoneAvailable FROM powered\n"
                + "LET $value = $record.powered + $record.redstoneAvailable\n"
                + "NAME \"redstone() powered() redstoneAvailable()\" -- .redstone() .powered()");
        assertStyle(highlighted.lines.get(0), "redstone", Style.ACTION);
        assertStyle(highlighted.lines.get(1), "powered", Style.ACTION);
        assertStyle(highlighted.lines.get(2), "redstoneAvailable", Style.ACTION);
        assertStyle(highlighted.lines.get(4), "mod:redstone", Style.RESOURCE);
        assertStyle(highlighted.lines.get(4), "mod:powered", Style.RESOURCE);
        assertStyle(highlighted.lines.get(4), "mod:redstoneAvailable", Style.RESOURCE);
        assertStyle(highlighted.lines.get(5), "powered", Style.LABEL);
        assertStyle(highlighted.lines.get(5), "redstoneAvailable", Style.LABEL);
        assertStyle(highlighted.lines.get(6), "\"redstone() powered() redstoneAvailable()\"", Style.STRING);
        assertStyle(highlighted.lines.get(6), "-- .redstone() .powered()", Style.COMMENT);
        assertCompleteSpans(highlighted);
    }

    @Test
    void playerFunctionSignaturesCallsAndReturnsHaveContextualColors() {
        Highlighted highlighted = SfmlHighlighter.highlight(
            "FUNCTION math.double($value: INTEGER) RETURNS INTEGER DO\nRETURN $value * 2\nEND\n"
                + "LET $result = math.double(3)\n"
                + "FUNCTION stock.total($values: {name: STRING, count: INTEGER}[3]) RETURNS INTEGER DO\nRETURN 0 END\n"
                + "INPUT FUNCTION:RETURN FROM RETURNS\n"
                + "LET $value = $record.FUNCTION + $record.RETURN\n"
                + "NAME \"FUNCTION RETURNS RETURN\" -- FUNCTION RETURNS RETURN");
        assertStyle(highlighted.lines.get(0), "FUNCTION", Style.FLOW);
        assertStyle(highlighted.lines.get(0), "math", Style.ACTION);
        assertStyle(highlighted.lines.get(0), "double", Style.ACTION);
        assertStyle(highlighted.lines.get(0), "RETURNS", Style.FLOW);
        assertStyle(highlighted.lines.get(0), "INTEGER", Style.KEYWORD);
        assertStyle(highlighted.lines.get(1), "RETURN", Style.FLOW);
        assertStyle(highlighted.lines.get(3), "math", Style.ACTION);
        assertStyle(highlighted.lines.get(3), "double", Style.ACTION);
        assertStyle(highlighted.lines.get(4), "STRING", Style.KEYWORD);
        assertStyle(highlighted.lines.get(4), "INTEGER", Style.KEYWORD);
        assertStyle(highlighted.lines.get(6), "FUNCTION:RETURN", Style.RESOURCE);
        assertStyle(highlighted.lines.get(6), "RETURNS", Style.LABEL);
        assertStyle(highlighted.lines.get(7), "FUNCTION", Style.LABEL);
        assertStyle(highlighted.lines.get(7), "RETURN", Style.LABEL);
        assertStyle(highlighted.lines.get(8), "\"FUNCTION RETURNS RETURN\"", Style.STRING);
        assertStyle(highlighted.lines.get(8), "-- FUNCTION RETURNS RETURN", Style.COMMENT);
        assertCompleteSpans(highlighted);
    }

    @Test
    void loopHeadersUseFlowColorsWithoutRecoloringLegacyNamesOrResources() {
        Highlighted highlighted = SfmlHighlighter.highlight(
            "FOR $i = 0 TO 3 STEP 1 DO END\n" + "FOR $value IN [1, 2] DO END\n"
                + "while (FALSE) DO END\n"
                + "INPUT FOR:WHILE FROM STEP\n"
                + "INPUT FROM FOR, WHILE, STEP TOP SIDE\n"
                + "LET $value = $record.FOR + $record.WHILE + $record.STEP\n"
                + "NAME \"FOR WHILE STEP\" -- FOR WHILE STEP");
        assertStyle(highlighted.lines.get(0), "FOR", Style.FLOW);
        assertStyle(highlighted.lines.get(0), "STEP", Style.FLOW);
        assertStyle(highlighted.lines.get(1), "FOR", Style.FLOW);
        assertStyle(highlighted.lines.get(2), "while", Style.FLOW);
        assertStyle(highlighted.lines.get(3), "FOR:WHILE", Style.RESOURCE);
        assertStyle(highlighted.lines.get(3), "STEP", Style.LABEL);
        for (String word : new String[] { "FOR", "WHILE", "STEP" }) {
            assertStyle(highlighted.lines.get(4), word, Style.LABEL);
            assertStyle(highlighted.lines.get(5), word, Style.LABEL);
        }
        assertStyle(highlighted.lines.get(6), "\"FOR WHILE STEP\"", Style.STRING);
        assertStyle(highlighted.lines.get(6), "-- FOR WHILE STEP", Style.COMMENT);
        assertCompleteSpans(highlighted);
    }

    @Test
    void loopHeaderLookaheadCrossesCommentsAndPhysicalLines() {
        Highlighted highlighted = SfmlHighlighter.highlight(
            "for -- binding follows\n$i = 0 TO 3 -- step follows\nstep 1 DO\nWHILE source HAS GT 0 DO END\nEND");
        assertStyle(highlighted.lines.get(0), "for", Style.FLOW);
        assertStyle(highlighted.lines.get(2), "step", Style.FLOW);
        assertStyle(highlighted.lines.get(3), "WHILE", Style.FLOW);
        assertCompleteSpans(highlighted);
    }

    @Test
    void printCallsAreActionsWhileLegacyNamesResourcesStringsAndCommentsKeepTheirContext() {
        Highlighted highlighted = SfmlHighlighter.highlight(
            "PRINT(1)\nINPUT PRINT:PRINT FROM PRINT\n" + "LET $value = $record.PRINT\nPRINT(\"PRINT(1)\") -- PRINT(2)");
        assertStyle(highlighted.lines.get(0), "PRINT", Style.ACTION);
        assertStyle(highlighted.lines.get(1), "PRINT:PRINT", Style.RESOURCE);
        assertEquals(
            Style.LABEL,
            highlighted.lines.get(1)
                .styleAt(highlighted.lines.get(1).text.lastIndexOf("PRINT")));
        assertStyle(highlighted.lines.get(2), "PRINT", Style.LABEL);
        assertStyle(highlighted.lines.get(3), "\"PRINT(1)\"", Style.STRING);
        assertStyle(highlighted.lines.get(3), "-- PRINT(2)", Style.COMMENT);
    }

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

    @Test
    void stylesTypedConstructorsAndOnlyDotCallQueriesAsActions() {
        Highlighted highlighted = SfmlHighlighter.highlight(
            "LET $src = lAbEl(\"Exact Case\")\n" + "LET $n = $src.CoUnT(iTeM(\"minecraft:iron_ingot\"))\n"
                + "LET $e = $src.empty(ITEM(\"*\"))\n"
                + "LET $a = $src.available -- comment before call\n(ITEM(\"*\"))");
        assertStyle(highlighted.lines.get(0), "lAbEl", Style.ACTION);
        assertStyle(highlighted.lines.get(0), "\"Exact Case\"", Style.STRING);
        assertStyle(highlighted.lines.get(1), "CoUnT", Style.ACTION);
        assertStyle(highlighted.lines.get(1), "iTeM", Style.ACTION);
        assertStyle(highlighted.lines.get(2), "empty", Style.ACTION);
        assertStyle(highlighted.lines.get(3), "available", Style.ACTION);
        assertStyle(highlighted.lines.get(3), "-- comment before call", Style.COMMENT);
        assertCompleteSpans(highlighted);
    }

    @Test
    void typedCallColorsDoNotLeakIntoRawLabelsResourcesFieldsCommentsOrStrings() {
        Highlighted highlighted = SfmlHighlighter.highlight(
            "INPUT mod:count FROM count\n" + "LET $x = {count: 1, empty: FALSE}.count\n"
                + "LET $y = $x.empty\n"
                + "NAME \"LABEL ITEM count empty available(\" -- LABEL( ITEM( .count(\n"
                + "INPUT FROM item\n"
                + "ROUND ROBIN BY LABEL");
        assertStyle(highlighted.lines.get(0), "mod:count", Style.RESOURCE);
        assertEquals(
            Style.LABEL,
            highlighted.lines.get(0)
                .styleAt(highlighted.lines.get(0).text.lastIndexOf("count")));
        assertNotEquals(
            Style.ACTION,
            highlighted.lines.get(1)
                .styleAt(highlighted.lines.get(1).text.lastIndexOf("count")));
        assertNotEquals(
            Style.ACTION,
            highlighted.lines.get(2)
                .styleAt(highlighted.lines.get(2).text.indexOf("empty")));
        assertStyle(highlighted.lines.get(3), "\"LABEL ITEM count empty available(\"", Style.STRING);
        assertStyle(highlighted.lines.get(3), "-- LABEL( ITEM( .count(", Style.COMMENT);
        assertStyle(highlighted.lines.get(4), "item", Style.LABEL);
        assertStyle(highlighted.lines.get(5), "LABEL", Style.KEYWORD);
    }

    @Test
    void typedQueryPaletteRetainsExecutableOneArgumentExamplesInQueryMenu() {
        SyntaxSnippet label = SyntaxRegistry.getSnippetsForCategory(SnippetCategory.LABEL)
            .stream()
            .filter(
                snippet -> snippet.getId()
                    .equals("label_typed_local"))
            .findFirst()
            .get();
        String binding = label.getTemplate()
            .replace("<label>", "storage");
        assertEquals("LET $src = LABEL(\"storage\")", binding);
        int checked = 0;
        for (SyntaxSnippet snippet : SyntaxRegistry.getSnippetsForCategory(SnippetCategory.QUERY)) {
            if (!snippet.getId()
                .startsWith("cond_typed_")) continue;
            String condition = snippet.getTemplate()
                .replace("<label>", "storage")
                .replace("<quantity>", "1");
            assertTrue(condition.contains("ITEM("));
            assertFalse(
                condition.contains(".count()") || condition.contains(".empty()") || condition.contains(".available()"));
            String source = "EVERY 20 TICKS DO\n" + binding + "\nIF " + condition + " THEN FORGET END\nEND";
            ca.teamdman.sfml.program_builder.ProgramBuildResult result = new ProgramBuilder(source).build();
            assertTrue(result.isSuccess(), result.getErrorMessage() + "\n" + source);
            checked++;
        }
        assertEquals(3, checked);
    }

    @Test
    void bitwiseSymbolsAreOperatorsWithoutLeakingIntoStringsOrComments() {
        Highlighted highlighted = SfmlHighlighter
            .highlight("LET $n = ~1 & 2 | 3 ^ 4 << 5 >> 6 >>> 7\nNAME \"& | ^ ~ << >> >>>\" -- & | ^ ~ << >> >>>");
        for (String operator : new String[] { "~", "&", "|", "^", "<<", ">>", ">>>" }) {
            assertStyle(highlighted.lines.get(0), operator, Style.OPERATOR);
        }
        assertStyle(highlighted.lines.get(1), "\"& | ^ ~ << >> >>>\"", Style.STRING);
        assertStyle(highlighted.lines.get(1), "-- & | ^ ~ << >> >>>", Style.COMMENT);
        assertCompleteSpans(highlighted);
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
