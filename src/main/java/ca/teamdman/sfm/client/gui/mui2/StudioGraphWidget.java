/*
 * Graph interaction adapted from the RetroSFM MUI2 port, Copyright (c) 2026 Saereth.
 * See META-INF/licenses/RetroSFM-MIT.txt. Project ownership and native widget integration are specific to this port.
 */
package ca.teamdman.sfm.client.gui.mui2;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.widget.IFocusedWidget;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.Stencil;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widget.Widget;

import ca.teamdman.sfm.client.blueprint.BlueprintGraph;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Node;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Pin;
import ca.teamdman.sfm.client.blueprint.BlueprintGraph.Type;
import ca.teamdman.sfm.client.blueprint.StudioDocument;

/** Project-backed graph surface. It never opens a manager session or sends a disk-save packet. */
public final class StudioGraphWidget extends Widget<StudioGraphWidget> implements Interactable, IFocusedWidget {

    private static final int NODE_W = 144;
    private final StudioDocument document;
    private final BooleanSupplier editable;
    private BooleanSupplier inputGate = () -> true;
    private final Runnable changed, selectionChanged, addMenu;
    private Runnable focused = () -> {};
    private final Consumer<String> status;
    private UUID selected, wireFrom;
    private Pin wirePin;
    private boolean dragging, panning, moved;
    private double panX = 16, panY = 16, zoom = 1;
    private int lastX, lastY;

    public StudioGraphWidget(StudioDocument document, BooleanSupplier editable, Runnable changed,
        Runnable selectionChanged, Runnable addMenu, Consumer<String> status) {
        this.document = document;
        this.editable = editable;
        this.changed = changed;
        this.selectionChanged = selectionChanged;
        this.addMenu = addMenu;
        this.status = status;
    }

    @Override
    public boolean isFocused() {
        return isValid() && getContext().isFocused(this);
    }

    @Override
    public void onFocus(ModularGuiContext context) {
        focused.run();
    }

    public void onFocused(Runnable action) {
        focused = action;
    }

    @Override
    public void onRemoveFocus(ModularGuiContext context) {
        finishInteraction();
    }

    public void finishInteraction() {
        if (moved) {
            document.graphChanged();
            changed.run();
        }
        dragging = panning = moved = false;
    }

    public Node selected() {
        return document.graph() == null ? null
            : document.graph()
                .node(selected);
    }

    public void inputGate(BooleanSupplier inputGate) {
        this.inputGate = inputGate;
    }

    public void select(UUID id) {
        selected = id;
        selectionChanged.run();
    }

    public boolean cancelWire() {
        boolean pending = wireFrom != null;
        wireFrom = null;
        return pending;
    }

    public void resetSelection() {
        selected = wireFrom = null;
        dragging = panning = moved = false;
        selectionChanged.run();
    }

    public void add(Type type) {
        if (!editable.getAsBoolean()) return;
        try {
            Node previous = selected();
            double x = previous == null ? worldX(getArea().width / 2) - NODE_W / 2d : previous.x();
            double y = previous == null ? worldY(getArea().height / 2) - 24 : previous.y() + height(previous) + 40;
            if (type.isTrigger() && previous != null) x += NODE_W + 48;
            // Keep successive additions visible rather than stacking nodes at the same point.
            while (occupied(x, y)) y += 112;
            Node node = new Node(type, clamp(x), clamp(y));
            document.editGraph(graph -> {
                graph.add(node);
                if (previous != null && !type.isTrigger()
                    && graph.next(previous.id, previous.type.continuation()) == null)
                    graph.connect(previous.id, previous.type.continuation(), node.id);
            });
            selected = node.id;
            changed.run();
            selectionChanged.run();
        } catch (IllegalArgumentException error) {
            status.accept(error.getMessage());
        }
    }

    private boolean occupied(double x, double y) {
        for (Node node : document.graph()
            .nodes())
            if (x < node.x() + NODE_W + 16 && x + NODE_W + 16 > node.x()
                && y < node.y() + height(node) + 16
                && y + 100 > node.y()) return true;
        return false;
    }

    public void deleteSelected() {
        if (!editable.getAsBoolean() || selected() == null) return;
        document.editGraph(graph -> graph.remove(selected));
        resetSelection();
        changed.run();
    }

    public void undo(boolean redo) {
        if (!editable.getAsBoolean()) return;
        if (redo ? document.redo() : document.undo()) {
            resetSelection();
            changed.run();
        }
    }

    public void fit() {
        BlueprintGraph graph = document.graph();
        if (graph == null || graph.nodes()
            .isEmpty()) {
            panX = panY = 16;
            zoom = 1;
            return;
        }
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (Node node : graph.nodes()) {
            minX = Math.min(minX, node.x());
            minY = Math.min(minY, node.y());
            maxX = Math.max(maxX, node.x() + NODE_W);
            maxY = Math.max(maxY, node.y() + height(node));
        }
        zoom = Math.max(
            .25,
            Math.min(1, Math.min((getArea().width - 32) / (maxX - minX), (getArea().height - 32) / (maxY - minY))));
        panX = 16 - minX * zoom;
        panY = 16 - minY * zoom;
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> theme) {
        int width = getArea().width, height = getArea().height;
        Gui.drawRect(0, 0, width, height, 0xff12161a);
        Stencil.apply(0, 0, width, height, context);
        try {
            int step = Math.max(8, (int) (24 * zoom));
            for (int x = ((int) panX % step + step) % step; x < width; x += step)
                Gui.drawRect(x, 0, x + 1, height, 0xff22282e);
            for (int y = ((int) panY % step + step) % step; y < height; y += step)
                Gui.drawRect(0, y, width, y + 1, 0xff22282e);
            BlueprintGraph graph = document.graph();
            if (graph == null) {
                Minecraft.getMinecraft().fontRenderer
                    .drawString("Visual unavailable for this code", 10, 12, 0xffb0b3bb);
                Minecraft.getMinecraft().fontRenderer.drawString("The source is kept in Code view", 10, 26, 0xffb0b3bb);
                return;
            }
            if (graph.nodes()
                .isEmpty()) {
                Minecraft.getMinecraft().fontRenderer.drawString("Right-click to add a node", 10, 12, 0xffb0b3bb);
                Minecraft.getMinecraft().fontRenderer.drawString("Wires define execution order", 10, 26, 0xffb0b3bb);
                return;
            }
            GL11.glPushMatrix();
            try {
                GL11.glTranslated(panX, panY, 0);
                GL11.glScaled(zoom, zoom, 1);
                for (BlueprintGraph.Edge edge : graph.edges()) {
                    Node from = graph.node(edge.from), to = graph.node(edge.to);
                    wire(
                        from,
                        edge.pin,
                        (int) to.x(),
                        (int) to.y() + 28,
                        edge.pin == Pin.FALSE ? 0xffc4787d : 0xffa8bbb0);
                }
                if (wireFrom != null && graph.node(wireFrom) != null) wire(
                    graph.node(wireFrom),
                    wirePin,
                    (int) worldX(context.getMouseX()),
                    (int) worldY(context.getMouseY()),
                    0xffe5c77f);
                for (Node node : graph.nodes()) drawNode(node);
            } finally {
                GL11.glPopMatrix();
            }
        } finally {
            Stencil.remove();
        }
    }

    private void drawNode(Node node) {
        int x = (int) node.x(), y = (int) node.y(), h = height(node);
        Gui.drawRect(x - 1, y - 1, x + NODE_W + 1, y + h + 1, node.id.equals(selected) ? 0xffefa09a : 0xff59616b);
        Gui.drawRect(x, y, x + NODE_W, y + h, 0xff242a31);
        Gui.drawRect(x, y, x + NODE_W, y + 16, 0xff000000 | node.type.color);
        Minecraft.getMinecraft().fontRenderer.drawString(node.type.title, x + 5, y + 4, 0xffeeeeee);
        Minecraft.getMinecraft().fontRenderer.drawString(
            Minecraft.getMinecraft().fontRenderer
                .trimStringToWidth(node.clause(), NODE_W - (node.type == Type.IF ? 44 : 12)),
            x + 6,
            y + 24,
            0xffdddddd);
        if (!node.type.isTrigger()) pin(x, y + 28, 0xffc6c9ca);
        for (Pin output : Pin.values()) if (node.type.hasPin(output)) {
            int py = y + pinY(node, output);
            pin(x + NODE_W, py, output == Pin.FALSE ? 0xffc4787d : 0xffa8bbb0);
            if (node.type == Type.IF)
                Minecraft.getMinecraft().fontRenderer.drawString(output.label(), x + NODE_W - 36, py - 3, 0xffdddddd);
        }
    }

    private static void pin(int x, int y, int color) {
        Gui.drawRect(x - 4, y - 4, x + 4, y + 4, 0xff0b0e10);
        Gui.drawRect(x - 3, y - 3, x + 3, y + 3, color);
    }

    private void wire(Node from, Pin pin, int x2, int y2, int color) {
        int x1 = (int) from.x() + NODE_W, y1 = (int) from.y() + pinY(from, pin), lane = x2 - 12;
        if (x2 > x1 + 24) {
            horizontal(x1, lane, y1, color);
            vertical(lane, y1, y2, color);
        } else {
            int exit = x1 + 12, below = (int) from.y() + height(from) + 12;
            horizontal(x1, exit, y1, color);
            vertical(exit, y1, below, color);
            horizontal(exit, lane, below, color);
            vertical(lane, below, y2, color);
        }
        horizontal(lane, x2, y2, color);
    }

    private static void horizontal(int x1, int x2, int y, int color) {
        Gui.drawRect(Math.min(x1, x2), y - 1, Math.max(x1, x2) + 1, y + 1, color);
    }

    private static void vertical(int x, int y1, int y2, int color) {
        Gui.drawRect(x - 1, Math.min(y1, y2), x + 1, Math.max(y1, y2) + 1, color);
    }

    private static int height(Node node) {
        return node.type == Type.IF ? 88 : 48;
    }

    private static int pinY(Node node, Pin pin) {
        return node.type == Type.IF ? 28 + (pin.ordinal() - 1) * 24 : 28;
    }

    private double worldX(int x) {
        return (x - panX) / zoom;
    }

    private double worldY(int y) {
        return (y - panY) / zoom;
    }

    private static double clamp(double x) {
        return Math.max(-999000, Math.min(999000, x));
    }

    @Override
    public Result onMousePressed(int button) {
        if (!inputGate.getAsBoolean()) return Result.IGNORE;
        if (!editable.getAsBoolean() || document.graph() == null) return Result.STOP;
        getContext().focus(this);
        int x = getContext().getMouseX(), y = getContext().getMouseY();
        lastX = x;
        lastY = y;
        // Navigation works over cards and pins too, without changing selection or the document.
        if (button == 2) {
            panning = true;
            return Result.SUCCESS;
        }
        double wx = worldX(x), wy = worldY(y);
        List<Node> nodes = new ArrayList<>(
            document.graph()
                .nodes());
        try {
            for (int i = nodes.size() - 1; i >= 0; i--) {
                Node node = nodes.get(i);
                for (Pin pin : Pin.values()) if (node.type.hasPin(pin) && Math.abs(wx - node.x() - NODE_W) <= 8
                    && Math.abs(wy - node.y() - pinY(node, pin)) <= 8) {
                        if (button == 1) {
                            document.editGraph(graph -> graph.disconnect(node.id, pin));
                            changed.run();
                        } else if (button == 0) {
                            wireFrom = node.id;
                            wirePin = pin;
                            status.accept("Choose an input pin; Esc cancels. Right-click a pin to disconnect.");
                        }
                        return Result.SUCCESS;
                    }
                if (!node.type.isTrigger() && Math.abs(wx - node.x()) <= 8 && Math.abs(wy - node.y() - 28) <= 8) {
                    if (button == 1) {
                        document.editGraph(graph -> graph.disconnectInput(node.id));
                        changed.run();
                    } else if (button == 0 && wireFrom != null) {
                        document.editGraph(graph -> graph.connect(wireFrom, wirePin, node.id));
                        wireFrom = null;
                        changed.run();
                    } else if (button == 0) {
                        selected = node.id;
                        selectionChanged.run();
                    }
                    return Result.SUCCESS;
                }
                if (wx >= node.x() && wx <= node.x() + NODE_W && wy >= node.y() && wy <= node.y() + height(node)) {
                    selected = node.id;
                    selectionChanged.run();
                    if (button == 0 && wy < node.y() + 18) {
                        dragging = true;
                        moved = false;
                    }
                    return Result.SUCCESS;
                }
            }
            if (button == 1) addMenu.run();
            else {
                selected = null;
                selectionChanged.run();
            }
        } catch (IllegalArgumentException error) {
            status.accept(error.getMessage());
        }
        return Result.SUCCESS;
    }

    @Override
    public void onMouseDrag(int button, long elapsed) {
        if (!editable.getAsBoolean() || !inputGate.getAsBoolean()) return;
        int x = getContext().getMouseX(), y = getContext().getMouseY();
        if (dragging && selected() != null) {
            if (!moved && (x != lastX || y != lastY)) {
                document.checkpoint();
                moved = true;
            }
            Node node = selected();
            node.setPosition(clamp(node.x() + (x - lastX) / zoom), clamp(node.y() + (y - lastY) / zoom));
        } else if (panning) {
            panX += x - lastX;
            panY += y - lastY;
        }
        lastX = x;
        lastY = y;
    }

    @Override
    public boolean onMouseRelease(int button) {
        finishInteraction();
        // MUI2 offers release to the focused widget before clearing its pressed-button state.
        // Let that cleanup continue so a subsequent drag may use a different mouse button.
        return false;
    }

    @Override
    public boolean onMouseScroll(UpOrDown direction, int amount) {
        if (!isHovering() || !editable.getAsBoolean() || !inputGate.getAsBoolean()) return false;
        int x = getContext().getMouseX(), y = getContext().getMouseY();
        double wx = worldX(x), wy = worldY(y);
        zoom = Math.max(.25, Math.min(2, zoom * (direction.isUp() ? 1.1 : 1 / 1.1)));
        panX = x - wx * zoom;
        panY = y - wy * zoom;
        return true;
    }

    @Override
    public Result onKeyPressed(char character, int key) {
        if (!isFocused() || !inputGate.getAsBoolean()) return Result.IGNORE;
        if (key == Keyboard.KEY_ESCAPE && wireFrom != null) {
            wireFrom = null;
            return Result.SUCCESS;
        }
        if (!editable.getAsBoolean()) return Result.STOP;
        if (key == Keyboard.KEY_DELETE) deleteSelected();
        else if (Interactable.hasControlDown() && key == Keyboard.KEY_Z) undo(Interactable.hasShiftDown());
        else if (Interactable.hasControlDown() && key == Keyboard.KEY_Y) undo(true);
        else if (key == Keyboard.KEY_F) fit();
        else if (key == Keyboard.KEY_INSERT) addMenu.run();
        else return Result.IGNORE;
        return Result.SUCCESS;
    }
}
