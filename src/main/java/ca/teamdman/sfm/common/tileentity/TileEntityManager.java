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
import ca.teamdman.sfm.common.program.DebugTerminalService;
import ca.teamdman.sfm.common.program.ManagerExecutionProfiler;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.program.ProgramDiskBinding;
import ca.teamdman.sfm.common.program.ProgramDiskData;
import ca.teamdman.sfm.common.program.ProgramStateStore;
import ca.teamdman.sfm.common.program.project.ExecutionReference;
import ca.teamdman.sfm.common.program.project.PreparedExecution;
import ca.teamdman.sfm.common.program.project.ProjectResult;
import ca.teamdman.sfm.common.program.project.ProjectWorkspace;
import ca.teamdman.sfm.common.program.project.StationProjectService;
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
    private ManagerExecutionProfiler executionProfiler;
    private boolean profiledDueUpdate;

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

    /** Diagnostic entry points are used from the existing authenticated server UI queue only. */
    public boolean isLiveDiagnosticsThread() {
        return (worldObj == null || !worldObj.isRemote) && !isInvalid()
            && (net.minecraft.server.MinecraftServer.getServer() == null
                || ca.teamdman.sfm.common.net.ServerUiTasks.isServerThread());
    }

    public boolean startExecutionProfile(int updateLimit) {
        if (!isLiveDiagnosticsThread()) return false;
        executionProfiler = new ManagerExecutionProfiler(updateLimit);
        return true;
    }

    public void stopExecutionProfile() {
        if (isLiveDiagnosticsThread() && executionProfiler != null) executionProfiler.stop();
    }

    public void resetExecutionProfile() {
        if (isLiveDiagnosticsThread()) executionProfiler = null;
    }

    public ManagerExecutionProfiler.Snapshot getExecutionProfileSnapshot() {
        return !isLiveDiagnosticsThread() || executionProfiler == null ? ManagerExecutionProfiler.Snapshot.EMPTY
            : executionProfiler.snapshot();
    }

    /** A bearer can execute/copy a reference; only its validated source author may inspect private variables. */
    public boolean canInspectVariables(UUID actor) {
        if (!isLiveDiagnosticsThread()) return false;
        ProgramDiskData.Mode mode = ProgramDiskData.mode(diskStack);
        if (mode == ProgramDiskData.Mode.LEGACY) return referenceBinding == null;
        return mode == ProgramDiskData.Mode.REFERENCE && actor != null
            && actor.equals(referenceSourceOwner)
            && referenceBinding != null
            && referenceBinding.isCurrent(diskStack)
            && compiledProgram != null
            && referenceRequest == null
            && referenceWorld == worldObj
            && worldObj != null
            && referenceWorkspace != null
            && executionWorkspaceCurrent(referenceWorkspace, worldObj)
            && (programState == null || programState.isCurrent(diskStack))
            && worldObj.blockExists(xCoord, yCoord, zCoord)
            && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this;
    }

    public List<String> getLiveVariableSnapshot(String requested, UUID actor) {
        if (!canInspectVariables(actor)) return DebugTerminalService.variableAccessDenied();
        ca.teamdman.sfml.data.ProgramGlobals values = globalProgram == compiledProgram ? programGlobals : null;
        return DebugTerminalService.inspectVariables(programState, compiledProgram, values, requested);
    }

    public boolean hasDebugOutputEnabled() {
        return debugConsole != null && debugConsole.isEnabled();
    }

    private ProgramDiskBinding referenceBinding;
    private UUID referenceSourceOwner;
    private net.minecraft.world.World referenceWorld;
    private ProjectWorkspace referenceWorkspace;
    private ReferenceRequest referenceRequest;
    private long referenceActivationGeneration;
    private boolean referenceAttempted;
    private boolean referenceAdmissionQueued;
    private long referenceAdmissionRetryTick;
    private boolean referenceExecutionScope;

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
        ManagerExecutionProfiler profiler = executionProfiler;
        boolean profiling = profiler != null && profiler.isCollecting();
        if (profiling) profiledDueUpdate = false;
        try {
            updateServer();
        } finally {
            long elapsedNs = System.nanoTime() - startNs;
            lastTickTimeMs = elapsedNs / 1_000_000.0;
            peakTickTimeMs = Math.max(peakTickTimeMs, lastTickTimeMs);
            tickTimes[tickTimeIndex] = lastTickTimeMs;
            tickTimeIndex = (tickTimeIndex + 1) % tickTimes.length;
            timedUpdateCount++;
            if (profiling && executionProfiler == profiler) profiler.recordUpdate(elapsedNs, profiledDueUpdate);
            ManagerTickProfiler.INSTANCE.recordUpdate(elapsedNs);
            ManagerPhaseProfiler.update(elapsedNs);
        }
    }

    private void updateServer() {
        if (blockItemRecovery() || recoverFluid()) {
            resetRedstoneTriggerTracker();
            return;
        }
        if (!prepareExecutableDisk(true)) return;
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
        if (referenceBinding == null) {
            String code = ItemDisk.getProgramString(diskStack);
            if (!code.equals(lastCodeCompiled) || programState != null && !programState.isCurrent(diskStack))
                recompile(code);
        }
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

        if (!prepareExecutableDisk(false)) return;
        if (referenceBinding == null) {
            String currentCode = ItemDisk.getProgramString(diskStack);
            if (!currentCode.equals(lastCodeCompiled)) recompile(currentCode);
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
        boolean privateProgram = referenceBinding != null;
        try {
            tickCompiledProgram(context);

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
            statusMessage = privateProgram ? "Published program runtime error; manager paused"
                : "Runtime error (paused): " + e.getMessage();
            log(
                LogLevel.ERROR,
                privateProgram ? "Published program runtime failed; manager paused"
                    : "Runtime exception: " + e.getMessage());
        } finally {
            if (executionProfiler != null && executionProfiler.isCollecting())
                profiledDueUpdate = context.didSomething();
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
        if (!prepareExecutableDisk(false)) return;
        LogLevel prev = this.logLevel;
        this.logLevel = level;

        log(level, "Manual execution triggered with log level: " + level.name());

        if (cableNetwork == null) {
            rebuildNetwork();
        }

        if (diskStack != null && diskStack.getItem() instanceof ItemDisk) {
            if (!prepareExecutableDisk(false)) return;
            if (referenceBinding == null && (!ItemDisk.getProgramString(diskStack)
                .equals(lastCodeCompiled) || programState != null && !programState.isCurrent(diskStack))) {
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
                boolean privateProgram = referenceBinding != null;
                try {
                    tickCompiledProgram(context);
                    long elapsedNs = System.nanoTime() - startNs;
                    double ms = elapsedNs / 1_000_000.0;
                    log(LogLevel.INFO, String.format("Manual run completed in %.2f ms", ms));
                } catch (Exception e) {
                    isRunning = false;
                    resetRedstoneTriggerTracker();
                    markDirty();
                    statusMessage = privateProgram ? "Published program runtime error; manager paused"
                        : "Runtime error (paused): " + e.getMessage();
                    log(
                        LogLevel.ERROR,
                        privateProgram ? "Published program runtime failed; manager paused"
                            : "Manual run failed: " + e.getMessage());
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
        if (referenceExecutionScope || level == null || message == null) return;
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
        return referenceExecutionScope ? LogLevel.OFF : logLevel;
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

    /** Legacy engine diagnostics reconstruct AST/source. Execution authority does not grant source-read authority. */
    private void tickCompiledProgram(ProgramContext context) {
        boolean previous = referenceExecutionScope;
        referenceExecutionScope = previous || referenceBinding != null;
        try {
            compiledProgram.tick(context);
        } finally {
            referenceExecutionScope = previous;
        }
    }

    /** Source-editor/reset APIs never resolve or replace a published executable. */
    private boolean blockUnsupportedProgramDisk() {
        if (ProgramDiskData.mode(diskStack) == ProgramDiskData.Mode.LEGACY) return false;
        prepareExecutableDisk(false);
        return true;
    }

    /** Protected gateways keep tests deterministic while production callbacks remain server-drained. */
    protected ProjectWorkspace executionWorkspace(net.minecraft.world.World expectedWorld) {
        return StationProjectService.INSTANCE.executionWorkspace(expectedWorld);
    }

    protected boolean executionWorkspaceCurrent(ProjectWorkspace expected, net.minecraft.world.World expectedWorld) {
        return !expected.isClosed() && StationProjectService.INSTANCE.isCurrent(expected, expectedWorld);
    }

    protected boolean prepareReferenceExecution(ProjectWorkspace workspace, ExecutionReference reference,
        java.util.function.Consumer<ProjectResult<PreparedExecution>> callback) {
        ReferenceRequest request = referenceRequest;
        return workspace.enqueueExecution(request, reference, request.current, callback);
    }

    protected boolean referenceServerThread() {
        return ca.teamdman.sfm.common.net.ServerUiTasks.isServerThread();
    }

    private void cancelReferenceRequest() {
        if (referenceWorkspace != null && referenceRequest != null)
            referenceWorkspace.cancelExecution(referenceRequest);
        referenceAdmissionQueued = false;
        referenceAdmissionRetryTick = 0;
    }

    private void invalidateReferenceActivation() {
        cancelReferenceRequest();
        referenceActivationGeneration = Math.incrementExact(referenceActivationGeneration);
        referenceRequest = null;
        referenceAttempted = false;
        referenceBinding = null;
        referenceSourceOwner = null;
        referenceWorld = null;
        referenceWorkspace = null;
    }

    /** Flush the previously accepted schema before discarding its execution binding. */
    private boolean retireReferencePlan() {
        referenceSourceOwner = null;
        compiledProgram = null;
        lastCodeCompiled = null;
        clearProgramGlobals();
        resetRedstoneTriggerTracker();
        try {
            flushProgramState();
            programState = null;
            return true;
        } catch (IllegalArgumentException | IllegalStateException error) {
            isRunning = false;
            statusMessage = "Persistent state conflict; manager paused";
            markDirty();
            return false;
        }
    }

    private void failReference(String message) {
        cancelReferenceRequest();
        referenceRequest = null;
        referenceWorkspace = null;
        referenceAttempted = true;
        boolean retired = retireReferencePlan();
        isRunning = false;
        if (retired) statusMessage = message;
        // Fixed operational reasons are useful to an executor; parser/source details are not part of its grant.
        log(LogLevel.WARN, retired ? message : "Persistent state conflict; manager paused");
        markDirty();
    }

    /** Cold requests are admitted only here, on a server update; warm checks never copy NBT, compile or read files. */
    private boolean prepareExecutableDisk(boolean allowQueue) {
        if (referenceBinding != null && referenceWorld == worldObj && referenceBinding.isCurrent(diskStack)) {
            if (referenceWorkspace != null && !executionWorkspaceCurrent(referenceWorkspace, referenceWorld)) {
                failReference("Reference storage changed; use Run to retry");
                return false;
            }
            if (compiledProgram != null && (programState == null || programState.isCurrent(diskStack))) return true;
            if (compiledProgram != null) {
                if (!retireReferencePlan()) {
                    referenceAttempted = true;
                    return false;
                }
                referenceAttempted = false;
            }
            if (allowQueue && (referenceRequest == null && !referenceAttempted
                || referenceRequest != null && !referenceAdmissionQueued
                    && worldObj.getTotalWorldTime() - referenceAdmissionRetryTick >= 0))
                queueReferenceActivation();
            return false;
        }
        ProgramDiskData.Mode mode = ProgramDiskData.mode(diskStack);
        if (mode == ProgramDiskData.Mode.LEGACY) {
            if (referenceBinding != null || referenceRequest != null || referenceAttempted) {
                if (!retireReferencePlan()) return false;
                invalidateReferenceActivation();
            }
            return true;
        }
        if (referenceBinding != null || referenceRequest != null) {
            if (!retireReferencePlan()) {
                referenceAttempted = true;
                return false;
            }
            invalidateReferenceActivation();
        }
        if (mode == ProgramDiskData.Mode.INVALID) {
            if (!referenceAttempted || compiledProgram != null || isRunning)
                failReference("Invalid program disk format; manager paused");
            return false;
        }
        if (referenceAttempted) return false;
        if (!retireReferencePlan()) {
            referenceAttempted = true;
            return false;
        }
        try {
            referenceBinding = ProgramDiskBinding.capture(diskStack);
            referenceWorld = worldObj;
            statusMessage = "Published reference awaiting activation";
        } catch (IllegalArgumentException | IllegalStateException error) {
            failReference("Invalid reference document metadata; manager paused");
            return false;
        }
        if (allowQueue) queueReferenceActivation();
        return false;
    }

    private void queueReferenceActivation() {
        if (!referenceServerThread() || worldObj == null || worldObj.isRemote || isInvalid()) return;
        if (referenceRequest == null && (!worldObj.blockExists(xCoord, yCoord, zCoord)
            || worldObj.getTileEntity(xCoord, yCoord, zCoord) != this)) return;
        if (hasPendingRecovery()) {
            isRunning = false;
            statusMessage = "Transfer recovery pending; reference activation paused";
            return;
        }
        referenceAttempted = true;
        try {
            if (referenceRequest == null) {
                ProjectWorkspace workspace = executionWorkspace(worldObj);
                if (workspace == null || !executionWorkspaceCurrent(workspace, worldObj)) {
                    failReference("Reference storage unavailable; use Run to retry");
                    return;
                }
                referenceWorkspace = workspace;
                referenceRequest = new ReferenceRequest(this, ProgramDiskData.readReference(diskStack));
            }
            ReferenceRequest request = referenceRequest;
            // Full live authority fences run at the bounded FIFO head immediately before worker submission,
            // not for every overflowing tile's retry. Completion rechecks them again before publication.
            referenceAdmissionQueued = prepareReferenceExecution(
                referenceWorkspace,
                request.reference,
                request.callback);
            if (referenceAdmissionQueued) statusMessage = "Loading published program (queued)";
            else {
                // No worker submission, log spam, disk dirtying or new request/closure allocation on overflow retries.
                int phase = (int) Math.floorMod(31L * xCoord + 17L * yCoord + zCoord + diskGeneration, 20L);
                referenceAdmissionRetryTick = worldObj.getTotalWorldTime() + 20 + phase;
                statusMessage = "Loading published program (waiting for capacity)";
            }
        } catch (IllegalArgumentException | IllegalStateException error) {
            failReference("Reference load unavailable; use Run to retry");
        }
    }

    /** Pure server-side fence reused immediately before worker admission and before plan publication. */
    private boolean referenceRequestCurrent(ReferenceRequest request) {
        return referenceRequest == request && referenceActivationGeneration == request.activationGeneration
            && worldObj == request.world
            && xCoord == request.x
            && yCoord == request.y
            && zCoord == request.z
            && !isInvalid()
            && diskStack == request.disk
            && diskGeneration == request.diskGeneration
            && referenceBinding == request.binding
            && request.binding.isCurrent(diskStack)
            && request.stateCurrent()
            && worldObj.blockExists(xCoord, yCoord, zCoord)
            && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
            && executionWorkspaceCurrent(request.workspace, request.world);
    }

    private void completeReference(ReferenceRequest request, ProjectResult<PreparedExecution> result) {
        if (!referenceServerThread() || referenceRequest != request
            || referenceActivationGeneration != request.activationGeneration) return;
        if (!referenceRequestCurrent(request)) {
            failReference("Reference activation became stale; use Run to retry");
            return;
        }
        referenceRequest = null;
        referenceAdmissionQueued = false;
        if (hasPendingRecovery()) {
            failReference("Transfer recovery pending; use Run after recovery to retry");
            return;
        }
        if (result == null || !result.isSuccess()
            || result.value == null
            || result.value.program == null
            || !request.reference.equals(result.value.reference)) {
            failReference("Published program unavailable; use Run to retry");
            return;
        }
        try {
            flushProgramState();
            Program candidate = result.value.program;
            ProgramStateStore state = null;
            if (candidate.getStateSchema()
                .size() != 0 || diskStack.hasTagCompound()
                    && diskStack.getTagCompound()
                        .hasKey(ProgramStateStore.NBT_KEY)) {
                state = ProgramStateStore.activate(diskStack, candidate.getStateSchema());
                state.flush();
            }
            // No fallible compilation or schema reconciliation after publication. Never restore captured run intent.
            programState = state;
            compiledProgram = candidate;
            referenceSourceOwner = result.value.sourceOwner;
            lastCodeCompiled = null;
            clearProgramGlobals();
            resetRedstoneTriggerTracker();
            statusMessage = isRunning ? "Running published program" : "Published program ready; paused";
            markDirty();
        } catch (IllegalArgumentException | IllegalStateException error) {
            failReference("Published program STATE could not be activated; manager paused");
        }
    }

    private static final class ReferenceRequest {

        final net.minecraft.world.World world;
        final ItemStack disk;
        final ProgramDiskBinding binding;
        final ProjectWorkspace workspace;
        final ExecutionReference reference;
        final java.util.function.BooleanSupplier current;
        final java.util.function.Consumer<ProjectResult<PreparedExecution>> callback;
        final long diskGeneration, activationGeneration;
        final int x, y, z;
        final NBTTagCompound root;
        final net.minecraft.nbt.NBTBase state, stateRevision;

        ReferenceRequest(TileEntityManager manager, ExecutionReference reference) {
            world = manager.worldObj;
            x = manager.xCoord;
            y = manager.yCoord;
            z = manager.zCoord;
            disk = manager.diskStack;
            binding = manager.referenceBinding;
            workspace = manager.referenceWorkspace;
            diskGeneration = manager.diskGeneration;
            activationGeneration = manager.referenceActivationGeneration;
            this.reference = reference;
            current = () -> manager.referenceRequestCurrent(this);
            callback = result -> manager.completeReference(this, result);
            root = disk.getTagCompound();
            state = root == null ? null : root.getTag(ProgramStateStore.NBT_KEY);
            stateRevision = state instanceof NBTTagCompound ? ((NBTTagCompound) state).getTag("revision") : null;
        }

        boolean stateCurrent() {
            if (disk.getTagCompound() != root) return false;
            net.minecraft.nbt.NBTBase current = root == null ? null : root.getTag(ProgramStateStore.NBT_KEY);
            return current == state && (!(current instanceof NBTTagCompound)
                || ((NBTTagCompound) current).getTag("revision") == stateRevision);
        }
    }

    public void recompile(String code) {
        if (blockUnsupportedProgramDisk()) return;
        invalidateReferenceActivation();
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
        if (blockUnsupportedProgramDisk()) return "Reference disks cannot be saved as embedded source";
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
        if (blockUnsupportedProgramDisk()) return;
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
        if (ProgramDiskData.mode(diskStack) == ProgramDiskData.Mode.REFERENCE) {
            resetReferenceProgramState();
            return;
        }
        if (diskStack == null || blockUnsupportedProgramDisk()) return;
        invalidateReferenceActivation();
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

    /** Explicit STATE reset uses only the exact already-loaded private schema; no source/storage resolution. */
    private void resetReferenceProgramState() {
        if (!referenceServerThread()) {
            statusMessage = "State not reset: server operation required";
            return;
        }
        // A rejected request during loading still records the user's pause intent. A later completion cannot resume.
        setRunning(false);
        if (hasPendingRecovery()) {
            statusMessage = "State not reset: resolve transfer recovery first; program paused";
            return;
        }
        if (referenceRequest != null) {
            statusMessage = "State not reset: wait for published program loading to finish; program paused";
            return;
        }
        if (compiledProgram == null || referenceBinding == null
            || referenceWorld != worldObj
            || worldObj == null
            || worldObj.isRemote
            || isInvalid()
            || !referenceBinding.isCurrent(diskStack)
            || referenceWorkspace == null
            || !executionWorkspaceCurrent(referenceWorkspace, worldObj)
            || !worldObj.blockExists(xCoord, yCoord, zCoord)
            || worldObj.getTileEntity(xCoord, yCoord, zCoord) != this) {
            statusMessage = "State not reset: published program is unavailable or changed; program paused";
            return;
        }
        try {
            long nextActivationGeneration = Math.incrementExact(referenceActivationGeneration);
            ProgramStateStore candidate = ProgramStateStore.reset(diskStack, compiledProgram.getStateSchema());
            // Reset deliberately discards old STATE only after complete candidate validation and publication.
            candidate.flush();
            programState = candidate;
            referenceActivationGeneration = nextActivationGeneration;
            clearProgramGlobals();
            resetRedstoneTriggerTracker();
            statusMessage = "Persistent reference variables reset; program paused";
            markDirty();
        } catch (IllegalArgumentException | IllegalStateException | ArithmeticException error) {
            statusMessage = "State not reset: published STATE could not be reset safely; program paused";
            log(LogLevel.ERROR, statusMessage);
        }
    }

    public ItemStack getDisk() {
        return diskStack;
    }

    public Program getCompiledProgram() {
        if (referenceBinding != null) return referenceWorld == worldObj && referenceBinding.isCurrent(diskStack)
            && (referenceWorkspace == null || executionWorkspaceCurrent(referenceWorkspace, referenceWorld))
                ? compiledProgram
                : null;
        return ProgramDiskData.mode(diskStack) == ProgramDiskData.Mode.LEGACY ? compiledProgram : null;
    }

    public boolean hasCompiledProgram() {
        return getCompiledProgram() != null;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public boolean isRunning() {
        return isRunning && ProgramDiskData.mode(diskStack) != ProgramDiskData.Mode.INVALID;
    }

    public boolean canRequestRun() {
        return !hasPendingRecovery()
            && (hasCompiledProgram() || ProgramDiskData.mode(diskStack) == ProgramDiskData.Mode.REFERENCE);
    }

    public void setRunning(boolean running) {
        if (ProgramDiskData.mode(diskStack) == ProgramDiskData.Mode.INVALID) {
            prepareExecutableDisk(false);
            return;
        }
        if (running && ProgramDiskData.mode(diskStack) == ProgramDiskData.Mode.REFERENCE) {
            prepareExecutableDisk(false);
            if (hasPendingRecovery()) return;
            if (referenceRequest == null && compiledProgram == null) {
                referenceAttempted = false;
                referenceWorkspace = null;
                statusMessage = "Published reference retry requested";
            }
        }
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
        executionProfiler = null;
        boolean clearReferencePlan = referenceBinding != null
            || ProgramDiskData.mode(diskStack) != ProgramDiskData.Mode.LEGACY;
        try {
            flushProgramState();
        } finally {
            invalidateReferenceActivation();
            if (clearReferencePlan) {
                compiledProgram = null;
                lastCodeCompiled = null;
            }
        }
        resetRedstoneTriggerTracker();
        clearProgramGlobals();
        cableNetwork = null;
        super.onChunkUnload();
    }

    @Override
    public void invalidate() {
        executionProfiler = null;
        boolean clearReferencePlan = referenceBinding != null
            || ProgramDiskData.mode(diskStack) != ProgramDiskData.Mode.LEGACY;
        try {
            flushProgramState();
        } finally {
            invalidateReferenceActivation();
            if (clearReferencePlan) {
                compiledProgram = null;
                lastCodeCompiled = null;
            }
            clearProgramGlobals();
            resetRedstoneTriggerTracker();
            super.invalidate();
        }
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
        if (blockUnsupportedProgramDisk()) return;
        invalidateReferenceActivation();
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
            executionProfiler = null;
            invalidateReferenceActivation();
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
            executionProfiler = null;
            flushProgramState();
            programState = null;
            invalidateReferenceActivation();
            diskGeneration = Math.incrementExact(diskGeneration);
            diskStack = stack;
            clearProgramGlobals();
            if (diskStack != null && diskStack.getItem() instanceof ItemDisk) {
                log(LogLevel.INFO, "Disk inserted");
                if (!blockUnsupportedProgramDisk()) recompile(ItemDisk.getProgramString(diskStack));
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
        prepareExecutableDisk(false);
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
        executionProfiler = null;
        // Deserialization replaces the physical disk even when its document/source NBT is identical. Fence existing
        // editor/control authority independently of the reference loader; client packets retain server-issued tokens.
        diskGeneration = Math.incrementExact(diskGeneration);
        invalidateReferenceActivation();
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
                if (!blockUnsupportedProgramDisk()) recompile(ItemDisk.getProgramString(diskStack));
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
        NBTTagCompound itemData = syncData.getCompoundTag("disk");
        if (itemData.hasKey("tag", 10) && itemData.getCompoundTag("tag")
            .hasKey(ProgramDiskData.NBT_REFERENCE)) {
            // ItemStack.writeToNBT may retain its live tag compound. Redact only a deep snapshot: nearby block
            // watchers have no physical-inventory authority to receive a reference's bearer grant or source.
            syncData = (NBTTagCompound) syncData.copy();
            NBTTagCompound diskData = syncData.getCompoundTag("disk")
                .getCompoundTag("tag");
            diskData.setTag(ProgramDiskData.NBT_REFERENCE, new NBTTagCompound());
            diskData.removeTag(ItemDisk.NBT_PROGRAM);
            // Keep the marker so a client cannot mistake this source-free mirror for an editable legacy disk.
        }
        syncData.setString("statusMessage", statusMessage);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, syncData);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
        // A redacted block mirror is deliberately INVALID, but its server-authored run intent must survive until
        // authorized inventory synchronization supplies the physical reference. This never grants a local plan.
        isRunning = pkt.func_148857_g()
            .getBoolean("isRunning");
        if (pkt.func_148857_g()
            .hasKey("statusMessage")) {
            statusMessage = pkt.func_148857_g()
                .getString("statusMessage");
        }
    }
}
