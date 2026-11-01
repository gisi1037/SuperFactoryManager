package ca.teamdman.sfm.common.program;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidHandler;

import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfm.common.util.BoundedUtf8;

/** Server-thread metadata inspection. Never discovers topology, reads contents, saves disks or opens providers. */
public final class ManagerNetworkBrowser {

    public static final int PAGE_SIZE = 8, MAX_INSPECTIONS = 128, MAX_CANDIDATES = 32768;
    public static final int SNAPSHOT_TICKS = 600, ADMISSION_TICKS = 10, MAX_REQUESTS = 2;
    public static final int MAX_QUERY_CHARS = 64, MAX_QUERY_BYTES = 256;
    public static final int MAX_NAME_CHARS = 96, MAX_NAME_BYTES = 384;
    public static final int MAX_LABEL_CHARS = 256, MAX_LABEL_BYTES = 1024;
    public static final int MAX_MESSAGE_CHARS = 256, MAX_MESSAGE_BYTES = 1024;
    public static final int KIND_ITEM = 1, KIND_FLUID = 2;
    public static final int FILTER_ALL = 0, FILTER_ITEMS = 1, FILTER_FLUIDS = 2;
    public static final int STATE_CONNECTED = 0, STATE_UNCONFIRMED = 1, STATE_UNLOADED = 2, STATE_MISSING = 3,
        STATE_UNREADABLE = 4, STATE_DISCONNECTED = 5, MAX_STATE = 5;
    // Values contain only scalars. Weak keys cannot keep managers, their providers or unloaded worlds alive.
    private static final Map<Object, Long> IDENTITIES = new WeakHashMap<>();
    private static final Map<TileEntityManager, Admission> ADMISSIONS = new WeakHashMap<>();
    private static long nextIdentity = 1;

    private ManagerNetworkBrowser() {}

    /** One per live container. Only immutable coordinates/strings and scalar identity/cursor data are retained. */
    public static final class Session {

        private Snapshot snapshot;

        public void clear() {
            snapshot = null;
        }
    }

    public static final class Row {

        public final BlockPos position;
        public final String name, labels;
        public final int kind, state;

        public Row(BlockPos position, String name, String labels, int kind, int state) {
            this.position = Objects.requireNonNull(position, "position");
            requireText(name, MAX_NAME_CHARS, MAX_NAME_BYTES);
            requireText(labels, MAX_LABEL_CHARS, MAX_LABEL_BYTES);
            if (kind < 0 || kind > (KIND_ITEM | KIND_FLUID) || state < 0 || state > MAX_STATE)
                throw invalid("Invalid network row");
            this.name = name;
            this.labels = labels;
            this.kind = kind;
            this.state = state;
        }
    }

    public static final class Page {

        public final long snapshotId, capturedTick, topologyTick;
        public final int cursor, nextCursor, totalCandidates;
        public final boolean topologyCurrent, partial;
        public final String message;
        public final List<Row> rows;

        public Page(long snapshotId, int cursor, int nextCursor, int totalCandidates, long capturedTick,
            long topologyTick, boolean topologyCurrent, boolean partial, String message, List<Row> rows) {
            requireText(message, MAX_MESSAGE_CHARS, MAX_MESSAGE_BYTES);
            if (snapshotId <= 0 || totalCandidates < 0
                || totalCandidates > MAX_CANDIDATES
                || cursor < 0
                || cursor > totalCandidates
                || nextCursor < -1
                || nextCursor > totalCandidates
                || nextCursor != -1
                    && (nextCursor <= cursor || nextCursor >= totalCandidates || nextCursor - cursor > MAX_INSPECTIONS)
                || nextCursor == -1 && totalCandidates - cursor > MAX_INSPECTIONS
                || rows == null
                || rows.size() > PAGE_SIZE
                || rows.size() > totalCandidates - cursor
                || rows.contains(null)) throw invalid("Invalid network page");
            if (nextCursor != -1 && rows.size() > nextCursor - cursor) throw invalid("Invalid network page");
            this.snapshotId = snapshotId;
            this.cursor = cursor;
            this.nextCursor = nextCursor;
            this.totalCandidates = totalCandidates;
            this.capturedTick = capturedTick;
            this.topologyTick = topologyTick;
            this.topologyCurrent = topologyCurrent;
            this.partial = partial;
            this.message = message;
            this.rows = Collections.unmodifiableList(new ArrayList<>(rows));
        }
    }

    public static Page read(TileEntityManager manager, Session session, long snapshotId, int cursor, String query,
        int filter) {
        Objects.requireNonNull(session, "session");
        requireText(query, MAX_QUERY_CHARS, MAX_QUERY_BYTES);
        if (manager == null || snapshotId < 0
            || cursor < 0
            || cursor > MAX_CANDIDATES
            || snapshotId == 0 && cursor != 0
            || filter < FILTER_ALL
            || filter > FILTER_FLUIDS) throw invalid("Invalid network request");
        World world = manager.getWorldObj();
        if (world == null || world.isRemote) throw invalid("Manager is unavailable");
        long now = world.getTotalWorldTime(), managerId = identity(manager), worldId = identity(world);
        admit(manager, now);
        ItemStack disk = manager.getDisk();
        long generation = manager.getDiskGeneration();
        requireBinding(manager, world, disk, generation);
        String search = query.toLowerCase(Locale.ROOT);
        Snapshot snapshot = session.snapshot;
        if (snapshotId == 0) {
            snapshot = snapshot(manager, disk, generation, managerId, worldId, now, search, filter);
            requireBinding(manager, world, disk, generation);
            session.snapshot = snapshot;
        } else if (snapshot == null || snapshot.id != snapshotId
            || snapshot.manager != managerId
            || snapshot.world != worldId
            || snapshot.generation != generation
            || now < snapshot.tick
            || now - snapshot.tick >= SNAPSHOT_TICKS
            || !snapshot.query.equals(search)
            || snapshot.filter != filter) {
                session.clear();
                throw invalid("Network snapshot changed or expired; refresh the browser");
            }
        if (cursor > snapshot.entries.size()) throw invalid("Invalid network cursor");
        CableNetwork network = manager.getCachedCableNetwork();
        requireLifetime(snapshot, manager, world);
        requireTopology(snapshot, network);
        boolean current = network != null && network.isSnapshotCurrent();
        List<Row> rows = new ArrayList<>();
        int next = cursor, inspected = 0;
        while (next < snapshot.entries.size() && rows.size() < PAGE_SIZE && inspected < MAX_INSPECTIONS) {
            Entry entry = snapshot.entries.get(next++);
            inspected++;
            Inspection inspection = inspect(manager, world, disk, generation, entry, current, search);
            requireLifetime(snapshot, manager, world);
            requireTopology(snapshot, manager.getCachedCableNetwork());
            Row row = inspection.row;
            if (filter == FILTER_ITEMS && (row.kind & KIND_ITEM) == 0
                || filter == FILTER_FLUIDS && (row.kind & KIND_FLUID) == 0) continue;
            if (inspection.matches) rows.add(row);
        }
        requireBinding(manager, world, disk, generation);
        requireLifetime(snapshot, manager, world);
        requireTopology(snapshot, manager.getCachedCableNetwork());
        current = network != null && network.isSnapshotCurrent();
        if (!current) for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            if (row.state == STATE_CONNECTED || row.state == STATE_DISCONNECTED)
                rows.set(i, new Row(row.position, row.name, row.labels, row.kind, STATE_UNCONFIRMED));
        }
        String message = snapshot.network == 0
            ? "No cached discovery. Use Rebuild network; saved labels are unconfirmed."
            : current ? "Cached discovery metadata only; side access and transfer acceptance are not checked."
                : "Cached discovery is stale. Use Rebuild network; connectivity is unconfirmed.";
        if (snapshot.partial) message += " Partial index: position limit reached.";
        return new Page(
            snapshot.id,
            cursor,
            next >= snapshot.entries.size() ? -1 : next,
            snapshot.entries.size(),
            snapshot.tick,
            snapshot.topologyTick,
            current,
            snapshot.partial,
            message,
            rows);
    }

    private static Snapshot snapshot(TileEntityManager manager, ItemStack disk, long generation, long managerId,
        long worldId, long now, String query, int filter) {
        CableNetwork network = manager.getCachedCableNetwork();
        Set<BlockPos> peripherals = network == null ? Collections.emptySet() : network.peekPeripherals();
        Map<BlockPos, MutableEntry> positions = new HashMap<>();
        LabelPositionHolder labels = strictLabels(disk);
        for (Map.Entry<String, Set<BlockPos>> label : labels.getAllLabels()
            .entrySet()) {
            // Match the complete name ONCE, not once per position. Display summaries stay independently bounded.
            String name = clean(label.getKey(), MAX_LABEL_CHARS);
            boolean matches = !query.isEmpty() && label.getKey()
                .toLowerCase(Locale.ROOT)
                .contains(query);
            for (BlockPos position : label.getValue()) {
                MutableEntry entry = positions
                    .computeIfAbsent(position, key -> new MutableEntry(key, peripherals.contains(key)));
                entry.addLabel(name);
                entry.labelMatch |= matches;
            }
        }
        boolean partial = false;
        int scanned = 0;
        Iterator<BlockPos> iterator = peripherals.iterator();
        while (iterator.hasNext() && scanned++ < MAX_CANDIDATES) {
            BlockPos position = iterator.next();
            if (positions.containsKey(position)) continue;
            if (positions.size() >= MAX_CANDIDATES) {
                partial = true;
                break;
            }
            positions.put(position, new MutableEntry(position, true));
        }
        if (iterator.hasNext()) partial = true;
        List<Entry> entries = new ArrayList<>();
        for (MutableEntry entry : positions.values()) entries.add(entry.freeze());
        entries.sort(
            Comparator.comparingInt((Entry entry) -> entry.position.x)
                .thenComparingInt(entry -> entry.position.y)
                .thenComparingInt(entry -> entry.position.z));
        return new Snapshot(
            identity(new Object()),
            managerId,
            worldId,
            generation,
            now,
            new BlockPos(manager.xCoord, manager.yCoord, manager.zCoord),
            network == null ? 0 : identity(network),
            network == null ? -1 : network.getLastScanTime(),
            network == null ? 0 : network.getSnapshotRevision(),
            partial,
            query,
            filter,
            entries);
    }

    private static Inspection inspect(TileEntityManager manager, World world, ItemStack disk, long generation,
        Entry entry, boolean current, String query) {
        BlockPos position = entry.position;
        if (!inWorld(position) || !world.blockExists(position.x, position.y, position.z))
            return inspected(new Row(position, "Unloaded position", entry.labels, 0, STATE_UNLOADED), entry, query, "");
        TileEntity tile = world.getTileEntity(position.x, position.y, position.z);
        Block block = world.getBlock(position.x, position.y, position.z);
        if (tile != null && tile.isInvalid()) return inspected(
            new Row(position, "Invalid tile; refresh", entry.labels, 0, STATE_UNREADABLE),
            entry,
            query,
            "");
        int kind = (tile instanceof IInventory ? KIND_ITEM : 0) | (tile instanceof IFluidHandler ? KIND_FLUID : 0);
        String registry = block == null ? "" : clean(Block.blockRegistry.getNameForObject(block), MAX_NAME_CHARS);
        String name = !registry.isEmpty() ? registry
            : tile == null ? "Missing block"
                : clean(
                    tile.getClass()
                        .getSimpleName(),
                    MAX_NAME_CHARS);
        if (name.isEmpty()) name = "Loaded block";
        int state = tile == null && (entry.connected || block == null || block == Blocks.air) ? STATE_MISSING
            : !current ? STATE_UNCONFIRMED : entry.connected ? STATE_CONNECTED : STATE_DISCONNECTED;
        try {
            if (tile instanceof IInventory) {
                String friendly = clean(((IInventory) tile).getInventoryName(), MAX_NAME_CHARS);
                if (!friendly.isEmpty()) name = friendly;
            }
        } catch (RuntimeException unreadable) {
            state = STATE_UNREADABLE;
        }
        requireBinding(manager, world, disk, generation);
        if (!world.blockExists(position.x, position.y, position.z)
            || world.getTileEntity(position.x, position.y, position.z) != tile
            || tile != null && tile.isInvalid())
            return inspected(
                new Row(position, "Metadata changed; refresh", entry.labels, 0, STATE_UNREADABLE),
                entry,
                query,
                "");
        return inspected(new Row(position, name, entry.labels, kind, state), entry, query, registry);
    }

    private static Inspection inspected(Row row, Entry entry, String query, String registry) {
        boolean matches = query.isEmpty() || entry.labelMatch
            || (row.name + " " + registry + " " + row.position.x + " " + row.position.y + " " + row.position.z)
                .toLowerCase(Locale.ROOT)
                .contains(query);
        return new Inspection(row, matches);
    }

    private static void requireTopology(Snapshot snapshot, CableNetwork network) {
        if (snapshot.network == 0 ? network != null
            : network == null || identity(network) != snapshot.network
                || network.getSnapshotRevision() != snapshot.topologyRevision
                || network.getLastScanTime() != snapshot.topologyTick)
            throw invalid("Cached topology changed; refresh the browser");
    }

    private static void requireLifetime(Snapshot snapshot, TileEntityManager manager, World world) {
        long now = world.getTotalWorldTime();
        if (now < snapshot.tick || now - snapshot.tick >= SNAPSHOT_TICKS
            || manager.xCoord != snapshot.origin.x
            || manager.yCoord != snapshot.origin.y
            || manager.zCoord != snapshot.origin.z)
            throw invalid("Network snapshot changed or expired; refresh the browser");
    }

    private static LabelPositionHolder strictLabels(ItemStack disk) {
        NBTTagCompound root = disk == null ? null : disk.getTagCompound();
        if (root == null || !root.hasKey(LabelPositionHolder.NBT_KEY)) return new LabelPositionHolder();
        if (!root.hasKey(LabelPositionHolder.NBT_KEY, 10)) throw invalid("Invalid saved labels");
        NBTTagCompound labels = root.getCompoundTag(LabelPositionHolder.NBT_KEY);
        Set<?> names = labels.func_150296_c();
        if (names.size() > LabelPositionHolder.MAX_SNAPSHOT_LABELS) throw invalid("Saved labels exceed browser limits");
        int count = 0, characters = 0;
        for (Object key : names) {
            String name = (String) key;
            if (name.length() > LabelPositionHolder.MAX_SNAPSHOT_NAME_CHARACTERS - characters)
                throw invalid("Saved labels exceed browser limits");
            characters += name.length();
            NBTBase tag = labels.getTag(name);
            if (!(tag instanceof NBTTagList)) throw invalid("Invalid saved labels");
            NBTTagList list = (NBTTagList) tag;
            if (list.func_150303_d() != 11 && !(list.tagCount() == 0 && list.func_150303_d() == 0))
                throw invalid("Invalid saved labels");
            if (list.tagCount() > LabelPositionHolder.MAX_SNAPSHOT_POSITIONS - count)
                throw invalid("Saved labels exceed browser limits");
            count += list.tagCount();
            for (int i = 0; i < list.tagCount(); i++)
                if (list.func_150306_c(i).length != 3) throw invalid("Invalid saved labels");
        }
        return LabelPositionHolder.boundedSnapshot(disk);
    }

    private static void requireBinding(TileEntityManager manager, World world, ItemStack disk, long generation) {
        if (manager.getWorldObj() != world || manager.isInvalid()
            || manager.getDisk() != disk
            || manager.getDiskGeneration() != generation
            || !world.blockExists(manager.xCoord, manager.yCoord, manager.zCoord)
            || world.getTileEntity(manager.xCoord, manager.yCoord, manager.zCoord) != manager)
            throw invalid("Manager or disk changed; refresh the browser");
    }

    private static boolean inWorld(BlockPos position) {
        return position.y >= 0 && position.y < 256
            && position.x >= -30000000
            && position.x <= 30000000
            && position.z >= -30000000
            && position.z <= 30000000;
    }

    private static synchronized long identity(Object object) {
        Long id = IDENTITIES.get(object);
        if (id == null) {
            if (nextIdentity == Long.MAX_VALUE) throw invalid("Network browser identity exhausted");
            id = nextIdentity++;
            IDENTITIES.put(object, id);
        }
        return id;
    }

    private static synchronized void admit(TileEntityManager manager, long now) {
        Admission admission = ADMISSIONS.computeIfAbsent(manager, ignored -> new Admission());
        long world = identity(manager.getWorldObj());
        if (admission.world != world || now < admission.tick || now - admission.tick >= ADMISSION_TICKS) {
            admission.tick = now;
            admission.count = 0;
            admission.world = world;
        }
        if (admission.count >= MAX_REQUESTS) throw invalid("Network browser busy; retry in a moment");
        admission.count++;
    }

    private static void requireText(String text, int characters, int bytes) {
        if (text == null || text.length() > characters) throw invalid("Network text exceeds its limit");
        BoundedUtf8.encode(text, bytes);
        for (int i = 0; i < text.length(); i++)
            if (text.charAt(i) == '\u00a7' || Character.isISOControl(text.charAt(i)))
                throw invalid("Invalid network text");
    }

    private static String clean(String text, int limit) {
        if (text == null) return "";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length() && result.length() < limit; i++) {
            char value = text.charAt(i);
            if (value == '\u00a7' || Character.isISOControl(value)) result.append(' ');
            else if (Character.isHighSurrogate(value)) {
                if (i + 1 < text.length() && Character.isLowSurrogate(text.charAt(i + 1))
                    && result.length() + 2 <= limit)
                    result.append(value)
                        .append(text.charAt(++i));
                else result.append('?');
            } else result.append(Character.isLowSurrogate(value) ? '?' : value);
        }
        return result.toString();
    }

    private static IllegalArgumentException invalid(String message) {
        return new IllegalArgumentException(message);
    }

    private static final class Admission {

        long tick, world;
        int count;
    }

    private static final class MutableEntry {

        final BlockPos position;
        final boolean connected;
        final StringBuilder labels = new StringBuilder();
        boolean labelMatch;

        MutableEntry(BlockPos position, boolean connected) {
            this.position = position;
            this.connected = connected;
        }

        void addLabel(String label) {
            if (labels.length() >= MAX_LABEL_CHARS) return;
            if (labels.length() > 0) {
                if (labels.length() + 2 >= MAX_LABEL_CHARS) return;
                labels.append(", ");
            }
            labels.append(label, 0, Math.min(label.length(), MAX_LABEL_CHARS - labels.length()));
        }

        Entry freeze() {
            return new Entry(position, clean(labels.toString(), MAX_LABEL_CHARS), connected, labelMatch);
        }
    }

    private static final class Entry {

        final BlockPos position;
        final String labels;
        final boolean connected, labelMatch;

        Entry(BlockPos position, String labels, boolean connected, boolean labelMatch) {
            this.position = position;
            this.labels = labels;
            this.connected = connected;
            this.labelMatch = labelMatch;
        }
    }

    private static final class Snapshot {

        final long id, manager, world, generation, tick, network, topologyTick, topologyRevision;
        final BlockPos origin;
        final boolean partial;
        final String query;
        final int filter;
        final List<Entry> entries;

        Snapshot(long id, long manager, long world, long generation, long tick, BlockPos origin, long network,
            long topologyTick, long topologyRevision, boolean partial, String query, int filter, List<Entry> entries) {
            this.id = id;
            this.manager = manager;
            this.world = world;
            this.generation = generation;
            this.tick = tick;
            this.origin = origin;
            this.network = network;
            this.topologyTick = topologyTick;
            this.topologyRevision = topologyRevision;
            this.partial = partial;
            this.query = query;
            this.filter = filter;
            this.entries = Collections.unmodifiableList(entries);
        }
    }

    private static final class Inspection {

        final Row row;
        final boolean matches;

        Inspection(Row row, boolean matches) {
            this.row = row;
            this.matches = matches;
        }
    }
}
