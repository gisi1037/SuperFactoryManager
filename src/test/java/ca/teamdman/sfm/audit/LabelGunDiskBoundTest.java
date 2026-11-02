package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Field;
import java.util.Collections;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.item.ItemLabelGun;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.program.ProgramStateStore;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ProgramStateSchema;

/** Exercise the actual gun push path with pending heap STATE and vanilla's complete NBT encoding. */
class LabelGunDiskBoundTest {

    @Test
    void nearLimitPushKeepsTheLiveDiskRootAndState() throws Exception {
        GunFixture f = new GunFixture();
        f.padTo(29800);
        NBTTagCompound root = f.disk.getTagCompound();
        f.state.write(0, DataValue.integer(47));
        long generation = f.manager.getDiskGeneration();
        f.push();
        assertSame(root, f.disk.getTagCompound());
        assertSame(f.disk, f.manager.getDisk());
        assertSame(f.state, f.manager.getProgramState());
        assertEquals(generation, f.manager.getDiskGeneration());
        assertTrue(f.state.isCurrent(f.disk));
        assertFalse(f.state.isDirty());
        assertEquals(
            47,
            ProgramStateStore.activate(f.disk, f.state.getSchema())
                .read(0)
                .asLong());
        assertEquals(
            Collections.singleton(SRC),
            LabelPositionHolder.from(f.disk)
                .getPositions("new"));
        ProgramStateStore.validateCompleteDisk(f.disk);
    }

    @Test
    void oversizedPushLeavesOldLabelsAndCurrentStateIntact() throws Exception {
        GunFixture f = new GunFixture();
        LabelPositionHolder labels = new LabelPositionHolder();
        labels.addPosition(String.join("", Collections.nCopies(300, "x")), SRC);
        LabelPositionHolder.saveTo(f.gun, labels);
        f.padTo(29900);
        NBTTagCompound root = f.disk.getTagCompound();
        NBTBase oldLabels = root.getTag(LabelPositionHolder.NBT_KEY)
            .copy();
        NBTBase gunBefore = f.gun.getTagCompound()
            .copy();
        f.state.write(0, DataValue.integer(83));
        long generation = f.manager.getDiskGeneration();
        f.push();
        assertSame(root, f.disk.getTagCompound());
        assertSame(f.disk, f.manager.getDisk());
        assertEquals(generation, f.manager.getDiskGeneration());
        assertEquals(oldLabels, root.getTag(LabelPositionHolder.NBT_KEY));
        assertEquals(gunBefore, f.gun.getTagCompound());
        assertTrue(f.state.isCurrent(f.disk));
        assertFalse(f.state.isDirty());
        assertEquals(
            83,
            ProgramStateStore.activate(f.disk, f.state.getSchema())
                .read(0)
                .asLong());
        ProgramStateStore.validateCompleteDisk(f.disk);
    }

    @Test
    void stateFlushCallbacksCannotRedirectThePushToAnotherManagerOrDisk() throws Exception {
        for (boolean replaceManager : new boolean[] { false, true }) {
            GunFixture f = new GunFixture();
            ItemStack replacement = new ItemStack(new ItemDisk());
            NBTBase oldLabels = f.disk.getTagCompound()
                .getTag(LabelPositionHolder.NBT_KEY)
                .copy();
            f.manager.onDirty = () -> {
                if (replaceManager) f.world.tiles.put(MANAGER, new TileEntityManager());
                else set(f.manager, "diskStack", replacement);
            };
            f.state.write(0, DataValue.integer(99));
            f.push();
            assertEquals(
                oldLabels,
                f.disk.getTagCompound()
                    .getTag(LabelPositionHolder.NBT_KEY));
            assertFalse(replacement.hasTagCompound());
            assertEquals(
                99,
                ProgramStateStore.activate(f.disk, f.state.getSchema())
                    .read(0)
                    .asLong());
        }
    }

    private static void set(Object object, String name, Object value) {
        try {
            Field field = TileEntityManager.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(object, value);
        } catch (ReflectiveOperationException error) {
            throw new AssertionError(error);
        }
    }

    private static final class GunFixture {

        final FakeWorld world = world();
        final TestPlayer player = allocate(TestPlayer.class);
        final TestManager manager = new TestManager();
        final ItemStack disk = new ItemStack(new ItemDisk()), gun = new ItemStack(new ItemLabelGun());
        final ProgramStateStore state;

        GunFixture() throws Exception {
            player.worldObj = world;
            manager.setWorldObj(world);
            manager.xCoord = MANAGER.x;
            manager.yCoord = MANAGER.y;
            manager.zCoord = MANAGER.z;
            world.tiles.put(MANAGER, manager);
            set(manager, "diskStack", disk);
            LabelPositionHolder old = new LabelPositionHolder(), next = new LabelPositionHolder();
            old.addPosition("old", DST);
            next.addPosition("new", SRC);
            LabelPositionHolder.saveTo(disk, old);
            LabelPositionHolder.saveTo(gun, next);
            state = ProgramStateStore.activate(
                disk,
                new ProgramStateSchema(
                    Collections.singletonList(new ProgramStateSchema.Declaration("counter", DataValue.integer(0)))));
            set(manager, "programState", state);
            state.flush();
        }

        void padTo(int bytes) throws Exception {
            disk.getTagCompound()
                .setString("padding", "");
            ByteArrayOutputStream encoded = new ByteArrayOutputStream();
            CompressedStreamTools.write(disk.getTagCompound(), new DataOutputStream(encoded));
            disk.getTagCompound()
                .setString("padding", String.join("", Collections.nCopies(bytes - encoded.size(), "a")));
            ProgramStateStore.validateCompleteDisk(disk);
        }

        void push() {
            assertTrue(
                gun.getItem()
                    .onItemUseFirst(gun, player, world, MANAGER.x, MANAGER.y, MANAGER.z, 1, 0, 0, 0));
        }
    }

    private static final class TestPlayer extends EntityPlayer {

        private TestPlayer() {
            super(null, null);
        }

        @Override
        public boolean isSneaking() {
            return false;
        }

        @Override
        public void addChatMessage(net.minecraft.util.IChatComponent text) {}

        @Override
        public boolean canCommandSenderUseCommand(int level, String command) {
            return false;
        }

        @Override
        public net.minecraft.util.ChunkCoordinates getPlayerCoordinates() {
            return new net.minecraft.util.ChunkCoordinates(MANAGER.x, MANAGER.y, MANAGER.z);
        }
    }

    private static final class TestManager extends TileEntityManager {

        Runnable onDirty;

        @Override
        public void markDirty() {
            Runnable callback = onDirty;
            onDirty = null;
            if (callback != null) callback.run();
        }
    }
}
