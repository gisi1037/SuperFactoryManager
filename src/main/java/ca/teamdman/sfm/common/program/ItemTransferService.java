package ca.teamdman.sfm.common.program;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import ca.teamdman.sfm.common.inventory.InventoryUtils;
import ca.teamdman.sfm.common.program.ProgramContext.ItemMoveEndpoint;
import ca.teamdman.sfm.common.program.ProgramContext.ItemMoveGuard;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.ItemTransferResult;
import ca.teamdman.sfml.data.ItemTransferResult.Status;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;

/** Bounded direct-inventory actions; advisory origins are revalidated after permission callbacks. */
public final class ItemTransferService {

    private static final int MAX_ENDPOINTS = 1024;
    private static final int COMMIT_VIEW_CHECKS = 11, COMMIT_COMPARISONS = 8, COMMIT_COPIES = 5;
    private static final int COMMIT_CALLBACKS = COMMIT_VIEW_CHECKS * 36 + 40, COMMIT_POSITIONS = COMMIT_VIEW_CHECKS + 2;
    private static final ForgeDirection[] HORIZONTAL = { ForgeDirection.WEST, ForgeDirection.EAST, ForgeDirection.NORTH,
        ForgeDirection.SOUTH };
    private final ProgramContext context;
    private World world;
    private QueryBudget budget;
    private DataValue filter;
    private boolean unavailable;

    public ItemTransferService(ProgramContext context) {
        this.context = context;
    }

    static void validateArguments(LabelReference source, LabelReference destination, DataValue filter, long amount) {
        if (amount < 0) throw new IllegalArgumentException("Item transfer amount cannot be negative");
        if (source == null || destination == null
            || filter == null
            || filter.getType() != DataType.ITEM_KEY && filter.getType() != DataType.RESOURCE_SELECTOR)
            throw new IllegalArgumentException(
                "Item transfer requires source/destination labels and an item key or selector");
    }

    public ItemTransferResult transfer(LabelReference source, LabelReference destination, DataValue itemOrSelector,
        long requested) {
        return transfer(source, destination, itemOrSelector, requested, false);
    }

    public ItemTransferResult transfer(LabelReference source, LabelReference destination, DataValue itemOrSelector) {
        return transfer(source, destination, itemOrSelector, 0, true);
    }

    private ItemTransferResult transfer(LabelReference source, LabelReference destination, DataValue itemOrSelector,
        long requested, boolean all) {
        validateArguments(source, destination, itemOrSelector, requested);
        unavailable = false;
        if (!all && requested == 0) return new ItemTransferResult(0, 0, Status.COMPLETE);
        world = context.getWorld();
        if (world == null || world.provider == null || context.getNetwork() == null)
            return new ItemTransferResult(requested, 0, Status.UNAVAILABLE);
        if (world.isRemote) throw new IllegalArgumentException("Item transfers require server-side execution");
        budget = context.getInvocationQueryBudget();
        filter = itemOrSelector;
        long moved = 0;
        boolean matched = false, requestedKnown = !all;
        try {
            List<Endpoint> sources = gather(source), destinations = gather(destination);
            budget.chargeAllocation(1);
            IdentityHashMap<IInventory, Set<Integer>> selectedSources = new IdentityHashMap<>();
            for (Endpoint input : sources) {
                budget.chargeSlot();
                Set<Integer> slots = selectedSources.get(input.inv);
                if (slots == null) {
                    budget.chargeAllocation(2);
                    slots = new HashSet<>();
                    selectedSources.put(input.inv, slots);
                }
                budget.chargeAllocation(1);
                slots.add(input.slot);
                if (all) {
                    ItemStack stack = read(input, budget);
                    if (stack == null || stack.stackSize == 0) continue;
                    int count = stack.stackSize;
                    boolean selected = filter.getType() == DataType.ITEM_KEY
                        ? matchesStack(filter.asItemIdentity(), stack, budget)
                        : filter.asSelector()
                            .matches(stack, budget);
                    if (stack.stackSize != count || stack.getItem() == null)
                        throw new IllegalArgumentException("Transfer source changed during amount observation");
                    if (selected) requested = Math.addExact(requested, count);
                }
            }
            // A finite stock snapshot is an aggregate cap, not a reservation or an unbounded drain loop.
            requestedKnown = true;
            for (Endpoint input : sources) {
                if (moved == requested) break;
                ItemStack first = read(input, budget);
                if (first == null || first.stackSize <= 0) continue;
                ItemIdentity expected = matchingIdentity(first, budget);
                if (expected == null) continue;
                ForgeDirection sourceSide = extractSide(input, first, expected, budget);
                if (sourceSide == null) continue;
                matched = true;
                long sourceAllowance = first.stackSize;
                for (Endpoint output : destinations) {
                    if (moved == requested || sourceAllowance <= 0) break;
                    budget.chargeSlot();
                    Set<Integer> selected = selectedSources.get(output.inv);
                    // Include empty selected source slots: output may never feed a later input in this action.
                    if (selected != null && selected.contains(output.slot)) continue;
                    if (fullDestination(output)) continue;
                    for (ForgeDirection targetSide : ForgeDirection.VALID_DIRECTIONS) {
                        if ((output.sideMask & 1 << targetSide.ordinal()) == 0) continue;
                        if (!valid(input, sourceSide, budget) || !valid(output, targetSide, budget)) break;
                        ItemStack current = read(input, budget);
                        if (current == null || current.stackSize <= 0 || !matchesExpected(current, expected, budget))
                            break;
                        int inspectedAmount = current.stackSize;
                        ItemStack limitProbe = inspectionCopy(current, expected, budget);
                        budget.chargeProvider();
                        int maximum = limitProbe.getMaxStackSize();
                        if (maximum <= 0 || limitProbe.stackSize != inspectedAmount
                            || !matchesStack(expected, limitProbe, budget)
                            || !matchesExpected(current, expected, budget)) break;
                        int amount = (int) Math
                            .min(Math.min(requested - moved, sourceAllowance), Math.min(current.stackSize, maximum));
                        if (amount <= 0) break;
                        budget.chargeSlots(expected.getCopyWork());
                        budget.chargeAllocation(expected.getCopyWork() * 2 + 4);
                        ItemStack probe = current.copy();
                        probe.stackSize = amount;
                        Guard guard = new Guard(input, sourceSide, output, targetSide, expected);
                        int inserted;
                        try {
                            inserted = context
                                .moveItemPair(input.move(sourceSide), output.move(targetSide), probe, guard);
                        } catch (ProgramContext.ItemMoveFailure failure) {
                            moved = Math.addExact(moved, failure.moved);
                            if (moved > 0) context.setDidSomething(true);
                            throw failure;
                        } finally {
                            // Keep commit credit held until insertion and any mandatory recovery finish.
                            guard.close();
                        }
                        if (inserted > 0) {
                            moved += inserted;
                            sourceAllowance -= inserted;
                            context.setDidSomething(true);
                            break;
                        }
                        if (!(output.inv instanceof ISidedInventory)) break;
                    }
                }
            }
        } catch (Unavailable failure) {
            unavailable = true;
        } catch (RuntimeException failure) {
            throw new IllegalArgumentException(
                "Item transfer failed after moving " + moved + " of " + requested + " items: " + failure.getMessage(),
                failure);
        }
        return new ItemTransferResult(
            requested,
            moved,
            moved == requested && requestedKnown ? Status.COMPLETE
                : moved > 0 ? Status.PARTIAL
                    : unavailable ? Status.UNAVAILABLE : matched ? Status.NO_CAPACITY : Status.NO_MATCH);
    }

    private ItemIdentity matchingIdentity(ItemStack stack, QueryBudget allowance) {
        if (filter.getType() == DataType.ITEM_KEY) {
            ItemIdentity key = filter.asItemIdentity();
            return matchesStack(key, stack, allowance) ? key : null;
        }
        return filter.asSelector()
            .matches(stack, allowance) ? identity(stack, allowance) : null;
    }

    private boolean matchesExpected(ItemStack stack, ItemIdentity expected, QueryBudget allowance) {
        return (filter.getType() == DataType.ITEM_KEY || filter.asSelector()
            .matches(stack, allowance)) && matchesStack(expected, stack, allowance);
    }

    private static boolean matchesStack(ItemIdentity expected, ItemStack stack, QueryBudget allowance) {
        allowance.chargeProvider();
        return expected.matchesStack(stack, allowance);
    }

    private static ItemIdentity identity(ItemStack stack, QueryBudget allowance) {
        allowance.chargeProvider();
        return ItemIdentity.fromStack(stack, allowance);
    }

    private static int size(IInventory inventory, QueryBudget allowance) {
        allowance.chargeProvider();
        int size = inventory.getSizeInventory();
        if (size < 0) throw new IllegalArgumentException("Transfer provider returned a negative inventory size");
        return size;
    }

    private static ItemStack read(Endpoint endpoint, QueryBudget allowance) {
        allowance.chargeSlot();
        allowance.chargeProvider();
        ItemStack stack = endpoint.inv.getStackInSlot(endpoint.slot);
        if (stack != null && (stack.stackSize < 0 || stack.getItem() == null))
            throw new IllegalArgumentException("Transfer provider returned an invalid stack");
        return stack;
    }

    private TileEntity tile(BlockPos position, QueryBudget allowance) {
        allowance.chargeProviders(2);
        return position.isLoaded(world) ? world.getTileEntity(position.x, position.y, position.z) : null;
    }

    private View resolve(LabelReference label, BlockPos position, QueryBudget allowance) {
        allowance.chargePosition();
        if (!context.getLabelPositionHolder()
            .getPositions(label.getName())
            .contains(position)) return null;
        allowance.chargeProvider();
        if (!context.getNetwork()
            .isConnected(position)) return null;
        TileEntity found = tile(position, allowance);
        if (!(found instanceof IInventory)) return null;
        IInventory first = (IInventory) found, second = null;
        BlockPos firstPosition = position, secondPosition = null;
        if (found instanceof TileEntityChest) {
            TileEntityChest chest = (TileEntityChest) found, partner = null;
            ForgeDirection partnerSide = null;
            allowance.chargeProvider();
            int type = chest.func_145980_j();
            for (ForgeDirection direction : HORIZONTAL) {
                allowance.chargeAllocation(1);
                BlockPos neighbor = position.offset(direction);
                allowance.chargeProvider();
                if (!neighbor.isLoaded(world)) return null;
                TileEntity adjacent = tile(neighbor, allowance);
                if (!(adjacent instanceof TileEntityChest)) continue;
                allowance.chargeProvider();
                if (((TileEntityChest) adjacent).func_145980_j() != type) continue;
                if (partner != null) throw new IllegalArgumentException("Transfer found an ambiguous chest group");
                partner = (TileEntityChest) adjacent;
                partnerSide = direction;
                secondPosition = neighbor;
            }
            if (partner != null) {
                for (ForgeDirection direction : HORIZONTAL) {
                    allowance.chargeAllocation(1);
                    BlockPos neighbor = secondPosition.offset(direction);
                    if (neighbor.equals(position)) continue;
                    allowance.chargeProvider();
                    if (!neighbor.isLoaded(world)) return null;
                    TileEntity adjacent = tile(neighbor, allowance);
                    if (adjacent instanceof TileEntityChest) {
                        allowance.chargeProvider();
                        if (((TileEntityChest) adjacent).func_145980_j() == type)
                            throw new IllegalArgumentException("Transfer found an ambiguous chest group");
                    }
                }
                second = partner;
                if (partnerSide == ForgeDirection.WEST || partnerSide == ForgeDirection.NORTH) {
                    first = partner;
                    second = chest;
                    firstPosition = secondPosition;
                    secondPosition = position;
                }
            }
        }
        allowance.chargeAllocation(1);
        return new View(first, second, firstPosition, secondPosition);
    }

    private List<Endpoint> gather(LabelReference label) {
        Set<BlockPos> positions = context.getLabelPositionHolder()
            .getPositions(label.getName());
        if (positions.isEmpty()) throw new Unavailable();
        budget.chargeAllocation(2);
        List<Endpoint> endpoints = new ArrayList<>();
        IdentityHashMap<IInventory, Set<Integer>> seen = new IdentityHashMap<>();
        for (BlockPos position : positions) {
            View view = resolve(label, position, budget);
            if (view == null) throw new Unavailable();
            int firstSize = size(view.first, budget), secondSize = view.second == null ? 0 : size(view.second, budget);
            long combined = (long) firstSize + secondSize;
            if (combined > Integer.MAX_VALUE) throw new IllegalArgumentException("Transfer inventory size overflow");
            int end = Math.min((int) combined - 1, label.getLastSlot());
            budget.chargeAllocation(1);
            TreeMap<Integer, Integer> slots = new TreeMap<>();
            if (view.second == null && view.first instanceof ISidedInventory) {
                for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
                    if ((label.getSideMask() & 1 << side.ordinal()) == 0) continue;
                    budget.chargeProvider();
                    int[] exposed = ((ISidedInventory) view.first).getAccessibleSlotsFromSide(side.ordinal());
                    if (exposed == null) continue;
                    budget.chargeSlots(exposed.length);
                    for (int slot : exposed) if (slot >= label.getFirstSlot() && slot <= end) {
                        budget.chargeAllocation(2);
                        Integer mask = slots.get(slot);
                        slots.put(slot, (mask == null ? 0 : mask) | 1 << side.ordinal());
                    }
                }
            } else {
                int count = Math.max(0, end - label.getFirstSlot() + 1);
                budget.chargeSlots(count);
                budget.chargeAllocation(count * 2);
                for (int slot = label.getFirstSlot(); slot <= end; slot++) slots.put(slot, label.getSideMask());
            }
            if (slots.isEmpty()) throw new Unavailable();
            for (Map.Entry<Integer, Integer> slot : slots.entrySet()) {
                int logical = slot.getKey();
                IInventory inventory = logical < firstSize ? view.first : view.second;
                BlockPos physical = logical < firstSize ? view.firstPosition : view.secondPosition;
                int local = logical < firstSize ? logical : logical - firstSize;
                Set<Integer> known = seen.get(inventory);
                if (known == null) {
                    budget.chargeAllocation(2);
                    known = new HashSet<>();
                    seen.put(inventory, known);
                }
                budget.chargeSlot();
                if (known.contains(local)) continue;
                if (endpoints.size() >= MAX_ENDPOINTS)
                    throw new IllegalArgumentException("Transfer endpoint limit exceeded");
                budget.chargeAllocation(4);
                known.add(local);
                endpoints
                    .add(new Endpoint(label, position, inventory, physical, local, logical, slot.getValue(), view));
            }
        }
        return endpoints;
    }

    private boolean valid(Endpoint endpoint, ForgeDirection side, QueryBudget allowance) {
        View view = resolve(endpoint.label, endpoint.labelPosition, allowance);
        if (view == null) return missing();
        int firstSize = size(view.first, allowance);
        IInventory inventory = endpoint.logicalSlot < firstSize ? view.first : view.second;
        BlockPos position = endpoint.logicalSlot < firstSize ? view.firstPosition : view.secondPosition;
        int slot = endpoint.logicalSlot < firstSize ? endpoint.logicalSlot : endpoint.logicalSlot - firstSize;
        if (inventory != endpoint.inv || position == null
            || !position.equals(endpoint.position)
            || slot != endpoint.slot
            || slot < 0
            || slot >= size(inventory, allowance)) return missing();
        if ((endpoint.label.getSideMask() & 1 << side.ordinal()) == 0) return false;
        if (inventory instanceof ISidedInventory) {
            allowance.chargeProvider();
            int[] exposed = ((ISidedInventory) inventory).getAccessibleSlotsFromSide(side.ordinal());
            if (exposed == null) return false;
            allowance.chargeSlots(exposed.length);
            endpoint.sideWork = Math.max(endpoint.sideWork, exposed.length);
            for (int exposedSlot : exposed) if (exposedSlot == slot) return true;
            return false;
        }
        return true;
    }

    private boolean missing() {
        unavailable = true;
        return false;
    }

    /** Fresh negative observation only: a full nonsided slot needs no source key or permission probe. */
    private boolean fullDestination(Endpoint output) {
        if (output.inv instanceof ISidedInventory) return false;
        ForgeDirection side = ForgeDirection.VALID_DIRECTIONS[Integer.numberOfTrailingZeros(output.sideMask)];
        if (!valid(output, side, budget)) return true;
        budget.chargeProvider();
        int limit = output.inv.getInventoryStackLimit();
        ItemStack current = limit <= 0 ? null : read(output, budget);
        // Limit/stack callbacks may have replaced or unloaded the provider. Never reuse this observation
        // as transfer authority; all positive candidates still enter the full simulation/commit guards.
        if (!present(output, budget)) return true;
        return limit <= 0 || current != null && current.stackSize >= limit;
    }

    private ForgeDirection extractSide(Endpoint endpoint, ItemStack stack, ItemIdentity key, QueryBudget allowance) {
        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            if ((endpoint.sideMask & 1 << side.ordinal()) == 0 || !valid(endpoint, side, allowance)) continue;
            int originalAmount = stack.stackSize;
            if (!matchesExpected(stack, key, allowance)) return null;
            ItemStack inspection = inspectionCopy(stack, key, allowance);
            allowance.chargeProvider();
            if (InventoryUtils.canExtract(endpoint.inv, endpoint.slot, inspection, side)
                && inspection.stackSize == originalAmount
                && matchesStack(key, inspection, allowance)) return side;
        }
        return null;
    }

    private static ItemStack inspectionCopy(ItemStack stack, ItemIdentity key, QueryBudget allowance) {
        chargeCopy(key, allowance);
        return stack.copy();
    }

    private static void chargeCopy(ItemIdentity key, QueryBudget allowance) {
        allowance.chargeSlots(key.getCopyWork());
        allowance.chargeAllocation(key.getCopyWork() * 2 + 4);
    }

    /** No inventory data/permission callbacks follow this final physical-view check before mutation. */
    private boolean present(Endpoint endpoint, QueryBudget allowance) {
        View view = resolve(endpoint.label, endpoint.labelPosition, allowance);
        if (view == null || view.first != endpoint.view.first
            || view.second != endpoint.view.second
            || !view.firstPosition.equals(endpoint.view.firstPosition)
            || view.secondPosition != null && !view.secondPosition.equals(endpoint.view.secondPosition))
            return missing();
        if (tile(view.firstPosition, allowance) != view.first) return missing();
        return view.second == null || tile(view.secondPosition, allowance) == view.second || missing();
    }

    private final class Guard implements ItemMoveGuard {

        final Endpoint source, target;
        final ForgeDirection sourceSide, targetSide;
        final ItemIdentity expected;
        QueryBudget commit;
        QueryBudget.Reservation reservation;
        boolean attempted;
        int beforeCount, attemptedCount;

        Guard(Endpoint source, ForgeDirection sourceSide, Endpoint target, ForgeDirection targetSide,
            ItemIdentity expected) {
            this.source = source;
            this.sourceSide = sourceSide;
            this.target = target;
            this.targetSide = targetSide;
            this.expected = expected;
        }

        public int simulate(ItemStack probe) {
            return insertion(probe, true, budget);
        }

        public boolean beforeExtract(ItemStack probe, int accepted) {
            if (!valid(source, sourceSide, budget) || !valid(target, targetSide, budget)) return false;
            ItemStack current = read(source, budget);
            if (current == null || current.stackSize <= 0 || !matchesExpected(current, expected, budget)) return false;
            // Stable-view upper bound, including rollback: extract/accept/insert use 4/1/6 view
            // resolutions, with at most 8 neighbor positions plus one View allocation and 36
            // callbacks each. There are 3/2/2/1 live-key comparisons in extract/accept/insert/
            // restore, and 1/0/2/2 stack copies (the last two include the shared helper's remainder).
            // Seven sided-slot walks use the latest observed array lengths: source four, target
            // three. Changed callback shapes may exhaust credit, but cannot gate disk preservation.
            int copy = expected.getCopyWork();
            int work = Math.addExact(5, Math.multiplyExact(COMMIT_COMPARISONS, expected.getComparisonWork()));
            work = Math.addExact(work, Math.multiplyExact(COMMIT_COPIES, copy));
            work = Math.addExact(
                work,
                Math.addExact(Math.multiplyExact(source.sideWork, 4), Math.multiplyExact(target.sideWork, 3)));
            int allocations = Math.addExact(
                COMMIT_VIEW_CHECKS * 9 + COMMIT_COMPARISONS,
                Math.multiplyExact(COMMIT_COPIES, Math.addExact(Math.multiplyExact(copy, 2), 4)));
            reservation = budget.reserve(COMMIT_POSITIONS, COMMIT_CALLBACKS, work, allocations);
            commit = reservation.budget();
            return true;
        }

        void close() {
            if (reservation != null) reservation.close();
        }

        public ItemStack extract(int accepted) {
            ItemStack current = read(source, commit);
            if (current == null || current.stackSize <= 0) return null;
            if (!matchesStack(expected, current, commit)) return null;
            int inspectedAmount = current.stackSize;
            ItemStack inspection = inspectionCopy(current, expected, commit);
            commit.chargeProvider();
            if (!InventoryUtils.canExtract(source.inv, source.slot, inspection, sourceSide)) return null;
            if (inspection.stackSize != inspectedAmount || !matchesStack(expected, inspection, commit)) return null;
            if (!valid(source, sourceSide, commit) || !valid(target, targetSide, commit)) return null;
            current = read(source, commit);
            if (current == null || !matchesStack(expected, current, commit)) return null;
            int amount = Math.min(accepted, current.stackSize);
            if (amount <= 0) return null;
            beforeCount = current.stackSize;
            if (!present(source, commit) || !present(target, commit)) return null;
            commit.chargeProviders(2);
            attemptedCount = amount;
            attempted = true;
            ItemStack extracted = source.inv.decrStackSize(source.slot, amount);
            source.inv.markDirty();
            return extracted;
        }

        public boolean extractionAttempted() {
            return attempted;
        }

        public int attemptedAmount() {
            return attemptedCount;
        }

        public boolean acceptExtracted(ItemStack extracted) {
            if (!matchesStack(expected, extracted, commit) || !valid(source, sourceSide, commit)) return false;
            ItemStack remaining = read(source, commit);
            int expectedRemaining = beforeCount - extracted.stackSize;
            return remaining == null ? expectedRemaining == 0
                : remaining.stackSize == expectedRemaining && matchesStack(expected, remaining, commit);
        }

        public int insert(ItemStack extracted) {
            int inserted = insertion(extracted, false, commit);
            // The shared pair helper copies a remainder immediately after this callback returns.
            if (inserted < extracted.stackSize) chargeCopy(expected, commit);
            return inserted;
        }

        private int insertion(ItemStack offered, boolean simulate, QueryBudget allowance) {
            if (!valid(source, sourceSide, allowance) || !valid(target, targetSide, allowance)) return 0;
            int offeredAmount = offered.stackSize;
            ItemStack inspection = inspectionCopy(offered, expected, allowance);
            allowance.chargeProvider();
            if (!target.inv.isItemValidForSlot(target.slot, inspection)) return 0;
            if (target.inv instanceof ISidedInventory) {
                allowance.chargeProvider();
                if (!((ISidedInventory) target.inv).canInsertItem(target.slot, inspection, targetSide.ordinal()))
                    return 0;
            }
            allowance.chargeProviders(2);
            int maximum = Math.min(target.inv.getInventoryStackLimit(), inspection.getMaxStackSize());
            // Permission/limit callbacks can mutate stacks or replace/unload either tile. Resolve both
            // physical views after those callbacks, then read current destination stock before writing.
            if (maximum <= 0 || !valid(source, sourceSide, allowance) || !valid(target, targetSide, allowance))
                return 0;
            if (inspection.stackSize != offeredAmount || !matchesStack(expected, inspection, allowance)) return 0;
            ItemStack existing = read(target, allowance);
            if (existing != null && !matchesStack(expected, existing, allowance)) return 0;
            int amount = (int) Math
                .min(offered.stackSize, Math.max(0L, (long) maximum - (existing == null ? 0 : existing.stackSize)));
            if (amount <= 0 || simulate) return amount;
            allowance.chargeSlots(expected.getCopyWork());
            allowance.chargeAllocation(expected.getCopyWork() * 2 + 4);
            ItemStack replacement = offered.copy();
            replacement.stackSize = amount + (existing == null ? 0 : existing.stackSize);
            if (!present(source, allowance) || !present(target, allowance)) return 0;
            allowance.chargeProviders(2);
            target.inv.setInventorySlotContents(target.slot, replacement);
            target.inv.markDirty();
            return amount;
        }

        public boolean restore(ItemStack remainder) {
            // Recovery consumes previously reserved work. If validation cannot complete, return the
            // known remainder to the shared disk-preservation path rather than gate recovery on budget.
            ItemStack replacement;
            try {
                // Fixed recovery header covers loaded identity checks, limit/read callbacks, final
                // set/markDirty and the bounded known-payload copy; live matching charges itself.
                commit.chargeProviders(16);
                commit.chargePositions(2);
                commit.chargeSlot();
                chargeCopy(expected, commit);
                if (!recoveryPresent()) return false;
                int maximum = source.inv.getInventoryStackLimit();
                ItemStack existing = source.inv.getStackInSlot(source.slot);
                if (existing != null && (existing.stackSize < 0 || !matchesStack(expected, existing, commit)))
                    return false;
                long total = (long) remainder.stackSize + (existing == null ? 0 : existing.stackSize);
                if (total > maximum || total > Integer.MAX_VALUE) return false;
                replacement = remainder.copy();
                replacement.stackSize = (int) total;
                if (!recoveryPresent()) return false;
            } catch (RuntimeException failure) {
                return false; // Nothing was written: preserve this known remainder on disk.
            }
            source.inv.setInventorySlotContents(source.slot, replacement);
            source.inv.markDirty();
            return true;
        }

        private boolean recoveryPresent() {
            return source.position.isLoaded(world)
                && world.getTileEntity(source.position.x, source.position.y, source.position.z) == source.inv;
        }
    }

    private static final class View {

        final IInventory first, second;
        final BlockPos firstPosition, secondPosition;

        View(IInventory first, IInventory second, BlockPos firstPosition, BlockPos secondPosition) {
            this.first = first;
            this.second = second;
            this.firstPosition = firstPosition;
            this.secondPosition = secondPosition;
        }
    }

    private static final class Endpoint {

        final LabelReference label;
        final BlockPos labelPosition, position;
        final IInventory inv;
        final View view;
        final int slot, logicalSlot, sideMask;
        int sideWork;

        Endpoint(LabelReference label, BlockPos labelPosition, IInventory inv, BlockPos position, int slot,
            int logicalSlot, int sideMask, View view) {
            this.label = label;
            this.labelPosition = labelPosition;
            this.inv = inv;
            this.position = position;
            this.slot = slot;
            this.logicalSlot = logicalSlot;
            this.sideMask = sideMask;
            this.view = view;
        }

        ItemMoveEndpoint move(ForgeDirection side) {
            return new ItemMoveEndpoint(inv, position, slot, side, false);
        }
    }

    private static final class Unavailable extends RuntimeException {
    }
}
