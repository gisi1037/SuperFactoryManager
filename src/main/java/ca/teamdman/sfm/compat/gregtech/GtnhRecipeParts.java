package ca.teamdman.sfm.compat.gregtech;

import java.lang.reflect.Method;

import net.minecraft.item.ItemStack;

/** Optional GT classes never enter common verification; lookup occurs only during final recipe registration. */
public final class GtnhRecipeParts {

    private final Class<?> itemList;
    private final Method get, hasBeenSet;

    public GtnhRecipeParts() throws ReflectiveOperationException {
        itemList = Class.forName("gregtech.api.enums.ItemList");
        get = itemList.getMethod("get", long.class, Object[].class);
        hasBeenSet = itemList.getMethod("hasBeenSet");
    }

    public ItemStack get(String name) {
        try {
            Object entry = itemList.getField(name)
                .get(null);
            if (!Boolean.TRUE.equals(hasBeenSet.invoke(entry)))
                throw new IllegalArgumentException("GT component is unset: " + name);
            Object value = get.invoke(entry, 1L, new Object[0]);
            if (!(value instanceof ItemStack) || ((ItemStack) value).getItem() == null)
                throw new IllegalArgumentException("GT component unavailable: " + name);
            return ((ItemStack) value).copy();
        } catch (ReflectiveOperationException failure) {
            throw new IllegalArgumentException("Cannot resolve GT component " + name, failure);
        }
    }

    /** Pack items resolve by registry identity/metadata, without linking optional mod classes. */
    public static ItemStack packItem(String mod, String name, int metadata) {
        ItemStack stack = cpw.mods.fml.common.registry.GameRegistry.findItemStack(mod, name, 1);
        if (stack == null || stack.getItem() == null)
            throw new IllegalArgumentException("Missing cable recipe component: " + mod + ":" + name);
        stack = stack.copy();
        stack.stackSize = 1;
        stack.setItemDamage(metadata);
        return stack;
    }
}
