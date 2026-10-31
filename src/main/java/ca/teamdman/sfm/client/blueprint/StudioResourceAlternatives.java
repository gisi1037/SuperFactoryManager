package ca.teamdman.sfm.client.blueprint;

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

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.langs.SFMLParser;

/**
 * Heap-only resource alternatives in one shared limit. Keep source lexemes and delimiters; never infer removal from a
 * blank cell.
 */
public final class StudioResourceAlternatives {

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

    public StudioResourceAlternatives(String source) {
        SFMLParser.ResourceIdDisjunctionContext parsed = parse(source);
        if (parsed == null) return;
        int previousEnd = -1;
        for (SFMLParser.ResourceIdContext resource : parsed.resourceId()) {
            int start = source.offsetByCodePoints(
                0,
                resource.getStart()
                    .getStartIndex());
            int end = source.offsetByCodePoints(
                0,
                resource.getStop()
                    .getStopIndex() + 1);
            if (previousEnd >= 0) separators.add(source.substring(previousEnd, start));
            rows.add(new Row(UUID.randomUUID(), source.substring(start, end)));
            previousEnd = end;
        }
    }

    public StudioResourceAlternatives(StudioResourceAlternatives other) {
        rows.addAll(other.rows); // Row snapshots are immutable; edits replace them.
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

    public boolean sameContent(StudioResourceAlternatives other) {
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

    /** Invalid text stays pending; Apply validates every cell, even if joined text looks unchanged. */
    public boolean set(UUID row, String value) {
        int index = index(row);
        String previous = rows.get(index).text;
        if (value == null || (long) source().length() - previous.length() + value.length() > BlueprintGraph.MAX_CLAUSE)
            throw new IllegalArgumentException("Alternatives exceed 4096 characters");
        if (previous.equals(value)) return false;
        rows.set(index, new Row(row, value));
        return true;
    }

    public UUID append(String value) {
        if (rows.size() >= MAX_ROWS)
            throw new IllegalArgumentException("Use the full clause for more than 32 alternatives");
        if (value == null
            || (long) source().length() + (rows.isEmpty() ? 0 : 4) + value.length() > BlueprintGraph.MAX_CLAUSE)
            throw new IllegalArgumentException("Alternatives exceed 4096 characters");
        Row row = new Row(UUID.randomUUID(), value);
        if (!rows.isEmpty()) separators.add(" OR ");
        rows.add(row);
        revision = UUID.randomUUID();
        return row.id;
    }

    /** Remove the following separator, or the preceding one for the final row. Zero rows restores match-all. */
    public void remove(UUID row) {
        int index = index(row);
        if (rows.size() > 1) separators.remove(index == rows.size() - 1 ? index - 1 : index);
        rows.remove(index);
        revision = UUID.randomUUID();
    }

    private int index(UUID row) {
        for (int i = 0; i < rows.size(); i++) if (rows.get(i).id.equals(row)) return i;
        throw new IllegalArgumentException("Alternative row changed. Reopen the picker.");
    }

    public static void validateLiteral(String value) {
        SFMLParser.ResourceIdDisjunctionContext parsed = parse(value);
        if (parsed == null || parsed.resourceId()
            .size() != 1)
            throw new IllegalArgumentException("Enter one resource per alternative row; use Remove for an empty row");
    }

    /** Empty represents no selector. Comments, lexer errors, trailing OR and oversize groups stay manual. */
    public static SFMLParser.ResourceIdDisjunctionContext parse(String value) {
        if (value == null || value.length() > BlueprintGraph.MAX_CLAUSE
            || value.indexOf('\n') >= 0
            || value.indexOf('\r') >= 0
            || value.indexOf('\0') >= 0) throw new IllegalArgumentException("Alternatives must be one bounded line");
        if (value.isEmpty()) return null;
        try {
            SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(value));
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
            for (Token token : tokens.getTokens()) if (token.getType() == SFMLLexer.LINE_COMMENT)
                throw new IllegalArgumentException("Comments are not alternative rows");
            SFMLParser parser = new SFMLParser(tokens);
            parser.removeErrorListeners();
            parser.setErrorHandler(new BailErrorStrategy());
            SFMLParser.ResourceIdDisjunctionContext parsed = parser.resourceIdDisjunction();
            if (parser.getCurrentToken()
                .getType() != Token.EOF
                || parsed.resourceId()
                    .size() > MAX_ROWS
                || parsed.OR()
                    .size()
                    != parsed.resourceId()
                        .size() - 1)
                throw new IllegalArgumentException("Use 0–32 resource rows without trailing OR");
            return parsed;
        } catch (ParseCancellationException invalid) {
            throw new IllegalArgumentException("Invalid resource alternative", invalid);
        }
    }
}
