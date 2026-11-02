package ca.teamdman.sfm.common.command;

import java.util.Locale;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;

import ca.teamdman.sfm.common.diagnostics.ManagerTickProfiler;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.recipe.RecipeCatalogService;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.RecipeValues;
import ca.teamdman.sfml.data.recipe.RecipeCatalog;
import ca.teamdman.sfml.data.recipe.RecipeDescriptor;

/** Operator-only capture control. Profiling is disabled on every server start. */
public final class CommandSfmProfile extends CommandBase {

    @Override
    public String getCommandName() {
        return "sfmprofile";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/sfmprofile [start|status|stop|recipes|recipecheck] [<map> <item-id> <metadata> [candidate]]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        String action = args.length == 0 ? "status" : args[0];
        if ("recipes".equals(action) || "recipecheck".equals(action)) {
            if (args.length == 1) message(sender, RecipeCatalogService.INSTANCE.summary());
            else inspectRecipe(sender, args, "recipecheck".equals(action));
            return;
        }
        if (args.length > 1) throw new WrongUsageException(getCommandUsage(sender));
        ManagerTickProfiler profiler = ManagerTickProfiler.INSTANCE;
        if ("start".equals(action)) profiler.setEnabled(true);
        else if ("stop".equals(action)) profiler.setEnabled(false);
        else if (!"status".equals(action)) throw new WrongUsageException(getCommandUsage(sender));
        ManagerTickProfiler.Snapshot snapshot = profiler.snapshot();
        sender.addChatMessage(
            new ChatComponentText(
                String.format(
                    Locale.ROOT,
                    "SFM manager profiling %s: %d ticks, %d updates; total %.3f ms; per tick p50/p95/p99/max %.3f/%.3f/%.3f/%.3f ms",
                    snapshot.enabled ? "on" : "off",
                    snapshot.ticks,
                    snapshot.updates,
                    snapshot.totalNanos / 1e6,
                    snapshot.p50Nanos / 1e6,
                    snapshot.p95Nanos / 1e6,
                    snapshot.p99Nanos / 1e6,
                    snapshot.maxNanos / 1e6)));
    }

    /** Operator inspection uses the same bounded index as scripts, never native registries or input caches. */
    private void inspectRecipe(ICommandSender sender, String[] args, boolean checkLanguage) {
        if (args.length != 4 && args.length != 5) throw new WrongUsageException(getCommandUsage(sender));
        QueryBudget budget = new QueryBudget();
        ItemIdentity output = ItemIdentity.of(args[2], parseInt(sender, args[3]), null, budget);
        RecipeCatalog.Result result = RecipeCatalogService.INSTANCE.find(output, args[1], budget);
        message(
            sender,
            "Recipe query " + result.status
                + "; complete="
                + result.coverageComplete
                + "; candidates="
                + result.recipes.size()
                + "; exact output has no NBT");
        int index = args.length == 5 ? parseIntBounded(sender, args[4], 0, RecipeCatalog.MAX_RESULTS - 1) : 0;
        if (checkLanguage) checkLanguage(sender, output, args[1], index, result, budget);
        if (index >= result.recipes.size()) return;
        RecipeDescriptor recipe = result.recipes.get(index);
        message(
            sender,
            "Candidate " + index
                + " "
                + recipe.fingerprint.substring(0, 12)
                + "; EU/t="
                + recipe.eut
                + "; ticks="
                + recipe.duration
                + "; amperage="
                + recipe.amperage
                + "; hidden="
                + recipe.hidden
                + "; emptyOutput="
                + recipe.needsEmptyOutput
                + "; unsupported="
                + recipe.unsupportedRequirements.size());
        // Show at most two entries per group. Counts remain explicit; this preview never implies truncation is
        // complete.
        for (boolean input : new boolean[] { true, false }) {
            java.util.List<RecipeDescriptor.ItemPart> items = input ? recipe.itemInputs : recipe.itemOutputs;
            java.util.List<RecipeDescriptor.FluidPart> fluids = input ? recipe.fluidInputs : recipe.fluidOutputs;
            String kind = input ? "input" : "output";
            for (int i = 0; i < Math.min(2, items.size()); i++) {
                RecipeDescriptor.ItemPart part = items.get(i);
                message(
                    sender,
                    "Item " + kind
                        + " "
                        + (i + 1)
                        + "/"
                        + items.size()
                        + " slot="
                        + part.slot
                        + " "
                        + part.item.getRegistryId()
                        + ":"
                        + part.item.getMetadata()
                        + " x"
                        + part.amount
                        + "; chance="
                        + part.chance
                        + "/10000; "
                        + part.matching
                        + "; nbt="
                        + part.nbtSensitive
                        + "; ore="
                        + part.ore
                        + "; alternatives="
                        + part.alternatives.size()
                        + "; display="
                        + part.displayAlternatives.size());
            }
            for (int i = 0; i < Math.min(2, fluids.size()); i++) {
                RecipeDescriptor.FluidPart part = fluids.get(i);
                message(
                    sender,
                    "Fluid " + kind
                        + " "
                        + (i + 1)
                        + "/"
                        + fluids.size()
                        + " slot="
                        + part.slot
                        + " "
                        + part.fluid.getId()
                        + " "
                        + part.amount
                        + "mB; chance="
                        + part.chance
                        + "/10000; alternatives="
                        + part.alternatives.size());
            }
        }
    }

    /** Fixed read-only expressions exercise the real compiler/runtime/provider path; no player source is executed. */
    private static void checkLanguage(ICommandSender sender, ItemIdentity output, String map, int index,
        RecipeCatalog.Result expected, QueryBudget budget) {
        DataCompiler compiler = new DataCompiler();
        compiler.beginTrigger();
        compiler.pushScope();
        compiler.declareLocal("output", DataType.ITEM_KEY);
        compiler.declareLocal("map", DataType.STRING);
        compiler.declareLocal("found", RecipeValues.QUERY);
        // Recipe observations are world-free. No labels, network, inventory or manager authority is supplied.
        DataRuntime runtime = new DataRuntime(null, 3, new ProgramContext(null, null, null, null, 0, false), budget);
        runtime.write(false, 0, DataValue.itemIdentity(output));
        runtime.write(false, 1, DataValue.string(map));
        DataValue found = compiler.compile("recipes(output, map)", 1, 0)
            .evaluate(runtime);
        // This diagnostic makes two admitted searches. Other work can leave room for only the first.
        if (found.get("status")
            .asString()
            .equals("limit_exceeded") && expected.status != RecipeCatalog.Status.LIMIT_EXCEEDED) {
            message(sender, "SFML recipe view check inconclusive: shared catalog work limit; retry on another tick");
            return;
        }
        if (!found.get("status")
            .asString()
            .equals(
                expected.status.name()
                    .toLowerCase(Locale.ROOT))
            || found.get("coverageComplete")
                .asBoolean() != expected.coverageComplete
            || found.get("recipes")
                .size() != expected.recipes.size())
            throw new IllegalStateException("SFML recipe search differs from the native inspection index");
        runtime.write(false, 2, found);
        if (index < expected.recipes.size()) {
            DataValue detail = compiler.compile("recipe(found.recipes[" + index + "].reference)", 1, 0)
                .evaluate(runtime);
            if (detail.get("status")
                .asString()
                .equals("limit_exceeded")) {
                message(
                    sender,
                    "SFML recipe detail check inconclusive: shared catalog work limit; retry on another tick");
                return;
            }
            RecipeDescriptor descriptor = expected.recipes.get(index);
            if (detail.get("recipes")
                .size() != 1) throw new IllegalStateException("SFML recipe reference failed to resolve");
            DataValue row = detail.get("recipes")
                .get(0);
            if (row.get("eut")
                .asLong() != descriptor.eut
                || row.get("duration")
                    .asLong() != descriptor.duration
                || row.get("itemInputs")
                    .size() != descriptor.itemInputs.size()
                || row.get("itemOutputs")
                    .size() != descriptor.itemOutputs.size()
                || row.get("fluidInputs")
                    .size() != descriptor.fluidInputs.size()
                || row.get("fluidOutputs")
                    .size() != descriptor.fluidOutputs.size())
                throw new IllegalStateException("SFML recipe detail differs from the native inspection index");
        }
        message(sender, "SFML recipe view check passed; exact search and selected detail agree with catalog");
    }

    private static void message(ICommandSender sender, String text) {
        sender.addChatMessage(new ChatComponentText(text));
    }
}
