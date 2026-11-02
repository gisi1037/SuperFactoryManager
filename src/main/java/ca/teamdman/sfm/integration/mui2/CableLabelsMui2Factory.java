package ca.teamdman.sfm.integration.mui2;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.UIFactory;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.block.BlockCable;
import ca.teamdman.sfm.common.net.PlayerUiAccess;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.network.CableLabelEditor;
import ca.teamdman.sfm.common.util.BlockPos;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public final class CableLabelsMui2Factory implements UIFactory<CableLabelsMui2Factory.Data> {

    public static final CableLabelsMui2Factory INSTANCE = new CableLabelsMui2Factory();
    public static final String NAME = "sfm_cable_labels", DENIED = "sfm_cable_labels_denied";

    public static final class Data extends PosGuiData {

        private UUID session;

        public Data(EntityPlayer player, int x, int y, int z) {
            super(player, x, y, z);
        }
    }

    private CableLabelsMui2Factory() {}

    public void open(EntityPlayer player, int x, int y, int z) {
        if (!(player instanceof EntityPlayerMP) || !ServerUiTasks.isServerThread()) return;
        Data data = new Data(player, x, y, z);
        if (loadedCable(data)) GuiManager.open(this, data, (EntityPlayerMP) player);
    }

    @Override
    public String getFactoryName() {
        return NAME;
    }

    @Override
    public ModularPanel createPanel(Data data, PanelSyncManager sync, UISettings settings) {
        boolean server = data.getPlayer() instanceof EntityPlayerMP;
        if (server && !ServerUiTasks.isServerThread() || !server && data.session == null || !loadedCable(data)) {
            data.session = null;
            settings.canInteractWith(player -> false);
            return new ModularPanel(DENIED).size(240, 60);
        }
        if (server) data.session = UUID.randomUUID();
        CableLabelEditor editor = server
            ? new CableLabelEditor(data.getPlayer(), new BlockPos(data.getX(), data.getY(), data.getZ()), data.session)
            : null;
        settings.getRecipeViewerSettings()
            .disable();
        settings.canInteractWith(player -> player == data.getPlayer() && loadedCable(data));
        settings.customContainer(() -> new CableLabelsMui2Container(editor));
        return CableLabelsMui2Panel.build(data.getPlayer(), editor, sync);
    }

    private static boolean loadedCable(Data data) {
        EntityPlayer player = data.getPlayer();
        return PlayerUiAccess.isLive(player) && data.getWorld()
            .blockExists(data.getX(), data.getY(), data.getZ())
            && data.getWorld()
                .getBlock(data.getX(), data.getY(), data.getZ()) instanceof BlockCable
            && player.getDistanceSq(data.getX() + .5, data.getY() + .5, data.getZ() + .5) <= 64;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player, Data data) {
        return data.session != null && player == data.getPlayer()
            && (!(player instanceof EntityPlayerMP) || ServerUiTasks.isServerThread())
            && loadedCable(data);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModularScreen createScreen(Data data, ModularPanel panel) {
        return new ModularScreen(SuperFactoryManager.MODID, panel);
    }

    @Override
    public void writeGuiData(Data data, PacketBuffer buffer) {
        buffer.writeInt(data.getX());
        buffer.writeInt(data.getY());
        buffer.writeInt(data.getZ());
        buffer.writeBoolean(data.session != null);
        if (data.session != null) {
            buffer.writeLong(data.session.getMostSignificantBits());
            buffer.writeLong(data.session.getLeastSignificantBits());
        }
    }

    @Override
    public Data readGuiData(EntityPlayer player, PacketBuffer buffer) {
        Data data = new Data(player, buffer.readInt(), buffer.readInt(), buffer.readInt());
        if (buffer.readBoolean()) {
            UUID session = new UUID(buffer.readLong(), buffer.readLong());
            if (!(player instanceof EntityPlayerMP)) data.session = session;
        }
        return data;
    }
}
