package ca.teamdman.sfm.common.label;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import ca.teamdman.sfm.common.util.BlockPos;

public final class LabelPositionHolder {

    public static final String NBT_KEY = "sfm:labels";

    private final Map<String, Set<BlockPos>> labels = new LinkedHashMap<>();

    public Set<BlockPos> getPositions(String label) {
        if (label == null) return Collections.emptySet();
        Set<BlockPos> set = labels.get(label);
        return set != null ? Collections.unmodifiableSet(set) : Collections.emptySet();
    }

    public Set<String> getLabelsForPosition(BlockPos pos) {
        if (pos == null) return Collections.emptySet();
        Set<String> result = new LinkedHashSet<>();
        for (Map.Entry<String, Set<BlockPos>> entry : labels.entrySet()) {
            if (entry.getValue()
                .contains(pos)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public void addPosition(String label, BlockPos pos) {
        if (label == null || pos == null) return;
        labels.computeIfAbsent(label, k -> new LinkedHashSet<>())
            .add(pos);
    }

    public void removePosition(String label, BlockPos pos) {
        if (label == null || pos == null) return;
        Set<BlockPos> set = labels.get(label);
        if (set != null) {
            set.remove(pos);
            if (set.isEmpty()) {
                labels.remove(label);
            }
        }
    }

    public boolean togglePosition(String label, BlockPos pos) {
        if (label == null || pos == null) return false;
        Set<BlockPos> set = labels.get(label);
        if (set != null && set.contains(pos)) {
            removePosition(label, pos);
            return false;
        } else {
            addPosition(label, pos);
            return true;
        }
    }

    public void addReferencedLabel(String label) {
        if (label == null || label.trim()
            .isEmpty()) return;
        labels.computeIfAbsent(label.trim(), k -> new LinkedHashSet<>());
    }

    public void prune() {
        labels.entrySet()
            .removeIf(
                entry -> entry.getValue()
                    .isEmpty());
    }

    public Map<String, Set<BlockPos>> getAllLabels() {
        return Collections.unmodifiableMap(labels);
    }

    public void clear() {
        labels.clear();
    }

    public void readFromNBT(NBTTagCompound tag) {
        readFromNBT(tag, null);
    }

    /** Decode one execution snapshot; null selects all labels for tools and editors. */
    public void readFromNBT(NBTTagCompound tag, Set<String> selectedLabels) {
        labels.clear();
        if (tag == null || !tag.hasKey(NBT_KEY)) return;
        NBTTagCompound labelCompound = tag.getCompoundTag(NBT_KEY);
        if (selectedLabels == null) {
            for (Object key : labelCompound.func_150296_c()) readLabel(labelCompound, (String) key);
        } else {
            for (String label : selectedLabels) {
                if (labelCompound.hasKey(label)) readLabel(labelCompound, label);
            }
        }
    }

    private void readLabel(NBTTagCompound labelCompound, String label) {
        NBTTagList list = labelCompound.getTagList(label, 11); // 11 = NBTTagIntArray
        Set<BlockPos> positions = new LinkedHashSet<>();
        for (int i = 0; i < list.tagCount(); i++) {
            int[] coords = list.func_150306_c(i);
            if (coords.length >= 3) positions.add(new BlockPos(coords[0], coords[1], coords[2]));
        }
        labels.put(label, positions);
    }

    public void writeToNBT(NBTTagCompound tag) {
        if (tag == null) return;
        NBTTagCompound labelCompound = new NBTTagCompound();
        for (Map.Entry<String, Set<BlockPos>> entry : labels.entrySet()) {
            NBTTagList list = new NBTTagList();
            for (BlockPos pos : entry.getValue()) {
                list.appendTag(new net.minecraft.nbt.NBTTagIntArray(new int[] { pos.x, pos.y, pos.z }));
            }
            labelCompound.setTag(entry.getKey(), list);
        }
        tag.setTag(NBT_KEY, labelCompound);
    }

    public static LabelPositionHolder from(ItemStack stack) {
        return from(stack, null);
    }

    public static LabelPositionHolder from(ItemStack stack, Set<String> selectedLabels) {
        LabelPositionHolder holder = new LabelPositionHolder();
        if (stack != null && stack.hasTagCompound()) holder.readFromNBT(stack.getTagCompound(), selectedLabels);
        return holder;
    }

    public static void saveTo(ItemStack stack, LabelPositionHolder holder) {
        if (stack == null || holder == null) return;
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        holder.writeToNBT(stack.getTagCompound());
    }
}
