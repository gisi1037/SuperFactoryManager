package ca.teamdman.sfml.data;

import java.util.regex.Pattern;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

/** Prevalidated resource selector. Matching never enumerates resource registries. */
public final class ResourceSelector {

    public enum Kind {
        ITEM,
        ORE,
        FLUID
    }

    private static final ResourceSelector ALL_ITEMS = new ResourceSelector(Kind.ITEM, "*");
    private static final ResourceSelector ALL_FLUIDS = new ResourceSelector(Kind.FLUID, "*");
    private static final Pattern ORE_NAME = Pattern.compile("[A-Za-z0-9_]+");
    private static final Pattern FLUID_NAME = Pattern.compile("[A-Za-z0-9_.:/-]+");
    private final Kind kind;
    private final String id;
    private final boolean inverted;

    private ResourceSelector(Kind kind, String id) {
        this(kind, id, false);
    }

    private ResourceSelector(Kind kind, String id, boolean inverted) {
        this.kind = kind;
        this.id = id;
        this.inverted = inverted;
    }

    public static ResourceSelector item(String id) {
        if (id == null || !(id.equals("*") || ItemIdentity.validRegistryId(id))) throw new IllegalArgumentException(
            "ITEM needs namespace:name or '*'; patterns, tags, metadata and fluid selectors are not supported yet");
        return id.equals("*") ? ALL_ITEMS : new ResourceSelector(Kind.ITEM, id);
    }

    /** Exact, case-sensitive OreDictionary membership; construction does not register or resolve an ore ID. */
    public static ResourceSelector ore(String name) {
        if (name == null || name.length() > 256
            || !ORE_NAME.matcher(name)
                .matches())
            throw new IllegalArgumentException(
                "ORE needs an exact ore name of 1 to 256 ASCII letters, digits or underscores; patterns are unsupported");
        return new ResourceSelector(Kind.ORE, name);
    }

    /** Exact Forge fluid registry name, or '*' for every fluid; amounts are millibuckets. */
    public static ResourceSelector fluid(String name) {
        if ("*".equals(name)) return ALL_FLUIDS;
        if (name == null || name.length() > 256
            || !FLUID_NAME.matcher(name)
                .matches())
            throw new IllegalArgumentException("FLUID needs an exact Forge fluid name or '*'");
        return new ResourceSelector(Kind.FLUID, name);
    }

    public ResourceSelector invert() {
        return new ResourceSelector(kind, id, !inverted);
    }

    public boolean isInverted() {
        return inverted;
    }

    public boolean matchesFluid(FluidStack stack) {
        return stack != null && stack.getFluid() != null
            && matchesFluidId(
                stack.getFluid()
                    .getName());
    }

    public boolean matchesFluidId(String registryId) {
        return kind == Kind.FLUID && registryId != null && ((id.equals("*") || id.equals(registryId)) != inverted);
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
        if (kind == Kind.FLUID) return false;
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
            for (int oreId : oreIds) if (id.equals(OreDictionary.getOreName(oreId))) return !inverted;
            return inverted;
        }
        if (id.equals("*")) return !inverted;
        String registered = Item.itemRegistry == null ? null : Item.itemRegistry.getNameForObject(stack.getItem());
        return id.equals(registered) != inverted;
    }

    @Override
    public String toString() {
        return (inverted ? "!" : "") + kind.name() + "(" + DataValue.string(id) + ")";
    }
}
