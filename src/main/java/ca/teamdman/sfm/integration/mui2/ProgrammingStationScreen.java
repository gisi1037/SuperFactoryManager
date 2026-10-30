package ca.teamdman.sfm.integration.mui2;

import java.util.UUID;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.client.gui.mui2.Mui2ProgrammingStation;
import ca.teamdman.sfm.client.gui.mui2.StudioScaledPanel;
import ca.teamdman.sfm.common.net.PacketStationDebugReply;
import ca.teamdman.sfm.common.net.PacketStationReply;
import ca.teamdman.sfm.common.tileentity.TileEntityProgrammingStation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** One station container/session remains open beneath the authoring modal. */
@SideOnly(Side.CLIENT)
public final class ProgrammingStationScreen extends ModularScreen {

    private final IPanelHandler projects;
    private Mui2ProgrammingStation editor;
    private boolean opened;

    public ProgrammingStationScreen(TileEntityProgrammingStation station, UUID sessionId, ModularPanel mainPanel) {
        super(SuperFactoryManager.MODID, mainPanel);
        projects = IPanelHandler.simple(mainPanel, (parent, player) -> {
            editor = new Mui2ProgrammingStation(station, sessionId, getContainer().windowId, this::closeProjects);
            return editor.build();
        }, true);
    }

    public void openDebugTerminal() {
        if (editor != null) editor.openDebugDock();
    }

    @Override
    public void onResize(int width, int height) {
        StudioScaledPanel.updateResolution();
        super.onResize(width, height);
    }

    public void receiveStationDebugReply(PacketStationDebugReply reply) {
        if (editor != null && projects.isPanelOpen()) editor.receiveStationDebugReply(reply);
    }

    public void openProjects() {
        if (projects.isPanelOpen()) return;
        projects.deleteCachedPanel();
        projects.openPanel();
    }

    private void closeProjects() {
        if (editor != null) editor.cancel();
        editor = null;
        projects.closePanel();
        projects.deleteCachedPanel();
        close();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!opened) {
            opened = true;
            openProjects();
        }
        if (editor != null && !projects.isPanelOpen()) closeProjects();
        if (editor != null) editor.tick();
    }

    public void receiveStationReply(PacketStationReply reply) {
        if (editor != null && projects.isPanelOpen()) editor.receive(reply);
    }

    @Override
    public void onClose() {
        if (editor != null) editor.cancel();
        super.onClose();
    }
}
