package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.program.ProgramStateStore;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

class ManagerDataLifecycleTest {

    private static final String COUNTER = "STATE $count = 0 EVERY 1 TICKS DO SET $count = $count + 1 END";

    private static TileEntityManager counter(AuditRegressionTest.Fixture fixture) throws Exception {
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        manager.saveCodeAndRecompile(COUNTER);
        assertTrue(manager.hasCompiledProgram(), manager.getStatusMessage());
        return manager;
    }

    @Test
    void liveStateStaysInHeapUntilSaveAndSurvivesRealManagerNbtReload() throws Exception {
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        TileEntityManager manager = counter(fixture);
        NBTBase initial = manager.getDisk()
            .getTagCompound()
            .getTag(ProgramStateStore.NBT_KEY);
        for (int i = 0; i < 100; i++) manager.updateEntity();
        assertEquals(
            100,
            manager.getProgramState()
                .read(0)
                .asLong());
        assertTrue(
            manager.getProgramState()
                .isDirty());
        assertSame(
            initial,
            manager.getDisk()
                .getTagCompound()
                .getTag(ProgramStateStore.NBT_KEY));
        NBTTagCompound saved = new NBTTagCompound();
        manager.writeToNBT(saved);
        assertFalse(
            manager.getProgramState()
                .isDirty());
        NBTBase snapshot = manager.getDisk()
            .getTagCompound()
            .getTag(ProgramStateStore.NBT_KEY);
        assertNotSame(initial, snapshot);
        manager.writeToNBT(new NBTTagCompound());
        assertSame(
            snapshot,
            manager.getDisk()
                .getTagCompound()
                .getTag(ProgramStateStore.NBT_KEY));
        TileEntityManager loaded = new TileEntityManager();
        loaded.setWorldObj(fixture.world);
        loaded.readFromNBT(saved);
        assertTrue(loaded.hasCompiledProgram(), loaded.getStatusMessage());
        assertEquals(
            100,
            loaded.getProgramState()
                .read(0)
                .asLong());
        loaded.updateEntity();
        assertEquals(
            101,
            loaded.getProgramState()
                .read(0)
                .asLong());
    }

    @Test
    void removingAndMovingDiskPublishesHeapStateBeforeDetachment() throws Exception {
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        TileEntityManager manager = counter(fixture);
        manager.updateEntity();
        manager.updateEntity();
        ItemStack moved = manager.decrStackSize(0, 1);
        assertNull(manager.getProgramState());
        assertNull(manager.getDisk());
        TileEntityManager target = RecoveryFixtures.manager(new AuditRegressionTest.Fixture());
        target.setInventorySlotContents(0, moved);
        assertEquals(
            2,
            target.getProgramState()
                .read(0)
                .asLong());
        target.updateEntity();
        assertEquals(
            3,
            target.getProgramState()
                .read(0)
                .asLong());
    }

    @Test
    void inventoryExposureCopiesCurrentValuesAndCodeEditsReconcileFromHeap() throws Exception {
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        TileEntityManager manager = counter(fixture);
        manager.updateEntity();
        ItemStack copy = manager.getStackInSlot(0)
            .copy();
        assertFalse(
            manager.getProgramState()
                .isDirty());
        assertEquals(
            1,
            ProgramStateStore.activate(
                copy,
                manager.getCompiledProgram()
                    .getStateSchema())
                .read(0)
                .asLong());
        manager.updateEntity();
        manager.saveCodeAndRecompile("-- layout/comment edit\n" + COUNTER.replace("= 0", "= 99"));
        assertEquals(
            2,
            manager.getProgramState()
                .read(0)
                .asLong());
        manager.updateEntity();
        assertEquals(
            3,
            manager.getProgramState()
                .read(0)
                .asLong());
        // Raw source changes still snapshot accepted old-schema writes before rebinding.
        ItemDisk.setProgramString(manager.getDisk(), COUNTER.replace("= 0", "= 10"));
        manager.updateEntity();
        assertEquals(
            4,
            manager.getProgramState()
                .read(0)
                .asLong());
    }

    @Test
    void incompatibleAndCorruptStatePauseWithoutOverwritingPayloadAndExplicitResetKeepsCodeAndLabels()
        throws Exception {
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        TileEntityManager manager = counter(fixture);
        LabelPositionHolder labels = new LabelPositionHolder();
        labels.addPosition("kept", AuditRegressionTest.SRC);
        LabelPositionHolder.saveTo(manager.getDisk(), labels);
        manager.updateEntity();
        String changed = "STATE $count = [1, 2] EVERY 1 TICKS DO END";
        manager.saveCodeAndRecompile(changed);
        assertFalse(manager.hasCompiledProgram());
        assertFalse(manager.isRunning());
        NBTTagCompound prior = (NBTTagCompound) manager.getDisk()
            .getTagCompound()
            .getTag(ProgramStateStore.NBT_KEY);
        assertEquals(
            1,
            ProgramStateStore
                .activate(
                    manager.getDisk(),
                    new ca.teamdman.sfml.program_builder.ProgramBuilder(COUNTER).build()
                        .getProgram()
                        .getStateSchema())
                .read(0)
                .asLong());
        prior.setInteger("version", 999);
        manager.recompile(changed);
        assertFalse(manager.hasCompiledProgram());
        assertSame(
            prior,
            manager.getDisk()
                .getTagCompound()
                .getTag(ProgramStateStore.NBT_KEY));
        manager.resetProgramState();
        assertTrue(manager.hasCompiledProgram(), manager.getStatusMessage());
        assertEquals(changed, ItemDisk.getProgramString(manager.getDisk()));
        assertTrue(
            LabelPositionHolder.from(manager.getDisk())
                .getAllLabels()
                .containsKey("kept"));
        assertEquals(
            2,
            manager.getProgramState()
                .read(0)
                .size());
        assertFalse(manager.isRunning()); // Reset does not automatically resume a paused program.
    }

    @Test
    void runtimeFaultPausesButEarlierHeapWritesReachSaveAndRecoveryRecordsSurviveStateReset() throws Exception {
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        TileEntityManager manager = counter(fixture);
        manager.saveCodeAndRecompile(
            "STATE $count = 0 EVERY 1 TICKS DO SET $count = 7 LET $n = 0 SET $count = 1 / $n END");
        manager.updateEntity();
        assertFalse(manager.isRunning());
        assertEquals(
            7,
            manager.getProgramState()
                .read(0)
                .asLong());
        NBTTagCompound saved = new NBTTagCompound();
        manager.writeToNBT(saved);
        TileEntityManager loaded = new TileEntityManager();
        loaded.readFromNBT(saved);
        assertEquals(
            7,
            loaded.getProgramState()
                .read(0)
                .asLong());
        NBTTagCompound recovery = new NBTTagCompound();
        recovery.setString("sentinel", "ownership evidence");
        manager.getDisk()
            .getTagCompound()
            .setTag("sfm:recovery_item", recovery);
        manager.resetProgramState();
        assertSame(
            recovery,
            manager.getDisk()
                .getTagCompound()
                .getTag("sfm:recovery_item"));
    }

    @Test
    void existingConstantProgramsDoNotAllocateStateOrFrames() throws Exception {
        AuditRegressionTest.Fixture fixture = new AuditRegressionTest.Fixture();
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        manager.saveCodeAndRecompile("EVERY 1 TICKS DO END");
        manager.updateEntity();
        assertNull(manager.getProgramState());
        assertFalse(
            manager.getDisk()
                .getTagCompound()
                .hasKey(ProgramStateStore.NBT_KEY));
    }
}
