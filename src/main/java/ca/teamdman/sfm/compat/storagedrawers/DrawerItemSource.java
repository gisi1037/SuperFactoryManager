package ca.teamdman.sfm.compat.storagedrawers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BooleanSupplier;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.QueryBudget;

/** Storage Drawers exposes virtual stacks; its backing count, not the displayed stack, proves extraction. */
public final class DrawerItemSource {

    private DrawerItemSource() {}

    public static boolean handles(IInventory inventory) {
        String name = inventory.getClass()
            .getName();
        return name.equals("com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawersStandard")
            || name.equals("com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityDrawersComp")
            || name.equals("com.jaquadro.minecraft.storagedrawers.block.tile.TileEntityController")
            || name.equals("com.jaquadro.minecraft.storagedrawers.block.tile.TileEntitySlave");
    }

    private static final class Holder {

        static final Api API = load();

        private static Api load() {
            try {
                ClassLoader loader = DrawerItemSource.class.getClassLoader();
                String root = "com.jaquadro.minecraft.storagedrawers.api.";
                return new Api(
                    Class.forName(root + "storage.IDrawerGroup", false, loader),
                    Class.forName(root + "inventory.IDrawerInventory", false, loader),
                    Class.forName(root + "storage.IDrawer", false, loader),
                    Class.forName(root + "inventory.SlotType", false, loader));
            } catch (ReflectiveOperationException | LinkageError unavailable) {
                return null;
            }
        }
    }

    /** Called only for a recognized native tile and before any source mutation. */
    public static Observation observe(IInventory inventory, int slot, ItemIdentity key, QueryBudget budget) {
        if (!handles(inventory)) return null;
        if (inventory.getClass()
            .getName()
            .endsWith(".TileEntityDrawersComp"))
            throw new IllegalArgumentException(
                "Compacting drawer extraction requires qualified pooled-count handling; use a standard drawer source");
        if (!inventory.getClass()
            .getName()
            .endsWith(".TileEntityDrawersStandard"))
            throw new IllegalArgumentException(
                "Drawer controller source mapping is not qualified; connect to a standard drawer source directly");
        Api api = Holder.API;
        if (api == null) throw new IllegalArgumentException("Storage Drawers item source API unavailable");
        return api.observe(inventory, slot, key, budget);
    }

    // Public APIs only. Package-local binding permits structural tests without fake optional-mod classes.
    static final class Api {

        final Class<?> group;
        final Method inventory, drawers, enabled, drawer, drawerSlot, slotType, stored, count, vending, capacity,
            setCount, setStored;
        final Object output;

        Api(Class<?> group, Class<?> inventory, Class<?> drawer, Class<?> slotType)
            throws ReflectiveOperationException {
            this.group = group;
            this.inventory = group.getMethod("getDrawerInventory");
            drawers = group.getMethod("getDrawerCount");
            enabled = group.getMethod("isDrawerEnabled", int.class);
            this.drawer = group.getMethod("getDrawer", int.class);
            drawerSlot = inventory.getMethod("getDrawerSlot", int.class);
            this.slotType = inventory.getMethod("getInventorySlotType", int.class);
            output = slotType.getField("OUTPUT")
                .get(null);
            stored = drawer.getMethod("getStoredItemPrototype");
            count = drawer.getMethod("getStoredItemCount");
            vending = drawer.getMethod("isVendingUnlimited");
            capacity = drawer.getMethod("getMaxCapacity", ItemStack.class);
            setCount = drawer.getMethod("setStoredItemCount", int.class);
            setStored = drawer.getMethod("setStoredItemRedir", ItemStack.class, int.class);
        }

        Observation observe(IInventory source, int slot, ItemIdentity key, QueryBudget budget) {
            if (!group.isInstance(source)) return null;
            Object inv = call(inventory, source, budget);
            int width = (Integer) call(drawers, source, budget);
            if (inv == null || width <= 0 || width > 64 || call(slotType, inv, budget, slot) != output) return null;
            int index = (Integer) call(drawerSlot, inv, budget, slot);
            if (index < 0 || index >= width || !(Boolean) call(enabled, source, budget, index)) return null;
            Object selected = call(drawer, source, budget, index);
            if (selected == null) return null;
            int amount = (Integer) call(count, selected, budget);
            boolean infinite = (Boolean) call(vending, selected, budget);
            ItemStack prototype = (ItemStack) call(stored, selected, budget);
            if (amount <= 0 || !matches(key, prototype, budget)) return null;
            budget.chargeAllocation(1);
            Observation observation = new Observation(this, source, inv, slot, index, selected, key, amount, infinite);
            return observation.current(budget) && call(stored, selected, budget) == prototype
                && (Integer) call(count, selected, budget) == amount
                && (Boolean) call(vending, selected, budget) == infinite ? observation : null;
        }
    }

    public static final class Observation {

        private final Api api;
        private final IInventory source;
        private final Object inventory, drawer;
        private final int slot, index, before;
        private final ItemIdentity key;
        private final boolean infinite;

        private Observation(Api api, IInventory source, Object inventory, int slot, int index, Object drawer,
            ItemIdentity key, int before, boolean infinite) {
            this.api = api;
            this.source = source;
            this.inventory = inventory;
            this.slot = slot;
            this.index = index;
            this.drawer = drawer;
            this.key = key;
            this.before = before;
            this.infinite = infinite;
        }

        private boolean current(QueryBudget budget) {
            return call(api.inventory, source, budget) == inventory
                && (Integer) call(api.drawers, source, budget) > index
                && call(api.slotType, inventory, budget, slot) == api.output
                && (Integer) call(api.drawerSlot, inventory, budget, slot) == index
                && (Boolean) call(api.enabled, source, budget, index)
                && call(api.drawer, source, budget, index) == drawer;
        }

        public boolean acceptsExtraction(int extracted, QueryBudget budget) {
            if (extracted <= 0 || !current(budget) || (Boolean) call(api.vending, drawer, budget) != infinite)
                return false;
            long expectedRemaining = infinite ? before : (long) before - extracted;
            ItemStack prototype = (ItemStack) call(api.stored, drawer, budget);
            if (prototype == null ? expectedRemaining != 0 : !matches(key, prototype, budget)) return false;
            // Item metadata/NBT comparisons can call item code. Check backing state after the last comparison.
            return current(budget) && (Boolean) call(api.vending, drawer, budget) == infinite
                && (Integer) call(api.count, drawer, budget) == expectedRemaining
                && call(api.stored, drawer, budget) == prototype;
        }

        /** Known rollback. A failed preflight writes nothing; failures after the setter remain uncertain. */
        public boolean restore(ItemStack remainder, QueryBudget budget, BooleanSupplier physicallyPresent) {
            final int total;
            final boolean empty;
            final ItemStack copy;
            try {
                if (remainder == null || remainder.stackSize <= 0) return false;
                if (!current(budget) || (Boolean) call(api.vending, drawer, budget) != infinite) return false;
                int amount = (Integer) call(api.count, drawer, budget);
                ItemStack prototype = (ItemStack) call(api.stored, drawer, budget);
                if (amount < 0 || prototype == null && amount != 0
                    || prototype != null && !matches(key, prototype, budget)
                    || !matches(key, remainder, budget)) return false;
                if (infinite) return prototype != null && current(budget)
                    && (Boolean) call(api.vending, drawer, budget)
                    && (Integer) call(api.count, drawer, budget) == before
                    && call(api.stored, drawer, budget) == prototype
                    && physicallyPresent.getAsBoolean();
                long next = (long) amount + remainder.stackSize;
                if (next > Integer.MAX_VALUE) return false;
                total = (int) next;
                empty = prototype == null;
                budget.chargeSlots(key.getCopyWork());
                budget.chargeAllocation(key.getCopyWork() * 2 + 4);
                copy = remainder.copy();
                int offered = copy.stackSize;
                if (total > (Integer) call(api.capacity, drawer, budget, copy) || copy.stackSize != offered
                    || !matches(key, copy, budget)) return false;
                ItemStack latest = (ItemStack) call(api.stored, drawer, budget);
                if ((latest == null) != empty || latest != null && !matches(key, latest, budget)) return false;
                if (!current(budget) || (Boolean) call(api.vending, drawer, budget) != infinite
                    || (Integer) call(api.count, drawer, budget) != amount
                    || call(api.stored, drawer, budget) != latest) return false;
                if (!physicallyPresent.getAsBoolean()) return false;
            } catch (RuntimeException refused) {
                return false;
            }
            // No provider reads between the caller's last physical check and the native setter.
            if (empty) call(api.setStored, drawer, budget, copy, total);
            else call(api.setCount, drawer, budget, total);
            budget.chargeProvider();
            source.markDirty();
            ItemStack restored = (ItemStack) call(api.stored, drawer, budget);
            if (!matches(key, restored, budget) || !current(budget)
                || (Boolean) call(api.vending, drawer, budget) != infinite
                || (Integer) call(api.count, drawer, budget) != total
                || call(api.stored, drawer, budget) != restored
                || !physicallyPresent.getAsBoolean())
                throw new IllegalStateException("Storage Drawers rollback could not be confirmed");
            return true;
        }
    }

    private static boolean matches(ItemIdentity key, ItemStack stack, QueryBudget budget) {
        budget.chargeProviders(3);
        return key.matchesStack(stack, budget);
    }

    private static Object call(Method method, Object owner, QueryBudget budget, Object... arguments) {
        budget.chargeProvider();
        budget.chargeAllocation(1 + arguments.length);
        try {
            return method.invoke(owner, arguments);
        } catch (InvocationTargetException failure) {
            throw new IllegalArgumentException("Storage Drawers source callback failed", failure.getCause());
        } catch (ReflectiveOperationException failure) {
            throw new IllegalArgumentException("Storage Drawers source API unavailable", failure);
        }
    }
}
