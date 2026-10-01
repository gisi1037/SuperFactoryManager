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
            // When Steve's Factory Manager is present:
            // 1) Allow players with leftover SuperFM regular cables to convert them to SteveFM cables
            // 2) Allow converting SteveFM cables <-> Fancy cables in both directions
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
