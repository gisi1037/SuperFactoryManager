package ca.teamdman.sfm.common.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

import ca.teamdman.sfm.common.tileentity.TileEntityWirelessConnector;
import ca.teamdman.sfm.common.util.BlockPos;

/** Right-click two endpoints with one tool. Selecting an endpoint never changes an existing pair. */
public final class ItemWirelessLinker extends Item {

    private static final String SELECTION = "sfm:wirelessSelection";

    public ItemWirelessLinker() {
        setMaxStackSize(1);
        setUnlocalizedName("sfm.wirelessLinker");
        setTextureName("superfactorymanager:wireless_linker");
        setCreativeTab(CreativeTabs.tabTools);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        TileEntityWirelessConnector target = TileEntityWirelessConnector.at(world, new BlockPos(x, y, z));
        if (target == null) return false;
        if (world.isRemote) return true;
        if (!editable(player, world, target.position(), side, stack)) return true;
        if (player.isSneaking()) {
            target.unlink();
            clear(stack);
            message(player, "unlinked");
            return true;
        }
        NBTTagCompound selected = stack.hasTagCompound() ? stack.getTagCompound()
            .getCompoundTag(SELECTION) : new NBTTagCompound();
        if (!selected.hasKey("id", 8)) {
            select(stack, world, target);
            message(player, "selected", x, y, z);
            return true;
        }
        if (selected.getInteger("dimension") != world.provider.dimensionId) {
            message(player, "dimension");
            return true;
        }
        BlockPos firstPosition = new BlockPos(
            selected.getInteger("x"),
            selected.getInteger("y"),
            selected.getInteger("z"));
        TileEntityWirelessConnector first = TileEntityWirelessConnector.at(world, firstPosition);
        if (first == null) {
            message(player, "unavailable");
            return true;
        }
        if (first.getIdentity() == null || !first.getIdentity()
            .toString()
            .equals(selected.getString("id"))) {
            clear(stack);
            message(player, "replaced");
            return true;
        }
        if (first == target) {
            message(player, "same");
            return true;
        }
        if (!editable(player, world, firstPosition, side, stack)) return true;
        if (first.linkTo(target)) {
            clear(stack);
            message(player, "linked");
        }
        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote && player.isSneaking()) {
            clear(stack);
            message(player, "cleared");
        }
        return stack;
    }

    private static boolean editable(EntityPlayer player, World world, BlockPos pos, int side, ItemStack stack) {
        return world.canMineBlock(player, pos.x, pos.y, pos.z)
            && player.canPlayerEdit(pos.x, pos.y, pos.z, side, stack);
    }

    private static void select(ItemStack stack, World world, TileEntityWirelessConnector connector) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString(
            "id",
            connector.identify()
                .toString());
        tag.setInteger("dimension", world.provider.dimensionId);
        tag.setInteger("x", connector.xCoord);
        tag.setInteger("y", connector.yCoord);
        tag.setInteger("z", connector.zCoord);
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound()
            .setTag(SELECTION, tag);
    }

    private static void clear(ItemStack stack) {
        if (stack.hasTagCompound()) stack.getTagCompound()
            .removeTag(SELECTION);
    }

    private static void message(EntityPlayer player, String key, Object... values) {
        player.addChatMessage(new ChatComponentTranslation("sfm.wireless." + key, values));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        lines.add(net.minecraft.util.StatCollector.translateToLocal("sfm.wireless.help"));
        lines.add(net.minecraft.util.StatCollector.translateToLocal("sfm.wireless.unlinkHelp"));
        lines.add(net.minecraft.util.StatCollector.translateToLocal("sfm.wireless.clearHelp"));
        if (stack.hasTagCompound() && stack.getTagCompound()
            .hasKey(SELECTION, 10)) {
            NBTTagCompound tag = stack.getTagCompound()
                .getCompoundTag(SELECTION);
            lines.add(
                net.minecraft.util.StatCollector.translateToLocalFormatted(
                    "sfm.wireless.selection",
                    tag.getInteger("dimension"),
                    tag.getInteger("x"),
                    tag.getInteger("y"),
                    tag.getInteger("z")));
        }
    }
}
