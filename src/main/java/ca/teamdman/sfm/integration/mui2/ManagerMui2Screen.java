package ca.teamdman.sfm.integration.mui2;

import java.awt.Desktop;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.TextWidget;

import ca.teamdman.sfm.SuperFactoryManager;
import ca.teamdman.sfm.client.gui.ProgramEditSession;
import ca.teamdman.sfm.client.gui.ProgramEditState;
import ca.teamdman.sfm.client.gui.mui2.Mui2BlueprintEditor;
import ca.teamdman.sfm.client.gui.mui2.Mui2CodeEditor;
import ca.teamdman.sfm.client.gui.mui2.Mui2DebugTerminal;
import ca.teamdman.sfm.client.gui.mui2.Mui2LogsPanel;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.net.PacketDebugTerminalReply;
import ca.teamdman.sfm.common.net.PacketManagerAction;
import ca.teamdman.sfm.common.net.PacketManagerLogs;
import ca.teamdman.sfm.common.net.PacketProgramEditReply;
import ca.teamdman.sfm.common.net.SFMNetwork;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfm.common.util.BlockPos;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Authoring pages share one live MUI2 container and its server-issued save authority. */
@SideOnly(Side.CLIENT)
public final class ManagerMui2Screen extends ModularScreen implements ProgramEditSession.Receiver {

    // Client-session recovery only: no source is written to a second disk or a controller here.
    private static final Map<World, LinkedHashMap<BlockPos, Drafts>> DRAFTS = new WeakHashMap<>();

    private static final class Drafts {

        String text;
        Mui2BlueprintEditor.Draft visual;
    }

    private final TileEntityManager manager;
    private final Map<String, IPanelHandler> pages = new LinkedHashMap<>();
    private Mui2CodeEditor text;
    private Mui2BlueprintEditor visual;
    private Mui2LogsPanel logs;
    private Mui2DebugTerminal terminal;
    private IPanelHandler active;
    private ProgramEditSession pasteSession;
    private String pasteDraft;
    private boolean resetState;
    private ca.teamdman.sfm.common.gui.ManagerControlToken controlToken;

    public ManagerMui2Screen(TileEntityManager manager, ModularPanel mainPanel) {
        super(SuperFactoryManager.MODID, mainPanel);
        this.manager = manager;
        pages.put("text", IPanelHandler.simple(mainPanel, (parent, player) -> {
            String recovered = pasteDraft != null ? pasteDraft : takeDraft();
            pasteDraft = null;
            if (recovered != null) message("Recovered draft; verify it before saving to the installed disk.");
            text = new Mui2CodeEditor(manager, windowId(), recovered, this::closePage);
            return text.build();
        }, true));
        pages.put("visual", IPanelHandler.simple(mainPanel, (parent, player) -> {
            visual = new Mui2BlueprintEditor(manager, windowId(), this::closePage, () -> {
                closePage();
                openPage("text");
            }, takeVisualDraft());
            return visual.build();
        }, true));
        pages.put("logs", IPanelHandler.simple(mainPanel, (parent, player) -> {
            logs = new Mui2LogsPanel(manager, controlToken);
            return logs.build(this::closePage);
        }, true));
        pages.put("terminal", IPanelHandler.simple(mainPanel, (parent, player) -> {
            terminal = new Mui2DebugTerminal(manager, windowId());
            return terminal.build(this::closePage);
        }, true));
        pages.put("confirm", IPanelHandler.simple(mainPanel, (parent, player) -> confirmation(), true));
    }

    private int windowId() {
        return Minecraft.getMinecraft().thePlayer.openContainer.windowId;
    }

    public void openPage(String page) {
        openPage(page, controlToken);
    }

    public void openPage(String page, ca.teamdman.sfm.common.gui.ManagerControlToken token) {
        if (active != null && active.isPanelOpen() || pasteSession != null && pasteSession.state.isPending()) return;
        if (("text".equals(page) || "visual".equals(page) || "paste".equals(page)) && !hasRecoveryCapacity()) {
            message(
                "Local recovery holds 8 manager drafts. Reopen and save a retained draft before editing another manager.");
            return;
        }
        controlToken = token;
        IPanelHandler handler = pages.get(page);
        if (handler != null) {
            active = handler;
            handler.openPanel();
            return;
        }
        switch (page) {
            case "paste":
                String clipboard = GuiScreen.getClipboardString();
                if (clipboard == null || clipboard.isEmpty()) return;
                try {
                    ca.teamdman.sfm.common.program.ProgramDocument.validateSource(clipboard);
                } catch (IllegalArgumentException error) {
                    message(error.getMessage() + ". Clipboard is unchanged; paste a smaller program.");
                    return;
                }
                pasteDraft = clipboard;
                pasteSession = new ProgramEditSession(manager, windowId());
                pasteSession.open();
                break;
            case "copy":
                GuiScreen.setClipboardString(ItemDisk.getProgramString(manager.getDisk()));
                message("Program copied to clipboard.");
                break;
            case "example":
                GuiScreen.setClipboardString(
                    "NAME \"Transfer Iron\"\n\nEVERY 20 TICKS DO\n  INPUT FROM chest_in\n  OUTPUT TO chest_out\nEND\n");
                message("Example program copied to clipboard.");
                break;
            case "run":
                action(PacketManagerAction.ACTION_TOGGLE_RUNNING);
                break;
            case "rebuild":
                action(PacketManagerAction.ACTION_REBUILD);
                break;
            case "reset_state":
            case "reset":
                resetState = page.equals("reset_state");
                active = pages.get("confirm");
                active.openPanel();
                break;
            case "diagnostics":
                GuiScreen.setClipboardString("SFM Diagnostics: " + manager.getStatusMessage());
                message(manager.getStatusMessage());
                break;
            case "config":
                message("Server limits: 500 log entries; cable discovery has no configured size limit.");
                break;
            case "discord":
                try {
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop()
                        .isSupported(Desktop.Action.BROWSE))
                        Desktop.getDesktop()
                            .browse(new URI("https://discord.gg/xjXYj9MmS4"));
                } catch (Exception error) {
                    message("Could not open Discord: " + error.getMessage());
                }
                break;
            default:
                break;
        }
    }

    private ModularPanel confirmation() {
        ModularPanel panel = new ModularPanel("sfm_reset_confirmation") {

            @Override
            public boolean disablePanelsBelow() {
                return true;
            }
        }.size(300, 104);
        panel.child(
            new TextWidget<>(resetState ? "Reset persistent variables?" : "Reset disk program and labels?").pos(10, 12)
                .size(280, 14));
        panel.child(
            new TextWidget<>(
                resetState ? "Program, labels and recovery records are kept."
                    : "This clears the installed program and labels.").pos(10, 32)
                        .size(280, 24));
        panel.child(
            new ButtonWidget<>().pos(10, 70)
                .size(134, 20)
                .overlay(IKey.str("Reset"))
                .onMousePressed(button -> {
                    if (button != 0) return false;
                    action(resetState ? PacketManagerAction.ACTION_RESET_STATE : PacketManagerAction.ACTION_RESET);
                    closePage();
                    return true;
                }));
        panel.child(
            new ButtonWidget<>().pos(156, 70)
                .size(134, 20)
                .overlay(IKey.str("Cancel"))
                .onMousePressed(button -> {
                    if (button != 0) return false;
                    closePage();
                    return true;
                }));
        return panel;
    }

    private void action(int action) {
        SFMNetwork.WRAPPER.sendToServer(
            new PacketManagerAction(manager.xCoord, manager.yCoord, manager.zCoord, action, controlToken));
    }

    private void closePage() {
        if (text != null && !text.canClose() || visual != null && !visual.canClose()) return;
        retainDraft();
        if (text != null) text.cancel();
        if (visual != null) visual.cancel();
        text = null;
        visual = null;
        logs = null;
        if (terminal != null) terminal.cancel();
        terminal = null;
        if (active != null) {
            active.closePanel();
            active.deleteCachedPanel();
            active = null;
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (active != null && !active.isPanelOpen()) closePage();
        if (text != null) text.tick();
        if (visual != null) visual.tick();
        if (pasteSession != null && pasteSession.state.tick())
            message("No save confirmation; save may have applied. Draft kept: choose Text to recover it.");
    }

    @Override
    public boolean onKeyPressed(char character, int key) {
        if (key == Keyboard.KEY_ESCAPE && !canClose()) return true;
        return super.onKeyPressed(character, key);
    }

    private boolean canClose() {
        return (text == null || text.canClose()) && (visual == null || visual.canClose())
            && (pasteSession == null || !pasteSession.state.isPending());
    }

    @Override
    public void close(boolean force) {
        if (force || canClose()) super.close(force);
    }

    @Override
    public void onClose() {
        retainDraft();
        if (text != null) text.cancel();
        if (visual != null) visual.cancel();
        if (terminal != null) terminal.cancel();
        if (pasteSession != null) pasteSession.state.cancel();
        super.onClose();
    }

    @Override
    public void receiveProgramEditReply(PacketProgramEditReply reply) {
        if (text != null) text.receiveProgramEditReply(reply);
        if (visual != null) visual.receiveProgramEditReply(reply);
        if (pasteSession == null) return;
        ProgramEditState.Action action = pasteSession.state.action();
        if (!pasteSession.receive(reply)) return;
        if (reply.accepted && action == ProgramEditState.Action.OPEN) {
            try {
                pasteSession.save(pasteDraft);
            } catch (IllegalArgumentException | IllegalStateException error) {
                pasteSession.state.cancel();
                message(error.getMessage() + ". Draft kept: choose Text to recover it.");
            }
        } else {
            message(reply.accepted ? "Program saved." : reply.message + ". Draft kept: choose Text to recover it.");
            if (reply.accepted) pasteDraft = null;
        }
    }

    public void receiveLogs(PacketManagerLogs reply) {
        if (logs != null) logs.receive(reply.getX(), reply.getY(), reply.getZ(), reply.getLogLevel(), reply.getLogs());
    }

    public void receiveDebugTerminal(PacketDebugTerminalReply reply) {
        if (terminal != null && active == pages.get("terminal") && active.isPanelOpen()) terminal.receive(reply);
    }

    private void retainDraft() {
        boolean visualDraft = visual != null && visual.hasDraft();
        String source = pasteDraft;
        if (text != null && text.hasDraft()) source = text.draft();
        if (source == null && !visualDraft) return;
        LinkedHashMap<BlockPos, Drafts> worldDrafts = DRAFTS
            .computeIfAbsent(manager.getWorldObj(), world -> new LinkedHashMap<>());
        Drafts retained = worldDrafts.computeIfAbsent(position(), position -> new Drafts());
        if (source != null) retained.text = source;
        if (visualDraft) retained.visual = visual.exportDraft();
        // Admission reserves capacity before editing. Forced closure must never evict another unsaved draft.
    }

    private boolean hasRecoveryCapacity() {
        Map<BlockPos, Drafts> drafts = DRAFTS.get(manager.getWorldObj());
        return drafts == null || drafts.size() < 8 || drafts.containsKey(position());
    }

    private Drafts retainedDrafts() {
        Map<BlockPos, Drafts> drafts = DRAFTS.get(manager.getWorldObj());
        return drafts == null ? null : drafts.get(position());
    }

    private String takeDraft() {
        Drafts drafts = retainedDrafts();
        if (drafts == null) return null;
        String source = drafts.text;
        drafts.text = null;
        discardEmptyDrafts(drafts);
        return source;
    }

    private Mui2BlueprintEditor.Draft takeVisualDraft() {
        Drafts drafts = retainedDrafts();
        if (drafts == null) return null;
        Mui2BlueprintEditor.Draft visual = drafts.visual;
        drafts.visual = null;
        discardEmptyDrafts(drafts);
        return visual;
    }

    private void discardEmptyDrafts(Drafts drafts) {
        if (drafts.text != null || drafts.visual != null) return;
        Map<BlockPos, Drafts> worldDrafts = DRAFTS.get(manager.getWorldObj());
        if (worldDrafts == null) return;
        worldDrafts.remove(position());
        if (worldDrafts.isEmpty()) DRAFTS.remove(manager.getWorldObj());
    }

    private BlockPos position() {
        return new BlockPos(manager.xCoord, manager.yCoord, manager.zCoord);
    }

    private static void message(String text) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("[SFM] " + text));
    }
}
