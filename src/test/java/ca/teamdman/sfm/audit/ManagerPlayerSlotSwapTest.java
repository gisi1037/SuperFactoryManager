package ca.teamdman.sfm.audit;

import static ca.teamdman.sfm.audit.AuditRegressionTest.*;
import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Test;

import com.cleanroommc.modularui.utils.item.PlayerMainInvWrapper;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.integration.mui2.ManagerMui2Container;
import ca.teamdman.sfm.integration.mui2.ManagerPlayerSlotSwap;

class ManagerPlayerSlotSwapTest {

    @Test
    void occupiedExpandedPlayerIndexAndHotbarSwapExactlyAndInvertWithNbtAndCursorIntact() throws Exception {
        FakePlayer player = player();
        player.inventory.mainInventory = new ItemStack[82];
        // Public Dynamic Inventory binding supplies a PlayerMainInvWrapper subclass,
        // with canonical player keys separate from these raw indices around offhand.
        PlayerMainInvWrapper handler = new PlayerMainInvWrapper(player.inventory) {};
        ModularSlot target = new ModularSlot(handler, 81), hotbar = new ModularSlot(handler, 0);
        ItemStack marker = stack(17, "endpoint"), book = stack(3, "hotbar"), offhand = stack(2, "offhand");
        ItemStack cursor = stack(5, "cursor");
        target.putStack(marker);
        hotbar.putStack(book);
        player.inventory.mainInventory[36] = offhand;
        player.inventory.setItemStack(cursor);
        int total = quantity(player);
        assertTrue(ManagerPlayerSlotSwap.apply(target, hotbar, player));
        assertSame(book, player.inventory.mainInventory[81]);
        assertSame(marker, player.inventory.mainInventory[0]);
        assertSame(offhand, player.inventory.mainInventory[36]);
        assertSame(cursor, player.inventory.getItemStack());
        assertEquals(total, quantity(player));
        assertTrue(ManagerPlayerSlotSwap.apply(target, hotbar, player));
        assertSame(marker, player.inventory.mainInventory[81]);
        assertSame(book, player.inventory.mainInventory[0]);
        assertEquals(
            "endpoint",
            target.getStack()
                .getTagCompound()
                .getString("audit:identity"));
        assertEquals(
            "hotbar",
            hotbar.getStack()
                .getTagCompound()
                .getString("audit:identity"));
        for (int i = 1; i < 81; i++) if (i != 36) assertNull(player.inventory.mainInventory[i]);
        assertSame(cursor, player.inventory.getItemStack());
        assertEquals(total, quantity(player));
    }

    @Test
    void emptyTargetsAndEmptyHotbarMoveOnlyBetweenTheTwoIndices() throws Exception {
        for (boolean inHotbar : new boolean[] { false, true }) {
            FakePlayer player = player();
            PlayerMainInvWrapper handler = new PlayerMainInvWrapper(player.inventory);
            ModularSlot target = new ModularSlot(handler, 35), hotbar = new ModularSlot(handler, 8);
            ItemStack marker = stack(32, "only");
            (inHotbar ? hotbar : target).putStack(marker);
            assertTrue(ManagerPlayerSlotSwap.apply(target, hotbar, player));
            assertSame(marker, (inHotbar ? target : hotbar).getStack());
            assertNull((inHotbar ? hotbar : target).getStack());
            assertEquals(32, quantity(player));
            assertTrue(ManagerPlayerSlotSwap.apply(target, hotbar, player));
            assertSame(marker, (inHotbar ? hotbar : target).getStack());
            assertNull(player.inventory.getItemStack());
        }
    }

    @Test
    void sameUnderlyingSlotForeignOwnerAndNonHotbarDestinationAreRejected() throws Exception {
        FakePlayer player = player(), other = player();
        PlayerMainInvWrapper handler = new PlayerMainInvWrapper(player.inventory);
        ModularSlot target = new ModularSlot(handler, 35), hotbar = new ModularSlot(handler, 0);
        target.putStack(stack(4, "target"));
        hotbar.putStack(stack(7, "hotbar"));
        ItemStack[] before = snapshot(player);
        assertFalse(ManagerPlayerSlotSwap.apply(hotbar, new ModularSlot(handler, 0), player));
        ModularSlot foreign = new ModularSlot(new PlayerMainInvWrapper(other.inventory), 0);
        foreign.putStack(stack(11, "other"));
        assertFalse(ManagerPlayerSlotSwap.owns(foreign, player));
        assertFalse(ManagerPlayerSlotSwap.apply(target, foreign, player));
        assertFalse(ManagerPlayerSlotSwap.apply(foreign, hotbar, player));
        assertFalse(ManagerPlayerSlotSwap.apply(hotbar, target, player));
        assertFalse(ManagerPlayerSlotSwap.apply(null, hotbar, player));
        assertUnchanged(before, player);
        assertEquals(11, quantity(other));
    }

    @Test
    void bothSidesRespectEnabledExtractionInsertionFiltersAndWholeStackLimits() throws Exception {
        for (boolean restrictHotbar : new boolean[] { false, true })
            for (int restriction = 0; restriction < 6; restriction++) {
                FakePlayer player = player();
                PlayerMainInvWrapper handler = new PlayerMainInvWrapper(player.inventory);
                final int selectedRestriction = restriction;
                ModularSlot restricted = new ModularSlot(handler, restrictHotbar ? 0 : 35) {

                    @Override
                    public int getSlotStackLimit() {
                        return selectedRestriction == 4 ? 8 : super.getSlotStackLimit();
                    }

                    @Override
                    public int getItemStackLimit(ItemStack stack) {
                        return selectedRestriction == 5 ? 8 : super.getItemStackLimit(stack);
                    }
                };
                ModularSlot target = restrictHotbar ? new ModularSlot(handler, 35) : restricted;
                ModularSlot hotbar = restrictHotbar ? restricted : new ModularSlot(handler, 0);
                target.putStack(stack(17, "target"));
                hotbar.putStack(stack(23, "hotbar"));
                if (restriction == 0) restricted.setEnabled(false);
                if (restriction == 1) restricted.canTake(false);
                if (restriction == 2) restricted.canPut(false);
                if (restriction == 3) restricted.filter(stack -> false);
                ItemStack[] before = snapshot(player);
                assertFalse(ManagerPlayerSlotSwap.apply(target, hotbar, player));
                assertUnchanged(before, player);
                assertEquals(40, quantity(player));
            }
    }

    @Test
    void invalidNumberSwapIndicesReturnBeforeParentRouting() throws Exception {
        FakePlayer player = player();
        ManagerMui2Container container = new ManagerMui2Container(new TileEntityManager());
        container.inventorySlots.add(new ModularSlot(new PlayerMainInvWrapper(player.inventory), 35));
        for (int index : new int[] { -999, -1, 1, Integer.MAX_VALUE })
            assertNull(container.slotClick(index, 0, 2, player));
        for (int button : new int[] { -1, 9, Integer.MAX_VALUE }) assertNull(container.slotClick(0, button, 2, player));
    }

    private static FakePlayer player() throws Exception {
        FakePlayer player = allocate(FakePlayer.class);
        player.worldObj = world();
        player.inventory = new InventoryPlayer(player);
        return player;
    }

    private static ItemStack stack(int count, String identity) {
        ItemStack stack = new ItemStack(ITEM, count, 0);
        stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound()
            .setString("audit:identity", identity);
        return stack;
    }

    private static int quantity(FakePlayer player) {
        int count = player.inventory.getItemStack() == null ? 0 : player.inventory.getItemStack().stackSize;
        for (ItemStack stack : player.inventory.mainInventory) if (stack != null) count += stack.stackSize;
        return count;
    }

    private static ItemStack[] snapshot(FakePlayer player) {
        ItemStack[] snapshot = new ItemStack[player.inventory.mainInventory.length];
        for (int i = 0; i < snapshot.length; i++) {
            ItemStack stack = player.inventory.mainInventory[i];
            snapshot[i] = stack == null ? null : stack.copy();
        }
        return snapshot;
    }

    private static void assertUnchanged(ItemStack[] before, FakePlayer player) {
        for (int i = 0; i < before.length; i++) assertTrue(
            ItemStack.areItemStacksEqual(before[i], player.inventory.mainInventory[i]),
            "raw player index " + i);
        assertNull(player.inventory.getItemStack());
    }
}
