package ca.teamdman.sfm.common.program;

import net.minecraft.tileentity.TileEntity;

import ca.teamdman.sfml.data.QueryBudget;

/** A qualified ghost-setting adapter; no physical circuit item may be consumed or created. */
public interface GhostCircuitAccess {

    boolean current(TileEntity tile, QueryBudget budget);

    int get(QueryBudget budget);

    void set(int configuration, QueryBudget budget);

    interface Adapter {

        GhostCircuitAccess find(TileEntity tile, QueryBudget budget);
    }
}
