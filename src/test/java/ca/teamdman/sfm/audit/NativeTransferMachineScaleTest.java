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
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.Config;
import ca.teamdman.sfm.common.block.BlockCable;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ItemTransferResult;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

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

    @Test
    void repeatedBatchesShareInvocationBudgetAndConserveItemsOnExhaustion() throws Exception {
        int previousMultiplier = Config.queryBudgetMultiplier;
        Config.queryBudgetMultiplier = 1;
        try {
            for (boolean selectionQueries : new boolean[] { false, true }) {
                ScaleFixture fixture = new ScaleFixture();
                QueryBudget shared = fixture.context.getInvocationQueryBudget();
                IllegalArgumentException stopped = null;
                int completed = 0;
                for (int batch = 0; batch < 8; batch++) {
                    // Distinct input labels may resolve to the same physical provider, but do not get fresh budgets.
                    String sourceName = "input" + batch;
                    fixture.base.labels.addPosition(sourceName, SRC);
                    LabelReference source = LabelReference.literal(sourceName);
                    for (int slot = 0; slot < 6; slot++)
                        fixture.sourceChest.setInventorySlotContents(slot, new ItemStack(INPUT, 1, slot));
                    int before = fixture.total();
                    try {
                        LabelReference selected = LabelReference.literal("machine" + batch);
                        if (selectionQueries) {
                            assertTrue(fixture.context.available(source, ALL, shared));
                            assertFalse(fixture.context.empty(source, ALL, shared));
                            // Model findempty's ordered item-side scan, stopping at the first empty machine.
                            for (int candidate = 0; candidate <= batch; candidate++) {
                                LabelReference label = LabelReference.literal("machine" + candidate);
                                boolean empty = fixture.context.empty(label, ALL, shared);
                                assertEquals(candidate == batch, empty);
                                if (empty) {
                                    selected = label;
                                    break;
                                }
                            }
                        }
                        ItemTransferResult result = fixture
                            .transfer("batch-" + batch + "-queries-" + selectionQueries, source, selected);
                        assertEquals(6, result.getMoved());
                        completed++;
                    } catch (IllegalArgumentException failure) {
                        assertTrue(
                            failure.getMessage()
                                .contains("budget exhausted"),
                            failure.getMessage());
                        assertTrue(
                            failure.getMessage()
                                .contains("slot work before transfer:"),
                            failure.getMessage());
                        assertTrue(
                            failure.getMessage()
                                .contains("destination: machine" + batch),
                            failure.getMessage());
                        stopped = failure;
                    } finally {
                        assertEquals(before, fixture.total());
                        assertSame(shared, fixture.context.getInvocationQueryBudget());
                    }
                    if (stopped != null) break;
                }
                assertNotNull(stopped, "Eight batches must share the bounded invocation allowance");
                System.out.println(
                    "shared-batch-summary queries=" + selectionQueries
                        + " completed="
                        + completed
                        + " budget="
                        + counters(shared)
                        + " failure="
                        + stopped.getMessage());
            }
        } finally {
            Config.queryBudgetMultiplier = previousMultiplier;
        }
    }

    @Test
    void fourfoldBudgetCompletesEightSelectedBatchesWithinOneInvocation() throws Exception {
        int previousMultiplier = Config.queryBudgetMultiplier;
        Config.queryBudgetMultiplier = 4;
        try {
            ScaleFixture fixture = new ScaleFixture();
            QueryBudget shared = fixture.context.getInvocationQueryBudget();
            assertEquals(QueryBudget.MAX_SLOT_WORK * 4, shared.getSlotWorkLimit());
            for (int batch = 0; batch < 8; batch++) {
                LabelReference source = LabelReference.literal("input" + batch);
                fixture.base.labels.addPosition("input" + batch, SRC);
                for (int slot = 0; slot < 6; slot++)
                    fixture.sourceChest.setInventorySlotContents(slot, new ItemStack(INPUT, 1, slot));
                assertTrue(fixture.context.available(source, ALL, shared));
                assertFalse(fixture.context.empty(source, ALL, shared));
                LabelReference target = null;
                for (int candidate = 0; candidate <= batch; candidate++) {
                    LabelReference next = LabelReference.literal("machine" + candidate);
                    if (fixture.context.empty(next, ALL, shared)) {
                        target = next;
                        break;
                    }
                }
                assertNotNull(target);
                int before = fixture.total();
                ItemTransferResult result = fixture.transfer("scaled-batch-" + batch, source, target);
                assertEquals(6, result.getMoved());
                assertEquals(
                    "COMPLETE",
                    result.getStatus()
                        .name());
                assertEquals(before, fixture.total());
                assertSame(shared, fixture.context.getInvocationQueryBudget());
            }
            assertEquals(48, fixture.total());
            assertEquals(0, count(fixture.sourceChest));
            for (Machine machine : fixture.machines) assertEquals(6, count(machine));
            assertTrue(shared.getSlotWork() > QueryBudget.MAX_SLOT_WORK);
            assertTrue(shared.getSlotWork() < shared.getSlotWorkLimit());
        } finally {
            Config.queryBudgetMultiplier = previousMultiplier;
        }
    }

    @Test
    void configurationChangesOnlyAffectNewInvocationBudgetOwners() throws Exception {
        int previousMultiplier = Config.queryBudgetMultiplier;
        try {
            Config.queryBudgetMultiplier = 1;
            ScaleFixture old = new ScaleFixture();
            QueryBudget existing = old.context.getInvocationQueryBudget();
            existing.chargeSlots(9);
            Config.queryBudgetMultiplier = 4;
            assertSame(existing, old.context.getInvocationQueryBudget());
            assertEquals(QueryBudget.MAX_SLOT_WORK, existing.getSlotWorkLimit());
            assertEquals(9, existing.getSlotWork());
            QueryBudget fresh = new ScaleFixture().context.getInvocationQueryBudget();
            assertEquals(QueryBudget.MAX_SLOT_WORK * 4, fresh.getSlotWorkLimit());
            assertEquals(0, fresh.getSlotWork());
            assertEquals(QueryBudget.MAX_SLOT_WORK, new QueryBudget().getSlotWorkLimit());
        } finally {
            Config.queryBudgetMultiplier = previousMultiplier;
        }
    }

    @Test
    void compiledBatchLoopWithWaitPreservesProgressAndUsesFreshInvocationBudgets() throws Exception {
        ScaleFixture fixture = new ScaleFixture();
        EmptyFluidChest source = new EmptyFluidChest();
        fixture.base.world.tiles.put(SRC, source);
        for (int i = 0; i < 8; i++) {
            fixture.base.labels.addPosition("input" + i, SRC);
            Machine machine = fixture.machines[i] = new EmptyFluidMachine();
            machine.setInventorySlotContents(14, new ItemStack(INPUT, 0, 24));
            fixture.base.world.tiles.put(new BlockPos(i + 1, 64, 1), machine);
        }
        ProgramBuildResult built = new ProgramBuilder(
            "label_ref findempty() { for i = 0 to 7 { let candidate = label(\"machine\" + i); "
                + "if (candidate.empty() && candidate.fempty()) { return candidate; } } return label(\"missing\"); } "
                + "every 20 ticks { for i = 0 to 7 { let src = label(\"input\" + i); "
                + "if (src.available() && !src.empty()) { let dest = findempty(); if (dest.available()) { "
                + "transfer(src, dest); transferf(src, dest); setcircuit(dest, i + 1); } } wait(1); } "
                + "INPUT FROM \"machines\" OUTPUT TO \"dest\" FORGET; }").build();
        assertTrue(built.isSuccess(), built.getErrorMessage());
        Program program = built.getProgram();
        QueryBudget previous = null;
        for (int step = 0; step <= 8; step++) {
            if (step < 8) for (int slot = 0; slot < 6; slot++)
                source.setInventorySlotContents(slot, new ItemStack(INPUT, 1, slot));
            long tick = 20 + step;
            fixture.base.world.time = tick;
            ProgramContext live = new ProgramContext(
                fixture.base.world,
                fixture.context.getManager(),
                fixture.context.getNetwork(),
                fixture.base.labels,
                tick,
                false);
            QueryBudget budget = live.getInvocationQueryBudget();
            assertNotSame(previous, budget);
            assertEquals(0, budget.getSlotWork());
            program.tick(live);
            System.out.println("wait-batch step=" + step + " budget=" + counters(budget));
            assertEquals(0, count(source));
            for (int i = 0; i < 8; i++) assertEquals(i <= step ? 6 : 0, count(fixture.machines[i]));
            assertEquals(
                step < 8 ? 1 : 0,
                fixture.context.getManager()
                    .getProgramExecution(program)
                    .sleepingCount());
            previous = budget;
        }
        assertEquals(0, count(fixture.chest));
    }

    /** Empty Forge fluid ports qualify fempty without inventing GT integration or real fluid movements. */
    private interface EmptyFluidPort extends IFluidHandler {

        @Override
        default int fill(ForgeDirection side, FluidStack fluid, boolean commit) {
            return 0;
        }

        @Override
        default FluidStack drain(ForgeDirection side, FluidStack fluid, boolean commit) {
            return null;
        }

        @Override
        default FluidStack drain(ForgeDirection side, int amount, boolean commit) {
            return null;
        }

        @Override
        default boolean canFill(ForgeDirection side, Fluid fluid) {
            return false;
        }

        @Override
        default boolean canDrain(ForgeDirection side, Fluid fluid) {
            return false;
        }

        @Override
        default FluidTankInfo[] getTankInfo(ForgeDirection side) {
            return new FluidTankInfo[] { new FluidTankInfo(null, 1000) };
        }
    }

    private static final class EmptyFluidChest extends InventoryTile implements EmptyFluidPort {

        EmptyFluidChest() {
            super(6);
        }
    }

    private static final class EmptyFluidMachine extends Machine implements EmptyFluidPort {
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
