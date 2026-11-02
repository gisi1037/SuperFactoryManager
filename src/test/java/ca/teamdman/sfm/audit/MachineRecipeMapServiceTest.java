package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import net.minecraft.tileentity.TileEntity;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.recipe.MachineRecipeMapService;
import ca.teamdman.sfm.common.recipe.MachineRecipeMapService.Access;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataQueries;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.MachineRecipeMaps;
import ca.teamdman.sfml.data.MachineRecipeMaps.Status;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

class MachineRecipeMapServiceTest {

    private static final LabelReference SOURCE = LabelReference.literal("source");
    private static final String MAP = "gt.recipe.macerator";

    @Test
    void observesFreshMachineEachTimeWithoutCopyingRecipesOrMutatingInventories() throws Exception {
        Setup setup = new Setup();
        setup.fixture.stock(0, 64);
        MachineRecipeMaps first = setup.inspect();
        assertEquals(Status.MATCHES, first.status);
        assertEquals(MAP, first.current);
        assertEquals(java.util.Collections.singletonList(MAP), first.available);
        assertThrows(UnsupportedOperationException.class, () -> first.available.add("other"));
        setup.target.map = "gt.recipe.metalbender";
        assertEquals(setup.target.map, setup.inspect().current);
        assertEquals(MAP, first.current, "An earlier observation remains immutable");
        assertEquals(2, setup.lookups.get());
        assertEquals(64, count(setup.fixture.source));
    }

    @Test
    void unloadedMissingDisconnectedOrInvalidMachineDoesNotReachAdapter() throws Exception {
        Setup setup = new Setup();
        setup.fixture.world.unloaded.add(SRC);
        assertEquals(Status.UNAVAILABLE, setup.inspect().status);
        setup.fixture.world.unloaded.clear();
        setup.fixture.world.tiles.remove(SRC);
        assertEquals(Status.UNAVAILABLE, setup.inspect().status);
        setup.fixture.world.tiles.put(SRC, setup.fixture.source);
        setup.fixture.source.invalidate();
        assertEquals(Status.UNAVAILABLE, setup.inspect().status);
        setup.fixture.add("remote", new ca.teamdman.sfm.common.util.BlockPos(100, 64, 0), new InventoryTile(1));
        assertEquals(
            Status.UNAVAILABLE,
            setup.service.inspect(LabelReference.literal("remote"), new QueryBudget()).status);
        assertEquals(
            Status.UNAVAILABLE,
            setup.service.inspect(LabelReference.literal("missing"), new QueryBudget()).status);
        assertEquals(0, setup.lookups.get());
    }

    @Test
    void multipleMachinesAreAmbiguousEvenWhenTheyCouldUseTheSameMap() throws Exception {
        Setup setup = new Setup();
        setup.fixture.labels.addPosition("source", DST);
        assertEquals(Status.AMBIGUOUS, setup.inspect().status);
        assertEquals(0, setup.lookups.get());
        assertEquals(
            Status.UNSUPPORTED,
            new MachineRecipeMapService(setup.context, (tile, budget) -> null, () -> 1)
                .inspect(LabelReference.literal("dest"), new QueryBudget()).status);
    }

    @Test
    void sideOrSlotSelectionsAreRejectedBeforeTouchingProviders() throws Exception {
        Setup setup = new Setup();
        QueryBudget budget = new QueryBudget();
        assertThrows(IllegalArgumentException.class, () -> setup.service.inspect(SOURCE.side(0), budget));
        assertThrows(IllegalArgumentException.class, () -> setup.service.inspect(SOURCE.slots(0, 0), budget));
        assertEquals(0, budget.getPositions());
        assertEquals(0, budget.getProviderCalls());
        assertEquals(0, setup.lookups.get());
    }

    @Test
    void sessionRestartAndInvalidationDuringCallbacksCannotPublishOldAssociation() throws Exception {
        Setup setup = new Setup();
        setup.epoch.set(-1);
        assertEquals(Status.NOT_READY, setup.inspect().status);
        assertEquals(0, setup.lookups.get());
        setup.epoch.set(1);
        setup.onFind = () -> setup.epoch.incrementAndGet();
        assertEquals(Status.STALE, setup.inspect().status);
        setup.onFind = () -> {};
        setup.target.onCurrent = () -> setup.epoch.incrementAndGet();
        assertEquals(Status.STALE, setup.inspect().status);
    }

    @Test
    void providerOrManagerReplacementDuringCallbacksRefusesResult() throws Exception {
        Setup setup = new Setup();
        setup.onFind = () -> setup.fixture.world.tiles.put(SRC, new InventoryTile(1));
        assertEquals(Status.UNAVAILABLE, setup.inspect().status);
        setup.fixture.world.tiles.put(SRC, setup.fixture.source);
        setup.onFind = () -> {};
        setup.target.onCurrent = () -> setup.manager.setInventorySlotContents(
            0,
            setup.manager.getDisk()
                .copy());
        assertEquals(Status.UNAVAILABLE, setup.inspect().status);
    }

    @Test
    void changedModeOrUnloadedProviderDuringFinalReadCannotReportMatches() throws Exception {
        Setup setup = new Setup();
        setup.target.valid = false;
        assertEquals(Status.UNAVAILABLE, setup.inspect().status);
        setup.target.valid = true;
        setup.target.onCurrent = () -> setup.fixture.world.unloaded.add(SRC);
        assertEquals(Status.UNAVAILABLE, setup.inspect().status);
    }

    @Test
    void nativeFailuresAndExhaustedBudgetsAreNotConvertedToSuccessfulOrPartialViews() throws Exception {
        Setup setup = new Setup();
        setup.onFind = () -> { throw new IllegalStateException("native callback"); };
        assertThrows(IllegalStateException.class, setup::inspect);
        QueryBudget budget = new QueryBudget();
        budget.chargePositions(budget.getPositionLimit());
        int lookups = setup.lookups.get();
        assertThrows(IllegalArgumentException.class, () -> setup.service.inspect(SOURCE, budget));
        assertEquals(lookups, setup.lookups.get());
    }

    @Test
    void compilerAndRuntimeExposeTypedBoundedViewAndRespectUserOverrides() throws Exception {
        Setup setup = new Setup();
        QueryBudget owner = new QueryBudget();
        ProgramContext provider = new ProgramContext(null, null, null, null, 0, false);
        DataValue unsupported = new DataRuntime(null, 0, provider, owner).recipeMaps(SOURCE);
        assertEquals(
            "unavailable",
            unsupported.get("status")
                .asString());
        assertEquals(
            0,
            unsupported.get("available")
                .size());
        DataCompiler compiler = new DataCompiler();
        DataValue result = compiler.compile("recipemaps(label(\"source\")).status", 1, 0)
            .evaluate(new DataRuntime(null, 0, provider, new QueryBudget()));
        assertEquals("unavailable", result.asString());
        QueryBudget shared = new QueryBudget();
        DataQueries live = new DataQueries() {

            @Override
            public MachineRecipeMaps recipeMaps(LabelReference source, QueryBudget budget) {
                assertSame(shared, budget, "Compiler and native service must share the invocation budget");
                return setup.service.inspect(source, budget);
            }

            @Override
            public long count(LabelReference source, ResourceSelector selector, QueryBudget budget) {
                throw new AssertionError("Map inspection cannot scan item slots");
            }

            @Override
            public boolean empty(LabelReference source, ResourceSelector selector, QueryBudget budget) {
                throw new AssertionError("Map inspection cannot scan item slots");
            }

            @Override
            public boolean available(LabelReference source, ResourceSelector selector, QueryBudget budget) {
                throw new AssertionError("Map inspection cannot scan item slots");
            }
        };
        DataValue maps = compiler.compile("recipemaps(label(\"source\"))", 1, 0)
            .evaluate(new DataRuntime(null, 0, live, shared));
        assertEquals(
            "matches",
            maps.get("status")
                .asString());
        assertEquals(
            MAP,
            maps.get("current")
                .asString());
        assertEquals(
            MAP,
            maps.get("available")
                .get(0)
                .asString());
        assertTrue(shared.getProviderCalls() > 0);
        assertThrows(IllegalArgumentException.class, () -> compiler.compile("recipemaps()", 1, 0));
        assertThrows(IllegalArgumentException.class, () -> compiler.compile("recipemaps(\"source\")", 1, 0));
        assertFalse(
            new ProgramBuilder("state maps = recipemaps(label(\"source\"))").build()
                .isSuccess());
        assertTrue(
            new ProgramBuilder("integer recipemaps(integer n) { return n } every 20 ticks { let n = recipemaps(7) }")
                .build()
                .isSuccess());
        assertEquals(Status.MATCHES, setup.inspect().status);
        assertTrue(owner.getSlotWork() > 0);
    }

    private static final class Target implements Access {

        String map = MAP;
        boolean valid = true;
        Runnable onCurrent = () -> {};

        @Override
        public MachineRecipeMaps maps() {
            return MachineRecipeMaps.matches(map);
        }

        @Override
        public boolean current(TileEntity tile, QueryBudget budget) {
            onCurrent.run();
            return valid;
        }
    }

    private static final class Setup {

        final Fixture fixture = new Fixture();
        final TileEntityManager manager = RecoveryFixtures.manager(fixture);
        final ProgramContext context = new ProgramContext(
            fixture.world,
            manager,
            new CableNetwork(fixture.world, MANAGER),
            fixture.labels,
            20,
            false);
        final Target target = new Target();
        final AtomicLong epoch = new AtomicLong(1);
        final AtomicInteger lookups = new AtomicInteger();
        Runnable onFind = () -> {};
        final MachineRecipeMapService service = new MachineRecipeMapService(context, (tile, budget) -> {
            lookups.incrementAndGet();
            onFind.run();
            return target;
        }, epoch::get);

        Setup() throws Exception {}

        MachineRecipeMaps inspect() {
            return service.inspect(SOURCE, new QueryBudget());
        }
    }
}
