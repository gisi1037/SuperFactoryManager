package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.langs.SFMLParser;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataQueries;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.FluidIdentity;
import ca.teamdman.sfml.data.FunctionCompiler;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.RecipeValues;
import ca.teamdman.sfml.data.ResourceSelector;
import ca.teamdman.sfml.data.recipe.RecipeCatalog;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor.FluidAlternative;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor.FluidPart;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor.ItemPart;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor.Matching;

class SfmlRecipeQueryTest {

    private static final String MAP = "gt.recipe.metalbender";
    private static final ItemIdentity OUTPUT = key("recipe_test:output", 7);

    @Test
    void lastAdmittedSearchCannotFaultWhileMintingItsViewReferences() {
        Fixture provider = new Fixture(recipe(24));
        for (int i = 0; i < RecipeCatalog.WORK_PER_TICK - 2; i++) assertEquals(
            RecipeCatalog.Status.NO_MATCH,
            provider.catalog.find(key("recipe_test:absent", 0), MAP, new QueryBudget(), provider.tick).status);
        DataValue found = assertDoesNotThrow(() -> new DataRuntime(null, 0, provider).recipes(OUTPUT, MAP));
        assertEquals(
            "matches",
            found.get("status")
                .asString());
        assertEquals(
            1,
            found.get("recipes")
                .size());
        DataValue denied = new DataRuntime(null, 0, provider).recipes(OUTPUT, MAP);
        assertEquals(
            "limit_exceeded",
            denied.get("status")
                .asString());
        assertEquals(
            0,
            denied.get("recipes")
                .size());
        provider.tick++;
        assertEquals(
            "matches",
            new DataRuntime(null, 0, provider).recipe(
                found.get("recipes")
                    .get(0)
                    .get("reference"))
                .get("status")
                .asString());
    }

    @Test
    void catalogReplacementAfterSearchKeepsOneImmutableObservationAndExpiresItsReference() {
        Fixture provider = new Fixture(recipe(24));
        provider.replaceAfterFind = new Fixture(recipe(96)).catalog;
        DataValue found = assertDoesNotThrow(() -> new DataRuntime(null, 0, provider).recipes(OUTPUT, MAP));
        assertEquals(
            24,
            found.get("recipes")
                .get(0)
                .get("eut")
                .asLong());
        DataValue resolved = new DataRuntime(null, 0, provider).recipe(
            found.get("recipes")
                .get(0)
                .get("reference"));
        assertEquals(
            "stale",
            resolved.get("status")
                .asString());
        assertEquals(
            0,
            resolved.get("recipes")
                .size());
    }

    @Test
    void typedSearchAndResolutionPreserveCatalystsChancesAlternativesAndByproducts() {
        Fixture provider = new Fixture(recipe(24), recipe(96));
        QueryBudget budget = new QueryBudget();
        DataRuntime runtime = new DataRuntime(null, 1, provider, budget);
        DataValue result = new DataCompiler()
            .compile("recipes(itemkey(\"recipe_test:output\", 7), \"" + MAP + "\")", 1, 0)
            .evaluate(runtime);
        assertEquals(
            "matches",
            result.get("status")
                .asString());
        assertTrue(
            result.get("coverageComplete")
                .asBoolean());
        assertTrue(
            result.get("ambiguous")
                .asBoolean());
        assertEquals(
            2,
            result.get("recipes")
                .size());
        assertEquals(
            2,
            result.get("recipes")
                .get(0)
                .get("itemOutputCount")
                .asLong());
        assertSame(budget, provider.owner);

        DataCompiler compiler = new DataCompiler();
        compiler.beginTrigger();
        compiler.pushScope();
        compiler.declareLocal("found", RecipeValues.QUERY);
        runtime.write(false, 0, result);
        DataValue detail = compiler.compile("recipe(found.recipes[0].reference)", 1, 0)
            .evaluate(runtime);
        assertEquals(
            "matches",
            detail.get("status")
                .asString());
        DataValue row = detail.get("recipes")
            .get(0);
        assertEquals(
            24,
            row.get("eut")
                .asLong());
        assertEquals(
            0,
            row.get("itemInputs")
                .get(0)
                .get("amount")
                .asLong());
        assertEquals(
            3000,
            row.get("itemInputs")
                .get(0)
                .get("chance")
                .asLong());
        assertEquals(
            "ore_or_explicit",
            row.get("itemInputs")
                .get(0)
                .get("matching")
                .asString());
        assertTrue(
            row.get("itemInputs")
                .get(0)
                .get("nbtSensitive")
                .asBoolean());
        assertEquals(
            1,
            row.get("itemInputs")
                .get(0)
                .get("alternatives")
                .size());
        assertEquals(
            1,
            row.get("itemInputs")
                .get(0)
                .get("displayAlternatives")
                .size());
        assertEquals(
            2500,
            row.get("itemOutputs")
                .get(1)
                .get("chance")
                .asLong());
        assertEquals(
            4,
            row.get("itemOutputs")
                .get(1)
                .get("slot")
                .asLong());
        assertEquals(
            1000,
            row.get("fluidInputs")
                .get(0)
                .get("amount")
                .asLong());
        assertEquals(
            250,
            row.get("fluidInputs")
                .get(0)
                .get("alternatives")
                .get(0)
                .get("amount")
                .asLong());
        assertEquals(
            8000,
            row.get("fluidInputs")
                .get(0)
                .get("chance")
                .asLong());
        assertEquals(
            2000,
            row.get("fluidOutputs")
                .get(0)
                .get("chance")
                .asLong());
        assertEquals(
            0,
            compiler.compile("recipe(found.recipes[0].reference).recipes[0].itemInputs[0].amount", 1, 0)
                .evaluate(runtime)
                .asLong());
    }

    @Test
    void savedReferenceExpiresAndUnavailableStatusesNeverBecomeEmptySuccessfulSearches() {
        Fixture provider = new Fixture(recipe(24));
        DataRuntime runtime = new DataRuntime(null, 0, provider);
        DataValue original = runtime.recipes(OUTPUT, MAP);
        DataValue reference = original.get("recipes")
            .get(0)
            .get("reference");
        provider.catalog = new Fixture(recipe(24)).catalog;
        DataValue stale = runtime.recipe(reference);
        assertEquals(
            "stale",
            stale.get("status")
                .asString());
        assertFalse(
            stale.get("coverageComplete")
                .asBoolean());
        assertEquals(
            0,
            stale.get("recipes")
                .size());
        assertEquals(
            1,
            original.get("recipes")
                .size(),
            "Later catalog replacement cannot mutate an earlier view");
        Fixture unavailable = new Fixture();
        unavailable.catalog = null;
        DataValue refused = new DataRuntime(null, 0, unavailable).recipes(OUTPUT, MAP);
        assertEquals(
            "unsupported",
            refused.get("status")
                .asString());
        assertFalse(
            refused.get("coverageComplete")
                .asBoolean());
        assertEquals(
            0,
            refused.get("recipes")
                .size());
    }

    @Test
    void outputKeysAreExactAndPureConstructionDoesNotResolveARegistry() {
        DataRuntime runtime = new DataRuntime(null, 0);
        assertEquals(
            0,
            new DataCompiler().compile("itemkey(\"does_not_exist:valid\")", 1, 0)
                .evaluate(runtime)
                .asItemIdentity()
                .getMetadata());
        assertEquals(
            "ProjRed|Exploration:test",
            new DataCompiler().compile("itemkey(\"ProjRed|Exploration:test\", 4)", 1, 0)
                .evaluate(runtime)
                .asItemIdentity()
                .getRegistryId());
        Fixture provider = new Fixture(recipe(24));
        DataValue absent = new DataRuntime(null, 0, provider).recipes(key("recipe_test:output", 8), MAP);
        assertEquals(
            "no_match",
            absent.get("status")
                .asString());
        assertTrue(
            absent.get("coverageComplete")
                .asBoolean());
        for (String expression : new String[] { "itemkey(\"*\")", "itemkey(\"recipe_test:output\", 2147483648)" })
            assertThrows(
                IllegalArgumentException.class,
                () -> new DataCompiler().compile(expression, 1, 0)
                    .evaluate(new DataRuntime(null, 0)));
    }

    @Test
    void compileChecksArityTypesStateDefaultsAndReferenceArguments() {
        for (String expression : new String[] { "itemkey()", "itemkey(1)", "itemkey(\"a:b\", true)",
            "itemkey(\"a:b\", 0, 1)", "recipes(item(\"a:b\"), \"map\")", "recipes(itemkey(\"a:b\"))", "recipe(1)",
            "recipe({map:\"x\"})", "recipes(ref itemkey(\"a:b\"), \"map\")" })
            assertThrows(
                IllegalArgumentException.class,
                () -> new DataCompiler().compile(expression, 1, 0),
                expression);
        assertThrows(
            IllegalArgumentException.class,
            () -> new DataCompiler().declareState("bad", "recipes(itemkey(\"a:b\"), \"map\")", 1, 0));
    }

    @Test
    void oversizedDetailAndExhaustedWorkRefuseBeforePublishingAnyPartialView() {
        ItemIdentity largeName = key("test:" + String.join("", Collections.nCopies(250, "x")), 0);
        List<ItemPart> inputs = new ArrayList<>();
        for (int i = 0; i < 64; i++) inputs.add(part(i, largeName, 1, 10000, Matching.GT_NATIVE_ITEM));
        Fixture provider = new Fixture(descriptor(24, inputs, Collections.emptyList(), Collections.emptyList()));
        DataValue reference = new DataRuntime(null, 0, provider).recipes(OUTPUT, MAP)
            .get("recipes")
            .get(0)
            .get("reference");
        IllegalArgumentException oversized = assertThrows(
            IllegalArgumentException.class,
            () -> new DataRuntime(null, 0, provider).recipe(reference));
        assertTrue(
            oversized.getMessage()
                .contains("retained-value limits"));
        DataRuntime exhausted = new DataRuntime(null, 0, provider);
        exhausted.work(16383);
        assertThrows(IllegalArgumentException.class, () -> exhausted.recipes(OUTPUT, MAP));
    }

    @Test
    void playerFunctionsStillOverrideOnlyTheirExactBuiltinName() {
        DataCompiler compiler = new DataCompiler();
        SFMLParser parser = new SFMLParser(
            new CommonTokenStream(
                new SFMLLexer(CharStreams.fromString("integer recipes(integer n) { return n + 1 }"))));
        new FunctionCompiler(compiler).compile(
            parser.program()
                .functionDefinition());
        assertEquals(0, parser.getNumberOfSyntaxErrors());
        compiler.beginTrigger();
        compiler.pushScope();
        assertEquals(
            5,
            compiler.compile("recipes(4)", 1, 0)
                .evaluate(new DataRuntime(null, 0))
                .asLong());
        assertThrows(IllegalArgumentException.class, () -> compiler.compile("RECIPES(4)", 1, 0));
    }

    private static ItemIdentity key(String id, int metadata) {
        return ItemIdentity.of(id, metadata, null, new QueryBudget());
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

    private static RecipeDescriptor recipe(long eut) {
        ItemPart input = new ItemPart(
            0,
            key("recipe_test:catalyst", 1),
            0,
            3000,
            Matching.ORE_OR_EXPLICIT,
            true,
            "testOre",
            Collections.singletonList(key("recipe_test:alternative", 2)),
            Collections.singletonList(key("recipe_test:display", 3)));
        FluidIdentity water = FluidIdentity.fromStack(TestFluid.water(1000), new QueryBudget());
        FluidPart fluid = new FluidPart(
            0,
            water,
            1000,
            8000,
            Collections.singletonList(new FluidAlternative(water, 250)));
        return descriptor(
            eut,
            Collections.singletonList(input),
            Collections.singletonList(fluid),
            Collections.singletonList(new FluidPart(2, water, 100, 2000, Collections.emptyList())));
    }

    private static RecipeDescriptor descriptor(long eut, List<ItemPart> inputs, List<FluidPart> fluids,
        List<FluidPart> outputs) {
        return new RecipeDescriptor(
            "fixture-v1",
            MAP,
            eut,
            20,
            1,
            false,
            true,
            inputs,
            java.util.Arrays.asList(
                part(0, OUTPUT, 2, 10000, Matching.EXACT),
                part(4, key("recipe_test:byproduct", 0), 1, 2500, Matching.EXACT)),
            fluids,
            outputs,
            Collections.emptyList(),
            new QueryBudget(16));
    }

    private static final class Fixture implements DataQueries {

        RecipeCatalog catalog;
        RecipeCatalog replaceAfterFind;
        long tick = 1;
        QueryBudget owner;

        Fixture(RecipeDescriptor... recipes) {
            RecipeCatalog.Builder builder = new RecipeCatalog.Builder(UUID.randomUUID(), 1);
            builder.registerMap(MAP);
            for (RecipeDescriptor recipe : recipes) builder.add(recipe, new QueryBudget(16));
            catalog = builder.finish();
        }

        @Override
        public RecipeCatalog.Result recipes(ItemIdentity output, String map, QueryBudget budget) {
            owner = budget;
            RecipeCatalog.Result result = catalog == null ? DataQueries.super.recipes(output, map, budget)
                : catalog.find(output, map, budget, tick);
            if (replaceAfterFind != null) {
                catalog = replaceAfterFind;
                replaceAfterFind = null;
            }
            return result;
        }

        @Override
        public RecipeCatalog.Result recipe(RecipeCatalog.Token reference, QueryBudget budget) {
            return catalog == null ? DataQueries.super.recipe(reference, budget)
                : catalog.resolve(reference, budget, tick);
        }

        @Override
        public long count(LabelReference source, ResourceSelector selector, QueryBudget budget) {
            throw new AssertionError("Recipe views cannot query inventories");
        }

        @Override
        public boolean empty(LabelReference source, ResourceSelector selector, QueryBudget budget) {
            throw new AssertionError("Recipe views cannot query inventories");
        }

        @Override
        public boolean available(LabelReference source, ResourceSelector selector, QueryBudget budget) {
            throw new AssertionError("Recipe views cannot query inventories");
        }
    }
}
