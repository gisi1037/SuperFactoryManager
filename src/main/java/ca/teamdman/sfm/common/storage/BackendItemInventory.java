package ca.teamdman.sfm.common.storage;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import ca.teamdman.sfml.data.ItemIdentity;
import ca.teamdman.sfml.data.QueryBudget;

/** Operation-local index of exact keys. Slot zero is insertion; remaining indexes are snapshot rows. */
public final class BackendItemInventory implements IInventory {

    public final StorageBackend backend;
    private final List<StorageBackend.StoredItem> rows;
    private final ItemStack[] displayed;

    public BackendItemInventory(StorageBackend backend, QueryBudget budget, boolean observeStock) {
        this.backend = backend;
        rows = observeStock ? backend.items(budget) : java.util.Collections.emptyList();
        if (rows.size() > 1024) throw new IllegalArgumentException("Adapter item snapshot exceeds 1024 types");
        budget.chargeAllocation(rows.size() + 1);
        displayed = new ItemStack[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            StorageBackend.StoredItem row = rows.get(i);
            if (row.amount <= 0) throw new IllegalArgumentException("Invalid adapter item count");
            ItemIdentity key = ItemIdentity.fromStack(row.item, budget);
            budget.chargeSlots(key.getCopyWork());
            budget.chargeAllocation(key.getCopyWork() * 2 + 4);
            displayed[i] = row.item.copy();
            displayed[i].stackSize = (int) Math.min(Integer.MAX_VALUE, row.amount);
        }
    }

    public long amount(int slot) {
        return slot == 0 ? 0 : rows.get(slot - 1).amount;
    }

    @Override
    public int getSizeInventory() {
        return displayed.length + 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot == 0 ? null : displayed[slot - 1];
    }

    // Mutations require an explicit backend operation under the transfer guard and recovery budget.
    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        throw new UnsupportedOperationException("Use a guarded adapter extraction");
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        throw new UnsupportedOperationException("Use a guarded adapter insertion");
    }

    @Override
    public int getInventoryStackLimit() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot == 0;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return false;
    }

    @Override
    public String getInventoryName() {
        return "SFM adapter snapshot";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public void markDirty() {}

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}
}
