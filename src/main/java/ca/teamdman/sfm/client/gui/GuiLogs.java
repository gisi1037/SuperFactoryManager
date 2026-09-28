package ca.teamdman.sfm.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import ca.teamdman.sfm.common.net.PacketManagerClearLogs;
import ca.teamdman.sfm.common.net.PacketManagerRequestLogs;
import ca.teamdman.sfm.common.net.PacketManagerSetLogLevel;
import ca.teamdman.sfm.common.net.SFMNetwork;
import ca.teamdman.sfm.common.tileentity.LogEntry;
import ca.teamdman.sfm.common.tileentity.LogLevel;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

public class GuiLogs extends GuiScreen {

    private final TileEntityManager manager;
    private final GuiScreen parentScreen;

    private final List<LogEntry> logs = new ArrayList<>();
    private LogLevel currentLogLevel = LogLevel.OFF;

    // Viewport layout (matching modern SFM 400x180 px centered terminal)
    private int termX;
    private int termY;
    private int termW = 400;
    private int termH = 180;
    private int lineHeight = 10;
    private int visibleLines;
    private int scrollOffset = 0;

    // Buttons
    private final List<GuiButton> levelButtons = new ArrayList<>();
    private GuiButton btnCopy;
    private GuiButton btnDone;
    private GuiButton btnClear;

    private static final String NO_CONTENT_TEXT =
        "Ahoy, world!\n" +
        "Change the log level using the buttons at the top of this screen.\n" +
        "Trace, debug, and info log levels will be turned off after a single program execution.\n" +
        "Logging can make statements take longer to execute.\n" +
        "Use the copy button to help view in a different editor.";

    public GuiLogs(TileEntityManager manager, GuiScreen parentScreen) {
        this.manager = manager;
        this.parentScreen = parentScreen;
        this.currentLogLevel = manager.getLogLevel();
        this.logs.addAll(manager.getLogs());
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public static LogLevel[] getDisplayLevels() {
        return new LogLevel[] {
            LogLevel.OFF, LogLevel.TRACE, LogLevel.DEBUG, LogLevel.INFO, LogLevel.WARN, LogLevel.ERROR
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        buttonList.clear();
        levelButtons.clear();
        Keyboard.enableRepeatEvents(true);

        termX = (width - termW) / 2;
        termY = (height - termH) / 2;
        visibleLines = Math.max(1, (termH - 8) / lineHeight);

        // 1. Top row: 6 log level buttons (OFF, TRACE, DEBUG, INFO, WARN, ERROR)
        LogLevel[] levels = getDisplayLevels();
        int btnW = 60;
        int btnH = 20;
        int spacing = 5;
        int totalBtnW = btnW * levels.length + spacing * (levels.length - 1);
        int startX = (width - totalBtnW) / 2;
        int startY = height / 2 - 115;

        for (int i = 0; i < levels.length; i++) {
            LogLevel lvl = levels[i];
            GuiButton btn = new GuiButton(10 + i, startX + i * (btnW + spacing), startY, btnW, btnH, lvl.name());
            btn.enabled = (lvl != currentLogLevel);
            levelButtons.add(btn);
            buttonList.add(btn);
        }

        // 2. Bottom row: Copy logs (80x20), Done (200x20), Clear logs (80x20)
        int bottomY = height / 2 + 95;
        btnCopy = new GuiButton(1, width / 2 - 200, bottomY, 80, 20, "Copy logs");
        btnDone = new GuiButton(0, width / 2 - 102, bottomY, 200, 20, "Done");
        btnClear = new GuiButton(2, width / 2 + 113, bottomY, 80, 20, "Clear logs");

        buttonList.add(btnCopy);
        buttonList.add(btnDone);
        buttonList.add(btnClear);

        // Request logs from server
        SFMNetwork.WRAPPER.sendToServer(new PacketManagerRequestLogs(manager.xCoord, manager.yCoord, manager.zCoord));
        scrollToBottom();
    }

    private int pollTimer = 0;

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (++pollTimer % 10 == 0) {
            SFMNetwork.WRAPPER.sendToServer(new PacketManagerRequestLogs(manager.xCoord, manager.yCoord, manager.zCoord));
        }
    }

    public void onLogsReceived(String logLevelName, List<LogEntry> newLogs) {
        this.currentLogLevel = LogLevel.fromString(logLevelName);
        this.logs.clear();
        if (newLogs != null) {
            this.logs.addAll(newLogs);
        }
        updateLevelButtons();
    }

    private void updateLevelButtons() {
        LogLevel[] levels = getDisplayLevels();
        for (int i = 0; i < levels.length && i < levelButtons.size(); i++) {
            levelButtons.get(i).enabled = (levels[i] != currentLogLevel);
        }
    }

    private void scrollToBottom() {
        List<String> rendered = getFormattedDisplayLines();
        if (rendered.size() > visibleLines) {
            scrollOffset = rendered.size() - visibleLines;
        } else {
            scrollOffset = 0;
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int dWheel = Mouse.getDWheel();
        if (dWheel != 0) {
            List<String> rendered = getFormattedDisplayLines();
            int maxOffset = Math.max(0, rendered.size() - visibleLines);
            if (dWheel > 0) {
                scrollOffset = Math.max(0, scrollOffset - 2);
            } else {
                scrollOffset = Math.min(maxOffset, scrollOffset + 2);
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        if (btn.id == 0) {
            // Done -> back to manager
            mc.displayGuiScreen(parentScreen);
        } else if (btn.id == 1) {
            // Copy logs
            StringBuilder sb = new StringBuilder();
            sb.append("-- SUPER FACTORY MANAGER LOGS --\n");
            long now = System.currentTimeMillis();
            for (LogEntry e : logs) {
                long sec = Math.max(0, (now - e.getTimestamp()) / 1000);
                sb.append(sec / 60).append("m").append(sec % 60).append("s ago [")
                  .append(e.getLevel().name()).append("] ")
                  .append(e.getMessage()).append("\n");
            }
            setClipboardString(sb.toString());
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "[SFM] Logs copied to clipboard!"));
            }
        } else if (btn.id == 2) {
            // Clear logs
            SFMNetwork.WRAPPER.sendToServer(new PacketManagerClearLogs(manager.xCoord, manager.yCoord, manager.zCoord));
            logs.clear();
            scrollOffset = 0;
        } else if (btn.id >= 10 && btn.id < 10 + getDisplayLevels().length) {
            LogLevel lvl = getDisplayLevels()[btn.id - 10];
            currentLogLevel = lvl;
            manager.setLogLevel(lvl);
            SFMNetwork.WRAPPER.sendToServer(
                new PacketManagerSetLogLevel(manager.xCoord, manager.yCoord, manager.zCoord, lvl.name(), false));
            updateLevelButtons();
        }
    }

    private List<String> getFormattedDisplayLines() {
        List<String> result = new ArrayList<>();
        if (logs.isEmpty()) {
            String[] emptyLines = NO_CONTENT_TEXT.split("\n");
            for (String el : emptyLines) {
                result.add(EnumChatFormatting.GRAY + el);
            }
            return result;
        }

        long now = System.currentTimeMillis();
        for (LogEntry e : logs) {
            long totalSec = Math.max(0, (now - e.getTimestamp()) / 1000);
            long m = totalSec / 60;
            long s = totalSec % 60;
            String ago = EnumChatFormatting.GRAY + "" + m + "m" + s + "s ago ";

            String lvlBadge;
            LogLevel lvl = e.getLevel();
            if (lvl == LogLevel.TRACE) {
                lvlBadge = EnumChatFormatting.DARK_GRAY + "[TRACE]";
            } else if (lvl == LogLevel.DEBUG) {
                lvlBadge = EnumChatFormatting.AQUA + "[DEBUG]";
            } else if (lvl == LogLevel.INFO) {
                lvlBadge = EnumChatFormatting.GREEN + "[INFO]";
            } else if (lvl == LogLevel.WARN) {
                lvlBadge = EnumChatFormatting.YELLOW + "[WARN]";
            } else if (lvl == LogLevel.ERROR) {
                lvlBadge = EnumChatFormatting.RED + "[ERROR]";
            } else {
                lvlBadge = EnumChatFormatting.WHITE + "[" + lvl.name() + "]";
            }

            String msg = EnumChatFormatting.WHITE + " " + e.getMessage();
            result.add(ago + lvlBadge + msg);
        }
        return result;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        // 1. Crisp 1px white border around terminal box (matching modern SFM)
        drawRect(termX - 1, termY - 1, termX + termW + 1, termY + termH + 1, 0xFFFFFFFF);
        drawRect(termX, termY, termX + termW, termY + termH, 0xFF000000);

        // 2. Render log lines with scissor clipping
        List<String> displayLines = getFormattedDisplayLines();
        int maxOffset = Math.max(0, displayLines.size() - visibleLines);
        if (scrollOffset > maxOffset) {
            scrollOffset = maxOffset;
        }

        int textX = termX + 4;
        int textY = termY + 4;

        int scale = mc.gameSettings.guiScale;
        if (scale == 0) scale = 2; // fallback

        // Enable GL scissor for exact terminal window clipping
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(termX * scale, (height - (termY + termH)) * scale, termW * scale, termH * scale);

        for (int i = 0; i < visibleLines; i++) {
            int lineIdx = scrollOffset + i;
            if (lineIdx >= displayLines.size()) break;
            String line = displayLines.get(lineIdx);
            fontRendererObj.drawString(line, textX, textY + i * lineHeight, 0xFFFFFF);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // 3. Scrollbar if needed
        if (displayLines.size() > visibleLines) {
            int scrollbarX = termX + termW - 4;
            int trackH = termH;
            float thumbH = Math.max(16, (float) visibleLines / displayLines.size() * trackH);
            float thumbY = termY + ((float) scrollOffset / maxOffset) * (trackH - thumbH);
            drawRect(scrollbarX, (int) thumbY, scrollbarX + 2, (int) (thumbY + thumbH), 0xFF888888);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
