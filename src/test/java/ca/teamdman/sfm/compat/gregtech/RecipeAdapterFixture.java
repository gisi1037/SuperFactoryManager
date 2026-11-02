package ca.teamdman.sfm.compat.gregtech;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.recipe.RecipeCatalog.Status;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor.Matching;

/** Structural common API fixtures; positive stack/ore cases run only in the existing isolated real Forge runtime. */
public final class RecipeAdapterFixture {

    private RecipeAdapterFixture() {}

    public static GregTechRecipeCatalogAdapter openEmpty() {
        Maps.reset();
        Values.worldTickHappened = true;
        Backend.reads = 0;
        return open("5.09.54.265");
    }

    public static GregTechRecipeCatalogAdapter open(String version) {
        try {
            return new GregTechRecipeCatalogAdapter(
                version,
                Maps.class,
                NativeMap.class,
                Backend.class,
                NativeRecipe.class,
                AlternativeRecipe.class,
                Metadata.class,
                Values.class);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    public static void snapshotsPreserveNativeDeclarationsWithoutMutatingCaches() throws Exception {
        GregTechRecipeCatalogAdapter adapter = openEmpty();
        NativeRecipe recipe = recipe();
        recipe.mInputs = new ItemStack[] { new ItemStack(Items.iron_ingot, 2, 99), new ItemStack(Items.paper, 0) };
        recipe.mInputChances = new int[] { 5000, 0 };
        recipe.mOutputs = new ItemStack[] { new ItemStack(Items.gold_ingot), null, new ItemStack(Items.redstone, 2) };
        recipe.mOutputChances = new int[] { 10000, 10000, 2500 };
        recipe.mFluidInputs = new FluidStack[] { new FluidStack(FluidRegistry.WATER, 1000) };
        recipe.mAltFluidInputs = new FluidStack[][] { { new FluidStack(FluidRegistry.LAVA, 250) } };
        recipe.mFluidInputChances = new int[] { 8000 };
        recipe.mFluidOutputs = new FluidStack[] { new FluidStack(FluidRegistry.WATER, 100) };
        recipe.mFluidOutputChances = new int[] { 3000 };
        recipe.mHidden = true;
        recipe.mNeedsEmptyOutput = true;
        recipe.mOutputs[0].setTagCompound(new NBTTagCompound());
        recipe.mOutputs[0].getTagCompound()
            .setInteger("batch", 4);
        Maps.maceratorRecipes.backend.recipes.add(recipe);
        ItemIdentity output = ItemIdentity.fromStack(recipe.mOutputs[0], new QueryBudget());
        GregTechRecipeCatalogAdapter.Build build = adapter.build(UUID.randomUUID(), 1);
        RecipeDescriptor descriptor = build.catalog.find(output, "gt.recipe.macerator", new QueryBudget(), 1).recipes
            .get(0);
        assertEquals(1, build.visited);
        assertEquals(0, build.unsupported);
        assertEquals(0, build.incompleteMaps);
        assertEquals(5000, descriptor.itemInputs.get(0).chance);
        assertTrue(descriptor.itemInputs.get(0).nbtSensitive, "Effective special-item NBT matching is captured");
        assertTrue(
            descriptor.itemInputs.get(1)
                .isCatalyst());
        assertEquals(0, descriptor.itemInputs.get(1).chance);
        assertEquals(2, descriptor.itemOutputs.get(1).slot);
        assertEquals(2500, descriptor.itemOutputs.get(1).chance);
        assertEquals(8000, descriptor.fluidInputs.get(0).chance);
        assertEquals(250, descriptor.fluidInputs.get(0).alternatives.get(0).amount);
        assertEquals(3000, descriptor.fluidOutputs.get(0).chance);
        assertTrue(descriptor.hidden);
        assertTrue(descriptor.needsEmptyOutput);
        String fingerprint = descriptor.fingerprint;
        recipe.mOutputs[0].getTagCompound()
            .setInteger("batch", 9);
        recipe.mInputs = new ItemStack[0];
        int reads = Backend.reads;
        for (int i = 0; i < 20; i++) assertSame(
            descriptor,
            build.catalog.find(output, "gt.recipe.macerator", new QueryBudget(), 2).recipes.get(0));
        assertEquals(reads, Backend.reads, "Indexed lookup does not touch the registry/backend");
        assertEquals(fingerprint, descriptor.fingerprint);
    }

    public static void alternativesAndUnsupportedCoverageAreConservative() throws Exception {
        GregTechRecipeCatalogAdapter adapter = openEmpty();
        AlternativeRecipe recipe = new AlternativeRecipe();
        recipe.mInputs = new ItemStack[] { new ItemStack(Items.iron_ingot), new ItemStack(Items.paper) };
        recipe.mOutputs = new ItemStack[] { new ItemStack(Items.gold_ingot) };
        OreDictionary.registerOre("catalogTestIron", new ItemStack(Items.iron_ingot));
        recipe.mOreDictIds = new int[] { OreDictionary.getOreID("catalogTestIron"), -1 };
        recipe.mOreDictAlt = new ItemStack[][] { { new ItemStack(Items.gold_ingot) },
            { new ItemStack(Items.redstone) } };
        Maps.maceratorRecipes.backend.recipes.add(recipe);
        ItemIdentity output = ItemIdentity.fromStack(recipe.mOutputs[0], new QueryBudget());
        RecipeDescriptor descriptor = adapter.build(UUID.randomUUID(), 1).catalog
            .find(output, "gt.recipe.macerator", new QueryBudget(), 0).recipes.get(0);
        assertEquals(Matching.ORE_OR_EXPLICIT, descriptor.itemInputs.get(0).matching);
        assertEquals("catalogTestIron", descriptor.itemInputs.get(0).ore);
        assertEquals(1, descriptor.itemInputs.get(0).alternatives.size());
        assertEquals(Matching.GT_NATIVE_ITEM, descriptor.itemInputs.get(1).matching);
        assertTrue(descriptor.itemInputs.get(1).alternatives.isEmpty());
        assertEquals(1, descriptor.itemInputs.get(1).displayAlternatives.size());

        for (int invalid = 0; invalid < 4; invalid++) {
            adapter = openEmpty();
            NativeRecipe unsupported = recipe();
            if (invalid == 0) unsupported.mSpecialItems = new Object();
            if (invalid == 1) unsupported.metadata.contents = Collections.singleton("unknown metadata");
            if (invalid == 2) unsupported.mOutputChances = new int[] { 10001 };
            if (invalid == 3) unsupported.mSpecialValue = 1;
            Maps.maceratorRecipes.backend.recipes.add(unsupported);
            output = ItemIdentity.fromStack(unsupported.mOutputs[0], new QueryBudget());
            GregTechRecipeCatalogAdapter.Build build = adapter.build(UUID.randomUUID(), 1);
            assertEquals(1, build.unsupported);
            assertEquals(
                Status.UNSUPPORTED,
                build.catalog.find(output, "gt.recipe.macerator", new QueryBudget(), 0).status);
            assertEquals(0, build.catalog.recipeCount());
        }
        adapter = openEmpty();
        NativeRecipe dynamic = new DerivedRecipe();
        dynamic.mOutputs = recipe().mOutputs;
        Maps.maceratorRecipes.backend.recipes.add(dynamic);
        GregTechRecipeCatalogAdapter.Build incomplete = adapter.build(UUID.randomUUID(), 1);
        assertEquals(1, incomplete.incompleteMaps);
        assertEquals(
            Status.UNSUPPORTED,
            incomplete.catalog.find(output, "gt.recipe.macerator", new QueryBudget(), 0).status);
        adapter = openEmpty();
        Maps.maceratorRecipes.backend = new DerivedBackend();
        assertEquals(
            Status.UNSUPPORTED,
            adapter.build(UUID.randomUUID(), 1).catalog
                .find(output, "gt.recipe.macerator", new QueryBudget(), 0).status);
        assertThrows(UnsupportedOperationException.class, () -> open("unknown-version"));
    }

    private static NativeRecipe recipe() {
        NativeRecipe recipe = new NativeRecipe();
        recipe.mOutputs = new ItemStack[] { new ItemStack(Items.gold_ingot) };
        return recipe;
    }

    public static final class Values {

        public static boolean worldTickHappened;
    }

    public static final class Maps {

        public static NativeMap maceratorRecipes, benderRecipes, cutterRecipes;

        static void reset() {
            NativeMap.ALL_RECIPE_MAPS.clear();
            maceratorRecipes = new NativeMap("gt.recipe.macerator");
            benderRecipes = new NativeMap("gt.recipe.metalbender");
            cutterRecipes = new NativeMap("gt.recipe.cuttingsaw");
        }
    }

    public static final class NativeMap {

        public static final Map<String, NativeMap> ALL_RECIPE_MAPS = new HashMap<>();
        public final String unlocalizedName;
        public Backend backend = new Backend();

        NativeMap(String name) {
            unlocalizedName = name;
            ALL_RECIPE_MAPS.put(name, this);
        }

        public Backend getBackend() {
            return backend;
        }

        public int getAmperage() {
            return 1;
        }
    }

    public static class Backend {

        public static int reads;
        public Collection<NativeRecipe> recipes = new ArrayList<>();
        public Runnable onRead;

        public Map<Object, Collection<NativeRecipe>> getRecipeCategoryMap() {
            reads++;
            if (onRead != null) onRead.run();
            return Collections.singletonMap("category", recipes);
        }

        public Collection<NativeRecipe> getAllRecipes() {
            throw new AssertionError("Do not flatten native recipes");
        }
    }

    public static final class DerivedBackend extends Backend {
    }

    public static class NativeRecipe {

        public ItemStack[] mInputs = new ItemStack[0], mOutputs = new ItemStack[0];
        public FluidStack[] mFluidInputs = new FluidStack[0], mFluidOutputs = new FluidStack[0];
        public FluidStack[][] mAltFluidInputs;
        public int[] mInputChances, mOutputChances, mFluidInputChances, mFluidOutputChances;
        public Object mSpecialItems;
        public int mSpecialValue, mDuration = 20, mEUt = 8;
        public boolean mEnabled = true, mHidden, mFakeRecipe, mNeedsEmptyOutput, isNBTSensitive;
        public final Metadata metadata = new Metadata();

        public Metadata getMetadataStorage() {
            return metadata;
        }

        public static boolean jvmdowngrader$nest$gregtech_api_util_GTRecipe$shouldCheckNBT(ItemStack item) {
            return item.getItemDamage() == 99;
        }

        public Object getCachedCombinedItemInputs() {
            throw new AssertionError("Do not mutate native input caches");
        }
    }

    public static final class AlternativeRecipe extends NativeRecipe {

        public ItemStack[][] mOreDictAlt;
        public int[] mOreDictIds;
    }

    public static final class DerivedRecipe extends NativeRecipe {
    }

    public static final class Metadata {

        public Set<?> contents = Collections.emptySet();

        public Set<?> getEntries() {
            return contents;
        }
    }
}
