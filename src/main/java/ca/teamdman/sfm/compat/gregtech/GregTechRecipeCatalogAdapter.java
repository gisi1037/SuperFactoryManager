package ca.teamdman.sfm.compat.gregtech;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfml.data.FluidIdentity;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.recipe.RecipeCatalog;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor.FluidAlternative;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor.FluidPart;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor.ItemPart;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor.Matching;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

/** Explicit common-only adapter for inspected GT APIs. Never constructs native recipe input caches or calls NEI. */
public final class GregTechRecipeCatalogAdapter {

    private static final String[] FIELDS = { "mInputs", "mOutputs", "mFluidInputs", "mFluidOutputs", "mAltFluidInputs",
        "mInputChances", "mOutputChances", "mFluidInputChances", "mFluidOutputChances", "mSpecialItems",
        "mSpecialValue", "mDuration", "mEUt", "mEnabled", "mHidden", "mFakeRecipe", "mNeedsEmptyOutput",
        "isNBTSensitive" };
    private static final String[] MAP_FIELDS = { "maceratorRecipes", "benderRecipes", "cutterRecipes" };
    private static final String[] MAP_IDS = { "gt.recipe.macerator", "gt.recipe.metalbender", "gt.recipe.cuttingsaw" };
    private static final int MAX_NATIVE_VISITS = 65536, MAX_CATEGORIES = 256;
    private final Class<?> recipeClass, alternativeClass, mapClass, backendClass;
    private final Field allMaps, worldReady, oreAlternatives, oreIds;
    private final Field[] selectedMaps = new Field[3];
    private final Map<String, Field> fields = new HashMap<>();
    private final Method getBackend, categoryMap, getAmperage, metadata, entries, getSpecialItem, stacksEqual;
    private final Field legacyNbtHelper;
    private final Field[] specialItems = new Field[3];
    private final Field mapName;
    private final String adapter;
    private final long ownerThread = Thread.currentThread()
        .getId();

    public static GregTechRecipeCatalogAdapter open() throws ReflectiveOperationException {
        if (!Loader.isModLoaded("gregtech")) throw new UnsupportedOperationException("GregTech is not loaded");
        ModContainer mod = Loader.instance()
            .getIndexedModList()
            .get("gregtech");
        String version = mod == null ? "" : mod.getVersion();
        return new GregTechRecipeCatalogAdapter(
            version,
            Class.forName("gregtech.api.recipe.RecipeMaps"),
            Class.forName("gregtech.api.recipe.RecipeMap"),
            Class.forName("gregtech.api.recipe.RecipeMapBackend"),
            Class.forName("gregtech.api.util.GTRecipe"),
            Class.forName("gregtech.api.util.GTRecipe$GTRecipe_WithAlt"),
            Class.forName("gregtech.api.recipe.metadata.IRecipeMetadataStorage"),
            Class.forName("gregtech.api.enums.GTValues"),
            Class.forName("gregtech.api.enums.ItemList"),
            Class.forName("gregtech.api.util.GTUtility"));
    }

    // Package-local binding seam permits common-only fixtures without shipping fake GT classes or loading its registry.
    GregTechRecipeCatalogAdapter(String version, Class<?> maps, Class<?> mapClass, Class<?> backendClass,
        Class<?> recipeClass, Class<?> alternativeClass, Class<?> storage, Class<?> values, Class<?> itemList,
        Class<?> utility) throws ReflectiveOperationException {
        if (!"5.09.54.250".equals(version) && !"5.09.54.265".equals(version))
            throw new UnsupportedOperationException("Unqualified GregTech recipe API version: " + version);
        adapter = "gtnh-gt-" + version + "-inspection-v1";
        this.recipeClass = recipeClass;
        this.alternativeClass = alternativeClass;
        this.mapClass = mapClass;
        this.backendClass = backendClass;
        allMaps = mapClass.getField("ALL_RECIPE_MAPS");
        mapName = mapClass.getField("unlocalizedName");
        getBackend = mapClass.getMethod("getBackend");
        getAmperage = mapClass.getMethod("getAmperage");
        categoryMap = backendClass.getMethod("getRecipeCategoryMap");
        metadata = recipeClass.getMethod("getMetadataStorage");
        entries = storage.getMethod("getEntries");
        // Java17+ GT classes have no Java8 downgrade bridge. Use the same public prototypes/comparison as GT's rule.
        legacyNbtHelper = recipeClass.getField("GTppRecipeHelper");
        getSpecialItem = itemList.getMethod("get", long.class, Object[].class);
        stacksEqual = utility.getMethod("areStacksEqual", ItemStack.class, ItemStack.class, boolean.class);
        String[] specialNames = { "Cell_Universal_Fluid", "Tool_DataStick", "Tool_DataOrb" };
        for (int i = 0; i < specialNames.length; i++) specialItems[i] = itemList.getField(specialNames[i]);
        worldReady = values.getField("worldTickHappened");
        oreAlternatives = alternativeClass.getField("mOreDictAlt");
        oreIds = alternativeClass.getField("mOreDictIds");
        for (String name : FIELDS) fields.put(name, recipeClass.getField(name));
        for (int i = 0; i < MAP_FIELDS.length; i++) selectedMaps[i] = maps.getField(MAP_FIELDS[i]);
    }

    public boolean firstWorldTickReady() throws IllegalAccessException {
        thread();
        return worldReady.getBoolean(null);
    }

    public static final class Build {

        public final RecipeCatalog catalog;
        public final int visited, unsupported, incompleteMaps;
        public final long elapsedNanos;

        private Build(RecipeCatalog catalog, int visited, int unsupported, int incompleteMaps, long elapsedNanos) {
            this.catalog = catalog;
            this.visited = visited;
            this.unsupported = unsupported;
            this.incompleteMaps = incompleteMaps;
            this.elapsedNanos = elapsedNanos;
        }
    }

    /** One bounded cold snapshot on the server thread, after GT's first-world startup work has completed. */
    public Build build(UUID session, long revision) throws ReflectiveOperationException {
        thread();
        if (!firstWorldTickReady()) throw new IllegalStateException("GregTech startup recipe work is not complete");
        long started = System.nanoTime();
        RecipeCatalog.Builder builder = new RecipeCatalog.Builder(session, revision);
        Object registry = allMaps.get(null);
        if (!(registry instanceof Map)) throw new UnsupportedOperationException("Unsupported recipe registry");
        int visited = 0, unsupported = 0;
        Set<String> incompleteMaps = new HashSet<>();
        ItemStack[] nbtItems = specialNbtItems();
        for (int i = 0; i < MAP_IDS.length; i++) {
            String id = MAP_IDS[i];
            builder.registerMap(id);
            Object map = selectedMaps[i].get(null);
            if (map == null || map.getClass() != mapClass
                || ((Map<?, ?>) registry).get(id) != map
                || !id.equals(mapName.get(map))) {
                incomplete(builder, id, incompleteMaps, "unqualified map identity");
                continue;
            }
            Object backend = call(getBackend, map);
            if (backend == null || backend.getClass() != backendClass) {
                incomplete(builder, id, incompleteMaps, "unqualified backend");
                continue;
            }
            Object categories = call(categoryMap, backend);
            if (!(categories instanceof Map) || ((Map<?, ?>) categories).size() > MAX_CATEGORIES)
                throw new UnsupportedOperationException("Unsupported recipe category registry");
            // Opt-in cold-build diagnostics use collection sizes, never flattening or touching native input caches.
            if (Boolean.getBoolean("sfm.recipeDiagnostics")) {
                long entries = 0;
                for (Object recipes : ((Map<?, ?>) categories).values()) {
                    if (!(recipes instanceof Collection))
                        throw new UnsupportedOperationException("Unsupported recipe collection");
                    entries += ((Collection<?>) recipes).size();
                }
                SuperFactoryManager.LOG.info("Recipe catalog input {}: {} native entries", id, entries);
            }
            int amperage = (Integer) call(getAmperage, map);
            for (Object recipes : ((Map<?, ?>) categories).values()) {
                if (!(recipes instanceof Collection))
                    throw new UnsupportedOperationException("Unsupported recipe collection");
                for (Object recipe : (Collection<?>) recipes) {
                    if (++visited > MAX_NATIVE_VISITS)
                        throw new IllegalArgumentException("Native recipe visit limit exceeded");
                    if (recipe == null || !recipeClass.isInstance(recipe)) {
                        incomplete(builder, id, incompleteMaps, "invalid native recipe entry");
                        unsupported++;
                        continue;
                    }
                    if (!(Boolean) read("mEnabled", recipe) || (Boolean) read("mFakeRecipe", recipe)) continue;
                    if (recipe.getClass() != recipeClass && recipe.getClass() != alternativeClass) {
                        incomplete(
                            builder,
                            id,
                            incompleteMaps,
                            "unqualified recipe subclass " + recipe.getClass()
                                .getName());
                        unsupported++;
                        continue;
                    }
                    RecipeDescriptor descriptor;
                    try {
                        descriptor = describe(recipe, id, amperage, nbtItems, new QueryBudget(16));
                    } catch (IllegalArgumentException | UnsupportedOperationException unavailable) {
                        unsupported++;
                        List<ItemIdentity> outputs;
                        try {
                            outputs = outputKeys(recipe, new QueryBudget(16));
                        } catch (IllegalArgumentException badOutput) {
                            incomplete(builder, id, incompleteMaps, "unreadable output identity");
                            continue;
                        }
                        if (!outputs.isEmpty()) builder.unsupported(id, outputs, new QueryBudget(16));
                        continue;
                    }
                    // Build/admission failures are fatal, never quietly converted into partial published maps.
                    builder.add(descriptor, new QueryBudget(16));
                }
            }
        }
        return new Build(builder.finish(), visited, unsupported, incompleteMaps.size(), System.nanoTime() - started);
    }

    private static void incomplete(RecipeCatalog.Builder builder, String id, Set<String> maps, String reason) {
        builder.incomplete(id);
        if (maps.add(id) && Boolean.getBoolean("sfm.recipeDiagnostics"))
            SuperFactoryManager.LOG.info("Recipe catalog incomplete {}: {}", id, reason);
    }

    private RecipeDescriptor describe(Object recipe, String id, int amperage, ItemStack[] nbtItems, QueryBudget budget)
        throws ReflectiveOperationException {
        if (recipe.getClass() != recipeClass && recipe.getClass() != alternativeClass)
            throw new UnsupportedOperationException("Unqualified recipe subclass");
        Object storage = call(metadata, recipe);
        if (storage != null) {
            Object contents = call(entries, storage);
            if (!(contents instanceof Collection) || !((Collection<?>) contents).isEmpty())
                throw new UnsupportedOperationException("Unqualified recipe metadata");
        }
        if (read("mSpecialItems", recipe) != null || (Integer) read("mSpecialValue", recipe) != 0)
            throw new UnsupportedOperationException("Unqualified special recipe requirement");
        ItemStack[] inputs = items(read("mInputs", recipe)), outputs = items(read("mOutputs", recipe));
        ItemStack[][] alternatives = recipe.getClass() == alternativeClass ? (ItemStack[][]) oreAlternatives.get(recipe)
            : null;
        int[] ore = recipe.getClass() == alternativeClass ? (int[]) oreIds.get(recipe) : null;
        if (alternatives != null && alternatives.length > inputs.length || ore != null && ore.length > inputs.length)
            throw new IllegalArgumentException("Invalid item alternative slots");
        boolean declaredNbt = (Boolean) read("isNBTSensitive", recipe);
        List<ItemPart> itemInputs = new ArrayList<>(), itemOutputs = new ArrayList<>();
        for (int i = 0; i < inputs.length; i++) {
            ItemStack stack = inputs[i];
            if (stack == null) continue;
            List<ItemIdentity> display = identities(
                alternatives == null || i >= alternatives.length ? null : alternatives[i],
                budget);
            boolean hasOre = ore != null && i < ore.length && ore[i] >= 0;
            String name = hasOre ? OreDictionary.getOreName(ore[i]) : "";
            if (hasOre && (name == null || "Unknown".equals(name)))
                throw new IllegalArgumentException("Unknown ore predicate");
            boolean nbt = declaredNbt || !hasOre && specialNbt(stack, nbtItems, budget);
            budget.chargeProviders(3);
            itemInputs.add(
                new ItemPart(
                    i,
                    ItemIdentity.fromStack(stack, budget),
                    stack.stackSize,
                    chance("mInputChances", recipe, i),
                    hasOre ? Matching.ORE_OR_EXPLICIT : Matching.GT_NATIVE_ITEM,
                    nbt,
                    name,
                    hasOre ? display : Collections.emptyList(),
                    display));
        }
        for (int i = 0; i < outputs.length; i++) if (outputs[i] != null) {
            budget.chargeProviders(3);
            itemOutputs.add(
                new ItemPart(
                    i,
                    ItemIdentity.fromStack(outputs[i], budget),
                    outputs[i].stackSize,
                    chance("mOutputChances", recipe, i),
                    Matching.EXACT,
                    false,
                    "",
                    Collections.emptyList(),
                    Collections.emptyList()));
        }
        return new RecipeDescriptor(
            adapter,
            id,
            (Integer) read("mEUt", recipe),
            (Integer) read("mDuration", recipe),
            amperage,
            (Boolean) read("mHidden", recipe),
            (Boolean) read("mNeedsEmptyOutput", recipe),
            itemInputs,
            itemOutputs,
            fluidParts(recipe, false, budget),
            fluidParts(recipe, true, budget),
            Collections.emptyList(),
            budget);
    }

    private ItemStack[] specialNbtItems() throws ReflectiveOperationException {
        if (!legacyNbtHelper.getBoolean(null)) return new ItemStack[0];
        ItemStack[] result = new ItemStack[specialItems.length];
        for (int i = 0; i < specialItems.length; i++) {
            result[i] = (ItemStack) call(getSpecialItem, specialItems[i].get(null), 1L, new Object[0]);
            if (result[i] == null || result[i].getItem() == null)
                throw new UnsupportedOperationException("GT special-item NBT prototypes are unavailable");
        }
        return result;
    }

    private boolean specialNbt(ItemStack stack, ItemStack[] special, QueryBudget budget)
        throws ReflectiveOperationException {
        for (ItemStack candidate : special) {
            budget.chargeProvider();
            if ((Boolean) call(stacksEqual, null, stack, candidate, true)) return true;
        }
        return false;
    }

    private List<FluidPart> fluidParts(Object recipe, boolean output, QueryBudget budget)
        throws IllegalAccessException {
        FluidStack[] values = fluids(read(output ? "mFluidOutputs" : "mFluidInputs", recipe));
        FluidStack[][] alternatives = output ? null : (FluidStack[][]) read("mAltFluidInputs", recipe);
        if (alternatives != null && alternatives.length > values.length)
            throw new IllegalArgumentException("Invalid fluid alternative slots");
        List<FluidPart> result = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            FluidStack value = values[i];
            if (value == null) continue;
            List<FluidAlternative> choices = new ArrayList<>();
            if (alternatives != null && i < alternatives.length && alternatives[i] != null) {
                if (alternatives[i].length > RecipeDescriptor.MAX_ALTERNATIVES)
                    throw new IllegalArgumentException("Fluid alternative limit exceeded");
                for (FluidStack choice : alternatives[i]) if (choice != null) {
                    budget.chargeProvider();
                    choices.add(new FluidAlternative(FluidIdentity.fromStack(choice, budget), choice.amount));
                }
            }
            budget.chargeProvider();
            result.add(
                new FluidPart(
                    i,
                    FluidIdentity.fromStack(value, budget),
                    value.amount,
                    chance(output ? "mFluidOutputChances" : "mFluidInputChances", recipe, i),
                    choices));
        }
        return result;
    }

    private List<ItemIdentity> outputKeys(Object recipe, QueryBudget budget) throws IllegalAccessException {
        return identities(items(read("mOutputs", recipe)), budget);
    }

    private static List<ItemIdentity> identities(ItemStack[] stacks, QueryBudget budget) {
        if (stacks == null) return Collections.emptyList();
        if (stacks.length > RecipeDescriptor.MAX_ALTERNATIVES)
            throw new IllegalArgumentException("Item alternative limit exceeded");
        List<ItemIdentity> result = new ArrayList<>();
        for (ItemStack stack : stacks) if (stack != null) {
            budget.chargeProviders(3);
            result.add(ItemIdentity.fromStack(stack, budget));
        }
        return result;
    }

    private int chance(String name, Object recipe, int slot) throws IllegalAccessException {
        int[] chances = (int[]) read(name, recipe);
        if (chances != null && chances.length > RecipeDescriptor.MAX_SLOTS)
            throw new IllegalArgumentException("Recipe chance array limit exceeded");
        return chances == null || slot >= chances.length ? 10000 : chances[slot];
    }

    private Object read(String name, Object target) throws IllegalAccessException {
        return fields.get(name)
            .get(target);
    }

    private static ItemStack[] items(Object value) {
        ItemStack[] values = value == null ? new ItemStack[0] : (ItemStack[]) value;
        if (values.length > RecipeDescriptor.MAX_SLOTS) throw new IllegalArgumentException("Item slot limit exceeded");
        return values;
    }

    private static FluidStack[] fluids(Object value) {
        FluidStack[] values = value == null ? new FluidStack[0] : (FluidStack[]) value;
        if (values.length > RecipeDescriptor.MAX_SLOTS) throw new IllegalArgumentException("Fluid slot limit exceeded");
        return values;
    }

    private static Object call(Method method, Object target, Object... arguments) throws ReflectiveOperationException {
        try {
            return method.invoke(target, arguments);
        } catch (InvocationTargetException failure) {
            throw new UnsupportedOperationException("Native recipe inspection callback failed", failure.getCause());
        }
    }

    private void thread() {
        if (Thread.currentThread()
            .getId() != ownerThread)
            throw new IllegalStateException("Recipe registry access requires its server thread");
    }
}
