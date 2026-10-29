package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.ast.InputStatement;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.ast.Statement;

/** Repeated INPUT executions own independent quotas; FORGET releases their invocation-local caches. */
class LoopTransferTest {

    private static final String[] INPUT_CACHES = { "inputCache", "inputPositions", "fluidInputQuotas",
        "fluidReservations", "reservedFluids" };

    private static ProgramContext context(Fixture fixture) {
        return new ProgramContext(
            fixture.world,
            null,
            new CableNetwork(fixture.world, MANAGER),
            fixture.labels,
            20,
            false);
    }

    private static List<Statement> statements(String body) {
        return parse("EVERY 20 TICKS DO " + body + " END").getTriggers()
            .get(0)
            .getBlock()
            .getStatements();
    }

    @SuppressWarnings("unchecked")
    private static Map<InputStatement, ?> cache(ProgramContext context, String name) throws Exception {
        Field field = ProgramContext.class.getDeclaredField(name);
        field.setAccessible(true);
        return (Map<InputStatement, ?>) field.get(context);
    }

    @SuppressWarnings("unchecked")
    private static List<InputStatement> inputs(ProgramContext context) throws Exception {
        Field field = ProgramContext.class.getDeclaredField("inputs");
        field.setAccessible(true);
        return (List<InputStatement>) field.get(context);
    }

    @Test
    void countedArrayAndWhileLoopsGiveLiteralAndDynamicInputsIndependentExecutionQuotas() throws Exception {
        String[] loops = { "FOR $i = 1 TO 3 DO %s OUTPUT 1 TO dest END",
            "FOR $i IN [1, 2, 3] DO %s OUTPUT 1 TO dest END",
            "LET $i = 1 WHILE ($i <= 3) DO %s OUTPUT 1 TO dest SET $i = $i + 1 END" };
        for (String loop : loops) for (String quantity : new String[] { "2", "($quantity)" }) {
            Fixture fixture = new Fixture();
            fixture.stock(0, 30);
            String body = "LET $quantity = 2 " + String.format(loop, "INPUT " + quantity + " FROM source")
                + " OUTPUT 100 TO dest";
            fixture.run(body);
            assertEquals(6, count(fixture.dest), body);
            assertEquals(24, count(fixture.source), body);
        }
    }

    @Test
    void anInputRegisteredBeforeTheLoopKeepsOneCapturedQuotaAcrossAllOutputIterations() throws Exception {
        for (String quantity : new String[] { "5", "($quantity)" }) {
            Fixture fixture = new Fixture();
            fixture.stock(0, 30);
            fixture.run(
                "LET $quantity = 5 INPUT " + quantity
                    + " FROM source FOR $i = 1 TO 3 DO OUTPUT 2 TO dest END OUTPUT 100 TO dest");
            assertEquals(5, count(fixture.dest));
            assertEquals(25, count(fixture.source));
        }
    }

    @Test
    void loopItemInputsKeepEachSourceQuotaAndRetentionWhileConservingResources() throws Exception {
        Fixture fixture = new Fixture();
        fixture.stock(0, 20);
        InventoryTile second = new InventoryTile(1);
        second.setInventorySlotContents(0, new ItemStack(ITEM, 20));
        fixture.add("second", EXTRA, second);
        fixture.run(
            "FOR $i = 1 TO 3 DO INPUT 4 RETAIN 10 FROM EACH source, second OUTPUT 1 TO dest END OUTPUT 100 TO dest");
        assertEquals(20, count(fixture.dest));
        assertEquals(10, count(fixture.source));
        assertEquals(10, count(second));
        assertEquals(40, count(fixture.source) + count(second) + count(fixture.dest));
    }

    @Test
    void loopFluidInputsKeepEachTankQuotaAndRetentionWhileConservingResources() throws Exception {
        Fixture fixture = new Fixture();
        TankTile source = new TankTile(5000), second = new TankTile(5000), destination = new TankTile(0);
        fixture.world.tiles.put(SRC, source);
        fixture.world.tiles.put(DST, destination);
        fixture.add("second", EXTRA, second);
        fixture.run(
            "FOR $i = 1 TO 3 DO INPUT 500 RETAIN 4000 fluid:water FROM EACH source, second "
                + "OUTPUT 100 fluid:water TO dest END OUTPUT 10000 fluid:water TO dest");
        assertEquals(2000, destination.amount);
        assertEquals(4000, source.amount);
        assertEquals(4000, second.amount);
        assertEquals(10000, source.amount + second.amount + destination.amount);
    }

    @Test
    void freshLoopRegistrationsShareRoundRobinCursorButDiscoverOnlyWhenOutputNeedsThem() throws Exception {
        Fixture fixture = new Fixture();
        fixture.stock(0, 20);
        InventoryTile second = new InventoryTile(1);
        second.setInventorySlotContents(0, new ItemStack(ITEM, 20));
        fixture.add("second", EXTRA, second);
        List<Statement> body = statements("INPUT 1 FROM source, second ROUND ROBIN BY LABEL OUTPUT 100 TO dest");
        InputStatement input = (InputStatement) body.get(0);
        ProgramContext context = context(fixture);
        context.beginDataInvocation(0, false);
        context.enterLoop();
        try {
            input.tick(context);
            input.tick(context);
        } finally {
            context.exitLoop();
        }
        List<InputStatement> registrations = inputs(context);
        assertEquals(2, registrations.size());
        assertNotSame(registrations.get(0), registrations.get(1));
        assertSame(
            input.getLabelAccess(),
            registrations.get(0)
                .getLabelAccess());
        assertSame(
            input.getLabelAccess(),
            registrations.get(1)
                .getLabelAccess());
        assertTrue(cache(context, "inputPositions").isEmpty(), "INPUT registration must not advance lazy discovery");
        body.get(1)
            .tick(context);
        assertEquals(19, count(fixture.source));
        assertEquals(19, count(second));
        body.get(1)
            .tick(context);
        assertEquals(2, count(fixture.dest), "Cached OUTPUT cannot renew exhausted registration quotas");
        context.enterLoop();
        try {
            input.tick(context);
        } finally {
            context.exitLoop();
        }
        body.get(1)
            .tick(context);
        assertEquals(18, count(fixture.source), "The third discovery continues the shared round-robin cursor");
        assertEquals(19, count(second));
        assertEquals(3, count(fixture.dest));
    }

    @Test
    void forgetCannotRenewTheCumulativeInputAllowanceAndFaultKeepsEarlierTransfersAndUnwindsLoopDepth()
        throws Exception {
        Fixture fixture = new Fixture();
        InventoryTile source = new InventoryTile(10), destination = new InventoryTile(10);
        for (int slot = 0; slot < 10; slot++) source.setInventorySlotContents(slot, new ItemStack(ITEM, 64));
        fixture.world.tiles.put(SRC, source);
        fixture.world.tiles.put(DST, destination);
        Program program = parse(
            "EVERY 20 TICKS DO FOR $i = 1 TO 257 DO INPUT 1 FROM source OUTPUT 1 TO dest FORGET END END");
        ProgramContext context = context(fixture);
        IllegalArgumentException fault = assertThrows(IllegalArgumentException.class, () -> program.tick(context));
        assertTrue(
            fault.getMessage()
                .contains("input registration budget"),
            fault.getMessage());
        assertEquals(256, count(destination));
        assertEquals(384, count(source));
        assertFalse(context.isInLoop());
        assertTrue(inputs(context).isEmpty());
        for (String name : INPUT_CACHES) assertTrue(cache(context, name).isEmpty(), name);
    }

    @Test
    void partialAndFullForgetRemoveAllOldCacheKeysWithoutRenewingSurvivingItemOrFluidQuotas() throws Exception {
        Fixture fixture = new Fixture();
        HybridTile source = new HybridTile(20, 20), second = new HybridTile(20, 20), destination = new HybridTile(0, 0);
        fixture.world.tiles.put(SRC, source);
        fixture.world.tiles.put(DST, destination);
        fixture.add("second", EXTRA, second);
        List<Statement> body = statements(
            "INPUT 5 RETAIN 3 FROM source, second OUTPUT 2 TO dest "
                + "FORGET source OUTPUT 100 TO dest FORGET second OUTPUT 100 TO dest");
        ProgramContext context = context(fixture);
        context.beginDataInvocation(0, false);
        context.enterLoop();
        try {
            body.get(0)
                .tick(context);
        } finally {
            context.exitLoop();
        }
        body.get(1)
            .tick(context);
        InputStatement old = inputs(context).get(0);
        for (String name : INPUT_CACHES) assertEquals(Collections.singleton(old), cache(context, name).keySet(), name);
        assertEquals(2, count(destination));
        assertEquals(2, destination.tank.amount);
        body.get(2)
            .tick(context);
        InputStatement replacement = inputs(context).get(0);
        assertNotSame(old, replacement);
        for (String name : INPUT_CACHES) assertEquals(
            Collections.singleton(replacement),
            cache(context, name).keySet(),
            name + " retained an orphan key");
        body.get(3)
            .tick(context);
        assertEquals(5, count(destination), "Partial FORGET must preserve the already spent item quota");
        assertEquals(5, destination.tank.amount, "Partial FORGET must preserve the already spent fluid quota");
        assertEquals(18, count(source));
        assertEquals(18, source.tank.amount);
        assertEquals(17, count(second));
        assertEquals(17, second.tank.amount);
        body.get(4)
            .tick(context);
        assertTrue(inputs(context).isEmpty());
        for (String name : INPUT_CACHES) assertTrue(cache(context, name).isEmpty(), name + " retained an orphan key");
        body.get(5)
            .tick(context);
        assertEquals(5, count(destination));
        assertEquals(5, destination.tank.amount);
        assertEquals(40, count(source) + count(second) + count(destination));
        assertEquals(40, source.tank.amount + second.tank.amount + destination.tank.amount);
    }

    /** A real dual-domain provider materializes all item and fluid invocation caches through ordinary OUTPUT. */
    private static final class HybridTile extends InventoryTile implements IFluidHandler {

        final TankTile tank;

        HybridTile(int items, int fluid) {
            super(1);
            tank = new TankTile(fluid);
            if (items > 0) setInventorySlotContents(0, new ItemStack(ITEM, items));
        }

        public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
            return tank.fill(side, fluid, actual);
        }

        public FluidStack drain(ForgeDirection side, FluidStack fluid, boolean actual) {
            return tank.drain(side, fluid, actual);
        }

        public FluidStack drain(ForgeDirection side, int amount, boolean actual) {
            return tank.drain(side, amount, actual);
        }

        public boolean canFill(ForgeDirection side, Fluid fluid) {
            return tank.canFill(side, fluid);
        }

        public boolean canDrain(ForgeDirection side, Fluid fluid) {
            return tank.canDrain(side, fluid);
        }

        public FluidTankInfo[] getTankInfo(ForgeDirection side) {
            return tank.getTankInfo(side);
        }
    }
}
