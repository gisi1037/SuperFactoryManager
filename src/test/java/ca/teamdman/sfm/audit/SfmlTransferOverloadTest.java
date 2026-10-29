package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.RegistryNamespaced;
import net.minecraft.util.RegistrySimple;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.ast.TransferStatement;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.ItemTransferResult;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;
import ca.teamdman.sfml.program_builder.VisualCapabilityAnalyzer;
import ca.teamdman.sfml.program_builder.VisualCapabilityReport;

/** Automatic amounts observe live visible stock once, then use the ordinary bounded transfer path. */
class SfmlTransferOverloadTest {

    private static final Item FIRST = new Item(), SECOND = new Item();
    private static final String PREFIX = "TRANSFER (LABEL(\"source\"), LABEL(\"dest\")";
    private static final LabelReference SOURCE = LabelReference.literal("source"),
        DEST = LabelReference.literal("dest");
    private static final DataValue ALL = DataValue.selector(ResourceSelector.item("*"));

    static {
        try {
            Field namesField = RegistrySimple.class.getDeclaredField("registryObjects");
            namesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> names = (Map<String, Object>) namesField.get(Item.itemRegistry);
            names.put("sfm_transfer_overloads:first", FIRST);
            names.put("sfm_transfer_overloads:second", SECOND);
            Field idsField = RegistryNamespaced.class.getDeclaredField("underlyingIntegerMap");
            idsField.setAccessible(true);
            ObjectIntIdentityMap ids = (ObjectIntIdentityMap) idsField.get(Item.itemRegistry);
            ids.func_148746_a(FIRST, 32061);
            ids.func_148746_a(SECOND, 32062);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    @Test
    void twoArgumentsMoveMixedItemsAndVariantsWithAnObservedNumericRequest() throws Exception {
        Fixture fixture = new Fixture();
        fixture.source.setInventorySlotContents(0, stack(FIRST, 3, 1, "a"));
        fixture.source.setInventorySlotContents(1, stack(FIRST, 4, 2, "b"));
        fixture.source.setInventorySlotContents(2, stack(SECOND, 5, 0, null));
        Program program = program(
            "STATE $r = {requested:0,moved:0,remaining:0,status:\"\"} EVERY 20 TICKS DO " + PREFIX
                + ") INTO $sent SET $r = $sent END");
        Memory memory = new Memory(program);
        ProgramContext context = context(fixture);
        context.setStateAccess(memory);
        program.tick(context);
        assertEquals(
            12,
            memory.value.get("requested")
                .asLong());
        assertEquals(
            12,
            memory.value.get("moved")
                .asLong());
        assertEquals(
            0,
            memory.value.get("remaining")
                .asLong());
        assertEquals(
            "complete",
            memory.value.get("status")
                .asString());
        assertEquals(0, count(fixture.source));
        assertEquals(12, count(fixture.dest));
        assertEquals(
            1,
            fixture.dest.getStackInSlot(0)
                .getItemDamage());
        assertEquals(
            2,
            fixture.dest.getStackInSlot(1)
                .getItemDamage());
        assertSame(
            SECOND,
            fixture.dest.getStackInSlot(2)
                .getItem());
    }

    @Test
    void threeArgumentsUseOnlyTheMatchingSelectorStock() throws Exception {
        Fixture fixture = new Fixture();
        fixture.source.setInventorySlotContents(0, stack(FIRST, 3, 1, "a"));
        fixture.source.setInventorySlotContents(1, stack(FIRST, 4, 2, "b"));
        fixture.source.setInventorySlotContents(2, stack(SECOND, 9, 0, null));
        program("EVERY 20 TICKS DO " + PREFIX + ", ITEM(\"sfm_transfer_overloads:first\")) END").tick(context(fixture));
        assertEquals(9, count(fixture.source));
        assertEquals(7, count(fixture.dest));
        assertSame(
            SECOND,
            fixture.source.getStackInSlot(2)
                .getItem());
    }

    @Test
    void exactKeyAutomaticAmountCountsOnlyItsFullIdentity() throws Exception {
        Fixture fixture = new Fixture();
        fixture.source.setInventorySlotContents(0, stack(FIRST, 3, 7, "same"));
        fixture.source.setInventorySlotContents(1, stack(FIRST, 5, 7, "different"));
        fixture.source.setInventorySlotContents(2, stack(FIRST, 11, 8, "same"));
        ItemStack selected = fixture.source.getStackInSlot(0);
        DataValue exact = DataValue.itemIdentity(ItemIdentity.fromStack(selected, new QueryBudget()));
        assertResult(context(fixture).transferItems(SOURCE, DEST, exact), 3, 3, "COMPLETE");
        assertEquals(16, count(fixture.source));
        assertEquals(3, count(fixture.dest));
        assertEquals(
            selected.getTagCompound(),
            fixture.dest.getStackInSlot(0)
                .getTagCompound());
    }

    @Test
    void automaticRequestReportsRealRemainderForPartialAndFullDestinations() throws Exception {
        Fixture fixture = new Fixture();
        fixture.source.setInventorySlotContents(0, stack(FIRST, 10, 0, null));
        InventoryTile destination = new InventoryTile(1);
        destination.setInventorySlotContents(0, stack(FIRST, 60, 0, null));
        fixture.world.tiles.put(DST, destination);
        assertResult(context(fixture).transferItems(SOURCE, DEST, ALL), 10, 4, "PARTIAL");
        assertResult(context(fixture).transferItems(SOURCE, DEST, ALL), 6, 0, "NO_CAPACITY");
        assertEquals(6, count(fixture.source));
        assertEquals(64, count(destination));
    }

    @Test
    void emptyStockAndUnavailableGroupsRemainDistinct() throws Exception {
        Fixture empty = new Fixture();
        assertResult(context(empty).transferItems(SOURCE, DEST, ALL), 0, 0, "COMPLETE");
        Fixture missing = new Fixture();
        assertResult(context(missing).transferItems(LabelReference.literal("absent"), DEST, ALL), 0, 0, "UNAVAILABLE");
        Fixture unloaded = new Fixture();
        unloaded.source.setInventorySlotContents(0, stack(FIRST, 9, 0, null));
        unloaded.world.unloaded.add(SRC);
        assertResult(context(unloaded).transferItems(SOURCE, DEST, ALL), 0, 0, "UNAVAILABLE");
        Fixture missingDestination = new Fixture();
        missingDestination.source.setInventorySlotContents(0, stack(FIRST, 9, 0, null));
        assertResult(
            context(missingDestination).transferItems(SOURCE, LabelReference.literal("absent"), ALL),
            0,
            0,
            "UNAVAILABLE");
        assertEquals(9, count(missingDestination.source));
    }

    @Test
    void automaticSnapshotHonorsVisibleSidesAndSlotsAndEngineExtractionPermissions() throws Exception {
        Fixture fixture = new Fixture();
        SidedInventory source = new SidedInventory(), destination = new SidedInventory();
        source.visibleSide = ForgeDirection.UP.ordinal();
        destination.visibleSide = ForgeDirection.DOWN.ordinal();
        source.setInventorySlotContents(0, stack(FIRST, 13, 0, null));
        source.setInventorySlotContents(1, stack(FIRST, 8, 0, null));
        fixture.world.tiles.put(SRC, source);
        fixture.world.tiles.put(DST, destination);
        LabelReference from = SOURCE.side(ForgeDirection.UP.ordinal())
            .slots(1, 1);
        LabelReference to = DEST.side(ForgeDirection.DOWN.ordinal())
            .slots(1, 1);
        assertResult(context(fixture).transferItems(from, to, ALL), 8, 8, "COMPLETE");
        assertEquals(13, count(source));
        assertNull(destination.getStackInSlot(0));
        assertEquals(8, destination.getStackInSlot(1).stackSize);
        source.setInventorySlotContents(1, stack(FIRST, 8, 0, null));
        source.allowExtraction = false;
        assertResult(context(fixture).transferItems(from, to, ALL), 8, 0, "NO_MATCH");
        assertEquals(21, count(source));
    }

    @Test
    void overlappingPhysicalInventoryCannotFeedItsOwnSelectedSourceSlots() throws Exception {
        Fixture fixture = new Fixture();
        fixture.source.setInventorySlotContents(0, stack(FIRST, 9, 0, null));
        fixture.world.tiles.put(DST, fixture.source);
        assertResult(context(fixture).transferItems(SOURCE, DEST, ALL), 9, 0, "NO_CAPACITY");
        assertEquals(9, count(fixture.source));
        assertNull(fixture.source.getStackInSlot(1));
    }

    @Test
    void automaticSnapshotDoesNotReserveStockAgainstLaterProviderChanges() throws Exception {
        Fixture fixture = new Fixture();
        SidedInventory source = new SidedInventory() {

            boolean changed;

            @Override
            public boolean canExtractItem(int slot, ItemStack stack, int side) {
                if (!changed) {
                    changed = true;
                    getStackInSlot(slot).stackSize = 3;
                }
                return super.canExtractItem(slot, stack, side);
            }
        };
        source.visibleSide = ForgeDirection.UP.ordinal();
        source.setInventorySlotContents(1, stack(FIRST, 9, 0, null));
        fixture.world.tiles.put(SRC, source);
        assertResult(
            context(fixture).transferItems(
                SOURCE.side(ForgeDirection.UP.ordinal())
                    .slots(1, 1),
                DEST,
                ALL),
            9,
            3,
            "PARTIAL");
        assertEquals(0, count(source));
        assertEquals(3, count(fixture.dest));
    }

    @Test
    void allAritiesExecuteWithoutIntoAndRoundTripWithOrWithoutAResult() throws Exception {
        for (String arguments : Arrays.asList("", ", ITEM(\"*\")", ", ITEM(\"*\"), 2")) {
            for (String result : Arrays.asList("", " INTO $sent")) {
                Fixture fixture = new Fixture();
                fixture.source.setInventorySlotContents(0, stack(FIRST, 7, 0, null));
                String source = "EVERY 20 TICKS DO " + PREFIX + arguments + ")" + result + " END";
                ProgramBuildResult built = new ProgramBuilder(source).buildWithSyntax();
                assertTrue(built.isSuccess(), built.getErrorMessage());
                Program program = built.getProgram();
                assertEquals(program.toString(), program(program.toString()).toString());
                VisualCapabilityReport report = VisualCapabilityAnalyzer.analyze(built);
                assertFalse(report.isAvailable());
                assertTrue(
                    report.getBlockers()
                        .stream()
                        .anyMatch(blocker -> blocker.kind == VisualCapabilityReport.Kind.TRANSFER_STATEMENT));
                program.tick(context(fixture));
                assertEquals(arguments.endsWith(", 2") ? 2 : 7, count(fixture.dest), source);
            }
        }
    }

    @Test
    void explicitArgumentExpressionsEvaluateExactlyOnceInSourceOrderForEveryArity() throws Exception {
        for (int arity : new int[] { 2, 3, 4 }) {
            Fixture fixture = new Fixture();
            fixture.source.setInventorySlotContents(0, stack(FIRST, 7, 0, null));
            List<Integer> order = new ArrayList<>();
            List<DataExpression> arguments = new ArrayList<>();
            arguments.add(argument(0, DataValue.label(SOURCE), order));
            arguments.add(argument(1, DataValue.label(DEST), order));
            if (arity >= 3) arguments.add(argument(2, ALL, order));
            if (arity == 4) arguments.add(argument(3, DataValue.integer(2), order));
            ProgramContext context = context(fixture);
            context.beginDataInvocation(0, false);
            new TransferStatement(arguments, null).tick(context);
            assertEquals(
                Arrays.asList(0, 1, 2, 3)
                    .subList(0, arity),
                order);
            assertEquals(arity == 4 ? 2 : 7, count(fixture.dest));
        }
    }

    @Test
    void discardedResultsDoNotAllocateARecordButStillRequireAuthority() throws Exception {
        Fixture fixture = new Fixture();
        fixture.source.setInventorySlotContents(0, stack(FIRST, 7, 0, null));
        Program program = program("EVERY 20 TICKS DO " + PREFIX + ") END");
        ProgramContext context = context(fixture);
        context.beginDataInvocation(0, false);
        context.getDataRuntime()
            .allocate(16384);
        program.getTriggers()
            .get(0)
            .getBlock()
            .tick(context);
        assertEquals(7, count(fixture.dest));
        ProgramContext unauthorized = new ProgramContext(null, null, null, null, 20, false);
        unauthorized.beginDataInvocation(0, false);
        assertThrows(
            IllegalStateException.class,
            () -> program.getTriggers()
                .get(0)
                .getBlock()
                .tick(unauthorized));
        assertEquals(
            0,
            unauthorized.getInvocationQueryBudget()
                .getProviderCalls());
        Fixture removedDisk = new Fixture();
        ProgramContext withoutDisk = context(removedDisk);
        withoutDisk.getManager()
            .setInventorySlotContents(0, null);
        assertThrows(IllegalStateException.class, () -> withoutDisk.transferItems(SOURCE, DEST, ALL));
        assertEquals(
            0,
            withoutDisk.getInvocationQueryBudget()
                .getProviderCalls());
    }

    @Test
    void ignoredAutomaticResultsStillSpendTheInvocationActionLimitEvenWithEmptySources() throws Exception {
        Fixture fixture = new Fixture();
        Program program = program("EVERY 20 TICKS DO " + PREFIX + ") END");
        ProgramContext context = context(fixture);
        context.beginDataInvocation(0, false);
        for (int i = 0; i < 64; i++) program.getTriggers()
            .get(0)
            .getBlock()
            .tick(context);
        int providerCalls = context.getInvocationQueryBudget()
            .getProviderCalls();
        IllegalArgumentException exhausted = assertThrows(
            IllegalArgumentException.class,
            () -> program.getTriggers()
                .get(0)
                .getBlock()
                .tick(context));
        assertTrue(
            exhausted.getMessage()
                .contains("action limit"));
        assertEquals(
            providerCalls,
            context.getInvocationQueryBudget()
                .getProviderCalls());
        assertEquals(0, count(fixture.source));
        assertEquals(0, count(fixture.dest));
    }

    @Test
    void automaticSnapshotBudgetFailureCannotMoveAnythingOrBecomeAnUnavailableResult() throws Exception {
        Fixture fixture = new Fixture();
        fixture.source.setInventorySlotContents(0, stack(FIRST, 7, 0, null));
        ProgramContext context = context(fixture);
        context.getInvocationQueryBudget()
            .chargeProviders(QueryBudget.MAX_PROVIDER_CALLS);
        assertThrows(IllegalArgumentException.class, () -> context.transferItems(SOURCE, DEST, ALL));
        assertEquals(7, count(fixture.source));
        assertEquals(0, count(fixture.dest));
    }

    @Test
    void omittedArgumentsDoNotPermitBadTypesAritiesOrExpressionEffects() {
        for (String statement : Arrays.asList(
            "TRANSFER ()",
            "TRANSFER (LABEL(\"source\"))",
            "TRANSFER (7, LABEL(\"dest\"))",
            PREFIX + ", 7)",
            PREFIX + ", ITEM(\"*\"), TRUE)",
            PREFIX + ", ITEM(\"*\"), 1, 2)",
            "LET $x = " + PREFIX + ")")) {
            ProgramBuildResult built = new ProgramBuilder("EVERY 20 TICKS DO " + statement + " END").build();
            assertFalse(built.isSuccess(), statement);
        }
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

    private static Program program(String text) {
        ProgramBuildResult result = new ProgramBuilder(text).build();
        assertTrue(result.isSuccess(), result.getErrorMessage());
        return result.getProgram();
    }

    private static ItemStack stack(Item item, int count, int metadata, String name) {
        ItemStack stack = new ItemStack(item, count, metadata);
        if (name != null) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("name", name);
            stack.setTagCompound(nbt);
        }
        return stack;
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

    private static DataExpression argument(int index, DataValue value, List<Integer> order) {
        return new DataExpression() {

            @Override
            public DataType getType() {
                return value.getType();
            }

            @Override
            public DataValue evaluate(DataRuntime runtime) {
                order.add(index);
                return value;
            }
        };
    }

    private static class SidedInventory extends InventoryTile implements ISidedInventory {

        int visibleSide;
        boolean allowExtraction = true;

        SidedInventory() {
            super(2);
        }

        @Override
        public int[] getAccessibleSlotsFromSide(int side) {
            return side == visibleSide ? new int[] { 1 } : new int[0];
        }

        @Override
        public boolean canExtractItem(int slot, ItemStack stack, int side) {
            return allowExtraction && side == visibleSide && slot == 1;
        }

        @Override
        public boolean canInsertItem(int slot, ItemStack stack, int side) {
            return side == visibleSide && slot == 1;
        }
    }

    private static final class Memory implements StateAccess {

        DataValue value;

        Memory(Program program) {
            value = program.getStateSchema()
                .getDeclarations()
                .get(0)
                .getDefaultValue();
        }

        @Override
        public DataValue read(int slot) {
            return value;
        }

        @Override
        public void write(int slot, DataValue next) {
            value = next;
        }
    }
}
