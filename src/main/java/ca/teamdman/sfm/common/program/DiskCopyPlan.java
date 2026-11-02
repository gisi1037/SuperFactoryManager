package ca.teamdman.sfm.common.program;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.item.ModItems;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.recovery.FluidRecovery;
import ca.teamdman.sfm.common.recovery.ItemRecovery;
import cpw.mods.fml.relauncher.ReflectionHelper;

/** Cold, bounded copy preparation. No compilation, project access, new grants or filesystem reads. */
public final class DiskCopyPlan {

    private static final Field LIST = ReflectionHelper.findField(NBTTagList.class, "tagList", "field_74747_a");
    private final ItemStack source, medium, candidate;
    private final NBTTagCompound sourceTags, mediumTags;
    private boolean completed;

    private DiskCopyPlan(ItemStack source, ItemStack medium) {
        requireDisk(source);
        if (!isBlank(medium)) throw new IllegalArgumentException("Insert one fresh blank SFM disk");
        validateMetadata(source);
        NBTTagCompound tags = source.getTagCompound();
        if (tags == null) throw new IllegalArgumentException("Source disk has no program");
        if (tags.hasKey(ItemRecovery.KEY) || tags.hasKey(FluidRecovery.KEY))
            throw new IllegalArgumentException("Resolve source disk recovery before copying");
        ProgramDiskData.Mode mode = ProgramDiskData.mode(source);
        if (mode == ProgramDiskData.Mode.INVALID) throw new IllegalArgumentException("Invalid source program disk");
        ProgramDocument.read(source); // Reject unsupported identity metadata without changing the source.
        validateLabels(tags);
        candidate = new ItemStack(ModItems.disk);
        ProgramDocument.ensure(candidate); // Fresh editor identity, STATE and execution clock.
        if (mode == ProgramDiskData.Mode.LEGACY) {
            String code = ItemDisk.getProgramString(source);
            if (code.trim()
                .isEmpty()) throw new IllegalArgumentException("Source disk has no program");
            ProgramDocument.validateSource(code);
            candidate.getTagCompound()
                .setString(ItemDisk.NBT_PROGRAM, code);
        } else {
            // Retain the same immutable revision and execution-only bearer authority, never authoring access.
            candidate.getTagCompound()
                .setTag(
                    ProgramDiskData.NBT_REFERENCE,
                    tags.getTag(ProgramDiskData.NBT_REFERENCE)
                        .copy());
        }
        if (tags.hasKey(LabelPositionHolder.NBT_KEY)) candidate.getTagCompound()
            .setTag(
                LabelPositionHolder.NBT_KEY,
                tags.getTag(LabelPositionHolder.NBT_KEY)
                    .copy());
        ProgramStateStore.validateCompleteDisk(candidate);
        this.source = source;
        this.medium = medium;
        sourceTags = (NBTTagCompound) tags.copy();
        mediumTags = medium.getTagCompound() == null ? null
            : (NBTTagCompound) medium.getTagCompound()
                .copy();
    }

    public static DiskCopyPlan prepare(ItemStack source, ItemStack medium) {
        return new DiskCopyPlan(source, medium);
    }

    public static boolean isDisk(ItemStack stack) {
        return stack != null && ModItems.disk != null
            && stack.getItem() == ModItems.disk
            && stack.stackSize == 1
            && stack.getItemDamage() == 0;
    }

    public static boolean isBlank(ItemStack stack) {
        return isDisk(stack) && (stack.getTagCompound() == null || stack.getTagCompound()
            .hasNoTags());
    }

    private static void requireDisk(ItemStack disk) {
        if (!isDisk(disk)) throw new IllegalArgumentException("Insert one SFM source disk");
    }

    /** Exact object binding is cheap during work; metadata is revalidated once at completion. */
    public boolean owns(ItemStack currentSource, ItemStack currentMedium) {
        return !completed && currentSource == source
            && currentMedium == medium
            && isDisk(currentSource)
            && isBlank(currentMedium);
    }

    public ItemStack complete(ItemStack currentSource, ItemStack currentMedium) {
        if (!owns(currentSource, currentMedium)) throw new IllegalArgumentException("Disk changed; copy cancelled");
        validateMetadata(currentSource);
        if (!sourceTags.equals(currentSource.getTagCompound())
            || !java.util.Objects.equals(mediumTags, currentMedium.getTagCompound()))
            throw new IllegalArgumentException("Disk metadata changed; copy cancelled");
        completed = true;
        return candidate;
    }

    private static void validateLabels(NBTTagCompound root) {
        if (!root.hasKey(LabelPositionHolder.NBT_KEY)) return;
        if (!root.hasKey(LabelPositionHolder.NBT_KEY, 10)) throw new IllegalArgumentException("Invalid source labels");
        NBTTagCompound labels = root.getCompoundTag(LabelPositionHolder.NBT_KEY);
        if (labels.func_150296_c()
            .size() > LabelPositionHolder.MAX_SNAPSHOT_LABELS)
            throw new IllegalArgumentException("Too many source labels");
        int positions = 0;
        for (String name : labels.func_150296_c()) {
            if (!labels.hasKey(name, 9)) throw new IllegalArgumentException("Invalid source label positions");
            NBTTagList list = labels.getTagList(name, 11);
            NBTTagList original = (NBTTagList) labels.getTag(name);
            if (original.func_150303_d() != 0 && original.func_150303_d() != 11
                || original.tagCount() != list.tagCount())
                throw new IllegalArgumentException("Invalid source label position type");
            positions += list.tagCount();
            if (positions > LabelPositionHolder.MAX_SNAPSHOT_POSITIONS)
                throw new IllegalArgumentException("Too many source label positions");
            for (int i = 0; i < list.tagCount(); i++) if (list.func_150306_c(i).length != 3)
                throw new IllegalArgumentException("Invalid source label coordinates");
        }
    }

    /** Bound recursion before vanilla's recursive serializer/copy/equals touches any input metadata. */
    private static void validateMetadata(ItemStack disk) {
        NBTTagCompound tags = disk.getTagCompound();
        if (tags == null) return;
        try {
            visit(tags, 0, new int[1]);
        } catch (IllegalAccessException error) {
            throw new IllegalStateException("Cannot inspect source disk metadata", error);
        }
        ProgramStateStore.validateCompleteDisk(disk);
    }

    private static void visit(NBTBase tag, int depth, int[] count) throws IllegalAccessException {
        if (tag == null || depth > 32 || ++count[0] > 4096)
            throw new IllegalArgumentException("Source disk metadata exceeds depth/tag limits");
        switch (tag.getId()) {
            case 10:
                NBTTagCompound compound = (NBTTagCompound) tag;
                if (compound.func_150296_c()
                    .size() > 4096) throw new IllegalArgumentException("Too many source disk tags");
                for (String key : compound.func_150296_c()) {
                    if (key.length() > ProgramStateStore.MAX_DISK_NBT_BYTES)
                        throw new IllegalArgumentException("Source disk tag name exceeds limits");
                    visit(compound.getTag(key), depth + 1, count);
                }
                break;
            case 9:
                List<?> values = (List<?>) LIST.get(tag);
                if (values.size() > 4096) throw new IllegalArgumentException("Source disk list exceeds limits");
                for (Object value : values) {
                    if (!(value instanceof NBTBase)) throw new IllegalArgumentException("Invalid source disk list");
                    visit((NBTBase) value, depth + 1, count);
                }
                break;
            case 7:
                if (((NBTTagByteArray) tag).func_150292_c().length > ProgramStateStore.MAX_DISK_NBT_BYTES)
                    throw new IllegalArgumentException("Source disk byte array exceeds limits");
                break;
            case 11:
                if (((NBTTagIntArray) tag).func_150302_c().length > ProgramStateStore.MAX_DISK_NBT_BYTES / 4)
                    throw new IllegalArgumentException("Source disk integer array exceeds limits");
                break;
            case 8:
                if (((NBTTagString) tag).func_150285_a_()
                    .length() > ProgramStateStore.MAX_DISK_NBT_BYTES)
                    throw new IllegalArgumentException("Source disk string exceeds limits");
                break;
            default:
                if (tag.getId() < 1 || tag.getId() > 8)
                    throw new IllegalArgumentException("Unsupported source disk metadata");
        }
    }
}
