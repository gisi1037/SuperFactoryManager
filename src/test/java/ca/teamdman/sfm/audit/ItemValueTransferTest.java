package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.RegistryNamespaced;
import net.minecraft.util.RegistrySimple;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.block.BlockCable;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ItemContents;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.ItemTransferResult;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;

/** Real inventory/recovery path behind value transfers; completed snapshots never authorize extraction. */
class ItemValueTransferTest {

    private static final Item TRANSFER_ITEM = new Item(), TRANSFER_OTHER = new Item();
    private static final LabelReference SOURCE = LabelReference.literal("source"),
        DEST = LabelReference.literal("dest");
    private static final DataValue ALL = DataValue.selector(ResourceSelector.item("*"));

    static {
        try {
            // Populate only vanilla lookups exercised by exact keys and actual recovery ItemStack codecs.
            Field namesField = RegistrySimple.class.getDeclaredField("registryObjects");
            namesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> names = (Map<String, Object>) namesField.get(Item.itemRegistry);
            names.put("sfm_transfer_test:item", TRANSFER_ITEM);
            names.put("sfm_transfer_test:other", TRANSFER_OTHER);
            Field idsField = RegistryNamespaced.class.getDeclaredField("underlyingIntegerMap");
            idsField.setAccessible(true);
            ObjectIntIdentityMap ids = (ObjectIntIdentityMap) idsField.get(Item.itemRegistry);
            ids.func_148746_a(TRANSFER_ITEM, 32011);
            ids.func_148746_a(TRANSFER_OTHER, 32012);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    @Test
    void exactKeyRequiresRegistryMetadataAndFullNbt() throws Exception {
        Fixture fixture = new Fixture();
        CountingInventory source = source(fixture, 5);
        source.setInventorySlotContents(0, stack(TRANSFER_OTHER, 2, 7, tag("same")));
        source.setInventorySlotContents(1, stack(TRANSFER_ITEM, 3, 8, tag("same")));
        source.setInventorySlotContents(2, stack(TRANSFER_ITEM, 5, 7, tag("different")));
        source.setInventorySlotContents(3, stack(TRANSFER_ITEM, 11, 7, tag("same")));
        source.setInventorySlotContents(4, stack(TRANSFER_ITEM, 13, 7, tag("same")));
        ProgramContext context = context(fixture);
        ItemTransferResult result = context.transferItems(SOURCE, DEST, key(7, tag("same")), 17);
        assertResult(result, 17, 17, "COMPLETE");
        assertEquals(17, count(fixture.dest));
        assertEquals(17, count(source));
        assertEquals(2, source.inv.getStackInSlot(0).stackSize);
        assertEquals(3, source.inv.getStackInSlot(1).stackSize);
        assertEquals(5, source.inv.getStackInSlot(2).stackSize);
        assertEquals(
            7,
            fixture.dest.getStackInSlot(0)
                .getItemDamage());
        assertEquals(
            tag("same"),
            fixture.dest.getStackInSlot(0)
                .getTagCompound());
        assertFalse(
            context.getManager()
                .hasPendingRecovery());
    }

    @Test
    void absentAndEmptyCompoundNbtAreDifferentExactKeys() throws Exception {
        Fixture fixture = new Fixture();
        fixture.source.setInventorySlotContents(0, stack(TRANSFER_ITEM, 5, 0, null));
        fixture.source.setInventorySlotContents(1, stack(TRANSFER_ITEM, 7, 0, new NBTTagCompound()));
        ProgramContext context = context(fixture);
        assertResult(context.transferItems(SOURCE, DEST, key(0, null), 20), 20, 5, "PARTIAL");
        assertEquals(7, count(fixture.source));
        assertNull(
            fixture.dest.getStackInSlot(0)
                .getTagCompound());
        assertResult(context.transferItems(SOURCE, DEST, key(0, new NBTTagCompound()), 7), 7, 7, "COMPLETE");
        assertEquals(0, count(fixture.source));
        assertEquals(12, count(fixture.dest));
        assertNotNull(
            fixture.dest.getStackInSlot(1)
                .getTagCompound());
    }

    @Test
    void broadItemSelectorCanMoveSeveralMetadataAndNbtVariants() throws Exception {
        Fixture fixture = new Fixture();
        fixture.source.setInventorySlotContents(0, stack(TRANSFER_ITEM, 3, 1, tag("a")));
        fixture.source.setInventorySlotContents(1, stack(TRANSFER_ITEM, 4, 2, tag("b")));
        fixture.source.setInventorySlotContents(2, stack(TRANSFER_OTHER, 9, 1, null));
        ProgramContext context = context(fixture);
        DataValue selector = DataValue.selector(ResourceSelector.item("sfm_transfer_test:item"));
        assertResult(context.transferItems(SOURCE, DEST, selector, 7), 7, 7, "COMPLETE");
        assertEquals(9, count(fixture.source));
        assertEquals(7, count(fixture.dest));
        assertSame(
            TRANSFER_OTHER,
            fixture.source.getStackInSlot(2)
                .getItem());
        assertEquals(16, count(fixture.source) + count(fixture.dest));
    }

    @Test
    void totalsDistinguishPartialCompleteNoMatchAndNoCapacity() throws Exception {
        Fixture fixture = new Fixture();
        fixture.source.setInventorySlotContents(0, stack(TRANSFER_ITEM, 10, 0, null));
        InventoryTile destination = new InventoryTile(1);
        destination.setInventorySlotContents(0, stack(TRANSFER_ITEM, 60, 0, null));
        fixture.world.tiles.put(DST, destination);
        ProgramContext context = context(fixture);
        assertResult(context.transferItems(SOURCE, DEST, ALL, 9), 9, 4, "PARTIAL");
        assertResult(context.transferItems(SOURCE, DEST, ALL, 3), 3, 0, "NO_CAPACITY");
        assertEquals(6, count(fixture.source));
        assertEquals(64, count(destination));
        destination.setInventorySlotContents(0, null);
        assertResult(context.transferItems(SOURCE, DEST, key(3, null), 3), 3, 0, "NO_MATCH");
        assertResult(context.transferItems(SOURCE, DEST, ALL, 6), 6, 6, "COMPLETE");
        assertEquals(0, count(fixture.source));
        assertEquals(6, count(destination));
    }

    @Test
    void zeroAndInvalidArgumentsHaveNoProviderOrInventoryEffects() throws Exception {
        Fixture fixture = new Fixture();
        CountingInventory source = source(fixture, 1);
        source.setInventorySlotContents(0, stack(TRANSFER_ITEM, 9, 0, null));
        ProgramContext context = context(fixture);
        fixture.world.unloaded.add(SRC);
        QueryBudget budget = context.getInvocationQueryBudget();
        assertResult(context.transferItems(SOURCE, DEST, ALL, 0), 0, 0, "COMPLETE");
        assertThrows(IllegalArgumentException.class, () -> context.transferItems(SOURCE, DEST, ALL, -1));
        assertThrows(
            IllegalArgumentException.class,
            () -> context.transferItems(SOURCE, DEST, DataValue.integer(1), 3));
        assertThrows(IllegalArgumentException.class, () -> context.transferItems(null, DEST, ALL, 3));
        assertThrows(IllegalArgumentException.class, () -> context.transferItems(SOURCE, null, ALL, 3));
        assertThrows(IllegalArgumentException.class, () -> context.transferItems(SOURCE, DEST, null, 3));
        assertEquals(0, budget.getProviderCalls());
        assertEquals(0, source.reads);
        assertEquals(0, source.extractions);
        assertEquals(9, source.inv.getStackInSlot(0).stackSize);
    }

    @Test
    void explicitSidesAndSlotRangesApplyExtractionAndInsertionPermissions() throws Exception {
        Fixture fixture = new Fixture();
        SidedInventory source = new SidedInventory(3), destination = new SidedInventory(3);
        source.extractSide = ForgeDirection.UP.ordinal();
        source.faces[ForgeDirection.UP.ordinal()] = new int[] { -1, 1, 1, 9 };
        destination.insertSide = ForgeDirection.DOWN.ordinal();
        destination.faces[ForgeDirection.DOWN.ordinal()] = new int[] { 1, 1 };
        source.setInventorySlotContents(0, stack(TRANSFER_ITEM, 13, 0, null));
        source.setInventorySlotContents(1, stack(TRANSFER_ITEM, 8, 0, null));
        fixture.world.tiles.put(SRC, source);
        fixture.world.tiles.put(DST, destination);
        ProgramContext context = context(fixture);
        LabelReference selectedSource = SOURCE.side(ForgeDirection.UP.ordinal())
            .slots(1, 1);
        LabelReference selectedDestination = DEST.side(ForgeDirection.DOWN.ordinal())
            .slots(1, 1);
        assertResult(context.transferItems(selectedSource, selectedDestination, ALL, 3), 3, 3, "COMPLETE");
        assertEquals(18, count(source));
        assertEquals(3, count(destination));
        assertEquals(13, source.inv.getStackInSlot(0).stackSize);
        assertNull(destination.inv.getStackInSlot(0));
        assertEquals(3, destination.inv.getStackInSlot(1).stackSize);
        assertEquals(1, source.extractions);
        assertTrue(source.extractChecks > 0);
        assertTrue(destination.insertChecks > 0);
        source.extractSide = ForgeDirection.EAST.ordinal();
        ItemTransferResult denied = context.transferItems(selectedSource, selectedDestination, ALL, 2);
        assertEquals(0, denied.getMoved());
        assertEquals(1, source.extractions);
        assertEquals(21, count(source) + count(destination));
    }

    @Test
    void aliasesAndDoubleChestViewsNeverCountSamePhysicalSlotAsMoved() throws Exception {
        Fixture fixture = new Fixture();
        CountingInventory shared = source(fixture, 1);
        shared.setInventorySlotContents(0, stack(TRANSFER_ITEM, 9, 0, null));
        fixture.world.tiles.put(DST, shared);
        ProgramContext context = context(fixture);
        ItemTransferResult alias = context.transferItems(SOURCE.slots(0, 0), DEST.slots(0, 0), ALL, 5);
        assertEquals(0, alias.getMoved());
        assertEquals(9, count(shared));
        assertEquals(0, shared.extractions);

        Fixture chests = new Fixture();
        BlockPos left = new BlockPos(0, 64, -1), right = new BlockPos(1, 64, -1);
        CountingChest first = chest(chests, left, 3), second = chest(chests, right, 7);
        chests.labels.addPosition("left", left);
        chests.labels.addPosition("right", right);
        chests.world.blocks.put(SRC, new BlockCable());
        ProgramContext chestContext = context(chests);
        ItemTransferResult same = chestContext.transferItems(
            LabelReference.literal("left")
                .slots(27, 27),
            LabelReference.literal("right")
                .slots(27, 27),
            ALL,
            5);
        assertEquals(0, same.getMoved());
        assertEquals(3, count(first));
        assertEquals(7, count(second));
        assertEquals(0, first.extractions + second.extractions);
    }

    @Test
    void completedSnapshotDoesNotReserveAmountsOrAuthorizeItsOldLocations() throws Exception {
        Fixture fixture = new Fixture();
        CountingInventory source = source(fixture, 3);
        source.setInventorySlotContents(0, stack(TRANSFER_ITEM, 9, 4, tag("snapshot")));
        ProgramContext context = context(fixture);
        QueryBudget owner = context.getInvocationQueryBudget();
        ItemContents snapshot = context.contents(SOURCE, ResourceSelector.item("*"), owner);
        DataValue exact = DataValue.itemIdentity(
            snapshot.groups(owner)
                .get(0)
                .getItem());
        source.setInventorySlotContents(0, stack(TRANSFER_OTHER, 2, 0, null));
        source.setInventorySlotContents(2, stack(TRANSFER_ITEM, 4, 4, tag("snapshot")));
        assertResult(context.transferItems(SOURCE, DEST, exact, 6), 6, 4, "PARTIAL");
        assertEquals(2, count(source));
        assertSame(
            TRANSFER_OTHER,
            source.inv.getStackInSlot(0)
                .getItem());
        assertNull(source.inv.getStackInSlot(2));
        assertEquals(4, count(fixture.dest));
        assertEquals(
            9,
            snapshot.groups(owner)
                .get(0)
                .getTotal());
        assertEquals(
            0,
            snapshot.groups(owner)
                .get(0)
                .getLocations()
                .get(0).slot);
    }

    @Test
    void sourceIdentityMutationDuringCapacitySimulationPreventsExtraction() throws Exception {
        Fixture fixture = new Fixture();
        CountingInventory source = source(fixture, 1);
        source.setInventorySlotContents(0, stack(TRANSFER_ITEM, 9, 4, tag("before")));
        InventoryTile destination = new InventoryTile(1) {

            @Override
            public boolean isItemValidForSlot(int slot, ItemStack stack) {
                source.inv.getStackInSlot(0)
                    .getTagCompound()
                    .setString("name", "after");
                return true;
            }
        };
        fixture.world.tiles.put(DST, destination);
        ProgramContext context = context(fixture);
        assertEquals(
            0,
            context.transferItems(SOURCE, DEST, key(4, tag("before")), 5)
                .getMoved());
        assertEquals(0, source.extractions);
        assertEquals(9, count(source));
        assertEquals(0, count(destination));
        assertFalse(
            context.getManager()
                .hasPendingRecovery());
    }

    @Test
    void replacementOrUnloadingDuringSimulationCannotExtractFromDetachedOrUnloadedSource() throws Exception {
        for (boolean unload : new boolean[] { false, true }) {
            Fixture fixture = new Fixture();
            CountingInventory source = source(fixture, 1), replacement = new CountingInventory(1);
            source.setInventorySlotContents(0, stack(TRANSFER_ITEM, 9, 0, null));
            replacement.setInventorySlotContents(0, stack(TRANSFER_OTHER, 6, 0, null));
            InventoryTile destination = new InventoryTile(1) {

                @Override
                public boolean isItemValidForSlot(int slot, ItemStack stack) {
                    if (unload) fixture.world.unloaded.add(SRC);
                    else fixture.world.tiles.put(SRC, replacement);
                    return true;
                }
            };
            fixture.world.tiles.put(DST, destination);
            ProgramContext context = context(fixture);
            ItemTransferResult result = context.transferItems(SOURCE, DEST, ALL, 5);
            assertEquals(0, result.getMoved());
            assertEquals(0, source.extractions);
            assertEquals(0, replacement.extractions);
            assertEquals(9, count(source));
            assertEquals(6, count(replacement));
            assertEquals(0, count(destination));
            assertFalse(
                context.getManager()
                    .hasPendingRecovery());
        }
    }

    @Test
    void replacingDestinationDuringSimulationCannotInsertIntoDetachedInventory() throws Exception {
        Fixture fixture = new Fixture();
        CountingInventory source = source(fixture, 1), replacement = new CountingInventory(1);
        source.setInventorySlotContents(0, stack(TRANSFER_ITEM, 9, 0, null));
        InventoryTile detached = new InventoryTile(1) {

            @Override
            public boolean isItemValidForSlot(int slot, ItemStack stack) {
                fixture.world.tiles.put(DST, replacement);
                return true;
            }
        };
        fixture.world.tiles.put(DST, detached);
        ProgramContext context = context(fixture);
        assertEquals(
            0,
            context.transferItems(SOURCE, DEST, ALL, 5)
                .getMoved());
        assertEquals(0, source.extractions);
        assertEquals(9, count(source));
        assertEquals(0, count(detached));
        assertEquals(0, count(replacement));
    }

    @Test
    void budgetFailureIsSharedAndOccursBeforeExtractionWithoutLosingOwnedStock() throws Exception {
        for (int exhausted = 0; exhausted < 3; exhausted++) {
            Fixture fixture = new Fixture();
            CountingInventory source = source(fixture, 1);
            source.setInventorySlotContents(0, stack(TRANSFER_ITEM, 9, 0, null));
            ProgramContext context = context(fixture);
            QueryBudget budget = context.getInvocationQueryBudget();
            if (exhausted == 0) budget.chargeAllocation(QueryBudget.MAX_ALLOCATIONS);
            if (exhausted == 1) budget.chargeProviders(QueryBudget.MAX_PROVIDER_CALLS);
            if (exhausted == 2) budget.chargeSlots(QueryBudget.MAX_SLOT_WORK);
            assertThrows(IllegalArgumentException.class, () -> context.transferItems(SOURCE, DEST, ALL, 5));
            assertEquals(0, source.extractions);
            assertEquals(9, count(source));
            assertEquals(0, count(fixture.dest));
            assertFalse(
                context.getManager()
                    .hasPendingRecovery());
        }
    }

    @Test
    void insertionFailureAfterMutationIsRecordedUncertainAndNeverAutomaticallyRetried() throws Exception {
        Fixture fixture = new Fixture();
        fixture.source.setInventorySlotContents(0, stack(TRANSFER_ITEM, 1, 7, tag("owned")));
        int[] insertionCalls = { 0 };
        InventoryTile destination = new InventoryTile(1) {

            @Override
            public void setInventorySlotContents(int slot, ItemStack stack) {
                insertionCalls[0]++;
                super.setInventorySlotContents(slot, stack);
                throw new IllegalStateException("transfer fixture after insertion mutation");
            }
        };
        fixture.world.tiles.put(DST, destination);
        ProgramContext context = context(fixture);
        assertThrows(RuntimeException.class, () -> context.transferItems(SOURCE, DEST, ALL, 1));
        assertEquals(0, count(fixture.source));
        assertEquals(1, count(destination));
        TileEntityManager manager = context.getManager();
        assertTrue(manager.hasPendingRecovery());
        NBTTagCompound record = manager.getDisk()
            .getTagCompound()
            .getCompoundTag("sfm:recovery_item");
        assertEquals("uncertain", record.getString("state"));
        assertEquals(
            1,
            record.getCompoundTag("stack")
                .getByte("Count"));
        assertEquals(
            7,
            record.getCompoundTag("stack")
                .getShort("Damage"));
        assertEquals(
            tag("owned"),
            record.getCompoundTag("stack")
                .getCompoundTag("tag"));
        manager.updateEntity();
        assertEquals(1, insertionCalls[0]);
        assertEquals(1, count(destination));
    }

    @Test
    void destinationSlotsInTheSelectedSourceViewAreExcludedEvenWhenInitiallyEmpty() throws Exception {
        Fixture fixture = new Fixture();
        CountingInventory source = source(fixture, 1);
        source.setInventorySlotContents(0, stack(TRANSFER_ITEM, 9, 0, null));
        // Empty destination slots are nevertheless part of the selected source access view.
        fixture.labels.addPosition("source", DST);
        ProgramContext context = context(fixture);
        assertResult(context.transferItems(SOURCE, DEST, ALL, 20), 20, 0, "NO_CAPACITY");
        assertEquals(9, count(source));
        assertEquals(0, count(fixture.dest));
        assertEquals(0, source.extractions);
        assertFalse(
            context.getManager()
                .hasPendingRecovery());
    }

    @Test
    void overlappingLabelsCannotRefeedMovedStockButDisjointDestinationSlotsStillWork() throws Exception {
        Fixture fixture = new Fixture();
        CountingInventory source = source(fixture, 1);
        source.setInventorySlotContents(0, stack(TRANSFER_ITEM, 9, 0, null));
        fixture.labels.addPosition("source", DST);
        InventoryTile disjoint = new InventoryTile(1);
        fixture.add("dest", EXTRA, disjoint);
        ProgramContext context = context(fixture);
        assertResult(context.transferItems(SOURCE, DEST, ALL, 20), 20, 9, "PARTIAL");
        assertEquals(0, count(source));
        assertEquals(0, count(fixture.dest), "The overlapping empty source view cannot receive onward-fed stock");
        assertEquals(9, count(disjoint));
        assertEquals(1, source.extractions);
        assertFalse(
            context.getManager()
                .hasPendingRecovery());
    }

    private static ProgramContext context(Fixture fixture) throws Exception {
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        return new ProgramContext(
            fixture.world,
            manager,
            new CableNetwork(fixture.world, MANAGER),
            fixture.labels,
            20,
            false);
    }

    private static CountingInventory source(Fixture fixture, int size) {
        CountingInventory source = new CountingInventory(size);
        fixture.world.tiles.put(SRC, source);
        return source;
    }

    private static ItemStack stack(Item item, int amount, int metadata, NBTTagCompound nbt) {
        ItemStack stack = new ItemStack(item, amount, metadata);
        stack.setTagCompound(nbt);
        return stack;
    }

    private static NBTTagCompound tag(String name) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("name", name);
        return tag;
    }

    private static DataValue key(int metadata, NBTTagCompound nbt) {
        return DataValue.itemIdentity(ItemIdentity.of("sfm_transfer_test:item", metadata, nbt, new QueryBudget()));
    }

    private static void assertResult(ItemTransferResult result, long requested, long moved, String status) {
        assertEquals(requested, result.getRequested());
        assertEquals(moved, result.getMoved());
        assertEquals(requested - moved, result.getRemaining());
        assertEquals(
            status,
            result.getStatus()
                .name());
    }

    private static CountingChest chest(Fixture fixture, BlockPos pos, int amount) {
        CountingChest chest = new CountingChest();
        chest.setInventorySlotContents(0, stack(TRANSFER_ITEM, amount, 0, null));
        chest.xCoord = pos.x;
        chest.yCoord = pos.y;
        chest.zCoord = pos.z;
        chest.setWorldObj(fixture.world);
        fixture.world.tiles.put(pos, chest);
        return chest;
    }

    private static class CountingInventory extends InventoryTile {

        int reads, extractions;

        CountingInventory(int size) {
            super(size);
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            reads++;
            return super.getStackInSlot(slot);
        }

        @Override
        public ItemStack decrStackSize(int slot, int amount) {
            extractions++;
            return super.decrStackSize(slot, amount);
        }
    }

    private static final class SidedInventory extends CountingInventory implements ISidedInventory {

        final int[][] faces = new int[6][];
        int extractSide = -1, insertSide = -1, extractChecks, insertChecks;

        SidedInventory(int size) {
            super(size);
        }

        @Override
        public int[] getAccessibleSlotsFromSide(int side) {
            return faces[side];
        }

        @Override
        public boolean canExtractItem(int slot, ItemStack stack, int side) {
            extractChecks++;
            return side == extractSide;
        }

        @Override
        public boolean canInsertItem(int slot, ItemStack stack, int side) {
            insertChecks++;
            return side == insertSide;
        }
    }

    private static final class CountingChest extends TileEntityChest {

        int extractions;

        @Override
        public ItemStack decrStackSize(int slot, int amount) {
            extractions++;
            return super.decrStackSize(slot, amount);
        }

        @Override
        public void checkForAdjacentChests() {
            throw new AssertionError("Value transfer must resolve loaded chest pairs safely");
        }
    }
}
