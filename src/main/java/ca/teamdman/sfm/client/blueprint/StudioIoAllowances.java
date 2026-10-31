package ca.teamdman.sfm.client.blueprint;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import ca.teamdman.langs.SFMLParser;

/**
 * Two optional numeric blocks in one static IO limit. Disabled controls keep latent text/EACH until Apply or Cancel.
 */
public final class StudioIoAllowances {

    public enum Part {
        AMOUNT,
        RETAIN
    }

    private static final class Number {

        boolean enabled;
        String literal = "", each = "false";

        Number() {}

        Number(Number other) {
            enabled = other.enabled;
            literal = other.literal;
            each = other.each;
        }
    }

    private final Number amount, retain;

    private StudioIoAllowances() {
        amount = new Number();
        retain = new Number();
    }

    /** An absent limit has no enabled numbers or invented literal defaults. */
    public static StudioIoAllowances absent() {
        return new StudioIoAllowances();
    }

    public StudioIoAllowances(String row) {
        SFMLParser.ResourceLimitContext parsed = single(row);
        amount = read(row, quantity(parsed), Part.AMOUNT);
        retain = read(row, retention(parsed), Part.RETAIN);
    }

    public StudioIoAllowances(StudioIoAllowances other) {
        amount = new Number(other.amount);
        retain = new Number(other.retain);
    }

    public boolean enabled(Part part) {
        return number(part).enabled;
    }

    public String literal(Part part) {
        return number(part).literal;
    }

    public boolean each(Part part) {
        return Boolean.parseBoolean(number(part).each);
    }

    public boolean setEnabled(Part part, boolean value) {
        Number number = number(part);
        if (number.enabled == value) return false;
        number.enabled = value;
        return true;
    }

    public boolean setLiteral(Part part, String value) {
        bounded(value);
        Number number = number(part);
        if (number.literal.equals(value)) return false;
        number.literal = value;
        return true;
    }

    public boolean setEach(Part part, boolean value) {
        return setEachText(part, Boolean.toString(value));
    }

    // Legacy pending setters retain invalid text for Apply to reject, just like their numeric cells.
    boolean setEachText(Part part, String value) {
        bounded(value);
        Number number = number(part);
        if (number.each.equals(value)) return false;
        number.each = value;
        return true;
    }

    public boolean sameContent(StudioIoAllowances other) {
        if (other == null) return false;
        for (Part part : Part.values()) {
            Number a = number(part), b = other.number(part);
            if (a.enabled != b.enabled || !a.literal.equals(b.literal) || !a.each.equals(b.each)) return false;
        }
        return true;
    }

    public void validate() {
        for (Part part : Part.values()) {
            Number number = number(part);
            if (!number.enabled) continue; // Latent values are preserved locally, never emitted while disabled.
            if (number.literal.indexOf('\n') >= 0 || number.literal.indexOf('\r') >= 0
                || number.literal.indexOf('\0') >= 0)
                throw new IllegalArgumentException("Enabled numbers must be one line without NUL");
            String value = number.literal.trim();
            try {
                if (!value.matches("[0-9]+") || Long.parseLong(value) < 0) throw new NumberFormatException();
            } catch (NumberFormatException invalid) {
                throw new IllegalArgumentException(
                    "Enabled " + part.name()
                        .toLowerCase(java.util.Locale.ROOT)
                        + " needs a nonnegative signed 64-bit integer; blank blocks Apply",
                    invalid);
            }
            if (!number.each.equalsIgnoreCase("true") && !number.each.equalsIgnoreCase("false"))
                throw new IllegalArgumentException("EACH must be true or false");
        }
    }

    /** Replace the complete prefix once; removing a number also removes its own EACH/RETAIN tokens. */
    public String applyTo(String row) {
        validate();
        SFMLParser.ResourceLimitContext parsed = single(row);
        ParserRuleContext q = quantity(parsed), r = retention(parsed);
        String quantity = render(row, q, amount, Part.AMOUNT), retention = render(row, r, retain, Part.RETAIN);
        String separator = q != null && r != null ? row.substring(end(row, q), start(row, r))
            : !quantity.isEmpty() && !retention.isEmpty() ? " " : "";
        String prefix = quantity + separator + retention;
        if (parsed.limit() == null) return prefix.isEmpty() ? row : prefix + " " + row;
        String tail = row.substring(end(row, parsed.limit()));
        String changed = prefix + tail;
        if (!BlueprintCompiler.sameTokens(prefix + " " + tail, changed)) changed = prefix + " " + tail;
        return changed.trim()
            .isEmpty() ? "" : changed;
    }

    String absentPrefix() {
        validate();
        String quantity = render("", null, amount, Part.AMOUNT);
        String retention = render("", null, retain, Part.RETAIN);
        return quantity + (!quantity.isEmpty() && !retention.isEmpty() ? " " : "") + retention;
    }

    private Number number(Part part) {
        if (part == null) throw new IllegalArgumentException("Choose amount or retention");
        return part == Part.AMOUNT ? amount : retain;
    }

    private static void bounded(String value) {
        if (value == null || value.length() > BlueprintGraph.MAX_CLAUSE)
            throw new IllegalArgumentException("Numeric control exceeds 4096 characters");
    }

    private static Number read(String row, ParserRuleContext context, Part part) {
        Number number = new Number();
        if (context != null) {
            number.enabled = true;
            ParserRuleContext literal = part == Part.AMOUNT ? ((SFMLParser.QuantityContext) context).number()
                : ((SFMLParser.RetentionContext) context).number();
            if (literal == null) throw new IllegalArgumentException("Computed allowances require Text");
            number.literal = row.substring(start(row, literal), end(row, literal));
            number.each = Boolean.toString(each(context, part) != null);
        }
        return number;
    }

    private static String render(String row, ParserRuleContext context, Number desired, Part part) {
        if (!desired.enabled) return "";
        boolean each = Boolean.parseBoolean(desired.each);
        if (context == null)
            return (part == Part.RETAIN ? "RETAIN " : "") + desired.literal.trim() + (each ? " EACH" : "");
        int base = start(row, context);
        StringBuilder block = new StringBuilder(row.substring(base, end(row, context)));
        TerminalNode token = each(context, part);
        if (token != null && !each) {
            int first = row.offsetByCodePoints(
                0,
                token.getSymbol()
                    .getStartIndex());
            int last = row.offsetByCodePoints(
                0,
                token.getSymbol()
                    .getStopIndex() + 1);
            block.delete(first - base, last - base);
        } else if (token == null && each) block.append(" EACH");
        ParserRuleContext literal = part == Part.AMOUNT ? ((SFMLParser.QuantityContext) context).number()
            : ((SFMLParser.RetentionContext) context).number();
        block.replace(start(row, literal) - base, end(row, literal) - base, desired.literal.trim());
        return block.toString();
    }

    private static TerminalNode each(ParserRuleContext context, Part part) {
        return part == Part.AMOUNT ? ((SFMLParser.QuantityContext) context).EACH()
            : ((SFMLParser.RetentionContext) context).EACH();
    }

    private static SFMLParser.ResourceLimitContext single(String row) {
        SFMLParser.ResourceLimitListContext list = StudioLimitRows.parse(row);
        if (list.resourceLimit()
            .size() != 1) throw new IllegalArgumentException("Numbers need one static limit");
        SFMLParser.ResourceLimitContext parsed = list.resourceLimit(0);
        SFMLParser.ResourceIdDisjunctionContext ids = parsed.resourceIdDisjunction();
        if (ids != null && (ids.resourceId()
            .size() > StudioResourceAlternatives.MAX_ROWS
            || ids.OR()
                .size()
                != ids.resourceId()
                    .size() - 1))
            throw new IllegalArgumentException("Unsupported selector group; use the full clause");
        if (parsed.with() != null && !(parsed.with()
            .withClause() instanceof SFMLParser.WithTagContext))
            throw new IllegalArgumentException("Unsupported filter; use the full clause");
        return parsed;
    }

    private static SFMLParser.QuantityContext quantity(SFMLParser.ResourceLimitContext parsed) {
        return child(parsed.limit(), SFMLParser.QuantityContext.class);
    }

    private static SFMLParser.RetentionContext retention(SFMLParser.ResourceLimitContext parsed) {
        return child(parsed.limit(), SFMLParser.RetentionContext.class);
    }

    private static <T> T child(ParserRuleContext parent, Class<T> type) {
        if (parent != null) for (int i = 0; i < parent.getChildCount(); i++)
            if (type.isInstance(parent.getChild(i))) return type.cast(parent.getChild(i));
        return null;
    }

    private static int start(String source, ParserRuleContext context) {
        return source.offsetByCodePoints(
            0,
            context.getStart()
                .getStartIndex());
    }

    private static int end(String source, ParserRuleContext context) {
        return source.offsetByCodePoints(
            0,
            context.getStop()
                .getStopIndex() + 1);
    }
}
