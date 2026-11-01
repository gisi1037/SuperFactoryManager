package ca.teamdman.sfm.client.gui.mui2;

import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.sfm.common.program.project.LibraryImport;
import ca.teamdman.sfm.common.program.project.ProjectWorkspace;
import ca.teamdman.sfm.common.program.project.PublicationReceipt;
import ca.teamdman.sfm.common.util.BoundedUtf8;
import ca.teamdman.sfml.program_builder.LibraryImports;

/** Prepares a bounded insertion only. The editor applies it as one undoable edit to the captured draft. */
public final class StudioLibraryImports {

    public static final class Insertion {

        public final int offset;
        public final String text;

        private Insertion(int offset, String text) {
            this.offset = offset;
            this.text = text;
        }
    }

    private StudioLibraryImports() {}

    public static Insertion prepare(String source, String alias, PublicationReceipt receipt) {
        if (source == null || receipt == null) throw new IllegalArgumentException("Select an exact library revision.");
        LibraryImport selected = new LibraryImport(alias, receipt.project, receipt.revision, receipt.digest);
        List<LibraryImport> existing = LibraryImports.parse(source);
        if (existing.size() >= 8) throw new IllegalArgumentException("A program can import at most eight libraries.");
        for (LibraryImport reference : existing) if (reference.alias.equals(selected.alias))
            throw new IllegalArgumentException("That library alias is already imported; choose another alias.");
        int offset = headerOffset(source);
        String declaration = "import " + selected.alias
            + " from \""
            + selected.project
            + "\" revision "
            + selected.revision
            + " digest \""
            + selected.digest
            + "\";\n";
        String text = (offset > 0 && source.charAt(offset - 1) != '\n' ? "\n" : "") + declaration;
        String candidate = source.substring(0, offset) + text + source.substring(offset);
        BoundedUtf8.encode(candidate, ProjectWorkspace.MAX_SOURCE_BYTES);
        LibraryImports.parse(candidate); // Duplicate references, malformed headers and parser limits stay
                                         // authoritative.
        return new Insertion(offset, text);
    }

    private static int headerOffset(String source) {
        SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(source));
        lexer.removeErrorListeners();
        Token first = visible(lexer);
        if (first.getType() == Token.EOF) return source.length();
        if (first.getType() != SFMLLexer.NAME) return source.offsetByCodePoints(0, first.getStartIndex());
        Token name = visible(lexer);
        if (name.getType() != SFMLLexer.STRING) throw new IllegalArgumentException("Finish the NAME header first.");
        int end = source.offsetByCodePoints(0, name.getStopIndex() + 1);
        Token next = visible(lexer);
        if (next.getType() == Token.EOF) return source.length();
        int nextOffset = source.offsetByCodePoints(0, next.getStartIndex());
        int newline = source.indexOf('\n', end);
        // Keep a NAME-line comment attached to NAME, while also supporting same-line declarations.
        return newline >= 0 && newline < nextOffset ? newline + 1 : end;
    }

    private static Token visible(SFMLLexer lexer) {
        Token token;
        do {
            token = lexer.nextToken();
        } while (token.getType() != Token.EOF && token.getChannel() != Token.DEFAULT_CHANNEL);
        return token;
    }
}
