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

public class GuiLogs extends GuiScreen implements ProgramEditSession.Receiver {

    private ProgramEditSession authority;
    private boolean authorityRequested;
    private ca.teamdman.sfm.common.gui.ManagerControlToken token;

    @Override
    public void receiveProgramEditReply(ca.teamdman.sfm.common.net.PacketProgramEditReply reply) {
        if (authority != null && authority.receive(reply) && reply.accepted && reply.snapshot != null)
            token = new ca.teamdman.sfm.common.gui.ManagerControlToken(
                reply.snapshot.sessionId,
                reply.snapshot.bindingGeneration,
                reply.windowId);
    }

    @Override
    public void onGuiClosed() {
        if (authority != null) authority.state.cancel();
        super.onGuiClosed();
    }

    private final TileEntityManager manager;
    private final GuiScreen parentScreen;

    private static boolean savedWordWrap = false;
    private boolean wordWrap = savedWordWrap;

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
    private int scrollColOffset = 0;

    // Scrollbar interaction state
    private boolean isDraggingVScroll = false;
    private boolean isDraggingHScroll = false;
    private int dragClickOffsetY = 0;
    private int dragClickOffsetX = 0;
    private int scrollbarW = 8;
    private int scrollbarH = 8;

    // Buttons
    private final List<GuiButton> levelButtons = new ArrayList<>();
    private GuiButton btnCopy;
    private GuiButton btnWrap;
    private GuiButton btnDone;
    private GuiButton btnClear;

    private static final String NO_CONTENT_TEXT = "Ahoy, world!\n"
        + "Change the log level using the buttons at the top of this screen.\n"
        + "Trace, debug, and info log levels will be turned off after a single program execution.\n"
        + "Logging can make statements take longer to execute.\n"
        + "Use the copy button to help view in a different editor.";

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
        return new LogLevel[] { LogLevel.OFF, LogLevel.TRACE, LogLevel.DEBUG, LogLevel.INFO, LogLevel.WARN,
            LogLevel.ERROR };
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

        // 2. Bottom row: Copy logs (80x20), Wrap toggle (72x20), Done (152x20), Clear logs (84x20)
        int bottomY = height / 2 + 95;
        btnCopy = new GuiButton(1, width / 2 - 200, bottomY, 80, 20, "Copy logs");
        btnWrap = new GuiButton(3, width / 2 - 116, bottomY, 72, 20, wordWrap ? "Wrap: ON" : "Wrap: OFF");
        btnDone = new GuiButton(0, width / 2 - 40, bottomY, 152, 20, "Done");
        btnClear = new GuiButton(2, width / 2 + 116, bottomY, 84, 20, "Clear logs");

        buttonList.add(btnCopy);
        buttonList.add(btnWrap);
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
        if (!authorityRequested) {
            authorityRequested = true;
            authority = new ProgramEditSession(manager, mc.thePlayer.openContainer.windowId);
            authority.open();
        }
        authority.state.tick();
        if (++pollTimer % 10 == 0) {
            SFMNetwork.WRAPPER
                .sendToServer(new PacketManagerRequestLogs(manager.xCoord, manager.yCoord, manager.zCoord));
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
        int wrapWidth = termW - 8 - scrollbarW;
        List<String> rendered = getFormattedDisplayLines(wrapWidth);
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
            int wrapWidth = termW - 8 - scrollbarW;
            List<String> rendered = getFormattedDisplayLines(wrapWidth);
            boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

            if (shift && !wordWrap) {
                // Horizontal scrolling
                int maxLineWidth = 0;
                for (String l : rendered) {
                    int w = fontRendererObj.getStringWidth(l);
                    if (w > maxLineWidth) maxLineWidth = w;
                }
                int viewW = termW - (rendered.size() > visibleLines ? scrollbarW : 0);
                int maxOffset = Math.max(0, (maxLineWidth + 12) - viewW);
                if (dWheel > 0) {
                    scrollColOffset = Math.max(0, scrollColOffset - 16);
                } else {
                    scrollColOffset = Math.min(maxOffset, scrollColOffset + 16);
                }
            } else {
                // Vertical scrolling
                int maxOffset = Math.max(0, rendered.size() - visibleLines);
                if (dWheel > 0) {
                    scrollOffset = Math.max(0, scrollOffset - 2);
                } else {
                    scrollOffset = Math.min(maxOffset, scrollOffset + 2);
                }
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
                sb.append(sec / 60)
                    .append("m")
                    .append(sec % 60)
                    .append("s ago [")
                    .append(
                        e.getLevel()
                            .name())
                    .append("] ")
                    .append(e.getMessage())
                    .append("\n");
            }
            setClipboardString(sb.toString());
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(
                    new ChatComponentText(EnumChatFormatting.GREEN + "[SFM] Logs copied to clipboard!"));
            }
        } else if (btn.id == 2) {
            // Clear logs
            SFMNetwork.WRAPPER
                .sendToServer(new PacketManagerClearLogs(manager.xCoord, manager.yCoord, manager.zCoord, token));
            logs.clear();
            scrollOffset = 0;
            scrollColOffset = 0;
        } else if (btn.id == 3) {
            // Toggle word wrap
            wordWrap = !wordWrap;
            savedWordWrap = wordWrap;
            btnWrap.displayString = wordWrap ? "Wrap: ON" : "Wrap: OFF";
            scrollColOffset = 0;
            scrollOffset = 0;
        } else if (btn.id >= 10 && btn.id < 10 + getDisplayLevels().length) {
            LogLevel lvl = getDisplayLevels()[btn.id - 10];
            currentLogLevel = lvl;
            manager.setLogLevel(lvl);
            SFMNetwork.WRAPPER.sendToServer(
                new PacketManagerSetLogLevel(manager.xCoord, manager.yCoord, manager.zCoord, lvl.name(), false, token));
            updateLevelButtons();
        }
    }

    private List<String> getRawLogLines() {
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

    @SuppressWarnings("unchecked")
    private List<String> getFormattedDisplayLines(int wrapWidth) {
        List<String> raw = getRawLogLines();
        if (!wordWrap) {
            return raw;
        }
        List<String> result = new ArrayList<>();
        for (String line : raw) {
            List<String> wrapped = fontRendererObj.listFormattedStringToWidth(line, Math.max(20, wrapWidth));
            result.addAll(wrapped);
        }
        return result;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            int wrapWidth = termW - 8 - scrollbarW;
            List<String> displayLines = getFormattedDisplayLines(wrapWidth);
            boolean hasVScroll = displayLines.size() > visibleLines;
            int viewW = termW - (hasVScroll ? scrollbarW : 0);

            int maxLineWidth = 0;
            for (String l : displayLines) {
                int w = fontRendererObj.getStringWidth(l);
                if (w > maxLineWidth) maxLineWidth = w;
            }
            boolean hasHScroll = !wordWrap && (maxLineWidth + 8 > viewW);
            int availH = termH - (hasHScroll ? scrollbarH : 0);
            int maxVOffset = Math.max(0, displayLines.size() - visibleLines);
            int maxHOffset = Math.max(0, (maxLineWidth + 12) - viewW);

            // Vertical scrollbar click
            if (hasVScroll && mouseX >= termX + termW - scrollbarW
                && mouseX <= termX + termW
                && mouseY >= termY
                && mouseY <= termY + availH) {
                float thumbH = Math.max(16, (float) visibleLines / displayLines.size() * availH);
                float maxThumbY = availH - thumbH;
                float currentThumbY = termY + ((float) scrollOffset / maxVOffset) * maxThumbY;
                if (mouseY >= currentThumbY && mouseY <= currentThumbY + thumbH) {
                    isDraggingVScroll = true;
                    dragClickOffsetY = (int) (mouseY - currentThumbY);
                } else {
                    float clickRatio = (float) (mouseY - termY) / availH;
                    scrollOffset = Math.min(Math.max(0, (int) (clickRatio * maxVOffset)), maxVOffset);
                    isDraggingVScroll = true;
                    dragClickOffsetY = (int) (thumbH / 2);
                }
                return;
            }

            // Horizontal scrollbar click
            if (hasHScroll && mouseY >= termY + termH - scrollbarH
                && mouseY <= termY + termH
                && mouseX >= termX
                && mouseX <= termX + viewW) {
                float thumbW = Math.max(16, (float) viewW / (maxLineWidth + 12) * viewW);
                float maxThumbX = viewW - thumbW;
                float currentThumbX = termX + ((float) scrollColOffset / maxHOffset) * maxThumbX;
                if (mouseX >= currentThumbX && mouseX <= currentThumbX + thumbW) {
                    isDraggingHScroll = true;
                    dragClickOffsetX = (int) (mouseX - currentThumbX);
                } else {
                    float clickRatio = (float) (mouseX - termX) / viewW;
                    scrollColOffset = Math.min(Math.max(0, (int) (clickRatio * maxHOffset)), maxHOffset);
                    isDraggingHScroll = true;
                    dragClickOffsetX = (int) (thumbW / 2);
                }
                return;
            }
        }
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int which) {
        super.mouseMovedOrUp(mouseX, mouseY, which);
        if (which == 0) {
            isDraggingVScroll = false;
            isDraggingHScroll = false;
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        int wrapWidth = termW - 8 - scrollbarW;
        List<String> displayLines = getFormattedDisplayLines(wrapWidth);
        boolean hasVScroll = displayLines.size() > visibleLines;
        int viewW = termW - (hasVScroll ? scrollbarW : 0);

        int maxLineWidth = 0;
        for (String l : displayLines) {
            int w = fontRendererObj.getStringWidth(l);
            if (w > maxLineWidth) maxLineWidth = w;
        }
        boolean hasHScroll = !wordWrap && (maxLineWidth + 8 > viewW);
        int availH = termH - (hasHScroll ? scrollbarH : 0);
        int maxVOffset = Math.max(0, displayLines.size() - visibleLines);
        int maxHOffset = Math.max(0, (maxLineWidth + 12) - viewW);

        if (isDraggingVScroll && maxVOffset > 0) {
            float thumbH = Math.max(16, (float) visibleLines / displayLines.size() * availH);
            float maxThumbY = availH - thumbH;
            float newThumbY = mouseY - termY - dragClickOffsetY;
            float ratio = Math.max(0.0f, Math.min(1.0f, newThumbY / maxThumbY));
            scrollOffset = Math.round(ratio * maxVOffset);
        }

        if (isDraggingHScroll && maxHOffset > 0) {
            float thumbW = Math.max(16, (float) viewW / (maxLineWidth + 12) * viewW);
            float maxThumbX = viewW - thumbW;
            float newThumbX = mouseX - termX - dragClickOffsetX;
            float ratio = Math.max(0.0f, Math.min(1.0f, newThumbX / maxThumbX));
            scrollColOffset = Math.round(ratio * maxHOffset);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        // 1. Crisp 1px white border around terminal box (matching modern SFM)
        drawRect(termX - 1, termY - 1, termX + termW + 1, termY + termH + 1, 0xFFFFFFFF);
        drawRect(termX, termY, termX + termW, termY + termH, 0xFF000000);

        // 2. Measure display lines and scrollbar needs
        int wrapWidth = termW - 8 - scrollbarW;
        List<String> displayLines = getFormattedDisplayLines(wrapWidth);

        int maxLineWidth = 0;
        for (String l : displayLines) {
            int w = fontRendererObj.getStringWidth(l);
            if (w > maxLineWidth) maxLineWidth = w;
        }

        boolean hasVScroll = displayLines.size() > visibleLines;
        int viewW = termW - (hasVScroll ? scrollbarW : 0);
        boolean hasHScroll = !wordWrap && (maxLineWidth + 8 > viewW);
        int availH = termH - (hasHScroll ? scrollbarH : 0);
        visibleLines = Math.max(1, (availH - 8) / lineHeight);

        int maxVOffset = Math.max(0, displayLines.size() - visibleLines);
        if (scrollOffset > maxVOffset) {
            scrollOffset = maxVOffset;
        }

        int maxHOffset = Math.max(0, (maxLineWidth + 12) - viewW);
        if (wordWrap || scrollColOffset > maxHOffset) {
            scrollColOffset = wordWrap ? 0 : maxHOffset;
        }

        int textX = termX + 4;
        int textY = termY + 4;

        net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(
            mc,
            mc.displayWidth,
            mc.displayHeight);
        int scale = sr.getScaleFactor();

        // Enable GL scissor for exact terminal window clipping
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(termX * scale, (height - (termY + availH)) * scale, viewW * scale, availH * scale);

        for (int i = 0; i < visibleLines; i++) {
            int lineIdx = scrollOffset + i;
            if (lineIdx >= displayLines.size()) break;
            String line = displayLines.get(lineIdx);
            fontRendererObj.drawString(line, textX - scrollColOffset, textY + i * lineHeight, 0xFFFFFF);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // 3. Vertical Scrollbar if needed
        if (hasVScroll) {
            int scrollbarX = termX + termW - scrollbarW;
            drawRect(scrollbarX, termY, scrollbarX + scrollbarW, termY + availH, 0xFF2D2D30);
            float thumbH = Math.max(16, (float) visibleLines / displayLines.size() * availH);
            float maxThumbY = availH - thumbH;
            float thumbY = termY + ((float) scrollOffset / maxVOffset) * maxThumbY;
            int thumbCol = isDraggingVScroll ? 0xFF7A7A7E : 0xFF505054;
            drawRect(scrollbarX + 1, (int) thumbY, scrollbarX + scrollbarW - 1, (int) (thumbY + thumbH), thumbCol);
        }

        // 4. Horizontal Scrollbar if needed (mutually exclusive with word-wrap)
        if (hasHScroll) {
            int hScrollY = termY + termH - scrollbarH;
            drawRect(termX, hScrollY, termX + viewW, hScrollY + scrollbarH, 0xFF2D2D30);
            float thumbW = Math.max(16, (float) viewW / (maxLineWidth + 12) * viewW);
            float maxThumbX = viewW - thumbW;
            float thumbX = termX + ((float) scrollColOffset / maxHOffset) * maxThumbX;
            int thumbCol = isDraggingHScroll ? 0xFF7A7A7E : 0xFF505054;
            drawRect((int) thumbX, hScrollY + 1, (int) (thumbX + thumbW), hScrollY + scrollbarH - 1, thumbCol);
        }

        // 5. Corner junction if both scrollbars are visible
        if (hasVScroll && hasHScroll) {
            drawRect(termX + termW - scrollbarW, termY + termH - scrollbarH, termX + termW, termY + termH, 0xFF252526);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
