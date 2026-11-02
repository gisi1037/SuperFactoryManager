package ca.teamdman.sfm.integration.mui2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

import com.cleanroommc.modularui.api.UIFactory;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.tileentity.TileEntityDiskCopier;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Synchronized copier factory. Registration and activation are owned by common initialization. */
public final class DiskCopierMui2Factory implements UIFactory<DiskCopierMui2Factory.Data> {

    public static final DiskCopierMui2Factory INSTANCE = new DiskCopierMui2Factory();
    public static final String DENIED_PANEL = "sfm_copier_denied";

    /** Server approval travels with the open reply, so both sides bind the same panel. */
    public static final class Data extends PosGuiData {

        private boolean approved;

        public Data(EntityPlayer player, int x, int y, int z) {
            super(player, x, y, z);
        }

        public boolean isApproved() {
            return approved;
        }
    }

    private DiskCopierMui2Factory() {}

    @Override
    public String getFactoryName() {
        return DiskCopierMui2Panel.NAME;
    }

    public void open(EntityPlayer player, TileEntityDiskCopier copier) {
        if (!(player instanceof EntityPlayerMP) || copier == null || !copier.isUseableByPlayer(player)) return;
        GuiManager.open(this, new Data(player, copier.xCoord, copier.yCoord, copier.zCoord), (EntityPlayerMP) player);
    }

    @Override
    public ModularPanel createPanel(Data data, PanelSyncManager sync, UISettings settings) {
        // This screen opens from the block interaction. A remote framework-open
        // packet must not resolve tiles or bind inventories on a Netty thread.
        boolean server = data.getPlayer() instanceof EntityPlayerMP;
        if (server && !ServerUiTasks.isServerThread() || !server && !data.approved) return denied(data, settings);
        TileEntityDiskCopier copier = findCopier(data);
        // MUI2 also accepts open requests from clients: validate before binding any slots.
        if (copier == null || !copier.isUseableByPlayer(data.getPlayer())) return denied(data, settings);
        if (server) data.approved = true;
        settings.canInteractWith(player -> player == data.getPlayer() && copier.isUseableByPlayer(player));
        settings.customContainer(() -> new DiskCopierMui2Container(copier));
        return DiskCopierMui2Panel.build(copier, data.getPlayer(), sync);
    }

    private static ModularPanel denied(Data data, UISettings settings) {
        data.approved = false;
        // MUI2 has no cancellation return value. This panel binds no copier slots;
        // its container closes on the next validity check without a network error.
        settings.canInteractWith(player -> false);
        return new ModularPanel(DENIED_PANEL).size(248, 64);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModularScreen createScreen(Data data, ModularPanel mainPanel) {
        return new ModularScreen(SuperFactoryManager.MODID, mainPanel);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player, Data data) {
        if (!data.approved || player != data.getPlayer()
            || player instanceof EntityPlayerMP && !ServerUiTasks.isServerThread()) return false;
        TileEntityDiskCopier copier = findCopier(data);
        return copier != null && copier.isUseableByPlayer(player);
    }

    private static TileEntityDiskCopier findCopier(PosGuiData data) {
        // Never turn a remote UI-open request into a chunk load.
        if (!data.getWorld()
            .blockExists(data.getX(), data.getY(), data.getZ())) return null;
        TileEntity tile = data.getTileEntity();
        return tile instanceof TileEntityDiskCopier ? (TileEntityDiskCopier) tile : null;
    }

    @Override
    public void writeGuiData(Data data, PacketBuffer buffer) {
        buffer.writeVarIntToBuffer(data.getX());
        buffer.writeVarIntToBuffer(data.getY());
        buffer.writeVarIntToBuffer(data.getZ());
        buffer.writeBoolean(data.approved);
    }

    @Override
    public Data readGuiData(EntityPlayer player, PacketBuffer buffer) {
        Data data = new Data(
            player,
            buffer.readVarIntFromBuffer(),
            buffer.readVarIntFromBuffer(),
            buffer.readVarIntFromBuffer());
        boolean approved = buffer.readBoolean();
        // A client cannot grant itself access by setting the reply-only flag.
        data.approved = !(player instanceof EntityPlayerMP) && approved;
        return data;
    }
}
