package ca.teamdman.sfm.client.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
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
    private final List<LogEntry> filteredLogs = new ArrayList<>();

    private GuiTextField searchField;
    private LogLevel currentLogLevel = LogLevel.OFF;
    private boolean autoScroll = true;

    // Viewport layout
    private int termX;
    private int termY;
    private int termW;
    private int termH;
    private int scrollbarW = 10;
    private int lineHeight = 10;
    private int visibleLines;
    private int scrollOffset = 0;

    // Scrollbar drag
    private boolean isDraggingVScroll = false;
    private int dragClickOffsetY = 0;

    // Buttons
    private final List<GuiButton> levelButtons = new ArrayList<>();
    private GuiButton btnRunOnce;
    private GuiButton btnAutoScroll;
    private GuiButton btnClear;
    private GuiButton btnCopy;
    private GuiButton btnBack;

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    public GuiLogs(TileEntityManager manager, GuiScreen parentScreen) {
        this.manager = manager;
        this.parentScreen = parentScreen;
        this.currentLogLevel = manager.getLogLevel();
        this.logs.addAll(manager.getLogs());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        buttonList.clear();
        levelButtons.clear();
        Keyboard.enableRepeatEvents(true);

        int marginX = 20;
        int topMargin = 58;
        int bottomMargin = 36;

        termX = marginX;
        termY = topMargin;
        termW = width - marginX * 2;
        termH = height - topMargin - bottomMargin;

        visibleLines = Math.max(1, (termH - 4) / lineHeight);

        // 1. Top Row: Log Level buttons
        LogLevel[] levels = LogLevel.values();
        int btnW = 46;
        int btnH = 18;
        int startX = marginX;
        for (int i = 0; i < levels.length; i++) {
            LogLevel lvl = levels[i];
            boolean isActive = lvl == currentLogLevel;
            String label = (isActive ? EnumChatFormatting.YELLOW + "> " : "") + lvl.getDisplayName();
            GuiButton btn = new GuiButton(10 + i, startX + i * (btnW + 4), 18, btnW, btnH, label);
            levelButtons.add(btn);
            buttonList.add(btn);
        }

        btnRunOnce = new GuiButton(
            20,
            startX + levels.length * (btnW + 4) + 8,
            18,
            90,
            btnH,
            EnumChatFormatting.AQUA + "Run with "
                + (currentLogLevel == LogLevel.OFF ? "INFO" : currentLogLevel.getDisplayName()));
        buttonList.add(btnRunOnce);

        // 2. Search Field
        int searchY = 38;
        searchField = new GuiTextField(fontRendererObj, marginX, searchY, 220, 16);
        searchField.setMaxStringLength(64);

        btnAutoScroll = new GuiButton(
            30,
            marginX + 225,
            searchY - 1,
            90,
            18,
            "Auto-scroll: " + (autoScroll ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF"));
        buttonList.add(btnAutoScroll);

        // 3. Bottom Row
        int btnY = height - bottomMargin + 8;
        btnClear = new GuiButton(1, marginX, btnY, 80, 20, "Clear Logs");
        btnCopy = new GuiButton(2, marginX + 85, btnY, 100, 20, "Copy to Clipboard");
        btnBack = new GuiButton(0, marginX + 190, btnY, 90, 20, "Back to Manager");

        buttonList.add(btnClear);
        buttonList.add(btnCopy);
        buttonList.add(btnBack);

        // Request freshest logs from server
        SFMNetwork.WRAPPER.sendToServer(new PacketManagerRequestLogs(manager.xCoord, manager.yCoord, manager.zCoord));

        rebuildFilteredLogs();
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
        rebuildFilteredLogs();
        updateLevelButtons();
    }

    private void updateLevelButtons() {
        LogLevel[] levels = LogLevel.values();
        for (int i = 0; i < levels.length && i < levelButtons.size(); i++) {
            LogLevel lvl = levels[i];
            boolean isActive = lvl == currentLogLevel;
            levelButtons.get(i).displayString = (isActive ? EnumChatFormatting.YELLOW + "> " : "")
                + lvl.getDisplayName();
        }
        if (btnRunOnce != null) {
            btnRunOnce.displayString = EnumChatFormatting.AQUA + "Run with "
                + (currentLogLevel == LogLevel.OFF ? "INFO" : currentLogLevel.getDisplayName());
        }
    }

    private void rebuildFilteredLogs() {
        filteredLogs.clear();
        String query = searchField != null ? searchField.getText()
            .trim()
            .toLowerCase() : "";

        for (LogEntry entry : logs) {
            if (query.isEmpty() || entry.getMessage()
                .toLowerCase()
                .contains(query)
                || entry.getLevel()
                    .name()
                    .toLowerCase()
                    .contains(query)) {
                filteredLogs.add(entry);
            }
        }

        if (autoScroll) {
            scrollToBottom();
        } else {
            scrollOffset = Math.min(scrollOffset, Math.max(0, filteredLogs.size() - visibleLines));
        }
    }

    private void scrollToBottom() {
        scrollOffset = Math.max(0, filteredLogs.size() - visibleLines);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parentScreen);
            return;
        }

        if (searchField.textboxKeyTyped(typedChar, keyCode)) {
            rebuildFilteredLogs();
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        searchField.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0) {
            int scrollbarX = termX + termW - scrollbarW;
            if (mouseX >= scrollbarX && mouseX <= scrollbarX + scrollbarW
                && mouseY >= termY
                && mouseY <= termY + termH) {
                int total = filteredLogs.size();
                if (total > visibleLines) {
                    float thumbHeight = Math.max(16, (float) visibleLines / total * termH);
                    float maxThumbY = termH - thumbHeight;
                    float currentThumbY = termY + ((float) scrollOffset / (total - visibleLines)) * maxThumbY;

                    if (mouseY >= currentThumbY && mouseY <= currentThumbY + thumbHeight) {
                        isDraggingVScroll = true;
                        dragClickOffsetY = (int) (mouseY - currentThumbY);
                    } else {
                        float clickRatio = (float) (mouseY - termY) / termH;
                        scrollOffset = Math
                            .min(Math.max(0, (int) (clickRatio * (total - visibleLines))), total - visibleLines);
                        isDraggingVScroll = true;
                        dragClickOffsetY = (int) (thumbHeight / 2);
                        autoScroll = false;
                        btnAutoScroll.displayString = "Auto-scroll: " + EnumChatFormatting.RED + "OFF";
                    }
                }
            }
        }
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int which) {
        super.mouseMovedOrUp(mouseX, mouseY, which);
        if (which == 0) {
            isDraggingVScroll = false;
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (isDraggingVScroll && filteredLogs.size() > visibleLines) {
            int total = filteredLogs.size();
            float thumbHeight = Math.max(16, (float) visibleLines / total * termH);
            float maxThumbY = termH - thumbHeight;
            float newThumbY = mouseY - termY - dragClickOffsetY;
            float ratio = Math.max(0.0f, Math.min(1.0f, newThumbY / maxThumbY));
            scrollOffset = Math.round(ratio * (total - visibleLines));
            autoScroll = false;
            btnAutoScroll.displayString = "Auto-scroll: " + EnumChatFormatting.RED + "OFF";
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int dWheel = Mouse.getDWheel();
        if (dWheel != 0) {
            int linesToScroll = 3;
            if (dWheel > 0) {
                scrollOffset = Math.max(0, scrollOffset - linesToScroll);
                autoScroll = false;
                btnAutoScroll.displayString = "Auto-scroll: " + EnumChatFormatting.RED + "OFF";
            } else {
                scrollOffset = Math.min(Math.max(0, filteredLogs.size() - visibleLines), scrollOffset + linesToScroll);
                if (scrollOffset >= filteredLogs.size() - visibleLines) {
                    autoScroll = true;
                    btnAutoScroll.displayString = "Auto-scroll: " + EnumChatFormatting.GREEN + "ON";
                }
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        if (btn.id == 0) {
            mc.displayGuiScreen(parentScreen);
        } else if (btn.id == 1) {
            // Clear
            SFMNetwork.WRAPPER.sendToServer(new PacketManagerClearLogs(manager.xCoord, manager.yCoord, manager.zCoord));
            logs.clear();
            filteredLogs.clear();
            scrollOffset = 0;
        } else if (btn.id == 2) {
            // Copy
            StringBuilder sb = new StringBuilder();
            for (LogEntry e : filteredLogs) {
                sb.append(TIME_FORMAT.format(new Date(e.getTimestamp())))
                    .append(" [")
                    .append(
                        e.getLevel()
                            .name())
                    .append("] ")
                    .append(e.getMessage())
                    .append("\n");
            }
            setClipboardString(sb.toString());
        } else if (btn.id == 20) {
            // Run Once
            LogLevel runLvl = currentLogLevel == LogLevel.OFF ? LogLevel.INFO : currentLogLevel;
            SFMNetwork.WRAPPER.sendToServer(
                new PacketManagerSetLogLevel(manager.xCoord, manager.yCoord, manager.zCoord, runLvl.name(), true));
        } else if (btn.id == 30) {
            // Toggle Auto-scroll
            autoScroll = !autoScroll;
            btnAutoScroll.displayString = "Auto-scroll: "
                + (autoScroll ? EnumChatFormatting.GREEN + "ON" : EnumChatFormatting.RED + "OFF");
            if (autoScroll) {
                scrollToBottom();
            }
        } else if (btn.id >= 10 && btn.id < 10 + LogLevel.values().length) {
            LogLevel lvl = LogLevel.values()[btn.id - 10];
            currentLogLevel = lvl;
            SFMNetwork.WRAPPER.sendToServer(
                new PacketManagerSetLogLevel(manager.xCoord, manager.yCoord, manager.zCoord, lvl.name(), false));
            updateLevelButtons();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        // 1. Header
        drawString(
            fontRendererObj,
            EnumChatFormatting.BOLD + "Super Factory Manager - Output Terminal & Logs",
            termX,
            6,
            0x55FFFF);

        // 2. Search Field
        searchField.drawTextBox();
        if (searchField.getText()
            .isEmpty() && !searchField.isFocused()) {
            drawString(
                fontRendererObj,
                EnumChatFormatting.ITALIC + "Search logs...",
                searchField.xPosition + 4,
                searchField.yPosition + 4,
                0x777777);
        }

        // 3. Terminal Box
        drawRect(termX - 1, termY - 1, termX + termW + 1, termY + termH + 1, 0xFF444444);
        drawRect(termX, termY, termX + termW, termY + termH, 0xFF0C0C0C);

        // 4. Scrollbar track
        int scrollbarX = termX + termW - scrollbarW;
        drawRect(scrollbarX, termY, scrollbarX + scrollbarW, termY + termH, 0xFF222222);

        // 5. Render Log Lines
        net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(
            mc,
            mc.displayWidth,
            mc.displayHeight);
        int sf = sr.getScaleFactor();
        int contentW = termW - scrollbarW - 4;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(termX * sf, (height - (termY + termH)) * sf, contentW * sf, termH * sf);

        if (filteredLogs.isEmpty()) {
            drawString(
                fontRendererObj,
                EnumChatFormatting.DARK_GRAY
                    + "No logs to display. Select a log level above or click 'Run' to generate logs.",
                termX + 8,
                termY + 8,
                0x888888);
        } else {
            for (int i = 0; i < visibleLines; i++) {
                int idx = scrollOffset + i;
                if (idx >= filteredLogs.size()) break;

                LogEntry entry = filteredLogs.get(idx);
                int y = termY + 3 + i * lineHeight;

                // Time
                String timeStr = TIME_FORMAT.format(new Date(entry.getTimestamp()));
                fontRendererObj.drawString(timeStr, termX + 4, y, 0x777777);

                // Level badge
                String badge = "[" + entry.getLevel()
                    .name() + "]";
                fontRendererObj.drawString(
                    badge,
                    termX + 54,
                    y,
                    entry.getLevel()
                        .getColor());

                // Message
                int msgX = termX + 104;
                fontRendererObj.drawString(entry.getMessage(), msgX, y, 0xEEEEEE);
            }
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // 6. Scrollbar thumb
        int total = filteredLogs.size();
        if (total > visibleLines) {
            float thumbHeight = Math.max(16, (float) visibleLines / total * termH);
            float maxThumbY = termH - thumbHeight;
            float thumbY = termY + ((float) scrollOffset / (total - visibleLines)) * maxThumbY;
            int thumbColor = isDraggingVScroll ? 0xFF7A7A7E : 0xFF505054;
            drawRect(
                scrollbarX + 1,
                (int) thumbY,
                scrollbarX + scrollbarW - 1,
                (int) (thumbY + thumbHeight),
                thumbColor);
        }

        // 7. Footer stats
        String countStr = "Showing " + filteredLogs.size() + " / " + logs.size() + " logs";
        drawString(
            fontRendererObj,
            countStr,
            termX + termW - fontRendererObj.getStringWidth(countStr),
            height - 20,
            0x888888);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
