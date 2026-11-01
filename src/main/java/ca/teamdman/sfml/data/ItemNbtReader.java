package ca.teamdman.sfml.data;

/** Reads validated immutable canonical bytes, without live item/provider access or retained mutable tags. */
final class ItemNbtReader {

    private static final String[] TYPES = { "missing", "byte", "short", "int", "long", "float", "double", "byte_array",
        "string", "list", "compound", "int_array" };
    private final byte[] bytes;
    private final int limit;
    private final QueryBudget budget;
    private final DataRuntime runtime;
    private int position;

    private ItemNbtReader(byte[] bytes, int limit, QueryBudget budget, DataRuntime runtime) {
        this.bytes = bytes;
        this.limit = limit;
        this.budget = budget;
        this.runtime = runtime;
    }

    static DataValue read(byte[] bytes, int limit, String method, String path, QueryBudget budget,
        DataRuntime runtime) {
        if (path == null || path.length() > 512 || !path.isEmpty() && path.charAt(0) != '/')
            throw new IllegalArgumentException("NBT path must be empty for root, or a JSON Pointer starting with '/'");
        int count = path.isEmpty() ? 0 : 1;
        for (int i = 1; i < path.length(); i++) if (path.charAt(i) == '/') count++;
        if (count > BoundedItemNbt.MAX_DEPTH) throw new IllegalArgumentException("NBT path exceeds 16 segments");
        runtime.work(path.length() + count + 1);
        runtime.allocate(count + 1);
        budget.chargeSlots(path.length() + count + 1);
        budget.chargeAllocation(path.length() * 4 + count * 8 + 64);
        String[] parts = count == 0 ? new String[0]
            : path.substring(1)
                .split("/", -1);
        for (int i = 0; i < parts.length; i++) {
            String raw = parts[i];
            for (int j = 0; j < raw.length(); j++) if (raw.charAt(j) == '~') {
                if (++j >= raw.length() || raw.charAt(j) != '0' && raw.charAt(j) != '1')
                    throw new IllegalArgumentException("NBT path escapes must be ~0 for '~' or ~1 for '/'");
            }
            parts[i] = raw.replace("~1", "/")
                .replace("~0", "~");
        }
        ItemNbtReader reader = new ItemNbtReader(bytes, limit, budget, runtime);
        Target target = reader.find(parts, 0, 1);
        if (method.equals("HASNBT")) return DataValue.bool(target != null);
        if (method.equals("NBTTYPE")) return reader.string(TYPES[target == null ? 0 : target.type]);
        if (target == null) throw new IllegalArgumentException("NBT path is missing: " + path);
        reader.position = target.offset;
        if (method.equals("NBTINTEGER")) {
            switch (target.type) {
                case 1:
                    return DataValue.integer((byte) reader.number(1));
                case 2:
                    return DataValue.integer((short) reader.number(2));
                case 3:
                    return DataValue.integer((int) reader.number(4));
                case 4:
                    return DataValue.integer(reader.number(8));
                default:
                    throw new IllegalArgumentException("nbtinteger requires byte, short, int or long NBT");
            }
        }
        if (method.equals("NBTSTRING")) {
            if (target.type != 8) throw new IllegalArgumentException("nbtstring requires string NBT");
            return reader.string(reader.text());
        }
        if (method.equals("NBTTEXT")) {
            String value;
            switch (target.type) {
                case 1:
                    value = Byte.toString((byte) reader.number(1));
                    break;
                case 2:
                    value = Short.toString((short) reader.number(2));
                    break;
                case 3:
                    value = Integer.toString((int) reader.number(4));
                    break;
                case 4:
                    value = Long.toString(reader.number(8));
                    break;
                case 5:
                    value = Float.toString(Float.intBitsToFloat((int) reader.number(4)));
                    break;
                case 6:
                    value = Double.toString(Double.longBitsToDouble(reader.number(8)));
                    break;
                case 8:
                    value = reader.text();
                    break;
                default:
                    throw new IllegalArgumentException("nbttext requires scalar NBT; inspect container keys/indices");
            }
            return reader.string(value);
        }
        if (method.equals("NBTLENGTH")) {
            if (target.type == 9) reader.number(1);
            if (target.type != 7 && target.type != 8 && target.type != 9 && target.type != 10 && target.type != 11)
                throw new IllegalArgumentException("nbtlength requires compound, list, array or string NBT");
            return DataValue.integer(reader.count());
        }
        if (method.equals("NBTKEYS")) {
            if (target.type != 10) throw new IllegalArgumentException("nbtkeys requires compound NBT");
            int size = reader.count();
            if (size > ExpressionLimits.MAX_CONTAINER_WIDTH)
                throw new IllegalArgumentException("NBT keys exceed 256 entries; select a smaller compound");
            runtime.allocate(size + 1);
            budget.chargeAllocation(size * 8 + 1);
            DataValue[] keys = new DataValue[size];
            for (int i = 0; i < size; i++) {
                keys[i] = reader.string(reader.text());
                reader.skip(1);
            }
            return DataValue.ownedDynamicArray(DataType.dynamicArray(DataType.STRING), keys);
        }
        throw new IllegalArgumentException("Unsupported NBT inspection method");
    }

    private Target find(String[] parts, int index, int depth) {
        if (depth > BoundedItemNbt.MAX_DEPTH) throw new IllegalArgumentException("NBT path nesting exceeds 16");
        int type = (int) number(1);
        if (type == 0) return null;
        if (index == parts.length) return new Target(type, position);
        if (type == 10) {
            int count = count();
            for (int i = 0; i < count; i++) {
                String key = text();
                runtime.work(key.length());
                budget.chargeSlots(key.length());
                if (key.equals(parts[index])) return find(parts, index + 1, depth + 1);
                skip(depth + 1);
            }
        } else if (type == 9 || type == 7 || type == 11) {
            if (type == 9) number(1);
            int count = count(), wanted = arrayIndex(parts[index]);
            if (wanted < 0 || wanted >= count) return null;
            if (type != 9) {
                if (index + 1 != parts.length) return null;
                advance(wanted * (type == 7 ? 1 : 4));
                return new Target(type == 7 ? 1 : 3, position);
            }
            for (int i = 0; i < wanted; i++) skip(depth + 1);
            return find(parts, index + 1, depth + 1);
        }
        return null;
    }

    private static int arrayIndex(String value) {
        if (value.isEmpty() || value.length() > 10 || value.length() > 1 && value.charAt(0) == '0') return -1;
        long index = 0;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c < '0' || c > '9') return -1;
            index = index * 10 + c - '0';
            if (index > Integer.MAX_VALUE) return -1;
        }
        return (int) index;
    }

    private void skip(int depth) {
        if (depth > BoundedItemNbt.MAX_DEPTH) throw new IllegalArgumentException("Invalid canonical NBT depth");
        int type = (int) number(1);
        switch (type) {
            case 0:
                break;
            case 1:
                advance(1);
                break;
            case 2:
                advance(2);
                break;
            case 3:
            case 5:
                advance(4);
                break;
            case 4:
            case 6:
                advance(8);
                break;
            case 7:
                advance(count());
                break;
            case 8:
                advance(count() * 2);
                break;
            case 11:
                advance(count() * 4);
                break;
            case 9: {
                number(1);
                int count = count();
                for (int i = 0; i < count; i++) skip(depth + 1);
                break;
            }
            case 10: {
                int count = count();
                for (int i = 0; i < count; i++) {
                    advance(count() * 2);
                    skip(depth + 1);
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Invalid canonical NBT type");
        }
    }

    private int count() {
        long count = number(4);
        if (count > BoundedItemNbt.MAX_BYTES) throw new IllegalArgumentException("Invalid canonical NBT length");
        return (int) count;
    }

    private long number(int width) {
        require(width);
        long value = 0;
        for (int i = 0; i < width; i++) value = value << 8 | bytes[position++] & 255L;
        return value;
    }

    private String text() {
        int count = count();
        require(count * 2);
        budget.chargeAllocation(count * 2 + 24);
        char[] value = new char[count];
        for (int i = 0; i < count; i++) value[i] = (char) ((bytes[position++] & 255) << 8 | bytes[position++] & 255);
        return new String(value);
    }

    private DataValue string(String value) {
        if (value.length() > ExpressionLimits.MAX_STRING_LENGTH)
            throw new IllegalArgumentException("NBT string result exceeds 4096 characters");
        runtime.allocate(1);
        runtime.allocateString(value.length());
        runtime.work(value.length());
        budget.chargeAllocation(value.length() * 2 + 1);
        budget.chargeSlots(value.length());
        return DataValue.string(value);
    }

    private void advance(int amount) {
        require(amount);
        position += amount;
    }

    private void require(int amount) {
        if (amount < 0 || amount > limit - position) throw new IllegalArgumentException("Invalid canonical NBT offset");
        runtime.work(amount);
        budget.chargeSlots(amount);
    }

    private static final class Target {

        final int type, offset;

        Target(int type, int offset) {
            this.type = type;
            this.offset = offset;
        }
    }
}
