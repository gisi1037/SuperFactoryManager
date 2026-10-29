/*
 * RetroSFM visual editor port. Graph concepts adapted from Saereth's MIT-licensed RetroSFM.
 * Copyright (c) 2026 Saereth. See META-INF/licenses/RetroSFM-MIT.txt.
 * Rendering/input adapter for Minecraft 1.7.10 is specific to this port.
 */
package ca.teamdman.sfm.client.gui.mui2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.client.blueprint.BlueprintDocument;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Edge;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Pin;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.gui.ProgramEditSession;
import ca.teamdman.sfm.client.gui.ProgramEditState;
import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter;
import ca.teamdman.sfm.client.gui.syntax.SfmlSyntaxRenderer;
import ca.teamdman.sfm.common.net.PacketProgramEditReply;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;
import me.eigenraven.lwjgl3ify.api.InputEvents;

/** Native secondary MUI2 panel; only graph rendering and hit testing are custom. */
public final class Mui2BlueprintEditor implements ProgramEditSession.Receiver {

    private static final int NODE_W = 144;
    private final ProgramEditSession editSession;
    private final Runnable closeAction, openText;
    private Draft pendingDraft;
    private UUID canonicalDocumentId;
    private String canonicalSource = "";
    private boolean recoveryOnly, cancelled;
    private final Deque<BlueprintGraph> undo = new ArrayDeque<>();
    private BlueprintGraph graph;
    private BlueprintDocument openedDocument;
    private UUID selected, wireFrom;
    private Pin wirePin;
    private TextFieldWidget field;
    private Canvas canvas;
    private String property = "", status = "Loading program from server", preview = "";
    private String[] previewLines = new String[0];
    private SfmlHighlighter.Highlighted highlighted = SfmlHighlighter.highlight("");
    private boolean opened, valid, dirty, editingName, showPreview;
    private boolean dragging, panning, dragCheckpoint;
    private double panX = 24, panY = 16, zoom = 1;
    private int lastX, lastY, previewOffset;

    public Mui2BlueprintEditor(TileEntityManager manager, int windowId, Runnable closeAction, Runnable openText) {
        this(manager, windowId, closeAction, openText, null);
    }

    public Mui2BlueprintEditor(TileEntityManager manager, int windowId, Runnable closeAction, Runnable openText,
        Draft draft) {
        pendingDraft = draft;
        editSession = new ProgramEditSession(manager, windowId);
        this.closeAction = closeAction;
        this.openText = openText;
    }

    public ModularPanel build() {
        ModularPanel panel = new ModularPanel("sfm_blueprint") {

            @Override
            public void closeIfOpen() {
                if (isSaving()) {
                    status = "Saving - wait for server confirmation";
                    return;
                }
                super.closeIfOpen();
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
                int x = Math.max(2, Math.min(getArea().rx, getParentArea().w() - getArea().w() - 2));
                int y = Math.max(2, Math.min(getArea().ry, getParentArea().h() - getArea().h() - 2));
                if (x != getArea().rx || y != getArea().ry) {
                    left(x).top(y);
                    scheduleResize();
                }
            }

            @Override
            public boolean disablePanelsBelow() {
                return true;
            }

            @Override
            public boolean onKeyPressed(char character, int key) {
                if (cancelled) return true;
                if (key == Keyboard.KEY_ESCAPE) {
                    escape();
                    return true;
                }
                if (editSession.state.isPending()) return true;
                if (key == Keyboard.KEY_DELETE && !field.isFocused()) {
                    removeSelected();
                    return true;
                }
                return super.onKeyPressed(character, key);
            }
        }.widthRel(.98f)
            .heightRel(.98f)
            .resizeableOnDrag(true)
            .onCloseAction(this::cancel);
        String[] titles = { "Save", "Cancel", "Text", "Preview", "Fit", "Undo", "Copy", "Name", "Delete" };
        for (int i = 0; i < titles.length; i++) {
            final int action = i;
            panel.child(
                button(titles[i], 6 + (i % 5) * 58, 5 + (i / 5) * 23, 54, () -> action(action))
                    .overlay(IKey.dynamic(() -> (enabled(action) ? "" : "\u00a78") + titles[action])));
        }
        for (int i = 0; i < Type.values().length; i++) {
            final int action = 10 + i;
            panel.child(
                button(
                    new String[] { "Timer", "Pulse", "Input", "Output", "If", "Forget", "Signal" }[i],
                    6 + i * 41,
                    51,
                    39,
                    () -> action(action)).overlay(
                        IKey.dynamic(
                            () -> (editable() ? "" : "\u00a78")
                                + new String[] { "Timer", "Pulse", "Input", "Output", "If", "Forget", "Signal" }[action
                                    - 10])));
        }
        canvas = new Canvas();
        panel.child(
            canvas.left(8)
                .right(8)
                .top(76)
                .bottom(76));
        panel.child(
            new TextWidget<>("Drag headers | wheel zoom/scroll | right-drag pan | right-click pin unwires").left(8)
                .right(8)
                .bottom(61)
                .height(12));
        field = new TextFieldWidget() {

            @Override
            public boolean onTextInput(InputEvents.TextEvent event) {
                return !editable() || super.onTextInput(event);
            }

            @Override
            public Result onMousePressed(int button) {
                return editable() ? super.onMousePressed(button) : Result.STOP;
            }

            @Override
            public Result onKeyPressed(char character, int key) {
                if (!editable()) return Result.STOP;
                if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) {
                    safeApply();
                    return Result.SUCCESS;
                }
                return super.onKeyPressed(character, key);
            }
        }.setMaxLength(BlueprintGraph.MAX_CLAUSE)
            .autoUpdateOnChange(true)
            .value(new StringValue.Dynamic(() -> property, value -> property = value));
        panel.child(
            field.left(8)
                .right(82)
                .bottom(39)
                .height(18));
        panel.child(
            new ButtonWidget<>().right(8)
                .bottom(39)
                .size(68, 20)
                .overlay(IKey.str("Apply"))
                .onMousePressed(button -> {
                    if (button != 0) return false;
                    safeApply();
                    return true;
                })
                .overlay(IKey.dynamic(() -> (editable() ? "" : "\u00a78") + "Apply")));
        panel.child(
            new TextWidget<>(IKey.dynamic(() -> status)).left(8)
                .right(8)
                .bottom(22)
                .height(12));
        panel.child(
            new TextWidget<>(
                IKey.dynamic(() -> editingName ? "NAME header" : "Selected node SFML clause; Enter or Apply commits"))
                    // Stock pack inventory buttons occupy the bottom-left 40 logical pixels.
                    .left(48)
                    .right(8)
                    .bottom(7)
                    .height(12));
        return panel;
    }

    public void tick() {
        if (cancelled) return;
        if (!opened) {
            opened = true;
            editSession.open();
        }
        if (editSession.state.tick())
            status = "No server confirmation; save may have applied. Draft kept; Copy exports SFML.";
    }

    @Override
    public void receiveProgramEditReply(PacketProgramEditReply reply) {
        if (cancelled) return;
        ProgramEditState.Action action = editSession.state.action();
        if (!editSession.receive(reply)) return;
        if (!reply.accepted || reply.snapshot == null) {
            status = reply.message;
            return;
        }
        if (action == ProgramEditState.Action.SAVE) {
            dirty = false;
            closeAction.run();
            return;
        }
        canonicalDocumentId = reply.snapshot.getDocumentId();
        canonicalSource = reply.snapshot.getSource();
        // Keep the authoritative source baseline even when restoring an editable local draft.
        // A draft for a changed document remains Copy-only and can never save against this baseline.
        openedDocument = null;
        try {
            openedDocument = BlueprintDocument.open(canonicalSource);
        } catch (IllegalArgumentException unsupported) {
            status = unsupported.getMessage();
        }
        if (pendingDraft != null) {
            Draft recovered = pendingDraft;
            pendingDraft = null;
            graph = recovered.graphCopy();
            selected = recovered.selected;
            property = recovered.property;
            editingName = recovered.editingName;
            dirty = recovered.dirty;
            preview = recovered.preview;
            recoveryOnly = !recovered.matches(canonicalDocumentId, canonicalSource);
            canonicalDocumentId = recovered.documentId;
            canonicalSource = recovered.canonicalSource;
            if (graph != null) rebuildPreview(false);
            else {
                previewLines = preview.split("\n", -1);
                highlighted = SfmlHighlighter.highlight(preview);
            }
            if (field != null) field.setText(property);
            fit();
            status = recoveryOnly
                ? "Disk document changed. Draft recovered for Copy only; close and reopen to edit current source."
                : "Visual draft restored; review properties and graph before saving.";
            return;
        }
        try {
            if (openedDocument == null) throw new IllegalArgumentException(status);
            graph = openedDocument.graph;
            status = openedDocument.notice;
            rebuildPreview(false);
            fit();
        } catch (IllegalArgumentException error) {
            graph = null;
            status = error.getMessage();
            preview = reply.snapshot.getSource();
            previewLines = preview.split("\n", -1);
            highlighted = SfmlHighlighter.highlight(preview);
            showPreview = true;
        }
    }

    public void cancel() {
        cancelled = true;
        editSession.state.cancel();
        dragging = panning = dragCheckpoint = false;
        clearOwnedFocus();
    }

    private void clearOwnedFocus() {
        ModularScreen current = ModularScreen.getCurrent();
        if (current == null) return;
        Object focused = current.getContext()
            .getFocusedWidget()
            .getElement();
        if (focused == field || focused == canvas) current.getContext()
            .removeFocus();
    }

    public boolean hasDraft() {
        return pendingDraft != null || recoveryOnly || dirty || pendingField();
    }

    public String draft() {
        return exportDraft().recoveryText();
    }

    public Draft exportDraft() {
        if (pendingDraft != null) return pendingDraft;
        return new Draft(
            graph,
            selected,
            field == null ? property : field.getText(),
            editingName,
            dirty,
            canonicalDocumentId,
            canonicalSource,
            preview);
    }

    /** Heap-only defensive snapshot: no tile, world, session token, widget or callback references. */
    public static final class Draft {

        private final BlueprintGraph graph;
        public final UUID selected, documentId;
        public final String property, canonicalSource, preview;
        public final boolean editingName, dirty;

        public Draft(BlueprintGraph graph, UUID selected, String property, boolean editingName, boolean dirty,
            UUID documentId, String canonicalSource, String preview) {
            this.graph = graph == null ? null : graph.copy();
            this.selected = selected;
            this.property = property;
            this.editingName = editingName;
            this.dirty = dirty;
            this.documentId = documentId;
            this.canonicalSource = canonicalSource;
            this.preview = preview;
        }

        public BlueprintGraph graphCopy() {
            return graph == null ? null : graph.copy();
        }

        public boolean matches(UUID documentId, String source) {
            return this.documentId != null && this.documentId.equals(documentId) && canonicalSource.equals(source);
        }

        /** Invalid control flow and unapplied properties are exported as explicitly non-executable notes. */
        public String recoveryText() {
            if (graph == null) return preview;
            try {
                String generated = BlueprintCompiler.generate(graph);
                Node node = graph.node(selected);
                boolean pending = editingName ? !property.trim()
                    .equals(graph.nameClause())
                    : node != null && !property.trim()
                        .equals(node.clause());
                if (!pending) return generated;
            } catch (IllegalArgumentException invalid) { /* Preserve the full incomplete graph below. */ }
            StringBuilder text = new StringBuilder(
                "-- SFM INCOMPLETE VISUAL DRAFT - recovery notes, not executable SFML\n");
            text.append("-- Name: ")
                .append(graph.nameClause())
                .append('\n');
            text.append("-- Comments: ")
                .append(graph.comments())
                .append('\n');
            for (Node node : graph.nodes()) text.append("-- Node ")
                .append(node.id)
                .append(' ')
                .append(node.type)
                .append(" at ")
                .append(node.x())
                .append(',')
                .append(node.y())
                .append(" clause: ")
                .append(node.clause())
                .append('\n');
            for (Edge edge : graph.edges()) text.append("-- Wire ")
                .append(edge.from)
                .append(' ')
                .append(edge.pin)
                .append(" -> ")
                .append(edge.to)
                .append('\n');
            text.append("-- Selected: ")
                .append(selected)
                .append(editingName ? " NAME" : " clause")
                .append("\n-- Unapplied property: ")
                .append(property)
                .append('\n');
            return text.toString();
        }
    }

    public boolean canClose() {
        return cancelled || !isSaving();
    }

    public boolean isSaving() {
        return editSession.state.isPending() && editSession.state.action() == ProgramEditState.Action.SAVE;
    }

    private boolean editable() {
        return !cancelled && !recoveryOnly && graph != null && editSession.state.isEditable();
    }

    private boolean enabled(int action) {
        if (cancelled) return false;
        if (action == 6) return pendingDraft != null || graph != null || editSession.state.snapshot() != null;
        if (editSession.state.isPending()) return false;
        if (action == 1) return true;
        if (action == 2) return editSession.state.snapshot() != null;
        if (!editable()) return false;
        return action != 0 || valid;
    }

    private void escape() {
        if (isSaving()) {
            status = "Saving - wait for server confirmation";
            return;
        }
        if (wireFrom != null) {
            wireFrom = null;
            status = "Wire cancelled";
        } else if (hasDraft()) status = "Unsaved draft - choose Save, Copy or Cancel";
        else closeAction.run();
    }

    private void action(int action) {
        if (!enabled(action < 10 ? action : 7)) return;
        try {
            if (pendingField() && action != 1 && action != 2 && action != 5 && action != 6) applyField();
            switch (action) {
                case 0:
                    applyField();
                    if (valid) {
                        editSession.save(
                            openedDocument == null ? BlueprintDocument.save(graph) : openedDocument.saveEdited(graph));
                        clearOwnedFocus();
                        status = "Saving - waiting for server";
                    }
                    break;
                case 1:
                    closeAction.run();
                    break;
                case 2:
                    if (hasDraft()) status = "Save or Cancel this draft before switching to Text";
                    else openText.run();
                    break;
                case 3:
                    showPreview = !showPreview;
                    break;
                case 4:
                    fit();
                    break;
                case 5:
                    if (!undo.isEmpty()) {
                        graph = undo.pop();
                        selected = wireFrom = null;
                        editingName = false;
                        property = "";
                        field.setText("");
                        dirty = true;
                        rebuildPreview(true);
                    }
                    break;
                case 6:
                    GuiScreen.setClipboardString(draft());
                    status = "Draft copied; incomplete graphs/properties are recovery notes";
                    break;
                case 7:
                    editingName = true;
                    selected = null;
                    property = graph.nameClause();
                    field.setText(property);
                    break;
                case 8:
                    removeSelected();
                    break;
                default:
                    add(Type.values()[action - 10]);
            }
        } catch (IllegalArgumentException error) {
            editSession.state.cancel();
            status = error.getMessage();
        }
    }

    private void add(Type type) {
        checkpoint();
        Node previous = graph.node(selected);
        double x = previous != null && !type.isTrigger() ? previous.x() : worldX(canvas.getArea().width / 2);
        double y = previous != null && !type.isTrigger() ? previous.y() + nodeHeight(previous) + 32
            : worldY(canvas.getArea().height / 2);
        while (occupied(x, y, type == Type.IF ? 88 : 48)) y += 120;
        Node node = new Node(type, Math.max(-999000, Math.min(999000, x)), Math.max(-999000, Math.min(999000, y)));
        graph.add(node);
        if (previous != null && !type.isTrigger() && graph.next(previous.id, previous.type.continuation()) == null)
            graph.connect(previous.id, previous.type.continuation(), node.id);
        select(node);
        showPreview = false;
        rebuildPreview(true);
    }

    private void removeSelected() {
        if (!editable() || selected == null) return;
        checkpoint();
        graph.remove(selected);
        selected = wireFrom = null;
        property = "";
        field.setText("");
        rebuildPreview(true);
    }

    private void safeApply() {
        try {
            applyField();
        } catch (IllegalArgumentException error) {
            status = error.getMessage();
        }
    }

    private void select(Node node) {
        selected = node.id;
        editingName = false;
        property = node.clause();
        field.setText(property);
    }

    private boolean pendingField() {
        if (field == null || graph == null) return false;
        Node node = graph.node(selected);
        return editingName ? !field.getText()
            .trim()
            .equals(graph.nameClause())
            : node != null && !field.getText()
                .trim()
                .equals(node.clause());
    }

    private void applyField() {
        if (!editable()) return;
        String value = field.getText()
            .trim();
        Node node = graph.node(selected);
        if (editingName && !value.equals(graph.nameClause())) {
            BlueprintGraph probe = graph.copy();
            probe.setNameClause(value);
            BlueprintCompiler.generate(probe);
            checkpoint();
            graph.setNameClause(value);
        } else if (!editingName && node != null && !value.equals(node.clause())) {
            BlueprintCompiler.validateClause(node.type, value);
            checkpoint();
            node.setClause(value);
        }
        rebuildPreview(true);
    }

    private void checkpoint() {
        undo.push(graph.copy());
        while (undo.size() > 32) undo.removeLast();
        dirty = true;
    }

    private void rebuildPreview(boolean report) {
        try {
            preview = BlueprintCompiler.generate(graph);
            valid = true;
            if (report) status = "Valid SFML - " + graph.nodes()
                .size() + " nodes";
        } catch (IllegalArgumentException error) {
            valid = false;
            status = error.getMessage();
            preview = "-- Preview unavailable: " + status;
        }
        previewLines = preview.split("\n", -1);
        highlighted = SfmlHighlighter.highlight(preview);
        previewOffset = Math.min(previewOffset, Math.max(0, previewLines.length - 1));
    }

    private boolean occupied(double x, double y, int h) {
        for (Node node : graph.nodes()) if (x < node.x() + NODE_W + 12 && x + NODE_W + 12 > node.x()
            && y < node.y() + nodeHeight(node) + 12
            && y + h + 12 > node.y()) return true;
        return false;
    }

    private double worldX(int x) {
        return (x - panX) / zoom;
    }

    private double worldY(int y) {
        return (y - panY) / zoom;
    }

    private int nodeHeight(Node node) {
        return node.type == Type.IF ? 88 : 48;
    }

    private int pinY(Node node, Pin pin) {
        return node.type == Type.IF ? 28 + (pin.ordinal() - 1) * 24 : 28;
    }

    private void fit() {
        if (graph == null || graph.nodes()
            .isEmpty()) {
            zoom = 1;
            panX = 24;
            panY = 16;
            return;
        }
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (Node node : graph.nodes()) {
            minX = Math.min(minX, node.x());
            minY = Math.min(minY, node.y());
            maxX = Math.max(maxX, node.x() + NODE_W);
            maxY = Math.max(maxY, node.y() + nodeHeight(node));
        }
        zoom = Math.max(
            .25,
            Math.min(
                1,
                Math.min(
                    (canvas.getArea().width - 24) / (maxX - minX),
                    (canvas.getArea().height - 20) / (maxY - minY))));
        panX = 12 - minX * zoom;
        panY = 10 - minY * zoom;
    }

    private void drawNode(Node node) {
        int x = (int) node.x(), y = (int) node.y(), h = nodeHeight(node);
        Gui.drawRect(x - 1, y - 1, x + NODE_W + 1, y + h + 1, node.id.equals(selected) ? 0xffe7d699 : 0xff101010);
        Gui.drawRect(x, y, x + NODE_W, y + h, 0xff424648);
        Gui.drawRect(x, y, x + NODE_W, y + 16, 0xff000000 | node.type.color);
        Minecraft.getMinecraft().fontRenderer.drawString(node.type.title, x + 5, y + 4, 0xffeeeeee);
        Minecraft.getMinecraft().fontRenderer.drawString(
            Minecraft.getMinecraft().fontRenderer
                .trimStringToWidth(node.clause(), NODE_W - (node.type == Type.IF ? 45 : 12)),
            x + 6,
            y + 24,
            0xffdddddd);
        if (!node.type.isTrigger()) Gui.drawRect(x - 4, y + 24, x + 4, y + 32, 0xffc6c9ca);
        for (Pin pin : Pin.values()) if (node.type.hasPin(pin)) {
            int py = y + pinY(node, pin);
            Gui.drawRect(x + NODE_W - 4, py - 4, x + NODE_W + 4, py + 4, pin == Pin.FALSE ? 0xffb87d73 : 0xffa0bb91);
            if (node.type == Type.IF)
                Minecraft.getMinecraft().fontRenderer.drawString(pin.label(), x + NODE_W - 36, py - 3, 0xffdddddd);
        }
    }

    private void wire(int belowSource, int x1, int y1, int x2, int y2, int color) {
        int lane = x2 - 12;
        if (x2 > x1 + 24) {
            horizontal(x1, lane, y1, color);
            vertical(lane, y1, y2, color);
        } else {
            // A continuation returns to the original column: leave the source body before crossing its column.
            int exit = x1 + 12;
            horizontal(x1, exit, y1, color);
            vertical(exit, y1, belowSource, color);
            horizontal(exit, lane, belowSource, color);
            vertical(lane, belowSource, y2, color);
        }
        horizontal(lane, x2, y2, color);
    }

    private void horizontal(int x1, int x2, int y, int color) {
        Gui.drawRect(Math.min(x1, x2), y - 1, Math.max(x1, x2) + 1, y + 1, color);
    }

    private void vertical(int x, int y1, int y2, int color) {
        Gui.drawRect(x - 1, Math.min(y1, y2), x + 1, Math.max(y1, y2) + 1, color);
    }

    private final class Canvas extends Widget<Canvas> implements Interactable {

        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> theme) {
            int width = getArea().width, height = getArea().height;
            Gui.drawRect(0, 0, width, height, 0xff26292b);
            Stencil.apply(0, 0, width, height, context);
            try {
                if (showPreview) {
                    for (int row = 0; row < height / 10 && previewOffset + row < previewLines.length; row++) {
                        int line = previewOffset + row;
                        int end = Minecraft.getMinecraft().fontRenderer.trimStringToWidth(previewLines[line], width - 8)
                            .length();
                        SfmlSyntaxRenderer
                            .draw(Minecraft.getMinecraft().fontRenderer, highlighted, line, 0, end, 4, 4 + row * 10);
                    }
                } else if (graph != null) {
                    GL11.glPushMatrix();
                    try {
                        GL11.glTranslated(panX, panY, 0);
                        GL11.glScaled(zoom, zoom, 1);
                        for (Edge edge : graph.edges()) {
                            Node from = graph.node(edge.from), to = graph.node(edge.to);
                            wire(
                                (int) from.y() + nodeHeight(from) + 12,
                                (int) from.x() + NODE_W,
                                (int) from.y() + pinY(from, edge.pin),
                                (int) to.x(),
                                (int) to.y() + 28,
                                0xffb2b9a7);
                        }
                        if (wireFrom != null) {
                            Node from = graph.node(wireFrom);
                            if (from != null) wire(
                                (int) from.y() + nodeHeight(from) + 12,
                                (int) from.x() + NODE_W,
                                (int) from.y() + pinY(from, wirePin),
                                (int) worldX(context.getMouseX()),
                                (int) worldY(context.getMouseY()),
                                0xffead79a);
                        }
                        for (Node node : graph.nodes()) drawNode(node);
                    } finally {
                        GL11.glPopMatrix();
                    }
                }
            } finally {
                Stencil.remove();
            }
        }

        @Override
        public Result onMousePressed(int button) {
            if (cancelled) return Result.IGNORE;
            if (!editable() || showPreview) return Result.STOP;
            try {
                if (pendingField()) applyField();
            } catch (IllegalArgumentException error) {
                status = error.getMessage();
                return Result.STOP;
            }
            getContext().removeFocus();
            int x = getContext().getMouseX(), y = getContext().getMouseY();
            lastX = x;
            lastY = y;
            double wx = worldX(x), wy = worldY(y);
            List<Node> nodes = new ArrayList<>(graph.nodes());
            for (int i = nodes.size() - 1; i >= 0; i--) {
                Node node = nodes.get(i);
                for (Pin pin : Pin.values()) if (node.type.hasPin(pin) && Math.abs(wx - node.x() - NODE_W) <= 7
                    && Math.abs(wy - node.y() - pinY(node, pin)) <= 7) {
                        if (button == 1) {
                            checkpoint();
                            graph.disconnect(node.id, pin);
                            rebuildPreview(true);
                        } else if (button == 0) {
                            wireFrom = node.id;
                            wirePin = pin;
                            status = "Click an input pin to connect; Esc cancels";
                        }
                        return Result.SUCCESS;
                    }
                if (!node.type.isTrigger() && Math.abs(wx - node.x()) <= 7 && Math.abs(wy - node.y() - 28) <= 7) {
                    if (button == 1) {
                        checkpoint();
                        graph.disconnectInput(node.id);
                        rebuildPreview(true);
                    } else if (button == 0 && wireFrom != null) {
                        try {
                            checkpoint();
                            graph.connect(wireFrom, wirePin, node.id);
                            wireFrom = null;
                            rebuildPreview(true);
                        } catch (IllegalArgumentException e) {
                            status = e.getMessage();
                        }
                    } else if (button == 0) select(node);
                    return Result.SUCCESS;
                }
                if (wx >= node.x() && wx <= node.x() + NODE_W && wy >= node.y() && wy <= node.y() + nodeHeight(node)) {
                    select(node);
                    if (button == 0 && wy < node.y() + 18) {
                        dragging = true;
                        dragCheckpoint = false;
                    }
                    return Result.SUCCESS;
                }
            }
            if (button == 1 || button == 2) panning = true;
            return Result.SUCCESS;
        }

        @Override
        public void onMouseDrag(int button, long elapsed) {
            if (!editable()) return;
            int x = getContext().getMouseX(), y = getContext().getMouseY();
            if (dragging && graph != null && selected != null) {
                Node node = graph.node(selected);
                if (!dragCheckpoint && (x != lastX || y != lastY)) {
                    checkpoint();
                    dragCheckpoint = true;
                }
                node.setPosition(
                    Math.max(-1000000, Math.min(1000000, node.x() + (x - lastX) / zoom)),
                    Math.max(-1000000, Math.min(1000000, node.y() + (y - lastY) / zoom)));
            } else if (panning) {
                panX += x - lastX;
                panY += y - lastY;
            }
            lastX = x;
            lastY = y;
        }

        @Override
        public boolean onMouseRelease(int button) {
            if (cancelled) return false;
            if (dragCheckpoint) rebuildPreview(true);
            dragging = panning = dragCheckpoint = false;
            return true;
        }

        @Override
        public boolean onMouseScroll(UpOrDown direction, int amount) {
            if (cancelled || isSaving()) return false;
            if (showPreview) previewOffset = Math.max(
                0,
                Math.min(
                    Math.max(0, previewLines.length - getArea().height / 10),
                    previewOffset - direction.modifier * 3));
            else {
                int x = getContext().getMouseX(), y = getContext().getMouseY();
                double wx = worldX(x), wy = worldY(y);
                zoom = Math.max(.25, Math.min(1.5, zoom * (direction.isUp() ? 1.1 : 1 / 1.1)));
                panX = x - wx * zoom;
                panY = y - wy * zoom;
            }
            return true;
        }
    }

    private static ButtonWidget<?> button(String title, int x, int y, int width, Runnable action) {
        return new ButtonWidget<>().pos(x, y)
            .size(width, 20)
            .overlay(IKey.str(title))
            .onMousePressed(button -> {
                if (button != 0) return false;
                action.run();
                return true;
            });
    }
}
