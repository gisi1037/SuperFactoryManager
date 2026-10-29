package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.RegistrySimple;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.data.DataCompiler;
import ca.teamdman.sfml.data.DataQueries;
import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataType;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ItemContents;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;
import ca.teamdman.sfml.data.StateAccess;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

/** Native contents become immutable typed observations; this adds no transfer actions or snapshot authority. */
class SfmlContentsIntegrationTest {

    private static final Item SCAN_ITEM = new Item(), SCAN_OTHER = new Item();

    static {
        try {
            Field backing = RegistrySimple.class.getDeclaredField("registryObjects");
            backing.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> names = (Map<String, Object>) backing.get(Item.itemRegistry);
            names.put("sfm_contents_integration:item", SCAN_ITEM);
            names.put("sfm_contents_integration:other", SCAN_OTHER);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    @Test
    void nestedLoopsIndexingAndRepeatedLengthsReadOnlyTheMaterializedSnapshot() throws Exception {
        Fixture fixture = fixture();
        CountingInventory source = new CountingInventory(4);
        fixture.add("source", SRC, source);
        source.setInventorySlotContents(0, stack(SCAN_ITEM, 3, 4, "a"));
        source.setInventorySlotContents(2, stack(SCAN_ITEM, 7, 4, "a"));
        source.setInventorySlotContents(3, stack(SCAN_ITEM, 5, 5, "b"));
        Program program = program(
            "STATE $result = {total:0, locations:0, length:0, first:0, dimension:0, repeat:0} EVERY 20 TICKS DO "
                + "LET $stock = LABEL(\"source\").contents() SET $result.length = LEN($stock) "
                + "SET $result.first = $stock[0].locations[1].amount SET $result.dimension = $stock[0].locations[0].dimension "
                + "FOR $group IN $stock DO SET $result.total = $result.total + $group.total "
                + "FOR $location IN $group.locations DO SET $result.locations = $result.locations + 1 END END "
                + "FOR $group IN $stock DO SET $result.repeat = $result.repeat + LEN($group.locations) END "
                + "SET $result.repeat = $result.repeat + LEN($stock) + LEN($stock[0].locations) END");
        Memory state = new Memory(program);
        program.tick(context(fixture, state));
        assertEquals(15, state.number("total"));
        assertEquals(3, state.number("locations"));
        assertEquals(2, state.number("length"));
        assertEquals(7, state.number("first"));
        assertEquals(37, state.number("dimension"));
        assertEquals(7, state.number("repeat"));
        assertEquals(4, source.reads, "Indexing, LEN and both loop levels must not query providers again");
    }

    @Test
    void functionsAcceptAndReturnViewsLocationsAndExactKeysWithoutLosingTheirShapes() throws Exception {
        Fixture fixture = fixture();
        fixture.source.setInventorySlotContents(0, stack(SCAN_ITEM, 3, 4, null));
        fixture.source.setInventorySlotContents(1, stack(SCAN_ITEM, 7, 4, null));
        Program program = program(
            "STATE $result = {total:0, locations:0, same:FALSE} "
                + "FUNCTION view($s: LABEL_REF) RETURNS ITEM_CONTENTS DO RETURN $s.contents() END "
                + "FUNCTION places($s: ITEM_CONTENTS) RETURNS ITEM_LOCATIONS DO RETURN $s[0].locations END "
                + "FUNCTION key($k: ITEM_KEY) RETURNS ITEM_KEY DO RETURN $k END "
                + "FUNCTION total($rows: ITEM_LOCATIONS) RETURNS INTEGER DO LET $sum = 0 "
                + "FOR $row IN $rows DO SET $sum = $sum + $row.amount END RETURN $sum END "
                + "EVERY 20 TICKS DO LET $stock = view(LABEL(\"source\")) "
                + "SET $result.total = total(places($stock)) SET $result.locations = LEN(places($stock)) "
                + "SET $result.same = key($stock[0].item) = $stock[0].item END");
        Memory state = new Memory(program);
        program.tick(context(fixture, state));
        assertEquals(10, state.number("total"));
        assertEquals(2, state.number("locations"));
        assertTrue(
            state.value.get("same")
                .asBoolean());
    }

    @Test
    void snapshotRetainsEarlierAmountsWhileLaterScansObserveCallerTransfers() throws Exception {
        Fixture fixture = fixture();
        fixture.source.setInventorySlotContents(0, stack(SCAN_ITEM, 6, 0, null));
        Program program = program(
            "STATE $result = {before:0, after:0, origin:0, same:FALSE} EVERY 20 TICKS DO "
                + "LET $before = LABEL(\"source\").contents() INPUT 2 FROM source OUTPUT TO dest "
                + "LET $after = LABEL(\"source\").contents() SET $result.before = $before[0].total "
                + "SET $result.origin = $before[0].locations[0].amount SET $result.after = $after[0].total "
                + "SET $result.same = $before[0].item = $after[0].item END");
        Memory state = new Memory(program);
        program.tick(context(fixture, state));
        assertEquals(6, state.number("before"));
        assertEquals(6, state.number("origin"));
        assertEquals(4, state.number("after"));
        assertTrue(
            state.value.get("same")
                .asBoolean());
        assertEquals(4, count(fixture.source));
        assertEquals(2, count(fixture.dest));
    }

    @Test
    void noArgumentAndItemFilteredQueriesProduceRealVariableLengthsIncludingEmptyViews() throws Exception {
        Fixture fixture = fixture();
        fixture.source.setInventorySlotContents(0, stack(SCAN_ITEM, 3, 0, null));
        fixture.source.setInventorySlotContents(1, stack(SCAN_OTHER, 5, 0, null));
        Program program = program(
            "STATE $result = {all:0, selected:0, total:0, empty:0, visited:0} EVERY 20 TICKS DO "
                + "LET $all = LABEL(\"source\").contents() "
                + "LET $selected = LABEL(\"source\").contents(ITEM(\"sfm_contents_integration:item\")) "
                + "LET $empty = LABEL(\"source\").contents(ITEM(\"sfm_contents_integration:absent\")) "
                + "SET $result.all = LEN($all) SET $result.selected = LEN($selected) "
                + "SET $result.total = $selected[0].total SET $result.empty = LEN($empty) "
                + "FOR $group IN $empty DO SET $result.visited = $result.visited + 1 END END");
        Memory state = new Memory(program);
        program.tick(context(fixture, state));
        assertEquals(2, state.number("all"));
        assertEquals(1, state.number("selected"));
        assertEquals(3, state.number("total"));
        assertEquals(0, state.number("empty"));
        assertEquals(0, state.number("visited"));
    }

    @Test
    void oreSelectorCompilesAndReachesTheContentsObservationBoundary() {
        ResourceSelector[] received = new ResourceSelector[1];
        DataQueries queries = new DataQueries() {

            @Override
            public long count(LabelReference source, ResourceSelector selector, QueryBudget budget) {
                throw new AssertionError("Unexpected count");
            }

            @Override
            public boolean empty(LabelReference source, ResourceSelector selector, QueryBudget budget) {
                throw new AssertionError("Unexpected empty");
            }

            @Override
            public boolean available(LabelReference source, ResourceSelector selector, QueryBudget budget) {
                throw new AssertionError("Unexpected available");
            }

            @Override
            public ItemContents contents(LabelReference source, ResourceSelector selector, QueryBudget budget) {
                assertEquals("source", source.getName());
                received[0] = selector;
                return new ItemContents.Builder(budget).build();
            }
        };
        DataValue result = new DataCompiler().compile("LEN(LABEL(\"source\").contents(ORE(\"ingotIron\")))", 1, 0)
            .evaluate(new DataRuntime(null, 0, queries));
        assertEquals(0, result.asLong());
        assertEquals(ResourceSelector.Kind.ORE, received[0].getKind());
        assertEquals("ingotIron", received[0].getId());
        // Actual Forge OreDictionary matching is covered by the isolated ResourceSelectorOreTest fixture.
    }

    @Test
    void exactKeyEqualityIncludesMetadataAndNbtAcrossSeparateFreshScans() throws Exception {
        Fixture fixture = fixture();
        fixture.source.setInventorySlotContents(0, stack(SCAN_ITEM, 3, 4, "a"));
        fixture.source.setInventorySlotContents(1, stack(SCAN_ITEM, 3, 4, "b"));
        fixture.source.setInventorySlotContents(2, stack(SCAN_ITEM, 1, 5, "a"));
        Program program = program(
            "STATE $result = {same:FALSE, differentNbt:TRUE, differentMeta:TRUE} EVERY 20 TICKS DO "
                + "LET $a = LABEL(\"source\").contents() LET $b = LABEL(\"source\").contents() "
                + "SET $result.same = $a[0].item = $b[0].item "
                + "SET $result.differentNbt = $a[0].item = $b[1].item "
                + "SET $result.differentMeta = $a[0].item = $b[2].item END");
        Memory state = new Memory(program);
        program.tick(context(fixture, state));
        assertTrue(
            state.value.get("same")
                .asBoolean());
        assertFalse(
            state.value.get("differentNbt")
                .asBoolean());
        assertFalse(
            state.value.get("differentMeta")
                .asBoolean());
    }

    @Test
    void stateAndSnapshotElementMutationAreRejectedWhileFixedArrayUpdatesStillWork() throws Exception {
        String[] invalid = { "STATE $s = LABEL(\"source\").contents() EVERY 20 TICKS DO FORGET END",
            "STATE $s = {nested:[LABEL(\"source\").contents()]} EVERY 20 TICKS DO FORGET END",
            "STATE $s = LABEL(\"source\").contents()[0].item EVERY 20 TICKS DO FORGET END",
            "STATE $s = LABEL(\"source\").contents()[0].locations EVERY 20 TICKS DO FORGET END",
            "EVERY 20 TICKS DO LET $s = LABEL(\"source\").contents() SET $s[0].total = 99 END",
            "EVERY 20 TICKS DO LET $s = LABEL(\"source\").contents()[0].locations SET $s[0].amount = 99 END",
            "EVERY 20 TICKS DO LET $s = {nested:LABEL(\"source\").contents()} SET $s.nested[0].total = 99 END" };
        for (String source : invalid) assertFalse(
            new ProgramBuilder(source).build()
                .isSuccess(),
            source);
        Fixture fixture = fixture();
        Program program = program(
            "STATE $result = 0 EVERY 20 TICKS DO LET $a = [1,2] SET $a[0] = 7 SET $result = $a[0] END");
        Memory state = new Memory(program);
        program.tick(context(fixture, state));
        assertEquals(7, state.value.asLong());
    }

    @Test
    void wrongArityTypesAndIndexesFailBeforePublishingTheirResult() throws Exception {
        String[] invalid = { "LABEL(\"source\").contents(ITEM(\"*\"), ITEM(\"*\"))", "LABEL(\"source\").contents(1)",
            "ITEM(\"*\").contents()", "LABEL(\"source\").contents()[TRUE]", "LEN(LABEL(\"source\").contents()[0].item)",
            "LABEL(\"source\").contents()[0].item = ITEM(\"*\")" };
        for (String expression : invalid) assertFalse(
            new ProgramBuilder("EVERY 20 TICKS DO LET $n = " + expression + " END").build()
                .isSuccess(),
            expression);
        for (String index : new String[] { "-1", "0", "2147483648" }) {
            Fixture fixture = fixture();
            Program program = program(
                "STATE $result = 0 EVERY 20 TICKS DO LET $s = LABEL(\"source\").contents() " + "SET $result = $s["
                    + index
                    + "].total END");
            Memory state = new Memory(program);
            assertThrows(IllegalArgumentException.class, () -> program.tick(context(fixture, state)));
            assertEquals(0, state.value.asLong());
        }
    }

    @Test
    void exhaustedSharedQueryBudgetPublishesNoContentsResultOrTransfer() throws Exception {
        Fixture fixture = fixture();
        fixture.source.setInventorySlotContents(0, stack(SCAN_ITEM, 6, 0, null));
        Program program = program(
            "STATE $result = 0 EVERY 20 TICKS DO LET $s = LABEL(\"source\").contents() "
                + "SET $result = LEN($s) INPUT FROM source OUTPUT TO dest END");
        Memory state = new Memory(program);
        ProgramContext context = context(fixture, state);
        context.getInvocationQueryBudget()
            .chargeAllocation(QueryBudget.MAX_ALLOCATIONS);
        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class, () -> program.tick(context));
        assertTrue(
            failure.getMessage()
                .contains("budget exhausted"));
        assertEquals(0, state.value.asLong());
        assertEquals(6, count(fixture.source));
        assertEquals(0, count(fixture.dest));
    }

    @Test
    void exactKeyComparisonNeedsOnlyItsSharedBudgetAndNoLiveQueryProvider() {
        QueryBudget owner = new QueryBudget();
        DataCompiler compiler = new DataCompiler();
        compiler.pushScope();
        DataCompiler.Binding left = compiler.declareLocal("left", DataType.ITEM_KEY);
        DataCompiler.Binding right = compiler.declareLocal("right", DataType.ITEM_KEY);
        DataRuntime runtime = new DataRuntime(null, compiler.getMaxLocals(), null, owner);
        left.write(runtime, DataValue.itemIdentity(ItemIdentity.of("sfm_contents_integration:item", 4, null, owner)));
        right.write(runtime, DataValue.itemIdentity(ItemIdentity.of("sfm_contents_integration:item", 4, null, owner)));
        assertTrue(
            compiler.compile("$left = $right", 1, 0)
                .evaluate(runtime)
                .asBoolean());
    }

    @Test
    void nativeProviderFailureStopsBeforeAnyContentsValueOrStatePublication() throws Exception {
        Fixture fixture = fixture();
        fixture.add("source", SRC, new InventoryTile(3) {

            @Override
            public ItemStack getStackInSlot(int slot) {
                throw new IllegalStateException("fixture provider fault");
            }
        });
        Program program = program(
            "STATE $result = 0 EVERY 20 TICKS DO LET $s = LABEL(\"source\").contents() " + "SET $result = LEN($s) END");
        Memory state = new Memory(program);
        IllegalArgumentException failure = assertThrows(
            IllegalArgumentException.class,
            () -> program.tick(context(fixture, state)));
        assertTrue(
            failure.getMessage()
                .contains("provider failed"));
        assertEquals(0, state.value.asLong());
        assertEquals(0, count(fixture.dest));
    }

    private static Fixture fixture() throws Exception {
        Fixture fixture = new Fixture();
        WorldProviderSurface provider = allocate(WorldProviderSurface.class);
        provider.dimensionId = 37;
        Field field = World.class.getDeclaredField("provider");
        field.setAccessible(true);
        field.set(fixture.world, provider);
        return fixture;
    }

    private static ItemStack stack(Item item, int amount, int metadata, String value) {
        ItemStack stack = new ItemStack(item, amount, metadata);
        if (value != null) {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("variant", value);
            stack.setTagCompound(nbt);
        }
        return stack;
    }

    private static Program program(String source) {
        ProgramBuildResult result = new ProgramBuilder(source).build();
        assertTrue(result.isSuccess(), result.getErrorMessage());
        return result.getProgram();
    }

    private static ProgramContext context(Fixture fixture, StateAccess state) {
        ProgramContext context = new ProgramContext(
            fixture.world,
            null,
            new CableNetwork(fixture.world, MANAGER),
            fixture.labels,
            20,
            false);
        context.setStateAccess(state);
        return context;
    }

    private static final class CountingInventory extends InventoryTile {

        int reads;

        CountingInventory(int size) {
            super(size);
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            reads++;
            return super.getStackInSlot(slot);
        }
    }

    private static final class Memory implements StateAccess {

        DataValue value;

        Memory(Program program) {
            value = program.getStateSchema()
                .getDeclarations()
                .get(0)
                .getDefaultValue();
        }

        @Override
        public DataValue read(int slot) {
            return value;
        }

        @Override
        public void write(int slot, DataValue next) {
            value = next;
        }

        long number(String field) {
            return value.get(field)
                .asLong();
        }
    }
}
