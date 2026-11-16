package ca.teamdman.sfm.common.block;

import java.util.Locale;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.storage.StorageBackend;
import ca.teamdman.sfm.common.tileentity.TileEntityModAdapter;
import ca.teamdman.sfml.data.QueryBudget;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Place against an interface; the saved facing selects exactly one neighboring backend. */
public final class BlockModAdapter extends BlockCable {

    private IIcon portIcon;

    public BlockModAdapter() {
        setBlockName("sfm.modAdapter");
        setBlockTextureName("superfactorymanager:mod_adapter");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister registry) {
        super.registerBlockIcons(registry);
        portIcon = registry.registerIcon("superfactorymanager:mod_adapter_port");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata) {
        return side == metadata ? portIcon : blockIcon;
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TileEntityModAdapter();
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ,
        int metadata) {
        return ForgeDirection.getOrientation(side)
            .getOpposite()
            .ordinal();
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        if (!world.isRemote && placer instanceof EntityPlayer player
            && world.getTileEntity(x, y, z) instanceof TileEntityModAdapter adapter)
            adapter.setOwner(player.getGameProfile());
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (player.getHeldItem() != null) return super.onBlockActivated(world, x, y, z, player, side, hitX, hitY, hitZ);
        if (!player.isSneaking()) {
            if (!world.isRemote && world.getTileEntity(x, y, z) instanceof TileEntityModAdapter adapter)
                reportAccess(player, adapter);
            return super.onBlockActivated(world, x, y, z, player, side, hitX, hitY, hitZ);
        }
        if (!world.isRemote && world.canMineBlock(player, x, y, z) && player.canPlayerEdit(x, y, z, side, null)) {
            int next = (world.getBlockMetadata(x, y, z) + 1) % 6;
            world.setBlockMetadataWithNotify(x, y, z, next, 3);
            ca.teamdman.sfm.common.network.CableNetwork.invalidate(world, x, y, z);
            player.addChatMessage(
                new ChatComponentTranslation(
                    "sfm.adapter.facing",
                    ForgeDirection.getOrientation(next)
                        .name()
                        .toLowerCase(Locale.ROOT)));
        }
        return true;
    }

    private static void reportAccess(EntityPlayer player, TileEntityModAdapter adapter) {
        String face = adapter.targetSide()
            .name()
            .toLowerCase(Locale.ROOT);
        try {
            StorageBackend backend = adapter.open(
                new QueryBudget(),
                problem -> player.addChatMessage(
                    new ChatComponentTranslation(
                        "sfm.adapter.status",
                        face,
                        new ChatComponentTranslation("sfm.adapter.status." + problem))));
            if (backend != null) player.addChatMessage(
                new ChatComponentTranslation(
                    "sfm.adapter.status",
                    face,
                    new ChatComponentTranslation(
                        "sfm.adapter.status.ready",
                        backend.supportsItems(),
                        backend.supportsFluids())));
        } catch (RuntimeException failure) {
            SuperFactoryManager.LOG.warn("Mod Storage Adapter diagnostic failed", failure);
            player.addChatMessage(new ChatComponentTranslation("sfm.adapter.status.failed"));
        }
    }
}
