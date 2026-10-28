package ca.teamdman.sfm.common.network;

import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public final class NetworkEvents {

    @SubscribeEvent
    public void onLoad(ChunkEvent.Load event) {
        CableNetwork.invalidate(event.world);
    }

    @SubscribeEvent
    public void onUnload(ChunkEvent.Unload event) {
        CableNetwork.invalidate(event.world);
    }

    @SubscribeEvent
    public void onPlace(BlockEvent.PlaceEvent event) {
        CableNetwork.invalidate(event.world);
    }

    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent event) {
        CableNetwork.invalidate(event.world);
    }
}
