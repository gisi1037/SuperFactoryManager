package ca.teamdman.sfm.common.program;

import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.LabelReference;

/** Bounded display only: never converts an entire collection or arbitrary value to a string. */
public final class DebugValueFormatter {

    public static final int MAX_DEPTH = 6;
    public static final int MAX_CELLS = 64;
    private static final String[] SIDES = { "down", "up", "north", "south", "west", "east" };
    private final StringBuilder out = new StringBuilder(128);
    private final DataRuntime runtime;
    private int cells;
    private boolean truncated;

    private DebugValueFormatter(DataRuntime runtime) {
        this.runtime = runtime;
    }

    public static String format(DataValue... values) {
        return format(null, values);
    }

    public static String format(DataRuntime runtime, DataValue... values) {
        return format(runtime, false, values);
    }

    /** Conventional PRINT text for top-level strings; collection strings retain quotes. */
    public static String formatPrint(DataRuntime runtime, DataValue... values) {
        return format(runtime, true, values);
    }

    private static String format(DataRuntime runtime, boolean rawStrings, DataValue... values) {
        DebugValueFormatter formatter = new DebugValueFormatter(runtime);
        for (int i = 0; i < values.length && !formatter.truncated; i++) {
            if (i > 0) formatter.add(" ");
            formatter.value(values[i], 0, rawStrings);
        }
        return formatter.finish();
    }

    /** Also bounds untrusted command/provider text before history storage. */
    public static String sanitize(String text) {
        DebugValueFormatter formatter = new DebugValueFormatter(null);
        formatter.text(text == null ? "null" : text, false);
        return formatter.finish();
    }

    private void work(int amount) {
        if (runtime != null) runtime.work(amount);
    }

    private void add(String text) {
        if (truncated) return;
        if (text.length() > DebugConsole.MAX_LINE_LENGTH - 1 - out.length()) {
            truncated = true;
            return;
        }
        work(text.length());
        out.append(text);
    }

    private void text(String text, boolean quoted) {
        if (quoted) add("\"");
        for (int i = 0; i < text.length() && !truncated; i++) {
            char c = text.charAt(i);
            work(1);
            if (Character.isHighSurrogate(c)) {
                if (i + 1 < text.length() && Character.isLowSurrogate(text.charAt(i + 1))) {
                    if (Character.getType(Character.toCodePoint(c, text.charAt(i + 1))) == Character.FORMAT) {
                        work(1);
                        add("\\u" + Integer.toHexString(c) + "\\u" + Integer.toHexString(text.charAt(++i)));
                        continue;
                    }
                    if (out.length() > DebugConsole.MAX_LINE_LENGTH - 3) {
                        truncated = true;
                        break;
                    }
                    work(3);
                    out.append(c)
                        .append(text.charAt(++i));
                } else add("\\ufffd");
            } else if (Character.isLowSurrogate(c)) add("\\ufffd");
            else if (c == '\n') add("\\n");
            else if (c == '\r') add("\\r");
            else if (c == '\t') add("\\t");
            else if (quoted && c == '\\') add("\\\\");
            else if (quoted && c == '"') add("\\\"");
            else if (Character.isISOControl(c) || Character.getType(c) == Character.FORMAT
                || c == '\u2028'
                || c == '\u2029'
                || c == '\u00a7') {
                    String hex = Integer.toHexString(c);
                    add("\\u" + "0000".substring(hex.length()) + hex);
                } else {
                    if (out.length() >= DebugConsole.MAX_LINE_LENGTH - 1) truncated = true;
                    else {
                        work(1);
                        out.append(c);
                    }
                }
        }
        if (quoted) add("\"");
    }

    private void value(DataValue value, int depth, boolean rawString) {
        if (truncated) return;
        if (cells >= MAX_CELLS) {
            truncated = true;
            return;
        }
        cells++;
        work(1);
        DataType type = value.getType();
        switch (type.getKind()) {
            case INTEGER:
                add(Long.toString(value.asLong()));
                return;
            case BOOLEAN:
                add(value.asBoolean() ? "TRUE" : "FALSE");
                return;
            case STRING:
                text(value.asString(), !rawString);
                return;
            case LABEL_REF:
                LabelReference label = value.asLabel();
                add("LABEL(");
                text(label.getName(), true);
                add(")");
                if (label.getSideMask() != 63) {
                    add(".side(");
                    text(SIDES[Integer.numberOfTrailingZeros(label.getSideMask())], true);
                    add(")");
                }
                if (label.getFirstSlot() != 0 || label.getLastSlot() != Integer.MAX_VALUE)
                    add(".slots(" + label.getFirstSlot() + ", " + label.getLastSlot() + ")");
                return;
            case RESOURCE_SELECTOR:
                add(
                    value.asSelector()
                        .getKind()
                        .name() + "(");
                text(
                    value.asSelector()
                        .getId(),
                    true);
                add(")");
                return;
            case ITEM_KEY:
                ItemIdentity item = value.asItemIdentity();
                add("ITEM_KEY(");
                text(item.getRegistryId(), true);
                add(", " + item.getMetadata() + ")");
                return;
            default:
                if (depth >= MAX_DEPTH) {
                    add("...");
                    return;
                }
                boolean array = type.isSequence();
                add(array ? "[" : "{");
                for (int i = 0; i < value.size() && !truncated; i++) {
                    if (cells >= MAX_CELLS) {
                        truncated = true;
                        break;
                    }
                    if (i > 0) add(", ");
                    if (!array) {
                        text(
                            type.getFieldNames()
                                .get(i),
                            false);
                        add(": ");
                    }
                    value(value.get(i), depth + 1, false);
                }
                add(array ? "]" : "}");
        }
    }

    private String finish() {
        if (truncated) out.append('\u2026');
        return out.toString();
    }
}
