package ca.teamdman.sfm.common.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

public final class ItemBlockModAdapter extends ItemBlock {

    public ItemBlockModAdapter(Block block) {
        super(block);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        lines.add(StatCollector.translateToLocal("sfm.adapter.help"));
        lines.add(StatCollector.translateToLocal("sfm.adapter.rotate"));
        lines.add(StatCollector.translateToLocal("sfm.adapter.owner"));
    }
}
