package ca.teamdman.sfm.common.program;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ExpressionLimits;
import ca.teamdman.sfml.data.ProgramStateSchema;
import ca.teamdman.sfml.data.ProgramStateSchema.Declaration;
import ca.teamdman.sfml.data.StateAccess;

/**
 * One disk's validated state. Activation/reset prepare a candidate; only flush publishes NBT.
 * Live values remain in heap; flush only at save/exposure/disk/source lifecycle boundaries.
 * Invocation cleanup should mark the manager dirty when this store is dirty, without encoding NBT.
 */
public final class ProgramStateStore implements StateAccess {

    public static final String NBT_KEY = "sfm:program_state";
    public static final int FORMAT_VERSION = 1;
    public static final int MAX_PAYLOAD_BYTES = ExpressionLimits.MAX_STATE_BYTES;
    /** Complete disk root, including source/labels/recovery; leaves room below vanilla signed-short gzip length. */
    public static final int MAX_DISK_NBT_BYTES = 30000;

    private static final int MAX_DECLARATIONS = ExpressionLimits.MAX_STATE_DECLARATIONS;
    private static final int MAX_CELLS = ExpressionLimits.MAX_STATE_CELLS;
    private static final int MAX_DEPTH = ExpressionLimits.MAX_DEPTH;
    private static final int MAX_WIDTH = ExpressionLimits.MAX_CONTAINER_WIDTH;
    private static final int MAX_NBT_DEPTH = MAX_DEPTH * 3 + 8;
    private static final int MAX_NBT_TAGS = MAX_CELLS * 8 + MAX_DECLARATIONS * 8;

    private final ItemStack disk;
    private final ProgramStateSchema schema;
    private final DataValue[] values;
    private final NBTTagCompound[] encodedEntries;
    private final boolean[] dirtySlots;
    private final List<String> names;
    private final List<String> activationWarnings;
    private final String source;
    private NBTTagCompound observedRoot;
    private NBTBase observedEnvelope;
    private NBTBase observedRevisionTag;
    private long revision;
    private boolean dirty;
    private NBTTagCompound validatedDiskRoot;
    private String[] validatedDiskKeys;
    private NBTBase[] validatedDiskTags;
    // Exact encoded sizes; only modified-UTF8 string payload lengths vary within a schema.
    private int envelopeBytes;
    private int diskBytes;
    private int stringCharacters;

    private ProgramStateStore(ItemStack disk, ProgramStateSchema schema, boolean reset) {
        if (disk == null || schema == null) throw new IllegalArgumentException("Disk and state schema are required");
        this.disk = disk;
        this.schema = schema;
        observedRoot = disk.getTagCompound();
        observedEnvelope = observedRoot == null ? null : observedRoot.getTag(NBT_KEY);
        observedRevisionTag = observedEnvelope instanceof NBTTagCompound
            ? ((NBTTagCompound) observedEnvelope).getTag("revision")
            : null;
        source = ItemDisk.getProgramString(disk);
        Map<String, StoredEntry> old = reset ? Collections.emptyMap() : decode(observedEnvelope);
        revision = reset || observedEnvelope == null ? 0 : ((NBTTagCompound) observedEnvelope).getLong("revision");
        values = new DataValue[schema.size()];
        encodedEntries = new NBTTagCompound[schema.size()];
        dirtySlots = new boolean[schema.size()];
        List<String> mutableNames = new ArrayList<>();
        List<String> removed = new ArrayList<>(old.keySet());
        if (schema.size() > MAX_DECLARATIONS) throw invalid("Too many STATE declarations");
        int cells = 0;
        for (int slot = 0; slot < schema.size(); slot++) {
            Declaration declaration = schema.getDeclarations()
                .get(slot);
            String name = declaration.getName();
            cells += declaration.getType()
                .getCellCount();
            if (cells > MAX_CELLS) throw invalid("STATE exceeds " + MAX_CELLS + " cells");
            mutableNames.add(name);
            StoredEntry previous = old.get(name);
            if (previous != null && !previous.value.getType()
                .equals(declaration.getType())) {
                throw invalid("STATE $" + display(name) + " changed type or shape");
            }
            values[slot] = previous == null ? declaration.getDefaultValue() : previous.value;
            stringCharacters += values[slot].getStringCharacterCount();
            if (stringCharacters > ExpressionLimits.MAX_STATE_STRING_CHARACTERS)
                throw invalid("STATE exceeds string character limit");
            encodedEntries[slot] = encodeEntry(name, values[slot]);
            removed.remove(name);
        }
        names = Collections.unmodifiableList(mutableNames);
        if (removed.isEmpty()) {
            activationWarnings = Collections.emptyList();
        } else {
            activationWarnings = Collections.singletonList(
                "Removed STATE declarations " + displayNames(removed)
                    + "; their values are dropped on activation. "
                    + "Renamed declarations initialize from defaults and do not migrate old values.");
        }
        dirty = reset || !removed.isEmpty()
            || (observedEnvelope == null ? schema.size() != 0 : old.size() != schema.size());
        // Reject an oversized schema/default before the candidate can be accepted or published.
        NBTTagCompound candidate = envelope(encodedEntries, revision);
        envelopeBytes = measureEnvelope(candidate);
        diskBytes = validateDiskSnapshotSize(effectiveEnvelopeBytes());
        cacheDiskMetadata();
    }

    private int effectiveEnvelopeBytes() {
        return !dirty && observedEnvelope == null ? 0 : envelopeBytes;
    }

    /** Validates all saved entries, then reconciles by case-sensitive name and structural type. */
    public static ProgramStateStore activate(ItemStack disk, ProgramStateSchema schema) {
        return new ProgramStateStore(disk, schema, false);
    }

    /** Explicit recovery route; corrupt old state is retained until this candidate's flush succeeds. */
    public static ProgramStateStore reset(ItemStack disk, ProgramStateSchema schema) {
        return new ProgramStateStore(disk, schema, true);
    }

    public ProgramStateSchema getSchema() {
        return schema;
    }

    public List<String> getNames() {
        return names;
    }

    public List<String> getActivationWarnings() {
        return activationWarnings;
    }

    public boolean isDirty() {
        return dirty;
    }

    /**
     * O(1) cache ownership/revision check. External editors must replace the envelope or update its
     * revision; vanilla NBT exposes no callback for arbitrary in-place descendant mutations.
     */
    public boolean isCurrent(ItemStack candidate) {
        return ownsEnvelope(candidate) && source.equals(ItemDisk.getProgramString(disk));
    }

    private boolean ownsEnvelope(ItemStack candidate) {
        if (candidate != disk || disk.getTagCompound() != observedRoot) return false;
        NBTBase current = observedRoot == null ? null : observedRoot.getTag(NBT_KEY);
        if (current != observedEnvelope) return false;
        return !(current instanceof NBTTagCompound)
            || ((NBTTagCompound) current).getTag("revision") == observedRevisionTag;
    }

    @Override
    public DataValue read(int slot) {
        return values[slot];
    }

    @Override
    public void write(int slot, DataValue value) {
        DataValue previous = values[slot];
        if (value == null || !schema.getDeclarations()
            .get(slot)
            .getType()
            .equals(value.getType())) {
            throw invalid("Assignment to STATE $" + display(names.get(slot)) + " has an incompatible type or shape");
        }
        if (previous == value) return;
        if (value.getType()
            .getKind() == DataType.Kind.INTEGER && previous.asLong() == value.asLong()) return;
        if (value.getType()
            .getKind() == DataType.Kind.BOOLEAN && previous.asBoolean() == value.asBoolean()) return;
        // Reused immutable strings return above; avoid scanning text outside the expression work budget.
        int nextCharacters = stringCharacters - previous.getStringCharacterCount() + value.getStringCharacterCount();
        if (nextCharacters > ExpressionLimits.MAX_STATE_STRING_CHARACTERS)
            throw invalid("STATE exceeds string character limit");
        int delta = value.getStringNbtBytes() - previous.getStringNbtBytes();
        int nextEnvelopeBytes = envelopeBytes + delta;
        if (nextEnvelopeBytes > MAX_PAYLOAD_BYTES) throw invalid("STATE exceeds 256 KiB payload limit");
        int nextDiskBytes;
        if (diskMetadataCurrent()) {
            nextDiskBytes = diskBytes + delta;
            if (nextDiskBytes > MAX_DISK_NBT_BYTES) throw diskSizeFailure();
        } else {
            // Cold metadata edits remeasure metadata only; pending heap values need no NBT encoding.
            nextDiskBytes = validateDiskSnapshotSize(nextEnvelopeBytes);
            cacheDiskMetadata();
        }
        envelopeBytes = nextEnvelopeBytes;
        diskBytes = nextDiskBytes;
        stringCharacters = nextCharacters;
        values[slot] = value;
        dirtySlots[slot] = true;
        dirty = true;
    }

    /**
     * Coalesces successful heap writes into one complete in-memory NBT envelope at a lifecycle boundary.
     * Returns true only after publication. This does not itself write the world save to the drive.
     * Unchanged stores only compare cached direct disk-tag identities, without serialization or allocations.
     * Metadata APIs must replace a top-level tag when changing descendants; arbitrary foreign in-place
     * descendant edits require that same replacement protocol to trigger transport revalidation.
     * A source-only edit invalidates execution binding, but still permits preserving the old-schema
     * committed values here before a replacement schema is reconciled. Source text is never changed.
     */
    public boolean flush() {
        if (!dirty) {
            if (!diskMetadataCurrent()) {
                // A foreign replacement still needs its own raw transport check before exposure.
                diskBytes = ownsEnvelope(disk) ? validateDiskSnapshotSize(effectiveEnvelopeBytes())
                    : measureDiskSnapshot(disk.getTagCompound(), 0);
                cacheDiskMetadata();
            }
            return false;
        }
        if (!ownsEnvelope(disk)) throw invalid("Disk or STATE was replaced while state changes were pending");
        if (revision >= Long.MAX_VALUE - 1) throw invalid("STATE revision is exhausted");
        NBTTagCompound[] candidateEntries = encodedEntries.clone();
        for (int slot = 0; slot < values.length; slot++) {
            if (dirtySlots[slot]) candidateEntries[slot] = encodeEntry(names.get(slot), values[slot]);
        }
        NBTTagCompound candidate = envelope(candidateEntries, revision + 1);
        // Recheck actual bytes at publication; SET already bounded each variable string-size delta.
        int candidateBytes = measureEnvelope(candidate);
        int candidateDiskBytes = validateDiskSnapshotSize(candidateBytes);
        NBTTagCompound root = observedRoot;
        if (root == null) root = new NBTTagCompound();
        root.setTag(NBT_KEY, candidate);
        if (observedRoot == null) disk.setTagCompound(root);
        observedRoot = root;
        observedEnvelope = candidate;
        observedRevisionTag = candidate.getTag("revision");
        revision++;
        envelopeBytes = candidateBytes;
        diskBytes = candidateDiskBytes;
        System.arraycopy(candidateEntries, 0, encodedEntries, 0, encodedEntries.length);
        Arrays.fill(dirtySlots, false);
        dirty = false;
        cacheDiskMetadata();
        return true;
    }

    /**
     * Bound the entire disk before publication/exposure. The shallow snapshot omits saved STATE;
     * the exact candidate envelope size includes all pending heap writes without encoding them.
     * Fixed shapes keep NBT overhead constant; SET applies cached modified-UTF8 payload deltas.
     * The 30,000-byte bound leaves room for gzip expansion below vanilla's signed-short ceiling.
     */
    private int validateDiskSnapshotSize(int candidateEnvelopeBytes) {
        NBTTagCompound root = disk.getTagCompound();
        NBTTagCompound snapshot = new NBTTagCompound();
        if (root != null) {
            if (root.func_150296_c()
                .size() > MAX_DISK_NBT_BYTES / 3) throw diskSizeFailure();
            for (String key : root.func_150296_c()) {
                if (!NBT_KEY.equals(key)) snapshot.setTag(key, root.getTag(key));
            }
        }
        return measureDiskSnapshot(snapshot, candidateEnvelopeBytes);
    }

    private static int measureDiskSnapshot(NBTTagCompound snapshot, int candidateEnvelopeBytes) {
        if (snapshot == null) return candidateEnvelopeBytes;
        try {
            // Read-only shallow metadata snapshot; never copy or serialize the live state on SET.
            DiskSizeSink sink = new DiskSizeSink();
            CompressedStreamTools.write(snapshot, new DataOutputStream(sink));
            sink.add(candidateEnvelopeBytes);
            return sink.bytes;
        } catch (DiskSizeLimitException exception) {
            throw diskSizeFailure();
        } catch (IOException exception) {
            throw new IllegalStateException(
                "Disk NBT cannot be encoded safely for vanilla transport; correct disk metadata before retrying.",
                exception);
        }
    }

    private boolean diskMetadataCurrent() {
        NBTTagCompound root = disk.getTagCompound();
        if (root != validatedDiskRoot) return false;
        if (root == null) return true;
        if (root.func_150296_c()
            .size() != validatedDiskKeys.length) return false;
        for (int i = 0; i < validatedDiskKeys.length; i++) {
            if (root.getTag(validatedDiskKeys[i]) != validatedDiskTags[i]) return false;
        }
        return true;
    }

    private void cacheDiskMetadata() {
        validatedDiskRoot = disk.getTagCompound();
        if (validatedDiskRoot == null) {
            validatedDiskKeys = new String[0];
            validatedDiskTags = new NBTBase[0];
            return;
        }
        validatedDiskKeys = validatedDiskRoot.func_150296_c()
            .toArray(new String[0]);
        validatedDiskTags = new NBTBase[validatedDiskKeys.length];
        for (int i = 0; i < validatedDiskKeys.length; i++) {
            validatedDiskTags[i] = validatedDiskRoot.getTag(validatedDiskKeys[i]);
        }
    }

    private static IllegalStateException diskSizeFailure() {
        return new IllegalStateException(
            "Complete disk NBT exceeds the " + MAX_DISK_NBT_BYTES
                + "-byte vanilla transport budget. Reduce source, labels, graph, recovery metadata or STATE; "
                + "no state was discarded.");
    }

    private static final class DiskSizeLimitException extends IOException {

        private static final long serialVersionUID = 1L;
    }

    private static final class DiskSizeSink extends OutputStream {

        private int bytes;

        @Override
        public void write(int value) throws IOException {
            add(1);
        }

        @Override
        public void write(byte[] value, int offset, int length) throws IOException {
            add(length);
        }

        private void add(int amount) throws DiskSizeLimitException {
            if (amount < 0 || bytes > MAX_DISK_NBT_BYTES - amount) throw new DiskSizeLimitException();
            bytes += amount;
        }
    }

    private static NBTTagCompound envelope(NBTTagCompound[] entries, long revision) {
        NBTTagCompound result = new NBTTagCompound();
        result.setInteger("version", FORMAT_VERSION);
        result.setLong("revision", revision);
        result.setInteger("count", entries.length);
        NBTTagList list = new NBTTagList();
        for (NBTTagCompound entry : entries) list.appendTag(entry.copy());
        result.setTag("entries", list);
        return result;
    }

    private static NBTTagCompound encodeEntry(String name, DataValue value) {
        NBTTagCompound result = new NBTTagCompound();
        result.setString("name", name);
        result.setString(
            "type",
            value.getType()
                .getSignature());
        result.setTag("value", encodeValue(value));
        return result;
    }

    private static NBTTagCompound encodeValue(DataValue value) {
        NBTTagCompound result = new NBTTagCompound();
        switch (value.getType()
            .getKind()) {
            case INTEGER:
                result.setByte("kind", (byte) 1);
                result.setLong("value", value.asLong());
                break;
            case BOOLEAN:
                result.setByte("kind", (byte) 2);
                result.setByte("value", value.asBoolean() ? (byte) 1 : (byte) 0);
                break;
            case STRING:
                result.setByte("kind", (byte) 5);
                result.setString("value", value.asString());
                break;
            case ARRAY:
                result.setByte("kind", (byte) 3);
                NBTTagList elements = new NBTTagList();
                for (int i = 0; i < value.size(); i++) elements.appendTag(encodeValue(value.get(i)));
                result.setTag("value", elements);
                break;
            case STRUCT:
                result.setByte("kind", (byte) 4);
                NBTTagList fields = new NBTTagList();
                for (String name : value.getType()
                    .getFieldNames()) {
                    NBTTagCompound field = new NBTTagCompound();
                    field.setString("name", name);
                    field.setTag("value", encodeValue(value.get(name)));
                    fields.appendTag(field);
                }
                result.setTag("value", fields);
                break;
            default:
                throw invalid("Unsupported STATE value kind");
        }
        return result;
    }

    private static Map<String, StoredEntry> decode(NBTBase raw) {
        if (raw == null) return Collections.emptyMap();
        if (!(raw instanceof NBTTagCompound)) throw invalid("STATE envelope is not a compound");
        NBTTagCompound envelope = (NBTTagCompound) raw;
        measureEnvelope(envelope);
        keys(envelope, "version", "revision", "count", "entries");
        kind(envelope, "version", 3);
        kind(envelope, "revision", 4);
        kind(envelope, "count", 3);
        if (envelope.getInteger("version") != FORMAT_VERSION) throw invalid("Unsupported STATE format version");
        if (envelope.getLong("revision") < 0 || envelope.getLong("revision") == Long.MAX_VALUE) {
            throw invalid("Invalid STATE revision");
        }
        NBTTagList entries = compoundList(envelope, "entries", MAX_DECLARATIONS);
        if (envelope.getInteger("count") != entries.tagCount())
            throw invalid("STATE entry count does not match schema");
        Map<String, StoredEntry> result = new LinkedHashMap<>();
        DecodeBudget budget = new DecodeBudget();
        for (int i = 0; i < entries.tagCount(); i++) {
            NBTTagCompound entry = entries.getCompoundTagAt(i);
            keys(entry, "name", "type", "value");
            kind(entry, "name", 8);
            kind(entry, "type", 8);
            kind(entry, "value", 10);
            String name = entry.getString("name");
            identifier(name);
            DataValue value = decodeValue(entry.getCompoundTag("value"), 1, budget);
            if (!value.getType()
                .getSignature()
                .equals(entry.getString("type"))) {
                throw invalid("STATE $" + display(name) + " descriptor does not match its value");
            }
            if (result.put(name, new StoredEntry(value)) != null) throw invalid("Duplicate STATE $" + display(name));
        }
        return result;
    }

    private static DataValue decodeValue(NBTTagCompound value, int depth, DecodeBudget budget) {
        if (depth > MAX_DEPTH || ++budget.cells > MAX_CELLS) throw invalid("STATE value exceeds depth/cell limits");
        keys(value, "kind", "value");
        kind(value, "kind", 1);
        switch (value.getByte("kind")) {
            case 1:
                kind(value, "value", 4);
                return DataValue.integer(value.getLong("value"));
            case 2:
                kind(value, "value", 1);
                byte bool = value.getByte("value");
                if (bool != 0 && bool != 1) throw invalid("STATE boolean must be byte 0 or 1");
                return DataValue.bool(bool == 1);
            case 5:
                kind(value, "value", 8);
                try {
                    DataValue string = DataValue.string(value.getString("value"));
                    budget.stringCharacters += string.getStringCharacterCount();
                    if (budget.stringCharacters > ExpressionLimits.MAX_STATE_STRING_CHARACTERS)
                        throw invalid("STATE exceeds string character limit");
                    return string;
                } catch (IllegalArgumentException exception) {
                    throw invalid("Invalid STATE string: " + exception.getMessage());
                }
            case 3:
                NBTTagList elements = compoundList(value, "value", MAX_WIDTH);
                if (elements.tagCount() == 0) throw invalid("Empty STATE arrays have no inferred element type");
                List<DataValue> array = new ArrayList<>();
                for (int i = 0; i < elements.tagCount(); i++) {
                    array.add(decodeValue(elements.getCompoundTagAt(i), depth + 1, budget));
                }
                try {
                    return DataValue.array(array);
                } catch (IllegalArgumentException exception) {
                    throw invalid("Invalid STATE array: " + exception.getMessage());
                }
            case 4:
                NBTTagList fields = compoundList(value, "value", MAX_WIDTH);
                Map<String, DataValue> struct = new HashMap<>();
                for (int i = 0; i < fields.tagCount(); i++) {
                    NBTTagCompound field = fields.getCompoundTagAt(i);
                    keys(field, "name", "value");
                    kind(field, "name", 8);
                    kind(field, "value", 10);
                    String name = field.getString("name");
                    identifier(name);
                    if (struct.containsKey(name)) throw invalid("Duplicate STATE field " + display(name));
                    struct.put(name, decodeValue(field.getCompoundTag("value"), depth + 1, budget));
                }
                return DataValue.struct(struct);
            default:
                throw invalid("Unsupported STATE value kind");
        }
    }

    private static NBTTagList compoundList(NBTTagCompound parent, String key, int maximum) {
        kind(parent, key, 9);
        NBTTagList list = parent.getTagList(key, 10);
        // getTagList returns an empty replacement for a wrong element kind, so inspect the raw list.
        NBTTagList raw = (NBTTagList) parent.getTag(key);
        if (raw.tagCount() > maximum || raw.tagCount() != list.tagCount()
            || raw.func_150303_d() != 10 && (raw.tagCount() > 0 || raw.func_150303_d() != 0)) {
            throw invalid("Invalid STATE list " + key);
        }
        return raw;
    }

    private static void identifier(String value) {
        if (value.isEmpty() || !identifierStart(value.charAt(0))) throw invalid("Invalid STATE name");
        for (int i = 1; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!identifierStart(c) && (c < '0' || c > '9')) throw invalid("Invalid STATE name");
        }
    }

    private static boolean identifierStart(char c) {
        return c == '_' || c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
    }

    private static void kind(NBTTagCompound compound, String key, int id) {
        if (!compound.hasKey(key, id)) throw invalid("STATE field " + key + " has a missing or invalid tag kind");
    }

    private static void keys(NBTTagCompound compound, String... expected) {
        Set<String> actual = compound.func_150296_c();
        if (actual.size() != expected.length) throw invalid("STATE has missing or unknown fields");
        for (String name : expected) {
            if (!actual.contains(name)) throw invalid("STATE is missing field " + name);
        }
    }

    private static int measureEnvelope(NBTTagCompound envelope) {
        SizeBudget budget = new SizeBudget();
        budget.add(1 + utfSize(NBT_KEY));
        measure(envelope, 0, budget);
        return budget.bytes;
    }

    /** Exact uncompressed named-NBT payload size; rejects unknown kinds and cyclic/deep trees. */
    private static void measure(NBTBase tag, int depth, SizeBudget budget) {
        if (depth > MAX_NBT_DEPTH || ++budget.tags > MAX_NBT_TAGS)
            throw invalid("STATE NBT exceeds nesting/tag limits");
        if (tag == null) throw invalid("STATE has a null NBT tag");
        switch (tag.getId()) {
            case 1:
                budget.add(1);
                return;
            case 3:
                budget.add(4);
                return;
            case 4:
                budget.add(8);
                return;
            case 8:
                budget.add(utfSize(((net.minecraft.nbt.NBTTagString) tag).func_150285_a_()));
                return;
            case 9:
                NBTTagList list = (NBTTagList) tag;
                if (list.tagCount() > MAX_CELLS || list.tagCount() > 0 && list.func_150303_d() != 10) {
                    throw invalid("STATE NBT list has invalid size or element kind");
                }
                budget.add(5);
                for (int i = 0; i < list.tagCount(); i++) measure(list.getCompoundTagAt(i), depth + 1, budget);
                return;
            case 10:
                NBTTagCompound compound = (NBTTagCompound) tag;
                budget.add(1);
                for (String key : compound.func_150296_c()) {
                    budget.add(1 + utfSize(key));
                    measure(compound.getTag(key), depth + 1, budget);
                }
                return;
            default:
                throw invalid("Unsupported STATE NBT tag kind");
        }
    }

    private static int utfSize(String value) {
        if (value.length() > 65535) throw invalid("STATE string exceeds NBT UTF limit");
        int bytes = 0;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            bytes += c >= 1 && c <= 127 ? 1 : c <= 2047 ? 2 : 3;
        }
        if (bytes > 65535) throw invalid("STATE string exceeds NBT UTF limit");
        return bytes + 2;
    }

    private static String display(String name) {
        return name.length() <= 64 ? name : name.substring(0, 61) + "...";
    }

    private static String displayNames(List<String> names) {
        List<String> bounded = new ArrayList<>();
        for (int i = 0; i < Math.min(8, names.size()); i++) bounded.add("$" + display(names.get(i)));
        if (names.size() > 8) bounded.add("...");
        return bounded.toString();
    }

    private static IllegalStateException invalid(String message) {
        return new IllegalStateException(message + ". Reset program variables explicitly after correcting the source.");
    }

    private static final class StoredEntry {

        private final DataValue value;

        private StoredEntry(DataValue value) {
            this.value = value;
        }
    }

    private static final class DecodeBudget {

        private int cells;
        private int stringCharacters;
    }

    private static final class SizeBudget {

        private int bytes;
        private int tags;

        private void add(int amount) {
            if (amount < 0 || bytes > MAX_PAYLOAD_BYTES - amount) throw invalid("STATE exceeds 256 KiB payload limit");
            bytes += amount;
        }
    }
}
