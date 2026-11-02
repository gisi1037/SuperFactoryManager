package ca.teamdman.sfm.common.command;

import java.util.Locale;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;

import ca.teamdman.sfm.common.diagnostics.ManagerTickProfiler;
import ca.teamdman.sfm.common.recipe.RecipeCatalogService;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.QueryBudget;
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
        return "/sfmprofile [start|status|stop|recipes [<map> <item-id> <metadata> [candidate]]]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        String action = args.length == 0 ? "status" : args[0];
        if ("recipes".equals(action)) {
            if (args.length == 1) message(sender, RecipeCatalogService.INSTANCE.summary());
            else inspectRecipe(sender, args);
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
    private void inspectRecipe(ICommandSender sender, String[] args) {
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

    private static void message(ICommandSender sender, String text) {
        sender.addChatMessage(new ChatComponentText(text));
    }
}
