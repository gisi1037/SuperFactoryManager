package ca.teamdman.sfm.common.util;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

/** Read-only power-provider view: even reads made inside vanilla dust callbacks never request an unloaded block. */
public final class LoadedRedstoneAccess implements IBlockAccess {

    private static final int MAX_NESTED_READS = 128;
    private World world;
    private Runnable beforeProviderCall;
    private boolean complete;
    private int reads;

    public void attach(World world, Runnable beforeProviderCall) {
        this.world = world;
        this.beforeProviderCall = beforeProviderCall;
        reset();
    }

    public void reset() {
        complete = true;
        reads = 0;
    }

    public boolean isComplete() {
        return complete;
    }

    public void detach() {
        world = null;
        beforeProviderCall = null;
    }

    private boolean loaded(int x, int y, int z) {
        if (++reads > MAX_NESTED_READS) throw new IllegalArgumentException("Redstone provider read limit exceeded");
        before();
        if (world != null && world.blockExists(x, y, z)) return true;
        complete = false;
        return false;
    }

    private void before() {
        if (beforeProviderCall != null) beforeProviderCall.run();
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        if (!loaded(x, y, z)) return Blocks.air;
        before();
        return world.getBlock(x, y, z);
    }

    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        if (!loaded(x, y, z)) return null;
        before();
        return world.getTileEntity(x, y, z);
    }

    @Override
    public int getBlockMetadata(int x, int y, int z) {
        if (!loaded(x, y, z)) return 0;
        before();
        return world.getBlockMetadata(x, y, z);
    }

    @Override
    public int isBlockProvidingPowerTo(int x, int y, int z, int direction) {
        Block block = getBlock(x, y, z);
        before();
        return block.isProvidingStrongPower(this, x, y, z, direction);
    }

    @Override
    public boolean isAirBlock(int x, int y, int z) {
        Block block = getBlock(x, y, z);
        before();
        return block.isAir(this, x, y, z);
    }

    @Override
    public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean defaultValue) {
        if (!loaded(x, y, z)) return defaultValue;
        before();
        Block block = world.getBlock(x, y, z);
        before();
        return block.isSideSolid(this, x, y, z, side);
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(int x, int z) {
        if (!loaded(x, 0, z)) return BiomeGenBase.plains;
        before();
        return world.getBiomeGenForCoords(x, z);
    }

    @Override
    public int getHeight() {
        if (++reads > MAX_NESTED_READS) throw new IllegalArgumentException("Redstone provider read limit exceeded");
        before();
        return world.getHeight();
    }

    @Override
    public int getLightBrightnessForSkyBlocks(int x, int y, int z, int minimum) {
        throw new IllegalArgumentException("Client lighting is unavailable to server redstone providers");
    }

    @Override
    public boolean extendedLevelsInChunkCache() {
        return false;
    }
}
