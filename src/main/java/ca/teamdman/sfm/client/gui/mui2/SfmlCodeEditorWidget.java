package ca.teamdman.sfm.client.gui.mui2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.regex.Matcher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IFocusedWidget;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.api.widget.ModernInteractable;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.widget.AbstractScrollWidget;
import com.cleanroommc.modularui.widget.scroll.HorizontalScrollData;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.VoidWidget;

import ca.teamdman.sfm.client.gui.EditorDiagnostic;
import ca.teamdman.sfm.client.gui.syntax.SfmlCompletionCatalog;
import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter;
import ca.teamdman.sfm.client.gui.syntax.SfmlSyntaxRenderer;
import ca.teamdman.sfm.client.gui.syntax.StudioSymbolHelp;
import ca.teamdman.sfm.client.gui.syntax.SyntaxSnippet;
import me.eigenraven.lwjgl3ify.api.InputEvents;

/** A native MUI2 focus/scroll surface; visual wrapping is separate from the authoritative source. */
public final class SfmlCodeEditorWidget extends AbstractScrollWidget<VoidWidget, SfmlCodeEditorWidget>
    implements IFocusedWidget, ModernInteractable {

    private static final int LINE_HEIGHT = 10;
    public final SfmlEditorBuffer buffer = new SfmlEditorBuffer();
    private final Runnable changed, save, suggest, close;
    private final BooleanSupplier editable;
    private BooleanSupplier inputGate = () -> true;
    private final Consumer<String> error;
    private final List<Row> rows = new ArrayList<>();
    private SfmlHighlighter.Highlighted highlighted = SfmlHighlighter.highlight("");
    private String cachedSource, highlightedSource = "";
    private int cachedWidth = -1, cachedHeight = -1, gutter = 25, blink;
    private boolean wrap;
    private Runnable diagnosticAction = () -> {};
    private Runnable focused = () -> {}, find = () -> {};
    private SfmlCompletionCatalog.Snapshot catalog = SfmlCompletionCatalog.scan("");
    private SfmlCompletionCatalog.Snapshot hoverCatalog;
    private SfmlCompletionCatalog.Entry hoverEntry;
    private String hoverSource, diagnosticSource, diagnosticMessage;
    private int hoverOffset = -1, errorOffset = -1;
    private EditorDiagnostic diagnostic;
    private boolean hoveringError;

    private static final class Row {

        final int line, column, offset, end;

        Row(int line, int column, int offset, int end) {
            this.line = line;
            this.column = column;
            this.offset = offset;
            this.end = end;
        }
    }

    public SfmlCodeEditorWidget(Runnable changed, Runnable save, Runnable suggest, Runnable close,
        BooleanSupplier editable, Consumer<String> error) {
        super(new HorizontalScrollData(false, 6), new VerticalScrollData(false, 6));
        this.changed = changed;
        this.save = save;
        this.suggest = suggest;
        this.close = close;
        this.editable = editable;
        this.error = error;
        padding(0);
        background(new Rectangle().color(0xff181b20));
        tooltipShowUpTimer(12);
        tooltipDynamic(tooltip -> {
            if (hoveringError) tooltip.addLine(
                IKey.str(diagnosticMessage)
                    .color(0xffefa09a));
            else if (hoverEntry != null) {
                tooltip.addLine(
                    IKey.str(hoverEntry.getSignature())
                        .color(0xffe7e8eb));
                tooltip.addLine(
                    IKey.str(hoverEntry.getDescription())
                        .color(0xffb0b3bb));
            }
        });
    }

    @Override
    public List<IWidget> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public boolean isChildValid(VoidWidget child) {
        return false;
    }

    @Override
    public boolean isFocused() {
        return getContext().isFocused(this);
    }

    public String source() {
        return buffer.source();
    }

    public void load(String source) {
        buffer.load(source);
        refresh();
    }

    public void onDiagnostic(Runnable action) {
        diagnosticAction = action;
    }

    public void inputGate(BooleanSupplier inputGate) {
        this.inputGate = inputGate;
    }

    public void onFocused(Runnable action) {
        focused = action;
    }

    public void onFind(Runnable action) {
        find = action;
    }

    public void catalog(SfmlCompletionCatalog.Snapshot catalog) {
        this.catalog = catalog;
    }

    /** Only a result for this exact source is decorated; edits clear it before delayed compilation. */
    public void diagnostic(String message) {
        diagnostic = EditorDiagnostic.first(message);
        diagnosticSource = source();
        diagnosticMessage = diagnostic == null ? null : message;
        errorOffset = diagnostic == null ? -1 : diagnostic.offset(source());
        markTooltipDirty();
    }

    /** Apply a precomputed completion as one undoable edit, never a whole-document reload. */
    public boolean replaceRange(int start, int end, String text) {
        if (!editable.getAsBoolean()) return false;
        int anchor = buffer.anchor(), caret = buffer.caret();
        buffer.select(start, end);
        if (insert(text)) {
            Matcher placeholder = SyntaxSnippet.PLACEHOLDER_PATTERN.matcher(text);
            if (placeholder.find()) {
                buffer.select(start + placeholder.start(), start + placeholder.end());
                ensureVisible();
            }
            return true;
        }
        buffer.select(anchor, caret);
        return false;
    }

    public boolean jumpToDiagnostic(String message) {
        ca.teamdman.sfm.client.gui.EditorDiagnostic location = ca.teamdman.sfm.client.gui.EditorDiagnostic
            .first(message);
        if (location == null) return false;
        buffer.move(location.offset(source()), false);
        blink = 0;
        if (isValid()) {
            getContext().focus(this);
            ensureVisible();
        }
        return true;
    }

    public void goTo(int offset) {
        buffer.move(offset, false);
        blink = 0;
        if (isValid()) {
            getContext().focus(this);
            ensureVisible();
        }
    }

    public void undo() {
        if (buffer.undo()) refresh();
    }

    public void redo() {
        if (buffer.redo()) refresh();
    }

    public void toggleWrap() {
        wrap = !wrap;
        cachedWidth = -1;
        refresh();
    }

    public boolean wraps() {
        return wrap;
    }

    public int line() {
        return lineAt(buffer.caret());
    }

    public int column() {
        return buffer.caret() - lineStart(buffer.caret());
    }

    @Override
    public void onFocus(ModularGuiContext context) {
        blink = 0;
        focused.run();
        if (ModularUI.Mods.LWJGL3IFY.isLoaded()) InputEvents.beginTextInput();
    }

    @Override
    public void onRemoveFocus(ModularGuiContext context) {
        if (ModularUI.Mods.LWJGL3IFY.isLoaded()) InputEvents.endTextInput();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        blink++;
    }

    private FontRenderer font() {
        return Minecraft.getMinecraft().fontRenderer;
    }

    private void measure() {
        int width = Math.max(32, getArea().w() - gutter - 12);
        if (cachedWidth == width && cachedHeight == getArea().h() && source().equals(cachedSource)) return;
        cachedSource = source();
        cachedWidth = width;
        cachedHeight = getArea().h();
        rows.clear();
        String[] lines = source().split("\n", -1);
        gutter = Math.max(25, font().getStringWidth(Integer.toString(lines.length)) + 10);
        int offset = 0, widest = 0;
        for (int line = 0; line < lines.length; line++) {
            String text = lines[line];
            widest = Math.max(widest, font().getStringWidth(text));
            int start = 0;
            do {
                int count = wrap ? font().trimStringToWidth(text.substring(start), width)
                    .length() : text.length();
                if (wrap && start + count < text.length()
                    && count > 0
                    && Character.isHighSurrogate(text.charAt(start + count - 1))) count--;
                if (start < text.length() && count <= 0) count = Character.charCount(text.codePointAt(start));
                int end = Math.min(text.length(), start + count);
                rows.add(new Row(line, start, offset + start, offset + end));
                start = end;
            } while (start < text.length());
            offset += text.length() + 1;
        }
        getScrollArea().getScrollY()
            .setScrollSize(rows.size() * LINE_HEIGHT + 6);
        getScrollArea().getScrollX()
            .setScrollSize(wrap ? 0 : widest + gutter + 16);
        getScrollArea().getScrollX()
            .clamp(getScrollArea());
        getScrollArea().getScrollY()
            .clamp(getScrollArea());
    }

    @Override
    public void preDraw(ModularGuiContext context, boolean transformed) {
        super.preDraw(context, transformed);
        if (!transformed) return;
        measure();
        int first = Math.max(0, getScrollY() / LINE_HEIGHT - 1);
        int last = Math.min(rows.size(), first + getArea().h() / LINE_HEIGHT + 3);
        for (int i = first; i < last; i++) {
            Row row = rows.get(i);
            int y = i * LINE_HEIGHT + 3;
            int selectionStart = Math.max(row.offset, buffer.start()), selectionEnd = Math.min(row.end, buffer.end());
            if (selectionEnd > selectionStart) {
                int x1 = gutter + 4 + font().getStringWidth(source().substring(row.offset, selectionStart));
                int x2 = gutter + 4 + font().getStringWidth(source().substring(row.offset, selectionEnd));
                Gui.drawRect(x1, y - 1, x2, y + 9, 0xff254b70);
            }
            SfmlSyntaxRenderer
                .draw(font(), highlighted, row.line, row.column, row.column + row.end - row.offset, gutter + 4, y);
            if (diagnostic != null && row.line == diagnostic.line - 1
                && errorOffset >= row.offset
                && errorOffset <= row.end) {
                int x = gutter + 4 + font().getStringWidth(source().substring(row.offset, errorOffset));
                Gui.drawRect(x, y + 8, x + 5, y + 9, 0xffe07373);
            }
            if (isFocused() && blink % 20 < 10 && i == caretRow()) {
                int x = gutter + 4 + font().getStringWidth(source().substring(row.offset, buffer.caret()));
                Gui.drawRect(x, y - 1, x + 1, y + 9, 0xffeeeeee);
            }
        }
        // Compensate only for horizontal scrolling: the gutter follows vertical rows.
        Gui.drawRect(getScrollX(), getScrollY(), getScrollX() + gutter, getScrollY() + getArea().h(), 0xff202329);
        for (int i = first; i < last; i++) {
            Row row = rows.get(i);
            if (row.column == 0) {
                boolean errorLine = diagnostic != null && row.line == diagnostic.line - 1;
                font().drawString(
                    Integer.toString(row.line + 1),
                    getScrollX() + 4,
                    i * LINE_HEIGHT + 3,
                    errorLine ? 0xe07373 : 0x858b96);
            }
        }
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        if (!isHovering() || !inputGate.getAsBoolean()) return;
        int offset = atMouse();
        boolean error = diagnostic != null && lineAt(offset) == diagnostic.line - 1
            && (context.getMouseX() < gutter || Math.abs(offset - errorOffset) <= 1);
        if (hoverOffset != offset || hoverCatalog != catalog
            || !source().equals(hoverSource)
            || hoveringError != error) {
            hoverOffset = offset;
            hoverCatalog = catalog;
            hoverSource = source();
            hoveringError = error;
            hoverEntry = error ? null : StudioSymbolHelp.at(source(), highlighted, catalog, offset);
            markTooltipDirty();
        }
        if (hoveringError || hoverEntry != null) super.drawForeground(context);
    }

    @Override
    public Result onMousePressed(int button) {
        if (!inputGate.getAsBoolean()) return Result.IGNORE;
        if (getScrollArea().mouseClicked(getContext())) return Result.SUCCESS;
        if (!isHovering()) return Result.IGNORE;
        if (button == 0) {
            getContext().focus(this);
            buffer.move(atMouse(), Interactable.hasShiftDown());
        }
        return Result.SUCCESS;
    }

    @Override
    public void onMouseDrag(int button, long time) {
        super.onMouseDrag(button, time);
        if (button == 0 && isFocused() && !getScrollArea().isDragging()) buffer.move(atMouse(), true);
    }

    private int atMouse() {
        measure();
        int row = Math.max(0, Math.min(rows.size() - 1, (getContext().getMouseY() + getScrollY() - 3) / LINE_HEIGHT));
        return atX(rows.get(row), getContext().getMouseX() + getScrollX() - gutter - 4);
    }

    private int atX(Row row, int x) {
        int offset = row.offset, pixel = 0;
        while (offset < row.end) {
            int next = buffer.next(offset);
            int width = font().getStringWidth(source().substring(offset, next));
            if (pixel + width / 2 >= x) break;
            pixel += width;
            offset = next;
        }
        return offset;
    }

    @Override
    public Result onKeyPressed(char character, int key) {
        if (!isFocused() || !inputGate.getAsBoolean()) return Result.IGNORE;
        boolean ctrl = Interactable.hasControlDown(), shift = Interactable.hasShiftDown();
        if (ctrl && (key == Keyboard.KEY_F || key == Keyboard.KEY_H)) {
            find.run();
            return Result.SUCCESS;
        }
        if (ctrl && key == Keyboard.KEY_C) {
            GuiScreen.setClipboardString(buffer.selected() ? buffer.selection() : source());
            return Result.SUCCESS;
        }
        if (ctrl && key == Keyboard.KEY_A) {
            buffer.select(0, source().length());
            return Result.SUCCESS;
        }
        if (key == Keyboard.KEY_ESCAPE) {
            close.run();
            return Result.SUCCESS;
        }
        if (!editable.getAsBoolean()) return Result.SUCCESS;
        if (key == Keyboard.KEY_F4) diagnosticAction.run();
        else if (ctrl && key == Keyboard.KEY_S) save.run();
        else if (ctrl && key == Keyboard.KEY_SPACE) suggest.run();
        else if (ctrl && key == Keyboard.KEY_Z) {
            if (shift) buffer.redo();
            else buffer.undo();
            refresh();
        } else if (ctrl && key == Keyboard.KEY_Y) {
            buffer.redo();
            refresh();
        } else if (ctrl && key == Keyboard.KEY_V) insert(GuiScreen.getClipboardString());
        else if (ctrl && key == Keyboard.KEY_X) {
            GuiScreen.setClipboardString(buffer.selection());
            buffer.insert("");
            refresh();
        } else if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) newline();
        else if (key == Keyboard.KEY_TAB) {
            if (!placeholder(!shift)) insert("  ");
        } else if (key == Keyboard.KEY_BACK || key == Keyboard.KEY_DELETE) {
            if (ctrl && !buffer.selected()) buffer.select(buffer.caret(), word(key == Keyboard.KEY_DELETE));
            buffer.delete(key == Keyboard.KEY_DELETE);
            refresh();
        } else if (key == Keyboard.KEY_LEFT || key == Keyboard.KEY_RIGHT) {
            boolean forward = key == Keyboard.KEY_RIGHT;
            buffer.move(
                ctrl ? word(forward) : forward ? buffer.next(buffer.caret()) : buffer.previous(buffer.caret()),
                shift);
        } else if (key == Keyboard.KEY_HOME || key == Keyboard.KEY_END) {
            buffer.move(
                ctrl ? key == Keyboard.KEY_HOME ? 0 : source().length()
                    : key == Keyboard.KEY_HOME ? lineStart(buffer.caret()) : lineEnd(buffer.caret()),
                shift);
        } else if (key == Keyboard.KEY_UP || key == Keyboard.KEY_DOWN
            || key == Keyboard.KEY_PRIOR
            || key == Keyboard.KEY_NEXT) {
                measure();
                int current = caretRow();
                int delta = key == Keyboard.KEY_UP ? -1
                    : key == Keyboard.KEY_DOWN ? 1
                        : (key == Keyboard.KEY_PRIOR ? -1 : 1) * Math.max(1, getArea().h() / LINE_HEIGHT - 1);
                int x = font().getStringWidth(source().substring(rows.get(current).offset, buffer.caret()));
                buffer.move(atX(rows.get(Math.max(0, Math.min(rows.size() - 1, current + delta))), x), shift);
            } else if (!ctrl && character >= 32 && character != 127 && !ModularUI.Mods.LWJGL3IFY.isLoaded()) {
                insert(String.valueOf(character));
            }
        ensureVisible();
        return Result.SUCCESS;
    }

    @Override
    public Result onKeyEvent(InputEvents.KeyEvent event) {
        return isFocused() && inputGate.getAsBoolean() ? Result.SUCCESS : Result.IGNORE;
    }

    @Override
    public boolean onTextInput(InputEvents.TextEvent event) {
        if (!isFocused() || !inputGate.getAsBoolean()) return false;
        if (editable.getAsBoolean()) insert(event.text);
        return true;
    }

    public boolean insert(String text) {
        try {
            buffer.insert(text);
            refresh();
            return true;
        } catch (IllegalArgumentException rejected) {
            error.accept(rejected.getMessage());
            return false;
        }
    }

    private void refresh() {
        if (diagnostic != null && !source().equals(diagnosticSource)) diagnostic(null);
        if (!source().equals(highlightedSource)) {
            highlighted = SfmlHighlighter.highlight(source());
            highlightedSource = source();
        }
        cachedWidth = -1;
        changed.run();
        if (isValid()) ensureVisible();
    }

    public void refreshAfterCommand() {
        refresh();
        ensureVisible();
    }

    private int word(boolean forward) {
        int offset = buffer.caret();
        while (forward ? offset < source().length() : offset > 0) {
            int next = forward ? buffer.next(offset) : buffer.previous(offset);
            char c = source().charAt(forward ? offset : next);
            offset = next;
            if (Character.isWhitespace(c)) break;
        }
        return offset;
    }

    private int lineStart(int offset) {
        return source().lastIndexOf('\n', Math.max(-1, offset - 1)) + 1;
    }

    private int lineEnd(int offset) {
        int end = source().indexOf('\n', offset);
        return end < 0 ? source().length() : end;
    }

    private int lineAt(int offset) {
        int line = 0;
        for (int i = 0; i < offset; i++) if (source().charAt(i) == '\n') line++;
        return line;
    }

    private void newline() {
        int start = lineStart(buffer.caret());
        String before = source().substring(start, buffer.caret());
        int indent = leading(before);
        String upper = before.trim()
            .toUpperCase(Locale.ROOT);
        if (upper.endsWith(" DO") || upper.endsWith(" THEN") || upper.equals("ELSE")) indent += 2;
        insert("\n" + spaces(indent));
    }

    public void autoIndent() {
        int oldAnchor = buffer.anchor(), oldCaret = buffer.caret();
        String[] lines = source().split("\n", -1);
        int depth = 0;
        long characters = lines.length - 1;
        for (int i = 0; i < lines.length; i++) {
            SfmlHighlighter.Line styled = highlighted.lines.get(i);
            StringBuilder code = new StringBuilder();
            for (SfmlHighlighter.Span span : styled.spans)
                if (span.style != SfmlHighlighter.Style.STRING && span.style != SfmlHighlighter.Style.COMMENT)
                    code.append(styled.text, span.start, span.end);
            String text = lines[i].substring(leading(lines[i])), upper = code.toString()
                .trim()
                .toUpperCase(Locale.ROOT);
            boolean end = upper.equals("END"), branch = upper.equals("ELSE") || upper.startsWith("ELSE IF ");
            if (end || branch) depth = Math.max(0, depth - 1);
            boolean protectedString = styled.styleAt(0) == SfmlHighlighter.Style.STRING;
            characters += protectedString ? lines[i].length() : text.isEmpty() ? 0 : depth * 2L + text.length();
            if (characters > SfmlEditorBuffer.MAX_CHARACTERS) {
                error.accept("Indent would exceed the 65536-character editor limit; source was kept unchanged");
                return;
            }
            if (!protectedString) lines[i] = text.isEmpty() ? "" : spaces(depth * 2) + text;
            if (branch || upper.endsWith(" DO") || upper.endsWith(" THEN")) depth++;
        }
        buffer.select(0, source().length());
        if (!insert(String.join("\n", lines))) buffer.select(oldAnchor, oldCaret);
    }

    public void snippet(SyntaxSnippet snippet) {
        if (!editable.getAsBoolean() || snippet == null) return;
        int oldAnchor = buffer.anchor(), cursor = buffer.caret(), start = cursor, end = cursor;
        String text = source();
        int[] placeholder = SyntaxSnippet.placeholderAt(text, cursor);
        if (placeholder != null) {
            start = placeholder[0];
            end = placeholder[1];
        } else while (start > lineStart(cursor) && Character.isLetterOrDigit(text.charAt(start - 1))) start--;
        int indent = leading(text.substring(lineStart(cursor), cursor));
        buffer.select(start, end);
        boolean accepted = insert(
            snippet.getTemplate()
                .replace("\n", "\n" + spaces(indent)));
        if (!accepted) {
            buffer.select(oldAnchor, cursor);
            return;
        }
        buffer.move(start, false);
        placeholder(true);
        if (isValid()) getContext().focus(this);
    }

    public boolean placeholder(boolean forward) {
        Matcher matcher = SyntaxSnippet.PLACEHOLDER_PATTERN.matcher(source());
        int first = -1, last = -1, found = -1;
        int from = forward ? buffer.end() : buffer.start();
        while (matcher.find()) {
            if (first < 0) first = matcher.start();
            last = matcher.start();
            if (forward && found < 0 && matcher.start() >= from) found = matcher.start();
            if (!forward && matcher.start() < from) found = matcher.start();
        }
        if (found < 0) found = forward ? first : last;
        if (found < 0) return false;
        matcher.find(found);
        buffer.select(matcher.start(), matcher.end());
        if (isValid()) ensureVisible();
        return true;
    }

    private int caretRow() {
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            if (buffer.caret() >= row.offset && buffer.caret() <= row.end
                && (buffer.caret() < row.end || i == rows.size() - 1 || rows.get(i + 1).line != row.line)) return i;
        }
        return Math.max(0, rows.size() - 1);
    }

    private void ensureVisible() {
        measure();
        int row = caretRow(), y = row * LINE_HEIGHT;
        if (y < getScrollY()) getScrollArea().getScrollY()
            .scrollTo(getScrollArea(), y);
        else if (y + 16 > getScrollY() + getArea().h()) getScrollArea().getScrollY()
            .scrollTo(getScrollArea(), y + 16 - getArea().h());
        if (wrap) getScrollArea().getScrollX()
            .scrollTo(getScrollArea(), 0);
        else {
            int x = font().getStringWidth(source().substring(rows.get(row).offset, buffer.caret()));
            if (x < getScrollX()) getScrollArea().getScrollX()
                .scrollTo(getScrollArea(), x);
            else if (x + gutter + 15 > getScrollX() + getArea().w()) getScrollArea().getScrollX()
                .scrollTo(getScrollArea(), x + gutter + 15 - getArea().w());
        }
    }

    private static int leading(String text) {
        int i = 0;
        while (i < text.length() && text.charAt(i) == ' ') i++;
        return i;
    }

    private static String spaces(int count) {
        return String.join("", Collections.nCopies(Math.max(0, count), " "));
    }
}
