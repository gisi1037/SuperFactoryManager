package ca.teamdman.sfml.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public final class ResourceIdSet implements ASTNode {

    public static final ResourceIdSet MATCH_ALL = new ResourceIdSet(
        Collections.singletonList(ResourceIdentifier.MATCH_ALL));

    private final List<ResourceIdentifier> identifiers;

    public ResourceIdSet(List<ResourceIdentifier> identifiers) {
        this.identifiers = identifiers != null ? identifiers : Collections.emptyList();
    }

    public List<ResourceIdentifier> getIdentifiers() {
        return identifiers;
    }

    public boolean matchesItem(ItemStack stack) {
        if (identifiers.isEmpty()) {
            return true;
        }
        for (ResourceIdentifier id : identifiers) {
            if (id.matchesItem(stack)) {
                return true;
            }
        }
        return false;
    }

    public boolean matchesFluid(FluidStack fluid) {
        if (identifiers.isEmpty()) {
            return true;
        }
        for (ResourceIdentifier id : identifiers) {
            if (id.matchesFluid(fluid)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasFluidType() {
        for (ResourceIdentifier id : identifiers) {
            if (id.getType() == ResourceIdentifier.Type.FLUID) {
                return true;
            }
        }
        return false;
    }

    public boolean hasItemType() {
        for (ResourceIdentifier id : identifiers) {
            if (id.getType() == ResourceIdentifier.Type.ITEM || id.getType() == ResourceIdentifier.Type.ANY) {
                return true;
            }
        }
        return false;
    }

    public static ResourceIdSet of(ResourceIdentifier... ids) {
        List<ResourceIdentifier> list = new ArrayList<>();
        Collections.addAll(list, ids);
        return new ResourceIdSet(list);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < identifiers.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(identifiers.get(i));
        }
        return sb.toString();
    }
}
