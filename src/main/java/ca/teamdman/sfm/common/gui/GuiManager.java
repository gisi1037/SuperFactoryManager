package ca.teamdman.sfm.common.gui;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import ca.teamdman.sfm.Config;
import ca.teamdman.sfm.client.gui.GuiCodeEditor;
import ca.teamdman.sfm.client.gui.GuiLogs;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.net.PacketManagerAction;
import ca.teamdman.sfm.common.net.SFMNetwork;
import ca.teamdman.sfm.common.tileentity.LogEntry;
import ca.teamdman.sfm.common.tileentity.LogLevel;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

public class GuiManager extends GuiContainer
    implements net.minecraft.client.gui.GuiYesNoCallback, ca.teamdman.sfm.client.gui.ProgramEditSession.Receiver {

    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(
        "superfactorymanager:textures/gui/manager.png");

    private final TileEntityManager manager;
    private ca.teamdman.sfm.client.gui.ProgramEditSession pasteSession;
    private String pasteDraft;

    @Override
    public void receiveProgramEditReply(ca.teamdman.sfm.common.net.PacketProgramEditReply reply) {
        if (pasteSession == null) return;
        ca.teamdman.sfm.client.gui.ProgramEditState.Action action = pasteSession.state.action();
        if (!pasteSession.receive(reply)) return;
        if (reply.accepted && reply.snapshot != null
            && action == ca.teamdman.sfm.client.gui.ProgramEditState.Action.OPEN) {
            try {
                pasteSession.save(pasteDraft);
            } catch (IllegalArgumentException e) {
                pasteSession.state.cancel();
                showPasteMessage(e.getMessage() + " Draft kept: choose Text to edit or copy it.");
            }
        } else {
            showPasteMessage(
                reply.accepted ? "Program saved" : reply.message + " Draft kept: choose Text to edit or copy it.");
            if (reply.accepted) pasteDraft = null;
        }
    }

    private void showPasteMessage(String message) {
        mc.thePlayer.addChatMessage(new ChatComponentText("[SFM] " + message));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (pasteSession != null && pasteSession.state.tick()) showPasteMessage(
            "No save confirmation; the save may have applied. Draft kept: choose Text to edit or copy it.");
    }

    @Override
    public void onGuiClosed() {
        if (pasteSession != null) pasteSession.state.cancel();
        super.onGuiClosed();
    }

    // Left button stack
    private GuiButton btnPaste;
    private GuiButton btnEdit;
    private GuiButton btnExamples;
    private GuiButton btnDiscord;
    private GuiButton btnCopy;
    private GuiButton btnRun;
    private GuiButton btnLogs;
    private GuiButton btnRebuild;
    private GuiButton btnServerConfig;

    // In-GUI buttons
    private GuiButton btnReset;
    private GuiButton btnDiag;

    public GuiManager(InventoryPlayer playerInv, TileEntityManager manager) {
        super(new ContainerManager(playerInv, manager));
        this.manager = manager;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        super.initGui();
        buttonList.clear();

        int buttonWidth = 120;
        int buttonHeight = 16;
        int btnX = guiLeft - buttonWidth;
        if (btnX < 2) {
            btnX = guiLeft + xSize + 4;
        }

        // Left button stack (matching modern SFM positions)
        btnPaste = new GuiButton(1, btnX, guiTop + 16, buttonWidth, buttonHeight, "Paste from clipboard");
        btnEdit = new GuiButton(2, btnX, guiTop + 66, 39, buttonHeight, "Text");
        buttonList.add(new GuiButton(11, btnX + 40, guiTop + 66, 39, buttonHeight, "Visual"));
        buttonList.add(new GuiButton(12, btnX + 80, guiTop + 66, 40, buttonHeight, "State"));
        btnExamples = new GuiButton(3, btnX, guiTop + 82, buttonWidth, buttonHeight, "View examples");
        if (Config.showDiscordButton) {
            btnDiscord = new GuiButton(4, btnX, guiTop + 112, buttonWidth, buttonHeight, "Discord");
        } else {
            btnDiscord = null;
        }
        btnCopy = new GuiButton(5, btnX, guiTop + 128, 79, buttonHeight, "Copy code");
        btnRun = new GuiButton(13, btnX + 80, guiTop + 128, 40, buttonHeight, "Run");
        buttonList.add(btnRun);
        btnLogs = new GuiButton(6, btnX, guiTop + 144, buttonWidth, buttonHeight, "View logs");
        btnRebuild = new GuiButton(7, btnX, guiTop + 160, buttonWidth, buttonHeight, "Rebuild cable network");
        btnServerConfig = new GuiButton(8, btnX, guiTop + 176, buttonWidth, buttonHeight, "View server config");

        // In-GUI buttons
        btnReset = new ca.teamdman.sfm.client.gui.GuiButtonSFM(9, guiLeft + 120, guiTop + 10, 50, 12, "Reset");
        btnDiag = new GuiButton(10, guiLeft + 35, guiTop + 48, 12, 14, "!");

        buttonList.add(btnPaste);
        buttonList.add(btnEdit);
        buttonList.add(btnExamples);
        if (btnDiscord != null) {
            buttonList.add(btnDiscord);
        }
        buttonList.add(btnCopy);
        buttonList.add(btnLogs);
        buttonList.add(btnRebuild);
        buttonList.add(btnServerConfig);
        buttonList.add(btnReset);
        buttonList.add(btnDiag);

        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean hasDisk = manager.getDisk() != null;
        btnPaste.enabled = hasDisk && (pasteSession == null || !pasteSession.state.isPending());
        btnEdit.enabled = hasDisk && (pasteSession == null || !pasteSession.state.isPending());
        for (Object obj : buttonList)
            if (((GuiButton) obj).id == 11 || ((GuiButton) obj).id == 12) ((GuiButton) obj).enabled = hasDisk;
        btnCopy.enabled = hasDisk;
        btnRun.enabled = hasDisk && manager.hasCompiledProgram() && !manager.hasPendingRecovery();
        btnRun.displayString = manager.isRunning() ? "Stop" : "Run";
        btnLogs.enabled = hasDisk;
        btnRebuild.enabled = hasDisk;
        btnReset.enabled = hasDisk;
        btnExamples.enabled = true;
        if (btnDiscord != null) {
            btnDiscord.enabled = true;
        }
        btnServerConfig.enabled = true;
        btnDiag.visible = hasDisk && manager.getStatusMessage() != null
            && !manager.getStatusMessage()
                .isEmpty()
            && !manager.getStatusMessage()
                .equalsIgnoreCase("Running");
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            // Paste from clipboard
            String clip = getClipboardString();
            if (clip != null && !clip.isEmpty() && manager.getDisk() != null) {
                pasteDraft = clip;
                pasteSession = new ca.teamdman.sfm.client.gui.ProgramEditSession(manager, inventorySlots.windowId);
                pasteSession.open();
            }
        } else if (button.id == 2) {
            // Edit
            if (pasteSession != null && pasteSession.state.isPending()) return;
            String retainedDraft = pasteDraft;
            pasteDraft = null;
            mc.displayGuiScreen(new GuiCodeEditor(manager, this, retainedDraft));
        } else if (button.id == 13) {
            SFMNetwork.WRAPPER.sendToServer(
                new PacketManagerAction(
                    manager.xCoord,
                    manager.yCoord,
                    manager.zCoord,
                    PacketManagerAction.ACTION_TOGGLE_RUNNING));
        } else if (button.id == 12) {
            mc.displayGuiScreen(
                new net.minecraft.client.gui.GuiYesNo(
                    this,
                    "Reset persistent variables?",
                    "Program, labels and recovery records are kept.",
                    12));
        } else if (button.id == 11) {
            mc.displayGuiScreen(new ca.teamdman.sfm.client.gui.GuiBlueprintEditor(manager, this));
        } else if (button.id == 3) {
            // View examples
            String exampleProgram = "name \"Transfer Iron\"\n\nevery 20 ticks do\n  input from chest_in\n  output to chest_out\nend\n";
            setClipboardString(exampleProgram);
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(
                    new ChatComponentText(EnumChatFormatting.GREEN + "[SFM] Example program copied to clipboard!"));
            }
        } else if (button.id == 4) {
            // Discord
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop()
                    .isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop()
                        .browse(new URI("https://discord.gg/xjXYj9MmS4"));
                }
            } catch (Exception ignored) {}
        } else if (button.id == 5) {
            // Copy to clipboard
            if (manager.getDisk() != null) {
                setClipboardString(ItemDisk.getProgramString(manager.getDisk()));
                if (mc.thePlayer != null) {
                    mc.thePlayer.addChatMessage(
                        new ChatComponentText(EnumChatFormatting.GREEN + "[SFM] Program copied to clipboard!"));
                }
            }
        } else if (button.id == 6) {
            // View logs
            mc.displayGuiScreen(new GuiLogs(manager, this));
        } else if (button.id == 7) {
            // Rebuild cable network
            SFMNetwork.WRAPPER.sendToServer(
                new PacketManagerAction(
                    manager.xCoord,
                    manager.yCoord,
                    manager.zCoord,
                    PacketManagerAction.ACTION_REBUILD));
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(
                    new ChatComponentText(EnumChatFormatting.AQUA + "[SFM] Cable network rebuild requested."));
            }
        } else if (button.id == 8) {
            // View server config
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(
                    new ChatComponentText(
                        EnumChatFormatting.GRAY + "[SFM Server Config] Max logs: 500 | Cable limit: unlimited"));
            }
        } else if (button.id == 9) {
            // Reset
            SFMNetwork.WRAPPER.sendToServer(
                new PacketManagerAction(
                    manager.xCoord,
                    manager.yCoord,
                    manager.zCoord,
                    PacketManagerAction.ACTION_RESET));
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(
                    new ChatComponentText(EnumChatFormatting.YELLOW + "[SFM] Disk program and labels reset."));
            }
        } else if (button.id == 10) {
            // Diag button
            if (manager.getDisk() != null) {
                String diag = "SFM Diagnostics: " + manager.getStatusMessage();
                setClipboardString(diag);
                if (mc.thePlayer != null) {
                    mc.thePlayer.addChatMessage(
                        new ChatComponentText(
                            EnumChatFormatting.RED + "[SFM Diagnostics] " + manager.getStatusMessage()));
                }
            }
        }
    }

    @Override
    public void confirmClicked(boolean confirmed, int id) {
        if (confirmed && id == 12) SFMNetwork.WRAPPER.sendToServer(
            new PacketManagerAction(
                manager.xCoord,
                manager.yCoord,
                manager.zCoord,
                PacketManagerAction.ACTION_RESET_STATE));
        mc.displayGuiScreen(this);
    }

    public void onLogsReceived(String logLevel, List<LogEntry> logs) {
        // Logs updated from server
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        updateButtonStates();

        if (manager.getLogLevel() != LogLevel.OFF) {
            GL11.glColor4f(0.2F, 0.8F, 1.0F, 1.0F);
        } else {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }

        mc.getTextureManager()
            .bindTexture(GUI_TEXTURE);
        int left = (width - xSize) / 2;
        int top = (height - ySize) / 2;
        drawTexturedModalRect(left, top, 0, 0, xSize, ySize);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString("Manager", 8, 6, 0x404040);

        String stateStr = manager.isRunning() ? EnumChatFormatting.GREEN + "running"
            : EnumChatFormatting.RED + "stopped";
        fontRendererObj.drawString("State: " + stateStr, 8, 20, 0x404040);

        if (manager.getLogLevel() != LogLevel.OFF) {
            GL11.glPushMatrix();
            GL11.glScalef(0.5F, 0.5F, 1.0F);
            fontRendererObj.drawString(
                manager.getLogLevel()
                    .name(),
                16,
                27,
                0x00AAAA);
            GL11.glPopMatrix();
        }

        ItemStack disk = manager.getDisk();
        int relMouseX = mouseX - guiLeft;
        int relMouseY = mouseY - guiTop;
        boolean hoveringGraph = (relMouseX >= 50 && relMouseX <= 167 && relMouseY >= 38 && relMouseY <= 72);
        int hoveredIdx = -1;
        if (hoveringGraph) {
            hoveredIdx = Math.max(0, Math.min(19, (relMouseX - 53 + 3) / 6));
        }

        if (disk != null) {
            if (hoveredIdx != -1) {
                double[] tt = manager.getTickTimes();
                double hoveredMs = tt[hoveredIdx];
                EnumChatFormatting col = getLatencyColor(hoveredMs);
                String msg = "Hovered tick time: " + col
                    + String.format("%.3f", hoveredMs)
                    + EnumChatFormatting.RESET
                    + " ms";
                fontRendererObj.drawString(msg, 8, 29, 0x404040);
            } else {
                double peakMs = manager.getPeakTickTimeMs();
                EnumChatFormatting col = getLatencyColor(peakMs);
                String msg = "Peak tick time: " + col
                    + String.format("%.3f", peakMs)
                    + EnumChatFormatting.RESET
                    + " ms";
                fontRendererObj.drawString(msg, 8, 29, 0x404040);
            }

            drawTickGraph(hoveredIdx);
        }

        fontRendererObj.drawString("Inventory", 8, 73, 0x404040);
    }

    private EnumChatFormatting getLatencyColor(double ms) {
        if (ms <= 5.0) return EnumChatFormatting.GREEN;
        if (ms <= 15.0) return EnumChatFormatting.YELLOW;
        return EnumChatFormatting.RED;
    }

    private void drawTickGraph(int hoveredIdx) {
        int plotX = 53;
        int plotY = 40;
        int plotWidth = 114;
        int plotHeight = 30;

        // Subtle dark background
        drawRect(plotX, plotY, plotX + plotWidth, plotY + plotHeight, 0x22000000);

        // Border rectangle
        drawRect(plotX, plotY, plotX + plotWidth, plotY + 1, 0x80000000);
        drawRect(plotX, plotY + plotHeight - 1, plotX + plotWidth, plotY + plotHeight, 0x80000000);
        drawRect(plotX, plotY, plotX + 1, plotY + plotHeight, 0x80000000);
        drawRect(plotX + plotWidth - 1, plotY, plotX + plotWidth, plotY + plotHeight, 0x80000000);

        double[] tt = manager.getTickTimes();
        double peakMs = manager.getPeakTickTimeMs();
        long yMax = Math.max((long) (peakMs * 1_000_000L), 50_000_000L);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(1.5F);

        // Draw line strip
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (int i = 0; i < tt.length && i < 20; i++) {
            long yNanos = (long) (tt[i] * 1_000_000L);
            float norm = yNanos <= 0 ? 0.0f : (float) (Math.log10(yNanos) / Math.log10(yMax));
            int py = (plotY + plotHeight) - (int) (norm * (float) plotHeight);
            int px = plotX + 6 * i;

            if (tt[i] <= 5.0) {
                GL11.glColor4f(0.33F, 1.0F, 0.33F, 1.0F);
            } else if (tt[i] <= 15.0) {
                GL11.glColor4f(1.0F, 1.0F, 0.33F, 1.0F);
            } else {
                GL11.glColor4f(1.0F, 0.33F, 0.33F, 1.0F);
            }
            GL11.glVertex2i(px, py);
        }
        GL11.glEnd();

        // Draw hover vertical indicator line
        if (hoveredIdx >= 0 && hoveredIdx < 20) {
            int hx = plotX + 6 * hoveredIdx;
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.85F);
            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex2i(hx, plotY);
            GL11.glVertex2i(hx, plotY + plotHeight);
            GL11.glEnd();
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
