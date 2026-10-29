package ca.teamdman.sfm.integration.mui2;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.screen.ModularContainer;

import ca.teamdman.sfm.common.net.PacketStationReply;
import ca.teamdman.sfm.common.tileentity.TileEntityProgrammingStation;

/** One server-issued authority and bounded reply cache per open station session. */
public final class ProgrammingStationMui2Container extends ModularContainer {

    public final TileEntityProgrammingStation station;
    public final UUID sessionId;
    public long lastRequestId, pendingRequestId;
    public PacketStationReply lastReply;

    public ProgrammingStationMui2Container(TileEntityProgrammingStation station, UUID sessionId) {
        this.station = station;
        this.sessionId = sessionId;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return station.isUsableBy(player) && super.canInteractWith(player);
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        ca.teamdman.sfm.common.net.PacketStationDebugRequest.clear(this);
        super.onContainerClosed(player);
    }

    /** MUI2 registers player sync slots automatically. They are never usable in the IDE. */
    @Override
    public ItemStack slotClick(int slot, int button, int mode, EntityPlayer player) {
        return null;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
        return null;
    }

    @Override
    public boolean canDragIntoSlot(Slot slot) {
        return false;
    }
}
