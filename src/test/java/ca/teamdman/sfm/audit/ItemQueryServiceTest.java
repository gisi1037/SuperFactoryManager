package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.RegistrySimple;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.block.BlockCable;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ItemQueryService;
import ca.teamdman.sfm.common.program.ItemQueryService.Access;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;

/** World-facing observations: no reservation/extraction, no partial counts, and no stale input cache. */
class ItemQueryServiceTest {

    private static final LabelReference SOURCE = LabelReference.literal("source");
    private static final ResourceSelector ALL = ResourceSelector.item("*");
    private static final Item FILTER_ITEM = new Item(), FILTER_OTHER = new Item();

    static {
        try {
            // Only the real registry name lookup exercised by selectors; no Forge registration lifecycle in JUnit.
            Field backing = RegistrySimple.class.getDeclaredField("registryObjects");
            backing.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> names = (Map<String, Object>) backing.get(Item.itemRegistry);
            names.put("sfm_query_test:kept", FILTER_ITEM);
            names.put("sfm_query_test:other", FILTER_OTHER);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    @Test
    void countAndEmptyObserveSelectedLiveStockWithoutTreatingMissingLabelsAsEmpty() throws Exception {
        Fixture fixture = new Fixture();
        fixture.source.setInventorySlotContents(0, new ItemStack(FILTER_ITEM, 7));
        fixture.source.setInventorySlotContents(1, new ItemStack(FILTER_OTHER, 13));
        ItemQueryService service = service(fixture);
        assertEquals(20, service.count(SOURCE, ALL, new QueryBudget()));
        assertEquals(7, service.count(SOURCE, ResourceSelector.item("sfm_query_test:kept"), new QueryBudget()));
        assertFalse(service.empty(SOURCE, ALL, new QueryBudget()));
        assertTrue(service.empty(SOURCE, ResourceSelector.item("sfm_query_test:absent"), new QueryBudget()));
        assertTrue(service.available(SOURCE, ResourceSelector.item("sfm_query_test:absent"), new QueryBudget()));
        assertUnavailable(service, LabelReference.literal("missing"));
        fixture.labels.addReferencedLabel("empty_group");
        assertUnavailable(service, LabelReference.literal("empty_group"));
    }

    @Test
    void partiallyMissingDisconnectedUnloadedOrUnsupportedGroupsNeverReturnAPartialCount() throws Exception {
        for (int kind = 0; kind < 4; kind++) {
            Fixture fixture = new Fixture();
            fixture.stock(0, 17);
            BlockPos bad = kind == 0 ? DST : kind == 1 ? new BlockPos(40, 64, 40) : EXTRA;
            fixture.labels.addPosition("source", bad);
            if (kind == 0) fixture.world.tiles.remove(bad);
            if (kind == 1) fixture.world.tiles.put(bad, new InventoryTile(1));
            if (kind == 2) {
                fixture.world.tiles.put(bad, new InventoryTile(1));
                fixture.world.unloaded.add(bad);
            }
            if (kind == 3) fixture.world.tiles.put(bad, new TileEntity());
            assertUnavailable(service(fixture), SOURCE);
            assertEquals(17, AuditRegressionTest.count(fixture.source));
        }
    }

    @Test
    void everyQueryRechecksLoadedStateAndReplacementTileInsteadOfReusingAnEarlierInventory() throws Exception {
        Fixture fixture = new Fixture();
        fixture.stock(0, 9);
        ItemQueryService service = service(fixture);
        assertEquals(9, service.count(SOURCE, ALL, new QueryBudget()));
        CountingInventory replacement = new CountingInventory(1);
        replacement.setInventorySlotContents(0, new ItemStack(ITEM, 21));
        fixture.world.tiles.put(SRC, replacement);
        assertEquals(21, service.count(SOURCE, ALL, new QueryBudget()));
        fixture.world.unloaded.add(SRC);
        assertUnavailable(service, SOURCE); // FakeWorld throws on any unguarded unloaded lookup.
        fixture.world.unloaded.remove(SRC);
        fixture.world.tiles.put(SRC, new TileEntity());
        assertUnavailable(service, SOURCE);
        fixture.world.tiles.put(SRC, replacement);
        CableNetwork.invalidate(fixture.world);
        assertEquals(21, service.count(SOURCE, ALL, new QueryBudget()));
    }

    @Test
    void aliasesOfTheSamePhysicalInventoryAreCountedOnlyOnce() throws Exception {
        Fixture fixture = new Fixture();
        CountingInventory shared = new CountingInventory(1);
        shared.setInventorySlotContents(0, new ItemStack(ITEM, 11));
        fixture.world.tiles.put(SRC, shared);
        fixture.world.tiles.put(DST, shared);
        fixture.labels.addPosition("source", DST);
        fixture.labels.addPosition("alias", DST);
        ItemQueryService service = service(fixture);
        assertEquals(11, service.count(SOURCE, ALL, new QueryBudget()));
        assertEquals(1, shared.stackReads);
        shared.stackReads = 0;
        assertEquals(11, service.count(LabelReference.literal("alias"), ALL, new QueryBudget()));
        assertEquals(1, shared.stackReads);
    }

    @Test
    void defaultSidedUnionDeduplicatesAndIgnoresInvalidIndicesWithoutCallingExtractionChecks() throws Exception {
        Fixture fixture = new Fixture();
        SidedInventory sided = new SidedInventory(3);
        sided.setInventorySlotContents(0, new ItemStack(ITEM, 3));
        sided.setInventorySlotContents(1, new ItemStack(ITEM, 5));
        sided.setInventorySlotContents(2, new ItemStack(ITEM, 47)); // hidden on all physical faces
        Arrays.fill(sided.faces, null);
        sided.faces[ForgeDirection.DOWN.ordinal()] = new int[] { -1, 0, 0, 3, Integer.MAX_VALUE };
        sided.faces[ForgeDirection.UP.ordinal()] = new int[] { 1, 0, 1 };
        fixture.world.tiles.put(SRC, sided);
        ItemQueryService service = service(fixture);
        assertTrue(service.available(SOURCE, ALL, new QueryBudget()));
        assertEquals(0, sided.stackReads, "available reports accessibility without reading or matching stacks");
        assertEquals(8, service.count(SOURCE, ALL, new QueryBudget()));
        assertEquals(2, sided.stackReads, "Each physically visible slot is counted once across six sides");
        assertEquals(0, sided.extractCalls);
        assertEquals(0, sided.insertCalls);
        assertEquals(47, sided.inv.getStackInSlot(2).stackSize);
    }

    @Test
    void zeroNullAndOnlyInvalidAccessibleSlotsRemainUnavailableRatherThanEmpty() throws Exception {
        for (int[] exposed : new int[][] { null, new int[0], new int[] { -1, 1, 1, Integer.MAX_VALUE } }) {
            Fixture fixture = new Fixture();
            SidedInventory sided = new SidedInventory(1);
            Arrays.fill(sided.faces, exposed);
            fixture.world.tiles.put(SRC, sided);
            assertUnavailable(service(fixture), SOURCE);
            assertEquals(0, sided.stackReads);
        }
    }

    @Test
    void explicitSideAndSlotRestrictionsUseTheSameObservationContract() throws Exception {
        Fixture fixture = new Fixture();
        SidedInventory sided = new SidedInventory(3);
        sided.setInventorySlotContents(0, new ItemStack(ITEM, 2));
        sided.setInventorySlotContents(1, new ItemStack(ITEM, 7));
        sided.setInventorySlotContents(2, new ItemStack(ITEM, 13));
        Arrays.fill(sided.faces, null);
        sided.faces[ForgeDirection.UP.ordinal()] = new int[] { 0, 1 };
        sided.faces[ForgeDirection.DOWN.ordinal()] = new int[] { 2 };
        fixture.world.tiles.put(SRC, sided);
        ItemQueryService service = service(fixture);
        Access upperSlotOne = new Access(1 << ForgeDirection.UP.ordinal(), 1, 1);
        assertEquals(7, service.count(SOURCE, ALL, new QueryBudget(), upperSlotOne));
        assertTrue(service.available(SOURCE, ALL, new QueryBudget(), upperSlotOne));
        assertFalse(service.empty(SOURCE, ALL, new QueryBudget(), upperSlotOne));
        LabelReference selected = SOURCE.side(ForgeDirection.UP.ordinal())
            .slots(1, 1);
        assertEquals(7, service.count(selected, ALL, new QueryBudget()));
        sided.stackReads = 0;
        assertTrue(service.available(selected, ALL, new QueryBudget()));
        assertEquals(0, sided.stackReads);
        assertFalse(service.empty(selected, ALL, new QueryBudget()));
        assertEquals(
            22,
            service.count(SOURCE, ALL, new QueryBudget()),
            "The original handle retains all visible slots");
        Access hiddenUpperSlotTwo = new Access(1 << ForgeDirection.UP.ordinal(), 2, 2);
        assertFalse(service.available(SOURCE, ALL, new QueryBudget(), hiddenUpperSlotTwo));
        assertFalse(service.empty(SOURCE, ALL, new QueryBudget(), hiddenUpperSlotTwo));
        assertThrows(
            IllegalArgumentException.class,
            () -> service.count(SOURCE, ALL, new QueryBudget(), hiddenUpperSlotTwo));
        LabelReference hidden = selected.slots(2, 2);
        assertFalse(service.available(hidden, ALL, new QueryBudget()));
        assertFalse(service.empty(hidden, ALL, new QueryBudget()));
        assertThrows(IllegalArgumentException.class, () -> service.count(hidden, ALL, new QueryBudget()));
        assertEquals(0, sided.extractCalls);
        assertEquals(0, sided.insertCalls);
    }

    @Test
    void countIsFreshAfterRealLegacyTransferAndDoesNotChangeLegacyHasOrReservations() throws Exception {
        Fixture fixture = new Fixture();
        fixture.stock(0, 12);
        ProgramContext context = context(fixture);
        ItemQueryService service = new ItemQueryService(context);
        assertEquals(12, service.count(SOURCE, ALL, new QueryBudget()));
        parse("EVERY 20 TICKS DO IF source HAS >= 10 THEN INPUT 5 FROM source OUTPUT TO dest END END").tick(context);
        assertEquals(5, AuditRegressionTest.count(fixture.dest));
        assertEquals(7, AuditRegressionTest.count(fixture.source));
        assertEquals(7, service.count(SOURCE, ALL, new QueryBudget()));
        fixture.source.inv.getStackInSlot(0).stackSize = 2;
        assertEquals(2, service.count(SOURCE, ALL, new QueryBudget()));
    }

    @Test
    void providerExceptionsAndNegativeSizesFaultInsteadOfBecomingFalseOrZero() throws Exception {
        for (int fault = 0; fault < 3; fault++) {
            Fixture fixture = new Fixture();
            SidedInventory provider = new SidedInventory(1);
            provider.failSize = fault == 0;
            provider.failAccess = fault == 1;
            if (fault == 2) provider.reportedSize = -1;
            fixture.world.tiles.put(SRC, provider);
            ItemQueryService service = service(fixture);
            assertThrows(IllegalArgumentException.class, () -> service.count(SOURCE, ALL, new QueryBudget()));
            assertThrows(IllegalArgumentException.class, () -> service.empty(SOURCE, ALL, new QueryBudget()));
            assertThrows(IllegalArgumentException.class, () -> service.available(SOURCE, ALL, new QueryBudget()));
        }
        Fixture fixture = new Fixture();
        CountingInventory provider = new CountingInventory(1);
        provider.failStack = true;
        fixture.world.tiles.put(SRC, provider);
        ItemQueryService service = service(fixture);
        assertTrue(service.available(SOURCE, ALL, new QueryBudget()));
        assertEquals(0, provider.stackReads);
        assertThrows(IllegalArgumentException.class, () -> service.count(SOURCE, ALL, new QueryBudget()));
        assertThrows(IllegalArgumentException.class, () -> service.empty(SOURCE, ALL, new QueryBudget()));
    }

    @Test
    void invalidNegativeAmountsFaultEvenForNonmatchingSelectorsButZeroAmountsAreEmpty() throws Exception {
        Fixture fixture = new Fixture();
        ItemStack stack = new ItemStack(FILTER_ITEM, 1);
        fixture.source.setInventorySlotContents(0, stack);
        ItemQueryService service = service(fixture);
        stack.stackSize = -1;
        ResourceSelector nonmatching = ResourceSelector.item("sfm_query_test:other");
        assertTrue(service.available(SOURCE, nonmatching, new QueryBudget()));
        assertThrows(IllegalArgumentException.class, () -> service.count(SOURCE, nonmatching, new QueryBudget()));
        assertThrows(IllegalArgumentException.class, () -> service.empty(SOURCE, nonmatching, new QueryBudget()));
        stack.stackSize = 0;
        assertEquals(0, service.count(SOURCE, ALL, new QueryBudget()));
        assertTrue(service.empty(SOURCE, ALL, new QueryBudget()));
    }

    @Test
    void absurdUnsidedSizeAndOversizedRawSidedArraysFaultBeforeAnyStackReads() throws Exception {
        Fixture fixture = new Fixture();
        CountingInventory huge = new CountingInventory(1);
        huge.reportedSize = Integer.MAX_VALUE;
        fixture.world.tiles.put(SRC, huge);
        ItemQueryService service = service(fixture);
        assertThrows(IllegalArgumentException.class, () -> service.count(SOURCE, ALL, new QueryBudget()));
        assertThrows(IllegalArgumentException.class, () -> service.empty(SOURCE, ALL, new QueryBudget()));
        assertThrows(IllegalArgumentException.class, () -> service.available(SOURCE, ALL, new QueryBudget()));
        assertEquals(0, huge.stackReads);
        SidedInventory largeArray = new SidedInventory(1);
        Arrays.fill(largeArray.faces, new int[QueryBudget.MAX_SLOT_WORK + 1]);
        fixture.world.tiles.put(SRC, largeArray);
        QueryBudget budget = new QueryBudget();
        IllegalArgumentException failure = assertThrows(
            IllegalArgumentException.class,
            () -> service.count(SOURCE, ALL, budget));
        assertTrue(
            failure.getMessage()
                .contains("slot work"),
            failure.getMessage());
        assertEquals(0, largeArray.stackReads);
        assertEquals(0, budget.getSlotWork(), "A rejected raw array is charged atomically before iteration");
    }

    @Test
    void hugeSidedInventoryCanExposeASmallSparseUnionWithoutSizeProportionalAllocation() throws Exception {
        Fixture fixture = new Fixture();
        SidedInventory sparse = new SidedInventory(1);
        sparse.reportedSize = Integer.MAX_VALUE;
        sparse.setInventorySlotContents(0, new ItemStack(ITEM, 4));
        sparse.virtual.put(Integer.MAX_VALUE - 1, new ItemStack(ITEM, 9));
        Arrays.fill(sparse.faces, new int[] { 0, Integer.MAX_VALUE - 1 });
        fixture.world.tiles.put(SRC, sparse);
        QueryBudget budget = new QueryBudget();
        assertEquals(13, service(fixture).count(SOURCE, ALL, budget));
        assertEquals(2, sparse.stackReads);
        assertTrue(budget.getAllocations() < 32);
        assertTrue(budget.getSlotWork() < 32);
    }

    @Test
    void invocationBudgetsRemainSharedAndExhaustionIsNeverAnUnavailableResult() throws Exception {
        Fixture fixture = new Fixture();
        ItemQueryService service = service(fixture);
        QueryBudget shared = new QueryBudget();
        for (int i = 0; i < QueryBudget.MAX_POSITIONS - 1; i++) shared.chargePosition();
        assertTrue(service.available(SOURCE, ALL, shared));
        assertEquals(QueryBudget.MAX_POSITIONS, shared.getPositions());
        assertThrows(IllegalArgumentException.class, () -> service.available(SOURCE, ALL, shared));
        for (int operation = 0; operation < 3; operation++) {
            QueryBudget exhausted = new QueryBudget();
            exhausted.chargeAllocation(QueryBudget.MAX_ALLOCATIONS);
            final int query = operation;
            assertThrows(IllegalArgumentException.class, () -> {
                if (query == 0) service.count(SOURCE, ALL, exhausted);
                else if (query == 1) service.empty(SOURCE, ALL, exhausted);
                else service.available(SOURCE, ALL, exhausted);
            });
        }
    }

    @Test
    void vanillaChestPairUsesPhysicalDedupAndStableCombinedSlotOrderFromEitherHalf() throws Exception {
        Fixture fixture = new Fixture();
        BlockPos left = new BlockPos(0, 64, -1), right = new BlockPos(1, 64, -1);
        CountingChest first = chest(fixture, left, 3), second = chest(fixture, right, 7);
        fixture.labels.addPosition("left", left);
        fixture.labels.addPosition("right", right);
        fixture.labels.addPosition("both", right); // reverse label order must not reverse combined chest slots
        fixture.labels.addPosition("both", left);
        fixture.world.blocks.put(SRC, new BlockCable());
        ItemQueryService service = service(fixture);
        assertEquals(10, service.count(LabelReference.literal("both"), ALL, new QueryBudget()));
        assertEquals(27, first.stackReads);
        assertEquals(27, second.stackReads);
        Access combinedFirstSlot = new Access(63, 0, 0);
        Access combinedSecondHalf = new Access(63, 27, 27);
        for (String label : new String[] { "left", "right", "both" }) {
            assertEquals(3, service.count(LabelReference.literal(label), ALL, new QueryBudget(), combinedFirstSlot));
            assertEquals(7, service.count(LabelReference.literal(label), ALL, new QueryBudget(), combinedSecondHalf));
            assertEquals(
                3,
                service.count(
                    LabelReference.literal(label)
                        .side(2)
                        .slots(0, 0),
                    ALL,
                    new QueryBudget()));
            assertEquals(
                7,
                service.count(
                    LabelReference.literal(label)
                        .side(1)
                        .slots(27, 27),
                    ALL,
                    new QueryBudget()));
        }
    }

    @Test
    void chestObservationsRefuseUnloadedNeighborsAndFaultOnAmbiguousPhysicalGroups() throws Exception {
        Fixture fixture = new Fixture();
        BlockPos chest = new BlockPos(0, 64, -1);
        CountingChest first = chest(fixture, chest, 5);
        fixture.labels.addPosition("chest", chest);
        ItemQueryService service = service(fixture);
        fixture.world.unloaded.add(chest.offset(ForgeDirection.WEST));
        assertUnavailable(service, LabelReference.literal("chest"));
        assertEquals(0, first.stackReads);
        fixture.world.unloaded.clear();
        chest(fixture, chest.offset(ForgeDirection.WEST), 3);
        chest(fixture, chest.offset(ForgeDirection.EAST), 7);
        assertThrows(
            IllegalArgumentException.class,
            () -> service.available(LabelReference.literal("chest"), ALL, new QueryBudget()));
        assertThrows(
            IllegalArgumentException.class,
            () -> service.empty(LabelReference.literal("chest"), ALL, new QueryBudget()));
        assertThrows(
            IllegalArgumentException.class,
            () -> service.count(LabelReference.literal("chest"), ALL, new QueryBudget()));
        assertEquals(0, first.stackReads);
    }

    private static ProgramContext context(Fixture fixture) {
        return new ProgramContext(
            fixture.world,
            null,
            new CableNetwork(fixture.world, MANAGER),
            fixture.labels,
            20,
            false);
    }

    private static ItemQueryService service(Fixture fixture) {
        return new ItemQueryService(context(fixture));
    }

    private static void assertUnavailable(ItemQueryService service, LabelReference label) {
        assertFalse(service.available(label, ALL, new QueryBudget()));
        assertFalse(service.empty(label, ALL, new QueryBudget()));
        assertThrows(IllegalArgumentException.class, () -> service.count(label, ALL, new QueryBudget()));
    }

    private static CountingChest chest(Fixture fixture, BlockPos pos, int amount) {
        CountingChest chest = new CountingChest();
        // Vanilla markDirty is safe before attaching to the deliberately minimal fake world.
        chest.setInventorySlotContents(0, new ItemStack(ITEM, amount));
        chest.xCoord = pos.x;
        chest.yCoord = pos.y;
        chest.zCoord = pos.z;
        chest.setWorldObj(fixture.world);
        fixture.world.tiles.put(pos, chest);
        return chest;
    }

    private static class CountingInventory extends InventoryTile {

        int stackReads;
        Integer reportedSize;
        boolean failSize, failStack;
        final Map<Integer, ItemStack> virtual = new HashMap<>();

        CountingInventory(int size) {
            super(size);
        }

        @Override
        public int getSizeInventory() {
            if (failSize) throw new IllegalStateException("size fixture failure");
            return reportedSize == null ? super.getSizeInventory() : reportedSize;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            stackReads++;
            if (failStack) throw new IllegalStateException("stack fixture failure");
            if (virtual.containsKey(slot)) return virtual.get(slot);
            if (slot < 0 || slot >= inv.getSizeInventory())
                throw new AssertionError("Invalid provider slot read: " + slot);
            return super.getStackInSlot(slot);
        }
    }

    private static final class SidedInventory extends CountingInventory implements ISidedInventory {

        final int[][] faces = new int[6][];
        int extractCalls, insertCalls;
        boolean failAccess;

        SidedInventory(int size) {
            super(size);
            Arrays.fill(faces, new int[] { 0 });
        }

        @Override
        public int[] getAccessibleSlotsFromSide(int side) {
            if (failAccess) throw new IllegalStateException("access fixture failure");
            return faces[side];
        }

        @Override
        public boolean canExtractItem(int slot, ItemStack stack, int side) {
            extractCalls++;
            throw new AssertionError("Observation must not ask whether extraction is permitted");
        }

        @Override
        public boolean canInsertItem(int slot, ItemStack stack, int side) {
            insertCalls++;
            throw new AssertionError("Observation must not ask whether insertion is permitted");
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
            throw new AssertionError("Typed queries must not invoke unsafe vanilla adjacency discovery");
        }
    }
}
