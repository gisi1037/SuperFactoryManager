package ca.teamdman.sfml.ast;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public final class ResourceLimit implements ASTNode {

    public static final ResourceLimit DEFAULT_INPUT = new ResourceLimit(
        ResourceIdSet.MATCH_ALL,
        new Limit(ResourceQuantity.UNLIMITED, ResourceQuantity.ZERO),
        With.ALWAYS_TRUE);

    public static final ResourceLimit DEFAULT_OUTPUT = new ResourceLimit(
        ResourceIdSet.MATCH_ALL,
        new Limit(ResourceQuantity.UNLIMITED, ResourceQuantity.UNLIMITED),
        With.ALWAYS_TRUE);

    private final ResourceIdSet resourceIds;
    private final Limit limit;
    private final With with;

    public ResourceLimit(ResourceIdSet resourceIds, Limit limit, With with) {
        this.resourceIds = resourceIds != null ? resourceIds : ResourceIdSet.MATCH_ALL;
        this.limit = limit != null ? limit : Limit.UNLIMITED;
        this.with = with != null ? with : With.ALWAYS_TRUE;
    }

    public ResourceIdSet getResourceIds() {
        return resourceIds;
    }

    public Limit getLimit() {
        return limit;
    }

    public With getWith() {
        return with;
    }

    public boolean matchesItem(ItemStack stack) {
        if (!resourceIds.matchesItem(stack)) {
            return false;
        }
        return with.matches(stack);
    }

    public boolean matchesFluid(FluidStack fluid) {
        return resourceIds.matchesFluid(fluid);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // Retention precedes selectors. Keep zero/MAX and EACH explicit for both IO defaults.
        sb.append(limit.getQuantity())
            .append(" RETAIN ")
            .append(limit.getRetention())
            .append(" ");
        if (resourceIds.getIdentifiers()
            .isEmpty()) sb.append(ResourceIdentifier.MATCH_ALL);
        for (int i = 0; i < resourceIds.getIdentifiers()
            .size(); i++) {
            if (i > 0) sb.append(" OR ");
            sb.append(
                resourceIds.getIdentifiers()
                    .get(i));
        }
        if (with.getTagMatcher() != null) {
            sb.append(with.isNegated() ? " WITHOUT TAG " : " WITH TAG ");
            sb.append(
                with.getTagMatcher()
                    .getPatternString());
        }
        return sb.toString();
    }
}
