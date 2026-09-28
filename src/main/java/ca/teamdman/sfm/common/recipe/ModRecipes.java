package ca.teamdman.sfm.common.recipe;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import ca.teamdman.sfm.common.block.ModBlocks;
import ca.teamdman.sfm.common.item.ModItems;
import ca.teamdman.sfm.compat.Compat;
import ca.teamdman.sfm.compat.stevesaddons.StevesAddonsCompat;
import cpw.mods.fml.common.registry.GameRegistry;

public final class ModRecipes {

    public static void init() {
        // Manager Block
        GameRegistry.addRecipe(
            new ItemStack(ModBlocks.manager),
            "III",
            "IRI",
            "SPS",
            'I',
            Items.iron_ingot,
            'R',
            Blocks.redstone_block,
            'S',
            Blocks.stone,
            'P',
            Blocks.piston);

        // Inventory Cables (yields 8) - only register if Steve's Factory Manager is not present!
        if (!Compat.isSFMLoaded()) {
            GameRegistry.addRecipe(
                new ItemStack(ModBlocks.cable, 8),
                " B ",
                "BRB",
                " B ",
                'B',
                Blocks.iron_bars,
                'R',
                Items.redstone);
        }

        // Disk
        GameRegistry.addRecipe(
            new ItemStack(ModItems.disk),
            " I ",
            "IRI",
            " P ",
            'I',
            Items.iron_ingot,
            'R',
            Items.redstone,
            'P',
            Items.paper);

        // Label Gun
        GameRegistry.addRecipe(
            new ItemStack(ModItems.labelGun),
            "  I",
            " R ",
            "G  ",
            'I',
            Items.iron_ingot,
            'R',
            Items.redstone,
            'G',
            Items.gold_ingot);
    }

    public static void postInit() {
        if (Compat.isStevesAddonsLoaded()) {
            StevesAddonsCompat.integrateRecipes();
        }
    }
}
