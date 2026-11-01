package ca.teamdman.sfm.common.recipe;

import java.util.Collections;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.world.World;

/** NEI-visible 1:1 conversion; never implicitly erase a program, label table or other NBT. */
public final class PlainConversionRecipe extends ShapelessRecipes {

    public PlainConversionRecipe(ItemStack input, ItemStack output) {
        super(require(output).copy(), Collections.singletonList(require(input).copy()));
        if (input.stackSize != 1 || output.stackSize != 1)
            throw new IllegalArgumentException("Conversions require one input and output");
    }

    private static ItemStack require(ItemStack stack) {
        if (stack == null || stack.getItem() == null
            || stack.getItemDamage() < 0
            || stack.getItemDamage() == net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE
            || !plain(stack)) throw new IllegalArgumentException("Conversion requires an exact plain item");
        return stack;
    }

    static boolean plain(ItemStack stack) {
        return stack == null || stack.getTagCompound() == null
            || stack.getTagCompound()
                .hasNoTags();
    }

    static boolean plainGrid(InventoryCrafting grid) {
        for (int i = 0; i < grid.getSizeInventory(); i++) {
            ItemStack stack = grid.getStackInSlot(i);
            if (stack != null && (stack.stackSize <= 0 || !plain(stack))) return false;
        }
        return true;
    }

    @Override
    public boolean matches(InventoryCrafting grid, World world) {
        return plainGrid(grid) && super.matches(grid, world);
    }
}
