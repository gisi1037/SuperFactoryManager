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
import ca.teamdman.sfm.common.recovery.ItemRecovery.BulkJournal;
import ca.teamdman.sfm.common.storage.BackendItemInventory;
import ca.teamdman.sfm.common.storage.StorageBackend;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.tileentity.TileEntityModAdapter;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfm.compat.storagedrawers.DrawerItemSource;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.ItemTransferResult;
import ca.teamdman.sfml.data.ItemTransferResult.Status;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;

/** Bounded direct-inventory actions; advisory origins are revalidated after permission callbacks. */
public final class ItemTransferService {

    private static final int MAX_ENDPOINTS = 1024;
    private static final int MAX_BULK_SLOTS = 16, MAX_NATIVE_AMOUNT = 65536;
    private static final int COMMIT_VIEW_CHECKS = 11, COMMIT_COMPARISONS = 8, COMMIT_COPIES = 5;
    private static final int COMMIT_CALLBACKS = COMMIT_VIEW_CHECKS * 36 + 40, COMMIT_POSITIONS = COMMIT_VIEW_CHECKS + 2;
    private static final ForgeDirection[] HORIZONTAL = { ForgeDirection.WEST, ForgeDirection.EAST, ForgeDirection.NORTH,
        ForgeDirection.SOUTH };
    private final ProgramContext context;
    private World world;
    private QueryBudget budget;
    private DataValue filter;
    private ResourceSelector excluded;
    private boolean unavailable, forceExtraction;
    private final Map<TileEntityModAdapter, BackendItemInventory> backendViews = new IdentityHashMap<>();

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
        if (filter.getType() == DataType.RESOURCE_SELECTOR && filter.asSelector()
            .getKind() == ca.teamdman.sfml.data.ResourceSelector.Kind.FLUID)
            throw new IllegalArgumentException("A fluid selector requires a fluid transfer");
    }

    public ItemTransferResult transfer(LabelReference source, LabelReference destination, DataValue itemOrSelector,
        long requested) {
        return transfer(source, destination, itemOrSelector, null, requested, false);
    }

    public ItemTransferResult transfer(LabelReference source, LabelReference destination, DataValue itemOrSelector) {
        return transfer(source, destination, itemOrSelector, null, 0, true);
    }

    static void validateExclusion(ResourceSelector excluded) {
        if (excluded != null && excluded.getKind() != ResourceSelector.Kind.ITEM)
            throw new IllegalArgumentException("TRANSFER EXCEPT requires ITEM(\"namespace:name\")");
    }

    ItemTransferResult transferExact(LabelReference source, LabelReference destination, DataValue selection,
        long requested) {
        world = context.getWorld();
        if (world == null || world.provider == null || context.getNetwork() == null)
            return new ItemTransferResult(requested, 0, Status.UNAVAILABLE);
        ExactTransferPlan<ExactItem> plan = new ExactTransferPlan<>(context, requested);
        budget = context.getInvocationQueryBudget();
        filter = selection;
        try {
            return plan.execute(exactSlots(source, true), exactSlots(destination, false));
        } catch (Unavailable missing) {
            return new ItemTransferResult(requested, 0, Status.UNAVAILABLE);
        }
    }

    private List<ExactTransferPlan.Cell<ExactItem>> exactSlots(LabelReference label, boolean source) {
        // Reject arbitrary inventory callbacks before using the ordinary chest/slot resolver.
        for (BlockPos position : context.getLabelPositionHolder()
            .getPositions(label.getName())) {
            TileEntity found = tile(position, budget);
            if (found == null || found.getClass() != TileEntityChest.class) throw new Unavailable();
        }
        return chestSlots(gather(label, source), source, false);
    }

    private List<ExactTransferPlan.Cell<ExactItem>> chestSlots(List<Endpoint> endpoints, boolean source,
        boolean ordered) {
        budget.chargeAllocation(1);
        List<ExactTransferPlan.Cell<ExactItem>> slots = new ArrayList<>();
        for (Endpoint endpoint : endpoints) {
            if (endpoint.inv.getClass() != TileEntityChest.class) throw new Unavailable();
            TileEntityChest chest = (TileEntityChest) endpoint.inv;
            ItemStack[] storage = chestStorage(chest);
            if (storage == null || endpoint.slot >= storage.length) throw new Unavailable();
            ItemStack stack = read(endpoint, budget);
            if (stack != storage[endpoint.slot]) throw new Unavailable();
            int count = stack == null ? 0 : stack.stackSize;
            ItemIdentity key = stack == null ? null
                : ordered && source && count > 0 ? matchingIdentity(stack, budget) : identity(stack, budget);
            ExactItem item = null;
            if (ordered && !source && stack != null
                || count > 0 && (!source || (ordered ? key != null : matchesExpected(stack, key, budget)))) {
                int maximum = chest.getInventoryStackLimit();
                ItemStack prototype = stack;
                if (source) {
                    prototype = inspectionCopy(stack, key, budget);
                    budget.chargeProvider();
                    maximum = Math.min(maximum, prototype.getMaxStackSize());
                    if (prototype.stackSize != count || !matchesStack(key, prototype, budget)) throw new Unavailable();
                }
                budget.chargeAllocation(1);
                item = new ExactItem(key, prototype, maximum);
            }
            budget.chargeAllocation(2);
            slots.add(new ExactChestSlot(endpoint, chest, storage, stack, key, item, count));
        }
        return slots;
    }

    private static ItemStack[] chestStorage(TileEntityChest chest) {
        try {
            return ChestStorage.CONTENTS == null ? null : (ItemStack[]) ChestStorage.CONTENTS.get(chest);
        } catch (IllegalAccessException failure) {
            return null;
        }
    }

    private static final class ChestStorage {

        static final java.lang.reflect.Field CONTENTS = find();

        private static java.lang.reflect.Field find() {
            for (String name : new String[] { "field_145985_p", "chestContents" }) {
                try {
                    java.lang.reflect.Field field = TileEntityChest.class.getDeclaredField(name);
                    if (field.getType() == ItemStack[].class) {
                        field.setAccessible(true);
                        return field;
                    }
                } catch (NoSuchFieldException ignored) {
                    // Try the development mapping.
                } catch (RuntimeException inaccessible) {
                    return null;
                }
            }
            return null;
        }
    }

    private static final class ExactItem {

        final ItemIdentity identity;
        final ItemStack prototype;
        final int limit;

        ExactItem(ItemIdentity identity, ItemStack prototype, int limit) {
            this.identity = identity;
            this.prototype = prototype;
            this.limit = limit;
        }
    }

    private final class ExactChestSlot extends ExactTransferPlan.Cell<ExactItem> {

        final Endpoint endpoint;
        final ItemStack[] contents;
        final ItemStack original;
        final ItemIdentity originalKey;
        ItemStack replacement;

        ExactChestSlot(Endpoint endpoint, TileEntityChest chest, ItemStack[] contents, ItemStack original,
            ItemIdentity originalKey, ExactItem item, int count) {
            super(chest, contents, endpoint.slot, item, count, chest.getInventoryStackLimit());
            this.endpoint = endpoint;
            this.contents = contents;
            this.original = original;
            this.originalKey = originalKey;
        }

        @Override
        boolean matches(ExactItem other) {
            return key.identity.matches(other.identity, budget);
        }

        @Override
        int limit(ExactItem incoming) {
            return Math.min(capacity, incoming.limit);
        }

        @Override
        void prepare() {
            if (amount == 0) return;
            replacement = inspectionCopy(key.prototype, key.identity, budget);
            replacement.stackSize = amount;
        }

        @Override
        boolean present() {
            return ItemTransferService.this.present(endpoint, budget) && !tile.isInvalid();
        }

        @Override
        boolean unchanged() {
            budget.chargeSlot();
            return chestStorage((TileEntityChest) tile) == contents && contents[index] == original
                && (original == null
                    || original.stackSize == initialAmount && matchesStack(originalKey, original, budget));
        }

        @Override
        void write() {
            contents[index] = replacement;
        }
    }

    ItemTransferResult transfer(LabelReference source, LabelReference destination, DataValue itemOrSelector,
        ResourceSelector excluded, long requested, boolean all) {
        return transfer(source, destination, itemOrSelector, excluded, requested, all, false);
    }

    ItemTransferResult transfer(LabelReference source, LabelReference destination, DataValue itemOrSelector,
        ResourceSelector excluded, long requested, boolean all, boolean forceExtraction) {
        validateArguments(source, destination, itemOrSelector, requested);
        validateExclusion(excluded);
        unavailable = false;
        this.forceExtraction = forceExtraction;
        if (!all && requested == 0) return new ItemTransferResult(0, 0, Status.COMPLETE);
        world = context.getWorld();
        if (world == null || world.provider == null || context.getNetwork() == null)
            return new ItemTransferResult(requested, 0, Status.UNAVAILABLE);
        if (world.isRemote || net.minecraft.server.MinecraftServer.getServer() != null
            && !ca.teamdman.sfm.common.net.ServerUiTasks.isServerThread())
            throw new IllegalArgumentException("Item transfers require server-side execution");
        budget = context.getInvocationQueryBudget();
        int slotWorkAtStart = budget.getSlotWork();
        filter = itemOrSelector;
        this.excluded = excluded;
        long moved = 0;
        boolean matched = false, requestedKnown = !all;
        TileEntityManager manager = context.getManager();
        ItemStack disk = manager == null ? null : manager.getDisk();
        long generation = manager == null ? 0 : manager.getDiskGeneration();
        try {
            IdentityHashMap<IInventory, Set<Integer>> selectedSources = new IdentityHashMap<>();
            List<Endpoint> sources = gather(source, true, selectedSources), destinations = gather(destination, false);
            if (manager != null && sources.stream()
                .allMatch(ItemTransferService::plainChest)
                && destinations.stream()
                    .allMatch(ItemTransferService::plainChest)) {
                requireBulkOwner(manager, disk, generation);
                ExactTransferPlan<ExactItem> plan = new ExactTransferPlan<>(context, requested);
                return plan.executeOrdered(chestSlots(sources, true, true), chestSlots(destinations, false, true), all);
            }
            if (all) for (Endpoint input : sources) {
                budget.chargeSlot();
                ItemStack stack = read(input, budget);
                if (stack == null || stack.stackSize == 0 || excluded != null && excluded.matches(stack)) continue;
                int count = stack.stackSize;
                boolean selected = filter.getType() == DataType.ITEM_KEY
                    ? matchesStack(filter.asItemIdentity(), stack, budget)
                    : filter.asSelector()
                        .matches(stack, budget);
                if (stack.stackSize != count || stack.getItem() == null)
                    throw new IllegalArgumentException("Transfer source changed during amount observation");
                if (selected) requested = Math.addExact(
                    requested,
                    input.inv instanceof BackendItemInventory nativeView ? nativeView.amount(input.slot) : count);
            }
            // A finite stock snapshot is an aggregate cap, not a reservation or an unbounded drain loop.
            requestedKnown = true;
            Boolean bulkTargets = null;
            for (Endpoint input : sources) {
                if (moved == requested) break;
                ItemStack first = read(input, budget);
                if (first == null || first.stackSize <= 0) continue;
                ItemIdentity expected = matchingIdentity(first, budget);
                if (expected == null) continue;
                ForgeDirection sourceSide = extractSide(input, first, expected, budget);
                if (sourceSide == null) continue;
                matched = true;
                long sourceAllowance = input.inv instanceof BackendItemInventory nativeView
                    ? nativeView.amount(input.slot)
                    : first.stackSize;
                if (input.inv instanceof BackendItemInventory nativeView && nativeView.backend.supportsBulkItems()
                    && manager != null
                    && Math.min(sourceAllowance, requested - moved) > 64) {
                    if (bulkTargets == null) {
                        bulkTargets = destinations.stream()
                            .noneMatch(
                                output -> output.inv instanceof BackendItemInventory target
                                    && !target.backend.supportsBulkItems());
                    }
                    if (bulkTargets) {
                        try {
                            long bulk = moveNativeBatches(
                                input,
                                sourceSide,
                                nativeView.backend,
                                first,
                                expected,
                                destinations,
                                selectedSources,
                                Math.min(sourceAllowance, requested - moved),
                                disk,
                                generation);
                            if (bulk >= 0) {
                                moved = Math.addExact(moved, bulk);
                                continue;
                            }
                        } catch (ProgramContext.ItemMoveFailure failure) {
                            moved = Math.addExact(moved, failure.moved);
                            throw failure;
                        }
                    }
                }
                for (Endpoint output : destinations) {
                    if (moved == requested || sourceAllowance <= 0) break;
                    budget.chargeSlot();
                    Set<Integer> selected = selectedSources.get(output.inv);
                    // Include empty selected source slots: output may never feed a later input in this action.
                    if (selected != null && selected.contains(output.slot)) continue;
                    if (input.inv instanceof BackendItemInventory from && output.inv instanceof BackendItemInventory to
                        && from.backend.identity() == to.backend.identity()) continue;
                    if (rejectDestination(output, expected)) continue;
                    boolean repeat;
                    do {
                        repeat = false;
                        for (ForgeDirection targetSide : ForgeDirection.VALID_DIRECTIONS) {
                            if ((output.sideMask & 1 << targetSide.ordinal()) == 0) continue;
                            if (!valid(input, sourceSide, budget)) break;
                            // Exposure can change after gathering; a closed face does not close later faces.
                            if (!valid(output, targetSide, budget)) continue;
                            ItemStack current = read(input, budget);
                            if (current == null || current.stackSize <= 0
                                || !matchesExpected(current, expected, budget)) break;
                            int inspectedAmount = current.stackSize;
                            ItemStack limitProbe = inspectionCopy(current, expected, budget);
                            budget.chargeProvider();
                            int maximum = limitProbe.getMaxStackSize();
                            // Each owned payload must fit the existing item recovery format's stack count.
                            if (input.inv instanceof BackendItemInventory) maximum = Math.min(maximum, 64);
                            if (maximum <= 0 || limitProbe.stackSize != inspectedAmount
                                || !matchesStack(expected, limitProbe, budget)
                                || !matchesExpected(current, expected, budget)) break;
                            int amount = (int) Math.min(
                                Math.min(requested - moved, sourceAllowance),
                                Math.min(current.stackSize, maximum));
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
                                repeat = input.inv instanceof BackendItemInventory && sourceAllowance > 0
                                    && moved < requested;
                                break;
                            }
                            if (!(output.inv instanceof ISidedInventory)) break;
                        }
                    } while (repeat);
                }
            }
        } catch (Unavailable failure) {
            unavailable = true;
        } catch (RuntimeException failure) {
            throw new IllegalArgumentException(
                "Item transfer failed after moving " + moved
                    + " of "
                    + requested
                    + " items: "
                    + failure.getMessage()
                    + " [slot work before transfer: "
                    + slotWorkAtStart
                    + "; now: "
                    + budget.getSlotWork()
                    + "/"
                    + budget.getSlotWorkLimit()
                    + "; source: "
                    + source.getName()
                    + "; destination: "
                    + destination.getName()
                    + "]",
                failure);
        }
        return new ItemTransferResult(
            requested,
            moved,
            moved == requested && requestedKnown ? Status.COMPLETE
                : moved > 0 ? Status.PARTIAL
                    : unavailable ? Status.UNAVAILABLE : matched ? Status.NO_CAPACITY : Status.NO_MATCH);
    }

    private static boolean plainChest(Endpoint endpoint) {
        return endpoint.view.first.getClass() == TileEntityChest.class
            && (endpoint.view.second == null || endpoint.view.second.getClass() == TileEntityChest.class)
            && chestStorage((TileEntityChest) endpoint.inv) != null;
    }

    private long moveNativeBatches(Endpoint source, ForgeDirection side, StorageBackend backend, ItemStack first,
        ItemIdentity key, List<Endpoint> destinations, Map<IInventory, Set<Integer>> selected, long limit,
        ItemStack disk, long generation) {
        ItemStack unit = inspectionCopy(first, key, budget);
        budget.chargeProvider();
        int stackLimit = unit.getMaxStackSize();
        if (stackLimit > 64) return -1; // Oversized stacks can fill a physical slot in several legacy chunks.
        if (stackLimit <= 0 || unit.stackSize != first.stackSize || !matchesStack(key, unit, budget)) return 0;
        unit.stackSize = 1;
        int next = 0;
        long moved = 0;
        try {
            while (next < destinations.size() && moved < limit) {
                budget.chargeAllocation(1);
                List<BulkPlacement> batch = new ArrayList<>();
                int planned = 0;
                boolean recheckLast = false;
                while (next < destinations.size() && batch.size() < MAX_BULK_SLOTS && planned < limit - moved) {
                    Endpoint target = destinations.get(next);
                    boolean nativeTarget = target.inv instanceof BackendItemInventory;
                    if (nativeTarget && !batch.isEmpty()) break;
                    next++;
                    budget.chargeSlot();
                    Set<Integer> sourceSlots = selected.get(target.inv);
                    if (sourceSlots != null && sourceSlots.contains(target.slot)
                        || nativeTarget && ((BackendItemInventory) target.inv).backend.identity() == backend.identity())
                        continue;
                    int preceding = batch.size();
                    if (rejectDestination(target, key)) {
                        if (preceding == 0) continue;
                        next--;
                        break;
                    }
                    int offer = (int) Math.min(limit - moved - planned, nativeTarget ? MAX_NATIVE_AMOUNT : stackLimit);
                    for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
                        if ((target.sideMask & 1 << face.ordinal()) == 0) continue;
                        budget.chargeAllocation(1);
                        Guard placement = new Guard(source, side, target, face, key);
                        int accepted = placement.insertion(unit, offer, true, budget, true, null);
                        if (accepted > 0) {
                            budget.chargeAllocation(1);
                            batch.add(new BulkPlacement(placement, accepted));
                            planned += accepted;
                            recheckLast = nativeTarget ? accepted == MAX_NATIVE_AMOUNT : accepted < offer;
                            break;
                        }
                        if (!(target.inv instanceof ISidedInventory)) break;
                    }
                    // Earlier writes can unlock a later machine slot. Recheck it after committing them.
                    if (preceding > 0 && batch.size() == preceding) {
                        next--;
                        break;
                    }
                    if ((nativeTarget || recheckLast) && !batch.isEmpty()) break;
                }
                if (batch.isEmpty()) continue;
                int placed = commitNativeBatch(source, side, backend, unit, key, batch, planned, disk, generation);
                moved += placed;
                if (recheckLast && placed == planned) next--;
            }
        } catch (ProgramContext.ItemMoveFailure failure) {
            throw new ProgramContext.ItemMoveFailure(moved + failure.moved, failure);
        } catch (RuntimeException failure) {
            throw new ProgramContext.ItemMoveFailure(moved, failure);
        }
        return moved;
    }

    private int commitNativeBatch(Endpoint source, ForgeDirection side, StorageBackend backend, ItemStack unit,
        ItemIdentity key, List<BulkPlacement> batch, int planned, ItemStack disk, long generation) {
        if (!valid(source, side, budget)) return 0;
        ItemStack request = inspectionCopy(unit, key, budget);
        request.stackSize = planned;
        Guard first = batch.get(0).guard;
        TileEntityManager manager = context.getManager();
        requireBulkOwner(manager, disk, generation);
        int inserted = 0;
        int copy = key.getCopyWork(), compare = key.getComparisonWork();
        int attempts = 0, sideWork = 0, positions = 16, providers = 160;
        for (BulkPlacement placement : batch) {
            Guard guard = placement.guard;
            int faces = guard.target.inv instanceof ISidedInventory
                ? Integer.bitCount(guard.target.sideMask >>> guard.targetSide.ordinal())
                : 1;
            attempts += faces;
            sideWork += faces * guard.target.sideWork;
            boolean chest = guard.target.inv instanceof TileEntityChest;
            positions += (chest ? 32 : 9) * faces;
            providers += (chest ? 160 : 80) * faces;
        }
        // Preflight an extraction, bounded slot commits and a source return. Each target commit
        // resolves up to three views and walks the current face's exposed slots twice.
        try (QueryBudget.Reservation reserved = budget.reserve(
            positions,
            providers,
            64 + 2 * sideWork + (2 + 3 * attempts) * copy + (1 + 3 * attempts) * compare + 8 * attempts,
            64 + (2 + 3 * attempts) * (2 * copy + 4) + 40 * attempts)) {
            QueryBudget commit = reserved.budget();
            budget.chargeAllocation(3);
            BulkJournal journal = null;
            manager.setItemRecoveryInProgress(true);
            try {
                journal = new BulkJournal(
                    manager,
                    unit,
                    planned,
                    source.position,
                    source.slot,
                    side,
                    first.target.position,
                    first.target.slot,
                    first.targetSide);
                ItemStack extracted = backend.extract(request, false, commit);
                int remaining = extracted == null ? 0 : extracted.stackSize;
                if (extracted != null
                    && (remaining <= 0 || remaining > planned || !matchesStack(key, extracted, commit)))
                    throw new IllegalArgumentException("Invalid bulk item extraction result");
                journal.confirmed(remaining);
                for (BulkPlacement placement : batch) {
                    if (remaining == 0) break;
                    int offer = Math.min(remaining, placement.amount);
                    int accepted = placeNativeItems(placement.guard, unit, offer, commit, journal);
                    remaining -= accepted;
                    inserted += accepted;
                    if (accepted > 0) context.setDidSomething(true);
                    journal.confirmed(remaining);
                }
                if (remaining > 0) {
                    if (!present(source, commit))
                        throw new IllegalStateException("Bulk item source unavailable for rollback");
                    ItemStack remainder = inspectionCopy(unit, key, commit);
                    remainder.stackSize = remaining;
                    journal.attempt("source rollback", remaining);
                    int returned = backend.insert(remainder, false, commit);
                    if (returned < 0 || returned > remaining)
                        throw new IllegalArgumentException("Invalid bulk item rollback result");
                    remaining -= returned;
                    journal.confirmed(remaining);
                    if (remaining > 0) throw new IllegalStateException("Bulk item remainder preserved on disk");
                }
                journal.finish();
                return inserted;
            } catch (RuntimeException failure) {
                if (journal != null) journal.pause();
                throw new ProgramContext.ItemMoveFailure(inserted, failure);
            } finally {
                manager.setItemRecoveryInProgress(false);
            }
        }
    }

    private int placeNativeItems(Guard guard, ItemStack unit, int offer, QueryBudget commit, BulkJournal journal) {
        int inserted = guard.insertion(unit, offer, false, commit, true, journal);
        if (inserted != 0 || !(guard.target.inv instanceof ISidedInventory)) return inserted;
        // A face can close between the plan and commit. Try the remaining selected faces in order.
        for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
            if (face.ordinal() <= guard.targetSide.ordinal() || (guard.target.sideMask & 1 << face.ordinal()) == 0)
                continue;
            commit.chargeAllocation(1);
            Guard alternative = new Guard(guard.source, guard.sourceSide, guard.target, face, guard.expected);
            inserted = alternative.insertion(unit, offer, false, commit, true, journal);
            if (inserted > 0) return inserted;
        }
        return 0;
    }

    private static void requireBulkOwner(TileEntityManager manager, ItemStack disk, long generation) {
        if (manager.getDisk() != disk || manager.getDiskGeneration() != generation || manager.hasPendingRecovery())
            throw new IllegalStateException("Bulk item transfer owner changed before extraction");
    }

    private static final class BulkPlacement {

        final Guard guard;
        final int amount;

        BulkPlacement(Guard guard, int amount) {
            this.guard = guard;
            this.amount = amount;
        }
    }

    private ItemIdentity matchingIdentity(ItemStack stack, QueryBudget allowance) {
        if (excluded != null && excluded.matches(stack)) return null;
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

    private TileEntity linkedTile(LabelReference label, BlockPos position, QueryBudget allowance) {
        allowance.chargePosition();
        if (!context.getLabelPositionHolder()
            .getPositions(label.getName())
            .contains(position)) return null;
        allowance.chargeProvider();
        return context.getNetwork()
            .isConnected(position) ? tile(position, allowance) : null;
    }

    private View resolve(LabelReference label, BlockPos position, QueryBudget allowance, boolean observeStock) {
        TileEntity found = linkedTile(label, position, allowance);
        if (found instanceof TileEntityModAdapter adapter) {
            if (label.hasSlotSelection())
                throw new IllegalArgumentException("Network adapters do not have physical slots");
            BackendItemInventory inventory = backendViews.get(adapter);
            if (inventory == null) {
                StorageBackend backend = adapter.open(allowance);
                if (backend == null || !backend.supportsItems()) return null;
                inventory = new BackendItemInventory(backend, allowance, observeStock);
                backendViews.put(adapter, inventory);
            }
            allowance.chargeAllocation(1);
            return inventory.backend.current(allowance) ? new View(inventory, null, position, null) : null;
        }
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

    private List<Endpoint> gather(LabelReference label, boolean source) {
        return gather(label, source, new IdentityHashMap<>());
    }

    private List<Endpoint> gather(LabelReference label, boolean source, Map<IInventory, Set<Integer>> seen) {
        Set<BlockPos> positions = context.getLabelPositionHolder()
            .getPositions(label.getName());
        if (positions.isEmpty()) throw new Unavailable();
        budget.chargeAllocation(2);
        List<Endpoint> endpoints = new ArrayList<>();
        Set<Object> backends = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        for (BlockPos position : positions) {
            View view = resolve(label, position, budget, source);
            if (view == null) throw new Unavailable();
            if (view.first instanceof BackendItemInventory nativeView && !backends.add(nativeView.backend.identity()))
                continue;
            int firstSize = size(view.first, budget), secondSize = view.second == null ? 0 : size(view.second, budget);
            long combined = (long) firstSize + secondSize;
            if (combined > Integer.MAX_VALUE) throw new IllegalArgumentException("Transfer inventory size overflow");
            int end = Math.min((int) combined - 1, label.getLastSlot());
            int first = label.getFirstSlot();
            if (view.first instanceof BackendItemInventory) {
                if (source && firstSize > 1) first = 1; // Skip insertion slot zero when stock rows exist.
                else end = 0;
            }
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
                int count = Math.max(0, end - first + 1);
                budget.chargeSlots(count);
                budget.chargeAllocation(count * 2);
                for (int slot = first; slot <= end; slot++) slots.put(slot, label.getSideMask());
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
        if (endpoint.view.first instanceof TileEntityChest) {
            View view = resolve(endpoint.label, endpoint.labelPosition, allowance, true);
            if (view == null) return missing();
            int firstSize = size(view.first, allowance);
            boolean first = endpoint.logicalSlot < firstSize;
            IInventory inventory = first ? view.first : view.second;
            BlockPos position = first ? view.firstPosition : view.secondPosition;
            int slot = first ? endpoint.logicalSlot : endpoint.logicalSlot - firstSize;
            if (inventory != endpoint.inv || position == null
                || !position.equals(endpoint.position)
                || slot != endpoint.slot
                || slot < 0
                || slot >= (first ? firstSize : size(inventory, allowance))) return missing();
        } else if (!presentSingle(endpoint, allowance) || endpoint.slot >= size(endpoint.inv, allowance)) {
            return missing();
        }
        if ((endpoint.label.getSideMask() & 1 << side.ordinal()) == 0) return false;
        if (endpoint.inv instanceof ISidedInventory sided) {
            allowance.chargeProvider();
            int[] exposed = sided.getAccessibleSlotsFromSide(side.ordinal());
            if (exposed == null) return false;
            endpoint.sideWork = Math.max(endpoint.sideWork, exposed.length);
            boolean reusedExposure = exposed == endpoint.view.exposure;
            endpoint.view.exposure = exposed;
            // A fresh provider call is required. The old index is only a hint and must still match
            // the live slot, even when the provider returns the same array after changing it in place.
            int hint = endpoint.exposedIndex;
            if (hint >= 0 && hint < exposed.length && exposed[hint] == endpoint.slot) {
                allowance.chargeSlots(reusedExposure ? 1 : exposed.length);
                return true;
            }
            allowance.chargeSlots(exposed.length);
            for (int index = 0; index < exposed.length; index++) {
                if (exposed[index] != endpoint.slot) continue;
                endpoint.exposedIndex = index;
                return true;
            }
            return false;
        }
        return true;
    }

    private boolean presentSingle(Endpoint endpoint, QueryBudget allowance) {
        TileEntity found = linkedTile(endpoint.label, endpoint.labelPosition, allowance);
        if (endpoint.inv instanceof BackendItemInventory nativeView)
            return backendViews.get(found) == nativeView && nativeView.backend.current(allowance);
        return found == endpoint.inv;
    }

    private boolean missing() {
        unavailable = true;
        return false;
    }

    /** Fresh negative observation only; positive candidates retain every simulation/commit check. */
    private boolean rejectDestination(Endpoint output, ItemIdentity expected) {
        // An adapter destination is a write-only virtual slot. Its simulation already validates the binding.
        if (output.inv instanceof BackendItemInventory) return false;
        ForgeDirection side = ForgeDirection.VALID_DIRECTIONS[Integer.numberOfTrailingZeros(output.sideMask)];
        boolean sided = output.inv instanceof ISidedInventory;
        // A missing advisory face is not evidence that every other face rejects the slot.
        if (!valid(output, side, budget)) return !sided;
        int limit = Integer.MAX_VALUE;
        if (!sided) {
            budget.chargeProvider();
            limit = output.inv.getInventoryStackLimit();
        }
        ItemStack current = limit <= 0 ? null : read(output, budget);
        // Limit/stack callbacks may have replaced or unloaded the provider. Never reuse this observation
        // as transfer authority; all positive candidates still enter the full simulation/commit guards.
        if (!present(output, budget)) return true;
        // Occupied slots of a different item/metadata cannot merge on any side. Avoid repeating
        // expensive source/NBT/permission work for every face; do not cache this observation.
        return limit <= 0 || current != null && (current.stackSize >= limit || !matchesType(expected, current));
    }

    private boolean matchesType(ItemIdentity expected, ItemStack current) {
        budget.chargeProvider();
        return expected.matchesType(current, budget);
    }

    private ForgeDirection extractSide(Endpoint endpoint, ItemStack stack, ItemIdentity key, QueryBudget allowance) {
        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            if ((endpoint.sideMask & 1 << side.ordinal()) == 0 || !valid(endpoint, side, allowance)) continue;
            int originalAmount = stack.stackSize;
            if (!matchesExpected(stack, key, allowance)) return null;
            // Force mode skips only the extraction veto. The selected face must still expose the slot.
            if (forceExtraction || !(endpoint.inv instanceof ISidedInventory)) return side;
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
        if (!(endpoint.view.first instanceof TileEntityChest)) return presentSingle(endpoint, allowance) || missing();
        View view = resolve(endpoint.label, endpoint.labelPosition, allowance, true);
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
        ItemStack preparedSource;
        boolean attempted;
        int beforeCount, attemptedCount;
        DrawerItemSource.Observation drawerSource;

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
            boolean virtualDrawer = DrawerItemSource.handles(source.inv);
            boolean backend = source.inv instanceof BackendItemInventory || target.inv instanceof BackendItemInventory;
            if (backend) {
                work = Math.addExact(work, 256);
                allocations = Math.addExact(allocations, 256);
            }
            if (virtualDrawer) {
                // Public drawer mapping/count checks and known rollback need extra credit; ordinary slots are
                // unchanged.
                work = Math.addExact(work, Math.multiplyExact(6, expected.getComparisonWork()));
                work = Math.addExact(work, expected.getCopyWork());
                allocations = Math.addExact(allocations, expected.getCopyWork() * 2 + 384);
            }
            reservation = budget.reserve(
                COMMIT_POSITIONS + (backend ? 128 : 0),
                COMMIT_CALLBACKS + (virtualDrawer ? 128 : 0) + (backend ? 1536 : 0),
                work,
                allocations);
            commit = reservation.budget();
            preparedSource = current;
            return true;
        }

        void close() {
            if (reservation != null) reservation.close();
        }

        public ItemStack extract(int accepted) {
            // moveItemPair invokes this immediately after beforeExtract, with no provider callback in between.
            ItemStack current = preparedSource;
            if (!forceExtraction && source.inv instanceof ISidedInventory) {
                int inspectedAmount = current.stackSize;
                ItemStack inspection = inspectionCopy(current, expected, commit);
                commit.chargeProvider();
                if (!InventoryUtils.canExtract(source.inv, source.slot, inspection, sourceSide)) return null;
                if (inspection.stackSize != inspectedAmount || !matchesStack(expected, inspection, commit)) return null;
            }
            if (!valid(source, sourceSide, commit) || !valid(target, targetSide, commit)) return null;
            current = read(source, commit);
            if (current == null || !matchesStack(expected, current, commit)) return null;
            int amount = Math.min(accepted, current.stackSize);
            if (amount <= 0) return null;
            beforeCount = current.stackSize;
            if (DrawerItemSource.handles(source.inv)) {
                drawerSource = DrawerItemSource.observe(source.inv, source.slot, expected, commit);
                if (drawerSource == null) return null;
            }
            if (!present(source, commit) || !present(target, commit)) return null;
            commit.chargeProviders(2);
            attemptedCount = amount;
            attempted = true;
            if (source.inv instanceof BackendItemInventory nativeView) {
                ItemStack request = inspectionCopy(current, expected, commit);
                request.stackSize = amount;
                ItemStack extracted = nativeView.backend.extract(request, false, commit);
                if (extracted == null) attempted = false; // A successful AE call returning null extracted nothing.
                return extracted;
            }
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
            if (!matchesStack(expected, extracted, commit)) return false;
            if (source.inv instanceof BackendItemInventory) return true; // The backend's returned amount is
                                                                         // authoritative.
            if (!valid(source, sourceSide, commit)) return false;
            if (drawerSource != null) return drawerSource.acceptsExtraction(extracted.stackSize, commit);
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
            return insertion(offered, offered.stackSize, simulate, allowance, false, null);
        }

        private int insertion(ItemStack offered, int offeredAmount, boolean simulate, QueryBudget allowance,
            boolean bulk, BulkJournal journal) {
            if ((!bulk && !valid(source, sourceSide, allowance)) || !valid(target, targetSide, allowance)) return 0;
            if (target.inv instanceof BackendItemInventory nativeView) {
                if (target.slot != 0) return 0;
                ItemStack copy = inspectionCopy(offered, expected, allowance);
                copy.stackSize = offeredAmount;
                if (journal != null) journal.placement(target.position, target.slot, targetSide, offeredAmount);
                int inserted = nativeView.backend.insert(copy, simulate, allowance);
                if (inserted < 0 || inserted > offeredAmount)
                    throw new IllegalArgumentException("Invalid adapter insertion result");
                return inserted;
            }
            ItemStack inspection = inspectionCopy(offered, expected, allowance);
            inspection.stackSize = offeredAmount;
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
            if (maximum <= 0 || (!bulk && !valid(source, sourceSide, allowance))
                || !valid(target, targetSide, allowance)) return 0;
            if (inspection.stackSize != offeredAmount || !matchesStack(expected, inspection, allowance)) return 0;
            ItemStack existing = read(target, allowance);
            if (existing != null && !matchesStack(expected, existing, allowance)) return 0;
            int amount = (int) Math
                .min(offeredAmount, Math.max(0L, (long) maximum - (existing == null ? 0 : existing.stackSize)));
            if (amount <= 0 || simulate) return amount;
            allowance.chargeSlots(expected.getCopyWork());
            allowance.chargeAllocation(expected.getCopyWork() * 2 + 4);
            ItemStack replacement = offered.copy();
            replacement.stackSize = amount + (existing == null ? 0 : existing.stackSize);
            if ((!bulk && !present(source, allowance)) || !present(target, allowance)) return 0;
            allowance.chargeProviders(2);
            if (journal != null) journal.placement(target.position, target.slot, targetSide, amount);
            target.inv.setInventorySlotContents(target.slot, replacement);
            target.inv.markDirty();
            return amount;
        }

        public boolean restore(ItemStack remainder) {
            if (source.inv instanceof BackendItemInventory nativeView) {
                if (!recoveryPresent()) return false;
                ItemStack copy = inspectionCopy(remainder, expected, commit);
                int restored = nativeView.backend.insert(copy, false, commit);
                if (restored < 0 || restored > remainder.stackSize)
                    throw new IllegalArgumentException("Invalid adapter rollback result");
                remainder.stackSize -= restored;
                return remainder.stackSize == 0;
            }
            if (drawerSource != null) {
                try {
                    commit.chargeProviders(4);
                    commit.chargePositions(2);
                } catch (RuntimeException refused) {
                    return false;
                }
                return drawerSource.restore(remainder, commit, this::recoveryPresent);
            }
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
            if (source.inv instanceof BackendItemInventory nativeView)
                return source.position.isLoaded(world)
                    && backendViews.get(world.getTileEntity(source.position.x, source.position.y, source.position.z))
                        == nativeView
                    && nativeView.backend.current(commit);
            return source.position.isLoaded(world)
                && world.getTileEntity(source.position.x, source.position.y, source.position.z) == source.inv;
        }
    }

    private static final class View {

        final IInventory first, second;
        final BlockPos firstPosition, secondPosition;
        int[] exposure;

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
        int exposedIndex = -1;

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
