package ca.teamdman.sfm.integration.mui2;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

import com.cleanroommc.modularui.api.UIFactory;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.tileentity.TileEntityProgrammingStation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Server-approved station shell; authoring requests also validate their live session. */
public final class ProgrammingStationMui2Factory implements UIFactory<ProgrammingStationMui2Factory.Data> {

    public static final ProgrammingStationMui2Factory INSTANCE = new ProgrammingStationMui2Factory();
    public static final String PANEL_NAME = "sfm_programming_station", DENIED_PANEL = "sfm_station_denied";

    public static final class Data extends PosGuiData {

        private UUID sessionId;

        public Data(EntityPlayer player, int x, int y, int z) {
            super(player, x, y, z);
        }

        public UUID getSessionId() {
            return sessionId;
        }
    }

    private ProgrammingStationMui2Factory() {}

    public void open(EntityPlayer player, TileEntityProgrammingStation station) {
        if (player instanceof EntityPlayerMP && station.isUsableBy(player)) GuiManager
            .open(this, new Data(player, station.xCoord, station.yCoord, station.zCoord), (EntityPlayerMP) player);
    }

    @Override
    public String getFactoryName() {
        return PANEL_NAME;
    }

    @Override
    public ModularPanel createPanel(Data data, PanelSyncManager sync, UISettings settings) {
        boolean server = data.getPlayer() instanceof EntityPlayerMP;
        if (server && !ServerUiTasks.isServerThread() || !server && data.sessionId == null)
            return denied(data, settings);
        TileEntityProgrammingStation station = find(data);
        if (station == null || !station.isUsableBy(data.getPlayer())) return denied(data, settings);
        if (server) data.sessionId = UUID.randomUUID();
        settings.getRecipeViewerSettings()
            .disable();
        settings.canInteractWith(player -> player == data.getPlayer() && station.isUsableBy(player));
        settings.customContainer(() -> new ProgrammingStationMui2Container(station, data.sessionId));
        // The screen opens its full-screen authoring panel immediately. An empty
        // authority shell avoids another dialog showing through the translucent IDE.
        // MUI2's automatic player sync slots are guarded by the station container.
        return new ModularPanel(PANEL_NAME).size(1, 1)
            .background(IDrawable.EMPTY);
    }

    private static ModularPanel denied(Data data, UISettings settings) {
        data.sessionId = null;
        settings.canInteractWith(player -> false);
        return new ModularPanel(DENIED_PANEL).size(248, 64);
    }

    private static TileEntityProgrammingStation find(Data data) {
        if (!data.getWorld()
            .blockExists(data.getX(), data.getY(), data.getZ())) return null;
        TileEntity tile = data.getTileEntity();
        return tile instanceof TileEntityProgrammingStation ? (TileEntityProgrammingStation) tile : null;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player, Data data) {
        if (data.sessionId == null || player != data.getPlayer()
            || player instanceof EntityPlayerMP && !ServerUiTasks.isServerThread()) return false;
        TileEntityProgrammingStation station = find(data);
        return station != null && station.isUsableBy(player);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModularScreen createScreen(Data data, ModularPanel panel) {
        TileEntityProgrammingStation station = data.sessionId == null ? null : find(data);
        return station == null ? new ModularScreen(SuperFactoryManager.MODID, panel)
            : new ProgrammingStationScreen(station, data.sessionId, panel);
    }

    @Override
    public void writeGuiData(Data data, PacketBuffer buffer) {
        buffer.writeVarIntToBuffer(data.getX());
        buffer.writeVarIntToBuffer(data.getY());
        buffer.writeVarIntToBuffer(data.getZ());
        buffer.writeBoolean(data.sessionId != null);
        if (data.sessionId != null) {
            buffer.writeLong(data.sessionId.getMostSignificantBits());
            buffer.writeLong(data.sessionId.getLeastSignificantBits());
        }
    }

    @Override
    public Data readGuiData(EntityPlayer player, PacketBuffer buffer) {
        Data data = new Data(
            player,
            buffer.readVarIntFromBuffer(),
            buffer.readVarIntFromBuffer(),
            buffer.readVarIntFromBuffer());
        if (buffer.readBoolean()) {
            UUID sessionId = new UUID(buffer.readLong(), buffer.readLong());
            if (!(player instanceof EntityPlayerMP)) data.sessionId = sessionId;
        }
        return data;
    }
}
