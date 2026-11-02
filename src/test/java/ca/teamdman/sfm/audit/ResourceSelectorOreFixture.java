package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.oredict.OreDictionary;

import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioFieldDraft;
import ca.teamdman.sfm.client.blueprint.StudioIoAllowances.Part;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.ast.BoolHas;
import ca.teamdman.sfml.ast.IfStatement;
import ca.teamdman.sfml.ast.InputStatement;
import ca.teamdman.sfml.ast.ResourceLimit;
import ca.teamdman.sfml.ast.ResourceLimits;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ItemTransferResult;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.FMLRelaunchLog;
import cpw.mods.fml.relauncher.Side;

/** Runs only inside the isolated LaunchClassLoader owned by ResourceSelectorOreTest. */
public final class ResourceSelectorOreFixture {

    public static void legacyForgePipeNamespacesCaptureAndMatchRealRegisteredStacks() throws Exception {
        String name = "ProjRed|Exploration:sfm_legacy_identity_fixture";
        Item nativeItem = new Item();
        // Forge chooses the active mod namespace; supplying a foreign prefix alone would prepend minecraft:.
        Field controllerField = Loader.class.getDeclaredField("modController");
        controllerField.setAccessible(true);
        Object previous = controllerField.get(Loader.instance());
        cpw.mods.fml.common.LoadController controller = new cpw.mods.fml.common.LoadController(Loader.instance());
        Field active = cpw.mods.fml.common.LoadController.class.getDeclaredField("activeContainer");
        active.setAccessible(true);
        cpw.mods.fml.common.ModMetadata metadata = new cpw.mods.fml.common.ModMetadata();
        metadata.modId = "ProjRed|Exploration";
        active.set(controller, new cpw.mods.fml.common.DummyModContainer(metadata));
        try {
            controllerField.set(Loader.instance(), controller);
            Item.itemRegistry.addObject(-1, "sfm_legacy_identity_fixture", nativeItem);
        } finally {
            controllerField.set(Loader.instance(), previous);
        }
        assertEquals(name, Item.itemRegistry.getNameForObject(nativeItem));
        ItemStack stack = new ItemStack(nativeItem, 1, 7);
        ca.teamdman.sfml.data.ItemIdentity captured = ca.teamdman.sfml.data.ItemIdentity
            .fromStack(stack, new QueryBudget());
        assertEquals(name, captured.getRegistryId());
        assertTrue(captured.matchesStack(stack, new QueryBudget()));
        assertTrue(
            ResourceSelector.item(name)
                .matches(stack, new QueryBudget()));
        assertFalse(
            ResourceSelector.item(name.toLowerCase(java.util.Locale.ROOT))
                .matches(stack, new QueryBudget()));
        assertFalse(captured.matchesStack(new ItemStack(nativeItem, 1, 8), new QueryBudget()));
    }

    public static void recipeAdapterPreservesDeclarations() throws Exception {
        ca.teamdman.sfm.compat.gregtech.RecipeAdapterFixture.snapshotsPreserveNativeDeclarationsWithoutMutatingCaches();
    }

    public static void recipeAdapterAlternativesAndCoverage() throws Exception {
        ca.teamdman.sfm.compat.gregtech.RecipeAdapterFixture.alternativesAndUnsupportedCoverageAreConservative();
    }

    public static void diskCopierPersistsMediaWithoutPendingCandidates() throws Exception {
        DiskCopierPersistenceFixture.roundTrip();
    }

    public static void cableConversionsHaveNoCompetingOutputs() {
        RecipeCraftingFixture.cableChoices();
    }

    public static void recipeConversionsPreserveDataAndExactMetadata() {
        RecipeCraftingFixture.conversions();
    }

    public static void lateHvRecipesCraftWithRealForgeOreMatching() {
        RecipeCraftingFixture.catalog();
    }

    private static final String METAL = "sfmOreSelectorMetal", SECONDARY = "sfmOreSelectorSecondary";
    private static Item wildcard, metadata, unrelated, late;

    public static void automaticReturnCannotClearReplacementOrReplayRemovedDisk() throws Exception {
        new FluidCallbackRecoveryTest().automaticReturnCannotClearReplacementOrReplayRemovedDisk();
    }

    public static void automaticReturnFencesAbaDiskReplacement() throws Exception {
        new FluidCallbackRecoveryTest().automaticReturnFencesAbaDiskReplacement();
    }

    public static void automaticReturnJournalsBeforeReentrantManualExecution() throws Exception {
        new FluidCallbackRecoveryTest().automaticReturnJournalsBeforeReentrantManualExecution();
    }

    public static void saveInsideThrowingAutomaticReturnRetainsUncertaintyAfterReload() throws Exception {
        new FluidCallbackRecoveryTest().saveInsideThrowingAutomaticReturnRetainsUncertaintyAfterReload();
    }

    public static void confirmedAutomaticReturnRestoresOnlyKnownRemainderAcrossSaveReload() throws Exception {
        new FluidCallbackRecoveryTest().confirmedAutomaticReturnRestoresOnlyKnownRemainderAcrossSaveReload();
    }

    public static void automaticReturnRejectsOwnerChangedDuringHandlerLookup() throws Exception {
        new FluidCallbackRecoveryTest().automaticReturnRejectsOwnerChangedDuringHandlerLookup();
    }

    public static void automaticReturnCannotClearReplacedRecordOnSameDisk() throws Exception {
        new FluidCallbackRecoveryTest().automaticReturnCannotClearReplacedRecordOnSameDisk();
    }

    public static void registerFixtures() throws ReflectiveOperationException {
        configureServerLogging();
        // OreDictionary initializes vanilla recipes: initialize their real inputs before first use.
        Loader.injectData("7", "10", "99", "99", "1.7.10", "9.05", new File("."), Collections.emptyList());
        Bootstrap.func_151354_b();
        wildcard = registerItem("wildcard");
        metadata = registerItem("metadata");
        unrelated = registerItem("unrelated");
        late = registerItem("late");
        OreDictionary.registerOre(METAL, new ItemStack(wildcard, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre(METAL, new ItemStack(metadata, 1, 4));
        OreDictionary.registerOre(SECONDARY, new ItemStack(metadata, 1, 4));
    }

    public static void studioLegacyOreFiltersKeepRealMembershipCaseUnknownAndFluidSemantics() {
        String clause = "INPUT FROM \"source\"";
        String with = StudioNodeProperties.update(Type.INPUT, clause, "oreFilter", "WITH #sfmoreselectormet*");
        String without = StudioNodeProperties.update(Type.INPUT, with, "oreFilter", "WITHOUT #sfmoreselectormet*");
        ResourceLimit included = legacyLimit(with), excluded = legacyLimit(without);
        assertTrue(included.matchesItem(new ItemStack(wildcard, 1, 7)));
        assertTrue(included.matchesItem(new ItemStack(metadata, 1, 4)));
        assertFalse(included.matchesItem(new ItemStack(metadata, 1, 3)));
        assertFalse(included.matchesItem(new ItemStack(unrelated, 1, 0)));
        assertFalse(excluded.matchesItem(new ItemStack(wildcard, 1, 7)));
        assertTrue(excluded.matchesItem(new ItemStack(unrelated, 1, 0)));
        String unknown = "sfmStudioNeverRegistered";
        String[] names = OreDictionary.getOreNames();
        assertFalse(legacyLimit("INPUT WITH #" + unknown + " FROM \"source\"").matchesItem(new ItemStack(wildcard)));
        assertTrue(legacyLimit("INPUT WITHOUT #" + unknown + " FROM \"source\"").matchesItem(new ItemStack(wildcard)));
        assertFalse(OreDictionary.doesOreNameExist(unknown));
        assertArrayEquals(names, OreDictionary.getOreNames());
        // A namespace-looking pattern is literal, not a mapping to a modern tag collection.
        assertFalse(legacyLimit("INPUT WITH #forge:ingots FROM \"source\"").matchesItem(new ItemStack(wildcard)));
        Fluid fluid = new Fluid("sfm_studio_filter_fluid");
        assertTrue(FluidRegistry.registerFluid(fluid));
        FluidStack stack = new FluidStack(fluid, 1000);
        assertTrue(included.matchesFluid(stack));
        assertTrue(excluded.matchesFluid(stack));
        assertTrue(legacyLimit(clause).matchesFluid(stack));
        assertTrue(legacyLimit("INPUT WITH #" + unknown + " FROM \"source\"").matchesFluid(stack));
    }

    private static ResourceLimit legacyLimit(String clause) {
        return legacyLimits(clause).getLimits()
            .get(0);
    }

    public static void studioGlobalExclusionsKeepRealItemMetadataAndFluidSemantics() {
        String clause = "INPUT FROM \"source\"";
        String excluded = StudioNodeProperties
            .update(Type.INPUT, clause, "exclusion", "\"item:minecraft:sfm_ore_selector_test_metadata:4\"");
        ResourceLimits filtered = legacyLimits(excluded);
        assertFalse(filtered.matchesItem(new ItemStack(metadata, 1, 4)));
        assertTrue(filtered.matchesItem(new ItemStack(metadata, 1, 3)));
        assertTrue(filtered.matchesItem(new ItemStack(wildcard, 1, 7)));
        assertTrue(
            legacyLimits(StudioNodeProperties.update(Type.INPUT, excluded, "exclusion", ""))
                .matchesItem(new ItemStack(metadata, 1, 4)));
        Fluid fluid = new Fluid("sfm_studio_exclusion_fluid");
        assertTrue(FluidRegistry.registerFluid(fluid));
        FluidStack stack = new FluidStack(fluid, 1000);
        assertTrue(filtered.matchesFluid(stack));
        String fluidExcluded = StudioNodeProperties
            .update(Type.INPUT, excluded, "exclusion", "fluid:sfm_studio_exclusion_fluid");
        assertFalse(legacyLimits(fluidExcluded).matchesFluid(stack));
        assertTrue(legacyLimits(fluidExcluded).matchesItem(new ItemStack(metadata, 1, 4)));
        assertTrue(
            legacyLimits(StudioNodeProperties.update(Type.INPUT, fluidExcluded, "exclusion", "")).matchesFluid(stack));
    }

    private static ResourceLimits legacyLimits(String clause) {
        ProgramBuildResult built = new ProgramBuilder("every 20 ticks do " + clause + " end").build();
        assertTrue(built.isSuccess(), built.getErrorMessage());
        return ((InputStatement) built.getProgram()
            .getTriggers()
            .get(0)
            .getBlock()
            .getStatements()
            .get(0)).getResourceLimits();
    }

    public static void studioExclusionRowsRetainRealMetadataAndMixedItemFluidMatching() {
        Fluid fluid = new Fluid("sfm_studio_exclusion_rows_fluid");
        assertTrue(FluidRegistry.registerFluid(fluid));
        String clause = "INPUT FROM \"source\"";
        String metadataSelector = "\"item:minecraft:sfm_ore_selector_test_metadata:4\"";
        String both = StudioNodeProperties.updateExclusions(
            Type.INPUT,
            clause,
            metadataSelector + ",fluid:sfm_studio_exclusion_rows_fluid," + metadataSelector);
        ResourceLimits filtered = legacyLimits(both);
        assertEquals(
            3,
            filtered.getExclusions()
                .getIdentifiers()
                .size());
        assertFalse(filtered.matchesItem(new ItemStack(metadata, 1, 4)));
        assertTrue(filtered.matchesItem(new ItemStack(metadata, 1, 3)));
        assertTrue(filtered.matchesItem(new ItemStack(unrelated)));
        assertFalse(filtered.matchesFluid(new FluidStack(fluid, 1000)));
        String itemsOnly = StudioNodeProperties.updateExclusions(Type.INPUT, both, metadataSelector);
        assertFalse(legacyLimits(itemsOnly).matchesItem(new ItemStack(metadata, 1, 4)));
        assertTrue(legacyLimits(itemsOnly).matchesFluid(new FluidStack(fluid, 1000)));
        String removed = StudioNodeProperties.updateExclusions(Type.INPUT, both, "");
        assertTrue(legacyLimits(removed).matchesItem(new ItemStack(metadata, 1, 4)));
        assertTrue(legacyLimits(removed).matchesFluid(new FluidStack(fluid, 1000)));
    }

    public static void studioHasRowsKeepRealCountsSetOperatorsDuplicatesAndUnavailableDefaults() throws Exception {
        Fixture fixture = new Fixture();
        fixture.source.setInventorySlotContents(0, new ItemStack(wildcard, 3, 7));
        fixture.source.setInventorySlotContents(1, new ItemStack(metadata, 5, 4));
        fixture.dest.setInventorySlotContents(0, new ItemStack(wildcard, 2, 7));
        fixture.dest.setInventorySlotContents(1, new ItemStack(unrelated, 4));
        ProgramContext context = transferContext(fixture);
        String[] operators = { "OVERALL", "SOME", "EVERY", "EACH", "ONE", "LONE" };
        boolean[] expected = { true, true, false, false, true, true };
        String selector = "item:minecraft:sfm_ore_selector_test_wildcard OR \"item:minecraft:sfm_ore_selector_test_metadata:4\"";
        for (int i = 0; i < operators.length; i++) {
            String clause = "(" + operators[i]
                + " \"source\" HAS >= 8 "
                + selector
                + " WITH #"
                + METAL
                + " EXCEPT item:minecraft:sfm_ore_selector_test_unrelated)";
            String grouped = StudioNodeProperties.update(Type.IF, clause, "label", "\"source\",\"dest\",\"source\"");
            grouped = StudioNodeProperties.updateResourceAlternatives(Type.IF, grouped, selector + " OR " + selector);
            BoolHas has = legacyHas(grouped);
            assertEquals(expected[i], has.evaluate(context), grouped);
            assertEquals(8, has.getQuantity());
            assertEquals(
                4,
                has.getResourceIds()
                    .getIdentifiers()
                    .size());
            assertEquals(
                3,
                has.getLabelAccess()
                    .getLabels()
                    .size());
            // Duplicate references/selectors cannot multiply counts: both inventories total ten matching items.
            String overall = grouped.replace(operators[i], "OVERALL");
            overall = StudioNodeProperties.update(Type.IF, overall, "comparator", "=");
            overall = StudioNodeProperties.update(Type.IF, overall, "amount", "10");
            assertTrue(legacyHas(overall).evaluate(context), overall);
            assertFalse(legacyHas(StudioNodeProperties.update(Type.IF, overall, "amount", "11")).evaluate(context));
            assertTrue(
                legacyHas(StudioNodeProperties.updateResourceAlternatives(Type.IF, overall, "")).evaluate(context));
        }
        String slot = "OVERALL \"source\",\"dest\" SLOTS 0 HAS = 5 " + selector;
        assertTrue(legacyHas(slot).evaluate(context));
        assertFalse(legacyHas(StudioNodeProperties.update(Type.IF, slot, "amount", "10")).evaluate(context));
        for (String set : operators) {
            String clause = set + " \"missing\" HAS = 0";
            String same = StudioNodeProperties.updateResourceAlternatives(Type.IF, clause, "item:*");
            assertEquals(legacyHas(clause).evaluate(context), legacyHas(same).evaluate(context));
        }
        assertEquals(8, count(fixture.source));
        assertEquals(6, count(fixture.dest));
    }

    private static BoolHas legacyHas(String condition) throws ReflectiveOperationException {
        ProgramBuildResult built = new ProgramBuilder("every 20 ticks do if " + condition + " then forget end end")
            .build();
        assertTrue(built.isSuccess(), built.getErrorMessage());
        ca.teamdman.sfml.ast.BoolExpr expression = ((IfStatement) built.getProgram()
            .getTriggers()
            .get(0)
            .getBlock()
            .getStatements()
            .get(0)).getCondition();
        // Inspect the existing wrapper only in this fixture; no runtime API is added for editor tests.
        while (expression instanceof ca.teamdman.sfml.ast.BoolParen) {
            Field inner = expression.getClass()
                .getDeclaredField("inner");
            inner.setAccessible(true);
            expression = (ca.teamdman.sfml.ast.BoolExpr) inner.get(expression);
        }
        return (BoolHas) expression;
    }

    public static void studioIndependentLimitRowsKeepItemAllowancesOreMetadataAndOverlapPriority() throws Exception {
        Fixture fixture = new Fixture();
        fixture.source.setInventorySlotContents(0, new ItemStack(wildcard, 7, 7));
        fixture.source.setInventorySlotContents(1, new ItemStack(metadata, 5, 4));
        fixture.source.setInventorySlotContents(2, new ItemStack(unrelated, 9));
        String union = "item:minecraft:sfm_ore_selector_test_wildcard OR \"item:minecraft:sfm_ore_selector_test_metadata:4\"";
        String unrelatedSelector = "item:minecraft:sfm_ore_selector_test_unrelated";
        String input = StudioNodeProperties.updateIndependentLimits(
            Type.INPUT,
            "INPUT item:a,item:b FROM \"source\"",
            "4 RETAIN 1 EACH " + union + " WITH #" + METAL + ",3 " + unrelatedSelector);
        String output = StudioNodeProperties.updateIndependentLimits(
            Type.OUTPUT,
            "OUTPUT item:a,item:b TO \"dest\"",
            "2 EACH RETAIN 5 EACH " + union + ",2 RETAIN 4 " + unrelatedSelector);
        ProgramContext context = transferContext(fixture);
        parse("every 20 ticks do " + input + " " + output + " end").tick(context);
        assertEquals(6, count(fixture.dest));
        assertEquals(15, count(fixture.source));
        assertEquals(21, count(fixture.dest) + count(fixture.source));
        assertEquals(5, fixture.source.getStackInSlot(0).stackSize);
        assertEquals(3, fixture.source.getStackInSlot(1).stackSize);
        assertEquals(7, fixture.source.getStackInSlot(2).stackSize);
        for (boolean broadFirst : new boolean[] { true, false }) {
            Fixture overlap = new Fixture();
            overlap.source.setInventorySlotContents(0, new ItemStack(wildcard, 10, 7));
            String precise = "9 item:minecraft:sfm_ore_selector_test_wildcard";
            String rows = broadFirst ? "2 item:*," + precise : precise + ",2 item:*";
            String edited = StudioNodeProperties
                .updateIndependentLimits(Type.INPUT, "INPUT item:a,item:b FROM \"source\"", rows);
            parse("every 20 ticks do " + edited + " OUTPUT TO \"dest\" end").tick(transferContext(overlap));
            assertEquals(broadFirst ? 2 : 9, count(overlap.dest));
            assertEquals(10, count(overlap.source) + count(overlap.dest));
        }
    }

    public static void studioIndependentLimitRowsKeepRealFluidMatchingPriorityRemovalAndConservation()
        throws Exception {
        Fluid fluid = new Fluid("sfm_studio_independent_fluid");
        assertTrue(FluidRegistry.registerFluid(fluid));
        String selector = "fluid:sfm_studio_independent_fluid";
        for (int mode = 0; mode < 3; mode++) {
            Fixture fixture = new Fixture();
            LimitTank source = new LimitTank(fluid, 1000), dest = new LimitTank(fluid, 0);
            fixture.world.tiles.put(SRC, source);
            fixture.world.tiles.put(DST, dest);
            String rows = mode == 1 ? "50 fluid:*,200 RETAIN 100 " + selector
                : "200 RETAIN 100 " + selector + " WITH #sfmUnknown,50 fluid:*";
            String input = StudioNodeProperties
                .updateIndependentLimits(Type.INPUT, "INPUT fluid:a,fluid:b FROM \"source\"", rows);
            if (mode == 2) input = StudioNodeProperties.updateIndependentLimits(Type.INPUT, input, "50 fluid:*");
            assertTrue(legacyLimits(input).matchesFluid(new FluidStack(fluid, 1)));
            parse("every 20 ticks do " + input + " OUTPUT 150 " + selector + " TO \"dest\" end")
                .tick(transferContext(fixture));
            assertEquals(mode == 0 ? 150 : 50, dest.tank.getFluidAmount());
            assertEquals(1000, source.tank.getFluidAmount() + dest.tank.getFluidAmount());
        }
    }

    public static void studioSingleLimitPromotionKeepsRealItemMetadataQuotasAndOverlapPriority() throws Exception {
        Fixture fixture = new Fixture();
        fixture.source.setInventorySlotContents(0, new ItemStack(wildcard, 7, 7));
        fixture.source.setInventorySlotContents(1, new ItemStack(metadata, 5, 4));
        fixture.source.setInventorySlotContents(2, new ItemStack(unrelated, 9));
        String union = "item:minecraft:sfm_ore_selector_test_wildcard OR \"item:minecraft:sfm_ore_selector_test_metadata:4\"";
        String other = "item:minecraft:sfm_ore_selector_test_unrelated";
        String input = promotedClause(
            Type.INPUT,
            "INPUT 9 RETAIN 1 EACH " + union + " WITH #" + METAL + " FROM \"source\"",
            "4",
            "3 " + other,
            false);
        String output = promotedClause(
            Type.OUTPUT,
            "OUTPUT 8 EACH RETAIN 5 EACH " + union + " TO \"dest\"",
            "2",
            "2 RETAIN 4 " + other,
            false);
        parse("every 20 ticks do " + input + " " + output + " end").tick(transferContext(fixture));
        assertEquals(6, count(fixture.dest));
        assertEquals(15, count(fixture.source));
        assertEquals(21, count(fixture.dest) + count(fixture.source));
        assertEquals(5, fixture.source.getStackInSlot(0).stackSize);
        assertEquals(3, fixture.source.getStackInSlot(1).stackSize);
        assertEquals(7, fixture.source.getStackInSlot(2).stackSize);
        for (boolean broadFirst : new boolean[] { true, false }) {
            Fixture overlap = new Fixture();
            overlap.source.setInventorySlotContents(0, new ItemStack(wildcard, 10, 7));
            String precise = "9 item:minecraft:sfm_ore_selector_test_wildcard";
            String first = broadFirst ? "2 item:*" : precise;
            String second = broadFirst ? precise : "2 item:*";
            String edited = promotedClause(Type.INPUT, "INPUT " + first + " FROM \"source\"", null, second, false);
            parse("every 20 ticks do " + edited + " OUTPUT TO \"dest\" end").tick(transferContext(overlap));
            assertEquals(broadFirst ? 2 : 9, count(overlap.dest));
            assertEquals(10, count(overlap.source) + count(overlap.dest));
        }
    }

    public static void studioSingleLimitPromotionKeepsFluidDefaultsRetentionOrderAndTwoToOne() throws Exception {
        Fluid fluid = new Fluid("sfm_studio_promoted_fluid");
        assertTrue(FluidRegistry.registerFluid(fluid));
        String specific = "200 RETAIN 100 fluid:sfm_studio_promoted_fluid WITH #sfmUnknown";
        for (int mode = 0; mode < 3; mode++) {
            Fixture fixture = new Fixture();
            LimitTank source = new LimitTank(fluid, 1000), dest = new LimitTank(fluid, 0);
            fixture.world.tiles.put(SRC, source);
            fixture.world.tiles.put(DST, dest);
            String input = promotedClause(
                Type.INPUT,
                "INPUT " + (mode == 1 ? "50 fluid:*" : specific) + " FROM \"source\"",
                null,
                mode == 1 ? specific : "50 fluid:*",
                mode == 2);
            assertTrue(legacyLimits(input).matchesFluid(new FluidStack(fluid, 1)));
            parse("every 20 ticks do " + input + " OUTPUT 150 fluid:sfm_studio_promoted_fluid TO \"dest\" end")
                .tick(transferContext(fixture));
            assertEquals(mode == 0 ? 150 : 50, dest.tank.getFluidAmount());
            assertEquals(1000, source.tank.getFluidAmount() + dest.tank.getFluidAmount());
        }
    }

    private static String promotedClause(Type type, String clause, String amount, String second, boolean removeFirst) {
        UUID project = new UUID(15, 1037);
        Node node = new Node(UUID.randomUUID(), type, 0, 0, clause);
        String source = "every 20 ticks do " + clause + " end";
        StudioFieldDraft typed = new StudioFieldDraft(
            project,
            node,
            source,
            StudioNodeProperties.inspect(type, clause));
        if (amount != null) typed.set("amount", amount);
        StudioFieldDraft rows = typed.addIndependentLimit(project, node, source);
        rows.setLimit(
            rows.limitRows()
                .get(1).id,
            second);
        if (removeFirst) rows.removeLimit(
            rows.limitRows()
                .get(0).id);
        return rows.appliedClause(project, node, source);
    }

    public static void studioOptionalIoNumbersKeepRealItemReserveTargetEachAndConservation() throws Exception {
        String selector = "item:minecraft:sfm_ore_selector_test_wildcard";
        int[] moved = { 5, 8, 0, 5, 0, 3, 20 };
        for (int mode = 0; mode < moved.length; mode++) {
            Fixture fixture = new Fixture();
            int stocked = mode >= 3 ? 5 : 0;
            fixture.source.setInventorySlotContents(0, new ItemStack(wildcard, 20, 7));
            if (stocked > 0) fixture.dest.setInventorySlotContents(0, new ItemStack(wildcard, stocked, 7));
            String input = optionalClause(
                Type.INPUT,
                "INPUT " + selector + " FROM \"source\"",
                mode == 0 || mode == 1 ? "8" : mode == 2 ? "0" : null,
                false,
                mode == 0 ? "15" : null,
                false);
            String output = optionalClause(
                Type.OUTPUT,
                "OUTPUT " + selector + " TO \"dest\"",
                mode == 5 ? "3" : null,
                false,
                mode == 3 || mode == 5 ? "10" : mode == 4 ? "0" : null,
                false);
            parse("every 20 ticks do " + input + " " + output + " end").tick(transferContext(fixture));
            assertEquals(stocked + moved[mode], count(fixture.dest));
            assertEquals(20 + stocked, count(fixture.source) + count(fixture.dest));
        }
        String union = selector + " OR \"item:minecraft:sfm_ore_selector_test_metadata:4\"";
        for (boolean retainEach : new boolean[] { false, true }) {
            Fixture fixture = new Fixture();
            fixture.source.setInventorySlotContents(0, new ItemStack(wildcard, 4, 7));
            fixture.source.setInventorySlotContents(1, new ItemStack(metadata, 4, 4));
            String input = optionalClause(
                Type.INPUT,
                "INPUT " + union + " FROM \"source\"",
                "3",
                true,
                "3",
                retainEach);
            parse("every 20 ticks do " + input + " OUTPUT TO \"dest\" end").tick(transferContext(fixture));
            assertEquals(retainEach ? 2 : 4, count(fixture.dest));
            assertEquals(8, count(fixture.source) + count(fixture.dest));
        }
    }

    public static void studioOptionalIoNumbersKeepRealFluidMillibucketsDefaultsZeroAndConservation() throws Exception {
        Fluid fluid = new Fluid("sfm_studio_optional_numbers_fluid");
        assertTrue(FluidRegistry.registerFluid(fluid));
        String selector = "fluid:sfm_studio_optional_numbers_fluid";
        int[] moved = { 700, 500, 200, 0, 0, 3000 };
        for (int mode = 0; mode < moved.length; mode++) {
            Fixture fixture = new Fixture();
            LimitTank source = new LimitTank(fluid, 3000), dest = new LimitTank(fluid, 500);
            fixture.world.tiles.put(SRC, source);
            fixture.world.tiles.put(DST, dest);
            String input = optionalClause(
                Type.INPUT,
                "INPUT " + selector + " FROM \"source\"",
                mode == 0 ? "800" : mode == 4 ? "0" : null,
                false,
                mode == 0 ? "2300" : null,
                false);
            String output = optionalClause(
                Type.OUTPUT,
                "OUTPUT " + selector + " TO \"dest\"",
                mode == 2 ? "200" : null,
                false,
                mode == 1 ? "1000" : mode == 3 ? "0" : null,
                false);
            parse("every 20 ticks do " + input + " " + output + " end").tick(transferContext(fixture));
            assertEquals(500 + moved[mode], dest.tank.getFluidAmount());
            assertEquals(3500, source.tank.getFluidAmount() + dest.tank.getFluidAmount());
        }
    }

    public static void studioAbsentIoNumbersKeepRealItemDefaultsZeroReserveCapAndConservation() throws Exception {
        int[] moved = { 5, 8, 0, 5, 0, 3, 20 };
        for (int mode = 0; mode < moved.length; mode++) {
            Fixture fixture = new Fixture();
            int stocked = mode >= 3 ? 5 : 0;
            fixture.source.setInventorySlotContents(0, new ItemStack(wildcard, 20, 7));
            if (stocked > 0) fixture.dest.setInventorySlotContents(0, new ItemStack(wildcard, stocked, 7));
            String input = optionalClause(
                Type.INPUT,
                "FROM \"source\" INPUT",
                mode <= 1 ? "8" : mode == 2 ? "0" : null,
                false,
                mode == 0 ? "15" : null,
                false);
            String output = optionalClause(
                Type.OUTPUT,
                "OUTPUT TO \"dest\"",
                mode == 5 ? "3" : null,
                false,
                mode == 3 || mode == 5 ? "10" : mode == 4 ? "0" : null,
                false);
            parse("every 20 ticks do " + input + " " + output + " end").tick(transferContext(fixture));
            assertEquals(stocked + moved[mode], count(fixture.dest));
            assertEquals(20 + stocked, count(fixture.source) + count(fixture.dest));
        }
    }

    public static void studioAbsentIoNumbersKeepRealFluidMillibucketsAndConservation() throws Exception {
        Fluid fluid = new Fluid("sfm_studio_absent_numbers_fluid");
        assertTrue(FluidRegistry.registerFluid(fluid));
        int[] moved = { 700, 500, 200, 0, 0, 3000 };
        for (int mode = 0; mode < moved.length; mode++) {
            Fixture fixture = new Fixture();
            LimitTank source = new LimitTank(fluid, 3000), dest = new LimitTank(fluid, 500);
            fixture.world.tiles.put(SRC, source);
            fixture.world.tiles.put(DST, dest);
            String input = optionalClause(
                Type.INPUT,
                "INPUT FROM \"source\"",
                mode == 0 ? "800" : mode == 4 ? "0" : null,
                false,
                mode == 0 ? "2300" : null,
                false);
            String output = optionalClause(
                Type.OUTPUT,
                "TO \"dest\" OUTPUT",
                mode == 2 ? "200" : null,
                false,
                mode == 1 ? "1000" : mode == 3 ? "0" : null,
                false);
            parse("every 20 ticks do " + input + " " + output + " end").tick(transferContext(fixture));
            assertEquals(500 + moved[mode], dest.tank.getFluidAmount());
            assertEquals(3500, source.tank.getFluidAmount() + dest.tank.getFluidAmount());
        }
    }

    private static String optionalClause(Type type, String clause, String amount, boolean amountEach, String retain,
        boolean retainEach) {
        UUID project = new UUID(16, 1037);
        Node node = new Node(UUID.randomUUID(), type, 0, 0, clause);
        String source = "every 20 ticks do " + clause + " end";
        StudioFieldDraft draft = new StudioFieldDraft(
            project,
            node,
            source,
            StudioNodeProperties.inspect(type, clause));
        draft.setAllowanceEnabled(Part.AMOUNT, amount != null);
        draft.setAllowanceEnabled(Part.RETAIN, retain != null);
        if (amount != null) draft.setAllowanceText(Part.AMOUNT, amount);
        if (retain != null) draft.setAllowanceText(Part.RETAIN, retain);
        draft.setAllowanceEach(Part.AMOUNT, amountEach);
        draft.setAllowanceEach(Part.RETAIN, retainEach);
        return draft.appliedClause(project, node, source);
    }

    private static final class LimitTank extends TileEntity implements IFluidHandler {

        final FluidTank tank;

        LimitTank(Fluid fluid, int amount) {
            tank = new FluidTank(new FluidStack(fluid, amount), 10000);
        }

        @Override
        public int fill(ForgeDirection side, FluidStack fluid, boolean actual) {
            return tank.fill(fluid, actual);
        }

        @Override
        public FluidStack drain(ForgeDirection side, FluidStack fluid, boolean actual) {
            return fluid != null && tank.getFluid() != null
                && tank.getFluid()
                    .isFluidEqual(fluid) ? tank.drain(fluid.amount, actual) : null;
        }

        @Override
        public FluidStack drain(ForgeDirection side, int amount, boolean actual) {
            return tank.drain(amount, actual);
        }

        @Override
        public boolean canFill(ForgeDirection side, Fluid fluid) {
            return true;
        }

        @Override
        public boolean canDrain(ForgeDirection side, Fluid fluid) {
            return true;
        }

        @Override
        public FluidTankInfo[] getTankInfo(ForgeDirection side) {
            return new FluidTankInfo[] { tank.getInfo() };
        }
    }

    private static void configureServerLogging() throws ReflectiveOperationException {
        // The pinned Forge API has package-private launch-side fields and no side-only public setter.
        // Set the real child-runtime logging side without starting coremod discovery or modifying Loader.
        Field loggingSide = FMLRelaunchLog.class.getDeclaredField("side");
        loggingSide.setAccessible(true);
        loggingSide.set(null, Side.SERVER);
        PrintStream originalOut = System.out, originalErr = System.err;
        try {
            FMLLog.fine("Initializing isolated server-side ore selector fixture");
        } finally {
            // Normal FML logging installs tracing streams; keep that JVM-global effect out of other JUnit tests.
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }

    public static void studioResourceAlternativesShareOneLimitAndKeepMixedUnionAndExclusions() {
        Fluid fluid = new Fluid("sfm_studio_alternative_fluid");
        assertTrue(FluidRegistry.registerFluid(fluid));
        String clause = "INPUT 16 EACH RETAIN 2 EACH EXCEPT item:minecraft:sfm_ore_selector_test_unrelated FROM \"source\"";
        String metadataSelector = "\"item:minecraft:sfm_ore_selector_test_metadata:4\"";
        String selected = StudioNodeProperties.updateResourceAlternatives(
            Type.INPUT,
            clause,
            metadataSelector
                + " OR item:minecraft:sfm_ore_selector_test_wildcard OR fluid:sfm_studio_alternative_fluid OR "
                + metadataSelector);
        ResourceLimits union = legacyLimits(selected);
        assertEquals(
            1,
            union.getLimits()
                .size());
        ResourceLimit common = union.getLimits()
            .get(0);
        assertEquals(
            4,
            common.getResourceIds()
                .getIdentifiers()
                .size());
        assertEquals(
            16,
            common.getLimit()
                .getQuantity()
                .getAmount());
        assertTrue(
            common.getLimit()
                .getQuantity()
                .isEach());
        assertEquals(
            2,
            common.getLimit()
                .getRetention()
                .getAmount());
        assertTrue(
            common.getLimit()
                .getRetention()
                .isEach());
        assertTrue(union.matchesItem(new ItemStack(metadata, 1, 4)));
        assertFalse(union.matchesItem(new ItemStack(metadata, 1, 3)));
        assertTrue(union.matchesItem(new ItemStack(wildcard, 1, 7)));
        assertFalse(union.matchesItem(new ItemStack(unrelated)));
        assertTrue(union.matchesFluid(new FluidStack(fluid, 1000)));
        String removed = StudioNodeProperties.updateResourceAlternatives(Type.INPUT, selected, "");
        assertEquals(
            1,
            legacyLimits(removed).getLimits()
                .size());
        assertTrue(legacyLimits(removed).matchesItem(new ItemStack(metadata, 1, 3)));
        assertFalse(legacyLimits(removed).matchesItem(new ItemStack(unrelated)));
        assertEquals(
            16,
            legacyLimits(removed).getLimits()
                .get(0)
                .getLimit()
                .getQuantity()
                .getAmount());
    }

    @SuppressWarnings("deprecation")
    private static Item registerItem(String name) {
        Item item = new Item();
        // No mod-loading lifecycle is needed; Forge allocates an unused item ID and names its delegate.
        Item.itemRegistry.addObject(-1, "minecraft:sfm_ore_selector_test_" + name, item);
        return item;
    }

    public static void wildcardRegistrationMatchesEveryMetadataWhileExactRegistrationRemainsSpecific() {
        ResourceSelector selector = ResourceSelector.ore(METAL);
        assertTrue(selector.matches(new ItemStack(wildcard, 1, 0)));
        assertTrue(selector.matches(new ItemStack(wildcard, 1, 7)));
        assertTrue(selector.matches(new ItemStack(wildcard, 1, OreDictionary.WILDCARD_VALUE)));
        assertTrue(selector.matches(new ItemStack(metadata, 1, 4)));
        assertFalse(selector.matches(new ItemStack(metadata, 1, 3)));
        assertFalse(selector.matches(new ItemStack(unrelated, 1, 4)));
    }

    public static void membershipIsExactCaseSensitiveAndCanBelongToSeveralCategories() {
        ItemStack stack = new ItemStack(metadata, 1, 4);
        assertTrue(
            ResourceSelector.ore(METAL)
                .matches(stack));
        assertTrue(
            ResourceSelector.ore(SECONDARY)
                .matches(stack));
        assertFalse(
            ResourceSelector.ore("sfmOreSelectormetal")
                .matches(stack));
        assertFalse(
            ResourceSelector.ore("sfmOreSelectorMeta")
                .matches(stack));
        assertFalse(
            ResourceSelector.ore(METAL)
                .matches(null));
        assertFalse(
            ResourceSelector.ore(METAL)
                .matches(new ItemStack((Item) null)));
    }

    public static void unknownNameMatchesNothingWithoutRegisteringIt() {
        String unknown = "sfmOreSelectorNeverRegistered";
        assertFalse(OreDictionary.doesOreNameExist(unknown));
        String[] names = OreDictionary.getOreNames();
        ResourceSelector selector = ResourceSelector.ore(unknown);
        assertFalse(selector.matches(new ItemStack(wildcard, 1, 4)));
        assertFalse(selector.matches(new ItemStack(unrelated, 1, 0)));
        assertFalse(OreDictionary.doesOreNameExist(unknown));
        assertArrayEquals(names, OreDictionary.getOreNames());
    }

    public static void previouslyCompiledSelectorSeesLaterCategoryAndMembershipRegistrations() {
        String name = "sfmOreSelectorLateRegistration";
        ResourceSelector selector = ResourceSelector.ore(name);
        ItemStack stack = new ItemStack(late, 1, 2);
        assertFalse(selector.matches(stack));
        assertFalse(OreDictionary.doesOreNameExist(name));
        OreDictionary.registerOre(name, new ItemStack(unrelated, 1, 0));
        assertFalse(selector.matches(stack));
        OreDictionary.registerOre(name, stack.copy());
        assertTrue(selector.matches(stack));
        assertFalse(selector.matches(new ItemStack(late, 1, 3)));
    }

    public static void itemSelectorsRetainRegistryIdentityAndWildcardBehavior() {
        String id = "minecraft:sfm_ore_selector_test_metadata";
        ResourceSelector selector = ResourceSelector.item(id);
        assertEquals(ResourceSelector.Kind.ITEM, selector.getKind());
        assertEquals(id, selector.getId());
        assertEquals("ITEM(\"" + id + "\")", selector.toString());
        assertTrue(selector.matches(new ItemStack(metadata, 1, 4)));
        assertTrue(selector.matches(new ItemStack(metadata, 1, 3)));
        assertFalse(selector.matches(new ItemStack(wildcard, 1, 4)));
        assertTrue(
            ResourceSelector.item("*")
                .matches(new ItemStack(unrelated)));
        assertFalse(
            ResourceSelector.item("*")
                .matches(null));
        assertEquals(
            "ITEM(\"*\")",
            ResourceSelector.item("*")
                .toString());
        assertThrows(IllegalArgumentException.class, () -> ResourceSelector.item("#ore:ingotIron"));
    }

    public static void oreMembershipWorkAndAllocationShareTheInvocationQueryBudget() {
        ItemStack stack = new ItemStack(metadata, 1, 4);
        ResourceSelector selector = ResourceSelector.ore(METAL);
        QueryBudget budget = new QueryBudget();
        assertTrue(selector.matches(stack, budget));
        assertEquals(2, budget.getSlotWork());
        assertEquals(2, budget.getAllocations());
        assertEquals(0, budget.getProviderCalls());

        QueryBudget exhaustedWork = new QueryBudget();
        exhaustedWork.chargeSlots(QueryBudget.MAX_SLOT_WORK - 1);
        IllegalArgumentException workFailure = assertThrows(
            IllegalArgumentException.class,
            () -> selector.matches(stack, exhaustedWork));
        assertTrue(
            workFailure.getMessage()
                .contains("slot work budget exhausted"));
        assertEquals(QueryBudget.MAX_SLOT_WORK - 1, exhaustedWork.getSlotWork());
        assertEquals(0, exhaustedWork.getAllocations());

        QueryBudget exhaustedAllocations = new QueryBudget();
        exhaustedAllocations.chargeAllocation(QueryBudget.MAX_ALLOCATIONS - 1);
        IllegalArgumentException allocationFailure = assertThrows(
            IllegalArgumentException.class,
            () -> selector.matches(stack, exhaustedAllocations));
        assertTrue(
            allocationFailure.getMessage()
                .contains("allocation budget exhausted"));
        assertEquals(2, exhaustedAllocations.getSlotWork());
        assertEquals(QueryBudget.MAX_ALLOCATIONS - 1, exhaustedAllocations.getAllocations());
    }

    public static void itemMatchingDoesNotAddOreMembershipBudgetCharges() {
        QueryBudget budget = new QueryBudget();
        ItemStack stack = new ItemStack(metadata, 1, 4);
        assertTrue(
            ResourceSelector.item("minecraft:sfm_ore_selector_test_metadata")
                .matches(stack, budget));
        assertTrue(
            ResourceSelector.item("*")
                .matches(stack, budget));
        assertEquals(0, budget.getSlotWork());
        assertEquals(0, budget.getAllocations());
        assertEquals(0, budget.getProviderCalls());
    }

    public static void oreSelectorsRemainResourceSelectorsAndFormatWithTheirOwnKind() {
        ResourceSelector selector = ResourceSelector.ore(METAL);
        assertEquals(ResourceSelector.Kind.ORE, selector.getKind());
        assertEquals(METAL, selector.getId());
        assertEquals("ORE(\"" + METAL + "\")", selector.toString());
        assertEquals(
            DataType.RESOURCE_SELECTOR,
            DataValue.selector(selector)
                .getType());
        assertSame(
            selector,
            DataValue.selector(selector)
                .asSelector());
        char[] boundary = new char[256];
        Arrays.fill(boundary, 'a');
        assertEquals(
            new String(boundary),
            ResourceSelector.ore(new String(boundary))
                .getId());
        assertEquals(
            "ore_123",
            ResourceSelector.ore("ore_123")
                .getId());
    }

    public static void invalidOreNamesRejectPatternsWhitespacePrefixesUnicodeAndOversizedNames() {
        char[] oversized = new char[257];
        Arrays.fill(oversized, 'a');
        String[] invalid = { null, "", "*", "ingot*", "ingot?", "[Ii]ngotIron", "ore:ingotIron", "#ingotIron",
            "ingot-Iron", "ingot.Iron", "ingot Iron", " ingotIron", "ingotIron ", "ingot\tIron", "ingot\nIron",
            "ingot\u00e9", new String(oversized) };
        for (String name : invalid) {
            IllegalArgumentException failure = assertThrows(
                IllegalArgumentException.class,
                () -> ResourceSelector.ore(name),
                String.valueOf(name));
            assertTrue(
                failure.getMessage()
                    .contains("ORE needs an exact ore name"));
        }
    }

    public static void nativeOreTransferSharesOneQuotaAcrossWildcardAndExactMetadataMembers() throws Exception {
        Fixture fixture = new Fixture();
        InventoryTile source = new InventoryTile(5);
        source.setInventorySlotContents(0, new ItemStack(wildcard, 3, 0));
        source.setInventorySlotContents(1, new ItemStack(wildcard, 4, 7));
        source.setInventorySlotContents(2, new ItemStack(metadata, 5, 4));
        source.setInventorySlotContents(3, new ItemStack(metadata, 11, 3)); // Not in this ore category.
        source.setInventorySlotContents(4, new ItemStack(unrelated, 13, 4));
        fixture.world.tiles.put(SRC, source);
        ProgramContext context = transferContext(fixture);
        DataValue ore = DataValue.selector(ResourceSelector.ore(METAL));
        ItemTransferResult result = context
            .transferItems(LabelReference.literal("source"), LabelReference.literal("dest"), ore, 8);
        assertEquals(8, result.getRequested());
        assertEquals(8, result.getMoved(), "The quota is aggregate, not eight per item/metadata variant");
        assertEquals(0, result.getRemaining());
        assertEquals(
            "COMPLETE",
            result.getStatus()
                .name());
        assertEquals(28, count(source));
        assertEquals(8, count(fixture.dest));
        assertEquals(11, source.getStackInSlot(3).stackSize);
        assertEquals(13, source.getStackInSlot(4).stackSize);
        int exactMetadataMoved = 0;
        for (int slot = 0; slot < fixture.dest.getSizeInventory(); slot++) {
            ItemStack moved = fixture.dest.getStackInSlot(slot);
            if (moved == null) continue;
            assertTrue(
                ResourceSelector.ore(METAL)
                    .matches(moved, new QueryBudget()));
            if (moved.getItem() == metadata) {
                assertEquals(4, moved.getItemDamage());
                exactMetadataMoved += moved.stackSize;
            }
        }
        assertTrue(
            exactMetadataMoved > 0,
            "Only seven wildcard items exist; exact metadata membership must contribute");
        assertTrue(
            context.getInvocationQueryBudget()
                .getProviderCalls() > 0);
        assertFalse(
            context.getManager()
                .hasPendingRecovery());
    }

    public static void nativeUnknownOreTransferMovesNothingAndDoesNotRegisterACategory() throws Exception {
        String unknown = "sfmOreTransferNeverRegistered";
        assertFalse(OreDictionary.doesOreNameExist(unknown));
        String[] names = OreDictionary.getOreNames();
        Fixture fixture = new Fixture();
        fixture.source.setInventorySlotContents(0, new ItemStack(wildcard, 3, 7));
        fixture.source.setInventorySlotContents(1, new ItemStack(metadata, 5, 4));
        ProgramContext context = transferContext(fixture);
        ItemTransferResult result = context.transferItems(
            LabelReference.literal("source"),
            LabelReference.literal("dest"),
            DataValue.selector(ResourceSelector.ore(unknown)),
            4);
        assertEquals(4, result.getRequested());
        assertEquals(0, result.getMoved());
        assertEquals(4, result.getRemaining());
        assertEquals(
            "NO_MATCH",
            result.getStatus()
                .name());
        assertEquals(8, count(fixture.source));
        assertEquals(0, count(fixture.dest));
        assertFalse(OreDictionary.doesOreNameExist(unknown));
        assertArrayEquals(names, OreDictionary.getOreNames());
        assertFalse(
            context.getManager()
                .hasPendingRecovery());
    }

    private static ProgramContext transferContext(Fixture fixture) throws Exception {
        // Unlike the ordinary headless fixture, this child has bootstrapped vanilla block callbacks.
        // Supply real air around markDirty's neighbor notifications instead of FakeWorld's null default.
        for (int x = -2; x <= 2; x++) for (int y = 62; y <= 66; y++) for (int z = -2; z <= 2; z++)
            fixture.world.blocks.put(new ca.teamdman.sfm.common.util.BlockPos(x, y, z), net.minecraft.init.Blocks.air);
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        return new ProgramContext(
            fixture.world,
            manager,
            new CableNetwork(fixture.world, MANAGER),
            fixture.labels,
            20,
            false);
    }
}
