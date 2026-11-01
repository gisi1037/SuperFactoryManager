package ca.teamdman.sfm.common.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;

/** Standard Forge/NEI recipe shape; refuse programmed data sticks and other data-bearing ingredients. */
public final class PlainShapedOreRecipe extends ShapedOreRecipe {

    public PlainShapedOreRecipe(ItemStack output, Object... recipe) {
        super(output.copy(), recipe);
    }

    @Override
    public boolean matches(InventoryCrafting grid, World world) {
        return PlainConversionRecipe.plainGrid(grid) && super.matches(grid, world);
    }
}
