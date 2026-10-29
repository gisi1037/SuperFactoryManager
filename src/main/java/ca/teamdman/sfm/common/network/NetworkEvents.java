package ca.teamdman.sfm.common.network;

import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;

import ca.teamdman.sfm.common.tileentity.TileEntityRedstonePort;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public final class NetworkEvents {

    @SubscribeEvent
    public void onLoad(ChunkEvent.Load event) {
        CableNetwork.invalidateChunk(event.world, event.getChunk().xPosition, event.getChunk().zPosition);
        if (!event.world.isRemote) {
            for (Object tile : event.getChunk().chunkTileEntityMap.values()) {
                if (tile instanceof TileEntityRedstonePort) ((TileEntityRedstonePort) tile).onLoaded();
            }
        }
    }

    @SubscribeEvent
    public void onUnload(ChunkEvent.Unload event) {
        CableNetwork.invalidateChunk(event.world, event.getChunk().xPosition, event.getChunk().zPosition);
    }

    @SubscribeEvent
    public void onPlace(BlockEvent.PlaceEvent event) {
        CableNetwork.invalidate(event.world, event.x, event.y, event.z);
    }

    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent event) {
        CableNetwork.invalidate(event.world, event.x, event.y, event.z);
    }
}
