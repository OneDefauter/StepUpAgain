package com.nottoomanyitems.stepup;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class StepUpConfigScreen extends Screen {
    private final Screen parent;
    private Button toggleButton;

    public StepUpConfigScreen(Screen parent) {
        super(Component.translatable("title.stepup.config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int buttonTop = Math.max(this.height / 4 + 40, getTextBottom() + 18);

        toggleButton = addRenderableWidget(Button.builder(buildToggleLabel(), button -> {
            StepUpConfig.setVanillaAutoJumpModeEnabled(!StepUpConfig.isVanillaAutoJumpModeEnabled());
            toggleButton.setMessage(buildToggleLabel());
        }).bounds(centerX - 100, buttonTop, 200, 20)
                .tooltip(Tooltip.create(Component.translatable("mod.stepup.config.allowvanillamode.tooltip")))
                .build());

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(centerX - 100, buttonTop + 52, 200, 20)
                .build());
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        guiGraphics.centeredText(font, title, centerX, 20, 0xFFFFFF);

        int y = 44;
        y = drawWrappedCentered(
                guiGraphics,
                Component.translatable("mod.stepup.config.allowvanillamode.tooltip.short"),
                centerX,
                y,
                0xA0A0A0
        ) + 8;

        drawWrappedCentered(
                guiGraphics,
                Component.translatable("mod.stepup.config.togglehint"),
                centerX,
                y,
                0xA0A0A0
        );
    }

    private Component buildToggleLabel() {
        return Component.translatable("mod.stepup.config.allowvanillamode")
                .append(Component.literal(": "))
                .append(Component.translatable(
                        StepUpConfig.isVanillaAutoJumpModeEnabled()
                                ? "mod.stepup.enabled"
                                : "mod.stepup.disabled"
                ));
    }

    private int getTextBottom() {
        int textWidth = getTextWidth();
        int y = 44;
        y += font.split(Component.translatable("mod.stepup.config.allowvanillamode.tooltip.short"), textWidth).size() * 10;
        y += 8;
        y += font.split(Component.translatable("mod.stepup.config.togglehint"), textWidth).size() * 10;
        return y;
    }

    private int drawWrappedCentered(GuiGraphicsExtractor guiGraphics, Component text, int centerX, int y, int color) {
        for (FormattedCharSequence line : font.split(text, getTextWidth())) {
            guiGraphics.text(font, line, centerX - font.width(line) / 2, y, color, false);
            y += 10;
        }

        return y;
    }

    private int getTextWidth() {
        return Math.min(280, Math.max(120, width - 40));
    }
}
