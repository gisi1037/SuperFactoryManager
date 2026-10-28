package ca.teamdman.sfm.compat.stevefm;

import net.minecraft.item.ItemStack;

import ca.teamdman.sfm.common.block.ModBlocks;
import cpw.mods.fml.common.registry.GameRegistry;

/** Loaded only when legacy SteveFM is present; its block types must not enter standalone recipe verification. */
public final class SteveFMRecipes {

    private SteveFMRecipes() {}

    public static void registerCableConversions() {
        try {
            if (vswe.stevesfactory.blocks.ModBlocks.blockCable != null) {
                // Leftover SuperFM cable -> SteveFM cable (one-way cleanup)
                GameRegistry.addShapelessRecipe(
                    new ItemStack(vswe.stevesfactory.blocks.ModBlocks.blockCable),
                    new ItemStack(ModBlocks.cable));

                // SteveFM cable <-> Fancy cable (both directions)
                GameRegistry.addShapelessRecipe(
                    new ItemStack(ModBlocks.fancyCable),
                    new ItemStack(vswe.stevesfactory.blocks.ModBlocks.blockCable));
                GameRegistry.addShapelessRecipe(
                    new ItemStack(vswe.stevesfactory.blocks.ModBlocks.blockCable),
                    new ItemStack(ModBlocks.fancyCable));
            }
        } catch (Throwable ignored) {}
    }
}
