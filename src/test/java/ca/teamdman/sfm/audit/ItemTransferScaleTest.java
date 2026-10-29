package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.RegistryNamespaced;
import net.minecraft.util.RegistrySimple;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ItemQueryService;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.ItemContents;
import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.ItemTransferResult;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;
import ca.teamdman.sfml.data.ResourceSelector;

/** Counts actual native work; unused commit reservations must not masquerade as consumed inventory work. */
class ItemTransferScaleTest {

    private static final Item ITEM = new Item();

    static {
        try {
            Field field = RegistrySimple.class.getDeclaredField("registryObjects");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> names = (Map<String, Object>) field.get(Item.itemRegistry);
            names.put("sfm_transfer_scale:item", ITEM);
            Field idsField = RegistryNamespaced.class.getDeclaredField("underlyingIntegerMap");
            idsField.setAccessible(true);
            ((ObjectIntIdentityMap) idsField.get(Item.itemRegistry)).func_148746_a(ITEM, 32031);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    @Test
    void twentyOrdinaryStacksBetweenFiftyFourSlotInventoriesUseActualCommitWork() throws Exception {
        Fixture fixture = new Fixture();
        CountingInventory source = new CountingInventory(54), target = new CountingInventory(54);
        for (int slot = 0; slot < 54; slot++) source.inv.setInventorySlotContents(slot, new ItemStack(ITEM, 64));
        fixture.world.tiles.put(SRC, source);
        fixture.world.tiles.put(DST, target);
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        ProgramContext context = new ProgramContext(
            fixture.world,
            manager,
            new CableNetwork(fixture.world, MANAGER),
            fixture.labels,
            20,
            false);
        QueryBudget budget = context.getInvocationQueryBudget();
        ItemTransferResult result;
        try {
            result = context.transferItems(
                LabelReference.literal("source"),
                LabelReference.literal("dest"),
                DataValue.selector(ResourceSelector.item("*")),
                20L * 64);
        } catch (RuntimeException failure) {
            throw new AssertionError(
                "Actual query work: positions=" + budget.getPositions()
                    + ", providers="
                    + budget.getProviderCalls()
                    + ", slots="
                    + budget.getSlotWork()
                    + ", allocations="
                    + budget.getAllocations()
                    + ", extracted="
                    + source.extractions
                    + ", inserted="
                    + target.insertions,
                failure);
        }
        assertEquals(ItemTransferResult.Status.COMPLETE, result.getStatus());
        assertEquals(20L * 64, result.getMoved());
        assertEquals(0, result.getRemaining());
        assertEquals(34 * 64, count(source.inv));
        assertEquals(20 * 64, count(target.inv));
        assertEquals(20, source.extractions);
        assertEquals(20, target.insertions);
        assertFalse(manager.hasPendingRecovery());
        System.out.println(
            "Native54-slot20-stack work: positions=" + budget.getPositions()
                + ", providers="
                + budget.getProviderCalls()
                + ", slots="
                + budget.getSlotWork()
                + ", allocations="
                + budget.getAllocations());
    }

    @Test
    void complexNbtAcrossTwoSourcesSharesTheScanAndExactTransferBudget() throws Exception {
        Fixture fixture = new Fixture();
        CountingInventory sourceLeft = new CountingInventory(3), sourceRight = new CountingInventory(4),
            target = new CountingInventory(54);
        NBTTagCompound payload = complexPayload();
        ItemStack plain = new ItemStack(ITEM, 7), empty = new ItemStack(ITEM, 11);
        empty.setTagCompound(new NBTTagCompound());
        ItemStack first = new ItemStack(ITEM, 12), second = new ItemStack(ITEM, 5);
        first.setTagCompound(payload);
        second.setTagCompound((NBTTagCompound) payload.copy());
        sourceLeft.inv.setInventorySlotContents(0, plain);
        sourceLeft.inv.setInventorySlotContents(1, empty);
        sourceLeft.inv.setInventorySlotContents(2, first);
        sourceRight.inv.setInventorySlotContents(3, second);
        fixture.world.tiles.put(SRC, sourceLeft);
        fixture.world.tiles.put(EXTRA, sourceRight);
        fixture.labels.addPosition("source", EXTRA);
        fixture.world.tiles.put(DST, target);
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        ProgramContext context = new ProgramContext(
            fixture.world,
            manager,
            new CableNetwork(fixture.world, MANAGER),
            fixture.labels,
            20,
            false);
        QueryBudget owner = context.getInvocationQueryBudget();
        ItemTransferResult result;
        ItemIdentity complexKey = null;
        try {
            ItemContents contents = new ItemQueryService(context)
                .contents(LabelReference.literal("source"), ResourceSelector.item("*"), owner);
            assertEquals(
                3,
                contents.groups(owner)
                    .size());
            long scanned = 0;
            for (ItemContents.Group group : contents.groups(owner)) {
                scanned += group.getTotal();
                if (group.getTotal() == 17) {
                    complexKey = group.getItem();
                    assertEquals(
                        2,
                        group.getLocations()
                            .size());
                }
            }
            assertEquals(35, scanned);
            assertNotNull(complexKey);
            result = context.transferItems(
                LabelReference.literal("source"),
                LabelReference.literal("dest"),
                DataValue.itemIdentity(complexKey),
                20);
        } catch (RuntimeException failure) {
            throw new AssertionError(
                "Complex-NBT actual query work: positions=" + owner.getPositions()
                    + ", providers="
                    + owner.getProviderCalls()
                    + ", slots="
                    + owner.getSlotWork()
                    + ", allocations="
                    + owner.getAllocations()
                    + ", extracted="
                    + (sourceLeft.extractions + sourceRight.extractions)
                    + ", inserted="
                    + target.insertions,
                failure);
        }
        assertEquals(ItemTransferResult.Status.PARTIAL, result.getStatus());
        assertEquals(20, result.getRequested());
        assertEquals(17, result.getMoved());
        assertEquals(3, result.getRemaining());
        assertEquals(18, count(sourceLeft.inv) + count(sourceRight.inv));
        assertEquals(17, count(target.inv));
        assertEquals(2, sourceLeft.extractions + sourceRight.extractions);
        assertEquals(complexKey, ItemIdentity.fromStack(target.inv.getStackInSlot(0), new QueryBudget()));
        assertNull(
            sourceLeft.inv.getStackInSlot(0)
                .getTagCompound());
        assertEquals(
            new NBTTagCompound(),
            sourceLeft.inv.getStackInSlot(1)
                .getTagCompound());
        assertFalse(manager.hasPendingRecovery());
        System.out.println(
            "Complex-NBT shared scan/transfer work: positions=" + owner.getPositions()
                + ", providers="
                + owner.getProviderCalls()
                + ", slots="
                + owner.getSlotWork()
                + ", allocations="
                + owner.getAllocations());
    }

    /** Same small all-type/list payload as the opt-in FoundationsQualification fixture. */
    private static NBTTagCompound complexPayload() {
        NBTTagCompound root = new NBTTagCompound();
        root.setByte("b", (byte) -1);
        root.setShort("s", (short) -2);
        root.setInteger("i", 3);
        root.setLong("l", 4L);
        root.setFloat("f", 1.25F);
        root.setDouble("d", -2.5D);
        root.setByteArray("a", new byte[] { 1, -2 });
        root.setIntArray("j", new int[] { 7 });
        root.setString("t", "x");
        NBTTagList numbers = new NBTTagList();
        numbers.appendTag(new NBTTagByte((byte) 5));
        root.setTag("n", numbers);
        NBTTagList strings = new NBTTagList();
        strings.appendTag(new NBTTagString("y"));
        root.setTag("q", strings);
        NBTTagCompound child = new NBTTagCompound();
        child.setByte("v", (byte) 6);
        NBTTagList compounds = new NBTTagList();
        compounds.appendTag(child);
        root.setTag("c", compounds);
        root.setTag("e", new NBTTagList());
        return root;
    }

    private static final class CountingInventory extends InventoryTile {

        int extractions, insertions, permissions;

        CountingInventory(int size) {
            super(size);
        }

        @Override
        public ItemStack decrStackSize(int slot, int amount) {
            extractions++;
            return super.decrStackSize(slot, amount);
        }

        @Override
        public void setInventorySlotContents(int slot, ItemStack stack) {
            insertions++;
            super.setInventorySlotContents(slot, stack);
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack) {
            permissions++;
            return true;
        }
    }

    @Test
    void fiftyFourFullSlotsRejectWithoutSourceCopiesOrPermissionProbes() throws Exception {
        Fixture fixture = new Fixture();
        CountingInventory source = new CountingInventory(54), target = new CountingInventory(54);
        source.inv.setInventorySlotContents(0, new ItemStack(ITEM, 64));
        for (int slot = 0; slot < 54; slot++) target.inv.setInventorySlotContents(slot, new ItemStack(ITEM, 64));
        fixture.world.tiles.put(SRC, source);
        fixture.world.tiles.put(DST, target);
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        ProgramContext context = new ProgramContext(
            fixture.world,
            manager,
            new CableNetwork(fixture.world, MANAGER),
            fixture.labels,
            20,
            false);
        ItemTransferResult result = context.transferItems(
            LabelReference.literal("source"),
            LabelReference.literal("dest"),
            DataValue.selector(ResourceSelector.item("*")),
            64);
        assertEquals(ItemTransferResult.Status.NO_CAPACITY, result.getStatus());
        assertEquals(0, result.getMoved());
        assertEquals(64, count(source.inv));
        assertEquals(54 * 64, count(target.inv));
        assertEquals(0, source.extractions);
        assertEquals(0, target.insertions);
        assertEquals(
            0,
            target.permissions,
            "Inventory-limit-full slots cannot accept anything, independent of permissions");
        assertFalse(manager.hasPendingRecovery());
    }

    @Test
    void fullSlotObservationRevalidatesProviderAfterItsInventoryLimitCallback() throws Exception {
        Fixture fixture = new Fixture();
        fixture.source.setInventorySlotContents(0, new ItemStack(ITEM, 64));
        InventoryTile replacement = new InventoryTile(1);
        InventoryTile target = new InventoryTile(1) {

            @Override
            public int getInventoryStackLimit() {
                fixture.world.tiles.put(DST, replacement);
                return 64;
            }
        };
        target.inv.setInventorySlotContents(0, new ItemStack(ITEM, 64));
        fixture.world.tiles.put(DST, target);
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        ProgramContext context = new ProgramContext(
            fixture.world,
            manager,
            new CableNetwork(fixture.world, MANAGER),
            fixture.labels,
            20,
            false);
        ItemTransferResult result = context.transferItems(
            LabelReference.literal("source"),
            LabelReference.literal("dest"),
            DataValue.selector(ResourceSelector.item("*")),
            1);
        assertEquals(ItemTransferResult.Status.UNAVAILABLE, result.getStatus());
        assertEquals(0, result.getMoved());
        assertEquals(64, count(fixture.source.inv));
        assertEquals(64, count(target.inv));
        assertEquals(0, count(replacement.inv));
        assertFalse(manager.hasPendingRecovery());
    }
}
