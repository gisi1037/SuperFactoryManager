package ca.teamdman.sfm.client.gui.mui2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.ClientGUI;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import ca.teamdman.sfm.common.item.ItemLabelGun;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.net.PacketLabelGunAction;
import ca.teamdman.sfm.common.net.PacketSetLabel;
import ca.teamdman.sfm.common.net.SFMNetwork;

/** Client-only label selection, using MUI2 focus, text editing, buttons and list layout. */
public final class Mui2LabelGun {

    private static final int PAGE_ROWS = 5;
    private final EntityPlayer player;
    private final ca.teamdman.sfm.common.net.LabelGunAccess.Token token;
    private boolean stale;

    private ca.teamdman.sfm.common.net.LabelGunAccess.Token token() {
        if (token == null || stale) return null;
        ca.teamdman.sfm.common.net.LabelGunAccess.Token current = ca.teamdman.sfm.client.gui.ClientLabelGunSession
            .current(player);
        if (current == null || !token.session.equals(current.session)) {
            stale = true;
            return null;
        }
        return token;
    }

    private String readiness() {
        if (token() != null) return "Search or enter a label";
        return stale ? "Label gun changed. Close and reopen." : "Sync pending. Close and reopen.";
    }

    private final ItemStack gun;
    private final LabelPositionHolder labels;
    private final List<String> filtered = new ArrayList<>();
    private String query;
    private int page;
    private TextFieldWidget field;

    private Mui2LabelGun(EntityPlayer player, ItemStack gun) {
        this.player = player;
        token = ca.teamdman.sfm.client.gui.ClientLabelGunSession.current(player);
        this.gun = gun;
        labels = LabelPositionHolder.from(gun);
        query = ItemLabelGun.getActiveLabel(gun);
        filter(query);
    }

    public static ModularScreen build(EntityPlayer player, ItemStack gun) {
        return new Mui2LabelGun(player, gun).screen();
    }

    public static void open(EntityPlayer player, ItemStack gun) {
        ClientGUI.open(build(player, gun));
    }

    private ModularScreen screen() {
        ModularPanel panel = ModularPanel.defaultPanel("sfm_label_gun", 312, 228);
        panel.child(
            new TextWidget<>("Label gun").pos(12, 8)
                .size(288, 14));
        panel.child(
            new TextWidget<>(IKey.dynamic(this::readiness)).pos(12, 25)
                .size(288, 12));
        field = new TextFieldWidget() {

            @Override
            public Result onKeyPressed(char character, int key) {
                if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) {
                    select(getText());
                    return Result.SUCCESS;
                }
                return super.onKeyPressed(character, key);
            }
        }.setFocusOnGuiOpen(true)
            .setMaxLength(256)
            .autoUpdateOnChange(true)
            .value(new StringValue.Dynamic(() -> query, this::filter));
        panel.child(
            field.pos(12, 40)
                .size(288, 18));
        ListWidget<IWidget, ?> list = new ListWidget<>().pos(12, 65)
            .size(288, 104);
        for (int i = 0; i < PAGE_ROWS; i++) {
            final int row = i;
            list.child(
                new ButtonWidget<>().size(284, 20)
                    .overlay(
                        IKey.dynamic(() -> Minecraft.getMinecraft().fontRenderer.trimStringToWidth(labelAt(row), 274)))
                    .addTooltipLine(IKey.dynamic(() -> labelAt(row)))
                    .setEnabledIf(widget -> token() != null && page * PAGE_ROWS + row < filtered.size())
                    .onMousePressed(button -> {
                        int index = page * PAGE_ROWS + row;
                        if (button != 0 || index >= filtered.size()) return false;
                        select(filtered.get(index));
                        return true;
                    }));
        }
        panel.child(list);
        panel.child(button("Previous", 12, 174, 62, () -> page = Math.max(0, page - 1)));
        panel.child(
            new TextWidget<>(
                IKey.dynamic(() -> (page + 1) + " / " + Math.max(1, (filtered.size() + PAGE_ROWS - 1) / PAGE_ROWS)))
                    .pos(80, 178)
                    .size(154, 12));
        panel.child(
            button(
                "Next",
                240,
                174,
                60,
                () -> page = Math.min(Math.max(0, (filtered.size() - 1) / PAGE_ROWS), page + 1)));
        panel.child(button("Clear", 12, 199, 48, () -> {
            if (token() == null) return;
            ItemLabelGun.clearAll(gun);
            labels.clear();
            sendAction(PacketLabelGunAction.ACTION_CLEAR);
            field.setText("");
            filter("");
        }).setEnabledIf(widget -> token() != null));
        panel.child(button("Prune", 64, 199, 48, () -> {
            if (token() == null) return;
            ItemLabelGun.prune(gun);
            labels.prune();
            sendAction(PacketLabelGunAction.ACTION_PRUNE);
            filter(field.getText());
        }).setEnabledIf(widget -> token() != null));
        panel.child(button("Cycle view", 116, 199, 88, () -> {
            if (token() == null) return;
            ItemLabelGun.cycleViewMode(gun);
            sendAction(PacketLabelGunAction.ACTION_CYCLE_VIEW);
            ClientGUI.close();
        }).setEnabledIf(widget -> token() != null));
        panel
            .child(button("Done", 208, 199, 92, () -> select(field.getText())).setEnabledIf(widget -> token() != null));
        return new ModularScreen("superfactorymanager", panel);
    }

    private String labelAt(int row) {
        int index = page * PAGE_ROWS + row;
        if (index >= filtered.size()) return "";
        String label = filtered.get(index);
        return (label.equals(ItemLabelGun.getActiveLabel(gun)) ? "> " : "") + label
            + " ("
            + labels.getPositions(label)
                .size()
            + ")";
    }

    private void filter(String value) {
        query = value;
        page = 0;
        filtered.clear();
        String search = value.trim()
            .toLowerCase(Locale.ROOT);
        for (String label : labels.getAllLabels()
            .keySet())
            if (label.toLowerCase(Locale.ROOT)
                .contains(search)) filtered.add(label);
        Collections.sort(filtered);
    }

    private void select(String value) {
        if (token() == null) return;
        String label = value.trim();
        ItemLabelGun.setActiveLabel(gun, label);
        SFMNetwork.WRAPPER.sendToServer(new PacketSetLabel(label, token()));
        // Server reply/inventory synchronization updates the authoritative held binding.
        ClientGUI.close();
    }

    private void sendAction(int action) {
        SFMNetwork.WRAPPER.sendToServer(new PacketLabelGunAction(action, token()));
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
