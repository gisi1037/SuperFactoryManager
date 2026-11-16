package ca.teamdman.sfm.common.program;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.tileentity.TileEntityRedstonePort;
import ca.teamdman.sfm.common.util.BlockPos;
import ca.teamdman.sfm.common.util.LoadedRedstoneAccess;
import ca.teamdman.sfm.compat.Compat;
import ca.teamdman.sfm.compat.stevefm.SteveFMCompat;
import ca.teamdman.sfml.ast.LabelAccess;
import ca.teamdman.sfml.ast.Side;
import ca.teamdman.sfml.data.LabelReference;
import ca.teamdman.sfml.data.QueryBudget;

/** Fresh native-port observations. A missing member never becomes a fictitious zero signal. */
public final class RedstoneQueryService {

    private final ProgramContext context;

    public RedstoneQueryService(ProgramContext context) {
        this.context = context;
    }

    public long redstone(LabelReference source, QueryBudget budget) {
        int level = observe(source, budget);
        if (level < 0)
            throw new IllegalArgumentException("Redstone query unavailable for LABEL(" + source.getName() + ")");
        return level;
    }

    public boolean powered(LabelReference source, QueryBudget budget) {
        return redstone(source, budget) > 0;
    }

    public boolean available(LabelReference source, QueryBudget budget) {
        return observe(source, budget) >= 0;
    }

    List<Integer> labelledLevels(LabelAccess selection, QueryBudget budget) {
        World world = context.getWorld();
        if (world != null && (world.isRemote || MinecraftServer.getServer() != null && !ServerUiTasks.isServerThread()))
            throw new IllegalArgumentException("Redstone queries require server-thread execution");
        LabelAccess access = selection.resolve(context);
        if (!access.getSlots()
            .isAll() || access.hasHandleSlots())
            throw new IllegalArgumentException("Labelled REDSTONE does not accept item slot restrictions");
        budget.chargeAllocation(4);
        LabelPositionHolder holder = context.getLabelPositionHolder();
        List<BlockPos> positions = access.getPositions(holder, budget);
        budget.chargeAllocation(positions.size() * 2);
        List<Integer> levels = new ArrayList<>(positions.size());
        LoadedRedstoneAccess reads = new LoadedRedstoneAccess();
        reads.attach(world, budget::chargeProvider);
        for (BlockPos pos : positions) {
            budget.chargeProviders(2);
            if (world == null || context.getNetwork() == null
                || !pos.isLoaded(world)
                || !context.getNetwork()
                    .isConnected(pos))
                throw unavailable(pos);
            int mask = 0;
            budget.chargeAllocation(1);
            for (Side side : access.getSides(pos, holder, budget)
                .getSides()) {
                if (side == Side.NULL) throw new IllegalArgumentException("Labelled REDSTONE requires physical faces");
                mask |= 1 << side.getDirection()
                    .ordinal();
            }
            if (mask == 0) throw unavailable(pos);
            reads.reset();
            int level = 0;
            for (ForgeDirection face : ForgeDirection.VALID_DIRECTIONS) {
                if ((mask & 1 << face.ordinal()) == 0) continue;
                int y = pos.y + face.offsetY;
                if (y < 0 || y >= 256) continue; // Vanilla treats positions beyond the world height as air.
                int x = pos.x + face.offsetX, z = pos.z + face.offsetZ;
                Block source = reads.getBlock(x, y, z);
                if (!reads.isComplete()) throw unavailable(pos);
                budget.chargeProvider();
                level = Math.max(level, source.isProvidingStrongPower(reads, x, y, z, face.ordinal()));
                if (!reads.isComplete()) throw unavailable(pos);
            }
            if (Compat.isSFMLoaded()) {
                budget.chargeProviders(3);
                level = Math.max(level, SteveFMCompat.getReceiverPower(world, pos, mask));
            }
            levels.add(level);
        }
        return levels;
    }

    private static IllegalArgumentException unavailable(BlockPos pos) {
        return new IllegalArgumentException("Labelled redstone query unavailable at " + pos);
    }

    private int observe(LabelReference source, QueryBudget budget) {
        if (source == null || budget == null) throw new IllegalArgumentException("Typed query arguments are required");
        if (source.hasSlotSelection())
            throw new IllegalArgumentException("Redstone observations do not accept slot restrictions");
        World world = context.getWorld();
        if (world == null || context.getNetwork() == null) return -1;
        if (world.isRemote || MinecraftServer.getServer() != null && !ServerUiTasks.isServerThread())
            throw new IllegalArgumentException("Redstone queries require server-thread execution");
        Set<BlockPos> positions = context.getLabelPositionHolder()
            .getPositions(source.getName());
        if (positions.isEmpty()) return -1;
        budget.chargeAllocation(1);
        Runnable beforeProviderCall = budget::chargeProvider;
        int max = 0;
        try {
            for (BlockPos pos : positions) {
                budget.chargePosition();
                budget.chargeProvider();
                if (!context.getNetwork()
                    .isConnected(pos)) return -1;
                TileEntity tile = loadedTile(world, pos, budget);
                if (!(tile instanceof TileEntityRedstonePort) || tile.isInvalid()) return -1;
                TileEntityRedstonePort port = (TileEntityRedstonePort) tile;
                // Charge before each native lookup, retaining cheap cases without free provider work.
                port.refreshInputs(beforeProviderCall);
                if (loadedTile(world, pos, budget) != port || port.isInvalid()) return -1;
                budget.chargeProvider();
                if (!context.getNetwork()
                    .isConnected(pos)) return -1;
                for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
                    if ((source.getSideMask() & 1 << side.ordinal()) == 0) continue;
                    budget.chargeProvider();
                    if (!port.isInputAvailable(side.ordinal())) return -1;
                    budget.chargeProvider();
                    int level = port.getInputLevel(side.ordinal());
                    if (level < 0 || level > 15)
                        throw new IllegalArgumentException("Redstone query provider returned an invalid signal level");
                    max = Math.max(max, level);
                }
                // Validate every member even once max reaches 15; the whole group must be complete.
            }
            return max;
        } catch (IllegalArgumentException failure) {
            throw failure;
        } catch (RuntimeException failure) {
            throw new IllegalArgumentException(
                "Redstone query provider failed: " + failure.getClass()
                    .getSimpleName(),
                failure);
        }
    }

    private static TileEntity loadedTile(World world, BlockPos pos, QueryBudget budget) {
        budget.chargeProvider();
        if (!pos.isLoaded(world)) return null;
        budget.chargeProvider();
        return world.getTileEntity(pos.x, pos.y, pos.z);
    }
}
