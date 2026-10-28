package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.diagnostics.ManagerTickProfiler;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import cpw.mods.fml.common.gameevent.TickEvent;

class ManagerTickProfilerTest {

    private final ManagerTickProfiler profiler = new ManagerTickProfiler();

    private void start(ManagerTickProfiler capture) {
        capture.onServerTick(new TickEvent.ServerTickEvent(TickEvent.Phase.START));
    }

    private void end(ManagerTickProfiler capture) {
        capture.onServerTick(new TickEvent.ServerTickEvent(TickEvent.Phase.END));
    }

    @AfterEach
    void resetSingleton() {
        ManagerTickProfiler.INSTANCE.reset();
    }

    @Test
    void disabledCaptureAndOutOfTickUpdatesDoNotProduceSamples() {
        start(profiler);
        profiler.recordUpdate(100);
        end(profiler);
        assertEquals(0, profiler.snapshot().ticks);
        profiler.setEnabled(true);
        profiler.recordUpdate(100);
        end(profiler);
        assertEquals(0, profiler.snapshot().updates);
        assertEquals(0, profiler.snapshot().ticks);
    }

    @Test
    void aggregatesAllManagersAndIncludesEmptyServerTicks() {
        profiler.setEnabled(true);
        start(profiler);
        profiler.recordUpdate(100);
        profiler.recordUpdate(200);
        end(profiler);
        start(profiler);
        end(profiler);
        ManagerTickProfiler.Snapshot snapshot = profiler.snapshot();
        assertEquals(2, snapshot.ticks);
        assertEquals(2, snapshot.updates);
        assertEquals(300, snapshot.totalNanos);
        assertEquals(0, snapshot.p50Nanos);
        assertEquals(300, snapshot.p95Nanos);
        assertEquals(300, snapshot.maxNanos);
    }

    @Test
    void ringRetainsOnlyLast600CompletedTicks() {
        profiler.setEnabled(true);
        for (int i = 1; i <= 700; i++) {
            start(profiler);
            profiler.recordUpdate(i);
            end(profiler);
        }
        ManagerTickProfiler.Snapshot snapshot = profiler.snapshot();
        assertEquals(600, snapshot.ticks);
        assertEquals(600, snapshot.updates);
        assertEquals(240300, snapshot.totalNanos);
        assertEquals(400, snapshot.p50Nanos);
        assertEquals(670, snapshot.p95Nanos);
        assertEquals(694, snapshot.p99Nanos);
        assertEquals(700, snapshot.maxNanos);
    }

    @Test
    void stopRetainsCompletedCaptureAndRestartClearsIt() {
        profiler.setEnabled(true);
        start(profiler);
        profiler.recordUpdate(100);
        end(profiler);
        ManagerTickProfiler.Snapshot before = profiler.snapshot();
        start(profiler);
        profiler.recordUpdate(999);
        profiler.setEnabled(false);
        end(profiler);
        assertEquals(100, profiler.snapshot().totalNanos);
        assertFalse(profiler.snapshot().enabled);
        profiler.setEnabled(true);
        assertEquals(0, profiler.snapshot().ticks);
        assertEquals(100, before.totalNanos);
    }

    @Test
    void fullManagerTimingIncludesIdlePausedAndInvalidProgramUpdates() throws Exception {
        ManagerTickProfiler capture = ManagerTickProfiler.INSTANCE;
        capture.setEnabled(true);
        start(capture);
        TileEntityManager manager = new TileEntityManager();
        manager.setWorldObj(AuditRegressionTest.world());
        manager.updateEntity();
        ItemStack disk = new ItemStack(new ItemDisk());
        manager.setInventorySlotContents(0, disk);
        manager.setRunning(false);
        manager.updateEntity();
        manager.setRunning(true);
        ItemDisk.setProgramString(disk, "invalid source");
        manager.updateEntity();
        end(capture);
        assertEquals(3, manager.getTimedUpdateCount());
        assertEquals(3, capture.snapshot().updates);
        assertEquals(1, capture.snapshot().ticks);
        assertFalse(manager.hasCompiledProgram());
    }

    @Test
    void failedDiscoveryIsRecordedByTheOuterTimer() throws Exception {
        ManagerTickProfiler capture = ManagerTickProfiler.INSTANCE;
        capture.setEnabled(true);
        start(capture);
        TileEntityManager manager = new TileEntityManager() {

            @Override
            public void rebuildNetwork() {
                throw new IllegalStateException("discovery fixture failure");
            }
        };
        manager.setWorldObj(AuditRegressionTest.world());
        ItemStack disk = new ItemStack(new ItemDisk());
        ItemDisk.setProgramString(disk, "EVERY 1 TICKS DO INPUT FROM source END");
        manager.setInventorySlotContents(0, disk);
        assertThrows(IllegalStateException.class, manager::updateEntity);
        end(capture);
        assertEquals(1, manager.getTimedUpdateCount());
        assertEquals(1, capture.snapshot().updates);
    }

    @Test
    void recoveryCountsButClientUpdatesDoNot() throws Exception {
        ManagerTickProfiler capture = ManagerTickProfiler.INSTANCE;
        capture.setEnabled(true);
        start(capture);
        TileEntityManager manager = new TileEntityManager();
        AuditRegressionTest.FakeWorld world = AuditRegressionTest.world();
        manager.setWorldObj(world);
        ItemStack disk = new ItemStack(new ItemDisk());
        net.minecraft.nbt.NBTTagCompound root = new net.minecraft.nbt.NBTTagCompound();
        root.setString("sfm:recovery_item", "preserved malformed record");
        disk.setTagCompound(root);
        manager.setInventorySlotContents(0, disk);
        manager.updateEntity();
        world.isRemote = true;
        manager.updateEntity();
        end(capture);
        assertEquals(1, manager.getTimedUpdateCount());
        assertEquals(1, capture.snapshot().updates);
        assertFalse(manager.isRunning());
    }
}
