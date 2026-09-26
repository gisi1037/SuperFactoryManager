package ca.teamdman.sfm.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import ca.teamdman.sfm.common.item.ItemDisk;
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
    private int gutterW = 32;
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
    private GuiButton btnSave;
    private GuiButton btnCancel;
    private GuiButton btnCheck;
    private GuiButton btnCopy;

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

        int marginX = 20;
        int topMargin = 32;
        int bottomMargin = 36;

        editorX = marginX;
        editorY = topMargin;
        editorW = width - marginX * 2;
        editorH = height - topMargin - bottomMargin;

        visibleLines = Math.max(1, (editorH - 4) / lineHeight);

        int btnY = height - bottomMargin + 8;
        btnSave = new GuiButton(0, editorX, btnY, 100, 20, EnumChatFormatting.GREEN + "Save & Done");
        btnCancel = new GuiButton(1, editorX + 110, btnY, 80, 20, "Cancel");
        btnCheck = new GuiButton(2, editorX + 200, btnY, 95, 20, "Check Syntax");
        btnCopy = new GuiButton(3, editorX + 305, btnY, 75, 20, "Copy All");

        buttonList.add(btnSave);
        buttonList.add(btnCancel);
        buttonList.add(btnCheck);
        buttonList.add(btnCopy);

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
        } else if (btn.id == 2) {
            validateSyntax();
        } else if (btn.id == 3) {
            setClipboardString(String.join("\n", lines));
            syntaxStatus = EnumChatFormatting.YELLOW + "Copied code to clipboard!";
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int dWheel = Mouse.getDWheel();
        if (dWheel != 0) {
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
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parentScreen);
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
                String curTab = lines.get(cursorLine);
                lines.set(cursorLine, curTab.substring(0, cursorCol) + "    " + curTab.substring(cursorCol));
                cursorCol += 4;
                validateSyntax();
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

        // 1. Header
        drawString(
            fontRendererObj,
            EnumChatFormatting.BOLD + "Super Factory Manager - Code Editor",
            editorX,
            10,
            0x55FFFF);
        String shortcutTip = EnumChatFormatting.DARK_GRAY + "Shift+Enter / Ctrl+S to save | Esc to cancel";
        drawString(
            fontRendererObj,
            shortcutTip,
            editorX + editorW - fontRendererObj.getStringWidth(shortcutTip),
            10,
            0x888888);

        // Status bar above editor
        drawString(fontRendererObj, fontRendererObj.trimStringToWidth(syntaxStatus, editorW - 120), editorX, 22, 0xFFFFFF);
        String posInfo = "Ln " + (cursorLine + 1) + ", Col " + (cursorCol + 1) + " | " + lines.size() + " lines";
        drawString(
            fontRendererObj,
            posInfo,
            editorX + editorW - fontRendererObj.getStringWidth(posInfo),
            22,
            0xAAAAAA);

        // 2. Editor Outer Border
        drawRect(editorX - 1, editorY - 1, editorX + editorW + 1, editorY + editorH + 1, 0xFF444444);

        // 3. Gutter Background (Line numbers)
        drawRect(editorX, editorY, editorX + gutterW, editorY + editorH, 0xFF252526);

        // 4. Code Background (Dark IDE style)
        int codeAreaX = editorX + gutterW;
        int codeAreaW = editorW - gutterW - scrollbarW;
        drawRect(codeAreaX, editorY, codeAreaX + codeAreaW, editorY + editorH, 0xFF1E1E1E);

        // 5. Scrollbar Track
        int scrollbarX = editorX + editorW - scrollbarW;
        drawRect(scrollbarX, editorY, scrollbarX + scrollbarW, editorY + editorH, 0xFF2D2D30);

        // Render code lines & gutter
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        // Scissor box for code view (convert GUI coords to display coords)
        int scale = mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale;
        // In 1.7.10 Minecraft, ScaledResolution calculates factor
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
                drawRect(codeAreaX, y - 1, codeAreaX + codeAreaW, y + lineHeight - 1, 0xFF282828);
            }

            // Syntax highlighted code
            renderSyntaxLine(line, codeAreaX + 4 - scrollColOffset, y);

            // Cursor rendering
            if (lineIdx == cursorLine && (System.currentTimeMillis() / 450) % 2 == 0) {
                int cursorPixelX = codeAreaX + 4
                    - scrollColOffset
                    + fontRendererObj.getStringWidth(line.substring(0, Math.min(cursorCol, line.length())));
                drawRect(cursorPixelX, y - 1, cursorPixelX + 1, y + lineHeight - 1, 0xFF00FF00);
            }
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Gutter line numbers
        for (int i = 0; i < visibleLines; i++) {
            int lineIdx = scrollLineOffset + i;
            if (lineIdx >= lines.size()) break;

            int y = editorY + 3 + i * lineHeight;
            String numStr = String.valueOf(lineIdx + 1);
            int numW = fontRendererObj.getStringWidth(numStr);
            int color = lineIdx == cursorLine ? 0xFFFFFF : 0x707070;
            fontRendererObj.drawString(numStr, editorX + gutterW - numW - 4, y, color);
        }

        // 6. Scrollbar Thumb
        int totalLines = lines.size();
        if (totalLines > visibleLines) {
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


        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private static final Pattern KEYWORD_PATTERN = Pattern.compile(
        "\\b(EVERY|TICKS|REDSTONE|PULSE|INPUT|OUTPUT|FORGET|IF|FROM|TO|EACH|ROUND_ROBIN|WITH|TRUE|FALSE|AND|OR|NOT|NAME)\\b",
        Pattern.CASE_INSENSITIVE);

    private void renderSyntaxLine(String line, int x, int y) {
        if (line.isEmpty()) return;

        // Check for comment
        int commentIdx = line.indexOf("//");
        if (commentIdx == -1) commentIdx = line.indexOf("#");

        String codePart = commentIdx != -1 ? line.substring(0, commentIdx) : line;
        String commentPart = commentIdx != -1 ? line.substring(commentIdx) : null;

        // Render code part with simple token highlighting
        int curX = x;
        int lastIndex = 0;
        Matcher m = KEYWORD_PATTERN.matcher(codePart);
        while (m.find()) {
            // Text before match
            if (m.start() > lastIndex) {
                String sub = codePart.substring(lastIndex, m.start());
                fontRendererObj.drawString(sub, curX, y, 0xE0E0E0);
                curX += fontRendererObj.getStringWidth(sub);
            }
            // Keyword
            String kw = m.group();
            int kwColor = 0x569CD6; // VS Code style keyword blue
            String upper = kw.toUpperCase();
            if (upper.equals("INPUT")) kwColor = 0x4EC9B0; // Cyan
            else if (upper.equals("OUTPUT")) kwColor = 0xCE9178; // Orange/Coral
            else if (upper.equals("EVERY") || upper.equals("TICKS")
                || upper.equals("REDSTONE")
                || upper.equals("PULSE")) kwColor = 0xDCDCAA; // Yellow
            else if (upper.equals("IF") || upper.equals("TRUE") || upper.equals("FALSE")) kwColor = 0xC586C0; // Purple

            fontRendererObj.drawString(kw, curX, y, kwColor);
            curX += fontRendererObj.getStringWidth(kw);
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
}
