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

/** Fixed Transfer roles. Only optional trailing arguments can be added/removed; Apply owns the complete list. */
public final class StudioTransferArguments {

    public static final int MIN_ROWS = 2, MAX_ROWS = 4;

    public enum Role {

        SOURCE("source", "Source"),
        DESTINATION("destination", "Destination"),
        ITEM("resource", "Resource filter / amount"),
        AMOUNT("amount", "Amount");

        public final String key, title;

        Role(String key, String title) {
            this.key = key;
            this.title = title;
        }

        public static Role forKey(String key) {
            for (Role role : values()) if (role.key.equals(key)) return role;
            return null;
        }
    }

    public static final class Row {

        public final UUID id;
        public final Role role;
        public final String text;
        private final String originalText;

        private Row(UUID id, Role role, String text, String originalText) {
            this.id = id;
            this.role = role;
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

    public StudioTransferArguments(String clause) {
        SFMLParser parser = parser(StudioExpressionSyntax.transfer(clause));
        try {
            SFMLParser.TransferStatementContext transfer = parser.transferStatement();
            List<SFMLParser.DataExpressionContext> expressions = transfer.dataExpression();
            if (parser.getCurrentToken()
                .getType() != Token.EOF || transfer.INTO() != null
                || expressions.size() < MIN_ROWS
                || expressions.size() > MAX_ROWS)
                throw new IllegalArgumentException("Use one standalone TRANSFER with 2 to 4 arguments");
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
                String text = clause.substring(start, end);
                Row row = new Row(UUID.randomUUID(), Role.values()[rows.size()], text, text);
                rows.add(row);
                aliases.add(row.id);
                previous = end;
            }
            original = clause;
            prefix = clause.substring(0, first);
            suffix = clause.substring(previous);
        } catch (ParseCancellationException invalid) {
            throw new IllegalArgumentException("Use 2 to 4 complete Transfer expressions", invalid);
        }
    }

    public StudioTransferArguments(StudioTransferArguments other) {
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

    public Role role(UUID id) {
        return rows.get(index(id)).role;
    }

    public UUID alias(Role role) {
        if (aliasesRetired)
            throw new IllegalArgumentException("Transfer arguments changed. Use roles or reopen Fields.");
        if (role == null || role.ordinal() >= aliases.size())
            throw new IllegalArgumentException("Unknown Transfer argument");
        return aliases.get(role.ordinal());
    }

    public boolean set(UUID id, String value) {
        int index = index(id);
        Row old = rows.get(index);
        bounded(value, length() - old.text.length());
        if (old.text.equals(value)) return false;
        rows.set(index, new Row(id, old.role, value, old.originalText));
        return true;
    }

    public UUID append(String value) {
        if (rows.size() >= MAX_ROWS) throw new IllegalArgumentException("Transfer has at most four arguments");
        bounded(value, length() + 2);
        Row row = new Row(UUID.randomUUID(), Role.values()[rows.size()], value, null);
        rows.add(row);
        separators.add(", ");
        changedStructure();
        return row.id;
    }

    public void remove(UUID id) {
        int index = index(id);
        if (index < MIN_ROWS || index != rows.size() - 1) throw new IllegalArgumentException(
            "Remove only the last optional argument; remove amount before item filter");
        rows.remove(index);
        separators.remove(index - 1);
        changedStructure();
    }

    private void changedStructure() {
        aliasesRetired = true;
        revision = UUID.randomUUID();
    }

    public boolean sameContent(StudioTransferArguments other) {
        if (other == null || aliasesRetired != other.aliasesRetired
            || rows.size() != other.rows.size()
            || !original.equals(other.original)
            || !separators.equals(other.separators)) return false;
        for (int i = 0; i < rows.size(); i++) if (!rows.get(i).text.equals(other.rows.get(i).text)) return false;
        return true;
    }

    public String applyTo(String clause) {
        if (!original.equals(clause)) throw new IllegalArgumentException("Transfer clause changed. Reopen Fields.");
        StringBuilder result = new StringBuilder(prefix);
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            validateRow(row.text);
            if (i > 0) result.append(separators.get(i - 1));
            result.append(row.text.equals(row.originalText) ? row.text : row.text.trim());
        }
        result.append(suffix);
        StudioExpressionSyntax.transfer(result.toString());
        return result.toString();
    }

    public static void validateRow(String expression) {
        SFMLParser parser = parser(StudioExpressionSyntax.transferExpression(expression));
        try {
            parser.dataExpression();
            if (parser.getCurrentToken()
                .getType() != Token.EOF) throw new IllegalArgumentException("Enter one expression per Transfer role");
        } catch (ParseCancellationException invalid) {
            throw new IllegalArgumentException("Enter one complete Transfer expression; blank blocks Apply", invalid);
        }
    }

    private int index(UUID id) {
        for (int i = 0; i < rows.size(); i++) if (rows.get(i).id.equals(id)) return i;
        throw new IllegalArgumentException("Transfer role changed. Reopen Fields.");
    }

    private int length() {
        int count = prefix.length() + suffix.length();
        for (Row row : rows) count += row.text.length();
        for (String gap : separators) count += gap.length();
        return count;
    }

    private static void bounded(String value, int other) {
        if (value == null || (long) other + value.length() > BlueprintGraph.MAX_CLAUSE)
            throw new IllegalArgumentException("Transfer clause exceeds 4096 characters");
    }

    private static SFMLParser parser(CommonTokenStream tokens) {
        SFMLParser parser = new SFMLParser(tokens);
        parser.removeErrorListeners();
        parser.setErrorHandler(new BailErrorStrategy());
        return parser;
    }
}
