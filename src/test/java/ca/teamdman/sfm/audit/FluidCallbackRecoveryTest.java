package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

/** Throwing providers may mutate before failing; preserve evidence without inventing ownership. */
class FluidCallbackRecoveryTest {

    private static final String CODE = "EVERY 20 TICKS DO INPUT fluid:water FROM source OUTPUT 1000 fluid:water TO dest END";

    @Test
    void drainFailureBeforeOrAfterMutationIsQuarantined() throws Exception {
        for (boolean mutate : new boolean[] { false, true }) {
            Fixture fixture = new Fixture();
            TankTile source = new TankTile(1000) {

                @Override
                public FluidStack drain(ForgeDirection side, FluidStack fluid, boolean actual) {
                    if (!actual) return super.drain(side, fluid, false);
                    if (mutate) amount -= fluid.amount;
                    throw new IllegalStateException("drain failed");
                }
            };
            TileEntityManager manager = runFailure(fixture, source, new TankTile(0));
            assertRecord(manager, "extraction", 1000);
            assertEquals(mutate ? 0 : 1000, source.amount);
        }
    }

    @Test
    void insertionFailureNeverBlindlyReturnsPossiblyInsertedFluid() throws Exception {
        for (boolean mutate : new boolean[] { false, true }) {
            Fixture fixture = new Fixture();
            TankTile source = new TankTile(1000);
            TankTile target = new TankTile(0) {

                @Override
                public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
                    if (!actual) return fluid.amount;
                    if (mutate) amount += 400;
                    throw new IllegalStateException("fill failed");
                }
            };
            TileEntityManager manager = runFailure(fixture, source, target);
            NBTTagCompound record = assertRecord(manager, "destination insertion", 1000);
            assertEquals(0, source.amount);
            assertEquals(mutate ? 400 : 0, target.amount);
            int logs = manager.getLogs()
                .size();
            record.setBoolean("recoveryBlocked", false); // State itself must also prevent replay.
            for (int i = 0; i < 3; i++) {
                manager.setRunning(true);
                manager.updateEntity();
                manager.runOnceWithLogLevel(ca.teamdman.sfm.common.tileentity.LogLevel.OFF);
            }
            assertFalse(manager.isRunning());
            assertEquals(0, source.amount);
            assertEquals(mutate ? 400 : 0, target.amount);
            assertEquals(
                logs,
                manager.getLogs()
                    .size());
            NBTTagCompound saved = new NBTTagCompound();
            manager.writeToNBT(saved);
            TileEntityManager restored = new TileEntityManager();
            restored.setWorldObj(fixture.world);
            restored.readFromNBT(saved);
            assertRecord(restored, "destination insertion", 1000);
            restored.updateEntity();
            assertEquals(0, source.amount);
        }
    }

    @Test
    void throwingRollbackRecordsOnlyTheKnownRemainderAndDoesNotReplayIt() throws Exception {
        Fixture fixture = new Fixture();
        TankTile source = new TankTile(1000) {

            @Override
            public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
                if (!actual) return fluid.amount;
                amount += 20;
                throw new IllegalStateException("rollback failed");
            }
        };
        TankTile target = new TankTile(0) {

            @Override
            public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
                if (!actual) return fluid.amount;
                amount += 10;
                return 10;
            }
        };
        TileEntityManager manager = runFailure(fixture, source, target);
        assertRecord(manager, "source rollback", 990);
        manager.updateEntity();
        assertEquals(20, source.amount);
        assertEquals(10, target.amount);
    }

    @Test
    void unknownStateIsRetainedWithoutCallingProviders() throws Exception {
        Fixture fixture = new Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        NBTTagCompound record = manager.preserveFluid(water(100), SRC, ForgeDirection.NORTH, false, "test");
        record.setString("state", "future");
        manager.updateEntity();
        assertTrue(manager.hasPendingRecovery());
        assertEquals("future", record.getString("state"));
        assertFalse(manager.isRunning());
    }

    private static TileEntityManager runFailure(Fixture fixture, TankTile source, TankTile target) throws Exception {
        fixture.world.tiles.put(SRC, source);
        fixture.world.tiles.put(DST, target);
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        assertThrows(
            IllegalStateException.class,
            () -> parse(CODE).tick(
                new ProgramContext(
                    fixture.world,
                    manager,
                    new CableNetwork(fixture.world, MANAGER),
                    fixture.labels,
                    20,
                    false)));
        assertFalse(manager.isRunning());
        return manager;
    }

    private static NBTTagCompound assertRecord(TileEntityManager manager, String stage, int amount) {
        NBTTagCompound record = manager.getStackInSlot(0)
            .getTagCompound()
            .getCompoundTag("sfm:recovery_fluid");
        assertEquals("uncertain", record.getString("state"));
        assertEquals(stage, record.getString("stage"));
        assertEquals(amount, record.getInteger("Amount"));
        assertEquals(DST.x, record.getInteger("targetX"));
        assertTrue(manager.hasPendingRecovery());
        return record;
    }
}
