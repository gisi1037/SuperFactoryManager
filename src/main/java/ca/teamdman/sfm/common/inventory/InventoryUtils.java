package ca.teamdman.sfm.common.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
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

    public static IInventory getInventory(World world, BlockPos pos) {
        if (world == null || pos == null) return null;
        TileEntity te = pos.getTileEntity(world);
        if (te instanceof TileEntityChest) {
            TileEntityChest chest = (TileEntityChest) te;
            chest.checkForAdjacentChests();
            if (chest.adjacentChestXNeg != null) {
                return new InventoryLargeChest("container.chestDouble", chest.adjacentChestXNeg, chest);
            }
            if (chest.adjacentChestXPos != null) {
                return new InventoryLargeChest("container.chestDouble", chest, chest.adjacentChestXPos);
            }
            if (chest.adjacentChestZNeg != null) {
                return new InventoryLargeChest("container.chestDouble", chest.adjacentChestZNeg, chest);
            }
            if (chest.adjacentChestZPos != null) {
                return new InventoryLargeChest("container.chestDouble", chest, chest.adjacentChestZPos);
            }
            return chest;
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
        if (chest == null) return null;
        chest.checkForAdjacentChests();
        if (chest.adjacentChestXNeg != null) return chest.adjacentChestXNeg;
        if (chest.adjacentChestXPos != null) return chest.adjacentChestXPos;
        if (chest.adjacentChestZNeg != null) return chest.adjacentChestZNeg;
        if (chest.adjacentChestZPos != null) return chest.adjacentChestZPos;
        return null;
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
            ItemStack decr = inv.decrStackSize(slot, toExtract);
            if (decr != null && decr.stackSize > 0) {
                extracted = decr;
            } else if (inSlot.stackSize <= toExtract) {
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
        List<ForgeDirection> directions;
        if (sides == null || sides.getSides()
            .isEmpty()
            || sides.getSides()
                .contains(ca.teamdman.sfml.ast.Side.NULL)) {
            // Unspecified side: check UNKNOWN first so single-tank handlers aren't counted 6 times
            FluidTankInfo[] unknownTanks = handler.getTankInfo(ForgeDirection.UNKNOWN);
            if (unknownTanks != null && unknownTanks.length > 0) {
                directions = Collections.singletonList(ForgeDirection.UNKNOWN);
            } else {
                directions = Arrays.asList(ForgeDirection.VALID_DIRECTIONS);
            }
        } else {
            directions = sides.resolveDirections();
        }

        Set<FluidTankInfo> seenTanks = Collections.newSetFromMap(new IdentityHashMap<>());
        for (ForgeDirection side : directions) {
            FluidTankInfo[] tanks = handler.getTankInfo(side);
            if (tanks == null) continue;
            for (FluidTankInfo tank : tanks) {
                if (tank == null || !seenTanks.add(tank)) continue;
                if (tank.fluid == null) continue;
                if (except != null && except.matchesFluid(tank.fluid)) continue;
                if (resourceIds != null && !resourceIds.matchesFluid(tank.fluid)) continue;
                count += tank.fluid.amount;
            }
        }
        return count;
    }
}
