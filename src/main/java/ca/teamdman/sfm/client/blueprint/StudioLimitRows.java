package ca.teamdman.sfm.client.blueprint;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.langs.SFMLParser;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;

/** Pending full independent-limit clauses. Commas are taken from parsed boundaries, never split as text. */
public final class StudioLimitRows {

    public static final int MAX_ROWS = 32;

    public static final class Row {

        public final UUID id;
        public final String text;

        private Row(UUID id, String text) {
            this.id = id;
            this.text = text;
        }
    }

    private final List<Row> rows = new ArrayList<>();
    private final List<String> separators = new ArrayList<>();
    private UUID revision = UUID.randomUUID();

    public StudioLimitRows(String source) {
        SFMLParser.ResourceLimitListContext parsed = parse(source);
        int previousEnd = -1;
        for (SFMLParser.ResourceLimitContext limit : parsed.resourceLimit()) {
            int start = source.offsetByCodePoints(
                0,
                limit.getStart()
                    .getStartIndex());
            int end = source.offsetByCodePoints(
                0,
                limit.getStop()
                    .getStopIndex() + 1);
            if (previousEnd >= 0) separators.add(source.substring(previousEnd, start));
            rows.add(new Row(UUID.randomUUID(), source.substring(start, end)));
            previousEnd = end;
        }
    }

    public StudioLimitRows(StudioLimitRows other) {
        rows.addAll(other.rows); // Immutable snapshots; editing replaces a row instead of sharing mutations.
        separators.addAll(other.separators);
        revision = other.revision;
    }

    public UUID revision() {
        return revision;
    }

    public int size() {
        return rows.size();
    }

    public List<Row> rows() {
        return Collections.unmodifiableList(new ArrayList<>(rows));
    }

    public String value(UUID row) {
        return rows.get(index(row)).text;
    }

    /** Joined source alone can conceal an invalid split inside a quoted comma. */
    public boolean sameContent(StudioLimitRows other) {
        if (other == null || rows.size() != other.rows.size() || !separators.equals(other.separators)) return false;
        for (int i = 0; i < rows.size(); i++) if (!rows.get(i).text.equals(other.rows.get(i).text)) return false;
        return true;
    }

    public String source() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) result.append(separators.get(i - 1));
            result.append(rows.get(i).text);
        }
        return result.toString();
    }

    /** Keep invalid text locally for correction. Each cell is validated before Apply touches source. */
    public boolean set(UUID row, String value) {
        int index = index(row);
        String previous = rows.get(index).text;
        if (value == null || (long) source().length() - previous.length() + value.length() > BlueprintGraph.MAX_CLAUSE)
            throw new IllegalArgumentException("Limit list exceeds 4096 characters");
        if (previous.equals(value)) return false;
        rows.set(index, new Row(row, value));
        return true;
    }

    public UUID append(String value) {
        if (rows.size() >= MAX_ROWS) throw new IllegalArgumentException("Keep at most 32 independent limit rows");
        if (value == null || (long) source().length() + 2 + value.length() > BlueprintGraph.MAX_CLAUSE)
            throw new IllegalArgumentException("Limit list exceeds 4096 characters");
        Row row = new Row(UUID.randomUUID(), value);
        rows.add(row);
        separators.add(", ");
        revision = UUID.randomUUID();
        return row.id;
    }

    public void remove(UUID row) {
        int index = index(row);
        if (rows.size() == 1) throw new IllegalArgumentException("Keep at least one independent limit");
        separators.remove(index == rows.size() - 1 ? index - 1 : index);
        rows.remove(index);
        revision = UUID.randomUUID();
    }

    private int index(UUID row) {
        for (int i = 0; i < rows.size(); i++) if (rows.get(i).id.equals(row)) return i;
        throw new IllegalArgumentException("Limit row changed. Reopen Fields.");
    }

    public static boolean supports(String source) {
        try {
            parse(source);
            return true;
        } catch (IllegalArgumentException invalid) {
            return false;
        }
    }

    public static void validateRow(Type type, String source) {
        if (type != Type.INPUT && type != Type.OUTPUT) throw new IllegalArgumentException("Limits belong to IO nodes");
        if (parse(source).resourceLimit()
            .size() != 1)
            throw new IllegalArgumentException("Enter one complete limit per row; use Add limit for another");
        // The complete edited IO clause is compiled after joining all rows, not once per cell or frame.
    }

    public static SFMLParser.ResourceLimitListContext parse(String source) {
        CommonTokenStream tokens = safeTokens(source);
        try {
            SFMLParser parser = new SFMLParser(tokens);
            parser.removeErrorListeners();
            parser.setErrorHandler(new BailErrorStrategy());
            SFMLParser.ResourceLimitListContext parsed = parser.resourceLimitList();
            if (parser.getCurrentToken()
                .getType() != Token.EOF || parsed.resourceLimit()
                    .isEmpty()
                || parsed.resourceLimit()
                    .size() > MAX_ROWS
                || parsed.COMMA()
                    .size()
                    != parsed.resourceLimit()
                        .size() - 1
                || hasExpression(parsed))
                throw new IllegalArgumentException("Use 1–32 complete static limits without a trailing comma");
            return parsed;
        } catch (ParseCancellationException invalid) {
            throw new IllegalArgumentException("Enter one complete SFML limit; blank rows block Apply", invalid);
        }
    }

    /** Conversion checks pending text before any full-clause or typed-field recursive parser sees it. */
    public static void requireSafeSyntax(String source) {
        safeTokens(source);
    }

    private static CommonTokenStream safeTokens(String source) {
        if (source == null || source.length() > BlueprintGraph.MAX_CLAUSE
            || source.indexOf('\n') >= 0
            || source.indexOf('\r') >= 0
            || source.indexOf('\0') >= 0) throw new IllegalArgumentException("Limits must be one bounded line");
        try {
            SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(source));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new BaseErrorListener() {

                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object symbol, int line, int column,
                    String message, RecognitionException error) {
                    throw new ParseCancellationException(message);
                }
            });
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();
            guardSyntax(tokens);
            return tokens;
        } catch (ParseCancellationException invalid) {
            throw new IllegalArgumentException("Enter one complete SFML limit; blank rows block Apply", invalid);
        }
    }

    private static boolean hasExpression(ParseTree tree) {
        ArrayDeque<ParseTree> pending = new ArrayDeque<>();
        pending.add(tree);
        while (!pending.isEmpty()) {
            ParseTree next = pending.removeLast();
            if (next instanceof SFMLParser.DataExpressionContext) return true;
            for (int i = 0; i < next.getChildCount(); i++) pending.add(next.getChild(i));
        }
        return false;
    }

    /** Bound parser recursion before it starts. STRING contents are literals, not nested syntax. */
    private static void guardSyntax(CommonTokenStream tokens) {
        int depth = 0, filterOperators = 0, previous = Token.EOF;
        boolean filter = false;
        for (Token token : tokens.getTokens()) {
            int kind = token.getType();
            if (kind == SFMLLexer.LINE_COMMENT) throw new IllegalArgumentException("Comments are not limit rows");
            if (token.getChannel() != Token.DEFAULT_CHANNEL) continue;
            if (kind == SFMLLexer.LPAREN) {
                if (previous == Token.EOF || previous == SFMLLexer.COMMA || previous == SFMLLexer.RETAIN)
                    throw new IllegalArgumentException("Computed limits require Text");
                if (++depth > 32) throw new IllegalArgumentException("Limit row syntax is too deeply nested");
            } else if (kind == SFMLLexer.RPAREN && --depth < 0)
                throw new IllegalArgumentException("Unbalanced limit row parentheses");
            if (kind == SFMLLexer.COMMA && depth == 0) {
                filter = false;
                filterOperators = 0;
            } else if (kind == SFMLLexer.WITH || kind == SFMLLexer.WITHOUT) filter = true;
            else if (filter && (kind == SFMLLexer.NOT || kind == SFMLLexer.AND || kind == SFMLLexer.OR)
                && ++filterOperators > 32) throw new IllegalArgumentException("Limit row filter syntax is too complex");
            previous = kind;
        }
        if (depth != 0) throw new IllegalArgumentException("Unbalanced limit row parentheses");
    }
}
