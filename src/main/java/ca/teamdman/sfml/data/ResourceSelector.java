package ca.teamdman.sfml.data;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/** Prevalidated item-domain selector. Matching never enumerates the item or ore registries. */
public final class ResourceSelector {

    public enum Kind {
        ITEM,
        ORE
    }

    private static final ResourceSelector ALL_ITEMS = new ResourceSelector(Kind.ITEM, "*");
    private final Kind kind;
    private final String id;

    private ResourceSelector(Kind kind, String id) {
        this.kind = kind;
        this.id = id;
    }

    public static ResourceSelector item(String id) {
        if (id == null || id.length() > 256 || !(id.equals("*") || id.matches("[A-Za-z0-9_.-]+:[A-Za-z0-9_./-]+")))
            throw new IllegalArgumentException(
                "ITEM needs namespace:name or '*'; patterns, tags, metadata and fluid selectors are not supported yet");
        return id.equals("*") ? ALL_ITEMS : new ResourceSelector(Kind.ITEM, id);
    }

    /** Exact, case-sensitive OreDictionary membership; construction does not register or resolve an ore ID. */
    public static ResourceSelector ore(String name) {
        if (name == null || name.isEmpty() || name.length() > 256 || !name.matches("[A-Za-z0-9_]+"))
            throw new IllegalArgumentException(
                "ORE needs an exact ore name of 1 to 256 ASCII letters, digits or underscores; patterns are unsupported");
        return new ResourceSelector(Kind.ORE, name);
    }

    public Kind getKind() {
        return kind;
    }

    /** Item registry ID (or '*') for ITEM; exact OreDictionary name for ORE. */
    public String getId() {
        return id;
    }

    public boolean matches(ItemStack stack) {
        return matches(stack, null);
    }

    public boolean matches(ItemStack stack, QueryBudget budget) {
        if (stack == null || stack.getItem() == null) return false;
        if (kind == Kind.ORE) {
            // Read memberships afresh: registrations may arrive after this selector was compiled.
            // getOreID(name) would create unknown names; only inspect this stack's existing memberships.
            int[] oreIds = OreDictionary.getOreIDs(stack);
            // Forge returns this array from an external callback; its internal allocation cannot be preempted.
            // Reserve every returned entry before our matching walk, including entries after an early match.
            if (budget != null) {
                budget.chargeSlots(oreIds.length);
                budget.chargeAllocation(oreIds.length);
            }
            for (int oreId : oreIds) if (id.equals(OreDictionary.getOreName(oreId))) return true;
            return false;
        }
        if (id.equals("*")) return true;
        String registered = Item.itemRegistry == null ? null : Item.itemRegistry.getNameForObject(stack.getItem());
        return id.equals(registered);
    }

    @Override
    public String toString() {
        return kind.name() + "(" + DataValue.string(id) + ")";
    }
}
