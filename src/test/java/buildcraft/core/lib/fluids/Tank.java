package buildcraft.core.lib.fluids;

import net.minecraftforge.fluids.FluidTank;

/** Headless stand-in for the optional BuildCraft backing storage shape. */
public final class Tank extends FluidTank {

    public Tank(int capacity) {
        super(capacity);
    }
}
