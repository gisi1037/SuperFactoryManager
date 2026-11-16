package ca.teamdman.sfm.common.tileentity;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import ca.teamdman.sfm.common.block.BlockWirelessConnector;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.network.CableNetwork;
import ca.teamdman.sfm.common.util.BlockPos;

/** Coordinates and identities only. Wireless links never tick or keep chunks loaded. */
public final class TileEntityWirelessConnector extends TileEntity {

    private UUID identity, peerIdentity;
    private BlockPos peer;

    @Override
    public boolean canUpdate() {
        return false;
    }

    public UUID getIdentity() {
        return identity;
    }

    public UUID identify() {
        requireServer();
        if (identity == null) {
            identity = UUID.randomUUID();
            markDirty();
        }
        return identity;
    }

    /** Observe even an offline peer in discovery footprints so chunk reloads invalidate cached views. */
    public BlockPos getPeerPosition() {
        return peer;
    }

    public BlockPos getConnectedPeerPosition() {
        if (worldObj == null || worldObj.isRemote || isInvalid() || identity == null || peerIdentity == null)
            return null;
        TileEntityWirelessConnector other = at(worldObj, peer);
        return other != null && other != this
            && peerIdentity.equals(other.identity)
            && identity.equals(other.peerIdentity)
            && position().equals(other.peer) ? peer : null;
    }

    public boolean linkTo(TileEntityWirelessConnector other) {
        requireServer();
        if (other == null || other == this
            || other.worldObj != worldObj
            || at(worldObj, position()) != this
            || at(worldObj, other.position()) != other) return false;
        UUID first = identify(), second = other.identify();
        unlink();
        other.unlink();
        peer = other.position();
        peerIdentity = second;
        other.peer = position();
        other.peerIdentity = first;
        changed();
        other.changed();
        return true;
    }

    public void unlink() {
        requireServer();
        TileEntityWirelessConnector other = at(worldObj, peer);
        boolean reciprocal = other != null && other != this
            && identity != null
            && identity.equals(other.peerIdentity)
            && peerIdentity != null
            && peerIdentity.equals(other.identity)
            && position().equals(other.peer);
        if (peer != null || peerIdentity != null) {
            peer = null;
            peerIdentity = null;
            changed();
        }
        if (reciprocal) {
            other.peer = null;
            other.peerIdentity = null;
            other.changed();
        }
    }

    public static TileEntityWirelessConnector at(World world, BlockPos position) {
        if (world == null || position == null
            || !position.isLoaded(world)
            || !(position.getBlock(world) instanceof BlockWirelessConnector)) return null;
        TileEntity tile = position.getTileEntity(world);
        return tile instanceof TileEntityWirelessConnector connector && !connector.isInvalid() ? connector : null;
    }

    public BlockPos position() {
        return new BlockPos(xCoord, yCoord, zCoord);
    }

    private void changed() {
        markDirty();
        CableNetwork.invalidate(worldObj, xCoord, yCoord, zCoord);
    }

    private void requireServer() {
        if (worldObj == null || worldObj.isRemote || !ServerUiTasks.isServerThread())
            throw new IllegalStateException("Wireless links require the server thread");
    }

    @Override
    public void validate() {
        super.validate();
        CableNetwork.invalidate(worldObj, xCoord, yCoord, zCoord);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        CableNetwork.invalidate(worldObj, xCoord, yCoord, zCoord);
    }

    @Override
    public void onChunkUnload() {
        CableNetwork.invalidate(worldObj, xCoord, yCoord, zCoord);
        super.onChunkUnload();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagCompound link = new NBTTagCompound();
        if (identity != null) link.setString("id", identity.toString());
        if (peer != null && peerIdentity != null) {
            link.setString("peerId", peerIdentity.toString());
            link.setInteger("x", peer.x);
            link.setInteger("y", peer.y);
            link.setInteger("z", peer.z);
        }
        tag.setTag("sfm:wireless", link);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        NBTTagCompound link = tag.getCompoundTag("sfm:wireless");
        identity = uuid(link.getString("id"));
        peerIdentity = uuid(link.getString("peerId"));
        peer = identity != null && peerIdentity != null
            && link.hasKey("x", 3)
            && link.hasKey("y", 3)
            && link.hasKey("z", 3)
            && link.getInteger("y") >= 0
            && link.getInteger("y") < 256
            && link.getInteger("x") >= -30000000
            && link.getInteger("x") < 30000000
            && link.getInteger("z") >= -30000000
            && link.getInteger("z") < 30000000
                ? new BlockPos(link.getInteger("x"), link.getInteger("y"), link.getInteger("z"))
                : null;
        if (peer == null) peerIdentity = null;
    }

    private static UUID uuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException invalid) {
            return null;
        }
    }
}
