package ca.teamdman.sfm.client.blueprint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.langs.SFMLParser;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;

/** Small safe inspector. A field replaces only its parsed source span; every other byte stays untouched. */
public final class StudioNodeProperties {

    public enum FieldType {
        LABEL,
        NONNEGATIVE_INTEGER,
        INTERVAL,
        SIDES,
        SLOTS,
        BOOLEAN,
        ROUND_ROBIN
    }

    public static final class Field {

        private final String key, name, value, help;
        private final FieldType type;
        private final int start, end;
        private final String insertionPrefix, insertionSuffix, enabledText;

        private Field(String key, String name, String help, FieldType type, ParserRuleContext context, String clause) {
            this(key, name, help, type, context.getStart(), context.getStop(), null, 0, false, "", clause);
        }

        private Field(String key, String name, String help, FieldType type, Token first, Token last, String displayed,
            int insertion, boolean before, String enabledText, String clause) {
            this.key = key;
            this.name = name;
            this.help = help;
            this.type = type;
            // ANTLR CharStreams reports code-point positions; Java text widgets use UTF-16 positions.
            start = clause.offsetByCodePoints(0, first == null ? insertion : first.getStartIndex());
            end = last == null ? start : clause.offsetByCodePoints(0, last.getStopIndex() + 1);
            value = displayed == null ? clause.substring(start, end) : displayed;
            insertionPrefix = first == null && !before ? " " : "";
            insertionSuffix = first == null && before ? " " : "";
            this.enabledText = enabledText;
        }

        public String getKey() {
            return key;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public FieldType getType() {
            return type;
        }

        public String getHelp() {
            return help;
        }
    }

    private StudioNodeProperties() {}

    /** Empty means use the advanced full-clause editor, including for invalid/incomplete clauses. */
    public static List<Field> inspect(Type type, String clause) {
        if (type == null || clause == null
            || clause.length() > BlueprintGraph.MAX_CLAUSE
            || clause.indexOf('\n') >= 0
            || clause.indexOf('\r') >= 0) return Collections.emptyList();
        try {
            BlueprintCompiler.validateClause(type, clause);
            SFMLParser parser = parser(clause + (type == Type.TIMER ? " DO END" : ""));
            List<Field> result = new ArrayList<>();
            if (type == Type.TIMER) {
                SFMLParser.TriggerContext trigger = parser.trigger();
                if (!(trigger instanceof SFMLParser.TimerTriggerContext)) return Collections.emptyList();
                SFMLParser.IntervalContext interval = ((SFMLParser.TimerTriggerContext) trigger).interval();
                result.add(
                    new Field(
                        "interval",
                        "Interval",
                        "Complete interval, including units and optional GLOBAL phase; e.g. 20 ticks.",
                        FieldType.INTERVAL,
                        interval,
                        clause));
                return Collections.unmodifiableList(result);
            }
            SFMLParser.LabelAccessContext labels;
            SFMLParser.ResourceLimitListContext limits = null;
            TerminalNode each;
            SFMLParser.EmptyslotsContext empty = null;
            if (type == Type.INPUT) {
                SFMLParser.InputStatementContext input = parser.inputStatement();
                labels = input.labelAccess();
                each = input.EACH();
                if (input.inputResourceLimits() != null) limits = input.inputResourceLimits()
                    .resourceLimitList();
            } else if (type == Type.OUTPUT) {
                SFMLParser.OutputStatementContext output = parser.outputStatement();
                labels = output.labelAccess();
                each = output.EACH();
                empty = output.emptyslots();
                if (output.outputResourceLimits() != null) limits = output.outputResourceLimits()
                    .resourceLimitList();
            } else return Collections.emptyList();
            // A single editable label must never silently replace or collapse a group of labels.
            if (labels.label()
                .size() != 1) return Collections.emptyList();
            result.add(
                new Field(
                    "label",
                    type == Type.INPUT ? "Source label" : "Destination label",
                    "Exact case-sensitive inventory label. Keep quotes around labels containing spaces.",
                    FieldType.LABEL,
                    labels.label(0),
                    clause));
            // Multiple resource limits have independent allowances; an aggregate amount field would be misleading.
            if (limits != null && limits.resourceLimit()
                .size() == 1) {
                SFMLParser.LimitContext limit = limits.resourceLimit(0)
                    .limit();
                if (limit != null) {
                    SFMLParser.QuantityContext quantity = first(limit, SFMLParser.QuantityContext.class);
                    SFMLParser.RetentionContext retention = first(limit, SFMLParser.RetentionContext.class);
                    if (quantity != null && quantity.number() != null) {
                        result.add(
                            new Field(
                                "amount",
                                "Amount",
                                "Nonnegative signed 64-bit item allowance; existing EACH and filters are retained.",
                                FieldType.NONNEGATIVE_INTEGER,
                                quantity.number(),
                                clause));
                        result.add(
                            booleanField(
                                "amountEach",
                                "Amount per item type",
                                quantity.EACH(),
                                quantity.getStop()
                                    .getStopIndex() + 1,
                                false,
                                "EACH",
                                clause));
                    }
                    if (retention != null && retention.number() != null) {
                        result.add(
                            new Field(
                                "retain",
                                "Retain",
                                "Nonnegative signed 64-bit reserve; existing EACH and filters are retained.",
                                FieldType.NONNEGATIVE_INTEGER,
                                retention.number(),
                                clause));
                        result.add(
                            booleanField(
                                "retainEach",
                                "Retain per item type",
                                retention.EACH(),
                                retention.getStop()
                                    .getStopIndex() + 1,
                                false,
                                "EACH",
                                clause));
                    }
                }
            }
            result.add(
                booleanField(
                    "each",
                    "Allowance per container",
                    each,
                    labels.getStart()
                        .getStartIndex(),
                    true,
                    "EACH",
                    clause));
            SFMLParser.RoundrobinContext round = labels.roundrobin();
            SFMLParser.SidequalifierContext sides = labels.sidequalifier();
            SFMLParser.SlotqualifierContext slots = labels.slotqualifier();
            int tail = labels.getStop()
                .getStopIndex() + 1;
            int roundAnchor = sides != null ? sides.getStart()
                .getStartIndex()
                : slots != null ? slots.getStart()
                    .getStartIndex() : tail;
            result.add(
                modifierField(
                    "roundRobin",
                    "Round robin",
                    "off, label, or block; unrelated modifiers are retained.",
                    FieldType.ROUND_ROBIN,
                    round,
                    round == null ? "off" : round.LABEL() != null ? "label" : "block",
                    roundAnchor,
                    sides != null || slots != null,
                    clause));
            String sideValue = "";
            if (sides instanceof SFMLParser.EachSideContext) sideValue = "each";
            else if (sides instanceof SFMLParser.ListedSidesContext) {
                Token end = ((SFMLParser.ListedSidesContext) sides).SIDE()
                    .getSymbol();
                sideValue = clause.substring(
                    clause.offsetByCodePoints(
                        0,
                        sides.getStart()
                            .getStartIndex()),
                    clause.offsetByCodePoints(0, end.getStartIndex()))
                    .trim();
            }
            result.add(
                modifierField(
                    "sides",
                    "Sides",
                    "Face names separated by commas, each for all faces, or blank for default.",
                    FieldType.SIDES,
                    sides,
                    sideValue,
                    slots == null ? tail
                        : slots.getStart()
                            .getStartIndex(),
                    slots != null,
                    clause));
            result.add(
                modifierField(
                    "slots",
                    "Slots",
                    "Zero-based slots/ranges, e.g. 0,2-8. Blank removes the slot restriction.",
                    FieldType.SLOTS,
                    slots,
                    slots == null ? "" : text(slots.rangeset(), clause),
                    tail,
                    false,
                    clause));
            if (type == Type.OUTPUT) result.add(
                new Field(
                    "emptySlots",
                    "Empty slots only",
                    "true or false; preserves all resource filters.",
                    FieldType.BOOLEAN,
                    empty == null ? null : empty.getStart(),
                    empty == null ? null : empty.getStop(),
                    Boolean.toString(empty != null),
                    each == null ? labels.getStart()
                        .getStartIndex()
                        : each.getSymbol()
                            .getStartIndex(),
                    true,
                    "EMPTY SLOTS IN",
                    clause));
            return Collections.unmodifiableList(result);
        } catch (IllegalArgumentException | org.antlr.v4.runtime.misc.ParseCancellationException invalid) {
            return Collections.emptyList();
        }
    }

    /** Reinspect instead of trusting a stale descriptor after the raw clause has changed. */
    public static String update(Type type, String clause, String fieldKey, String value) {
        if (value == null || value.length() > BlueprintGraph.MAX_CLAUSE
            || value.indexOf('\n') >= 0
            || value.indexOf('\r') >= 0
            || value.indexOf('\0') >= 0) throw new IllegalArgumentException("A property must be one bounded line");
        Field selected = null;
        for (Field field : inspect(type, clause)) if (field.key.equals(fieldKey)) selected = field;
        if (selected == null) throw new IllegalArgumentException("This property requires the full-clause editor");
        String replacement = value.trim();
        if (replacement.equals(selected.value)) return clause;
        if ((selected.type == FieldType.BOOLEAN || selected.type == FieldType.ROUND_ROBIN)
            && replacement.equalsIgnoreCase(selected.value)) return clause;
        try {
            if (selected.type == FieldType.NONNEGATIVE_INTEGER) {
                if (!replacement.matches("[0-9]{1,19}") || Long.parseLong(replacement) < 0)
                    throw new IllegalArgumentException("Use a nonnegative signed 64-bit integer");
            } else if (selected.type == FieldType.BOOLEAN) {
                if (!replacement.equalsIgnoreCase("true") && !replacement.equalsIgnoreCase("false"))
                    throw new IllegalArgumentException("Use true or false");
                replacement = Boolean.parseBoolean(replacement) ? selected.enabledText : "";
            } else if (selected.type == FieldType.ROUND_ROBIN) {
                if (replacement.equalsIgnoreCase("off")) replacement = "";
                else if (replacement.equalsIgnoreCase("label") || replacement.equalsIgnoreCase("block"))
                    replacement = "ROUND ROBIN BY " + replacement.toUpperCase(java.util.Locale.ROOT);
                else throw new IllegalArgumentException("Use off, label, or block");
            } else if (selected.type == FieldType.SIDES || selected.type == FieldType.SLOTS) {
                if (!replacement.isEmpty()) {
                    replacement = selected.type == FieldType.SIDES ? replacement + " SIDE" : "SLOTS " + replacement;
                    SFMLParser parser = parser(replacement);
                    if (selected.type == FieldType.SIDES) parser.sidequalifier();
                    else parser.slotqualifier();
                    requireEnd(parser);
                }
            } else {
                SFMLParser parser = parser(replacement);
                if (selected.type == FieldType.LABEL) parser.label();
                else parser.interval();
                requireEnd(parser);
            }
        } catch (org.antlr.v4.runtime.misc.ParseCancellationException | NumberFormatException invalid) {
            throw new IllegalArgumentException("Invalid " + selected.name.toLowerCase(java.util.Locale.ROOT), invalid);
        }
        if (!replacement.isEmpty()) replacement = selected.insertionPrefix + replacement + selected.insertionSuffix;
        String result = clause.substring(0, selected.start) + replacement + clause.substring(selected.end);
        BlueprintCompiler.validateClause(type, result);
        return result;
    }

    private static Field booleanField(String key, String name, TerminalNode each, int insertion, boolean before,
        String enabled, String clause) {
        Token token = each == null ? null : each.getSymbol();
        return new Field(
            key,
            name,
            "true or false; separate from per-container and per-item allowances.",
            FieldType.BOOLEAN,
            token,
            token,
            Boolean.toString(each != null),
            insertion,
            before,
            enabled,
            clause);
    }

    private static Field modifierField(String key, String name, String help, FieldType type, ParserRuleContext context,
        String value, int insertion, boolean before, String clause) {
        return new Field(
            key,
            name,
            help,
            type,
            context == null ? null : context.getStart(),
            context == null ? null : context.getStop(),
            value,
            insertion,
            before,
            "",
            clause);
    }

    private static String text(ParserRuleContext context, String clause) {
        return clause.substring(
            clause.offsetByCodePoints(
                0,
                context.getStart()
                    .getStartIndex()),
            clause.offsetByCodePoints(
                0,
                context.getStop()
                    .getStopIndex() + 1));
    }

    private static void requireEnd(SFMLParser parser) {
        if (parser.getCurrentToken()
            .getType() != Token.EOF)
            throw new IllegalArgumentException("Enter only the selected property, without extra modifiers");
    }

    private static SFMLParser parser(String source) {
        SFMLLexer lexer = new SFMLLexer(CharStreams.fromString(source));
        lexer.removeErrorListeners();
        SFMLParser parser = new SFMLParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.setErrorHandler(new BailErrorStrategy());
        return parser;
    }

    private static <T> T first(ParseTree tree, Class<T> type) {
        if (type.isInstance(tree)) return type.cast(tree);
        for (int i = 0; i < tree.getChildCount(); i++) {
            T found = first(tree.getChild(i), type);
            if (found != null) return found;
        }
        return null;
    }
}
