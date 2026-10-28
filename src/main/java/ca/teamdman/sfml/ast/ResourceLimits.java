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
    private final boolean dynamic;

    public ResourceLimits(List<ResourceLimit> limits, ResourceIdSet exclusions) {
        this.limits = limits != null && !limits.isEmpty() ? limits
            : Collections.singletonList(ResourceLimit.DEFAULT_INPUT);
        this.exclusions = exclusions;
        boolean found = false;
        for (ResourceLimit entry : this.limits) found |= entry.getLimit()
            .getQuantity()
            .isDynamic()
            || entry.getLimit()
                .getRetention()
                .isDynamic();
        this.dynamic = found;
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

    @Override
    public String toString() {
        if (this == DEFAULT || (limits.size() == 1 && limits.get(0)
            .getResourceIds() == ResourceIdSet.MATCH_ALL
            && limits.get(0)
                .getLimit() == Limit.UNLIMITED
            && exclusions == null)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < limits.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(limits.get(i));
        }
        if (exclusions != null) {
            sb.append(" EXCEPT ")
                .append(exclusions);
        }
        return sb.toString();
    }

    public ResourceLimits resolve(ca.teamdman.sfm.common.program.ProgramContext context) {
        if (!dynamic) return this;
        List<ResourceLimit> resolved = new java.util.ArrayList<>(limits.size());
        for (ResourceLimit entry : limits) {
            Limit value = entry.getLimit();
            ResourceQuantity quantity = value.getQuantity()
                .resolve(context);
            ResourceQuantity retention = value.getRetention()
                .resolve(context);
            resolved.add(new ResourceLimit(entry.getResourceIds(), new Limit(quantity, retention), entry.getWith()));
        }
        return new ResourceLimits(resolved, exclusions);
    }
}
