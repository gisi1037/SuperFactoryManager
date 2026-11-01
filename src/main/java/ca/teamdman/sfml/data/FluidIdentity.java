package ca.teamdman.sfml.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

/** Immutable fluid/NBT identity. No tank, world or mutable provider payload escapes a snapshot. */
public final class FluidIdentity {

    private final Fluid fluid;
    private final String id;
    private final BoundedItemNbt nbt;
    private final NBTTagCompound tag;

    private FluidIdentity(Fluid fluid, String id, BoundedItemNbt nbt, NBTTagCompound tag) {
        this.fluid = fluid;
        this.id = id;
        this.nbt = nbt;
        this.tag = tag;
    }

    public static FluidIdentity fromStack(FluidStack stack, QueryBudget budget) {
        if (stack == null || stack.getFluid() == null || stack.amount < 0)
            throw new IllegalArgumentException("Invalid fluid stack");
        String id = stack.getFluid()
            .getName();
        if (id == null || id.length() > 256) throw new IllegalArgumentException("Invalid fluid registry name");
        BoundedItemNbt nbt = BoundedItemNbt.snapshot(stack.tag, budget);
        budget.chargeAllocation(2 + nbt.getLength());
        budget.chargeSlots(nbt.getLength());
        return new FluidIdentity(
            stack.getFluid(),
            id,
            nbt,
            stack.tag == null ? null : (NBTTagCompound) stack.tag.copy());
    }

    public String getId() {
        return id;
    }

    public int copyWork() {
        return 1 + nbt.getLength();
    }

    public int comparisonWork() {
        return 1 + nbt.getComparisonWork();
    }

    public int getRetainedCharacters() {
        return id.length() + nbt.getRetainedCharacters();
    }

    public int getRetainedOpaqueBytes() {
        return 64 + nbt.getRetainedOpaqueBytes() + nbt.getLength() * 32;
    }

    public boolean matches(FluidStack stack, QueryBudget budget) {
        budget.chargeSlot();
        return stack != null && stack.getFluid() == fluid && nbt.matches(stack.tag, budget);
    }

    public boolean matches(FluidIdentity other, QueryBudget budget) {
        budget.chargeSlots(1 + nbt.getLength());
        return other != null && fluid == other.fluid && nbt.equals(other.nbt);
    }

    public FluidStack stack(int amount, QueryBudget budget) {
        budget.chargeAllocation(copyWork());
        budget.chargeSlots(copyWork());
        // The Forge constructor copies NBT; do not copy it a second time here.
        return new FluidStack(fluid, amount, tag);
    }
}
