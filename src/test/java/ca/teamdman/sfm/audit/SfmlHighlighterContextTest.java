package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.junit.jupiter.api.Test;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter;
import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter.Style;

class SfmlHighlighterContextTest {

    @Test
    void contextMatchesThePriorNearestKeywordWalkIncludingIncompleteCode() throws Exception {
        for (String source : new String[] { "", "INTEGER STEP", "FUNCTION utility() RETURNS INTEGER DO END",
            "FUNCTION utility($x: {count: INTEGER, nested: BOOLEAN[2]}) RETURNS STRING DO END",
            "INPUT FROM FUNCTION INTEGER", "FUNCTION utility() RETURNS INTEGER LET $x = INTEGER",
            "FOR $i = 0 TO 4 STEP 1 DO END", "FOR label TO 4 STEP 1", "FOR $i = 0 TO 4 FORGET STEP",
            "FOR $i = 0 TO 4 -- keep\n STEP 1 DO END", "\"FUNCTION x() INTEGER\" -- FOR $i TO STEP\n STEP",
            "FUNCTION a() RETURNS INTEGER . FUNCTION b INTEGER", "FOR $i TO FOR $j STEP TO STEP" }) equivalent(source);
        Random random = new Random(1093);
        String[] fragments = { "FUNCTION", "utility", "RETURNS", "RETURN", "INTEGER", "BOOLEAN", "STRING", "FOR", "$i",
            "STEP", "TO", "DO", "END", "INPUT", "OUTPUT", "FORGET", "LET", "STATE", ".", ",", "(", ")",
            "-- FUNCTION false() FOR $x TO STEP\n", "\"INTEGER STEP\"", "\uD83C\uDFED", " ", "\n" };
        for (int sample = 0; sample < 400; sample++) {
            StringBuilder source = new StringBuilder();
            for (int i = 0; i < 80; i++) source.append(fragments[random.nextInt(fragments.length)])
                .append(' ');
            equivalent(source.toString());
        }
    }

    @Test
    void nearLimitTypeAndStepFragmentsKeepColorsWithLinearContextReads() throws Exception {
        for (int count : new int[] { 100, 1000, 3500 }) {
            for (String source : new String[] {
                "FUNCTION utility() RETURNS " + String.join("", Collections.nCopies(count, "INTEGER ")),
                "FOR $i = 0 TO 4 " + String.join("", Collections.nCopies(count, "STEP ")) }) {
                CountingTokens tokens = new CountingTokens(lex(source));
                boolean[] names = names(tokens.values);
                context(tokens, names);
                assertTrue(
                    tokens.reads <= 12L * tokens.size(),
                    "Context token reads=" + tokens.reads + " size=" + tokens.size());
                String word = source.startsWith("FUNCTION") ? "INTEGER" : "STEP";
                Style expected = source.startsWith("FUNCTION") ? Style.KEYWORD : Style.FLOW;
                SfmlHighlighter.Line line = SfmlHighlighter.highlight(source).lines.get(0);
                int start = source.indexOf(word);
                for (int i = start; i < source.length(); i += word.length() + 1)
                    assertEquals(expected, line.styleAt(i));
            }
        }
        String field = "FOR $i TO 4 $record.STEP";
        assertEquals(
            Style.LABEL,
            SfmlHighlighter.highlight(field).lines.get(0)
                .styleAt(field.indexOf("STEP")));
    }

    private static void equivalent(String source) throws Exception {
        List<? extends Token> tokens = lex(source);
        boolean[] names = names(tokens);
        Object actual = context(tokens, names);
        Field header = actual.getClass()
            .getDeclaredField("functionHeader");
        Field step = actual.getClass()
            .getDeclaredField("countedStep");
        header.setAccessible(true);
        step.setAccessible(true);
        boolean[] headers = (boolean[]) header.get(actual), steps = (boolean[]) step.get(actual);
        for (int i = 0; i < tokens.size(); i++) {
            assertEquals(legacyFunction(tokens, i, names), headers[i], "Header token " + i + " in " + source);
            assertEquals(legacyStep(tokens, i), steps[i], "STEP token " + i + " in " + source);
        }
    }

    private static boolean legacyFunction(List<? extends Token> tokens, int index, boolean[] names) throws Exception {
        for (int i = previous(tokens, index); i >= 0; i = previous(tokens, i)) {
            int type = tokens.get(i)
                .getType();
            if (type == SFMLLexer.FUNCTION) {
                Method flow = SfmlHighlighter.class
                    .getDeclaredMethod("isFunctionFlow", List.class, int.class, boolean[].class);
                flow.setAccessible(true);
                return (boolean) flow.invoke(null, tokens, i, names);
            }
            if (type == SFMLLexer.DO || type == SFMLLexer.END
                || type == SFMLLexer.INPUT
                || type == SFMLLexer.OUTPUT
                || type == SFMLLexer.LET
                || type == SFMLLexer.STATE) return false;
        }
        return false;
    }

    private static boolean legacyStep(List<? extends Token> tokens, int index) {
        boolean hasTo = false;
        for (int i = previous(tokens, index); i >= 0; i = previous(tokens, i)) {
            int type = tokens.get(i)
                .getType();
            if (type == SFMLLexer.FOR) {
                int next = i + 1;
                while (next < tokens.size() && tokens.get(next)
                    .getChannel() != Token.DEFAULT_CHANNEL) next++;
                return hasTo && next < tokens.size()
                    && tokens.get(next)
                        .getType() == SFMLLexer.VARIABLE;
            }
            if (type == SFMLLexer.TO) hasTo = true;
            if (type == SFMLLexer.DO || type == SFMLLexer.END
                || type == SFMLLexer.INPUT
                || type == SFMLLexer.OUTPUT
                || type == SFMLLexer.FORGET) return false;
        }
        return false;
    }

    private static int previous(List<? extends Token> tokens, int index) {
        do {
            index--;
        } while (index >= 0 && tokens.get(index)
            .getChannel() != Token.DEFAULT_CHANNEL);
        return index;
    }

    private static Object context(List<? extends Token> tokens, boolean[] names) throws Exception {
        Method method = SfmlHighlighter.class.getDeclaredMethod("findContext", List.class, boolean[].class);
        method.setAccessible(true);
        return method.invoke(null, tokens, names);
    }

    private static boolean[] names(List<? extends Token> tokens) throws Exception {
        Method method = SfmlHighlighter.class.getDeclaredMethod("findFunctionNames", List.class);
        method.setAccessible(true);
        return (boolean[]) method.invoke(null, tokens);
    }

    private static List<? extends Token> lex(String source) {
        SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(source));
        lexer.INCLUDE_UNUSED = true;
        lexer.removeErrorListeners();
        return lexer.getAllTokens();
    }

    private static final class CountingTokens extends AbstractList<Token> {

        final List<? extends Token> values;
        long reads;

        CountingTokens(List<? extends Token> values) {
            this.values = values;
        }

        @Override
        public Token get(int index) {
            reads++;
            return values.get(index);
        }

        @Override
        public int size() {
            return values.size();
        }
    }
}
