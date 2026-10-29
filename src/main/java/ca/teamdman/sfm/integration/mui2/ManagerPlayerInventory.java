package ca.teamdman.sfm.integration.mui2;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.entity.player.InventoryPlayer;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.layout.IViewport;
import com.cleanroommc.modularui.api.layout.IViewportStack;
import com.cleanroommc.modularui.utils.HoveredWidgetList;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

/** One widget per bound canonical player slot; repartitioning changes coordinates only. */
final class ManagerPlayerInventory extends ParentWidget<ManagerPlayerInventory> {

    private static final Map<InventoryPlayer, ManagerInventoryLayout> PREFERENCES = new WeakHashMap<>();
    private final InventoryPlayer owner;
    private final boolean client;
    private final ItemSlot[] slots;
    private final StorageViewport viewport = new StorageViewport();
    private final Deque<ManagerInventoryLayout> undo = new ArrayDeque<>();
    private ManagerInventoryLayout layout;
    private boolean controls;
    private int selected, width = -1, height = -1;

    ManagerPlayerInventory(PanelSyncManager sync, InventoryPlayer owner) {
        this.owner = owner;
        client = sync.isClient();
        // Raw arrays may include Backhand equipment. Only the public binding knows
        // which contiguous player keys represent actual hotbar and storage slots.
        int slotCount = 0;
        while (slotCount < owner.mainInventory.length && sync.findSyncHandlerNullable("player", slotCount) != null)
            slotCount++;
        int storage = Math.max(0, slotCount - 9);
        ManagerInventoryLayout remembered = client ? PREFERENCES.get(owner) : null;
        layout = remembered != null && remembered.storage == storage ? remembered.copy()
            : new ManagerInventoryLayout(storage);
        name("player_inventory");
        String[] names = { "Player", "Left", "Right" };
        for (int i = 0; i < names.length; i++) {
            final int section = i;
            child(
                control(
                    () -> (selected == section ? "\u00a7f" : "\u00a77") + names[section] + " " + layout.count(section),
                    8 + i * 68,
                    0,
                    66,
                    () -> {
                        selected = section;
                        changed();
                    }));
        }
        child(
            control(() -> "Layout", 212, 0, 52, () -> controls = !controls)
                .addTooltipLine("Configure player storage views. Shift reveals these controls too."));
        child(
            IKey.dynamic(layoutSummary())
                .asWidget()
                .pos(8, 20)
                .height(10)
                .scale(.7f)
                .setEnabledIf(widget -> !showControls()));
        // Reserved control row means Shift never moves the slots beneath the pointer.
        String[] captions = { "Row -", "Row +", "Col -", "Col +" };
        for (int i = 0; i < captions.length; i++) {
            final boolean columns = i >= 2;
            final int delta = i % 2 == 0 ? -1 : 1;
            child(
                control(
                    () -> (layout.canResize(editSection(), columns, delta) ? "" : "\u00a78")
                        + captions[(columns ? 2 : 0) + (delta > 0 ? 1 : 0)],
                    8 + i * 43,
                    18,
                    41,
                    () -> {
                        if (!layout.canResize(editSection(), columns, delta)) return;
                        remember();
                        layout.resize(editSection(), columns, delta);
                        changed();
                    }).setEnabledIf(widget -> showControls())
                        .addTooltipLine(IKey.dynamic(() -> resizePreview(columns, delta))));
        }
        child(control(() -> "Split", 180, 18, 38, () -> preset(true)).setEnabledIf(widget -> showControls()));
        child(control(() -> "All", 220, 18, 30, () -> preset(false)).setEnabledIf(widget -> showControls()));
        child(control(() -> "Undo", 252, 18, 38, () -> {
            if (undo.isEmpty()) return;
            layout = undo.removeLast();
            changed();
        }).setEnabledIf(widget -> showControls()));
        child(viewport);
        slots = new ItemSlot[slotCount];
        for (int i = 0; i < slots.length; i++) {
            ItemSlot slot = new ItemSlot().syncHandler("player", i)
                .name("slot_" + i);
            slots[i] = slot;
            if (i < 9) child(slot);
            else viewport.child(slot);
        }
        addTooltipLine("Player storage only. Disk equipment stays in the manager above.");
    }

    private java.util.function.Supplier<String> layoutSummary() {
        return () -> layout.summary() + (isWide() ? "" : " | Choose a view above");
    }

    private boolean showControls() {
        return controls || client && shiftDown();
    }

    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    private static boolean shiftDown() {
        return net.minecraft.client.gui.GuiScreen.isShiftKeyDown();
    }

    private int editSection() {
        return selected == 2 ? 2 : 1;
    }

    private String resizePreview(boolean columns, int delta) {
        int section = editSection();
        if (!layout.canResize(section, columns, delta)) return "Cannot borrow more storage from Main";
        ManagerInventoryLayout preview = layout.copy();
        preview.resize(section, columns, delta);
        return (section == 1 ? "Left " : "Right ") + layout
            .count(section) + " -> " + preview.count(section) + "; Main " + layout.count(0) + " -> " + preview.count(0);
    }

    private void preset(boolean split) {
        remember();
        layout.preset(split);
        selected = 0;
        changed();
    }

    private void remember() {
        if (undo.size() == 8) undo.removeFirst();
        undo.addLast(layout.copy());
    }

    private void changed() {
        if (client) PREFERENCES.put(owner, layout.copy());
        width = -1;
        scheduleResize();
    }

    private boolean isWide() {
        return getArea().width >= 424;
    }

    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    private static boolean pointerHeld() {
        return org.lwjgl.input.Mouse.isButtonDown(0) || org.lwjgl.input.Mouse.isButtonDown(1);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (client && width < 0 && !pointerHeld()) scheduleResize();
    }

    @Override
    public void postResize() {
        super.postResize();
        int w = getArea().width, h = getArea().height;
        if (width == w && height == h) return;
        if (client && pointerHeld()) {
            width = -1;
            return;
        }
        width = w;
        height = h;
        int visibleHeight = Math.max(18, h - 60);
        int maxRows = 0;
        int leftWidth = layout.count(1) == 0 ? 0 : layout.columns(1) * 18 + 8;
        int rightWidth = layout.count(2) == 0 ? 0 : layout.columns(2) * 18 + 8;
        int leftX = (w - 162 - leftWidth - rightWidth) / 2;
        int mainX = isWide() ? leftX + leftWidth : (w - 162) / 2;
        int rightX = mainX + 162 + 8;
        viewport.pos(0, 34)
            .size(w, visibleHeight);
        for (int i = 9; i < slots.length; i++) {
            int section = layout.section(i), offset = layout.offset(i), cols = layout.columns(section);
            int x = isWide() ? section == 1 ? leftX : section == 2 ? rightX : mainX
                : section == selected ? (w - cols * 18) / 2 : w + 36;
            slots[i].pos(x + offset % cols * 18, offset / cols * 18);
            if (isWide() || section == selected) maxRows = Math.max(maxRows, (layout.count(section) + cols - 1) / cols);
        }
        viewport.getScrollArea()
            .getScrollY()
            .setScrollSize(maxRows * 18);
        for (int i = 0; i < Math.min(9, slots.length); i++) slots[i].pos(mainX + i * 18, h - 20);
        scheduleResize();
    }

    private static ButtonWidget<?> control(java.util.function.Supplier<String> title, int x, int y, int width,
        Runnable action) {
        return new ButtonWidget<>().pos(x, y)
            .size(width, 16)
            .background(ManagerConsoleTheme.BUTTON)
            .hoverBackground(ManagerConsoleTheme.HOVER)
            .overlay(IKey.dynamic(title))
            .onMousePressed(button -> {
                if (button != 0) return false;
                action.run();
                return true;
            });
    }

    /** Transparent scroll surfaces must participate in MUI2's viewport hit testing. */
    private static final class StorageViewport extends ScrollWidget<StorageViewport> {

        StorageViewport() {
            super(new VerticalScrollData());
            getScrollArea().getScrollY()
                .setScrollSpeed(18);
        }

        @Override
        public void postResize() {
            super.postResize();
            getScrollArea().getScrollY()
                .clamp(getScrollArea());
        }

        @Override
        public boolean canHover() {
            return true;
        }

        @Override
        public void getWidgetsAt(IViewportStack stack, HoveredWidgetList widgets, int x, int y) {
            if (widgets.peek() != this) return;
            int localX = stack.unTransformX(x, y) - getScrollX();
            int localY = stack.unTransformY(x, y) - getScrollY();
            if (!getScrollArea().isInsideScrollbarArea(localX, localY))
                IViewport.getChildrenAt(this, stack, widgets, x, y);
        }
    }
}
