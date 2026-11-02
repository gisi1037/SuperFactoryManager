package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfml.data.FluidIdentity;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.recipe.RecipeCatalog;
import ca.teamdman.sfml.data.recipe.RecipeCatalog.Builder;
import ca.teamdman.sfml.data.recipe.RecipeCatalog.Result;
import ca.teamdman.sfml.data.recipe.RecipeCatalog.Status;
import ca.teamdman.sfml.data.recipe.RecipeCatalog.Token;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor.FluidAlternative;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor.FluidPart;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor.ItemPart;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor.Matching;

class RecipeCatalogTest {

    private static final String MAP = "gt.recipe.macerator";
    private static final ItemIdentity OUTPUT = key("result", 0, null);

    @Test
    void catalystsZeroChanceAndDuplicateRequirementsStaySeparateAndImmutable() {
        List<ItemPart> inputs = new ArrayList<>();
        inputs.add(part(0, key("circuit", 1, null), 0, 10000, Matching.GT_NATIVE_ITEM));
        inputs.add(part(2, key("ore", 0, null), 1, 5000, Matching.GT_NATIVE_ITEM));
        inputs.add(part(3, key("ore", 0, null), 2, 0, Matching.GT_NATIVE_ITEM));
        RecipeDescriptor recipe = descriptor(
            8,
            inputs,
            outputParts(),
            Collections.emptyList(),
            Collections.emptyList());
        inputs.clear();
        assertEquals(3, recipe.itemInputs.size());
        assertTrue(
            recipe.itemInputs.get(0)
                .isCatalyst());
        assertEquals(2, recipe.itemInputs.get(1).slot);
        assertEquals(5000, recipe.itemInputs.get(1).chance);
        assertEquals(0, recipe.itemInputs.get(2).chance);
        assertEquals(2, recipe.itemInputs.get(2).amount);
        assertThrows(UnsupportedOperationException.class, () -> recipe.itemInputs.clear());
    }

    @Test
    void displayAlternativesNeverBecomeExecutionAlternatives() {
        List<ItemIdentity> display = new ArrayList<>(Collections.singletonList(key("display", 0, null)));
        ItemPart input = new ItemPart(
            0,
            key("ore", 0, null),
            1,
            10000,
            Matching.GT_NATIVE_ITEM,
            false,
            "",
            Collections.emptyList(),
            display);
        display.clear();
        assertEquals(1, input.displayAlternatives.size());
        assertTrue(input.alternatives.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> input.displayAlternatives.clear());
        assertThrows(
            IllegalArgumentException.class,
            () -> new ItemPart(
                0,
                input.item,
                1,
                10000,
                Matching.GT_NATIVE_ITEM,
                false,
                "",
                input.displayAlternatives,
                Collections.emptyList()));
        ItemPart ore = new ItemPart(
            0,
            input.item,
            1,
            10000,
            Matching.ORE_OR_EXPLICIT,
            true,
            "oreIron",
            input.displayAlternatives,
            input.displayAlternatives);
        assertEquals("oreIron", ore.ore);
        assertTrue(ore.nbtSensitive);
    }

    @Test
    void fluidAlternativeAmountsAndAllChancesContributeToIdentity() {
        FluidIdentity water = FluidIdentity.fromStack(TestFluid.water(1000), new QueryBudget());
        FluidIdentity lubricant = FluidIdentity.fromStack(TestFluid.of(new Fluid("lubricant"), 1), new QueryBudget());
        FluidPart first = new FluidPart(
            0,
            water,
            1000,
            9000,
            Collections.singletonList(new FluidAlternative(lubricant, 250)));
        FluidPart changed = new FluidPart(
            0,
            water,
            1000,
            9000,
            Collections.singletonList(new FluidAlternative(lubricant, 251)));
        RecipeDescriptor a = descriptor(
            8,
            Collections.emptyList(),
            outputParts(),
            Collections.singletonList(first),
            Collections.emptyList());
        RecipeDescriptor b = descriptor(
            8,
            Collections.emptyList(),
            outputParts(),
            Collections.singletonList(changed),
            Collections.emptyList());
        assertNotEquals(a.fingerprint, b.fingerprint);
        assertEquals(250, a.fluidInputs.get(0).alternatives.get(0).amount);
        assertNotEquals(
            a.fingerprint,
            descriptor(
                8,
                Collections.emptyList(),
                outputParts(),
                Collections.singletonList(new FluidPart(0, water, 1000, 8999, first.alternatives)),
                Collections.emptyList()).fingerprint);
        RecipeDescriptor byproduct = new RecipeDescriptor(
            "gt-265-v1",
            MAP,
            8,
            20,
            1,
            false,
            false,
            Collections.emptyList(),
            outputParts(),
            Collections.emptyList(),
            Collections.singletonList(new FluidPart(1, water, 100, 5000, Collections.emptyList())),
            Collections.emptyList(),
            new QueryBudget(16));
        assertNotEquals(
            byproduct.fingerprint,
            descriptor(
                8,
                Collections.emptyList(),
                outputParts(),
                Collections.emptyList(),
                Collections.emptyList()).fingerprint);
    }

    @Test
    void requirementsAreCanonicalWhileCostsFlagsPoliciesAndSlotOrderRemainExact() {
        RecipeDescriptor a = descriptor(
            8,
            Collections.emptyList(),
            outputParts(),
            Collections.emptyList(),
            Arrays.asList("heat", "cleanroom"));
        RecipeDescriptor reordered = descriptor(
            8,
            Collections.emptyList(),
            outputParts(),
            Collections.emptyList(),
            Arrays.asList("cleanroom", "heat"));
        assertEquals(a.fingerprint, reordered.fingerprint);
        assertNotEquals(
            a.fingerprint,
            descriptor(
                9,
                Collections.emptyList(),
                outputParts(),
                Collections.emptyList(),
                a.unsupportedRequirements).fingerprint);
        RecipeDescriptor flags = new RecipeDescriptor(
            "gt-265-v1",
            MAP,
            8,
            21,
            2,
            true,
            true,
            Collections.emptyList(),
            outputParts(),
            Collections.emptyList(),
            Collections.emptyList(),
            a.unsupportedRequirements,
            new QueryBudget(16));
        assertNotEquals(a.fingerprint, flags.fingerprint);
        assertEquals(64, a.fingerprint.length());
    }

    @Test
    void malformedAndOversizedDeclarationsRefuseInsteadOfNormalizingRequirements() {
        assertThrows(IllegalArgumentException.class, () -> part(0, OUTPUT, -1, 10000, Matching.EXACT));
        assertThrows(IllegalArgumentException.class, () -> part(0, OUTPUT, 1, 10001, Matching.EXACT));
        assertThrows(
            IllegalArgumentException.class,
            () -> descriptor(
                8,
                Collections.emptyList(),
                Collections.singletonList(part(0, OUTPUT, 0, 10000, Matching.EXACT)),
                Collections.emptyList(),
                Collections.emptyList()));
        assertThrows(
            IllegalArgumentException.class,
            () -> descriptor(
                8,
                Collections.emptyList(),
                Arrays.asList(part(1, OUTPUT, 1, 10000, Matching.EXACT), part(0, OUTPUT, 1, 10000, Matching.EXACT)),
                Collections.emptyList(),
                Collections.emptyList()));
        assertThrows(
            IllegalArgumentException.class,
            () -> descriptor(
                8,
                Collections.nCopies(65, part(0, OUTPUT, 1, 10000, Matching.GT_NATIVE_ITEM)),
                outputParts(),
                Collections.emptyList(),
                Collections.emptyList()));
        assertThrows(
            IllegalArgumentException.class,
            () -> new ItemPart(
                0,
                OUTPUT,
                1,
                10000,
                Matching.ORE_OR_EXPLICIT,
                false,
                "",
                Collections.emptyList(),
                Collections.emptyList()));
    }

    @Test
    void exactOutputIndexPreservesAmbiguityAndCoalescesOnlyEquivalentDescriptors() {
        Builder builder = builder();
        RecipeDescriptor first = recipe(8), second = recipe(16);
        builder.add(first, new QueryBudget());
        builder.add(recipe(8), new QueryBudget());
        builder.add(second, new QueryBudget());
        RecipeCatalog catalog = builder.finish();
        Result result = catalog.find(OUTPUT, MAP, new QueryBudget(), 0);
        assertEquals(Status.MATCHES, result.status);
        assertTrue(result.coverageComplete);
        assertTrue(result.isAmbiguous());
        assertEquals(2, result.recipes.size());
        assertEquals(2, catalog.recipeCount());
        assertThrows(UnsupportedOperationException.class, () -> result.recipes.clear());
        assertEquals(Status.NO_MATCH, catalog.find(key("result", 1, null), MAP, new QueryBudget(), 0).status);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("heat", 1);
        assertEquals(Status.NO_MATCH, catalog.find(key("result", 0, tag), MAP, new QueryBudget(), 0).status);
        assertEquals(Status.UNSUPPORTED, catalog.find(OUTPUT, "unknown", new QueryBudget(), 0).status);
    }

    @Test
    void duplicateOutputSlotsHaveOnePostingButRemainInTheDescriptor() {
        Builder builder = builder();
        RecipeDescriptor recipe = descriptor(
            8,
            Collections.emptyList(),
            Arrays.asList(part(0, OUTPUT, 1, 10000, Matching.EXACT), part(2, OUTPUT, 2, 5000, Matching.EXACT)),
            Collections.emptyList(),
            Collections.emptyList());
        builder.add(recipe, new QueryBudget());
        RecipeCatalog catalog = builder.finish();
        assertEquals(1, catalog.postingCount());
        assertEquals(2, catalog.find(OUTPUT, MAP, new QueryBudget(), 0).recipes.get(0).itemOutputs.size());
    }

    @Test
    void unsupportedOutputBucketsAndIncompleteMapsNeverProveAbsenceOrUniqueness() {
        Builder builder = builder();
        builder.add(recipe(8), new QueryBudget());
        builder.unsupported(MAP, Collections.singletonList(OUTPUT), new QueryBudget());
        RecipeCatalog catalog = builder.finish();
        Result result = catalog.find(OUTPUT, MAP, new QueryBudget(), 0);
        assertEquals(Status.UNSUPPORTED, result.status);
        assertFalse(result.coverageComplete);
        assertEquals(1, result.recipes.size());
        assertEquals(Status.NO_MATCH, catalog.find(key("other", 0, null), MAP, new QueryBudget(), 0).status);
        Builder incomplete = builder();
        incomplete.unsupported(MAP, Collections.emptyList(), new QueryBudget());
        assertEquals(
            Status.UNSUPPORTED,
            incomplete.finish()
                .find(OUTPUT, MAP, new QueryBudget(), 0).status);
    }

    @Test
    void unknownRequirementDescriptorsRemainVisibleButExplicitlyIncomplete() {
        Builder builder = builder();
        RecipeDescriptor recipe = descriptor(
            8,
            Collections.emptyList(),
            outputParts(),
            Collections.emptyList(),
            Collections.singletonList("unqualified_fluid_matching"));
        builder.add(recipe, new QueryBudget());
        RecipeCatalog catalog = builder.finish();
        assertEquals(Status.UNSUPPORTED, catalog.find(OUTPUT, MAP, new QueryBudget(), 0).status);
        Result resolved = catalog.resolve(catalog.token(recipe, new QueryBudget(), 0), new QueryBudget(), 0);
        assertEquals(Status.UNSUPPORTED, resolved.status);
        assertFalse(resolved.coverageComplete);
        assertSame(recipe, resolved.recipes.get(0));
    }

    @Test
    void tokensAreSessionRevisionAndAdapterScopedAndNeverUseAnIterationIndex() {
        Builder builder = builder();
        RecipeDescriptor recipe = recipe(8);
        builder.add(recipe, new QueryBudget());
        RecipeCatalog catalog = builder.finish();
        Token token = catalog.token(recipe, new QueryBudget(), 0);
        assertSame(recipe, catalog.resolve(token, new QueryBudget(), 0).recipes.get(0));
        for (Token stale : Arrays.asList(
            new Token(UUID.randomUUID(), token.revision, token.adapter, token.map, token.fingerprint),
            new Token(token.session, token.revision + 1, token.adapter, token.map, token.fingerprint),
            new Token(token.session, token.revision, "other", token.map, token.fingerprint)))
            assertEquals(Status.STALE, catalog.resolve(stale, new QueryBudget(), 0).status);
        assertThrows(IllegalArgumentException.class, () -> catalog.token(recipe(8), new QueryBudget(), 0));
        assertThrows(
            IllegalArgumentException.class,
            () -> new Token(token.session, token.revision, token.adapter, MAP, "bad"));
    }

    @Test
    void manyManagersShareOneTickAllowanceAndDeniedQueriesDoNotHash() {
        Builder builder = builder();
        builder.add(recipe(8), new QueryBudget());
        RecipeCatalog catalog = builder.finish();
        for (int i = 0; i < RecipeCatalog.WORK_PER_TICK / 2; i++)
            assertEquals(Status.MATCHES, catalog.find(OUTPUT, MAP, new QueryBudget(), 40).status);
        QueryBudget denied = new QueryBudget();
        assertEquals(Status.LIMIT_EXCEEDED, catalog.find(OUTPUT, MAP, denied, 40).status);
        assertEquals(0, denied.getAllocations());
        assertEquals(0, denied.getSlotWork());
        assertEquals(Status.MATCHES, catalog.find(OUTPUT, MAP, new QueryBudget(), 41).status);
        assertThrows(
            java.util.concurrent.CompletionException.class,
            () -> CompletableFuture.supplyAsync(() -> catalog.find(OUTPUT, MAP, new QueryBudget(), 42))
                .join());
    }

    @Test
    void tooManyResultsReturnNoTruncatedCandidateList() {
        Builder builder = builder();
        for (int i = 0; i <= RecipeCatalog.MAX_RESULTS; i++) builder.add(recipe(i), new QueryBudget());
        Result result = builder.finish()
            .find(OUTPUT, MAP, new QueryBudget(), 0);
        assertEquals(Status.LIMIT_EXCEEDED, result.status);
        assertTrue(result.recipes.isEmpty());
        assertFalse(result.coverageComplete);
    }

    @Test
    void failedOrConsumedBuildersCannotPublishPartialMaps() {
        Builder capped = builder();
        RecipeDescriptor repeated = recipe(8);
        for (int i = 0; i < RecipeCatalog.MAX_RECIPES; i++) capped.add(repeated, new QueryBudget());
        IllegalArgumentException failure = assertThrows(
            IllegalArgumentException.class,
            () -> capped.add(repeated, new QueryBudget()));
        assertTrue(
            failure.getMessage()
                .contains("visits limit: used " + RecipeCatalog.MAX_RECIPES));
        assertTrue(
            failure.getMessage()
                .contains("descriptors 1, postings 1"));
        assertThrows(IllegalStateException.class, capped::finish);
        Builder finished = builder();
        finished.finish();
        assertThrows(IllegalStateException.class, () -> finished.add(repeated, new QueryBudget()));
        Builder invalid = builder();
        assertThrows(IllegalArgumentException.class, () -> invalid.registerMap("bad\nmap"));
        assertThrows(IllegalStateException.class, invalid::finish);
    }

    @Test
    void aggregateRetainedSizeIsCappedBeforePublishingAndCountsSharedKeysConservatively() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("large", String.join("", Collections.nCopies(3000, "x")));
        ItemIdentity large = ItemIdentity.of("recipe_test:tagged", 0, tag, new QueryBudget(16));
        List<ItemPart> inputs = new ArrayList<>();
        for (int i = 0; i < 32; i++) inputs.add(part(i, large, 1, 10000, Matching.GT_NATIVE_ITEM));
        Builder capped = builder();
        long retained = 2048;
        int entries = 0;
        while (true) {
            RecipeDescriptor descriptor = descriptor(
                entries,
                inputs,
                outputParts(),
                Collections.emptyList(),
                Collections.emptyList());
            try {
                capped.add(descriptor, new QueryBudget());
            } catch (IllegalArgumentException exhausted) {
                assertTrue(
                    exhausted.getMessage()
                        .contains("retained bytes limit: used "));
                assertTrue(
                    exhausted.getMessage()
                        .contains("limit " + RecipeCatalog.MAX_RETAINED_BYTES));
                break;
            }
            retained += descriptor.retainedBytes() + 1024;
            entries++;
            assertTrue(
                entries < RecipeCatalog.MAX_RECIPES / 4,
                "Retained descriptor budget must bind long before the entry cap");
        }
        assertTrue(entries > 0);
        assertTrue(retained <= RecipeCatalog.MAX_RETAINED_BYTES);
        assertThrows(IllegalStateException.class, capped::finish);
    }

    private static Builder builder() {
        Builder builder = new Builder(UUID.randomUUID(), 1);
        builder.registerMap(MAP);
        return builder;
    }

    private static RecipeDescriptor recipe(long eut) {
        return descriptor(
            eut,
            Collections.emptyList(),
            outputParts(),
            Collections.emptyList(),
            Collections.emptyList());
    }

    private static RecipeDescriptor descriptor(long eut, List<ItemPart> inputs, List<ItemPart> outputs,
        List<FluidPart> fluids, List<String> unsupported) {
        return new RecipeDescriptor(
            "gt-265-v1",
            MAP,
            eut,
            20,
            1,
            false,
            false,
            inputs,
            outputs,
            fluids,
            Collections.emptyList(),
            unsupported,
            new QueryBudget(16));
    }

    private static List<ItemPart> outputParts() {
        return Collections.singletonList(part(0, OUTPUT, 1, 10000, Matching.EXACT));
    }

    private static ItemPart part(int slot, ItemIdentity key, long amount, int chance, Matching matching) {
        return new ItemPart(
            slot,
            key,
            amount,
            chance,
            matching,
            false,
            "",
            Collections.emptyList(),
            Collections.emptyList());
    }

    private static ItemIdentity key(String name, int metadata, NBTTagCompound nbt) {
        return ItemIdentity.of("recipe_test:" + name, metadata, nbt, new QueryBudget());
    }
}
