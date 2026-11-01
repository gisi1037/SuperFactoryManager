package ca.teamdman.sfm.compat.stevefm;

import net.minecraft.item.ItemStack;

import ca.teamdman.sfm.common.block.ModBlocks;
import ca.teamdman.sfm.common.recipe.ModRecipes;

/** Loaded only with SteveFM. Each legacy cable has one unambiguous conversion output. */
public final class SteveFMRecipes {

    private SteveFMRecipes() {}

    public static void registerConversions() {
        if (vswe.stevesfactory.blocks.ModBlocks.blockManager != null) ModRecipes
            .convert(new ItemStack(vswe.stevesfactory.blocks.ModBlocks.blockManager), new ItemStack(ModBlocks.manager));
    }

    public static ItemStack cable() {
        return vswe.stevesfactory.blocks.ModBlocks.blockCable == null ? null
            : new ItemStack(vswe.stevesfactory.blocks.ModBlocks.blockCable);
    }
}
