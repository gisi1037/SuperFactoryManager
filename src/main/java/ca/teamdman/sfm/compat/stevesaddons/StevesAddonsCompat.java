package ca.teamdman.sfm.compat.stevesaddons;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.item.ModItems;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.util.BlockPos;
import cpw.mods.fml.common.registry.GameRegistry;

public final class StevesAddonsCompat {

    private StevesAddonsCompat() {}

    public static void onBlockTagged(World world, int x, int y, int z, String label) {
        try {
            stevesaddons.naming.NameRegistry.saveName(world, x, y, z, label);
        } catch (Throwable t) {
            SuperFactoryManager.LOG
                .warn("Failed to sync label to StevesAddons NameRegistry at {},{},{}: {}", x, y, z, t.getMessage());
        }
    }

    public static void onBlockUntagged(World world, int x, int y, int z) {
        try {
            stevesaddons.naming.NameRegistry.removeName(world, x, y, z);
        } catch (Throwable t) {
            SuperFactoryManager.LOG
                .warn("Failed to remove label from StevesAddons NameRegistry at {},{},{}: {}", x, y, z, t.getMessage());
        }
    }

    public static String getSavedName(World world, int x, int y, int z) {
        try {
            return stevesaddons.naming.NameRegistry.getSavedName(world, x, y, z);
        } catch (Throwable t) {
            return null;
        }
    }

    public static void syncAll(World world, LabelPositionHolder holder) {
        if (holder == null || world == null) return;
        try {
            for (Map.Entry<String, Set<BlockPos>> entry : holder.getAllLabels()
                .entrySet()) {
                String label = entry.getKey();
                for (BlockPos pos : entry.getValue()) {
                    stevesaddons.naming.NameRegistry.saveName(world, pos.x, pos.y, pos.z, label);
                }
            }
        } catch (Throwable t) {
            SuperFactoryManager.LOG
                .warn("Failed to batch sync labels to StevesAddons NameRegistry: {}", t.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static void integrateRecipes() {
        try {
            Item stevesLabeler = stevesaddons.registry.ItemRegistry.labeler;
            if (stevesLabeler == null) return;

            // Remove StevesAddons labeler recipe so it's not crafted
            List<IRecipe> recipes = (List<IRecipe>) CraftingManager.getInstance()
                .getRecipeList();
            Iterator<IRecipe> it = recipes.iterator();
            while (it.hasNext()) {
                IRecipe recipe = it.next();
                ItemStack out = recipe.getRecipeOutput();
                if (out != null && out.getItem() == stevesLabeler) {
                    it.remove();
                }
            }

            // Provide 1:1 conversion: StevesAddons Labeler -> SuperFM Label Gun
            GameRegistry.addShapelessRecipe(
                new ItemStack(ModItems.labelGun),
                new ItemStack(stevesLabeler, 1, OreDictionary.WILDCARD_VALUE));

            // Replace original recipe pattern to yield SuperFM Label Gun instead
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    new ItemStack(ModItems.labelGun),
                    "ppp",
                    " i ",
                    "rxr",
                    'p',
                    new ItemStack(Items.paper),
                    'i',
                    "dyeBlack",
                    'r',
                    "dustRedstone",
                    'x',
                    new ItemStack(Blocks.piston)));

            SuperFactoryManager.LOG
                .info("Successfully integrated StevesAddons Labeler recipe replacement & conversion.");
        } catch (Throwable t) {
            SuperFactoryManager.LOG.warn("Failed to integrate StevesAddons recipe replacements: {}", t.getMessage());
        }
    }
}
