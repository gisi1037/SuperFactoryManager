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
        new Limit(ResourceQuantity.UNLIMITED, ResourceQuantity.ZERO),
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
}
