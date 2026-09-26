package ca.teamdman.sfml.ast;

import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public final class ResourceLimits implements ASTNode {

    public static final ResourceLimits DEFAULT = new ResourceLimits(
        Collections.singletonList(ResourceLimit.DEFAULT_INPUT),
        null);

    private final List<ResourceLimit> limits;
    private final ResourceIdSet exclusions;

    public ResourceLimits(List<ResourceLimit> limits, ResourceIdSet exclusions) {
        this.limits = limits != null && !limits.isEmpty() ? limits
            : Collections.singletonList(ResourceLimit.DEFAULT_INPUT);
        this.exclusions = exclusions;
    }

    public List<ResourceLimit> getLimits() {
        return limits;
    }

    public ResourceIdSet getExclusions() {
        return exclusions;
    }

    public boolean matchesItem(ItemStack stack) {
        if (stack == null) return false;
        if (exclusions != null && exclusions.matchesItem(stack)) {
            return false;
        }
        for (ResourceLimit limit : limits) {
            if (limit.matchesItem(stack)) {
                return true;
            }
        }
        return false;
    }

    public boolean matchesFluid(FluidStack fluid) {
        if (fluid == null) return false;
        if (exclusions != null && exclusions.matchesFluid(fluid)) {
            return false;
        }
        for (ResourceLimit limit : limits) {
            if (limit.matchesFluid(fluid)) {
                return true;
            }
        }
        return false;
    }

    public ResourceLimit getFirstMatchingLimit(ItemStack stack) {
        for (ResourceLimit limit : limits) {
            if (limit.matchesItem(stack)) {
                return limit;
            }
        }
        return null;
    }

    public ResourceLimit getFirstMatchingLimit(FluidStack fluid) {
        for (ResourceLimit limit : limits) {
            if (limit.matchesFluid(fluid)) {
                return limit;
            }
        }
        return null;
    }
}
