package ca.teamdman.sfm.common.program;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

import ca.teamdman.sfm.common.inventory.InventoryUtils;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.tileentity.LogLevel;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.ast.InputStatement;
import ca.teamdman.sfml.ast.Label;
import ca.teamdman.sfml.ast.OutputStatement;
import ca.teamdman.sfml.ast.ResourceLimit;
import ca.teamdman.sfml.ast.ResourceLimits;

public final class ProgramContext {

    private final World world;
    private final TileEntityManager manager;
    private final CableNetwork network;
    private final LabelPositionHolder labelPositions;
    private final long managerTick;
    private final boolean redstonePulse;
    private final List<InputStatement> inputs = new ArrayList<>();

    public ProgramContext(World world, TileEntityManager manager, CableNetwork network,
        LabelPositionHolder labelPositions, long managerTick, boolean redstonePulse) {
        this.world = world;
        this.manager = manager;
        this.network = network;
        this.labelPositions = labelPositions != null ? labelPositions : new LabelPositionHolder();
        this.managerTick = managerTick;
        this.redstonePulse = redstonePulse;
    }

    public void log(LogLevel level, String message) {
        if (manager != null) {
            manager.log(level, message);
        }
    }

    public World getWorld() {
        return world;
    }

    public TileEntityManager getManager() {
        return manager;
    }

    public CableNetwork getNetwork() {
        return network;
    }

    public LabelPositionHolder getLabelPositionHolder() {
        return labelPositions;
    }

    public long getManagerTick() {
        return managerTick;
    }

    public long getWorldTime() {
        return world != null ? world.getTotalWorldTime() : managerTick;
    }

    public boolean hasRedstonePulse() {
        return redstonePulse;
    }

    public int getRedstonePower() {
        if (manager == null || world == null) return 0;
        return world.getBlockPowerInput(manager.xCoord, manager.yCoord, manager.zCoord);
    }

    public void addInputStatement(InputStatement stmt) {
        inputs.add(stmt);
    }

    public void clearInputsForLabel(String label) {
        inputs.removeIf(stmt -> {
            for (Label l : stmt.getLabelAccess()
                .getLabels()) {
                if (l.getName()
                    .equals(label)) return true;
            }
            return false;
        });
    }

    public void clearAllInputs() {
        inputs.clear();
    }

    public void executeOutput(OutputStatement outputStmt) {
        if (inputs.isEmpty()) return;

        List<BlockPos> outputPositions = outputStmt.getLabelAccess()
            .getPositions(labelPositions);
        if (outputPositions.isEmpty()) {
            if (manager != null && manager.getLogLevel()
                .includes(LogLevel.DEBUG)) {
                log(LogLevel.DEBUG, "Output statement target had 0 positions");
            }
            inputs.clear();
            return;
        }

        // Process transfers for all queued input statements
        for (InputStatement inputStmt : inputs) {
            List<BlockPos> inputPositions = inputStmt.getLabelAccess()
                .getPositions(labelPositions);
            if (inputPositions.isEmpty()) continue;

            transferItems(inputStmt, outputStmt, inputPositions, outputPositions);
            transferFluids(inputStmt, outputStmt, inputPositions, outputPositions);
        }

        inputs.clear();
    }

    private void transferItems(InputStatement in, OutputStatement out, List<BlockPos> inPositions,
        List<BlockPos> outPositions) {
        ResourceLimits inLimits = in.getResourceLimits();
        ResourceLimits outLimits = out.getResourceLimits();

        List<ForgeDirection> inSides = in.getLabelAccess()
            .getSides()
            .resolveDirections();
        List<ForgeDirection> outSides = out.getLabelAccess()
            .getSides()
            .resolveDirections();

        Map<ResourceLimit, Long> movedPerLimit = new HashMap<>();

        for (BlockPos inPos : inPositions) {
            if (!network.isConnected(inPos)) {
                if (manager != null && manager.getLogLevel()
                    .includes(LogLevel.TRACE)) {
                    log(LogLevel.TRACE, "Block not connected to cable network: " + inPos);
                }
                continue;
            }
            IInventory inInv = InventoryUtils.getInventory(world, inPos);
            if (inInv == null) continue;

            for (ForgeDirection inSide : inSides) {
                int[] inSlots = InventoryUtils.getAccessibleSlots(
                    inInv,
                    inSide,
                    in.getLabelAccess()
                        .getSlots());

                for (int inSlot : inSlots) {
                    ItemStack inStack = inInv.getStackInSlot(inSlot);
                    if (inStack == null || inStack.stackSize <= 0) continue;
                    if (!inLimits.matchesItem(inStack)) continue;
                    if (!outLimits.matchesItem(inStack)) continue;

                    ResourceLimit inLimit = inLimits.getFirstMatchingLimit(inStack);
                    long maxAllowedToMove = Long.MAX_VALUE;
                    long retentionRequired = 0;

                    if (inLimit != null) {
                        maxAllowedToMove = inLimit.getLimit()
                            .getQuantity()
                            .getAmount();
                        retentionRequired = inLimit.getLimit()
                            .getRetention()
                            .getAmount();
                    }

                    long alreadyMoved = movedPerLimit.getOrDefault(inLimit, 0L);
                    long remainingQuota = maxAllowedToMove - alreadyMoved;
                    if (remainingQuota <= 0) continue;

                    long availableAfterRetention = inStack.stackSize - retentionRequired;
                    if (availableAfterRetention <= 0) continue;

                    long toTransfer = Math.min(availableAfterRetention, remainingQuota);

                    // Push to output positions
                    for (BlockPos outPos : outPositions) {
                        if (!network.isConnected(outPos)) continue;
                        IInventory outInv = InventoryUtils.getInventory(world, outPos);
                        if (outInv == null) continue;

                        for (ForgeDirection outSide : outSides) {
                            int[] outSlots = InventoryUtils.getAccessibleSlots(
                                outInv,
                                outSide,
                                out.getLabelAccess()
                                    .getSlots());

                            for (int outSlot : outSlots) {
                                if (toTransfer <= 0) break;

                                ItemStack probe = inStack.copy();
                                probe.stackSize = (int) Math.min(toTransfer, probe.getMaxStackSize());

                                int accepted = InventoryUtils
                                    .insertItem(outInv, outSlot, outSide, probe, true, out.isEmptySlotsOnly());
                                if (accepted > 0) {
                                    ItemStack extracted = InventoryUtils
                                        .extractItem(inInv, inSlot, inSide, accepted, false);
                                    if (extracted != null && extracted.stackSize > 0) {
                                        InventoryUtils.insertItem(
                                            outInv,
                                            outSlot,
                                            outSide,
                                            extracted,
                                            false,
                                            out.isEmptySlotsOnly());
                                        toTransfer -= extracted.stackSize;
                                        movedPerLimit.put(inLimit, alreadyMoved + extracted.stackSize);

                                        if (manager != null && manager.getLogLevel()
                                            .includes(LogLevel.DEBUG)) {
                                            log(
                                                LogLevel.DEBUG,
                                                String.format(
                                                    "Moved %dx %s from %s to %s",
                                                    extracted.stackSize,
                                                    extracted.getDisplayName(),
                                                    inPos,
                                                    outPos));
                                        }
                                    }
                                }
                            }
                            if (toTransfer <= 0) break;
                        }
                        if (toTransfer <= 0) break;
                    }
                }
            }
        }
    }

    private void transferFluids(InputStatement in, OutputStatement out, List<BlockPos> inPositions,
        List<BlockPos> outPositions) {
        ResourceLimits inLimits = in.getResourceLimits();
        ResourceLimits outLimits = out.getResourceLimits();

        List<ForgeDirection> inSides = in.getLabelAccess()
            .getSides()
            .resolveDirections();
        List<ForgeDirection> outSides = out.getLabelAccess()
            .getSides()
            .resolveDirections();

        for (BlockPos inPos : inPositions) {
            if (!network.isConnected(inPos)) continue;
            IFluidHandler inTank = InventoryUtils.getFluidHandler(world, inPos);
            if (inTank == null) continue;

            for (ForgeDirection inSide : inSides) {
                FluidStack drainedSim = inTank.drain(inSide, Integer.MAX_VALUE, false);
                if (drainedSim == null || drainedSim.amount <= 0) continue;
                if (!inLimits.matchesFluid(drainedSim)) continue;
                if (!outLimits.matchesFluid(drainedSim)) continue;

                ResourceLimit inLimit = inLimits.getFirstMatchingLimit(drainedSim);
                long maxAllowed = inLimit != null ? inLimit.getLimit()
                    .getQuantity()
                    .getAmount() : Long.MAX_VALUE;
                long retention = inLimit != null ? inLimit.getLimit()
                    .getRetention()
                    .getAmount() : 0;

                long availableAfterRetention = drainedSim.amount - retention;
                if (availableAfterRetention <= 0) continue;

                int toDrain = (int) Math.min(availableAfterRetention, maxAllowed);

                for (BlockPos outPos : outPositions) {
                    if (!network.isConnected(outPos)) continue;
                    IFluidHandler outTank = InventoryUtils.getFluidHandler(world, outPos);
                    if (outTank == null) continue;

                    for (ForgeDirection outSide : outSides) {
                        FluidStack candidate = drainedSim.copy();
                        candidate.amount = toDrain;

                        int filled = outTank.fill(outSide, candidate, false);
                        if (filled > 0) {
                            FluidStack actualDrained = inTank.drain(inSide, filled, true);
                            if (actualDrained != null && actualDrained.amount > 0) {
                                outTank.fill(outSide, actualDrained, true);
                                toDrain -= actualDrained.amount;

                                if (manager != null && manager.getLogLevel()
                                    .includes(LogLevel.DEBUG)) {
                                    log(
                                        LogLevel.DEBUG,
                                        String.format(
                                            "Moved %dmB %s from %s to %s",
                                            actualDrained.amount,
                                            actualDrained.getFluid()
                                                .getLocalizedName(actualDrained),
                                            inPos,
                                            outPos));
                                }
                            }
                        }
                        if (toDrain <= 0) break;
                    }
                    if (toDrain <= 0) break;
                }
            }
        }
    }
}
