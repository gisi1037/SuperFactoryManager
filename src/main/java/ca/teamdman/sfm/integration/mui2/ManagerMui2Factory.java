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
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Synchronized manager factory. Registration and activation are owned by common initialization. */
public final class ManagerMui2Factory implements UIFactory<ManagerMui2Factory.ManagerGuiData> {

    public static final ManagerMui2Factory INSTANCE = new ManagerMui2Factory();
    public static final String DENIED_PANEL = "sfm_manager_denied";

    /** Server approval travels with the open reply, so both sides bind the same panel. */
    public static final class ManagerGuiData extends PosGuiData {

        private boolean approved;

        public ManagerGuiData(EntityPlayer player, int x, int y, int z) {
            super(player, x, y, z);
        }

        public boolean isApproved() {
            return approved;
        }
    }

    private ManagerMui2Factory() {}

    @Override
    public String getFactoryName() {
        return ManagerMui2Panel.PANEL_NAME;
    }

    public void open(EntityPlayer player, TileEntityManager manager) {
        if (!(player instanceof EntityPlayerMP) || manager == null || !manager.isUseableByPlayer(player)) return;
        GuiManager.open(
            this,
            new ManagerGuiData(player, manager.xCoord, manager.yCoord, manager.zCoord),
            (EntityPlayerMP) player);
    }

    @Override
    public ModularPanel createPanel(ManagerGuiData data, PanelSyncManager sync, UISettings settings) {
        // This screen opens from the block interaction. A remote framework-open
        // packet must not resolve tiles or bind inventories on a Netty thread.
        boolean server = data.getPlayer() instanceof EntityPlayerMP;
        if (server && !ServerUiTasks.isServerThread() || !server && !data.approved) return denied(data, settings);
        TileEntityManager manager = findManager(data);
        // MUI2 also accepts open requests from clients: validate before binding any slots.
        if (manager == null || !manager.isUseableByPlayer(data.getPlayer())) return denied(data, settings);
        if (server) data.approved = true;
        settings.canInteractWith(player -> player == data.getPlayer() && manager.isUseableByPlayer(player));
        settings.customContainer(() -> new ManagerMui2Container(manager));
        return ManagerMui2Panel.build(manager, data.getPlayer(), sync);
    }

    private static ModularPanel denied(ManagerGuiData data, UISettings settings) {
        data.approved = false;
        // MUI2 has no cancellation return value. This panel binds no manager slots;
        // its container closes on the next validity check without a network error.
        settings.canInteractWith(player -> false);
        return new ModularPanel(DENIED_PANEL).size(248, 64);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModularScreen createScreen(ManagerGuiData data, ModularPanel mainPanel) {
        if (!data.approved || DENIED_PANEL.equals(mainPanel.getName()))
            return new ModularScreen(SuperFactoryManager.MODID, mainPanel);
        TileEntityManager manager = findManager(data);
        // A client chunk may unload between the reply and screen creation. The
        // server's validity check closes this screen; do not disconnect the player.
        if (manager == null) return new ModularScreen(SuperFactoryManager.MODID, mainPanel);
        return new ManagerMui2Screen(manager, mainPanel);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player, ManagerGuiData data) {
        if (!data.approved || player != data.getPlayer()
            || player instanceof EntityPlayerMP && !ServerUiTasks.isServerThread()) return false;
        TileEntityManager manager = findManager(data);
        return manager != null && manager.isUseableByPlayer(player);
    }

    private static TileEntityManager findManager(PosGuiData data) {
        // Never turn a remote UI-open request into a chunk load.
        if (!data.getWorld()
            .blockExists(data.getX(), data.getY(), data.getZ())) return null;
        TileEntity tile = data.getTileEntity();
        return tile instanceof TileEntityManager ? (TileEntityManager) tile : null;
    }

    @Override
    public void writeGuiData(ManagerGuiData data, PacketBuffer buffer) {
        buffer.writeVarIntToBuffer(data.getX());
        buffer.writeVarIntToBuffer(data.getY());
        buffer.writeVarIntToBuffer(data.getZ());
        buffer.writeBoolean(data.approved);
    }

    @Override
    public ManagerGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        ManagerGuiData data = new ManagerGuiData(
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
