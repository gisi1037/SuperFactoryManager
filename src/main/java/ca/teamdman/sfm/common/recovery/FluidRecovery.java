package ca.teamdman.sfm.common.recovery;

import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

/** Operator acknowledgements only. An uncertain amount never becomes owned fluid. */
public final class FluidRecovery {

    public static final String KEY = "sfm:recovery_fluid";
    public static final String LAST_KEY = "sfm:last_fluid_recovery";
    private static final String[] SOURCE = { "sourceX", "sourceY", "sourceZ", "sourceSide", "sourceDimension" };
    private static final String[] TARGET = { "targetX", "targetY", "targetZ", "targetSide", "targetDimension" };

    private FluidRecovery() {}

    public static void requireServerThread() {
        if (!ServerUiTasks.isServerThread())
            throw new IllegalStateException("Fluid recovery requires the server thread");
    }

    private static NBTTagCompound diskTags(TileEntityManager manager) {
        requireServerThread();
        World world = manager.getWorldObj();
        if (world == null || world.isRemote
            || manager.isInvalid()
            || manager.getBlockPos()
                .getTileEntity(world) != manager)
            throw new IllegalStateException("Manager is not loaded on this server");
        // Pause even unsupported or corrupt evidence, without rewriting the record.
        if (manager.isRunning()) manager.setRunning(false);
        ItemStack disk = manager.getDisk();
        if (disk == null || !(disk.getItem() instanceof ItemDisk)
            || !disk.hasTagCompound()
            || !disk.getTagCompound()
                .hasKey(KEY))
            throw new IllegalStateException("No fluid recovery record");
        return disk.getTagCompound();
    }

    /** Read-only evidence snapshot. No legacy IDs, versions or ownership states are invented. */
    public static NBTTagCompound inspect(TileEntityManager manager) {
        NBTTagCompound tags = diskTags(manager);
        if (!tags.hasKey(KEY, 10)) throw new IllegalStateException("Corrupt fluid recovery record; retained");
        return (NBTTagCompound) tags.getCompoundTag(KEY)
            .copy();
    }

    private static void requireInt(NBTTagCompound record, String key) {
        if (!record.hasKey(key, 3)) throw new IllegalStateException("Invalid fluid recovery " + key + "; retained");
    }

    private static void requirePosition(NBTTagCompound record, String[] keys) {
        for (String key : keys) requireInt(record, key);
        int side = record.getInteger(keys[3]);
        if (side < 0 || side > 5) throw new IllegalStateException("Invalid fluid recovery face; retained");
    }

    private static NBTTagCompound resolvable(NBTTagCompound tags, String id) {
        if (!tags.hasKey(KEY, 10)) throw new IllegalStateException("Corrupt fluid recovery record; retained");
        NBTTagCompound record = tags.getCompoundTag(KEY);
        if (!record.hasKey("version", 3) || record.getInteger("version") != 1)
            throw new IllegalStateException("Unsupported fluid recovery version; retained");
        if (!record.hasKey("state", 8) || !"uncertain".equals(record.getString("state")))
            throw new IllegalStateException(
                "Only uncertain fluid can be acknowledged; pending/unknown records retained");
        String current = record.getString("recoveryId");
        try {
            if (!record.hasKey("recoveryId", 8) || !UUID.fromString(current)
                .toString()
                .equals(current)) throw new IllegalArgumentException();
        } catch (IllegalArgumentException error) {
            throw new IllegalStateException("Invalid fluid recovery ID; retained");
        }
        if (!current.equals(id)) throw new IllegalStateException("Recovery ID changed; inspect the current disk first");
        if (tags.hasKey(ItemRecovery.KEY)) throw new IllegalStateException("Conflicting item/fluid records; retained");
        requireInt(record, "Amount");
        if (record.getInteger("Amount") <= 0 || !record.hasKey("FluidName", 8)
            || record.getString("FluidName")
                .isEmpty()
            || record.getString("FluidName")
                .length() > 256
            || !record.hasKey("stage", 8)
            || record.getString("stage")
                .length() > 256
            || (record.hasKey("Tag") && !record.hasKey("Tag", 10)))
            throw new IllegalStateException("Invalid fluid evidence; retained");
        if (record.hasKey("recoveryBlocked") && !record.hasKey("recoveryBlocked", 1))
            throw new IllegalStateException("Invalid fluid recovery blocked flag; retained");
        requirePosition(record, SOURCE);
        boolean target = false;
        for (String key : TARGET) target |= record.hasKey(key);
        if (target) requirePosition(record, TARGET);
        return record;
    }

    /** Resolves evidence after manual reconciliation; never queries or mutates either tank. */
    public static void resolvePresent(TileEntityManager manager, String id, String actor) {
        NBTTagCompound tags = diskTags(manager);
        NBTTagCompound record = resolvable(tags, id);
        NBTTagCompound receipt = new NBTTagCompound();
        receipt.setInteger("version", 1);
        receipt.setString("recoveryId", record.getString("recoveryId"));
        receipt.setString("state", "resolved-present");
        receipt.setString("FluidName", record.getString("FluidName"));
        receipt.setInteger("Amount", record.getInteger("Amount"));
        receipt.setString("stage", record.getString("stage"));
        for (String key : SOURCE) receipt.setInteger(key, record.getInteger(key));
        if (record.hasKey("targetX")) {
            for (String key : TARGET) receipt.setInteger(key, record.getInteger(key));
        }
        receipt.setString("actor", actor == null ? "" : actor.substring(0, Math.min(actor.length(), 64)));
        receipt.setLong(
            "resolvedAt",
            manager.getWorldObj()
                .getTotalWorldTime());
        // Fixed fields, bounded strings, one inactive receipt: no arbitrary payload/history growth.
        tags.setTag(LAST_KEY, receipt);
        tags.removeTag(KEY);
        manager.fluidRecoveryResolved();
    }
}
