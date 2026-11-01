package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.program.RedstoneTriggerTracker;
import ca.teamdman.sfm.common.tileentity.TileEntityRedstonePort;
import ca.teamdman.sfml.ast.Block;
import ca.teamdman.sfml.ast.BoolHas;
import ca.teamdman.sfml.ast.ComparisonOperator;
import ca.teamdman.sfml.ast.ForgetStatement;
import ca.teamdman.sfml.ast.InputStatement;
import ca.teamdman.sfml.ast.Label;
import ca.teamdman.sfml.ast.LabelAccess;
import ca.teamdman.sfml.ast.NamedRedstoneTrigger;
import ca.teamdman.sfml.ast.NumberRange;
import ca.teamdman.sfml.ast.NumberRangeSet;
import ca.teamdman.sfml.ast.OutputStatement;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.ast.RedstoneOutputStatement;
import ca.teamdman.sfml.ast.ResourceIdSet;
import ca.teamdman.sfml.ast.ResourceLimits;
import ca.teamdman.sfml.ast.RoundRobin;
import ca.teamdman.sfml.ast.SetOperator;
import ca.teamdman.sfml.ast.Side;
import ca.teamdman.sfml.ast.SideQualifier;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataExpression;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.ProgramStateSchema;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

/** Typed legacy IO keeps immutable name/side/slot snapshots, never captured inventories or live bindings. */
class SfmlTypedLabelAccessTest {

    private static LabelAccess access(Label... labels) {
        return new LabelAccess(Arrays.asList(labels), null, null, null);
    }

    private static final ResourceLimits OUTPUT_LIMITS = new ResourceLimits(
        Collections.singletonList(ca.teamdman.sfml.ast.ResourceLimit.DEFAULT_OUTPUT),
        null);

    private static DataCompiler localCompiler() {
        DataCompiler compiler = new DataCompiler();
        compiler.beginTrigger();
        compiler.pushScope();
        return compiler;
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

    @Test
    void quotedNamesAreExactAndBareNamesNeverFallBackToDiskLabels() throws Exception {
        Fixture fixture = new Fixture();
        fixture.stock(0, 5);
        fixture.run("INPUT FROM \"source\" OUTPUT TO \"dest\"");
        assertEquals(5, count(fixture.dest));
        for (String body : Arrays.asList(
            "INPUT FROM source",
            "OUTPUT TO dest",
            "FORGET source",
            "IF source HAS > 0 THEN FORGET END",
            "OUTPUT 1 REDSTONE LEVEL TO source")) {
            assertFalse(
                new ProgramBuilder("EVERY 20 TICKS DO " + body + " END").build()
                    .isSuccess(),
                body);
        }
        assertFalse(
            new ProgramBuilder("EVERY REDSTONE PULSE FROM source DO END").build()
                .isSuccess());
        assertFalse(
            new ProgramBuilder("EVERY 20 TICKS DO LET source = \"source\" INPUT FROM source END").build()
                .isSuccess(),
            "STRING variables are not implicitly converted to LABEL_REF");
        assertThrows(IllegalArgumentException.class, () -> new Label(" source"));
        assertThrows(IllegalArgumentException.class, () -> new Label("source "));
        assertEquals("\"Exact Case\"", new Label("Exact Case").toString());
    }

    @Test
    void declaredBareVariablesAndConstantCallsTransferAndRoundTrip() throws Exception {
        Fixture fixture = new Fixture();
        fixture.stock(0, 7);
        Program program = parse(
            "FUNCTION target() RETURNS LABEL_REF DO RETURN label(\"dest\") END "
                + "EVERY 20 TICKS DO LET source = label(\"source\") INPUT FROM source OUTPUT TO target() END");
        program.tick(context(fixture));
        assertEquals(7, count(fixture.dest));
        assertEquals(0, count(fixture.source));
        parse(program.toString());
    }

    @Test
    void quotedProcedureLabelsFollowReachabilityWithoutForcingUnusedDataFrames() {
        Program unused = parse("void unused() { INPUT FROM \"hidden\"; } EVERY 20 TICKS {}");
        assertTrue(
            unused.getReferencedLabels()
                .isEmpty());
        assertFalse(unused.hasDataFeatures());
        ProgramContext context = new ProgramContext(null, null, null, null, 20, false);
        unused.tick(context);
        assertThrows(IllegalStateException.class, context::getDataRuntime);
        Program called = parse(
            "void unused() { INPUT FROM \"hidden\"; } "
                + "void used() { INPUT FROM \"source\"; OUTPUT TO \"dest\"; } EVERY 20 TICKS { used(); }");
        assertEquals(new java.util.HashSet<>(java.util.Arrays.asList("source", "dest")), called.getReferencedLabels());
    }

    @Test
    void inputSnapshotsAHandleBeforeItsBindingChanges() throws Exception {
        Fixture fixture = new Fixture();
        fixture.stock(0, 9);
        DataCompiler compiler = localCompiler();
        DataCompiler.Binding binding = compiler.declareLocal("selected", DataType.LABEL_REF);
        ProgramContext context = context(fixture);
        context.beginDataInvocation(1, false);
        binding.write(
            context.getDataRuntime(),
            DataValue.label(
                LabelReference.of("source")
                    .slots(0, 0)));
        InputStatement input = new InputStatement(access(new Label(compiler.compile("selected", 1, 0))), null, false);
        input.tick(context);
        binding.write(context.getDataRuntime(), DataValue.label(LabelReference.of("dest")));
        new OutputStatement(access(new Label("dest")), OUTPUT_LIMITS, false, false).tick(context);
        assertEquals(9, count(fixture.dest));
        assertEquals(0, count(fixture.source));
        assertTrue(
            input.getLabelAccess()
                .isDynamic(),
            "Compiled plan remains dynamic and contains no invocation state");
    }

    @Test
    void existingBoundArrayHandlesResolveAndSnapshotWithoutStringCoercion() throws Exception {
        Fixture fixture = new Fixture();
        fixture.stock(1, 4);
        DataCompiler compiler = localCompiler();
        DataExpression array = compiler.compile("[label(\"source\").slots(1,1), label(\"dest\") ]", 1, 0);
        DataCompiler.Binding refs = compiler.declareLocal("refs", array.getType());
        ProgramContext context = context(fixture);
        context.beginDataInvocation(1, false);
        refs.write(context.getDataRuntime(), array.evaluate(context.getDataRuntime()));
        new InputStatement(access(new Label(compiler.compile("refs[0]", 1, 0))), null, false).tick(context);
        new OutputStatement(access(new Label(compiler.compile("refs[1]", 1, 0))), OUTPUT_LIMITS, false, false)
            .tick(context);
        assertEquals(4, count(fixture.dest));
        assertThrows(IllegalArgumentException.class, () -> new Label(compiler.compile("\"source\"", 1, 0)));
    }

    @Test
    void parsedArrayAndPostfixHandlesKeepTheInputSnapshotAfterReassignment() throws Exception {
        Fixture fixture = new Fixture();
        PairInventory target = new PairInventory();
        fixture.add("dest", DST, target);
        fixture.stock(0, 3);
        fixture.stock(1, 5);
        Program program = parse(
            "EVERY 20 TICKS DO "
                + "LET refs = [label(\"source\").slots(1,1), label(\"dest\").side(\"east\").slots(1,1)] "
                + "INPUT FROM refs[0] refs[0] = label(\"dest\") OUTPUT TO refs[1] END");
        program.tick(context(fixture));
        assertEquals(5, count(target));
        assertEquals(5, target.getStackInSlot(1).stackSize);
        assertEquals(3, count(fixture.source));
        parse(program.toString());
    }

    @Test
    void multipleHandlesNeverCrossProductSidesAndSlotsAtOnePosition() throws Exception {
        Fixture fixture = new Fixture();
        PairInventory inventory = new PairInventory();
        fixture.add("source", SRC, inventory);
        fixture.labels.addPosition("alias", SRC);
        inventory.setInventorySlotContents(0, new ItemStack(ITEM, 3));
        inventory.setInventorySlotContents(1, new ItemStack(ITEM, 5));
        inventory.setInventorySlotContents(2, new ItemStack(ITEM, 11));
        LabelAccess selected = access(
            new Label(
                LabelReference.of("source")
                    .side(2)
                    .slots(0, 0)),
            new Label(
                LabelReference.of("alias")
                    .side(5)
                    .slots(1, 1)));
        assertTrue(
            selected.getSlots(SRC, ForgeDirection.NORTH, fixture.labels)
                .contains(0));
        assertFalse(
            selected.getSlots(SRC, ForgeDirection.NORTH, fixture.labels)
                .contains(1));
        assertTrue(
            selected.getSlots(SRC, ForgeDirection.EAST, fixture.labels)
                .contains(1));
        assertFalse(
            selected.getSlots(SRC, ForgeDirection.EAST, fixture.labels)
                .contains(0));
        ProgramContext context = context(fixture);
        assertTrue(
            new BoolHas(
                SetOperator.OVERALL,
                selected,
                ComparisonOperator.EQUALS,
                8,
                ResourceIdSet.MATCH_ALL,
                null,
                null).evaluate(context));
        new InputStatement(selected, null, false).tick(context);
        new OutputStatement(access(new Label("dest")), OUTPUT_LIMITS, false, false).tick(context);
        assertEquals(8, count(fixture.dest));
        assertEquals(11, count(inventory));
        assertTrue(inventory.extractionPairs.contains("2:0"));
        assertTrue(inventory.extractionPairs.contains("5:1"));
        assertFalse(inventory.extractionPairs.contains("2:1"));
        assertFalse(inventory.extractionPairs.contains("5:0"));
    }

    @Test
    void statementQualifiersIntersectHandleRestrictionsInsteadOfReplacingThem() throws Exception {
        Fixture fixture = new Fixture();
        LabelAccess selected = new LabelAccess(
            Collections.singletonList(
                new Label(
                    LabelReference.of("source")
                        .side(2)
                        .slots(1, 3))),
            new SideQualifier(Arrays.asList(Side.NORTH, Side.EAST)),
            NumberRangeSet.of(new NumberRange(2, 4)),
            null);
        assertEquals(
            Collections.singletonList(ForgeDirection.NORTH),
            selected.getSides(SRC, fixture.labels)
                .resolveDirections());
        assertFalse(
            selected.getSlots(SRC, ForgeDirection.NORTH, fixture.labels)
                .contains(1));
        assertTrue(
            selected.getSlots(SRC, ForgeDirection.NORTH, fixture.labels)
                .contains(2));
        assertFalse(
            selected.getSlots(SRC, ForgeDirection.NORTH, fixture.labels)
                .contains(4));
        assertFalse(
            selected.getSlots(SRC, ForgeDirection.EAST, fixture.labels)
                .contains(2));
        assertTrue(
            selected.getFluidSides(SRC, fixture.labels)
                .resolveDirections()
                .isEmpty(),
            "Slot-restricted handles cannot silently broaden to every tank");
    }

    @Test
    void outputHandlesPreserveDestinationSlotsAndSides() throws Exception {
        Fixture fixture = new Fixture();
        PairInventory target = new PairInventory();
        fixture.add("dest", DST, target);
        fixture.stock(0, 10);
        ProgramContext context = context(fixture);
        new InputStatement(access(new Label("source")), null, false).tick(context);
        new OutputStatement(
            access(
                new Label(
                    LabelReference.of("dest")
                        .side(5)
                        .slots(1, 1))),
            OUTPUT_LIMITS,
            false,
            false).tick(context);
        assertEquals(10, target.getStackInSlot(1).stackSize);
        assertNull(target.getStackInSlot(0));
        assertNull(target.getStackInSlot(2));
        assertTrue(target.insertionPairs.contains("5:1"));
        assertFalse(target.insertionPairs.contains("2:1"));
    }

    @Test
    void qualifiedForgetFailsBeforeClearingAnyInput() throws Exception {
        Fixture fixture = new Fixture();
        fixture.stock(0, 6);
        ProgramContext context = context(fixture);
        new InputStatement(access(new Label("source")), null, false).tick(context);
        ForgetStatement forget = new ForgetStatement(
            Arrays.asList(
                new Label("source"),
                new Label(
                    LabelReference.of("dest")
                        .side(2))));
        assertThrows(IllegalArgumentException.class, () -> forget.tick(context));
        new OutputStatement(access(new Label("dest")), OUTPUT_LIMITS, false, false).tick(context);
        assertEquals(6, count(fixture.dest));
    }

    @Test
    void forgettingOneAliasRemovesItsCachedRestrictedSlots() throws Exception {
        Fixture fixture = new Fixture();
        fixture.stock(0, 3);
        fixture.stock(1, 5);
        fixture.labels.addPosition("alias", SRC);
        ProgramContext context = context(fixture);
        new InputStatement(
            access(
                new Label(
                    LabelReference.of("source")
                        .slots(0, 0)),
                new Label(
                    LabelReference.of("alias")
                        .slots(1, 1))),
            null,
            false).tick(context);
        // A zero output gathers the input cache without consuming the registration.
        ca.teamdman.sfml.ast.ResourceLimit zero = new ca.teamdman.sfml.ast.ResourceLimit(
            null,
            new ca.teamdman.sfml.ast.Limit(
                ca.teamdman.sfml.ast.ResourceQuantity.ZERO,
                ca.teamdman.sfml.ast.ResourceQuantity.ZERO),
            null);
        new OutputStatement(
            access(new Label("dest")),
            new ResourceLimits(Collections.singletonList(zero), null),
            false,
            false).tick(context);
        new ForgetStatement(Collections.singletonList(new Label("source"))).tick(context);
        new OutputStatement(access(new Label("dest")), OUTPUT_LIMITS, false, false).tick(context);
        assertEquals(5, count(fixture.dest));
        assertEquals(3, count(fixture.source));
    }

    @Test
    void partialForgetPreservesQualifiedRoundRobinSelectionAndItsConsumedQuota() throws Exception {
        for (boolean forgetSelected : new boolean[] { false, true }) {
            Fixture fixture = new Fixture();
            PairInventory inventory = new PairInventory();
            fixture.add("source", SRC, inventory);
            fixture.labels.addPosition("alias", SRC);
            inventory.setInventorySlotContents(0, new ItemStack(ITEM, 3));
            inventory.setInventorySlotContents(1, new ItemStack(ITEM, 5));
            RoundRobin cursor = new RoundRobin(RoundRobin.Mode.BY_LABEL);
            LabelAccess selected = new LabelAccess(
                java.util.Arrays.asList(
                    new Label(
                        LabelReference.of("source")
                            .side(2)
                            .slots(0, 0)),
                    new Label(
                        LabelReference.of("alias")
                            .side(5)
                            .slots(1, 1))),
                null,
                null,
                cursor);
            ResourceLimits inputTwo = new ResourceLimits(
                Collections.singletonList(
                    new ca.teamdman.sfml.ast.ResourceLimit(
                        null,
                        new ca.teamdman.sfml.ast.Limit(
                            new ca.teamdman.sfml.ast.ResourceQuantity(2, false),
                            ca.teamdman.sfml.ast.ResourceQuantity.ZERO),
                        null)),
                null);
            ResourceLimits outputOne = new ResourceLimits(
                Collections.singletonList(
                    new ca.teamdman.sfml.ast.ResourceLimit(
                        null,
                        new ca.teamdman.sfml.ast.Limit(
                            new ca.teamdman.sfml.ast.ResourceQuantity(1, false),
                            ca.teamdman.sfml.ast.ResourceQuantity.UNLIMITED),
                        null)),
                null);
            ProgramContext context = context(fixture);
            new InputStatement(selected, inputTwo, false).tick(context);
            new OutputStatement(access(new Label("dest")), outputOne, false, false).tick(context);
            assertEquals(1, count(fixture.dest));
            new ForgetStatement(Collections.singletonList(new Label(forgetSelected ? "source" : "alias")))
                .tick(context);
            new OutputStatement(access(new Label("dest")), OUTPUT_LIMITS, false, false).tick(context);
            assertEquals(forgetSelected ? 1 : 2, count(fixture.dest));
            assertEquals(forgetSelected ? 2 : 1, inventory.getStackInSlot(0).stackSize);
            assertEquals(5, inventory.getStackInSlot(1).stackSize, "FORGET must never activate the unselected alias");
            assertEquals(
                1,
                cursor.nextIndex(2),
                "Partial FORGET must neither reset selection nor consume another turn");
        }
    }

    @Test
    void globalDynamicHeadersHaveFramesOnEveryInvocationAndRespectSelectedSides() throws Exception {
        Fixture fixture = new Fixture();
        CounterPort port = new CounterPort();
        fixture.add("signal", SRC, port);
        DataCompiler compiler = new DataCompiler();
        compiler.declareGlobal("signal", "label(\"signal\").side(\"north\")", 1, 0);
        int[] hits = { 0 };
        NamedRedstoneTrigger trigger = new NamedRedstoneTrigger(
            access(new Label(compiler.compile("signal", 1, 0))),
            false,
            Block.of(ignored -> hits[0]++));
        Program program = new Program(
            "",
            Collections.singletonList(trigger),
            Collections.singleton("signal"),
            ProgramStateSchema.EMPTY,
            true,
            0,
            Collections.emptyList(),
            compiler.getGlobalDeclarations());
        ProgramContext context = context(fixture);
        context.setRedstoneTracker(new RedstoneTriggerTracker());
        program.tick(context);
        port.rising[5]++;
        program.tick(context);
        assertEquals(0, hits[0]);
        port.rising[2]++;
        program.tick(context);
        assertEquals(1, hits[0]);
    }

    @Test
    void changingADynamicSubscriptionRebasesEvenIfAnotherTriggerAlreadyObservedItsPort() throws Exception {
        Fixture fixture = new Fixture();
        CounterPort first = new CounterPort(), second = new CounterPort();
        fixture.add("source", SRC, first);
        fixture.add("dest", DST, second);
        DataCompiler compiler = localCompiler();
        DataCompiler.Binding selected = compiler.declareLocal("selected", DataType.LABEL_REF);
        NamedRedstoneTrigger dynamic = new NamedRedstoneTrigger(
            access(new Label(compiler.compile("selected", 1, 0))),
            false,
            null);
        NamedRedstoneTrigger fixed = new NamedRedstoneTrigger(
            access(
                new Label(
                    LabelReference.of("dest")
                        .side(2))),
            false,
            null);
        List<NamedRedstoneTrigger> triggers = Arrays.asList(dynamic, fixed);
        RedstoneTriggerTracker tracker = new RedstoneTriggerTracker();
        ProgramContext context = context(fixture);
        context.beginDataInvocation(1, false);
        selected.write(
            context.getDataRuntime(),
            DataValue.label(
                LabelReference.of("source")
                    .side(2)));
        tracker.capture(context, triggers, new ca.teamdman.sfml.data.QueryBudget());
        second.rising[2]++;
        selected.write(
            context.getDataRuntime(),
            DataValue.label(
                LabelReference.of("dest")
                    .side(2)));
        java.util.Map<NamedRedstoneTrigger, Boolean> edges = tracker
            .capture(context, triggers, new ca.teamdman.sfml.data.QueryBudget());
        assertEquals(
            Boolean.FALSE,
            edges.get(dynamic),
            "A newly selected subscription starts from the current baseline");
        assertEquals(Boolean.TRUE, edges.get(fixed));
        second.rising[2]++;
        assertEquals(
            Boolean.TRUE,
            tracker.capture(context, triggers, new ca.teamdman.sfml.data.QueryBudget())
                .get(dynamic));
    }

    @Test
    void redstoneSlotHandlesAreRejectedBeforeOutputOrSampling() throws Exception {
        Fixture fixture = new Fixture();
        CounterPort port = new CounterPort();
        fixture.add("signal", SRC, port);
        LabelAccess selected = access(
            new Label("signal"),
            new Label(
                LabelReference.of("signal")
                    .slots(0, 0)));
        assertThrows(
            IllegalArgumentException.class,
            () -> new RedstoneOutputStatement(selected, 7, false).tick(context(fixture)));
        NamedRedstoneTrigger trigger = new NamedRedstoneTrigger(selected, false, null);
        assertThrows(
            IllegalArgumentException.class,
            () -> new RedstoneTriggerTracker().capture(
                context(fixture),
                Collections.singletonList(trigger),
                new ca.teamdman.sfml.data.QueryBudget()));
        assertEquals(0, port.refreshes);
    }

    private static final class PairInventory extends InventoryTile implements ISidedInventory {

        final List<String> extractionPairs = new ArrayList<>(), insertionPairs = new ArrayList<>();

        PairInventory() {
            super(3);
        }

        public int[] getAccessibleSlotsFromSide(int side) {
            return new int[] { 0, 1, 2 };
        }

        public boolean canExtractItem(int slot, ItemStack stack, int side) {
            extractionPairs.add(side + ":" + slot);
            return true;
        }

        public boolean canInsertItem(int slot, ItemStack stack, int side) {
            insertionPairs.add(side + ":" + slot);
            return true;
        }
    }

    private static final class CounterPort extends TileEntityRedstonePort {

        final long[] rising = new long[6];
        int refreshes;

        public boolean isInputInitialized() {
            return true;
        }

        public long getSignalEpoch() {
            return 1;
        }

        public long getRisingCount(int side) {
            return rising[side];
        }

        public long getFallingCount(int side) {
            return 0;
        }

        public boolean isInputAvailable(int side) {
            return true;
        }

        public void refreshInputs(Runnable beforeProviderCall) {
            beforeProviderCall.run();
            refreshes++;
        }
    }
}
