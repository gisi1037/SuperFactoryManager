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
    private final Object opaque;
    private final int stringCharacterCount, stringNbtBytes;
    private final int retainedCellCount, retainedCharacters, retainedOpaqueBytes;

    private DataValue(DataType type, long scalar, DataValue[] children) {
        this(type, scalar, children, null, 0, 0);
    }

    private DataValue(DataType type, long scalar, DataValue[] children, String text, int characters, int nbtBytes) {
        this(type, scalar, children, text, characters, nbtBytes, null);
    }

    private DataValue(DataType type, long scalar, DataValue[] children, String text, int characters, int nbtBytes,
        Object opaque) {
        int cells = 1, retained = characters, opaqueBytes = 0;
        if (type == DataType.LABEL_REF) retained = ((LabelReference) opaque).getName()
            .length();
        else if (type == DataType.RESOURCE_SELECTOR) retained = ((ResourceSelector) opaque).getId()
            .length();
        else if (type == DataType.ITEM_KEY) {
            retained = ((ItemIdentity) opaque).getRetainedCharacters();
            opaqueBytes = ((ItemIdentity) opaque).getRetainedOpaqueBytes();
        }
        if (children != null) for (DataValue child : children) {
            characters = Math.addExact(characters, child.stringCharacterCount);
            nbtBytes = Math.addExact(nbtBytes, child.stringNbtBytes);
            cells = Math.addExact(cells, child.retainedCellCount);
            retained = Math.addExact(retained, child.retainedCharacters);
            opaqueBytes = Math.addExact(opaqueBytes, child.retainedOpaqueBytes);
        }
        this.type = type;
        this.opaque = opaque;
        this.scalar = scalar;
        this.children = children;
        this.text = text;
        this.stringCharacterCount = characters;
        this.stringNbtBytes = nbtBytes;
        this.retainedCellCount = cells;
        this.retainedCharacters = retained;
        this.retainedOpaqueBytes = opaqueBytes;
    }

    /** Ancestor copies already know every aggregate from the replaced child's cached delta. */
    private DataValue(DataType type, DataValue[] children, int characters, int nbtBytes, int cells, int retained,
        int opaqueBytes) {
        this.type = type;
        this.scalar = 0;
        this.children = children;
        this.text = null;
        this.opaque = null;
        this.stringCharacterCount = characters;
        this.stringNbtBytes = nbtBytes;
        this.retainedCellCount = cells;
        this.retainedCharacters = retained;
        this.retainedOpaqueBytes = opaqueBytes;
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

    public static DataValue label(LabelReference label) {
        if (label == null) throw new IllegalArgumentException("Label reference required");
        return new DataValue(DataType.LABEL_REF, 0, null, null, 0, 0, label);
    }

    public static DataValue selector(ResourceSelector selector) {
        if (selector == null) throw new IllegalArgumentException("Resource selector required");
        return new DataValue(DataType.RESOURCE_SELECTOR, 0, null, null, 0, 0, selector);
    }

    public static DataValue itemIdentity(ItemIdentity item) {
        if (item == null) throw new IllegalArgumentException("Item identity required");
        return new DataValue(DataType.ITEM_KEY, 0, null, null, 0, 0, item);
    }

    /** Eager, immutable rows; owner verification and all budgets precede our row/array allocation. */
    public static DataValue contents(ItemContents contents, QueryBudget owner, DataRuntime runtime) {
        return ItemContentsValues.materialize(contents, owner, runtime);
    }

    public ItemIdentity asItemIdentity() {
        if (type != DataType.ITEM_KEY) throw new IllegalArgumentException("Item identity required");
        return (ItemIdentity) opaque;
    }

    public LabelReference asLabel() {
        if (type != DataType.LABEL_REF) throw new IllegalArgumentException("Label reference required");
        return (LabelReference) opaque;
    }

    public ResourceSelector asSelector() {
        if (type != DataType.RESOURCE_SELECTOR) throw new IllegalArgumentException("Resource selector required");
        return (ResourceSelector) opaque;
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

    /** Actual logical cells, including native observation rows and repeated shared children. */
    public int getRetainedCellCount() {
        return retainedCellCount;
    }

    /** UTF-16 payload retained by strings, labels, selectors, item IDs and canonical NBT key names. */
    public int getRetainedCharacters() {
        return retainedCharacters;
    }

    /** Conservative item-key storage, including allocated NBT capacity and retained key sidecars. */
    public int getRetainedOpaqueBytes() {
        return retainedOpaqueBytes;
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

    /** Only the bounded materializer may adopt fresh sequence arrays. Public collection() stays fixed-shape. */
    static DataValue ownedSequence(DataType type, DataValue[] values) {
        int limit = type == DataType.ITEM_CONTENTS ? ItemContents.MAX_GROUPS
            : type == DataType.ITEM_LOCATIONS ? ItemContents.MAX_LOCATIONS : -1;
        if (limit < 0 || values.length > limit) throw new IllegalArgumentException("Invalid item observation sequence");
        for (DataValue value : values) if (value == null || !type.getElementType()
            .equals(value.type)) throw new IllegalArgumentException("Observation row type mismatch");
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
        if (type == DataType.ITEM_CONTENTS || type == DataType.ITEM_LOCATIONS)
            throw new IllegalArgumentException("Item observation sequences are immutable");
        DataValue old = get(index);
        if (!old.type.equals(replacement.type)) throw new IllegalArgumentException("Assignment type/shape mismatch");
        DataValue[] copy = children.clone();
        copy[index] = replacement;
        // Ancestor updates use cached payload deltas; they never scan nested string contents.
        return new DataValue(
            type,
            copy,
            replaceCount(stringCharacterCount, old.stringCharacterCount, replacement.stringCharacterCount),
            replaceCount(stringNbtBytes, old.stringNbtBytes, replacement.stringNbtBytes),
            replaceCount(retainedCellCount, old.retainedCellCount, replacement.retainedCellCount),
            replaceCount(retainedCharacters, old.retainedCharacters, replacement.retainedCharacters),
            replaceCount(retainedOpaqueBytes, old.retainedOpaqueBytes, replacement.retainedOpaqueBytes));
    }

    private static int replaceCount(int total, int old, int replacement) {
        return Math.addExact(Math.subtractExact(total, old), replacement);
    }

    @Override
    public String toString() {
        if (type == DataType.INTEGER) return Long.toString(scalar);
        if (type == DataType.BOOLEAN) return scalar == 0 ? "FALSE" : "TRUE";
        if (type == DataType.STRING) return quote(text);
        if (type == DataType.ITEM_KEY) {
            ItemIdentity item = asItemIdentity();
            return "ITEM_KEY(" + quote(item.getRegistryId()) + ", " + item.getMetadata() + ")";
        }
        if (type == DataType.LABEL_REF || type == DataType.RESOURCE_SELECTOR) return opaque.toString();
        StringBuilder out = new StringBuilder(type.isSequence() ? "[" : "{");
        for (int i = 0; i < children.length; i++) {
            if (i > 0) out.append(", ");
            if (type.getKind() == DataType.Kind.STRUCT) out.append(
                type.getFieldNames()
                    .get(i))
                .append(": ");
            out.append(children[i]);
        }
        return out.append(type.isSequence() ? ']' : '}')
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
