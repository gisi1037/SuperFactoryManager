package ca.teamdman.sfm.common.program;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.program.project.ExecutionReference;

/** NBT classification and client presentation provenance. Reading never loads files or creates authority. */
public final class ProgramDiskData {

    public static final String NBT_REFERENCE = "sfm:reference";
    public static final int REFERENCE_FORMAT = 1;
    /** Presentation provenance only: never serialized, copied or consulted by executable validation. */
    private static final Set<ItemStack> CLIENT_PREVIEWS = Collections
        .synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    public enum Mode {
        LEGACY,
        REFERENCE,
        INVALID
    }

    private ProgramDiskData() {}

    public static void markClientPreview(ItemStack disk) {
        if (redactedShape(disk)) CLIENT_PREVIEWS.add(disk);
    }

    private static boolean redactedShape(ItemStack disk) {
        NBTTagCompound root = disk == null ? null : disk.getTagCompound();
        return root != null && !root.hasKey(ItemDisk.NBT_PROGRAM)
            && root.hasKey(NBT_REFERENCE, 10)
            && root.getCompoundTag(NBT_REFERENCE)
                .hasNoTags();
    }

    public static boolean isClientPreview(ItemStack disk) {
        return CLIENT_PREVIEWS.contains(disk) && redactedShape(disk);
    }

    public static Mode mode(ItemStack disk) {
        NBTTagCompound root = disk == null ? null : disk.getTagCompound();
        if (root == null) return Mode.LEGACY;
        if (!root.hasKey(NBT_REFERENCE)) {
            return root.hasKey(ItemDisk.NBT_PROGRAM) && !root.hasKey(ItemDisk.NBT_PROGRAM, 8) ? Mode.INVALID
                : Mode.LEGACY;
        }
        // Even an empty source tag is ambiguous alongside a reference. Never guess or migrate it.
        if (root.hasKey(ItemDisk.NBT_PROGRAM) || !root.hasKey(NBT_REFERENCE, 10)) return Mode.INVALID;
        NBTTagCompound tag = root.getCompoundTag(NBT_REFERENCE);
        if (tag.func_150296_c()
            .size() != 3 || !tag.hasKey("format", 3)
            || tag.getInteger("format") != REFERENCE_FORMAT
            || !tag.hasKey("grant", 8)
            || !tag.hasKey("digest", 8)
            || !isHex(tag.getString("grant"))
            || !isHex(tag.getString("digest"))) return Mode.INVALID;
        return Mode.REFERENCE;
    }

    public static void requireLegacy(ItemStack disk) {
        if (mode(disk) != Mode.LEGACY)
            throw new IllegalStateException("Published or invalid reference disks cannot be edited as source disks");
    }

    public static ExecutionReference readReference(ItemStack disk) {
        if (mode(disk) != Mode.REFERENCE) throw new IllegalStateException("Invalid program disk reference");
        NBTTagCompound tag = disk.getTagCompound()
            .getCompoundTag(NBT_REFERENCE);
        return new ExecutionReference(tag.getString("grant"), tag.getString("digest"));
    }

    private static boolean isHex(String value) {
        if (value.length() != 64) return false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!(c >= '0' && c <= '9' || c >= 'a' && c <= 'f')) return false;
        }
        return true;
    }
}
