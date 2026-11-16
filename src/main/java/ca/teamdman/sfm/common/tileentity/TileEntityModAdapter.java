package ca.teamdman.sfm.common.tileentity;

import java.util.UUID;
import java.util.function.Consumer;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import ca.teamdman.sfm.common.storage.StorageBackend;
import ca.teamdman.sfml.data.QueryBudget;
import cpw.mods.fml.common.Loader;

/** Physical optional-mod access point. Backends belong to an operation, never to saved block data. */
public class TileEntityModAdapter extends TileEntity {

    private UUID owner;
    private String ownerName = "";

    @Override
    public boolean canUpdate() {
        return false;
    }

    public void setOwner(com.mojang.authlib.GameProfile profile) {
        owner = profile.getId();
        ownerName = profile.getName() == null ? "" : profile.getName();
        markDirty();
    }

    public UUID getOwner() {
        return owner;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public ForgeDirection targetSide() {
        return ForgeDirection.getOrientation(getBlockMetadata());
    }

    public StorageBackend open(QueryBudget budget) {
        return open(budget, null);
    }

    /** Reports a translation key suffix only when a player requests diagnostics. */
    public StorageBackend open(QueryBudget budget, Consumer<String> diagnostic) {
        String problem;
        if (owner == null) problem = "owner";
        else if (worldObj == null || worldObj.isRemote || !ca.teamdman.sfm.common.net.ServerUiTasks.isServerThread())
            problem = "server";
        else if (!Loader.isModLoaded("appliedenergistics2")) problem = "missingAe2";
        else return ca.teamdman.sfm.compat.ae2.Ae2StorageBackend.open(this, budget, diagnostic);
        if (diagnostic != null) diagnostic.accept(problem);
        return null;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        if (owner != null) {
            tag.setString("sfm:adapterOwner", owner.toString());
            tag.setString("sfm:adapterOwnerName", ownerName);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        owner = null;
        try {
            owner = UUID.fromString(tag.getString("sfm:adapterOwner"));
        } catch (IllegalArgumentException invalid) {
            // Ownerless or malformed adapters cannot access a secured network.
        }
        ownerName = tag.getString("sfm:adapterOwnerName");
        if (ownerName.length() > 64) ownerName = "";
    }
}
