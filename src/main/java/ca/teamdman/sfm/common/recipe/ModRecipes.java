package ca.teamdman.sfm.common.recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.block.ModBlocks;
import ca.teamdman.sfm.common.item.ModItems;
import ca.teamdman.sfm.compat.Compat;
import ca.teamdman.sfm.compat.gregtech.GtnhRecipeParts;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;

public final class ModRecipes {

    private ModRecipes() {}

    /** Register after pack recipe replacement so reverse legacy outputs survive. */
    public static void loadComplete() {
        net.minecraftforge.oredict.RecipeSorter.register(
            SuperFactoryManager.MODID + ":plain_conversion",
            PlainConversionRecipe.class,
            net.minecraftforge.oredict.RecipeSorter.Category.SHAPELESS,
            "after:minecraft:shapeless");
        net.minecraftforge.oredict.RecipeSorter.register(
            SuperFactoryManager.MODID + ":plain_shaped",
            PlainShapedOreRecipe.class,
            net.minecraftforge.oredict.RecipeSorter.Category.SHAPED,
            "after:forge:shapedore");
        ItemStack legacyCable = Compat.isSFMLoaded() ? ca.teamdman.sfm.compat.stevefm.SteveFMRecipes.cable() : null;
        for (IRecipe recipe : cableRecipes(
            new ItemStack(ModBlocks.cable),
            new ItemStack(ModBlocks.fancyCable),
            legacyCable)) GameRegistry.addRecipe(recipe);
        if (Compat.isSFMLoaded()) ca.teamdman.sfm.compat.stevefm.SteveFMRecipes.registerConversions();
        if (Compat.isStevesAddonsLoaded()) ca.teamdman.sfm.compat.stevesaddons.StevesAddonsCompat.integrateRecipes();
        if (!Loader.isModLoaded("gregtech") && !Loader.isModLoaded("gregtech_nh")) {
            SuperFactoryManager.LOG
                .info("GT absent: keeping conversions; late-HV direct recipes require GregTech components.");
            return;
        }
        try {
            GtnhRecipeParts parts = new GtnhRecipeParts();
            Map<String, ItemStack> outputs = new LinkedHashMap<>();
            outputs.put("manager", new ItemStack(ModBlocks.manager));
            outputs.put("cable", new ItemStack(ModBlocks.cable));
            outputs.put("disk", new ItemStack(ModItems.disk));
            outputs.put("labelgun", new ItemStack(ModItems.labelGun));
            outputs.put("programmingStation", new ItemStack(ModBlocks.programmingStation));
            outputs.put("redstonePort", new ItemStack(ModBlocks.redstonePort));
            for (IRecipe recipe : LateHvRecipes.build(outputs, parts::get)) GameRegistry.addRecipe(recipe);
        } catch (ReflectiveOperationException | IllegalArgumentException | LinkageError failure) {
            SuperFactoryManager.LOG
                .error("Late-HV SFM recipes could not be registered; no vanilla fallback was added", failure);
        }
    }

    public static void convert(ItemStack first, ItemStack second) {
        GameRegistry.addRecipe(new PlainConversionRecipe(first, second));
        GameRegistry.addRecipe(new PlainConversionRecipe(second, first));
    }

    /** A one-item swap stays unambiguous: put a full cable left for legacy, right for fancy. */
    public static List<IRecipe> cableRecipes(ItemStack full, ItemStack fancy, ItemStack legacy) {
        List<IRecipe> recipes = new ArrayList<>();
        recipes.add(new PlainShapedOreRecipe(fancy, "  C", 'C', full).setMirrored(false));
        recipes.add(new PlainConversionRecipe(fancy, full));
        if (legacy != null) {
            recipes.add(new PlainConversionRecipe(legacy, full));
            recipes.add(new PlainShapedOreRecipe(legacy, "C  ", 'C', full).setMirrored(false));
        }
        return recipes;
    }
}
