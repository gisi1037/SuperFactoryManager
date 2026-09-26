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
        } catch (Exception e) {
            errors.add("Compilation failed: " + e.getMessage());
            return ProgramBuildResult.failure(errors);
        }
    }
}
