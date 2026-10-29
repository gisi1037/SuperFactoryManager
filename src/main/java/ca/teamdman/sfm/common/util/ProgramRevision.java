package ca.teamdman.sfm.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import ca.teamdman.langs.SFMLLexer;

/** Small compare-and-save token; source length does not enlarge a guarded save packet. */
public final class ProgramRevision {

    private ProgramRevision() {}

    public static byte[] digest(String source) {
        try {
            return MessageDigest.getInstance("SHA-256")
                .digest(source.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean matches(byte[] expected, String source) {
        return expected != null && expected.length == 32 && MessageDigest.isEqual(expected, digest(source));
    }

    /** Cold save-boundary comparison only; comments do not replace the live executable or its cursors. */
    public static boolean sameExecutableTokens(String previous, String next) {
        if (previous == null || next == null) return false;
        final boolean[] invalid = { false };
        BaseErrorListener errors = new BaseErrorListener() {

            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object symbol, int line, int column, String message,
                RecognitionException error) {
                invalid[0] = true;
            }
        };
        SFMLLexer left = new SFMLLexer(CharStreams.fromString(previous));
        SFMLLexer right = new SFMLLexer(CharStreams.fromString(next));
        left.removeErrorListeners();
        right.removeErrorListeners();
        left.addErrorListener(errors);
        right.addErrorListener(errors);
        while (true) {
            Token a = nextExecutable(left), b = nextExecutable(right);
            if (invalid[0] || a.getType() != b.getType()
                || !a.getText()
                    .equals(b.getText()))
                return false;
            if (a.getType() == Token.EOF) return true;
        }
    }

    private static Token nextExecutable(SFMLLexer lexer) {
        Token token;
        do {
            token = lexer.nextToken();
        } while (token.getType() != Token.EOF && token.getChannel() != Token.DEFAULT_CHANNEL);
        return token;
    }
}
