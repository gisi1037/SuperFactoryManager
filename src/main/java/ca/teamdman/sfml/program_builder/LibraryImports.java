package ca.teamdman.sfml.program_builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.langs.SFMLParser;
import ca.teamdman.sfm.common.program.project.LibraryImport;
import ca.teamdman.sfml.data.DataCompiler;

/** Exact imports read from bounded grammar contexts, never a source-text search or rewrite. */
public final class LibraryImports {

    private LibraryImports() {}

    public static List<LibraryImport> parse(String source) {
        if (!hasImports(source)) return Collections.emptyList();
        ProgramBuildResult syntax = ProgramBuilder.parseSyntax(source);
        if (!syntax.isSuccess()) throw new IllegalArgumentException(syntax.getErrorMessage());
        return fromSyntax(syntax.getSyntax());
    }

    /** Header-only lexer probe. Invalid headers fall through to the normal compiler diagnostics. */
    public static boolean hasImports(String source) {
        if (source == null || source.length() > 32767) return false;
        final boolean[] invalid = { false };
        SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(source));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new BaseErrorListener() {

            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object symbol, int line, int column, String message,
                RecognitionException cause) {
                invalid[0] = true;
            }
        });
        Token first = next(lexer);
        if (first.getType() == SFMLLexer.NAME) {
            if (next(lexer).getType() != SFMLLexer.STRING) return false;
            first = next(lexer);
        }
        return !invalid[0] && first.getType() == SFMLLexer.IMPORT;
    }

    private static Token next(SFMLLexer lexer) {
        Token token;
        do {
            token = lexer.nextToken();
        } while (token.getType() != Token.EOF && token.getChannel() != Token.DEFAULT_CHANNEL);
        return token;
    }

    static List<LibraryImport> fromSyntax(SFMLParser.ProgramContext tree) {
        List<LibraryImport> imports = new ArrayList<>();
        Set<String> aliases = new HashSet<>(), references = new HashSet<>();
        for (SFMLParser.ImportDeclarationContext declaration : tree.importDeclaration()) {
            try {
                String alias = declaration.identifier()
                    .getText();
                String projectText = DataCompiler.decodeStringLiteral(
                    declaration.string(0)
                        .getText());
                UUID project = UUID.fromString(projectText);
                if (!project.toString()
                    .equalsIgnoreCase(projectText)) throw new IllegalArgumentException();
                long revision = Long.parseLong(
                    declaration.NUMBER()
                        .getText());
                String digest = DataCompiler.decodeStringLiteral(
                    declaration.string(1)
                        .getText());
                LibraryImport reference = new LibraryImport(alias, project, revision, digest);
                if (!aliases.add(alias)) throw new IllegalArgumentException("Duplicate library alias");
                if (!references.add(project + "/" + revision))
                    throw new IllegalArgumentException("Duplicate library revision");
                if (imports.size() >= 8) throw new IllegalArgumentException("Library imports exceed 8");
                imports.add(reference);
            } catch (IllegalArgumentException invalid) {
                throw new IllegalArgumentException(
                    "Line " + declaration.getStart()
                        .getLine()
                        + ", Col "
                        + declaration.getStart()
                            .getCharPositionInLine()
                        + ": Invalid or duplicate exact library import");
            }
        }
        return Collections.unmodifiableList(imports);
    }
}
