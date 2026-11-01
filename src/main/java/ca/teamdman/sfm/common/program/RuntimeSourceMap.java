package ca.teamdman.sfm.common.program;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.sfml.data.SourceLocation;

/** Manager-owned cold-save coordinates; formatting edits never replace the executable or sleeping frames. */
public final class RuntimeSourceMap {

    private final long[] original;
    private final int[] currentLines, currentColumns;

    private RuntimeSourceMap(long[] original, int[] currentLines, int[] currentColumns) {
        this.original = original;
        this.currentLines = currentLines;
        this.currentColumns = currentColumns;
    }

    /** Align executable tokens, retaining the first compiled coordinates across any number of saves. */
    public static RuntimeSourceMap remapForSave(RuntimeSourceMap previousMap, String oldSource, String newSource) {
        List<Token> before = executable(oldSource), after = executable(newSource);
        if (before.size() != after.size() || previousMap != null && previousMap.original.length != before.size())
            throw new IllegalArgumentException("Runtime source mapping requires matching executable tokens");
        long[] original = previousMap == null ? new long[before.size()] : previousMap.original;
        int[] lines = new int[after.size()], columns = new int[after.size()];
        for (int i = 0; i < before.size(); i++) {
            Token oldToken = before.get(i), newToken = after.get(i);
            if (oldToken.getType() != newToken.getType() || !oldToken.getText()
                .equals(newToken.getText()))
                throw new IllegalArgumentException("Runtime source mapping requires matching executable tokens");
            if (previousMap == null) original[i] = key(oldToken.getLine(), oldToken.getCharPositionInLine());
            else if (previousMap.currentLines[i] != oldToken.getLine()
                || previousMap.currentColumns[i] != oldToken.getCharPositionInLine())
                throw new IllegalArgumentException("Runtime source mapping does not match the installed source");
            lines[i] = newToken.getLine();
            columns[i] = newToken.getCharPositionInLine();
        }
        return new RuntimeSourceMap(original, lines, columns);
    }

    /** Failure path only. An annotation outside the token map retains its original location. */
    public SourceLocation map(SourceLocation compiledLocation) {
        if (compiledLocation == null || compiledLocation.isModuleLocation()) return compiledLocation;
        int index = Arrays.binarySearch(original, key(compiledLocation.getLine(), compiledLocation.getColumn()));
        if (index < 0 || currentLines[index] == compiledLocation.getLine()
            && currentColumns[index] == compiledLocation.getColumn()) return compiledLocation;
        return new SourceLocation(currentLines[index], currentColumns[index], compiledLocation.getStatementKind());
    }

    private static long key(int line, int column) {
        return (long) line << 32 | column & 0xffffffffL;
    }

    private static List<Token> executable(String source) {
        if (source == null) throw new IllegalArgumentException("Runtime source mapping requires saved source");
        final boolean[] invalid = { false };
        SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(source));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new BaseErrorListener() {

            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object symbol, int line, int column, String message,
                RecognitionException error) {
                invalid[0] = true;
            }
        });
        List<Token> tokens = new ArrayList<>();
        while (true) {
            Token token = lexer.nextToken();
            if (invalid[0]) throw new IllegalArgumentException("Runtime source mapping requires valid source tokens");
            if (token.getType() == Token.EOF) return tokens;
            if (token.getChannel() == Token.DEFAULT_CHANNEL) tokens.add(token);
        }
    }
}
