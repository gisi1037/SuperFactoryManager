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

/** Pending ordered legacy label lexemes. Row IDs and structural revisions never leave client memory. */
public final class StudioLabelRows {

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

    public StudioLabelRows(String source) {
        SFMLParser.LabelAccessContext parsed = parse(source);
        int previousEnd = -1;
        for (SFMLParser.LabelContext label : parsed.label()) {
            int start = source.offsetByCodePoints(
                0,
                label.getStart()
                    .getStartIndex()),
                end = source.offsetByCodePoints(
                    0,
                    label.getStop()
                        .getStopIndex() + 1);
            if (previousEnd >= 0) separators.add(source.substring(previousEnd, start));
            rows.add(new Row(UUID.randomUUID(), source.substring(start, end)));
            previousEnd = end;
        }
    }

    public StudioLabelRows(StudioLabelRows other) {
        rows.addAll(other.rows); // Immutable row snapshots; mutations replace a row, never edit a shared object.
        separators.addAll(other.separators);
        revision = other.revision;
    }

    public UUID revision() {
        return revision;
    }

    public int size() {
        return rows.size();
    }

    /** Joined text alone can hide malformed row boundaries (e.g. a quoted comma split between cells). */
    public boolean sameContent(StudioLabelRows other) {
        if (other == null || rows.size() != other.rows.size() || !separators.equals(other.separators)) return false;
        for (int i = 0; i < rows.size(); i++) if (!rows.get(i).text.equals(other.rows.get(i).text)) return false;
        return true;
    }

    public List<Row> rows() {
        return Collections.unmodifiableList(new ArrayList<>(rows));
    }

    public String value(UUID row) {
        return rows.get(index(row)).text;
    }

    public String source() {
        StringBuilder source = new StringBuilder();
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) source.append(separators.get(i - 1));
            source.append(rows.get(i).text);
        }
        return source.toString();
    }

    /** Invalid text remains pending for correction; only Apply validates the combined form. */
    public boolean set(UUID row, String value) {
        int index = index(row);
        String before = rows.get(index).text;
        if (value == null || (long) source().length() - before.length() + value.length() > BlueprintGraph.MAX_CLAUSE)
            throw new IllegalArgumentException("Label list exceeds 4096 characters");
        if (before.equals(value)) return false;
        rows.set(index, new Row(row, value));
        return true;
    }

    public UUID append(String value) {
        if (rows.size() >= MAX_ROWS) throw new IllegalArgumentException(
            "Structured labels support at most 32 rows; use the full clause for larger groups.");
        if (value == null || (long) source().length() + 2 + value.length() > BlueprintGraph.MAX_CLAUSE)
            throw new IllegalArgumentException("Label list exceeds 4096 characters");
        Row row = new Row(UUID.randomUUID(), value);
        rows.add(row);
        separators.add(", ");
        revision = UUID.randomUUID();
        return row.id;
    }

    /** Remove the following delimiter, or the preceding one for the final row. Other lexemes/gaps stay exact. */
    public void remove(UUID row) {
        int index = index(row);
        if (rows.size() == 1) throw new IllegalArgumentException("Keep at least one label");
        separators.remove(index == rows.size() - 1 ? index - 1 : index);
        rows.remove(index);
        revision = UUID.randomUUID();
    }

    private int index(UUID id) {
        for (int i = 0; i < rows.size(); i++) if (rows.get(i).id.equals(id)) return i;
        throw new IllegalArgumentException("Label row changed. Reopen the picker.");
    }

    public static void validateLiteral(String value) {
        if (parse(value).label()
            .size() != 1) throw new IllegalArgumentException("Enter exactly one literal label in each row");
    }

    public static SFMLParser.LabelAccessContext parse(String value) {
        if (value == null || value.length() > BlueprintGraph.MAX_CLAUSE
            || value.indexOf('\n') >= 0
            || value.indexOf('\r') >= 0
            || value.indexOf('\0') >= 0) throw new IllegalArgumentException("Labels must be one bounded line");
        try {
            SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(value));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new BaseErrorListener() {

                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int column,
                    String message, RecognitionException error) {
                    throw new ParseCancellationException(message);
                }
            });
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();
            for (Token token : tokens.getTokens()) if (token.getType() == SFMLLexer.LINE_COMMENT)
                throw new IllegalArgumentException("Comments are not label rows");
            SFMLParser parser = new SFMLParser(tokens);
            parser.removeErrorListeners();
            parser.setErrorHandler(new BailErrorStrategy());
            SFMLParser.LabelAccessContext labels = parser.labelAccess();
            if (parser.getCurrentToken()
                .getType() != Token.EOF || labels.label()
                    .isEmpty()
                || labels.label()
                    .size() > MAX_ROWS
                || labels.roundrobin() != null
                || labels.sidequalifier() != null
                || labels.slotqualifier() != null)
                throw new IllegalArgumentException("Use 1–32 literal labels without modifiers in the label rows");
            return labels;
        } catch (ParseCancellationException invalid) {
            throw new IllegalArgumentException("Invalid literal label list", invalid);
        }
    }
}
