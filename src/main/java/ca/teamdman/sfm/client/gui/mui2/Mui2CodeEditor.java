package ca.teamdman.sfm.client.gui.mui2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ChatComponentText;

import org.lwjgl.input.Keyboard;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.TextWidget;

import ca.teamdman.sfm.client.blueprint.BlueprintDocument;
import ca.teamdman.sfm.client.gui.ProgramEditSession;
import ca.teamdman.sfm.client.gui.ProgramEditState;
import ca.teamdman.sfm.client.gui.syntax.SnippetCategory;
import ca.teamdman.sfm.client.gui.syntax.SyntaxRegistry;
import ca.teamdman.sfm.client.gui.syntax.SyntaxSnippet;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.net.PacketProgramEditReply;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;

/** Native MUI2 secondary authoring panel. The caller keeps the manager screen/container open. */
public final class Mui2CodeEditor implements ProgramEditSession.Receiver {

    private static int savedWidth = 520, savedHeight = 320;
    private static boolean savedWrap;
    private final TileEntityManager manager;
    private final ProgramEditSession session;
    private final Runnable closeAction;
    private final String initialDraft;
    private String rejectedImport;
    private String pendingLayoutNotice = "";
    private final SfmlCodeEditorWidget editor;
    private final List<ButtonWidget<?>> categoryButtons = new ArrayList<>();
    private int headerWidth = -1;
    private ModularPanel panel;
    private IPanelHandler snippetPanel;
    private List<SyntaxSnippet> suggestions = Collections.emptyList();
    private int suggestionIndex, validationDelay = -1;
    private String status = "Loading program from server", loadedDraft = "";
    private boolean opened, cancelled, closeAfterSave, saveUnconfirmed, importedUnsaved;

    public Mui2CodeEditor(TileEntityManager manager, int windowId, String initialDraft, Runnable closeAction) {
        this.manager = manager;
        this.initialDraft = initialDraft;
        importedUnsaved = initialDraft != null;
        this.closeAction = closeAction;
        session = new ProgramEditSession(manager, windowId);
        editor = new SfmlCodeEditorWidget(
            () -> validationDelay = 4,
            () -> save(false),
            this::suggest,
            this::close,
            this::editable,
            error -> status = error);
        editor.onDiagnostic(this::jumpToError);
        try {
            editor.load(initialDraft == null ? "" : initialDraft);
        } catch (IllegalArgumentException oversized) {
            rejectedImport = initialDraft;
            status = importLimitMessage();
        }
        if (savedWrap) editor.toggleWrap();
    }

    public ModularPanel build() {
        if (panel != null) return panel;
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int initialWidth = Math.min(savedWidth, (int) (resolution.getScaledWidth() * .98f));
        panel = new ModularPanel("sfm_code_editor") {

            @Override
            public boolean disablePanelsBelow() {
                return true;
            }

            @Override
            public int getMinDragWidth() {
                return 300;
            }

            @Override
            public int getMinDragHeight() {
                return 220;
            }

            @Override
            public void beforeResize(boolean onOpen) {
                super.beforeResize(onOpen);
                if (getArea().w() > getParentArea().w() * .98f) width((int) (getParentArea().w() * .98f));
                if (getArea().h() > getParentArea().h() * .98f) height((int) (getParentArea().h() * .98f));
            }

            @Override
            public void postResize() {
                super.postResize();
                if (layoutHeader(getArea().w())) scheduleResize();
                int x = Math.max(2, Math.min(getArea().rx, getParentArea().w() - getArea().w() - 2));
                int y = Math.max(2, Math.min(getArea().ry, getParentArea().h() - getArea().h() - 2));
                if (x != getArea().rx || y != getArea().ry) {
                    left(x).top(y);
                    scheduleResize();
                }
            }

            @Override
            public void closeIfOpen() {
                if (!canClose()) {
                    status = "Saving - wait for server confirmation";
                    return;
                }
                super.closeIfOpen();
            }

            @Override
            public boolean onKeyPressed(char character, int key) {
                if (key == Keyboard.KEY_ESCAPE) {
                    close();
                    return true;
                }
                if (key == Keyboard.KEY_F4) {
                    jumpToError();
                    return true;
                }
                if (GuiScreen.isCtrlKeyDown() && key == Keyboard.KEY_S) {
                    save(false);
                    return true;
                }
                return super.onKeyPressed(character, key);
            }
        }.size(initialWidth, Math.min(savedHeight, (int) (resolution.getScaledHeight() * .98f)))
            .resizeableOnDrag(true)
            .onCloseAction(this::cancel);
        panel.child(
            new TextWidget<>("SFML text editor").left(8)
                .top(7)
                .height(12)
                .width(150));
        panel.child(button("Copy", 8, 23, 36, () -> {
            GuiScreen.setClipboardString(draft());
            status = "Draft copied";
        }));
        panel.child(
            new ButtonWidget<>().pos(48, 23)
                .size(52, 20)
                .overlay(IKey.dynamic(() -> "Wrap: " + (editor.wraps() ? "ON" : "OFF")))
                .onMousePressed(mouse -> {
                    if (mouse != 0) return false;
                    editor.toggleWrap();
                    savedWrap = editor.wraps();
                    return true;
                }));
        panel.child(
            new GuardedButton("Indent", this::editable, editor::autoIndent).pos(104, 23)
                .size(40, 20));
        panel.child(
            new GuardedButton("Suggest", this::editable, this::suggest).pos(148, 23)
                .size(48, 20));
        panel.child(
            new GuardedButton("Undo", this::editable, editor::undo).pos(200, 23)
                .size(42, 20));
        panel.child(
            new GuardedButton("Redo", this::editable, editor::redo).pos(246, 23)
                .size(42, 20));
        for (SnippetCategory category : SnippetCategory.values()) {
            ButtonWidget<?> categoryButton = new GuardedButton(
                category.getDisplayName(),
                this::editable,
                () -> category(category));
            categoryButton.addTooltipLine(IKey.str(category.getDescription()));
            categoryButtons.add(categoryButton);
            panel.child(categoryButton);
        }
        layoutHeader(initialWidth);
        panel.child(
            editor.left(8)
                .right(8)
                .top(new HeaderLayout(initialWidth, categoryButtons.size()).editorTop)
                .bottom(61));
        panel.child(
            new DiagnosticStatusWidget(this::statusLine, () -> status, this::jumpToError).left(8)
                .right(8)
                .bottom(43)
                .height(12));
        panel.child(
            new TextWidget<>(
                IKey.dynamic(
                    () -> "Ln " + (editor.line() + 1)
                        + ", Col "
                        + (editor.column() + 1)
                        + " | Ctrl+S Save | Ctrl+Space")).left(8)
                            .right(8)
                            .bottom(28)
                            .height(12));
        panel.child(
            new GuardedButton("Save", this::editable, () -> save(false)).right(180)
                .bottom(6)
                .size(90, 20));
        panel.child(
            new GuardedButton("Save & Close", this::editable, () -> save(true)).right(84)
                .bottom(6)
                .size(90, 20));
        panel.child(
            new GuardedButton("Cancel", this::canClose, this::close).right(8)
                .bottom(6)
                .size(70, 20));
        snippetPanel = IPanelHandler.simple(panel, (parent, player) -> buildSuggestions(), true);
        return panel;
    }

    /** Called once per client screen tick by the manager screen. */
    public void tick() {
        if (cancelled) return;
        if (!opened) {
            opened = true;
            session.open();
        }
        if (session.state.tick()) status = session.state.action() == ProgramEditState.Action.SAVE
            ? "No server confirmation; save may have applied. Draft kept. Copy before leaving."
            : "Program could not be loaded. Draft kept and can be copied; reopen to retry.";
        if (validationDelay >= 0 && --validationDelay == 0 && !session.state.isPending()) validate();
    }

    @Override
    public void receiveProgramEditReply(PacketProgramEditReply reply) {
        if (cancelled) return;
        ProgramEditState.Action action = session.state.action();
        if (!session.receive(reply)) return;
        if (action == ProgramEditState.Action.SAVE) saveUnconfirmed = false;
        if (!reply.accepted || reply.snapshot == null) {
            status = reply.message;
            return;
        }
        if (action == ProgramEditState.Action.OPEN) {
            if (rejectedImport != null) {
                status = importLimitMessage();
                return;
            }
            editor.load(initialDraft != null ? initialDraft : BlueprintDocument.withoutMetadata(reply.snapshot.source));
            loadedDraft = editor.source();
            validate();
        } else {
            loadedDraft = editor.source();
            importedUnsaved = false;
            status = pendingLayoutNotice.isEmpty() ? "Saved" : "Saved. " + pendingLayoutNotice;
            if (closeAfterSave) closeAction.run();
        }
    }

    public String draft() {
        return rejectedImport != null ? rejectedImport : editor.source();
    }

    public boolean hasDraft() {
        return importedUnsaved || !draft().equals(loadedDraft);
    }

    public boolean canClose() {
        return !session.state.isPending() || session.state.action() != ProgramEditState.Action.SAVE;
    }

    public boolean isSaving() {
        return !canClose();
    }

    /** Forced screen/container loss: keep source available to the caller, reject late replies. */
    public void cancel() {
        if (cancelled) return;
        cancelled = true;
        session.state.cancel();
        if (editor.isValid() && editor.isFocused()) editor.getContext()
            .removeFocus();
        if (panel != null && panel.isValid()) {
            savedWidth = panel.getArea()
                .w();
            savedHeight = panel.getArea()
                .h();
        }
        if (saveUnconfirmed && Minecraft.getMinecraft().thePlayer != null)
            Minecraft.getMinecraft().thePlayer.addChatMessage(
                new ChatComponentText(
                    "[SFM] Save unconfirmed; the server may have saved the program. Your editor draft has been retained."));
    }

    public void close() {
        if (!canClose()) {
            status = "Saving - wait for server confirmation";
            return;
        }
        closeAction.run();
    }

    private boolean editable() {
        return !cancelled && rejectedImport == null && session.state.isEditable();
    }

    private void validate() {
        validationDelay = -1;
        if (rejectedImport != null) {
            status = importLimitMessage();
            return;
        }
        if (draft().trim()
            .isEmpty()) {
            status = "Empty program";
            return;
        }
        ProgramBuildResult result = new ProgramBuilder(draft()).build();
        status = result.isSuccess() ? "Valid program" : result.getErrorMessage();
    }

    private boolean jumpToError() {
        if (!editable()) return false;
        // Revalidate the current draft; an older displayed error may refer to edited text.
        validate();
        return editor.jumpToDiagnostic(status);
    }

    private void save(boolean close) {
        if (!editable()) return;
        closeAfterSave = close;
        try {
            String source = draft();
            // Entering and leaving Text without editing preserves exact source and its existing layout sidecar.
            pendingLayoutNotice = "";
            if (initialDraft == null && source.equals(loadedDraft)) source = session.state.snapshot().source;
            else {
                BlueprintDocument.TextSave prepared = BlueprintDocument
                    .prepareTextSave(source, session.state.snapshot().source);
                source = prepared.source;
                pendingLayoutNotice = prepared.notice;
            }
            session.save(source);
            saveUnconfirmed = true;
            status = "Saving - waiting for the server";
        } catch (IllegalArgumentException | IllegalStateException error) {
            session.state.cancel();
            status = error.getMessage();
        }
    }

    private Set<String> labels() {
        return manager.getDisk() == null ? Collections.emptySet()
            : LabelPositionHolder.from(manager.getDisk())
                .getAllLabels()
                .keySet();
    }

    private void suggest() {
        if (!editable()) return;
        showSuggestions(SyntaxRegistry.getContextualSuggestions(draft(), editor.line(), editor.column(), labels()));
    }

    private void category(SnippetCategory category) {
        List<SyntaxSnippet> list = new ArrayList<>(SyntaxRegistry.getSnippetsForCategory(category));
        if (category == SnippetCategory.LABEL) list.addAll(0, SyntaxRegistry.getLabelSnippets(labels()));
        showSuggestions(list);
    }

    private void showSuggestions(List<SyntaxSnippet> list) {
        if (snippetPanel == null || snippetPanel.isPanelOpen()) return;
        if (editor.isValid()) editor.getContext()
            .removeFocus();
        suggestions = list;
        suggestionIndex = 0;
        snippetPanel.deleteCachedPanel();
        snippetPanel.openPanel();
    }

    private ModularPanel buildSuggestions() {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        ModularPanel popup = new ModularPanel("sfm_code_suggestions") {

            @Override
            public boolean disablePanelsBelow() {
                return true;
            }

            @Override
            public boolean closeOnOutOfBoundsClick() {
                return true;
            }

            @Override
            public boolean onKeyPressed(char character, int key) {
                if (key == Keyboard.KEY_ESCAPE) {
                    snippetPanel.closePanel();
                    return true;
                }
                if (key == Keyboard.KEY_UP || key == Keyboard.KEY_DOWN) {
                    suggestionIndex = Math
                        .max(0, Math.min(suggestions.size() - 1, suggestionIndex + (key == Keyboard.KEY_UP ? -1 : 1)));
                    return true;
                }
                if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_TAB) {
                    insertSuggestion(suggestionIndex);
                    return true;
                }
                return super.onKeyPressed(character, key);
            }
        }.onCloseAction(
            () -> {
                if (!cancelled && editor.isValid()) editor.getContext()
                    .focus(editor);
            })
            .size(
                Math.min(312, (int) (resolution.getScaledWidth() * .98f)),
                Math.min(228, (int) (resolution.getScaledHeight() * .98f)));
        popup.child(
            new TextWidget<>("Snippets | Enter inserts | Esc closes").left(8)
                .right(8)
                .top(7)
                .height(12));
        ListWidget<IWidget, ?> list = new ListWidget<>().left(8)
            .right(8)
            .top(24)
            .bottom(31);
        for (int i = 0; i < suggestions.size(); i++) {
            final int index = i;
            SyntaxSnippet snippet = suggestions.get(i);
            list.child(
                new ButtonWidget<>().widthRel(1f)
                    .height(28)
                    .addTooltipLine(IKey.str(snippet.getDescription()))
                    .addTooltipLine(IKey.str(snippet.getTemplate()))
                    .overlay(
                        IKey.dynamic(
                            () -> Minecraft.getMinecraft().fontRenderer.trimStringToWidth(
                                (suggestionIndex == index ? "> " : "") + snippet.getTitle(),
                                Math.max(
                                    40,
                                    popup.getArea()
                                        .w() - 30))
                                + "\n"
                                + Minecraft.getMinecraft().fontRenderer.trimStringToWidth(
                                    snippet.getDescription(),
                                    Math.max(
                                        40,
                                        popup.getArea()
                                            .w() - 30))))
                    .onMousePressed(mouse -> {
                        if (mouse != 0) return false;
                        insertSuggestion(index);
                        return true;
                    }));
        }
        popup.child(list);
        popup.child(
            new ButtonWidget<>().right(8)
                .bottom(6)
                .size(70, 20)
                .overlay(IKey.str("Close"))
                .onMousePressed(mouse -> {
                    if (mouse != 0) return false;
                    snippetPanel.closePanel();
                    return true;
                }));
        return popup;
    }

    private void insertSuggestion(int index) {
        if (index < 0 || index >= suggestions.size()) return;
        snippetPanel.closePanel();
        editor.snippet(suggestions.get(index));
    }

    private String statusLine() {
        int newline = status.indexOf('\n');
        String first = newline < 0 ? status : status.substring(0, newline);
        return Minecraft.getMinecraft().fontRenderer.trimStringToWidth(
            first,
            Math.max(
                40,
                panel == null ? 280
                    : panel.getArea()
                        .w() - 20));
    }

    private static String importLimitMessage() {
        return "Imported draft exceeds the 65536-character editor limit. Copy keeps the original; shorten it before importing.";
    }

    /** Reflows only when the panel width changes, including drag resize and GUI-scale changes. */
    private boolean layoutHeader(int width) {
        if (headerWidth == width || categoryButtons.size() != SnippetCategory.values().length) return false;
        headerWidth = width;
        HeaderLayout layout = new HeaderLayout(width, categoryButtons.size());
        for (int i = 0; i < categoryButtons.size(); i++) categoryButtons.get(i)
            .pos(layout.x(i), layout.y(i))
            .size(layout.buttonWidth, 20);
        editor.top(layout.editorTop);
        return true;
    }

    /** Pure geometry: balanced rows leave every category visible even at 320x240 logical resolution. */
    public static final class HeaderLayout {

        public final int columns, buttonWidth, editorTop;

        public HeaderLayout(int panelWidth, int count) {
            int available = Math.max(1, panelWidth - 16);
            int capacity = Math.max(1, (available + 4) / 56);
            int rows = Math.max(1, (count + capacity - 1) / capacity);
            columns = Math.max(1, (count + rows - 1) / rows);
            buttonWidth = Math.max(1, (available - (columns - 1) * 4) / columns);
            editorTop = 47 + rows * 24 + 1;
        }

        public int x(int index) {
            return 8 + (index % columns) * (buttonWidth + 4);
        }

        public int y(int index) {
            return 47 + (index / columns) * 24;
        }
    }

    private static ButtonWidget<?> button(String label, int x, int y, int width, Runnable action) {
        return new ButtonWidget<>().pos(x, y)
            .size(width, 20)
            .overlay(IKey.str(label))
            .onMousePressed(mouse -> {
                if (mouse != 0) return false;
                action.run();
                return true;
            });
    }

    /** MUI2 setEnabled hides widgets; unavailable actions instead stay visible and reject input. */
    private static final class GuardedButton extends ButtonWidget<GuardedButton> {

        private final BooleanSupplier available;

        GuardedButton(String label, BooleanSupplier available, Runnable action) {
            this.available = available;
            overlay(
                IKey.str(label)
                    .color(() -> available.getAsBoolean() ? 0xffeeeeee : 0xff777777));
            onMousePressed(mouse -> {
                if (mouse != 0) return false;
                action.run();
                return true;
            });
        }

        @Override
        public Result onMousePressed(int mouse) {
            return available.getAsBoolean() ? super.onMousePressed(mouse) : Result.STOP;
        }
    }
}
