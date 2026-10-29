package ca.teamdman.sfm.common.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfm.compat.Compat;
import ca.teamdman.sfm.compat.stevefm.SteveFMCompat;
import ca.teamdman.sfml.ast.NumberRangeSet;
import ca.teamdman.sfml.ast.ResourceIdSet;
import ca.teamdman.sfml.ast.SideQualifier;
import ca.teamdman.sfml.ast.With;

public final class InventoryUtils {

    private InventoryUtils() {}

    private static final ForgeDirection[] CHEST_DIRECTIONS = { ForgeDirection.WEST, ForgeDirection.EAST,
        ForgeDirection.NORTH, ForgeDirection.SOUTH };

    public static IInventory getInventory(World world, BlockPos pos) {
        if (world == null || pos == null) return null;
        TileEntity te = pos.getTileEntity(world);
        if (te instanceof TileEntityChest) {
            ChestPair pair = resolveChest((TileEntityChest) te);
            if (pair == null) return null;
            return pair.second == null ? pair.first : new ResolvedChestInventory(pair.first, pair.second);
        }
        if (te instanceof IInventory) {
            return (IInventory) te;
        }
        if (Compat.isSFMLoaded()) {
            try {
                IInventory steveInv = SteveFMCompat.getInventory(world, pos, te);
                if (steveInv != null) {
                    return steveInv;
                }
            } catch (Throwable ignored) {}
        }
        return null;
    }

    public static TileEntityChest getAdjacentChest(TileEntityChest chest) {
        ChestPair pair = resolveChest(chest);
        if (pair == null || pair.second == null) return null;
        return pair.first == chest ? pair.second : pair.first;
    }

    /** Resolve fresh loaded members; vanilla adjacency discovery can load chunks and retain stale partners. */
    private static ChestPair resolveChest(TileEntityChest chest) {
        if (chest == null || chest.isInvalid()) return null;
        World world = chest.getWorldObj();
        BlockPos pos = new BlockPos(chest.xCoord, chest.yCoord, chest.zCoord);
        if (!pos.isLoaded(world) || pos.getTileEntity(world) != chest) return null;
        int type = chest.func_145980_j();
        TileEntityChest partner = null;
        ForgeDirection partnerDirection = null;
        for (ForgeDirection direction : CHEST_DIRECTIONS) {
            BlockPos neighbor = pos.offset(direction);
            if (!neighbor.isLoaded(world)) return null;
            TileEntity adjacent = neighbor.getTileEntity(world);
            if (!(adjacent instanceof TileEntityChest)) continue;
            TileEntityChest candidate = (TileEntityChest) adjacent;
            if (candidate.isInvalid()) return null;
            if (candidate.func_145980_j() != type) continue;
            if (partner != null) return null;
            partner = candidate;
            partnerDirection = direction;
        }
        if (partner == null) return new ChestPair(chest, null);
        BlockPos partnerPos = pos.offset(partnerDirection);
        for (ForgeDirection direction : CHEST_DIRECTIONS) {
            BlockPos neighbor = partnerPos.offset(direction);
            if (neighbor.equals(pos)) continue;
            if (!neighbor.isLoaded(world)) return null;
            TileEntity adjacent = neighbor.getTileEntity(world);
            if (adjacent instanceof TileEntityChest) {
                TileEntityChest candidate = (TileEntityChest) adjacent;
                if (candidate.isInvalid() || candidate.func_145980_j() == type) return null;
            }
        }
        return partnerDirection == ForgeDirection.WEST || partnerDirection == ForgeDirection.NORTH
            ? new ChestPair(partner, chest)
            : new ChestPair(chest, partner);
    }

    /** Compare slots from the loaded chest views returned here without allocating or using reflection. */
    public static boolean samePhysicalSlot(IInventory first, int firstSlot, IInventory second, int secondSlot) {
        if (first == null || second == null || firstSlot < 0 || secondSlot < 0) return false;
        if (first instanceof ResolvedChestInventory) {
            ResolvedChestInventory chest = (ResolvedChestInventory) first;
            if (firstSlot >= chest.firstSize) {
                firstSlot -= chest.firstSize;
                first = chest.second;
            } else first = chest.first;
        }
        if (second instanceof ResolvedChestInventory) {
            ResolvedChestInventory chest = (ResolvedChestInventory) second;
            if (secondSlot >= chest.firstSize) {
                secondSlot -= chest.firstSize;
                second = chest.second;
            } else second = chest.first;
        }
        return first == second && firstSlot == secondSlot;
    }

    private static final class ChestPair {

        final TileEntityChest first, second;

        ChestPair(TileEntityChest first, TileEntityChest second) {
            this.first = first;
            this.second = second;
        }
    }

    private static final class ResolvedChestInventory extends InventoryLargeChest {

        final IInventory first, second;
        final int firstSize;

        ResolvedChestInventory(IInventory first, IInventory second) {
            super("container.chestDouble", first, second);
            this.first = first;
            this.second = second;
            firstSize = first.getSizeInventory();
        }
    }

    public static IFluidHandler getFluidHandler(World world, BlockPos pos) {
        if (world == null || pos == null) return null;
        TileEntity te = pos.getTileEntity(world);
        if (te instanceof IFluidHandler) {
            return (IFluidHandler) te;
        }
        if (Compat.isSFMLoaded()) {
            try {
                IFluidHandler steveFluid = SteveFMCompat.getFluidHandler(world, pos, te);
                if (steveFluid != null) {
                    return steveFluid;
                }
            } catch (Throwable ignored) {}
        }
        return null;
    }

    public static int[] getAccessibleSlots(IInventory inv, ForgeDirection side, NumberRangeSet slotFilter) {
        if (inv == null) return new int[0];

        int[] rawSlots;
        if (inv instanceof ISidedInventory && side != null && side != ForgeDirection.UNKNOWN) {
            rawSlots = ((ISidedInventory) inv).getAccessibleSlotsFromSide(side.ordinal());
            if (rawSlots == null) rawSlots = new int[0];
        } else {
            rawSlots = new int[inv.getSizeInventory()];
            for (int i = 0; i < rawSlots.length; i++) {
                rawSlots[i] = i;
            }
        }

        int size = inv.getSizeInventory();
        boolean[] seen = new boolean[size];
        int[] result = new int[Math.min(size, rawSlots.length)];
        int count = 0;
        for (int slot : rawSlots) {
            if (slot >= 0 && slot < size && !seen[slot] && (slotFilter == null || slotFilter.contains(slot))) {
                seen[slot] = true;
                result[count++] = slot;
            }
        }
        return Arrays.copyOf(result, count);
    }

    public static boolean canExtract(IInventory inv, int slot, ItemStack stack, ForgeDirection side) {
        if (inv == null || stack == null) return false;
        if (inv instanceof ISidedInventory && side != null && side != ForgeDirection.UNKNOWN) {
            return ((ISidedInventory) inv).canExtractItem(slot, stack, side.ordinal());
        }
        return true;
    }

    public static boolean canInsert(IInventory inv, int slot, ItemStack stack, ForgeDirection side) {
        if (inv == null || stack == null) return false;
        if (!inv.isItemValidForSlot(slot, stack)) return false;
        if (inv instanceof ISidedInventory && side != null && side != ForgeDirection.UNKNOWN) {
            return ((ISidedInventory) inv).canInsertItem(slot, stack, side.ordinal());
        }
        return true;
    }

    public static ItemStack extractItem(IInventory inv, int slot, ForgeDirection side, long maxAmount,
        boolean simulate) {
        if (inv == null || slot < 0 || slot >= inv.getSizeInventory() || maxAmount <= 0) {
            return null;
        }
        ItemStack inSlot = inv.getStackInSlot(slot);
        if (inSlot == null || inSlot.stackSize <= 0) {
            return null;
        }
        if (!canExtract(inv, slot, inSlot, side)) {
            return null;
        }

        int toExtract = (int) Math.min(inSlot.stackSize, Math.min(maxAmount, inSlot.getMaxStackSize()));
        ItemStack extracted = inSlot.copy();
        extracted.stackSize = toExtract;

        if (!simulate) {
            extracted = inv.decrStackSize(slot, toExtract);
            if (extracted == null || extracted.stackSize <= 0) return null;
            inv.markDirty();
        }

        return extracted;
    }

    public static int insertItem(IInventory inv, int slot, ForgeDirection side, ItemStack stack, boolean simulate,
        boolean emptySlotsOnly) {
        if (inv == null || slot < 0 || slot >= inv.getSizeInventory() || stack == null || stack.stackSize <= 0) {
            return 0;
        }

        ItemStack inSlot = inv.getStackInSlot(slot);
        if (emptySlotsOnly && inSlot != null) {
            return 0;
        }

        if (!canInsert(inv, slot, stack, side)) {
            return 0;
        }

        int maxLimit = Math.min(inv.getInventoryStackLimit(), stack.getMaxStackSize());
        if (maxLimit <= 0) return 0;

        if (inSlot == null) {
            int toInsert = Math.min(stack.stackSize, maxLimit);
            if (!simulate) {
                ItemStack newStack = stack.copy();
                newStack.stackSize = toInsert;
                inv.setInventorySlotContents(slot, newStack);
                inv.markDirty();
            }
            return toInsert;
        } else {
            if (!areItemStacksEqual(inSlot, stack)) {
                return 0;
            }
            int space = maxLimit - inSlot.stackSize;
            if (space <= 0) return 0;

            int toInsert = Math.min(stack.stackSize, space);
            if (!simulate) {
                inSlot.stackSize += toInsert;
                inv.setInventorySlotContents(slot, inSlot);
                inv.markDirty();
            }
            return toInsert;
        }
    }

    public static boolean areItemStacksEqual(ItemStack a, ItemStack b) {
        if (a == null || b == null) return false;
        if (a.getItem() != b.getItem()) return false;
        if (a.getItemDamage() != b.getItemDamage()) return false;
        return ItemStack.areItemStackTagsEqual(a, b);
    }

    public static long countMatchingItems(World world, BlockPos pos, SideQualifier sides, NumberRangeSet slots,
        ResourceIdSet resourceIds, With with, ResourceIdSet except) {
        IInventory inv = getInventory(world, pos);
        return countMatchingItems(inv, sides, slots, resourceIds, with, except);
    }

    public static long countMatchingItems(IInventory inv, SideQualifier sides, NumberRangeSet slots,
        ResourceIdSet resourceIds, With with, ResourceIdSet except) {
        if (inv == null) return 0;

        long count = 0;
        boolean isSided = inv instanceof ISidedInventory;
        List<ForgeDirection> directions;
        if (!isSided) {
            // Unsided inventory: querying multiple faces accesses the same backing slots
            directions = Collections.singletonList(ForgeDirection.UNKNOWN);
        } else if (sides != null) {
            directions = sides.resolveDirections();
        } else {
            directions = Arrays.asList(ForgeDirection.VALID_DIRECTIONS);
        }

        Set<Integer> countedSlots = new HashSet<>();
        for (ForgeDirection side : directions) {
            int[] accessible = getAccessibleSlots(inv, side, slots);
            for (int slot : accessible) {
                if (!countedSlots.add(slot)) continue;
                ItemStack stack = inv.getStackInSlot(slot);
                if (stack == null) continue;
                if (except != null && except.matchesItem(stack)) continue;
                if (resourceIds != null && !resourceIds.matchesItem(stack)) continue;
                if (with != null && !with.matches(stack)) continue;
                count += stack.stackSize;
            }
        }
        return count;
    }

    public static long countMatchingFluids(World world, BlockPos pos, SideQualifier sides, ResourceIdSet resourceIds,
        ResourceIdSet except) {
        IFluidHandler handler = getFluidHandler(world, pos);
        return countMatchingFluids(handler, sides, resourceIds, except);
    }

    public static long countMatchingFluids(IFluidHandler handler, SideQualifier sides, ResourceIdSet resourceIds,
        ResourceIdSet except) {
        if (handler == null) return 0;

        long count = 0;
        for (net.minecraftforge.fluids.FluidStack stack : getVisibleFluids(handler, sides)) {
            if (except != null && except.matchesFluid(stack)) continue;
            if (resourceIds != null && !resourceIds.matchesFluid(stack)) continue;
            count += stack.amount;
        }
        return count;
    }

    /**
     * Prefer UNKNOWN when available, otherwise use the largest per-face total for each fluid.
     * Forge does not identify tanks across faces; summing snapshots would count shared tanks repeatedly.
     */
    public static List<net.minecraftforge.fluids.FluidStack> getVisibleFluids(IFluidHandler handler,
        SideQualifier sides) {
        java.util.Map<net.minecraftforge.fluids.Fluid, net.minecraftforge.fluids.FluidStack> maximum = new java.util.LinkedHashMap<>();
        List<ForgeDirection> directions = sides == null ? SideQualifier.DEFAULT.resolveDirections()
            : sides.resolveDirections();
        if (sides == null || sides == SideQualifier.DEFAULT) {
            FluidTankInfo[] unknown = handler.getTankInfo(ForgeDirection.UNKNOWN);
            if (unknown != null && unknown.length > 0) directions = Collections.singletonList(ForgeDirection.UNKNOWN);
        }
        for (ForgeDirection side : directions) {
            FluidTankInfo[] tanks = handler.getTankInfo(side);
            if (tanks == null) continue;
            java.util.Map<net.minecraftforge.fluids.Fluid, net.minecraftforge.fluids.FluidStack> perFace = new java.util.LinkedHashMap<>();
            for (FluidTankInfo tank : tanks) {
                if (tank == null || tank.fluid == null || tank.fluid.amount <= 0) continue;
                net.minecraftforge.fluids.Fluid fluid = tank.fluid.getFluid();
                net.minecraftforge.fluids.FluidStack total = perFace.get(fluid);
                if (total == null) perFace.put(fluid, tank.fluid.copy());
                else total.amount = (int) Math.min(Integer.MAX_VALUE, (long) total.amount + tank.fluid.amount);
            }
            for (java.util.Map.Entry<net.minecraftforge.fluids.Fluid, net.minecraftforge.fluids.FluidStack> entry : perFace
                .entrySet()) {
                net.minecraftforge.fluids.FluidStack prior = maximum.get(entry.getKey());
                if (prior == null || prior.amount < entry.getValue().amount)
                    maximum.put(entry.getKey(), entry.getValue());
            }
        }
        return new ArrayList<>(maximum.values());
    }
}
