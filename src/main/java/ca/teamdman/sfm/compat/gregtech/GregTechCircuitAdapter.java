package ca.teamdman.sfm.compat.gregtech;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;

import ca.teamdman.sfm.common.program.GhostCircuitAccess;
import ca.teamdman.sfml.data.QueryBudget;

/** Optional GTNH adapter. Use the same ghost handler as GregTech's MUI2 circuit selector. */
public final class GregTechCircuitAdapter {

    private GregTechCircuitAdapter() {}

    private static final class Holder {

        static final Bridge API = load();

        private static Bridge load() {
            try {
                return new Bridge();
            } catch (ReflectiveOperationException | LinkageError unavailable) {
                return null;
            }
        }
    }

    public static GhostCircuitAccess find(TileEntity tile, QueryBudget budget) {
        Bridge api = Holder.API;
        if (api == null || !api.host.isInstance(tile)) return null;
        Object meta = invoke(api.meta, tile, budget);
        if (meta == null || !api.support.isInstance(meta) || !((Boolean) invoke(api.allowed, meta, budget)))
            return null;
        int slot = (Integer) invoke(api.slot, meta, budget);
        Object inventory = invoke(api.inventory, meta, budget);
        if (!(inventory instanceof IItemHandlerModifiable)) return null;
        IItemHandlerModifiable items = (IItemHandlerModifiable) inventory;
        budget.chargeProvider();
        if (slot < 0 || slot >= items.getSlots()) return null;
        budget.chargeAllocation(3);
        Object ghost;
        try {
            ghost = api.ghost.newInstance(meta);
        } catch (InvocationTargetException failure) {
            throw callback(failure.getCause());
        } catch (ReflectiveOperationException failure) {
            throw new IllegalArgumentException("GT ghost circuit adapter unavailable", failure);
        }
        Native access = new Native(api, tile, meta, items, slot, ghost);
        return access.current(tile, budget) ? access : null;
    }

    private static final class Bridge {

        final Class<?> host, support, integrated;
        final Method meta, allowed, slot, inventory, read, write, modified;
        final Constructor<?> ghost;

        Bridge() throws ReflectiveOperationException {
            ClassLoader loader = GregTechCircuitAdapter.class.getClassLoader();
            host = Class.forName("gregtech.api.interfaces.tileentity.IGregTechTileEntity", false, loader);
            support = Class.forName("gregtech.api.interfaces.IConfigurationCircuitSupport", false, loader);
            Class<?> metaType = Class.forName("gregtech.api.interfaces.metatileentity.IMetaTileEntity", false, loader);
            Class<?> ghostType = Class.forName("gregtech.api.util.item.GhostCircuitItemStackHandler", false, loader);
            Class<?> inventoryType = Class.forName("gregtech.api.interfaces.tileentity.IHasInventory", false, loader);
            integrated = Class.forName("gregtech.common.items.ItemIntegratedCircuit", false, loader);
            meta = host.getMethod("getMetaTileEntity");
            allowed = support.getMethod("allowSelectCircuit");
            slot = support.getMethod("getCircuitSlot");
            inventory = metaType.getMethod("getInventoryHandler");
            ghost = ghostType.getConstructor(metaType);
            read = ghostType.getMethod("getCircuitConfig");
            write = ghostType.getMethod("setCircuitConfig", int.class);
            modified = inventoryType.getMethod("markInventoryBeenModified");
        }
    }

    private static final class Native implements GhostCircuitAccess {

        final Bridge api;
        final TileEntity host;
        final Object meta, ghost;
        final IItemHandlerModifiable inventory;
        final int slot;

        Native(Bridge api, TileEntity host, Object meta, IItemHandlerModifiable inventory, int slot, Object ghost) {
            this.api = api;
            this.host = host;
            this.meta = meta;
            this.inventory = inventory;
            this.slot = slot;
            this.ghost = ghost;
        }

        @Override
        public boolean current(TileEntity tile, QueryBudget budget) {
            if (tile != host || invoke(api.meta, host, budget) != meta
                || !((Boolean) invoke(api.allowed, meta, budget))
                || (Integer) invoke(api.slot, meta, budget) != slot
                || invoke(api.inventory, meta, budget) != inventory) return false;
            budget.chargeProvider();
            if (slot < 0 || slot >= inventory.getSlots()) return false;
            budget.chargeProvider();
            ItemStack selected = inventory.getStackInSlot(slot);
            if (selected != null && (selected.stackSize != 0 || !api.integrated.isInstance(selected.getItem())))
                throw new IllegalArgumentException(
                    "Refusing to replace a physical item in the configured circuit slot");
            return true;
        }

        @Override
        public int get(QueryBudget budget) {
            return (Integer) invoke(api.read, ghost, budget);
        }

        @Override
        public void set(int configuration, QueryBudget budget) {
            invoke(api.write, ghost, budget, configuration);
            invoke(api.modified, host, budget);
            host.markDirty();
        }
    }

    private static Object invoke(Method method, Object target, QueryBudget budget, Object... arguments) {
        budget.chargeProvider();
        try {
            return method.invoke(target, arguments);
        } catch (InvocationTargetException failure) {
            throw callback(failure.getCause());
        } catch (ReflectiveOperationException failure) {
            throw new IllegalArgumentException("GT ghost circuit adapter failed", failure);
        }
    }

    private static RuntimeException callback(Throwable failure) {
        if (failure instanceof RuntimeException) return (RuntimeException) failure;
        if (failure instanceof Error) throw (Error) failure;
        return new IllegalArgumentException("GT ghost circuit callback failed", failure);
    }
}
