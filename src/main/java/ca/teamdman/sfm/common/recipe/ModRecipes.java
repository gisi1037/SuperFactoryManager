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
        // One peripheral detects inputs and emits independent signals on all six faces.
        GameRegistry.addRecipe(
            new ItemStack(ModBlocks.redstonePort),
            "IRI",
            "RCR",
            "IRI",
            'I',
            Items.iron_ingot,
            'R',
            Items.redstone,
            'C',
            ModBlocks.cable);
        // The station authors world-owned projects; it is separate from execution managers.
        GameRegistry.addRecipe(
            new ItemStack(ModBlocks.programmingStation),
            "IRI",
            "BCB",
            "III",
            'I',
            Items.iron_ingot,
            'R',
            Items.redstone,
            'B',
            Items.book,
            'C',
            Blocks.crafting_table);
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

        if (!Compat.isSFMLoaded()) {
            // Inventory Cables (yields 8) - only register if Steve's Factory Manager is not present!
            GameRegistry.addRecipe(
                new ItemStack(ModBlocks.cable, 8),
                " B ",
                "BRB",
                " B ",
                'B',
                Blocks.iron_bars,
                'R',
                Items.redstone);

            // In standalone mode, allow 1:1 conversion between regular cable and fancy cable
            GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.fancyCable), new ItemStack(ModBlocks.cable));
            GameRegistry.addShapelessRecipe(new ItemStack(ModBlocks.cable), new ItemStack(ModBlocks.fancyCable));
        } else {
            ca.teamdman.sfm.compat.stevefm.SteveFMRecipes.registerCableConversions();
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
