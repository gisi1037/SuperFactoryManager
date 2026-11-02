package ca.teamdman.sfm.common.recipe;

import java.util.function.LongSupplier;

import net.minecraft.tileentity.TileEntity;

import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfm.compat.gregtech.GregTechMachineRecipeMapAdapter;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.MachineRecipeMaps;
import ca.teamdman.sfml.data.MachineRecipeMaps.Status;
import ca.teamdman.sfml.data.QueryBudget;

/** Fresh loaded-only machine observations. No provider, mode or availability cache survives a call. */
public final class MachineRecipeMapService {

    public interface Access {

        MachineRecipeMaps maps();

        boolean current(TileEntity tile, QueryBudget budget);
    }

    public interface Adapter {

        Access find(TileEntity tile, QueryBudget budget);
    }

    private final ProgramContext context;
    private final Adapter adapter;
    private final LongSupplier epoch;

    public MachineRecipeMapService(ProgramContext context) {
        this(context, GregTechMachineRecipeMapAdapter::find, RecipeCatalogService.INSTANCE::observationEpoch);
    }

    public MachineRecipeMapService(ProgramContext context, Adapter adapter, LongSupplier epoch) {
        this.context = context;
        this.adapter = adapter;
        this.epoch = epoch;
    }

    public MachineRecipeMaps inspect(LabelReference label, QueryBudget budget) {
        if (label == null || label.hasSlotSelection() || label.getSideMask() != 63)
            throw new IllegalArgumentException("RECIPEMAPS requires a whole-machine label without side/slot selection");
        if (context.getWorld() == null || context.getNetwork() == null) return absent(Status.UNAVAILABLE);
        if (context.getWorld().isRemote
            || net.minecraft.server.MinecraftServer.getServer() != null && !ServerUiTasks.isServerThread())
            throw new IllegalStateException("Machine recipe maps require the server thread");
        long session = epoch.getAsLong();
        if (session < 0) return absent(Status.NOT_READY);
        TileEntityManager manager = context.getManager();
        Object disk = manager == null ? null : manager.getDisk();
        long generation = manager == null ? 0 : manager.getDiskGeneration();
        TileEntity selected = null;
        BlockPos selectedPosition = null;
        for (BlockPos position : context.getLabelPositionHolder()
            .getPositions(label.getName())) {
            TileEntity tile = tile(position, budget);
            if (tile == null) return absent(Status.UNAVAILABLE);
            if (selected != null && selected != tile) return absent(Status.AMBIGUOUS);
            selected = tile;
            selectedPosition = position;
        }
        if (selected == null) return absent(Status.UNAVAILABLE);
        budget.chargeAllocation(4);
        Access access = adapter.find(selected, budget);
        if (tile(selectedPosition, budget) != selected || !authority(manager, disk, generation, budget))
            return absent(Status.UNAVAILABLE);
        if (epoch.getAsLong() != session) return absent(Status.STALE);
        if (access == null) return absent(Status.UNSUPPORTED);
        MachineRecipeMaps maps = access.maps();
        budget.chargeProvider();
        if (!access.current(selected, budget) || tile(selectedPosition, budget) != selected
            || !authority(manager, disk, generation, budget)) return absent(Status.UNAVAILABLE);
        return epoch.getAsLong() == session ? maps : absent(Status.STALE);
    }

    private boolean authority(TileEntityManager manager, Object disk, long generation, QueryBudget budget) {
        if (manager == null) return true;
        if (manager.isInvalid() || manager.getWorldObj() != context.getWorld()
            || manager.getDisk() != disk
            || manager.getDiskGeneration() != generation
            || manager.hasPendingRecovery()) return false;
        budget.chargePosition();
        budget.chargeProviders(2);
        BlockPos position = manager.getBlockPos();
        return position.isLoaded(context.getWorld()) && context.getWorld()
            .getTileEntity(position.x, position.y, position.z) == manager
            && !manager.isInvalid()
            && manager.getWorldObj() == context.getWorld()
            && manager.getDisk() == disk
            && manager.getDiskGeneration() == generation
            && !manager.hasPendingRecovery();
    }

    private TileEntity tile(BlockPos position, QueryBudget budget) {
        budget.chargePosition();
        budget.chargeProviders(3);
        if (!context.getNetwork()
            .isConnected(position) || !position.isLoaded(context.getWorld())) return null;
        TileEntity tile = context.getWorld()
            .getTileEntity(position.x, position.y, position.z);
        return tile != null && !tile.isInvalid() ? tile : null;
    }

    private static MachineRecipeMaps absent(Status status) {
        return MachineRecipeMaps.unavailable(status);
    }
}
