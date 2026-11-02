package ca.teamdman.sfm.client.gui.mui2;

import java.util.UUID;
import java.util.function.BooleanSupplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import ca.teamdman.sfm.client.gui.RecipeInspectorState;
import ca.teamdman.sfm.client.gui.RecipeSelectionHint;
import ca.teamdman.sfm.common.net.PacketStationRecipeReply;
import ca.teamdman.sfm.common.net.PacketStationRecipeRequest;
import ca.teamdman.sfm.common.net.SFMNetwork;

/** MUI2 auto-layout inspection surface. Copy is explicit; this component has no program-edit callbacks. */
public final class StudioRecipeInspector {

    private static final String[] MAPS = { "gt.recipe.macerator", "gt.recipe.metalbender", "gt.recipe.cuttingsaw" };
    private static final String[] NAMES = { "Macerator", "Bending machine", "Cutting machine" };
    private final RecipeInspectorState state;
    private String item = "minecraft:flint", metadata = "0", error = "";
    private int map;

    public StudioRecipeInspector(int x, int y, int z, int window, UUID session) {
        state = new RecipeInspectorState(x, y, z, window, session);
    }

    public void tick() {
        state.tick();
    }

    public void cancel() {
        state.cancel();
    }

    public void receive(PacketStationRecipeReply reply) {
        state.receive(reply);
    }

    public ModularPanel build(ModularPanel panel, Runnable focusAuthoring) {
        panel.onCloseAction(() -> {
            state.cancel();
            focusAuthoring.run();
        });
        panel.child(
            new TextWidget<>("Recipe inspector - read only").color(0xffefa09a)
                .pos(8, 8)
                .size(240, 12));
        panel.child(
            new StudioPanelDragWidget().pos(4, 4)
                .widthRelOffset(1f, -8)
                .height(20));
        Flow content = new Column().childPadding(4)
            .left(8)
            .right(8)
            .top(28)
            .bottom(8);
        Flow output = new Row().childPadding(3)
            .widthRel(1f)
            .height(18);
        output.child(
            new TextWidget<>("Output").width(40)
                .height(18));
        output.child(
            new TextFieldWidget().setMaxLength(128)
                .autoUpdateOnChange(true)
                .value(new StringValue.Dynamic(() -> item, value -> {
                    if (!item.equals(value)) {
                        item = value;
                        changed();
                    }
                }))
                .height(18)
                .expanded());
        output.child(
            new TextWidget<>("Meta").width(28)
                .height(18));
        output.child(
            new TextFieldWidget().setMaxLength(11)
                .autoUpdateOnChange(true)
                .value(new StringValue.Dynamic(() -> metadata, value -> {
                    if (!metadata.equals(value)) {
                        metadata = value;
                        changed();
                    }
                }))
                .width(54)
                .height(18));
        content.child(output);
        Flow actions = new Row().childPadding(3)
            .widthRel(1f)
            .height(18);
        actions.child(button(() -> NAMES[map], () -> !state.busy(), () -> {
            map = (map + 1) % MAPS.length;
            changed();
        }).expanded());
        actions.child(
            button(
                () -> "NEI hint",
                () -> RecipeSelectionHint.NEI.current(Minecraft.getMinecraft().theWorld) != null,
                () -> {
                    RecipeSelectionHint.Value hint = RecipeSelectionHint.NEI.current(Minecraft.getMinecraft().theWorld);
                    if (hint == null) return;
                    item = hint.item;
                    metadata = Integer.toString(hint.metadata);
                    for (int i = 0; i < MAPS.length; i++) if (MAPS[i].equals(hint.map)) map = i;
                    changed();
                }).width(70)
                    .addTooltipLine(
                        "In NEI, Ctrl+Shift+S remembers a focused GT output/map hint. No NBT outputs yet."));
        actions.child(button(() -> "Lookup", () -> !state.busy(), () -> {
            try {
                send(state.search(new RecipeSelectionHint.Value(item, Integer.parseInt(metadata), MAPS[map])));
            } catch (IllegalArgumentException invalid) {
                error = "Use a valid exact item ID and 32-bit metadata.";
            }
        }).width(58));
        content.child(actions);
        content.child(
            new TextWidget<>(
                IKey.dynamic(
                    () -> shortText(
                        error.isEmpty() ? state.status() : error,
                        Math.max(12, (panel.getArea().width - 24) / 6)))).widthRel(1f)
                            .height(12)
                            .color(0xffb0b3bb)
                            .addTooltipLine(IKey.dynamic(() -> error.isEmpty() ? state.status() : error)));
        content.child(
            new TextWidget<>("Server candidates (click to inspect)").widthRel(1f)
                .height(12));
        ListWidget<IWidget, ?> rows = new ListWidget<>().collapseDisabledChild()
            .widthRel(1f)
            .expanded()
            .background(StudioTheme.INSET);
        for (int i = 0; i < PacketStationRecipeReply.MAX_ROWS; i++) {
            final int index = i;
            rows.child(
                button(
                    () -> index < state.entries()
                        .size() ? (index + 1) + ". "
                            + state.entries()
                                .get(index).title
                            : "",
                    () -> !state.busy(),
                    () -> {
                        try {
                            send(state.select(index));
                        } catch (IllegalStateException invalid) {
                            error = invalid.getMessage();
                        }
                    }).widthRel(1f)
                        .setEnabledIf(
                            widget -> index < state.entries()
                                .size())
                        .overlay(
                            IKey.dynamic(
                                () -> index < state.entries()
                                    .size() ? shortText(
                                        (index + 1) + ". "
                                            + state.entries()
                                                .get(index).title,
                                        Math.max(12, (panel.getArea().width - 34) / 6)) : "")
                                .color(() -> state.selected(index) ? 0xffefa09a : 0xffe7e8eb))
                        .addTooltipLine(
                            IKey.dynamic(
                                () -> index < state.entries()
                                    .size()
                                        ? state.entries()
                                            .get(index).title
                                        : "")));
        }
        content.child(rows);
        content.child(
            new TextWidget<>("Candidate preview (partial)").widthRel(1f)
                .height(12));
        ListWidget<IWidget, ?> detail = new ListWidget<>().collapseDisabledChild()
            .widthRel(1f)
            .expanded()
            .background(StudioTheme.INSET);
        for (int i = 0; i < 16; i++) {
            final int index = i;
            detail.child(
                new TextWidget<>(
                    IKey.dynamic(
                        () -> shortText(state.detailLine(index), Math.max(12, (panel.getArea().width - 34) / 6))))
                            .widthRel(1f)
                            .height(12)
                            .setEnabledIf(widget -> index < state.detailLineCount())
                            .addTooltipLine(IKey.dynamic(() -> state.detailLine(index))));
        }
        content.child(detail);
        Flow footer = new Row().childPadding(3)
            .widthRel(1f)
            .height(18);
        footer.child(
            button(() -> "Copy inspection query", state::canCopy, () -> GuiScreen.setClipboardString(state.copyQuery()))
                .expanded()
                .addTooltipLine(
                    "Explicit clipboard action only. No program is changed; paste through the normal editor."));
        footer.child(button(() -> "Done", () -> true, panel::closeIfOpen).width(64));
        content.child(footer);
        panel.child(content);
        return panel;
    }

    private void changed() {
        state.clear();
        error = "";
    }

    private void send(PacketStationRecipeRequest request) {
        error = "";
        SFMNetwork.WRAPPER.sendToServer(request);
    }

    private static String shortText(String text, int length) {
        return text.length() > length ? text.substring(0, length - 3) + "..." : text;
    }

    private static ButtonWidget<?> button(java.util.function.Supplier<String> label, BooleanSupplier available,
        Runnable action) {
        return new ButtonWidget<>().height(18)
            .background(StudioTheme.BUTTON)
            .hoverBackground(StudioTheme.HOVER)
            .overlay(
                IKey.dynamic(label)
                    .color(() -> available.getAsBoolean() ? 0xffe7e8eb : 0xff777777))
            .onMousePressed(mouse -> {
                if (mouse != 0) return false;
                if (available.getAsBoolean()) action.run();
                return true;
            });
    }
}
