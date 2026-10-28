package ca.teamdman.sfml.program_builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.langs.SFMLParser;
import ca.teamdman.sfml.ast.ASTBuilder;
import ca.teamdman.sfml.ast.Program;

public final class ProgramBuilder {

    private final String source;

    public ProgramBuilder(String source) {
        this.source = source != null ? source : "";
    }

    public ProgramBuildResult build() {
        if (source.trim()
            .isEmpty()) {
            return ProgramBuildResult.failure(Collections.singletonList("Program is empty"));
        }

        if (source.length() > 32767)
            return ProgramBuildResult.failure(Collections.singletonList("Program exceeds 32767 characters"));
        List<String> errors = new ArrayList<>();
        BaseErrorListener errorListener = new BaseErrorListener() {

            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                int charPositionInLine, String msg, RecognitionException e) {
                errors.add("Line " + line + ", Col " + charPositionInLine + ": " + msg);
            }
        };

        try {
            SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(source));
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);

            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();
            int nesting = 0;
            int unary = 0;
            int dataNesting = 0, dataUnary = 0;
            boolean hasData = false;
            for (org.antlr.v4.runtime.Token token : tokens.getTokens()) {
                if (token.getType() == SFMLLexer.VARIABLE || token.getType() == SFMLLexer.LBRACKET
                    || token.getType() == SFMLLexer.LBRACE) hasData = true;
            }
            for (org.antlr.v4.runtime.Token token : tokens.getTokens()) {
                int type = token.getType();
                if (token.getChannel() != org.antlr.v4.runtime.Token.DEFAULT_CHANNEL) continue;
                if (type == SFMLLexer.DO || type == SFMLLexer.IF || type == SFMLLexer.LPAREN) nesting++;
                if (type == SFMLLexer.END || type == SFMLLexer.RPAREN) nesting--;
                unary = type == SFMLLexer.NOT ? unary + 1 : 0;
                if (hasData) {
                    if (type == SFMLLexer.LPAREN || type == SFMLLexer.LBRACKET || type == SFMLLexer.LBRACE)
                        dataNesting++;
                    if (type == SFMLLexer.RPAREN || type == SFMLLexer.RBRACKET || type == SFMLLexer.RBRACE)
                        dataNesting--;
                    dataUnary = type == SFMLLexer.NOT || type == SFMLLexer.PLUS || type == SFMLLexer.DASH
                        ? dataUnary + 1
                        : 0;
                    if (dataNesting > 32 || dataUnary > 32) return ProgramBuildResult
                        .failure(Collections.singletonList("Data expression nesting exceeds 32"));
                }
                if (nesting > 128 || unary > 128)
                    return ProgramBuildResult.failure(Collections.singletonList("Program nesting exceeds 128"));
            }
            SFMLParser parser = new SFMLParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            SFMLParser.ProgramContext tree = parser.program();

            if (!errors.isEmpty()) {
                return ProgramBuildResult.failure(errors);
            }

            ASTBuilder builder = new ASTBuilder();
            Program program = (Program) builder.visit(tree);
            return ProgramBuildResult.success(program);
        } catch (StackOverflowError e) {
            return ProgramBuildResult.failure(Collections.singletonList("Program expression is too complex"));
        } catch (Exception e) {
            errors.add("Compilation failed: " + e.getMessage());
            return ProgramBuildResult.failure(errors);
        }
    }
}
