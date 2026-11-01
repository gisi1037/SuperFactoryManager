package ca.teamdman.sfm.client.blueprint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
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
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties.Field;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties.FieldType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.ResourceSelector;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

/** Bounded authoring snapshots. No world/provider/NEI access; registry iteration is supplied once by the client. */
public final class StudioFieldPicker {

    public static final int MAX_LABELS = 256, MAX_DRAFT_VISITS = 4096, MAX_REGISTRY_VISITS = 8192, MAX_RESULTS = 64,
        MAX_QUERY = 96, MAX_VALUE = 256;

    public enum Kind {
        LEGACY_LABEL,
        TYPED_LABEL,
        LEGACY_RESOURCE,
        TYPED_ITEM
    }

    public static final class Target {

        public final Kind kind;
        public final String key, original;
        private final String prefix, suffix;
        private final UUID row, rowRevision;
        private final String rowOriginal;
        private final StudioFieldDraft transferOwner;

        private Target(Kind kind, String key, String original, String prefix, String suffix) {
            this(kind, key, original, prefix, suffix, null, null, null);
        }

        private Target(Kind kind, String key, String original, String prefix, String suffix, UUID row, UUID revision,
            String rowOriginal) {
            this(kind, key, original, prefix, suffix, row, revision, rowOriginal, null);
        }

        private Target(Kind kind, String key, String original, String prefix, String suffix, UUID row, UUID revision,
            String rowOriginal, StudioFieldDraft transferOwner) {
            this.kind = kind;
            this.key = key;
            this.original = original;
            this.prefix = prefix;
            this.suffix = suffix;
            this.row = row;
            rowRevision = revision;
            this.rowOriginal = rowOriginal;
            this.transferOwner = transferOwner;
        }

        private String currentValue(StudioFieldDraft draft) {
            return transferOwner != null ? draft.transferArgumentValue(row) : draft.value(key);
        }

        public boolean labels() {
            return kind == Kind.LEGACY_LABEL || kind == Kind.TYPED_LABEL;
        }

        private String encode(String value) {
            if (value == null || value.isEmpty()
                || value.length() > MAX_VALUE
                || value.chars()
                    .anyMatch(c -> Character.isISOControl(c)))
                throw new IllegalArgumentException("Unrepresentable picker value");
            DataValue.string(value); // The picker and compiler share standard SFML string escaping.
            String literal;
            if (kind == Kind.LEGACY_LABEL || kind == Kind.TYPED_LABEL) {
                LabelReference.literal(value);
                literal = DataValue.string(value)
                    .toString();
            } else {
                ResourceSelector.item(value);
                literal = DataValue.string((kind == Kind.LEGACY_RESOURCE ? "item:" : "") + value)
                    .toString();
            }
            return prefix + literal + suffix;
        }
    }

    public static final class Candidate {

        public final String value, replacement;
        private final String search;

        private Candidate(String value, String replacement) {
            this.value = value;
            this.replacement = replacement;
            search = value.toLowerCase(Locale.ROOT);
        }
    }

    public static final class Results {

        public final List<Candidate> entries;
        public final int matches;

        private Results(List<Candidate> entries, int matches) {
            this.entries = Collections.unmodifiableList(entries);
            this.matches = matches;
        }
    }

    public static final class Snapshot {

        public final String origin;
        public final int visited, omitted;
        public final boolean truncated;
        private final List<Candidate> candidates;
        private final Target target;

        private Snapshot(String origin, int visited, int omitted, boolean truncated, List<Candidate> candidates,
            Target target) {
            this.origin = origin;
            this.visited = visited;
            this.omitted = omitted;
            this.truncated = truncated;
            this.candidates = Collections.unmodifiableList(candidates);
            this.target = target;
        }

        public Results search(String query) {
            if (query == null || query.length() > MAX_QUERY)
                throw new IllegalArgumentException("Search exceeds 96 characters");
            String find = query.toLowerCase(Locale.ROOT);
            List<Candidate> entries = new ArrayList<>();
            int matches = 0;
            for (Candidate candidate : candidates) if (candidate.search.contains(find)) {
                matches++;
                if (entries.size() < MAX_RESULTS) entries.add(candidate);
            }
            return new Results(entries, matches);
        }
    }

    /** Selecting a row is local to this modal. Only Choose writes a pending field; never the graph or source. */
    public static final class Session {

        public final Snapshot snapshot;
        private final StudioFieldDraft owner;
        private final Target target;
        private Candidate selected;

        public Session(StudioFieldDraft owner, Target target, Snapshot snapshot) {
            if (owner == null || target == null
                || snapshot == null
                || snapshot.target != target
                || !target.currentValue(owner)
                    .equals(target.original))
                throw new IllegalArgumentException("Picker field changed");
            this.owner = owner;
            this.target = target;
            this.snapshot = snapshot;
            requireRow(owner, target);
        }

        public void select(Candidate candidate) {
            if (!snapshot.candidates.contains(candidate))
                throw new IllegalArgumentException("Foreign picker candidate");
            selected = candidate;
        }

        public String selectedValue() {
            return selected == null ? "" : selected.value;
        }

        public boolean labels() {
            return target.labels();
        }

        public boolean choose(StudioFieldDraft draft, UUID project, Node node, String source) {
            if (draft != owner || !target.currentValue(owner)
                .equals(target.original))
                throw new IllegalArgumentException("Picker field changed. Cancel and reopen the picker.");
            owner.requireCurrent(project, node, source);
            requireRow(owner, target);
            if (selected == null) throw new IllegalArgumentException("Select a suggestion first");
            if (target.transferOwner != null) {
                // Encoding already validates the bare LABEL/ITEM literal; other pending roles may still be invalid.
                StudioTransferArguments.validateRow(selected.replacement);
                return owner.setTransferArgument(target.row, selected.replacement);
            }
            if (target.row != null) {
                // Only this row changes pending state. Apply validates every row and other field atomically.
                if (target.key.equals("exclusion")) {
                    StudioExclusionRows.validateLiteral(selected.replacement);
                    return owner.setExclusion(target.row, selected.replacement);
                }
                if (target.key.equals("resource")) {
                    StudioResourceAlternatives.validateLiteral(selected.replacement);
                    return owner.setResource(target.row, selected.replacement);
                }
                StudioLabelRows.validateLiteral(selected.replacement);
                return owner.setLabel(target.row, selected.replacement);
            }
            // Compile the exact proposed span before touching even the pending value.
            StudioNodeProperties.update(node.type, node.clause(), target.key, selected.replacement);
            return owner.set(target.key, selected.replacement);
        }
    }

    private StudioFieldPicker() {}

    private static void requireRow(StudioFieldDraft draft, Target target) {
        if (target.transferOwner != null) {
            if (draft != target.transferOwner || !draft.hasTransferArguments()
                || !draft.transferArgumentRevision()
                    .equals(target.rowRevision)
                || !draft.transferArgumentValue(target.row)
                    .equals(target.rowOriginal))
                throw new IllegalArgumentException("Transfer roles changed. Cancel and reopen the picker.");
            return;
        }
        if (target.row != null && target.key.equals("exclusion")) {
            if (!draft.hasExclusionRows() || !draft.exclusionRevision()
                .equals(target.rowRevision)
                || !draft.exclusionValue(target.row)
                    .equals(target.rowOriginal))
                throw new IllegalArgumentException("Exclusion rows changed. Cancel and reopen the picker.");
            return;
        }
        if (target.row != null && target.key.equals("resource")) {
            if (!draft.hasResourceRows() || !draft.resourceRevision()
                .equals(target.rowRevision)
                || !draft.resourceValue(target.row)
                    .equals(target.rowOriginal))
                throw new IllegalArgumentException("Resource rows changed. Cancel and reopen the picker.");
            return;
        }
        if (target.row != null && (!draft.hasLabelRows() || !draft.labelRevision()
            .equals(target.rowRevision)
            || !draft.labelValue(target.row)
                .equals(target.rowOriginal)))
            throw new IllegalArgumentException("Label rows changed. Cancel and reopen the picker.");
    }

    public static Target exclusionRowTarget(StudioFieldDraft draft, UUID row) {
        try {
            return new Target(
                Kind.LEGACY_RESOURCE,
                "exclusion",
                draft.value("exclusion"),
                "",
                "",
                row,
                draft.exclusionRevision(),
                draft.exclusionValue(row));
        } catch (IllegalArgumentException invalid) {
            return null;
        }
    }

    public static Target resourceRowTarget(StudioFieldDraft draft, UUID row) {
        try {
            return new Target(
                Kind.LEGACY_RESOURCE,
                "resource",
                draft.value("resource"),
                "",
                "",
                row,
                draft.resourceRevision(),
                draft.resourceValue(row));
        } catch (IllegalArgumentException invalid) {
            return null;
        }
    }

    public static Target labelRowTarget(StudioFieldDraft draft, UUID row) {
        try {
            String literal = draft.labelValue(row);
            // A suggestion may repair blank/invalid pending text. Its replacement is validated on Choose.
            return new Target(
                Kind.LEGACY_LABEL,
                "label",
                draft.value("label"),
                "",
                "",
                row,
                draft.labelRevision(),
                literal);
        } catch (IllegalArgumentException invalid) {
            return null;
        }
    }

    /** Complex LABEL/ITEM expressions remain manual: only a bare one-string constructor is eligible. */
    public static Target target(Type type, Field field, String value) {
        if (value == null || value.length() > BlueprintGraph.MAX_CLAUSE
            || value.indexOf('\n') >= 0
            || value.indexOf('\r') >= 0
            || value.indexOf('\0') >= 0) return null;
        try {
            if (field.getType() == FieldType.LABEL || field.getType() == FieldType.OPTIONAL_RESOURCE
                || field.getType() == FieldType.OPTIONAL_EXCLUSION) {
                SFMLParser parser = parser(value);
                if (field.getType() == FieldType.LABEL) parser.label();
                else if (!value.trim()
                    .isEmpty()) parser.resourceId();
                requireEnd(parser);
                return new Target(
                    field.getType() == FieldType.LABEL ? Kind.LEGACY_LABEL : Kind.LEGACY_RESOURCE,
                    field.getKey(),
                    value,
                    "",
                    "");
            }
            if (type != Type.TRANSFER || field.getType() != FieldType.EXPRESSION) return null;
            return typedTarget(field.getKey(), value);
        } catch (IllegalArgumentException | ParseCancellationException invalid) {
            return null;
        }
    }

    /** Role ownership survives alias retirement; shape/value ABA and copied owners cannot revive an old session. */
    public static Target transferRowTarget(StudioFieldDraft draft, UUID row) {
        try {
            String value = draft.transferArgumentValue(row);
            Target bare = typedTarget(draft.transferArgumentRole(row).key, value);
            return bare == null ? null
                : new Target(
                    bare.kind,
                    bare.key,
                    value,
                    bare.prefix,
                    bare.suffix,
                    row,
                    draft.transferArgumentRevision(),
                    value,
                    draft);
        } catch (IllegalArgumentException invalid) {
            return null;
        }
    }

    private static Target typedTarget(String key, String value) {
        try {
            boolean label = key.equals("source") || key.equals("destination");
            if (!label && !key.equals("resource")) return null;
            CommonTokenStream tokens = StudioExpressionSyntax.transferExpression(value);
            List<Token> visible = new ArrayList<>();
            for (Token token : tokens.getTokens()) {
                if (token.getType() == SFMLLexer.LINE_COMMENT) return null;
                if (token.getChannel() == Token.DEFAULT_CHANNEL && token.getType() != Token.EOF) visible.add(token);
            }
            if (visible.size() != 4 || !visible.get(0)
                .getText()
                .equalsIgnoreCase(label ? "LABEL" : "ITEM")
                || visible.get(1)
                    .getType() != SFMLLexer.LPAREN
                || visible.get(2)
                    .getType() != SFMLLexer.STRING
                || visible.get(3)
                    .getType() != SFMLLexer.RPAREN)
                return null;
            Token literal = visible.get(2);
            return new Target(
                label ? Kind.TYPED_LABEL : Kind.TYPED_ITEM,
                key,
                value,
                value.substring(0, value.offsetByCodePoints(0, literal.getStartIndex())),
                value.substring(value.offsetByCodePoints(0, literal.getStopIndex() + 1)));
        } catch (IllegalArgumentException | ParseCancellationException invalid) {
            return null;
        }
    }

    public static Snapshot labels(String source, Target target) {
        if (!target.labels()) throw new IllegalArgumentException("Not a label field");
        String origin = "Draft references only; not live connectivity";
        if (source == null || source.length() > 32767)
            return new Snapshot(origin + " (source unavailable)", 0, 0, true, Collections.emptyList(), target);
        ProgramBuildResult built = new ProgramBuilder(source).build();
        if (!built.isSuccess())
            return new Snapshot(origin + " (source invalid)", 0, 0, true, Collections.emptyList(), target);
        TreeSet<String> names = new TreeSet<>();
        int visited = 0;
        boolean limited = false;
        Iterator<String> iterator = built.getProgram()
            .getReferencedLabels()
            .iterator();
        while (iterator.hasNext() && visited < MAX_DRAFT_VISITS) {
            names.add(iterator.next());
            visited++;
            if (names.size() > MAX_LABELS) {
                names.pollLast();
                limited = true;
            }
        }
        limited |= iterator.hasNext();
        return snapshot(origin, names, visited, limited, target);
    }

    /**
     * The caller supplies registry keys, not item display names/stacks. Iteration is capped, including invalid keys.
     */
    public static Snapshot items(Iterable<String> registry, Target target) {
        if (target.labels()) throw new IllegalArgumentException("Not an item field");
        if (registry == null) throw new IllegalArgumentException("Missing registry snapshot");
        TreeSet<String> names = new TreeSet<>();
        names.add("*");
        int visited = 0;
        Iterator<String> iterator = registry.iterator();
        int omitted = 0;
        while (iterator.hasNext() && visited < MAX_REGISTRY_VISITS) {
            String value = iterator.next();
            visited++;
            if (value == null || value.length() > MAX_VALUE) omitted++;
            else names.add(value);
        }
        Snapshot result = snapshot(
            "Client base item IDs; metadata/NBT stay manual",
            names,
            visited,
            iterator.hasNext(),
            target);
        return new Snapshot(
            result.origin,
            visited,
            result.omitted + omitted,
            result.truncated,
            result.candidates,
            target);
    }

    private static Snapshot snapshot(String origin, Iterable<String> values, int visited, boolean truncated,
        Target target) {
        List<Candidate> candidates = new ArrayList<>();
        int omitted = 0;
        for (String value : values) {
            try {
                candidates.add(new Candidate(value, target.encode(value)));
            } catch (IllegalArgumentException | ParseCancellationException invalid) {
                omitted++;
            }
        }
        return new Snapshot(origin, visited, omitted, truncated, candidates, target);
    }

    private static CommonTokenStream tokens(String source) {
        SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(source));
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
        return tokens;
    }

    private static SFMLParser parser(String source) {
        CommonTokenStream tokens = tokens(source);
        for (Token token : tokens.getTokens()) if (token.getType() == SFMLLexer.LINE_COMMENT)
            throw new IllegalArgumentException("Comments are not a picker field");
        SFMLParser parser = new SFMLParser(tokens);
        parser.removeErrorListeners();
        parser.setErrorHandler(new BailErrorStrategy());
        return parser;
    }

    private static void requireEnd(SFMLParser parser) {
        if (parser.getCurrentToken()
            .getType() != Token.EOF) throw new IllegalArgumentException("Not one field value");
    }
}
