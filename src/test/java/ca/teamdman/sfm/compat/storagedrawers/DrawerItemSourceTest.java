package ca.teamdman.sfm.compat.storagedrawers;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.RegistrySimple;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.QueryBudget;

class DrawerItemSourceTest {

    private static final CallbackItem ITEM = new CallbackItem();
    private static final ItemIdentity KEY = ItemIdentity.of("drawer_test:item", 0, null, new QueryBudget());

    static {
        try {
            Field field = RegistrySimple.class.getDeclaredField("registryObjects");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> names = (Map<String, Object>) field.get(Item.itemRegistry);
            names.put("drawer_test:item", ITEM);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    @Test
    void backingQuantityAndExplicitVendingModeDetermineExtractionNotVirtualStackSize() throws Exception {
        Source source = new Source();
        QueryBudget budget = new QueryBudget(16);
        DrawerItemSource.Observation read = api().observe(source, 1, KEY, budget);
        assertNotNull(read);
        assertFalse(DrawerItemSource.handles(source), "Structural fixtures never qualify production inventories");
        source.drawer.count = 99;
        assertTrue(read.acceptsExtraction(1, budget));
        source.drawer.count = 100;
        assertFalse(read.acceptsExtraction(1, budget), "Ordinary unchanged backing storage remains inconsistent");
        source.drawer.infinite = true;
        assertFalse(read.acceptsExtraction(1, budget), "Changing vending mode invalidates the original observation");
        read = api().observe(source, 1, KEY, budget);
        assertTrue(read.acceptsExtraction(1, budget));
        source.drawer.infinite = false;
        assertFalse(read.acceptsExtraction(1, budget));
    }

    @Test
    void capacityCallbacksCannotOverwriteConcurrentCountsTypesOrMapping() throws Exception {
        for (int mutation = 0; mutation < 4; mutation++) {
            Source source = new Source();
            QueryBudget budget = new QueryBudget(16);
            DrawerItemSource.Observation read = api().observe(source, 1, KEY, budget);
            source.drawer.count = 99;
            int change = mutation;
            source.drawer.onCapacity = () -> {
                if (change == 0) source.drawer.count++;
                if (change == 1) source.drawer.prototype = new ItemStack(ITEM, 1, 7);
                if (change == 2) source.mapping = new Mapping();
                if (change == 3) source.drawer.infinite = true;
            };
            ItemStack remainder = new ItemStack(ITEM);
            assertFalse(read.restore(remainder, budget, () -> true));
            assertEquals(0, source.drawer.writes, "Refusal must not overwrite callback changes");
            assertEquals(1, remainder.stackSize, "The caller still owns this known remainder");
            assertEquals(0, remainder.getItemDamage());
            if (change == 0) assertEquals(100, source.drawer.count);
            if (change == 1) assertEquals(7, source.drawer.prototype.getItemDamage());
        }
    }

    @Test
    void capacityInspectionCannotMutateTheOwnedRemainder() throws Exception {
        Source source = new Source();
        QueryBudget budget = new QueryBudget(16);
        DrawerItemSource.Observation read = api().observe(source, 1, KEY, budget);
        source.drawer.count = 99;
        source.drawer.mutateCapacityProbe = true;
        ItemStack remainder = new ItemStack(ITEM);
        assertFalse(read.restore(remainder, budget, () -> true));
        assertEquals(0, source.drawer.writes);
        assertEquals(1, remainder.stackSize);
        assertEquals(0, remainder.getItemDamage());
    }

    @Test
    void itemCallbacksAfterCapacityAndAfterSetterCannotEvadeBackingCountValidation() throws Exception {
        Source source = new Source();
        QueryBudget budget = new QueryBudget(16);
        DrawerItemSource.Observation read = api().observe(source, 1, KEY, budget);
        source.drawer.count = 99;
        source.drawer.onCapacity = () -> ITEM.arm(source.drawer.prototype, () -> source.drawer.count++);
        try {
            assertFalse(read.restore(new ItemStack(ITEM), budget, () -> true));
            assertEquals(0, source.drawer.writes);
            assertEquals(100, source.drawer.count);
            source.drawer.onCapacity = null;
            source.drawer.count = 99;
            source.drawer.afterWrite = () -> ITEM.arm(source.drawer.prototype, () -> source.drawer.count++);
            assertThrows(IllegalStateException.class, () -> read.restore(new ItemStack(ITEM), budget, () -> true));
            assertEquals(101, source.drawer.count);
            assertEquals(1, source.drawer.writes);
        } finally {
            ITEM.arm(null, null);
        }
    }

    @Test
    void ordinaryAndCreativeReturnsAreConfirmedAndPostWriteFaultsRemainUncertain() throws Exception {
        Source source = new Source();
        QueryBudget budget = new QueryBudget(16);
        DrawerItemSource.Observation read = api().observe(source, 1, KEY, budget);
        source.drawer.count = 99;
        assertTrue(read.restore(new ItemStack(ITEM), budget, () -> true));
        assertEquals(100, source.drawer.count);
        assertEquals(1, source.drawer.writes);
        source.drawer.infinite = true;
        read = api().observe(source, 1, KEY, budget);
        assertTrue(read.restore(new ItemStack(ITEM), budget, () -> true));
        assertEquals(
            1,
            source.drawer.writes,
            "Creative return discards only an explicitly unlimited source's remainder");
        source.drawer.infinite = false;
        DrawerItemSource.Observation failing = api().observe(source, 1, KEY, budget);
        source.drawer.count = 99;
        source.drawer.throwAfterWrite = true;
        assertThrows(IllegalArgumentException.class, () -> failing.restore(new ItemStack(ITEM), budget, () -> true));
        assertEquals(100, source.drawer.count);
    }

    private static DrawerItemSource.Api api() throws Exception {
        return new DrawerItemSource.Api(Source.class, Mapping.class, Drawer.class, Slots.class);
    }

    public enum Slots {
        OUTPUT
    }

    private static final class CallbackItem extends Item {

        ItemStack target;
        Runnable hook;

        void arm(ItemStack target, Runnable hook) {
            this.target = target;
            this.hook = hook;
        }

        @Override
        public int getDamage(ItemStack stack) {
            if (stack == target && hook != null) {
                Runnable callback = hook;
                hook = null;
                callback.run();
            }
            return super.getDamage(stack);
        }
    }

    public static final class Mapping {

        public int getDrawerSlot(int slot) {
            return 0;
        }

        public Slots getInventorySlotType(int slot) {
            return Slots.OUTPUT;
        }
    }

    public static final class Source extends TileEntityChest {

        Mapping mapping = new Mapping();
        final Drawer drawer = new Drawer();

        public Mapping getDrawerInventory() {
            return mapping;
        }

        public int getDrawerCount() {
            return 1;
        }

        public boolean isDrawerEnabled(int slot) {
            return true;
        }

        public Drawer getDrawer(int slot) {
            return drawer;
        }

        @Override
        public void markDirty() {}
    }

    public static final class Drawer {

        ItemStack prototype = new ItemStack(ITEM);
        int count = 100, writes;
        boolean infinite, mutateCapacityProbe, throwAfterWrite;
        Runnable onCapacity;
        Runnable afterWrite;

        public ItemStack getStoredItemPrototype() {
            return prototype;
        }

        public int getStoredItemCount() {
            return infinite ? Integer.MAX_VALUE : count;
        }

        public boolean isVendingUnlimited() {
            return infinite;
        }

        public int getMaxCapacity(ItemStack stack) {
            if (onCapacity != null) onCapacity.run();
            if (mutateCapacityProbe) {
                stack.stackSize = 999;
                stack.setItemDamage(7);
            }
            return 10000;
        }

        public void setStoredItemCount(int next) {
            writes++;
            count = next;
            if (afterWrite != null) afterWrite.run();
            if (throwAfterWrite) throw new IllegalStateException("after native write");
        }

        public Drawer setStoredItemRedir(ItemStack stack, int next) {
            prototype = stack;
            setStoredItemCount(next);
            return this;
        }
    }
}
