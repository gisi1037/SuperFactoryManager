package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.RegistrySimple;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.block.BlockCable;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ItemQueryService;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.data.ItemContents;
import ca.teamdman.sfml.data.ItemContents.Group;
import ca.teamdman.sfml.data.ItemContents.Location;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;

/** Native immutable scan foundation; this does not establish an exposed SFML CONTENTS API. */
class ItemContentsQueryTest {

    private static final LabelReference SOURCE = LabelReference.literal("source");
    private static final ResourceSelector ALL = ResourceSelector.item("*");
    private static final Item SCAN_ITEM = new Item(), SCAN_OTHER = new Item();
    private static final int DIMENSION = 37;

    static {
        try {
            Field backing = RegistrySimple.class.getDeclaredField("registryObjects");
            backing.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> names = (Map<String, Object>) backing.get(Item.itemRegistry);
            names.put("sfm_contents_test:item", SCAN_ITEM);
            names.put("sfm_contents_test:other", SCAN_OTHER);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    @Test
    void equalKeysGroupTotalsAndKeepTwoPhysicalLocationsWithoutRescanning() throws Exception {
        Fixture fixture = fixture();
        CountingInventory source = inventory(fixture, SRC, 4);
        source.setInventorySlotContents(0, stack(SCAN_ITEM, 3, 5, "same"));
        source.setInventorySlotContents(2, stack(SCAN_ITEM, 7, 5, "same"));
        QueryBudget owner = new QueryBudget();
        ItemContents snapshot = service(fixture).contents(SOURCE, ALL, owner);
        List<Group> groups = snapshot.groups(owner);
        assertEquals(1, groups.size());
        Group group = groups.get(0);
        assertEquals(10L, group.getTotal());
        assertEquals(identity(5, "same"), group.getItem());
        assertEquals(
            2,
            group.getLocations()
                .size());
        assertLocation(
            group.getLocations()
                .get(0),
            SRC,
            0,
            63,
            3);
        assertLocation(
            group.getLocations()
                .get(1),
            SRC,
            2,
            63,
            7);
        assertEquals(4, source.stackReads, "Null slots are inspected once; groups do not reread providers");
        snapshot.groups(owner);
        assertEquals(4, source.stackReads);
        assertEquals(10, count(source));
        assertEquals(0, source.mutations);
    }

    @Test
    void metadataNbtAndRegistryNameSplitGroupsAndTotalsUseLong() throws Exception {
        Fixture fixture = fixture();
        CountingInventory source = inventory(fixture, SRC, 6);
        source.setInventorySlotContents(0, stack(SCAN_ITEM, 1, 1, "a"));
        source.setInventorySlotContents(1, stack(SCAN_ITEM, 2, 2, "a"));
        source.setInventorySlotContents(2, stack(SCAN_ITEM, 3, 1, "b"));
        source.setInventorySlotContents(3, stack(SCAN_OTHER, 4, 1, "a"));
        source.setInventorySlotContents(4, stack(SCAN_ITEM, 1, 7, null));
        source.setInventorySlotContents(5, stack(SCAN_ITEM, 1, 7, null));
        source.inv.getStackInSlot(4).stackSize = Integer.MAX_VALUE;
        source.inv.getStackInSlot(5).stackSize = Integer.MAX_VALUE;
        QueryBudget owner = new QueryBudget();
        List<Group> groups = service(fixture).contents(SOURCE, ALL, owner)
            .groups(owner);
        assertEquals(5, groups.size());
        Map<ItemIdentity, Long> totals = new HashMap<>();
        for (Group group : groups) totals.put(group.getItem(), group.getTotal());
        assertEquals(1L, totals.get(identity(1, "a")));
        assertEquals(2L, totals.get(identity(2, "a")));
        assertEquals(3L, totals.get(identity(1, "b")));
        assertEquals(2L * Integer.MAX_VALUE, totals.get(identity(7, null)));
        assertEquals(6, source.stackReads);
    }

    @Test
    void inventoryAliasesDoNotDuplicateLocationsOrReads() throws Exception {
        Fixture fixture = fixture();
        CountingInventory shared = inventory(fixture, SRC, 2);
        shared.setInventorySlotContents(1, stack(SCAN_ITEM, 11, 0, null));
        fixture.world.tiles.put(DST, shared);
        fixture.labels.addPosition("source", DST);
        QueryBudget owner = new QueryBudget();
        List<Group> groups = service(fixture).contents(SOURCE, ALL, owner)
            .groups(owner);
        assertEquals(1, groups.size());
        assertEquals(
            11L,
            groups.get(0)
                .getTotal());
        assertEquals(
            1,
            groups.get(0)
                .getLocations()
                .size());
        assertEquals(
            1,
            groups.get(0)
                .getLocations()
                .get(0).slot);
        assertEquals(2, shared.stackReads);
    }

    @Test
    void doubleChestLogicalRestrictionReportsCanonicalPhysicalHalfAndLocalSlot() throws Exception {
        Fixture fixture = fixture();
        BlockPos left = new BlockPos(0, 64, -1), right = new BlockPos(1, 64, -1);
        CountingChest first = chest(fixture, left, 3), second = chest(fixture, right, 7);
        fixture.labels.addPosition("both", right);
        fixture.labels.addPosition("both", left);
        fixture.labels.addPosition("right", right);
        fixture.world.blocks.put(SRC, new BlockCable());
        ItemQueryService service = service(fixture);
        for (String label : new String[] { "right", "both" }) {
            first.stackReads = second.stackReads = 0;
            QueryBudget owner = new QueryBudget();
            List<Group> groups = service.contents(
                LabelReference.literal(label)
                    .side(1)
                    .slots(27, 27),
                ALL,
                owner)
                .groups(owner);
            assertEquals(1, groups.size());
            assertEquals(
                7L,
                groups.get(0)
                    .getTotal());
            assertEquals(
                1,
                groups.get(0)
                    .getLocations()
                    .size());
            assertLocation(
                groups.get(0)
                    .getLocations()
                    .get(0),
                right,
                0,
                1 << 1,
                7);
            assertEquals(0, first.stackReads);
            assertEquals(1, second.stackReads);
        }
        QueryBudget owner = new QueryBudget();
        Group merged = service.contents(LabelReference.literal("both"), ALL, owner)
            .groups(owner)
            .get(0);
        assertEquals(10L, merged.getTotal());
        assertEquals(
            2,
            merged.getLocations()
                .size());
        assertLocation(
            merged.getLocations()
                .get(0),
            left,
            0,
            63,
            3);
        assertLocation(
            merged.getLocations()
                .get(1),
            right,
            0,
            63,
            7);
    }

    @Test
    void selectedSidesAndSlotRangeHideStockWithoutAskingTransferPermission() throws Exception {
        Fixture fixture = fixture();
        SidedInventory source = new SidedInventory(3);
        fixture.world.tiles.put(SRC, source);
        source.setInventorySlotContents(0, stack(SCAN_ITEM, 2, 0, null));
        source.setInventorySlotContents(1, stack(SCAN_ITEM, 7, 0, null));
        source.setInventorySlotContents(2, stack(SCAN_ITEM, 41, 0, null));
        source.faces[ForgeDirection.UP.ordinal()] = new int[] { -1, 0, 1, 1, 3, Integer.MAX_VALUE };
        source.faces[ForgeDirection.DOWN.ordinal()] = new int[] { 0 };
        LabelReference selected = SOURCE.side(ForgeDirection.UP.ordinal())
            .slots(1, 1);
        QueryBudget owner = new QueryBudget();
        Group group = service(fixture).contents(selected, ALL, owner)
            .groups(owner)
            .get(0);
        assertEquals(7L, group.getTotal());
        assertLocation(
            group.getLocations()
                .get(0),
            SRC,
            1,
            1 << ForgeDirection.UP.ordinal(),
            7);
        assertEquals(1, source.stackReads);
        assertThrows(
            IllegalArgumentException.class,
            () -> service(fixture).contents(selected.slots(2, 2), ALL, new QueryBudget()));
        assertEquals(1, source.stackReads);
        owner = new QueryBudget();
        Group union = service(fixture).contents(SOURCE, ALL, owner)
            .groups(owner)
            .get(0);
        assertEquals(9L, union.getTotal());
        assertEquals(
            2,
            union.getLocations()
                .size());
        assertEquals(3, source.stackReads);
    }

    @Test
    void unavailableGroupsNeverReturnPartialStockAndChestAmbiguityFaults() throws Exception {
        for (int kind = 0; kind < 4; kind++) {
            Fixture fixture = fixture();
            fixture.source.setInventorySlotContents(0, stack(SCAN_ITEM, 17, 0, null));
            BlockPos bad = kind == 1 ? new BlockPos(40, 64, 40) : DST;
            fixture.labels.addPosition("source", bad);
            if (kind == 0) fixture.world.tiles.remove(bad);
            if (kind == 1) fixture.world.tiles.put(bad, new InventoryTile(1));
            if (kind == 2) fixture.world.unloaded.add(bad);
            if (kind == 3) fixture.world.tiles.put(bad, new TileEntity());
            assertThrows(
                IllegalArgumentException.class,
                () -> service(fixture).contents(SOURCE, ALL, new QueryBudget()));
            assertEquals(17, count(fixture.source));
        }
        Fixture fixture = fixture();
        BlockPos center = new BlockPos(0, 64, -1);
        CountingChest chest = chest(fixture, center, 5);
        fixture.labels.addPosition("chest", center);
        fixture.world.unloaded.add(center.offset(ForgeDirection.WEST));
        assertThrows(
            IllegalArgumentException.class,
            () -> service(fixture).contents(LabelReference.literal("chest"), ALL, new QueryBudget()));
        assertEquals(0, chest.stackReads);
        fixture.world.unloaded.clear();
        chest(fixture, center.offset(ForgeDirection.WEST), 3);
        chest(fixture, center.offset(ForgeDirection.EAST), 7);
        assertThrows(
            IllegalArgumentException.class,
            () -> service(fixture).contents(LabelReference.literal("chest"), ALL, new QueryBudget()));
        assertEquals(0, chest.stackReads);
    }

    @Test
    void earlierSnapshotSurvivesMutationReplacementAndUnloadingButRequiresItsOwner() throws Exception {
        Fixture fixture = fixture();
        CountingInventory source = inventory(fixture, SRC, 1);
        ItemStack original = stack(SCAN_ITEM, 9, 3, "before");
        source.setInventorySlotContents(0, original);
        ItemQueryService service = service(fixture);
        QueryBudget owner = new QueryBudget();
        ItemContents snapshot = service.contents(SOURCE, ALL, owner);
        Group group = snapshot.groups(owner)
            .get(0);
        ItemIdentity identity = group.getItem();
        original.stackSize = 1;
        original.setItemDamage(4);
        original.getTagCompound()
            .setString("name", "after");
        CountingInventory replacement = inventory(fixture, SRC, 1);
        replacement.setInventorySlotContents(0, stack(SCAN_ITEM, 21, 0, null));
        QueryBudget freshOwner = new QueryBudget();
        assertEquals(
            21L,
            service.contents(SOURCE, ALL, freshOwner)
                .groups(freshOwner)
                .get(0)
                .getTotal());
        fixture.world.unloaded.add(SRC);
        assertThrows(IllegalArgumentException.class, () -> service.contents(SOURCE, ALL, new QueryBudget()));
        assertEquals(
            9L,
            snapshot.groups(owner)
                .get(0)
                .getTotal());
        assertEquals(identity(3, "before"), identity);
        assertLocation(
            group.getLocations()
                .get(0),
            SRC,
            0,
            63,
            9);
        assertThrows(
            UnsupportedOperationException.class,
            () -> snapshot.groups(owner)
                .clear());
        assertThrows(
            UnsupportedOperationException.class,
            () -> group.getLocations()
                .clear());
        assertThrows(IllegalArgumentException.class, () -> snapshot.groups(freshOwner));
        assertThrows(IllegalArgumentException.class, () -> snapshot.groups(null));
        assertEquals(1, source.stackReads);
        assertEquals(1, replacement.stackReads);
    }

    @Test
    void knownZeroStockAndNoMatchesAreEmptyWhileMissingOrHiddenViewsAreUnavailable() throws Exception {
        Fixture fixture = fixture();
        fixture.source.setInventorySlotContents(0, stack(SCAN_ITEM, 0, 0, null));
        ItemQueryService service = service(fixture);
        QueryBudget owner = new QueryBudget();
        assertTrue(
            service.contents(SOURCE, ALL, owner)
                .groups(owner)
                .isEmpty());
        fixture.source.setInventorySlotContents(0, stack(SCAN_ITEM, 7, 0, null));
        owner = new QueryBudget();
        assertTrue(
            service.contents(SOURCE, ResourceSelector.item("sfm_contents_test:absent"), owner)
                .groups(owner)
                .isEmpty());
        assertThrows(
            IllegalArgumentException.class,
            () -> service.contents(LabelReference.literal("missing"), ALL, new QueryBudget()));
        fixture.labels.addReferencedLabel("known_empty_label");
        assertThrows(
            IllegalArgumentException.class,
            () -> service.contents(LabelReference.literal("known_empty_label"), ALL, new QueryBudget()));
        SidedInventory hidden = new SidedInventory(1);
        fixture.world.tiles.put(SRC, hidden);
        assertThrows(IllegalArgumentException.class, () -> service.contents(SOURCE, ALL, new QueryBudget()));
        assertEquals(0, hidden.stackReads);
    }

    @Test
    void sharedBudgetProviderAndMalformedStackFailuresCannotProduceASnapshot() throws Exception {
        Fixture fixture = fixture();
        CountingInventory source = inventory(fixture, SRC, 1);
        source.setInventorySlotContents(0, stack(SCAN_ITEM, 5, 0, null));
        ItemQueryService service = service(fixture);
        QueryBudget noAllocations = new QueryBudget();
        noAllocations.chargeAllocation(QueryBudget.MAX_ALLOCATIONS);
        assertThrows(IllegalArgumentException.class, () -> service.contents(SOURCE, ALL, noAllocations));
        QueryBudget noProviders = new QueryBudget();
        noProviders.chargeProviders(QueryBudget.MAX_PROVIDER_CALLS);
        assertThrows(IllegalArgumentException.class, () -> service.contents(SOURCE, ALL, noProviders));
        assertEquals(0, source.stackReads);
        source.failStack = true;
        assertThrows(IllegalArgumentException.class, () -> service.contents(SOURCE, ALL, new QueryBudget()));
        source.failStack = false;
        source.inv.getStackInSlot(0).stackSize = -1;
        assertThrows(IllegalArgumentException.class, () -> service.contents(SOURCE, ALL, new QueryBudget()));
        source.inv.getStackInSlot(0).stackSize = 5;
        NBTTagCompound huge = new NBTTagCompound();
        huge.setByteArray("too_big", new byte[8192]);
        source.inv.getStackInSlot(0)
            .setTagCompound(huge);
        assertThrows(IllegalArgumentException.class, () -> service.contents(SOURCE, ALL, new QueryBudget()));
        assertEquals(5, source.inv.getStackInSlot(0).stackSize);
        assertThrows(IllegalArgumentException.class, () -> service.contents(null, ALL, new QueryBudget()));
        assertThrows(IllegalArgumentException.class, () -> service.contents(SOURCE, null, new QueryBudget()));
        assertThrows(IllegalArgumentException.class, () -> service.contents(SOURCE, ALL, null));
    }

    @Test
    void distinctGroupsDoNotMultiplyInventoryReadsAndOversizedResultsFailClosed() throws Exception {
        Fixture fixture = fixture();
        CountingInventory first = inventory(fixture, SRC, 32), second = inventory(fixture, DST, 24);
        fixture.labels.addPosition("source", DST);
        for (int i = 0; i < 32; i++) first.setInventorySlotContents(i, stack(SCAN_ITEM, 1, i, null));
        for (int i = 0; i < 24; i++) second.setInventorySlotContents(i, stack(SCAN_ITEM, 2, i, null));
        QueryBudget owner = new QueryBudget();
        List<Group> groups = service(fixture).contents(SOURCE, ALL, owner)
            .groups(owner);
        assertEquals(32, groups.size());
        assertEquals(32, first.stackReads);
        assertEquals(24, second.stackReads);
        for (Group group : groups) assertEquals(
            group.getItem()
                .getMetadata() < 24 ? 3L : 1L,
            group.getTotal());
        // The result and invocation caps are both mandatory fail-closed bounds; either may reject first.
        Fixture tooManyGroups = fixture();
        CountingInventory varied = inventory(tooManyGroups, SRC, 257);
        for (int i = 0; i < 257; i++) varied.setInventorySlotContents(i, stack(SCAN_ITEM, 1, i, null));
        assertThrows(
            IllegalArgumentException.class,
            () -> service(tooManyGroups).contents(SOURCE, ALL, new QueryBudget()));
        Fixture tooManyLocations = fixture();
        CountingInventory repeated = inventory(tooManyLocations, SRC, 1025);
        for (int i = 0; i < 1025; i++) repeated.setInventorySlotContents(i, stack(SCAN_ITEM, 1, 0, null));
        assertThrows(
            IllegalArgumentException.class,
            () -> service(tooManyLocations).contents(SOURCE, ALL, new QueryBudget()));
    }

    private static Fixture fixture() throws Exception {
        Fixture fixture = new Fixture();
        WorldProviderSurface provider = allocate(WorldProviderSurface.class);
        provider.dimensionId = DIMENSION;
        Field field = World.class.getDeclaredField("provider");
        field.setAccessible(true);
        field.set(fixture.world, provider);
        return fixture;
    }

    private static ItemQueryService service(Fixture fixture) {
        return new ItemQueryService(
            new ProgramContext(
                fixture.world,
                null,
                new CableNetwork(fixture.world, MANAGER),
                fixture.labels,
                20,
                false));
    }

    private static ItemStack stack(Item item, int amount, int metadata, String name) {
        ItemStack stack = new ItemStack(item, amount, metadata);
        stack.setTagCompound(tag(name));
        return stack;
    }

    private static NBTTagCompound tag(String name) {
        if (name == null) return null;
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("name", name);
        return tag;
    }

    private static ItemIdentity identity(int metadata, String name) {
        return ItemIdentity.of("sfm_contents_test:item", metadata, tag(name), new QueryBudget());
    }

    private static CountingInventory inventory(Fixture fixture, BlockPos pos, int size) {
        CountingInventory inventory = new CountingInventory(size);
        fixture.world.tiles.put(pos, inventory);
        return inventory;
    }

    private static void assertLocation(Location location, BlockPos pos, int slot, int sideMask, long amount) {
        assertEquals(DIMENSION, location.dimension);
        assertEquals(pos.x, location.x);
        assertEquals(pos.y, location.y);
        assertEquals(pos.z, location.z);
        assertEquals(slot, location.slot);
        assertEquals(sideMask, location.sideMask);
        assertEquals(amount, location.amount);
    }

    private static CountingChest chest(Fixture fixture, BlockPos pos, int amount) {
        CountingChest chest = new CountingChest();
        chest.setInventorySlotContents(0, stack(SCAN_ITEM, amount, 0, null));
        chest.xCoord = pos.x;
        chest.yCoord = pos.y;
        chest.zCoord = pos.z;
        chest.setWorldObj(fixture.world);
        fixture.world.tiles.put(pos, chest);
        return chest;
    }

    private static class CountingInventory extends InventoryTile {

        int stackReads, mutations;
        boolean failStack;

        CountingInventory(int size) {
            super(size);
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            stackReads++;
            if (failStack) throw new IllegalStateException("contents fixture failure");
            return super.getStackInSlot(slot);
        }

        @Override
        public ItemStack decrStackSize(int slot, int amount) {
            mutations++;
            throw new AssertionError("Contents must not extract stock");
        }
    }

    private static final class SidedInventory extends CountingInventory implements ISidedInventory {

        final int[][] faces = new int[6][];

        SidedInventory(int size) {
            super(size);
            Arrays.fill(faces, null);
        }

        @Override
        public int[] getAccessibleSlotsFromSide(int side) {
            return faces[side];
        }

        @Override
        public boolean canExtractItem(int slot, ItemStack stack, int side) {
            throw new AssertionError("Contents must not ask transfer permission");
        }

        @Override
        public boolean canInsertItem(int slot, ItemStack stack, int side) {
            throw new AssertionError("Contents must not ask transfer permission");
        }
    }

    private static final class CountingChest extends TileEntityChest {

        int stackReads;

        @Override
        public ItemStack getStackInSlot(int slot) {
            stackReads++;
            return super.getStackInSlot(slot);
        }

        @Override
        public void checkForAdjacentChests() {
            throw new AssertionError("Contents must use loaded-boundary chest discovery");
        }
    }
}
