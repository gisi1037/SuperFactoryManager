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
        if (limit.getQuantity()
            .isDynamic()
            || limit.getRetention()
                .isDynamic()) {
            // Keep both retention extremes and EACH, without leaking internal wildcard patterns.
            sb.append(limit.getQuantity())
                .append(" RETAIN ")
                .append(limit.getRetention())
                .append(" ");
            for (int i = 0; i < resourceIds.getIdentifiers()
                .size(); i++) {
                if (i > 0) sb.append(" OR ");
                ResourceIdentifier id = resourceIds.getIdentifiers()
                    .get(i);
                if (id == ResourceIdentifier.MATCH_ALL) sb.append("*");
                else sb.append('"')
                    .append(
                        id.toString()
                            .replace("\"", "\\\""))
                    .append('"');
            }
            if (with.getTagMatcher() != null) {
                sb.append(with.isNegated() ? " WITHOUT TAG " : " WITH TAG ");
                sb.append(
                    with.getTagMatcher()
                        .getPatternString());
            }
            return sb.toString();
        }
        if (limit != null && limit.getQuantity()
            .getAmount() != Long.MAX_VALUE) {
            sb.append(
                limit.getQuantity()
                    .getAmount())
                .append(" ");
        }
        sb.append(resourceIds);
        if (limit != null && limit.getRetention()
            .getAmount() > 0) {
            sb.append(" RETAIN ")
                .append(
                    limit.getRetention()
                        .getAmount());
        }
        return sb.toString();
    }
}
