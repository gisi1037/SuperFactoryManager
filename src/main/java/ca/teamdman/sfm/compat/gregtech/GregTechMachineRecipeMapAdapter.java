package ca.teamdman.sfm.compat.gregtech;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import net.minecraft.tileentity.TileEntity;

import ca.teamdman.sfm.common.recipe.MachineRecipeMapService.Access;
import ca.teamdman.sfml.data.MachineRecipeMaps;
import ca.teamdman.sfml.data.QueryBudget;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

/** Qualified stock single-block machines only. Public API reflection remains optional/common-only. */
public final class GregTechMachineRecipeMapAdapter {

    private GregTechMachineRecipeMapAdapter() {}

    private static final class Holder {

        static final Bridge API = load();

        private static Bridge load() {
            if (!Loader.isModLoaded("gregtech")) return null;
            ModContainer mod = Loader.instance()
                .getIndexedModList()
                .get("gregtech");
            String version = mod == null ? "" : mod.getVersion();
            if (!"5.09.54.250".equals(version) && !"5.09.54.265".equals(version)) return null;
            try {
                ClassLoader loader = GregTechMachineRecipeMapAdapter.class.getClassLoader();
                return new Bridge(
                    Class.forName("gregtech.api.metatileentity.BaseMetaTileEntity", false, loader),
                    Class.forName(
                        "gregtech.api.metatileentity.implementations.MTEBasicMachineWithRecipe",
                        false,
                        loader),
                    Class.forName("gregtech.api.interfaces.metatileentity.IMetaTileEntity", false, loader),
                    Class.forName("gregtech.api.recipe.RecipeMaps", false, loader),
                    Class.forName("gregtech.api.recipe.RecipeMap", false, loader),
                    Class.forName("gregtech.api.recipe.RecipeMapBackend", false, loader));
            } catch (ReflectiveOperationException | LinkageError unavailable) {
                return null;
            }
        }
    }

    public static Access find(TileEntity tile, QueryBudget budget) {
        Bridge api = Holder.API;
        return api == null ? null : api.find(tile, budget);
    }

    /** Binding seam tests native contracts without publishing classes in optional mods' packages. */
    static final class Bridge {

        final Class<?> host, basic, mapType, backendType;
        final Method meta, base, recipeMap, available, backend;
        final Field registry, mapName;
        final Field[] maps;

        Bridge(Class<?> host, Class<?> basic, Class<?> metaType, Class<?> maps, Class<?> mapType, Class<?> backendType)
            throws ReflectiveOperationException {
            this.host = host;
            this.basic = basic;
            this.mapType = mapType;
            this.backendType = backendType;
            meta = host.getMethod("getMetaTileEntity");
            base = metaType.getMethod("getBaseMetaTileEntity");
            recipeMap = basic.getMethod("getRecipeMap");
            available = basic.getMethod("getAvailableRecipeMaps");
            backend = mapType.getMethod("getBackend");
            registry = mapType.getField("ALL_RECIPE_MAPS");
            mapName = mapType.getField("unlocalizedName");
            this.maps = new Field[] { maps.getField("maceratorRecipes"), maps.getField("benderRecipes"),
                maps.getField("cutterRecipes") };
        }

        Access find(TileEntity tile, QueryBudget budget) {
            if (tile == null || tile.getClass() != host) return null;
            Object machine = invoke(meta, tile, budget);
            if (machine == null || machine.getClass() != basic || invoke(base, machine, budget) != tile) return null;
            Object map = invoke(recipeMap, machine, budget);
            String id = identify(map, budget);
            if (id == null) return null;
            budget.chargeAllocation(3);
            MachineRecipeMaps result = MachineRecipeMaps.matches(id);
            return new Access() {

                @Override
                public MachineRecipeMaps maps() {
                    return result;
                }

                @Override
                public boolean current(TileEntity actual, QueryBudget owner) {
                    if (actual != tile || invoke(meta, tile, owner) != machine
                        || invoke(base, machine, owner) != tile
                        || invoke(recipeMap, machine, owner) != map) return false;
                    owner.chargeAllocation(1);
                    Object choices = invoke(available, machine, owner);
                    owner.chargeProviders(3);
                    if (!(choices instanceof Collection) || ((Collection<?>) choices).size() != 1
                        || ((Collection<?>) choices).iterator()
                            .next() != map)
                        return false;
                    // These stock native methods have no world lookup. Recheck host/map after the last callback.
                    return id.equals(identify(map, owner)) && invoke(meta, tile, owner) == machine
                        && invoke(base, machine, owner) == tile
                        && invoke(recipeMap, machine, owner) == map;
                }
            };
        }

        private String identify(Object map, QueryBudget budget) {
            if (map == null || map.getClass() != mapType) return null;
            Object registered = read(registry, null, budget), name = read(mapName, map, budget);
            if (!(registered instanceof Map) || !(name instanceof String)
                || !MachineRecipeMaps.qualified((String) name)) return null;
            budget.chargeProvider();
            Object nativeBackend = invoke(backend, map, budget);
            if (((Map<?, ?>) registered).get(name) != map || nativeBackend == null
                || nativeBackend.getClass() != backendType) return null;
            for (Field field : maps) if (read(field, null, budget) == map) return (String) name;
            return null;
        }
    }

    private static Object read(Field field, Object target, QueryBudget budget) {
        budget.chargeProvider();
        try {
            return field.get(target);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalArgumentException("GT machine recipe map API failed", failure);
        }
    }

    private static Object invoke(Method method, Object target, QueryBudget budget) {
        budget.chargeProvider();
        try {
            return method.invoke(target);
        } catch (InvocationTargetException failure) {
            Throwable cause = failure.getCause();
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            if (cause instanceof Error) throw (Error) cause;
            throw new IllegalArgumentException("GT machine recipe map callback failed", cause);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalArgumentException("GT machine recipe map API failed", failure);
        }
    }
}
