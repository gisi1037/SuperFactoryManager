package ca.teamdman.sfml.data;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** Immutable exact item key used by contents observations and native item transfers. */
public final class ItemIdentity {

    private final String registryId;
    private final int metadata;
    private final BoundedItemNbt nbt;
    private final int hash;

    private ItemIdentity(String registryId, int metadata, BoundedItemNbt nbt) {
        this.registryId = registryId;
        this.metadata = metadata;
        this.nbt = nbt;
        this.hash = 31 * (31 * registryId.hashCode() + metadata) + nbt.hashCode();
    }

    /** Metadata is exact (including values sometimes used as selector wildcards). Count is not identity. */
    public static ItemIdentity of(String registryId, int metadata, NBTTagCompound nbt, QueryBudget budget) {
        if (budget == null) throw new IllegalArgumentException("Item identity requires a query budget");
        if (!validRegistryId(registryId)) throw new IllegalArgumentException("Item identity requires namespace:name");
        budget.chargeSlots(registryId.length());
        budget.chargeAllocation(1); // One retained key; registryId is already immutable.
        return new ItemIdentity(registryId, metadata, BoundedItemNbt.snapshot(nbt, budget));
    }

    /** Call on the server thread. External registry/stack callbacks cannot be preempted by this budget. */
    public static ItemIdentity fromStack(ItemStack stack, QueryBudget budget) {
        if (stack == null || stack.getItem() == null) throw new IllegalArgumentException("Missing item stack");
        String id = Item.itemRegistry.getNameForObject(stack.getItem());
        return of(id, stack.getItemDamage(), stack.getTagCompound(), budget);
    }

    public String getRegistryId() {
        return registryId;
    }

    public int getMetadata() {
        return metadata;
    }

    int getRetainedCharacters() {
        return Math.addExact(registryId.length(), nbt.getRetainedCharacters());
    }

    int getRetainedOpaqueBytes() {
        return Math
            .addExact(40, Math.addExact(BoundedItemNbt.retainedStringBytes(registryId), nbt.getRetainedOpaqueBytes()));
    }

    private static boolean validRegistryId(String id) {
        if (id == null || id.isEmpty() || id.length() > 256) return false;
        int colon = -1;
        for (int i = 0; i < id.length(); i++) {
            char c = id.charAt(i);
            if (c == ':') {
                if (colon >= 0 || i == 0 || i == id.length() - 1) return false;
                colon = i;
            } else if (!(c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z'
                || c >= '0' && c <= '9'
                || c == '_'
                || c == '.'
                || c == '-'
                || c == '/' && colon >= 0)) return false;
        }
        return colon >= 0;
    }

    /** Conservative live-match units: registry comparison plus snapshot-derived NBT byte/node/child/key work. */
    public int getComparisonWork() {
        return 1 + registryId.length() + nbt.getComparisonWork();
    }

    /** Stack-copy work shares immutable registry/item identity and copies only the bounded NBT payload. */
    public int getCopyWork() {
        return 1 + nbt.getLength();
    }

    /**
     * Exact live comparison without another canonical snapshot. Native callers charge their provider boundary;
     * synchronous stack/registry/NBT getters cannot themselves be preempted. A false result grants no authority
     * and may stop before unrelated descendants; malformed data encountered during comparison still faults.
     */
    public boolean matchesStack(ItemStack stack, QueryBudget budget) {
        if (budget == null) throw new IllegalArgumentException("Item matching requires a query budget");
        budget.chargeSlot();
        if (stack == null || stack.getItem() == null || stack.getItemDamage() != metadata) return false;
        String actual = Item.itemRegistry.getNameForObject(stack.getItem());
        if (actual == null || actual.length() != registryId.length()) return false;
        budget.chargeSlots(registryId.length());
        return registryId.equals(actual) && nbt.matches(stack.getTagCompound(), budget);
    }

    /** Budgeted exact matching for native scan hash buckets; reserves comparisons before walking payloads. */
    public boolean matches(ItemIdentity other, QueryBudget budget) {
        if (budget == null) throw new IllegalArgumentException("Item matching requires a query budget");
        budget.chargeSlot();
        if (this == other) return true;
        if (other == null || hash != other.hash
            || metadata != other.metadata
            || registryId.length() != other.registryId.length()) return false;
        budget.chargeSlots(registryId.length());
        if (!registryId.equals(other.registryId) || nbt.getLength() != other.nbt.getLength()) return false;
        budget.chargeSlots(nbt.getLength());
        return nbt.equals(other.nbt);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ItemIdentity)) return false;
        ItemIdentity that = (ItemIdentity) other;
        return hash == that.hash && metadata == that.metadata
            && registryId.equals(that.registryId)
            && nbt.equals(that.nbt);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
