package ca.teamdman.sfm.client.gui.syntax;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.antlr.v4.runtime.Token;

import ca.teamdman.langs.SFMLLexer;

/** Best-effort delimiter feedback while editing, built from the existing color token stream. */
public final class SfmlBlockMatches {

    public static final class Pair {

        public final int start, end, closeStart, closeEnd;

        Pair(Token open, Token close, int[] offsets) {
            start = offsets[open.getStartIndex()];
            end = offsets[open.getStopIndex() + 1];
            closeStart = offsets[close.getStartIndex()];
            closeEnd = offsets[close.getStopIndex() + 1];
        }

        public boolean touches(int caret) {
            return (caret >= start && caret <= end) || (caret >= closeStart && caret <= closeEnd);
        }
    }

    private SfmlBlockMatches() {}

    static List<Pair> collect(List<? extends Token> tokens, int[] offsets) {
        List<Pair> pairs = new ArrayList<>();
        Deque<Token> opens = new ArrayDeque<>();
        boolean branch = false;
        int previous = -1;
        for (Token token : tokens) {
            if (token.getChannel() != Token.DEFAULT_CHANNEL) continue;
            int type = token.getType();
            // Keywords also occur as member names. Strings/comments are already separate lexer tokens.
            if (previous != SFMLLexer.DOT) {
                if (type == SFMLLexer.ELSE) branch = true;
                else if (type == SFMLLexer.THEN && branch) branch = false;
                else if (type == SFMLLexer.DO || type == SFMLLexer.THEN || type == SFMLLexer.LBRACE) {
                    opens.push(token);
                    branch = false;
                } else if (type == SFMLLexer.END || type == SFMLLexer.RBRACE) {
                    if (!opens.isEmpty() && (opens.peek()
                        .getType() == SFMLLexer.LBRACE) == (type == SFMLLexer.RBRACE)) {
                        pairs.add(new Pair(opens.pop(), token, offsets));
                    }
                    branch = false;
                } else if (previous == SFMLLexer.ELSE && type != SFMLLexer.IF) branch = false;
            }
            previous = type;
        }
        return pairs;
    }
}
