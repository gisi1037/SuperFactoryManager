package ca.teamdman.sfm.common.tileentity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import ca.teamdman.sfm.Config;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfm.compat.Compat;
import ca.teamdman.sfm.compat.stevefm.SteveFMCompat;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

public class TileEntityManager extends TileEntity implements IInventory {

    private ItemStack diskStack = null;
    private Program compiledProgram = null;
    private String lastCodeCompiled = null;
    private String statusMessage = "No Disk";
    private boolean isRunning = true;

    private long managerTick = 0;
    private int lastRedstonePower = 0;
    private CableNetwork cableNetwork = null;

    // Logging & Diagnostics
    private LogLevel logLevel = LogLevel.OFF;
    private final List<LogEntry> logs = new ArrayList<>();
    private static final int MAX_LOGS = 500;
    private double lastTickTimeMs = 0.0;
    private double peakTickTimeMs = 0.0;
    private final double[] tickTimes = new double[20];
    private int tickTimeIndex = 0;

    @Override
    public void updateEntity() {
        if (worldObj == null || worldObj.isRemote) {
            return;
        }

        if (cableNetwork == null) {
            rebuildNetwork();
        }

        int currentPower = worldObj.getBlockPowerInput(xCoord, yCoord, zCoord);
        if (Compat.isSFMLoaded()) {
            try {
                int receiverPower = SteveFMCompat.getMaxReceiverPower(worldObj, cableNetwork.getPeripherals());
                if (receiverPower > currentPower) {
                    currentPower = receiverPower;
                }
            } catch (Throwable ignored) {}
        }
        boolean budPulse = false;
        if (Compat.isSFMLoaded()) {
            try {
                budPulse = SteveFMCompat.checkAndClearBUDUpdates(worldObj, cableNetwork.getPeripherals());
            } catch (Throwable ignored) {}
        }
        boolean pulse = (currentPower > 0 && lastRedstonePower == 0) || budPulse;
        lastRedstonePower = currentPower;

        if (pulse && logLevel.includes(LogLevel.DEBUG)) {
            if (budPulse) {
                log(LogLevel.DEBUG, "Program ticking with 1 unprocessed BUD update pulse.");
            } else {
                log(LogLevel.DEBUG, "Program ticking with 1 unprocessed redstone pulses.");
            }
        }

        if (diskStack == null || !(diskStack.getItem() instanceof ItemDisk)) {
            compiledProgram = null;
            lastCodeCompiled = null;
            statusMessage = "No Disk";
            return;
        }

        String currentCode = ItemDisk.getProgramString(diskStack);
        if (!currentCode.equals(lastCodeCompiled)) {
            recompile(currentCode);
        }

        if (!isRunning || compiledProgram == null) {
            return;
        }

        managerTick++;

        if (cableNetwork == null) {
            rebuildNetwork();
        }

        LabelPositionHolder labelPositions = LabelPositionHolder.from(diskStack);
        ProgramContext context = new ProgramContext(worldObj, this, cableNetwork, labelPositions, managerTick, pulse);

        long startNs = System.nanoTime();
        try {
            compiledProgram.tick(context);

            if (context.didSomething()) {
                long elapsedNs = System.nanoTime() - startNs;
                lastTickTimeMs = elapsedNs / 1_000_000.0;
                if (lastTickTimeMs > peakTickTimeMs) {
                    peakTickTimeMs = lastTickTimeMs;
                }
                tickTimes[tickTimeIndex] = lastTickTimeMs;
                tickTimeIndex = (tickTimeIndex + 1) % 20;

                if (logLevel.includes(LogLevel.TRACE)) {
                    log(LogLevel.TRACE, String.format("Program tick took %.2f ms", lastTickTimeMs));
                }

                if (logLevel == LogLevel.TRACE || logLevel == LogLevel.DEBUG || logLevel == LogLevel.INFO) {
                    setLogLevel(LogLevel.OFF);
                }
            }
        } catch (Exception e) {
            statusMessage = "Runtime error: " + e.getMessage();
            log(LogLevel.ERROR, "Runtime exception: " + e.getMessage());
        }
    }

    public void runOnceWithLogLevel(LogLevel level) {
        if (worldObj == null || worldObj.isRemote) return;
        LogLevel prev = this.logLevel;
        this.logLevel = level;

        log(level, "Manual execution triggered with log level: " + level.name());

        if (cableNetwork == null) {
            rebuildNetwork();
        }

        if (diskStack != null && diskStack.getItem() instanceof ItemDisk) {
            if (compiledProgram == null) {
                recompile(ItemDisk.getProgramString(diskStack));
            }

            if (compiledProgram != null) {
                int currentPower = worldObj.getBlockPowerInput(xCoord, yCoord, zCoord);
                if (Compat.isSFMLoaded()) {
                    try {
                        int receiverPower = SteveFMCompat.getMaxReceiverPower(worldObj, cableNetwork.getPeripherals());
                        if (receiverPower > currentPower) {
                            currentPower = receiverPower;
                        }
                    } catch (Throwable ignored) {}
                }
                boolean pulse = currentPower > 0;
                LabelPositionHolder labelPositions = LabelPositionHolder.from(diskStack);
                ProgramContext context = new ProgramContext(
                    worldObj,
                    this,
                    cableNetwork,
                    labelPositions,
                    managerTick,
                    pulse);

                long startNs = System.nanoTime();
                try {
                    compiledProgram.tick(context);
                    long elapsedNs = System.nanoTime() - startNs;
                    double ms = elapsedNs / 1_000_000.0;
                    log(LogLevel.INFO, String.format("Manual run completed in %.2f ms", ms));
                } catch (Exception e) {
                    log(LogLevel.ERROR, "Manual run failed: " + e.getMessage());
                }
            } else {
                log(LogLevel.ERROR, "Cannot run: program is invalid or missing.");
            }
        } else {
            log(LogLevel.ERROR, "Cannot run: no Disk inserted.");
        }

        // Revert log level if it was OFF
        if (prev == LogLevel.OFF) {
            this.logLevel = LogLevel.OFF;
        }
    }

    public void log(LogLevel level, String message) {
        if (level == null || message == null) return;
        if (!this.logLevel.includes(level) && level != LogLevel.ERROR && level != LogLevel.WARN) {
            return;
        }
        synchronized (logs) {
            logs.add(new LogEntry(level, message));
            if (logs.size() > MAX_LOGS) {
                logs.remove(0);
            }
        }
        if (Config.exportLogsToFile) {
            try {
                File logDir = new File("logs");
                if (!logDir.exists()) {
                    logDir.mkdirs();
                }
                File logFile = new File(logDir, "sfm_server_export.log");
                try (FileWriter fw = new FileWriter(logFile, true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    PrintWriter out = new PrintWriter(bw)) {
                    out.println("[" + level.name() + "] " + message);
                }
            } catch (Exception ignored) {}
        }
    }

    public void logNetworkDiagnostics(ProgramContext context) {
        log(LogLevel.TRACE, "======= Cable network =======");
        log(LogLevel.TRACE, "Cable positions:");
        if (cableNetwork != null) {
            for (BlockPos pos : cableNetwork.getCables()) {
                Block b = pos.getBlock(worldObj);
                int meta = pos.getBlockMetadata(worldObj);
                String bName = Block.blockRegistry.getNameForObject(b);
                log(LogLevel.TRACE, "- " + pos + " Block{" + bName + (meta != 0 ? ":" + meta : "") + "}");
            }
            log(LogLevel.TRACE, "Capability positions:");
            for (BlockPos pos : cableNetwork.getPeripherals()) {
                Block b = pos.getBlock(worldObj);
                int meta = pos.getBlockMetadata(worldObj);
                String bName = Block.blockRegistry.getNameForObject(b);
                log(LogLevel.TRACE, "- " + pos + " Block{" + bName + (meta != 0 ? ":" + meta : "") + "}");
            }
        }
        log(LogLevel.TRACE, "=============================");
        log(LogLevel.TRACE, "=== Label position holder ===");
        if (diskStack != null) {
            LabelPositionHolder holder = LabelPositionHolder.from(diskStack);
            for (Map.Entry<String, Set<BlockPos>> entry : holder.getAllLabels()
                .entrySet()) {
                for (BlockPos pos : entry.getValue()) {
                    Block b = pos.getBlock(worldObj);
                    int meta = pos.getBlockMetadata(worldObj);
                    String bName = Block.blockRegistry.getNameForObject(b);
                    log(
                        LogLevel.TRACE,
                        "- " + entry.getKey() + ": " + pos + " Block{" + bName + (meta != 0 ? ":" + meta : "") + "}");
                }
            }
        }
        log(LogLevel.TRACE, "=============================");
        log(LogLevel.TRACE, "Initial program context: " + context.toString());
    }

    public List<LogEntry> getLogs() {
        synchronized (logs) {
            return new ArrayList<>(logs);
        }
    }

    public void clearLogs() {
        synchronized (logs) {
            logs.clear();
        }
    }

    public void setClientLogs(List<LogEntry> newLogs) {
        synchronized (logs) {
            logs.clear();
            if (newLogs != null) {
                logs.addAll(newLogs);
            }
        }
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        LogLevel target = logLevel != null ? logLevel : LogLevel.OFF;
        log(LogLevel.INFO, "Log level updated to " + target.name());
        this.logLevel = target;
    }

    public double getLastTickTimeMs() {
        return lastTickTimeMs;
    }

    public double getPeakTickTimeMs() {
        return peakTickTimeMs;
    }

    public void recompile(String code) {
        lastCodeCompiled = code;
        if (code == null || code.trim()
            .isEmpty()) {
            compiledProgram = null;
            statusMessage = "Empty program";
            log(LogLevel.INFO, "Program cleared (empty)");
            return;
        }

        ProgramBuilder builder = new ProgramBuilder(code);
        ProgramBuildResult result = builder.build();
        if (result.isSuccess()) {
            compiledProgram = result.getProgram();
            statusMessage = "Running";
            log(LogLevel.INFO, "Program recompiled successfully: " + compiledProgram.getName());
        } else {
            compiledProgram = null;
            statusMessage = result.getErrorMessage();
            log(LogLevel.ERROR, "Compilation error: " + result.getErrorMessage());
        }
    }

    public void saveCodeAndRecompile(String newCode) {
        if (diskStack != null && diskStack.getItem() instanceof ItemDisk) {
            ItemDisk.setProgramString(diskStack, newCode);
            markDirty();
        }
        recompile(newCode);
    }

    public ItemStack getDisk() {
        return diskStack;
    }

    public Program getCompiledProgram() {
        return compiledProgram;
    }

    public boolean hasCompiledProgram() {
        return compiledProgram != null;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        this.isRunning = running;
        log(LogLevel.INFO, running ? "Manager started (RUNNING)" : "Manager paused (STOPPED)");
    }

    public long getManagerTick() {
        return managerTick;
    }

    public void setManagerTick(long managerTick) {
        this.managerTick = managerTick;
    }

    public BlockPos getBlockPos() {
        return new BlockPos(xCoord, yCoord, zCoord);
    }

    public double[] getTickTimes() {
        double[] result = new double[tickTimes.length];
        System.arraycopy(tickTimes, tickTimeIndex, result, 0, tickTimes.length - tickTimeIndex);
        System.arraycopy(tickTimes, 0, result, tickTimes.length - tickTimeIndex, tickTimeIndex);
        return result;
    }

    public void rebuildNetwork() {
        if (worldObj != null) {
            cableNetwork = new CableNetwork(worldObj, new BlockPos(xCoord, yCoord, zCoord));
            cableNetwork.scan();
            log(
                LogLevel.INFO,
                "Cable network rebuilt (" + cableNetwork.getPeripherals()
                    .size() + " inventories discovered)");
            markDirty();
        }
    }

    public void reset() {
        if (diskStack != null) {
            ItemDisk.setProgramString(diskStack, "");
            LabelPositionHolder.from(diskStack)
                .clear();
            recompile("");
            markDirty();
            log(LogLevel.INFO, "Program and labels reset");
        }
    }

    // --- IInventory Implementation ---

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot == 0 ? diskStack : null;
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (slot == 0 && diskStack != null) {
            ItemStack result = diskStack;
            diskStack = null;
            compiledProgram = null;
            lastCodeCompiled = null;
            statusMessage = "No Disk";
            log(LogLevel.INFO, "Disk removed");
            markDirty();
            return result;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return decrStackSize(slot, 1);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot == 0) {
            diskStack = stack;
            if (diskStack != null && diskStack.getItem() instanceof ItemDisk) {
                log(LogLevel.INFO, "Disk inserted");
                recompile(ItemDisk.getProgramString(diskStack));
            } else {
                compiledProgram = null;
                lastCodeCompiled = null;
                statusMessage = "No Disk";
            }
            markDirty();
        }
    }

    @Override
    public String getInventoryName() {
        return "container.sfm.manager";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot == 0 && stack != null && stack.getItem() instanceof ItemDisk;
    }

    // --- NBT Serialization ---

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("isRunning", isRunning);
        tag.setLong("managerTick", managerTick);
        tag.setString("logLevel", logLevel.name());
        tag.setDouble("lastTickTimeMs", lastTickTimeMs);
        tag.setDouble("peakTickTimeMs", peakTickTimeMs);
        for (int i = 0; i < 20; i++) {
            tag.setDouble("tt_" + i, tickTimes[i]);
        }
        tag.setInteger("tt_idx", tickTimeIndex);
        if (diskStack != null) {
            NBTTagCompound itemTag = new NBTTagCompound();
            diskStack.writeToNBT(itemTag);
            tag.setTag("disk", itemTag);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        isRunning = tag.getBoolean("isRunning");
        managerTick = tag.getLong("managerTick");
        if (tag.hasKey("logLevel")) {
            logLevel = LogLevel.fromString(tag.getString("logLevel"));
        }
        if (tag.hasKey("lastTickTimeMs")) {
            lastTickTimeMs = tag.getDouble("lastTickTimeMs");
        }
        if (tag.hasKey("peakTickTimeMs")) {
            peakTickTimeMs = tag.getDouble("peakTickTimeMs");
        }
        if (tag.hasKey("tt_idx")) {
            tickTimeIndex = tag.getInteger("tt_idx");
        }
        for (int i = 0; i < 20; i++) {
            if (tag.hasKey("tt_" + i)) {
                tickTimes[i] = tag.getDouble("tt_" + i);
            }
        }
        if (tag.hasKey("disk")) {
            diskStack = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("disk"));
            if (diskStack != null && diskStack.getItem() instanceof ItemDisk) {
                recompile(ItemDisk.getProgramString(diskStack));
            }
        } else {
            diskStack = null;
            compiledProgram = null;
            statusMessage = "No Disk";
        }
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound syncData = new NBTTagCompound();
        writeToNBT(syncData);
        syncData.setString("statusMessage", statusMessage);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, syncData);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
        if (pkt.func_148857_g()
            .hasKey("statusMessage")) {
            statusMessage = pkt.func_148857_g()
                .getString("statusMessage");
        }
    }
}
