package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter;

class SfmlBlockMatchesTest {

    @Test
    void mixedBlocksAndElseIfShareTheCorrectEnd() {
        String source = "every 20 ticks do if true then print(1) else if false then print(2) else while true { print(3) } end end";
        var pairs = SfmlHighlighter.highlight(source).blockMatches;
        assertEquals(3, pairs.size());
        assertEquals("{", source.substring(pairs.get(0).start, pairs.get(0).end));
        assertEquals(source.indexOf("then"), pairs.get(1).start);
        assertEquals(source.lastIndexOf("end"), pairs.get(2).closeStart);
    }

    @Test
    void ignoresStringsCommentsAndMemberNamesWithUtf16Offsets() {
        String source = "-- 😀 { do end\nevery 1 ticks do print(\"} end\", value.end, value.do) end";
        var pairs = SfmlHighlighter.highlight(source).blockMatches;
        assertEquals(1, pairs.size());
        var pair = pairs.get(0);
        assertEquals(source.indexOf("do print"), pair.start);
        assertEquals(source.lastIndexOf("end"), pair.closeStart);
        assertTrue(pair.touches(pair.start));
        assertTrue(pair.touches(pair.closeEnd));
        assertFalse(pair.touches(pair.end + 1));
    }

    @Test
    void incompleteAndMismatchedDelimitersDoNotInventPairs() {
        assertTrue(SfmlHighlighter.highlight("every 1 ticks do { end").blockMatches.isEmpty());
        assertTrue(SfmlHighlighter.highlight("}").blockMatches.isEmpty());
        assertTrue(SfmlHighlighter.highlight("").blockMatches.isEmpty());
    }
}
