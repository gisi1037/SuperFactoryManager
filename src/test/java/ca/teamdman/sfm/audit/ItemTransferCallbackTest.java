package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.RegistryNamespaced;
import net.minecraft.util.RegistrySimple;

import org.junit.jupiter.api.Test;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.recovery.ItemRecovery;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.data.DataValue;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.ResourceSelector;

/** Adversarial callbacks at the final boundaries between advisory reads and physical mutations. */
class ItemTransferCallbackTest {

    private static final Item ITEM = new Item();
    private static final LabelReference SOURCE = LabelReference.literal("source")
        .slots(0, 0);
    private static final LabelReference DEST = LabelReference.literal("dest")
        .slots(0, 0);
    private static final DataValue ALL = DataValue.selector(ResourceSelector.item("*"));

    static {
        try {
            Field namesField = RegistrySimple.class.getDeclaredField("registryObjects");
            namesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Object> names = (Map<String, Object>) namesField.get(Item.itemRegistry);
            names.put("sfm_transfer_callback:item", ITEM);
            Field idsField = RegistryNamespaced.class.getDeclaredField("underlyingIntegerMap");
            idsField.setAccessible(true);
            ((ObjectIntIdentityMap) idsField.get(Item.itemRegistry)).func_148746_a(ITEM, 32021);
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }

    @Test
    void mutatingSimulationPermissionCopyCannotEnlargeTheRequestedTransfer() throws Exception {
        Fixture fixture = new Fixture();
        fixture.source.setInventorySlotContents(0, stack(6));
        InventoryTile target = new InventoryTile(1) {

            @Override
            public boolean isItemValidForSlot(int slot, ItemStack inspection) {
                inspection.stackSize = 64;
                return true;
            }
        };
        fixture.world.tiles.put(DST, target);
        ProgramContext context = context(fixture);
        assertEquals(
            0,
            context.transferItems(SOURCE, DEST, ALL, 1)
                .getMoved());
        assertEquals(6, rawCount(fixture.source));
        assertEquals(0, rawCount(target));
        assertFalse(
            context.getManager()
                .hasPendingRecovery());
    }

    @Test
    void mutatingActualInsertionPermissionCopyCannotTransformOwnedItems() throws Exception {
        Fixture fixture = new Fixture();
        fixture.source.setInventorySlotContents(0, stack(6));
        InventoryTile target = new InventoryTile(1) {

            int permissions;

            @Override
            public boolean isItemValidForSlot(int slot, ItemStack inspection) {
                if (++permissions == 2) {
                    inspection.stackSize = 64;
                    inspection.setItemDamage(9);
                    inspection.setTagCompound(tag("changed"));
                }
                return true;
            }
        };
        fixture.world.tiles.put(DST, target);
        ProgramContext context = context(fixture);
        assertEquals(
            0,
            context.transferItems(SOURCE, DEST, ALL, 1)
                .getMoved());
        assertEquals(6, rawCount(fixture.source));
        assertEquals(0, rawCount(target));
        assertEquals(
            7,
            fixture.source.inv.getStackInSlot(0)
                .getItemDamage());
        assertEquals(
            tag("original"),
            fixture.source.inv.getStackInSlot(0)
                .getTagCompound());
        assertFalse(
            context.getManager()
                .hasPendingRecovery());
    }

    @Test
    void mutatingSourcePermissionCopiesAreRejectedBeforeExtraction() throws Exception {
        Fixture fixture = new Fixture();
        HookSource source = new HookSource();
        source.mutateInspection = true;
        source.setInventorySlotContents(0, stack(6));
        fixture.world.tiles.put(SRC, source);
        ProgramContext context = context(fixture);
        assertEquals(
            0,
            context.transferItems(SOURCE, DEST, ALL, 1)
                .getMoved());
        assertEquals(0, source.extractions);
        assertEquals(6, rawCount(source));
        assertEquals(0, rawCount(fixture.dest));
        assertFalse(
            context.getManager()
                .hasPendingRecovery());
    }

    @Test
    void finalSourceReadReplacementOrUnloadCannotExtractFromTheOldProvider() throws Exception {
        for (boolean unload : new boolean[] { false, true }) {
            Fixture fixture = new Fixture();
            HookSource source = new HookSource();
            InventoryTile replacement = new InventoryTile(1);
            source.setInventorySlotContents(0, stack(6));
            source.lateRead = () -> {
                if (unload) fixture.world.unloaded.add(SRC);
                else fixture.world.tiles.put(SRC, replacement);
            };
            fixture.world.tiles.put(SRC, source);
            ProgramContext context = context(fixture);
            assertEquals(
                0,
                context.transferItems(SOURCE, DEST, ALL, 1)
                    .getMoved());
            assertEquals(0, source.extractions);
            assertEquals(6, rawCount(source));
            assertEquals(0, rawCount(replacement));
            assertEquals(0, rawCount(fixture.dest));
            assertFalse(
                context.getManager()
                    .hasPendingRecovery());
        }
    }

    @Test
    void finalDestinationReadReplacementOrUnloadRestoresTheKnownExtraction() throws Exception {
        for (boolean unload : new boolean[] { false, true }) {
            Fixture fixture = new Fixture();
            fixture.source.setInventorySlotContents(0, stack(6));
            InventoryTile replacement = new InventoryTile(1);
            InventoryTile target = new InventoryTile(1) {

                int permissions;
                boolean lateRead;

                @Override
                public boolean isItemValidForSlot(int slot, ItemStack inspection) {
                    lateRead = ++permissions == 2;
                    return true;
                }

                @Override
                public ItemStack getStackInSlot(int slot) {
                    if (lateRead) {
                        lateRead = false;
                        if (unload) fixture.world.unloaded.add(DST);
                        else fixture.world.tiles.put(DST, replacement);
                    }
                    return super.getStackInSlot(slot);
                }
            };
            fixture.world.tiles.put(DST, target);
            ProgramContext context = context(fixture);
            assertEquals(
                0,
                context.transferItems(SOURCE, DEST, ALL, 1)
                    .getMoved());
            assertEquals(6, rawCount(fixture.source));
            assertEquals(0, rawCount(target));
            assertEquals(0, rawCount(replacement));
            assertFalse(
                context.getManager()
                    .hasPendingRecovery());
        }
    }

    @Test
    void rollbackLimitCallbackCannotWriteKnownRemainderIntoDetachedSource() throws Exception {
        Fixture fixture = new Fixture();
        InventoryTile replacement = new InventoryTile(1);
        InventoryTile source = new InventoryTile(1) {

            @Override
            public int getInventoryStackLimit() {
                fixture.world.tiles.put(SRC, replacement);
                return 64;
            }
        };
        source.setInventorySlotContents(0, stack(6));
        InventoryTile target = new InventoryTile(1) {

            int permissions;

            @Override
            public boolean isItemValidForSlot(int slot, ItemStack inspection) {
                return ++permissions == 1;
            }
        };
        fixture.world.tiles.put(SRC, source);
        fixture.world.tiles.put(DST, target);
        ProgramContext context = context(fixture);
        assertThrows(RuntimeException.class, () -> context.transferItems(SOURCE, DEST, ALL, 1));
        assertEquals(5, rawCount(source));
        assertEquals(0, rawCount(replacement));
        assertEquals(0, rawCount(target));
        assertTrue(
            context.getManager()
                .hasPendingRecovery());
        NBTTagCompound record = ItemRecovery.inspect(context.getManager());
        assertEquals(1, ItemStack.loadItemStackFromNBT(record.getCompoundTag("stack")).stackSize);
    }

    @Test
    void malformedReturnedNbtPreservesBoundedKnownProbeWithoutSerializingTheReturnedTree() throws Exception {
        Fixture fixture = new Fixture();
        InventoryTile source = new InventoryTile(1) {

            @Override
            public ItemStack decrStackSize(int slot, int amount) {
                ItemStack extracted = super.decrStackSize(slot, amount);
                NBTTagCompound root = new NBTTagCompound(), cursor = root;
                for (int depth = 0; depth < 20; depth++) {
                    NBTTagCompound child = new NBTTagCompound();
                    cursor.setTag("nested", child);
                    cursor = child;
                }
                extracted.setTagCompound(root);
                return extracted;
            }
        };
        source.setInventorySlotContents(0, stack(6));
        fixture.world.tiles.put(SRC, source);
        ProgramContext context = context(fixture);
        assertThrows(RuntimeException.class, () -> context.transferItems(SOURCE, DEST, ALL, 1));
        assertEquals(5, rawCount(source));
        assertEquals(0, rawCount(fixture.dest));
        assertTrue(
            context.getManager()
                .hasPendingRecovery());
        ItemStack preserved = ItemStack.loadItemStackFromNBT(
            ItemRecovery.inspect(context.getManager())
                .getCompoundTag("stack"));
        assertEquals(1, preserved.stackSize);
        assertEquals(7, preserved.getItemDamage());
        assertEquals(tag("original"), preserved.getTagCompound());
    }

    @Test
    void oversizedExtractionReturnIsBoundedByActualDecrementRatherThanEarlierSimulation() throws Exception {
        Fixture fixture = new Fixture();
        int[] actualAsked = { -1 }, simulationOffered = { -1 };
        InventoryTile source = new InventoryTile(1) {

            @Override
            public ItemStack decrStackSize(int slot, int amount) {
                actualAsked[0] = amount;
                ItemStack extracted = super.decrStackSize(slot, amount);
                extracted.stackSize = 3; // Corrupt provider: returns more than the two actually requested.
                return extracted;
            }
        };
        source.setInventorySlotContents(0, stack(8));
        InventoryTile target = new InventoryTile(1) {

            @Override
            public boolean isItemValidForSlot(int slot, ItemStack inspection) {
                simulationOffered[0] = inspection.stackSize;
                source.inv.getStackInSlot(0).stackSize = 2;
                return true;
            }
        };
        fixture.world.tiles.put(SRC, source);
        fixture.world.tiles.put(DST, target);
        ProgramContext context = context(fixture);
        RuntimeException failure = assertThrows(
            RuntimeException.class,
            () -> context.transferItems(SOURCE, DEST, ALL, 8));
        assertEquals(8, simulationOffered[0]);
        assertEquals(2, actualAsked[0]);
        assertEquals(0, rawCount(source));
        assertEquals(0, rawCount(target), "Unexpected returned amounts must never reach destination commit");
        assertTrue(
            failure.getMessage()
                .contains("after moving 0 of 8"));
        assertTrue(
            context.getManager()
                .hasPendingRecovery());
        NBTTagCompound record = ItemRecovery.inspect(context.getManager());
        assertEquals("uncertain", record.getString("state"));
        ItemStack preserved = ItemStack.loadItemStackFromNBT(record.getCompoundTag("stack"));
        assertEquals(
            2,
            preserved.stackSize,
            "Evidence must use the actual decrement argument, not simulated eight or returned three");
        assertEquals(7, preserved.getItemDamage());
        assertEquals(tag("original"), preserved.getTagCompound());
    }

    private static ProgramContext context(Fixture fixture) throws Exception {
        TileEntityManager manager = RecoveryFixtures.manager(fixture);
        return new ProgramContext(
            fixture.world,
            manager,
            new CableNetwork(fixture.world, MANAGER),
            fixture.labels,
            20,
            false);
    }

    private static ItemStack stack(int amount) {
        ItemStack stack = new ItemStack(ITEM, amount, 7);
        stack.setTagCompound(tag("original"));
        return stack;
    }

    private static NBTTagCompound tag(String value) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("variant", value);
        return tag;
    }

    private static int rawCount(InventoryTile inventory) {
        return count(inventory.inv);
    }

    private static final class HookSource extends InventoryTile implements ISidedInventory {

        int permissions, extractions;
        boolean mutateInspection, armed;
        Runnable lateRead;

        HookSource() {
            super(1);
        }

        @Override
        public int[] getAccessibleSlotsFromSide(int side) {
            return new int[] { 0 };
        }

        @Override
        public boolean canExtractItem(int slot, ItemStack inspection, int side) {
            if (mutateInspection) inspection.stackSize = 64;
            if (++permissions == 2) armed = true;
            return true;
        }

        @Override
        public boolean canInsertItem(int slot, ItemStack stack, int side) {
            return true;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (armed && lateRead != null) {
                armed = false;
                lateRead.run();
            }
            return super.getStackInSlot(slot);
        }

        @Override
        public ItemStack decrStackSize(int slot, int amount) {
            extractions++;
            return super.decrStackSize(slot, amount);
        }
    }
}
