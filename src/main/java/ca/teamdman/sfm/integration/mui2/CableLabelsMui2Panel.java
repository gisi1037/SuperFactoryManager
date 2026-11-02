package ca.teamdman.sfm.integration.mui2;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

import net.minecraft.entity.player.EntityPlayer;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.value.sync.GenericSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.sizer.Unit;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import ca.teamdman.sfm.common.net.CableLabelData;
import ca.teamdman.sfm.common.net.CableLabelData.Action;
import ca.teamdman.sfm.common.net.CableLabelData.Mode;
import ca.teamdman.sfm.common.net.CableLabelData.Request;
import ca.teamdman.sfm.common.net.CableLabelData.View;
import ca.teamdman.sfm.common.net.ServerUiTasks;
import ca.teamdman.sfm.common.network.CableLabelEditor;

/** Paged tool window with bounded server actions and no inventory/provider interaction. */
final class CableLabelsMui2Panel {

    private CableLabelsMui2Panel() {}

    static ModularPanel build(EntityPlayer owner, CableLabelEditor editor, PanelSyncManager sync) {
        GenericSyncValue<View, ?> value = GenericSyncValue.builder(View.class)
            .getter(() -> editor == null ? null : editor.view())
            .equalsDefault()
            .copyImmutable()
            .nullable()
            .serializer((buffer, view) -> view.write(buffer))
            .deserializer(View::read)
            .build();
        sync.syncValue("labels", value);
        AtomicBoolean queued = new AtomicBoolean();
        sync.registerSyncedAction("edit_labels", false, true, buffer -> {
            final Request request;
            try {
                request = Request.read(buffer);
            } catch (RuntimeException malformed) {
                return;
            }
            if (editor == null || !queued.compareAndSet(false, true)) return;
            ModularContainer container = sync.getContainer();
            if (!ServerUiTasks.enqueue(() -> {
                try {
                    if (owner.openContainer == container && container instanceof CableLabelsMui2Container
                        && ((CableLabelsMui2Container) container).editor == editor
                        && editor.canInteract(owner)) editor.act(request);
                } finally {
                    queued.set(false);
                }
            })) queued.set(false);
        });
        sync.onServerTick(() -> { if (owner.openContainer == sync.getContainer() && editor != null) editor.tick(); });
        long[] pendingRevision = { 0 };
        int[] pendingTicks = { 0 };
        sync.onClientTick(() -> {
            View view = value.getValue();
            if (pendingRevision[0] != 0
                && (view != null && view.revision != pendingRevision[0] || ++pendingTicks[0] > 100))
                pendingRevision[0] = 0;
        });
        BooleanSupplier idle = () -> value.getValue() != null && pendingRevision[0] == 0;
        java.util.function.Consumer<Request> send = request -> {
            if (!sync.isClient() || !idle.getAsBoolean()) return;
            pendingRevision[0] = request.revision;
            pendingTicks[0] = 0;
            sync.callSyncedAction("edit_labels", buffer -> request.write(buffer));
        };
        java.util.function.BiConsumer<Action, String> action = (kind, text) -> {
            View view = value.getValue();
            if (view != null) send.accept(new Request(view.session, view.revision, kind, 0, text));
        };
        ModularPanel panel = new ModularPanel(CableLabelsMui2Factory.NAME).size(540, 390)
            .background(ManagerConsoleTheme.SHELL)
            .resizeableOnDrag(false);
        panel.width(
            () -> Math.max(
                300,
                Math.min(
                    540,
                    panel.getScreen()
                        .getScreenArea().width - 12)),
            Unit.Measure.PIXEL);
        panel.height(
            () -> Math.max(
                216,
                Math.min(
                    390,
                    panel.getScreen()
                        .getScreenArea().height - 12)),
            Unit.Measure.PIXEL);
        panel.child(
            new TextWidget<>(IKey.dynamic(() -> value.getValue() == null ? "Cable labels" : value.getValue().title))
                .pos(8, 8)
                .right(8)
                .height(12)
                .scale(.8f)
                .color(0xffefa09a));
        panel.child(
            button("Refresh", idle, () -> action.accept(Action.REFRESH, "")).pos(8, 27)
                .size(68, 16));
        panel.child(
            button("Managers", idle, () -> action.accept(Action.MANAGERS, "")).pos(80, 27)
                .size(76, 16));
        panel.child(
            button("Close", () -> true, panel::closeIfOpen).right(8)
                .top(27)
                .size(60, 16));
        StringValue search = new StringValue(""), label = new StringValue("");
        panel.child(
            new TextFieldWidget().value(search)
                .setMaxLength(64)
                .pos(8, 49)
                .right(77)
                .height(16)
                .addTooltipLine("Search cached coordinates, block IDs and labels. Selected blocks stay selected."));
        panel.child(
            button("Search", idle, () -> action.accept(Action.SEARCH, search.getStringValue())).right(8)
                .top(49)
                .size(64, 16));
        panel.child(
            new TextFieldWidget().value(label)
                .setMaxLength(256)
                .pos(8, 72)
                .right(145)
                .height(16)
                .addTooltipLine(
                    "Exact SFML label. Add/remove affects only the selected disk's selected blocks; no source renaming."));
        BooleanSupplier editing = () -> idle.getAsBoolean() && value.getValue().mode == Mode.BLOCKS
            && value.getValue().selected > 0;
        panel.child(
            button("Add label", editing, () -> action.accept(Action.ADD, label.getStringValue())).right(77)
                .top(72)
                .size(64, 16));
        panel.child(
            button("Remove", editing, () -> action.accept(Action.REMOVE, label.getStringValue())).right(8)
                .top(72)
                .size(64, 16));
        ListWidget<IWidget, ?> list = new ListWidget<>().pos(8, 95)
            .right(8)
            .bottom(68);
        for (int index = 0; index < CableLabelData.PAGE_SIZE; index++) {
            final int row = index;
            ButtonWidget<?> button = button("", () -> idle.getAsBoolean() && row < value.getValue().rows.size(), () -> {
                View view = value.getValue();
                if (view != null) send.accept(new Request(view.session, view.revision, Action.ROW, row, ""));
            }).widthRel(1f)
                .height(32)
                .marginBottom(2);
            button.overlay(IKey.dynamic(() -> {
                View view = value.getValue();
                if (view == null || row >= view.rows.size()) return "";
                return rowText(
                    (view.rows.get(row).selected ? "[x] " : "[ ] ") + view.rows.get(row).text,
                    panel.getArea().width);
            })
                .scale(.75f));
            button.addTooltipLine(IKey.dynamic(() -> {
                View view = value.getValue();
                return view == null || row >= view.rows.size() ? "" : view.rows.get(row).text;
            }));
            button.setEnabledIf(widget -> value.getValue() != null && row < value.getValue().rows.size());
            list.child(button);
        }
        panel.child(list);
        panel.child(
            button("<", idle, () -> action.accept(Action.PREVIOUS, "")).left(8)
                .bottom(46)
                .size(24, 16));
        panel.child(
            button(">", idle, () -> action.accept(Action.NEXT, "")).left(36)
                .bottom(46)
                .size(24, 16));
        panel.child(
            button("Clear selection", idle, () -> action.accept(Action.CLEAR, "")).right(8)
                .bottom(46)
                .size(96, 16));
        panel.child(new TextWidget<>(IKey.dynamic(() -> {
            View view = value.getValue();
            return view == null ? "Waiting for server..."
                : "Page " + (view.page / CableLabelData.PAGE_SIZE + 1)
                    + "/"
                    + Math.max(1, (view.total + CableLabelData.PAGE_SIZE - 1) / CableLabelData.PAGE_SIZE)
                    + " | selected "
                    + view.selected;
        })).left(65)
            .right(108)
            .bottom(48)
            .height(12)
            .scale(.7f)
            .color(0xffb7bcc5));
        panel.child(
            new TextWidget<>(
                IKey.dynamic(() -> value.getValue() == null ? "Opening label session..." : value.getValue().message))
                    .left(8)
                    .right(8)
                    .bottom(8)
                    .height(32)
                    .maxWidth(280)
                    .scale(.75f)
                    .textAlign(Alignment.TopLeft)
                    .color(0xffd3d6dc));
        return panel;
    }

    private static ButtonWidget<?> button(String title, BooleanSupplier enabled, Runnable action) {
        return new ButtonWidget<>().background(ManagerConsoleTheme.BUTTON)
            .hoverBackground(ManagerConsoleTheme.HOVER)
            .overlay(
                IKey.str(title)
                    .scale(.8f))
            .onMousePressed(mouse -> {
                if (mouse != 0) return false;
                if (enabled.getAsBoolean()) action.run();
                return true;
            });
    }

    private static String rowText(String text, int panelWidth) {
        int columns = Math.max(20, (panelWidth - 36) / 5);
        StringBuilder display = new StringBuilder();
        for (String line : text.split("\n", -1)) {
            if (display.length() != 0) display.append('\n');
            display.append(line.length() <= columns ? line : line.substring(0, columns - 3) + "...");
        }
        return display.toString();
    }
}
