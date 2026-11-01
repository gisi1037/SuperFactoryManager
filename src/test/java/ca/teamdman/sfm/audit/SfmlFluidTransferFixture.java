package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.client.blueprint.StudioTransferArguments;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.FluidIdentity;
import ca.teamdman.sfml.data.ItemTransferResult;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

/** Exercise real SFML parsing, provider callbacks, mB accounting and disk recovery together. */
public final class SfmlFluidTransferFixture {

    public static void registerFixtures() throws Exception {
        ResourceSelectorOreFixture.registerFixtures();
    }

    private static FluidStack water(int amount) {
        return new FluidStack(net.minecraftforge.fluids.FluidRegistry.WATER, amount);
    }

    /** Real registry-backed payloads, independently of the old registry-free tank fixture. */
    private static class TankTile extends AuditRegressionTest.TankTile {

        TankTile(int amount) {
            super(amount);
        }

        @Override
        public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
            if (fluid == null || fluid.getFluid() != net.minecraftforge.fluids.FluidRegistry.WATER) return 0;
            int accepted = Math.min(10000 - amount, fluid.amount);
            if (actual) amount += accepted;
            return accepted;
        }

        @Override
        public FluidStack drain(ForgeDirection side, int requested, boolean actual) {
            int drained = Math.min(requested, amount);
            if (drained <= 0) return null;
            if (actual) amount -= drained;
            return water(drained);
        }

        @Override
        public FluidStack drain(ForgeDirection side, FluidStack fluid, boolean actual) {
            return fluid == null || fluid.getFluid() != net.minecraftforge.fluids.FluidRegistry.WATER ? null
                : drain(side, fluid.amount, actual);
        }

        @Override
        public FluidTankInfo[] getTankInfo(ForgeDirection side) {
            return new FluidTankInfo[] { new FluidTankInfo(amount == 0 ? null : water(amount), 10000) };
        }
    }

    private static final LabelReference SOURCE = LabelReference.literal("source"),
        DEST = LabelReference.literal("dest");
    private static final ResourceSelector FLUIDS = ResourceSelector.fluid("*");
    private static final DataValue ALL = DataValue.selector(FLUIDS);
    private static final String CALL = "transferf(label(\"source\"), label(\"dest\")";

    public static void allFourFormsTransferInMillibuckets() throws Exception {
        String[] suffixes = { ")", ", 700)", ", fluid(\"water\"))", ", fluid(\"*\"), 700)" };
        for (int i = 0; i < suffixes.length; i++) {
            Setup setup = new Setup(new TankTile(2500), new TankTile(0));
            program("EVERY 20 TICKS DO " + CALL + suffixes[i] + " END").tick(setup.context);
            int expected = i % 2 == 0 ? 2500 : 700;
            assertEquals(expected, setup.target.amount, suffixes[i]);
            assertEquals(2500 - expected, setup.source.amount);
            assertFalse(
                setup.context.getManager()
                    .hasPendingRecovery());
        }
    }

    public static void resultRecordsPartialCapacityAndFiniteRequestedStock() throws Exception {
        Setup setup = new Setup(new TankTile(2500), new TankTile(9800));
        Program program = program(
            "STATE $r = {requested:0,moved:0,remaining:0,status:\"\"} " + "EVERY 20 TICKS DO "
                + CALL
                + ") INTO $sent SET $r = $sent END");
        Memory memory = run(program, setup.context);
        assertEquals(
            2500,
            memory.value.get("requested")
                .asLong());
        assertEquals(
            200,
            memory.value.get("moved")
                .asLong());
        assertEquals(
            2300,
            memory.value.get("remaining")
                .asLong());
        assertEquals(
            "partial",
            memory.value.get("status")
                .asString());
        assertEquals(10000, setup.target.amount);
        assertEquals(2300, setup.source.amount);
    }

    public static void queriesCountOnePhysicalTankAndLeaveItemQueriesUnchanged() throws Exception {
        Setup setup = new Setup(new TankTile(2500), new TankTile(0));
        assertEquals(2500, setup.context.fluidCount(SOURCE, FLUIDS, new QueryBudget()));
        assertTrue(setup.context.fluidAvailable(SOURCE, FLUIDS, new QueryBudget()));
        assertFalse(setup.context.fluidEmpty(SOURCE, FLUIDS, new QueryBudget()));
        assertTrue(setup.context.fluidEmpty(DEST, FLUIDS, new QueryBudget()));
        assertEquals(0, setup.context.fluidCount(SOURCE, ResourceSelector.fluid("lava"), new QueryBudget()));
        assertFalse(setup.context.available(SOURCE, ResourceSelector.item("*"), new QueryBudget()));
        assertThrows(IllegalArgumentException.class, () -> setup.context.count(SOURCE, FLUIDS, new QueryBudget()));
        assertThrows(
            IllegalArgumentException.class,
            () -> setup.context.fluidCount(SOURCE, ResourceSelector.item("*"), new QueryBudget()));
    }

    public static void missingGroupIsUnavailableRatherThanEmpty() throws Exception {
        Setup setup = new Setup(new TankTile(2500), new TankTile(0));
        setup.fixture.world.tiles.remove(SRC);
        assertFalse(setup.context.fluidAvailable(SOURCE, FLUIDS, new QueryBudget()));
        assertFalse(setup.context.fluidEmpty(SOURCE, FLUIDS, new QueryBudget()));
        assertThrows(IllegalArgumentException.class, () -> setup.context.fluidCount(SOURCE, FLUIDS, new QueryBudget()));
        assertThrows(
            IllegalArgumentException.class,
            () -> setup.context.fluidContents(SOURCE, FLUIDS, new QueryBudget()));
        ItemTransferResult result = setup.context.transferFluids(SOURCE, DEST, ALL);
        assertEquals(ItemTransferResult.Status.UNAVAILABLE, result.getStatus());
        assertEquals(0, setup.target.amount);
    }

    public static void slotsAreRejectedAndZeroAndNegativeAmountsCallNoProviders() throws Exception {
        Setup setup = new Setup(new TankTile(1000), new TankTile(0));
        assertEquals(
            0,
            setup.context.transferFluids(SOURCE, DEST, ALL, 0)
                .getMoved());
        assertThrows(IllegalArgumentException.class, () -> setup.context.transferFluids(SOURCE, DEST, ALL, -1));
        assertThrows(
            IllegalArgumentException.class,
            () -> setup.context.transferFluids(SOURCE.slots(0, 0), DEST, ALL, 1000));
        assertThrows(
            IllegalArgumentException.class,
            () -> setup.context.fluidCount(SOURCE.slots(0, 0), FLUIDS, new QueryBudget()));
        assertEquals(
            0,
            setup.context.getInvocationQueryBudget()
                .getProviderCalls());
    }

    public static void equalGroupLabelsCannotFeedTheirOwnSources() throws Exception {
        Setup setup = new Setup(new TankTile(1000), new TankTile(0));
        ItemTransferResult result = setup.context.transferFluids(SOURCE, SOURCE, ALL, 500);
        assertEquals(0, result.getMoved());
        assertEquals(1000, setup.source.amount);
    }

    public static void qualifiedSideResolvesAmbiguousTankViews() throws Exception {
        Setup setup = new Setup(new TankTile(1000) {

            @Override
            public FluidTankInfo[] getTankInfo(ForgeDirection side) {
                return new FluidTankInfo[] {
                    new FluidTankInfo(water(side == ForgeDirection.DOWN ? 1000 : 500), 10000) };
            }
        }, new TankTile(0));
        assertFalse(setup.context.fluidAvailable(SOURCE, FLUIDS, new QueryBudget()));
        assertEquals(1000, setup.context.fluidCount(SOURCE.side(0), FLUIDS, new QueryBudget()));
        assertEquals(
            500,
            setup.context.transferFluids(SOURCE.side(0), DEST, ALL, 500)
                .getMoved());
    }

    public static void sidePermissionsAreHonored() throws Exception {
        Setup setup = new Setup(new TankTile(1000) {

            @Override
            public boolean canDrain(ForgeDirection side, net.minecraftforge.fluids.Fluid fluid) {
                return side == ForgeDirection.UP;
            }
        }, new TankTile(0));
        assertEquals(
            0,
            setup.context.transferFluids(SOURCE.side(0), DEST, ALL, 500)
                .getMoved());
        assertEquals(
            500,
            setup.context.transferFluids(SOURCE.side(1), DEST, ALL, 500)
                .getMoved());
    }

    public static void defaultFluidQueriesWorkInFunctionsLoopsAndExpressions() throws Exception {
        Setup setup = new Setup(new TankTile(2500), new TankTile(0));
        Program program = program(
            "STATE $r = 0 " + "FUNCTION observed($label: LABEL_REF) RETURNS INTEGER DO RETURN $label.fcount() END "
                + "EVERY 20 TICKS DO LET $src = label(\"source\") LET $dest = label(\"dest\") "
                + "IF ($src.favailable() AND !$src.fempty() AND $dest.fempty()) THEN "
                + "LET $rows = $src.fcontents() FOR $row IN $rows DO SET $r = $r + $row.amount END "
                + "IF (observed($src) == $r AND len($rows) == 1 AND $rows[0].id == \"water\") THEN "
                + "transferf($src, $dest, $rows[0].fluid, 1000) END END END");
        Memory memory = run(program, setup.context);
        assertEquals(2500, memory.value.asLong());
        assertEquals(1000, setup.target.amount);
    }

    public static void fluidKeyEqualityIncludesNbtAndSnapshotsRemainImmutable() throws Exception {
        FluidStack original = water(1000);
        original.tag = new NBTTagCompound();
        original.tag.setString("batch", "first");
        QueryBudget budget = new QueryBudget();
        FluidIdentity key = FluidIdentity.fromStack(original, budget);
        FluidIdentity equal = FluidIdentity.fromStack(original, budget);
        original.tag.setString("batch", "second");
        assertTrue(key.matches(equal, budget));
        assertFalse(key.matches(original, budget));
        FluidStack copy = key.stack(20, budget);
        copy.tag.setString("batch", "third");
        assertTrue(key.matches(equal, budget));
        assertEquals("first", key.stack(20, budget).tag.getString("batch"));
    }

    public static void exactFluidKeyRejectsDifferentNbt() throws Exception {
        Setup setup = new Setup(new TankTile(1000), new TankTile(0));
        FluidStack tagged = water(1000);
        tagged.tag = new NBTTagCompound();
        tagged.tag.setString("batch", "first");
        DataValue key = DataValue.fluidIdentity(FluidIdentity.fromStack(tagged, new QueryBudget()));
        assertEquals(
            0,
            setup.context.transferFluids(SOURCE, DEST, key, 1000)
                .getMoved());
        assertEquals(1000, setup.source.amount);
    }

    public static void automaticAmountRemainsFiniteWhenSourceRefills() throws Exception {
        Setup setup = new Setup(new TankTile(1000) {

            @Override
            public FluidStack drain(ForgeDirection side, FluidStack fluid, boolean actual) {
                FluidStack result = super.drain(side, fluid, actual);
                if (actual) amount = 1000;
                return result;
            }
        }, new TankTile(0));
        assertEquals(
            1000,
            setup.context.transferFluids(SOURCE, DEST, ALL)
                .getMoved());
        assertEquals(1000, setup.target.amount);
    }

    public static void workExhaustionFailsBeforeExtraction() throws Exception {
        Setup setup = new Setup(new TankTile(1000), new TankTile(0));
        setup.context.getInvocationQueryBudget()
            .chargeAllocation(QueryBudget.MAX_ALLOCATIONS - 3);
        assertThrows(IllegalArgumentException.class, () -> setup.context.transferFluids(SOURCE, DEST, ALL, 1000));
        assertEquals(1000, setup.source.amount);
        assertEquals(0, setup.target.amount);
        assertFalse(
            setup.context.getManager()
                .hasPendingRecovery());
    }

    public static void throwingExtractionPreservesUncertainEvidence() throws Exception {
        for (boolean mutate : new boolean[] { false, true }) {
            Setup setup = new Setup(new TankTile(1000) {

                @Override
                public FluidStack drain(ForgeDirection side, FluidStack fluid, boolean actual) {
                    if (!actual) return super.drain(side, fluid, false);
                    if (mutate) amount -= fluid.amount;
                    throw new IllegalStateException("drain failed");
                }
            }, new TankTile(0));
            assertThrows(IllegalStateException.class, () -> setup.context.transferFluids(SOURCE, DEST, ALL, 1000));
            assertRecord(setup, "uncertain", "extraction", 1000);
            assertEquals(mutate ? 0 : 1000, setup.source.amount);
            assertEquals(0, setup.target.amount);
        }
    }

    public static void throwingInsertionNeverBlindlyRollsBackPossiblyInsertedFluid() throws Exception {
        Setup setup = new Setup(new TankTile(1000), new TankTile(0) {

            @Override
            public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
                if (!actual) return super.fill(side, fluid, false);
                amount += 400;
                throw new IllegalStateException("fill failed");
            }
        });
        assertThrows(IllegalStateException.class, () -> setup.context.transferFluids(SOURCE, DEST, ALL, 1000));
        assertRecord(setup, "uncertain", "destination insertion", 1000);
        assertEquals(0, setup.source.amount);
        assertEquals(400, setup.target.amount);
        assertThrows(IllegalStateException.class, () -> setup.context.transferFluids(SOURCE, DEST, ALL, 1000));
    }

    public static void knownUnderfillReturnsTheRemainder() throws Exception {
        Setup setup = new Setup(new TankTile(1000), new TankTile(0) {

            @Override
            public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
                if (!actual) return fluid.amount;
                amount += 100;
                return 100;
            }
        });
        // One face means one underfilling commit; the rest is returned to the source.
        assertEquals(
            100,
            setup.context.transferFluids(SOURCE.side(0), DEST.side(0), ALL, 1000)
                .getMoved());
        assertEquals(900, setup.source.amount);
        assertEquals(100, setup.target.amount);
        assertFalse(
            setup.context.getManager()
                .hasPendingRecovery());
    }

    public static void rollbackRefusalRetainsKnownOwnedFluid() throws Exception {
        Setup setup = new Setup(new TankTile(1000) {

            @Override
            public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
                return 0;
            }
        }, new TankTile(0) {

            @Override
            public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
                if (!actual) return fluid.amount;
                amount += 100;
                return 100;
            }
        });
        assertThrows(IllegalArgumentException.class, () -> setup.context.transferFluids(SOURCE, DEST, ALL, 1000));
        assertRecord(setup, "pending", "source rollback", 900);
        assertEquals(0, setup.source.amount);
        assertEquals(100, setup.target.amount);
    }

    public static void callbackReplacementDoesNotAuthorizeExtractionFromStaleProvider() throws Exception {
        Setup setup = new Setup(new TankTile(1000), new TankTile(0));
        setup.fixture.world.tiles.put(DST, new TankTile(0) {

            @Override
            public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
                setup.fixture.world.tiles.remove(SRC);
                return fluid.amount;
            }
        });
        assertEquals(
            ItemTransferResult.Status.UNAVAILABLE,
            setup.context.transferFluids(SOURCE, DEST, ALL, 1000)
                .getStatus());
        assertEquals(1000, setup.source.amount);
    }

    public static void simulationDiskReplacementStopsBeforeExtraction() throws Exception {
        Setup setup = new Setup(new TankTile(1000), new TankTile(0));
        setup.fixture.world.tiles.put(DST, new TankTile(0) {

            @Override
            public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
                setup.context.getManager()
                    .setInventorySlotContents(
                        0,
                        setup.context.getManager()
                            .getDisk()
                            .copy());
                return fluid.amount;
            }
        });
        assertThrows(IllegalArgumentException.class, () -> setup.context.transferFluids(SOURCE, DEST, ALL, 1000));
        assertEquals(1000, setup.source.amount);
    }

    public static void itemAndFluidActionsShareOneLimit() throws Exception {
        Setup setup = new Setup(new TankTile(0), new TankTile(0));
        for (int i = 0; i < 32; i++) {
            setup.context.transferFluids(SOURCE, DEST, ALL);
            setup.context.transferItems(SOURCE, DEST, DataValue.selector(ResourceSelector.item("*")));
        }
        int calls = setup.context.getInvocationQueryBudget()
            .getProviderCalls();
        assertThrows(IllegalArgumentException.class, () -> setup.context.transferFluids(SOURCE, DEST, ALL));
        assertEquals(
            calls,
            setup.context.getInvocationQueryBudget()
                .getProviderCalls());
    }

    public static void studioPreservesFluidKeywordAndArgumentSpans() {
        String clause = "transferf( label(\"source\") , label(\"dest\") )";
        StudioTransferArguments arguments = new StudioTransferArguments(clause);
        assertEquals(
            2,
            arguments.rows()
                .size());
        String code = "EVERY 20 TICKS DO " + clause + " END";
        String visual = BlueprintCompiler.generate(BlueprintCompiler.importText(code));
        assertTrue(
            visual.toLowerCase()
                .contains("transferf"));
        program(visual);
    }

    public static void invalidOverloadsAreRejectedAndOldItemFormsStayValid() {
        for (String args : new String[] { ", true)", ", 1000, 1000)", ", item(\"*\"), 1000)" }) {
            ProgramBuildResult build = new ProgramBuilder("EVERY 20 TICKS DO " + CALL + args + " END").build();
            // Selectors share a static type; wrong resource domains are rejected before provider calls at runtime.
            if (!args.contains("item")) assertFalse(build.isSuccess());
        }
        program("EVERY 20 TICKS DO transfer(label(\"source\"), label(\"dest\"), 10) END");
    }

    public static void mixedFluidsAndNbtAreConservedAndAmountIsAggregate() throws Exception {
        Setup setup = new Setup(new TankTile(0), new TankTile(0));
        FluidStack tagged = water(600);
        tagged.tag = new NBTTagCompound();
        tagged.tag.setString("batch", "a");
        ModelTank source = new ModelTank(tagged, new FluidStack(net.minecraftforge.fluids.FluidRegistry.LAVA, 900));
        ModelTank target = new ModelTank(null, null);
        setup.fixture.world.tiles.put(SRC, source);
        setup.fixture.world.tiles.put(DST, target);
        ItemTransferResult result = setup.context.transferFluids(SOURCE, DEST, ALL, 1000);
        assertEquals(1000, result.getMoved());
        assertEquals(600, target.stacks[0].amount);
        assertEquals("a", target.stacks[0].tag.getString("batch"));
        assertEquals(400, target.stacks[1].amount);
        assertEquals(500, source.stacks[1].amount);
        assertNull(source.stacks[0]);
        assertEquals(500, setup.context.fluidCount(SOURCE, FLUIDS, new QueryBudget()));
        assertEquals(1000, setup.context.fluidCount(DEST, FLUIDS, new QueryBudget()));
    }

    public static void capturedFluidKeyTransfersOnlyTheExactNbtVariant() throws Exception {
        Setup setup = new Setup(new TankTile(0), new TankTile(0));
        FluidStack first = water(600), second = water(900);
        first.tag = new NBTTagCompound();
        first.tag.setString("batch", "a");
        second.tag = new NBTTagCompound();
        second.tag.setString("batch", "b");
        ModelTank source = new ModelTank(first, second), target = new ModelTank(null, null);
        setup.fixture.world.tiles.put(SRC, source);
        setup.fixture.world.tiles.put(DST, target);
        program(
            "EVERY 20 TICKS DO LET $src = label(\"source\") LET $rows = $src.fcontents() "
                + "IF ($rows[0].fluid == $rows[0].fluid AND $rows[0].fluid != $rows[1].fluid) THEN "
                + "transferf($src, label(\"dest\"), $rows[0].fluid) END END").tick(setup.context);
        assertEquals(600, target.stacks[0].amount);
        assertNull(target.stacks[1]);
        assertNull(source.stacks[0]);
        assertEquals(900, source.stacks[1].amount);
    }

    public static void fullSourceGroupMustBeAvailableAndUnloadedTilesAreNeverRead() throws Exception {
        Setup setup = new Setup(new TankTile(1000), new TankTile(0));
        ca.teamdman.sfm.common.util.BlockPos missing = new ca.teamdman.sfm.common.util.BlockPos(2, 64, 0);
        setup.fixture.labels.addPosition("source", missing);
        setup.fixture.world.unloaded.add(missing);
        assertFalse(setup.context.fluidEmpty(SOURCE, FLUIDS, new QueryBudget()));
        assertFalse(setup.context.fluidAvailable(SOURCE, FLUIDS, new QueryBudget()));
        assertEquals(
            ItemTransferResult.Status.UNAVAILABLE,
            setup.context.transferFluids(SOURCE, DEST, ALL, 1000)
                .getStatus());
        assertEquals(1000, setup.source.amount);
        assertEquals(0, setup.target.amount);
    }

    public static void providerBudgetConsumptionCannotSpendCommitRecoveryCredit() throws Exception {
        Setup setup = new Setup(new TankTile(1000), new TankTile(0));
        setup.fixture.world.tiles.put(SRC, new TankTile(1000) {

            @Override
            public FluidStack drain(ForgeDirection side, FluidStack fluid, boolean actual) {
                FluidStack result = super.drain(side, fluid, actual);
                if (actual) {
                    QueryBudget owner = setup.context.getInvocationQueryBudget();
                    owner.chargeAllocation(QueryBudget.MAX_ALLOCATIONS - owner.getAllocations());
                    owner.chargeSlots(QueryBudget.MAX_SLOT_WORK - owner.getSlotWork());
                    owner.chargeProviders(QueryBudget.MAX_PROVIDER_CALLS - owner.getProviderCalls());
                    owner.chargePositions(QueryBudget.MAX_POSITIONS - owner.getPositions());
                }
                return result;
            }
        });
        assertEquals(
            1000,
            setup.context.transferFluids(SOURCE.side(0), DEST.side(0), ALL, 1000)
                .getMoved());
        assertEquals(1000, setup.target.amount);
        assertFalse(
            setup.context.getManager()
                .hasPendingRecovery());
    }

    public static void nonblockingReadinessFunctionAcceptsExplicitLabelParameters() throws Exception {
        Setup setup = new Setup(new TankTile(1000), new TankTile(0));
        Program code = program(
            "STATE $r = false "
                + "FUNCTION ready($key: INTEGER, $plasma: LABEL_REF, $coolant: LABEL_REF) RETURNS BOOLEAN DO "
                + "IF ($key == 1) THEN RETURN $plasma.fempty() END "
                + "IF ($key == 2) THEN RETURN $coolant.fempty() END RETURN false END "
                + "EVERY 20 TICKS DO SET $r = !ready(1, label(\"source\"), label(\"dest\")) "
                + "AND ready(2, label(\"source\"), label(\"dest\")) "
                + "AND !ready(3, label(\"source\"), label(\"dest\")) END");
        assertTrue(run(code, setup.context).value.asBoolean());
    }

    public static void journalPreventsReentrantTransfersDuringActualDrain() throws Exception {
        Setup setup = new Setup(new TankTile(1000), new TankTile(0));
        setup.fixture.world.tiles.put(SRC, new TankTile(1000) {

            @Override
            public FluidStack drain(ForgeDirection side, FluidStack fluid, boolean actual) {
                if (actual) {
                    assertTrue(
                        setup.context.getManager()
                            .hasPendingRecovery());
                    assertThrows(
                        IllegalStateException.class,
                        () -> setup.context.transferFluids(SOURCE, DEST, ALL, 1000));
                }
                return super.drain(side, fluid, actual);
            }
        });
        assertEquals(
            1000,
            setup.context.transferFluids(SOURCE, DEST, ALL, 1000)
                .getMoved());
        assertEquals(1000, setup.target.amount);
        assertFalse(
            setup.context.getManager()
                .hasPendingRecovery());
    }

    public static void diskReplacementAfterExtractionRetainsEvidenceOnOriginalDisk() throws Exception {
        Setup setup = new Setup(new TankTile(1000), new TankTile(0));
        net.minecraft.item.ItemStack original = setup.context.getManager()
            .getDisk();
        setup.fixture.world.tiles.put(SRC, new TankTile(1000) {

            @Override
            public FluidStack drain(ForgeDirection side, FluidStack fluid, boolean actual) {
                FluidStack result = super.drain(side, fluid, actual);
                if (actual) setup.context.getManager()
                    .setInventorySlotContents(0, original.copy());
                return result;
            }
        });
        assertThrows(IllegalArgumentException.class, () -> setup.context.transferFluids(SOURCE, DEST, ALL, 1000));
        NBTTagCompound evidence = original.getTagCompound()
            .getCompoundTag("sfm:recovery_fluid");
        assertEquals("uncertain", evidence.getString("state"));
        assertEquals(1000, evidence.getInteger("Amount"));
        assertEquals(0, setup.target.amount);
        assertFalse(
            setup.context.getManager()
                .isRunning());
    }

    public static void throwingRollbackRetainsOnlyTheUncertainRemainder() throws Exception {
        Setup setup = new Setup(new TankTile(1000) {

            @Override
            public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
                if (!actual) return fluid.amount;
                amount += 20;
                throw new IllegalStateException("rollback failed");
            }
        }, new TankTile(0) {

            @Override
            public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
                if (!actual) return fluid.amount;
                amount += 100;
                return 100;
            }
        });
        assertThrows(IllegalStateException.class, () -> setup.context.transferFluids(SOURCE, DEST, ALL, 1000));
        assertRecord(setup, "uncertain", "source rollback", 900);
        assertEquals(20, setup.source.amount);
        assertEquals(100, setup.target.amount);
        setup.context.getManager()
            .updateEntity();
        assertEquals(20, setup.source.amount);
        assertRecord(setup, "uncertain", "source rollback", 900);
    }

    private static final class ModelTank extends net.minecraft.tileentity.TileEntity
        implements net.minecraftforge.fluids.IFluidHandler {

        final FluidStack[] stacks;

        ModelTank(FluidStack first, FluidStack second) {
            stacks = new FluidStack[] { first, second };
        }

        @Override
        public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
            for (int i = 0; i < stacks.length; i++) {
                FluidStack current = stacks[i];
                if (current != null && !current.isFluidEqual(fluid)) continue;
                int accepted = Math.min(fluid.amount, 10000 - (current == null ? 0 : current.amount));
                if (actual && accepted > 0) {
                    if (current == null) {
                        current = fluid.copy();
                        current.amount = 0;
                        stacks[i] = current;
                    }
                    current.amount += accepted;
                }
                return accepted;
            }
            return 0;
        }

        @Override
        public FluidStack drain(ForgeDirection side, FluidStack fluid, boolean actual) {
            for (int i = 0; i < stacks.length; i++) {
                FluidStack current = stacks[i];
                if (current == null || !current.isFluidEqual(fluid)) continue;
                FluidStack result = current.copy();
                result.amount = Math.min(fluid.amount, current.amount);
                if (actual) {
                    current.amount -= result.amount;
                    if (current.amount == 0) stacks[i] = null;
                }
                return result;
            }
            return null;
        }

        @Override
        public FluidStack drain(ForgeDirection side, int amount, boolean actual) {
            for (FluidStack current : stacks) if (current != null) {
                FluidStack request = current.copy();
                request.amount = amount;
                return drain(side, request, actual);
            }
            return null;
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
            return new FluidTankInfo[] { new FluidTankInfo(stacks[0], 10000), new FluidTankInfo(stacks[1], 10000) };
        }
    }

    private static void assertRecord(Setup setup, String state, String stage, int amount) {
        TileEntityManager manager = setup.context.getManager();
        assertTrue(manager.hasPendingRecovery());
        NBTTagCompound record = manager.getDisk()
            .getTagCompound()
            .getCompoundTag("sfm:recovery_fluid");
        assertEquals(state, record.getString("state"));
        assertEquals(stage, record.getString("stage"));
        assertEquals(amount, record.getInteger("Amount"));
        assertFalse(manager.isRunning());
    }

    private static Program program(String source) {
        ProgramBuildResult result = new ProgramBuilder(source).build();
        assertTrue(result.isSuccess(), result.getErrorMessage());
        return result.getProgram();
    }

    private static Memory run(Program program, ProgramContext context) {
        Memory memory = new Memory();
        memory.value = program.getStateSchema()
            .getDeclarations()
            .get(0)
            .getDefaultValue();
        context.setStateAccess(memory);
        program.tick(context);
        return memory;
    }

    private static final class Memory implements StateAccess {

        DataValue value;

        @Override
        public DataValue read(int slot) {
            return value;
        }

        @Override
        public void write(int slot, DataValue next) {
            value = next;
        }
    }

    private static final class Setup {

        final Fixture fixture;
        final TankTile source, target;
        final ProgramContext context;

        Setup(TankTile source, TankTile target) throws Exception {
            fixture = new Fixture();
            for (int x = -2; x <= 4; x++)
                for (int y = 62; y <= 66; y++) for (int z = -2; z <= 2; z++) fixture.world.blocks
                    .put(new ca.teamdman.sfm.common.util.BlockPos(x, y, z), net.minecraft.init.Blocks.air);
            this.source = source;
            this.target = target;
            fixture.world.tiles.put(SRC, source);
            fixture.world.tiles.put(DST, target);
            TileEntityManager manager = RecoveryFixtures.manager(fixture);
            source.setWorldObj(fixture.world);
            target.setWorldObj(fixture.world);
            context = new ProgramContext(
                fixture.world,
                manager,
                new CableNetwork(fixture.world, MANAGER),
                fixture.labels,
                20,
                false);
        }
    }
}
