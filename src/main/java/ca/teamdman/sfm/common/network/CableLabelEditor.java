package ca.teamdman.sfm.common.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import ca.teamdman.sfm.common.block.BlockCable;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.net.CableLabelData;
import ca.teamdman.sfm.common.net.CableLabelData.Mode;
import ca.teamdman.sfm.common.net.CableLabelData.Request;
import ca.teamdman.sfm.common.net.CableLabelData.Row;
import ca.teamdman.sfm.common.net.CableLabelData.View;
import ca.teamdman.sfm.common.net.PlayerUiAccess;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.program.ManagerNetworkBrowser;
import ca.teamdman.sfm.common.program.ProgramDiskData;
import ca.teamdman.sfm.common.program.ProgramStateStore;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfm.common.util.BoundedUtf8;

/** One cable-window session. No program/control capability is granted by this label-only tool. */
public final class CableLabelEditor {

    public static final int MAX_BLOCKS = 8192, MAX_MANAGERS = 64, MAX_SELECTED = 32;
    public static final int WORK_PER_TICK = 128;

    private enum Work {
        SCAN,
        LABELS,
        METADATA,
        FILTER,
        IDLE
    }

    private final EntityPlayer owner;
    private final World world;
    private final BlockPos root;
    private final Block cable;
    private final UUID session;
    private CableNetworkDiscovery scan;
    private long revision = 1, lastTick = Long.MIN_VALUE;
    private CableNetworkDiscovery.Snapshot component, network;
    private long lastActionTick = Long.MIN_VALUE;
    private Work work = Work.SCAN;
    private Mode mode = Mode.BUSY;
    private String message = "Discovering cable component...", query = "";
    private List<BlockPos> positions = new ArrayList<>(), visible = new ArrayList<>();
    private final Set<BlockPos> selected = new LinkedHashSet<>();
    private final Map<BlockPos, String> descriptions = new HashMap<>();
    private final Map<BlockPos, TileEntity> inspectedTiles = new HashMap<>();
    private final Map<BlockPos, Set<String>> labelIndex = new HashMap<>();
    private final Map<BlockPos, String> searchText = new HashMap<>();
    private Iterator<Map.Entry<String, Set<BlockPos>>> labelGroups;
    private Iterator<BlockPos> groupPositions = Collections.emptyIterator();
    private String groupName;
    private int filterCursor, lastWorkUnits;
    private String filterMessage;
    private int metadataCursor, page;
    private TileEntityManager manager;
    private ItemStack disk;
    private long diskGeneration;
    private NBTBase expectedLabels;
    private LabelPositionHolder labels;
    private View view;

    public CableLabelEditor(EntityPlayer owner, BlockPos root, UUID session) {
        if (!ServerUiTasks.isServerThread() || owner == null || session == null || root == null)
            throw new IllegalArgumentException("Invalid cable label session");
        this.owner = owner;
        this.world = owner.worldObj;
        this.root = root;
        this.session = session;
        if (world == null || world.isRemote || !root.isLoaded(world) || !(root.getBlock(world) instanceof BlockCable))
            throw new IllegalArgumentException("Cable unavailable");
        cable = root.getBlock(world);
        if (!canInteract(owner)) throw new IllegalArgumentException("Cable unavailable");
        refresh();
    }

    public boolean canInteract(EntityPlayer player) {
        return ServerUiTasks.isServerThread() && player == owner
            && PlayerUiAccess.isLive(player)
            && player.worldObj == world
            && root.isLoaded(world)
            && root.getBlock(world) == cable
            && player.getDistanceSq(root.x + .5, root.y + .5, root.z + .5) <= 64;
    }

    /** Called by the window's server tick; no hidden, idle or closed window work is registered globally. */
    public void tick() {
        if (!canInteract(owner)) {
            stale("Cable session closed or out of range.");
            return;
        }
        long now = world.getTotalWorldTime();
        if (now == lastTick) return;
        if (now < lastTick) {
            stale("World clock changed. Refresh before editing.");
            return;
        }
        lastTick = now;
        lastWorkUnits = 0;
        if (!topologyCurrent()) {
            stale("Network changed. Refresh before editing.");
            return;
        }
        if (manager != null && !bindingCurrent()) {
            stale("Manager, disk or labels changed. Choose the Manager again.");
            return;
        }
        if (mode != Mode.BUSY) return;
        try {
            if (work == Work.SCAN) {
                int before = scan.getInspectedPositions();
                CableNetworkDiscovery.Status status = scan.advance();
                lastWorkUnits = scan.getInspectedPositions() - before;
                if (status == CableNetworkDiscovery.Status.SCANNING) return;
                if (status != CableNetworkDiscovery.Status.COMPLETE) {
                    stale("Discovery " + status + ". Refresh or reduce network size.");
                    return;
                }
                CableNetworkDiscovery.Snapshot snapshot = scan.getSnapshot();
                scan = null;
                if (manager == null) {
                    component = snapshot;
                    if (snapshot.managers.size() > MAX_MANAGERS)
                        throw invalid("More than 64 Managers on this component.");
                    positions = new ArrayList<>(snapshot.managers);
                    mode = Mode.MANAGERS;
                    filter();
                    changed("Choose a Manager explicitly; only its installed disk labels will be edited.");
                    return;
                }
                if (!snapshot.cables.contains(root)) throw invalid("Selected Manager no longer reaches this cable.");
                network = snapshot;
                if (snapshot.peripherals.size() > MAX_BLOCKS)
                    throw invalid("More than 8192 connected blocks; reduce network size.");
                positions = new ArrayList<>(snapshot.peripherals);
                metadataCursor = 0;
                labelGroups = labels.getAllLabels()
                    .entrySet()
                    .iterator();
                groupPositions = Collections.emptyIterator();
                work = Work.LABELS;
                return;
            }
            if (work == Work.LABELS) {
                // Each group and each assignment costs one unit, including empty groups. Never P x L work.
                while (lastWorkUnits < WORK_PER_TICK) {
                    if (groupPositions.hasNext()) {
                        labelIndex.computeIfAbsent(groupPositions.next(), key -> new LinkedHashSet<>())
                            .add(groupName);
                    } else if (labelGroups.hasNext()) {
                        Map.Entry<String, Set<BlockPos>> group = labelGroups.next();
                        groupName = group.getKey();
                        groupPositions = group.getValue()
                            .iterator();
                    } else {
                        work = Work.METADATA;
                        return;
                    }
                    lastWorkUnits++;
                }
                return;
            }
            if (work == Work.FILTER) {
                while (filterCursor < positions.size() && lastWorkUnits < WORK_PER_TICK) {
                    BlockPos pos = positions.get(filterCursor++);
                    if (query.isEmpty() || searchText.get(pos)
                        .contains(query)) visible.add(pos);
                    lastWorkUnits++;
                }
                if (filterCursor == positions.size()) {
                    mode = Mode.BLOCKS;
                    work = Work.IDLE;
                    page = Math.min(
                        page,
                        Math.max(0, (visible.size() - 1) / CableLabelData.PAGE_SIZE * CableLabelData.PAGE_SIZE));
                    changed(filterMessage);
                }
                return;
            }
            int end = Math.min(positions.size(), metadataCursor + WORK_PER_TICK);
            for (; metadataCursor < end; metadataCursor++) {
                lastWorkUnits++;
                BlockPos pos = positions.get(metadataCursor);
                if (!pos.isLoaded(world)) throw invalid("A connected block unloaded. Refresh.");
                TileEntity tile = pos.getTileEntity(world);
                if (tile == null || tile.isInvalid()) throw invalid("A connected block changed. Refresh.");
                inspectedTiles.put(pos, tile);
                Block block = pos.getBlock(world);
                Object name = block == null ? null : Block.blockRegistry.getNameForObject(block);
                descriptions.put(
                    pos,
                    safe(
                        name == null ? tile.getClass()
                            .getSimpleName() : name.toString(),
                        128));
                cacheSearch(pos);
            }
            if (metadataCursor == positions.size()) {
                beginFilter("Select blocks, then add/remove one exact label. Program source is never renamed.");
            }
        } catch (RuntimeException error) {
            stale(errorText(error));
        }
    }

    public View view() {
        if (view != null) return view;
        List<Row> rows = new ArrayList<>();
        boolean ready = mode == Mode.MANAGERS || mode == Mode.BLOCKS;
        for (int i = page; ready && i < Math.min(visible.size(), page + CableLabelData.PAGE_SIZE); i++) {
            BlockPos pos = visible.get(i);
            String text = mode == Mode.MANAGERS ? "Select Manager " + pos
                : pos + " " + descriptions.get(pos) + "\nLabels: " + safe(labelText(pos), 220);
            rows.add(new Row(safe(text, 480), selected.contains(pos)));
        }
        String title = manager == null ? "Cable labels | " + root
            : "Disk labels | Manager " + new BlockPos(manager.xCoord, manager.yCoord, manager.zCoord);
        return view = new View(
            session,
            revision,
            mode,
            title,
            message,
            ready ? page : 0,
            ready ? visible.size() : 0,
            selected.size(),
            rows);
    }

    public void act(Request request) {
        if (!canInteract(owner) || request == null || !session.equals(request.session)) return;
        tick();
        long now = world.getTotalWorldTime();
        if (now == lastActionTick) {
            changed("One action per tick; review the current page and retry.");
            return;
        }
        lastActionTick = now;
        if (request.revision != revision) {
            changed("View changed. Review the current page before retrying.");
            return;
        }
        try {
            switch (request.action) {
                case REFRESH:
                    refresh();
                    break;
                case MANAGERS:
                    refresh();
                    break;
                case SEARCH:
                    if (mode != Mode.BLOCKS && mode != Mode.MANAGERS) throw invalid("Wait for discovery or refresh.");
                    query = request.text.toLowerCase(Locale.ROOT);
                    if (mode == Mode.BLOCKS) beginFilter(
                        "Filter applied across all labels; selected blocks retained. Shortened row text ends in ...");
                    else {
                        filter();
                        changed("Manager filter applied.");
                    }
                    break;
                case PREVIOUS:
                    page = Math.max(0, page - CableLabelData.PAGE_SIZE);
                    changed("Previous page.");
                    break;
                case NEXT:
                    if (page + CableLabelData.PAGE_SIZE < visible.size()) page += CableLabelData.PAGE_SIZE;
                    changed("Next page.");
                    break;
                case CLEAR:
                    selected.clear();
                    changed("Block selection cleared.");
                    break;
                case ROW:
                    if (mode != Mode.MANAGERS && mode != Mode.BLOCKS) throw invalid("Refresh before selecting blocks.");
                    int index = page + request.row;
                    if (index >= visible.size()) throw invalid("This row is no longer available.");
                    BlockPos pos = visible.get(index);
                    if (mode == Mode.MANAGERS) choose(pos);
                    else if (selected.remove(pos)) changed("Block unselected.");
                    else {
                        if (selected.size() == MAX_SELECTED) throw invalid("Select at most 32 blocks per edit.");
                        selected.add(pos);
                        changed("Block selected.");
                    }
                    break;
                case ADD:
                    edit(request.text, true);
                    break;
                case REMOVE:
                    edit(request.text, false);
                    break;
                default:
                    throw invalid("Unsupported cable label action");
            }
        } catch (RuntimeException error) {
            changed(errorText(error));
        }
    }

    private void refresh() {
        component = null;
        network = null;
        manager = null;
        disk = null;
        expectedLabels = null;
        labels = null;
        positions.clear();
        visible.clear();
        descriptions.clear();
        inspectedTiles.clear();
        labelIndex.clear();
        searchText.clear();
        selected.clear();
        query = "";
        page = 0;
        work = Work.SCAN;
        mode = Mode.BUSY;
        scan = CableNetworkDiscovery.fromCable(world, root);
        changed("Discovering cable component...");
    }

    private void choose(BlockPos pos) {
        if (!topologyCurrent() || !pos.isLoaded(world)) throw invalid("Network changed. Refresh.");
        TileEntity tile = pos.getTileEntity(world);
        if (!(tile instanceof TileEntityManager) || tile.isInvalid()) throw invalid("Manager unavailable. Refresh.");
        TileEntityManager candidate = (TileEntityManager) tile;
        ItemStack candidateDisk = candidate.getDisk();
        requireDisk(candidateDisk);
        LabelPositionHolder candidateLabels = readLabels(candidateDisk);
        manager = candidate;
        disk = candidateDisk;
        diskGeneration = manager.getDiskGeneration();
        labels = candidateLabels;
        expectedLabels = copyLabels(disk);
        positions = new ArrayList<>();
        visible = new ArrayList<>();
        descriptions.clear();
        inspectedTiles.clear();
        labelIndex.clear();
        searchText.clear();
        selected.clear();
        query = "";
        page = 0;
        work = Work.SCAN;
        mode = Mode.BUSY;
        scan = CableNetworkDiscovery.fromManager(world, pos);
        changed("Discovering selected Manager's connected blocks...");
    }

    private void edit(String name, boolean add) {
        if (mode != Mode.BLOCKS || selected.isEmpty()) throw invalid("Select one or more connected blocks first.");
        if (name.isEmpty() || !name.equals(name.trim()))
            throw invalid("Enter an exact label without surrounding whitespace.");
        for (int i = 0; i < name.length(); i++)
            if (name.charAt(i) < 32 || name.charAt(i) == 127 || name.charAt(i) == '\u00a7')
                throw invalid("Label contains a control character.");
        BoundedUtf8.encode(name, 768);
        if (!topologyCurrent() || !bindingCurrent()) throw invalid("Labels or network changed. Refresh.");
        if (!targetsCurrent()) throw invalid("A selected block changed. Refresh.");
        if (manager.hasPendingRecovery())
            throw invalid("Resolve the selected Manager's transfer recovery before label edits.");
        // Flush pending heap STATE before sizing, but never replace its live root or STATE envelope with a copied one.
        try {
            manager.flushProgramState();
        } catch (RuntimeException unavailable) {
            throw invalid(
                "Could not save current runtime STATE. Inspect the Manager diagnostics before editing labels.");
        }
        requireDisk(disk);
        LabelPositionHolder candidate = readLabels(disk);
        for (BlockPos pos : selected) {
            if (add) candidate.addPosition(name, pos);
            else candidate.removePosition(name, pos);
        }
        ItemStack preview = new ItemStack(disk.getItem(), 1, disk.getItemDamage());
        NBTTagCompound rootTag = disk.getTagCompound(), shallow = new NBTTagCompound();
        if (rootTag != null) for (String key : rootTag.func_150296_c()) shallow.setTag(key, rootTag.getTag(key));
        preview.setTagCompound(shallow);
        LabelPositionHolder.saveTo(preview, candidate);
        try {
            ProgramStateStore.validateCompleteDisk(preview);
        } catch (RuntimeException invalidSize) {
            throw invalid("Label edit exceeds the 30,000-byte disk NBT limit or cannot be encoded safely.");
        }
        readLabels(preview);
        if (!canInteract(owner) || !bindingCurrent() || !targetsCurrent() || !topologyCurrent())
            throw invalid("Session changed before applying labels.");
        if (rootTag == null) {
            rootTag = new NBTTagCompound();
            disk.setTagCompound(rootTag);
        }
        rootTag.setTag(LabelPositionHolder.NBT_KEY, shallow.getTag(LabelPositionHolder.NBT_KEY));
        manager.markDirty();
        labels = candidate;
        expectedLabels = copyLabels(disk);
        for (BlockPos pos : selected) {
            Set<String> names = labelIndex.computeIfAbsent(pos, key -> new LinkedHashSet<>());
            if (add) names.add(name);
            else names.remove(name);
            cacheSearch(pos);
        }
        beginFilter(
            (add ? "Added " : "Removed ") + safe(name, 80)
                + " on "
                + selected.size()
                + " block(s). Source references unchanged.");
    }

    private boolean targetsCurrent() {
        for (BlockPos pos : selected) {
            TileEntity expected = inspectedTiles.get(pos);
            if (expected == null || expected.isInvalid()
                || !pos.isLoaded(world)
                || pos.getTileEntity(world) != expected) return false;
        }
        return true;
    }

    private boolean topologyCurrent() {
        return (component == null || component.isCurrent()) && (network == null || network.isCurrent());
    }

    private boolean bindingCurrent() {
        if (manager == null || manager.getWorldObj() != world
            || manager.isInvalid()
            || !world.blockExists(manager.xCoord, manager.yCoord, manager.zCoord)
            || world.getTileEntity(manager.xCoord, manager.yCoord, manager.zCoord) != manager
            || manager.getDisk() != disk
            || manager.getDiskGeneration() != diskGeneration) return false;
        NBTBase current = disk.getTagCompound() == null ? null
            : disk.getTagCompound()
                .getTag(LabelPositionHolder.NBT_KEY);
        return expectedLabels == null ? current == null : expectedLabels.equals(current);
    }

    private static void requireDisk(ItemStack disk) {
        if (disk == null || !(disk.getItem() instanceof ItemDisk)
            || disk.stackSize != 1
            || ProgramDiskData.mode(disk) == ProgramDiskData.Mode.INVALID)
            throw invalid("Install one supported SFM program disk in this Manager first.");
        try {
            ProgramStateStore.validateCompleteDisk(disk);
        } catch (RuntimeException invalidSize) {
            throw invalid("Program disk metadata is invalid or exceeds the 30,000-byte NBT limit.");
        }
    }

    private static LabelPositionHolder readLabels(ItemStack disk) {
        try {
            return ManagerNetworkBrowser.strictLabels(disk);
        } catch (RuntimeException malformed) {
            throw invalid("Saved labels are malformed or exceed the label limits.");
        }
    }

    private static NBTBase copyLabels(ItemStack disk) {
        NBTBase tag = disk.getTagCompound() == null ? null
            : disk.getTagCompound()
                .getTag(LabelPositionHolder.NBT_KEY);
        return tag == null ? null : tag.copy();
    }

    private void filter() {
        visible = new ArrayList<>();
        for (BlockPos pos : positions) {
            String text = pos.toString();
            if (query.isEmpty() || text.toLowerCase(Locale.ROOT)
                .contains(query)) visible.add(pos);
        }
        page = Math.min(page, Math.max(0, (visible.size() - 1) / CableLabelData.PAGE_SIZE * CableLabelData.PAGE_SIZE));
    }

    private String labelText(BlockPos pos) {
        return String.join(", ", labelIndex.getOrDefault(pos, Collections.emptySet()));
    }

    private void cacheSearch(BlockPos pos) {
        searchText.put(pos, (pos + " " + descriptions.get(pos) + " " + labelText(pos)).toLowerCase(Locale.ROOT));
    }

    private void beginFilter(String completeMessage) {
        visible = new ArrayList<>();
        page = 0;
        filterCursor = 0;
        filterMessage = completeMessage;
        mode = Mode.BUSY;
        work = Work.FILTER;
        changed("Filtering cached metadata...");
    }

    /** Counter for deterministic work-bound qualification; no timing or profiling cost on ordinary Managers. */
    public int getLastWorkUnits() {
        return lastWorkUnits;
    }

    private void stale(String text) {
        if (mode != Mode.STALE) {
            mode = Mode.STALE;
            work = Work.IDLE;
            scan = null;
            visible = new ArrayList<>();
            page = 0;
            selected.clear();
            changed(text);
        }
    }

    private void changed(String text) {
        message = text;
        revision = Math.incrementExact(revision);
        view = null;
    }

    private static LabelFailure invalid(String text) {
        return new LabelFailure(text);
    }

    private static final class LabelFailure extends IllegalArgumentException {

        private LabelFailure(String text) {
            super(text);
        }
    }

    private static String errorText(RuntimeException error) {
        return error instanceof LabelFailure ? safe(error.getMessage(), 220)
            : "Label operation unavailable. Refresh or inspect the Manager diagnostics.";
    }

    private static String safe(String text, int maximum) {
        if (text == null) return "Unavailable; refresh before editing.";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < Math.min(text.length(), maximum); i++) {
            char c = text.charAt(i);
            result.append((c < 32 && c != '\n') || c == 127 || c == '\u00a7' || Character.isSurrogate(c) ? '?' : c);
        }
        if (text.length() > maximum) result.append("...");
        return result.toString();
    }
}
