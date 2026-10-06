package ca.teamdman.sfm.common.program;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
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
import ca.teamdman.sfm.compat.Compat;
import ca.teamdman.sfm.compat.stevefm.SteveFMCompat;
import ca.teamdman.sfml.ast.InputStatement;
import ca.teamdman.sfml.ast.Label;
import ca.teamdman.sfml.ast.OutputStatement;
import ca.teamdman.sfml.ast.RedstoneOutputStatement;
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
    private boolean didSomething = false;

    public ProgramContext(World world, TileEntityManager manager, CableNetwork network,
        LabelPositionHolder labelPositions, long managerTick, boolean redstonePulse) {
        this.world = world;
        this.manager = manager;
        this.network = network;
        this.labelPositions = labelPositions != null ? labelPositions : new LabelPositionHolder();
        this.managerTick = managerTick;
        this.redstonePulse = redstonePulse;
    }

    public boolean didSomething() {
        return didSomething;
    }

    public void setDidSomething(boolean didSomething) {
        this.didSomething = didSomething;
    }

    @Override
    public String toString() {
        return "ProgramContext{managerPos=" + (manager != null ? manager.getBlockPos() : "null")
            + ", tick="
            + managerTick
            + ", pulse="
            + redstonePulse
            + "}";
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
        int power = world.getBlockPowerInput(manager.xCoord, manager.yCoord, manager.zCoord);
        if (Compat.isSFMLoaded() && network != null) {
            try {
                int receiverPower = SteveFMCompat.getMaxReceiverPower(world, network.getPeripherals());
                if (receiverPower > power) {
                    power = receiverPower;
                }
            } catch (Throwable ignored) {}
        }
        return power;
    }

    public int getRedstonePower(BlockPos pos) {
        if (world == null || pos == null) return 0;
        int power = world.getBlockPowerInput(pos.x, pos.y, pos.z);
        if (Compat.isSFMLoaded()) {
            try {
                int receiverPower = SteveFMCompat.getReceiverPower(world, pos);
                if (receiverPower > power) {
                    power = receiverPower;
                }
            } catch (Throwable ignored) {}
        }
        return power;
    }

    public int getRedstonePower(String label) {
        if (labelPositions == null || label == null) return 0;
        Set<BlockPos> positions = labelPositions.getPositions(label);
        int max = 0;
        for (BlockPos pos : positions) {
            int p = getRedstonePower(pos);
            if (p > max) {
                max = p;
                if (max == 15) break;
            }
        }
        return max;
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

    public void executeRedstoneOutput(RedstoneOutputStatement stmt) {
        List<BlockPos> outPositions = stmt.getLabelAccess()
            .getPositions(labelPositions);
        List<ForgeDirection> outSides = stmt.getLabelAccess()
            .getSides()
            .resolveDirections();

        int strength = stmt.getLevel();
        boolean anyEmitted = false;

        if (Compat.isSFMLoaded()) {
            for (BlockPos outPos : outPositions) {
                if (!network.isConnected(outPos)) {
                    log(LogLevel.TRACE, "Block not connected to cable network: " + outPos);
                    continue;
                }
                if (SteveFMCompat.isEmitter(world, outPos)) {
                    boolean success = SteveFMCompat.setRedstoneOutput(world, outPos, outSides, strength, true);
                    if (success) {
                        anyEmitted = true;
                        log(
                            LogLevel.INFO,
                            String.format("Set redstone emitter at %s to strength %d", outPos, strength));
                    }
                }
            }
        }

        if (anyEmitted) {
            setDidSomething(true);
        }
    }

    public void executeOutput(OutputStatement outputStmt) {
        if (inputs.isEmpty()) return;

        // --- Items Gathering & Transfer ---
        List<InputSlot> inputSlots = new ArrayList<>();
        for (InputStatement inputStmt : inputs) {
            log(LogLevel.DEBUG, "Gathering slots for IO statement \n```\n" + inputStmt.toStringPretty() + "\n```\n");
            log(LogLevel.TRACE, "Statement cache miss - this is the first time this statement is being gathered");
            log(
                LogLevel.DEBUG,
                inputStmt.isEach() ? "EACH keyword used - trackers will be unique to each block"
                    : "EACH keyword not used - trackers will be shared between blocks");
            log(LogLevel.DEBUG, "Gathering for: Item (Item)");
            log(
                LogLevel.DEBUG,
                "Gathering slots in range set: " + inputStmt.getLabelAccess()
                    .getSlots());

            List<BlockPos> inPositions = inputStmt.getLabelAccess()
                .getPositions(labelPositions);
            List<ForgeDirection> inSides = inputStmt.getLabelAccess()
                .getSides()
                .resolveDirections();
            String labelStr = inputStmt.getLabelAccess()
                .getLabels()
                .isEmpty() ? ""
                    : inputStmt.getLabelAccess()
                        .getLabels()
                        .get(0)
                        .getName();

            for (BlockPos inPos : inPositions) {
                if (!network.isConnected(inPos)) {
                    log(LogLevel.TRACE, "Block not connected to cable network: " + inPos);
                    continue;
                }
                IInventory inInv = InventoryUtils.getInventory(world, inPos);
                if (inInv == null) continue;

                boolean isInSided = inInv instanceof ISidedInventory;
                List<ForgeDirection> inSidesToCheck = isInSided ? inSides
                    : Collections.singletonList(ForgeDirection.UNKNOWN);

                for (ForgeDirection inSide : inSidesToCheck) {
                    int[] accessibleSlots = InventoryUtils.getAccessibleSlots(
                        inInv,
                        inSide,
                        inputStmt.getLabelAccess()
                            .getSlots());
                    for (int slot : accessibleSlots) {
                        ItemStack inStack = inInv.getStackInSlot(slot);
                        if (inStack == null || inStack.stackSize <= 0) {
                            log(LogLevel.TRACE, String.format("Slot %d - skipping - null", slot));
                            continue;
                        }
                        if (!InventoryUtils.canExtract(inInv, slot, inStack, inSide)) {
                            log(LogLevel.TRACE, String.format("Slot %d - skipping - cannot extract", slot));
                            continue;
                        }
                        if (!inputStmt.getResourceLimits()
                            .matchesItem(inStack)) {
                            log(LogLevel.TRACE, String.format("Slot %d - skipping - %s", slot, getItemName(inStack)));
                            continue;
                        }
                        ResourceLimit matchingLimit = inputStmt.getResourceLimits()
                            .getFirstMatchingLimit(inStack);
                        log(
                            LogLevel.TRACE,
                            String.format(
                                "Slot %d - tracking - %dx %s - %s",
                                slot,
                                inStack.stackSize,
                                getItemName(inStack),
                                (matchingLimit != null ? matchingLimit.toString() : "all")));
                        inputSlots.add(new InputSlot(labelStr, inPos, inSide, slot, inInv, matchingLimit, inStack));
                    }
                }
            }
        }

        log(LogLevel.INFO, String.format("Discovered %d input slots", inputSlots.size()));
        if (inputSlots.isEmpty()) {
            log(LogLevel.DEBUG, "No input slots, skipping");
            transferFluidsOnly(outputStmt);
            inputs.clear();
            return;
        }

        log(LogLevel.DEBUG, "Gathering slots for IO statement \n```\n" + outputStmt.toStringPretty() + "\n```\n");
        log(
            LogLevel.DEBUG,
            outputStmt.isEach() ? "EACH keyword used - trackers will be unique to each block"
                : "EACH keyword not used - trackers will be shared between blocks");
        log(LogLevel.DEBUG, "Gathering for: Item (Item)");
        log(
            LogLevel.DEBUG,
            "Gathering slots in range set: " + outputStmt.getLabelAccess()
                .getSlots());

        List<OutputSlot> outputSlots = new ArrayList<>();
        List<BlockPos> outPositions = outputStmt.getLabelAccess()
            .getPositions(labelPositions);
        List<ForgeDirection> outSides = outputStmt.getLabelAccess()
            .getSides()
            .resolveDirections();
        String outLabelStr = outputStmt.getLabelAccess()
            .getLabels()
            .isEmpty() ? ""
                : outputStmt.getLabelAccess()
                    .getLabels()
                    .get(0)
                    .getName();

        for (BlockPos outPos : outPositions) {
            if (!network.isConnected(outPos)) {
                log(LogLevel.TRACE, "Block not connected to cable network: " + outPos);
                continue;
            }
            IInventory outInv = InventoryUtils.getInventory(world, outPos);
            if (outInv == null) continue;

            boolean isOutSided = outInv instanceof ISidedInventory;
            List<ForgeDirection> outSidesToCheck = isOutSided ? outSides
                : Collections.singletonList(ForgeDirection.UNKNOWN);

            for (ForgeDirection outSide : outSidesToCheck) {
                int[] accessibleSlots = InventoryUtils.getAccessibleSlots(
                    outInv,
                    outSide,
                    outputStmt.getLabelAccess()
                        .getSlots());
                for (int slot : accessibleSlots) {
                    ItemStack outStack = outInv.getStackInSlot(slot);
                    log(
                        LogLevel.TRACE,
                        String.format(
                            "Slot %d - tracking - %s - %s",
                            slot,
                            (outStack != null ? getItemName(outStack) : "empty"),
                            outputStmt.getResourceLimits()));
                    outputSlots.add(
                        new OutputSlot(
                            outLabelStr,
                            outPos,
                            outSide,
                            slot,
                            outInv,
                            outputStmt.getResourceLimits(),
                            outputStmt.isEmptySlotsOnly()));
                }
            }
        }

        log(LogLevel.INFO, String.format("Discovered %d output slots", outputSlots.size()));
        if (outputSlots.isEmpty()) {
            log(LogLevel.DEBUG, "No output slots, skipping");
            transferFluidsOnly(outputStmt);
            inputs.clear();
            return;
        }

        Map<ResourceLimit, Long> movedPerLimit = new HashMap<>();

        for (InputSlot inSlot : inputSlots) {
            ItemStack currentInStack = inSlot.inv.getStackInSlot(inSlot.slot);
            if (currentInStack == null || currentInStack.stackSize <= 0) continue;
            if (!outputStmt.getResourceLimits()
                .matchesItem(currentInStack)) {
                continue;
            }

            ResourceLimit inLimit = inSlot.limit;
            long maxAllowedToMove = inLimit != null ? inLimit.getLimit()
                .getQuantity()
                .getAmount() : Long.MAX_VALUE;
            long retentionRequired = inLimit != null ? inLimit.getLimit()
                .getRetention()
                .getAmount() : 0;

            long alreadyMoved = movedPerLimit.getOrDefault(inLimit, 0L);
            long remainingQuota = maxAllowedToMove - alreadyMoved;
            if (remainingQuota <= 0) continue;

            long availableAfterRetention = currentInStack.stackSize - retentionRequired;
            if (retentionRequired > 0) {
                log(
                    LogLevel.TRACE,
                    String.format(
                        "Promised to leave %d in the source slot, still obligated to leave %d",
                        retentionRequired,
                        Math.max(0, retentionRequired - (currentInStack.stackSize - availableAfterRetention))));
            }
            if (availableAfterRetention <= 0) {
                log(
                    LogLevel.TRACE,
                    "Nothing to move after retention obligations, marking source slot done and skipping");
                continue;
            }

            long toTransfer = Math.min(availableAfterRetention, remainingQuota);

            for (OutputSlot outSlot : outputSlots) {
                if (toTransfer <= 0) break;

                log(LogLevel.TRACE, "Begin moving " + inSlot + " into " + outSlot);

                long maxStackSize = currentInStack.getMaxStackSize();
                long toMove = Math.min(toTransfer, maxStackSize);
                log(
                    LogLevel.TRACE,
                    String.format(
                        "Max transferable dest=%d, source=%d, stack limit=%d; new toMove=%d",
                        maxStackSize,
                        currentInStack.stackSize,
                        maxStackSize,
                        toMove));

                if (toMove <= 0) {
                    log(LogLevel.TRACE, "toMove=0, skipping");
                    continue;
                }

                ItemStack probe = currentInStack.copy();
                probe.stackSize = (int) toMove;

                int accepted = InventoryUtils
                    .insertItem(outSlot.inv, outSlot.slot, outSlot.side, probe, true, outSlot.emptySlotsOnly);
                if (accepted <= 0) {
                    log(
                        LogLevel.TRACE,
                        String.format(
                            "Got remainder %d after simulated insertion of potential %d (0 to move), skipping",
                            toMove,
                            toMove));
                    continue;
                }

                ItemStack extracted = InventoryUtils.extractItem(inSlot.inv, inSlot.slot, inSlot.side, accepted, false);
                if (extracted == null || extracted.stackSize <= 0) {
                    log(LogLevel.TRACE, "extracted nothing, marking this input slot as done");
                    break;
                }

                log(LogLevel.DEBUG, String.format("Extracted %d from slot %d", extracted.stackSize, inSlot.slot));

                InventoryUtils
                    .insertItem(outSlot.inv, outSlot.slot, outSlot.side, extracted, false, outSlot.emptySlotsOnly);

                toTransfer -= extracted.stackSize;
                movedPerLimit.put(inLimit, alreadyMoved + extracted.stackSize);
                alreadyMoved += extracted.stackSize;

                log(
                    LogLevel.INFO,
                    String.format(
                        "Moved %d %s - source=%s, dest=%s",
                        extracted.stackSize,
                        getItemName(extracted),
                        inSlot,
                        outSlot));
            }
        }

        transferFluidsOnly(outputStmt);
        inputs.clear();
    }

    private void transferFluidsOnly(OutputStatement out) {
        for (InputStatement in : inputs) {
            ResourceLimits inLimits = in.getResourceLimits();
            ResourceLimits outLimits = out.getResourceLimits();

            List<ForgeDirection> inSides = in.getLabelAccess()
                .getSides()
                .resolveDirections();
            List<ForgeDirection> outSides = out.getLabelAccess()
                .getSides()
                .resolveDirections();

            List<BlockPos> inPositions = in.getLabelAccess()
                .getPositions(labelPositions);
            List<BlockPos> outPositions = out.getLabelAccess()
                .getPositions(labelPositions);

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

                                    String fluidName = actualDrained.getFluid()
                                        .getName();
                                    String srcStr = "LimitedInputSlot{label=" + (in.getLabelAccess()
                                        .getLabels()
                                        .isEmpty() ? ""
                                            : in.getLabelAccess()
                                                .getLabels()
                                                .get(0)
                                                .getName())
                                        + ", pos="
                                        + inPos
                                        + ", direction="
                                        + inSide
                                        + "}";
                                    String dstStr = "LimitedOutputSlot{label=" + (out.getLabelAccess()
                                        .getLabels()
                                        .isEmpty() ? ""
                                            : out.getLabelAccess()
                                                .getLabels()
                                                .get(0)
                                                .getName())
                                        + ", pos="
                                        + outPos
                                        + ", direction="
                                        + outSide
                                        + "}";
                                    log(
                                        LogLevel.INFO,
                                        String.format(
                                            "Moved %dmB %s - source=%s, dest=%s",
                                            actualDrained.amount,
                                            fluidName,
                                            srcStr,
                                            dstStr));
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

    public static class InputSlot {

        public final String label;
        public final BlockPos pos;
        public final ForgeDirection side;
        public final int slot;
        public final IInventory inv;
        public final ResourceLimit limit;
        public final ItemStack stack;

        public InputSlot(String label, BlockPos pos, ForgeDirection side, int slot, IInventory inv, ResourceLimit limit,
            ItemStack stack) {
            this.label = label;
            this.pos = pos;
            this.side = side;
            this.slot = slot;
            this.inv = inv;
            this.limit = limit;
            this.stack = stack;
        }

        @Override
        public String toString() {
            return "LimitedInputSlot{label=" + label + ", pos=" + pos + ", direction=" + side + ", slot=" + slot + "}";
        }
    }

    public static class OutputSlot {

        public final String label;
        public final BlockPos pos;
        public final ForgeDirection side;
        public final int slot;
        public final IInventory inv;
        public final ResourceLimits limits;
        public final boolean emptySlotsOnly;

        public OutputSlot(String label, BlockPos pos, ForgeDirection side, int slot, IInventory inv,
            ResourceLimits limits, boolean emptySlotsOnly) {
            this.label = label;
            this.pos = pos;
            this.side = side;
            this.slot = slot;
            this.inv = inv;
            this.limits = limits;
            this.emptySlotsOnly = emptySlotsOnly;
        }

        @Override
        public String toString() {
            return "LimitedOutputSlot{label=" + label + ", pos=" + pos + ", direction=" + side + ", slot=" + slot + "}";
        }
    }

    public static String getItemName(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return "null";
        Object objName = net.minecraft.item.Item.itemRegistry.getNameForObject(stack.getItem());
        String name = objName != null ? objName.toString() : stack.getUnlocalizedName();
        if (stack.getItemDamage() != 0
            && stack.getItemDamage() != net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE) {
            return name + "@" + stack.getItemDamage();
        }
        return name;
    }
}
