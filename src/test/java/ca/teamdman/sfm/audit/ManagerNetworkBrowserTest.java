package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static ca.teamdman.sfm.common.program.ManagerNetworkBrowser.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.gui.ManagerEditContext;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;

class ManagerNetworkBrowserTest {

    @Test
    void invalidOrVanishedTilesAreNotPresentedAsUsableCachedInventories() throws Exception {
        Fixture f = new Fixture();
        BlockPos position = new BlockPos(1, 64, 0);
        MetadataTile inventory = new MetadataTile("Old chest");
        f.world.tiles.put(position, inventory);
        f.cache();
        inventory.invalidate();
        Row invalid = row(read(f.manager, new Session(), 0, 0, "", FILTER_ALL), position);
        assertEquals(STATE_UNREADABLE, invalid.state);
        assertEquals(0, invalid.kind);
        f.advance(10);
        f.world.tiles.remove(position);
        f.world.blocks.put(position, new ca.teamdman.sfm.common.block.BlockCable());
        Row missing = row(read(f.manager, new Session(), 0, 0, "", FILTER_ALL), position);
        assertEquals(STATE_MISSING, missing.state);
        assertEquals(0, missing.kind);
    }

    @Test
    void cachedUnionIncludesUnlabeledAndDisconnectedMissingAndUnloadedLabelsWithoutContentsOrMutation()
        throws Exception {
        Fixture f = new Fixture();
        MetadataTile item = new MetadataTile("Named chest");
        BlockPos connected = new BlockPos(1, 64, 0), disconnected = new BlockPos(8, 64, 0),
            missing = new BlockPos(9, 64, 0), unloaded = new BlockPos(10, 64, 0);
        f.world.tiles.put(connected, item);
        f.world.tiles.put(disconnected, new MetadataTile("Disconnected"));
        f.labels.addPosition("off-grid", disconnected);
        f.labels.addPosition("removed", missing);
        f.labels.addPosition("unloaded", unloaded);
        f.world.unloaded.add(unloaded);
        f.installLabels();
        NBTTagCompound before = (NBTTagCompound) f.disk.getTagCompound()
            .copy();
        CableNetwork network = f.cache();
        f.world.blockReads = 0;
        Page page = read(f.manager, new Session(), 0, 0, "", FILTER_ALL);
        assertEquals(4, page.totalCandidates);
        assertEquals(4, page.rows.size());
        assertEquals(STATE_CONNECTED, row(page, connected).state);
        assertEquals("", row(page, connected).labels);
        assertEquals(KIND_ITEM | KIND_FLUID, row(page, connected).kind);
        assertEquals(STATE_DISCONNECTED, row(page, disconnected).state);
        assertEquals(STATE_MISSING, row(page, missing).state);
        assertEquals(STATE_UNLOADED, row(page, unloaded).state);
        assertEquals(before, f.disk.getTagCompound());
        assertSame(network, f.manager.getCachedCableNetwork());
        assertEquals(3, f.world.blockReads, "Only loaded candidate metadata is inspected; no discovery");
        assertTrue(page.topologyCurrent);
        assertEquals(100, page.topologyTick);
        assertThrows(UnsupportedOperationException.class, () -> page.rows.clear());
    }

    @Test
    void absentAndStaleCachesNeverDiscoverOrPromiseConnectivity() throws Exception {
        Fixture f = new Fixture();
        BlockPos position = new BlockPos(1, 64, 0);
        f.labels.addPosition("known", position);
        f.world.tiles.put(position, new MetadataTile("Known"));
        f.installLabels();
        f.world.blockReads = 0;
        Page absent = read(f.manager, new Session(), 0, 0, "", FILTER_ALL);
        assertNull(f.manager.getCachedCableNetwork());
        assertFalse(absent.topologyCurrent);
        assertEquals(-1, absent.topologyTick);
        assertTrue(absent.message.contains("Rebuild"));
        assertEquals(STATE_UNCONFIRMED, absent.rows.get(0).state);
        assertEquals(1, f.world.blockReads);
        CableNetwork cache = f.cache();
        long revision = cache.getSnapshotRevision(), scan = cache.getLastScanTime();
        CableNetwork.invalidate(f.world);
        f.world.blockReads = 0;
        Page stale = read(f.manager, new Session(), 0, 0, "", FILTER_ALL);
        assertFalse(stale.topologyCurrent);
        assertEquals(STATE_UNCONFIRMED, stale.rows.get(0).state);
        assertEquals(revision, cache.getSnapshotRevision());
        assertEquals(scan, cache.getLastScanTime());
        assertEquals(1, f.world.blockReads);
        cache.peekPeripherals();
        cache.isSnapshotCurrent();
        assertEquals(1, f.world.blockReads, "Read-only cache APIs never scan");
    }

    @Test
    void progressiveSearchInspectsAtMost128CandidatesAndReturnsAtMostEightRows() throws Exception {
        Fixture f = new Fixture();
        f.cachePositions(300);
        Session session = new Session();
        f.world.blockReads = 0;
        Page first = read(f.manager, session, 0, 0, "needle absent", FILTER_ALL);
        assertTrue(first.rows.isEmpty());
        assertEquals(128, first.nextCursor);
        assertEquals(128, f.world.blockReads);
        f.world.blockReads = 0;
        Page second = read(f.manager, session, first.snapshotId, first.nextCursor, "needle absent", FILTER_ALL);
        assertTrue(second.rows.isEmpty());
        assertEquals(256, second.nextCursor);
        assertEquals(128, f.world.blockReads);
        f.advance(10);
        Page last = read(f.manager, session, first.snapshotId, second.nextCursor, "needle absent", FILTER_ALL);
        assertEquals(-1, last.nextCursor);
        assertTrue(last.rows.isEmpty());
        Page all = read(f.manager, new Session(), 0, 0, "", FILTER_ALL);
        assertEquals(8, all.rows.size());
        assertEquals(8, all.nextCursor);
    }

    @Test
    void nameCoordinatesKindsAndFullTruncatedLabelsAreSearchableWithoutReadingProviders() throws Exception {
        Fixture f = new Fixture();
        BlockPos position = new BlockPos(1, 64, 0);
        f.world.tiles.put(position, new MetadataTile("Friendly\n\u00a7cName\uD800"));
        String longName = repeat("a", 500) + "HIDDEN_NEEDLE";
        f.labels.addPosition(longName, position);
        f.installLabels();
        f.cache();
        Page label = read(f.manager, new Session(), 0, 0, "hidden_needle", FILTER_ITEMS);
        assertEquals(1, label.rows.size());
        assertFalse(label.rows.get(0).labels.contains("HIDDEN_NEEDLE"));
        assertEquals(256, label.rows.get(0).labels.length());
        assertFalse(label.rows.get(0).name.contains("\n"));
        assertFalse(label.rows.get(0).name.contains("\u00a7"));
        assertFalse(label.rows.get(0).name.contains("\uD800"));
        Page name = read(f.manager, new Session(), 0, 0, "friendly", FILTER_FLUIDS);
        assertEquals(1, name.rows.size());
        f.advance(10);
        Page coordinates = read(f.manager, new Session(), 0, 0, "1 64 0", FILTER_ALL);
        assertEquals(1, coordinates.rows.size());
    }

    @Test
    void managerWideAdmissionCoversNewIndicesAcrossDifferentObserversAndWindows() throws Exception {
        Fixture f = new Fixture();
        assertNotNull(read(f.manager, new Session(), 0, 0, "", FILTER_ALL));
        assertNotNull(read(f.manager, new Session(), 0, 0, "", FILTER_ALL));
        IllegalArgumentException denied = assertThrows(
            IllegalArgumentException.class,
            () -> read(f.manager, new Session(), 0, 0, "", FILTER_ALL));
        assertTrue(
            denied.getMessage()
                .contains("busy"));
        f.advance(9);
        assertThrows(IllegalArgumentException.class, () -> read(f.manager, new Session(), 0, 0, "", FILTER_ALL));
        f.advance(1);
        assertNotNull(read(f.manager, new Session(), 0, 0, "", FILTER_ALL));
        Fixture independent = new Fixture();
        assertNotNull(read(independent.manager, new Session(), 0, 0, "", FILTER_ALL));
    }

    @Test
    void snapshotRejectsChangedQueryDiskWorldAndExpiryAndSameTickTopologyRebuild() throws Exception {
        Fixture f = new Fixture();
        Session session = new Session();
        Page page = read(f.manager, session, 0, 0, "", FILTER_ALL);
        long querySnapshot = page.snapshotId;
        assertThrows(
            IllegalArgumentException.class,
            () -> read(f.manager, session, querySnapshot, 0, "different", FILTER_ALL));
        f.advance(10);
        page = read(f.manager, session, 0, 0, "", FILTER_ALL);
        long diskSnapshot = page.snapshotId;
        set(f.manager, "diskGeneration", f.manager.getDiskGeneration() + 1);
        assertThrows(IllegalArgumentException.class, () -> read(f.manager, session, diskSnapshot, 0, "", FILTER_ALL));
        f.advance(10);
        Page worldSnapshot = read(f.manager, session, 0, 0, "", FILTER_ALL);
        FakeWorld otherWorld = world();
        otherWorld.tiles.put(MANAGER, f.manager);
        f.manager.setWorldObj(otherWorld);
        assertThrows(
            IllegalArgumentException.class,
            () -> read(f.manager, session, worldSnapshot.snapshotId, 0, "", FILTER_ALL));
        f.manager.setWorldObj(f.world);
        f.advance(10);
        Page expiry = read(f.manager, session, 0, 0, "", FILTER_ALL);
        f.advance(SNAPSHOT_TICKS);
        assertThrows(
            IllegalArgumentException.class,
            () -> read(f.manager, session, expiry.snapshotId, 0, "", FILTER_ALL));
        f.advance(10);
        CableNetwork network = f.cache();
        Page topology = read(f.manager, session, 0, 0, "", FILTER_ALL);
        network.scan();
        assertThrows(
            IllegalArgumentException.class,
            () -> read(f.manager, session, topology.snapshotId, 0, "", FILTER_ALL));
    }

    @Test
    void providerCallbacksCannotReturnMetadataAfterManagerDiskWorldOrTileIdentityChanges() throws Exception {
        Fixture f = new Fixture();
        MetadataTile provider = new MetadataTile("Before");
        f.world.tiles.put(SRC, provider);
        f.cache();
        provider.onName = () -> f.world.tiles.put(SRC, new MetadataTile("After"));
        Page changedTile = read(f.manager, new Session(), 0, 0, "", FILTER_ALL);
        assertEquals(STATE_UNREADABLE, changedTile.rows.get(0).state);
        f.world.tiles.put(SRC, provider);
        provider.onName = () -> {
            try {
                set(f.manager, "diskStack", new ItemStack(new ItemDisk()));
            } catch (Exception error) {
                throw new AssertionError(error);
            }
        };
        assertThrows(IllegalArgumentException.class, () -> read(f.manager, new Session(), 0, 0, "", FILTER_ALL));
        f.advance(10);
        set(f.manager, "diskStack", f.disk);
        f.cache();
        provider.onName = () -> CableNetwork.invalidate(f.world);
        Page invalidated = read(f.manager, new Session(), 0, 0, "", FILTER_ALL);
        assertFalse(invalidated.topologyCurrent);
        assertEquals(STATE_UNCONFIRMED, invalidated.rows.get(0).state);
    }

    @Test
    void metadataExceptionsBecomeUnreadableAndUnloadedGeometryNeverQueriesBlocks() throws Exception {
        Fixture f = new Fixture();
        MetadataTile provider = new MetadataTile("Before");
        provider.onName = () -> { throw new IllegalStateException("private provider detail"); };
        f.world.tiles.put(SRC, provider);
        f.labels.addPosition("out-of-world", new BlockPos(Integer.MAX_VALUE, -1, Integer.MIN_VALUE));
        f.installLabels();
        f.cache();
        f.world.blockReads = 0;
        Page page = read(f.manager, new Session(), 0, 0, "", FILTER_ALL);
        assertEquals(STATE_UNREADABLE, row(page, SRC).state);
        assertFalse(page.message.contains("private"));
        assertEquals(STATE_UNLOADED, page.rows.get(1).state);
        assertEquals(1, f.world.blockReads);
    }

    @Test
    void strictSavedLabelSchemaAndBoundsFailBeforeCandidateProviderReads() throws Exception {
        for (int malformed = 0; malformed < 4; malformed++) {
            Fixture f = new Fixture();
            NBTTagCompound root = new NBTTagCompound(), labels = new NBTTagCompound();
            if (malformed == 0) root.setString(LabelPositionHolder.NBT_KEY, "invalid");
            else {
                if (malformed == 1) labels.setString("wrong-list", "invalid");
                if (malformed == 2) {
                    NBTTagList list = new NBTTagList();
                    list.appendTag(new NBTTagIntArray(new int[] { 1, 64, 0, 9 }));
                    labels.setTag("bad-coordinate", list);
                }
                if (malformed == 3) {
                    NBTTagList list = new NBTTagList();
                    list.appendTag(new NBTTagString("wrong-element"));
                    labels.setTag("wrong-element", list);
                }
                root.setTag(LabelPositionHolder.NBT_KEY, labels);
            }
            f.disk.setTagCompound(root);
            assertThrows(IllegalArgumentException.class, () -> read(f.manager, new Session(), 0, 0, "", FILTER_ALL));
            assertEquals(0, f.world.blockReads);
        }
        Fixture tooMany = new Fixture();
        NBTTagCompound labels = new NBTTagCompound();
        for (int i = 0; i <= LabelPositionHolder.MAX_SNAPSHOT_LABELS; i++) labels.setTag("label" + i, new NBTTagList());
        NBTTagCompound root = new NBTTagCompound();
        root.setTag(LabelPositionHolder.NBT_KEY, labels);
        tooMany.disk.setTagCompound(root);
        assertThrows(IllegalArgumentException.class, () -> read(tooMany.manager, new Session(), 0, 0, "", FILTER_ALL));
        assertEquals(0, tooMany.world.blockReads);
        Fixture longName = new Fixture();
        longName.labels.addPosition(repeat("x", LabelPositionHolder.MAX_SNAPSHOT_NAME_CHARACTERS + 1), SRC);
        longName.installLabels();
        assertThrows(IllegalArgumentException.class, () -> read(longName.manager, new Session(), 0, 0, "", FILTER_ALL));
        assertEquals(0, longName.world.blockReads);
        Fixture manyPositions = new Fixture();
        for (int i = 0; i <= LabelPositionHolder.MAX_SNAPSHOT_POSITIONS; i++)
            manyPositions.labels.addPosition("many", new BlockPos(i, 64, 10));
        manyPositions.installLabels();
        assertThrows(
            IllegalArgumentException.class,
            () -> read(manyPositions.manager, new Session(), 0, 0, "", FILTER_ALL));
        assertEquals(0, manyPositions.world.blockReads);
    }

    @Test
    void oversizedTopologyIsExplicitlyPartialAndCandidateOrderIsStableAndBounded() throws Exception {
        Fixture f = new Fixture();
        f.cachePositions(MAX_CANDIDATES + 100);
        Page page = read(f.manager, new Session(), 0, 0, "", FILTER_ALL);
        assertTrue(page.partial);
        assertEquals(MAX_CANDIDATES, page.totalCandidates);
        assertEquals(8, page.rows.size());
        for (int i = 1; i < page.rows.size(); i++)
            assertTrue(page.rows.get(i - 1).position.x < page.rows.get(i).position.x);
    }

    @Test
    void containerSessionIsLazyDistinctAndRetainsNoWorldProviderOrManagerReferences() throws Exception {
        Fixture f = new Fixture();
        ManagerEditContext context = context(f.manager), other = context(f.manager);
        assertSame(context.getNetworkBrowserSession(), context.getNetworkBrowserSession());
        assertNotSame(context.getNetworkBrowserSession(), other.getNetworkBrowserSession());
        read(f.manager, context.getNetworkBrowserSession(), 0, 0, "", FILTER_ALL);
        Field snapshot = Session.class.getDeclaredField("snapshot");
        snapshot.setAccessible(true);
        Object stored = snapshot.get(context.getNetworkBrowserSession());
        for (Field field : stored.getClass()
            .getDeclaredFields()) {
            assertFalse(net.minecraft.world.World.class.isAssignableFrom(field.getType()));
            assertFalse(net.minecraft.tileentity.TileEntity.class.isAssignableFrom(field.getType()));
            assertFalse(ItemStack.class.isAssignableFrom(field.getType()));
        }
        context.getNetworkBrowserSession()
            .clear();
        assertNull(snapshot.get(context.getNetworkBrowserSession()));
    }

    @Test
    void dtoAndRequestValidationRejectUnboundedOrInconsistentPayloads() {
        Row row = new Row(SRC, "Chest", "label", KIND_ITEM, STATE_CONNECTED);
        assertThrows(IllegalArgumentException.class, () -> new Row(SRC, "bad\nname", "", 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Row(SRC, repeat("x", 97), "", 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Row(SRC, "", "", 4, 0));
        assertThrows(IllegalArgumentException.class, () -> new Row(SRC, "", "", 0, 6));
        assertThrows(
            IllegalArgumentException.class,
            () -> new Page(1, 0, 0, 1, 0, -1, false, false, "", Collections.singletonList(row)));
        assertThrows(
            IllegalArgumentException.class,
            () -> new Page(1, 0, -1, 9, 0, -1, false, false, "", Collections.nCopies(9, row)));
        assertThrows(
            IllegalArgumentException.class,
            () -> read(null, new Session(), 0, 0, repeat("x", 65), FILTER_ALL));
    }

    private static ManagerEditContext context(TileEntityManager manager) {
        return new ManagerEditContext() {

            final State state = new State();

            public TileEntityManager getManager() {
                return manager;
            }

            public State getEditState() {
                return state;
            }
        };
    }

    private static Row row(Page page, BlockPos position) {
        return page.rows.stream()
            .filter(value -> value.position.equals(position))
            .findFirst()
            .orElseThrow(AssertionError::new);
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = TileEntityManager.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static String repeat(String text, int count) {
        return String.join("", Collections.nCopies(count, text));
    }

    private static final class Fixture {

        final FakeWorld world = world();
        final TileEntityManager manager = new TileEntityManager() {

            @Override
            public ItemStack getStackInSlot(int slot) {
                throw new AssertionError("Must not flush STATE");
            }
        };
        final ItemStack disk = new ItemStack(new ItemDisk());
        final LabelPositionHolder labels = new LabelPositionHolder();

        Fixture() throws Exception {
            manager.setWorldObj(world);
            manager.xCoord = MANAGER.x;
            manager.yCoord = MANAGER.y;
            manager.zCoord = MANAGER.z;
            world.tiles.put(MANAGER, manager);
            set(manager, "diskStack", disk);
        }

        void installLabels() {
            LabelPositionHolder.saveTo(disk, labels);
        }

        void advance(long ticks) {
            world.time += ticks;
        }

        CableNetwork cache() throws Exception {
            CableNetwork network = new CableNetwork(world, MANAGER);
            set(manager, "cableNetwork", network);
            return network;
        }

        @SuppressWarnings("unchecked")
        void cachePositions(int count) throws Exception {
            CableNetwork network = cache();
            Field field = CableNetwork.class.getDeclaredField("peripherals");
            field.setAccessible(true);
            Set<BlockPos> positions = (Set<BlockPos>) field.get(network);
            positions.clear();
            for (int i = 0; i < count; i++) positions.add(new BlockPos(i + 1, 64, 20));
        }
    }

    /** Every capability/contents method fails if inspection accidentally probes transfer availability. */
    private static final class MetadataTile extends InventoryTile implements IFluidHandler {

        final String name;
        Runnable onName;

        MetadataTile(String name) {
            super(1);
            this.name = name;
        }

        @Override
        public String getInventoryName() {
            if (onName != null) onName.run();
            return name;
        }

        @Override
        public int getSizeInventory() {
            throw new AssertionError("No capacity reads");
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            throw new AssertionError("No contents reads");
        }

        @Override
        public boolean isUseableByPlayer(EntityPlayer player) {
            throw new AssertionError("No access probes");
        }

        @Override
        public int fill(ForgeDirection side, FluidStack resource, boolean execute) {
            throw new AssertionError("No transfer");
        }

        @Override
        public FluidStack drain(ForgeDirection side, FluidStack resource, boolean execute) {
            throw new AssertionError("No transfer");
        }

        @Override
        public FluidStack drain(ForgeDirection side, int maximum, boolean execute) {
            throw new AssertionError("No transfer");
        }

        @Override
        public boolean canFill(ForgeDirection side, net.minecraftforge.fluids.Fluid fluid) {
            throw new AssertionError("No access probes");
        }

        @Override
        public boolean canDrain(ForgeDirection side, net.minecraftforge.fluids.Fluid fluid) {
            throw new AssertionError("No access probes");
        }

        @Override
        public FluidTankInfo[] getTankInfo(ForgeDirection side) {
            throw new AssertionError("No tank reads");
        }
    }
}
