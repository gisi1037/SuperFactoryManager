package ca.teamdman.sfml.data;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import ca.teamdman.sfml.data.recipe.RecipeCatalog;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor.FluidAlternative;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor.FluidPart;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor.ItemPart;

/** Bounded immutable language views. Candidate summaries keep large ambiguous searches inexpensive. */
public final class RecipeValues {

    public static final DataType REFERENCE = shape(
        "adapter",
        DataType.STRING,
        "fingerprint",
        DataType.STRING,
        "map",
        DataType.STRING,
        "revision",
        DataType.INTEGER,
        "session",
        DataType.STRING);
    private static final DataType SUMMARY = shape(
        "amperage",
        DataType.INTEGER,
        "duration",
        DataType.INTEGER,
        "eut",
        DataType.INTEGER,
        "fluidInputCount",
        DataType.INTEGER,
        "fluidOutputCount",
        DataType.INTEGER,
        "hidden",
        DataType.BOOLEAN,
        "itemInputCount",
        DataType.INTEGER,
        "itemOutputCount",
        DataType.INTEGER,
        "needsEmptyOutput",
        DataType.BOOLEAN,
        "reference",
        REFERENCE,
        "unsupportedCount",
        DataType.INTEGER);
    private static final DataType ITEM_KEYS = DataType.dynamicArray(DataType.ITEM_KEY);
    private static final DataType ITEM_PART = shape(
        "alternatives",
        ITEM_KEYS,
        "amount",
        DataType.INTEGER,
        "chance",
        DataType.INTEGER,
        "displayAlternatives",
        ITEM_KEYS,
        "item",
        DataType.ITEM_KEY,
        "matching",
        DataType.STRING,
        "nbtSensitive",
        DataType.BOOLEAN,
        "ore",
        DataType.STRING,
        "slot",
        DataType.INTEGER);
    private static final DataType FLUID_ALTERNATIVE = shape("amount", DataType.INTEGER, "fluid", DataType.FLUID_KEY);
    private static final DataType FLUID_ALTERNATIVES = DataType.dynamicArray(FLUID_ALTERNATIVE);
    private static final DataType FLUID_PART = shape(
        "alternatives",
        FLUID_ALTERNATIVES,
        "amount",
        DataType.INTEGER,
        "chance",
        DataType.INTEGER,
        "fluid",
        DataType.FLUID_KEY,
        "slot",
        DataType.INTEGER);
    private static final DataType ITEM_PARTS = DataType.dynamicArray(ITEM_PART);
    private static final DataType FLUID_PARTS = DataType.dynamicArray(FLUID_PART);
    private static final DataType STRINGS = DataType.dynamicArray(DataType.STRING);
    private static final DataType DETAIL = shape(
        "amperage",
        DataType.INTEGER,
        "duration",
        DataType.INTEGER,
        "eut",
        DataType.INTEGER,
        "fluidInputs",
        FLUID_PARTS,
        "fluidOutputs",
        FLUID_PARTS,
        "hidden",
        DataType.BOOLEAN,
        "itemInputs",
        ITEM_PARTS,
        "itemOutputs",
        ITEM_PARTS,
        "needsEmptyOutput",
        DataType.BOOLEAN,
        "reference",
        REFERENCE,
        "unsupportedRequirements",
        STRINGS);
    private static final DataType SUMMARIES = DataType.dynamicArray(SUMMARY);
    private static final DataType DETAILS = DataType.dynamicArray(DETAIL);
    public static final DataType QUERY = resultType(SUMMARIES);
    public static final DataType RESOLUTION = resultType(DETAILS);

    private RecipeValues() {}

    private static DataType shape(Object... fields) {
        Map<String, DataType> types = new TreeMap<>();
        for (int i = 0; i < fields.length; i += 2) types.put((String) fields[i], (DataType) fields[i + 1]);
        return DataType.struct(types);
    }

    private static DataType resultType(DataType rows) {
        return shape(
            "ambiguous",
            DataType.BOOLEAN,
            "coverageComplete",
            DataType.BOOLEAN,
            "recipes",
            rows,
            "status",
            DataType.STRING);
    }

    /** No native callback occurs during sizing. Reserve the complete view before allocating any rows. */
    static DataValue materialize(RecipeCatalog.Result result, boolean detailed, QueryBudget owner,
        DataRuntime runtime) {
        runtime.requireQueryBudget(owner);
        if (result.recipes.size() > (detailed ? 1 : RecipeCatalog.MAX_RESULTS))
            throw new IllegalArgumentException("Recipe view result limit exceeded");
        DataType resultType = detailed ? RESOLUTION : QUERY;
        Size size = new Size(owner, runtime, resultType.getCellCount());
        size.text(result.status.name());
        size.scan(result.recipes.size());
        for (RecipeDescriptor recipe : result.recipes) {
            size.cells += (detailed ? DETAIL : SUMMARY).getCellCount();
            // UUID.toString has exactly 36 characters. Other reference strings already belong to this descriptor.
            size.text(recipe.adapter);
            size.text(recipe.map);
            size.text(recipe.fingerprint);
            size.strings += 36;
            size.retained += 36;
            if (detailed) {
                measureItems(recipe.itemInputs, size);
                measureItems(recipe.itemOutputs, size);
                measureFluids(recipe.fluidInputs, size);
                measureFluids(recipe.fluidOutputs, size);
                size.cells += recipe.unsupportedRequirements.size();
                size.scan(recipe.unsupportedRequirements.size());
                for (String code : recipe.unsupportedRequirements) size.text(code);
            }
        }
        size.reserve();
        DataValue[] rows = new DataValue[result.recipes.size()];
        for (int i = 0; i < rows.length; i++) {
            RecipeDescriptor recipe = result.recipes.get(i);
            RecipeCatalog.Token token = result.reference(i);
            if (!recipe.adapter.equals(token.adapter) || !recipe.map.equals(token.map)
                || !recipe.fingerprint.equals(token.fingerprint))
                throw new IllegalArgumentException("Recipe reference does not describe this candidate");
            DataValue reference = reference(token);
            // Fields are in DataType's alphabetical order; ownedCollection verifies the complete shape.
            rows[i] = detailed
                ? DataValue.ownedCollection(
                    DETAIL,
                    new DataValue[] { DataValue.integer(recipe.amperage), DataValue.integer(recipe.duration),
                        DataValue.integer(recipe.eut), fluids(recipe.fluidInputs), fluids(recipe.fluidOutputs),
                        DataValue.bool(recipe.hidden), items(recipe.itemInputs), items(recipe.itemOutputs),
                        DataValue.bool(recipe.needsEmptyOutput), reference, strings(recipe.unsupportedRequirements) })
                : DataValue.ownedCollection(
                    SUMMARY,
                    new DataValue[] { DataValue.integer(recipe.amperage), DataValue.integer(recipe.duration),
                        DataValue.integer(recipe.eut), DataValue.integer(recipe.fluidInputs.size()),
                        DataValue.integer(recipe.fluidOutputs.size()), DataValue.bool(recipe.hidden),
                        DataValue.integer(recipe.itemInputs.size()), DataValue.integer(recipe.itemOutputs.size()),
                        DataValue.bool(recipe.needsEmptyOutput), reference,
                        DataValue.integer(recipe.unsupportedRequirements.size()) });
        }
        return DataValue.ownedCollection(
            resultType,
            new DataValue[] { DataValue.bool(result.isAmbiguous()), DataValue.bool(result.coverageComplete),
                DataValue.ownedDynamicArray(detailed ? DETAILS : SUMMARIES, rows), DataValue.string(
                    result.status.name()
                        .toLowerCase(Locale.ROOT)) });
    }

    private static void measureItems(List<ItemPart> parts, Size size) {
        size.scan(parts.size());
        size.cells += parts.size() * ITEM_PART.getCellCount();
        for (ItemPart part : parts) {
            size.item(part.item);
            size.text(part.matching.name());
            size.text(part.ore);
            measureKeys(part.alternatives, size);
            measureKeys(part.displayAlternatives, size);
        }
    }

    private static void measureKeys(List<ItemIdentity> choices, Size size) {
        size.scan(choices.size());
        size.cells += choices.size();
        for (ItemIdentity key : choices) size.item(key);
    }

    private static void measureFluids(List<FluidPart> parts, Size size) {
        size.scan(parts.size());
        size.cells += parts.size() * FLUID_PART.getCellCount();
        for (FluidPart part : parts) {
            size.fluid(part.fluid);
            size.scan(part.alternatives.size());
            size.cells += part.alternatives.size() * FLUID_ALTERNATIVE.getCellCount();
            for (FluidAlternative alternative : part.alternatives) size.fluid(alternative.fluid);
        }
    }

    private static DataValue reference(RecipeCatalog.Token token) {
        return DataValue.ownedCollection(
            REFERENCE,
            new DataValue[] { DataValue.string(token.adapter), DataValue.string(token.fingerprint),
                DataValue.string(token.map), DataValue.integer(token.revision),
                DataValue.string(token.session.toString()) });
    }

    static RecipeCatalog.Token parseReference(DataValue value, QueryBudget budget, DataRuntime runtime) {
        if (!REFERENCE.equals(value.getType())) throw new IllegalArgumentException("Recipe reference required");
        int work = value.getRetainedCharacters() + REFERENCE.getCellCount();
        runtime.work(work);
        runtime.allocate(2);
        budget.chargeSlots(work);
        budget.chargeAllocation(2);
        String session = value.get("session")
            .asString();
        if (session.length() != 36) throw new IllegalArgumentException("Invalid recipe session");
        return new RecipeCatalog.Token(
            UUID.fromString(session),
            value.get("revision")
                .asLong(),
            value.get("adapter")
                .asString(),
            value.get("map")
                .asString(),
            value.get("fingerprint")
                .asString());
    }

    private static DataValue items(List<ItemPart> parts) {
        DataValue[] rows = new DataValue[parts.size()];
        for (int i = 0; i < rows.length; i++) {
            ItemPart part = parts.get(i);
            rows[i] = DataValue.ownedCollection(
                ITEM_PART,
                new DataValue[] { keys(part.alternatives), DataValue.integer(part.amount),
                    DataValue.integer(part.chance), keys(part.displayAlternatives), DataValue.itemIdentity(part.item),
                    DataValue.string(
                        part.matching.name()
                            .toLowerCase(Locale.ROOT)),
                    DataValue.bool(part.nbtSensitive), DataValue.string(part.ore), DataValue.integer(part.slot) });
        }
        return DataValue.ownedDynamicArray(ITEM_PARTS, rows);
    }

    private static DataValue keys(List<ItemIdentity> keys) {
        DataValue[] rows = new DataValue[keys.size()];
        for (int i = 0; i < rows.length; i++) rows[i] = DataValue.itemIdentity(keys.get(i));
        return DataValue.ownedDynamicArray(ITEM_KEYS, rows);
    }

    private static DataValue fluids(List<FluidPart> parts) {
        DataValue[] rows = new DataValue[parts.size()];
        for (int i = 0; i < rows.length; i++) {
            FluidPart part = parts.get(i);
            DataValue[] alternatives = new DataValue[part.alternatives.size()];
            for (int j = 0; j < alternatives.length; j++) {
                FluidAlternative choice = part.alternatives.get(j);
                alternatives[j] = DataValue.ownedCollection(
                    FLUID_ALTERNATIVE,
                    new DataValue[] { DataValue.integer(choice.amount), DataValue.fluidIdentity(choice.fluid) });
            }
            rows[i] = DataValue.ownedCollection(
                FLUID_PART,
                new DataValue[] { DataValue.ownedDynamicArray(FLUID_ALTERNATIVES, alternatives),
                    DataValue.integer(part.amount), DataValue.integer(part.chance), DataValue.fluidIdentity(part.fluid),
                    DataValue.integer(part.slot) });
        }
        return DataValue.ownedDynamicArray(FLUID_PARTS, rows);
    }

    private static DataValue strings(List<String> strings) {
        DataValue[] rows = new DataValue[strings.size()];
        for (int i = 0; i < rows.length; i++) rows[i] = DataValue.string(strings.get(i));
        return DataValue.ownedDynamicArray(STRINGS, rows);
    }

    private static final class Size {

        private final QueryBudget owner;
        private final DataRuntime runtime;
        private int cells, strings, retained, opaque;

        Size(QueryBudget owner, DataRuntime runtime, int cells) {
            this.owner = owner;
            this.runtime = runtime;
            this.cells = cells;
        }

        void scan(int count) {
            runtime.work(count);
            owner.chargeSlots(count);
        }

        void text(String text) {
            strings = Math.addExact(strings, text.length());
            retained = Math.addExact(retained, text.length());
        }

        void item(ItemIdentity item) {
            retained = Math.addExact(retained, item.getRetainedCharacters());
            opaque = Math.addExact(opaque, item.getRetainedOpaqueBytes());
        }

        void fluid(FluidIdentity fluid) {
            retained = Math.addExact(retained, fluid.getRetainedCharacters());
            opaque = Math.addExact(opaque, fluid.getRetainedOpaqueBytes());
        }

        void reserve() {
            if (cells > ProgramGlobals.MAX_CELLS || retained > ProgramGlobals.MAX_CHARACTERS
                || opaque > ProgramGlobals.MAX_OPAQUE_BYTES)
                throw new IllegalArgumentException(
                    "Recipe view exceeds retained-value limits; inspect one candidate at a time");
            runtime.allocate(cells);
            runtime.allocateString(strings);
            int work = Math.addExact(cells * 4, strings);
            runtime.work(work);
            owner.chargeSlots(work);
            owner.chargeAllocation(cells * 4);
        }
    }
}
