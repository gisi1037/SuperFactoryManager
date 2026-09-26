package ca.teamdman.sfm.common.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.input.Keyboard;

import ca.teamdman.sfm.common.item.ItemLabelGun;
import ca.teamdman.sfm.common.label.LabelPositionHolder;
import ca.teamdman.sfm.common.net.PacketLabelGunAction;
import ca.teamdman.sfm.common.net.PacketSetLabel;
import ca.teamdman.sfm.common.net.SFMNetwork;
import ca.teamdman.sfm.common.util.BlockPos;

public class GuiLabelGun extends GuiScreen {

    private final EntityPlayer player;
    private final ItemStack labelGun;
    private final LabelPositionHolder labelHolder;

    private GuiTextField labelField;
    private GuiButton btnClear;
    private GuiButton btnPrune;
    private GuiButton btnCycleView;
    private GuiButton btnDone;

    private final List<GuiButton> labelButtons = new ArrayList<>();
    private static final int BASE_LABEL_BTN_ID = 100;

    public GuiLabelGun(EntityPlayer player, ItemStack labelGun) {
        this.player = player;
        this.labelGun = labelGun;
        this.labelHolder = LabelPositionHolder.from(labelGun);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        buttonList.clear();
        labelButtons.clear();
        Keyboard.enableRepeatEvents(true);

        int fieldWidth = 240;
        int fieldHeight = 20;
        int fieldX = width / 2 - fieldWidth / 2;
        int fieldY = 40;

        labelField = new GuiTextField(fontRendererObj, fieldX, fieldY, fieldWidth, fieldHeight);
        labelField.setMaxStringLength(64);
        labelField.setFocused(true);
        String active = ItemLabelGun.getActiveLabel(labelGun);
        labelField.setText(active);

        // Action buttons
        btnClear = new GuiButton(1, fieldX - 65, fieldY, 55, 20, "Clear");
        btnPrune = new GuiButton(2, fieldX + fieldWidth + 10, fieldY, 55, 20, "Prune");

        btnCycleView = new GuiButton(
            3,
            width / 2 - 120,
            height - 52,
            240,
            20,
            "View: " + ItemLabelGun.getViewMode(labelGun)
                .getDisplayName());

        btnDone = new GuiButton(0, width / 2 - 120, height - 28, 240, 20, "Done");

        buttonList.add(btnClear);
        buttonList.add(btnPrune);
        buttonList.add(btnCycleView);
        buttonList.add(btnDone);

        rebuildLabelButtons();
    }

    @SuppressWarnings("unchecked")
    private void rebuildLabelButtons() {
        for (GuiButton btn : labelButtons) {
            buttonList.remove(btn);
        }
        labelButtons.clear();

        String filter = labelField != null ? labelField.getText()
            .trim()
            .toLowerCase() : "";
        List<String> labels = new ArrayList<>(
            labelHolder.getAllLabels()
                .keySet());
        Collections.sort(labels);

        List<String> filtered = new ArrayList<>();
        for (String lbl : labels) {
            if (filter.isEmpty() || lbl.toLowerCase()
                .contains(filter)) {
                filtered.add(lbl);
            }
        }

        int btnWidth = 110;
        int btnHeight = 20;
        int paddingX = 6;
        int paddingY = 4;

        int availableWidth = width - 40;
        int cols = Math.max(1, availableWidth / (btnWidth + paddingX));
        int totalWidth = cols * (btnWidth + paddingX) - paddingX;
        int startX = (width - totalWidth) / 2;
        int startY = 70;

        String currentActive = ItemLabelGun.getActiveLabel(labelGun);

        for (int i = 0; i < filtered.size(); i++) {
            String label = filtered.get(i);
            Set<BlockPos> positions = labelHolder.getPositions(label);
            int count = positions.size();

            int col = i % cols;
            int row = i / cols;
            int x = startX + col * (btnWidth + paddingX);
            int y = startY + row * (btnHeight + paddingY);

            if (y + btnHeight > height - 60) {
                // Limit to visible area
                break;
            }

            boolean isActive = label.equals(currentActive);
            String display = (isActive ? EnumChatFormatting.AQUA + "> " : "") + label + " (" + count + ")";
            GuiButton btn = new GuiButton(BASE_LABEL_BTN_ID + i, x, y, btnWidth, btnHeight, display);
            labelButtons.add(btn);
            buttonList.add(btn);
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
            return;
        }

        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            onDone();
            return;
        }

        if (labelField.textboxKeyTyped(typedChar, keyCode)) {
            rebuildLabelButtons();
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        labelField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void onDone() {
        String newLabel = labelField.getText()
            .trim();
        ItemLabelGun.setActiveLabel(labelGun, newLabel);
        SFMNetwork.WRAPPER.sendToServer(new PacketSetLabel(newLabel));
        mc.displayGuiScreen(null);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            // Done
            onDone();
        } else if (button.id == 1) {
            // Clear
            ItemLabelGun.clearAll(labelGun);
            labelHolder.clear();
            SFMNetwork.WRAPPER.sendToServer(new PacketLabelGunAction(PacketLabelGunAction.ACTION_CLEAR));
            labelField.setText("");
            rebuildLabelButtons();
        } else if (button.id == 2) {
            // Prune
            ItemLabelGun.prune(labelGun);
            labelHolder.prune();
            SFMNetwork.WRAPPER.sendToServer(new PacketLabelGunAction(PacketLabelGunAction.ACTION_PRUNE));
            rebuildLabelButtons();
        } else if (button.id == 3) {
            // Cycle View Mode
            ItemLabelGun.LabelGunViewMode mode = ItemLabelGun.cycleViewMode(labelGun);
            SFMNetwork.WRAPPER.sendToServer(new PacketLabelGunAction(PacketLabelGunAction.ACTION_CYCLE_VIEW));
            btnCycleView.displayString = "View: " + mode.getDisplayName();
        } else if (button.id >= BASE_LABEL_BTN_ID) {
            // Clicked a label button
            int idx = button.id - BASE_LABEL_BTN_ID;
            String filter = labelField.getText()
                .trim()
                .toLowerCase();
            List<String> labels = new ArrayList<>(
                labelHolder.getAllLabels()
                    .keySet());
            Collections.sort(labels);
            List<String> filtered = new ArrayList<>();
            for (String lbl : labels) {
                if (filter.isEmpty() || lbl.toLowerCase()
                    .contains(filter)) {
                    filtered.add(lbl);
                }
            }
            if (idx >= 0 && idx < filtered.size()) {
                String chosen = filtered.get(idx);
                labelField.setText(chosen);
                onDone();
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        drawCenteredString(
            fontRendererObj,
            EnumChatFormatting.BOLD + "Super Factory Manager - Label Gun",
            width / 2,
            14,
            0xFFFFFF);

        String active = ItemLabelGun.getActiveLabel(labelGun);
        String activeInfo = "Active: " + (active.isEmpty() ? EnumChatFormatting.DARK_GRAY + "(none)"
            : EnumChatFormatting.AQUA + "\"" + active + "\"");
        drawCenteredString(fontRendererObj, activeInfo, width / 2, 26, 0xDDDDDD);

        labelField.drawTextBox();

        if (labelField.getText()
            .isEmpty() && !labelField.isFocused()) {
            drawString(
                fontRendererObj,
                EnumChatFormatting.ITALIC + "Search or type new label...",
                labelField.xPosition + 6,
                labelField.yPosition + 6,
                0x888888);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
