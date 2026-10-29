package ca.teamdman.sfml.data;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;

import cpw.mods.fml.relauncher.ReflectionHelper;

/**
 * Internal canonical NBT, not the wire format: type bytes, fixed-width big-endian values, UTF-16 code units,
 * sorted compound keys, and ordered lists/arrays. Float/double raw bits are exact, including NaN payloads.
 * Null is encoded separately from an empty compound. Nothing mutable from the input escapes this builder.
 *
 * Provisional limits per key are depth 16 (root depth 1), 1024 tag nodes, and 8192 encoded bytes. They bound
 * recursive stack use and preparatory matching keys; they are not measured whole-pack execution budgets.
 * Snapshot QueryBudget allocation units are payload bytes plus 8 per temporary/retained key reference and
 * sidecar-array reference. Live comparison allocates only one control object for a non-null NBT walk; slot
 * units cover tag visits, encoded bytes, copied bytes, key collection, and conservative string comparisons.
 * Growth/copies/loops are charged before work. Native getters/reflection cannot themselves be preempted.
 * Use only on the server thread, without concurrent input mutation. Unsupported/corrupt input fails closed.
 */
final class BoundedItemNbt {

    static final int MAX_DEPTH = 16, MAX_NODES = 1024, MAX_BYTES = 8192;
    private static final Class<?>[] TYPES = { null, NBTTagByte.class, NBTTagShort.class, NBTTagInt.class,
        NBTTagLong.class, NBTTagFloat.class, NBTTagDouble.class, NBTTagByteArray.class, NBTTagString.class,
        NBTTagList.class, NBTTagCompound.class, NBTTagIntArray.class };
    // 1.7.10 has no general non-mutating list accessor. Names cover MCP and production SRG environments.
    private static final Field LIST = ReflectionHelper.findField(NBTTagList.class, "tagList", "field_74747_a");
    private static final byte[] NO_BYTES = new byte[0];
    private static final String[][] NO_KEYS = new String[0][];
    private final byte[] payload;
    private final int length;
    private final int hash;
    private final String[][] compoundKeys;
    private final int compoundCount, comparisonWork;
    private final int retainedCharacters, retainedOpaqueBytes;

    private BoundedItemNbt(Writer writer) {
        payload = writer.bytes;
        length = writer.used;
        compoundKeys = writer.keys;
        compoundCount = writer.compounds;
        comparisonWork = writer.comparisonWork;
        int characters = 0;
        // Conservative 64-bit headers/references; count allocated capacity, not just encoded length.
        int bytes = Math.addExact(64, Math.addExact(24, payload.length));
        bytes = Math.addExact(bytes, Math.addExact(24, Math.multiplyExact(8, compoundKeys.length)));
        for (int i = 0; i < compoundCount; i++) {
            String[] names = compoundKeys[i];
            bytes = Math.addExact(bytes, Math.addExact(24, Math.multiplyExact(8, names.length)));
            for (String name : names) {
                characters = Math.addExact(characters, name.length());
                bytes = Math.addExact(bytes, retainedStringBytes(name));
            }
        }
        retainedCharacters = characters;
        retainedOpaqueBytes = bytes;
        writer.budget.chargeSlots(length);
        int value = 1;
        for (int i = 0; i < length; i++) value = 31 * value + payload[i];
        hash = value;
    }

    static BoundedItemNbt snapshot(NBTTagCompound input, QueryBudget budget) {
        try {
            Writer writer = new Writer(budget);
            if (input == null) writer.number(0, 1);
            else writer.tag(input, 1);
            return new BoundedItemNbt(writer);
        } catch (IllegalAccessException | RuntimeException failure) {
            throw new IllegalArgumentException("Invalid or over-budget item NBT", failure);
        }
    }

    int getLength() {
        return length;
    }

    int getComparisonWork() {
        return comparisonWork;
    }

    int getRetainedCharacters() {
        return retainedCharacters;
    }

    int getRetainedOpaqueBytes() {
        return retainedOpaqueBytes;
    }

    static int retainedStringBytes(String value) {
        // String object, backing char-array header and UTF-16 payload, rounded conservatively to 8 bytes.
        int bytes = Math.addExact(56, Math.multiplyExact(2, value.length()));
        return Math.multiplyExact(Math.floorDiv(Math.addExact(bytes, 7), 8), 8);
    }

    /**
     * Compare live NBT against this key without copying/sorting/hashing it. Snapshot-owned key arrays contain
     * only immutable Strings. Corruption encountered during the walk faults; an early mismatch denies the
     * match without visiting or validating unrelated descendants. Callers must not treat false as validation.
     */
    boolean matches(NBTTagCompound input, QueryBudget budget) {
        if (input == null || payload[0] == 0) {
            budget.chargeSlot();
            return input == null && payload[0] == 0;
        }
        try {
            budget.chargeAllocation(1); // One control object; no live payload/key arrays are allocated.
            Writer writer = new Writer(budget, this);
            writer.tag(input, 1);
            return writer.equal && writer.used == length && writer.compounds == compoundCount;
        } catch (IllegalAccessException | RuntimeException failure) {
            throw new IllegalArgumentException("Invalid or over-budget live item NBT", failure);
        }
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof BoundedItemNbt)) return false;
        BoundedItemNbt that = (BoundedItemNbt) other;
        if (hash != that.hash || length != that.length) return false;
        for (int i = 0; i < length; i++) if (payload[i] != that.payload[i]) return false;
        return true;
    }

    private static IllegalArgumentException invalid() {
        return new IllegalArgumentException("Unsupported, corrupt, or oversized item NBT");
    }

    private static final class Writer {

        final QueryBudget budget;
        final BoundedItemNbt expected;
        byte[] bytes = NO_BYTES;
        String[][] keys = NO_KEYS;
        int used, nodes, compounds, comparisonWork;
        boolean equal = true;

        Writer(QueryBudget budget) {
            this(budget, null);
        }

        Writer(QueryBudget budget, BoundedItemNbt expected) {
            this.budget = budget;
            this.expected = expected;
            if (expected != null) {
                bytes = expected.payload;
                keys = expected.compoundKeys;
            }
        }

        void reserve(int amount) {
            if (!equal) return;
            if (amount < 0 || amount > MAX_BYTES - used) throw invalid();
            if (expected != null && amount > expected.length - used) {
                equal = false;
                return;
            }
            budget.chargeSlots(amount);
            if (expected != null) return;
            comparisonWork += amount;
            int required = used + amount;
            if (required <= bytes.length) return;
            int capacity = Math.max(required, Math.min(MAX_BYTES, Math.max(32, bytes.length * 2)));
            budget.chargeAllocation(capacity);
            budget.chargeSlots(used);
            bytes = Arrays.copyOf(bytes, capacity);
        }

        void number(long value, int width) {
            reserve(width);
            for (int shift = (width - 1) * 8; shift >= 0 && equal; shift -= 8) emit((byte) (value >>> shift));
        }

        void emit(byte value) {
            if (!equal) return;
            if (expected == null) bytes[used++] = value;
            else if (bytes[used] == value) used++;
            else equal = false;
        }

        void text(String value) {
            if (value == null || value.length() > (MAX_BYTES - 4) / 2) throw invalid();
            number(value.length(), 4);
            if (!equal) return;
            reserve(value.length() * 2);
            for (int i = 0; i < value.length() && equal; i++) {
                char c = value.charAt(i);
                emit((byte) (c >>> 8));
                emit((byte) c);
            }
        }

        void skipName(String name) {
            int amount = 4 + name.length() * 2;
            reserve(amount);
            if (equal) used += amount;
        }

        void retainKeys(String[] names) {
            if (compounds == keys.length) {
                int capacity = Math.min(MAX_NODES, Math.max(4, keys.length * 2));
                budget.chargeAllocation(capacity * 8);
                budget.chargeSlots(compounds * 8);
                keys = Arrays.copyOf(keys, capacity);
            }
            keys[compounds++] = names;
        }

        int type(NBTBase tag) {
            if (tag == null) throw invalid();
            for (int i = 1; i < TYPES.length; i++) if (tag.getClass() == TYPES[i]) return i;
            throw invalid();
        }

        void validateChildren(int count) {
            if (count < 0 || count > MAX_NODES - nodes) throw invalid();
        }

        void children(int count) {
            validateChildren(count);
            budget.chargeSlots(count);
            if (expected == null) comparisonWork += count;
        }

        void tag(NBTBase tag, int depth) throws IllegalAccessException {
            if (!equal) return;
            if (depth > MAX_DEPTH || ++nodes > MAX_NODES) throw invalid();
            budget.chargeSlot();
            if (expected == null) comparisonWork++;
            int type = type(tag);
            number(type, 1);
            if (!equal) return;
            switch (type) {
                case 1:
                    number(((NBTTagByte) tag).func_150290_f(), 1);
                    break;
                case 2:
                    number(((NBTTagShort) tag).func_150289_e(), 2);
                    break;
                case 3:
                    number(((NBTTagInt) tag).func_150287_d(), 4);
                    break;
                case 4:
                    number(((NBTTagLong) tag).func_150291_c(), 8);
                    break;
                case 5:
                    number(Float.floatToRawIntBits(((NBTTagFloat) tag).func_150288_h()), 4);
                    break;
                case 6:
                    number(Double.doubleToRawLongBits(((NBTTagDouble) tag).func_150286_g()), 8);
                    break;
                case 7: {
                    byte[] values = ((NBTTagByteArray) tag).func_150292_c();
                    if (values == null || values.length > MAX_BYTES) throw invalid();
                    number(values.length, 4);
                    if (!equal) return;
                    reserve(values.length);
                    if (expected == null) {
                        System.arraycopy(values, 0, bytes, used, values.length);
                        used += values.length;
                    } else for (int i = 0; i < values.length && equal; i++) emit(values[i]);
                    break;
                }
                case 8:
                    text(((NBTTagString) tag).func_150285_a_());
                    break;
                case 9: {
                    NBTTagList list = (NBTTagList) tag;
                    Object raw = LIST.get(list);
                    if (!(raw instanceof List)) throw invalid();
                    List<?> values = (List<?>) raw;
                    // Vanilla uses ArrayList; reject replacements before invoking their indexing/size behavior.
                    if (values.getClass() != java.util.ArrayList.class) throw invalid();
                    int size = values.size(), elementType = list.func_150303_d();
                    validateChildren(size);
                    if (elementType < 0 || elementType > 11 || (elementType == 0 && size != 0)) throw invalid();
                    number(elementType, 1);
                    number(size, 4);
                    if (!equal) return;
                    // A differing header needs no child walk or child-iteration reservation.
                    children(size);
                    for (int i = 0; i < size && equal; i++) {
                        Object value = values.get(i);
                        if (!(value instanceof NBTBase) || type((NBTBase) value) != elementType) throw invalid();
                        tag((NBTBase) value, depth + 1);
                    }
                    break;
                }
                case 10: {
                    NBTTagCompound compound = (NBTTagCompound) tag;
                    Set<?> names = compound.func_150296_c();
                    if (names == null) throw invalid();
                    int size = names.size();
                    validateChildren(size);
                    if (expected != null) {
                        if (compounds >= expected.compoundCount || keys[compounds].length != size) {
                            equal = false;
                            return;
                        }
                        String[] known = keys[compounds++];
                        children(size);
                        number(size, 4);
                        for (int i = 0; i < known.length && equal; i++) {
                            String name = known[i];
                            budget.chargeSlots(2); // Presence and value lookup before touching the live compound.
                            skipName(name); // Names are this snapshot's immutable canonical names.
                            if (!equal) return;
                            if (!compound.hasKey(name)) {
                                equal = false;
                                return;
                            }
                            tag(compound.getTag(name), depth + 1); // Present null is corrupt, not an absent key.
                        }
                        break;
                    }
                    children(size);
                    budget.chargeAllocation(size * 8);
                    String[] sorted = new String[size];
                    Iterator<?> iterator = names.iterator();
                    for (int i = 0; i < size; i++) {
                        if (!iterator.hasNext()) throw invalid();
                        Object name = iterator.next();
                        if (!(name instanceof String) || ((String) name).length() > MAX_BYTES / 2) throw invalid();
                        sorted[i] = (String) name;
                    }
                    if (iterator.hasNext()) throw invalid();
                    // Reserve a conservative full reference array for the JDK sort's temporary storage.
                    budget.chargeAllocation(size * 8);
                    Arrays.sort(sorted, (a, b) -> {
                        budget.chargeSlots(Math.min(a.length(), b.length()) + 1);
                        return a.compareTo(b);
                    });
                    retainKeys(sorted);
                    number(size, 4);
                    for (String key : sorted) {
                        comparisonWork += 2;
                        budget.chargeSlots(2);
                        text(key);
                        tag(compound.getTag(key), depth + 1);
                    }
                    break;
                }
                case 11: {
                    int[] values = ((NBTTagIntArray) tag).func_150302_c();
                    if (values == null || values.length > MAX_BYTES / 4) throw invalid();
                    number(values.length, 4);
                    if (!equal) return;
                    reserve(values.length * 4);
                    for (int i = 0; i < values.length && equal; i++) {
                        int value = values[i];
                        emit((byte) (value >>> 24));
                        emit((byte) (value >>> 16));
                        emit((byte) (value >>> 8));
                        emit((byte) value);
                    }
                    break;
                }
                default:
                    throw invalid();
            }
        }
    }
}
