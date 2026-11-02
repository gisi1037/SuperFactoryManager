package ca.teamdman.sfm.common.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

/** Startup-only catalog. Resolve exact GT parts at the optional boundary, never on a game tick. */
public final class LateHvRecipes {

    private LateHvRecipes() {}

    public static List<IRecipe> build(Map<String, ItemStack> outputs, Function<String, ItemStack> gt) {
        for (String ore : new String[] { "plateStainlessSteel", "circuitAdvanced", "foilElectrum", "wireFineElectrum" })
            if (net.minecraftforge.oredict.OreDictionary.getOres(ore, false)
                .isEmpty()) throw new IllegalArgumentException("Missing late-HV material: " + ore);
        // Resolve all parts first so missing dependencies cannot leave a half-built catalog or cheap fallback.
        ItemStack hull = part(gt.apply("Hull_HV")), screen = part(gt.apply("Cover_Screen")),
            motor = part(gt.apply("Electric_Motor_HV")), board = part(gt.apply("Circuit_Board_Epoxy_Advanced")),
            stick = part(gt.apply("Tool_DataStick")), sensor = part(gt.apply("Sensor_HV")),
            emitter = part(gt.apply("Emitter_HV"));
        ItemStack manager = part(outputs.get("manager")), cable = part(outputs.get("cable")),
            disk = part(outputs.get("disk")), gun = part(outputs.get("labelgun")),
            station = part(outputs.get("programmingStation")), port = part(outputs.get("redstonePort")),
            copier = part(outputs.get("diskCopier"));
        List<IRecipe> recipes = new ArrayList<>();
        recipes.add(
            new PlainShapedOreRecipe(
                manager,
                "PDP",
                "CHC",
                "PMP",
                'P',
                "plateStainlessSteel",
                'D',
                screen,
                'C',
                "circuitAdvanced",
                'H',
                hull,
                'M',
                motor));
        recipes.add(
            new PlainShapedOreRecipe(
                disk,
                "FBF",
                "WDW",
                "FCF",
                'F',
                "foilElectrum",
                'B',
                board,
                'W',
                "wireFineElectrum",
                'D',
                stick,
                'C',
                "circuitAdvanced"));
        recipes.add(
            new PlainShapedOreRecipe(
                gun,
                "DPS",
                " CE",
                "M  ",
                'P',
                "plateStainlessSteel",
                'S',
                sensor,
                'C',
                "circuitAdvanced",
                'E',
                emitter,
                'M',
                motor,
                'D',
                screen));
        recipes.add(
            new PlainShapedOreRecipe(
                station,
                "PDP",
                "BHB",
                "CMC",
                'P',
                "plateStainlessSteel",
                'D',
                screen,
                'B',
                board,
                'H',
                hull,
                'C',
                "circuitAdvanced",
                'M',
                manager));
        ItemStack single = cable.copy();
        single.stackSize = 1;
        recipes.add(
            new PlainShapedOreRecipe(
                port,
                "PSP",
                "CEC",
                "PWP",
                'P',
                "plateStainlessSteel",
                'S',
                sensor,
                'C',
                "circuitAdvanced",
                'E',
                emitter,
                'W',
                single));
        recipes.add(
            new PlainShapedOreRecipe(
                copier,
                "PSE",
                "BHB",
                "CDC",
                'P',
                "plateStainlessSteel",
                'S',
                sensor,
                'E',
                emitter,
                'B',
                board,
                'H',
                hull,
                'C',
                "circuitAdvanced",
                'D',
                screen));
        return recipes;
    }

    /** Same 4 plates/3 AE glass cables/2 bundled cables as SteveFM; rotate the layout to avoid competing outputs. */
    public static IRecipe cableRecipe(ItemStack output, ItemStack glassCable, ItemStack bundledCable) {
        if (net.minecraftforge.oredict.OreDictionary.getOres("plateStainlessSteel", false)
            .isEmpty())
            throw new IllegalArgumentException("Missing stainless-steel plates for the Inventory Cable recipe");
        ItemStack cable = part(output);
        cable.stackSize = 2;
        return new PlainShapedOreRecipe(
            cable,
            "PAP",
            "WAW",
            "PAP",
            'P',
            "plateStainlessSteel",
            'A',
            part(glassCable),
            'W',
            part(bundledCable)).setMirrored(false);
    }

    private static ItemStack part(ItemStack stack) {
        if (stack == null || stack.getItem() == null || stack.stackSize != 1 || !PlainConversionRecipe.plain(stack))
            throw new IllegalArgumentException("Missing or nonplain late-HV recipe component");
        return stack.copy();
    }
}
