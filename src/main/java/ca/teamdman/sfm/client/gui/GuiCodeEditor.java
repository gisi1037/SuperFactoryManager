package ca.teamdman.sfm.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter;
import ca.teamdman.sfm.client.gui.syntax.SfmlSyntaxRenderer;
import ca.teamdman.sfm.client.gui.syntax.SnippetCategory;
import ca.teamdman.sfm.client.gui.syntax.SyntaxRegistry;
import ca.teamdman.sfm.client.gui.syntax.SyntaxSnippet;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

public class GuiCodeEditor extends GuiScreen implements ProgramEditSession.Receiver {

    private final TileEntityManager manager;
    private final GuiScreen parentScreen;

    private final List<String> lines = new ArrayList<>();
    private int cursorLine = 0;
    private int cursorCol = 0;

    private int scrollLineOffset = 0;
    private int scrollColOffset = 0;

    // Session persisted state
    private static int lastEditorW = 400;
    private static int lastEditorH = 180;
    private static int lastEditorX = -1;
    private static int lastEditorY = -1;
    private static boolean savedWordWrap = false;

    // Corner Resizing state
    private static final int CORNER_NONE = 0;
    private static final int CORNER_NW = 1;
    private static final int CORNER_NE = 2;
    private static final int CORNER_SW = 3;
    private static final int CORNER_SE = 4;

    private int resizingCorner = CORNER_NONE;
    private int dragStartX, dragStartY;
    private int origEditorX, origEditorY, origEditorW, origEditorH;

    // Word Wrap & Horizontal Scrolling
    private boolean wordWrap = savedWordWrap;
    private boolean isDraggingHScroll = false;
    private int dragClickOffsetX = 0;
    private int scrollbarH = 8;
    private GuiButton btnWrap;

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

    // Visual row wrapping model
    public static class VisualRow {

        public final int physicalLine;
        public final int startChar;
        public final int endChar;
        public final String text;

        public VisualRow(int physicalLine, int startChar, int endChar, String text) {
            this.physicalLine = physicalLine;
            this.startChar = startChar;
            this.endChar = endChar;
            this.text = text;
        }
    }

    private final List<VisualRow> visualRows = new ArrayList<>();

    // Syntax validation status
    private SfmlHighlighter.Highlighted highlighted = SfmlHighlighter.highlight("");
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
        this(manager, parentScreen, null);
    }

    /** An imported draft is displayed only after OPEN supplies an authoritative save token. */
    public GuiCodeEditor(TileEntityManager manager, GuiScreen parentScreen, String initialDraft) {
        this.initialDraft = initialDraft;
        this.manager = manager;
        this.parentScreen = parentScreen;

        editSession = new ProgramEditSession(
            manager,
            net.minecraft.client.Minecraft.getMinecraft().thePlayer.openContainer.windowId);
        lines.add("");
        syntaxStatus = "Loading program from server";
    }

    private final ProgramEditSession editSession;
    private final String initialDraft;
    private boolean openRequested, closeAfterSave, saveUnconfirmed;

    @Override
    public void receiveProgramEditReply(ca.teamdman.sfm.common.net.PacketProgramEditReply reply) {
        ProgramEditState.Action action = editSession.state.action();
        if (!editSession.receive(reply)) return;
        if (action == ProgramEditState.Action.SAVE) saveUnconfirmed = false;
        if (!reply.accepted || reply.snapshot == null) {
            syntaxStatus = reply.message;
            return;
        }
        if (action == ProgramEditState.Action.OPEN) {
            lines.clear();
            lines.addAll(
                Arrays.asList(
                    (initialDraft != null ? initialDraft
                        : ca.teamdman.sfm.client.blueprint.BlueprintDocument
                            .withoutMetadata(reply.snapshot.getSource())).split("\n", -1)));
            rebuildVisualRows();
            validateSyntax();
        } else {
            syntaxStatus = "Saved";
            if (closeAfterSave) mc.displayGuiScreen(parentScreen);
        }
    }

    @Override
    public void updateScreen() {
        if (!openRequested) {
            openRequested = true;
            editSession.open();
        }
        if (editSession.state.tick()) syntaxStatus = editSession.state.action() == ProgramEditState.Action.SAVE
            ? "No server confirmation; the save may have applied. Copy your draft before leaving."
            : "Program could not be loaded. Reopen to retry; an imported draft can still be copied.";
        if (btnSave != null) btnSave.enabled = editSession.state.isEditable();
        if (btnCancel != null) btnCancel.enabled = !editSession.state.isPending()
            || editSession.state.action() != ProgramEditState.Action.SAVE;
    }

    private boolean isVScrollNeeded() {
        if (visibleLines <= 0) return false;
        return visualRows.isEmpty() ? lines.size() > visibleLines : visualRows.size() > visibleLines;
    }

    private void rebuildVisualRows() {
        visualRows.clear();
        int hasVScrollEst = (lines.size() > visibleLines) ? scrollbarW : 0;
        int wrapWidth = Math.max(50, editorW - gutterW - hasVScrollEst - 8);

        for (int pLine = 0; pLine < lines.size(); pLine++) {
            String line = lines.get(pLine);
            if (!wordWrap) {
                visualRows.add(new VisualRow(pLine, 0, line.length(), line));
            } else {
                if (line.isEmpty()) {
                    visualRows.add(new VisualRow(pLine, 0, 0, ""));
                } else {
                    int start = 0;
                    while (start < line.length()) {
                        int count = fontRendererObj.trimStringToWidth(line.substring(start), wrapWidth)
                            .length();
                        if (count <= 0) count = 1;
                        int end = start + count;
                        if (end < line.length()) {
                            int lastSpace = line.lastIndexOf(' ', end - 1);
                            if (lastSpace >= start + 1) {
                                end = lastSpace + 1;
                            }
                        }
                        visualRows.add(new VisualRow(pLine, start, end, line.substring(start, end)));
                        start = end;
                    }
                }
            }
        }
    }

    private int getCursorVisualRow() {
        int lastMatchingRow = 0;
        for (int r = 0; r < visualRows.size(); r++) {
            VisualRow vr = visualRows.get(r);
            if (vr.physicalLine == cursorLine) {
                lastMatchingRow = r;
                if (cursorCol >= vr.startChar && cursorCol < vr.endChar) {
                    return r;
                }
            }
        }
        return lastMatchingRow;
    }

    public void updateLayout() {
        int minW = 320;
        int minH = 120;
        editorW = Math.max(minW, Math.min(width - 20, editorW));
        editorH = Math.max(minH, Math.min(height - 50, editorH));
        editorX = Math.max(10, Math.min(width - editorW - 10, editorX));
        editorY = Math.max(36, Math.min(height - editorH - 45, editorY));

        lastEditorW = editorW;
        lastEditorH = editorH;
        lastEditorX = editorX;
        lastEditorY = editorY;

        int maxLineW = 0;
        for (String l : lines) {
            int w = fontRendererObj.getStringWidth(l);
            if (w > maxLineW) maxLineW = w;
        }

        int codeAreaW = editorW - gutterW - (lines.size() > visibleLines ? scrollbarW : 0);
        boolean hasHScroll = !wordWrap && (maxLineW + 16 > codeAreaW);
        int availH = editorH - (hasHScroll ? scrollbarH : 0);
        visibleLines = Math.max(1, (availH - 8) / lineHeight);

        rebuildVisualRows();

        // Reposition Top Toolbar
        int barY = editorY - 22;
        int barX = editorX;
        if (editorW < 380) {
            btnTriggers.width = 54;
            btnTriggers.displayString = "Trig \u25BC";
            btnIO.width = 44;
            btnLogic.width = 46;
            btnLabels.width = 50;
            btnModifiers.width = 54;
            btnModifiers.displayString = "Mod \u25BC";
            btnSuggest.width = 68;
            btnSuggest.displayString = "\u2728 Suggest";
        } else {
            btnTriggers.width = 65;
            btnTriggers.displayString = "Triggers \u25BC";
            btnIO.width = 52;
            btnLogic.width = 56;
            btnLabels.width = 60;
            btnModifiers.width = 68;
            btnModifiers.displayString = "Modifiers \u25BC";
            btnSuggest.width = 89;
            btnSuggest.displayString = "\u2728 Suggest...";
        }

        btnTriggers.xPosition = barX;
        btnTriggers.yPosition = barY;
        btnIO.xPosition = barX + btnTriggers.width + 2;
        btnIO.yPosition = barY;
        btnLogic.xPosition = btnIO.xPosition + btnIO.width + 2;
        btnLogic.yPosition = barY;
        btnLabels.xPosition = btnLogic.xPosition + btnLogic.width + 2;
        btnLabels.yPosition = barY;
        btnModifiers.xPosition = btnLabels.xPosition + btnLabels.width + 2;
        btnModifiers.yPosition = barY;

        if (editorW >= 400) {
            btnSuggest.xPosition = editorX + editorW - btnSuggest.width;
        } else {
            btnSuggest.xPosition = btnModifiers.xPosition + btnModifiers.width + 2;
        }
        btnSuggest.yPosition = barY;

        // Reposition Bottom Buttons
        int btnY = editorY + editorH + 16;
        btnConfig.xPosition = editorX;
        btnConfig.yPosition = btnY;
        btnConfig.width = 16;

        btnWrap.xPosition = editorX + 20;
        btnWrap.yPosition = btnY;
        btnWrap.width = 64;
        btnWrap.displayString = wordWrap ? "Wrap: ON" : "Wrap: OFF";

        btnCancel.width = 65;
        btnCancel.xPosition = editorX + editorW - btnCancel.width;
        btnCancel.yPosition = btnY;

        int saveX = editorX + 88;
        int saveW = (btnCancel.xPosition - 4) - saveX;
        btnSave.xPosition = saveX;
        btnSave.yPosition = btnY;
        btnSave.width = Math.max(50, saveW);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        buttonList.clear();
        Keyboard.enableRepeatEvents(true);

        editorW = lastEditorW;
        editorH = lastEditorH;
        if (lastEditorX != -1 && lastEditorY != -1) {
            editorX = lastEditorX;
            editorY = lastEditorY;
        } else {
            editorX = width / 2 - editorW / 2;
            editorY = height / 2 - editorH / 2 - 5;
        }

        int btnY = editorY + editorH + 16;
        btnConfig = new GuiButton(4, editorX, btnY, 16, 20, "#");
        btnWrap = new GuiButton(5, editorX + 20, btnY, 64, 20, wordWrap ? "Wrap: ON" : "Wrap: OFF");
        btnSave = new GuiButton(0, editorX + 88, btnY, 100, 20, "Done");
        btnCancel = new GuiButton(1, editorX + editorW - 65, btnY, 65, 20, "Cancel");

        int barY = editorY - 22;
        int barX = editorX;
        btnTriggers = new GuiButton(101, barX, barY, 65, 18, "Triggers \u25BC");
        btnIO = new GuiButton(102, barX + 67, barY, 52, 18, "I/O \u25BC");
        btnLogic = new GuiButton(103, barX + 121, barY, 56, 18, "Logic \u25BC");
        btnLabels = new GuiButton(104, barX + 179, barY, 60, 18, "Labels \u25BC");
        btnModifiers = new GuiButton(105, barX + 241, barY, 68, 18, "Modifiers \u25BC");
        btnSuggest = new GuiButton(100, barX + 311, barY, 89, 18, "\u2728 Suggest...");

        buttonList.add(btnConfig);
        buttonList.add(btnWrap);
        buttonList.add(btnSave);
        buttonList.add(btnCancel);

        buttonList.add(btnTriggers);
        buttonList.add(btnIO);
        buttonList.add(btnLogic);
        buttonList.add(btnLabels);
        buttonList.add(btnModifiers);
        buttonList.add(btnSuggest);

        updateLayout();
        if (editSession.state.snapshot() != null) validateSyntax();
        btnSave.enabled = editSession.state.isEditable();
    }

    @Override
    public void onGuiClosed() {
        editSession.state.cancel();
        Keyboard.enableRepeatEvents(false);
    }

    private void validateSyntax() {
        String code = String.join("\n", lines);
        highlighted = SfmlHighlighter.highlight(code);
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
        requestSave(true);
    }

    private void requestSave(boolean close) {
        if (!editSession.state.isEditable()) return;
        closeAfterSave = close;
        try {
            editSession.save(String.join("\n", lines));
            saveUnconfirmed = true;
            syntaxStatus = "Saving - waiting for the server";
        } catch (IllegalArgumentException e) {
            editSession.state.cancel();
            syntaxStatus = e.getMessage();
        }
    }

    private void leaveEditor() {
        if (saveUnconfirmed) mc.thePlayer.addChatMessage(
            new net.minecraft.util.ChatComponentText(
                "[SFM] Save confirmation timed out; the server may have saved the program. Check the disk before retrying."));
        mc.displayGuiScreen(parentScreen);
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        if (btn.id == 1 && editSession.state.isPending() && editSession.state.action() == ProgramEditState.Action.SAVE)
            return;
        boolean canCopy = btn.id == 4 && (editSession.state.snapshot() != null || initialDraft != null);
        if (btn.id != 1 && !canCopy && !editSession.state.isEditable()) return;
        if (btn.id == 0) {
            saveAndClose();
        } else if (btn.id == 1) {
            leaveEditor();
        } else if (btn.id == 4) {
            setClipboardString(
                editSession.state.snapshot() == null && initialDraft != null ? initialDraft : String.join("\n", lines));
            syntaxStatus = EnumChatFormatting.YELLOW + "Copied code to clipboard!";
        } else if (btn.id == 5) {
            // Toggle word wrap
            wordWrap = !wordWrap;
            savedWordWrap = wordWrap;
            scrollColOffset = 0;
            updateLayout();
            ensureCursorVisible();
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
        int cursorRow = getCursorVisualRow();
        VisualRow vr = (cursorRow >= 0 && cursorRow < visualRows.size()) ? visualRows.get(cursorRow) : null;
        String curRowText = vr != null ? vr.text : "";
        int colInRow = vr != null ? Math.max(0, Math.min(cursorCol - vr.startChar, curRowText.length())) : 0;
        String beforeCursor = curRowText.substring(0, colInRow);

        int codeAreaX = editorX + gutterW;
        int cursorPixelX = codeAreaX + 4 - scrollColOffset + fontRendererObj.getStringWidth(beforeCursor);
        int cursorPixelY = editorY + 3 + (cursorRow - scrollLineOffset) * lineHeight;

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

    public static int getLeadingSpaces(String s) {
        if (s == null) return 0;
        int count = 0;
        while (count < s.length() && s.charAt(count) == ' ') {
            count++;
        }
        return count;
    }

    public static String spaces(int count) {
        if (count <= 0) return "";
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) sb.append(' ');
        return sb.toString();
    }

    public static String stripComment(String s) {
        if (s == null) return "";
        int cIdx = s.indexOf("//");
        if (cIdx == -1) cIdx = s.indexOf("#");
        return cIdx != -1 ? s.substring(0, cIdx) : s;
    }

    public static boolean isAllSpaces(String s) {
        if (s == null || s.isEmpty()) return true;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != ' ') return false;
        }
        return true;
    }

    public static int getBlockDepthAtLine(List<String> lines, int targetLine) {
        int depth = 0;
        for (int i = 0; i < targetLine; i++) {
            if (i >= lines.size()) break;
            String raw = lines.get(i);
            String code = stripComment(raw).trim();
            if (code.isEmpty()) continue;
            String upper = code.toUpperCase(Locale.ROOT);

            if (upper.equals("END") || upper.startsWith("END ")) {
                depth = Math.max(0, depth - 1);
            } else if (upper.equals("ELSE") || upper.startsWith("ELSE ") || upper.startsWith("ELSEIF ")) {
                // ELSE / ELSE IF continues current block depth, does not increase depth
            } else if (upper.endsWith(" DO") || upper.equals("DO") || upper.endsWith(" THEN") || upper.equals("THEN")) {
                depth++;
            }
        }
        return depth;
    }

    public static int getExpectedIndentForLineText(int blockDepth, String lineText) {
        String code = stripComment(lineText).trim();
        String upper = code.toUpperCase(Locale.ROOT);
        if (upper.equals("END") || upper.startsWith("END ")
            || upper.equals("ELSE")
            || upper.startsWith("ELSE ")
            || upper.startsWith("ELSEIF ")) {
            return Math.max(0, blockDepth - 1) * 2;
        }
        return blockDepth * 2;
    }

    public static int getExpectedIndent(List<String> lines, int targetLine) {
        int depth = getBlockDepthAtLine(lines, targetLine);
        String lineText = (targetLine >= 0 && targetLine < lines.size()) ? lines.get(targetLine) : "";
        return getExpectedIndentForLineText(depth, lineText);
    }

    public void autoIndentAll() {
        int oldCursorLine = cursorLine;
        int oldCursorCol = cursorCol;
        int oldIndent = (oldCursorLine >= 0 && oldCursorLine < lines.size())
            ? getLeadingSpaces(lines.get(oldCursorLine))
            : 0;

        for (int i = 0; i < lines.size(); i++) {
            int expected = getExpectedIndent(lines, i);
            String raw = lines.get(i);
            int curSpaces = getLeadingSpaces(raw);
            String code = raw.substring(curSpaces);
            if (!code.isEmpty()) {
                lines.set(i, spaces(expected) + code);
            } else {
                lines.set(i, "");
            }
        }

        if (oldCursorLine >= 0 && oldCursorLine < lines.size()) {
            int newIndent = getLeadingSpaces(lines.get(oldCursorLine));
            cursorCol = Math.max(0, oldCursorCol + (newIndent - oldIndent));
        }

        validateSyntax();
        ensureCursorVisible();
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

            boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
            if (shift && !wordWrap) {
                // Horizontal scrolling
                int maxLineW = 0;
                for (String l : lines) {
                    int w = fontRendererObj.getStringWidth(l);
                    if (w > maxLineW) maxLineW = w;
                }
                int codeAreaW = editorW - gutterW - (visualRows.size() > visibleLines ? scrollbarW : 0);
                int maxColOffset = Math.max(0, (maxLineW + 20) - codeAreaW);
                if (dWheel > 0) {
                    scrollColOffset = Math.max(0, scrollColOffset - 20);
                } else {
                    scrollColOffset = Math.min(maxColOffset, scrollColOffset + 20);
                }
            } else {
                int linesToScroll = 3;
                int maxOffset = Math.max(0, visualRows.size() - visibleLines);
                if (dWheel > 0) {
                    scrollLineOffset = Math.max(0, scrollLineOffset - linesToScroll);
                } else {
                    scrollLineOffset = Math.min(maxOffset, scrollLineOffset + linesToScroll);
                }
            }
        }
    }

    private int getHoveredCorner(int mouseX, int mouseY) {
        int radius = 7;
        if (Math.abs(mouseX - editorX) <= radius && Math.abs(mouseY - editorY) <= radius) {
            return CORNER_NW;
        }
        if (Math.abs(mouseX - (editorX + editorW)) <= radius && Math.abs(mouseY - editorY) <= radius) {
            return CORNER_NE;
        }
        if (Math.abs(mouseX - editorX) <= radius && Math.abs(mouseY - (editorY + editorH)) <= radius) {
            return CORNER_SW;
        }
        if (Math.abs(mouseX - (editorX + editorW)) <= radius && Math.abs(mouseY - (editorY + editorH)) <= radius) {
            return CORNER_SE;
        }
        return CORNER_NONE;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!editSession.state.isEditable()) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }
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
            // Check corner resize handles first
            int corner = getHoveredCorner(mouseX, mouseY);
            if (corner != CORNER_NONE) {
                resizingCorner = corner;
                dragStartX = mouseX;
                dragStartY = mouseY;
                origEditorX = editorX;
                origEditorY = editorY;
                origEditorW = editorW;
                origEditorH = editorH;
                return;
            }

            // Check horizontal scrollbar
            int maxLineW = 0;
            for (String l : lines) {
                int w = fontRendererObj.getStringWidth(l);
                if (w > maxLineW) maxLineW = w;
            }
            boolean hasVScroll = visualRows.size() > visibleLines;
            int codeAreaW = editorW - gutterW - (hasVScroll ? scrollbarW : 0);
            boolean hasHScroll = !wordWrap && (maxLineW + 16 > codeAreaW);
            int hScrollY = editorY + editorH - scrollbarH;
            int hScrollX = editorX + gutterW;

            if (hasHScroll && mouseY >= hScrollY
                && mouseY <= hScrollY + scrollbarH
                && mouseX >= hScrollX
                && mouseX <= hScrollX + codeAreaW) {
                int maxColOffset = Math.max(0, (maxLineW + 20) - codeAreaW);
                float viewRatio = (float) codeAreaW / (maxLineW + 20);
                float thumbW = Math.max(16, viewRatio * codeAreaW);
                float maxThumbX = codeAreaW - thumbW;
                float currentThumbX = hScrollX + ((float) scrollColOffset / maxColOffset) * maxThumbX;

                if (mouseX >= currentThumbX && mouseX <= currentThumbX + thumbW) {
                    isDraggingHScroll = true;
                    dragClickOffsetX = (int) (mouseX - currentThumbX);
                } else {
                    float clickRatio = (float) (mouseX - hScrollX) / codeAreaW;
                    scrollColOffset = Math.min(Math.max(0, (int) (clickRatio * maxColOffset)), maxColOffset);
                    isDraggingHScroll = true;
                    dragClickOffsetX = (int) (thumbW / 2);
                }
                return;
            }

            // Check vertical scrollbar
            int scrollbarX = editorX + editorW - scrollbarW;
            int availVScrollH = editorH - (hasHScroll ? scrollbarH : 0);
            if (hasVScroll && mouseX >= scrollbarX
                && mouseX <= scrollbarX + scrollbarW
                && mouseY >= editorY
                && mouseY <= editorY + availVScrollH) {
                int totalRows = visualRows.size();
                float thumbHeight = Math.max(16, (float) visibleLines / totalRows * availVScrollH);
                float maxThumbY = availVScrollH - thumbHeight;
                float currentThumbY = editorY + ((float) scrollLineOffset / (totalRows - visibleLines)) * maxThumbY;

                if (mouseY >= currentThumbY && mouseY <= currentThumbY + thumbHeight) {
                    isDraggingVScroll = true;
                    dragClickOffsetY = (int) (mouseY - currentThumbY);
                } else {
                    float clickRatio = (float) (mouseY - editorY) / availVScrollH;
                    scrollLineOffset = Math
                        .min(Math.max(0, (int) (clickRatio * (totalRows - visibleLines))), totalRows - visibleLines);
                    isDraggingVScroll = true;
                    dragClickOffsetY = (int) (thumbHeight / 2);
                }
                return;
            }

            // Clicked inside code editor area
            int textAreaX = editorX + gutterW;
            int textAreaW = codeAreaW;
            int textAreaH = availVScrollH;
            if (mouseX >= textAreaX && mouseX <= textAreaX + textAreaW
                && mouseY >= editorY
                && mouseY <= editorY + textAreaH) {
                int clickedRow = (mouseY - editorY) / lineHeight + scrollLineOffset;
                if (clickedRow >= 0 && clickedRow < visualRows.size()) {
                    VisualRow vr = visualRows.get(clickedRow);
                    cursorLine = vr.physicalLine;
                    int relX = mouseX - textAreaX - 4 + scrollColOffset;
                    int col = 0;
                    int accW = 0;
                    for (int c = 0; c < vr.text.length(); c++) {
                        int charW = fontRendererObj.getCharWidth(vr.text.charAt(c));
                        if (accW + charW / 2 > relX) {
                            break;
                        }
                        accW += charW;
                        col++;
                    }
                    cursorCol = Math.min(
                        vr.startChar + col,
                        lines.get(cursorLine)
                            .length());
                }
            }
        }
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int which) {
        super.mouseMovedOrUp(mouseX, mouseY, which);
        if (which == 0) {
            isDraggingVScroll = false;
            isDraggingHScroll = false;
            resizingCorner = CORNER_NONE;
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (!editSession.state.isEditable()) return;
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        if (resizingCorner != CORNER_NONE) {
            int dx = mouseX - dragStartX;
            int dy = mouseY - dragStartY;
            int minW = 320;
            int minH = 120;
            int maxW = width - 20;
            int maxH = height - 50;

            int newX = origEditorX;
            int newY = origEditorY;
            int newW = origEditorW;
            int newH = origEditorH;

            switch (resizingCorner) {
                case CORNER_NW:
                    newW = Math.max(minW, Math.min(maxW, origEditorW - dx));
                    newH = Math.max(minH, Math.min(maxH, origEditorH - dy));
                    newX = origEditorX + (origEditorW - newW);
                    newY = origEditorY + (origEditorH - newH);
                    if (newX < 10) {
                        newW = origEditorX + origEditorW - 10;
                        newX = 10;
                    }
                    if (newY < 36) {
                        newH = origEditorY + origEditorH - 36;
                        newY = 36;
                    }
                    break;
                case CORNER_NE:
                    newW = Math.max(minW, Math.min(width - origEditorX - 10, origEditorW + dx));
                    newH = Math.max(minH, Math.min(maxH, origEditorH - dy));
                    newY = origEditorY + (origEditorH - newH);
                    if (newY < 36) {
                        newH = origEditorY + origEditorH - 36;
                        newY = 36;
                    }
                    break;
                case CORNER_SW:
                    newW = Math.max(minW, Math.min(maxW, origEditorW - dx));
                    newH = Math.max(minH, Math.min(height - origEditorY - 45, origEditorH + dy));
                    newX = origEditorX + (origEditorW - newW);
                    if (newX < 10) {
                        newW = origEditorX + origEditorW - 10;
                        newX = 10;
                    }
                    break;
                case CORNER_SE:
                    newW = Math.max(minW, Math.min(width - origEditorX - 10, origEditorW + dx));
                    newH = Math.max(minH, Math.min(height - origEditorY - 45, origEditorH + dy));
                    break;
            }

            editorX = newX;
            editorY = newY;
            editorW = newW;
            editorH = newH;
            updateLayout();
            return;
        }

        int maxLineW = 0;
        for (String l : lines) {
            int w = fontRendererObj.getStringWidth(l);
            if (w > maxLineW) maxLineW = w;
        }
        boolean hasVScroll = visualRows.size() > visibleLines;
        int codeAreaW = editorW - gutterW - (hasVScroll ? scrollbarW : 0);
        boolean hasHScroll = !wordWrap && (maxLineW + 16 > codeAreaW);
        int availVScrollH = editorH - (hasHScroll ? scrollbarH : 0);

        if (isDraggingVScroll && visualRows.size() > visibleLines) {
            int totalRows = visualRows.size();
            float thumbHeight = Math.max(16, (float) visibleLines / totalRows * availVScrollH);
            float maxThumbY = availVScrollH - thumbHeight;
            float newThumbY = mouseY - editorY - dragClickOffsetY;
            float ratio = Math.max(0.0f, Math.min(1.0f, newThumbY / maxThumbY));
            scrollLineOffset = Math.round(ratio * (totalRows - visibleLines));
        }

        if (isDraggingHScroll && hasHScroll) {
            int maxColOffset = Math.max(0, (maxLineW + 20) - codeAreaW);
            float viewRatio = (float) codeAreaW / (maxLineW + 20);
            float thumbW = Math.max(16, viewRatio * codeAreaW);
            float maxThumbX = codeAreaW - thumbW;
            float newThumbX = mouseX - (editorX + gutterW) - dragClickOffsetX;
            float ratio = Math.max(0.0f, Math.min(1.0f, newThumbX / maxThumbX));
            scrollColOffset = Math.round(ratio * maxColOffset);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (!editSession.state.isEditable()) {
            if (keyCode == Keyboard.KEY_ESCAPE && !editSession.state.isPending()) leaveEditor();
            return;
        }
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
            leaveEditor();
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
            requestSave(false);
            return;
        }

        // Ctrl + I: Auto-indent all lines (2 spaces per level)
        if (isCtrlKeyDown() && keyCode == Keyboard.KEY_I) {
            autoIndentAll();
            syntaxStatus = EnumChatFormatting.GREEN + "Auto-indented (2 spaces per level)";
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
                if (wordWrap) {
                    int curRow = getCursorVisualRow();
                    if (curRow > 0) {
                        VisualRow curVr = visualRows.get(curRow);
                        int colInRow = Math.max(0, Math.min(cursorCol - curVr.startChar, curVr.text.length()));
                        VisualRow target = visualRows.get(curRow - 1);
                        cursorLine = target.physicalLine;
                        cursorCol = target.startChar + Math.min(colInRow, target.text.length());
                    }
                } else {
                    if (cursorLine > 0) {
                        cursorLine--;
                        cursorCol = Math.min(
                            cursorCol,
                            lines.get(cursorLine)
                                .length());
                    }
                }
                break;
            case Keyboard.KEY_DOWN:
                if (wordWrap) {
                    int curRow = getCursorVisualRow();
                    if (curRow < visualRows.size() - 1) {
                        VisualRow curVr = visualRows.get(curRow);
                        int colInRow = Math.max(0, Math.min(cursorCol - curVr.startChar, curVr.text.length()));
                        VisualRow target = visualRows.get(curRow + 1);
                        cursorLine = target.physicalLine;
                        cursorCol = target.startChar + Math.min(colInRow, target.text.length());
                    }
                } else {
                    if (cursorLine < lines.size() - 1) {
                        cursorLine++;
                        cursorCol = Math.min(
                            cursorCol,
                            lines.get(cursorLine)
                                .length());
                    }
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

                int curIndent = getLeadingSpaces(cur);
                String leftCode = stripComment(left).trim();
                String upperLeft = leftCode.toUpperCase(Locale.ROOT);

                boolean opensBlock = upperLeft.endsWith(" DO") || upperLeft.equals("DO")
                    || upperLeft.endsWith(" THEN")
                    || upperLeft.equals("THEN")
                    || upperLeft.equals("ELSE")
                    || upperLeft.endsWith(" ELSE");

                String rightTrim = right.trim();
                String upperRight = rightTrim.toUpperCase(Locale.ROOT);
                boolean rightIsCloser = upperRight.equals("END") || upperRight.startsWith("END ")
                    || upperRight.equals("ELSE")
                    || upperRight.startsWith("ELSE ");

                if (opensBlock && rightIsCloser) {
                    lines.set(cursorLine, left);
                    lines.add(cursorLine + 1, spaces(curIndent + 2));
                    lines.add(cursorLine + 2, spaces(curIndent) + rightTrim);
                    cursorLine++;
                    cursorCol = curIndent + 2;
                } else if (opensBlock) {
                    lines.set(cursorLine, left);
                    lines.add(cursorLine + 1, spaces(curIndent + 2) + rightTrim);
                    cursorLine++;
                    cursorCol = curIndent + 2;
                } else if (rightIsCloser) {
                    int newIndent = Math.max(0, curIndent - 2);
                    lines.set(cursorLine, left);
                    lines.add(cursorLine + 1, spaces(newIndent) + rightTrim);
                    cursorLine++;
                    cursorCol = newIndent;
                } else {
                    lines.set(cursorLine, left);
                    lines.add(cursorLine + 1, spaces(curIndent) + rightTrim);
                    cursorLine++;
                    cursorCol = curIndent;
                }
                validateSyntax();
                break;
            case Keyboard.KEY_BACK:
                if (cursorCol > 0) {
                    String s = lines.get(cursorLine);
                    // Smart dedent: if cursor is in leading whitespace at an even indentation >= 2, delete 2 spaces
                    if (isAllSpaces(s.substring(0, cursorCol)) && cursorCol >= 2 && cursorCol % 2 == 0) {
                        lines.set(cursorLine, s.substring(0, cursorCol - 2) + s.substring(cursorCol));
                        cursorCol -= 2;
                    } else {
                        lines.set(cursorLine, s.substring(0, cursorCol - 1) + s.substring(cursorCol));
                        cursorCol--;
                    }
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
                } else if (isShiftKeyDown()) {
                    // Shift+Tab: dedent current line up to 2 spaces
                    String curLine = lines.get(cursorLine);
                    if (curLine.startsWith("  ")) {
                        lines.set(cursorLine, curLine.substring(2));
                        cursorCol = Math.max(0, cursorCol - 2);
                        validateSyntax();
                    } else if (curLine.startsWith(" ")) {
                        lines.set(cursorLine, curLine.substring(1));
                        cursorCol = Math.max(0, cursorCol - 1);
                        validateSyntax();
                    }
                } else {
                    // Tab: indent 2 spaces
                    String curTab = lines.get(cursorLine);
                    lines.set(cursorLine, curTab.substring(0, cursorCol) + "  " + curTab.substring(cursorCol));
                    cursorCol += 2;
                    validateSyntax();
                }
                break;
            default:
                if (typedChar >= 32 && typedChar != 127) {
                    String curLine = lines.get(cursorLine);
                    String updated = curLine.substring(0, cursorCol) + typedChar + curLine.substring(cursorCol);
                    String trimmed = updated.trim();
                    String upperTrimmed = trimmed.toUpperCase(Locale.ROOT);

                    // Auto-dedent when completing 'END' or 'ELSE'
                    if ((upperTrimmed.equals("END") || upperTrimmed.equals("ELSE")) && updated.startsWith("  ")) {
                        int depth = getBlockDepthAtLine(lines, cursorLine);
                        int expected = getExpectedIndentForLineText(depth, updated);
                        int curLeading = getLeadingSpaces(updated);
                        if (curLeading > expected) {
                            int diff = curLeading - expected;
                            updated = spaces(expected) + trimmed;
                            cursorCol = Math.max(0, cursorCol + 1 - diff);
                            lines.set(cursorLine, updated);
                            validateSyntax();
                            break;
                        }
                    }

                    lines.set(cursorLine, updated);
                    cursorCol++;
                    validateSyntax();
                }
                break;
        }

        ensureCursorVisible();
    }

    private void ensureCursorVisible() {
        rebuildVisualRows();
        int cursorRow = getCursorVisualRow();
        if (cursorRow < scrollLineOffset) {
            scrollLineOffset = cursorRow;
        } else if (cursorRow >= scrollLineOffset + visibleLines) {
            scrollLineOffset = cursorRow - visibleLines + 1;
        }

        if (wordWrap) {
            scrollColOffset = 0;
        } else {
            if (cursorLine >= 0 && cursorLine < lines.size()) {
                String line = lines.get(cursorLine);
                String beforeCursor = line.substring(0, Math.min(cursorCol, line.length()));
                int cursorPixelX = fontRendererObj.getStringWidth(beforeCursor);
                int codeAreaW = editorW - gutterW - (visualRows.size() > visibleLines ? scrollbarW : 0);
                int textAreaW = codeAreaW - 14;

                if (cursorPixelX < scrollColOffset) {
                    scrollColOffset = Math.max(0, cursorPixelX - 20);
                } else if (cursorPixelX > scrollColOffset + textAreaW) {
                    scrollColOffset = cursorPixelX - textAreaW + 20;
                }
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

        int maxLineW = 0;
        for (String l : lines) {
            int w = fontRendererObj.getStringWidth(l);
            if (w > maxLineW) maxLineW = w;
        }

        boolean hasVScroll = visualRows.size() > visibleLines;
        int codeAreaX = editorX + gutterW;
        int codeAreaW = editorW - gutterW - (hasVScroll ? scrollbarW : 0);
        boolean hasHScroll = !wordWrap && (maxLineW + 16 > codeAreaW);
        int availVScrollH = editorH - (hasHScroll ? scrollbarH : 0);

        int maxColOffset = Math.max(0, (maxLineW + 20) - codeAreaW);
        if (wordWrap || scrollColOffset > maxColOffset) {
            scrollColOffset = wordWrap ? 0 : maxColOffset;
        }

        // 4. Render code lines with GL Scissor
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(
            mc,
            mc.displayWidth,
            mc.displayHeight);
        int sf = sr.getScaleFactor();
        GL11.glScissor(codeAreaX * sf, (height - (editorY + availVScrollH)) * sf, codeAreaW * sf, availVScrollH * sf);

        int cursorVisualRow = getCursorVisualRow();

        for (int i = 0; i < visibleLines; i++) {
            int rowIdx = scrollLineOffset + i;
            if (rowIdx >= visualRows.size()) break;

            VisualRow vr = visualRows.get(rowIdx);
            if (vr.physicalLine < 0 || vr.physicalLine >= lines.size()) continue;
            int y = editorY + 3 + i * lineHeight;

            // Active line highlight
            if (vr.physicalLine == cursorLine) {
                drawRect(codeAreaX, y - 1, codeAreaX + codeAreaW, y + lineHeight - 1, 0xFF181818);
            }

            SfmlSyntaxRenderer.draw(
                fontRendererObj,
                highlighted,
                vr.physicalLine,
                vr.startChar,
                vr.endChar,
                codeAreaX + 4 - scrollColOffset,
                y);

            // Cursor rendering
            if (rowIdx == cursorVisualRow && (System.currentTimeMillis() / 450) % 2 == 0) {
                int colInRow = Math.max(0, Math.min(cursorCol - vr.startChar, vr.text.length()));
                String before = vr.text.substring(0, colInRow);
                int cursorPixelX = codeAreaX + 4 - scrollColOffset + fontRendererObj.getStringWidth(before);
                drawRect(cursorPixelX, y + lineHeight - 2, cursorPixelX + 5, y + lineHeight - 1, 0xFFFFFFFF);
            }
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Gutter line numbers if enabled
        if (gutterW > 0) {
            for (int i = 0; i < visibleLines; i++) {
                int rowIdx = scrollLineOffset + i;
                if (rowIdx >= visualRows.size()) break;
                VisualRow vr = visualRows.get(rowIdx);
                if (vr.physicalLine < 0 || vr.physicalLine >= lines.size()) continue;
                if (vr.startChar == 0) {
                    int y = editorY + 3 + i * lineHeight;
                    String numStr = String.valueOf(vr.physicalLine + 1);
                    int numW = fontRendererObj.getStringWidth(numStr);
                    int color = vr.physicalLine == cursorLine ? 0xFFFFFF : 0x707070;
                    fontRendererObj.drawString(numStr, editorX + gutterW - numW - 4, y, color);
                }
            }
        }

        // 5. Vertical Scrollbar Thumb & Track
        int scrollbarX = editorX + editorW - scrollbarW;
        if (hasVScroll) {
            drawRect(scrollbarX, editorY, scrollbarX + scrollbarW, editorY + availVScrollH, 0xFF2D2D30);
            float thumbHeight = Math.max(16, (float) visibleLines / visualRows.size() * availVScrollH);
            float maxThumbY = availVScrollH - thumbHeight;
            float thumbY = editorY + ((float) scrollLineOffset / (visualRows.size() - visibleLines)) * maxThumbY;
            int thumbColor = isDraggingVScroll ? 0xFF7A7A7E : 0xFF505054;
            drawRect(
                scrollbarX + 1,
                (int) thumbY,
                scrollbarX + scrollbarW - 1,
                (int) (thumbY + thumbHeight),
                thumbColor);
        }

        // 6. Horizontal Scrollbar Track & Thumb (mutually exclusive with word-wrap)
        if (hasHScroll) {
            int hScrollY = editorY + editorH - scrollbarH;
            drawRect(codeAreaX, hScrollY, codeAreaX + codeAreaW, hScrollY + scrollbarH, 0xFF2D2D30);
            float viewRatio = (float) codeAreaW / (maxLineW + 20);
            float thumbW = Math.max(16, viewRatio * codeAreaW);
            float maxThumbX = codeAreaW - thumbW;
            float thumbX = codeAreaX + ((float) scrollColOffset / maxColOffset) * maxThumbX;
            int thumbColor = isDraggingHScroll ? 0xFF7A7A7E : 0xFF505054;
            drawRect((int) thumbX, hScrollY + 1, (int) (thumbX + thumbW), hScrollY + scrollbarH - 1, thumbColor);
        }

        // 7. Corner Junction if both scrollbars are visible
        if (hasVScroll && hasHScroll) {
            drawRect(scrollbarX, editorY + editorH - scrollbarH, editorX + editorW, editorY + editorH, 0xFF252526);
        }

        // 8. Resizable Corner Grips (NW, NE, SW, SE)
        int hoveredCorner = getHoveredCorner(mouseX, mouseY);
        drawCornerGrip(CORNER_NW, editorX, editorY, hoveredCorner == CORNER_NW || resizingCorner == CORNER_NW);
        drawCornerGrip(
            CORNER_NE,
            editorX + editorW,
            editorY,
            hoveredCorner == CORNER_NE || resizingCorner == CORNER_NE);
        drawCornerGrip(
            CORNER_SW,
            editorX,
            editorY + editorH,
            hoveredCorner == CORNER_SW || resizingCorner == CORNER_SW);
        drawCornerGrip(
            CORNER_SE,
            editorX + editorW,
            editorY + editorH,
            hoveredCorner == CORNER_SE || resizingCorner == CORNER_SE);

        // 9. Title and Quick Shortcuts bar (above toolbar)
        String title = (editorW < 360) ? "Code Editor" : "Super Factory Manager - Code Editor";
        fontRendererObj.drawStringWithShadow(title, editorX, editorY - 34, 0x00E0FF);
        String shortcutHelp = (editorW >= 420)
            ? (EnumChatFormatting.GRAY + "Autocomplete: "
                + EnumChatFormatting.AQUA
                + "Ctrl+Space"
                + EnumChatFormatting.GRAY
                + " | Indent: "
                + EnumChatFormatting.AQUA
                + "Ctrl+I"
                + EnumChatFormatting.GRAY
                + " | Next: "
                + EnumChatFormatting.AQUA
                + "Tab")
            : (EnumChatFormatting.GRAY + "Indent: "
                + EnumChatFormatting.AQUA
                + "Ctrl+I"
                + EnumChatFormatting.GRAY
                + " | Next: "
                + EnumChatFormatting.AQUA
                + "Tab");
        if (editorW >= 360) {
            fontRendererObj.drawStringWithShadow(
                shortcutHelp,
                editorX + editorW - fontRendererObj.getStringWidth(shortcutHelp),
                editorY - 34,
                0xFFFFFF);
        }

        // 10. Status Line (between editor and bottom buttons)
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

        // 11. Autocomplete Popup on top of everything
        popup.draw(fontRendererObj, mouseX, mouseY);
    }

    private void drawCornerGrip(int corner, int cx, int cy, boolean active) {
        int color = active ? 0xFF00E0FF : 0xFF808080;
        int size = 5;
        switch (corner) {
            case CORNER_NW:
                drawRect(cx - 2, cy - 2, cx + size, cy, color);
                drawRect(cx - 2, cy - 2, cx, cy + size, color);
                break;
            case CORNER_NE:
                drawRect(cx - size, cy - 2, cx + 2, cy, color);
                drawRect(cx, cy - 2, cx + 2, cy + size, color);
                break;
            case CORNER_SW:
                drawRect(cx - 2, cy, cx + size, cy + 2, color);
                drawRect(cx - 2, cy - size, cx, cy + 2, color);
                break;
            case CORNER_SE:
                drawRect(cx - size, cy, cx + 2, cy + 2, color);
                drawRect(cx, cy - size, cx + 2, cy + 2, color);
                // Diagonal grip ticks in bottom-right corner
                drawRect(cx - 3, cy - 1, cx - 1, cy, color);
                drawRect(cx - 6, cy - 1, cx - 4, cy, color);
                drawRect(cx - 1, cy - 3, cx, cy - 1, color);
                drawRect(cx - 3, cy - 3, cx - 2, cy - 2, color);
                break;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
