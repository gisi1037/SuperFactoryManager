package ca.teamdman.sfm.common.gui;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import ca.teamdman.sfm.client.gui.GuiCodeEditor;
import ca.teamdman.sfm.client.gui.GuiLogs;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.net.PacketManagerSetLogLevel;
import ca.teamdman.sfm.common.net.PacketSaveProgram;
import ca.teamdman.sfm.common.net.SFMNetwork;
import ca.teamdman.sfm.common.tileentity.LogEntry;
import ca.teamdman.sfm.common.tileentity.LogLevel;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

public class GuiManager extends GuiContainer {

    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(
        "superfactorymanager:textures/gui/manager.png");

    private final TileEntityManager manager;

    private GuiButton btnEdit;
    private GuiButton btnLogs;
    private GuiButton btnToggleRun;
    private GuiButton btnRunDebug;
    private GuiButton btnPaste;
    private GuiButton btnCopy;

    public GuiManager(InventoryPlayer playerInv, TileEntityManager manager) {
        super(new ContainerManager(playerInv, manager));
        this.manager = manager;
        this.xSize = 220;
        this.ySize = 224;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        super.initGui();
        int left = (width - xSize) / 2;
        int top = (height - ySize) / 2;

        boolean hasDisk = manager.getDisk() != null;

        // Button 1: Edit code in full viewpane
        btnEdit = new GuiButton(1, left + 10, top + 18, 70, 20, EnumChatFormatting.GREEN + "Edit Code");

        // Button 2: View logs and output terminal
        btnLogs = new GuiButton(2, left + 85, top + 18, 70, 20, EnumChatFormatting.AQUA + "View Logs");

        // Button 3: Toggle Running
        btnToggleRun = new GuiButton(
            3,
            left + 10,
            top + 42,
            70,
            20,
            manager.isRunning() ? EnumChatFormatting.GREEN + "Running" : EnumChatFormatting.RED + "Stopped");

        // Button 4: Run Once with Debug logging
        btnRunDebug = new GuiButton(4, left + 85, top + 42, 70, 20, "Run (Debug)");

        // Button 5: Paste from clipboard
        btnPaste = new GuiButton(5, left + 10, top + 66, 70, 20, "Paste Code");

        // Button 6: Copy code to clipboard
        btnCopy = new GuiButton(6, left + 85, top + 66, 70, 20, "Copy Code");

        btnEdit.enabled = hasDisk;
        btnLogs.enabled = hasDisk;
        btnToggleRun.enabled = hasDisk;
        btnRunDebug.enabled = hasDisk;
        btnPaste.enabled = hasDisk;
        btnCopy.enabled = hasDisk;

        buttonList.add(btnEdit);
        buttonList.add(btnLogs);
        buttonList.add(btnToggleRun);
        buttonList.add(btnRunDebug);
        buttonList.add(btnPaste);
        buttonList.add(btnCopy);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            // Open dedicated Code Editor viewpane
            mc.displayGuiScreen(new GuiCodeEditor(manager, this));
        } else if (button.id == 2) {
            // Open dedicated Logs & Output Terminal
            mc.displayGuiScreen(new GuiLogs(manager, this));
        } else if (button.id == 3) {
            // Toggle Running
            boolean nextState = !manager.isRunning();
            manager.setRunning(nextState);
            btnToggleRun.displayString = nextState ? EnumChatFormatting.GREEN + "Running"
                : EnumChatFormatting.RED + "Stopped";
            String code = manager.getDisk() != null ? ItemDisk.getProgramString(manager.getDisk()) : "";
            SFMNetwork.WRAPPER
                .sendToServer(new PacketSaveProgram(manager.xCoord, manager.yCoord, manager.zCoord, code, nextState));
        } else if (button.id == 4) {
            // Run Once with Debug logs
            SFMNetwork.WRAPPER.sendToServer(
                new PacketManagerSetLogLevel(
                    manager.xCoord,
                    manager.yCoord,
                    manager.zCoord,
                    LogLevel.DEBUG.name(),
                    true));
        } else if (button.id == 5) {
            // Paste
            String clip = getClipboardString();
            if (clip != null && !clip.isEmpty() && manager.getDisk() != null) {
                manager.saveCodeAndRecompile(clip);
                SFMNetwork.WRAPPER.sendToServer(
                    new PacketSaveProgram(manager.xCoord, manager.yCoord, manager.zCoord, clip, manager.isRunning()));
            }
        } else if (button.id == 6) {
            // Copy
            if (manager.getDisk() != null) {
                setClipboardString(ItemDisk.getProgramString(manager.getDisk()));
            }
        }
    }

    public void onLogsReceived(String logLevel, List<LogEntry> logs) {
        // Logs updated from server
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int left = (width - xSize) / 2;
        int top = (height - ySize) / 2;

        // Outer container background
        drawRect(left, top, left + xSize, top + ySize, 0xFF333333);
        drawRect(left + 2, top + 2, left + xSize - 2, top + ySize - 2, 0xFFC6C6C6);

        // Control Panel background
        drawRect(left + 6, top + 14, left + 165, top + 130, 0xFF2A2A2A);

        // Disk Slot border
        int slotX = left + 185;
        int slotY = top + 17;
        drawRect(slotX, slotY, slotX + 18, slotY + 18, 0xFF373737);
        drawRect(slotX + 1, slotY + 1, slotX + 17, slotY + 17, 0xFF8B8B8B);

        // Player inventory background boxes
        drawRect(left + 26, top + 140, left + 26 + 18 * 9 + 2, top + 140 + 18 * 3 + 2, 0xFF8B8B8B);
        drawRect(left + 26, top + 198, left + 26 + 18 * 9 + 2, top + 198 + 18 + 2, 0xFF8B8B8B);

        // Update button states
        boolean hasDisk = manager.getDisk() != null;
        btnEdit.enabled = hasDisk;
        btnLogs.enabled = hasDisk;
        btnToggleRun.enabled = hasDisk;
        btnRunDebug.enabled = hasDisk;
        btnPaste.enabled = hasDisk;
        btnCopy.enabled = hasDisk;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString("Super Factory Manager", 10, 4, 0x404040);
        fontRendererObj.drawString("Disk", 185, 6, 0x404040);

        // Status & Diagnostic section inside panel
        ItemStack disk = manager.getDisk();
        int panelY = 92;
        if (disk == null) {
            fontRendererObj.drawString(EnumChatFormatting.RED + "Please insert Disk", 12, panelY, 0xFF5555);
            fontRendererObj.drawString(EnumChatFormatting.GRAY + "to edit and run code.", 12, panelY + 10, 0x888888);
        } else {
            String prog = ItemDisk.getProgramString(disk);
            int lineCount = prog.isEmpty() ? 0 : prog.split("\n", -1).length;
            fontRendererObj.drawString("Lines: " + EnumChatFormatting.WHITE + lineCount, 12, panelY, 0xCCCCCC);

            String status = manager.getStatusMessage();
            int color = status.equals("Running") ? 0x55FF55 : 0xFF5555;
            fontRendererObj
                .drawString(fontRendererObj.trimStringToWidth("Status: " + status, 150), 12, panelY + 10, color);

            double peakMs = manager.getPeakTickTimeMs();
            String peakStr = String.format("Peak: %.2f ms", peakMs);
            fontRendererObj.drawString(peakStr, 12, panelY + 20, 0xAAAAAA);

            String logLvlStr = "Log: " + EnumChatFormatting.AQUA
                + manager.getLogLevel()
                    .getDisplayName();
            fontRendererObj.drawString(logLvlStr, 90, panelY, 0xCCCCCC);
        }
    }
}
