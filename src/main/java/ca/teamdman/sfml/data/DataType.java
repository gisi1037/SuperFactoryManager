package ca.teamdman.sfml.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Internally immutable structural type; dimensions are checked before values allocate. */
public final class DataType {

    public enum Kind {
        INTEGER,
        BOOLEAN,
        STRING,
        LABEL_REF,
        RESOURCE_SELECTOR,
        ARRAY,
        STRUCT,
        ITEM_KEY,
        ITEM_CONTENTS,
        ITEM_LOCATIONS
    }

    public static final DataType INTEGER = new DataType(
        Kind.INTEGER,
        null,
        0,
        Collections.emptyList(),
        Collections.emptyList());
    public static final DataType BOOLEAN = new DataType(
        Kind.BOOLEAN,
        null,
        0,
        Collections.emptyList(),
        Collections.emptyList());
    public static final DataType STRING = new DataType(
        Kind.STRING,
        null,
        0,
        Collections.emptyList(),
        Collections.emptyList());
    public static final DataType LABEL_REF = new DataType(
        Kind.LABEL_REF,
        null,
        0,
        Collections.emptyList(),
        Collections.emptyList());
    public static final DataType RESOURCE_SELECTOR = new DataType(
        Kind.RESOURCE_SELECTOR,
        null,
        0,
        Collections.emptyList(),
        Collections.emptyList());
    public static final DataType ITEM_KEY = new DataType(
        Kind.ITEM_KEY,
        null,
        0,
        Collections.emptyList(),
        Collections.emptyList());
    public static final DataType ITEM_LOCATIONS = new DataType(
        Kind.ITEM_LOCATIONS,
        locationRow(),
        0,
        Collections.emptyList(),
        Collections.emptyList());
    public static final DataType ITEM_CONTENTS = new DataType(
        Kind.ITEM_CONTENTS,
        contentsRow(),
        0,
        Collections.emptyList(),
        Collections.emptyList());
    private final Kind kind;
    private final DataType element;
    private final int length, cells, depth;
    private final List<String> names;
    private final List<DataType> fields;
    private final String signature;
    private final boolean persistable;

    private DataType(Kind kind, DataType element, int length, List<String> names, List<DataType> fields) {
        if (length < 0 || length > 256) throw new IllegalArgumentException("Collection width exceeds 256");
        this.kind = kind;
        this.element = element;
        this.length = length;
        this.names = Collections.unmodifiableList(new ArrayList<>(names));
        this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
        int count = 1, nesting = 1;
        boolean persistent = kind != Kind.LABEL_REF && kind != Kind.RESOURCE_SELECTOR
            && kind != Kind.ITEM_KEY
            && kind != Kind.ITEM_CONTENTS
            && kind != Kind.ITEM_LOCATIONS;
        StringBuilder out = new StringBuilder(kind.name());
        if (kind == Kind.ARRAY) {
            persistent = element.persistable;
            count = Math.addExact(1, Math.multiplyExact(length, element.cells));
            nesting = element.depth + 1;
            out.append('[')
                .append(length)
                .append(':')
                .append(element.signature)
                .append(']');
        } else if (kind == Kind.ITEM_CONTENTS || kind == Kind.ITEM_LOCATIONS) {
            // A sequence type describes one invocation-owned handle, not a padded fixed array.
            // Construction separately charges the actual rows, scalar cells and child references.
            nesting = element.depth + 1;
        } else if (kind == Kind.STRUCT) {
            out.append('{');
            for (int i = 0; i < fields.size(); i++) {
                DataType type = fields.get(i);
                persistent &= type.persistable;
                count = Math.addExact(count, type.cells);
                nesting = Math.max(nesting, type.depth + 1);
                out.append(names.get(i))
                    .append(':')
                    .append(type.signature)
                    .append(';');
            }
            out.append('}');
        }
        if (count > 4096) throw new IllegalArgumentException("Collection expanded cells exceed 4096");
        if (nesting > 32) throw new IllegalArgumentException("Data nesting exceeds 32");
        cells = count;
        depth = nesting;
        signature = out.toString();
        persistable = persistent;
    }

    public static DataType array(DataType element, int length) {
        if (element == null) throw new IllegalArgumentException("Array element type required");
        return new DataType(Kind.ARRAY, element, length, Collections.emptyList(), Collections.emptyList());
    }

    private static DataType locationRow() {
        Map<String, DataType> fields = new TreeMap<>();
        for (String name : new String[] { "dimension", "x", "y", "z", "slot", "sideMask", "amount" })
            fields.put(name, INTEGER);
        return struct(fields);
    }

    private static DataType contentsRow() {
        Map<String, DataType> fields = new TreeMap<>();
        fields.put("item", ITEM_KEY);
        fields.put("id", STRING);
        fields.put("metadata", INTEGER);
        fields.put("total", INTEGER);
        fields.put("locations", ITEM_LOCATIONS);
        return struct(fields);
    }

    public static DataType struct(Map<String, DataType> fields) {
        TreeMap<String, DataType> sorted = new TreeMap<>(fields);
        for (String name : sorted.keySet()) if (!name.matches("[A-Za-z_][A-Za-z0-9_]*"))
            throw new IllegalArgumentException("Invalid field name: " + name);
        return new DataType(
            Kind.STRUCT,
            null,
            sorted.size(),
            new ArrayList<>(sorted.keySet()),
            new ArrayList<>(sorted.values()));
    }

    public Kind getKind() {
        return kind;
    }

    public boolean isPersistable() {
        return persistable;
    }

    /** Fixed arrays and the two immutable native observation sequences support index/LEN/FOR. */
    public boolean isSequence() {
        return kind == Kind.ARRAY || kind == Kind.ITEM_CONTENTS || kind == Kind.ITEM_LOCATIONS;
    }

    public DataType getElementType() {
        return element;
    }

    public int getLength() {
        return length;
    }

    public int getCellCount() {
        return cells;
    }

    public int getDepth() {
        return depth;
    }

    public List<String> getFieldNames() {
        return names;
    }

    public DataType getFieldType(int index) {
        return fields.get(index);
    }

    public int fieldIndex(String name) {
        int index = Collections.binarySearch(names, name);
        if (kind != Kind.STRUCT || index < 0) throw new IllegalArgumentException("Unknown struct field: " + name);
        return index;
    }

    public String getSignature() {
        return signature;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof DataType && signature.equals(((DataType) other).signature);
    }

    @Override
    public int hashCode() {
        return signature.hashCode();
    }

    @Override
    public String toString() {
        return signature;
    }
}
