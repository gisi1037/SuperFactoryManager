package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.atn.ATNState;
import org.antlr.v4.runtime.atn.BasicState;
import org.antlr.v4.runtime.atn.EmptyPredictionContext;
import org.antlr.v4.runtime.atn.LexerATNConfig;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredicateTransition;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.dfa.DFAState;
import org.junit.jupiter.api.Test;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.sfml.program_builder.CachedSfmlLexerSimulator;

class SfmlLexerCacheTest {

    @Test
    void strictAndTolerantTokensAndErrorsMatchOriginalOnEdgeCases() {
        String[] sources = { "", "\n\r\n\t ", "EVERY 20 TICKS DO END", "eVeRy 20g PLUS 4 tIcKs Do EnD",
            "INPUT *:* FROM a OUTPUT minecraft:iron_* TO EACH b", "-- comment \" @ $\n@EVERY $x = 2",
            "\"closed\" \"escaped <BS>\" quote\" \"paired <BS><BS>\" -- end\r\nEND".replace("<BS>", "\\"),
            "\"unterminated", "\"unterminated\\", "\"bad\nstring", "@\u0000\u2603\ud83d\ude00\ud800x\udc00",
            "$name $ $a9 $x_ \"\" -- trailing comment", "123g 123G 3.14 << >> >>> != ! || | && & # / : * **",
            "FUNCTION f($x: INTEGER) RETURNS INTEGER DO RETURN $x END\nEVERY 1 TICKS DO LET $y = f(4) END" };
        for (String source : sources) {
            compare(source, false, false);
            compare(source, true, false);
            compare(source, false, true);
        }
    }

    @Test
    void seededMalformedUnicodeAndFlagFlipsMatchOriginal() {
        Random random = new Random(1037);
        String[] pieces = { "EVERY", "eNd", "  ", "\r\n", "\t", "$x", "*", "minecraft:iron_*", "42G",
            "-- ignored @\"\n", "\"abc\\\"def\"", "\"unterminated", "@", "!", "&", "|", ":", "#", "\u0000", "\u00e9",
            "\u2603", "\ud83d\ude00", "\ud800", "\udc00", "\\", "\"", "END\n" };
        for (int sample = 0; sample < 250; sample++) {
            StringBuilder source = new StringBuilder();
            int count = 1 + random.nextInt(100);
            for (int i = 0; i < count; i++) {
                source.append(pieces[random.nextInt(pieces.length)]);
                if (random.nextInt(8) == 0) source.append((char) random.nextInt(65536));
            }
            compare(source.toString(), false, false);
            compare(source.toString(), true, false);
            compare(source.toString(), sample % 2 == 0, true);
        }
    }

    @Test
    void independentStrictAndTolerantInstancesCanInterleaveAndReset() {
        Pair[] pairs = new Pair[8];
        for (int i = 0; i < pairs.length; i++) pairs[i] = new Pair("@ EVERY 20 TICKS\n\"unterminated " + i + "\nEND");
        for (int step = 0; step < 100; step++) {
            for (int i = 0; i < pairs.length; i++) {
                Pair pair = pairs[i];
                if (!pair.eof) pair.next((step + i) % 3 == 0);
            }
        }
        for (Pair pair : pairs) assertTrue(pair.eof);
        for (int i = 0; i < pairs.length; i++) {
            Pair pair = pairs[i];
            pair.reset("\n\n @ \"new\\\"text\" -- tail\r\nEVERY 3 TICKS DO END");
            for (int step = 0; !pair.eof; step++) pair.next((step + i) % 2 == 0);
        }
    }

    @Test
    void switchingPolicyCopiesManuallyChangedLineAndColumnAndResetState() {
        Pair pair = new Pair("A @ B @ C");
        pair.next(false);
        pair.optimized.setLine(41);
        pair.original.setLine(41);
        pair.optimized.setCharPositionInLine(17);
        pair.original.setCharPositionInLine(17);
        for (int step = 0; !pair.eof; step++) pair.next(step % 2 == 0);
        pair.optimized.reset();
        pair.original.reset();
        pair.eof = false;
        pair.optimizedErrors.clear();
        pair.originalErrors.clear();
        for (int step = 0; !pair.eof; step++) pair.next(step % 2 != 0);
    }

    @Test
    void startClosureIsCachedForEachPolicyInsteadOfRebuiltPerToken() {
        StringBuilder source = new StringBuilder();
        for (int i = 0; i < 300; i++) source.append("EVERY 20 TICKS DO END\n");
        for (boolean tolerant : new boolean[] { false, true }) {
            MeasuredLexer optimized = new MeasuredLexer(source.toString(), true);
            MeasuredLexer original = new MeasuredLexer(source.toString(), false);
            int tokens = consume(optimized, tolerant);
            assertEquals(tokens, consume(original, tolerant));
            assertEquals(1, optimized.work.starts, "Policy-specific start DFA was not reused");
            assertTrue(original.work.starts > 1000, "Oracle did not exercise the semantic-predicate start-cache miss");
            assertTrue(optimized.work.targets < tokens, "Repeated token paths did not use cached DFA edges");
            assertTrue(original.work.predicates > 1000);
            assertEquals(0, optimized.work.predicates, "Only constant UNUSED guard should be specialized");
        }
    }

    @Test
    void policyDfasStaySeparateAndEachStartClosureIsBuiltOnceWhenFlagChanges() {
        MeasuredLexer lexer = new MeasuredLexer("@ A @ B @ C @ D @ E @ F", true);
        int step = 0;
        Token token;
        do {
            lexer.INCLUDE_UNUSED = step++ % 2 == 0;
            token = lexer.nextToken();
        } while (token.getType() != Token.EOF);
        assertEquals(2, lexer.work.starts);
        assertNotSame(lexer.decisions[0], lexer.decisions[1]);
        assertNotSame(lexer.decisions[0][0], lexer.decisions[1][0]);
        assertNotNull(lexer.decisions[0][0].s0);
        assertNotNull(lexer.decisions[1][0].s0);
    }

    @Test
    void specializationPreservesOtherPredicatesAndExistingSemanticContext() {
        SFMLLexer lexer = new SFMLLexer(CharStreams.fromString("x"));
        PredicateTransition guard = CachedSfmlLexerSimulator.findUnusedGuard(lexer.getATN(), SFMLLexer.UNUSED);
        assertNotNull(guard);
        class Probe extends CachedSfmlLexerSimulator {

            Probe() {
                super(
                    lexer,
                    lexer.getATN(),
                    createDecisionDfas(lexer.getATN()),
                    new PredictionContextCache(),
                    true,
                    guard);
            }

            LexerATNConfig follow(PredicateTransition transition, ATNConfigSet configs) {
                return getEpsilonTarget(
                    lexer.getInputStream(),
                    new LexerATNConfig(new BasicState(), 1, EmptyPredictionContext.Instance),
                    transition,
                    configs,
                    false,
                    false);
            }
        }
        Probe probe = new Probe();
        ATNConfigSet semantic = new ATNConfigSet();
        semantic.hasSemanticContext = true;
        assertNotNull(probe.follow(guard, semantic));
        assertTrue(semantic.hasSemanticContext, "Constant specialization cleared another predicate's semantic marker");
        ATNConfigSet unknown = new ATNConfigSet();
        assertNotNull(probe.follow(new PredicateTransition(new BasicState(), 0, 99, false), unknown));
        assertTrue(
            unknown.hasSemanticContext,
            "Unknown predicates must retain original ANTLR evaluation and cache fences");
    }

    private static int consume(SFMLLexer lexer, boolean tolerant) {
        lexer.removeErrorListeners();
        lexer.INCLUDE_UNUSED = tolerant;
        int tokens = 0;
        while (lexer.nextToken()
            .getType() != Token.EOF) tokens++;
        return tokens;
    }

    private static void compare(String source, boolean tolerant, boolean flip) {
        Pair pair = new Pair(source);
        for (int step = 0; !pair.eof; step++) {
            assertTrue(step <= source.length() + 2, "Lexer did not make progress");
            pair.next(flip ? (step % 3 == 0) != tolerant : tolerant);
        }
    }

    private static final class Pair {

        final SFMLLexer optimized;
        final OriginalLexer original;
        final List<List<Object>> optimizedErrors = new ArrayList<>(), originalErrors = new ArrayList<>();
        boolean eof;

        Pair(String source) {
            optimized = new SFMLLexer(CharStreams.fromString(source));
            original = new OriginalLexer(source);
            optimized.removeErrorListeners();
            original.removeErrorListeners();
            optimized.addErrorListener(errors(optimizedErrors));
            original.addErrorListener(errors(originalErrors));
        }

        void next(boolean tolerant) {
            optimized.INCLUDE_UNUSED = tolerant;
            original.INCLUDE_UNUSED = tolerant;
            Token actual = optimized.nextToken(), expected = original.nextToken();
            assertEquals(token(expected), token(actual), "Token or source position differs for policy=" + tolerant);
            assertEquals(originalErrors, optimizedErrors, "Lexer diagnostics differ for policy=" + tolerant);
            assertEquals(original.getLine(), optimized.getLine());
            assertEquals(original.getCharPositionInLine(), optimized.getCharPositionInLine());
            eof = actual.getType() == Token.EOF;
        }

        void reset(String source) {
            optimized.setInputStream(CharStreams.fromString(source));
            original.setInputStream(CharStreams.fromString(source));
            optimizedErrors.clear();
            originalErrors.clear();
            eof = false;
        }
    }

    private static List<Object> token(Token token) {
        return Arrays.asList(
            token.getType(),
            token.getChannel(),
            token.getStartIndex(),
            token.getStopIndex(),
            token.getLine(),
            token.getCharPositionInLine(),
            token.getText());
    }

    private static BaseErrorListener errors(List<List<Object>> errors) {
        return new BaseErrorListener() {

            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int column,
                String message, RecognitionException error) {
                errors.add(
                    Arrays.asList(
                        line,
                        column,
                        message,
                        error == null ? null
                            : error.getClass()
                                .getName(),
                        error instanceof LexerNoViableAltException ? ((LexerNoViableAltException) error).getStartIndex()
                            : null));
            }
        };
    }

    /** The generated factory hook deliberately uses wholly independent, unoptimized DFA arrays for the oracle. */
    private static class OriginalLexer extends SFMLLexer {

        private LexerATNSimulator originalSimulator;

        OriginalLexer(String source) {
            super(CharStreams.fromString(source));
        }

        @Override
        protected LexerATNSimulator createLexerSimulator(boolean includeUnused) {
            // Original ANTLR consults the live predicate on one simulator. Reuse it across flag changes, so the
            // oracle independently detects lost line/column/mode state in the optimized simulator-swap path.
            if (originalSimulator == null) originalSimulator = new LexerATNSimulator(
                this,
                getATN(),
                CachedSfmlLexerSimulator.createDecisionDfas(getATN()),
                new PredictionContextCache());
            return originalSimulator;
        }
    }

    private static final class Work {

        int starts, targets, predicates;
    }

    private static final class MeasuredLexer extends SFMLLexer {

        final boolean optimized;
        final Work work = new Work();
        final DFA[][] decisions = new DFA[2][];

        MeasuredLexer(String source, boolean optimized) {
            super(CharStreams.fromString(source));
            this.optimized = optimized;
        }

        @Override
        protected LexerATNSimulator createLexerSimulator(boolean includeUnused) {
            DFA[] dfa = CachedSfmlLexerSimulator.createDecisionDfas(getATN());
            decisions[includeUnused ? 1 : 0] = dfa;
            if (optimized) return new CachedSfmlLexerSimulator(
                this,
                getATN(),
                dfa,
                new PredictionContextCache(),
                includeUnused,
                CachedSfmlLexerSimulator.findUnusedGuard(getATN(), UNUSED)) {

                @Override
                protected ATNConfigSet computeStartState(CharStream input, ATNState state) {
                    work.starts++;
                    return super.computeStartState(input, state);
                }

                @Override
                protected DFAState computeTargetState(CharStream input, DFAState state, int symbol) {
                    work.targets++;
                    return super.computeTargetState(input, state, symbol);
                }

                @Override
                protected boolean evaluatePredicate(CharStream input, int rule, int predicate, boolean speculative) {
                    work.predicates++;
                    return super.evaluatePredicate(input, rule, predicate, speculative);
                }
            };
            return new LexerATNSimulator(this, getATN(), dfa, new PredictionContextCache()) {

                @Override
                protected ATNConfigSet computeStartState(CharStream input, ATNState state) {
                    work.starts++;
                    return super.computeStartState(input, state);
                }

                @Override
                protected DFAState computeTargetState(CharStream input, DFAState state, int symbol) {
                    work.targets++;
                    return super.computeTargetState(input, state, symbol);
                }

                @Override
                protected boolean evaluatePredicate(CharStream input, int rule, int predicate, boolean speculative) {
                    work.predicates++;
                    return super.evaluatePredicate(input, rule, predicate, speculative);
                }
            };
        }
    }
}
