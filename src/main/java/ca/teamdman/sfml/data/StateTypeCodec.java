package ca.teamdman.sfml.data;

import java.util.LinkedHashMap;
import java.util.Map;

/** Strict persistence descriptor decoder; this is deliberately separate from SFML source type syntax. */
public final class StateTypeCodec {

    public static final int MAX_DESCRIPTOR_LENGTH = 30000;

    private final String source;
    private int offset;

    private StateTypeCodec(String source) {
        this.source = source;
    }

    /** Decode only bounded, canonical signatures of persistable types. No whitespace or opaque types are accepted. */
    public static DataType parse(String signature) {
        if (signature == null || signature.isEmpty() || signature.length() > MAX_DESCRIPTOR_LENGTH)
            throw new IllegalArgumentException("STATE type descriptor must contain 1–30000 characters");
        StateTypeCodec parser = new StateTypeCodec(signature);
        DataType type = parser.type(1);
        if (parser.offset != signature.length() || !type.getSignature()
            .equals(signature)) throw new IllegalArgumentException("Noncanonical STATE type descriptor");
        return type;
    }

    private DataType type(int depth) {
        if (depth > ExpressionLimits.MAX_DEPTH) throw invalid("Data nesting exceeds 32");
        if (take("INTEGER")) return DataType.INTEGER;
        if (take("BOOLEAN")) return DataType.BOOLEAN;
        if (take("STRING")) return DataType.STRING;
        if (take("ARRAY[")) {
            int length = width();
            expect(':');
            DataType element = type(depth + 1);
            expect(']');
            return DataType.array(element, length);
        }
        if (take("DYNAMIC_ARRAY[")) {
            DataType element = type(depth + 1);
            expect(']');
            return DataType.dynamicArray(element);
        }
        if (take("STRUCT{")) {
            Map<String, DataType> fields = new LinkedHashMap<>();
            while (peek() != '}') {
                if (fields.size() >= ExpressionLimits.MAX_CONTAINER_WIDTH)
                    throw invalid("Collection width exceeds 256");
                String name = name();
                if (fields.containsKey(name)) throw invalid("Duplicate STATE struct field");
                expect(':');
                fields.put(name, type(depth + 1));
                expect(';');
            }
            expect('}');
            return DataType.struct(fields);
        }
        throw invalid("Unsupported STATE type descriptor");
    }

    private int width() {
        int start = offset, width = 0;
        while (peek() >= '0' && peek() <= '9') {
            int digit = source.charAt(offset++) - '0';
            if (width > (ExpressionLimits.MAX_CONTAINER_WIDTH - digit) / 10)
                throw invalid("Collection width exceeds 256");
            width = width * 10 + digit;
        }
        if (offset == start) throw invalid("Missing fixed STATE array length");
        return width;
    }

    private String name() {
        int start = offset;
        char first = peek();
        if (!letter(first) && first != '_') throw invalid("Invalid STATE struct field");
        offset++;
        while (letter(peek()) || peek() == '_' || peek() >= '0' && peek() <= '9') offset++;
        return source.substring(start, offset);
    }

    private static boolean letter(char value) {
        return value >= 'A' && value <= 'Z' || value >= 'a' && value <= 'z';
    }

    private char peek() {
        return offset < source.length() ? source.charAt(offset) : '\0';
    }

    private boolean take(String token) {
        if (!source.startsWith(token, offset)) return false;
        offset += token.length();
        return true;
    }

    private void expect(char token) {
        if (peek() != token) throw invalid("Incomplete STATE type descriptor");
        offset++;
    }

    private IllegalArgumentException invalid(String message) {
        return new IllegalArgumentException(message + " at offset " + offset);
    }
}
