package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import ca.teamdman.sfm.common.recipe.LateHvRecipes;
import ca.teamdman.sfm.common.recipe.PlainConversionRecipe;
import ca.teamdman.sfm.common.recipe.PlainShapedOreRecipe;

/** Real Forge recipes inside the existing isolated registry harness. */
public final class RecipeCraftingFixture {

    public static void cableChoices() {
        ItemStack full = new ItemStack(item()), fancy = new ItemStack(item()), legacy = new ItemStack(item());
        for (boolean installed : new boolean[] { false, true }) {
            List<IRecipe> recipes = ca.teamdman.sfm.common.recipe.ModRecipes
                .cableRecipes(full, fancy, installed ? legacy : null);
            for (int slot = 0; slot < 9; slot++) {
                InventoryCrafting grid = grid();
                grid.setInventorySlotContents(slot, full.copy());
                int matches = 0;
                for (IRecipe recipe : recipes) if (recipe.matches(grid, null)) {
                    matches++;
                    ItemStack result = recipe.getCraftingResult(grid);
                    assertTrue((slot % 3 == 2 ? fancy : legacy).isItemEqual(result));
                    assertEquals(1, result.stackSize);
                }
                assertEquals(slot % 3 == 2 || installed && slot % 3 == 0 ? 1 : 0, matches, "slot " + slot);
            }
            InventoryCrafting grid = grid();
            grid.setInventorySlotContents(4, fancy.copy());
            assertEquals(
                1,
                recipes.stream()
                    .filter(r -> r.matches(grid, null))
                    .count());
        }
    }

    private static int id;

    private static Item item() {
        Item item = new Item();
        Item.itemRegistry.addObject(-1, "minecraft:sfm_recipe_test_" + (id++), item);
        return item;
    }

    private static InventoryCrafting grid() {
        return new InventoryCrafting(new Container() {

            public boolean canInteractWith(EntityPlayer player) {
                return true;
            }
        }, 3, 3);
    }

    public static void conversions() {
        ItemStack original = new ItemStack(item(), 1, 3), target = new ItemStack(item());
        PlainConversionRecipe forward = new PlainConversionRecipe(original, target),
            reverse = new PlainConversionRecipe(target, original);
        InventoryCrafting grid = grid();
        grid.setInventorySlotContents(4, original.copy());
        assertTrue(forward.matches(grid, null));
        ItemStack result = forward.getCraftingResult(grid);
        assertEquals(1, result.stackSize);
        assertTrue(target.isItemEqual(result));
        grid.setInventorySlotContents(4, result);
        assertTrue(reverse.matches(grid, null));
        assertTrue(original.isItemEqual(reverse.getCraftingResult(grid)));
        ItemStack different = original.copy();
        different.setItemDamage(4);
        grid.setInventorySlotContents(4, different);
        assertFalse(forward.matches(grid, null));
        for (String key : new String[] { "sfm:program", "sfm:labels", "sfm:reference", "display",
            "sfm:recovery_item" }) {
            ItemStack saved = original.copy();
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString(key, "preserve");
            saved.setTagCompound(nbt);
            grid.setInventorySlotContents(4, saved);
            assertFalse(forward.matches(grid, null));
            assertEquals(
                "preserve",
                saved.getTagCompound()
                    .getString(key));
        }
        ItemStack empty = original.copy();
        empty.setTagCompound(new NBTTagCompound());
        grid.setInventorySlotContents(4, empty);
        assertTrue(forward.matches(grid, null));
        grid.setInventorySlotContents(0, target.copy());
        assertFalse(forward.matches(grid, null));
        assertThrows(
            IllegalArgumentException.class,
            () -> new PlainConversionRecipe(
                new ItemStack(original.getItem(), 1, OreDictionary.WILDCARD_VALUE),
                target));
    }

    public static void catalog() {
        for (String ore : new String[] { "plateStainlessSteel", "circuitAdvanced", "cableGt01Gold", "foilElectrum",
            "wireFineElectrum" }) OreDictionary.registerOre(ore, new ItemStack(item()));
        Map<String, ItemStack> outputs = new LinkedHashMap<>(), components = new LinkedHashMap<>();
        for (String name : new String[] { "manager", "cable", "disk", "labelgun", "programmingStation", "redstonePort",
            "diskCopier" }) outputs.put(name, new ItemStack(item()));
        List<IRecipe> recipes = LateHvRecipes
            .build(outputs, name -> components.computeIfAbsent(name, k -> new ItemStack(item())));
        assertEquals(7, recipes.size());
        int index = 0;
        for (IRecipe recipe : recipes) {
            assertTrue(recipe instanceof PlainShapedOreRecipe);
            Object[] inputs = ((PlainShapedOreRecipe) recipe).getInput();
            InventoryCrafting grid = grid();
            for (int slot = 0; slot < inputs.length; slot++) {
                Object input = inputs[slot];
                if (input instanceof List) input = ((List<?>) input).get(0);
                if (input != null) grid.setInventorySlotContents(slot, ((ItemStack) input).copy());
            }
            assertTrue(recipe.matches(grid, null), "recipe " + index);
            ItemStack made = recipe.getCraftingResult(grid);
            assertEquals(index == 1 ? 2 : 1, made.stackSize);
            assertFalse(made.hasTagCompound());
            made.setTagCompound(new NBTTagCompound());
            made.getTagCompound()
                .setString("changed", "copy");
            assertFalse(
                recipe.getCraftingResult(grid)
                    .hasTagCompound());
            int first = 0;
            while (grid.getStackInSlot(first) == null) first++;
            ItemStack tagged = grid.getStackInSlot(first);
            tagged.setTagCompound(new NBTTagCompound());
            tagged.getTagCompound()
                .setString("private", "data");
            assertFalse(recipe.matches(grid, null));
            assertEquals(
                "data",
                tagged.getTagCompound()
                    .getString("private"));
            index++;
        }
        assertEquals(1, outputs.get("cable").stackSize);
        assertThrows(IllegalArgumentException.class, () -> LateHvRecipes.build(outputs, name -> null));
    }
}
