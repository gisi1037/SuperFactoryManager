package ca.teamdman.sfm.audit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfml.data.DataRuntime;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ExpressionLimits;
import ca.teamdman.sfml.data.ItemContents;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.ItemStockFunctions;
import ca.teamdman.sfml.data.QueryBudget;

class SfmlStockFunctionsTest {

    @Test
    void totalReadsGroupTotalsAndLocationAmountsWithoutAllocationsOrProviderCalls() {
        QueryBudget owner = new QueryBudget();
        ItemContents.Builder builder = new ItemContents.Builder(owner);
        ItemIdentity first = key(4, null), second = key(5, null);
        builder.add(first, 0, 1, 64, 0, 0, 63, 7);
        builder.add(first, 0, 2, 64, 0, 1, 63, 11);
        builder.add(second, 0, 3, 64, 0, 2, 63, 13);
        DataRuntime runtime = runtime(owner);
        DataValue contents = DataValue.contents(builder.build(), owner, runtime);
        int cells = runtime.getAllocatedCells(), strings = runtime.getAllocatedStringCharacters();
        int allocations = owner.getAllocations(), providers = owner.getProviderCalls();
        int work = runtime.getWork();
        assertEquals(31, ItemStockFunctions.total(contents, runtime));
        assertEquals(
            18,
            ItemStockFunctions.total(
                contents.get(0)
                    .get("locations"),
                runtime));
        assertEquals(
            13,
            ItemStockFunctions.total(
                contents.get(1)
                    .get("locations"),
                runtime));
        assertTrue(runtime.getWork() > work);
        assertEquals(cells, runtime.getAllocatedCells());
        assertEquals(strings, runtime.getAllocatedStringCharacters());
        assertEquals(allocations, owner.getAllocations());
        assertEquals(providers, owner.getProviderCalls());
    }

    @Test
    void stockMatchesExactMetadataAndNbtIncludingAbsentVersusEmpty() {
        QueryBudget owner = new QueryBudget();
        ItemContents.Builder builder = new ItemContents.Builder(owner);
        NBTTagCompound tagged = new NBTTagCompound();
        tagged.setInteger("grade", 3);
        builder.add(key(4, null), 0, 1, 64, 0, 0, 63, 7);
        builder.add(key(5, null), 0, 2, 64, 0, 0, 63, 11);
        builder.add(key(4, new NBTTagCompound()), 0, 3, 64, 0, 0, 63, 13);
        builder.add(key(4, tagged), 0, 4, 64, 0, 0, 63, 17);
        DataRuntime runtime = runtime(owner);
        DataValue contents = DataValue.contents(builder.build(), owner, runtime);
        int before = owner.getSlotWork();
        assertEquals(7, ItemStockFunctions.stock(contents, key(4, null), runtime));
        assertEquals(11, ItemStockFunctions.stock(contents, key(5, null), runtime));
        assertEquals(13, ItemStockFunctions.stock(contents, key(4, new NBTTagCompound()), runtime));
        assertEquals(17, ItemStockFunctions.stock(contents, key(4, tagged), runtime));
        assertEquals(0, ItemStockFunctions.stock(contents, key(6, null), runtime));
        assertEquals(
            0,
            ItemStockFunctions
                .stock(contents, ItemIdentity.of("stock_test:other", 4, null, new QueryBudget()), runtime));
        assertTrue(owner.getSlotWork() > before, "Exact comparisons share the invocation's query work");
        tagged.setInteger("grade", 4);
        assertEquals(0, ItemStockFunctions.stock(contents, key(4, tagged), runtime));
    }

    @Test
    void emptyObservationReturnsZeroWithItsKnownSequenceType() {
        QueryBudget owner = new QueryBudget();
        DataRuntime runtime = runtime(owner);
        DataValue empty = DataValue.contents(new ItemContents.Builder(owner).build(), owner, runtime);
        assertEquals(0, ItemStockFunctions.total(empty, runtime));
        assertEquals(0, ItemStockFunctions.stock(empty, key(0, null), runtime));
    }

    @Test
    void checkedTotalRejectsOverflowAcrossGroupsAndPreservesLongMaxLocationAmounts() {
        QueryBudget owner = new QueryBudget();
        ItemContents.Builder builder = new ItemContents.Builder(owner);
        builder.add(key(0, null), 0, 1, 64, 0, 0, 63, Long.MAX_VALUE);
        builder.add(key(1, null), 0, 2, 64, 0, 0, 63, 1);
        DataRuntime runtime = runtime(owner);
        DataValue contents = DataValue.contents(builder.build(), owner, runtime);
        assertThrows(IllegalArgumentException.class, () -> ItemStockFunctions.total(contents, runtime));
        assertEquals(
            Long.MAX_VALUE,
            ItemStockFunctions.total(
                contents.get(0)
                    .get("locations"),
                runtime));
        assertEquals(Long.MAX_VALUE, ItemStockFunctions.stock(contents, key(0, null), runtime));
    }

    @Test
    void invalidTypesAndNullArgumentsAreRejected() {
        QueryBudget owner = new QueryBudget();
        DataRuntime runtime = runtime(owner);
        DataValue empty = DataValue.contents(new ItemContents.Builder(owner).build(), owner, runtime);
        DataValue array = DataValue.array(Arrays.asList(DataValue.integer(1), DataValue.integer(2)));
        assertThrows(IllegalArgumentException.class, () -> ItemStockFunctions.total(array, runtime));
        assertThrows(IllegalArgumentException.class, () -> ItemStockFunctions.total(null, runtime));
        assertThrows(IllegalArgumentException.class, () -> ItemStockFunctions.total(empty, null));
        assertThrows(IllegalArgumentException.class, () -> ItemStockFunctions.stock(array, key(0, null), runtime));
        assertThrows(IllegalArgumentException.class, () -> ItemStockFunctions.stock(null, key(0, null), runtime));
        assertThrows(IllegalArgumentException.class, () -> ItemStockFunctions.stock(empty, null, runtime));
        assertThrows(IllegalArgumentException.class, () -> ItemStockFunctions.stock(empty, key(0, null), null));
    }

    @Test
    void expressionAndExactComparisonBudgetsFailClosed() {
        QueryBudget owner = new QueryBudget();
        ItemContents.Builder builder = new ItemContents.Builder(owner);
        builder.add(key(0, null), 0, 1, 64, 0, 0, 63, 7);
        DataRuntime runtime = runtime(owner);
        DataValue contents = DataValue.contents(builder.build(), owner, runtime);
        runtime.work(ExpressionLimits.MAX_WORK - runtime.getWork());
        assertThrows(IllegalArgumentException.class, () -> ItemStockFunctions.total(contents, runtime));
        assertThrows(IllegalArgumentException.class, () -> ItemStockFunctions.stock(contents, key(0, null), runtime));
        DataRuntime comparison = runtime(owner);
        owner.chargeSlots(QueryBudget.MAX_SLOT_WORK - owner.getSlotWork());
        assertThrows(
            IllegalArgumentException.class,
            () -> ItemStockFunctions.stock(contents, key(0, null), comparison));
        assertEquals(
            7,
            contents.get(0)
                .get("total")
                .asLong());
        assertEquals(QueryBudget.MAX_SLOT_WORK, owner.getSlotWork());
    }

    @Test
    void shortageHandlesLongBoundariesWithoutOverflowAndRejectsNegativeInputs() {
        assertEquals(0, ItemStockFunctions.shortage(0, 0));
        assertEquals(0, ItemStockFunctions.shortage(4, 9));
        assertEquals(0, ItemStockFunctions.shortage(Long.MAX_VALUE, Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, ItemStockFunctions.shortage(Long.MAX_VALUE, 0));
        assertEquals(Long.MAX_VALUE - 1, ItemStockFunctions.shortage(Long.MAX_VALUE, 1));
        assertEquals(6, ItemStockFunctions.shortage(9, 3));
        assertThrows(IllegalArgumentException.class, () -> ItemStockFunctions.shortage(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> ItemStockFunctions.shortage(0, -1));
        assertThrows(IllegalArgumentException.class, () -> ItemStockFunctions.shortage(Long.MIN_VALUE, Long.MAX_VALUE));
    }

    private static DataRuntime runtime(QueryBudget owner) {
        return new DataRuntime(null, 0, null, owner);
    }

    private static ItemIdentity key(int metadata, NBTTagCompound nbt) {
        return ItemIdentity.of("stock_test:item", metadata, nbt, new QueryBudget());
    }
}
