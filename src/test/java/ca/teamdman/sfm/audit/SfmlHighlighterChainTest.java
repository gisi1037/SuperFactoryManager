package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.List;
import java.util.Random;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter;
import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter.Highlighted;
import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter.Style;

class SfmlHighlighterChainTest {

    @Test
    void dottedChainMembershipMatchesPriorWalkForMalformedAndHiddenTokens() throws Exception {
        String[] examples = { "", "a.b()", "a.b", ".a.b()", "$x.a.b()", "a..b()", "a.b.()", "a.FUNCTION.b()",
            "a.RISING.FALLING()", "mod:a.b()", "FUNCTION a.b() RETURNS INTEGER DO END",
            "a -- hidden\n. -- hidden\nb -- hidden\n()", "a. -- hidden\n.b()", "a( b.c() ).d.e()",
            "NAME \"a.b()\" -- a.b()\nLET $x = \uD83D\uDE00 a.b()" };
        for (String example : examples) assertLegacyMembership(example);
        Random random = new Random(1037);
        String[] fragments = { "a", "b", "RISING", "FALLING", ".", "(", ")", "FUNCTION", "INTEGER", "FROM", "$x", ":",
            " ", "\n", "-- a.b()\n", "\"a.b()\"", "\uD83D\uDE00" };
        for (int sample = 0; sample < 300; sample++) {
            StringBuilder source = new StringBuilder();
            for (int i = 0; i < 80; i++) source.append(fragments[random.nextInt(fragments.length)])
                .append(' ');
            assertLegacyMembership(source.toString());
        }
    }

    @Test
    void nearLimitDottedCallKeepsExactAlternatingSpansAndFieldChainsStayLabels() {
        String chain = SfmlHighlighterCharacterization.chain(14980);
        Highlighted call = SfmlHighlighter.highlight(chain + "()");
        assertEquals(1, call.lines.size());
        assertEquals(chain + "()", call.lines.get(0).text);
        assertEquals(chain.length() + 1, call.lines.get(0).spans.size());
        for (int i = 0; i < chain.length(); i++) {
            SfmlHighlighter.Span span = call.lines.get(0).spans.get(i);
            assertEquals(i, span.start);
            assertEquals(i + 1, span.end);
            assertEquals(i % 2 == 0 ? Style.ACTION : Style.OPERATOR, span.style);
        }
        SfmlHighlighter.Span end = call.lines.get(0).spans.get(chain.length());
        assertEquals(chain.length(), end.start);
        assertEquals(chain.length() + 2, end.end);
        assertEquals(Style.TEXT, end.style);
        Highlighted field = SfmlHighlighter.highlight("$x." + chain + "()");
        // A dotted field after a variable never becomes a plain function name.
        for (int i = 0; i < chain.length(); i += 2) {
            assertEquals(Style.LABEL, field.lines.get(0).spans.get(i + 2).style);
        }
    }

    @Test
    void dottedChainPrepassUsesLinearTokenReads() throws Exception {
        for (String suffix : new String[] { "()", "", ".()" }) {
            for (int size : new int[] { 1000, 2000, 4000, 14980 }) {
                CountingTokens tokens = new CountingTokens(lex(SfmlHighlighterCharacterization.chain(size) + suffix));
                boolean[] names = names(tokens);
                assertTrue(
                    tokens.reads <= 12L * tokens.size(),
                    "Token reads=" + tokens.reads + " size=" + tokens.size());
                assertEquals(tokens.size(), names.length);
                assertEquals("()".equals(suffix), names[0]);
            }
        }
    }

    @Test
    @EnabledIfSystemProperty(named = "sfm.auditBenchmark", matches = "true")
    void characterizeDottedChains() throws Exception {
        SfmlHighlighterCharacterization.main(new String[0]);
    }

    private static void assertLegacyMembership(String source) throws Exception {
        List<? extends Token> tokens = lex(source);
        boolean[] actual = names(tokens);
        for (int i = 0; i < tokens.size(); i++) {
            assertEquals(legacyName(tokens, i), actual[i], "Token " + i + " in " + source);
        }
    }

    private static List<? extends Token> lex(String source) {
        SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(source));
        lexer.INCLUDE_UNUSED = true;
        lexer.removeErrorListeners();
        return lexer.getAllTokens();
    }

    private static boolean[] names(List<? extends Token> tokens) throws Exception {
        Method method = SfmlHighlighter.class.getDeclaredMethod("findFunctionNames", List.class);
        method.setAccessible(true);
        return (boolean[]) method.invoke(null, tokens);
    }

    // Independent copy of the old per-token chain walk; keeps the equivalence contract explicit.
    private static boolean legacyName(List<? extends Token> tokens, int index) {
        if (!identifier(
            tokens.get(index)
                .getType()))
            return false;
        int first = index, last = index;
        for (int dot = previous(tokens, first); dot >= 0 && tokens.get(dot)
            .getType() == SFMLLexer.DOT;) {
            int word = previous(tokens, dot);
            if (word < 0 || !identifier(
                tokens.get(word)
                    .getType()))
                break;
            first = word;
            dot = previous(tokens, first);
        }
        int before = previous(tokens, first);
        if (before >= 0 && tokens.get(before)
            .getType() == SFMLLexer.DOT) return false;
        for (int dot = next(tokens, last); dot < tokens.size() && tokens.get(dot)
            .getType() == SFMLLexer.DOT;) {
            int word = next(tokens, dot);
            if (word >= tokens.size() || !identifier(
                tokens.get(word)
                    .getType()))
                break;
            last = word;
            dot = next(tokens, last);
        }
        int after = next(tokens, last);
        return after < tokens.size() && tokens.get(after)
            .getType() == SFMLLexer.LPAREN;
    }

    private static boolean identifier(int type) {
        return type == SFMLLexer.IDENTIFIER || type == SFMLLexer.RISING || type == SFMLLexer.FALLING;
    }

    private static int next(List<? extends Token> tokens, int index) {
        do {
            index++;
        } while (index < tokens.size() && tokens.get(index)
            .getChannel() != Token.DEFAULT_CHANNEL);
        return index;
    }

    private static int previous(List<? extends Token> tokens, int index) {
        do {
            index--;
        } while (index >= 0 && tokens.get(index)
            .getChannel() != Token.DEFAULT_CHANNEL);
        return index;
    }

    private static final class CountingTokens extends AbstractList<Token> {

        final List<? extends Token> tokens;
        long reads;

        CountingTokens(List<? extends Token> tokens) {
            this.tokens = tokens;
        }

        @Override
        public Token get(int index) {
            reads++;
            return tokens.get(index);
        }

        @Override
        public int size() {
            return tokens.size();
        }
    }
}
