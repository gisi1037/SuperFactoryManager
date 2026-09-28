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
    private final java.util.Map<GuiButton, String> labelBtnMap = new java.util.HashMap<>();

    public GuiLabelGun(EntityPlayer player, ItemStack labelGun) {
        this.player = player;
        this.labelGun = labelGun;
        this.labelHolder = LabelPositionHolder.from(labelGun);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        buttonList.clear();
        labelButtons.clear();
        labelBtnMap.clear();
        Keyboard.enableRepeatEvents(true);

        int fieldWidth = 300;
        int fieldHeight = 20;
        int fieldX = width / 2 - fieldWidth / 2;
        int fieldY = 50;

        labelField = new GuiTextField(fontRendererObj, fieldX, fieldY, fieldWidth, fieldHeight);
        labelField.setMaxStringLength(256);
        labelField.setFocused(true);
        String active = ItemLabelGun.getActiveLabel(labelGun);
        labelField.setText(active);

        // Action buttons matching modern SFM
        btnClear = new GuiButton(1, width / 2 - 210, 50, 50, 20, "Clear");
        btnPrune = new GuiButton(2, width / 2 + 160, 50, 50, 20, "Prune");

        btnDone = new GuiButton(0, width / 2 - 150, height - 50, 300, 20, "Done");
        btnCycleView = new GuiButton(3, width / 2 - 100, height - 25, 200, 20, "Cycle label view");

        buttonList.add(btnClear);
        buttonList.add(btnPrune);
        buttonList.add(btnDone);
        buttonList.add(btnCycleView);

        rebuildLabelButtons();
    }

    @SuppressWarnings("unchecked")
    private void rebuildLabelButtons() {
        for (GuiButton btn : labelButtons) {
            buttonList.remove(btn);
        }
        labelButtons.clear();
        labelBtnMap.clear();

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
        int paddingY = 5;

        int availableWidth = width - 40;
        int cols = Math.max(1, availableWidth / (btnWidth + paddingX));
        int totalWidth = cols * (btnWidth + paddingX) - paddingX;
        int startX = (width - totalWidth) / 2;
        int startY = 80;

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
                break;
            }

            boolean isActive = label.equals(currentActive);
            String display = (isActive ? EnumChatFormatting.AQUA + "> " : "") + label + " (" + count + ")";
            GuiButton btn = new GuiButton(100 + i, x, y, btnWidth, btnHeight, display);
            labelButtons.add(btn);
            labelBtnMap.put(btn, label);
            buttonList.add(btn);
        }
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
        } else {
            super.keyTyped(typedChar, keyCode);
        }
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
        if (player.getHeldItem() != null && player.getHeldItem()
            .getItem() == labelGun.getItem()) {
            ItemLabelGun.setActiveLabel(player.getHeldItem(), newLabel);
        }
        mc.displayGuiScreen(null);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            onDone();
        } else if (button.id == 1) {
            ItemLabelGun.clearAll(labelGun);
            labelHolder.clear();
            SFMNetwork.WRAPPER.sendToServer(new PacketLabelGunAction(PacketLabelGunAction.ACTION_CLEAR));
            labelField.setText("");
            rebuildLabelButtons();
        } else if (button.id == 2) {
            ItemLabelGun.prune(labelGun);
            labelHolder.prune();
            SFMNetwork.WRAPPER.sendToServer(new PacketLabelGunAction(PacketLabelGunAction.ACTION_PRUNE));
            rebuildLabelButtons();
        } else if (button.id == 3) {
            ItemLabelGun.LabelGunViewMode mode = ItemLabelGun.cycleViewMode(labelGun);
            SFMNetwork.WRAPPER.sendToServer(new PacketLabelGunAction(PacketLabelGunAction.ACTION_CYCLE_VIEW));
            mc.displayGuiScreen(null);
        } else {
            String chosen = labelBtnMap.get(button);
            if (chosen != null) {
                labelField.setText(chosen);
                onDone();
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        labelField.drawTextBox();

        if (labelField.getText()
            .isEmpty()) {
            drawString(
                fontRendererObj,
                EnumChatFormatting.GRAY + "Search or enter new label",
                labelField.xPosition + 4,
                labelField.yPosition + 6,
                0x888888);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
