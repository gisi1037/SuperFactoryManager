package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.block.BlockManager;
import ca.teamdman.sfm.common.gui.ContainerManager;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.program.ProgramStateStore;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ProgramStateSchema;

class ContainerStateTest {

    private static final String COUNTER = "STATE $count = 0 EVERY 1 TICKS DO SET $count = $count + 1 END";
    private static final ProgramStateSchema SCHEMA = new ProgramStateSchema(
        Collections.singletonList(new ProgramStateSchema.Declaration("count", DataValue.integer(0))));

    @Test
    void ordinaryGuiPollingLeavesLiveStateDirtyAndThePublishedEnvelopeUnchanged() throws Exception {
        Fixture fixture = new Fixture();
        TileEntityManager manager = counter(fixture);
        FakePlayer player = player(fixture);
        ContainerManager container = new ContainerManager(player.inventory, manager);
        Slot diskSlot = container.getSlot(0);
        NBTBase original = manager.getDisk()
            .getTagCompound()
            .getTag(ProgramStateStore.NBT_KEY);
        container.detectAndSendChanges();
        for (int i = 0; i < 100; i++) {
            manager.updateEntity();
            assertTrue(diskSlot.getHasStack());
            assertSame(manager.getDisk(), diskSlot.getStack());
            container.detectAndSendChanges();
        }
        assertEquals(
            100,
            manager.getProgramState()
                .read(0)
                .asLong());
        assertTrue(
            manager.getProgramState()
                .isDirty());
        assertSame(
            original,
            manager.getDisk()
                .getTagCompound()
                .getTag(ProgramStateStore.NBT_KEY));
        assertEquals(0, snapshotValue(manager.getDisk()));
        assertEquals(1, countDisks(player, manager));
        // External inventory access remains a publication boundary.
        assertSame(manager.getDisk(), manager.getStackInSlot(0));
        assertFalse(
            manager.getProgramState()
                .isDirty());
        assertEquals(100, snapshotValue(manager.getDisk()));
    }

    @Test
    void pickupFlushesBeforeVanillaPredictionCopyAndConservesTheLatestDiskState() throws Exception {
        for (int button = 0; button <= 1; button++) {
            Fixture fixture = new Fixture();
            TileEntityManager manager = counter(fixture);
            FakePlayer player = player(fixture);
            ContainerManager container = new ContainerManager(player.inventory, manager);
            manager.updateEntity();
            manager.updateEntity();
            ProgramStateStore active = manager.getProgramState();
            assertTrue(active.isDirty());
            ItemStack prediction = container.slotClick(0, button, 0, player);
            ItemStack pickedUp = player.inventory.getItemStack();
            assertNotNull(prediction);
            assertNotNull(pickedUp);
            assertEquals(2, snapshotValue(prediction), "Vanilla's pre-removal copy must contain the latest state");
            assertEquals(2, snapshotValue(pickedUp));
            assertEquals(COUNTER, ItemDisk.getProgramString(pickedUp));
            assertEquals(
                "retained",
                pickedUp.getTagCompound()
                    .getString("audit:metadata"));
            assertFalse(active.isDirty());
            assertNull(manager.getDisk());
            assertNull(manager.getProgramState());
            assertEquals(1, countDisks(player, manager));
        }
    }

    @Test
    void shiftClickAndDirectTransferBothPublishBeforeAnyReturnedOrInventoryCopies() throws Exception {
        for (boolean direct : new boolean[] { false, true }) {
            Fixture fixture = new Fixture();
            TileEntityManager manager = counter(fixture);
            FakePlayer player = player(fixture);
            ContainerManager container = new ContainerManager(player.inventory, manager);
            manager.updateEntity();
            manager.updateEntity();
            manager.updateEntity();
            ItemStack prediction = direct ? container.transferStackInSlot(player, 0)
                : container.slotClick(0, 0, 1, player);
            assertNotNull(prediction);
            assertEquals(3, snapshotValue(prediction));
            assertNull(manager.getDisk());
            assertNull(player.inventory.getItemStack());
            ItemStack transferred = findDisk(player.inventory);
            assertNotNull(transferred);
            assertEquals(3, snapshotValue(transferred));
            assertEquals(1, countDisks(player, manager));
            // Returning the disk to the manager must resume from that same state.
            int diskIndex = findDiskIndex(player.inventory);
            ItemStack moved = player.inventory.decrStackSize(diskIndex, 1);
            manager.setInventorySlotContents(0, moved);
            assertEquals(
                3,
                manager.getProgramState()
                    .read(0)
                    .asLong());
            assertEquals(1, countDisks(player, manager));
        }
    }

    @Test
    void breakingManagerPublishesHeapStateBeforeTheDiskBecomesAWorldDrop() throws Exception {
        Fixture fixture = new Fixture();
        TileEntityManager manager = counter(fixture);
        manager.updateEntity();
        manager.updateEntity();
        assertTrue(
            manager.getProgramState()
                .isDirty());
        DropWorld world = allocate(DropWorld.class);
        world.manager = manager;
        setWorldField(world, "provider", new WorldProviderSurface());
        setWorldField(world, "rand", new Random(1037));
        BlockManager block = new BlockManager();
        block.breakBlock(world, MANAGER.x, MANAGER.y, MANAGER.z, block, 0);
        assertEquals(1, world.spawned);
        assertNotNull(world.dropped);
        ItemStack dropped = world.dropped.getEntityItem();
        assertEquals(1, dropped.stackSize);
        assertEquals(2, snapshotValue(dropped));
        assertEquals(COUNTER, ItemDisk.getProgramString(dropped));
        assertEquals(
            "retained",
            dropped.getTagCompound()
                .getString("audit:metadata"));
        assertFalse(
            manager.getProgramState()
                .isDirty());
    }

    private static TileEntityManager counter(Fixture fixture) throws Exception {
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        manager.saveCodeAndRecompile(COUNTER);
        assertTrue(manager.hasCompiledProgram(), manager.getStatusMessage());
        manager.getDisk()
            .getTagCompound()
            .setString("audit:metadata", "retained");
        return manager;
    }

    private static FakePlayer player(Fixture fixture) throws Exception {
        FakePlayer player = allocate(FakePlayer.class);
        player.worldObj = fixture.world;
        player.posX = MANAGER.x + 0.5;
        player.posY = MANAGER.y + 0.5;
        player.posZ = MANAGER.z + 0.5;
        player.inventory = new InventoryPlayer(player);
        return player;
    }

    private static long snapshotValue(ItemStack stack) {
        return ProgramStateStore.activate(stack, SCHEMA)
            .read(0)
            .asLong();
    }

    private static ItemStack findDisk(InventoryPlayer inventory) {
        int index = findDiskIndex(inventory);
        return index < 0 ? null : inventory.getStackInSlot(index);
    }

    private static int findDiskIndex(InventoryPlayer inventory) {
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemDisk) return i;
        }
        return -1;
    }

    private static int countDisks(FakePlayer player, TileEntityManager manager) {
        int count = manager.getDisk() == null ? 0 : manager.getDisk().stackSize;
        ItemStack cursor = player.inventory.getItemStack();
        if (cursor != null && cursor.getItem() instanceof ItemDisk) count += cursor.stackSize;
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemDisk) count += stack.stackSize;
        }
        return count;
    }

    private static void setWorldField(World world, String name, Object value) throws Exception {
        Field field = World.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(world, value);
    }

    private static final class DropWorld extends World {

        private TileEntityManager manager;
        private EntityItem dropped;
        private int spawned;

        private DropWorld() {
            super((ISaveHandler) null, "state-drop", (WorldSettings) null, (WorldProvider) null, (Profiler) null);
        }

        @Override
        protected IChunkProvider createChunkProvider() {
            return null;
        }

        @Override
        protected int func_152379_p() {
            return 0;
        }

        @Override
        public Entity getEntityByID(int id) {
            return null;
        }

        @Override
        public TileEntity getTileEntity(int x, int y, int z) {
            return manager;
        }

        @Override
        public boolean spawnEntityInWorld(Entity entity) {
            dropped = (EntityItem) entity;
            spawned++;
            return true;
        }

        @Override
        public void removeTileEntity(int x, int y, int z) {
            manager = null;
        }
    }
}
