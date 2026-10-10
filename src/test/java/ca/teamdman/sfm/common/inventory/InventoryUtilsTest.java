package ca.teamdman.sfm.common.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.util.Collections;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfml.ast.NumberRangeSet;
import ca.teamdman.sfml.ast.ResourceIdSet;
import ca.teamdman.sfml.ast.SideQualifier;
import ca.teamdman.sfml.ast.With;

public class InventoryUtilsTest {

    static class SidedInventoryWrapper implements ISidedInventory {

        private final InventoryBasic delegate;
        private final int[] accessibleSlots;

        SidedInventoryWrapper(InventoryBasic delegate, int[] accessibleSlots) {
            this.delegate = delegate;
            this.accessibleSlots = accessibleSlots;
        }

        @Override
        public int[] getAccessibleSlotsFromSide(int side) {
            return accessibleSlots;
        }

        @Override
        public boolean canInsertItem(int slot, ItemStack item, int side) {
            return true;
        }

        @Override
        public boolean canExtractItem(int slot, ItemStack item, int side) {
            return true;
        }

        @Override
        public int getSizeInventory() {
            return delegate.getSizeInventory();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return delegate.getStackInSlot(slot);
        }

        @Override
        public ItemStack decrStackSize(int slot, int amount) {
            return delegate.decrStackSize(slot, amount);
        }

        @Override
        public ItemStack getStackInSlotOnClosing(int slot) {
            return delegate.getStackInSlotOnClosing(slot);
        }

        @Override
        public void setInventorySlotContents(int slot, ItemStack stack) {
            delegate.setInventorySlotContents(slot, stack);
        }

        @Override
        public String getInventoryName() {
            return delegate.getInventoryName();
        }

        @Override
        public boolean hasCustomInventoryName() {
            return delegate.hasCustomInventoryName();
        }

        @Override
        public int getInventoryStackLimit() {
            return delegate.getInventoryStackLimit();
        }

        @Override
        public void markDirty() {
            delegate.markDirty();
        }

        @Override
        public boolean isUseableByPlayer(EntityPlayer player) {
            return true;
        }

        @Override
        public void openInventory() {}

        @Override
        public void closeInventory() {}

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack) {
            return true;
        }
    }

    @Test
    public void testUnsidedInventoryNotMultiCounted() {
        // Standard vanilla chest / unsided inventory with 40 items
        Item dummyItem = new Item();
        InventoryBasic inv = new InventoryBasic("Chest", false, 9);
        inv.setInventorySlotContents(0, new ItemStack(dummyItem, 40));

        // When side is unqualified (SideQualifier.DEFAULT), it must NOT count 6 times (40 * 6 = 240)
        long count = InventoryUtils.countMatchingItems(
            inv,
            SideQualifier.DEFAULT,
            NumberRangeSet.ALL,
            ResourceIdSet.MATCH_ALL,
            With.ALWAYS_TRUE,
            null);

        assertEquals(40, count, "Unsided inventory with unqualified side should count exactly 40 items, not 240");
    }

    @Test
    public void testSidedInventoryOverlappingSlotsDeduplicated() {
        // Mock sided inventory where all faces expose slot 0
        Item dummyItem = new Item();
        InventoryBasic base = new InventoryBasic("Sided", false, 3);
        base.setInventorySlotContents(0, new ItemStack(dummyItem, 25));
        ISidedInventory sidedInv = new SidedInventoryWrapper(base, new int[] { 0 });

        // Unqualified query resolves to all 6 faces; slot 0 must only be counted once
        long count = InventoryUtils.countMatchingItems(
            sidedInv,
            SideQualifier.DEFAULT,
            NumberRangeSet.ALL,
            ResourceIdSet.MATCH_ALL,
            With.ALWAYS_TRUE,
            null);

        assertEquals(25, count, "Sided inventory with overlapping slot across faces should count slot 0 only once");
    }

    @Test
    public void testFluidHandlerUnqualifiedSideNotMultiCounted() throws Exception {
        Field fUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        fUnsafe.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) fUnsafe.get(null);

        FluidStack stack = (FluidStack) unsafe.allocateInstance(FluidStack.class);
        stack.amount = 5000;
        Fluid fluid = new Fluid("water");
        Field fDelegate = FluidStack.class.getDeclaredField("fluidDelegate");
        fDelegate.setAccessible(true);
        fDelegate.set(stack, new cpw.mods.fml.common.registry.RegistryDelegate<Fluid>() {

            @Override
            public Fluid get() {
                return fluid;
            }

            @Override
            public String name() {
                return "water";
            }

            @Override
            public Class<Fluid> type() {
                return Fluid.class;
            }
        });
        FluidTankInfo info = new FluidTankInfo(stack, 10000);

        IFluidHandler handler = new IFluidHandler() {

            @Override
            public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
                return 0;
            }

            @Override
            public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
                return null;
            }

            @Override
            public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
                return null;
            }

            @Override
            public boolean canFill(ForgeDirection from, Fluid f) {
                return true;
            }

            @Override
            public boolean canDrain(ForgeDirection from, Fluid f) {
                return true;
            }

            @Override
            public FluidTankInfo[] getTankInfo(ForgeDirection from) {
                return new FluidTankInfo[] { info };
            }
        };

        long count = InventoryUtils.countMatchingFluids(handler, SideQualifier.DEFAULT, ResourceIdSet.MATCH_ALL, null);
        assertEquals(5000, count, "Single-tank handler queried with default side should count 5000 mB, not 30000 mB");
    }

    @Test
    void testDoubleChestUnifiedAccess() {
        InventoryBasic chest1 = new InventoryBasic("chest1", false, 27);
        InventoryBasic chest2 = new InventoryBasic("chest2", false, 27);
        net.minecraft.inventory.InventoryLargeChest largeChest = new net.minecraft.inventory.InventoryLargeChest(
            "large",
            chest1,
            chest2);

        int[] slots = InventoryUtils.getAccessibleSlots(largeChest, ForgeDirection.UNKNOWN, NumberRangeSet.ALL);
        assertEquals(54, slots.length, "Double chest should expose 54 slots");

        int[] half1 = InventoryUtils.getAccessibleSlots(
            largeChest,
            ForgeDirection.UNKNOWN,
            new NumberRangeSet(Collections.singletonList(new ca.teamdman.sfml.ast.NumberRange(0, 26))));
        assertEquals(27, half1.length, "First half slot filter should return 27 slots");
        assertEquals(0, half1[0]);
        assertEquals(26, half1[26]);

        int[] half2 = InventoryUtils.getAccessibleSlots(
            largeChest,
            ForgeDirection.UNKNOWN,
            new NumberRangeSet(Collections.singletonList(new ca.teamdman.sfml.ast.NumberRange(27, 53))));
        assertEquals(27, half2.length, "Second half slot filter should return 27 slots");
        assertEquals(27, half2[0]);
        assertEquals(53, half2[26]);
    }
}
