package ca.teamdman.sfm.client.gui.syntax;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class GuiAutocompletePopup extends Gui {

    private int x;
    private int y;
    private int width = 220;
    private int itemHeight = 18;
    private int maxVisible = 6;

    private boolean visible = false;
    private String header = null;
    private final List<SyntaxSnippet> items = new ArrayList<>();
    private int selectedIndex = 0;
    private int scrollOffset = 0;

    public void openAt(int x, int y, int screenW, int screenH, String header, List<SyntaxSnippet> snippets) {
        this.items.clear();
        if (snippets != null) {
            this.items.addAll(snippets);
        }
        this.header = header;
        this.selectedIndex = 0;
        this.scrollOffset = 0;
        this.visible = !this.items.isEmpty();

        // Calculate dimensions
        this.width = 240;
        int totalH = (header != null ? 14 : 0) + Math.min(items.size(), maxVisible) * itemHeight + 4;

        // Boundary adjustments so popup stays within screen
        if (x + width > screenW - 8) {
            x = screenW - width - 8;
        }
        if (y + totalH > screenH - 8) {
            y = y - totalH - 12; // Anchor above cursor if overflowing bottom
        }
        this.x = Math.max(8, x);
        this.y = Math.max(8, y);
    }

    public void close() {
        this.visible = false;
        this.items.clear();
        this.header = null;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean contains(int mouseX, int mouseY) {
        if (!visible || items.isEmpty()) return false;
        int visibleCount = Math.min(items.size(), maxVisible);
        int totalH = (header != null ? 14 : 0) + visibleCount * itemHeight + 4;
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + totalH;
    }

    public List<SyntaxSnippet> getItems() {
        return items;
    }

    public SyntaxSnippet getSelectedItem() {
        if (!visible || items.isEmpty() || selectedIndex < 0 || selectedIndex >= items.size()) {
            return null;
        }
        return items.get(selectedIndex);
    }

    public void draw(FontRenderer font, int mouseX, int mouseY) {
        if (!visible || items.isEmpty()) return;

        int visibleCount = Math.min(items.size(), maxVisible);
        int contentH = visibleCount * itemHeight;
        int totalH = (header != null ? 14 : 0) + contentH + 4;

        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0, 300.0F); // Draw above editor and buttons

        // Outer crisp border
        drawRect(x - 1, y - 1, x + width + 1, y + totalH + 1, 0xFF007ACC);
        // Popup Dark Background
        drawRect(x, y, x + width, y + totalH, 0xFF1E1E1E);

        int curY = y + 2;

        // Header if present
        if (header != null) {
            drawRect(x + 1, curY, x + width - 1, curY + 12, 0xFF2D2D30);
            font.drawString(EnumChatFormatting.BOLD + header, x + 5, curY + 2, 0x569CD6);
            curY += 13;
        }

        // Render visible snippet items
        for (int i = 0; i < visibleCount; i++) {
            int idx = scrollOffset + i;
            if (idx >= items.size()) break;

            SyntaxSnippet s = items.get(idx);
            int rowY = curY + i * itemHeight;
            boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= rowY && mouseY < rowY + itemHeight;
            boolean isSelected = idx == selectedIndex;

            if (isSelected) {
                drawRect(x + 1, rowY, x + width - 1, rowY + itemHeight, 0xFF094771); // VS Code selection blue
            } else if (isHovered) {
                drawRect(x + 1, rowY, x + width - 1, rowY + itemHeight, 0xFF2A2D2E); // Hover gray
            }

            // Category tag badge
            SnippetCategory cat = s.getCategory();
            String catBadge = "[" + cat.name()
                .substring(
                    0,
                    Math.min(
                        3,
                        cat.name()
                            .length()))
                + "]";
            font.drawString(catBadge, x + 4, rowY + 1, cat.getColor());

            // Title
            int textX = x + 34;
            int maxTitleW = width - 40;
            String title = font.trimStringToWidth(s.getTitle(), maxTitleW);
            font.drawString(title, textX, rowY + 1, isSelected ? 0xFFFFFF : 0xE0E0E0);

            // Description / hint
            String desc = font.trimStringToWidth(s.getDescription(), width - 12);
            font.drawString(desc, x + 6, rowY + 10, 0x858585);
        }

        // Scrollbar if more items than maxVisible
        if (items.size() > maxVisible) {
            int sbW = 4;
            int sbX = x + width - sbW - 1;
            int sbY = (header != null ? y + 15 : y + 2);
            drawRect(sbX, sbY, sbX + sbW, sbY + contentH, 0xFF2D2D30);

            float ratio = (float) maxVisible / items.size();
            int thumbH = Math.max(8, (int) (ratio * contentH));
            int maxThumbY = contentH - thumbH;
            int thumbY = sbY + (int) (((float) scrollOffset / (items.size() - maxVisible)) * maxThumbY);
            drawRect(sbX, thumbY, sbX + sbW, thumbY + thumbH, 0xFF007ACC);
        }

        GL11.glPopMatrix();
    }

    public SyntaxSnippet mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!visible || items.isEmpty()) return null;

        int visibleCount = Math.min(items.size(), maxVisible);
        int totalH = (header != null ? 14 : 0) + visibleCount * itemHeight + 4;

        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + totalH) {
            // Click outside closes the popup
            close();
            return null;
        }

        if (mouseButton == 0) {
            int startY = y + 2 + (header != null ? 13 : 0);
            int relY = mouseY - startY;
            if (relY >= 0) {
                int clickedIndex = scrollOffset + (relY / itemHeight);
                if (clickedIndex >= 0 && clickedIndex < items.size()) {
                    selectedIndex = clickedIndex;
                    SyntaxSnippet selected = items.get(clickedIndex);
                    close();
                    return selected;
                }
            }
        }
        return null;
    }

    public void handleMouseWheel(int dWheel) {
        if (!visible || items.size() <= maxVisible || dWheel == 0) return;
        if (dWheel > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else {
            scrollOffset = Math.min(items.size() - maxVisible, scrollOffset + 1);
        }
    }

    public SyntaxSnippet handleKeyInput(char typedChar, int keyCode) {
        if (!visible) return null;

        if (keyCode == Keyboard.KEY_ESCAPE) {
            close();
            return null;
        }

        if (keyCode == Keyboard.KEY_DOWN) {
            selectedIndex = (selectedIndex + 1) % items.size();
            ensureVisible();
            return null;
        }

        if (keyCode == Keyboard.KEY_UP) {
            selectedIndex = (selectedIndex - 1 + items.size()) % items.size();
            ensureVisible();
            return null;
        }

        if (keyCode == Keyboard.KEY_PRIOR) { // Page Up
            selectedIndex = Math.max(0, selectedIndex - maxVisible);
            ensureVisible();
            return null;
        }

        if (keyCode == Keyboard.KEY_NEXT) { // Page Down
            selectedIndex = Math.min(items.size() - 1, selectedIndex + maxVisible);
            ensureVisible();
            return null;
        }

        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER || keyCode == Keyboard.KEY_TAB) {
            SyntaxSnippet s = getSelectedItem();
            close();
            return s;
        }

        return null;
    }

    private void ensureVisible() {
        if (selectedIndex < scrollOffset) {
            scrollOffset = selectedIndex;
        } else if (selectedIndex >= scrollOffset + maxVisible) {
            scrollOffset = selectedIndex - maxVisible + 1;
        }
    }
}
