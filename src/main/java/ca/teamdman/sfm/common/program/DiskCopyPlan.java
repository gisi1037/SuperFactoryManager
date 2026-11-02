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
    public static final int MAX_BATCH_SIZE = 8;
    private final ItemStack source;
    private final ItemStack[] media, candidates;
    private final NBTTagCompound sourceTags;
    private final NBTTagCompound[] mediumTags;
    private boolean completed;

    private DiskCopyPlan(ItemStack source, ItemStack[] media) {
        requireDisk(source);
        if (media == null || media.length < 1 || media.length > MAX_BATCH_SIZE)
            throw new IllegalArgumentException("A disk copy batch requires 1-8 blank disks");
        this.media = media.clone();
        mediumTags = new NBTTagCompound[media.length];
        for (int i = 0; i < media.length; i++) {
            if (!isBlank(media[i])) throw new IllegalArgumentException("Insert fresh blank SFM disks");
            for (int j = 0; j < i; j++) if (media[i] == media[j])
                throw new IllegalArgumentException("Blank disk occupies multiple input slots");
            mediumTags[i] = media[i].getTagCompound() == null ? null
                : (NBTTagCompound) media[i].getTagCompound()
                    .copy();
        }
        validateMetadata(source);
        NBTTagCompound tags = source.getTagCompound();
        if (tags == null) throw new IllegalArgumentException("Source disk has no program");
        // Parse one immutable snapshot for the whole batch, rather than copying/revalidating STATE eight times.
        sourceTags = (NBTTagCompound) tags.copy();
        ItemStack snapshot = new ItemStack(ModItems.disk);
        snapshot.setTagCompound(sourceTags);
        tags = sourceTags;
        if (tags.hasKey(ItemRecovery.KEY) || tags.hasKey(FluidRecovery.KEY))
            throw new IllegalArgumentException("Resolve source disk recovery before copying");
        ProgramDiskData.Mode mode = ProgramDiskData.mode(snapshot);
        if (mode == ProgramDiskData.Mode.INVALID) throw new IllegalArgumentException("Invalid source program disk");
        ProgramDocument.read(snapshot); // Reject unsupported identity metadata without changing the source.
        validateLabels(tags);
        if (mode == ProgramDiskData.Mode.LEGACY) {
            String code = ItemDisk.getProgramString(snapshot);
            if (code.trim()
                .isEmpty()) throw new IllegalArgumentException("Source disk has no program");
            ProgramDocument.validateSource(code);
        }
        candidates = new ItemStack[media.length];
        for (int i = 0; i < candidates.length; i++) candidates[i] = copy(snapshot, mode);
        this.source = source;
    }

    private static ItemStack copy(ItemStack snapshot, ProgramDiskData.Mode mode) {
        NBTTagCompound tags = snapshot.getTagCompound();
        ItemStack candidate = new ItemStack(ModItems.disk);
        ProgramDocument.ensure(candidate); // Fresh editor identity, STATE and execution clock.
        if (mode == ProgramDiskData.Mode.LEGACY) {
            candidate.getTagCompound()
                .setString(ItemDisk.NBT_PROGRAM, ItemDisk.getProgramString(snapshot));
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
        return candidate;
    }

    public static DiskCopyPlan prepare(ItemStack source, ItemStack medium) {
        return new DiskCopyPlan(source, new ItemStack[] { medium });
    }

    public static DiskCopyPlan prepareBatch(ItemStack source, ItemStack[] media) {
        return new DiskCopyPlan(source, media);
    }

    public int size() {
        return media.length;
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
        return media.length == 1 && ownsSource(currentSource) && ownsMedium(0, currentMedium);
    }

    public boolean ownsSource(ItemStack current) {
        return !completed && current == source && isDisk(current);
    }

    public boolean ownsMedium(int index, ItemStack current) {
        return !completed && index >= 0 && index < media.length && current == media[index] && isBlank(current);
    }

    public ItemStack complete(ItemStack currentSource, ItemStack currentMedium) {
        if (media.length != 1) throw new IllegalArgumentException("Use batch completion for multiple media");
        return completeBatch(currentSource, new ItemStack[] { currentMedium })[0];
    }

    public ItemStack[] completeBatch(ItemStack currentSource, ItemStack[] currentMedia) {
        if (!ownsSource(currentSource) || currentMedia == null || currentMedia.length != media.length)
            throw new IllegalArgumentException("Disk changed; copy cancelled");
        for (int i = 0; i < media.length; i++) if (!ownsMedium(i, currentMedia[i]))
            throw new IllegalArgumentException("Blank media changed; copy cancelled");
        validateMetadata(currentSource);
        if (!sourceTags.equals(currentSource.getTagCompound()))
            throw new IllegalArgumentException("Disk metadata changed; copy cancelled");
        for (int i = 0; i < media.length; i++)
            if (!java.util.Objects.equals(mediumTags[i], currentMedia[i].getTagCompound()))
                throw new IllegalArgumentException("Blank metadata changed; copy cancelled");
        completed = true;
        return candidates;
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
