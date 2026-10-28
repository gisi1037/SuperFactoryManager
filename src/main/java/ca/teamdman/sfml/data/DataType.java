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
        ARRAY,
        STRUCT
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
    private final Kind kind;
    private final DataType element;
    private final int length, cells, depth;
    private final List<String> names;
    private final List<DataType> fields;
    private final String signature;

    private DataType(Kind kind, DataType element, int length, List<String> names, List<DataType> fields) {
        if (length < 0 || length > 256) throw new IllegalArgumentException("Collection width exceeds 256");
        this.kind = kind;
        this.element = element;
        this.length = length;
        this.names = Collections.unmodifiableList(new ArrayList<>(names));
        this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
        int count = 1, nesting = 1;
        StringBuilder out = new StringBuilder(kind.name());
        if (kind == Kind.ARRAY) {
            count = Math.addExact(1, Math.multiplyExact(length, element.cells));
            nesting = element.depth + 1;
            out.append('[')
                .append(length)
                .append(':')
                .append(element.signature)
                .append(']');
        } else if (kind == Kind.STRUCT) {
            out.append('{');
            for (int i = 0; i < fields.size(); i++) {
                DataType type = fields.get(i);
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
    }

    public static DataType array(DataType element, int length) {
        if (element == null) throw new IllegalArgumentException("Array element type required");
        return new DataType(Kind.ARRAY, element, length, Collections.emptyList(), Collections.emptyList());
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
