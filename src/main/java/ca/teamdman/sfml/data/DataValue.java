package ca.teamdman.sfml.data;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Immutable scalar/container values. Sharing never exposes a mutable child array. */
public final class DataValue {

    private static final DataValue TRUE = new DataValue(DataType.BOOLEAN, 1, null);
    private static final DataValue FALSE = new DataValue(DataType.BOOLEAN, 0, null);
    private static final DataValue[] SMALL = new DataValue[385];
    static {
        for (int i = 0; i < SMALL.length; i++) SMALL[i] = new DataValue(DataType.INTEGER, i - 128, null);
    }
    private final DataType type;
    private final long scalar;
    private final DataValue[] children;
    private final String text;
    private final int stringCharacterCount, stringNbtBytes;

    private DataValue(DataType type, long scalar, DataValue[] children) {
        this(type, scalar, children, null, countStrings(children, false), countStrings(children, true));
    }

    private DataValue(DataType type, long scalar, DataValue[] children, String text, int characters, int nbtBytes) {
        this.type = type;
        this.scalar = scalar;
        this.children = children;
        this.text = text;
        this.stringCharacterCount = characters;
        this.stringNbtBytes = nbtBytes;
    }

    private static int countStrings(DataValue[] children, boolean bytes) {
        int count = 0;
        if (children != null) for (DataValue child : children)
            count = Math.addExact(count, bytes ? child.stringNbtBytes : child.stringCharacterCount);
        return count;
    }

    public static DataValue integer(long value) {
        return value >= -128 && value <= 256 ? SMALL[(int) value + 128] : new DataValue(DataType.INTEGER, value, null);
    }

    public static DataValue bool(boolean value) {
        return value ? TRUE : FALSE;
    }

    public static DataValue string(String value) {
        if (value == null) throw new IllegalArgumentException("String value required");
        if (value.length() > ExpressionLimits.MAX_STRING_LENGTH)
            throw new IllegalArgumentException("String length exceeds " + ExpressionLimits.MAX_STRING_LENGTH);
        int bytes = 0;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isHighSurrogate(c)) {
                if (i + 1 >= value.length() || !Character.isLowSurrogate(value.charAt(i + 1)))
                    throw new IllegalArgumentException("Unpaired Unicode surrogate in string");
            } else if (Character.isLowSurrogate(c) && (i == 0 || !Character.isHighSurrogate(value.charAt(i - 1))))
                throw new IllegalArgumentException("Unpaired Unicode surrogate in string");
            // NBT uses Java modified UTF-8: NUL takes two bytes and each surrogate takes three.
            bytes += c >= 1 && c <= 0x7f ? 1 : c <= 0x7ff ? 2 : 3;
        }
        return new DataValue(DataType.STRING, 0, null, value, value.length(), bytes);
    }

    public String asString() {
        if (type != DataType.STRING) throw new IllegalArgumentException("String required");
        return text;
    }

    /** Aggregate logical UTF-16 payload, including repeated references in containers. */
    public int getStringCharacterCount() {
        return stringCharacterCount;
    }

    /** Aggregate modified UTF-8 payload, excluding NBT tag headers and length prefixes. */
    public int getStringNbtBytes() {
        return stringNbtBytes;
    }

    public static DataValue array(List<DataValue> values) {
        if (values.isEmpty()) throw new IllegalArgumentException("Empty array has no inferable element type");
        DataType type = DataType.array(values.get(0).type, values.size());
        for (DataValue value : values) if (!type.getElementType()
            .equals(value.type)) throw new IllegalArgumentException("Array elements must have one type/shape");
        return new DataValue(type, 0, values.toArray(new DataValue[0]));
    }

    public static DataValue struct(Map<String, DataValue> values) {
        TreeMap<String, DataType> types = new TreeMap<>();
        for (Map.Entry<String, DataValue> entry : values.entrySet()) types.put(entry.getKey(), entry.getValue().type);
        DataType type = DataType.struct(types);
        DataValue[] data = new DataValue[type.getLength()];
        for (int i = 0; i < data.length; i++) data[i] = values.get(
            type.getFieldNames()
                .get(i));
        return new DataValue(type, 0, data);
    }

    /** Typed construction used by compiled literals; caller array is copied. */
    public static DataValue collection(DataType type, DataValue[] values) {
        return ownedCollection(type, values.clone());
    }

    static DataValue ownedCollection(DataType type, DataValue[] values) {
        if (type.getKind() != DataType.Kind.ARRAY && type.getKind() != DataType.Kind.STRUCT)
            throw new IllegalArgumentException("Collection type required");
        if (values.length != type.getLength()) throw new IllegalArgumentException("Collection shape mismatch");
        for (int i = 0; i < values.length; i++) {
            DataType expected = type.getKind() == DataType.Kind.ARRAY ? type.getElementType() : type.getFieldType(i);
            if (values[i] == null || !expected.equals(values[i].type))
                throw new IllegalArgumentException("Collection type mismatch");
        }
        return new DataValue(type, 0, values);
    }

    public DataType getType() {
        return type;
    }

    public long asLong() {
        if (type != DataType.INTEGER) throw new IllegalArgumentException("Integer required");
        return scalar;
    }

    public boolean asBoolean() {
        if (type != DataType.BOOLEAN) throw new IllegalArgumentException("Boolean required");
        return scalar != 0;
    }

    public int size() {
        if (children == null) throw new IllegalArgumentException("Collection required");
        return children.length;
    }

    public DataValue get(int index) {
        if (children == null || index < 0 || index >= children.length)
            throw new IllegalArgumentException("Index out of range: " + index);
        return children[index];
    }

    public DataValue get(String name) {
        return get(type.fieldIndex(name));
    }

    public DataValue with(int index, DataValue replacement) {
        DataValue old = get(index);
        if (!old.type.equals(replacement.type)) throw new IllegalArgumentException("Assignment type/shape mismatch");
        DataValue[] copy = children.clone();
        copy[index] = replacement;
        // Ancestor updates use cached payload deltas; they never scan nested string contents.
        return new DataValue(
            type,
            0,
            copy,
            null,
            stringCharacterCount - old.stringCharacterCount + replacement.stringCharacterCount,
            stringNbtBytes - old.stringNbtBytes + replacement.stringNbtBytes);
    }

    @Override
    public String toString() {
        if (type == DataType.INTEGER) return Long.toString(scalar);
        if (type == DataType.BOOLEAN) return scalar == 0 ? "FALSE" : "TRUE";
        if (type == DataType.STRING) return quote(text);
        StringBuilder out = new StringBuilder(type.getKind() == DataType.Kind.ARRAY ? "[" : "{");
        for (int i = 0; i < children.length; i++) {
            if (i > 0) out.append(", ");
            if (type.getKind() == DataType.Kind.STRUCT) out.append(
                type.getFieldNames()
                    .get(i))
                .append(": ");
            out.append(children[i]);
        }
        return out.append(type.getKind() == DataType.Kind.ARRAY ? ']' : '}')
            .toString();
    }

    private static String quote(String value) {
        StringBuilder out = new StringBuilder(value.length() + 2).append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    out.append("\\\"");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                case '\t':
                    out.append("\\t");
                    break;
                case '\b':
                    out.append("\\b");
                    break;
                case '\f':
                    out.append("\\f");
                    break;
                default:
                    if (c < 0x20 || c == 0x7f) {
                        out.append("\\u");
                        for (int shift = 12; shift >= 0; shift -= 4)
                            out.append("0123456789abcdef".charAt((c >>> shift) & 15));
                    } else out.append(c);
            }
        }
        return out.append('"')
            .toString();
    }
}
