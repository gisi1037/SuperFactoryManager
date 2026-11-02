package ca.teamdman.sfm.audit;

import java.lang.reflect.Field;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import cpw.mods.fml.common.registry.RegistryDelegate;
import sun.misc.Unsafe;

/** Headless tank payload: real FluidStack identity/NBT rules, without Forge registry startup. */
public final class TestFluid extends FluidStack {

    public static final Fluid WATER = new Fluid("water");

    private TestFluid() {
        super(WATER, 0);
    }

    public static TestFluid water(int amount) {
        return of(WATER, amount);
    }

    public static TestFluid of(Fluid fluid, int amount) {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            TestFluid stack = (TestFluid) ((Unsafe) f.get(null)).allocateInstance(TestFluid.class);
            Field delegate = FluidStack.class.getDeclaredField("fluidDelegate");
            delegate.setAccessible(true);
            delegate.set(stack, new RegistryDelegate<Fluid>() {

                @Override
                public Fluid get() {
                    return fluid;
                }

                @Override
                public String name() {
                    return fluid.getName();
                }

                @Override
                public Class<Fluid> type() {
                    return Fluid.class;
                }
            });
            stack.amount = amount;
            return stack;
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public FluidStack copy() {
        TestFluid copy = of(getFluid(), amount);
        copy.tag = tag == null ? null : (NBTTagCompound) tag.copy();
        return copy;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setString("FluidName", getFluid().getName());
        tag.setInteger("Amount", amount);
        if (this.tag != null) tag.setTag("Tag", this.tag.copy());
        return tag;
    }
}
