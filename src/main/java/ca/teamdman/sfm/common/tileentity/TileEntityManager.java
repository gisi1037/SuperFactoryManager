package ca.teamdman.sfm.common.tileentity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
import ca.teamdman.sfm.common.diagnostics.ManagerPhaseProfiler;
import ca.teamdman.sfm.common.diagnostics.ManagerTickProfiler;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.DebugConsole;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.program.ProgramStateStore;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfm.compat.Compat;
import ca.teamdman.sfm.compat.stevefm.SteveFMCompat;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

public class TileEntityManager extends TileEntity implements IInventory {

    private ItemStack diskStack = null;
    private Program compiledProgram = null;
    private ProgramStateStore programState;
    private Program globalProgram;
    private ca.teamdman.sfml.data.ProgramGlobals programGlobals;
    private DebugConsole debugConsole;

    public ca.teamdman.sfml.data.ProgramGlobals getProgramGlobals(Program program) {
        if (globalProgram != program || programGlobals == null) {
            globalProgram = program;
            programGlobals = new ca.teamdman.sfml.data.ProgramGlobals(program.getGlobalDeclarations());
        }
        return programGlobals;
    }

    public void setProgramGlobals(Program program, ca.teamdman.sfml.data.ProgramGlobals values) {
        globalProgram = program;
        programGlobals = values;
    }

    private void clearProgramGlobals() {
        globalProgram = null;
        programGlobals = null;
    }

    public DebugConsole getDebugConsole() {
        if (debugConsole == null) debugConsole = new DebugConsole();
        return debugConsole;
    }

    public boolean hasDebugOutputEnabled() {
        return debugConsole != null && debugConsole.isEnabled();
    }

    private String lastCodeCompiled = null;
    private String statusMessage = "No Disk";
    private boolean isRunning = true;

    private long managerTick = 0;
    private long diskGeneration;

    public long getDiskGeneration() {
        return diskGeneration;
    }

    private int lastRedstonePower = 0;
    private ca.teamdman.sfm.common.program.RedstoneTriggerTracker redstoneTriggerTracker;

    public ca.teamdman.sfm.common.program.RedstoneTriggerTracker getRedstoneTriggerTracker() {
        if (redstoneTriggerTracker == null)
            redstoneTriggerTracker = new ca.teamdman.sfm.common.program.RedstoneTriggerTracker();
        return redstoneTriggerTracker;
    }

    private void resetRedstoneTriggerTracker() {
        if (redstoneTriggerTracker != null) redstoneTriggerTracker.reset();
    }

    private CableNetwork cableNetwork = null;

    // Logging & Diagnostics
    private LogLevel logLevel = LogLevel.OFF;
    private final List<LogEntry> logs = new ArrayList<>();
    private static final int MAX_LOGS = 500;
    private double lastTickTimeMs = 0.0;
    private double peakTickTimeMs = 0.0;
    private final double[] tickTimes = new double[20];
    private int tickTimeIndex = 0;
    private long timedUpdateCount;

    @Override
    public void updateEntity() {
        if (worldObj == null || worldObj.isRemote) {
            return;
        }

        // Include recovery, idle checks, compilation, discovery and compatibility work.
        long startNs = System.nanoTime();
        try {
            updateServer();
        } finally {
            long elapsedNs = System.nanoTime() - startNs;
            lastTickTimeMs = elapsedNs / 1_000_000.0;
            peakTickTimeMs = Math.max(peakTickTimeMs, lastTickTimeMs);
            tickTimes[tickTimeIndex] = lastTickTimeMs;
            tickTimeIndex = (tickTimeIndex + 1) % tickTimes.length;
            timedUpdateCount++;
            ManagerTickProfiler.INSTANCE.recordUpdate(elapsedNs);
            ManagerPhaseProfiler.update(elapsedNs);
        }
    }

    private void updateServer() {
        if (blockItemRecovery() || recoverFluid()) {
            resetRedstoneTriggerTracker();
            return;
        }
        if (diskStack == null || !(diskStack.getItem() instanceof ItemDisk) || !isRunning) {
            resetRedstoneTriggerTracker();
            lastRedstonePower = worldObj.getBlockPowerInput(xCoord, yCoord, zCoord);
            if (diskStack == null) {
                compiledProgram = null;
                clearProgramGlobals();
                lastCodeCompiled = null;
                statusMessage = "No Disk";
            }
            return;
        }
        String code = ItemDisk.getProgramString(diskStack);
        if (!code.equals(lastCodeCompiled) || programState != null && !programState.isCurrent(diskStack))
            recompile(code);
        if (compiledProgram == null) return;
        if (cableNetwork == null) rebuildNetwork();

        long compatibilityStart = ManagerPhaseProfiler.begin();
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
        ManagerPhaseProfiler.compatibility(compatibilityStart);

        if (pulse && logLevel.includes(LogLevel.DEBUG)) {
            if (budPulse) {
                log(LogLevel.DEBUG, "Program ticking with 1 unprocessed BUD update pulse.");
            } else {
                log(LogLevel.DEBUG, "Program ticking with 1 unprocessed redstone pulses.");
            }
        }

        if (diskStack == null || !(diskStack.getItem() instanceof ItemDisk)) {
            compiledProgram = null;
            clearProgramGlobals();
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

        LabelPositionHolder labelPositions = null;
        ProgramContext context = new ProgramContext(
            worldObj,
            this,
            cableNetwork,
            labelPositions,
            managerTick,
            pulse,
            compiledProgram.getReferencedLabels());

        context.setStateAccess(programState);
        long startNs = System.nanoTime();
        try {
            compiledProgram.tick(context);

            if (context.didSomething()) {
                if (logLevel.includes(LogLevel.TRACE)) {
                    log(
                        LogLevel.TRACE,
                        String.format("Program tick took %.2f ms", (System.nanoTime() - startNs) / 1e6));
                }

                if (logLevel == LogLevel.TRACE || logLevel == LogLevel.DEBUG || logLevel == LogLevel.INFO) {
                    setLogLevel(LogLevel.OFF);
                }
            }
        } catch (Exception e) {
            isRunning = false;
            resetRedstoneTriggerTracker();
            markDirty();
            statusMessage = "Runtime error (paused): " + e.getMessage();
            log(LogLevel.ERROR, "Runtime exception: " + e.getMessage());
        } finally {
            if (programState != null && programState.isDirty()) markDirty();
            ManagerPhaseProfiler.program(startNs);
        }
    }

    private static final String RECOVERY_ITEM = "sfm:recovery_item";
    private static final String RECOVERY_FLUID = "sfm:recovery_fluid";
    private static final int RECOVERY_VERSION = 1;

    public boolean hasPendingRecovery() {
        return diskStack != null && diskStack.hasTagCompound()
            && (diskStack.getTagCompound()
                .hasKey(RECOVERY_ITEM)
                || diskStack.getTagCompound()
                    .hasKey(RECOVERY_FLUID));
    }

    private void requireRecoverySpace() {
        if (diskStack == null || !(diskStack.getItem() instanceof ItemDisk))
            throw new IllegalStateException("No disk for transfer recovery");
        if (hasPendingRecovery()) throw new IllegalStateException("Resolve pending recovery before another transfer");
        if (!diskStack.hasTagCompound()) diskStack.setTagCompound(new NBTTagCompound());
    }

    /** A pending stack is owned by the disk; an uncertain stack is evidence, not safe to replay. */
    public NBTTagCompound preserveItem(ItemStack stack, BlockPos source, int slot,
        net.minecraftforge.common.util.ForgeDirection side, boolean uncertain, String stage) {
        requireRecoverySpace();
        NBTTagCompound entry = new NBTTagCompound();
        entry.setInteger("version", RECOVERY_VERSION);
        entry.setString(
            "recoveryId",
            UUID.randomUUID()
                .toString());
        entry.setString("state", uncertain ? "uncertain" : "pending");
        entry.setString("stage", stage);
        entry.setTag(
            "stack",
            stack.copy()
                .writeToNBT(new NBTTagCompound()));
        entry.setInteger("sourceX", source.x);
        entry.setInteger("sourceY", source.y);
        entry.setInteger("sourceZ", source.z);
        entry.setInteger("sourceSlot", slot);
        entry.setInteger("sourceSide", side.ordinal());
        entry.setInteger("sourceDimension", worldObj.provider.dimensionId);
        diskStack.getTagCompound()
            .setTag(RECOVERY_ITEM, entry);
        setRunning(false);
        markDirty();
        return entry;
    }

    public void itemRecoveryResolved() {
        statusMessage = "Item recovery resolved; restart manager when ready";
        if (isRunning) setRunning(false);
        markDirty();
    }

    public void fluidRecoveryResolved() {
        statusMessage = "Fluid recovery resolved; restart manager when ready";
        if (isRunning) setRunning(false);
        markDirty();
    }

    private boolean blockItemRecovery() {
        if (diskStack == null || !diskStack.hasTagCompound()
            || !diskStack.getTagCompound()
                .hasKey(RECOVERY_ITEM))
            return false;
        if (isRunning) setRunning(false);
        // Never guess where to return a stack after source replacement or an uncertain callback.
        // Keep even unknown/corrupt records intact for an explicit recovery workflow.
        statusMessage = "Item recovery pending; use /sfmrecovery x y z inspect";
        return true;
    }

    public void preserveFluid(net.minecraftforge.fluids.FluidStack fluid, BlockPos source,
        net.minecraftforge.common.util.ForgeDirection side) {
        preserveFluid(fluid, source, side, false, "rollback conflict");
    }

    /** Unknown callback ownership is evidence only and must never be automatically replayed. */
    public NBTTagCompound preserveFluid(net.minecraftforge.fluids.FluidStack fluid, BlockPos source,
        net.minecraftforge.common.util.ForgeDirection side, boolean uncertain, String stage) {
        if (fluid == null || fluid.amount <= 0) throw new IllegalArgumentException("Positive recovery fluid required");
        requireRecoverySpace();
        NBTTagCompound entry = new NBTTagCompound();
        entry.setInteger("version", RECOVERY_VERSION);
        entry.setString("state", uncertain ? "uncertain" : "pending");
        entry.setString("stage", stage);
        entry.setBoolean("recoveryBlocked", uncertain);
        entry.setString(
            "recoveryId",
            UUID.randomUUID()
                .toString());
        fluid.writeToNBT(entry);
        entry.setInteger("sourceX", source.x);
        entry.setInteger("sourceY", source.y);
        entry.setInteger("sourceZ", source.z);
        entry.setInteger("sourceSide", side.ordinal());
        entry.setInteger("sourceDimension", worldObj.provider.dimensionId);
        diskStack.getTagCompound()
            .setTag(RECOVERY_FLUID, entry);
        setRunning(false);
        markDirty();
        return entry;
    }

    private boolean recoverFluid() {
        if (diskStack == null || !diskStack.hasTagCompound()
            || !diskStack.getTagCompound()
                .hasKey(RECOVERY_FLUID))
            return false;
        if (isRunning) setRunning(false);
        NBTTagCompound entry = diskStack.getTagCompound()
            .getCompoundTag(RECOVERY_FLUID);
        // Records from the audit build have no version (0); keep them readable.
        if (!diskStack.getTagCompound()
            .hasKey(RECOVERY_FLUID, 10) || entry.getInteger("version") < 0
            || entry.getInteger("version") > RECOVERY_VERSION) {
            statusMessage = "Unsupported fluid recovery record; program paused";
            return true;
        }
        // Older records lack state. Unknown or uncertain records stay intact and paused.
        if (entry.hasKey("state") && (!entry.hasKey("state", 8) || !"pending".equals(entry.getString("state")))) {
            statusMessage = "Fluid recovery ownership uncertain; inspect source, destination and disk";
            return true;
        }
        if (entry.getBoolean("recoveryBlocked")) {
            statusMessage = "Fluid recovery blocked; inspect source and disk before retrying";
            return true;
        }
        statusMessage = "Fluid recovery pending; program paused";
        if (worldObj.getTotalWorldTime() % 20 != 0) return true;
        try {
            return recoverFluid(entry);
        } catch (Exception e) {
            // A callback may throw after mutating its tank. Do not automatically retry
            // an uncertain transfer; retain the record for operator reconciliation.
            entry.setBoolean("recoveryBlocked", true);
            entry.setString("state", "uncertain");
            entry.setString("stage", "automatic source return");
            markDirty();
            statusMessage = "Fluid recovery blocked: " + e.getMessage();
            log(LogLevel.ERROR, statusMessage);
            return true;
        }
    }

    private boolean recoverFluid(NBTTagCompound entry) {
        if (entry.getInteger("sourceDimension") != worldObj.provider.dimensionId) return true;
        BlockPos pos = new BlockPos(
            entry.getInteger("sourceX"),
            entry.getInteger("sourceY"),
            entry.getInteger("sourceZ"));
        net.minecraftforge.fluids.IFluidHandler source = ca.teamdman.sfm.common.inventory.InventoryUtils
            .getFluidHandler(worldObj, pos);
        net.minecraftforge.fluids.FluidStack fluid = net.minecraftforge.fluids.FluidStack.loadFluidStackFromNBT(entry);
        if (source == null || fluid == null) return true;
        int returned = Math.max(
            0,
            Math.min(
                fluid.amount,
                source.fill(
                    net.minecraftforge.common.util.ForgeDirection.getOrientation(entry.getInteger("sourceSide")),
                    fluid.copy(),
                    true)));
        fluid.amount -= returned;
        if (fluid.amount == 0) {
            diskStack.getTagCompound()
                .removeTag(RECOVERY_FLUID);
            statusMessage = "Recovered fluid; restart manager";
        } else fluid.writeToNBT(entry);
        if (returned > 0) markDirty();
        return true;
    }

    public void runOnceWithLogLevel(LogLevel level) {
        if (worldObj == null || worldObj.isRemote) return;
        if (!isRunning) resetRedstoneTriggerTracker();
        if (blockItemRecovery() || recoverFluid()) {
            resetRedstoneTriggerTracker();
            return;
        }
        LogLevel prev = this.logLevel;
        this.logLevel = level;

        log(level, "Manual execution triggered with log level: " + level.name());

        if (cableNetwork == null) {
            rebuildNetwork();
        }

        if (diskStack != null && diskStack.getItem() instanceof ItemDisk) {
            if (!ItemDisk.getProgramString(diskStack)
                .equals(lastCodeCompiled) || programState != null && !programState.isCurrent(diskStack)) {
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
                LabelPositionHolder labelPositions = null;
                ProgramContext context = new ProgramContext(
                    worldObj,
                    this,
                    cableNetwork,
                    labelPositions,
                    managerTick,
                    pulse,
                    compiledProgram.getReferencedLabels());

                context.setStateAccess(programState);
                long startNs = System.nanoTime();
                try {
                    compiledProgram.tick(context);
                    long elapsedNs = System.nanoTime() - startNs;
                    double ms = elapsedNs / 1_000_000.0;
                    log(LogLevel.INFO, String.format("Manual run completed in %.2f ms", ms));
                } catch (Exception e) {
                    isRunning = false;
                    resetRedstoneTriggerTracker();
                    markDirty();
                    statusMessage = "Runtime error (paused): " + e.getMessage();
                    log(LogLevel.ERROR, "Manual run failed: " + e.getMessage());
                } finally {
                    if (programState != null && programState.isDirty()) markDirty();
                }
            } else {
                log(LogLevel.ERROR, "Cannot run: program is invalid or missing.");
            }
        } else {
            log(LogLevel.ERROR, "Cannot run: no Disk inserted.");
        }

        // Revert log level if it was OFF
        this.logLevel = prev;
        if (!isRunning) resetRedstoneTriggerTracker();
    }

    public void log(LogLevel level, String message) {
        if (level == null || message == null) return;
        if (!this.logLevel.includes(level) && level != LogLevel.ERROR && level != LogLevel.WARN) {
            return;
        }
        if (message.length() > 4096) message = message.substring(0, 4096) + " [truncated]";
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
        if (!logLevel.includes(LogLevel.TRACE)) return;
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

    /** Copy only the requested tail for on-demand terminal inspection, without changing log capture. */
    public List<LogEntry> getRecentLogs(int maximum) {
        synchronized (logs) {
            int count = Math.max(0, Math.min(maximum, MAX_LOGS));
            return new ArrayList<>(logs.subList(Math.max(0, logs.size() - count), logs.size()));
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

    public long getTimedUpdateCount() {
        return timedUpdateCount;
    }

    public double getPeakTickTimeMs() {
        return peakTickTimeMs;
    }

    public void recompile(String code) {
        clearProgramGlobals();
        resetRedstoneTriggerTracker();
        // Preserve the last accepted heap values before replacing/reconciling their schema.
        try {
            flushProgramState();
        } catch (IllegalArgumentException | IllegalStateException e) {
            compiledProgram = null;
            isRunning = false;
            statusMessage = "Persistent state conflict (paused): " + e.getMessage();
            log(LogLevel.ERROR, statusMessage);
            return;
        }
        programState = null;
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
            Program candidate = result.getProgram();
            try {
                // Old programs with no state payload skip all value/schema allocation.
                if (diskStack != null && (candidate.getStateSchema()
                    .size() != 0 || diskStack.hasTagCompound()
                        && diskStack.getTagCompound()
                            .hasKey(ProgramStateStore.NBT_KEY))) {
                    ProgramStateStore candidateState = ProgramStateStore
                        .activate(diskStack, candidate.getStateSchema());
                    candidateState.flush();
                    for (String warning : candidateState.getActivationWarnings()) log(LogLevel.WARN, warning);
                    programState = candidateState;
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                compiledProgram = null;
                isRunning = false;
                statusMessage = "Persistent state error (paused): " + e.getMessage();
                log(LogLevel.ERROR, statusMessage);
                return;
            }
            compiledProgram = candidate;
            statusMessage = "Running";
            log(LogLevel.INFO, "Program recompiled successfully: " + compiledProgram.getName());
        } else {
            compiledProgram = null;
            statusMessage = result.getErrorMessage();
            log(LogLevel.ERROR, "Compilation error: " + result.getErrorMessage());
        }
    }

    /** Validate a private candidate before replacing the installed executable document. */
    public String trySaveProgram(String newCode) {
        if (diskStack == null || !(diskStack.getItem() instanceof ItemDisk)) return "No program disk";
        try {
            ca.teamdman.sfm.common.program.ProgramDocument.validateSource(newCode);
        } catch (IllegalArgumentException error) {
            return error.getMessage();
        }
        String installedSource = ItemDisk.getProgramString(diskStack);
        boolean reuseExecutable = compiledProgram != null && installedSource.equals(lastCodeCompiled)
            && ca.teamdman.sfm.common.util.ProgramRevision.sameExecutableTokens(installedSource, newCode);
        Program candidateProgram = compiledProgram;
        if (!reuseExecutable) {
            ProgramBuildResult result = new ProgramBuilder(newCode).build();
            if (!result.isSuccess()) return result.getErrorMessage();
            candidateProgram = result.getProgram();
        }
        try {
            flushProgramState();
            ItemStack candidateDisk = diskStack.copy();
            ca.teamdman.sfm.common.program.ProgramDocument.ensure(candidateDisk);
            String previousSource = ItemDisk.getProgramString(candidateDisk);
            ItemDisk.setProgramString(candidateDisk, newCode);
            if (previousSource.equals(newCode)) ca.teamdman.sfm.common.program.ProgramDocument.advance(candidateDisk);
            ProgramStateStore candidateState = null;
            if (candidateProgram.getStateSchema()
                .size() != 0 || candidateDisk.hasTagCompound()
                    && candidateDisk.getTagCompound()
                        .hasKey(ProgramStateStore.NBT_KEY)) {
                candidateState = ProgramStateStore.activate(candidateDisk, candidateProgram.getStateSchema());
                candidateState.flush();
            }
            ProgramStateStore.validateCompleteDisk(candidateDisk);
            long nextGeneration = Math.incrementExact(diskGeneration);
            // No fallible compilation or schema reconciliation after publication.
            diskStack = candidateDisk;
            diskGeneration = nextGeneration;
            compiledProgram = candidateProgram;
            programState = candidateState;
            lastCodeCompiled = newCode;
            if (!reuseExecutable) {
                resetRedstoneTriggerTracker();
                clearProgramGlobals();
            }
            statusMessage = isRunning ? "Running" : "Program saved; paused";
            markDirty();
            return null;
        } catch (IllegalArgumentException | IllegalStateException | ArithmeticException error) {
            return "Program not saved: " + error.getMessage();
        }
    }

    public void saveCodeAndRecompile(String newCode) {
        if (diskStack != null && diskStack.getItem() instanceof ItemDisk) {
            flushProgramState();
            ItemDisk.setProgramString(diskStack, newCode);
            markDirty();
        }
        if (diskStack != null && diskStack.getItem() instanceof ItemDisk) recompile(newCode);
    }

    /** Snapshot only at save/exposure/lifecycle boundaries; ordinary SET and manager ticks stay in heap. */
    public void flushProgramState() {
        if (programState != null && programState.flush()) markDirty();
    }

    public ProgramStateStore getProgramState() {
        return programState;
    }

    /** Explicitly discard persistent values, preserving program, labels and transfer recovery evidence. */
    public void resetProgramState() {
        if (diskStack == null) return;
        setRunning(false);
        String source = ItemDisk.getProgramString(diskStack);
        ProgramBuildResult result = new ProgramBuilder(source).build();
        if (!result.isSuccess()) {
            statusMessage = "State not reset: fix the program first. " + result.getErrorMessage();
            log(LogLevel.ERROR, statusMessage);
            return;
        }
        try {
            Program candidateProgram = result.getProgram();
            ProgramStateStore candidate = ProgramStateStore.reset(diskStack, candidateProgram.getStateSchema());
            // Validation/publication completes before replacing the accepted live cache.
            candidate.flush();
            programState = candidate;
            compiledProgram = candidateProgram;
            clearProgramGlobals();
            lastCodeCompiled = source;
            statusMessage = "Persistent variables reset; program paused";
            markDirty();
        } catch (IllegalArgumentException | IllegalStateException e) {
            statusMessage = "State not reset: " + e.getMessage();
            log(LogLevel.ERROR, statusMessage);
        }
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
        if (this.isRunning != running) resetRedstoneTriggerTracker();
        this.isRunning = running;
        markDirty();
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

    @Override
    public void onChunkUnload() {
        resetRedstoneTriggerTracker();
        clearProgramGlobals();
        cableNetwork = null;
        super.onChunkUnload();
    }

    public void rebuildNetwork() {
        if (worldObj != null) {
            cableNetwork = new CableNetwork(worldObj, new BlockPos(xCoord, yCoord, zCoord));

            log(
                LogLevel.INFO,
                "Cable network rebuilt (" + cableNetwork.getPeripherals()
                    .size() + " inventories discovered)");
            markDirty();
        }
    }

    public void reset() {
        if (diskStack != null) {
            flushProgramState();
            programState = null;
            if (diskStack.hasTagCompound()) diskStack.getTagCompound()
                .removeTag(ProgramStateStore.NBT_KEY);
            ItemDisk.setProgramString(diskStack, "");
            LabelPositionHolder.saveTo(diskStack, new LabelPositionHolder());
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
        if (slot == 0) flushProgramState();
        return slot == 0 ? diskStack : null;
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (slot == 0 && diskStack != null && amount > 0) {
            flushProgramState();
            ItemStack result = diskStack;
            diskGeneration = Math.incrementExact(diskGeneration);
            diskStack = null;
            programState = null;
            compiledProgram = null;
            clearProgramGlobals();
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
            flushProgramState();
            programState = null;
            diskGeneration = Math.incrementExact(diskGeneration);
            diskStack = stack;
            clearProgramGlobals();
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
        return player != null && worldObj != null
            && player.worldObj == worldObj
            && !isInvalid()
            && worldObj.blockExists(xCoord, yCoord, zCoord)
            && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
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
        flushProgramState();
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
        compiledProgram = null;
        clearProgramGlobals();
        programState = null;
        lastCodeCompiled = null;
        cableNetwork = null;
        timedUpdateCount = 0;
        statusMessage = "No Disk";
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
            tickTimeIndex = Math.floorMod(tag.getInteger("tt_idx"), tickTimes.length);
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
