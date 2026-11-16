package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.block.BlockCable;
import ca.teamdman.sfm.common.block.BlockWirelessConnector;
import ca.teamdman.sfm.common.item.ItemWirelessLinker;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.network.CableNetworkDiscovery;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityWirelessConnector;
import ca.teamdman.sfm.common.util.BlockPos;
import cpw.mods.fml.common.gameevent.TickEvent;

class WirelessConnectorTest {

    private static final BlockPos FIRST = new BlockPos(1, 64, 0), SECOND = new BlockPos(1000, 64, 0),
        REMOTE_CABLE = new BlockPos(1001, 64, 0), REMOTE_INVENTORY = new BlockPos(1002, 64, 0);
    private final BlockWirelessConnector block = new BlockWirelessConnector();
    private TestWorld world;
    private TileEntityWirelessConnector first, second;

    @BeforeAll
    static void registerTile() {
        TileEntity.addMapping(TileEntityWirelessConnector.class, "sfm.wirelessConnector");
    }

    @BeforeEach
    void setup() throws Exception {
        ServerUiTasks.clear();
        ServerUiTasks.INSTANCE.tick(new TickEvent.ServerTickEvent(TickEvent.Phase.START));
        world = newWorld();
        first = connector(FIRST);
        second = connector(SECOND);
        world.blocks.put(REMOTE_CABLE, new BlockCable());
        world.tiles.put(REMOTE_INVENTORY, new InventoryTile(3));
    }

    @AfterEach
    void cleanup() {
        ServerUiTasks.clear();
    }

    @Test
    void linkingMakesBothCableBranchesOneNetworkAndTransfersWithoutNewSyntax() {
        CableNetwork network = new CableNetwork(world, MANAGER);
        assertFalse(network.isConnected(REMOTE_INVENTORY));
        assertTrue(first.linkTo(second));
        assertTrue(network.isConnected(REMOTE_CABLE));
        assertTrue(network.isConnected(REMOTE_INVENTORY));
        assertTrue(new CableNetwork(world, SECOND.offset(0, 0, -1)).isConnected(FIRST));

        InventoryTile source = new InventoryTile(3);
        source.setInventorySlotContents(0, new ItemStack(ITEM, 12));
        BlockPos sourcePosition = new BlockPos(0, 65, 0);
        world.tiles.put(sourcePosition, source);
        CableNetwork.invalidate(world, sourcePosition.x, sourcePosition.y, sourcePosition.z);
        LabelPositionHolder labels = new LabelPositionHolder();
        labels.addPosition("source", sourcePosition);
        labels.addPosition("destination", REMOTE_INVENTORY);
        parse("every 20 ticks {input from \"source\" output 10 to \"destination\"}")
            .tick(new ProgramContext(world, null, network, labels, 20, false));
        assertEquals(2, count(source));
        assertEquals(10, count((InventoryTile) world.tiles.get(REMOTE_INVENTORY)));
    }

    @Test
    void discoveryIncludesWirelessBranchesAndFencesEditsAtTheRemoteEnd() {
        assertTrue(first.linkTo(second));
        CableNetworkDiscovery scan = CableNetworkDiscovery.fromCable(world, FIRST);
        assertEquals(CableNetworkDiscovery.Status.COMPLETE, scan.advance());
        CableNetworkDiscovery.Snapshot snapshot = scan.getSnapshot();
        assertTrue(snapshot.cables.contains(SECOND));
        assertTrue(snapshot.peripherals.contains(REMOTE_INVENTORY));
        second.unlink();
        assertFalse(snapshot.isCurrent());
        assertNull(first.getConnectedPeerPosition());
        assertNull(second.getPeerPosition());
    }

    @Test
    void unloadedPeerIsNeverReadAndItsReloadInvalidatesOfflineSnapshots() {
        first.linkTo(second);
        CableNetwork network = new CableNetwork(world, MANAGER);
        long before = network.getTopologyGeneration();
        world.unloaded.add(SECOND);
        second.onChunkUnload();
        assertFalse(network.isConnected(REMOTE_INVENTORY));
        assertTrue(network.getTopologyGeneration() > before);
        CableNetworkDiscovery scan = CableNetworkDiscovery.fromCable(world, FIRST);
        assertEquals(CableNetworkDiscovery.Status.COMPLETE, scan.advance());
        CableNetworkDiscovery.Snapshot snapshot = scan.getSnapshot();
        assertTrue(snapshot.unloaded.contains(SECOND));

        world.unloaded.clear();
        CableNetwork.invalidateChunk(world, SECOND.x >> 4, SECOND.z >> 4);
        assertFalse(snapshot.isCurrent());
        assertTrue(network.isConnected(REMOTE_INVENTORY));
    }

    @Test
    void saveReloadPreservesThePairButReplacementAtTheSamePositionDoesNot() {
        first.linkTo(second);
        NBTTagCompound a = save(first), b = save(second);
        first = connector(FIRST);
        second = connector(SECOND);
        first.readFromNBT(a);
        second.readFromNBT(b);
        assertEquals(SECOND, first.getConnectedPeerPosition());
        assertEquals(FIRST, second.getConnectedPeerPosition());
        second = connector(SECOND);
        assertNull(first.getConnectedPeerPosition());
        assertNull(second.getConnectedPeerPosition());
    }

    @Test
    void rePairingDisconnectsBothFormerPartnersAndOldOfflineLinksCannotReappear() {
        TileEntityWirelessConnector third = connector(new BlockPos(2000, 64, 0)),
            fourth = connector(new BlockPos(3000, 64, 0));
        first.linkTo(second);
        third.linkTo(fourth);
        world.unloaded.add(SECOND);
        assertTrue(first.linkTo(third));
        world.unloaded.clear();
        assertEquals(third.position(), first.getConnectedPeerPosition());
        assertNull(second.getConnectedPeerPosition());
        assertNull(fourth.getPeerPosition());
        second.unlink();
        assertEquals(third.position(), first.getConnectedPeerPosition());
    }

    @Test
    void physicalCableLoopsAndWirelessCyclesAreVisitedOnce() {
        TileEntityWirelessConnector nearby = connector(new BlockPos(3, 64, 0));
        world.blocks.put(new BlockPos(2, 64, 0), new BlockCable());
        first.linkTo(nearby);
        CableNetwork network = new CableNetwork(world, MANAGER);
        assertEquals(
            3,
            network.getCables()
                .size());
        assertTrue(world.blockReads < 100);
        int reads = world.blockReads;
        for (int i = 0; i < 100; i++) assertTrue(network.isConnected(nearby.position()));
        assertEquals(reads, world.blockReads, "Warm lookups must not rescan wireless links");
    }

    @Test
    void toolSelectsWithoutChangingLinksThenPairsAndSneakUnlinks() throws Exception {
        ItemWirelessLinker item = new ItemWirelessLinker();
        ItemStack tool = new ItemStack(item);
        TestPlayer player = allocate(TestPlayer.class);
        use(item, tool, player, FIRST);
        assertNull(first.getPeerPosition());
        assertNotNull(first.getIdentity());
        use(item, tool, player, SECOND);
        assertEquals(SECOND, first.getConnectedPeerPosition());
        player.sneaking = true;
        use(item, tool, player, FIRST);
        assertNull(first.getPeerPosition());
        assertNull(second.getPeerPosition());
    }

    @Test
    void toolRejectsReplacedSelectionsAndRechecksPermissionOnTheFirstEndpoint() throws Exception {
        ItemWirelessLinker item = new ItemWirelessLinker();
        ItemStack tool = new ItemStack(item);
        TestPlayer player = allocate(TestPlayer.class);
        use(item, tool, player, FIRST);
        player.denied = FIRST;
        use(item, tool, player, SECOND);
        assertNull(first.getPeerPosition());
        assertNull(second.getPeerPosition());
        player.denied = null;
        first = connector(FIRST);
        use(item, tool, player, SECOND);
        assertNull(first.getPeerPosition());
        assertNull(second.getPeerPosition());
        assertFalse(
            tool.getTagCompound()
                .hasKey("sfm:wirelessSelection"));
    }

    @Test
    void noCrossWorldPairsAndNoMutationsOnAWorkerThread() throws Exception {
        TestWorld otherWorld = newWorld();
        TileEntityWirelessConnector other = new TileEntityWirelessConnector();
        other.setWorldObj(otherWorld);
        other.xCoord = FIRST.x;
        other.yCoord = FIRST.y;
        other.zCoord = FIRST.z;
        otherWorld.blocks.put(FIRST, block);
        otherWorld.tiles.put(FIRST, other);
        assertFalse(first.linkTo(other));
        java.util.concurrent.atomic.AtomicReference<Throwable> failure = new java.util.concurrent.atomic.AtomicReference<>();
        Thread worker = new Thread(() -> {
            try {
                first.linkTo(second);
            } catch (Throwable error) {
                failure.set(error);
            }
        });
        worker.start();
        worker.join();
        assertInstanceOf(IllegalStateException.class, failure.get());
        assertNull(first.getPeerPosition());
    }

    private void use(ItemWirelessLinker item, ItemStack stack, TestPlayer player, BlockPos position) {
        assertTrue(item.onItemUse(stack, player, world, position.x, position.y, position.z, 1, 0, 0, 0));
    }

    private TileEntityWirelessConnector connector(BlockPos position) {
        TileEntityWirelessConnector connector = new TileEntityWirelessConnector();
        connector.setWorldObj(world);
        connector.xCoord = position.x;
        connector.yCoord = position.y;
        connector.zCoord = position.z;
        world.blocks.put(position, block);
        world.tiles.put(position, connector);
        CableNetwork.invalidate(world, position.x, position.y, position.z);
        return connector;
    }

    private static NBTTagCompound save(TileEntityWirelessConnector connector) {
        NBTTagCompound tag = new NBTTagCompound();
        connector.writeToNBT(tag);
        return tag;
    }

    private static TestWorld newWorld() throws Exception {
        TestWorld world = allocate(TestWorld.class);
        world.blocks = new HashMap<>();
        world.tiles = new HashMap<>();
        world.unloaded = new HashSet<>();
        WorldProviderSurface provider = new WorldProviderSurface();
        provider.worldObj = world;
        Field field = World.class.getDeclaredField("provider");
        field.setAccessible(true);
        field.set(world, provider);
        return world;
    }

    private static final class TestPlayer extends EntityPlayer {

        boolean sneaking;
        BlockPos denied;

        private TestPlayer() {
            super(null, null);
        }

        @Override
        public boolean isSneaking() {
            return sneaking;
        }

        @Override
        public boolean canPlayerEdit(int x, int y, int z, int side, ItemStack item) {
            return !new BlockPos(x, y, z).equals(denied);
        }

        @Override
        public void addChatMessage(IChatComponent text) {}

        @Override
        public boolean canCommandSenderUseCommand(int level, String command) {
            return false;
        }

        @Override
        public net.minecraft.util.ChunkCoordinates getPlayerCoordinates() {
            return new net.minecraft.util.ChunkCoordinates(0, 64, 0);
        }
    }

    private static final class TestWorld extends World {

        Map<BlockPos, Block> blocks;
        Map<BlockPos, TileEntity> tiles;
        Set<BlockPos> unloaded;
        int blockReads;

        private TestWorld() {
            super((ISaveHandler) null, "wireless", (WorldSettings) null, (WorldProvider) null, (Profiler) null);
        }

        @Override
        public long getTotalWorldTime() {
            return 100;
        }

        @Override
        public boolean blockExists(int x, int y, int z) {
            return y >= 0 && y < 256 && !unloaded.contains(new BlockPos(x, y, z));
        }

        @Override
        public Block getBlock(int x, int y, int z) {
            assertTrue(blockExists(x, y, z), "Read from an unloaded position");
            blockReads++;
            return blocks.get(new BlockPos(x, y, z));
        }

        @Override
        public TileEntity getTileEntity(int x, int y, int z) {
            assertTrue(blockExists(x, y, z), "Read from an unloaded position");
            return tiles.get(new BlockPos(x, y, z));
        }

        @Override
        public int getBlockMetadata(int x, int y, int z) {
            assertTrue(blockExists(x, y, z), "Read from an unloaded position");
            return 0;
        }

        @Override
        public void markTileEntityChunkModified(int x, int y, int z, TileEntity tile) {}

        @Override
        public void func_147453_f(int x, int y, int z, Block block) {}

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
    }
}
