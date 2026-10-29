package ca.teamdman.sfm.common.program;

import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** Small lazy document identity. STATE revisions and editor revisions have distinct lifetimes. */
public final class ProgramDocument {

    public static final String NBT_KEY = "sfm:document";
    public static final int MAX_SOURCE_BYTES = 30000;
    public final UUID id;
    public final long revision;

    private ProgramDocument(UUID id, long revision) {
        this.id = id;
        this.revision = revision;
    }

    public static void validateSource(String source) {
        ca.teamdman.sfm.common.util.BoundedUtf8.encode(source, MAX_SOURCE_BYTES);
    }

    public static ProgramDocument read(ItemStack disk) {
        if (disk == null || !disk.hasTagCompound()
            || !disk.getTagCompound()
                .hasKey(NBT_KEY))
            return null;
        NBTTagCompound root = disk.getTagCompound();
        if (!root.hasKey(NBT_KEY, 10)) throw new IllegalStateException("Invalid program document metadata");
        NBTTagCompound tag = root.getCompoundTag(NBT_KEY);
        if (tag.func_150296_c()
            .size() != 4 || !tag.hasKey("format", 3)
            || tag.getInteger("format") != 1
            || !tag.hasKey("idMost", 4)
            || !tag.hasKey("idLeast", 4)
            || !tag.hasKey("revision", 4)
            || tag.getLong("revision") < 0)
            throw new IllegalStateException("Unsupported or invalid program document metadata");
        return new ProgramDocument(new UUID(tag.getLong("idMost"), tag.getLong("idLeast")), tag.getLong("revision"));
    }

    public static ProgramDocument ensure(ItemStack disk) {
        ProgramDocument present = read(disk);
        if (present != null) return present;
        ProgramDocument created = new ProgramDocument(UUID.randomUUID(), 0);
        ItemStack candidate = disk.copy();
        created.store(candidate);
        ProgramStateStore.validateCompleteDisk(candidate);
        created.store(disk);
        return created;
    }

    public static void advance(ItemStack disk) {
        ProgramDocument current = read(disk);
        if (current != null) new ProgramDocument(current.id, Math.incrementExact(current.revision)).store(disk);
    }

    private void store(ItemStack disk) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("format", 1);
        tag.setLong("idMost", id.getMostSignificantBits());
        tag.setLong("idLeast", id.getLeastSignificantBits());
        tag.setLong("revision", revision);
        if (!disk.hasTagCompound()) disk.setTagCompound(new NBTTagCompound());
        disk.getTagCompound()
            .setTag(NBT_KEY, tag);
    }
}
