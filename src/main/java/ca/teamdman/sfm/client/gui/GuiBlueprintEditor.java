/*
 * RetroSFM visual editor port. Graph concepts adapted from Saereth's MIT-licensed RetroSFM.
 * Copyright (c) 2026 Saereth. See META-INF/licenses/RetroSFM-MIT.txt.
 * Rendering/input adapter for Minecraft 1.7.10 is specific to this port.
 */
package ca.teamdman.sfm.client.gui;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import ca.teamdman.sfm.client.blueprint.BlueprintCompiler;
import ca.teamdman.sfm.client.blueprint.BlueprintDocument;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Edge;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Pin;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.gui.syntax.SfmlHighlighter;
import ca.teamdman.sfm.client.gui.syntax.SfmlSyntaxRenderer;
import ca.teamdman.sfm.common.tileentity.TileEntityManager;

/**
 * Client-only canvas. Compilation and layout encoding happen on edits/save, never while drawing or ticking a manager.
 */
public final class GuiBlueprintEditor extends GuiScreen implements ProgramEditSession.Receiver {

    private static final int NODE_W = 144;
    private final TileEntityManager manager;
    private final GuiScreen parent;
    private String original = "";
    private final ProgramEditSession editSession;
    private boolean openRequested;
    private final Deque<BlueprintGraph> undo = new ArrayDeque<>();
    private BlueprintGraph graph;
    private UUID selected, wireFrom;
    private Pin wirePin;
    private GuiTextField field;
    private GuiButton save, apply;
    private String status = "";
    private String preview = "";
    private String[] previewLines = new String[0];
    private SfmlHighlighter.Highlighted highlighted = SfmlHighlighter.highlight("");
    private boolean valid, showPreview, dirty, editingName, dragging, dragCheckpoint, panning;
    private double panX = 24, panY = 16, zoom = 1;
    private int lastX, lastY, previewOffset;
    private boolean firstLayout = true;

    public GuiBlueprintEditor(TileEntityManager manager, GuiScreen parent) {
        this.manager = manager;
        this.parent = parent;
        editSession = new ProgramEditSession(
            manager,
            net.minecraft.client.Minecraft.getMinecraft().thePlayer.openContainer.windowId);
        status = "Loading program from server";
    }

    @Override
    public void receiveProgramEditReply(ca.teamdman.sfm.common.net.PacketProgramEditReply reply) {
        ProgramEditState.Action action = editSession.state.action();
        if (!editSession.receive(reply)) return;
        if (!reply.accepted || reply.snapshot == null) {
            status = reply.message;
            updateButtons();
            return;
        }
        if (action == ProgramEditState.Action.SAVE) {
            dirty = false;
            mc.displayGuiScreen(parent);
            return;
        }
        original = reply.snapshot.getSource();
        try {
            BlueprintDocument document = BlueprintDocument.open(original);
            graph = document.graph;
            status = document.notice;
            rebuildPreview(false);
        } catch (IllegalArgumentException e) {
            // An unrepresentable program stays untouched. Text remains available from this screen.
            status = e.getMessage();
            graph = null;
            preview = original;
            previewLines = original.split("\n", -1);
            highlighted = SfmlHighlighter.highlight(preview);
            showPreview = true;
        }
        fit();
        updateButtons();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        save = add(0, 6, 6, 48, "Save");
        add(1, 56, 6, 48, "Cancel");
        add(2, 106, 6, 46, "Text");
        add(3, 154, 6, 58, "Preview");
        add(4, 214, 6, 34, "Fit");
        add(5, 250, 6, 32, "Undo");
        add(6, 284, 6, 30, "Copy");
        String[] labels = { "Timer", "Pulse", "Input", "Output", "If", "Forget", "Signal" };
        for (int i = 0; i < labels.length; i++) add(10 + i, 6 + i * 39, 28, 37, labels[i]);
        add(7, 281, 28, 33, "Name");
        String old = field == null ? "" : field.getText();
        field = new GuiTextField(fontRendererObj, 8, height - 47, width - 89, 16);
        field.setMaxStringLength(BlueprintGraph.MAX_CLAUSE);
        field.setText(old);
        apply = add(8, width - 76, height - 49, 68, "Apply");
        if (firstLayout) {
            fit();
            firstLayout = false;
        }
        updateButtons();
    }

    @SuppressWarnings("unchecked")
    private GuiButton add(int id, int x, int y, int w, String title) {
        GuiButton button = new GuiButton(id, x, y, w, 18, title);
        buttonList.add(button);
        return button;
    }

    private int bottom() {
        return height - 68;
    }

    private void updateButtons() {
        boolean editable = graph != null && editSession.state.isEditable();
        for (Object obj : buttonList) {
            GuiButton button = (GuiButton) obj;
            if (editSession.state.isPending()) {
                button.enabled = false;
                continue;
            }
            button.enabled = true;
            if (button.id >= 10 || button.id == 5 || button.id == 7) button.enabled = editable;
            if (button.id == 5) button.enabled = editable && !undo.isEmpty();
        }
        save.enabled = editable && valid;
        apply.enabled = editable && (selected != null || editingName);
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

    private void checkpoint() {
        undo.push(graph.copy());
        while (undo.size() > 32) undo.removeLast();
        dirty = true;
    }

    private void rebuildPreview(boolean report) {
        try {
            preview = BlueprintCompiler.generate(graph);
            previewLines = preview.split("\n", -1);
            valid = true;
            if (report) status = "Valid SFML - " + graph.nodes()
                .size() + " nodes";
        } catch (IllegalArgumentException e) {
            valid = false;
            status = e.getMessage();
            preview = "-- Preview unavailable: " + status;
            previewLines = new String[] { preview };
        }
        highlighted = SfmlHighlighter.highlight(preview);
        previewOffset = Math.min(previewOffset, Math.max(0, previewLines.length - 1));
        if (save != null) updateButtons();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;
        if (!editSession.state.isEditable() && button.id != 1) return;
        try {
            if (pendingField() && button.id != 1 && button.id != 2 && button.id != 5) applyField();
            switch (button.id) {
                case 0:
                    applyField();
                    if (!valid) return;
                    editSession.save(BlueprintDocument.save(graph));
                    status = "Saving - waiting for the server";
                    updateButtons();
                    break;
                case 1:
                    mc.displayGuiScreen(parent);
                    break;
                case 2:
                    if (dirty || pendingField()) {
                        status = "Save or Cancel this draft before switching to Text";
                        return;
                    }
                    mc.displayGuiScreen(new GuiCodeEditor(manager, parent));
                    break;
                case 3:
                    showPreview = !showPreview;
                    break;
                case 4:
                    fit();
                    break;
                case 5:
                    graph = undo.pop();
                    selected = null;
                    editingName = false;
                    wireFrom = null;
                    field.setText("");
                    dirty = true;
                    rebuildPreview(true);
                    break;
                case 6:
                    if (graph != null && !valid) {
                        status = "Connect and validate the graph before copying SFML";
                        return;
                    }
                    setClipboardString(preview);
                    status = "SFML copied (without editor layout)";
                    break;
                case 7:
                    editingName = true;
                    selected = null;
                    field.setText(graph.nameClause());
                    field.setFocused(true);
                    status = "Program header: NAME \"Program name\" (or leave empty)";
                    updateButtons();
                    break;
                case 8:
                    applyField();
                    break;
                default:
                    if (button.id >= 10 && button.id <= 16) {
                        checkpoint();
                        Type type = Type.values()[button.id - 10];
                        Node previous = graph.node(selected);
                        double x = previous != null && !type.isTrigger() ? previous.x() : (width / 2.0 - panX) / zoom;
                        double y = previous != null && !type.isTrigger() ? previous.y() + nodeHeight(previous) + 32
                            : (bottom() / 2.0 - 50 - panY) / zoom;
                        x = Math.max(-999000, Math.min(999000, x));
                        y = Math.max(-999000, Math.min(999000, y));
                        while (occupied(x, y, type == Type.IF ? 88 : 48)) y += 120;
                        Node node = new Node(type, x, y);
                        // Append to a selected continuation when it is empty; otherwise place an unwired node.
                        graph.add(node);
                        if (previous != null && !type.isTrigger()
                            && graph.next(previous.id, previous.type.continuation()) == null)
                            graph.connect(previous.id, previous.type.continuation(), node.id);
                        select(node);
                        if (node.x() < worldX(6) || node.x() + NODE_W > worldX(width - 6)
                            || node.y() < worldY(50)
                            || node.y() + nodeHeight(node) > worldY(bottom())) {
                            panX = width / 2.0 - 6 - (node.x() + NODE_W / 2.0) * zoom;
                            panY = (bottom() - 50) / 2.0 - (node.y() + nodeHeight(node) / 2.0) * zoom;
                        }
                        showPreview = false;
                        rebuildPreview(true);
                    }
            }
        } catch (IllegalArgumentException e) {
            editSession.state.cancel();
            status = e.getMessage();
            updateButtons();
        }
    }

    private boolean occupied(double x, double y, int height) {
        for (Node node : graph.nodes()) if (x < node.x() + NODE_W + 12 && x + NODE_W + 12 > node.x()
            && y < node.y() + nodeHeight(node) + 12
            && y + height + 12 > node.y()) return true;
        return false;
    }

    private void applyField() {
        if (graph == null || editSession.state.isPending()) return;
        String value = field.getText()
            .trim();
        Node node = graph.node(selected);
        if (editingName) {
            if (!value.equals(graph.nameClause())) {
                BlueprintGraph probe = graph.copy();
                probe.setNameClause(value);
                BlueprintCompiler.generate(probe);
                checkpoint();
                graph.setNameClause(value);
            }
        } else if (node != null && !value.equals(node.clause())) {
            BlueprintCompiler.validateClause(node.type, value);
            checkpoint();
            node.setClause(value);
        }
        rebuildPreview(true);
    }

    private void select(Node node) {
        selected = node.id;
        editingName = false;
        field.setText(node.clause());
        field.setFocused(false);
        updateButtons();
    }

    @Override
    public void updateScreen() {
        field.updateCursorCounter();
        if (!openRequested) {
            openRequested = true;
            editSession.open();
        }
        if (editSession.state.tick()) {
            status = "No server confirmation. Draft kept; reopen if the program changed.";
            updateButtons();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        editSession.state.cancel();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void keyTyped(char character, int key) {
        if (!editSession.state.isEditable()) return;
        if (key == Keyboard.KEY_ESCAPE) {
            field.setFocused(false);
            if (wireFrom != null) {
                wireFrom = null;
                status = "Wire cancelled";
            } else if (dirty || pendingField()) status = "Unsaved draft - choose Save or Cancel";
            else mc.displayGuiScreen(parent);
            return;
        }
        if (field.isFocused()) {
            if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) {
                try {
                    applyField();
                    field.setFocused(false);
                } catch (IllegalArgumentException e) {
                    status = e.getMessage();
                }
            } else field.textboxKeyTyped(character, key);
            return;
        }
        if (graph != null && key == Keyboard.KEY_DELETE && selected != null) {
            checkpoint();
            graph.remove(selected);
            selected = null;
            wireFrom = null;
            field.setText("");
            rebuildPreview(true);
        }
    }

    private double worldX(int x) {
        return (x - 6 - panX) / zoom;
    }

    private double worldY(int y) {
        return (y - 50 - panY) / zoom;
    }

    private int nodeHeight(Node node) {
        return node.type == Type.IF ? 88 : 48;
    }

    private int pinY(Node node, Pin pin) {
        return node.type == Type.IF ? 28 + (pin.ordinal() - 1) * 24 : 28;
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        if (!editSession.state.isEditable()) return;
        field.mouseClicked(x, y, button);
        if (graph == null || showPreview || y < 50 || y >= bottom() || x < 6 || x >= width - 6) return;
        if (pendingField()) {
            try {
                applyField();
            } catch (IllegalArgumentException e) {
                status = e.getMessage();
                return;
            }
        }
        field.setFocused(false);
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
                    return;
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
                return;
            }
            if (wx >= node.x() && wx <= node.x() + NODE_W && wy >= node.y() && wy <= node.y() + nodeHeight(node)) {
                select(node);
                if (button == 0 && wy < node.y() + 18) {
                    dragging = true;
                    dragCheckpoint = false;
                }
                return;
            }
        }
        if (button == 1 || button == 2) panning = true;
    }

    @Override
    protected void mouseClickMove(int x, int y, int button, long elapsed) {
        if (!editSession.state.isEditable()) return;
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
    protected void mouseMovedOrUp(int x, int y, int button) {
        super.mouseMovedOrUp(x, y, button);
        dragging = false;
        dragCheckpoint = false;
        panning = false;
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        int x = Mouse.getEventX() * width / mc.displayWidth;
        int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        if (wheel == 0 || y < 50 || y >= bottom()) return;
        if (showPreview) previewOffset = Math.max(
            0,
            Math.min(
                Math.max(0, previewLines.length - (bottom() - 56) / 10),
                previewOffset - Integer.signum(wheel) * 3));
        else {
            double wx = worldX(x), wy = worldY(y);
            zoom = Math.max(0.25, Math.min(1.5, zoom * (wheel > 0 ? 1.1 : 1 / 1.1)));
            panX = x - 6 - wx * zoom;
            panY = y - 50 - wy * zoom;
        }
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
            0.25,
            Math.min(1, Math.min((width - 40) / (maxX - minX), Math.max(40, bottom() - 66) / (maxY - minY))));
        panX = 12 - minX * zoom;
        panY = 10 - minY * zoom;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(4, 48, width - 4, bottom() + 2, 0xff151718);
        drawRect(6, 50, width - 6, bottom(), 0xff26292b);
        // Clip in physical pixels so every Minecraft GUI scale uses the same logical layout.
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(
            6 * mc.displayWidth / width,
            (height - bottom()) * mc.displayHeight / height,
            (width - 12) * mc.displayWidth / width,
            Math.max(0, bottom() - 50) * mc.displayHeight / height);
        if (showPreview) {
            for (int row = 0; row < (bottom() - 54) / 10 && previewOffset + row < previewLines.length; row++) {
                int line = previewOffset + row;
                int end = fontRendererObj.trimStringToWidth(previewLines[line], width - 28)
                    .length();
                SfmlSyntaxRenderer.draw(fontRendererObj, highlighted, line, 0, end, 12, 54 + row * 10);
            }
        } else if (graph != null) {
            GL11.glPushMatrix();
            GL11.glTranslated(6 + panX, 50 + panY, 0);
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
                    (int) worldX(mouseX),
                    (int) worldY(mouseY),
                    0xffead79a);
            }
            for (Node node : graph.nodes()) if (node.x() + NODE_W >= worldX(6) && node.x() <= worldX(width - 6)
                && node.y() + nodeHeight(node) >= worldY(50)
                && node.y() <= worldY(bottom())) drawNode(node);
            GL11.glPopMatrix();
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        String help = showPreview ? "SFML preview - wheel scrolls; Copy exports text"
            : "Drag headers | wheel zoom | right-drag pan | right-click pin unwires | Del removes";
        fontRendererObj.drawString(fontRendererObj.trimStringToWidth(help, width - 16), 8, bottom() + 4, 0xffaaaaaa);
        field.drawTextBox();
        fontRendererObj.drawString(
            fontRendererObj.trimStringToWidth(status, width - 16),
            8,
            height - 25,
            valid ? 0xffb9cfad : 0xffe0b58b);
        fontRendererObj.drawString(
            fontRendererObj.trimStringToWidth(
                editingName ? "NAME header"
                    : selected != null ? "Selected node's SFML clause; Enter or Apply commits"
                        : "Select a node, then edit its clause here",
                width - 16),
            8,
            height - 13,
            0xff9a9a9a);
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (mouseY >= height - 27 && mouseY < height - 15 && !status.isEmpty()) drawHoveringText(
            fontRendererObj.listFormattedStringToWidth(status, Math.max(100, width - 40)),
            mouseX,
            mouseY,
            fontRendererObj);
    }

    private void drawNode(Node node) {
        int x = (int) node.x(), y = (int) node.y(), h = nodeHeight(node);
        drawRect(x - 1, y - 1, x + NODE_W + 1, y + h + 1, node.id.equals(selected) ? 0xffe7d699 : 0xff101010);
        drawRect(x, y, x + NODE_W, y + h, 0xff424648);
        drawRect(x, y, x + NODE_W, y + 16, 0xff000000 | node.type.color);
        fontRendererObj.drawString(node.type.title, x + 5, y + 4, 0xffeeeeee);
        fontRendererObj.drawString(
            fontRendererObj.trimStringToWidth(node.clause(), NODE_W - (node.type == Type.IF ? 45 : 12)),
            x + 6,
            y + 24,
            0xffdddddd);
        if (!node.type.isTrigger()) drawRect(x - 4, y + 24, x + 4, y + 32, 0xffc6c9ca);
        for (Pin pin : Pin.values()) if (node.type.hasPin(pin)) {
            int py = y + pinY(node, pin);
            drawRect(x + NODE_W - 4, py - 4, x + NODE_W + 4, py + 4, pin == Pin.FALSE ? 0xffb87d73 : 0xffa0bb91);
            if (node.type == Type.IF) fontRendererObj.drawString(pin.label(), x + NODE_W - 36, py - 3, 0xffdddddd);
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
        drawRect(Math.min(x1, x2), y - 1, Math.max(x1, x2) + 1, y + 1, color);
    }

    private void vertical(int x, int y1, int y2, int color) {
        drawRect(x - 1, Math.min(y1, y2), x + 1, Math.max(y1, y2) + 1, color);
    }
}
