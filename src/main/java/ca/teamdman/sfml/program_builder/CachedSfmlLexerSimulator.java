package ca.teamdman.sfml.program_builder;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.atn.ATNState;
import org.antlr.v4.runtime.atn.LexerATNConfig;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredicateTransition;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.atn.Transition;
import org.antlr.v4.runtime.dfa.DFA;

/**
 * SFML's UNUSED guard is constant for one token match. Keeping its two policies in separate DFA arrays lets ANTLR
 * cache the start closure instead of rebuilding it for every token. Other predicates retain ANTLR's normal behavior.
 * This caches lexer automata only: no input, tokens, source, AST or mutable execution Program is retained.
 */
public class CachedSfmlLexerSimulator extends LexerATNSimulator {

    private final boolean includeUnused;
    private final PredicateTransition unusedGuard;

    public CachedSfmlLexerSimulator(Lexer lexer, ATN atn, DFA[] decisions, PredictionContextCache contexts,
        boolean includeUnused, PredicateTransition unusedGuard) {
        super(lexer, atn, decisions, contexts);
        this.includeUnused = includeUnused;
        this.unusedGuard = unusedGuard;
    }

    public static DFA[] createDecisionDfas(ATN atn) {
        DFA[] decisions = new DFA[atn.getNumberOfDecisions()];
        for (int i = 0; i < decisions.length; i++) decisions[i] = new DFA(atn.getDecisionState(i), i);
        return decisions;
    }

    /**
     * Find precisely the grammar's single {INCLUDE_UNUSED}? guard. Unexpected rule/predicate shape falls back to
     * ordinary ANTLR evaluation. Keep this specialization in step with the UNUSED rule when editing the grammar.
     */
    public static PredicateTransition findUnusedGuard(ATN atn, int unusedTokenType) {
        PredicateTransition found = null;
        for (ATNState state : atn.states) {
            if (state == null) continue;
            for (int i = 0; i < state.getNumberOfTransitions(); i++) {
                Transition transition = state.transition(i);
                if (!(transition instanceof PredicateTransition)) continue;
                PredicateTransition predicate = (PredicateTransition) transition;
                if (atn.ruleToTokenType[predicate.ruleIndex] != unusedTokenType) continue;
                if (found != null || predicate.predIndex != 0 || predicate.isCtxDependent) return null;
                found = predicate;
            }
        }
        return found;
    }

    @Override
    protected LexerATNConfig getEpsilonTarget(CharStream input, LexerATNConfig config, Transition transition,
        ATNConfigSet configs, boolean speculative, boolean treatEofAsEpsilon) {
        if (transition == unusedGuard) {
            // This exact predicate is constant in this simulator. Do not mark its closure semantic, and never clear
            // hasSemanticContext: a different predicate may already have made the closure input-dependent.
            return includeUnused ? new LexerATNConfig(config, transition.target) : null;
        }
        return super.getEpsilonTarget(input, config, transition, configs, speculative, treatEofAsEpsilon);
    }
}
