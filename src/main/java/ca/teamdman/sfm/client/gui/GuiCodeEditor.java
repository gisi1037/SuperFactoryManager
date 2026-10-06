package ca.teamdman.sfm.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import ca.teamdman.sfm.client.gui.syntax.GuiAutocompletePopup;
import ca.teamdman.sfm.client.gui.syntax.SnippetCategory;
import ca.teamdman.sfm.client.gui.syntax.SyntaxRegistry;
import ca.teamdman.sfm.client.gui.syntax.SyntaxSnippet;
import ca.teamdman.sfm.common.item.ItemDisk;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.net.PacketSaveProgram;
import ca.teamdman.sfm.common.net.SFMNetwork;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

public class GuiCodeEditor extends GuiScreen {

    private final TileEntityManager manager;
    private final GuiScreen parentScreen;

    private final List<String> lines = new ArrayList<>();
    private int cursorLine = 0;
    private int cursorCol = 0;

    private int scrollLineOffset = 0;
    private int scrollColOffset = 0;

    // Layout
    private int editorX;
    private int editorY;
    private int editorW;
    private int editorH;
    private int gutterW = 0;
    private int scrollbarW = 10;
    private int lineHeight = 10;
    private int visibleLines;

    // Scrollbar state
    private boolean isDraggingVScroll = false;
    private int dragClickOffsetY = 0;

    // Syntax validation status
    private String syntaxStatus = "Ready";
    private boolean syntaxValid = true;

    // Buttons
    private GuiButton btnConfig;
    private GuiButton btnSave;
    private GuiButton btnCancel;

    // Autocomplete & Syntax Snippet UI
    private final GuiAutocompletePopup popup = new GuiAutocompletePopup();
    private GuiButton btnTriggers;
    private GuiButton btnIO;
    private GuiButton btnLogic;
    private GuiButton btnLabels;
    private GuiButton btnModifiers;
    private GuiButton btnSuggest;

    public GuiCodeEditor(TileEntityManager manager, GuiScreen parentScreen) {
        this.manager = manager;
        this.parentScreen = parentScreen;

        // Load code from disk
        if (manager.getDisk() != null) {
            String code = ItemDisk.getProgramString(manager.getDisk());
            if (!code.isEmpty()) {
                lines.addAll(Arrays.asList(code.split("\n", -1)));
            }
        }
        if (lines.isEmpty()) {
            lines.add("");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        buttonList.clear();
        Keyboard.enableRepeatEvents(true);

        editorW = 400;
        editorH = 180;
        editorX = width / 2 - 200;
        editorY = height / 2 - 95;

        visibleLines = Math.max(1, (editorH - 8) / lineHeight);

        int btnY = height / 2 + 96;
        btnConfig = new GuiButton(4, width / 2 - 200, btnY, 16, 20, "#");
        btnSave = new GuiButton(0, width / 2 - 152, btnY, 200, 20, "Done");
        btnCancel = new GuiButton(1, width / 2 + 98, btnY, 100, 20, "Cancel");

        // Top Snippet Toolbar (aligned directly above editor)
        int barY = editorY - 22;
        int barX = editorX;
        btnTriggers = new GuiButton(101, barX, barY, 65, 18, "Triggers \u25BC");
        btnIO = new GuiButton(102, barX + 67, barY, 52, 18, "I/O \u25BC");
        btnLogic = new GuiButton(103, barX + 121, barY, 56, 18, "Logic \u25BC");
        btnLabels = new GuiButton(104, barX + 179, barY, 60, 18, "Labels \u25BC");
        btnModifiers = new GuiButton(105, barX + 241, barY, 68, 18, "Modifiers \u25BC");
        btnSuggest = new GuiButton(100, barX + 311, barY, 89, 18, "\u2728 Suggest...");

        buttonList.add(btnConfig);
        buttonList.add(btnSave);
        buttonList.add(btnCancel);

        buttonList.add(btnTriggers);
        buttonList.add(btnIO);
        buttonList.add(btnLogic);
        buttonList.add(btnLabels);
        buttonList.add(btnModifiers);
        buttonList.add(btnSuggest);

        validateSyntax();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    private void validateSyntax() {
        String code = String.join("\n", lines);
        if (code.trim()
            .isEmpty()) {
            syntaxStatus = EnumChatFormatting.GRAY + "Empty program";
            syntaxValid = true;
            return;
        }

        try {
            ProgramBuilder builder = new ProgramBuilder(code);
            ProgramBuildResult res = builder.build();
            if (res.isSuccess()) {
                syntaxStatus = EnumChatFormatting.GREEN + "Valid program: "
                    + res.getProgram()
                        .getName();
                syntaxValid = true;
            } else {
                syntaxStatus = EnumChatFormatting.RED + res.getErrorMessage();
                syntaxValid = false;
            }
        } catch (Exception e) {
            syntaxStatus = EnumChatFormatting.RED + "Error: " + e.getMessage();
            syntaxValid = false;
        }
    }

    private void saveAndClose() {
        String code = String.join("\n", lines);
        SFMNetwork.WRAPPER.sendToServer(
            new PacketSaveProgram(manager.xCoord, manager.yCoord, manager.zCoord, code, manager.isRunning()));
        manager.saveCodeAndRecompile(code);
        mc.displayGuiScreen(parentScreen);
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        if (btn.id == 0) {
            saveAndClose();
        } else if (btn.id == 1) {
            mc.displayGuiScreen(parentScreen);
        } else if (btn.id == 4) {
            setClipboardString(String.join("\n", lines));
            syntaxStatus = EnumChatFormatting.YELLOW + "Copied code to clipboard!";
        } else if (btn.id == 100) {
            openAutocompleteAtCursor();
        } else if (btn.id == 101) {
            popup.openAt(
                btn.xPosition,
                btn.yPosition + btn.height + 1,
                width,
                height,
                "Triggers",
                SyntaxRegistry.getSnippetsForCategory(SnippetCategory.TRIGGER));
        } else if (btn.id == 102) {
            popup.openAt(
                btn.xPosition,
                btn.yPosition + btn.height + 1,
                width,
                height,
                "I/O Transfer",
                SyntaxRegistry.getSnippetsForCategory(SnippetCategory.IO));
        } else if (btn.id == 103) {
            popup.openAt(
                btn.xPosition,
                btn.yPosition + btn.height + 1,
                width,
                height,
                "Logic & Conditions",
                SyntaxRegistry.getSnippetsForCategory(SnippetCategory.CONDITION));
        } else if (btn.id == 104) {
            popup.openAt(
                btn.xPosition,
                btn.yPosition + btn.height + 1,
                width,
                height,
                "Labels & Sides",
                getCombinedLabelSnippets());
        } else if (btn.id == 105) {
            popup.openAt(
                btn.xPosition,
                btn.yPosition + btn.height + 1,
                width,
                height,
                "Modifiers & Limits",
                SyntaxRegistry.getSnippetsForCategory(SnippetCategory.MODIFIER));
        }
    }

    public void openAutocompleteAtCursor() {
        String curLine = (cursorLine >= 0 && cursorLine < lines.size()) ? lines.get(cursorLine) : "";
        String beforeCursor = curLine.substring(0, Math.min(cursorCol, curLine.length()));
        int cursorPixelX = editorX + gutterW + 4 - scrollColOffset + fontRendererObj.getStringWidth(beforeCursor);
        int cursorPixelY = editorY + 3 + (cursorLine - scrollLineOffset) * lineHeight;

        List<SyntaxSnippet> suggestions = SyntaxRegistry
            .getContextualSuggestions(String.join("\n", lines), cursorLine, cursorCol, getActiveDiskLabels());
        popup.openAt(
            cursorPixelX,
            cursorPixelY + lineHeight + 2,
            width,
            height,
            "Suggestions (Ctrl+Space)",
            suggestions);
    }

    public Set<String> getActiveDiskLabels() {
        if (manager != null && manager.getDisk() != null) {
            return LabelPositionHolder.from(manager.getDisk())
                .getAllLabels()
                .keySet();
        }
        return Collections.emptySet();
    }

    public List<SyntaxSnippet> getCombinedLabelSnippets() {
        List<SyntaxSnippet> list = new ArrayList<>();
        Set<String> diskLabels = getActiveDiskLabels();
        list.addAll(SyntaxRegistry.getLabelSnippets(diskLabels));
        list.addAll(SyntaxRegistry.getSnippetsForCategory(SnippetCategory.LABEL));
        return list;
    }

    public void insertSnippet(SyntaxSnippet snippet) {
        if (snippet == null) return;
        String template = snippet.getTemplate();
        String curLine = lines.get(cursorLine);

        int tokenStart = cursorCol;
        int tokenEnd = cursorCol;

        // Check if cursor is on or inside a <placeholder>
        int lt = curLine.lastIndexOf('<', cursorCol);
        int gt = curLine.indexOf('>', cursorCol);
        if (lt != -1 && gt != -1 && gt > lt && (cursorCol >= lt && cursorCol <= gt + 1)) {
            tokenStart = lt;
            tokenEnd = gt + 1;
        } else {
            // Check if cursor is preceded by a word token (e.g. partial typing)
            while (tokenStart > 0 && Character.isLetterOrDigit(curLine.charAt(tokenStart - 1))) {
                tokenStart--;
            }
        }

        String before = curLine.substring(0, tokenStart);
        String after = curLine.substring(tokenEnd);

        // Determine base indentation of current line
        int indentSpaces = 0;
        while (indentSpaces < before.length() && before.charAt(indentSpaces) == ' ') {
            indentSpaces++;
        }
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < indentSpaces; i++) indent.append(' ');

        String[] tLines = template.split("\n", -1);
        if (tLines.length == 1) {
            lines.set(cursorLine, before + tLines[0] + after);
            cursorCol = before.length() + tLines[0].length();
        } else {
            lines.set(cursorLine, before + tLines[0]);
            for (int i = 1; i < tLines.length - 1; i++) {
                lines.add(cursorLine + i, indent.toString() + tLines[i]);
            }
            String lastLine = indent.toString() + tLines[tLines.length - 1];
            lines.add(cursorLine + tLines.length - 1, lastLine + afterCursorOrEmpty(after));
            cursorLine += tLines.length - 1;
            cursorCol = lastLine.length();
        }

        // Auto-navigate to first placeholder if present
        jumpToPlaceholder(true);

        validateSyntax();
        ensureCursorVisible();
    }

    private String afterCursorOrEmpty(String after) {
        return after != null ? after : "";
    }

    public boolean jumpToPlaceholder(boolean forward) {
        Pattern p = Pattern.compile("<([^>]+)>");
        if (forward) {
            // Search forward from current line and col
            for (int l = cursorLine; l < lines.size(); l++) {
                String s = lines.get(l);
                int startCol = (l == cursorLine) ? Math.min(cursorCol + 1, s.length()) : 0;
                if (startCol < s.length()) {
                    Matcher m = p.matcher(s);
                    if (m.find(startCol)) {
                        cursorLine = l;
                        cursorCol = m.start();
                        ensureCursorVisible();
                        return true;
                    }
                }
            }
            // Wrap around from top
            for (int l = 0; l <= cursorLine; l++) {
                String s = lines.get(l);
                Matcher m = p.matcher(s);
                if (m.find()) {
                    if (l < cursorLine || m.start() < cursorCol) {
                        cursorLine = l;
                        cursorCol = m.start();
                        ensureCursorVisible();
                        return true;
                    }
                }
            }
        } else {
            // Search backward
            for (int l = cursorLine; l >= 0; l--) {
                String s = lines.get(l);
                Matcher m = p.matcher(s);
                int bestStart = -1;
                while (m.find()) {
                    if (l < cursorLine || m.start() < cursorCol) {
                        bestStart = m.start();
                    }
                }
                if (bestStart != -1) {
                    cursorLine = l;
                    cursorCol = bestStart;
                    ensureCursorVisible();
                    return true;
                }
            }
        }
        return false;
    }

    public int countPlaceholders() {
        int count = 0;
        Pattern p = Pattern.compile("<([^>]+)>");
        for (String line : lines) {
            Matcher m = p.matcher(line);
            while (m.find()) count++;
        }
        return count;
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int dWheel = Mouse.getDWheel();
        if (dWheel != 0) {
            int mouseX = Mouse.getEventX() * width / mc.displayWidth;
            int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            if (popup.isVisible() && popup.contains(mouseX, mouseY)) {
                popup.handleMouseWheel(dWheel);
                return;
            }
            int linesToScroll = 3;
            if (dWheel > 0) {
                scrollLineOffset = Math.max(0, scrollLineOffset - linesToScroll);
            } else {
                scrollLineOffset = Math.min(Math.max(0, lines.size() - visibleLines), scrollLineOffset + linesToScroll);
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (popup.isVisible()) {
            if (popup.contains(mouseX, mouseY)) {
                SyntaxSnippet snippet = popup.mouseClicked(mouseX, mouseY, mouseButton);
                if (snippet != null) {
                    insertSnippet(snippet);
                }
                return;
            } else {
                popup.close();
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0) {
            int scrollbarX = editorX + editorW - scrollbarW;
            if (mouseX >= scrollbarX && mouseX <= scrollbarX + scrollbarW
                && mouseY >= editorY
                && mouseY <= editorY + editorH) {
                // Clicked on scrollbar track or thumb
                int totalLines = lines.size();
                if (totalLines > visibleLines) {
                    float thumbHeight = Math.max(16, (float) visibleLines / totalLines * editorH);
                    float maxThumbY = editorH - thumbHeight;
                    float currentThumbY = editorY
                        + ((float) scrollLineOffset / (totalLines - visibleLines)) * maxThumbY;

                    if (mouseY >= currentThumbY && mouseY <= currentThumbY + thumbHeight) {
                        isDraggingVScroll = true;
                        dragClickOffsetY = (int) (mouseY - currentThumbY);
                    } else {
                        // Clicked track, jump scroll
                        float clickRatio = (float) (mouseY - editorY) / editorH;
                        scrollLineOffset = Math.min(
                            Math.max(0, (int) (clickRatio * (totalLines - visibleLines))),
                            totalLines - visibleLines);
                        isDraggingVScroll = true;
                        dragClickOffsetY = (int) (thumbHeight / 2);
                    }
                }
                return;
            }

            // Clicked inside code editor area
            int textAreaX = editorX + gutterW;
            int textAreaW = editorW - gutterW - scrollbarW;
            if (mouseX >= textAreaX && mouseX <= textAreaX + textAreaW
                && mouseY >= editorY
                && mouseY <= editorY + editorH) {
                int clickedRow = (mouseY - editorY) / lineHeight + scrollLineOffset;
                if (clickedRow >= 0 && clickedRow < lines.size()) {
                    cursorLine = clickedRow;
                    String line = lines.get(cursorLine);
                    int relX = mouseX - textAreaX + scrollColOffset;
                    int col = 0;
                    int accW = 0;
                    for (int c = 0; c < line.length(); c++) {
                        int charW = fontRendererObj.getCharWidth(line.charAt(c));
                        if (accW + charW / 2 > relX) {
                            break;
                        }
                        accW += charW;
                        col++;
                    }
                    cursorCol = Math.min(col, line.length());
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
        if (isDraggingVScroll && lines.size() > visibleLines) {
            int totalLines = lines.size();
            float thumbHeight = Math.max(16, (float) visibleLines / totalLines * editorH);
            float maxThumbY = editorH - thumbHeight;
            float newThumbY = mouseY - editorY - dragClickOffsetY;
            float ratio = Math.max(0.0f, Math.min(1.0f, newThumbY / maxThumbY));
            scrollLineOffset = Math.round(ratio * (totalLines - visibleLines));
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (popup.isVisible()) {
            SyntaxSnippet snippet = popup.handleKeyInput(typedChar, keyCode);
            if (snippet != null) {
                insertSnippet(snippet);
                return;
            }
            if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_DOWN
                || keyCode == Keyboard.KEY_UP
                || keyCode == Keyboard.KEY_PRIOR
                || keyCode == Keyboard.KEY_NEXT
                || keyCode == Keyboard.KEY_RETURN
                || keyCode == Keyboard.KEY_NUMPADENTER
                || keyCode == Keyboard.KEY_TAB) {
                return;
            }
        }

        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parentScreen);
            return;
        }

        // Ctrl + Space: Contextual Autocomplete
        if (isCtrlKeyDown() && keyCode == Keyboard.KEY_SPACE) {
            openAutocompleteAtCursor();
            return;
        }

        // Shift + Enter: Save and Close
        if ((keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) && isShiftKeyDown()) {
            saveAndClose();
            return;
        }

        // Ctrl + S: Quick Save
        if (isCtrlKeyDown() && keyCode == Keyboard.KEY_S) {
            String code = String.join("\n", lines);
            SFMNetwork.WRAPPER.sendToServer(
                new PacketSaveProgram(manager.xCoord, manager.yCoord, manager.zCoord, code, manager.isRunning()));
            manager.saveCodeAndRecompile(code);
            validateSyntax();
            return;
        }

        // Ctrl + A: Select All (jump to end)
        if (isCtrlKeyDown() && keyCode == Keyboard.KEY_A) {
            cursorLine = lines.size() - 1;
            cursorCol = lines.get(cursorLine)
                .length();
            ensureCursorVisible();
            return;
        }

        // Ctrl + C: Copy all
        if (isCtrlKeyDown() && keyCode == Keyboard.KEY_C) {
            setClipboardString(String.join("\n", lines));
            syntaxStatus = EnumChatFormatting.YELLOW + "Copied code to clipboard!";
            return;
        }

        // Ctrl + V: Paste clipboard at cursor
        if (isCtrlKeyDown() && keyCode == Keyboard.KEY_V) {
            String clip = getClipboardString();
            if (clip != null && !clip.isEmpty()) {
                String[] clipLines = clip.replace("\r", "")
                    .split("\n", -1);
                String current = lines.get(cursorLine);
                String before = current.substring(0, cursorCol);
                String after = current.substring(cursorCol);

                if (clipLines.length == 1) {
                    lines.set(cursorLine, before + clipLines[0] + after);
                    cursorCol += clipLines[0].length();
                } else {
                    lines.set(cursorLine, before + clipLines[0]);
                    for (int i = 1; i < clipLines.length - 1; i++) {
                        lines.add(cursorLine + i, clipLines[i]);
                    }
                    lines.add(cursorLine + clipLines.length - 1, clipLines[clipLines.length - 1] + after);
                    cursorLine += clipLines.length - 1;
                    cursorCol = clipLines[clipLines.length - 1].length();
                }
                validateSyntax();
            }
            ensureCursorVisible();
            return;
        }

        switch (keyCode) {
            case Keyboard.KEY_UP:
                if (cursorLine > 0) {
                    cursorLine--;
                    cursorCol = Math.min(
                        cursorCol,
                        lines.get(cursorLine)
                            .length());
                }
                break;
            case Keyboard.KEY_DOWN:
                if (cursorLine < lines.size() - 1) {
                    cursorLine++;
                    cursorCol = Math.min(
                        cursorCol,
                        lines.get(cursorLine)
                            .length());
                }
                break;
            case Keyboard.KEY_LEFT:
                if (cursorCol > 0) {
                    cursorCol--;
                } else if (cursorLine > 0) {
                    cursorLine--;
                    cursorCol = lines.get(cursorLine)
                        .length();
                }
                break;
            case Keyboard.KEY_RIGHT:
                if (cursorCol < lines.get(cursorLine)
                    .length()) {
                    cursorCol++;
                } else if (cursorLine < lines.size() - 1) {
                    cursorLine++;
                    cursorCol = 0;
                }
                break;
            case Keyboard.KEY_HOME:
                cursorCol = 0;
                break;
            case Keyboard.KEY_END:
                cursorCol = lines.get(cursorLine)
                    .length();
                break;
            case Keyboard.KEY_PRIOR: // Page Up
                cursorLine = Math.max(0, cursorLine - visibleLines);
                cursorCol = Math.min(
                    cursorCol,
                    lines.get(cursorLine)
                        .length());
                break;
            case Keyboard.KEY_NEXT: // Page Down
                cursorLine = Math.min(lines.size() - 1, cursorLine + visibleLines);
                cursorCol = Math.min(
                    cursorCol,
                    lines.get(cursorLine)
                        .length());
                break;
            case Keyboard.KEY_RETURN:
            case Keyboard.KEY_NUMPADENTER:
                String cur = lines.get(cursorLine);
                String left = cur.substring(0, cursorCol);
                String right = cur.substring(cursorCol);

                // Preserve leading indent spaces
                int indentSpaces = 0;
                while (indentSpaces < left.length() && left.charAt(indentSpaces) == ' ') {
                    indentSpaces++;
                }
                StringBuilder indent = new StringBuilder();
                for (int i = 0; i < indentSpaces; i++) indent.append(' ');

                lines.set(cursorLine, left);
                cursorLine++;
                lines.add(cursorLine, indent.toString() + right);
                cursorCol = indentSpaces;
                validateSyntax();
                break;
            case Keyboard.KEY_BACK:
                if (cursorCol > 0) {
                    String s = lines.get(cursorLine);
                    lines.set(cursorLine, s.substring(0, cursorCol - 1) + s.substring(cursorCol));
                    cursorCol--;
                    validateSyntax();
                } else if (cursorLine > 0) {
                    String prev = lines.get(cursorLine - 1);
                    String now = lines.get(cursorLine);
                    cursorCol = prev.length();
                    lines.set(cursorLine - 1, prev + now);
                    lines.remove(cursorLine);
                    cursorLine--;
                    validateSyntax();
                }
                break;
            case Keyboard.KEY_DELETE:
                String s = lines.get(cursorLine);
                if (cursorCol < s.length()) {
                    lines.set(cursorLine, s.substring(0, cursorCol) + s.substring(cursorCol + 1));
                    validateSyntax();
                } else if (cursorLine < lines.size() - 1) {
                    lines.set(cursorLine, s + lines.get(cursorLine + 1));
                    lines.remove(cursorLine + 1);
                    validateSyntax();
                }
                break;
            case Keyboard.KEY_TAB:
                if (countPlaceholders() > 0) {
                    jumpToPlaceholder(!isShiftKeyDown());
                } else {
                    String curTab = lines.get(cursorLine);
                    lines.set(cursorLine, curTab.substring(0, cursorCol) + "    " + curTab.substring(cursorCol));
                    cursorCol += 4;
                    validateSyntax();
                }
                break;
            default:
                if (typedChar >= 32 && typedChar != 127) {
                    String curLine = lines.get(cursorLine);
                    lines.set(cursorLine, curLine.substring(0, cursorCol) + typedChar + curLine.substring(cursorCol));
                    cursorCol++;
                    validateSyntax();
                }
                break;
        }

        ensureCursorVisible();
    }

    private void ensureCursorVisible() {
        if (cursorLine < scrollLineOffset) {
            scrollLineOffset = cursorLine;
        } else if (cursorLine >= scrollLineOffset + visibleLines) {
            scrollLineOffset = cursorLine - visibleLines + 1;
        }

        // Horizontal scroll check
        if (cursorLine >= 0 && cursorLine < lines.size()) {
            String line = lines.get(cursorLine);
            String beforeCursor = line.substring(0, Math.min(cursorCol, line.length()));
            int cursorPixelX = fontRendererObj.getStringWidth(beforeCursor);
            int textAreaW = editorW - gutterW - scrollbarW - 10;

            if (cursorPixelX < scrollColOffset) {
                scrollColOffset = Math.max(0, cursorPixelX - 20);
            } else if (cursorPixelX > scrollColOffset + textAreaW) {
                scrollColOffset = cursorPixelX - textAreaW + 20;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        // 1. Editor Outer Border (1px crisp white border matching modern SFM)
        drawRect(editorX - 1, editorY - 1, editorX + editorW + 1, editorY + editorH + 1, 0xFFFFFFFF);

        // 2. Editor Background (Pure black matching modern SFM)
        drawRect(editorX, editorY, editorX + editorW, editorY + editorH, 0xFF000000);

        // 3. Gutter Background (Line numbers if enabled)
        if (gutterW > 0) {
            drawRect(editorX, editorY, editorX + gutterW, editorY + editorH, 0xFF252526);
        }

        int codeAreaX = editorX + gutterW;
        boolean hasScroll = lines.size() > visibleLines;
        int codeAreaW = editorW - gutterW - (hasScroll ? scrollbarW : 0);

        // 4. Scrollbar Track
        int scrollbarX = editorX + editorW - scrollbarW;
        if (hasScroll) {
            drawRect(scrollbarX, editorY, scrollbarX + scrollbarW, editorY + editorH, 0xFF2D2D30);
        }

        // Render code lines
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(
            mc,
            mc.displayWidth,
            mc.displayHeight);
        int sf = sr.getScaleFactor();
        GL11.glScissor(codeAreaX * sf, (height - (editorY + editorH)) * sf, codeAreaW * sf, editorH * sf);

        for (int i = 0; i < visibleLines; i++) {
            int lineIdx = scrollLineOffset + i;
            if (lineIdx >= lines.size()) break;

            int y = editorY + 3 + i * lineHeight;
            String line = lines.get(lineIdx);

            // Active line highlight
            if (lineIdx == cursorLine) {
                drawRect(codeAreaX, y - 1, codeAreaX + codeAreaW, y + lineHeight - 1, 0xFF181818);
            }

            // Syntax highlighted code
            renderSyntaxLine(line, codeAreaX + 4 - scrollColOffset, y);

            // Cursor rendering (white underscore matching Minecraft text inputs)
            if (lineIdx == cursorLine && (System.currentTimeMillis() / 450) % 2 == 0) {
                int cursorPixelX = codeAreaX + 4
                    - scrollColOffset
                    + fontRendererObj.getStringWidth(line.substring(0, Math.min(cursorCol, line.length())));
                drawRect(cursorPixelX, y + lineHeight - 2, cursorPixelX + 5, y + lineHeight - 1, 0xFFFFFFFF);
            }
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Gutter line numbers if enabled
        if (gutterW > 0) {
            for (int i = 0; i < visibleLines; i++) {
                int lineIdx = scrollLineOffset + i;
                if (lineIdx >= lines.size()) break;

                int y = editorY + 3 + i * lineHeight;
                String numStr = String.valueOf(lineIdx + 1);
                int numW = fontRendererObj.getStringWidth(numStr);
                int color = lineIdx == cursorLine ? 0xFFFFFF : 0x707070;
                fontRendererObj.drawString(numStr, editorX + gutterW - numW - 4, y, color);
            }
        }

        // 5. Scrollbar Thumb
        int totalLines = lines.size();
        if (hasScroll) {
            float thumbHeight = Math.max(16, (float) visibleLines / totalLines * editorH);
            float maxThumbY = editorH - thumbHeight;
            float thumbY = editorY + ((float) scrollLineOffset / (totalLines - visibleLines)) * maxThumbY;
            int thumbColor = isDraggingVScroll ? 0xFF7A7A7E : 0xFF505054;
            drawRect(
                scrollbarX + 1,
                (int) thumbY,
                scrollbarX + scrollbarW - 1,
                (int) (thumbY + thumbHeight),
                thumbColor);
        }

        // 6. Title and Quick Shortcuts bar (above toolbar)
        fontRendererObj.drawStringWithShadow("Super Factory Manager - Code Editor", editorX, editorY - 34, 0x00E0FF);
        String shortcutHelp = EnumChatFormatting.GRAY + "Autocomplete: "
            + EnumChatFormatting.AQUA
            + "Ctrl+Space"
            + EnumChatFormatting.GRAY
            + " | Next: "
            + EnumChatFormatting.AQUA
            + "Tab";
        fontRendererObj.drawStringWithShadow(
            shortcutHelp,
            editorX + editorW - fontRendererObj.getStringWidth(shortcutHelp),
            editorY - 34,
            0xFFFFFF);

        // 7. Status Line (between editor and bottom buttons)
        int statusY = editorY + editorH + 4;
        int phCount = countPlaceholders();
        int phWidth = 0;
        if (phCount > 0) {
            String phText = EnumChatFormatting.GOLD.toString() + phCount
                + EnumChatFormatting.YELLOW
                + (phCount == 1 ? " placeholder" : " placeholders")
                + " remaining (\u21E5 Tab)";
            phWidth = fontRendererObj.getStringWidth(phText) + 8;
            fontRendererObj
                .drawString(phText, editorX + editorW - fontRendererObj.getStringWidth(phText) - 2, statusY, 0xFFFFFF);
        }

        int maxStatusW = editorW - phWidth - 6;
        String trimmedStatus = fontRendererObj.trimStringToWidth(syntaxStatus, maxStatusW);
        fontRendererObj.drawString(trimmedStatus, editorX + 2, statusY, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);

        // 8. Autocomplete Popup on top of everything
        popup.draw(fontRendererObj, mouseX, mouseY);
    }

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
        "(<[^>]+>)|(\"[^\"]*\")|(\\b(EVERY|TICKS|REDSTONE|LEVEL|LEVELS|PULSE|INPUT|OUTPUT|FORGET|IF|FROM|TO|EACH|ROUND_ROBIN|WITH|TRUE|FALSE|AND|OR|NOT|NAME)\\b)",
        Pattern.CASE_INSENSITIVE);

    private void renderSyntaxLine(String line, int x, int y) {
        if (line.isEmpty()) return;

        // Check for comment
        int commentIdx = line.indexOf("//");
        if (commentIdx == -1) commentIdx = line.indexOf("#");

        String codePart = commentIdx != -1 ? line.substring(0, commentIdx) : line;
        String commentPart = commentIdx != -1 ? line.substring(commentIdx) : null;

        // Render code part with token highlighting
        int curX = x;
        int lastIndex = 0;
        Matcher m = TOKEN_PATTERN.matcher(codePart);
        while (m.find()) {
            // Text before match
            if (m.start() > lastIndex) {
                String sub = codePart.substring(lastIndex, m.start());
                fontRendererObj.drawString(sub, curX, y, 0xE0E0E0);
                curX += fontRendererObj.getStringWidth(sub);
            }
            String match = m.group();
            if (m.group(1) != null) {
                // <placeholder> -> warm gold/amber
                fontRendererObj.drawString(match, curX, y, 0xE5C07B);
            } else if (m.group(2) != null) {
                // "string literal" -> green
                fontRendererObj.drawString(match, curX, y, 0x98C379);
            } else if (m.group(3) != null) {
                // Keyword
                int kwColor = 0x569CD6; // VS Code style keyword blue
                String upper = match.toUpperCase();
                if (upper.equals("INPUT")) kwColor = 0x4EC9B0; // Cyan
                else if (upper.equals("OUTPUT")) kwColor = 0xCE9178; // Orange/Coral
                else if (upper.equals("EVERY") || upper.equals("TICKS")
                    || upper.equals("REDSTONE")
                    || upper.equals("LEVEL")
                    || upper.equals("LEVELS")
                    || upper.equals("PULSE")) kwColor = 0xDCDCAA; // Yellow
                else if (upper.equals("IF") || upper.equals("TRUE") || upper.equals("FALSE")) kwColor = 0xC586C0; // Purple

                fontRendererObj.drawString(match, curX, y, kwColor);
            }
            curX += fontRendererObj.getStringWidth(match);
            lastIndex = m.end();
        }
        if (lastIndex < codePart.length()) {
            String rest = codePart.substring(lastIndex);
            fontRendererObj.drawString(rest, curX, y, 0xE0E0E0);
            curX += fontRendererObj.getStringWidth(rest);
        }

        // Render comment part in green/gray
        if (commentPart != null) {
            fontRendererObj.drawString(commentPart, curX, y, 0x6A9955);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
