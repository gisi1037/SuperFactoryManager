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
        ROUND_ROBIN,
        EXPRESSION,
        COMPARISON,
        OPTIONAL_RESOURCE,
        OPTIONAL_EXCLUSION,
        ORE_FILTER,
        INDEPENDENT_LIMITS,
        TRIGGER_HEADER,
        CONDITION,
        REDSTONE_EDGE,
        PHYSICAL_SIDES,
        VARIABLE_NAME,
        ASSIGNMENT_TARGET
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
            boolean optional = type == FieldType.OPTIONAL_RESOURCE || type == FieldType.OPTIONAL_EXCLUSION
                || type == FieldType.ORE_FILTER;
            insertionPrefix = first == null && (!before || optional) ? " " : "";
            insertionSuffix = first == null && (before || optional) ? " " : "";
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
            if (type.isLoop() || type == Type.LET || type == Type.SET || type == Type.GLOBAL_LET || type == Type.STATE)
                return dataFields(type, clause);
            if (type == Type.NAMED_REDSTONE) {
                SFMLParser.NamedRedstoneTriggerContext trigger = StudioNamedRedstoneHeader.parse(clause);
                SFMLParser.LabelAccessContext labels = trigger.labelAccess();
                if (labels.label()
                    .size() > StudioLabelRows.MAX_ROWS)
                    return Collections.singletonList(namedHeaderField(trigger, clause));
                Field labelField = new Field(
                    "label",
                    "Port labels",
                    "1–32 ordered quoted label names. Physical sides remain unchanged.",
                    FieldType.LABEL,
                    labels.label(0)
                        .getStart(),
                    labels.label(
                        labels.label()
                            .size() - 1)
                        .getStop(),
                    null,
                    0,
                    false,
                    "",
                    clause);
                try {
                    StudioLabelRows.parse(labelField.value);
                } catch (IllegalArgumentException manual) {
                    // Never expose only part of a header whose labels cannot be represented by the row model.
                    return Collections.singletonList(namedHeaderField(trigger, clause));
                }
                List<Field> fields = new ArrayList<>();
                Token edge = (trigger.RISING() != null ? trigger.RISING()
                    : trigger.PULSE() != null ? trigger.PULSE() : trigger.FALLING()).getSymbol();
                fields.add(
                    new Field(
                        "edge",
                        "Edge",
                        "RISING or PULSE detect a rising edge; FALLING detects a falling edge.",
                        FieldType.REDSTONE_EDGE,
                        edge,
                        edge,
                        null,
                        0,
                        false,
                        "",
                        clause));
                fields.add(labelField);
                SFMLParser.SidequalifierContext sides = labels.sidequalifier();
                fields.add(
                    new Field(
                        "sides",
                        "Physical sides (optional)",
                        "Blank defaults to all six faces; EACH explicitly selects all. Use TOP, BOTTOM, NORTH, SOUTH, EAST, WEST separated by commas. Omit SIDE; no relative faces or NULL.",
                        FieldType.PHYSICAL_SIDES,
                        sides == null ? null : sides.getStart(),
                        sides == null ? null : sides.getStop(),
                        StudioNamedRedstoneHeader.physicalSideValue(clause),
                        labels.getStop()
                            .getStopIndex() + 1,
                        false,
                        "",
                        clause));
                return Collections.unmodifiableList(fields);
            }
            SFMLParser parser = parser(clause + (type == Type.TIMER ? " DO END" : ""));
            List<Field> result = new ArrayList<>();
            if (type == Type.IF) {
                SFMLParser.BoolexprContext condition = StudioConditionSyntax.parse(clause);
                if (StudioConditionSyntax.hasData(condition)) return Collections.singletonList(
                    new Field(
                        "condition",
                        "Condition",
                        "One boolean predicate using variables in scope, queries or computed thresholds. Leave out IF/THEN/END. Player functions still require Code.",
                        FieldType.CONDITION,
                        condition,
                        clause));
                while (condition instanceof SFMLParser.BooleanParenContext)
                    condition = ((SFMLParser.BooleanParenContext) condition).boolexpr();
                if (!(condition instanceof SFMLParser.BooleanHasContext)) return Collections.emptyList();
                SFMLParser.BooleanHasContext has = (SFMLParser.BooleanHasContext) condition;
                SFMLParser.ResourceIdDisjunctionContext resources = has.resourceIdDisjunction();
                if (has.labelAccess()
                    .label()
                    .size() > StudioLabelRows.MAX_ROWS || has.number() == null
                    || resources != null && (resources.resourceId()
                        .size() > StudioResourceAlternatives.MAX_ROWS
                        || resources.OR()
                            .size()
                            != resources.resourceId()
                                .size() - 1))
                    return Collections.emptyList();
                result.add(
                    new Field(
                        "label",
                        "Inventory labels",
                        "Ordered quoted label names; set operator, sides and slots are retained.",
                        FieldType.LABEL,
                        has.labelAccess()
                            .label(0)
                            .getStart(),
                        has.labelAccess()
                            .label(
                                has.labelAccess()
                                    .label()
                                    .size() - 1)
                            .getStop(),
                        null,
                        0,
                        false,
                        "",
                        clause));
                result.add(
                    new Field(
                        "comparator",
                        "Comparison",
                        "Existing SFML comparison, e.g. =, !=, <, <=, >, >= or GT.",
                        FieldType.COMPARISON,
                        has.comparisonOp(),
                        clause));
                result.add(
                    new Field(
                        "amount",
                        "Comparison threshold",
                        "Nonnegative signed 64-bit literal; the existing set operator determines how counts are compared. No query is evaluated here.",
                        FieldType.NONNEGATIVE_INTEGER,
                        has.number(),
                        clause));
                result.add(
                    new Field(
                        "resource",
                        "Resources (OR, optional)",
                        "0â€“32 OR selectors form one matching set for the comparison. Zero rows restores match-all; existing set operator, filters and exclusions stay. Blank rows block Apply.",
                        FieldType.OPTIONAL_RESOURCE,
                        resources == null ? null : resources.getStart(),
                        resources == null ? null : resources.getStop(),
                        resources == null ? "" : null,
                        has.number()
                            .getStop()
                            .getStopIndex() + 1,
                        false,
                        "",
                        clause));
                return Collections.unmodifiableList(result);
            }
            if (type == Type.REDSTONE) {
                SFMLParser.RedstoneOutputStatementContext output = parser.redstoneOutputStatement();
                if (output.labelAccess()
                    .label()
                    .size() > StudioLabelRows.MAX_ROWS
                    || output.quantity()
                        .size() > 1)
                    return Collections.emptyList();
                SFMLParser.QuantityContext level = output.quantity()
                    .isEmpty() ? null : output.quantity(0);
                if (level != null && level.number() == null) return Collections.emptyList();
                result.add(
                    new Field(
                        "label",
                        "Destination labels",
                        "Ordered quoted label names; EACH, round robin and side/slot modifiers stay.",
                        FieldType.LABEL,
                        output.labelAccess()
                            .label(0)
                            .getStart(),
                        output.labelAccess()
                            .label(
                                output.labelAccess()
                                    .label()
                                    .size() - 1)
                            .getStop(),
                        null,
                        0,
                        false,
                        "",
                        clause));
                SFMLParser.NumberContext number = level == null ? null : level.number();
                result.add(
                    new Field(
                        "level",
                        "Literal output level",
                        "Source literal, default 15. The existing compiler clamps static levels to 0..15; no new editor clamp is applied.",
                        FieldType.NONNEGATIVE_INTEGER,
                        number == null ? null : number.getStart(),
                        number == null ? null : number.getStop(),
                        number == null ? "15" : null,
                        output.REDSTONE()
                            .getSymbol()
                            .getStartIndex(),
                        true,
                        "",
                        clause));
                return Collections.unmodifiableList(result);
            }
            if (type == Type.TRANSFER || type == Type.PRINT) {
                List<SFMLParser.DataExpressionContext> arguments = type == Type.TRANSFER ? parser.transferStatement()
                    .dataExpression()
                    : parser.printStatement()
                        .dataExpression();
                for (int i = 0; i < arguments.size(); i++) {
                    String key = type == Type.PRINT ? "expression" + i
                        : new String[] { "source", "destination", "resource", "amount" }[i];
                    String name = type == Type.PRINT ? "Message / expression " + (i + 1)
                        : new String[] { "Source", "Destination", "Resource filter / amount", "Amount" }[i];
                    String help = type == Type.PRINT
                        ? "One expression; quote literal messages. Argument order is retained."
                        : i < 2 ? "LABEL expression; .side() selects a face, while .slots() applies only to items."
                            : i == 2
                                ? "Resource selector/key for matching stock, or (with three arguments) an integer amount; TRANSFERF uses mB."
                                : "Integer expression. Omitted amounts keep the existing automatic-stock behavior.";
                    result.add(new Field(key, name, help, FieldType.EXPRESSION, arguments.get(i), clause));
                }
                // Structured Print/Transfer models own any Add/Remove argument edits.
                return Collections.unmodifiableList(result);
            }
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
            SFMLParser.ResourceExclusionContext exclusion;
            Token ioKeyword;
            TerminalNode each;
            SFMLParser.EmptyslotsContext empty = null;
            if (type == Type.INPUT) {
                SFMLParser.InputStatementContext input = parser.inputStatement();
                labels = input.labelAccess();
                ioKeyword = input.INPUT()
                    .getSymbol();
                each = input.EACH();
                exclusion = input.resourceExclusion();
                if (input.inputResourceLimits() != null) limits = input.inputResourceLimits()
                    .resourceLimitList();
            } else if (type == Type.OUTPUT) {
                SFMLParser.OutputStatementContext output = parser.outputStatement();
                labels = output.labelAccess();
                ioKeyword = output.OUTPUT()
                    .getSymbol();
                each = output.EACH();
                empty = output.emptyslots();
                exclusion = output.resourceExclusion();
                if (output.outputResourceLimits() != null) limits = output.outputResourceLimits()
                    .resourceLimitList();
            } else return Collections.emptyList();
            // Large groups remain completely manual; never truncate a program's label list.
            if (labels.label()
                .isEmpty()
                || labels.label()
                    .size() > StudioLabelRows.MAX_ROWS)
                return Collections.emptyList();
            result.add(
                new Field(
                    "label",
                    type == Type.INPUT ? "Source label" : "Destination label",
                    "Ordered quoted label names. Duplicates/case are retained; this edits references, not block bindings.",
                    FieldType.LABEL,
                    labels.label(0)
                        .getStart(),
                    labels.label(
                        labels.label()
                            .size() - 1)
                        .getStop(),
                    null,
                    0,
                    false,
                    "",
                    clause));
            // Full clause rows keep each independent allowance together; single-limit typed controls stay unchanged.
            if (limits != null && limits.resourceLimit()
                .size() >= 2 && StudioLimitRows.supports(text(limits, clause))) {
                result.add(
                    new Field(
                        "limits",
                        "Independent limits",
                        "One complete SFML limit per row. OR shares only that row's allowance; first matching row wins.",
                        FieldType.INDEPENDENT_LIMITS,
                        limits,
                        clause));
            }
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
            // OR alternatives share one limit. Never collapse comma-separated independent allowances.
            SFMLParser.ResourceLimitContext resourceLimit = limits == null ? null
                : limits.resourceLimit()
                    .size() == 1 && limits.COMMA()
                        .isEmpty() ? limits.resourceLimit(0) : null;
            SFMLParser.ResourceIdDisjunctionContext selectors = resourceLimit == null ? null
                : resourceLimit.resourceIdDisjunction();
            if (limits == null || resourceLimit != null && (selectors == null || selectors.resourceId()
                .size() <= StudioResourceAlternatives.MAX_ROWS
                && selectors.OR()
                    .size()
                    == selectors.resourceId()
                        .size() - 1)) {
                SFMLParser.ResourceIdDisjunctionContext resource = selectors;
                int anchor = resource != null ? resource.getStart()
                    .getStartIndex()
                    : resourceLimit == null ? ioKeyword.getStopIndex() + 1
                        : resourceLimit.with() != null ? resourceLimit.with()
                            .getStart()
                            .getStartIndex()
                            : resourceLimit.limit()
                                .getStop()
                                .getStopIndex() + 1;
                result.add(
                    new Field(
                        "resource",
                        "Resources (OR, optional)",
                        "0â€“32 OR selectors share one allowance, retention and ore filter. Zero rows matches all resources. Blank rows block Apply; Remove deletes. Item picker only; fluids/metadata stay manual.",
                        FieldType.OPTIONAL_RESOURCE,
                        resource == null ? null : resource.getStart(),
                        resource == null ? null : resource.getStop(),
                        resource == null ? "" : null,
                        anchor,
                        resourceLimit != null && resourceLimit.with() != null,
                        "",
                        clause));
                SFMLParser.WithContext filter = resourceLimit == null ? null : resourceLimit.with();
                result.add(
                    new Field(
                        "oreFilter",
                        "OreDictionary filter (items only)",
                        "Off removes the filter; With/Without requires one name pattern. * wildcard, ASCII case-insensitive. Unknown names are valid. Fluids ignore this filter; no modern tag mapping.",
                        FieldType.ORE_FILTER,
                        filter == null ? null : filter.getStart(),
                        filter == null ? null : filter.getStop(),
                        filter == null ? "" : null,
                        resourceLimit == null ? ioKeyword.getStopIndex() + 1
                            : resourceLimit.getStop()
                                .getStopIndex() + 1,
                        false,
                        "",
                        clause));
            }
            // EXCEPT is global to IO, independent of inclusion limits or ore-filter eligibility.
            if (exclusion == null || exclusion.resourceIdList()
                .resourceId()
                .size() <= StudioExclusionRows.MAX_ROWS
                && exclusion.resourceIdList()
                    .COMMA()
                    .size()
                    == exclusion.resourceIdList()
                        .resourceId()
                        .size() - 1) {
                SFMLParser.ResourceIdListContext excluded = exclusion == null ? null : exclusion.resourceIdList();
                result.add(
                    new Field(
                        "exclusion",
                        "Exclusions (optional)",
                        "0â€“32 ordered global EXCEPT rows. Blank/invalid rows block Apply; Remove the last row to remove EXCEPT. Item picker only; fluids/metadata stay manual. Inclusion limits and ore filters are unchanged.",
                        FieldType.OPTIONAL_EXCLUSION,
                        excluded == null ? null : excluded.getStart(),
                        excluded == null ? null : excluded.getStop(),
                        excluded == null ? "" : null,
                        limits == null ? ioKeyword.getStopIndex() + 1
                            : limits.getStop()
                                .getStopIndex() + 1,
                        false,
                        "",
                        clause));
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
        return update(type, clause, fieldKey, value, false);
    }

    private static Field namedHeaderField(SFMLParser.NamedRedstoneTriggerContext trigger, String clause) {
        return new Field(
            "header",
            "Trigger header",
            "Manual header: use EVERY REDSTONE RISING, PULSE or FALLING FROM labels [physical SIDE]. Leave out DO/END.",
            FieldType.TRIGGER_HEADER,
            trigger.getStart(),
            trigger.labelAccess()
                .getStop(),
            null,
            0,
            false,
            "",
            clause);
    }

    /** Row edits replace the list span together. The legacy single-selector entry point remains available. */
    public static String updateExclusions(Type type, String clause, String value) {
        return update(type, clause, "exclusion", value, true);
    }

    public static String updateResourceAlternatives(Type type, String clause, String value) {
        if (type != Type.INPUT && type != Type.OUTPUT && type != Type.IF)
            throw new IllegalArgumentException("Resource alternatives belong to IO or simple HAS nodes");
        return update(type, clause, "resource", value, true);
    }

    /** Print rows own the entire argument list; scalar aliases cannot overwrite them afterward. */
    public static String updatePrintArguments(Type type, String clause, StudioPrintArguments arguments) {
        if (type != Type.PRINT || arguments == null) throw new IllegalArgumentException("Arguments belong to PRINT");
        String result = arguments.applyTo(clause);
        BlueprintCompiler.validateClause(Type.PRINT, result);
        return result;
    }

    public static String updateTransferArguments(Type type, String clause, StudioTransferArguments arguments) {
        if (type != Type.TRANSFER || arguments == null)
            throw new IllegalArgumentException("Arguments belong to TRANSFER");
        String result = arguments.applyTo(clause);
        BlueprintCompiler.validateClause(Type.TRANSFER, result);
        return result;
    }

    /** Replace the whole parsed list last, without relying on the inspector's initial multi-limit eligibility. */
    public static String updateIndependentLimits(Type type, String clause, String value) {
        return updateLimits(type, clause, value, false);
    }

    /** Explicit promotion only; ordinary field/list setters must not silently create independent allowances. */
    public static String promoteSingleLimit(Type type, String clause, String value) {
        return updateLimits(type, clause, value, true);
    }

    /** One coupled optional prefix, only on an existing supported static single limit. */
    public static String updateSingleAllowances(Type type, String clause, StudioIoAllowances allowances) {
        if (allowances == null) throw new IllegalArgumentException("Missing allowance controls");
        allowances.validate();
        SFMLParser.ResourceLimitListContext list = ioLimits(type, clause);
        if (list == null) throw new IllegalArgumentException("Numbers need an existing explicit limit");
        String current = text(list, clause), changed = allowances.applyTo(current);
        return current.equals(changed) ? clause : replaceIoLimits(type, clause, list, changed);
    }

    /** Explicit absent-list path; never broadens the existing single-limit replacement API. */
    public static boolean hasAbsentIoLimit(Type type, String clause) {
        return ioLimits(type, clause) == null;
    }

    public static String updateAbsentAllowances(Type type, String clause, StudioIoAllowances allowances) {
        if (allowances == null) throw new IllegalArgumentException("Missing allowance controls");
        if (!hasAbsentIoLimit(type, clause)) throw new IllegalArgumentException("Numbers need an absent limit list");
        String prefix = allowances.absentPrefix();
        if (prefix.isEmpty()) return clause; // Disabled latent text must not materialize a match-all row.
        SFMLParser parser = parser(clause);
        Token keyword = type == Type.INPUT ? parser.inputStatement()
            .INPUT()
            .getSymbol()
            : parser.outputStatement()
                .OUTPUT()
                .getSymbol();
        int insertion = clause.offsetByCodePoints(0, keyword.getStopIndex() + 1);
        return replaceIoSpan(type, clause, insertion, insertion, prefix);
    }

    /** Extract one complete static limit, preserving its exact source spelling and omitted defaults. */
    public static String singleLimitText(Type type, String clause) {
        SFMLParser.ResourceLimitListContext list = ioLimits(type, clause);
        if (list == null) throw new IllegalArgumentException("No explicit limit; add one in the full clause first");
        StudioLimitRows rows = new StudioLimitRows(text(list, clause));
        if (rows.size() != 1) throw new IllegalArgumentException("Conversion needs exactly one static limit");
        return rows.source();
    }

    private static SFMLParser.ResourceLimitListContext ioLimits(Type type, String clause) {
        if (type != Type.INPUT && type != Type.OUTPUT) throw new IllegalArgumentException("Limits belong to IO nodes");
        StudioLimitRows.requireSafeSyntax(clause);
        BlueprintCompiler.validateClause(type, clause);
        SFMLParser parser = parser(clause);
        if (type == Type.INPUT) {
            SFMLParser.InputResourceLimitsContext limits = parser.inputStatement()
                .inputResourceLimits();
            return limits == null ? null : limits.resourceLimitList();
        } else {
            SFMLParser.OutputResourceLimitsContext limits = parser.outputStatement()
                .outputResourceLimits();
            return limits == null ? null : limits.resourceLimitList();
        }
    }

    private static String updateLimits(Type type, String clause, String value, boolean promotion) {
        StudioLimitRows replacement = new StudioLimitRows(value);
        SFMLParser.ResourceLimitListContext list = ioLimits(type, clause);
        if (list == null) throw new IllegalArgumentException("No independent limit list to edit");
        StudioLimitRows original = new StudioLimitRows(text(list, clause));
        if (promotion && original.size() != 1)
            throw new IllegalArgumentException("Conversion needs exactly one static limit");
        if (!promotion && original.size() == 1 && replacement.size() > 1)
            throw new IllegalArgumentException("Use the full clause to create a multi-limit list");
        for (StudioLimitRows.Row row : replacement.rows()) StudioLimitRows.validateRow(type, row.text);
        String current = text(list, clause);
        if (current.equals(replacement.source())) return clause;
        return replaceIoLimits(type, clause, list, replacement.source());
    }

    private static String replaceIoLimits(Type type, String clause, SFMLParser.ResourceLimitListContext list,
        String changed) {
        if (changed.length() > BlueprintGraph.MAX_CLAUSE)
            throw new IllegalArgumentException("Limit exceeds 4096 characters");
        int start = clause.offsetByCodePoints(
            0,
            list.getStart()
                .getStartIndex());
        int end = clause.offsetByCodePoints(
            0,
            list.getStop()
                .getStopIndex() + 1);
        return replaceIoSpan(type, clause, start, end, changed);
    }

    private static String replaceIoSpan(Type type, String clause, int start, int end, String changed) {
        String prefix = clause.substring(0, start), suffix = clause.substring(end);
        String result = prefix + changed + suffix, separated = prefix + " " + changed + " " + suffix;
        // Quoted lists can touch IO keywords; introduce only separators needed to retain lexer boundaries.
        if (!BlueprintCompiler.sameTokens(separated, result)) {
            result = prefix + " " + changed + suffix;
            if (!BlueprintCompiler.sameTokens(separated, result)) {
                result = prefix + changed + " " + suffix;
                if (!BlueprintCompiler.sameTokens(separated, result)) result = separated;
            }
        }
        if (result.length() > BlueprintGraph.MAX_CLAUSE)
            throw new IllegalArgumentException("Clause exceeds 4096 characters");
        BlueprintCompiler.validateClause(type, result);
        return result;
    }

    private static String update(Type type, String clause, String fieldKey, String value, boolean rows) {
        if (value == null || value.length() > BlueprintGraph.MAX_CLAUSE
            || value.indexOf('\n') >= 0
            || value.indexOf('\r') >= 0
            || value.indexOf('\0') >= 0) throw new IllegalArgumentException("A property must be one bounded line");
        Field selected = null;
        for (Field field : inspect(type, clause)) if (field.key.equals(fieldKey)) selected = field;
        // Preserve the stateless raw-header API without giving structured drafts two overlapping owners.
        if (selected == null && type == Type.NAMED_REDSTONE && "header".equals(fieldKey)) {
            BlueprintCompiler.validateClause(type, clause);
            selected = namedHeaderField(StudioNamedRedstoneHeader.parse(clause), clause);
        }
        if (selected == null) throw new IllegalArgumentException("This property requires the full-clause editor");
        if (selected.type == FieldType.PHYSICAL_SIDES)
            return StudioNamedRedstoneHeader.updatePhysicalSides(clause, value);
        if (selected.type == FieldType.INDEPENDENT_LIMITS) return updateIndependentLimits(type, clause, value);
        String replacement = value.trim();
        if (replacement.equals(selected.value)) return clause;
        if ((selected.type == FieldType.BOOLEAN || selected.type == FieldType.ROUND_ROBIN
            || selected.type == FieldType.REDSTONE_EDGE) && replacement.equalsIgnoreCase(selected.value)) return clause;
        try {
            if (selected.type == FieldType.NONNEGATIVE_INTEGER) {
                if (!replacement.matches("[0-9]+") || Long.parseLong(replacement) < 0)
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
            } else if (selected.type == FieldType.OPTIONAL_EXCLUSION) {
                if (!replacement.isEmpty()) {
                    if (rows) StudioExclusionRows.parse(replacement);
                    else StudioExclusionRows.validateLiteral(replacement);
                }
            } else if (selected.type == FieldType.ORE_FILTER) {
                if (!replacement.isEmpty()) {
                    StudioOreFilter.parse(replacement);
                    if (!selected.value.isEmpty())
                        replacement = StudioOreFilter.replaceExisting(selected.value, replacement);
                }
            } else if (selected.type == FieldType.TRIGGER_HEADER) {
                StudioNamedRedstoneHeader.parse(replacement);
            } else if (selected.type == FieldType.REDSTONE_EDGE) {
                if (!replacement.equalsIgnoreCase("RISING") && !replacement.equalsIgnoreCase("PULSE")
                    && !replacement.equalsIgnoreCase("FALLING"))
                    throw new IllegalArgumentException("Use RISING, PULSE, or FALLING");
            } else if (selected.type == FieldType.VARIABLE_NAME) {
                SFMLParser parser = parser(replacement);
                parser.variableName();
                requireEnd(parser);
            } else if (selected.type == FieldType.ASSIGNMENT_TARGET) {
                StudioExpressionSyntax.expression(replacement);
                SFMLParser parser = parser(replacement);
                parser.dataTarget();
                requireEnd(parser);
            } else if (selected.type == FieldType.CONDITION) {
                StudioConditionSyntax.parse(replacement);
            } else {
                if (selected.type == FieldType.EXPRESSION) StudioExpressionSyntax.expression(replacement);
                if (selected.type == FieldType.EXPRESSION && type == Type.TRANSFER)
                    StudioExpressionSyntax.transferExpression(replacement);
                SFMLParser parser = parser(replacement);
                if (selected.type == FieldType.LABEL && (type == Type.INPUT || type == Type.OUTPUT
                    || type == Type.IF
                    || type == Type.REDSTONE
                    || type == Type.NAMED_REDSTONE)) {
                    StudioLabelRows.parse(replacement);
                    parser.labelAccess();
                } else if (selected.type == FieldType.LABEL) parser.label();
                else if (selected.type == FieldType.EXPRESSION) {
                    parser.dataExpression();
                } else if (selected.type == FieldType.COMPARISON) parser.comparisonOp();
                else if (selected.type == FieldType.OPTIONAL_RESOURCE) {
                    if (rows) {
                        StudioResourceAlternatives.parse(replacement);
                        if (!replacement.isEmpty()) parser.resourceIdDisjunction();
                    } else if (!replacement.isEmpty()) parser.resourceId();
                } else parser.interval();
                requireEnd(parser);
            }
        } catch (org.antlr.v4.runtime.misc.ParseCancellationException | NumberFormatException invalid) {
            throw new IllegalArgumentException("Invalid " + selected.name.toLowerCase(java.util.Locale.ROOT), invalid);
        }
        int start = selected.start;
        if (selected.type == FieldType.OPTIONAL_EXCLUSION) {
            if (replacement.isEmpty()) {
                SFMLParser parser = parser(clause);
                SFMLParser.ResourceExclusionContext exclusion = type == Type.INPUT ? parser.inputStatement()
                    .resourceExclusion()
                    : parser.outputStatement()
                        .resourceExclusion();
                start = clause.offsetByCodePoints(
                    0,
                    exclusion.getStart()
                        .getStartIndex());
            } else if (selected.value.isEmpty()) replacement = "EXCEPT " + replacement;
        }
        if (!replacement.isEmpty()) replacement = selected.insertionPrefix + replacement + selected.insertionSuffix;
        String prefix = clause.substring(0, start), suffix = clause.substring(selected.end);
        String result = prefix + replacement + suffix;
        if ((selected.type == FieldType.OPTIONAL_RESOURCE || selected.type == FieldType.LABEL
            || selected.type == FieldType.ORE_FILTER
            || selected.type == FieldType.OPTIONAL_EXCLUSION)
            && (type == Type.INPUT || type == Type.OUTPUT
                || type == Type.IF && (selected.type == FieldType.LABEL || selected.type == FieldType.OPTIONAL_RESOURCE)
                || (type == Type.REDSTONE || type == Type.NAMED_REDSTONE) && selected.type == FieldType.LABEL)) {
            // A quoted selector may touch keywords. Compare actual lexer tokens and add only needed separators.
            String separated = prefix + " " + replacement + " " + suffix;
            if (!BlueprintCompiler.sameTokens(separated, result)) {
                result = prefix + " " + replacement + suffix;
                if (!BlueprintCompiler.sameTokens(separated, result)) {
                    result = prefix + replacement + " " + suffix;
                    if (!BlueprintCompiler.sameTokens(separated, result)) result = separated;
                }
            }
        }
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

    /** Parsed spans keep names and expressions independent; whole-program validation owns their scope/types. */
    private static List<Field> dataFields(Type type, String clause) {
        SFMLParser parser = parser(clause + (type.isLoop() ? " DO END" : ""));
        List<Field> fields = new ArrayList<>();
        if (type == Type.WHILE) {
            fields.add(
                new Field(
                    "condition",
                    "While condition",
                    "Rechecked before each iteration; must be boolean.",
                    FieldType.CONDITION,
                    parser.whileStatement()
                        .boolexpr(),
                    clause));
            return fields;
        }
        if (type == Type.FOR || type == Type.FOREACH) {
            SFMLParser.ForStatementContext loop = parser.forStatement();
            if (loop instanceof SFMLParser.CountedForContext) {
                SFMLParser.CountedForContext counted = (SFMLParser.CountedForContext) loop;
                fields.add(
                    new Field(
                        "name",
                        "Loop variable",
                        "Read-only inside the loop body.",
                        FieldType.VARIABLE_NAME,
                        counted.variableName(),
                        clause));
                String[] keys = { "start", "end", "step" };
                String[] names = { "First value", "Last value (inclusive)", "Step" };
                for (int i = 0; i < counted.dataExpression()
                    .size(); i++)
                    fields.add(
                        new Field(
                            keys[i],
                            names[i],
                            "Integer expression evaluated in the enclosing scope.",
                            FieldType.EXPRESSION,
                            counted.dataExpression(i),
                            clause));
            } else {
                SFMLParser.ArrayForContext array = (SFMLParser.ArrayForContext) loop;
                fields.add(
                    new Field(
                        "name",
                        "Element variable",
                        "Read-only element value inside the body.",
                        FieldType.VARIABLE_NAME,
                        array.variableName(),
                        clause));
                fields.add(
                    new Field(
                        "values",
                        "Array or contents",
                        "Iterate the captured sequence in order.",
                        FieldType.EXPRESSION,
                        array.dataExpression(),
                        clause));
            }
        } else {
            ParserRuleContext target;
            SFMLParser.DataExpressionContext value;
            if (type == Type.STATE) {
                SFMLParser.StateDeclarationContext declaration = parser.stateDeclaration();
                target = declaration.variableName();
                value = declaration.dataExpression();
            } else if (type == Type.SET) {
                SFMLParser.DataAssignmentContext assignment = parser.dataAssignment();
                target = assignment.dataTarget();
                value = assignment.dataExpression();
            } else {
                SFMLParser.DataDeclarationContext declaration = parser.dataDeclaration();
                target = declaration.variableName();
                value = declaration.dataExpression();
            }
            fields.add(
                new Field(
                    "name",
                    type == Type.SET ? "Target" : "Variable name",
                    type == Type.SET ? "Writable variable, array element or struct field."
                        : "Case-sensitive; the $ prefix is optional.",
                    type == Type.SET ? FieldType.ASSIGNMENT_TARGET : FieldType.VARIABLE_NAME,
                    target,
                    clause));
            fields.add(
                new Field(
                    "value",
                    type == Type.SET ? "New value" : "Initial value",
                    type == Type.STATE ? "Persistable constant default, used when state is first initialized."
                        : "Expression using variables available at this point.",
                    FieldType.EXPRESSION,
                    value,
                    clause));
        }
        return Collections.unmodifiableList(fields);
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
