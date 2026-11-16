package buildcraft.factory;

import net.minecraft.tileentity.TileEntity;

import buildcraft.core.lib.fluids.Tank;

/** Exercises adapter discovery and notifications without loading the BuildCraft runtime. */
public final class TileTank extends TileEntity {

    public final Tank tank = new Tank(16000);
    public boolean hasNetworkUpdate;
    public int comparatorUpdates;

    protected void updateComparators() {
        comparatorUpdates++;
    }
}
