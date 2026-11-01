package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistrySimple;
import net.minecraft.world.WorldProviderSurface;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ItemQueryService;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfml.data.ItemContents;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;

/** Provider-observable behavior of allocation-free range/access checks and ordered contents scans. */
class QueryFastPathTest {

    private static final Item SCAN_ITEM = new Item();
    private static final ResourceSelector ALL = ResourceSelector.item("*");
    private static final LabelReference SOURCE = LabelReference.literal("source");

    static {
        try {
            Field backing = RegistrySimple.class.getDeclaredField("registryObjects");
            backing.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> names = (Map<String, Object>) backing.get(Item.itemRegistry);
            names.put("sfm_query_fast_path:item", SCAN_ITEM);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    @Test
    void availabilityVisitsLaterProvidersAndPreservesTheirUnavailableAndErrorResults() throws Exception {
        for (boolean throwsLater : new boolean[] { false, true }) {
            Fixture fixture = new Fixture();
            List<String> calls = new ArrayList<>();
            TraceInventory first = new TraceInventory(3, "first", calls);
            TraceInventory second = new TraceInventory(3, "second", calls);
            fixture.world.tiles.put(SRC, first);
            fixture.world.tiles.put(DST, second);
            fixture.labels.addPosition("both", SRC);
            fixture.labels.addPosition("both", DST);
            second.failSize = throwsLater;
            if (!throwsLater) fixture.world.tiles.remove(DST);
            ItemQueryService service = service(fixture);
            if (throwsLater) {
                IllegalArgumentException failure = assertThrows(
                    IllegalArgumentException.class,
                    () -> service.available(LabelReference.literal("both"), ALL, new QueryBudget()));
                assertInstanceOf(IllegalStateException.class, failure.getCause());
                assertEquals(Arrays.asList("first:size", "second:size"), calls);
            } else {
                assertFalse(service.available(LabelReference.literal("both"), ALL, new QueryBudget()));
                assertEquals(Arrays.asList("first:size"), calls);
            }
        }
    }

    @Test
    void sidedAvailabilityVisitsEverySelectedFaceWithoutReadingStockOrDeduplicatingWork() throws Exception {
        Fixture fixture = new Fixture();
        List<String> calls = new ArrayList<>();
        SidedInventory inventory = new SidedInventory(calls);
        fixture.world.tiles.put(SRC, inventory);
        ItemQueryService service = service(fixture);
        QueryBudget budget = new QueryBudget();
        assertTrue(service.available(SOURCE, ALL, budget));
        assertEquals(Arrays.asList("sided:size", "side:0", "side:1", "side:2", "side:3", "side:4", "side:5"), calls);
        assertEquals(
            24,
            budget.getSlotWork(),
            "All raw entries remain charged, including duplicates and invalid slots");
        calls.clear();
        inventory.failLastFace = true;
        assertThrows(IllegalArgumentException.class, () -> service.available(SOURCE, ALL, new QueryBudget()));
        assertEquals(Arrays.asList("sided:size", "side:0", "side:1", "side:2", "side:3", "side:4", "side:5"), calls);
        inventory.failLastFace = false;
        inventory.exposed = new int[] { -1, 1, 1 };
        assertFalse(service.available(SOURCE, ALL, new QueryBudget()));
    }

    @Test
    void absurdRangesAndRawSideArraysStillFaultBeforeAnyStackRead() throws Exception {
        for (boolean sided : new boolean[] { false, true }) {
            Fixture fixture = new Fixture();
            List<String> calls = new ArrayList<>();
            TraceInventory inventory;
            if (sided) {
                SidedInventory exposed = new SidedInventory(calls);
                exposed.exposed = new int[QueryBudget.MAX_SLOT_WORK + 1];
                inventory = exposed;
            } else {
                inventory = new TraceInventory(1, "huge", calls);
                inventory.reportedSize = Integer.MAX_VALUE;
            }
            fixture.world.tiles.put(SRC, inventory);
            ItemQueryService service = service(fixture);
            assertThrows(IllegalArgumentException.class, () -> service.available(SOURCE, ALL, new QueryBudget()));
            assertThrows(IllegalArgumentException.class, () -> service.contents(SOURCE, ALL, new QueryBudget()));
            assertTrue(
                calls.stream()
                    .noneMatch(call -> call.contains(":read:")));
        }
    }

    @Test
    void restrictedNonsidedContentsReadAscendingAndPreserveLiveMutationAndPhysicalOrigins() throws Exception {
        Fixture fixture = new Fixture();
        List<String> calls = new ArrayList<>();
        TraceInventory inventory = new TraceInventory(40, "contents", calls) {

            @Override
            public ItemStack getStackInSlot(int slot) {
                if (slot == 30) inv.setInventorySlotContents(31, new ItemStack(SCAN_ITEM, 2, 31));
                return super.getStackInSlot(slot);
            }
        };
        for (int slot = 30; slot <= 34; slot++)
            inventory.setInventorySlotContents(slot, new ItemStack(SCAN_ITEM, 1, slot));
        fixture.world.tiles.put(SRC, inventory);
        fixture.world.tiles.put(DST, inventory);
        fixture.labels.addPosition("both", SRC);
        fixture.labels.addPosition("both", DST);
        calls.clear();
        QueryBudget budget = new QueryBudget();
        List<ItemContents.Group> groups = service(fixture).contents(
            LabelReference.literal("both")
                .slots(30, 34),
            ALL,
            budget)
            .groups(budget);
        assertEquals(
            Arrays.asList(
                "contents:size",
                "contents:read:30",
                "contents:read:31",
                "contents:read:32",
                "contents:read:33",
                "contents:read:34"),
            calls);
        assertEquals(5, groups.size());
        for (int i = 0; i < groups.size(); i++) {
            ItemContents.Group group = groups.get(i);
            assertEquals(
                30 + i,
                group.getItem()
                    .getMetadata());
            assertEquals(i == 1 ? 2 : 1, group.getTotal());
            assertEquals(
                1,
                group.getLocations()
                    .size());
            ItemContents.Location origin = group.getLocations()
                .get(0);
            assertEquals(SRC.x, origin.x);
            assertEquals(SRC.y, origin.y);
            assertEquals(SRC.z, origin.z);
            assertEquals(30 + i, origin.slot);
        }
    }

    private static ItemQueryService service(Fixture fixture) throws Exception {
        Field provider = net.minecraft.world.World.class.getDeclaredField("provider");
        provider.setAccessible(true);
        provider.set(fixture.world, new WorldProviderSurface());
        return new ItemQueryService(
            new ProgramContext(
                fixture.world,
                null,
                new CableNetwork(fixture.world, MANAGER),
                fixture.labels,
                20,
                false));
    }

    private static class TraceInventory extends InventoryTile {

        final String name;
        final List<String> calls;
        int reportedSize;
        boolean failSize;

        TraceInventory(int size, String name, List<String> calls) {
            super(size);
            this.name = name;
            this.calls = calls;
            reportedSize = size;
        }

        @Override
        public int getSizeInventory() {
            calls.add(name + ":size");
            if (failSize) throw new IllegalStateException("later size callback");
            return reportedSize;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            calls.add(name + ":read:" + slot);
            return super.getStackInSlot(slot);
        }
    }

    private static final class SidedInventory extends TraceInventory implements ISidedInventory {

        int[] exposed = { -1, 0, 0, 1 };
        boolean failLastFace;

        SidedInventory(List<String> calls) {
            super(1, "sided", calls);
        }

        @Override
        public int[] getAccessibleSlotsFromSide(int side) {
            calls.add("side:" + side);
            if (failLastFace && side == 5) throw new IllegalStateException("later face callback");
            return exposed;
        }

        @Override
        public boolean canExtractItem(int slot, ItemStack stack, int side) {
            throw new AssertionError("Observation cannot extract");
        }

        @Override
        public boolean canInsertItem(int slot, ItemStack stack, int side) {
            throw new AssertionError("Observation cannot insert");
        }
    }
}
