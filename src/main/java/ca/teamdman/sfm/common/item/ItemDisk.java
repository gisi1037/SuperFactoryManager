package ca.teamdman.sfm.common.item;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.program.ProgramDiskData;
import ca.teamdman.sfm.common.util.BlockPos;

public class ItemDisk extends Item {

    public static final String NBT_PROGRAM = "sfm:program";

    public ItemDisk() {
        setMaxStackSize(1);
        setUnlocalizedName("sfm.disk");
        setTextureName("superfactorymanager:disk");
        setCreativeTab(CreativeTabs.tabRedstone);
    }

    public static String getProgramString(ItemStack stack) {
        ProgramDiskData.requireLegacy(stack);
        if (stack == null || !stack.hasTagCompound()) return "";
        return stack.getTagCompound()
            .getString(NBT_PROGRAM);
    }

    public static void setProgramString(ItemStack stack, String program) {
        if (stack == null) return;
        ProgramDiskData.requireLegacy(stack);
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        String source = program != null ? program : "";
        if (!getProgramString(stack).equals(source)) ca.teamdman.sfm.common.program.ProgramDocument.advance(stack);
        stack.getTagCompound()
            .setString(NBT_PROGRAM, source);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        ProgramDiskData.Mode mode = ProgramDiskData.mode(stack);
        if (mode == ProgramDiskData.Mode.REFERENCE) {
            list.add(EnumChatFormatting.AQUA + "Published program reference");
            list.add(
                EnumChatFormatting.GRAY + "Build: " + ProgramDiskData.readReference(stack).digest.substring(0, 12));
        } else if (mode == ProgramDiskData.Mode.INVALID) {
            list.add(EnumChatFormatting.RED + "Unsupported or invalid program disk");
        } else {
            String code = getProgramString(stack);
            if (code.isEmpty()) {
                list.add(EnumChatFormatting.GRAY + "Empty Disk");
            } else {
                String[] lines = code.split("\n");
                list.add(EnumChatFormatting.GREEN + "Program: " + lines.length + " lines");
                if (lines.length > 0 && lines[0].toUpperCase()
                    .startsWith("NAME ")) {
                    list.add(EnumChatFormatting.AQUA + lines[0]);
                }
            }

        }

        LabelPositionHolder holder = LabelPositionHolder.from(stack);
        Map<String, Set<BlockPos>> labels = holder.getAllLabels();
        if (!labels.isEmpty()) {
            list.add(EnumChatFormatting.YELLOW + "Labels (" + labels.size() + "):");
            for (Map.Entry<String, Set<BlockPos>> entry : labels.entrySet()) {
                list.add(
                    EnumChatFormatting.DARK_GRAY + " - "
                        + entry.getKey()
                        + ": "
                        + entry.getValue()
                            .size()
                        + " blocks");
            }
        }
    }
}
