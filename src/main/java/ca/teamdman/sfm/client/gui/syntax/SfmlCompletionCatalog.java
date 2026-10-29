package ca.teamdman.sfm.client.gui.syntax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

import ca.teamdman.langs.SFMLLexer;

/**
 * UI-only reference data. Built-ins mirror DataCompiler; compilation remains the authority for types and scope.
 * Cache one Snapshot per source edit, then search it without lexing during rendering or typing in the search box.
 */
public final class SfmlCompletionCatalog {

    public enum Kind {
        CONSTRUCTOR,
        FUNCTION,
        METHOD,
        STATEMENT,
        PLAYER_FUNCTION,
        VARIABLE
    }

    public enum Context {
        EXPRESSION,
        RECEIVER,
        TRIGGER_STATEMENT
    }

    public enum OutlineKind {
        FUNCTION,
        TRIGGER
    }

    // ProgramDocument accepts 30,000 UTF-8 bytes. The editor may temporarily contain an oversized draft.
    public static final int MAX_SOURCE_CHARACTERS = 30000;
    public static final int MAX_ENTRIES = 256;
    private static final int MAX_TOKENS = 16000;
    private static final int MAX_HEADER_TOKENS = 512;
    private static final List<Entry> BUILT_INS = createBuiltIns();

    private SfmlCompletionCatalog() {}

    public static List<Entry> builtIns() {
        return BUILT_INS;
    }

    static Entry variable(String name, String description) {
        return new Entry(name, name, name, description, "", Kind.VARIABLE, Context.EXPRESSION);
    }

    public static final class Entry {

        private final String name, signature, template, description, example, searchText;
        private final Kind kind;
        private final Context context;

        private Entry(String name, String signature, String template, String description, String example, Kind kind,
            Context context) {
            this.name = name;
            this.signature = signature;
            this.template = template;
            this.description = description;
            this.example = example;
            this.kind = kind;
            this.context = context;
            searchText = (name + " " + signature + " " + description).toLowerCase(Locale.ROOT);
        }

        public String getName() {
            return name;
        }

        public String getSignature() {
            return signature;
        }

        public String getTemplate() {
            return template;
        }

        public String getDescription() {
            return description;
        }

        /** A compiler-checkable example body, without a trigger wrapper. */
        public String getExample() {
            return example;
        }

        public Kind getKind() {
            return kind;
        }

        public Context getContext() {
            return context;
        }
    }

    public static final class OutlineEntry {

        private final String title;
        private final int offset, line;
        private final OutlineKind kind;

        private OutlineEntry(String title, Token token, OutlineKind kind, String source) {
            this.title = title;
            // CharStreams indexes Unicode code points; editor buffers index UTF-16 code units.
            offset = source.offsetByCodePoints(0, token.getStartIndex());
            line = token.getLine();
            this.kind = kind;
        }

        public String getTitle() {
            return title;
        }

        public int getOffset() {
            return offset;
        }

        public int getLine() {
            return line;
        }

        public OutlineKind getKind() {
            return kind;
        }
    }

    public static final class Snapshot {

        private final List<Entry> entries;
        private final List<OutlineEntry> outline;
        private final boolean truncated;

        private Snapshot(List<Entry> entries, List<OutlineEntry> outline, boolean truncated) {
            this.entries = Collections.unmodifiableList(entries);
            this.outline = Collections.unmodifiableList(outline);
            this.truncated = truncated;
        }

        public List<Entry> entries() {
            return entries;
        }

        public List<OutlineEntry> outline() {
            return outline;
        }

        public boolean isTruncated() {
            return truncated;
        }
    }

    /** Lexer-based so comments, quoted text and escape sequences share the real language's rules. */
    public static Snapshot scan(String source) {
        if (source == null) source = "";
        boolean truncated = source.length() > MAX_SOURCE_CHARACTERS;
        String bounded = truncated ? source.substring(0, MAX_SOURCE_CHARACTERS) : source;
        SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(bounded));
        lexer.removeErrorListeners();
        List<Token> tokens = new ArrayList<>();
        for (int count = 0; count < MAX_TOKENS; count++) {
            Token token = lexer.nextToken();
            if (token.getType() == Token.EOF) break;
            if (token.getChannel() == Token.DEFAULT_CHANNEL) tokens.add(token);
            if (count == MAX_TOKENS - 1) truncated = true;
        }
        List<Entry> players = new ArrayList<>();
        List<OutlineEntry> outline = new ArrayList<>();
        Set<String> names = new HashSet<>();
        for (int i = 0; i < tokens.size(); i++) {
            Token start = tokens.get(i);
            if (start.getType() == SFMLLexer.FUNCTION) {
                Entry entry = functionHeader(tokens, i);
                if (entry != null && names.add(entry.name)) {
                    if (players.size() < MAX_ENTRIES) players.add(entry);
                    else truncated = true;
                    if (outline.size() < MAX_ENTRIES)
                        outline.add(new OutlineEntry(entry.signature, start, OutlineKind.FUNCTION, bounded));
                    else truncated = true;
                }
            } else if (start.getType() == SFMLLexer.EVERY) {
                int end = i + 1;
                while (end < tokens.size() && end - i <= MAX_HEADER_TOKENS
                    && tokens.get(end)
                        .getType() != SFMLLexer.DO
                    && tokens.get(end)
                        .getType() != SFMLLexer.END)
                    end++;
                if (end < tokens.size() && tokens.get(end)
                    .getType() == SFMLLexer.DO) {
                    if (outline.size() < MAX_ENTRIES)
                        outline.add(new OutlineEntry(join(tokens, i, end), start, OutlineKind.TRIGGER, bounded));
                    else truncated = true;
                }
            }
        }
        // Exact-name user functions take precedence in DataCompiler; constructors and statements cannot be shadowed.
        List<Entry> all = new ArrayList<>();
        for (Entry entry : BUILT_INS) if (entry.kind != Kind.FUNCTION || !names.contains(entry.name)) all.add(entry);
        all.addAll(players);
        return new Snapshot(all, outline, truncated);
    }

    /** All query words must match. Empty results stay empty rather than silently showing unrelated entries. */
    public static List<Entry> search(Snapshot snapshot, String query, int limit) {
        if (snapshot == null || limit <= 0) return Collections.emptyList();
        String normalized = query == null ? ""
            : query.substring(0, Math.min(256, query.length()))
                .trim()
                .toLowerCase(Locale.ROOT);
        String[] words = normalized.isEmpty() ? new String[0] : normalized.split("\\s+");
        List<Entry> result = new ArrayList<>();
        int boundedLimit = Math.min(limit, MAX_ENTRIES);
        for (Entry entry : snapshot.entries) {
            boolean match = true;
            for (String word : words) if (!entry.searchText.contains(word)) {
                match = false;
                break;
            }
            if (match) result.add(entry);
            if (result.size() == boundedLimit) break;
        }
        return Collections.unmodifiableList(result);
    }

    private static Entry functionHeader(List<Token> tokens, int start) {
        int end = Math.min(tokens.size(), start + MAX_HEADER_TOKENS);
        int open = start + 1;
        while (open < end && tokens.get(open)
            .getType() != SFMLLexer.LPAREN) {
            int type = tokens.get(open)
                .getType();
            if (type == SFMLLexer.DO || type == SFMLLexer.END
                || type == SFMLLexer.FUNCTION
                || type == SFMLLexer.EVERY
                || type == SFMLLexer.STRING) return null;
            open++;
        }
        if (open >= end || open == start + 1) return null;
        String name = joinName(tokens, start + 1, open);
        if (!name.matches("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*")) return null;
        int close = open + 1;
        // Function parameter types contain records and fixed arrays, but no nested parenthesis.
        while (close < end && tokens.get(close)
            .getType() != SFMLLexer.RPAREN) {
            int type = tokens.get(close)
                .getType();
            if (type == SFMLLexer.DO || type == SFMLLexer.END
                || type == SFMLLexer.FUNCTION
                || type == SFMLLexer.EVERY
                || type == SFMLLexer.LPAREN
                || type == SFMLLexer.STRING) return null;
            close++;
        }
        if (close + 2 >= end || tokens.get(close + 1)
            .getType() != SFMLLexer.RETURNS) return null;
        int body = close + 2;
        while (body < end && tokens.get(body)
            .getType() != SFMLLexer.DO) {
            int type = tokens.get(body)
                .getType();
            if (type == SFMLLexer.END || type == SFMLLexer.FUNCTION || type == SFMLLexer.EVERY) return null;
            body++;
        }
        if (body >= end || body == close + 2) return null;
        List<String> arguments = new ArrayList<>();
        for (int n = open + 1; n < close; n++) if (tokens.get(n)
            .getType() == SFMLLexer.VARIABLE)
            arguments.add(
                "<" + tokens.get(n)
                    .getText()
                    .substring(1) + ">");
        String signature = name + "(" + join(tokens, open + 1, close) + ") -> " + join(tokens, close + 2, body);
        return new Entry(
            name,
            signature,
            name + "(" + String.join(", ", arguments) + ")",
            "Current-program function. Arguments and return value use its declared fixed types; check compiler diagnostics.",
            "",
            Kind.PLAYER_FUNCTION,
            Context.EXPRESSION);
    }

    private static String joinName(List<Token> tokens, int start, int end) {
        StringBuilder text = new StringBuilder();
        for (int i = start; i < end; i++) text.append(
            tokens.get(i)
                .getText());
        return text.toString();
    }

    private static String join(List<Token> tokens, int start, int end) {
        StringBuilder text = new StringBuilder();
        for (int i = start; i < end; i++) {
            String value = tokens.get(i)
                .getText();
            String previous = i > start ? tokens.get(i - 1)
                .getText() : "";
            if (i > start && !value.equals(",")
                && !value.equals(":")
                && !value.equals(")")
                && !value.equals("]")
                && !value.equals(".")
                && !previous.equals(".")
                && !previous.equals("[")
                && !previous.equals("(")) text.append(' ');
            text.append(value);
        }
        return text.toString();
    }

    private static List<Entry> createBuiltIns() {
        List<Entry> entries = new ArrayList<>();
        add(
            entries,
            "label",
            "label(\"name\") -> label_ref",
            "label(\"<name>\")",
            "Select an exact case-sensitive label group. Requires a nonempty quoted literal; computed strings are unsupported.",
            "let $value = label(\"inputs\")",
            Kind.CONSTRUCTOR,
            Context.EXPRESSION);
        add(
            entries,
            "item",
            "item(\"namespace:name\" | \"*\") -> resource_selector",
            "item(\"<namespace:item>\")",
            "Select all metadata/NBT variants of an item, or every item with *. A selector is not an exact item key.",
            "let $value = item(\"minecraft:iron_ingot\")",
            Kind.CONSTRUCTOR,
            Context.EXPRESSION);
        add(
            entries,
            "ore",
            "ore(\"oreName\") -> resource_selector",
            "ore(\"<oreName>\")",
            "Select an exact case-sensitive OreDictionary name. Requires a quoted literal; unknown names match nothing.",
            "let $value = ore(\"ingotIron\")",
            Kind.CONSTRUCTOR,
            Context.EXPRESSION);
        add(
            entries,
            "min",
            "min(a: integer, b: integer) -> integer",
            "min(<a>, <b>)",
            "Return the smaller of two signed 64-bit integers.",
            "let $value = min(1, 2)",
            Kind.FUNCTION,
            Context.EXPRESSION);
        add(
            entries,
            "max",
            "max(a: integer, b: integer) -> integer",
            "max(<a>, <b>)",
            "Return the larger of two signed 64-bit integers.",
            "let $value = max(1, 2)",
            Kind.FUNCTION,
            Context.EXPRESSION);
        add(
            entries,
            "clamp",
            "clamp(value: integer, low: integer, high: integer) -> integer",
            "clamp(<value>, <low>, <high>)",
            "Constrain to inclusive bounds. Reversed bounds fault.",
            "let $value = clamp(20, 0, 15)",
            Kind.FUNCTION,
            Context.EXPRESSION);
        add(
            entries,
            "abs",
            "abs(value: integer) -> integer",
            "abs(<value>)",
            "Absolute integer value. The minimum signed 64-bit value overflows and faults.",
            "let $value = abs(-2)",
            Kind.FUNCTION,
            Context.EXPRESSION);
        add(
            entries,
            "len",
            "len(string | T[N] | item_contents | item_locations) -> integer",
            "len(<values>)",
            "Count outer sequence entries or Unicode code points in a string; captured contents are not rescanned.",
            "let $value = len([1, 2])",
            Kind.FUNCTION,
            Context.EXPRESSION);
        add(
            entries,
            "total",
            "total(item_contents | item_locations) -> integer",
            "total(<snapshot>)",
            "Sum quantities in captured native contents or locations, without another inventory scan. Ordinary arrays are unsupported.",
            "let $value = total(label(\"inputs\").contents())",
            Kind.FUNCTION,
            Context.EXPRESSION);
        add(
            entries,
            "stock",
            "stock(contents: item_contents, item: item_key) -> integer",
            "stock(<snapshot>, <itemKey>)",
            "Read captured quantity for an exact item key including metadata/NBT, or zero. Item/ore selectors are unsupported.",
            "let $snapshot = label(\"inputs\").contents()\nif(len($snapshot) > 0) then\nlet $value = stock($snapshot, $snapshot[0].item)\nend",
            Kind.FUNCTION,
            Context.EXPRESSION);
        add(
            entries,
            "shortage",
            "shortage(required: integer, available: integer) -> integer",
            "shortage(<required>, <available>)",
            "Return max(required - available, 0). Both quantities must be nonnegative. Does not query inventories.",
            "let $value = shortage(16, 8)",
            Kind.FUNCTION,
            Context.EXPRESSION);
        add(
            entries,
            "transfer",
            "transfer(source, destination [, selector | item_key [, amount]]) [into $result]",
            "transfer(<source>, <destination>)",
            "Trigger statement: omit selector to move all items; omit amount to use visible stock. Optional INTO captures moved/remaining/status. No expression return value.",
            "transfer(label(\"inputs\"), label(\"outputs\"))",
            Kind.STATEMENT,
            Context.TRIGGER_STATEMENT);
        add(
            entries,
            "print",
            "print(value1 [, ... value8])",
            "print(<value>)",
            "Trigger debug-output statement accepting 1–8 typed expressions. Captured by the debug terminal; not a value-returning function.",
            "print(\"stock\", 1)",
            Kind.STATEMENT,
            Context.TRIGGER_STATEMENT);
        method(
            entries,
            "contents",
            "$label.contents([selector]) -> item_contents",
            "$<label>.contents()",
            "Capture exact item groups and locations. Optional item/ore selector. Unavailable/incomplete access faults; not a reservation or persistent state.",
            "label(\"inputs\").contents()");
        method(
            entries,
            "count",
            "$label.count(selector: resource_selector) -> integer",
            "$<label>.count(item(\"*\"))",
            "Count visible matching stock. Requires one selector, not an exact item key. Unavailable access faults; does not prove extractability.",
            "label(\"inputs\").count(item(\"*\"))");
        method(
            entries,
            "empty",
            "$label.empty([selector]) -> boolean",
            "$<label>.empty()",
            "True only for a complete available view with no matching visible items. Missing/incomplete access returns false. Not an insertion-capacity query.",
            "label(\"inputs\").empty()");
        method(
            entries,
            "available",
            "$label.available([selector]) -> boolean",
            "$<label>.available()",
            "Check complete loaded inventory access, not stock, extractability or free capacity. Missing/incomplete access returns false.",
            "label(\"inputs\").available()");
        method(
            entries,
            "side",
            "$label.side(\"absolute direction\") -> label_ref",
            "$<label>.side(\"<direction>\")",
            "Restrict a new label handle to one absolute face: down/up/north/south/west/east, bottom/top aliases. Requires a literal; preserves slot restriction.",
            "label(\"inputs\").side(\"north\")");
        method(
            entries,
            "slots",
            "$label.slots(first: integer, last: integer) -> label_ref",
            "$<label>.slots(<first>, <last>)",
            "Restrict a new handle to an inclusive zero-based backend slot range. Preserves the side; does not bypass permissions. Invalid for redstone queries.",
            "label(\"inputs\").slots(0, 8)");
        method(
            entries,
            "redstone",
            "$label.redstone() -> integer",
            "$<label>.redstone()",
            "Maximum native Redstone Port input across selected ports/faces. Unavailable access faults; omitted side selects all faces. No slot restrictions.",
            "label(\"signal\").redstone()");
        method(
            entries,
            "powered",
            "$label.powered() -> boolean",
            "$<label>.powered()",
            "Whether the selected native Redstone Port maximum input exceeds zero. Unavailable access faults; no slot restrictions.",
            "label(\"signal\").powered()");
        method(
            entries,
            "redstoneAvailable",
            "$label.redstoneAvailable() -> boolean",
            "$<label>.redstoneAvailable()",
            "Whether every selected native Redstone Port and face is loaded and readable. Missing/incomplete access returns false; no slot restrictions.",
            "label(\"signal\").redstoneAvailable()");
        return Collections.unmodifiableList(entries);
    }

    private static void method(List<Entry> entries, String name, String signature, String template, String description,
        String expression) {
        add(
            entries,
            name,
            signature,
            template,
            description,
            "let $value = " + expression,
            Kind.METHOD,
            Context.RECEIVER);
    }

    private static void add(List<Entry> entries, String name, String signature, String template, String description,
        String example, Kind kind, Context context) {
        entries.add(new Entry(name, signature, template, description, example, kind, context));
    }
}
