package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.item.ModItems;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.tileentity.TileEntityDiskCopier;
import ca.teamdman.sfm.common.util.BlockPos;
import cpw.mods.fml.common.gameevent.TickEvent;

/** Real ItemStack persistence, inside the isolated Forge registry fixture only. */
public final class DiskCopierPersistenceFixture {

    public static void roundTrip() throws Exception {
        Item previous = ModItems.disk;
        assertNull(Item.getItemById(31999));
        ModItems.disk = new ItemDisk();
        Item.itemRegistry.addObject(31999, "superfactorymanager:copier_fixture_disk", ModItems.disk);
        net.minecraft.tileentity.TileEntity.addMapping(TileEntityDiskCopier.class, "sfm.diskCopier");
        ServerUiTasks.INSTANCE.tick(new TickEvent.ServerTickEvent(TickEvent.Phase.START));
        try {
            TileEntityDiskCopier copier = loaded(new TileEntityDiskCopier());
            ItemStack source = new ItemStack(ModItems.disk);
            ItemDisk.setProgramString(source, "every 20 ticks do print(1) end");
            copier.setInventorySlotContents(0, source);
            copier.setInventorySlotContents(1, new ItemStack(ModItems.disk));
            for (int i = 0; i < 99; i++) copier.updateEntity();
            assertEquals(99, copier.getProgress());
            NBTTagCompound saved = new NBTTagCompound();
            copier.writeToNBT(saved);
            assertFalse(saved.hasKey("progress"));
            assertFalse(saved.hasKey("candidate"));
            TileEntityDiskCopier reloaded = new TileEntityDiskCopier();
            reloaded.readFromNBT(saved);
            loaded(reloaded);
            assertEquals(0, reloaded.getProgress());
            assertEquals(
                source.getTagCompound(),
                reloaded.getStackInSlot(0)
                    .getTagCompound());
            assertNotNull(reloaded.getStackInSlot(1));
            assertNull(reloaded.getStackInSlot(2));
            for (int i = 0; i < 100; i++) reloaded.updateEntity();
            assertNull(reloaded.getStackInSlot(1));
            assertNotNull(reloaded.getStackInSlot(2));
            reloaded.writeToNBT(saved); // Reusing a save object must clear the consumed medium's old entry.
            assertFalse(saved.hasKey("disk1"));
            TileEntityDiskCopier completed = new TileEntityDiskCopier();
            completed.readFromNBT(saved);
            loaded(completed);
            for (int i = 0; i < 200; i++) completed.updateEntity();
            assertNull(completed.getStackInSlot(1));
            assertNotNull(completed.getStackInSlot(2));
            assertEquals(1, completed.getStackInSlot(2).stackSize);
            // A break/extraction drains each physical stack once, never an uncommitted candidate.
            assertNotNull(completed.getStackInSlotOnClosing(0));
            assertNull(completed.getStackInSlotOnClosing(1));
            assertNotNull(completed.getStackInSlotOnClosing(2));
            for (int slot = 0; slot < 3; slot++) assertNull(completed.getStackInSlotOnClosing(slot));
        } finally {
            ModItems.disk = previous;
            ServerUiTasks.clear();
        }
    }

    private static TileEntityDiskCopier loaded(TileEntityDiskCopier copier) throws Exception {
        AuditRegressionTest.FakeWorld world = AuditRegressionTest.world();
        // Vanilla markDirty notifies the block above; use real air after bootstrap.
        for (int x = -1; x <= 1; x++) for (int y = 63; y <= 65; y++)
            for (int z = -1; z <= 1; z++) world.blocks.put(new BlockPos(x, y, z), net.minecraft.init.Blocks.air);
        copier.setWorldObj(world);
        copier.xCoord = 0;
        copier.yCoord = 64;
        copier.zCoord = 0;
        world.tiles.put(new BlockPos(0, 64, 0), copier);
        return copier;
    }
}
