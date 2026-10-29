package ca.teamdman.sfm.common.tileentity;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/** Only a project identifier lives in block data; source and revisions belong to world files. */
public final class TileEntityProgrammingStation extends TileEntity {

    private UUID projectId;

    @Override
    public boolean canUpdate() {
        return false;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void selectProject(UUID id) {
        if (java.util.Objects.equals(id, projectId)) return;
        projectId = id;
        markDirty();
    }

    public boolean isUsableBy(EntityPlayer player) {
        return player != null && worldObj != null
            && player.worldObj == worldObj
            && worldObj.blockExists(xCoord, yCoord, zCoord)
            && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + .5, yCoord + .5, zCoord + .5) <= 64;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        if (projectId != null) tag.setString("sfm:project", projectId.toString());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        projectId = null;
        if (tag.hasKey("sfm:project", 8)) {
            try {
                projectId = UUID.fromString(tag.getString("sfm:project"));
            } catch (IllegalArgumentException malformed) {
                // A broken identifier never becomes a path, source payload or permission grant.
            }
        }
    }
}
