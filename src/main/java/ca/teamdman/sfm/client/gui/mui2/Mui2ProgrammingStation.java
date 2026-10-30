package ca.teamdman.sfm.client.gui.mui2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.sizer.Unit;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.StudioDocument;
import ca.teamdman.sfm.client.blueprint.StudioNodeProperties;
import ca.teamdman.sfm.client.gui.ProgrammingStationDraftState;
import ca.teamdman.sfm.client.gui.syntax.SfmlCompletionCatalog;
import ca.teamdman.sfm.client.gui.syntax.SnippetCategory;
import ca.teamdman.sfm.client.gui.syntax.StudioCompletion;
import ca.teamdman.sfm.client.gui.syntax.SyntaxRegistry;
import ca.teamdman.sfm.client.gui.syntax.SyntaxSnippet;
import ca.teamdman.sfm.common.net.PacketStationDebugReply;
import ca.teamdman.sfm.common.net.PacketStationReply;
import ca.teamdman.sfm.common.net.PacketStationRequest;
import ca.teamdman.sfm.common.net.PacketStationRequest.Action;
import ca.teamdman.sfm.common.net.SFMNetwork;
import ca.teamdman.sfm.common.program.project.ProjectDraft;
import ca.teamdman.sfm.common.program.project.ProjectPage;
import ca.teamdman.sfm.common.program.project.ProjectWorkspace;
import ca.teamdman.sfm.common.program.project.PublicationPage;
import ca.teamdman.sfm.common.program.project.PublicationReceipt;
import ca.teamdman.sfm.common.tileentity.TileEntityProgrammingStation;
import ca.teamdman.sfm.common.util.BoundedUtf8;

/** Client authoring only. Saving and validation are separate exact-revision server requests. */
public final class Mui2ProgrammingStation {

    /** Client discovery only. The server independently validates session, ownership, exact build and held disk. */
    public static final boolean REFERENCE_DISKS_ENABLED = true;
    private static final int CATALOG_ROWS = 8, BUILD_ROWS = 6, MAX_RECOVERED_DRAFTS = 16;
    // Values contain immutable text/UUIDs only, so they do not retain their weak world keys.
    private static final Map<World, LinkedHashMap<String, Retained>> RECOVERY = new WeakHashMap<>();
    private static boolean savedWrap;
    private static boolean advancedTools;
    private static double savedSplit = .5;
    private static double savedDock = .28;
    private static int savedSidebar = 154;
    private static int savedInspector = 150;
    private static StudioProjectMemory projectMemory;
    private final TileEntityProgrammingStation station;
    private final UUID sessionId;
    private final int windowId;
    private final Runnable closeAction;
    private final ProgrammingStationDraftState state = new ProgrammingStationDraftState();
    private final StudioDocument document = new StudioDocument();
    private final SfmlCodeEditorWidget editor;
    private final String scope;
    private final String memoryScope;
    private final UUID rememberedProject;
    private ModularPanel panel;
    private final List<ButtonWidget<?>> categories = new ArrayList<>();
    private int layoutWidth = -1, layoutHeight = -1, diagnosticDelay = -1;
    private String draftDiagnostic = "Draft not checked";
    private String snippetSearch = "";
    private SnippetCategory snippetCategory;

    private enum View {
        VISUAL,
        SPLIT,
        CODE
    }

    private View view = View.CODE;
    private boolean codeFocused = true;
    private FieldDraft fieldDraft;

    /** Unapplied typed fields belong to one exact project/node clause, including during recovery. */
    private static final class FieldDraft {

        final UUID project, node;
        final String original;
        final Map<String, String> values = new LinkedHashMap<>();
        final Map<String, String> initial = new LinkedHashMap<>();

        FieldDraft(UUID project, Node node, List<StudioNodeProperties.Field> fields) {
            this.project = project;
            this.node = node.id;
            original = node.clause();
            for (StudioNodeProperties.Field field : fields) values.put(field.getKey(), field.getValue());
            initial.putAll(values);
        }

        FieldDraft(FieldDraft from) {
            project = from.project;
            node = from.node;
            original = from.original;
            values.putAll(from.values);
            initial.putAll(from.initial);
        }

        boolean dirty() {
            return !values.equals(initial);
        }
    }

    private StudioGraphWidget graphEditor;
    private StudioDividerWidget divider;
    private ParentWidget<?> outlinePane, inspectorPane, insertTray;
    private ParentWidget<?> dockPane, problemDock, terminalDock, watchDock, buildDock;
    private StudioReadOnlyPane problemOutput;
    private StudioDebugTerminal terminal;
    private StudioDividerWidget dockDivider, sidebarDivider, inspectorDivider;
    private StudioLayout.Workspace workspace;
    private TextFieldWidget nameField;
    private TextWidget<?> authoringLabel;
    private ButtonWidget<?> projectSelector, toolsButton, saveButton, checkButton, imprintButton, allInsert, apiInsert;
    private ButtonWidget<?> publishDockButton, refreshBuildsButton, imprintDockButton;
    private final List<ButtonWidget<?>> viewButtons = new ArrayList<>(), dockTabs = new ArrayList<>();
    private String lastProblemDiagnostic, lastProblemStatus;
    private ProjectDraft lastProblemSaved;
    private Object lastProblemValidation;
    private boolean lastProblemDirty;
    private boolean refreshCatalog;

    private enum Dock {
        PROBLEMS,
        TERMINAL,
        WATCH,
        BUILDS
    }

    private Dock dock = Dock.PROBLEMS;
    private ListWidget<IWidget, ?> outlineList;
    private TextFieldWidget clauseField;
    private String clause = "";
    private UUID inspected;
    private boolean syncingEditor;
    private String completionSource;
    private int completionSelection;
    private ListWidget<IWidget, ?> completionList;
    private List<StudioCompletion.Candidate> completions = Collections.emptyList();
    private SfmlCompletionCatalog.Snapshot symbols = SfmlCompletionCatalog.scan("");
    private IPanelHandler nodesPanel, apiPanel, problemsPanel, inspectPanel, fieldsPanel, completionPanel, findPanel;
    private IPanelHandler catalogPanel, confirmationPanel, snippetsPanel, toolsPanel, buildsPanel;
    private ProjectPage catalog = new ProjectPage(Collections.emptyList(), 0, 0);
    private PublicationPage publications = new PublicationPage(Collections.emptyList(), 0, 0);
    private String status = "New local draft. Name it, then Create to save a project.";
    private boolean cancelled, opened;
    private Runnable protectedAction;
    private String protectedLabel;
    private Retained loadingRecovery;
    private boolean recoveryOverCurrent;

    private static final class Retained {

        final ProjectDraft saved;
        final UUID localId;
        final String name, source;
        final boolean uncertain;
        final StudioDocument.Snapshot visual;
        final UUID selected;
        final String clause;
        final View view;
        final FieldDraft fields;

        Retained(Mui2ProgrammingStation studio) {
            ProgrammingStationDraftState state = studio.state;
            saved = state.saved;
            localId = state.localId;
            name = state.name;
            source = state.source;
            uncertain = state.uncertainWrite
                || state.awaitingReply() && (state.action() == Action.CREATE || state.action() == Action.SAVE);
            visual = studio.document.snapshot();
            selected = studio.inspected;
            clause = studio.clause;
            view = studio.view;
            fields = studio.fieldDraft == null ? null : new FieldDraft(studio.fieldDraft);
        }
    }

    public Mui2ProgrammingStation(TileEntityProgrammingStation station, UUID sessionId, int windowId,
        Runnable closeAction) {
        this.station = station;
        this.sessionId = sessionId;
        this.windowId = windowId;
        this.closeAction = closeAction;
        scope = Minecraft.getMinecraft().thePlayer
            .getUniqueID() + ":" + station.xCoord + ":" + station.yCoord + ":" + station.zCoord + ":";
        Minecraft mc = Minecraft.getMinecraft();
        String location = mc.isSingleplayer() && mc.getIntegratedServer() != null ? "world:" + mc.getIntegratedServer()
            .getFolderName()
            : mc.func_147104_D() == null ? "world:" + station.getWorldObj()
                .getWorldInfo()
                .getWorldName() : "server:" + mc.func_147104_D().serverIP.toLowerCase(Locale.ROOT);
        memoryScope = StudioProjectMemory.scope(location, mc.thePlayer.getUniqueID());
        if (projectMemory == null) projectMemory = new StudioProjectMemory(
            new java.io.File(mc.mcDataDir, "config/sfm-studio-projects.properties").toPath());
        rememberedProject = projectMemory.selected(memoryScope);
        Retained recovered = rememberedProject == null ? latestRecovery() : cache().get(key(rememberedProject));
        if (recovered != null) restore(recovered);
        else document.load(state.source);
        editor = new SfmlCodeEditorWidget(
            this::codeChanged,
            this::save,
            this::complete,
            this::close,
            this::codeEditable,
            error -> status = error);
        editor.onDiagnostic(this::jumpToError);
        editor.inputGate(this::authoringOnTop);
        editor.onFocused(() -> codeFocused = true);
        editor.onFind(this::openFind);
        syncEditor();
        if (savedWrap) editor.toggleWrap();
    }

    private String editorSource() {
        return document.code();
    }

    private void codeChanged() {
        if (syncingEditor || editor == null) return;
        if (editor.source()
            .equals(document.code())) return;
        if (document.incomplete() || pendingClause() || pendingFields()) {
            syncEditor();
            status = "Visual draft kept. Finish it or Undo before editing the code.";
            return;
        }
        document.editCode(editor.source());
        codeFocused = true;
        state.noteLocalEdit();
        state.source = document.source();
        if (!document.code()
            .equals(editor.source())) syncEditor();
        diagnosticDelay = 6;
        draftDiagnostic = "Draft changed; checking...";
    }

    private void syncEditor() {
        if (editor == null) return;
        syncingEditor = true;
        try {
            editor.load(document.code());
        } finally {
            syncingEditor = false;
        }
        diagnosticDelay = 6;
    }

    public ModularPanel build() {
        if (panel != null) return panel;
        panel = new StudioScaledPanel("sfm_station_editor") {

            @Override
            public boolean disablePanelsBelow() {
                return true;
            }

            @Override
            public void postResize() {
                super.postResize();
                if (layoutHeader(getArea().w())) {
                    scheduleResize();
                }
            }

            @Override
            public boolean onKeyPressed(char character, int key) {
                if (key == Keyboard.KEY_F4) {
                    jumpToError();
                    return true;
                }
                if (GuiScreen.isCtrlKeyDown() && key == Keyboard.KEY_S) {
                    save();
                    return true;
                }
                if (key == Keyboard.KEY_ESCAPE) {
                    if (graphEditor != null && graphEditor.cancelWire()) return true;
                    close();
                    return true;
                }
                return super.onKeyPressed(character, key);
            }
        }.width(() -> StudioScaledPanel.logicalWidth(panel.getScreen()), Unit.Measure.PIXEL)
            .height(() -> StudioScaledPanel.logicalHeight(panel.getScreen()), Unit.Measure.PIXEL)
            .pos(0, 0)
            .background(StudioTheme.SHELL)
            .excludeAreaInRecipeViewer()
            .resizeableOnDrag(false)
            .onCloseAction(this::cancel);
        panel.child(
            new TextWidget<>("SFM Studio").color(0xffefa09a)
                .pos(8, 8)
                .size(70, 12));
        insertTray = new ParentWidget<>().background(StudioTheme.INSET);
        panel.child(insertTray);
        projectSelector = button("", 82, 3, 100, () -> true, this::openCatalog).height(21)
            .overlay(
                IKey.dynamic(() -> trimFor(state.name + " v", projectSelector.getArea().width - 10))
                    .color(0xffe7e8eb))
            .addTooltipLine("Current project. Browse and switch in the Projects column.");
        panel.child(projectSelector);
        toolsButton = button("Tools", 0, 3, 46, () -> true, () -> {
            removeFocus();
            toolsPanel.deleteCachedPanel();
            toolsPanel.openPanel();
        }).height(21)
            .overlay(
                IKey.dynamic(() -> advancedTools ? "Advanced" : "Essentials")
                    .scale(.75f))
            .addTooltipLine(
                "Tool mode and editor actions. Essentials uses typed node fields; Advanced also exposes raw clauses.");
        saveButton = button("Save", 0, 3, 46, this::editable, this::save).height(21)
            .background(StudioTheme.PRIMARY)
            .addTooltipLine("Ctrl+S. Save this draft to your private project.");
        checkButton = button("Check", 0, 3, 46, () -> !state.busy(), this::validate).height(21)
            .addTooltipLine("Checks the exact saved server revision. Save edits first.");
        imprintButton = button("Imprint disk", 0, 3, 80, () -> true, this::openBuilds).height(21)
            .addTooltipLine("Select an immutable saved build in Build revisions, then imprint the held fresh disk.");
        panel.child(toolsButton);
        panel.child(saveButton);
        panel.child(checkButton);
        panel.child(imprintButton);
        panel.child(
            new TextWidget<>(
                IKey.dynamic(() -> state.busy() ? "SYNCING" : state.dirty() || visualDraft() ? "DRAFT *" : "SAVED"))
                    .color(0xffe6b399)
                    .pos(8, 29)
                    .size(55, 10));
        panel.child(
            new TextWidget<>("SAFE").color(0xff9bc0a5)
                .pos(66, 29)
                .size(28, 10)
                .addTooltipLine(
                    "Authoring mode. This station edits private projects; it does not execute a sandbox or install a running manager."));
        nameField = new TextFieldWidget() {

            @Override
            public Result onKeyPressed(char character, int key) {
                return editable() ? super.onKeyPressed(character, key) : Result.STOP;
            }
        }.setMaxLength(ProjectWorkspace.MAX_NAME_BYTES)
            .autoUpdateOnChange(true)
            .value(new StringValue.Dynamic(() -> state.name, value -> {
                if (editable() && !state.name.equals(value)) {
                    state.noteLocalEdit();
                    state.name = value;
                }
            }));
        panel.child(
            nameField.left(101)
                .right(8)
                .top(26)
                .height(16)
                .addTooltipLine("Project name"));
        allInsert = button("Insert / all", 0, 0, 65, this::editable, () -> {
            snippetCategory = null;
            snippets();
        }).height(16)
            .addTooltipLine("All supported syntax, functions and examples. Search or choose a category.");
        apiInsert = button("API", 0, 0, 44, this::editable, this::openApi).height(16);
        panel.child(allInsert);
        panel.child(apiInsert);
        for (SnippetCategory category : SnippetCategory.values()) {
            ButtonWidget<?> entry = button(category.getDisplayName(), 0, 0, 52, this::editable, () -> {
                snippetCategory = category;
                snippets();
            }).height(16)
                .addTooltipLine(category.getDescription());
            categories.add(entry);
            panel.child(entry.setEnabledIf(widget -> workspace != null && !workspace.compact));
        }
        viewButtons.add(viewButton("Visual", 0, 44, View.VISUAL));
        viewButtons.add(viewButton("Split", 0, 36, View.SPLIT));
        viewButtons.add(viewButton("Code", 0, 36, View.CODE));
        for (ButtonWidget<?> entry : viewButtons) panel.child(entry);
        authoringLabel = new TextWidget<>("AUTHORING").color(0xff868b96)
            .right(10)
            .top(68)
            .size(62, 10)
            .setEnabledIf(widget -> workspace != null && panel.getArea().width - workspace.mainLeft >= 290);
        panel.child(authoringLabel);
        panel.child(
            editor.left(8)
                .right(8)
                .top(90)
                .bottom(140));
        graphEditor = new StudioGraphWidget(
            document,
            this::graphEditable,
            this::graphChanged,
            this::selectionChanged,
            this::openNodes,
            text -> status = text);
        graphEditor.inputGate(this::authoringOnTop);
        graphEditor.onFocused(() -> codeFocused = false);
        panel.child(
            graphEditor.left(8)
                .right(8)
                .top(90)
                .bottom(140)
                .setEnabledIf(widget -> view != View.CODE));
        editor.setEnabledIf(widget -> view != View.VISUAL);
        divider = new StudioDividerWidget(() -> authoringOnTop() && view == View.SPLIT, x -> {
            if (workspace == null) return;
            int right = inspectorReserve();
            savedSplit = (double) (x - panel.getArea().x - workspace.mainLeft)
                / Math.max(1, panel.getArea().width - workspace.mainLeft - right);
            layoutWidth = -1;
            panel.scheduleResize();
        });
        panel.child(divider.setEnabledIf(widget -> view == View.SPLIT));
        buildWorkspacePanes();
        inspectorDivider = new StudioDividerWidget(this::authoringOnTop, x -> {
            savedInspector = panel.getArea().x + panel.getArea().width - x - 12;
            layoutWidth = -1;
            panel.scheduleResize();
        }, false, "Drag to resize Inspector. Node fields and source are preserved.");
        panel.child(
            inspectorDivider.setEnabledIf(widget -> view != View.CODE && workspace != null && workspace.inspector));
        buildDock();
        sidebarDivider = new StudioDividerWidget(this::authoringOnTop, x -> {
            savedSidebar = x - panel.getArea().x - 4;
            layoutWidth = -1;
            panel.scheduleResize();
        }, false, "Drag to resize Projects. Drafts and graph positions are preserved.");
        panel.child(sidebarDivider);
        dockDivider = new StudioDividerWidget(this::authoringOnTop, y -> {
            savedDock = (double) (panel.getArea().y + panel.getArea().height - 18 - y)
                / Math.max(1, panel.getArea().height);
            layoutWidth = -1;
            panel.scheduleResize();
        }, true, "Drag to resize the bottom dock. Problems, terminal, watch and build selection are preserved.");
        panel.child(dockDivider);
        panel.child(
            new TextWidget<>(IKey.dynamic(() -> trim(status))).color(0xffa8adb7)
                .left(8)
                .right(8)
                .bottom(3)
                .height(11)
                .addTooltipLine(IKey.dynamic(() -> status)));
        catalogPanel = IPanelHandler.simple(panel, (parent, player) -> buildCatalog(), true);
        confirmationPanel = IPanelHandler.simple(panel, (parent, player) -> buildConfirmation(), true);
        snippetsPanel = IPanelHandler.simple(panel, (parent, player) -> buildSnippets(), true);
        nodesPanel = IPanelHandler.simple(panel, (parent, player) -> buildNodes(), true);
        apiPanel = IPanelHandler.simple(panel, (parent, player) -> buildApi(), true);
        problemsPanel = IPanelHandler.simple(panel, (parent, player) -> buildProblems(), true);
        inspectPanel = IPanelHandler.simple(panel, (parent, player) -> buildInspectorPopup(), true);
        fieldsPanel = IPanelHandler.simple(panel, (parent, player) -> buildTypedFields(), true);
        completionPanel = IPanelHandler.simple(panel, (parent, player) -> buildCompletion(), true);
        findPanel = IPanelHandler.simple(panel, (parent, player) -> buildFind(), true);
        toolsPanel = IPanelHandler.simple(panel, (parent, player) -> buildTools(), true);
        buildsPanel = IPanelHandler.simple(panel, (parent, player) -> buildPublications(), true);
        if (document.graph() != null) {
            UUID recoveredSelection = inspected;
            String recoveredClause = clause;
            graphEditor.select(recoveredSelection);
            clause = recoveredClause;
            clauseField.setText(clause);
        }
        rebuildOutline();
        return panel;
    }

    private boolean layoutHeader(int width) {
        if (width == layoutWidth && panel.getArea().height == layoutHeight) return false;
        layoutWidth = width;
        layoutHeight = panel.getArea().height;
        workspace = StudioLayout.workspace(width, layoutHeight, savedSidebar, savedDock, categories.size() + 1);
        boolean narrow = width < 540;
        toolsButton.left(width - (narrow ? 148 : 232));
        saveButton.left(width - (narrow ? 99 : 183));
        checkButton.left(width - (narrow ? 50 : 134));
        imprintButton.left(width - 85)
            .top(narrow ? 26 : 3)
            .height(narrow ? 16 : 21);
        nameField.right(narrow ? 93 : 8);
        projectSelector.left(82)
            .width(Math.max(8, width - (narrow ? 234 : 318)));
        insertTray.pos(8, 43)
            .size(Math.max(1, width - 13), workspace.viewTop - 46);
        allInsert.pos(workspace.insertX(0), workspace.insertY(0))
            .width(workspace.insertWidth);
        apiInsert.pos(workspace.insertX(1), workspace.insertY(1))
            .width(workspace.insertWidth)
            .setEnabledIf(widget -> workspace.compact);
        for (int i = 0; i < categories.size(); i++) categories.get(i)
            .pos(workspace.insertX(i + 1), workspace.insertY(i + 1))
            .width(workspace.insertWidth);
        for (int i = 0, x = workspace.mainLeft; i < viewButtons.size(); i++) {
            int w = (i == 0 ? 70 : 62) - (workspace.compact ? 12 : 0);
            viewButtons.get(i)
                .pos(x, workspace.viewTop)
                .size(w, workspace.viewHeight);
            x += w + 3;
        }
        authoringLabel.top(workspace.viewTop + (workspace.viewHeight - 10) / 2);
        layoutWorkspace(width, workspace.editorTop);
        if (dockPane != null) {
            int tabWidth = Math.max(1, (width - workspace.mainLeft - 18) / 4);
            for (int i = 0; i < dockTabs.size(); i++) dockTabs.get(i)
                .pos(4 + i * tabWidth, 2)
                .size(tabWidth - 2, 18);
            int buildWidth = Math.max(1, Math.min(80, (width - workspace.mainLeft - 32) / 3));
            publishDockButton.left(4)
                .width(buildWidth);
            refreshBuildsButton.left(7 + buildWidth)
                .width(buildWidth);
            imprintDockButton.left(10 + buildWidth * 2)
                .width(buildWidth);
        }
        return true;
    }

    private boolean compactToolbar() {
        return workspace == null || workspace.compact;
    }

    private ModularPanel buildTools() {
        ModularPanel popup = modal("sfm_studio_tools", 312, 228);
        popup.child(
            new TextWidget<>("Editor tools").color(0xffefa09a)
                .pos(8, 8)
                .size(296, 12));
        String[] names = { "New draft", "Copy", "Undo", "Redo", "Indent", "Wrap", "Find / Replace", "Validate saved",
            "API reference", "Problems", "Terminal", "Saved builds" };
        Runnable[] commands = { () -> protect("New draft", this::newDraft), this::copy, () -> undo(false),
            () -> undo(true), () -> { if (codeEditable()) editor.autoIndent(); }, () -> {
                editor.toggleWrap();
                savedWrap = editor.wraps();
            }, this::openFind, this::validate, this::openApi, this::openProblems, this::openTerminal,
            this::openBuilds };
        for (int i = 0; i < names.length; i++) {
            Runnable command = commands[i];
            boolean readOnly = i == 1 || i == 9;
            popup.child(button(names[i], 8 + i % 2 * 150, 29 + i / 2 * 24, 146, () -> readOnly || !state.busy(), () -> {
                toolsPanel.closePanel();
                command.run();
            }).height(20));
        }
        popup.child(button("Done", 216, 174, 88, () -> true, toolsPanel::closePanel));
        popup.child(button("", 8, 174, 202, () -> !state.busy(), () -> {
            if (!applyClause()) return;
            removeFocus();
            advancedTools = !advancedTools;
        }).overlay(IKey.dynamic(() -> advancedTools ? "Use Essentials tools" : "Use Advanced tools")));
        popup.child(
            new TextWidget<>("Studio scale").pos(8, 202)
                .size(92, 12)
                .color(0xffb0b3bb));
        popup.child(button("-", 104, 199, 24, () -> true, () -> StudioScaledPanel.changeScale(panel.getScreen(), -1)));
        popup.child(
            new TextWidget<>(IKey.dynamic(StudioScaledPanel::scaleLabel)).pos(132, 202)
                .size(80, 12)
                .color(0xffe7e8eb)
                .addTooltipLine("Studio only. Larger values make controls bigger; window size limits the maximum."));
        popup.child(button("+", 216, 199, 24, () -> true, () -> StudioScaledPanel.changeScale(panel.getScreen(), 1)));
        popup.child(
            button("Game", 244, 199, 60, () -> true, () -> StudioScaledPanel.changeScale(panel.getScreen(), 0))
                .addTooltipLine("Follow Minecraft's GUI scale again. Other screens are never changed."));
        return popup;
    }

    private boolean codeEditable() {
        return editable() && view != View.VISUAL && !document.incomplete() && !pendingClause() && !pendingFields();
    }

    private ButtonWidget<?> viewButton(String title, int x, int width, View target) {
        return button(title, x, 3, width, () -> !state.busy(), () -> switchView(target)).height(17)
            .overlay(
                IKey.str(title)
                    .color(() -> view == target ? 0xffefa09a : 0xffe7e8eb));
    }

    private void openTerminal() {
        selectDock(Dock.TERMINAL);
    }

    public void openDebugDock() {
        openTerminal();
    }

    private boolean graphEditable() {
        return editable() && view != View.CODE && document.graph() != null && !pendingClause() && !pendingFields();
    }

    private boolean authoringOnTop() {
        return panel != null && panel.isValid()
            && panel.getScreen()
                .getPanelManager()
                .getTopMostPanel() == panel;
    }

    private void clearAuthorFocus() {
        if (graphEditor != null) {
            graphEditor.finishInteraction();
            graphEditor.cancelWire();
        }
        removeFocus();
    }

    private void undo(boolean redo) {
        if (!document.incomplete() && (view == View.CODE || view == View.SPLIT && codeFocused)) {
            if (redo) editor.redo();
            else editor.undo();
        } else graphEditor.undo(redo);
    }

    private void switchView(View next) {
        if (state.busy() || !applyClause()) return;
        if (next == View.CODE && document.incomplete()) {
            status = "Visual draft kept. Connect its nodes or Undo before Code; Split can preview the last safe source.";
            return;
        }
        if (next == View.SPLIT && (workspace == null || panel.getArea().width - workspace.mainLeft < 390)) {
            status = "Split needs more authoring space. Resize Projects or use Visual / Code.";
            return;
        }
        if (next != View.CODE) {
            try {
                document.openVisual();
            } catch (IllegalArgumentException unsupported) {
                status = "Code kept. Visual unavailable: " + unsupported.getMessage();
                return;
            }
        }
        clearAuthorFocus();
        view = next;
        if (next != View.SPLIT) codeFocused = next == View.CODE;
        layoutWidth = -1;
        panel.scheduleResize();
        status = document.incomplete() ? "Incomplete visual draft kept; connect nodes or Undo before saving."
            : next == View.CODE ? "Code view | Ctrl+Space: insert | F4: first error"
                : "Visual | Shift-drag: snap | Right-click: add | Middle-drag: pan | Wheel: zoom | F: fit";
        if (next != View.CODE) graphEditor.fit();
    }

    private void layoutWorkspace(int width, int top) {
        if (workspace == null) return;
        if (view == View.SPLIT && width - workspace.mainLeft < 390) {
            clearAuthorFocus();
            view = document.incomplete() ? View.VISUAL : View.CODE;
            status = "Split needs more authoring space. Resize Projects or use Visual / Code.";
        }
        int left = workspace.mainLeft, right = inspectorReserve();
        int middle = StudioLayout.splitMiddle(left, right, width, savedSplit);
        int bottom = layoutHeight - workspace.dockTop + 5;
        editor.left(view == View.SPLIT ? middle + 4 : left)
            .right(right)
            .top(top)
            .bottom(bottom);
        graphEditor.left(left)
            .right(view == View.SPLIT ? width - middle + 4 : right)
            .top(top)
            .bottom(bottom);
        divider.left(middle - 3)
            .top(top)
            .bottom(bottom);
        outlinePane.left(4)
            .top(workspace.viewTop)
            .width(workspace.sidebar)
            .bottom(18);
        inspectorPane.top(top)
            .width(Math.max(150, right - 16))
            .bottom(bottom)
            .setEnabledIf(widget -> view != View.CODE && workspace.inspector);
        if (inspectorDivider != null) inspectorDivider.left(width - right + 1)
            .top(top)
            .bottom(bottom);
        if (dockPane != null) dockPane.left(left)
            .right(8)
            .top(workspace.dockTop)
            .bottom(18);
        if (dockDivider != null) dockDivider.left(left)
            .right(8)
            .top(workspace.dockTop - 5)
            .height(5);
        if (sidebarDivider != null) sidebarDivider.left(workspace.sidebar + 4)
            .top(workspace.viewTop)
            .bottom(18);
    }

    private void buildWorkspacePanes() {
        outlinePane = new ParentWidget<>().left(4)
            .width(154)
            .top(46)
            .bottom(18)
            .background(StudioTheme.PANEL);
        outlinePane.child(
            new TextWidget<>(IKey.dynamic(() -> outlinePane.getArea().width < 100 ? "FILES" : "PROJECTS"))
                .color(0xffd6d9df)
                .pos(6, 7)
                .size(65, 11));
        outlinePane.child(
            listButton("+", 20, () -> !state.busy(), () -> protect("New draft", this::newDraft)).right(4)
                .top(3)
                .height(18)
                .addTooltipLine("New empty private project"));
        outlineList = new ListWidget<>().left(4)
            .right(4)
            .top(27)
            .bottom(4);
        outlinePane.child(outlineList);
        panel.child(outlinePane);
        inspectorPane = new ParentWidget<>().right(8)
            .width(150)
            .top(96)
            .bottom(47)
            .background(new Rectangle().color(0xff24262b));
        inspectorPane.child(
            new TextWidget<>(
                IKey.dynamic(() -> graphEditor.selected() == null ? "Inspector" : graphEditor.selected().type.title))
                    .color(0xffe7e8eb)
                    .pos(6, 7)
                    .widthRelOffset(1f, -12)
                    .height(12));
        inspectorPane.child(button("Add node", 6, 24, 72, this::graphEditable, this::openNodes));
        inspectorPane.child(button("Fit", 0, 24, 62, () -> true, graphEditor::fit).leftRelOffset(1f, -68));
        inspectorPane.child(
            new TextWidget<>(IKey.dynamic(() -> advancedTools ? "SFML clause" : "Node options")).color(0xffb0b3bb)
                .pos(6, 51)
                .size(86, 12));
        inspectorPane.child(
            button("Fields", 0, 47, 48, () -> graphEditor.selected() != null && !state.busy(), this::openFields)
                .leftRelOffset(1f, -54)
                .height(17));
        clauseField = new TextFieldWidget().setMaxLength(BlueprintGraph.MAX_CLAUSE)
            .autoUpdateOnChange(true)
            .value(new StringValue.Dynamic(() -> clause, value -> {
                if (editable() && !clause.equals(value)) {
                    state.noteLocalEdit();
                    clause = value;
                }
            }));
        inspectorPane.child(
            clauseField.pos(6, 66)
                .widthRelOffset(1f, -12)
                .height(18)
                .setEnabledIf(widget -> advancedTools)
                .addTooltipLine("Edit the selected clause, then Apply. Modifiers remain part of the clause."));
        inspectorPane
            .child(button("Apply", 6, 90, 64, this::editable, this::applyClause).setEnabledIf(widget -> advancedTools));
        inspectorPane.child(
            button("Reset", 0, 90, 68, this::editable, this::selectionChanged).leftRelOffset(1f, -74)
                .setEnabledIf(widget -> advancedTools));
        inspectorPane.child(
            new TextWidget<>("Use Fields to edit this node. Advanced tools also expose its raw SFML clause.").pos(6, 66)
                .widthRelOffset(1f, -12)
                .height(42)
                .scale(.8f)
                .color(0xffa9b3bd)
                .setEnabledIf(widget -> !advancedTools));
        inspectorPane.child(
            button("Delete", 6, 116, 138, this::graphEditable, graphEditor::deleteSelected).widthRelOffset(1f, -12));
        panel.child(inspectorPane);
    }

    private int inspectorReserve() {
        return view != View.CODE && workspace != null && workspace.inspector
            ? StudioLayout
                .inspectorPaneWidth(panel.getArea().width, workspace.mainLeft, view == View.SPLIT, savedInspector) + 16
            : 8;
    }

    private void rebuildOutline() {
        editor.catalog(symbols);
        if (outlineList == null) return;
        outlineList.removeAll();
        if (catalog.entries.isEmpty()) outlineList.child(
            new TextWidget<>("No saved projects").color(0xff979da7)
                .height(14)
                .widthRel(1f));
        for (ProjectPage.Entry entry : catalog.entries) {
            outlineList.child(
                listButton("", 120, () -> !state.busy(), () -> selectProject(entry)).widthRel(1f)
                    .height(20)
                    .overlay(
                        IKey.dynamic(
                            () -> trimFor(
                                (state.saved != null && state.saved.id.equals(entry.id) ? "> " : "") + entry.name,
                                Math.max(20, outlinePane.getArea().width - 20)))
                            .color(0xffe7e8eb))
                    .addTooltipLine(entry.name + " | saved r" + entry.revision));
        }
        if (catalog.total > CATALOG_ROWS) {
            ParentWidget<?> paging = new ParentWidget<>().widthRel(1f)
                .height(20);
            paging.child(
                button(
                    "<",
                    0,
                    0,
                    28,
                    () -> !state.awaitingReply() && catalog.offset > 0,
                    () -> request(Action.LIST, null, Math.max(0, catalog.offset - CATALOG_ROWS))).height(18));
            paging.child(
                button(
                    ">",
                    32,
                    0,
                    28,
                    () -> !state.awaitingReply() && catalog.offset + CATALOG_ROWS < catalog.total,
                    () -> request(Action.LIST, null, catalog.offset + CATALOG_ROWS)).height(18));
            outlineList.child(paging);
        }
        outlineList.child(
            listButton(
                "Refresh projects",
                120,
                () -> !state.awaitingReply(),
                () -> request(Action.LIST, null, catalog.offset)).widthRel(1f)
                    .height(18));
        outlineList.child(
            new TextWidget<>("TEMPLATES").color(0xffc1878e)
                .height(19)
                .widthRel(1f));
        for (StudioTemplates.Template template : StudioTemplates.Template.values()) outlineList.child(
            listButton(
                template.title,
                120,
                () -> !state.busy(),
                () -> protect("Use " + template.title, () -> template(template))).widthRel(1f)
                    .height(20)
                    .addTooltipLine("Create a local draft. Label names must match your disk labels."));
        outlineList.child(
            new TextWidget<>("program.sfml").color(0xff9ca5b0)
                .height(20)
                .widthRel(1f));
        for (SfmlCompletionCatalog.OutlineEntry entry : symbols.outline())
            outlineList.child(listButton("", 120, () -> !document.incomplete(), () -> {
                switchView(View.CODE);
                editor.goTo(entry.getOffset());
            }).widthRel(1f)
                .height(18)
                .overlay(
                    IKey.str(entry.getTitle())
                        .color(0xffc3c8d0))
                .addTooltipLine(entry.getTitle() + " | line " + entry.getLine()));
        if (outlineList.isValid()) outlineList.scheduleResize();
    }

    private void selectProject(ProjectPage.Entry entry) {
        protect("Load " + entry.name, () -> {
            loadingRecovery = cache().get(key(entry.id));
            request(Action.LOAD, entry.id, 0);
        });
    }

    private void template(StudioTemplates.Template template) {
        newDraft();
        state.name = template.title;
        state.source = template.source;
        state.noteLocalEdit();
        document.load(template.source);
        syncEditor();
        if (view != View.CODE) document.openVisual();
        status = "Template draft. Set source / destination disk labels, then Save and Check.";
    }

    private void buildDock() {
        dockPane = new ParentWidget<>().left(164)
            .right(8)
            .top(360)
            .bottom(18)
            .background(StudioTheme.PANEL);
        String[] titles = { "Problems", "Debug terminal", "Watch", "Build revisions" };
        Dock[] tabs = Dock.values();
        for (int i = 0; i < tabs.length; i++) {
            final Dock tab = tabs[i];
            final String title = titles[i];
            ButtonWidget<?> button = button("", 4 + i * 100, 2, 98, () -> true, () -> selectDock(tab)).height(18)
                .background(StudioTheme.TAB)
                .overlay(IKey.dynamic(() -> {
                    String label = dockPane.getArea().width < 360 ? tab == Dock.TERMINAL ? "Debug"
                        : tab == Dock.BUILDS ? "Builds" : tab == Dock.PROBLEMS ? "Issues" : title : title;
                    return label;
                })
                    .color(() -> dock == tab ? 0xffefa09a : 0xffa9aeb7));
            dockTabs.add(button);
            dockPane.child(button);
        }
        problemDock = new ParentWidget<>().left(4)
            .right(4)
            .top(24)
            .bottom(4)
            .setEnabledIf(widget -> dock == Dock.PROBLEMS);
        problemDock.child(button("Go to error", 4, 3, 76, () -> true, this::jumpToError).height(18));
        problemDock.child(button("Check saved", 83, 3, 78, () -> !state.busy(), this::validate).height(18));
        problemOutput = new StudioReadOnlyPane(this::authoringOnTop).left(4)
            .right(4)
            .top(26)
            .bottom(4);
        problemDock.child(problemOutput);
        dockPane.child(problemDock);
        terminal = new StudioDebugTerminal(station, sessionId, windowId);
        terminalDock = terminal
            .buildDock(
                panel,
                () -> authoringOnTop() && (dock == Dock.TERMINAL || dock == Dock.WATCH),
                () -> dock == Dock.WATCH,
                this::authoringOnTop)
            .left(4)
            .right(4)
            .top(24)
            .bottom(4)
            .setEnabledIf(widget -> dock == Dock.TERMINAL);
        watchDock = terminal.buildWatch(this::authoringOnTop)
            .left(4)
            .right(4)
            .top(24)
            .bottom(4)
            .setEnabledIf(widget -> dock == Dock.WATCH);
        dockPane.child(terminalDock);
        dockPane.child(watchDock);
        buildDock = new ParentWidget<>().left(4)
            .right(4)
            .top(24)
            .bottom(4)
            .setEnabledIf(widget -> dock == Dock.BUILDS);
        publishDockButton = button(
            "",
            4,
            3,
            74,
            () -> state.saved != null && !state.awaitingReply() && !state.dirty() && !visualDraft(),
            this::publish).height(18)
                .overlay(
                    IKey.dynamic(() -> publishDockButton.getArea().width < 74 ? "Publish" : "Publish saved")
                        .color(0xffe7e8eb))
                .addTooltipLine("Compile and publish the exact saved server revision as an immutable build.");
        refreshBuildsButton = button(
            "Refresh",
            81,
            3,
            46,
            () -> state.saved != null && !state.awaitingReply(),
            () -> request(Action.LIST_PUBLICATIONS, state.saved.id, 0)).height(18);
        imprintDockButton = button(
            "",
            130,
            3,
            78,
            () -> REFERENCE_DISKS_ENABLED && state.saved != null
                && state.selectedBuild() != null
                && !state.awaitingReply(),
            () -> request(Action.WRITE_DISK, state.selectedBuild().project, 0)).height(18)
                .background(StudioTheme.PRIMARY)
                .overlay(
                    IKey.dynamic(() -> imprintDockButton.getArea().width < 74 ? "Imprint" : "Imprint disk")
                        .color(0xffe7e8eb))
                .addTooltipLine(IKey.dynamic(this::imprintHelp));
        buildDock.child(publishDockButton);
        buildDock.child(refreshBuildsButton);
        buildDock.child(imprintDockButton);
        ListWidget<IWidget, ?> revisions = new ListWidget<>().left(4)
            .right(4)
            .top(26)
            .bottom(32);
        for (int i = 0; i < BUILD_ROWS; i++) {
            final int row = i;
            revisions.child(listButton("", 200, () -> !state.awaitingReply() && publishedEntry(row) != null, () -> {
                PublicationReceipt receipt = publishedEntry(row);
                if (receipt == null) return;
                state.selectBuild(receipt);
                status = "Selected immutable build r" + receipt.revision + ". Imprint disk writes exactly this build.";
            }).widthRel(1f)
                .height(18)
                .overlay(IKey.dynamic(() -> {
                    PublicationReceipt receipt = publishedEntry(row);
                    if (receipt == null) return row == 0 ? state.saved == null ? "Save a project to create builds."
                        : "No builds loaded. Publish saved or Refresh." : "";
                    return (state.isSelectedBuild(receipt) ? "> " : "  ") + "r"
                        + receipt.revision
                        + " / "
                        + receipt.digest.substring(0, 16);
                })
                    .color(0xffdce0e6)));
        }
        ParentWidget<?> paging = new ParentWidget<>().widthRel(1f)
            .height(19);
        paging.child(
            button(
                "Previous",
                0,
                0,
                60,
                () -> state.saved != null && !state.awaitingReply() && publications.offset > 0,
                () -> request(Action.LIST_PUBLICATIONS, state.saved.id, Math.max(0, publications.offset - BUILD_ROWS)))
                    .height(17));
        paging.child(
            button(
                "Next",
                63,
                0,
                48,
                () -> state.saved != null && !state.awaitingReply()
                    && publications.offset + BUILD_ROWS < publications.total,
                () -> request(Action.LIST_PUBLICATIONS, state.saved.id, publications.offset + BUILD_ROWS)).height(17));
        revisions.child(paging);
        buildDock.child(revisions);
        buildDock.child(new TextWidget<>(IKey.dynamic(() -> {
            PublicationReceipt receipt = state.selectedBuild();
            return receipt == null ? "Save > Publish saved > select build"
                : "Selected saved r" + receipt.revision + " / " + receipt.digest.substring(0, 12);
        })).color(0xffe4b4ae)
            .left(4)
            .right(4)
            .bottom(17)
            .height(10)
            .setEnabledIf(widget -> buildDock.getArea().height >= 82));
        buildDock.child(
            new TextWidget<>(
                IKey.dynamic(
                    () -> REFERENCE_DISKS_ENABLED ? "Hold 1 fresh disk before opening station."
                        : "Imprint locked: awaiting runtime verification.")).color(0xffa5aab4)
                            .left(4)
                            .right(4)
                            .bottom(4)
                            .height(10)
                            .addTooltipLine(IKey.dynamic(this::imprintHelp))
                            .setEnabledIf(widget -> buildDock.getArea().height >= 82));
        final int[] lastBuildHeight = { -1 };
        buildDock.onUpdateListener(widget -> {
            if (lastBuildHeight[0] != buildDock.getArea().height) {
                lastBuildHeight[0] = buildDock.getArea().height;
                revisions.bottom(lastBuildHeight[0] >= 82 ? 32 : 4);
                buildDock.scheduleResize();
            }
        }, true);
        dockPane.child(buildDock);
        panel.child(dockPane);
        updateProblems();
    }

    private String imprintHelp() {
        if (!REFERENCE_DISKS_ENABLED)
            return "Reference disk writing awaits runtime verification. Saved build publishing and selection remain available.";
        if (state.saved == null) return "Save this project first, then Publish saved and select the exact build.";
        if (state.selectedBuild() == null)
            return "Publish the saved revision and select an immutable build before imprinting.";
        return "Imprints selected saved r" + state.selectedBuild().revision
            + ". Hold exactly one fresh program disk before opening the station. Server permission and held-medium checks apply.";
    }

    private void selectDock(Dock next) {
        clearAuthorFocus();
        dock = next;
        if (next == Dock.BUILDS && state.saved != null && !state.awaitingReply())
            request(Action.LIST_PUBLICATIONS, state.saved.id, publications.offset);
        if (next == Dock.PROBLEMS) updateProblems();
    }

    private void updateProblems() {
        boolean dirty = state.dirty() || visualDraft();
        if (problemOutput == null || draftDiagnostic.equals(lastProblemDiagnostic) && status.equals(lastProblemStatus)
            && lastProblemSaved == state.saved
            && lastProblemValidation == state.validation
            && dirty == lastProblemDirty) return;
        lastProblemDiagnostic = draftDiagnostic;
        lastProblemStatus = status;
        lastProblemSaved = state.saved;
        lastProblemValidation = state.validation;
        lastProblemDirty = dirty;
        List<String> lines = new ArrayList<>();
        Collections.addAll(lines, draftDiagnostic.split("\\n"));
        lines.add(state.revisionLine());
        Collections.addAll(lines, status.split("\\n"));
        problemOutput.lines(lines);
    }

    public void receiveStationDebugReply(PacketStationDebugReply reply) {
        if (terminal != null) terminal.receive(reply);
    }

    private void graphChanged() {
        codeFocused = false;
        state.noteLocalEdit();
        state.source = document.source();
        syncEditor();
        diagnosticDelay = 6;
        draftDiagnostic = document.incomplete() ? "Visual draft: " + document.problem() : "Draft changed; checking...";
        if (document.incomplete()) status = "Visual draft kept: " + document.problem();
        else status = "Visual draft updated. Save writes this project; it does not install a manager program.";
    }

    private void selectionChanged() {
        Node node = graphEditor == null ? null : graphEditor.selected();
        inspected = node == null ? null : node.id;
        clause = node == null ? "" : node.clause();
        if (clauseField != null) clauseField.setText(clause);
    }

    private boolean pendingClause() {
        Node node = graphEditor == null ? null : graphEditor.selected();
        return node != null && inspected != null
            && inspected.equals(node.id)
            && !clause.trim()
                .equals(node.clause());
    }

    private boolean applyClause() {
        if (pendingFields()) {
            status = "Typed fields kept. Reopen Fields and Apply or Cancel before changing the document.";
            return false;
        }
        if (!pendingClause()) return true;
        Node node = graphEditor.selected();
        String value = clause.trim();
        try {
            BlueprintCompiler.validateClause(node.type, value);
            document.editGraph(
                graph -> graph.node(node.id)
                    .setClause(value));
            graphChanged();
            return true;
        } catch (IllegalArgumentException invalid) {
            status = "Inspector kept: " + invalid.getMessage() + ". Apply a valid clause or Reset.";
            return false;
        }
    }

    private void openNodes() {
        if (!editable() || !applyClause()) return;
        if (document.graph() == null) {
            try {
                document.openVisual();
            } catch (IllegalArgumentException error) {
                status = error.getMessage();
                return;
            }
        }
        removeFocus();
        nodesPanel.deleteCachedPanel();
        nodesPanel.openPanel();
    }

    private ModularPanel buildNodes() {
        ModularPanel popup = modal("sfm_studio_add", 312, 228);
        popup.child(
            new TextWidget<>("Add a step / execution order").color(0xffe7e8eb)
                .pos(8, 8)
                .size(296, 12));
        int index = 0;
        for (BlueprintGraph.Type type : BlueprintGraph.Type.values()) {
            popup.child(button(type.title, 8, 26 + index++ * 21, 296, this::graphEditable, () -> {
                nodesPanel.closePanel();
                graphEditor.add(type);
            }).height(18)
                .addTooltipLine(type.defaultClause));
        }
        popup.child(button("Inspect selected", 8, 184, 138, () -> graphEditor.selected() != null, () -> {
            nodesPanel.closePanel();
            inspectPanel.deleteCachedPanel();
            inspectPanel.openPanel();
        }));
        popup.child(button("Fit", 150, 184, 62, () -> true, graphEditor::fit));
        popup.child(button("Done", 216, 184, 88, () -> true, nodesPanel::closePanel));
        return popup;
    }

    private ModularPanel buildInspectorPopup() {
        ModularPanel popup = modal("sfm_studio_inspect", 312, 124);
        popup.child(
            new TextWidget<>("Selected SFML clause").color(0xffe7e8eb)
                .pos(8, 8)
                .size(296, 12));
        popup.child(
            new TextFieldWidget().setMaxLength(BlueprintGraph.MAX_CLAUSE)
                .autoUpdateOnChange(true)
                .value(new StringValue.Dynamic(() -> clause, value -> {
                    if (!clause.equals(value)) {
                        state.noteLocalEdit();
                        clause = value;
                    }
                }))
                .pos(8, 30)
                .size(296, 18));
        popup
            .child(button("Apply", 8, 72, 90, this::editable, () -> { if (applyClause()) inspectPanel.closePanel(); }));
        popup.child(button("Reset", 102, 72, 90, this::editable, this::selectionChanged));
        popup.child(button("Fields", 8, 96, 90, this::editable, () -> {
            if (applyClause()) {
                inspectPanel.closePanel();
                openFields();
            }
        }));
        popup.child(button("Done", 196, 72, 108, () -> true, inspectPanel::closePanel));
        return popup;
    }

    private void openApi() {
        if (!editable()) return;
        symbols = SfmlCompletionCatalog.scan(document.code());
        removeFocus();
        apiPanel.deleteCachedPanel();
        apiPanel.openPanel();
    }

    private void complete() {
        if (!codeEditable() || completionPanel == null || completionPanel.isPanelOpen()) return;
        completionSource = editor.source();
        // Explicit completion scans once at the caret; no compilation or world reads per frame.
        completions = StudioCompletion.suggest(completionSource, editor.buffer.caret(), 64);
        if (completions.isEmpty()) {
            status = "No completion here. Check syntax or use Insert for statements and examples.";
            return;
        }
        removeFocus();
        completionSelection = 0;
        completionPanel.deleteCachedPanel();
        completionPanel.openPanel();
    }

    private ModularPanel buildCompletion() {
        ModularPanel popup = modal("sfm_studio_completion", 312, 228, this::completionKey);
        popup.child(
            new TextWidget<>("Complete / Up Down: choose, Enter: insert").color(0xffefa09a)
                .pos(8, 8)
                .size(296, 12));
        completionList = new ListWidget<>().pos(8, 28)
            .size(296, 148);
        for (int i = 0; i < completions.size(); i++) {
            final int index = i;
            StudioCompletion.Candidate candidate = completions.get(i);
            completionList.child(
                listButton("", 296, this::codeEditable, () -> applyCompletion(index)).height(18)
                    .overlay(
                        IKey.dynamic(() -> (completionSelection == index ? "> " : "  ") + candidate.getName())
                            .color(0xffe7e8eb))
                    .addTooltipLine(candidate.getSignature())
                    .addTooltipLine(candidate.getHelp())
                    .addTooltipLine("Inserts: " + candidate.getText()));
        }
        popup.child(completionList);
        popup.child(button("Cancel", 216, 188, 88, () -> true, completionPanel::closePanel));
        return popup;
    }

    private boolean completionKey(char character, int key) {
        if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) {
            applyCompletion(completionSelection);
            return true;
        }
        int next = completionSelection;
        if (key == Keyboard.KEY_UP) next--;
        else if (key == Keyboard.KEY_DOWN) next++;
        else if (key == Keyboard.KEY_PRIOR) next -= 8;
        else if (key == Keyboard.KEY_NEXT) next += 8;
        else if (key == Keyboard.KEY_HOME) next = 0;
        else if (key == Keyboard.KEY_END) next = completions.size() - 1;
        else return false;
        completionSelection = Math.max(0, Math.min(completions.size() - 1, next));
        // Keep keyboard selection visible without rebuilding any rows or reparsing the source.
        int top = completionSelection * 18;
        int scroll = completionList.getScrollData()
            .getScroll();
        int visible = completionList.getScrollData()
            .getVisibleSize(completionList.getScrollArea());
        if (top < scroll) completionList.getScrollData()
            .scrollTo(completionList.getScrollArea(), top);
        else if (top + 18 > scroll + visible) completionList.getScrollData()
            .scrollTo(completionList.getScrollArea(), top + 18 - visible);
        return true;
    }

    private void applyCompletion(int index) {
        if (!codeEditable() || index < 0 || index >= completions.size()) return;
        if (!completionSource.equals(editor.source())) {
            status = "Document changed; request completion again.";
            completionPanel.closePanel();
            return;
        }
        StudioCompletion.Candidate candidate = completions.get(index);
        completionPanel.closePanel();
        editor.replaceRange(candidate.getStart(), candidate.getEnd(), candidate.getText());
        focusAuthoring();
    }

    private String findText = "", replacementText = "", findStatus = "Literal search / wraps at document ends";
    private boolean matchCase;

    private void openFind() {
        if (view == View.VISUAL || findPanel == null || findPanel.isPanelOpen()) return;
        removeFocus();
        findPanel.deleteCachedPanel();
        findPanel.openPanel();
    }

    private ModularPanel buildFind() {
        ModularPanel popup = modal("sfm_studio_find", 312, 228);
        popup.child(
            new TextWidget<>("Find / Replace").color(0xffefa09a)
                .pos(8, 8)
                .size(296, 12));
        popup.child(
            new TextWidget<>("Find").color(0xffb0b3bb)
                .pos(8, 29)
                .size(296, 12));
        popup.child(
            new TextFieldWidget().setMaxLength(512)
                .autoUpdateOnChange(true)
                .value(new StringValue.Dynamic(() -> findText, value -> findText = value))
                .pos(8, 43)
                .size(296, 18));
        popup.child(
            new TextWidget<>("Replace with").color(0xffb0b3bb)
                .pos(8, 67)
                .size(296, 12));
        popup.child(
            new TextFieldWidget().setMaxLength(512)
                .autoUpdateOnChange(true)
                .value(new StringValue.Dynamic(() -> replacementText, value -> replacementText = value))
                .pos(8, 81)
                .size(296, 18));
        popup.child(
            button("", 8, 108, 108, () -> true, () -> matchCase = !matchCase).overlay(
                IKey.dynamic(() -> "Case: " + (matchCase ? "match" : "ignore"))
                    .color(0xffe7e8eb)));
        popup.child(button("Previous", 120, 108, 88, () -> !findText.isEmpty(), () -> find(false)));
        popup.child(button("Next", 212, 108, 92, () -> !findText.isEmpty(), () -> find(true)));
        popup.child(button("Replace", 8, 134, 138, () -> codeEditable() && !findText.isEmpty(), () -> {
            try {
                boolean replaced = editor.buffer.replaceMatch(findText, replacementText, matchCase);
                editor.refreshAfterCommand();
                findStatus = replaced ? "Replaced selection; moved to next match." : "Select a match with Next first.";
            } catch (IllegalArgumentException invalid) {
                findStatus = invalid.getMessage();
            }
        }));
        popup.child(button("Replace all", 150, 134, 154, () -> codeEditable() && !findText.isEmpty(), () -> {
            try {
                int count = editor.buffer.replaceAll(findText, replacementText, matchCase);
                editor.refreshAfterCommand();
                findStatus = "Replaced " + count + " matches. Undo restores them together.";
            } catch (IllegalArgumentException invalid) {
                findStatus = invalid.getMessage();
            }
        }));
        popup.child(
            new TextWidget<>(IKey.dynamic(() -> trimFor(findStatus, 296))).color(0xffb0b3bb)
                .pos(8, 166)
                .size(296, 12)
                .addTooltipLine(IKey.dynamic(() -> findStatus)));
        popup.child(button("Done", 216, 188, 88, () -> true, findPanel::closePanel));
        return popup;
    }

    private void find(boolean forward) {
        findStatus = editor.buffer.find(findText, !forward, matchCase) ? "Match selected in code." : "No matches.";
        editor.refreshAfterCommand();
    }

    private void openFields() {
        if ((!pendingFields() && !applyClause()) || graphEditor.selected() == null) return;
        clearAuthorFocus();
        fieldsPanel.deleteCachedPanel();
        fieldsPanel.openPanel();
    }

    private ModularPanel buildTypedFields() {
        int width = StudioLayout.inspectorWidth(
            panel.getArea()
                .w());
        int height = StudioLayout.inspectorHeight(
            panel.getArea()
                .h());
        int contentWidth = width - 28;
        ModularPanel popup = modal("sfm_studio_fields", width, height);
        // Resize the existing controls/draft rather than rebuilding and losing unapplied fields.
        popup.width(() -> StudioLayout.inspectorWidth(panel.getArea().width), Unit.Measure.PIXEL)
            .height(() -> StudioLayout.inspectorHeight(panel.getArea().height), Unit.Measure.PIXEL);
        Node node = graphEditor.selected();
        List<StudioNodeProperties.Field> fields = StudioNodeProperties.inspect(node.type, node.clause());
        UUID project = state.saved == null ? state.localId : state.saved.id;
        if (fieldDraft == null || !fieldDraft.project.equals(project)
            || !fieldDraft.node.equals(node.id)
            || !fieldDraft.original.equals(node.clause())) fieldDraft = new FieldDraft(project, node, fields);
        FieldDraft draft = fieldDraft;
        popup.child(
            new TextWidget<>(node.type.title + " / fields").color(0xffefa09a)
                .left(8)
                .right(8)
                .top(8)
                .height(12));
        String[] error = { "" };
        ListWidget<IWidget, ?> fieldList = new ListWidget<>().left(8)
            .right(8)
            .top(28)
            .bottom(66);
        for (StudioNodeProperties.Field field : fields) {
            ParentWidget<?> row = new ParentWidget<>().widthRelOffset(1f, -6)
                .height(43);
            row.child(
                new TextWidget<>(field.getName()).color(0xffb0b3bb)
                    .pos(0, 0)
                    .widthRelOffset(1f, -6)
                    .height(12));
            java.util.function.Consumer<String> change = value -> {
                if (!value.equals(draft.values.get(field.getKey()))) {
                    state.noteLocalEdit();
                    draft.values.put(field.getKey(), value);
                    diagnosticDelay = 0;
                }
            };
            if (field.getType() == StudioNodeProperties.FieldType.BOOLEAN) {
                row.child(
                    button(
                        "",
                        0,
                        14,
                        contentWidth,
                        this::editable,
                        () -> change.accept(Boolean.toString(!Boolean.parseBoolean(draft.values.get(field.getKey())))))
                            .name("studio_field_" + field.getKey())
                            .widthRelOffset(1f, -6)
                            .height(18)
                            .overlay(
                                IKey.dynamic(
                                    () -> Boolean.parseBoolean(draft.values.get(field.getKey())) ? "Enabled"
                                        : "Disabled")
                                    .color(0xffe7e8eb))
                            .addTooltipLine(field.getHelp()));
            } else if (field.getType() == StudioNodeProperties.FieldType.ROUND_ROBIN) {
                row.child(button("", 0, 14, contentWidth, this::editable, () -> {
                    String current = draft.values.get(field.getKey());
                    change.accept(current.equals("off") ? "label" : current.equals("label") ? "block" : "off");
                }).name("studio_field_" + field.getKey())
                    .widthRelOffset(1f, -6)
                    .height(18)
                    .overlay(
                        IKey.dynamic(() -> "Round robin: " + draft.values.get(field.getKey()))
                            .color(0xffe7e8eb))
                    .addTooltipLine(field.getHelp()));
            } else row.child(
                new TextFieldWidget().setMaxLength(BlueprintGraph.MAX_CLAUSE)
                    .autoUpdateOnChange(true)
                    .value(new StringValue.Dynamic(() -> draft.values.get(field.getKey()), change))
                    .pos(0, 14)
                    .widthRelOffset(1f, -6)
                    .height(18)
                    .addTooltipLine(field.getHelp()));
            fieldList.child(row);
        }
        popup.child(fieldList);
        if (fields.isEmpty()) popup.child(
            new TextWidget<>("Use the full clause for this node.").color(0xffb0b3bb)
                .left(8)
                .right(8)
                .top(36)
                .height(12));
        popup.child(
            new TextWidget<>(
                IKey.dynamic(
                    () -> trimFor(
                        error[0],
                        popup.getArea()
                            .w() - 16))).color(0xffefa09a)
                                .left(8)
                                .right(8)
                                .bottom(46)
                                .height(12)
                                .addTooltipLine(IKey.dynamic(() -> error[0])));
        popup.child(button("Apply fields", 8, height - 40, width - 166, () -> !fields.isEmpty() && editable(), () -> {
            try {
                UUID currentProject = state.saved == null ? state.localId : state.saved.id;
                Node current = document.graph() == null ? null
                    : document.graph()
                        .node(draft.node);
                if (fieldDraft != draft || !draft.project.equals(currentProject)
                    || current == null
                    || !current.clause()
                        .equals(draft.original))
                    throw new IllegalArgumentException("Project or node changed. Cancel and reopen Fields.");
                String next = node.clause();
                for (StudioNodeProperties.Field field : fields) if (!draft.values.get(field.getKey())
                    .equals(field.getValue()))
                    next = StudioNodeProperties
                        .update(node.type, next, field.getKey(), draft.values.get(field.getKey()));
                String applied = next;
                document.editGraph(
                    graph -> graph.node(node.id)
                        .setClause(applied));
                fieldDraft = null;
                graphChanged();
                selectionChanged();
                fieldsPanel.closePanel();
            } catch (IllegalArgumentException invalid) {
                error[0] = invalid.getMessage();
                status = "Fields kept: " + error[0];
            }
        }).widthRelOffset(1f, -166)
            .topRelOffset(1f, -40));
        popup.child(button("Cancel", width - 96, height - 40, 88, () -> true, () -> {
            if (fieldDraft == draft) fieldDraft = null;
            fieldsPanel.closePanel();
        }).leftRelOffset(1f, -96)
            .topRelOffset(1f, -40));
        return popup;
    }

    private ModularPanel buildApi() {
        ModularPanel popup = modal("sfm_studio_api", 312, 228);
        popup.child(
            new TextWidget<>("Functions / hover for signature and rules").color(0xffe7e8eb)
                .pos(8, 7)
                .size(296, 12));
        ListWidget<IWidget, ?> list = new ListWidget<>().pos(8, 50)
            .size(296, 132);
        String[] query = { "" };
        Runnable rebuild = () -> {
            list.removeAll();
            for (SfmlCompletionCatalog.Entry entry : SfmlCompletionCatalog.search(symbols, query[0], 256)) {
                list.child(
                    listButton(
                        entry.getName(),
                        296,
                        () -> !document.incomplete() && !pendingClause() && !pendingFields(),
                        () -> {
                            switchView(View.CODE);
                            apiPanel.closePanel();
                            editor.snippet(
                                new SyntaxSnippet(
                                    "api_" + entry.getName(),
                                    entry.getName(),
                                    SnippetCategory.DATA,
                                    entry.getTemplate(),
                                    entry.getDescription()));
                        }).height(18)
                            .addTooltipLine(entry.getSignature())
                            .addTooltipLine(entry.getDescription())
                            .addTooltipLine("Inserts: " + entry.getTemplate()));
            }
            if (list.isValid()) list.scheduleResize();
        };
        rebuild.run();
        popup.child(
            new TextFieldWidget().setMaxLength(96)
                .autoUpdateOnChange(true)
                .value(new StringValue.Dynamic(() -> query[0], value -> {
                    query[0] = value;
                    rebuild.run();
                }))
                .pos(8, 26)
                .size(296, 18)
                .addTooltipLine("Search built-ins, methods and current-program functions"));
        popup.child(list);
        popup.child(button("Done", 242, 188, 62, () -> true, apiPanel::closePanel));
        return popup;
    }

    private void openProblems() {
        checkDraft();
        selectDock(Dock.PROBLEMS);
    }

    private ModularPanel buildProblems() {
        ModularPanel popup = modal("sfm_studio_problems", 312, 228);
        popup.child(
            new TextWidget<>("Current draft / saved revision").color(0xffe7e8eb)
                .pos(8, 8)
                .size(296, 12));
        ListWidget<IWidget, ?> list = new ListWidget<>().pos(8, 28)
            .size(296, 140);
        for (String message : new String[] { draftDiagnostic, state.revisionLine(), status }) {
            for (String line : message.split("\n")) list.child(
                new TextWidget<>(trimFor(line, 290)).color(0xffe7e8eb)
                    .size(292, 12)
                    .addTooltipLine(line));
        }
        popup.child(list);
        popup.child(button("Go to error", 8, 188, 108, this::editable, () -> {
            problemsPanel.closePanel();
            jumpToError();
        }));
        popup.child(button("Terminal", 120, 188, 94, () -> !state.busy(), () -> {
            problemsPanel.closePanel();
            openTerminal();
        }));
        popup.child(button("Done", 242, 188, 62, () -> true, problemsPanel::closePanel));
        return popup;
    }

    public void tick() {
        if (cancelled) return;
        // Compile once after typing settles, never once per rendered frame.
        if (diagnosticDelay >= 0 && --diagnosticDelay < 0) checkDraft();
        if (!opened) {
            opened = true;
            refreshCatalog = true;
            if (state.saved != null) {
                loadingRecovery = cache().get(key(state.saved.id));
                request(Action.LOAD, state.saved.id, 0);
            } else if (rememberedProject != null && !state.dirty() && !state.uncertainWrite) {
                loadingRecovery = cache().get(key(rememberedProject));
                request(Action.LOAD, rememberedProject, 0);
            }
        }
        if (refreshCatalog && !state.awaitingReply()) {
            refreshCatalog = false;
            request(Action.LIST, null, 0);
        }
        if (state.tick()) {
            status = state.timeoutMessage();
            retain();
        }
        if (terminal != null) terminal.tick();
        updateProblems();
    }

    public void receive(PacketStationReply reply) {
        if (cancelled || !ProgrammingStationDraftState
            .matches(sessionId, windowId, station.xCoord, station.yCoord, station.zCoord, reply)) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.thePlayer.openContainer.windowId != windowId) return;
        UUID previous = state.saved == null ? null : state.saved.id;
        if (!state
            .receive(reply.requestId, reply.action, reply.accepted, reply.draft, reply.validation, reply.publication))
            return;
        status = reply.message;
        if (!reply.accepted) {
            if (reply.action == Action.LOAD) loadingRecovery = null;
            retain();
            return;
        }
        if (reply.page != null) {
            catalog = reply.page;
            rebuildOutline();
        }
        if (reply.publications != null && state.saved != null) {
            // A catalog contains metadata only, and belongs to the current authenticated project.
            boolean matches = true;
            for (PublicationReceipt entry : reply.publications.entries)
                matches &= entry.owner.equals(state.saved.owner) && entry.project.equals(state.saved.id);
            if (matches) publications = reply.publications;
        }
        if (reply.publication != null && state.publication == reply.publication) {
            status = "Published saved r" + reply.publication.revision
                + " | "
                + reply.publication.digest.substring(0, 12)
                + ". Draft edits cannot change this build.";
        }
        if (reply.draft != null && !state.ignoredLateLoad) {
            projectMemory.remember(memoryScope, reply.draft.id);
            if (!reply.draft.id.equals(previous)) publications = new PublicationPage(Collections.emptyList(), 0, 0);
            boolean recoveredVisual = reply.action == Action.LOAD && loadingRecovery != null;
            if (reply.action == Action.CREATE || reply.action == Action.SAVE) cache().remove(key(previous));
            if (reply.action == Action.CREATE || reply.action == Action.SAVE) refreshCatalog = true;
            if (reply.action == Action.LOAD && loadingRecovery != null) {
                if (!recoveryOverCurrent) {
                    state.name = loadingRecovery.name;
                    state.source = loadingRecovery.source;
                    restoreVisual(loadingRecovery);
                }
                state.uncertainWrite = false; // A fresh LOAD has reconciled the saved identity/revision.
                status = "Recovered local draft over saved r" + state.saved.revision + "; review it before Save.";
                loadingRecovery = null;
            }
            // Write acknowledgements advance only the saved baseline. Newer code,
            // disconnected nodes and unapplied inspector text remain locally owned.
            if (reply.action == Action.LOAD && !recoveredVisual) {
                fieldDraft = null;
                document.load(state.source);
                if (graphEditor != null) graphEditor.resetSelection();
            }
            if (reply.action == Action.LOAD) syncEditor();
            if (view != View.CODE) {
                try {
                    document.openVisual();
                } catch (IllegalArgumentException unsupported) {
                    clearAuthorFocus();
                    view = View.CODE;
                }
                layoutWidth = -1;
                panel.scheduleResize();
            }
        }
        if (state.ignoredLateLoad)
            status = "Load arrived after editing; your draft was kept. Save or retain it before loading again.";
        if (reply.validation != null) status = reply.validation.message;
        if (reply.action == Action.PUBLISH && state.publication != null && !state.awaitingReply()) {
            state.selectBuild(state.publication);
            if (!publications.entries.contains(state.publication)) request(Action.LIST_PUBLICATIONS, state.saved.id, 0);
        }
        if (!state.dirty() && !state.uncertainWrite && !visualDraft())
            cache().remove(key(state.saved == null ? null : state.saved.id));
    }

    private boolean editable() {
        return !cancelled && !state.busy() && recoveryCapacity();
    }

    private void save() {
        if (!applyClause()) return;
        if (document.incomplete()) {
            status = "Save blocked: " + document.problem() + ". The complete visual draft remains in local recovery.";
            return;
        }
        request(state.saved == null ? Action.CREATE : Action.SAVE, state.saved == null ? null : state.saved.id, 0);
    }

    private boolean jumpToError() {
        if (!editable()) return false;
        checkDraft();
        if (document.incomplete() || pendingClause()) {
            status = draftDiagnostic;
            return false;
        }
        if (view == View.VISUAL) switchView(View.CODE);
        return editor.jumpToDiagnostic(draftDiagnostic);
    }

    private void checkDraft() {
        diagnosticDelay = -1;
        symbols = SfmlCompletionCatalog.scan(document.code());
        rebuildOutline();
        if (document.incomplete()) {
            draftDiagnostic = "Visual draft: " + document.problem();
            return;
        }
        if (pendingClause() || pendingFields()) {
            draftDiagnostic = "Inspector has unapplied changes. Apply or Reset before continuing.";
            return;
        }
        ca.teamdman.sfml.program_builder.ProgramBuildResult result = new ca.teamdman.sfml.program_builder.ProgramBuilder(
            editorSource()).build();
        editor.diagnostic(result.isSuccess() ? null : result.getErrorMessage());
        draftDiagnostic = result.isSuccess() ? "Draft: valid | Ctrl+S save | Ctrl+Space insert | F4 error"
            : result.getErrorMessage();
        if (view == View.SPLIT && document.graph() == null) {
            try {
                document.openVisual();
            } catch (IllegalArgumentException unsupported) {
                status = "Code kept; Visual unavailable for current source: " + unsupported.getMessage();
            }
        }
    }

    private void validate() {
        if (document.incomplete() || pendingClause()) {
            status = "Finish the visual draft before validating a saved revision.";
            return;
        }
        request(Action.VALIDATE, state.saved == null ? null : state.saved.id, 0);
    }

    private void request(Action action, UUID id, int offset) {
        if (cancelled) return;
        try {
            if (action == Action.WRITE_DISK && !REFERENCE_DISKS_ENABLED)
                throw new IllegalStateException("Reference disk writing awaits runtime verification.");
            boolean write = action == Action.CREATE || action == Action.SAVE;
            if (write) {
                BoundedUtf8.encode(state.name, ProjectWorkspace.MAX_NAME_BYTES);
                BoundedUtf8.encode(state.source, ProjectWorkspace.MAX_SOURCE_BYTES);
                if (state.name.trim()
                    .isEmpty()) throw new IllegalArgumentException("Give the project a name before saving.");
            }
            PacketStationRequest request = new PacketStationRequest();
            request.x = station.xCoord;
            request.y = station.yCoord;
            request.z = station.zCoord;
            request.windowId = windowId;
            request.sessionId = sessionId;
            request.action = action;
            request.projectId = id;
            request.offset = offset;
            PublicationReceipt selectedBuild = state.selectedBuild();
            request.expectedRevision = action == Action.WRITE_DISK && selectedBuild != null ? selectedBuild.revision
                : state.saved == null ? 0 : state.saved.revision;
            request.buildDigest = action == Action.WRITE_DISK && selectedBuild != null ? selectedBuild.digest : "";
            request.name = write ? state.name : "";
            request.source = write ? state.source : "";
            recoveryOverCurrent = action == Action.LOAD && loadingRecovery != null
                && state.saved != null
                && state.saved.id.equals(id);
            request.requestId = recoveryOverCurrent ? state.beginRecoveryLoad() : state.begin(action);
            try {
                request.validate();
            } catch (IllegalArgumentException invalid) {
                state.notSent();
                throw invalid;
            }
            SFMNetwork.WRAPPER.sendToServer(request);
            status = action == Action.VALIDATE ? "Validating saved revision r" + state.saved.revision + "..."
                : action == Action.PUBLISH ? "Publishing saved revision r" + state.saved.revision + "..."
                    : action == Action.WRITE_DISK
                        ? "Writing selected build r" + request.expectedRevision + " to the held disk..."
                        : action == Action.CREATE || action == Action.SAVE
                            ? "Saving project; waiting for confirmation..."
                            : action == Action.LIST_PUBLICATIONS ? "Loading saved builds..."
                                : "Loading project data...";
        } catch (IllegalArgumentException | IllegalStateException error) {
            status = error.getMessage() + " Draft kept.";
        }
    }

    private void newDraft() {
        state.newDraft();
        projectMemory.remember(memoryScope, null);
        publications = new PublicationPage(Collections.emptyList(), 0, 0);
        fieldDraft = null;
        document.load(state.source);
        if (graphEditor != null) graphEditor.resetSelection();
        syncEditor();
        if (view != View.CODE) document.openVisual();
        status = "New local draft. Name it, then Create.";
    }

    private void openBuilds() {
        selectDock(Dock.BUILDS);
        if (state.saved == null) status = "Create and save a project before publishing a build.";
    }

    private void publish() {
        if (document.incomplete() || pendingClause() || pendingFields()) {
            status = "Finish and save the visual draft before publishing a saved revision.";
            return;
        }
        request(Action.PUBLISH, state.saved == null ? null : state.saved.id, 0);
    }

    private ModularPanel buildPublications() {
        ModularPanel popup = modal("sfm_station_builds", 312, 228);
        popup.child(
            new TextWidget<>("Immutable saved builds").color(0xffefa09a)
                .pos(8, 7)
                .size(296, 12));
        popup.child(
            button("Publish saved", 8, 24, 112, () -> state.saved != null && !state.awaitingReply(), this::publish)
                .addTooltipLine("Compiles the exact saved revision, then stores an immutable source bundle."));
        popup.child(
            button(
                "Refresh",
                124,
                24,
                78,
                () -> state.saved != null && !state.awaitingReply(),
                () -> request(Action.LIST_PUBLICATIONS, state.saved.id, 0)));
        for (int i = 0; i < BUILD_ROWS; i++) {
            final int row = i;
            popup.child(
                button("", 8, 48 + i * 14, 296, () -> !state.awaitingReply() && publishedEntry(row) != null, () -> {
                    PublicationReceipt entry = publishedEntry(row);
                    if (entry == null) return;
                    state.selectBuild(entry);
                    status = "Selected immutable build r" + entry.revision
                        + " | "
                        + entry.digest.substring(0, 12)
                        + ".";
                }).height(13)
                    .overlay(IKey.dynamic(() -> {
                        PublicationReceipt entry = publishedEntry(row);
                        return entry == null ? ""
                            : trimFor(
                                (state.isSelectedBuild(entry) ? "> " : "") + "r"
                                    + entry.revision
                                    + " | "
                                    + entry.name
                                    + " | "
                                    + entry.digest.substring(0, 10),
                                280);
                    })
                        .color(0xffe7e8eb))
                    .addTooltipLine("Click to select this exact published revision. Draft changes are separate."));
        }
        popup.child(
            button(
                "Previous",
                8,
                136,
                64,
                () -> state.saved != null && !state.awaitingReply() && publications.offset > 0,
                () -> request(Action.LIST_PUBLICATIONS, state.saved.id, Math.max(0, publications.offset - BUILD_ROWS)))
                    .height(17));
        popup.child(
            new TextWidget<>(
                IKey.dynamic(
                    () -> publications.total == 0 ? "No published builds"
                        : (publications.offset + 1) + "-"
                            + Math.min(publications.total, publications.offset + BUILD_ROWS)
                            + " / "
                            + publications.total)).color(0xffe7e8eb)
                                .pos(76, 140)
                                .size(148, 12));
        popup.child(
            button(
                "Next",
                240,
                136,
                64,
                () -> state.saved != null && !state.awaitingReply()
                    && publications.offset + BUILD_ROWS < publications.total,
                () -> request(Action.LIST_PUBLICATIONS, state.saved.id, publications.offset + BUILD_ROWS)).height(17));
        popup.child(new TextWidget<>(IKey.dynamic(() -> {
            PublicationReceipt selected = state.selectedBuild();
            return selected == null ? "Select a published build."
                : trimFor("Selected r" + selected.revision + " | " + selected.digest.substring(0, 16), 296);
        })).color(0xffe7e8eb)
            .pos(8, 158)
            .size(296, 12));
        popup.child(
            button(
                "Write held disk",
                8,
                176,
                128,
                () -> REFERENCE_DISKS_ENABLED && state.saved != null
                    && state.selectedBuild() != null
                    && !state.awaitingReply(),
                () -> request(Action.WRITE_DISK, state.selectedBuild().project, 0)).height(17)
                    .addTooltipLine(
                        REFERENCE_DISKS_ENABLED
                            ? "Hold exactly one fresh program disk before opening the station. Writes the selected immutable build."
                            : "Reference disk writing awaits runtime verification."));
        popup.child(button("Copy digest", 140, 176, 96, () -> state.selectedBuild() != null, () -> {
            PublicationReceipt selected = state.selectedBuild();
            if (selected == null) return;
            GuiScreen.setClipboardString(selected.digest);
            status = "Copied build digest for saved r" + selected.revision + ".";
        }).height(17));
        popup.child(button("Done", 242, 176, 62, () -> true, buildsPanel::closePanel).height(17));
        popup.child(
            new TextWidget<>(IKey.dynamic(() -> trimFor(status, 296))).color(0xffb0b3bb)
                .pos(8, 198)
                .size(296, 12)
                .addTooltipLine(IKey.dynamic(() -> status)));
        popup.child(
            new TextWidget<>(
                REFERENCE_DISKS_ENABLED ? "Hold 1 fresh disk before opening the station."
                    : "Reference disk writing awaits runtime verification.").color(0xffb0b3bb)
                        .pos(8, 213)
                        .size(296, 12));
        return popup;
    }

    private PublicationReceipt publishedEntry(int row) {
        return row < publications.entries.size() ? publications.entries.get(row) : null;
    }

    private void openCatalog() {
        removeFocus();
        if (!state.awaitingReply()) request(Action.LIST, null, catalog.offset);
    }

    private ModularPanel buildCatalog() {
        ModularPanel popup = modal("sfm_station_projects", 312, 228);
        popup.child(
            new TextWidget<>("Your private projects").pos(8, 7)
                .color(0xffe7e8eb)
                .size(296, 12));
        popup.child(
            new TextWidget<>(IKey.dynamic(() -> trimFor(status, 296))).pos(8, 23)
                .color(0xffb0b3bb)
                .size(296, 12)
                .addTooltipLine(IKey.dynamic(() -> status)));
        for (int i = 0; i < CATALOG_ROWS; i++) {
            final int row = i;
            popup.child(button("", 8, 40 + i * 15, 296, () -> !state.busy(), () -> {
                ProjectPage.Entry entry = entry(row);
                if (entry == null) return;
                catalogPanel.closePanel();
                protect("Load " + entry.name, () -> {
                    loadingRecovery = cache().get(key(entry.id));
                    request(Action.LOAD, entry.id, 0);
                });
            }).height(14)
                .overlay(IKey.dynamic(() -> {
                    ProjectPage.Entry entry = entry(row);
                    return entry == null ? ""
                        : trimFor(
                            entry.name + " | r"
                                + entry.revision
                                + (cache().containsKey(key(entry.id)) ? " | local draft" : ""),
                            280);
                })
                    .color(0xffe7e8eb)));
        }
        popup.child(
            button(
                "Previous",
                8,
                164,
                64,
                () -> !state.awaitingReply() && catalog.offset > 0,
                () -> request(Action.LIST, null, Math.max(0, catalog.offset - CATALOG_ROWS))));
        popup.child(
            new TextWidget<>(
                IKey.dynamic(
                    () -> catalog.total == 0 ? "No saved projects"
                        : (catalog.offset + 1) + "-"
                            + Math.min(catalog.total, catalog.offset + CATALOG_ROWS)
                            + " / "
                            + catalog.total)).color(0xffe7e8eb)
                                .pos(76, 168)
                                .size(148, 12));
        popup.child(
            button(
                "Next",
                240,
                164,
                64,
                () -> !state.awaitingReply() && catalog.offset + CATALOG_ROWS < catalog.total,
                () -> request(Action.LIST, null, catalog.offset + CATALOG_ROWS)));
        popup.child(button("Recover local", 8, 188, 98, () -> !state.busy() && latestRecovery() != null, () -> {
            Retained retained = latestRecovery();
            if (retained == null) return;
            catalogPanel.closePanel();
            protect("Recover local draft", () -> {
                restore(retained);
                syncEditor();
                if (graphEditor != null) {
                    String recoveredClause = clause;
                    graphEditor.select(retained.selected);
                    clause = recoveredClause;
                    clauseField.setText(clause);
                }
                layoutWidth = -1;
                panel.scheduleResize();
                if (state.saved != null) {
                    loadingRecovery = retained;
                    request(Action.LOAD, state.saved.id, 0);
                }
            });
        }));
        popup.child(button("New", 110, 188, 52, () -> !state.busy(), () -> {
            catalogPanel.closePanel();
            protect("New draft", this::newDraft);
        }));
        popup.child(button("Done", 242, 188, 62, () -> true, catalogPanel::closePanel));
        return popup;
    }

    private ProjectPage.Entry entry(int row) {
        return row < catalog.entries.size() ? catalog.entries.get(row) : null;
    }

    private void protect(String label, Runnable action) {
        if (state.canReplace() && !visualDraft()) {
            action.run();
            return;
        }
        if (state.busy()) {
            status = "Wait for the current server request. Copy remains available.";
            return;
        }
        protectedAction = action;
        protectedLabel = label;
        removeFocus();
        confirmationPanel.deleteCachedPanel();
        confirmationPanel.openPanel();
    }

    private ModularPanel buildConfirmation() {
        ModularPanel popup = modal("sfm_station_keep_draft", 300, 124);
        popup.child(
            new TextWidget<>("Keep unsaved source and name?").pos(8, 10)
                .color(0xffe7e8eb)
                .size(284, 12));
        popup.child(
            new TextWidget<>("Your draft will stay in local recovery.").pos(8, 29)
                .color(0xffb0b3bb)
                .size(284, 12));
        popup.child(
            new TextWidget<>(IKey.dynamic(() -> trimFor(protectedLabel, 284))).pos(8, 48)
                .color(0xffe7e8eb)
                .size(284, 12));
        popup.child(button("Keep draft & continue", 8, 88, 176, () -> !state.busy(), () -> {
            if (!retain()) return;
            state.afterRetainingDraft();
            document.load(state.source);
            graphEditor.resetSelection();
            syncEditor();
            confirmationPanel.closePanel();
            protectedAction.run();
        }));
        popup.child(button("Cancel", 192, 88, 100, () -> true, confirmationPanel::closePanel));
        return popup;
    }

    private void snippets() {
        if (!editable() || snippetsPanel == null || snippetsPanel.isPanelOpen()) return;
        snippetSearch = "";
        removeFocus();
        snippetsPanel.deleteCachedPanel();
        snippetsPanel.openPanel();
    }

    private ModularPanel buildSnippets() {
        ModularPanel popup = modal("sfm_station_snippets", 312, 228);
        popup.child(
            new TextWidget<>(
                snippetCategory == null ? "Insert / all SFML" : "Insert / " + snippetCategory.getDisplayName())
                    .color(0xffe7e8eb)
                    .pos(8, 7)
                    .size(296, 12));
        ListWidget<IWidget, ?> list = new ListWidget<>().pos(8, 49)
            .size(296, 131);
        List<SyntaxSnippet> entries = snippetCategory == null ? SyntaxRegistry.getAllSnippets()
            : SyntaxRegistry.getSnippetsForCategory(snippetCategory);
        List<SyntaxSnippet> visible = new ArrayList<>(entries);
        Runnable rebuild = () -> {
            list.removeAll();
            for (SyntaxSnippet snippet : visible) {
                list.child(
                    new ButtonWidget<>().widthRel(1f)
                        .height(20)
                        .background(StudioTheme.BUTTON)
                        .hoverBackground(StudioTheme.HOVER)
                        .overlay(
                            IKey.dynamic(() -> trimFor(snippet.getTitle(), 280))
                                .color(0xffe7e8eb))
                        .addTooltipLine(
                            IKey.str(
                                snippet.getCategory()
                                    .getDisplayName()))
                        .addTooltipLine(IKey.str(snippet.getDescription()))
                        .addTooltipLine(IKey.str(snippet.getTemplate()))
                        .onMousePressed(mouse -> {
                            if (mouse != 0 || !editable()) return false;
                            if (document.incomplete() || !applyClause()) return true;
                            switchView(View.CODE);
                            snippetsPanel.closePanel();
                            editor.snippet(snippet);
                            return true;
                        }));
            }
            if (list.isValid()) list.scheduleResize();
        };
        rebuild.run();
        popup.child(
            new TextFieldWidget().setMaxLength(96)
                .autoUpdateOnChange(true)
                .value(new StringValue.Dynamic(() -> snippetSearch, value -> {
                    snippetSearch = value;
                    String query = value.trim()
                        .toLowerCase(Locale.ROOT);
                    visible.clear();
                    for (SyntaxSnippet entry : entries) {
                        if ((entry.getCategory()
                            .getDisplayName() + " "
                            + entry.getTitle()
                            + " "
                            + entry.getDescription()
                            + " "
                            + entry.getTemplate()).toLowerCase(Locale.ROOT)
                                .contains(query))
                            visible.add(entry);
                    }
                    rebuild.run();
                }))
                .pos(8, 26)
                .size(296, 18)
                .addTooltipLine("Search functions, examples and syntax"));
        popup.child(list);
        popup.child(button("Done", 242, 188, 62, () -> true, snippetsPanel::closePanel));
        return popup;
    }

    private ModularPanel modal(String name, int width, int height) {
        return modal(name, width, height, (character, key) -> false);
    }

    private ModularPanel modal(String name, int width, int height, BiPredicate<Character, Integer> keys) {
        return new StudioScaledPanel(name) {

            @Override
            public boolean disablePanelsBelow() {
                return true;
            }

            @Override
            public boolean onKeyPressed(char character, int key) {
                if (key == Keyboard.KEY_ESCAPE) {
                    closeIfOpen();
                    return true;
                }
                return keys.test(character, key) || super.onKeyPressed(character, key);
            }
        }.size(width, height)
            .background(StudioTheme.SHELL)
            .onCloseAction(() -> { focusAuthoring(); });
    }

    public void focusAuthoring() {
        if (!cancelled && editor.isValid()) editor.getContext()
            .focus(view == View.CODE || view == View.SPLIT && codeFocused ? editor : graphEditor);
    }

    private void restore(Retained retained) {
        state.saved = retained.saved;
        state.localId = retained.localId;
        state.name = retained.name;
        state.source = retained.source;
        state.uncertainWrite = retained.uncertain;
        restoreVisual(retained);
        status = retained.uncertain
            ? "Recovered draft; previous save may have applied. Inspect Projects before writing again."
            : "Recovered unsaved source/name. Review before saving.";
    }

    private void restoreVisual(Retained retained) {
        document.restore(retained.visual);
        view = retained.view;
        inspected = retained.selected;
        clause = retained.clause;
        fieldDraft = retained.fields == null ? null : new FieldDraft(retained.fields);
        if (graphEditor != null) {
            graphEditor.select(retained.selected);
            clause = retained.clause;
            clauseField.setText(clause);
        }
    }

    private LinkedHashMap<String, Retained> cache() {
        return RECOVERY.computeIfAbsent(station.getWorldObj(), world -> new LinkedHashMap<>());
    }

    private String key(UUID id) {
        return scope + (id == null ? "new:" + state.localId : id);
    }

    private Retained latestRecovery() {
        Retained found = null;
        for (Map.Entry<String, Retained> entry : cache().entrySet()) if (entry.getKey()
            .startsWith(scope)) found = entry.getValue();
        return found;
    }

    private boolean recoveryCapacity() {
        return cache().size() < MAX_RECOVERED_DRAFTS
            || cache().containsKey(key(state.saved == null ? null : state.saved.id));
    }

    private boolean retain() {
        boolean writePending = state.awaitingReply()
            && (state.action() == Action.CREATE || state.action() == Action.SAVE);
        if (!state.dirty() && !state.uncertainWrite && !writePending && !visualDraft()) return true;
        UUID id = state.saved == null ? null : state.saved.id;
        if (!recoveryCapacity()) {
            status = "Local recovery holds 16 drafts. Save a retained draft before opening another editable project.";
            return false;
        }
        LinkedHashMap<String, Retained> drafts = cache();
        drafts.remove(key(id));
        drafts.put(key(id), new Retained(this));
        return true;
    }

    private void copy() {
        if (document.incomplete() || pendingClause() || pendingFields()) {
            status = "Finish or undo the visual draft before copying executable source. The graph is kept in recovery.";
            return;
        }
        GuiScreen.setClipboardString(document.code());
        status = "Draft source copied; project name remains " + state.name;
    }

    private boolean visualDraft() {
        return document.incomplete() || pendingClause() || pendingFields();
    }

    private boolean pendingFields() {
        return fieldDraft != null && fieldDraft.dirty();
    }

    public void close() {
        if (graphEditor != null) graphEditor.finishInteraction();
        if (!retain()) return;
        closeAction.run();
    }

    public void cancel() {
        if (cancelled) return;
        if (graphEditor != null) graphEditor.finishInteraction();
        retain();
        cancelled = true;
        if (terminal != null) terminal.cancel();
        if (editor.isValid() && editor.isFocused()) editor.getContext()
            .removeFocus();
    }

    private void removeFocus() {
        if (editor != null && editor.isValid()) editor.getContext()
            .removeFocus();
    }

    private String trim(String text) {
        return trimFor(
            text,
            panel == null ? 280
                : Math.max(
                    40,
                    panel.getArea()
                        .w() - 16));
    }

    private static String trimFor(String text, int width) {
        int newline = text.indexOf('\n');
        return Minecraft.getMinecraft().fontRenderer
            .trimStringToWidth(newline < 0 ? text : text.substring(0, newline), width);
    }

    private static ButtonWidget<?> button(String label, int x, int y, int width, BooleanSupplier available,
        Runnable action) {
        return listButton(label, width, available, action).pos(x, y);
    }

    private static ButtonWidget<?> listButton(String label, int width, BooleanSupplier available, Runnable action) {
        // ListWidget owns row positions; an explicit y=0 would stack every row and break scrolling.
        return new ButtonWidget<>().size(width, 20)
            .background(StudioTheme.BUTTON)
            .hoverBackground(StudioTheme.HOVER)
            .overlay(
                IKey.dynamic(() -> label)
                    .color(() -> available.getAsBoolean() ? 0xffe7e8eb : 0xff777777))
            .onMousePressed(mouse -> {
                if (mouse != 0) return false;
                if (available.getAsBoolean()) action.run();
                return true;
            });
    }
}
