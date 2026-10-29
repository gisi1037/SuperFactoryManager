package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ItemTransferResult;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.FMLRelaunchLog;
import cpw.mods.fml.relauncher.Side;

/** Runs only inside the isolated LaunchClassLoader owned by ResourceSelectorOreTest. */
public final class ResourceSelectorOreFixture {

    private static final String METAL = "sfmOreSelectorMetal", SECONDARY = "sfmOreSelectorSecondary";
    private static Item wildcard, metadata, unrelated, late;

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
