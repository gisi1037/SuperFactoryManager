package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.block.BlockCable;
import ca.teamdman.sfm.common.gui.ContainerManager;
import ca.teamdman.sfm.common.inventory.InventoryUtils;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.net.ManagerAccess;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.ast.NumberRangeSet;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.ast.ResourceIdSet;
import ca.teamdman.sfml.ast.RoundRobin;
import ca.teamdman.sfml.ast.SideQualifier;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;
import sun.misc.Unsafe;

public class AuditRegressionTest {

    static final Item ITEM = new Item();
    static final Item OTHER = new Item();
    static final BlockPos SRC = new BlockPos(1, 64, 0);
    static final BlockPos DST = new BlockPos(-1, 64, 0);
    static final BlockPos EXTRA = new BlockPos(0, 64, 1);
    static final BlockPos MANAGER = new BlockPos(0, 64, 0);

    static <T> T allocate(Class<T> type) throws Exception {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return type.cast(((Unsafe) field.get(null)).allocateInstance(type));
    }

    static FakeWorld world() throws Exception {
        FakeWorld world = allocate(FakeWorld.class);
        world.tiles = new HashMap<>();
        world.blocks = new HashMap<>();
        world.unloaded = new HashSet<>();
        world.time = 100;
        return world;
    }

    static Program parse(String text) {
        ProgramBuildResult result = new ProgramBuilder(text).build();
        assertTrue(
            result.isSuccess(),
            result.getErrors()
                .toString());
        return result.getProgram();
    }

    static class Fixture {

        final FakeWorld world = world();
        final LabelPositionHolder labels = new LabelPositionHolder();
        final InventoryTile source = new InventoryTile(3);
        final InventoryTile dest = new InventoryTile(3);

        Fixture() throws Exception {
            add("source", SRC, source);
            add("dest", DST, dest);
        }

        void add(String label, BlockPos pos, TileEntity tile) {
            world.tiles.put(pos, tile);
            labels.addPosition(label, pos);
        }

        void run(String statements) {
            runProgram("EVERY 20 TICKS DO " + statements + " END");
        }

        void runProgram(String code) {
            parse(code).tick(new ProgramContext(world, null, new CableNetwork(world, MANAGER), labels, 20, false));
        }

        void stock(int slot, int amount) {
            source.setInventorySlotContents(slot, new ItemStack(ITEM, amount));
        }
    }

    static int count(IInventory inv) {
        int total = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack s = inv.getStackInSlot(i);
            if (s != null) total += s.stackSize;
        }
        return total;
    }

    @Test
    void outputQuantityCapsAcrossSlots() throws Exception {
        Fixture f = new Fixture();
        f.stock(0, 64);
        f.stock(1, 64);
        f.run("INPUT FROM source OUTPUT 1 TO dest");
        assertEquals(1, count(f.dest));
        assertEquals(127, count(f.source));
    }

    @Test
    void outputEachCapsEachDestination() throws Exception {
        Fixture f = new Fixture();
        f.stock(0, 64);
        InventoryTile d2 = new InventoryTile(2);
        f.add("dest", EXTRA, d2);
        f.run("INPUT FROM source OUTPUT 5 TO EACH dest");
        assertEquals(5, count(f.dest));
        assertEquals(5, count(d2));
    }

    @Test
    void sharedOutputQuantityIsNotPerDestination() throws Exception {
        Fixture f = new Fixture();
        f.stock(0, 64);
        InventoryTile d2 = new InventoryTile(2);
        f.add("dest", EXTRA, d2);
        f.run("INPUT FROM source OUTPUT 5 TO dest");
        assertEquals(5, count(f.dest) + count(d2));
    }

    @Test
    void inputEachCapsEachSource() throws Exception {
        Fixture f = new Fixture();
        f.stock(0, 64);
        InventoryTile s2 = new InventoryTile(2);
        s2.setInventorySlotContents(0, new ItemStack(ITEM, 64));
        f.add("source", EXTRA, s2);
        f.run("INPUT 3 FROM EACH source OUTPUT TO dest");
        assertEquals(6, count(f.dest));
    }

    @Test
    void inputRetentionIsSharedAcrossSlots() throws Exception {
        Fixture f = new Fixture();
        f.stock(0, 20);
        f.stock(1, 20);
        f.run("INPUT RETAIN 25 FROM source OUTPUT TO dest");
        assertEquals(25, count(f.source));
        assertEquals(15, count(f.dest));
    }

    @Test
    void outputRetentionTopsUpExistingContents() throws Exception {
        Fixture f = new Fixture();
        f.stock(0, 64);
        f.dest.setInventorySlotContents(0, new ItemStack(ITEM, 7));
        f.run("INPUT FROM source OUTPUT RETAIN 10 TO dest");
        assertEquals(10, count(f.dest));
        assertEquals(61, count(f.source));
    }

    @Test
    void explicitZeroOutputRetentionTransfersNothing() throws Exception {
        Fixture f = new Fixture();
        f.stock(0, 64);
        f.run("INPUT FROM source OUTPUT RETAIN 0 TO dest");
        assertEquals(0, count(f.dest));
    }

    @Test
    void eachQuantityIsPerResource() throws Exception {
        Fixture f = new Fixture();
        f.stock(0, 20);
        f.source.setInventorySlotContents(1, new ItemStack(OTHER, 20));
        f.run("INPUT FROM source OUTPUT 3 EACH TO dest");
        assertEquals(6, count(f.dest));
    }

    @Test
    void inputQuotaPersistsAcrossOutputs() throws Exception {
        Fixture f = new Fixture();
        f.stock(0, 64);
        f.run("INPUT 5 FROM source OUTPUT 2 TO dest OUTPUT TO dest");
        assertEquals(5, count(f.dest));
    }

    @Test
    void unconsumedInputsRemainForLaterOutput() throws Exception {
        Fixture f = new Fixture();
        f.stock(0, 64);
        f.run("INPUT FROM source OUTPUT 1 TO dest OUTPUT 2 TO dest");
        assertEquals(3, count(f.dest));
    }

    @Test
    void triggersDoNotShareInputs() throws Exception {
        Fixture f = new Fixture();
        f.stock(0, 64);
        f.runProgram("EVERY 20 TICKS DO INPUT FROM source END EVERY 20 TICKS DO OUTPUT TO dest END");
        assertEquals(0, count(f.dest));
    }

    @Test
    void forgetPreventsLaterTransfer() throws Exception {
        Fixture f = new Fixture();
        f.stock(0, 64);
        f.run("INPUT FROM source FORGET source OUTPUT TO dest");
        assertEquals(0, count(f.dest));
    }

    @Test
    void sameSlotDoesNotConsumeQuota() throws Exception {
        Fixture f = new Fixture();
        f.stock(0, 60);
        f.labels.addPosition("both", SRC);
        f.labels.addPosition("both", DST);
        f.run("INPUT FROM source SLOTS 0 OUTPUT 5 TO both SLOTS 0");
        assertEquals(5, count(f.dest));
        assertEquals(55, count(f.source));
    }

    @Test
    void duplicateLabelsDoNotDuplicateRetentionOrQuota() throws Exception {
        Fixture f = new Fixture();
        f.stock(0, 64);
        f.labels.addPosition("alias", SRC);
        f.run("INPUT 5 FROM EACH source, alias OUTPUT TO dest");
        assertEquals(5, count(f.dest));
    }

    @Test
    void declinedExtractionDoesNotFabricateItems() {
        InventoryBasic inv = new InventoryBasic("refuse", false, 1) {

            @Override
            public ItemStack decrStackSize(int s, int n) {
                return null;
            }
        };
        inv.setInventorySlotContents(0, new ItemStack(ITEM, 20));
        assertNull(InventoryUtils.extractItem(inv, 0, ForgeDirection.UNKNOWN, 10, false));
        assertEquals(20, count(inv));
    }

    @Test
    void partialExtractionDoesNotDuplicateItems() throws Exception {
        Fixture f = new Fixture();
        InventoryTile partial = new InventoryTile(1) {

            @Override
            public ItemStack decrStackSize(int slot, int n) {
                return super.decrStackSize(slot, Math.min(n, 2));
            }
        };
        partial.setInventorySlotContents(0, new ItemStack(ITEM, 20));
        f.world.tiles.put(SRC, partial);
        f.run("INPUT FROM source OUTPUT 8 TO dest");
        assertEquals(20, count(partial) + count(f.dest));
        assertTrue(count(f.dest) <= 8);
    }

    @Test
    void rejectedCommitRollsBackExtraction() throws Exception {
        Fixture f = new Fixture();
        f.stock(0, 20);
        InventoryTile rejecting = new InventoryTile(1) {

            int checks;

            @Override
            public boolean isItemValidForSlot(int slot, ItemStack s) {
                return ++checks == 1;
            }
        };
        f.world.tiles.put(DST, rejecting);
        f.run("INPUT FROM source OUTPUT 10 TO dest");
        assertEquals(20, count(f.source));
        assertEquals(0, count(rejecting));
    }

    @Test
    void invalidAndDuplicateSidedSlotsAreFiltered() {
        SidedTile inv = new SidedTile();
        inv.slots = new int[] { -1, 0, 0, 999 };
        assertArrayEquals(
            new int[] { 0 },
            InventoryUtils.getAccessibleSlots(inv, ForgeDirection.UP, NumberRangeSet.ALL));
    }

    @Test
    void sidedOverlapDoesNotMultiplyRetention() throws Exception {
        Fixture f = new Fixture();
        SidedTile inv = new SidedTile();
        inv.setInventorySlotContents(0, new ItemStack(ITEM, 40));
        f.world.tiles.put(SRC, inv);
        f.run("INPUT RETAIN 10 FROM source OUTPUT TO dest");
        assertEquals(30, count(f.dest));
        assertEquals(10, count(inv));
    }

    @Test
    void unsupportedSidesFailClosed() {
        for (String side : Arrays.asList("LEFT", "RIGHT", "FRONT", "BACK")) assertFalse(
            new ProgramBuilder("EVERY 20 TICKS DO INPUT FROM source " + side + " SIDE END").build()
                .isSuccess());
        assertEquals(Collections.singletonList(ForgeDirection.UNKNOWN), SideQualifier.NULL.resolveDirections());
    }

    @Test
    void unsupportedWithExpressionFailsClosed() {
        assertFalse(
            new ProgramBuilder("EVERY 20 TICKS DO INPUT WITH (TAG iron AND TAG gold) FROM source END").build()
                .isSuccess());
    }

    @Test
    void overflowingAndZeroIntervalsFailInsteadOfRunningEachTick() {
        for (String interval : Arrays.asList("0 TICKS", "2147483647 SECONDS", "999999999999999 TICKS")) assertFalse(
            new ProgramBuilder("EVERY " + interval + " DO END").build()
                .isSuccess());
    }

    @Test
    void roundRobinRemainsValidAtIntegerOverflow() throws Exception {
        RoundRobin rr = new RoundRobin(RoundRobin.Mode.BY_BLOCK);
        Field f = RoundRobin.class.getDeclaredField("counter");
        f.setAccessible(true);
        f.setInt(rr, Integer.MIN_VALUE);
        for (int i = 0; i < 20; i++) {
            int n = rr.nextIndex(3);
            assertTrue(n >= 0 && n < 3);
        }
    }

    @Test
    void unloadedPositionsAreNeverRead() throws Exception {
        Fixture f = new Fixture();
        f.stock(0, 20);
        f.world.unloaded.add(SRC);
        f.run("INPUT FROM source OUTPUT TO dest");
        assertEquals(0, count(f.dest));
    }

    @Test
    void warmNetworkDoesNotRescanPerLookup() throws Exception {
        FakeWorld w = world();
        Block cable = new BlockCable();
        for (int i = 1; i <= 1000; i++) w.blocks.put(new BlockPos(i, 64, 0), cable);
        CableNetwork net = new CableNetwork(w, MANAGER);
        int reads = w.blockReads;
        for (int i = 0; i < 10000; i++) net.isConnected(new BlockPos(1000, 64, 0));
        assertEquals(reads, w.blockReads);
        w.time += 21;
        net.isConnected(new BlockPos(1000, 64, 0));
        assertTrue(w.blockReads > reads);
    }

    @Test
    void disconnectedHasDoesNotReadRemoteInventory() throws Exception {
        Fixture f = new Fixture();
        f.stock(0, 20);
        BlockPos remote = new BlockPos(100, 64, 100);
        InventoryTile inv = new InventoryTile(1);
        inv.setInventorySlotContents(0, new ItemStack(ITEM, 64));
        f.add("remote", remote, inv);
        f.run("INPUT FROM source IF remote HAS > 0 THEN OUTPUT TO dest END");
        assertEquals(0, count(f.dest));
    }

    @Test
    void resetPersistsLabelRemovalOnDisk() {
        TileEntityManager m = new TileEntityManager();
        ItemStack disk = new ItemStack(new ItemDisk());
        LabelPositionHolder h = new LabelPositionHolder();
        h.addPosition("source", SRC);
        LabelPositionHolder.saveTo(disk, h);
        m.setInventorySlotContents(0, disk);
        m.reset();
        assertTrue(
            LabelPositionHolder.from(disk)
                .getAllLabels()
                .isEmpty());
    }

    @Test
    void zeroDiskExtractionDoesNotRemoveDisk() {
        TileEntityManager m = new TileEntityManager();
        ItemStack disk = new ItemStack(new ItemDisk());
        m.setInventorySlotContents(0, disk);
        assertNull(m.decrStackSize(0, 0));
        assertSame(disk, m.getDisk());
    }

    @Test
    void malformedTimingIndexDoesNotCrashGraph() {
        TileEntityManager m = new TileEntityManager();
        NBTTagCompound n = new NBTTagCompound();
        n.setInteger("tt_idx", Integer.MIN_VALUE);
        m.readFromNBT(n);
        assertEquals(20, m.getTickTimes().length);
    }

    @Test
    void disabledDiagnosticsDoNotReadWorld() throws Exception {
        FakeWorld w = world();
        TileEntityManager m = new TileEntityManager();
        m.setWorldObj(w);
        m.logNetworkDiagnostics(null);
        assertEquals(0, w.blockReads);
    }

    @Test
    void largeInventoryConservesItemsAndHonorsOutputCap() throws Exception {
        Fixture f = new Fixture();
        InventoryTile src = new InventoryTile(10000);
        for (int i = 0; i < 10000; i++) src.setInventorySlotContents(i, new ItemStack(ITEM, 64));
        f.world.tiles.put(SRC, src);
        f.run("INPUT FROM source OUTPUT 100 TO dest");
        assertEquals(100, count(f.dest));
        assertEquals(640000, count(src) + count(f.dest));
    }

    @Test
    void authorizationRequiresMatchingOpenContainerAndProximity() throws Exception {
        FakeWorld w = world();
        TileEntityManager m = new TileEntityManager();
        m.setWorldObj(w);
        m.xCoord = 0;
        m.yCoord = 64;
        m.zCoord = 0;
        w.tiles.put(MANAGER, m);
        FakePlayer p = allocate(FakePlayer.class);
        p.worldObj = w;
        p.posX = 0.5;
        p.posY = 64.5;
        p.posZ = 0.5;
        p.inventory = new InventoryPlayer(p);
        assertNull(ManagerAccess.get(p, 0, 64, 0));
        p.openContainer = new ContainerManager(p.inventory, m);
        assertSame(m, ManagerAccess.get(p, 0, 64, 0));
        assertNull(ManagerAccess.get(p, 1, 64, 0));
        p.posX = 50;
        assertNull(ManagerAccess.get(p, 0, 64, 0));
        p.posX = 0.5;
        w.unloaded.add(MANAGER);
        assertNull(ManagerAccess.get(p, 0, 64, 0));
    }

    static FluidStack water(int amount) {
        return TestFluid.water(amount);
    }

    @Test
    void fluidOutputQuantityAndRetention() throws Exception {
        Fixture f = new Fixture();
        TankTile src = new TankTile(5000), dst = new TankTile(0);
        f.world.tiles.put(SRC, src);
        f.world.tiles.put(DST, dst);
        f.run("INPUT fluid:water FROM source OUTPUT 1000 fluid:water TO dest");
        assertEquals(1000, dst.amount);
        assertEquals(4000, src.amount);
        f.run("INPUT fluid:water FROM source OUTPUT RETAIN 1200 fluid:water TO dest");
        assertEquals(1200, dst.amount);
        assertEquals(3800, src.amount);
    }

    @Test
    void fluidPartialFillReturnsRemainder() throws Exception {
        Fixture f = new Fixture();
        TankTile src = new TankTile(5000), dst = new TankTile(0) {

            @Override
            public int fill(ForgeDirection d, FluidStack f, boolean actual) {
                FluidStack c = f.copy();
                if (actual) c.amount = Math.min(c.amount, 10);
                return super.fill(d, c, actual);
            }
        };
        f.world.tiles.put(SRC, src);
        f.world.tiles.put(DST, dst);
        f.run("INPUT fluid:water FROM source OUTPUT 100 fluid:water TO dest");
        assertEquals(5000, src.amount + dst.amount);
        assertTrue(dst.amount <= 100);
    }

    @Test
    void newFluidSnapshotsAcrossFacesDoNotMultiplyCount() {
        TankTile tank = new TankTile(1234) {

            @Override
            public FluidTankInfo[] getTankInfo(ForgeDirection d) {
                return d == ForgeDirection.UNKNOWN ? new FluidTankInfo[0] : super.getTankInfo(d);
            }
        };
        assertEquals(
            1234,
            InventoryUtils.countMatchingFluids(tank, SideQualifier.DEFAULT, ResourceIdSet.MATCH_ALL, null));
    }

    @Test
    void forgettingOneLabelPreservesOtherSourceAndQuota() throws Exception {
        Fixture f = new Fixture();
        f.stock(0, 20);
        InventoryTile s2 = new InventoryTile(1);
        s2.setInventorySlotContents(0, new ItemStack(ITEM, 20));
        f.add("second", EXTRA, s2);
        f.run("INPUT 5 FROM source, second OUTPUT 2 TO dest FORGET source OUTPUT TO dest");
        assertEquals(5, count(f.dest));
        assertEquals(17, count(s2));
    }

    @Test
    void networkInvalidationRemovesStalePathsImmediately() throws Exception {
        FakeWorld w = world();
        Block cable = new BlockCable();
        BlockPos link = new BlockPos(1, 64, 0), end = new BlockPos(2, 64, 0);
        w.blocks.put(link, cable);
        w.tiles.put(end, new InventoryTile(1));
        CableNetwork net = new CableNetwork(w, MANAGER);
        assertTrue(net.isConnected(end));
        w.blocks.remove(link);
        CableNetwork.invalidate(w);
        assertFalse(net.isConnected(end));
    }

    @Test
    void disabledTraceDoesNotResolveItemNames() throws Exception {
        Fixture f = new Fixture();
        final int[] names = { 0 };
        Item item = new Item() {

            @Override
            public String getUnlocalizedName(ItemStack stack) {
                names[0]++;
                return "audit";
            }
        };
        f.source.setInventorySlotContents(0, new ItemStack(item, 20));
        f.run("INPUT FROM source OUTPUT 1 TO dest");
        assertEquals(0, names[0]);
    }

    @Test
    void deepProgramReturnsDiagnostic() {
        StringBuilder code = new StringBuilder("EVERY 20 TICKS DO IF ");
        for (int i = 0; i < 200; i++) code.append("NOT ");
        code.append("TRUE THEN END END");
        assertFalse(
            new ProgramBuilder(code.toString()).build()
                .isSuccess());
    }

    @Test
    void refusedFluidRollbackIsPreservedOnDiskAndPausesManager() throws Exception {
        Fixture f = new Fixture();
        Field provider = World.class.getDeclaredField("provider");
        provider.setAccessible(true);
        provider.set(f.world, new net.minecraft.world.WorldProviderSurface());
        TankTile src = new TankTile(5000) {

            @Override
            public int fill(ForgeDirection d, FluidStack s, boolean a) {
                return 0;
            }
        };
        TankTile dst = new TankTile(0) {

            @Override
            public int fill(ForgeDirection d, FluidStack s, boolean a) {
                return a ? 0 : s.amount;
            }
        };
        f.world.tiles.put(SRC, src);
        f.world.tiles.put(DST, dst);
        TileEntityManager manager = new TileEntityManager();
        manager.setWorldObj(f.world);
        manager.xCoord = 0;
        manager.yCoord = 64;
        manager.zCoord = 0;
        ItemStack disk = new ItemStack(new ItemDisk());
        manager.setInventorySlotContents(0, disk);
        Program program = parse("EVERY 20 TICKS DO INPUT fluid:water FROM source OUTPUT 100 fluid:water TO dest END");
        assertThrows(
            IllegalStateException.class,
            () -> program
                .tick(new ProgramContext(f.world, manager, new CableNetwork(f.world, MANAGER), f.labels, 20, false)));
        assertFalse(manager.isRunning());
        NBTTagCompound recovery = disk.getTagCompound()
            .getCompoundTag("sfm:recovery_fluid");
        assertEquals(100, recovery.getInteger("Amount"));
        assertEquals(5000, src.amount + dst.amount + recovery.getInteger("Amount"));
        manager.reset();
        assertEquals(
            100,
            disk.getTagCompound()
                .getCompoundTag("sfm:recovery_fluid")
                .getInteger("Amount"));
    }

    @Test
    void recoveryExceptionPausesAndPreservesRecordWithoutAutomaticRetry() throws Exception {
        Fixture f = new Fixture();
        TileEntityManager manager = new TileEntityManager();
        manager.setWorldObj(f.world);
        ItemStack disk = new ItemStack(new ItemDisk());
        NBTTagCompound root = new NBTTagCompound();
        NBTTagCompound recovery = new NBTTagCompound();
        recovery.setInteger("Amount", 100);
        root.setTag("sfm:recovery_fluid", recovery);
        disk.setTagCompound(root);
        manager.setInventorySlotContents(0, disk);
        // This unloaded fake world's provider is absent: recovery cannot resolve the source.
        assertDoesNotThrow(manager::updateEntity);
        assertFalse(manager.isRunning());
        assertTrue(recovery.getBoolean("recoveryBlocked"));
        assertEquals(100, recovery.getInteger("Amount"));
        int logs = manager.getLogs()
            .size();
        manager.setRunning(true);
        assertDoesNotThrow(manager::updateEntity);
        assertFalse(manager.isRunning());
        assertEquals(
            logs,
            manager.getLogs()
                .size());
        assertEquals(100, recovery.getInteger("Amount"));
    }

    @Test
    void randomizedItemQuotasConserveAndMatchExpectedAmounts() throws Exception {
        java.util.Random random = new java.util.Random(701);
        for (int trial = 0; trial < 200; trial++) {
            Fixture f = new Fixture();
            int total = 0;
            for (int slot = 0; slot < 3; slot++) {
                int n = random.nextInt(65);
                f.stock(slot, n);
                total += n;
            }
            int existing = random.nextInt(65);
            f.dest.setInventorySlotContents(0, new ItemStack(ITEM, existing));
            int inQuota = random.nextInt(150), outQuota = random.nextInt(150), retain = random.nextInt(150),
                cap = random.nextInt(180);
            f.run(
                "INPUT " + inQuota
                    + " RETAIN "
                    + retain
                    + " FROM source OUTPUT "
                    + outQuota
                    + " RETAIN "
                    + cap
                    + " TO dest");
            int expected = Math
                .min(Math.min(inQuota, outQuota), Math.min(Math.max(0, total - retain), Math.max(0, cap - existing)));
            assertEquals(existing + expected, count(f.dest), "trial " + trial);
            assertEquals(total + existing, count(f.source) + count(f.dest));
        }
    }

    @Test
    void networkLogPayloadStaysBoundedAndDecodes() {
        java.util.List<ca.teamdman.sfm.common.tileentity.LogEntry> logs = new java.util.ArrayList<>();
        String huge = String.join("", Collections.nCopies(8000, "x"));
        for (int i = 0; i < 500; i++) logs
            .add(new ca.teamdman.sfm.common.tileentity.LogEntry(ca.teamdman.sfm.common.tileentity.LogLevel.INFO, huge));
        ca.teamdman.sfm.common.net.PacketManagerLogs packet = new ca.teamdman.sfm.common.net.PacketManagerLogs(
            0,
            64,
            0,
            "INFO",
            logs);
        io.netty.buffer.ByteBuf buffer = io.netty.buffer.Unpooled.buffer();
        try {
            packet.toBytes(buffer);
            assertTrue(buffer.readableBytes() < 32767);
            new ca.teamdman.sfm.common.net.PacketManagerLogs().fromBytes(buffer);
            assertEquals(0, buffer.readableBytes());
        } finally {
            buffer.release();
        }
    }

    public static final class FakeWorld extends World {

        Map<BlockPos, TileEntity> tiles;
        Map<BlockPos, Block> blocks;
        Set<BlockPos> unloaded;
        long time;
        int blockReads;

        private FakeWorld() {
            super((ISaveHandler) null, "audit", (WorldSettings) null, (WorldProvider) null, (Profiler) null);
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
        public long getTotalWorldTime() {
            return time;
        }

        @Override
        public boolean blockExists(int x, int y, int z) {
            return y >= 0 && y < 256 && !unloaded.contains(new BlockPos(x, y, z));
        }

        void check(int x, int y, int z) {
            if (!blockExists(x, y, z)) throw new AssertionError("Unloaded world access");
        }

        @Override
        public TileEntity getTileEntity(int x, int y, int z) {
            check(x, y, z);
            return tiles.get(new BlockPos(x, y, z));
        }

        @Override
        public Block getBlock(int x, int y, int z) {
            check(x, y, z);
            blockReads++;
            return blocks.get(new BlockPos(x, y, z));
        }

        @Override
        public int getBlockMetadata(int x, int y, int z) {
            check(x, y, z);
            return 0;
        }

        @Override
        public void markTileEntityChunkModified(int x, int y, int z, TileEntity tile) {}

        @Override
        public int getBlockPowerInput(int x, int y, int z) {
            return 0;
        }
    }

    static class InventoryTile extends TileEntity implements IInventory {

        final InventoryBasic inv;

        InventoryTile(int size) {
            inv = new InventoryBasic("audit", false, size);
        }

        @Override
        public int getSizeInventory() {
            return inv.getSizeInventory();
        }

        @Override
        public ItemStack getStackInSlot(int s) {
            return inv.getStackInSlot(s);
        }

        @Override
        public ItemStack decrStackSize(int s, int n) {
            return inv.decrStackSize(s, n);
        }

        @Override
        public ItemStack getStackInSlotOnClosing(int s) {
            return inv.getStackInSlotOnClosing(s);
        }

        @Override
        public void setInventorySlotContents(int s, ItemStack i) {
            inv.setInventorySlotContents(s, i);
        }

        @Override
        public String getInventoryName() {
            return "audit";
        }

        @Override
        public boolean hasCustomInventoryName() {
            return true;
        }

        @Override
        public int getInventoryStackLimit() {
            return 64;
        }

        @Override
        public void markDirty() {}

        @Override
        public boolean isUseableByPlayer(EntityPlayer p) {
            return true;
        }

        @Override
        public void openInventory() {}

        @Override
        public void closeInventory() {}

        @Override
        public boolean isItemValidForSlot(int s, ItemStack i) {
            return true;
        }
    }

    static class SidedTile extends InventoryTile implements ISidedInventory {

        int[] slots = { 0 };

        SidedTile() {
            super(1);
        }

        @Override
        public int[] getAccessibleSlotsFromSide(int side) {
            return slots;
        }

        @Override
        public boolean canExtractItem(int s, ItemStack i, int d) {
            return true;
        }

        @Override
        public boolean canInsertItem(int s, ItemStack i, int d) {
            return true;
        }
    }

    static class TankTile extends TileEntity implements IFluidHandler {

        int amount;

        TankTile(int amount) {
            this.amount = amount;
        }

        @Override
        public int fill(ForgeDirection side, FluidStack f, boolean actual) {
            if (f == null || f.getFluid() != TestFluid.WATER) return 0;
            int n = Math.min(10000 - amount, f.amount);
            if (actual) amount += n;
            return n;
        }

        @Override
        public FluidStack drain(ForgeDirection side, int n, boolean actual) {
            n = Math.min(n, amount);
            if (n <= 0) return null;
            if (actual) amount -= n;
            return water(n);
        }

        @Override
        public FluidStack drain(ForgeDirection side, FluidStack f, boolean actual) {
            return f == null || f.getFluid() != TestFluid.WATER ? null : drain(side, f.amount, actual);
        }

        @Override
        public boolean canFill(ForgeDirection side, Fluid f) {
            return true;
        }

        @Override
        public boolean canDrain(ForgeDirection side, Fluid f) {
            return true;
        }

        @Override
        public FluidTankInfo[] getTankInfo(ForgeDirection side) {
            return new FluidTankInfo[] { new FluidTankInfo(amount > 0 ? water(amount) : null, 10000) };
        }
    }

    public static class FakePlayer extends EntityPlayer {

        @Override
        public void addChatMessage(net.minecraft.util.IChatComponent text) {}

        @Override
        public boolean canCommandSenderUseCommand(int level, String command) {
            return false;
        }

        @Override
        public net.minecraft.util.ChunkCoordinates getPlayerCoordinates() {
            return new net.minecraft.util.ChunkCoordinates(0, 64, 0);
        }

        private FakePlayer() {
            super(null, null);
        }
    }
}
