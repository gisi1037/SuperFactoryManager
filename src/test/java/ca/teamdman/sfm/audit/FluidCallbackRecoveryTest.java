package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.data.SfmlRuntimeException;

/** Throwing providers may mutate before failing; preserve evidence without inventing ownership. */
class FluidCallbackRecoveryTest {

    private static final String CODE = "EVERY 20 TICKS DO INPUT fluid:water FROM \"source\" OUTPUT 1000 fluid:water TO \"dest\" END";

    @Test
    void oversizedLegacyJournalRefusesBeforeAnyActualFluidCallbackAndFlushesCurrentState() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(f);
        ItemStack owner = manager.getDisk();
        ca.teamdman.sfml.data.ProgramStateSchema schema = new ca.teamdman.sfml.data.ProgramStateSchema(
            java.util.Collections.singletonList(
                new ca.teamdman.sfml.data.ProgramStateSchema.Declaration(
                    "count",
                    ca.teamdman.sfml.data.DataValue.integer(0))));
        ca.teamdman.sfm.common.program.ProgramStateStore state = ca.teamdman.sfm.common.program.ProgramStateStore
            .activate(owner, schema);
        java.lang.reflect.Field stateField = TileEntityManager.class.getDeclaredField("programState");
        stateField.setAccessible(true);
        stateField.set(manager, state);
        state.flush();
        padDisk(owner, 29900);
        NBTTagCompound root = owner.getTagCompound();
        NBTTagCompound before = (NBTTagCompound) root.copy();
        state.write(0, ca.teamdman.sfml.data.DataValue.integer(77));
        int[] actual = { 0 };
        TankTile source = new TankTile(1000) {

            @Override
            public FluidStack drain(ForgeDirection side, int amount, boolean action) {
                if (action) actual[0]++;
                FluidStack fluid = super.drain(side, amount, action);
                if (fluid != null) {
                    fluid.tag = new NBTTagCompound();
                    fluid.tag.setString("batch", String.join("", java.util.Collections.nCopies(500, "x")));
                }
                return fluid;
            }
        };
        TankTile target = new TankTile(0) {

            @Override
            public int fill(ForgeDirection side, FluidStack fluid, boolean action) {
                if (action) actual[0]++;
                return super.fill(side, fluid, action);
            }
        };
        f.world.tiles.put(SRC, source);
        f.world.tiles.put(DST, target);
        SfmlRuntimeException refused = assertThrows(
            SfmlRuntimeException.class,
            () -> parse(CODE)
                .tick(new ProgramContext(f.world, manager, new CableNetwork(f.world, MANAGER), f.labels, 20, false)));
        assertTrue(
            refused.getCause()
                .getMessage()
                .contains("Complete disk NBT"));
        assertEquals(0, actual[0]);
        assertEquals(1000, source.amount);
        assertEquals(0, target.amount);
        assertSame(root, owner.getTagCompound());
        assertFalse(manager.hasPendingRecovery());
        before.removeTag(ca.teamdman.sfm.common.program.ProgramStateStore.NBT_KEY);
        NBTTagCompound after = (NBTTagCompound) root.copy();
        after.removeTag(ca.teamdman.sfm.common.program.ProgramStateStore.NBT_KEY);
        assertEquals(before, after);
        assertEquals(
            77,
            ca.teamdman.sfm.common.program.ProgramStateStore.activate(owner, schema)
                .read(0)
                .asLong());
        ca.teamdman.sfm.common.program.ProgramStateStore.validateCompleteDisk(owner);
    }

    @Test
    void nearLimitCompleteJournalReservesTargetFieldsAndLaterStageGrowth() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(f);
        ItemStack owner = manager.getDisk();
        padDisk(owner, 29400);
        NBTTagCompound root = owner.getTagCompound();
        FluidStack fluid = water(1000);
        fluid.tag = new NBTTagCompound();
        fluid.tag.setString("batch", String.join("", java.util.Collections.nCopies(100, "x")));
        NBTTagCompound entry = manager
            .journalFluidTransfer(fluid, SRC, ForgeDirection.NORTH, DST, ForgeDirection.SOUTH);
        assertSame(root, owner.getTagCompound());
        assertEquals(DST.x, entry.getInteger("targetX"));
        assertEquals(ForgeDirection.SOUTH.ordinal(), entry.getInteger("targetSide"));
        for (String stage : new String[] { "extraction", "destination insertion", "source rollback",
            "rollback conflict", "automatic source return" }) {
            entry.setString("stage", stage);
            ca.teamdman.sfm.common.program.ProgramStateStore.validateCompleteDisk(owner);
        }
    }

    @Test
    void emergencyEvidenceForAlreadyOwnedFluidIsNeverDiscardedByThePreflightLimit() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(f);
        ItemStack owner = manager.getDisk();
        padDisk(owner, 29900);
        FluidStack owned = water(1000);
        owned.tag = new NBTTagCompound();
        owned.tag.setString("batch", String.join("", java.util.Collections.nCopies(500, "x")));
        NBTTagCompound entry = manager.preserveFluid(owned, SRC, ForgeDirection.NORTH, true, "provider failure");
        assertSame(
            entry,
            owner.getTagCompound()
                .getTag("sfm:recovery_fluid"));
        assertEquals(1000, entry.getInteger("Amount"));
        assertEquals(owned.tag, entry.getCompoundTag("Tag"));
        assertTrue(manager.hasPendingRecovery());
        assertFalse(manager.isRunning());
    }

    static void padDisk(ItemStack disk, int bytes) throws Exception {
        if (!disk.hasTagCompound()) disk.setTagCompound(new NBTTagCompound());
        disk.getTagCompound()
            .setString("padding", "");
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        net.minecraft.nbt.CompressedStreamTools.write(disk.getTagCompound(), new java.io.DataOutputStream(output));
        disk.getTagCompound()
            .setString("padding", String.join("", java.util.Collections.nCopies(bytes - output.size(), "a")));
        ca.teamdman.sfm.common.program.ProgramStateStore.validateCompleteDisk(disk);
    }

    @Test
    void legacyCallbacksCannotMoveRecoveryEvidenceOntoAReplacementDisk() throws Exception {
        for (boolean replaceOnDrain : new boolean[] { true, false })
            for (boolean throwAfterMutation : new boolean[] { true, false }) {
                Fixture f = new Fixture();
                TileEntityManager manager = RecoveryFixtures.manager(f);
                ItemStack owner = manager.getDisk(), replacement = owner.copy();
                boolean[] observedJournal = { false };
                Runnable replace = () -> {
                    observedJournal[0] = manager.hasPendingRecovery();
                    manager.setInventorySlotContents(0, replacement);
                };
                TankTile source = new TankTile(1000) {

                    @Override
                    public FluidStack drain(ForgeDirection side, FluidStack fluid, boolean actual) {
                        FluidStack drained = super.drain(side, fluid, actual);
                        if (actual && replaceOnDrain) {
                            replace.run();
                            if (throwAfterMutation) throw new IllegalStateException("drain mutated then replaced disk");
                        }
                        return drained;
                    }
                };
                TankTile target = new TankTile(0) {

                    @Override
                    public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
                        int inserted = super.fill(side, fluid, actual);
                        if (actual && !replaceOnDrain) {
                            replace.run();
                            if (throwAfterMutation) throw new IllegalStateException("fill mutated then replaced disk");
                        }
                        return inserted;
                    }
                };
                f.world.tiles.put(SRC, source);
                f.world.tiles.put(DST, target);
                assertThrows(
                    SfmlRuntimeException.class,
                    () -> parse(CODE).tick(
                        new ProgramContext(f.world, manager, new CableNetwork(f.world, MANAGER), f.labels, 20, false)));
                assertTrue(observedJournal[0], "Actual provider callbacks need write-ahead ownership evidence");
                assertSame(replacement, manager.getDisk());
                assertFalse(
                    replacement.hasTagCompound() && replacement.getTagCompound()
                        .hasKey("sfm:recovery_fluid"));
                NBTTagCompound record = owner.getTagCompound()
                    .getCompoundTag("sfm:recovery_fluid");
                assertEquals("uncertain", record.getString("state"));
                assertEquals(1000, record.getInteger("Amount"));
                assertTrue(record.getBoolean("recoveryBlocked"));
                assertEquals(0, source.amount);
                assertEquals(replaceOnDrain ? 0 : 1000, target.amount);
                assertFalse(manager.isRunning());
            }
    }

    @Test
    void legacyActualDrainAndFillFenceReentrantManualRuns() throws Exception {
        for (boolean reenterOnDrain : new boolean[] { true, false }) {
            Fixture f = new Fixture();
            TileEntityManager manager = RecoveryFixtures.manager(f);
            ca.teamdman.sfm.common.item.ItemDisk.setProgramString(manager.getDisk(), CODE);
            ca.teamdman.sfm.common.label.LabelPositionHolder.saveTo(manager.getDisk(), f.labels);
            manager.setInventorySlotContents(0, manager.getDisk());
            int[] calls = { 0 };
            Runnable nested = () -> {
                calls[0]++;
                if (calls[0] == 1) manager.runOnceWithLogLevel(ca.teamdman.sfm.common.tileentity.LogLevel.OFF);
            };
            TankTile source = new TankTile(1000) {

                @Override
                public FluidStack drain(ForgeDirection side, FluidStack fluid, boolean actual) {
                    if (actual && reenterOnDrain) nested.run();
                    return super.drain(side, fluid, actual);
                }
            };
            TankTile target = new TankTile(0) {

                @Override
                public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
                    if (actual && !reenterOnDrain) nested.run();
                    return super.fill(side, fluid, actual);
                }
            };
            f.world.tiles.put(SRC, source);
            f.world.tiles.put(DST, target);
            parse(CODE)
                .tick(new ProgramContext(f.world, manager, new CableNetwork(f.world, MANAGER), f.labels, 20, false));
            assertEquals(1, calls[0]);
            assertEquals(0, source.amount);
            assertEquals(1000, target.amount);
            assertFalse(manager.hasPendingRecovery());
            assertFalse(manager.isRunning(), "A blocked reentrant manual run pauses the Manager");
        }
    }

    @Test
    void legacyDrainFencesAbaDiskAndPhysicalManagerReplacement() throws Exception {
        for (boolean aba : new boolean[] { true, false }) {
            Fixture f = new Fixture();
            TileEntityManager manager = RecoveryFixtures.manager(f);
            ItemStack owner = manager.getDisk();
            long generation = manager.getDiskGeneration();
            TankTile source = new TankTile(1000) {

                @Override
                public FluidStack drain(ForgeDirection side, FluidStack fluid, boolean actual) {
                    FluidStack extracted = super.drain(side, fluid, actual);
                    if (actual) {
                        if (aba) {
                            manager.setInventorySlotContents(0, owner.copy());
                            manager.setInventorySlotContents(0, owner);
                        } else f.world.tiles.put(MANAGER, new TileEntityManager());
                    }
                    return extracted;
                }
            };
            TankTile target = new TankTile(0);
            f.world.tiles.put(SRC, source);
            f.world.tiles.put(DST, target);
            assertThrows(
                SfmlRuntimeException.class,
                () -> parse(CODE).tick(
                    new ProgramContext(f.world, manager, new CableNetwork(f.world, MANAGER), f.labels, 20, false)));
            assertSame(owner, manager.getDisk());
            if (aba) assertTrue(manager.getDiskGeneration() > generation);
            assertEquals(
                "uncertain",
                owner.getTagCompound()
                    .getCompoundTag("sfm:recovery_fluid")
                    .getString("state"));
            assertEquals(0, source.amount);
            assertEquals(0, target.amount);
            assertFalse(manager.isRunning());
        }
    }

    @Test
    void legacyRollbackReplacementRetainsOnlyItsUncertainRemainderOnTheOriginalDisk() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(f);
        ItemStack owner = manager.getDisk(), replacement = owner.copy();
        TankTile source = new TankTile(1000) {

            @Override
            public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
                if (!actual) return fluid.amount;
                manager.setInventorySlotContents(0, replacement);
                amount += 20;
                throw new IllegalStateException("rollback mutated then replaced disk");
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
        f.world.tiles.put(SRC, source);
        f.world.tiles.put(DST, target);
        assertThrows(
            SfmlRuntimeException.class,
            () -> parse(CODE)
                .tick(new ProgramContext(f.world, manager, new CableNetwork(f.world, MANAGER), f.labels, 20, false)));
        assertSame(replacement, manager.getDisk());
        assertFalse(
            replacement.hasTagCompound() && replacement.getTagCompound()
                .hasKey("sfm:recovery_fluid"));
        NBTTagCompound record = owner.getTagCompound()
            .getCompoundTag("sfm:recovery_fluid");
        assertEquals("uncertain", record.getString("state"));
        assertEquals("source rollback", record.getString("stage"));
        assertEquals(990, record.getInteger("Amount"));
        assertEquals(20, source.amount);
        assertEquals(10, target.amount);
    }

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

    void automaticReturnCannotClearReplacementOrReplayRemovedDisk() throws Exception {
        Fixture fixture = automaticFixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        NBTTagCompound original = manager.preserveFluid(water(1000), SRC, ForgeDirection.NORTH, false, "test");
        ItemStack owner = manager.getDisk();
        ItemStack replacement = owner.copy();
        replacement.getTagCompound()
            .getCompoundTag("sfm:recovery_fluid")
            .setString("recoveryId", "replacement");
        NBTTagCompound replacementBefore = (NBTTagCompound) replacement.writeToNBT(new NBTTagCompound())
            .copy();
        RecoveryTank source = new RecoveryTank(0) {

            @Override
            public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
                assertTrue(actual);
                manager.setInventorySlotContents(0, replacement);
                return super.fill(side, fluid, true);
            }
        };
        fixture.world.tiles.put(SRC, source);
        manager.updateEntity();
        assertEquals(replacementBefore, replacement.writeToNBT(new NBTTagCompound()));
        assertEquals("uncertain", original.getString("state"));
        assertEquals(1000, original.getInteger("Amount"));
        manager.setInventorySlotContents(0, owner);
        manager.updateEntity();
        assertEquals(1000, source.amount);
        assertTrue(manager.hasPendingRecovery());
        assertFalse(manager.isRunning());
    }

    void automaticReturnFencesAbaDiskReplacement() throws Exception {
        Fixture fixture = automaticFixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        NBTTagCompound original = manager.preserveFluid(water(1000), SRC, ForgeDirection.NORTH, false, "test");
        ItemStack owner = manager.getDisk();
        long generation = manager.getDiskGeneration();
        fixture.world.tiles.put(SRC, new RecoveryTank(0) {

            @Override
            public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
                manager.setInventorySlotContents(0, owner.copy());
                manager.setInventorySlotContents(0, owner);
                return super.fill(side, fluid, actual);
            }
        });
        manager.updateEntity();
        assertSame(owner, manager.getDisk());
        assertTrue(manager.getDiskGeneration() > generation);
        assertEquals("uncertain", original.getString("state"));
        assertTrue(manager.hasPendingRecovery());
        manager.updateEntity();
        assertEquals(1000, ((TankTile) fixture.world.tiles.get(SRC)).amount);
    }

    void automaticReturnJournalsBeforeReentrantManualExecution() throws Exception {
        Fixture fixture = automaticFixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        manager.preserveFluid(water(1000), SRC, ForgeDirection.NORTH, false, "test");
        int[] calls = { 0 };
        RecoveryTank source = new RecoveryTank(0) {

            @Override
            public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
                if (++calls[0] == 1) manager.runOnceWithLogLevel(ca.teamdman.sfm.common.tileentity.LogLevel.OFF);
                return super.fill(side, fluid, actual);
            }
        };
        fixture.world.tiles.put(SRC, source);
        manager.updateEntity();
        assertEquals(1, calls[0]);
        assertEquals(1000, source.amount);
        assertFalse(manager.hasPendingRecovery());
        assertFalse(manager.isRunning());
    }

    void saveInsideThrowingAutomaticReturnRetainsUncertaintyAfterReload() throws Exception {
        for (boolean mutate : new boolean[] { false, true }) {
            Fixture fixture = automaticFixture();
            TileEntityManager manager = RecoveryFixtures.manager(fixture);
            NBTTagCompound original = manager.preserveFluid(water(1000), SRC, ForgeDirection.NORTH, false, "test");
            NBTTagCompound savedInside = new NBTTagCompound();
            RecoveryTank source = new RecoveryTank(0) {

                @Override
                public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
                    manager.writeToNBT(savedInside);
                    savedInside.setTag(
                        "disk",
                        savedInside.getCompoundTag("disk")
                            .copy());
                    if (mutate) amount += 400;
                    throw new IllegalStateException("automatic return failed");
                }
            };
            fixture.world.tiles.put(SRC, source);
            manager.updateEntity();
            assertEquals("uncertain", original.getString("state"));
            assertEquals("automatic source return", original.getString("stage"));
            TileEntityManager restored = new TileEntityManager();
            restored.setWorldObj(fixture.world);
            restored.readFromNBT(savedInside);
            fixture.world.tiles.put(MANAGER, restored);
            assertEquals(
                "uncertain",
                restored.getDisk()
                    .getTagCompound()
                    .getCompoundTag("sfm:recovery_fluid")
                    .getString("state"));
            restored.updateEntity();
            assertEquals(mutate ? 400 : 0, source.amount);
        }
    }

    void confirmedAutomaticReturnRestoresOnlyKnownRemainderAcrossSaveReload() throws Exception {
        for (int accepted : new int[] { 0, 400, 1000 }) {
            Fixture fixture = automaticFixture();
            TileEntityManager manager = RecoveryFixtures.manager(fixture);
            NBTTagCompound original = manager.preserveFluid(water(1000), SRC, ForgeDirection.NORTH, false, "test");
            NBTTagCompound savedInside = new NBTTagCompound();
            RecoveryTank source = new RecoveryTank(0) {

                @Override
                public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
                    manager.writeToNBT(savedInside);
                    savedInside.setTag(
                        "disk",
                        savedInside.getCompoundTag("disk")
                            .copy());
                    amount += accepted;
                    return accepted;
                }
            };
            fixture.world.tiles.put(SRC, source);
            manager.updateEntity();
            assertEquals(
                "uncertain",
                savedInside.getCompoundTag("disk")
                    .getCompoundTag("tag")
                    .getCompoundTag("sfm:recovery_fluid")
                    .getString("state"));
            assertEquals(accepted, source.amount);
            assertEquals(accepted < 1000, manager.hasPendingRecovery());
            if (accepted < 1000) {
                assertEquals("pending", original.getString("state"));
                assertFalse(original.getBoolean("recoveryBlocked"));
                assertEquals("test", original.getString("stage"));
                assertEquals(1000 - accepted, original.getInteger("Amount"));
            }
            NBTTagCompound savedAfter = new NBTTagCompound();
            manager.writeToNBT(savedAfter);
            TileEntityManager restored = new TileEntityManager();
            restored.setWorldObj(fixture.world);
            restored.readFromNBT(savedAfter);
            fixture.world.tiles.put(MANAGER, restored);
            assertEquals(accepted < 1000, restored.hasPendingRecovery());
            fixture.world.tiles.put(SRC, new RecoveryTank(source.amount));
            restored.updateEntity();
            assertFalse(restored.hasPendingRecovery());
            assertEquals(1000, ((TankTile) fixture.world.tiles.get(SRC)).amount);
        }
    }

    void automaticReturnRejectsOwnerChangedDuringHandlerLookup() throws Exception {
        Fixture fixture = automaticFixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        NBTTagCompound original = manager.preserveFluid(water(1000), SRC, ForgeDirection.NORTH, false, "test");
        NBTTagCompound before = (NBTTagCompound) original.copy();
        ItemStack replacement = manager.getDisk()
            .copy();
        NBTTagCompound replacementBefore = (NBTTagCompound) replacement.writeToNBT(new NBTTagCompound())
            .copy();
        RecoveryTank source = new RecoveryTank(0);
        fixture.world.tiles.put(SRC, source);
        java.util.Map<ca.teamdman.sfm.common.util.BlockPos, net.minecraft.tileentity.TileEntity> previous = fixture.world.tiles;
        fixture.world.tiles = new java.util.HashMap<ca.teamdman.sfm.common.util.BlockPos, net.minecraft.tileentity.TileEntity>(
            previous) {

            private boolean changed;

            @Override
            public net.minecraft.tileentity.TileEntity get(Object key) {
                if (!changed && SRC.equals(key)) {
                    changed = true;
                    manager.setInventorySlotContents(0, replacement);
                }
                return super.get(key);
            }
        };
        manager.updateEntity();
        assertEquals(0, source.amount);
        assertEquals(before, original);
        assertEquals(replacementBefore, replacement.writeToNBT(new NBTTagCompound()));
    }

    void automaticReturnCannotClearReplacedRecordOnSameDisk() throws Exception {
        Fixture fixture = automaticFixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        NBTTagCompound original = manager.preserveFluid(water(1000), SRC, ForgeDirection.NORTH, false, "test");
        ItemStack owner = manager.getDisk();
        long generation = manager.getDiskGeneration();
        NBTTagCompound[] replaced = { null };
        RecoveryTank source = new RecoveryTank(0) {

            @Override
            public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
                NBTTagCompound copy = (NBTTagCompound) original.copy();
                owner.getTagCompound()
                    .setTag("sfm:recovery_fluid", copy);
                replaced[0] = (NBTTagCompound) copy.copy();
                return super.fill(side, fluid, actual);
            }
        };
        fixture.world.tiles.put(SRC, source);
        manager.updateEntity();
        assertSame(owner, manager.getDisk());
        assertEquals(generation, manager.getDiskGeneration());
        assertEquals(
            replaced[0],
            owner.getTagCompound()
                .getCompoundTag("sfm:recovery_fluid"));
        assertEquals("uncertain", original.getString("state"));
        manager.updateEntity();
        assertEquals(1000, source.amount);
    }

    /** Real-registry decoding yields Forge WATER rather than the headless TestFluid identity. */
    private static class RecoveryTank extends TankTile {

        RecoveryTank(int amount) {
            super(amount);
        }

        @Override
        public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
            if (fluid == null || !"water".equals(
                fluid.getFluid()
                    .getName()))
                return 0;
            int accepted = Math.min(10000 - amount, fluid.amount);
            if (actual) amount += accepted;
            return accepted;
        }
    }

    private static Fixture automaticFixture() throws Exception {
        Fixture fixture = new Fixture();
        // Real vanilla bootstrap enables comparator notifications from TileEntity.markDirty.
        fixture.world.blocks.put(MANAGER, net.minecraft.init.Blocks.air);
        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
            fixture.world.blocks.put(MANAGER.offset(side), net.minecraft.init.Blocks.air);
        return fixture;
    }

    private static TileEntityManager runFailure(Fixture fixture, TankTile source, TankTile target) throws Exception {
        fixture.world.tiles.put(SRC, source);
        fixture.world.tiles.put(DST, target);
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        SfmlRuntimeException failure = assertThrows(
            SfmlRuntimeException.class,
            () -> parse(CODE).tick(
                new ProgramContext(
                    fixture.world,
                    manager,
                    new CableNetwork(fixture.world, MANAGER),
                    fixture.labels,
                    20,
                    false)));
        assertInstanceOf(IllegalStateException.class, failure.getCause());
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
