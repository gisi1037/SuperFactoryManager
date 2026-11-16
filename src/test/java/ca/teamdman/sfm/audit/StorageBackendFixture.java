package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.FluidTransferService;
import ca.teamdman.sfm.common.program.ItemQueryService;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.recovery.ItemRecovery;
import ca.teamdman.sfm.common.storage.StorageBackend;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.tileentity.TileEntityModAdapter;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ItemContents;
import ca.teamdman.sfml.data.ItemTransferResult;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;

/** Tests the actual transfer guards/journals with a bounded, native-count backend in an isolated Forge runtime. */
public final class StorageBackendFixture {

    public static void longCountsAndAliasesAreQueriedOnceAndQueriesSeeEarlierTransfers() throws Exception {
        Setup setup = new Setup();
        setup.backend.items = 3000000000L;
        setup.fixture.add("source", EXTRA, new Adapter(setup.backend));
        CableNetwork.invalidate(setup.fixture.world);
        ItemQueryService queries = new ItemQueryService(setup.context);
        LabelReference source = LabelReference.of("source");
        assertEquals(3000000000L, queries.count(source, ResourceSelector.item("*"), new QueryBudget()));
        QueryBudget budget = new QueryBudget();
        ItemContents contents = queries.contents(source, ResourceSelector.item("*"), budget);
        assertEquals(
            1,
            contents.groups(budget)
                .size());
        assertEquals(
            3000000000L,
            contents.groups(budget)
                .get(0)
                .getTotal());
        parse("every 20 ticks {transfer(label(\"source\"), label(\"dest\"), 10)}").tick(setup.context);
        assertEquals(10, count(setup.fixture.dest));
        assertEquals(2999999990L, queries.count(source, ResourceSelector.item("*"), new QueryBudget()));
    }

    public static void nativeItemInsertionUsesAcceptedAmountsAndNeverFabricatesInventoryWrites() throws Exception {
        Setup setup = new Setup();
        setup.fixture.source.setInventorySlotContents(0, new ItemStack(Items.iron_ingot, 40));
        setup.fixture.world.tiles.put(SRC, setup.fixture.source);
        setup.fixture.world.tiles.put(DST, new Adapter(setup.backend));
        setup.backend.capacity = 205;
        parse("every 20 ticks {transfer(label(\"source\"), label(\"dest\"), 20) into result}").tick(setup.context);
        assertEquals(205, setup.backend.items);
        assertEquals(35, count(setup.fixture.source));
        assertEquals(1, setup.backend.itemInsertions);
        assertEquals(0, setup.backend.itemScans, "Insertion must not enumerate the ME network");
    }

    public static void transferExclusionSkipsAdapterStockAndCountsOnlyEligibleItems() throws Exception {
        Setup setup = new Setup();
        InventoryTile mixed = new InventoryTile(2);
        mixed.setInventorySlotContents(0, new ItemStack(Items.iron_ingot, 4, 3));
        mixed.setInventorySlotContents(1, new ItemStack(Items.gold_ingot, 8));
        setup.fixture.add("source", EXTRA, mixed);
        CableNetwork.invalidate(setup.fixture.world);

        parse("""
            void except(integer value) { print(value) }
            every 20 ticks {
              transfer(label("source"), label("dest"), 3) except item("minecraft:iron_ingot")
              transfer(label("source"), label("dest"), 0)
              except(3)
            }
            """).tick(setup.context);
        assertEquals(3, count(setup.fixture.dest));

        ItemTransferResult result = setup.context.transferItems(
            LabelReference.of("source"),
            LabelReference.of("dest"),
            DataValue.selector(ResourceSelector.item("*")),
            ResourceSelector.item("minecraft:iron_ingot"),
            0,
            true);
        assertEquals(5, result.getRequested());
        assertEquals(5, result.getMoved());
        assertEquals(ItemTransferResult.Status.COMPLETE, result.getStatus());
        assertEquals(4, count(mixed));
        assertEquals(8, count(setup.fixture.dest));
        assertSame(
            Items.gold_ingot,
            setup.fixture.dest.getStackInSlot(0)
                .getItem());
        assertEquals(200, setup.backend.items);
        assertEquals(0, setup.backend.itemExtractions);
    }

    public static void emptyLiveNetworkIsCompleteAndIsNotUnavailable() throws Exception {
        Setup setup = new Setup();
        setup.backend.items = 0;
        setup.backend.water = 0;
        ItemQueryService queries = new ItemQueryService(setup.context);
        assertTrue(queries.empty(LabelReference.of("source"), ResourceSelector.item("*"), new QueryBudget()));
        ItemTransferResult result = setup.context.transferItems(
            LabelReference.of("source"),
            LabelReference.of("dest"),
            DataValue.selector(ResourceSelector.item("*")));
        assertEquals(ItemTransferResult.Status.COMPLETE, result.getStatus());
        assertEquals(0, result.getMoved());
        assertTrue(
            new FluidTransferService(setup.context)
                .empty(LabelReference.of("source"), ResourceSelector.fluid("*"), new QueryBudget()));
    }

    public static void invertedItemSelectorFiltersQueriesAndTransfers() throws Exception {
        Setup setup = new Setup();
        InventoryTile mixed = new InventoryTile(2);
        mixed.setInventorySlotContents(0, new ItemStack(Items.iron_ingot, 4, 3));
        mixed.setInventorySlotContents(1, new ItemStack(Items.gold_ingot, 8));
        setup.fixture.add("source", EXTRA, mixed);
        CableNetwork.invalidate(setup.fixture.world);

        parse("""
            every 20 ticks {
              let source = label("source")
              let filter = !item("minecraft:iron_ingot")
              if (source.count(filter) == 8 and source.count(!item("*")) == 0) {
                transfer(source, label("dest"), filter)
              }
            }
            """).tick(setup.context);
        assertEquals(8, count(setup.fixture.dest));
        assertSame(
            Items.gold_ingot,
            setup.fixture.dest.getStackInSlot(0)
                .getItem());
        assertEquals(4, count(mixed));
        assertEquals(200, setup.backend.items);
        assertEquals(0, setup.backend.itemExtractions);
    }

    public static void invertedFluidSelectorFiltersQueriesAndTransfers() throws Exception {
        Setup setup = new Setup();
        Tank target = new Tank();
        setup.fixture.world.tiles.put(DST, target);
        parse("""
            every 20 ticks {
              let source = label("source")
              let dest = label("dest")
              if (source.fcount(!fluid("water")) == 0 and source.fcount(!fluid("lava")) > 1000) {
                transferf(source, dest, !fluid("water"), 1000)
                transferf(source, dest, !fluid("lava"), 1000)
              }
            }
            """).tick(setup.context);
        assertEquals(1000, target.tank.getFluidAmount());
        assertEquals(2999999000L, setup.backend.water);
        assertEquals(1, setup.backend.fluidExtractions);
        assertFalse(setup.manager.hasPendingRecovery());
    }

    public static void bulkNativeItemsUseBoundedStackPayloadsAndStillMoveTheRequestedTotal() throws Exception {
        Setup setup = new Setup();
        Backend destination = new Backend();
        setup.fixture.world.tiles.put(DST, new Adapter(destination));
        parse("every 20 ticks {transfer(label(\"source\"), label(\"dest\"), 130)}").tick(setup.context);
        assertEquals(70, setup.backend.items);
        assertEquals(330, destination.items);
        assertEquals(3, setup.backend.itemExtractions);
        assertFalse(setup.manager.hasPendingRecovery());
    }

    public static void nativeBulkFillsMultipleBusSlotsWithOneExtraction() throws Exception {
        Setup setup = new Setup();
        setup.backend.bulk = true;
        setup.backend.items = 10240;
        SidedBus bus = new SidedBus(16);
        setup.fixture.world.tiles.put(DST, bus);
        parse("""
            every 20 ticks { transfer(label("source"), label("dest").side("north"), 960) }
            """).tick(setup.context);
        assertEquals(960, count(bus));
        assertEquals(9280, setup.backend.items);
        assertEquals(1, setup.backend.itemExtractions);
        for (int i = 0; i < 15; i++) assertEquals(64, bus.getStackInSlot(i).stackSize);
        assertFalse(setup.manager.hasPendingRecovery());

        Setup baseline = new Setup();
        baseline.backend.items = 10240;
        SidedBus legacyBus = new SidedBus(16);
        baseline.fixture.world.tiles.put(DST, legacyBus);
        parse("""
            every 20 ticks { transfer(label("source"), label("dest").side("north"), 960) }
            """).tick(baseline.context);
        assertEquals(960, count(legacyBus));
        assertEquals(15, baseline.backend.itemExtractions);
        int bulkWork = setup.context.getInvocationQueryBudget()
            .getSlotWork();
        int baselineWork = baseline.context.getInvocationQueryBudget()
            .getSlotWork();
        assertTrue(bulkWork < baselineWork / 2, "Bulk work " + bulkWork + " vs stack-by-stack " + baselineWork);
        System.out.println("960 item slot work: bulk=" + bulkWork + ", stack-by-stack=" + baselineWork);
    }

    public static void nativeBulkUsesBoundedWorkOnAWideSidedBus() throws Exception {
        Setup setup = new Setup();
        setup.backend.bulk = true;
        setup.backend.items = 8192;
        SidedBus bus = new SidedBus(128);
        setup.fixture.world.tiles.put(DST, bus);
        parse("""
            every 20 ticks { transfer(label("source"), label("dest").side("north")) }
            """).tick(setup.context);
        assertEquals(8192, count(bus));
        assertEquals(0, setup.backend.items);
        assertEquals(8, setup.backend.itemExtractions);
        assertFalse(setup.manager.hasPendingRecovery());
        QueryBudget budget = setup.context.getInvocationQueryBudget();
        System.out.println(
            "8192 item wide bus: slots=" + budget.getSlotWork()
                + ", positions="
                + budget.getPositions()
                + ", providers="
                + budget.getProviderCalls()
                + ", allocations="
                + budget.getAllocations());
    }

    public static void nativeBulkRechecksSlotThatUnlocksAfterEarlierPlacement() throws Exception {
        Setup setup = new Setup();
        setup.backend.bulk = true;
        SidedBus bus = new SidedBus(2);
        bus.sequential = true;
        setup.fixture.world.tiles.put(DST, bus);
        parse("every 20 ticks { transfer(label(\"source\"), label(\"dest\"), 128) }").tick(setup.context);
        assertEquals(128, count(bus));
        assertEquals(72, setup.backend.items);
        assertEquals(2, setup.backend.itemExtractions);

        Setup expanding = new Setup();
        expanding.backend.bulk = true;
        InventoryTile variableLimit = new InventoryTile(2) {

            @Override
            public int getInventoryStackLimit() {
                return getStackInSlot(0) == null ? 1 : 64;
            }
        };
        expanding.fixture.world.tiles.put(DST, variableLimit);
        parse("every 20 ticks { transfer(label(\"source\"), label(\"dest\"), 128) }").tick(expanding.context);
        assertEquals(64, variableLimit.getStackInSlot(0).stackSize);
        assertEquals(64, variableLimit.getStackInSlot(1).stackSize);
        assertEquals(72, expanding.backend.items);
    }

    public static void bulkNativeEndpointsMoveOneExactIdentityWithoutStackSizedCallbacks() throws Exception {
        Setup setup = new Setup();
        setup.backend.items = 10240;
        setup.backend.bulk = true;
        Backend destination = new Backend();
        destination.bulk = true;
        destination.items = 0;
        destination.capacity = 9800;
        setup.fixture.world.tiles.put(DST, new Adapter(destination));
        ItemTransferResult result = setup.context.transferItems(
            LabelReference.of("source"),
            LabelReference.of("dest"),
            DataValue.selector(ResourceSelector.item("minecraft:iron_ingot")));
        assertEquals(10240, result.getRequested());
        assertEquals(9800, result.getMoved());
        assertEquals(ItemTransferResult.Status.PARTIAL, result.getStatus());
        assertEquals(440, setup.backend.items);
        assertEquals(9800, destination.items);
        assertEquals(1, setup.backend.itemExtractions);
        assertEquals(1, destination.itemInsertions);
        assertEquals(0, destination.itemScans);
        assertFalse(setup.manager.hasPendingRecovery());
    }

    public static void bulkFailureRetainsUnattemptedItemsAfterOperatorReconciliation() throws Exception {
        Setup setup = new Setup();
        setup.backend.bulk = true;
        setup.backend.items = 1024;
        InventoryTile destination = new InventoryTile(16) {

            @Override
            public void setInventorySlotContents(int slot, ItemStack stack) {
                super.setInventorySlotContents(slot, stack);
                throw new IllegalStateException("failure after writing the first slot");
            }
        };
        setup.fixture.world.tiles.put(DST, destination);
        assertThrows(
            RuntimeException.class,
            () -> parse("every 20 ticks { transfer(label(\"source\"), label(\"dest\"), 512) }").tick(setup.context));
        var record = ItemRecovery.inspect(setup.manager);
        assertEquals("uncertain", record.getString("state"));
        assertEquals(512, ItemRecovery.amount(record));
        assertEquals(64, record.getInteger("attemptAmount"));
        assertEquals(
            1,
            record.getCompoundTag("stack")
                .getByte("Count"));
        assertEquals(64, count(destination));
        assertEquals(512, setup.backend.items);

        ItemRecovery.resolvePresent(setup.manager, record.getString("recoveryId"), "operator");
        var pending = ItemRecovery.inspect(setup.manager);
        assertEquals("pending", pending.getString("state"));
        assertEquals(448, ItemRecovery.amount(pending));
        InventoryTile recovery = new InventoryTile(7) {

            @Override
            public void setInventorySlotContents(int slot, ItemStack stack) {
                assertThrows(
                    IllegalStateException.class,
                    () -> ItemRecovery.resolvePresent(setup.manager, record.getString("recoveryId"), "nested"));
                super.setInventorySlotContents(slot, stack);
            }
        };
        setup.fixture.world.tiles.put(DST, recovery);
        assertEquals(
            448,
            ItemRecovery.claim(setup.manager, record.getString("recoveryId"), ForgeDirection.WEST, "operator"));
        assertEquals(448, count(recovery));
        assertFalse(setup.manager.hasPendingRecovery());
    }

    public static void nativeBulkReservesBeforeExtractionAndFencesReplacedDisks() throws Exception {
        Setup limited = new Setup();
        limited.backend.bulk = true;
        limited.backend.items = 512;
        limited.fixture.world.tiles.put(DST, new SidedBus(8));
        QueryBudget budget = limited.context.getInvocationQueryBudget();
        budget.chargePositions(budget.getPositionLimit() - 60);
        assertThrows(
            RuntimeException.class,
            () -> parse("every 20 ticks { transfer(label(\"source\"), label(\"dest\"), 512) }").tick(limited.context));
        assertEquals(512, limited.backend.items);
        assertEquals(0, limited.backend.itemExtractions);
        assertFalse(limited.manager.hasPendingRecovery());

        Setup swapped = new Setup();
        swapped.backend.bulk = true;
        swapped.backend.items = 512;
        swapped.fixture.world.tiles.put(DST, new SidedBus(8));
        ItemStack original = swapped.manager.getDisk(), replacement = original.copy();
        swapped.backend.onActualExtract = () -> {
            assertThrows(IllegalStateException.class, () -> ItemRecovery.inspect(swapped.manager));
            swapped.manager.setInventorySlotContents(0, replacement);
        };
        assertThrows(
            RuntimeException.class,
            () -> parse("every 20 ticks { transfer(label(\"source\"), label(\"dest\"), 512) }").tick(swapped.context));
        assertEquals(0, swapped.backend.items);
        assertFalse(swapped.manager.hasPendingRecovery());
        var record = original.getTagCompound()
            .getCompoundTag(ItemRecovery.KEY);
        assertEquals("uncertain", record.getString("state"));
        assertEquals(512, ItemRecovery.amount(record));
        assertEquals(512, record.getInteger("attemptAmount"));
    }

    public static void sameBackingNetworkCannotFeedItselfThroughDifferentAdapters() throws Exception {
        Setup setup = new Setup();
        setup.fixture.world.tiles.put(DST, new Adapter(setup.backend));
        parse("every 20 ticks {transfer(label(\"source\"), label(\"dest\"), 10)}").tick(setup.context);
        assertEquals(200, setup.backend.items);
        assertEquals(0, setup.backend.itemExtractions);
        assertEquals(0, setup.backend.itemInsertions);
        parse("every 20 ticks {transferf(label(\"source\"), label(\"dest\"), 1000)}").tick(setup.context);
        assertEquals(3000000000L, setup.backend.water);
        assertEquals(0, setup.backend.fluidExtractions);
    }

    public static void unavailableAdapterIsNeverReportedEmptyAndSlotIndexesAreRefused() throws Exception {
        Setup setup = new Setup();
        setup.backend.active = false;
        ItemQueryService queries = new ItemQueryService(setup.context);
        LabelReference label = LabelReference.of("source");
        assertFalse(queries.available(label, ResourceSelector.item("*"), new QueryBudget()));
        assertFalse(queries.empty(label, ResourceSelector.item("*"), new QueryBudget()));
        assertFalse(
            new FluidTransferService(setup.context).empty(label, ResourceSelector.fluid("*"), new QueryBudget()));
        setup.backend.active = true;
        assertThrows(
            IllegalArgumentException.class,
            () -> queries.count(label.slots(0, 0), ResourceSelector.item("*"), new QueryBudget()));
    }

    public static void callbackReplacementRefusesBeforeNativeExtraction() throws Exception {
        Setup setup = new Setup();
        Backend destination = new Backend();
        destination.onSimulateInsert = () -> setup.fixture.world.tiles.remove(SRC);
        setup.fixture.world.tiles.put(DST, new Adapter(destination));
        parse("every 20 ticks {transfer(label(\"source\"), label(\"dest\"), 10)}").tick(setup.context);
        assertEquals(0, setup.backend.itemExtractions);
        assertEquals(200, setup.backend.items);
        assertEquals(200, destination.items);
    }

    public static void nativeItemRollbackCanBePartialWithoutDuplicatingTheReturnedPortion() throws Exception {
        Setup setup = new Setup();
        Backend destination = new Backend();
        destination.refuseActualInsert = true;
        setup.backend.capacity = 195; // Only five of ten extracted items can return.
        setup.fixture.world.tiles.put(DST, new Adapter(destination));
        assertThrows(
            RuntimeException.class,
            () -> parse("every 20 ticks {transfer(label(\"source\"), label(\"dest\"), 10)}").tick(setup.context));
        assertEquals(195, setup.backend.items);
        assertEquals(200, destination.items);
        assertTrue(setup.manager.hasPendingRecovery());
        assertEquals(
            5,
            setup.manager.getDisk()
                .getTagCompound()
                .getCompoundTag("sfm:recovery_item")
                .getCompoundTag("stack")
                .getByte("Count"));
    }

    public static void lostNativeAccessAfterExtractionPreservesKnownOwnedItems() throws Exception {
        Setup setup = new Setup();
        setup.backend.onActualExtract = () -> setup.backend.active = false;
        assertThrows(
            RuntimeException.class,
            () -> parse("every 20 ticks {transfer(label(\"source\"), label(\"dest\"), 10)}").tick(setup.context));
        assertEquals(190, setup.backend.items);
        assertEquals(0, count(setup.fixture.dest));
        assertTrue(setup.manager.hasPendingRecovery());
        net.minecraft.nbt.NBTTagCompound recovery = setup.manager.getDisk()
            .getTagCompound()
            .getCompoundTag("sfm:recovery_item");
        assertEquals("pending", recovery.getString("state"));
        assertEquals(
            10,
            recovery.getCompoundTag("stack")
                .getByte("Count"));
    }

    public static void nativeFluidQueriesKeepLongCountsAndTransfersUseTheExistingJournal() throws Exception {
        Setup setup = new Setup();
        FluidTransferService queries = new FluidTransferService(setup.context);
        assertEquals(
            3000000000L,
            queries.count(LabelReference.of("source"), ResourceSelector.fluid("*"), new QueryBudget()));
        Tank target = new Tank();
        setup.fixture.world.tiles.put(DST, target);
        parse("every 20 ticks {transferf(label(\"source\"), label(\"dest\"), fluid(\"water\"), 1000)}")
            .tick(setup.context);
        assertEquals(1000, target.tank.getFluidAmount());
        assertEquals(2999999000L, setup.backend.water);
        assertFalse(setup.manager.hasPendingRecovery());
        assertEquals(1, setup.backend.fluidExtractions);
        assertEquals(
            2999999000L,
            new FluidTransferService(setup.context)
                .count(LabelReference.of("source"), ResourceSelector.fluid("*"), new QueryBudget()));
    }

    public static void nativeFluidQueriesAvoidRedundantNetworkScans() throws Exception {
        Setup setup = new Setup();
        setup.fixture.add("source", EXTRA, new Adapter(setup.backend));
        CableNetwork.invalidate(setup.fixture.world);
        FluidTransferService queries = new FluidTransferService(setup.context);
        LabelReference source = LabelReference.of("source");
        ResourceSelector all = ResourceSelector.fluid("*");

        assertTrue(queries.available(source, all, new QueryBudget()));
        assertEquals(0, setup.backend.fluidScans);
        assertEquals(3000000000L, queries.count(source, all, new QueryBudget()));
        assertEquals(1, setup.backend.fluidScans);
        setup.backend.water = 500;
        assertNotNull(queries.contents(source, all, new QueryBudget()));
        assertEquals(2, setup.backend.fluidScans);
        setup.backend.water = 0;
        assertTrue(queries.empty(source, all, new QueryBudget()));
        assertEquals(3, setup.backend.fluidScans);
        setup.backend.active = false;
        assertFalse(queries.available(source, all, new QueryBudget()));
        assertEquals(3, setup.backend.fluidScans);
    }

    public static void nativeFluidInsertionAndShortDestinationConserveEveryMillibucket() throws Exception {
        Setup setup = new Setup();
        Tank tank = new Tank();
        tank.tank.setFluid(new FluidStack(FluidRegistry.WATER, 2000));
        setup.fixture.world.tiles.put(SRC, tank);
        setup.fixture.world.tiles.put(DST, new Adapter(setup.backend));
        setup.backend.fluidCapacity = setup.backend.water + 500;
        parse("every 20 ticks {transferf(label(\"source\"), label(\"dest\"), 1000)}").tick(setup.context);
        assertEquals(1500, tank.tank.getFluidAmount());
        assertEquals(3000000500L, setup.backend.water);
        assertFalse(setup.manager.hasPendingRecovery());
        assertEquals(0, setup.backend.fluidScans, "Fluid insertion must not enumerate the ME network");
    }

    public static void nativeFluidTransferSkipsNoProgressFaceRetries() throws Exception {
        Setup setup = new Setup();
        Tank full = new Tank();
        full.tank.setFluid(new FluidStack(FluidRegistry.WATER, full.tank.getCapacity()));
        setup.fixture.world.tiles.put(DST, full);
        parse("every 20 ticks { transferf(label(\"source\"), label(\"dest\")) }").tick(setup.context);
        assertEquals(6, setup.backend.fluidDrainSimulations, "Check each real destination face once");
        assertEquals(0, setup.backend.fluidExtractions);
        assertEquals(3000000000L, setup.backend.water);
        assertFalse(setup.manager.hasPendingRecovery());

        Setup bulk = new Setup();
        Backend destination = new Backend();
        destination.water = 0;
        bulk.fixture.world.tiles.put(DST, new Adapter(destination));
        parse("every 20 ticks { transferf(label(\"source\"), label(\"dest\")) }").tick(bulk.context);
        assertEquals(2, bulk.backend.fluidExtractions, "Keep progressing across the int limit");
        assertEquals(0, bulk.backend.water);
        assertEquals(3000000000L, destination.water);
        assertFalse(bulk.manager.hasPendingRecovery());
    }

    private static final class Setup {

        final Fixture fixture = new Fixture();
        final Backend backend = new Backend();
        final TileEntityManager manager;
        final ProgramContext context;

        Setup() throws Exception {
            for (int x = -2; x <= 4; x++) for (int y = 62; y <= 66; y++)
                for (int z = -2; z <= 2; z++) fixture.world.blocks.put(new BlockPos(x, y, z), Blocks.air);
            fixture.world.tiles.put(SRC, new Adapter(backend));
            manager = RecoveryFixtures.manager(fixture);
            context = new ProgramContext(
                fixture.world,
                manager,
                new CableNetwork(fixture.world, MANAGER),
                fixture.labels,
                20,
                false);
        }
    }

    private static final class Adapter extends TileEntityModAdapter {

        final Backend backend;

        Adapter(Backend backend) {
            this.backend = backend;
        }

        @Override
        public StorageBackend open(QueryBudget budget) {
            return backend.active ? backend : null;
        }
    }

    private static final class Backend implements StorageBackend {

        long items = 200, water = 3000000000L, capacity = Long.MAX_VALUE, fluidCapacity = Long.MAX_VALUE;
        boolean active = true, refuseActualInsert, bulk;
        int itemExtractions, itemInsertions, fluidExtractions, fluidDrainSimulations, itemScans, fluidScans;
        Runnable onSimulateInsert, onActualExtract;

        @Override
        public Object identity() {
            return this;
        }

        @Override
        public boolean current(QueryBudget budget) {
            return active;
        }

        @Override
        public boolean supportsBulkItems() {
            return bulk;
        }

        @Override
        public List<StoredItem> items(QueryBudget budget) {
            itemScans++;
            List<StoredItem> rows = new ArrayList<>();
            if (items > 0) rows.add(new StoredItem(new ItemStack(Items.iron_ingot), items));
            return rows;
        }

        @Override
        public List<StoredFluid> fluids(QueryBudget budget) {
            fluidScans++;
            List<StoredFluid> rows = new ArrayList<>();
            if (water > 0) rows.add(new StoredFluid(new FluidStack(FluidRegistry.WATER, 1), water));
            return rows;
        }

        @Override
        public ItemStack extract(ItemStack requested, boolean simulate, QueryBudget budget) {
            int count = requested.getItem() == Items.iron_ingot ? (int) Math.min(items, requested.stackSize) : 0;
            if (!simulate) {
                items -= count;
                itemExtractions++;
                if (onActualExtract != null) onActualExtract.run();
            }
            return count == 0 ? null : new ItemStack(Items.iron_ingot, count);
        }

        @Override
        public int insert(ItemStack offered, boolean simulate, QueryBudget budget) {
            if (simulate && onSimulateInsert != null) onSimulateInsert.run();
            int count = offered.getItem() == Items.iron_ingot
                ? (int) Math.min(offered.stackSize, Math.max(0, capacity - items))
                : 0;
            if (!simulate) {
                if (refuseActualInsert) return 0;
                items += count;
                itemInsertions++;
            }
            return count;
        }

        @Override
        public FluidStack drain(FluidStack requested, boolean simulate, QueryBudget budget) {
            int amount = (int) Math.min(requested.amount, water);
            if (simulate) fluidDrainSimulations++;
            else {
                water -= amount;
                fluidExtractions++;
            }
            return amount == 0 ? null : new FluidStack(FluidRegistry.WATER, amount);
        }

        @Override
        public int fill(FluidStack offered, boolean simulate, QueryBudget budget) {
            int amount = (int) Math.min(offered.amount, Math.max(0, fluidCapacity - water));
            if (!simulate) water += amount;
            return amount;
        }
    }

    private static final class SidedBus extends InventoryTile implements ISidedInventory {

        private final int[] slots;
        boolean sequential;

        SidedBus(int size) {
            super(size);
            slots = new int[size];
            for (int i = 0; i < size; i++) slots[i] = i;
        }

        @Override
        public int[] getAccessibleSlotsFromSide(int side) {
            return side == ForgeDirection.NORTH.ordinal() ? slots : new int[0];
        }

        @Override
        public boolean canInsertItem(int slot, ItemStack stack, int side) {
            return side == ForgeDirection.NORTH.ordinal()
                && (!sequential || slot == 0 || getStackInSlot(slot - 1) != null);
        }

        @Override
        public boolean canExtractItem(int slot, ItemStack stack, int side) {
            return false;
        }
    }

    private static final class Tank extends TileEntity implements IFluidHandler {

        final FluidTank tank = new FluidTank(10000);

        @Override
        public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
            return tank.fill(fluid, actual);
        }

        @Override
        public FluidStack drain(ForgeDirection side, FluidStack fluid, boolean actual) {
            return tank.getFluid() != null && tank.getFluid()
                .isFluidEqual(fluid) ? tank.drain(fluid.amount, actual) : null;
        }

        @Override
        public FluidStack drain(ForgeDirection side, int amount, boolean actual) {
            return tank.drain(amount, actual);
        }

        @Override
        public boolean canFill(ForgeDirection side, net.minecraftforge.fluids.Fluid fluid) {
            return true;
        }

        @Override
        public boolean canDrain(ForgeDirection side, net.minecraftforge.fluids.Fluid fluid) {
            return true;
        }

        @Override
        public FluidTankInfo[] getTankInfo(ForgeDirection side) {
            return new FluidTankInfo[] { tank.getInfo() };
        }
    }
}
