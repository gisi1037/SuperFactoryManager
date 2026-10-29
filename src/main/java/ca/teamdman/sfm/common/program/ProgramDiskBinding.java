package ca.teamdman.sfm.common.program;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import ca.teamdman.sfm.common.item.ItemDisk;

/** Exact local executable identity; no source reads from storage and no per-tick decoding or allocation. */
public final class ProgramDiskBinding {

    private final ItemStack disk;
    private final String source;
    private final NBTTagCompound reference;
    private final NBTBase format, grant, digest;
    private final NBTTagCompound document;
    private final NBTBase documentFormat, idMost, idLeast, documentRevision;

    private ProgramDiskBinding(ItemStack disk) {
        if (disk == null) throw new IllegalArgumentException("Disk is required");
        this.disk = disk;
        ProgramDiskData.Mode mode = ProgramDiskData.mode(disk);
        if (mode == ProgramDiskData.Mode.INVALID) throw new IllegalStateException("Invalid program disk format");
        source = mode == ProgramDiskData.Mode.LEGACY ? ItemDisk.getProgramString(disk) : null;
        reference = source != null ? null
            : disk.getTagCompound()
                .getCompoundTag(ProgramDiskData.NBT_REFERENCE);
        format = reference == null ? null : reference.getTag("format");
        grant = reference == null ? null : reference.getTag("grant");
        digest = reference == null ? null : reference.getTag("digest");
        // Legacy document revisions may change for layout-only edits; they are not executable identity.
        ProgramDocument present = reference == null ? null : ProgramDocument.read(disk);
        document = present == null ? null
            : disk.getTagCompound()
                .getCompoundTag(ProgramDocument.NBT_KEY);
        documentFormat = document == null ? null : document.getTag("format");
        idMost = document == null ? null : document.getTag("idMost");
        idLeast = document == null ? null : document.getTag("idLeast");
        documentRevision = document == null ? null : document.getTag("revision");
    }

    public static ProgramDiskBinding capture(ItemStack disk) {
        return new ProgramDiskBinding(disk);
    }

    public boolean isCurrent(ItemStack candidate) {
        if (candidate != disk) return false;
        NBTTagCompound root = disk.getTagCompound();
        if (reference == null) {
            if (root == null) return source.isEmpty();
            if (root.hasKey(ProgramDiskData.NBT_REFERENCE)) return false;
            return !root.hasKey(ItemDisk.NBT_PROGRAM) ? source.isEmpty()
                : root.hasKey(ItemDisk.NBT_PROGRAM, 8) && source.equals(root.getString(ItemDisk.NBT_PROGRAM));
        }
        if (root == null || root.hasKey(ItemDisk.NBT_PROGRAM)
            || root.getTag(ProgramDiskData.NBT_REFERENCE) != reference
            || reference.func_150296_c()
                .size() != 3
            || reference.getTag("format") != format
            || reference.getTag("grant") != grant
            || reference.getTag("digest") != digest) return false;
        // Normal NBT setters replace primitive tags; compare those identities without decoding every tick.
        if (document == null) return !root.hasKey(ProgramDocument.NBT_KEY);
        return root.getTag(ProgramDocument.NBT_KEY) == document && document.func_150296_c()
            .size() == 4
            && document.getTag("format") == documentFormat
            && document.getTag("idMost") == idMost
            && document.getTag("idLeast") == idLeast
            && document.getTag("revision") == documentRevision;
    }
}
