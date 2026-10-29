package ca.teamdman.sfm.perf;

import java.io.File;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.oredict.OreDictionary;

import com.google.gson.GsonBuilder;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.Tags;
import ca.teamdman.sfm.common.block.ModBlocks;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.item.ModItems;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.LogLevel;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ItemContents;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.ItemTransferResult;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/** Opt-in, perf-source-set-only functional fixture. Never starts, closes or restarts a client. */
@Mod(
    modid = "sfmfoundationsqa",
    name = "SFM Foundations Qualification (development only)",
    version = "1",
    acceptedMinecraftVersions = "[1.7.10]",
    acceptableRemoteVersions = "*",
    dependencies = "required-after:superfactorymanager")
public final class FoundationsQualification {

    private static final String PREFIX = "sfm-foundations-qa-";
    private static final String ORE = "sfmFoundationsQaIron", UNKNOWN_ORE = "sfmFoundationsQaNeverRegistered";
    private static final BlockPos MANAGER = new BlockPos(0, 244, 0);
    private static final BlockPos SOURCE_LEFT = new BlockPos(2, 244, 0), SOURCE_RIGHT = new BlockPos(3, 244, 0);
    private static final BlockPos DEST_LEFT = new BlockPos(1, 244, 2), DEST_RIGHT = new BlockPos(2, 244, 2);
    // The (1,244,1) link connects both destination halves without traversing an inventory.
    private static final BlockPos[] CABLES = { new BlockPos(1, 244, 0), new BlockPos(1, 244, -1),
        new BlockPos(2, 244, -1), new BlockPos(3, 244, -1), new BlockPos(1, 244, 1), new BlockPos(2, 244, 1) };
    private static final String PROGRAM = "STATE $qa = {groups:0,total:0,exact:0,places:0,shortage:0,functionTotal:0,"
        + "requested:0,moved:0,remaining:0,status:\"\",oreMoved:0,oreStatus:\"\",finalSource:0,finalDest:0}\n"
        + "FUNCTION readTotal($items: ITEM_CONTENTS) RETURNS INTEGER DO RETURN TOTAL($items) END\n"
        + "EVERY 1 TICKS DO\n LET $stock = LABEL(\"source\").contents()\n"
        + " SET $qa.groups = LEN($stock) SET $qa.total = TOTAL($stock) SET $qa.functionTotal = readTotal($stock)\n"
        + " FOR $group IN $stock DO\n  IF ($group.total = 17) THEN\n"
        + "   SET $qa.exact = STOCK($stock, $group.item) SET $qa.places = LEN($group.locations)\n"
        + "   SET $qa.shortage = SHORTAGE(20, $qa.exact)\n"
        + "   TRANSFER (LABEL(\"source\"), LABEL(\"dest\").slots(2,2), $group.item, 20) INTO $sent\n"
        + "   SET $qa.requested = $sent.requested SET $qa.moved = $sent.moved\n"
        + "   SET $qa.remaining = $sent.remaining SET $qa.status = $sent.status\n  END\n END\n"
        + " TRANSFER (LABEL(\"source\").side(\"up\").slots(0,0), LABEL(\"dest\").side(\"down\").slots(0,0), "
        + "ORE(\""
        + ORE
        + "\"), 4) INTO $ore\n"
        + " SET $qa.oreMoved = $ore.moved SET $qa.oreStatus = $ore.status\n"
        + " SET $qa.finalSource = LABEL(\"source\").count(ITEM(\"*\"))\n"
        + " SET $qa.finalDest = LABEL(\"dest\").count(ITEM(\"*\"))\nEND";
    private static final String PROBE_PROGRAM = "STATE $probe = {before:0,after:0,unknown:0,unknownStatus:\"\","
        + "badAvailable:TRUE,badMove:0,badStatus:\"\"}\nEVERY 1 TICKS DO\n"
        + " SET $probe.before = LABEL(\"source\").count(ITEM(\"*\"))\n"
        + " TRANSFER (LABEL(\"source\"), LABEL(\"dest\"), ORE(\""
        + UNKNOWN_ORE
        + "\"), 3) INTO $unknown\n"
        + " SET $probe.unknown = $unknown.moved SET $probe.unknownStatus = $unknown.status\n"
        + " SET $probe.badAvailable = LABEL(\"source\").side(\"north\").slots(54,54).available(ITEM(\"*\"))\n"
        + " TRANSFER (LABEL(\"source\").side(\"north\").slots(54,54), LABEL(\"dest\"), ITEM(\"*\"), 3) INTO $bad\n"
        + " SET $probe.badMove = $bad.moved SET $probe.badStatus = $bad.status\n"
        + " SET $probe.after = LABEL(\"source\").count(ITEM(\"*\"))\nEND";

    private final Map<String, Object> report = new LinkedHashMap<>();
    private final List<Map<String, Object>> assertions = new ArrayList<>();
    private final List<String> cleanupIssues = new ArrayList<>();
    private final List<OwnedBlock> owned = new ArrayList<>();
    private final List<ItemIdentity> ownedItems = new ArrayList<>();
    private final List<ItemStack> originalStacks = new ArrayList<>();
    private WorldServer world;
    private TileEntityManager manager;
    private TileEntityChest sourceLeft, sourceRight, destLeft, destRight;
    private LabelPositionHolder labels;
    private ItemStack installedDisk;
    private ItemIdentity complexKey;
    private File output;
    private int delay;
    private boolean running, finished, disposable;

    @Mod.EventHandler
    public void starting(FMLServerStartingEvent event) {
        if (Boolean.getBoolean("sfm.foundationQualification")) FMLCommonHandler.instance()
            .bus()
            .register(this);
    }

    @Mod.EventHandler
    public void started(FMLServerStartedEvent event) {
        if (Boolean.getBoolean("sfm.foundationQualification")) delay = 100;
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || finished || running || delay <= 0) return;
        if (--delay != 0) return;
        qualify();
    }

    @Mod.EventHandler
    public void stopping(FMLServerStoppingEvent event) {
        if (!Boolean.getBoolean("sfm.foundationQualification")) return;
        if (!finished) {
            report.put("passed", false);
            report.put("failure", "Server stopped before qualification completed");
            finish();
        }
        FMLCommonHandler.instance()
            .bus()
            .unregister(this);
    }

    private void qualify() {
        running = true;
        try {
            initializeReport();
            MinecraftServer server = MinecraftServer.getServer();
            require(
                server.isDedicatedServer(),
                "Dedicated disposable server required; integrated clients are untouched");
            world = server.worldServerForDimension(0);
            require(
                world != null && world.getWorldInfo()
                    .getWorldName()
                    .startsWith(PREFIX),
                "World name must start with " + PREFIX);
            disposable = true;
            require(server.getCurrentPlayerCount() == 0, "No players may be connected during qualification");
            report.put(
                "world",
                world.getWorldInfo()
                    .getWorldName());
            report.put("worldTick", world.getTotalWorldTime());
            prepare();
            inspectNativeContents();
            installAndRun(PROGRAM);
            DataValue state = manager.getProgramState()
                .read(0);
            report.put("mainState", stateRecord(state));
            number(
                "scan groups",
                state.get("groups")
                    .asLong(),
                3);
            number(
                "scan total",
                state.get("total")
                    .asLong(),
                35);
            number(
                "read-only player function",
                state.get("functionTotal")
                    .asLong(),
                35);
            number(
                "exact STOCK",
                state.get("exact")
                    .asLong(),
                17);
            number(
                "complex physical origins",
                state.get("places")
                    .asLong(),
                2);
            number(
                "SHORTAGE",
                state.get("shortage")
                    .asLong(),
                3);
            number(
                "exact requested",
                state.get("requested")
                    .asLong(),
                20);
            number(
                "exact moved",
                state.get("moved")
                    .asLong(),
                17);
            number(
                "exact remaining",
                state.get("remaining")
                    .asLong(),
                3);
            text(
                "exact status",
                state.get("status")
                    .asString(),
                "partial");
            number(
                "ORE side/slot moved",
                state.get("oreMoved")
                    .asLong(),
                4);
            text(
                "ORE status",
                state.get("oreStatus")
                    .asString(),
                "complete");
            number(
                "reported source",
                state.get("finalSource")
                    .asLong(),
                14);
            number(
                "reported destination",
                state.get("finalDest")
                    .asLong(),
                21);
            number("physical source", count(sourceLeft) + count(sourceRight), 14);
            number("physical destination", count(destLeft) + count(destRight), 21);
            number("null-NBT remainder", sourceLeft.getStackInSlot(0).stackSize, 3);
            number("empty compound stock preserved", sourceLeft.getStackInSlot(1).stackSize, 11);
            check(
                "empty compound remains present",
                sourceLeft.getStackInSlot(1)
                    .hasTagCompound());
            check(
                "null NBT remains absent",
                !sourceLeft.getStackInSlot(0)
                    .hasTagCompound());
            ItemStack destinationComplex = destLeft.getStackInSlot(2);
            number("complex destination amount", destinationComplex.stackSize, 17);
            check(
                "complex destination exact identity",
                complexKey.equals(ItemIdentity.fromStack(destinationComplex, new QueryBudget())));

            require(!OreDictionary.doesOreNameExist(UNKNOWN_ORE), "Unknown fixture category unexpectedly exists");
            String[] oreNames = OreDictionary.getOreNames();
            installAndRun(PROBE_PROGRAM);
            DataValue probe = manager.getProgramState()
                .read(0);
            report.put("probeState", stateRecord(probe));
            number(
                "fresh count after prior request",
                probe.get("before")
                    .asLong(),
                14);
            number(
                "unknown ORE moved",
                probe.get("unknown")
                    .asLong(),
                0);
            text(
                "unknown ORE status",
                probe.get("unknownStatus")
                    .asString(),
                "no_match");
            check(
                "invalid combined slot is unavailable",
                !probe.get("badAvailable")
                    .asBoolean());
            number(
                "invalid slot moves nothing",
                probe.get("badMove")
                    .asLong(),
                0);
            text(
                "invalid slot action status",
                probe.get("badStatus")
                    .asString(),
                "unavailable");
            number(
                "probe leaves source unchanged",
                probe.get("after")
                    .asLong(),
                14);
            check(
                "unknown ORE does not register",
                !OreDictionary.doesOreNameExist(UNKNOWN_ORE)
                    && java.util.Arrays.equals(oreNames, OreDictionary.getOreNames()));
            ProgramContext fresh = nativeContext();
            ItemTransferResult stale = fresh.transferItems(
                LabelReference.literal("source"),
                LabelReference.literal("dest"),
                DataValue.itemIdentity(complexKey),
                1);
            number("old exact key uses fresh stock", stale.getMoved(), 0);
            text(
                "old exact key status",
                stale.getStatus()
                    .name(),
                "NO_MATCH");
            number(
                "final conservation",
                count(sourceLeft) + count(sourceRight) + count(destLeft) + count(destRight),
                35);
            check("no pending recovery", !manager.hasPendingRecovery());
            report.put("passed", true);
        } catch (Throwable failure) {
            report.put("passed", false);
            report.put("failure", failure.toString());
            SuperFactoryManager.LOG.error("[SFM-FOUNDATIONS-QA] FAILED", failure);
        } finally {
            finish();
            if (disposable && Boolean.getBoolean("sfm.foundationQualification.stopAfter")) MinecraftServer.getServer()
                .initiateShutdown();
        }
    }

    private void initializeReport() throws Exception {
        output = MinecraftServer.getServer()
            .getFile("logs/sfm-foundations-qualification.json");
        report.put("schema", 1);
        report.put(
            "startedUtc",
            Instant.now()
                .toString());
        report.put(
            "runId",
            java.util.UUID.randomUUID()
                .toString());
        report.put("passed", false);
        report.put("complete", false);
        report.put("sfmVersion", Tags.VERSION);
        report.put("java", System.getProperty("java.runtime.version"));
        report.put("vm", System.getProperty("java.vm.name"));
        report.put(
            "maxHeapBytes",
            Runtime.getRuntime()
                .maxMemory());
        report.put("assertions", assertions);
        report.put("cleanupIssues", cleanupIssues);
        report.put("program", PROGRAM);
        report.put("probeProgram", PROBE_PROGRAM);
        report.put(
            "scope",
            "Functional dedicated-server fixture, not throughput/tail-latency or arbitrary-provider qualification. "
                + "Vanilla chests permit all physical sides; the negative access case tests an invalid logical slot, not a forbidden face.");
        Map<String, String> mods = new LinkedHashMap<>();
        for (ModContainer mod : Loader.instance()
            .getActiveModList()) mods.put(mod.getModId(), mod.getVersion());
        report.put("mods", mods);
        // Replace a previous successful report before any fixture mutation begins.
        writeReport();
    }

    private void prepare() {
        // Explicit, bounded test setup may load the four chunks touched by the fixture/one-block halo.
        ChunkProviderServer chunks = (ChunkProviderServer) world.getChunkProvider();
        for (int x = -1; x <= 0; x++) for (int z = -1; z <= 0; z++) chunks.loadChunk(x, z);
        List<BlockPos> footprint = new ArrayList<>();
        footprint.add(MANAGER);
        for (BlockPos cable : CABLES) footprint.add(cable);
        footprint.add(SOURCE_LEFT);
        footprint.add(SOURCE_RIGHT);
        footprint.add(DEST_LEFT);
        footprint.add(DEST_RIGHT);
        for (BlockPos pos : footprint)
            for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) for (int dz = -1; dz <= 1; dz++) {
                BlockPos halo = pos.offset(dx, dy, dz);
                require(halo.isLoaded(world), "Unloaded fixture halo " + halo);
                require(
                    world.isAirBlock(halo.x, halo.y, halo.z) && halo.getTileEntity(world) == null,
                    "Occupied fixture halo " + halo);
            }
        check("air halo verified before placement", true);
        for (BlockPos cable : CABLES) place(cable, ModBlocks.cable);
        manager = (TileEntityManager) place(MANAGER, ModBlocks.manager);
        manager.setRunning(false);
        sourceLeft = (TileEntityChest) place(SOURCE_LEFT, Blocks.chest);
        sourceRight = (TileEntityChest) place(SOURCE_RIGHT, Blocks.chest);
        destLeft = (TileEntityChest) place(DEST_LEFT, Blocks.chest);
        destRight = (TileEntityChest) place(DEST_RIGHT, Blocks.chest);
        labels = new LabelPositionHolder();
        labels.addPosition("source", SOURCE_LEFT);
        labels.addPosition("source", SOURCE_RIGHT);
        labels.addPosition("dest", DEST_RIGHT);
        labels.addPosition("dest", DEST_LEFT);
        // Only this disposable process receives this uniquely named development category.
        require(!OreDictionary.doesOreNameExist(ORE), "Fixture ORE category must initially be absent");
        OreDictionary.registerOre(ORE, new ItemStack(Items.iron_ingot, 1, OreDictionary.WILDCARD_VALUE));
        report.put(
            "disposableOreRegistration",
            ORE + " -> minecraft:iron_ingot wildcard metadata; process lifetime only");
        NBTTagCompound complex = complexNbt();
        ItemStack plain = new ItemStack(Items.iron_ingot, 7), empty = new ItemStack(Items.iron_ingot, 11);
        empty.setTagCompound(new NBTTagCompound());
        ItemStack first = new ItemStack(Items.iron_ingot, 12), second = new ItemStack(Items.iron_ingot, 5);
        first.setTagCompound(complex);
        second.setTagCompound((NBTTagCompound) complex.copy());
        originalStacks.add(plain);
        originalStacks.add(empty);
        originalStacks.add(first);
        originalStacks.add(second);
        sourceLeft.setInventorySlotContents(0, plain);
        sourceLeft.setInventorySlotContents(1, empty);
        sourceLeft.setInventorySlotContents(2, first);
        sourceRight.setInventorySlotContents(3, second);
        ownedItems.add(ItemIdentity.fromStack(plain, new QueryBudget()));
        ownedItems.add(ItemIdentity.fromStack(empty, new QueryBudget()));
        complexKey = ItemIdentity.fromStack(first, new QueryBudget());
        ownedItems.add(complexKey);
        CableNetwork.invalidate(world);
        for (OwnedBlock block : owned) block.metadata = world.getBlockMetadata(block.pos.x, block.pos.y, block.pos.z);
    }

    /** Small payload, exercising primitive kinds 1..8/11, compounds, and several list element kinds. */
    private static NBTTagCompound complexNbt() {
        NBTTagCompound root = new NBTTagCompound();
        root.setByte("b", (byte) -1);
        root.setShort("s", (short) -2);
        root.setInteger("i", 3);
        root.setLong("l", 4L);
        root.setFloat("f", 1.25F);
        root.setDouble("d", -2.5D);
        root.setByteArray("a", new byte[] { 1, -2 });
        root.setIntArray("j", new int[] { 7 });
        root.setString("t", "x");
        NBTTagList numbers = new NBTTagList();
        numbers.appendTag(new NBTTagByte((byte) 5));
        root.setTag("n", numbers);
        NBTTagList strings = new NBTTagList();
        strings.appendTag(new NBTTagString("y"));
        root.setTag("q", strings);
        NBTTagCompound child = new NBTTagCompound();
        child.setByte("v", (byte) 6);
        NBTTagList compounds = new NBTTagList();
        compounds.appendTag(child);
        root.setTag("c", compounds);
        root.setTag("e", new NBTTagList());
        return root;
    }

    private void inspectNativeContents() {
        QueryBudget owner = new QueryBudget();
        ItemContents contents = nativeContext()
            .contents(LabelReference.literal("source"), ResourceSelector.item("*"), owner);
        List<ItemContents.Group> groups = contents.groups(owner);
        number("native duplicate-half labels deduplicate groups", groups.size(), 3);
        long total = 0;
        boolean found = false;
        for (ItemContents.Group group : groups) {
            total = Math.addExact(total, group.getTotal());
            if (!complexKey.equals(group.getItem())) continue;
            found = true;
            number("native exact complex total", group.getTotal(), 17);
            number(
                "native physical origin count",
                group.getLocations()
                    .size(),
                2);
            ItemContents.Location first = group.getLocations()
                .get(0),
                second = group.getLocations()
                    .get(1);
            check(
                "native first physical half/local slot",
                first.x == SOURCE_LEFT.x && first.y == 244
                    && first.z == SOURCE_LEFT.z
                    && first.slot == 2
                    && first.amount == 12
                    && first.dimension == world.provider.dimensionId);
            check(
                "native second physical half/local slot",
                second.x == SOURCE_RIGHT.x && second.y == 244
                    && second.z == SOURCE_RIGHT.z
                    && second.slot == 3
                    && second.amount == 5
                    && second.dimension == world.provider.dimensionId);
        }
        check("native all-type NBT group exists", found);
        number("native total before moves", total, 35);
        report.put("nativeScanAllocations", owner.getAllocations());
        report.put("nativeScanWork", owner.getSlotWork());
    }

    private ProgramContext nativeContext() {
        return new ProgramContext(world, manager, new CableNetwork(world, MANAGER), labels, 1, false);
    }

    private void installAndRun(String source) {
        installedDisk = new ItemStack(ModItems.disk);
        ItemDisk.setProgramString(installedDisk, source);
        LabelPositionHolder.saveTo(installedDisk, labels);
        manager.setInventorySlotContents(0, installedDisk);
        require(manager.hasCompiledProgram(), "Fixture program did not compile: " + manager.getStatusMessage());
        manager.setManagerTick(1);
        manager.runOnceWithLogLevel(LogLevel.INFO);
        require(
            !manager.getStatusMessage()
                .startsWith("Runtime error"),
            manager.getStatusMessage());
        require(manager.getProgramState() != null, "Fixture STATE was not activated");
    }

    private TileEntity place(BlockPos position, Block block) {
        require(
            world.setBlock(position.x, position.y, position.z, block, 0, 2),
            "Cannot place owned fixture " + position);
        TileEntity tile = position.getTileEntity(world);
        owned.add(new OwnedBlock(position, block, tile));
        // Placing our second chest half can legitimately adjust the first half's facing.
        for (OwnedBlock entry : owned) entry.metadata = world.getBlockMetadata(entry.pos.x, entry.pos.y, entry.pos.z);
        return tile;
    }

    private static long count(IInventory inventory) {
        long total = 0;
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack != null) total = Math.addExact(total, stack.stackSize);
        }
        return total;
    }

    private Map<String, Object> stateRecord(DataValue value) {
        Map<String, Object> record = new LinkedHashMap<>();
        for (String name : value.getType()
            .getFieldNames()) {
            DataValue field = value.get(name);
            record.put(
                name,
                field.getType() == DataType.STRING ? field.asString()
                    : field.getType() == DataType.BOOLEAN ? field.asBoolean() : field.asLong());
        }
        return record;
    }

    private void number(String name, long actual, long expected) {
        assertion(name, actual, expected, actual == expected);
    }

    private void text(String name, String actual, String expected) {
        assertion(name, actual, expected, expected.equals(actual));
    }

    private void check(String name, boolean value) {
        assertion(name, value, true, value);
    }

    private void assertion(String name, Object actual, Object expected, boolean passed) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("name", name);
        value.put("actual", actual);
        value.put("expected", expected);
        value.put("passed", passed);
        assertions.add(value);
        require(passed, name + ": expected " + expected + ", got " + actual);
    }

    private void finish() {
        if (finished) return;
        try {
            cleanup();
        } catch (Throwable failure) {
            cleanupIssues.add(failure.toString());
        }
        if (!cleanupIssues.isEmpty()) report.put("passed", false);
        finished = true;
        running = false;
        report.put("complete", true);
        report.put("cleanupCompleted", cleanupIssues.isEmpty());
        report.put(
            "finishedUtc",
            Instant.now()
                .toString());
        if (output == null) output = MinecraftServer.getServer()
            .getFile("logs/sfm-foundations-qualification.json");
        try {
            writeReport();
            SuperFactoryManager.LOG.info("[SFM-FOUNDATIONS-QA] passed=" + report.get("passed") + "; report=" + output);
        } catch (Throwable failure) {
            SuperFactoryManager.LOG
                .error("[SFM-FOUNDATIONS-QA] Cannot write report; qualification is unconfirmed", failure);
        }
    }

    private void writeReport() throws Exception {
        Files.createDirectories(
            output.toPath()
                .getParent());
        File temporary = new File(output.getAbsolutePath() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temporary.toPath(), StandardCharsets.UTF_8)) {
            new GsonBuilder().setPrettyPrinting()
                .create()
                .toJson(report, writer);
        }
        Files.move(temporary.toPath(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void cleanup() {
        if (world == null) return;
        List<OwnedBlock> cleared = new ArrayList<>();
        for (OwnedBlock block : owned) {
            if (!block.matches(world)) {
                cleanupIssues.add("Changed/unloaded owned block retained: " + block.pos);
                continue;
            }
            if (block.tile instanceof IInventory) {
                IInventory inventory = (IInventory) block.tile;
                if (!ownedContents(inventory)) {
                    cleanupIssues.add("Unexpected inventory retained: " + block.pos);
                    continue;
                }
                for (int slot = 0; slot < inventory.getSizeInventory(); slot++)
                    inventory.setInventorySlotContents(slot, null);
            }
            cleared.add(block);
        }
        // Reverse order removes chest inventories before the cable/manager footprint.
        int removed = 0;
        for (int i = cleared.size() - 1; i >= 0; i--) {
            OwnedBlock block = cleared.get(i);
            if (!block.matchesIdentity(world) || !world.setBlockToAir(block.pos.x, block.pos.y, block.pos.z))
                cleanupIssues.add("Could not safely remove owned block: " + block.pos);
            else removed++;
        }
        report.put("placedBlocks", owned.size());
        report.put("removedBlocks", removed);
        owned.clear();
        CableNetwork.invalidate(world);
    }

    private boolean ownedContents(IInventory inventory) {
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack == null) continue;
            if (inventory == manager) {
                if (stack != installedDisk) return false;
                continue;
            }
            if (stack.stackSize <= 0 || stack.stackSize > 35) return false;
            boolean original = false;
            for (ItemStack expected : originalStacks) if (stack == expected) {
                original = true;
                break;
            }
            if (original) continue; // Cleanup still works if the canonicalizer being qualified fails at setup.
            ItemIdentity key;
            try {
                key = ItemIdentity.fromStack(stack, new QueryBudget());
            } catch (RuntimeException invalid) {
                return false;
            }
            if (!ownedItems.contains(key)) return false;
        }
        return true;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static final class OwnedBlock {

        final BlockPos pos;
        final Block block;
        final TileEntity tile;
        int metadata;

        OwnedBlock(BlockPos pos, Block block, TileEntity tile) {
            this.pos = pos;
            this.block = block;
            this.tile = tile;
        }

        boolean matchesIdentity(WorldServer world) {
            return pos.isLoaded(world) && pos.getBlock(world) == block && pos.getTileEntity(world) == tile;
        }

        boolean matches(WorldServer world) {
            return matchesIdentity(world) && world.getBlockMetadata(pos.x, pos.y, pos.z) == metadata;
        }
    }
}
