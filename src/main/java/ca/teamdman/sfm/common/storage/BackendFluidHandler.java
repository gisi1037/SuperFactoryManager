package ca.teamdman.sfm.common.storage;

import java.util.List;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import ca.teamdman.sfml.data.QueryBudget;

/** A transaction-local bridge to authoritative network fill/drain returns. No fabricated Forge tanks. */
public final class BackendFluidHandler implements IFluidHandler {

    public final StorageBackend backend;
    private final List<StorageBackend.StoredFluid> rows;
    private QueryBudget budget;

    public BackendFluidHandler(StorageBackend backend, QueryBudget budget, boolean observeStock) {
        this.backend = backend;
        rows = observeStock ? backend.fluids(budget) : java.util.Collections.emptyList();
        if (rows.size() > 1024) throw new IllegalArgumentException("Adapter fluid snapshot exceeds 1024 types");
        this.budget = budget;
    }

    public List<StorageBackend.StoredFluid> rows() {
        return rows;
    }

    public boolean current(QueryBudget budget) {
        this.budget = budget;
        return backend.current(budget);
    }

    @Override
    public int fill(ForgeDirection side, FluidStack offered, boolean actual) {
        return backend.fill(offered, !actual, budget);
    }

    @Override
    public FluidStack drain(ForgeDirection side, FluidStack requested, boolean actual) {
        return backend.drain(requested, !actual, budget);
    }

    @Override
    public FluidStack drain(ForgeDirection side, int amount, boolean actual) {
        throw new UnsupportedOperationException("Adapter drains require an exact fluid key");
    }

    @Override
    public boolean canFill(ForgeDirection side, Fluid fluid) {
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection side, Fluid fluid) {
        return true;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection side) {
        throw new UnsupportedOperationException("Use the bounded adapter fluid snapshot");
    }
}
