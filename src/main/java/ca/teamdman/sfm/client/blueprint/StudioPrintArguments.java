package ca.teamdman.sfm.client.blueprint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import ca.teamdman.langs.SFMLParser;
import ca.teamdman.sfml.ast.PrintStatement;

/** Authoritative pending Print expressions; only parsed top-level boundaries are delimiters. */
public final class StudioPrintArguments {

    public static final int MAX_ROWS = PrintStatement.MAX_ARGUMENTS;

    public static final class Row {

        public final UUID id;
        public final String text;
        private final String originalText;

        private Row(UUID id, String text) {
            this(id, text, text);
        }

        private Row(UUID id, String text, String originalText) {
            this.id = id;
            this.text = text;
            this.originalText = originalText;
        }
    }

    private final String original, prefix, suffix;
    private final List<Row> rows = new ArrayList<>();
    private final List<String> separators = new ArrayList<>();
    private final List<UUID> aliases = new ArrayList<>();
    private UUID revision = UUID.randomUUID();
    private boolean aliasesRetired;

    public StudioPrintArguments(String clause) {
        SFMLParser parser = parser(StudioExpressionSyntax.print(clause));
        try {
            List<SFMLParser.DataExpressionContext> expressions = parser.printStatement()
                .dataExpression();
            if (parser.getCurrentToken()
                .getType() != Token.EOF || expressions.isEmpty()
                || expressions.size() > MAX_ROWS)
                throw new IllegalArgumentException("PRINT needs 1 to 8 expressions without a trailing comma");
            int first = -1, previous = -1;
            for (SFMLParser.DataExpressionContext expression : expressions) {
                int start = clause.offsetByCodePoints(
                    0,
                    expression.getStart()
                        .getStartIndex());
                int end = clause.offsetByCodePoints(
                    0,
                    expression.getStop()
                        .getStopIndex() + 1);
                if (first < 0) first = start;
                if (previous >= 0) separators.add(clause.substring(previous, start));
                Row row = new Row(UUID.randomUUID(), clause.substring(start, end));
                rows.add(row);
                aliases.add(row.id);
                previous = end;
            }
            original = clause;
            prefix = clause.substring(0, first);
            suffix = clause.substring(previous);
        } catch (ParseCancellationException invalid) {
            throw new IllegalArgumentException("Use 1 to 8 complete Print expressions", invalid);
        }
    }

    public StudioPrintArguments(StudioPrintArguments other) {
        original = other.original;
        prefix = other.prefix;
        suffix = other.suffix;
        rows.addAll(other.rows);
        separators.addAll(other.separators);
        aliases.addAll(other.aliases);
        revision = other.revision;
        aliasesRetired = other.aliasesRetired;
    }

    public List<Row> rows() {
        return Collections.unmodifiableList(new ArrayList<>(rows));
    }

    public int size() {
        return rows.size();
    }

    public UUID revision() {
        return revision;
    }

    public boolean aliasesRetired() {
        return aliasesRetired;
    }

    public String value(UUID id) {
        return rows.get(index(id)).text;
    }

    public UUID alias(int index) {
        if (aliasesRetired)
            throw new IllegalArgumentException("Print argument positions changed. Use rows or reopen Fields.");
        if (index < 0 || index >= aliases.size()) throw new IllegalArgumentException("Unknown Print argument");
        return aliases.get(index);
    }

    public boolean set(UUID id, String value) {
        int index = index(id);
        bounded(value, length() - rows.get(index).text.length());
        if (rows.get(index).text.equals(value)) return false;
        rows.set(index, new Row(id, value, rows.get(index).originalText));
        return true;
    }

    public UUID append(String value) {
        if (rows.size() >= MAX_ROWS) throw new IllegalArgumentException("Keep at most 8 Print arguments");
        bounded(value, length() + 2);
        Row row = new Row(UUID.randomUUID(), value, null);
        rows.add(row);
        separators.add(", ");
        changedStructure();
        return row.id;
    }

    public void remove(UUID id) {
        int index = index(id);
        if (rows.size() == 1) throw new IllegalArgumentException("Keep at least one Print argument");
        separators.remove(index == rows.size() - 1 ? index - 1 : index);
        rows.remove(index);
        changedStructure();
    }

    private void changedStructure() {
        aliasesRetired = true;
        revision = UUID.randomUUID();
    }

    public boolean sameContent(StudioPrintArguments other) {
        if (other == null || aliasesRetired != other.aliasesRetired
            || rows.size() != other.rows.size()
            || !original.equals(other.original)
            || !separators.equals(other.separators)) return false;
        for (int i = 0; i < rows.size(); i++) if (!rows.get(i).text.equals(other.rows.get(i).text)) return false;
        return true;
    }

    public String applyTo(String clause) {
        if (!original.equals(clause)) throw new IllegalArgumentException("Print clause changed. Reopen Fields.");
        StringBuilder result = new StringBuilder(prefix);
        for (int i = 0; i < rows.size(); i++) {
            validateRow(rows.get(i).text);
            if (i > 0) result.append(separators.get(i - 1));
            Row row = rows.get(i);
            // Match the original expressionN updater while retaining entered text in the pending row.
            result.append(row.text.equals(row.originalText) ? row.text : row.text.trim());
        }
        result.append(suffix);
        StudioExpressionSyntax.print(result.toString());
        return result.toString();
    }

    public static void validateRow(String expression) {
        SFMLParser parser = parser(StudioExpressionSyntax.expression(expression));
        try {
            parser.dataExpression();
            if (parser.getCurrentToken()
                .getType() != Token.EOF) throw new IllegalArgumentException("Enter one expression per row");
        } catch (ParseCancellationException invalid) {
            throw new IllegalArgumentException("Enter one complete Print expression; blank blocks Apply", invalid);
        }
    }

    private int index(UUID id) {
        for (int i = 0; i < rows.size(); i++) if (rows.get(i).id.equals(id)) return i;
        throw new IllegalArgumentException("Print argument changed. Reopen Fields.");
    }

    private int length() {
        int count = prefix.length() + suffix.length();
        for (Row row : rows) count += row.text.length();
        for (String gap : separators) count += gap.length();
        return count;
    }

    private static void bounded(String value, int other) {
        if (value == null || (long) other + value.length() > BlueprintGraph.MAX_CLAUSE)
            throw new IllegalArgumentException("Print clause exceeds 4096 characters");
    }

    private static SFMLParser parser(CommonTokenStream tokens) {
        SFMLParser parser = new SFMLParser(tokens);
        parser.removeErrorListeners();
        parser.setErrorHandler(new BailErrorStrategy());
        return parser;
    }
}
