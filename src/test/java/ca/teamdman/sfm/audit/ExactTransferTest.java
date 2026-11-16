package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.RegistrySimple;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ItemTransferResult;
import ca.teamdman.sfml.data.ItemTransferResult.Status;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.ResourceSelector;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class ExactTransferTest {

    private static final Item ITEM = new Item();
    private static final DataValue ALL_ITEMS = DataValue.selector(ResourceSelector.item("*"));
    private static final DataValue ALL_FLUIDS = DataValue.selector(ResourceSelector.fluid("*"));

    static {
        try {
            Field field = RegistrySimple.class.getDeclaredField("registryObjects");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> names = (Map<String, Object>) field.get(Item.itemRegistry);
            names.put("sfm_exact_transfer:item", ITEM);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    @Test
    void chestsCommitOneBatchAcrossSeveralSlots() throws Exception {
        Setup setup = new Setup();
        setup.source.setInventorySlotContents(0, new ItemStack(ITEM, 4));
        setup.source.setInventorySlotContents(1, new ItemStack(ITEM, 7));
        setup.destination.setInventorySlotContents(0, new ItemStack(ITEM, 60));
        parse("""
            every 20 ticks {
              transferexact(label("source"), label("dest"), 10) into result
            }
            """).tick(setup.context);
        assertEquals(1, count(setup.source));
        assertEquals(64, setup.destination.getStackInSlot(0).stackSize);
        assertEquals(6, setup.destination.getStackInSlot(1).stackSize);
    }

    @Test
    void ordinaryChestBatchFillsEarlierEmptySlotBeforeLaterMergeAndAllowsPartial() throws Exception {
        Setup setup = new Setup();
        setup.source.setInventorySlotContents(0, new ItemStack(ITEM, 20));
        setup.source.setInventorySlotContents(1, new ItemStack(ITEM, 10));
        setup.destination.setInventorySlotContents(1, new ItemStack(ITEM, 60));
        ItemTransferResult result = setup.context
            .transferItems(LabelReference.of("source"), LabelReference.of("dest"), ALL_ITEMS, 31);
        assertEquals(Status.PARTIAL, result.getStatus());
        assertEquals(30, result.getMoved());
        assertEquals(30, setup.destination.getStackInSlot(0).stackSize);
        assertEquals(60, setup.destination.getStackInSlot(1).stackSize);
        assertEquals(0, count(setup.source));
    }

    @Test
    void shortStockAndShortCapacityLeaveEveryStackInPlace() throws Exception {
        Setup setup = new Setup();
        ItemStack source = new ItemStack(ITEM, 9);
        ItemStack target = new ItemStack(ITEM, 60);
        setup.source.setInventorySlotContents(0, source);
        setup.destination.setInventorySlotContents(0, target);

        assertRefused(
            Status.NO_MATCH,
            setup.context.transferExact(LabelReference.of("source"), LabelReference.of("dest"), ALL_ITEMS, 10, false));
        assertRefused(
            Status.NO_CAPACITY,
            setup.context.transferExact(
                LabelReference.of("source"),
                LabelReference.of("dest")
                    .slots(0, 0),
                ALL_ITEMS,
                9,
                false));

        assertSame(source, setup.source.getStackInSlot(0));
        assertSame(target, setup.destination.getStackInSlot(0));
        assertEquals(9, source.stackSize);
        assertEquals(60, target.stackSize);
    }

    @Test
    void disjointSlotsOfTheSameChestWorkButSelfTransferIsRefused() throws Exception {
        Setup setup = new Setup();
        setup.source.setInventorySlotContents(0, new ItemStack(ITEM, 10));
        LabelReference source = LabelReference.of("source")
            .slots(0, 0);
        assertRefused(Status.NO_CAPACITY, setup.context.transferExact(source, source, ALL_ITEMS, 5, false));
        ItemTransferResult result = setup.context.transferExact(
            source,
            LabelReference.of("source")
                .slots(1, 1),
            ALL_ITEMS,
            5,
            false);
        assertEquals(Status.COMPLETE, result.getStatus());
        assertEquals(5, setup.source.getStackInSlot(0).stackSize);
        assertEquals(5, setup.source.getStackInSlot(1).stackSize);
    }

    @Test
    void unsupportedEndpointsAreRefusedBeforeMovingAnything() throws Exception {
        Setup setup = new Setup();
        setup.source.setInventorySlotContents(0, new ItemStack(ITEM, 9));
        setup.fixture.add("dest", EXTRA, setup.fixture.dest);
        assertRefused(
            Status.UNAVAILABLE,
            setup.context.transferExact(LabelReference.of("source"), LabelReference.of("dest"), ALL_ITEMS, 9, false));
        assertRefused(
            Status.UNAVAILABLE,
            setup.context.transferExact(LabelReference.of("source"), LabelReference.of("dest"), ALL_FLUIDS, 9, true));
        assertEquals(9, count(setup.source));
        assertEquals(0, count(setup.destination));
        assertEquals(0, count(setup.fixture.dest));
    }

    @Test
    void exactFormsRequireAnExplicitIntegerAmount() {
        for (String name : new String[] { "transferexact", "transferfexact" }) {
            assertFalse(
                new ProgramBuilder("every 20 ticks { " + name + "(label(\"a\"), label(\"b\")) }").build()
                    .isSuccess());
            assertFalse(
                new ProgramBuilder("every 20 ticks { " + name + "(label(\"a\"), label(\"b\"), \"10\") }").build()
                    .isSuccess());
        }
    }

    private static void assertRefused(Status status, ItemTransferResult result) {
        assertEquals(status, result.getStatus());
        assertEquals(0, result.getMoved());
    }

    private static final class Setup {

        final Fixture fixture = new Fixture();
        final TileEntityChest source = new TileEntityChest(), destination = new TileEntityChest();
        final ProgramContext context;

        Setup() throws Exception {
            for (int x = -2; x <= 4; x++) for (int y = 62; y <= 66; y++)
                for (int z = -2; z <= 2; z++) fixture.world.blocks.put(new BlockPos(x, y, z), Blocks.air);
            fixture.add("source", SRC, source);
            fixture.add("dest", DST, destination);
            context = new ProgramContext(
                fixture.world,
                RecoveryFixtures.manager(fixture),
                new CableNetwork(fixture.world, MANAGER),
                fixture.labels,
                20,
                false);
        }
    }
}
