package ca.teamdman.sfm.perf;

import java.io.File;
import java.io.Writer;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldServer;

import com.google.gson.GsonBuilder;
import com.sun.management.ThreadMXBean;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.Tags;
import ca.teamdman.sfm.common.block.ModBlocks;
import ca.teamdman.sfm.common.diagnostics.ManagerPhaseProfiler;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.item.ModItems;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/** Development artifact only. All world access, fixtures and checks run on the server thread. */
@Mod(
    modid = "sfmbenchmark",
    name = "SFM Pack Benchmark (development only)",
    version = "1",
    acceptedMinecraftVersions = "[1.7.10]",
    acceptableRemoteVersions = "*",
    dependencies = "required-after:superfactorymanager")
public final class PackBenchmark {

    private final List<OwnedBlock> owned = new ArrayList<>();
    private final List<Fixture> fixtures = new ArrayList<>();
    private final List<Map<String, Object>> results = new ArrayList<>();
    private final Map<String, Object> report = new LinkedHashMap<>();
    private final List<String> cleanupIssues = new ArrayList<>();
    private List<BenchmarkPlan> plans;
    private BenchmarkPlan current;
    private WorldServer world;
    private File output;
    private boolean active;
    private boolean tickOpen;
    private int index;
    private int elapsed;
    private int warmup;
    private int measured;
    private long[][] samples;
    private long[][] phaseSamples;
    private long tickStart;
    private long allocationStart;
    private long setupNanos;
    private long[] gcStart;
    private long heapStart;
    private ThreadMXBean allocationBean;
    private int autoDelay;

    @Mod.EventHandler
    public void start(FMLServerStartingEvent event) {
        if (!Boolean.getBoolean("sfm.packBenchmark")) return;
        java.lang.management.ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        if (bean instanceof ThreadMXBean) {
            ThreadMXBean candidate = (ThreadMXBean) bean;
            if (candidate.isThreadAllocatedMemorySupported()) {
                try {
                    candidate.setThreadAllocatedMemoryEnabled(true);
                    allocationBean = candidate;
                } catch (RuntimeException ignored) { /* Allocation is optional, never silently reported as zero. */ }
            }
        }
        FMLCommonHandler.instance()
            .bus()
            .register(this);
        event.registerServerCommand(new Command());
        log("Development fixture command enabled; use sfmbench run quick or standard in the disposable world.");
    }

    @Mod.EventHandler
    public void started(FMLServerStartedEvent event) {
        if (Boolean.getBoolean("sfm.packBenchmark") && System.getProperty("sfm.packBenchmark.auto") != null)
            autoDelay = 200;
    }

    @Mod.EventHandler
    public void stop(FMLServerStoppingEvent event) {
        if (active) abort(new IllegalStateException("Server stopping before suite completion"));
        FMLCommonHandler.instance()
            .bus()
            .unregister(this);
    }

    private final class Command extends CommandBase {

        @Override
        public String getCommandName() {
            return "sfmbench";
        }

        @Override
        public int getRequiredPermissionLevel() {
            return 4;
        }

        @Override
        public String getCommandUsage(ICommandSender sender) {
            return "/sfmbench [run quick|run standard|run focus|status|stop]";
        }

        @Override
        public void processCommand(ICommandSender sender, String[] args) {
            if (args.length == 2 && "run".equals(args[0])
                && ("quick".equals(args[1]) || "standard".equals(args[1]) || "focus".equals(args[1]))) {
                try {
                    if (active) {
                        log("A suite is already running");
                        return;
                    }
                    begin(args[1]);
                } catch (Exception failure) {
                    if (active) abort(failure);
                    else log("Cannot start: " + failure);
                }
            } else if (args.length == 1 && "stop".equals(args[0])) {
                if (active) abort(new IllegalStateException("Operator stopped suite"));
            } else if (args.length != 1 || !"status".equals(args[0])) {
                throw new WrongUsageException(getCommandUsage(sender));
            }
            sender.addChatMessage(
                new ChatComponentText(
                    "SFM benchmark "
                        + (active
                            ? "running " + index
                                + "/"
                                + plans.size()
                                + " "
                                + (current == null ? "preparing" : current.name() + " tick " + elapsed)
                            : "idle")
                        + (output == null ? "" : "; report=" + output.getAbsolutePath())));
        }
    }

    private void begin(String mode) throws Exception {
        if (active) throw new IllegalStateException("A suite is already running");
        world = MinecraftServer.getServer()
            .worldServerForDimension(0);
        require(
            world != null && "sfm-qualification".equals(
                world.getWorldInfo()
                    .getWorldName()),
            "Disposable qualification world required");
        require(
            MinecraftServer.getServer()
                .getCurrentPlayerCount() == 0,
            "No players may be connected");
        warmup = "focus".equals(mode) ? 100 : "quick".equals(mode) ? 40 : 200;
        measured = "focus".equals(mode) ? 420 : "quick".equals(mode) ? 120 : 1200;
        plans = "focus".equals(mode) ? BenchmarkPlan.focus() : BenchmarkPlan.matrix();
        results.clear();
        cleanupIssues.clear();
        report.clear();
        index = 0;
        elapsed = 0;
        current = null;
        output = MinecraftServer.getServer()
            .getFile("performance/sfm-pack-benchmark/run-" + UUID.randomUUID() + ".json");
        Files.createDirectories(
            output.toPath()
                .getParent());
        report.put("schema", 2);
        report.put("phaseProfile", ManagerPhaseProfiler.ENABLED);
        report.put(
            "phaseScope",
            "All SFM managers in the server body, including background fixtures; scan time is nested, not additive");
        report.put(
            "phaseColumns",
            Arrays.asList(
                "scanNanos",
                "scanCalls",
                "scannedCables",
                "visitedPositions",
                "compatibilityInclusiveNanos",
                "programInclusiveNanos",
                "allManagerNanos",
                "allManagerUpdates"));
        report.put(
            "startedUtc",
            Instant.now()
                .toString());
        report.put("mode", mode);
        report.put("warmupTicks", warmup);
        report.put("measuredTicks", measured);
        report.put("sfmVersion", Tags.VERSION);
        report.put("java", System.getProperty("java.runtime.version"));
        report.put("vm", System.getProperty("java.vm.name"));
        report.put("os", System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        report.put(
            "processors",
            Runtime.getRuntime()
                .availableProcessors());
        report.put(
            "maxHeapBytes",
            Runtime.getRuntime()
                .maxMemory());
        report.put("allocationSupported", allocationBean != null);
        report.put(
            "world",
            world.getWorldInfo()
                .getWorldName());
        report.put("initialWorldTick", world.getTotalWorldTime());
        report.put("emptyServerTickSuppressionReset", true);
        int background = 0;
        for (Object tile : world.loadedTileEntityList) if (tile instanceof TileEntityManager) background++;
        report.put("backgroundOverworldManagers", background);
        Map<String, String> mods = new LinkedHashMap<>();
        for (ModContainer mod : Loader.instance()
            .getActiveModList()) mods.put(mod.getModId(), mod.getVersion());
        report.put("mods", mods);
        report.put(
            "sampleColumns",
            Arrays.asList(
                "elapsedTick",
                "fixtureManagerNanos",
                "observedServerBodyNanos",
                "serverThreadAllocatedBytes",
                "verificationNanos",
                "managerUpdates",
                "primaryMoved",
                "witnessMoved"));
        report.put("cases", results);
        report.put("cleanupIssues", cleanupIssues);
        report.put("complete", false);
        report.put("passed", false);
        active = true;
        writeReport();
        log("Starting " + mode + " matrix of " + plans.size() + " cases; " + output.getAbsolutePath());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void beforeTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!active && autoDelay > 0) {
            if (--autoDelay == 0) {
                try {
                    String mode = System.getProperty("sfm.packBenchmark.auto");
                    require(
                        "focus".equals(mode) || "quick".equals(mode) || "standard".equals(mode),
                        "Invalid automatic mode");
                    begin(mode);
                } catch (Exception failure) {
                    abort(failure);
                }
            }
            return;
        }
        if (!active) return;
        try {
            require(
                MinecraftServer.getServer()
                    .getCurrentPlayerCount() == 0,
                "Player joined during benchmark");
            // Vanilla pauses tile updates after 1,200 empty-server ticks, even in spawn chunks.
            // Keep this disposable, playerless workload ticking without force-loading any chunk.
            world.resetUpdateEntityTick();
            if (current == null) {
                prepare(plans.get(index));
                return; // Placement/compilation tick is never a measured tick.
            }
            if (elapsed == 0) for (Fixture fixture : fixtures) fixture.manager.setRunning(true);
            if (elapsed == warmup) {
                gcStart = gc();
                heapStart = ManagementFactory.getMemoryMXBean()
                    .getHeapMemoryUsage()
                    .getUsed();
            }
            for (Fixture fixture : fixtures) fixture.updatesBefore = fixture.manager.getTimedUpdateCount();
            ManagerPhaseProfiler.reset();
            allocationStart = allocated();
            tickStart = System.nanoTime();
            tickOpen = true;
        } catch (Exception failure) {
            abort(failure);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void afterTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !active || !tickOpen) return;
        long serverNanos = System.nanoTime() - tickStart;
        long allocationEnd = allocated();
        long[] phases = ManagerPhaseProfiler.snapshot();
        tickOpen = false;
        try {
            elapsed++;
            long managerNanos = 0;
            long checkStart = System.nanoTime();
            int completed = current.completed(elapsed);
            for (Fixture fixture : fixtures) {
                require(
                    fixture.origin.getTileEntity(world) == fixture.manager,
                    "Manager replaced/unloaded at " + fixture.origin);
                require(
                    fixture.manager.getTimedUpdateCount() == fixture.updatesBefore + 1,
                    "Missing/duplicate manager update at " + fixture.origin
                        + "; before="
                        + fixture.updatesBefore
                        + "; after="
                        + fixture.manager.getTimedUpdateCount()
                        + "; worldTick="
                        + world.getTotalWorldTime());
                require(
                    fixture.manager.isRunning() && !fixture.manager.hasPendingRecovery(),
                    "Manager stopped/recovering: " + fixture.manager.getStatusMessage());
                require(fixture.manager.getManagerTick() == elapsed, "Manager tick mismatch");
                managerNanos += Math.round(fixture.manager.getLastTickTimeMs() * 1e6);
                verify(fixture, completed);
            }
            long checkNanos = System.nanoTime() - checkStart;
            if (elapsed > warmup) {
                phaseSamples[elapsed - warmup - 1] = phases;
                int executions = completed - current.completed(elapsed - 1);
                samples[elapsed - warmup - 1] = new long[] { elapsed, managerNanos, serverNanos,
                    allocationStart < 0 || allocationEnd < 0 ? -1 : allocationEnd - allocationStart, checkNanos,
                    fixtures.size(), current.primaryMoves() ? executions * fixtures.size() : 0,
                    executions * fixtures.size() };
            }
            if (elapsed == warmup + measured) finishCase();
        } catch (Exception failure) {
            abort(failure);
        }
    }

    private void prepare(BenchmarkPlan plan) {
        long start = System.nanoTime();
        current = plan;
        elapsed = 0;
        samples = new long[measured][8];
        phaseSamples = new long[measured][8];
        // Check a one-block halo first: no overwrite, joining chests, or accidental external cable connections.
        for (BlockPos pos : plan.positions) {
            for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) for (int dz = -1; dz <= 1; dz++) {
                BlockPos check = pos.offset(dx, dy, dz);
                require(check.isLoaded(world), "Unloaded fixture halo at " + check);
                require(world.isAirBlock(check.x, check.y, check.z), "Fixture area occupied at " + check);
            }
        }
        for (BlockPos cable : plan.cables) place(cable, ModBlocks.cable);
        for (BlockPos origin : plan.origins) {
            TileEntityManager manager = (TileEntityManager) place(origin, ModBlocks.manager);
            manager.setRunning(false);
            BlockPos[] positions = BenchmarkPlan.chests(origin);
            IInventory[] inventories = new IInventory[4];
            for (int i = 0; i < 4; i++) inventories[i] = (IInventory) place(positions[i], Blocks.chest);
            if (!"empty".equals(plan.workload)) stock(
                inventories[0],
                "absent_resource".equals(plan.workload) ? Item.getItemFromBlock(Blocks.stone)
                    : Item.getItemFromBlock(Blocks.cobblestone));
            if ("blocked".equals(plan.workload)) stock(inventories[1], Item.getItemFromBlock(Blocks.stone));
            stock(inventories[2], Items.stick);
            LabelPositionHolder labels = new LabelPositionHolder();
            String[] names = { "source", "dest", "witness_source", "witness_dest" };
            for (int i = 0; i < 4; i++) labels.addPosition(names[i], positions[i]);
            ItemStack disk = new ItemStack(ModItems.disk);
            ItemDisk.setProgramString(disk, plan.program());
            LabelPositionHolder.saveTo(disk, labels);
            manager.setInventorySlotContents(0, disk);
            require(
                manager.hasCompiledProgram(),
                "Invalid fixture program: " + manager.getStatusMessage() + "\n" + plan.program());
            manager.setManagerTick(0);
            fixtures.add(new Fixture(origin, manager, positions, inventories));
        }
        setupNanos = System.nanoTime() - start;
    }

    private TileEntity place(BlockPos pos, Block block) {
        require(world.setBlock(pos.x, pos.y, pos.z, block, 0, 2), "Cannot place fixture at " + pos);
        TileEntity tile = pos.getTileEntity(world);
        owned.add(new OwnedBlock(pos, block, tile));
        return tile;
    }

    private static void stock(IInventory inventory, Item item) {
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++)
            inventory.setInventorySlotContents(slot, new ItemStack(item, 64));
    }

    private void verify(Fixture fixture, int completed) {
        int primary = current.primaryMoves() ? completed : 0;
        int source = "empty".equals(current.workload) ? 0 : BenchmarkPlan.STOCK - primary;
        int dest = "blocked".equals(current.workload) ? BenchmarkPlan.STOCK : primary;
        for (int i = 0; i < 4; i++)
            require(fixture.positions[i].getTileEntity(world) == fixture.inventories[i], "Chest replaced/unloaded");
        verifyInventory(
            fixture.inventories[0],
            Item.getItemFromBlock("absent_resource".equals(current.workload) ? Blocks.stone : Blocks.cobblestone),
            source);
        verifyInventory(
            fixture.inventories[1],
            Item.getItemFromBlock("blocked".equals(current.workload) ? Blocks.stone : Blocks.cobblestone),
            dest);
        verifyInventory(fixture.inventories[2], Items.stick, BenchmarkPlan.STOCK - completed);
        verifyInventory(fixture.inventories[3], Items.stick, completed);
    }

    private static void verifyInventory(IInventory inventory, Item item, int expected) {
        int total = 0;
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack == null) continue;
            require(
                stack.getItem() == item && stack.getItemDamage() == 0
                    && !stack.hasTagCompound()
                    && stack.stackSize > 0
                    && stack.stackSize <= 64,
                "Unexpected item/stack in fixture");
            total += stack.stackSize;
        }
        require(total == expected, "Inventory total " + total + " != expected " + expected);
    }

    private void finishCase() throws Exception {
        long[] gcEnd = gc();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", current.name());
        result.put("managers", current.managers);
        result.put("shared", current.shared);
        result.put("workload", current.workload);
        result.put("interval", current.interval);
        result.put("cables", current.cables.size());
        result.put("program", current.program());
        result.put("setupNanos", setupNanos);
        result.put("samples", samples);
        result.put("phaseSamples", phaseSamples);
        Map<String, Object> phaseSummary = new LinkedHashMap<>();
        String[] phaseNames = { "scanNanos", "scanCalls", "scannedCables", "visitedPositions",
            "compatibilityInclusiveNanos", "programInclusiveNanos", "allManagerNanos", "allManagerUpdates" };
        for (int i = 0; i < phaseNames.length; i++) phaseSummary.put(phaseNames[i], distribution(phaseSamples, i));
        result.put("phases", phaseSummary);
        result.put("fixtureManagerNanos", distribution(1));
        result.put("observedServerBodyNanos", distribution(2));
        result.put("serverThreadAllocatedBytes", distribution(3));
        result.put("verificationNanos", distribution(4));
        result.put("finalExecutionsPerManager", current.completed(elapsed));
        result.put("finalPrimaryMoved", current.primaryMoves() ? current.completed(elapsed) * current.managers : 0);
        result.put("finalWitnessMoved", current.completed(elapsed) * current.managers);
        long[] finalItems = new long[4];
        for (Fixture fixture : fixtures) for (int i = 0; i < 4; i++) {
            IInventory inventory = fixture.inventories[i];
            for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack != null) finalItems[i] += stack.stackSize;
            }
        }
        result.put("finalInventoryTotals", finalItems);

        result.put("gcCountDelta", gcEnd[0] - gcStart[0]);
        result.put("gcMillisDelta", gcEnd[1] - gcStart[1]);
        result.put("heapUsedStart", heapStart);
        result.put(
            "heapUsedEnd",
            ManagementFactory.getMemoryMXBean()
                .getHeapMemoryUsage()
                .getUsed());
        result.put("passed", true);
        long cleanupStart = System.nanoTime();
        cleanup();
        result.put("cleanupNanos", System.nanoTime() - cleanupStart);
        results.add(result);
        require(cleanupIssues.isEmpty(), "Fixture cleanup incomplete");
        index++;
        log(
            String.format(
                Locale.ROOT,
                "completed %d/%d %s; fixture manager p95=%.3f ms",
                index,
                plans.size(),
                current.name(),
                percentile(1, .95) / 1e6));
        current = null;
        if (index == plans.size()) {
            active = false;
            report.put("complete", true);
            report.put("passed", true);
            report.put(
                "finishedUtc",
                Instant.now()
                    .toString());
            log("PASSED all " + results.size() + " cases; report=" + output.getAbsolutePath());
        }
        writeReport();
        if (!active && Boolean.getBoolean("sfm.packBenchmark.stopAfter")) MinecraftServer.getServer()
            .initiateShutdown();
    }

    private Map<String, Long> distribution(int column) {
        return distribution(samples, column);
    }

    private static Map<String, Long> distribution(long[][] samples, int column) {
        Map<String, Long> values = new LinkedHashMap<>();
        values.put("p50", percentile(samples, column, .50));
        values.put("p95", percentile(samples, column, .95));
        values.put("p99", percentile(samples, column, .99));
        values.put("max", percentile(samples, column, 1));
        long sum = 0;
        for (long[] sample : samples) sum += sample[column];
        values.put("sum", sum);
        return values;
    }

    private long percentile(int column, double fraction) {
        return percentile(samples, column, fraction);
    }

    private static long percentile(long[][] samples, int column, double fraction) {
        long[] values = new long[samples.length];
        for (int i = 0; i < values.length; i++) values[i] = samples[i][column];
        Arrays.sort(values);
        return values[Math.max(0, (int) Math.ceil(values.length * fraction) - 1)];
    }

    private void cleanup() {
        // Clear only our original inventories before breaking; otherwise vanilla/manager blocks drop items.
        for (OwnedBlock entry : owned) {
            if (!entry.matches(world)) {
                cleanupIssues.add("Changed/unloaded fixture at " + entry.pos);
                continue;
            }
            if (entry.tile instanceof IInventory) {
                IInventory inventory = (IInventory) entry.tile;
                for (int i = 0; i < inventory.getSizeInventory(); i++) inventory.setInventorySlotContents(i, null);
            }
        }
        for (OwnedBlock entry : owned) {
            if (entry.matches(world) && !world.setBlockToAir(entry.pos.x, entry.pos.y, entry.pos.z)) {
                cleanupIssues.add("Could not remove fixture at " + entry.pos);
            }
        }
        owned.clear();
        fixtures.clear();
    }

    private void abort(Exception failure) {
        active = false;
        tickOpen = false;
        report.put("failure", failure.toString());
        report.put("failedCase", current == null ? "preparing" : current.name());
        report.put("failedElapsedTick", elapsed);
        report.put("passed", false);
        report.put("complete", false);
        try {
            cleanup();
        } catch (Exception cleanupFailure) {
            cleanupIssues.add(cleanupFailure.toString());
        }
        try {
            writeReport();
        } catch (Exception writeFailure) {
            log("Report write failed: " + writeFailure);
        }
        SuperFactoryManager.LOG.error("[SFM-BENCH] FAILED", failure);
        if (Boolean.getBoolean("sfm.packBenchmark.stopAfter")) MinecraftServer.getServer()
            .initiateShutdown();
    }

    private void writeReport() throws Exception {
        if (output == null) return;
        File temporary = new File(output.getAbsolutePath() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temporary.toPath(), StandardCharsets.UTF_8)) {
            new GsonBuilder().setPrettyPrinting()
                .create()
                .toJson(report, writer);
        }
        Files.move(temporary.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private long allocated() {
        return allocationBean == null ? -1
            : allocationBean.getThreadAllocatedBytes(
                Thread.currentThread()
                    .getId());
    }

    private static long[] gc() {
        long count = 0;
        long millis = 0;
        for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (bean.getCollectionCount() >= 0) count += bean.getCollectionCount();
            if (bean.getCollectionTime() >= 0) millis += bean.getCollectionTime();
        }
        return new long[] { count, millis };
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static void log(String message) {
        SuperFactoryManager.LOG.info("[SFM-BENCH] " + message);
    }

    private static final class OwnedBlock {

        final BlockPos pos;
        final Block block;
        final TileEntity tile;

        OwnedBlock(BlockPos pos, Block block, TileEntity tile) {
            this.pos = pos;
            this.block = block;
            this.tile = tile;
        }

        boolean matches(WorldServer world) {
            return pos.isLoaded(world) && pos.getBlock(world) == block && pos.getTileEntity(world) == tile;
        }
    }

    private static final class Fixture {

        final BlockPos origin;
        final TileEntityManager manager;
        final BlockPos[] positions;
        final IInventory[] inventories;
        long updatesBefore;

        Fixture(BlockPos origin, TileEntityManager manager, BlockPos[] positions, IInventory[] inventories) {
            this.origin = origin;
            this.manager = manager;
            this.positions = positions;
            this.inventories = inventories;
        }
    }
}
