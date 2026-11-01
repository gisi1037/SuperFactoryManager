package ca.teamdman.sfm.common.program;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.FluidContents;
import ca.teamdman.sfml.data.FluidIdentity;
import ca.teamdman.sfml.data.ItemTransferResult;
import ca.teamdman.sfml.data.ItemTransferResult.Status;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;

/** Fresh, bounded Forge fluid observations and actions. All amounts are millibuckets. */
public final class FluidTransferService {

    private static final int MAX_TANKS_PER_HANDLER = 256;
    private static final String RECOVERY = "sfm:recovery_fluid";
    private final ProgramContext context;
    private ItemStack authorityDisk;
    private long authorityGeneration;

    public FluidTransferService(ProgramContext context) {
        this.context = context;
    }

    private static void label(LabelReference label) {
        if (label == null) throw new IllegalArgumentException("Fluid operations require a label");
        if (label.hasSlotSelection())
            throw new IllegalArgumentException("Fluid operations do not accept item slot restrictions");
    }

    private static void selector(ResourceSelector selector) {
        if (selector == null || selector.getKind() != ResourceSelector.Kind.FLUID)
            throw new IllegalArgumentException("Fluid operations require FLUID selectors");
    }

    static void validateArguments(LabelReference source, LabelReference destination, DataValue filter, long amount) {
        label(source);
        label(destination);
        if (amount < 0) throw new IllegalArgumentException("Fluid transfer amount cannot be negative");
        if (filter == null) throw new IllegalArgumentException("Fluid filter required");
        if (filter.getType() == DataType.RESOURCE_SELECTOR) selector(filter.asSelector());
        else if (filter.getType() != DataType.FLUID_KEY)
            throw new IllegalArgumentException("Fluid transfers require a FLUID selector or fluid key");
    }

    public long count(LabelReference source, ResourceSelector selector, QueryBudget budget) {
        return observation(source, selector, budget, false).amount;
    }

    public boolean empty(LabelReference source, ResourceSelector selector, QueryBudget budget) {
        Observation result = observation(source, selector, budget, true);
        return result.complete && result.amount == 0;
    }

    public boolean available(LabelReference source, ResourceSelector selector, QueryBudget budget) {
        return observation(source, selector, budget, true).complete;
    }

    public FluidContents contents(LabelReference source, ResourceSelector selector, QueryBudget budget) {
        Observation result = observation(source, selector, budget, false);
        budget.chargeAllocation(1);
        List<FluidContents.Row> rows = new ArrayList<>();
        for (Endpoint endpoint : result.endpoints) for (int i = 0; i < endpoint.tanks.size(); i++) {
            Tank tank = endpoint.tanks.get(i);
            budget.chargeSlot();
            if (tank.amount == 0 || !selected(selector, tank.fluid)) continue;
            budget.chargeAllocation(2);
            rows.add(
                new FluidContents.Row(
                    tank.fluid,
                    tank.amount,
                    context.getWorld().provider.dimensionId,
                    endpoint.position.x,
                    endpoint.position.y,
                    endpoint.position.z,
                    i,
                    endpoint.sideMask));
        }
        return new FluidContents(budget, rows);
    }

    private Observation observation(LabelReference source, ResourceSelector selector, QueryBudget budget,
        boolean allowUnavailable) {
        label(source);
        selector(selector);
        if (budget == null) throw new IllegalArgumentException("Fluid query budget required");
        try {
            List<Endpoint> endpoints = gather(source, budget);
            long amount = 0;
            for (Endpoint endpoint : endpoints) for (Tank tank : endpoint.tanks) {
                budget.chargeSlot();
                if (tank.amount > 0 && selected(selector, tank.fluid)) amount = Math.addExact(amount, tank.amount);
            }
            return new Observation(endpoints, amount, true);
        } catch (Unavailable missing) {
            if (!allowUnavailable) throw new IllegalArgumentException("Fluid query unavailable for " + source);
            return new Observation(null, 0, false);
        }
    }

    private static boolean selected(ResourceSelector selector, FluidIdentity fluid) {
        return selector.getId()
            .equals("*")
            || selector.getId()
                .equals(fluid.getId());
    }

    public ItemTransferResult transfer(LabelReference source, LabelReference destination, DataValue filter,
        long requested, boolean all) {
        validateArguments(source, destination, filter, requested);
        if (!all && requested == 0) return new ItemTransferResult(0, 0, Status.COMPLETE);
        QueryBudget budget = context.getInvocationQueryBudget();
        authorityDisk = context.getManager()
            .getDisk();
        authorityGeneration = context.getManager()
            .getDiskGeneration();
        long moved = 0;
        boolean matched = false;
        try {
            List<Endpoint> inputs = gather(source, budget), outputs = gather(destination, budget);
            budget.chargeAllocation(1);
            IdentityHashMap<IFluidHandler, Boolean> sources = new IdentityHashMap<>();
            long stock = 0;
            for (Endpoint input : inputs) {
                budget.chargeAllocation(1);
                sources.put(input.handler, Boolean.TRUE);
                for (Tank tank : input.tanks) {
                    budget.chargeSlot();
                    if (tank.amount > 0 && matches(filter, tank.fluid, budget))
                        stock = Math.addExact(stock, tank.amount);
                }
            }
            if (all) requested = stock;
            // The observation caps this action, including a tank refilled during callbacks.
            for (Endpoint input : inputs) for (Tank tank : input.tanks) {
                if (moved == requested) break;
                budget.chargeSlot();
                if (tank.amount <= 0 || !matches(filter, tank.fluid, budget)) continue;
                long allowance = Math.min(tank.amount, requested - moved);
                for (Endpoint output : outputs) {
                    if (allowance <= 0 || moved == requested) break;
                    budget.chargeSlot();
                    if (sources.containsKey(output.handler)) continue;
                    for (ForgeDirection from : ForgeDirection.VALID_DIRECTIONS) {
                        if ((input.sideMask & 1 << from.ordinal()) == 0) continue;
                        for (ForgeDirection to : ForgeDirection.VALID_DIRECTIONS) {
                            if ((output.sideMask & 1 << to.ordinal()) == 0 || allowance <= 0) continue;
                            int amount = (int) Math.min(Math.min(allowance, requested - moved), Integer.MAX_VALUE);
                            if (amount == 0) break;
                            int drained = simulateDrain(input, from, tank.fluid, amount, budget);
                            if (drained == 0) continue;
                            matched = true;
                            int accepted = simulateFill(output, to, tank.fluid, drained, budget);
                            if (accepted == 0) continue;
                            int sent = commit(input, from, output, to, tank.fluid, accepted, budget);
                            moved += sent;
                            allowance -= sent;
                            if (sent > 0) context.setDidSomething(true);
                        }
                    }
                }
            }
        } catch (Unavailable missing) {
            return new ItemTransferResult(requested, moved, moved > 0 ? Status.PARTIAL : Status.UNAVAILABLE);
        }
        return new ItemTransferResult(
            requested,
            moved,
            moved == requested ? Status.COMPLETE
                : moved > 0 ? Status.PARTIAL : matched ? Status.NO_CAPACITY : Status.NO_MATCH);
    }

    private static boolean matches(DataValue filter, FluidIdentity fluid, QueryBudget budget) {
        return filter.getType() == DataType.FLUID_KEY ? filter.asFluidIdentity()
            .matches(fluid, budget) : selected(filter.asSelector(), fluid);
    }

    private List<Endpoint> gather(LabelReference label, QueryBudget budget) {
        label(label);
        World world = context.getWorld();
        if (world == null || world.provider == null || context.getNetwork() == null) throw new Unavailable();
        if (world.isRemote) throw new IllegalArgumentException("Fluid operations require server-side execution");
        Set<BlockPos> positions = context.getLabelPositionHolder()
            .getPositions(label.getName());
        if (positions.isEmpty()) throw new Unavailable();
        budget.chargeAllocation(2);
        List<Endpoint> result = new ArrayList<>();
        IdentityHashMap<IFluidHandler, Boolean> seen = new IdentityHashMap<>();
        int tankCount = 0;
        for (BlockPos position : positions) {
            TileEntity tile = tile(position, budget);
            if (!(tile instanceof IFluidHandler)) throw new Unavailable();
            IFluidHandler handler = (IFluidHandler) tile;
            if (seen.containsKey(handler)) continue;
            List<Tank> canonical = null;
            int sideMask = 0;
            for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
                if ((label.getSideMask() & 1 << side.ordinal()) == 0) continue;
                List<Tank> view = tanks(handler, side, budget);
                if (view.isEmpty()) continue;
                if (canonical == null) canonical = view;
                else if (!sameView(canonical, view, budget)) throw new Unavailable();
                sideMask |= 1 << side.ordinal();
            }
            // Forge supplies no tank IDs. Ambiguous side views need explicit side selection.
            if (canonical == null || tile(position, budget) != tile) throw new Unavailable();
            tankCount = Math.addExact(tankCount, canonical.size());
            if (tankCount > FluidContents.MAX_ROWS) throw new IllegalArgumentException("Fluid tank limit exceeded");
            budget.chargeAllocation(3);
            seen.put(handler, Boolean.TRUE);
            result.add(new Endpoint(position, tile, handler, sideMask, canonical));
        }
        for (Endpoint endpoint : result) requirePresent(endpoint, budget);
        return result;
    }

    private TileEntity tile(BlockPos position, QueryBudget budget) {
        budget.chargePosition();
        budget.chargeProviders(3);
        World world = context.getWorld();
        return context.getNetwork()
            .isConnected(position) && position.isLoaded(world) ? world.getTileEntity(position.x, position.y, position.z)
                : null;
    }

    private void requirePresent(Endpoint endpoint, QueryBudget budget) {
        if (tile(endpoint.position, budget) != endpoint.tile || endpoint.tile.isInvalid()) throw new Unavailable();
    }

    private static List<Tank> tanks(IFluidHandler handler, ForgeDirection side, QueryBudget budget) {
        budget.chargeProvider();
        FluidTankInfo[] info = handler.getTankInfo(side);
        int length = info == null ? 0 : info.length;
        if (length > MAX_TANKS_PER_HANDLER) throw new IllegalArgumentException("Fluid tank view exceeds 256 tanks");
        budget.chargeSlots(length);
        budget.chargeAllocation(1 + length * 2);
        List<Tank> result = new ArrayList<>();
        if (info == null) return result;
        for (FluidTankInfo tank : info) {
            if (tank == null || tank.capacity < 0
                || tank.fluid != null && (tank.fluid.getFluid() == null || tank.fluid.amount < 0))
                throw new IllegalArgumentException("Invalid fluid tank view");
            int amount = tank.fluid == null ? 0 : tank.fluid.amount;
            if (amount > tank.capacity) throw new IllegalArgumentException("Tank contents exceed capacity");
            FluidIdentity key = amount == 0 ? null : FluidIdentity.fromStack(tank.fluid, budget);
            result.add(new Tank(key, amount, tank.capacity));
        }
        return result;
    }

    private static boolean sameView(List<Tank> left, List<Tank> right, QueryBudget budget) {
        if (left.size() != right.size()) return false;
        for (int i = 0; i < left.size(); i++) {
            budget.chargeSlot();
            Tank a = left.get(i), b = right.get(i);
            if (a.amount != b.amount || a.capacity != b.capacity || a.amount > 0 && !a.fluid.matches(b.fluid, budget))
                return false;
        }
        return true;
    }

    private int simulateDrain(Endpoint source, ForgeDirection side, FluidIdentity key, int amount, QueryBudget budget) {
        requirePresent(source, budget);
        FluidStack probe = key.stack(amount, budget);
        budget.chargeProvider();
        if (!source.handler.canDrain(side, probe.getFluid())) return 0;
        requirePresent(source, budget);
        budget.chargeProvider();
        FluidStack drained = source.handler.drain(side, probe, false);
        if (drained == null) return 0;
        if (drained.amount < 0 || drained.amount > amount || !key.matches(drained, budget))
            throw new IllegalArgumentException("Fluid provider returned an invalid drain simulation");
        requirePresent(source, budget);
        return drained.amount;
    }

    private int simulateFill(Endpoint target, ForgeDirection side, FluidIdentity key, int amount, QueryBudget budget) {
        requirePresent(target, budget);
        FluidStack probe = key.stack(amount, budget);
        budget.chargeProvider();
        if (!target.handler.canFill(side, probe.getFluid())) return 0;
        requirePresent(target, budget);
        budget.chargeProvider();
        int accepted = target.handler.fill(side, probe, false);
        if (accepted < 0 || accepted > amount || probe.amount != amount || !key.matches(probe, budget))
            throw new IllegalArgumentException("Fluid provider returned an invalid fill simulation");
        requirePresent(target, budget);
        return accepted;
    }

    private int commit(Endpoint source, ForgeDirection from, Endpoint target, ForgeDirection to, FluidIdentity key,
        int amount, QueryBudget budget) {
        // Hold all post-extraction work up front. Provider callbacks cannot consume recovery credit.
        int work = Math.addExact(128, key.comparisonWork() * 12 + key.copyWork() * 14);
        int allocation = Math.addExact(64, key.copyWork() * 14);
        try (QueryBudget.Reservation reserve = budget.reserve(24, 96, work, allocation)) {
            QueryBudget commit = reserve.budget();
            int drained = simulateDrain(source, from, key, amount, commit);
            if (drained == 0) return 0;
            int accepted = simulateFill(target, to, key, drained, commit);
            if (accepted == 0) return 0;
            FluidStack request = key.stack(accepted, commit);
            FluidStack evidence = key.stack(accepted, commit);
            requirePresent(target, commit);
            requirePresent(source, commit); // Last provider-view check before extraction.
            if (context.getManager()
                .getDisk() != authorityDisk
                || context.getManager()
                    .getDiskGeneration() != authorityGeneration
                || context.getManager()
                    .hasPendingRecovery())
                throw new IllegalArgumentException("Fluid transfer disk changed during simulation");
            Transaction transaction = new Transaction(source, from, target, to, evidence);
            try {
                // Journaling marks the tile dirty; neighbor callbacks may have invalidated the endpoints.
                if (!transaction.current())
                    throw new IllegalArgumentException("Fluid transfer disk changed before extraction");
                if (!present(target, commit) || !present(source, commit)) {
                    transaction.finish();
                    throw new Unavailable();
                }
                commit.chargeProvider();
                FluidStack owned = source.handler.drain(from, request, true);
                if (owned == null || owned.amount == 0) {
                    transaction.finish();
                    return 0;
                }
                if (owned.amount < 0 || owned.amount > accepted || !key.matches(owned, commit))
                    throw new IllegalArgumentException("Fluid provider returned an invalid extraction");
                // Never pass the recovery payload itself to a provider.
                int ownedAmount = owned.amount;
                transaction.record.setInteger("Amount", ownedAmount);
                transaction.stage("destination insertion");
                int inserted = 0;
                if (transaction.current() && present(target, commit)) {
                    int fresh = simulateFill(target, to, key, ownedAmount, commit);
                    if (fresh > 0 && transaction.current() && present(target, commit)) {
                        FluidStack insert = key.stack(fresh, commit);
                        commit.chargeProvider();
                        inserted = target.handler.fill(to, insert, true);
                        if (inserted < 0 || inserted > fresh)
                            throw new IllegalArgumentException("Fluid provider returned an invalid insertion");
                    }
                }
                if (inserted > 0) context.setDidSomething(true);
                int remaining = ownedAmount - inserted;
                if (remaining > 0) {
                    transaction.record.setInteger("Amount", remaining);
                    transaction.stage("source rollback");
                    if (transaction.current() && present(source, commit)) {
                        int canReturn = simulateFill(source, from, key, remaining, commit);
                        if (canReturn > 0 && transaction.current() && present(source, commit)) {
                            FluidStack rollback = key.stack(canReturn, commit);
                            commit.chargeProvider();
                            int returned = source.handler.fill(from, rollback, true);
                            if (returned < 0 || returned > canReturn)
                                throw new IllegalArgumentException("Fluid provider returned an invalid rollback");
                            remaining -= returned;
                        }
                    }
                    if (remaining > 0) {
                        transaction.record.setInteger("Amount", remaining);
                        transaction.record.setString("state", "pending");
                        transaction.record.setBoolean("recoveryBlocked", false);
                        if (!transaction.current()) transaction.uncertain();
                        transaction.fail();
                        throw new IllegalArgumentException(
                            "Fluid transfer paused: rollback remainder retained on disk");
                    }
                }
                transaction.finish();
                return inserted;
            } catch (RuntimeException failure) {
                transaction.fail();
                throw failure;
            }
        }
    }

    private boolean present(Endpoint endpoint, QueryBudget budget) {
        return tile(endpoint.position, budget) == endpoint.tile && !endpoint.tile.isInvalid();
    }

    /** Write-ahead evidence also fences reentrant runs and disk replacement during provider callbacks. */
    private final class Transaction {

        final TileEntityManager manager = context.getManager();
        final ItemStack disk = manager.getDisk();
        final long generation = manager.getDiskGeneration();
        final NBTTagCompound record;

        Transaction(Endpoint source, ForgeDirection from, Endpoint target, ForgeDirection to, FluidStack evidence) {
            record = manager.journalFluidTransfer(evidence, source.position, from);
            record.setInteger("targetX", target.position.x);
            record.setInteger("targetY", target.position.y);
            record.setInteger("targetZ", target.position.z);
            record.setInteger("targetSide", to.ordinal());
            record.setInteger("targetDimension", context.getWorld().provider.dimensionId);
        }

        boolean current() {
            return manager.getDisk() == disk && manager.getDiskGeneration() == generation
                && disk.hasTagCompound()
                && disk.getTagCompound()
                    .getTag(RECOVERY) == record;
        }

        void stage(String stage) {
            record.setString("stage", stage);
        }

        void uncertain() {
            record.setString("state", "uncertain");
            record.setBoolean("recoveryBlocked", true);
        }

        void fail() {
            manager.setRunning(false);
            manager.markDirty();
        }

        void finish() {
            if (!current()) {
                uncertain();
                throw new IllegalArgumentException(
                    "Fluid transfer disk changed during callback; inspect recovery evidence");
            }
            disk.getTagCompound()
                .removeTag(RECOVERY);
            manager.markDirty();
        }
    }

    private static final class Endpoint {

        final BlockPos position;
        final TileEntity tile;
        final IFluidHandler handler;
        final int sideMask;
        final List<Tank> tanks;

        Endpoint(BlockPos position, TileEntity tile, IFluidHandler handler, int sideMask, List<Tank> tanks) {
            this.position = position;
            this.tile = tile;
            this.handler = handler;
            this.sideMask = sideMask;
            this.tanks = tanks;
        }
    }

    private static final class Tank {

        final FluidIdentity fluid;
        final int amount, capacity;

        Tank(FluidIdentity fluid, int amount, int capacity) {
            this.fluid = fluid;
            this.amount = amount;
            this.capacity = capacity;
        }
    }

    private static final class Observation {

        final List<Endpoint> endpoints;
        final long amount;
        final boolean complete;

        Observation(List<Endpoint> endpoints, long amount, boolean complete) {
            this.endpoints = endpoints;
            this.amount = amount;
            this.complete = complete;
        }
    }

    private static final class Unavailable extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }
}
