package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfml.ast.GlobMatcher;
import ca.teamdman.sfml.ast.ResourceIdentifier;

class GlobMatcherRegressionTest {

    @Test
    void matchesExistingGlobSemanticsIncludingUnicodeCodePointsAndAsciiCase() {
        String[] patterns = { "", "*", "?", "**a?", "*a*b*", "a.b", "[a](b)+|^$\\", "\u00c4", "\ud83d\ude00?", "*\n*",
            "?x", "*x", ".*", "\ud800", "*\u2028*" };
        String[] inputs = { "", "A", "ab", "aaab", "a.b", "[a](b)+|^$\\", "\u00c4", "\u00e4", "\ud83d\ude00",
            "\ud83d\ude00x", "\n", "\rx", "\u0085x", "\u2028x", "\u2029x", ".suffix", "\ud800", "a\nb", "a\u2028b" };
        for (String pattern : patterns) for (String input : inputs) {
            assertEquals(
                reference(pattern, input),
                new GlobMatcher(pattern).matches(input),
                "pattern=" + pattern + ", input=" + input);
        }
        assertTrue(new GlobMatcher("?").matches("\ud83d\ude00"));
        assertFalse(new GlobMatcher("??").matches("\ud83d\ude00"));
        assertFalse(new GlobMatcher("\u00c4").matches("\u00e4"));
        assertFalse(new GlobMatcher("[ab]").matches("a"));
        assertTrue(new GlobMatcher("[ab]").matches("[ab]"));
    }

    @Test
    void repeatedAndSeparatedStarsHaveDeterministicPolynomialAccessBounds() {
        String[] patterns = { repeat("*", 4000) + "x", repeat("*a", 128) + "b", "*" + repeat("a", 128) + "b" };
        for (String pattern : patterns) {
            CountingText input = new CountingText(repeat("a", 4096));
            assertFalse(new GlobMatcher(pattern).matches(input));
            assertTrue(
                input.reads <= 8L * (pattern.length() + 1) * (input.length() + 1),
                "Character reads must stay within a polynomial bound: " + input.reads);
        }
        CountingText supplementary = new CountingText(repeat("\ud83d\ude00", 1024));
        String pattern = "*" + repeat("?", 64) + "x";
        assertFalse(new GlobMatcher(pattern).matches(supplementary));
        assertTrue(supplementary.reads <= 8L * (pattern.length() + 1) * (supplementary.length() + 1));
    }

    @Test
    void resourceSelectorsKeepTheirExplicitDotStarAliasAndLiteralPunctuation() {
        assertTrue(
            ResourceIdentifier.fromString("fluid:.*")
                .matchesFluid(TestFluid.water(1)));
        assertTrue(
            ResourceIdentifier.fromString("fluid:W?T*")
                .matchesFluid(TestFluid.water(1)));
        assertFalse(
            ResourceIdentifier.fromString("fluid:w.t*r")
                .matchesFluid(TestFluid.water(1)));
        assertFalse(
            ResourceIdentifier.fromString("fluid:" + repeat("*", 1000) + "x")
                .matchesFluid(TestFluid.water(1)));
    }

    private static boolean reference(String pattern, String input) {
        StringBuilder regex = new StringBuilder();
        for (int offset = 0; offset < pattern.length();) {
            int point = pattern.codePointAt(offset);
            offset += Character.charCount(point);
            if (point == '*') regex.append(".*");
            else if (point == '?') regex.append('.');
            else regex.append(Pattern.quote(new String(Character.toChars(point))));
        }
        return Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE)
            .matcher(input)
            .matches();
    }

    private static String repeat(String text, int times) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < times; i++) result.append(text);
        return result.toString();
    }

    private static final class CountingText implements CharSequence {

        private final String value;
        private long reads;

        CountingText(String value) {
            this.value = value;
        }

        @Override
        public int length() {
            return value.length();
        }

        @Override
        public char charAt(int index) {
            reads++;
            return value.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            throw new AssertionError("Matcher must not copy or slice the text");
        }
    }
}
