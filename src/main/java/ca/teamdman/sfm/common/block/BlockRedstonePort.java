package ca.teamdman.sfm.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.tileentity.TileEntityRedstonePort;
import ca.teamdman.sfm.common.util.LoadedRedstoneAccess;

/** Combined detector/emitter: six absolute faces, no continuous tile tick or separate configuration GUI. */
public final class BlockRedstonePort extends BlockContainer {

    public BlockRedstonePort() {
        super(Material.iron);
        setBlockName("sfm.redstonePort");
        setBlockTextureName("superfactorymanager:redstone_port");
        setHardness(2.5F);
        setResistance(15);
        setStepSound(soundTypeMetal);
        setCreativeTab(CreativeTabs.tabRedstone);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityRedstonePort();
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
        return true;
    }

    @Override
    public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        // Use our explicit output, never recursively sample power through another solid port.
        return false;
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int receiverSide) {
        TileEntityRedstonePort port = getPort(world, x, y, z);
        return port == null || receiverSide < 0 || receiverSide >= 6 ? 0
            : port.getOutputLevel(
                ForgeDirection.getOrientation(receiverSide)
                    .getOpposite()
                    .ordinal());
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int receiverSide) {
        return isProvidingWeakPower(world, x, y, z, receiverSide);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        super.onBlockAdded(world, x, y, z);
        CableNetwork.invalidate(world, x, y, z);
        if (!world.isRemote) {
            refresh(world, x, y, z);
            // BlockContainer creation may follow onBlockAdded. This is a one-shot baseline, not polling.
            world.scheduleBlockUpdate(x, y, z, this, 1);
        }
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (world.isRemote) return;
        TileEntityRedstonePort port = getPort(world, x, y, z);
        if (port != null) port.onScheduledInitialization();
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        refresh(world, x, y, z);
    }

    @Override
    public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ) {
        if (world instanceof World) refresh((World) world, x, y, z);
    }

    @Override
    public boolean getWeakChanges(IBlockAccess world, int x, int y, int z) {
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block broken, int metadata) {
        TileEntityRedstonePort port = getPort(world, x, y, z);
        int changedMask = port == null ? 0 : port.clearOutputsForRemoval();
        super.breakBlock(world, x, y, z, broken, metadata);
        CableNetwork.invalidate(world, x, y, z);
        if (!world.isRemote) notifyOutputNeighbors(world, x, y, z, this, changedMask);
    }

    private static void refresh(World world, int x, int y, int z) {
        if (world.isRemote) return;
        TileEntityRedstonePort port = getPort(world, x, y, z);
        if (port != null) port.refreshInputs();
    }

    private static TileEntityRedstonePort getPort(IBlockAccess world, int x, int y, int z) {
        if (world == null || world instanceof World && !((World) world).blockExists(x, y, z)) return null;
        TileEntity tile = world.getTileEntity(x, y, z);
        return tile instanceof TileEntityRedstonePort ? (TileEntityRedstonePort) tile : null;
    }

    /** Guard both direct updates and conduction updates around the solid block receiving strong power. */
    public static void notifyOutputNeighbors(World world, int x, int y, int z, Block source, int changedMask) {
        if (world.isRemote) return;
        if (changedMask == 0) return;
        LoadedRedstoneAccess access = new LoadedRedstoneAccess();
        access.attach(world, null);
        try {
            for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
                if ((changedMask & 1 << direction.ordinal()) == 0) continue;
                int neighborX = x + direction.offsetX;
                int neighborY = y + direction.offsetY;
                int neighborZ = z + direction.offsetZ;
                if (!world.blockExists(neighborX, neighborY, neighborZ)) continue;
                Block neighbor = world.getBlock(neighborX, neighborY, neighborZ);
                boolean conducts = true;
                access.reset();
                try {
                    conducts = neighbor
                        .shouldCheckWeakPower(access, neighborX, neighborY, neighborZ, direction.ordinal())
                        || !access.isComplete();
                } catch (RuntimeException invalidProvider) {
                    // Unknown conduction needs conservative loaded-only updates, never an unsafe World fallback.
                }
                world.notifyBlockOfNeighborChange(neighborX, neighborY, neighborZ, source);
                if (conducts) for (ForgeDirection around : ForgeDirection.VALID_DIRECTIONS) {
                    int targetX = neighborX + around.offsetX;
                    int targetY = neighborY + around.offsetY;
                    int targetZ = neighborZ + around.offsetZ;
                    if (world.blockExists(targetX, targetY, targetZ))
                        world.notifyBlockOfNeighborChange(targetX, targetY, targetZ, source);
                }
            }
        } finally {
            access.detach();
        }
    }
}
