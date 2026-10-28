package ca.teamdman.sfm.common.program;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;

import ca.teamdman.sfm.common.inventory.InventoryUtils;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.recovery.ItemRecovery;
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

    private ca.teamdman.sfml.data.StateAccess stateAccess;
    private ca.teamdman.sfml.data.DataRuntime dataRuntime;

    public void setStateAccess(ca.teamdman.sfml.data.StateAccess stateAccess) {
        this.stateAccess = stateAccess;
    }

    public ca.teamdman.sfml.data.StateAccess getStateAccess() {
        return stateAccess;
    }

    public ca.teamdman.sfml.data.DataRuntime getDataRuntime() {
        if (dataRuntime == null) throw new IllegalStateException("Data execution frame has not started");
        return dataRuntime;
    }

    public void beginDataInvocation(int localCount, boolean needsState) {
        if (needsState && stateAccess == null)
            throw new IllegalStateException("Persistent STATE requires an active disk state store");
        dataRuntime = new ca.teamdman.sfml.data.DataRuntime(stateAccess, localCount);
    }

    private final World world;
    private final TileEntityManager manager;
    private final CableNetwork network;
    private LabelPositionHolder labelPositions;
    private final Set<String> referencedLabels;
    private final long managerTick;
    private final boolean redstonePulse;
    private final List<InputStatement> inputs = new ArrayList<>();
    private final Map<InputStatement, List<InputSlot>> inputCache = new IdentityHashMap<>();
    private final Map<InputStatement, List<BlockPos>> inputPositions = new IdentityHashMap<>();
    private final Map<InputStatement, Map<Object, Map<ResourceLimit, TransferQuota>>> fluidInputQuotas = new IdentityHashMap<>();
    private boolean didSomething = false;

    public ProgramContext(World world, TileEntityManager manager, CableNetwork network,
        LabelPositionHolder labelPositions, long managerTick, boolean redstonePulse) {
        this(world, manager, network, labelPositions, managerTick, redstonePulse, null);
    }

    public ProgramContext(World world, TileEntityManager manager, CableNetwork network,
        LabelPositionHolder labelPositions, long managerTick, boolean redstonePulse, Set<String> referencedLabels) {
        this.referencedLabels = referencedLabels;
        this.world = world;
        this.manager = manager;
        this.network = network;
        this.labelPositions = labelPositions;
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
        if (labelPositions == null) labelPositions = manager == null ? new LabelPositionHolder()
            : LabelPositionHolder.from(manager.getDisk(), referencedLabels);
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
        if (label == null) return 0;
        Set<BlockPos> positions = getLabelPositionHolder().getPositions(label);
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
        stmt = stmt.resolve(this);
        inputs.add(stmt);
        inputCache.remove(stmt);
        inputPositions.remove(stmt);
        fluidInputQuotas.remove(stmt);
        fluidReservations.remove(stmt);
        reservedFluids.remove(stmt);
    }

    public void clearInputsForLabel(String label) {
        for (int i = inputs.size() - 1; i >= 0; i--) {
            InputStatement old = inputs.get(i);
            List<Label> keep = new ArrayList<>();
            boolean found = false;
            for (Label entry : old.getLabelAccess()
                .getLabels()) {
                if (entry.getName()
                    .equals(label)) found = true;
                else keep.add(entry);
            }
            if (!found) continue;
            if (keep.isEmpty()) {
                inputs.remove(i);
                continue;
            }
            ca.teamdman.sfml.ast.LabelAccess access = new ca.teamdman.sfml.ast.LabelAccess(
                keep,
                old.getLabelAccess()
                    .getSides(),
                old.getLabelAccess()
                    .getSlots(),
                old.getLabelAccess()
                    .getRoundRobin());
            InputStatement replacement = new InputStatement(access, old.getResourceLimits(), old.isEach());
            inputs.set(i, replacement);
            Set<BlockPos> remaining = new java.util.LinkedHashSet<>();
            for (Label entry : keep) remaining.addAll(getLabelPositionHolder().getPositions(entry.getName()));
            if (inputCache.containsKey(old)) {
                List<InputSlot> slots = new ArrayList<>(inputCache.get(old));
                slots.removeIf(slot -> !remaining.contains(slot.pos));
                inputCache.put(replacement, slots);
                List<BlockPos> positions = new ArrayList<>(inputPositions.get(old));
                positions.retainAll(remaining);
                inputPositions.put(replacement, positions);
            }
            if (fluidInputQuotas.containsKey(old)) fluidInputQuotas.put(replacement, fluidInputQuotas.get(old));
            if (fluidReservations.containsKey(old)) fluidReservations.put(replacement, fluidReservations.get(old));
            if (reservedFluids.containsKey(old)) reservedFluids.put(replacement, reservedFluids.get(old));
        }
    }

    public void clearAllInputs() {
        inputs.clear();
        inputCache.clear();
        inputPositions.clear();
        fluidInputQuotas.clear();
        fluidReservations.clear();
        reservedFluids.clear();
    }

    public void executeRedstoneOutput(RedstoneOutputStatement stmt) {
        List<BlockPos> outPositions = stmt.getLabelAccess()
            .getPositions(getLabelPositionHolder());
        List<ForgeDirection> outSides = stmt.getLabelAccess()
            .getSides()
            .resolveDirections();

        int strength = stmt.getLevel();
        boolean anyEmitted = false;

        if (Compat.isSFMLoaded()) {
            for (BlockPos outPos : outPositions) {
                if (!network.isConnected(outPos)) {
                    if (isLogging(LogLevel.TRACE))
                        log(LogLevel.TRACE, "Block not connected to cable network: " + outPos);
                    continue;
                }
                if (SteveFMCompat.isEmitter(world, outPos)) {
                    boolean success = SteveFMCompat.setRedstoneOutput(world, outPos, outSides, strength, true);
                    if (success) {
                        anyEmitted = true;
                        if (isLogging(LogLevel.INFO)) log(
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
        outputStmt = outputStmt.resolve(this);
        if (inputs.isEmpty()) return;

        List<BlockPos> outPositions = outputStmt.getLabelAccess()
            .getPositions(getLabelPositionHolder());

        // --- Items Gathering & Transfer ---
        List<InputSlot> inputSlots = new ArrayList<>();
        Map<InputStatement, List<BlockPos>> inPositionsMap = inputPositions;
        for (InputStatement inputStmt : inputs) {
            if (inputCache.containsKey(inputStmt)) {
                inputSlots.addAll(inputCache.get(inputStmt));
                continue;
            }
            List<InputSlot> gathered = new ArrayList<>();
            inputCache.put(inputStmt, gathered);
            Map<Object, Map<ResourceLimit, TransferQuota>> inputQuotas = new HashMap<>();
            if (isLogging(LogLevel.DEBUG)) log(
                LogLevel.DEBUG,
                "Gathering slots for IO statement \n```\n" + inputStmt.toStringPretty() + "\n```\n");
            if (isLogging(LogLevel.TRACE))
                log(LogLevel.TRACE, "Statement cache miss - this is the first time this statement is being gathered");
            if (isLogging(LogLevel.DEBUG)) log(
                LogLevel.DEBUG,
                inputStmt.isEach() ? "EACH keyword used - trackers will be unique to each block"
                    : "EACH keyword not used - trackers will be shared between blocks");
            if (isLogging(LogLevel.DEBUG)) log(LogLevel.DEBUG, "Gathering for: Item (Item)");
            if (isLogging(LogLevel.DEBUG)) log(
                LogLevel.DEBUG,
                "Gathering slots in range set: " + inputStmt.getLabelAccess()
                    .getSlots());

            List<BlockPos> inPositions = inputStmt.getLabelAccess()
                .getPositions(getLabelPositionHolder());
            inPositionsMap.put(inputStmt, inPositions);
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

            Set<TileEntityChest> seenInChests = Collections.newSetFromMap(new IdentityHashMap<>());
            for (BlockPos inPos : inPositions) {
                if (!network.isConnected(inPos)) {
                    if (isLogging(LogLevel.TRACE))
                        log(LogLevel.TRACE, "Block not connected to cable network: " + inPos);
                    continue;
                }
                TileEntity te = inPos.getTileEntity(world);
                if (te instanceof TileEntityChest) {
                    TileEntityChest c = (TileEntityChest) te;
                    if (seenInChests.contains(c)) {
                        if (isLogging(LogLevel.TRACE))
                            log(LogLevel.TRACE, "Double chest partner already gathered: " + inPos);
                        continue;
                    }
                    seenInChests.add(c);
                    TileEntityChest adj = InventoryUtils.getAdjacentChest(c);
                    if (adj != null) seenInChests.add(adj);
                }
                IInventory inInv = InventoryUtils.getInventory(world, inPos);
                if (inInv == null) continue;

                boolean isInSided = inInv instanceof ISidedInventory;
                List<ForgeDirection> inSidesToCheck = isInSided ? inSides
                    : Collections.singletonList(ForgeDirection.UNKNOWN);

                Set<Integer> seenSlots = new java.util.HashSet<>();
                for (ForgeDirection inSide : inSidesToCheck) {
                    int[] accessibleSlots = InventoryUtils.getAccessibleSlots(
                        inInv,
                        inSide,
                        inputStmt.getLabelAccess()
                            .getSlots());
                    for (int slot : accessibleSlots) {
                        ItemStack inStack = inInv.getStackInSlot(slot);
                        if (inStack == null || inStack.stackSize <= 0) {
                            if (isLogging(LogLevel.TRACE))
                                log(LogLevel.TRACE, String.format("Slot %d - skipping - null", slot));
                            continue;
                        }
                        if (!InventoryUtils.canExtract(inInv, slot, inStack, inSide)) {
                            if (isLogging(LogLevel.TRACE))
                                log(LogLevel.TRACE, String.format("Slot %d - skipping - cannot extract", slot));
                            continue;
                        }
                        if (!inputStmt.getResourceLimits()
                            .matchesItem(inStack)) {
                            if (isLogging(LogLevel.TRACE)) log(
                                LogLevel.TRACE,
                                String.format("Slot %d - skipping - %s", slot, getItemName(inStack)));
                            continue;
                        }
                        ResourceLimit matchingLimit = inputStmt.getResourceLimits()
                            .getFirstMatchingLimit(inStack);
                        if (isLogging(LogLevel.TRACE)) log(
                            LogLevel.TRACE,
                            String.format(
                                "Slot %d - tracking - %dx %s - %s",
                                slot,
                                inStack.stackSize,
                                getItemName(inStack),
                                (matchingLimit != null ? matchingLimit.toString() : "all")));
                        if (!seenSlots.add(slot)) continue;
                        TransferQuota quota = quota(inputQuotas, inputStmt.isEach() ? inPos : null, matchingLimit);
                        long retained = quota.reserve(TransferQuota.key(inStack), inStack.stackSize);
                        InputSlot entry = new InputSlot(labelStr, inPos, inSide, slot, inInv, matchingLimit, inStack);
                        entry.quota = quota;
                        entry.retained = retained;
                        gathered.add(entry);
                        inputSlots.add(entry);
                    }
                }
            }
        }

        if (isLogging(LogLevel.INFO)) log(LogLevel.INFO, String.format("Discovered %d input slots", inputSlots.size()));
        if (inputSlots.isEmpty()) {
            if (isLogging(LogLevel.DEBUG)) log(LogLevel.DEBUG, "No input slots, skipping");
            transferFluidsOnly(outputStmt, outPositions, inPositionsMap);
            return;
        }

        if (isLogging(LogLevel.DEBUG))
            log(LogLevel.DEBUG, "Gathering slots for IO statement \n```\n" + outputStmt.toStringPretty() + "\n```\n");
        if (isLogging(LogLevel.DEBUG)) log(
            LogLevel.DEBUG,
            outputStmt.isEach() ? "EACH keyword used - trackers will be unique to each block"
                : "EACH keyword not used - trackers will be shared between blocks");
        if (isLogging(LogLevel.DEBUG)) log(LogLevel.DEBUG, "Gathering for: Item (Item)");
        if (isLogging(LogLevel.DEBUG)) log(
            LogLevel.DEBUG,
            "Gathering slots in range set: " + outputStmt.getLabelAccess()
                .getSlots());

        List<OutputSlot> outputSlots = new ArrayList<>();
        Map<Object, Map<ResourceLimit, TransferQuota>> outputQuotas = new HashMap<>();
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

        Set<TileEntityChest> seenOutChests = Collections.newSetFromMap(new IdentityHashMap<>());
        for (BlockPos outPos : outPositions) {
            if (!network.isConnected(outPos)) {
                if (isLogging(LogLevel.TRACE)) log(LogLevel.TRACE, "Block not connected to cable network: " + outPos);
                continue;
            }
            TileEntity te = outPos.getTileEntity(world);
            if (te instanceof TileEntityChest) {
                TileEntityChest c = (TileEntityChest) te;
                if (seenOutChests.contains(c)) {
                    if (isLogging(LogLevel.TRACE))
                        log(LogLevel.TRACE, "Double chest partner already gathered for output: " + outPos);
                    continue;
                }
                seenOutChests.add(c);
                TileEntityChest adj = InventoryUtils.getAdjacentChest(c);
                if (adj != null) seenOutChests.add(adj);
            }
            IInventory outInv = InventoryUtils.getInventory(world, outPos);
            if (outInv == null) continue;

            boolean isOutSided = outInv instanceof ISidedInventory;
            List<ForgeDirection> outSidesToCheck = isOutSided ? outSides
                : Collections.singletonList(ForgeDirection.UNKNOWN);

            Set<Integer> observedSlots = new java.util.HashSet<>();
            for (ForgeDirection outSide : outSidesToCheck) {
                int[] accessibleSlots = InventoryUtils.getAccessibleSlots(
                    outInv,
                    outSide,
                    outputStmt.getLabelAccess()
                        .getSlots());
                for (int slot : accessibleSlots) {
                    ItemStack outStack = outInv.getStackInSlot(slot);
                    if (outStack != null && observedSlots.add(slot)
                        && outputStmt.getResourceLimits()
                            .matchesItem(outStack)) {
                        ResourceLimit limit = outputStmt.getResourceLimits()
                            .getFirstMatchingLimit(outStack);
                        quota(outputQuotas, outputStmt.isEach() ? outPos : null, limit)
                            .observe(TransferQuota.key(outStack), outStack.stackSize);
                    }
                    if (isLogging(LogLevel.TRACE)) log(
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

        if (isLogging(LogLevel.INFO))
            log(LogLevel.INFO, String.format("Discovered %d output slots", outputSlots.size()));
        if (outputSlots.isEmpty()) {
            if (isLogging(LogLevel.DEBUG)) log(LogLevel.DEBUG, "No output slots, skipping");
            transferFluidsOnly(outputStmt, outPositions, inPositionsMap);
            return;
        }

        TransferQuota sharedOutputQuota = null;
        if (!outputStmt.isEach() && outputStmt.getResourceLimits()
            .getLimits()
            .size() == 1) {
            ResourceLimit limit = outputStmt.getResourceLimits()
                .getLimits()
                .get(0);
            if (!limit.getLimit()
                .getQuantity()
                .isEach()
                && !limit.getLimit()
                    .getRetention()
                    .isEach())
                sharedOutputQuota = quota(outputQuotas, null, limit);
        }
        for (InputSlot inSlot : inputSlots) {
            if (sharedOutputQuota != null && sharedOutputQuota.remaining(null, true) == 0) break;
            ItemStack current = inSlot.inv.getStackInSlot(inSlot.slot);
            if (current == null || !InventoryUtils.areItemStacksEqual(current, inSlot.stack)) continue;
            if (!outputStmt.getResourceLimits()
                .matchesItem(current)) continue;
            Object key = TransferQuota.key(current);
            ResourceLimit outLimit = outputStmt.getResourceLimits()
                .getFirstMatchingLimit(current);
            for (OutputSlot outSlot : outputSlots) {
                current = inSlot.inv.getStackInSlot(inSlot.slot);
                if (current == null || !InventoryUtils.areItemStacksEqual(current, inSlot.stack)) break;
                // Do not count a no-op transfer as satisfying an OUTPUT quota.
                if (inSlot.inv == outSlot.inv && inSlot.slot == outSlot.slot) continue;
                if (inSlot.pos.equals(outSlot.pos) && inSlot.slot == outSlot.slot) continue;
                TransferQuota target = quota(outputQuotas, outputStmt.isEach() ? outSlot.pos : null, outLimit);
                long available = Math.min(current.stackSize - inSlot.retained, inSlot.quota.remaining(key, false));
                if (available <= 0) break;
                int amount = (int) Math
                    .min(Math.min(available, target.remaining(key, true)), current.getMaxStackSize());
                if (amount <= 0) continue;
                ItemStack probe = current.copy();
                probe.stackSize = amount;
                int accepted = InventoryUtils
                    .insertItem(outSlot.inv, outSlot.slot, outSlot.side, probe, true, outSlot.emptySlotsOnly);
                if (accepted <= 0) continue;
                ItemStack extracted;
                try {
                    extracted = InventoryUtils.extractItem(inSlot.inv, inSlot.slot, inSlot.side, accepted, false);
                } catch (RuntimeException e) {
                    // Extraction/markDirty can throw after changing the source: ownership is unknown.
                    probe.stackSize = accepted;
                    preserveItem(inSlot, outSlot, probe, true, "extraction");
                    throw e;
                }
                if (extracted == null) break;
                int inserted;
                try {
                    inserted = InventoryUtils
                        .insertItem(outSlot.inv, outSlot.slot, outSlot.side, extracted, false, outSlot.emptySlotsOnly);
                } catch (RuntimeException e) {
                    preserveItem(inSlot, outSlot, extracted, true, "destination insertion");
                    throw e;
                }
                if (inserted < extracted.stackSize) {
                    ItemStack remainder = extracted.copy();
                    remainder.stackSize -= inserted;
                    restoreItem(inSlot, outSlot, remainder);
                }
                inSlot.quota.moved(key, inserted, false);
                target.moved(key, inserted, true);
                if (isLogging(LogLevel.INFO))
                    log(LogLevel.INFO, "Moved " + inserted + " items from " + inSlot + " into " + outSlot);
                // Only successful bookkeeping can exhaust this quota; rejected pairs need no extra check.
                if (sharedOutputQuota != null && sharedOutputQuota.remaining(null, true) == 0) break;
            }
        }

        transferFluidsOnly(outputStmt, outPositions, inPositionsMap);
    }

    private static TransferQuota quota(Map<Object, Map<ResourceLimit, TransferQuota>> scopes, Object scope,
        ResourceLimit limit) {
        return scopes.computeIfAbsent(scope, k -> new IdentityHashMap<>())
            .computeIfAbsent(limit, TransferQuota::new);
    }

    private void restoreItem(InputSlot source, OutputSlot target, ItemStack remainder) {
        try {
            ItemStack current = source.inv.getStackInSlot(source.slot);
            if (current == null) source.inv.setInventorySlotContents(source.slot, remainder.copy());
            else if (InventoryUtils.areItemStacksEqual(current, remainder)
                && (long) current.stackSize + remainder.stackSize <= source.inv.getInventoryStackLimit()) {
                    ItemStack restored = current.copy();
                    restored.stackSize += remainder.stackSize;
                    source.inv.setInventorySlotContents(source.slot, restored);
                } else {
                    // Entity spawning can be cancelled. Preserve the known remainder on the disk instead.
                    preserveItem(source, target, remainder, false, "rollback conflict");
                    throw new IllegalStateException("Item transfer paused: remainder preserved on disk");
                }
            source.inv.markDirty();
        } catch (RuntimeException e) {
            if (manager == null || !manager.hasPendingRecovery())
                preserveItem(source, target, remainder, true, "source rollback");
            throw e;
        }
    }

    private void preserveItem(InputSlot source, OutputSlot target, ItemStack stack, boolean uncertain, String stage) {
        if (manager == null) throw new IllegalStateException("Item recovery requires a manager");
        NBTTagCompound record = manager.preserveItem(stack, source.pos, source.slot, source.side, uncertain, stage);
        ItemRecovery.setTarget(record, target.pos, target.slot, target.side, world.provider.dimensionId);
        manager.markDirty();
    }

    public boolean isLogging(LogLevel level) {
        return manager != null && (manager.getLogLevel()
            .includes(level) || level == LogLevel.ERROR
            || level == LogLevel.WARN);
    }

    private void transferFluidsOnly(OutputStatement out, List<BlockPos> outPositions,
        Map<InputStatement, List<BlockPos>> inPositionsMap) {
        Map<Object, Map<ResourceLimit, TransferQuota>> outputQuotas = new HashMap<>();
        List<ForgeDirection> outSides = out.getLabelAccess()
            .getSides()
            .resolveDirections();
        // Observe each destination once. Forge 1.7 tank snapshots do not expose tank identity.
        for (BlockPos pos : outPositions) {
            if (!network.isConnected(pos)) continue;
            IFluidHandler tank = InventoryUtils.getFluidHandler(world, pos);
            if (tank == null) continue;
            for (FluidStack stack : InventoryUtils.getVisibleFluids(
                tank,
                out.getLabelAccess()
                    .getSides())) {
                if (!out.getResourceLimits()
                    .matchesFluid(stack)) continue;
                ResourceLimit limit = out.getResourceLimits()
                    .getFirstMatchingLimit(stack);
                quota(outputQuotas, out.isEach() ? pos : null, limit).observe(TransferQuota.key(stack), stack.amount);
            }
        }
        for (InputStatement in : inputs) {
            Map<Object, Map<ResourceLimit, TransferQuota>> sourceQuotas = fluidInputQuotas
                .computeIfAbsent(in, k -> new HashMap<>());
            List<ForgeDirection> sides = in.getLabelAccess()
                .getSides()
                .resolveDirections();
            for (BlockPos sourcePos : inPositionsMap.get(in)) {
                if (!network.isConnected(sourcePos)) continue;
                IFluidHandler source = InventoryUtils.getFluidHandler(world, sourcePos);
                if (source == null) continue;
                // A single physical tank is commonly visible through every face.
                Set<Object> seenResources = new java.util.HashSet<>();
                for (ForgeDirection side : sides) {
                    FluidStack available = source.drain(side, Integer.MAX_VALUE, false);
                    if (available == null || available.amount <= 0
                        || !in.getResourceLimits()
                            .matchesFluid(available)
                        || !out.getResourceLimits()
                            .matchesFluid(available))
                        continue;
                    Object key = TransferQuota.key(available);
                    if (!seenResources.add(key)) continue;
                    ResourceLimit inLimit = in.getResourceLimits()
                        .getFirstMatchingLimit(available);
                    ResourceLimit outLimit = out.getResourceLimits()
                        .getFirstMatchingLimit(available);
                    TransferQuota sourceQuota = quota(sourceQuotas, in.isEach() ? sourcePos : null, inLimit);
                    long retained = fluidRetention(in, sourcePos, key, available.amount, inLimit);
                    int remaining = (int) Math
                        .max(0, Math.min(available.amount - retained, sourceQuota.remaining(key, false)));
                    for (BlockPos destPos : outPositions) {
                        if (remaining <= 0) break;
                        if (sourcePos.equals(destPos) || !network.isConnected(destPos)) continue;
                        IFluidHandler dest = InventoryUtils.getFluidHandler(world, destPos);
                        if (dest == null || dest == source) continue;
                        TransferQuota target = quota(outputQuotas, out.isEach() ? destPos : null, outLimit);
                        for (ForgeDirection destSide : outSides) {
                            int requested = (int) Math.min(remaining, target.remaining(key, true));
                            if (requested <= 0) break;
                            FluidStack candidate = available.copy();
                            candidate.amount = requested;
                            int accepted = Math.max(0, Math.min(requested, dest.fill(destSide, candidate, false)));
                            if (accepted == 0) continue;
                            candidate.amount = accepted;
                            // Fluid-specific drain prevents extracting a different fluid after callbacks.
                            FluidStack extracted = source.drain(side, candidate, true);
                            if (extracted == null || extracted.amount <= 0) continue;
                            int inserted = extracted.isFluidEqual(candidate)
                                ? Math.max(0, Math.min(extracted.amount, dest.fill(destSide, extracted.copy(), true)))
                                : 0;
                            int leftover = extracted.amount - inserted;
                            if (leftover > 0) {
                                FluidStack remainder = extracted.copy();
                                remainder.amount = leftover;
                                int returned = Math
                                    .max(0, Math.min(leftover, source.fill(side, remainder.copy(), true)));
                                remainder.amount -= returned;
                                if (remainder.amount > 0) {
                                    if (manager == null)
                                        throw new IllegalStateException("Fluid rollback requires a manager");
                                    manager.preserveFluid(remainder, sourcePos, side);
                                    throw new IllegalStateException(
                                        "Fluid transfer paused: remainder preserved on disk");
                                }
                            }
                            remaining -= inserted;
                            sourceQuota.moved(key, inserted, false);
                            target.moved(key, inserted, true);
                            if (inserted > 0 && isLogging(LogLevel.INFO))
                                log(LogLevel.INFO, "Moved " + inserted + "mB from " + sourcePos + " to " + destPos);
                        }
                    }
                }
            }
        }
    }

    private final Map<InputStatement, Map<Object, Map<ResourceLimit, TransferQuota>>> fluidReservations = new IdentityHashMap<>();
    private final Map<InputStatement, Map<BlockPos, Map<Object, Long>>> reservedFluids = new IdentityHashMap<>();

    private long fluidRetention(InputStatement statement, BlockPos pos, Object key, int amount, ResourceLimit limit) {
        Map<Object, Long> byResource = reservedFluids.computeIfAbsent(statement, k -> new HashMap<>())
            .computeIfAbsent(pos, k -> new HashMap<>());
        return byResource.computeIfAbsent(
            key,
            k -> quota(
                fluidReservations.computeIfAbsent(statement, ignored -> new HashMap<>()),
                statement.isEach() ? pos : null,
                limit).reserve(key, amount));
    }

    public static class InputSlot {

        public final String label;
        public final BlockPos pos;
        public final ForgeDirection side;
        public final int slot;
        public final IInventory inv;
        public final ResourceLimit limit;
        public final ItemStack stack;
        private TransferQuota quota;
        private long retained;

        public InputSlot(String label, BlockPos pos, ForgeDirection side, int slot, IInventory inv, ResourceLimit limit,
            ItemStack stack) {
            this.label = label;
            this.pos = pos;
            this.side = side;
            this.slot = slot;
            this.inv = inv;
            this.limit = limit;
            this.stack = stack.copy();
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
