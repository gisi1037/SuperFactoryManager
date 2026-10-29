package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneRepeater;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import ca.teamdman.sfm.common.block.BlockRedstonePort;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.tileentity.TileEntityRedstonePort;
import ca.teamdman.sfm.common.util.BlockPos;
import cpw.mods.fml.common.gameevent.TickEvent;

class RedstonePortTest {

    private static final BlockPos POSITION = new BlockPos(0, 64, 0);

    @BeforeEach
    void begin() {
        ServerUiTasks.clear();
        ServerUiTasks.INSTANCE.tick(new TickEvent.ServerTickEvent(TickEvent.Phase.START));
    }

    @AfterEach
    void end() {
        ServerUiTasks.clear();
    }

    @Test
    void eachPhysicalFaceUsesOppositeVanillaReceiverArgumentForWeakAndStrongOutput() throws Exception {
        Fixture fixture = new Fixture();
        assertFalse(fixture.tile.canUpdate());
        assertTrue(fixture.block.canProvidePower());
        assertFalse(fixture.block.shouldCheckWeakPower(fixture.world, 0, 64, 0, 0));
        for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
            fixture.tile.setOutput(null, 0);
            int level = face.ordinal() + 1;
            assertTrue(fixture.tile.setOutput(Arrays.asList(face), level));
            for (ForgeDirection receiver : ForgeDirection.VALID_DIRECTIONS) {
                int expected = receiver == face.getOpposite() ? level : 0;
                assertEquals(expected, fixture.block.isProvidingWeakPower(fixture.world, 0, 64, 0, receiver.ordinal()));
                assertEquals(
                    expected,
                    fixture.block.isProvidingStrongPower(fixture.world, 0, 64, 0, receiver.ordinal()));
            }
        }
        assertEquals(0, fixture.block.isProvidingStrongPower(fixture.world, 0, 64, 0, 6));
        assertEquals(0, fixture.block.isProvidingWeakPower(fixture.world, 0, 64, 0, -1));
    }

    @Test
    void outputBatchInstallsAllLevelsBeforeNotificationsAndUnchangedWritesAreSilent() throws Exception {
        Fixture fixture = new Fixture();
        fixture.world.onNotify = () -> {
            assertEquals(15, fixture.tile.getOutputLevel(ForgeDirection.NORTH.ordinal()));
            assertEquals(15, fixture.tile.getOutputLevel(ForgeDirection.EAST.ordinal()));
        };
        List<ForgeDirection> faces = Arrays.asList(ForgeDirection.NORTH, ForgeDirection.EAST);
        assertTrue(fixture.tile.setOutput(faces, 15));
        assertEquals(1, fixture.world.dirty);
        assertEquals(1, fixture.world.updates);
        assertEquals(2, fixture.world.notifications.size());
        assertFalse(fixture.tile.setOutput(faces, 15));
        assertEquals(1, fixture.world.dirty);
        assertEquals(1, fixture.world.updates);
        assertEquals(2, fixture.world.notifications.size());
        fixture.world.onNotify = null;
        assertTrue(fixture.tile.setOutput(faces, 0));
        assertEquals(2, fixture.world.dirty);
        assertEquals(2, fixture.world.updates);
        assertEquals(4, fixture.world.notifications.size());
    }

    @Test
    void invalidStrengthAndUnknownFacesNeverPartiallyChangeOutputs() throws Exception {
        Fixture fixture = new Fixture();
        assertFalse(fixture.tile.setOutput(null, -1));
        assertFalse(fixture.tile.setOutput(null, 16));
        assertFalse(fixture.tile.setOutput(Arrays.asList(ForgeDirection.NORTH, ForgeDirection.UNKNOWN), 4));
        assertFalse(fixture.tile.setOutput(Arrays.asList(ForgeDirection.EAST, null), 4));
        for (int side = 0; side < 6; side++) assertEquals(0, fixture.tile.getOutputLevel(side));
        assertEquals(0, fixture.world.dirty);
        assertTrue(fixture.world.notifications.isEmpty());
    }

    @Test
    void inputsUseReceiverFaceAndOnlyZeroCrossingsIncrementIndependentCounters() throws Exception {
        Fixture fixture = new Fixture();
        fixture.tile.refreshInputs();
        long epoch = fixture.tile.getSignalEpoch();
        for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
            fixture.neighbor(face).weak[face.ordinal()] = face.ordinal() + 1;
            fixture.tile.refreshInputs();
            assertEquals(face.ordinal() + 1, fixture.tile.getInputLevel(face.ordinal()));
            assertEquals(1, fixture.tile.getRisingCount(face.ordinal()));
            assertEquals(0, fixture.tile.getFallingCount(face.ordinal()));
        }
        assertEquals(epoch, fixture.tile.getSignalEpoch());
        fixture.neighbor(ForgeDirection.NORTH).weak[2] = 15;
        fixture.tile.refreshInputs();
        assertEquals(1, fixture.tile.getRisingCount(2));
        assertEquals(15, fixture.tile.getInputLevel(2));
        fixture.neighbor(ForgeDirection.NORTH).weak[2] = 0;
        fixture.tile.refreshInputs();
        assertEquals(1, fixture.tile.getFallingCount(2));
        assertEquals(0, fixture.tile.getFallingCount(3));
    }

    @Test
    void neighborCallbacksCapturePulseThatEndsBeforeNextQuery() throws Exception {
        Fixture fixture = new Fixture();
        fixture.tile.refreshInputs();
        fixture.neighbor(ForgeDirection.NORTH).weak[2] = 15;
        fixture.block.onNeighborBlockChange(fixture.world, 0, 64, 0, fixture.neighbor(ForgeDirection.NORTH));
        fixture.neighbor(ForgeDirection.NORTH).weak[2] = 0;
        fixture.block.onNeighborBlockChange(fixture.world, 0, 64, 0, fixture.neighbor(ForgeDirection.NORTH));
        assertEquals(0, fixture.tile.getInputLevel(2));
        assertEquals(1, fixture.tile.getRisingCount(2));
        assertEquals(1, fixture.tile.getFallingCount(2));
    }

    @Test
    void initializationReloadAndNeighborAvailabilityChangesRebaselineWithoutEdges() throws Exception {
        Fixture fixture = new Fixture();
        fixture.neighbor(ForgeDirection.NORTH).weak[2] = 15;
        fixture.tile.refreshInputs();
        assertEquals(15, fixture.tile.getInputLevel(2));
        assertEquals(0, fixture.tile.getRisingCount(2));
        long epoch = fixture.tile.getSignalEpoch();
        fixture.tile.onChunkUnload();
        assertNotEquals(epoch, fixture.tile.getSignalEpoch());
        assertFalse(fixture.tile.isInputInitialized());
        fixture.tile.validate();
        fixture.tile.refreshInputs();
        assertEquals(0, fixture.tile.getRisingCount(2));
        epoch = fixture.tile.getSignalEpoch();
        fixture.world.unloaded.add(POSITION.offset(ForgeDirection.NORTH));
        fixture.tile.refreshInputs();
        assertFalse(fixture.tile.isInputAvailable(2));
        assertNotEquals(epoch, fixture.tile.getSignalEpoch());
        assertEquals(0, fixture.tile.getFallingCount(2));
        epoch = fixture.tile.getSignalEpoch();
        fixture.world.unloaded.clear();
        fixture.tile.refreshInputs();
        assertTrue(fixture.tile.isInputAvailable(2));
        assertNotEquals(epoch, fixture.tile.getSignalEpoch());
        assertEquals(15, fixture.tile.getInputLevel(2));
        assertEquals(0, fixture.tile.getRisingCount(2));
    }

    @Test
    void solidNeighborConductionUsesLoadedStrongSourcesAndNeverLoadsItsFarNeighbors() throws Exception {
        Fixture fixture = new Fixture();
        BlockPos north = POSITION.offset(ForgeDirection.NORTH);
        fixture.world.blocks.put(north, new SignalBlock(true));
        SignalBlock source = new SignalBlock(false);
        source.strong[ForgeDirection.WEST.ordinal()] = 7;
        fixture.world.blocks.put(north.offset(ForgeDirection.WEST), source);
        fixture.tile.refreshInputs();
        assertTrue(fixture.tile.isInputAvailable(2));
        assertEquals(7, fixture.tile.getInputLevel(2));
        fixture.world.unloaded.add(north.offset(ForgeDirection.NORTH));
        fixture.tile.refreshInputs();
        assertFalse(fixture.tile.isInputAvailable(2));
        source.strong[ForgeDirection.WEST.ordinal()] = 15;
        fixture.tile.refreshInputs();
        assertFalse(fixture.tile.isInputAvailable(2), "Every required conductive source must remain loaded");
        assertEquals(0, fixture.tile.getInputLevel(2));
    }

    @Test
    void vanillaDustCannotReadUnloadedCoordinatesInsideItsPowerCallback() throws Exception {
        Fixture fixture = new Fixture();
        ClassLoader isolated = isolatedDustLoader();
        Class<?> registry = Class.forName("net.minecraft.init.Blocks", true, isolated);
        Block dust = (Block) Class.forName("net.minecraft.block.BlockRedstoneWire", true, isolated)
            .getConstructor()
            .newInstance();
        registry.getField("air")
            .set(null, fixture.world.air);
        registry.getField("redstone_wire")
            .set(null, dust);
        registry.getField("unpowered_repeater")
            .set(null, new InertRepeater());
        registry.getField("powered_repeater")
            .set(null, new InertRepeater());
        registry.getField("glowstone")
            .set(null, fixture.world.air);
        Class<?> accessType = Class.forName("ca.teamdman.sfm.common.util.LoadedRedstoneAccess", true, isolated);
        IBlockAccess access = (IBlockAccess) accessType.getConstructor()
            .newInstance();
        accessType.getMethod("attach", World.class, Runnable.class)
            .invoke(access, fixture.world, null);
        BlockPos north = POSITION.offset(ForgeDirection.NORTH);
        fixture.world.blocks.put(north, dust);
        fixture.world.metadata.put(north, 15);
        fixture.world.unloaded.add(north.offset(ForgeDirection.WEST));
        assertDoesNotThrow(() -> dust.isProvidingWeakPower(access, north.x, north.y, north.z, 2));
        assertEquals(
            false,
            accessType.getMethod("isComplete")
                .invoke(access));
        fixture.world.unloaded.clear();
        accessType.getMethod("reset")
            .invoke(access);
        assertDoesNotThrow(() -> dust.isProvidingWeakPower(access, north.x, north.y, north.z, 2));
        assertEquals(
            true,
            accessType.getMethod("isComplete")
                .invoke(access));
        accessType.getMethod("detach")
            .invoke(access);
    }

    @Test
    void providerFailureIsUnavailableDuringNeighborEventsAndExplicitQueriesStillReportTheFault() throws Exception {
        Fixture fixture = new Fixture();
        fixture.tile.refreshInputs();
        fixture.world.blocks.put(POSITION.offset(ForgeDirection.NORTH), new SignalBlock(false) {

            @Override
            public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
                // An invalid cast must not persuade SFM to retry using an unsafe raw World.
                return ((World) world).getBlockMetadata(x, y, z);
            }
        });
        assertDoesNotThrow(() -> fixture.block.onNeighborBlockChange(fixture.world, 0, 64, 0, fixture.block));
        assertEquals("ClassCastException", fixture.tile.getInputFailure());
        assertFalse(fixture.tile.isInputAvailable(ForgeDirection.NORTH.ordinal()));
        long epoch = fixture.tile.getSignalEpoch();
        assertThrows(ClassCastException.class, () -> fixture.tile.refreshInputs(() -> {}));
        assertEquals(epoch, fixture.tile.getSignalEpoch(), "A failed explicit query never publishes a partial sample");
    }

    @Test
    void restoredOutputsReassertOnOneScheduledLoadTickWithoutMakingQueriesWriteWorldState() throws Exception {
        Fixture fixture = new Fixture();
        fixture.tile.setOutput(Arrays.asList(ForgeDirection.NORTH), 15);
        fixture.world.notifications.clear();
        int dirty = fixture.world.dirty;
        fixture.tile.onChunkUnload();
        fixture.tile.onLoaded();
        assertEquals(1, fixture.world.scheduled);
        assertTrue(fixture.world.notifications.isEmpty());
        fixture.tile.refreshInputs(() -> {});
        assertTrue(fixture.world.notifications.isEmpty(), "Pure input queries never reassert output signals");
        fixture.block.updateTick(fixture.world, 0, 64, 0, null);
        assertEquals(1, fixture.world.notifications.size());
        assertEquals(dirty, fixture.world.dirty, "Reasserting an unchanged loaded output does not dirty NBT");
        fixture.block.updateTick(fixture.world, 0, 64, 0, null);
        assertEquals(1, fixture.world.notifications.size());
    }

    @Test
    void outputFeedbackThroughSolidNeighborIsObservedWithoutPortRecursion() throws Exception {
        Fixture fixture = new Fixture();
        fixture.world.blocks.put(POSITION.offset(ForgeDirection.NORTH), new SignalBlock(true));
        fixture.tile.refreshInputs();
        assertTrue(fixture.tile.setOutput(Arrays.asList(ForgeDirection.NORTH), 15));
        assertEquals(15, fixture.tile.getInputLevel(ForgeDirection.NORTH.ordinal()));
        assertEquals(1, fixture.tile.getRisingCount(ForgeDirection.NORTH.ordinal()));
        assertEquals(0, fixture.tile.getInputLevel(ForgeDirection.SOUTH.ordinal()));
    }

    @Test
    void notificationsGuardDirectAndStrongConductionTargetsAndRemovalDropsPower() throws Exception {
        Fixture fixture = new Fixture();
        BlockPos north = POSITION.offset(ForgeDirection.NORTH);
        fixture.world.blocks.put(north, new SignalBlock(true));
        fixture.world.unloaded.add(north.offset(ForgeDirection.NORTH));
        assertTrue(fixture.tile.setOutput(Arrays.asList(ForgeDirection.NORTH), 15));
        assertTrue(fixture.world.notifications.contains(north.offset(ForgeDirection.EAST)));
        assertFalse(fixture.world.notifications.contains(north.offset(ForgeDirection.NORTH)));
        fixture.world.notifications.clear();
        fixture.world.onNotify = () -> assertNull(fixture.world.tiles.get(POSITION));
        fixture.block.breakBlock(fixture.world, 0, 64, 0, fixture.block, 0);
        assertEquals(0, fixture.tile.getOutputLevel(2));
        assertFalse(fixture.world.notifications.isEmpty());
    }

    @Test
    void nbtSavesOnlySanitizedOutputsAndDescriptionPacketCannotMutateServer() throws Exception {
        TileEntity.addMapping(TileEntityRedstonePort.class, "sfm.redstonePort");
        Fixture fixture = new Fixture();
        fixture.tile.setOutput(Arrays.asList(ForgeDirection.UP), 15);
        fixture.tile.refreshInputs();
        NBTTagCompound saved = new NBTTagCompound();
        fixture.tile.writeToNBT(saved);
        assertEquals(
            5,
            saved.func_150296_c()
                .size(),
            "ID, coordinates and six output levels only");
        TileEntityRedstonePort loaded = new TileEntityRedstonePort();
        long epoch = loaded.getSignalEpoch();
        loaded.readFromNBT(saved);
        assertEquals(15, loaded.getOutputLevel(1));
        assertNotEquals(epoch, loaded.getSignalEpoch());
        assertFalse(loaded.isInputInitialized());
        saved.setIntArray("sfm:outputs", new int[] { -99, 16, 7, 0, 15, Integer.MAX_VALUE });
        loaded.readFromNBT(saved);
        assertEquals(0, loaded.getOutputLevel(0));
        assertEquals(15, loaded.getOutputLevel(1));
        assertEquals(7, loaded.getOutputLevel(2));
        assertEquals(15, loaded.getOutputLevel(5));
        saved.setIntArray("sfm:outputs", new int[] { 15 });
        loaded.readFromNBT(saved);
        for (int side = 0; side < 6; side++) assertEquals(0, loaded.getOutputLevel(side));
        S35PacketUpdateTileEntity description = (S35PacketUpdateTileEntity) fixture.tile.getDescriptionPacket();
        assertArrayEquals(
            new int[] { 0, 15, 0, 0, 0, 0 },
            description.func_148857_g()
                .getIntArray("sfm:outputs"));
        fixture.tile.onDataPacket(null, new S35PacketUpdateTileEntity(0, 64, 0, 1, saved));
        assertEquals(15, fixture.tile.getOutputLevel(1));
    }

    @Test
    void budgetFailureDoesNotPublishPartialInputSampleOrCountersAndChargedCallsAreBounded() throws Exception {
        Fixture fixture = new Fixture();
        fixture.tile.refreshInputs();
        long epoch = fixture.tile.getSignalEpoch();
        fixture.neighbor(ForgeDirection.DOWN).weak[0] = 15;
        AtomicInteger calls = new AtomicInteger();
        assertThrows(
            IllegalStateException.class,
            () -> fixture.tile.refreshInputs(
                () -> { if (calls.incrementAndGet() == 10) throw new IllegalStateException("budget"); }));
        assertEquals(0, fixture.tile.getInputLevel(0));
        assertEquals(0, fixture.tile.getRisingCount(0));
        assertEquals(epoch, fixture.tile.getSignalEpoch());
        calls.set(0);
        fixture.tile.refreshInputs(calls::incrementAndGet);
        assertEquals(15, fixture.tile.getInputLevel(0));
        assertEquals(1, fixture.tile.getRisingCount(0));
        assertTrue(calls.get() <= TileEntityRedstonePort.MAX_INPUT_PROVIDER_CALLS);
        for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS)
            fixture.world.blocks.put(POSITION.offset(face), new SignalBlock(true));
        calls.set(0);
        fixture.tile.refreshInputs(calls::incrementAndGet);
        assertTrue(calls.get() <= TileEntityRedstonePort.MAX_INPUT_PROVIDER_CALLS);
    }

    @Test
    void counterOverflowChangesEpochRatherThanWrappingOrCreatingAnEdge() throws Exception {
        Fixture fixture = new Fixture();
        fixture.tile.refreshInputs();
        Field field = TileEntityRedstonePort.class.getDeclaredField("rising");
        field.setAccessible(true);
        ((long[]) field.get(fixture.tile))[2] = Long.MAX_VALUE;
        long epoch = fixture.tile.getSignalEpoch();
        fixture.neighbor(ForgeDirection.NORTH).weak[2] = 15;
        fixture.tile.refreshInputs();
        assertNotEquals(epoch, fixture.tile.getSignalEpoch());
        assertEquals(0, fixture.tile.getRisingCount(2));
        assertEquals(15, fixture.tile.getInputLevel(2));
    }

    @Test
    void staleReplacedOrOffThreadTilesCannotReadWorldOrChangeOutputs() throws Exception {
        Fixture fixture = new Fixture();
        int reads = fixture.world.reads;
        AtomicBoolean changed = new AtomicBoolean(true);
        Thread worker = new Thread(() -> {
            fixture.tile.refreshInputs();
            changed.set(fixture.tile.setOutput(null, 15));
        });
        worker.start();
        worker.join();
        assertFalse(changed.get());
        assertEquals(reads, fixture.world.reads);
        fixture.world.tiles.put(POSITION, new TileEntityRedstonePort());
        fixture.tile.refreshInputs();
        assertFalse(fixture.tile.isInputInitialized());
        assertFalse(fixture.tile.setOutput(null, 15));
        assertEquals(0, fixture.world.dirty);
    }

    private static final class Fixture {

        final BlockRedstonePort block = new BlockRedstonePort();
        final TileEntityRedstonePort tile = new TileEntityRedstonePort();
        final PortWorld world = AuditRegressionTest.allocate(PortWorld.class);

        Fixture() throws Exception {
            world.blocks = new HashMap<>();
            world.metadata = new HashMap<>();
            world.tiles = new HashMap<>();
            world.unloaded = new HashSet<>();
            world.notifications = new ArrayList<>();
            world.air = new SignalBlock(false);
            world.blocks.put(POSITION, block);
            world.tiles.put(POSITION, tile);
            tile.setWorldObj(world);
            tile.xCoord = POSITION.x;
            tile.yCoord = POSITION.y;
            tile.zCoord = POSITION.z;
            for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS)
                world.blocks.put(POSITION.offset(face), new SignalBlock(false));
        }

        SignalBlock neighbor(ForgeDirection face) {
            return (SignalBlock) world.blocks.get(POSITION.offset(face));
        }
    }

    /**
     * The ordinary test JVM has no vanilla registry. Isolate only registry fields, keeping vanilla dust bytecode
     * intact.
     */
    private static ClassLoader isolatedDustLoader() {
        return new ClassLoader(RedstonePortTest.class.getClassLoader()) {

            @Override
            protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                boolean local = name.equals("net.minecraft.init.Blocks")
                    || name.equals("net.minecraft.block.BlockRedstoneWire")
                    || name.equals("ca.teamdman.sfm.common.util.LoadedRedstoneAccess");
                if (!local) return super.loadClass(name, resolve);
                Class<?> loaded = findLoadedClass(name);
                if (loaded == null) {
                    byte[] code;
                    if (name.equals("net.minecraft.init.Blocks")) {
                        ClassWriter writer = new ClassWriter(0);
                        writer.visit(
                            Opcodes.V1_8,
                            Opcodes.ACC_PUBLIC,
                            "net/minecraft/init/Blocks",
                            null,
                            "java/lang/Object",
                            null);
                        for (String[] field : new String[][] { { "air", "Block" }, { "glowstone", "Block" },
                            { "redstone_wire", "BlockRedstoneWire" }, { "unpowered_repeater", "BlockRedstoneRepeater" },
                            { "powered_repeater", "BlockRedstoneRepeater" } })
                            writer
                                .visitField(
                                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                                    field[0],
                                    "Lnet/minecraft/block/" + field[1] + ";",
                                    null,
                                    null)
                                .visitEnd();
                        writer.visitEnd();
                        code = writer.toByteArray();
                    } else
                        try (InputStream input = getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
                            if (input == null) throw new ClassNotFoundException(name);
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            byte[] buffer = new byte[4096];
                            int count;
                            while ((count = input.read(buffer)) != -1) bytes.write(buffer, 0, count);
                            code = bytes.toByteArray();
                        } catch (java.io.IOException error) {
                            throw new ClassNotFoundException(name, error);
                        }
                    loaded = defineClass(name, code, 0, code.length);
                }
                if (resolve) resolveClass(loaded);
                return loaded;
            }
        };
    }

    private static final class InertRepeater extends BlockRedstoneRepeater {

        InertRepeater() {
            super(false);
        }

        @Override
        public boolean func_149907_e(Block block) {
            return false;
        }
    }

    private static class SignalBlock extends Block {

        final int[] weak = new int[6];
        final int[] strong = new int[6];
        final boolean conducts;

        SignalBlock(boolean conducts) {
            super(Material.rock);
            this.conducts = conducts;
        }

        @Override
        public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side) {
            return conducts;
        }

        @Override
        public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
            return weak[side];
        }

        @Override
        public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side) {
            return strong[side];
        }
    }

    private static final class PortWorld extends World {

        Map<BlockPos, Block> blocks;
        Map<BlockPos, Integer> metadata;
        Map<BlockPos, TileEntity> tiles;
        Set<BlockPos> unloaded;
        List<BlockPos> notifications;
        Block air;
        Runnable onNotify;
        int dirty;
        int updates;
        int reads;
        int scheduled;

        private PortWorld() {
            super((ISaveHandler) null, "redstone", (WorldSettings) null, (WorldProvider) null, (Profiler) null);
        }

        @Override
        protected IChunkProvider createChunkProvider() {
            throw new AssertionError("No chunk provider is used by the port");
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
        public boolean blockExists(int x, int y, int z) {
            return y >= 0 && y < 256 && !unloaded.contains(new BlockPos(x, y, z));
        }

        private void check(int x, int y, int z) {
            assertTrue(blockExists(x, y, z), "Port must never access an unloaded position");
        }

        @Override
        public Block getBlock(int x, int y, int z) {
            check(x, y, z);
            reads++;
            return blocks.getOrDefault(new BlockPos(x, y, z), air);
        }

        @Override
        public TileEntity getTileEntity(int x, int y, int z) {
            check(x, y, z);
            reads++;
            return tiles.get(new BlockPos(x, y, z));
        }

        @Override
        public int getBlockMetadata(int x, int y, int z) {
            check(x, y, z);
            return metadata.getOrDefault(new BlockPos(x, y, z), 0);
        }

        @Override
        public void markTileEntityChunkModified(int x, int y, int z, TileEntity tile) {
            check(x, y, z);
            dirty++;
        }

        @Override
        public void markBlockForUpdate(int x, int y, int z) {
            check(x, y, z);
            updates++;
        }

        @Override
        public void notifyBlockOfNeighborChange(int x, int y, int z, Block block) {
            check(x, y, z);
            notifications.add(new BlockPos(x, y, z));
            if (onNotify != null) onNotify.run();
        }

        @Override
        public void removeTileEntity(int x, int y, int z) {
            check(x, y, z);
            tiles.remove(new BlockPos(x, y, z));
        }

        @Override
        public void scheduleBlockUpdate(int x, int y, int z, Block block, int delay) {
            check(x, y, z);
            scheduled++;
        }
    }
}
