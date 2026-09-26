package ca.teamdman.sfm.common.inventory;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfml.ast.NumberRangeSet;
import ca.teamdman.sfml.ast.ResourceIdSet;
import ca.teamdman.sfml.ast.SideQualifier;
import ca.teamdman.sfml.ast.With;

public final class InventoryUtils {

    private InventoryUtils() {}

    public static IInventory getInventory(World world, BlockPos pos) {
        if (world == null || pos == null) return null;
        TileEntity te = pos.getTileEntity(world);
        return te instanceof IInventory ? (IInventory) te : null;
    }

    public static IFluidHandler getFluidHandler(World world, BlockPos pos) {
        if (world == null || pos == null) return null;
        TileEntity te = pos.getTileEntity(world);
        return te instanceof IFluidHandler ? (IFluidHandler) te : null;
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

        if (slotFilter == null || slotFilter == NumberRangeSet.ALL) {
            return rawSlots;
        }

        List<Integer> filtered = new ArrayList<>(rawSlots.length);
        for (int slot : rawSlots) {
            if (slotFilter.contains(slot)) {
                filtered.add(slot);
            }
        }
        int[] result = new int[filtered.size()];
        for (int i = 0; i < filtered.size(); i++) {
            result[i] = filtered.get(i);
        }
        return result;
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
            if (inSlot.stackSize <= toExtract) {
                inv.setInventorySlotContents(slot, null);
            } else {
                inSlot.stackSize -= toExtract;
                inv.setInventorySlotContents(slot, inSlot);
            }
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
        if (inv == null) return 0;

        long count = 0;
        List<ForgeDirection> directions = sides != null ? sides.resolveDirections() : null;
        if (directions == null || directions.isEmpty()) {
            directions = java.util.Collections.singletonList(ForgeDirection.UNKNOWN);
        }

        for (ForgeDirection side : directions) {
            int[] accessible = getAccessibleSlots(inv, side, slots);
            for (int slot : accessible) {
                ItemStack stack = inv.getStackInSlot(slot);
                if (stack == null) continue;
                if (except != null && except.matchesItem(stack)) continue;
                if (resourceIds.matchesItem(stack) && with.matches(stack)) {
                    count += stack.stackSize;
                }
            }
        }
        return count;
    }

    public static long countMatchingFluids(World world, BlockPos pos, SideQualifier sides, ResourceIdSet resourceIds,
        ResourceIdSet except) {
        IFluidHandler handler = getFluidHandler(world, pos);
        if (handler == null) return 0;

        long count = 0;
        List<ForgeDirection> directions = sides != null ? sides.resolveDirections() : null;
        if (directions == null || directions.isEmpty()) {
            directions = java.util.Collections.singletonList(ForgeDirection.UNKNOWN);
        }

        for (ForgeDirection side : directions) {
            FluidTankInfo[] tanks = handler.getTankInfo(side);
            if (tanks == null) continue;
            for (FluidTankInfo tank : tanks) {
                if (tank.fluid == null) continue;
                if (except != null && except.matchesFluid(tank.fluid)) continue;
                if (resourceIds.matchesFluid(tank.fluid)) {
                    count += tank.fluid.amount;
                }
            }
        }
        return count;
    }
}
