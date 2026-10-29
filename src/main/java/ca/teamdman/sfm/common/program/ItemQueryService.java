package ca.teamdman.sfm.common.program;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.data.DataQueries;
import ca.teamdman.sfml.data.ItemContents;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;

/** Fresh item observations. Legacy HAS and transfer work deliberately keep their existing contracts. */
public final class ItemQueryService implements DataQueries {

    private static final ForgeDirection[] HORIZONTAL = { ForgeDirection.WEST, ForgeDirection.EAST, ForgeDirection.NORTH,
        ForgeDirection.SOUTH };
    private final ProgramContext context;

    public ItemQueryService(ProgramContext context) {
        this.context = context;
    }

    /** Physical access restrictions, also carried by immutable surface-language label handles. */
    public static final class Access {

        public static final Access DEFAULT = new Access(63, 0, Integer.MAX_VALUE);
        public final int sideMask, firstSlot, lastSlot;

        public Access(int sideMask, int firstSlot, int lastSlot) {
            if (sideMask <= 0 || (sideMask & ~63) != 0 || firstSlot < 0 || lastSlot < firstSlot)
                throw new IllegalArgumentException("Invalid item query access");
            this.sideMask = sideMask;
            this.firstSlot = firstSlot;
            this.lastSlot = lastSlot;
        }

        boolean includes(int slot) {
            return slot >= firstSlot && slot <= lastSlot;
        }

        static Access from(LabelReference label) {
            if (label == null) throw new IllegalArgumentException("Typed query arguments are required");
            return label.getSideMask() == 63 && label.getFirstSlot() == 0 && label.getLastSlot() == Integer.MAX_VALUE
                ? DEFAULT
                : new Access(label.getSideMask(), label.getFirstSlot(), label.getLastSlot());
        }
    }

    @Override
    public long count(LabelReference source, ResourceSelector selector, QueryBudget budget) {
        return count(source, selector, budget, Access.from(source));
    }

    public long count(LabelReference source, ResourceSelector selector, QueryBudget budget, Access access) {
        Observation result = observe(source, selector, budget, access, true);
        if (!result.complete)
            throw new IllegalArgumentException("Query unavailable for LABEL(" + source.getName() + ")");
        return result.count;
    }

    @Override
    public boolean empty(LabelReference source, ResourceSelector selector, QueryBudget budget) {
        return empty(source, selector, budget, Access.from(source));
    }

    public boolean empty(LabelReference source, ResourceSelector selector, QueryBudget budget, Access access) {
        Observation result = observe(source, selector, budget, access, true);
        return result.complete && result.count == 0;
    }

    @Override
    public boolean available(LabelReference source, ResourceSelector selector, QueryBudget budget) {
        return available(source, selector, budget, Access.from(source));
    }

    public boolean available(LabelReference source, ResourceSelector selector, QueryBudget budget, Access access) {
        return observe(source, selector, budget, access, false).complete;
    }

    /**
     * Fresh native snapshot grouped by exact item identity, with physical origins. Ordering follows label
     * encounter order and ascending eligible slots. Failure never publishes a partial snapshot. This is
     * observation-only: selected sides describe visibility and do not assert extraction permission.
     */
    public ItemContents contents(LabelReference source, ResourceSelector selector, QueryBudget budget) {
        Access access = Access.from(source);
        if (selector == null || budget == null)
            throw new IllegalArgumentException("Typed query arguments are required");
        World world = context.getWorld();
        if (world == null || world.provider == null)
            throw new IllegalArgumentException("Query unavailable for LABEL(" + source.getName() + ")");
        ContentsCollector collector = new ContentsCollector(world.provider.dimensionId, budget);
        Observation result = observe(source, selector, budget, access, true, collector);
        if (!result.complete)
            throw new IllegalArgumentException("Query unavailable for LABEL(" + source.getName() + ")");
        return collector.builder.build();
    }

    private Observation observe(LabelReference source, ResourceSelector selector, QueryBudget budget, Access access,
        boolean stock) {
        return observe(source, selector, budget, access, stock, null);
    }

    private Observation observe(LabelReference source, ResourceSelector selector, QueryBudget budget, Access access,
        boolean stock, ContentsCollector collector) {
        if (source == null || selector == null || budget == null || access == null)
            throw new IllegalArgumentException("Typed query arguments are required");
        World world = context.getWorld();
        if (world == null || context.getNetwork() == null) return Observation.UNAVAILABLE;
        if (world.isRemote) throw new IllegalArgumentException("Typed queries require server-side execution");
        Set<BlockPos> positions = context.getLabelPositionHolder()
            .getPositions(source.getName());
        if (positions.isEmpty()) return Observation.UNAVAILABLE;
        budget.chargeAllocation(1);
        Set<IInventory> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        long total = 0;
        try {
            for (BlockPos pos : positions) {
                budget.chargePosition();
                budget.chargeProvider();
                if (!context.getNetwork()
                    .isConnected(pos)) return Observation.UNAVAILABLE;
                TileEntity tile = loadedTile(world, pos, budget);
                if (!(tile instanceof IInventory)) return Observation.UNAVAILABLE;
                IInventory first = (IInventory) tile, second = null;
                BlockPos firstPosition = pos, secondPosition = null;
                if (tile instanceof TileEntityChest) {
                    // Never call vanilla adjacency discovery: it may probe an unloaded neighbor chunk.
                    // Resolve all four horizontal neighbors explicitly and reject ambiguous/triple chests.
                    TileEntityChest partner = null;
                    ForgeDirection partnerDirection = null;
                    for (ForgeDirection direction : HORIZONTAL) {
                        BlockPos neighbor = pos.offset(direction);
                        if (!loaded(world, neighbor, budget)) return Observation.UNAVAILABLE;
                        budget.chargeProvider();
                        TileEntity adjacent = world.getTileEntity(neighbor.x, neighbor.y, neighbor.z);
                        if (!(adjacent instanceof TileEntityChest)) continue;
                        budget.chargeProvider();
                        int chestType = ((TileEntityChest) tile).func_145980_j();
                        budget.chargeProvider();
                        if (((TileEntityChest) adjacent).func_145980_j() != chestType) continue;
                        if (partner != null) throw new IllegalArgumentException("Query found an ambiguous chest group");
                        partner = (TileEntityChest) adjacent;
                        partnerDirection = direction;
                    }
                    if (partner != null) {
                        // Both members must agree on an isolated pair; a hidden third chest must not be counted twice.
                        if (!validPartner(world, pos, partner, budget)) return Observation.UNAVAILABLE;
                        second = partner;
                        if (collector != null) {
                            budget.chargeAllocation(1);
                            secondPosition = pos.offset(partnerDirection);
                        }
                        if (partnerDirection == ForgeDirection.WEST || partnerDirection == ForgeDirection.NORTH) {
                            if (collector != null) {
                                firstPosition = secondPosition;
                                secondPosition = pos;
                            }
                            first = partner;
                            second = (IInventory) tile;
                        }
                    }
                }
                if (seen.contains(first) && (second == null || seen.contains(second))) continue;
                if (seen.contains(first) || second != null && seen.contains(second))
                    throw new IllegalArgumentException("Query found overlapping inventory views");
                budget.chargeAllocation(second == null ? 1 : 2);
                seen.add(first);
                if (second != null) seen.add(second);
                long count = second == null ? read(first, selector, budget, access, stock, firstPosition, collector)
                    : readPair(
                        first,
                        second,
                        selector,
                        budget,
                        access,
                        stock,
                        firstPosition,
                        secondPosition,
                        collector);
                if (count < 0) return Observation.UNAVAILABLE;
                total = Math.addExact(total, count);
            }
            return new Observation(true, total);
        } catch (ArithmeticException overflow) {
            throw new IllegalArgumentException("Query count overflow", overflow);
        } catch (IllegalArgumentException failure) {
            throw failure;
        } catch (RuntimeException failure) {
            throw new IllegalArgumentException(
                "Item query provider failed: " + failure.getClass()
                    .getSimpleName(),
                failure);
        }
    }

    private static boolean loaded(World world, BlockPos pos, QueryBudget budget) {
        budget.chargeProvider();
        return pos.isLoaded(world);
    }

    private static TileEntity loadedTile(World world, BlockPos pos, QueryBudget budget) {
        if (!loaded(world, pos, budget)) return null;
        budget.chargeProvider();
        return world.getTileEntity(pos.x, pos.y, pos.z);
    }

    private static boolean validPartner(World world, BlockPos original, TileEntityChest partner, QueryBudget budget) {
        BlockPos pos = new BlockPos(partner.xCoord, partner.yCoord, partner.zCoord);
        for (ForgeDirection direction : HORIZONTAL) {
            BlockPos neighbor = pos.offset(direction);
            if (neighbor.equals(original)) continue;
            if (!loaded(world, neighbor, budget)) return false;
            budget.chargeProvider();
            TileEntity tile = world.getTileEntity(neighbor.x, neighbor.y, neighbor.z);
            if (tile instanceof TileEntityChest) {
                budget.chargeProvider();
                int type = ((TileEntityChest) tile).func_145980_j();
                budget.chargeProvider();
                if (type == partner.func_145980_j())
                    throw new IllegalArgumentException("Query found an ambiguous chest group");
            }
        }
        return true;
    }

    private static int size(IInventory inventory, QueryBudget budget) {
        budget.chargeProvider();
        int size = inventory.getSizeInventory();
        if (size < 0) throw new IllegalArgumentException("Query provider returned a negative inventory size");
        return size;
    }

    private static long read(IInventory inventory, ResourceSelector selector, QueryBudget budget, Access access,
        boolean stock, BlockPos position, ContentsCollector collector) {
        int size = size(inventory, budget);
        budget.chargeAllocation(1);
        Set<Integer> slots = new HashSet<>();
        if (inventory instanceof ISidedInventory) {
            for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
                if ((access.sideMask & 1 << direction.ordinal()) == 0) continue;
                budget.chargeProvider();
                int[] exposed = ((ISidedInventory) inventory).getAccessibleSlotsFromSide(direction.ordinal());
                if (exposed == null) continue;
                // A provider may allocate internally; reserve all returned entries before walking/copying them.
                budget.chargeSlots(exposed.length);
                for (int slot : exposed)
                    if (slot >= 0 && slot < size && access.includes(slot) && !slots.contains(slot)) {
                        budget.chargeAllocation(1);
                        slots.add(slot);
                    }
            }
        } else {
            int end = Math.min(size - 1, access.lastSlot);
            int count = Math.max(0, end - access.firstSlot + 1);
            budget.chargeSlots(count);
            budget.chargeAllocation(count);
            for (int slot = access.firstSlot; slot <= end; slot++) slots.add(slot);
        }
        if (slots.isEmpty()) return -1;
        long total = 0;
        if (stock) {
            Iterable<Integer> ordered = slots;
            if (collector != null) {
                // HashSet iteration is not part of the observation order. Sort only contents scans;
                // legacy count/available retain their provider work and allocation behavior.
                budget.chargeSlots(slots.size());
                budget.chargeAllocation(slots.size() * 2 + 1);
                List<Integer> sorted = new ArrayList<>(slots);
                Collections.sort(sorted, (left, right) -> {
                    budget.chargeSlot();
                    return Integer.compare(left, right);
                });
                ordered = sorted;
            }
            for (int slot : ordered)
                total = Math.addExact(total, stack(inventory, slot, selector, budget, access, position, collector));
        }
        return total;
    }

    private static long readPair(IInventory first, IInventory second, ResourceSelector selector, QueryBudget budget,
        Access access, boolean stock, BlockPos firstPosition, BlockPos secondPosition, ContentsCollector collector) {
        int firstSize = size(first, budget), secondSize = size(second, budget);
        long combined = (long) firstSize + secondSize;
        if (combined > Integer.MAX_VALUE) throw new IllegalArgumentException("Query inventory size overflow");
        int end = Math.min((int) combined - 1, access.lastSlot);
        int count = Math.max(0, end - access.firstSlot + 1);
        budget.chargeSlots(count);
        if (count == 0) return -1;
        long total = 0;
        if (stock) for (int slot = access.firstSlot; slot <= end; slot++) {
            total = Math.addExact(
                total,
                slot < firstSize ? stack(first, slot, selector, budget, access, firstPosition, collector)
                    : stack(second, slot - firstSize, selector, budget, access, secondPosition, collector));
        }
        return total;
    }

    private static long stack(IInventory inventory, int slot, ResourceSelector selector, QueryBudget budget,
        Access access, BlockPos position, ContentsCollector collector) {
        budget.chargeSlot();
        budget.chargeProvider();
        ItemStack stack = inventory.getStackInSlot(slot);
        if (stack == null) return 0;
        if (stack.stackSize < 0 || stack.getItem() == null)
            throw new IllegalArgumentException("Query provider returned an invalid item stack");
        if (stack.stackSize <= 0 || !selector.matches(stack, budget)) return 0;
        int amount = stack.stackSize;
        if (collector != null) collector.add(stack, position, slot, access.sideMask, amount, budget);
        return amount;
    }

    private static final class ContentsCollector {

        final int dimension;
        final ItemContents.Builder builder;

        ContentsCollector(int dimension, QueryBudget budget) {
            budget.chargeAllocation(1);
            this.dimension = dimension;
            builder = new ItemContents.Builder(budget);
        }

        void add(ItemStack stack, BlockPos position, int slot, int sideMask, long amount, QueryBudget budget) {
            budget.chargeProvider(); // Item registry lookup used by the detached exact key.
            ItemIdentity item = ItemIdentity.fromStack(stack, budget);
            builder.add(item, dimension, position.x, position.y, position.z, slot, sideMask, amount);
        }
    }

    private static final class Observation {

        static final Observation UNAVAILABLE = new Observation(false, 0);
        final boolean complete;
        final long count;

        Observation(boolean complete, long count) {
            this.complete = complete;
            this.count = count;
        }
    }
}
