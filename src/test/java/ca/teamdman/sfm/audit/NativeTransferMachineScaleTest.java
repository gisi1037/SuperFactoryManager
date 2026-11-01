package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistrySimple;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.block.BlockCable;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ItemTransferResult;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;

/** Bounded headless accounting probe; these inventory permissions do not emulate the GregTech implementation. */
class NativeTransferMachineScaleTest {

    private static final Item INPUT = new Item(), OUTPUT = new Item();
    private static final ResourceSelector ALL = ResourceSelector.item("*");

    static {
        try {
            Field field = RegistrySimple.class.getDeclaredField("registryObjects");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> names = (Map<String, Object>) field.get(Item.itemRegistry);
            names.put("sfm_machine_scale:input", INPUT);
            names.put("sfm_machine_scale:output", OUTPUT);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    @Test
    void chestToSelectedMachinePreservesStockWithAndWithoutPriorSelectionQueries() throws Exception {
        for (boolean priorQueries : new boolean[] { false, true }) {
            ScaleFixture fixture = new ScaleFixture();
            for (int i = 0; i < 6; i++) fixture.sourceChest.setInventorySlotContents(i, new ItemStack(INPUT, 1, i));
            int before = fixture.total();
            if (priorQueries) fixture.selectionQueries();
            ItemTransferResult result = fixture
                .transfer("chest-to-machine", LabelReference.literal("source"), LabelReference.literal("machine0"));
            assertEquals(6, result.getRequested());
            assertEquals(6, result.getMoved());
            assertEquals(
                "COMPLETE",
                result.getStatus()
                    .name());
            assertEquals(before, fixture.total());
            assertEquals(6, count(fixture.machines[0]));
            assertEquals(0, count(fixture.sourceChest));
            for (int i = 0; i < 6; i++) assertEquals(
                i,
                fixture.machines[0].getStackInSlot(i)
                    .getItemDamage());
            assertEquals(0, fixture.machines[0].getStackInSlot(14).stackSize);
        }
    }

    @Test
    void machineGroupToOccupiedChestPreservesStockWithAndWithoutPriorSelectionQueries() throws Exception {
        for (boolean priorQueries : new boolean[] { false, true }) {
            ScaleFixture fixture = new ScaleFixture();
            for (int i = 0; i < fixture.machines.length; i++) {
                fixture.machines[i].setInventorySlotContents(0, new ItemStack(INPUT, 1));
                if (i < 6) fixture.machines[i].setInventorySlotContents(15, new ItemStack(OUTPUT, 1));
            }
            for (int i = 0; i < 53; i++) fixture.chest.setInventorySlotContents(i, new ItemStack(INPUT, 1));
            int before = fixture.total();
            if (priorQueries) fixture.selectionQueries();
            ItemTransferResult result = fixture.transfer(
                "machines-to-occupied-chest",
                LabelReference.literal("machines"),
                LabelReference.literal("dest"));
            assertEquals(14, result.getRequested());
            assertEquals(6, result.getMoved());
            assertEquals(
                "PARTIAL",
                result.getStatus()
                    .name());
            assertEquals(before, fixture.total());
            for (Machine machine : fixture.machines) {
                assertEquals(1, machine.getStackInSlot(0).stackSize);
                assertEquals(0, machine.getStackInSlot(14).stackSize);
            }
        }
    }

    @Test
    void negativeDestinationProbeDoesNotDiscardAnotherCurrentlyAccessibleFace() throws Exception {
        ScaleFixture fixture = new ScaleFixture();
        Machine shifted = new Machine() {

            int downCalls;

            @Override
            public int[] getAccessibleSlotsFromSide(int side) {
                if (side == ForgeDirection.DOWN.ordinal()) {
                    // Visible during gather, then removed before the early negative probe.
                    return downCalls++ == 0 ? new int[] { 0 } : new int[0];
                }
                return side == ForgeDirection.UP.ordinal() ? new int[] { 0 } : new int[0];
            }

            @Override
            public boolean canInsertItem(int slot, ItemStack stack, int side) {
                return slot == 0 && side == ForgeDirection.UP.ordinal();
            }
        };
        fixture.machines[0] = shifted;
        fixture.base.world.tiles.put(new BlockPos(1, 64, 1), shifted);
        fixture.sourceChest.setInventorySlotContents(0, new ItemStack(INPUT, 1));
        ItemTransferResult result = fixture
            .transfer("changed-destination-face", LabelReference.literal("source"), LabelReference.literal("machine0"));
        assertEquals(1, result.getMoved());
        assertEquals(
            "COMPLETE",
            result.getStatus()
                .name());
        assertEquals(0, count(fixture.sourceChest));
        assertEquals(1, count(shifted));
    }

    private static final class ScaleFixture {

        final Fixture base = new Fixture();
        final Machine[] machines = new Machine[8];
        final InventoryTile sourceChest = new InventoryTile(6);
        final InventoryTile chest = new InventoryTile(54);
        final ProgramContext context;

        ScaleFixture() throws Exception {
            BlockCable cable = new BlockCable();
            for (int i = 0; i < machines.length; i++) {
                base.world.blocks.put(new BlockPos(i + 1, 64, 0), cable);
                Machine machine = machines[i] = new Machine();
                machine.setInventorySlotContents(14, new ItemStack(INPUT, 0, 24));
                BlockPos pos = new BlockPos(i + 1, 64, 1);
                base.add("machines", pos, machine);
                base.labels.addPosition("machine" + i, pos);
            }
            base.world.tiles.put(DST, chest);
            base.world.tiles.put(SRC, sourceChest);
            TileEntityManager manager = RecoveryFixtures.manager(base);
            context = new ProgramContext(
                base.world,
                manager,
                new CableNetwork(base.world, MANAGER),
                base.labels,
                20,
                false);
        }

        void selectionQueries() {
            QueryBudget budget = context.getInvocationQueryBudget();
            for (int i = 0; i < machines.length; i++) {
                LabelReference label = LabelReference.literal("machine" + i);
                assertTrue(context.available(label, ALL, budget));
                context.empty(label, ALL, budget);
            }
            assertTrue(context.available(LabelReference.literal("dest"), ALL, budget));
            context.empty(LabelReference.literal("dest"), ALL, budget);
        }

        ItemTransferResult transfer(String name, LabelReference source, LabelReference target) {
            for (Machine machine : machines) {
                machine.reads = machine.sides = machine.extraction = machine.insertion = 0;
            }
            QueryBudget budget = context.getInvocationQueryBudget();
            String before = counters(budget);
            String outcome = "not completed";
            try {
                ItemTransferResult result = context.transferItems(source, target, DataValue.selector(ALL));
                outcome = result.getMoved() + "/" + result.getRequested() + " " + result.getStatus();
                return result;
            } catch (IllegalArgumentException failure) {
                outcome = failure.getMessage();
                throw failure;
            } finally {
                int reads = 0, sides = 0, extraction = 0, insertion = 0;
                for (Machine machine : machines) {
                    reads += machine.reads;
                    sides += machine.sides;
                    extraction += machine.extraction;
                    insertion += machine.insertion;
                }
                System.out.println(
                    name + " before="
                        + before
                        + " after="
                        + counters(budget)
                        + " outcome="
                        + outcome
                        + " machineCallbacks(reads/sides/extract/insert)="
                        + reads
                        + "/"
                        + sides
                        + "/"
                        + extraction
                        + "/"
                        + insertion);
            }
        }

        int total() {
            int count = count(sourceChest) + count(chest);
            for (Machine machine : machines) count += count(machine);
            return count;
        }
    }

    private static String counters(QueryBudget budget) {
        return "slots:" + budget.getSlotWork()
            + ",providers:"
            + budget.getProviderCalls()
            + ",positions:"
            + budget.getPositions()
            + ",allocations:"
            + budget.getAllocations();
    }

    private static class Machine extends InventoryTile implements ISidedInventory {

        int reads, sides, extraction, insertion;

        Machine() {
            super(16);
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            reads++;
            return super.getStackInSlot(slot);
        }

        @Override
        public int[] getAccessibleSlotsFromSide(int side) {
            sides++;
            return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
        }

        @Override
        public boolean canExtractItem(int slot, ItemStack stack, int side) {
            extraction++;
            return slot == 15;
        }

        @Override
        public boolean canInsertItem(int slot, ItemStack stack, int side) {
            insertion++;
            return slot < 9;
        }
    }
}
