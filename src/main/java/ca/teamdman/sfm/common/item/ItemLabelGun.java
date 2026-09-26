package ca.teamdman.sfm.common.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.gui.GuiHandler;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;

public class ItemLabelGun extends Item {

    public static final String NBT_ACTIVE_LABEL = "sfm:active_label";
    public static final String NBT_VIEW_MODE = "sfm:label_gun_view_mode";

    public enum LabelGunViewMode {

        SHOW_ALL("Show All"),
        SHOW_ONLY_ACTIVE_LABEL_AND_TARGETED_BLOCK("Active Label + Targeted"),
        SHOW_ONLY_TARGETED_BLOCK("Targeted Block Only");

        private final String displayName;

        LabelGunViewMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public ItemLabelGun() {
        setMaxStackSize(1);
        setUnlocalizedName("sfm.labelgun");
        setTextureName("superfactorymanager:labelgun");
        setCreativeTab(CreativeTabs.tabTools);
    }

    public static String getActiveLabel(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) return "";
        return stack.getTagCompound()
            .getString(NBT_ACTIVE_LABEL);
    }

    public static void setActiveLabel(ItemStack stack, String label) {
        if (stack == null) return;
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        if (label == null || label.trim()
            .isEmpty()) {
            stack.getTagCompound()
                .removeTag(NBT_ACTIVE_LABEL);
        } else {
            String trimmed = label.trim();
            stack.getTagCompound()
                .setString(NBT_ACTIVE_LABEL, trimmed);
            LabelPositionHolder holder = LabelPositionHolder.from(stack);
            holder.addReferencedLabel(trimmed);
            LabelPositionHolder.saveTo(stack, holder);
        }
    }

    public static LabelGunViewMode getViewMode(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) return LabelGunViewMode.SHOW_ALL;
        int ordinal = stack.getTagCompound()
            .getInteger(NBT_VIEW_MODE);
        if (ordinal < 0 || ordinal >= LabelGunViewMode.values().length) {
            return LabelGunViewMode.SHOW_ALL;
        }
        return LabelGunViewMode.values()[ordinal];
    }

    public static void setViewMode(ItemStack stack, LabelGunViewMode mode) {
        if (stack == null) return;
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound()
            .setInteger(NBT_VIEW_MODE, mode != null ? mode.ordinal() : 0);
    }

    public static LabelGunViewMode cycleViewMode(ItemStack stack) {
        LabelGunViewMode current = getViewMode(stack);
        int nextOrdinal = (current.ordinal() + 1) % LabelGunViewMode.values().length;
        LabelGunViewMode next = LabelGunViewMode.values()[nextOrdinal];
        setViewMode(stack, next);
        return next;
    }

    public static String getNextLabel(ItemStack stack, int direction) {
        if (stack == null) return "";
        LabelPositionHolder holder = LabelPositionHolder.from(stack);
        List<String> labels = new ArrayList<>(
            holder.getAllLabels()
                .keySet());
        if (labels.isEmpty()) return "";
        Collections.sort(labels);

        String current = getActiveLabel(stack);
        int currentIndex = labels.indexOf(current);
        if (currentIndex == -1) {
            return labels.get(0);
        }
        int nextIndex = currentIndex + direction;
        nextIndex = ((nextIndex % labels.size()) + labels.size()) % labels.size();
        return labels.get(nextIndex);
    }

    public static void clearAll(ItemStack stack) {
        if (stack == null) return;
        LabelPositionHolder holder = LabelPositionHolder.from(stack);
        holder.clear();
        LabelPositionHolder.saveTo(stack, holder);
        setActiveLabel(stack, "");
    }

    public static void prune(ItemStack stack) {
        if (stack == null) return;
        LabelPositionHolder holder = LabelPositionHolder.from(stack);
        holder.prune();
        LabelPositionHolder.saveTo(stack, holder);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String active = getActiveLabel(stack);
        if (active != null && !active.isEmpty()) {
            return super.getItemStackDisplayName(stack) + " (\"" + active + "\")";
        }
        return super.getItemStackDisplayName(stack);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        String active = getActiveLabel(stack);
        list.add(
            EnumChatFormatting.YELLOW + "Active Label: "
                + (active.isEmpty() ? EnumChatFormatting.DARK_GRAY + "(none)"
                    : EnumChatFormatting.AQUA + "\"" + active + "\""));
        list.add(
            EnumChatFormatting.GRAY + "View Mode: " + EnumChatFormatting.GREEN + getViewMode(stack).getDisplayName());
        list.add(EnumChatFormatting.DARK_AQUA + "Right-click air: " + EnumChatFormatting.GRAY + "Open Label Gun menu");
        list.add(
            EnumChatFormatting.DARK_AQUA + "Right-click block: " + EnumChatFormatting.GRAY + "Toggle active label");
        list.add(
            EnumChatFormatting.DARK_AQUA + "Shift + Right-click block: "
                + EnumChatFormatting.GRAY
                + "Pick label / clear block");
        list.add(
            EnumChatFormatting.DARK_AQUA + "Right-click Manager: " + EnumChatFormatting.GRAY + "Push labels to Disk");
        list.add(
            EnumChatFormatting.DARK_AQUA + "Shift + Right-click Manager: "
                + EnumChatFormatting.GRAY
                + "Pull labels from Disk");
        list.add(
            EnumChatFormatting.DARK_AQUA + "Shift + Mouse wheel: " + EnumChatFormatting.GRAY + "Cycle active label");

        LabelPositionHolder holder = LabelPositionHolder.from(stack);
        if (!holder.getAllLabels()
            .isEmpty()) {
            list.add(
                EnumChatFormatting.GOLD + "Registered Labels ("
                    + holder.getAllLabels()
                        .size()
                    + "):");
            for (java.util.Map.Entry<String, Set<BlockPos>> entry : holder.getAllLabels()
                .entrySet()) {
                list.add(
                    EnumChatFormatting.DARK_GRAY + " - "
                        + EnumChatFormatting.WHITE
                        + entry.getKey()
                        + ": "
                        + EnumChatFormatting.GRAY
                        + entry.getValue()
                            .size()
                        + " block(s)");
            }
        }
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }

        BlockPos targetPos = new BlockPos(x, y, z);
        TileEntity te = targetPos.getTileEntity(world);

        // If clicking on Manager
        if (te instanceof TileEntityManager) {
            TileEntityManager manager = (TileEntityManager) te;
            ItemStack disk = manager.getDisk();
            if (disk != null) {
                LabelPositionHolder gunHolder = LabelPositionHolder.from(stack);
                LabelPositionHolder diskHolder = LabelPositionHolder.from(disk);

                if (player.isSneaking()) {
                    // Pull from Disk to Gun
                    LabelPositionHolder.saveTo(stack, diskHolder);
                    player.addChatMessage(
                        new ChatComponentText(
                            EnumChatFormatting.GREEN + "[SFM] Copied "
                                + diskHolder.getAllLabels()
                                    .size()
                                + " labels from Manager's Disk to Label Gun!"));
                } else {
                    // Push from Gun to Disk
                    LabelPositionHolder.saveTo(disk, gunHolder);
                    manager.markDirty();
                    player.addChatMessage(
                        new ChatComponentText(
                            EnumChatFormatting.GREEN + "[SFM] Synced "
                                + gunHolder.getAllLabels()
                                    .size()
                                + " labels from Label Gun to Manager's Disk!"));
                }
                return true;
            } else {
                player.addChatMessage(
                    new ChatComponentText(
                        EnumChatFormatting.RED + "[SFM] Manager has no Disk inserted to sync labels with!"));
                return true;
            }
        }

        LabelPositionHolder gunHolder = LabelPositionHolder.from(stack);
        String blockName = targetPos.getBlock(world)
            .getLocalizedName();

        // If player is sneaking on a regular block: Pick label or Clear block labels
        if (player.isSneaking()) {
            Set<String> labelsOnBlock = gunHolder.getLabelsForPosition(targetPos);
            if (!labelsOnBlock.isEmpty()) {
                // If the block has labels, pick the first one as active label or if active label is already one of
                // them, clear labels
                String firstLabel = labelsOnBlock.iterator()
                    .next();
                if (!firstLabel.equals(getActiveLabel(stack))) {
                    setActiveLabel(stack, firstLabel);
                    player.addChatMessage(
                        new ChatComponentText(
                            EnumChatFormatting.AQUA + "[SFM] Picked active label: \""
                                + firstLabel
                                + "\" from "
                                + blockName));
                } else {
                    // Clear all labels from this block
                    for (String lbl : labelsOnBlock) {
                        gunHolder.removePosition(lbl, targetPos);
                    }
                    LabelPositionHolder.saveTo(stack, gunHolder);
                    player.addChatMessage(
                        new ChatComponentText(
                            EnumChatFormatting.YELLOW + "[SFM] Cleared all labels from "
                                + blockName
                                + " at "
                                + targetPos));
                }
                return true;
            } else {
                player.addChatMessage(
                    new ChatComponentText(
                        EnumChatFormatting.GRAY + "[SFM] No labels on " + blockName + " at " + targetPos));
                return true;
            }
        }

        // Toggling active label on target block
        String activeLabel = getActiveLabel(stack);
        if (activeLabel.isEmpty()) {
            player.addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.RED
                        + "[SFM] No active label selected! Right-click the air to set an active label."));
            return true;
        }

        boolean added = gunHolder.togglePosition(activeLabel, targetPos);
        LabelPositionHolder.saveTo(stack, gunHolder);

        if (added) {
            player.addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.GREEN + "[SFM] Tagged "
                        + blockName
                        + " at "
                        + targetPos
                        + " with \""
                        + activeLabel
                        + "\""));
        } else {
            player.addChatMessage(
                new ChatComponentText(
                    EnumChatFormatting.RED + "[SFM] Removed \""
                        + activeLabel
                        + "\" from "
                        + blockName
                        + " at "
                        + targetPos));
        }

        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            player.openGui(
                SuperFactoryManager.instance,
                GuiHandler.GUI_LABEL_GUN,
                world,
                (int) player.posX,
                (int) player.posY,
                (int) player.posZ);
        }
        return stack;
    }
}
