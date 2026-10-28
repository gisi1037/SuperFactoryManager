package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

class ItemRecoveryTest {

    private TileEntityManager manager(Fixture fixture) throws Exception {
        return RecoveryFixtures.manager(fixture);
    }

    private NBTTagCompound recovery(TileEntityManager manager) {
        return manager.getDisk()
            .getTagCompound()
            .getCompoundTag("sfm:recovery_item");
    }

    private void run(Fixture f, TileEntityManager manager) {
        parse("EVERY 20 TICKS DO INPUT FROM source OUTPUT 1 TO dest OUTPUT 1 TO dest END")
            .tick(new ProgramContext(f.world, manager, new CableNetwork(f.world, MANAGER), f.labels, 20, false));
    }

    @Test
    void rollbackConflictPreservesKnownRemainderAndStopsLaterOutputs() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = manager(f);
        InventoryTile source = new InventoryTile(1) {

            @Override
            public ItemStack decrStackSize(int slot, int amount) {
                ItemStack extracted = super.decrStackSize(slot, amount);
                // Another resource appears at the source while the transfer callback is running.
                super.setInventorySlotContents(slot, new ItemStack(OTHER));
                return extracted;
            }
        };
        source.setInventorySlotContents(0, new ItemStack(ITEM, 1, 7));
        NBTTagCompound payload = new NBTTagCompound();
        payload.setString("owner", "recovery fixture");
        source.getStackInSlot(0)
            .setTagCompound(payload);
        f.world.tiles.put(SRC, source);
        InventoryTile destination = new InventoryTile(1) {

            @Override
            public boolean isItemValidForSlot(int slot, ItemStack stack) {
                return source.getStackInSlot(0)
                    .getItem() != OTHER;
            }
        };
        f.world.tiles.put(DST, destination);

        assertThrows(IllegalStateException.class, () -> run(f, manager));
        NBTTagCompound record = recovery(manager);
        assertEquals(1, record.getInteger("version"));
        assertEquals("pending", record.getString("state"));
        assertEquals("rollback conflict", record.getString("stage"));
        assertEquals(
            1,
            record.getCompoundTag("stack")
                .getByte("Count"));
        assertEquals(
            7,
            record.getCompoundTag("stack")
                .getShort("Damage"));
        assertEquals(
            payload,
            record.getCompoundTag("stack")
                .getCompoundTag("tag"));
        assertEquals(SRC.x, record.getInteger("sourceX"));
        assertEquals(0, record.getInteger("sourceSlot"));
        assertEquals(0, count(destination));
        assertFalse(manager.isRunning());
        assertDoesNotThrow(manager::updateEntity);
        assertDoesNotThrow(() -> manager.runOnceWithLogLevel(ca.teamdman.sfm.common.tileentity.LogLevel.INFO));
        assertEquals(record, recovery(manager));
    }

    @Test
    void destinationMutationThenThrowIsRecordedAsUncertainWithoutRetry() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = manager(f);
        f.stock(0, 1);
        int[] calls = { 0 };
        InventoryTile destination = new InventoryTile(1) {

            @Override
            public void setInventorySlotContents(int slot, ItemStack stack) {
                calls[0]++;
                super.setInventorySlotContents(slot, stack);
                throw new IllegalStateException("after destination mutation");
            }
        };
        f.world.tiles.put(DST, destination);
        assertThrows(IllegalStateException.class, () -> run(f, manager));
        assertEquals("uncertain", recovery(manager).getString("state"));
        assertEquals("destination insertion", recovery(manager).getString("stage"));
        assertEquals(1, count(destination));
        assertEquals(0, count(f.source));
        manager.setRunning(true);
        manager.updateEntity();
        assertFalse(manager.isRunning());
        assertEquals(1, calls[0]);
        // The record is evidence of this attempt; it must not be counted as another owned stack.
    }

    @Test
    void extractionMutationThenThrowIsNotRepeated() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = manager(f);
        InventoryTile source = new InventoryTile(1) {

            @Override
            public ItemStack decrStackSize(int slot, int amount) {
                super.decrStackSize(slot, amount);
                throw new IllegalStateException("after extraction mutation");
            }
        };
        source.setInventorySlotContents(0, new ItemStack(ITEM));
        f.world.tiles.put(SRC, source);
        assertThrows(IllegalStateException.class, () -> run(f, manager));
        assertEquals("uncertain", recovery(manager).getString("state"));
        assertEquals("extraction", recovery(manager).getString("stage"));
        assertEquals(0, count(source));
        manager.updateEntity();
        assertEquals(0, count(f.dest));
    }

    @Test
    void rollbackMutationThenThrowIsNotRestoredTwice() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = manager(f);
        boolean[] extracted = { false };
        InventoryTile source = new InventoryTile(1) {

            @Override
            public ItemStack decrStackSize(int slot, int amount) {
                ItemStack stack = super.decrStackSize(slot, amount);
                extracted[0] = true;
                return stack;
            }

            @Override
            public void setInventorySlotContents(int slot, ItemStack stack) {
                super.setInventorySlotContents(slot, stack);
                if (extracted[0]) throw new IllegalStateException("after rollback mutation");
            }
        };
        source.setInventorySlotContents(0, new ItemStack(ITEM));
        f.world.tiles.put(SRC, source);
        f.world.tiles.put(DST, new InventoryTile(1) {

            @Override
            public boolean isItemValidForSlot(int slot, ItemStack stack) {
                return !extracted[0];
            }
        });
        assertThrows(IllegalStateException.class, () -> run(f, manager));
        assertEquals("uncertain", recovery(manager).getString("state"));
        assertEquals("source rollback", recovery(manager).getString("stage"));
        assertEquals(1, count(source));
        manager.updateEntity();
        assertEquals(1, count(source));
    }

    @Test
    void resetAndMovingDiskPreserveIndependentRecoveryPayload() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = manager(f);
        ItemStack original = new ItemStack(ITEM, 9, 4);
        original.setStackDisplayName("saved stack");
        manager.preserveItem(original, SRC, 2, ForgeDirection.WEST, false, "fixture");
        NBTTagCompound before = (NBTTagCompound) recovery(manager).copy();
        original.stackSize = 0;
        original.getTagCompound()
            .setString("changed", "outside");
        assertEquals(before, recovery(manager));
        manager.reset();
        assertEquals(before, recovery(manager));
        ItemStack moved = manager.decrStackSize(0, 1);
        TileEntityManager replacement = manager(f);
        replacement.setInventorySlotContents(0, moved);
        replacement.updateEntity();
        assertFalse(replacement.isRunning());
        assertEquals(before, recovery(replacement));
        NBTTagCompound encoded = new NBTTagCompound();
        replacement.writeToNBT(encoded);
        TileEntityManager reloaded = manager(f);
        reloaded.readFromNBT((NBTTagCompound) encoded.copy());
        reloaded.updateEntity();
        assertFalse(reloaded.isRunning());
        assertEquals(before, recovery(reloaded));
        assertEquals(
            before,
            encoded.getCompoundTag("disk")
                .getCompoundTag("tag")
                .getCompoundTag("sfm:recovery_item"));
    }

    @Test
    void pendingRecordCannotBeOverwrittenByItemOrFluidRecovery() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = manager(f);
        manager.preserveItem(new ItemStack(ITEM), SRC, 0, ForgeDirection.UP, false, "fixture");
        NBTTagCompound before = (NBTTagCompound) manager.getDisk()
            .getTagCompound()
            .copy();
        assertThrows(
            IllegalStateException.class,
            () -> manager.preserveItem(new ItemStack(OTHER), DST, 1, ForgeDirection.DOWN, false, "other"));
        assertThrows(IllegalStateException.class, () -> manager.preserveFluid(water(1), DST, ForgeDirection.DOWN));
        assertEquals(
            before,
            manager.getDisk()
                .getTagCompound());
    }

    @Test
    void legacyFluidRecordAlsoBlocksItemOverwrite() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = manager(f);
        NBTTagCompound root = new NBTTagCompound();
        NBTTagCompound fluid = new NBTTagCompound();
        fluid.setInteger("Amount", 20);
        root.setTag("sfm:recovery_fluid", fluid);
        manager.getDisk()
            .setTagCompound(root);
        assertThrows(
            IllegalStateException.class,
            () -> manager.preserveItem(new ItemStack(ITEM), SRC, 0, ForgeDirection.UP, false, "fixture"));
        assertEquals(fluid, root.getCompoundTag("sfm:recovery_fluid"));
    }

    @Test
    void corruptAndUnknownRecoveryRecordsAreKeptAndBlockExecution() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = manager(f);
        NBTTagCompound root = new NBTTagCompound();
        root.setString("sfm:recovery_item", "unknown payload");
        manager.getDisk()
            .setTagCompound(root);
        manager.updateEntity();
        assertFalse(manager.isRunning());
        assertEquals("unknown payload", root.getString("sfm:recovery_item"));
        root.removeTag("sfm:recovery_item");
        NBTTagCompound unknown = new NBTTagCompound();
        unknown.setInteger("version", 123);
        root.setTag("sfm:recovery_fluid", unknown);
        manager.updateEntity();
        assertEquals(unknown, root.getCompoundTag("sfm:recovery_fluid"));
        assertTrue(
            manager.getStatusMessage()
                .startsWith("Unsupported fluid"));
    }
}
