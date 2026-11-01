package ca.teamdman.sfm.client.gui.mui2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.widget.IFocusedWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.widget.Widget;

import ca.teamdman.sfm.client.gui.EditorDiagnostic;
import ca.teamdman.sfm.client.gui.StudioProblems;

/** Wrapped issue cards. Source extraction happens on snapshots, font measurement only on snapshot/width changes. */
final class StudioProblemsPane extends Widget<StudioProblemsPane> implements Interactable, IFocusedWidget {

    private static final int LINE_HEIGHT = 11;
    private final BooleanSupplier input;
    private final Consumer<EditorDiagnostic> jump;
    private StudioProblems.Snapshot snapshot = StudioProblems
        .snapshot("", Collections.emptyList(), Collections.emptyList());
    private final List<Line> lines = new ArrayList<>();
    private final List<Integer> starts = new ArrayList<>(), ends = new ArrayList<>();
    private int cachedWidth = -1, scroll, contentHeight, selected = -1;

    StudioProblemsPane(BooleanSupplier input, Consumer<EditorDiagnostic> jump) {
        this.input = input;
        this.jump = jump;
        addTooltipLine("Click an issue to jump. Wheel scrolls; Up/Down select; Enter jumps. Columns start at 1.");
    }

    void snapshot(String source, List<String> errors, List<String> summary) {
        EditorDiagnostic previous = selectedDiagnostic();
        snapshot = StudioProblems.snapshot(source, errors, summary);
        selected = snapshot.issues.isEmpty() ? -1 : 0;
        if (previous != null) {
            for (int i = 0; i < snapshot.issues.size(); i++) {
                EditorDiagnostic next = snapshot.issues.get(i).location;
                if (next != null && next.line == previous.line && next.column == previous.column) {
                    selected = i;
                    break;
                }
            }
        }
        cachedWidth = -1;
    }

    EditorDiagnostic selectedDiagnostic() {
        return selected < 0 ? null : snapshot.issues.get(selected).location;
    }

    void jumpSelected() {
        EditorDiagnostic location = selectedDiagnostic();
        if (input.getAsBoolean() && location != null) jump.accept(location);
    }

    private void measure() {
        int width = Math.max(1, getArea().width - 16);
        if (cachedWidth == width) return;
        cachedWidth = width;
        lines.clear();
        starts.clear();
        ends.clear();
        contentHeight = 4;
        for (int i = 0; i < snapshot.issues.size(); i++) {
            StudioProblems.Issue issue = snapshot.issues.get(i);
            starts.add(contentHeight - 2);
            wrap(issue.heading, width, 0xfff0b5b5);
            wrap(issue.message, width, 0xffe1e4e9);
            if (!issue.context.isEmpty()) wrap(issue.context, width, 0xffadb9ca);
            contentHeight += 4;
            ends.add(contentHeight - 2);
        }
        for (String summary : snapshot.summary) wrap(summary, width, 0xff9da8b6);
        contentHeight += 2;
        clampScroll();
    }

    private void wrap(String text, int width, int color) {
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        int start = 0;
        do {
            int count = font.trimStringToWidth(text.substring(start), width)
                .length();
            if (start + count < text.length() && count > 0 && Character.isHighSurrogate(text.charAt(start + count - 1)))
                count--;
            if (count == 0 && start < text.length()) count = Character.charCount(text.codePointAt(start));
            int end = Math.min(text.length(), start + count);
            // Prefer word boundaries while retaining every non-space character of compiler output.
            if (end < text.length()) {
                int space = text.lastIndexOf(' ', end);
                if (space > start + count / 2) end = space;
            }
            lines.add(new Line(text.substring(start, end), contentHeight, color));
            contentHeight += LINE_HEIGHT;
            start = end;
            while (start < text.length() && text.charAt(start) == ' ') start++;
        } while (start < text.length());
    }

    private void clampScroll() {
        scroll = Math.max(0, Math.min(scroll, Math.max(0, contentHeight - getArea().height)));
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> theme) {
        measure();
        clampScroll();
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        boolean unicode = font.getUnicodeFlag(), bidi = font.getBidiFlag();
        int width = getArea().width, height = getArea().height;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT | GL11.GL_TEXTURE_BIT);
        Stencil.apply(0, 0, width, height, context);
        try {
            Gui.drawRect(0, 0, width, height, 0xff15171b);
            if (selected >= 0) {
                int top = starts.get(selected) - scroll, bottom = ends.get(selected) - scroll;
                Gui.drawRect(2, top, width - 5, bottom, 0xff303b4b);
                Gui.drawRect(2, top, 4, bottom, 0xffe29a9a);
            }
            Platform.setupDrawFont();
            for (int i = firstVisible(); i < lines.size(); i++) {
                Line line = lines.get(i);
                int y = line.y - scroll;
                if (y >= height) break;
                if (y + 9 > 0) font.drawString(line.text, 7, y, line.color);
            }
            if (contentHeight > height && height > 0) {
                int marker = Math.min(height, Math.max(3, height * height / contentHeight));
                int y = scroll * (height - marker) / Math.max(1, contentHeight - height);
                Gui.drawRect(width - 3, y, width - 1, y + marker, 0xff98616a);
            }
        } finally {
            font.setUnicodeFlag(unicode);
            font.setBidiFlag(bidi);
            Stencil.remove();
            GL11.glPopAttrib();
        }
    }

    private int firstVisible() {
        int low = 0, high = lines.size();
        while (low < high) {
            int middle = (low + high) >>> 1;
            if (lines.get(middle).y + 9 <= scroll) low = middle + 1;
            else high = middle;
        }
        return low;
    }

    @Override
    public Result onMousePressed(int button) {
        if (button != 0 || !isHovering() || !input.getAsBoolean()) return Result.IGNORE;
        measure();
        getContext().focus(this);
        int y = getContext().getMouseY() + scroll;
        for (int i = 0; i < starts.size(); i++) {
            if (y >= starts.get(i) && y < ends.get(i)) {
                selected = i;
                jumpSelected();
                break;
            }
        }
        return Result.SUCCESS;
    }

    @Override
    public boolean onMouseScroll(UpOrDown direction, int amount) {
        if (!isHovering() || !input.getAsBoolean()) return false;
        measure();
        scroll += direction.isUp() ? -3 * LINE_HEIGHT : 3 * LINE_HEIGHT;
        clampScroll();
        return true;
    }

    @Override
    public boolean isFocused() {
        return isValid() && getContext().isFocused(this);
    }

    @Override
    public void onFocus(ModularGuiContext context) {}

    @Override
    public void onRemoveFocus(ModularGuiContext context) {}

    @Override
    public Result onKeyPressed(char character, int key) {
        if (!isFocused() || !input.getAsBoolean()) return Result.IGNORE;
        if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) jumpSelected();
        else if (key == Keyboard.KEY_UP || key == Keyboard.KEY_DOWN) {
            if (snapshot.issues.isEmpty()) return Result.SUCCESS;
            selected = Math.max(0, Math.min(snapshot.issues.size() - 1, selected + (key == Keyboard.KEY_UP ? -1 : 1)));
            measure();
            if (starts.get(selected) < scroll) scroll = starts.get(selected);
            else if (ends.get(selected) > scroll + getArea().height)
                scroll = Math.min(starts.get(selected), ends.get(selected) - getArea().height);
            clampScroll();
        } else return Result.IGNORE;
        return Result.SUCCESS;
    }

    private static final class Line {

        final String text;
        final int y, color;

        Line(String text, int y, int color) {
            this.text = text;
            this.y = y;
            this.color = color;
        }
    }
}
