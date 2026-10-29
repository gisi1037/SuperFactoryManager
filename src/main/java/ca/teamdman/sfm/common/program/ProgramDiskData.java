package ca.teamdman.sfm.common.program;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.program.project.ExecutionReference;

/** Pure NBT classification. Reading a disk never loads files, creates authority or rewrites metadata. */
public final class ProgramDiskData {

    public static final String NBT_REFERENCE = "sfm:reference";
    public static final int REFERENCE_FORMAT = 1;

    public enum Mode {
        LEGACY,
        REFERENCE,
        INVALID
    }

    private ProgramDiskData() {}

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
