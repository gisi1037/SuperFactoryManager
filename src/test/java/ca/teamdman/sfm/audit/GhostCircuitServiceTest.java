package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.tileentity.TileEntity;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.GhostCircuitAccess;
import ca.teamdman.sfm.common.program.GhostCircuitService;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.compat.gregtech.GregTechCircuitAdapter;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

/** Native action contract tests; full-pack GT machine qualification is a separate check. */
class GhostCircuitServiceTest {

    private static final LabelReference SOURCE = LabelReference.literal("source");

    @Test
    void setsClearsAndReportsAlreadySelectedWithoutTouchingRealItems() throws Exception {
        Setup setup = new Setup();
        setup.fixture.stock(0, 16);
        GhostCircuitService.Result result = setup.service.set(SOURCE, 7);
        assertEquals("complete", result.status);
        assertEquals(1, result.changed);
        assertEquals(1, result.matched);
        assertEquals(7, setup.source.configuration);
        assertEquals(0, setup.service.set(SOURCE, 7).changed);
        assertEquals(1, setup.service.set(SOURCE, 0).changed);
        assertEquals(-1, setup.source.configuration);
        assertEquals(0, setup.service.set(SOURCE, -1).changed);
        assertEquals(16, count(setup.fixture.source));
    }

    @Test
    void unsupportedOrUnavailableGroupPreflightNeverPartiallyWrites() throws Exception {
        Setup setup = new Setup();
        setup.fixture.labels.addPosition("machines", SRC);
        setup.fixture.labels.addPosition("machines", DST);
        LabelReference machines = LabelReference.literal("machines");
        setup.targets.remove(setup.fixture.dest);
        assertEquals("unsupported", setup.service.set(machines, 2).status);
        assertEquals(-1, setup.source.configuration);
        setup.targets.put(setup.fixture.dest, setup.target);
        setup.fixture.world.unloaded.add(DST);
        assertEquals("unavailable", setup.service.set(machines, 2).status);
        assertEquals(-1, setup.source.configuration);
    }

    @Test
    void appliesToEachDistinctMachineInAGroup() throws Exception {
        Setup setup = new Setup();
        setup.fixture.labels.addPosition("machines", SRC);
        setup.fixture.labels.addPosition("machines", DST);
        GhostCircuitService.Result result = setup.service.set(LabelReference.literal("machines"), 24);
        assertEquals(2, result.matched);
        assertEquals(2, result.changed);
        assertEquals(24, setup.source.configuration);
        assertEquals(24, setup.target.configuration);
    }

    @Test
    void invalidNumbersAndPhysicalAccessRestrictionsFaultBeforeProviders() throws Exception {
        Setup setup = new Setup();
        for (long number : new long[] { -2, 25, Long.MAX_VALUE })
            assertThrows(IllegalArgumentException.class, () -> setup.context.setCircuit(SOURCE, number));
        assertThrows(IllegalArgumentException.class, () -> setup.context.setCircuit(SOURCE.slots(0, 1), 1));
        assertThrows(IllegalArgumentException.class, () -> setup.context.setCircuit(SOURCE.side(0), 1));
        assertEquals(
            0,
            setup.context.getInvocationQueryBudget()
                .getProviderCalls());
    }

    @Test
    void replacedProviderIsNotWrittenAfterItsReadCallback() throws Exception {
        Setup setup = new Setup();
        setup.source.onGet = () -> setup.fixture.world.tiles.put(SRC, new InventoryTile(3));
        assertEquals("unavailable", setup.service.set(SOURCE, 4).status);
        assertEquals(-1, setup.source.configuration);
    }

    @Test
    void changedDiskAfterWriteCannotReportComplete() throws Exception {
        Setup setup = new Setup();
        setup.source.onSet = () -> setup.manager.setInventorySlotContents(
            0,
            setup.manager.getDisk()
                .copy());
        GhostCircuitService.Result result = setup.service.set(SOURCE, 4);
        assertEquals("partial", result.status);
        assertEquals(4, setup.source.configuration);
    }

    @Test
    void throwingOrRefusedWritesAreNotReportedAsSuccess() throws Exception {
        Setup setup = new Setup();
        setup.source.onSet = () -> { throw new IllegalStateException("setter failed"); };
        assertThrows(IllegalStateException.class, () -> setup.service.set(SOURCE, 4));
        setup.source.onSet = () -> setup.source.configuration = -1;
        assertEquals("partial", setup.service.set(SOURCE, 5).status);
    }

    @Test
    void exhaustedBudgetDoesNotBeginMutation() throws Exception {
        Setup setup = new Setup();
        QueryBudget budget = setup.context.getInvocationQueryBudget();
        budget.chargeAllocation(QueryBudget.MAX_ALLOCATIONS - 6);
        assertThrows(IllegalArgumentException.class, () -> setup.service.set(SOURCE, 7));
        assertEquals(-1, setup.source.configuration);
    }

    @Test
    void optionalGregTechAbsenceIsUnsupportedAndConsumesTheSharedActionLimit() throws Exception {
        Setup setup = new Setup();
        assertNull(GregTechCircuitAdapter.find(setup.fixture.source, new QueryBudget()));
        for (int i = 0; i < 64; i++) assertEquals("unsupported", setup.context.setCircuit(SOURCE, 1).status);
        int calls = setup.context.getInvocationQueryBudget()
            .getProviderCalls();
        assertThrows(IllegalArgumentException.class, () -> setup.context.setCircuit(SOURCE, 1));
        assertEquals(
            calls,
            setup.context.getInvocationQueryBudget()
                .getProviderCalls());
    }

    @Test
    void actualCompilerSupportsResultCaptureAndPreservesTheActionBoundary() throws Exception {
        ProgramBuildResult built = new ProgramBuilder(
            "STATE $status = \"\" EVERY 20 TICKS DO "
                + "setcircuit(label(\"source\"), 3) INTO $set $status = $set.status END").build();
        assertTrue(built.isSuccess(), built.getErrorMessage());
        Program program = built.getProgram();
        DataValue[] value = { DataValue.string("") };
        Setup setup = new Setup();
        setup.context.setStateAccess(new StateAccess() {

            @Override
            public DataValue read(int slot) {
                return value[slot];
            }

            @Override
            public void write(int slot, DataValue next) {
                value[slot] = next;
            }
        });
        program.tick(setup.context);
        assertEquals("unsupported", value[0].asString());
        assertTrue(
            new ProgramBuilder(program.toString()).build()
                .isSuccess());
        assertFalse(
            new ProgramBuilder("EVERY 20 TICKS DO LET $n = setcircuit(label(\"source\"), 3) END").build()
                .isSuccess());
        assertFalse(
            new ProgramBuilder("FUNCTION set() RETURNS INTEGER DO setcircuit(label(\"source\"), 3) RETURN 0 END")
                .build()
                .isSuccess());
    }

    private static final class Target implements GhostCircuitAccess {

        final TileEntity tile;
        int configuration = -1;
        Runnable onGet = () -> {}, onSet = () -> {};

        Target(TileEntity tile) {
            this.tile = tile;
        }

        @Override
        public boolean current(TileEntity actual, QueryBudget budget) {
            return actual == tile;
        }

        @Override
        public int get(QueryBudget budget) {
            onGet.run();
            return configuration;
        }

        @Override
        public void set(int value, QueryBudget budget) {
            configuration = value;
            onSet.run();
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
        final Target source = new Target(fixture.source), target = new Target(fixture.dest);
        final Map<TileEntity, Target> targets = new IdentityHashMap<>();
        final GhostCircuitService service;

        Setup() throws Exception {
            targets.put(fixture.source, source);
            targets.put(fixture.dest, target);
            service = new GhostCircuitService(context, (tile, budget) -> targets.get(tile));
        }
    }
}
